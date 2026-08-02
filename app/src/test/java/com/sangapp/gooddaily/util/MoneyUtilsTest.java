package com.sangapp.gooddaily.util;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MoneyUtilsTest {
    @Test public void format_addsVietnameseCurrencySymbol() {
        assertTrue(MoneyUtils.format(125000).contains("₫"));
    }
}
