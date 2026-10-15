# New app: SMS Forwarder

Draft merge-request description for fdroid/fdroiddata. Do not submit until
all remaining checks below are complete.

SMS Forwarder forwards newly received SMS from a selected SIM to a configured
phone number, using an independently selected sending SIM. It also provides
a test-send action and local status history. App support and development
are handled through GitHub Issues and pull requests.

- Source: https://github.com/twkevinzhang/sms-forwarder
- License: MIT (dependencies retain their upstream licenses)
- Application ID: `com.twkevinzhang.smsforwarder`
- Initial version: `1.0.0` / versionCode `1`
- Planned release tag: `v1.0.0` (not yet created)
- Signing: F-Droid builds and signs; no upstream binary is supplied.
- Upstream metadata: `fastlane/metadata/android/{en-US,zh-TW}`
- Automatic updates: strict numeric `vX.Y.Z` tags

## Validation

- [x] Owner consents to inclusion and has approved the listing text.
- [x] Public upstream repository and MIT license.
- [x] GitHub CI: unit tests, lint and unsigned release build.
- [x] Local fdroidserver 2.4.5 metadata lint.
- [x] Local source and APK non-free scanner: zero problems.
- [x] Resolved runtime dependency/constraint POM license inventory.
- [ ] Final real screenshots and owner approval of complete listing.
- [x] Original MIT-licensed launcher artwork replaces template assets.
- [ ] Approved immutable release tag exists upstream, and recipe `commit`
      is replaced with its full SHA (the current SHA is preparation only).
- [ ] Official container/server recipe build, including prebuild approval
      and origin/main ancestry checks, succeeds.
- [ ] fdroiddata fork CI succeeds.

This draft is not evidence of an accepted official build or publication.
