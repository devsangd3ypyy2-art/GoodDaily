package com.sangapp.gooddaily.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.sangapp.gooddaily.R;
import com.sangapp.gooddaily.data.local.entity.DailyAmount;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WeeklyBarChartView extends View {
    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private List<DailyAmount> data = new ArrayList<>();

    public WeeklyBarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        barPaint.setColor(ContextCompat.getColor(context, R.color.primary));
        textPaint.setColor(ContextCompat.getColor(context, R.color.on_surface_variant));
        textPaint.setTextSize(30f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        gridPaint.setColor(ContextCompat.getColor(context, R.color.outline));
        gridPaint.setStrokeWidth(1f);
    }

    public void setData(List<DailyAmount> values) {
        data = values == null ? new ArrayList<>() : values;
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        float left = 20f, right = w - 20f, top = 20f, bottom = h - 48f;
        canvas.drawLine(left, bottom, right, bottom, gridPaint);

        if (data.isEmpty()) {
            textPaint.setTextSize(34f);
            canvas.drawText("Chưa có khoản chi trong 7 ngày", w / 2f, h / 2f, textPaint);
            return;
        }

        double max = 1;
        for (DailyAmount item : data) max = Math.max(max, item.total);
        float slot = (right - left) / data.size();
        float barWidth = Math.min(slot * 0.56f, 60f);

        for (int i = 0; i < data.size(); i++) {
            DailyAmount item = data.get(i);
            float center = left + slot * i + slot / 2f;
            float barHeight = (float) ((item.total / max) * (bottom - top - 30f));
            canvas.drawRoundRect(center - barWidth / 2, bottom - barHeight, center + barWidth / 2, bottom, 12f, 12f, barPaint);
            textPaint.setTextSize(25f);
            canvas.drawText(item.label, center, h - 12f, textPaint);
            textPaint.setTextSize(22f);
            String value = NumberFormat.getIntegerInstance(new Locale("vi", "VN")).format(item.total / 1000) + "k";
            canvas.drawText(value, center, bottom - barHeight - 8f, textPaint);
        }
    }
}
