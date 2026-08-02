package com.sangapp.gooddaily.feature.metaphysics.domain;

public final class NaJiaLine {
    public final int position;
    public final boolean yang;
    public final boolean moving;
    public final HeavenlyStem stem;
    public final EarthlyBranch branch;
    public final FiveElement element;
    public final SixRelative relative;
    public final SixSpirit spirit;
    public final boolean shi;
    public final boolean ying;
    public final EarthlyBranch changedBranch;
    public final FiveElement changedElement;
    public final int strengthScore;
    public final boolean voidBranch;
    public final boolean monthBroken;
    public final String strengthLabel;
    public final String notes;

    NaJiaLine(int position, boolean yang, boolean moving,
              HeavenlyStem stem, EarthlyBranch branch, SixRelative relative,
              SixSpirit spirit, boolean shi, boolean ying,
              EarthlyBranch changedBranch, int strengthScore,
              boolean voidBranch, boolean monthBroken,
              String strengthLabel, String notes) {
        this.position = position;
        this.yang = yang;
        this.moving = moving;
        this.stem = stem;
        this.branch = branch;
        this.element = branch.element;
        this.relative = relative;
        this.spirit = spirit;
        this.shi = shi;
        this.ying = ying;
        this.changedBranch = changedBranch;
        this.changedElement = changedBranch == null ? null : changedBranch.element;
        this.strengthScore = strengthScore;
        this.voidBranch = voidBranch;
        this.monthBroken = monthBroken;
        this.strengthLabel = strengthLabel;
        this.notes = notes;
    }

    public String compact() {
        StringBuilder b = new StringBuilder();
        b.append("Hào ").append(position).append(" · ").append(spirit)
                .append(" · ").append(relative)
                .append(" · ").append(stem).append(branch)
                .append(" ").append(element.vietnamese);
        if (shi) b.append(" · THẾ");
        if (ying) b.append(" · ỨNG");
        if (moving) b.append(" · ĐỘNG → ").append(changedBranch).append(" ").append(changedElement.vietnamese);
        b.append(" · ").append(strengthLabel);
        if (!notes.isEmpty()) b.append("\n  ").append(notes);
        return b.toString();
    }
}
