package com.sangapp.gooddaily.feature.metaphysics.domain;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LiuHaoCalculatorTest {
    @Test public void sixYoungYang_createsPureQianWithoutMovingLines() {
        DivinationResult result = LiuHaoCalculator.calculate(new int[]{7,7,7,7,7,7});
        assertEquals(1, result.base.number);
        assertEquals(1, result.changed.number);
        assertEquals(0, result.movingLines.length);
    }

    @Test public void oldLines_areFlippedInChangedHexagram() {
        DivinationResult result = LiuHaoCalculator.calculate(new int[]{9,7,7,7,7,7});
        assertEquals(1, result.base.number);
        assertEquals(44, result.changed.number);
        assertEquals(1, result.movingLines[0]);
        assertTrue(result.interpretation.contains("hào động"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsInvalidLineValue() {
        LiuHaoCalculator.calculate(new int[]{5,7,7,7,7,7});
    }
}
