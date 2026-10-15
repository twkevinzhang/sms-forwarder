# Store listing review

The owner authorized final asset review, release and F-Droid submission.
English and Traditional Chinese text, icons and screenshots have been reviewed
under that authorization for version 1.0.0.

## Listing

| Field | English | Traditional Chinese |
| --- | --- | --- |
| Name | SMS Forwarder | 簡訊自動轉寄 |
| Summary | Forward incoming SMS using a SIM and destination you choose | 選擇接收與發送 SIM，將新簡訊自動轉寄至指定門號 |
| License | MIT | MIT |
| Support | GitHub Issues | GitHub Issues |

Complete descriptions and changelogs are under
`fastlane/metadata/android/{en-US,zh-TW}`. They describe permissions, SMS
charges, sending limits, background restrictions, privacy and unsupported
message types without device-specific validation claims.

## Visual assets

Each locale contains an original MIT-licensed icon and two unmodified real UI
screenshots: saved settings with automatic forwarding disabled, and background
limitations with empty activity history. Only the fictional example number
`+1 202-555-0123` is shown. Images were visually reviewed and have no textual
or EXIF metadata. No successful SMS delivery is simulated or claimed.

The icon source is `artwork/icon.svg`; the adaptive launcher uses matching
original vector artwork. Third-party dependency licensing is documented in
`fdroid/DEPENDENCY_REVIEW.md` and included in the APK under `assets/licenses`.

## Approval record

The published 1.0.0 source retains its original approval. On 2026-10-15, after
reviewing the R8/RB candidate, the owner supplied the production keystore and
explicitly authorized continuation, commit and publication of the reviewer changes.
The current approval record pins 1.0.1 and its listing/changelog digest. The new
changelog describes R8 and the RB publication gate; icons and screenshots are
unchanged. Future listing or version changes require renewed approval.
