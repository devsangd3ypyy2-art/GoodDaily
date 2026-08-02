package com.sangapp.gooddaily.ui.advanced;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sangapp.gooddaily.data.local.GoodDailyDatabase;
import com.sangapp.gooddaily.data.local.entity.BodyRecordEntity;
import com.sangapp.gooddaily.data.local.entity.MealEntity;
import com.sangapp.gooddaily.databinding.FragmentHealthReportBinding;
import com.sangapp.gooddaily.util.AppExecutors;
import com.sangapp.gooddaily.util.DateUtils;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

public class HealthReportFragment extends Fragment {
    private FragmentHealthReportBinding binding;
    private GoodDailyDatabase db;
    private int rangeDays = 30;
    private List<BodyRecordEntity> allBodies;
    private List<MealEntity> rangeMeals;

    private final ActivityResultLauncher<String> createCsv = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("text/csv"),
            uri -> { if (uri != null) writeCsv(uri); });

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
                                                   @Nullable ViewGroup container,
                                                   @Nullable Bundle savedInstanceState) {
        binding = FragmentHealthReportBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        db = GoodDailyDatabase.get(requireContext());
        binding.rangeGroup.check(binding.btnMonth.getId());
        binding.rangeGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == binding.btnWeek.getId()) rangeDays = 7;
            else if (checkedId == binding.btnYear.getId()) rangeDays = 365;
            else rangeDays = 30;
            load();
        });
        binding.btnExportCsv.setOnClickListener(v ->
                createCsv.launch("GoodDaily_Health_" + DateUtils.dateKey() + ".csv"));
        load();
    }

    private void load() {
        AppExecutors.database().execute(() -> {
            long end = DateUtils.endOfDay();
            long start = DateUtils.startOfDaysAgo(rangeDays - 1);
            List<BodyRecordEntity> bodies = db.healthDao().getBodiesSync();
            List<MealEntity> meals = db.healthDao().getMealsByRangeSync(start, end);
            requireActivity().runOnUiThread(() -> render(bodies, meals));
        });
    }

    private void render(List<BodyRecordEntity> bodies, List<MealEntity> meals) {
        if (binding == null) return;
        allBodies = bodies;
        rangeMeals = meals;
        binding.weightChart.setItems(bodies);
        double kcal = 0, protein = 0, carbs = 0, fat = 0;
        for (MealEntity meal : meals) {
            kcal += meal.calories;
            protein += meal.protein;
            carbs += meal.carbs;
            fat += meal.fat;
        }
        BodyRecordEntity latest = bodies == null || bodies.isEmpty() ? null : bodies.get(0);
        BodyRecordEntity oldest = bodies == null || bodies.isEmpty() ? null : bodies.get(bodies.size() - 1);
        double change = latest == null || oldest == null ? 0 : latest.weight - oldest.weight;
        binding.tvSummary.setText(String.format(Locale.getDefault(),
                "Khoảng thời gian: %d ngày\nCân nặng gần nhất: %s\nThay đổi toàn bộ lịch sử: %+.1f kg\nTổng năng lượng: %.0f kcal\nProtein: %.1f g · Carb: %.1f g · Chất béo: %.1f g\nSố bữa đã ghi: %d",
                rangeDays, latest == null ? "Chưa có" : String.format(Locale.getDefault(), "%.1f kg", latest.weight),
                change, kcal, protein, carbs, fat, meals.size()));
    }

    private void writeCsv(Uri uri) {
        AppExecutors.database().execute(() -> {
            try (OutputStream out = requireContext().getContentResolver().openOutputStream(uri)) {
                if (out == null) throw new IllegalStateException("Không mở được file");
                StringBuilder csv = new StringBuilder("sep=,\nLOAI,NGAY,TEN,CAN_NANG,CHIEU_CAO,KCAL,PROTEIN,CARB,FAT\n");
                if (allBodies != null) for (BodyRecordEntity b : allBodies) {
                    csv.append("CO_THE,").append(DateUtils.formatDateTime(b.recordedAt)).append(",,")
                            .append(b.weight).append(',').append(b.height).append(",,,,\n");
                }
                if (rangeMeals != null) for (MealEntity m : rangeMeals) {
                    csv.append("BUA_AN,").append(DateUtils.formatDateTime(m.eatenAt)).append(',')
                            .append(quote(m.name)).append(",,,").append(m.calories).append(',')
                            .append(m.protein).append(',').append(m.carbs).append(',').append(m.fat).append('\n');
                }
                out.write(csv.toString().getBytes(StandardCharsets.UTF_8));
                show("Đã xuất báo cáo CSV.");
            } catch (Exception e) {
                show("Không thể xuất: " + e.getMessage());
            }
        });
    }

    private String quote(String value) {
        String safe = value == null ? "" : value.replace("\"", "\"\"");
        return '"' + safe + '"';
    }

    private void show(String message) {
        if (!isAdded()) return;
        requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show());
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
