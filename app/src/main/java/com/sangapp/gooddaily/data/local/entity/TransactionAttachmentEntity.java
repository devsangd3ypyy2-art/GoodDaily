package com.sangapp.gooddaily.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "transaction_attachments", indices = {@Index(value = {"transactionId"})})
public class TransactionAttachmentEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public long transactionId;
    public String uri;
    public String displayName;
    public long createdAt;

    public TransactionAttachmentEntity(long transactionId, String uri, String displayName, long createdAt) {
        this.transactionId = transactionId;
        this.uri = uri;
        this.displayName = displayName;
        this.createdAt = createdAt;
    }
}
