package com.sangapp.gooddaily.feature;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FeaturePlacementCatalogTest {
    @Test public void everyCatalogFeatureHasAnOwnedSurface() {
        for (FeatureDefinition definition : FeatureCatalog.all()) {
            assertTrue(
                    "Feature is not exposed in a module: " + definition.feature,
                    FeaturePlacementCatalog.allPlacedFeatures().contains(definition.feature)
            );
        }
    }
}
