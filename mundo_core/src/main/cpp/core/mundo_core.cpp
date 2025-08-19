#include "../core/mundo_core.h"

namespace mundo {
namespace core {

static std::atomic<bool> g_ready{false};
static JavaVM* g_vm = nullptr;

void on_load(JavaVM* vm) {
    g_vm = vm;
}

bool ready() {
    return g_ready.load(std::memory_order_acquire);
}

void set_ready(bool v) {
    g_ready.store(v, std::memory_order_release);
}

JavaVM* vm() { return g_vm; }

} // namespace core
} // namespace mundo

