/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vss.a4.server;

import java.net.MalformedURLException;
import java.rmi.AccessException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;
import vss.a4.mainserver.Test;

/**
 *
 * @author abuch_000
 */
public class VssA4Server {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
//            Registry registry = LocateRegistry.getRegistry("192.168.1.66");
//            
//            Test test = (Test) registry.lookup("Hello");
//            
//            test.sayHello();
            String codebase = "file:/C:\\Users\\abuch_000\\Documents\\NetBeansProjects\\vss-a4-server\\build\\classes";

            System.setProperty("java.rmi.server.codebase", codebase);
            
            Test server = (Test)Naming.lookup("rmi://192.168.1.66/Hello");
            server.sayHello();
        } catch (MalformedURLException ex) {
            Logger.getLogger(VssA4Server.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(VssA4Server.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NotBoundException ex) {
            Logger.getLogger(VssA4Server.class.getName()).log(Level.SEVERE, null, ex);
        }      
    }
    
}