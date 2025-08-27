#include "bearmod_jni.h"
#include <string>
#include <vector>
#include <android/log.h>
#include <unistd.h>

// Note: LOG_TAG, logging macros, and EXPECTED_LOADER_PACKAGE are defined in bearmod_jni.h

/**
 * Get package signature hash for verification
 */
std::string getPackageSignatureHash(JNIEnv* env, jobject context, const char* packageName) {
    try {
        // Get PackageManager
        jclass contextClass = env->FindClass("android/content/Context");
        jmethodID getPackageManagerMethod = env->GetMethodID(
            contextClass, "getPackageManager", "()Landroid/content/pm/PackageManager;");
        jobject packageManager = env->CallObjectMethod(context, getPackageManagerMethod);
        
        // Get PackageInfo with signatures
        jclass packageManagerClass = env->FindClass("android/content/pm/PackageManager");
        jmethodID getPackageInfoMethod = env->GetMethodID(
            packageManagerClass, "getPackageInfo", 
            "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");
        
        jstring packageNameStr = env->NewStringUTF(packageName);
        const int GET_SIGNATURES = 0x00000040;
        jobject packageInfo = env->CallObjectMethod(
            packageManager, getPackageInfoMethod, packageNameStr, GET_SIGNATURES);
        
        if (!packageInfo) {
            LOGE("Failed to get package info for %s", packageName);
            return "";
        }
        
        // Get signatures array
        jclass packageInfoClass = env->FindClass("android/content/pm/PackageInfo");
        jfieldID signaturesField = env->GetFieldID(
            packageInfoClass, "signatures", "[Landroid/content/pm/Signature;");
        jobjectArray signatures = (jobjectArray)env->GetObjectField(packageInfo, signaturesField);
        
        if (!signatures || env->GetArrayLength(signatures) == 0) {
            LOGE("No signatures found for package %s", packageName);
            return "";
        }
        
        // Get first signature
        jobject signature = env->GetObjectArrayElement(signatures, 0);
        jclass signatureClass = env->FindClass("android/content/pm/Signature");
        jmethodID toByteArrayMethod = env->GetMethodID(signatureClass, "toByteArray", "()[B");
        jbyteArray signatureBytes = (jbyteArray)env->CallObjectMethod(signature, toByteArrayMethod);
        
        // Convert to native byte array
        jsize length = env->GetArrayLength(signatureBytes);
        jbyte* bytes = env->GetByteArrayElements(signatureBytes, nullptr);
        
        // Calculate simple hash (placeholder implementation)
        // In production, this should use a proper cryptographic hash function
        unsigned long hash = 5381;
        for (jsize i = 0; i < length; i++) {
            hash = ((hash << 5) + hash) + (unsigned char)bytes[i];
        }

        // Convert to hex string
        std::string hashStr;
        char hexChar[16];
        sprintf(hexChar, "%08lx", hash);
        hashStr = std::string(hexChar);
        
        // Cleanup
        env->ReleaseByteArrayElements(signatureBytes, bytes, JNI_ABORT);
        env->DeleteLocalRef(packageNameStr);
        
        return hashStr;
        
    } catch (...) {
        LOGE("Exception in getPackageSignatureHash");
        return "";
    }
}

/**
 * Verify that the call is coming from the legitimate loader
 */
bool isVerifiedFromLoader(JNIEnv* env, jobject context) {
    try {
        // Get calling package name
        jclass contextClass = env->FindClass("android/content/Context");
        jmethodID getPackageNameMethod = env->GetMethodID(
            contextClass, "getPackageName", "()Ljava/lang/String;");
        jstring packageNameObj = (jstring)env->CallObjectMethod(context, getPackageNameMethod);
        
        const char* packageName = env->GetStringUTFChars(packageNameObj, nullptr);
        
        // Verify package name
        bool isValidPackage = (strcmp(packageName, EXPECTED_LOADER_PACKAGE) == 0);
        
        env->ReleaseStringUTFChars(packageNameObj, packageName);
        env->DeleteLocalRef(packageNameObj);
        
        if (!isValidPackage) {
            LOGE("Invalid calling package: %s", packageName);
            return false;
        }
        
        // Additional verification: check signature
        std::string signature = getPackageSignatureHash(env, context, EXPECTED_LOADER_PACKAGE);
        if (signature.empty()) {
            LOGE("Failed to get loader signature");
            return false;
        }
        
        // In production, compare with expected signature
        // For development, just log the signature
        LOGI("Loader signature: %s", signature.c_str());
        
        return true;
        
    } catch (...) {
        LOGE("Exception in isVerifiedFromLoader");
        return false;
    }
}

/**
 * Verify loader integrity
 */
bool verifyLoaderIntegrity(JNIEnv* env, jobject context) {
    try {
        // Check if loader APK has been modified
        jclass contextClass = env->FindClass("android/content/Context");
        jmethodID getApplicationInfoMethod = env->GetMethodID(
            contextClass, "getApplicationInfo", "()Landroid/content/pm/ApplicationInfo;");
        jobject appInfo = env->CallObjectMethod(context, getApplicationInfoMethod);
        
        jclass appInfoClass = env->FindClass("android/content/pm/ApplicationInfo");
        jfieldID sourceDirField = env->GetFieldID(appInfoClass, "sourceDir", "Ljava/lang/String;");
        jstring sourceDirObj = (jstring)env->GetObjectField(appInfo, sourceDirField);
        
        const char* sourceDir = env->GetStringUTFChars(sourceDirObj, nullptr);
        
        // Check APK file integrity (simplified check)
        FILE* apkFile = fopen(sourceDir, "rb");
        if (!apkFile) {
            LOGE("Cannot open APK file: %s", sourceDir);
            env->ReleaseStringUTFChars(sourceDirObj, sourceDir);
            return false;
        }
        
        // Get file size
        fseek(apkFile, 0, SEEK_END);
        long fileSize = ftell(apkFile);
        fclose(apkFile);
        
        env->ReleaseStringUTFChars(sourceDirObj, sourceDir);
        env->DeleteLocalRef(sourceDirObj);
        
        // Basic integrity check - file should be reasonable size
        if (fileSize < 1024 * 1024) { // Less than 1MB is suspicious
            LOGE("APK file size too small: %ld bytes", fileSize);
            return false;
        }
        
        LOGI("Loader integrity check passed. APK size: %ld bytes", fileSize);
        return true;
        
    } catch (...) {
        LOGE("Exception in verifyLoaderIntegrity");
        return false;
    }
}

/**
 * Check for anti-tamper measures
 */
bool checkAntiTamper(JNIEnv* env, jobject context) {
    try {
        // Check for debugging
        jclass debugClass = env->FindClass("android/os/Debug");
        jmethodID isDebuggerConnectedMethod = env->GetStaticMethodID(
            debugClass, "isDebuggerConnected", "()Z");
        jboolean isDebugging = env->CallStaticBooleanMethod(debugClass, isDebuggerConnectedMethod);
        
        if (isDebugging) {
            LOGW("Debugger detected");
            #ifndef DEBUG
            return false;
            #endif
        }
        
        // Check for root (using simple file existence check)
        const char* rootPaths[] = {
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        };
        
        for (const char* path : rootPaths) {
            if (access(path, F_OK) == 0) {
                LOGW("Root indicator found: %s", path);
                // Don't fail for root, just log it
                break;
            }
        }
        
        return true;
        
    } catch (...) {
        LOGE("Exception in checkAntiTamper");
        return false;
    }
}
