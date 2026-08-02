package com.sangapp.gooddaily.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks")
public class TaskEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public String title;
    public int expectedMinutes;
    public boolean completed;
    public String dateKey;

    public TaskEntity(String title, int expectedMinutes, boolean completed, String dateKey) {
        this.title = title; this.expectedMinutes = expectedMinutes; this.completed = completed; this.dateKey = dateKey;
    }
}
