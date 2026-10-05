package com.example.smartautofiller.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.example.smartautofiller.database.AppDatabase;
import com.example.smartautofiller.model.UserProfile;

import java.util.List;

/**
 * Foreground Service displaying persistent quick-copy buttons (Name, Phone, Email)
 * in the system notification tray.
 */
public class QuickCopyService extends Service {

    private static final String CHANNEL_ID = "QuickCopyChannel";
    private static final int NOTIFICATION_ID = 101;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        updateNotification();
        return START_STICKY;
    }

    private void updateNotification() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(this);
            List<UserProfile> profiles = db.userProfileDao().getAllProfilesList();
            if (profiles != null && !profiles.isEmpty()) {
                UserProfile profile = profiles.get(0);

                Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_menu_edit)
                        .setContentTitle("Smart Autofill: " + profile.getProfileName())
                        .setContentText("Tap to copy your details")
                        .setPriority(NotificationCompat.PRIORITY_LOW)
                        .setOngoing(true)
                        .addAction(createCopyAction("Name", profile.getFullName()))
                        .addAction(createCopyAction("Phone", profile.getPhoneNumber()))
                        .addAction(createCopyAction("Email", profile.getEmail()))
                        .build();

                startForeground(NOTIFICATION_ID, notification);
            }
        });
    }

    private NotificationCompat.Action createCopyAction(String label, String text) {
        Intent intent = new Intent(this, CopyReceiver.class);
        intent.putExtra("text", text != null ? text : "");
        intent.putExtra("label", label);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                label.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        return new NotificationCompat.Action(0, label, pendingIntent);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Quick Copy Panel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
