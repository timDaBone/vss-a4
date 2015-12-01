/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vss.a4.client;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import vss.a4.exceptions.VssException;
import vss.a4.server.DistributionServer;
import vss.a4.server.Server;

/**
 *
 * @author abuch_000
 */
public class DistributedClient implements Client {

    public final static long PENALTY_TIME = 100;
    public final static long THINKING_TIME = 100;
    public final static long SLEEPING_TIME = 100;
    static int MAX_PLACES;

    String serverIpAdress;
    String clientIpAdress;
    Server server;
    private List<Client> clients;
    private int firstPhilosoph;
    private int lastPhilosoph;
    private List<Integer> eatingCounters;
    private int placeCount;
    private int lastPlace;
    private int firstPlace;
    private Table table;

    public List<Client> getClients() {
        return clients;
    }
    List<Philosoph> philosophs;

    public DistributedClient(String serverIpAdress, String clientIpAdress, int registryPort) throws Exception {
        this.serverIpAdress = serverIpAdress;
        this.clientIpAdress = clientIpAdress;
        this.clients = new ArrayList<>();

        // Setup RMI to server
        this.server = (Server) Naming.lookup("rmi://" + serverIpAdress + "/server");

        // Initiate local RMI
        Registry registry = LocateRegistry.getRegistry(registryPort);
        Client client = (Client) UnicastRemoteObject.exportObject(this, 0);
        registry.bind("client", client);
    }

    public static void main(String[] args) {
        try {
            DistributedClient distributedClient = new DistributedClient(args[0], args[1], Integer.parseInt(args[2]));
            distributedClient.server.addClient(distributedClient.clientIpAdress);
        } catch (Exception ex) {
            DistributionServer.logging("Client Registry Error or Server not available", ex);
        }
    }

    @Override
    public void setClients(List<String> clientIpAdresses) throws VssException {
        for (String ipAdress : clientIpAdresses) {
            if (!ipAdress.equals(this.clientIpAdress)) {
                try {
                    clients.add((Client) Naming.lookup("rmi://" + ipAdress + "/client"));
                } catch (RemoteException ex) {
                    DistributionServer.logging("VSSException was thrown from " + ipAdress, ex);
                    throw new VssException(ipAdress, ex.getMessage());
                } catch (Exception ex) {
                    DistributionServer.logging("Exception was thrown from " + ipAdress, ex);
                }
            }
        }
    }

    @Override
    public void init(int firstPhilosoph, int lastPhilosoph, List<Integer> eatingCounters, int firstPlace, int lastPlace, int placeCount) throws Exception {
        this.firstPhilosoph = firstPhilosoph;
        this.lastPhilosoph = lastPhilosoph;
        this.eatingCounters = eatingCounters;
        this.firstPlace = firstPlace;
        this.lastPlace = lastPlace;
        this.placeCount = placeCount;
        this.table = new Table(firstPlace, lastPlace);
        DistributionServer.logging("Philosoph " + this + " initialized", null);
    }

    @Override
    public Map<Integer, Integer> getPhiloCount() throws Exception {
        Map<Integer, Integer> philoCount = new HashMap<>();
        for(Philosoph philosoph: philosophs) {
            philoCount.put(philosoph.getIndex(), philosoph.getEatingCounter());
        }
        DistributionServer.logging("FATAL ERROR", ex);
        throw ex;
    }

    @Override
    public void startClient() throws RemoteException {
        
        // synchronizer because of ThreadState
        synchronized (this) {
            for(int index = this.firstPhilosoph; index <= this.lastPhilosoph; index++) {
                Philosoph philosoph = new Philosoph(table, index, SLEEPING_TIME, THINKING_TIME, placeCount, this);
                this.philosophs.add(philosoph);
                philosoph.start();
            }
        }

        DistributionServer.logging("Philosoph " + this + " started");
    }

    @Override
    public void stopClient() throws RemoteException {
        for(Philosoph philosoph: philosophs) {
            philosoph.stopPhilosoph();
        }
    }

    @Override
    public String toString() {
        return "Client (" + clientIpAdress + ")";
    }

    @Override
    public void takeFork(int forkIndex) throws Exception {
        if (forkIndex >= this.firstPlace && forkIndex <= this.lastPlace) {
            this.table.takeFork(forkIndex);
        } else {
            for (Client client : clients) {
                if (forkIndex >= client.getFirstPlace() && forkIndex <= client.getLastPlace()) {
                    client.takeFork(forkIndex);
                    break;
                }
            }
        }
    }

    @Override
    public void passBackFork(int forkIndex) throws Exception {
        if (forkIndex >= this.firstPlace && forkIndex <= this.lastPlace) {
            this.table.passBackFork(forkIndex);
        } else {
            for (Client client : clients) {
                if (forkIndex >= client.getFirstPlace() && forkIndex <= client.getLastPlace()) {
                    client.passBackFork(forkIndex);
                    break;
                }
            }
        }
    }

    @Override
    public void leavePlace(int placeIndex) throws Exception {
        if (placeIndex >= this.firstPlace && placeIndex <= this.lastPlace) {
            this.table.leavePlace(placeIndex);
        } else {
            for (Client client : clients) {
                if (placeIndex >= client.getFirstPlace() && placeIndex <= client.getLastPlace()) {
                    client.leavePlace(placeIndex);
                    break;
                }
            }
        }
    }


    @Override
    public int tryEnqueue() throws Exception {
        return this.table.tryEnqueue();
    }

    @Override
    public int getFirstPlace() throws RemoteException {
        return this.firstPlace;
    }

    @Override
    public int getLastPlace() throws RemoteException {
        return this.lastPlace;
    }

}
