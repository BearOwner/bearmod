# Testing Guide

This guide explains how to run unit tests, native tests, instrumented tests (emulator/device), and rooted Zygisk-based integration tests.

## Overview
- Unit (JVM): fast, runs on CI and locally. Mocks instead of JNI/network where possible.
- Native (GoogleTest): host C++ tests via CMake/ctest.
- Instrumented (androidTest): runs on emulator/device, supports MockWebServer for networking.
- Rooted (Zygisk): process-level tests inside target app context via Enc Root-style module.

## Prerequisites
- JDK 17, Android SDK Platform 36, Build-Tools 36.0.0, NDK 27.1.12297006
- Gradle wrapper available: `./gradlew`

## Commands
- Unit tests:
```bash
./gradlew :app:testDebugUnitTest
```
- Native tests:
```bash
cmake -S app/src/test/cpp -B app/src/test/cpp/build && \
cmake --build app/src/test/cpp/build && \
ctest --test-dir app/src/test/cpp/build --output-on-failure
```
- Instrumented tests (device/emulator):
```bash
./gradlew :app:connectedDebugAndroidTest
```

## Instrumented testing details
- Example tests:
  - `app/src/androidTest/java/com/bearmod/auth/SimpleLicenseVerifierIT.java` (MockWebServer)
  - `app/src/androidTest/java/com/bearmod/bridge/NativeLibIT.java` (native smoke)
- MockWebServer override:
  - `SimpleLicenseVerifier.setApiBaseForTesting(server.url("/").toString())`
  - `SimpleLicenseVerifier.clearApiBaseForTesting()`
- Keep timeouts small and use CountDownLatch/IdlingResource for async.

## CI
- Workflow file: `.github/workflows/android-ci.yml`
  - build job: assemble, unit, lint, checkstyle, native tests, JNI validation, ktlint, cppcheck
  - instrumented-tests job: emulator (API 30) runs `connectedDebugAndroidTest` (non-blocking initially)

## Rooted testing (Zygisk)
- See `docs/snippet/ZygiskEncRootModule.md` for a minimal Enc Root module scaffold.
- Checklist: `docs/ROOTED_TEST_CHECKLIST.md`
  - Build host loader (`libzygisk_bearmod.so`), deploy `bearmod.so`, enable Zygisk, reboot
  - Launch target app, verify `JNI_OnLoad` logs and bridge initialization

## Troubleshooting
- Emulator flakes: bump API to 31, use `-gpu swiftshader_indirect`, disable animations
- JNI signature mismatches: run `python3 jni_validator.py app` or see CI artifact `jni-validation-report`
- Network failures in androidTest: ensure base URL override points to MockWebServer URL
