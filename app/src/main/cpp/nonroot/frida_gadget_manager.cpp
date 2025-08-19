#include "frida_gadget_manager.h"
#include <fstream>
#include <sstream>
#include <thread>
#include <chrono>
#include <algorithm>
#include <cstdlib>
#include <unistd.h>
#include <dlfcn.h>
#include <sys/stat.h>

namespace BearLoader {
    namespace NonRoot {
        
        // Static member definitions
        std::unique_ptr<FridaGadgetManager> GadgetManagerInstance::s_instance = nullptr;
        std::mutex GadgetManagerInstance::s_instanceMutex;
        
        FridaGadgetManager::FridaGadgetManager() {
            m_lastError = "";
            m_lastResult = {};
        }
        
        FridaGadgetManager::~FridaGadgetManager() {
            shutdown();
        }
        
        bool FridaGadgetManager::initialize(JNIEnv* env, jobject context, const GadgetConfig& config) {
            std::lock_guard<std::mutex> lock(m_operationMutex);
            
            if (m_initialized.load()) {
                return true;
            }
            
            try {
                // Store JNI environment and context
                env->GetJavaVM(&m_jvm);
                m_context = env->NewGlobalRef(context);
                m_config = config;
                
                // Initialize enhanced security components
                if (config.enableGadgetDetection) {
                    if (!initializeGadgetDetection()) {
                        m_lastError = "Failed to initialize gadget detection";
                        return false;
                    }
                }
                
                if (config.enableMemoryProtector) {
                    if (!initializeMemoryProtection()) {
                        m_lastError = "Failed to initialize memory protection";
                        return false;
                    }
                }
                
                // Validate environment if enabled
                if (config.validateEnvironment) {
                    if (!validateSecurityEnvironment()) {
                        m_lastError = "Environment security validation failed";
                        return false;
                    }
                }
                
                // Authenticate with KeyAuth
                if (!config.keyAuthToken.empty() && !config.bearToken.empty()) {
                    if (!authenticateWithKeyAuth(config.bearToken)) {
                        m_lastError = "KeyAuth authentication failed";
                        return false;
                    }
                }
                
                // Prepare obfuscated gadget
                if (!prepareObfuscatedGadget()) {
                    m_lastError = "Failed to prepare obfuscated gadget";
                    return false;
                }
                
                // Create secure configuration
                if (!createSecureConfig()) {
                    m_lastError = "Failed to create secure configuration";
                    return false;
                }
                
                m_initialized = true;
                return true;
                
            } catch (const std::exception& e) {
                m_lastError = "Initialization exception: " + std::string(e.what());
                return false;
            }
        }
        
        void FridaGadgetManager::shutdown() {
            std::lock_guard<std::mutex> lock(m_operationMutex);
            
            if (!m_initialized.load()) {
                return;
            }
            
            // Stop any active injection
            m_injectionActive = false;
            
            // Cleanup temporary files
            cleanupTemporaryFiles();
            
            // Release JNI resources
            if (m_jvm && m_context) {
                JNIEnv* env;
                if (m_jvm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) == JNI_OK) {
                    env->DeleteGlobalRef(m_context);
                }
                m_context = nullptr;
            }
            
            // Shutdown security components
            shutdownSecurityComponents();
            
            m_initialized = false;
        }
        
        bool FridaGadgetManager::injectToRunningProcess(const std::string& packageName,
                                                       ProgressCallback progressCallback,
                                                       CompletionCallback completionCallback) {
            
            if (!m_initialized.load()) {
                m_lastError = "Manager not initialized";
                if (completionCallback) {
                    completionCallback(false, m_lastError);
                }
                return false;
            }
            
            std::lock_guard<std::mutex> lock(m_operationMutex);
            
            try {
                auto startTime = std::chrono::steady_clock::now();
                
                // Report progress
                if (progressCallback) {
                    progressCallback(10, "Starting injection process");
                }
                
                // Validate target package
                if (!validateTargetPackage(packageName)) {
                    m_lastError = "Target package validation failed";
                    if (completionCallback) {
                        completionCallback(false, m_lastError);
                    }
                    return false;
                }
                
                if (progressCallback) {
                    progressCallback(25, "Target package validated");
                }
                
                // Perform security checks
                if (!performSecurityChecks()) {
                    m_lastError = "Security checks failed";
                    if (completionCallback) {
                        completionCallback(false, m_lastError);
                    }
                    return false;
                }
                
                if (progressCallback) {
                    progressCallback(40, "Security checks passed");
                }
                
                // Enable anti-detection measures
                if (m_config.enableAntiDetection) {
                    if (!enableAntiDetectionMeasures()) {
                        m_lastError = "Failed to enable anti-detection measures";
                        if (completionCallback) {
                            completionCallback(false, m_lastError);
                        }
                        return false;
                    }
                }
                
                if (progressCallback) {
                    progressCallback(60, "Anti-detection measures enabled");
                }
                
                // Load and execute scripts
                if (!loadAndExecuteScripts()) {
                    m_lastError = "Failed to load and execute scripts";
                    if (completionCallback) {
                        completionCallback(false, m_lastError);
                    }
                    return false;
                }
                
                if (progressCallback) {
                    progressCallback(80, "Scripts loaded and executed");
                }
                
                // Verify injection success
                if (!verifyInjectionSuccess()) {
                    m_lastError = "Injection verification failed";
                    if (completionCallback) {
                        completionCallback(false, m_lastError);
                    }
                    return false;
                }
                
                // Calculate injection time
                auto endTime = std::chrono::steady_clock::now();
                auto injectionTime = std::chrono::duration_cast<std::chrono::milliseconds>(
                    endTime - startTime).count();
                
                // Update injection result
                m_lastResult.success = true;
                m_lastResult.message = "Injection completed successfully";
                m_lastResult.injectedProcessId = packageName;
                m_lastResult.injectionTime = injectionTime;
                m_lastResult.gadgetPath = m_obfuscatedGadgetPath;
                m_lastResult.securityLevel = static_cast<int>(m_config.security);
                m_lastResult.antiDetectionActive = m_config.enableAntiDetection;
                
                m_injectionActive = true;
                
                if (progressCallback) {
                    progressCallback(100, "Injection completed successfully");
                }
                
                if (completionCallback) {
                    completionCallback(true, "Injection completed successfully");
                }
                
                return true;
                
            } catch (const std::exception& e) {
                m_lastError = "Injection exception: " + std::string(e.what());
                if (completionCallback) {
                    completionCallback(false, m_lastError);
                }
                return false;
            }
        }
        
        bool FridaGadgetManager::authenticateWithKeyAuth(const std::string& bearToken) {
            try {
                // Validate BearToken format
                if (bearToken.empty() || bearToken.length() < 32) {
                    m_lastError = "Invalid BearToken format";
                    return false;
                }
                
                // Store validated token
                m_config.bearToken = bearToken;
                
                // TODO: Implement actual KeyAuth API validation
                // This should integrate with your existing KeyAuth system
                // For now, we'll perform basic validation
                
                return validateBearToken(bearToken);
                
            } catch (const std::exception& e) {
                m_lastError = "KeyAuth authentication exception: " + std::string(e.what());
                return false;
            }
        }
        
        bool FridaGadgetManager::validateLicenseForNonRoot() {
            try {
                if (m_config.bearToken.empty()) {
                    return false;
                }
                
                // TODO: Implement license validation logic
                // This should check if the license includes non-root features
                
                return true;
                
            } catch (const std::exception& e) {
                m_lastError = "License validation exception: " + std::string(e.what());
                return false;
            }
        }
        
        bool FridaGadgetManager::initializeGadgetDetection() {
            try {
                using namespace EnhancedAntiHook;
                
                GadgetDetector::DetectorConfig detectorConfig;
                detectorConfig.enableLibraryScan = true;
                detectorConfig.enableProcessScan = true;
                detectorConfig.enableMemoryScan = true;
                detectorConfig.enableGadgetSpecific = true;
                detectorConfig.exitOnDetection = false; // Don't exit, just report
                detectorConfig.maxThreatLevel = 8;
                
                if (!GadgetDetectorManager::createInstance(detectorConfig)) {
                    return false;
                }
                
                auto* detector = GadgetDetectorManager::getInstance();
                if (!detector) {
                    return false;
                }
                
                // Add detection callback
                detector->addDetectionCallback([this](const GadgetDetector::DetectionResult& result) {
                    if (result.detected && result.threatLevel >= 7) {
                        m_lastError = "High threat detected: " + result.details;
                        // Could trigger additional security measures here
                    }
                });
                
                return detector->startDetection();
                
            } catch (const std::exception& e) {
                m_lastError = "Gadget detection initialization failed: " + std::string(e.what());
                return false;
            }
        }
        
        bool FridaGadgetManager::initializeMemoryProtection() {
            try {
                using namespace EnhancedAntiHook;
                
                MemoryProtector::ProtectorConfig protectorConfig;
                protectorConfig.enableChecksumMonitoring = true;
                protectorConfig.enableHookDetection = true;
                protectorConfig.enableAntiDebug = true;
                protectorConfig.protectKeyFunctions = true;
                protectorConfig.exitOnTampering = false; // Don't exit, just report
                
                if (!MemoryProtectorManager::createInstance(protectorConfig)) {
                    return false;
                }
                
                auto* protector = MemoryProtectorManager::getInstance();
                if (!protector) {
                    return false;
                }
                
                // Add tampering callback
                protector->addTamperingCallback([this](const MemoryProtector::MemoryRegion& region, 
                                                      const std::string& details) {
                    m_lastError = "Memory tampering detected in " + region.name + ": " + details;
                    // Could trigger additional security measures here
                });
                
                return protector->startProtection();
                
            } catch (const std::exception& e) {
                m_lastError = "Memory protection initialization failed: " + std::string(e.what());
                return false;
            }
        }
        
        bool FridaGadgetManager::validateSecurityEnvironment() {
            try {
                // Check if we're running on a real device
                if (!isNonRootDevice()) {
                    m_lastError = "Not running on a non-root device";
                    return false;
                }
                
                // Check for analysis environment
                if (detectAnalysisEnvironment()) {
                    m_lastError = "Analysis environment detected";
                    return false;
                }
                
                // Check for debugging
                if (detectDebugging()) {
                    m_lastError = "Debugging detected";
                    return false;
                }
                
                return true;
                
            } catch (const std::exception& e) {
                m_lastError = "Environment validation exception: " + std::string(e.what());
                return false;
            }
        }
        
        // Static utility methods
        bool FridaGadgetManager::isNonRootDevice() {
            // Check for root indicators
            std::vector<std::string> rootPaths = {
                "/system/app/Superuser.apk",
                "/system/xbin/su",
                "/system/bin/su",
                "/sbin/su",
                "/system/su",
                "/system/bin/.ext/.su",
                "/system/xbin/daemonsu"
            };
            
            for (const auto& path : rootPaths) {
                struct stat buffer;
                if (stat(path.c_str(), &buffer) == 0) {
                    return false; // Root detected
                }
            }
            
            return true; // No root detected
        }
        
        std::vector<std::string> FridaGadgetManager::getSupportedArchitectures() {
            return {"arm64-v8a", "armeabi-v7a"};
        }
        
        bool FridaGadgetManager::checkFridaGadgetCompatibility() {
            // Check if the current architecture is supported
            auto supportedArchs = getSupportedArchitectures();
            
            // Get current architecture (simplified check)
            #if defined(__aarch64__)
                std::string currentArch = "arm64-v8a";
            #elif defined(__arm__)
                std::string currentArch = "armeabi-v7a";
            #else
                return false; // Unsupported architecture
            #endif
            
            return std::find(supportedArchs.begin(), supportedArchs.end(), currentArch) != supportedArchs.end();
        }
        
        // GadgetManagerInstance implementation
        FridaGadgetManager* GadgetManagerInstance::getInstance() {
            std::lock_guard<std::mutex> lock(s_instanceMutex);
            return s_instance.get();
        }
        
        bool GadgetManagerInstance::createInstance(JNIEnv* env, jobject context, const FridaGadgetManager::GadgetConfig& config) {
            std::lock_guard<std::mutex> lock(s_instanceMutex);
            
            if (s_instance) {
                return false;  // Already exists
            }
            
            s_instance = std::make_unique<FridaGadgetManager>();
            return s_instance->initialize(env, context, config);
        }
        
        void GadgetManagerInstance::destroyInstance() {
            std::lock_guard<std::mutex> lock(s_instanceMutex);
            s_instance.reset();
        }
        
    } // namespace NonRoot
} // namespace BearLoader

// JNI Bridge Implementation
extern "C" {

    JNIEXPORT jboolean JNICALL
    Java_com_bearmod_plugin_NonRootManager_initializeGadgetManager(JNIEnv *env, jobject thiz,
                                                                   jstring keyAuthToken, jstring bearToken) {
        try {
            // Convert Java strings to C++ strings
            const char* keyAuthStr = env->GetStringUTFChars(keyAuthToken, nullptr);
            const char* bearTokenStr = env->GetStringUTFChars(bearToken, nullptr);

            if (!keyAuthStr || !bearTokenStr) {
                return JNI_FALSE;
            }

            // Create gadget configuration
            BearLoader::NonRoot::FridaGadgetManager::GadgetConfig config;
            config.keyAuthToken = std::string(keyAuthStr);
            config.bearToken = std::string(bearTokenStr);
            config.mode = BearLoader::NonRoot::FridaGadgetManager::InjectionMode::STEALTH;
            config.security = BearLoader::NonRoot::FridaGadgetManager::SecurityLevel::MAXIMUM;
            config.enableAntiDetection = true;
            config.enableMemoryProtection = true;
            config.enableGadgetDetection = true;
            config.enableMemoryProtector = true;
            config.validateEnvironment = true;

            // Create and initialize gadget manager instance
            bool result = BearLoader::NonRoot::GadgetManagerInstance::createInstance(env, thiz, config);

            // Release Java strings
            env->ReleaseStringUTFChars(keyAuthToken, keyAuthStr);
            env->ReleaseStringUTFChars(bearToken, bearTokenStr);

            return result ? JNI_TRUE : JNI_FALSE;

        } catch (const std::exception& e) {
            // Log error if logging is available
            return JNI_FALSE;
        }
    }

    JNIEXPORT jboolean JNICALL
    Java_com_bearmod_plugin_NonRootManager_injectToPackage(JNIEnv *env, jobject thiz,
                                                           jstring packageName) {
        try {
            auto* manager = BearLoader::NonRoot::GadgetManagerInstance::getInstance();
            if (!manager) {
                return JNI_FALSE;
            }

            const char* packageStr = env->GetStringUTFChars(packageName, nullptr);
            if (!packageStr) {
                return JNI_FALSE;
            }

            std::string package(packageStr);
            env->ReleaseStringUTFChars(packageName, packageStr);

            // Start injection to running process
            bool result = manager->injectToRunningProcess(package);

            return result ? JNI_TRUE : JNI_FALSE;

        } catch (const std::exception& e) {
            return JNI_FALSE;
        }
    }

    JNIEXPORT jstring JNICALL
    Java_com_bearmod_plugin_NonRootManager_getInjectionStatusNative(JNIEnv *env, jobject thiz) {
        try {
            auto* manager = BearLoader::NonRoot::GadgetManagerInstance::getInstance();
            if (!manager) {
                return env->NewStringUTF("Manager not initialized");
            }

            std::string status;
            if (manager->isInitialized()) {
                if (manager->isInjectionActive()) {
                    status = "Injection active";
                } else {
                    status = "Initialized, ready for injection";
                }
            } else {
                status = "Not initialized";
            }

            auto result = manager->getLastInjectionResult();
            if (result.success) {
                status += " - Last injection: SUCCESS";
                status += " - Target: " + result.injectedProcessId;
                status += " - Time: " + std::to_string(result.injectionTime) + "ms";
            }

            return env->NewStringUTF(status.c_str());

        } catch (const std::exception& e) {
            return env->NewStringUTF(("Error: " + std::string(e.what())).c_str());
        }
    }

    JNIEXPORT jstring JNICALL
    Java_com_bearmod_plugin_NonRootManager_getLastErrorNative(JNIEnv *env, jobject thiz) {
        try {
            auto* manager = BearLoader::NonRoot::GadgetManagerInstance::getInstance();
            if (!manager) {
                return env->NewStringUTF("Manager not initialized");
            }

            std::string error = manager->getLastError();
            return env->NewStringUTF(error.c_str());

        } catch (const std::exception& e) {
            return env->NewStringUTF(("Exception: " + std::string(e.what())).c_str());
        }
    }

    JNIEXPORT void JNICALL
    Java_com_bearmod_plugin_NonRootManager_shutdownGadgetManager(JNIEnv *env, jobject thiz) {
        try {
            BearLoader::NonRoot::GadgetManagerInstance::destroyInstance();
        } catch (const std::exception& e) {
            // Log error if logging is available
        }
    }

    JNIEXPORT jboolean JNICALL
    Java_com_bearmod_plugin_NonRootManager_isNonRootSupportedNative(JNIEnv *env, jclass clazz) {
        try {
            return BearLoader::NonRoot::FridaGadgetManager::isNonRootDevice() ? JNI_TRUE : JNI_FALSE;
        } catch (const std::exception& e) {
            return JNI_FALSE;
        }
    }

    JNIEXPORT jboolean JNICALL
    Java_com_bearmod_plugin_NonRootManager_validateSystemRequirementsNative(JNIEnv *env, jclass clazz) {
        try {
            // Check Frida Gadget compatibility
            if (!BearLoader::NonRoot::FridaGadgetManager::checkFridaGadgetCompatibility()) {
                return JNI_FALSE;
            }

            // Check if running on non-root device
            if (!BearLoader::NonRoot::FridaGadgetManager::isNonRootDevice()) {
                return JNI_FALSE;
            }

            // Additional system requirement checks can be added here

            return JNI_TRUE;

        } catch (const std::exception& e) {
            return JNI_FALSE;
        }
    }

} // extern "C"
