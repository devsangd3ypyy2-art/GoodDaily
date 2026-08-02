package com.sangapp.gooddaily.data.local.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import com.sangapp.gooddaily.util.PasswordUtils;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class SecureStore {
    private static final String PREF = "good_daily_secure";
    private static final String ALIAS = "good_daily_secure_key";
    private final SharedPreferences prefs;

    public SecureStore(Context context) {
        prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public boolean hasPin() { return !getDecrypted("pin_hash").isEmpty(); }

    public void setPin(String pin) {
        if (pin == null || pin.length() < 4) throw new IllegalArgumentException("PIN cần ít nhất 4 số");
        String salt = PasswordUtils.newSalt();
        putEncrypted("pin_salt", salt);
        putEncrypted("pin_hash", PasswordUtils.hash(pin, salt));
    }

    public boolean verifyPin(String pin) {
        String salt = getDecrypted("pin_salt");
        String expected = getDecrypted("pin_hash");
        return !salt.isEmpty() && expected.equals(PasswordUtils.hash(pin == null ? "" : pin, salt));
    }

    public void clearPin() {
        prefs.edit().remove("pin_salt").remove("pin_hash").apply();
    }

    private void putEncrypted(String key, String value) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey());
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            String payload = Base64.encodeToString(cipher.getIV(), Base64.NO_WRAP) + ":" + Base64.encodeToString(encrypted, Base64.NO_WRAP);
            prefs.edit().putString(key, payload).apply();
        } catch (Exception e) {
            throw new IllegalStateException("Không thể lưu dữ liệu bảo mật", e);
        }
    }

    private String getDecrypted(String key) {
        String payload = prefs.getString(key, "");
        if (payload == null || payload.isEmpty() || !payload.contains(":")) return "";
        try {
            String[] parts = payload.split(":", 2);
            byte[] iv = Base64.decode(parts[0], Base64.NO_WRAP);
            byte[] encrypted = Base64.decode(parts[1], Base64.NO_WRAP);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), new GCMParameterSpec(128, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }

    private SecretKey getOrCreateKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        java.security.Key existing = keyStore.getKey(ALIAS, null);
        if (existing instanceof SecretKey) return (SecretKey) existing;
        KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        generator.init(new KeyGenParameterSpec.Builder(ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build());
        return generator.generateKey();
    }
}
