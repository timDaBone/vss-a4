/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vss.a4.client;

/**
 *
 * @author tboeh
 */
public class Philosoph extends Thread {

    private int counter;
    private boolean shouldRun;
    private int index;

    public Philosoph(int counter) {
        this.counter = counter;
        this.shouldRun = true;
    }
    
    

    public int getCounter() {
        return counter;
    }

    @Override
    public void run() {
        while (shouldRun) {
            this.counter++;
            try {
                Thread.sleep(1000);
        System.out.println("Philosoph " + this + " eating");
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    void stopPhilosoph() {
        System.out.println("Philosoph " + this + " stopped");
        shouldRun = false;
    }
}
