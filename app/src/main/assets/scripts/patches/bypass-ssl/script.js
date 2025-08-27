/**
 * BearMod SSL Pinning Bypass Script (Patches Version)
 *
 * Bypasses SSL/TLS certificate pinning for PUBG Mobile variants
 * Optimized for centralized ZIP delivery via FileHelper system
 *
 * Target Applications: PUBG Mobile variants (com.tencent.ig, com.pubg.krmobile, etc.)
 * Integration: SecureScriptManager, FileHelper, ptrace-based injection
 *
 * Based on main bypass-ssl.js but condensed for patch delivery
 */

console.log("[*] ======== BearMod SSL Bypass Patch v2.0 ========");
console.log("[*] Loaded via centralized FileHelper ZIP delivery");
console.log("[*] Target: PUBG Mobile SSL pinning bypass");

// Configuration for PUBG-specific SSL bypass
const SSLBypassConfig = {
    // Target PUBG packages
    targetPackages: [
        "com.tencent.ig",      // PUBG Mobile Global
        "com.pubg.krmobile",   // PUBG Mobile Korea
        "com.pubg.imobile",    // PUBG Mobile India
        "com.rekoo.pubgm",     // PUBG Mobile Taiwan
        "com.vng.pubgmobile"   // PUBG Mobile Vietnam
    ],

    // Bypass features
    features: {
        okHttpBypass: true,
        trustManagerBypass: true,
        hostnameVerifierBypass: true,
        certificatePinnerBypass: true,
        sslContextBypass: true
    },

    // Common PUBG domains to monitor
    pubgDomains: [
        "pubgmobile.com",
        "tencent.com",
        "gameloop.com",
        "pubg.com",
        "krafton.com"
    ]
};

// SSL bypass state tracking
const SSLBypassState = {
    targetInfo: {
        packageName: null,
        isTargetApp: false
    },

    bypassedConnections: [],
    pinnedDomains: [],

    // Statistics
    startTime: Date.now(),
    hooksInstalled: 0,
    bypassesTriggered: 0
};

// Main SSL bypass initialization
function initializeSSLBypass() {
    console.log("[*] Initializing BearMod SSL bypass");

    try {
        // Detect environment
        detectEnvironment();

        // Initialize bypass hooks
        if (Java.available) {
            Java.perform(function() {
                setupSSLBypass();
            });
        } else {
            console.log("[-] Java runtime not available - SSL bypass disabled");
        }

        console.log("[+] BearMod SSL bypass initialized successfully");

    } catch (error) {
        console.log("[-] SSL bypass initialization failed: " + error);
    }
}

// Detect current environment and target
function detectEnvironment() {
    console.log("[*] Detecting environment for SSL bypass");

    try {
        if (Java.available) {
            Java.perform(function() {
                try {
                    const context = Java.use("android.app.ActivityThread")
                        .currentApplication().getApplicationContext();
                    const packageName = context.getPackageName();

                    SSLBypassState.targetInfo.packageName = packageName;
                    SSLBypassState.targetInfo.isTargetApp = SSLBypassConfig.targetPackages.includes(packageName);

                    console.log("[*] Package: " + packageName);

                    if (SSLBypassState.targetInfo.isTargetApp) {
                        console.log("[+] PUBG target detected: " + packageName);
                        console.log("[*] Applying PUBG-specific SSL bypasses");
                    } else {
                        console.log("[*] Non-target application - applying basic SSL bypasses");
                    }

                } catch (e) {
                    console.log("[-] Could not detect package: " + e);
                }
            });
        }

    } catch (error) {
        console.log("[-] Environment detection failed: " + error);
    }
}

// Setup SSL bypass hooks
function setupSSLBypass() {
    console.log("[*] Setting up SSL bypass hooks");

    try {
        // Bypass OkHttp certificate pinning
        if (SSLBypassConfig.features.okHttpBypass) {
            bypassOkHttpPinning();
        }

        // Bypass TrustManager verification
        if (SSLBypassConfig.features.trustManagerBypass) {
            bypassTrustManager();
        }

        // Bypass hostname verification
        if (SSLBypassConfig.features.hostnameVerifierBypass) {
            bypassHostnameVerifier();
        }

        console.log("[+] SSL bypass hooks setup complete");

    } catch (error) {
        console.log("[-] SSL bypass setup failed: " + error);
    }
}

// Bypass OkHttp certificate pinning
function bypassOkHttpPinning() {
    console.log("[*] Setting up OkHttp certificate pinning bypass");

    try {
        // Hook OkHttp3 CertificatePinner
        try {
            const CertificatePinner = Java.use("okhttp3.CertificatePinner");
            const originalCheck = CertificatePinner.check.overload('java.lang.String', 'java.util.List');

            CertificatePinner.check.overload('java.lang.String', 'java.util.List').implementation = function(hostname, peerCertificates) {
                console.log("[+] Bypassed OkHttp3 certificate pinning for: " + hostname);

                // Check if it's a PUBG domain
                const isPubgDomain = SSLBypassConfig.pubgDomains.some(domain =>
                    hostname.includes(domain));

                if (isPubgDomain) {
                    console.log("[+] PUBG domain detected: " + hostname);
                }

                SSLBypassState.bypassedConnections.push({
                    type: "OkHttp3_CertificatePinner",
                    hostname: hostname,
                    isPubgDomain: isPubgDomain,
                    timestamp: Date.now()
                });

                SSLBypassState.bypassesTriggered++;

                // Skip certificate pinning check - just return without throwing
                return;
            };
            SSLBypassState.hooksInstalled++;

            console.log("[+] OkHttp3 certificate pinning bypass enabled");

        } catch (e) {
            console.log("[-] OkHttp3 CertificatePinner not found");
        }

    } catch (error) {
        console.log("[-] OkHttp certificate pinning bypass failed: " + error);
    }
}

// Bypass TrustManager verification
function bypassTrustManager() {
    console.log("[*] Setting up TrustManager bypass");

    try {
        // Hook TrustManagerImpl
        try {
            const TrustManagerImpl = Java.use("com.android.org.conscrypt.TrustManagerImpl");
            const originalVerifyChain = TrustManagerImpl.verifyChain;

            TrustManagerImpl.verifyChain.implementation = function(untrustedChain, trustAnchorChain, host, clientAuth, ocspData, tlsSctData) {
                console.log("[+] Bypassed TrustManagerImpl.verifyChain for: " + host);

                // Check if it's a PUBG domain
                const isPubgDomain = SSLBypassConfig.pubgDomains.some(domain =>
                    host && host.includes(domain));

                if (isPubgDomain) {
                    console.log("[+] PUBG domain detected in TrustManager: " + host);
                }

                SSLBypassState.bypassedConnections.push({
                    type: "TrustManagerImpl_verifyChain",
                    hostname: host,
                    isPubgDomain: isPubgDomain,
                    timestamp: Date.now()
                });

                SSLBypassState.bypassesTriggered++;

                // Return the original chain to bypass verification
                return untrustedChain;
            };
            SSLBypassState.hooksInstalled++;

            console.log("[+] TrustManagerImpl bypass enabled");

        } catch (e) {
            console.log("[-] TrustManagerImpl not found");
        }

        // Hook X509TrustManager
        try {
            const X509TrustManager = Java.use("javax.net.ssl.X509TrustManager");

            if (X509TrustManager.checkServerTrusted) {
                X509TrustManager.checkServerTrusted.implementation = function(chain, authType) {
                    console.log("[+] Bypassed X509TrustManager.checkServerTrusted");

                    SSLBypassState.bypassedConnections.push({
                        type: "X509TrustManager_checkServerTrusted",
                        authType: authType,
                        timestamp: Date.now()
                    });

                    SSLBypassState.bypassesTriggered++;

                    // Do nothing - bypass the check
                    return;
                };
                SSLBypassState.hooksInstalled++;
            }

            console.log("[+] X509TrustManager bypass enabled");

        } catch (e) {
            console.log("[-] X509TrustManager not found");
        }

    } catch (error) {
        console.log("[-] TrustManager bypass failed: " + error);
    }
}

// Bypass hostname verification
function bypassHostnameVerifier() {
    console.log("[*] Setting up hostname verifier bypass");

    try {
        // Hook HostnameVerifier
        try {
            const HostnameVerifier = Java.use("javax.net.ssl.HostnameVerifier");

            if (HostnameVerifier.verify) {
                HostnameVerifier.verify.implementation = function(hostname, session) {
                    console.log("[+] Bypassed HostnameVerifier.verify for: " + hostname);

                    // Check if it's a PUBG domain
                    const isPubgDomain = SSLBypassConfig.pubgDomains.some(domain =>
                        hostname && hostname.includes(domain));

                    if (isPubgDomain) {
                        console.log("[+] PUBG domain detected in hostname verifier: " + hostname);
                    }

                    SSLBypassState.bypassedConnections.push({
                        type: "HostnameVerifier_verify",
                        hostname: hostname,
                        isPubgDomain: isPubgDomain,
                        timestamp: Date.now()
                    });

                    SSLBypassState.bypassesTriggered++;

                    // Always return true to bypass hostname verification
                    return true;
                };
                SSLBypassState.hooksInstalled++;
            }

            console.log("[+] HostnameVerifier bypass enabled");

        } catch (e) {
            console.log("[-] HostnameVerifier not found");
        }

        // Hook HttpsURLConnection hostname verification
        try {
            const HttpsURLConnection = Java.use("javax.net.ssl.HttpsURLConnection");

            if (HttpsURLConnection.setDefaultHostnameVerifier) {
                // Create a permissive hostname verifier
                const PermissiveHostnameVerifier = Java.registerClass({
                    name: "com.bearmod.PermissiveHostnameVerifier",
                    implements: [Java.use("javax.net.ssl.HostnameVerifier")],
                    methods: {
                        verify: function(hostname, session) {
                            console.log("[+] Permissive hostname verifier for: " + hostname);
                            return true;
                        }
                    }
                });

                HttpsURLConnection.setDefaultHostnameVerifier(PermissiveHostnameVerifier.$new());
                SSLBypassState.hooksInstalled++;

                console.log("[+] HttpsURLConnection hostname verifier bypass enabled");
            }

        } catch (e) {
            console.log("[-] HttpsURLConnection hostname verifier bypass failed: " + e);
        }

    } catch (error) {
        console.log("[-] Hostname verifier bypass failed: " + error);
    }
}

// Initialize SSL bypass
console.log("[*] Starting BearMod SSL bypass initialization");
setTimeout(function() {
    initializeSSLBypass();
}, 100);

console.log("[*] BearMod SSL Bypass Patch loaded successfully");
