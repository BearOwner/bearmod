#pragma once
#include <atomic>
#include <jni.h>

namespace mundo { namespace core {
void on_load(JavaVM* vm);
bool ready();
void set_ready(bool v);
JavaVM* vm();
} }

