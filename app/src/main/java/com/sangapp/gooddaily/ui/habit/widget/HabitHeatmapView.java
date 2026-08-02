package com.sangapp.gooddaily.ui.habit.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.sangapp.gooddaily.R;
import com.sangapp.gooddaily.data.local.entity.HabitCheckInEntity;
import com.sangapp.gooddaily.util.DateUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HabitHeatmapView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Map<String, Integer> counts = new HashMap<>();
    private int days = 84;

    public HabitHeatmapView(Context context) { this(context, null); }
    public HabitHeatmapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        textPaint.setTextSize(sp(11));
        setMinimumHeight(Math.round(dp(170)));
    }

    public void setCheckIns(List<HabitCheckInEntity> items) {
        counts.clear();
        if (items != null) for (HabitCheckInEntity item : items) {
            if (item.dateKey != null) counts.put(item.dateKey, counts.getOrDefault(item.dateKey, 0) + 1);
        }
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int primary = resolveColor("colorPrimary", ContextCompat.getColor(getContext(), R.color.primary));
        int surfaceVariant = ContextCompat.getColor(getContext(), R.color.surface_variant);
        int onSurfaceVariant = ContextCompat.getColor(getContext(), R.color.on_surface_variant);
        textPaint.setColor(onSurfaceVariant);
        int columns = 12, rows = 7;
        float gap = dp(4);
        float labelWidth = dp(24);
        float cell = Math.min((getWidth() - labelWidth - gap * (columns - 1)) / columns,
                (getHeight() - dp(32) - gap * (rows - 1)) / rows);
        if (cell <= 0) return;
        String[] labels = {"T2", "", "T4", "", "T6", "", "CN"};
        for (int r = 0; r < rows; r++) {
            if (!labels[r].isEmpty()) canvas.drawText(labels[r], 0, dp(19) + r * (cell + gap), textPaint);
        }
        String start = DateUtils.shiftDateKey(DateUtils.dateKey(), -(days - 1));
        for (int i = 0; i < days; i++) {
            String key = DateUtils.shiftDateKey(start, i);
            int col = i / 7;
            int row = i % 7;
            int count = counts.getOrDefault(key, 0);
            int color = count == 0 ? surfaceVariant : blend(surfaceVariant, primary, Math.min(1f, 0.3f + count * 0.18f));
            paint.setColor(color);
            float left = labelWidth + col * (cell + gap);
            float top = dp(8) + row * (cell + gap);
            canvas.drawRoundRect(left, top, left + cell, top + cell, dp(4), dp(4), paint);
        }
        textPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("12 tuần gần nhất", getWidth(), getHeight() - dp(3), textPaint);
        textPaint.setTextAlign(Paint.Align.LEFT);
    }

    private int resolveColor(String name, int fallback) {
        int attr = getResources().getIdentifier(name, "attr", getContext().getPackageName());
        if (attr == 0) return fallback;
        android.util.TypedValue value = new android.util.TypedValue();
        return getContext().getTheme().resolveAttribute(attr, value, true) ? value.data : fallback;
    }

    private int blend(int from, int to, float ratio) {
        ratio = Math.max(0, Math.min(1, ratio));
        int r = Math.round(android.graphics.Color.red(from) * (1 - ratio) + android.graphics.Color.red(to) * ratio);
        int g = Math.round(android.graphics.Color.green(from) * (1 - ratio) + android.graphics.Color.green(to) * ratio);
        int b = Math.round(android.graphics.Color.blue(from) * (1 - ratio) + android.graphics.Color.blue(to) * ratio);
        return android.graphics.Color.rgb(r, g, b);
    }

    private float dp(float value) { return value * getResources().getDisplayMetrics().density; }
    private float sp(float value) { return value * getResources().getDisplayMetrics().scaledDensity; }
}
