package com.sangapp.gooddaily.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.sangapp.gooddaily.data.local.entity.BodyRecordEntity;
import com.sangapp.gooddaily.data.local.entity.MealEntity;

import java.util.List;

@Dao
public interface HealthDao {
    @Insert long insertBody(BodyRecordEntity entity);
    @Insert long insertMeal(MealEntity entity);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertBodies(List<BodyRecordEntity> entities);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertMeals(List<MealEntity> entities);

    @Query("SELECT * FROM body_records ORDER BY recordedAt DESC LIMIT 1")
    LiveData<BodyRecordEntity> observeLatestBody();

    @Query("SELECT * FROM body_records ORDER BY recordedAt DESC")
    LiveData<List<BodyRecordEntity>> observeBodies();

    @Query("SELECT * FROM meals ORDER BY eatenAt DESC")
    LiveData<List<MealEntity>> observeMeals();

    @Query("SELECT COALESCE(SUM(calories), 0) FROM meals WHERE eatenAt BETWEEN :start AND :end")
    LiveData<Double> observeCalories(long start, long end);

    @Query("SELECT COALESCE(SUM(protein), 0) FROM meals WHERE eatenAt BETWEEN :start AND :end")
    LiveData<Double> observeProtein(long start, long end);

    @Query("SELECT * FROM body_records ORDER BY recordedAt DESC") List<BodyRecordEntity> getBodiesSync();
    @Query("SELECT * FROM meals ORDER BY eatenAt DESC") List<MealEntity> getMealsSync();
    @Query("DELETE FROM body_records") void clearBodies();
    @Query("DELETE FROM meals") void clearMeals();
}
