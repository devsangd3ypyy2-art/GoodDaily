package com.sangapp.gooddaily.feature.driver.data;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.sangapp.gooddaily.data.local.GoodDailyDatabase;
import com.sangapp.gooddaily.feature.driver.data.entity.BatteryChargeEntity;
import com.sangapp.gooddaily.feature.driver.data.entity.DriverShiftEntity;
import com.sangapp.gooddaily.feature.driver.data.entity.FuelLogEntity;
import com.sangapp.gooddaily.feature.driver.data.entity.MaintenanceRecordEntity;
import com.sangapp.gooddaily.feature.driver.data.entity.VehicleEntity;
import com.sangapp.gooddaily.util.AppExecutors;

import java.util.List;

public class DriverRepository implements DriverDataSource {
    private final DriverDao dao;

    public DriverRepository(Context context) { dao = GoodDailyDatabase.get(context).driverDao(); }

    public LiveData<List<DriverShiftEntity>> observeShifts() { return dao.observeShifts(); }
    public LiveData<List<VehicleEntity>> observeVehicles() { return dao.observeVehicles(); }
    public LiveData<List<MaintenanceRecordEntity>> observeMaintenance() { return dao.observeMaintenance(); }
    public LiveData<List<BatteryChargeEntity>> observeCharges(long vehicleId) { return dao.observeCharges(vehicleId); }
    public LiveData<List<FuelLogEntity>> observeFuelLogs(long vehicleId) { return dao.observeFuelLogs(vehicleId); }

    public void saveShift(DriverShiftEntity entity, Runnable done) { AppExecutors.io().execute(() -> { if (entity.id == 0) entity.id = dao.insertShift(entity); else dao.updateShift(entity); finish(done); }); }
    public void deleteShift(DriverShiftEntity entity, Runnable done) { AppExecutors.io().execute(() -> { dao.deleteShift(entity); finish(done); }); }
    public void saveVehicle(VehicleEntity entity, Runnable done) { AppExecutors.io().execute(() -> { if (entity.id == 0) entity.id = dao.insertVehicle(entity); else dao.updateVehicle(entity); finish(done); }); }
    public void saveCharge(BatteryChargeEntity entity, Runnable done) { AppExecutors.io().execute(() -> { if (entity.id == 0) entity.id = dao.insertCharge(entity); else dao.updateCharge(entity); finish(done); }); }
    public void saveFuel(FuelLogEntity entity, Runnable done) { AppExecutors.io().execute(() -> { if (entity.id == 0) entity.id = dao.insertFuel(entity); else dao.updateFuel(entity); finish(done); }); }
    public void saveMaintenance(MaintenanceRecordEntity entity, Runnable done) { AppExecutors.io().execute(() -> { if (entity.id == 0) entity.id = dao.insertMaintenance(entity); else dao.updateMaintenance(entity); finish(done); }); }

    private void finish(Runnable done) { if (done != null) AppExecutors.main().post(done); }
}
