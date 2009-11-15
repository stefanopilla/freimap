/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package freimapgsoc;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author stefanopilla
 */
public class Interfaces {

    public Interfaces(String ip, String name) {
        this.ip = ip;
        this.name = name;
    }

     public Interfaces(String ip) {
        this.ip = ip;
    }

    public void add_interface(String ip) {
            interfaces.addElement(ip);
    }

    public void newInterface(String ip){
        interfaces.setElementAt(ip, 0);
    }

    public Vector<String> get_interfaces(MapNode node) {
        return this.interfaces;
    }

    public void get_interfaces() {
        try {
            Socket conn = new Socket(addr, 1978);
            //get info from HTTPINFOPLUGIN
        } catch (IOException ex) {
            log.append("IOException: " + ex.getMessage());
        }

    }
    public String ip;
    public String name;
    public InetAddress addr;
    public boolean HNA = false;
    public boolean OLSR = false;
    Vector<String> interfaces;
}
