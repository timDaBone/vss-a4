package vss.a4.client;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * A place where a philosoph can eat.
 *
 * @author Andi Buchmann
 * @author Tim Böhnel
 */
public class Place {

    private boolean empty = true;
    private final int index;
    private final Semaphore takePlace = new Semaphore(1, true);

    /**
     * Constructor for a place.
     *
     * @param index
     */
    public Place(int index) {
        this.index = index;
    }

    /**
     * Return true if empty.
     *
     * @return empty
     */
    private boolean isEmpty() {
        return empty;
    }

    public boolean tryEnqueue() throws Exception {
        boolean tookPlace = takePlace.tryAcquire(0, TimeUnit.SECONDS);
        if (tookPlace) {
            if (isEmpty()) {
                empty = false;
            } else {
                throw new Exception("Place-" + this.getIndex() + " is not empty!");
            }
        }
        return tookPlace;
    }

    /**
     * Enqueues a philosoph into the queue for a place. The philosoph waits
     * until the place is free.
     *
     * @throws Exception
     */
    public void enqueue() throws Exception {
        takePlace.acquire();
        if (isEmpty()) {
            empty = false;
        } else {
            throw new Exception("Place-" + this.getIndex() + " is not empty!");
        }
    }

    /**
     * Leave the place. (Should only be called by the Philosoph which sits on
     * the place.
     *
     */
    public void leave() {
        empty = true;
        takePlace.release();
    }

    /**
     * Get index of the place.
     *
     * @return index
     */
    int getIndex() {
        return index;
    }

    /**
     * Get the length of the queue.
     *
     * @return length
     */
    int getQueueLength() {
        return this.takePlace.getQueueLength();
    }

}
