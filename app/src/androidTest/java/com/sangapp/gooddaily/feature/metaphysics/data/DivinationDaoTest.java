package com.sangapp.gooddaily.feature.metaphysics.data;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.sangapp.gooddaily.data.local.GoodDailyDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class DivinationDaoTest {
    @Rule public InstantTaskExecutorRule instant = new InstantTaskExecutorRule();
    private GoodDailyDatabase db;
    private DivinationDao dao;

    @Before public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, GoodDailyDatabase.class).allowMainThreadQueries().build();
        dao = db.divinationDao();
    }

    @After public void close() { db.close(); }

    @Test public void insertAndCount() {
        DivinationSessionEntity item = new DivinationSessionEntity();
        item.system = "MAI_HOA";
        item.question = "Công việc";
        item.castTime = 1;
        item.status = "CHO_KIEM_CHUNG";
        item.createdAt = 1;
        item.updatedAt = 1;
        dao.insert(item);
        assertEquals(1, dao.count());
    }
}
