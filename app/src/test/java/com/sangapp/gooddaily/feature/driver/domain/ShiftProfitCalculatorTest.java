package com.sangapp.gooddaily.feature.driver.domain;

import com.sangapp.gooddaily.feature.driver.data.entity.DriverShiftEntity;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ShiftProfitCalculatorTest {
    @Test public void netProfit_subtractsAllCosts() {
        DriverShiftEntity shift = new DriverShiftEntity();
        shift.revenue = 500_000;
        shift.bonus = 50_000;
        shift.tips = 20_000;
        shift.platformFee = 70_000;
        shift.energyCost = 30_000;
        shift.mealCost = 25_000;
        shift.otherCost = 15_000;
        shift.depreciationCost = 10_000;
        assertEquals(420_000, ShiftProfitCalculator.netProfit(shift), 0.001);
    }

    @Test public void profitPerHour_usesShiftDuration() {
        DriverShiftEntity shift = new DriverShiftEntity();
        shift.revenue = 300_000;
        shift.startTime = 0;
        shift.endTime = 3 * 3_600_000L;
        assertEquals(100_000, ShiftProfitCalculator.profitPerHour(shift), 0.001);
    }
}
