package com.sangapp.gooddaily.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.sangapp.gooddaily.data.local.entity.PersonalRecordEntity;

import java.util.List;

@Dao
public interface AdvancedRecordDao {
    @Insert long insert(PersonalRecordEntity entity);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertAll(List<PersonalRecordEntity> entities);
    @Update void update(PersonalRecordEntity entity);
    @Delete void delete(PersonalRecordEntity entity);

    @Query("SELECT * FROM personal_records WHERE feature = :feature AND archived = 0 ORDER BY favorite DESC, dateKey DESC, updatedAt DESC")
    LiveData<List<PersonalRecordEntity>> observeFeature(String feature);

    @Query("SELECT * FROM personal_records WHERE feature = :feature AND archived = 0 ORDER BY favorite DESC, dateKey DESC, updatedAt DESC")
    List<PersonalRecordEntity> getFeatureSync(String feature);

    @Query("SELECT * FROM personal_records WHERE id = :id LIMIT 1")
    PersonalRecordEntity getById(long id);

    @Query("SELECT * FROM personal_records WHERE dateKey = :dateKey AND archived = 0 ORDER BY startMinutes ASC, updatedAt DESC")
    LiveData<List<PersonalRecordEntity>> observeDate(String dateKey);

    @Query("SELECT * FROM personal_records WHERE archived = 0 AND (title LIKE '%' || :query || '%' OR details LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%') ORDER BY updatedAt DESC LIMIT 200")
    LiveData<List<PersonalRecordEntity>> search(String query);

    @Query("SELECT COUNT(*) FROM personal_records WHERE feature = :feature AND archived = 0")
    LiveData<Integer> observeCount(String feature);

    @Query("SELECT COUNT(*) FROM personal_records WHERE feature = :feature AND archived = 0")
    int countFeatureSync(String feature);

    @Query("SELECT COALESCE(SUM(secondaryValue),0) FROM personal_records WHERE feature = :feature AND archived = 0")
    double sumSecondarySync(String feature);

    @Query("SELECT COALESCE(SUM(numericValue),0) FROM personal_records WHERE feature = :feature AND archived = 0 AND dateKey BETWEEN :startDate AND :endDate")
    LiveData<Double> observeSum(String feature, String startDate, String endDate);

    @Query("SELECT * FROM personal_records WHERE module = :module AND archived = 0 ORDER BY updatedAt DESC")
    List<PersonalRecordEntity> getModuleSync(String module);

    @Query("SELECT * FROM personal_records WHERE dateKey BETWEEN :startDate AND :endDate AND archived = 0 ORDER BY dateKey, startMinutes")
    List<PersonalRecordEntity> getRangeSync(String startDate, String endDate);

    @Query("DELETE FROM personal_records") void clear();
}
