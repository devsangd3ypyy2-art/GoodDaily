package com.sangapp.gooddaily.ui.profile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sangapp.gooddaily.R;
import com.sangapp.gooddaily.data.backup.BackupManager;
import com.sangapp.gooddaily.data.backup.SampleDataSeeder;
import com.sangapp.gooddaily.data.local.prefs.LocalUserStore;
import com.sangapp.gooddaily.databinding.DialogChangePasswordBinding;
import com.sangapp.gooddaily.databinding.FragmentProfileBinding;
import com.sangapp.gooddaily.notification.ReminderScheduler;
import com.sangapp.gooddaily.ui.auth.AuthActivity;
import com.sangapp.gooddaily.util.AppearanceUtils;
import com.sangapp.gooddaily.util.ThemeUtils;
import com.sangapp.gooddaily.viewmodel.ReminderViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private LocalUserStore userStore;
    private BackupManager backupManager;

    private final ActivityResultLauncher<String> createBackup = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("application/json"),
            uri -> { if (uri != null && backupManager != null) backupManager.exportTo(uri, callback()); }
    );

    private final ActivityResultLauncher<String[]> openBackup = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> { if (uri != null) confirmImport(uri); }
    );

    private final ActivityResultLauncher<String[]> pickAvatar = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri == null || userStore == null) return;
                try {
                    requireContext().getContentResolver().takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Exception ignored) {
                }
                userStore.setAvatarUri(uri.toString());
                loadAvatar();
                toast("Đã đổi ảnh đại diện.");
            }
    );

    private final ActivityResultLauncher<String> notificationPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            granted -> {
                if (granted) {
                    userStore.setReminderEnabled(true);
                    ReminderScheduler.schedule(requireContext());
                    toast("Đã bật quyền thông báo.");
                } else if (binding != null) {
                    binding.switchReminder.setChecked(false);
                    toast("Bạn chưa cấp quyền thông báo.");
                }
            }
    );

    private final ActivityResultLauncher<String> generalNotificationPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            granted -> toast(granted ? "Đã cấp quyền thông báo." : "Bạn chưa cấp quyền thông báo.")
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        userStore = new LocalUserStore(requireContext());
        backupManager = new BackupManager(requireContext());
        ReminderViewModel reminderVm = new ViewModelProvider(this).get(ReminderViewModel.class);

        binding.tvDisplayName.setText(userStore.getDisplayName());
        binding.tvUsername.setText("@" + userStore.getUsername());
        loadAvatar();
        binding.switchReminder.setChecked(userStore.isReminderEnabled());
        binding.switchFinancialAlert.setChecked(userStore.isFinancialAlertEnabled());
        binding.switchHideAmounts.setChecked(userStore.isHideAmountsEnabled());
        binding.switchDynamicColors.setChecked(userStore.isDynamicColorsEnabled());
        binding.tvNotificationToneSummary.setText("Âm báo: " + userStore.getNotificationSoundName());

        applyAppearance();
        setupThemeSelection();
        setupDisplayMode();

        reminderVm.enabledCount().observe(getViewLifecycleOwner(), count ->
                binding.tvCustomReminderCount.setText((count == null ? 0 : count) + " nhắc nhở đang bật"));

        binding.switchReminder.setOnCheckedChangeListener((button, checked) -> {
            if (checked) enableReminder();
            else {
                userStore.setReminderEnabled(false);
                ReminderScheduler.cancel(requireContext());
            }
        });
        binding.switchFinancialAlert.setOnCheckedChangeListener((button, checked) -> {
            userStore.setFinancialAlertEnabled(checked);
            if (checked) requestNotificationPermissionOnly();
        });
        binding.switchHideAmounts.setOnCheckedChangeListener((button, checked) -> {
            userStore.setHideAmountsEnabled(checked);
            toast(checked ? "Đã ẩn số tiền trên Tổng quan và Tài chính." : "Đã hiện lại số tiền.");
        });
        binding.switchDynamicColors.setOnCheckedChangeListener((button, checked) -> {
            if (checked == userStore.isDynamicColorsEnabled()) return;
            userStore.setDynamicColorsEnabled(checked);
            toast(checked ? "Đã bật màu Material You." : "Đã quay lại bảng màu Good Daily.");
            requireActivity().recreate();
        });

        View.OnClickListener openReminders = v -> Navigation.findNavController(v).navigate(R.id.reminderManagerFragment);
        binding.rowCustomReminders.setOnClickListener(openReminders);
        binding.btnNotificationSettings.setOnClickListener(openReminders);
        binding.cardMusicCenter.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.musicFragment));
        binding.btnOpenFeatureHub.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.featureHubFragment));
        binding.btnOpenFullBackup.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.backupCenterFragment));
        binding.btnOpenSecurity.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.securityCenterFragment));
        ThemeUtils.tintTonalButton(binding.btnOpenFeatureHub, requireContext(), userStore.getThemeKey());

        binding.cardExport.setOnClickListener(v -> {
            String date = new SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.US).format(new Date());
            createBackup.launch("GoodDaily_Backup_" + date + ".json");
        });
        binding.cardImport.setOnClickListener(v -> openBackup.launch(new String[]{"application/json", "text/plain"}));
        binding.cardSampleData.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Tạo dữ liệu mẫu?")
                .setMessage("Dữ liệu mẫu sẽ được thêm vào dữ liệu hiện có để bạn xem biểu đồ và giao diện.")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Tạo", (d, w) -> SampleDataSeeder.seed(requireContext(), () -> toast("Đã tạo dữ liệu mẫu.")))
                .show());
        binding.avatarBadge.setOnClickListener(v -> pickAvatar.launch(new String[]{"image/*"}));
        binding.tvChangeAvatar.setOnClickListener(v -> pickAvatar.launch(new String[]{"image/*"}));
        binding.btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        binding.btnLogout.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Đăng xuất?")
                .setMessage("Dữ liệu vẫn được giữ nguyên trên điện thoại.")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Đăng xuất", (d, w) -> logout())
                .show());
    }

    private void loadAvatar() {
        if (binding == null || userStore == null) return;
        String value = userStore.getAvatarUri();
        if (value == null || value.trim().isEmpty()) {
            binding.imgAvatar.setImageResource(R.drawable.ic_app_logo);
            binding.imgAvatar.setPadding(dp(16), dp(16), dp(16), dp(16));
            binding.imgAvatar.setScaleType(android.widget.ImageView.ScaleType.CENTER_INSIDE);
            binding.imgAvatar.setColorFilter(ThemeUtils.getPrimaryColor(requireContext(), userStore.getThemeKey()));
            return;
        }
        try {
            binding.imgAvatar.clearColorFilter();
            binding.imgAvatar.setPadding(0, 0, 0, 0);
            binding.imgAvatar.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
            binding.imgAvatar.setImageURI(Uri.parse(value));
        } catch (Exception e) {
            userStore.setAvatarUri("");
            binding.imgAvatar.setImageResource(R.drawable.ic_app_logo);
            binding.imgAvatar.setPadding(dp(16), dp(16), dp(16), dp(16));
            binding.imgAvatar.setColorFilter(ThemeUtils.getPrimaryColor(requireContext(), userStore.getThemeKey()));
        }
    }

    private void setupThemeSelection() {
        binding.themeGreen.setOnClickListener(v -> saveTheme(ThemeUtils.THEME_GREEN));
        binding.themeBlue.setOnClickListener(v -> saveTheme(ThemeUtils.THEME_BLUE));
        binding.themeOrange.setOnClickListener(v -> saveTheme(ThemeUtils.THEME_ORANGE));
        binding.themePurple.setOnClickListener(v -> saveTheme(ThemeUtils.THEME_PURPLE));
        updateThemeSelection(userStore.getThemeKey());
    }

    private void setupDisplayMode() {
        String mode = userStore.getAppearanceMode();
        int checkedId = R.id.btnModeSystem;
        if (AppearanceUtils.MODE_LIGHT.equals(mode)) checkedId = R.id.btnModeLight;
        else if (AppearanceUtils.MODE_DARK.equals(mode)) checkedId = R.id.btnModeDark;
        binding.displayModeToggle.check(checkedId);

        binding.displayModeToggle.addOnButtonCheckedListener((group, buttonId, isChecked) -> {
            if (!isChecked) return;
            String selected = AppearanceUtils.MODE_SYSTEM;
            if (buttonId == R.id.btnModeLight) selected = AppearanceUtils.MODE_LIGHT;
            else if (buttonId == R.id.btnModeDark) selected = AppearanceUtils.MODE_DARK;
            if (selected.equals(userStore.getAppearanceMode())) return;
            userStore.setAppearanceMode(selected);
            AppearanceUtils.apply(selected);
        });
    }

    private void saveTheme(String themeKey) {
        if (themeKey.equals(userStore.getThemeKey())) return;
        userStore.setThemeKey(themeKey);
        toast("Đã đổi màu giao diện.");
        requireActivity().recreate();
    }

    private void updateThemeSelection(String themeKey) {
        int accent = ThemeUtils.getPrimaryColor(requireContext(), themeKey);
        int accentContainer = ThemeUtils.getContainerColor(requireContext(), themeKey);

        applySelectionState(binding.themeGreen, ThemeUtils.THEME_GREEN.equals(themeKey));
        applySelectionState(binding.themeBlue, ThemeUtils.THEME_BLUE.equals(themeKey));
        applySelectionState(binding.themeOrange, ThemeUtils.THEME_ORANGE.equals(themeKey));
        applySelectionState(binding.themePurple, ThemeUtils.THEME_PURPLE.equals(themeKey));

        binding.cardProfileHeader.setStrokeColor(accentContainer);
        binding.avatarBadge.setCardBackgroundColor(accentContainer);
        binding.reminderIconBox.setCardBackgroundColor(accentContainer);
        binding.exportIconBox.setCardBackgroundColor(accentContainer);
        binding.importIconBox.setCardBackgroundColor(accentContainer);
        binding.sampleIconBox.setCardBackgroundColor(accentContainer);
        binding.paletteIconBox.setCardBackgroundColor(accentContainer);
        binding.musicIconBox.setCardBackgroundColor(accentContainer);

        if (userStore.getAvatarUri().isEmpty()) binding.imgAvatar.setColorFilter(accent);
        else binding.imgAvatar.clearColorFilter();
        binding.tvChangeAvatar.setTextColor(accent);
        binding.imgReminderIcon.setColorFilter(accent);
        binding.imgExport.setColorFilter(accent);
        binding.imgImport.setColorFilter(accent);
        binding.imgSample.setColorFilter(accent);
        binding.imgPalette.setColorFilter(accent);
        binding.imgMusicCenter.setColorFilter(accent);

        boolean dynamic = userStore.isDynamicColorsEnabled();
        float paletteAlpha = dynamic ? 0.42f : 1f;
        binding.themeGreen.setAlpha(paletteAlpha);
        binding.themeBlue.setAlpha(paletteAlpha);
        binding.themeOrange.setAlpha(paletteAlpha);
        binding.themePurple.setAlpha(paletteAlpha);
        binding.themeGreen.setEnabled(!dynamic);
        binding.themeBlue.setEnabled(!dynamic);
        binding.themeOrange.setEnabled(!dynamic);
        binding.themePurple.setEnabled(!dynamic);

        ThemeUtils.tintSwitch(binding.switchReminder, requireContext(), themeKey);
        ThemeUtils.tintSwitch(binding.switchFinancialAlert, requireContext(), themeKey);
        ThemeUtils.tintSwitch(binding.switchHideAmounts, requireContext(), themeKey);
        ThemeUtils.tintSwitch(binding.switchDynamicColors, requireContext(), themeKey);
        ThemeUtils.tintTonalButton(binding.btnNotificationSettings, requireContext(), themeKey);
    }

    private void applyAppearance() {
        updateThemeSelection(userStore.getThemeKey());
    }

    private void applySelectionState(MaterialCardView view, boolean selected) {
        view.setStrokeWidth(selected ? dp(3) : dp(0));
        view.setStrokeColor(selected
                ? ContextCompat.getColor(requireContext(), R.color.on_surface)
                : ContextCompat.getColor(requireContext(), R.color.transparent));
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }

    private void showChangePasswordDialog() {
        DialogChangePasswordBinding d = DialogChangePasswordBinding.inflate(getLayoutInflater());
        androidx.appcompat.app.AlertDialog alert = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Đổi mật khẩu")
                .setMessage("Mật khẩu mới cần có ít nhất 4 ký tự.")
                .setView(d.getRoot())
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Đổi mật khẩu", null)
                .create();
        alert.setOnShowListener(ignored -> alert.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String oldPassword = text(d.edtOldPassword.getText());
            String newPassword = text(d.edtNewPassword.getText());
            String confirm = text(d.edtConfirmPassword.getText());
            if (newPassword.length() < 4) {
                d.edtNewPassword.setError("Tối thiểu 4 ký tự");
                return;
            }
            if (!newPassword.equals(confirm)) {
                d.edtConfirmPassword.setError("Mật khẩu nhập lại chưa khớp");
                return;
            }
            if (!userStore.changePassword(oldPassword, newPassword)) {
                d.edtOldPassword.setError("Mật khẩu hiện tại không đúng");
                return;
            }
            alert.dismiss();
            toast("Đã đổi mật khẩu.");
        }));
        alert.show();
    }

    private String text(CharSequence value) { return value == null ? "" : value.toString(); }

    private void enableReminder() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
            return;
        }
        userStore.setReminderEnabled(true);
        ReminderScheduler.schedule(requireContext());
    }

    private void requestNotificationPermissionOnly() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            generalNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private void confirmImport(Uri uri) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Khôi phục dữ liệu?")
                .setMessage("Dữ liệu trong file sẽ thay thế dữ liệu hiện tại. Tài khoản đăng nhập cục bộ không bị thay đổi.")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Khôi phục", (d, w) -> backupManager.importFrom(uri, callback()))
                .show();
    }

    private BackupManager.Callback callback() {
        return new BackupManager.Callback() {
            @Override public void onSuccess(String message) { toast(message); }
            @Override public void onError(String message) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Có lỗi")
                        .setMessage(message)
                        .setPositiveButton("Đóng", null)
                        .show();
            }
        };
    }

    private void logout() {
        userStore.logout();
        Intent intent = new Intent(requireContext(), AuthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void toast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null && userStore != null) {
            binding.tvNotificationToneSummary.setText("Âm báo: " + userStore.getNotificationSoundName());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
