# F-Droid release guide

Application ID: `com.twkevinzhang.smsforwarder`.
Project source is MIT licensed; dependencies retain their respective licenses.
Signing model: F-Droid builds and signs the official APK. No private APK
signing key or GitLab token is required by these GitHub workflows.

## Current state

The owner authorized final asset review, release and initial submission.
The listing includes reviewed English and Traditional Chinese text, original
icons and real screenshots with a fictional example number. The version and
asset digest are recorded in `.github/store-listing-approval.json`.
See `DEPENDENCY_REVIEW.md` for the dependency inventory and local scanner
results. Version `v1.0.0` is released on GitHub, and its recipe passed the
official F-Droid container build and binary scan. See `BUILD_VERIFICATION.md`
for exact revisions, workflow links and the APK checksum. Initial official
repository inclusion is still pending; a passing build does not imply acceptance.

## CI and tag releases

- A `main` push or pull request to `main` runs tooling tests, Android unit
  tests, lint and `assembleRelease` with JDK 17 / SDK 36.
- Pushing a tag matching `vX.Y.Z` runs a release build. The workflow also
  checks that the tag commit is an ancestor of `origin/main`, and that its
  version matches `app/build.gradle.kts`. GitHub's `v*` event filter is broad;
  the validation script rejects invalid formats before building.
- Only an approved listing with the exact version and listing/icon digest enables
  the GitHub Release job. Unapproved tags build an Actions artifact only.
- Actions artifacts are **unsigned**, expire after 14 days, and are not
  installable distribution APKs. GitHub Releases publish source and notes;
  signed installation packages come from F-Droid after its own processing.
- The manually dispatched `fdroid.yml` workflow runs `scripts/verify_fdroid.sh`
  in a pinned official F-Droid container on a standard Linux runner, with
  a 30-minute timeout. It builds the recipe's pinned source, scans the APK
  and reports its identity and SHA-256. It does not sign or submit anything.
- Do not move an existing release tag. Increase `versionCode` for each
  published version and keep `versionName`, changelog and tag consistent.

The Android source-code namespace is independent of the installable application
ID. This release uses the application ID above. It is a separate app from builds with the old ID;
existing installations and settings are not automatically migrated.

## Initial submission

1. Review the listing, screenshots, artwork and dependency licenses. Record
   the approved version and asset digest. The launcher and store icon are
   original MIT-licensed artwork. The current 1.0.0 review is complete.
2. Commit the approved source on `main` and ensure CI passes. Pin the recipe
   to that full source SHA and run the F-Droid recipe verification workflow.
3. Create the numeric version tag at that same approved source commit and
   push it after validation. The tag must remain immutable. The 1.0.0 tag
   resolves to `2d6d89ac359eb50577c7ca4c047ec36ca6f67084`.
4. Fork `https://gitlab.com/fdroid/fdroiddata`. Create an app-specific,
   unprotected branch in the fork and copy only
   `metadata/com.twkevinzhang.smsforwarder.yml` from this directory to its
   `metadata/` directory. Keep store descriptions and images upstream.
5. Check the fork pipeline, then submit a `New app: SMS Forwarder` merge
   request using the current App inclusion template. `SUBMISSION_DRAFT.md`
   contains the prepared description. F-Droid's FOSS runners are free; if
   GitLab requests payment or identity verification for CI, request the
   maintainers' runner instead of buying capacity.
6. Respond to packaging review on GitLab. App support, bugs, feature requests
   and maintenance discussions remain on GitHub Issues and pull requests.

After the recipe is accepted, `UpdateCheckMode: Tags` and `AutoUpdateMode:
Version` let F-Droid detect later numeric version tags. It builds, reviews
and publishes on its own schedule. A GitHub workflow cannot upload an APK
directly into the official repository or guarantee acceptance or timing.
The recipe's prebuild check also requires origin/main ancestry and blocks
unapproved store drafts. The current recipe's origin/main and exact approval checks were exercised
successfully by the official-container verification workflow. Production
publication and the fdroiddata fork pipeline still require their own review.

## ProGuard / R8

`app/proguard-rules.pro` is present and contains no blanket keep rules or
warning suppression. Release shrinking remains disabled to avoid an
unverified runtime change during release preparation. Enabling R8/resource
shrinking later requires its own release-device regression; it is not
required for F-Droid inclusion.

## References

- https://f-droid.org/en/docs/Submitting_to_F-Droid_Quick_Start_Guide/
- https://f-droid.org/en/docs/Build_Metadata_Reference/
- https://f-droid.org/en/docs/Inclusion_Policy/
