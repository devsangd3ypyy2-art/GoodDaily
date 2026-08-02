package com.sangapp.gooddaily.util;

import android.content.Context;

import com.sangapp.gooddaily.data.local.prefs.LocalUserStore;
import com.sangapp.gooddaily.data.local.prefs.SecureStore;

import java.util.HashSet;
import java.util.Set;

public final class SecuritySession {
    public static final String MODULE_FINANCE = "FINANCE";
    public static final String MODULE_JOURNAL = "JOURNAL";
    private static boolean unlocked;
    private static final Set<String> unlockedModules = new HashSet<>();

    private SecuritySession() {}

    public static boolean shouldLock(Context context) {
        SecureStore secure = new SecureStore(context);
        if (!secure.hasPin()) return false;
        LocalUserStore store = new LocalUserStore(context);
        int minutes = store.getAutoLockMinutes();
        long last = store.getLastBackgroundAt();
        if (!unlocked) return true;
        if (minutes <= 0) return false;
        return last > 0 && System.currentTimeMillis() - last >= minutes * 60_000L;
    }

    public static boolean shouldLockModule(Context context, String module) {
        if (module == null || module.trim().isEmpty()) return false;
        SecureStore secure = new SecureStore(context);
        if (!secure.hasPin()) return false;
        LocalUserStore store = new LocalUserStore(context);
        boolean enabled = MODULE_FINANCE.equals(module) ? store.isFinanceLockEnabled()
                : MODULE_JOURNAL.equals(module) && store.isJournalLockEnabled();
        return enabled && !unlockedModules.contains(module);
    }

    public static void markUnlocked() { unlocked = true; }
    public static void markModuleUnlocked(String module) {
        if (module != null && !module.trim().isEmpty()) unlockedModules.add(module);
    }
    public static void lockModule(String module) { unlockedModules.remove(module); }
    public static void lockNow() {
        unlocked = false;
        unlockedModules.clear();
    }
}
