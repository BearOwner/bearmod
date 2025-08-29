#ifndef BEARMOD_ANTI_DETECTION_MANAGER_H
#define BEARMOD_ANTI_DETECTION_MANAGER_H

#include <string>
#include <vector>
#include <memory>
#include <atomic>
#include <mutex>
#include <functional>
#include <chrono>
#include <unordered_set>
#include <unordered_map>
#include <thread>
#include <jni.h>

namespace BearLoader::NonRoot {
    
    /**
     * Advanced Anti-Detection Manager for Non-Root Frida Gadget Injection
     * Provides comprehensive detection and evasion capabilities against analysis tools
     */
    class AntiDetectionManager {
    public:
        // Detection result structure
        struct DetectionResult {
            bool detected;
            std::string detectionType;
            std::string details;
            std::chrono::steady_clock::time_point timestamp;
            int threatLevel; // 1-10 scale
            bool shouldExit; // Whether this detection should trigger exit
        };
        
        // Detection callback type
        using DetectionCallback = std::function<void(const DetectionResult&)>;
        
        // Detection types enum
        enum class DetectionType {
            FRIDA_SERVER,           // Frida server detection
            FRIDA_LIBRARY,          // Frida library detection
            FRIDA_PROCESS,          // Frida process detection
            FRIDA_THREAD,           // Frida thread detection
            FRIDA_PORT,             // Frida port detection
            DEBUGGER,               // Debugger detection
            EMULATOR,               // Emulator detection
            HOOK_LIBRARY,           // Hook library detection
            SUSPICIOUS_PROCESS,     // Suspicious process detection
            MEMORY_TAMPERING,       // Memory tampering detection
            ENVIRONMENT_ANALYSIS,   // Analysis environment detection
            ROOT_ACCESS,            // Root access detection
            XPOSED_FRAMEWORK        // Xposed framework detection
        };
        
        // Configuration structure
        struct AntiDetectionConfig {
            // Detection toggles
            bool enableFridaServerDetection = true;
            bool enableFridaLibraryDetection = true;
            bool enableFridaProcessDetection = true;
            bool enableFridaThreadDetection = true;
            bool enableFridaPortDetection = true;
            bool enableDebuggerDetection = true;
            bool enableEmulatorDetection = true;
            bool enableHookLibraryDetection = true;
            bool enableSuspiciousProcessDetection = true;
            bool enableMemoryTamperingDetection = true;
            bool enableEnvironmentAnalysisDetection = true;
            bool enableRootAccessDetection = false; // Disabled for non-root
            bool enableXposedFrameworkDetection = true;
            
            // Monitoring settings
            int scanIntervalMs = 3000;          // 3 seconds
            int maxThreatLevel = 8;             // Exit if threat level exceeds this
            bool exitOnDetection = true;        // Exit immediately on high-threat detection
            bool logDetections = true;          // Log detection events
            bool enableContinuousMonitoring = true; // Continuous background monitoring
            
            // Performance settings
            int maxDetectionHistory = 100;     // Maximum detection history to keep
            int detectionTimeoutMs = 5000;     // Timeout for individual detection methods
            bool enableParallelScanning = true; // Enable parallel detection methods
            
            // Evasion settings
            bool enableAntiAnalysisEvasion = true;  // Enable anti-analysis evasion
            bool enableStealthMode = true;          // Enable stealth mode
            bool enableEnvironmentSpoofing = true;  // Enable environment spoofing
        };
        
        // Evasion result structure
        struct EvasionResult {
            bool success;
            std::string method;
            std::string details;
            std::chrono::steady_clock::time_point timestamp;
        };
        
    private:
        // Internal state
        std::atomic<bool> m_initialized{false};
        std::atomic<bool> m_monitoringActive{false};
        std::atomic<bool> m_stealthModeActive{false};
        std::mutex m_configMutex;
        std::mutex m_callbackMutex;
        mutable std::mutex m_historyMutex;  // mutable allows locking in const methods
        
        // Configuration
        AntiDetectionConfig m_config;
        std::vector<DetectionCallback> m_callbacks;
        
        // Detection state
        std::vector<DetectionResult> m_detectionHistory;
        std::unordered_set<std::string> m_knownFridaLibraries;
        std::unordered_set<std::string> m_knownFridaProcesses;
        std::unordered_set<int> m_knownFridaPorts;
        std::unordered_set<std::string> m_knownHookLibraries;
        std::unordered_set<std::string> m_knownSuspiciousProcesses;
        
        // Monitoring thread
        std::unique_ptr<std::thread> m_monitoringThread;
        std::atomic<bool> m_shouldStopMonitoring{false};
        
        // JNI environment
        JavaVM* m_jvm = nullptr;
        jobject m_context = nullptr;
        
        // Detection methods
        bool detectFridaServer();
        bool detectFridaLibraries();
        bool detectFridaProcesses();
        bool detectFridaThreads();
        bool detectFridaPorts();
        bool detectDebugger();
        bool detectEmulator();
        bool detectHookLibraries();
        bool detectSuspiciousProcesses();
        bool detectMemoryTampering();
        bool detectEnvironmentAnalysis();
        bool detectRootAccess();
        bool detectXposedFramework();
        
        // Utility methods
        std::vector<std::string> getLoadedLibraries();
        std::vector<std::string> getRunningProcesses();
        std::vector<int> getOpenPorts();
        std::vector<std::string> getRunningThreads();
        bool isProcessRunning(const std::string& processName);
        bool isLibraryLoaded(const std::string& libraryName);
        bool isPortOpen(int port);
        
        // Evasion methods
        bool enableAntiAnalysisEvasion();
        bool enableStealthMode();
        bool enableEnvironmentSpoofing();
        bool hideFromProcessList();
        bool hideFromLibraryList();
        bool spoofEnvironmentVariables();
        bool obfuscateMemoryLayout();
        
        // Monitoring methods
        void monitoringLoop();
        bool performSingleScan();
        bool performDetectionMethod(DetectionType type);
        
        // Reporting methods
        void reportDetection(DetectionType type, const std::string& details, int threatLevel, bool shouldExit = false);
        void notifyCallbacks(const DetectionResult& result);
        void handleHighThreatDetection(const DetectionResult& result);
        
        // Initialization methods
        void initializeKnownSignatures();
        void initializeJNIEnvironment(JNIEnv* env, jobject context);
        
    public:
        AntiDetectionManager();
        ~AntiDetectionManager();
        
        // Configuration
        bool initialize(JNIEnv* env, jobject context, const AntiDetectionConfig& config);
        void shutdown();
        bool updateConfig(const AntiDetectionConfig& config);
        AntiDetectionConfig getConfig() const;
        
        // Detection control
        bool startMonitoring();
        void stopMonitoring();
        bool isMonitoringActive() const { return m_monitoringActive.load(); }
        
        // Manual detection methods
        DetectionResult performQuickScan();
        DetectionResult performFullScan();
        DetectionResult performSpecificDetection(DetectionType type);
        std::vector<DetectionResult> performAllDetections();
        
        // Evasion control
        bool enableAllEvasionMethods();
        bool disableAllEvasionMethods();
        bool isStealthModeActive() const { return m_stealthModeActive.load(); }
        EvasionResult enableSpecificEvasion(const std::string& method);
        
        // Callback management
        void addDetectionCallback(const DetectionCallback& callback);
        void clearDetectionCallbacks();
        
        // Status and diagnostics
        std::vector<DetectionResult> getDetectionHistory() const;
        DetectionResult getLastDetection() const;
        int getCurrentThreatLevel() const;
        bool hasActiveThreats() const;
        std::vector<std::string> getActiveThreats() const;
        
        // Utility methods
        static bool isEnvironmentSafe();
        static bool isNonRootDevice();
        static std::vector<std::string> getKnownFridaSignatures();
        static std::vector<std::string> getKnownHookingTools();
        
    private:
        // Prevent copying
        AntiDetectionManager(const AntiDetectionManager&) = delete;
        AntiDetectionManager& operator=(const AntiDetectionManager&) = delete;
    };
    
    /**
     * Singleton manager for global anti-detection
     */
    class AntiDetectionManagerInstance {
    private:
        static std::unique_ptr<AntiDetectionManager> s_instance;
        static std::mutex s_instanceMutex;
        
    public:
        static AntiDetectionManager* getInstance();
        static bool createInstance(JNIEnv* env, jobject context, const AntiDetectionManager::AntiDetectionConfig& config);
        static void destroyInstance();
        
        // Quick detection methods
        static bool quickThreatCheck();
        static bool isEnvironmentSecure();
        static int assessCurrentThreatLevel();
    };
    
    /**
     * Detection utilities
     */
    namespace AntiDetectionUtils {
        std::string detectionTypeToString(AntiDetectionManager::DetectionType type);
        std::string threatLevelToString(int level);
        bool isHighThreat(int level);
        bool isCriticalThreat(int level);
        bool shouldExitOnThreat(int level);
    }
    
} // namespace BearLoader::NonRoot

#endif // BEARMOD_ANTI_DETECTION_MANAGER_H
