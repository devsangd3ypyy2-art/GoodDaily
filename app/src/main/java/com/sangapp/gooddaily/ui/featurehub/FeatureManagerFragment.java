package com.sangapp.gooddaily.ui.featurehub;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.sangapp.gooddaily.data.local.GoodDailyDatabase;
import com.sangapp.gooddaily.data.backup.PersonalRecordsPdfExporter;
import com.sangapp.gooddaily.data.local.entity.PersonalRecordEntity;
import com.sangapp.gooddaily.data.local.prefs.LocalUserStore;
import com.sangapp.gooddaily.databinding.BottomSheetPersonalRecordBinding;
import com.sangapp.gooddaily.databinding.FragmentFeatureManagerBinding;
import com.sangapp.gooddaily.feature.FeatureCatalog;
import com.sangapp.gooddaily.feature.FeatureDefinition;
import com.sangapp.gooddaily.notification.AdvancedReminderScheduler;
import com.sangapp.gooddaily.ui.auth.PinLockActivity;
import com.sangapp.gooddaily.util.AppExecutors;
import com.sangapp.gooddaily.util.AttachmentStore;
import com.sangapp.gooddaily.util.DateUtils;
import com.sangapp.gooddaily.util.SecuritySession;
import com.sangapp.gooddaily.util.ThemeUtils;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FeatureManagerFragment extends Fragment {
    public static final String ARG_FEATURE = "feature";

    private FragmentFeatureManagerBinding binding;
    private FeatureDefinition definition;
    private PersonalRecordAdapter adapter;
    private List<PersonalRecordEntity> source = new ArrayList<>();
    private String searchQuery = "";
    private BottomSheetPersonalRecordBinding activeForm;
    private String pendingAttachment = "";

    private final ActivityResultLauncher<String[]> pickAttachment = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null || activeForm == null) return;
                try {
                    if (definition != null && FeatureCatalog.ADVANCED_REMINDER.equals(definition.feature)) {
                        try {
                            requireContext().getContentResolver().takePersistableUriPermission(uri,
                                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (Exception ignored) {}
                        pendingAttachment = uri.toString();
                    } else {
                        pendingAttachment = AttachmentStore.copyToInternal(requireContext(), uri);
                    }
                    activeForm.btnPickRecordAttachment.setText("Đã chọn: " + AttachmentStore.displayName(requireContext(), uri));
                } catch (Exception e) {
                    toast("Không thể lưu file: " + e.getMessage());
                }
            });

    private final ActivityResultLauncher<String> createCsv = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("text/csv"), uri -> {
                if (uri != null) exportCsv(uri);
            });

    private final ActivityResultLauncher<String> createPdf = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("application/pdf"), uri -> {
                if (uri != null) exportPdf(uri);
            });

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFeatureManagerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        String feature = getArguments() == null ? "" : getArguments().getString(ARG_FEATURE, "");
        definition = FeatureCatalog.get(feature);
        if (definition == null) {
            new MaterialAlertDialogBuilder(requireContext()).setTitle("Không tìm thấy tính năng")
                    .setMessage("Mã tính năng không hợp lệ.").setPositiveButton("Đóng", (d, w) -> requireActivity().onBackPressed()).show();
            return;
        }
        if ((FeatureCatalog.JOURNAL_ENTRY.equals(definition.feature) || FeatureCatalog.DIVINATION_ENTRY.equals(definition.feature))
                && SecuritySession.shouldLockModule(requireContext(), SecuritySession.MODULE_JOURNAL)) {
            Intent intent = new Intent(requireContext(), PinLockActivity.class);
            intent.putExtra(PinLockActivity.EXTRA_MODULE, SecuritySession.MODULE_JOURNAL);
            startActivity(intent);
        }

        binding.tvFeatureTitle.setText(definition.title);
        binding.tvFeatureDescription.setText(definition.description);
        LocalUserStore store = new LocalUserStore(requireContext());
        ThemeUtils.tintFilledButton(binding.btnAddFeatureRecord, requireContext(), store.getThemeKey());
        ThemeUtils.tintTonalButton(binding.btnExportFeatureCsv, requireContext(), store.getThemeKey());
        boolean pdfEnabled = FeatureCatalog.JOURNAL_ENTRY.equals(definition.feature)
                || FeatureCatalog.DIVINATION_ENTRY.equals(definition.feature);
        binding.btnExportFeaturePdf.setVisibility(pdfEnabled ? View.VISIBLE : View.GONE);
        if (pdfEnabled) ThemeUtils.tintTonalButton(binding.btnExportFeaturePdf, requireContext(), store.getThemeKey());

        adapter = new PersonalRecordAdapter(definition, new PersonalRecordAdapter.Listener() {
            @Override public void onClick(PersonalRecordEntity item) { showRecordForm(item); }
            @Override public void onLongClick(PersonalRecordEntity item) { deleteWithUndo(item); }
            @Override public void onFavorite(PersonalRecordEntity item) {
                item.favorite = !item.favorite;
                item.updatedAt = System.currentTimeMillis();
                AppExecutors.io().execute(() -> GoodDailyDatabase.get(requireContext()).advancedRecordDao().update(item));
            }
        });
        binding.recyclerFeatureRecords.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerFeatureRecords.setAdapter(adapter);
        binding.recyclerFeatureRecords.setNestedScrollingEnabled(false);

        GoodDailyDatabase.get(requireContext()).advancedRecordDao().observeFeature(definition.feature)
                .observe(getViewLifecycleOwner(), records -> {
                    source = records == null ? new ArrayList<>() : new ArrayList<>(records);
                    filterAndRender();
                });

        binding.edtFeatureSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s == null ? "" : s.toString().trim().toLowerCase(Locale.ROOT);
                filterAndRender();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        binding.btnAddFeatureRecord.setOnClickListener(v -> showRecordForm(null));
        binding.btnExportFeatureCsv.setOnClickListener(v -> createCsv.launch("GoodDaily_" + definition.feature + ".csv"));
        binding.btnExportFeaturePdf.setOnClickListener(v -> createPdf.launch("GoodDaily_" + definition.feature + "_" + DateUtils.dateKey() + ".pdf"));
    }

    @Override public void onResume() {
        super.onResume();
        if (definition != null && (FeatureCatalog.JOURNAL_ENTRY.equals(definition.feature)
                || FeatureCatalog.DIVINATION_ENTRY.equals(definition.feature))
                && SecuritySession.shouldLockModule(requireContext(), SecuritySession.MODULE_JOURNAL)) {
            Intent intent = new Intent(requireContext(), PinLockActivity.class);
            intent.putExtra(PinLockActivity.EXTRA_MODULE, SecuritySession.MODULE_JOURNAL);
            startActivity(intent);
        }
    }

    private void filterAndRender() {
        if (binding == null || adapter == null) return;
        List<PersonalRecordEntity> filtered = new ArrayList<>();
        double sum = 0;
        for (PersonalRecordEntity item : source) {
            String haystack = safe(item.title) + " " + safe(item.details) + " " + safe(item.tags) + " " + safe(item.status);
            if (searchQuery.isEmpty() || haystack.toLowerCase(Locale.ROOT).contains(searchQuery)) {
                filtered.add(item);
                sum += item.numericValue;
            }
        }
        adapter.submitList(filtered);
        binding.tvFeatureEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        binding.recyclerFeatureRecords.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
        String sumText = Math.abs(sum) < 0.001 ? "" : " · Tổng: " + format(sum) + (empty(definition.valueSuffix) ? "" : " " + definition.valueSuffix);
        binding.tvFeatureSummary.setText(filtered.size() + " mục đang hiển thị" + sumText);
    }

    private void showRecordForm(@Nullable PersonalRecordEntity existing) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        BottomSheetPersonalRecordBinding form = BottomSheetPersonalRecordBinding.inflate(getLayoutInflater());
        activeForm = form;
        pendingAttachment = existing == null ? "" : safe(existing.attachmentUri);
        dialog.setContentView(form.getRoot());
        dialog.setOnDismissListener(x -> activeForm = null);
        dialog.setOnShowListener(x -> {
            View bottom = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottom != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottom);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
            }
        });

        form.tvRecordFormTitle.setText(existing == null ? "Thêm " + definition.title : "Sửa " + definition.title);
        form.layoutRecordTitle.setHint(definition.titleHint);
        form.layoutRecordDetails.setHint(definition.detailsHint);
        form.layoutRecordValue.setHint(definition.valueHint);
        form.layoutRecordSecondary.setHint(definition.secondaryHint);
        form.layoutRecordCount.setHint(definition.countHint);
        form.layoutRecordStatus.setHint(definition.statusHint);
        form.timeRow.setVisibility(definition.showTime ? View.VISIBLE : View.GONE);
        form.btnPickRecordAttachment.setVisibility(definition.allowAttachment ? View.VISIBLE : View.GONE);
        if (FeatureCatalog.ADVANCED_REMINDER.equals(definition.feature)) {
            form.layoutRecordTags.setHint("Nhóm / ngày lặp / rung");
            form.layoutRecordTags.setHelperText("Ví dụ: học tập, T2,T4,T6. Ghi ‘không rung’ để tắt rung.");
            form.btnPickRecordAttachment.setText("Chọn âm thanh riêng cho nhắc nhở");
        }
        ThemeUtils.tintFilledButton(form.btnSavePersonalRecord, requireContext(), new LocalUserStore(requireContext()).getThemeKey());

        final String[] dateKey = {existing == null || empty(existing.dateKey) ? DateUtils.dateKey() : existing.dateKey};
        final int[] startMinutes = {existing == null ? 8 * 60 : existing.startMinutes};
        final int[] endMinutes = {existing == null ? 9 * 60 : existing.endMinutes};
        updateDateButton(form, dateKey[0]);
        updateTimeButtons(form, startMinutes[0], endMinutes[0]);

        if (existing != null) {
            form.edtRecordTitle.setText(existing.title);
            form.edtRecordDetails.setText(existing.details);
            form.edtRecordValue.setText(number(existing.numericValue));
            form.edtRecordSecondary.setText(number(existing.secondaryValue));
            form.edtRecordCount.setText(existing.countValue == 0 ? "" : String.valueOf(existing.countValue));
            form.edtRecordStatus.setText(existing.status);
            form.edtRecordTags.setText(existing.tags);
            form.checkRecordFavorite.setChecked(existing.favorite);
            if (!empty(existing.attachmentUri)) form.btnPickRecordAttachment.setText("Đã có file đính kèm · chạm để thay");
        }

        form.btnRecordDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Chọn ngày")
                    .setSelection(DateUtils.toUtcPickerMillis(dateKey[0]))
                    .build();
            picker.addOnPositiveButtonClickListener(selection -> {
                if (selection == null) return;
                dateKey[0] = DateUtils.dateKeyFromUtcPicker(selection);
                updateDateButton(form, dateKey[0]);
            });
            picker.show(getParentFragmentManager(), "record_date");
        });
        form.btnRecordStartTime.setOnClickListener(v -> showTimePicker(startMinutes[0], value -> {
            startMinutes[0] = value; updateTimeButtons(form, startMinutes[0], endMinutes[0]);
        }));
        form.btnRecordEndTime.setOnClickListener(v -> showTimePicker(endMinutes[0], value -> {
            endMinutes[0] = value; updateTimeButtons(form, startMinutes[0], endMinutes[0]);
        }));
        form.btnPickRecordAttachment.setOnClickListener(v -> pickAttachment.launch(new String[]{"image/*", "application/pdf", "text/plain", "audio/*"}));
        form.btnSavePersonalRecord.setOnClickListener(v -> {
            String title = text(form.edtRecordTitle.getText());
            if (title.isEmpty()) {
                form.layoutRecordTitle.setError("Hãy nhập tiêu đề");
                return;
            }
            long now = System.currentTimeMillis();
            PersonalRecordEntity item = existing == null
                    ? new PersonalRecordEntity(definition.module, definition.feature, title,
                    text(form.edtRecordDetails.getText()), parseDouble(text(form.edtRecordValue.getText())),
                    parseDouble(text(form.edtRecordSecondary.getText())), parseInt(text(form.edtRecordCount.getText())),
                    dateKey[0], startMinutes[0], endMinutes[0], text(form.edtRecordStatus.getText()),
                    text(form.edtRecordTags.getText()), pendingAttachment, form.checkRecordFavorite.isChecked(),
                    false, now, now)
                    : existing;
            if (existing != null) {
                item.title = title;
                item.details = text(form.edtRecordDetails.getText());
                item.numericValue = parseDouble(text(form.edtRecordValue.getText()));
                item.secondaryValue = parseDouble(text(form.edtRecordSecondary.getText()));
                item.countValue = parseInt(text(form.edtRecordCount.getText()));
                item.dateKey = dateKey[0];
                item.startMinutes = startMinutes[0];
                item.endMinutes = endMinutes[0];
                item.status = text(form.edtRecordStatus.getText());
                item.tags = text(form.edtRecordTags.getText());
                item.attachmentUri = pendingAttachment;
                item.favorite = form.checkRecordFavorite.isChecked();
                item.updatedAt = now;
            }
            AppExecutors.io().execute(() -> {
                if (existing == null) item.id = GoodDailyDatabase.get(requireContext()).advancedRecordDao().insert(item);
                else GoodDailyDatabase.get(requireContext()).advancedRecordDao().update(item);
                if (FeatureCatalog.ADVANCED_REMINDER.equals(definition.feature)) AdvancedReminderScheduler.schedule(requireContext(), item);
            });
            dialog.dismiss();
            toast(existing == null ? "Đã thêm dữ liệu." : "Đã cập nhật dữ liệu.");
        });
        dialog.show();
    }

    private void deleteWithUndo(PersonalRecordEntity item) {
        if (FeatureCatalog.ADVANCED_REMINDER.equals(definition.feature)) AdvancedReminderScheduler.cancel(requireContext(), item.id);
        AppExecutors.io().execute(() -> GoodDailyDatabase.get(requireContext()).advancedRecordDao().delete(item));
        Snackbar.make(binding.getRoot(), "Đã xóa “" + safe(item.title) + "”", Snackbar.LENGTH_LONG)
                .setAction("Hoàn tác", v -> AppExecutors.io().execute(() -> {
                    item.id = 0;
                    GoodDailyDatabase.get(requireContext()).advancedRecordDao().insert(item);
                })).show();
    }

    private void exportCsv(Uri uri) {
        List<PersonalRecordEntity> snapshot = new ArrayList<>(source);
        AppExecutors.io().execute(() -> {
            try (OutputStream out = requireContext().getContentResolver().openOutputStream(uri)) {
                if (out == null) throw new IllegalStateException("Không mở được file");
                StringBuilder csv = new StringBuilder("id,title,details,value,secondary,count,date,start,end,status,tags,attachment\n");
                for (PersonalRecordEntity item : snapshot) {
                    csv.append(item.id).append(',').append(q(item.title)).append(',').append(q(item.details)).append(',')
                            .append(item.numericValue).append(',').append(item.secondaryValue).append(',').append(item.countValue).append(',')
                            .append(q(item.dateKey)).append(',').append(item.startMinutes).append(',').append(item.endMinutes).append(',')
                            .append(q(item.status)).append(',').append(q(item.tags)).append(',').append(q(item.attachmentUri)).append('\n');
                }
                out.write(csv.toString().getBytes(StandardCharsets.UTF_8));
                requireActivity().runOnUiThread(() -> toast("Đã xuất file CSV."));
            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> toast("Xuất CSV thất bại: " + e.getMessage()));
            }
        });
    }

    private void exportPdf(Uri uri) {
        List<PersonalRecordEntity> snapshot = new ArrayList<>(source);
        AppExecutors.io().execute(() -> {
            try {
                PersonalRecordsPdfExporter.export(requireContext(), uri, definition, snapshot);
                requireActivity().runOnUiThread(() -> toast("Đã xuất file PDF."));
            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> toast("Xuất PDF thất bại: " + e.getMessage()));
            }
        });
    }

    private void showTimePicker(int current, IntCallback callback) {
        new TimePickerDialog(requireContext(), (view, hour, minute) -> callback.accept(hour * 60 + minute),
                current / 60, current % 60, true).show();
    }

    private void updateDateButton(BottomSheetPersonalRecordBinding form, String dateKey) { form.btnRecordDate.setText("Ngày " + dateKey); }
    private void updateTimeButtons(BottomSheetPersonalRecordBinding form, int start, int end) {
        form.btnRecordStartTime.setText("Từ " + time(start));
        form.btnRecordEndTime.setText("Đến " + time(end));
    }
    private String time(int minutes) { return String.format(Locale.US, "%02d:%02d", minutes / 60, minutes % 60); }
    private String text(CharSequence value) { return value == null ? "" : value.toString().trim(); }
    private String safe(String value) { return value == null ? "" : value; }
    private boolean empty(String value) { return value == null || value.trim().isEmpty(); }
    private double parseDouble(String value) { try { return Double.parseDouble(value.replace(',', '.')); } catch (Exception e) { return 0; } }
    private int parseInt(String value) { try { return Integer.parseInt(value); } catch (Exception e) { return 0; } }
    private String number(double value) { return value == 0 ? "" : format(value); }
    private String format(double value) { return Math.abs(value - Math.rint(value)) < 0.001 ? String.format(Locale.US, "%.0f", value) : String.format(Locale.US, "%.1f", value); }
    private String q(String value) { return '"' + safe(value).replace("\"", "\"\"") + '"'; }
    private void toast(String message) { Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show(); }
    private interface IntCallback { void accept(int value); }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; activeForm = null; }
}
