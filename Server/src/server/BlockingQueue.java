
package server;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The blocking queue class contains a concurrent linked queue which is
 * non-blocking and multi-thread safe. This is to experiment on threads that are
 * blocked when a maximum number of resources are generated and/or taken. The
 * concurrent list or queue works with multi-threading applications so each
 * producer and consumer will be synchronized correctly.
 *
 * @author Richard DeSilvey
 * @param <T> The object type
 */
public class BlockingQueue<T> {

    private ConcurrentLinkedQueue<T> list;
    private int limit;
    
    /**
     * Create a new blocking queue
     * @param limit The maximum number of resources that can go into the queue
     */
    public BlockingQueue(int limit){
        this.limit = limit;
        list = new ConcurrentLinkedQueue<>();
    }
    
    /**
     * Adds a new object to the queue and blocks any thread trying to 
     * add new items when a limit is reached. When the list does become
     * full consumers will notify all threads that the list is full.
     * @param obj
     * @throws InterruptedException 
     */
    public synchronized void add(T obj) throws InterruptedException {
        while(list.size() == limit) wait();
        
        if (list.isEmpty()){
            notifyAll();
        }
        
        list.add(obj);
    }
    
    /**
     * Removes a resource from the list. Threads are blocked if the list
     * is empty.
     * @return The next resource in the queue
     * @throws InterruptedException 
     */
    public synchronized T poll() throws InterruptedException {
        
        while(list.isEmpty()) wait();
        
        if (list.size() == limit){
            notifyAll();
        }
        
        return list.poll();
    }
    
    /**
     * If the list is empty or not
     * @return If the list is empty or not
     */
    public boolean isEmpty(){
        return list.isEmpty();
    }
    
    /**
     * String representation of the list
     * @return String representation of the list
     */
    public String toString(){
        return list.toString();
    }
    
}
