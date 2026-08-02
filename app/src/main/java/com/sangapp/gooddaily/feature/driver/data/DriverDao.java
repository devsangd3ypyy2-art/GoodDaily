package com.sangapp.gooddaily.feature.driver.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.sangapp.gooddaily.feature.driver.data.entity.BatteryChargeEntity;
import com.sangapp.gooddaily.feature.driver.data.entity.DriverShiftEntity;
import com.sangapp.gooddaily.feature.driver.data.entity.FuelLogEntity;
import com.sangapp.gooddaily.feature.driver.data.entity.MaintenanceRecordEntity;
import com.sangapp.gooddaily.feature.driver.data.entity.VehicleEntity;

import java.util.List;

@Dao
public interface DriverDao {
    @Insert long insertShift(DriverShiftEntity entity);
    @Update void updateShift(DriverShiftEntity entity);
    @Delete void deleteShift(DriverShiftEntity entity);
    @Query("SELECT * FROM driver_shifts ORDER BY startTime DESC") LiveData<List<DriverShiftEntity>> observeShifts();
    @Query("SELECT * FROM driver_shifts WHERE dateKey BETWEEN :startDate AND :endDate ORDER BY startTime DESC") LiveData<List<DriverShiftEntity>> observeShiftsBetween(String startDate, String endDate);
    @Query("SELECT COALESCE(SUM(revenue + bonus + tips - platformFee - energyCost - mealCost - otherCost - depreciationCost), 0) FROM driver_shifts WHERE dateKey BETWEEN :startDate AND :endDate") LiveData<Double> observeNetProfit(String startDate, String endDate);

    @Insert long insertVehicle(VehicleEntity entity);
    @Update void updateVehicle(VehicleEntity entity);
    @Delete void deleteVehicle(VehicleEntity entity);
    @Query("SELECT * FROM vehicles ORDER BY active DESC, name ASC") LiveData<List<VehicleEntity>> observeVehicles();
    @Query("SELECT * FROM vehicles WHERE active = 1 ORDER BY name LIMIT 1") VehicleEntity firstActiveVehicle();

    @Insert long insertFuel(FuelLogEntity entity);
    @Update void updateFuel(FuelLogEntity entity);
    @Delete void deleteFuel(FuelLogEntity entity);
    @Query("SELECT * FROM fuel_logs WHERE vehicleId = :vehicleId ORDER BY fueledAt DESC") LiveData<List<FuelLogEntity>> observeFuelLogs(long vehicleId);

    @Insert long insertCharge(BatteryChargeEntity entity);
    @Update void updateCharge(BatteryChargeEntity entity);
    @Delete void deleteCharge(BatteryChargeEntity entity);
    @Query("SELECT * FROM battery_charges WHERE vehicleId = :vehicleId ORDER BY chargedAt DESC") LiveData<List<BatteryChargeEntity>> observeCharges(long vehicleId);
    @Query("SELECT COUNT(*) FROM battery_charges WHERE vehicleId = :vehicleId AND startPercent <= 20 AND endPercent >= 80") int estimatedFullCycles(long vehicleId);

    @Insert long insertMaintenance(MaintenanceRecordEntity entity);
    @Update void updateMaintenance(MaintenanceRecordEntity entity);
    @Delete void deleteMaintenance(MaintenanceRecordEntity entity);
    @Query("SELECT * FROM maintenance_records ORDER BY nextDueAt ASC, nextDueOdometerKm ASC") LiveData<List<MaintenanceRecordEntity>> observeMaintenance();
    @Query("SELECT * FROM maintenance_records WHERE completed = 0 AND ((nextDueAt > 0 AND nextDueAt <= :untilTime) OR (nextDueOdometerKm > 0 AND nextDueOdometerKm <= :odometerKm)) ORDER BY nextDueAt ASC") List<MaintenanceRecordEntity> dueMaintenance(long untilTime, double odometerKm);
}
