package com.sangapp.gooddaily.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "reminders")
public class ReminderEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String title;
    public String description;
    public String dateKey;
    public int hour;
    public int minute;
    public String repeatType;
    public String category;
    public boolean enabled;
    public long createdAt;

    public ReminderEntity(String title, String description, String dateKey, int hour, int minute,
                          String repeatType, String category, boolean enabled, long createdAt) {
        this.title = title;
        this.description = description;
        this.dateKey = dateKey;
        this.hour = hour;
        this.minute = minute;
        this.repeatType = repeatType;
        this.category = category;
        this.enabled = enabled;
        this.createdAt = createdAt;
    }
}
