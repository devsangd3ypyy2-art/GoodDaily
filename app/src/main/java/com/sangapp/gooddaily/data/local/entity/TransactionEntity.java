package com.sangapp.gooddaily.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions")
public class TransactionEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public String type;
    public double amount;
    public String category;
    public String account;
    public String note;
    public long transactionTime;

    public TransactionEntity(String type, double amount, String category, String account, String note, long transactionTime) {
        this.type = type; this.amount = amount; this.category = category; this.account = account; this.note = note; this.transactionTime = transactionTime;
    }
}
