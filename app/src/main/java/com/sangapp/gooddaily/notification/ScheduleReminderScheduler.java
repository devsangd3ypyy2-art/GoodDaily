package com.sangapp.gooddaily.notification;

import android.content.Context;

import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.sangapp.gooddaily.data.local.entity.ScheduleBlockEntity;
import com.sangapp.gooddaily.util.DateUtils;

import java.util.concurrent.TimeUnit;

public final class ScheduleReminderScheduler {
    private static final String PREFIX = "good_daily_schedule_";

    private ScheduleReminderScheduler() {}

    public static void schedule(Context context, ScheduleBlockEntity block) {
        if (block == null || block.id <= 0) return;
        cancel(context, block.id);
        if (!block.reminderEnabled) return;

        int hour = block.startMinutes / 60;
        int minute = block.startMinutes % 60;
        long trigger = DateUtils.atTime(block.dateKey, hour, minute) - 15 * 60_000L;
        if (trigger <= System.currentTimeMillis()) return;

        Data data = new Data.Builder()
                .putLong("schedule_id", block.id)
                .putString("title", block.title)
                .putString("category", block.category)
                .putString("time", DateUtils.formatTimeFromMinutes(block.startMinutes))
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(ScheduleReminderWorker.class)
                .setInputData(data)
                .setInitialDelay(trigger - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(context.getApplicationContext())
                .enqueueUniqueWork(PREFIX + block.id, ExistingWorkPolicy.REPLACE, request);
    }

    public static void cancel(Context context, long id) {
        WorkManager.getInstance(context.getApplicationContext()).cancelUniqueWork(PREFIX + id);
    }
}
