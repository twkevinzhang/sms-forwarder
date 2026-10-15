#!/usr/bin/env bash
# Build the checked-in recipe in F-Droid's official, pinned Linux container.
set -euo pipefail

project_root=$(git rev-parse --show-toplevel)
upstream_apk=""
if [[ $# -gt 0 ]]; then
  [[ $# -eq 2 && "$1" == --upstream-apk ]] || { echo 'Usage: verify_fdroid.sh [--upstream-apk FILE]' >&2; exit 2; }
  upstream_apk=$(python3 -c 'import pathlib,sys; print(pathlib.Path(sys.argv[1]).resolve(strict=True))' "$2")
fi
stage=$(mktemp -d)
container_name="sms-forwarder-fdroid-${GITHUB_RUN_ID:-local}"
image=registry.gitlab.com/fdroid/fdroidserver@sha256:9cb68105642ca4e7b295f0ceab10f069f5b3247dc18fa7c36046e9d81aa469a8
cleanup() {
  docker rm -f "$container_name" >/dev/null 2>&1 || true
  # Files created by container root must be removable by the Linux runner.
  if [[ -d "$stage/data" ]] && docker image inspect "$image" >/dev/null 2>&1; then
    docker run --rm -v "$stage/data:/data" --entrypoint /bin/chown \
      "$image" -R "$(id -u):$(id -g)" /data >/dev/null 2>&1 || true
  fi
  rm -rf "$stage"
}
trap cleanup EXIT
trap 'exit 130' INT
trap 'exit 143' TERM


# Mount only explicitly selected public inputs, never the working tree containing
# ignored production keystores or local signing credentials.
python3 - "$project_root" "$stage/public-source" <<'PUBLIC_SOURCE'
from pathlib import Path
import shutil
import sys

root, destination = map(Path, sys.argv[1:])
allowed = {
    Path('scripts/release.py'),
    Path('scripts/apk_release.py'),
    Path('app/build.gradle.kts'),
    Path('fdroid/signing-certificate.sha256'),
}
for relative in allowed:
    source = root / relative
    if any(part.is_symlink() for part in (source, *source.parents)):
        raise SystemExit('Public verification inputs must not be symlinks')
    if not source.is_file():
        raise SystemExit('A required public verification input is missing')
    target = destination / relative
    target.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(source, target)
actual = {path.relative_to(destination) for path in destination.rglob('*') if path.is_file()}
if actual != allowed or any(path.is_symlink() for path in destination.rglob('*')):
    raise SystemExit('Public verification staging contains unexpected paths')
print('Public verification staging checked: four allowlisted files; no working-tree mount')
PUBLIC_SOURCE

git clone --quiet https://gitlab.com/fdroid/fdroidserver.git "$stage/fdroidserver"
git -C "$stage/fdroidserver" checkout --quiet 5806784d0d43bed7b938fd4b327162da68927060
git clone --quiet --depth 1 --filter=blob:none --sparse \
  https://gitlab.com/fdroid/fdroiddata.git "$stage/upstream"
git -C "$stage/upstream" sparse-checkout set config
mkdir -p "$stage/data/metadata"
cp -R "$stage/upstream/config" "$stage/data/config"
if [[ -n "$upstream_apk" ]]; then
  sdk_root=${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}
  [[ -n "$sdk_root" ]] || { echo 'Android SDK path is required' >&2; exit 1; }
  source_sha=$(git rev-parse HEAD)
  python3 "$project_root/scripts/apk_release.py" metadata \
    --apksigner "$sdk_root/build-tools/34.0.0/apksigner" \
    --aapt "$sdk_root/build-tools/36.0.0/aapt" \
    --signed "$upstream_apk" --source-sha "$source_sha" \
    --output "$stage/data/metadata/com.twkevinzhang.smsforwarder.yml"
  # The signed reference is not public until this pre-publication check succeeds.
  # Use the local reference below rather than downloading the future Binaries URL.
  python3 - "$stage/data/metadata/com.twkevinzhang.smsforwarder.yml" <<'PY'
from pathlib import Path
import sys
p = Path(sys.argv[1])
p.write_text(''.join(x for x in p.read_text().splitlines(True) if not x.startswith('Binaries:')))
PY
  cp "$upstream_apk" "$stage/upstream.apk"
else
  cp "$project_root/fdroid/metadata/com.twkevinzhang.smsforwarder.yml" "$stage/data/metadata/"
fi
cat > "$stage/data/config.yml" <<'CONFIG'
repo_url: https://example.invalid/fdroid/repo
sdk_path: /opt/android-sdk
CONFIG
cat > "$stage/build.sh" <<'CONTAINER'
set -euo pipefail
export PATH=/home/vagrant/fdroidserver:$PATH
export PYTHONPATH=/home/vagrant/fdroidserver
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export GRADLE_OPTS='-Dorg.gradle.workers.max=2 -Dorg.gradle.daemon=false'
# The pinned tool checkout is mounted read-only from the runner's UID.
git config --global --add safe.directory /home/vagrant/fdroidserver
sdkmanager 'platforms;android-36' 'build-tools;36.0.0' 'build-tools;34.0.0'
cd /build/data
fdroid readmeta
fdroid rewritemeta com.twkevinzhang.smsforwarder
fdroid lint com.twkevinzhang.smsforwarder
# Initialize the source cache before fdroid build reads SOURCE_DATE_EPOCH.
python3 - <<'PY'
from pathlib import Path
import yaml
from fdroidserver import common
common.read_config()
recipe = yaml.safe_load(Path('metadata/com.twkevinzhang.smsforwarder.yml').read_text())
common.getvcs('git', recipe['Repo'], 'build/com.twkevinzhang.smsforwarder').gotorevision(recipe['Builds'][-1]['commit'])
PY
fdroid build --stop com.twkevinzhang.smsforwarder
code=$(python3 -c "import yaml; print(yaml.safe_load(open('metadata/com.twkevinzhang.smsforwarder.yml'))['Builds'][-1]['versionCode'])")
apk="unsigned/com.twkevinzhang.smsforwarder_${code}.apk"
test -s "$apk"
fdroid scanner --exit-code "$apk"
sha256sum "$apk"
/opt/android-sdk/build-tools/36.0.0/aapt dump badging "$apk"
if [[ -f /build/upstream.apk ]]; then
  if command -v apksigcopier >/dev/null 2>&1; then
    echo 'Signature copier CLI: available'
  else
    echo 'Signature copier CLI: absent; using pinned F-Droid Python API'
  fi
  python3 - <<'COPIER_API'
try:
    from fdroidserver import apksigcopier
except ImportError:
    raise SystemExit('Pinned signature copier API: import failed')
if not callable(getattr(apksigcopier, 'do_copy', None)):
    raise SystemExit('Pinned signature copier API: do_copy unavailable')
print('Pinned signature copier API: available')
COPIER_API
  cat > /tmp/sms-apksigcopier <<'COPIER_WRAPPER'
#!/usr/bin/env python3
import sys
from fdroidserver import apksigcopier

if len(sys.argv) != 5 or sys.argv[1] != 'copy':
    raise SystemExit('Signature copier accepts only copy with three file paths')
apksigcopier.do_copy(sys.argv[2], sys.argv[3], sys.argv[4],
                     v1_only=False, exclude=apksigcopier.exclude_meta)
COPIER_WRAPPER
  chmod 700 /tmp/sms-apksigcopier
  python3 /upstream/scripts/apk_release.py verify-rb \
    --copier /tmp/sms-apksigcopier \
    --apksigner /opt/android-sdk/build-tools/34.0.0/apksigner \
    --aapt /opt/android-sdk/build-tools/36.0.0/aapt \
    --unsigned "$apk" --signed /build/upstream.apk
fi
CONTAINER

docker run --rm --platform linux/amd64 --cpus 4 --memory 6g \
  --name "$container_name" \
  -v "$stage:/build" \
  -v "$stage/fdroidserver:/home/vagrant/fdroidserver:ro" \
  -v "$stage/public-source:/upstream:ro" \
  --entrypoint /bin/bash \
  "$image" \
  -l /build/build.sh
