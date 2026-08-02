package com.sangapp.gooddaily.feature.metaphysics.domain;

public final class MaiHoaAnalysis {
    public final String judgment;
    public final String technicalDetails;
    public final String timing;
    public final String confidence;

    MaiHoaAnalysis(String judgment, String technicalDetails, String timing, String confidence) {
        this.judgment = judgment;
        this.technicalDetails = technicalDetails;
        this.timing = timing;
        this.confidence = confidence;
    }
}
