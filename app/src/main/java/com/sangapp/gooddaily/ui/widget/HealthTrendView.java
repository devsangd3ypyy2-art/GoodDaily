package com.sangapp.gooddaily.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.sangapp.gooddaily.R;
import com.sangapp.gooddaily.data.local.entity.BodyRecordEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HealthTrendView extends View {
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private List<BodyRecordEntity> items = Collections.emptyList();

    public HealthTrendView(Context context) { this(context, null); }
    public HealthTrendView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(dp(3));
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        pointPaint.setStyle(Paint.Style.FILL);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(dp(1));
        textPaint.setTextSize(sp(12));
        setMinimumHeight(Math.round(dp(220)));
    }

    public void setItems(List<BodyRecordEntity> source) {
        if (source == null) items = Collections.emptyList();
        else {
            ArrayList<BodyRecordEntity> copy = new ArrayList<>(source);
            Collections.reverse(copy);
            if (copy.size() > 30) copy = new ArrayList<>(copy.subList(copy.size() - 30, copy.size()));
            items = copy;
        }
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int onSurface = ContextCompat.getColor(getContext(), R.color.on_surface);
        int outline = ContextCompat.getColor(getContext(), R.color.outline);
        int primary = resolvePrimary();
        linePaint.setColor(primary);
        pointPaint.setColor(primary);
        gridPaint.setColor(outline);
        textPaint.setColor(onSurface);

        float left = dp(44), top = dp(20), right = getWidth() - dp(18), bottom = getHeight() - dp(34);
        if (right <= left || bottom <= top) return;
        for (int i = 0; i <= 4; i++) {
            float y = top + (bottom - top) * i / 4f;
            canvas.drawLine(left, y, right, y, gridPaint);
        }
        if (items.isEmpty()) {
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Chưa có dữ liệu cân nặng", getWidth() / 2f, getHeight() / 2f, textPaint);
            return;
        }
        double min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
        for (BodyRecordEntity item : items) {
            min = Math.min(min, item.weight);
            max = Math.max(max, item.weight);
        }
        if (Math.abs(max - min) < 0.5) { min -= 1; max += 1; }
        else { min -= 0.5; max += 0.5; }

        textPaint.setTextAlign(Paint.Align.RIGHT);
        for (int i = 0; i <= 4; i++) {
            double value = max - (max - min) * i / 4d;
            float y = top + (bottom - top) * i / 4f;
            canvas.drawText(String.format(java.util.Locale.getDefault(), "%.1f", value), left - dp(8), y + dp(4), textPaint);
        }

        path.reset();
        float step = items.size() <= 1 ? 0 : (right - left) / (items.size() - 1f);
        for (int i = 0; i < items.size(); i++) {
            float x = left + step * i;
            float y = (float) (bottom - (items.get(i).weight - min) / (max - min) * (bottom - top));
            if (i == 0) path.moveTo(x, y); else path.lineTo(x, y);
        }
        canvas.drawPath(path, linePaint);
        for (int i = 0; i < items.size(); i++) {
            float x = left + step * i;
            float y = (float) (bottom - (items.get(i).weight - min) / (max - min) * (bottom - top));
            canvas.drawCircle(x, y, dp(4), pointPaint);
        }
        textPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Cũ", left, bottom + dp(24), textPaint);
        textPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Mới", right, bottom + dp(24), textPaint);
    }

    private int resolvePrimary() {
        int attrId = getResources().getIdentifier("colorPrimary", "attr", getContext().getPackageName());
        if (attrId != 0) {
            android.util.TypedValue value = new android.util.TypedValue();
            if (getContext().getTheme().resolveAttribute(attrId, value, true)) return value.data;
        }
        return ContextCompat.getColor(getContext(), R.color.primary);
    }

    private float dp(float value) { return value * getResources().getDisplayMetrics().density; }
    private float sp(float value) { return value * getResources().getDisplayMetrics().scaledDensity; }
}
