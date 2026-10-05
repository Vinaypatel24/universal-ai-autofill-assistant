package com.example.smartautofiller.service;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

/**
 * Handles copying text to clipboard and securely clears it after 30 seconds
 * to prevent sensitive data leaks to other apps.
 */
public class CopyReceiver extends BroadcastReceiver {

    private static final long CLEAR_TIMEOUT_MS = 30_000L;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        final String textToCopy = intent.getStringExtra("text");
        if (textToCopy == null) return;
        String label = intent.getStringExtra("label");
        if (label == null) label = "Data";

        final ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) return;

        ClipData clip = ClipData.newPlainText("SmartAutofill", textToCopy);
        clipboard.setPrimaryClip(clip);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(context, label + " Copied!", Toast.LENGTH_SHORT).show();
        }

        // Auto-clear clipboard after 30 seconds
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                ClipData currentClip = clipboard.getPrimaryClip();
                if (currentClip != null && currentClip.getItemCount() > 0) {
                    CharSequence currentText = currentClip.getItemAt(0).getText();
                    if (currentText != null && currentText.toString().equals(textToCopy)) {
                        clipboard.setPrimaryClip(ClipData.newPlainText("", ""));
                    }
                }
            } catch (Exception ignored) {}
        }, CLEAR_TIMEOUT_MS);
    }
}
