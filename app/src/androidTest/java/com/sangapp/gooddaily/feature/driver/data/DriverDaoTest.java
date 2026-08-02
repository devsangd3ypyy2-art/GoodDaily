package com.sangapp.gooddaily.feature.driver.data;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.sangapp.gooddaily.data.local.GoodDailyDatabase;
import com.sangapp.gooddaily.feature.driver.data.entity.MaintenanceRecordEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class DriverDaoTest {
    @Rule public InstantTaskExecutorRule instant = new InstantTaskExecutorRule();
    private GoodDailyDatabase db;
    private DriverDao dao;

    @Before public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, GoodDailyDatabase.class).allowMainThreadQueries().build();
        dao = db.driverDao();
    }

    @After public void close() { db.close(); }

    @Test public void dueMaintenance_returnsDueByDate() {
        MaintenanceRecordEntity item = new MaintenanceRecordEntity();
        item.vehicleId = 1;
        item.title = "Kiểm tra phanh";
        item.performedAt = 1;
        item.nextDueAt = 100;
        item.completed = false;
        dao.insertMaintenance(item);
        List<MaintenanceRecordEntity> due = dao.dueMaintenance(101, 0);
        assertEquals(1, due.size());
    }
}
