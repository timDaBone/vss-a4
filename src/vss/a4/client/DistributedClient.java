/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vss.a4.client;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
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
                } catch(RemoteException ex) {
                    DistributionServer.logging("VSSException was thrown from " + ipAdress, ex);
                    throw new VssException(ipAdress, ex.getMessage());
                } catch(Exception ex) {
                    DistributionServer.logging("Exception was thrown from " + ipAdress, ex);
                } 
            }
        }
    }

    @Override
    public void init(int firstPhilosoph, int lastPhilosoph, List<Integer> eatingCounters, int firstPlace, int lastPlace) throws Exception {
        this.philosoph = new Philosoph(firstPlace);
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
        philosoph.start();
        DistributionServer.logging("Philosoph " + this + " started");
    }

    @Override
    public void stopClient() throws RemoteException {
        if(philosoph != null) {
            philosoph.stopPhilosoph();
            DistributionServer.logging("Philosoph " + this + " stopped");
        }
    }
    
    @Override
    public String toString() {
        return "Client (" + clientIpAdress + ")";
    }

}
