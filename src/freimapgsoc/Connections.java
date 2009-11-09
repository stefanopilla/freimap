/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package freimapgsoc;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Vector;

/**
 *
 * @author stefanopilla
 */
public class Connections {

    public Connections(InetAddress ip) {
        connection = check_connection(ip);
    }

    public Vector get_connections() {
        return connection;
    }

    public Vector check_connection(InetAddress addr) {
        try {
            addr.getByName("localhost");

            // This constructor will block until the connection succeeds
            Socket socket = new Socket(addr, 23);
            if (socket.isConnected()) {
                this.ssh = true;
                connection.add("ssh");
            } else if (!socket.isConnected()) {
                this.ssh = false;
            }

            socket = new Socket(addr, 22);
            if (socket.isConnected()) {
                this.telnet = true;
                connection.add("telnet");
            } else if (!socket.isConnected()) {
                this.telnet = false;

            }

            //if you want to add a check_connection add a code like this (before declare service global variable):
           // if (socket.isConnected()) {
           //     this.service = true;
           //     connection.add("NAME_OF_SERVICE");
           // } else if (!socket.isConnected()) {
            //    this.service = false;

            //}

        } catch (UnknownHostException e) {
            log.append("UnknowHost Exeption: "+ e.getMessage());
        } catch (IOException io) {
            log.append("IOException: "+io.getMessage());
        }

        return connection;
    }
    Vector<String> connection = new Vector<String>();
    public boolean telnet;
    public boolean ssh;
}
