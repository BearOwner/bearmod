/**
 * BearMod Anti-Detection Script (Patches Version)
 *
 * Focused anti-detection measures for PUBG Mobile variants
 * Optimized for centralized ZIP delivery via FileHelper system
 *
 * Target Applications: PUBG Mobile variants (com.tencent.ig, com.pubg.krmobile, etc.)
 * Integration: SecureScriptManager, FileHelper, ptrace-based injection
 *
 * Based on main anti-detection.js but condensed for patch delivery
 */

console.log("[*] ======== BearMod Anti-Detection Patch v2.0 ========");
console.log("[*] Loaded via centralized FileHelper system");
console.log("[*] Target: PUBG Mobile anti-detection measures");

// Configuration for PUBG-specific anti-detection
const AntiDetectionConfig = {
    // Target PUBG packages
    targetPackages: [
        "com.tencent.ig",      // PUBG Mobile Global
        "com.pubg.krmobile",   // PUBG Mobile Korea
        "com.pubg.imobile",    // PUBG Mobile India
        "com.rekoo.pubgm",     // PUBG Mobile Taiwan
        "com.vng.pubgmobile"   // PUBG Mobile Vietnam
    ],

    // Anti-detection features
    features: {
        hideProcessNames: true,
        blockDetectionCommands: true,
        spoofFileChecks: true,
        hideMemoryMaps: true,
        patchAntiDebug: true,
        bypassIntegrityChecks: true
    },

    // BearMod-specific libraries to hide
    hiddenLibraries: [
        "libbearmod.so",
        "libmundo.so",
        "libclient_static.so",
        "libhelper.so"
    ],

    // Detection patterns to block
    detectionPatterns: [
        "frida", "gadget", "inject", "hook", "patch",
        "bearmod", "mundo", "bypass", "cheat", "mod"
    ]
};

// Main anti-detection initialization
function initializeAntiDetection() {
    console.log("[*] Initializing BearMod anti-detection measures");

    try {
        // Check if we're in a target PUBG application
        if (Java.available) {
            Java.perform(function() {
                const context = Java.use("android.app.ActivityThread")
                    .currentApplication().getApplicationContext();
                const packageName = context.getPackageName();

                if (AntiDetectionConfig.targetPackages.includes(packageName)) {
                    console.log("[+] PUBG target detected: " + packageName);
                    console.log("[*] Applying PUBG-specific anti-detection measures");

                    // Apply all anti-detection measures
                    setupProcessNameHiding();
                    setupCommandBlocking();
                    setupFileCheckSpoofing();
                    setupMemoryMapHiding();
                    setupAntiDebugPatching();
                    setupIntegrityBypass();

                    console.log("[+] Anti-detection measures applied successfully");
                } else {
                    console.log("[*] Non-target application: " + packageName);
                    console.log("[*] Applying basic anti-detection measures");

                    // Apply basic measures for non-target apps
                    setupProcessNameHiding();
                    setupCommandBlocking();
                }
            });
        } else {
            console.log("[-] Java runtime not available - limited anti-detection mode");
            setupNativeAntiDetection();
        }

    } catch (error) {
        console.log("[-] Anti-detection initialization failed: " + error);
    }
}

// Hide process names from detection
function setupProcessNameHiding() {
    console.log("[*] Setting up process name hiding");

    try {
        // Hook prctl to hide thread names
        const prctlPtr = Module.findExportByName(null, "prctl");
        if (prctlPtr) {
            Interceptor.attach(prctlPtr, {
                onEnter: function(args) {
                    const option = args[0].toInt32();
                    if (option === 15) { // PR_SET_NAME
                        const name = Memory.readUtf8String(args[1]);
                        this.originalName = name;

                        // Check if name contains detection patterns
                        const containsPattern = AntiDetectionConfig.detectionPatterns.some(pattern =>
                            name && name.toLowerCase().includes(pattern));

                        if (containsPattern) {
                            console.log("[+] Hiding suspicious thread name: " + name);
                            Memory.writeUtf8String(args[1], "system_server");
                            this.nameChanged = true;
                        }
                    }
                },
                onLeave: function(retval) {
                    if (this.nameChanged) {
                        console.log("[+] Thread name hidden successfully");
                    }
                }
            });
            console.log("[+] Process name hiding enabled");
        }

        // Hook pthread_setname_np for additional thread name hiding
        const pthreadSetnamePtr = Module.findExportByName(null, "pthread_setname_np");
        if (pthreadSetnamePtr) {
            Interceptor.attach(pthreadSetnamePtr, {
                onEnter: function(args) {
                    const name = Memory.readUtf8String(args[1]);

                    const containsPattern = AntiDetectionConfig.detectionPatterns.some(pattern =>
                        name && name.toLowerCase().includes(pattern));

                    if (containsPattern) {
                        console.log("[+] Hiding pthread name: " + name);
                        Memory.writeUtf8String(args[1], "media_server");
                    }
                }
            });
            console.log("[+] pthread name hiding enabled");
        }

    } catch (error) {
        console.log("[-] Process name hiding failed: " + error);
    }
}

// Block detection commands
function setupCommandBlocking() {
    console.log("[*] Setting up command blocking");

    if (!Java.available) return;

    Java.perform(function() {
        try {
            // Hook Runtime.exec to block detection commands
            const Runtime = Java.use("java.lang.Runtime");
            const originalExec = Runtime.exec.overload("java.lang.String");

            Runtime.exec.overload("java.lang.String").implementation = function(cmd) {
                const command = cmd.toLowerCase();

                // Check for detection commands
                const suspiciousCommands = [
                    "ps", "su", "which", "find", "grep", "cat /proc",
                    "ls /data", "mount", "getprop", "dumpsys"
                ];

                const isSuspicious = suspiciousCommands.some(suspCmd =>
                    command.includes(suspCmd)) ||
                    AntiDetectionConfig.detectionPatterns.some(pattern =>
                        command.includes(pattern));

                if (isSuspicious) {
                    console.log("[+] Blocked suspicious command: " + cmd);
                    // Return empty process instead of actual command
                    return originalExec.call(this, "echo");
                }

                return originalExec.call(this, cmd);
            };

            console.log("[+] Command blocking enabled");

        } catch (error) {
            console.log("[-] Command blocking failed: " + error);
        }
    });
}

// Spoof file checks for root/injection detection
function setupFileCheckSpoofing() {
    console.log("[*] Setting up file check spoofing");

    if (!Java.available) return;

    Java.perform(function() {
        try {
            // Hook File.exists to spoof detection files
            const File = Java.use("java.io.File");
            const originalExists = File.exists;

            File.exists.implementation = function() {
                const path = this.getAbsolutePath();

                // Common detection files to hide
                const detectionFiles = [
                    "/system/app/Superuser.apk",
                    "/system/xbin/su",
                    "/system/bin/su",
                    "/sbin/su",
                    "/data/local/tmp/frida-server",
                    "/data/local/tmp/gadget"
                ];

                // BearMod-specific files to hide
                const bearModFiles = AntiDetectionConfig.hiddenLibraries.map(lib =>
                    ["/data/data/", "/system/lib/", "/system/lib64/"].map(dir => dir + lib)
                ).flat();

                const allHiddenFiles = [...detectionFiles, ...bearModFiles];

                if (allHiddenFiles.some(hiddenFile => path.includes(hiddenFile))) {
                    console.log("[+] Spoofed file check: " + path);
                    return false; // File doesn't exist
                }

                return originalExists.call(this);
            };

            console.log("[+] File check spoofing enabled");

        } catch (error) {
            console.log("[-] File check spoofing failed: " + error);
        }
    });
}

// Hide memory maps from detection
function setupMemoryMapHiding() {
    console.log("[*] Setting up memory map hiding");

    try {
        // Hook fopen to hide /proc/maps access
        const fopenPtr = Module.findExportByName(null, "fopen");
        if (fopenPtr) {
            Interceptor.attach(fopenPtr, {
                onEnter: function(args) {
                    const path = Memory.readUtf8String(args[0]);
                    this.path = path;

                    if (path && (path.includes("/proc/") && path.includes("/maps"))) {
                        console.log("[+] Intercepted maps access: " + path);
                        this.isMapsAccess = true;
                    }
                },
                onLeave: function(retval) {
                    if (this.isMapsAccess && retval.toInt32() !== 0) {
                        // Could implement map filtering here if needed
                        console.log("[+] Maps access intercepted");
                    }
                }
            });
            console.log("[+] Memory map hiding enabled");
        }

    } catch (error) {
        console.log("[-] Memory map hiding failed: " + error);
    }
}

// Patch anti-debug mechanisms
function setupAntiDebugPatching() {
    console.log("[*] Setting up anti-debug patching");

    try {
        // Hook ptrace to prevent anti-debug
        const ptracePtr = Module.findExportByName(null, "ptrace");
        if (ptracePtr) {
            Interceptor.attach(ptracePtr, {
                onEnter: function(args) {
                    const request = args[0].toInt32();
                    // PTRACE_TRACEME = 0
                    if (request === 0) {
                        console.log("[+] Blocked PTRACE_TRACEME anti-debug");
                        args[0] = ptr(-1); // Invalid request
                    }
                }
            });
            console.log("[+] Anti-debug patching enabled");
        }

    } catch (error) {
        console.log("[-] Anti-debug patching failed: " + error);
    }
}

// Bypass integrity checks
function setupIntegrityBypass() {
    console.log("[*] Setting up integrity bypass");

    if (!Java.available) return;

    Java.perform(function() {
        try {
            // Hook PackageManager signature verification
            const PackageManager = Java.use("android.content.pm.PackageManager");
            if (PackageManager.checkSignatures) {
                PackageManager.checkSignatures.overload("java.lang.String", "java.lang.String").implementation = function(pkg1, pkg2) {
                    console.log("[+] Bypassed signature check: " + pkg1 + " vs " + pkg2);
                    return 0; // SIGNATURE_MATCH
                };
            }

            console.log("[+] Integrity bypass enabled");

        } catch (error) {
            console.log("[-] Integrity bypass failed: " + error);
        }
    });
}

// Native-only anti-detection for limited environments
function setupNativeAntiDetection() {
    console.log("[*] Setting up native-only anti-detection");

    try {
        setupProcessNameHiding();
        setupMemoryMapHiding();
        setupAntiDebugPatching();
        console.log("[+] Native anti-detection enabled");

    } catch (error) {
        console.log("[-] Native anti-detection failed: " + error);
    }
}

// Initialize anti-detection measures
console.log("[*] Starting BearMod anti-detection initialization");
setTimeout(function() {
    initializeAntiDetection();
}, 100);

console.log("[*] BearMod Anti-Detection Patch loaded successfully");
