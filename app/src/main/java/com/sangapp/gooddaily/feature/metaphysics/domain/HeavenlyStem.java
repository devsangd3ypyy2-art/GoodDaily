package com.sangapp.gooddaily.feature.metaphysics.domain;

public enum HeavenlyStem {
    JIA("Giáp", FiveElement.WOOD),
    YI("Ất", FiveElement.WOOD),
    BING("Bính", FiveElement.FIRE),
    DING("Đinh", FiveElement.FIRE),
    WU("Mậu", FiveElement.EARTH),
    JI("Kỷ", FiveElement.EARTH),
    GENG("Canh", FiveElement.METAL),
    XIN("Tân", FiveElement.METAL),
    REN("Nhâm", FiveElement.WATER),
    GUI("Quý", FiveElement.WATER);

    public final String vietnamese;
    public final FiveElement element;

    HeavenlyStem(String vietnamese, FiveElement element) {
        this.vietnamese = vietnamese;
        this.element = element;
    }

    @Override public String toString() {
        return vietnamese;
    }
}
