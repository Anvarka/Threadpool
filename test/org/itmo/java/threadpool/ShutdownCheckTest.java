package org.itmo.java.threadpool;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Function;
import java.util.function.Supplier;

public class ShutdownCheckTest {
    @Test
    public void threadPoolPrimitiveCheckShutdownWorkTest() throws LightExecutionException {
        ThreadPool executor = new ThreadPoolImpl(5);

        Supplier<String> task1 = ThreadPoolTest.createPrimitiveTask("test1", 300);
        Supplier<String> task2 = ThreadPoolTest.createPrimitiveTask("test2", 10);

        LightFuture<?> solution1 = executor.submit(task1);
        solution1.get();

        executor.shutdown();

        Exception exception = null;
        try {
            LightFuture<?> solution2 = executor.submit(task2);
            System.out.println(solution2.get());
        } catch (RuntimeException e) {
            exception = e;
        }
        Assertions.assertNotNull(exception);

        String expectedMessage = "threadPool is shutdown";
        String actualMessage = exception.getMessage();
        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void threadPoolCountWorkAfterShutdownWorkTest() throws LightExecutionException, InterruptedException {
        ThreadPool executor = new ThreadPoolImpl(5);

        Supplier<String> fastTask1 = ThreadPoolTest.createPrimitiveTask("test1", 3);
        Supplier<String> fastTask2 = ThreadPoolTest.createPrimitiveTask("test2", 30);
        Supplier<String> lazyTask = ThreadPoolTest.createPrimitiveTask("test3", 400);
        LightFuture<String> solution1 = executor.submit(fastTask1);
        LightFuture<String> solution2 = executor.submit(fastTask2);
        LightFuture<String> solution3 = executor.submit(lazyTask);
        Thread.sleep(100);
        executor.shutdown();
        String res1 = solution1.get();
        String res2 = solution2.get();
        String res3 = solution3.get();
        Assertions.assertEquals("test1", res1);
        Assertions.assertEquals("test2", res2);
        Assertions.assertNull(res3);
    }

    @Test
    public void threadPoolWeightToPoolByBigTaskCheckTest() throws LightExecutionException, InterruptedException {
        Function<String, String> function = s -> {
            System.out.println(Thread.currentThread().getName());
            return s + " add str smth";
        };

        ThreadPool executor = ThreadPool.create(3);
        Supplier<String> bigTask = ThreadPoolTest.createPrimitiveTask("test1", 200);
        Supplier<String> fastTask1 = ThreadPoolTest.createPrimitiveTask("test2", 20);
        Supplier<String> fastTask2 = ThreadPoolTest.createPrimitiveTask("test3", 30);
        Supplier<String> fastTask3 = ThreadPoolTest.createPrimitiveTask("test4", 10);

        LightFuture<String> bigSolution = executor.submit(bigTask);
        LightFuture<String> fastSolution1 = executor.submit(fastTask1);
        LightFuture<String> fastSolution2 = executor.submit(fastTask2);
        LightFuture<String> fastSolution3 = executor.submit(fastTask3);

        LightFuture<String> generateTask = bigSolution.thenApply(function);
        LightFuture<String> generateTask2 = generateTask.thenApply(function);
        LightFutureImpl<String> generateTask3 = (LightFutureImpl<String>) generateTask2.thenApply(function);
        LightFuture<String> generateTask4 = generateTask3.thenApply(function);
        LightFuture<String> generateTask5 = generateTask4.thenApply(function);
        LightFuture<String> generateTask6 = generateTask5.thenApply(function);

        Thread.sleep(100);
        executor.shutdown();
        String bigRes = bigSolution.get();
        String fastRes1 = fastSolution1.get();
        String fastRes2 = fastSolution2.get();
        String fastRes3 = fastSolution3.get();
        System.out.println("dog");

        Assertions.assertEquals("test2", fastRes1);
        Assertions.assertEquals("test3", fastRes2);
        Assertions.assertEquals("test4", fastRes3);
        Assertions.assertNull(bigRes);
    }
}
