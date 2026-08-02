package com.sangapp.gooddaily.feature;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class FeatureCatalogTest {
    @Test public void keyModulesAreRegistered() {
        assertNotNull(FeatureCatalog.get(FeatureCatalog.FINANCE_SAVING));
        assertNotNull(FeatureCatalog.get(FeatureCatalog.HEALTH_SLEEP));
        assertNotNull(FeatureCatalog.get(FeatureCatalog.DRIVER_SHIFT));
        assertNotNull(FeatureCatalog.get(FeatureCatalog.ADVANCED_REMINDER));
        assertFalse(FeatureCatalog.all().isEmpty());
    }
}
