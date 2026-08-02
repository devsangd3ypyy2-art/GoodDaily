package com.sangapp.gooddaily.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.sangapp.gooddaily.data.local.GoodDailyDatabase;
import com.sangapp.gooddaily.data.local.entity.AccountBalance;
import com.sangapp.gooddaily.data.local.entity.FinanceAccountEntity;
import com.sangapp.gooddaily.data.local.entity.TransactionEntity;
import com.sangapp.gooddaily.util.AppExecutors;
import com.sangapp.gooddaily.util.DateUtils;

import java.util.List;

public class FinanceViewModel extends AndroidViewModel {
    public static final String PERIOD_DAY = "DAY";
    public static final String PERIOD_MONTH = "MONTH";
    public static final String PERIOD_YEAR = "YEAR";

    private final GoodDailyDatabase db;
    private final MutableLiveData<Range> selectedRange = new MutableLiveData<>();
    private final LiveData<List<TransactionEntity>> filteredTransactions;
    private final LiveData<Double> filteredIncome;
    private final LiveData<Double> filteredExpense;

    public FinanceViewModel(@NonNull Application application) {
        super(application);
        db = GoodDailyDatabase.get(application);
        ensureDefaultAccounts();
        selectPeriod(PERIOD_MONTH, System.currentTimeMillis());
        filteredTransactions = Transformations.switchMap(selectedRange,
                range -> db.transactionDao().observeByRange(range.start, range.end));
        filteredIncome = Transformations.switchMap(selectedRange,
                range -> db.transactionDao().observeTotalByTypeAndRange("INCOME", range.start, range.end));
        filteredExpense = Transformations.switchMap(selectedRange,
                range -> db.transactionDao().observeTotalByTypeAndRange("EXPENSE", range.start, range.end));
    }

    private void ensureDefaultAccounts() {
        AppExecutors.database().execute(() -> {
            long now = System.currentTimeMillis();
            db.transactionDao().insertAccount(new FinanceAccountEntity("CASH", "Tiền mặt", 0, true, now));
            db.transactionDao().insertAccount(new FinanceAccountEntity("OTHER", "Tài khoản khác", 0, true, now));
            db.transactionDao().insertAccount(new FinanceAccountEntity("EWALLET", "Ví điện tử", 0, true, now));
        });
    }

    public void selectPeriod(String period, long referenceTime) {
        long start;
        long end;
        if (PERIOD_DAY.equals(period)) {
            start = DateUtils.startOfDay(referenceTime);
            end = DateUtils.endOfDay(referenceTime);
        } else if (PERIOD_YEAR.equals(period)) {
            start = DateUtils.startOfYear(referenceTime);
            end = DateUtils.endOfYear(referenceTime);
        } else {
            start = DateUtils.startOfMonth(referenceTime);
            end = DateUtils.endOfMonth(referenceTime);
        }
        selectedRange.setValue(new Range(period, referenceTime, start, end));
    }

    public LiveData<Range> selectedRange() { return selectedRange; }
    public LiveData<List<TransactionEntity>> filteredTransactions() { return filteredTransactions; }
    public LiveData<Double> filteredIncome() { return filteredIncome; }
    public LiveData<Double> filteredExpense() { return filteredExpense; }

    public LiveData<List<TransactionEntity>> transactions() { return db.transactionDao().observeAll(); }
    public LiveData<List<AccountBalance>> accountBalances() { return db.transactionDao().observeAccountBalances(); }
    public LiveData<List<FinanceAccountEntity>> accounts() { return db.transactionDao().observeAccounts(); }
    public LiveData<Double> totalBalance() { return db.transactionDao().observeTotalBalance(); }
    public LiveData<Double> cashBalance() { return db.transactionDao().observeAccountBalance("CASH"); }
    public LiveData<Double> otherBalance() { return db.transactionDao().observeAccountBalance("OTHER"); }
    public LiveData<Double> walletBalance() { return db.transactionDao().observeAccountBalance("EWALLET"); }
    public LiveData<Double> weekIncome() { return db.transactionDao().observeTotalByTypeAndRange("INCOME", DateUtils.startOfWeek(), DateUtils.endOfDay()); }
    public LiveData<Double> weekExpense() { return db.transactionDao().observeTotalByTypeAndRange("EXPENSE", DateUtils.startOfWeek(), DateUtils.endOfDay()); }
    public LiveData<Double> monthIncome() { return db.transactionDao().observeTotalByTypeAndRange("INCOME", DateUtils.startOfMonth(), DateUtils.endOfDay()); }
    public LiveData<Double> monthExpense() { return db.transactionDao().observeTotalByTypeAndRange("EXPENSE", DateUtils.startOfMonth(), DateUtils.endOfDay()); }
    public LiveData<Double> yearIncome() { return db.transactionDao().observeTotalByTypeAndRange("INCOME", DateUtils.startOfYear(), DateUtils.endOfDay()); }
    public LiveData<Double> yearExpense() { return db.transactionDao().observeTotalByTypeAndRange("EXPENSE", DateUtils.startOfYear(), DateUtils.endOfDay()); }

    public interface SaveCallback { void onSaved(long transactionId); }

    public void save(TransactionEntity entity) { save(entity, null); }

    public void save(TransactionEntity entity, SaveCallback callback) {
        AppExecutors.database().execute(() -> {
            long id = entity.id;
            if (entity.id == 0) {
                id = db.transactionDao().insert(entity);
                entity.id = id;
            } else {
                db.transactionDao().update(entity);
            }
            if (callback != null) callback.onSaved(id);
        });
    }

    public void saveAccount(FinanceAccountEntity entity) {
        AppExecutors.database().execute(() -> {
            if (entity.id == 0) db.transactionDao().insertAccount(entity);
            else db.transactionDao().updateAccount(entity);
        });
    }

    public void delete(TransactionEntity entity) {
        AppExecutors.database().execute(() -> {
            db.financeAdvancedDao().deleteAttachmentsForTransaction(entity.id);
            db.transactionDao().delete(entity);
        });
    }

    public static final class Range {
        public final String period;
        public final long referenceTime;
        public final long start;
        public final long end;

        public Range(String period, long referenceTime, long start, long end) {
            this.period = period;
            this.referenceTime = referenceTime;
            this.start = start;
            this.end = end;
        }
    }
}
