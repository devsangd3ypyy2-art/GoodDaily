package com.sangapp.gooddaily.ui.common;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.sangapp.gooddaily.R;
import com.sangapp.gooddaily.data.local.prefs.LocalUserStore;
import com.sangapp.gooddaily.util.ThemeUtils;

import java.util.List;

/**
 * Renders consistent, discoverable 2-column tool tiles inside each main module.
 * Advanced features remain owned by their module instead of being hidden in one hub.
 */
public final class ModuleToolsRenderer {
    private ModuleToolsRenderer() {}

    public static final class ToolItem {
        public final String title;
        public final String subtitle;
        @DrawableRes public final int icon;
        public final View.OnClickListener listener;

        public ToolItem(@NonNull String title,
                        @NonNull String subtitle,
                        @DrawableRes int icon,
                        @NonNull View.OnClickListener listener) {
            this.title = title;
            this.subtitle = subtitle;
            this.icon = icon;
            this.listener = listener;
        }
    }

    public static void render(@NonNull LinearLayout container, @NonNull List<ToolItem> items) {
        container.removeAllViews();
        Context context = container.getContext();
        String themeKey = new LocalUserStore(context).getThemeKey();

        for (int index = 0; index < items.size(); index += 2) {
            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.TOP);

            ToolItem first = items.get(index);
            row.addView(createCard(context, first, themeKey), weightedParams(context, 0, 6));

            if (index + 1 < items.size()) {
                ToolItem second = items.get(index + 1);
                row.addView(createCard(context, second, themeKey), weightedParams(context, 6, 0));
            } else {
                View spacer = new View(context);
                row.addView(spacer, weightedParams(context, 6, 0));
            }

            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            rowParams.bottomMargin = dp(context, 12);
            container.addView(row, rowParams);
        }
    }

    private static MaterialCardView createCard(Context context,
                                               ToolItem item,
                                               String themeKey) {
        int accent = ThemeUtils.getPrimaryColor(context, themeKey);
        int accentContainer = ThemeUtils.getContainerColor(context, themeKey);

        MaterialCardView card = new MaterialCardView(context);
        card.setMinimumHeight(dp(context, 148));
        card.setRadius(dp(context, 22));
        card.setCardElevation(dp(context, 1));
        card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.surface));
        card.setStrokeColor(ContextCompat.getColor(context, R.color.surface_variant));
        card.setStrokeWidth(dp(context, 1));
        card.setClickable(true);
        card.setFocusable(true);
        card.setContentDescription(item.title + ". " + item.subtitle);
        card.setOnClickListener(item.listener);

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(context, 14), dp(context, 14), dp(context, 14), dp(context, 14));

        MaterialCardView iconBox = new MaterialCardView(context);
        iconBox.setRadius(dp(context, 15));
        iconBox.setCardElevation(0);
        iconBox.setCardBackgroundColor(accentContainer);

        ImageView icon = new ImageView(context);
        icon.setImageResource(item.icon);
        icon.setColorFilter(accent);
        icon.setPadding(dp(context, 11), dp(context, 11), dp(context, 11), dp(context, 11));
        iconBox.addView(icon, new ViewGroup.LayoutParams(dp(context, 46), dp(context, 46)));
        content.addView(iconBox, new LinearLayout.LayoutParams(dp(context, 46), dp(context, 46)));

        TextView title = new TextView(context);
        title.setText(item.title);
        title.setTextSize(15);
        title.setTextColor(ContextCompat.getColor(context, R.color.on_surface));
        title.setTypeface(title.getTypeface(), Typeface.BOLD);
        title.setMaxLines(2);
        title.setEllipsize(android.text.TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.topMargin = dp(context, 12);
        content.addView(title, titleParams);

        TextView subtitle = new TextView(context);
        subtitle.setText(item.subtitle);
        subtitle.setTextSize(12);
        subtitle.setTextColor(ContextCompat.getColor(context, R.color.on_surface_variant));
        subtitle.setMaxLines(2);
        subtitle.setEllipsize(android.text.TextUtils.TruncateAt.END);
        subtitle.setLineSpacing(0f, 1.05f);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        subtitleParams.topMargin = dp(context, 5);
        content.addView(subtitle, subtitleParams);

        card.addView(content);
        return card;
    }

    private static LinearLayout.LayoutParams weightedParams(Context context, int startMargin, int endMargin) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        );
        params.leftMargin = dp(context, startMargin);
        params.rightMargin = dp(context, endMargin);
        return params;
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
