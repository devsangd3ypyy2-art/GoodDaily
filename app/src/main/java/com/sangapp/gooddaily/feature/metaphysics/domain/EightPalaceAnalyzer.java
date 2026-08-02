package com.sangapp.gooddaily.feature.metaphysics.domain;

import java.util.Arrays;

public final class EightPalaceAnalyzer {
    private static final String[] NAMES = {
            "Bản cung", "Nhất thế", "Nhị thế", "Tam thế",
            "Tứ thế", "Ngũ thế", "Du hồn", "Quy hồn"
    };
    private static final int[] SHI = {6, 1, 2, 3, 4, 5, 4, 3};
    private static final int[] MASKS = {
            0b000000,
            0b000001,
            0b000011,
            0b000111,
            0b001111,
            0b011111,
            0b010111,
            0b010000
    };

    private EightPalaceAnalyzer() {}

    public static EightPalaceResult analyze(HexagramInfo hexagram) {
        for (Trigram palace : Trigram.values()) {
            boolean[] pure = new boolean[6];
            System.arraycopy(palace.lines(), 0, pure, 0, 3);
            System.arraycopy(palace.lines(), 0, pure, 3, 3);
            for (int stage = 0; stage < MASKS.length; stage++) {
                boolean[] candidate = applyMask(pure, MASKS[stage]);
                if (Arrays.equals(candidate, hexagram.linesBottomUp)) {
                    return new EightPalaceResult(palace, stage, NAMES[stage], SHI[stage]);
                }
            }
        }
        throw new IllegalStateException("Không xác định được Bát cung cho " + hexagram.title());
    }

    private static boolean[] applyMask(boolean[] source, int mask) {
        boolean[] result = Arrays.copyOf(source, source.length);
        for (int i = 0; i < 6; i++) if ((mask & (1 << i)) != 0) result[i] = !result[i];
        return result;
    }
}
