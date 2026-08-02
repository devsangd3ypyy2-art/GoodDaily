package com.sangapp.gooddaily.feature.metaphysics.domain;

public enum SixSpirit {
    QING_LONG("Thanh Long"),
    ZHU_QUE("Chu Tước"),
    GOU_CHEN("Câu Trần"),
    TENG_SHE("Đằng Xà"),
    BAI_HU("Bạch Hổ"),
    XUAN_WU("Huyền Vũ");

    public final String vietnamese;

    SixSpirit(String vietnamese) {
        this.vietnamese = vietnamese;
    }

    @Override public String toString() {
        return vietnamese;
    }
}
