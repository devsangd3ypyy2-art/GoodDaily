package com.sangapp.gooddaily.ui.dashboard;

import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.sangapp.gooddaily.R;
import com.sangapp.gooddaily.data.local.prefs.LocalUserStore;
import com.sangapp.gooddaily.databinding.FragmentDashboardBinding;
import com.sangapp.gooddaily.util.DateUtils;
import com.sangapp.gooddaily.util.MoneyUtils;
import com.sangapp.gooddaily.util.ThemeUtils;
import com.sangapp.gooddaily.viewmodel.DashboardViewModel;

import java.util.Calendar;
import java.util.Locale;

public class DashboardFragment extends Fragment {
    private FragmentDashboardBinding binding;
    private double income;
    private double expense;
    private double totalBalance;
    private int taskCount;
    private int completedTasks;
    private int habitCount;
    private int checkedHabits;
    private boolean hideAmounts;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        DashboardViewModel vm = new ViewModelProvider(this).get(DashboardViewModel.class);
        LocalUserStore userStore = new LocalUserStore(requireContext());
        hideAmounts = userStore.isHideAmountsEnabled();
        int accent = ThemeUtils.getPrimaryColor(requireContext(), userStore.getThemeKey());
        int accentContainer = ThemeUtils.getContainerColor(requireContext(), userStore.getThemeKey());
        binding.cardFinanceSummary.setCardBackgroundColor(accent);
        binding.cardDashboardAvatar.setCardBackgroundColor(accentContainer);
        ThemeUtils.tintTonalButton(binding.btnDashboardNotifications, requireContext(), userStore.getThemeKey());
        loadAvatar(userStore, accent);

        updateHeader(userStore, accent);

        vm.totalBalance().observe(getViewLifecycleOwner(), value -> {
            totalBalance = value == null ? 0 : value;
            renderFinance();
        });
        vm.todayIncome().observe(getViewLifecycleOwner(), value -> {
            income = value == null ? 0 : value;
            renderFinance();
        });
        vm.todayExpense().observe(getViewLifecycleOwner(), value -> {
            expense = value == null ? 0 : value;
            renderFinance();
        });
        vm.todayCalories().observe(getViewLifecycleOwner(), value ->
                binding.tvCalories.setText(String.format(Locale.US, "%.0f", value == null ? 0 : value)));
        vm.studyMinutes().observe(getViewLifecycleOwner(), value ->
                binding.tvStudyMinutes.setText((value == null ? 0 : value) + " phút"));
        vm.latestBody().observe(getViewLifecycleOwner(), body -> {
            if (body == null) binding.tvLatestWeight.setText("Chưa có dữ liệu cân nặng");
            else binding.tvLatestWeight.setText(String.format(Locale.US,
                    "%.1f kg · BMI %.1f · cơ %.1f kg", body.weight, body.bmi(), body.muscleMass));
        });
        vm.taskCount().observe(getViewLifecycleOwner(), value -> {
            taskCount = value == null ? 0 : value;
            renderTasks();
        });
        vm.completedTaskCount().observe(getViewLifecycleOwner(), value -> {
            completedTasks = value == null ? 0 : value;
            renderTasks();
        });
        vm.habitCount().observe(getViewLifecycleOwner(), value -> {
            habitCount = value == null ? 0 : value;
            renderHabits();
        });
        vm.checkedHabitCount().observe(getViewLifecycleOwner(), value -> {
            checkedHabits = value == null ? 0 : value;
            renderHabits();
        });
        vm.weeklyExpenses().observe(getViewLifecycleOwner(), binding.weeklyChart::setData);

        binding.cardFinanceSummary.setOnClickListener(v -> navigate(v, R.id.financeFragment, "balance"));
        binding.cardWeeklyChart.setOnClickListener(v -> navigate(v, R.id.financeFragment, "history"));
        binding.cardCalories.setOnClickListener(v -> navigate(v, R.id.healthFragment, "meals"));
        binding.cardHealthSummary.setOnClickListener(v -> navigate(v, R.id.healthFragment, "body"));
        binding.cardStudy.setOnClickListener(v -> navigate(v, R.id.plannerFragment, "learning"));
        binding.cardHabits.setOnClickListener(v -> navigate(v, R.id.plannerFragment, "habits"));
        binding.cardTaskProgress.setOnClickListener(v -> navigate(v, R.id.plannerFragment, "tasks"));
        binding.btnDashboardNotifications.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.reminderManagerFragment));
        binding.cardDashboardAvatar.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.profileFragment));

        binding.getRoot().setAlpha(0f);
        binding.getRoot().animate().alpha(1f).setDuration(220L).start();
    }

    private void updateHeader(LocalUserStore store, int accent) {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting = hour < 11 ? "Chào buổi sáng" : hour < 18 ? "Chào buổi chiều" : "Chào buổi tối";
        binding.tvGreeting.setText(greeting + ", " + store.getDisplayName());
        binding.tvToday.setText(DateUtils.formatFullToday());
        loadAvatar(store, accent);
    }

    private void loadAvatar(LocalUserStore store, int accent) {
        String value = store.getAvatarUri();
        if (value == null || value.trim().isEmpty()) {
            binding.imgDashboardAvatar.setImageResource(R.drawable.ic_app_logo);
            int padding = dp(12);
            binding.imgDashboardAvatar.setPadding(padding, padding, padding, padding);
            binding.imgDashboardAvatar.setScaleType(android.widget.ImageView.ScaleType.CENTER_INSIDE);
            binding.imgDashboardAvatar.setColorFilter(accent);
            return;
        }
        try {
            binding.imgDashboardAvatar.clearColorFilter();
            binding.imgDashboardAvatar.setPadding(0, 0, 0, 0);
            binding.imgDashboardAvatar.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
            binding.imgDashboardAvatar.setImageURI(Uri.parse(value));
        } catch (Exception ignored) {
            binding.imgDashboardAvatar.setImageResource(R.drawable.ic_app_logo);
            int padding = dp(12);
            binding.imgDashboardAvatar.setPadding(padding, padding, padding, padding);
            binding.imgDashboardAvatar.setColorFilter(accent);
        }
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }

    private void navigate(View view, int destination, String focus) {
        Bundle args = new Bundle();
        args.putString("focus", focus);
        Navigation.findNavController(view).navigate(destination, args);
    }

    private void renderFinance() {
        if (hideAmounts) {
            binding.tvTodayIncome.setText("Thu: ••••••");
            binding.tvTodayExpense.setText("Chi: ••••••");
            binding.tvTodayBalance.setText("•••••• ₫");
            return;
        }
        binding.tvTodayIncome.setText("Thu: " + MoneyUtils.format(income));
        binding.tvTodayExpense.setText("Chi: " + MoneyUtils.format(expense));
        binding.tvTodayBalance.setText(MoneyUtils.format(totalBalance));
    }

    private void renderTasks() {
        binding.tvTaskProgress.setText("Công việc: " + completedTasks + "/" + taskCount + " hoàn thành");
    }

    private void renderHabits() {
        binding.tvHabitProgress.setText(checkedHabits + "/" + habitCount);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null) {
            LocalUserStore store = new LocalUserStore(requireContext());
            updateHeader(store, ThemeUtils.getPrimaryColor(requireContext(), store.getThemeKey()));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
