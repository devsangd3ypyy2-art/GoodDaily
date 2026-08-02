package com.sangapp.gooddaily.feature.metaphysics.ui;

import com.sangapp.gooddaily.feature.metaphysics.domain.DivinationResult;

public final class DivinationUi {
    private DivinationUi() {}

    public static String renderLines(DivinationResult result) {
        StringBuilder builder = new StringBuilder();
        for (int index = 5; index >= 0; index--) {
            boolean yang = result.base.linesBottomUp[index];
            boolean moving = false;
            for (int line : result.movingLines) if (line == index + 1) moving = true;
            builder.append(index + 1).append("  ")
                    .append(yang ? "━━━━━━━━" : "━━━  ━━━")
                    .append(moving ? "  ×" : "")
                    .append('\n');
        }
        return builder.toString().trim();
    }

    public static String csv(int[] values) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) b.append(',');
            b.append(values[i]);
        }
        return b.toString();
    }
}
