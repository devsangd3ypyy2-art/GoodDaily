package com.sangapp.gooddaily.ui.finance.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.sangapp.gooddaily.data.local.entity.CategoryTotal;

import java.util.ArrayList;
import java.util.List;

public class FinancePieChartView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private List<CategoryTotal> data = new ArrayList<>();
    private final int[] palette = {0xFF6E8B3D,0xFF2E6AA6,0xFFC46A2C,0xFF7A4CA2,0xFFE54B4B,0xFF169C8D,0xFFCC8B19,0xFF4E768D};

    public FinancePieChartView(Context context) { super(context); init(); }
    public FinancePieChartView(Context context, @Nullable AttributeSet attrs) { super(context, attrs); init(); }
    public FinancePieChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }
    private void init() { centerPaint.setColor(Color.TRANSPARENT); setLayerType(LAYER_TYPE_SOFTWARE, null); }

    public void setData(List<CategoryTotal> value) {
        data = value == null ? new ArrayList<>() : new ArrayList<>(value);
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float size = Math.min(getWidth(), getHeight());
        float pad = size * .08f;
        RectF oval = new RectF((getWidth()-size)/2+pad, (getHeight()-size)/2+pad, (getWidth()+size)/2-pad, (getHeight()+size)/2-pad);
        double total = 0;
        for (CategoryTotal item : data) total += Math.max(0, item.total);
        if (total <= 0) {
            paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(size*.12f); paint.setColor(0x33444444);
            canvas.drawOval(oval, paint); return;
        }
        paint.setStyle(Paint.Style.FILL);
        float start = -90;
        int count = Math.min(8, data.size());
        for (int i=0;i<count;i++) {
            float sweep = (float)(Math.max(0,data.get(i).total)/total*360f);
            paint.setColor(palette[i%palette.length]);
            canvas.drawArc(oval, start, sweep, true, paint);
            start += sweep;
        }
        paint.setColor(resolveSurfaceColor());
        canvas.drawCircle(getWidth()/2f, getHeight()/2f, size*.23f, paint);
    }

    private int resolveSurfaceColor() {
        android.util.TypedValue value = new android.util.TypedValue();
        if (getContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorSurface, value, true)) return value.data;
        return Color.WHITE;
    }
}
