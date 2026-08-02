package com.sangapp.gooddaily.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "finance_accounts", indices = {@Index(value = {"code"}, unique = true)})
public class FinanceAccountEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public String code;
    public String name;
    public double openingBalance;
    public boolean active;
    public long createdAt;

    public FinanceAccountEntity(String code, String name, double openingBalance, boolean active, long createdAt) {
        this.code = code;
        this.name = name;
        this.openingBalance = openingBalance;
        this.active = active;
        this.createdAt = createdAt;
    }
}
