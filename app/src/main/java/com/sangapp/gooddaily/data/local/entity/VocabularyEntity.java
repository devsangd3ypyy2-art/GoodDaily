package com.sangapp.gooddaily.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "vocabulary")
public class VocabularyEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public String word;
    public String meaning;
    public String dateKey;
    public long learnedAt;

    public VocabularyEntity(String word, String meaning, String dateKey, long learnedAt) {
        this.word = word; this.meaning = meaning; this.dateKey = dateKey; this.learnedAt = learnedAt;
    }
}
