package com.sangapp.gooddaily.notification;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public final class ReminderScheduler {
    private static final String WORK_NAME = "good_daily_evening_reminder";
    private ReminderScheduler() {}

    public static void schedule(Context context) {
        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.set(Calendar.HOUR_OF_DAY, 20);
        target.set(Calendar.MINUTE, 0);
        target.set(Calendar.SECOND, 0);
        if (!target.after(now)) target.add(Calendar.DAY_OF_YEAR, 1);
        long delay = target.getTimeInMillis() - now.getTimeInMillis();

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(DailyReminderWorker.class, 24, TimeUnit.HOURS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request);
    }

    public static void cancel(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME);
    }
}
