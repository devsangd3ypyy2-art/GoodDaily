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
import com.sangapp.gooddaily.ui.MainActivity;

public class ScheduleReminderWorker extends Worker {
    public ScheduleReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        long id = getInputData().getLong("schedule_id", 0);
        String title = getInputData().getString("title");
        String time = getInputData().getString("time");
        String channelId = "schedule_reminders";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, "Thời gian biểu", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Nhắc trước 15 phút cho các khối thời gian trong Good Daily");
            context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("open_destination", "planner");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, (int) (7000 + id), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        try {
            NotificationManagerCompat.from(context).notify((int) (7000 + id),
                    new NotificationCompat.Builder(context, channelId)
                            .setSmallIcon(R.drawable.ic_notification)
                            .setContentTitle(title == null || title.trim().isEmpty() ? "Sắp đến lịch" : title)
                            .setContentText("Bắt đầu lúc " + (time == null ? "" : time) + " · còn 15 phút")
                            .setContentIntent(pendingIntent)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setAutoCancel(true)
                            .build());
        } catch (SecurityException ignored) {
        }
        return Result.success();
    }
}
