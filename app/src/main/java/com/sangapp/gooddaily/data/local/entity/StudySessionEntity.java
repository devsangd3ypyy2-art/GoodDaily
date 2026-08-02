package com.sangapp.gooddaily.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "study_sessions")
public class StudySessionEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public String subject;
    public int minutes;
    public String note;
    public String dateKey;
    public long createdAt;

    public StudySessionEntity(String subject, int minutes, String note, String dateKey, long createdAt) {
        this.subject = subject; this.minutes = minutes; this.note = note; this.dateKey = dateKey; this.createdAt = createdAt;
    }
}
