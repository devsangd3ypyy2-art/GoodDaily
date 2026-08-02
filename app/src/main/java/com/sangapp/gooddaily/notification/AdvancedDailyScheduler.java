package com.sangapp.gooddaily.notification;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public final class AdvancedDailyScheduler {
    private AdvancedDailyScheduler() {}
    public static void schedule(Context context) {
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(AdvancedDailyWorker.class, 24, TimeUnit.HOURS).build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork("good_daily_advanced_daily", ExistingPeriodicWorkPolicy.UPDATE, request);
    }
}
