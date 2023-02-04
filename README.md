# Pool

* In the task, you need to implement a simple task pool with a fixed number of threads (the number is set in the constructor)
* When creating a `ThreadPoolImpl` object, `n` threads should start working in it
* Each thread has two states: waiting for a task / executing a task
* The task is to calculate some value, call `get` on an object of type `Supplier<R>`
* When adding a task, if there is a waiting thread in the pool, then it should start executing it. Otherwise, the task will wait for execution until some thread is freed.
* Tasks accepted for execution are represented as objects of the `LightFuture` interface
* The `shutdown` method must shut down the threads. To interrupt a thread, it is recommended to use the `Thread.interrupt()` method

---

#lightfuture

* The `isReady` method returns `true` if the task is completed
* The `get` method returns the result of the task
     * In case the `supplier` corresponding to the task exited with an exception, this method should exit with a `LightExecutionException`
     * If the result has not yet been calculated, the method waits for it and returns the resulting value

* Method `thenApply` - accepts an object of type `Function` that can be applied to the result of this task `X` and returns a new task `Y` accepted for execution
     * The new task will be executed no earlier than the original task is completed
     * As an argument to the `Function` object, the result of the original task will be passed, and all `Y` must be executed on a common basis (i.e., must be shared between pool threads)
     * The `thenApply` method can be called multiple times

---

# Notes

* In this work, it is forbidden to use the contents of the `java.util.concurrent` package, except for the `ReentrantLock`, `Condition` and `Atomic*` classes from this package
* All interface methods must be thread safe
* A simple test should be written for each basic use case
     * Requires a stress test with a large number of tasks (> 1000)
     * A test must also be written to check that there are indeed at least `n` threads in the thread pool
