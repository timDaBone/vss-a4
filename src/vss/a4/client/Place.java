/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vss.a4.client;

import java.rmi.Remote;
import java.rmi.RemoteException;
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

    public boolean tryEnqueue() {
        boolean tookPlace = false;
        try {
            tookPlace = takePlace.tryAcquire(0, TimeUnit.SECONDS);
        } catch(InterruptedException ex) {
            ex.printStackTrace();
        }
        if (tookPlace) {
            if (isEmpty()) {
                empty = false;
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
    public void enqueue() {
        try {
            takePlace.acquire();
        } catch(InterruptedException ex) {
            ex.printStackTrace();
        }
        if (isEmpty()) {
            empty = false;
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

