package JAVA.Multi_Threading.java;

import java.sql.SQLOutput;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadSimulation {

}

class PrintOddEvenWithTwoThreads {
    int counter = 0;
    int maxLimit = 10;
    boolean isOddTurn = false;
    ReentrantLock lock = new ReentrantLock();
    Condition evenCondition = lock.newCondition();
    Condition oddCondition = lock.newCondition();

    public void printEven() throws InterruptedException {
        lock.lock();
        while(isOddTurn){
            evenCondition.await();
        }
        if(counter<=maxLimit) {
            System.out.println(Thread.currentThread().getName() + "-" + (counter++));
            isOddTurn = true;
            oddCondition.signal();
        }
        lock.unlock();
        Thread.sleep(1000);
    }

    public void printOdd() throws InterruptedException {
        lock.lock();
        while(!isOddTurn){
            oddCondition.await();
        }
        if(counter<=maxLimit) {
            System.out.println(Thread.currentThread().getName() + "-" + (counter++));
            isOddTurn = false;
            evenCondition.signal();
        }
        lock.unlock();
        Thread.sleep(1000);
    }


    public static void main(String[] args) {
        PrintOddEvenWithTwoThreads sim = new PrintOddEvenWithTwoThreads();
        Thread t1 = new Thread(()->{
            for(int i=0;i<=10;i++){
                try {
                    sim.printEven();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        },"T1");
        Thread t2 = new Thread(()->{
            for(int i=0;i<=10;i++){
                try {
                    sim.printOdd();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        },"T2");

        t1.start();t2.start();
    }
}


class ProducerConsumer{
    boolean isAvailable = false;
    int data = 0;
    synchronized void produce(int data) throws InterruptedException {
        while(isAvailable){
            wait();
        }
        System.out.println("producing data:"+data);
        this.data = data;
        isAvailable = true;
        notify();
        Thread.sleep(1000);

    }

    synchronized void consume() throws InterruptedException {
        while(!isAvailable){
            wait();
        }
        System.out.println("consuming data:"+data);
        isAvailable = false;
        notify();
        Thread.sleep(1000);
    }

    public static void main(String[] args) {
        ProducerConsumer pc = new ProducerConsumer();
        Thread t1 = new Thread(()->{
            for(int i=1;i<=5;i++){
                try {
                    pc.produce(i);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        },"T1");
        Thread t2 = new Thread(()->{
            for(int i=1;i<=5;i++){
                try {
                    pc.consume();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        },"T2");

        t1.start();t2.start();
    }
}

class ThreadPoolExample{
    public static void main(String[] args) {
        ExecutorService executor = new ThreadPoolExecutor(1,5,/*Executors.newFixedThreadPool(4);*/
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());

        for(int i=1;i<100;i++){
            executor.submit(()-> {System.out.println(Thread.currentThread().getName()+"--");});
            executor.submit(()-> {System.out.println(Thread.currentThread().getName()+"--");});
            executor.submit(()-> {System.out.println(Thread.currentThread().getName()+"--");});
            executor.submit(()-> {System.out.println(Thread.currentThread().getName()+"--");});
        }

        executor.shutdown();

    }
}

/*
4. Implement a priority task executor
➤ Explanation
Tasks have priorities, and higher-priority tasks should run first. We can use a PriorityQueue to store tasks based on priority.
*/

class PriorityTask implements Comparable<PriorityTask> {
    private static final AtomicInteger count = new AtomicInteger(0);
    private final int priority;
    private final int taskId;

    public PriorityTask(int priority) {
        this.priority = priority;
        this.taskId = count.incrementAndGet();
    }

    public void run() {
        System.out.println("Executing task " + taskId + " with priority " + priority);
    }

    @Override
    public int compareTo(PriorityTask other) {
        return Integer.compare(other.priority, this.priority); // higher priority first
    }
}

class PriorityTaskExecutor {
    private final PriorityQueue<PriorityTask> queue = new PriorityQueue<>();

    public void submit(PriorityTask task) {
        synchronized (queue) {
            queue.add(task);
            queue.notify();
        }
    }

    public void start() {
        Thread worker = new Thread(() -> {
            while (true) {
                PriorityTask task;
                synchronized (queue) {
                    while (queue.isEmpty()) {
                        try {
                            queue.wait();
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                    task = queue.poll();
                }
                task.run();
            }
        });
        worker.setDaemon(true);
        worker.start();
    }

    public static void main(String[] args) {
        PriorityTaskExecutor executor = new PriorityTaskExecutor();
        executor.start();

        executor.submit(new PriorityTask(5));
        executor.submit(new PriorityTask(1));
        executor.submit(new PriorityTask(10));
    }
}

class Test{
    public static void main(String[] args) {
//        ArrayList<Integer> arr = new ArrayList<>();
//        arr.add(1);
//        arr.add(2);
//        arr.add(3);
//        generateSubArray(arr,0,new ArrayList<>());


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate parse = LocalDate.parse("2025-09-27", formatter);

        YearMonth currentMonth = YearMonth.now();
        System.out.println(currentMonth);



    }
     //123
     static void generateSubArray(ArrayList<Integer> arr, int start, ArrayList<Integer> current) {
         int n = arr.size();
    
         if (start == n) return;

         // Include arr[start] in current subarray
         current.add(arr.get(start));
         System.out.println(current);  // print current subarray

         // Extend the subarray by including next elements
         generateSubArray(arr, start + 1, current);

         // Backtrack: remove last element
         current.remove(current.size() - 1);

         // Start a new subarray starting from next element
         generateSubArray(arr, start + 1, new ArrayList<>());
     }

}

