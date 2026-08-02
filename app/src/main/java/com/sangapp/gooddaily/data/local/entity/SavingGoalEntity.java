package com.sangapp.gooddaily.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "saving_goals")
public class SavingGoalEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public String name;
    public double targetAmount;
    public double savedAmount;
    public long targetDate;
    public String colorHex;
    public boolean completed;
    public long createdAt;

    public SavingGoalEntity(String name, double targetAmount, double savedAmount, long targetDate,
                            String colorHex, boolean completed, long createdAt) {
        this.name = name;
        this.targetAmount = targetAmount;
        this.savedAmount = savedAmount;
        this.targetDate = targetDate;
        this.colorHex = colorHex;
        this.completed = completed;
        this.createdAt = createdAt;
    }
}
