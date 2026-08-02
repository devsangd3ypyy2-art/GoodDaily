package com.sangapp.gooddaily.feature.driver.data.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "fuel_logs", indices = {@Index("vehicleId"), @Index("fueledAt")})
public class FuelLogEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public long vehicleId;
    public long fueledAt;
    public double liters;
    public double cost;
    public double odometerKm;
    public boolean fullTank;
    public String station;
    public String note;
}
