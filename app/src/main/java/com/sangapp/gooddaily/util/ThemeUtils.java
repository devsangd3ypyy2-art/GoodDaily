package com.sangapp.gooddaily.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.sangapp.gooddaily.R;

public final class ThemeUtils {
    public static final String THEME_GREEN = "green";
    public static final String THEME_BLUE = "blue";
    public static final String THEME_ORANGE = "orange";
    public static final String THEME_PURPLE = "purple";

    private ThemeUtils() {}

    @ColorInt
    public static int getPrimaryColor(@NonNull Context context, @NonNull String themeKey) {
        switch (themeKey) {
            case THEME_BLUE:
                return ContextCompat.getColor(context, R.color.theme_blue);
            case THEME_ORANGE:
                return ContextCompat.getColor(context, R.color.theme_orange);
            case THEME_PURPLE:
                return ContextCompat.getColor(context, R.color.theme_purple);
            case THEME_GREEN:
            default:
                return ContextCompat.getColor(context, R.color.theme_green);
        }
    }

    @ColorInt
    public static int getContainerColor(@NonNull Context context, @NonNull String themeKey) {
        switch (themeKey) {
            case THEME_BLUE:
                return ContextCompat.getColor(context, R.color.theme_blue_container);
            case THEME_ORANGE:
                return ContextCompat.getColor(context, R.color.theme_orange_container);
            case THEME_PURPLE:
                return ContextCompat.getColor(context, R.color.theme_purple_container);
            case THEME_GREEN:
            default:
                return ContextCompat.getColor(context, R.color.theme_green_container);
        }
    }

    @ColorInt
    public static int getOnContainerColor(@NonNull Context context, @NonNull String themeKey) {
        switch (themeKey) {
            case THEME_BLUE:
                return ContextCompat.getColor(context, R.color.theme_blue_on_container);
            case THEME_ORANGE:
                return ContextCompat.getColor(context, R.color.theme_orange_on_container);
            case THEME_PURPLE:
                return ContextCompat.getColor(context, R.color.theme_purple_on_container);
            case THEME_GREEN:
            default:
                return ContextCompat.getColor(context, R.color.theme_green_on_container);
        }
    }

    @ColorInt
    public static int getContrastingTextColor(@ColorInt int background) {
        return ColorUtils.calculateLuminance(background) > 0.46 ? Color.BLACK : Color.WHITE;
    }

    public static ColorStateList createBottomNavColorStateList(@NonNull Context context, @NonNull String themeKey) {
        int[][] states = new int[][] {
                new int[] { android.R.attr.state_checked },
                new int[] {}
        };
        int[] colors = new int[] {
                getPrimaryColor(context, themeKey),
                ContextCompat.getColor(context, R.color.on_surface_variant)
        };
        return new ColorStateList(states, colors);
    }

    public static void applyBottomNavigation(@NonNull BottomNavigationView view, @NonNull Context context, @NonNull String themeKey) {
        ColorStateList tint = createBottomNavColorStateList(context, themeKey);
        view.setItemIconTintList(tint);
        view.setItemTextColor(tint);
        view.setItemActiveIndicatorEnabled(true);
        view.setItemActiveIndicatorColor(ColorStateList.valueOf(getContainerColor(context, themeKey)));
        view.setItemRippleColor(ColorStateList.valueOf(adjustAlpha(getPrimaryColor(context, themeKey), 0.18f)));
    }

    public static void tintSwitch(@NonNull MaterialSwitch view, @NonNull Context context, @NonNull String themeKey) {
        int accent = getPrimaryColor(context, themeKey);
        int container = getContainerColor(context, themeKey);
        int outline = ContextCompat.getColor(context, R.color.outline);
        int surface = ContextCompat.getColor(context, R.color.surface);

        int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{}
        };
        view.setThumbTintList(new ColorStateList(states, new int[]{accent, surface}));
        view.setTrackTintList(new ColorStateList(states, new int[]{container, outline}));
    }

    public static void tintFilledButton(@NonNull MaterialButton button, @NonNull Context context, @NonNull String themeKey) {
        int accent = getPrimaryColor(context, themeKey);
        int onAccent = getContrastingTextColor(accent);
        button.setBackgroundTintList(ColorStateList.valueOf(accent));
        button.setTextColor(onAccent);
        button.setIconTint(ColorStateList.valueOf(onAccent));
    }

    public static void tintTonalButton(@NonNull MaterialButton button, @NonNull Context context, @NonNull String themeKey) {
        button.setBackgroundTintList(ColorStateList.valueOf(getContainerColor(context, themeKey)));
        int on = getOnContainerColor(context, themeKey);
        button.setTextColor(on);
        button.setIconTint(ColorStateList.valueOf(on));
    }

    @ColorInt
    public static int adjustAlpha(@ColorInt int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }
}
