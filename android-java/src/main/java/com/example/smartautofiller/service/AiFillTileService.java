package com.example.smartautofiller.service;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

/**
 * Quick Settings Tile Service allowing the user to toggle the floating bubble
 * directly from the Android notification shade.
 */
public class AiFillTileService extends TileService {

    public static void requestTileUpdate(Context context) {
        requestListeningState(context, new ComponentName(context, AiFillTileService.class));
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        updateTile();
    }

    @Override
    public void onClick() {
        super.onClick();

        SharedPreferences prefs = getSharedPreferences("autofill_prefs", MODE_PRIVATE);
        boolean isBubbleEnabled = prefs.getBoolean("bubble_enabled", false);
        boolean accessibilityOn = SmartAccessibilityService.getInstance() != null;

        if (!accessibilityOn) {
            openAccessibilitySettings();
        } else {
            boolean newState = !isBubbleEnabled;
            prefs.edit().putBoolean("bubble_enabled", newState).apply();
            if (SmartAccessibilityService.getInstance() != null) {
                SmartAccessibilityService.getInstance().setBubbleVisible(newState);
            }
            updateTile();
        }
    }

    private void openAccessibilitySettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Bundle bundle = new Bundle();
            bundle.putString(":settings:fragment_args_key", "com.example.smartautofiller/.service.SmartAccessibilityService");
            intent.putExtra(":settings:show_fragment_args", bundle);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                PendingIntent pendingIntent = PendingIntent.getActivity(
                        this, 1001, intent,
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
                );
                startActivityAndCollapse(pendingIntent);
            } else {
                startActivityAndCollapse(intent);
            }
        } catch (Exception e) {
            Intent fallback = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                PendingIntent pendingIntent = PendingIntent.getActivity(
                        this, 1002, fallback,
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
                );
                startActivityAndCollapse(pendingIntent);
            } else {
                startActivityAndCollapse(fallback);
            }
        }
    }

    public void updateTile() {
        Tile tile = getQsTile();
        if (tile == null) return;

        SharedPreferences prefs = getSharedPreferences("autofill_prefs", MODE_PRIVATE);
        boolean isBubbleEnabled = prefs.getBoolean("bubble_enabled", false);
        boolean accessibilityOn = SmartAccessibilityService.getInstance() != null;

        if (!accessibilityOn) {
            tile.setState(Tile.STATE_INACTIVE);
            tile.setLabel("AI Fill");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.setSubtitle("Tap to Enable");
            }
        } else if (isBubbleEnabled) {
            tile.setState(Tile.STATE_ACTIVE);
            tile.setLabel("AI Fill");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.setSubtitle("Active");
            }
        } else {
            tile.setState(Tile.STATE_INACTIVE);
            tile.setLabel("AI Fill");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.setSubtitle("Off");
            }
        }
        tile.updateTile();
    }
}
