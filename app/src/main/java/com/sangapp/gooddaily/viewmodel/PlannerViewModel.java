package com.sangapp.gooddaily.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.sangapp.gooddaily.data.local.GoodDailyDatabase;
import com.sangapp.gooddaily.data.local.entity.DailyLearningEntity;
import com.sangapp.gooddaily.data.local.entity.DailyNoteEntity;
import com.sangapp.gooddaily.data.local.entity.HabitCheckInEntity;
import com.sangapp.gooddaily.data.local.entity.HabitEntity;
import com.sangapp.gooddaily.data.local.entity.ScheduleBlockEntity;
import com.sangapp.gooddaily.data.local.entity.StudySessionEntity;
import com.sangapp.gooddaily.data.local.entity.TaskEntity;
import com.sangapp.gooddaily.notification.ScheduleReminderScheduler;
import com.sangapp.gooddaily.util.AppExecutors;
import com.sangapp.gooddaily.util.DateUtils;

import java.util.List;

public class PlannerViewModel extends AndroidViewModel {
    private final GoodDailyDatabase db;
    private final MutableLiveData<String> selectedDate = new MutableLiveData<>(DateUtils.dateKey());

    private final LiveData<List<TaskEntity>> tasks;
    private final LiveData<List<StudySessionEntity>> studies;
    private final LiveData<Integer> studyMinutes;
    private final LiveData<List<HabitCheckInEntity>> checkIns;
    private final LiveData<DailyNoteEntity> note;
    private final LiveData<DailyLearningEntity> learning;
    private final LiveData<List<ScheduleBlockEntity>> schedules;
    private final LiveData<List<ScheduleBlockEntity>> weekSchedules;
    private final LiveData<Integer> weekVocabularyCount;
    private final LiveData<Integer> weekStudyMinutes;

    public PlannerViewModel(@NonNull Application application) {
        super(application);
        db = GoodDailyDatabase.get(application);
        tasks = Transformations.switchMap(selectedDate, db.plannerDao()::observeTasks);
        studies = Transformations.switchMap(selectedDate, db.plannerDao()::observeStudies);
        studyMinutes = Transformations.switchMap(selectedDate, db.plannerDao()::observeStudyMinutes);
        checkIns = Transformations.switchMap(selectedDate, db.habitDao()::observeCheckIns);
        note = Transformations.switchMap(selectedDate, db.plannerDao()::observeNote);
        learning = Transformations.switchMap(selectedDate, db.plannerDao()::observeLearning);
        schedules = Transformations.switchMap(selectedDate, db.plannerDao()::observeSchedules);
        weekSchedules = Transformations.switchMap(selectedDate, date ->
                db.plannerDao().observeSchedulesRange(DateUtils.startOfWeekKey(date), DateUtils.endOfWeekKey(date)));
        weekVocabularyCount = Transformations.switchMap(selectedDate, date ->
                db.plannerDao().observeVocabularyCountRange(DateUtils.startOfWeekKey(date), DateUtils.endOfWeekKey(date)));
        weekStudyMinutes = Transformations.switchMap(selectedDate, date ->
                db.plannerDao().observeStudyMinutesRange(DateUtils.startOfWeekKey(date), DateUtils.endOfWeekKey(date)));
    }

    public LiveData<String> selectedDate() { return selectedDate; }
    public void selectDate(String dateKey) {
        if (dateKey != null && !dateKey.trim().isEmpty()) selectedDate.setValue(dateKey);
    }

    private String currentDate() {
        String value = selectedDate.getValue();
        return value == null ? DateUtils.dateKey() : value;
    }

    public LiveData<List<TaskEntity>> tasks() { return tasks; }
    public LiveData<List<StudySessionEntity>> studies() { return studies; }
    public LiveData<Integer> studyMinutes() { return studyMinutes; }
    public LiveData<List<HabitEntity>> habits() { return db.habitDao().observeHabits(); }
    public LiveData<List<HabitCheckInEntity>> checkIns() { return checkIns; }
    public LiveData<List<HabitCheckInEntity>> allCheckIns() { return db.habitDao().observeAllCheckIns(); }
    public LiveData<DailyNoteEntity> note() { return note; }
    public LiveData<List<DailyNoteEntity>> noteHistory() { return db.plannerDao().observeAllNotes(); }
    public LiveData<DailyLearningEntity> learning() { return learning; }
    public LiveData<Integer> weekVocabularyCount() { return weekVocabularyCount; }
    public LiveData<Integer> weekStudyMinutes() { return weekStudyMinutes; }
    public LiveData<List<ScheduleBlockEntity>> schedules() { return schedules; }
    public LiveData<List<ScheduleBlockEntity>> weekSchedules() { return weekSchedules; }

    public void addTask(String title, int minutes) {
        String date = currentDate();
        AppExecutors.database().execute(() -> db.plannerDao().insertTask(new TaskEntity(title, minutes, false, date)));
    }

    public void toggleTask(TaskEntity task, boolean completed) {
        task.completed = completed;
        AppExecutors.database().execute(() -> db.plannerDao().updateTask(task));
    }

    public void deleteTask(TaskEntity task) {
        AppExecutors.database().execute(() -> db.plannerDao().deleteTask(task));
    }

    public void addStudy(String subject, int minutes, String note) {
        String date = currentDate();
        AppExecutors.database().execute(() -> db.plannerDao().insertStudy(
                new StudySessionEntity(subject, minutes, note, date, System.currentTimeMillis())));
    }

    public void deleteStudy(StudySessionEntity study) {
        AppExecutors.database().execute(() -> db.plannerDao().deleteStudy(study));
    }

    public void saveDailyLearning(int vocabularyCount, int mockScore) {
        String date = currentDate();
        AppExecutors.database().execute(() -> db.plannerDao().upsertLearning(
                new DailyLearningEntity(date, Math.max(0, vocabularyCount), Math.max(0, mockScore), System.currentTimeMillis())));
    }

    public void addHabit(String name, int target) {
        AppExecutors.database().execute(() -> db.habitDao().insertHabit(
                new HabitEntity(name, Math.max(1, Math.min(target, 7)), true)));
    }

    public void setHabitChecked(long habitId, boolean checked) {
        String date = currentDate();
        AppExecutors.database().execute(() -> {
            if (checked) db.habitDao().insertCheckIn(new HabitCheckInEntity(habitId, date, System.currentTimeMillis()));
            else db.habitDao().deleteCheckIn(habitId, date);
        });
    }

    public void saveNote(String content, String tags, String mood) {
        String date = currentDate();
        AppExecutors.database().execute(() -> db.plannerDao().upsertNote(
                new DailyNoteEntity(date, content, tags, mood, System.currentTimeMillis())));
    }

    public void saveSchedule(ScheduleBlockEntity block) {
        block.dateKey = currentDate();
        AppExecutors.database().execute(() -> {
            if (block.id == 0) block.id = db.plannerDao().insertSchedule(block);
            else db.plannerDao().updateSchedule(block);
            ScheduleReminderScheduler.schedule(getApplication(), block);
        });
    }

    public void deleteSchedule(ScheduleBlockEntity block) {
        AppExecutors.database().execute(() -> {
            db.plannerDao().deleteSchedule(block);
            ScheduleReminderScheduler.cancel(getApplication(), block.id);
        });
    }
}
