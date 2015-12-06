package vss.a4.server;

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
    private int philosophCount;
    private int placeCount;
    private MainSupervisor mainSupervisor;
    List<Integer> philosophEatingCounters;

    public DistributionServer(boolean debug) throws Exception {
        this.clientIpAdresses = new ArrayList<>();
        this.clients = new ArrayList<>();
        this.mainSupervisor = null;
        this.DEBUG = debug;
        DistributionServer.logging("DEBUGMODUS", null);

        // Get registry and bind server
        Registry registry = LocateRegistry.getRegistry(1099);
        Server server = (Server) UnicastRemoteObject.exportObject(this, 0);
        registry.bind("server", server);
    }

    public static void main(String[] args) {
        try {
            DistributionServer server = new DistributionServer(Boolean.parseBoolean(args[0]));
            //UserInterface userInterface = new UserInterface(server);
            //userInterface.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void initServer(int placeCount, int philliCount, boolean firstInit) {
        this.placeCount = placeCount;
        this.philosophCount = philliCount;
        initClients();
    }

    private void stopClients() throws RemoteException {
        DistributionServer.logging("stopClients()");
        for (Client client : clients) {
            client.stopClient();
        }
    }

    void initClients() {

        try {

            this.clients.clear();
            stopClients();

            if (mainSupervisor != null) {
                mainSupervisor.stopMainSupervisor();
            }
            DistributionServer.logging("initClients()");
            DistributionServer.logging("Client IP-List " + clientIpAdresses);
            DistributionServer.logging("Client Listsize " + clients.size());
            DistributionServer.logging("EatingCounters on Server " + this.philosophEatingCounters);

            for (String ipAdress : clientIpAdresses) {
                DistributionServer.logging("Adding IP " + ipAdress, null);
                Client client = null;
                try {
                    client = (Client) Naming.lookup("rmi://" + ipAdress + "/client");
                } catch (RemoteException ex) {
                    removeFromIpAdressList(ex, ipAdress);
                    initClients();
                    return;
                } catch (NotBoundException ex) {
                    removeFromIpAdressList(ex, ipAdress);
                    initClients();
                    return;
                } catch (Exception ex) {
                    DistributionServer.logging("Naming.lookup to Client " + ipAdress + " throws Exception", ex);
                }

                DistributionServer.logging("Working on ipAdress " + ipAdress, null);
                //DistributionServer.logging("Working on ipAdress " + client, null);
                if (client != null) {
                    try {
                        clients.add(client);
                        client.setClients(clientIpAdresses);
                    } catch (RemoteException ex) {
                        DistributionServer.logging("Client" + ipAdress + " is not available anymore.");
                        removeFromIpAdressList(ex, ipAdress);
                        initClients();
                        return;
                    } catch (VssException ex) {
                        DistributionServer.logging("Client" + ex.getIpAdress() + " is not available anymore.");
                        removeFromIpAdressList(ex, ex.getIpAdress());
                        initClients();
                        return;
                    }
                }
            }

            if (mainSupervisor == null) {
                mainSupervisor = new MainSupervisor(this, new ArrayList<>(), philosophCount);
            } else {
                mainSupervisor = new MainSupervisor(this, mainSupervisor.getEatingCounters(), philosophCount);

            }
            
            try {
                //stopClients();

                List<int[]> philosophsAndPlacesList = calculateDistribution();

                // todo verteilung über die clients starten
                int clientNumber = 0;
                for (Client client : clients) {
                    int[] philosophsAndPlaces = philosophsAndPlacesList.get(clientNumber);
                    // todo EATINGCOUNTERS
                    System.out.println(philosophsAndPlaces[0] + " " + philosophsAndPlaces[1] + " " + philosophsAndPlaces[2]+ " " + philosophsAndPlaces[3]);
                    client.init(philosophsAndPlaces[0], philosophsAndPlaces[1], mainSupervisor.getEatingCounters(), philosophsAndPlaces[2], philosophsAndPlaces[3], placeCount);
                    clientNumber++;
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
            mainSupervisor.start();
        } catch (RemoteException ex) {
            Logger.getLogger(DistributionServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void removeFromIpAdressList(Exception ex, String ipAdress) {
        DistributionServer.logging("cleanIpsAndClients()", ex);
        DistributionServer.logging("cleanIpsAndClients() removing " + ipAdress + " from IP-List", null);
        clientIpAdresses.remove(ipAdress);
    }

    public void addClient(String ipAdress) {
        DistributionServer.logging("adding client " + ipAdress, null);
        this.clientIpAdresses.add(ipAdress);
        initClients();
    }

    public static void logging(String message) {
        System.out.println(message +  System.currentTimeMillis());
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

    private List<int[]> calculateDistribution() {

        int numberOfClients = clients.size();

        List<int[]> philosophsAndPlacesList = new ArrayList<>();
        int[] philosophsAndPlaces = new int[4];

        if (numberOfClients != 0) {
            if (numberOfClients > 1) {

                int leftOverPhilosopsToDistibute = philosophCount % numberOfClients;
                int assuredPhilosophsForEachClient = philosophCount / numberOfClients;

                int leftOverPlacesToDistribute = placeCount % numberOfClients;
                int assuredPlacesForEachClient = placeCount / numberOfClients;

                int startIndexForPlaces = 0;
                int startIndexForPhilosops = 0;

                for (int clientNumber = 0; clientNumber < numberOfClients; clientNumber++) {
                    if (clientNumber < philosophCount) {
                        philosophsAndPlaces[0] = startIndexForPhilosops;
                        philosophsAndPlaces[1] = startIndexForPhilosops + assuredPhilosophsForEachClient - 1;
                    } else {
                        philosophsAndPlaces[0] = -1;
                        philosophsAndPlaces[1] = -1;
                    }

                    if (leftOverPhilosopsToDistibute > 0) {
                        philosophsAndPlaces[1] = philosophsAndPlaces[1] + 1;
                    }

                    if (clientNumber < placeCount) {
                        philosophsAndPlaces[2] = startIndexForPlaces;
                        philosophsAndPlaces[3] = startIndexForPlaces + assuredPlacesForEachClient - 1;
                    } else {
                        philosophsAndPlaces[2] = -1;
                        philosophsAndPlaces[3] = -1;
                    }

                    if (leftOverPlacesToDistribute > 0) {
                        philosophsAndPlaces[3] = philosophsAndPlaces[3] + 1;
                    }

                    leftOverPhilosopsToDistibute -= 1;
                    leftOverPlacesToDistribute -= 1;

                    startIndexForPhilosops = philosophsAndPlaces[1] + 1;
                    startIndexForPlaces = philosophsAndPlaces[3] + 1;

                    philosophsAndPlacesList.add(philosophsAndPlaces);
                    philosophsAndPlaces = new int[4];
                }
            } else {
                philosophsAndPlaces[0] = 0;
                philosophsAndPlaces[1] = philosophCount - 1;
                philosophsAndPlaces[2] = 0;
                philosophsAndPlaces[3] = placeCount - 1;
                philosophsAndPlacesList.add(philosophsAndPlaces);
            }
        } else {
            philosophsAndPlaces[0] = -1;
            philosophsAndPlaces[1] = -1;
            philosophsAndPlaces[2] = -1;
            philosophsAndPlaces[3] = -1;
            philosophsAndPlacesList.add(philosophsAndPlaces);
        }
        return philosophsAndPlacesList;
    }

}
