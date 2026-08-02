package com.sangapp.gooddaily.notification;

import android.app.NotificationManager;
import android.content.Context;

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
        String channelId = NotificationSoundManager.ensureChannel(
                context,
                "daily_summary",
                "Nhắc nhở hằng ngày",
                "Nhắc tổng kết và ghi lại hoạt động mỗi ngày",
                NotificationManager.IMPORTANCE_DEFAULT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Good Daily")
                .setContentText("Hãy ghi lại chi tiêu, sức khỏe và việc bạn đã hoàn thành hôm nay.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
        NotificationSoundManager.applySoundForLegacy(context, builder);
        try {
            NotificationManagerCompat.from(context).notify(2001, builder.build());
        } catch (SecurityException ignored) {
            // Người dùng chưa cấp quyền thông báo.
        }
        return Result.success();
    }
}
