package com.sangapp.gooddaily.feature.driver.domain;

public final class MaintenanceDueCalculator {
    private MaintenanceDueCalculator() {}

    public static boolean isDue(long now, double currentOdometerKm, long nextDueAt, double nextDueOdometerKm) {
        boolean dueByDate = nextDueAt > 0 && now >= nextDueAt;
        boolean dueByKm = nextDueOdometerKm > 0 && currentOdometerKm >= nextDueOdometerKm;
        return dueByDate || dueByKm;
    }

    public static boolean isDueSoon(long now, double currentOdometerKm, long nextDueAt, double nextDueOdometerKm,
                                    long warningMillis, double warningKm) {
        boolean soonByDate = nextDueAt > 0 && nextDueAt - now <= warningMillis;
        boolean soonByKm = nextDueOdometerKm > 0 && nextDueOdometerKm - currentOdometerKm <= warningKm;
        return soonByDate || soonByKm;
    }
}
