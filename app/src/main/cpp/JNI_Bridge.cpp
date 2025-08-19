#include "JNI_Bridge.h"
#include "Helper/Includes.h"
// #include "NRG.h"  // Commented out to avoid duplicate symbols
// #include "MOD/LOGO.h"  // Commented out to avoid duplicate symbols
// #include "nonroot/frida_gadget_manager.h"  // Commented out due to missing dependencies

// Provide a safe fallback for OBFUSCATE macro if not defined by other headers
#ifndef OBFUSCATE
#define OBFUSCATE(x) x
#endif

// ========================================
// NativeLib Java facade backing storage
// ========================================
namespace {
    static std::string s_authToken;
    static std::string s_authUser;
}

extern "C" {
// NativeLib JNI implementations (static methods)
JNIEXPORT void JNICALL Java_com_bearmod_bridge_NativeLib_initialize(JNIEnv* env, jclass clazz, jobject context) {
    (void)env; (void)clazz; (void)context;
    __android_log_print(ANDROID_LOG_INFO, "BearMod", "NativeLib.initialize called");
}

JNIEXPORT void JNICALL Java_com_bearmod_bridge_NativeLib_setAuthToken(JNIEnv* env, jclass clazz, jstring token) {
    (void)clazz;
    const char* c = token ? env->GetStringUTFChars(token, 0) : nullptr;
    s_authToken = c ? c : "";
    if (c) env->ReleaseStringUTFChars(token, c);
    __android_log_print(ANDROID_LOG_INFO, "BearMod", "NativeLib.setAuthToken called, length=%zu", s_authToken.size());
}

JNIEXPORT void JNICALL Java_com_bearmod_bridge_NativeLib_setAuth(JNIEnv* env, jclass clazz, jstring user, jstring token) {
    (void)clazz;
    const char* cu = user ? env->GetStringUTFChars(user, 0) : nullptr;
    const char* ct = token ? env->GetStringUTFChars(token, 0) : nullptr;
    s_authUser  = cu ? cu : "";
    s_authToken = ct ? ct : s_authToken;
    if (cu) env->ReleaseStringUTFChars(user, cu);
    if (ct) env->ReleaseStringUTFChars(token, ct);
    __android_log_print(ANDROID_LOG_INFO, "BearMod", "NativeLib.setAuth called, user_len=%zu, token_len=%zu", s_authUser.size(), s_authToken.size());
}

JNIEXPORT void JNICALL Java_com_bearmod_bridge_NativeLib_clearAuth(JNIEnv* env, jclass clazz) {
    (void)env; (void)clazz;
    s_authUser.clear();
    s_authToken.clear();
    __android_log_print(ANDROID_LOG_INFO, "BearMod", "NativeLib.clearAuth called");
}

JNIEXPORT jboolean JNICALL Java_com_bearmod_bridge_NativeLib_isAuthValid(JNIEnv* env, jclass clazz) {
    (void)env; (void)clazz;
    // Minimal check: token present. Real check remains under Java KeyAuth.
    return (s_authToken.empty() ? JNI_FALSE : JNI_TRUE);
}

JNIEXPORT jstring JNICALL Java_com_bearmod_bridge_NativeLib_getVersion(JNIEnv* env, jclass clazz) {
    (void)clazz;
    return env->NewStringUTF("1.0");
}
}

// ========================================
// Utility Functions Implementation
// ========================================

const char* GetJNIString(JNIEnv *env, jstring jstr) {
    if (jstr == nullptr) {
        return nullptr;
    }

int RegisterNativeLibNatives(JNIEnv *env) {
    JNINativeMethod methods[] = {
        {"initialize", "(Landroid/content/Context;)V", (void*) Java_com_bearmod_bridge_NativeLib_initialize},
        {"setAuthToken", "(Ljava/lang/String;)V", (void*) Java_com_bearmod_bridge_NativeLib_setAuthToken},
        {"setAuth", "(Ljava/lang/String;Ljava/lang/String;)V", (void*) Java_com_bearmod_bridge_NativeLib_setAuth},
        {"clearAuth", "()V", (void*) Java_com_bearmod_bridge_NativeLib_clearAuth},
        {"isAuthValid", "()Z", (void*) Java_com_bearmod_bridge_NativeLib_isAuthValid},
        {"getVersion", "()Ljava/lang/String;", (void*) Java_com_bearmod_bridge_NativeLib_getVersion},
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
JNIEXPORT jstring JNICALL Java_com_bearmod_Floating_iconenc(JNIEnv* env, jobject thiz) {
    __android_log_print(ANDROID_LOG_WARN, "BearMod", "Stub: Floating.iconenc()");
    return env->NewStringUTF("");
}

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
Java_com_bearmod_Floating_ChannelName(JNIEnv *env, jobject activityObject) {
    return env->NewStringUTF(OBFUSCATE("JOIN OFFICIAL CHANNEL"));
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_bearmod_Floating_FeedBackName(JNIEnv *env, jobject activityObject) {
    return env->NewStringUTF(OBFUSCATE("FeedBack"));
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_bearmod_Floating_cfg(JNIEnv *env, jobject activityObject) {
    return env->NewStringUTF(OBFUSCATE("NRG_SaveFile.cfg"));
}

// Note: iconenc, Switch, IsESPActive, DrawOn, and LoginActivity_Init are already implemented in other files
// Only implementing the missing ones:

extern "C" JNIEXPORT jboolean JNICALL
Java_com_bearmod_Floating_IsHideEsp(JNIEnv *env, jobject thiz) {
    // Simple implementation - can be enhanced
    return JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL
Java_com_bearmod_Floating_onSendConfig(JNIEnv *env, jobject thiz, jstring s, jstring v) {
    const char *config = GetJNIString(env, s);
    const char *value = GetJNIString(env, v);
    
    __android_log_print(ANDROID_LOG_INFO, "BearMod", "onSendConfig called: %s = %s", 
                       config ? config : "null", value ? value : "null");
    
    // Actual implementation would go here
    
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
    
    // Actual implementation would go here
    
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
        {"ChannelName", "()Ljava/lang/String;", (void *) Java_com_bearmod_Floating_ChannelName},
        {"FeedBackName", "()Ljava/lang/String;", (void *) Java_com_bearmod_Floating_FeedBackName},
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
        {"Init", "(Landroid/content/Context;)V", (void *) Java_com_bearmod_activity_LoginActivity_Init},
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

int RegisterNonRootManagerNatives(JNIEnv *env) {
    JNINativeMethod methods[] = {
        {"initializeGadgetManager", "(Landroid/content/Context;Ljava/lang/String;)Z", 
         (void *) Java_com_bearmod_plugin_NonRootManager_initializeGadgetManager},
        {"injectToPackage", "(Ljava/lang/String;Ljava/lang/String;)Z", 
         (void *) Java_com_bearmod_plugin_NonRootManager_injectToPackage},
        {"getInjectionStatusNative", "()I", 
         (void *) Java_com_bearmod_plugin_NonRootManager_getInjectionStatusNative},
        {"getLastErrorNative", "()Ljava/lang/String;", 
         (void *) Java_com_bearmod_plugin_NonRootManager_getLastErrorNative},
        {"shutdownGadgetManager", "()V", 
         (void *) Java_com_bearmod_plugin_NonRootManager_shutdownGadgetManager},
        {"isNonRootSupportedNative", "()Z", 
         (void *) Java_com_bearmod_plugin_NonRootManager_isNonRootSupportedNative},
        {"validateSystemRequirementsNative", "()Z", 
         (void *) Java_com_bearmod_plugin_NonRootManager_validateSystemRequirementsNative}
    };
    
    jclass clazz = env->FindClass("com/bearmod/plugin/NonRootManager");
    if (!clazz) {
        __android_log_print(ANDROID_LOG_WARN, "BearMod", "NonRootManager class not found, skipping registration");
        return 0; // Not an error, class might not exist
    }

    if (env->RegisterNatives(clazz, methods, sizeof(methods) / sizeof(methods[0])) != 0) {
        LogJNIError(env, "RegisterNonRootManagerNatives", "Failed to register NonRootManager natives");
        return -1;
    }

    __android_log_print(ANDROID_LOG_INFO, "BearMod", "NonRootManager natives registered successfully");
    return 0;
}

int RegisterSecurityManagerNatives(JNIEnv *env) {
    JNINativeMethod methods[] = {
        {"nativeDetectFrida", "()Z", 
         (void *) Java_com_bearmod_loader_security_NativeSecurityManager_nativeDetectFrida},
        {"nativeStartAntiHookMonitoring", "()V", 
         (void *) Java_com_bearmod_loader_security_NativeSecurityManager_nativeStartAntiHookMonitoring},
        {"nativeVerifyIntegrity", "()Z", 
         (void *) Java_com_bearmod_loader_security_NativeSecurityManager_nativeVerifyIntegrity}
    };
    
    jclass clazz = env->FindClass("com/bearmod/loader/security/NativeSecurityManager");
    if (!clazz) {
        __android_log_print(ANDROID_LOG_WARN, "BearMod", "NativeSecurityManager class not found, skipping registration");
        return 0; // Not an error, class might not exist
    }

    if (env->RegisterNatives(clazz, methods, sizeof(methods) / sizeof(methods[0])) != 0) {
        LogJNIError(env, "RegisterSecurityManagerNatives", "Failed to register NativeSecurityManager natives");
        return -1;
    }

    __android_log_print(ANDROID_LOG_INFO, "BearMod", "NativeSecurityManager natives registered successfully");
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

    __android_log_print(ANDROID_LOG_INFO, "BearMod", "JNI_OnLoad called - registering native methods");

    // Register all native methods
    if (RegisterFloatingNatives(env) != 0) {
        __android_log_print(ANDROID_LOG_ERROR, "BearMod", "Failed to register Floating natives");
        return -1;
    }

    if (RegisterLoginActivityNatives(env) != 0) {
        __android_log_print(ANDROID_LOG_ERROR, "BearMod", "Failed to register LoginActivity natives");
        return -1;
    }

    if (RegisterSimpleLicenseVerifierNatives(env) != 0) {
        __android_log_print(ANDROID_LOG_ERROR, "BearMod", "Failed to register SimpleLicenseVerifier natives");
        return -1;
    }

    if (RegisterNonRootPatchManagerNatives(env) != 0) {
        __android_log_print(ANDROID_LOG_ERROR, "BearMod", "Failed to register NonRootPatchManager natives");
        return -1;
    }

    if (RegisterNonRootManagerNatives(env) != 0) {
        __android_log_print(ANDROID_LOG_ERROR, "BearMod", "Failed to register NonRootManager natives");
        return -1;
    }

    if (RegisterSecurityManagerNatives(env) != 0) {
        __android_log_print(ANDROID_LOG_ERROR, "BearMod", "Failed to register SecurityManager natives");
        return -1;
    }

    if (RegisterNativeLibNatives(env) != 0) {
        __android_log_print(ANDROID_LOG_ERROR, "BearMod", "Failed to register NativeLib natives");
        return -1;
    }

    // Start background threads (disabled here to keep JNI_OnLoad minimal and avoid undefined symbols).
    // If needed, start threads explicitly after app initialization in Java-side Init.
    // pthread_t t;
    // extern void* Init_Thread(void*);
    // extern void* maps_thread(void*);
    // pthread_create(&t, nullptr, Init_Thread, 0);
    // pthread_create(&t, nullptr, maps_thread, 0);

    __android_log_print(ANDROID_LOG_INFO, "BearMod", "JNI_OnLoad completed successfully");
    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *vm, void *reserved) {
    __android_log_print(ANDROID_LOG_INFO, "BearMod", "JNI_OnUnload called");
    
    // Cleanup any resources here
    // Note: JNI_OnUnload is rarely called in Android, so don't rely on it for cleanup
}
