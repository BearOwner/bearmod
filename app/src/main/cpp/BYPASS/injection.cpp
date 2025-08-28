#include <jni.h>
#include <android/log.h>
#include <dlfcn.h>
#include <unistd.h>
#include <sys/mman.h>
#include <sys/ptrace.h>
#include <sys/wait.h>
#include <dirent.h>
#include <string>
#include <vector>
#include <fstream>
#include <thread>
#include <atomic>
#include <errno.h>
#include <sys/uio.h>

// Android ARM ptrace compatibility
#ifdef __aarch64__
    #include <asm/ptrace.h>
    #include <linux/ptrace.h>
    #include <linux/elf.h>
    #include <sys/uio.h>
    #define REGS_TYPE struct user_pt_regs
    #define REG_PC pc
    #define REG_SP sp
    #define REG_ARG0 regs[0]
    #define REG_ARG1 regs[1]
    #define REG_RETURN regs[0]
#elif defined(__arm__)
    #include <asm/ptrace.h>
    #include <linux/ptrace.h>
    #define REGS_TYPE struct pt_regs
    #define REG_PC ARM_pc
    #define REG_SP ARM_sp
    #define REG_ARG0 ARM_r0
    #define REG_ARG1 ARM_r1
    #define REG_RETURN ARM_r0
#else
    // x86_64 fallback (not typically used on Android)
    #include <sys/user.h>
    #define REGS_TYPE struct user_regs_struct
    #define REG_PC rip
    #define REG_SP rsp
    #define REG_ARG0 rdi
    #define REG_ARG1 rsi
    #define REG_RETURN rax
#endif

#define LOG_TAG "BearInjection"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)

// Global state for injection management
static std::atomic<bool> gNativeInjectionActive{false};
static std::atomic<bool> gFridaInjectionActive{false};
static pid_t gTargetPid = -1;
static std::string gTargetPackage;

/**
 * Find process ID by package name
 */
pid_t findProcessByPackage(const char* packageName) {
    DIR* procDir = opendir("/proc");
    if (!procDir) {
        LOGE("Failed to open /proc directory");
        return -1;
    }
    
    struct dirent* entry;
    while ((entry = readdir(procDir)) != nullptr) {
        // Skip non-numeric directories
        if (!isdigit(entry->d_name[0])) {
            continue;
        }
        
        pid_t pid = atoi(entry->d_name);
        if (pid <= 0) {
            continue;
        }
        
        // Read cmdline to get package name
        std::string cmdlinePath = "/proc/" + std::string(entry->d_name) + "/cmdline";
        std::ifstream cmdlineFile(cmdlinePath);
        if (cmdlineFile.is_open()) {
            std::string cmdline;
            std::getline(cmdlineFile, cmdline);
            cmdlineFile.close();
            
            if (cmdline == packageName) {
                closedir(procDir);
                LOGI("Found target process: %s (PID: %d)", packageName, pid);
                return pid;
            }
        }
    }
    
    closedir(procDir);
    LOGE("Target process not found: %s", packageName);
    return -1;
}

/**
 * Verify BearToken before injection
 */
bool verifyBearToken(const char* bearToken) {
    if (!bearToken || strlen(bearToken) == 0) {
        LOGE("Invalid BearToken provided");
        return false;
    }
    
    // Basic token validation - in production, implement proper decryption and verification
    if (strlen(bearToken) < 32) {
        LOGE("BearToken too short");
        return false;
    }
    
    LOGI("BearToken verification passed");
    return true;
}

/**
 * Inject library into target process using ptrace
 */
bool injectLibraryPtrace(pid_t targetPid, const char* libraryPath) {
    LOGI("Attempting ptrace injection into PID: %d", targetPid);
    
    // Attach to target process
    if (ptrace(PTRACE_ATTACH, targetPid, nullptr, nullptr) == -1) {
        LOGE("Failed to attach to target process");
        return false;
    }
    
    // Wait for process to stop
    int status;
    waitpid(targetPid, &status, 0);
    
    if (!WIFSTOPPED(status)) {
        LOGE("Target process did not stop properly");
        ptrace(PTRACE_DETACH, targetPid, nullptr, nullptr);
        return false;
    }
    
    LOGI("Successfully attached to target process");
    
    // Get registers
    REGS_TYPE regs;
#ifdef __aarch64__
    struct iovec iov;
    iov.iov_base = &regs;
    iov.iov_len = sizeof(regs);
    if (ptrace(PTRACE_GETREGSET, targetPid, NT_PRSTATUS, &iov) == -1) {
#else
    if (ptrace(PTRACE_GETREGS, targetPid, nullptr, &regs) == -1) {
#endif
        LOGE("Failed to get registers: %s", strerror(errno));
        ptrace(PTRACE_DETACH, targetPid, nullptr, nullptr);
        return false;
    }
    
    // Find dlopen and dlsym addresses in target process
    // This is a simplified implementation - in production, implement proper symbol resolution
    void* dlopenAddr = dlsym(RTLD_DEFAULT, "dlopen");
    void* dlsymAddr = dlsym(RTLD_DEFAULT, "dlsym");
    
    if (!dlopenAddr || !dlsymAddr) {
        LOGE("Failed to find dlopen/dlsym addresses");
        ptrace(PTRACE_DETACH, targetPid, nullptr, nullptr);
        return false;
    }
    
    LOGI("Found dlopen at: %p, dlsym at: %p", dlopenAddr, dlsymAddr);
    
    // Allocate memory in target process for library path
    size_t pathLen = strlen(libraryPath) + 1;
    // Use stack pointer to allocate memory for the library path
#ifdef __aarch64__
    void* remoteAddr = (void*)(regs.REG_SP - pathLen);
#elif defined(__arm__)
    void* remoteAddr = (void*)(regs.REG_SP - pathLen);
#else
    void* remoteAddr = (void*)(regs.REG_SP - pathLen);
#endif
    
    // Write library path to target process memory
    for (size_t i = 0; i < pathLen; i += sizeof(long)) {
        long data = 0;
        memcpy(&data, libraryPath + i, std::min(sizeof(long), pathLen - i));
        if (ptrace(PTRACE_POKETEXT, targetPid, (void*)((char*)remoteAddr + i), data) == -1) {
            LOGE("Failed to write library path to target memory");
            ptrace(PTRACE_DETACH, targetPid, nullptr, nullptr);
            return false;
        }
    }
    
    // Set up registers for dlopen call
    REGS_TYPE newRegs = regs;
    newRegs.REG_PC = (unsigned long)dlopenAddr;
    newRegs.REG_ARG0 = (unsigned long)remoteAddr;  // library path
    newRegs.REG_ARG1 = RTLD_NOW | RTLD_GLOBAL;     // flags

#ifdef __aarch64__
    struct iovec iov_set;
    iov_set.iov_base = &newRegs;
    iov_set.iov_len = sizeof(newRegs);
    if (ptrace(PTRACE_SETREGSET, targetPid, NT_PRSTATUS, &iov_set) == -1) {
        LOGE("Failed to set registers for dlopen call: %s", strerror(errno));
        ptrace(PTRACE_DETACH, targetPid, nullptr, nullptr);
        return false;
    }
#else
    if (ptrace(PTRACE_SETREGS, targetPid, nullptr, &newRegs) == -1) {
        LOGE("Failed to set registers for dlopen call: %s", strerror(errno));
        ptrace(PTRACE_DETACH, targetPid, nullptr, nullptr);
        return false;
    }
#endif
    
    // Execute dlopen
    if (ptrace(PTRACE_CONT, targetPid, nullptr, nullptr) == -1) {
        LOGE("Failed to continue target process");
        ptrace(PTRACE_DETACH, targetPid, nullptr, nullptr);
        return false;
    }
    
    // Wait for dlopen to complete
    waitpid(targetPid, &status, 0);
    
    // Get return value (library handle)
#ifdef __aarch64__
    struct iovec iov_get;
    iov_get.iov_base = &newRegs;
    iov_get.iov_len = sizeof(newRegs);
    if (ptrace(PTRACE_GETREGSET, targetPid, NT_PRSTATUS, &iov_get) == -1) {
        LOGE("Failed to get registers after dlopen: %s", strerror(errno));
        ptrace(PTRACE_DETACH, targetPid, nullptr, nullptr);
        return false;
    }
#else
    if (ptrace(PTRACE_GETREGS, targetPid, nullptr, &newRegs) == -1) {
        LOGE("Failed to get registers after dlopen: %s", strerror(errno));
        ptrace(PTRACE_DETACH, targetPid, nullptr, nullptr);
        return false;
    }
#endif

    void* libraryHandle = (void*)newRegs.REG_RETURN;
    if (!libraryHandle) {
        LOGE("dlopen failed - library handle is null");
        ptrace(PTRACE_DETACH, targetPid, nullptr, nullptr);
        return false;
    }

    LOGI("Library injected successfully, handle: %p", libraryHandle);

    // Restore original registers
#ifdef __aarch64__
    struct iovec iov_restore;
    iov_restore.iov_base = &regs;
    iov_restore.iov_len = sizeof(regs);
    if (ptrace(PTRACE_SETREGSET, targetPid, NT_PRSTATUS, &iov_restore) == -1) {
        LOGW("Failed to restore original registers: %s", strerror(errno));
    }
#else
    if (ptrace(PTRACE_SETREGS, targetPid, nullptr, &regs) == -1) {
        LOGW("Failed to restore original registers: %s", strerror(errno));
    }
#endif

    // Detach from target process
    if (ptrace(PTRACE_DETACH, targetPid, nullptr, nullptr) == -1) {
        LOGW("Failed to detach from target process");
    }

    LOGI("Ptrace injection completed successfully");
    return true;
}

/**
 * Perform native injection
 */
bool performNativeInjection(const char* packageName, const char* bearToken) {
    LOGI("Starting native injection for package: %s", packageName);
    
    // Verify BearToken
    if (!verifyBearToken(bearToken)) {
        LOGE("BearToken verification failed");
        return false;
    }
    
    // Find target process
    pid_t targetPid = findProcessByPackage(packageName);
    if (targetPid == -1) {
        LOGE("Target process not found");
        return false;
    }
    
    // Store global state
    gTargetPid = targetPid;
    gTargetPackage = packageName;
    
    // Get library path - try multiple locations
    std::vector<std::string> libraryPaths = {
        "/data/data/com.bearmod.loader/lib/arm64-v8a/libbearmod.so",
        "/data/data/com.bearmod.loader/lib/libbearmod.so",
        "/data/app/com.bearmod.loader/lib/arm64-v8a/libbearmod.so",
        "/system/lib64/libbearmod.so"
    };

    std::string libraryPath;
    bool libraryFound = false;

    for (const auto& path : libraryPaths) {
        if (access(path.c_str(), F_OK) == 0) {
            libraryPath = path;
            libraryFound = true;
            LOGI("Found library at: %s", libraryPath.c_str());
            break;
        }
    }

    if (!libraryFound) {
        LOGE("Library not found in any expected location");
        // Try to find the library using alternative method
        libraryPath = "/data/data/com.bearmod.loader/lib/arm64-v8a/libbearmod.so";
        LOGW("Using fallback library path: %s", libraryPath.c_str());
    }
    
    // Perform injection
    bool result = injectLibraryPtrace(targetPid, libraryPath.c_str());
    
    if (result) {
        gNativeInjectionActive.store(true);
        LOGI("Native injection completed successfully");
    } else {
        LOGE("Native injection failed");
    }
    
    return result;
}

/**
 * Perform Frida injection (development mode)
 */
bool performFridaInjection(const char* packageName, const char* script) {
    LOGI("Starting Frida injection for package: %s", packageName);
    
    // Find target process
    pid_t targetPid = findProcessByPackage(packageName);
    if (targetPid == -1) {
        LOGE("Target process not found for Frida injection");
        return false;
    }
    
    // Store global state
    gTargetPid = targetPid;
    gTargetPackage = packageName;
    
    // Frida injection is not fully implemented yet
    LOGW("Frida injection is not fully implemented - falling back to direct injection");

    // For now, fall back to direct injection method
    LOGI("Attempting direct injection as Frida fallback for PID: %d", targetPid);

    // Use the same library injection method as direct injection
    std::vector<std::string> libraryPaths = {
        "/data/data/com.bearmod.loader/lib/arm64-v8a/libbearmod.so",
        "/data/data/com.bearmod.loader/lib/libbearmod.so",
        "/data/app/com.bearmod.loader/lib/arm64-v8a/libbearmod.so"
    };

    std::string libraryPath;
    bool libraryFound = false;

    for (const auto& path : libraryPaths) {
        if (access(path.c_str(), F_OK) == 0) {
            libraryPath = path;
            libraryFound = true;
            LOGI("Found library for Frida fallback at: %s", libraryPath.c_str());
            break;
        }
    }

    if (!libraryFound) {
        LOGE("No library found for Frida fallback injection");
        return false;
    }

    // Perform library injection using ptrace (same as direct injection)
    bool result = injectLibraryPtrace(targetPid, libraryPath.c_str());

    if (result) {
        gFridaInjectionActive.store(true);
        LOGI("Frida fallback injection completed successfully");
    } else {
        LOGE("Frida fallback injection failed");
    }

    return result;
}

/**
 * Stop Frida injection
 */
void stopFridaInjection() {
    if (!gFridaInjectionActive.load()) {
        LOGW("Frida injection not active");
        return;
    }
    
    LOGI("Stopping Frida injection");
    
    // Kill frida processes
    system("pkill -f frida");
    
    // Clean up temporary files
    unlink("/data/local/tmp/frida_script.js");
    
    gFridaInjectionActive.store(false);
    gTargetPid = -1;
    gTargetPackage.clear();
    
    LOGI("Frida injection stopped");
}

/**
 * Stop native injection
 */
void stopNativeInjection() {
    if (!gNativeInjectionActive.load()) {
        LOGW("Native injection not active");
        return;
    }
    
    LOGI("Stopping native injection");
    
    // In production, implement proper cleanup
    // This might involve sending signals to injected code or cleaning up hooks
    
    gNativeInjectionActive.store(false);
    gTargetPid = -1;
    gTargetPackage.clear();
    
    LOGI("Native injection stopped");
}
