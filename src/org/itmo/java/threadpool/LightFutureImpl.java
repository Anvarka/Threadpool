package org.itmo.java.threadpool;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @param <R> value -- value of result
 *            isComplete -- flag of finishing task
 *            taskSupplier -- Supplier
 *            lock -- lock for tasksQueue
 *            conditionReadyTask -- Condition of finishing task
 *            threadPool -- need for thenApply
 *            exceptionFromGet -- for saving of exception
 */
public class LightFutureImpl<R> implements LightFuture<R> {
    volatile private R value;
    private final AtomicBoolean isComplete = new AtomicBoolean(false);
    private final Supplier<R> taskSupplier;
    private final Lock lock = new ReentrantLock();
    private final Condition conditionReadyTask = lock.newCondition();
    private final ThreadPoolImpl threadPool;
    private final Queue<LightFutureImpl<?>> thenApplyTasksQueue = new LinkedList<>();
    private LightExecutionException exceptionFromGet = null;

    public LightFutureImpl(Supplier<R> supplier, ThreadPoolImpl threadpool) {
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
        Supplier<R1> dependentTask = () -> {
            try {
                return function.apply(get());
            } catch (LightExecutionException e) {
                throw new RuntimeException(e);
            }
        };
        LightFutureImpl<R1> newTask = new LightFutureImpl<>(dependentTask, threadPool);
        if (isReady()) {
            return threadPool.submit(newTask);
        } else {
            thenApplyTasksQueue.add(newTask);
            return newTask;
        }
    }

    public void runTask() {
        lock.lock();
        try {
            value = taskSupplier.get();
        } catch (RuntimeException e) {
            exceptionFromGet = new LightExecutionException(e);
        } finally {
            System.out.println(Thread.currentThread().getName());
            isComplete.set(true);
            conditionReadyTask.signal();
            while(!thenApplyTasksQueue.isEmpty() && !threadPool.isShutdown.get()){
                threadPool.submit(thenApplyTasksQueue.remove());
            }
            lock.unlock();
        }
    }
}
