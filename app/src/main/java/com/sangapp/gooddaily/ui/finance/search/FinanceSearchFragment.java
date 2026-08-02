package com.sangapp.gooddaily.ui.finance.search;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sangapp.gooddaily.data.local.GoodDailyDatabase;
import com.sangapp.gooddaily.data.local.entity.FinanceAccountEntity;
import com.sangapp.gooddaily.data.local.entity.FinanceCategoryEntity;
import com.sangapp.gooddaily.data.local.entity.TransactionEntity;
import com.sangapp.gooddaily.databinding.FragmentFinanceSearchBinding;
import com.sangapp.gooddaily.ui.adapter.TransactionAdapter;
import com.sangapp.gooddaily.util.DateUtils;
import com.sangapp.gooddaily.util.MoneyUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FinanceSearchFragment extends Fragment {
    private FragmentFinanceSearchBinding binding;
    private GoodDailyDatabase db;
    private TransactionAdapter adapter;
    private LiveData<List<TransactionEntity>> activeQuery;
    private long start = DateUtils.startOfMonth();
    private long end = DateUtils.endOfMonth(System.currentTimeMillis());
    private String accountCode = "ALL";
    private String category = "ALL";
    private final Map<String, String> accountNameToCode = new LinkedHashMap<>();

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
                                                   @Nullable ViewGroup container,
                                                   @Nullable Bundle savedInstanceState) {
        binding = FragmentFinanceSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        db = GoodDailyDatabase.get(requireContext());
        adapter = new TransactionAdapter(new TransactionAdapter.Listener() {
            @Override public void onClick(TransactionEntity entity) {
                new MaterialAlertDialogBuilder(requireContext()).setTitle(entity.category)
                        .setMessage((entity.note == null || entity.note.isEmpty() ? "Không có ghi chú" : entity.note)
                                + "\n\n" + MoneyUtils.format(entity.amount) + " · " + DateUtils.formatDateTime(entity.transactionTime))
                        .setPositiveButton("Đóng", null).show();
            }
            @Override public void onLongClick(TransactionEntity entity) {
                new MaterialAlertDialogBuilder(requireContext()).setTitle("Xóa giao dịch?")
                        .setNegativeButton("Hủy", null)
                        .setPositiveButton("Xóa", (d, w) ->
                                com.sangapp.gooddaily.util.AppExecutors.database().execute(() -> db.transactionDao().delete(entity)))
                        .show();
            }
        });
        binding.recyclerResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerResults.setAdapter(adapter);
        binding.edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { applyFilter(); }
            @Override public void afterTextChanged(Editable s) {}
        });
        binding.btnStartDate.setOnClickListener(v -> pickDate(true));
        binding.btnEndDate.setOnClickListener(v -> pickDate(false));

        db.transactionDao().observeAccounts().observe(getViewLifecycleOwner(), this::setupAccounts);
        db.financeAdvancedDao().observeCategories("EXPENSE").observe(getViewLifecycleOwner(), x -> setupCategories());
        db.financeAdvancedDao().observeCategories("INCOME").observe(getViewLifecycleOwner(), x -> setupCategories());
        updateDateLabels();
        applyFilter();
    }

    private void setupAccounts(List<FinanceAccountEntity> accounts) {
        accountNameToCode.clear();
        accountNameToCode.put("Tất cả tài khoản", "ALL");
        Map<String, String> adapterNames = new HashMap<>();
        if (accounts != null) for (FinanceAccountEntity account : accounts) {
            if (!account.active) continue;
            accountNameToCode.put(account.name, account.code);
            adapterNames.put(account.code, account.name);
        }
        adapter.setAccountNames(adapterNames);
        ArrayAdapter<String> dropdown = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>(accountNameToCode.keySet()));
        binding.dropdownAccount.setAdapter(dropdown);
        if (binding.dropdownAccount.getText().toString().isEmpty()) binding.dropdownAccount.setText("Tất cả tài khoản", false);
        binding.dropdownAccount.setOnItemClickListener((p, v, position, id) -> {
            accountCode = accountNameToCode.getOrDefault(binding.dropdownAccount.getText().toString(), "ALL");
            applyFilter();
        });
    }

    private void setupCategories() {
        com.sangapp.gooddaily.util.AppExecutors.database().execute(() -> {
            List<FinanceCategoryEntity> items = db.financeAdvancedDao().getCategoriesSync();
            ArrayList<String> names = new ArrayList<>();
            names.add("Tất cả danh mục");
            if (items != null) for (FinanceCategoryEntity item : items) if (item.active && !names.contains(item.name)) names.add(item.name);
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                binding.dropdownCategory.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, names));
                if (binding.dropdownCategory.getText().toString().isEmpty()) binding.dropdownCategory.setText("Tất cả danh mục", false);
                binding.dropdownCategory.setOnItemClickListener((p, v, pos, id) -> {
                    String value = binding.dropdownCategory.getText().toString();
                    category = "Tất cả danh mục".equals(value) ? "ALL" : value;
                    applyFilter();
                });
            });
        });
    }

    private void applyFilter() {
        if (binding == null || db == null) return;
        if (activeQuery != null) activeQuery.removeObservers(getViewLifecycleOwner());
        String query = binding.edtSearch.getText() == null ? "" : binding.edtSearch.getText().toString().trim();
        activeQuery = db.transactionDao().searchFiltered(query, accountCode, category, start, end);
        activeQuery.observe(getViewLifecycleOwner(), items -> {
            List<TransactionEntity> safe = items == null ? new ArrayList<>() : items;
            adapter.submitList(new ArrayList<>(safe));
            binding.tvEmpty.setVisibility(safe.isEmpty() ? View.VISIBLE : View.GONE);
            double income = 0, expense = 0;
            for (TransactionEntity item : safe) {
                if ("INCOME".equals(item.type)) income += item.amount; else expense += item.amount;
            }
            binding.tvResultSummary.setText(String.format(Locale.getDefault(),
                    "%d giao dịch · Thu %s · Chi %s · Chênh lệch %s",
                    safe.size(), MoneyUtils.format(income), MoneyUtils.format(expense), MoneyUtils.format(income - expense)));
        });
    }

    private void pickDate(boolean startDate) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(startDate ? start : end);
        new DatePickerDialog(requireContext(), (view, year, month, day) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, day, 0, 0, 0);
            selected.set(Calendar.MILLISECOND, 0);
            if (startDate) start = selected.getTimeInMillis();
            else end = DateUtils.endOfDay(selected.getTimeInMillis());
            if (start > end) {
                Toast.makeText(requireContext(), "Ngày bắt đầu phải trước ngày kết thúc.", Toast.LENGTH_LONG).show();
                long tmp = start; start = DateUtils.startOfDay(end); end = DateUtils.endOfDay(tmp);
            }
            updateDateLabels();
            applyFilter();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateLabels() {
        binding.btnStartDate.setText("Từ " + DateUtils.formatShortDate(start));
        binding.btnEndDate.setText("Đến " + DateUtils.formatShortDate(end));
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
