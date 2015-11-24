package vss.a4.server;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import vss.a4.client.Client;
import vss.a4.exceptions.VssException;

/**
 *
 * @author abuch_000
 */
public class DistributionServer implements Server {

    private static boolean DEBUG;

    List<String> clientIpAdresses;
    List<Client> clients;
    private int philliCount;
    private int placeCount;
    private MainSupervisor mainSupervisor;
    List<Integer> philosophEatingCounters;

    public DistributionServer(boolean debug) throws Exception {
        this.clientIpAdresses = new ArrayList<>();
        this.clients = new ArrayList<>();
        this.mainSupervisor = null;
        this.DEBUG = debug;
        this.mainSupervisor = new MainSupervisor(this);
        this.mainSupervisor.start();

        // Get registry and bind server
        Registry registry = LocateRegistry.getRegistry(1099);
        Server server = (Server) UnicastRemoteObject.exportObject(this, 0);
        registry.bind("server", server);
    }

    public static void main(String[] args) {
        try {
            DistributionServer server = new DistributionServer(Boolean.parseBoolean(args[0]));
            UserInterface userInterface = new UserInterface(server);
            userInterface.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void initServer(int placeCount, int philliCount, boolean firstInit) {
        this.placeCount = placeCount;
        this.philliCount = philliCount;
        initClients();
    }

    private void stopClients() throws RemoteException {
        DistributionServer.logging("stopClients()");
        for (Client client : clients) {
            client.stopClient();
        }
    }

    void initClients() {
        DistributionServer.logging("initClients()");
        DistributionServer.logging("Client IP-List " + clientIpAdresses);
        DistributionServer.logging("Client List " + clients);
        DistributionServer.logging("EatingCounters on Server " + this.philosophEatingCounters);

        for (String ipAdress : clientIpAdresses) {
            DistributionServer.logging("Adding IP " + ipAdress, null);
            Client client = null;
            try {
                client = (Client) Naming.lookup("rmi://" + ipAdress + "/client");
            } catch (RemoteException ex) {
                cleanIpsAndClients(ex, ipAdress);
                initClients();
                return;
            } catch (Exception ex) {
                DistributionServer.logging("Naming.lookup to Client " + ipAdress + " throws Exception", ex);
            }

            DistributionServer.logging("Working on ipAdress " + ipAdress, null);
            DistributionServer.logging("Working on ipAdress " + client, null);
            if (client != null) {
                try {
                    clients.add(client);
                    client.setClients(clientIpAdresses);
                } catch (RemoteException ex) {
                    DistributionServer.logging("Client" + ipAdress + " is not available anymore.");
                    cleanIpsAndClients(ex, ipAdress);
                    initClients();
                    return;
                } catch (VssException ex) {
                    DistributionServer.logging("Client" + ex.getIpAdress() + " is not available anymore.");
                    cleanIpsAndClients(ex, ex.getIpAdress());
                    initClients();
                    return;
                }
            }
        }

        try {
            stopClients();

            // todo verteilung über die clients starten
            int index = 0;
            for (Client client : clients) {
                // Schleife ändern, wenn philosophen eingeführt werden TODO
                client.init(0, 0, new ArrayList<Integer>(), 0, 0);
                index++;
            }

            for (Client client : clients) {
                client.startClient();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            clients.clear();
            initClients();
            return;
        }
        mainSupervisor.setClients(clients);
    }

    private void cleanIpsAndClients(Exception ex, String ipAdress) {
        DistributionServer.logging("cleanIpsAndClients()", ex);
        DistributionServer.logging("cleanIpsAndClients() removing " + ipAdress + " from IP-List", null);
        clientIpAdresses.remove(ipAdress);
        DistributionServer.logging("cleanIpsAndClients() clear Client List", null);
        clients.clear();
    }

    public void addClient(String ipAdress) {
        this.clientIpAdresses.add(ipAdress);
        initClients();
    }

    public static void logging(String message) {
        System.out.println(message);
    }

    public static void logging(String message, Exception ex) {
        if (DEBUG && ex == null) {
            System.out.println(message);
        }
        if (DEBUG && ex != null) {
            System.out.println(message);
            ex.printStackTrace();
        }
    }

}
