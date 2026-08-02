package com.sangapp.gooddaily.feature.metaphysics.domain;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MaiHoaCalculatorTest {
    @Test public void fromNumbers_mapsToValidHexagramsAndMovingLine() {
        DivinationResult result = MaiHoaCalculator.fromNumbers(1, 8, 7);
        assertEquals(12, result.base.number);
        assertEquals(1, result.movingLines[0]);
        assertNotNull(result.changed);
        assertNotNull(result.nuclear);
    }

    @Test public void trigramNumbers_wrapAtEight() {
        assertEquals(Trigram.QIAN, Trigram.fromMaiHoaNumber(9));
        assertEquals(Trigram.KUN, Trigram.fromMaiHoaNumber(8));
    }
}
