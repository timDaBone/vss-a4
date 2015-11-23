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
    private MainSupervisor mainSupervisor;
    List<Integer> philosophEatingCounters;

    public DistributionServer() throws Exception {
        this.clientIpAdresses = new ArrayList<>();
        this.clients = new ArrayList<>();
        this.mainSupervisor = null;
        
        System.out.println("response: " + System.getProperty("sun.rmi.transport.tcp.responseTimeout"));
        System.out.println("connection: " + System.getProperty("sun.rmi.transport.connectionTimeout"));
        

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
            ex.printStackTrace();
        }
    }

    void initServer(int placeCount, int philliCount, boolean firstInit) throws Exception {
        this.placeCount = placeCount;
        this.philliCount = philliCount;
        if (firstInit) {
            philosophEatingCounters = new ArrayList<>();
            for (int index = 0; index < philliCount; index++) {
                philosophEatingCounters.add(0);
            }
        } else {
            int difference = philliCount - philosophEatingCounters.size();
            if (difference >= 0) {
                for (int index = 0; index < difference; index++) {
                    philosophEatingCounters.add(0);
                }
            } else {
                int size = philosophEatingCounters.size();
                for (int index = -1; index >= difference; index--) {
                    philosophEatingCounters.remove(index % size);
                }
            }
        }

        startClients();

    }

    public void startClients() {
        // todo methodenname ändern

        //Supervisor auslesen
        if (mainSupervisor != null) {
            int supervisorCounterSize = mainSupervisor.getEatingCounters().size();
            for (int index = 0; index < supervisorCounterSize; index++) {
                if (index < philosophEatingCounters.size()) {
                    philosophEatingCounters.set(index, mainSupervisor.getEatingCounters().get(index));

                }
            }
            //Clients stoppen, initClients TODO
            if (mainSupervisor.isAlive()) {

                mainSupervisor.stopMainSupervisor();
            }

        }
        initClients();

    }

    private void stopClients() throws RemoteException {
        for (Client client : clients) {
            client.stopClient();
        }
    }

    void initClients() {
        System.out.println(clientIpAdresses);
        System.out.println("EatingCounters: " + this.philosophEatingCounters);
        try {

            for (String ipAdress : clientIpAdresses) {
                System.out.println("Adding " + ipAdress);
                Client client = null;
                try {
                    client = (Client) Naming.lookup("rmi://" + ipAdress + "/client");
                } catch (RemoteException e) {
                    System.out.println("wie erwartet");
                    e.printStackTrace();
                    clientIpAdresses.remove(ipAdress);
                    clients.clear();
                    initClients();
                    return;
                } catch (NotBoundException ex) {
                    ex.printStackTrace();
                    System.out.println("nicht erwartet");
                } catch (MalformedURLException ex) {
                    ex.printStackTrace();
                    System.out.println("nicht erwartet 2");
                }

                if (client != null) {
                    try {
                        clients.add(client);
                        client.setClients(clientIpAdresses);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        clientIpAdresses.remove(ipAdress);
                        clients.clear();
                        initClients();
                        return;
                    }

                }
            }

            stopClients();

            // todo verteilung über die clients starten
            int index = 0;
            for (Client client : clients) {

                // Schleife ändern, wenn philosophen eingeführt werden TODO
                client.init(philosophEatingCounters.get(index), 0);
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
        System.out.println("MainSupervisor Started");
        mainSupervisor = new MainSupervisor(clients, this);
        mainSupervisor.start();
    }

    @Override
    public void run() {

    }

    public void addClient(String ipAdress) {
        this.clientIpAdresses.add(ipAdress);
    }

    @Override
    public void reportError() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
