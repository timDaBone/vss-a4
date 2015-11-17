/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.vss.a4.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author Tim
 */
public interface Test2 extends Remote {
    
    public void sayHello() throws RemoteException;
    
}