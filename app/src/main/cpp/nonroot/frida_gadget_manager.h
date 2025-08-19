#ifndef BEARMOD_FRIDA_GADGET_MANAGER_H
#define BEARMOD_FRIDA_GADGET_MANAGER_H

#include <string>
#include <vector>
#include <memory>
#include <functional>
#include <atomic>
#include <mutex>
#include <jni.h>
#include "../enhanced_antihook/gadget_detector.h"
#include "../enhanced_antihook/memory_protector.h"

namespace BearLoader {
    namespace NonRoot {
        
        /**
         * Secure Frida Gadget Manager for Non-Root Injection
         * Handles obfuscated gadget loading, anti-detection, and secure script injection
         */
        class FridaGadgetManager {
        public:
            // Callback types for async operations
            using ProgressCallback = std::function<void(int progress, const std::string& message)>;
            using CompletionCallback = std::function<void(bool success, const std::string& result)>;
            
            // Gadget injection modes
            enum class InjectionMode {
                STEALTH,        // Maximum stealth, slower injection
                BALANCED,       // Balance between speed and stealth
                FAST           // Fast injection, less stealth
            };
            
            // Security levels
            enum class SecurityLevel {
                MINIMAL,        // Basic obfuscation
                STANDARD,       // Standard security measures
                MAXIMUM        // Maximum security and anti-detection
            };
            
            struct GadgetConfig {
                std::string targetPackage;
                std::string targetActivity;
                InjectionMode mode;
                SecurityLevel security;
                bool enableAntiDetection;
                bool enableMemoryProtection;
                std::vector<std::string> scriptPaths;
                std::string keyAuthToken;  // For authentication
                std::string bearToken;     // BearToken for validation

                // Enhanced security settings
                bool enableGadgetDetection = true;
                bool enableMemoryProtector = true;
                bool validateEnvironment = true;
                int maxRetryAttempts = 3;
                int injectionTimeoutMs = 30000;
            };
            
            struct InjectionResult {
                bool success;
                std::string message;
                std::string injectedProcessId;
                std::vector<std::string> loadedScripts;
                long injectionTime;
            };
            
        private:
            // Security and state management
            std::atomic<bool> m_initialized{false};
            std::atomic<bool> m_injectionActive{false};
            std::mutex m_operationMutex;
            
            // Configuration
            GadgetConfig m_config;
            std::string m_obfuscatedGadgetPath;
            std::string m_configPath;
            
            // Security components
            std::unique_ptr<class GadgetObfuscator> m_obfuscator;
            std::unique_ptr<class AntiDetectionManager> m_antiDetection;
            std::unique_ptr<class MemoryProtector> m_memoryProtector;
            
            // Internal methods
            bool validateTargetAPK(const std::string& apkPath);
            bool prepareObfuscatedGadget();
            bool createSecureConfig();
            bool injectGadgetToAPK(const std::string& apkPath, const std::string& outputPath);
            bool verifyInjectionIntegrity(const std::string& patchedApkPath);
            void cleanupTemporaryFiles();
            
            // Security methods
            bool performSecurityChecks();
            bool enableAntiDetectionMeasures();
            bool protectGadgetMemory();
            std::string generateSecureToken();
            
            // Obfuscation methods
            std::string obfuscateLibraryName(const std::string& originalName);
            bool obfuscateGadgetBinary(const std::string& inputPath, const std::string& outputPath);
            std::string encryptConfiguration(const std::string& config);
            
        public:
            FridaGadgetManager();
            ~FridaGadgetManager();
            
            // Core functionality
            bool initialize(const GadgetConfig& config);
            void shutdown();
            
            // Main injection methods
            bool injectToAPK(const std::string& targetApkPath, 
                           const std::string& outputApkPath,
                           ProgressCallback progressCallback = nullptr,
                           CompletionCallback completionCallback = nullptr);
            
            bool injectToRunningProcess(const std::string& packageName,
                                      ProgressCallback progressCallback = nullptr,
                                      CompletionCallback completionCallback = nullptr);
            
            // Script management
            bool loadScript(const std::string& scriptPath);
            bool executeScript(const std::string& scriptContent);
            bool unloadAllScripts();
            
            // Security and monitoring
            bool isDetectionRiskHigh();
            bool enableStealthMode();
            bool disableStealthMode();
            std::vector<std::string> getActiveDetectionMethods();
            
            // Configuration management
            bool updateConfig(const GadgetConfig& newConfig);
            GadgetConfig getCurrentConfig() const;
            
            // Status and diagnostics
            bool isInitialized() const { return m_initialized.load(); }
            bool isInjectionActive() const { return m_injectionActive.load(); }
            std::string getLastError() const;
            InjectionResult getLastInjectionResult() const;
            
            // KeyAuth integration
            bool authenticateWithKeyAuth(const std::string& bearToken);
            bool validateLicenseForNonRoot();
            
            // Utility methods
            static bool isNonRootDevice();
            static std::vector<std::string> getSupportedArchitectures();
            static bool checkFridaGadgetCompatibility();
            
        private:
            // Prevent copying
            FridaGadgetManager(const FridaGadgetManager&) = delete;
            FridaGadgetManager& operator=(const FridaGadgetManager&) = delete;
        };
        
        /**
         * Factory class for creating configured FridaGadgetManager instances
         */
        class GadgetManagerFactory {
        public:
            static std::unique_ptr<FridaGadgetManager> createSecureManager(
                const std::string& keyAuthToken,
                FridaGadgetManager::SecurityLevel securityLevel = FridaGadgetManager::SecurityLevel::MAXIMUM
            );
            
            static std::unique_ptr<FridaGadgetManager> createStealthManager(
                const std::string& targetPackage,
                const std::string& keyAuthToken
            );
        };
        
    } // namespace NonRoot
} // namespace BearLoader

#endif // BEARMOD_FRIDA_GADGET_MANAGER_H
