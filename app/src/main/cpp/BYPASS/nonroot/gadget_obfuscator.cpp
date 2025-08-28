#include "gadget_obfuscator.h"
#include <fstream>
#include <sstream>
#include <iomanip>
#include <cstring>
#include <ctime>
#include <android/log.h>

#define LOG_TAG "GadgetObfuscator"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace BearLoader {
    namespace NonRoot {
        
        GadgetObfuscator::GadgetObfuscator() {
            // Initialize with default configuration
            m_config = {};
            m_randomGenerator.seed(static_cast<unsigned int>(std::time(nullptr)));
            m_obfuscationKey = "BearMod_Default_Key_2024";
        }
        
        GadgetObfuscator::GadgetObfuscator(const ObfuscationConfig& config) : m_config(config) {
            m_randomGenerator.seed(static_cast<unsigned int>(std::time(nullptr)));
            m_obfuscationKey = m_config.obfuscationSeed + "_Key";
        }
        
        GadgetObfuscator::~GadgetObfuscator() {
            clearObfuscationKey();
        }
        
        bool GadgetObfuscator::initialize(const ObfuscationConfig& config) {
            m_config = config;
            m_obfuscationKey = m_config.obfuscationSeed + "_Key";
            LOGI("GadgetObfuscator initialized with strength level: %d", m_config.obfuscationStrength);
            return true;
        }
        
        void GadgetObfuscator::updateConfig(const ObfuscationConfig& config) {
            m_config = config;
            m_obfuscationKey = m_config.obfuscationSeed + "_Key";
        }
        
        GadgetObfuscator::ObfuscationConfig GadgetObfuscator::getConfig() const {
            return m_config;
        }
        
        std::string GadgetObfuscator::generateObfuscatedFileName(const std::string& originalName) {
            if (!m_config.enableFileNameObfuscation) {
                return originalName;
            }
            
            // Generate a random filename that looks innocent
            std::vector<std::string> innocentNames = {
                "libmedia.so", "libutils.so", "liblog.so", "libcutils.so",
                "libbase.so", "libhardware.so", "libsystem.so"
            };
            
            std::uniform_int_distribution<> dist(0, innocentNames.size() - 1);
            return innocentNames[dist(m_randomGenerator)];
        }
        
        std::vector<uint8_t> GadgetObfuscator::obfuscateFileContent(const std::vector<uint8_t>& content) {
            if (!m_config.enableContentObfuscation) {
                return content;
            }
            
            std::vector<uint8_t> obfuscated = content;
            
            // Apply XOR obfuscation
            xorObfuscate(obfuscated, m_obfuscationKey);
            
            // Apply advanced obfuscation if strength is high
            if (m_config.obfuscationStrength >= 3) {
                applyAdvancedObfuscation(obfuscated);
            }
            
            return obfuscated;
        }
        
        std::vector<uint8_t> GadgetObfuscator::deobfuscateFileContent(const std::vector<uint8_t>& obfuscatedContent) {
            std::vector<uint8_t> deobfuscated = obfuscatedContent;
            
            // Reverse advanced obfuscation if applied
            if (m_config.obfuscationStrength >= 3) {
                reverseAdvancedObfuscation(deobfuscated);
            }
            
            // Reverse XOR obfuscation
            xorDeobfuscate(deobfuscated, m_obfuscationKey);
            
            return deobfuscated;
        }
        
        void GadgetObfuscator::xorObfuscate(std::vector<uint8_t>& data, const std::string& key) {
            if (key.empty() || data.empty()) return;
            
            for (size_t i = 0; i < data.size(); ++i) {
                data[i] ^= key[i % key.length()];
            }
        }
        
        void GadgetObfuscator::xorDeobfuscate(std::vector<uint8_t>& data, const std::string& key) {
            // XOR is symmetric, so deobfuscation is the same as obfuscation
            xorObfuscate(data, key);
        }
        
        void GadgetObfuscator::applyAdvancedObfuscation(std::vector<uint8_t>& data) {
            // Simple byte shuffling for advanced obfuscation
            if (data.size() < 2) return;
            
            // Reverse every 4 bytes
            for (size_t i = 0; i + 3 < data.size(); i += 4) {
                std::reverse(data.begin() + i, data.begin() + i + 4);
            }
        }
        
        void GadgetObfuscator::reverseAdvancedObfuscation(std::vector<uint8_t>& data) {
            // Reverse the advanced obfuscation (same operation in this case)
            applyAdvancedObfuscation(data);
        }
        
        GadgetObfuscator::ObfuscationResult GadgetObfuscator::obfuscateFile(const std::string& inputPath, const std::string& outputPath) {
            ObfuscationResult result;
            result.originalPath = inputPath;
            
            try {
                // Read input file
                std::ifstream inputFile(inputPath, std::ios::binary);
                if (!inputFile.is_open()) {
                    result.errorMessage = "Failed to open input file: " + inputPath;
                    return result;
                }
                
                std::vector<uint8_t> fileContent((std::istreambuf_iterator<char>(inputFile)),
                                                std::istreambuf_iterator<char>());
                inputFile.close();
                
                // Obfuscate content
                result.obfuscatedData = obfuscateFileContent(fileContent);
                
                // Write obfuscated file
                std::ofstream outputFile(outputPath, std::ios::binary);
                if (!outputFile.is_open()) {
                    result.errorMessage = "Failed to create output file: " + outputPath;
                    return result;
                }
                
                outputFile.write(reinterpret_cast<const char*>(result.obfuscatedData.data()),
                                result.obfuscatedData.size());
                outputFile.close();
                
                result.success = true;
                result.obfuscatedPath = outputPath;
                
                LOGI("File obfuscated successfully: %s -> %s", inputPath.c_str(), outputPath.c_str());
                
            } catch (const std::exception& e) {
                result.errorMessage = "Obfuscation failed: " + std::string(e.what());
                LOGE("Obfuscation error: %s", e.what());
            }
            
            return result;
        }
        
        GadgetObfuscator::ObfuscationResult GadgetObfuscator::deobfuscateFile(const std::string& obfuscatedPath, const std::string& outputPath) {
            ObfuscationResult result;
            result.originalPath = obfuscatedPath;
            
            try {
                // Read obfuscated file
                std::ifstream inputFile(obfuscatedPath, std::ios::binary);
                if (!inputFile.is_open()) {
                    result.errorMessage = "Failed to open obfuscated file: " + obfuscatedPath;
                    return result;
                }
                
                std::vector<uint8_t> obfuscatedContent((std::istreambuf_iterator<char>(inputFile)),
                                                      std::istreambuf_iterator<char>());
                inputFile.close();
                
                // Deobfuscate content
                std::vector<uint8_t> deobfuscatedData = deobfuscateFileContent(obfuscatedContent);
                
                // Write deobfuscated file
                std::ofstream outputFile(outputPath, std::ios::binary);
                if (!outputFile.is_open()) {
                    result.errorMessage = "Failed to create output file: " + outputPath;
                    return result;
                }
                
                outputFile.write(reinterpret_cast<const char*>(deobfuscatedData.data()),
                                deobfuscatedData.size());
                outputFile.close();
                
                result.success = true;
                result.obfuscatedPath = outputPath;
                result.obfuscatedData = deobfuscatedData;
                
                LOGI("File deobfuscated successfully: %s -> %s", obfuscatedPath.c_str(), outputPath.c_str());
                
            } catch (const std::exception& e) {
                result.errorMessage = "Deobfuscation failed: " + std::string(e.what());
                LOGE("Deobfuscation error: %s", e.what());
            }
            
            return result;
        }
        
        std::vector<uint8_t> GadgetObfuscator::obfuscateData(const std::vector<uint8_t>& data) {
            return obfuscateFileContent(data);
        }
        
        std::vector<uint8_t> GadgetObfuscator::deobfuscateData(const std::vector<uint8_t>& obfuscatedData) {
            return deobfuscateFileContent(obfuscatedData);
        }
        
        std::string GadgetObfuscator::obfuscateText(const std::string& text) {
            std::vector<uint8_t> textData(text.begin(), text.end());
            std::vector<uint8_t> obfuscated = obfuscateData(textData);
            return std::string(obfuscated.begin(), obfuscated.end());
        }
        
        std::string GadgetObfuscator::deobfuscateText(const std::string& obfuscatedText) {
            std::vector<uint8_t> obfuscatedData(obfuscatedText.begin(), obfuscatedText.end());
            std::vector<uint8_t> deobfuscated = deobfuscateData(obfuscatedData);
            return std::string(deobfuscated.begin(), deobfuscated.end());
        }
        
        std::string GadgetObfuscator::generateRandomString(size_t length) {
            const std::string chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            std::uniform_int_distribution<> dist(0, chars.size() - 1);
            
            std::string result;
            result.reserve(length);
            
            for (size_t i = 0; i < length; ++i) {
                result += chars[dist(m_randomGenerator)];
            }
            
            return result;
        }
        
        void GadgetObfuscator::clearObfuscationKey() {
            // Clear the obfuscation key from memory for security
            std::fill(m_obfuscationKey.begin(), m_obfuscationKey.end(), 0);
            m_obfuscationKey.clear();
        }
        
        bool GadgetObfuscator::isObfuscated(const std::vector<uint8_t>& data) {
            // Simple heuristic: check for patterns that indicate obfuscation
            if (data.size() < 16) return false;
            
            // Check entropy - obfuscated data should have higher entropy
            std::array<int, 256> frequency = {};
            for (uint8_t byte : data) {
                frequency[byte]++;
            }
            
            int nonZeroCount = 0;
            for (int count : frequency) {
                if (count > 0) nonZeroCount++;
            }
            
            // If more than 50% of possible byte values are present, likely obfuscated
            return nonZeroCount > 128;
        }
        
        // Namespace utility functions
        namespace ObfuscationUtils {
            std::string quickObfuscate(const std::string& input, const std::string& key) {
                std::string result = input;
                for (size_t i = 0; i < result.length(); ++i) {
                    result[i] ^= key[i % key.length()];
                }
                return result;
            }
            
            std::string quickDeobfuscate(const std::string& obfuscated, const std::string& key) {
                return quickObfuscate(obfuscated, key); // XOR is symmetric
            }
            
            bool isFileObfuscated(const std::string& filePath) {
                std::ifstream file(filePath, std::ios::binary);
                if (!file.is_open()) return false;
                
                std::vector<uint8_t> sample(1024);
                file.read(reinterpret_cast<char*>(sample.data()), sample.size());
                sample.resize(file.gcount());
                file.close();
                
                GadgetObfuscator obfuscator;
                return obfuscator.isObfuscated(sample);
            }
            
            std::string generateSecureFileName() {
                GadgetObfuscator obfuscator;
                return "lib" + obfuscator.generateRandomString(8) + ".so";
            }
        }
        
    } // namespace NonRoot
} // namespace BearLoader
