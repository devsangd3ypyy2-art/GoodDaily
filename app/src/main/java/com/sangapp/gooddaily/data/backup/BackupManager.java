package com.sangapp.gooddaily.data.backup;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.sangapp.gooddaily.data.local.GoodDailyDatabase;
import com.sangapp.gooddaily.data.local.entity.BodyRecordEntity;
import com.sangapp.gooddaily.data.local.entity.DailyNoteEntity;
import com.sangapp.gooddaily.data.local.entity.DailyLearningEntity;
import com.sangapp.gooddaily.data.local.entity.FinanceAccountEntity;
import com.sangapp.gooddaily.data.local.entity.ScheduleBlockEntity;
import com.sangapp.gooddaily.data.local.entity.HabitCheckInEntity;
import com.sangapp.gooddaily.data.local.entity.HabitEntity;
import com.sangapp.gooddaily.data.local.entity.MealEntity;
import com.sangapp.gooddaily.data.local.entity.ReminderEntity;
import com.sangapp.gooddaily.data.local.entity.StudySessionEntity;
import com.sangapp.gooddaily.data.local.entity.TaskEntity;
import com.sangapp.gooddaily.data.local.entity.TransactionEntity;
import com.sangapp.gooddaily.data.local.entity.VocabularyEntity;
import com.sangapp.gooddaily.data.local.prefs.LocalUserStore;
import com.sangapp.gooddaily.notification.CustomReminderScheduler;
import com.sangapp.gooddaily.notification.ReminderScheduler;
import com.sangapp.gooddaily.notification.ScheduleReminderScheduler;
import com.sangapp.gooddaily.util.AppExecutors;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BackupManager {
    public interface Callback {
        void onSuccess(String message);
        void onError(String message);
    }

    private final Context context;
    private final GoodDailyDatabase db;
    private final Handler main = new Handler(Looper.getMainLooper());

    public BackupManager(Context context) {
        this.context = context.getApplicationContext();
        this.db = GoodDailyDatabase.get(context);
    }

    public void exportTo(Uri uri, Callback callback) {
        AppExecutors.io().execute(() -> {
            try {
                JSONObject root = new JSONObject();
                root.put("app", "Good Daily");
                root.put("backupVersion", 3);
                root.put("createdAt", System.currentTimeMillis());
                root.put("transactions", transactionsToJson(db.transactionDao().getAllSync()));
                root.put("financeAccounts", accountsToJson(db.transactionDao().getAccountsSync()));
                root.put("bodyRecords", bodiesToJson(db.healthDao().getBodiesSync()));
                root.put("meals", mealsToJson(db.healthDao().getMealsSync()));
                root.put("tasks", tasksToJson(db.plannerDao().getTasksSync()));
                root.put("studySessions", studiesToJson(db.plannerDao().getStudiesSync()));
                root.put("vocabulary", vocabularyToJson(db.plannerDao().getVocabularySync()));
                root.put("habits", habitsToJson(db.habitDao().getHabitsSync()));
                root.put("habitCheckIns", checkInsToJson(db.habitDao().getCheckInsSync()));
                root.put("dailyNotes", notesToJson(db.plannerDao().getNotesSync()));
                root.put("dailyLearning", learningToJson(db.plannerDao().getLearningSync()));
                root.put("scheduleBlocks", schedulesToJson(db.plannerDao().getSchedulesSync()));
                root.put("reminders", remindersToJson(db.reminderDao().getAllSync()));
                LocalUserStore store = new LocalUserStore(context);
                root.put("settings", new JSONObject()
                        .put("themeKey", store.getThemeKey())
                        .put("monthlyBudget", store.getMonthlyBudget())
                        .put("financialAlertEnabled", store.isFinancialAlertEnabled())
                        .put("hideAmounts", store.isHideAmountsEnabled())
                        .put("dailyReminderEnabled", store.isReminderEnabled())
                        .put("weeklyVocabularyGoal", store.getWeeklyVocabularyGoal()));

                try (OutputStream output = context.getContentResolver().openOutputStream(uri, "w")) {
                    if (output == null) throw new IllegalStateException("Không mở được file đích");
                    output.write(root.toString(2).getBytes(StandardCharsets.UTF_8));
                }
                success(callback, "Đã xuất dữ liệu Good Daily thành công.");
            } catch (Exception e) {
                error(callback, "Xuất dữ liệu thất bại: " + e.getMessage());
            }
        });
    }

    public void importFrom(Uri uri, Callback callback) {
        AppExecutors.io().execute(() -> {
            try {
                StringBuilder text = new StringBuilder();
                try (InputStream input = context.getContentResolver().openInputStream(uri)) {
                    if (input == null) throw new IllegalStateException("Không mở được file sao lưu");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
                    String line;
                    while ((line = reader.readLine()) != null) text.append(line);
                }

                JSONObject root = new JSONObject(text.toString());
                if (!"Good Daily".equals(root.optString("app"))) {
                    throw new IllegalArgumentException("File không phải bản sao lưu Good Daily");
                }

                List<TransactionEntity> transactions = jsonToTransactions(root.optJSONArray("transactions"));
                List<FinanceAccountEntity> accounts = jsonToAccounts(root.optJSONArray("financeAccounts"));
                List<BodyRecordEntity> bodies = jsonToBodies(root.optJSONArray("bodyRecords"));
                List<MealEntity> meals = jsonToMeals(root.optJSONArray("meals"));
                List<TaskEntity> tasks = jsonToTasks(root.optJSONArray("tasks"));
                List<StudySessionEntity> studies = jsonToStudies(root.optJSONArray("studySessions"));
                List<VocabularyEntity> vocabulary = jsonToVocabulary(root.optJSONArray("vocabulary"));
                List<HabitEntity> habits = jsonToHabits(root.optJSONArray("habits"));
                List<HabitCheckInEntity> checkIns = jsonToCheckIns(root.optJSONArray("habitCheckIns"));
                List<DailyNoteEntity> notes = jsonToNotes(root.optJSONArray("dailyNotes"));
                List<DailyLearningEntity> importedLearning = jsonToLearning(root.optJSONArray("dailyLearning"));
                final List<DailyLearningEntity> learning =
                        importedLearning.isEmpty() && !vocabulary.isEmpty()
                                ? learningFromVocabulary(vocabulary)
                                : importedLearning;
                List<ScheduleBlockEntity> schedules = jsonToSchedules(root.optJSONArray("scheduleBlocks"));
                List<ReminderEntity> reminders = jsonToReminders(root.optJSONArray("reminders"));
                JSONObject settings = root.optJSONObject("settings");

                db.runInTransaction(() -> {
                    db.reminderDao().clear();
                    db.habitDao().clearCheckIns();
                    db.habitDao().clearHabits();
                    db.plannerDao().clearSchedules();
                    db.plannerDao().clearLearning();
                    db.plannerDao().clearNotes();
                    db.plannerDao().clearVocabulary();
                    db.plannerDao().clearStudies();
                    db.plannerDao().clearTasks();
                    db.healthDao().clearMeals();
                    db.healthDao().clearBodies();
                    db.transactionDao().clear();
                    db.transactionDao().clearAccounts();

                    db.transactionDao().insertAccounts(accounts);
                    db.transactionDao().insertAll(transactions);
                    db.healthDao().insertBodies(bodies);
                    db.healthDao().insertMeals(meals);
                    db.plannerDao().insertTasks(tasks);
                    db.plannerDao().insertStudies(studies);
                    db.plannerDao().insertVocabularyList(vocabulary);
                    db.habitDao().insertHabits(habits);
                    db.habitDao().insertCheckIns(checkIns);
                    db.plannerDao().insertNotes(notes);
                    db.plannerDao().insertLearningList(learning);
                    db.plannerDao().insertSchedules(schedules);
                    db.reminderDao().insertAll(reminders);
                });

                if (settings != null) {
                    LocalUserStore store = new LocalUserStore(context);
                    store.setThemeKey(settings.optString("themeKey", store.getThemeKey()));
                    store.setMonthlyBudget(settings.optDouble("monthlyBudget", store.getMonthlyBudget()));
                    store.setFinancialAlertEnabled(settings.optBoolean("financialAlertEnabled", store.isFinancialAlertEnabled()));
                    store.setHideAmountsEnabled(settings.optBoolean("hideAmounts", store.isHideAmountsEnabled()));
                    store.setWeeklyVocabularyGoal(settings.optInt("weeklyVocabularyGoal", store.getWeeklyVocabularyGoal()));
                    boolean daily = settings.optBoolean("dailyReminderEnabled", store.isReminderEnabled());
                    store.setReminderEnabled(daily);
                    if (daily) ReminderScheduler.schedule(context); else ReminderScheduler.cancel(context);
                }
                CustomReminderScheduler.scheduleAll(context);
                for (ScheduleBlockEntity block : schedules) ScheduleReminderScheduler.schedule(context, block);
                success(callback, "Đã khôi phục " + transactions.size() + " giao dịch, " + schedules.size() + " lịch và toàn bộ dữ liệu cá nhân.");
            } catch (Exception e) {
                error(callback, "Nhập dữ liệu thất bại: " + e.getMessage());
            }
        });
    }

    private void success(Callback callback, String message) { main.post(() -> callback.onSuccess(message)); }
    private void error(Callback callback, String message) { main.post(() -> callback.onError(message)); }

    private JSONArray accountsToJson(List<FinanceAccountEntity> list) throws Exception {
        JSONArray array = new JSONArray();
        for (FinanceAccountEntity e : list) array.put(new JSONObject().put("id",e.id).put("code",e.code).put("name",e.name).put("openingBalance",e.openingBalance).put("active",e.active).put("createdAt",e.createdAt));
        return array;
    }
    private JSONArray learningToJson(List<DailyLearningEntity> list) throws Exception {
        JSONArray array = new JSONArray();
        for (DailyLearningEntity e : list) array.put(new JSONObject().put("dateKey",e.dateKey).put("vocabularyCount",e.vocabularyCount).put("mockScore",e.mockScore).put("updatedAt",e.updatedAt));
        return array;
    }
    private JSONArray schedulesToJson(List<ScheduleBlockEntity> list) throws Exception {
        JSONArray array = new JSONArray();
        for (ScheduleBlockEntity e : list) array.put(new JSONObject().put("id",e.id).put("dateKey",e.dateKey).put("title",e.title).put("category",e.category).put("startMinutes",e.startMinutes).put("endMinutes",e.endMinutes).put("reminderEnabled",e.reminderEnabled).put("note",e.note).put("createdAt",e.createdAt));
        return array;
    }

    private JSONArray transactionsToJson(List<TransactionEntity> list) throws Exception {
        JSONArray array = new JSONArray();
        for (TransactionEntity e : list) array.put(new JSONObject().put("id",e.id).put("type",e.type).put("amount",e.amount).put("category",e.category).put("account",e.account).put("note",e.note).put("transactionTime",e.transactionTime));
        return array;
    }
    private JSONArray bodiesToJson(List<BodyRecordEntity> list) throws Exception {
        JSONArray array = new JSONArray();
        for (BodyRecordEntity e : list) array.put(new JSONObject().put("id",e.id).put("weight",e.weight).put("height",e.height).put("bodyFatPercent",e.bodyFatPercent).put("muscleMass",e.muscleMass).put("recordedAt",e.recordedAt));
        return array;
    }
    private JSONArray mealsToJson(List<MealEntity> list) throws Exception {
        JSONArray array = new JSONArray();
        for (MealEntity e : list) array.put(new JSONObject().put("id",e.id).put("name",e.name).put("calories",e.calories).put("protein",e.protein).put("eatenAt",e.eatenAt));
        return array;
    }
    private JSONArray tasksToJson(List<TaskEntity> list) throws Exception {
        JSONArray array = new JSONArray();
        for (TaskEntity e : list) array.put(new JSONObject().put("id",e.id).put("title",e.title).put("expectedMinutes",e.expectedMinutes).put("completed",e.completed).put("dateKey",e.dateKey));
        return array;
    }
    private JSONArray studiesToJson(List<StudySessionEntity> list) throws Exception {
        JSONArray array = new JSONArray();
        for (StudySessionEntity e : list) array.put(new JSONObject().put("id",e.id).put("subject",e.subject).put("minutes",e.minutes).put("note",e.note).put("dateKey",e.dateKey).put("createdAt",e.createdAt));
        return array;
    }
    private JSONArray vocabularyToJson(List<VocabularyEntity> list) throws Exception {
        JSONArray array = new JSONArray();
        for (VocabularyEntity e : list) array.put(new JSONObject().put("id",e.id).put("word",e.word).put("meaning",e.meaning).put("dateKey",e.dateKey).put("learnedAt",e.learnedAt));
        return array;
    }
    private JSONArray habitsToJson(List<HabitEntity> list) throws Exception {
        JSONArray array = new JSONArray();
        for (HabitEntity e : list) array.put(new JSONObject().put("id",e.id).put("name",e.name).put("targetDaysPerWeek",e.targetDaysPerWeek).put("active",e.active));
        return array;
    }
    private JSONArray checkInsToJson(List<HabitCheckInEntity> list) throws Exception {
        JSONArray array = new JSONArray();
        for (HabitCheckInEntity e : list) array.put(new JSONObject().put("id",e.id).put("habitId",e.habitId).put("dateKey",e.dateKey).put("checkedAt",e.checkedAt));
        return array;
    }
    private JSONArray notesToJson(List<DailyNoteEntity> list) throws Exception {
        JSONArray array = new JSONArray();
        for (DailyNoteEntity e : list) array.put(new JSONObject().put("dateKey",e.dateKey).put("content",e.content).put("tags",e.tags).put("mood",e.mood).put("updatedAt",e.updatedAt));
        return array;
    }
    private JSONArray remindersToJson(List<ReminderEntity> list) throws Exception {
        JSONArray array = new JSONArray();
        for (ReminderEntity e : list) array.put(new JSONObject()
                .put("id", e.id).put("title", e.title).put("description", e.description)
                .put("dateKey", e.dateKey).put("hour", e.hour).put("minute", e.minute)
                .put("repeatType", e.repeatType).put("category", e.category)
                .put("enabled", e.enabled).put("createdAt", e.createdAt));
        return array;
    }


    private List<DailyLearningEntity> learningFromVocabulary(List<VocabularyEntity> vocabulary) {
        java.util.Map<String, Integer> counts = new java.util.LinkedHashMap<>();
        java.util.Map<String, Long> latest = new java.util.HashMap<>();
        for (VocabularyEntity item : vocabulary) {
            String key = item.dateKey == null ? "" : item.dateKey;
            if (key.isEmpty()) continue;
            counts.put(key, counts.getOrDefault(key, 0) + 1);
            latest.put(key, Math.max(latest.getOrDefault(key, 0L), item.learnedAt));
        }
        List<DailyLearningEntity> result = new ArrayList<>();
        for (java.util.Map.Entry<String, Integer> entry : counts.entrySet()) {
            result.add(new DailyLearningEntity(entry.getKey(), entry.getValue(), 0,
                    latest.getOrDefault(entry.getKey(), System.currentTimeMillis())));
        }
        return result;
    }

    private List<FinanceAccountEntity> jsonToAccounts(JSONArray array) throws Exception {
        List<FinanceAccountEntity> list = new ArrayList<>();
        if (array != null) {
            for (int i=0;i<array.length();i++) { JSONObject o=array.getJSONObject(i); FinanceAccountEntity e=new FinanceAccountEntity(o.optString("code"),o.optString("name"),o.optDouble("openingBalance"),o.optBoolean("active",true),o.optLong("createdAt",System.currentTimeMillis())); e.id=o.optLong("id"); list.add(e); }
        }
        if (list.isEmpty()) {
            long now=System.currentTimeMillis();
            list.add(new FinanceAccountEntity("CASH","Tiền mặt",0,true,now));
            list.add(new FinanceAccountEntity("OTHER","Tài khoản khác",0,true,now));
            list.add(new FinanceAccountEntity("EWALLET","Ví điện tử",0,true,now));
        }
        return list;
    }
    private List<DailyLearningEntity> jsonToLearning(JSONArray array) throws Exception {
        List<DailyLearningEntity> list=new ArrayList<>(); if(array==null)return list;
        for(int i=0;i<array.length();i++){JSONObject o=array.getJSONObject(i);list.add(new DailyLearningEntity(o.getString("dateKey"),o.optInt("vocabularyCount"),o.optInt("mockScore"),o.optLong("updatedAt")));}return list;
    }
    private List<ScheduleBlockEntity> jsonToSchedules(JSONArray array) throws Exception {
        List<ScheduleBlockEntity> list=new ArrayList<>(); if(array==null)return list;
        for(int i=0;i<array.length();i++){JSONObject o=array.getJSONObject(i);ScheduleBlockEntity e=new ScheduleBlockEntity(o.optString("dateKey"),o.optString("title"),o.optString("category"),o.optInt("startMinutes"),o.optInt("endMinutes"),o.optBoolean("reminderEnabled"),o.optString("note"),o.optLong("createdAt"));e.id=o.optLong("id");list.add(e);}return list;
    }

    private List<TransactionEntity> jsonToTransactions(JSONArray array) throws Exception {
        List<TransactionEntity> list = new ArrayList<>(); if (array == null) return list;
        for (int i=0;i<array.length();i++){JSONObject o=array.getJSONObject(i); TransactionEntity e=new TransactionEntity(o.getString("type"),o.getDouble("amount"),o.optString("category"),o.optString("account"),o.optString("note"),o.getLong("transactionTime")); e.id=o.optLong("id"); list.add(e);} return list;
    }
    private List<BodyRecordEntity> jsonToBodies(JSONArray array) throws Exception {
        List<BodyRecordEntity> list = new ArrayList<>(); if (array == null) return list;
        for (int i=0;i<array.length();i++){JSONObject o=array.getJSONObject(i); BodyRecordEntity e=new BodyRecordEntity(o.getDouble("weight"),o.getDouble("height"),o.optDouble("bodyFatPercent"),o.optDouble("muscleMass"),o.getLong("recordedAt")); e.id=o.optLong("id"); list.add(e);} return list;
    }
    private List<MealEntity> jsonToMeals(JSONArray array) throws Exception {
        List<MealEntity> list = new ArrayList<>(); if (array == null) return list;
        for (int i=0;i<array.length();i++){JSONObject o=array.getJSONObject(i); MealEntity e=new MealEntity(o.getString("name"),o.getDouble("calories"),o.optDouble("protein"),o.getLong("eatenAt")); e.id=o.optLong("id"); list.add(e);} return list;
    }
    private List<TaskEntity> jsonToTasks(JSONArray array) throws Exception {
        List<TaskEntity> list = new ArrayList<>(); if (array == null) return list;
        for (int i=0;i<array.length();i++){JSONObject o=array.getJSONObject(i); TaskEntity e=new TaskEntity(o.getString("title"),o.optInt("expectedMinutes"),o.optBoolean("completed"),o.getString("dateKey")); e.id=o.optLong("id"); list.add(e);} return list;
    }
    private List<StudySessionEntity> jsonToStudies(JSONArray array) throws Exception {
        List<StudySessionEntity> list = new ArrayList<>(); if (array == null) return list;
        for (int i=0;i<array.length();i++){JSONObject o=array.getJSONObject(i); StudySessionEntity e=new StudySessionEntity(o.getString("subject"),o.optInt("minutes"),o.optString("note"),o.getString("dateKey"),o.optLong("createdAt")); e.id=o.optLong("id"); list.add(e);} return list;
    }
    private List<VocabularyEntity> jsonToVocabulary(JSONArray array) throws Exception {
        List<VocabularyEntity> list = new ArrayList<>(); if (array == null) return list;
        for (int i=0;i<array.length();i++){JSONObject o=array.getJSONObject(i); VocabularyEntity e=new VocabularyEntity(o.getString("word"),o.optString("meaning"),o.getString("dateKey"),o.optLong("learnedAt")); e.id=o.optLong("id"); list.add(e);} return list;
    }
    private List<HabitEntity> jsonToHabits(JSONArray array) throws Exception {
        List<HabitEntity> list = new ArrayList<>(); if (array == null) return list;
        for (int i=0;i<array.length();i++){JSONObject o=array.getJSONObject(i); HabitEntity e=new HabitEntity(o.getString("name"),o.optInt("targetDaysPerWeek",7),o.optBoolean("active",true)); e.id=o.optLong("id"); list.add(e);} return list;
    }
    private List<HabitCheckInEntity> jsonToCheckIns(JSONArray array) throws Exception {
        List<HabitCheckInEntity> list = new ArrayList<>(); if (array == null) return list;
        for (int i=0;i<array.length();i++){JSONObject o=array.getJSONObject(i); HabitCheckInEntity e=new HabitCheckInEntity(o.getLong("habitId"),o.getString("dateKey"),o.optLong("checkedAt")); e.id=o.optLong("id"); list.add(e);} return list;
    }
    private List<DailyNoteEntity> jsonToNotes(JSONArray array) throws Exception {
        List<DailyNoteEntity> list = new ArrayList<>(); if (array == null) return list;
        for (int i=0;i<array.length();i++){JSONObject o=array.getJSONObject(i); list.add(new DailyNoteEntity(o.getString("dateKey"),o.optString("content"),o.optString("tags"),o.optString("mood"),o.optLong("updatedAt")));} return list;
    }
    private List<ReminderEntity> jsonToReminders(JSONArray array) throws Exception {
        List<ReminderEntity> list = new ArrayList<>(); if (array == null) return list;
        for (int i=0;i<array.length();i++) {
            JSONObject o = array.getJSONObject(i);
            ReminderEntity e = new ReminderEntity(
                    o.optString("title"), o.optString("description"), o.optString("dateKey"),
                    o.optInt("hour", 20), o.optInt("minute", 0), o.optString("repeatType", "ONCE"),
                    o.optString("category", "GENERAL"), o.optBoolean("enabled", true), o.optLong("createdAt")
            );
            e.id = o.optLong("id");
            list.add(e);
        }
        return list;
    }
}
