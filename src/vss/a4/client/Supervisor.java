package vss.a4.client;

import java.util.List;

/**
 *
 * @author Andi Buchmann
 * @author Tim Böhnel
 */
public class Supervisor extends Thread {

    private final List<Philosoph> philosophs;
    private boolean shouldRun;

    public Supervisor(List<Philosoph> philosophs) {
        this.philosophs = philosophs;
        this.shouldRun = true;
    }

    @Override
    public void run() {
        super.run();
        while (shouldRun) {

            // Get average eatingcounter
            int averageEatingCounter = 0;
            for (Philosoph philosoph : philosophs) {
                averageEatingCounter += philosoph.getEatingCounter();
            }

            averageEatingCounter = averageEatingCounter / philosophs.size();

            // Punish philos which ate too much
            for (Philosoph philosoph : philosophs) {
                if (philosoph.getEatingCounter() - DistributedClient.MAXIMUM_EATING_DIFFERENCE_AVERAGE >= averageEatingCounter) {
                    philosoph.punish();
                }
            }

            try {
                Thread.sleep(DistributedClient.SLEEPING_TIME_SUPERVISOR);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    void stopSupervisor() {
        shouldRun = false;
    }

}
