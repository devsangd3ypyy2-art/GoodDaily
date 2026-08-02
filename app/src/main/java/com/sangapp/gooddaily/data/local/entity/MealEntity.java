package com.sangapp.gooddaily.data.local.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "meals")
public class MealEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public String name;
    public double calories;
    public double protein;
    public double carbs;
    public double fat;
    public double grams;
    public String mealType;
    public String imagePath;
    public long eatenAt;

    public MealEntity(String name, double calories, double protein, double carbs, double fat,
                      double grams, String mealType, String imagePath, long eatenAt) {
        this.name = name;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
        this.grams = grams;
        this.mealType = mealType;
        this.imagePath = imagePath;
        this.eatenAt = eatenAt;
    }

    @Ignore
    public MealEntity(String name, double calories, double protein, long eatenAt) {
        this(name, calories, protein, 0, 0, 0, "Khác", "", eatenAt);
    }
}
