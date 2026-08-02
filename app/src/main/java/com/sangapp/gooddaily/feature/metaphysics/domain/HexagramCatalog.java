package com.sangapp.gooddaily.feature.metaphysics.domain;

import java.util.Arrays;

public final class HexagramCatalog {
    private static final Trigram[] ORDER = {
            Trigram.QIAN, Trigram.DUI, Trigram.LI, Trigram.ZHEN,
            Trigram.XUN, Trigram.KAN, Trigram.GEN, Trigram.KUN
    };

    // Hàng: hạ quái; cột: thượng quái. Thứ tự Càn, Đoài, Ly, Chấn, Tốn, Khảm, Cấn, Khôn.
    private static final int[][] KING_WEN = {
            {1, 43, 14, 34, 9, 5, 26, 11},
            {10, 58, 38, 54, 61, 60, 41, 19},
            {13, 49, 30, 55, 37, 63, 22, 36},
            {25, 17, 21, 51, 42, 3, 27, 24},
            {44, 28, 50, 32, 57, 48, 18, 46},
            {6, 47, 64, 40, 59, 29, 4, 7},
            {33, 31, 56, 62, 53, 39, 52, 15},
            {12, 45, 35, 16, 20, 8, 23, 2}
    };

    private static final String[] NAMES = {
            "", "Thuần Càn", "Thuần Khôn", "Thủy Lôi Truân", "Sơn Thủy Mông",
            "Thủy Thiên Nhu", "Thiên Thủy Tụng", "Địa Thủy Sư", "Thủy Địa Tỷ",
            "Phong Thiên Tiểu Súc", "Thiên Trạch Lý", "Địa Thiên Thái", "Thiên Địa Bĩ",
            "Thiên Hỏa Đồng Nhân", "Hỏa Thiên Đại Hữu", "Địa Sơn Khiêm", "Lôi Địa Dự",
            "Trạch Lôi Tùy", "Sơn Phong Cổ", "Địa Trạch Lâm", "Phong Địa Quan",
            "Hỏa Lôi Phệ Hạp", "Sơn Hỏa Bí", "Sơn Địa Bác", "Địa Lôi Phục",
            "Thiên Lôi Vô Vọng", "Sơn Thiên Đại Súc", "Sơn Lôi Di", "Trạch Phong Đại Quá",
            "Thuần Khảm", "Thuần Ly", "Trạch Sơn Hàm", "Lôi Phong Hằng",
            "Thiên Sơn Độn", "Lôi Thiên Đại Tráng", "Hỏa Địa Tấn", "Địa Hỏa Minh Di",
            "Phong Hỏa Gia Nhân", "Hỏa Trạch Khuê", "Thủy Sơn Kiển", "Lôi Thủy Giải",
            "Sơn Trạch Tổn", "Phong Lôi Ích", "Trạch Thiên Quải", "Thiên Phong Cấu",
            "Trạch Địa Tụy", "Địa Phong Thăng", "Trạch Thủy Khốn", "Thủy Phong Tỉnh",
            "Trạch Hỏa Cách", "Hỏa Phong Đỉnh", "Thuần Chấn", "Thuần Cấn",
            "Phong Sơn Tiệm", "Lôi Trạch Quy Muội", "Lôi Hỏa Phong", "Hỏa Sơn Lữ",
            "Thuần Tốn", "Thuần Đoài", "Phong Thủy Hoán", "Thủy Trạch Tiết",
            "Phong Trạch Trung Phu", "Lôi Sơn Tiểu Quá", "Thủy Hỏa Ký Tế", "Hỏa Thủy Vị Tế"
    };

    private HexagramCatalog() {}

    public static HexagramInfo fromTrigrams(Trigram upper, Trigram lower) {
        int row = indexOf(lower);
        int col = indexOf(upper);
        int number = KING_WEN[row][col];
        boolean[] lines = new boolean[6];
        System.arraycopy(lower.lines(), 0, lines, 0, 3);
        System.arraycopy(upper.lines(), 0, lines, 3, 3);
        return new HexagramInfo(number, NAMES[number], upper, lower, lines);
    }

    public static HexagramInfo fromLines(boolean[] linesBottomUp) {
        if (linesBottomUp == null || linesBottomUp.length != 6) throw new IllegalArgumentException("Cần đúng 6 hào");
        Trigram lower = Trigram.fromLines(Arrays.copyOfRange(linesBottomUp, 0, 3));
        Trigram upper = Trigram.fromLines(Arrays.copyOfRange(linesBottomUp, 3, 6));
        return fromTrigrams(upper, lower);
    }

    public static HexagramInfo nuclearOf(boolean[] originalLinesBottomUp) {
        if (originalLinesBottomUp == null || originalLinesBottomUp.length != 6) throw new IllegalArgumentException("Cần đúng 6 hào");
        boolean[] nuclear = {
                originalLinesBottomUp[1], originalLinesBottomUp[2], originalLinesBottomUp[3],
                originalLinesBottomUp[2], originalLinesBottomUp[3], originalLinesBottomUp[4]
        };
        return fromLines(nuclear);
    }

    public static String nameOf(int number) {
        if (number < 1 || number > 64) return "Không xác định";
        return NAMES[number];
    }

    private static int indexOf(Trigram trigram) {
        for (int i = 0; i < ORDER.length; i++) if (ORDER[i] == trigram) return i;
        throw new IllegalArgumentException("Quái không hợp lệ");
    }
}
