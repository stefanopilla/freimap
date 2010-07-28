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
        this(ip, "noname"); //use "noname" as a name
        System.out.println("New MapNode Created from this(ip, noname)");
    }

    public MapNode(String ip, String name) {
        this.name = name;
        this.ip = ip;
        this.lat = DEFAULT_LAT;
        this.lon = DEFAULT_LON;
        this.uptime = "00:00:00";
        System.out.println("New MapNode Created from MapNode(ip, name)");

    }

    public MapNode(String id, double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
        this.uptime = "00:00:00";

    }

    public MapNode(String ip, String name, double lat, double lon) {
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.uptime = "00:00:00";
    }

    public MapNode(String ip, String name, String uptime) {
        this.name = name;
        this.ip = ip;
        this.uptime = uptime;
    }

     public MapNode(String ip, String name, double lat, double lon, Vector<String> ifaces, String uptime) {
        this.name = name;
        this.ip = ip;
        this.lat = lat;
        this.lon = lon;
        this.inter = ifaces;
        this.uptime = uptime;
    }

    public MapNode(String ip, String name, double lat, double lon, Vector<String> ifaces) {
        this.name = name;
        this.ip = ip;
        this.lat = lat;
        this.lon = lon;
        this.inter = ifaces;
        this.uptime = "00:00:00";
    }

    public MapNode(String ip, String name, String uptime, Connections conn) {
        this.name = name;
        this.ip = ip;
        this.uptime = uptime;
        this.conn = conn;
    }

    public MapNode(String ip, String name, String uptime, Connections conn, Vector<String> ifaces) {
        this.name = name;
        this.ip = ip;
        this.uptime = uptime;
        this.conn = conn;
        this.inter = ifaces;
    }

    public MapNode(String ip, String name, String uptime, Connections conn, Vector<String> ifaces, double lat, double lon) {
        this.name = name;
        this.ip = ip;
        this.uptime = uptime;
        this.conn = conn;
        this.inter = ifaces;
        this.lat = lat;
        this.lon = lon;

    }

    public MapNode(String ip, String name, String uptime, Connections conn, Vector<String> ifaces, double lon) {
        this.name = name;
        this.ip = ip;
        this.uptime = uptime;
        this.conn = conn;
        this.inter = ifaces;
        this.lat = DEFAULT_LAT;
        this.lon = DEFAULT_LON;

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

    public MapNode(String ip, String name, String uptime, Vector<String> ifaces, double lat, double lon, HashMap<String, Object> attributes) {
        this.name = name;
        this.ip = ip;
        this.uptime = uptime;
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
    public double DEFAULT_LAT = Double.NaN;
    public double DEFAULT_LON = Double.NaN;
    public double lat;
    public double lon;
    public HashMap<String, Object> attributes = new HashMap<String, Object>();
}
