package com.sangapp.gooddaily.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class DateUtils {
    private static final Locale VI = new Locale("vi", "VN");
    private static final String KEY_PATTERN = "yyyy-MM-dd";

    private DateUtils() {}

    public static long startOfDay() { return startOfDay(System.currentTimeMillis()); }

    public static long startOfDay(long time) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    public static long endOfDay() { return endOfDay(System.currentTimeMillis()); }

    public static long endOfDay(long time) { return startOfDay(time) + 86_400_000L - 1; }

    public static long startOfWeek() { return startOfWeek(System.currentTimeMillis()); }

    public static long startOfMonth() { return startOfMonth(System.currentTimeMillis()); }

    public static long startOfYear() { return startOfYear(System.currentTimeMillis()); }


    public static long startOfWeek(long time) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        int day = c.get(Calendar.DAY_OF_WEEK);
        int delta = day == Calendar.SUNDAY ? -6 : Calendar.MONDAY - day;
        c.add(Calendar.DAY_OF_YEAR, delta);
        return startOfDay(c.getTimeInMillis());
    }

    public static String startOfWeekKey(String dateKey) {
        return dateKey(startOfWeek(parseDateKey(dateKey)));
    }

    public static String endOfWeekKey(String dateKey) {
        return shiftDateKey(startOfWeekKey(dateKey), 6);
    }

    public static long startOfMonth(long time) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        c.set(Calendar.DAY_OF_MONTH, 1);
        return startOfDay(c.getTimeInMillis());
    }

    public static long endOfMonth(long time) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(startOfMonth(time));
        c.add(Calendar.MONTH, 1);
        return c.getTimeInMillis() - 1;
    }

    public static long startOfYear(long time) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        c.set(Calendar.MONTH, Calendar.JANUARY);
        c.set(Calendar.DAY_OF_MONTH, 1);
        return startOfDay(c.getTimeInMillis());
    }

    public static long endOfYear(long time) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(startOfYear(time));
        c.add(Calendar.YEAR, 1);
        return c.getTimeInMillis() - 1;
    }

    public static String formatTimeFromMinutes(int minutes) {
        int safe = Math.max(0, Math.min(minutes, 24 * 60 - 1));
        return String.format(Locale.getDefault(), "%02d:%02d", safe / 60, safe % 60);
    }

    public static String formatShortWeekday(String key) {
        return new SimpleDateFormat("EEE", VI).format(new Date(parseDateKey(key)));
    }

    public static String formatDayOfMonth(String key) {
        return new SimpleDateFormat("dd", VI).format(new Date(parseDateKey(key)));
    }

    public static String formatMonthYear(String key) {
        return new SimpleDateFormat("'Tháng' MM yyyy", VI).format(new Date(parseDateKey(key)));
    }

    public static long startOfDaysAgo(int daysAgo) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, -daysAgo);
        return startOfDay(c.getTimeInMillis());
    }

    public static String dateKey() { return dateKey(System.currentTimeMillis()); }

    public static String dateKey(long time) {
        return new SimpleDateFormat(KEY_PATTERN, Locale.US).format(new Date(time));
    }

    public static long parseDateKey(String key) {
        if (key == null || key.trim().isEmpty()) return startOfDay();
        try {
            Date date = new SimpleDateFormat(KEY_PATTERN, Locale.US).parse(key);
            return date == null ? startOfDay() : startOfDay(date.getTime());
        } catch (ParseException e) {
            return startOfDay();
        }
    }

    public static String shiftDateKey(String key, int days) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(parseDateKey(key));
        c.add(Calendar.DAY_OF_YEAR, days);
        return dateKey(c.getTimeInMillis());
    }

    public static long toUtcPickerMillis(String key) {
        Calendar local = Calendar.getInstance();
        local.setTimeInMillis(parseDateKey(key));
        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utc.clear();
        utc.set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH));
        return utc.getTimeInMillis();
    }

    public static String dateKeyFromUtcPicker(long utcMillis) {
        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utc.setTimeInMillis(utcMillis);
        Calendar local = Calendar.getInstance();
        local.clear();
        local.set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH));
        return dateKey(local.getTimeInMillis());
    }

    public static String formatDateTime(long time) {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm", VI).format(new Date(time));
    }

    public static String formatShortDate(long time) {
        return new SimpleDateFormat("dd/MM/yyyy", VI).format(new Date(time));
    }

    public static String formatDateKey(String key) {
        return new SimpleDateFormat("EEEE, dd 'tháng' MM yyyy", VI)
                .format(new Date(parseDateKey(key)));
    }

    public static String formatCompactDateKey(String key) {
        return new SimpleDateFormat("dd/MM/yyyy", VI).format(new Date(parseDateKey(key)));
    }

    public static String formatWeekdayDateKey(String key) {
        return new SimpleDateFormat("EEEE", VI).format(new Date(parseDateKey(key)));
    }

    public static String formatFullToday() { return formatDateKey(dateKey()); }

    public static String dayLabel(long time) {
        return new SimpleDateFormat("EEE", VI).format(new Date(time));
    }

    public static long atTime(String key, int hour, int minute) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(parseDateKey(key));
        c.set(Calendar.HOUR_OF_DAY, Math.max(0, Math.min(hour, 23)));
        c.set(Calendar.MINUTE, Math.max(0, Math.min(minute, 59)));
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    public static String formatTime(int hour, int minute) {
        return String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
    }
}
