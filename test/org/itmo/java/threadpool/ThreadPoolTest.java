package org.itmo.java.threadpool;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class ThreadPoolTest {
    public static <R> Supplier<R> createPrimitiveTask(R res, int time) {
        return () -> {
            try {
                System.out.println(Thread.currentThread().getName());
                Thread.sleep(time);
            } catch (InterruptedException e) {
                return null;
            }
            return res;
        };
    }

    public static Supplier<String> taskWithInfoThread(int time) {
        return () -> {
            try {
                System.out.println(Thread.currentThread().getName());
                Thread.sleep(time);
            } catch (InterruptedException e) {
                return null;
            }
            return Thread.currentThread().getName();
        };
    }

    @Test
    public void threadPoolPrimitiveTest() throws LightExecutionException {
        ThreadPool executor = new ThreadPoolImpl(5);
        Supplier<String> task1 = createPrimitiveTask("test1", 300);
        Supplier<String> task2 = createPrimitiveTask("test2", 10);
        Supplier<String> task3 = createPrimitiveTask("test3", 15);
        Supplier<String> task4 = createPrimitiveTask("test4", 20);

        LightFuture<?> solution1 = executor.submit(task1);
        Assertions.assertEquals("test1", solution1.get());
        LightFuture<?> solution2 = executor.submit(task2);
        Assertions.assertEquals("test2", solution2.get());
        LightFuture<?> solution3 = executor.submit(task3);
        Assertions.assertEquals("test3", solution3.get());
        LightFuture<?> solution4 = executor.submit(task4);
        Assertions.assertEquals("test4", solution4.get());

        executor.shutdown();
    }

    @Test
    public void threadPoolDifferentTypeWithThrowTest() throws LightExecutionException {
        ThreadPool executor = new ThreadPoolImpl(5);
        Supplier<Integer> task1 = () -> {
            System.out.println(Thread.currentThread().getName());
            return 300 + 400 + 100 - 9000;
        };
        Supplier<String> task2 = () -> {
            System.out.println(Thread.currentThread().getName());
            return "abc" + "ABC" + 123;
        };

        Supplier<Integer> task3 = () -> {
            System.out.println(Thread.currentThread().getName());
            int count = 0;
            for (int i = 0; i < 100; i++) {
                count += i;
            }
            System.out.println(count);
            return count;
        };

        Supplier<Integer> task4 = () -> {
            System.out.println(Thread.currentThread().getName());
            return 123 / 0;
        };

        LightFuture<?> solution1 = executor.submit(task1);
        Assertions.assertEquals(300 + 400 + 100 - 9000, (int) solution1.get());
        LightFuture<?> solution2 = executor.submit(task2);
        Assertions.assertEquals("abc" + "ABC" + 123, solution2.get());
        LightFuture<?> solution3 = executor.submit(task3);
        Assertions.assertEquals(4950, (int) solution3.get());
        LightFuture<?> solution4 = executor.submit(task4);
        Exception exception = null;
        try {
            solution4.get();
        } catch (LightExecutionException e1) {
            exception = e1;
        }
        Assertions.assertNotNull(exception);
        Assertions.assertEquals(exception.getClass(), LightExecutionException.class);
        executor.shutdown();
    }

    @Test
    public void countPoolTest() throws LightExecutionException {
        ThreadPool executor = new ThreadPoolImpl(5);
        Assertions.assertEquals(executor.getNumberOfThreads(), 5);

        List<LightFuture<String>> runTasks = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Supplier<String> task = ThreadPoolTest.taskWithInfoThread(100);
            runTasks.add(executor.submit(task));
        }
        Set<String> resSet = new HashSet<>();
        for (LightFuture<String> runTask : runTasks) {
            resSet.add(runTask.get());
        }
        Assertions.assertEquals(5, resSet.size());
        executor.shutdown();
    }

    @Test
    public void stressTest() throws LightExecutionException {
        ThreadPool executor = new ThreadPoolImpl(5);
        List<LightFuture<String>> runTasks = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            Supplier<String> task = ThreadPoolTest.createPrimitiveTask("test" + i, 10);
            runTasks.add(executor.submit(task));
        }
        for (int i = 0; i < runTasks.size(); i++) {
            Assertions.assertEquals(runTasks.get(i).get(), "test" + i);
        }
        executor.shutdown();
    }
}
