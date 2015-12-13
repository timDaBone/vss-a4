package vss.a4.client;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import vss.a4.exceptions.VssException;

/**
 * @author Andreas Buchmann
 * @author Tim Böhnel
 */
public interface Client extends Remote {

    public void setClients(Set<String> clientIpAdresses) throws VssException, RemoteException;

    public void init(int firstPhilosoph, int lastPhilosoph, List<Integer> eatingCounters, int firstPlace, int lastPlace, int placeCount) throws Exception;

    public void startClient() throws RemoteException;

    public void stopClient() throws RemoteException;

    public Map<Integer, Integer> getPhiloCount() throws Exception;

    public int tryEnqueue() throws Exception;

    public void takeFork(int index) throws Exception;

    public void passBackFork(int index) throws Exception;

    public void leavePlace(int index) throws Exception;

    public int getFirstPlace() throws RemoteException;

    public int getLastPlace() throws RemoteException;

    public void punish() throws RemoteException;
}
