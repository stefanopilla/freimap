/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package freimapgsoc;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author stefanopilla
 */
public class Interfaces {

    public Interfaces(InetAddress addr){
        this.addr=addr;

    }

    public void get_interfaces(){
        try {
            Socket conn = new Socket(addr, 1978);
            //get info from HTTPINFOPLUGIN
        } catch (IOException ex) {
            log.append("IOException: " + ex.getMessage());
        }

    }

    public InetAddress addr;
    public boolean HNS=false;
    public boolean OLSR=false;
}
