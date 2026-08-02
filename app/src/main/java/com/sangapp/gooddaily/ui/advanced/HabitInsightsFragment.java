package com.sangapp.gooddaily.ui.advanced;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.sangapp.gooddaily.R;
import com.sangapp.gooddaily.data.local.GoodDailyDatabase;
import com.sangapp.gooddaily.data.local.entity.HabitCheckInEntity;
import com.sangapp.gooddaily.data.local.entity.HabitEntity;
import com.sangapp.gooddaily.databinding.FragmentHabitInsightsBinding;
import com.sangapp.gooddaily.util.DateUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HabitInsightsFragment extends Fragment {
    private FragmentHabitInsightsBinding binding;
    private List<HabitEntity> habits = new ArrayList<>();
    private List<HabitCheckInEntity> checkIns = new ArrayList<>();

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                                   @Nullable Bundle savedInstanceState) {
        binding = FragmentHabitInsightsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        GoodDailyDatabase db = GoodDailyDatabase.get(requireContext());
        db.habitDao().observeHabits().observe(getViewLifecycleOwner(), items -> {
            habits = items == null ? new ArrayList<>() : new ArrayList<>(items); render();
        });
        db.habitDao().observeAllCheckIns().observe(getViewLifecycleOwner(), items -> {
            checkIns = items == null ? new ArrayList<>() : new ArrayList<>(items); render();
        });
    }

    private void render() {
        if (binding == null) return;
        binding.heatmap.setCheckIns(checkIns);
        int last7 = countDistinctDays(7), last30 = countDistinctDays(30);
        int possible7 = Math.max(1, habits.size() * 7);
        int possible30 = Math.max(1, habits.size() * 30);
        int entries7 = countEntries(7), entries30 = countEntries(30);
        binding.tvOverall.setText(String.format(Locale.getDefault(),
                "7 ngày: %d lượt (%.0f%%) · 30 ngày: %d lượt (%.0f%%)\nCó hoạt động trong %d/7 ngày và %d/30 ngày.",
                entries7, entries7 * 100d / possible7, entries30, entries30 * 100d / possible30,
                last7, last30));
        binding.habitStatsContainer.removeAllViews();
        if (habits.isEmpty()) {
            binding.habitStatsContainer.addView(text("Chưa có thói quen nào."));
            return;
        }
        for (HabitEntity habit : habits) {
            Set<String> dates = datesFor(habit.id);
            int current = streak(dates, DateUtils.dateKey());
            int longest = longest(dates);
            int month = 0;
            String monthStart = DateUtils.shiftDateKey(DateUtils.dateKey(), -29);
            for (String date : dates) if (date.compareTo(monthStart) >= 0) month++;
            MaterialCardView card = new MaterialCardView(requireContext());
            card.setCardBackgroundColor(getResources().getColor(R.color.surface, requireContext().getTheme()));
            card.setStrokeColor(getResources().getColor(R.color.outline, requireContext().getTheme()));
            card.setStrokeWidth(dp(1)); card.setRadius(dp(18)); card.setCardElevation(0);
            LinearLayout wrap = new LinearLayout(requireContext()); wrap.setOrientation(LinearLayout.VERTICAL); wrap.setPadding(dp(15),dp(15),dp(15),dp(15));
            TextView title = text(habit.name); title.setTextSize(16); title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);
            TextView detail = text(String.format(Locale.getDefault(), "🔥 Hiện tại %d ngày · Dài nhất %d ngày · 30 ngày: %d lần", current, longest, month));
            detail.setTextColor(getResources().getColor(R.color.on_surface_variant, requireContext().getTheme())); detail.setPadding(0,dp(6),0,0);
            wrap.addView(title); wrap.addView(detail); card.addView(wrap);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); lp.bottomMargin=dp(9);
            binding.habitStatsContainer.addView(card,lp);
        }
    }

    private int countEntries(int days) {
        String start = DateUtils.shiftDateKey(DateUtils.dateKey(), -(days - 1));
        int count=0; for(HabitCheckInEntity c:checkIns) if(c.dateKey!=null&&c.dateKey.compareTo(start)>=0) count++; return count;
    }
    private int countDistinctDays(int days) {
        String start=DateUtils.shiftDateKey(DateUtils.dateKey(),-(days-1)); Set<String>s=new HashSet<>();
        for(HabitCheckInEntity c:checkIns)if(c.dateKey!=null&&c.dateKey.compareTo(start)>=0)s.add(c.dateKey);return s.size();
    }
    private Set<String> datesFor(long id){Set<String>s=new HashSet<>();for(HabitCheckInEntity c:checkIns)if(c.habitId==id&&c.dateKey!=null)s.add(c.dateKey);return s;}
    private int streak(Set<String> dates,String end){int n=0;String key=end;while(dates.contains(key)){n++;key=DateUtils.shiftDateKey(key,-1);}return n;}
    private int longest(Set<String> dates){int best=0;for(String d:dates){if(!dates.contains(DateUtils.shiftDateKey(d,-1))){int n=0,k=0;String x=d;while(dates.contains(x)&&k<5000){n++;k++;x=DateUtils.shiftDateKey(x,1);}best=Math.max(best,n);}}return best;}
    private TextView text(String s){TextView v=new TextView(requireContext());v.setText(s);v.setTextSize(14);v.setTextColor(getResources().getColor(R.color.on_surface,requireContext().getTheme()));v.setGravity(Gravity.START);return v;}
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
    @Override public void onDestroyView(){super.onDestroyView();binding=null;}
}
