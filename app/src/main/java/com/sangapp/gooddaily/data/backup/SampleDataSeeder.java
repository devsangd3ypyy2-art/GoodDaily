package com.sangapp.gooddaily.data.backup;

import android.content.Context;

import com.sangapp.gooddaily.data.local.GoodDailyDatabase;
import com.sangapp.gooddaily.data.local.entity.BodyRecordEntity;
import com.sangapp.gooddaily.data.local.entity.DailyLearningEntity;
import com.sangapp.gooddaily.data.local.entity.DailyNoteEntity;
import com.sangapp.gooddaily.data.local.entity.ScheduleBlockEntity;
import com.sangapp.gooddaily.data.local.entity.HabitCheckInEntity;
import com.sangapp.gooddaily.data.local.entity.HabitEntity;
import com.sangapp.gooddaily.data.local.entity.MealEntity;
import com.sangapp.gooddaily.data.local.entity.StudySessionEntity;
import com.sangapp.gooddaily.data.local.entity.TaskEntity;
import com.sangapp.gooddaily.data.local.entity.TransactionEntity;
import com.sangapp.gooddaily.data.local.entity.VocabularyEntity;
import com.sangapp.gooddaily.util.AppExecutors;
import com.sangapp.gooddaily.util.DateUtils;

public final class SampleDataSeeder {
    private SampleDataSeeder() {}

    public static void seed(Context context, Runnable done) {
        AppExecutors.database().execute(() -> {
            GoodDailyDatabase db = GoodDailyDatabase.get(context);
            long now = System.currentTimeMillis();
            db.transactionDao().insert(new TransactionEntity("INCOME", 520000, "Thu nhập công việc", "OTHER", "Doanh thu hôm nay", now - 3_600_000));
            db.transactionDao().insert(new TransactionEntity("EXPENSE", 45000, "Xăng xe", "CASH", "Đổ xăng", now - 2_800_000));
            db.transactionDao().insert(new TransactionEntity("EXPENSE", 35000, "Ăn uống", "CASH", "Bữa trưa", now - 2_000_000));
            for (int i = 1; i <= 6; i++) {
                long day = DateUtils.startOfDaysAgo(i) + 14 * 3_600_000L;
                db.transactionDao().insert(new TransactionEntity("EXPENSE", 30000 + i * 12000, i % 2 == 0 ? "Ăn uống" : "Xăng xe", "CASH", "Chi tiêu mẫu", day));
            }
            db.healthDao().insertBody(new BodyRecordEntity(55.4, 185, 12.5, 42.0, now));
            db.healthDao().insertMeal(new MealEntity("Cơm, trứng và chuối", 720, 31, now - 4_000_000));
            db.healthDao().insertMeal(new MealEntity("Bữa sáng", 430, 18, now - 8_000_000));
            db.plannerDao().insertTask(new TaskEntity("Học Android Room", 60, false, DateUtils.dateKey()));
            db.plannerDao().insertTask(new TaskEntity("Tập thể dục", 30, true, DateUtils.dateKey()));
            db.plannerDao().insertStudy(new StudySessionEntity("Tiếng Anh", 45, "Ôn ngữ pháp và nghe", DateUtils.dateKey(), now));
            db.plannerDao().insertVocabulary(new VocabularyEntity("legacy", "dữ liệu cũ tương thích", DateUtils.dateKey(), now));
            db.plannerDao().upsertLearning(new DailyLearningEntity(DateUtils.dateKey(), 12, 425, now));
            db.plannerDao().upsertNote(new DailyNoteEntity(DateUtils.dateKey(), "Hôm nay hoàn thành phần quản lý lịch và tài chính.", "học tập, phát triển app", "Tích cực", now));
            db.plannerDao().insertSchedule(new ScheduleBlockEntity(DateUtils.dateKey(), "Chạy giao hàng", "WORK", 7 * 60, 11 * 60, true, "Ca sáng", now));
            db.plannerDao().insertSchedule(new ScheduleBlockEntity(DateUtils.dateKey(), "Học Android", "STUDY", 19 * 60, 21 * 60, true, "Room và MVVM", now));
            db.plannerDao().insertSchedule(new ScheduleBlockEntity(DateUtils.dateKey(), "Ngủ", "SLEEP", 23 * 60, 7 * 60, false, "Chuẩn bị ngủ sớm", now));
            long habit1 = db.habitDao().insertHabit(new HabitEntity("Uống đủ 2 lít nước", 7, true));
            db.habitDao().insertHabit(new HabitEntity("Ngủ trước 23 giờ", 6, true));
            db.habitDao().insertHabit(new HabitEntity("Học ít nhất 10 từ mới", 7, true));
            db.habitDao().insertCheckIn(new HabitCheckInEntity(habit1, DateUtils.dateKey(), now));
            if (done != null) new android.os.Handler(android.os.Looper.getMainLooper()).post(done);
        });
    }
}
