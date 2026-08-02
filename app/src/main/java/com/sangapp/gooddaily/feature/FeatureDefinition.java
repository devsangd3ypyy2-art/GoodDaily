package com.sangapp.gooddaily.feature;

public class FeatureDefinition {
    public final String module;
    public final String feature;
    public final String title;
    public final String description;
    public final String titleHint;
    public final String detailsHint;
    public final String valueHint;
    public final String secondaryHint;
    public final String countHint;
    public final String statusHint;
    public final String valueSuffix;
    public final boolean showTime;
    public final boolean allowAttachment;

    public FeatureDefinition(String module, String feature, String title, String description,
                             String titleHint, String detailsHint, String valueHint,
                             String secondaryHint, String countHint, String statusHint,
                             String valueSuffix, boolean showTime, boolean allowAttachment) {
        this.module = module;
        this.feature = feature;
        this.title = title;
        this.description = description;
        this.titleHint = titleHint;
        this.detailsHint = detailsHint;
        this.valueHint = valueHint;
        this.secondaryHint = secondaryHint;
        this.countHint = countHint;
        this.statusHint = statusHint;
        this.valueSuffix = valueSuffix;
        this.showTime = showTime;
        this.allowAttachment = allowAttachment;
    }
}
