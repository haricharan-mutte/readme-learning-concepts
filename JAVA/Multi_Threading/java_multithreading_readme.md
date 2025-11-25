# Java Multithreading Concepts: From Basics to Advanced

This document covers all the core and advanced concepts we discussed so far related to **Multithreading in Java**, including explanations and code examples. It can serve as a reference for interviews, learning, or GitHub documentation.

---

## 🔹 1. What is Multithreading in Java?

Multithreading is the process of executing two or more threads simultaneously. Each thread runs in parallel and shares the process memory space.

### Benefits:

- Better CPU utilization
- Faster execution for I/O-bound tasks
- Asynchronous processing

### Basic Thread Creation:

```java
public class MyThread extends Thread {
    public void run() {
        System.out.println("Thread running...");
    }

    public static void main(String[] args) {
        MyThread t = new MyThread();
        t.start();
    }
}
```

---

## 🔹 2. Thread Lifecycle

- **New**: Thread is created
- **Runnable**: Invoked using `start()`
- **Running**: Actual execution
- **Blocked/Waiting**: Waiting for a lock/resource
- **Terminated**: Execution complete

---

## 🔹 3. Synchronization & Locking

### 🔸 Static vs Non-Static Locking

- **Static synchronized** method/blocks lock on the `.class` object (shared across all instances)
- **Non-static synchronized** locks on the instance (`this`)

### 🔸 Custom Object Locking:

```java
synchronized(lockObj) {
    // critical section
}
```

Useful when you want finer control over the lock.

### 🔸 Example:

```java
public synchronized void instanceLock() {}
public static synchronized void classLock() {}
```

---

## 🔹 4. Thread Safety & Race Conditions

Thread safety ensures shared data structures are accessed without causing corruption.

### Techniques:

- Synchronization
- Volatile keyword
- Atomic classes
- Concurrent collections (like `ConcurrentHashMap`)

---

## 🔹 5. Volatile Keyword

Used to indicate that a variable's value will be modified by different threads.

```java
private volatile boolean flag = true;
```

It ensures **visibility**, but not **atomicity**.

---

## 🔹 6. Thread Communication

### wait(), notify(), notifyAll():

Used to make threads communicate with each other, avoiding busy waiting.

```java
synchronized(obj) {
    while (!condition) obj.wait();
    // do work
    obj.notify();
}
```

---

## 🔹 7. Deadlock, Starvation, and Livelock

- **Deadlock**: Threads wait forever for each other's lock.
- **Starvation**: A thread is never scheduled.
- **Livelock**: Threads keep responding to each other but can’t proceed.

Avoid using **nested locks**, and always acquire them in a **fixed order**.

---

## 🔹 8. Thread Pools (Executors)

Java provides a high-level API to manage threads efficiently.

```java
ExecutorService executor = Executors.newFixedThreadPool(2);
executor.submit(() -> System.out.println("Task executed"));
```

---

## 🔹 9. Future and CompletableFuture

### 🔸 Future (Blocking)

```java
Future<String> future = executor.submit(task);
String result = future.get(); // BLOCKING
```

### 🔸 CompletableFuture (Non-Blocking)

```java
CompletableFuture.supplyAsync(() -> "Result")
    .thenAccept(result -> System.out.println("Received: " + result));
```

Use `.thenAccept()`, `.thenApply()`, `.exceptionally()`, etc. for non-blocking chaining.

---

## 🔹 10. Blocking vs Non-Blocking

- `Future.get()` blocks until the result is ready.
- `CompletableFuture.thenAccept()` is non-blocking; it registers a callback.

Use `CompletableFuture` for modern asynchronous programming.

---

## 🔹 11. ThreadLocal

Used when each thread needs its own isolated variable copy.

```java
ThreadLocal<Integer> local = ThreadLocal.withInitial(() -> 1);
local.set(100);
```

---

## 🔹 12. Concurrency Utilities

- `CountDownLatch`
- `CyclicBarrier`
- `Semaphore`
- `ReentrantLock`

These give more control over thread coordination than `synchronized`.

---

## 13. Background Thread (Daemon Thread)

Definition: A thread that runs in the background to provide services to other threads.

Lifecycle: Ends automatically when all user threads finish.

Examples: Garbage Collector, JVM housekeeping threads.

Difference from main/normal thread:

User thread → keeps JVM alive until it completes.

Daemon thread → terminates automatically when no user threads remain.

***daemonThread.setDaemon(true);*** // mark as daemon

```java
public class DaemonVsUserThread {
    public static void main(String[] args) {
        // Normal User Thread
        Thread userThread = new Thread(() -> {
            while (true) {
                System.out.println("User thread is running...");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        userThread.start();

        // Daemon Thread
        Thread daemonThread = new Thread(() -> {
            while (true) {
                System.out.println("Daemon thread is running...");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        daemonThread.setDaemon(true); // mark as daemon
        daemonThread.start();

        System.out.println("Main thread finished work!");
    }
}

```
**What happens when you run this?**

Output flow:

Main thread finished work! (main thread ends quickly).

The user thread keeps printing forever → JVM keeps running.

If you comment out the user thread, only daemon runs → JVM exits immediately after main ends, daemon is killed.

--- 

## 📌 Summary Table

| Concept                  | Blocking | Non-blocking | Use-case                        |
| ------------------------ | -------- | ------------ | ------------------------------- |
| `Future.get()`           | ✅        | ❌            | Blocking response               |
| `CompletableFuture.then` | ❌        | ✅            | Async chaining, microservices   |
| `synchronized`           | ✅        | ❌            | Locks on instance/class         |
| `ThreadPool`             | ❌        | ✅            | Efficient thread reuse          |
| `ThreadLocal`            | ❌        | ✅            | Isolated thread-specific values |

---
# Miscellianous

difference between sleep(ms),join(),wait(),yield()

difference between execute(Runnable) & submit(callable/Runnable)

difference between deamon thread & user thread

volatile, understanding difference between visibility & atomicity

understanding context switching

understanding concurrency vs parallelism

Happens-Before relationship

--- 


> 🔔 This document is updated till **Java 17** compatible features.

For more updates or visual examples, visit the [Java Concurrency Docs](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/package-summary.html)

---

Feel free to fork, clone, and expand with your custom code snippets and visuals!

Please add Atomic Classes as well

---
# 🔹 I/O-bound tasks

**Definition**: Tasks that spend most of their time waiting for Input/Output operations (like disk, network, or user input) rather than actively using the CPU.

**Bottleneck**: The speed of I/O devices (disk, network, database, APIs, etc.), not the CPU.

**Examples**:

Reading/writing files on disk.

Downloading/uploading data from the internet.

Querying a database.

Waiting for user input.

Performance improvement: Using asynchronous programming, multi-threading, or non-blocking I/O helps because while one task waits, the CPU can work on something else.

#### 👉 In Java: Tasks using Future, CompletableFuture, Reactor, or async/await patterns are often used for I/O-bound workloads.

## 🔹 CPU-bound tasks

**Definition**: Tasks that spend most of their time using the CPU for computation and very little on I/O.

**Bottleneck**: The CPU speed/cores, not I/O devices.

**Examples**:

Image/video/audio processing.

Complex mathematical calculations (e.g., prime numbers, matrix multiplication).

Machine learning model training.

Data compression/encryption.

Performance improvement: Using multi-core parallelism (multi-threading, parallel streams, ForkJoinPool) helps because the workload can be split across CPU cores.

👉 In Java: You’d use parallel streams, ForkJoinPool, or Executors tuned for number of cores.

---
# Multi THreading Practicing Programming Questions

#### ✅ Beginner Level

Create and run a thread by extending Thread and implementing Runnable.

Explain the lifecycle of a thread.

Demonstrate how sleep(), yield(), and join() methods work.

Write a program to print even and odd numbers using two threads sequentially

Implement thread-safe counter using synchronized.


#### ✅ Intermediate Level

Implement a producer-consumer problem using wait() and notify().

Simulate a ticket booking system where multiple threads try to book tickets.

Explain the difference between notify() and notifyAll() with examples.

Implement deadlock and show how it can be resolved.

Demonstrate thread priorities and how they affect execution.


#### ✅ Advanced Level

Use ExecutorService to run multiple tasks concurrently.

Explain the difference between submit() and execute() methods in ExecutorService.

Use Callable and Future to compute values in separate threads.

Create a thread-safe cache using ConcurrentHashMap.

Implement graceful shutdown of threads when interrupted.

Simulate multiple readers and writers accessing a shared resource using ReadWriteLock.

Explain the happens-before relationship and memory consistency errors in Java.

Use Phaser, CountDownLatch, and CyclicBarrier in different scenarios.


#### ✅ Expert / Interview Challenge

Implement a thread pool from scratch without using ExecutorService.

Write a thread-safe singleton implementation using double-checked locking.

Detect and prevent starvation and deadlock in a multi-threaded application.

Implement a priority task executor that executes tasks based on priority.

Use atomic variables (AtomicInteger, AtomicReference) to solve concurrency problems.

Write a performance test to compare synchronized, ReentrantLock, and StampedLock.

Explain and implement lock-free algorithms in Java.