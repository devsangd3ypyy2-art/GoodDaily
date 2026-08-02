package com.sangapp.gooddaily.feature.metaphysics.domain;

public enum SixRelative {
    PARENTS("Phụ Mẫu"),
    SIBLINGS("Huynh Đệ"),
    DESCENDANTS("Tử Tôn"),
    WEALTH("Thê Tài"),
    OFFICIAL_GHOST("Quan Quỷ");

    public final String vietnamese;

    SixRelative(String vietnamese) {
        this.vietnamese = vietnamese;
    }

    @Override public String toString() {
        return vietnamese;
    }
}
