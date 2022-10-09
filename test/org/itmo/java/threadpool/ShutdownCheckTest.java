package org.itmo.java.threadpool;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        }catch (RuntimeException e){
            exception = e;
        }
        Assertions.assertNotNull(exception);

        String expectedMessage = "threadPool is shutdown";
        String actualMessage = exception.getMessage();
        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }
}
