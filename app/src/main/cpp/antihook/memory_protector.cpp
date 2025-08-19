#include "memory_protector.h"
#include <sys/mman.h>
#include <unistd.h>
#include <dlfcn.h>
#include <thread>
#include <algorithm>
#include <cstring>
#include <fstream>
#include <sstream>

namespace BearLoader {
    namespace EnhancedAntiHook {
        
        // Static member definitions
        std::unique_ptr<MemoryProtector> MemoryProtectorManager::s_instance = nullptr;
        std::mutex MemoryProtectorManager::s_instanceMutex;
        
        MemoryProtector::MemoryProtector() {
            // Initialize with default configuration
        }
        
        MemoryProtector::~MemoryProtector() {
            shutdown();
        }
        
        bool MemoryProtector::initialize(const ProtectorConfig& config) {
            std::lock_guard<std::mutex> lock(m_regionsMutex);
            m_config = config;
            
            if (m_config.enableAntiDebug) {
                enableAntiDebugging();
            }
            
            return true;
        }
        
        void MemoryProtector::shutdown() {
            stopProtection();
            
            // Unprotect all regions
            std::lock_guard<std::mutex> lock(m_regionsMutex);
            for (auto& [address, region] : m_protectedRegions) {
                if (region.isProtected) {
                    unprotectRegion(address);
                }
            }
            m_protectedRegions.clear();
        }
        
        bool MemoryProtector::startProtection() {
            if (m_protectionActive.load()) {
                return true;
            }
            
            m_running = true;
            m_protectionActive = true;
            
            // Start monitoring thread
            std::thread monitoringThread([this]() {
                while (m_running.load()) {
                    try {
                        if (m_config.enableChecksumMonitoring) {
                            performIntegrityCheck();
                        }
                        
                        if (m_config.enableAntiDebug) {
                            if (detectDebugger() || detectTracer()) {
                                reportTampering({}, "Debugger or tracer detected");
                                if (m_config.exitOnTampering) {
                                    std::exit(1);
                                }
                            }
                        }
                        
                        std::this_thread::sleep_for(
                            std::chrono::milliseconds(m_config.checksumIntervalMs));
                            
                    } catch (const std::exception& e) {
                        std::this_thread::sleep_for(std::chrono::seconds(1));
                    }
                }
            });
            
            monitoringThread.detach();
            return true;
        }
        
        void MemoryProtector::stopProtection() {
            m_running = false;
            m_protectionActive = false;
        }
        
        MemoryProtector::ProtectionResult MemoryProtector::protectRegion(
            uintptr_t address, size_t size, ProtectionType type, const std::string& name) {
            
            ProtectionResult result;
            result.protectedAddress = address;
            result.protectedSize = size;
            result.type = type;
            result.success = false;
            
            if (!isAddressValid(address)) {
                result.errorMessage = "Invalid address";
                return result;
            }
            
            // Align to page boundaries
            uintptr_t alignedAddress = alignToPageBoundary(address);
            size_t alignedSize = ((address + size - alignedAddress + getPageSize() - 1) / getPageSize()) * getPageSize();
            
            bool protectionSuccess = false;
            
            switch (type) {
                case ProtectionType::READ_ONLY:
                    protectionSuccess = protectRegionReadOnly(alignedAddress, alignedSize);
                    break;
                    
                case ProtectionType::EXECUTE_ONLY:
                    protectionSuccess = protectRegionExecuteOnly(alignedAddress, alignedSize);
                    break;
                    
                case ProtectionType::NO_ACCESS:
                    protectionSuccess = protectRegionNoAccess(alignedAddress, alignedSize);
                    break;
                    
                case ProtectionType::GUARD_PAGE:
                    protectionSuccess = addGuardPages(alignedAddress, alignedSize);
                    break;
                    
                case ProtectionType::CHECKSUM_MONITOR:
                    protectionSuccess = setupChecksumMonitoring(address, size);
                    break;
                    
                case ProtectionType::HOOK_DETECTION:
                    protectionSuccess = setupHookDetection(address, size);
                    break;
                    
                default:
                    result.errorMessage = "Unsupported protection type";
                    return result;
            }
            
            if (protectionSuccess) {
                std::lock_guard<std::mutex> lock(m_regionsMutex);
                
                MemoryRegion region;
                region.startAddress = address;
                region.size = size;
                region.protectionType = type;
                region.originalChecksum = calculateChecksum(address, size);
                region.lastCheck = std::chrono::steady_clock::now();
                region.isProtected = true;
                region.name = name.empty() ? "Region_" + std::to_string(address) : name;
                
                m_protectedRegions[address] = region;
                result.success = true;
            } else {
                result.errorMessage = "Failed to apply protection";
            }
            
            return result;
        }
        
        bool MemoryProtector::protectRegionReadOnly(uintptr_t address, size_t size) {
            return mprotect(reinterpret_cast<void*>(address), size, PROT_READ) == 0;
        }
        
        bool MemoryProtector::protectRegionExecuteOnly(uintptr_t address, size_t size) {
            return mprotect(reinterpret_cast<void*>(address), size, PROT_EXEC) == 0;
        }
        
        bool MemoryProtector::protectRegionNoAccess(uintptr_t address, size_t size) {
            return mprotect(reinterpret_cast<void*>(address), size, PROT_NONE) == 0;
        }
        
        bool MemoryProtector::addGuardPages(uintptr_t address, size_t size) {
            size_t pageSize = getPageSize();
            
            // Add guard page before the region
            uintptr_t guardBefore = address - pageSize;
            if (mprotect(reinterpret_cast<void*>(guardBefore), pageSize, PROT_NONE) == 0) {
                m_guardPages.push_back(guardBefore);
            }
            
            // Add guard page after the region
            uintptr_t guardAfter = address + size;
            if (mprotect(reinterpret_cast<void*>(guardAfter), pageSize, PROT_NONE) == 0) {
                m_guardPages.push_back(guardAfter);
                return true;
            }
            
            return false;
        }
        
        bool MemoryProtector::setupChecksumMonitoring(uintptr_t address, size_t size) {
            // Checksum monitoring is handled in the monitoring thread
            return true;
        }
        
        bool MemoryProtector::setupHookDetection(uintptr_t address, size_t size) {
            // Hook detection is handled in the monitoring thread
            return true;
        }
        
        bool MemoryProtector::performIntegrityCheck() {
            std::lock_guard<std::mutex> lock(m_regionsMutex);
            
            bool allIntact = true;
            
            for (auto& [address, region] : m_protectedRegions) {
                if (!verifyRegionIntegrity(region)) {
                    allIntact = false;
                    reportTampering(region, "Integrity check failed");
                    
                    m_tamperingEventCount++;
                    if (m_config.exitOnTampering && 
                        m_tamperingEventCount >= m_config.maxTamperingEvents) {
                        std::exit(1);
                    }
                }
            }
            
            return allIntact;
        }
        
        bool MemoryProtector::verifyRegionIntegrity(const MemoryRegion& region) {
            if (region.protectionType == ProtectionType::CHECKSUM_MONITOR) {
                uint32_t currentChecksum = calculateChecksum(region.startAddress, region.size);
                return currentChecksum == region.originalChecksum;
            }
            
            if (region.protectionType == ProtectionType::HOOK_DETECTION) {
                return !detectHookModifications(region);
            }
            
            return true;
        }
        
        uint32_t MemoryProtector::calculateChecksum(uintptr_t address, size_t size) {
            uint32_t checksum = 0;
            
            try {
                const uint8_t* data = reinterpret_cast<const uint8_t*>(address);
                for (size_t i = 0; i < size; ++i) {
                    checksum = (checksum << 1) ^ data[i];
                }
            } catch (...) {
                // Memory access failed
                return 0;
            }
            
            return checksum;
        }
        
        bool MemoryProtector::detectHookModifications(const MemoryRegion& region) {
            return detectInlineHooks(region.startAddress, region.size) ||
                   detectJumpHooks(region.startAddress, region.size);
        }
        
        bool MemoryProtector::detectInlineHooks(uintptr_t address, size_t size) {
            try {
                const uint8_t* code = reinterpret_cast<const uint8_t*>(address);
                
                // Check for common hook patterns
                for (size_t i = 0; i < size - 4; ++i) {
                    // Check for jump instructions (0xE9, 0xEB)
                    if (code[i] == 0xE9 || code[i] == 0xEB) {
                        return true;
                    }
                    
                    // Check for call instructions (0xE8)
                    if (code[i] == 0xE8) {
                        return true;
                    }
                    
                    // Check for int3 breakpoints (0xCC)
                    if (code[i] == 0xCC) {
                        return true;
                    }
                }
            } catch (...) {
                return true; // Assume hooked if we can't read
            }
            
            return false;
        }
        
        bool MemoryProtector::detectJumpHooks(uintptr_t address, size_t size) {
            try {
                const uint8_t* code = reinterpret_cast<const uint8_t*>(address);
                
                // Check for long jump patterns
                for (size_t i = 0; i < size - 8; ++i) {
                    // Check for mov rax, addr; jmp rax pattern
                    if (code[i] == 0x48 && code[i+1] == 0xB8) { // mov rax, imm64
                        if (i + 10 < size && code[i+10] == 0xFF && code[i+11] == 0xE0) { // jmp rax
                            return true;
                        }
                    }
                }
            } catch (...) {
                return true; // Assume hooked if we can't read
            }
            
            return false;
        }
        
        bool MemoryProtector::enableAntiDebugging() {
            // Implement anti-debugging measures
            return true;
        }
        
        bool MemoryProtector::detectDebugger() {
            try {
                // Check TracerPid in /proc/self/status
                std::ifstream statusFile("/proc/self/status");
                std::string line;
                
                while (std::getline(statusFile, line)) {
                    if (line.find("TracerPid:") == 0) {
                        std::istringstream iss(line);
                        std::string label;
                        int tracerPid;
                        iss >> label >> tracerPid;
                        return tracerPid != 0;
                    }
                }
            } catch (...) {
                return true; // Assume debugger present on error
            }
            
            return false;
        }
        
        bool MemoryProtector::detectTracer() {
            // Additional tracer detection methods can be implemented here
            return false;
        }
        
        bool MemoryProtector::isAddressValid(uintptr_t address) {
            // Basic address validation
            return address != 0 && address > 0x1000;
        }
        
        size_t MemoryProtector::getPageSize() {
            static size_t pageSize = sysconf(_SC_PAGESIZE);
            return pageSize;
        }
        
        uintptr_t MemoryProtector::alignToPageBoundary(uintptr_t address) {
            size_t pageSize = getPageSize();
            return (address / pageSize) * pageSize;
        }
        
        void MemoryProtector::reportTampering(const MemoryRegion& region, const std::string& details) {
            if (m_config.logTamperingEvents) {
                // Log tampering event (implement logging as needed)
            }
            
            notifyCallbacks(region, details);
        }
        
        void MemoryProtector::notifyCallbacks(const MemoryRegion& region, const std::string& details) {
            std::lock_guard<std::mutex> lock(m_callbackMutex);
            
            for (const auto& callback : m_callbacks) {
                try {
                    callback(region, details);
                } catch (...) {
                    // Ignore callback errors
                }
            }
        }
        
        MemoryProtector::ProtectionResult MemoryProtector::protectFunction(void* functionPtr, const std::string& functionName) {
            if (!functionPtr) {
                ProtectionResult result;
                result.success = false;
                result.errorMessage = "Null function pointer";
                return result;
            }
            
            uintptr_t address = reinterpret_cast<uintptr_t>(functionPtr);
            size_t functionSize = 64; // Estimate function size (could be improved)
            
            return protectRegion(address, functionSize, ProtectionType::HOOK_DETECTION, functionName);
        }
        
        // MemoryProtectorManager implementation
        MemoryProtector* MemoryProtectorManager::getInstance() {
            std::lock_guard<std::mutex> lock(s_instanceMutex);
            return s_instance.get();
        }
        
        bool MemoryProtectorManager::createInstance(const MemoryProtector::ProtectorConfig& config) {
            std::lock_guard<std::mutex> lock(s_instanceMutex);
            
            if (s_instance) {
                return false;  // Already exists
            }
            
            s_instance = std::make_unique<MemoryProtector>();
            return s_instance->initialize(config);
        }
        
        void MemoryProtectorManager::destroyInstance() {
            std::lock_guard<std::mutex> lock(s_instanceMutex);
            s_instance.reset();
        }
        
        // MemoryProtectionGuard implementation
        MemoryProtectionGuard::MemoryProtectionGuard(MemoryProtector* protector, uintptr_t address, 
                                                    size_t size, MemoryProtector::ProtectionType type)
            : m_protector(protector), m_address(address), m_isActive(false) {
            
            if (m_protector) {
                auto result = m_protector->protectRegion(address, size, type);
                m_isActive = result.success;
            }
        }
        
        MemoryProtectionGuard::~MemoryProtectionGuard() {
            release();
        }
        
        void MemoryProtectionGuard::release() {
            if (m_isActive && m_protector) {
                m_protector->unprotectRegion(m_address);
                m_isActive = false;
            }
        }
        
    } // namespace EnhancedAntiHook
} // namespace BearLoader
