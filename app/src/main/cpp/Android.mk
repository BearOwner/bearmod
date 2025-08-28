LOCAL_PATH := $(call my-dir)
MAIN_LOCAL_PATH := $(call my-dir)

# ⬅️ Include libclient_static first
include $(LOCAL_PATH)/BYPASS/Bypass.mk

# ⬅️ Restore LOCAL_PATH after BYPASS include
LOCAL_PATH := $(MAIN_LOCAL_PATH)

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

#libdobby
include $(CLEAR_VARS)
LOCAL_MODULE := libdobby-prebuilt
LOCAL_SRC_FILES := BYPASS/Helper/Dobby/libraries/$(TARGET_ARCH_ABI)/libdobby.a
 LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/curl/openssl-android-arm64-v8a/include
include $(PREBUILT_STATIC_LIBRARY)

# ==================== Main Library ====================

# Main BearMod Shared Lib
include $(CLEAR_VARS)
LOCAL_MODULE := bearmod

LOCAL_CPPFLAGS := -w -std=c++17 -Wno-error=format-security -fvisibility=hidden -ffunction-sections -fdata-sections -fno-rtti -fpermissive
LOCAL_CFLAGS   := -Wno-error=format-security -fvisibility=hidden -ffunction-sections -fdata-sections -fno-rtti -fpermissive
LOCAL_LDFLAGS  := -Wl,--gc-sections,--strip-all

# Debug vs Release
ifeq ($(APP_OPTIM),debug)
    LOCAL_CPPFLAGS += -DDEBUG_BUILD -g -O0
else
    LOCAL_CPPFLAGS += -DPRODUCTION_BUILD -Os -fomit-frame-pointer
endif

# Only include BearMod-specific sources
LOCAL_SRC_FILES := \
    bearmod.cpp \
    JNI_Bridge.cpp \
    Tools.cpp \
    md5.cpp \
    Time/Time.cpp \
    base64/base64.cpp \
    SDK/ARMP_PUBGM_Basic.cpp \
    SDK/ARMP_PUBGM_Basic_functions.cpp \
    SDK/ARMP_PUBGM_CoreUObject_functions.cpp \
    SDK/ARMP_PUBGM_Engine_functions.cpp \
    SDK/ARMP_PUBGM_ShadowTrackerExtra_functions.cpp \
    SDK/ARMP_PUBGM_Client_functions.cpp

# Include paths
LOCAL_C_INCLUDES := \
    $(LOCAL_PATH) \
    $(LOCAL_PATH)/SDK \
    $(LOCAL_PATH)/MD5 \
    $(LOCAL_PATH)/curl/openssl-android-arm64-v8a/include \
    $(LOCAL_PATH)/BYPASS \
    $(LOCAL_PATH)/BYPASS/Helper \
    $(LOCAL_PATH)/BYPASS/Substrate

# Static libraries
LOCAL_STATIC_LIBRARIES := \
    libssl-prebuilt \
    libcrypto-prebuilt \
    libdobby-prebuilt

# Build as shared library
include $(BUILD_SHARED_LIBRARY)