package com.sangapp.gooddaily.feature.driver.data;

import androidx.lifecycle.LiveData;

import com.sangapp.gooddaily.feature.driver.data.entity.BatteryChargeEntity;
import com.sangapp.gooddaily.feature.driver.data.entity.DriverShiftEntity;
import com.sangapp.gooddaily.feature.driver.data.entity.FuelLogEntity;
import com.sangapp.gooddaily.feature.driver.data.entity.MaintenanceRecordEntity;
import com.sangapp.gooddaily.feature.driver.data.entity.VehicleEntity;

import java.util.List;

public interface DriverDataSource {
    LiveData<List<DriverShiftEntity>> observeShifts();
    LiveData<List<VehicleEntity>> observeVehicles();
    LiveData<List<MaintenanceRecordEntity>> observeMaintenance();
    LiveData<List<BatteryChargeEntity>> observeCharges(long vehicleId);
    LiveData<List<FuelLogEntity>> observeFuelLogs(long vehicleId);
    void saveShift(DriverShiftEntity entity, Runnable done);
    void deleteShift(DriverShiftEntity entity, Runnable done);
    void saveVehicle(VehicleEntity entity, Runnable done);
    void saveCharge(BatteryChargeEntity entity, Runnable done);
    void saveFuel(FuelLogEntity entity, Runnable done);
    void saveMaintenance(MaintenanceRecordEntity entity, Runnable done);
}
