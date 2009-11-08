/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package freimapgsoc;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ResultSet;
import com.mysql.jdbc.Statement;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.sql.DriverManager;

/**
 *
 * @author stefanopilla
 */
public class CheckActiveDataSource {

    ServerSocket serverSocket;
    public boolean mySql = false;
    public boolean olsr = false;
    public boolean nameservice = false;

    public CheckActiveDataSource() {
        mySql = check_mysql();
        olsr = check_olsr();
        nameservice = check_nameservice();

    }

    public boolean check_mysql() {


        return true;
    }

    public boolean check_olsr() {
      
       // Create a socket without a timeout
    try {
        InetAddress addr = InetAddress.getByName("localhost");
        int port = 2004;

        // This constructor will block until the connection succeeds
        Socket socket = new Socket(addr, port);
        if (socket.isConnected()){
            return true;
            }else if (!socket.isConnected()){
             return false;
            }

    } catch (UnknownHostException e) {
    } catch (IOException io){

    }
        return false;
    }

    public boolean check_nameservice() {
        File latlon=new File("/var/run/latlon.js");
        if(latlon.exists()){
            return true; 
        }
        return false;
    }

    public static void main(String arg[]) {
        CheckActiveDataSource check = new CheckActiveDataSource();
        System.out.println(check.check_olsr());
       System.out.println(check.check_nameservice());

    }
}

