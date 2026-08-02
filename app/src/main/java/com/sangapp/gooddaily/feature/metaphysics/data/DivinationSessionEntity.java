package com.sangapp.gooddaily.feature.metaphysics.data;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "divination_sessions", indices = {
        @Index("system"), @Index("castTime"), @Index("status")
})
public class DivinationSessionEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String system;
    public String method;
    public String question;
    public long castTime;
    public String lunarText;
    public String inputData;
    public int baseHexagramNumber;
    public String baseHexagramName;
    public int changedHexagramNumber;
    public String changedHexagramName;
    public int nuclearHexagramNumber;
    public String nuclearHexagramName;
    public String movingLines;
    public String bodyUse;
    public String elementRelation;
    public String interpretation;
    public String verification;
    public long verifiedAt;
    public String status;
    public boolean favorite;
    public long createdAt;
    public long updatedAt;
}
