package JAVA.Multi_Threading.java;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class ThreadLocalExample {
    // Create ThreadLocal variable
    static ThreadLocal<Integer> threadLocalCounter = ThreadLocal.withInitial(() -> 0);

    public static void main(String[] args) throws InterruptedException {
//        Runnable task = () -> {
//            // Increment thread-local counter
//            for (int i = 0; i < 5; i++) {
//                threadLocalCounter.set(threadLocalCounter.get() + 1);
//                System.out.println(Thread.currentThread().getName() + " => " + threadLocalCounter.get());
//            }
//        };
//
//        Thread t1 = new Thread(task, "Thread-A");
//        Thread t2 = new Thread(task, "Thread-B");
//
//        t1.start();
//        t2.start();'
    }
}

class ThreadCommunicationExamples{
    public static void main(String[] args) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(5); // Match the number of threads

        for (int i = 0; i < 5; i++) {
            final int threadId = i; // Capture for lambda
            Thread thread = new Thread(() -> {
                try {
                    System.out.println("Thread " + threadId + " starting work");
                    // Simulate some actual work
                    Thread.sleep(1000 + threadId * 500);
                    System.out.println("Thread " + threadId + " completed work");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown(); // ✅ Called after thread's work is done
                }
            }, "T-" + i);
            thread.start();
        }

        latch.await(); // Wait for all 5 threads to complete
        System.out.println("All threads completed. Main thread continuing.");

    }

}

class CyclicBarrierExample{
    public static void main(String[] args) {
        // Barrier action runs when all parties reach the barrier
        int parties =3;
        Runnable barrierAction = () -> System.out.println("All parties reached barrier. Proceeding...");
        CyclicBarrier barrier = new CyclicBarrier(parties, barrierAction);
        ExecutorService executor = Executors.newFixedThreadPool(parties);

        for (int i = 1; i <= parties; i++) {
            final int workerId = i;
            executor.submit(() -> {
                try {
                    System.out.printf("Worker %d: Phase 1 work%n", workerId);
                    Thread.sleep(workerId * 500L);

                    System.out.printf("Worker %d: Waiting at barrier (phase 1)%n", workerId);
                    barrier.await();  // Wait for all threads

                    System.out.printf("Worker %d: Phase 2 work%n", workerId);
                    Thread.sleep(300L);

                    System.out.printf("Worker %d: Waiting at barrier (phase 2)%n", workerId);
                    barrier.await();  // Reuse barrier for next phase

                    System.out.printf("Worker %d: Final phase work%n", workerId);
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        executor.shutdown();
    }
}

class Atomicity{
//    static AtomicInteger integer = new AtomicInteger(1);
    volatile static int integer = 1;
    public static void main(String[] args) throws InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(3);
        CountDownLatch latch = new CountDownLatch(1000);

        for(int i=0;i<1000;i++){
            service.submit(()->{
//                integer.getAndIncrement();
                integer+=1;
                latch.countDown();
            });
        }
        service.shutdown();
        latch.await();
        System.out.println(integer);
    }
}

class Example{
    public static void main(String[] args) {
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
        scheduledThreadPoolExecutor.scheduleWithFixedDelay(()-> System.out.println("testing"),1000,3000,TimeUnit.MILLISECONDS);


    }

}

//Java COncurrent Features

class ConcurrentFeatures{
    ReentrantLock lock = new ReentrantLock(true);
    synchronized void testOuter() throws InterruptedException {
        System.out.println("testOuter");
        testInner();
        System.out.println("finishedOuter");
    }


    synchronized void testInner() throws InterruptedException {
        System.out.println("testInner");
        Thread.sleep(1000);

    }

    public static void main(String[] args) {
        ConcurrentFeatures features = new ConcurrentFeatures();
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.submit(()-> {
            try {
                features.testOuter();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }
}

class TimedTryLockExample {
    private final ReentrantLock lock = new ReentrantLock();

    public void attemptTimedWork(int workerId) {
        try {
            System.out.println("Worker " + workerId + " attempting to acquire lock (2 sec timeout)");

            if (lock.tryLock(2, TimeUnit.SECONDS)) {
                try {
                    System.out.println("Worker " + workerId + " acquired lock");
                    Thread.sleep(1500); // Simulate work
                    System.out.println("Worker " + workerId + " completed work");
                } finally {
                    lock.unlock();
                }
            } else {
                System.out.println("Worker " + workerId + " timed out waiting for lock");
            }
        } catch (InterruptedException e) {
            System.out.println("Worker " + workerId + " was interrupted");
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        TimedTryLockExample example = new TimedTryLockExample();

        for (int i = 1; i <= 3; i++) {
            final int id = i;
            new Thread(() -> example.attemptTimedWork(id)).start();
        }
    }
}

 class ConditionExample {
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();
    private final Condition notFull = lock.newCondition();

    private final int[] buffer = new int[5];
    private int count = 0, putIndex = 0, takeIndex = 0;

    public void put(int value) throws InterruptedException {
        lock.lock();
        try {
            while (count == buffer.length) {
                System.out.println("Buffer full, producer waiting...");
                notFull.await(); // Wait until buffer not full
            }

            buffer[putIndex] = value;
            putIndex = (putIndex + 1) % buffer.length;
            count++;

            System.out.println("Produced: " + value + ", buffer size: " + count);
            notEmpty.signalAll(); // Signal consumers
        } finally {
            lock.unlock();
        }
    }

    public int take() throws InterruptedException {
        lock.lock();
        try {
            while (count == 0) {
                System.out.println("Buffer empty, consumer waiting...");
                notEmpty.await(); // Wait until buffer not empty
            }

            int value = buffer[takeIndex];
            takeIndex = (takeIndex + 1) % buffer.length;
            count--;

            System.out.println("Consumed: " + value + ", buffer size: " + count);
            notFull.signalAll(); // Signal producers
            return value;
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) {
        ConditionExample example = new ConditionExample();

        // Producer thread
        new Thread(() -> {
            try {
                for (int i = 1; i <= 10; i++) {
                    example.put(i);
                    Thread.sleep(200);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Producer").start();

        // Consumer thread
        new Thread(() -> {
            try {
                for (int i = 1; i <= 10; i++) {
                    example.take();
                    Thread.sleep(300);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Consumer").start();
    }
}

class ReadWriteExample {
    int value =1;
    ReadWriteLock lock = new ReentrantReadWriteLock(true);

    void read() throws InterruptedException {
        lock.readLock().lock();
        try {
            System.out.println("reading value: "+ Thread.currentThread().getName()+"-"+value);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println("Error");
            Thread.currentThread().interrupt();
        }finally{
            lock.readLock().unlock();
            System.out.println("read finishes by "+ Thread.currentThread().getName());
        }

    }

    void write() throws InterruptedException {
        lock.writeLock().lock();
        try {
            System.out.println("write value starts: curr value: "+value);
            value +=1;
            System.out.println("new value: "+Thread.currentThread().getName()+"-"+value);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println("Error");
            Thread.currentThread().interrupt();
        }finally{
            System.out.println("write finished -"+Thread.currentThread().getName());
            lock.writeLock().unlock();

        }

    }

    public static void main(String[] args) {
        ReadWriteExample example = new ReadWriteExample();

        for (int i=1;i<=2;i++){
            Thread t1 = new Thread(()->{
                for(int j=1;j<=2;j++){
                    try {
                        example.read();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            },"TR-"+i);
            t1.start();
        }

        for (int i=1;i<=2;i++){
            Thread t1 = new Thread(()->{
                for(int j=1;j<=2;j++){
                    try {
                        example.write();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            },"TW-"+i);
            t1.start();
        }
    }

}


class ReadWriteCache<K,V> {
    private final Map<K,V> cache = new HashMap<>();
    private final ReadWriteLock rwLock;

    public ReadWriteCache(boolean fair) {
        // Fair = true prevents writer starvation at slight performance cost
        this.rwLock = new ReentrantReadWriteLock(fair);
    }

    // Retrieve value; allows concurrent reads
    public V get(K key) {
        rwLock.readLock().lock();
        try {
            return cache.get(key);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    // Insert or update entry; exclusive write access
    public void put(K key, V value) {
        rwLock.writeLock().lock();
        try {
            cache.put(key, value);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    // Remove entry; exclusive write access
    public V remove(K key) {
        rwLock.writeLock().lock();
        try {
            return cache.remove(key);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    // Example workload
    public static void main(String[] args) throws InterruptedException {
        // Toggle fair vs non-fair mode
        ReadWriteCache<String, String> cache = new ReadWriteCache<>(true);

        // Preload cache
        for (int i = 0; i < 100; i++) {
            cache.put("key" + i, "value" + i);
        }

        Runnable reader = () -> {
            for (int i = 0; i < 1000; i++) {
                String key = "key" + (i % 100);
                cache.get(key);
                // Simulate processing
                try { Thread.sleep(1); } catch (InterruptedException ignored) {}
            }
        };

        Runnable writer = () -> {
            for (int i = 0; i < 100; i++) {
                String key = "key" + (i % 100);
                cache.put(key, "value" + i + "-updated");
                // Simulate update interval
                try { Thread.sleep(10); } catch (InterruptedException ignored) {}
            }
        };

        // Launch readers and writers
        Thread[] readers = new Thread[5];
        for (int i = 0; i < readers.length; i++) {
            readers[i] = new Thread(reader, "Reader-" + i);
            readers[i].start();
        }

        Thread[] writers = new Thread[2];
        for (int i = 0; i < writers.length; i++) {
            writers[i] = new Thread(writer, "Writer-" + i);
            writers[i].start();
        }

        long start = System.currentTimeMillis();

        // Wait for completion
        for (Thread t : readers) t.join();
        for (Thread t : writers) t.join();

        long duration = System.currentTimeMillis() - start;
        System.out.println("Test completed in " + duration + " ms");
    }
}
