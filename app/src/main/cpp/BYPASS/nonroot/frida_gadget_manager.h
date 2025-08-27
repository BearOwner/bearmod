#ifndef BEARMOD_FRIDA_GADGET_MANAGER_H
#define BEARMOD_FRIDA_GADGET_MANAGER_H

#include <string>
#include <vector>
#include <memory>
#include <functional>
#include <atomic>
#include <mutex>
#include <jni.h>
#include "../anti_hook/gadget_detector.h"
#include "../anti_hook/memory_protector.h"
#include "anti_detection_manager.h"


    namespace BearLoader::NonRoot {
        
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
                std::string gadgetPath;
                int securityLevel;
                bool antiDetectionActive;
            };
            
        private:
            // Security and state management
            std::atomic<bool> m_initialized{false};
            std::atomic<bool> m_injectionActive{false};
            mutable std::mutex m_operationMutex;

            // Configuration
            GadgetConfig m_config;
            std::string m_obfuscatedGadgetPath;
            std::string m_configPath;

            // JNI environment
            JavaVM* m_jvm = nullptr;
            jobject m_context = nullptr;

            // Error and result tracking
            std::string m_lastError;
            InjectionResult m_lastResult;

            // Security components
            std::unique_ptr<class GadgetObfuscator> m_obfuscator;
            std::unique_ptr<class AntiDetectionManager> m_antiDetection;
            std::unique_ptr<BearLoader::EnhancedAntiHook::MemoryProtector> m_memoryProtector;
            
            // Internal methods
            bool validateTargetAPK(const std::string& apkPath);  // Changed from static to instance method
            static bool validateTargetPackage(const std::string& packageName);
            bool prepareObfuscatedGadget();  // Changed from static to instance method
            bool createSecureConfig();  // Changed from static to instance method
            bool injectGadgetToAPK(const std::string& apkPath, const std::string& outputPath);  // Changed from static to instance method
            bool verifyInjectionIntegrity(const std::string& patchedApkPath);  // Changed from static to instance method
            bool loadAndExecuteScripts();  // Changed from static to instance method
            bool verifyInjectionSuccess();  // Changed from static to instance method
            void cleanupTemporaryFiles();
            void shutdownSecurityComponents();

            // Security methods
            bool performSecurityChecks();  // Changed from static to instance method
            bool enableAntiDetectionMeasures();  // Changed from static to instance method
            bool protectGadgetMemory();  // Changed from static to instance method
            bool initializeGadgetDetection();
            bool initializeMemoryProtection();
            bool initializeAntiDetection();
            bool validateSecurityEnvironment();
            static bool validateBearToken(const std::string& bearToken);
            static bool detectAnalysisEnvironment();
            bool detectDebugging();  // Changed from static to instance method
            static std::string generateSecureToken();

            // Obfuscation methods
            std::string obfuscateLibraryName(const std::string& originalName);  // Changed from static to instance method
            bool obfuscateGadgetBinary(const std::string& inputPath, const std::string& outputPath);  // Changed from static to instance method
            static std::string encryptConfiguration(const std::string& config);
            
        public:
            FridaGadgetManager();
            ~FridaGadgetManager();
            
            // Core functionality
            bool initialize(JNIEnv* env, jobject context, const GadgetConfig& config);
            void shutdown();
            
            // Main injection methods
            bool injectToAPK(const std::string& targetApkPath,
                           const std::string& outputApkPath,
                           const ProgressCallback& progressCallback = nullptr,
                           const CompletionCallback& completionCallback = nullptr);
            
            bool injectToRunningProcess(const std::string& packageName,
                                      const ProgressCallback& progressCallback = nullptr,
                                      const CompletionCallback& completionCallback = nullptr);
            
            // Script management
            bool loadScript(const std::string& scriptPath);  // Changed from static to instance method
            bool executeScript(const std::string& scriptContent);
            bool unloadAllScripts();
            
            // Security and monitoring
            bool isDetectionRiskHigh();
            bool enableStealthMode();
            bool disableStealthMode();
            std::vector<std::string> getActiveDetectionMethods();

            // Anti-detection methods
            bool performSecurityScan();
            int getCurrentThreatLevel();
            std::vector<std::string> getActiveThreats();
            bool isAntiDetectionActive();
            
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
         * Singleton manager for FridaGadgetManager instances
         */
        class GadgetManagerInstance {
        private:
            static std::unique_ptr<FridaGadgetManager> s_instance;
            static std::mutex s_instanceMutex;

        public:
            static FridaGadgetManager* getInstance();
            static bool createInstance(JNIEnv* env, jobject context, const FridaGadgetManager::GadgetConfig& config);
            static void destroyInstance();
        };

        /**
         * Factory class for creating configured FridaGadgetManager instances
         */
        class GadgetManagerFactory {
        public:
            static std::unique_ptr<FridaGadgetManager>
            createSecureManager();

            static std::unique_ptr<FridaGadgetManager> createStealthManager(
                const std::string& targetPackage,
                const std::string& keyAuthToken
            );
        };
        
    } // namespace BearLoader::NonRoot


#endif // BEARMOD_FRIDA_GADGET_MANAGER_H
