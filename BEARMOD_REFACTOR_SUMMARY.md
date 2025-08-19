# BearMod Codebase – Current State and Refactor Plan

## Overview
This document summarizes the current JNI/auth architecture, what works today, and the planned refactor that separates concerns while preserving the working KeyAuth Java flow. It also lists benefits, trade-offs, and a rollback plan.

## Current State (as of now)
- Java KeyAuth flow (`com.bearmod.auth.SimpleLicenseVerifier`) works end-to-end: license/HWID/token handling.
- Native methods are registered via a centralized hub: `app/src/main/cpp/JNI_Bridge.cpp` and `JNI_Bridge.h`.
- Legacy native code lives in `NRG.h` (ESP/UE4/hooks, auth gating with `g_Token/g_Auth`, configs).
- `LoginActivity` invokes a native Init path; legacy Init logic previously lived in `NRG.h`/`Main.cpp`.
- Missing or duplicated Init logic caused past JNI crashes; these were stabilized by aligning Java declarations with registered natives.

## Key Decisions
- Keep `JNI_Bridge.cpp` as the single `JNI_OnLoad` owner and registration hub.
- Preserve the Java KeyAuth API; native side consumes auth state only.
- Introduce a small Java facade `com.bearmod.bridge.NativeLib` to bridge Java→C++ for init and token handoff.
- Rename C++ files for clarity: `Main.cpp` → `bearmod.cpp`, `NRG.h` → `bearmod.h`.

## Target Architecture (separation of concerns)

### Java (UI + Lifecycle)
- `SplashActivity`
  - Bootstrap only. Calls `NativeLib.initialize(context)` once.
  - On failure → `CrashActivity`. On success → `LoginActivity`.
- `LoginActivity`
  - KeyAuth-only UI. On success, calls `NativeLib.setAuthToken(token)` and `NativeLib.setAuth(token)`; starts overlay/services; navigates to `MainActivity`.
- `MainActivity`
  - Feature hub (ESP/overlay toggles). Reads `NativeLib.isAuthValid()`; no init or duplicate security.
- `ModeActivity` (rename of `TargetAppManager`)
  - Optional: game variant selection; may call target verification JNI.
- `CrashHandler` + `CrashActivity`
  - Global crash capture and display; no JNI calls here.
- `App` (Application class)
  - Registers `CrashHandler` in `onCreate()`.

### JNI / C++
- `JNI_Bridge.cpp`
  - Single hub for `JNI_OnLoad` and `RegisterNatives`.
  - Registers `com/bearmod/bridge/NativeLib` methods:
    - `initialize(Context)`
    - `setAuthToken(String)`, `setAuth(String)`, `clearAuth()`
    - `isAuthValid(): boolean`, `getVersion(): String`
  - Delegates to functions implemented in `bearmod.cpp`.
- `bearmod.h` (rename of `NRG.h`)
  - Declarations for globals/configs and feature APIs: `g_Token`, `g_Auth`, `isModEnabled()`, `DrawESP(...)`, `Config` map.
- `bearmod.cpp` (rename of `Main.cpp` content + `NRG.h` implementations where applicable)
  - Implements auth setters/getters and gating (`isAuthValid()`), `isModEnabled()`, `DrawESP(...)`, overlay toggles.
  - No `JNI_OnLoad` or direct registrations here.
- Optional:
  - `bear_trust_verifier.cpp`: loader/self-trust checks, JNI under `com/bearmod/security/BearTrust`.
  - `signature_verifier.cpp`: target (PUBG) verification, JNI under `com/bearmod/security/NativeSecurityManager`.

## Benefits
- Clear ownership: one JNI hub; UI free of init/security duplication.
- Safer startup: minimal work in `JNI_OnLoad`; crashes easier to localize (splash → JNI; login → auth; main → features).
- Maintainable: Java KeyAuth remains canonical; C++ consumes tokens via explicit API.
- Easier rollbacks: file renames clarify responsibilities; Git PR captures each step.

## Trade-offs / Risks
- Short-term stubs: minimal `initialize()` and token bridge land first; some legacy Init logic will be ported incrementally.
- File renames require CMake/Gradle updates and include fixes.
- JNI signature mismatches can reappear if Java declarations or class names drift.

## Implementation Steps (high-level)
1. Add `com.bearmod.bridge.NativeLib` with native declarations (no change to KeyAuth).
2. Implement and register the above methods in `JNI_Bridge.cpp`.
3. Refactor `SplashActivity` to call `NativeLib.initialize(context)` once.
4. Update `LoginActivity` success path to call `NativeLib.setAuthToken(token)` and `NativeLib.setAuth(token)` then start overlay.
5. Rename `NRG.h` → `bearmod.h`, `Main.cpp` → `bearmod.cpp`; update includes/build config.
6. Port minimal required Init logic from legacy into `bearmod.cpp` (invoked by `NativeLib.initialize`).
7. Optional: restore trust/target verifiers as separate modules; register via `JNI_Bridge.cpp`.

## Rollback Plan
- Each change will be a separate Git commit on a feature branch (`refactor/native-bridge`).
- PR into `main` with clear diffs and CI build.
- If issues arise, revert the specific commit or close the PR; `main` remains stable.

## Testing Checklist
- App starts; no JNI load-time errors.
- Splash → Login → Main flow works.
- After KeyAuth success, `isAuthValid()` returns true; overlay/ESP available.
- Negative path: `clearAuth()` disables features (`DrawESP()` exits early).

## Naming & Conventions
- Java bridge class: `com.bearmod.bridge.NativeLib`.
- C++ files: `JNI_Bridge.cpp`, `bearmod.h`, `bearmod.cpp`.
- Keep ProGuard/R8 rules for JNI classes and native methods.
