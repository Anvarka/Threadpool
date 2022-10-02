package org.itmo.java.threadpool;

import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

public interface ThreadPool {
    static @NotNull ThreadPool create(int threads) {
        // TODO: implement
        throw new UnsupportedOperationException();
    }

    <R> @NotNull LightFuture<R> submit(Supplier<R> supplier);

    void shutdown();

    int getNumberOfThreads();
}
