package JAVA.Multi_Threading.java;

import java.util.concurrent.*;
public class RateLimiterWithWait {

    // Allow only 5 executions per minute
    private static final int LIMIT = 5;
    private static final Semaphore semaphore = new Semaphore(LIMIT, true);

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(10);

        // Reset permits every minute
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            int toRelease = LIMIT - semaphore.availablePermits();
            if (toRelease > 0) {
                semaphore.release(toRelease);
                System.out.println("---- Refilling permits ----");
            }
        }, 1, 1, TimeUnit.MINUTES);

        // Simulate 20 tasks trying to execute
        for (int i = 1; i <= 20; i++) {
            int id = i;
            executor.submit(() -> runTask(id));
        }

        executor.shutdown();
    }

    private static void runTask(int id) {
        try {
            semaphore.acquire(); // waits if limit is reached
            System.out.println("Thread " + id + " executing at " + System.currentTimeMillis());
            processTask();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void processTask() {
        try {
            Thread.sleep(1000); // simulate some work
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class TestMyknowledge{
    private Semaphore limiter;
    private final int maxEligible;
    private ScheduledExecutorService scheduledExecutorService;

    public TestMyknowledge(int limiter,int maxEligible) {
        this.limiter = new Semaphore(limiter,true);
        this.maxEligible = maxEligible;
        this.scheduledExecutorService = Executors.newScheduledThreadPool(1);
    }

     void scheduleRefelling(){
        scheduledExecutorService.scheduleAtFixedRate(()->{
            int rleaseLimit = maxEligible - limiter.availablePermits();
            if(rleaseLimit>0){
                limiter.release(rleaseLimit);
            }
        },1,5,TimeUnit.SECONDS);
    }

    void testSeamphore() throws InterruptedException {
        //blocks the threads until availble permit
        limiter.acquire();
        System.out.println("started by Thread :"+Thread.currentThread().getName());
        Thread.sleep(1000);

    }

    public static void main(String[] args) throws InterruptedException {
        ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(10);
        TestMyknowledge simulator = new TestMyknowledge(5,5);
        Runnable task = ()-> {
            try {
                simulator.testSeamphore();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };
        for(int i=1;i<10;i++){
            threadPoolExecutor.submit(new Thread(task,"T-"+i));
        }
        Thread.currentThread().join();
        System.out.println("completed the task");

    }
}

