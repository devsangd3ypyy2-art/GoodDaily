package com.sangapp.gooddaily.feature.metaphysics.domain;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EightPalaceAnalyzerTest {
    @Test public void pureQian_isQianPalaceWithShiAtSix() {
        EightPalaceResult result = EightPalaceAnalyzer.analyze(
                HexagramCatalog.fromTrigrams(Trigram.QIAN, Trigram.QIAN));
        assertEquals(Trigram.QIAN, result.palace);
        assertEquals(6, result.shiLine);
        assertEquals(3, result.yingLine);
    }

    @Test public void qianFirstGeneration_hasShiAtOne() {
        HexagramInfo gou = HexagramCatalog.fromLines(new boolean[]{false, true, true, true, true, true});
        EightPalaceResult result = EightPalaceAnalyzer.analyze(gou);
        assertEquals(Trigram.QIAN, result.palace);
        assertEquals(1, result.shiLine);
        assertEquals("Nhất thế", result.generationName);
    }
}
