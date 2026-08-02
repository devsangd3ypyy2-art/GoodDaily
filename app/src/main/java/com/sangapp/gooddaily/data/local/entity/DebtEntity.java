package com.sangapp.gooddaily.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "debts", indices = {@Index(value = {"status"}), @Index(value = {"dueDate"})})
public class DebtEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public String direction;
    public String person;
    public double originalAmount;
    public double remainingAmount;
    public long dueDate;
    public String note;
    public String status;
    public long createdAt;

    public DebtEntity(String direction, String person, double originalAmount, double remainingAmount,
                      long dueDate, String note, String status, long createdAt) {
        this.direction = direction;
        this.person = person;
        this.originalAmount = originalAmount;
        this.remainingAmount = remainingAmount;
        this.dueDate = dueDate;
        this.note = note;
        this.status = status;
        this.createdAt = createdAt;
    }
}
