package com.sangapp.gooddaily.feature.metaphysics.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DivinationDao {
    @Insert long insert(DivinationSessionEntity entity);
    @Update void update(DivinationSessionEntity entity);
    @Delete void delete(DivinationSessionEntity entity);

    @Query("SELECT * FROM divination_sessions ORDER BY castTime DESC")
    LiveData<List<DivinationSessionEntity>> observeAll();

    @Query("SELECT * FROM divination_sessions WHERE system = :system ORDER BY castTime DESC")
    LiveData<List<DivinationSessionEntity>> observeBySystem(String system);

    @Query("SELECT * FROM divination_sessions WHERE id = :id LIMIT 1")
    DivinationSessionEntity findById(long id);

    @Query("SELECT * FROM divination_sessions WHERE question LIKE '%' || :query || '%' OR interpretation LIKE '%' || :query || '%' OR verification LIKE '%' || :query || '%' ORDER BY castTime DESC")
    LiveData<List<DivinationSessionEntity>> search(String query);

    @Query("SELECT COUNT(*) FROM divination_sessions")
    int count();
}
