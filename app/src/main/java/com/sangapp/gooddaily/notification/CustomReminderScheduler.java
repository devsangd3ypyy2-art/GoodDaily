package com.sangapp.gooddaily.notification;

import android.content.Context;

import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.sangapp.gooddaily.data.local.GoodDailyDatabase;
import com.sangapp.gooddaily.data.local.entity.ReminderEntity;
import com.sangapp.gooddaily.util.AppExecutors;
import com.sangapp.gooddaily.util.DateUtils;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class CustomReminderScheduler {
    private static final String PREFIX = "good_daily_custom_reminder_";

    private CustomReminderScheduler() {}

    public static void scheduleAll(Context context) {
        Context appContext = context.getApplicationContext();
        AppExecutors.io().execute(() -> {
            List<ReminderEntity> reminders = GoodDailyDatabase.get(appContext).reminderDao().getEnabledSync();
            for (ReminderEntity reminder : reminders) schedule(appContext, reminder);
        });
    }

    public static void schedule(Context context, ReminderEntity reminder) {
        if (reminder == null || reminder.id <= 0) return;
        if (!reminder.enabled) {
            cancel(context, reminder.id);
            return;
        }

        long triggerAt = nextTrigger(reminder);
        if (triggerAt <= 0) {
            cancel(context, reminder.id);
            return;
        }

        Data input = new Data.Builder()
                .putLong("reminder_id", reminder.id)
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(ReminderNotificationWorker.class)
                .setInputData(input)
                .setInitialDelay(Math.max(0, triggerAt - System.currentTimeMillis()), TimeUnit.MILLISECONDS)
                .addTag(PREFIX + reminder.id)
                .build();

        WorkManager.getInstance(context.getApplicationContext())
                .enqueueUniqueWork(PREFIX + reminder.id, ExistingWorkPolicy.REPLACE, request);
    }

    public static void cancel(Context context, long reminderId) {
        WorkManager.getInstance(context.getApplicationContext()).cancelUniqueWork(PREFIX + reminderId);
    }

    public static long nextTrigger(ReminderEntity reminder) {
        Calendar now = Calendar.getInstance();
        String repeat = reminder.repeatType == null ? "ONCE" : reminder.repeatType;

        if ("DAILY".equals(repeat)) {
            Calendar target = Calendar.getInstance();
            target.set(Calendar.HOUR_OF_DAY, reminder.hour);
            target.set(Calendar.MINUTE, reminder.minute);
            target.set(Calendar.SECOND, 0);
            target.set(Calendar.MILLISECOND, 0);
            if (!target.after(now)) target.add(Calendar.DAY_OF_YEAR, 1);
            return target.getTimeInMillis();
        }

        if ("WEEKLY".equals(repeat)) {
            Calendar seed = Calendar.getInstance();
            seed.setTimeInMillis(DateUtils.parseDateKey(reminder.dateKey));
            int targetDay = seed.get(Calendar.DAY_OF_WEEK);
            Calendar target = Calendar.getInstance();
            target.set(Calendar.HOUR_OF_DAY, reminder.hour);
            target.set(Calendar.MINUTE, reminder.minute);
            target.set(Calendar.SECOND, 0);
            target.set(Calendar.MILLISECOND, 0);
            int delta = (targetDay - target.get(Calendar.DAY_OF_WEEK) + 7) % 7;
            target.add(Calendar.DAY_OF_YEAR, delta);
            if (!target.after(now)) target.add(Calendar.DAY_OF_YEAR, 7);
            return target.getTimeInMillis();
        }

        long once = DateUtils.atTime(reminder.dateKey, reminder.hour, reminder.minute);
        return once > System.currentTimeMillis() ? once : -1;
    }
}
