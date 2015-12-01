/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vss.a4.client;

import java.rmi.RemoteException;

/**
 *
 * @author tboeh
 */
public class Table {

    private int startPlace;
    private int lastPlace;

    public Table(int startPlace, int lastPlace) {
        for (int index = 0; index < VssA3.MAX_PLACES; index++) {
            Fork fork = new Fork(index);
            Place place = new Place(index);
            this.forks.add(fork);
            this.places.add(place);
        }
    }

    void passBackFork(int placeIndex) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    void leavePlace(int placeIndex) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    void takeFork(int placeIndex) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    Iterable<Place> getPlaces() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    Place getPlace(int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choosethrows RemoteException throws RemoteException  Tools | Templates.
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
