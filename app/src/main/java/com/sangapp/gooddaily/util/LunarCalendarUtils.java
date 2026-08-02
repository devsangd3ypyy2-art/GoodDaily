package com.sangapp.gooddaily.util;

import android.icu.util.ChineseCalendar;

import java.util.Date;
import java.util.Locale;

public final class LunarCalendarUtils {
    private LunarCalendarUtils() {}

    public static String formatLunar(String dateKey) {
        try {
            ChineseCalendar lunar = new ChineseCalendar();
            lunar.setTime(new Date(DateUtils.parseDateKey(dateKey)));
            int day = lunar.get(ChineseCalendar.DAY_OF_MONTH);
            int month = lunar.get(ChineseCalendar.MONTH) + 1;
            return String.format(Locale.getDefault(), "%02d/%02d âm", day, month);
        } catch (Exception e) {
            return "--/-- âm";
        }
    }
}
