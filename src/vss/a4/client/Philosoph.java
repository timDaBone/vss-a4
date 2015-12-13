package vss.a4.client;

import java.rmi.RemoteException;
import java.util.List;
import vss.a4.server.DistributionServer;

/**
 * @author Andreas Buchmann
 * @author Tim Böhnel
 */
public class Philosoph extends Thread {

    private final DistributedClient distributedClient;
    private int eatingCounter;
    private final int index;
    private boolean willBePunished;
    private final Table table;

    public Philosoph(Table table, int index, int eatingCounter, DistributedClient distributedClient) {
        this.distributedClient = distributedClient;
        this.eatingCounter = eatingCounter;
        this.index = index;
        this.table = table;
    }

    public int getCounter() {
        return eatingCounter;
    }

    @Override
    public void run() {
        try {
            // Philo life
            while (distributedClient.shouldRun()) {
                for (int eatCounter = 0; eatCounter < 3; eatCounter++) {
                    thinking();
                    if (willBePunished) {
                        DistributionServer.logging(this + " starts punishment");
                        Thread.sleep(DistributedClient.PENALTY_TIME);
                        DistributionServer.logging(this + " punishment over");
                        willBePunished = false;
                    }
                    int placeIndex = enqueueToPlace();

                    eating(placeIndex);
                }
                sleeping();
            }
        } catch (RemoteException e) {
            DistributionServer.logging("RemoteException at Philosoph-" + getIndex(), e);
        } catch (Exception e) {
            DistributionServer.logging("Exception at Philosoph-" + getIndex(), e);
        }
        stopped();
    }

    private void thinking() throws InterruptedException {
        DistributionServer.logging(this + " goes thinking");
        Thread.sleep(DistributedClient.THINKING_TIME);
    }

    private int enqueueToPlace() throws Exception {
        DistributionServer.logging("Philosoph-" + this.getIndex() + " enqueues.");
        Place minPlace = table.getPlace(distributedClient.getStartPlace());
        DistributionServer.logging("Philosoph-" + this.getIndex() + " Has min place");

        // Try enqueue local and get shortest queue
        for (Place place : table.getPlaces()) {
            if (place.tryEnqueue()) {
                System.out.println(this + " is enqueued at local place " + place.getIndex());
                return place.getIndex();
            }
            if (place.getQueueLength() < minPlace.getQueueLength()) {
                minPlace = place;
            }
        }
        DistributionServer.logging("Philosoph-" + this.getIndex() + " tried enqueue locally");

        // Try enqueue remote
        List<Client> clients = distributedClient.getClients();
        DistributionServer.logging("Philosoph-" + this.getIndex() + " has clinets");
        for (Client client : clients) {
            int placeIndex = client.tryEnqueue();
            if (placeIndex >= 0) {
                System.out.println(this + " is enqueued at remote place " + placeIndex);
                return placeIndex;
            }
        }

        DistributionServer.logging("Philosoph-" + this.getIndex() + " tried enqueue remote");

        // Enqueue at local place with shortest queue
        minPlace.enqueue();
        System.out.println(this + " is enqueued at local minPlace " + minPlace.getIndex());
        return minPlace.getIndex();
    }

    private void sleeping() throws InterruptedException {
        DistributionServer.logging(this + " goes sleeping");
        Thread.sleep(DistributedClient.SLEEPING_TIME);
    }

    private void eating(int placeIndex) throws Exception {
        DistributionServer.logging(this + " goes eating");
        DistributionServer.logging(this + " took place " + placeIndex);

        // Even philos begin with "left fork", odd with "right fork"
        if (placeIndex % 2 == 0) {
            distributedClient.takeFork((placeIndex + 1) % distributedClient.getPlaceCount());
            DistributionServer.logging(this + " has fork " + (placeIndex + 1) % distributedClient.getPlaceCount());
            distributedClient.takeFork(placeIndex);
            DistributionServer.logging(this + " has fork " + placeIndex);
        } else {
            distributedClient.takeFork(placeIndex);
            DistributionServer.logging(this + " has fork " + placeIndex);
            distributedClient.takeFork((placeIndex + 1) % distributedClient.getPlaceCount());
            DistributionServer.logging(this + " has fork " + (placeIndex + 1) % distributedClient.getPlaceCount());
        }

        DistributionServer.logging(this + " goes eating");

        // schbaggeddi  schbaggeddi  schbaggeddi mmmmmmhhhhhh  schbaggeddi mmmmmmhhhhhh schbaggeddi  
        // schbaggeddi mmmmmmhhhhhh schbaggeddi  schbaggeddi  schbaggeddi  schbaggeddi mmmmmmhhhhhh
        Thread.sleep(DistributedClient.EATING_TIME);

        this.eatingCounter++;

        DistributionServer.logging(this + " passes forks back");
        distributedClient.passBackFork(placeIndex);
        distributedClient.passBackFork((placeIndex + 1) % distributedClient.getPlaceCount());

        DistributionServer.logging(this + " goes thinking");
        distributedClient.leavePlace(placeIndex);
    }

    public int getIndex() {
        return this.index;
    }

    public int getEatingCounter() {
        return this.eatingCounter;
    }

    @Override
    public String toString() {
        return "Philosoph-" + this.getIndex() + " at Table " + table.hashCode();
    }

    void punish() {
        willBePunished = true;
    }

    private void stopped() {
        DistributionServer.logging(this + " stopped");
        this.distributedClient.stopped();
    }

}
