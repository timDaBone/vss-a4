/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vss.a4.mainserver;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author abuch_000
 */
public interface Test extends Remote {
    
    public void sayHello() throws RemoteException;
    
}
