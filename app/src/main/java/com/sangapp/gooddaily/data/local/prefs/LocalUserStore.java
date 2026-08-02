package com.sangapp.gooddaily.data.local.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.sangapp.gooddaily.util.PasswordUtils;

public class LocalUserStore {
    private static final String PREF = "good_daily_user";
    private final SharedPreferences prefs;

    public LocalUserStore(Context context) {
        prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public boolean hasAccount() { return prefs.contains("username"); }
    public boolean isLoggedIn() { return prefs.getBoolean("logged_in", false); }
    public String getUsername() { return prefs.getString("username", ""); }
    public String getDisplayName() { return prefs.getString("display_name", "Người dùng"); }
    public boolean isReminderEnabled() { return prefs.getBoolean("reminder_enabled", false); }
    public String getThemeKey() { return prefs.getString("theme_key", "green"); }
    public String getAppearanceMode() { return prefs.getString("appearance_mode", "system"); }
    public double getMonthlyBudget() { return Double.longBitsToDouble(prefs.getLong("monthly_budget", Double.doubleToRawLongBits(0))); }
    public boolean isFinancialAlertEnabled() { return prefs.getBoolean("financial_alert_enabled", true); }
    public boolean isHideAmountsEnabled() { return prefs.getBoolean("hide_amounts", false); }
    public String getAvatarUri() { return prefs.getString("avatar_uri", ""); }
    public int getWeeklyVocabularyGoal() { return prefs.getInt("weekly_vocabulary_goal", 70); }
    public String getMusicUri() { return prefs.getString("music_uri", ""); }
    public String getMusicName() { return prefs.getString("music_name", "Chưa chọn bài hát"); }
    public String getNotificationSoundUri() { return prefs.getString("notification_sound_uri", ""); }
    public String getNotificationSoundName() { return prefs.getString("notification_sound_name", "Âm thanh mặc định"); }
    public boolean isDynamicColorsEnabled() { return prefs.getBoolean("dynamic_colors_enabled", false); }

    public boolean register(String displayName, String username, String password) {
        if (displayName.trim().isEmpty() || username.trim().isEmpty() || password.length() < 4) return false;
        String salt = PasswordUtils.newSalt();
        prefs.edit()
                .putString("display_name", displayName.trim())
                .putString("username", username.trim())
                .putString("salt", salt)
                .putString("password_hash", PasswordUtils.hash(password, salt))
                .putBoolean("logged_in", true)
                .apply();
        return true;
    }

    public boolean login(String username, String password) {
        String salt = prefs.getString("salt", "");
        String expected = prefs.getString("password_hash", "");
        boolean ok = getUsername().equals(username.trim()) && expected.equals(PasswordUtils.hash(password, salt));
        if (ok) prefs.edit().putBoolean("logged_in", true).apply();
        return ok;
    }

    public boolean changePassword(String oldPassword, String newPassword) {
        if (newPassword == null || newPassword.length() < 4) return false;
        String salt = prefs.getString("salt", "");
        String expected = prefs.getString("password_hash", "");
        if (!expected.equals(PasswordUtils.hash(oldPassword == null ? "" : oldPassword, salt))) return false;
        String newSalt = PasswordUtils.newSalt();
        prefs.edit()
                .putString("salt", newSalt)
                .putString("password_hash", PasswordUtils.hash(newPassword, newSalt))
                .apply();
        return true;
    }

    public void logout() { prefs.edit().putBoolean("logged_in", false).apply(); }
    public void setReminderEnabled(boolean enabled) { prefs.edit().putBoolean("reminder_enabled", enabled).apply(); }
    public void setThemeKey(String key) { prefs.edit().putString("theme_key", key).apply(); }
    public void setAppearanceMode(String mode) { prefs.edit().putString("appearance_mode", mode == null ? "system" : mode).apply(); }
    public void setMonthlyBudget(double amount) { prefs.edit().putLong("monthly_budget", Double.doubleToRawLongBits(Math.max(0, amount))).apply(); }
    public void setFinancialAlertEnabled(boolean enabled) { prefs.edit().putBoolean("financial_alert_enabled", enabled).apply(); }
    public void setHideAmountsEnabled(boolean enabled) { prefs.edit().putBoolean("hide_amounts", enabled).apply(); }
    public void setAvatarUri(String uri) { prefs.edit().putString("avatar_uri", uri == null ? "" : uri).apply(); }
    public void setWeeklyVocabularyGoal(int value) { prefs.edit().putInt("weekly_vocabulary_goal", Math.max(1, value)).apply(); }
    public void setMusic(String uri, String name) {
        prefs.edit()
                .putString("music_uri", uri == null ? "" : uri)
                .putString("music_name", name == null || name.trim().isEmpty() ? "Bài hát đã chọn" : name.trim())
                .apply();
    }
    public void setNotificationSound(String uri, String name) {
        prefs.edit()
                .putString("notification_sound_uri", uri == null ? "" : uri)
                .putString("notification_sound_name", name == null || name.trim().isEmpty() ? "Âm thanh mặc định" : name.trim())
                .apply();
    }
    public void setDynamicColorsEnabled(boolean enabled) { prefs.edit().putBoolean("dynamic_colors_enabled", enabled).apply(); }
}
