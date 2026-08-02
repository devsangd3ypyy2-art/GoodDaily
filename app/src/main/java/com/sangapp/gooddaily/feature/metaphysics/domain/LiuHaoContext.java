package com.sangapp.gooddaily.feature.metaphysics.domain;

public final class LiuHaoContext {
    public final QuestionTopic topic;
    public final String question;
    public final HeavenlyStem dayStem;
    public final EarthlyBranch dayBranch;
    public final EarthlyBranch monthBranch;

    public LiuHaoContext(QuestionTopic topic, String question,
                         HeavenlyStem dayStem, EarthlyBranch dayBranch,
                         EarthlyBranch monthBranch) {
        this.topic = topic == null ? QuestionTopic.GENERAL : topic;
        this.question = question == null ? "" : question.trim();
        this.dayStem = dayStem == null ? HeavenlyStem.JIA : dayStem;
        this.dayBranch = dayBranch == null ? EarthlyBranch.ZI : dayBranch;
        this.monthBranch = monthBranch == null ? EarthlyBranch.ZI : monthBranch;
    }
}
