/**
 * Bear-Mod SSL Pinning Bypass Script
 *
 * This script bypasses SSL certificate pinning by hooking
 * various certificate validation methods and making them
 * always return true/success.
 *
 * DISCLAIMER:
 * Bear-Mod is designed for security researchers, app developers, and educational purposes only.
 * Users must:
 * 1. Only analyze applications they own or have explicit permission to test
 * 2. Respect intellectual property rights and terms of service
 * 3. Use findings responsibly through proper disclosure channels
 * 4. Not use this tool to access unauthorized content or services
 *
 * Misuse of this tool may violate laws including but not limited to the Computer Fraud and Abuse Act,
 * Digital Millennium Copyright Act, and equivalent legislation in other jurisdictions.
 */

console.log("[*] SSL Pinning Bypass Script Loaded");

Java.perform(function() {
    console.log("[*] Java VM initialized");

    // Bypass OkHttp3 certificate pinning
    try {
        var CertificatePinner = Java.use("okhttp3.CertificatePinner");
        CertificatePinner.check.overload('java.lang.String', 'java.util.List').implementation = function(hostname, peerCertificates) {
            console.log("[+] OkHttp3 CertificatePinner.check() bypassed for " + hostname);
            return;
        };

        CertificatePinner.check.overload('java.lang.String', '[Ljava.security.cert.Certificate;').implementation = function(hostname, peerCertificates) {
            console.log("[+] OkHttp3 CertificatePinner.check() bypassed for " + hostname);
            return;
        };

        console.log("[+] OkHttp3 CertificatePinner hooks installed");
    } catch (e) {
        console.log("[-] OkHttp3 CertificatePinner not found: " + e);
    }

    // Bypass TrustManagerImpl certificate verification
    try {
        var TrustManagerImpl = Java.use("com.android.org.conscrypt.TrustManagerImpl");

        TrustManagerImpl.verifyChain.implementation = function(untrustedChain, trustAnchorChain, host, clientAuth, ocspData, tlsSctData) {
            console.log("[+] TrustManagerImpl.verifyChain() bypassed for " + host);
            return untrustedChain;
        };

        console.log("[+] TrustManagerImpl hooks installed");
    } catch (e) {
        console.log("[-] TrustManagerImpl not found: " + e);
    }

    // Bypass X509TrustManager certificate verification
    try {
        var X509TrustManager = Java.use("javax.net.ssl.X509TrustManager");
        var SSLContext = Java.use("javax.net.ssl.SSLContext");

        // Create a custom TrustManager that trusts all certificates
        var TrustManager = Java.registerClass({
            name: "com.bearmod.TrustAllCertificates",
            implements: [X509TrustManager],
            methods: {
                checkClientTrusted: function(chain, authType) {
                    console.log("[+] checkClientTrusted bypassed");
                },
                checkServerTrusted: function(chain, authType) {
                    console.log("[+] checkServerTrusted bypassed");
                },
                getAcceptedIssuers: function() {
                    return [];
                }
            }
        });

        // Create a new SSLContext with our custom TrustManager
        var TrustManagers = [TrustManager.$new()];
        var SSLContext_init = SSLContext.init.overload(
            "[Ljavax.net.ssl.KeyManager;",
            "[Ljavax.net.ssl.TrustManager;",
            "java.security.SecureRandom"
        );

        SSLContext_init.implementation = function(keyManager, trustManager, secureRandom) {
            console.log("[+] SSLContext.init() hooked");
            SSLContext_init.call(this, keyManager, TrustManagers, secureRandom);
        };

        console.log("[+] X509TrustManager hooks installed");
    } catch (e) {
        console.log("[-] X509TrustManager not found: " + e);
    }

    // Bypass Appcelerator Titanium certificate pinning
    try {
        var PinningTrustManager = Java.use("appcelerator.https.PinningTrustManager");
        PinningTrustManager.checkServerTrusted.implementation = function(chain, authType) {
            console.log("[+] PinningTrustManager.checkServerTrusted() bypassed");
            return;
        };

        console.log("[+] Appcelerator Titanium hooks installed");
    } catch (e) {
        console.log("[-] Appcelerator Titanium not found: " + e);
    }

    // Bypass Trustkit certificate pinning
    try {
        var TrustKit = Java.use("com.datatheorem.android.trustkit.pinning.OkHostnameVerifier");
        TrustKit.verify.overload('java.lang.String', 'javax.net.ssl.SSLSession').implementation = function(hostname, session) {
            console.log("[+] TrustKit.verify() bypassed for " + hostname);
            return true;
        };

        console.log("[+] TrustKit hooks installed");
    } catch (e) {
        console.log("[-] TrustKit not found: " + e);
    }

    // Bypass TrustManagerImpl check
    try {
        var ArrayList = Java.use("java.util.ArrayList");
        var TrustManagerImpl = Java.use("com.android.org.conscrypt.TrustManagerImpl");

        TrustManagerImpl.checkTrustedRecursive.implementation = function(certs, host, clientAuth, untrustedChain, trustedChain, used) {
            console.log("[+] TrustManagerImpl.checkTrustedRecursive() bypassed for " + host);
            return ArrayList.$new();
        };

        console.log("[+] TrustManagerImpl.checkTrustedRecursive hooks installed");
    } catch (e) {
        console.log("[-] TrustManagerImpl.checkTrustedRecursive not found: " + e);
    }

    // Bypass Android WebViewClient certificate verification
    try {
        var WebViewClient = Java.use("android.webkit.WebViewClient");

        WebViewClient.onReceivedSslError.implementation = function(webView, sslErrorHandler, sslError) {
            console.log("[+] WebViewClient.onReceivedSslError() bypassed");
            sslErrorHandler.proceed();
            return;
        };

        console.log("[+] WebViewClient hooks installed");
    } catch (e) {
        console.log("[-] WebViewClient not found: " + e);
    }

    // Bypass Cordova certificate verification
    try {
        var CordovaWebViewClient = Java.use("org.apache.cordova.CordovaWebViewClient");

        CordovaWebViewClient.onReceivedSslError.implementation = function(view, handler, error) {
            console.log("[+] CordovaWebViewClient.onReceivedSslError() bypassed");
            handler.proceed();
            return;
        };

        console.log("[+] CordovaWebViewClient hooks installed");
    } catch (e) {
        console.log("[-] CordovaWebViewClient not found: " + e);
    }

    console.log("[*] SSL pinning bypass complete");
});

console.log("[*] SSL pinning bypass script initialized");
