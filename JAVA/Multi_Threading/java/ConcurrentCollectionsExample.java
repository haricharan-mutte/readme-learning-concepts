package JAVA.Multi_Threading.java;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentCollectionsExample {
    static ConcurrentHashMap<String,String> map = new ConcurrentHashMap<>();
    PriorityBlockingQueue<String> queue = new PriorityBlockingQueue<>();

    static int p1,p2,c1,c2,p3=0;
    public static void main(String[] args) throws InterruptedException {
       BoundedBuffer<Integer> buffer = new BoundedBuffer<>(5);
        ExecutorService executorService = Executors.newCachedThreadPool();
            executorService.submit(new Thread(()-> {
                for(int i=1;i<10;i++){
                    int data = i;
                    try {
                        buffer.add(data);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }},"P1-"+(++p1)));

        executorService.submit(new Thread(()-> {
            for(int i=1;i<10;i++){
                int data = i;
                try {
                    buffer.add(data);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }},"P2-"+(++p2)));

        executorService.submit(new Thread(()-> {
            for(int i=1;i<10;i++){
                int data = i;
                try {
                    buffer.add(data);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }},"P2-"+(++p3)));
        executorService.submit(new Thread(()-> {
        for(int i=1;i<15;i++){
            int data = i;
            try {
                buffer.consume();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }},"P2-"+(++c1)));

        executorService.submit(new Thread(()-> {
            for(int i=1;i<15;i++){
                int data = i;
                try {
                    buffer.consume();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }},"P2-"+(++c2)));
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        Thread.currentThread().join();
        System.out.println("C1-"+c1+" C2-"+c2+" P1-"+p1+" P2-"+p2+ " " +
                "P3-"+p3);
    }

}

class BoundedBuffer<T>{
    private final Queue<T> elements;
    private final int maxsize;
    Lock lock;

    public BoundedBuffer(int maxsize) {
        this.elements = new LinkedList<>();
        this.maxsize = maxsize;
        this.lock = new ReentrantLock(true);
    }

    synchronized void add(T data) throws InterruptedException {
        while(elements.size()>=maxsize){
            System.out.println("waiting for space: "+Thread.currentThread().getName());
            wait();
        }
//        lock.lock();
            System.out.println("producing data:"+ Thread.currentThread().getName()+"and element: "+data);
            elements.add(data);
            notifyAll();
//        lock.unlock();
//        Thread.sleep(1000);
    }

    synchronized void consume() throws InterruptedException {
        while(elements.isEmpty()){
            System.out.println("waiting to consume: "+Thread.currentThread().getName());
            wait();
        }

//        lock.lock();
        System.out.println("consuming data by Thread: "+Thread.currentThread().getName()+"and element: "+elements.remove());
        notifyAll();
//        lock.unlock();
//        Thread.sleep(1000);
    }

    int size(){
        return elements.size();
    }
}


class PrintNumbersSequence {
    private final Lock lock = new ReentrantLock();
    private final Condition condT1 = lock.newCondition();
    private final Condition condT2 = lock.newCondition();
    private final Condition condT3 = lock.newCondition();

    private String turn = "T1";
    private int number = 1;
    private final int limit = 9;

    public static void main(String[] args) {
        PrintNumbersSequence seq = new PrintNumbersSequence();
        new Thread(() -> seq.print("T1", seq.condT1, seq.condT2), "T1").start();
        new Thread(() -> seq.print("T2", seq.condT2, seq.condT3), "T2").start();
        new Thread(() -> seq.print("T3", seq.condT3, seq.condT1), "T3").start();
    }

    private void print(String myTurn, Condition myCond, Condition nextCond) {
        lock.lock();
        try {
            while (number <= limit) {
                while (!turn.equals(myTurn)) {
                    myCond.await();
                }
                if (number <= limit) {
                    System.out.println(number + " printed by " + myTurn);
                    number++;
                    // set next turn
                    if (myTurn.equals("T1")) turn = "T2";
                    else if (myTurn.equals("T2")) turn = "T3";
                    else turn = "T1";

                    nextCond.signal();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }

}

class NumberSequenceNThreads {
    private final int N;          // number of threads
    private final int LIMIT;      // maximum number to print
    private int number = 1;
    private final Object lock = new Object();

    NumberSequenceNThreads(int n, int limit) {
        this.N = n;
        this.LIMIT = limit;
    }

    public void print(int threadId) {
        while (true) {
            synchronized (lock) {
                while (number <= LIMIT && (number - 1) % N != threadId) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                if (number > LIMIT)
                    break;

                System.out.println("Thread " + (threadId + 1) + " → " + number);
                number++;
                lock.notifyAll();
            }
        }
    }

    public static void main(String[] args) {
        int N = 5;     // Try changing this to 2 or 3
        int LIMIT = 20;

        NumberSequenceNThreads obj = new NumberSequenceNThreads(N, LIMIT);
        for (int i = 0; i < N; i++) {
            int threadId = i;
            new Thread(() -> obj.print(threadId), "T" + (threadId + 1)).start();
        }
    }
}
