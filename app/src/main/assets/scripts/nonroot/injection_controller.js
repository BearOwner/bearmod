/**
 * BearMod Injection Controller
 * 
 * Controls script injection timing and target app detection
 * Integrates with Gadget Manager for secure injection
 */

const config = require('../config.js');

console.log("[*] BearMod Injection Controller Loaded");

/**
 * Injection Controller Class
 * Manages script injection lifecycle and target detection
 */
class InjectionController {
    constructor() {
        this.targetPackage = null;
        this.targetActivity = null;
        this.injectionState = 'idle'; // idle, detecting, injecting, active, error
        this.retryCount = 0;
        this.maxRetries = config.nonRoot.injection.retryCount;
        this.injectionTimeout = config.nonRoot.injection.timeoutMs;
        this.autoStart = config.nonRoot.injection.autoStart;
        
        // Callbacks
        this.progressCallback = null;
        this.completionCallback = null;
        
        // Injection tracking
        this.injectionStartTime = null;
        this.lastError = null;
        this.injectedScripts = [];
        
        console.log("[*] InjectionController initialized");
    }
    
    /**
     * Set target application
     */
    setTarget(packageName, activityName = null) {
        console.log("[*] Setting target: " + packageName);
        
        this.targetPackage = packageName;
        this.targetActivity = activityName;
        
        // Validate target if enabled
        if (config.nonRoot.injection.validateTarget) {
            return this.validateTarget();
        }
        
        return true;
    }
    
    /**
     * Validate target application
     */
    validateTarget() {
        console.log("[*] Validating target application...");
        
        try {
            if (!this.targetPackage) {
                throw new Error("No target package specified");
            }
            
            // Check if target app is installed
            const isInstalled = this.isAppInstalled(this.targetPackage);
            if (!isInstalled) {
                throw new Error("Target application not installed: " + this.targetPackage);
            }
            
            // Verify app signature if enabled
            if (config.nonRoot.security.signatureCheck) {
                const signatureValid = this.verifyAppSignature(this.targetPackage);
                if (!signatureValid) {
                    console.log("[-] Warning: Target app signature verification failed");
                    // Don't fail completely, just warn
                }
            }
            
            console.log("[+] Target validation successful");
            return true;
            
        } catch (error) {
            console.log("[-] Target validation failed: " + error.message);
            this.lastError = error.message;
            return false;
        }
    }
    
    /**
     * Check if app is installed
     */
    isAppInstalled(packageName) {
        try {
            // Use Android API to check if package is installed
            Java.perform(function() {
                const PackageManager = Java.use("android.content.pm.PackageManager");
                const ActivityThread = Java.use("android.app.ActivityThread");
                const context = ActivityThread.currentApplication();
                
                if (context) {
                    const pm = context.getPackageManager();
                    try {
                        const packageInfo = pm.getPackageInfo(packageName, 0);
                        return packageInfo !== null;
                    } catch (e) {
                        return false;
                    }
                }
                return false;
            });
            
            return true; // Assume installed if we can't check
        } catch (error) {
            console.log("[-] Failed to check app installation: " + error.message);
            return true; // Assume installed on error
        }
    }
    
    /**
     * Verify app signature
     */
    verifyAppSignature(packageName) {
        try {
            // Implement signature verification
            // This would check against known good signatures
            console.log("[*] Verifying signature for: " + packageName);
            
            // For now, return true (implement actual verification as needed)
            return true;
        } catch (error) {
            console.log("[-] Signature verification failed: " + error.message);
            return false;
        }
    }
    
    /**
     * Start injection process
     */
    async startInjection(progressCallback = null, completionCallback = null) {
        console.log("[*] Starting injection process...");
        
        this.progressCallback = progressCallback;
        this.completionCallback = completionCallback;
        this.injectionState = 'detecting';
        this.injectionStartTime = Date.now();
        this.retryCount = 0;
        
        try {
            // Check if gadget manager is ready
            if (!global.BearModGadgetManager || !global.BearModGadgetManager.initialized) {
                throw new Error("Gadget Manager not initialized");
            }
            
            this.reportProgress(10, "Gadget Manager ready");
            
            // Wait for target app if auto-start is enabled
            if (this.autoStart) {
                const targetDetected = await this.waitForTargetApp();
                if (!targetDetected) {
                    throw new Error("Target app not detected within timeout");
                }
            }
            
            this.reportProgress(30, "Target app detected");
            
            // Perform pre-injection security checks
            const securityOk = this.performPreInjectionChecks();
            if (!securityOk) {
                throw new Error("Pre-injection security checks failed");
            }
            
            this.reportProgress(50, "Security checks passed");
            
            // Load and inject scripts
            await this.injectScripts();
            
            this.reportProgress(80, "Scripts injected");
            
            // Verify injection success
            const injectionVerified = this.verifyInjection();
            if (!injectionVerified) {
                throw new Error("Injection verification failed");
            }
            
            this.reportProgress(100, "Injection completed successfully");
            this.injectionState = 'active';
            
            if (this.completionCallback) {
                this.completionCallback(true, "Injection completed successfully");
            }
            
            console.log("[+] Injection process completed successfully");
            return true;
            
        } catch (error) {
            console.log("[-] Injection failed: " + error.message);
            this.lastError = error.message;
            this.injectionState = 'error';
            
            // Retry if configured
            if (this.retryCount < this.maxRetries) {
                this.retryCount++;
                console.log("[*] Retrying injection (" + this.retryCount + "/" + this.maxRetries + ")");
                
                // Wait before retry
                setTimeout(() => {
                    this.startInjection(progressCallback, completionCallback);
                }, 2000);
                
                return false;
            }
            
            if (this.completionCallback) {
                this.completionCallback(false, error.message);
            }
            
            return false;
        }
    }
    
    /**
     * Wait for target app to be running
     */
    async waitForTargetApp() {
        console.log("[*] Waiting for target app: " + this.targetPackage);
        
        return new Promise((resolve) => {
            const startTime = Date.now();
            const checkInterval = 1000; // Check every second
            
            const checkTarget = () => {
                const elapsed = Date.now() - startTime;
                
                if (elapsed > this.injectionTimeout) {
                    console.log("[-] Timeout waiting for target app");
                    resolve(false);
                    return;
                }
                
                // Check if target app is running
                const isRunning = this.isAppRunning(this.targetPackage);
                if (isRunning) {
                    console.log("[+] Target app detected: " + this.targetPackage);
                    resolve(true);
                    return;
                }
                
                // Continue checking
                setTimeout(checkTarget, checkInterval);
            };
            
            checkTarget();
        });
    }
    
    /**
     * Check if app is currently running
     */
    isAppRunning(packageName) {
        try {
            // Check if app process is running
            Java.perform(function() {
                const ActivityManager = Java.use("android.app.ActivityManager");
                const ActivityThread = Java.use("android.app.ActivityThread");
                const context = ActivityThread.currentApplication();
                
                if (context) {
                    const am = context.getSystemService("activity");
                    const runningApps = am.getRunningAppProcesses();
                    
                    for (let i = 0; i < runningApps.size(); i++) {
                        const processInfo = runningApps.get(i);
                        if (processInfo.processName.value === packageName) {
                            return true;
                        }
                    }
                }
                return false;
            });
            
            return false; // Default to not running
        } catch (error) {
            console.log("[-] Failed to check if app is running: " + error.message);
            return false;
        }
    }
    
    /**
     * Perform pre-injection security checks
     */
    performPreInjectionChecks() {
        console.log("[*] Performing pre-injection security checks...");
        
        try {
            // Check gadget manager status
            const gadgetStatus = global.BearModGadgetManager.getStatus();
            if (!gadgetStatus.keyAuthValidated) {
                console.log("[-] KeyAuth validation required");
                return false;
            }
            
            // Perform periodic security check
            global.BearModGadgetManager.performPeriodicSecurityCheck();
            
            // Check for high detection risk
            if (global.BearModGadgetManager.isDetectionRiskHigh()) {
                console.log("[-] High detection risk detected");
                return false;
            }
            
            console.log("[+] Pre-injection security checks passed");
            return true;
            
        } catch (error) {
            console.log("[-] Pre-injection security checks failed: " + error.message);
            return false;
        }
    }
    
    /**
     * Inject scripts into target
     */
    async injectScripts() {
        console.log("[*] Injecting scripts...");
        
        try {
            // Load core BearMod scripts
            const coreScripts = [
                '../bearmod_analyzer.js',
                '../bypass-ssl-pinning.js',
                '../bypass-signature-verification.js'
            ];
            
            for (const scriptPath of coreScripts) {
                try {
                    console.log("[*] Loading script: " + scriptPath);
                    const script = require(scriptPath);
                    this.injectedScripts.push(scriptPath);
                    console.log("[+] Script loaded: " + scriptPath);
                } catch (error) {
                    console.log("[-] Failed to load script " + scriptPath + ": " + error.message);
                }
            }
            
            console.log("[+] Script injection completed");
            
        } catch (error) {
            console.log("[-] Script injection failed: " + error.message);
            throw error;
        }
    }
    
    /**
     * Verify injection success
     */
    verifyInjection() {
        console.log("[*] Verifying injection...");
        
        try {
            // Check if scripts are loaded and active
            if (this.injectedScripts.length === 0) {
                console.log("[-] No scripts were injected");
                return false;
            }
            
            // Verify gadget is still active
            if (!global.BearModGadgetManager.injectionActive) {
                console.log("[-] Gadget is not active");
                return false;
            }
            
            console.log("[+] Injection verification successful");
            return true;
            
        } catch (error) {
            console.log("[-] Injection verification failed: " + error.message);
            return false;
        }
    }
    
    /**
     * Report progress to callback
     */
    reportProgress(percentage, message) {
        console.log("[*] Progress: " + percentage + "% - " + message);
        
        if (this.progressCallback) {
            this.progressCallback(percentage, message);
        }
    }
    
    /**
     * Stop injection
     */
    stopInjection() {
        console.log("[*] Stopping injection...");
        
        this.injectionState = 'idle';
        this.injectedScripts = [];
        this.lastError = null;
        
        console.log("[+] Injection stopped");
    }
    
    /**
     * Get injection status
     */
    getStatus() {
        return {
            state: this.injectionState,
            targetPackage: this.targetPackage,
            targetActivity: this.targetActivity,
            retryCount: this.retryCount,
            injectedScripts: this.injectedScripts,
            lastError: this.lastError,
            injectionTime: this.injectionStartTime ? Date.now() - this.injectionStartTime : 0
        };
    }
}

// Create global instance
const injectionController = new InjectionController();

// Export for use by other scripts
global.BearModInjectionController = injectionController;

console.log("[+] BearMod Injection Controller ready");

// Export module
module.exports = {
    InjectionController: InjectionController,
    instance: injectionController
};
