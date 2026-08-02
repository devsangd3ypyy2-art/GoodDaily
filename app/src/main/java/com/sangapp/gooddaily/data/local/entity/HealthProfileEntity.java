package com.sangapp.gooddaily.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "health_profile")
public class HealthProfileEntity {
    @PrimaryKey @NonNull public String profileId;
    public int birthYear;
    public String gender;
    public String activityLevel;
    public String goalType;
    public double targetWeight;
    public double manualCalorieTarget;
    public long updatedAt;

    public HealthProfileEntity(@NonNull String profileId, int birthYear, String gender,
                               String activityLevel, String goalType, double targetWeight,
                               double manualCalorieTarget, long updatedAt) {
        this.profileId = profileId;
        this.birthYear = birthYear;
        this.gender = gender;
        this.activityLevel = activityLevel;
        this.goalType = goalType;
        this.targetWeight = targetWeight;
        this.manualCalorieTarget = manualCalorieTarget;
        this.updatedAt = updatedAt;
    }
}
