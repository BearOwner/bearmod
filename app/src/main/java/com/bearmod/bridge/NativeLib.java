package com.bearmod.bridge;

import android.content.Context;

/**
 * NativeLib - Bridge for native library initialization
 * 
 * This class provides the Java interface to the native bearmod library.
 * After cleanup, it only handles native initialization to ensure proper
 * JNI registration occurs in the correct ClassLoader context.
 * 
 * Authentication is now handled exclusively through KeyAuth API in Java,
 * with state synchronized to native globals via LoginActivity.updateAuthenticationState().
 * 
 * <h3>JNI Initialization Flow:</h3>
 * <ol>
 *   <li>SplashActivity calls System.loadLibrary("bearmod")</li>
 *   <li>JNI_OnLoad registers native methods (may fail due to ClassLoader context)</li>
 *   <li>SplashActivity calls NativeLib.initialize(Context) for deferred registration</li>
 *   <li>Native code can now safely call FindClass with app ClassLoader available</li>
 * </ol>
 * 
 * <h3>Authentication Flow:</h3>
 * <ol>
 *   <li>LoginActivity uses KeyAuth API for license validation</li>
 *   <li>On auth success, LoginActivity.updateAuthenticationState() syncs to native</li>
 *   <li>Native globals (g_Auth, g_Token, bValid) are updated for C++ code access</li>
 * </ol>
 */
public class NativeLib {
    static {
        try {
            System.loadLibrary("bearmod");
        } catch (Throwable t) {
            // Let caller handle init failure
        }
    }

    private NativeLib() {}

    /**
     * Initialize native components with proper ClassLoader context.
     * Must be called after System.loadLibrary() to ensure JNI registration
     * occurs with the application ClassLoader available.
     * 
     * This method performs deferred JNI class registration that may have failed
     * during JNI_OnLoad due to ClassLoader context issues. It's safe to call
     * multiple times - subsequent calls are no-ops.
     * 
     * @param context Application context for native initialization
     * @throws RuntimeException if critical native initialization fails
     */
    public static native void initialize(Context context);

    /**
     * Register native methods for provided classes using their Class objects.
     * This avoids relying on string-based FindClass, allowing R8 to obfuscate
     * class names safely. Should be called early after initialize().
     *
     * @param floatingCls       Class object for com.bearmod.Floating
     * @param loginActivityCls  Class object for com.bearmod.activity.LoginActivity
     * @param nativeLibCls      Class object for com.bearmod.bridge.NativeLib
     */
    public static native void registerNatives(Class<?> floatingCls,
                                              Class<?> loginActivityCls,
                                              Class<?> nativeLibCls);
}
