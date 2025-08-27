package com.bearmod.util;

/**
 * Lightweight string decoder to avoid embedding cleartext constants.
 * Provides a simple XOR-based decode from int[] using a small salt.
 * This is a minimal Phase-1 obfuscation and can be replaced later.
 */
public final class StrObf {
    private StrObf() {}

    /**
     * Decode a string from an int array by XOR-ing with a rolling salt.
     * Each element represents a UTF-16 code unit.
     */
    public static String d(int[] data, int salt) {
        char[] out = new char[data.length];
        int k = (salt & 0x7FFF) | 1; // ensure odd non-zero
        for (int i = 0; i < data.length; i++) {
            out[i] = (char) (data[i] ^ ((k + i * 31) & 0xFFFF));
        }
        return new String(out);
    }
}
