package com.sangapp.gooddaily.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "recurring_transactions", indices = {@Index(value = {"nextRunAt"})})
public class RecurringTransactionEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public String type;
    public double amount;
    public String category;
    public String accountCode;
    public String title;
    public String frequency;
    public long nextRunAt;
    public boolean enabled;
    public long createdAt;

    public RecurringTransactionEntity(String type, double amount, String category, String accountCode,
                                      String title, String frequency, long nextRunAt,
                                      boolean enabled, long createdAt) {
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.accountCode = accountCode;
        this.title = title;
        this.frequency = frequency;
        this.nextRunAt = nextRunAt;
        this.enabled = enabled;
        this.createdAt = createdAt;
    }
}
