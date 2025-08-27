package com.bearmod.injection;

import com.bearmod.patch.model.PatchResult;

/**
 * Minimal KeyAuthInjectionManager contract used by adapters and bridges.
 * This is an interface-only stub to satisfy references without adding behavior.
 */
public interface KeyAuthInjectionManager {
    interface InjectionCallback {
        void onInjectionStarted();
        void onInjectionProgress(int progress, String message);
        void onInjectionSuccess(PatchResult result);
        void onInjectionFailed(String error);
    }
}
