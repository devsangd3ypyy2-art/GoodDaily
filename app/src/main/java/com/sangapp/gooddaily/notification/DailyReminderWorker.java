package com.sangapp.gooddaily.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.sangapp.gooddaily.R;

public class DailyReminderWorker extends Worker {
    public DailyReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) { super(context, params); }

    @NonNull @Override public Result doWork() {
        Context context = getApplicationContext();
        String channelId = "daily_summary";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Nhắc nhở hằng ngày", NotificationManager.IMPORTANCE_DEFAULT);
            context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
        try {
            NotificationManagerCompat.from(context).notify(2001,
                    new NotificationCompat.Builder(context, channelId)
                            .setSmallIcon(R.drawable.ic_notification)
                            .setContentTitle("Good Daily")
                            .setContentText("Hãy ghi lại chi tiêu, sức khỏe và việc bạn đã hoàn thành hôm nay.")
                            .setAutoCancel(true)
                            .build());
        } catch (SecurityException ignored) {
            // Người dùng chưa cấp quyền thông báo.
        }
        return Result.success();
    }
}
