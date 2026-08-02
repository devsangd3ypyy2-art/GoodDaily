package com.sangapp.gooddaily.ui.reminder;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sangapp.gooddaily.data.local.entity.ReminderEntity;
import com.sangapp.gooddaily.data.local.prefs.LocalUserStore;
import com.sangapp.gooddaily.databinding.BottomSheetReminderBinding;
import com.sangapp.gooddaily.databinding.FragmentReminderManagerBinding;
import com.sangapp.gooddaily.util.DateUtils;
import com.sangapp.gooddaily.util.ThemeUtils;
import com.sangapp.gooddaily.viewmodel.ReminderViewModel;

import java.util.Locale;

public class ReminderManagerFragment extends Fragment {
    private FragmentReminderManagerBinding binding;
    private ReminderViewModel vm;

    private final ActivityResultLauncher<String> notificationPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            granted -> Toast.makeText(requireContext(),
                    granted ? "Đã cấp quyền thông báo." : "Bạn có thể cấp quyền thông báo sau trong cài đặt điện thoại.",
                    Toast.LENGTH_SHORT).show()
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentReminderManagerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        vm = new ViewModelProvider(this).get(ReminderViewModel.class);
        String themeKey = new LocalUserStore(requireContext()).getThemeKey();
        ThemeUtils.tintFilledButton(binding.btnAddReminder, requireContext(), themeKey);
        ThemeUtils.tintTonalButton(binding.btnBack, requireContext(), themeKey);
        ReminderAdapter adapter = new ReminderAdapter(new ReminderAdapter.Listener() {
            @Override public void onEdit(ReminderEntity entity) { showEditor(entity); }
            @Override public void onToggle(ReminderEntity entity, boolean enabled) {
                vm.setEnabled(entity, enabled);
                if (enabled) requestNotificationPermissionIfNeeded();
            }
            @Override public void onDelete(ReminderEntity entity) { confirmDelete(entity); }
        });
        binding.recyclerReminders.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerReminders.setAdapter(adapter);

        vm.reminders().observe(getViewLifecycleOwner(), items -> {
            adapter.submitList(items);
            boolean empty = items == null || items.isEmpty();
            binding.tvEmptyReminders.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.recyclerReminders.setVisibility(empty ? View.GONE : View.VISIBLE);
        });
        vm.enabledCount().observe(getViewLifecycleOwner(), count ->
                binding.tvReminderSummary.setText((count == null ? 0 : count) + " nhắc nhở đang bật"));

        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        binding.btnAddReminder.setOnClickListener(v -> showEditor(null));
    }

    private void showEditor(@Nullable ReminderEntity existing) {
        BottomSheetDialog sheet = new BottomSheetDialog(requireContext());
        BottomSheetReminderBinding d = BottomSheetReminderBinding.inflate(getLayoutInflater());
        sheet.setContentView(d.getRoot());
        String themeKey = new LocalUserStore(requireContext()).getThemeKey();
        ThemeUtils.tintFilledButton(d.btnSaveReminder, requireContext(), themeKey);
        ThemeUtils.tintTonalButton(d.btnReminderDate, requireContext(), themeKey);
        ThemeUtils.tintTonalButton(d.btnReminderTime, requireContext(), themeKey);
        ThemeUtils.tintSwitch(d.switchReminderEnabled, requireContext(), themeKey);

        final String[] dateKey = {existing == null ? DateUtils.dateKey() : existing.dateKey};
        final int[] hour = {existing == null ? 20 : existing.hour};
        final int[] minute = {existing == null ? 0 : existing.minute};

        String[] categories = {"Chung", "Công việc", "Học tập", "Sức khỏe", "Tài chính"};
        String[] repeats = {"Một lần", "Hằng ngày", "Hằng tuần"};
        d.dropdownReminderCategory.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, categories));
        d.dropdownRepeatType.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, repeats));

        if (existing != null) {
            d.tvSheetTitle.setText("Sửa nhắc nhở");
            d.edtReminderTitle.setText(existing.title);
            d.edtReminderDescription.setText(existing.description);
            d.dropdownReminderCategory.setText(categoryDisplay(existing.category), false);
            d.dropdownRepeatType.setText(repeatDisplay(existing.repeatType), false);
            d.switchReminderEnabled.setChecked(existing.enabled);
        } else {
            d.dropdownReminderCategory.setText(categories[0], false);
            d.dropdownRepeatType.setText(repeats[0], false);
        }
        renderDateTime(d, dateKey[0], hour[0], minute[0]);

        d.btnReminderDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Chọn ngày nhắc")
                    .setSelection(DateUtils.toUtcPickerMillis(dateKey[0]))
                    .build();
            picker.addOnPositiveButtonClickListener(selection -> {
                if (selection != null) {
                    dateKey[0] = DateUtils.dateKeyFromUtcPicker(selection);
                    renderDateTime(d, dateKey[0], hour[0], minute[0]);
                }
            });
            picker.show(getParentFragmentManager(), "reminder_date");
        });

        d.btnReminderTime.setOnClickListener(v -> new TimePickerDialog(
                requireContext(),
                (picker, selectedHour, selectedMinute) -> {
                    hour[0] = selectedHour;
                    minute[0] = selectedMinute;
                    renderDateTime(d, dateKey[0], hour[0], minute[0]);
                },
                hour[0],
                minute[0],
                true
        ).show());

        d.btnCancelReminder.setOnClickListener(v -> sheet.dismiss());
        d.btnSaveReminder.setOnClickListener(v -> {
            String title = text(d.edtReminderTitle.getText());
            if (title.isEmpty()) {
                d.edtReminderTitle.setError("Nhập tiêu đề nhắc nhở");
                return;
            }
            ReminderEntity entity = existing == null
                    ? new ReminderEntity(title, text(d.edtReminderDescription.getText()), dateKey[0], hour[0], minute[0],
                    repeatCode(d.dropdownRepeatType.getText().toString()), categoryCode(d.dropdownReminderCategory.getText().toString()),
                    d.switchReminderEnabled.isChecked(), System.currentTimeMillis())
                    : existing;
            if (existing != null) {
                entity.title = title;
                entity.description = text(d.edtReminderDescription.getText());
                entity.dateKey = dateKey[0];
                entity.hour = hour[0];
                entity.minute = minute[0];
                entity.repeatType = repeatCode(d.dropdownRepeatType.getText().toString());
                entity.category = categoryCode(d.dropdownReminderCategory.getText().toString());
                entity.enabled = d.switchReminderEnabled.isChecked();
            }
            vm.save(entity);
            if (entity.enabled) requestNotificationPermissionIfNeeded();
            sheet.dismiss();
            Toast.makeText(requireContext(), "Đã lưu nhắc nhở", Toast.LENGTH_SHORT).show();
        });
        sheet.show();
    }

    private void renderDateTime(BottomSheetReminderBinding d, String dateKey, int hour, int minute) {
        d.btnReminderDate.setText(DateUtils.formatCompactDateKey(dateKey));
        d.btnReminderTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
    }

    private void confirmDelete(ReminderEntity entity) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xóa nhắc nhở?")
                .setMessage(entity.title)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> vm.delete(entity))
                .show();
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private String categoryCode(String display) {
        if (display.contains("Công")) return "TASK";
        if (display.contains("Học")) return "STUDY";
        if (display.contains("Sức")) return "HEALTH";
        if (display.contains("Tài")) return "FINANCE";
        return "GENERAL";
    }

    private String categoryDisplay(String code) {
        if ("TASK".equals(code)) return "Công việc";
        if ("STUDY".equals(code)) return "Học tập";
        if ("HEALTH".equals(code)) return "Sức khỏe";
        if ("FINANCE".equals(code)) return "Tài chính";
        return "Chung";
    }

    private String repeatCode(String display) {
        if (display.contains("ngày")) return "DAILY";
        if (display.contains("tuần")) return "WEEKLY";
        return "ONCE";
    }

    private String repeatDisplay(String code) {
        if ("DAILY".equals(code)) return "Hằng ngày";
        if ("WEEKLY".equals(code)) return "Hằng tuần";
        return "Một lần";
    }

    private String text(CharSequence value) { return value == null ? "" : value.toString().trim(); }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
