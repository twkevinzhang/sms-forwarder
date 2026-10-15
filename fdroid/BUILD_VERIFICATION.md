# Build verification

## Version 1.0.1 execution record

- App/version: `com.twkevinzhang.smsforwarder`, `1.0.1` / `2`.
- Immutable source: `92dfcc0545cd9ee53256c51e129dc1be245e2469` (`v1.0.1`).
- R8: enabled; full/release marker present. Resource shrinking is unchanged.
- [Main CI](https://github.com/twkevinzhang/sms-forwarder/actions/runs/38763093913): passed.
- [Initial release run](https://github.com/twkevinzhang/sms-forwarder/actions/runs/38763655654):
  official-container source build and binary scan passed, but final RB verification
  failed at the external signature-copy helper (the recovery removes that CLI
  dependency). This run did
  not publish the release and is not recorded as a successful RB run.
- [Recovery run](https://github.com/twkevinzhang/sms-forwarder/actions/runs/38766062942):
  **passed (build and publication)**. It retains the exact app tag and uses verification
  utility commit `8c73edb` via the immutable workflow context. The helper calls
  the pinned F-Droid signature-copy API; certificate, identity and reconstructed
  signed-byte checks remain required.

The initial official-container unsigned APK hash exactly matches the downloaded
upstream unsigned artifact:

```text
32b93843b4593ca085487c48147f521679ef3431d41aa2a4c298c17cdb8c0bcc
```

Local production-certificate verification and signature-copy comparison passed.
The local signed reference has SHA-256
`aafff86bd4e82b0dd525a4eba526a5a58ca6b5920cfe11e88b9cdbe09efd199d`.
The unauthenticated public release download has this same hash; its checksum,
pinned certificate and signature-copy verification passed.

The recovery log confirmed the external copier CLI was absent, the pinned API
was available, and the reconstructed signed APK was byte-identical. The signed
[APK](https://github.com/twkevinzhang/sms-forwarder/releases/download/v1.0.1/com.twkevinzhang.smsforwarder_2.apk)
and its checksum are publicly available. Remaining submission checks:
- Updated fdroiddata commit: `62fd7456d683a717e65979d091dab7754ada21f2`; [MR pipeline](https://gitlab.com/twkevinzhang/fdroiddata/-/pipelines/2951909408) passed all nine jobs.
- Updated MR Reports review: ten findings reviewed and explained (see below).

The first updated pipeline `2951893406` failed only metadata formatting; applying
the official rewritemeta artifact resolved it in the successful pipeline above.
The checked-in recipe preserves that artifact exactly, including the formatter's
space after `Binaries:` before its wrapped URL.

## Current MR Reports review

The final MR Reports tab was reviewed: ten findings, three Minor and seven Info.
The three Minor findings are the required `RECEIVE_SMS`, `SEND_SMS` and
`READ_PHONE_STATE` permissions for SMS forwarding/test-send and SIM selection.
Informational findings identify the AndroidX dynamic-receiver signature permission,
production signing identity (`CN=SMS Forwarder contributors`), approximately
2.1 MB universal / 1.6 MB per-ABI size, R8 9.0.32 full/release (min API 26),
en-US and zh-TW Fastlane metadata/assets, and the reproducible-build APK.
All are explained; no R8-marker or RB omission remains.

The [F-Droid CI binary artifact](https://gitlab.com/twkevinzhang/fdroiddata/-/jobs/17208851692/artifacts/raw/tmp/binaries/com.twkevinzhang.smsforwarder_2.binary.apk)
was downloaded and has SHA-256
`aafff86bd4e82b0dd525a4eba526a5a58ca6b5920cfe11e88b9cdbe09efd199d`,
exactly matching the public upstream release. CI success and this comparison do
not imply maintainer acceptance or official repository publication.

## Runtime verification boundary

An isolated Android 35 emulator smoke test covered cold launch, English/Traditional
Chinese switching, settings persistence, permission/SIM validation and returning
from a synthetic inbound event with forwarding disabled. No crashes were recorded.
The smoke APK was debug-test-signed. Comparing it with the tagged CI artifact showed
identical DEX and ZIP member contents except AGP's Git revision metadata
(`META-INF/version-control-info.textproto`). This supports the same optimized
runtime code; it is not a production-signature runtime test.

No real SMS, physical-device installation, true dual-SIM routing, successful
background forwarding, carrier delivery, multipart/duplicate/loop business-terminal
E2E or OEM background behavior was verified. No existing ASUS app/data was changed.
The temporary emulator and test APK were removed; logs remain in
`/tmp/sms-r8-runtime-review/REPORT.md` for this session.

## Historical version 1.0.0


- Application ID: `com.twkevinzhang.smsforwarder`
- versionName / versionCode: `1.0.0` / `1`
- Release tag: `v1.0.0`
- Exact source: `2d6d89ac359eb50577c7ca4c047ec36ca6f67084`
- Store/icon approval digest: `1b994232a923b1ab936d382207c0165d10aeb9cd396d482d82e02205eff3dbab`

## Completed checks

[Source CI](https://github.com/twkevinzhang/sms-forwarder/actions/runs/38725121541)
passed release tooling tests, Android unit tests, lint and unsigned release
packaging with JDK 17 / SDK 36. The APK contains the Apache 2.0 license and
third-party inventory, byte-for-byte matching the committed assets.

[Official-container verification](https://github.com/twkevinzhang/sms-forwarder/actions/runs/38726174239)
passed metadata parsing/rewriting/lint, the recipe prebuild approval and
origin/main ancestry gate, source scanning, release compilation and binary
scanning. F-Droid reported `1 build succeeded`. APK identity was independently
read with aapt. This ran with:

- Official image: `registry.gitlab.com/fdroid/fdroidserver@sha256:9cb68105642ca4e7b295f0ceab10f069f5b3247dc18fa7c36046e9d81aa469a8`
- fdroidserver: `5806784d0d43bed7b938fd4b327162da68927060`
- JDK 21, Gradle 9.1.0, Android SDK 36, Build Tools 36.0.0

Both independently built unsigned APKs have the same SHA-256:

```text
ad66b142677eb1c05e3e0a114b56c36e97bab70e2c7bc5df80f52d14c5826f15
```

This demonstrates byte-identical unsigned output for these two builds of this
exact revision. It is not the F-Droid upstream-signature reproducible-build
mode: no independently signed upstream APK or signing key is supplied.
F-Droid will sign the official installation package with its own key.

[Tag release workflow](https://github.com/twkevinzhang/sms-forwarder/actions/runs/38726773290)
also passed actual tag/version/main ancestry/approval checks and published
[the GitHub source release](https://github.com/twkevinzhang/sms-forwarder/releases/tag/v1.0.0).
F-Droid's tag checker detected `1.0.0`, code `1`, tag `v1.0.0`, resolving to
the exact approved source SHA above.

## Publication boundary

These results verify the upstream release and build recipe. They do not mean
F-Droid has accepted, signed or published the app. Its own fork pipeline,
maintainer review and production build/publication remain independent stages.
