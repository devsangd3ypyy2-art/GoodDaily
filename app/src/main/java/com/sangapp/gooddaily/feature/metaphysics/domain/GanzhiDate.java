package com.sangapp.gooddaily.feature.metaphysics.domain;

import java.util.Calendar;
import java.util.TimeZone;

public final class GanzhiDate {
    public final HeavenlyStem dayStem;
    public final EarthlyBranch dayBranch;
    public final EarthlyBranch monthBranch;

    public GanzhiDate(HeavenlyStem dayStem, EarthlyBranch dayBranch, EarthlyBranch monthBranch) {
        this.dayStem = dayStem;
        this.dayBranch = dayBranch;
        this.monthBranch = monthBranch;
    }

    public static GanzhiDate approximate(Calendar calendar) {
        Calendar target = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        target.clear();
        target.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);

        // 27/10/2024 được dùng làm mốc Giáp Tý; người dùng vẫn có thể chỉnh tay trong UI.
        Calendar anchor = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        anchor.clear();
        anchor.set(2024, Calendar.OCTOBER, 27, 0, 0, 0);
        long days = Math.floorDiv(target.getTimeInMillis() - anchor.getTimeInMillis(), 86_400_000L);
        int cycle = (int) Math.floorMod(days, 60L);
        HeavenlyStem stem = HeavenlyStem.values()[cycle % 10];
        EarthlyBranch branch = EarthlyBranch.values()[cycle % 12];
        EarthlyBranch month = approximateMonthBranch(calendar.get(Calendar.MONTH) + 1);
        return new GanzhiDate(stem, branch, month);
    }

    private static EarthlyBranch approximateMonthBranch(int month) {
        switch (month) {
            case 1: return EarthlyBranch.CHOU;
            case 2: return EarthlyBranch.YIN;
            case 3: return EarthlyBranch.MAO;
            case 4: return EarthlyBranch.CHEN;
            case 5: return EarthlyBranch.SI;
            case 6: return EarthlyBranch.WU;
            case 7: return EarthlyBranch.WEI;
            case 8: return EarthlyBranch.SHEN;
            case 9: return EarthlyBranch.YOU;
            case 10: return EarthlyBranch.XU;
            case 11: return EarthlyBranch.HAI;
            case 12: return EarthlyBranch.ZI;
            default: return EarthlyBranch.ZI;
        }
    }
}
