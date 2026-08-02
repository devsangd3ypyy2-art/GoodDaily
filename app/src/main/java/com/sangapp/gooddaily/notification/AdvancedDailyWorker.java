package com.sangapp.gooddaily.notification;

import android.app.NotificationManager;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.sangapp.gooddaily.R;
import com.sangapp.gooddaily.data.local.GoodDailyDatabase;
import com.sangapp.gooddaily.data.local.entity.FinanceAccountEntity;
import com.sangapp.gooddaily.data.local.entity.BodyRecordEntity;
import com.sangapp.gooddaily.data.local.entity.PersonalRecordEntity;
import com.sangapp.gooddaily.data.local.entity.TransactionEntity;
import com.sangapp.gooddaily.data.local.entity.ScheduleBlockEntity;
import com.sangapp.gooddaily.feature.FeatureCatalog;
import com.sangapp.gooddaily.util.DateUtils;
import com.sangapp.gooddaily.util.MoneyUtils;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdvancedDailyWorker extends Worker {
    public AdvancedDailyWorker(@NonNull Context context, @NonNull WorkerParameters params) { super(context, params); }

    @NonNull @Override public Result doWork() {
        try {
            GoodDailyDatabase db = GoodDailyDatabase.get(getApplicationContext());
            processRecurring(db);
            processBudgets(db);
            processScheduleTemplates(db);
            processDueRecords(db, FeatureCatalog.PLAN_TASK, "Công việc đến hạn");
            processDueRecords(db, FeatureCatalog.PLAN_EVENT, "Sự kiện trong lịch");
            processDueRecords(db, FeatureCatalog.FINANCE_DEBT, "Khoản nợ sắp đến hạn");
            processDueRecords(db, FeatureCatalog.DRIVER_MAINTENANCE, "Bảo dưỡng phương tiện đến hạn");
            processDueRecords(db, FeatureCatalog.HEALTH_MEDICATION, "Đừng quên thuốc và lịch sức khỏe");
            updatePersonalGoals(db);
            return Result.success();
        } catch (Exception e) { return Result.retry(); }
    }

    private void processRecurring(GoodDailyDatabase db) {
        String today = DateUtils.dateKey();
        List<PersonalRecordEntity> records = db.advancedRecordDao().getFeatureSync(FeatureCatalog.FINANCE_RECURRING);
        List<FinanceAccountEntity> accounts = db.transactionDao().getAccountsSync();
        for (PersonalRecordEntity item : records) {
            if (item.dateKey == null || item.dateKey.compareTo(today) > 0 || isPaused(item.status)) continue;
            String rule = upper(item.status);
            String type = rule.contains("INCOME") || rule.contains("THU") ? "INCOME" : "EXPENSE";
            String account = resolveAccount(item.tags, accounts);
            String category = item.details == null || item.details.trim().isEmpty() ? item.title : item.details;
            db.transactionDao().insert(new TransactionEntity(type, Math.max(0, item.numericValue), category, account,
                    "Tự động định kỳ: " + item.title, System.currentTimeMillis()));
            item.dateKey = nextDate(item.dateKey, rule, item.secondaryValue);
            item.updatedAt = System.currentTimeMillis();
            db.advancedRecordDao().update(item);
            notifySimple("Đã ghi giao dịch định kỳ", item.title + " · " + MoneyUtils.format(item.numericValue), (int)(100000 + item.id));
        }
    }

    private void processBudgets(GoodDailyDatabase db) {
        List<PersonalRecordEntity> budgets = db.advancedRecordDao().getFeatureSync(FeatureCatalog.FINANCE_BUDGET);
        long start = DateUtils.startOfMonth();
        long end = DateUtils.endOfMonth(System.currentTimeMillis());
        List<TransactionEntity> transactions = db.transactionDao().getByRangeSync(start, end);
        for (PersonalRecordEntity budget : budgets) {
            if (budget.numericValue <= 0 || isPaused(budget.status)) continue;
            double spent = 0;
            for (TransactionEntity transaction : transactions) {
                if ("EXPENSE".equals(transaction.type) && safe(transaction.category).equalsIgnoreCase(safe(budget.title))) spent += transaction.amount;
            }
            int percent = (int)Math.round(spent / budget.numericValue * 100);
            int bucket = percent >= 100 ? 100 : percent >= 90 ? 90 : percent >= 70 ? 70 : 0;
            if (bucket > budget.countValue) {
                budget.countValue = bucket;
                budget.secondaryValue = spent;
                budget.updatedAt = System.currentTimeMillis();
                db.advancedRecordDao().update(budget);
                notifySimple("Cảnh báo ngân sách " + budget.title,
                        "Đã dùng " + percent + "% · " + MoneyUtils.format(spent) + " / " + MoneyUtils.format(budget.numericValue),
                        (int)(200000 + budget.id));
            }
        }
    }

    private void processScheduleTemplates(GoodDailyDatabase db) {
        String today = DateUtils.dateKey();
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK);
        List<ScheduleBlockEntity> existing = db.plannerDao().getSchedulesByDateSync(today);
        for (PersonalRecordEntity template : db.advancedRecordDao().getFeatureSync(FeatureCatalog.PLAN_TEMPLATE)) {
            if (isPaused(template.status) || !matchesWeekday(safe(template.details) + " " + safe(template.tags), dayOfWeek)) continue;
            boolean duplicate = false;
            for (ScheduleBlockEntity block : existing) {
                if (safe(block.title).equalsIgnoreCase(safe(template.title))
                        && block.startMinutes == template.startMinutes
                        && block.endMinutes == template.endMinutes) {
                    duplicate = true; break;
                }
            }
            if (duplicate) continue;
            ScheduleBlockEntity block = new ScheduleBlockEntity(today, template.title,
                    scheduleCategory(safe(template.details) + " " + safe(template.tags)),
                    template.startMinutes, template.endMinutes, template.secondaryValue > 0,
                    "Tạo từ mẫu thời gian biểu" + (safe(template.details).isEmpty() ? "" : " · " + template.details),
                    System.currentTimeMillis());
            block.id = db.plannerDao().insertSchedule(block);
            existing.add(block);
            if (block.reminderEnabled) ScheduleReminderScheduler.schedule(getApplicationContext(), block);
        }
    }

    private boolean matchesWeekday(String text, int dayOfWeek) {
        String value = upper(text);
        boolean hasAny = value.matches(".*(T2|T3|T4|T5|T6|T7|CN|THỨ|MON|TUE|WED|THUR|FRI|SAT|SUN).*?");
        if (!hasAny) return true;
        switch (dayOfWeek) {
            case java.util.Calendar.MONDAY: return hasToken(value, "T2", "THỨ 2", "MON");
            case java.util.Calendar.TUESDAY: return hasToken(value, "T3", "THỨ 3", "TUE");
            case java.util.Calendar.WEDNESDAY: return hasToken(value, "T4", "THỨ 4", "WED");
            case java.util.Calendar.THURSDAY: return hasToken(value, "T5", "THỨ 5", "THUR");
            case java.util.Calendar.FRIDAY: return hasToken(value, "T6", "THỨ 6", "FRI");
            case java.util.Calendar.SATURDAY: return hasToken(value, "T7", "THỨ 7", "SAT");
            default: return hasToken(value, "CN", "CHỦ NHẬT", "SUN");
        }
    }

    private boolean hasToken(String value, String... tokens) {
        for (String token : tokens) if (value.contains(token)) return true;
        return false;
    }

    private String scheduleCategory(String text) {
        String value = upper(text);
        if (value.contains("HỌC") || value.contains("STUDY")) return "Học tập";
        if (value.contains("NGỦ") || value.contains("SLEEP")) return "Ngủ";
        if (value.contains("TẬP") || value.contains("SỨC KHỎE") || value.contains("WORKOUT")) return "Tập luyện";
        if (value.contains("LÀM") || value.contains("CHẠY") || value.contains("GRAB") || value.contains("WORK")) return "Làm việc";
        if (value.contains("NGHỈ") || value.contains("REST")) return "Nghỉ ngơi";
        return "Khác";
    }

    private void processDueRecords(GoodDailyDatabase db, String feature, String notificationTitle) {
        String today = DateUtils.dateKey();
        for (PersonalRecordEntity item : db.advancedRecordDao().getFeatureSync(feature)) {
            if (item.dateKey == null || item.dateKey.compareTo(today) > 0 || upper(item.status).contains("HOÀN") || upper(item.status).contains("DONE")) continue;
            int todayHash = today.hashCode();
            if (item.countValue == todayHash) continue;
            item.countValue = todayHash;
            item.updatedAt = System.currentTimeMillis();
            db.advancedRecordDao().update(item);
            notifySimple(notificationTitle, item.title + (safe(item.details).isEmpty() ? "" : " · " + item.details), (int)(300000 + item.id));
        }
    }


    private void updatePersonalGoals(GoodDailyDatabase db) {
        List<PersonalRecordEntity> goals = db.advancedRecordDao().getFeatureSync(FeatureCatalog.PERSONAL_GOAL);
        if (goals == null || goals.isEmpty()) return;
        BodyRecordEntity latestBody = db.healthDao().getLatestBodySync();
        int vocabulary = db.plannerDao().getTotalVocabularyCountSync();
        int workouts = db.advancedRecordDao().countFeatureSync(FeatureCatalog.HEALTH_WORKOUT);
        double savings = db.advancedRecordDao().sumSecondarySync(FeatureCatalog.FINANCE_SAVING);
        for (PersonalRecordEntity goal : goals) {
            String source = upper(safe(goal.tags) + " " + safe(goal.details) + " " + safe(goal.title));
            double current = goal.secondaryValue;
            if ((source.contains("WEIGHT") || source.contains("CÂN")) && latestBody != null) current = latestBody.weight;
            else if (source.contains("SAVING") || source.contains("TIẾT KIỆM")) current = savings;
            else if (source.contains("VOCAB") || source.contains("TỪ MỚI")) current = vocabulary;
            else if (source.contains("WORKOUT") || source.contains("TẬP")) current = workouts;
            if (Math.abs(current - goal.secondaryValue) > 0.001) {
                goal.secondaryValue = current;
                if (goal.numericValue > 0 && current >= goal.numericValue) goal.status = "Hoàn thành";
                goal.updatedAt = System.currentTimeMillis();
                db.advancedRecordDao().update(goal);
            }
        }
    }

    private void notifySimple(String title, String text, int id) {
        Context context = getApplicationContext();
        String channel = NotificationSoundManager.ensureChannel(context, "advanced_daily", "Good Daily nâng cao", "Ngân sách, định kỳ, nợ và bảo dưỡng", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channel)
                .setSmallIcon(R.drawable.ic_notification).setContentTitle(title).setContentText(text).setStyle(new NotificationCompat.BigTextStyle().bigText(text)).setAutoCancel(true);
        NotificationSoundManager.applySoundForLegacy(context, builder);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) manager.notify(id, builder.build());
    }

    private String resolveAccount(String tags, List<FinanceAccountEntity> accounts) {
        String value = safe(tags).trim();
        for (FinanceAccountEntity account : accounts) {
            if (account.active && (account.code.equalsIgnoreCase(value) || account.name.equalsIgnoreCase(value) || value.contains(account.code))) return account.code;
        }
        for (FinanceAccountEntity account : accounts) if (account.active) return account.code;
        return "CASH";
    }

    private String nextDate(String current, String rule, double intervalDays) {
        Calendar c = Calendar.getInstance(); c.setTimeInMillis(DateUtils.parseDateKey(current));
        if (rule.contains("YEARLY") || rule.contains("NĂM")) c.add(Calendar.YEAR,1);
        else if (rule.contains("MONTHLY") || rule.contains("THÁNG")) c.add(Calendar.MONTH,1);
        else if (rule.contains("WEEKLY") || rule.contains("TUẦN")) c.add(Calendar.DAY_OF_YEAR,7);
        else c.add(Calendar.DAY_OF_YEAR, intervalDays > 0 ? Math.max(1,(int)Math.round(intervalDays)) : 1);
        return DateUtils.dateKey(c.getTimeInMillis());
    }
    private boolean isPaused(String status){String s=upper(status);return s.contains("TẠM")||s.contains("PAUSE")||s.contains("OFF");}
    private String upper(String value){return safe(value).toUpperCase(new Locale("vi","VN"));}
    private String safe(String value){return value==null?"":value;}
}
