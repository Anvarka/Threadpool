package org.itmo.java.threadpool;

import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

public interface ThreadPool {

    <R> @NotNull LightFuture<R> submit(Supplier<R> supplier);

    void shutdown();

    int getNumberOfThreads();
}
