package com.sangapp.gooddaily.feature.metaphysics.domain;

import java.util.Arrays;

public final class HexagramInfo {
    public final int number;
    public final String name;
    public final Trigram upper;
    public final Trigram lower;
    public final boolean[] linesBottomUp;

    public HexagramInfo(int number, String name, Trigram upper, Trigram lower, boolean[] linesBottomUp) {
        this.number = number;
        this.name = name;
        this.upper = upper;
        this.lower = lower;
        this.linesBottomUp = Arrays.copyOf(linesBottomUp, linesBottomUp.length);
    }

    public String title() {
        return "Quẻ " + number + " · " + name;
    }

    public String symbols() {
        return upper.symbol + " trên " + lower.symbol;
    }
}
