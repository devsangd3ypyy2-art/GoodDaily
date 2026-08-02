package com.sangapp.gooddaily.feature.driver.ui;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.sangapp.gooddaily.feature.driver.data.DriverDataSource;
import com.sangapp.gooddaily.feature.driver.data.entity.BatteryChargeEntity;
import com.sangapp.gooddaily.feature.driver.data.entity.DriverShiftEntity;
import com.sangapp.gooddaily.feature.driver.data.entity.FuelLogEntity;
import com.sangapp.gooddaily.feature.driver.data.entity.MaintenanceRecordEntity;
import com.sangapp.gooddaily.feature.driver.data.entity.VehicleEntity;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class DriverViewModelTest {
    @Test public void saveShift_isDelegatedToRepository() {
        FakeSource source = new FakeSource();
        Application app = ApplicationProvider.getApplicationContext();
        DriverViewModel viewModel = new DriverViewModel(app, source);
        viewModel.saveShift(new DriverShiftEntity(), null);
        assertEquals(1, source.savedShifts);
    }

    private static class FakeSource implements DriverDataSource {
        int savedShifts;
        MutableLiveData<List<DriverShiftEntity>> shifts = new MutableLiveData<>(new ArrayList<>());
        MutableLiveData<List<VehicleEntity>> vehicles = new MutableLiveData<>(new ArrayList<>());
        MutableLiveData<List<MaintenanceRecordEntity>> maintenance = new MutableLiveData<>(new ArrayList<>());
        MutableLiveData<List<BatteryChargeEntity>> charges = new MutableLiveData<>(new ArrayList<>());
        MutableLiveData<List<FuelLogEntity>> fuel = new MutableLiveData<>(new ArrayList<>());
        @Override public LiveData<List<DriverShiftEntity>> observeShifts() { return shifts; }
        @Override public LiveData<List<VehicleEntity>> observeVehicles() { return vehicles; }
        @Override public LiveData<List<MaintenanceRecordEntity>> observeMaintenance() { return maintenance; }
        @Override public LiveData<List<BatteryChargeEntity>> observeCharges(long vehicleId) { return charges; }
        @Override public LiveData<List<FuelLogEntity>> observeFuelLogs(long vehicleId) { return fuel; }
        @Override public void saveShift(DriverShiftEntity entity, Runnable done) { savedShifts++; if(done!=null)done.run(); }
        @Override public void deleteShift(DriverShiftEntity entity, Runnable done) { if(done!=null)done.run(); }
        @Override public void saveVehicle(VehicleEntity entity, Runnable done) { if(done!=null)done.run(); }
        @Override public void saveCharge(BatteryChargeEntity entity, Runnable done) { if(done!=null)done.run(); }
        @Override public void saveFuel(FuelLogEntity entity, Runnable done) { if(done!=null)done.run(); }
        @Override public void saveMaintenance(MaintenanceRecordEntity entity, Runnable done) { if(done!=null)done.run(); }
    }
}
