package com.sangapp.gooddaily.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.sangapp.gooddaily.data.local.entity.HealthProfileEntity;

@Dao
public interface HealthProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) void save(HealthProfileEntity entity);
    @Query("SELECT * FROM health_profile WHERE profileId = 'MAIN' LIMIT 1")
    LiveData<HealthProfileEntity> observe();
    @Query("SELECT * FROM health_profile WHERE profileId = 'MAIN' LIMIT 1")
    HealthProfileEntity getSync();
    @Query("DELETE FROM health_profile") void clear();
}
