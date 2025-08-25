# Rooted Test Checklist (Zygisk)

Use this checklist to validate JNI and integration on rooted devices using a minimal Zygisk Enc Root module.

## Prepare
- Device with Magisk + Zygisk enabled
- Build host loader shared object (e.g., `libzygisk_bearmod.so`)
- `bearmod.so` placed alongside or inside module assets per scaffold

## Install Module
- Package module per `docs/snippet/ZygiskEncRootModule.md`
- Flash/install in Magisk, enable, reboot

## Verify load
- Collect `logcat` after boot:
  - Expect logs from loader and `JNI_OnLoad` inside `bearmod.so`
- Confirm target app is in the allowlist / matches process filter

## Runtime checks
- Launch target app
- From in-app or via test harness, call `com.bearmod.bridge.NativeLib.getVersion()`
  - Expect non-empty version string
- Exercise license flow if applicable (mock endpoints or dev backend)

## Troubleshooting
- If `JNI_OnLoad` not called:
  - Verify `abiFilters` match device ABI
  - Check SELinux denials / Zygisk denylist
  - Ensure correct process package filter
- If symbols unresolved:
  - Rebuild with correct NDK/stdlib settings
  - Check `minSdk` and `android:extractNativeLibs` packing

## Safety
- Guard experimental code with `#ifdef ROOT_MODE`
- Keep module minimal and documented; avoid persistent hooks until validated
