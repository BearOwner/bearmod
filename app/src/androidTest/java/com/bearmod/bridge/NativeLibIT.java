package com.bearmod.security;

import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.bearmod.security.NativeLib;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.Assume;

@RunWith(AndroidJUnit4.class)
public class NativeLibIT {

    @Test
    public void getVersion_returnsNonEmpty_whenNativeLoaded() {
        // Skip if native library isn't loaded in this environment
        Assume.assumeTrue("Native lib not loaded; skipping", NativeLib.isNativeLoaded());
        NativeLib lib = new NativeLib();
        String v = lib.getVersion();
        assertNotNull(v);
        assertFalse(v.trim().isEmpty());
    }
}
