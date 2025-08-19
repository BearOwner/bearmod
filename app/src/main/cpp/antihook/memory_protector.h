#ifndef BEARMOD_MEMORY_PROTECTOR_H
#define BEARMOD_MEMORY_PROTECTOR_H

#include <string>
#include <vector>
#include <memory>
#include <atomic>
#include <mutex>
#include <functional>
#include <chrono>
#include <unordered_map>
#include <cstdint>

namespace BearLoader {
    namespace EnhancedAntiHook {
        
        /**
         * Advanced Memory Protection System
         * Protects critical memory regions from tampering and hook injection
         */
        class MemoryProtector {
        public:
            // Protection types
            enum class ProtectionType {
                READ_ONLY,              // Make region read-only
                EXECUTE_ONLY,           // Make region execute-only
                NO_ACCESS,              // Remove all access
                GUARD_PAGE,             // Add guard pages around region
                CHECKSUM_MONITOR,       // Monitor region with checksums
                HOOK_DETECTION,         // Detect hook modifications
                ANTI_DEBUG             // Anti-debugging protection
            };
            
            // Protection result
            struct ProtectionResult {
                bool success;
                std::string errorMessage;
                uintptr_t protectedAddress;
                size_t protectedSize;
                ProtectionType type;
            };
            
            // Memory region structure
            struct MemoryRegion {
                uintptr_t startAddress;
                size_t size;
                ProtectionType protectionType;
                uint32_t originalChecksum;
                std::chrono::steady_clock::time_point lastCheck;
                bool isProtected;
                std::string name;
            };
            
            // Tampering detection callback
            using TamperingCallback = std::function<void(const MemoryRegion&, const std::string&)>;
            
            // Configuration
            struct ProtectorConfig {
                bool enableChecksumMonitoring = true;
                bool enableHookDetection = true;
                bool enableGuardPages = true;
                bool enableAntiDebug = true;
                
                int checksumIntervalMs = 1000;     // 1 second
                int maxTamperingEvents = 3;        // Exit after this many events
                bool exitOnTampering = true;       // Exit immediately on tampering
                bool logTamperingEvents = true;    // Log tampering events
                
                // Critical function protection
                bool protectKeyFunctions = true;
                std::vector<std::string> criticalFunctions = {
                    "Java_com_bearmod_plugin_NonRootManager_initializeGadgetManager",
                    "Java_com_bearmod_plugin_NonRootManager_injectToPackage",
                    "authenticateWithKeyAuth",
                    "validateLicenseForNonRoot"
                };
            };
            
        private:
            // Internal state
            std::atomic<bool> m_running{false};
            std::atomic<bool> m_protectionActive{false};
            std::mutex m_regionsMutex;
            std::mutex m_callbackMutex;
            
            // Configuration
            ProtectorConfig m_config;
            std::vector<TamperingCallback> m_callbacks;
            
            // Protected regions
            std::unordered_map<uintptr_t, MemoryRegion> m_protectedRegions;
            std::vector<uintptr_t> m_guardPages;
            
            // Tampering detection
            std::atomic<int> m_tamperingEventCount{0};
            
            // Protection methods
            bool protectRegionReadOnly(uintptr_t address, size_t size);
            bool protectRegionExecuteOnly(uintptr_t address, size_t size);
            bool protectRegionNoAccess(uintptr_t address, size_t size);
            bool addGuardPages(uintptr_t address, size_t size);
            bool setupChecksumMonitoring(uintptr_t address, size_t size);
            bool setupHookDetection(uintptr_t address, size_t size);
            
            // Monitoring methods
            bool verifyRegionIntegrity(const MemoryRegion& region);
            uint32_t calculateChecksum(uintptr_t address, size_t size);
            bool detectHookModifications(const MemoryRegion& region);
            bool detectInlineHooks(uintptr_t address, size_t size);
            bool detectJumpHooks(uintptr_t address, size_t size);
            
            // Anti-debugging
            bool enableAntiDebugging();
            bool detectDebugger();
            bool detectTracer();
            
            // Utility methods
            bool isAddressValid(uintptr_t address);
            bool isRegionExecutable(uintptr_t address, size_t size);
            std::vector<uint8_t> readMemoryRegion(uintptr_t address, size_t size);
            
            // Reporting
            void reportTampering(const MemoryRegion& region, const std::string& details);
            void notifyCallbacks(const MemoryRegion& region, const std::string& details);
            
        public:
            MemoryProtector();
            ~MemoryProtector();
            
            // Configuration
            bool initialize(const ProtectorConfig& config);
            void shutdown();
            bool updateConfig(const ProtectorConfig& config);
            ProtectorConfig getConfig() const;
            
            // Protection control
            bool startProtection();
            void stopProtection();
            bool isProtectionActive() const { return m_protectionActive.load(); }
            
            // Region protection
            ProtectionResult protectRegion(uintptr_t address, size_t size, 
                                         ProtectionType type, const std::string& name = "");
            bool unprotectRegion(uintptr_t address);
            bool isRegionProtected(uintptr_t address) const;
            
            // Function protection
            ProtectionResult protectFunction(void* functionPtr, const std::string& functionName);
            ProtectionResult protectFunction(const std::string& symbolName);
            bool protectCriticalFunctions();
            
            // Memory monitoring
            bool performIntegrityCheck();
            bool performFullScan();
            std::vector<MemoryRegion> getProtectedRegions() const;
            
            // Callback management
            void addTamperingCallback(TamperingCallback callback);
            void clearTamperingCallbacks();
            
            // Status and diagnostics
            int getTamperingEventCount() const { return m_tamperingEventCount.load(); }
            bool hasDetectedTampering() const { return m_tamperingEventCount.load() > 0; }
            std::vector<std::string> getProtectionStatus() const;
            
            // Utility methods
            static bool isMemoryExecutable(uintptr_t address);
            static bool isMemoryWritable(uintptr_t address);
            static size_t getPageSize();
            static uintptr_t alignToPageBoundary(uintptr_t address);
            
        private:
            // Prevent copying
            MemoryProtector(const MemoryProtector&) = delete;
            MemoryProtector& operator=(const MemoryProtector&) = delete;
        };
        
        /**
         * Singleton manager for global memory protection
         */
        class MemoryProtectorManager {
        private:
            static std::unique_ptr<MemoryProtector> s_instance;
            static std::mutex s_instanceMutex;
            
        public:
            static MemoryProtector* getInstance();
            static bool createInstance(const MemoryProtector::ProtectorConfig& config);
            static void destroyInstance();
            
            // Quick protection methods
            static bool quickProtectFunction(void* functionPtr, const std::string& name);
            static bool quickProtectRegion(uintptr_t address, size_t size);
            static bool isSystemSecure();
        };
        
        /**
         * RAII Memory Protection Guard
         * Automatically protects and unprotects memory regions
         */
        class MemoryProtectionGuard {
        private:
            MemoryProtector* m_protector;
            uintptr_t m_address;
            bool m_isActive;
            
        public:
            MemoryProtectionGuard(MemoryProtector* protector, uintptr_t address, 
                                size_t size, MemoryProtector::ProtectionType type);
            ~MemoryProtectionGuard();
            
            bool isActive() const { return m_isActive; }
            void release();
            
            // Prevent copying
            MemoryProtectionGuard(const MemoryProtectionGuard&) = delete;
            MemoryProtectionGuard& operator=(const MemoryProtectionGuard&) = delete;
        };
        
        /**
         * Protection utilities
         */
        namespace ProtectionUtils {
            std::string protectionTypeToString(MemoryProtector::ProtectionType type);
            bool isHighSecurityType(MemoryProtector::ProtectionType type);
            MemoryProtector::ProtectionType getRecommendedProtection(uintptr_t address);
        }
        
    } // namespace EnhancedAntiHook
} // namespace BearLoader

#endif // BEARMOD_MEMORY_PROTECTOR_H
