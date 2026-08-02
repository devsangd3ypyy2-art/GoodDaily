package com.sangapp.gooddaily.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "daily_notes")
public class DailyNoteEntity {
    @PrimaryKey @NonNull public String dateKey;
    public String content;
    public String tags;
    public String mood;
    public long updatedAt;

    public DailyNoteEntity(@NonNull String dateKey, String content, String tags, String mood, long updatedAt) {
        this.dateKey = dateKey;
        this.content = content;
        this.tags = tags;
        this.mood = mood;
        this.updatedAt = updatedAt;
    }
}
