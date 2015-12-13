package vss.a4.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Andreas Buchmann
 * @author Tim Böhnel
 */
public interface Server extends Remote {
    public void addClient(String ipAdress) throws RemoteException;
    public void initServer(int i, int j) throws Exception;
    public void reportError() throws RemoteException;
}
