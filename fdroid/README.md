# F-Droid release preparation

Application ID: `com.twkevinzhang.smsforwarder`.
Project source is MIT licensed; dependencies retain their respective licenses.
Signing model: F-Droid builds and signs the official APK. No private APK
signing key or GitLab token is required by these GitHub workflows.

## Current state

The owner has approved the listing text. Final screenshots and the initial
recipe remain in preparation; no release tag or F-Droid submission has been
created. Main-branch GitHub CI has passed. Final asset approval is required
before publication. See `DEPENDENCY_REVIEW.md` for the dependency inventory
and local scanner results. The local
recipe is not yet proof of an accepted F-Droid build: run its checks in the
official build environment before submission.

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
- Do not move an existing release tag. Increase `versionCode` for each
  published version and keep `versionName`, changelog and tag consistent.

The Android source-code namespace is independent of the installable application
ID. This release uses the application ID above. It is a separate app from builds with the old ID;
existing installations and settings are not automatically migrated.

## Initial submission

1. Finish the listing review in `STORE_LISTING_DRAFT.md`, audit dependency
   and artwork licenses, and verify a clean release build. The launcher uses original MIT-licensed SMS bubble/arrow vector artwork;
   its final appearance must be reviewed with the screenshots.
2. Merge the prepared source, listing and approval record into the public
   GitHub repository's `main`; ensure CI passes.
3. Create and push `v1.0.0` only after the owner approves release. Tagging
   requires a separate explicit action; this preparation does not do it.
4. Fork `https://gitlab.com/fdroid/fdroiddata` and copy
   `metadata/com.twkevinzhang.smsforwarder.yml` from this directory to its
   `metadata/` directory. Run there:

   ```sh
   fdroid readmeta
   fdroid rewritemeta com.twkevinzhang.smsforwarder
   fdroid lint com.twkevinzhang.smsforwarder
   fdroid build --server com.twkevinzhang.smsforwarder
   ```

5. Submit a `New App: com.twkevinzhang.smsforwarder` merge request and respond
   to packaging review. Use GitHub Issues for app support and upstream code
   discussions; packaging review itself necessarily happens on F-Droid's
   GitLab infrastructure.

After the recipe is accepted, `UpdateCheckMode: Tags` and `AutoUpdateMode:
Version` let F-Droid detect later numeric version tags. It builds, reviews
and publishes on its own schedule. A GitHub workflow cannot upload an APK
directly into the official repository or guarantee acceptance or timing.
The recipe's prebuild check also requires origin/main ancestry and blocks
unapproved store drafts. Its clone must retain origin/main through prebuild;
verify this in the official builder during initial packaging review.

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
