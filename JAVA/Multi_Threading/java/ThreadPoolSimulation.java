package JAVA.Multi_Threading.java;

import java.util.LinkedList;
import java.util.Queue;

class WorkerThread extends Thread {
    private TaskQueue taskQueue;
    private boolean isRunning = true;

    public WorkerThread(TaskQueue taskQueue, String name) {
        super(name);
        this.taskQueue = taskQueue;
    }

    public void run() {
        while (isRunning) {
            Runnable task = taskQueue.getTask(); // Waits if no task
            if (task != null) {
                System.out.println(getName() + " picked up a task");
                task.run(); // Execute the task
                System.out.println(getName() + " finished the task");
            }
        }
    }

    public void shutdown() {
        isRunning = false;
        this.interrupt(); // Stop waiting if blocked
    }
}

class TaskQueue {
    private Queue<Runnable> queue = new LinkedList<>();

    public synchronized void addTask(Runnable task) {
        queue.add(task);
        notify(); // Notify waiting thread that a task is available
    }

    public synchronized Runnable getTask() {
        while (queue.isEmpty()) {
            try {
                wait(); // Wait for a task
            } catch (InterruptedException e) {
                return null; // Thread shutdown
            }
        }
        return queue.poll();
    }
}

class CustomThreadPool {
    private WorkerThread[] workers;
    private TaskQueue taskQueue;

    public CustomThreadPool(int poolSize) {
        taskQueue = new TaskQueue();
        workers = new WorkerThread[poolSize];
        for (int i = 0; i < poolSize; i++) {
            workers[i] = new WorkerThread(taskQueue, "Worker-" + (i + 1));
            workers[i].start(); // Start worker threads
        }
    }

    public void submit(Runnable task) {
        taskQueue.addTask(task);
    }

    public void shutdown() {
        for (WorkerThread worker : workers) {
            worker.shutdown();
        }
    }
}

public class ThreadPoolSimulation {
    public static void main(String[] args) throws InterruptedException {
        CustomThreadPool pool = new CustomThreadPool(2);

        for (int i = 1; i <= 100; i++) {
            int taskId = i;
            pool.submit(() -> {
                System.out.println("Executing task " + taskId + " by " + Thread.currentThread().getName());
                try { Thread.sleep(1000); } catch (InterruptedException e) { }
            });
        }

        Thread.sleep(7000); // Wait for all tasks to finish
        pool.shutdown();
    }
}

