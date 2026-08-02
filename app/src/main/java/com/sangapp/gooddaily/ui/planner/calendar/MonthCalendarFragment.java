package com.sangapp.gooddaily.ui.planner.calendar;

import com.sangapp.gooddaily.ui.featurehub.FeatureManagerFragment;
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
import androidx.navigation.Navigation;

import com.google.android.material.card.MaterialCardView;
import com.sangapp.gooddaily.R;
import com.sangapp.gooddaily.data.local.GoodDailyDatabase;
import com.sangapp.gooddaily.data.local.entity.DailyNoteEntity;
import com.sangapp.gooddaily.data.local.entity.MealEntity;
import com.sangapp.gooddaily.data.local.entity.PersonalRecordEntity;
import com.sangapp.gooddaily.data.local.entity.ScheduleBlockEntity;
import com.sangapp.gooddaily.data.local.entity.StudySessionEntity;
import com.sangapp.gooddaily.data.local.entity.TaskEntity;
import com.sangapp.gooddaily.data.local.entity.TransactionEntity;
import com.sangapp.gooddaily.data.local.prefs.LocalUserStore;
import com.sangapp.gooddaily.databinding.FragmentMonthCalendarBinding;
import com.sangapp.gooddaily.feature.FeatureCatalog;
import com.sangapp.gooddaily.util.AppExecutors;
import com.sangapp.gooddaily.util.DateUtils;
import com.sangapp.gooddaily.util.LunarCalendarUtils;
import com.sangapp.gooddaily.util.MoneyUtils;
import com.sangapp.gooddaily.util.ThemeUtils;
import com.sangapp.gooddaily.ui.planner.widget.BusyMonthCalendarView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

public class MonthCalendarFragment extends Fragment {
    private FragmentMonthCalendarBinding binding;
    private String selectedDateKey = DateUtils.dateKey();

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMonthCalendarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        LocalUserStore store = new LocalUserStore(requireContext());
        ThemeUtils.tintFilledButton(binding.btnOpenPlannerForDate, requireContext(), store.getThemeKey());
        ThemeUtils.tintTonalButton(binding.btnAddCalendarEvent, requireContext(), store.getThemeKey());
        binding.monthCalendarView.setSelectedDate(selectedDateKey);
        binding.tvCalendarMonthTitle.setText(binding.monthCalendarView.getDisplayMonthTitle());
        binding.monthCalendarView.setOnDateSelectedListener(dateKey -> {
            selectedDateKey = dateKey;
            binding.tvCalendarMonthTitle.setText(binding.monthCalendarView.getDisplayMonthTitle());
            loadSelectedDate();
        });
        binding.monthCalendarView.setOnMonthChangedListener((year, month) -> {
            binding.tvCalendarMonthTitle.setText(binding.monthCalendarView.getDisplayMonthTitle());
            loadMonthBusyDays(year, month);
        });
        binding.btnCalendarPreviousMonth.setOnClickListener(v -> binding.monthCalendarView.previousMonth());
        binding.btnCalendarNextMonth.setOnClickListener(v -> binding.monthCalendarView.nextMonth());
        binding.calendarModeToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            boolean showCalendar = checkedId != R.id.btnCalendarDayMode;
            binding.monthCalendarHeader.setVisibility(showCalendar ? View.VISIBLE : View.GONE);
            binding.monthCalendarView.setVisibility(showCalendar ? View.VISIBLE : View.GONE);
            binding.tvCalendarDotLegend.setVisibility(showCalendar ? View.VISIBLE : View.GONE);
            loadSelectedDate();
        });
        binding.btnOpenPlannerForDate.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("dateKey", selectedDateKey);
            args.putString("focus", "tasks");
            Navigation.findNavController(v).navigate(R.id.plannerFragment, args);
        });
        binding.btnAddCalendarEvent.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString(FeatureManagerFragment.ARG_FEATURE, FeatureCatalog.PLAN_EVENT);
            Navigation.findNavController(v).navigate(R.id.featureManagerFragment, args);
        });
        loadSelectedDate();
        loadMonthBusyDays(binding.monthCalendarView.getDisplayYear(), binding.monthCalendarView.getDisplayMonth());
    }

    private void loadMonthBusyDays(int year, int monthZeroBased) {
        Calendar start = Calendar.getInstance();
        start.clear();
        start.set(year, monthZeroBased, 1, 0, 0, 0);
        Calendar end = (Calendar) start.clone();
        end.add(Calendar.MONTH, 1);
        end.add(Calendar.MILLISECOND, -1);
        String startKey = DateUtils.dateKey(start.getTimeInMillis());
        String endKey = DateUtils.dateKey(end.getTimeInMillis());
        long startMillis = start.getTimeInMillis();
        long endMillis = end.getTimeInMillis();
        AppExecutors.io().execute(() -> {
            GoodDailyDatabase db = GoodDailyDatabase.get(requireContext());
            Map<String, Integer> busy = new HashMap<>();
            for (TaskEntity item : db.plannerDao().getTasksSync()) if (inside(item.dateKey, startKey, endKey)) addBusy(busy, item.dateKey, BusyMonthCalendarView.DOT_PLAN);
            for (ScheduleBlockEntity item : db.plannerDao().getSchedulesSync()) if (inside(item.dateKey, startKey, endKey)) addBusy(busy, item.dateKey, BusyMonthCalendarView.DOT_PLAN);
            for (StudySessionEntity item : db.plannerDao().getStudiesSync()) if (inside(item.dateKey, startKey, endKey)) addBusy(busy, item.dateKey, BusyMonthCalendarView.DOT_STUDY);
            for (DailyNoteEntity item : db.plannerDao().getNotesSync()) if (inside(item.dateKey, startKey, endKey) && item.content != null && !item.content.trim().isEmpty()) addBusy(busy, item.dateKey, BusyMonthCalendarView.DOT_JOURNAL);
            for (TransactionEntity item : db.transactionDao().getByRangeSync(startMillis, endMillis)) addBusy(busy, DateUtils.dateKey(item.transactionTime), BusyMonthCalendarView.DOT_FINANCE);
            for (MealEntity item : db.healthDao().getMealsByRangeSync(startMillis, endMillis)) addBusy(busy, DateUtils.dateKey(item.eatenAt), BusyMonthCalendarView.DOT_JOURNAL);
            for (PersonalRecordEntity item : db.advancedRecordDao().getRangeSync(startKey, endKey)) {
                int mask = "FINANCE".equals(item.module) ? BusyMonthCalendarView.DOT_FINANCE
                        : "LEARNING".equals(item.module) ? BusyMonthCalendarView.DOT_STUDY
                        : ("JOURNAL".equals(item.module) || "HEALTH".equals(item.module)) ? BusyMonthCalendarView.DOT_JOURNAL
                        : BusyMonthCalendarView.DOT_PLAN;
                addBusy(busy, item.dateKey, mask);
            }
            if (isAdded()) requireActivity().runOnUiThread(() -> {
                if (binding != null && binding.monthCalendarView.getDisplayYear() == year
                        && binding.monthCalendarView.getDisplayMonth() == monthZeroBased) {
                    binding.monthCalendarView.setBusyDays(busy);
                }
            });
        });
    }

    private boolean inside(String key, String start, String end) {
        return key != null && key.compareTo(start) >= 0 && key.compareTo(end) <= 0;
    }

    private void addBusy(Map<String, Integer> values, String key, int mask) {
        if (key == null || key.trim().isEmpty()) return;
        values.put(key, values.getOrDefault(key, 0) | mask);
    }

    private void loadSelectedDate() {
        if (binding == null) return;
        long dayStart = DateUtils.parseDateKey(selectedDateKey);
        long dayEnd = dayStart + 24L * 60 * 60 * 1000 - 1;
        AppExecutors.io().execute(() -> {
            GoodDailyDatabase db = GoodDailyDatabase.get(requireContext());
            List<TaskEntity> tasks = db.plannerDao().getTasksByDateSync(selectedDateKey);
            List<StudySessionEntity> studies = db.plannerDao().getStudiesByDateSync(selectedDateKey);
            List<ScheduleBlockEntity> schedules = db.plannerDao().getSchedulesByDateSync(selectedDateKey);
            DailyNoteEntity note = db.plannerDao().getNoteByDateSync(selectedDateKey);
            List<TransactionEntity> transactions = db.transactionDao().getByRangeSync(dayStart, dayEnd);
            List<MealEntity> meals = db.healthDao().getMealsByRangeSync(dayStart, dayEnd);
            List<PersonalRecordEntity> advanced = db.advancedRecordDao().getModuleSync("PLANNER");
            List<PersonalRecordEntity> dayAdvanced = new ArrayList<>();
            for (PersonalRecordEntity item : advanced) if (selectedDateKey.equals(item.dateKey)) dayAdvanced.add(item);
            requireActivity().runOnUiThread(() -> render(tasks, studies, schedules, note, transactions, meals, dayAdvanced));
        });
    }

    private void render(List<TaskEntity> tasks, List<StudySessionEntity> studies, List<ScheduleBlockEntity> schedules,
                        DailyNoteEntity note, List<TransactionEntity> transactions, List<MealEntity> meals,
                        List<PersonalRecordEntity> advanced) {
        if (binding == null) return;
        long millis = DateUtils.parseDateKey(selectedDateKey);
        String solar = new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("vi", "VN")).format(new Date(millis));
        binding.tvCalendarSelectedDate.setText(capitalize(solar));
        binding.tvCalendarLunar.setText("Âm lịch: " + LunarCalendarUtils.formatLunar(selectedDateKey));
        int completed = 0;
        for (TaskEntity task : tasks) if (task.completed) completed++;
        double income = 0, expense = 0, calories = 0;
        for (TransactionEntity t : transactions) { if ("INCOME".equals(t.type)) income += t.amount; else expense += t.amount; }
        for (MealEntity m : meals) calories += m.calories;
        int studyMinutes = 0;
        for (StudySessionEntity s : studies) studyMinutes += s.minutes;
        binding.tvCalendarSummary.setText("Công việc: " + completed + "/" + tasks.size()
                + " · Lịch: " + schedules.size()
                + "\nHọc: " + studyMinutes + " phút · Kcal: " + String.format(Locale.US, "%.0f", calories)
                + "\nThu: " + MoneyUtils.format(income) + " · Chi: " + MoneyUtils.format(expense)
                + (note != null && note.content != null && !note.content.trim().isEmpty() ? "\nCó nhật ký trong ngày" : ""));

        binding.calendarDetailContainer.removeAllViews();
        for (ScheduleBlockEntity item : schedules) addDetail("🕒 " + item.title, time(item.startMinutes) + "–" + time(item.endMinutes) + " · " + item.category);
        for (TaskEntity item : tasks) addDetail(item.completed ? "✓ " + item.title : "○ " + item.title, item.expectedMinutes + " phút dự kiến");
        for (StudySessionEntity item : studies) addDetail("📘 " + item.subject, item.minutes + " phút" + suffix(item.note));
        for (TransactionEntity item : transactions) addDetail("INCOME".equals(item.type) ? "＋ " + item.category : "－ " + item.category, MoneyUtils.format(item.amount) + suffix(item.note));
        for (MealEntity item : meals) addDetail("🍽 " + item.name, String.format(Locale.US, "%.0f kcal · %.1f g protein", item.calories, item.protein));
        for (PersonalRecordEntity item : advanced) addDetail("◆ " + item.title, item.details == null ? "" : item.details);
        if (note != null && note.content != null && !note.content.trim().isEmpty()) addDetail("📝 Nhật ký", note.content);
        if (binding.calendarDetailContainer.getChildCount() == 0) binding.calendarDetailContainer.addView(empty("Ngày này chưa có dữ liệu."));
    }

    private void addDetail(String title, String details) {
        MaterialCardView card = new MaterialCardView(requireContext());
        card.setRadius(dp(16)); card.setCardElevation(0); card.setStrokeWidth(dp(1));
        card.setStrokeColor(getResources().getColor(R.color.outline, requireContext().getTheme()));
        card.setCardBackgroundColor(getResources().getColor(R.color.surface, requireContext().getTheme()));
        LinearLayout wrap = new LinearLayout(requireContext()); wrap.setOrientation(LinearLayout.VERTICAL); wrap.setPadding(dp(14),dp(13),dp(14),dp(13));
        TextView t = text(title, 15, true); TextView d = text(details, 13, false); d.setTextColor(getResources().getColor(R.color.on_surface_variant, requireContext().getTheme())); d.setPadding(0,dp(4),0,0);
        wrap.addView(t); if (details != null && !details.trim().isEmpty()) wrap.addView(d); card.addView(wrap);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); p.bottomMargin=dp(8); binding.calendarDetailContainer.addView(card,p);
    }
    private TextView empty(String value) { TextView v=text(value,14,false); v.setGravity(Gravity.CENTER); v.setTextColor(getResources().getColor(R.color.on_surface_variant, requireContext().getTheme())); v.setPadding(dp(20),dp(24),dp(20),dp(24)); return v; }
    private TextView text(String value,int sp,boolean bold){TextView v=new TextView(requireContext());v.setText(value);v.setTextSize(sp);v.setTextColor(getResources().getColor(R.color.on_surface,requireContext().getTheme()));if(bold)v.setTypeface(v.getTypeface(),android.graphics.Typeface.BOLD);return v;}
    private String suffix(String s){return s==null||s.trim().isEmpty()?"":" · "+s;}
    private String time(int min){return String.format(Locale.US,"%02d:%02d",min/60,min%60);}
    private String capitalize(String s){return s==null||s.isEmpty()?s:Character.toUpperCase(s.charAt(0))+s.substring(1);}
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
    @Override public void onDestroyView(){super.onDestroyView();binding=null;}
}
