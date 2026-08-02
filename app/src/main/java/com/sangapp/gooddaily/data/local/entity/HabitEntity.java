package com.sangapp.gooddaily.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "habits")
public class HabitEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public String name;
    public int targetDaysPerWeek;
    public boolean active;

    public HabitEntity(String name, int targetDaysPerWeek, boolean active) {
        this.name = name; this.targetDaysPerWeek = targetDaysPerWeek; this.active = active;
    }
}
