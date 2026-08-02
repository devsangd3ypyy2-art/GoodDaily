package com.sangapp.gooddaily.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.sangapp.gooddaily.data.local.entity.ReminderEntity;

import java.util.List;

@Dao
public interface ReminderDao {
    @Insert long insert(ReminderEntity entity);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertAll(List<ReminderEntity> entities);
    @Update void update(ReminderEntity entity);
    @Delete void delete(ReminderEntity entity);

    @Query("SELECT * FROM reminders ORDER BY enabled DESC, dateKey ASC, hour ASC, minute ASC")
    LiveData<List<ReminderEntity>> observeAll();

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    ReminderEntity getByIdSync(long id);

    @Query("SELECT * FROM reminders WHERE enabled = 1")
    List<ReminderEntity> getEnabledSync();

    @Query("UPDATE reminders SET enabled = :enabled WHERE id = :id")
    void setEnabled(long id, boolean enabled);

    @Query("SELECT COUNT(*) FROM reminders WHERE enabled = 1")
    LiveData<Integer> observeEnabledCount();

    @Query("SELECT * FROM reminders")
    List<ReminderEntity> getAllSync();

    @Query("DELETE FROM reminders")
    void clear();
}
