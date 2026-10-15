# Contributing and support

Use GitHub Issues for bugs, feature requests, device compatibility questions
and maintenance discussions:
https://github.com/twkevinzhang/sms-forwarder/issues

Propose changes through GitHub pull requests. Include reproduction steps,
expected and actual behavior, Android version and relevant permission or
background settings. Do not include real phone numbers, SMS bodies,
verification codes, PINs, signing keys or private logs. Use fictional
`+1 202-555-01xx` numbers in examples.

Run Android unit tests, lint and the release build, plus:

```sh
python3 -m unittest discover -s scripts -p 'test_*.py'
python3 scripts/release.py check
```

Code contributions are submitted under the project's MIT license.
