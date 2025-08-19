/**
 * BearMod Analyzer - Comprehensive Frida script for analyzing the BearMod app
 * 
 * This script provides extensive instrumentation capabilities for:
 * - Hooking Java methods
 * - Tracing native function calls
 * - Analyzing SDK behavior
 * - Monitoring memory access
 * - Intercepting network traffic
 */

// Configuration
const config = {
    debug: true,
    traceNativeCalls: true,
    monitorJavaClasses: true,
    interceptNetwork: true,
    monitorFileAccess: true,
    logLevel: 'info', // 'debug', 'info', 'warn', 'error'
};

// Logging utilities
const Log = {
    d: function(message) {
        if (config.logLevel === 'debug' || config.debug) {
            console.log(`[D] ${message}`);
        }
    },
    i: function(message) {
        if (config.logLevel === 'debug' || config.logLevel === 'info' || config.debug) {
            console.log(`[I] ${message}`);
        }
    },
    w: function(message) {
        if (config.logLevel === 'debug' || config.logLevel === 'info' || config.logLevel === 'warn' || config.debug) {
            console.log(`[W] ${message}`);
        }
    },
    e: function(message) {
        console.log(`[E] ${message}`);
    },
    highlight: function(message) {
        console.log(`\n[*] ======== ${message} ========\n`);
    }
};

// Utility functions
const Utils = {
    // Convert ArrayBuffer to hex string
    byteArrayToHex: function(arrayBuffer) {
        return Array.from(new Uint8Array(arrayBuffer))
            .map(b => b.toString(16).padStart(2, '0'))
            .join('');
    },
    
    // Convert hex string to ASCII if printable, otherwise keep hex
    hexToAscii: function(hex) {
        let str = '';
        for (let i = 0; i < hex.length; i += 2) {
            const charCode = parseInt(hex.substr(i, 2), 16);
            if (charCode >= 32 && charCode <= 126) { // Printable ASCII
                str += String.fromCharCode(charCode);
            } else {
                return hex; // Not all printable, return original hex
            }
        }
        return str;
    },
    
    // Format arguments for logging
    formatArgs: function(args, types) {
        let result = [];
        for (let i = 0; i < args.length; i++) {
            if (types && types[i]) {
                switch (types[i]) {
                    case 'pointer':
                        result.push(`ptr(${args[i]})`);
                        break;
                    case 'string':
                        const str = Memory.readUtf8String(args[i]);
                        result.push(`"${str}"`);
                        break;
                    case 'buffer':
                        const size = args[i+1] || 32; // Assume next arg is size, or use 32 bytes
                        const buffer = Memory.readByteArray(args[i], size);
                        const hex = this.byteArrayToHex(buffer);
                        result.push(`buffer(${hex})`);
                        break;
                    default:
                        result.push(args[i]);
                }
            } else {
                result.push(args[i]);
            }
        }
        return result.join(', ');
    }
};

// Main initialization
function initialize() {
    Log.highlight("BearMod Analyzer Starting");
    
    // Hook Java methods when Java VM is available
    if (Java.available) {
        Java.perform(hookJavaMethods);
    } else {
        Log.e("Java is not available");
    }
    
    // Hook native functions
    if (config.traceNativeCalls) {
        hookNativeFunctions();
    }
    
    Log.highlight("BearMod Analyzer Initialized");
}

// Hook Java methods
function hookJavaMethods() {
    Log.i("Setting up Java hooks");
    
    // Hook MainActivity
    try {
        const MainActivity = Java.use("com.bearmod.MainActivity");
        
        // Hook onCreate method
        MainActivity.onCreate.implementation = function(savedInstanceState) {
            Log.i("MainActivity.onCreate() called");
            
            // Call original implementation
            this.onCreate(savedInstanceState);
            
            Log.i("MainActivity.onCreate() completed");
        };
        
        // Hook onResume method
        MainActivity.onResume.implementation = function() {
            Log.i("MainActivity.onResume() called");
            
            // Call original implementation
            this.onResume();
            
            Log.i("MainActivity.onResume() completed");
        };
        
        Log.i("MainActivity hooks installed");
    } catch (e) {
        Log.e(`Failed to hook MainActivity: ${e}`);
    }
    
    // Hook NativeUtils class if it exists
    try {
        const NativeUtils = Java.use("com.bearmod.NativeUtils");
        
        // Hook native method loading
        if (NativeUtils.getVersion) {
            NativeUtils.getVersion.implementation = function() {
                Log.i("NativeUtils.getVersion() called");
                const result = this.getVersion();
                Log.i(`NativeUtils.getVersion() returned: ${result}`);
                return result;
            };
        }
        
        if (NativeUtils.initialize) {
            NativeUtils.initialize.implementation = function(context) {
                Log.i("NativeUtils.initialize() called");
                const result = this.initialize(context);
                Log.i(`NativeUtils.initialize() returned: ${result}`);
                return result;
            };
        }
        
        Log.i("NativeUtils hooks installed");
    } catch (e) {
        Log.d(`NativeUtils class not found or failed to hook: ${e}`);
    }
    
    // Monitor System.loadLibrary calls
    const System = Java.use("java.lang.System");
    System.loadLibrary.implementation = function(libraryName) {
        Log.highlight(`Loading native library: ${libraryName}`);
        
        // Call original implementation
        this.loadLibrary(libraryName);
        
        // If it's our target library, hook its exports
        if (libraryName === "bearmod") {
            setTimeout(function() {
                hookBearModLibrary();
            }, 500); // Give it some time to load
        }
        
        Log.i(`Library ${libraryName} loaded successfully`);
    };
    
    // Monitor network activity
    if (config.interceptNetwork) {
        hookNetworkCalls();
    }
    
    // Monitor file access
    if (config.monitorFileAccess) {
        hookFileAccess();
    }
    
    // Enumerate loaded classes if enabled
    if (config.monitorJavaClasses) {
        enumerateLoadedClasses();
    }
}

// Hook native functions
function hookNativeFunctions() {
    Log.i("Setting up native function hooks");
    
    // Hook dlopen to detect library loading
    Interceptor.attach(Module.findExportByName(null, "dlopen"), {
        onEnter: function(args) {
            const path = Memory.readUtf8String(args[0]);
            this.path = path;
            Log.i(`dlopen called with path: ${path}`);
        },
        onLeave: function(retval) {
            Log.i(`dlopen for ${this.path} returned: ${retval}`);
            
            // If it's our target library, hook its exports
            if (this.path && this.path.includes("libbearmod.so")) {
                setTimeout(function() {
                    hookBearModLibrary();
                }, 500); // Give it some time to load
            }
        }
    });
    
    // Hook malloc/free to monitor memory allocations
    const malloc = Module.findExportByName(null, "malloc");
    if (malloc) {
        Interceptor.attach(malloc, {
            onEnter: function(args) {
                this.size = args[0].toInt32();
            },
            onLeave: function(retval) {
                if (this.size > 1024 * 1024) { // Only log allocations > 1MB
                    Log.d(`malloc(${this.size}) returned: ${retval}`);
                }
            }
        });
    }
    
    // Hook memcpy to monitor large memory copies
    const memcpy = Module.findExportByName(null, "memcpy");
    if (memcpy) {
        Interceptor.attach(memcpy, {
            onEnter: function(args) {
                this.dst = args[0];
                this.src = args[1];
                this.size = args[2].toInt32();
                
                if (this.size > 1024 * 1024) { // Only log copies > 1MB
                    Log.d(`memcpy(${this.dst}, ${this.src}, ${this.size})`);
                }
            }
        });
    }
}

// Hook the BearMod library specifically
function hookBearModLibrary() {
    Log.highlight("Hooking BearMod library functions");
    
    const bearmodModule = Process.findModuleByName("libbearmod.so");
    if (!bearmodModule) {
        Log.e("libbearmod.so module not found");
        return;
    }
    
    Log.i(`Found libbearmod.so at base address: ${bearmodModule.base}`);
    
    // Enumerate exports
    const exports = bearmodModule.enumerateExports();
    Log.i(`Found ${exports.length} exported functions`);
    
    exports.forEach(exp => {
        if (exp.type === 'function') {
            Log.d(`Export: ${exp.name} at ${exp.address}`);
            
            // Hook specific functions of interest
            if (exp.name.includes("JNI_") || 
                exp.name.includes("Java_") || 
                exp.name.includes("native_")) {
                
                Interceptor.attach(exp.address, {
                    onEnter: function(args) {
                        Log.i(`Called ${exp.name}`);
                        this.args = args;
                    },
                    onLeave: function(retval) {
                        Log.i(`${exp.name} returned: ${retval}`);
                    }
                });
                
                Log.i(`Hooked ${exp.name}`);
            }
        }
    });
    
    // Look for specific functions by pattern
    findAndHookFunctions(bearmodModule);
}

// Find and hook functions by pattern matching
function findAndHookFunctions(module) {
    Log.i("Searching for functions by pattern");
    
    // Example: Find functions that might be related to SDK initialization
    const initPattern = "48 89 5C 24 ?? 48 89 74 24 ?? 57 48 83 EC ?? 48 8B D9 48 8B F2 33 FF";
    Memory.scan(module.base, module.size, initPattern, {
        onMatch: function(address, size) {
            Log.i(`Found potential init function at ${address}`);
            
            Interceptor.attach(address, {
                onEnter: function(args) {
                    Log.i(`Called potential init function at ${address}`);
                    this.args = args;
                },
                onLeave: function(retval) {
                    Log.i(`Potential init function returned: ${retval}`);
                }
            });
        },
        onComplete: function() {
            Log.d("Pattern scanning completed");
        }
    });
}

// Hook network calls
function hookNetworkCalls() {
    Log.i("Setting up network hooks");
    
    // Hook URL connection
    try {
        const URL = Java.use("java.net.URL");
        URL.openConnection.overload().implementation = function() {
            const url = this.toString();
            Log.i(`Opening connection to: ${url}`);
            return this.openConnection();
        };
        
        // Hook HttpURLConnection
        const HttpURLConnection = Java.use("java.net.HttpURLConnection");
        HttpURLConnection.connect.implementation = function() {
            const url = this.getURL().toString();
            const method = this.getRequestMethod();
            Log.i(`HTTP ${method} request to ${url}`);
            this.connect();
        };
        
        // Hook OkHttp if available
        try {
            const OkHttpClient = Java.use("okhttp3.OkHttpClient");
            const Request = Java.use("okhttp3.Request");
            
            OkHttpClient.newCall.overload("okhttp3.Request").implementation = function(request) {
                const url = request.url().toString();
                const method = request.method();
                Log.i(`OkHttp ${method} request to ${url}`);
                return this.newCall(request);
            };
        } catch (e) {
            Log.d("OkHttp not found or failed to hook");
        }
        
        Log.i("Network hooks installed");
    } catch (e) {
        Log.e(`Failed to hook network calls: ${e}`);
    }
}

// Hook file access
function hookFileAccess() {
    Log.i("Setting up file access hooks");
    
    try {
        // Hook FileInputStream
        const FileInputStream = Java.use("java.io.FileInputStream");
        FileInputStream.$init.overload("java.io.File").implementation = function(file) {
            const path = file.getAbsolutePath();
            Log.i(`Reading file: ${path}`);
            return this.$init(file);
        };
        
        // Hook FileOutputStream
        const FileOutputStream = Java.use("java.io.FileOutputStream");
        FileOutputStream.$init.overload("java.io.File", "boolean").implementation = function(file, append) {
            const path = file.getAbsolutePath();
            Log.i(`Writing file: ${path} (append: ${append})`);
            return this.$init(file, append);
        };
        
        Log.i("File access hooks installed");
    } catch (e) {
        Log.e(`Failed to hook file access: ${e}`);
    }
}

// Enumerate loaded classes
function enumerateLoadedClasses() {
    Log.i("Enumerating loaded classes");
    
    Java.enumerateLoadedClasses({
        onMatch: function(className) {
            if (className.includes("bearmod") || 
                className.includes("sdk") || 
                className.includes("game")) {
                Log.i(`Found interesting class: ${className}`);
                
                // Try to hook all methods of interesting classes
                try {
                    const javaClass = Java.use(className);
                    const methods = javaClass.class.getDeclaredMethods();
                    
                    for (let i = 0; i < methods.length; i++) {
                        const method = methods[i];
                        const methodName = method.getName();
                        
                        // Skip common methods
                        if (methodName === "$init" || 
                            methodName === "toString" || 
                            methodName === "hashCode" || 
                            methodName === "equals") {
                            continue;
                        }
                        
                        Log.d(`Found method: ${className}.${methodName}`);
                        
                        // Try to hook the method
                        try {
                            // This is a simplified approach and might not work for all methods
                            // due to overloads and parameter types
                            if (javaClass[methodName] && javaClass[methodName].overloads.length > 0) {
                                javaClass[methodName].overloads[0].implementation = function() {
                                    Log.i(`Called ${className}.${methodName}`);
                                    
                                    // Call original implementation with arguments
                                    const result = this[methodName].apply(this, arguments);
                                    
                                    Log.i(`${className}.${methodName} returned: ${result}`);
                                    return result;
                                };
                                
                                Log.d(`Hooked ${className}.${methodName}`);
                            }
                        } catch (e) {
                            Log.d(`Could not hook ${className}.${methodName}: ${e}`);
                        }
                    }
                } catch (e) {
                    Log.d(`Could not analyze class ${className}: ${e}`);
                }
            }
        },
        onComplete: function() {
            Log.i("Class enumeration completed");
        }
    });
}

// Start the script
initialize();
