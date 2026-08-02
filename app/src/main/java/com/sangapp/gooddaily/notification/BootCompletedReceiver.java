package com.sangapp.gooddaily.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sangapp.gooddaily.data.backup.AutoBackupScheduler;
import com.sangapp.gooddaily.data.local.prefs.LocalUserStore;

/** Recreates local schedules after a device restart or app replacement. */
public class BootCompletedReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        String action = intent == null ? "" : intent.getAction();
        if (!Intent.ACTION_BOOT_COMPLETED.equals(action)
                && !Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)
                && !Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(action)) return;

        LocalUserStore store = new LocalUserStore(context);
        if (store.isReminderEnabled()) ReminderScheduler.schedule(context);
        CustomReminderScheduler.scheduleAll(context);
        AdvancedReminderScheduler.scheduleAll(context);
        AdvancedDailyScheduler.schedule(context);
        AutoBackupScheduler.schedule(context, store.getAutoBackupCadence());
    }
}
