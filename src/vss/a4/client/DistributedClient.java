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
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import vss.a4.exceptions.VssException;
import vss.a4.server.DistributionServer;
import vss.a4.server.Server;

/**
 * @author Andreas Buchmann
 * @author Tim Böhnel
 */
public class DistributedClient implements Client {

    public final static long PENALTY_TIME = 5000;
    public final static long THINKING_TIME = 100;
    public final static long SLEEPING_TIME = 100;
    public final static int MAXIMUM_EATING_DIFFERENCE_AVERAGE = 10;
    public final static long SLEEPING_TIME_SUPERVISOR = 100;
    public final static long EATING_TIME = 100;

    private final List<Client> clients;
    private final String clientIpAdress;
    private final Server server;
    private int firstPhilosoph;
    private int lastPhilosoph;
    private List<Integer> eatingCounters;
    private int placeCount;
    private int lastPlace;
    private int firstPlace;
    private Table table;
    private Supervisor supervisor;
    private boolean notAlreadyReported;
    private int stoppedPhilosophs;
    private boolean firstInit;
    private boolean shouldRun;

    public List<Client> getClients() {
        return clients;
    }
    List<Philosoph> philosophs;

    public DistributedClient(String serverIpAdress, String clientIpAdress, int registryPort, boolean DEBUG) throws Exception {
        DistributionServer.DEBUG = DEBUG;
        this.notAlreadyReported = true;
        this.clientIpAdress = clientIpAdress;
        this.clients = new ArrayList<>();
        this.philosophs = new ArrayList<>();
        this.stoppedPhilosophs = -1;
        this.firstInit = true;
        this.shouldRun = true;

        // Setup RMI to server
        DistributionServer.logging("Lookup rmi");
        this.server = (Server) Naming.lookup("rmi://" + serverIpAdress + "/server");

        // Initiate local RMI
        Registry registry = LocateRegistry.getRegistry(registryPort);

        String[] alreadyBindList = registry.list();
        DistributionServer.logging("RMI done");
        boolean bind = true;
        for (String alreadyBind : alreadyBindList) {
            System.out.println(alreadyBind);
            if (alreadyBind.equals("client")) {
                bind = false;
            }
        }

        Client client = (Client) UnicastRemoteObject.exportObject(this, 0);

        if (bind) {
            registry.bind("client", client);
        } else {
            registry.rebind("client", client);
        }
    }

    public static void main(String[] args) {
        try {
            DistributionServer.logging("Main started");
            DistributedClient distributedClient = new DistributedClient(args[0], args[1], Integer.parseInt(args[2]), Boolean.parseBoolean(args[3]));
            DistributionServer.logging("add client");
            distributedClient.server.addClient(distributedClient.clientIpAdress);
            DistributionServer.logging("Added client");
            System.out.println("Client connected to Server");
        } catch (Exception ex) {
            ex.printStackTrace();
            DistributionServer.logging("Client Registry Error or Server not available", ex);
        }
    }

    @Override
    public void setClients(Set<String> clientIpAdresses) throws VssException {
        DistributionServer.logging("Set clients");
        this.clients.clear();
        // Create RMI connection to all clients
        for (String ipAdress : clientIpAdresses) {
            if (!ipAdress.equals(this.clientIpAdress)) {
                DistributionServer.logging("Set client " + ipAdress);
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
        DistributionServer.logging("Set clients done");
    }

    @Override
    public void init(int firstPhilosoph, int lastPhilosoph, List<Integer> eatingCounters, int firstPlace, int lastPlace, int placeCount) throws Exception {
        DistributionServer.logging("Client init");
        if (!firstInit) {
            waitForPhilosophsStop();
        }
        DistributionServer.logging("All philos stopped at stopClient");
        this.notAlreadyReported = true;
        this.firstPhilosoph = firstPhilosoph;
        this.lastPhilosoph = lastPhilosoph;
        this.eatingCounters = eatingCounters;
        this.firstPlace = firstPlace;
        this.lastPlace = lastPlace;
        this.placeCount = placeCount;
        this.stoppedPhilosophs = -1;
        this.table = new Table(firstPlace, lastPlace);
        this.shouldRun = true;
        DistributionServer.logging(firstPhilosoph + " " + lastPhilosoph + " " + firstPlace + " " + this.lastPlace);
        DistributionServer.logging("Philosoph " + this + " initialized");
    }

    @Override
    public Map<Integer, Integer> getPhiloCount() {
        Map<Integer, Integer> philoCount = new HashMap<>();
        for (Philosoph philosoph : philosophs) {
            philoCount.put(philosoph.getIndex(), philosoph.getEatingCounter());
        }
        return philoCount;
    }

    @Override
    public void startClient() throws RemoteException {
        DistributionServer.logging("Client starts");
        synchronized (this) {
            this.philosophs.clear();
            // Start all philos
            for (int index = this.firstPhilosoph; index <= this.lastPhilosoph; index++) {
                Philosoph philosoph = new Philosoph(table, index, this.eatingCounters.get(index), this);
                this.philosophs.add(philosoph);
                philosoph.start();
            }
            this.supervisor = new Supervisor(philosophs);
            this.supervisor.start();
        }
        this.firstInit = false;
        DistributionServer.logging("Philosoph " + this + " started");
    }

    @Override
    public void stopClient() throws RemoteException {
        DistributionServer.logging("Client stops");
        this.table.releaseAll();
        this.shouldRun = false;
        this.supervisor.stopSupervisor();
        waitForPhilosophsStop();
    }

    private void waitForPhilosophsStop() {
        DistributionServer.logging("Wait for all philos to stop at stopClient");
        if (!firstInit) {
            while (stoppedPhilosophs < this.lastPhilosoph - this.firstPhilosoph) {
                DistributionServer.logging("Still waiting..." + stoppedPhilosophs);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Logger.getLogger(DistributedClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        DistributionServer.logging("All philos stopped at stopClient");
    }

    @Override
    public String toString() {
        return "Client (" + clientIpAdress + ")";
    }

    @Override
    public void takeFork(int forkIndex) throws Exception {
        // take local fork
        if (forkIndex >= this.firstPlace && forkIndex <= this.lastPlace) {
            this.table.takeFork(forkIndex);
        } else {
            // take remote fork
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
        // pass back local fork
        if (forkIndex >= this.firstPlace && forkIndex <= this.lastPlace) {
            this.table.passBackFork(forkIndex);
        } else {
            // pass back remote fork
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
        // leave local place
        if (placeIndex >= this.firstPlace && placeIndex <= this.lastPlace) {
            this.table.leavePlace(placeIndex);
            // leave remote place
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

    int getPlaceCount() {
        return this.placeCount;
    }

    int getStartPlace() {
        return this.firstPlace;
    }

    void reportError() throws RemoteException {
        if (notAlreadyReported) {
            this.server.reportError();
            notAlreadyReported = false;
        }
    }

    void stopped() {
        synchronized (this) {
            this.stoppedPhilosophs++;
        }
    }

    boolean shouldRun() {
        return this.shouldRun;
    }

    @Override
    public void punish() throws RemoteException {
        for (Philosoph philosoph : philosophs) {
            philosoph.punish();
        }
    }

}
