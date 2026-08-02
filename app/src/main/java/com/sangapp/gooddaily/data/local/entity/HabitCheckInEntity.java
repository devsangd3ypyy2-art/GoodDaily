package com.sangapp.gooddaily.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "habit_checkins", indices = {@Index(value = {"habitId", "dateKey"}, unique = true)})
public class HabitCheckInEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public long habitId;
    public String dateKey;
    public long checkedAt;

    public HabitCheckInEntity(long habitId, String dateKey, long checkedAt) {
        this.habitId = habitId; this.dateKey = dateKey; this.checkedAt = checkedAt;
    }
}
