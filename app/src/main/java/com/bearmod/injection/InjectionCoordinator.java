package com.bearmod.injection;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * InjectionCoordinator
 *
 * Minimal concurrency gate for coordinating single active injection
 * across managers. Thread-safe and intentionally simple.
 */
public final class InjectionCoordinator {
    private static final AtomicBoolean active = new AtomicBoolean(false);
    private static volatile String status = "Idle";

    private InjectionCoordinator() {}

    /**
     * Try to start an injection. Returns false if one is already active.
     */
    public static boolean startInjection(String source, String targetPackage) {
        boolean acquired = active.compareAndSet(false, true);
        if (acquired) {
            status = "Active from " + source + " -> " + targetPackage;
        }
        return acquired;
    }

    /**
     * Mark injection as stopped.
     */
    public static void stopInjection(String source) {
        active.set(false);
        status = "Idle (last source: " + source + ")";
    }

    /**
     * Human-readable injection status.
     */
    public static String getInjectionStatus() {
        return status;
    }
}
