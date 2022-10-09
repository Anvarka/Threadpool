package org.itmo.java.threadpool;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 *
 * @param <R>
 *    value -- value of result
 *    isComplete -- flag of finishing task
 *    taskSupplier -- Supplier
 *    lock -- lock for tasksQueue
 *    conditionReadyTask -- Condition of finishing task
 *    threadPool -- need for thenApply
 *    exceptionFromGet -- for saving of exception
 */
public class LightFutureImpl<R> implements LightFuture<R> {
    volatile private R value;
    AtomicBoolean isComplete = new AtomicBoolean(false);
    public Supplier<R> taskSupplier;

    Lock lock = new ReentrantLock();
    Condition conditionReadyTask = lock.newCondition();
    ThreadPool threadPool;
    LightExecutionException exceptionFromGet = null;

    public LightFutureImpl(Supplier<R> supplier, ThreadPool threadpool) {
        taskSupplier = supplier;
        threadPool = threadpool;
    }

    @Override
    public boolean isReady() {
        return isComplete.get();
    }

    @Override
    public @NotNull R get() throws LightExecutionException {
        lock.lock();
        try {
            while (!isReady()) {
                conditionReadyTask.await();
            }
            if (exceptionFromGet != null) throw exceptionFromGet;
        } catch (InterruptedException ignored) {
        } finally {
            lock.unlock();
        }
        return value;
    }

    @Override
    public @NotNull <R1> LightFuture<R1> thenApply(Function<R, R1> function) {
        return threadPool.submit(() -> {
            try {
                return function.apply(get());
            } catch (LightExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void runTask() {
        lock.lock();
        try {
            value = taskSupplier.get();
        } catch (RuntimeException e) {
            exceptionFromGet = new LightExecutionException(e);
        } finally {
            conditionReadyTask.signal();
            isComplete.set(true);
            lock.unlock();
        }
    }
}