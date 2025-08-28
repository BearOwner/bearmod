package com.bearmod.injection;

import android.content.Context;

import com.bearmod.auth.SimpleLicenseVerifier;

/**
 * AuthenticationManager
 *
 * Minimal singleton wrapper exposing current authentication state
 * for injection readiness checks. This delegates to the existing
 * KeyAuth-backed {@link SimpleLicenseVerifier} to avoid duplicating logic.
 */
public class AuthenticationManager {
    private static volatile AuthenticationManager instance;
    private static final Object lock = new Object();

    private final Context appContext;

    private AuthenticationManager(Context ctx) {
        this.appContext = ctx.getApplicationContext();
    }

    public static AuthenticationManager getInstance(Context ctx) {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new AuthenticationManager(ctx);
                }
            }
        }
        return instance;
    }

    /**
     * Returns true if there is a valid stored auth session.
     * This keeps the logic centralized in SimpleLicenseVerifier.
     */
    public boolean isAuthenticated() {
        return SimpleLicenseVerifier.hasValidStoredAuth(appContext);
    }
}
