package com.sangapp.gooddaily.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.sangapp.gooddaily.data.local.entity.AccountBalance;
import com.sangapp.gooddaily.data.local.entity.CategoryTotal;
import com.sangapp.gooddaily.data.local.entity.DailyAmount;
import com.sangapp.gooddaily.data.local.entity.FinanceAccountEntity;
import com.sangapp.gooddaily.data.local.entity.TransactionEntity;

import java.util.List;

@Dao
public interface TransactionDao {
    @Insert long insert(TransactionEntity entity);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertAll(List<TransactionEntity> entities);
    @Delete void delete(TransactionEntity entity);
    @Update void update(TransactionEntity entity);

    @Insert(onConflict = OnConflictStrategy.IGNORE) long insertAccount(FinanceAccountEntity entity);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insertAccounts(List<FinanceAccountEntity> entities);
    @Update void updateAccount(FinanceAccountEntity entity);
    @Query("UPDATE finance_accounts SET active = 0 WHERE id = :id") void deactivateAccount(long id);
    @Query("SELECT * FROM finance_accounts WHERE id = :id LIMIT 1") FinanceAccountEntity getAccountSync(long id);

    @Query("SELECT * FROM transactions ORDER BY transactionTime DESC")
    LiveData<List<TransactionEntity>> observeAll();

    @Query("SELECT * FROM transactions WHERE transactionTime BETWEEN :start AND :end ORDER BY transactionTime DESC")
    LiveData<List<TransactionEntity>> observeByRange(long start, long end);

    @Query("SELECT * FROM transactions ORDER BY transactionTime DESC")
    List<TransactionEntity> getAllSync();

    @Query("SELECT * FROM finance_accounts ORDER BY id ASC")
    LiveData<List<FinanceAccountEntity>> observeAccounts();

    @Query("SELECT * FROM finance_accounts ORDER BY id ASC")
    List<FinanceAccountEntity> getAccountsSync();

    @Query("SELECT a.id AS id, a.code AS code, a.name AS name, a.openingBalance AS openingBalance, " +
            "a.openingBalance + " +
            "COALESCE((SELECT SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE -t.amount END) FROM transactions t WHERE t.account = a.code), 0) + " +
            "COALESCE((SELECT SUM(mt.amount) FROM money_transfers mt WHERE mt.toAccountCode = a.code), 0) - " +
            "COALESCE((SELECT SUM(mt.amount + mt.fee) FROM money_transfers mt WHERE mt.fromAccountCode = a.code), 0) AS currentBalance " +
            "FROM finance_accounts a WHERE a.active = 1 ORDER BY a.id ASC")
    LiveData<List<AccountBalance>> observeAccountBalances();

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = :type AND transactionTime BETWEEN :start AND :end")
    LiveData<Double> observeTotalByTypeAndRange(String type, long start, long end);

    @Query("SELECT " +
            "COALESCE((SELECT SUM(openingBalance) FROM finance_accounts WHERE active = 1), 0) + " +
            "COALESCE((SELECT SUM(CASE WHEN type = 'INCOME' THEN amount ELSE -amount END) FROM transactions), 0) - " +
            "COALESCE((SELECT SUM(fee) FROM money_transfers), 0)")
    LiveData<Double> observeTotalBalance();

    @Query("SELECT COALESCE((SELECT openingBalance FROM finance_accounts WHERE code = :account LIMIT 1), 0) + " +
            "COALESCE((SELECT SUM(CASE WHEN type = 'INCOME' THEN amount ELSE -amount END) FROM transactions WHERE account = :account), 0) + " +
            "COALESCE((SELECT SUM(amount) FROM money_transfers WHERE toAccountCode = :account), 0) - " +
            "COALESCE((SELECT SUM(amount + fee) FROM money_transfers WHERE fromAccountCode = :account), 0)")
    LiveData<Double> observeAccountBalance(String account);

    @Query("SELECT strftime('%d/%m', transactionTime / 1000, 'unixepoch', 'localtime') AS label, SUM(amount) AS total " +
            "FROM transactions WHERE type = 'EXPENSE' AND transactionTime BETWEEN :start AND :end " +
            "GROUP BY strftime('%Y-%m-%d', transactionTime / 1000, 'unixepoch', 'localtime') ORDER BY transactionTime ASC")
    LiveData<List<DailyAmount>> observeDailyExpenses(long start, long end);


    @Query("SELECT category AS category, COALESCE(SUM(amount),0) AS total FROM transactions WHERE type = 'EXPENSE' AND transactionTime BETWEEN :start AND :end GROUP BY category ORDER BY total DESC")
    LiveData<List<CategoryTotal>> observeExpenseByCategory(long start, long end);

    @Query("SELECT category AS category, COALESCE(SUM(amount),0) AS total FROM transactions WHERE type = 'EXPENSE' AND transactionTime BETWEEN :start AND :end GROUP BY category ORDER BY total DESC")
    List<CategoryTotal> getExpenseByCategorySync(long start, long end);

    @Query("SELECT * FROM transactions WHERE transactionTime BETWEEN :start AND :end ORDER BY transactionTime DESC")
    List<TransactionEntity> getByRangeSync(long start, long end);

    @Query("SELECT COALESCE(SUM(amount),0) FROM transactions WHERE type = :type AND transactionTime BETWEEN :start AND :end")
    double totalByTypeRangeSync(String type, long start, long end);

    @Query("DELETE FROM transactions") void clear();
    @Query("DELETE FROM finance_accounts") void clearAccounts();
    @Query("SELECT * FROM transactions WHERE (:query = '' OR note LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%') AND (:account = 'ALL' OR account = :account) AND (:category = 'ALL' OR category = :category) AND transactionTime BETWEEN :start AND :end ORDER BY transactionTime DESC")
    LiveData<List<TransactionEntity>> searchFiltered(String query, String account, String category, long start, long end);
}
