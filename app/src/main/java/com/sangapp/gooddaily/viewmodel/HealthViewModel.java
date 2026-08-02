package com.sangapp.gooddaily.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.sangapp.gooddaily.data.local.GoodDailyDatabase;
import com.sangapp.gooddaily.data.local.entity.BodyRecordEntity;
import com.sangapp.gooddaily.data.local.entity.MealEntity;
import com.sangapp.gooddaily.util.AppExecutors;
import com.sangapp.gooddaily.util.DateUtils;

import java.util.List;

public class HealthViewModel extends AndroidViewModel {
    private final GoodDailyDatabase db;

    public HealthViewModel(@NonNull Application application) {
        super(application);
        db = GoodDailyDatabase.get(application);
    }

    public LiveData<BodyRecordEntity> latestBody() { return db.healthDao().observeLatestBody(); }
    public LiveData<List<BodyRecordEntity>> bodies() { return db.healthDao().observeBodies(); }
    public LiveData<List<MealEntity>> meals() { return db.healthDao().observeMeals(); }
    public LiveData<Double> calories() { return db.healthDao().observeCalories(DateUtils.startOfDay(), DateUtils.endOfDay()); }
    public LiveData<Double> protein() { return db.healthDao().observeProtein(DateUtils.startOfDay(), DateUtils.endOfDay()); }

    public void addBody(double weight, double height, double bodyFat, double muscle) {
        AppExecutors.database().execute(() -> db.healthDao().insertBody(new BodyRecordEntity(weight, height, bodyFat, muscle, System.currentTimeMillis())));
    }

    public void addMeal(String name, double calories, double protein) {
        AppExecutors.database().execute(() -> db.healthDao().insertMeal(new MealEntity(name, calories, protein, System.currentTimeMillis())));
    }
}
