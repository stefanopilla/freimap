/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package freimapgsoc;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

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
    }

    public boolean check_mysql() {
    try {
        InetAddress addr = InetAddress.getByName("localhost");
        int port = 3306;

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


     private String selectIpAddress() {
        try {
            Object[] poss = null;
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            Vector<InetAddress> v = new Vector<InetAddress>();
            Enumeration<InetAddress> inetAddresses;
            for (NetworkInterface netint : Collections.list(nets)) {
                inetAddresses = netint.getInetAddresses();
                for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                    if (inetAddress.isReachable(2000)) {
                        v.add(inetAddress);
                    }
                }
            }
            poss = new Object[v.size()];
            v.copyInto(poss);

            for (int i = 0; i < v.size(); i++) {
                String tmp = v.elementAt(i).toString().substring(1);
                poss[i] = tmp;
            }
            String s = (String) JOptionPane.showInputDialog(null, "Please select interface connected to OLSR Network: ", "Interface Selection", JOptionPane.PLAIN_MESSAGE, null, poss, poss[0]);
            if ((s != null) && (s.length() > 0)) {
                return s;
            }else if (s== null){

            }
        } catch (IOException ex) {
            Logger.getLogger(ServiceDiscovery.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "127.0.0.1";
    }

    public boolean check_olsr() {
       // Create a socket without a timeout with a dotDrawPlugin
    try {
        InetAddress addr = InetAddress.getByName("127.0.0.1");
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

    public String[] getActiveDataSource(){

        if(check_olsr()){
            ActiveDataSources[0]="OLSR";
        }
        if(check_mysql()){
            ActiveDataSources[1]="MySQL";
        }
        if(check_nameservice()){
            ActiveDataSources[2]="NameService";
        }

        return ActiveDataSources;
    }

    public String[] ActiveDataSources;
/*
    public static void main(String arg[]) {
        CheckActiveDataSource check = new CheckActiveDataSource();
        System.out.println(check.check_olsr());
       System.out.println(check.check_nameservice());
    }
*/
}

