package com.sangapp.gooddaily;

import android.app.Application;

import com.sangapp.gooddaily.data.local.prefs.LocalUserStore;
import com.sangapp.gooddaily.notification.CustomReminderScheduler;
import com.sangapp.gooddaily.notification.ReminderScheduler;
import com.sangapp.gooddaily.util.AppearanceUtils;

public class GoodDailyApp extends Application {
    @Override public void onCreate() {
        super.onCreate();
        LocalUserStore userStore = new LocalUserStore(this);
        AppearanceUtils.apply(userStore.getAppearanceMode());
        if (userStore.isReminderEnabled()) ReminderScheduler.schedule(this);
        CustomReminderScheduler.scheduleAll(this);
    }
}
