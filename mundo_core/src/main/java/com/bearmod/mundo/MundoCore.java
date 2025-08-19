package com.bearmod.mundo;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

// BearMod-compatible MundoCore orchestrator (API-first, no new JNI methods)
public final class MundoCore {
    private static final String TAG = "BearMod.MundoCore";
    private static final AtomicBoolean INIT = new AtomicBoolean(false);
    private static final AtomicBoolean READY = new AtomicBoolean(false);
    private static volatile Context app;
    private static final ExecutorService bg = Executors.newSingleThreadExecutor();

    private MundoCore() {}

    public static void init(Application application, MundoConfig cfg) {
        if (!INIT.compareAndSet(false, true)) return;
        app = application.getApplicationContext();
        try {
            System.loadLibrary("mundo");
            System.loadLibrary("bearmod");
            // Respect existing JNI entry: invoke LoginActivity.safeInit if present; do not hard-fail otherwise
            try {
                Class<?> login = Class.forName("com.bearmod.activity.LoginActivity");
                login.getMethod("safeInit", Context.class).invoke(null, app);
                Log.d(TAG, "LoginActivity.safeInit invoked");
            } catch (Throwable t1) {
                Log.w(TAG, "LoginActivity.safeInit not available; continuing without explicit init", t1);
            }
            // Native libs are loaded and JNI is initialized; full readiness will be set after plugin bootstrap.
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Native libraries missing: " + e.getMessage());
            throw e;
        }
        // background managers/auth bootstrap
        bg.execute(() -> {
            try {
                // 1) Anti-detection (cheap)
                try { Class.forName("com.bearmod.security.AntiDetectionManager").getConstructor(Context.class).newInstance(app);} catch (Throwable ignored) {}
                // 2) Prepare scripts directory (required before plugins)
                try {
                    Object sm = Class.forName("com.bearmod.patch.SecureScriptManager").getMethod("getInstance", Context.class).invoke(null, app);
                    Class.forName("com.bearmod.patch.SecureScriptManager").getMethod("initializeScriptsDirectory").invoke(sm);
                } catch (Throwable ignored) {}
                // 3) Load plugins (centralized here)
                try {
                    Object pl = Class.forName("com.bearmod.plugin.PluginLoader").getMethod("getInstance", Context.class).invoke(null, app);
                    Class.forName("com.bearmod.plugin.PluginLoader").getMethod("loadPlugins").invoke(pl);
                    READY.set(true); // mark app initialization complete after plugins are ready
                    Log.d(TAG, "MundoCore ready: plugins loaded");
                } catch (Throwable pluginErr) {
                    Log.e(TAG, "Plugin bootstrap failed", pluginErr);
                    READY.set(true); // still allow app to proceed; plugins may be unavailable
                }
                // 4) Continue other managers (non-blocking for readiness)
                try { Class.forName("com.bearmod.patch.AutoPatchManager").getMethod("getInstance", Context.class).invoke(null, app);} catch (Throwable ignored) {}
                // 5) Auth validation (non-blocking)
                try {
                    Object mgr = Class.forName("com.bearmod.auth.BearModManager").getMethod("getInstance", Context.class).invoke(null, app);
                    boolean has = (boolean) Class.forName("com.bearmod.auth.BearModManager").getMethod("hasValidStoredSession").invoke(mgr);
                    if (has) { Class.forName("com.bearmod.auth.BearModManager").getMethod("validateStoredSession").invoke(mgr); }
                } catch (Throwable ignored) {}
            } catch (Throwable t) { Log.e(TAG, "Bootstrap error", t); }
        });
    }

    public static boolean isReady() { return READY.get(); }
    public static void requireReady() { if (!READY.get()) throw new IllegalStateException("MundoCore not ready"); }
}

