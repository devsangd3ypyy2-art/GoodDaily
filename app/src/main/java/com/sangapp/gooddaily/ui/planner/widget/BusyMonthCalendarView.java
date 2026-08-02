package com.sangapp.gooddaily.ui.planner.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.sangapp.gooddaily.R;
import com.sangapp.gooddaily.util.DateUtils;
import com.sangapp.gooddaily.util.LunarCalendarUtils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** A lightweight Material-friendly month grid with solar/lunar labels and busy dots. */
public class BusyMonthCalendarView extends View {
    public interface OnDateSelectedListener { void onDateSelected(String dateKey); }
    public interface OnMonthChangedListener { void onMonthChanged(int year, int monthZeroBased); }

    public static final int DOT_PLAN = 1;
    public static final int DOT_STUDY = 1 << 1;
    public static final int DOT_FINANCE = 1 << 2;
    public static final int DOT_JOURNAL = 1 << 3;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Calendar display = Calendar.getInstance();
    private String selectedDateKey = DateUtils.dateKey();
    private final Map<String, Integer> busyDays = new HashMap<>();
    private OnDateSelectedListener listener;
    private OnMonthChangedListener monthListener;
    private float downX;

    private int onSurface;
    private int onSurfaceVariant;
    private int primary;
    private int primaryContainer;
    private int surface;
    private int planColor;
    private int studyColor;
    private int financeColor;
    private int journalColor;

    public BusyMonthCalendarView(Context context) { this(context, null); }
    public BusyMonthCalendarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setMinimumHeight(Math.round(dp(360)));
        setFocusable(true);
        setClickable(true);
        Calendar selected = Calendar.getInstance();
        selected.setTimeInMillis(DateUtils.parseDateKey(selectedDateKey));
        display.set(selected.get(Calendar.YEAR), selected.get(Calendar.MONTH), 1);
        resolveColors();
    }

    private void resolveColors() {
        onSurface = ContextCompat.getColor(getContext(), R.color.on_surface);
        onSurfaceVariant = ContextCompat.getColor(getContext(), R.color.on_surface_variant);
        primary = ContextCompat.getColor(getContext(), R.color.primary);
        primaryContainer = ContextCompat.getColor(getContext(), R.color.primary_container);
        surface = ContextCompat.getColor(getContext(), R.color.surface);
        planColor = ContextCompat.getColor(getContext(), R.color.schedule_work_text);
        studyColor = ContextCompat.getColor(getContext(), R.color.schedule_study_text);
        financeColor = ContextCompat.getColor(getContext(), R.color.warning);
        journalColor = ContextCompat.getColor(getContext(), R.color.theme_purple);
    }

    public void setSelectedDate(String dateKey) {
        selectedDateKey = dateKey == null ? DateUtils.dateKey() : dateKey;
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(DateUtils.parseDateKey(selectedDateKey));
        display.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), 1);
        invalidate();
    }

    public void setBusyDays(Map<String, Integer> values) {
        busyDays.clear();
        if (values != null) busyDays.putAll(values);
        invalidate();
    }

    public int getDisplayYear() { return display.get(Calendar.YEAR); }
    public int getDisplayMonth() { return display.get(Calendar.MONTH); }
    public String getDisplayMonthTitle() {
        return String.format(new Locale("vi", "VN"), "Tháng %02d / %d",
                display.get(Calendar.MONTH) + 1, display.get(Calendar.YEAR));
    }

    public void previousMonth() { changeMonth(-1); }
    public void nextMonth() { changeMonth(1); }
    private void changeMonth(int delta) {
        display.add(Calendar.MONTH, delta);
        display.set(Calendar.DAY_OF_MONTH, 1);
        if (monthListener != null) monthListener.onMonthChanged(getDisplayYear(), getDisplayMonth());
        invalidate();
    }

    public void setOnDateSelectedListener(OnDateSelectedListener value) { listener = value; }
    public void setOnMonthChangedListener(OnMonthChangedListener value) { monthListener = value; }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        resolveColors();
        canvas.drawColor(surface);
        float width = getWidth();
        float header = dp(34);
        float cellW = width / 7f;
        float cellH = (getHeight() - header) / 6f;
        String[] weekdays = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        paint.setTextSize(sp(12));
        paint.setColor(onSurfaceVariant);
        for (int col = 0; col < 7; col++) {
            canvas.drawText(weekdays[col], col * cellW + cellW / 2f, dp(22), paint);
        }

        Calendar first = (Calendar) display.clone();
        int calendarDay = first.get(Calendar.DAY_OF_WEEK);
        int offset = calendarDay == Calendar.SUNDAY ? 6 : calendarDay - Calendar.MONDAY;
        Calendar cursor = (Calendar) first.clone();
        cursor.add(Calendar.DAY_OF_MONTH, -offset);
        int displayMonth = display.get(Calendar.MONTH);

        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                float left = col * cellW;
                float top = header + row * cellH;
                float cx = left + cellW / 2f;
                String key = DateUtils.dateKey(cursor.getTimeInMillis());
                boolean inMonth = cursor.get(Calendar.MONTH) == displayMonth;
                boolean selected = key.equals(selectedDateKey);
                boolean today = key.equals(DateUtils.dateKey());

                if (selected) {
                    paint.setColor(primaryContainer);
                    canvas.drawRoundRect(new RectF(left + dp(4), top + dp(3),
                            left + cellW - dp(4), top + cellH - dp(3)), dp(14), dp(14), paint);
                } else if (today) {
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(dp(1.5f));
                    paint.setColor(primary);
                    canvas.drawRoundRect(new RectF(left + dp(5), top + dp(4),
                            left + cellW - dp(5), top + cellH - dp(4)), dp(13), dp(13), paint);
                    paint.setStyle(Paint.Style.FILL);
                }

                paint.setTextAlign(Paint.Align.CENTER);
                paint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
                paint.setTextSize(sp(16));
                paint.setColor(inMonth ? onSurface : adjustAlpha(onSurfaceVariant, 0.48f));
                canvas.drawText(String.valueOf(cursor.get(Calendar.DAY_OF_MONTH)), cx, top + dp(24), paint);

                paint.setTypeface(android.graphics.Typeface.DEFAULT);
                paint.setTextSize(sp(9));
                paint.setColor(inMonth ? onSurfaceVariant : adjustAlpha(onSurfaceVariant, 0.38f));
                String lunar = LunarCalendarUtils.formatLunar(key).replace(" âm", "");
                canvas.drawText(lunar, cx, top + dp(38), paint);

                int mask = busyDays.getOrDefault(key, 0);
                int count = Integer.bitCount(mask);
                if (count > 0 && inMonth) {
                    float gap = dp(6);
                    float start = cx - (count - 1) * gap / 2f;
                    int index = 0;
                    if ((mask & DOT_PLAN) != 0) drawDot(canvas, start + index++ * gap, top + cellH - dp(9), planColor);
                    if ((mask & DOT_STUDY) != 0) drawDot(canvas, start + index++ * gap, top + cellH - dp(9), studyColor);
                    if ((mask & DOT_FINANCE) != 0) drawDot(canvas, start + index++ * gap, top + cellH - dp(9), financeColor);
                    if ((mask & DOT_JOURNAL) != 0) drawDot(canvas, start + index * gap, top + cellH - dp(9), journalColor);
                }
                cursor.add(Calendar.DAY_OF_MONTH, 1);
            }
        }
    }

    private void drawDot(Canvas canvas, float x, float y, int color) {
        paint.setColor(color);
        canvas.drawCircle(x, y, dp(2.2f), paint);
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            downX = event.getX();
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            float dx = event.getX() - downX;
            if (Math.abs(dx) > dp(52)) {
                if (dx < 0) nextMonth(); else previousMonth();
                performClick();
                return true;
            }
            float header = dp(34);
            if (event.getY() < header) return true;
            int col = Math.max(0, Math.min(6, (int) (event.getX() / (getWidth() / 7f))));
            int row = Math.max(0, Math.min(5, (int) ((event.getY() - header) / ((getHeight() - header) / 6f))));
            Calendar first = (Calendar) display.clone();
            int day = first.get(Calendar.DAY_OF_WEEK);
            int offset = day == Calendar.SUNDAY ? 6 : day - Calendar.MONDAY;
            first.add(Calendar.DAY_OF_MONTH, row * 7 + col - offset);
            selectedDateKey = DateUtils.dateKey(first.getTimeInMillis());
            if (first.get(Calendar.MONTH) != display.get(Calendar.MONTH)) {
                display.set(first.get(Calendar.YEAR), first.get(Calendar.MONTH), 1);
                if (monthListener != null) monthListener.onMonthChanged(getDisplayYear(), getDisplayMonth());
            }
            if (listener != null) listener.onDateSelected(selectedDateKey);
            invalidate();
            performClick();
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override public boolean performClick() { super.performClick(); return true; }
    private int adjustAlpha(int color, float factor) {
        return android.graphics.Color.argb(Math.round(android.graphics.Color.alpha(color) * factor),
                android.graphics.Color.red(color), android.graphics.Color.green(color), android.graphics.Color.blue(color));
    }
    private float dp(float value) { return value * getResources().getDisplayMetrics().density; }
    private float sp(float value) { return value * getResources().getDisplayMetrics().scaledDensity; }
}
