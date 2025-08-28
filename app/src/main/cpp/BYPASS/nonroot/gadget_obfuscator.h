#ifndef BEARMOD_GADGET_OBFUSCATOR_H
#define BEARMOD_GADGET_OBFUSCATOR_H

#include <string>
#include <vector>
#include <memory>
#include <functional>
#include <random>
#include <algorithm>

namespace BearLoader {
    namespace NonRoot {
        
        /**
         * Gadget Obfuscator
         * Provides obfuscation capabilities for Frida Gadget files and configurations
         */
        class GadgetObfuscator {
        public:
            // Obfuscation configuration
            struct ObfuscationConfig {
                bool enableFileNameObfuscation = true;
                bool enableContentObfuscation = true;
                bool enablePathObfuscation = true;
                bool enableConfigObfuscation = true;
                std::string obfuscationSeed = "BearMod2024";
                int obfuscationStrength = 3; // 1-5 scale
            };
            
            // Obfuscation result
            struct ObfuscationResult {
                bool success = false;
                std::string obfuscatedPath;
                std::string originalPath;
                std::vector<uint8_t> obfuscatedData;
                std::string errorMessage;
            };
            
        private:
            ObfuscationConfig m_config;
            std::mt19937 m_randomGenerator;
            std::string m_obfuscationKey;
            
            // Internal obfuscation methods
            std::string generateObfuscatedFileName(const std::string& originalName);
            std::vector<uint8_t> obfuscateFileContent(const std::vector<uint8_t>& content);
            std::vector<uint8_t> deobfuscateFileContent(const std::vector<uint8_t>& obfuscatedContent);
            std::string obfuscateString(const std::string& input);
            std::string deobfuscateString(const std::string& obfuscatedInput);
            
            // XOR-based obfuscation
            void xorObfuscate(std::vector<uint8_t>& data, const std::string& key);
            void xorDeobfuscate(std::vector<uint8_t>& data, const std::string& key);
            
            // Advanced obfuscation methods
            void applyAdvancedObfuscation(std::vector<uint8_t>& data);
            void reverseAdvancedObfuscation(std::vector<uint8_t>& data);
            
        public:
            GadgetObfuscator();
            explicit GadgetObfuscator(const ObfuscationConfig& config);
            ~GadgetObfuscator();
            
            // Configuration
            bool initialize(const ObfuscationConfig& config);
            void updateConfig(const ObfuscationConfig& config);
            ObfuscationConfig getConfig() const;
            
            // File obfuscation
            ObfuscationResult obfuscateFile(const std::string& inputPath, const std::string& outputPath);
            ObfuscationResult deobfuscateFile(const std::string& obfuscatedPath, const std::string& outputPath);
            
            // Memory obfuscation
            std::vector<uint8_t> obfuscateData(const std::vector<uint8_t>& data);
            std::vector<uint8_t> deobfuscateData(const std::vector<uint8_t>& obfuscatedData);
            
            // String obfuscation
            std::string obfuscateText(const std::string& text);
            std::string deobfuscateText(const std::string& obfuscatedText);
            
            // Configuration obfuscation
            std::string obfuscateGadgetConfig(const std::string& configJson);
            std::string deobfuscateGadgetConfig(const std::string& obfuscatedConfig);
            
            // Path obfuscation
            std::string generateObfuscatedPath(const std::string& basePath, const std::string& fileName);
            std::string getOriginalFileName(const std::string& obfuscatedName);
            
            // Utility methods
            bool isObfuscated(const std::vector<uint8_t>& data);
            bool isObfuscatedFile(const std::string& filePath);
            std::string generateRandomString(size_t length);
            
            // Security methods
            void clearObfuscationKey();
            bool validateObfuscationIntegrity(const std::vector<uint8_t>& data);
        };
        
        /**
         * Obfuscation utilities
         */
        namespace ObfuscationUtils {
            // Quick obfuscation functions
            std::string quickObfuscate(const std::string& input, const std::string& key = "default");
            std::string quickDeobfuscate(const std::string& obfuscated, const std::string& key = "default");
            
            // File utilities
            bool isFileObfuscated(const std::string& filePath);
            std::string generateSecureFileName();
            
            // Validation
            bool validateObfuscationKey(const std::string& key);
            int calculateObfuscationStrength(const std::vector<uint8_t>& data);
        }
        
    } // namespace NonRoot
} // namespace BearLoader

#endif // BEARMOD_GADGET_OBFUSCATOR_H
