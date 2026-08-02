package com.sangapp.gooddaily.ui.advanced;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.sangapp.gooddaily.R;
import com.sangapp.gooddaily.data.local.GoodDailyDatabase;
import com.sangapp.gooddaily.data.local.entity.AccountBalance;
import com.sangapp.gooddaily.data.local.entity.FinanceAccountEntity;
import com.sangapp.gooddaily.data.local.entity.FinanceCategoryEntity;
import com.sangapp.gooddaily.data.local.entity.MoneyTransferEntity;
import com.sangapp.gooddaily.data.local.prefs.LocalUserStore;
import com.sangapp.gooddaily.databinding.FragmentLocalAccountManagerBinding;
import com.sangapp.gooddaily.util.AppExecutors;
import com.sangapp.gooddaily.util.MoneyUtils;
import com.sangapp.gooddaily.util.ThemeUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LocalAccountManagerFragment extends Fragment {
    private FragmentLocalAccountManagerBinding binding;
    private List<FinanceAccountEntity> accounts = new ArrayList<>();
    private List<AccountBalance> balances = new ArrayList<>();
    private List<MoneyTransferEntity> transfers = new ArrayList<>();
    private List<FinanceCategoryEntity> incomeCategories = new ArrayList<>();
    private List<FinanceCategoryEntity> expenseCategories = new ArrayList<>();

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLocalAccountManagerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        LocalUserStore store = new LocalUserStore(requireContext());
        ThemeUtils.tintFilledButton(binding.btnAddLocalAccount, requireContext(), store.getThemeKey());
        ThemeUtils.tintTonalButton(binding.btnTransferMoney, requireContext(), store.getThemeKey());
        ThemeUtils.tintTonalButton(binding.btnManageFinanceCategories, requireContext(), store.getThemeKey());

        GoodDailyDatabase db = GoodDailyDatabase.get(requireContext());
        db.transactionDao().observeAccounts().observe(getViewLifecycleOwner(), list -> {
            accounts = list == null ? new ArrayList<>() : new ArrayList<>(list);
            renderAccounts();
        });
        db.transactionDao().observeAccountBalances().observe(getViewLifecycleOwner(), list -> {
            balances = list == null ? new ArrayList<>() : new ArrayList<>(list);
            renderAccounts();
        });
        db.financeAdvancedDao().observeCategories("INCOME").observe(getViewLifecycleOwner(), list -> incomeCategories = list == null ? new ArrayList<>() : new ArrayList<>(list));
        db.financeAdvancedDao().observeCategories("EXPENSE").observe(getViewLifecycleOwner(), list -> expenseCategories = list == null ? new ArrayList<>() : new ArrayList<>(list));
        db.financeAdvancedDao().observeTransfers().observe(getViewLifecycleOwner(), list -> {
            transfers = list == null ? new ArrayList<>() : new ArrayList<>(list);
            renderTransfers();
        });

        binding.btnAddLocalAccount.setOnClickListener(v -> showAccountDialog(null));
        binding.btnTransferMoney.setOnClickListener(v -> showTransferDialog());
        binding.btnManageFinanceCategories.setOnClickListener(v -> showCategoryManager());
    }

    private void renderAccounts() {
        if (binding == null) return;
        binding.accountListContainer.removeAllViews();
        for (FinanceAccountEntity account : accounts) {
            if (!account.active) continue;
            double current = balanceFor(account.code);
            MaterialCardView card = card();
            LinearLayout row = row();
            LinearLayout text = new LinearLayout(requireContext());
            text.setOrientation(LinearLayout.VERTICAL);
            TextView name = label(account.name, 16, true);
            TextView code = label("Mã local: " + account.code, 12, false);
            code.setTextColor(getResources().getColor(R.color.on_surface_variant, requireContext().getTheme()));
            text.addView(name); text.addView(code);
            row.addView(text, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            TextView money = label(MoneyUtils.format(current), 16, true);
            row.addView(money);
            card.addView(row);
            card.setOnClickListener(v -> showAccountDialog(account));
            card.setOnLongClickListener(v -> {
                if ("CASH".equals(account.code)) {
                    toast("Không thể ẩn ví Tiền mặt mặc định.");
                } else {
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Ẩn tài khoản?")
                            .setMessage("Giao dịch cũ vẫn được giữ. Tài khoản sẽ không còn xuất hiện khi nhập giao dịch mới.")
                            .setNegativeButton("Hủy", null)
                            .setPositiveButton("Ẩn", (d, w) -> AppExecutors.io().execute(() -> GoodDailyDatabase.get(requireContext()).transactionDao().deactivateAccount(account.id)))
                            .show();
                }
                return true;
            });
            addCard(binding.accountListContainer, card);
        }
        if (binding.accountListContainer.getChildCount() == 0) binding.accountListContainer.addView(emptyState("Chưa có tài khoản tiền."));
    }

    private void renderTransfers() {
        if (binding == null) return;
        binding.transferListContainer.removeAllViews();
        SimpleDateFormat date = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("vi", "VN"));
        for (MoneyTransferEntity item : transfers) {
            MaterialCardView card = card();
            LinearLayout wrap = vertical();
            wrap.addView(label(accountName(item.fromAccountCode) + " → " + accountName(item.toAccountCode), 15, true));
            wrap.addView(label(MoneyUtils.format(item.amount) + (item.fee > 0 ? " · phí " + MoneyUtils.format(item.fee) : ""), 14, true));
            TextView meta = label(date.format(new Date(item.transferTime)) + (empty(item.note) ? "" : " · " + item.note), 12, false);
            meta.setTextColor(getResources().getColor(R.color.on_surface_variant, requireContext().getTheme()));
            wrap.addView(meta);
            card.addView(wrap);
            card.setOnLongClickListener(v -> {
                new MaterialAlertDialogBuilder(requireContext()).setTitle("Xóa lần chuyển tiền?")
                        .setNegativeButton("Hủy", null)
                        .setPositiveButton("Xóa", (d, w) -> AppExecutors.io().execute(() -> GoodDailyDatabase.get(requireContext()).financeAdvancedDao().deleteTransfer(item))).show();
                return true;
            });
            addCard(binding.transferListContainer, card);
        }
        if (transfers.isEmpty()) binding.transferListContainer.addView(emptyState("Chưa có lần chuyển tiền nào."));
    }

    private void showCategoryManager() {
        String[] groups = {"Danh mục chi", "Danh mục thu"};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Quản lý danh mục")
                .setItems(groups, (dialog, which) -> showCategoryList(which == 0 ? "EXPENSE" : "INCOME"))
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void showCategoryList(String type) {
        List<FinanceCategoryEntity> source = "INCOME".equals(type) ? incomeCategories : expenseCategories;
        List<String> labels = new ArrayList<>();
        for (FinanceCategoryEntity item : source) labels.add(item.name);
        labels.add("＋ Thêm danh mục mới");
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("INCOME".equals(type) ? "Danh mục thu" : "Danh mục chi")
                .setItems(labels.toArray(new String[0]), (dialog, which) -> {
                    if (which == source.size()) showCategoryEditor(type, null);
                    else showCategoryActions(source.get(which));
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void showCategoryActions(FinanceCategoryEntity category) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(category.name)
                .setItems(new String[]{"Đổi tên", "Xóa danh mục"}, (dialog, which) -> {
                    if (which == 0) showCategoryEditor(category.type, category);
                    else new MaterialAlertDialogBuilder(requireContext()).setTitle("Xóa danh mục?")
                            .setMessage("Giao dịch cũ vẫn giữ tên danh mục đã lưu.")
                            .setNegativeButton("Hủy", null)
                            .setPositiveButton("Xóa", (d,w) -> AppExecutors.io().execute(() -> GoodDailyDatabase.get(requireContext()).financeAdvancedDao().deleteCategory(category)))
                            .show();
                }).show();
    }

    private void showCategoryEditor(String type, @Nullable FinanceCategoryEntity existing) {
        TextInputLayout layout = input("Tên danh mục", false);
        TextInputEditText edit = (TextInputEditText) layout.getEditText();
        if (existing != null) edit.setText(existing.name);
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(existing == null ? "Thêm danh mục" : "Đổi tên danh mục")
                .setView(layout).setNegativeButton("Hủy", null).setPositiveButton("Lưu", null).create();
        dialog.setOnShowListener(x -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = text(edit.getText());
            if (name.isEmpty()) { layout.setError("Hãy nhập tên"); return; }
            FinanceCategoryEntity item = existing == null
                    ? new FinanceCategoryEntity(name, type, "INCOME".equals(type) ? "#16A34A" : "#F59E0B", true, System.currentTimeMillis())
                    : existing;
            item.name = name; item.active = true;
            AppExecutors.io().execute(() -> {
                if (existing == null) GoodDailyDatabase.get(requireContext()).financeAdvancedDao().insertCategory(item);
                else GoodDailyDatabase.get(requireContext()).financeAdvancedDao().updateCategory(item);
            });
            dialog.dismiss();
        }));
        dialog.show();
    }

    private void showAccountDialog(@Nullable FinanceAccountEntity existing) {
        LinearLayout form = form();
        TextInputLayout nameLayout = input("Tên ví hoặc tài khoản local", false);
        TextInputLayout balanceLayout = input("Số dư ban đầu", true);
        form.addView(nameLayout); form.addView(balanceLayout);
        TextInputEditText name = (TextInputEditText) nameLayout.getEditText();
        TextInputEditText balance = (TextInputEditText) balanceLayout.getEditText();
        if (existing != null) {
            name.setText(existing.name);
            balance.setText(clean(existing.openingBalance));
        }
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(existing == null ? "Thêm tài khoản tiền" : "Sửa tài khoản tiền")
                .setView(form).setNegativeButton("Hủy", null).setPositiveButton("Lưu", null).create();
        dialog.setOnShowListener(x -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String accountName = text(name.getText());
            if (accountName.isEmpty()) { nameLayout.setError("Hãy nhập tên"); return; }
            double opening = parse(text(balance.getText()));
            long now = System.currentTimeMillis();
            FinanceAccountEntity account = existing == null
                    ? new FinanceAccountEntity("ACC_" + now, accountName, opening, true, now)
                    : existing;
            account.name = accountName;
            account.openingBalance = opening;
            account.active = true;
            AppExecutors.io().execute(() -> {
                if (existing == null) GoodDailyDatabase.get(requireContext()).transactionDao().insertAccount(account);
                else GoodDailyDatabase.get(requireContext()).transactionDao().updateAccount(account);
            });
            dialog.dismiss();
        }));
        dialog.show();
    }

    private void showTransferDialog() {
        List<FinanceAccountEntity> active = new ArrayList<>();
        for (FinanceAccountEntity account : accounts) if (account.active) active.add(account);
        if (active.size() < 2) { toast("Cần ít nhất hai tài khoản tiền để chuyển."); return; }
        LinearLayout form = form();
        TextInputLayout fromLayout = dropdown("Chuyển từ");
        TextInputLayout toLayout = dropdown("Chuyển đến");
        TextInputLayout amountLayout = input("Số tiền", true);
        TextInputLayout feeLayout = input("Phí chuyển (nếu có)", true);
        TextInputLayout noteLayout = input("Ghi chú", false);
        form.addView(fromLayout); form.addView(toLayout); form.addView(amountLayout); form.addView(feeLayout); form.addView(noteLayout);
        List<String> names = new ArrayList<>();
        for (FinanceAccountEntity account : active) names.add(account.name);
        AutoCompleteTextView from = (AutoCompleteTextView) fromLayout.getEditText();
        AutoCompleteTextView to = (AutoCompleteTextView) toLayout.getEditText();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, names);
        from.setAdapter(adapter); to.setAdapter(adapter); from.setText(names.get(0), false); to.setText(names.get(1), false);
        TextInputEditText amount = (TextInputEditText) amountLayout.getEditText();
        TextInputEditText fee = (TextInputEditText) feeLayout.getEditText();
        TextInputEditText note = (TextInputEditText) noteLayout.getEditText();
        final long[] time = {System.currentTimeMillis()};
        Calendar c = Calendar.getInstance();
        TextView dateButton = label("Ngày chuyển: " + new SimpleDateFormat("dd/MM/yyyy", Locale.US).format(c.getTime()), 14, true);
        dateButton.setPadding(dp(12), dp(14), dp(12), dp(14));
        dateButton.setBackgroundColor(getResources().getColor(R.color.surface_variant, requireContext().getTheme()));
        dateButton.setOnClickListener(v -> new DatePickerDialog(requireContext(), (view, year, month, day) -> {
            Calendar selected = Calendar.getInstance(); selected.set(year, month, day);
            time[0] = selected.getTimeInMillis();
            dateButton.setText("Ngày chuyển: " + new SimpleDateFormat("dd/MM/yyyy", Locale.US).format(selected.getTime()));
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show());
        form.addView(dateButton);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Chuyển tiền nội bộ").setView(form).setNegativeButton("Hủy", null).setPositiveButton("Chuyển", null).create();
        dialog.setOnShowListener(x -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            FinanceAccountEntity fromAccount = byName(text(from.getText()));
            FinanceAccountEntity toAccount = byName(text(to.getText()));
            double value = parse(text(amount.getText()));
            double feeValue = parse(text(fee.getText()));
            if (fromAccount == null || toAccount == null || fromAccount.id == toAccount.id) { toast("Hãy chọn hai tài khoản khác nhau."); return; }
            if (value <= 0) { amountLayout.setError("Số tiền phải lớn hơn 0"); return; }
            if (balanceFor(fromAccount.code) < value + feeValue) {
                new MaterialAlertDialogBuilder(requireContext()).setTitle("Số dư không đủ")
                        .setMessage("Bạn vẫn có thể ghi nhận giao dịch nếu số dư thực tế âm.")
                        .setNegativeButton("Hủy", null)
                        .setPositiveButton("Vẫn chuyển", (d, w) -> saveTransfer(fromAccount, toAccount, value, feeValue, text(note.getText()), time[0], dialog)).show();
                return;
            }
            saveTransfer(fromAccount, toAccount, value, feeValue, text(note.getText()), time[0], dialog);
        }));
        dialog.show();
    }

    private void saveTransfer(FinanceAccountEntity from, FinanceAccountEntity to, double amount, double fee, String note, long time, androidx.appcompat.app.AlertDialog dialog) {
        AppExecutors.io().execute(() -> GoodDailyDatabase.get(requireContext()).financeAdvancedDao()
                .insertTransfer(new MoneyTransferEntity(from.code, to.code, amount, fee, note, time)));
        dialog.dismiss(); toast("Đã chuyển tiền nội bộ.");
    }

    private FinanceAccountEntity byName(String name) { for (FinanceAccountEntity a : accounts) if (a.active && name.equals(a.name)) return a; return null; }
    private double balanceFor(String code) { for (AccountBalance b : balances) if (code.equals(b.code)) return b.currentBalance; return 0; }
    private String accountName(String code) { for (FinanceAccountEntity a : accounts) if (code.equals(a.code)) return a.name; return code; }

    private LinearLayout form() { LinearLayout l = vertical(); l.setPadding(dp(4), dp(4), dp(4), 0); return l; }
    private TextInputLayout input(String hint, boolean numeric) {
        TextInputLayout layout = new TextInputLayout(requireContext()); layout.setHint(hint); layout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        TextInputEditText edit = new TextInputEditText(requireContext()); edit.setInputType(numeric ? android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL : android.text.InputType.TYPE_CLASS_TEXT);
        layout.addView(edit); LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); p.bottomMargin = dp(10); layout.setLayoutParams(p); return layout;
    }
    private TextInputLayout dropdown(String hint) {
        TextInputLayout layout = new TextInputLayout(requireContext()); layout.setHint(hint); layout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE); layout.setEndIconMode(TextInputLayout.END_ICON_DROPDOWN_MENU);
        AutoCompleteTextView view = new AutoCompleteTextView(requireContext()); view.setInputType(0); view.setPadding(dp(14), dp(14), dp(14), dp(14)); layout.addView(view);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); p.bottomMargin = dp(10); layout.setLayoutParams(p); return layout;
    }
    private MaterialCardView card() { MaterialCardView c = new MaterialCardView(requireContext()); c.setRadius(dp(18)); c.setCardElevation(0); c.setStrokeWidth(dp(1)); c.setStrokeColor(getResources().getColor(R.color.outline, requireContext().getTheme())); c.setCardBackgroundColor(getResources().getColor(R.color.surface, requireContext().getTheme())); return c; }
    private LinearLayout row() { LinearLayout l = new LinearLayout(requireContext()); l.setOrientation(LinearLayout.HORIZONTAL); l.setGravity(Gravity.CENTER_VERTICAL); l.setPadding(dp(15), dp(15), dp(15), dp(15)); return l; }
    private LinearLayout vertical() { LinearLayout l = new LinearLayout(requireContext()); l.setOrientation(LinearLayout.VERTICAL); l.setPadding(dp(15), dp(15), dp(15), dp(15)); return l; }
    private TextView label(String text, int sp, boolean bold) { TextView v = new TextView(requireContext()); v.setText(text); v.setTextSize(sp); v.setTextColor(getResources().getColor(R.color.on_surface, requireContext().getTheme())); if (bold) v.setTypeface(v.getTypeface(), android.graphics.Typeface.BOLD); return v; }
    private TextView emptyState(String text) { TextView v = label(text, 14, false); v.setGravity(Gravity.CENTER); v.setTextColor(getResources().getColor(R.color.on_surface_variant, requireContext().getTheme())); v.setPadding(dp(20), dp(26), dp(20), dp(26)); return v; }
    private void addCard(LinearLayout container, View card) { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); p.bottomMargin = dp(10); container.addView(card, p); }
    private String text(CharSequence s) { return s == null ? "" : s.toString().trim(); }
    private boolean empty(String s) { return s == null || s.trim().isEmpty(); }
    private double parse(String s) { try { return Double.parseDouble(s.replace(',', '.')); } catch (Exception e) { return 0; } }
    private String clean(double v) { return Math.abs(v - Math.rint(v)) < .001 ? String.format(Locale.US, "%.0f", v) : String.format(Locale.US, "%.1f", v); }
    private int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }
    private void toast(String m) { Toast.makeText(requireContext(), m, Toast.LENGTH_LONG).show(); }
    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
