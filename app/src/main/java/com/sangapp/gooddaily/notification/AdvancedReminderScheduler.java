package com.sangapp.gooddaily.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.sangapp.gooddaily.data.local.entity.PersonalRecordEntity;
import com.sangapp.gooddaily.util.DateUtils;

public final class AdvancedReminderScheduler {
    private AdvancedReminderScheduler() {}


    public static void scheduleAll(Context context) {
        com.sangapp.gooddaily.util.AppExecutors.io().execute(() -> {
            java.util.List<PersonalRecordEntity> items = com.sangapp.gooddaily.data.local.GoodDailyDatabase
                    .get(context).advancedRecordDao()
                    .getFeatureSync(com.sangapp.gooddaily.feature.FeatureCatalog.ADVANCED_REMINDER);
            for (PersonalRecordEntity item : items) {
                String status = item.status == null ? "" : item.status.toLowerCase(java.util.Locale.ROOT);
                if (item.archived || status.contains("hoàn thành") || status.contains("tạm dừng") || status.contains("disabled")) continue;
                schedule(context, item);
            }
        });
    }

    public static void schedule(Context context, PersonalRecordEntity item) {
        if (item == null || item.id <= 0) return;
        long trigger = DateUtils.parseDateKey(item.dateKey) + item.startMinutes * 60_000L - Math.max(0, (long)item.numericValue) * 60_000L;
        while (trigger <= System.currentTimeMillis()) {
            long next = nextTrigger(trigger, item);
            if (next <= trigger) break;
            trigger = next;
        }
        Intent intent = new Intent(context, AdvancedReminderReceiver.class);
        intent.setAction(AdvancedReminderReceiver.ACTION_FIRE);
        intent.putExtra("record_id", item.id);
        PendingIntent pending = PendingIntent.getBroadcast(context, requestCode(item.id,0), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !manager.canScheduleExactAlarms()) {
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pending);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pending);
        } else manager.setExact(AlarmManager.RTC_WAKEUP, trigger, pending);
    }

    public static void scheduleAt(Context context, long recordId, long triggerAt) {
        Intent intent = new Intent(context, AdvancedReminderReceiver.class);
        intent.setAction(AdvancedReminderReceiver.ACTION_FIRE);
        intent.putExtra("record_id", recordId);
        PendingIntent pending = PendingIntent.getBroadcast(context, requestCode(recordId,0), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending);
            else manager.set(AlarmManager.RTC_WAKEUP, triggerAt, pending);
        }
    }

    public static void cancel(Context context, long recordId) {
        Intent intent = new Intent(context, AdvancedReminderReceiver.class);
        intent.setAction(AdvancedReminderReceiver.ACTION_FIRE);
        PendingIntent pending = PendingIntent.getBroadcast(context, requestCode(recordId,0), intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager != null && pending != null) manager.cancel(pending);
    }

    public static long nextTrigger(long current, String repeat) {
        java.util.Calendar c = java.util.Calendar.getInstance(); c.setTimeInMillis(current);
        String type = repeat == null ? "" : repeat.toUpperCase(java.util.Locale.ROOT);
        if (type.contains("DAILY") || type.contains("HẰNG NGÀY")) c.add(java.util.Calendar.DAY_OF_YEAR,1);
        else if (type.contains("WEEKLY") || type.contains("HẰNG TUẦN")) c.add(java.util.Calendar.DAY_OF_YEAR,7);
        else if (type.contains("MONTHLY") || type.contains("HẰNG THÁNG")) c.add(java.util.Calendar.MONTH,1);
        else if (type.contains("YEARLY") || type.contains("HẰNG NĂM")) c.add(java.util.Calendar.YEAR,1);
        else return current;
        return c.getTimeInMillis();
    }

    /**
     * Calculates the next reminder occurrence. For WEEKLY reminders the user can write
     * days in the details/tags field such as "T2,T4,T6", "thứ 2, thứ 4" or
     * "MON,WED,FRI". Only one exact alarm is kept at a time and the receiver schedules
     * the next selected weekday after it fires.
     */
    public static long nextTrigger(long current, PersonalRecordEntity item) {
        String repeat = item == null ? "" : item.status;
        String type = repeat == null ? "" : repeat.toUpperCase(java.util.Locale.ROOT);
        if (!(type.contains("WEEKLY") || type.contains("HẰNG TUẦN"))) {
            return nextTrigger(current, repeat);
        }
        java.util.Set<Integer> days = selectedWeekdays(item == null ? "" :
                ((item.details == null ? "" : item.details) + " " + (item.tags == null ? "" : item.tags)));
        if (days.isEmpty()) return nextTrigger(current, repeat);
        java.util.Calendar candidate = java.util.Calendar.getInstance();
        candidate.setTimeInMillis(current);
        for (int offset = 1; offset <= 7; offset++) {
            candidate.add(java.util.Calendar.DAY_OF_YEAR, 1);
            if (days.contains(candidate.get(java.util.Calendar.DAY_OF_WEEK))) return candidate.getTimeInMillis();
        }
        return nextTrigger(current, repeat);
    }

    private static java.util.Set<Integer> selectedWeekdays(String text) {
        java.util.Set<Integer> result = new java.util.HashSet<>();
        String value = text == null ? "" : text.toUpperCase(new java.util.Locale("vi", "VN"));
        addDay(result, value, java.util.Calendar.MONDAY, "T2", "THỨ 2", "THU 2", "MON");
        addDay(result, value, java.util.Calendar.TUESDAY, "T3", "THỨ 3", "THU 3", "TUE");
        addDay(result, value, java.util.Calendar.WEDNESDAY, "T4", "THỨ 4", "THU 4", "WED");
        addDay(result, value, java.util.Calendar.THURSDAY, "T5", "THỨ 5", "THU 5", "THUR", "THURSDAY");
        addDay(result, value, java.util.Calendar.FRIDAY, "T6", "THỨ 6", "THU 6", "FRI");
        addDay(result, value, java.util.Calendar.SATURDAY, "T7", "THỨ 7", "THU 7", "SAT");
        addDay(result, value, java.util.Calendar.SUNDAY, "CN", "CHỦ NHẬT", "CHU NHAT", "SUN");
        return result;
    }

    private static void addDay(java.util.Set<Integer> result, String text, int day, String... tokens) {
        for (String token : tokens) {
            if (containsToken(text, token)) { result.add(day); return; }
        }
    }

    private static boolean containsToken(String text, String token) {
        if (token.length() > 2) return text.contains(token);
        String normalized = text.replace(';', ',').replace('|', ',').replace('/', ',');
        for (String part : normalized.split("[,\\s]+")) if (token.equals(part.trim())) return true;
        return false;
    }

    private static int requestCode(long id, int suffix) { return (int)((id * 31 + suffix) & 0x7fffffff); }
}
