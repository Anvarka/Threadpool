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
    private final int countOfThreads;
    public final AtomicBoolean isShutdown = new AtomicBoolean(false);
    private final Queue<LightFutureImpl<?>> tasksQueue = new LinkedList<>();
    private final Lock lock = new ReentrantLock();
    private final Condition hasTasksInQueue = lock.newCondition();
    private final List<Thread> listOfThreads = new ArrayList<>();

    public ThreadPoolImpl(int threads) {
        for (int i = 0; i < threads; i++) {
            Thread thread = new Thread(createTask());
            thread.start();
            listOfThreads.add(thread);
        }
        countOfThreads = threads;
    }

    @Override
    public @NotNull <R> LightFuture<R> submit(Supplier<R> supplier) {
        if (!isShutdown.get()) {
            lock.lock();
            try {
                LightFutureImpl<R> newTask = new LightFutureImpl<>(supplier, this);
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

    public @NotNull <R> LightFuture<R> submit(LightFutureImpl<R> newTask) {
        if (!isShutdown.get()) {
            lock.lock();
            try {
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
        tasksQueue.clear();
        for (Thread thread : listOfThreads) {
            thread.interrupt();
        }
    }

    @Override
    public int getNumberOfThreads() {
        return countOfThreads;
    }

    private Runnable createTask() {
        return () -> {
            while (!isShutdown.get() && !Thread.interrupted()) {
                LightFutureImpl<?> task;
                lock.lock();
                try {
                    while (tasksQueue.isEmpty()) {
                        hasTasksInQueue.await();
                    }
                    task = tasksQueue.remove();
                } catch (InterruptedException ignored) {
                    return;
                } finally {
                    lock.unlock();
                }
                if (!isShutdown.get() && !Thread.interrupted()) {
                    task.runTask();
                }
            }
        };
    }

}
