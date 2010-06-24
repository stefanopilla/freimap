/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package freimapgsoc;

import freimapgsoc.ServiceDiscovery;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;
import javax.jmdns.ServiceTypeListener;

/**
 *
 * @author stefano
 */
public class SDMain {
static class SampleListener implements ServiceListener, ServiceTypeListener {
        public void serviceAdded(ServiceEvent event) {
            System.out.println("ADD: " + event.getDNS().getServiceInfo(event.getType(), event.getName(), 3*1000));
        }
        public void serviceRemoved(ServiceEvent event) {
            System.out.println("REMOVE: " + event.getName());
        }
        public void serviceResolved(ServiceEvent event) {
            System.out.println("RESOLVED: " + event.getInfo());
        }
        public void serviceTypeAdded(ServiceEvent event) {
            System.out.println("TYPE: " + event.getType());
        }
    }


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws UnknownHostException, IOException {
       int argc = args.length;
        boolean debug = false;
        String ip="127.0.0.1";
         InetAddress add=null;
         
        try {
            add = InetAddress.getByName("127.0.0.1");
        } catch (UnknownHostException ex) {
            Logger.getLogger(SDMain.class.getName()).log(Level.SEVERE, null, ex);
        }


        /*
        System.getProperties().put("jmdns.debug", "1");
        debug = true;

        if ((argc > 0) && "-d".equals(args[0])) {
            System.arraycopy(args, 1, args, 0, --argc);
            System.getProperties().put("jmdns.debug", "1");
            debug = true;
        }
        if ((argc > 1) && "-i".equals(args[0])) {
            System.out.println("GetByName: " +InetAddress.getByName("10.0.1.29"));
            intf = InetAddress.getByName("10.0.1.29");
            System.arraycopy(args, 2, args, 0, argc -= 2);
        }

        if (intf == null) {
            System.out.println("GetLocalhost(): "+ InetAddress.getLocalHost());
            intf = InetAddress.getLocalHost();
        }
*/
        
        new ServiceDiscovery(JmDNS.create(add)).setVisible(true);
/*
        if ((argc == 0) || ((argc >= 1) && "-browse".equals(args[0]))) {
            new ServiceDiscovery(jmdns);
            for (int i = 2 ; i < argc ; i++) {
                jmdns.registerServiceType(args[i]);
            }
        } else if ((argc == 1) && "-bt".equals(args[0])) {
            jmdns.addServiceTypeListener(new SampleListener());
        } else if ((argc == 3) && "-bs".equals(args[0])) {
            jmdns.addServiceListener(args[1] + "." + args[2], new SampleListener());
        } else if ((argc > 4) && "-rs".equals(args[0])) {
            String type = args[2] + "." + args[3];
            String name = args[1];
            Hashtable props = null;
            for (int i = 5 ; i < argc ; i++) {
                int j = args[i].indexOf('=');
                if (j < 0) {
                    throw new RuntimeException("not key=val: " + args[i]);
                }
                if (props == null) {
                    props = new Hashtable();
                }
                props.put(args[i].substring(0, j), args[i].substring(j+1));
            }
            jmdns.registerService(ServiceInfo.create(type, name, Integer.parseInt(args[4]), 0, 0, props));

            // This while loop keeps the main thread alive
            while (true) {
                try {
                    Thread.sleep(Integer.MAX_VALUE);
                } catch (InterruptedException e) {
                    break;
                }
            }
        } else if ((argc == 2) && "-f".equals(args[0])) {
            new Responder(jmdns, args[1]);
        } else if (!debug) {
            System.out.println();
            System.out.println("jmdns:");
            System.out.println("     -d                                       - output debugging info");
            System.out.println("     -i <addr>                                - specify the interface address");
            System.out.println("     -browse [<type>...]                      - GUI browser (default)");
            System.out.println("     -bt                                      - browse service types");
            System.out.println("     -bs <type> <domain>                      - browse services by type");
            System.out.println("     -rs <name> <type> <domain> <port> <txt>  - register service");
            System.out.println("     -f <file>                                - rendezvous responder");
            System.out.println();
            System.exit(1);
        }
    }*/
    }
}


