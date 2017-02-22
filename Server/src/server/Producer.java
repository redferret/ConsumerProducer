
package server;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * The producer will constantly add new resources to the buffer until it
 * is full; Once full each producer will be blocked by the blocking queue.
 * A producer will sleep for a random amount of time specified in the
 * program arguments after a resource has been placed.
 * @author Richard DeSilvey 
 */
public class Producer implements Runnable {

    private int maxSleepTime;
    private BlockingQueue<String> buffer;
    
    /**
     * Resource counter, thread-safe
     */
    private final static AtomicInteger counter = new AtomicInteger(0);
    
    /**
     * Set up a new Producer
     * @param maxSleepTime The maximum amount of time the producer will
     * sleep at.
     * @param buffer Reference to the resource buffer
     */
    public Producer(int maxSleepTime, BlockingQueue<String> buffer){
        this.maxSleepTime = maxSleepTime;
        this.buffer = buffer;
    }
    
    /**
     * Since the JVM can have many implementations it is safe to assume
     * that the System.out is not synchronized. 
     * @param s The string being printed
     */
    public void safePrintln(String s) {
        synchronized (System.out) {
            System.out.println(s);
        }
    }
    
    /**
     * The producer will run indefinity until the server is shut down,
     * each thread can be blocked in the add method of the resource list before
     * the thread will sleep for a random amount of time.
     */
    @Override public void run() {
        int count;
        
        while(true){
            try {
                count = counter.incrementAndGet();
                buffer.add("Res-" + count + ": " 
                            + Thread.currentThread().getName());
                safePrintln(buffer.toString() + " reported by: " + Thread.currentThread().getName());
                Thread.sleep(Math.max(100, maxSleepTime));
            } catch (InterruptedException ie) {}
        }   
    }
}
