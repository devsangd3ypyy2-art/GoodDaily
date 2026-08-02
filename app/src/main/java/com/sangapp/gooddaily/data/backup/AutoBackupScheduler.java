package com.sangapp.gooddaily.data.backup;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public final class AutoBackupScheduler {
    private static final String NAME = "good_daily_auto_backup";
    private AutoBackupScheduler() {}

    public static void schedule(Context context, String cadence) {
        if (cadence == null || "OFF".equals(cadence)) { cancel(context); return; }
        long days = "MONTHLY".equals(cadence) ? 30 : 7;
        Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).setRequiresBatteryNotLow(true).build();
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(AutoBackupWorker.class, days, TimeUnit.DAYS)
                .setConstraints(constraints).build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(NAME, ExistingPeriodicWorkPolicy.UPDATE, request);
    }

    public static void cancel(Context context) { WorkManager.getInstance(context).cancelUniqueWork(NAME); }
}
