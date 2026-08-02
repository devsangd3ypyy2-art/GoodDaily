package com.sangapp.gooddaily.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "finance_categories", indices = {@Index(value = {"name", "type"}, unique = true)})
public class FinanceCategoryEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public String name;
    public String type;
    public String colorHex;
    public boolean active;
    public long createdAt;

    public FinanceCategoryEntity(String name, String type, String colorHex, boolean active, long createdAt) {
        this.name = name;
        this.type = type;
        this.colorHex = colorHex;
        this.active = active;
        this.createdAt = createdAt;
    }
}
