#!/usr/bin/env python3
"""Sign, verify RB, or render metadata locally; never create keys or publish."""
import argparse
import base64
import hashlib
import os
from pathlib import Path
import re
import subprocess
import tempfile

import release


def run(command):
    # Tool diagnostics may contain signing input details. Do not echo them.
    result = subprocess.run(command, capture_output=True, text=True, timeout=120)
    if result.returncode:
        raise ValueError(f"{Path(command[0]).name} failed; no signing inputs logged")
    return result.stdout + result.stderr


def fingerprint(path):
    value = Path(path).read_text().strip().lower()
    if not re.fullmatch(r"[0-9a-f]{64}", value) or len(set(value)) == 1:
        raise ValueError("A real SHA-256 signing certificate fingerprint is required")
    return value


def approved_source(root):
    config = release.configuration(root)
    release.check(root, "v" + config["versionName"], require_approval=True)
    if run(["git", "-C", str(root), "status", "--porcelain"]).strip():
        raise ValueError("Signing requires a clean, reviewed source checkout")
    head = run(["git", "-C", str(root), "rev-parse", "HEAD"]).strip()
    tag = run(["git", "-C", str(root), "rev-parse", "--verify", "v" + config["versionName"] + "^{commit}"]).strip()
    if head != tag:
        raise ValueError("Signing checkout must match the immutable release tag")
    run(["git", "-C", str(root), "merge-base", "--is-ancestor", "HEAD", "refs/remotes/origin/main"])
    return head


def verify_certificate(apksigner, apk, expected):
    output = run([apksigner, "verify", "--verbose", "--print-certs", str(apk)])
    digests = re.findall(r"^Signer #\d+ certificate SHA-256 digest: ([0-9a-fA-F]{64})$", output, re.M)
    if len(digests) != 1 or digests[0].lower() != expected:
        raise ValueError("APK must have exactly one signer matching the pinned certificate")


def sha256(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def verify_identity(aapt, apk, config):
    output = run([aapt, "dump", "badging", str(apk)])
    match = re.search(r"^package: name='([^']+)' versionCode='(\d+)' versionName='([^']+)'", output, re.M)
    expected = (config["applicationId"], str(config["versionCode"]), config["versionName"])
    if not match or match.groups() != expected:
        raise ValueError("APK identity/version does not match the release configuration")


def sign(apksigner, unsigned, output, expected):
    required = ("SMS_KEY_ALIAS", "SMS_KEYSTORE_PASSWORD", "SMS_KEY_PASSWORD")
    if any(not os.environ.get(name) for name in required):
        raise ValueError("Signing inputs are missing; see fdroid/RB_REVIEW.md")
    key_path = os.environ.get("SMS_KEYSTORE_PATH")
    key_base64 = os.environ.get("SMS_KEYSTORE_BASE64")
    if bool(key_path) == bool(key_base64):
        raise ValueError("Provide exactly one keystore source: PATH or BASE64")
    output = Path(output)
    if output.exists():
        raise ValueError("Refusing to overwrite an existing signed APK")
    output.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(prefix="sms-sign-") as stage:
        key = Path(key_path) if key_path else Path(stage) / "release.keystore"
        if key_base64:
            key.write_bytes(base64.b64decode(key_base64, validate=True))
            key.chmod(0o600)
        candidate = Path(stage) / "signed.apk"
        run([apksigner, "sign", "--ks", str(key), "--ks-key-alias", os.environ["SMS_KEY_ALIAS"],
             "--ks-pass", "env:SMS_KEYSTORE_PASSWORD", "--key-pass", "env:SMS_KEY_PASSWORD",
             "--v1-signing-enabled", "false", "--v2-signing-enabled", "true",
             "--v3-signing-enabled", "true", "--v4-signing-enabled", "false",
             "--out", str(candidate), str(unsigned)])
        verify_certificate(apksigner, candidate, expected)
        output.write_bytes(candidate.read_bytes())
    output.with_suffix(".apk.sha256").write_text(f"{sha256(output)}  {output.name}\n")


def verify_rb(apksigner, copier, unsigned, signed, expected):
    verify_certificate(apksigner, signed, expected)
    with tempfile.TemporaryDirectory(prefix="sms-rb-") as stage:
        reconstructed = Path(stage) / "reconstructed.apk"
        run([copier, "copy", str(signed), str(unsigned), str(reconstructed)])
        verify_certificate(apksigner, reconstructed, expected)
        if sha256(reconstructed) != sha256(signed):
            raise ValueError("Reconstructed APK differs from the upstream signed APK")
    print("RB signature-copy verification passed: reconstructed signed APK is byte-identical")


def render_metadata(root, source_sha, expected, output):
    if not re.fullmatch(r"[0-9a-f]{40}", source_sha):
        raise ValueError("Metadata requires a full immutable source commit SHA")
    config = release.configuration(root)
    tag = "v" + config["versionName"]
    actual = run(["git", "-C", str(root), "rev-parse", "--verify", tag + "^{commit}"]).strip()
    if actual != source_sha:
        raise ValueError("Source SHA must match the immutable version tag")
    template = (root / "fdroid/metadata/com.twkevinzhang.smsforwarder.yml.in").read_text()
    template = template.replace("# REVIEW TEMPLATE ONLY. Render after approval, signing-key setup and immutable tag.",
                                "# Generated RB metadata; review before submitting.")
    for key, value in {"VERSION_NAME": config["versionName"], "VERSION_CODE": str(config["versionCode"]),
                       "SOURCE_SHA": source_sha, "CERT_SHA256": expected}.items():
        template = template.replace("@" + key + "@", value)
    if re.search(r"@[A-Z_]+@", template):
        raise ValueError("Metadata contains unresolved template values")
    output = Path(output)
    if output.exists():
        raise ValueError("Refusing to overwrite existing metadata")
    output.write_text(template)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("command", choices=("sign", "verify-rb", "metadata"))
    parser.add_argument("--apksigner", default="apksigner")
    parser.add_argument("--copier", default="apksigcopier")
    parser.add_argument("--aapt", default="aapt")
    parser.add_argument("--certificate", type=Path, default=release.ROOT / "fdroid/signing-certificate.sha256")
    parser.add_argument("--unsigned", type=Path)
    parser.add_argument("--signed", type=Path)
    parser.add_argument("--output", type=Path)
    parser.add_argument("--source-sha")
    args = parser.parse_args()
    try:
        expected = fingerprint(args.certificate)
        config = release.configuration(release.ROOT)
        if args.command == "sign":
            if not args.unsigned or not args.output:
                raise ValueError("sign requires --unsigned and --output")
            approved_source(release.ROOT)
            verify_identity(args.aapt, args.unsigned, config)
            sign(args.apksigner, args.unsigned, args.output, expected)
            print("Signed APK verified against the pinned certificate")
        elif args.command == "verify-rb":
            if not args.unsigned or not args.signed:
                raise ValueError("verify-rb requires independent --unsigned and upstream --signed APKs")
            verify_identity(args.aapt, args.unsigned, config)
            verify_identity(args.aapt, args.signed, config)
            verify_rb(args.apksigner, args.copier, args.unsigned, args.signed, expected)
        else:
            if not args.source_sha or not args.output or not args.signed:
                raise ValueError("metadata requires --source-sha, --signed and --output")
            if approved_source(release.ROOT) != args.source_sha:
                raise ValueError("Metadata must describe the exact reviewed checkout")
            verify_identity(args.aapt, args.signed, config)
            verify_certificate(args.apksigner, args.signed, expected)
            render_metadata(release.ROOT, args.source_sha, expected, args.output)
            print("RB metadata rendered; review before submitting it")
    except (ValueError, OSError, subprocess.SubprocessError):
        parser.exit(1, "APK release check failed: missing/invalid inputs, unapproved release, or verification failure. No secrets logged. See fdroid/RB_REVIEW.md.\n")


if __name__ == "__main__":
    main()
