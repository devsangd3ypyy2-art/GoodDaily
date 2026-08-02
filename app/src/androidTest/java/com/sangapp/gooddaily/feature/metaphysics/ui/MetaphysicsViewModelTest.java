package com.sangapp.gooddaily.feature.metaphysics.ui;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.sangapp.gooddaily.feature.metaphysics.data.DivinationSessionEntity;
import com.sangapp.gooddaily.feature.metaphysics.data.MetaphysicsDataSource;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class MetaphysicsViewModelTest {
    @Test public void save_isDelegatedToRepository() {
        FakeSource source = new FakeSource();
        Application app = ApplicationProvider.getApplicationContext();
        MetaphysicsViewModel viewModel = new MetaphysicsViewModel(app, source);
        viewModel.save(new DivinationSessionEntity(), null);
        assertEquals(1, source.savedCount);
    }

    private static class FakeSource implements MetaphysicsDataSource {
        int savedCount;
        MutableLiveData<List<DivinationSessionEntity>> data = new MutableLiveData<>(new ArrayList<>());
        @Override public LiveData<List<DivinationSessionEntity>> observeAll() { return data; }
        @Override public LiveData<List<DivinationSessionEntity>> search(String query) { return data; }
        @Override public void save(DivinationSessionEntity entity, Runnable done) { savedCount++; if (done != null) done.run(); }
        @Override public void delete(DivinationSessionEntity entity, Runnable done) { if (done != null) done.run(); }
    }
}
