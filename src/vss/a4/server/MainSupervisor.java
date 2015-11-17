/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vss.a4.server;

import java.util.ArrayList;
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
    private boolean shoudRun;
    private final List<Client> clients;
    private final DistributionServer distributionServer;

    public MainSupervisor(List<Client> clients, DistributionServer distributionServer) {
        this.philosophEatingCounters = new ArrayList<>();
        this.clients = clients;
        this.distributionServer = distributionServer;
    }

    @Override
    public void run() {
        while(shoudRun) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            System.out.println("Get eating counters");
            // read out clients
            int index = 0;
            for(Client client: clients) {
                
                try {
                    philosophEatingCounters.set(index, client.getPhiloCount());
                } catch (Exception ex) {
                    
                    ex.printStackTrace();
                    distributionServer.startClients();
                }
                
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
