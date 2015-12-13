package vss.a4.client;

import java.util.concurrent.Semaphore;

/**
 *
 * @author Andi Buchmann
 * @author Tim Böhnel
 */
public class Fork {

    private final Semaphore available = new Semaphore(1, true);

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
