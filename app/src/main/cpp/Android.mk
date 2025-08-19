LOCAL_PATH := $(call my-dir)

# ==================== Prebuilt Libraries ====================

# libssl
include $(CLEAR_VARS)
LOCAL_MODULE := libssl-prebuilt
LOCAL_SRC_FILES := curl/openssl-android-arm64-v8a/lib/libssl.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/curl/openssl-android-arm64-v8a/include
include $(PREBUILT_STATIC_LIBRARY)

# libcrypto
include $(CLEAR_VARS)
LOCAL_MODULE := libcrypto-prebuilt
LOCAL_SRC_FILES := curl/openssl-android-arm64-v8a/lib/libcrypto.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/curl/openssl-android-arm64-v8a/include
include $(PREBUILT_STATIC_LIBRARY)

# ==================== Main Library ====================
include $(CLEAR_VARS)

# Module name (must match Java System.loadLibrary())
LOCAL_MODULE := bearmod

# Compiler flags (preserve all your original flags)
LOCAL_CFLAGS := \
    -Wno-error=format-security \
    -fvisibility=hidden \
    -ffunction-sections \
    -fdata-sections \
    -w \
    -fpermissive

LOCAL_CPPFLAGS := \
    -std=c++17 \
    -Wno-error=c++11-narrowing \
    -fms-extensions \
    -frtti \
    -fexceptions

# Linker flags
LOCAL_LDFLAGS := -Wl,--gc-sections,--strip-all
LOCAL_LDLIBS := -llog -landroid -lz


# Source files (keep your exact file structure)
LOCAL_SRC_FILES := \
    bearmod.cpp \
    JNI_Bridge.cpp \
    Tools.cpp \
    md5.cpp \
    KittyMemory/KittyMemory.cpp \
    KittyMemory/MemoryPatch.cpp \
    KittyMemory/MemoryBackup.cpp \
    KittyMemory/KittyUtils.cpp \
    base64/base64.cpp \
    Substrate/hde64.c \
    Substrate/SubstrateDebug.cpp \
    Substrate/SubstrateHook.cpp \
    Substrate/SubstratePosixMemory.cpp \
    Substrate/And64InlineHook.cpp \
    SDK/ARMP_PUBGM_Basic.cpp \
    SDK/ARMP_PUBGM_Basic_functions.cpp \
    SDK/ARMP_PUBGM_CoreUObject_functions.cpp \
    SDK/ARMP_PUBGM_Engine_functions.cpp \
    SDK/ARMP_PUBGM_ShadowTrackerExtra_functions.cpp \
    SDK/ARMP_PUBGM_Client_functions.cpp

# Include paths
LOCAL_C_INCLUDES := \
    $(LOCAL_PATH) \
    $(LOCAL_PATH)/KittyMemory \
    $(LOCAL_PATH)/Substrate \
    $(LOCAL_PATH)/SDK \
    $(LOCAL_PATH)/MD5

# Static libraries
LOCAL_STATIC_LIBRARIES := \
    libssl-prebuilt \
    libcrypto-prebuilt

# Build as shared library
include $(BUILD_SHARED_LIBRARY)