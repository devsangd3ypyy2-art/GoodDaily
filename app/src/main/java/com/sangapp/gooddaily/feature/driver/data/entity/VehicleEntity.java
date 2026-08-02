package com.sangapp.gooddaily.feature.driver.data.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "vehicles", indices = {@Index(value = {"name", "plateNumber"}, unique = true)})
public class VehicleEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public String name;
    public String plateNumber;
    public String vehicleType;
    public String energyType;
    public double currentOdometerKm;
    public double purchasePrice;
    public long purchaseDate;
    public double batteryNominalCapacity;
    public boolean active;
    public String note;
    public long createdAt;
    public long updatedAt;
}
