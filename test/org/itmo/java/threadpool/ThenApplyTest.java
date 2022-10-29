package org.itmo.java.threadpool;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class ThenApplyTest {
    @Test
    public void primitiveThenApplyTest() throws LightExecutionException {
        ThreadPool executor = new ThreadPoolImpl(2);
        Supplier<String> task = ThreadPoolTest.createPrimitiveTask("test1", 10);
        Supplier<String> task2 = ThreadPoolTest.createPrimitiveTask("test2", 10);

        LightFuture<String> runTask = executor.submit(task);
        LightFuture<String> runTask2 = executor.submit(task2);
        Assertions.assertEquals("test1", runTask.get());
        Assertions.assertEquals("test2", runTask2.get());

        Function<String, String> function = s -> {
            System.out.println(Thread.currentThread().getName());
            return s + " add str smth";
        };

        LightFuture<String> generateTask = runTask.thenApply(function);
        LightFuture<String> generateTask2 = runTask2.thenApply(function);
        Assertions.assertEquals("test1 add str smth", generateTask.get());
        Assertions.assertEquals("test2 add str smth", generateTask2.get());
    }

    @Test
    public void thenApplyTest() throws LightExecutionException {

        Supplier<Integer> task1 = () -> 1000 + 10000;
        Supplier<Integer> task2 = () -> 100000 + 100;

        ThreadPool executor = new ThreadPoolImpl(2);

        LightFuture<Integer> runTask = executor.submit(task1);
        LightFuture<Integer> runTask2 = executor.submit(task2);
        Assertions.assertEquals(11000, runTask.get());
        Assertions.assertEquals(100100, runTask2.get());

        Function<Integer, Integer> function = num -> {
            System.out.println(Thread.currentThread().getName());
            return num + 50;
        };

        LightFuture<Integer> generateTask = runTask.thenApply(function);
        LightFuture<Integer> generateTask2 = runTask2.thenApply(function);
        Assertions.assertEquals(11050, generateTask.get());
        Assertions.assertEquals(100150, generateTask2.get());
    }


    @Test
    public void repeatThenApplyTest() throws LightExecutionException {
        ThreadPool executor = new ThreadPoolImpl(2);

        Supplier<Integer> task = () -> 100 + 100;
        LightFuture<Integer> runTask = executor.submit(task);

        Assertions.assertEquals(200, runTask.get());

        Function<Integer, Integer> function = num -> {
            System.out.println(Thread.currentThread().getName());
            return num + 50;
        };

        LightFuture<Integer> generateTask1 = runTask.thenApply(function);
        LightFuture<Integer> generateTask2 = generateTask1.thenApply(function);
        Assertions.assertEquals(250, generateTask1.get());
        Assertions.assertEquals(300, generateTask2.get());

        Function<Integer, String> function2 = num -> {
            System.out.println(Thread.currentThread().getName());
            return num + " abc";
        };

        LightFuture<String> generateTask3 = generateTask2.thenApply(function2);
        Assertions.assertEquals(generateTask3.get(), "300 abc");

        Function<String, String> function3 = num -> {
            System.out.println(Thread.currentThread().getName());
            return num + " info";
        };

        LightFuture<String> generateTask4 = generateTask3.thenApply(function3);
        Assertions.assertEquals(generateTask4.get(), "300 abc info");

    }

    @Test
    public void stressThenApplyTest() throws LightExecutionException {
        ThreadPool executor = new ThreadPoolImpl(5);
        List<LightFuture<String>> runTasks = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            Supplier<String> task = ThreadPoolTest.createPrimitiveTask("test" + i, 10);
            runTasks.add(executor.submit(task));
        }

        Function<String, String> fun = s -> s + " add info";

        for (int i = 0; i < 1000; i++) {
            LightFuture<String> gentask = runTasks.get(i).thenApply(fun);
            runTasks.add(gentask);
        }

        for (int i = 0; i < runTasks.size(); i++) {
            if (i < 1000) {
                Assertions.assertEquals(runTasks.get(i).get(), "test" + i);
            } else {
                System.out.println(runTasks.get(i).get());
                Assertions.assertEquals(runTasks.get(i).get(), "test" + (i - 1000) + " add info");
            }
        }
    }

    @Test
    public void thenApplyTimeTest() throws LightExecutionException {
        ThreadPool executor = new ThreadPoolImpl(5);
        List<LightFuture<String>> depTasks = new ArrayList<>();
        List<LightFuture<String>> deepDepTasks = new ArrayList<>();

        Supplier<String> bigTask = ThreadPoolTest.createPrimitiveTask("test", 10000);
        LightFuture<String> bigTaskFuture = executor.submit(bigTask);

        Function<String, String> fun = s -> s + " add info";

        for (int i = 0; i < 10; i++) {
            LightFuture<String> dependTask = bigTaskFuture.thenApply(fun);
            LightFuture<String> deepDependTask = dependTask.thenApply(fun);
            depTasks.add(dependTask);
            deepDepTasks.add(deepDependTask);
        }

        String str = depTasks.get(9).get();
        String deepRes = deepDepTasks.get(9).get();
        Assertions.assertEquals(str, "test add info");
        Assertions.assertEquals(deepRes, "test add info add info");
    }
}

