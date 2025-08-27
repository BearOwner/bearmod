package com.bearmod.patch.injection;

/**
 * Minimal InjectionManager interface to satisfy adapters and legacy references.
 */
public interface InjectionManager {
    interface InjectionCallback {
        void onInjectionProgress(String targetPackage, int progress);
        void onInjectionSuccess(String targetPackage, String libraryPathOrId);
        void onInjectionFailed(String targetPackage, String error);
    }
}
