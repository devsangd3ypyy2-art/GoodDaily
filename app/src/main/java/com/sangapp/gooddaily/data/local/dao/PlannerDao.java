package com.sangapp.gooddaily.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.sangapp.gooddaily.data.local.entity.DailyLearningEntity;
import com.sangapp.gooddaily.data.local.entity.DailyNoteEntity;
import com.sangapp.gooddaily.data.local.entity.DateCount;
import com.sangapp.gooddaily.data.local.entity.ScheduleBlockEntity;
import com.sangapp.gooddaily.data.local.entity.StudySessionEntity;
import com.sangapp.gooddaily.data.local.entity.TaskEntity;
import com.sangapp.gooddaily.data.local.entity.VocabularyEntity;

import java.util.List;

@Dao
public interface PlannerDao {
    @Insert long insertTask(TaskEntity entity);
    @Update void updateTask(TaskEntity entity);
    @Delete void deleteTask(TaskEntity entity);

    @Insert long insertStudy(StudySessionEntity entity);
    @Delete void deleteStudy(StudySessionEntity entity);

    @Insert long insertVocabulary(VocabularyEntity entity);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void upsertNote(DailyNoteEntity entity);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void upsertLearning(DailyLearningEntity entity);

    @Insert long insertSchedule(ScheduleBlockEntity entity);
    @Update void updateSchedule(ScheduleBlockEntity entity);
    @Delete void deleteSchedule(ScheduleBlockEntity entity);

    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertTasks(List<TaskEntity> entities);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertStudies(List<StudySessionEntity> entities);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertVocabularyList(List<VocabularyEntity> entities);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertNotes(List<DailyNoteEntity> entities);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertLearningList(List<DailyLearningEntity> entities);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertSchedules(List<ScheduleBlockEntity> entities);

    @Query("SELECT * FROM tasks WHERE dateKey = :dateKey ORDER BY completed ASC, id DESC")
    LiveData<List<TaskEntity>> observeTasks(String dateKey);

    @Query("SELECT COUNT(*) FROM tasks WHERE dateKey = :dateKey")
    LiveData<Integer> observeTaskCount(String dateKey);

    @Query("SELECT COUNT(*) FROM tasks WHERE dateKey = :dateKey AND completed = 1")
    LiveData<Integer> observeCompletedTaskCount(String dateKey);

    @Query("SELECT * FROM study_sessions WHERE dateKey = :dateKey ORDER BY createdAt DESC")
    LiveData<List<StudySessionEntity>> observeStudies(String dateKey);

    @Query("SELECT COALESCE(SUM(minutes), 0) FROM study_sessions WHERE dateKey = :dateKey")
    LiveData<Integer> observeStudyMinutes(String dateKey);

    @Query("SELECT COALESCE(SUM(minutes), 0) FROM study_sessions WHERE dateKey BETWEEN :startDate AND :endDate")
    LiveData<Integer> observeStudyMinutesRange(String startDate, String endDate);

    @Query("SELECT * FROM vocabulary WHERE dateKey = :dateKey ORDER BY learnedAt DESC")
    LiveData<List<VocabularyEntity>> observeVocabulary(String dateKey);

    @Query("SELECT COUNT(*) FROM vocabulary WHERE dateKey = :dateKey")
    LiveData<Integer> observeVocabularyCount(String dateKey);

    @Query("SELECT * FROM daily_learning WHERE dateKey = :dateKey LIMIT 1")
    LiveData<DailyLearningEntity> observeLearning(String dateKey);

    @Query("SELECT COALESCE(SUM(vocabularyCount), 0) FROM daily_learning WHERE dateKey BETWEEN :startDate AND :endDate")
    LiveData<Integer> observeVocabularyCountRange(String startDate, String endDate);

    @Query("SELECT * FROM daily_notes WHERE dateKey = :dateKey LIMIT 1")
    LiveData<DailyNoteEntity> observeNote(String dateKey);

    @Query("SELECT * FROM daily_notes WHERE content IS NOT NULL AND TRIM(content) != '' ORDER BY dateKey DESC")
    LiveData<List<DailyNoteEntity>> observeAllNotes();

    @Query("SELECT * FROM schedule_blocks WHERE dateKey = :dateKey ORDER BY startMinutes ASC, id ASC")
    LiveData<List<ScheduleBlockEntity>> observeSchedules(String dateKey);

    @Query("SELECT * FROM schedule_blocks WHERE dateKey BETWEEN :startDate AND :endDate ORDER BY dateKey ASC, startMinutes ASC")
    LiveData<List<ScheduleBlockEntity>> observeSchedulesRange(String startDate, String endDate);

    @Query("SELECT dateKey, COUNT(*) AS count FROM schedule_blocks WHERE dateKey BETWEEN :startDate AND :endDate GROUP BY dateKey")
    LiveData<List<DateCount>> observeScheduleCounts(String startDate, String endDate);


    @Query("SELECT * FROM tasks WHERE dateKey = :dateKey ORDER BY completed ASC, id DESC") List<TaskEntity> getTasksByDateSync(String dateKey);
    @Query("SELECT * FROM study_sessions WHERE dateKey = :dateKey ORDER BY createdAt DESC") List<StudySessionEntity> getStudiesByDateSync(String dateKey);
    @Query("SELECT * FROM schedule_blocks WHERE dateKey = :dateKey ORDER BY startMinutes ASC") List<ScheduleBlockEntity> getSchedulesByDateSync(String dateKey);
    @Query("SELECT * FROM daily_notes WHERE dateKey = :dateKey LIMIT 1") DailyNoteEntity getNoteByDateSync(String dateKey);

    @Query("SELECT * FROM tasks") List<TaskEntity> getTasksSync();
    @Query("SELECT * FROM study_sessions") List<StudySessionEntity> getStudiesSync();
    @Query("SELECT * FROM vocabulary") List<VocabularyEntity> getVocabularySync();
    @Query("SELECT * FROM daily_notes") List<DailyNoteEntity> getNotesSync();
    @Query("SELECT COALESCE(SUM(vocabularyCount),0) FROM daily_learning") int getTotalVocabularyCountSync();
    @Query("SELECT * FROM daily_learning") List<DailyLearningEntity> getLearningSync();
    @Query("SELECT * FROM schedule_blocks") List<ScheduleBlockEntity> getSchedulesSync();

    @Query("DELETE FROM tasks") void clearTasks();
    @Query("DELETE FROM study_sessions") void clearStudies();
    @Query("DELETE FROM vocabulary") void clearVocabulary();
    @Query("DELETE FROM daily_notes") void clearNotes();
    @Query("DELETE FROM daily_learning") void clearLearning();
    @Query("DELETE FROM schedule_blocks") void clearSchedules();
}
