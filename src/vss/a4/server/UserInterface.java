package vss.a4.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 *
 * @author abuch_000
 */
public class UserInterface extends Thread {

    private final DistributionServer server;
    private final BufferedReader reader;
    private boolean firstInit = true;

    public UserInterface(DistributionServer server) {
        this.server = server;
        this.reader = new BufferedReader(new InputStreamReader(System.in));
    }

    @Override
    public void run() {
        try {
            while (true) {
                System.out.println("Enter new configuration (Places,Philosophs): ");
                String startConfig = reader.readLine();
                String[] startConfigParts = startConfig.split(",");
                int placeCount = Integer.parseInt(startConfigParts[0]);
                int philliCount = Integer.parseInt(startConfigParts[1]);
                
                System.out.println("Connect Clients and press Enter if all Clients connected ... ");
                reader.readLine();
                server.initServer(placeCount, philliCount, firstInit);
                firstInit = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
