package com.sangapp.gooddaily;

import android.app.Application;

import com.sangapp.gooddaily.data.local.prefs.LocalUserStore;
import com.sangapp.gooddaily.notification.CustomReminderScheduler;
import com.sangapp.gooddaily.notification.ReminderScheduler;

public class GoodDailyApp extends Application {
    @Override public void onCreate() {
        super.onCreate();
        if (new LocalUserStore(this).isReminderEnabled()) ReminderScheduler.schedule(this);
        CustomReminderScheduler.scheduleAll(this);
    }
}
