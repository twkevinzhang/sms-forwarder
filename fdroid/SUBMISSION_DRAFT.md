# New app: SMS Forwarder

## Abstract

SMS Forwarder forwards new incoming SMS from a selected receiving SIM to a
configured phone number, using an independently selected sending SIM. It
provides a test-send button using saved settings and local status history
that distinguishes sending from carrier-supported delivery confirmation.

The project focuses on a small SMS-only workflow with explicit SIM selection,
visible configuration and no server, account, advertising or analytics. It
preserves SMS body text without retaining it in app activity records. This
is its design emphasis, rather than a claim that no similar app exists.
SMS charges and device background restrictions are disclosed in the listing.

- Upstream: https://github.com/twkevinzhang/sms-forwarder
- Release: https://github.com/twkevinzhang/sms-forwarder/releases/tag/v1.0.0
- App ID: `com.twkevinzhang.smsforwarder`
- versionName / versionCode: `1.0.0` / `1`
- Source commit: `2d6d89ac359eb50577c7ca4c047ec36ca6f67084`
- App support and maintenance: GitHub Issues and pull requests.

<img src="https://raw.githubusercontent.com/twkevinzhang/sms-forwarder/v1.0.0/fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" width="240" />
<img src="https://raw.githubusercontent.com/twkevinzhang/sms-forwarder/v1.0.0/fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" width="240" />

## Policy and upstream metadata

- [x] Public, current source; MIT app license; reviewed FOSS dependencies.
- [x] The repository owner authorized inclusion and final asset review;
      the upstream release records the listing review and approval digest.
- [x] Fastlane metadata includes en-US and zh-TW descriptions, changelog,
      original MIT artwork and real screenshots with a fictional number.
- [x] Permissions, SMS fees, privacy and background limitations are disclosed.

## Documentation and packaging

- [x] Contribution guide, current inclusion template, metadata reference and
      quick-start guide reviewed; metadata parsing, rewriting and lint passed.
- [x] Only the app YAML belongs in this MR; descriptions and images stay upstream.
- [x] Latest release only, immutable version tag, full source SHA and tag updates.
- [x] AuthorName and GitHub contact/issue tracker supplied; no external source
      repositories or disabled builds are used.
- [x] No ABI split is requested for this small APK.

## Signing and build evidence

F-Droid signing is requested. There is no independently signed upstream APK,
so upstream-signature reproducible-build mode is not configured. GitHub
publishes a source release; its Actions APK is unsigned. No cross-channel
signature compatibility is claimed.

The [upstream CI](https://github.com/twkevinzhang/sms-forwarder/actions/runs/38725121541),
[official F-Droid container build and scan](https://github.com/twkevinzhang/sms-forwarder/actions/runs/38726174239)
and [tag release workflow](https://github.com/twkevinzhang/sms-forwarder/actions/runs/38726773290)
all passed. Two independent unsigned builds of this exact source have SHA-256
`ad66b142677eb1c05e3e0a114b56c36e97bab70e2c7bc5df80f52d14c5826f15`.
The container run exercised the prebuild main-ancestry and exact asset-approval
gates. Tag detection also found the correct version, code and source revision.

## Merge request and pipeline

Submitted on 2026-10-15 using the current App inclusion template:
[fdroiddata MR !52669](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/52669).
The MR is open; maintainer review, merge and official publication are pending.
The submitted MR description is the authoritative template/checklist record.

- [x] Public [fdroiddata fork](https://gitlab.com/twkevinzhang/fdroiddata), dedicated
  unprotected [app branch](https://gitlab.com/twkevinzhang/fdroiddata/-/tree/com.twkevinzhang.smsforwarder),
  one-app diff containing only `metadata/com.twkevinzhang.smsforwarder.yml`.
- Commit: `e199ad8d0252c435709f552b59b87a31a22d369a`.
- [x] [Fork push pipeline](https://gitlab.com/twkevinzhang/fdroiddata/-/pipelines/2950886308)
  passed all nine jobs in 409 seconds.
- [x] [Automatic MR pipeline](https://gitlab.com/twkevinzhang/fdroiddata/-/pipelines/2950920367)
  passed all nine jobs in 406 seconds.
- [x] MR Reports tab reviewed after the MR pipeline: nine findings explained
  below. Its initial “No pipeline results yet” state was superseded by the report.

The MR explains warnings from the shared CI environment: initial cache misses,
root-level Gradle cache paths, Debian package/manual-page cleanup, Python escape
sequences, pip-as-root and inherited `config.yml` permissions. Gradle also retained
the packaged AndroidX native library when stripping was unavailable. These did not
fail the push pipeline. No payment or paid runner capacity was requested.

The fork was initially created under `neslxzhen`; GitLab project ID `87671295`
now reports the canonical namespace `twkevinzhang/fdroiddata` (same project and
user ID `5571694`). Current links above use the canonical namespace.

## Reports review

The MR Reports tab showed four minor and five informational findings. These are
reviewed observations, not an assertion that F-Droid has accepted the app.

- Minor: `RECEIVE_SMS`, `SEND_SMS` and `READ_PHONE_STATE` are the three declared
  runtime permissions needed to receive new SMS, forward/test-send SMS and query
  active subscriptions for explicit dual-SIM selection. They are declared in
  `app/src/main/AndroidManifest.xml` and checked by `TelephonyGateway` before use.
- Minor: no R8 marker is expected because `isMinifyEnabled = false`. Release
  shrinking is intentionally disabled pending its own device regression, as
  documented in the release guide's ProGuard / R8 section.
- Informational: `com.twkevinzhang.smsforwarder.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`
  is a signature-level permission merged automatically from AndroidX Core. The
  local release manifest-merger report confirms its dependency origin; it is not
  an additional dangerous runtime permission.
- Informational: the ABI/size summary lists arm64-v8a, armeabi-v7a, x86 and x86_64
  at approximately 8.0 MB each, with an 8.5 MB universal APK. The APK is small and
  an ABI split offers limited savings, so no split is requested.
- Informational: en-US and zh-TW listing text, screenshots and icons were detected.
- Informational: the CI-signed APK artifact link is for CI verification. It is not
  the official F-Droid production signing or evidence of repository publication.

The ABI summary and two localized metadata entries account for the five
informational findings together with the receiver permission and artifact link.
No app code or release settings were changed to suppress these reports.
