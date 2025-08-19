

#include "Helper/Includes.h"
#include "NRG.h"
#include "JNI_Bridge.h"


bool TestGGG = false;
bool IsHideEsp(JNIEnv * env, jclass clazz)
{
    if (TestGGG == true){
        return true;
    }
    return false;
}

void native_onSendConfig(JNIEnv *env, jobject thiz, jstring s, jstring v) {
    const char *config = env->GetStringUTFChars(s, 0);
    const char *value = env->GetStringUTFChars(v, 0);
    HandleOnSendConfig(config, value);
    env->ReleaseStringUTFChars(s, config);
    env->ReleaseStringUTFChars(v, value);
}

// Note: JNI implementations are now centralized in JNI_Bridge.cpp
// This file only contains the registration logic and legacy functions

// Implementation moved to NRG.h to keep Main.cpp as a pure JNI gateway file.


// JNI exports moved to JNI_Bridge.cpp to avoid duplicates

// ========================================
// OBSOLETE JNI METHODS REMOVED
// All authentication now handled through KeyAuth API in Java layer
// C++ global variables (bValid, g_Token, g_Auth) are updated via existing JNI bridge
// ========================================

jclass (*orig_FindClass)(JNIEnv *env, const char *name);
// Legacy registration functions - now handled by JNI_Bridge.cpp
int Register1(JNIEnv *env) {
    // Legacy registration - now handled by JNI_Bridge.cpp
    return 0;
}

int Register2(JNIEnv *env) {
    // Legacy registration - now handled by JNI_Bridge.cpp
    return 0;
}

int Register3(JNIEnv *env) {
    // Legacy registration - now handled by JNI_Bridge.cpp
    return 0;
}


#include <iostream>
#include <fstream>
#include <string>
#include <sstream>
#include <vector>
#include <unistd.h>
#include <sys/mman.h>
#include <fcntl.h>
#include <cstring>
uintptr_t GetLibraryBaseAddress(const std::string& lib_name) {
    std::ifstream maps_file("/proc/self/maps");
    std::string line;
    uintptr_t base_address = 0;
    while (std::getline(maps_file, line)) {
        if (line.find(lib_name) != std::string::npos) {
            std::istringstream iss(line);
            std::string address_range;
            if (iss >> address_range) {
                std::string base_str = address_range.substr(0, address_range.find('-'));
                std::stringstream ss;
                ss << std::hex << base_str;
                ss >> base_address;
                if (ss.fail()) return 0;
                break;
            }
        }
    }
    return base_address;
}
uintptr_t xelahot_pointer(const std::vector<std::string>& lib_info, const std::vector<uintptr_t>& offsets, bool some_flag) {
    std::string lib_name = lib_info[0];
    uintptr_t base_address = GetLibraryBaseAddress(lib_name);
    if (base_address == 0) return 0;
    for (const auto& offset : offsets) {
        base_address += offset;
    }
    return base_address;
}
void setMemoryValue(uintptr_t address, int value) {
    int mem_fd = open("/proc/self/mem", O_RDWR);
    if (mem_fd == -1) return;
    pwrite(mem_fd, &value, sizeof(value), address);
    close(mem_fd);
}
void setValues(uintptr_t address, int flags, int value, bool freeze) {
    setMemoryValue(address, value);
}



#define targetLibName OBFUSCATE("libanogs.so")
void *IsAntiCheat(void *) {
    do {
        sleep(5);
    } while (!isLibraryLoaded(targetLibName));


    return nullptr;
}


__attribute((constructor)) void _bypass() {
    pthread_t t;
    pthread_create(&t, nullptr, IsAntiCheat, 0);
    // Additional threads, if any, should be started from Java-side init after app is ready
}


