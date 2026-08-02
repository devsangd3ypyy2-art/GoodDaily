package com.sangapp.gooddaily.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.sangapp.gooddaily.data.local.entity.CategoryBudgetEntity;
import com.sangapp.gooddaily.data.local.entity.DebtEntity;
import com.sangapp.gooddaily.data.local.entity.DebtPaymentEntity;
import com.sangapp.gooddaily.data.local.entity.FinanceCategoryEntity;
import com.sangapp.gooddaily.data.local.entity.MoneyTransferEntity;
import com.sangapp.gooddaily.data.local.entity.RecurringTransactionEntity;
import com.sangapp.gooddaily.data.local.entity.SavingGoalEntity;
import com.sangapp.gooddaily.data.local.entity.TransactionAttachmentEntity;

import java.util.List;

@Dao
public interface FinanceAdvancedDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE) long insertCategory(FinanceCategoryEntity entity);
    @Update void updateCategory(FinanceCategoryEntity entity);
    @Delete void deleteCategory(FinanceCategoryEntity entity);
    @Query("SELECT * FROM finance_categories WHERE active = 1 AND type = :type ORDER BY name")
    LiveData<List<FinanceCategoryEntity>> observeCategories(String type);
    @Query("SELECT * FROM finance_categories ORDER BY type, name")
    List<FinanceCategoryEntity> getCategoriesSync();

    @Insert long insertTransfer(MoneyTransferEntity entity);
    @Update void updateTransfer(MoneyTransferEntity entity);
    @Delete void deleteTransfer(MoneyTransferEntity entity);
    @Query("SELECT * FROM money_transfers ORDER BY transferTime DESC")
    LiveData<List<MoneyTransferEntity>> observeTransfers();
    @Query("SELECT * FROM money_transfers ORDER BY transferTime DESC")
    List<MoneyTransferEntity> getTransfersSync();

    @Insert long insertRecurring(RecurringTransactionEntity entity);
    @Update void updateRecurring(RecurringTransactionEntity entity);
    @Delete void deleteRecurring(RecurringTransactionEntity entity);
    @Query("SELECT * FROM recurring_transactions ORDER BY enabled DESC, nextRunAt ASC")
    LiveData<List<RecurringTransactionEntity>> observeRecurring();
    @Query("SELECT * FROM recurring_transactions WHERE enabled = 1 AND nextRunAt <= :now")
    List<RecurringTransactionEntity> dueRecurring(long now);

    @Insert(onConflict = OnConflictStrategy.REPLACE) long saveBudget(CategoryBudgetEntity entity);
    @Delete void deleteBudget(CategoryBudgetEntity entity);
    @Query("SELECT * FROM category_budgets WHERE yearMonth = :yearMonth ORDER BY category")
    LiveData<List<CategoryBudgetEntity>> observeBudgets(String yearMonth);
    @Query("SELECT * FROM category_budgets ORDER BY yearMonth DESC, category")
    List<CategoryBudgetEntity> getBudgetsSync();

    @Insert long insertSavingGoal(SavingGoalEntity entity);
    @Update void updateSavingGoal(SavingGoalEntity entity);
    @Delete void deleteSavingGoal(SavingGoalEntity entity);
    @Query("SELECT * FROM saving_goals ORDER BY completed ASC, targetDate ASC")
    LiveData<List<SavingGoalEntity>> observeSavingGoals();
    @Query("SELECT * FROM saving_goals ORDER BY completed ASC, targetDate ASC")
    List<SavingGoalEntity> getSavingGoalsSync();

    @Insert long insertDebt(DebtEntity entity);
    @Update void updateDebt(DebtEntity entity);
    @Delete void deleteDebt(DebtEntity entity);
    @Query("SELECT * FROM debts ORDER BY status ASC, dueDate ASC")
    LiveData<List<DebtEntity>> observeDebts();
    @Query("SELECT * FROM debts ORDER BY status ASC, dueDate ASC")
    List<DebtEntity> getDebtsSync();

    @Insert long insertDebtPayment(DebtPaymentEntity entity);
    @Query("SELECT * FROM debt_payments WHERE debtId = :debtId ORDER BY paidAt DESC")
    List<DebtPaymentEntity> debtPayments(long debtId);

    @Insert long insertAttachment(TransactionAttachmentEntity entity);
    @Delete void deleteAttachment(TransactionAttachmentEntity entity);
    @Query("SELECT * FROM transaction_attachments WHERE transactionId = :transactionId ORDER BY createdAt DESC")
    LiveData<List<TransactionAttachmentEntity>> observeAttachments(long transactionId);
    @Query("SELECT * FROM transaction_attachments WHERE transactionId = :transactionId ORDER BY createdAt DESC")
    List<TransactionAttachmentEntity> getAttachmentsSync(long transactionId);
    @Query("DELETE FROM transaction_attachments WHERE transactionId = :transactionId")
    void deleteAttachmentsForTransaction(long transactionId);

    @Query("DELETE FROM finance_categories") void clearCategories();
    @Query("DELETE FROM money_transfers") void clearTransfers();
    @Query("DELETE FROM recurring_transactions") void clearRecurring();
    @Query("DELETE FROM category_budgets") void clearBudgets();
    @Query("DELETE FROM saving_goals") void clearSavingGoals();
    @Query("DELETE FROM debts") void clearDebts();
    @Query("DELETE FROM debt_payments") void clearDebtPayments();
    @Query("DELETE FROM transaction_attachments") void clearAttachments();
}
