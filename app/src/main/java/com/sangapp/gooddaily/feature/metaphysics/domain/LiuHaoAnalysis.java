package com.sangapp.gooddaily.feature.metaphysics.domain;

public final class LiuHaoAnalysis {
    public final EightPalaceResult palace;
    public final NaJiaLine[] lines;
    public final NaJiaLine targetLine;
    public final String targetReason;
    public final String judgment;
    public final String technicalDetails;
    public final String timing;
    public final String confidence;

    LiuHaoAnalysis(EightPalaceResult palace, NaJiaLine[] lines, NaJiaLine targetLine,
                   String targetReason, String judgment, String technicalDetails,
                   String timing, String confidence) {
        this.palace = palace;
        this.lines = lines;
        this.targetLine = targetLine;
        this.targetReason = targetReason;
        this.judgment = judgment;
        this.technicalDetails = technicalDetails;
        this.timing = timing;
        this.confidence = confidence;
    }
}
