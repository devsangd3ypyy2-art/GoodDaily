package com.sangapp.gooddaily.data.backup;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.sangapp.gooddaily.data.local.entity.TransactionEntity;
import com.sangapp.gooddaily.util.AppExecutors;
import com.sangapp.gooddaily.util.DateUtils;
import com.sangapp.gooddaily.util.MoneyUtils;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/** Xuất báo cáo tài chính cục bộ bằng PdfDocument, không cần server. */
public final class FinancePdfExporter {
    public interface Callback {
        void onSuccess();
        void onError(String message);
    }

    private static final int PAGE_WIDTH = 595;
    private static final int PAGE_HEIGHT = 842;
    private static final int MARGIN = 42;

    private FinancePdfExporter() {}

    public static void export(Context context,
                              Uri uri,
                              List<TransactionEntity> transactions,
                              double weekIncome,
                              double weekExpense,
                              double monthIncome,
                              double monthExpense,
                              double yearIncome,
                              double yearExpense,
                              double budget,
                              Callback callback) {
        Context appContext = context.getApplicationContext();
        List<TransactionEntity> safeItems = transactions == null
                ? new ArrayList<>()
                : new ArrayList<>(transactions);

        AppExecutors.io().execute(() -> {
            PdfDocument document = new PdfDocument();
            try {
                Paint title = paint(22f, Color.rgb(31, 27, 22), true);
                Paint heading = paint(14f, Color.rgb(31, 27, 22), true);
                Paint body = paint(10.5f, Color.rgb(55, 51, 47), false);
                Paint muted = paint(9f, Color.rgb(105, 96, 87), false);
                Paint line = paint(1f, Color.rgb(220, 214, 205), false);

                PageWriter writer = new PageWriter(document, title, heading, body, muted, line);
                writer.startPage();
                writer.text("GOOD DAILY - BÁO CÁO TÀI CHÍNH", title, MARGIN, 66);
                writer.text("Ngày xuất: " + DateUtils.formatDateTime(System.currentTimeMillis()), muted, MARGIN, 86);
                writer.horizontalLine(100);

                writer.section("TỔNG HỢP", 126);
                writer.summaryRow("Tuần này", weekIncome, weekExpense);
                writer.summaryRow("Tháng này", monthIncome, monthExpense);
                writer.summaryRow("Năm nay", yearIncome, yearExpense);
                if (budget > 0) {
                    double percent = monthExpense / budget * 100.0;
                    writer.wrappedText("Ngân sách tháng: " + MoneyUtils.format(budget)
                            + " - Đã dùng: " + String.format(java.util.Locale.getDefault(), "%.0f%%", percent), body);
                }

                writer.section("LỊCH SỬ GIAO DỊCH", writer.y + 20);
                if (safeItems.isEmpty()) {
                    writer.wrappedText("Chưa có giao dịch.", body);
                } else {
                    for (TransactionEntity item : safeItems) {
                        writer.ensureSpace(48);
                        boolean income = "INCOME".equals(item.type);
                        String amount = (income ? "+" : "-") + MoneyUtils.format(item.amount);
                        writer.text(safe(item.category, "Chưa phân loại"), heading, MARGIN, writer.y);
                        writer.text(amount, heading, PAGE_WIDTH - MARGIN - heading.measureText(amount), writer.y);
                        writer.y += 17;
                        String detail = DateUtils.formatDateTime(item.transactionTime)
                                + " | " + accountName(item.account);
                        writer.text(detail, muted, MARGIN, writer.y);
                        writer.y += 14;
                        if (item.note != null && !item.note.trim().isEmpty()) {
                            writer.wrappedText("Ghi chú: " + item.note.trim(), body);
                        }
                        writer.horizontalLine(writer.y + 5);
                        writer.y += 17;
                    }
                }
                writer.finishPage();

                try (OutputStream output = appContext.getContentResolver().openOutputStream(uri, "w")) {
                    if (output == null) throw new IllegalStateException("Không mở được file đích.");
                    document.writeTo(output);
                }
                postSuccess(callback);
            } catch (Exception e) {
                postError(callback, e.getMessage() == null ? "Không thể tạo báo cáo PDF." : e.getMessage());
            } finally {
                document.close();
            }
        });
    }

    private static Paint paint(float size, int color, boolean bold) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setTextSize(size);
        p.setColor(color);
        p.setTypeface(bold ? Typeface.create(Typeface.DEFAULT, Typeface.BOLD) : Typeface.DEFAULT);
        return p;
    }

    private static String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private static String accountName(String account) {
        if ("BANK".equals(account)) return "Ngân hàng";
        if ("EWALLET".equals(account)) return "Ví điện tử";
        return "Tiền mặt";
    }

    private static void postSuccess(Callback callback) {
        new Handler(Looper.getMainLooper()).post(callback::onSuccess);
    }

    private static void postError(Callback callback, String message) {
        new Handler(Looper.getMainLooper()).post(() -> callback.onError(message));
    }

    private static final class PageWriter {
        private final PdfDocument document;
        private final Paint title;
        private final Paint heading;
        private final Paint body;
        private final Paint muted;
        private final Paint line;
        private PdfDocument.Page page;
        private Canvas canvas;
        private int pageNumber;
        private float y;

        PageWriter(PdfDocument document, Paint title, Paint heading, Paint body, Paint muted, Paint line) {
            this.document = document;
            this.title = title;
            this.heading = heading;
            this.body = body;
            this.muted = muted;
            this.line = line;
        }

        void startPage() {
            pageNumber++;
            page = document.startPage(new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create());
            canvas = page.getCanvas();
            y = MARGIN;
        }

        void finishPage() {
            if (page == null) return;
            String footer = "Good Daily | Trang " + pageNumber;
            canvas.drawText(footer, PAGE_WIDTH - MARGIN - muted.measureText(footer), PAGE_HEIGHT - 24, muted);
            document.finishPage(page);
            page = null;
        }

        void ensureSpace(float needed) {
            if (y + needed < PAGE_HEIGHT - 48) return;
            finishPage();
            startPage();
            text("GOOD DAILY - BÁO CÁO TÀI CHÍNH", heading, MARGIN, 58);
            horizontalLine(72);
            y = 94;
        }

        void section(String value, float targetY) {
            y = targetY;
            ensureSpace(32);
            text(value, heading, MARGIN, y);
            y += 20;
        }

        void summaryRow(String label, double income, double expense) {
            ensureSpace(24);
            text(label, heading, MARGIN, y);
            String value = "Thu " + MoneyUtils.format(income) + " | Chi " + MoneyUtils.format(expense)
                    + " | Còn " + MoneyUtils.format(income - expense);
            text(value, body, 150, y);
            y += 22;
        }

        void horizontalLine(float lineY) {
            canvas.drawRect(MARGIN, lineY, PAGE_WIDTH - MARGIN, lineY + 1, line);
        }

        void text(String value, Paint paint, float x, float baseline) {
            canvas.drawText(value == null ? "" : value, x, baseline, paint);
        }

        void wrappedText(String value, Paint paint) {
            if (value == null || value.isEmpty()) return;
            float maxWidth = PAGE_WIDTH - (MARGIN * 2f);
            String[] words = value.replace('\n', ' ').split("\\s+");
            StringBuilder lineBuilder = new StringBuilder();
            for (String word : words) {
                String candidate = lineBuilder.length() == 0 ? word : lineBuilder + " " + word;
                if (paint.measureText(candidate) <= maxWidth) {
                    lineBuilder.setLength(0);
                    lineBuilder.append(candidate);
                } else {
                    ensureSpace(18);
                    text(lineBuilder.toString(), paint, MARGIN, y);
                    y += 15;
                    lineBuilder.setLength(0);
                    lineBuilder.append(word);
                }
            }
            if (lineBuilder.length() > 0) {
                ensureSpace(18);
                text(lineBuilder.toString(), paint, MARGIN, y);
                y += 15;
            }
        }
    }
}
