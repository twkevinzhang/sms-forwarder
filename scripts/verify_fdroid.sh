#!/usr/bin/env bash
# Build the checked-in recipe in F-Droid's official, pinned Linux container.
set -euo pipefail

project_root=$(git rev-parse --show-toplevel)
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

git clone --quiet https://gitlab.com/fdroid/fdroidserver.git "$stage/fdroidserver"
git -C "$stage/fdroidserver" checkout --quiet 5806784d0d43bed7b938fd4b327162da68927060
git clone --quiet --depth 1 --filter=blob:none --sparse \
  https://gitlab.com/fdroid/fdroiddata.git "$stage/upstream"
git -C "$stage/upstream" sparse-checkout set config
mkdir -p "$stage/data/metadata"
cp -R "$stage/upstream/config" "$stage/data/config"
cp "$project_root/fdroid/metadata/com.twkevinzhang.smsforwarder.yml" "$stage/data/metadata/"
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
sdkmanager 'platforms;android-36' 'build-tools;36.0.0'
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
test -s unsigned/com.twkevinzhang.smsforwarder_1.apk
fdroid scanner --exit-code unsigned/com.twkevinzhang.smsforwarder_1.apk
sha256sum unsigned/com.twkevinzhang.smsforwarder_1.apk
/opt/android-sdk/build-tools/36.0.0/aapt dump badging unsigned/com.twkevinzhang.smsforwarder_1.apk
CONTAINER

docker run --rm --platform linux/amd64 --cpus 4 --memory 6g \
  --name "$container_name" \
  -v "$stage:/build" \
  -v "$stage/fdroidserver:/home/vagrant/fdroidserver:ro" \
  --entrypoint /bin/bash \
  "$image" \
  -l /build/build.sh
