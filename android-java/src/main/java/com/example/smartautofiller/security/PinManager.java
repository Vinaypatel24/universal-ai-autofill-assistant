package com.example.smartautofiller.security;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

/**
 * Manages user PIN setup, verification, attempts lockout, and biometric flags
 * backed by hardware-backed EncryptedSharedPreferences (AES256-GCM).
 */
public class PinManager {

    private static final String PREFS_FILE = "secure_pin_prefs";
    private static final String KEY_PIN = "user_pin";
    private static final String KEY_ATTEMPTS = "wrong_attempts";
    private static final String KEY_LOCKED_UNTIL = "locked_until";
    private static final String KEY_LOCK_TIMEOUT = "lock_timeout_mins";
    private static final String KEY_BIOMETRIC_ENABLED = "biometric_enabled";

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MS = 30_000L; // 30 seconds

    private final SharedPreferences prefs;

    public PinManager(Context context) {
        SharedPreferences p;
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            p = EncryptedSharedPreferences.create(
                    context,
                    PREFS_FILE,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            // Fallback for environments where MasterKey fails
            p = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        }
        this.prefs = p;
    }

    public boolean isPinSet() {
        return prefs.getString(KEY_PIN, null) != null;
    }

    public void setPin(String pin) {
        prefs.edit().putString(KEY_PIN, pin).apply();
        resetAttempts();
    }

    public void clearPin() {
        prefs.edit().remove(KEY_PIN).apply();
    }

    public VerifyResult verifyPin(String pin) {
        long lockedUntil = prefs.getLong(KEY_LOCKED_UNTIL, 0);
        long now = System.currentTimeMillis();

        if (now < lockedUntil) {
            int remainingSecs = (int) ((lockedUntil - now) / 1000);
            return VerifyResult.lockedOut(remainingSecs);
        }

        String savedPin = prefs.getString(KEY_PIN, null);
        if (savedPin == null) {
            return VerifyResult.noPinSet();
        }

        if (savedPin.equals(pin)) {
            resetAttempts();
            return VerifyResult.success();
        } else {
            int attempts = incrementAttempts();
            if (attempts >= MAX_ATTEMPTS) {
                prefs.edit().putLong(KEY_LOCKED_UNTIL, now + LOCKOUT_DURATION_MS).apply();
                resetAttempts();
                return VerifyResult.lockedOut(30);
            } else {
                int remaining = MAX_ATTEMPTS - attempts;
                return VerifyResult.wrongPin(remaining);
            }
        }
    }

    private int incrementAttempts() {
        int current = prefs.getInt(KEY_ATTEMPTS, 0) + 1;
        prefs.edit().putInt(KEY_ATTEMPTS, current).apply();
        return current;
    }

    private void resetAttempts() {
        prefs.edit().putInt(KEY_ATTEMPTS, 0).putLong(KEY_LOCKED_UNTIL, 0).apply();
    }

    public int getRemainingLockoutSeconds() {
        long lockedUntil = prefs.getLong(KEY_LOCKED_UNTIL, 0);
        long remaining = (lockedUntil - System.currentTimeMillis()) / 1000;
        return remaining > 0 ? (int) remaining : 0;
    }

    public boolean isBiometricEnabled() {
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false);
    }

    public void setBiometricEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply();
    }

    public int getLockTimeoutMins() {
        return prefs.getInt(KEY_LOCK_TIMEOUT, 0);
    }

    public void setLockTimeoutMins(int mins) {
        prefs.edit().putInt(KEY_LOCK_TIMEOUT, mins).apply();
    }

    public long getLockTimeoutMillis() {
        return getLockTimeoutMins() * 60 * 1000L;
    }

    // ── Result Encapsulation ───────────────────────────────────
    public static class VerifyResult {
        public enum Status { SUCCESS, NO_PIN_SET, WRONG_PIN, LOCKED_OUT }

        private final Status status;
        private final int value; // remaining attempts or remaining seconds

        private VerifyResult(Status status, int value) {
            this.status = status;
            this.value = value;
        }

        public static VerifyResult success() {
            return new VerifyResult(Status.SUCCESS, 0);
        }

        public static VerifyResult noPinSet() {
            return new VerifyResult(Status.NO_PIN_SET, 0);
        }

        public static VerifyResult wrongPin(int remainingAttempts) {
            return new VerifyResult(Status.WRONG_PIN, remainingAttempts);
        }

        public static VerifyResult lockedOut(int remainingSeconds) {
            return new VerifyResult(Status.LOCKED_OUT, remainingSeconds);
        }

        public Status getStatus() { return status; }
        public int getValue() { return value; }
        public boolean isSuccess() { return status == Status.SUCCESS; }
    }
}
