/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vss.a4.exceptions;

/**
 *
 * @author abuch_000
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
