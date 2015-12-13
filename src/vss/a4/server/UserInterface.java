package vss.a4.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.Naming;

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
            DistributionServer.logging("", ex);
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
                DistributionServer.logging("Enter new configuration (Places,Philosophs): ");
                String startConfig = reader.readLine();
                String[] startConfigParts = startConfig.split(",");
                int placeCount = Integer.parseInt(startConfigParts[0]);
                int philliCount = Integer.parseInt(startConfigParts[1]);
                server.initServer(placeCount, philliCount);
                firstInit = false;
            } catch (Exception e) {
                DistributionServer.logging("IOException in UserInterface", e);
            }
        }

    }

}
