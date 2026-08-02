package com.sangapp.gooddaily.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "debt_payments", indices = {@Index(value = {"debtId"})})
public class DebtPaymentEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public long debtId;
    public double amount;
    public long paidAt;
    public String note;

    public DebtPaymentEntity(long debtId, double amount, long paidAt, String note) {
        this.debtId = debtId;
        this.amount = amount;
        this.paidAt = paidAt;
        this.note = note;
    }
}
