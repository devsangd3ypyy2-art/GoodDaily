package com.sangapp.gooddaily.ui.health;

import android.os.Bundle;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.sangapp.gooddaily.R;
import com.sangapp.gooddaily.data.local.entity.BodyRecordEntity;
import com.sangapp.gooddaily.data.local.entity.MealEntity;
import com.sangapp.gooddaily.data.local.prefs.LocalUserStore;
import com.sangapp.gooddaily.feature.FeatureCatalog;
import com.sangapp.gooddaily.databinding.DialogAddBodyBinding;
import com.sangapp.gooddaily.databinding.DialogAddMealBinding;
import com.sangapp.gooddaily.databinding.FragmentHealthBinding;
import com.sangapp.gooddaily.ui.common.FeatureNavigator;
import com.sangapp.gooddaily.ui.common.ModuleToolsRenderer;
import com.sangapp.gooddaily.util.DateUtils;
import com.sangapp.gooddaily.util.AttachmentStore;
import com.sangapp.gooddaily.util.ThemeUtils;
import com.sangapp.gooddaily.viewmodel.HealthViewModel;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class HealthFragment extends Fragment {
    private FragmentHealthBinding binding;
    private HealthViewModel vm;
    private double todayProtein;
    private double todayCarbs;
    private double todayFat;
    private DialogAddMealBinding activeMealForm;
    private String pendingMealImage = "";

    private final ActivityResultLauncher<String[]> pickMealImage = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null || activeMealForm == null) return;
                try {
                    pendingMealImage = AttachmentStore.copyToInternal(requireContext(), uri);
                    activeMealForm.btnMealImage.setText("Đã chọn ảnh: " + AttachmentStore.displayName(requireContext(), uri));
                } catch (Exception e) {
                    Snackbar.make(activeMealForm.getRoot(), "Không thể lưu ảnh: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            });

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHealthBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        vm = new ViewModelProvider(this).get(HealthViewModel.class);
        String themeKey = new LocalUserStore(requireContext()).getThemeKey();
        ThemeUtils.tintFilledButton(binding.btnAddBody, requireContext(), themeKey);
        setupHealthTools();

        vm.latestBody().observe(getViewLifecycleOwner(), body -> {
            if (body == null) {
                binding.tvWeight.setText("-- kg");
                binding.tvBmi.setText("BMI: --");
            } else {
                binding.tvWeight.setText(String.format(Locale.getDefault(), "%.1f kg", body.weight));
                binding.tvBmi.setText(String.format(Locale.getDefault(), "BMI: %.1f", body.bmi()));
            }
        });
        vm.calories().observe(getViewLifecycleOwner(), value ->
                binding.tvCalories.setText(String.format(Locale.getDefault(), "%.0f", safe(value))));
        vm.protein().observe(getViewLifecycleOwner(), value -> {
            todayProtein = safe(value);
            renderMacros();
        });
        vm.carbs().observe(getViewLifecycleOwner(), value -> {
            todayCarbs = safe(value);
            renderMacros();
        });
        vm.fat().observe(getViewLifecycleOwner(), value -> {
            todayFat = safe(value);
            renderMacros();
        });
        vm.meals().observe(getViewLifecycleOwner(), this::renderMeals);
        vm.bodies().observe(getViewLifecycleOwner(), this::renderBodies);

        binding.btnAddBody.setOnClickListener(v -> showBodyDialog(null));
        binding.btnAddMeal.setOnClickListener(v -> showMealDialog(null));
        binding.btnHealthAdvanced.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.healthCalculatorFragment));
        binding.btnHealthReport.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.healthReportFragment));

        Bundle args = getArguments();
        String focus = args == null ? "" : args.getString("focus", "");
        binding.healthScroll.post(() -> {
            if ("meals".equals(focus)) binding.healthScroll.smoothScrollTo(0, binding.tvMealsSection.getTop());
            else if ("body".equals(focus)) binding.healthScroll.smoothScrollTo(0, binding.tvBodiesSection.getTop());
        });
    }

    private void setupHealthTools() {
        ModuleToolsRenderer.render(binding.healthToolsContainer, Arrays.asList(
                new ModuleToolsRenderer.ToolItem(
                        "Thực phẩm thường dùng",
                        "Tạo món mẫu để nhập kcal và dinh dưỡng nhanh hơn.",
                        R.drawable.ic_book,
                        v -> FeatureNavigator.openFeature(v, FeatureCatalog.HEALTH_FOOD)),
                new ModuleToolsRenderer.ToolItem(
                        "Lượng nước",
                        "Ghi lượng nước uống và mục tiêu mỗi ngày.",
                        R.drawable.ic_plus,
                        v -> FeatureNavigator.openFeature(v, FeatureCatalog.HEALTH_WATER)),
                new ModuleToolsRenderer.ToolItem(
                        "Giấc ngủ",
                        "Theo dõi giờ ngủ, giờ thức và chất lượng giấc ngủ.",
                        R.drawable.ic_clock,
                        v -> FeatureNavigator.openFeature(v, FeatureCatalog.HEALTH_SLEEP)),
                new ModuleToolsRenderer.ToolItem(
                        "Tâm trạng",
                        "Ghi cảm xúc, căng thẳng và mức năng lượng.",
                        R.drawable.ic_star,
                        v -> FeatureNavigator.openFeature(v, FeatureCatalog.HEALTH_MOOD)),
                new ModuleToolsRenderer.ToolItem(
                        "Bài tập",
                        "Lưu thời gian, số hiệp, số lần và kcal vận động.",
                        R.drawable.ic_health,
                        v -> FeatureNavigator.openFeature(v, FeatureCatalog.HEALTH_WORKOUT)),
                new ModuleToolsRenderer.ToolItem(
                        "Thuốc và lịch uống",
                        "Theo dõi liều lượng, số lần và trạng thái đã uống.",
                        R.drawable.ic_bell,
                        v -> FeatureNavigator.openFeature(v, FeatureCatalog.HEALTH_MEDICATION)),
                new ModuleToolsRenderer.ToolItem(
                        "Số đo cơ thể",
                        "Theo dõi eo, ngực, tay, chân và các chỉ số bổ sung.",
                        R.drawable.ic_chart,
                        v -> FeatureNavigator.openFeature(v, FeatureCatalog.HEALTH_MEASUREMENT))
        ));
    }

    private void renderMacros() {
        if (binding == null) return;
        binding.tvProtein.setText(String.format(Locale.getDefault(),
                "P %.1fg · C %.1fg · F %.1fg", todayProtein, todayCarbs, todayFat));
    }

    private void showBodyDialog(@Nullable BodyRecordEntity existing) {
        DialogAddBodyBinding dialog = DialogAddBodyBinding.inflate(getLayoutInflater());
        if (existing != null) {
            set(dialog.edtWeight, existing.weight);
            set(dialog.edtHeight, existing.height);
            set(dialog.edtBodyFat, existing.bodyFatPercent);
            set(dialog.edtMuscle, existing.muscleMass);
            set(dialog.edtWaist, existing.waist);
            set(dialog.edtChest, existing.chest);
            set(dialog.edtArm, existing.arm);
            set(dialog.edtThigh, existing.thigh);
        }
        AlertDialog alert = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(existing == null ? "Ghi số đo cơ thể" : "Sửa số đo cơ thể")
                .setView(dialog.getRoot())
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", null)
                .create();
        alert.setOnShowListener(x -> alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            double weight = parse(dialog.edtWeight.getText());
            double height = parse(dialog.edtHeight.getText());
            if (weight <= 0 || height <= 0) {
                dialog.edtWeight.setError("Nhập cân nặng và chiều cao hợp lệ");
                return;
            }
            vm.saveBody(existing, weight, height,
                    parse(dialog.edtBodyFat.getText()), parse(dialog.edtMuscle.getText()),
                    parse(dialog.edtWaist.getText()), parse(dialog.edtChest.getText()),
                    parse(dialog.edtArm.getText()), parse(dialog.edtThigh.getText()));
            alert.dismiss();
        }));
        alert.show();
    }

    private void showMealDialog(@Nullable MealEntity existing) {
        DialogAddMealBinding dialog = DialogAddMealBinding.inflate(getLayoutInflater());
        activeMealForm = dialog;
        pendingMealImage = existing == null || existing.imagePath == null ? "" : existing.imagePath;
        if (!pendingMealImage.isEmpty()) dialog.btnMealImage.setText("Đã có ảnh · chạm để thay");
        dialog.btnMealImage.setOnClickListener(v -> pickMealImage.launch(new String[]{"image/*"}));
        String[] types = {"Bữa sáng", "Bữa trưa", "Bữa tối", "Bữa phụ", "Khác"};
        dialog.dropdownMealType.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, types));
        dialog.dropdownMealType.setText(existing == null || empty(existing.mealType) ? "Khác" : existing.mealType, false);
        if (existing != null) {
            dialog.edtMealName.setText(existing.name);
            set(dialog.edtMealGrams, existing.grams);
            set(dialog.edtCalories, existing.calories);
            set(dialog.edtProtein, existing.protein);
            set(dialog.edtCarbs, existing.carbs);
            set(dialog.edtFat, existing.fat);
        }
        AlertDialog alert = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(existing == null ? "Thêm bữa ăn" : "Sửa bữa ăn")
                .setView(dialog.getRoot())
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", null)
                .create();
        alert.setOnShowListener(x -> alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = text(dialog.edtMealName.getText());
            double calories = parse(dialog.edtCalories.getText());
            if (name.isEmpty() || calories <= 0) {
                dialog.edtMealName.setError("Nhập tên món và kcal");
                return;
            }
            vm.saveMeal(existing, name, text(dialog.dropdownMealType.getText()),
                    parse(dialog.edtMealGrams.getText()), calories,
                    parse(dialog.edtProtein.getText()), parse(dialog.edtCarbs.getText()),
                    parse(dialog.edtFat.getText()), pendingMealImage);
            alert.dismiss();
            activeMealForm = null;
        }));
        alert.setOnDismissListener(x -> activeMealForm = null);
        alert.show();
    }

    private void renderMeals(List<MealEntity> meals) {
        if (binding == null) return;
        binding.mealContainer.removeAllViews();
        if (meals == null || meals.isEmpty()) {
            binding.mealContainer.addView(infoText("Chưa có bữa ăn nào. Nhấn Thêm bữa ăn để bắt đầu."));
            return;
        }
        int limit = Math.min(meals.size(), 12);
        for (int i = 0; i < limit; i++) {
            MealEntity item = meals.get(i);
            String type = empty(item.mealType) ? "Khác" : item.mealType;
            TextView row = infoText(String.format(Locale.getDefault(),
                    "%s · %s\n%.0f kcal · %.1fg P · %.1fg C · %.1fg F%s\n%s",
                    type, value(item.name), item.calories, item.protein, item.carbs, item.fat,
                    item.grams > 0 ? String.format(Locale.getDefault(), " · %.0fg", item.grams) : "",
                    DateUtils.formatDateTime(item.eatenAt) + (!empty(item.imagePath) ? " · Có ảnh" : "")));
            row.setOnClickListener(v -> showMealDialog(item));
            row.setOnLongClickListener(v -> {
                confirmDeleteMeal(item);
                return true;
            });
            binding.mealContainer.addView(row);
        }
    }

    private void renderBodies(List<BodyRecordEntity> bodies) {
        if (binding == null) return;
        binding.bodyContainer.removeAllViews();
        if (bodies == null || bodies.isEmpty()) {
            binding.bodyContainer.addView(infoText("Chưa có lịch sử số đo. Nhấn Đo cơ thể để thêm."));
            return;
        }
        int limit = Math.min(bodies.size(), 12);
        for (int i = 0; i < limit; i++) {
            BodyRecordEntity item = bodies.get(i);
            String extra = measurements(item);
            TextView row = infoText(String.format(Locale.getDefault(),
                    "%.1f kg · %.0f cm · BMI %.1f\nMỡ %.1f%% · Cơ %.1f kg%s\n%s",
                    item.weight, item.height, item.bmi(), item.bodyFatPercent,
                    item.muscleMass, extra, DateUtils.formatDateTime(item.recordedAt)));
            row.setOnClickListener(v -> showBodyDialog(item));
            row.setOnLongClickListener(v -> {
                confirmDeleteBody(item);
                return true;
            });
            binding.bodyContainer.addView(row);
        }
    }

    private String measurements(BodyRecordEntity item) {
        StringBuilder sb = new StringBuilder();
        if (item.waist > 0) sb.append(String.format(Locale.getDefault(), " · Eo %.1f", item.waist));
        if (item.chest > 0) sb.append(String.format(Locale.getDefault(), " · Ngực %.1f", item.chest));
        if (item.arm > 0) sb.append(String.format(Locale.getDefault(), " · Tay %.1f", item.arm));
        if (item.thigh > 0) sb.append(String.format(Locale.getDefault(), " · Đùi %.1f", item.thigh));
        return sb.toString();
    }

    private void confirmDeleteMeal(MealEntity item) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xóa bữa ăn?")
                .setMessage("Bạn có thể nhấn Hoàn tác ngay sau khi xóa.")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (d, w) -> {
                    vm.deleteMeal(item);
                    Snackbar.make(binding.getRoot(), "Đã xóa bữa ăn", Snackbar.LENGTH_LONG)
                            .setAction("Hoàn tác", v -> vm.saveMeal(null, item.name, item.mealType,
                                    item.grams, item.calories, item.protein, item.carbs, item.fat, item.imagePath))
                            .show();
                }).show();
    }

    private void confirmDeleteBody(BodyRecordEntity item) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xóa số đo?")
                .setMessage("Bạn có thể nhấn Hoàn tác ngay sau khi xóa.")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (d, w) -> {
                    vm.deleteBody(item);
                    Snackbar.make(binding.getRoot(), "Đã xóa số đo", Snackbar.LENGTH_LONG)
                            .setAction("Hoàn tác", v -> vm.saveBody(null, item.weight, item.height,
                                    item.bodyFatPercent, item.muscleMass, item.waist, item.chest,
                                    item.arm, item.thigh))
                            .show();
                }).show();
    }

    private TextView infoText(String text) {
        TextView tv = new TextView(requireContext());
        tv.setText(text);
        tv.setTextColor(requireContext().getColor(R.color.on_surface));
        tv.setTextSize(14);
        tv.setBackgroundResource(R.drawable.bg_list_item_surface);
        tv.setClickable(true);
        tv.setFocusable(true);
        int p = dp(14);
        tv.setPadding(p, p, p, p);
        android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(8);
        tv.setLayoutParams(params);
        return tv;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void set(android.widget.EditText editText, double value) {
        if (value > 0) editText.setText(String.format(Locale.getDefault(), "%.1f", value));
    }

    private double parse(CharSequence value) {
        try { return Double.parseDouble(text(value).replace(',', '.')); }
        catch (Exception e) { return 0; }
    }

    private double safe(Double value) { return value == null ? 0 : value; }
    private String text(CharSequence value) { return value == null ? "" : value.toString().trim(); }
    private boolean empty(String value) { return value == null || value.trim().isEmpty(); }
    private String value(String value) { return empty(value) ? "Không tên" : value; }

    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        activeMealForm = null;
    }
}
