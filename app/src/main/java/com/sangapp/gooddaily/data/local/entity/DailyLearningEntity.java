package com.sangapp.gooddaily.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "daily_learning")
public class DailyLearningEntity {
    @PrimaryKey @NonNull public String dateKey;
    public int vocabularyCount;
    public int mockScore;
    public long updatedAt;

    public DailyLearningEntity(@NonNull String dateKey, int vocabularyCount, int mockScore, long updatedAt) {
        this.dateKey = dateKey;
        this.vocabularyCount = vocabularyCount;
        this.mockScore = mockScore;
        this.updatedAt = updatedAt;
    }
}
