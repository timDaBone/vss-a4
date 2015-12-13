package vss.a4.exceptions;

/**
 * @author Andreas Buchmann
 * @author Tim Böhnel
 */
public class VssException extends Exception {

    private String ipAdress;

    public VssException(String ipAdress, String message) {
        super(message);
        this.ipAdress = ipAdress;
    }

    public String getIpAdress() {
        return this.ipAdress;
    }

}
