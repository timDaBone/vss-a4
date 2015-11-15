package vss.a4.mainserver;

import java.net.MalformedURLException;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author abuch_000
 */
public class VssA4Mainserver {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException, NotBoundException, MalformedURLException {
        try {

            //String codebase = "file:/C:\\Users\\abuch_000\\Documents\\NetBeansProjects\\vss-a4-mainserver\\build\\classes";

            //System.setProperty("java.rmi.server.codebase", codebase);

            Registry vRegistry = LocateRegistry.getRegistry(1099);

            TestImpl testImpl = new TestImpl();
            Test test = (Test) UnicastRemoteObject.exportObject(testImpl, 0);

            vRegistry.bind("Hello", test);
            
            Thread.sleep(10000);
            System.out.println("Ready!");
            Test server = (Test)Naming.lookup("rmi://192.168.1.58/Hello");
            server.sayHello();

        } catch (RemoteException ex) {
            ex.printStackTrace();
        } catch (AlreadyBoundException ex) {
            ex.printStackTrace();
        }
    }

}
