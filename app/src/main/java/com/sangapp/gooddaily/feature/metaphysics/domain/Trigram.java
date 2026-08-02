package com.sangapp.gooddaily.feature.metaphysics.domain;

import java.util.Arrays;

public enum Trigram {
    QIAN(1, "Càn", "☰", "Trời", FiveElement.METAL, new boolean[]{true, true, true}),
    DUI(2, "Đoài", "☱", "Đầm", FiveElement.METAL, new boolean[]{true, true, false}),
    LI(3, "Ly", "☲", "Lửa", FiveElement.FIRE, new boolean[]{true, false, true}),
    ZHEN(4, "Chấn", "☳", "Sấm", FiveElement.WOOD, new boolean[]{true, false, false}),
    XUN(5, "Tốn", "☴", "Gió", FiveElement.WOOD, new boolean[]{false, true, true}),
    KAN(6, "Khảm", "☵", "Nước", FiveElement.WATER, new boolean[]{false, true, false}),
    GEN(7, "Cấn", "☶", "Núi", FiveElement.EARTH, new boolean[]{false, false, true}),
    KUN(8, "Khôn", "☷", "Đất", FiveElement.EARTH, new boolean[]{false, false, false});

    public final int number;
    public final String vietnamese;
    public final String symbol;
    public final String image;
    public final FiveElement element;
    private final boolean[] linesBottomUp;

    Trigram(int number, String vietnamese, String symbol, String image, FiveElement element, boolean[] linesBottomUp) {
        this.number = number;
        this.vietnamese = vietnamese;
        this.symbol = symbol;
        this.image = image;
        this.element = element;
        this.linesBottomUp = linesBottomUp;
    }

    public boolean[] lines() {
        return Arrays.copyOf(linesBottomUp, linesBottomUp.length);
    }

    public String label() {
        return symbol + " " + vietnamese + " · " + image + " · " + element.vietnamese;
    }

    public static Trigram fromMaiHoaNumber(long value) {
        int normalized = (int) Math.floorMod(value - 1L, 8L) + 1;
        for (Trigram trigram : values()) if (trigram.number == normalized) return trigram;
        return KUN;
    }

    public static Trigram fromLines(boolean[] linesBottomUp) {
        if (linesBottomUp == null || linesBottomUp.length != 3) throw new IllegalArgumentException("Cần đúng 3 hào");
        for (Trigram trigram : values()) {
            if (Arrays.equals(trigram.linesBottomUp, linesBottomUp)) return trigram;
        }
        throw new IllegalArgumentException("Không xác định được quái");
    }
}
