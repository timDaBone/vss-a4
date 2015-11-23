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
    private final List<Client> clients;
    private final DistributionServer distributionServer;

    public MainSupervisor(List<Client> clients, DistributionServer distributionServer) {
        System.out.println("MainSupervisor Created");
        this.philosophEatingCounters = new ArrayList<>();
        for (int i = 0; i < clients.size(); i++) {
            philosophEatingCounters.add(0);
        }
        this.clients = clients;
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
            System.out.println("Get eating counters");
            // read out clients
            int index = 0;
            try {
                for (Client client : clients) {
                    philosophEatingCounters.set(index, client.getPhiloCount());
                    index++;
                }
            } catch (ConcurrentModificationException ex) {
                System.out.println(clients);
                ex.printStackTrace();
            } catch (RemoteException ex) {
                ex.printStackTrace();
                distributionServer.startClients();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            System.out.println("Supervisor Eatingcounter: " + philosophEatingCounters);

        }
    }

    void stopMainSupervisor() {
        this.shoudRun = false;
    }

    List<Integer> getEatingCounters() {
        return this.philosophEatingCounters;
    }

}
