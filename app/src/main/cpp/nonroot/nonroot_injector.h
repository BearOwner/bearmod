#ifndef BEARMOD_NONROOT_INJECTOR_H
#define BEARMOD_NONROOT_INJECTOR_H

#include <string>
#include <vector>
#include <memory>
#include <functional>
#include <atomic>
#include <mutex>
#include <chrono>
#include "frida_gadget_manager.h"
#include "objection_patcher.h"

namespace BearLoader {
    namespace NonRoot {
        
        /**
         * Non-Root Injection Coordinator
         * Orchestrates the complete non-root injection workflow
         * Combines gadget management, APK patching, and script injection
         */
        class NonRootInjector {
        public:
            // Injection workflow stages
            enum class InjectionStage {
                IDLE,
                INITIALIZING,
                AUTHENTICATING,
                ANALYZING_TARGET,
                PATCHING_APK,
                INSTALLING_PATCHED_APK,
                LAUNCHING_TARGET,
                INJECTING_SCRIPTS,
                VERIFYING_INJECTION,
                MONITORING,
                COMPLETED,
                FAILED
            };
            
            // Injection configuration
            struct InjectionConfig {
                // Target application
                std::string targetPackageName;
                std::string targetApkPath;
                std::string targetActivity;
                
                // Authentication
                std::string keyAuthToken;
                std::string bearToken;
                
                // Injection settings
                FridaGadgetManager::InjectionMode injectionMode = FridaGadgetManager::InjectionMode::STEALTH;
                FridaGadgetManager::SecurityLevel securityLevel = FridaGadgetManager::SecurityLevel::MAXIMUM;
                
                // Patching settings
                bool requireApkPatching = true;
                bool autoInstallPatchedApk = true;
                bool autoLaunchTarget = true;
                std::string patchedApkOutputPath;
                
                // Script settings
                std::vector<std::string> scriptPaths;
                bool loadDefaultScripts = true;
                bool enableAntiDetection = true;
                
                // Monitoring settings
                bool enableContinuousMonitoring = true;
                int monitoringIntervalMs = 5000;
                int maxInjectionRetries = 3;
                int injectionTimeoutMs = 60000;
                
                // Security settings
                bool enableSecurityValidation = true;
                bool enableEnvironmentChecks = true;
                bool exitOnSecurityThreat = true;
            };
            
            // Injection result
            struct InjectionResult {
                bool success;
                std::string message;
                InjectionStage finalStage;
                std::string targetPackage;
                std::string patchedApkPath;
                std::vector<std::string> loadedScripts;
                long totalInjectionTime;
                long patchingTime;
                long scriptLoadingTime;
                int retryAttempts;
                bool isMonitoring;
                std::chrono::steady_clock::time_point injectionTimestamp;
            };
            
            // Callback types
            using ProgressCallback = std::function<void(InjectionStage stage, int progress, const std::string& message)>;
            using CompletionCallback = std::function<void(const InjectionResult&)>;
            using MonitoringCallback = std::function<void(const std::string& event, const std::string& details)>;
            
        private:
            // Internal state
            std::atomic<bool> m_initialized{false};
            std::atomic<bool> m_injectionActive{false};
            std::atomic<bool> m_monitoring{false};
            std::mutex m_operationMutex;
            std::mutex m_callbackMutex;
            
            // Configuration
            InjectionConfig m_config;
            
            // Components
            std::unique_ptr<FridaGadgetManager> m_gadgetManager;
            std::unique_ptr<ObjectionPatcher> m_patcher;
            
            // Callbacks
            ProgressCallback m_progressCallback;
            CompletionCallback m_completionCallback;
            MonitoringCallback m_monitoringCallback;
            
            // State tracking
            InjectionStage m_currentStage;
            InjectionResult m_lastResult;
            std::string m_lastError;
            int m_retryCount;
            std::chrono::steady_clock::time_point m_injectionStartTime;
            
            // Workflow methods
            bool performAuthentication();
            bool analyzeTargetApplication();
            bool patchTargetApk();
            bool installPatchedApk();
            bool launchTargetApplication();
            bool injectScripts();
            bool verifyInjectionSuccess();
            bool startMonitoring();
            bool stopMonitoring();
            
            // Utility methods
            bool isTargetAppRunning();
            bool waitForTargetApp(int timeoutMs);
            bool validateInjectionEnvironment();
            bool loadDefaultScriptSet();
            std::vector<std::string> getDefaultScripts();
            
            // Security methods
            bool performSecurityValidation();
            bool checkEnvironmentSecurity();
            bool validateTargetAppIntegrity();
            
            // Monitoring methods
            void monitoringLoop();
            bool checkInjectionHealth();
            bool detectSecurityThreats();
            
            // Reporting
            void reportProgress(InjectionStage stage, int progress, const std::string& message);
            void reportStageChange(InjectionStage newStage);
            void reportError(const std::string& error);
            void reportMonitoringEvent(const std::string& event, const std::string& details);
            
        public:
            NonRootInjector();
            ~NonRootInjector();
            
            // Configuration
            bool initialize(const InjectionConfig& config);
            void shutdown();
            bool updateConfig(const InjectionConfig& config);
            InjectionConfig getConfig() const;
            
            // Main injection workflow
            bool startInjection(ProgressCallback progressCallback = nullptr,
                               CompletionCallback completionCallback = nullptr);
            
            bool startInjectionAsync(ProgressCallback progressCallback = nullptr,
                                    CompletionCallback completionCallback = nullptr);
            
            void stopInjection();
            
            // Monitoring
            bool startContinuousMonitoring(MonitoringCallback monitoringCallback = nullptr);
            void stopContinuousMonitoring();
            bool isMonitoringActive() const { return m_monitoring.load(); }
            
            // Status and results
            bool isInjectionActive() const { return m_injectionActive.load(); }
            InjectionStage getCurrentStage() const { return m_currentStage; }
            InjectionResult getLastResult() const { return m_lastResult; }
            std::string getLastError() const { return m_lastError; }
            
            // Component access
            FridaGadgetManager* getGadgetManager() const { return m_gadgetManager.get(); }
            ObjectionPatcher* getPatcher() const { return m_patcher.get(); }
            
            // Utility methods
            static bool isNonRootInjectionSupported();
            static std::vector<std::string> getRequiredPermissions();
            static bool validateSystemRequirements();
            
        private:
            // Prevent copying
            NonRootInjector(const NonRootInjector&) = delete;
            NonRootInjector& operator=(const NonRootInjector&) = delete;
        };
        
        /**
         * Singleton manager for non-root injection operations
         */
        class NonRootInjectorManager {
        private:
            static std::unique_ptr<NonRootInjector> s_instance;
            static std::mutex s_instanceMutex;
            
        public:
            static NonRootInjector* getInstance();
            static bool createInstance(const NonRootInjector::InjectionConfig& config);
            static void destroyInstance();
            
            // Quick injection methods
            static bool quickInject(const std::string& packageName, const std::string& bearToken);
            static bool isPackageInjectable(const std::string& packageName);
        };
        
        /**
         * Injection workflow utilities
         */
        namespace InjectionUtils {
            std::string stageToString(NonRootInjector::InjectionStage stage);
            bool isStageComplete(NonRootInjector::InjectionStage stage);
            bool isStageError(NonRootInjector::InjectionStage stage);
            int getStageProgress(NonRootInjector::InjectionStage stage);
            std::vector<NonRootInjector::InjectionStage> getWorkflowStages();
        }
        
    } // namespace NonRoot
} // namespace BearLoader

#endif // BEARMOD_NONROOT_INJECTOR_H
