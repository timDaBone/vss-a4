/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vss.a4.client;

import java.net.MalformedURLException;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import vss.a4.server.DistributionServer;
import vss.a4.server.Server;

/**
 *
 * @author abuch_000
 */
public class DistributedClient extends Thread implements Client {

    String serverIpAdress;
    String clientIpAdress;
    Server server;
    private List<Client> clients;

    public DistributedClient(String serverIpAdress, String clientIpAdress, int registryPort) {
        this.serverIpAdress = serverIpAdress;
        this.clientIpAdress = clientIpAdress;

        try {
            this.server = (Server) Naming.lookup("rmi://" + serverIpAdress + "/server");

            Registry registry = LocateRegistry.getRegistry(registryPort);
            Client client = (Client) UnicastRemoteObject.exportObject(this, 0);
            registry.bind("client", client);

        } catch (NotBoundException ex) {
            ex.printStackTrace();
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (RemoteException ex) {
            ex.printStackTrace();
        } catch (AlreadyBoundException ex) {
            Logger.getLogger(DistributedClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        DistributedClient distributedClient = new DistributedClient(args[0], args[1], Integer.parseInt(args[2]));
        try {
            distributedClient.server.addClient(distributedClient.clientIpAdress);
        } catch (RemoteException ex) {
            Logger.getLogger(DistributedClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void setClients(List<String> clientIpAdresses) throws RemoteException {
        try {
            for (String ipAdress : clientIpAdresses) {
                if (ipAdress != this.clientIpAdress) {
                    clients.add((Client) Naming.lookup("rmi://" + ipAdress + "/client"));
                }

            }
            for (Client client : clients) {
                client.hello(this.clientIpAdress);
            }
        } catch (NotBoundException ex) {
            Logger.getLogger(DistributionServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(DistributionServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(DistributionServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void hello(String callingIp) {
        System.out.println("This ipAdress " + this.clientIpAdress + " calling Ip Adress " + callingIp);
    }
}
