/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vss.a4.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author abuch_000
 */
public interface Server extends Remote {

    public void addClient(String ipAdress) throws RemoteException;
    public void initServer(int i, int j, boolean firstInit) throws Exception;
    public void reportError() throws RemoteException;
}
