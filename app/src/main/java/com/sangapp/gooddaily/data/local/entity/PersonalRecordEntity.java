package com.sangapp.gooddaily.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "personal_records", indices = {
        @Index(value = {"module"}),
        @Index(value = {"feature"}),
        @Index(value = {"dateKey"}),
        @Index(value = {"updatedAt"})
})
public class PersonalRecordEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public String module;
    public String feature;
    public String title;
    public String details;
    public double numericValue;
    public double secondaryValue;
    public int countValue;
    public String dateKey;
    public int startMinutes;
    public int endMinutes;
    public String status;
    public String tags;
    public String attachmentUri;
    public boolean favorite;
    public boolean archived;
    public long createdAt;
    public long updatedAt;

    public PersonalRecordEntity(String module, String feature, String title, String details,
                                double numericValue, double secondaryValue, int countValue,
                                String dateKey, int startMinutes, int endMinutes, String status,
                                String tags, String attachmentUri, boolean favorite,
                                boolean archived, long createdAt, long updatedAt) {
        this.module = module;
        this.feature = feature;
        this.title = title;
        this.details = details;
        this.numericValue = numericValue;
        this.secondaryValue = secondaryValue;
        this.countValue = countValue;
        this.dateKey = dateKey;
        this.startMinutes = startMinutes;
        this.endMinutes = endMinutes;
        this.status = status;
        this.tags = tags;
        this.attachmentUri = attachmentUri;
        this.favorite = favorite;
        this.archived = archived;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
