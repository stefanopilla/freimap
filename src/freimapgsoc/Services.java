/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package freimapgsoc;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceTypeListener;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;

/**
 *
 * @author stefanopilla
 */
public class Services {

    public Services(InetAddress addr, String ip) {
        try {
            this.jmdns = JmDNS.create(addr.getByName(ip));
            jmdns.addServiceTypeListener((ServiceTypeListener) this);
            System.out.println("HostName:" + jmdns.getHostName());
            System.out.println("Interface: " + jmdns.getInterface().toString());
        } catch (IOException ex) {
            log.append("IOException:" + ex.getMessage());
        }

        // register some well known types
        list = new String[]{
                    "_http._tcp.local.",
                    "_ftp._tcp.local.",
                    "_sftp._tcp.local.",
                    "_tftp._tcp.local.",
                    "_ssh._tcp.local.",
                    "_smb._tcp.local.",
                    "_printer._tcp.local.",
                    "_airport._tcp.local.",
                    "_afpovertcp._tcp.local.",
                    "_nfs._tcp.local.",
                    "_webdav._tcp.local.",
                    "_presence._tcp.local.",
                    "_eppc._tcp.local.",
                    "_telnet._tcp.local.",
                    "_raop._tcp.local.",
                    "_ipp._tcp.local.",
                    "_pdl-datastream._tcp.local.",
                    "_riousbprint._tcp.local.",
                    "_daap._tcp.local.",
                    "_distcc._tcp.local.",
                    "_xserveraid._tcp.local.",
                    "_net-assistant._tcp.local.",
                    "_workstation._tcp.local.",
                    "_h323._tcp.local.",
                    "_sip._udp.local."
                };

        for (int i = 0; i < list.length; i++) {
            jmdns.registerServiceType(list[i]);
        }
    }

    public Services(JmDNS jmdns) {
        try {
            this.jmdns = jmdns;
            jmdns.addServiceTypeListener((ServiceTypeListener) this);
            // register some well known types
            list = new String[]{"_http._tcp.local.", "_ftp._tcp.local.", "_sftp._tcp.local.", "_tftp._tcp.local.", "_ssh._tcp.local.", "_smb._tcp.local.", "_printer._tcp.local.", "_airport._tcp.local.", "_afpovertcp._tcp.local.", "_nfs._tcp.local.", "_webdav._tcp.local.", "_presence._tcp.local.", "_eppc._tcp.local.", "_telnet._tcp.local", "_raop._tcp.local.", "_ipp._tcp.local.", "_pdl-datastream._tcp.local.", "_riousbprint._tcp.local.", "_daap._tcp.local", "_distcc._tcp.local.", "_xserveraid._tcp.local.", "_net-assistant._tcp.local.", "_workstation._tcp.local.", "_h323._tcp.local.", "_sip._udp.local."};
            for (int i = 0; i < list.length; i++) {
                jmdns.registerServiceType(list[i]);
            }
        } catch (IOException ex) {
            log.append("IOException " + ex.getMessage());
        }
    }




    public HashMap<String, String> services = new HashMap<String, String>();
    private JmDNS jmdns;
    private String list[];
}
