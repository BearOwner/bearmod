#include <jni.h>
#include "../core/mundo_core.h"

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void*) {
    mundo::core::on_load(vm);
    return JNI_VERSION_1_6;
}

