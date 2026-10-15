#!/usr/bin/env python3
"""Validate release inputs; never approve, tag, push, sign, or publish anything."""
import argparse
import hashlib
import json
import re
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
APP_ID = "com.twkevinzhang.smsforwarder"
VERSION = r"(?:0|[1-9][0-9]*)\.(?:0|[1-9][0-9]*)\.(?:0|[1-9][0-9]*)"


def configuration(root):
    source = (root / "app/build.gradle.kts").read_text()
    result = {}
    for key in ("applicationId", "versionName", "versionCode"):
        pattern = rf'(?m)^\s*{key}\s*=\s*' + (r'(\d+)' if key == "versionCode" else r'"([^"\n]+)"') + r'\s*(?://[^\n]*)?$'
        found = re.findall(pattern, source)
        if len(found) != 1:
            raise ValueError(f"Expected one literal {key} in app/build.gradle.kts")
        result[key] = int(found[0]) if key == "versionCode" else found[0]
    if result["applicationId"] != APP_ID:
        raise ValueError("Unexpected applicationId")
    if not re.fullmatch(VERSION, result["versionName"]) or result["versionCode"] < 1:
        raise ValueError("Use a numeric x.y.z versionName and positive versionCode")
    return result


def listing_digest(root):
    base = root / "fastlane/metadata/android"
    listing_files = [p for p in base.rglob("*") if p.is_file()]
    if not listing_files:
        raise ValueError("Store listing is missing")
    # F-Droid can extract the launcher icon from the APK, so it is reviewed too.
    icons = [p for p in (root / "app/src/main/res").rglob("ic_launcher*") if p.is_file()]
    files = sorted(set(listing_files + icons))
    digest = hashlib.sha256()
    for path in files:
        if path.is_symlink():
            raise ValueError("Store listing must not contain symbolic links")
        name = path.relative_to(root).as_posix().encode()
        data = path.read_bytes()
        digest.update(len(name).to_bytes(8, "big") + name)
        digest.update(len(data).to_bytes(8, "big") + data)
    return digest.hexdigest()


def check(root, tag=None, require_approval=False):
    config = configuration(root)
    version = config["versionName"]
    if tag is not None and (not re.fullmatch("v" + VERSION, tag) or tag != "v" + version):
        raise ValueError("Tag must be vX.Y.Z and match versionName exactly")
    if "MIT License" not in (root / "LICENSE").read_text():
        raise ValueError("MIT LICENSE is missing")
    for locale in ("en-US", "zh-TW"):
        base = root / "fastlane/metadata/android" / locale
        for name, limit in (("title.txt", 50), ("short_description.txt", 80),
                            ("full_description.txt", 4000),
                            (f'changelogs/{config["versionCode"]}.txt', 500)):
            text = (base / name).read_text().strip()
            if not 1 <= len(text) <= limit:
                raise ValueError(f"{locale}/{name}: expected 1–{limit} characters")
    digest = listing_digest(root)
    approval = json.loads((root / ".github/store-listing-approval.json").read_text())
    if not isinstance(approval.get("approved"), bool):
        raise ValueError("Approval must be an explicit boolean")
    approved = approval["approved"]
    if approved:
        if approval.get("version") != version or approval.get("listingSha256") != digest:
            raise ValueError("Approval is stale: version or listing changed; request review again")
        screenshots = list((root / "fastlane/metadata/android/en-US/images/phoneScreenshots").glob("*.png"))
        if len(screenshots) < 2 or any(not p.read_bytes().startswith(b"\x89PNG\r\n\x1a\n") for p in screenshots):
            raise ValueError("Approval requires at least two real PNG phone screenshots")
    if require_approval and not approved:
        raise ValueError("Store listing is a draft; publishing is blocked until user review")
    return {**config, "listingSha256": digest, "approved": approved}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("command", choices=("check", "digest"))
    parser.add_argument("--tag")
    parser.add_argument("--require-approval", action="store_true")
    parser.add_argument("--check-main", action="store_true")
    parser.add_argument("--github-output", action="store_true")
    args = parser.parse_args()
    try:
        if args.command == "digest":
            print(listing_digest(ROOT))
            return
        result = check(ROOT, args.tag, args.require_approval)
        if args.check_main:
            subprocess.run(["git", "merge-base", "--is-ancestor", "HEAD", "refs/remotes/origin/main"], cwd=ROOT, check=True)
        if args.github_output:
            import os
            with open(os.environ["GITHUB_OUTPUT"], "a") as output:
                output.write(f'approved={str(result["approved"]).lower()}\n')
        print(json.dumps(result, ensure_ascii=False))
    except (ValueError, OSError, KeyError, json.JSONDecodeError, subprocess.CalledProcessError) as exc:
        parser.exit(1, f"Release validation failed: {exc}\n")


if __name__ == "__main__":
    main()
