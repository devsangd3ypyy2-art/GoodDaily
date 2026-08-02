package com.sangapp.gooddaily.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "schedule_blocks", indices = {@Index(value = {"dateKey"})})
public class ScheduleBlockEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public String dateKey;
    public String title;
    public String category;
    public int startMinutes;
    public int endMinutes;
    public boolean reminderEnabled;
    public String note;
    public long createdAt;

    public ScheduleBlockEntity(String dateKey, String title, String category, int startMinutes,
                               int endMinutes, boolean reminderEnabled, String note, long createdAt) {
        this.dateKey = dateKey;
        this.title = title;
        this.category = category;
        this.startMinutes = startMinutes;
        this.endMinutes = endMinutes;
        this.reminderEnabled = reminderEnabled;
        this.note = note;
        this.createdAt = createdAt;
    }
}
