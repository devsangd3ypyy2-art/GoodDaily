package com.sangapp.gooddaily.feature.driver.domain;

public final class BatteryHealthCalculator {
    private BatteryHealthCalculator() {}

    public static double efficiencyKmPerKwh(double distanceKm, double energyKwh) {
        return energyKwh <= 0 ? 0 : distanceKm / energyKwh;
    }

    public static double estimatedHealthPercent(double nominalCapacityKwh, double measuredUsableCapacityKwh) {
        if (nominalCapacityKwh <= 0 || measuredUsableCapacityKwh <= 0) return 0;
        return Math.max(0, Math.min(100, measuredUsableCapacityKwh / nominalCapacityKwh * 100));
    }

    public static int estimateEquivalentCycles(int startPercent, int endPercent, int currentAccumulator) {
        int delta = Math.max(0, endPercent - startPercent);
        return currentAccumulator + delta;
    }
}
