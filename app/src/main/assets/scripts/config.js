/**
 * Bear-Mod Configuration
 *
 * This file contains the configuration for Bear-Mod.
 * Edit this file to customize the behavior of Bear-Mod.
 *
 * DISCLAIMER:
 * Bear-Mod is designed for security researchers, app developers, and educational purposes only.
 * Users must:
 * 1. Only analyze applications they own or have explicit permission to test
 * 2. Respect intellectual property rights and terms of service
 * 3. Use findings responsibly through proper disclosure channels
 * 4. Not use this tool to access unauthorized content or services
 */

const config = {
    // Core functionality
    enableStealth: true,         // Enable anti-detection measures
    bypassSignature: true,       // Bypass signature verification
    bypassSSLPinning: true,      // Bypass SSL pinning
    bypassRootDetection: true,   // Bypass root detection
    analyzeApp: true,            // Analyze the application

    // Logging
    logLevel: "info",            // debug, info, warn, error

    // Remote logging configuration
    remoteLogging: {
        enabled: false,          // Enable remote logging

        // Telegram configuration
        telegram: {
            enabled: false,
            botToken: "",        // Your Telegram bot token
            chatId: ""           // Your Telegram chat ID
        },

        // Discord configuration
        discord: {
            enabled: false,
            webhookUrl: ""       // Your Discord webhook URL
        },

        // File logging configuration
        file: {
            enabled: true,
            path: "/sdcard/bear-mod-logs.txt"
        },

        // Web UI configuration
        webUI: {
            enabled: false,      // Enable Web UI
            port: 8080,          // Port to run the Web UI on
            maxLogEntries: 1000, // Maximum number of log entries to keep
            enableRemoteCommands: false // Enable remote command execution (security risk!)
        }
    },

    // Analysis configuration
    analysis: {
        traceNativeCalls: true,  // Trace native function calls
        monitorNetwork: true,    // Monitor network traffic
        monitorFileAccess: true, // Monitor file access
        monitorCrypto: true,     // Monitor cryptographic operations

        // Classes to monitor (lowercase)
        monitorClasses: [
            "security",
            "crypto",
            "network",
            "http",
            "socket",
            "ssl",
            "tls",
            "cert"
        ]
    },

    // Non-root support configuration
    nonRoot: {
        enabled: true,           // Enable non-root injection capabilities
        gadgetMode: "stealth",   // stealth, balanced, fast

        // Obfuscation settings
        obfuscation: {
            enabled: true,
            hideFromPs: true,             // Hide from process lists
            hideFromMaps: true,           // Hide from memory maps
            spoofProcessName: "media_server", // Spoof process name
            encryptConfig: true           // Encrypt gadget configuration
        },

        // Injection configuration
        injection: {
            method: "gadget",        // gadget, spawn, attach
            autoStart: true,         // Auto-start injection
            delayMs: 2000,          // Delay before injection
            retryCount: 3,          // Retry attempts
            timeoutMs: 30000,       // Injection timeout
            validateTarget: true     // Validate target before injection
        },

        // KeyAuth integration for non-root
        keyAuth: {
            validateBeforeInjection: true,  // Validate session before injection
            sessionTokenRequired: true,     // Require KeyAuth session token
            licenseLevel: "premium",        // basic, premium, enterprise
            checkInterval: 300000,          // Session check interval (5 minutes)
            offlineGracePeriod: 3600000    // Offline grace period (1 hour)
        },

        // Security settings
        security: {
            antiTampering: true,     // Enable anti-tampering protection
            memoryProtection: true,  // Enable memory protection
            signatureCheck: true,    // Verify target app signature
            environmentCheck: true,  // Check for analysis environment
            threadMonitoring: true   // Monitor for suspicious threads
        },

        // Advanced stealth features
        stealth: {
            hideThreads: true,       // Hide Frida threads
            spoofMaps: true,         // Spoof memory maps
            hookDlopen: true,        // Hook dlopen to hide libraries
            patchAntiDebug: true,    // Patch anti-debug checks
            bypassIntegrityChecks: true // Bypass integrity checks
        }
    }
};

// Export configuration
module.exports = config;
