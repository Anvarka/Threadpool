package org.itmo.java.threadpool;

import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

public interface ThreadPool {

    static @NotNull ThreadPool create(int threads) {
        return new ThreadPoolImpl(threads);
    }

    <R> @NotNull LightFuture<R> submit(Supplier<R> supplier);

    void shutdown();

    int getNumberOfThreads();
}
