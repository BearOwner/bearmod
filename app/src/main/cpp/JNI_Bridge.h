#ifndef JNI_BRIDGE_H
#define JNI_BRIDGE_H

#include <jni.h>
#include <android/log.h>

// ========================================
// JNI Bridge Header for BearMod
// ========================================
// This file contains all JNI function declarations and registration
// for the BearMod project. It serves as a central reference for
// all native method implementations.

// ========================================
// Floating Service JNI Functions
// ========================================

// String return functions
extern "C" JNIEXPORT jstring JNICALL
Java_com_bearmod_Floating_onlinename(JNIEnv *env, jobject thiz);

extern "C" JNIEXPORT jstring JNICALL
Java_com_bearmod_Floating_channellink(JNIEnv *env, jobject activityObject);

extern "C" JNIEXPORT jstring JNICALL
Java_com_bearmod_Floating_feedbacklink(JNIEnv *env, jobject activityObject);

extern "C" JNIEXPORT jstring JNICALL
Java_com_bearmod_Floating_ChannelName(JNIEnv *env, jobject activityObject);

extern "C" JNIEXPORT jstring JNICALL
Java_com_bearmod_Floating_FeedBackName(JNIEnv *env, jobject activityObject);

extern "C" JNIEXPORT jstring JNICALL
Java_com_bearmod_Floating_cfg(JNIEnv *env, jobject activityObject);

extern "C" JNIEXPORT jstring JNICALL
Java_com_bearmod_Floating_iconenc(JNIEnv *env, jobject thiz);

// Control functions
extern "C" JNIEXPORT void JNICALL
Java_com_bearmod_Floating_Switch(JNIEnv *env, jobject thiz, jint i);

extern "C" JNIEXPORT jboolean JNICALL
Java_com_bearmod_Floating_IsESPActive(JNIEnv *env, jobject thiz);

extern "C" JNIEXPORT void JNICALL
Java_com_bearmod_Floating_DrawOn(JNIEnv *env, jclass type, jobject espView, jobject canvas);

extern "C" JNIEXPORT jboolean JNICALL
Java_com_bearmod_Floating_IsHideEsp(JNIEnv *env, jobject thiz);

// Configuration function
extern "C" JNIEXPORT void JNICALL
Java_com_bearmod_Floating_onSendConfig(JNIEnv *env, jobject thiz, jstring s, jstring v);

// ========================================
// LoginActivity JNI Functions
// ========================================

extern "C" JNIEXPORT void JNICALL
Java_com_bearmod_activity_LoginActivity_Init(JNIEnv *env, jclass clazz, jobject m_context);

extern "C" JNIEXPORT void JNICALL
Java_com_bearmod_activity_LoginActivity_updateAuthenticationState(JNIEnv *env, jclass clazz, 
    jstring sessionId, jstring token, jstring hwid, jboolean isValid);

// ========================================
// SimpleLicenseVerifier JNI Functions
// ========================================

extern "C" JNIEXPORT jstring JNICALL
Java_com_bearmod_auth_SimpleLicenseVerifier_ID(JNIEnv *env, jobject thiz);

// ========================================
// NonRootPatchManager JNI Functions
// ========================================

extern "C" JNIEXPORT jboolean JNICALL
Java_com_bearmod_patch_NonRootPatchManager_nativeInjectPatchNative(JNIEnv *env, jobject thiz,
    jstring targetPackage, jstring patchId, jstring scriptContent);

// ========================================
// NonRootManager JNI Functions (if class exists)
// ========================================

extern "C" JNIEXPORT jboolean JNICALL
Java_com_bearmod_plugin_NonRootManager_initializeGadgetManager(JNIEnv *env, jobject thiz,
    jobject context, jstring configJson);

extern "C" JNIEXPORT jboolean JNICALL
Java_com_bearmod_plugin_NonRootManager_injectToPackage(JNIEnv *env, jobject thiz,
    jstring targetPackage, jstring scriptContent);

extern "C" JNIEXPORT jint JNICALL
Java_com_bearmod_plugin_NonRootManager_getInjectionStatusNative(JNIEnv *env, jobject thiz);

extern "C" JNIEXPORT jstring JNICALL
Java_com_bearmod_plugin_NonRootManager_getLastErrorNative(JNIEnv *env, jobject thiz);

extern "C" JNIEXPORT void JNICALL
Java_com_bearmod_plugin_NonRootManager_shutdownGadgetManager(JNIEnv *env, jobject thiz);

extern "C" JNIEXPORT jboolean JNICALL
Java_com_bearmod_plugin_NonRootManager_isNonRootSupportedNative(JNIEnv *env, jclass clazz);

extern "C" JNIEXPORT jboolean JNICALL
Java_com_bearmod_plugin_NonRootManager_validateSystemRequirementsNative(JNIEnv *env, jclass clazz);

// ========================================
// Security Manager JNI Functions (if class exists)
// ========================================

extern "C" JNIEXPORT jboolean JNICALL
Java_com_bearmod_loader_security_NativeSecurityManager_nativeDetectFrida(JNIEnv *env, jobject thiz);

extern "C" JNIEXPORT void JNICALL
Java_com_bearmod_loader_security_NativeSecurityManager_nativeStartAntiHookMonitoring(JNIEnv *env, jobject thiz);

extern "C" JNIEXPORT jboolean JNICALL
Java_com_bearmod_loader_security_NativeSecurityManager_nativeVerifyIntegrity(JNIEnv *env, jobject thiz);

// ========================================
// JNI Registration Functions
// ========================================

// Register Floating service native methods
int RegisterFloatingNatives(JNIEnv *env);

// Register LoginActivity native methods
int RegisterLoginActivityNatives(JNIEnv *env);

// Register SimpleLicenseVerifier native methods
int RegisterSimpleLicenseVerifierNatives(JNIEnv *env);

// Register NonRootPatchManager native methods
int RegisterNonRootPatchManagerNatives(JNIEnv *env);

// Register NonRootManager native methods (if class exists)
int RegisterNonRootManagerNatives(JNIEnv *env);

// Register Security Manager native methods (if class exists)
int RegisterSecurityManagerNatives(JNIEnv *env);

// ========================================
// Utility Functions
// ========================================

// Helper function to safely get string from JNI
const char* GetJNIString(JNIEnv *env, jstring jstr);

// Helper function to safely release JNI string
void ReleaseJNIString(JNIEnv *env, jstring jstr, const char* cstr);

// Helper function to log JNI errors
void LogJNIError(JNIEnv *env, const char* function, const char* error);

// ========================================
// JNI OnLoad/OnUnload
// ========================================

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved);
JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *vm, void *reserved);

#endif // JNI_BRIDGE_H
