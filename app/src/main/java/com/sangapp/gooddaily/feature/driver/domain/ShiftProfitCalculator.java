package com.sangapp.gooddaily.feature.driver.domain;

import com.sangapp.gooddaily.feature.driver.data.entity.DriverShiftEntity;

public final class ShiftProfitCalculator {
    private ShiftProfitCalculator() {}

    public static double grossIncome(DriverShiftEntity shift) {
        if (shift == null) return 0;
        return shift.revenue + shift.bonus + shift.tips;
    }

    public static double totalCost(DriverShiftEntity shift) {
        if (shift == null) return 0;
        return shift.platformFee + shift.energyCost + shift.mealCost + shift.otherCost + shift.depreciationCost;
    }

    public static double netProfit(DriverShiftEntity shift) {
        return grossIncome(shift) - totalCost(shift);
    }

    public static double durationHours(DriverShiftEntity shift) {
        if (shift == null || shift.endTime <= shift.startTime) return 0;
        return (shift.endTime - shift.startTime) / 3_600_000d;
    }

    public static double profitPerHour(DriverShiftEntity shift) {
        double hours = durationHours(shift);
        return hours <= 0 ? 0 : netProfit(shift) / hours;
    }

    public static double profitPerKm(DriverShiftEntity shift) {
        return shift == null || shift.distanceKm <= 0 ? 0 : netProfit(shift) / shift.distanceKm;
    }
}
