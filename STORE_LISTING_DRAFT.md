# Store listing — draft, not approved

The repository owner has approved the English and Traditional Chinese text
under `fastlane/metadata/android/`. Final screenshots and the complete listing
still require review. Do not publish a release or submit this recipe for
inclusion until the final assets are approved.

## Proposed listing

| Field | English | Traditional Chinese |
| --- | --- | --- |
| Name | SMS Forwarder | 簡訊自動轉寄 |
| Summary | Forward incoming SMS using a SIM and destination you choose | 選擇接收與發送 SIM，將新簡訊自動轉寄至指定門號 |
| License | MIT | MIT |
| Support | GitHub Issues | GitHub Issues |

Read `full_description.txt` and `changelogs/1.txt` in both locales for the
complete proposed public text. The descriptions include permissions, mobile
operator charges, forwarding limits, background restrictions, privacy and
unsupported message types. They make no device-specific validation claims.

## Screenshots still required

No private device screenshots are reused. Before approval, capture at least
two actual screenshots of the final UI with fictional numbers and messages:

1. SIM selectors, destination, enable switch and saved settings.
2. Test action and status history, using fictional sample messages.

Place PNG files in `en-US/images/phoneScreenshots/` beneath the metadata root;
localized screenshots may also be added under `zh-TW`. Review status bars,
notifications and image metadata before committing. The original MIT-licensed store icon is in each locale's `images/icon.png`;
its vector source is `artwork/icon.svg`. The adaptive launcher icon uses the
same artwork. Final visual approval includes these icons.

## Approval record

`.github/store-listing-approval.json` intentionally starts with `approved:
false`. After the owner explicitly approves the final text and screenshots:

1. Run `python3 scripts/release.py digest`.
2. Record the digest in `listingSha256`, set the matching `version`, and
   change `approved` to `true` in a reviewed commit on `main`.
3. Re-run `python3 scripts/release.py check --require-approval`.

Changing a listing file, screenshot, changelog, launcher icon or release version invalidates
approval. Never approve on behalf of the owner or bypass the checks in CI or
the F-Droid build recipe.
