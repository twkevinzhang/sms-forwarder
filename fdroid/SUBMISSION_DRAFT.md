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

- [ ] Public fdroiddata fork, dedicated unprotected app branch, one-app diff.
- [ ] Fork pipeline checked and any reports resolved or explained.

Replace the branch item with the actual fork details and report its pipeline
status before submission. If GitLab restricts personal runner access, leave
the pipeline checkbox open, explain the restriction and request F-Droid's
FOSS runner in the MR; do not provide payment details to enable CI.
