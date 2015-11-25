package vss.a4.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import vss.a4.client.Client;
import vss.a4.client.DistributedClient;

/**
 *
 * @author abuch_000
 */
public class UserInterface extends Thread {

    private final Server server;
    private final BufferedReader reader;
    private boolean firstInit = true;

    public static void main(String[] args) {
        try {
            UserInterface ui = new UserInterface(args[0]);
            ui.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public UserInterface(String ipAdress) throws Exception {
        this.server = (Server) Naming.lookup("rmi://" + ipAdress + "/server");;
        this.reader = new BufferedReader(new InputStreamReader(System.in));
    }

    @Override
    public void run() {
        while (true) {
            try {
                System.out.println("Enter new configuration (Places,Philosophs): ");
                String startConfig = reader.readLine();
                String[] startConfigParts = startConfig.split(",");
                int placeCount = Integer.parseInt(startConfigParts[0]);
                int philliCount = Integer.parseInt(startConfigParts[1]);
                System.out.println("Connect Clients and press Enter if all Clients connected ... ");
                reader.readLine();
                server.initServer(placeCount, philliCount, firstInit);
                firstInit = false;
            } catch (Exception e) {
                DistributionServer.logging("IOException in UserInterface", e);
            }
        }

    }

}
