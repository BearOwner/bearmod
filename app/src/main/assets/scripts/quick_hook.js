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

    var bearmodModule = Process.findModuleByName("libbearmod.so");
    if (!bearmodModule) return console.log("[!] libbearmod.so not found");

    bearmodModule.enumerateExports().forEach(function(exp) {
        if (exp.type === 'function' && /JNI_|init/.test(exp.name)) {
            console.log("[*] Hooking: " + exp.name);

            Interceptor.attach(exp.address, {
                onEnter: function(args) {
                    try {
                        console.log("[*] " + exp.name + " called");
                        // Example: log first arg if string
                        if (args[0] && Memory.readUtf8String(args[0])) {
                            console.log("    arg0: " + Memory.readUtf8String(args[0]));
                        }
                    } catch (e) { }
                },
                onLeave: function(retval) {
                    console.log("[*] " + exp.name + " returned: " + retval);
                }
            });
        }
    });
}


console.log("[*] Quick Hook script initialized");
