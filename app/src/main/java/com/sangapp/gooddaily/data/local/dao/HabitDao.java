package com.sangapp.gooddaily.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.sangapp.gooddaily.data.local.entity.HabitCheckInEntity;
import com.sangapp.gooddaily.data.local.entity.HabitEntity;

import java.util.List;

@Dao
public interface HabitDao {
    @Insert long insertHabit(HabitEntity entity);
    @Insert(onConflict = OnConflictStrategy.IGNORE) long insertCheckIn(HabitCheckInEntity entity);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertHabits(List<HabitEntity> entities);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertCheckIns(List<HabitCheckInEntity> entities);

    @Query("SELECT * FROM habits WHERE active = 1 ORDER BY id DESC")
    LiveData<List<HabitEntity>> observeHabits();

    @Query("SELECT * FROM habit_checkins WHERE dateKey = :dateKey")
    LiveData<List<HabitCheckInEntity>> observeCheckIns(String dateKey);

    @Query("SELECT * FROM habit_checkins ORDER BY dateKey DESC")
    LiveData<List<HabitCheckInEntity>> observeAllCheckIns();

    @Query("SELECT COUNT(*) FROM habits WHERE active = 1")
    LiveData<Integer> observeHabitCount();

    @Query("SELECT COUNT(*) FROM habit_checkins WHERE dateKey = :dateKey")
    LiveData<Integer> observeCheckedCount(String dateKey);

    @Query("DELETE FROM habit_checkins WHERE habitId = :habitId AND dateKey = :dateKey")
    void deleteCheckIn(long habitId, String dateKey);

    @Query("SELECT * FROM habits") List<HabitEntity> getHabitsSync();
    @Query("SELECT * FROM habit_checkins") List<HabitCheckInEntity> getCheckInsSync();
    @Query("DELETE FROM habit_checkins") void clearCheckIns();
    @Query("DELETE FROM habits") void clearHabits();
}
