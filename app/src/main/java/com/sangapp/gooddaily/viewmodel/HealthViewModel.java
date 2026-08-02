package com.sangapp.gooddaily.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    public LiveData<Double> carbs() { return db.healthDao().observeCarbs(DateUtils.startOfDay(), DateUtils.endOfDay()); }
    public LiveData<Double> fat() { return db.healthDao().observeFat(DateUtils.startOfDay(), DateUtils.endOfDay()); }

    public void saveBody(@Nullable BodyRecordEntity existing,
                         double weight, double height, double bodyFat, double muscle,
                         double waist, double chest, double arm, double thigh) {
        AppExecutors.database().execute(() -> {
            if (existing == null) {
                db.healthDao().insertBody(new BodyRecordEntity(weight, height, bodyFat, muscle,
                        waist, chest, arm, thigh, System.currentTimeMillis()));
            } else {
                existing.weight = weight;
                existing.height = height;
                existing.bodyFatPercent = bodyFat;
                existing.muscleMass = muscle;
                existing.waist = waist;
                existing.chest = chest;
                existing.arm = arm;
                existing.thigh = thigh;
                db.healthDao().updateBody(existing);
            }
        });
    }

    public void deleteBody(BodyRecordEntity entity) {
        AppExecutors.database().execute(() -> db.healthDao().deleteBody(entity));
    }

    public void saveMeal(@Nullable MealEntity existing, String name, String mealType,
                         double grams, double calories, double protein, double carbs, double fat) {
        saveMeal(existing, name, mealType, grams, calories, protein, carbs, fat,
                existing == null ? "" : existing.imagePath);
    }

    public void saveMeal(@Nullable MealEntity existing, String name, String mealType,
                         double grams, double calories, double protein, double carbs, double fat,
                         String imagePath) {
        AppExecutors.database().execute(() -> {
            if (existing == null) {
                db.healthDao().insertMeal(new MealEntity(name, calories, protein, carbs, fat,
                        grams, mealType, imagePath == null ? "" : imagePath, System.currentTimeMillis()));
            } else {
                existing.name = name;
                existing.mealType = mealType;
                existing.grams = grams;
                existing.calories = calories;
                existing.protein = protein;
                existing.carbs = carbs;
                existing.fat = fat;
                existing.imagePath = imagePath == null ? "" : imagePath;
                db.healthDao().updateMeal(existing);
            }
        });
    }

    public void deleteMeal(MealEntity entity) {
        AppExecutors.database().execute(() -> db.healthDao().deleteMeal(entity));
    }
}
