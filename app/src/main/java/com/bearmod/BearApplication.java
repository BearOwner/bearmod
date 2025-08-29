package com.bearmod;

import android.app.Application;
import android.util.Log;

import com.bearmod.bridge.NativeLib;
import com.bearmod.loader.utilities.NativeUtils;

import timber.log.Timber;

/**
 * BearApplication
 *
 * Custom Application used to perform early, process-wide initialization.
 * Responsibilities:
 * - Set up logging (Timber) with different trees for debug vs release.
 * - Initialize native layer early with proper ClassLoader context via {@link NativeLib#initialize(android.content.Context)}.
 * - Register JNI classes in a way that is safe for R8/ProGuard shrinking.
 * - Mark native layer as ready through {@link NativeUtils#setNativeLoaded(boolean)} once init succeeds.
 *
 * Note: Application#onTerminate is generally not called on real devices; cleanup is handled by the OS.
 */
public class BearApplication extends Application {
    private static final String TAG = "BearApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Timber
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new ReleaseTree());
        }
        Timber.i("Application onCreate: starting native initialization");

        try {
            // Ensure native lib is initialized with app ClassLoader context
            NativeLib.initialize(this);
            // Register classes to avoid string-based FindClass and keep R8 free to obfuscate names
            NativeLib.registerNatives(
                    com.bearmod.Floating.class,
                    com.bearmod.activity.LoginActivity.class,
                    com.bearmod.bridge.NativeLib.class
            );

            // Mark native layer as ready for guarded calls
            NativeUtils.setNativeLoaded(true);
            Timber.i("Native initialization completed successfully");
        } catch (Throwable t) {
            Timber.e(t, "Native initialization failed");
        }
    }

    /**
     * Custom Timber tree for release builds. Logs INFO+ to Logcat.
     * Hook here to forward to crash reporting if desired.
     */
    private static class ReleaseTree extends Timber.Tree {
        @Override
        protected void log(int priority, String tag, String message, Throwable t) {
            if (priority < Log.INFO) return; // Only INFO and above in release
            Log.println(priority, tag != null ? tag : TAG, message);
            // Example hook for crash reporting (commented):
            // if (priority >= Log.ERROR) {
            //     Crashlytics.log(priority, tag, message);
            //     if (t != null) Crashlytics.logException(t);
            // }
        }
    }
}
