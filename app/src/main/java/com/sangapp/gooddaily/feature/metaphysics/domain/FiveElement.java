package com.sangapp.gooddaily.feature.metaphysics.domain;

public enum FiveElement {
    WOOD("Mộc"), FIRE("Hỏa"), EARTH("Thổ"), METAL("Kim"), WATER("Thủy");

    public final String vietnamese;

    FiveElement(String vietnamese) {
        this.vietnamese = vietnamese;
    }

    public boolean generates(FiveElement other) {
        return (this == WOOD && other == FIRE)
                || (this == FIRE && other == EARTH)
                || (this == EARTH && other == METAL)
                || (this == METAL && other == WATER)
                || (this == WATER && other == WOOD);
    }

    public boolean controls(FiveElement other) {
        return (this == WOOD && other == EARTH)
                || (this == EARTH && other == WATER)
                || (this == WATER && other == FIRE)
                || (this == FIRE && other == METAL)
                || (this == METAL && other == WOOD);
    }
}
