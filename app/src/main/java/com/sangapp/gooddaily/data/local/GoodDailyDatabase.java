package com.sangapp.gooddaily.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.sangapp.gooddaily.data.local.dao.HabitDao;
import com.sangapp.gooddaily.data.local.dao.HealthDao;
import com.sangapp.gooddaily.data.local.dao.PlannerDao;
import com.sangapp.gooddaily.data.local.dao.ReminderDao;
import com.sangapp.gooddaily.data.local.dao.TransactionDao;
import com.sangapp.gooddaily.data.local.entity.BodyRecordEntity;
import com.sangapp.gooddaily.data.local.entity.DailyLearningEntity;
import com.sangapp.gooddaily.data.local.entity.DailyNoteEntity;
import com.sangapp.gooddaily.data.local.entity.FinanceAccountEntity;
import com.sangapp.gooddaily.data.local.entity.HabitCheckInEntity;
import com.sangapp.gooddaily.data.local.entity.HabitEntity;
import com.sangapp.gooddaily.data.local.entity.MealEntity;
import com.sangapp.gooddaily.data.local.entity.ReminderEntity;
import com.sangapp.gooddaily.data.local.entity.ScheduleBlockEntity;
import com.sangapp.gooddaily.data.local.entity.StudySessionEntity;
import com.sangapp.gooddaily.data.local.entity.TaskEntity;
import com.sangapp.gooddaily.data.local.entity.TransactionEntity;
import com.sangapp.gooddaily.data.local.entity.VocabularyEntity;

@Database(
        entities = {
                TransactionEntity.class, FinanceAccountEntity.class,
                BodyRecordEntity.class, MealEntity.class,
                TaskEntity.class, StudySessionEntity.class, VocabularyEntity.class,
                HabitEntity.class, HabitCheckInEntity.class, DailyNoteEntity.class,
                ReminderEntity.class, ScheduleBlockEntity.class, DailyLearningEntity.class
        },
        version = 3,
        exportSchema = true
)
public abstract class GoodDailyDatabase extends RoomDatabase {
    private static volatile GoodDailyDatabase INSTANCE;

    public abstract TransactionDao transactionDao();
    public abstract HealthDao healthDao();
    public abstract PlannerDao plannerDao();
    public abstract HabitDao habitDao();
    public abstract ReminderDao reminderDao();

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `reminders` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`title` TEXT, `description` TEXT, `dateKey` TEXT, " +
                    "`hour` INTEGER NOT NULL, `minute` INTEGER NOT NULL, " +
                    "`repeatType` TEXT, `category` TEXT, " +
                    "`enabled` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL)");
        }
    };

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `finance_accounts` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`code` TEXT, `name` TEXT, `openingBalance` REAL NOT NULL, " +
                    "`active` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL)");
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_finance_accounts_code` ON `finance_accounts` (`code`)");

            database.execSQL("CREATE TABLE IF NOT EXISTS `schedule_blocks` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`dateKey` TEXT, `title` TEXT, `category` TEXT, " +
                    "`startMinutes` INTEGER NOT NULL, `endMinutes` INTEGER NOT NULL, " +
                    "`reminderEnabled` INTEGER NOT NULL, `note` TEXT, `createdAt` INTEGER NOT NULL)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_schedule_blocks_dateKey` ON `schedule_blocks` (`dateKey`)");

            database.execSQL("CREATE TABLE IF NOT EXISTS `daily_learning` (" +
                    "`dateKey` TEXT NOT NULL, `vocabularyCount` INTEGER NOT NULL, " +
                    "`mockScore` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`dateKey`))");
            database.execSQL("INSERT OR IGNORE INTO daily_learning(dateKey, vocabularyCount, mockScore, updatedAt) " +
                    "SELECT dateKey, COUNT(*), 0, COALESCE(MAX(learnedAt), 0) FROM vocabulary GROUP BY dateKey");

            database.execSQL("ALTER TABLE `daily_notes` ADD COLUMN `tags` TEXT");
            database.execSQL("ALTER TABLE `daily_notes` ADD COLUMN `mood` TEXT");

            long now = System.currentTimeMillis();
            database.execSQL("INSERT OR IGNORE INTO finance_accounts(code, name, openingBalance, active, createdAt) VALUES('CASH','Tiền mặt',0,1," + now + ")");
            database.execSQL("INSERT OR IGNORE INTO finance_accounts(code, name, openingBalance, active, createdAt) VALUES('BANK','Ngân hàng',0,1," + now + ")");
            database.execSQL("INSERT OR IGNORE INTO finance_accounts(code, name, openingBalance, active, createdAt) VALUES('EWALLET','Ví điện tử',0,1," + now + ")");
        }
    };

    public static GoodDailyDatabase get(Context context) {
        if (INSTANCE == null) {
            synchronized (GoodDailyDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    GoodDailyDatabase.class,
                                    "good_daily_database"
                            )
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                            .addCallback(new Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    long now = System.currentTimeMillis();
                                    db.execSQL("INSERT OR IGNORE INTO finance_accounts(code, name, openingBalance, active, createdAt) VALUES('CASH','Tiền mặt',0,1," + now + ")");
                                    db.execSQL("INSERT OR IGNORE INTO finance_accounts(code, name, openingBalance, active, createdAt) VALUES('BANK','Ngân hàng',0,1," + now + ")");
                                    db.execSQL("INSERT OR IGNORE INTO finance_accounts(code, name, openingBalance, active, createdAt) VALUES('EWALLET','Ví điện tử',0,1," + now + ")");
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
