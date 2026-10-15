import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

import apk_release


class APKReleaseChecksTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.pin = "12" * 32

    def test_missing_or_placeholder_certificate_is_rejected(self):
        path = self.root / "certificate"
        for value in ("", "TODO", "0" * 64, "f" * 64, "ab" * 31):
            path.write_text(value)
            with self.subTest(value=value), self.assertRaises(ValueError):
                apk_release.fingerprint(path)
        path.write_text(self.pin)
        self.assertEqual(apk_release.fingerprint(path), self.pin)

    def test_signer_mismatch_or_multiple_signers_is_rejected(self):
        good = "Signer #1 certificate SHA-256 digest: " + self.pin + "\n"
        bad = "Signer #1 certificate SHA-256 digest: " + "34" * 32 + "\n"
        for output in (bad, good + good.replace("#1", "#2"), ""):
            with patch.object(apk_release, "run", return_value=output), self.assertRaises(ValueError):
                apk_release.verify_certificate("apksigner", "candidate.apk", self.pin)
        with patch.object(apk_release, "run", return_value=good):
            apk_release.verify_certificate("apksigner", "candidate.apk", self.pin)

    def test_wrong_apk_version_or_package_is_rejected(self):
        config = {"applicationId": "com.twkevinzhang.smsforwarder", "versionCode": 2, "versionName": "1.0.1"}
        for name, code, version in (("com.other", 2, "1.0.1"), (config["applicationId"], 1, "1.0.0")):
            output = f"package: name='{name}' versionCode='{code}' versionName='{version}'"
            with patch.object(apk_release, "run", return_value=output), self.assertRaises(ValueError):
                apk_release.verify_identity("aapt", "candidate.apk", config)

    def test_rb_rejects_different_bytes_even_after_certificate_check(self):
        signed = self.root / "signed.apk"
        signed.write_bytes(b"upstream")
        def copy(command):
            Path(command[-1]).write_bytes(b"different")
        with patch.object(apk_release, "verify_certificate"), patch.object(apk_release, "run", side_effect=copy):
            with self.assertRaisesRegex(ValueError, "differs"):
                apk_release.verify_rb("apksigner", "apksigcopier", "unsigned.apk", signed, self.pin)

    def test_missing_secret_inputs_fail_before_invoking_signer(self):
        with patch.dict("os.environ", {}, clear=True), patch.object(apk_release, "run") as run:
            with self.assertRaises(ValueError):
                apk_release.sign("apksigner", "unsigned.apk", self.root / "signed.apk", self.pin)
            run.assert_not_called()

    def test_metadata_refuses_a_tag_pointing_to_a_different_commit(self):
        config = {"versionName": "1.0.1", "versionCode": 2}
        with patch.object(apk_release.release, "configuration", return_value=config), \
             patch.object(apk_release, "run", return_value="b" * 40):
            with self.assertRaisesRegex(ValueError, "immutable"):
                apk_release.render_metadata(self.root, "a" * 40, self.pin, self.root / "out.yml")

    def test_tool_failure_does_not_echo_secret_diagnostics(self):
        import subprocess
        result = subprocess.CompletedProcess([], 1, "secret-value", "private-key-path")
        with patch.object(apk_release.subprocess, "run", return_value=result):
            with self.assertRaises(ValueError) as error:
                apk_release.run(["apksigner", "sign"])
        self.assertNotIn("secret-value", str(error.exception))
        self.assertNotIn("private-key-path", str(error.exception))

    def test_uncommitted_or_wrong_tag_checkout_cannot_sign(self):
        config = {"versionName": "1.0.1", "versionCode": 2}
        with patch.object(apk_release.release, "configuration", return_value=config), \
             patch.object(apk_release.release, "check"), \
             patch.object(apk_release, "run", side_effect=[" M app/build.gradle.kts"]):
            with self.assertRaisesRegex(ValueError, "clean"):
                apk_release.approved_source(self.root)
        with patch.object(apk_release.release, "configuration", return_value=config), \
             patch.object(apk_release.release, "check"), \
             patch.object(apk_release, "run", side_effect=["", "a" * 40, "b" * 40]):
            with self.assertRaisesRegex(ValueError, "tag"):
                apk_release.approved_source(self.root)


if __name__ == "__main__":
    unittest.main()
