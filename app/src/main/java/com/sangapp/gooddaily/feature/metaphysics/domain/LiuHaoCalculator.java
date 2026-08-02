package com.sangapp.gooddaily.feature.metaphysics.domain;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public final class LiuHaoCalculator {
    private static final SecureRandom RANDOM = new SecureRandom();

    private LiuHaoCalculator() {}

    public static int castThreeCoins() {
        int sum = 0;
        for (int i = 0; i < 3; i++) sum += RANDOM.nextBoolean() ? 3 : 2;
        return sum;
    }

    public static int[] castSixLines() {
        int[] values = new int[6];
        for (int i = 0; i < values.length; i++) values[i] = castThreeCoins();
        return values;
    }

    public static DivinationResult calculate(int[] valuesBottomUp) {
        GanzhiDate ganzhi = GanzhiDate.approximate(Calendar.getInstance());
        LiuHaoContext context = new LiuHaoContext(QuestionTopic.GENERAL, "",
                ganzhi.dayStem, ganzhi.dayBranch, ganzhi.monthBranch);
        return calculate(valuesBottomUp, context);
    }

    public static DivinationResult calculate(int[] valuesBottomUp, LiuHaoContext context) {
        if (valuesBottomUp == null || valuesBottomUp.length != 6) throw new IllegalArgumentException("Cần đúng 6 lần gieo từ hào 1 đến hào 6");
        boolean[] baseLines = new boolean[6];
        boolean[] changedLines = new boolean[6];
        List<Integer> moving = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            int value = valuesBottomUp[i];
            if (value < 6 || value > 9) throw new IllegalArgumentException("Giá trị hào chỉ có thể là 6, 7, 8 hoặc 9");
            boolean yang = value == 7 || value == 9;
            boolean isMoving = value == 6 || value == 9;
            baseLines[i] = yang;
            changedLines[i] = isMoving ? !yang : yang;
            if (isMoving) moving.add(i + 1);
        }
        int[] movingArray = new int[moving.size()];
        for (int i = 0; i < moving.size(); i++) movingArray[i] = moving.get(i);
        HexagramInfo base = HexagramCatalog.fromLines(baseLines);
        HexagramInfo changed = HexagramCatalog.fromLines(changedLines);
        HexagramInfo nuclear = HexagramCatalog.nuclearOf(baseLines);
        String relation = InterpretationEngine.relationText(base.lower.element, base.upper.element);
        String bodyUse = "Nội quái: " + base.lower.label() + "\nNgoại quái: " + base.upper.label();
        LiuHaoAnalysis analysis = NaJiaAnalyzer.analyze(base, changed, valuesBottomUp, context);
        return new DivinationResult(base, changed, nuclear, movingArray, valuesBottomUp,
                bodyUse, relation, analysis.judgment, analysis.technicalDetails,
                analysis.timing, analysis.confidence);
    }

    public static String lineLabel(int value) {
        switch (value) {
            case 6: return "Lão Âm ⚋ động";
            case 7: return "Thiếu Dương ⚊";
            case 8: return "Thiếu Âm ⚋";
            case 9: return "Lão Dương ⚊ động";
            default: return "Không hợp lệ";
        }
    }
}
