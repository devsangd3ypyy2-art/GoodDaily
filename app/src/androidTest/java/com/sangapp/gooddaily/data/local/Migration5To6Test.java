package com.sangapp.gooddaily.data.local;

import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Focused migration test that verifies the new v6 tables and preservation of
 * generic divination / driver records from v5. It intentionally does not rely
 * on generated schema JSON, so it can run on a clean clone of the project.
 */
@RunWith(AndroidJUnit4.class)
public class Migration5To6Test {
    private SupportSQLiteOpenHelper helper;
    private SupportSQLiteDatabase database;

    @Before public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        String name = "migration_5_6_test.db";
        context.deleteDatabase(name);
        SupportSQLiteOpenHelper.Configuration configuration =
                SupportSQLiteOpenHelper.Configuration.builder(context)
                        .name(name)
                        .callback(new SupportSQLiteOpenHelper.Callback(5) {
                            @Override public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                db.execSQL("CREATE TABLE personal_records (" +
                                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                                        "module TEXT, feature TEXT, title TEXT, details TEXT, " +
                                        "numericValue REAL NOT NULL DEFAULT 0, secondaryValue REAL NOT NULL DEFAULT 0, " +
                                        "countValue INTEGER NOT NULL DEFAULT 0, dateKey TEXT, " +
                                        "startMinutes INTEGER NOT NULL DEFAULT 0, endMinutes INTEGER NOT NULL DEFAULT 0, " +
                                        "status TEXT, tags TEXT, attachmentUri TEXT, favorite INTEGER NOT NULL DEFAULT 0, " +
                                        "archived INTEGER NOT NULL DEFAULT 0, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL)");
                            }

                            @Override public void onUpgrade(@NonNull SupportSQLiteDatabase db, int oldVersion, int newVersion) {
                                // This test invokes the production Migration object directly.
                            }
                        })
                        .build();
        helper = new FrameworkSQLiteOpenHelperFactory().create(configuration);
        database = helper.getWritableDatabase();
        database.execSQL("INSERT INTO personal_records(module,feature,title,details,numericValue,secondaryValue,countValue,dateKey,startMinutes,endMinutes,status,tags,attachmentUri,favorite,archived,createdAt,updatedAt) " +
                "VALUES('LEGACY','DIVINATION_ENTRY','Việc làm','Nội dung cũ',0,0,0,'2026-08-02',0,0,'CHO_KIEM_CHUNG','tag','',1,0,1000,1000)");
        database.execSQL("INSERT INTO personal_records(module,feature,title,details,numericValue,secondaryValue,countValue,dateKey,startMinutes,endMinutes,status,tags,attachmentUri,favorite,archived,createdAt,updatedAt) " +
                "VALUES('LEGACY','DRIVER_SHIFT','Ca sáng','Ghi chú',500000,100000,12,'2026-08-02',480,720,'CLOSED','', '',0,0,2000,4000)");
    }

    @After public void tearDown() {
        if (helper != null) helper.close();
    }

    @Test public void migration_createsFeatureTablesAndPreservesLegacyRows() {
        GoodDailyDatabase.MIGRATION_5_6.migrate(database);
        assertEquals(1, count("divination_sessions"));
        assertEquals(1, count("driver_shifts"));
        assertEquals(0, count("vehicles"));
    }

    private int count(String table) {
        try (Cursor cursor = database.query("SELECT COUNT(*) FROM " + table)) {
            cursor.moveToFirst();
            return cursor.getInt(0);
        }
    }
}
