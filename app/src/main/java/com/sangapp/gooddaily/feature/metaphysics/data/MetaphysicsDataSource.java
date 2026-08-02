package com.sangapp.gooddaily.feature.metaphysics.data;

import androidx.lifecycle.LiveData;

import java.util.List;

public interface MetaphysicsDataSource {
    LiveData<List<DivinationSessionEntity>> observeAll();
    LiveData<List<DivinationSessionEntity>> search(String query);
    void save(DivinationSessionEntity entity, Runnable done);
    void delete(DivinationSessionEntity entity, Runnable done);
}
