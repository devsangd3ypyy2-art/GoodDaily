package com.sangapp.gooddaily.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "meals")
public class MealEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public String name;
    public double calories;
    public double protein;
    public long eatenAt;

    public MealEntity(String name, double calories, double protein, long eatenAt) {
        this.name = name; this.calories = calories; this.protein = protein; this.eatenAt = eatenAt;
    }
}
