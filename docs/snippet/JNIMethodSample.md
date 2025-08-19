# JNI Method Pattern Sample

Java declaration and native implementation with docs and resource safety.

```java
// Java side
/**
 * Returns native library version string.
 * @return version string, never null. On error, may return "unknown".
 */
public static native String getVersion();
```

```c++
// C++ side
#include <jni.h>
#include "JNI_Bridge.h" // your central header

/**
 * JNI: com.bearmod.bridge.NativeLib.getVersion()
 *
 * Params: (JNIEnv* env, jclass clazz)
 * Return: jstring (UTF-8)
 * Errors: returns a fallback string and logs via LogJNIError on failures
 * Threading: safe from any thread; no global state mutated
 */
extern "C" JNIEXPORT jstring JNICALL
Java_com_bearmod_bridge_NativeLib_getVersion(JNIEnv* env, jclass clazz) {
    (void)clazz; // unused
    const char* kFallback = "unknown";
    const char* version = nullptr;
    try {
        version = "1.0.0"; // compute or read from your lib
        return env->NewStringUTF(version);
    } catch (...) {
        LogJNIError(env, "getVersion failed");
        return env->NewStringUTF(kFallback);
    }
}
```

Registration sample (optional):

```c++
static JNINativeMethod kMethods[] = {
    {"getVersion", "()Ljava/lang/String;", (void*)Java_com_bearmod_bridge_NativeLib_getVersion},
};
```

Notes:
- Always release acquired resources (e.g., `GetStringUTFChars` → `ReleaseStringUTFChars`).
- Prefer safe fallbacks and log with `LogJNIError` for diagnosis.
