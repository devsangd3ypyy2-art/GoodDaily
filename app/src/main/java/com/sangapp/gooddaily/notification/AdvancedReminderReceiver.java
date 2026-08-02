package com.sangapp.gooddaily.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.app.NotificationCompat;

import com.sangapp.gooddaily.R;
import com.sangapp.gooddaily.data.local.GoodDailyDatabase;
import com.sangapp.gooddaily.data.local.entity.PersonalRecordEntity;
import com.sangapp.gooddaily.ui.MainActivity;
import com.sangapp.gooddaily.util.AppExecutors;

public class AdvancedReminderReceiver extends BroadcastReceiver {
    public static final String ACTION_FIRE = "com.sangapp.gooddaily.ADVANCED_REMINDER_FIRE";
    public static final String ACTION_SNOOZE = "com.sangapp.gooddaily.ADVANCED_REMINDER_SNOOZE";
    public static final String ACTION_COMPLETE = "com.sangapp.gooddaily.ADVANCED_REMINDER_COMPLETE";

    @Override public void onReceive(Context context, Intent intent) {
        long id = intent.getLongExtra("record_id", 0);
        if (id <= 0) return;
        PendingResult pendingResult = goAsync();
        AppExecutors.io().execute(() -> {
            try {
                PersonalRecordEntity item = GoodDailyDatabase.get(context).advancedRecordDao().getById(id);
                if (item == null) return;
                String action = intent.getAction();
                if (ACTION_SNOOZE.equals(action)) {
                    AdvancedReminderScheduler.scheduleAt(context, id, System.currentTimeMillis() + 10 * 60_000L);
                    return;
                }
                if (ACTION_COMPLETE.equals(action)) {
                    item.status = "Đã hoàn thành";
                    item.updatedAt = System.currentTimeMillis();
                    GoodDailyDatabase.get(context).advancedRecordDao().update(item);
                    NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (manager != null) manager.cancel((int)(id & 0x7fffffff));
                    return;
                }
                show(context, item);
                long next = AdvancedReminderScheduler.nextTrigger(System.currentTimeMillis(), item);
                if (next > System.currentTimeMillis()) AdvancedReminderScheduler.scheduleAt(context, id, next);
            } finally { pendingResult.finish(); }
        });
    }

    private void show(Context context, PersonalRecordEntity item) {
        boolean vibrate = item.tags == null || !item.tags.toLowerCase(java.util.Locale.ROOT).contains("không rung");
        Uri recordSound = audioUri(item.attachmentUri);
        String group = reminderGroup(item.tags);
        String channel = NotificationSoundManager.ensureChannelWithSound(context,
                "advanced_reminders_" + group, "Nhắc nhở · " + group,
                "Nhắc việc, thuốc, thói quen và sự kiện", NotificationManager.IMPORTANCE_HIGH,
                recordSound, vibrate);
        Intent open = new Intent(context, MainActivity.class); open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent openPi = PendingIntent.getActivity(context, (int)(item.id & 0x7fffffff), open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Intent snooze = new Intent(context, AdvancedReminderReceiver.class).setAction(ACTION_SNOOZE).putExtra("record_id", item.id);
        Intent done = new Intent(context, AdvancedReminderReceiver.class).setAction(ACTION_COMPLETE).putExtra("record_id", item.id);
        PendingIntent snoozePi = PendingIntent.getBroadcast(context, (int)((item.id*31+1)&0x7fffffff), snooze, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        PendingIntent donePi = PendingIntent.getBroadcast(context, (int)((item.id*31+2)&0x7fffffff), done, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channel)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(item.title == null ? "Good Daily" : item.title)
                .setContentText(item.details == null || item.details.trim().isEmpty() ? "Đã đến giờ nhắc." : item.details)
                .setContentIntent(openPi).setAutoCancel(true).setPriority(NotificationCompat.PRIORITY_HIGH)
                .addAction(R.drawable.ic_clock, "Nhắc lại 10 phút", snoozePi)
                .addAction(R.drawable.ic_check_circle, "Hoàn thành", donePi);
        if (vibrate) builder.setVibrate(new long[]{0,200,100,200});
        NotificationSoundManager.applySoundForLegacy(context, builder);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) manager.notify((int)(item.id & 0x7fffffff), builder.build());
    }
    private Uri audioUri(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        String lower = value.toLowerCase(java.util.Locale.ROOT);
        if (!(lower.endsWith(".mp3") || lower.endsWith(".m4a") || lower.endsWith(".wav")
                || lower.endsWith(".ogg") || lower.startsWith("content://"))) return null;
        try { return Uri.parse(value); } catch (Exception ignored) { return null; }
    }

    private String reminderGroup(String tags) {
        String value = tags == null ? "" : tags.toLowerCase(java.util.Locale.ROOT);
        if (value.contains("thuốc") || value.contains("sức khỏe")) return "Sức khỏe";
        if (value.contains("học")) return "Học tập";
        if (value.contains("tài chính") || value.contains("tiền")) return "Tài chính";
        if (value.contains("thói quen")) return "Thói quen";
        return "Chung";
    }

}
