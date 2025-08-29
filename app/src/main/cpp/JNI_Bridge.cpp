#include "JNI_Bridge.h"
#include "Helper/Includes.h"
#include <string>
#include <jni.h>

// #include "nonroot/frida_gadget_manager.h"  // Commented out due to missing dependencies

// Provide a safe fallback for OBFUSCATE macro if not defined by other headers
#ifndef OBFUSCATE
#define OBFUSCATE(x) x
#endif

// No NativeLib auth backing storage; KeyAuth state is bridged via LoginActivity.updateAuthenticationState

// Forward declarations to avoid including heavy NRG.h (which defines globals)
extern "C" void HandleOnSendConfig_Bridge(const char* key, const char* value);
extern std::string g_Auth;
extern std::string g_Token;
extern bool bValid;

// Forward declaration for BearMod JNI initialization (implemented in BYPASS/bearmod_jni.cpp)
extern bool initializeBearModJNI(JavaVM* vm, JNIEnv* env);

// --------------------------------------------------------
// Forward declarations for NativeLib JNI functions (static)
// Needed because we take their addresses in RegisterNativeLibNatives
// --------------------------------------------------------
extern "C" {
JNIEXPORT void JNICALL Java_com_bearmod_bridge_NativeLib_initialize(JNIEnv* env, jclass clazz, jobject context);
}

int RegisterNativeLibNatives(JNIEnv *env) {
    JNINativeMethod methods[] = {
        {"initialize", "(Landroid/content/Context;)V", (void*) Java_com_bearmod_bridge_NativeLib_initialize},
    };

    jclass clazz = env->FindClass("com/bearmod/bridge/NativeLib");
    if (!clazz) {
        __android_log_print(ANDROID_LOG_WARN, "BearMod", "NativeLib class not found, skipping registration");
        return 0; // Not fatal if class absent in some builds
    }
    if (env->RegisterNatives(clazz, methods, sizeof(methods)/sizeof(methods[0])) != 0) {
        LogJNIError(env, "RegisterNativeLibNatives", "Failed to register NativeLib natives");
        return -1;
    }
    __android_log_print(ANDROID_LOG_INFO, "BearMod", "NativeLib natives registered successfully");
    return 0;
}

extern "C" {
// NativeLib JNI implementations (static methods)
JNIEXPORT void JNICALL Java_com_bearmod_bridge_NativeLib_initialize(JNIEnv* env, jclass clazz, jobject context) {
    (void)clazz; (void)context;
    __android_log_print(ANDROID_LOG_INFO, "BearMod", "NativeLib.initialize called - minimal registration");

    int failures = 0;

    // Only ensure NativeLib natives are registered here; other classes should be registered via registerNatives(Class, ...)
    if (RegisterNativeLibNatives(env) != 0) {
        __android_log_print(ANDROID_LOG_ERROR, "BearMod", "initialize: Failed to register NativeLib natives (will continue)");
        if (env->ExceptionCheck()) env->ExceptionClear();
        failures++;
    }

    __android_log_print(ANDROID_LOG_INFO, "BearMod", "NativeLib.initialize completed with %d failure(s)", failures);
}


/* move Bypass.cpp
// New: Register natives for classes provided by Java to avoid FindClass string lookups
JNIEXPORT void JNICALL Java_com_bearmod_bridge_NativeLib_registerNatives(JNIEnv* env, jclass, jclass floatingCls, jclass loginActivityCls, jclass nativeLibCls) {
    int failures = 0;

    if (floatingCls != nullptr) {
        JNINativeMethod methods[] = {
            {"onlinename", "()Ljava/lang/String;", (void *) Java_com_bearmod_Floating_onlinename},
            {"channellink", "()Ljava/lang/String;", (void *) Java_com_bearmod_Floating_channellink},
            {"feedbacklink", "()Ljava/lang/String;", (void *) Java_com_bearmod_Floating_feedbacklink},
            {"cfg", "()Ljava/lang/String;", (void *) Java_com_bearmod_Floating_cfg},
            {"iconenc", "()Ljava/lang/String;", (void *) Java_com_bearmod_Floating_iconenc},
            {"Switch", "(I)V", (void *) Java_com_bearmod_Floating_Switch},
            {"IsESPActive", "()Z", (void *) Java_com_bearmod_Floating_IsESPActive},
            {"DrawOn", "(Lcom/bearmod/ESPView;Landroid/graphics/Canvas;)V", (void *) Java_com_bearmod_Floating_DrawOn},
            {"IsHideEsp", "()Z", (void *) Java_com_bearmod_Floating_IsHideEsp},
            {"onSendConfig", "(Ljava/lang/String;Ljava/lang/String;)V", (void *) Java_com_bearmod_Floating_onSendConfig}
        };
        if (env->RegisterNatives(floatingCls, methods, sizeof(methods)/sizeof(methods[0])) != 0) {
            LogJNIError(env, "registerNatives", "Failed to register Floating natives on provided class");
            if (env->ExceptionCheck()) env->ExceptionClear();
            failures++;
        }
    }

    if (loginActivityCls != nullptr) {
        JNINativeMethod methods[] = {
            {"updateAuthenticationState", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Z)V", (void *) Java_com_bearmod_activity_LoginActivity_updateAuthenticationState}
        };
        if (env->RegisterNatives(loginActivityCls, methods, sizeof(methods)/sizeof(methods[0])) != 0) {
            LogJNIError(env, "registerNatives", "Failed to register LoginActivity natives on provided class");
            if (env->ExceptionCheck()) env->ExceptionClear();
            failures++;
        }
    }

    if (nativeLibCls != nullptr) {
        JNINativeMethod methods[] = {
            {"initialize", "(Landroid/content/Context;)V", (void*) Java_com_bearmod_bridge_NativeLib_initialize},
        };
        if (env->RegisterNatives(nativeLibCls, methods, sizeof(methods)/sizeof(methods[0])) != 0) {
            LogJNIError(env, "registerNatives", "Failed to register NativeLib natives on provided class");
            if (env->ExceptionCheck()) env->ExceptionClear();
            failures++;
        }
    }

    __android_log_print(ANDROID_LOG_INFO, "BearMod", "NativeLib.registerNatives completed with %d failure(s)", failures);
}
*/

// Removed all NativeLib auth-related JNI implementations to avoid duplication with SimpleLicenseVerifier path
}

// ========================================
// Utility Functions Implementation
// ========================================

const char* GetJNIString(JNIEnv *env, jstring jstr) {
    if (jstr == nullptr) {
        return nullptr;
    }
    return env->GetStringUTFChars(jstr, 0);
}

void ReleaseJNIString(JNIEnv *env, jstring jstr, const char* cstr) {
    if (jstr != nullptr && cstr != nullptr) {
        env->ReleaseStringUTFChars(jstr, cstr);
    }
}

void LogJNIError(JNIEnv *env, const char* function, const char* error) {
    __android_log_print(ANDROID_LOG_ERROR, "BearMod", "JNI Error in %s: %s", function, error);
}

// ========================================
// JNI Function Implementations
// ========================================

// Stubs for undefined JNI symbols referenced in registration arrays
// This ensures linking succeeds even if real implementations are not yet provided.
extern "C" {
// NonRootManager stubs
JNIEXPORT jboolean JNICALL Java_com_bearmod_plugin_NonRootManager_initializeGadgetManager(JNIEnv* env, jobject thiz, jobject context, jstring configJson) {
    __android_log_print(ANDROID_LOG_WARN, "BearMod", "Stub: NonRootManager.initializeGadgetManager()");
    return JNI_FALSE;
}
JNIEXPORT jboolean JNICALL Java_com_bearmod_plugin_NonRootManager_injectToPackage(JNIEnv* env, jobject thiz, jstring targetPackage, jstring scriptContent) {
    __android_log_print(ANDROID_LOG_WARN, "BearMod", "Stub: NonRootManager.injectToPackage()");
    return JNI_FALSE;
}
JNIEXPORT jint JNICALL Java_com_bearmod_plugin_NonRootManager_getInjectionStatusNative(JNIEnv* env, jobject thiz) {
    __android_log_print(ANDROID_LOG_WARN, "BearMod", "Stub: NonRootManager.getInjectionStatusNative()");
    return 0;
}
JNIEXPORT jstring JNICALL Java_com_bearmod_plugin_NonRootManager_getLastErrorNative(JNIEnv* env, jobject thiz) {
    __android_log_print(ANDROID_LOG_WARN, "BearMod", "Stub: NonRootManager.getLastErrorNative()");
    return env->NewStringUTF("not implemented");
}
JNIEXPORT void JNICALL Java_com_bearmod_plugin_NonRootManager_shutdownGadgetManager(JNIEnv* env, jobject thiz) {
    __android_log_print(ANDROID_LOG_WARN, "BearMod", "Stub: NonRootManager.shutdownGadgetManager()");
}
JNIEXPORT jboolean JNICALL Java_com_bearmod_plugin_NonRootManager_isNonRootSupportedNative(JNIEnv* env, jclass clazz) {
    __android_log_print(ANDROID_LOG_WARN, "BearMod", "Stub: NonRootManager.isNonRootSupportedNative()");
    return JNI_FALSE;
}
JNIEXPORT jboolean JNICALL Java_com_bearmod_plugin_NonRootManager_validateSystemRequirementsNative(JNIEnv* env, jclass clazz) {
    __android_log_print(ANDROID_LOG_WARN, "BearMod", "Stub: NonRootManager.validateSystemRequirementsNative()");
    return JNI_FALSE;
}

// NativeSecurityManager stubs
JNIEXPORT jboolean JNICALL Java_com_bearmod_loader_security_NativeSecurityManager_nativeDetectFrida(JNIEnv* env, jobject thiz) {
    __android_log_print(ANDROID_LOG_WARN, "BearMod", "Stub: NativeSecurityManager.nativeDetectFrida()");
    return JNI_FALSE;
}
JNIEXPORT void JNICALL Java_com_bearmod_loader_security_NativeSecurityManager_nativeStartAntiHookMonitoring(JNIEnv* env, jobject thiz) {
    __android_log_print(ANDROID_LOG_WARN, "BearMod", "Stub: NativeSecurityManager.nativeStartAntiHookMonitoring()");
}
JNIEXPORT jboolean JNICALL Java_com_bearmod_loader_security_NativeSecurityManager_nativeVerifyIntegrity(JNIEnv* env, jobject thiz) {
    __android_log_print(ANDROID_LOG_WARN, "BearMod", "Stub: NativeSecurityManager.nativeVerifyIntegrity()");
    return JNI_FALSE;
}
}

// Floating Service JNI Implementations
// Note: Some functions are already implemented in NRG.h and MOD/LOGO.h
// Only implementing the missing ones here

extern "C" JNIEXPORT jstring JNICALL
Java_com_bearmod_Floating_onlinename(JNIEnv *env, jobject thiz) {
    return env->NewStringUTF(OBFUSCATE("BEAR-MOD"));
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_bearmod_Floating_channellink(JNIEnv *env, jobject activityObject) {
    return env->NewStringUTF(OBFUSCATE("https://t.me/bear_mod"));
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_bearmod_Floating_feedbacklink(JNIEnv *env, jobject activityObject) {
    return env->NewStringUTF(OBFUSCATE("https://t.me/bearfeedbackbot"));
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_bearmod_Floating_cfg(JNIEnv *env, jobject activityObject) {
    return env->NewStringUTF(OBFUSCATE("NRG_SaveFile.cfg"));
}

// Note: iconenc, Switch, IsESPActive, DrawOn, and LoginActivity_Init are already implemented in other files
// Only implementing the missing ones:

//move Bypass.cpp
//extern "C" JNIEXPORT jboolean JNICALL
//Java_com_bearmod_Floating_IsHideEsp(JNIEnv *env, jobject thiz) {
    // Simple implementation - can be enhanced
   // return JNI_FALSE;
//}

extern "C" JNIEXPORT void JNICALL
Java_com_bearmod_Floating_onSendConfig(JNIEnv *env, jobject thiz, jstring s, jstring v) {
    const char *config = GetJNIString(env, s);
    const char *value = GetJNIString(env, v);
    
    __android_log_print(ANDROID_LOG_INFO, "BearMod", "onSendConfig called: %s = %s", 
                       config ? config : "null", value ? value : "null");
    
    // Forward to business logic handler via bridge implemented in bearmod.cpp
    HandleOnSendConfig_Bridge(config, value);
    
    ReleaseJNIString(env, s, config);
    ReleaseJNIString(env, v, value);
}

// LoginActivity JNI Implementations
extern "C" JNIEXPORT void JNICALL
Java_com_bearmod_activity_LoginActivity_updateAuthenticationState(JNIEnv *env, jclass clazz, 
    jstring sessionId, jstring token, jstring hwid, jboolean isValid) {
    
    const char* sessionStr = GetJNIString(env, sessionId);
    const char* tokenStr = GetJNIString(env, token);
    const char* hwidStr = GetJNIString(env, hwid);
    
    __android_log_print(ANDROID_LOG_INFO, "BearMod", "updateAuthenticationState called - Valid: %s", 
                       isValid ? "true" : "false");
    
    // Update C++ global authentication variables
    if (isValid == JNI_TRUE) {
        g_Auth  = sessionStr ? sessionStr : "";
        g_Token = tokenStr   ? tokenStr   : "";
        bValid  = true;
    } else {
        g_Token.clear();
        g_Auth.clear();
        bValid = false;
    }
    
    ReleaseJNIString(env, sessionId, sessionStr);
    ReleaseJNIString(env, token, tokenStr);
    ReleaseJNIString(env, hwid, hwidStr);
}

// SimpleLicenseVerifier JNI Implementation
extern "C" JNIEXPORT jstring JNICALL
Java_com_bearmod_auth_SimpleLicenseVerifier_ID(JNIEnv *env, jobject thiz) {
    return env->NewStringUTF(OBFUSCATE("BEAR_MOD_LICENSE_ID"));
}

// NonRootPatchManager JNI Implementation
extern "C" JNIEXPORT jboolean JNICALL
Java_com_bearmod_patch_NonRootPatchManager_nativeInjectPatchNative(JNIEnv *env, jobject thiz,
    jstring targetPackage, jstring patchId, jstring scriptContent) {
    
    const char* targetPkg = GetJNIString(env, targetPackage);
    const char* patch = GetJNIString(env, patchId);
    const char* script = GetJNIString(env, scriptContent);
    
    __android_log_print(ANDROID_LOG_INFO, "BearMod", "nativeInjectPatchNative called for package: %s, patch: %s", 
                       targetPkg ? targetPkg : "null", patch ? patch : "null");
    
    // For now, return success (true) - actual implementation would go here
    bool result = true;
    
    ReleaseJNIString(env, targetPackage, targetPkg);
    ReleaseJNIString(env, patchId, patch);
    ReleaseJNIString(env, scriptContent, script);
    
    return result ? JNI_TRUE : JNI_FALSE;
}

// NonRootPatchManager injection method (if needed)
extern "C" JNIEXPORT jboolean JNICALL
Java_com_bearmod_patch_NonRootPatchManager_injection(JNIEnv *env, jobject thiz) {
    __android_log_print(ANDROID_LOG_INFO, "BearMod", "NonRootPatchManager_injection called");
    return JNI_TRUE; // Actual implementation would go here
}

// ========================================
// JNI Registration Functions Implementation
// ========================================

int RegisterFloatingNatives(JNIEnv *env) {
    JNINativeMethod methods[] = {
       {"onlinename", "()Ljava/lang/String;", (void *) Java_com_bearmod_Floating_onlinename},
        {"channellink", "()Ljava/lang/String;", (void *) Java_com_bearmod_Floating_channellink},
        {"feedbacklink", "()Ljava/lang/String;", (void *) Java_com_bearmod_Floating_feedbacklink},
        {"cfg", "()Ljava/lang/String;", (void *) Java_com_bearmod_Floating_cfg},
        {"iconenc", "()Ljava/lang/String;", (void *) Java_com_bearmod_Floating_iconenc},
        {"Switch", "(I)V", (void *) Java_com_bearmod_Floating_Switch},
        {"IsESPActive", "()Z", (void *) Java_com_bearmod_Floating_IsESPActive},
        {"DrawOn", "(Lcom/bearmod/ESPView;Landroid/graphics/Canvas;)V", (void *) Java_com_bearmod_Floating_DrawOn},
        {"IsHideEsp", "()Z", (void *) Java_com_bearmod_Floating_IsHideEsp},
        {"onSendConfig", "(Ljava/lang/String;Ljava/lang/String;)V", (void *) Java_com_bearmod_Floating_onSendConfig}
    };
    
    jclass clazz = env->FindClass("com/bearmod/Floating");
    if (!clazz) {
        LogJNIError(env, "RegisterFloatingNatives", "Could not find com.bearmod.Floating class");
        return -1;
    }

    if (env->RegisterNatives(clazz, methods, sizeof(methods) / sizeof(methods[0])) != 0) {
        LogJNIError(env, "RegisterFloatingNatives", "Failed to register Floating natives");
        return -1;
    }

    __android_log_print(ANDROID_LOG_INFO, "BearMod", "Floating natives registered successfully");
    return 0;
}

int RegisterLoginActivityNatives(JNIEnv *env) {
    JNINativeMethod methods[] = {
        {"updateAuthenticationState", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Z)V", 
         (void *) Java_com_bearmod_activity_LoginActivity_updateAuthenticationState}
    };

    jclass clazz = env->FindClass("com/bearmod/activity/LoginActivity");
    if (!clazz) {
        LogJNIError(env, "RegisterLoginActivityNatives", "Could not find com.bearmod.activity.LoginActivity class");
        return -1;
    }

    if (env->RegisterNatives(clazz, methods, sizeof(methods) / sizeof(methods[0])) != 0) {
        LogJNIError(env, "RegisterLoginActivityNatives", "Failed to register LoginActivity natives");
        return -1;
    }

    __android_log_print(ANDROID_LOG_INFO, "BearMod", "LoginActivity natives registered successfully");
    return 0;
}

int RegisterSimpleLicenseVerifierNatives(JNIEnv *env) {
    JNINativeMethod methods[] = {
        {"ID", "()Ljava/lang/String;", (void *) Java_com_bearmod_auth_SimpleLicenseVerifier_ID}
    };
    
    jclass clazz = env->FindClass("com/bearmod/auth/SimpleLicenseVerifier");
    if (!clazz) {
        __android_log_print(ANDROID_LOG_WARN, "BearMod", "SimpleLicenseVerifier class not found, skipping registration");
        return 0; // Not an error, class might not exist
    }

    if (env->RegisterNatives(clazz, methods, sizeof(methods) / sizeof(methods[0])) != 0) {
        LogJNIError(env, "RegisterSimpleLicenseVerifierNatives", "Failed to register SimpleLicenseVerifier natives");
        return -1;
    }

    __android_log_print(ANDROID_LOG_INFO, "BearMod", "SimpleLicenseVerifier natives registered successfully");
    return 0;
}

int RegisterNonRootPatchManagerNatives(JNIEnv *env) {
    JNINativeMethod methods[] = {
        {"nativeInjectPatchNative", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Z", 
         (void *) Java_com_bearmod_patch_NonRootPatchManager_nativeInjectPatchNative},
        {"injection", "()Z", 
         (void *) Java_com_bearmod_patch_NonRootPatchManager_injection}
    };
    
    jclass clazz = env->FindClass("com/bearmod/patch/NonRootPatchManager");
    if (!clazz) {
        __android_log_print(ANDROID_LOG_WARN, "BearMod", "NonRootPatchManager class not found, skipping registration");
        return 0; // Not an error, class might not exist
    }

    if (env->RegisterNatives(clazz, methods, sizeof(methods) / sizeof(methods[0])) != 0) {
        LogJNIError(env, "RegisterNonRootPatchManagerNatives", "Failed to register NonRootPatchManager natives");
        return -1;
    }

    __android_log_print(ANDROID_LOG_INFO, "BearMod", "NonRootPatchManager natives registered successfully");
    return 0;
}

// ========================================
// JNI OnLoad/OnUnload Implementation
// ========================================

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        __android_log_print(ANDROID_LOG_ERROR, "BearMod", "Failed to get JNI environment");
        return -1;
    }

    // Initialize BearMod JNI core state (gJavaVM, etc.) implemented in bearmod_jni.cpp
    if (!initializeBearModJNI(vm, env)) {
        __android_log_print(ANDROID_LOG_ERROR, "BearMod", "initializeBearModJNI failed");
        return -1;
    }

    __android_log_print(ANDROID_LOG_INFO, "BearMod", "JNI_OnLoad called - registering native methods");

    // Attempt to register native methods; tolerate failures due to class loader timing.
    int failures = 0;
    if (RegisterFloatingNatives(env) != 0) {
        __android_log_print(ANDROID_LOG_ERROR, "BearMod", "OnLoad: Failed to register Floating natives (will continue)");
        if (env->ExceptionCheck()) env->ExceptionClear();
        failures++;
    }

    if (RegisterLoginActivityNatives(env) != 0) {
        __android_log_print(ANDROID_LOG_ERROR, "BearMod", "OnLoad: Failed to register LoginActivity natives (will continue)");
        if (env->ExceptionCheck()) env->ExceptionClear();
        failures++;
    }

    if (RegisterSimpleLicenseVerifierNatives(env) != 0) {
        __android_log_print(ANDROID_LOG_WARN, "BearMod", "OnLoad: SimpleLicenseVerifier natives not registered (optional)");
        if (env->ExceptionCheck()) env->ExceptionClear();
    }

    if (RegisterNonRootPatchManagerNatives(env) != 0) {
        __android_log_print(ANDROID_LOG_WARN, "BearMod", "OnLoad: NonRootPatchManager natives not registered (optional)");
        if (env->ExceptionCheck()) env->ExceptionClear();
    }

    if (RegisterNativeLibNatives(env) != 0) {
        __android_log_print(ANDROID_LOG_ERROR, "BearMod", "OnLoad: Failed to register NativeLib natives (will continue)");
        if (env->ExceptionCheck()) env->ExceptionClear();
        failures++;
    }

    __android_log_print(ANDROID_LOG_INFO, "BearMod", "JNI_OnLoad completed with %d failure(s)", failures);
    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *vm, void *reserved) {
    __android_log_print(ANDROID_LOG_INFO, "BearMod", "JNI_OnUnload called");
    // Cleanup if needed (rarely called on Android)
}