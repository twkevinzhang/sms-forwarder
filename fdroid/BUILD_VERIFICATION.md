# Version 1.0.0 build verification

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
