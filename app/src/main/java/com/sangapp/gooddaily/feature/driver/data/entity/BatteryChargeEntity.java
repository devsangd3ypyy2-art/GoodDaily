package com.sangapp.gooddaily.feature.driver.data.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "battery_charges", indices = {@Index("vehicleId"), @Index("chargedAt")})
public class BatteryChargeEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public long vehicleId;
    public long chargedAt;
    public int startPercent;
    public int endPercent;
    public double energyKwh;
    public double cost;
    public double odometerKm;
    public double estimatedRangeKm;
    public String chargerType;
    public String note;
}
