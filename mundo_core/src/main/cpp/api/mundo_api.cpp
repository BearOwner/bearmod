#include "../../main/cpp/include/mundo/mundo_api.h"
#include "../core/mundo_core.h"

namespace mundo {
namespace api {

bool is_ready() {
    return core::ready();
}

void set_ready(bool v) {
    core::set_ready(v);
}

} // namespace api
} // namespace mundo

