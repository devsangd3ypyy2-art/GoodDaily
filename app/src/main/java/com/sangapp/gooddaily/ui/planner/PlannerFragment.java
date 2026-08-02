package com.sangapp.gooddaily.ui.planner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.sangapp.gooddaily.R;
import com.sangapp.gooddaily.data.local.entity.DailyLearningEntity;
import com.sangapp.gooddaily.data.local.entity.DailyNoteEntity;
import com.sangapp.gooddaily.data.local.entity.HabitCheckInEntity;
import com.sangapp.gooddaily.data.local.entity.HabitEntity;
import com.sangapp.gooddaily.data.local.entity.ScheduleBlockEntity;
import com.sangapp.gooddaily.data.local.entity.StudySessionEntity;
import com.sangapp.gooddaily.data.local.entity.TaskEntity;
import com.sangapp.gooddaily.data.local.prefs.LocalUserStore;
import com.sangapp.gooddaily.databinding.BottomSheetScheduleBinding;
import com.sangapp.gooddaily.databinding.DialogAddHabitBinding;
import com.sangapp.gooddaily.databinding.DialogAddStudyBinding;
import com.sangapp.gooddaily.databinding.DialogAddTaskBinding;
import com.sangapp.gooddaily.databinding.DialogVocabularyGoalBinding;
import com.sangapp.gooddaily.databinding.FragmentPlannerBinding;
import com.sangapp.gooddaily.util.DateUtils;
import com.sangapp.gooddaily.util.LunarCalendarUtils;
import com.sangapp.gooddaily.util.ThemeUtils;
import com.sangapp.gooddaily.viewmodel.PlannerViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class PlannerFragment extends Fragment {
    private static final long POMODORO_MS = 25 * 60_000L;

    private FragmentPlannerBinding binding;
    private PlannerViewModel vm;
    private LocalUserStore userStore;
    private String selectedDate = DateUtils.dateKey();
    private List<HabitEntity> habits = new ArrayList<>();
    private List<HabitCheckInEntity> allCheckIns = new ArrayList<>();
    private final Set<Long> checkedHabitIds = new HashSet<>();
    private List<ScheduleBlockEntity> weekSchedules = new ArrayList<>();
    private List<ScheduleBlockEntity> currentSchedules = new ArrayList<>();
    private List<DailyNoteEntity> noteHistory = new ArrayList<>();
    private CountDownTimer pomodoroTimer;
    private long pomodoroRemaining = POMODORO_MS;
    private boolean pomodoroRunning;
    private int selectedDayStudyMinutes;
    private int selectedWeekStudyMinutes;

    private final ActivityResultLauncher<String> notificationPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            granted -> toast(granted ? "Đã cấp quyền nhắc thời gian biểu." : "Chưa cấp quyền thông báo.")
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPlannerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        vm = new ViewModelProvider(this).get(PlannerViewModel.class);
        userStore = new LocalUserStore(requireContext());
        applyTheme();
        setupMoodDropdown();
        setupActions();
        observeData();
        renderPomodoro();
        scrollToRequestedSection();
    }

    private void scrollToRequestedSection() {
        Bundle args = getArguments();
        String focus = args == null ? "" : args.getString("focus", "");
        binding.plannerScroll.post(() -> {
            View target;
            if ("learning".equals(focus)) target = binding.cardLearning;
            else if ("habits".equals(focus)) target = binding.cardHabits;
            else if ("tasks".equals(focus)) target = binding.cardTasks;
            else target = binding.cardWeekCalendar;
            binding.plannerScroll.smoothScrollTo(0, target.getTop());
        });
    }

    private void applyTheme() {
        String theme = userStore.getThemeKey();
        ThemeUtils.tintTonalButton(binding.btnReminderManager, requireContext(), theme);
        ThemeUtils.tintTonalButton(binding.btnOpenCalendar, requireContext(), theme);
        ThemeUtils.tintTonalButton(binding.btnAddSchedule, requireContext(), theme);
        binding.btnCopySchedule.setIconTint(ColorStateList.valueOf(ThemeUtils.getPrimaryColor(requireContext(), theme)));
        ThemeUtils.tintFilledButton(binding.btnStartPomodoro, requireContext(), theme);
        ThemeUtils.tintFilledButton(binding.btnSaveLearning, requireContext(), theme);
        ThemeUtils.tintFilledButton(binding.btnSaveNote, requireContext(), theme);
        ThemeUtils.tintTonalButton(binding.btnAddStudy, requireContext(), theme);
        binding.vocabularyProgress.setIndicatorColor(ThemeUtils.getPrimaryColor(requireContext(), theme));
        int accent = ThemeUtils.getPrimaryColor(requireContext(), theme);
        binding.btnAddTask.setTextColor(accent);
        binding.btnAddTask.setIconTint(ColorStateList.valueOf(accent));
        binding.btnAddHabit.setTextColor(accent);
        binding.btnAddHabit.setIconTint(ColorStateList.valueOf(accent));
    }

    private void setupMoodDropdown() {
        String[] moods = {"Bình thường", "Vui", "Tích cực", "Mệt", "Căng thẳng", "Buồn"};
        binding.dropdownMood.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, moods));
        binding.dropdownMood.setText(moods[0], false);
    }

    private void setupActions() {
        binding.btnReminderManager.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.reminderManagerFragment));
        binding.btnOpenCalendar.setOnClickListener(v -> showDatePicker());
        binding.selectedDateHeader.setOnClickListener(v -> showDatePicker());
        binding.btnToday.setOnClickListener(v -> vm.selectDate(DateUtils.dateKey()));
        binding.btnPreviousWeek.setOnClickListener(v -> vm.selectDate(DateUtils.shiftDateKey(selectedDate, -7)));
        binding.btnNextWeek.setOnClickListener(v -> vm.selectDate(DateUtils.shiftDateKey(selectedDate, 7)));
        binding.btnAddSchedule.setOnClickListener(v -> showScheduleSheet(null));
        binding.btnCopySchedule.setOnClickListener(v -> showCopySchedulePicker());
        binding.btnAddTask.setOnClickListener(v -> showTaskDialog());
        binding.btnAddStudy.setOnClickListener(v -> showStudyDialog());
        binding.btnAddHabit.setOnClickListener(v -> showHabitDialog());
        binding.btnSaveLearning.setOnClickListener(v -> saveLearning());
        binding.tvWeeklyLearning.setOnClickListener(v -> showVocabularyGoalDialog());
        binding.vocabularyProgress.setOnClickListener(v -> showVocabularyGoalDialog());
        binding.btnStartPomodoro.setOnClickListener(v -> togglePomodoro());
        binding.btnResetPomodoro.setOnClickListener(v -> resetPomodoro());
        binding.btnSaveNote.setOnClickListener(v -> saveNote());
        binding.btnPlannerAdvanced.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.featureHubFragment));
        binding.btnNoteHistory.setOnClickListener(v -> showNoteHistory());
    }

    private void observeData() {
        vm.selectedDate().observe(getViewLifecycleOwner(), dateKey -> {
            selectedDate = TextUtils.isEmpty(dateKey) ? DateUtils.dateKey() : dateKey;
            binding.tvPlannerDate.setText(DateUtils.formatDateKey(selectedDate));
            binding.tvPlannerMonth.setText(DateUtils.formatMonthYear(selectedDate));
            binding.tvLunarDate.setText(LunarCalendarUtils.formatLunar(selectedDate));
            binding.edtDailyNote.clearFocus();
            binding.edtNoteTags.clearFocus();
            binding.edtVocabularyCount.clearFocus();
            binding.edtMockScore.clearFocus();
            binding.tvNoteDateHint.setText("Nhật ký ngày " + DateUtils.formatCompactDateKey(selectedDate));
            renderWeekCalendar();
        });

        vm.schedules().observe(getViewLifecycleOwner(), this::renderSchedules);
        vm.weekSchedules().observe(getViewLifecycleOwner(), items -> {
            weekSchedules = items == null ? new ArrayList<>() : items;
            renderWeekCalendar();
        });
        vm.tasks().observe(getViewLifecycleOwner(), this::renderTasks);
        vm.studies().observe(getViewLifecycleOwner(), this::renderStudies);
        vm.studyMinutes().observe(getViewLifecycleOwner(), value -> {
            selectedDayStudyMinutes = value == null ? 0 : value;
            renderStudySummary();
        });
        vm.learning().observe(getViewLifecycleOwner(), this::renderLearning);
        vm.weekVocabularyCount().observe(getViewLifecycleOwner(), this::renderWeeklyLearning);
        vm.weekStudyMinutes().observe(getViewLifecycleOwner(), value -> {
            selectedWeekStudyMinutes = value == null ? 0 : value;
            renderStudySummary();
        });
        vm.habits().observe(getViewLifecycleOwner(), value -> {
            habits = value == null ? new ArrayList<>() : value;
            renderHabits();
        });
        vm.checkIns().observe(getViewLifecycleOwner(), value -> {
            checkedHabitIds.clear();
            if (value != null) for (HabitCheckInEntity item : value) checkedHabitIds.add(item.habitId);
            renderHabits();
        });
        vm.allCheckIns().observe(getViewLifecycleOwner(), value -> {
            allCheckIns = value == null ? new ArrayList<>() : value;
            renderHabits();
        });
        vm.note().observe(getViewLifecycleOwner(), note -> {
            if (binding == null || binding.edtDailyNote.hasFocus()) return;
            binding.edtDailyNote.setText(note == null ? "" : safe(note.content));
            binding.edtNoteTags.setText(note == null ? "" : safe(note.tags));
            binding.dropdownMood.setText(note == null || TextUtils.isEmpty(note.mood) ? "Bình thường" : note.mood, false);
        });
        vm.noteHistory().observe(getViewLifecycleOwner(), value ->
                noteHistory = value == null ? new ArrayList<>() : value);
    }

    private void renderWeekCalendar() {
        if (binding == null) return;
        binding.weekContainer.removeAllViews();
        String weekStart = DateUtils.startOfWeekKey(selectedDate);
        int accent = ThemeUtils.getPrimaryColor(requireContext(), userStore.getThemeKey());
        int container = ThemeUtils.getContainerColor(requireContext(), userStore.getThemeKey());

        Map<String, Set<String>> categories = new HashMap<>();
        for (ScheduleBlockEntity block : weekSchedules) {
            categories.computeIfAbsent(block.dateKey, key -> new HashSet<>()).add(block.category);
        }

        for (int i = 0; i < 7; i++) {
            String date = DateUtils.shiftDateKey(weekStart, i);
            boolean selected = date.equals(selectedDate);

            MaterialCardView card = new MaterialCardView(requireContext());
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(dp(68), ViewGroup.LayoutParams.WRAP_CONTENT);
            cardParams.setMargins(dp(3), 0, dp(3), 0);
            card.setLayoutParams(cardParams);
            card.setRadius(dp(18));
            card.setCardElevation(0);
            card.setStrokeWidth(selected ? dp(2) : dp(1));
            card.setStrokeColor(selected ? accent : ContextCompat.getColor(requireContext(), R.color.surface_variant));
            card.setCardBackgroundColor(selected ? container : ContextCompat.getColor(requireContext(), R.color.surface));
            card.setClickable(true);
            card.setFocusable(true);

            LinearLayout content = new LinearLayout(requireContext());
            content.setOrientation(LinearLayout.VERTICAL);
            content.setGravity(Gravity.CENTER);
            content.setPadding(dp(5), dp(9), dp(5), dp(9));

            TextView weekday = textView(DateUtils.formatShortWeekday(date), 11, R.color.on_surface_variant, false);
            TextView solar = textView(DateUtils.formatDayOfMonth(date), 20, R.color.on_surface, true);
            String lunar = LunarCalendarUtils.formatLunar(date).replace(" âm", "");
            TextView lunarText = textView(lunar, 10, R.color.on_surface_variant, false);
            TextView dots = textView(buildDots(categories.get(date)), 10, R.color.on_surface_variant, false);
            dots.setTextColor(categories.containsKey(date) ? accent : ContextCompat.getColor(requireContext(), R.color.transparent));

            content.addView(weekday);
            content.addView(solar);
            content.addView(lunarText);
            content.addView(dots);
            card.addView(content);
            card.setOnClickListener(v -> vm.selectDate(date));
            binding.weekContainer.addView(card);
        }
    }

    private String buildDots(Set<String> categories) {
        if (categories == null || categories.isEmpty()) return "·";
        return categories.size() > 1 ? "●  ●" : "●";
    }

    private void showCopySchedulePicker() {
        if (currentSchedules.isEmpty()) {
            toast("Ngày đang chọn chưa có thời gian biểu để sao chép.");
            return;
        }
        com.google.android.material.datepicker.MaterialDatePicker<Long> picker =
                com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker()
                        .setTitleText("Sao chép lịch sang ngày")
                        .setSelection(DateUtils.toUtcPickerMillis(DateUtils.shiftDateKey(selectedDate, 1)))
                        .build();
        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection == null) return;
            String target = DateUtils.dateKeyFromUtcPicker(selection);
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Sao chép " + currentSchedules.size() + " khối thời gian?")
                    .setMessage("Từ " + DateUtils.formatCompactDateKey(selectedDate) + " sang "
                            + DateUtils.formatCompactDateKey(target) + ". Dữ liệu ở ngày đích vẫn được giữ.")
                    .setNegativeButton("Hủy", null)
                    .setPositiveButton("Sao chép", (d, w) -> {
                        vm.copySchedulesTo(new ArrayList<>(currentSchedules), target);
                        toast("Đã sao chép thời gian biểu.");
                    }).show();
        });
        picker.show(getParentFragmentManager(), "copy_schedule_date");
    }

    private void renderSchedules(List<ScheduleBlockEntity> list) {
        currentSchedules = list == null ? new ArrayList<>() : new ArrayList<>(list);
        binding.scheduleContainer.removeAllViews();
        if (list == null || list.isEmpty()) {
            binding.scheduleContainer.addView(infoText("Chưa có khối thời gian. Thêm giờ học, giờ làm, giờ ngủ hoặc nghỉ ngơi."));
            return;
        }
        for (ScheduleBlockEntity block : list) {
            MaterialCardView card = new MaterialCardView(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = dp(9);
            card.setLayoutParams(params);
            card.setRadius(dp(18));
            card.setCardElevation(0);
            card.setStrokeWidth(0);
            card.setCardBackgroundColor(categoryBackground(block.category));
            card.setClickable(true);
            card.setFocusable(true);

            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(13), dp(12), dp(13), dp(12));

            LinearLayout timeCol = new LinearLayout(requireContext());
            timeCol.setOrientation(LinearLayout.VERTICAL);
            timeCol.setGravity(Gravity.CENTER);
            timeCol.setLayoutParams(new LinearLayout.LayoutParams(dp(70), ViewGroup.LayoutParams.WRAP_CONTENT));
            TextView start = textView(DateUtils.formatTimeFromMinutes(block.startMinutes), 15, R.color.on_surface, true);
            TextView end = textView(DateUtils.formatTimeFromMinutes(block.endMinutes) + (block.endMinutes < block.startMinutes ? " (+1)" : ""), 12, R.color.on_surface_variant, false);
            timeCol.addView(start);
            timeCol.addView(end);

            View line = new View(requireContext());
            LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(dp(3), dp(52));
            lineParams.setMargins(dp(4), 0, dp(12), 0);
            line.setLayoutParams(lineParams);
            line.setBackgroundColor(categoryText(block.category));

            LinearLayout info = new LinearLayout(requireContext());
            info.setOrientation(LinearLayout.VERTICAL);
            info.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            TextView title = textView(block.title, 16, R.color.on_surface, true);
            String subtitle = categoryName(block.category);
            if (!TextUtils.isEmpty(block.note)) subtitle += " · " + block.note;
            TextView note = textView(subtitle, 12, R.color.on_surface_variant, false);
            note.setMaxLines(2);
            info.addView(title);
            info.addView(note);

            row.addView(timeCol);
            row.addView(line);
            row.addView(info);
            if (block.reminderEnabled) {
                ImageView bell = new ImageView(requireContext());
                bell.setImageResource(R.drawable.ic_bell);
                bell.setColorFilter(categoryText(block.category));
                bell.setPadding(dp(8), dp(8), dp(8), dp(8));
                row.addView(bell, new LinearLayout.LayoutParams(dp(40), dp(40)));
            }
            card.addView(row);
            card.setOnClickListener(v -> showScheduleSheet(block));
            card.setOnLongClickListener(v -> {
                confirmDeleteSchedule(block);
                return true;
            });
            binding.scheduleContainer.addView(card);
        }
    }

    private void renderTasks(List<TaskEntity> list) {
        binding.taskContainer.removeAllViews();
        if (list == null || list.isEmpty()) {
            binding.taskContainer.addView(infoText("Chưa có việc cần làm trong ngày này."));
            return;
        }
        for (TaskEntity task : list) {
            CheckBox box = new CheckBox(requireContext());
            box.setText(task.title + (task.expectedMinutes > 0 ? " · " + task.expectedMinutes + " phút" : ""));
            box.setTextColor(ContextCompat.getColor(requireContext(), R.color.on_surface));
            box.setChecked(task.completed);
            setRowPadding(box);
            box.setOnCheckedChangeListener((button, checked) -> vm.toggleTask(task, checked));
            box.setOnLongClickListener(v -> {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Xóa công việc?")
                        .setMessage(task.title)
                        .setNegativeButton("Hủy", null)
                        .setPositiveButton("Xóa", (d, w) -> vm.deleteTask(task))
                        .show();
                return true;
            });
            binding.taskContainer.addView(box);
        }
    }


    private void renderStudySummary() {
        if (binding == null) return;
        binding.tvStudySummary.setText("Ngày này: " + durationText(selectedDayStudyMinutes)
                + " · Tuần này: " + durationText(selectedWeekStudyMinutes));
    }

    private String durationText(int minutes) {
        int safe = Math.max(0, minutes);
        int hours = safe / 60;
        int remain = safe % 60;
        if (hours <= 0) return remain + " phút";
        if (remain == 0) return hours + " giờ";
        return hours + " giờ " + remain + " phút";
    }

    private void renderStudies(List<StudySessionEntity> list) {
        binding.studyContainer.removeAllViews();
        if (list == null || list.isEmpty()) return;
        for (StudySessionEntity item : list) {
            MaterialCardView card = compactInfoCard(item.subject + " · " + item.minutes + " phút",
                    TextUtils.isEmpty(item.note) ? "Phiên học đã lưu" : item.note);
            card.setOnLongClickListener(v -> {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Xóa phiên học?")
                        .setMessage(item.subject + " · " + item.minutes + " phút")
                        .setNegativeButton("Hủy", null)
                        .setPositiveButton("Xóa", (d, w) -> vm.deleteStudy(item))
                        .show();
                return true;
            });
            binding.studyContainer.addView(card);
        }
    }

    private void renderLearning(DailyLearningEntity learning) {
        if (binding == null) return;
        if (!binding.edtVocabularyCount.hasFocus()) {
            binding.edtVocabularyCount.setText(learning == null ? "" : String.valueOf(learning.vocabularyCount));
        }
        if (!binding.edtMockScore.hasFocus()) {
            binding.edtMockScore.setText(learning == null || learning.mockScore <= 0 ? "" : String.valueOf(learning.mockScore));
        }
    }

    private void renderWeeklyLearning(Integer value) {
        int learned = value == null ? 0 : value;
        int goal = userStore.getWeeklyVocabularyGoal();
        int percent = Math.min(100, Math.round(learned * 100f / Math.max(1, goal)));
        binding.vocabularyProgress.setProgressCompat(percent, true);
        binding.tvWeeklyLearning.setText("Tuần này: " + learned + "/" + goal + " từ · chạm để đổi mục tiêu");
    }

    private void renderHabits() {
        if (binding == null) return;
        binding.habitContainer.removeAllViews();
        if (habits.isEmpty()) {
            binding.habitContainer.addView(infoText("Thêm thói quen đầu tiên để tạo chuỗi tốt mỗi ngày."));
            return;
        }
        int accent = ThemeUtils.getPrimaryColor(requireContext(), userStore.getThemeKey());
        int container = ThemeUtils.getContainerColor(requireContext(), userStore.getThemeKey());
        for (HabitEntity habit : habits) {
            boolean checked = checkedHabitIds.contains(habit.id);
            int streak = calculateStreak(habit.id, selectedDate);
            MaterialButton button = new MaterialButton(requireContext(), null,
                    com.google.android.material.R.attr.materialButtonOutlinedStyle);
            button.setText((checked ? "Đã hoàn thành · " : "Chưa hoàn thành · ") + habit.name + "     🔥 " + streak + " ngày");
            button.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            button.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
            button.setCornerRadius(dp(16));
            button.setMinHeight(dp(56));
            button.setIconResource(checked ? R.drawable.ic_check_circle : R.drawable.ic_clock);
            button.setIconTint(ColorStateList.valueOf(checked ? accent : ContextCompat.getColor(requireContext(), R.color.on_surface_variant)));
            button.setBackgroundTintList(ColorStateList.valueOf(checked ? container : ContextCompat.getColor(requireContext(), R.color.surface)));
            button.setStrokeColor(ColorStateList.valueOf(checked ? accent : ContextCompat.getColor(requireContext(), R.color.surface_variant)));
            button.setStrokeWidth(dp(1));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = dp(8);
            button.setLayoutParams(params);
            button.setOnClickListener(v -> vm.setHabitChecked(habit.id, !checked));
            binding.habitContainer.addView(button);
        }
    }

    private int calculateStreak(long habitId, String endDate) {
        Set<String> dates = new HashSet<>();
        for (HabitCheckInEntity checkIn : allCheckIns) if (checkIn.habitId == habitId) dates.add(checkIn.dateKey);
        int streak = 0;
        String cursor = endDate;
        for (int i = 0; i < 3660; i++) {
            if (!dates.contains(cursor)) break;
            streak++;
            cursor = DateUtils.shiftDateKey(cursor, -1);
        }
        return streak;
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Chọn ngày cần xem")
                .setSelection(DateUtils.toUtcPickerMillis(selectedDate))
                .build();
        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection != null) vm.selectDate(DateUtils.dateKeyFromUtcPicker(selection));
        });
        picker.show(getParentFragmentManager(), "planner_date_picker");
    }

    private void showScheduleSheet(@Nullable ScheduleBlockEntity existing) {
        BottomSheetDialog sheet = new BottomSheetDialog(requireContext());
        BottomSheetScheduleBinding d = BottomSheetScheduleBinding.inflate(getLayoutInflater());
        sheet.setContentView(d.getRoot());
        ThemeUtils.tintFilledButton(d.btnSaveSchedule, requireContext(), userStore.getThemeKey());
        ThemeUtils.tintTonalButton(d.btnStartTime, requireContext(), userStore.getThemeKey());
        ThemeUtils.tintTonalButton(d.btnEndTime, requireContext(), userStore.getThemeKey());
        ThemeUtils.tintSwitch(d.switchScheduleReminder, requireContext(), userStore.getThemeKey());

        String[] labels = {"Học tập", "Làm việc / chạy xe", "Ngủ", "Tập luyện", "Nghỉ ngơi", "Khác"};
        String[] codes = {"STUDY", "WORK", "SLEEP", "HEALTH", "REST", "OTHER"};
        d.dropdownScheduleCategory.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, labels));

        final int[] startMinutes = {existing == null ? 8 * 60 : existing.startMinutes};
        final int[] endMinutes = {existing == null ? 9 * 60 : existing.endMinutes};
        d.tvScheduleTitle.setText(existing == null ? "Thêm thời gian biểu" : "Sửa thời gian biểu");
        d.tvScheduleDate.setText(DateUtils.formatDateKey(selectedDate) + " · " + LunarCalendarUtils.formatLunar(selectedDate));
        if (existing != null) {
            d.edtScheduleName.setText(existing.title);
            d.dropdownScheduleCategory.setText(categoryName(existing.category), false);
            d.edtScheduleNote.setText(existing.note);
            d.switchScheduleReminder.setChecked(existing.reminderEnabled);
        } else {
            d.dropdownScheduleCategory.setText(labels[0], false);
        }
        updateTimeButtons(d, startMinutes[0], endMinutes[0]);
        d.btnStartTime.setOnClickListener(v -> showTimePicker("Giờ bắt đầu", startMinutes[0], value -> {
            startMinutes[0] = value;
            updateTimeButtons(d, startMinutes[0], endMinutes[0]);
        }));
        d.btnEndTime.setOnClickListener(v -> showTimePicker("Giờ kết thúc", endMinutes[0], value -> {
            endMinutes[0] = value;
            updateTimeButtons(d, startMinutes[0], endMinutes[0]);
        }));
        d.btnCancelSchedule.setOnClickListener(v -> sheet.dismiss());
        d.btnSaveSchedule.setOnClickListener(v -> {
            String name = text(d.edtScheduleName.getText());
            if (name.isEmpty()) {
                d.edtScheduleName.setError("Nhập tên hoạt động");
                return;
            }
            if (endMinutes[0] == startMinutes[0]) {
                toast("Giờ bắt đầu và kết thúc không được trùng nhau.");
                return;
            }
            String selectedLabel = d.dropdownScheduleCategory.getText().toString();
            String code = codes[0];
            for (int i = 0; i < labels.length; i++) if (labels[i].equals(selectedLabel)) code = codes[i];
            ScheduleBlockEntity block = existing == null
                    ? new ScheduleBlockEntity(selectedDate, name, code, startMinutes[0], endMinutes[0],
                    d.switchScheduleReminder.isChecked(), text(d.edtScheduleNote.getText()), System.currentTimeMillis())
                    : existing;
            if (existing != null) {
                block.title = name;
                block.category = code;
                block.startMinutes = startMinutes[0];
                block.endMinutes = endMinutes[0];
                block.reminderEnabled = d.switchScheduleReminder.isChecked();
                block.note = text(d.edtScheduleNote.getText());
            }
            vm.saveSchedule(block);
            if (block.reminderEnabled) requestNotificationPermissionIfNeeded();
            sheet.dismiss();
        });
        sheet.setOnShowListener(dialog -> {
            FrameLayout bottom = sheet.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottom != null) {
                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottom);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(false);
                behavior.setDraggable(true);
                behavior.setPeekHeight((int) (getResources().getDisplayMetrics().heightPixels * 0.72f));
                bottom.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                bottom.requestLayout();
            }
        });
        sheet.show();
    }

    private void showTimePicker(String title, int currentMinutes, TimeResult result) {
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(currentMinutes / 60)
                .setMinute(currentMinutes % 60)
                .setTitleText(title)
                .build();
        picker.addOnPositiveButtonClickListener(v -> result.onTime(picker.getHour() * 60 + picker.getMinute()));
        picker.show(getParentFragmentManager(), "planner_time_picker");
    }

    private void updateTimeButtons(BottomSheetScheduleBinding d, int start, int end) {
        d.btnStartTime.setText("Bắt đầu " + DateUtils.formatTimeFromMinutes(start));
        d.btnEndTime.setText("Kết thúc " + DateUtils.formatTimeFromMinutes(end));
    }

    private void confirmDeleteSchedule(ScheduleBlockEntity block) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xóa khối thời gian?")
                .setMessage(block.title + " · " + DateUtils.formatTimeFromMinutes(block.startMinutes)
                        + "–" + DateUtils.formatTimeFromMinutes(block.endMinutes))
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (d, w) -> vm.deleteSchedule(block))
                .show();
    }

    private void showTaskDialog() {
        DialogAddTaskBinding d = DialogAddTaskBinding.inflate(getLayoutInflater());
        showValidatedDialog("Thêm công việc · " + DateUtils.formatCompactDateKey(selectedDate), d.getRoot(), () -> {
            String title = text(d.edtTaskTitle.getText());
            if (title.isEmpty()) return false;
            vm.addTask(title, parseInt(d.edtTaskMinutes.getText(), 0));
            return true;
        });
    }

    private void showStudyDialog() {
        DialogAddStudyBinding d = DialogAddStudyBinding.inflate(getLayoutInflater());
        showValidatedDialog("Ghi phiên học · " + DateUtils.formatCompactDateKey(selectedDate), d.getRoot(), () -> {
            String subject = text(d.edtSubject.getText());
            int minutes = parseInt(d.edtStudyMinutes.getText(), 0);
            if (subject.isEmpty() || minutes <= 0) return false;
            vm.addStudy(subject, minutes, text(d.edtStudyNote.getText()));
            return true;
        });
    }

    private void showHabitDialog() {
        DialogAddHabitBinding d = DialogAddHabitBinding.inflate(getLayoutInflater());
        d.edtHabitTarget.setText("7");
        showValidatedDialog("Thêm thói quen", d.getRoot(), () -> {
            String name = text(d.edtHabitName.getText());
            if (name.isEmpty()) return false;
            vm.addHabit(name, parseInt(d.edtHabitTarget.getText(), 7));
            return true;
        });
    }

    private void saveLearning() {
        int words = parseInt(binding.edtVocabularyCount.getText(), 0);
        int score = parseInt(binding.edtMockScore.getText(), 0);
        vm.saveDailyLearning(words, score);
        binding.edtVocabularyCount.clearFocus();
        binding.edtMockScore.clearFocus();
        toast("Đã lưu tiến độ học của ngày này.");
    }

    private void showVocabularyGoalDialog() {
        DialogVocabularyGoalBinding d = DialogVocabularyGoalBinding.inflate(getLayoutInflater());
        d.edtVocabularyGoal.setText(String.valueOf(userStore.getWeeklyVocabularyGoal()));
        androidx.appcompat.app.AlertDialog alert = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Mục tiêu từ mới mỗi tuần")
                .setView(d.getRoot())
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", null)
                .create();
        alert.setOnShowListener(x -> alert.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    int value = parseInt(d.edtVocabularyGoal.getText(), 0);
                    if (value <= 0) {
                        d.edtVocabularyGoal.setError("Nhập mục tiêu lớn hơn 0");
                        return;
                    }
                    userStore.setWeeklyVocabularyGoal(value);
                    Integer current = vm.weekVocabularyCount().getValue();
                    renderWeeklyLearning(current);
                    alert.dismiss();
                }));
        alert.show();
    }

    private void togglePomodoro() {
        if (pomodoroRunning) {
            stopPomodoro(false);
            return;
        }
        pomodoroRunning = true;
        binding.btnStartPomodoro.setText("Tạm dừng");
        pomodoroTimer = new CountDownTimer(pomodoroRemaining, 1000) {
            @Override public void onTick(long millisUntilFinished) {
                pomodoroRemaining = millisUntilFinished;
                renderPomodoro();
            }

            @Override public void onFinish() {
                pomodoroRemaining = POMODORO_MS;
                pomodoroRunning = false;
                binding.btnStartPomodoro.setText("Bắt đầu");
                renderPomodoro();
                vm.addStudy("Pomodoro", 25, "Tự động ghi từ bộ đếm tập trung");
                toast("Hoàn thành 25 phút tập trung. Đã cộng vào thời gian học.");
            }
        }.start();
    }

    private void stopPomodoro(boolean reset) {
        if (pomodoroTimer != null) pomodoroTimer.cancel();
        pomodoroTimer = null;
        pomodoroRunning = false;
        if (reset) pomodoroRemaining = POMODORO_MS;
        binding.btnStartPomodoro.setText("Bắt đầu");
        renderPomodoro();
    }

    private void resetPomodoro() { stopPomodoro(true); }

    private void renderPomodoro() {
        if (binding == null) return;
        long seconds = Math.max(0, pomodoroRemaining / 1000);
        binding.tvPomodoro.setText(String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60));
    }

    private void saveNote() {
        vm.saveNote(text(binding.edtDailyNote.getText()), text(binding.edtNoteTags.getText()),
                binding.dropdownMood.getText().toString());
        binding.edtDailyNote.clearFocus();
        binding.edtNoteTags.clearFocus();
        toast("Đã lưu nhật ký ngày " + DateUtils.formatCompactDateKey(selectedDate));
    }

    private void showNoteHistory() {
        if (noteHistory.isEmpty()) {
            toast("Chưa có ghi chú cũ.");
            return;
        }
        String[] items = new String[noteHistory.size()];
        for (int i = 0; i < noteHistory.size(); i++) {
            DailyNoteEntity note = noteHistory.get(i);
            String preview = safe(note.content).replace('\n', ' ');
            if (preview.length() > 55) preview = preview.substring(0, 55) + "…";
            items[i] = DateUtils.formatCompactDateKey(note.dateKey) + " · "
                    + LunarCalendarUtils.formatLunar(note.dateKey) + " · " + preview;
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Lịch sử ghi chú")
                .setItems(items, (d, which) -> vm.selectDate(noteHistory.get(which).dateKey))
                .setNegativeButton("Đóng", null)
                .show();
    }

    private MaterialCardView compactInfoCard(String title, String subtitle) {
        MaterialCardView card = new MaterialCardView(requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(7);
        card.setLayoutParams(params);
        card.setRadius(dp(15));
        card.setCardElevation(0);
        card.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.surface_variant));
        LinearLayout content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(12), dp(10), dp(12), dp(10));
        content.addView(textView(title, 14, R.color.on_surface, true));
        content.addView(textView(subtitle, 12, R.color.on_surface_variant, false));
        card.addView(content);
        return card;
    }

    private TextView infoText(String value) {
        TextView tv = textView(value, 14, R.color.on_surface_variant, false);
        setRowPadding(tv);
        return tv;
    }

    private TextView textView(String text, float size, int colorRes, boolean bold) {
        TextView tv = new TextView(requireContext());
        tv.setText(text);
        tv.setTextSize(size);
        tv.setTextColor(ContextCompat.getColor(requireContext(), colorRes));
        if (bold) tv.setTypeface(tv.getTypeface(), android.graphics.Typeface.BOLD);
        return tv;
    }

    private void setRowPadding(View view) {
        view.setPadding(0, dp(9), 0, dp(9));
        view.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    @ColorInt private int categoryBackground(String category) {
        switch (safe(category)) {
            case "WORK": return ContextCompat.getColor(requireContext(), R.color.schedule_work);
            case "STUDY": return ContextCompat.getColor(requireContext(), R.color.schedule_study);
            case "SLEEP":
            case "REST": return ContextCompat.getColor(requireContext(), R.color.schedule_sleep);
            case "HEALTH": return ContextCompat.getColor(requireContext(), R.color.schedule_health);
            default: return ContextCompat.getColor(requireContext(), R.color.schedule_other);
        }
    }

    @ColorInt private int categoryText(String category) {
        switch (safe(category)) {
            case "WORK": return ContextCompat.getColor(requireContext(), R.color.schedule_work_text);
            case "STUDY": return ContextCompat.getColor(requireContext(), R.color.schedule_study_text);
            case "SLEEP":
            case "REST": return ContextCompat.getColor(requireContext(), R.color.schedule_sleep_text);
            case "HEALTH": return ContextCompat.getColor(requireContext(), R.color.schedule_health_text);
            default: return ContextCompat.getColor(requireContext(), R.color.schedule_other_text);
        }
    }

    private String categoryName(String category) {
        switch (safe(category)) {
            case "WORK": return "Làm việc / chạy xe";
            case "STUDY": return "Học tập";
            case "SLEEP": return "Ngủ";
            case "HEALTH": return "Tập luyện";
            case "REST": return "Nghỉ ngơi";
            default: return "Khác";
        }
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private interface Validator { boolean save(); }
    private interface TimeResult { void onTime(int minutes); }

    private void showValidatedDialog(String title, View content, Validator validator) {
        androidx.appcompat.app.AlertDialog alert = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setView(content)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", null)
                .create();
        alert.setOnShowListener(x -> alert.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    if (validator.save()) alert.dismiss();
                    else toast("Hãy nhập đầy đủ thông tin hợp lệ.");
                }));
        alert.show();
    }

    private String text(CharSequence value) { return value == null ? "" : value.toString().trim(); }
    private String safe(String value) { return value == null ? "" : value; }
    private int parseInt(CharSequence value, int fallback) {
        try { return Integer.parseInt(text(value)); }
        catch (Exception e) { return fallback; }
    }
    private int dp(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                getResources().getDisplayMetrics());
    }
    private void toast(String message) { Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show(); }

    @Override
    public void onDestroyView() {
        if (pomodoroTimer != null) pomodoroTimer.cancel();
        pomodoroTimer = null;
        super.onDestroyView();
        binding = null;
    }
}
