package vss.a4.server;

import java.net.MalformedURLException;
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
import test.vss.a4.server.Test2;
import test.vss.a4.server.Test2Impl;
import vss.a4.client.Client;

/**
 *
 * @author abuch_000
 */
public class DistributionServer extends Thread implements Server {

    List<String> clientIpAdresses;
    List<Client> clients;
    private int philliCount;
    private int placeCount;

    public DistributionServer() {
        this.clientIpAdresses = new ArrayList<>();
        this.clients = new ArrayList<>();

        // Get registry and bind server
        try {
            Registry registry = LocateRegistry.getRegistry(1099);
            Server server = (Server) UnicastRemoteObject.exportObject(this, 0);
            registry.bind("server", server);
        } catch (RemoteException ex) {
            Logger.getLogger(DistributionServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AlreadyBoundException ex) {
            Logger.getLogger(DistributionServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        DistributionServer server = new DistributionServer();
        UserInterface userInterface = new UserInterface(server);
        userInterface.start();
    }

    void initServer(int placeCount, int philliCount) {
        this.placeCount = placeCount;
        this.philliCount = philliCount;
    }

    void initClients() {
        System.out.println(clientIpAdresses);

        try {
            for (String ipAdress : clientIpAdresses) {
                clients.add((Client) Naming.lookup("rmi://" + ipAdress + "/client"));
            }
        } catch (NotBoundException ex) {
            Logger.getLogger(DistributionServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(DistributionServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(DistributionServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.start();
    }

    @Override
    public void run() {

    }

    public void addClient(String ipAdress) {
        this.clientIpAdresses.add(ipAdress);
    }

}
