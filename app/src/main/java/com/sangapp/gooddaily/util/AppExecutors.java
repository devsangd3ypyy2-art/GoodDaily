package com.sangapp.gooddaily.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AppExecutors {
    private static final ExecutorService DATABASE = Executors.newSingleThreadExecutor();
    private static final ExecutorService IO = Executors.newFixedThreadPool(2);

    private AppExecutors() {}

    public static ExecutorService database() { return DATABASE; }
    public static ExecutorService io() { return IO; }
}
