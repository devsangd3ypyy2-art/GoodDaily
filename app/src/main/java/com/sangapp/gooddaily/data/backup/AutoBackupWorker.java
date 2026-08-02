package com.sangapp.gooddaily.data.backup;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class AutoBackupWorker extends Worker {
    public AutoBackupWorker(@NonNull Context context, @NonNull WorkerParameters params) { super(context, params); }
    @NonNull @Override public Result doWork() {
        try {
            new FullBackupManager(getApplicationContext()).createInternalAutoBackup();
            return Result.success();
        } catch (Exception e) { return Result.retry(); }
    }
}
