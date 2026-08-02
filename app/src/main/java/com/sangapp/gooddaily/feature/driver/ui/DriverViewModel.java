package com.sangapp.gooddaily.feature.driver.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.sangapp.gooddaily.feature.driver.data.DriverDataSource;
import com.sangapp.gooddaily.feature.driver.data.DriverRepository;
import com.sangapp.gooddaily.feature.driver.data.entity.BatteryChargeEntity;
import com.sangapp.gooddaily.feature.driver.data.entity.DriverShiftEntity;
import com.sangapp.gooddaily.feature.driver.data.entity.FuelLogEntity;
import com.sangapp.gooddaily.feature.driver.data.entity.MaintenanceRecordEntity;
import com.sangapp.gooddaily.feature.driver.data.entity.VehicleEntity;

import java.util.List;

public class DriverViewModel extends AndroidViewModel {
    private final DriverDataSource repository;

    public DriverViewModel(@NonNull Application application) {
        this(application, new DriverRepository(application));
    }

    DriverViewModel(@NonNull Application application, DriverDataSource repository) {
        super(application);
        this.repository = repository;
    }

    public LiveData<List<DriverShiftEntity>> shifts() { return repository.observeShifts(); }
    public LiveData<List<VehicleEntity>> vehicles() { return repository.observeVehicles(); }
    public LiveData<List<MaintenanceRecordEntity>> maintenance() { return repository.observeMaintenance(); }
    public LiveData<List<BatteryChargeEntity>> charges(long vehicleId) { return repository.observeCharges(vehicleId); }
    public LiveData<List<FuelLogEntity>> fuelLogs(long vehicleId) { return repository.observeFuelLogs(vehicleId); }
    public void saveShift(DriverShiftEntity entity, Runnable done) { repository.saveShift(entity, done); }
    public void deleteShift(DriverShiftEntity entity, Runnable done) { repository.deleteShift(entity, done); }
    public void saveVehicle(VehicleEntity entity, Runnable done) { repository.saveVehicle(entity, done); }
    public void saveCharge(BatteryChargeEntity entity, Runnable done) { repository.saveCharge(entity, done); }
    public void saveFuel(FuelLogEntity entity, Runnable done) { repository.saveFuel(entity, done); }
    public void saveMaintenance(MaintenanceRecordEntity entity, Runnable done) { repository.saveMaintenance(entity, done); }
}
