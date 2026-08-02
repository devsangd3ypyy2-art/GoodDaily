package com.sangapp.gooddaily.data.backup;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;

import com.sangapp.gooddaily.data.local.entity.PersonalRecordEntity;
import com.sangapp.gooddaily.feature.FeatureDefinition;
import com.sangapp.gooddaily.util.DateUtils;

import java.io.OutputStream;
import java.util.List;

public final class PersonalRecordsPdfExporter {
    private PersonalRecordsPdfExporter() {}

    public static void export(Context context, Uri uri, FeatureDefinition definition,
                              List<PersonalRecordEntity> records) throws Exception {
        PdfDocument pdf = new PdfDocument();
        Paint title = new Paint(Paint.ANTI_ALIAS_FLAG); title.setTextSize(20); title.setFakeBoldText(true);
        Paint body = new Paint(Paint.ANTI_ALIAS_FLAG); body.setTextSize(11);
        Paint muted = new Paint(Paint.ANTI_ALIAS_FLAG); muted.setTextSize(9); muted.setColor(0xff666666);
        int pageNo = 1, y = 55;
        PdfDocument.Page page = newPage(pdf, pageNo);
        Canvas canvas = page.getCanvas();
        canvas.drawText("GOOD DAILY · " + definition.title, 42, y, title); y += 26;
        canvas.drawText("Xuất ngày " + DateUtils.formatDateTime(System.currentTimeMillis()), 42, y, muted); y += 28;
        for (PersonalRecordEntity item : records) {
            List<String> lines = wrap((item.title == null ? "Không tiêu đề" : item.title)
                    + " · " + (item.dateKey == null ? "" : item.dateKey), 78);
            List<String> details = wrap(item.details == null ? "" : item.details, 90);
            int need = 24 + lines.size() * 15 + details.size() * 14;
            if (y + need > 790) {
                pdf.finishPage(page); pageNo++; page = newPage(pdf, pageNo); canvas = page.getCanvas(); y = 50;
            }
            body.setFakeBoldText(true);
            for (String line : lines) { canvas.drawText(line, 42, y, body); y += 15; }
            body.setFakeBoldText(false);
            for (String line : details) { canvas.drawText(line, 52, y, body); y += 14; }
            String meta = "Giá trị: " + item.numericValue + " · Hiện tại: " + item.secondaryValue
                    + " · Trạng thái: " + safe(item.status) + " · Tag: " + safe(item.tags);
            for (String line : wrap(meta, 100)) { canvas.drawText(line, 52, y, muted); y += 13; }
            y += 10;
        }
        pdf.finishPage(page);
        try (OutputStream out = context.getContentResolver().openOutputStream(uri)) {
            if (out == null) throw new IllegalStateException("Không thể mở file PDF");
            pdf.writeTo(out);
        } finally { pdf.close(); }
    }

    private static PdfDocument.Page newPage(PdfDocument pdf, int pageNo) {
        return pdf.startPage(new PdfDocument.PageInfo.Builder(595, 842, pageNo).create());
    }

    private static java.util.List<String> wrap(String text, int max) {
        java.util.ArrayList<String> out = new java.util.ArrayList<>();
        if (text == null || text.trim().isEmpty()) return out;
        for (String paragraph : text.split("\\n")) {
            StringBuilder line = new StringBuilder();
            for (String word : paragraph.trim().split("\\s+")) {
                if (line.length() > 0 && line.length() + word.length() + 1 > max) {
                    out.add(line.toString()); line.setLength(0);
                }
                if (line.length() > 0) line.append(' ');
                line.append(word);
            }
            if (line.length() > 0) out.add(line.toString());
        }
        return out;
    }

    private static String safe(String value) { return value == null ? "" : value; }
}
