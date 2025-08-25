# Zygisk Enc Root Module Scaffold (Minimal)

Purpose: Provide a minimal host loader to inject `bearmod.so` into the target app process early (zygote stage) and execute `JNI_OnLoad`.

This is a documentation-only scaffold. Build and packaging as a Magisk/Zygisk module is required on a rooted device.

## Structure
- Module package (e.g., `zygisk-bearmod/`)
  - `module.prop` (module metadata)
  - `zygisk/` (compiled .so payloads by ABI)
    - `arm64-v8a/libzygisk_bearmod.so` (host loader)
    - `armeabi-v7a/libzygisk_bearmod.so`
  - `system_ext/` (optional) or assets needed for config

Your own native library to inject:
- `/data/adb/modules/zygisk-bearmod/bearmod/arm64-v8a/bearmod.so`

## Host loader responsibilities
- Register with Zygisk API, filter target app (e.g., package name contains `com.tencent.`)
- On app process creation, load `bearmod.so` with `dlopen`
- Ensure `JNI_OnLoad(JavaVM*, void*)` resolves and runs successfully
- Optionally export bridge symbols or configuration via properties/props

## Pseudocode (C++)
```cpp
// libzygisk_bearmod.cpp (host loader)
#include <dlfcn.h>
#include <jni.h>
#include "zygisk.hpp" // Zygisk API headers from NDK or module template

class BearModModule : public zygisk::ModuleBase {
 public:
  void onLoad(zygisk::Api *api, JNIEnv *env) override {
    this->api_ = api;
  }

  void preAppSpecialize(zygisk::AppSpecializeArgs *args) override {
    // Filter target process by package name
    const char* pkg = args->nice_name; // or use args->app_data_dir etc.
    if (!pkg || !strstr(pkg, "com.tencent.")) return;

    // Map target arch path for bearmod.so
    const char* so_path = "/data/adb/modules/zygisk-bearmod/bearmod/arm64-v8a/bearmod.so";
    void* handle = dlopen(so_path, RTLD_NOW);
    if (!handle) return;

    using JNI_OnLoad_t = jint(*)(JavaVM*, void*);
    JNI_OnLoad_t jni_onload = (JNI_OnLoad_t)dlsym(handle, "JNI_OnLoad");
    if (!jni_onload) return;

    JavaVM* vm = nullptr;
    api_->get_java_vm(&vm);
    jni_onload(vm, nullptr);
  }

 private:
  zygisk::Api* api_ = nullptr;
};

static BearModModule gModule;

extern "C" void zygisk_module_register(zygisk::Api *api, JNIEnv *env) {
  api->registerModule(&gModule);
}
```

Notes:
- Use correct include paths for Zygisk headers; versions differ.
- Use `abi` detection to compute correct `bearmod.so` path.
- Harden with error logging via `__android_log_print`.

## JNI_Bridge entrypoint
Your `bearmod.so` must export:
```c
jint JNI_OnLoad(JavaVM* vm, void* reserved);
```
Inside it, register your JNI methods and initialize your bridge (e.g., License state, Patch manager):
```cpp
jint JNI_OnLoad(JavaVM* vm, void*) {
  JNIEnv* env = nullptr;
  if (vm->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK) return JNI_ERR;
  // RegisterNatives for classes: com.bearmod.bridge.NativeLib, etc.
  // Initialize patch managers, hooks guarded by ROOT_MODE if needed
  return JNI_VERSION_1_6;
}
```

## Packaging (Magisk/Zygisk)
- Build `libzygisk_bearmod.so` for each ABI.
- Place under `zygisk/<abi>/` inside the module folder.
- Place `bearmod.so` under a known path readable by the module at runtime.
- Install module via Magisk; enable Zygisk; reboot.

## Testing on rooted device
- Enable verbose logging (logcat tag: `BearMod` and `Zygisk`)
- Launch Tencent app; verify `JNI_OnLoad` log lines appear.
- Validate bridge calls (e.g., `NativeLib.getVersion()`) from in-app or via hooks.

## Safety and build flags
- Wrap root-only code with `#ifdef ROOT_MODE` and expose a rootless fallback.
- Keep JNI signatures aligned with Java (`JNI_Bridge.h` / `NativeLib.java`).
- Avoid blocking operations in `JNI_OnLoad`.
