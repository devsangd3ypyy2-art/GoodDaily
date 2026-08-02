package com.sangapp.gooddaily.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "category_budgets", indices = {@Index(value = {"category", "yearMonth"}, unique = true)})
public class CategoryBudgetEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public String category;
    public String yearMonth;
    public double limitAmount;
    public boolean enabled;

    public CategoryBudgetEntity(String category, String yearMonth, double limitAmount, boolean enabled) {
        this.category = category;
        this.yearMonth = yearMonth;
        this.limitAmount = limitAmount;
        this.enabled = enabled;
    }
}
