#include "anti_detection_manager.h"
#include <android/log.h>
#include <unistd.h>
#include <dirent.h>
#include <fstream>
#include <sstream>
#include <algorithm>
#include <cstdlib>
#include <sys/stat.h>
#include <dlfcn.h>
#include <pthread.h>

#define LOG_TAG "AntiDetectionManager"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace BearLoader::NonRoot {
    
    // Static instance management
    std::unique_ptr<AntiDetectionManager> AntiDetectionManagerInstance::s_instance = nullptr;
    std::mutex AntiDetectionManagerInstance::s_instanceMutex;
    
    // Known Frida signatures and patterns
    static const std::vector<std::string> FRIDA_LIBRARIES = {
        "libfrida-gadget.so",
        "libfrida-agent.so", 
        "libfrida-gum.so",
        "libfrida.so",
        "libgum.so",
        "libhelper.so", // Common obfuscated name
        "libgadget.so"
    };
    
    static const std::vector<std::string> FRIDA_PROCESSES = {
        "frida-server",
        "frida-portal", 
        "re.frida.server",
        "frida-agent",
        "gum-js-loop",
        "gmain",
        "gdbus"
    };
    
    static const std::vector<int> FRIDA_PORTS = {
        27042, 27043, 27044, 27045, 27046, 27047
    };
    
    static const std::vector<std::string> HOOK_LIBRARIES = {
        "libxposed",
        "libsubstrate", 
        "libdobby",
        "libwhale",
        "libsandhook"
    };
    
    static const std::vector<std::string> SUSPICIOUS_PROCESSES = {
        "xposed",
        "substrate",
        "magisk",
        "supersu",
        "chainfire"
    };
    
    AntiDetectionManager::AntiDetectionManager() {
        LOGI("AntiDetectionManager constructor");
        initializeKnownSignatures();
    }
    
    AntiDetectionManager::~AntiDetectionManager() {
        LOGI("AntiDetectionManager destructor");
        shutdown();
    }
    
    bool AntiDetectionManager::initialize(JNIEnv* env, jobject context, const AntiDetectionConfig& config) {
        std::lock_guard<std::mutex> lock(m_configMutex);
        
        if (m_initialized.load()) {
            LOGW("AntiDetectionManager already initialized");
            return true;
        }
        
        LOGI("Initializing AntiDetectionManager");
        
        // Store configuration
        m_config = config;
        
        // Initialize JNI environment
        initializeJNIEnvironment(env, context);
        
        // Initialize known signatures
        initializeKnownSignatures();
        
        // Enable evasion methods if configured
        if (m_config.enableAntiAnalysisEvasion) {
            enableAntiAnalysisEvasion();
        }
        
        if (m_config.enableStealthMode) {
            enableStealthMode();
        }
        
        if (m_config.enableEnvironmentSpoofing) {
            enableEnvironmentSpoofing();
        }
        
        m_initialized.store(true);
        LOGI("AntiDetectionManager initialized successfully");
        
        return true;
    }
    
    void AntiDetectionManager::shutdown() {
        LOGI("Shutting down AntiDetectionManager");
        
        // Stop monitoring
        stopMonitoring();
        
        // Clear callbacks
        {
            std::lock_guard<std::mutex> lock(m_callbackMutex);
            m_callbacks.clear();
        }
        
        // Clear detection history
        {
            std::lock_guard<std::mutex> lock(m_historyMutex);
            m_detectionHistory.clear();
        }
        
        // Clean up JNI references
        if (m_context && m_jvm) {
            JNIEnv* env = nullptr;
            if (m_jvm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) == JNI_OK) {
                env->DeleteGlobalRef(m_context);
                m_context = nullptr;
            }
        }
        
        m_initialized.store(false);
        LOGI("AntiDetectionManager shutdown complete");
    }
    
    bool AntiDetectionManager::startMonitoring() {
        if (!m_initialized.load()) {
            LOGE("AntiDetectionManager not initialized");
            return false;
        }
        
        if (m_monitoringActive.load()) {
            LOGW("Monitoring already active");
            return true;
        }
        
        if (!m_config.enableContinuousMonitoring) {
            LOGI("Continuous monitoring disabled in config");
            return false;
        }
        
        LOGI("Starting anti-detection monitoring");
        
        m_shouldStopMonitoring.store(false);
        m_monitoringActive.store(true);
        
        // Start monitoring thread
        m_monitoringThread = std::make_unique<std::thread>(&AntiDetectionManager::monitoringLoop, this);
        
        LOGI("Anti-detection monitoring started");
        return true;
    }
    
    void AntiDetectionManager::stopMonitoring() {
        if (!m_monitoringActive.load()) {
            return;
        }
        
        LOGI("Stopping anti-detection monitoring");
        
        m_shouldStopMonitoring.store(true);
        m_monitoringActive.store(false);
        
        // Wait for monitoring thread to finish
        if (m_monitoringThread && m_monitoringThread->joinable()) {
            m_monitoringThread->join();
            m_monitoringThread.reset();
        }
        
        LOGI("Anti-detection monitoring stopped");
    }
    
    void AntiDetectionManager::monitoringLoop() {
        LOGI("Anti-detection monitoring loop started");
        
        while (!m_shouldStopMonitoring.load()) {
            try {
                // Perform detection scan
                if (!performSingleScan()) {
                    LOGW("Detection scan failed");
                }
                
                // Sleep for configured interval
                std::this_thread::sleep_for(std::chrono::milliseconds(m_config.scanIntervalMs));
                
            } catch (const std::exception& e) {
                LOGE("Exception in monitoring loop: %s", e.what());
            } catch (...) {
                LOGE("Unknown exception in monitoring loop");
            }
        }
        
        LOGI("Anti-detection monitoring loop ended");
    }
    
    bool AntiDetectionManager::performSingleScan() {
        bool anyDetected = false;
        
        try {
            // Perform enabled detection methods
            if (m_config.enableFridaServerDetection && detectFridaServer()) {
                anyDetected = true;
            }
            
            if (m_config.enableFridaLibraryDetection && detectFridaLibraries()) {
                anyDetected = true;
            }
            
            if (m_config.enableFridaProcessDetection && detectFridaProcesses()) {
                anyDetected = true;
            }
            
            if (m_config.enableFridaThreadDetection && detectFridaThreads()) {
                anyDetected = true;
            }
            
            if (m_config.enableFridaPortDetection && detectFridaPorts()) {
                anyDetected = true;
            }
            
            if (m_config.enableDebuggerDetection && detectDebugger()) {
                anyDetected = true;
            }
            
            if (m_config.enableEmulatorDetection && detectEmulator()) {
                anyDetected = true;
            }
            
            if (m_config.enableHookLibraryDetection && detectHookLibraries()) {
                anyDetected = true;
            }
            
            if (m_config.enableSuspiciousProcessDetection && detectSuspiciousProcesses()) {
                anyDetected = true;
            }
            
            if (m_config.enableMemoryTamperingDetection && detectMemoryTampering()) {
                anyDetected = true;
            }
            
            if (m_config.enableEnvironmentAnalysisDetection && detectEnvironmentAnalysis()) {
                anyDetected = true;
            }
            
            if (m_config.enableRootAccessDetection && detectRootAccess()) {
                anyDetected = true;
            }
            
            if (m_config.enableXposedFrameworkDetection && detectXposedFramework()) {
                anyDetected = true;
            }
            
        } catch (const std::exception& e) {
            LOGE("Exception during detection scan: %s", e.what());
            return false;
        }
        
        return !anyDetected; // Return true if no threats detected
    }
    
    bool AntiDetectionManager::detectFridaServer() {
        try {
            auto openPorts = getOpenPorts();
            
            for (int port : FRIDA_PORTS) {
                if (std::find(openPorts.begin(), openPorts.end(), port) != openPorts.end()) {
                    reportDetection(DetectionType::FRIDA_SERVER, 
                                  "Frida server detected on port " + std::to_string(port), 
                                  9, true);
                    return true;
                }
            }
            
            return false;
        } catch (...) {
            return false;
        }
    }
    
    bool AntiDetectionManager::detectFridaLibraries() {
        try {
            auto loadedLibraries = getLoadedLibraries();
            
            for (const auto& fridaLib : FRIDA_LIBRARIES) {
                for (const auto& loadedLib : loadedLibraries) {
                    if (loadedLib.find(fridaLib) != std::string::npos) {
                        reportDetection(DetectionType::FRIDA_LIBRARY,
                                      "Frida library detected: " + loadedLib,
                                      8, true);
                        return true;
                    }
                }
            }
            
            return false;
        } catch (...) {
            return false;
        }
    }
    
    bool AntiDetectionManager::detectFridaProcesses() {
        try {
            auto runningProcesses = getRunningProcesses();
            
            for (const auto& fridaProcess : FRIDA_PROCESSES) {
                for (const auto& process : runningProcesses) {
                    if (process.find(fridaProcess) != std::string::npos) {
                        reportDetection(DetectionType::FRIDA_PROCESS,
                                      "Frida process detected: " + process,
                                      9, true);
                        return true;
                    }
                }
            }
            
            return false;
        } catch (...) {
            return false;
        }
    }
    
    void AntiDetectionManager::initializeKnownSignatures() {
        // Initialize known Frida libraries
        m_knownFridaLibraries.insert(FRIDA_LIBRARIES.begin(), FRIDA_LIBRARIES.end());
        
        // Initialize known Frida processes
        m_knownFridaProcesses.insert(FRIDA_PROCESSES.begin(), FRIDA_PROCESSES.end());
        
        // Initialize known Frida ports
        m_knownFridaPorts.insert(FRIDA_PORTS.begin(), FRIDA_PORTS.end());
        
        // Initialize known hook libraries
        m_knownHookLibraries.insert(HOOK_LIBRARIES.begin(), HOOK_LIBRARIES.end());
        
        // Initialize known suspicious processes
        m_knownSuspiciousProcesses.insert(SUSPICIOUS_PROCESSES.begin(), SUSPICIOUS_PROCESSES.end());
        
        LOGI("Initialized known signatures: %zu Frida libs, %zu processes, %zu ports",
             m_knownFridaLibraries.size(), m_knownFridaProcesses.size(), m_knownFridaPorts.size());
    }
    
    void AntiDetectionManager::initializeJNIEnvironment(JNIEnv* env, jobject context) {
        if (env && context) {
            env->GetJavaVM(&m_jvm);
            m_context = env->NewGlobalRef(context);
            LOGI("JNI environment initialized");
        }
    }

    bool AntiDetectionManager::detectFridaThreads() {
        try {
            std::ifstream statusFile("/proc/self/status");
            std::string line;

            while (std::getline(statusFile, line)) {
                if (line.find("Threads:") == 0) {
                    int threadCount = std::stoi(line.substr(8));
                    // Frida typically creates additional threads
                    if (threadCount > 15) {  // Threshold for suspicious thread count
                        reportDetection(DetectionType::FRIDA_THREAD,
                                      "Suspicious thread count: " + std::to_string(threadCount),
                                      6, false);
                        return true;
                    }
                    break;
                }
            }

            return false;
        } catch (...) {
            return false;
        }
    }

    bool AntiDetectionManager::detectFridaPorts() {
        try {
            auto openPorts = getOpenPorts();

            for (int port : m_knownFridaPorts) {
                if (std::find(openPorts.begin(), openPorts.end(), port) != openPorts.end()) {
                    reportDetection(DetectionType::FRIDA_PORT,
                                  "Frida port detected: " + std::to_string(port),
                                  8, true);
                    return true;
                }
            }

            return false;
        } catch (...) {
            return false;
        }
    }

    bool AntiDetectionManager::detectDebugger() {
        try {
            // Check for debugger via JNI
            if (m_jvm && m_context) {
                JNIEnv* env = nullptr;
                if (m_jvm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) == JNI_OK) {
                    jclass debugClass = env->FindClass("android/os/Debug");
                    if (debugClass) {
                        jmethodID isDebuggerConnectedMethod = env->GetStaticMethodID(
                            debugClass, "isDebuggerConnected", "()Z");
                        if (isDebuggerConnectedMethod) {
                            jboolean isDebugging = env->CallStaticBooleanMethod(debugClass, isDebuggerConnectedMethod);
                            if (isDebugging) {
                                reportDetection(DetectionType::DEBUGGER,
                                              "Debugger connected via Android Debug class",
                                              9, true);
                                return true;
                            }
                        }
                        env->DeleteLocalRef(debugClass);
                    }
                }
            }

            // Check TracerPid in /proc/self/status
            std::ifstream statusFile("/proc/self/status");
            std::string line;
            while (std::getline(statusFile, line)) {
                if (line.find("TracerPid:") == 0) {
                    std::string tracerPid = line.substr(10);
                    tracerPid.erase(0, tracerPid.find_first_not_of(" \t"));
                    if (tracerPid != "0") {
                        reportDetection(DetectionType::DEBUGGER,
                                      "Tracer detected with PID: " + tracerPid,
                                      9, true);
                        return true;
                    }
                    break;
                }
            }

            return false;
        } catch (...) {
            return false;
        }
    }

    bool AntiDetectionManager::detectEmulator() {
        try {
            // Check for common emulator indicators
            const char* emulatorFiles[] = {
                "/system/lib/libc_malloc_debug_qemu.so",
                "/sys/qemu_trace",
                "/system/bin/qemu-props",
                "/dev/socket/qemud",
                "/dev/qemu_pipe"
            };

            for (const char* file : emulatorFiles) {
                if (access(file, F_OK) == 0) {
                    reportDetection(DetectionType::EMULATOR,
                                  "Emulator file detected: " + std::string(file),
                                  5, false);
                    return true;
                }
            }

            return false;
        } catch (...) {
            return false;
        }
    }

    bool AntiDetectionManager::detectHookLibraries() {
        try {
            auto loadedLibraries = getLoadedLibraries();

            for (const auto& hookLib : m_knownHookLibraries) {
                for (const auto& loadedLib : loadedLibraries) {
                    if (loadedLib.find(hookLib) != std::string::npos) {
                        reportDetection(DetectionType::HOOK_LIBRARY,
                                      "Hook library detected: " + loadedLib,
                                      7, false);
                        return true;
                    }
                }
            }

            return false;
        } catch (...) {
            return false;
        }
    }

    bool AntiDetectionManager::detectSuspiciousProcesses() {
        try {
            auto runningProcesses = getRunningProcesses();

            for (const auto& suspiciousProcess : m_knownSuspiciousProcesses) {
                for (const auto& process : runningProcesses) {
                    if (process.find(suspiciousProcess) != std::string::npos) {
                        reportDetection(DetectionType::SUSPICIOUS_PROCESS,
                                      "Suspicious process detected: " + process,
                                      6, false);
                        return true;
                    }
                }
            }

            return false;
        } catch (...) {
            return false;
        }
    }

    bool AntiDetectionManager::detectMemoryTampering() {
        try {
            // Basic memory tampering detection
            // Check for unusual memory mappings
            std::ifstream mapsFile("/proc/self/maps");
            std::string line;
            int suspiciousCount = 0;

            while (std::getline(mapsFile, line)) {
                if (line.find("frida") != std::string::npos ||
                    line.find("gum") != std::string::npos ||
                    line.find("gadget") != std::string::npos) {
                    suspiciousCount++;
                }
            }

            if (suspiciousCount > 0) {
                reportDetection(DetectionType::MEMORY_TAMPERING,
                              "Suspicious memory mappings detected: " + std::to_string(suspiciousCount),
                              7, false);
                return true;
            }

            return false;
        } catch (...) {
            return false;
        }
    }

    bool AntiDetectionManager::detectEnvironmentAnalysis() {
        try {
            // Check for analysis environment indicators
            const char* analysisEnvVars[] = {
                "FRIDA_SCRIPT",
                "FRIDA_OPTIONS",
                "GUM_SCRIPT",
                "XPOSED_BRIDGE"
            };

            for (const char* envVar : analysisEnvVars) {
                if (getenv(envVar) != nullptr) {
                    reportDetection(DetectionType::ENVIRONMENT_ANALYSIS,
                                  "Analysis environment variable detected: " + std::string(envVar),
                                  6, false);
                    return true;
                }
            }

            return false;
        } catch (...) {
            return false;
        }
    }

    bool AntiDetectionManager::detectRootAccess() {
        try {
            const char* rootPaths[] = {
                "/system/bin/su",
                "/system/xbin/su",
                "/sbin/su",
                "/system/app/Superuser.apk",
                "/system/app/SuperSU.apk"
            };

            for (const char* path : rootPaths) {
                if (access(path, F_OK) == 0) {
                    reportDetection(DetectionType::ROOT_ACCESS,
                                  "Root indicator detected: " + std::string(path),
                                  4, false);
                    return true;
                }
            }

            return false;
        } catch (...) {
            return false;
        }
    }

    bool AntiDetectionManager::detectXposedFramework() {
        try {
            // Check for Xposed framework
            if (dlopen("libxposed_art.so", RTLD_NOLOAD) != nullptr) {
                reportDetection(DetectionType::XPOSED_FRAMEWORK,
                              "Xposed framework detected",
                              7, false);
                return true;
            }

            return false;
        } catch (...) {
            return false;
        }
    }

    std::vector<std::string> AntiDetectionManager::getLoadedLibraries() {
        std::vector<std::string> libraries;

        try {
            std::ifstream mapsFile("/proc/self/maps");
            std::string line;

            while (std::getline(mapsFile, line)) {
                size_t pos = line.find(".so");
                if (pos != std::string::npos) {
                    size_t start = line.rfind(' ', pos) + 1;
                    std::string libPath = line.substr(start);

                    // Extract library name from path
                    size_t lastSlash = libPath.rfind('/');
                    if (lastSlash != std::string::npos) {
                        std::string libName = libPath.substr(lastSlash + 1);
                        if (std::find(libraries.begin(), libraries.end(), libName) == libraries.end()) {
                            libraries.push_back(libName);
                        }
                    }
                }
            }
        } catch (...) {
            LOGE("Failed to get loaded libraries");
        }

        return libraries;
    }

    std::vector<std::string> AntiDetectionManager::getRunningProcesses() {
        std::vector<std::string> processes;

        try {
            DIR* dir = opendir("/proc");
            if (!dir) return processes;

            struct dirent* entry;
            while ((entry = readdir(dir)) != nullptr) {
                if (entry->d_type == DT_DIR) {
                    std::string pid = entry->d_name;
                    if (pid == "." || pid == "..") continue;

                    // Check if it's a valid PID
                    if (std::all_of(pid.begin(), pid.end(), ::isdigit)) {
                        std::string cmdlinePath = "/proc/" + pid + "/cmdline";
                        std::ifstream cmdlineFile(cmdlinePath);
                        if (cmdlineFile.is_open()) {
                            std::string cmdline;
                            std::getline(cmdlineFile, cmdline);
                            if (!cmdline.empty()) {
                                processes.push_back(cmdline);
                            }
                        }
                    }
                }
            }
            closedir(dir);
        } catch (...) {
            LOGE("Failed to get running processes");
        }

        return processes;
    }

    std::vector<int> AntiDetectionManager::getOpenPorts() {
        std::vector<int> ports;

        try {
            std::ifstream netFile("/proc/net/tcp");
            std::string line;

            // Skip header line
            std::getline(netFile, line);

            while (std::getline(netFile, line)) {
                std::istringstream iss(line);
                std::string token;

                // Skip first column (sl)
                iss >> token;

                // Get local address (second column)
                iss >> token;

                size_t colonPos = token.find(':');
                if (colonPos != std::string::npos) {
                    std::string portHex = token.substr(colonPos + 1);
                    int port = std::stoi(portHex, nullptr, 16);
                    ports.push_back(port);
                }
            }
        } catch (...) {
            LOGE("Failed to get open ports");
        }

        return ports;
    }

    void AntiDetectionManager::reportDetection(DetectionType type, const std::string& details,
                                             int threatLevel, bool shouldExit) {
        DetectionResult result;
        result.detected = true;
        result.detectionType = AntiDetectionUtils::detectionTypeToString(type);
        result.details = details;
        result.timestamp = std::chrono::steady_clock::now();
        result.threatLevel = threatLevel;
        result.shouldExit = shouldExit;

        // Log the detection
        if (m_config.logDetections) {
            LOGW("DETECTION: %s - %s (Threat Level: %d)",
                 result.detectionType.c_str(), details.c_str(), threatLevel);
        }

        // Add to history
        {
            std::lock_guard<std::mutex> lock(m_historyMutex);
            m_detectionHistory.push_back(result);

            // Limit history size
            if (m_detectionHistory.size() > static_cast<size_t>(m_config.maxDetectionHistory)) {
                m_detectionHistory.erase(m_detectionHistory.begin());
            }
        }

        // Notify callbacks
        notifyCallbacks(result);

        // Handle high threat detection
        if (threatLevel >= m_config.maxThreatLevel || shouldExit) {
            handleHighThreatDetection(result);
        }
    }

    void AntiDetectionManager::notifyCallbacks(const DetectionResult& result) {
        std::lock_guard<std::mutex> lock(m_callbackMutex);

        for (const auto& callback : m_callbacks) {
            try {
                callback(result);
            } catch (const std::exception& e) {
                LOGE("Exception in detection callback: %s", e.what());
            } catch (...) {
                LOGE("Unknown exception in detection callback");
            }
        }
    }

    void AntiDetectionManager::handleHighThreatDetection(const DetectionResult& result) {
        LOGE("HIGH THREAT DETECTED: %s - %s (Level: %d)",
             result.detectionType.c_str(), result.details.c_str(), result.threatLevel);

        if (m_config.exitOnDetection && result.shouldExit) {
            LOGE("Exiting due to high threat detection");
            std::exit(1);
        }
    }

    // Singleton implementation
    AntiDetectionManager* AntiDetectionManagerInstance::getInstance() {
        std::lock_guard<std::mutex> lock(s_instanceMutex);
        return s_instance.get();
    }

    bool AntiDetectionManagerInstance::createInstance(JNIEnv* env, jobject context,
                                                     const AntiDetectionManager::AntiDetectionConfig& config) {
        std::lock_guard<std::mutex> lock(s_instanceMutex);

        if (s_instance) {
            LOGW("AntiDetectionManager instance already exists");
            return false;
        }

        s_instance = std::make_unique<AntiDetectionManager>();
        return s_instance->initialize(env, context, config);
    }

    void AntiDetectionManagerInstance::destroyInstance() {
        std::lock_guard<std::mutex> lock(s_instanceMutex);
        s_instance.reset();
    }

    // Utility functions
    namespace AntiDetectionUtils {
        std::string detectionTypeToString(AntiDetectionManager::DetectionType type) {
            switch (type) {
                case AntiDetectionManager::DetectionType::FRIDA_SERVER: return "FRIDA_SERVER";
                case AntiDetectionManager::DetectionType::FRIDA_LIBRARY: return "FRIDA_LIBRARY";
                case AntiDetectionManager::DetectionType::FRIDA_PROCESS: return "FRIDA_PROCESS";
                case AntiDetectionManager::DetectionType::FRIDA_THREAD: return "FRIDA_THREAD";
                case AntiDetectionManager::DetectionType::FRIDA_PORT: return "FRIDA_PORT";
                case AntiDetectionManager::DetectionType::DEBUGGER: return "DEBUGGER";
                case AntiDetectionManager::DetectionType::EMULATOR: return "EMULATOR";
                case AntiDetectionManager::DetectionType::HOOK_LIBRARY: return "HOOK_LIBRARY";
                case AntiDetectionManager::DetectionType::SUSPICIOUS_PROCESS: return "SUSPICIOUS_PROCESS";
                case AntiDetectionManager::DetectionType::MEMORY_TAMPERING: return "MEMORY_TAMPERING";
                case AntiDetectionManager::DetectionType::ENVIRONMENT_ANALYSIS: return "ENVIRONMENT_ANALYSIS";
                case AntiDetectionManager::DetectionType::ROOT_ACCESS: return "ROOT_ACCESS";
                case AntiDetectionManager::DetectionType::XPOSED_FRAMEWORK: return "XPOSED_FRAMEWORK";
                default: return "UNKNOWN";
            }
        }

        std::string threatLevelToString(int level) {
            if (level >= 9) return "CRITICAL";
            if (level >= 7) return "HIGH";
            if (level >= 5) return "MEDIUM";
            if (level >= 3) return "LOW";
            return "MINIMAL";
        }

        bool isHighThreat(int level) {
            return level >= 7;
        }

        bool isCriticalThreat(int level) {
            return level >= 9;
        }

        bool shouldExitOnThreat(int level) {
            return level >= 8;
        }
    }

    // Additional public methods implementation
    AntiDetectionManager::DetectionResult AntiDetectionManager::performQuickScan() {
        DetectionResult result;
        result.detected = false;
        result.detectionType = "QUICK_SCAN";
        result.details = "Quick scan completed";
        result.timestamp = std::chrono::steady_clock::now();
        result.threatLevel = 0;
        result.shouldExit = false;

        try {
            // Perform only critical detection methods for quick scan
            if (detectFridaServer() || detectFridaLibraries() || detectDebugger()) {
                result.detected = true;
                result.details = "Threats detected during quick scan";
                result.threatLevel = getCurrentThreatLevel();
            }
        } catch (...) {
            result.details = "Quick scan failed with exception";
        }

        return result;
    }

    AntiDetectionManager::DetectionResult AntiDetectionManager::performFullScan() {
        DetectionResult result;
        result.detected = false;
        result.detectionType = "FULL_SCAN";
        result.details = "Full scan completed";
        result.timestamp = std::chrono::steady_clock::now();
        result.threatLevel = 0;
        result.shouldExit = false;

        try {
            bool anyDetected = !performSingleScan();
            result.detected = anyDetected;
            result.threatLevel = getCurrentThreatLevel();

            if (anyDetected) {
                result.details = "Threats detected during full scan";
            }
        } catch (...) {
            result.details = "Full scan failed with exception";
        }

        return result;
    }

    int AntiDetectionManager::getCurrentThreatLevel() const {
        std::lock_guard<std::mutex> lock(m_historyMutex);

        int maxThreatLevel = 0;
        auto now = std::chrono::steady_clock::now();

        // Check recent detections (last 30 seconds)
        for (const auto& detection : m_detectionHistory) {
            auto timeDiff = std::chrono::duration_cast<std::chrono::seconds>(now - detection.timestamp);
            if (timeDiff.count() <= 30) {
                maxThreatLevel = std::max(maxThreatLevel, detection.threatLevel);
            }
        }

        return maxThreatLevel;
    }

    bool AntiDetectionManager::hasActiveThreats() const {
        return getCurrentThreatLevel() > 0;
    }

    std::vector<std::string> AntiDetectionManager::getActiveThreats() const {
        std::vector<std::string> activeThreats;
        std::lock_guard<std::mutex> lock(m_historyMutex);

        auto now = std::chrono::steady_clock::now();

        // Get recent detections (last 30 seconds)
        for (const auto& detection : m_detectionHistory) {
            auto timeDiff = std::chrono::duration_cast<std::chrono::seconds>(now - detection.timestamp);
            if (timeDiff.count() <= 30 && detection.detected) {
                activeThreats.push_back(detection.detectionType + ": " + detection.details);
            }
        }

        return activeThreats;
    }

    void AntiDetectionManager::addDetectionCallback(const DetectionCallback& callback) {
        std::lock_guard<std::mutex> lock(m_callbackMutex);
        m_callbacks.push_back(callback);
    }

    void AntiDetectionManager::clearDetectionCallbacks() {
        std::lock_guard<std::mutex> lock(m_callbackMutex);
        m_callbacks.clear();
    }

    std::vector<AntiDetectionManager::DetectionResult> AntiDetectionManager::getDetectionHistory() const {
        std::lock_guard<std::mutex> lock(m_historyMutex);
        return m_detectionHistory;
    }

    AntiDetectionManager::DetectionResult AntiDetectionManager::getLastDetection() const {
        std::lock_guard<std::mutex> lock(m_historyMutex);

        if (m_detectionHistory.empty()) {
            DetectionResult emptyResult;
            emptyResult.detected = false;
            emptyResult.detectionType = "NONE";
            emptyResult.details = "No detections recorded";
            emptyResult.timestamp = std::chrono::steady_clock::now();
            emptyResult.threatLevel = 0;
            emptyResult.shouldExit = false;
            return emptyResult;
        }

        return m_detectionHistory.back();
    }

    bool AntiDetectionManager::enableAntiAnalysisEvasion() {
        try {
            LOGI("Enabling anti-analysis evasion");
            // Implementation would include various evasion techniques
            return true;
        } catch (...) {
            LOGE("Failed to enable anti-analysis evasion");
            return false;
        }
    }

    bool AntiDetectionManager::enableStealthMode() {
        try {
            LOGI("Enabling stealth mode");
            m_stealthModeActive.store(true);
            // Implementation would include stealth techniques
            return true;
        } catch (...) {
            LOGE("Failed to enable stealth mode");
            return false;
        }
    }

    bool AntiDetectionManager::enableEnvironmentSpoofing() {
        try {
            LOGI("Enabling environment spoofing");
            // Implementation would include environment spoofing techniques
            return true;
        } catch (...) {
            LOGE("Failed to enable environment spoofing");
            return false;
        }
    }

    // Static utility methods
    bool AntiDetectionManager::isEnvironmentSafe() {
        // Quick static check for environment safety
        return access("/system/bin/su", F_OK) != 0 &&
               access("/dev/socket/qemud", F_OK) != 0;
    }

    bool AntiDetectionManager::isNonRootDevice() {
        const char* rootPaths[] = {
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su"
        };

        for (const char* path : rootPaths) {
            if (access(path, F_OK) == 0) {
                return false;
            }
        }

        return true;
    }

    std::vector<std::string> AntiDetectionManager::getKnownFridaSignatures() {
        return FRIDA_LIBRARIES;
    }

    std::vector<std::string> AntiDetectionManager::getKnownHookingTools() {
        return HOOK_LIBRARIES;
    }

} // namespace BearLoader::NonRoot
