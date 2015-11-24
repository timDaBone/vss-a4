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

    public MainSupervisor(DistributionServer distributionServer) {
        DistributionServer.logging("MainSupervisor Created");
        this.clients = new ArrayList<>();
        this.philosophEatingCounters = new ArrayList<>();
        for (int i = 0; i < clients.size(); i++) {
            philosophEatingCounters.add(0);
        }
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
                        DistributionServer.logging("MainSupervisor get eating counter " + client.getPhiloCount());
                    }
                }

            } catch (ConcurrentModificationException ex) {
                DistributionServer.logging("MainSupervisor gets ModificationException with List " + clients, ex);
            } catch (RemoteException ex) {
                DistributionServer.logging("MainSupervisor has Connection Problem with Client", ex);
                distributionServer.initClients();
            } catch (Exception ex) {
                DistributionServer.logging("MainSupervisor has ConnectionProbblem with Client", ex);
            }
            DistributionServer.logging("EatingCounters on MainSupervisor " + this.philosophEatingCounters, null);
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
