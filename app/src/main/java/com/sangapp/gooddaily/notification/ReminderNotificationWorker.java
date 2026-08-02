package com.sangapp.gooddaily.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.sangapp.gooddaily.R;
import com.sangapp.gooddaily.data.local.GoodDailyDatabase;
import com.sangapp.gooddaily.data.local.entity.ReminderEntity;
import com.sangapp.gooddaily.ui.MainActivity;

public class ReminderNotificationWorker extends Worker {
    public ReminderNotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        long id = getInputData().getLong("reminder_id", -1);
        if (id <= 0) return Result.failure();

        Context context = getApplicationContext();
        GoodDailyDatabase db = GoodDailyDatabase.get(context);
        ReminderEntity reminder = db.reminderDao().getByIdSync(id);
        if (reminder == null || !reminder.enabled) return Result.success();

        String channelId = "custom_reminders";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Nhắc nhở cá nhân",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Thông báo lịch, học tập, sức khỏe và tài chính của Good Daily");
            context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (int) id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            NotificationManagerCompat.from(context).notify((int) (3000 + id),
                    new NotificationCompat.Builder(context, channelId)
                            .setSmallIcon(R.drawable.ic_notification)
                            .setContentTitle(reminder.title == null || reminder.title.trim().isEmpty() ? "Good Daily" : reminder.title)
                            .setContentText(reminder.description == null || reminder.description.trim().isEmpty()
                                    ? "Đã đến thời gian bạn đặt lịch."
                                    : reminder.description)
                            .setContentIntent(pendingIntent)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setAutoCancel(true)
                            .build());
        } catch (SecurityException ignored) {
            // Người dùng chưa cấp quyền thông báo.
        }

        if ("ONCE".equals(reminder.repeatType)) {
            db.reminderDao().setEnabled(id, false);
        } else {
            CustomReminderScheduler.schedule(context, reminder);
        }
        return Result.success();
    }
}
