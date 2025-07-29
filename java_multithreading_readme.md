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

## 📌 Summary Table

| Concept                  | Blocking | Non-blocking | Use-case                        |
| ------------------------ | -------- | ------------ | ------------------------------- |
| `Future.get()`           | ✅        | ❌            | Blocking response               |
| `CompletableFuture.then` | ❌        | ✅            | Async chaining, microservices   |
| `synchronized`           | ✅        | ❌            | Locks on instance/class         |
| `ThreadPool`             | ❌        | ✅            | Efficient thread reuse          |
| `ThreadLocal`            | ❌        | ✅            | Isolated thread-specific values |

---

> 🔔 This document is updated till **Java 17** compatible features.

For more updates or visual examples, visit the [Java Concurrency Docs](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/package-summary.html)

---

Feel free to fork, clone, and expand with your custom code snippets and visuals!

Please add Atomic Classes as well

