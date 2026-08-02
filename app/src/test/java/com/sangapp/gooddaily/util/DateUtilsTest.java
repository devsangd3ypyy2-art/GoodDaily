package com.sangapp.gooddaily.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DateUtilsTest {
    @Test public void shiftDateKey_movesAcrossMonthBoundary() {
        assertEquals("2026-03-01", DateUtils.shiftDateKey("2026-02-28", 1));
    }

    @Test public void dayRange_isOrdered() {
        long now = System.currentTimeMillis();
        assertTrue(DateUtils.startOfDay(now) <= now);
        assertTrue(DateUtils.endOfDay(now) >= now);
    }

    @Test public void pickerRoundTrip_keepsCalendarDate() {
        String key = "2026-08-02";
        assertEquals(key, DateUtils.dateKeyFromUtcPicker(DateUtils.toUtcPickerMillis(key)));
    }
}
