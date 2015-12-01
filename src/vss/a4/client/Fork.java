package vss.a4.client;

import java.util.concurrent.Semaphore;

/**
 *
 * @author Andi Buchmann
 * @author Tim Böhnel
 */
public class Fork {
    
    private final Semaphore available = new Semaphore(1, true);
    private final int index;
    
    /**
     * Constructor for a fork.
     * 
     * @param index 
     */
    public Fork(int index) {
        this.index = index;
    }
    
    /**
     * Takes the fork.
     * 
     * @throws InterruptedException 
     */
    public void take() throws InterruptedException {
        available.acquire();
    }
    
    /**
     * Passes the fork back.
     * 
     */
    public void passBack() {
        available.release();
    }
    
}
