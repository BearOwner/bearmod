package com.bearmod.bridge;

import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.bearmod.util.NativeUtils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.Assume;

@RunWith(AndroidJUnit4.class)
public class NativeLibIT {

    @Test
    public void getVersion_returnsNonEmpty_whenNativeLoaded() {
        // Skip if native library isn't loaded in this environment
        Assume.assumeTrue("Native lib not loaded; skipping", NativeUtils.isNativeLoaded());
        String v = NativeLib.getVersion();
        assertNotNull(v);
        assertFalse(v.trim().isEmpty());
    }
}
