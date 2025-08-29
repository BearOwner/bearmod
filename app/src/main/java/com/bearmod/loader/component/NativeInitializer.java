package com.bearmod.loader.component;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.provider.Settings;
import com.bearmod.loader.utilities.Logx;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * NativeInitializer - Manages native library initialization and system checks
 *
 * Extracted from LoginActivity.java to separate native library initialization logic.
 * Handles safe initialization, overlay permission checks, and shared preferences setup.
 *
 * Migrated from com.bearmod.activity.LoginActivity to com.bearmod.loader.component
 */
public class NativeInitializer {
    private static final String TAG = "NativeInitializer";

    // Initialization state management
    private static final AtomicBoolean isInitializing = new AtomicBoolean(false);
    private static final Object lockObject = new Object();
    private static volatile SharedPreferences gifs;

    // Native library loaded state
    private static boolean nativeLibraryLoaded = false;

    /**
     * Initialize native library and perform system checks
     */
    public static void initialize(Object object) {
        try {
            if (object == null) return;
            if (!isInitializing.compareAndSet(false, true)) return;

            synchronized (lockObject) {
                final Context context = (Context) object;
                Activity activity = (Activity) object;

                safeInit(context);

                // Check overlay permission
                if (!Settings.canDrawOverlays(context)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + context.getPackageName()));
                    activity.startActivity(intent);
                }

                try {
                    gifs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
                } catch (Exception e) {
                    Logx.w("Failed to initialize shared preferences", e);
                    isInitializing.set(false);
                    return;
                }

                Logx.d("Native initialization completed");
            }
        } catch (Exception e) {
            Logx.w("Native initialization failed", e);
            isInitializing.set(false);
        }
    }

    /**
     * Safe initialization of native library
     */
    public static void safeInit(Context context) {
        // Do NOT load libraries here; SplashActivity controls load order (bearmod -> mundo)
        // Only attempt native Init; proceed gracefully if unavailable
        try {
            initialize(context);
            Logx.d("Native initialization successful");
        } catch (UnsatisfiedLinkError e) {
            Logx.w("Native library not available (expected during development)");
        } catch (Throwable t) {
            Logx.w("Native initialization error", t);
        }
    }

    /**
     * Check if native library is loaded
     */
    public static boolean isNativeLibraryLoaded() {
        return nativeLibraryLoaded;
    }

    /**
     * Set native library loaded state
     */
    public static void setNativeLibraryLoaded(boolean loaded) {
        nativeLibraryLoaded = loaded;
        Logx.d("Native library loaded state: " + loaded);
    }

    /**
     * Get shared preferences instance
     */
    public static SharedPreferences getSharedPreferences() {
        return gifs;
    }

    /**
     * Check if initialization is in progress
     */
    public static boolean isInitializing() {
        return isInitializing.get();
    }

    /**
     * Reset initialization state (for testing/debugging)
     */
    public static void resetInitializationState() {
        isInitializing.set(false);
        Logx.d("Initialization state reset");
    }

    /**
     * Get initialization lock object
     */
    public static Object getLockObject() {
        return lockObject;
    }
}