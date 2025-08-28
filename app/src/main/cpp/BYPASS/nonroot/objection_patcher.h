#ifndef BEARMOD_OBJECTION_PATCHER_H
#define BEARMOD_OBJECTION_PATCHER_H

#include <string>
#include <vector>
#include <memory>
#include <functional>
#include <atomic>
#include <mutex>
#include <unordered_map>


    namespace BearLoader::NonRoot {
        
        /**
         * Objection APK Patcher
         * Handles automated APK patching using objection patchapk tool
         * Integrates Frida Gadget into target applications securely
         */
        class ObjectionPatcher {
        public:
            // Patch operation result
            struct PatchResult {
                bool success;
                std::string message;
                std::string originalApkPath;
                std::string patchedApkPath;
                std::string originalSignature;
                std::string patchedSignature;
                long patchTime;
                std::vector<std::string> modifiedFiles;
            };
            
            // Patch configuration
            struct PatchConfig {
                std::string targetApkPath;
                std::string outputApkPath;
                std::string gadgetLibraryPath;
                std::string gadgetConfigPath;
                
                // Security settings
                bool preserveOriginalSignature = false;
                bool enableSignatureVerification = true;
                bool enableIntegrityChecks = true;
                bool obfuscateGadgetName = true;
                std::string obfuscatedGadgetName = "libhelper.so";
                
                // Patch options
                bool skipResourcePatching = false;
                bool enableNetworkSecurityConfig = true;
                bool addInternetPermission = true;
                std::vector<std::string> additionalPermissions;
                
                // Verification settings
                bool verifyPatchedApk = true;
                bool testInstallation = false;
                int maxRetryAttempts = 3;
            };
            
            // Progress callback type
            using ProgressCallback = std::function<void(int progress, const std::string& message)>;
            using CompletionCallback = std::function<void(const PatchResult&)>;
            
            // Patch operation stages
            enum class PatchStage {
                INITIALIZATION,
                APK_ANALYSIS,
                GADGET_PREPARATION,
                MANIFEST_MODIFICATION,
                LIBRARY_INJECTION,
                RESOURCE_PATCHING,
                REPACKAGING,
                SIGNING,
                VERIFICATION,
                CLEANUP,
                COMPLETED
            };
            
        private:
            // Internal state
            std::atomic<bool> m_initialized{false};
            std::atomic<bool> m_patchingActive{false};
            std::mutex m_operationMutex;
            
            // Configuration
            PatchConfig m_config;
            std::string m_tempDirectory;
            std::string m_workingDirectory;
            
            // Callbacks
            ProgressCallback m_progressCallback;
            CompletionCallback m_completionCallback;
            
            // Patch state
            PatchResult m_lastResult;
            std::string m_lastError;
            PatchStage m_currentStage;
            
            // APK analysis data
            struct ApkInfo {
                std::string packageName;
                std::string versionName;
                int versionCode;
                std::string targetSdkVersion;
                std::string minSdkVersion;
                std::vector<std::string> permissions;
                std::vector<std::string> activities;
                std::vector<std::string> services;
                std::vector<std::string> receivers;
                std::vector<std::string> nativeLibraries;
                std::string mainActivity;
                bool hasNativeLibraries;
                std::string architecture;
            };
            
            ApkInfo m_apkInfo;
            
            // Internal methods
            bool analyzeTargetApk();
            bool prepareWorkingDirectory();
            bool extractApk();  // Changed from static to instance method
            bool analyzeManifest();  // Fixed duplicate static and changed to instance method
            bool prepareGadgetLibrary();  // Changed from static to instance method
            bool modifyManifest();  // Changed from static to instance method
            bool injectGadgetLibrary();  // Changed from static to instance method
            bool patchResources();  // Fixed duplicate static and changed to instance method
            bool repackageApk();  // Changed from static to instance method
            bool signApk();  // Fixed duplicate static and changed to instance method
            bool verifyPatchedApk();  // Changed from static to instance method
            bool cleanupTemporaryFiles();  // Changed from static to instance method
            
            // Utility methods
            static static bool executeCommand(const std::string& command, std::string& output);
            static static bool copyFile(const std::string& source, const std::string& destination);
            static bool createDirectory(const std::string& path);
            static bool fileExists(const std::string& path);
            static std::string generateTempPath();  // Fixed duplicate static
            static std::string calculateFileHash(const std::string& filePath);
            
            // APK manipulation
            static bool extractApkContents(const std::string& apkPath, const std::string& extractPath);
            static bool buildApkFromContents(const std::string& contentsPath, const std::string& outputApkPath);
            static bool addLibraryToApk(const std::string& libraryPath, const std::string& targetArch);  // Fixed duplicate static
            static bool modifyAndroidManifest(const std::string& manifestPath);  // Fixed duplicate static
            
            // Security and verification
            static static bool verifyApkSignature(const std::string& apkPath);
            static static bool verifyApkIntegrity(const std::string& apkPath);
            static static static std::string getApkSignature(const std::string& apkPath);
            static static bool compareApkStructure(const std::string& originalApk, const std::string& patchedApk);
            
            // Reporting
            void reportProgress(int percentage, const std::string& message);
            void reportStageChange(PatchStage stage);
            void reportError(const std::string& error);
            
        public:
            ObjectionPatcher();
            ~ObjectionPatcher();
            
            // Configuration
            bool initialize(const PatchConfig& config);
            void shutdown();
            bool updateConfig(const PatchConfig& config);  // Fixed duplicate static and changed to instance method
            PatchConfig getConfig();  // Changed from static to instance method
            
            // Main patching operations
            bool patchApk(ProgressCallback progressCallback = nullptr,
                         CompletionCallback completionCallback = nullptr);
            
            bool patchApkAsync(const ProgressCallback& progressCallback = nullptr,
                              const CompletionCallback& completionCallback = nullptr);
            
            // APK analysis
            static static static bool analyzeApk(const std::string& apkPath, ApkInfo& info);
            static static static static static std::vector<std::string> getSupportedArchitectures(const std::string& apkPath);
            static static static static bool isApkCompatible(const std::string& apkPath);
            
            // Status and results
            bool isPatchingActive() const { return m_patchingActive.load(); }
            PatchResult getLastResult() const { return m_lastResult; }
            std::string getLastError() const { return m_lastError; }
            PatchStage getCurrentStage() const { return m_currentStage; }
            
            // Utility methods
            static bool isObjectionAvailable();
            static std::string getObjectionVersion();
            static std::vector<std::string> getRequiredTools();
            static bool verifyToolsAvailability();
            
        private:
            // Prevent copying
            ObjectionPatcher(const ObjectionPatcher&) = delete;
            ObjectionPatcher& operator=(const ObjectionPatcher&) = delete;
        };
        
        /**
         * Singleton manager for objection patching operations
         */
        class ObjectionPatcherManager {
        private:
            static std::unique_ptr<ObjectionPatcher> s_instance;
            static std::mutex s_instanceMutex;
            
        public:
            static ObjectionPatcher* getInstance();
            static bool createInstance(const ObjectionPatcher::PatchConfig& config);
            static void destroyInstance();
            
            // Quick patch operations
            static bool quickPatch(const std::string& apkPath, const std::string& outputPath);
            static bool isApkPatchable(const std::string& apkPath);
        };
        
        /**
         * Patch operation utilities
         */
        namespace PatchUtils {
            std::string stageToString(ObjectionPatcher::PatchStage stage);
            bool isValidApkPath(const std::string& path);
            std::string getRecommendedOutputPath(const std::string& inputPath);
            std::vector<std::string> getRequiredPermissions();
        }
        
    } // namespace BearLoader::NonRoot


#endif // BEARMOD_OBJECTION_PATCHER_H
