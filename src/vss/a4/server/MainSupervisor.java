/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vss.a4.server;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import vss.a4.client.Client;

/**
 *
 * @author tboeh
 */
public class MainSupervisor extends Thread {

    private final List<Integer> philosophEatingCounters;
    private boolean shoudRun = true;
    private List<Client> clients;
    private final DistributionServer distributionServer;

    public MainSupervisor(DistributionServer distributionServer,List<Integer> philosophEatingCounters, int philoCount) {
        DistributionServer.logging("MainSupervisor Created");
        this.clients = new ArrayList<>();
        this.philosophEatingCounters = philosophEatingCounters;
        System.out.println("SIIIIIIIIIIIIZZEZEEEEE PHILO LISTE: " + philosophEatingCounters.size());
        System.out.println("Philocount: " + philoCount);
        if(philoCount > philosophEatingCounters.size()) {
            System.out.println("Add " + (philoCount-philosophEatingCounters.size()) + " philos");
            for(int index = philosophEatingCounters.size(); index < philoCount; index ++) {
                System.out.println("ADDD PHILO AT INDEX " + index);
                philosophEatingCounters.add(0);
            }
        } else if(philoCount < philosophEatingCounters.size()) {
            for(int index = philosophEatingCounters.size()-1; index >= philoCount; index--) {
                philosophEatingCounters.remove(index);
            }
        }
        System.out.println("PHILO SIZE AFTER: " + philosophEatingCounters.size());
        this.distributionServer = distributionServer;
    }

    @Override
    public void run() {
        while (shoudRun) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }

            // read out clients
            try {
                synchronized (this) {
                    for (Client client : clients) {
                        Map<Integer, Integer> philoCount = client.getPhiloCount();
                        for(Entry<Integer, Integer> entry: philoCount.entrySet()) {
                            philosophEatingCounters.set(entry.getKey(), entry.getValue());
                        }
                        DistributionServer.logging("MainSupervisor get eating counter " + philoCount);
                    }
                }
            } catch (ConcurrentModificationException ex) {
                DistributionServer.logging("MainSupervisor gets ModificationException with List " + clients, ex);
            } catch (RemoteException ex) {
                DistributionServer.logging("MainSupervisor has Connection Problem with Client", ex);
                shoudRun = false;
                distributionServer.initClients();
            } catch (Exception ex) {
                DistributionServer.logging("MainSupervisor has ConnectionProbblem with Client", ex);
            }
            //DistributionServer.logging("Clients on MainSupervisor "  + clients, null);
        }
    }

    void stopMainSupervisor() {
        DistributionServer.logging("MainSupervisor Stopped", null);
        this.shoudRun = false;
    }

    List<Integer> getEatingCounters() {
        return this.philosophEatingCounters;
    }

    void setClients(List<Client> clients) {
        synchronized (this) {
            this.clients = clients;
        }

    }

}
