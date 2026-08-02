package com.sangapp.gooddaily.ui.health;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sangapp.gooddaily.R;
import com.sangapp.gooddaily.data.local.entity.BodyRecordEntity;
import com.sangapp.gooddaily.data.local.entity.MealEntity;
import com.sangapp.gooddaily.databinding.DialogAddBodyBinding;
import com.sangapp.gooddaily.databinding.DialogAddMealBinding;
import com.sangapp.gooddaily.databinding.FragmentHealthBinding;
import com.sangapp.gooddaily.data.local.prefs.LocalUserStore;
import com.sangapp.gooddaily.util.DateUtils;
import com.sangapp.gooddaily.util.ThemeUtils;
import com.sangapp.gooddaily.viewmodel.HealthViewModel;

import java.util.List;
import java.util.Locale;

public class HealthFragment extends Fragment {
    private FragmentHealthBinding binding;
    private HealthViewModel vm;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHealthBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        vm = new ViewModelProvider(this).get(HealthViewModel.class);
        String themeKey = new LocalUserStore(requireContext()).getThemeKey();
        ThemeUtils.tintFilledButton(binding.btnAddBody, requireContext(), themeKey);
        vm.latestBody().observe(getViewLifecycleOwner(), body -> {
            if (body == null) {
                binding.tvWeight.setText("-- kg");
                binding.tvBmi.setText("BMI: --");
            } else {
                binding.tvWeight.setText(String.format(Locale.US, "%.1f kg", body.weight));
                binding.tvBmi.setText(String.format(Locale.US, "BMI: %.1f", body.bmi()));
            }
        });
        vm.calories().observe(getViewLifecycleOwner(), value -> binding.tvCalories.setText(String.format(Locale.US, "%.0f", value == null ? 0 : value)));
        vm.protein().observe(getViewLifecycleOwner(), value -> binding.tvProtein.setText(String.format(Locale.US, "Protein: %.1f g", value == null ? 0 : value)));
        vm.meals().observe(getViewLifecycleOwner(), this::renderMeals);
        vm.bodies().observe(getViewLifecycleOwner(), this::renderBodies);

        binding.btnAddBody.setOnClickListener(v -> showBodyDialog());
        binding.btnAddMeal.setOnClickListener(v -> showMealDialog());
        Bundle args = getArguments();
        String focus = args == null ? "" : args.getString("focus", "");
        binding.healthScroll.post(() -> {
            if ("meals".equals(focus)) binding.healthScroll.smoothScrollTo(0, binding.tvMealsSection.getTop());
            else if ("body".equals(focus)) binding.healthScroll.smoothScrollTo(0, binding.tvBodiesSection.getTop());
        });
    }

    private void showBodyDialog() {
        DialogAddBodyBinding dialog = DialogAddBodyBinding.inflate(getLayoutInflater());
        androidx.appcompat.app.AlertDialog alert = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Ghi số đo cơ thể")
                .setView(dialog.getRoot())
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", null)
                .create();
        alert.setOnShowListener(x -> alert.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            double weight = parse(dialog.edtWeight.getText());
            double height = parse(dialog.edtHeight.getText());
            if (weight <= 0 || height <= 0) {
                dialog.edtWeight.setError("Nhập cân nặng và chiều cao hợp lệ");
                return;
            }
            vm.addBody(weight, height, parse(dialog.edtBodyFat.getText()), parse(dialog.edtMuscle.getText()));
            alert.dismiss();
        }));
        alert.show();
    }

    private void showMealDialog() {
        DialogAddMealBinding dialog = DialogAddMealBinding.inflate(getLayoutInflater());
        androidx.appcompat.app.AlertDialog alert = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Thêm bữa ăn")
                .setView(dialog.getRoot())
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", null)
                .create();
        alert.setOnShowListener(x -> alert.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = dialog.edtMealName.getText() == null ? "" : dialog.edtMealName.getText().toString().trim();
            double calories = parse(dialog.edtCalories.getText());
            if (name.isEmpty() || calories <= 0) {
                dialog.edtMealName.setError("Nhập tên món và kcal");
                return;
            }
            vm.addMeal(name, calories, parse(dialog.edtProtein.getText()));
            alert.dismiss();
        }));
        alert.show();
    }

    private void renderMeals(List<MealEntity> meals) {
        binding.mealContainer.removeAllViews();
        if (meals == null || meals.isEmpty()) {
            binding.mealContainer.addView(infoText("Chưa có bữa ăn nào."));
            return;
        }
        int limit = Math.min(meals.size(), 8);
        for (int i = 0; i < limit; i++) {
            MealEntity item = meals.get(i);
            binding.mealContainer.addView(infoText(item.name + "\n" + String.format(Locale.US, "%.0f kcal · %.1f g protein · ", item.calories, item.protein) + DateUtils.formatDateTime(item.eatenAt)));
        }
    }

    private void renderBodies(List<BodyRecordEntity> bodies) {
        binding.bodyContainer.removeAllViews();
        if (bodies == null || bodies.isEmpty()) {
            binding.bodyContainer.addView(infoText("Chưa có lịch sử số đo."));
            return;
        }
        int limit = Math.min(bodies.size(), 8);
        for (int i = 0; i < limit; i++) {
            BodyRecordEntity item = bodies.get(i);
            binding.bodyContainer.addView(infoText(String.format(Locale.US, "%.1f kg · %.0f cm · BMI %.1f\nMỡ %.1f%% · Cơ %.1f kg · %s", item.weight, item.height, item.bmi(), item.bodyFatPercent, item.muscleMass, DateUtils.formatDateTime(item.recordedAt))));
        }
    }

    private TextView infoText(String text) {
        TextView tv = new TextView(requireContext());
        tv.setText(text);
        tv.setTextColor(requireContext().getColor(R.color.on_surface));
        tv.setTextSize(14);
        tv.setBackgroundColor(requireContext().getColor(R.color.surface));
        int p = (int) (14 * getResources().getDisplayMetrics().density);
        tv.setPadding(p, p, p, p);
        LinearLayoutParamsCompat.applyBottomMargin(tv, (int) (8 * getResources().getDisplayMetrics().density));
        return tv;
    }

    private double parse(CharSequence value) {
        try { return Double.parseDouble(value == null ? "" : value.toString().trim().replace(',', '.')); }
        catch (Exception e) { return 0; }
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }

    private static final class LinearLayoutParamsCompat {
        static void applyBottomMargin(View view, int margin) {
            android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = margin;
            view.setLayoutParams(params);
        }
    }
}
