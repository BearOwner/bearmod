LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := libclient_static

LOCAL_CPPFLAGS := -w -std=c++17 -Wno-error=format-security -fvisibility=hidden -ffunction-sections -fdata-sections -fno-rtti -fpermissive
LOCAL_CFLAGS   := -Wno-error=format-security -fvisibility=hidden -ffunction-sections -fdata-sections -fno-rtti -fpermissive
LOCAL_LDFLAGS  := -Wl,--gc-sections,--strip-all

# Debug vs Release
ifeq ($(APP_OPTIM),debug)
    LOCAL_CPPFLAGS += -DDEBUG_BUILD -g -O0
else
    LOCAL_CPPFLAGS += -DPRODUCTION_BUILD -Os -fomit-frame-pointer
endif

LOCAL_SRC_FILES := \
    BYPASS.cpp \
    bearmod_jni.cpp \
    security_verifier.cpp \
    injection.cpp \
    Includes/Utils.cpp \
    KittyMemory/KittyMemory.cpp \
    KittyMemory/MemoryPatch.cpp \
    KittyMemory/MemoryBackup.cpp \
    KittyMemory/KittyUtils.cpp \
    Substrate/hde64.c \
    Substrate/SubstrateDebug.cpp \
    Substrate/SubstrateHook.cpp \
    Substrate/SubstratePosixMemory.cpp \
    Substrate/And64InlineHook.cpp \
    anti_hook/anti_hook.cpp \
    anti_hook/gadget_detector.cpp \
    anti_hook/memory_protector.cpp \
    nonroot/nonroot_injector.cpp \
    nonroot/anti_detection_manager.cpp
    
    # Removed obsolete Frida Gadget components (replaced by ptrace-based injection):
    # nonroot/frida_gadget_manager.cpp
    # nonroot/gadget_obfuscator.cpp
    # nonroot/objection_patcher.cpp

LOCAL_C_INCLUDES := \
    $(LOCAL_PATH) \
    $(LOCAL_PATH)/Includes \
    $(LOCAL_PATH)/Substrate \
    $(LOCAL_PATH)/KittyMemory \
    $(LOCAL_PATH)/anti_hook \
    $(LOCAL_PATH)/patch \
    $(LOCAL_PATH)/nonroot \
    $(LOCAL_PATH)/Helper

# Exception handling required for ptrace injection error handling and JNI integration
LOCAL_CPP_FEATURES := exceptions
include $(BUILD_STATIC_LIBRARY)