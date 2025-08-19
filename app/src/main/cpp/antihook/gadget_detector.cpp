#include "gadget_detector.h"
#include <fstream>
#include <sstream>
#include <thread>
#include <algorithm>
#include <cstdlib>
#include <unistd.h>
#include <dirent.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>

namespace BearLoader {
    namespace EnhancedAntiHook {
        
        // Static member definitions
        std::unique_ptr<GadgetDetector> GadgetDetectorManager::s_instance = nullptr;
        std::mutex GadgetDetectorManager::s_instanceMutex;
        
        GadgetDetector::GadgetDetector() {
            // Initialize known Frida artifacts
            m_knownLibraries = {
                "libfrida-gadget.so",
                "libhelper.so",  // Common obfuscated name
                "libgadget.so",
                "libfrida.so",
                "libgum.so",
                "frida-agent",
                "gum-js-loop"
            };
            
            m_knownProcesses = {
                "frida-server",
                "frida",
                "gum-js-loop",
                "frida-helper",
                "re.frida.server"
            };
            
            m_knownPorts = {
                27042, 27043, 27044, 27045  // Common Frida server ports
            };
        }
        
        GadgetDetector::~GadgetDetector() {
            shutdown();
        }
        
        bool GadgetDetector::initialize(const DetectorConfig& config) {
            std::lock_guard<std::mutex> lock(m_configMutex);
            m_config = config;
            return true;
        }
        
        void GadgetDetector::shutdown() {
            stopDetection();
        }
        
        bool GadgetDetector::startDetection() {
            if (m_detectionActive.load()) {
                return true;
            }
            
            m_running = true;
            m_detectionActive = true;
            
            // Start detection thread
            std::thread detectionThread([this]() {
                while (m_running.load()) {
                    try {
                        auto result = performSingleScan();
                        
                        if (result.detected) {
                            notifyCallbacks(result);
                            
                            if (m_config.exitOnDetection && 
                                result.threatLevel >= m_config.maxThreatLevel) {
                                // Critical threat detected - exit immediately
                                std::exit(1);
                            }
                        }
                        
                        std::this_thread::sleep_for(
                            std::chrono::milliseconds(m_config.scanIntervalMs));
                            
                    } catch (const std::exception& e) {
                        // Log error and continue
                        std::this_thread::sleep_for(std::chrono::seconds(1));
                    }
                }
            });
            
            detectionThread.detach();
            return true;
        }
        
        void GadgetDetector::stopDetection() {
            m_running = false;
            m_detectionActive = false;
        }
        
        GadgetDetector::DetectionResult GadgetDetector::performSingleScan() {
            DetectionResult result;
            result.detected = false;
            result.timestamp = std::chrono::steady_clock::now();
            result.threatLevel = 0;
            
            // Perform all enabled detection methods
            if (m_config.enableLibraryScan && detectFridaLibraries()) {
                result.detected = true;
                result.detectionMethod = "Library Scan";
                result.details = "Frida library detected in process memory";
                result.threatLevel = std::max(result.threatLevel, 8);
            }
            
            if (m_config.enableProcessScan && detectFridaProcesses()) {
                result.detected = true;
                result.detectionMethod = "Process Scan";
                result.details = "Frida process detected on system";
                result.threatLevel = std::max(result.threatLevel, 7);
            }
            
            if (m_config.enableMemoryScan && detectFridaInMemory()) {
                result.detected = true;
                result.detectionMethod = "Memory Scan";
                result.details = "Frida signatures found in memory";
                result.threatLevel = std::max(result.threatLevel, 9);
            }
            
            if (m_config.enableThreadScan && detectFridaThreads()) {
                result.detected = true;
                result.detectionMethod = "Thread Scan";
                result.details = "Frida threads detected";
                result.threatLevel = std::max(result.threatLevel, 6);
            }
            
            if (m_config.enablePortScan && detectFridaPorts()) {
                result.detected = true;
                result.detectionMethod = "Port Scan";
                result.details = "Frida server port detected";
                result.threatLevel = std::max(result.threatLevel, 5);
            }
            
            if (m_config.enableGadgetSpecific && detectGadgetSpecific()) {
                result.detected = true;
                result.detectionMethod = "Gadget Specific";
                result.details = "Frida Gadget specific artifacts detected";
                result.threatLevel = std::max(result.threatLevel, 10);
            }
            
            if (result.detected) {
                m_detectionHistory.push_back(result);
            }
            
            return result;
        }
        
        bool GadgetDetector::detectFridaLibraries() {
            try {
                auto libraries = getLoadedLibraries();
                
                for (const auto& lib : libraries) {
                    for (const auto& knownLib : m_knownLibraries) {
                        if (lib.find(knownLib) != std::string::npos) {
                            return true;
                        }
                    }
                }
                
                return false;
            } catch (...) {
                return false;
            }
        }
        
        bool GadgetDetector::detectFridaProcesses() {
            try {
                auto processes = getRunningProcesses();
                
                for (const auto& proc : processes) {
                    for (const auto& knownProc : m_knownProcesses) {
                        if (proc.find(knownProc) != std::string::npos) {
                            return true;
                        }
                    }
                }
                
                return false;
            } catch (...) {
                return false;
            }
        }
        
        bool GadgetDetector::detectFridaInMemory() {
            try {
                // Check for common Frida strings in memory
                std::vector<std::string> fridaStrings = {
                    "frida",
                    "gum-js-loop",
                    "frida-agent",
                    "FRIDA",
                    "gum_script_scheduler"
                };
                
                for (const auto& str : fridaStrings) {
                    if (findStringInMemory(str)) {
                        return true;
                    }
                }
                
                return false;
            } catch (...) {
                return false;
            }
        }
        
        bool GadgetDetector::detectFridaThreads() {
            try {
                std::ifstream statusFile("/proc/self/status");
                std::string line;
                
                while (std::getline(statusFile, line)) {
                    if (line.find("Threads:") == 0) {
                        int threadCount = std::stoi(line.substr(8));
                        // Frida typically creates additional threads
                        if (threadCount > 10) {  // Adjust threshold as needed
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
        
        bool GadgetDetector::detectFridaPorts() {
            try {
                auto openPorts = getOpenPorts();
                
                for (int port : openPorts) {
                    if (m_knownPorts.find(port) != m_knownPorts.end()) {
                        return true;
                    }
                }
                
                return false;
            } catch (...) {
                return false;
            }
        }
        
        bool GadgetDetector::detectGadgetSpecific() {
            try {
                // Check for gadget-specific environment variables
                const char* gadgetEnvVars[] = {
                    "FRIDA_GADGET_CONFIG",
                    "FRIDA_GADGET_SCRIPT",
                    "FRIDA_GADGET_INTERACTION"
                };
                
                for (const char* envVar : gadgetEnvVars) {
                    if (std::getenv(envVar) != nullptr) {
                        return true;
                    }
                }
                
                // Check for gadget configuration files
                std::vector<std::string> configPaths = {
                    "/data/local/tmp/gadget.config",
                    "/sdcard/gadget.config",
                    "/data/data/com.android.chrome/gadget.config"
                };
                
                for (const auto& path : configPaths) {
                    std::ifstream file(path);
                    if (file.good()) {
                        return true;
                    }
                }
                
                return false;
            } catch (...) {
                return false;
            }
        }
        
        std::vector<std::string> GadgetDetector::getLoadedLibraries() {
            std::vector<std::string> libraries;
            
            try {
                std::ifstream mapsFile("/proc/self/maps");
                std::string line;
                
                while (std::getline(mapsFile, line)) {
                    if (line.find(".so") != std::string::npos) {
                        size_t pos = line.find_last_of('/');
                        if (pos != std::string::npos) {
                            std::string libName = line.substr(pos + 1);
                            libraries.push_back(libName);
                        }
                    }
                }
            } catch (...) {
                // Ignore errors
            }
            
            return libraries;
        }
        
        std::vector<std::string> GadgetDetector::getRunningProcesses() {
            std::vector<std::string> processes;
            
            try {
                DIR* procDir = opendir("/proc");
                if (procDir) {
                    struct dirent* entry;
                    while ((entry = readdir(procDir)) != nullptr) {
                        if (std::isdigit(entry->d_name[0])) {
                            std::string cmdlinePath = "/proc/" + std::string(entry->d_name) + "/cmdline";
                            std::ifstream cmdlineFile(cmdlinePath);
                            std::string cmdline;
                            if (std::getline(cmdlineFile, cmdline)) {
                                processes.push_back(cmdline);
                            }
                        }
                    }
                    closedir(procDir);
                }
            } catch (...) {
                // Ignore errors
            }
            
            return processes;
        }
        
        std::vector<int> GadgetDetector::getOpenPorts() {
            std::vector<int> ports;
            
            try {
                std::ifstream tcpFile("/proc/net/tcp");
                std::string line;
                
                while (std::getline(tcpFile, line)) {
                    std::istringstream iss(line);
                    std::string token;
                    int tokenCount = 0;
                    
                    while (iss >> token && tokenCount < 2) {
                        if (tokenCount == 1) {  // Local address column
                            size_t colonPos = token.find(':');
                            if (colonPos != std::string::npos) {
                                std::string portHex = token.substr(colonPos + 1);
                                int port = std::stoi(portHex, nullptr, 16);
                                ports.push_back(port);
                            }
                        }
                        tokenCount++;
                    }
                }
            } catch (...) {
                // Ignore errors
            }
            
            return ports;
        }
        
        bool GadgetDetector::findStringInMemory(const std::string& searchString) {
            try {
                std::ifstream mapsFile("/proc/self/maps");
                std::string line;
                
                while (std::getline(mapsFile, line)) {
                    if (line.find("r-xp") != std::string::npos) {  // Executable regions
                        // Parse memory region
                        size_t dashPos = line.find('-');
                        if (dashPos != std::string::npos) {
                            std::string startAddr = line.substr(0, dashPos);
                            size_t spacePos = line.find(' ', dashPos);
                            if (spacePos != std::string::npos) {
                                std::string endAddr = line.substr(dashPos + 1, spacePos - dashPos - 1);
                                
                                // Convert addresses and search memory region
                                uintptr_t start = std::stoull(startAddr, nullptr, 16);
                                uintptr_t end = std::stoull(endAddr, nullptr, 16);
                                
                                // Simple memory search (this is a basic implementation)
                                // In a real implementation, you'd want more sophisticated memory scanning
                                for (uintptr_t addr = start; addr < end - searchString.length(); addr += 4096) {
                                    try {
                                        const char* memPtr = reinterpret_cast<const char*>(addr);
                                        if (std::string(memPtr, std::min(searchString.length(), end - addr)).find(searchString) != std::string::npos) {
                                            return true;
                                        }
                                    } catch (...) {
                                        // Memory access failed, continue
                                        continue;
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (...) {
                // Ignore errors
            }
            
            return false;
        }
        
        void GadgetDetector::notifyCallbacks(const DetectionResult& result) {
            std::lock_guard<std::mutex> lock(m_callbackMutex);
            
            for (const auto& callback : m_callbacks) {
                try {
                    callback(result);
                } catch (...) {
                    // Ignore callback errors
                }
            }
        }
        
        void GadgetDetector::addDetectionCallback(DetectionCallback callback) {
            std::lock_guard<std::mutex> lock(m_callbackMutex);
            m_callbacks.push_back(callback);
        }
        
        // GadgetDetectorManager implementation
        GadgetDetector* GadgetDetectorManager::getInstance() {
            std::lock_guard<std::mutex> lock(s_instanceMutex);
            return s_instance.get();
        }
        
        bool GadgetDetectorManager::createInstance(const GadgetDetector::DetectorConfig& config) {
            std::lock_guard<std::mutex> lock(s_instanceMutex);
            
            if (s_instance) {
                return false;  // Already exists
            }
            
            s_instance = std::make_unique<GadgetDetector>();
            return s_instance->initialize(config);
        }
        
        void GadgetDetectorManager::destroyInstance() {
            std::lock_guard<std::mutex> lock(s_instanceMutex);
            s_instance.reset();
        }
        
        bool GadgetDetectorManager::quickGadgetCheck() {
            auto* detector = getInstance();
            if (!detector) {
                return false;
            }
            
            auto result = detector->performSingleScan();
            return result.detected;
        }
        
    } // namespace EnhancedAntiHook
} // namespace BearLoader
