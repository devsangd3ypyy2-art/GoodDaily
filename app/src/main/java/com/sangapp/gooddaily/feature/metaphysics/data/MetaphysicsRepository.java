package com.sangapp.gooddaily.feature.metaphysics.data;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.sangapp.gooddaily.data.local.GoodDailyDatabase;
import com.sangapp.gooddaily.util.AppExecutors;

import java.util.List;

public class MetaphysicsRepository implements MetaphysicsDataSource {
    private final DivinationDao dao;

    public MetaphysicsRepository(Context context) {
        dao = GoodDailyDatabase.get(context).divinationDao();
    }

    public LiveData<List<DivinationSessionEntity>> observeAll() { return dao.observeAll(); }
    public LiveData<List<DivinationSessionEntity>> search(String query) { return dao.search(query == null ? "" : query.trim()); }

    public void save(DivinationSessionEntity entity, Runnable done) {
        AppExecutors.io().execute(() -> {
            if (entity.id == 0) entity.id = dao.insert(entity); else dao.update(entity);
            if (done != null) AppExecutors.main().post(done);
        });
    }

    public void delete(DivinationSessionEntity entity, Runnable done) {
        AppExecutors.io().execute(() -> {
            dao.delete(entity);
            if (done != null) AppExecutors.main().post(done);
        });
    }
}
