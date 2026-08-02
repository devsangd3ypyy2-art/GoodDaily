package com.sangapp.gooddaily.feature;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Documents where every advanced feature is surfaced in the main UI.
 * This is intentionally pure Java so coverage can be verified by unit tests.
 */
public final class FeaturePlacementCatalog {
    public static final String SURFACE_FINANCE = "finance";
    public static final String SURFACE_HEALTH = "health";
    public static final String SURFACE_PLANNER = "planner";
    public static final String SURFACE_PROFILE = "profile";
    public static final String SURFACE_SPECIALIZED = "specialized";

    private FeaturePlacementCatalog() {}

    public static Set<String> finance() {
        return setOf(
                FeatureCatalog.FINANCE_RECURRING,
                FeatureCatalog.FINANCE_BUDGET,
                FeatureCatalog.FINANCE_SAVING,
                FeatureCatalog.FINANCE_DEBT,
                FeatureCatalog.FINANCE_RECEIPT
        );
    }

    public static Set<String> health() {
        return setOf(
                FeatureCatalog.HEALTH_FOOD,
                FeatureCatalog.HEALTH_WATER,
                FeatureCatalog.HEALTH_SLEEP,
                FeatureCatalog.HEALTH_MOOD,
                FeatureCatalog.HEALTH_WORKOUT,
                FeatureCatalog.HEALTH_MEDICATION,
                FeatureCatalog.HEALTH_MEASUREMENT
        );
    }

    public static Set<String> planner() {
        return setOf(
                FeatureCatalog.PLAN_EVENT,
                FeatureCatalog.PLAN_TEMPLATE,
                FeatureCatalog.PLAN_TASK,
                FeatureCatalog.LEARNING_SUBJECT,
                FeatureCatalog.LEARNING_GOAL,
                FeatureCatalog.LEARNING_POMODORO,
                FeatureCatalog.HABIT_PLAN,
                FeatureCatalog.PERSONAL_GOAL,
                FeatureCatalog.JOURNAL_ENTRY
        );
    }

    public static Set<String> profile() {
        return setOf(FeatureCatalog.ADVANCED_REMINDER);
    }

    public static Set<String> specialized() {
        return setOf(
                FeatureCatalog.DIVINATION_ENTRY,
                FeatureCatalog.DRIVER_SHIFT,
                FeatureCatalog.DRIVER_VEHICLE,
                FeatureCatalog.DRIVER_FUEL,
                FeatureCatalog.DRIVER_MAINTENANCE
        );
    }

    public static Set<String> allPlacedFeatures() {
        LinkedHashSet<String> values = new LinkedHashSet<>();
        values.addAll(finance());
        values.addAll(health());
        values.addAll(planner());
        values.addAll(profile());
        values.addAll(specialized());
        return Collections.unmodifiableSet(values);
    }

    private static Set<String> setOf(String... values) {
        return Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(values)));
    }
}
