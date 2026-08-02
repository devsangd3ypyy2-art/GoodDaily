package com.sangapp.gooddaily.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.sangapp.gooddaily.data.local.GoodDailyDatabase;
import com.sangapp.gooddaily.data.local.entity.ReminderEntity;
import com.sangapp.gooddaily.notification.CustomReminderScheduler;
import com.sangapp.gooddaily.util.AppExecutors;

import java.util.List;

public class ReminderViewModel extends AndroidViewModel {
    private final GoodDailyDatabase db;

    public ReminderViewModel(@NonNull Application application) {
        super(application);
        db = GoodDailyDatabase.get(application);
    }

    public LiveData<List<ReminderEntity>> reminders() { return db.reminderDao().observeAll(); }
    public LiveData<Integer> enabledCount() { return db.reminderDao().observeEnabledCount(); }

    public void save(ReminderEntity entity) {
        AppExecutors.database().execute(() -> {
            if (entity.id == 0) entity.id = db.reminderDao().insert(entity);
            else db.reminderDao().update(entity);
            if (entity.enabled) CustomReminderScheduler.schedule(getApplication(), entity);
            else CustomReminderScheduler.cancel(getApplication(), entity.id);
        });
    }

    public void setEnabled(ReminderEntity entity, boolean enabled) {
        entity.enabled = enabled;
        AppExecutors.database().execute(() -> {
            db.reminderDao().update(entity);
            if (enabled) CustomReminderScheduler.schedule(getApplication(), entity);
            else CustomReminderScheduler.cancel(getApplication(), entity.id);
        });
    }

    public void delete(ReminderEntity entity) {
        AppExecutors.database().execute(() -> {
            db.reminderDao().delete(entity);
            CustomReminderScheduler.cancel(getApplication(), entity.id);
        });
    }
}
