# R8 and Reproducible Builds — review candidate

Status: owner-approved release preparation. On 2026-10-15 the owner supplied a
production keystore and local signing configuration, plus the public certificate
pin, and authorized commit/publication of the reviewer changes. Local key access
and its match to the public pin have been verified without disclosing secrets.
GitHub signing secrets are configured; the release workflow will validate them
when signing. Production RB results are recorded separately after execution.
Do not use the Android debug key for production.

## Proposed release

- Candidate: `1.0.1`, version code `2`, with R8 enabled for release builds.
- Keep the published `v1.0.0` tag and source release immutable.
- Listing approval now pins the owner-authorized 1.0.1 version and changelog digest.
- Continue using AGP/AndroidX consumer rules; no blanket keep or warning suppression.
- Retain resource shrinking's current setting; this change enables code R8 only.

```text
owner review → dedicated production key + public certificate pin
  → reviewed source on main → immutable v1.0.1 tag
  → unsigned R8 APK → upstream signing → independent official-container rebuild
  → copy signature + verify + byte comparison
  → publish signed APK and SHA-256 → render final F-Droid YAML → update MR
```

## Changes to inspect

- `app/build.gradle.kts`: R8 and candidate version.
- `fastlane/metadata/android/{en-US,zh-TW}/changelogs/2.txt`: candidate notes.
- `scripts/apk_release.py`: explicit signing, certificate/identity checks, signature
  copying verification, and metadata generation. It never creates keys or publishes.
- `.github/workflows/release.yml`: sign only approved versions; independent RB
  verification must succeed before the publish job can upload an installable APK.
- `scripts/verify_fdroid.sh --upstream-apk FILE`: render the candidate recipe with
  its real tag SHA, rebuild in the pinned official container and verify RB.
- `fdroid/metadata/com.twkevinzhang.smsforwarder.yml.in`: review template containing
  `Binaries` and `AllowedAPKSigningKeys`. It is deliberately not a ready-to-submit
  YAML file. No invented key fingerprint or future commit is inserted into the
  existing `1.0.0` submission recipe.

## Production key handling

Keep the production keystore and an encrypted backup outside the repository.
The same key must be retained for future upgrades. Review backup/recovery and
access before publishing the first signed release.

Commit only the public certificate's 64-character SHA-256 fingerprint in
`fdroid/signing-certificate.sha256`. This file is not the private key. The tool
rejects missing pins, placeholders, different signers and multiple signers.

GitHub Actions uses these securely configured repository secrets:

- `SMS_KEYSTORE_BASE64`: base64-encoded keystore (base64 is not encryption).
- `SMS_KEY_ALIAS`
- `SMS_KEYSTORE_PASSWORD`
- `SMS_KEY_PASSWORD`

For local signing, supply `SMS_KEYSTORE_PATH` instead of `SMS_KEYSTORE_BASE64`.
Passwords are passed to `apksigner` through environment references, not command
arguments. Do not paste secrets into chat, logs or tracked files. The transient
decoded keystore is mode `0600` and removed when signing exits. Keys and signing
properties are ignored by Git as an extra precaution.

Build Tools 34.0.0 is explicitly used for `apksigner`, following the F-Droid
documented compatibility issue with newer signers. Compilation still uses SDK
and Build Tools 36.0.0. Signature schemes v2/v3 are enabled, v1/v4 disabled; the
app's minimum SDK is 26. Review these pinned tools again before actual release.

## Verification and final metadata

After approval, production key setup and the immutable version tag, signing is:

```sh
python3 scripts/apk_release.py sign \
  --apksigner "$ANDROID_HOME/build-tools/34.0.0/apksigner" \
  --aapt "$ANDROID_HOME/build-tools/36.0.0/aapt" \
  --unsigned app/build/outputs/apk/release/app-release-unsigned.apk \
  --output app/build/outputs/signed/com.twkevinzhang.smsforwarder_2.apk
```

The script checks release approval, a clean source checkout at the immutable tag
on origin/main, APK package/version and the pinned certificate.
The automated release then runs the independent F-Droid-container check before
publishing. During this pre-publication check, the temporary build recipe omits
only the future `Binaries` download URL and uses the supplied signed reference
locally. The final metadata keeps that URL. Verification copies the upstream
signature onto the independently built unsigned APK, verifies its signature and
requires the reconstructed APK to be byte-identical to the upstream APK.

The normal no-argument `verify_fdroid.sh` still checks the existing recipe's pinned
source. Until that recipe is replaced, it verifies historical `1.0.0`, not this
uncommitted R8 candidate. Do not report that run as candidate RB evidence.

After publication, render the actual one-app MR metadata using the approved source
SHA and verified signed APK (output must not already exist):

```sh
python3 scripts/apk_release.py metadata \
  --apksigner "$ANDROID_HOME/build-tools/34.0.0/apksigner" \
  --aapt "$ANDROID_HOME/build-tools/36.0.0/aapt" \
  --source-sha FULL_APPROVED_TAG_COMMIT_SHA \
  --signed app/build/outputs/signed/com.twkevinzhang.smsforwarder_2.apk \
  --output build/com.twkevinzhang.smsforwarder.yml
```

The final URL is
`https://github.com/twkevinzhang/sms-forwarder/releases/download/v1.0.1/com.twkevinzhang.smsforwarder_2.apk`.
Confirm it is public and immutable, run F-Droid metadata lint/build/RB verification,
and replace the old build in MR !52669 with only the latest version. Mark RB
complete only after these steps, not when the template is written.

## Current evidence and remaining acceptance

Local JDK 17 checks passed: release R8 build, 7 unit tests and lint (0 errors,
29 warnings). The candidate unsigned APK is 2,127,103 bytes (2.029 MiB), with R8
9.0.32 full/release marker and mapping output. This is not a claim of runtime
regression completion or F-Droid RB acceptance. A debug-key-only signing smoke
test verified signature copying and rejected a mismatched certificate; it neither
created a production key nor tested the production identity. Historical `1.0.0` build evidence
remains in `BUILD_VERIFICATION.md`.

Two local builds of the same uncommitted candidate (one isolated source copy with
the same Git revision metadata, its own project cache and no build cache) produced
byte-identical unsigned APKs:

```text
7b728aef25ce62bc9fe8c937966cf2f6a40063d3cec128c00bc9764fd93119eb
```

The signature-copy smoke also used that isolated rebuild as its unsigned input.
Both builds ran on this Mac with JDK 17; this does not substitute for the future
F-Droid Linux/JDK 21 production comparison. A first copy without Git metadata had
different AGP version-control metadata; the equivalent-input rebuild above
resolved that test-fixture difference without changing app code.

Release tooling has 19 passing unit checks, including rejection of missing
secrets, wrong certificates, changed APK identity, different reconstructed bytes,
uncommitted checkouts and mismatched version tags. Workflow YAML parsing, shell
syntax and whitespace checks passed. The production certificate pin is now present and approval is true; signing still
requires a clean checkout at the approved immutable tag.

The optimized release is checked on an isolated emulator for launch, language,
settings and background smoke behavior. Physical dual-SIM carrier delivery,
background OEM behavior and real-SMS regressions are not exercised in this
packaging-only change; no real SMS destination or cost has been authorized.
No existing ASUS app is overwritten. Do not describe emulator or build checks as
proof of carrier delivery.

Production signing, official-container independent comparison, public download
and F-Droid CI/MR results must be recorded from their actual execution. The owner
will review and submit the final discussion reply personally; Codex leaves it in
the browser input without sending.

References:
- https://f-droid.org/docs/Reproducible_Builds/
- https://f-droid.org/docs/Build_Metadata_Reference/#Binaries
- https://developer.android.com/topic/performance/app-optimization/enable-app-optimization
