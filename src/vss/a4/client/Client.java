/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vss.a4.client;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 *
 * @author abuch_000
 */
public interface Client extends Remote {
    public void setClients(List<String> clientIpAdresses) throws RemoteException;
    public void hello(String callingIp) throws RemoteException;
}
