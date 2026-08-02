package com.sangapp.gooddaily.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.sangapp.gooddaily.data.local.GoodDailyDatabase;
import com.sangapp.gooddaily.data.local.entity.BodyRecordEntity;
import com.sangapp.gooddaily.data.local.entity.DailyAmount;
import com.sangapp.gooddaily.util.DateUtils;

import java.util.List;

public class DashboardViewModel extends AndroidViewModel {
    private final GoodDailyDatabase db;

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        db = GoodDailyDatabase.get(application);
    }

    public LiveData<Double> totalBalance() { return db.transactionDao().observeTotalBalance(); }
    public LiveData<Double> todayIncome() { return db.transactionDao().observeTotalByTypeAndRange("INCOME", DateUtils.startOfDay(), DateUtils.endOfDay()); }
    public LiveData<Double> todayExpense() { return db.transactionDao().observeTotalByTypeAndRange("EXPENSE", DateUtils.startOfDay(), DateUtils.endOfDay()); }
    public LiveData<Double> todayCalories() { return db.healthDao().observeCalories(DateUtils.startOfDay(), DateUtils.endOfDay()); }
    public LiveData<BodyRecordEntity> latestBody() { return db.healthDao().observeLatestBody(); }
    public LiveData<Integer> studyMinutes() { return db.plannerDao().observeStudyMinutes(DateUtils.dateKey()); }
    public LiveData<Integer> taskCount() { return db.plannerDao().observeTaskCount(DateUtils.dateKey()); }
    public LiveData<Integer> completedTaskCount() { return db.plannerDao().observeCompletedTaskCount(DateUtils.dateKey()); }
    public LiveData<Integer> habitCount() { return db.habitDao().observeHabitCount(); }
    public LiveData<Integer> checkedHabitCount() { return db.habitDao().observeCheckedCount(DateUtils.dateKey()); }
    public LiveData<List<DailyAmount>> weeklyExpenses() { return db.transactionDao().observeDailyExpenses(DateUtils.startOfDaysAgo(6), DateUtils.endOfDay()); }
}
