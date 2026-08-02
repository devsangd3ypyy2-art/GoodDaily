package com.sangapp.gooddaily.util;

import java.text.NumberFormat;
import java.util.Locale;

public final class MoneyUtils {
    private static final NumberFormat FORMAT = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    private MoneyUtils() {}
    public static String format(double value) { return FORMAT.format(Math.round(value)) + " ₫"; }
}
