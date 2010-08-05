/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package freimapgsoc;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Vector;

/**
 *
 * @author stefanopilla
 */
public class MapNode implements Comparable, Serializable {

    public MapNode() {
    }

    public MapNode(String ip) {
        this(ip,  "noname",  "00:00:00", null, null,  41.86378, 12.55347, null);
        System.out.println("New MapNode Created from this(ip, noname)");
    }

    public MapNode(String ip, String name) {
        this(ip,  name,  "00:00:00", null, null, 41.86378, 12.55347,null);
        System.out.println("New MapNode Created from MapNode(ip, name)");

    }

    public MapNode(String ip, double lat, double lon) {
        this(ip,  "noname",  "00:00:00", null, null, lat, lon,null);


    }

    public MapNode(String ip, String name, double lat, double lon) {
        this(ip,  name,  "00:00:00", null, null, lat, lon,null);

    }

    public MapNode(String ip, String name, String uptime) {
        this(ip,  name,  uptime, null, null, 41.86378, 12.55347,null);

    }

     public MapNode(String ip, String name, String uptime, Vector<String> ifaces, double lat, double lon) {
         this(ip,  name,  uptime, null, ifaces, lat, lon, null);

    }

    public MapNode(String ip, String name,  Vector<String> ifaces, double lat, double lon) {
        this(ip,  name,  "00:00:00", null, ifaces, lat, lon, null);
    }

    public MapNode(String ip, String name, String uptime, Connections conn) {
        this(ip,  name,  uptime, conn, null, 41.86378, 12.55347, null);

    }

    public MapNode(String ip, String name, String uptime, Connections conn, Vector<String> ifaces) {
        this(ip,  name,  uptime, conn, ifaces, 41.86378, 12.55347, null);
    }

    public MapNode(String ip, String name, String uptime, Connections conn, Vector<String> ifaces, double lat, double lon) {
        this(ip,  name,  uptime, conn, ifaces, lat, lon, null);
    }

    public MapNode(String ip, String name, String uptime, Vector<String> ifaces, double lat, double lon, HashMap<String, Object> attributes) {
        this(ip,  name,  uptime, null, ifaces, lat, lon, attributes);
    }

    public MapNode(String ip, String name, String uptime, Connections conn, Vector<String> ifaces, double lat, double lon, HashMap<String, Object> attributes) {
        this.name = name;
        this.ip = ip;
        this.uptime = uptime;
        this.conn = conn;
        this.inter = ifaces;
        this.lat = lat;
        this.lon = lon;
        this.attributes = attributes;
    }

    MapNode eqo;

    public boolean equals(Object o) {
        if (!(o instanceof MapNode)) {
            return false;
        }
        eqo = (MapNode) o;
        if (this.name.equals(eqo.name)) {
            return true;
        }
        /*if (this.fqid != null) {
        if (this.fqid.equals(eqo.id)) return true;
        if (eqo.fqid != null) {
        if (this.fqid.equals(eqo.fqid)) return true;
        if (eqo.fqid.equals(this.id)) return true;
        }
        }*/
        return false;
    }

    public String toString() {
        return name;
    }

    public Vector<String> getInterfaces() {
        return this.inter;
    }

    public int compareTo(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    public String name;
    public String ip;
    public String uptime;
    public Connections conn;
    public Vector<String> inter = new Vector<String>();
    public Services services;
    public InetAddress addr;
    //Now in ROME
    public double DEFAULT_LAT=41.86378;
    public double DEFAULT_LON=12.55347;
    public double lat;
    public double lon;
    public HashMap<String, Object> attributes = new HashMap<String, Object>();
}
