package com.sangapp.gooddaily.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.media.RingtoneManager;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.sangapp.gooddaily.data.local.prefs.LocalUserStore;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class NotificationSoundManager {
    private static final Set<String> BASE_CHANNELS = new HashSet<>(Arrays.asList(
            "daily_summary", "custom_reminders", "schedule_reminders", "finance_alerts"));

    private NotificationSoundManager() {}

    @NonNull
    public static Uri getSelectedSound(@NonNull Context context) {
        String value = new LocalUserStore(context).getNotificationSoundUri();
        if (value == null || value.trim().isEmpty()) {
            return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        try {
            return Uri.parse(value);
        } catch (Exception ignored) {
            return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
    }

    @NonNull
    public static String ensureChannel(@NonNull Context context,
                                       @NonNull String baseId,
                                       @NonNull String name,
                                       @NonNull String description,
                                       int importance) {
        Uri sound = getSelectedSound(context);
        String key = Integer.toHexString(sound.toString().hashCode());
        String channelId = baseId + "_" + key;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null && manager.getNotificationChannel(channelId) == null) {
                NotificationChannel channel = new NotificationChannel(channelId, name, importance);
                channel.setDescription(description);
                AudioAttributes attributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();
                channel.setSound(sound, attributes);
                channel.enableVibration(true);
                manager.createNotificationChannel(channel);
            }
        }
        return channelId;
    }


    @NonNull
    public static String ensureChannelWithSound(@NonNull Context context,
                                                @NonNull String baseId,
                                                @NonNull String name,
                                                @NonNull String description,
                                                int importance,
                                                Uri customSound,
                                                boolean vibrationEnabled) {
        Uri sound = customSound == null ? getSelectedSound(context) : customSound;
        String key = Integer.toHexString((sound.toString() + vibrationEnabled).hashCode());
        String channelId = baseId + "_" + key;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null && manager.getNotificationChannel(channelId) == null) {
                NotificationChannel channel = new NotificationChannel(channelId, name, importance);
                channel.setDescription(description);
                AudioAttributes attributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();
                channel.setSound(sound, attributes);
                channel.enableVibration(vibrationEnabled);
                manager.createNotificationChannel(channel);
            }
        }
        return channelId;
    }

    public static void applySoundForLegacy(@NonNull Context context,
                                           @NonNull NotificationCompat.Builder builder) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setSound(getSelectedSound(context));
            builder.setVibrate(new long[]{0, 180, 100, 180});
        }
    }

    public static void clearOldChannels(@NonNull Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) return;
        for (NotificationChannel channel : manager.getNotificationChannels()) {
            String id = channel.getId();
            for (String base : BASE_CHANNELS) {
                if (id.equals(base) || id.startsWith(base + "_")) {
                    manager.deleteNotificationChannel(id);
                    break;
                }
            }
        }
    }
}
