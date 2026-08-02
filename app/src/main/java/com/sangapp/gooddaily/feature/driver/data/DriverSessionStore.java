package com.sangapp.gooddaily.feature.driver.data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Stores the currently running driver shift locally so it survives Activity recreation
 * and an application process restart. This is intentionally separate from Room: a shift
 * is written to the database only after the user reviews its revenue and costs.
 */
public final class DriverSessionStore {
    private static final String PREF = "good_daily_driver_session";
    private static final String KEY_START_TIME = "start_time";

    private final SharedPreferences preferences;

    public DriverSessionStore(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public boolean isRunning() {
        return getStartTime() > 0;
    }

    public long getStartTime() {
        return preferences.getLong(KEY_START_TIME, 0L);
    }

    public void start(long startedAt) {
        preferences.edit().putLong(KEY_START_TIME, startedAt).apply();
    }

    public void clear() {
        preferences.edit().remove(KEY_START_TIME).apply();
    }
}
