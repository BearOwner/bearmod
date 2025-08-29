/**
 * BearMod Frida Injection Script
 * Development mode injection for rapid prototyping
 */

console.log("[*] BearMod Frida injection script loaded");

// Configuration
const CONFIG = {
    enableLogging: true,
    hookMainActivity: true,
    hookGameActivity: true,
    hookNativeLibrary: true,
    targetLibrary: "bearmod"
};

/**
 * Utility functions
 */
function log(message) {
    if (CONFIG.enableLogging) {
        console.log(`[BearMod] ${message}`);
    }
}

function hookMethod(className, methodName, implementation) {
    try {
        let targetClass = Java.use(className);
        let originalMethod = targetClass[methodName];
        
        originalMethod.implementation = implementation;
        log(`✓ Hooked ${className}.${methodName}()`);
        return true;
    } catch (error) {
        log(`✗ Failed to hook ${className}.${methodName}(): ${error}`);
        return false;
    }
}

/**
 * Hook BearMod MainActivity (Debug App)
 */
function hookBearModMainActivity() {
    if (!CONFIG.hookMainActivity) return;
    
    hookMethod("com.bearmod.MainActivity", "onCreate", function(bundle) {
        log(`MainActivity.onCreate() called with bundle: ${bundle}`);
        
        // Call original method
        this.onCreate(bundle);
        
        // Additional debug logging
        log("BearMod MainActivity initialized");
    });
    
    // Hook other important MainActivity methods
    hookMethod("com.bearmod.MainActivity", "onResume", function() {
        log("MainActivity.onResume() called");
        this.onResume();
    });
}

/**
 * Hook Target Game Activity
 */
function hookTargetGameActivity() {
    if (!CONFIG.hookGameActivity) return;
    
    // Common game activity classes
    const gameActivityClasses = [
        "com.epicgames.ue4.GameActivity",
        "com.unity3d.player.UnityPlayerActivity",
        "com.tencent.tmgp.pubgmhd.MainActivity",
        "com.pubg.krmobile.MainActivity"
    ];
    
    gameActivityClasses.forEach(className => {
        try {
            hookMethod(className, "onCreate", function(bundle) {
                log(`${className}.onCreate() called with bundle: ${bundle}`);
                
                // Call original method
                this.onCreate(bundle);
                
                // Trigger BearMod injection after game activity is ready
                setTimeout(() => {
                    log("Game activity ready - triggering mod injection");
                    triggerModInjection();
                }, 1000);
            });
        } catch (error) {
            // Class not found - this is normal
        }
    });
}

/**
 * Hook System.loadLibrary to detect native library loading
 */
function hookNativeLibraryLoading() {
    if (!CONFIG.hookNativeLibrary) return;
    
    let System = Java.use("java.lang.System");
    let originalLoadLibrary = System.loadLibrary;
    
    originalLoadLibrary.implementation = function(libraryName) {
        log(`System.loadLibrary() called with: ${libraryName}`);
        
        // Call original method
        originalLoadLibrary.call(this, libraryName);
        
        // If it's our target library, hook native functions
        if (libraryName === CONFIG.targetLibrary) {
            log(`Target library ${libraryName} loaded - hooking native functions`);
            setTimeout(() => {
                Interceptor.detachAll();
                log("All hooks detached (cleanup)");
            }, 5 * 60 * 1000); // 5 min            
        }
    };
}

/**
 * Hook native functions after library is loaded
 */
function hookNativeFunctions() {
    try {
        // Hook JNI_OnLoad
        let jniOnLoad = Module.findExportByName("libbearmod.so", "JNI_OnLoad");
        if (jniOnLoad) {
            Interceptor.attach(jniOnLoad, {
                onEnter: function(args) {
                    log("JNI_OnLoad called");
                },
                onLeave: function(retval) {
                    log(`JNI_OnLoad returned: ${retval}`);
                }
            });
        }
        
        // Hook native initialization function
        let nativeInit = Module.findExportByName("libbearmod.so", "Java_com_bearmod_security_NativeLib_initialize");
        if (nativeInit) {
            Interceptor.attach(nativeInit, {
                onEnter: function(args) {
                    log("Native initialization called");
                },
                onLeave: function(retval) {
                    log(`Native initialization returned: ${retval}`);
                }
            });
        }
        
        log("Native function hooking completed");
        
    } catch (error) {
        log(`Error hooking native functions: ${error}`);
    }
}

/**
 * Trigger mod injection into target process
 */
function triggerModInjection() {
    try {
        setInterval(() => {
            log("Checking for mod updates...");
            triggerModInjection();
        }, 10 * 60 * 1000); // every 10 minutes
        
        log("Triggering mod injection...");
        
        // This would call back to the loader to start injection
        Java.choose("com.bearmod.patch.injection.HybridInjectionManager", {
            onMatch: function(instance) {
                log("Found HybridInjectionManager instance");
                // Could call methods on the manager here
            },
            onComplete: function() {
                log("Injection trigger completed");
            }
        });
        
    } catch (error) {
        log(`Error triggering injection: ${error}`);
    }
}

/**
 * Monitor for anti-detection bypass
 */
function setupAntiDetectionBypass() {
    try {
        // Hook common anti-Frida checks
        let Debug = Java.use("android.os.Debug");
        Debug.isDebuggerConnected.implementation = function() {
            log("Anti-debug check bypassed");
            return false;
        };
        
        // Hook file existence checks for Frida detection
        let File = Java.use("java.io.File");
        let originalExists = File.exists;
        File.exists.implementation = function() {
            let path = this.getAbsolutePath();
            
            // Block detection of Frida-related files
            if (path.includes("frida") || path.includes("gum") || path.includes("gadget")) {
                log(`Blocked file existence check: ${path}`);
                return false;
            }
            
            return originalExists.call(this);
        };
        
        log("Anti-detection bypass setup completed");
        
    } catch (error) {
        log(`Error setting up anti-detection bypass: ${error}`);
    }
}

/**
 * Main injection logic
 */
Java.perform(function() {
    log("Starting BearMod Frida injection...");
    
    // Setup anti-detection first
    setupAntiDetectionBypass();
    
    // Hook various components
    hookBearModMainActivity();
    hookTargetGameActivity();
    hookNativeLibraryLoading();
    
    log("BearMod injection setup completed");
});

// Export functions for external use
if (typeof module !== 'undefined') {
    module.exports = {
        hookMethod: hookMethod,
        triggerModInjection: triggerModInjection,
        log: log
    };
}
