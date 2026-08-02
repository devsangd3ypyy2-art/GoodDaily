package com.sangapp.gooddaily.feature.metaphysics.ui.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.sangapp.gooddaily.R;

import java.util.Arrays;

/**
 * Draws a six-line I Ching hexagram without relying on text glyph alignment.
 * Input arrays are always bottom-up: index 0 is line 1, index 5 is line 6.
 */
public class HexagramView extends View {
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint placeholderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final boolean[] linesBottomUp = new boolean[6];
    private final boolean[] movingBottomUp = new boolean[6];
    private final boolean[] presentBottomUp = new boolean[6];
    private boolean showLineNumbers = true;
    private float pulseAlpha = 1f;
    private ValueAnimator pulseAnimator;

    public HexagramView(Context context) {
        this(context, null);
    }

    public HexagramView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setMinimumHeight((int) dp(232));
        linePaint.setStyle(Paint.Style.FILL);
        labelPaint.setTextSize(sp(11));
        labelPaint.setTextAlign(Paint.Align.CENTER);
        placeholderPaint.setStyle(Paint.Style.STROKE);
        placeholderPaint.setStrokeWidth(dp(2));
        placeholderPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setShowLineNumbers(boolean show) {
        showLineNumbers = show;
        invalidate();
    }

    public void clearLines() {
        Arrays.fill(linesBottomUp, false);
        Arrays.fill(movingBottomUp, false);
        Arrays.fill(presentBottomUp, false);
        stopPulse();
        invalidate();
    }

    public void setHexagram(boolean[] lines, int[] movingPositions) {
        if (lines == null || lines.length != 6) {
            clearLines();
            return;
        }
        System.arraycopy(lines, 0, linesBottomUp, 0, 6);
        Arrays.fill(presentBottomUp, true);
        Arrays.fill(movingBottomUp, false);
        if (movingPositions != null) {
            for (int position : movingPositions) {
                if (position >= 1 && position <= 6) movingBottomUp[position - 1] = true;
            }
        }
        updatePulse();
        invalidate();
    }

    /**
     * Values are 6/7/8/9; 0 means that line has not been cast yet.
     * When changedState is true, moving yin/yang lines are flipped.
     */
    public void setLineValues(int[] values, boolean changedState) {
        Arrays.fill(presentBottomUp, false);
        Arrays.fill(movingBottomUp, false);
        Arrays.fill(linesBottomUp, false);
        if (values != null) {
            int length = Math.min(6, values.length);
            for (int i = 0; i < length; i++) {
                int value = values[i];
                if (value < 6 || value > 9) continue;
                boolean yang = value == 7 || value == 9;
                boolean moving = value == 6 || value == 9;
                linesBottomUp[i] = changedState && moving ? !yang : yang;
                movingBottomUp[i] = !changedState && moving;
                presentBottomUp[i] = true;
            }
        }
        updatePulse();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        float height = getHeight();
        float leftLabelWidth = showLineNumbers ? dp(28) : 0;
        float horizontalPadding = dp(12);
        float lineLeft = horizontalPadding + leftLabelWidth;
        float lineRight = width - horizontalPadding;
        float lineWidth = Math.max(dp(80), lineRight - lineLeft);
        float lineHeight = dp(12);
        float rowHeight = height / 6f;
        float segmentGap = Math.max(dp(14), lineWidth * 0.16f);

        @ColorInt int normalColor = ContextCompat.getColor(getContext(), R.color.hexagram_line);
        @ColorInt int movingColor = ContextCompat.getColor(getContext(), R.color.hexagram_moving_line);
        @ColorInt int placeholderColor = ContextCompat.getColor(getContext(), R.color.hexagram_placeholder);
        @ColorInt int labelColor = ContextCompat.getColor(getContext(), R.color.on_surface_variant);
        labelPaint.setColor(labelColor);
        placeholderPaint.setColor(placeholderColor);

        // Draw line 6 at the top and line 1 at the bottom.
        for (int visualRow = 0; visualRow < 6; visualRow++) {
            int lineIndex = 5 - visualRow;
            float centerY = rowHeight * visualRow + rowHeight / 2f;
            if (showLineNumbers) {
                canvas.drawText(String.valueOf(lineIndex + 1), horizontalPadding + leftLabelWidth / 2f,
                        centerY - (labelPaint.ascent() + labelPaint.descent()) / 2f, labelPaint);
            }

            if (!presentBottomUp[lineIndex]) {
                float dash = dp(12);
                float cursor = lineLeft;
                while (cursor < lineRight) {
                    canvas.drawLine(cursor, centerY, Math.min(cursor + dash, lineRight), centerY, placeholderPaint);
                    cursor += dash + dp(8);
                }
                continue;
            }

            boolean moving = movingBottomUp[lineIndex];
            int color = moving ? movingColor : normalColor;
            linePaint.setColor(color);
            linePaint.setAlpha(moving ? Math.round(255 * pulseAlpha) : 255);

            if (linesBottomUp[lineIndex]) {
                canvas.drawRoundRect(new RectF(lineLeft, centerY - lineHeight / 2f,
                        lineRight, centerY + lineHeight / 2f), lineHeight / 2f, lineHeight / 2f, linePaint);
            } else {
                float segmentWidth = (lineWidth - segmentGap) / 2f;
                canvas.drawRoundRect(new RectF(lineLeft, centerY - lineHeight / 2f,
                        lineLeft + segmentWidth, centerY + lineHeight / 2f),
                        lineHeight / 2f, lineHeight / 2f, linePaint);
                canvas.drawRoundRect(new RectF(lineRight - segmentWidth, centerY - lineHeight / 2f,
                        lineRight, centerY + lineHeight / 2f),
                        lineHeight / 2f, lineHeight / 2f, linePaint);
            }
        }
    }

    private void updatePulse() {
        boolean hasMoving = false;
        for (boolean moving : movingBottomUp) hasMoving |= moving;
        if (hasMoving) startPulse(); else stopPulse();
    }

    private void startPulse() {
        if (pulseAnimator != null) return;
        pulseAnimator = ValueAnimator.ofFloat(1f, 0.42f, 1f);
        pulseAnimator.setDuration(1150L);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.addUpdateListener(animation -> {
            pulseAlpha = (float) animation.getAnimatedValue();
            invalidate();
        });
        pulseAnimator.start();
    }

    private void stopPulse() {
        if (pulseAnimator != null) {
            pulseAnimator.cancel();
            pulseAnimator = null;
        }
        pulseAlpha = 1f;
    }

    @Override
    protected void onDetachedFromWindow() {
        stopPulse();
        super.onDetachedFromWindow();
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private float sp(float value) {
        return value * getResources().getDisplayMetrics().scaledDensity;
    }
}
