package com.sangapp.gooddaily.data.local.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "body_records")
public class BodyRecordEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public double weight;
    public double height;
    public double bodyFatPercent;
    public double muscleMass;
    public double waist;
    public double chest;
    public double arm;
    public double thigh;
    public long recordedAt;

    public BodyRecordEntity(double weight, double height, double bodyFatPercent, double muscleMass,
                            double waist, double chest, double arm, double thigh, long recordedAt) {
        this.weight = weight;
        this.height = height;
        this.bodyFatPercent = bodyFatPercent;
        this.muscleMass = muscleMass;
        this.waist = waist;
        this.chest = chest;
        this.arm = arm;
        this.thigh = thigh;
        this.recordedAt = recordedAt;
    }

    @Ignore
    public BodyRecordEntity(double weight, double height, double bodyFatPercent, double muscleMass, long recordedAt) {
        this(weight, height, bodyFatPercent, muscleMass, 0, 0, 0, 0, recordedAt);
    }

    public double bmi() {
        if (height <= 0) return 0;
        double meters = height / 100.0;
        return weight / (meters * meters);
    }
}
