---
description: "Example description"
---

---
Type: "manual"
Description: "Example description"---
# BearMod v2.0 - Android Injection Project Summary

## Project Purpose
BearMod is an Android application injection/modification framework designed for manipulating non-rooted devices. The project includes native C++ components, Java/Kotlin Android code, and JavaScript injection scripts.

## Main Components

### Main Application Module
- **Entry Point**- [app/src/main/java/com/bearmod/MainActivity.java](mdc:app/src/main/java/com/bearmod/MainActivity.java)
- **Build Config**- [app/build.gradle](mdc:app/build.gradle)
- **Manifest**- [app/src/main/AndroidManifest.xml](mdc:app/src/main/AndroidManifest.xml)

### Native C++ Components
- **Anti-Detection**- [app/src/main/cpp/antihook/](mdc:app/src/main/cpp/antihook/)
- **Non-Root Manager**- [app/src/main/cpp/nonroot/](mdc:app/src/main/cpp/nonroot/)
- **Build System**- [app/src/main/cpp/Android.mk](mdc:app/src/main/cpp/Android.mk)

### JavaScript Injection Scripts
- ** Gadget Manager**: [app/src/main/assets/script/nonroot/gadget_manager.js](mdc:app/src/main/assets/script/nonroot/gadget_manager.js)
- **Injection Controller**: [app/src/main/assets/script/nonroot/injection_controller.js](mdc:app/src/main/assets/script/nonroot/injection_controller.js)
- ** Anti-Detection Scripts**: [app/src/main/assets/script/patches/anti-detection/](mdc:app/src/main/assets/script/patches/anti-detection/)

### Mundo Core Module // Future update
- **Core Library**- [mundo_core/src/main/cpp/core/mundo_core.cpp](mdc:mundo_core/src/main/cpp/core/mundo_core.cpp)
- **Frida Integration**- [mundo_core/src/main/cpp/integration/frida_integration.cpp](mdc:mundo_core/src/main/cpp/integration/frida_integration.cpp)

## Development Guidelines
- **Target SDK**- Android 35 (API 35)
- **Minimum SDK**- Android 28 (API 28)
- **Architecture**- ARM64-v8a based
- **APK Size Limit**: Maximum 20MB
- **Java Version**: 17
- **NDK Version**: 27.1.12297006

## Security Considerations
- No hardcoded API keys or secrets
- Sensitive data should be encrypted.
- Error logging in release builds is reduced.
- Anti-hooking mechanisms are required.
- Input validation on all user input.

## Build commands.
- **Debug**- `./gradlew assembleDebug` or `build.bat debug`
- **Release**- `./gradlew assembleRelease` or `build.bat release`
- **Clean**- `./gradlew clean` or `build.bat clean`
Description-
globs-
alwaysApply: false
---