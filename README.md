# BearMod

[![Android CI](https://github.com/BearOwner/bearmod/actions/workflows/android-ci.yml/badge.svg)](https://github.com/BearOwner/bearmod/actions/workflows/android-ci.yml)
[![CodeQL](https://github.com/BearOwner/bearmod/actions/workflows/codeql.yml/badge.svg)](https://github.com/BearOwner/bearmod/actions/workflows/codeql.yml)

Modern Android app with native (NDK) components and secure CI.

## Repository structure

- `app/`
  - Android application module
  - Native build via `externalNativeBuild.ndkBuild` with `src/main/cpp/Android.mk`
  - Signing:
    - Debug uses Android debug keystore automatically
    - Release uses CI-injected signing (secrets) or optional local `signing.gradle`
- `mundo_core/`
  - Library module (CMake-based native build). CI installs CMake 3.22.1
- `.github/workflows/`
  - `android-ci.yml`: assembleDebug + tests; optional signed assembleRelease with repo secrets
  - `codeql.yml`: CodeQL (Java/Kotlin + C/C++)
- `docs/`
  - `CI_SETUP.md`: CI/CL overview, secrets, troubleshooting
  - `TESTING_GUIDE.md`: How to run unit, native, and instrumented tests; rooted Zygisk flow
  - `snippet/MockWebServerSample.md`: instrumentation networking with MockWebServer
  - `snippet/ZygiskEncRootModule.md`: minimal Zygisk Enc Root module scaffold
- `keys/` (ignored)
  - Local keystores for developer machines. Never committed
- Root build files
  - `build.gradle`: common repos, AGP/Kotlin versions, `ext.ndkVersion`
  - `gradle.properties`: `android.ndkVersion=27.1.12297006` and AndroidX flags
  - `settings.gradle`: modules inclusion

## Build requirements

- JDK 17
- Android SDK Platform 36 + Build-Tools 36.0.0
- NDK 27.1.12297006
- Gradle wrapper (`./gradlew`)

## Build locally

```bash
# Windows PowerShell / macOS / Linux (Java 17 required)
./gradlew :app:assembleDebug
```

Optional release (locally): either use `signing.properties` + `signing.gradle` (kept out of VCS) or pass properties at runtime:

```bash
./gradlew :app:assembleRelease \
  -Pandroid.injected.signing.store.file=/path/to/release.jks \
  -Pandroid.injected.signing.store.password=*** \
  -Pandroid.injected.signing.key.alias=*** \
  -Pandroid.injected.signing.key.password=***
```

## CI pipelines

- Android CI
  - Installs SDK/NDK/CMake
  - Builds `:app:assembleDebug`, runs unit tests
  - If the following repository secrets exist, builds signed release and uploads artifacts:
    - `ANDROID_KEYSTORE_BASE64`
    - `ANDROID_KEYSTORE_PASSWORD`
    - `ANDROID_KEY_ALIAS`
    - `ANDROID_KEY_ALIAS_PASSWORD`
- CodeQL
  - Initializes for `java, cpp`
  - Tries autobuild, falls back to Gradle `assembleDebug`

## Testing

- See `docs/TESTING_GUIDE.md` for detailed instructions and troubleshooting.
- Instrumented tests run on emulator in CI (non-blocking job: Instrumented Tests).

## Security & secrets

- Keystores and `signing.properties` are ignored by `.gitignore`
- Base64 keystore files (e.g., `*.b64`, `*.b64.txt`) are ignored and must not be committed
- CI restores keystore from `ANDROID_KEYSTORE_BASE64` and injects signing via Gradle properties

## Native build notes

- App module uses ndk-build with `Android.mk` under `app/src/main/cpp`
- `mundo_core` uses CMake; CI installs CMake 3.22.1

## Troubleshooting

- NDK mismatch: ensure `gradle.properties` and CI agree on `android.ndkVersion`
- Missing SDK/platforms: update SDK Platform to 36 and Build-Tools to 36.0.0
- Signing failures in CI: verify all four repo secrets are present and correct

## Roadmap / Refactor plan

See `BEARMOD_REFACTOR_SUMMARY.md` for JNI bridge refactor, naming, and testing checklist.
