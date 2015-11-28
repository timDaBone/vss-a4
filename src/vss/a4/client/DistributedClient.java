/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vss.a4.client;

import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import vss.a4.exceptions.VssException;
import vss.a4.server.DistributionServer;
import vss.a4.server.Server;

/**
 *
 * @author abuch_000
 */
public class DistributedClient implements Client {

    String serverIpAdress;
    String clientIpAdress;
    Server server;
    private List<Client> clients;

    Philosoph philosoph;
    int firstPhilosoph;
    int lastPhilosoph;
    List<Integer> eatingCounters;
    int firstPlace;
    int lastPlace;
    

    public DistributedClient(String serverIpAdress, String clientIpAdress, int registryPort) throws Exception {
        this.serverIpAdress = serverIpAdress;
        this.clientIpAdress = clientIpAdress;
        this.clients = new ArrayList<>();

        // Setup RMI to server
        this.server = (Server) Naming.lookup("rmi://" + serverIpAdress + "/server");
        
        // Initiate local RMI
        Registry registry = LocateRegistry.getRegistry(registryPort);
             
        String[] alreadyBindList = registry.list();
        
        boolean bind = true;
        for(String alreadyBind: alreadyBindList) {
            System.out.println(alreadyBind);
            if(alreadyBind.equals("client"))
                bind = false;
        }
        
        Client client = (Client) UnicastRemoteObject.exportObject(this, 0);
        
        if(bind) {
            registry.bind("client", client);
        } else {
            registry.rebind("client", client);
        }
    }

    public static void main(String[] args) {
        try {
            DistributedClient distributedClient = new DistributedClient(args[0], args[1], Integer.parseInt(args[2]));
            distributedClient.server.addClient(distributedClient.clientIpAdress);
            System.out.println("Client connected to Server");
        } catch (Exception ex) {
            ex.printStackTrace();
            DistributionServer.logging("Client Registry Error or Server not available", ex);
        }
    }

    @Override
    public void setClients(List<String> clientIpAdresses) throws VssException {

        // Cient behält nicht erreichbaren Client in Liste ?!?!
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
    public void init(int firstPhilosoph, int lastPhilosoph, List<Integer> eatingCounters, int firstPlace, int lastPlace) throws Exception {
        this.firstPhilosoph = firstPhilosoph;
        this.lastPhilosoph = lastPhilosoph;
        this.firstPlace = firstPlace;
        this.lastPlace = lastPlace;
        DistributionServer.logging("Philosoph " + this + " initialized", null);
    }

    @Override
    public int getPhiloCount() throws Exception {
        if (philosoph != null) {
            return philosoph.getCounter();
        }
        Exception ex = new Exception("Philosop " + this + " is NULL");
        DistributionServer.logging("FATAL ERROR", ex);
        throw ex;
    }

    @Override
    public void startClient() throws RemoteException {
        synchronized (this) {
            this.philosoph = new Philosoph(1);
            philosoph.start();
        }

        DistributionServer.logging("Philosoph " + this + " started");
    }

    @Override
    public void stopClient() throws RemoteException {
        if (philosoph != null) {
            philosoph.stopPhilosoph();
            DistributionServer.logging("Philosoph " + this + " stopped");
        }
    }

    @Override
    public String toString() {
        return "Client (" + clientIpAdress + ")";
    }

}
