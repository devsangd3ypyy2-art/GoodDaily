package com.sangapp.gooddaily.ui.finance;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sangapp.gooddaily.R;
import com.sangapp.gooddaily.data.backup.FinancePdfExporter;
import com.sangapp.gooddaily.data.local.entity.AccountBalance;
import com.sangapp.gooddaily.data.local.entity.FinanceAccountEntity;
import com.sangapp.gooddaily.data.local.entity.TransactionEntity;
import com.sangapp.gooddaily.data.local.prefs.LocalUserStore;
import com.sangapp.gooddaily.databinding.BottomSheetTransactionBinding;
import com.sangapp.gooddaily.databinding.DialogBudgetBinding;
import com.sangapp.gooddaily.databinding.DialogInitialBalanceBinding;
import com.sangapp.gooddaily.databinding.FragmentFinanceBinding;
import com.sangapp.gooddaily.notification.FinanceAlertManager;
import com.sangapp.gooddaily.ui.adapter.TransactionAdapter;
import com.sangapp.gooddaily.util.DateUtils;
import com.sangapp.gooddaily.util.MoneyUtils;
import com.sangapp.gooddaily.util.ThemeUtils;
import com.sangapp.gooddaily.viewmodel.FinanceViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FinanceFragment extends Fragment {
    private FragmentFinanceBinding binding;
    private FinanceViewModel vm;
    private LocalUserStore userStore;
    private List<FinanceAccountEntity> accounts = new ArrayList<>();
    private List<AccountBalance> currentAccountBalances = new ArrayList<>();
    private List<TransactionEntity> allTransactions = new ArrayList<>();
    private double weekIncome, weekExpense, monthIncome, monthExpense, yearIncome, yearExpense;
    private double filteredIncome, filteredExpense;
    private boolean hideAmounts;
    private String selectedPeriod = FinanceViewModel.PERIOD_MONTH;
    private long periodReferenceTime = System.currentTimeMillis();

    private final ActivityResultLauncher<String> createFinancePdf = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("application/pdf"),
            uri -> {
                if (uri == null || userStore == null) return;
                FinancePdfExporter.export(
                        requireContext(), uri, allTransactions,
                        weekIncome, weekExpense, monthIncome, monthExpense, yearIncome, yearExpense,
                        userStore.getMonthlyBudget(),
                        new FinancePdfExporter.Callback() {
                            @Override public void onSuccess() { toast("Đã xuất báo cáo PDF."); }
                            @Override public void onError(String message) {
                                new MaterialAlertDialogBuilder(requireContext())
                                        .setTitle("Không thể xuất PDF")
                                        .setMessage(message)
                                        .setPositiveButton("Đóng", null)
                                        .show();
                            }
                        });
            });

    private final ActivityResultLauncher<String> notificationPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            granted -> toast(granted ? "Đã cấp quyền cảnh báo tài chính." : "Chưa cấp quyền thông báo."));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentFinanceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        vm = new ViewModelProvider(this).get(FinanceViewModel.class);
        userStore = new LocalUserStore(requireContext());
        hideAmounts = userStore.isHideAmountsEnabled();
        applyTheme();

        TransactionAdapter adapter = new TransactionAdapter(new TransactionAdapter.Listener() {
            @Override public void onClick(TransactionEntity entity) { showTransactionSheet(entity.type, entity); }
            @Override public void onLongClick(TransactionEntity entity) { confirmDelete(entity); }
        });
        binding.recyclerTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerTransactions.setAdapter(adapter);
        binding.recyclerTransactions.setNestedScrollingEnabled(false);

        vm.filteredTransactions().observe(getViewLifecycleOwner(), items -> {
            List<TransactionEntity> safe = items == null ? new ArrayList<>() : items;
            adapter.submitList(new ArrayList<>(safe));
            boolean empty = safe.isEmpty();
            binding.tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.recyclerTransactions.setVisibility(empty ? View.GONE : View.VISIBLE);
        });
        vm.transactions().observe(getViewLifecycleOwner(), items ->
                allTransactions = items == null ? new ArrayList<>() : new ArrayList<>(items));
        vm.accounts().observe(getViewLifecycleOwner(), value -> accounts = value == null ? new ArrayList<>() : value);
        vm.accountBalances().observe(getViewLifecycleOwner(), this::renderAccounts);
        vm.totalBalance().observe(getViewLifecycleOwner(), value ->
                binding.tvTotalBalance.setText(displayMoney(value == null ? 0 : value)));
        vm.filteredIncome().observe(getViewLifecycleOwner(), value -> {
            filteredIncome = value == null ? 0 : value;
            renderFilteredSummary();
        });
        vm.filteredExpense().observe(getViewLifecycleOwner(), value -> {
            filteredExpense = value == null ? 0 : value;
            renderFilteredSummary();
        });
        vm.selectedRange().observe(getViewLifecycleOwner(), range -> {
            if (range == null) return;
            selectedPeriod = range.period;
            periodReferenceTime = range.referenceTime;
            renderPeriodButton();
        });
        vm.weekIncome().observe(getViewLifecycleOwner(), value -> { weekIncome = n(value); renderPeriods(); });
        vm.weekExpense().observe(getViewLifecycleOwner(), value -> { weekExpense = n(value); renderPeriods(); });
        vm.monthIncome().observe(getViewLifecycleOwner(), value -> { monthIncome = n(value); renderPeriods(); });
        vm.monthExpense().observe(getViewLifecycleOwner(), value -> {
            monthExpense = n(value); renderPeriods(); renderBudget(); FinanceAlertManager.check(requireContext(), monthExpense);
        });
        vm.yearIncome().observe(getViewLifecycleOwner(), value -> { yearIncome = n(value); renderPeriods(); });
        vm.yearExpense().observe(getViewLifecycleOwner(), value -> { yearExpense = n(value); renderPeriods(); });

        binding.periodToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.btnPeriodDay) selectedPeriod = FinanceViewModel.PERIOD_DAY;
            else if (checkedId == R.id.btnPeriodYear) selectedPeriod = FinanceViewModel.PERIOD_YEAR;
            else selectedPeriod = FinanceViewModel.PERIOD_MONTH;
            vm.selectPeriod(selectedPeriod, periodReferenceTime);
        });
        binding.btnPeriodDate.setOnClickListener(v -> showPeriodDatePicker());
        binding.btnAddIncome.setOnClickListener(v -> showTransactionSheet("INCOME", null));
        binding.btnAddExpense.setOnClickListener(v -> showTransactionSheet("EXPENSE", null));
        binding.btnEditOpeningBalance.setOnClickListener(v -> showOpeningBalances());
        binding.cardBudget.setOnClickListener(v -> showBudgetDialog());
        binding.btnExportFinancePdf.setOnClickListener(v -> {
            String stamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.US).format(new Date());
            createFinancePdf.launch("GoodDaily_TaiChinh_" + stamp + ".pdf");
        });
        renderBudget();
        scrollToRequestedSection();
    }

    private void scrollToRequestedSection() {
        Bundle args = getArguments();
        String focus = args == null ? "" : args.getString("focus", "");
        binding.financeScroll.post(() -> {
            if ("history".equals(focus)) binding.financeScroll.smoothScrollTo(0, binding.tvHistoryTitle.getTop());
            else binding.financeScroll.smoothScrollTo(0, 0);
        });
    }

    private void applyTheme() {
        int accent = ThemeUtils.getPrimaryColor(requireContext(), userStore.getThemeKey());
        int container = ThemeUtils.getContainerColor(requireContext(), userStore.getThemeKey());
        binding.financeIconBox.setCardBackgroundColor(container);
        ((android.widget.ImageView) binding.financeIconBox.getChildAt(0)).setColorFilter(accent);
        binding.budgetProgress.setIndicatorColor(accent);
        ThemeUtils.tintFilledButton(binding.btnAddIncome, requireContext(), userStore.getThemeKey());
        ThemeUtils.tintTonalButton(binding.btnExportFinancePdf, requireContext(), userStore.getThemeKey());
        ThemeUtils.tintTonalButton(binding.btnEditOpeningBalance, requireContext(), userStore.getThemeKey());
        ThemeUtils.tintTonalButton(binding.btnPeriodDate, requireContext(), userStore.getThemeKey());
    }

    private void renderAccounts(List<AccountBalance> balances) {
        currentAccountBalances = balances == null ? new ArrayList<>() : new ArrayList<>(balances);
        if (balances == null || balances.isEmpty()) {
            binding.tvAccountBalances.setText("Chưa thiết lập nguồn tiền");
            return;
        }
        StringBuilder builder = new StringBuilder();
        for (AccountBalance balance : balances) {
            if (builder.length() > 0) builder.append('\n');
            builder.append(balance.name).append(": ").append(displayMoney(balance.currentBalance));
        }
        binding.tvAccountBalances.setText(builder.toString());
    }

    private void renderFilteredSummary() {
        if (binding == null) return;
        binding.tvFilteredSummary.setText("Thu: " + displayMoney(filteredIncome)
                + "\nChi: " + displayMoney(filteredExpense)
                + "\nCòn lại trong kỳ: " + displayMoney(filteredIncome - filteredExpense));
    }

    private void renderPeriods() {
        if (binding == null) return;
        binding.tvPeriodStats.setText("Tuần này: thu " + displayMoney(weekIncome) + " · chi " + displayMoney(weekExpense)
                + "\nTháng này: thu " + displayMoney(monthIncome) + " · chi " + displayMoney(monthExpense)
                + "\nNăm nay: thu " + displayMoney(yearIncome) + " · chi " + displayMoney(yearExpense));
    }

    private void renderPeriodButton() {
        if (FinanceViewModel.PERIOD_DAY.equals(selectedPeriod)) {
            binding.btnPeriodDate.setText("Ngày " + DateUtils.formatShortDate(periodReferenceTime));
        } else if (FinanceViewModel.PERIOD_YEAR.equals(selectedPeriod)) {
            binding.btnPeriodDate.setText(new SimpleDateFormat("'Năm' yyyy", new Locale("vi", "VN")).format(new Date(periodReferenceTime)));
        } else {
            binding.btnPeriodDate.setText(new SimpleDateFormat("'Tháng' MM/yyyy", new Locale("vi", "VN")).format(new Date(periodReferenceTime)));
        }
    }

    private void showPeriodDatePicker() {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Chọn ngày tham chiếu")
                .setSelection(DateUtils.toUtcPickerMillis(DateUtils.dateKey(periodReferenceTime)))
                .build();
        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection == null) return;
            periodReferenceTime = DateUtils.parseDateKey(DateUtils.dateKeyFromUtcPicker(selection));
            vm.selectPeriod(selectedPeriod, periodReferenceTime);
        });
        picker.show(getParentFragmentManager(), "finance_period_date");
    }

    private void renderBudget() {
        if (binding == null || userStore == null) return;
        double budget = userStore.getMonthlyBudget();
        if (budget <= 0) {
            binding.tvBudgetStatus.setText("Chưa đặt hạn mức · chạm để thiết lập");
            binding.budgetProgress.setProgressCompat(0, true);
            return;
        }
        int percent = (int) Math.min(100, Math.round(monthExpense / budget * 100));
        binding.budgetProgress.setProgressCompat(percent, true);
        String status = "Đã chi " + displayMoney(monthExpense) + " / " + displayMoney(budget) + " · " + percent + "%";
        if (monthExpense > budget) status += " · vượt " + displayMoney(monthExpense - budget);
        binding.tvBudgetStatus.setText(status);
    }

    private void showOpeningBalances() {
        if (accounts.isEmpty()) {
            toast("Đang tải tài khoản tiền, hãy thử lại sau một chút.");
            return;
        }
        DialogInitialBalanceBinding d = DialogInitialBalanceBinding.inflate(getLayoutInflater());
        FinanceAccountEntity cash = accountByCode("CASH");
        FinanceAccountEntity bank = accountByCode("BANK");
        FinanceAccountEntity wallet = accountByCode("EWALLET");
        if (cash != null) d.edtOpeningCash.setText(cleanNumber(currentBalanceByCode("CASH")));
        if (bank != null) d.edtOpeningBank.setText(cleanNumber(currentBalanceByCode("BANK")));
        if (wallet != null) d.edtOpeningWallet.setText(cleanNumber(currentBalanceByCode("EWALLET")));

        androidx.appcompat.app.AlertDialog alert = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cập nhật số tiền đang có")
                .setView(d.getRoot())
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", null)
                .create();
        alert.setOnShowListener(x -> alert.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    updateCurrentBalance(cash, parseDouble(text(d.edtOpeningCash.getText())));
                    updateCurrentBalance(bank, parseDouble(text(d.edtOpeningBank.getText())));
                    updateCurrentBalance(wallet, parseDouble(text(d.edtOpeningWallet.getText())));
                    alert.dismiss();
                    toast("Đã cập nhật số tiền hiện có.");
                }));
        alert.show();
    }

    private void updateCurrentBalance(FinanceAccountEntity account, double desiredCurrentBalance) {
        if (account == null) return;
        double current = currentBalanceByCode(account.code);
        account.openingBalance = account.openingBalance + Math.max(0, desiredCurrentBalance) - current;
        vm.saveAccount(account);
    }

    private double currentBalanceByCode(String code) {
        for (AccountBalance balance : currentAccountBalances) {
            if (code.equals(balance.code)) return balance.currentBalance;
        }
        FinanceAccountEntity account = accountByCode(code);
        return account == null ? 0 : account.openingBalance;
    }

    private FinanceAccountEntity accountByCode(String code) {
        for (FinanceAccountEntity account : accounts) if (code.equals(account.code)) return account;
        return null;
    }

    private void showTransactionSheet(String type, @Nullable TransactionEntity existing) {
        BottomSheetDialog sheet = new BottomSheetDialog(requireContext());
        BottomSheetTransactionBinding d = BottomSheetTransactionBinding.inflate(getLayoutInflater());
        sheet.setContentView(d.getRoot());
        ThemeUtils.tintFilledButton(d.btnSaveTransaction, requireContext(), userStore.getThemeKey());
        ThemeUtils.tintTonalButton(d.btnTransactionDate, requireContext(), userStore.getThemeKey());

        boolean income = "INCOME".equals(type);
        String[] categories = income
                ? new String[]{"Thu nhập công việc", "Lương", "Thưởng", "Bán hàng", "Thu nhập khác"}
                : new String[]{"Ăn uống", "Xăng xe", "Nhà ở", "Học tập", "Sức khỏe", "Mua sắm", "Giải trí", "Bảo dưỡng xe", "Khác"};
        List<String> accountNames = new ArrayList<>();
        for (FinanceAccountEntity account : accounts) if (account.active) accountNames.add(account.name);
        if (accountNames.isEmpty()) {
            accountNames.add("Tiền mặt"); accountNames.add("Ngân hàng"); accountNames.add("Ví điện tử");
        }
        d.dropdownTransactionCategory.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, categories));
        d.dropdownTransactionAccount.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, accountNames));

        int typeColor = ContextCompat.getColor(requireContext(), income ? R.color.income : R.color.expense);
        int typeContainer = ContextCompat.getColor(requireContext(), income ? R.color.income_container : R.color.expense_container);
        d.transactionTypeIconBox.setCardBackgroundColor(typeContainer);
        d.imgTransactionType.setImageResource(income ? R.drawable.ic_arrow_down : R.drawable.ic_arrow_up);
        d.imgTransactionType.setColorFilter(typeColor);
        d.tvTransactionSheetTitle.setText(existing == null
                ? (income ? "Thêm khoản thu" : "Thêm khoản chi")
                : (income ? "Sửa khoản thu" : "Sửa khoản chi"));

        final long[] transactionTime = {existing == null ? System.currentTimeMillis() : existing.transactionTime};
        if (existing != null) {
            d.edtTransactionAmount.setText(cleanNumber(existing.amount));
            d.dropdownTransactionCategory.setText(existing.category, false);
            d.dropdownTransactionAccount.setText(accountName(existing.account), false);
            d.edtTransactionNote.setText(existing.note);
        } else {
            d.dropdownTransactionCategory.setText(categories[0], false);
            d.dropdownTransactionAccount.setText(accountNames.get(0), false);
        }
        d.btnTransactionDate.setText(DateUtils.formatShortDate(transactionTime[0]));
        d.btnTransactionDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Chọn ngày giao dịch")
                    .setSelection(DateUtils.toUtcPickerMillis(DateUtils.dateKey(transactionTime[0])))
                    .build();
            picker.addOnPositiveButtonClickListener(selection -> {
                if (selection == null) return;
                Calendar old = Calendar.getInstance();
                old.setTimeInMillis(transactionTime[0]);
                transactionTime[0] = DateUtils.atTime(DateUtils.dateKeyFromUtcPicker(selection),
                        old.get(Calendar.HOUR_OF_DAY), old.get(Calendar.MINUTE));
                d.btnTransactionDate.setText(DateUtils.formatShortDate(transactionTime[0]));
            });
            picker.show(getParentFragmentManager(), "transaction_date");
        });
        d.btnCancelTransaction.setOnClickListener(v -> sheet.dismiss());
        d.btnSaveTransaction.setOnClickListener(v -> {
            double amount = parseDouble(text(d.edtTransactionAmount.getText()));
            if (amount <= 0) {
                d.edtTransactionAmount.setError("Nhập số tiền lớn hơn 0");
                return;
            }
            String category = d.dropdownTransactionCategory.getText().toString().trim();
            String account = accountCode(d.dropdownTransactionAccount.getText().toString());
            String note = text(d.edtTransactionNote.getText());
            TransactionEntity entity = existing == null
                    ? new TransactionEntity(type, amount, category, account, note, transactionTime[0])
                    : existing;
            if (existing != null) {
                entity.amount = amount;
                entity.category = category;
                entity.account = account;
                entity.note = note;
                entity.transactionTime = transactionTime[0];
            }
            vm.save(entity);
            sheet.dismiss();
            toast(existing == null ? "Đã thêm giao dịch" : "Đã cập nhật giao dịch");
        });
        sheet.setOnShowListener(dialog -> {
            FrameLayout bottom = sheet.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottom != null) {
                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottom);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setDraggable(true);
                behavior.setSkipCollapsed(false);
                behavior.setPeekHeight((int) (getResources().getDisplayMetrics().heightPixels * 0.72f));
                bottom.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                bottom.requestLayout();
            }
        });
        sheet.show();
    }

    private void showBudgetDialog() {
        DialogBudgetBinding d = DialogBudgetBinding.inflate(getLayoutInflater());
        double budget = userStore.getMonthlyBudget();
        if (budget > 0) d.edtMonthlyBudget.setText(cleanNumber(budget));
        d.switchFinancialAlert.setChecked(userStore.isFinancialAlertEnabled());
        androidx.appcompat.app.AlertDialog alert = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Ngân sách và cảnh báo tài chính")
                .setMessage("Good Daily sẽ cảnh báo khi chi tiêu tháng đạt 70%, 90% và vượt hạn mức.")
                .setView(d.getRoot())
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", null)
                .create();
        alert.setOnShowListener(ignored -> alert.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    double value = parseDouble(text(d.edtMonthlyBudget.getText()));
                    if (value < 0) {
                        d.edtMonthlyBudget.setError("Số tiền không hợp lệ");
                        return;
                    }
                    userStore.setMonthlyBudget(value);
                    userStore.setFinancialAlertEnabled(d.switchFinancialAlert.isChecked());
                    if (d.switchFinancialAlert.isChecked()) requestNotificationPermissionIfNeeded();
                    renderBudget();
                    alert.dismiss();
                }));
        alert.show();
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private void confirmDelete(TransactionEntity entity) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xóa giao dịch?")
                .setMessage(entity.category + " · " + MoneyUtils.format(entity.amount))
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (d, w) -> {
                    vm.delete(entity);
                    toast("Đã xóa giao dịch");
                }).show();
    }

    private String displayMoney(double value) { return hideAmounts ? "•••••• ₫" : MoneyUtils.format(value); }
    private double n(Double value) { return value == null ? 0 : value; }
    private String text(CharSequence value) { return value == null ? "" : value.toString().trim(); }
    private double parseDouble(String value) {
        try { return Double.parseDouble(value.trim().replace(',', '.')); }
        catch (Exception e) { return 0; }
    }
    private String cleanNumber(double value) {
        return value == Math.rint(value) ? String.valueOf((long) value) : String.valueOf(value);
    }
    private String accountCode(String value) {
        for (FinanceAccountEntity account : accounts) if (account.name.equals(value)) return account.code;
        if (value.contains("Ngân")) return "BANK";
        if (value.contains("Ví")) return "EWALLET";
        return "CASH";
    }
    private String accountName(String code) {
        for (FinanceAccountEntity account : accounts) if (code.equals(account.code)) return account.name;
        if ("BANK".equals(code)) return "Ngân hàng";
        if ("EWALLET".equals(code)) return "Ví điện tử";
        return "Tiền mặt";
    }
    private void toast(String message) { Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show(); }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
