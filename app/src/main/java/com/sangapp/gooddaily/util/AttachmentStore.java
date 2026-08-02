package com.sangapp.gooddaily.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public final class AttachmentStore {
    private AttachmentStore() {}

    public static String copyToInternal(Context context, Uri source) throws Exception {
        File directory = new File(context.getFilesDir(), "attachments");
        if (!directory.exists() && !directory.mkdirs()) throw new IllegalStateException("Không tạo được thư mục đính kèm");
        String name = displayName(context, source);
        if (name == null || name.trim().isEmpty()) name = "attachment_" + System.currentTimeMillis();
        name = name.replaceAll("[^a-zA-Z0-9._-]", "_");
        File target = new File(directory, System.currentTimeMillis() + "_" + name);
        try (InputStream in = context.getContentResolver().openInputStream(source);
             FileOutputStream out = new FileOutputStream(target)) {
            if (in == null) throw new IllegalArgumentException("Không đọc được file");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) >= 0) out.write(buffer, 0, read);
        }
        return target.getAbsolutePath();
    }

    public static String displayName(Context context, Uri uri) {
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) return cursor.getString(index);
            }
        } catch (Exception ignored) {}
        return uri.getLastPathSegment();
    }
}
