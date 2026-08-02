package com.sangapp.gooddaily.feature.metaphysics.domain;

public enum EarthlyBranch {
    ZI("Tý", FiveElement.WATER),
    CHOU("Sửu", FiveElement.EARTH),
    YIN("Dần", FiveElement.WOOD),
    MAO("Mão", FiveElement.WOOD),
    CHEN("Thìn", FiveElement.EARTH),
    SI("Tỵ", FiveElement.FIRE),
    WU("Ngọ", FiveElement.FIRE),
    WEI("Mùi", FiveElement.EARTH),
    SHEN("Thân", FiveElement.METAL),
    YOU("Dậu", FiveElement.METAL),
    XU("Tuất", FiveElement.EARTH),
    HAI("Hợi", FiveElement.WATER);

    public final String vietnamese;
    public final FiveElement element;

    EarthlyBranch(String vietnamese, FiveElement element) {
        this.vietnamese = vietnamese;
        this.element = element;
    }

    public EarthlyBranch opposite() {
        return values()[(ordinal() + 6) % 12];
    }

    public EarthlyBranch combinePartner() {
        switch (this) {
            case ZI: return CHOU;
            case CHOU: return ZI;
            case YIN: return HAI;
            case HAI: return YIN;
            case MAO: return XU;
            case XU: return MAO;
            case CHEN: return YOU;
            case YOU: return CHEN;
            case SI: return SHEN;
            case SHEN: return SI;
            case WU: return WEI;
            case WEI: return WU;
            default: return ZI;
        }
    }

    public boolean clashes(EarthlyBranch other) {
        return other != null && opposite() == other;
    }

    public boolean combines(EarthlyBranch other) {
        return other != null && combinePartner() == other;
    }

    @Override public String toString() {
        return vietnamese;
    }
}
