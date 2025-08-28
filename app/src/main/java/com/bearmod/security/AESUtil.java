package com.bearmod.security;

import android.util.Base64;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * BearMod secure comms (AES-256-GCM, keystore-backed, versioned, AAD)
 */
public final class AESUtil {
    private static final String TAG = "AESUtil";
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String KEY_ALIAS = "bear_comm_aes_v1";
    private static final String TRANSFORM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LEN = 12;     // 96-bit IV
    private static final int GCM_TAG_BITS = 128;  // 16-byte tag
    private static final byte ENVELOPE_V1 = 1;    // version byte

    private static final SecureRandom RNG = new SecureRandom();

    private AESUtil() {}

    /** Call once early (e.g., Application.onCreate). Idempotent. */
    public static void ensureKey() {
        try {
            KeyStore ks = KeyStore.getInstance(ANDROID_KEYSTORE);
            ks.load(null);
            if (!ks.containsAlias(KEY_ALIAS)) {
                KeyGenerator gen = KeyGenerator.getInstance("AES", ANDROID_KEYSTORE);
                gen.init(new android.security.keystore.KeyGenParameterSpec.Builder(
                        KEY_ALIAS,
                        android.security.keystore.KeyProperties.PURPOSE_ENCRYPT
                                | android.security.keystore.KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(256)
                        .build());
                gen.generateKey();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Keystore init failed", e);
        }
    }

    // --- Compatibility wrappers for existing callers ---
    // These wrappers provide defaults for AAD to preserve security binding while
    // satisfying older call sites that don't pass plugin/app identifiers.
    public static String encryptForBearMod(String plaintext) {
        // Use default AAD values identifying the core app channel
        return encrypt(plaintext, "core", "bearmod");
    }

    public static String decryptFromPlugin(String b64Envelope) {
        return decrypt(b64Envelope, "core", "bearmod");
    }

    /** Encrypts plaintext with AAD binding (e.g., pluginId/appId). Returns Base64 envelope. */
    public static String encrypt(String plaintext, String pluginId, String appId) {
        try {
            SecretKey key = getKey();
            byte[] iv = new byte[GCM_IV_LEN];
            RNG.nextBytes(iv);

            Cipher c = Cipher.getInstance(TRANSFORM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_BITS, iv);
            c.init(Cipher.ENCRYPT_MODE, key, spec);
            addAad(c, pluginId, appId);

            byte[] pt = plaintext.getBytes(StandardCharsets.UTF_8);
            byte[] ct = c.doFinal(pt);

            // Envelope: [v(1)][ivLen(1)][iv][ct]
            ByteBuffer buf = ByteBuffer.allocate(1 + 1 + iv.length + ct.length).order(ByteOrder.BIG_ENDIAN);
            buf.put(ENVELOPE_V1);
            buf.put((byte) iv.length);
            buf.put(iv);
            buf.put(ct);

            // zeroize plaintext
            zero(pt);
            return Base64.encodeToString(buf.array(), Base64.NO_WRAP);
        } catch (Exception e) {
            throw new RuntimeException("Encrypt failed", e);
        }
    }

    /** Decrypts Base64 envelope. Must supply same AAD params used on encrypt. */
    public static String decrypt(String b64Envelope, String pluginId, String appId) {
        byte[] env = Base64.decode(b64Envelope, Base64.NO_WRAP);
        try {
            ByteBuffer buf = ByteBuffer.wrap(env).order(ByteOrder.BIG_ENDIAN);
            byte ver = buf.get();
            if (ver != ENVELOPE_V1) {
                throw new IllegalArgumentException("Unsupported envelope version: " + ver);
            }
            int ivLen = Byte.toUnsignedInt(buf.get());
            if (ivLen != GCM_IV_LEN) throw new IllegalArgumentException("Bad IV length");
            byte[] iv = new byte[ivLen];
            buf.get(iv);
            byte[] ct = new byte[buf.remaining()];
            buf.get(ct);

            SecretKey key = getKey();
            Cipher c = Cipher.getInstance(TRANSFORM);
            c.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
            addAad(c, pluginId, appId);

            byte[] pt = c.doFinal(ct);
            String out = new String(pt, StandardCharsets.UTF_8);
            zero(pt);
            return out;
        } catch (Exception e) {
            throw new RuntimeException("Decrypt failed", e);
        } finally {
            zero(env);
        }
    }

    private static SecretKey getKey() throws Exception {
        KeyStore ks = KeyStore.getInstance(ANDROID_KEYSTORE);
        ks.load(null);
        KeyStore.Entry e = ks.getEntry(KEY_ALIAS, null);
        if (!(e instanceof KeyStore.SecretKeyEntry)) {
            throw new IllegalStateException("Missing AES key");
        }
        return ((KeyStore.SecretKeyEntry) e).getSecretKey();
    }

    private static void addAad(Cipher c, String pluginId, String appId) {
        // Bind ciphertext to context; order & exact bytes must match on decrypt.
        String aad = "app=" + (appId == null ? "" : appId)
                   + ";plugin=" + (pluginId == null ? "" : pluginId)
                   + ";proto=1";
        c.updateAAD(aad.getBytes(StandardCharsets.UTF_8));
    }

    private static void zero(byte[] a) {
        if (a == null) return;
        for (int i = 0; i < a.length; i++) a[i] = 0;
    }
}
