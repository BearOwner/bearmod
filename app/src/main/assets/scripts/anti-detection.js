/**
 * Bear-Mod Anti-Detection Module
 * 
 * This script implements stealth techniques to avoid Frida detection.
 * 
 * DISCLAIMER:
 * Bear-Mod is designed for security researchers, app developers, and educational purposes only.
 * Users must:
 * 1. Only analyze applications they own or have explicit permission to test
 * 2. Respect intellectual property rights and terms of service
 * 3. Use findings responsibly through proper disclosure channels
 * 4. Not use this tool to access unauthorized content or services
 */

console.log("[*] Anti-Detection Module Loaded");

// Import configuration
const config = require('./config.js');

function setupAntiDetection() {
    console.log("[*] Setting up anti-detection measures");

    // Setup non-root specific anti-detection if enabled
    if (config.nonRoot && config.nonRoot.enabled) {
        setupNonRootAntiDetection();
    }
    
    // Hide Frida process names
    try {
        const ps = Module.findExportByName(null, "ps");
        if (ps) {
            Interceptor.attach(ps, {
                onLeave: function(retval) {
                    if (retval.toInt32() !== 0) {
                        const output = Memory.readUtf8String(retval.toPointer());
                        if (output && output.includes("frida")) {
                            Memory.writeUtf8String(retval.toPointer(), output.replace(/frida\S*/g, "media_server"));
                        }
                    }
                }
            });
            console.log("[+] Hooked ps command");
        }
    } catch (e) {
        console.log("[-] Failed to hook ps command: " + e);
    }
    
    // Hide Frida strings in memory
    try {
        const strstr = Module.findExportByName(null, "strstr");
        if (strstr) {
            Interceptor.attach(strstr, {
                onEnter: function(args) {
                    const haystack = Memory.readUtf8String(args[0]);
                    const needle = Memory.readUtf8String(args[1]);
                    if (needle && (
                        needle.includes("frida") || 
                        needle.includes("gum") || 
                        needle.includes("gadget") || 
                        needle.includes("script"))) {
                        args[1] = Memory.allocUtf8String("dummy");
                    }
                }
            });
            console.log("[+] Hooked strstr function");
        }
    } catch (e) {
        console.log("[-] Failed to hook strstr function: " + e);
    }
    
    // Hide Frida related files
    try {
        const fopen = Module.findExportByName(null, "fopen");
        if (fopen) {
            Interceptor.attach(fopen, {
                onEnter: function(args) {
                    const path = Memory.readUtf8String(args[0]);
                    if (path && (
                        path.includes("/proc/") || 
                        path.includes("/sys/") || 
                        path.includes("frida") || 
                        path.includes("gum"))) {
                        this.shouldModify = true;
                    }
                },
                onLeave: function(retval) {
                    if (this.shouldModify && !retval.isNull()) {
                        retval.replace(ptr(0)); // Return NULL for suspicious files
                    }
                }
            });
            console.log("[+] Hooked fopen function");
        }
    } catch (e) {
        console.log("[-] Failed to hook fopen function: " + e);
    }
    
    // Hook Java-based detection methods
    Java.perform(function() {
        try {
            // Hook Runtime.exec to prevent detection commands
            const Runtime = Java.use("java.lang.Runtime");
            Runtime.exec.overload("java.lang.String").implementation = function(cmd) {
                if (cmd.includes("su") || 
                    cmd.includes("which") || 
                    cmd.includes("frida") || 
                    cmd.includes("ps")) {
                    console.log("[+] Blocked suspicious command: " + cmd);
                    return this.exec("echo");
                }
                return this.exec(cmd);
            };
            console.log("[+] Hooked Runtime.exec");
            
            // Hook ProcessBuilder to prevent detection commands
            const ProcessBuilder = Java.use("java.lang.ProcessBuilder");
            ProcessBuilder.start.implementation = function() {
                const cmd = this.command.value.toString();
                if (cmd.includes("su") || 
                    cmd.includes("which") || 
                    cmd.includes("frida") || 
                    cmd.includes("ps")) {
                    console.log("[+] Blocked suspicious ProcessBuilder command: " + cmd);
                    this.command.value = Java.array('java.lang.String', ['echo']);
                }
                return this.start();
            };
            console.log("[+] Hooked ProcessBuilder.start");
        } catch (e) {
            console.log("[-] Failed to hook Java detection methods: " + e);
        }
    });
    
    console.log("[*] Anti-detection measures in place");
}

/**
 * Non-Root Specific Anti-Detection
 * Enhanced stealth for Frida Gadget injection
 */
function setupNonRootAntiDetection() {
    console.log("[*] Setting up non-root anti-detection measures");

    if (config.nonRoot.obfuscation.hideFromMaps) {
        hideLibraryFromMaps();
    }

    if (config.nonRoot.stealth.hideThreads) {
        hideFridaThreads();
    }

    if (config.nonRoot.stealth.hookDlopen) {
        hookDlopenForStealth();
    }

    if (config.nonRoot.stealth.spoofMaps) {
        spoofMemoryMaps();
    }

    if (config.nonRoot.obfuscation.spoofProcessName) {
        spoofProcessInformation();
    }
}

/**
 * Hide obfuscated library from memory maps
 */
function hideLibraryFromMaps() {
    try {
        const fopen = Module.findExportByName(null, "fopen");
        if (fopen) {
            Interceptor.attach(fopen, {
                onEnter: function(args) {
                    const filename = Memory.readUtf8String(args[0]);
                    if (filename && filename.includes("/proc/") && filename.includes("/maps")) {
                        this.isMapsFile = true;
                    }
                },
                onLeave: function(retval) {
                    if (this.isMapsFile && retval.toInt32() !== 0) {
                        // Hook fgets to filter out our library
                        const fgets = Module.findExportByName(null, "fgets");
                        if (fgets) {
                            Interceptor.attach(fgets, {
                                onLeave: function(retval) {
                                    if (retval.toInt32() !== 0) {
                                        const line = Memory.readUtf8String(retval);
                                        if (line && line.includes(config.nonRoot.obfuscation.libraryName)) {
                                            // Replace with innocent library name
                                            const cleanLine = line.replace(config.nonRoot.obfuscation.libraryName, "libmedia.so");
                                            Memory.writeUtf8String(retval, cleanLine);
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
            });
            console.log("[+] Hooked fopen for maps hiding");
        }
    } catch (e) {
        console.log("[-] Failed to hide library from maps: " + e);
    }
}

/**
 * Hide Frida-related threads
 */
function hideFridaThreads() {
    try {
        const pthread_create = Module.findExportByName(null, "pthread_create");
        if (pthread_create) {
            Interceptor.attach(pthread_create, {
                onEnter: function(args) {
                    // Monitor thread creation and hide Frida threads
                    this.threadFunc = args[2];
                },
                onLeave: function(retval) {
                    if (retval.toInt32() === 0 && this.threadFunc) {
                        // Successfully created thread, monitor its name
                        try {
                            const pthread_setname_np = Module.findExportByName(null, "pthread_setname_np");
                            if (pthread_setname_np) {
                                Interceptor.attach(pthread_setname_np, {
                                    onEnter: function(args) {
                                        const threadName = Memory.readUtf8String(args[1]);
                                        if (threadName && (threadName.includes("frida") || threadName.includes("gum"))) {
                                            // Change to innocent name
                                            Memory.writeUtf8String(args[1], "media_thread");
                                        }
                                    }
                                });
                            }
                        } catch (e) {
                            // Ignore errors
                        }
                    }
                }
            });
            console.log("[+] Hooked pthread_create for thread hiding");
        }
    } catch (e) {
        console.log("[-] Failed to hide Frida threads: " + e);
    }
}

// Export the functions
module.exports = {
    setupAntiDetection: setupAntiDetection,
    setupNonRootAntiDetection: setupNonRootAntiDetection
};
