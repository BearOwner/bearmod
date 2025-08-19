#include <jni.h>
#include <android/log.h>
#include <unistd.h>
#include <sys/ptrace.h>
#include <sys/wait.h>
#include <dirent.h>
#include <string.h>
#include <fstream>
#include <thread>
#include <atomic>
#include <vector>
#include <regex>

#define LOG_TAG "BearAntiHook"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)

namespace BearLoader {
    namespace AntiHook {
        
        static std::atomic<bool> gMonitoringActive{false};
        static std::thread gMonitoringThread;
        
        /**
         * Check for Frida server on common ports
         */
        bool detectFridaServer() {
            // Common Frida server ports
            std::vector<int> fridaPorts = {27042, 27043, 27044, 27045};
            
            for (int port : fridaPorts) {
                std::string command = "netstat -an | grep :" + std::to_string(port);
                FILE* pipe = popen(command.c_str(), "r");
                if (pipe) {
                    char buffer[128];
                    if (fgets(buffer, sizeof(buffer), pipe) != nullptr) {
                        pclose(pipe);
                        LOGE("Frida server detected on port %d", port);
                        return true;
                    }
                    pclose(pipe);
                }
            }
            
            // Check for frida-server process
            FILE* pipe = popen("ps | grep frida-server", "r");
            if (pipe) {
                char buffer[256];
                if (fgets(buffer, sizeof(buffer), pipe) != nullptr) {
                    if (strstr(buffer, "frida-server") != nullptr) {
                        pclose(pipe);
                        LOGE("Frida server process detected");
                        return true;
                    }
                }
                pclose(pipe);
            }
            
            return false;
        }
        
        /**
         * Detect debugger attachment
         */
        bool detectDebugger() {
            // Check TracerPid in /proc/self/status
            std::ifstream statusFile("/proc/self/status");
            if (statusFile.is_open()) {
                std::string line;
                while (std::getline(statusFile, line)) {
                    if (line.find("TracerPid:") == 0) {
                        std::string tracerPid = line.substr(10);
                        // Remove whitespace
                        tracerPid.erase(0, tracerPid.find_first_not_of(" \t"));
                        if (tracerPid != "0") {
                            LOGE("Debugger detected - TracerPid: %s", tracerPid.c_str());
                            return true;
                        }
                        break;
                    }
                }
                statusFile.close();
            }
            
            // Try ptrace self-attach
            if (ptrace(PTRACE_TRACEME, 0, 1, 0) == -1) {
                LOGE("Debugger detected - ptrace failed");
                return true;
            }
            
            return false;
        }
        
        /**
         * Detect hooking libraries in memory
         */
        bool detectHookingLibraries() {
            std::vector<std::string> suspiciousLibs = {
                "frida",
                "gum-js-loop",
                "gmain",
                "gdbus",
                "gum",
                "linjector",
                "xposed",
                "substrate",
                "cydia"
            };
            
            std::ifstream mapsFile("/proc/self/maps");
            if (mapsFile.is_open()) {
                std::string line;
                while (std::getline(mapsFile, line)) {
                    for (const auto& lib : suspiciousLibs) {
                        if (line.find(lib) != std::string::npos) {
                            LOGE("Suspicious library detected: %s", lib.c_str());
                            return true;
                        }
                    }
                }
                mapsFile.close();
            }
            
            return false;
        }
        
        /**
         * Detect suspicious threads
         */
        bool detectSuspiciousThreads() {
            DIR* taskDir = opendir("/proc/self/task");
            if (!taskDir) {
                return false;
            }
            
            struct dirent* entry;
            std::vector<std::string> suspiciousNames = {
                "frida",
                "gum",
                "js-loop",
                "gdbus",
                "gmain"
            };
            
            while ((entry = readdir(taskDir)) != nullptr) {
                if (strcmp(entry->d_name, ".") == 0 || strcmp(entry->d_name, "..") == 0) {
                    continue;
                }
                
                std::string commPath = "/proc/self/task/" + std::string(entry->d_name) + "/comm";
                std::ifstream commFile(commPath);
                if (commFile.is_open()) {
                    std::string threadName;
                    std::getline(commFile, threadName);
                    commFile.close();
                    
                    for (const auto& suspicious : suspiciousNames) {
                        if (threadName.find(suspicious) != std::string::npos) {
                            LOGE("Suspicious thread detected: %s", threadName.c_str());
                            closedir(taskDir);
                            return true;
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
            LOGI("Performing anti-hooking security check...");
            
            // Check for Frida server
            if (detectFridaServer()) {
                LOGE("Frida server detected - security compromised");
                return false;
            }
            
            // Check for debugger
            if (detectDebugger()) {
                LOGE("Debugger detected - security compromised");
                return false;
            }
            
            // Check for hooking libraries
            if (detectHookingLibraries()) {
                LOGE("Hooking libraries detected - security compromised");
                return false;
            }
            
            // Check for suspicious threads
            if (detectSuspiciousThreads()) {
                LOGE("Suspicious threads detected - security compromised");
                return false;
            }
            
            LOGI("Anti-hooking check passed");
            return true;
        }
        
        /**
         * Continuous monitoring thread function
         */
        void monitoringThreadFunction() {
            LOGI("Starting continuous anti-hooking monitoring");
            
            while (gMonitoringActive.load()) {
                if (!performAntiHookingCheck()) {
                    LOGE("Security breach detected during monitoring - terminating");
                    // Force exit on detection
                    _exit(1);
                }
                
                // Check every 5 seconds
                std::this_thread::sleep_for(std::chrono::seconds(5));
            }
            
            LOGI("Anti-hooking monitoring stopped");
        }
        
        /**
         * Start continuous monitoring
         */
        void startContinuousMonitoring() {
            if (gMonitoringActive.load()) {
                LOGW("Monitoring already active");
                return;
            }
            
            gMonitoringActive.store(true);
            gMonitoringThread = std::thread(monitoringThreadFunction);
            gMonitoringThread.detach();
            
            LOGI("Continuous anti-hooking monitoring started");
        }
        
        /**
         * Stop continuous monitoring
         */
        void stopContinuousMonitoring() {
            if (!gMonitoringActive.load()) {
                return;
            }
            
            gMonitoringActive.store(false);
            
            if (gMonitoringThread.joinable()) {
                gMonitoringThread.join();
            }
            
            LOGI("Continuous anti-hooking monitoring stopped");
        }
    }
}
