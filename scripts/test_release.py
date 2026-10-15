import json
import tempfile
import subprocess
import sys
import unittest
from pathlib import Path

import release


class ReleaseChecksTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        (self.root / "app").mkdir()
        (self.root / "app/build.gradle.kts").write_text(
            'applicationId = "com.twkevinzhang.smsforwarder"\nversionName = "1.0.0"\nversionCode = 1\n'
        )
        (self.root / "LICENSE").write_text("MIT License")
        (self.root / ".github").mkdir()
        self.approval = self.root / ".github/store-listing-approval.json"
        self.approval.write_text(json.dumps({"approved": False, "version": "1.0.0", "listingSha256": ""}))
        for locale in ("en-US", "zh-TW"):
            base = self.root / "fastlane/metadata/android" / locale
            (base / "changelogs").mkdir(parents=True)
            for name in ("title.txt", "short_description.txt", "full_description.txt", "changelogs/1.txt"):
                (base / name).write_text("Example text")

    def approve(self, screenshots=True):
        if screenshots:
            base = self.root / "fastlane/metadata/android/en-US/images/phoneScreenshots"
            base.mkdir(parents=True)
            # Signature-only fixture exercises the check; production images need human review.
            for name in ("1.png", "2.png"):
                (base / name).write_bytes(b"\x89PNG\r\n\x1a\nfixture")
        self.approval.write_text(json.dumps({
            "approved": True, "version": "1.0.0", "listingSha256": release.listing_digest(self.root)
        }))

    def test_draft_can_build_but_cannot_publish(self):
        self.assertFalse(release.check(self.root, "v1.0.0")["approved"])
        with self.assertRaisesRegex(ValueError, "draft"):
            release.check(self.root, "v1.0.0", True)

    def test_tag_must_be_strict_and_match_version(self):
        for tag in ("v1.0", "v1.0.1", "v01.0.0", "v1.0.0-beta", "v1.0.0\n", "1.0.0"):
            with self.subTest(tag=tag), self.assertRaises(ValueError):
                release.check(self.root, tag)

    def test_approved_listing_passes(self):
        self.approve()
        self.assertTrue(release.check(self.root, "v1.0.0", True)["approved"])

    def test_changed_text_invalidates_approval(self):
        self.approve()
        (self.root / "fastlane/metadata/android/zh-TW/full_description.txt").write_text("Changed")
        with self.assertRaisesRegex(ValueError, "stale"):
            release.check(self.root, require_approval=True)

    def test_changed_image_invalidates_approval(self):
        self.approve()
        (self.root / "fastlane/metadata/android/en-US/images/phoneScreenshots/1.png").write_bytes(b"changed")
        with self.assertRaisesRegex(ValueError, "stale"):
            release.check(self.root, require_approval=True)

    def test_changed_launcher_icon_invalidates_approval(self):
        icon = self.root / "app/src/main/res/drawable/ic_launcher_foreground.xml"
        icon.parent.mkdir(parents=True)
        icon.write_text("original fixture")
        self.approve()
        icon.write_text("changed fixture")
        with self.assertRaisesRegex(ValueError, "stale"):
            release.check(self.root, require_approval=True)

    def test_approval_requires_screenshots(self):
        self.approve(screenshots=False)
        with self.assertRaisesRegex(ValueError, "screenshots"):
            release.check(self.root, require_approval=True)

    def test_computed_versions_are_rejected(self):
        path = self.root / "app/build.gradle.kts"
        path.write_text(path.read_text().replace('versionName = "1.0.0"', 'versionName = "1.0.0" + "-debug"'))
        with self.assertRaisesRegex(ValueError, "literal versionName"):
            release.check(self.root)

    def test_wrong_application_id_is_rejected(self):
        path = self.root / "app/build.gradle.kts"
        path.write_text(path.read_text().replace(release.APP_ID, "com.example.other"))
        with self.assertRaisesRegex(ValueError, "applicationId"):
            release.check(self.root)

    def test_main_ancestry_is_enforced_by_the_cli(self):
        scripts = self.root / "scripts"
        scripts.mkdir()
        (scripts / "release.py").write_text(Path(release.__file__).read_text())
        def git(*args):
            return subprocess.run(["git", "-C", str(self.root), "-c", "user.name=Release fixture",
                                   "-c", "user.email=fixture@example.invalid", "-c", "commit.gpgsign=false",
                                   *args], check=True, capture_output=True)
        git("init", "-b", "main")
        git("add", ".")
        git("commit", "-m", "fixture")
        git("update-ref", "refs/remotes/origin/main", "HEAD")
        command = [sys.executable, str(scripts / "release.py"), "check", "--tag", "v1.0.0", "--check-main"]
        self.assertEqual(subprocess.run(command, capture_output=True).returncode, 0)
        git("commit", "--allow-empty", "-m", "not on remote main")
        self.assertNotEqual(subprocess.run(command, capture_output=True).returncode, 0)

    def test_store_limits_and_current_changelog_are_checked(self):
        path = self.root / "fastlane/metadata/android/en-US/short_description.txt"
        path.write_text("x" * 81)
        with self.assertRaisesRegex(ValueError, "short_description"):
            release.check(self.root)


if __name__ == "__main__":
    unittest.main()
