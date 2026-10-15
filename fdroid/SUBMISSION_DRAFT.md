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
- Release: https://github.com/twkevinzhang/sms-forwarder/releases/tag/v1.0.1
- App ID: `com.twkevinzhang.smsforwarder`
- versionName / versionCode: `1.0.1` / `2`
- Source commit: `92dfcc0545cd9ee53256c51e129dc1be245e2469`
- App support and maintenance: GitHub Issues and pull requests.

<img src="https://raw.githubusercontent.com/twkevinzhang/sms-forwarder/v1.0.1/fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" width="240" />
<img src="https://raw.githubusercontent.com/twkevinzhang/sms-forwarder/v1.0.1/fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" width="240" />

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

Version 1.0.1 enables R8 and configures upstream-signature Reproducible Builds.
The recipe uses the immutable source above, the GitHub signed-APK `Binaries` URL
and `AllowedAPKSigningKeys` pinned to `signing-certificate.sha256`.
See [BUILD_VERIFICATION.md](BUILD_VERIFICATION.md) for the exact unsigned hash,
production signing checks, recovery results and runtime verification boundary.

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

## Merge request and pipeline

The existing [MR !52669](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/52669)
is updated on the same public fork and unprotected app branch. Only
`metadata/com.twkevinzhang.smsforwarder.yml` belongs in its diff.

- Metadata commit: `62fd7456d683a717e65979d091dab7754ada21f2`.
- [Updated MR pipeline](https://gitlab.com/twkevinzhang/fdroiddata/-/pipelines/2951909408): all nine jobs passed.
- New Reports review: ten findings reviewed and explained (see below).
- Reviewer discussion reply: prepared for owner review; **not sent by Codex**.

Historical 1.0.0 pipelines passed all nine jobs each. The earlier Reports
permission, AndroidX receiver, ABI and metadata observations were explained;
R8 and RB are now changed, so new Reports must be inspected. A successful CI
artifact or upstream GitHub release does not prove official F-Droid publication.

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
