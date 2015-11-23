/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vss.a4.client;

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
            ex.printStackTrace();
        }
    }

    @Override
    public void setClients(List<String> clientIpAdresses) throws Exception {
        for (String ipAdress : clientIpAdresses) {
            if (!ipAdress.equals(this.clientIpAdress)) {
                try {
                    clients.add((Client) Naming.lookup("rmi://" + ipAdress + "/client"));
                } catch(RemoteException ex) {
                    throw new VssException(ipAdress, ex.getMessage());
                }
            }
        }
    }

    @Override
    public void init(int i, int places) throws Exception {
        this.philosoph = new Philosoph(i);
    }

    @Override
    public int getPhiloCount() throws Exception {
        if (philosoph != null) {
            return philosoph.getCounter();
        }
        throw new Exception("Philosoph is null.");
    }

    @Override
    public void startClient() throws RemoteException {
        philosoph.start();
        System.out.println("Philo started");
    }

    @Override
    public void stopClient() throws RemoteException {
        if(philosoph != null) {
            philosoph.stopPhilosoph();
            System.out.println("Philo stopped");
        }
    }

}
