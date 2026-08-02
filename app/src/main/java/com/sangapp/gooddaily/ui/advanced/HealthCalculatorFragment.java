package com.sangapp.gooddaily.ui.advanced;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sangapp.gooddaily.data.local.GoodDailyDatabase;
import com.sangapp.gooddaily.data.local.entity.BodyRecordEntity;
import com.sangapp.gooddaily.data.local.entity.HealthProfileEntity;
import com.sangapp.gooddaily.data.local.prefs.LocalUserStore;
import com.sangapp.gooddaily.databinding.FragmentHealthCalculatorBinding;
import com.sangapp.gooddaily.util.AppExecutors;
import com.sangapp.gooddaily.util.ThemeUtils;

import java.util.Calendar;
import java.util.Locale;

public class HealthCalculatorFragment extends Fragment {
    private FragmentHealthCalculatorBinding binding;
    private BodyRecordEntity latestBody;
    private HealthProfileEntity profile;

    private static final String[] GENDERS = {"Nam", "Nữ"};
    private static final String[] ACTIVITIES = {"Ít vận động", "Vận động nhẹ", "Vận động vừa", "Vận động nhiều", "Rất nhiều"};
    private static final String[] GOALS = {"Giữ cân", "Tăng cân", "Giảm cân"};

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHealthCalculatorBinding.inflate(inflater, container, false); return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.dropdownGender.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, GENDERS));
        binding.dropdownActivity.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, ACTIVITIES));
        binding.dropdownGoalType.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, GOALS));
        ThemeUtils.tintFilledButton(binding.btnSaveHealthProfile, requireContext(), new LocalUserStore(requireContext()).getThemeKey());

        GoodDailyDatabase db = GoodDailyDatabase.get(requireContext());
        db.healthDao().observeLatestBody().observe(getViewLifecycleOwner(), body -> { latestBody = body; render(); });
        db.healthProfileDao().observe().observe(getViewLifecycleOwner(), item -> { profile = item; fillProfile(); render(); });
        binding.btnSaveHealthProfile.setOnClickListener(v -> save());
    }

    private void fillProfile() {
        if (binding == null || profile == null) return;
        binding.edtBirthYear.setText(String.valueOf(profile.birthYear));
        binding.dropdownGender.setText(profile.gender, false);
        binding.dropdownActivity.setText(profile.activityLevel, false);
        binding.dropdownGoalType.setText(profile.goalType, false);
        binding.edtTargetWeight.setText(number(profile.targetWeight));
        binding.edtManualCalories.setText(profile.manualCalorieTarget > 0 ? number(profile.manualCalorieTarget) : "");
    }

    private void save() {
        int year = parseInt(text(binding.edtBirthYear.getText()));
        int current = Calendar.getInstance().get(Calendar.YEAR);
        if (year < current - 100 || year > current - 10) { toast("Năm sinh chưa hợp lệ."); return; }
        String gender = text(binding.dropdownGender.getText());
        String activity = text(binding.dropdownActivity.getText());
        String goal = text(binding.dropdownGoalType.getText());
        if (gender.isEmpty() || activity.isEmpty() || goal.isEmpty()) { toast("Hãy chọn giới tính, mức vận động và mục tiêu."); return; }
        profile = new HealthProfileEntity("MAIN", year, gender, activity, goal,
                parseDouble(text(binding.edtTargetWeight.getText())),
                parseDouble(text(binding.edtManualCalories.getText())), System.currentTimeMillis());
        AppExecutors.io().execute(() -> GoodDailyDatabase.get(requireContext()).healthProfileDao().save(profile));
        render(); toast("Đã lưu mục tiêu sức khỏe.");
    }

    private void render() {
        if (binding == null) return;
        if (latestBody == null) {
            binding.tvEnergyResult.setText("Chưa có số đo cơ thể. Vào Sức khỏe → Đo cơ thể để nhập cân nặng và chiều cao.");
            binding.weightGoalProgress.setProgressCompat(0, true); return;
        }
        if (profile == null) {
            binding.tvEnergyResult.setText(String.format(Locale.US, "Cân nặng %.1f kg · chiều cao %.0f cm · BMI %.1f\nHãy hoàn thiện hồ sơ để tính BMR và TDEE.", latestBody.weight, latestBody.height, latestBody.bmi()));
            return;
        }
        int age = Calendar.getInstance().get(Calendar.YEAR) - profile.birthYear;
        double bmr = 10 * latestBody.weight + 6.25 * latestBody.height - 5 * age + ("Nam".equals(profile.gender) ? 5 : -161);
        double factor = activityFactor(profile.activityLevel);
        double tdee = bmr * factor;
        double autoTarget = tdee + ("Tăng cân".equals(profile.goalType) ? 300 : "Giảm cân".equals(profile.goalType) ? -300 : 0);
        double target = profile.manualCalorieTarget > 0 ? profile.manualCalorieTarget : autoTarget;
        binding.tvEnergyResult.setText(String.format(Locale.US,
                "Tuổi: %d · BMI: %.1f\nBMR: %.0f kcal/ngày\nTDEE: %.0f kcal/ngày\nKcal mục tiêu: %.0f kcal/ngày\nGợi ý protein: %.0f–%.0f g/ngày",
                age, latestBody.bmi(), bmr, tdee, target, latestBody.weight * 1.6, latestBody.weight * 2.2));

        if (profile.targetWeight > 0) {
            double start = latestBody.weight;
            double distance = Math.abs(profile.targetWeight - start);
            int progress = distance < .1 ? 100 : Math.max(0, Math.min(100, (int)Math.round((1 - Math.abs(profile.targetWeight - latestBody.weight) / Math.max(distance, 1)) * 100)));
            binding.weightGoalProgress.setProgressCompat(progress, true);
            binding.tvWeightGoal.setText(String.format(Locale.US, "Mục tiêu %.1f kg · hiện tại %.1f kg", profile.targetWeight, latestBody.weight));
        } else {
            binding.weightGoalProgress.setProgressCompat(0, true);
            binding.tvWeightGoal.setText("Chưa đặt cân nặng mục tiêu.");
        }
    }

    private double activityFactor(String value) {
        switch (value) {
            case "Vận động nhẹ": return 1.375;
            case "Vận động vừa": return 1.55;
            case "Vận động nhiều": return 1.725;
            case "Rất nhiều": return 1.9;
            default: return 1.2;
        }
    }
    private String text(CharSequence v) { return v == null ? "" : v.toString().trim(); }
    private int parseInt(String v) { try { return Integer.parseInt(v); } catch (Exception e) { return 0; } }
    private double parseDouble(String v) { try { return Double.parseDouble(v.replace(',','.')); } catch (Exception e) { return 0; } }
    private String number(double v) { return Math.abs(v-Math.rint(v))<.001 ? String.format(Locale.US,"%.0f",v) : String.format(Locale.US,"%.1f",v); }
    private void toast(String m) { Toast.makeText(requireContext(), m, Toast.LENGTH_LONG).show(); }
    @Override public void onDestroyView() { super.onDestroyView(); binding=null; }
}
