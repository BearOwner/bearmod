/**
 * Bear-Mod Signature Verification Bypass (SignKill)
 *
 * This script bypasses signature verification by hooking into
 * the getPackageInfo method and returning a fake valid signature.
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

console.log("[*] Signature Verification Bypass (SignKill) Loaded");

// Enable stealth mode by loading the anti-detection module
try {
    const antiDetection = require('./anti-detection.js');
    antiDetection.setupAntiDetection();
    console.log("[*] Anti-detection measures enabled");
} catch (e) {
    console.log("[!] Anti-detection module not available: " + e);
}

Java.perform(function() {
    console.log("[*] Java VM initialized");

    // Store original signatures for reporting
    var originalSignatures = {};

    // Hook ApplicationPackageManager.getPackageInfo
    var PackageManager = Java.use("android.app.ApplicationPackageManager");
    PackageManager.getPackageInfo.overload("java.lang.String", "int").implementation = function(pkg, flags) {
        // Check if signature flag is set
        var GET_SIGNATURES = 64; // PackageManager.GET_SIGNATURES
        var checkingSignature = (flags & GET_SIGNATURES) != 0;

        // Get the original package info
        var packageInfo = this.getPackageInfo.call(this, pkg, flags);

        // Only modify if checking signatures
        if (checkingSignature && packageInfo.signatures) {
            // Store original signature for reporting if we haven't seen it before
            if (!originalSignatures[pkg] && packageInfo.signatures.value) {
                try {
                    var sigHex = "";
                    if (packageInfo.signatures.value.length > 0) {
                        sigHex = packageInfo.signatures.value[0].toByteArray().map(function(b) {
                            return ('0' + (b & 0xFF).toString(16)).slice(-2);
                        }).join('');
                    }
                    originalSignatures[pkg] = sigHex;
                } catch (e) {
                    console.log("[!] Error getting original signature: " + e);
                }
            }

            console.log("[+] Spoofing signature check for " + pkg);

            // Create a fake signature
            // This is a generic debug signature - replace with a valid one for the specific app
            var Signature = Java.use("android.content.pm.Signature");
            var fakeSignature = Signature.$new([48, -126, 3, -126, 2, -96, 48, -126, 2, -126, 2, -96, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 11, 5, 0, 48, -126, 1, 56, 49, 11, 48, 9, 6, 3, 85, 4, 6, 19, 2, 85, 83, 49, 11, 48, 9, 6, 3, 85, 4, 8, 19, 2, 67, 65, 49, 22, 48, 20, 6, 3, 85, 4, 7, 19, 13, 83, 97, 110, 32, 70, 114, 97, 110, 99, 105, 115, 99, 111, 49, 16, 48, 14, 6, 3, 85, 4, 10, 19, 7, 65, 110, 100, 114, 111, 105, 100, 49, 16, 48, 14, 6, 3, 85, 4, 11, 19, 7, 65, 110, 100, 114, 111, 105, 100, 49, 16, 48, 14, 6, 3, 85, 4, 3, 19, 7, 65, 110, 100, 114, 111, 105, 100, 49, 66, 48, 64, 6, 9, 42, -122, 72, -122, -9, 13, 1, 9, 1, 22, 57, 97, 110, 100, 114, 111, 105, 100, 46, 111, 115, 64, 97, 110, 100, 114, 111, 105, 100, 46, 99, 111, 109, 48, 30, 23, 13, 49, 57, 48, 51, 48, 53, 48, 57, 52, 57, 50, 54, 90, 23, 13, 52, 54, 48, 55, 50, 48, 48, 57, 52, 57, 50, 54, 90, 48, -126, 1, 56, 49, 11, 48, 9, 6, 3, 85, 4, 6, 19, 2, 85, 83, 49, 11, 48, 9, 6, 3, 85, 4, 8, 19, 2, 67, 65, 49, 22, 48, 20, 6, 3, 85, 4, 7, 19, 13, 83, 97, 110, 32, 70, 114, 97, 110, 99, 105, 115, 99, 111, 49, 16, 48, 14, 6, 3, 85, 4, 10, 19, 7, 65, 110, 100, 114, 111, 105, 100, 49, 16, 48, 14, 6, 3, 85, 4, 11, 19, 7, 65, 110, 100, 114, 111, 105, 100, 49, 16, 48, 14, 6, 3, 85, 4, 3, 19, 7, 65, 110, 100, 114, 111, 105, 100, 49, 66, 48, 64, 6, 9, 42, -122, 72, -122, -9, 13, 1, 9, 1, 22, 57, 97, 110, 100, 114, 111, 105, 100, 46, 111, 115, 64, 97, 110, 100, 114, 111, 105, 100, 46, 99, 111, 109, 48, -126, 1, 34, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 1, 5, 0, 3, -126, 1, 15, 0, 48, -126, 1, 10, 2, -126, 1, 1, 0, -77, 69, -96, 56, 52, 93, 95, -88, -117, -55, -68, -78, 13, 6, 47, 13, 41, 44, 64, 64, 53, -28, -37, 65, -25, 14, 36, 9, 58, -114, 57, 125, 85, 101, 73, 36, 80, 89, 48, -11, -127, -112, 27, 126, 44, 83, 85, 38, 48, 49, 63, -33, 96, -56, 68, 30, 106, -128, 32, 57, 85, 95, -128, 78, 31, -77, 28, 15, -98, 89, 79, 15, -34, 103, -78, -101, -120, -114, 47, 32, 95, 60, -128, 89, 85, 108, -47, 127, 27, -83, 93, 86, 23, -3, 56, 58, -106, 59, 60, -128, 66, 82, 123, -77, 80, 39, 27, -49, 121, -114, 62, 108, 33, -27, -19, 121, 45, 58, 44, 126, -89, -50, -35, 80, 96, 116, -38, -12, 76, 51, 19, -52, 56, -38, 30, -29, -3, 127, 104, 1, 110, -112, 15, 38, 42, 8, 108, -123, -127, -115, -126, -70, -101, -19, 89, 58, 89, 41, 69, 40, 124, -112, -68, -109, 43, -83, 108, 44, -40, 42, -120, -53, 114, 15, 113, 33, -40, -55, -93, 91, 11, 69, 54, 35, -73, 53, 44, 84, 96, 3, 51, -119, -128, 39, 29, -93, -92, 42, -12, -35, 78, 61, 58, 125, 94, 126, 93, 64, 88, 80, 98, 115, 38, -82, -127, 75, -77, 75, 58, -68, 121, 63, 120, -61, 26, 28, 34, 110, 13, 53, 2, 3, 1, 0, 1, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 11, 5, 0, 3, -126, 1, 1, 0, 112, -95, 117, 9, 42, -86, 29, -112, -40, 30, -78, 73, 11, 104, -75, -5, 41, 96, -101, 57, -35, -93, 89, -118, -98, -122, 127, 97, 87, -44, 118, -96, 87, 76, 95, 57, -124, 25, 32, -104, 24, 2, -110, 71, 91, 86, 66, 51, 67, 56, 4, 47, -109, 60, 26, 53, 38, -49, 56, 12, 59, 126, -35, 53, 40, 69, -17, -20, -80, 88, 96, 42, 44, 126, 79, 31, 52, 44, -127, 115, 49, 118, 62, 61, 41, 28, -98, -5, 107, 65, 91, -127, 22, 66, 77, 101, -89, 38, 112, 49, 76, 121, -40, 96, 12, 100, -100, 89, 34, 28, 36, 88, -5, 60, 58, 12, 77, 54, 45, 5, 47, 117, 23, 36, 29, 95, 114, 109, 44, 86, 42, 33, 8, 117, 124, 33, 28, 75, 110, -89, 32, 89, 95, 3, 12, 29, 122, -112, 61, 98, -47, 59, 49, 95, 127, 119, -110, 81, 91, 13, 40, 48, 48, 123, 65, 63, -116, 19, 9, 24, 42, 41, 116, 59, 126, -122, 83, 96, 88, -128, 22, 11, 25, 60, -105, 59, 26, -125, 27, 61, 67, 31, 82, 82, 105, 119, 70, 46, 46, 88, 61, 110, 65, 114, 41, 27, 48, 71, 100, 46, 46, 73, 127, 96, 126, 74, 79, 35, 18, 64, 56, 95, 127, 47, 126, 115, 87, 49, 77, 127, 54, 22, 9, 24, 23, 123, 110, -17, 65, 8, 57, 78, 3, 108, 105, 11, 49, 107, 123, 80, 41, 83, 95, 47, 28, 1, 119, 98, 65, 75, 127, 41, 12, 98, 65, 75, 127, 41, 12]);

            // Replace the signatures in the package info
            packageInfo.signatures.value = [fakeSignature];

            // Log the action
            console.log("[+] Signature spoofed for " + pkg);
        }

        return packageInfo;
    };

    // Also hook PackageManager.getPackageArchiveInfo for APK files
    try {
        PackageManager.getPackageArchiveInfo.overload("java.lang.String", "int").implementation = function(archivePath, flags) {
            // Check if signature flag is set
            var GET_SIGNATURES = 64; // PackageManager.GET_SIGNATURES
            var checkingSignature = (flags & GET_SIGNATURES) != 0;

            // Get the original package info
            var packageInfo = this.getPackageArchiveInfo.call(this, archivePath, flags);

            // Only modify if checking signatures and packageInfo exists
            if (checkingSignature && packageInfo && packageInfo.signatures) {
                console.log("[+] Spoofing archive signature check for " + archivePath);

                // Create a fake signature
                var Signature = Java.use("android.content.pm.Signature");
                var fakeSignature = Signature.$new([48, -126, 3, -126, 2, -96, 48, -126, 2, -126, 2, -96, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 11, 5, 0, 48, -126, 1, 56, 49, 11, 48, 9, 6, 3, 85, 4, 6, 19, 2, 85, 83, 49, 11, 48, 9, 6, 3, 85, 4, 8, 19, 2, 67, 65, 49, 22, 48, 20, 6, 3, 85, 4, 7, 19, 13, 83, 97, 110, 32, 70, 114, 97, 110, 99, 105, 115, 99, 111, 49, 16, 48, 14, 6, 3, 85, 4, 10, 19, 7, 65, 110, 100, 114, 111, 105, 100, 49, 16, 48, 14, 6, 3, 85, 4, 11, 19, 7, 65, 110, 100, 114, 111, 105, 100, 49, 16, 48, 14, 6, 3, 85, 4, 3, 19, 7, 65, 110, 100, 114, 111, 105, 100, 49, 66, 48, 64, 6, 9, 42, -122, 72, -122, -9, 13, 1, 9, 1, 22, 57, 97, 110, 100, 114, 111, 105, 100, 46, 111, 115, 64, 97, 110, 100, 114, 111, 105, 100, 46, 99, 111, 109, 48, 30, 23, 13, 49, 57, 48, 51, 48, 53, 48, 57, 52, 57, 50, 54, 90, 23, 13, 52, 54, 48, 55, 50, 48, 48, 57, 52, 57, 50, 54, 90, 48, -126, 1, 56, 49, 11, 48, 9, 6, 3, 85, 4, 6, 19, 2, 85, 83, 49, 11, 48, 9, 6, 3, 85, 4, 8, 19, 2, 67, 65, 49, 22, 48, 20, 6, 3, 85, 4, 7, 19, 13, 83, 97, 110, 32, 70, 114, 97, 110, 99, 105, 115, 99, 111, 49, 16, 48, 14, 6, 3, 85, 4, 10, 19, 7, 65, 110, 100, 114, 111, 105, 100, 49, 16, 48, 14, 6, 3, 85, 4, 11, 19, 7, 65, 110, 100, 114, 111, 105, 100, 49, 16, 48, 14, 6, 3, 85, 4, 3, 19, 7, 65, 110, 100, 114, 111, 105, 100, 49, 66, 48, 64, 6, 9, 42, -122, 72, -122, -9, 13, 1, 9, 1, 22, 57, 97, 110, 100, 114, 111, 105, 100, 46, 111, 115, 64, 97, 110, 100, 114, 111, 105, 100, 46, 99, 111, 109, 48, -126, 1, 34, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 1, 5, 0, 3, -126, 1, 15, 0, 48, -126, 1, 10, 2, -126, 1, 1, 0, -77, 69, -96, 56, 52, 93, 95, -88, -117, -55, -68, -78, 13, 6, 47, 13, 41, 44, 64, 64, 53, -28, -37, 65, -25, 14, 36, 9, 58, -114, 57, 125, 85, 101, 73, 36, 80, 89, 48, -11, -127, -112, 27, 126, 44, 83, 85, 38, 48, 49, 63, -33, 96, -56, 68, 30, 106, -128, 32, 57, 85, 95, -128, 78, 31, -77, 28, 15, -98, 89, 79, 15, -34, 103, -78, -101, -120, -114, 47, 32, 95, 60, -128, 89, 85, 108, -47, 127, 27, -83, 93, 86, 23, -3, 56, 58, -106, 59, 60, -128, 66, 82, 123, -77, 80, 39, 27, -49, 121, -114, 62, 108, 33, -27, -19, 121, 45, 58, 44, 126, -89, -50, -35, 80, 96, 116, -38, -12, 76, 51, 19, -52, 56, -38, 30, -29, -3, 127, 104, 1, 110, -112, 15, 38, 42, 8, 108, -123, -127, -115, -126, -70, -101, -19, 89, 58, 89, 41, 69, 40, 124, -112, -68, -109, 43, -83, 108, 44, -40, 42, -120, -53, 114, 15, 113, 33, -40, -55, -93, 91, 11, 69, 54, 35, -73, 53, 44, 84, 96, 3, 51, -119, -128, 39, 29, -93, -92, 42, -12, -35, 78, 61, 58, 125, 94, 126, 93, 64, 88, 80, 98, 115, 38, -82, -127, 75, -77, 75, 58, -68, 121, 63, 120, -61, 26, 28, 34, 110, 13, 53, 2, 3, 1, 0, 1, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 11, 5, 0, 3, -126, 1, 1, 0, 112, -95, 117, 9, 42, -86, 29, -112, -40, 30, -78, 73, 11, 104, -75, -5, 41, 96, -101, 57, -35, -93, 89, -118, -98, -122, 127, 97, 87, -44, 118, -96, 87, 76, 95, 57, -124, 25, 32, -104, 24, 2, -110, 71, 91, 86, 66, 51, 67, 56, 4, 47, -109, 60, 26, 53, 38, -49, 56, 12, 59, 126, -35, 53, 40, 69, -17, -20, -80, 88, 96, 42, 44, 126, 79, 31, 52, 44, -127, 115, 49, 118, 62, 61, 41, 28, -98, -5, 107, 65, 91, -127, 22, 66, 77, 101, -89, 38, 112, 49, 76, 121, -40, 96, 12, 100, -100, 89, 34, 28, 36, 88, -5, 60, 58, 12, 77, 54, 45, 5, 47, 117, 23, 36, 29, 95, 114, 109, 44, 86, 42, 33, 8, 117, 124, 33, 28, 75, 110, -89, 32, 89, 95, 3, 12, 29, 122, -112, 61, 98, -47, 59, 49, 95, 127, 119, -110, 81, 91, 13, 40, 48, 48, 123, 65, 63, -116, 19, 9, 24, 42, 41, 116, 59, 126, -122, 83, 96, 88, -128, 22, 11, 25, 60, -105, 59, 26, -125, 27, 61, 67, 31, 82, 82, 105, 119, 70, 46, 46, 88, 61, 110, 65, 114, 41, 27, 48, 71, 100, 46, 46, 73, 127, 96, 126, 74, 79, 35, 18, 64, 56, 95, 127, 47, 126, 115, 87, 49, 77, 127, 54, 22, 9, 24, 23, 123, 110, -17, 65, 8, 57, 78, 3, 108, 105, 11, 49, 107, 123, 80, 41, 83, 95, 47, 28, 1, 119, 98, 65, 75, 127, 41, 12, 98, 65, 75, 127, 41, 12]);

                // Replace the signatures in the package info
                packageInfo.signatures.value = [fakeSignature];

                // Log the action
                console.log("[+] Archive signature spoofed for " + archivePath);
            }

            return packageInfo;
        };
        console.log("[+] PackageManager.getPackageArchiveInfo hooked");
    } catch (e) {
        console.log("[-] Failed to hook PackageManager.getPackageArchiveInfo: " + e);
    }

    // Hook signature verification in SignatureVerifier class if available
    try {
        var SignatureVerifier = Java.use("android.content.pm.SignatureVerifier");

        // Hook verifySignature method
        if (SignatureVerifier.verifySignature) {
            SignatureVerifier.verifySignature.implementation = function() {
                console.log("[+] SignatureVerifier.verifySignature bypassed");
                return true;
            };
            console.log("[+] SignatureVerifier.verifySignature hooked");
        }
    } catch (e) {
        console.log("[-] SignatureVerifier not found or failed to hook: " + e);
    }

    // Print a summary of original signatures at the end
    setTimeout(function() {
        console.log("\n[*] Original Signature Summary:");
        for (var pkg in originalSignatures) {
            console.log(`  ${pkg}: ${originalSignatures[pkg]}`);
        }
    }, 5000);

    console.log("[*] Signature verification bypass complete");
});

console.log("[*] Signature verification bypass script initialized");
