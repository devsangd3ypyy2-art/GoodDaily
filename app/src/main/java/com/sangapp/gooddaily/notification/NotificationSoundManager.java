package com.sangapp.gooddaily.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.sangapp.gooddaily.R;
import com.sangapp.gooddaily.data.local.prefs.LocalUserStore;
import com.sangapp.gooddaily.ui.MainActivity;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class NotificationSoundManager {
    private static final String CHANNEL_REVISION = "v3";
    private static final Set<String> BASE_CHANNELS = new HashSet<>(Arrays.asList(
            "daily_summary", "custom_reminders", "schedule_reminders", "finance_alerts",
            "advanced_reminders_Chung", "advanced_reminders_Sức khỏe",
            "advanced_reminders_Học tập", "advanced_reminders_Tài chính",
            "advanced_reminders_Thói quen", "test_notifications"));

    private NotificationSoundManager() {}

    @NonNull
    public static Uri getSelectedSound(@NonNull Context context) {
        String value = new LocalUserStore(context).getNotificationSoundUri();
        if (value == null || value.trim().isEmpty()) {
            return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        try {
            Uri uri = Uri.parse(value);
            Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
            if (ringtone != null) return uri;
        } catch (Exception ignored) {
        }
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    }

    @NonNull
    public static String ensureChannel(@NonNull Context context,
                                       @NonNull String baseId,
                                       @NonNull String name,
                                       @NonNull String description,
                                       int importance) {
        return ensureChannelWithSound(context, baseId, name, description, importance,
                getSelectedSound(context), true);
    }

    @NonNull
    public static String ensureChannelWithSound(@NonNull Context context,
                                                @NonNull String baseId,
                                                @NonNull String name,
                                                @NonNull String description,
                                                int importance,
                                                @Nullable Uri customSound,
                                                boolean vibrationEnabled) {
        Uri sound = customSound == null ? getSelectedSound(context) : customSound;
        String key = Integer.toHexString((sound.toString() + vibrationEnabled + CHANNEL_REVISION).hashCode());
        String channelId = baseId + "_" + CHANNEL_REVISION + "_" + key;

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
                if (vibrationEnabled) channel.setVibrationPattern(new long[]{0, 220, 120, 220});
                channel.enableLights(true);
                manager.createNotificationChannel(channel);
            }
        }
        return channelId;
    }

    public static void applySoundForLegacy(@NonNull Context context,
                                           @NonNull NotificationCompat.Builder builder) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setSound(getSelectedSound(context));
            builder.setVibrate(new long[]{0, 220, 120, 220});
        }
    }

    /**
     * Copies a picked SAF audio document into MediaStore/Notifications on Android 10+.
     * The system notification service can reliably read this URI later, even when the app
     * is not in the foreground. On older Android versions the original URI is returned.
     */
    @Nullable
    public static Uri importAsNotificationSound(@NonNull Context context,
                                                @NonNull Uri source,
                                                @NonNull String displayName) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return source;

        ContentResolver resolver = context.getContentResolver();
        String safeName = sanitizeFileName(displayName);
        String mime = resolver.getType(source);
        if (mime == null || !mime.toLowerCase(Locale.ROOT).startsWith("audio/")) {
            mime = "audio/mpeg";
        }
        if (!safeName.contains(".")) safeName += extensionForMime(mime);

        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Media.DISPLAY_NAME, "GoodDaily_" + System.currentTimeMillis() + "_" + safeName);
        values.put(MediaStore.Audio.Media.MIME_TYPE, mime);
        values.put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_NOTIFICATIONS + "/GoodDaily");
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, 1);
        values.put(MediaStore.Audio.Media.IS_PENDING, 1);

        Uri destination = null;
        try {
            destination = resolver.insert(
                    MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), values);
            if (destination == null) return null;
            try (InputStream input = resolver.openInputStream(source);
                 OutputStream output = resolver.openOutputStream(destination, "w")) {
                if (input == null || output == null) throw new IllegalStateException("Không mở được file âm thanh");
                byte[] buffer = new byte[16 * 1024];
                int read;
                while ((read = input.read(buffer)) != -1) output.write(buffer, 0, read);
                output.flush();
            }
            ContentValues ready = new ContentValues();
            ready.put(MediaStore.Audio.Media.IS_PENDING, 0);
            resolver.update(destination, ready, null, null);
            return destination;
        } catch (Exception error) {
            if (destination != null) {
                try { resolver.delete(destination, null, null); } catch (Exception ignored) {}
            }
            return null;
        }
    }

    public static boolean postTestNotification(@NonNull Context context) {
        String channelId = ensureChannelWithSound(
                context,
                "test_notifications",
                "Kiểm tra âm báo",
                "Kênh dùng để kiểm tra chuông thông báo Good Daily",
                NotificationManager.IMPORTANCE_HIGH,
                getSelectedSound(context),
                true);

        Intent open = new Intent(context, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                9101,
                open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Kiểm tra chuông Good Daily")
                .setContentText("Nếu bạn nghe thấy âm thanh, cài đặt thông báo đang hoạt động.")
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 220, 120, 220});
        applySoundForLegacy(context, builder);
        try {
            NotificationManagerCompat.from(context).notify(9101, builder.build());
            return true;
        } catch (SecurityException ignored) {
            return false;
        }
    }

    public static void openNotificationSettings(@NonNull Context context) {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent = new Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.getPackageName());
        } else {
            intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.parse("package:" + context.getPackageName()));
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
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

    private static String sanitizeFileName(String value) {
        String result = value == null ? "notification.mp3" : value.trim();
        if (result.isEmpty()) result = "notification.mp3";
        return result.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private static String extensionForMime(String mime) {
        String lower = mime == null ? "" : mime.toLowerCase(Locale.ROOT);
        if (lower.contains("wav")) return ".wav";
        if (lower.contains("ogg")) return ".ogg";
        if (lower.contains("mp4") || lower.contains("m4a")) return ".m4a";
        return ".mp3";
    }
}
