#include <jni.h>
#include <string>
#include <vector>
#include <unistd.h>
#include <sys/ptrace.h>
#include <sys/wait.h>
#include <dirent.h>
#include <fstream>
#include <android/log.h>
#include <pthread.h>

#define LOG_TAG "AntiFrida"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace BearLoader {
    namespace AntiHook {
        
        // Frida detection signatures
        static const std::vector<std::string> FRIDA_SIGNATURES = {
            "frida-server",
            "frida-agent",
            "frida-gadget",
            "frida_agent_main",
            "gum-js-loop",
            "gmain",
            "gdbus",
            "pool-frida"
        };
        
        // Suspicious library names
        static const std::vector<std::string> HOOK_LIBRARIES = {
            "libfrida",
            "libgum",
            "libxposed",
            "libsubstrate",
            "libdobby"
        };
        
        // Suspicious process names
        static const std::vector<std::string> SUSPICIOUS_PROCESSES = {
            "frida-server",
            "frida-portal",
            "re.frida.server",
            "xposed",
            "substrate"
        };
        
        /**
         * Check for Frida server running
         */
        bool detectFridaServer() {
            DIR* dir = opendir("/proc");
            if (!dir) return false;
            
            struct dirent* entry;
            while ((entry = readdir(dir)) != nullptr) {
                if (entry->d_type == DT_DIR) {
                    std::string pid = entry->d_name;
                    if (pid == "." || pid == "..") continue;
                    
                    // Check cmdline for suspicious processes
                    std::string cmdlinePath = "/proc/" + pid + "/cmdline";
                    std::ifstream cmdlineFile(cmdlinePath);
                    if (cmdlineFile.is_open()) {
                        std::string cmdline;
                        std::getline(cmdlineFile, cmdline);
                        cmdlineFile.close();
                        
                        for (const auto& suspiciousProcess : SUSPICIOUS_PROCESSES) {
                            if (cmdline.find(suspiciousProcess) != std::string::npos) {
                                LOGE("Detected suspicious process: %s", cmdline.c_str());
                                closedir(dir);
                                return true;
                            }
                        }
                    }
                }
            }
            closedir(dir);
            return false;
        }
        
        /**
         * Check for Frida libraries in memory
         */
        bool detectFridaLibraries() {
            std::ifstream mapsFile("/proc/self/maps");
            if (!mapsFile.is_open()) return false;
            
            std::string line;
            while (std::getline(mapsFile, line)) {
                for (const auto& hookLib : HOOK_LIBRARIES) {
                    if (line.find(hookLib) != std::string::npos) {
                        LOGE("Detected hook library: %s", line.c_str());
                        mapsFile.close();
                        return true;
                    }
                }
                
                for (const auto& fridaSig : FRIDA_SIGNATURES) {
                    if (line.find(fridaSig) != std::string::npos) {
                        LOGE("Detected Frida signature: %s", line.c_str());
                        mapsFile.close();
                        return true;
                    }
                }
            }
            mapsFile.close();
            return false;
        }
        
        /**
         * Check for debugger attachment
         */
        bool detectDebugger() {
            // Check TracerPid in status file
            std::ifstream statusFile("/proc/self/status");
            if (!statusFile.is_open()) return false;
            
            std::string line;
            while (std::getline(statusFile, line)) {
                if (line.find("TracerPid:") == 0) {
                    std::string tracerPid = line.substr(10);
                    // Remove whitespace
                    tracerPid.erase(0, tracerPid.find_first_not_of(" \t"));
                    
                    if (tracerPid != "0") {
                        LOGE("Debugger detected - TracerPid: %s", tracerPid.c_str());
                        statusFile.close();
                        return true;
                    }
                    break;
                }
            }
            statusFile.close();
            
            // Try ptrace self-attach
            if (ptrace(PTRACE_TRACEME, 0, 1, 0) == -1) {
                LOGE("Debugger detected - ptrace failed");
                return true;
            }
            
            return false;
        }
        
        /**
         * Check for Frida ports
         */
        bool detectFridaPorts() {
            // Common Frida ports: 27042, 27043
            std::vector<int> fridaPorts = {27042, 27043};
            
            for (int port : fridaPorts) {
                std::string netstatCmd = "netstat -an | grep :" + std::to_string(port);
                FILE* pipe = popen(netstatCmd.c_str(), "r");
                if (pipe) {
                    char buffer[128];
                    if (fgets(buffer, sizeof(buffer), pipe) != nullptr) {
                        LOGE("Detected Frida port: %d", port);
                        pclose(pipe);
                        return true;
                    }
                    pclose(pipe);
                }
            }
            return false;
        }
        
        /**
         * Check for suspicious threads
         */
        bool detectSuspiciousThreads() {
            DIR* taskDir = opendir("/proc/self/task");
            if (!taskDir) return false;
            
            struct dirent* entry;
            while ((entry = readdir(taskDir)) != nullptr) {
                if (entry->d_type == DT_DIR) {
                    std::string tid = entry->d_name;
                    if (tid == "." || tid == "..") continue;
                    
                    std::string commPath = "/proc/self/task/" + tid + "/comm";
                    std::ifstream commFile(commPath);
                    if (commFile.is_open()) {
                        std::string threadName;
                        std::getline(commFile, threadName);
                        commFile.close();
                        
                        for (const auto& fridaSig : FRIDA_SIGNATURES) {
                            if (threadName.find(fridaSig) != std::string::npos) {
                                LOGE("Detected suspicious thread: %s", threadName.c_str());
                                closedir(taskDir);
                                return true;
                            }
                        }
                    }
                }
            }
            closedir(taskDir);
            return false;
        }
        
        /**
         * Comprehensive anti-hooking check
         */
        bool performAntiHookingCheck() {
            LOGI("Performing comprehensive anti-hooking check");
            
            // Check for Frida server
            if (detectFridaServer()) {
                LOGE("Frida server detected");
                return false;
            }
            
            // Check for Frida libraries
            if (detectFridaLibraries()) {
                LOGE("Frida libraries detected");
                return false;
            }
            
            // Check for debugger
            if (detectDebugger()) {
                LOGE("Debugger detected");
                return false;
            }
            
            // Check for Frida ports
            if (detectFridaPorts()) {
                LOGE("Frida ports detected");
                return false;
            }
            
            // Check for suspicious threads
            if (detectSuspiciousThreads()) {
                LOGE("Suspicious threads detected");
                return false;
            }
            
            LOGI("Anti-hooking check passed");
            return true;
        }
        
        /**
         * Continuous monitoring thread
         */
        void* monitoringThread(void* arg) {
            while (true) {
                if (!performAntiHookingCheck()) {
                    LOGE("Hooking detected - terminating application");
                    exit(1);
                }
                
                // Check every 5 seconds
                sleep(5);
            }
            return nullptr;
        }
        
        /**
         * Start continuous monitoring
         */
        void startContinuousMonitoring() {
            pthread_t monitorThread;
            pthread_create(&monitorThread, nullptr, monitoringThread, nullptr);
            pthread_detach(monitorThread);
        }
    }
}

// JNI exports
extern "C" JNIEXPORT jboolean JNICALL
Java_com_bearmod_loader_security_NativeSecurityManager_nativeDetectFrida(
        JNIEnv* env, jobject thiz) {
    return !BearLoader::AntiHook::performAntiHookingCheck();
}

extern "C" JNIEXPORT void JNICALL
Java_com_bearmod_loader_security_NativeSecurityManager_nativeStartAntiHookMonitoring(
        JNIEnv* env, jobject thiz) {
    BearLoader::AntiHook::startContinuousMonitoring();
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_bearmod_loader_security_NativeSecurityManager_nativeVerifyIntegrity(
        JNIEnv* env, jobject thiz, jobject context) {
    
    // Perform anti-hooking check first
    if (!BearLoader::AntiHook::performAntiHookingCheck()) {
        LOGE("Integrity check failed - hooking detected");
        return JNI_FALSE;
    }
    
    // Additional integrity checks can be added here
    // - Code section checksums
    // - Critical function verification
    // - Memory protection checks
    
    return JNI_TRUE;
}
