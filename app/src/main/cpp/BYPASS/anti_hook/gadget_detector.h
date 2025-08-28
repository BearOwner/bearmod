#ifndef BEARMOD_GADGET_DETECTOR_H
#define BEARMOD_GADGET_DETECTOR_H

#include <string>
#include <vector>
#include <memory>
#include <atomic>
#include <mutex>
#include <functional>
#include <chrono>
#include <unordered_set>

namespace BearLoader {
    namespace EnhancedAntiHook {
        
        /**
         * Advanced Gadget Detection System
         * Detects Frida Gadget presence and injection attempts on non-root devices
         */
        class GadgetDetector {
        public:
            // Detection result structure
            struct DetectionResult {
                bool detected;
                std::string detectionMethod;
                std::string details;
                std::chrono::steady_clock::time_point timestamp;
                int threatLevel; // 1-10 scale
            };
            
            // Detection callback type
            using DetectionCallback = std::function<void(const DetectionResult&)>;
            
            // Detection methods enum
            enum class DetectionMethod {
                LIBRARY_SCAN,           // Scan for Frida libraries
                PROCESS_SCAN,           // Scan for Frida processes
                MEMORY_SCAN,            // Scan memory for Frida signatures
                THREAD_SCAN,            // Scan for Frida threads
                PORT_SCAN,              // Scan for Frida server ports
                ENVIRONMENT_SCAN,       // Scan environment variables
                SYMBOL_SCAN,            // Scan for Frida symbols
                HOOK_DETECTION,         // Detect hook modifications
                GADGET_SPECIFIC         // Gadget-specific detection
            };
            
            // Configuration structure
            struct DetectorConfig {
                bool enableLibraryScan = true;
                bool enableProcessScan = true;
                bool enableMemoryScan = true;
                bool enableThreadScan = true;
                bool enablePortScan = true;
                bool enableEnvironmentScan = true;
                bool enableSymbolScan = true;
                bool enableHookDetection = true;
                bool enableGadgetSpecific = true;
                
                int scanIntervalMs = 5000;     // 5 seconds
                int maxThreatLevel = 7;        // Exit if threat level exceeds this
                bool exitOnDetection = true;   // Exit immediately on detection
                bool logDetections = true;     // Log detection events
            };
            
        private:
            // Internal state
            std::atomic<bool> m_running{false};
            std::atomic<bool> m_detectionActive{false};
            std::mutex m_configMutex;
            std::mutex m_callbackMutex;
            
            // Configuration
            DetectorConfig m_config;
            std::vector<DetectionCallback> m_callbacks;
            
            // Detection state
            std::unordered_set<std::string> m_knownLibraries;
            std::unordered_set<std::string> m_knownProcesses;
            std::unordered_set<int> m_knownPorts;
            std::vector<DetectionResult> m_detectionHistory;
            
            // Detection methods
            bool detectFridaLibraries();
            bool detectFridaProcesses();
            static bool detectFridaInMemory();
            static bool detectFridaThreads();
            bool detectFridaPorts();
            static bool detectFridaEnvironment();
            static bool detectFridaSymbols();
            static bool detectHookModifications();
            static bool detectGadgetSpecific();

            // Utility methods
            bool scanProcessMaps();
            bool scanRunningProcesses();
            bool scanOpenPorts();
            bool scanMemoryRegions();
            bool scanLoadedLibraries();
            static std::vector<std::string> getLoadedLibraries();
            static std::vector<std::string> getRunningProcesses();
            static std::vector<int> getOpenPorts();

            // Signature detection
            bool findSignatureInMemory(const std::vector<uint8_t>& signature);
            static bool findStringInMemory(const std::string& searchString);
            
            // Reporting
            void reportDetection(DetectionMethod method, const std::string& details, int threatLevel);
            void notifyCallbacks(const DetectionResult& result);
            
        public:
            GadgetDetector();
            ~GadgetDetector();
            
            // Configuration
            bool initialize(const DetectorConfig& config);
            void shutdown();
            bool updateConfig(const DetectorConfig& config);
            DetectorConfig getConfig() const;
            
            // Detection control
            bool startDetection();
            void stopDetection();
            bool isDetectionActive() const { return m_detectionActive.load(); }
            
            // Manual detection methods
            DetectionResult performSingleScan();
            DetectionResult performMethodScan(DetectionMethod method);
            std::vector<DetectionResult> performFullScan();
            
            // Callback management
            void addDetectionCallback(const DetectionCallback& callback);
            void clearDetectionCallbacks();
            
            // Status and history
            std::vector<DetectionResult> getDetectionHistory() const;
            DetectionResult getLastDetection() const;
            int getCurrentThreatLevel() const;
            bool hasActiveThreats() const;
            
            // Utility methods
            static bool isGadgetPresent();
            static std::vector<std::string> getKnownGadgetNames();
            static std::vector<std::string> getKnownFridaSignatures();
            
        private:
            // Prevent copying
            GadgetDetector(const GadgetDetector&) = delete;
            GadgetDetector& operator=(const GadgetDetector&) = delete;
        };
        
        /**
         * Singleton manager for global gadget detection
         */
        class GadgetDetectorManager {
        private:
            static std::unique_ptr<GadgetDetector> s_instance;
            static std::mutex s_instanceMutex;
            
        public:
            static GadgetDetector* getInstance();
            static bool createInstance(const GadgetDetector::DetectorConfig& config);
            static void destroyInstance();
            
            // Quick detection methods
            static bool quickGadgetCheck();
            static bool isEnvironmentSafe();
            static int assessThreatLevel();
        };
        
        /**
         * Detection result utilities
         */
        namespace DetectionUtils {
            std::string detectionMethodToString(GadgetDetector::DetectionMethod method);
            std::string threatLevelToString(int level);
            bool isHighThreat(int level);
            bool isCriticalThreat(int level);
        }
        
    } // namespace EnhancedAntiHook
} // namespace BearLoader

#endif // BEARMOD_GADGET_DETECTOR_H
