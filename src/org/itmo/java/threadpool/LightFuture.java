package org.itmo.java.threadpool;

import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

public interface LightFuture<R> {
    boolean isReady();

    @NotNull R get() throws LightExecutionException;

    <R1> @NotNull LightFuture<R1> thenApply(Function<R, R1> function);

    void runTask();
}
