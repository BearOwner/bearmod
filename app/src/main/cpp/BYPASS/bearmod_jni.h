#ifndef BEARMOD_JNI_H
#define BEARMOD_JNI_H

#include <jni.h>
#include <android/log.h>

// Constants
#define BEARMOD_VERSION "3.9.0"
#define MIN_ANDROID_SDK 24
#define EXPECTED_LOADER_PACKAGE "com.bearmod"

// Logging macros
#define LOG_TAG "BearMod"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

#endif // BEARMOD_JNI_H
