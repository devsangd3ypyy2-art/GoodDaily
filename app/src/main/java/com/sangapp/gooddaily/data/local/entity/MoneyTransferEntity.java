package com.sangapp.gooddaily.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "money_transfers", indices = {
        @Index(value = {"fromAccountCode"}), @Index(value = {"toAccountCode"}), @Index(value = {"transferTime"})
})
public class MoneyTransferEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public String fromAccountCode;
    public String toAccountCode;
    public double amount;
    public double fee;
    public String note;
    public long transferTime;

    public MoneyTransferEntity(String fromAccountCode, String toAccountCode, double amount,
                               double fee, String note, long transferTime) {
        this.fromAccountCode = fromAccountCode;
        this.toAccountCode = toAccountCode;
        this.amount = amount;
        this.fee = fee;
        this.note = note;
        this.transferTime = transferTime;
    }
}
