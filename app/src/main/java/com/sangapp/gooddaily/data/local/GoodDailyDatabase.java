package com.sangapp.gooddaily.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.sangapp.gooddaily.data.local.dao.AdvancedRecordDao;
import com.sangapp.gooddaily.data.local.dao.FinanceAdvancedDao;
import com.sangapp.gooddaily.data.local.dao.HealthProfileDao;
import com.sangapp.gooddaily.data.local.dao.HabitDao;
import com.sangapp.gooddaily.data.local.dao.HealthDao;
import com.sangapp.gooddaily.data.local.dao.PlannerDao;
import com.sangapp.gooddaily.data.local.dao.ReminderDao;
import com.sangapp.gooddaily.data.local.dao.TransactionDao;
import com.sangapp.gooddaily.data.local.entity.BodyRecordEntity;
import com.sangapp.gooddaily.data.local.entity.CategoryBudgetEntity;
import com.sangapp.gooddaily.data.local.entity.DebtEntity;
import com.sangapp.gooddaily.data.local.entity.DebtPaymentEntity;
import com.sangapp.gooddaily.data.local.entity.FinanceCategoryEntity;
import com.sangapp.gooddaily.data.local.entity.HealthProfileEntity;
import com.sangapp.gooddaily.data.local.entity.MoneyTransferEntity;
import com.sangapp.gooddaily.data.local.entity.PersonalRecordEntity;
import com.sangapp.gooddaily.data.local.entity.RecurringTransactionEntity;
import com.sangapp.gooddaily.data.local.entity.SavingGoalEntity;
import com.sangapp.gooddaily.data.local.entity.TransactionAttachmentEntity;
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
import com.sangapp.gooddaily.feature.driver.data.DriverDao;
import com.sangapp.gooddaily.feature.driver.data.entity.BatteryChargeEntity;
import com.sangapp.gooddaily.feature.driver.data.entity.DriverShiftEntity;
import com.sangapp.gooddaily.feature.driver.data.entity.FuelLogEntity;
import com.sangapp.gooddaily.feature.driver.data.entity.MaintenanceRecordEntity;
import com.sangapp.gooddaily.feature.driver.data.entity.VehicleEntity;
import com.sangapp.gooddaily.feature.metaphysics.data.DivinationDao;
import com.sangapp.gooddaily.feature.metaphysics.data.DivinationSessionEntity;

@Database(
        entities = {
                TransactionEntity.class, FinanceAccountEntity.class,
                BodyRecordEntity.class, MealEntity.class,
                TaskEntity.class, StudySessionEntity.class, VocabularyEntity.class,
                HabitEntity.class, HabitCheckInEntity.class, DailyNoteEntity.class,
                ReminderEntity.class, ScheduleBlockEntity.class, DailyLearningEntity.class,
                PersonalRecordEntity.class, FinanceCategoryEntity.class, MoneyTransferEntity.class,
                RecurringTransactionEntity.class, CategoryBudgetEntity.class, SavingGoalEntity.class,
                DebtEntity.class, DebtPaymentEntity.class, TransactionAttachmentEntity.class,
                HealthProfileEntity.class,
                DivinationSessionEntity.class, DriverShiftEntity.class, VehicleEntity.class,
                BatteryChargeEntity.class, FuelLogEntity.class, MaintenanceRecordEntity.class
        },
        version = 6,
        exportSchema = true
)
public abstract class GoodDailyDatabase extends RoomDatabase {
    private static volatile GoodDailyDatabase INSTANCE;

    public abstract TransactionDao transactionDao();
    public abstract HealthDao healthDao();
    public abstract PlannerDao plannerDao();
    public abstract HabitDao habitDao();
    public abstract ReminderDao reminderDao();
    public abstract AdvancedRecordDao advancedRecordDao();
    public abstract FinanceAdvancedDao financeAdvancedDao();
    public abstract HealthProfileDao healthProfileDao();
    public abstract DivinationDao divinationDao();
    public abstract DriverDao driverDao();

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
            database.execSQL("INSERT OR IGNORE INTO finance_accounts(code, name, openingBalance, active, createdAt) VALUES('OTHER','Tài khoản khác',0,1," + now + ")");
            database.execSQL("INSERT OR IGNORE INTO finance_accounts(code, name, openingBalance, active, createdAt) VALUES('EWALLET','Ví điện tử',0,1," + now + ")");
        }
    };

    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `personal_records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `module` TEXT, `feature` TEXT, `title` TEXT, `details` TEXT, `numericValue` REAL NOT NULL, `secondaryValue` REAL NOT NULL, `countValue` INTEGER NOT NULL, `dateKey` TEXT, `startMinutes` INTEGER NOT NULL, `endMinutes` INTEGER NOT NULL, `status` TEXT, `tags` TEXT, `attachmentUri` TEXT, `favorite` INTEGER NOT NULL, `archived` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_personal_records_module` ON `personal_records` (`module`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_personal_records_feature` ON `personal_records` (`feature`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_personal_records_dateKey` ON `personal_records` (`dateKey`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_personal_records_updatedAt` ON `personal_records` (`updatedAt`)");

            database.execSQL("CREATE TABLE IF NOT EXISTS `finance_categories` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT, `type` TEXT, `colorHex` TEXT, `active` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL)");
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_finance_categories_name_type` ON `finance_categories` (`name`, `type`)");
            database.execSQL("CREATE TABLE IF NOT EXISTS `money_transfers` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `fromAccountCode` TEXT, `toAccountCode` TEXT, `amount` REAL NOT NULL, `fee` REAL NOT NULL, `note` TEXT, `transferTime` INTEGER NOT NULL)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_money_transfers_fromAccountCode` ON `money_transfers` (`fromAccountCode`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_money_transfers_toAccountCode` ON `money_transfers` (`toAccountCode`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_money_transfers_transferTime` ON `money_transfers` (`transferTime`)");
            database.execSQL("CREATE TABLE IF NOT EXISTS `recurring_transactions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `type` TEXT, `amount` REAL NOT NULL, `category` TEXT, `accountCode` TEXT, `title` TEXT, `frequency` TEXT, `nextRunAt` INTEGER NOT NULL, `enabled` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_recurring_transactions_nextRunAt` ON `recurring_transactions` (`nextRunAt`)");
            database.execSQL("CREATE TABLE IF NOT EXISTS `category_budgets` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `category` TEXT, `yearMonth` TEXT, `limitAmount` REAL NOT NULL, `enabled` INTEGER NOT NULL)");
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_category_budgets_category_yearMonth` ON `category_budgets` (`category`, `yearMonth`)");
            database.execSQL("CREATE TABLE IF NOT EXISTS `saving_goals` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT, `targetAmount` REAL NOT NULL, `savedAmount` REAL NOT NULL, `targetDate` INTEGER NOT NULL, `colorHex` TEXT, `completed` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL)");
            database.execSQL("CREATE TABLE IF NOT EXISTS `debts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `direction` TEXT, `person` TEXT, `originalAmount` REAL NOT NULL, `remainingAmount` REAL NOT NULL, `dueDate` INTEGER NOT NULL, `note` TEXT, `status` TEXT, `createdAt` INTEGER NOT NULL)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_debts_status` ON `debts` (`status`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_debts_dueDate` ON `debts` (`dueDate`)");
            database.execSQL("CREATE TABLE IF NOT EXISTS `debt_payments` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `debtId` INTEGER NOT NULL, `amount` REAL NOT NULL, `paidAt` INTEGER NOT NULL, `note` TEXT)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_debt_payments_debtId` ON `debt_payments` (`debtId`)");
            database.execSQL("CREATE TABLE IF NOT EXISTS `transaction_attachments` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `transactionId` INTEGER NOT NULL, `uri` TEXT, `displayName` TEXT, `createdAt` INTEGER NOT NULL)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_transaction_attachments_transactionId` ON `transaction_attachments` (`transactionId`)");
            database.execSQL("CREATE TABLE IF NOT EXISTS `health_profile` (`profileId` TEXT NOT NULL, `birthYear` INTEGER NOT NULL, `gender` TEXT, `activityLevel` TEXT, `goalType` TEXT, `targetWeight` REAL NOT NULL, `manualCalorieTarget` REAL NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`profileId`))");

            database.execSQL("UPDATE finance_accounts SET name='Tài khoản khác' WHERE code='BANK'");
            long now = System.currentTimeMillis();
            String[][] defaults = {
                    {"Ăn uống", "EXPENSE", "#F59E0B"}, {"Di chuyển", "EXPENSE", "#3B82F6"},
                    {"Nhà ở", "EXPENSE", "#8B5CF6"}, {"Học tập", "EXPENSE", "#06B6D4"},
                    {"Sức khỏe", "EXPENSE", "#EF4444"}, {"Mua sắm", "EXPENSE", "#EC4899"},
                    {"Thu nhập công việc", "INCOME", "#16A34A"}, {"Thưởng", "INCOME", "#22C55E"},
                    {"Thu nhập khác", "INCOME", "#14B8A6"}
            };
            for (String[] item : defaults) {
                database.execSQL("INSERT OR IGNORE INTO finance_categories(name,type,colorHex,active,createdAt) VALUES(?,?,?,?,?)", new Object[]{item[0], item[1], item[2], 1, now});
            }
        }
    };

    private static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE `meals` ADD COLUMN `carbs` REAL NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE `meals` ADD COLUMN `fat` REAL NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE `meals` ADD COLUMN `grams` REAL NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE `meals` ADD COLUMN `mealType` TEXT");
            database.execSQL("ALTER TABLE `meals` ADD COLUMN `imagePath` TEXT");
            database.execSQL("ALTER TABLE `body_records` ADD COLUMN `waist` REAL NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE `body_records` ADD COLUMN `chest` REAL NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE `body_records` ADD COLUMN `arm` REAL NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE `body_records` ADD COLUMN `thigh` REAL NOT NULL DEFAULT 0");
        }
    };

    public static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `divination_sessions` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `system` TEXT, `method` TEXT, `question` TEXT, " +
                    "`castTime` INTEGER NOT NULL, `lunarText` TEXT, `inputData` TEXT, `baseHexagramNumber` INTEGER NOT NULL, " +
                    "`baseHexagramName` TEXT, `changedHexagramNumber` INTEGER NOT NULL, `changedHexagramName` TEXT, " +
                    "`nuclearHexagramNumber` INTEGER NOT NULL, `nuclearHexagramName` TEXT, `movingLines` TEXT, " +
                    "`bodyUse` TEXT, `elementRelation` TEXT, `interpretation` TEXT, `verification` TEXT, " +
                    "`verifiedAt` INTEGER NOT NULL, `status` TEXT, `favorite` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_divination_sessions_system` ON `divination_sessions` (`system`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_divination_sessions_castTime` ON `divination_sessions` (`castTime`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_divination_sessions_status` ON `divination_sessions` (`status`)");
            database.execSQL("INSERT INTO divination_sessions(system, method, question, castTime, lunarText, inputData, baseHexagramNumber, baseHexagramName, changedHexagramNumber, changedHexagramName, nuclearHexagramNumber, nuclearHexagramName, movingLines, bodyUse, elementRelation, interpretation, verification, verifiedAt, status, favorite, createdAt, updatedAt) SELECT 'LEGACY', 'MANUAL', title, createdAt, '', tags, 0, 'Chưa xác định', 0, 'Chưa xác định', 0, 'Chưa xác định', '', '', '', details, '', 0, CASE WHEN status IS NULL THEN 'CHO_KIEM_CHUNG' ELSE status END, favorite, createdAt, updatedAt FROM personal_records WHERE feature='DIVINATION_ENTRY'");

            database.execSQL("CREATE TABLE IF NOT EXISTS `driver_shifts` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `dateKey` TEXT, `startTime` INTEGER NOT NULL, `endTime` INTEGER NOT NULL, " +
                    "`revenue` REAL NOT NULL, `bonus` REAL NOT NULL, `tips` REAL NOT NULL, `orderCount` INTEGER NOT NULL, " +
                    "`distanceKm` REAL NOT NULL, `platformFee` REAL NOT NULL, `energyCost` REAL NOT NULL, `mealCost` REAL NOT NULL, " +
                    "`otherCost` REAL NOT NULL, `depreciationCost` REAL NOT NULL, `area` TEXT, `note` TEXT, `status` TEXT, " +
                    "`createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_driver_shifts_dateKey` ON `driver_shifts` (`dateKey`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_driver_shifts_startTime` ON `driver_shifts` (`startTime`)");

            database.execSQL("CREATE TABLE IF NOT EXISTS `vehicles` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT, `plateNumber` TEXT, `vehicleType` TEXT, `energyType` TEXT, " +
                    "`currentOdometerKm` REAL NOT NULL, `purchasePrice` REAL NOT NULL, `purchaseDate` INTEGER NOT NULL, " +
                    "`batteryNominalCapacity` REAL NOT NULL, `active` INTEGER NOT NULL, `note` TEXT, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)");
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_vehicles_name_plateNumber` ON `vehicles` (`name`, `plateNumber`)");

            database.execSQL("CREATE TABLE IF NOT EXISTS `battery_charges` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `vehicleId` INTEGER NOT NULL, `chargedAt` INTEGER NOT NULL, " +
                    "`startPercent` INTEGER NOT NULL, `endPercent` INTEGER NOT NULL, `energyKwh` REAL NOT NULL, `cost` REAL NOT NULL, " +
                    "`odometerKm` REAL NOT NULL, `estimatedRangeKm` REAL NOT NULL, `chargerType` TEXT, `note` TEXT)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_battery_charges_vehicleId` ON `battery_charges` (`vehicleId`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_battery_charges_chargedAt` ON `battery_charges` (`chargedAt`)");

            database.execSQL("CREATE TABLE IF NOT EXISTS `fuel_logs` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `vehicleId` INTEGER NOT NULL, `fueledAt` INTEGER NOT NULL, " +
                    "`liters` REAL NOT NULL, `cost` REAL NOT NULL, `odometerKm` REAL NOT NULL, `fullTank` INTEGER NOT NULL, `station` TEXT, `note` TEXT)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_fuel_logs_vehicleId` ON `fuel_logs` (`vehicleId`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_fuel_logs_fueledAt` ON `fuel_logs` (`fueledAt`)");

            database.execSQL("CREATE TABLE IF NOT EXISTS `maintenance_records` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `vehicleId` INTEGER NOT NULL, `itemType` TEXT, `title` TEXT, " +
                    "`performedAt` INTEGER NOT NULL, `odometerKm` REAL NOT NULL, `cost` REAL NOT NULL, `nextDueAt` INTEGER NOT NULL, " +
                    "`nextDueOdometerKm` REAL NOT NULL, `condition` TEXT, `note` TEXT, `completed` INTEGER NOT NULL)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_maintenance_records_vehicleId` ON `maintenance_records` (`vehicleId`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_maintenance_records_performedAt` ON `maintenance_records` (`performedAt`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_maintenance_records_nextDueAt` ON `maintenance_records` (`nextDueAt`)");

            // Preserve records entered through the generic v2.0 feature manager.
            database.execSQL("INSERT INTO driver_shifts(dateKey,startTime,endTime,revenue,bonus,tips,orderCount,distanceKm,platformFee,energyCost,mealCost,otherCost,depreciationCost,area,note,status,createdAt,updatedAt) " +
                    "SELECT dateKey, createdAt, CASE WHEN updatedAt > createdAt THEN updatedAt ELSE createdAt + 3600000 END, numericValue, 0, 0, countValue, 0, 0, 0, 0, secondaryValue, 0, title, details, COALESCE(status,'LEGACY'), createdAt, updatedAt FROM personal_records WHERE feature='DRIVER_SHIFT'");
            database.execSQL("INSERT OR IGNORE INTO vehicles(name,plateNumber,vehicleType,energyType,currentOdometerKm,purchasePrice,purchaseDate,batteryNominalCapacity,active,note,createdAt,updatedAt) " +
                    "SELECT CASE WHEN title IS NULL OR title='' THEN 'Phương tiện cũ' ELSE title END, '', 'Khác', tags, numericValue, secondaryValue, 0, 0, 1, details, createdAt, updatedAt FROM personal_records WHERE feature='DRIVER_VEHICLE'");
            database.execSQL("INSERT INTO fuel_logs(vehicleId,fueledAt,liters,cost,odometerKm,fullTank,station,note) " +
                    "SELECT 0, createdAt, numericValue, secondaryValue, countValue, 0, title, details FROM personal_records WHERE feature='DRIVER_FUEL'");
            database.execSQL("INSERT INTO maintenance_records(vehicleId,itemType,title,performedAt,odometerKm,cost,nextDueAt,nextDueOdometerKm,condition,note,completed) " +
                    "SELECT 0, COALESCE(tags,'LEGACY'), CASE WHEN title IS NULL OR title='' THEN 'Bảo dưỡng cũ' ELSE title END, createdAt, numericValue, secondaryValue, 0, 0, COALESCE(status,'RECORDED'), details, archived FROM personal_records WHERE feature='DRIVER_MAINTENANCE'");
        }
    };

    public static void closeInstance() {
        synchronized (GoodDailyDatabase.class) {
            if (INSTANCE != null) {
                INSTANCE.close();
                INSTANCE = null;
            }
        }
    }

    public static GoodDailyDatabase get(Context context) {
        if (INSTANCE == null) {
            synchronized (GoodDailyDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    GoodDailyDatabase.class,
                                    "good_daily_database"
                            )
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                            .addCallback(new Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    long now = System.currentTimeMillis();
                                    db.execSQL("INSERT OR IGNORE INTO finance_accounts(code, name, openingBalance, active, createdAt) VALUES('CASH','Tiền mặt',0,1," + now + ")");
                                    db.execSQL("INSERT OR IGNORE INTO finance_accounts(code, name, openingBalance, active, createdAt) VALUES('OTHER','Tài khoản khác',0,1," + now + ")");
                                    db.execSQL("INSERT OR IGNORE INTO finance_accounts(code, name, openingBalance, active, createdAt) VALUES('EWALLET','Ví điện tử',0,1," + now + ")");
                                    String[][] defaults = {
                                            {"Ăn uống", "EXPENSE", "#F59E0B"}, {"Di chuyển", "EXPENSE", "#3B82F6"},
                                            {"Nhà ở", "EXPENSE", "#8B5CF6"}, {"Học tập", "EXPENSE", "#06B6D4"},
                                            {"Sức khỏe", "EXPENSE", "#EF4444"}, {"Mua sắm", "EXPENSE", "#EC4899"},
                                            {"Thu nhập công việc", "INCOME", "#16A34A"}, {"Thưởng", "INCOME", "#22C55E"},
                                            {"Thu nhập khác", "INCOME", "#14B8A6"}
                                    };
                                    for (String[] item : defaults) {
                                        db.execSQL("INSERT OR IGNORE INTO finance_categories(name,type,colorHex,active,createdAt) VALUES(?,?,?,?,?)", new Object[]{item[0], item[1], item[2], 1, now});
                                    }
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
