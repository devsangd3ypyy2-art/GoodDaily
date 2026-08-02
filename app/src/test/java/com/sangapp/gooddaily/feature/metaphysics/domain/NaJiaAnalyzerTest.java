package com.sangapp.gooddaily.feature.metaphysics.domain;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NaJiaAnalyzerTest {
    @Test public void pureQian_assignsZiToFirstLineAndDescendantRelative() {
        int[] values = {7,7,7,7,7,7};
        LiuHaoContext context = new LiuHaoContext(QuestionTopic.GENERAL, "",
                HeavenlyStem.JIA, EarthlyBranch.ZI, EarthlyBranch.YIN);
        HexagramInfo base = HexagramCatalog.fromTrigrams(Trigram.QIAN, Trigram.QIAN);
        LiuHaoAnalysis analysis = NaJiaAnalyzer.analyze(base, base, values, context);
        assertEquals(EarthlyBranch.ZI, analysis.lines[0].branch);
        assertEquals(SixRelative.DESCENDANTS, analysis.lines[0].relative);
        assertEquals(6, analysis.palace.shiLine);
    }

    @Test public void financeQuestion_selectsWealthWhenPresent() {
        int[] values = {7,8,9,7,6,8};
        LiuHaoContext context = new LiuHaoContext(QuestionTopic.FINANCE, "thu nhập",
                HeavenlyStem.JIA, EarthlyBranch.ZI, EarthlyBranch.SHEN);
        DivinationResult result = LiuHaoCalculator.calculate(values, context);
        assertTrue(result.interpretation.contains("Dụng thần"));
        assertTrue(result.technicalDetails.contains("NẠP GIÁP"));
    }
}
