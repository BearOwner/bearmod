# CI/CD Setup Guide

This document describes the secrets, environment, and workflows used by GitHub Actions for this repository.

## Workflows

- `.github/workflows/android-ci.yml`
  - Build matrix: Ubuntu runner with JDK 17, Android SDK/NDK (API 36, Build-Tools 36.0.0, NDK 27.1.12297006, CMake 3.22.1)
  - Jobs:
    - `build`: `:app:assembleDebug` + `test`
    - `release` (optional): `:app:assembleRelease` using signing secrets
- `.github/workflows/codeql.yml`
  - CodeQL analysis for Java/Kotlin and C/C++
  - Attempts CodeQL autobuild and falls back to Gradle `assembleDebug`

## Required repository secrets (for signed release job)

Set these under GitHub → Settings → Secrets and variables → Actions → Secrets → New repository secret

- `ANDROID_KEYSTORE_BASE64`
  - Base64 of your `release.jks` (do not commit the file)
  - Create with: `base64 -w0 release.jks > release.jks.b64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_ALIAS_PASSWORD`

The workflow restores the keystore from Base64 and injects signing params via Gradle project properties.

## Optional variables

Define Actions variables (not secrets) for SDK versions if you want to override defaults:
- `ANDROID_API_LEVEL` (default: 36)
- `ANDROID_BUILD_TOOLS` (default: 36.0.0)
- `ANDROID_NDK_VERSION` (default: 27.1.12297006)

Then reference them in the workflow instead of hard-coded values.

## Codespaces (optional)

If you use Codespaces, add a devcontainer to preinstall JDK and convenience tooling. Android SDK/NDK installation can be scripted on-demand inside Codespaces:

Example steps inside Codespace terminal:
```
wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O cmdtools.zip
unzip cmdtools.zip -d $HOME/android-sdk
yes | $HOME/android-sdk/cmdline-tools/bin/sdkmanager --sdk_root=$HOME/android-sdk \
  "platform-tools" "platforms;android-36" "build-tools;36.0.0" \
  "cmake;3.22.1" "ndk;27.1.12297006"
export ANDROID_HOME=$HOME/android-sdk
export ANDROID_SDK_ROOT=$HOME/android-sdk
export PATH=$PATH:$HOME/android-sdk/platform-tools
```

For automated prebuilds, enable Codespaces Prebuilds in the repo settings; consider creating a devcontainer with a postCreateCommand script that installs the SDK if your org policy allows it.

## Additional Code Scanning (optional)

- Android Lint in CI:
  - Add a job step: `./gradlew :app:lintDebug --stacktrace`
- Semgrep:
  - Add `.semgrep.yml` and a workflow using `returntocorp/semgrep-action@v1`
- C/C++ static analysis:
  - `cppcheck` or `clang-tidy` can run in a separate job; requires configuration for your NDK headers.

## Common CI Pitfalls

- Missing NDK version mismatch
  - Ensure the NDK installed in CI matches `gradle.properties`/AGP expectations (here: 27.1.12297006)
- Keystore paths with spaces
  - This workflow copies the keystore into `$RUNNER_TEMP` to avoid spaces in HOME path
- Private email push rejection
  - Set local commit author to GitHub noreply format: `<username>@users.noreply.github.com`

## Local build parity

- JDK 17
- Android SDK Platform 36 + Build-Tools 36.0.0
- NDK 27.1.12297006
- Gradle wrapper as in repo (`./gradlew`)

## Rollback strategy

- Each workflow lives under `.github/workflows/`. Revert via PR if a change breaks builds.
- Keep `pre-refactor-snapshot` tag for a clean fallback.
