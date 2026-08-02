package com.sangapp.gooddaily.feature.metaphysics.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.sangapp.gooddaily.feature.metaphysics.data.DivinationSessionEntity;
import com.sangapp.gooddaily.feature.metaphysics.data.MetaphysicsDataSource;
import com.sangapp.gooddaily.feature.metaphysics.data.MetaphysicsRepository;

import java.util.List;

public class MetaphysicsViewModel extends AndroidViewModel {
    private final MetaphysicsDataSource repository;
    private final LiveData<List<DivinationSessionEntity>> sessions;

    public MetaphysicsViewModel(@NonNull Application application) {
        this(application, new MetaphysicsRepository(application));
    }

    MetaphysicsViewModel(@NonNull Application application, MetaphysicsDataSource repository) {
        super(application);
        this.repository = repository;
        sessions = repository.observeAll();
    }

    public LiveData<List<DivinationSessionEntity>> sessions() { return sessions; }
    public LiveData<List<DivinationSessionEntity>> search(String query) { return repository.search(query); }
    public void save(DivinationSessionEntity entity, Runnable done) { repository.save(entity, done); }
    public void delete(DivinationSessionEntity entity, Runnable done) { repository.delete(entity, done); }
}
