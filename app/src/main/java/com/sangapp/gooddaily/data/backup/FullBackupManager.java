package com.sangapp.gooddaily.data.backup;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import androidx.core.content.ContextCompat;

import com.sangapp.gooddaily.BuildConfig;
import com.sangapp.gooddaily.data.local.GoodDailyDatabase;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class FullBackupManager {
    private static final byte[] MAGIC = new byte[]{'G','D','Z','1'};
    private final Context context;

    public interface Callback { void onSuccess(String message); void onError(String message); }
    public interface PreviewCallback { void onPreview(String text); void onError(String message); }

    public FullBackupManager(Context context) { this.context = context.getApplicationContext(); }

    public void exportTo(Uri destination, String password, Callback callback) {
        new Thread(() -> {
            try {
                byte[] zip = createZipBytes();
                byte[] output = password == null || password.isEmpty() ? zip : encrypt(zip, password);
                try (OutputStream out = context.getContentResolver().openOutputStream(destination)) {
                    if (out == null) throw new IllegalStateException("Không mở được file đích");
                    out.write(output);
                }
                postSuccess(callback, "Đã tạo backup đầy đủ" + (password == null || password.isEmpty() ? " dạng ZIP." : " có mật khẩu."));
            } catch (Exception e) { postError(callback, e); }
        }).start();
    }

    public File createInternalAutoBackup() throws Exception {
        File dir = new File(context.getFilesDir(), "auto_backups");
        if (!dir.exists() && !dir.mkdirs()) throw new IllegalStateException("Không tạo được thư mục backup");
        File target = new File(dir, "GoodDaily_Auto_" + System.currentTimeMillis() + ".zip");
        try (FileOutputStream out = new FileOutputStream(target)) { out.write(createZipBytes()); }
        prune(dir, 8);
        return target;
    }

    public List<File> listAutoBackups() {
        File dir = new File(context.getFilesDir(), "auto_backups");
        File[] files = dir.listFiles((d, name) -> name.endsWith(".zip") || name.endsWith(".gdz"));
        List<File> result = new ArrayList<>();
        if (files != null) {
            java.util.Arrays.sort(files, (a,b) -> Long.compare(b.lastModified(), a.lastModified()));
            java.util.Collections.addAll(result, files);
        }
        return result;
    }

    public void preview(Uri source, String password, PreviewCallback callback) {
        new Thread(() -> {
            try {
                byte[] bytes = readAll(context.getContentResolver().openInputStream(source));
                byte[] zip = isEncrypted(bytes) ? decrypt(bytes, password) : bytes;
                String manifest = readZipEntry(zip, "manifest.json");
                JSONObject json = new JSONObject(manifest);
                String text = "Ứng dụng: " + json.optString("appVersion") +
                        "\nTạo lúc: " + json.optString("createdAt") +
                        "\nDatabase: " + json.optString("databaseName") +
                        "\nĐính kèm: " + json.optInt("attachmentCount") +
                        "\nMã hóa: " + isEncrypted(bytes);
                ContextCompat.getMainExecutor(context).execute(() -> callback.onPreview(text));
            } catch (Exception e) { ContextCompat.getMainExecutor(context).execute(() -> callback.onError(message(e))); }
        }).start();
    }

    public void importFrom(Uri source, String password, Callback callback) {
        importFrom(source, password, false, callback);
    }

    public void importFrom(Uri source, String password, boolean merge, Callback callback) {
        new Thread(() -> {
            File temp = null;
            try {
                byte[] bytes = readAll(context.getContentResolver().openInputStream(source));
                byte[] zip = isEncrypted(bytes) ? decrypt(bytes, password) : bytes;
                temp = new File(context.getCacheDir(), "restore_" + System.currentTimeMillis());
                if (!temp.mkdirs()) throw new IllegalStateException("Không tạo được thư mục tạm");
                extract(zip, temp);
                File dbFile = new File(temp, "database/good_daily_database");
                if (!dbFile.exists()) throw new IllegalArgumentException("Backup không chứa database hợp lệ");

                GoodDailyDatabase.closeInstance();
                File liveDb = context.getDatabasePath("good_daily_database");
                File parent = liveDb.getParentFile(); if (parent != null && !parent.exists()) parent.mkdirs();
                if (merge && liveDb.exists()) {
                    mergeDatabase(liveDb, dbFile);
                    File restoredFiles = new File(temp, "files");
                    if (restoredFiles.exists()) copyDirectoryMerge(restoredFiles, context.getFilesDir());
                    postSuccess(callback, "Đã gộp dữ liệu từ backup. Dữ liệu trùng được bỏ qua. Hãy mở lại ứng dụng.");
                } else {
                    copy(dbFile, liveDb);
                    copyIfExists(new File(temp, "database/good_daily_database-wal"), new File(liveDb.getPath()+"-wal"));
                    copyIfExists(new File(temp, "database/good_daily_database-shm"), new File(liveDb.getPath()+"-shm"));

                    File shared = new File(context.getApplicationInfo().dataDir, "shared_prefs");
                    if (!shared.exists()) shared.mkdirs();
                    copyIfExists(new File(temp, "prefs/good_daily_user.xml"), new File(shared, "good_daily_user.xml"));
                    // PIN/Keystore không thể chuyển an toàn giữa hai máy, nên không khôi phục good_daily_secure.xml.
                    File restoredFiles = new File(temp, "files");
                    if (restoredFiles.exists()) copyDirectory(restoredFiles, context.getFilesDir());
                    postSuccess(callback, "Đã thay thế dữ liệu bằng backup. PIN cũ không được chuyển máy; hãy đặt lại PIN sau khi mở app.");
                }
            } catch (Exception e) { postError(callback, e); }
            finally { if (temp != null) deleteRecursively(temp); }
        }).start();
    }

    private byte[] createZipBytes() throws Exception {
        // Flush Room's WAL first so the database and its sidecar files form a consistent snapshot.
        try (android.database.Cursor ignored = GoodDailyDatabase.get(context).getOpenHelper()
                .getWritableDatabase().query("PRAGMA wal_checkpoint(FULL)")) {
            // Executing the query is sufficient; the cursor is closed immediately.
        } catch (Exception ignored) {
            // The WAL/SHM files are still included below as a fallback.
        }
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            JSONObject manifest = new JSONObject();
            manifest.put("format", 2);
            manifest.put("appVersion", BuildConfig.VERSION_NAME);
            manifest.put("createdAt", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(new java.util.Date()));
            manifest.put("databaseName", "good_daily_database");
            manifest.put("attachmentCount", countFiles(new File(context.getFilesDir(), "attachments")));
            addBytes(zip, "manifest.json", manifest.toString(2).getBytes(StandardCharsets.UTF_8));

            File db = context.getDatabasePath("good_daily_database");
            addFileIfExists(zip, db, "database/good_daily_database");
            addFileIfExists(zip, new File(db.getPath()+"-wal"), "database/good_daily_database-wal");
            addFileIfExists(zip, new File(db.getPath()+"-shm"), "database/good_daily_database-shm");

            File prefs = new File(context.getApplicationInfo().dataDir, "shared_prefs");
            addFileIfExists(zip, new File(prefs, "good_daily_user.xml"), "prefs/good_daily_user.xml");
            // Không xuất PIN/khóa Keystore vì khóa thiết bị không thể chuyển sang máy khác.
            addDirectory(zip, context.getFilesDir(), "files", new File(context.getFilesDir(), "auto_backups"));
        }
        return bytes.toByteArray();
    }


    private void mergeDatabase(File liveFile, File importedFile) throws Exception {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(liveFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
        db.execSQL("PRAGMA foreign_keys=OFF");
        db.execSQL("ATTACH DATABASE ? AS imported", new Object[]{importedFile.getAbsolutePath()});
        db.beginTransaction();
        try {
            List<String> tables = new ArrayList<>();
            try (Cursor cursor = db.rawQuery("SELECT name FROM imported.sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'", null)) {
                while (cursor.moveToNext()) tables.add(cursor.getString(0));
            }
            for (String table : tables) {
                if ("room_master_table".equals(table) || "android_metadata".equals(table)
                        || "habit_checkins".equals(table) || "debt_payments".equals(table)
                        || "transaction_attachments".equals(table)) continue;
                mergeSimpleTable(db, table);
            }
            mergeHabitCheckIns(db);
            mergeDebtPayments(db);
            mergeTransactionAttachments(db);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            try { db.execSQL("DETACH DATABASE imported"); } catch (Exception ignored) {}
            db.close();
        }
    }

    private void mergeSimpleTable(SQLiteDatabase db, String table) {
        try {
            List<ColumnInfo> columns = tableColumns(db, "imported", table);
            if (columns.isEmpty() || !tableExists(db, "main", table)) return;
            List<String> liveColumns = new ArrayList<>();
            for (ColumnInfo c : tableColumns(db, "main", table)) liveColumns.add(c.name);
            List<ColumnInfo> common = new ArrayList<>();
            for (ColumnInfo c : columns) if (liveColumns.contains(c.name)) common.add(c);
            if (common.isEmpty()) return;
            boolean autoId = false;
            for (ColumnInfo c : common) if ("id".equals(c.name) && c.primaryKey) autoId = true;
            List<ColumnInfo> insertColumns = new ArrayList<>();
            for (ColumnInfo c : common) if (!(autoId && "id".equals(c.name))) insertColumns.add(c);
            if (insertColumns.isEmpty()) return;
            String cols = joinColumns(insertColumns, null);
            String source = joinColumns(insertColumns, "i");
            StringBuilder equals = new StringBuilder();
            for (ColumnInfo c : insertColumns) {
                if (equals.length() > 0) equals.append(" AND ");
                equals.append("m.").append(qi(c.name)).append(" IS i.").append(qi(c.name));
            }
            String verb = autoId ? "INSERT OR IGNORE" : "INSERT OR REPLACE";
            String sql = verb + " INTO main." + qi(table) + " (" + cols + ") SELECT " + source
                    + " FROM imported." + qi(table) + " i";
            if (autoId) sql += " WHERE NOT EXISTS (SELECT 1 FROM main." + qi(table) + " m WHERE " + equals + ")";
            db.execSQL(sql);
        } catch (Exception ignored) {
            // Backup cũ có thể chưa có đủ cột; các bảng khác vẫn được gộp.
        }
    }

    private void mergeHabitCheckIns(SQLiteDatabase db) {
        if (!tableExists(db,"imported","habit_checkins") || !tableExists(db,"imported","habits")) return;
        try {
            db.execSQL("INSERT OR IGNORE INTO main.habit_checkins(habitId,dateKey,checkedAt) "
                    + "SELECT mh.id, ic.dateKey, ic.checkedAt FROM imported.habit_checkins ic "
                    + "JOIN imported.habits ih ON ih.id=ic.habitId "
                    + "JOIN main.habits mh ON mh.name=ih.name "
                    + "WHERE NOT EXISTS(SELECT 1 FROM main.habit_checkins x WHERE x.habitId=mh.id AND x.dateKey=ic.dateKey)");
        } catch (Exception ignored) {}
    }

    private void mergeDebtPayments(SQLiteDatabase db) {
        if (!tableExists(db,"imported","debt_payments") || !tableExists(db,"imported","debts")) return;
        try {
            db.execSQL("INSERT INTO main.debt_payments(debtId,amount,paidAt,note) "
                    + "SELECT md.id,p.amount,p.paidAt,p.note FROM imported.debt_payments p "
                    + "JOIN imported.debts idb ON idb.id=p.debtId "
                    + "JOIN main.debts md ON md.person IS idb.person AND md.originalAmount=idb.originalAmount AND md.dueDate=idb.dueDate "
                    + "WHERE NOT EXISTS(SELECT 1 FROM main.debt_payments x WHERE x.debtId=md.id AND x.amount=p.amount AND x.paidAt=p.paidAt AND x.note IS p.note)");
        } catch (Exception ignored) {}
    }

    private void mergeTransactionAttachments(SQLiteDatabase db) {
        if (!tableExists(db,"imported","transaction_attachments") || !tableExists(db,"imported","transactions")) return;
        try {
            db.execSQL("INSERT INTO main.transaction_attachments(transactionId,uri,displayName,createdAt) "
                    + "SELECT mt.id,a.uri,a.displayName,a.createdAt FROM imported.transaction_attachments a "
                    + "JOIN imported.transactions it ON it.id=a.transactionId "
                    + "JOIN main.transactions mt ON mt.type IS it.type AND mt.amount=it.amount AND mt.category IS it.category "
                    + "AND mt.account IS it.account AND mt.note IS it.note AND mt.transactionTime=it.transactionTime "
                    + "WHERE NOT EXISTS(SELECT 1 FROM main.transaction_attachments x WHERE x.transactionId=mt.id AND x.uri IS a.uri AND x.createdAt=a.createdAt)");
        } catch (Exception ignored) {}
    }

    private boolean tableExists(SQLiteDatabase db, String schema, String table) {
        try (Cursor c = db.rawQuery("SELECT 1 FROM " + schema + ".sqlite_master WHERE type='table' AND name=? LIMIT 1", new String[]{table})) {
            return c.moveToFirst();
        }
    }

    private List<ColumnInfo> tableColumns(SQLiteDatabase db, String schema, String table) {
        List<ColumnInfo> out = new ArrayList<>();
        try (Cursor c = db.rawQuery("PRAGMA " + schema + ".table_info(" + qi(table) + ")", null)) {
            int nameIndex = c.getColumnIndex("name"), pkIndex = c.getColumnIndex("pk");
            while (c.moveToNext()) out.add(new ColumnInfo(c.getString(nameIndex), c.getInt(pkIndex) > 0));
        }
        return out;
    }

    private String joinColumns(List<ColumnInfo> columns, String alias) {
        StringBuilder out = new StringBuilder();
        for (ColumnInfo c : columns) {
            if (out.length() > 0) out.append(',');
            if (alias != null) out.append(alias).append('.');
            out.append(qi(c.name));
        }
        return out.toString();
    }

    private String qi(String value) { return "`" + value.replace("`", "``") + "`"; }
    private static final class ColumnInfo {
        final String name; final boolean primaryKey;
        ColumnInfo(String name, boolean primaryKey) { this.name=name; this.primaryKey=primaryKey; }
    }

    private void copyDirectoryMerge(File src, File dst) throws Exception {
        if (!dst.exists()) dst.mkdirs();
        File[] files=src.listFiles(); if(files==null)return;
        for(File f:files){
            File target=new File(dst,f.getName());
            if(f.isDirectory()) copyDirectoryMerge(f,target);
            else if(!target.exists()) copy(f,target);
            else if(target.length()!=f.length()) {
                String name=f.getName(); int dot=name.lastIndexOf('.');
                String base=dot>0?name.substring(0,dot):name, ext=dot>0?name.substring(dot):"";
                copy(f,new File(dst,base+"_import_"+System.currentTimeMillis()+ext));
            }
        }
    }

    private byte[] encrypt(byte[] plain, String password) throws Exception {
        byte[] salt = new byte[16], iv = new byte[12]; SecureRandom random = new SecureRandom(); random.nextBytes(salt); random.nextBytes(iv);
        SecretKeySpec key = derive(password, salt);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding"); cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
        byte[] encrypted = cipher.doFinal(plain);
        ByteArrayOutputStream out = new ByteArrayOutputStream(); out.write(MAGIC); out.write(salt); out.write(iv); out.write(encrypted); return out.toByteArray();
    }

    private byte[] decrypt(byte[] payload, String password) throws Exception {
        if (password == null || password.isEmpty()) throw new IllegalArgumentException("Backup có mật khẩu. Hãy nhập mật khẩu.");
        if (payload.length < 32) throw new IllegalArgumentException("File backup bị hỏng");
        byte[] salt = java.util.Arrays.copyOfRange(payload,4,20); byte[] iv=java.util.Arrays.copyOfRange(payload,20,32); byte[] encrypted=java.util.Arrays.copyOfRange(payload,32,payload.length);
        Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding"); cipher.init(Cipher.DECRYPT_MODE,derive(password,salt),new GCMParameterSpec(128,iv)); return cipher.doFinal(encrypted);
    }
    private SecretKeySpec derive(String password, byte[] salt) throws Exception { PBEKeySpec spec=new PBEKeySpec(password.toCharArray(),salt,120000,256); byte[] key=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded(); return new SecretKeySpec(key,"AES"); }
    private boolean isEncrypted(byte[] value){return value.length>4&&value[0]==MAGIC[0]&&value[1]==MAGIC[1]&&value[2]==MAGIC[2]&&value[3]==MAGIC[3];}

    private void extract(byte[] zipBytes, File root) throws Exception {
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry; byte[] buffer=new byte[8192];
            while((entry=zip.getNextEntry())!=null){
                File target=new File(root,entry.getName());
                String rootPath=root.getCanonicalPath()+File.separator;
                if(!target.getCanonicalPath().startsWith(rootPath)) throw new SecurityException("Đường dẫn backup không an toàn");
                if(entry.isDirectory()){target.mkdirs();continue;}
                File parent=target.getParentFile();if(parent!=null)parent.mkdirs();
                try(FileOutputStream out=new FileOutputStream(target)){int n;while((n=zip.read(buffer))>0)out.write(buffer,0,n);}
            }
        }
    }
    private String readZipEntry(byte[] zipBytes,String name)throws Exception{try(ZipInputStream zip=new ZipInputStream(new ByteArrayInputStream(zipBytes))){ZipEntry e;while((e=zip.getNextEntry())!=null){if(name.equals(e.getName()))return new String(readAll(zip),StandardCharsets.UTF_8);}}throw new IllegalArgumentException("Không tìm thấy manifest");}
    private byte[] readAll(InputStream in)throws Exception{if(in==null)throw new IllegalArgumentException("Không đọc được file");try(InputStream input=in;ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[8192];int n;while((n=input.read(b))>=0)out.write(b,0,n);return out.toByteArray();}}
    private void addBytes(ZipOutputStream zip,String name,byte[] bytes)throws Exception{zip.putNextEntry(new ZipEntry(name));zip.write(bytes);zip.closeEntry();}
    private void addFileIfExists(ZipOutputStream zip,File file,String name)throws Exception{if(!file.exists()||!file.isFile())return;zip.putNextEntry(new ZipEntry(name));try(FileInputStream in=new FileInputStream(file)){byte[] b=new byte[8192];int n;while((n=in.read(b))>=0)zip.write(b,0,n);}zip.closeEntry();}
    private void addDirectory(ZipOutputStream zip,File dir,String prefix,File excluded)throws Exception{File[] files=dir.listFiles();if(files==null)return;for(File f:files){if(excluded!=null&&f.getCanonicalPath().equals(excluded.getCanonicalPath()))continue;String name=prefix+"/"+f.getName();if(f.isDirectory())addDirectory(zip,f,name,excluded);else addFileIfExists(zip,f,name);}}
    private void copy(File src,File dst)throws Exception{File parent=dst.getParentFile();if(parent!=null)parent.mkdirs();try(FileInputStream in=new FileInputStream(src);FileOutputStream out=new FileOutputStream(dst)){byte[] b=new byte[8192];int n;while((n=in.read(b))>=0)out.write(b,0,n);}}
    private void copyIfExists(File src,File dst)throws Exception{if(src.exists())copy(src,dst);else if(dst.exists())dst.delete();}
    private void copyDirectory(File src,File dst)throws Exception{if(!dst.exists())dst.mkdirs();File[] files=src.listFiles();if(files==null)return;for(File f:files){File t=new File(dst,f.getName());if(f.isDirectory())copyDirectory(f,t);else copy(f,t);}}
    private int countFiles(File dir){if(!dir.exists())return 0;File[] files=dir.listFiles();if(files==null)return 0;int c=0;for(File f:files)c+=f.isDirectory()?countFiles(f):1;return c;}
    private void prune(File dir,int keep){File[] files=dir.listFiles();if(files==null||files.length<=keep)return;java.util.Arrays.sort(files,(a,b)->Long.compare(b.lastModified(),a.lastModified()));for(int i=keep;i<files.length;i++)files[i].delete();}
    private void deleteRecursively(File f){if(f.isDirectory()){File[] children=f.listFiles();if(children!=null)for(File c:children)deleteRecursively(c);}f.delete();}
    private void postSuccess(Callback callback,String message){ContextCompat.getMainExecutor(context).execute(()->callback.onSuccess(message));}
    private void postError(Callback callback,Exception e){ContextCompat.getMainExecutor(context).execute(()->callback.onError(message(e)));}
    private String message(Exception e){return e.getMessage()==null?e.getClass().getSimpleName():e.getMessage();}
}
