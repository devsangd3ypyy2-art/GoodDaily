package com.sangapp.gooddaily.feature.metaphysics.domain;

import java.util.Calendar;

public final class MaiHoaCalculator {
    private MaiHoaCalculator() {}

    public static DivinationResult fromNumbers(long upperNumber, long lowerNumber, long movingSeed) {
        return fromNumbers(upperNumber, lowerNumber, movingSeed,
                QuestionTopic.GENERAL, "", Calendar.getInstance());
    }

    public static DivinationResult fromNumbers(long upperNumber, long lowerNumber, long movingSeed,
                                                QuestionTopic topic, String question,
                                                Calendar castTime) {
        Trigram upper = Trigram.fromMaiHoaNumber(upperNumber);
        Trigram lower = Trigram.fromMaiHoaNumber(lowerNumber);
        int movingLine = (int) Math.floorMod(movingSeed - 1L, 6L) + 1;
        return calculate(upper, lower, movingLine, topic, question, castTime);
    }

    public static DivinationResult fromDateTime(Calendar calendar) {
        return fromDateTime(calendar, QuestionTopic.GENERAL, "");
    }

    public static DivinationResult fromDateTime(Calendar calendar, QuestionTopic topic, String question) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hourBranch = ((calendar.get(Calendar.HOUR_OF_DAY) + 1) / 2) % 12 + 1;
        long upperSeed = year + month + day;
        long lowerSeed = upperSeed + hourBranch;
        long movingSeed = lowerSeed;
        return fromNumbers(upperSeed, lowerSeed, movingSeed, topic, question, calendar);
    }

    private static DivinationResult calculate(Trigram upper, Trigram lower, int movingLine,
                                               QuestionTopic topic, String question,
                                               Calendar castTime) {
        HexagramInfo base = HexagramCatalog.fromTrigrams(upper, lower);
        boolean[] changedLines = base.linesBottomUp.clone();
        changedLines[movingLine - 1] = !changedLines[movingLine - 1];
        HexagramInfo changed = HexagramCatalog.fromLines(changedLines);
        HexagramInfo nuclear = HexagramCatalog.nuclearOf(base.linesBottomUp);

        Trigram body = movingLine <= 3 ? upper : lower;
        Trigram use = movingLine <= 3 ? lower : upper;
        String bodyUse = "Thể: " + body.label() + "\nDụng: " + use.label();
        String relation = InterpretationEngine.relationText(body.element, use.element);
        MaiHoaAnalysis analysis = AdvancedMaiHoaInterpreter.analyze(base, nuclear, changed,
                body, use, movingLine, topic, question, castTime);
        return new DivinationResult(base, changed, nuclear, new int[]{movingLine}, new int[0],
                bodyUse, relation, analysis.judgment, analysis.technicalDetails,
                analysis.timing, analysis.confidence);
    }
}
