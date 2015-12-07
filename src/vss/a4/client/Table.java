/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vss.a4.client;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author tboeh
 */
public class Table {

    private int startPlace;
    private int lastPlace;
    List<Fork> forks;
    List<Place> places;

    public Table(int startPlace, int lastPlace) {
        this.startPlace = startPlace;
        this.lastPlace = lastPlace;
        this.forks = new ArrayList<>();
        this.places = new ArrayList<>();
        for (int index = 0; index <= lastPlace - startPlace; index++) {
            Fork fork = new Fork(index);
            Place place = new Place(index);
            this.forks.add(fork);
            this.places.add(place);
        }
    }

    void passBackFork(int placeIndex) {
        this.forks.get(placeIndex-startPlace).passBack();
    }

    void leavePlace(int placeIndex) {
        this.places.get(placeIndex-startPlace).leave();
    }

    void takeFork(int placeIndex) throws InterruptedException {
       this.forks.get(placeIndex-startPlace).take();
    }

    Iterable<Place> getPlaces() {
        return this.places;
    }

    Place getPlace(int placeIndex) {
        return this.places.get(placeIndex-startPlace);
    }

    int tryEnqueue() throws Exception {
        for (Place place : getPlaces()) {
            if (place.tryEnqueue()) {
                return place.getIndex();
            }
        }
        return -1;
    }

}
