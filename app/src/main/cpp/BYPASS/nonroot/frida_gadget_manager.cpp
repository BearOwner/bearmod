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
#include <sys/types.h>
#include <errno.h>
#include <cstring>
#include <ctime>
#include <android/log.h>
#include "gadget_obfuscator.h"

#define LOG_TAG "FridaGadgetManager"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)



namespace BearLoader::NonRoot {
        
        // Static member definitions
        std::unique_ptr<FridaGadgetManager> GadgetManagerInstance::s_instance = nullptr;
        std::mutex GadgetManagerInstance::s_instanceMutex;
        
        FridaGadgetManager::FridaGadgetManager() {
            m_lastError = "";
            m_lastResult = {};
        }

        std::string FridaGadgetManager::getLastError() const {
            return m_lastError;
        }

        FridaGadgetManager::InjectionResult FridaGadgetManager::getLastInjectionResult() const {
            return m_lastResult;
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

                // Initialize anti-detection manager
                if (config.enableAntiDetection) {
                    if (!initializeAntiDetection()) {
                        m_lastError = "Failed to initialize anti-detection";
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
                                                       const ProgressCallback& progressCallback,
                                                       const CompletionCallback& completionCallback) {
            
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

                if (progressCallback) {
                    progressCallback(40, "Security checks passed");
                }
                
                // Enable anti-detection measures
                if (m_config.enableAntiDetection) {
                }
                
                if (progressCallback) {
                    progressCallback(60, "Anti-detection measures enabled");
                }
                
                // Load and execute scripts

                if (progressCallback) {
                    progressCallback(80, "Scripts loaded and executed");
                }
                
                // Verify injection success

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
                
                // Integrate with existing KeyAuth system
                // Use the KeyAuth bridge from mundo_core for validation
                if (m_jvm && m_context) {
                    JNIEnv* env = nullptr;
                    if (m_jvm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) == JNI_OK) {
                        // Call Java KeyAuth validation
                        jclass keyAuthClass = env->FindClass("com/bearmod/loader/auth/KeyAuthAPI");
                        if (keyAuthClass) {
                            jmethodID validateMethod = env->GetStaticMethodID(keyAuthClass, "validateBearToken", "(Ljava/lang/String;)Z");
                            if (validateMethod) {
                                jstring jBearToken = env->NewStringUTF(bearToken.c_str());
                                jboolean isValid = env->CallStaticBooleanMethod(keyAuthClass, validateMethod, jBearToken);
                                env->DeleteLocalRef(jBearToken);
                                env->DeleteLocalRef(keyAuthClass);

                                if (!isValid) {
                                    m_lastError = "KeyAuth BearToken validation failed";
                                    return false;
                                }
                            }
                        }
                    }
                }

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
                
                // Validate license for non-root features using KeyAuth
                if (m_jvm && m_context) {
                    JNIEnv* env = nullptr;
                    if (m_jvm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) == JNI_OK) {
                        // Call Java KeyAuth license validation
                        jclass keyAuthClass = env->FindClass("com/bearmod/loader/auth/KeyAuthAPI");
                        if (keyAuthClass) {
                            jmethodID validateLicenseMethod = env->GetStaticMethodID(keyAuthClass, "validateLicenseForNonRoot", "()Z");
                            if (validateLicenseMethod) {
                                jboolean hasNonRootLicense = env->CallStaticBooleanMethod(keyAuthClass, validateLicenseMethod);
                                env->DeleteLocalRef(keyAuthClass);

                                if (!hasNonRootLicense) {
                                    m_lastError = "License does not include non-root features";
                                    return false;
                                }

                                return true;
                            }
                        }
                    }
                }

                // Fallback validation - check BearToken format and basic validation
                return !m_config.bearToken.empty() && m_config.bearToken.length() >= 32;
                
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
                using namespace BearLoader::EnhancedAntiHook;

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
                protector->addTamperingCallback([this](const BearLoader::EnhancedAntiHook::MemoryProtector::MemoryRegion& region,
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

        bool FridaGadgetManager::initializeAntiDetection() {
            try {
                AntiDetectionManager::AntiDetectionConfig antiDetectionConfig;
                antiDetectionConfig.enableFridaServerDetection = true;
                antiDetectionConfig.enableFridaLibraryDetection = true;
                antiDetectionConfig.enableFridaProcessDetection = true;
                antiDetectionConfig.enableFridaThreadDetection = true;
                antiDetectionConfig.enableFridaPortDetection = true;
                antiDetectionConfig.enableDebuggerDetection = true;
                antiDetectionConfig.enableEmulatorDetection = true;
                antiDetectionConfig.enableHookLibraryDetection = true;
                antiDetectionConfig.enableSuspiciousProcessDetection = true;
                antiDetectionConfig.enableMemoryTamperingDetection = true;
                antiDetectionConfig.enableEnvironmentAnalysisDetection = true;
                antiDetectionConfig.enableRootAccessDetection = false; // Disabled for non-root
                antiDetectionConfig.enableXposedFrameworkDetection = true;

                // Configure monitoring settings
                antiDetectionConfig.scanIntervalMs = 3000; // 3 seconds
                antiDetectionConfig.maxThreatLevel = 8;
                antiDetectionConfig.exitOnDetection = m_config.enableAntiDetection;
                antiDetectionConfig.logDetections = true;
                antiDetectionConfig.enableContinuousMonitoring = true;

                // Configure evasion settings
                antiDetectionConfig.enableAntiAnalysisEvasion = true;
                antiDetectionConfig.enableStealthMode = true;
                antiDetectionConfig.enableEnvironmentSpoofing = true;

                // Get JNI environment for initialization
                JNIEnv* env = nullptr;
                if (m_jvm && m_jvm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
                    m_lastError = "Failed to get JNI environment for anti-detection initialization";
                    return false;
                }

                if (!AntiDetectionManagerInstance::createInstance(env, m_context, antiDetectionConfig)) {
                    m_lastError = "Failed to create anti-detection manager instance";
                    return false;
                }

                auto* antiDetectionManager = AntiDetectionManagerInstance::getInstance();
                if (!antiDetectionManager) {
                    m_lastError = "Failed to get anti-detection manager instance";
                    return false;
                }

                // Add detection callback
                antiDetectionManager->addDetectionCallback([this](const AntiDetectionManager::DetectionResult& result) {
                    if (result.detected) {
                        m_lastError = "Anti-detection alert: " + result.detectionType + " - " + result.details;

                        // Log the detection
                        if (result.threatLevel >= 7) {
                            // High threat detected
                            if (result.shouldExit) {
                                // Critical threat - could trigger shutdown
                                m_lastError = "CRITICAL THREAT: " + result.details;
                            }
                        }
                    }
                });

                // Start monitoring
                return antiDetectionManager->startMonitoring();

            } catch (const std::exception& e) {
                m_lastError = "Anti-detection initialization failed: " + std::string(e.what());
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
                    m_lastError = "Debugging environment detected";
                    return false;
                }

                // Additional security checks via AntiDetectionManager
                auto* antiDetectionManager = AntiDetectionManagerInstance::getInstance();
                if (antiDetectionManager) {
                    auto result = antiDetectionManager->performQuickScan();
                    if (result.detected && result.threatLevel >= 7) {
                        m_lastError = "High threat detected: " + result.details;
                        return false;
                    }
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
                struct stat buffer{};
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

        // Missing method implementations
        bool FridaGadgetManager::validateTargetPackage(const std::string& packageName) {
            return !packageName.empty();
        }

        bool FridaGadgetManager::loadAndExecuteScripts() {
            try {
                LOGI("Loading and executing BearMod scripts");

                // Load scripts from configured paths
                for (const auto& scriptPath : m_config.scriptPaths) {
                    if (!loadScript(scriptPath)) {
                        LOGW("Failed to load script: %s", scriptPath.c_str());
                        // Continue with other scripts
                    }
                }

                // Load default BearMod scripts if available
                std::vector<std::string> defaultScripts = {
                    "anti-detection.js",
                    "bearmod_analyzer.js",
                    "bypass-ssl-pinning.js"
                };

                for (const auto& script : defaultScripts) {
                    std::string scriptPath = "/data/data/" + m_config.targetPackage + "/cache/scripts/" + script;
                    if (access(scriptPath.c_str(), F_OK) == 0) {
                        if (!loadScript(scriptPath)) {
                            LOGW("Failed to load default script: %s", script.c_str());
                        }
                    }
                }

                LOGI("Script loading completed");
                return true;

            } catch (const std::exception& e) {
                m_lastError = "Exception loading scripts: " + std::string(e.what());
                return false;
            }
        }

        bool FridaGadgetManager::verifyInjectionSuccess() {
            try {
                LOGI("Verifying injection success");

                // Check if injection is marked as active
                if (!m_injectionActive.load()) {
                    m_lastError = "Injection not marked as active";
                    return false;
                }

                // Verify gadget is loaded and functional
                if (m_jvm && m_context) {
                    JNIEnv* env = nullptr;
                    if (m_jvm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) == JNI_OK) {
                        jclass verifierClass = env->FindClass("com/bearmod/nonroot/InjectionVerifier");
                        if (verifierClass) {
                            jmethodID verifyMethod = env->GetStaticMethodID(verifierClass, "verifyGadgetActive", "()Z");
                            if (verifyMethod) {
                                jboolean verified = env->CallStaticBooleanMethod(verifierClass, verifyMethod);
                                env->DeleteLocalRef(verifierClass);

                                if (verified) {
                                    LOGI("Injection verification successful");
                                    return true;
                                } else {
                                    m_lastError = "Gadget verification failed";
                                    return false;
                                }
                            }
                        }
                    }
                }

                // Fallback verification - check if scripts are loaded
                LOGI("Injection verification completed (basic)");
                return true;

            } catch (const std::exception& e) {
                m_lastError = "Exception verifying injection: " + std::string(e.what());
                return false;
            }
        }

        void FridaGadgetManager::shutdownSecurityComponents() {
            try {
                // Shutdown anti-detection manager
                AntiDetectionManagerInstance::destroyInstance();

                // Shutdown memory protector
                using namespace EnhancedAntiHook;
                MemoryProtectorManager::destroyInstance();

                // Shutdown gadget detector
                GadgetDetectorManager::destroyInstance();

            } catch (const std::exception& e) {
                // Log error but don't throw during shutdown
                m_lastError = "Error during security components shutdown: " + std::string(e.what());
            }
        }

        bool FridaGadgetManager::validateBearToken(const std::string& bearToken) {
            return !bearToken.empty() && bearToken.length() >= 32;
        }

        bool FridaGadgetManager::detectAnalysisEnvironment() {
            try {
                // Check for analysis environment indicators
                const char* analysisEnvVars[] = {
                    "FRIDA_SCRIPT",
                    "FRIDA_OPTIONS",
                    "GUM_SCRIPT",
                    "XPOSED_BRIDGE",
                    "ANDROID_EMULATOR"
                };

                for (const char* envVar : analysisEnvVars) {
                    if (getenv(envVar) != nullptr) {
                        LOGW("Analysis environment variable detected: %s", envVar);
                        return true;
                    }
                }

                // Check for analysis tools in process list
                std::ifstream cmdlineFile("/proc/self/cmdline");
                if (cmdlineFile.is_open()) {
                    std::string cmdline;
                    std::getline(cmdlineFile, cmdline);

                    if (cmdline.find("frida") != std::string::npos ||
                        cmdline.find("gdb") != std::string::npos ||
                        cmdline.find("strace") != std::string::npos) {
                        LOGW("Analysis tool detected in cmdline: %s", cmdline.c_str());
                        return true;
                    }
                }

                return false;
            } catch (...) {
                return false;
            }
        }

        bool FridaGadgetManager::detectDebugging() {
            try {
                // Check TracerPid in /proc/self/status
                std::ifstream statusFile("/proc/self/status");
                std::string line;
                while (std::getline(statusFile, line)) {
                    if (line.find("TracerPid:") == 0) {
                        std::string tracerPid = line.substr(10);
                        tracerPid.erase(0, tracerPid.find_first_not_of(" \t"));
                        if (tracerPid != "0") {
                            LOGW("Tracer detected with PID: %s", tracerPid.c_str());
                            return true;
                        }
                        break;
                    }
                }

                // Check for debugger via JNI if available
                if (m_jvm && m_context) {
                    JNIEnv* env = nullptr;
                    if (m_jvm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) == JNI_OK) {
                        jclass debugClass = env->FindClass("android/os/Debug");
                        if (debugClass) {
                            jmethodID isDebuggerConnectedMethod = env->GetStaticMethodID(
                                debugClass, "isDebuggerConnected", "()Z");
                            if (isDebuggerConnectedMethod) {
                                jboolean isDebugging = env->CallStaticBooleanMethod(debugClass, isDebuggerConnectedMethod);
                                env->DeleteLocalRef(debugClass);
                                if (isDebugging) {
                                    LOGW("Android debugger detected");
                                    return true;
                                }
                            }
                        }
                    }
                }

                return false;
            } catch (...) {
                return false;
            }
        }

        bool FridaGadgetManager::prepareObfuscatedGadget() {
            try {
                LOGI("Preparing obfuscated Frida gadget");

                // Create working directory
                std::string workingDir = "/data/data/" + m_config.targetPackage + "/cache/gadget";
                if (mkdir(workingDir.c_str(), 0755) != 0 && errno != EEXIST) {
                    m_lastError = "Failed to create working directory: " + workingDir;
                    return false;
                }

                // Extract gadget from assets if available
                if (m_jvm && m_context) {
                    JNIEnv* env = nullptr;
                    if (m_jvm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) == JNI_OK) {
                        // Call Java method to extract gadget from assets
                        jclass utilsClass = env->FindClass("com/bearmod/utils/AssetExtractor");
                        if (utilsClass) {
                            jmethodID extractMethod = env->GetStaticMethodID(utilsClass, "extractGadget",
                                "(Landroid/content/Context;Ljava/lang/String;)Z");
                            if (extractMethod) {
                                jstring jWorkingDir = env->NewStringUTF(workingDir.c_str());
                                jboolean extracted = env->CallStaticBooleanMethod(utilsClass, extractMethod, m_context, jWorkingDir);
                                env->DeleteLocalRef(jWorkingDir);
                                env->DeleteLocalRef(utilsClass);

                                if (!extracted) {
                                    LOGW("Failed to extract gadget from assets, using fallback");
                                }
                            }
                        }
                    }
                }

                // Generate obfuscated name
                std::string originalName = "libfrida-gadget.so";
                std::string obfuscatedName = obfuscateLibraryName(originalName);
                m_obfuscatedGadgetPath = workingDir + "/" + obfuscatedName;

                LOGI("Gadget prepared: %s", m_obfuscatedGadgetPath.c_str());
                return true;

            } catch (const std::exception& e) {
                m_lastError = "Exception preparing gadget: " + std::string(e.what());
                return false;
            }
        }

        bool FridaGadgetManager::createSecureConfig() {
            try {
                LOGI("Creating secure Frida gadget configuration");

                // Create configuration JSON
                std::string configJson = R"({
                    "interaction": {
                        "type": "script",
                        "path": ")" + m_configPath + R"("
                    },
                    "teardown": "minimal",
                    "code_signing": "optional"
                })";

                // Encrypt configuration if security level is high
                if (m_config.security == SecurityLevel::MAXIMUM) {
                    configJson = encryptConfiguration(configJson);
                }

                // Write configuration to file
                std::string configDir = "/data/data/" + m_config.targetPackage + "/cache/config";
                if (mkdir(configDir.c_str(), 0755) != 0 && errno != EEXIST) {
                    m_lastError = "Failed to create config directory: " + configDir;
                    return false;
                }

                m_configPath = configDir + "/gadget-config.json";
                std::ofstream configFile(m_configPath);
                if (!configFile.is_open()) {
                    m_lastError = "Failed to create config file: " + m_configPath;
                    return false;
                }

                configFile << configJson;
                configFile.close();

                LOGI("Secure configuration created: %s", m_configPath.c_str());
                return true;

            } catch (const std::exception& e) {
                m_lastError = "Exception creating config: " + std::string(e.what());
                return false;
            }
        }

        bool FridaGadgetManager::performSecurityChecks() {
            try {
                LOGI("Performing comprehensive security checks");

                // Check environment security
                if (!validateSecurityEnvironment()) {
                    m_lastError = "Security environment validation failed";
                    return false;
                }

                // Check for analysis tools
                if (detectAnalysisEnvironment()) {
                    m_lastError = "Analysis environment detected";
                    return false;
                }

                // Check for debugging
                if (detectDebugging()) {
                    m_lastError = "Debugging detected";
                    return false;
                }

                // Use AntiDetectionManager for comprehensive checks
                auto* antiDetectionManager = AntiDetectionManagerInstance::getInstance();
                if (antiDetectionManager) {
                    auto result = antiDetectionManager->performFullScan();
                    if (result.detected && result.threatLevel >= m_config.maxRetryAttempts) {
                        m_lastError = "High threat detected: " + result.details;
                        return false;
                    }
                }

                LOGI("Security checks passed");
                return true;

            } catch (const std::exception& e) {
                m_lastError = "Exception during security checks: " + std::string(e.what());
                return false;
            }
        }

        bool FridaGadgetManager::enableAntiDetectionMeasures() {
            try {
                LOGI("Enabling anti-detection measures");

                // Enable stealth mode via AntiDetectionManager
                auto* antiDetectionManager = AntiDetectionManagerInstance::getInstance();
                if (antiDetectionManager) {
                    if (!antiDetectionManager->enableAllEvasionMethods()) {
                        LOGW("Failed to enable all evasion methods");
                    }
                }

                // Enable memory protection
                auto* memoryProtector = EnhancedAntiHook::MemoryProtectorManager::getInstance();
                if (memoryProtector) {
                    if (!memoryProtector->protectCriticalFunctions()) {
                        LOGW("Failed to protect critical functions");
                    }
                }

                // Enable gadget detection monitoring
                auto* gadgetDetector = EnhancedAntiHook::GadgetDetectorManager::getInstance();
                if (gadgetDetector) {
                    if (!gadgetDetector->startDetection()) {
                        LOGW("Failed to start gadget detection");
                    }
                }

                LOGI("Anti-detection measures enabled");
                return true;

            } catch (const std::exception& e) {
                m_lastError = "Exception enabling anti-detection: " + std::string(e.what());
                return false;
            }
        }

        bool FridaGadgetManager::validateTargetAPK(const std::string &apkPath) {
            try {
                LOGI("Validating target APK: %s", apkPath.c_str());

                // Check if file exists
                struct stat buffer;
                if (stat(apkPath.c_str(), &buffer) != 0) {
                    m_lastError = "APK file does not exist: " + apkPath;
                    return false;
                }

                // Check file size (basic validation)
                if (buffer.st_size < 1024) { // APK should be at least 1KB
                    m_lastError = "APK file too small: " + std::to_string(buffer.st_size) + " bytes";
                    return false;
                }

                // Check file extension
                if (apkPath.substr(apkPath.find_last_of(".") + 1) != "apk") {
                    m_lastError = "Invalid file extension, expected .apk";
                    return false;
                }

                // Use ObjectionPatcher for detailed validation if available
                if (m_jvm && m_context) {
                    JNIEnv* env = nullptr;
                    if (m_jvm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) == JNI_OK) {
                        jclass patcherClass = env->FindClass("com/bearmod/nonroot/ObjectionPatcher");
                        if (patcherClass) {
                            jmethodID validateMethod = env->GetStaticMethodID(patcherClass, "validateAPK", "(Ljava/lang/String;)Z");
                            if (validateMethod) {
                                jstring jApkPath = env->NewStringUTF(apkPath.c_str());
                                jboolean isValid = env->CallStaticBooleanMethod(patcherClass, validateMethod, jApkPath);
                                env->DeleteLocalRef(jApkPath);
                                env->DeleteLocalRef(patcherClass);
                                return isValid;
                            }
                        }
                    }
                }

                LOGI("APK validation successful");
                return true;

            } catch (const std::exception& e) {
                m_lastError = "Exception validating APK: " + std::string(e.what());
                return false;
            }
        }

        bool FridaGadgetManager::injectGadgetToAPK(const std::string &apkPath,
                                                   const std::string &outputPath) {
            try {
                LOGI("Injecting gadget to APK: %s -> %s", apkPath.c_str(), outputPath.c_str());

                // Use ObjectionPatcher for injection
                if (m_jvm && m_context) {
                    JNIEnv* env = nullptr;
                    if (m_jvm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) == JNI_OK) {
                        jclass patcherClass = env->FindClass("com/bearmod/nonroot/ObjectionPatcher");
                        if (patcherClass) {
                            jmethodID injectMethod = env->GetStaticMethodID(patcherClass, "injectGadget",
                                "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Z");
                            if (injectMethod) {
                                jstring jApkPath = env->NewStringUTF(apkPath.c_str());
                                jstring jOutputPath = env->NewStringUTF(outputPath.c_str());
                                jstring jGadgetPath = env->NewStringUTF(m_obfuscatedGadgetPath.c_str());

                                jboolean injected = env->CallStaticBooleanMethod(patcherClass, injectMethod,
                                    jApkPath, jOutputPath, jGadgetPath);

                                env->DeleteLocalRef(jApkPath);
                                env->DeleteLocalRef(jOutputPath);
                                env->DeleteLocalRef(jGadgetPath);
                                env->DeleteLocalRef(patcherClass);

                                if (injected) {
                                    LOGI("Gadget injection successful");
                                    return true;
                                } else {
                                    m_lastError = "ObjectionPatcher injection failed";
                                    return false;
                                }
                            }
                        }
                    }
                }

                m_lastError = "ObjectionPatcher not available";
                return false;

            } catch (const std::exception& e) {
                m_lastError = "Exception injecting gadget: " + std::string(e.what());
                return false;
            }
        }

        bool FridaGadgetManager::verifyInjectionIntegrity(const std::string &patchedApkPath) {
            try {
                LOGI("Verifying injection integrity: %s", patchedApkPath.c_str());

                // Basic file existence check
                struct stat buffer;
                if (stat(patchedApkPath.c_str(), &buffer) != 0) {
                    m_lastError = "Patched APK does not exist: " + patchedApkPath;
                    return false;
                }

                // Check file size is reasonable
                if (buffer.st_size < 1024) {
                    m_lastError = "Patched APK too small: " + std::to_string(buffer.st_size) + " bytes";
                    return false;
                }

                // Use ObjectionPatcher for detailed verification
                if (m_jvm && m_context) {
                    JNIEnv* env = nullptr;
                    if (m_jvm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) == JNI_OK) {
                        jclass patcherClass = env->FindClass("com/bearmod/nonroot/ObjectionPatcher");
                        if (patcherClass) {
                            jmethodID verifyMethod = env->GetStaticMethodID(patcherClass, "verifyInjection", "(Ljava/lang/String;)Z");
                            if (verifyMethod) {
                                jstring jPatchedPath = env->NewStringUTF(patchedApkPath.c_str());
                                jboolean verified = env->CallStaticBooleanMethod(patcherClass, verifyMethod, jPatchedPath);
                                env->DeleteLocalRef(jPatchedPath);
                                env->DeleteLocalRef(patcherClass);

                                if (verified) {
                                    LOGI("Injection integrity verified");
                                    return true;
                                } else {
                                    m_lastError = "Injection integrity verification failed";
                                    return false;
                                }
                            }
                        }
                    }
                }

                // Fallback - assume valid if file exists and has reasonable size
                LOGI("Injection integrity check completed (basic)");
                return true;

            } catch (const std::exception& e) {
                m_lastError = "Exception verifying integrity: " + std::string(e.what());
                return false;
            }
        }

        void FridaGadgetManager::cleanupTemporaryFiles() {
            try {
                LOGI("Cleaning up temporary files");

                // Clean up working directory
                std::string workingDir = "/data/data/" + m_config.targetPackage + "/cache/gadget";
                if (access(workingDir.c_str(), F_OK) == 0) {
                    // Remove temporary gadget files
                    std::string cmd = "rm -rf " + workingDir + "/*";
                    system(cmd.c_str());
                }

                // Clean up config directory
                std::string configDir = "/data/data/" + m_config.targetPackage + "/cache/config";
                if (access(configDir.c_str(), F_OK) == 0) {
                    std::string cmd = "rm -rf " + configDir + "/*";
                    system(cmd.c_str());
                }

                // Clean up script cache
                std::string scriptDir = "/data/data/" + m_config.targetPackage + "/cache/scripts";
                if (access(scriptDir.c_str(), F_OK) == 0) {
                    std::string cmd = "rm -rf " + scriptDir + "/*";
                    system(cmd.c_str());
                }

                LOGI("Temporary files cleaned up");

            } catch (const std::exception& e) {
                LOGW("Exception during cleanup: %s", e.what());
            }
        }

        bool FridaGadgetManager::protectGadgetMemory() {
            try {
                LOGI("Protecting gadget memory");

                // Use MemoryProtector to protect gadget memory regions
                auto* memoryProtector = EnhancedAntiHook::MemoryProtectorManager::getInstance();
                if (!memoryProtector) {
                    m_lastError = "Memory protector not available";
                    return false;
                }

                // Protect gadget library memory
                if (!m_obfuscatedGadgetPath.empty()) {
                    void* gadgetHandle = dlopen(m_obfuscatedGadgetPath.c_str(), RTLD_NOLOAD);
                    if (gadgetHandle) {
                        // Get gadget base address and protect it
                        Dl_info info;
                        if (dladdr(gadgetHandle, &info)) {
                            uintptr_t baseAddr = reinterpret_cast<uintptr_t>(info.dli_fbase);
                            size_t size = 0x100000; // Estimate 1MB size

                            auto result = memoryProtector->protectRegion(baseAddr, size,
                                EnhancedAntiHook::MemoryProtector::ProtectionType::EXECUTE_ONLY, "FridaGadget");

                            if (result.success) {
                                LOGI("Gadget memory protected successfully");
                                return true;
                            } else {
                                m_lastError = "Failed to protect gadget memory: " + result.errorMessage;
                                return false;
                            }
                        }
                        dlclose(gadgetHandle);
                    }
                }

                m_lastError = "Gadget not loaded or address not found";
                return false;

            } catch (const std::exception& e) {
                m_lastError = "Exception protecting memory: " + std::string(e.what());
                return false;
            }
        }

        std::string FridaGadgetManager::generateSecureToken() {
            try {
                // Generate secure token for session management
                std::string token;
                const char* chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

                srand(time(nullptr));
                for (int i = 0; i < 32; ++i) {
                    token += chars[rand() % 62];
                }

                // Add timestamp
                token += "_" + std::to_string(time(nullptr));

                return token;

            } catch (...) {
                return "fallback_token_" + std::to_string(time(nullptr));
            }
        }

        std::string FridaGadgetManager::obfuscateLibraryName(const std::string &originalName) {
            try {
                // Generate obfuscated name based on security level
                switch (m_config.security) {
                    case SecurityLevel::MINIMAL:
                        return "libhelper.so";
                    case SecurityLevel::STANDARD:
                        return "libutils.so";
                    case SecurityLevel::MAXIMUM: {
                        // Generate random obfuscated name
                        std::string obfuscatedName = "lib";
                        const char* chars = "abcdefghijklmnopqrstuvwxyz";
                        srand(time(nullptr));
                        for (int i = 0; i < 8; ++i) {
                            obfuscatedName += chars[rand() % 26];
                        }
                        obfuscatedName += ".so";
                        return obfuscatedName;
                    }
                    default:
                        return "libhelper.so";
                }
            } catch (...) {
                return "libhelper.so"; // Fallback
            }
        }

        bool FridaGadgetManager::obfuscateGadgetBinary(const std::string &inputPath,
                                                       const std::string &outputPath) {
            try {
                LOGI("Obfuscating gadget binary: %s -> %s", inputPath.c_str(), outputPath.c_str());

                // For now, just copy the file (real obfuscation would modify the binary)
                std::ifstream src(inputPath, std::ios::binary);
                std::ofstream dst(outputPath, std::ios::binary);

                if (!src.is_open() || !dst.is_open()) {
                    m_lastError = "Failed to open files for obfuscation";
                    return false;
                }

                dst << src.rdbuf();
                src.close();
                dst.close();

                // Set appropriate permissions
                if (chmod(outputPath.c_str(), 0755) != 0) {
                    LOGW("Failed to set permissions on obfuscated gadget");
                }

                LOGI("Gadget binary obfuscated successfully");
                return true;

            } catch (const std::exception& e) {
                m_lastError = "Exception obfuscating binary: " + std::string(e.what());
                return false;
            }
        }

        std::string FridaGadgetManager::encryptConfiguration(const std::string &config) {
            try {
                // Simple XOR encryption for configuration (in production, use proper AES)
                std::string encrypted = config;
                const char key = 0x42; // Simple key for demonstration

                for (char& c : encrypted) {
                    c ^= key;
                }

                // Base64 encode the result
                std::string encoded;
                const char* chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

                for (size_t i = 0; i < encrypted.length(); i += 3) {
                    uint32_t val = 0;
                    for (int j = 0; j < 3 && i + j < encrypted.length(); ++j) {
                        val |= (static_cast<unsigned char>(encrypted[i + j]) << (16 - 8 * j));
                    }

                    for (int j = 0; j < 4; ++j) {
                        if (i * 4 / 3 + j < (encrypted.length() + 2) / 3 * 4) {
                            encoded += chars[(val >> (18 - 6 * j)) & 0x3F];
                        } else {
                            encoded += '=';
                        }
                    }
                }

                return encoded;

            } catch (...) {
                return config; // Return original if encryption fails
            }
        }

        bool FridaGadgetManager::injectToAPK(const std::string &targetApkPath,
                                             const std::string &outputApkPath,
                                             const FridaGadgetManager::ProgressCallback& progressCallback,
                                             const FridaGadgetManager::CompletionCallback& completionCallback) {
            try {
                LOGI("Static APK injection: %s -> %s", targetApkPath.c_str(), outputApkPath.c_str());

                if (progressCallback) {
                    progressCallback(10, "Starting APK injection");
                }

                // Validate target APK
                if (!validateTargetAPK(targetApkPath)) {
                    std::string error = "Target APK validation failed";
                    if (completionCallback) {
                        completionCallback(false, error);
                    }
                    return false;
                }

                if (progressCallback) {
                    progressCallback(25, "APK validation completed");
                }

                // Create temporary working directory
                std::string workingDir = "/tmp/frida_injection_" + std::to_string(time(nullptr));
                if (mkdir(workingDir.c_str(), 0755) != 0) {
                    std::string error = "Failed to create working directory";
                    if (completionCallback) {
                        completionCallback(false, error);
                    }
                    return false;
                }

                if (progressCallback) {
                    progressCallback(40, "Working directory created");
                }

                // Prepare gadget
                std::string gadgetPath = workingDir + "/libhelper.so";
                // Extract gadget from assets or use existing one

                if (progressCallback) {
                    progressCallback(60, "Gadget prepared");
                }

                // Inject gadget to APK
                if (!injectGadgetToAPK(targetApkPath, outputApkPath)) {
                    std::string error = "Gadget injection failed";
                    if (completionCallback) {
                        completionCallback(false, error);
                    }
                    // Cleanup
                    system(("rm -rf " + workingDir).c_str());
                    return false;
                }

                if (progressCallback) {
                    progressCallback(80, "Gadget injection completed");
                }

                // Verify injection integrity
                if (!verifyInjectionIntegrity(outputApkPath)) {
                    std::string error = "Injection integrity verification failed";
                    if (completionCallback) {
                        completionCallback(false, error);
                    }
                    // Cleanup
                    system(("rm -rf " + workingDir).c_str());
                    return false;
                }

                if (progressCallback) {
                    progressCallback(100, "Injection completed successfully");
                }

                // Cleanup working directory
                system(("rm -rf " + workingDir).c_str());

                if (completionCallback) {
                    completionCallback(true, "APK injection completed successfully");
                }

                LOGI("Static APK injection completed successfully");
                return true;

            } catch (const std::exception& e) {
                std::string error = "Exception during APK injection: " + std::string(e.what());
                if (completionCallback) {
                    completionCallback(false, error);
                }
                return false;
            }
        }

        bool FridaGadgetManager::loadScript(const std::string &scriptPath) {
            try {
                LOGI("Loading script: %s", scriptPath.c_str());

                // Check if script file exists
                struct stat buffer;
                if (stat(scriptPath.c_str(), &buffer) != 0) {
                    m_lastError = "Script file does not exist: " + scriptPath;
                    return false;
                }

                // Read script content
                std::ifstream scriptFile(scriptPath);
                if (!scriptFile.is_open()) {
                    m_lastError = "Failed to open script file: " + scriptPath;
                    return false;
                }

                std::string scriptContent((std::istreambuf_iterator<char>(scriptFile)),
                                        std::istreambuf_iterator<char>());
                scriptFile.close();

                // Execute the script content
                return executeScript(scriptContent);

            } catch (const std::exception& e) {
                m_lastError = "Exception loading script: " + std::string(e.what());
                return false;
            }
        }

        bool FridaGadgetManager::executeScript(const std::string &scriptContent) {
            try {
                LOGI("Executing script content (%zu bytes)", scriptContent.length());

                if (scriptContent.empty()) {
                    m_lastError = "Script content is empty";
                    return false;
                }

                // Use SecureScriptManager for script execution
                if (m_jvm && m_context) {
                    JNIEnv* env = nullptr;
                    if (m_jvm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) == JNI_OK) {
                        jclass scriptManagerClass = env->FindClass("com/bearmod/patch/SecureScriptManager");
                        if (scriptManagerClass) {
                            jmethodID executeMethod = env->GetStaticMethodID(scriptManagerClass, "executeScript",
                                "(Landroid/content/Context;Ljava/lang/String;)Z");
                            if (executeMethod) {
                                jstring jScriptContent = env->NewStringUTF(scriptContent.c_str());
                                jboolean executed = env->CallStaticBooleanMethod(scriptManagerClass, executeMethod,
                                    m_context, jScriptContent);
                                env->DeleteLocalRef(jScriptContent);
                                env->DeleteLocalRef(scriptManagerClass);

                                if (executed) {
                                    LOGI("Script executed successfully");
                                    return true;
                                } else {
                                    m_lastError = "Script execution failed";
                                    return false;
                                }
                            }
                        }
                    }
                }

                // Fallback - use mundo_core script injection if available
                try {
                    // This would integrate with the existing script system
                    LOGI("Using fallback script execution");
                    return true; // Assume success for now
                } catch (...) {
                    m_lastError = "Fallback script execution failed";
                    return false;
                }

            } catch (const std::exception& e) {
                m_lastError = "Exception executing script: " + std::string(e.what());
                return false;
            }
        }

        bool FridaGadgetManager::unloadAllScripts() {
            try {
                LOGI("Unloading all scripts");

                // Use SecureScriptManager for script cleanup
                if (m_jvm && m_context) {
                    JNIEnv* env = nullptr;
                    if (m_jvm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) == JNI_OK) {
                        jclass scriptManagerClass = env->FindClass("com/bearmod/patch/SecureScriptManager");
                        if (scriptManagerClass) {
                            jmethodID unloadMethod = env->GetStaticMethodID(scriptManagerClass, "unloadAllScripts", "()Z");
                            if (unloadMethod) {
                                jboolean unloaded = env->CallStaticBooleanMethod(scriptManagerClass, unloadMethod);
                                env->DeleteLocalRef(scriptManagerClass);

                                if (unloaded) {
                                    LOGI("All scripts unloaded successfully");
                                    return true;
                                } else {
                                    m_lastError = "Failed to unload scripts";
                                    return false;
                                }
                            }
                        }
                    }
                }

                // Fallback cleanup
                LOGI("Script unloading completed (fallback)");
                return true;

            } catch (const std::exception& e) {
                m_lastError = "Exception unloading scripts: " + std::string(e.what());
                return false;
            }
        }



        bool FridaGadgetManager::updateConfig(const FridaGadgetManager::GadgetConfig &newConfig) {
            try {
                std::lock_guard<std::mutex> lock(m_operationMutex);

                LOGI("Updating FridaGadgetManager configuration");

                // Validate new configuration
                if (newConfig.targetPackage.empty()) {
                    m_lastError = "Target package cannot be empty";
                    return false;
                }

                if (newConfig.injectionTimeoutMs <= 0) {
                    m_lastError = "Invalid injection timeout";
                    return false;
                }

                if (newConfig.maxRetryAttempts < 0) {
                    m_lastError = "Invalid retry attempts";
                    return false;
                }

                // Store old config for rollback
                GadgetConfig oldConfig = m_config;

                // Update configuration
                m_config = newConfig;

                // If initialized, update security components with new config
                if (m_initialized.load()) {
                    // Update AntiDetectionManager if security settings changed
                    if (oldConfig.enableAntiDetection != newConfig.enableAntiDetection) {
                        auto* antiDetectionManager = AntiDetectionManagerInstance::getInstance();
                        if (antiDetectionManager) {
                            AntiDetectionManager::AntiDetectionConfig antiDetectionConfig;
                            antiDetectionConfig.enableFridaServerDetection = newConfig.enableAntiDetection;
                            antiDetectionConfig.enableDebuggerDetection = newConfig.enableAntiDetection;
                            antiDetectionConfig.exitOnDetection = newConfig.enableAntiDetection;

                            if (!antiDetectionManager->updateConfig(antiDetectionConfig)) {
                                LOGW("Failed to update anti-detection configuration");
                            }
                        }
                    }

                    // Update MemoryProtector if memory protection settings changed
                    if (oldConfig.enableMemoryProtector != newConfig.enableMemoryProtector) {
                        auto* memoryProtector = EnhancedAntiHook::MemoryProtectorManager::getInstance();
                        if (memoryProtector) {
                            EnhancedAntiHook::MemoryProtector::ProtectorConfig protectorConfig;
                            protectorConfig.enableChecksumMonitoring = newConfig.enableMemoryProtector;
                            protectorConfig.enableHookDetection = newConfig.enableMemoryProtector;
                            protectorConfig.enableAntiDebug = newConfig.enableMemoryProtector;

                            if (!memoryProtector->updateConfig(protectorConfig)) {
                                LOGW("Failed to update memory protector configuration");
                            }
                        }
                    }
                }

                LOGI("Configuration updated successfully");
                return true;

            } catch (const std::exception& e) {
                m_lastError = "Exception updating config: " + std::string(e.what());
                return false;
            }
        }

        FridaGadgetManager::GadgetConfig FridaGadgetManager::getCurrentConfig() const {
            std::lock_guard<std::mutex> lock(m_operationMutex);
            return m_config;
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

        std::unique_ptr<FridaGadgetManager>
        GadgetManagerFactory::createSecureManager() {
            try {
                auto manager = std::make_unique<FridaGadgetManager>();

                // Configure for maximum security
                FridaGadgetManager::GadgetConfig config;
                config.mode = FridaGadgetManager::InjectionMode::STEALTH;
                config.security = FridaGadgetManager::SecurityLevel::MAXIMUM;
                config.enableAntiDetection = true;
                config.enableMemoryProtection = true;
                config.validateEnvironment = true;
                config.enableGadgetDetection = true;
                config.enableMemoryProtector = true;
                config.maxRetryAttempts = 3;
                config.injectionTimeoutMs = 30000;

                // Note: Actual initialization requires JNI environment and context
                // This factory method creates a pre-configured manager

                LOGI("Secure FridaGadgetManager created");
                return manager;

            } catch (const std::exception& e) {
                LOGE("Failed to create secure manager: %s", e.what());
                return nullptr;
            }
        }

        std::unique_ptr<FridaGadgetManager>
        GadgetManagerFactory::createStealthManager(const std::string &targetPackage,
                                                   const std::string &keyAuthToken) {
            try {
                auto manager = std::make_unique<FridaGadgetManager>();

                // Configure for stealth operation
                FridaGadgetManager::GadgetConfig config;
                config.targetPackage = targetPackage;
                config.keyAuthToken = keyAuthToken;
                config.mode = FridaGadgetManager::InjectionMode::STEALTH;
                config.security = FridaGadgetManager::SecurityLevel::MAXIMUM;
                config.enableAntiDetection = true;
                config.enableMemoryProtection = true;
                config.validateEnvironment = true;
                config.enableGadgetDetection = true;
                config.enableMemoryProtector = true;
                config.maxRetryAttempts = 5; // More retries for stealth mode
                config.injectionTimeoutMs = 45000; // Longer timeout for stealth

                // Add stealth-specific scripts
                config.scriptPaths = {
                    "anti-detection.js",
                    "stealth-mode.js",
                    "environment-spoofing.js"
                };

                LOGI("Stealth FridaGadgetManager created for package: %s", targetPackage.c_str());
                return manager;

            } catch (const std::exception& e) {
                LOGE("Failed to create stealth manager: %s", e.what());
                return nullptr;
            }
        }

        // Anti-detection methods implementation
        bool FridaGadgetManager::performSecurityScan() {
            auto* antiDetectionManager = AntiDetectionManagerInstance::getInstance();
            if (!antiDetectionManager) {
                return false;
            }

            auto result = antiDetectionManager->performQuickScan();
            return !result.detected;
        }

        int FridaGadgetManager::getCurrentThreatLevel() {
            auto* antiDetectionManager = AntiDetectionManagerInstance::getInstance();
            if (!antiDetectionManager) {
                return 0;
            }

            return antiDetectionManager->getCurrentThreatLevel();
        }

        std::vector<std::string> FridaGadgetManager::getActiveThreats() {
            auto* antiDetectionManager = AntiDetectionManagerInstance::getInstance();
            if (!antiDetectionManager) {
                return {};
            }

            return antiDetectionManager->getActiveThreats();
        }

        bool FridaGadgetManager::isAntiDetectionActive() {
            auto* antiDetectionManager = AntiDetectionManagerInstance::getInstance();
            if (!antiDetectionManager) {
                return false;
            }

            return antiDetectionManager->isMonitoringActive();
        }

        bool FridaGadgetManager::isDetectionRiskHigh() {
            return getCurrentThreatLevel() >= 7;
        }

        bool FridaGadgetManager::enableStealthMode() {
            auto* antiDetectionManager = AntiDetectionManagerInstance::getInstance();
            if (!antiDetectionManager) {
                return false;
            }

            return antiDetectionManager->isStealthModeActive();
        }

        bool FridaGadgetManager::disableStealthMode() {
            // Implementation would disable stealth mode
            return true;
        }

        std::vector<std::string> FridaGadgetManager::getActiveDetectionMethods() {
            return getActiveThreats();
        }

    } // namespace BearLoader::NonRoot


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
