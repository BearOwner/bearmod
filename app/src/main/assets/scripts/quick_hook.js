/**
 * Quick Hook - Simple Frida script for quick testing
 * 
 * This script provides a minimal set of hooks for testing purposes.
 * It's designed to be easy to understand and modify.
 */

console.log("[*] Quick Hook script loaded");

// Hook Java methods
Java.perform(function() {
    console.log("[*] Java.perform() called");
    
    // Hook MainActivity
    try {
        var MainActivity = Java.use("com.bearmod.MainActivity");
        
        MainActivity.onCreate.implementation = function(savedInstanceState) {
            console.log("[*] MainActivity.onCreate() called");
            
            // Call original implementation
            this.onCreate(savedInstanceState);
            
            console.log("[*] MainActivity.onCreate() completed");
        };
        
        console.log("[*] MainActivity.onCreate() hooked");
    } catch (e) {
        console.log("[!] Error hooking MainActivity: " + e);
    }
    
    // Hook System.loadLibrary to detect native library loading
    var System = Java.use("java.lang.System");
    System.loadLibrary.implementation = function(libraryName) {
        console.log("[*] Loading native library: " + libraryName);
        
        // Call original implementation
        this.loadLibrary(libraryName);
        
        console.log("[*] Library " + libraryName + " loaded successfully");
        
        // If it's our target library, hook native functions
        if (libraryName === "bearmod") {
            setTimeout(hookNativeFunctions, 500);
        }
    };
    
    console.log("[*] System.loadLibrary() hooked");
});

// Hook native functions
function hookNativeFunctions() {
    console.log("[*] Hooking native functions");
    
    // Find the bearmod module
    var bearmodModule = Process.findModuleByName("libbearmod.so");
    if (!bearmodModule) {
        console.log("[!] libbearmod.so module not found");
        return;
    }
    
    console.log("[*] Found libbearmod.so at base address: " + bearmodModule.base);
    
    // Enumerate exports
    var exports = bearmodModule.enumerateExports();
    console.log("[*] Found " + exports.length + " exported functions");
    
    // Hook interesting functions
    exports.forEach(function(exp) {
        if (exp.type === 'function' && 
            (exp.name.includes("JNI_") || 
             exp.name.includes("Java_") || 
             exp.name.includes("init"))) {
            
            console.log("[*] Hooking function: " + exp.name);
            
            Interceptor.attach(exp.address, {
                onEnter: function(args) {
                    console.log("[*] Called " + exp.name);
                },
                onLeave: function(retval) {
                    console.log("[*] " + exp.name + " returned: " + retval);
                }
            });
        }
    });
}

console.log("[*] Quick Hook script initialized");
