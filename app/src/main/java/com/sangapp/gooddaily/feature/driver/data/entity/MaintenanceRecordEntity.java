package com.sangapp.gooddaily.feature.driver.data.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "maintenance_records", indices = {@Index("vehicleId"), @Index("performedAt"), @Index("nextDueAt")})
public class MaintenanceRecordEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public long vehicleId;
    public String itemType;
    public String title;
    public long performedAt;
    public double odometerKm;
    public double cost;
    public long nextDueAt;
    public double nextDueOdometerKm;
    public String condition;
    public String note;
    public boolean completed;
}
