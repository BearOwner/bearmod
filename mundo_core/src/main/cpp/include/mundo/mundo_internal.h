#pragma once
#include <jni.h>
#include <atomic>

namespace mundo { namespace core {
void on_load(JavaVM* vm);
bool ready();
void set_ready(bool v);
JavaVM* vm();
} }

