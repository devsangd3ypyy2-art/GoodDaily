package com.sangapp.gooddaily.feature.driver.domain;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BatteryHealthCalculatorTest {
    @Test public void healthPercent_isClamped() {
        assertEquals(80, BatteryHealthCalculator.estimatedHealthPercent(10, 8), 0.001);
        assertEquals(100, BatteryHealthCalculator.estimatedHealthPercent(10, 12), 0.001);
    }

    @Test public void efficiency_handlesZeroEnergy() {
        assertEquals(0, BatteryHealthCalculator.efficiencyKmPerKwh(50, 0), 0.001);
    }
}
