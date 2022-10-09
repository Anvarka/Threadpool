package org.itmo.java.threadpool;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * ThreadPool implementation:
 * countOfThreads -- count of threads
 * isShutdown -- flag of threadPool's shutdown
 * tasksQueue -- task queue
 * lock -- lock for tasksQueue
 * hasTasksInQueue -- Condition of having task in queue
 */
public class ThreadPoolImpl implements ThreadPool {
    int countOfThreads;
    AtomicBoolean isShutdown = new AtomicBoolean(false);
    public Queue<LightFuture<?>> tasksQueue = new LinkedList<>();
    Lock lock = new ReentrantLock();
    Condition hasTasksInQueue = lock.newCondition();

    public ThreadPoolImpl(int threads) {
        for (int i = 0; i < threads; i++) {
            Thread thread = new Thread(() -> {
                while (!isShutdown.get() && !Thread.interrupted()) {
                    LightFuture<?> task = null;
                    lock.lock();
                    try {
                        while (tasksQueue.isEmpty()) {
                            hasTasksInQueue.await();
                        }
                        task = tasksQueue.remove();
                    } catch (InterruptedException ignored) {
                    } finally {
                        lock.unlock();
                    }
                    assert task != null;
                    task.runTask();
                }
            });
            thread.start();
        }
        countOfThreads = threads;
    }

    @Override
    public @NotNull <R> LightFuture<R> submit(Supplier<R> supplier) {
        if (!isShutdown.get()) {
            lock.lock();
            try {
                LightFuture<R> newTask = new LightFutureImpl<>(supplier, this);
                tasksQueue.add(newTask);
                hasTasksInQueue.signal();
                return newTask;
            } finally {
                lock.unlock();
            }
        } else {
            throw new RuntimeException("threadPool is shutdown");
        }

    }

    @Override
    public void shutdown() {
        isShutdown.set(true);
    }

    @Override
    public int getNumberOfThreads() {
        return countOfThreads;
    }
}
