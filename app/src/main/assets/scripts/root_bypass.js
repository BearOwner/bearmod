/**
 * BEAR-LOADER Advanced Anti-Detection Module v3.0
 * Integrated Multi-Layer Stealth System
 *
 * FEATURES:
 * ✅ Dynamic Root Detection Bypass (29 known root files)
 * ✅ Anti-Frida Detection (Process hiding, String obfuscation, File masking)
 * ✅ Runtime Command Blocking (su, ps, which, frida, magisk, busybox)
 * ✅ Native Function Hooking (fopen, strstr, access, ps)
 * ✅ Java Method Interception (Runtime.exec, ProcessBuilder)
 * ✅ Library-Specific Bypasses (RootBeer, RootTools, etc.)
 *
 * DISCLAIMER:
 * BEAR-LOADER is designed for security researchers, app developers, and educational purposes only.
 * Users must:
 * 1. Only analyze applications they own or have explicit permission to test
 * 2. Respect intellectual property rights and terms of service
 * 3. Use findings responsibly through proper disclosure channels
 * 4. Not use this tool to access unauthorized content or services
 *
 * Misuse of this tool may violate laws including but not limited to the Computer Fraud and Abuse Act,
 * Digital Millennium Copyright Act, and equivalent legislation in other jurisdictions.
 */

console.log("[*] BEAR-LOADER Advanced Anti-Detection Module v3.0 Loaded");

var BearBypass = {
    isActive: false,
    hookCount: 0,
    detectionAttempts: 0,
    
    rootFiles: [
        "/system/app/Superuser.apk",
        "/system/xbin/su",
        "/system/bin/su",
        "/sbin/su",
        "/system/su",
        "/system/bin/.ext/.su",
        "/system/xbin/daemonsu",
        "/system/etc/init.d/99SuperSUDaemon",
        "/dev/com.koushikdutta.superuser.daemon/",
        "/system/xbin/busybox",
        "/system/bin/busybox",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/local/su",
        "/su/bin/su",
        "/sbin/.magisk",
        "/sbin/.core",
        "/system/xbin/magisk",
        "/system/bin/magisk",
        "/data/adb/magisk",
        "/data/magisk",
        "/system/lib/libxposed_art.so",
        "/system/lib64/libxposed_art.so",
        "/system/framework/XposedBridge.jar",
        "/data/data/de.robv.android.xposed.installer",
        "/data/data/org.meowcat.edxposed.manager",
        "/data/data/top.canyie.dreamland.manager"
    ],
    
    init: function() {
        console.log("[*] Initializing BEAR-LOADER Bypass System...");
        this.isActive = true;
        this.setupDynamicHooks();
        this.setupKnownLibraryHooks();
        this.setupFileSystemHooks();
        this.setupCommandExecutionHooks();
        this.setupNativeAntiDetection();
        console.log("[+] BEAR-LOADER Bypass System Active - " + this.hookCount + " hooks installed");
    }
};

Java.perform(function() {
    console.log("[*] BEAR-LOADER Java VM initialized");
    
    BearBypass.setupDynamicHooks = function() {
        Java.enumerateLoadedClasses({
            onMatch: function(className) {
                if (className.includes("Security") ||
                    className.includes("Root") ||
                    className.includes("Emulator") ||
                    className.includes("Debug") ||
                    className.includes("AntiCheat") ||
                    className.includes("Integrity") ||
                    className.includes("Protection")) {

                    try {
                        var classObj = Java.use(className);

                        for (var method of classObj.class.getDeclaredMethods()) {
                            var methodName = method.getName();

                            if (methodName.includes("isRoot") ||
                                methodName.includes("checkRoot") ||
                                methodName.includes("detectRoot") ||
                                methodName.includes("hasRoot") ||
                                methodName.includes("isRooted") ||
                                methodName.includes("isSuperUser") ||
                                methodName.includes("isEmulator") ||
                                methodName.includes("isDebuggable") ||
                                methodName.includes("checkGameGuardian") ||
                                methodName.includes("detectModification") ||
                                methodName.includes("validateIntegrity")) {

                                console.log("[BEAR] Found detection method: " + className + "." + methodName);

                                if (method.getReturnType().getName() === "boolean") {
                                    try {
                                        classObj[methodName].implementation = function() {
                                            BearBypass.detectionAttempts++;
                                            console.log("[BEAR] Bypassed: " + className + "." + methodName + " (#" + BearBypass.detectionAttempts + ")");
                                            return false;
                                        };
                                        BearBypass.hookCount++;
                                    } catch (e) {
                                        // Silent fail
                                    }
                                }
                            }
                        }
                    } catch (e) {
                        // Skip inaccessible classes
                    }
                }
            },
            onComplete: function() {
                console.log("[BEAR] Dynamic hooks completed - " + BearBypass.hookCount + " installed");
            }
        });
    };
    
    BearBypass.setupKnownLibraryHooks = function() {
        // RootBeer
        try {
            var RootBeer = Java.use("com.scottyab.rootbeer.RootBeer");
            RootBeer.isRooted.implementation = function() {
                console.log("[BEAR] Bypassed RootBeer.isRooted()");
                return false;
            };
            BearBypass.hookCount++;
        } catch (e) {}

        // RootTools
        try {
            var RootTools = Java.use("com.stericson.RootTools.RootTools");
            RootTools.isRootAvailable.implementation = function() {
                console.log("[BEAR] Bypassed RootTools.isRootAvailable()");
                return false;
            };
            BearBypass.hookCount++;
        } catch (e) {}
    };
    
    BearBypass.setupFileSystemHooks = function() {
        var File = Java.use("java.io.File");
        File.exists.implementation = function() {
            var fileName = this.getAbsolutePath();

            for (var i = 0; i < BearBypass.rootFiles.length; i++) {
                if (fileName === BearBypass.rootFiles[i]) {
                    console.log("[BEAR] Bypassed file check: " + fileName);
                    return false;
                }
            }
            return this.exists();
        };
        BearBypass.hookCount++;
    };
    
    BearBypass.setupCommandExecutionHooks = function() {
        // Enhanced Runtime.exec hooks
        var Runtime = Java.use("java.lang.Runtime");
        Runtime.exec.overload("java.lang.String").implementation = function(cmd) {
            if (cmd.includes("su") || 
                cmd.includes("which") || 
                cmd.includes("frida") || 
                cmd.includes("ps") ||
                cmd.includes("busybox") ||
                cmd.includes("magisk")) {
                console.log("[BEAR] Bypassed Runtime.exec: " + cmd);
                return this.exec("echo");
            }
            return this.exec(cmd);
        };
        
        // Hook ProcessBuilder to prevent detection commands
        try {
            var ProcessBuilder = Java.use("java.lang.ProcessBuilder");
            ProcessBuilder.start.implementation = function() {
                var cmd = this.command.value.toString();
                if (cmd.includes("su") || 
                    cmd.includes("which") || 
                    cmd.includes("frida") || 
                    cmd.includes("ps") ||
                    cmd.includes("busybox") ||
                    cmd.includes("magisk")) {
                    console.log("[BEAR] Bypassed ProcessBuilder command: " + cmd);
                    this.command.value = Java.array('java.lang.String', ['echo']);
                }
                return this.start();
            };
            BearBypass.hookCount++;
        } catch (e) {
            console.log("[-] ProcessBuilder hook failed: " + e);
        }
        
        BearBypass.hookCount++;
    };
    
    BearBypass.init();
});

// Native Anti-Detection Module - Advanced Frida Hiding
BearBypass.setupNativeAntiDetection = function() {
    console.log("[*] Setting up native anti-detection measures");
    
    // Hide Frida process names from ps command
    try {
        var ps = Module.findExportByName(null, "ps");
        if (ps) {
            Interceptor.attach(ps, {
                onLeave: function(retval) {
                    if (retval.toInt32() !== 0) {
                        var output = Memory.readUtf8String(retval.toPointer());
                        if (output && output.includes("frida")) {
                            Memory.writeUtf8String(retval.toPointer(), 
                                output.replace(/frida\S*/g, "media_server"));
                            console.log("[BEAR] Masked frida processes in ps output");
                        }
                    }
                }
            });
            console.log("[+] Hooked ps command");
            BearBypass.hookCount++;
        }
    } catch (e) {
        console.log("[-] Failed to hook ps command: " + e);
    }
    
    // Hide Frida strings in memory using strstr
    try {
        var strstr = Module.findExportByName(null, "strstr");
        if (strstr) {
            Interceptor.attach(strstr, {
                onEnter: function(args) {
                    var haystack = Memory.readUtf8String(args[0]);
                    var needle = Memory.readUtf8String(args[1]);
                    if (needle && (
                        needle.includes("frida") || 
                        needle.includes("gum") || 
                        needle.includes("gadget") || 
                        needle.includes("script") ||
                        needle.includes("BEAR") ||
                        needle.includes("bear"))) {
                        args[1] = Memory.allocUtf8String("dummy");
                        console.log("[BEAR] Masked string search: " + needle);
                    }
                }
            });
            console.log("[+] Hooked strstr function");
            BearBypass.hookCount++;
        }
    } catch (e) {
        console.log("[-] Failed to hook strstr function: " + e);
    }
    
    // Hide Frida and suspicious files from fopen
    try {
        var fopen = Module.findExportByName(null, "fopen");
        if (fopen) {
            Interceptor.attach(fopen, {
                onEnter: function(args) {
                    var path = Memory.readUtf8String(args[0]);
                    if (path && (
                        path.includes("/proc/") || 
                        path.includes("/sys/") || 
                        path.includes("frida") || 
                        path.includes("gum") ||
                        path.includes("re.frida.server") ||
                        path.includes("tmp/frida") ||
                        path.includes("BEAR") ||
                        path.includes("bear"))) {
                        this.shouldModify = true;
                        console.log("[BEAR] Blocked file access: " + path);
                    }
                },
                onLeave: function(retval) {
                    if (this.shouldModify && !retval.isNull()) {
                        retval.replace(ptr(0)); // Return NULL for suspicious files
                    }
                }
            });
            console.log("[+] Hooked fopen function");
            BearBypass.hookCount++;
        }
    } catch (e) {
        console.log("[-] Failed to hook fopen function: " + e);
    }
    
    // Hook additional detection functions
    try {
        var access = Module.findExportByName(null, "access");
        if (access) {
            Interceptor.attach(access, {
                onEnter: function(args) {
                    var path = Memory.readUtf8String(args[0]);
                    if (path && (path.includes("frida") || path.includes("gum"))) {
                        console.log("[BEAR] Blocked access() call: " + path);
                        this.shouldBlock = true;
                    }
                },
                onLeave: function(retval) {
                    if (this.shouldBlock) {
                        retval.replace(-1); // Return -1 (file not found)
                    }
                }
            });
            console.log("[+] Hooked access function");
            BearBypass.hookCount++;
        }
    } catch (e) {
        console.log("[-] Failed to hook access function: " + e);
    }
    
    console.log("[*] Native anti-detection measures in place");
};

console.log("[BEAR] Advanced Anti-Detection Script Initialized"); 