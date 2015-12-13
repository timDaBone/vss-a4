package vss.a4.client;

import java.util.ArrayList;
import java.util.List;
import vss.a4.server.DistributionServer;

/**
 * 
 * @author Andreas Buchmann
 * @author Tim Böhnel
 */
public class Table {

    private final int startPlace;
    private final int lastPlace;
    List<Fork> forks;
    List<Place> places;

    public Table(int startPlace, int lastPlace) {
        this.startPlace = startPlace;
        this.lastPlace = lastPlace;
        this.forks = new ArrayList<>();
        this.places = new ArrayList<>();
        // Create all places and forks
        for (int index = startPlace; index <= lastPlace; index++) {
            Fork fork = new Fork();
            Place place = new Place(index);
            this.forks.add(fork);
            this.places.add(place);
        }
    }

    void passBackFork(int placeIndex) {
        this.forks.get(placeIndex - startPlace).passBack();
    }

    void leavePlace(int placeIndex) {
        this.places.get(placeIndex - startPlace).leave();
    }

    void takeFork(int placeIndex) throws InterruptedException {
        this.forks.get(placeIndex - startPlace).take();
    }

    Iterable<Place> getPlaces() {
        return this.places;
    }

    Place getPlace(int placeIndex) {
        return this.places.get(placeIndex - startPlace);
    }

    int tryEnqueue() throws Exception {
        for (Place place : getPlaces()) {
            if (place.tryEnqueue()) {
                return place.getIndex();
            }
        }
        return -1;
    }

    public void releaseAll() {
        DistributionServer.logging("Release all");
        for (Place place : this.places) {
            place.leave();
        }
        for (Fork fork : this.forks) {
            fork.passBack();
        }
        DistributionServer.logging("All released");
    }

}
