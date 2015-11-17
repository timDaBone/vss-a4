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

    public DistributionServer() throws Exception {
        this.clientIpAdresses = new ArrayList<>();
        this.clients = new ArrayList<>();

        // Get registry and bind server
        Registry registry = LocateRegistry.getRegistry(1099);
        Server server = (Server) UnicastRemoteObject.exportObject(this, 0);
        registry.bind("server", server);
    }

    public static void main(String[] args) {
        try {
            DistributionServer server = new DistributionServer();
            UserInterface userInterface = new UserInterface(server);
            userInterface.start();
        } catch (Exception ex) {
            Logger.getLogger(DistributionServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void initServer(int placeCount, int philliCount) {
        this.placeCount = placeCount;
        this.philliCount = philliCount;
    }

    void initClients() throws Exception {
        System.out.println(clientIpAdresses);
        for (String ipAdress : clientIpAdresses) {
            System.out.println("Adding " + ipAdress);
            Client client = (Client) Naming.lookup("rmi://" + ipAdress + "/client");
            clients.add(client);
            client.setClients(clientIpAdresses);
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
