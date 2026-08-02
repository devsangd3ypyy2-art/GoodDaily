package com.sangapp.gooddaily.ui.dashboard;

import android.os.Bundle;
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
        String name = userStore.getDisplayName();
        hideAmounts = userStore.isHideAmountsEnabled();
        binding.cardFinanceSummary.setCardBackgroundColor(
                ThemeUtils.getPrimaryColor(requireContext(), userStore.getThemeKey())
        );

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting = hour < 11 ? "Chào buổi sáng" : hour < 18 ? "Chào buổi chiều" : "Chào buổi tối";
        binding.tvGreeting.setText(greeting + ", " + name);
        binding.tvToday.setText(DateUtils.formatFullToday());

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
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
