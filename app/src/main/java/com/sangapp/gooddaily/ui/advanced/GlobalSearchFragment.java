package com.sangapp.gooddaily.ui.advanced;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.card.MaterialCardView;
import com.sangapp.gooddaily.R;
import com.sangapp.gooddaily.data.local.GoodDailyDatabase;
import com.sangapp.gooddaily.data.local.entity.DailyNoteEntity;
import com.sangapp.gooddaily.data.local.entity.MealEntity;
import com.sangapp.gooddaily.data.local.entity.PersonalRecordEntity;
import com.sangapp.gooddaily.data.local.entity.TaskEntity;
import com.sangapp.gooddaily.data.local.entity.TransactionEntity;
import com.sangapp.gooddaily.databinding.FragmentGlobalSearchBinding;
import com.sangapp.gooddaily.util.AppExecutors;
import com.sangapp.gooddaily.util.MoneyUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GlobalSearchFragment extends Fragment {
    private FragmentGlobalSearchBinding binding;
    private int requestVersion;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGlobalSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.edtGlobalSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { search(s == null ? "" : s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void search(String raw) {
        String query = raw.trim().toLowerCase(Locale.ROOT);
        int version = ++requestVersion;
        if (query.length() < 2) {
            binding.globalSearchResultContainer.removeAllViews();
            binding.tvGlobalSearchStatus.setText("Nhập ít nhất 2 ký tự để tìm kiếm.");
            return;
        }
        binding.tvGlobalSearchStatus.setText("Đang tìm...");
        AppExecutors.io().execute(() -> {
            GoodDailyDatabase db = GoodDailyDatabase.get(requireContext());
            List<Result> results = new ArrayList<>();
            for (TransactionEntity item : db.transactionDao().getAllSync()) {
                if (contains(query, item.category, item.note, item.account)) results.add(new Result("Tài chính", item.category, MoneyUtils.format(item.amount) + suffix(item.note), R.id.financeFragment, null));
            }
            for (TaskEntity item : db.plannerDao().getTasksSync()) {
                if (contains(query, item.title, item.dateKey)) results.add(new Result("Công việc", item.title, item.dateKey + (item.completed ? " · hoàn thành" : ""), R.id.plannerFragment, null));
            }
            for (DailyNoteEntity item : db.plannerDao().getNotesSync()) {
                if (contains(query, item.content, item.tags, item.mood)) results.add(new Result("Nhật ký", item.dateKey, trim(item.content, 120), R.id.plannerFragment, null));
            }
            for (MealEntity item : db.healthDao().getMealsSync()) {
                if (contains(query, item.name)) results.add(new Result("Bữa ăn", item.name, String.format(Locale.US, "%.0f kcal · %.1f g protein", item.calories, item.protein), R.id.healthFragment, null));
            }
            for (PersonalRecordEntity item : db.advancedRecordDao().getModuleSync("FINANCE")) addAdvanced(query, results, item);
            for (PersonalRecordEntity item : db.advancedRecordDao().getModuleSync("HEALTH")) addAdvanced(query, results, item);
            for (PersonalRecordEntity item : db.advancedRecordDao().getModuleSync("PLANNER")) addAdvanced(query, results, item);
            for (PersonalRecordEntity item : db.advancedRecordDao().getModuleSync("LEARNING")) addAdvanced(query, results, item);
            for (PersonalRecordEntity item : db.advancedRecordDao().getModuleSync("HABIT")) addAdvanced(query, results, item);
            for (PersonalRecordEntity item : db.advancedRecordDao().getModuleSync("GOAL")) addAdvanced(query, results, item);
            for (PersonalRecordEntity item : db.advancedRecordDao().getModuleSync("JOURNAL")) addAdvanced(query, results, item);
            for (PersonalRecordEntity item : db.advancedRecordDao().getModuleSync("DRIVER")) addAdvanced(query, results, item);
            for (PersonalRecordEntity item : db.advancedRecordDao().getModuleSync("REMINDER")) addAdvanced(query, results, item);
            if (results.size() > 100) results = new ArrayList<>(results.subList(0, 100));
            List<Result> finalResults = results;
            requireActivity().runOnUiThread(() -> { if (version == requestVersion) render(finalResults); });
        });
    }

    private void addAdvanced(String query, List<Result> results, PersonalRecordEntity item) {
        if (contains(query, item.title, item.details, item.tags, item.status)) {
            results.add(new Result(item.module, item.title, trim(item.details, 120), R.id.featureManagerFragment, item.feature));
        }
    }

    private void render(List<Result> results) {
        if (binding == null) return;
        binding.globalSearchResultContainer.removeAllViews();
        binding.tvGlobalSearchStatus.setText(results.isEmpty() ? "Không tìm thấy dữ liệu phù hợp." : "Tìm thấy " + results.size() + " kết quả.");
        for (Result result : results) {
            MaterialCardView card = new MaterialCardView(requireContext());
            card.setRadius(dp(18)); card.setCardElevation(0); card.setStrokeWidth(dp(1));
            card.setStrokeColor(getResources().getColor(R.color.outline, requireContext().getTheme()));
            card.setCardBackgroundColor(getResources().getColor(R.color.surface, requireContext().getTheme()));
            LinearLayout wrap = new LinearLayout(requireContext()); wrap.setOrientation(LinearLayout.VERTICAL); wrap.setPadding(dp(15),dp(14),dp(15),dp(14));
            TextView module = label(result.module,12,false); module.setTextColor(getResources().getColor(R.color.on_surface_variant, requireContext().getTheme()));
            TextView title = label(result.title,16,true);
            TextView details = label(result.details,13,false); details.setTextColor(getResources().getColor(R.color.on_surface_variant, requireContext().getTheme()));
            details.setPadding(0,dp(4),0,0);
            wrap.addView(module); wrap.addView(title); if (!result.details.isEmpty()) wrap.addView(details); card.addView(wrap);
            card.setOnClickListener(v -> {
                if (result.feature != null) {
                    Bundle args = new Bundle(); args.putString(FeatureManagerFragment.ARG_FEATURE, result.feature);
                    Navigation.findNavController(v).navigate(result.destination, args);
                } else Navigation.findNavController(v).navigate(result.destination);
            });
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); p.bottomMargin=dp(9); binding.globalSearchResultContainer.addView(card,p);
        }
    }

    private boolean contains(String query, String... values) { for (String v : values) if (v != null && v.toLowerCase(Locale.ROOT).contains(query)) return true; return false; }
    private String suffix(String s) { return s == null || s.trim().isEmpty() ? "" : " · " + s; }
    private String trim(String s, int max) { if (s == null) return ""; s=s.trim(); return s.length()<=max?s:s.substring(0,max)+"…"; }
    private TextView label(String value,int sp,boolean bold){TextView v=new TextView(requireContext());v.setText(value);v.setTextSize(sp);v.setTextColor(getResources().getColor(R.color.on_surface,requireContext().getTheme()));if(bold)v.setTypeface(v.getTypeface(),android.graphics.Typeface.BOLD);return v;}
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
    private static class Result { final String module,title,details; final int destination; final String feature; Result(String module,String title,String details,int destination,String feature){this.module=module;this.title=title;this.details=details;this.destination=destination;this.feature=feature;} }
    @Override public void onDestroyView(){super.onDestroyView();binding=null;}
}
