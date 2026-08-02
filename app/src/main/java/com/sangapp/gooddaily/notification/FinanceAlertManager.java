package com.sangapp.gooddaily.notification;

import android.app.NotificationManager;
import android.content.Context;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.sangapp.gooddaily.R;
import com.sangapp.gooddaily.data.local.prefs.LocalUserStore;
import com.sangapp.gooddaily.util.MoneyUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class FinanceAlertManager {
    private FinanceAlertManager() {}

    public static void check(Context context, double monthExpense) {
        LocalUserStore store = new LocalUserStore(context);
        double budget = store.getMonthlyBudget();
        if (!store.isFinancialAlertEnabled() || budget <= 0) return;

        int level = monthExpense >= budget ? 100 : monthExpense >= budget * 0.9 ? 90 : monthExpense >= budget * 0.7 ? 70 : 0;
        if (level == 0) return;

        String monthKey = new SimpleDateFormat("yyyy-MM", Locale.US).format(new Date());
        android.content.SharedPreferences prefs = context.getSharedPreferences("good_daily_finance_alert", Context.MODE_PRIVATE);
        int lastLevel = prefs.getInt("level_" + monthKey, 0);
        if (lastLevel >= level) return;
        prefs.edit().putInt("level_" + monthKey, level).apply();

        String channelId = NotificationSoundManager.ensureChannel(
                context,
                "finance_alerts",
                "Cảnh báo tài chính",
                "Cảnh báo khi chi tiêu gần hoặc vượt ngân sách tháng",
                NotificationManager.IMPORTANCE_HIGH);

        String title = level >= 100 ? "Đã vượt ngân sách tháng" : "Chi tiêu đã đạt " + level + "% ngân sách";
        String message = "Đã chi " + MoneyUtils.format(monthExpense) + " trên hạn mức " + MoneyUtils.format(budget) + ".";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_finance_alert)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);
        NotificationSoundManager.applySoundForLegacy(context, builder);
        try {
            NotificationManagerCompat.from(context).notify(4100 + level, builder.build());
        } catch (SecurityException ignored) {
            // Chưa cấp quyền thông báo.
        }
    }
}
