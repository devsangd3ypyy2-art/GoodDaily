package com.sangapp.gooddaily.feature.driver.data.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "driver_shifts", indices = {@Index("dateKey"), @Index("startTime")})
public class DriverShiftEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public String dateKey;
    public long startTime;
    public long endTime;
    public double revenue;
    public double bonus;
    public double tips;
    public int orderCount;
    public double distanceKm;
    public double platformFee;
    public double energyCost;
    public double mealCost;
    public double otherCost;
    public double depreciationCost;
    public String area;
    public String note;
    public String status;
    public long createdAt;
    public long updatedAt;
}
