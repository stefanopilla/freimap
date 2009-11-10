/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package freimapgsoc;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.HashMap;

/**
 *
 * @author stefanopilla
 */
public class MapNode implements Comparable, Serializable {

    public MapNode() {
    }

    public MapNode(String id) {
        this(id, id); //use id as a name
    }

    public MapNode(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public MapNode(String id, String name, String ip) {
        this.id = id;
        this.name = name;
        this.ip = ip;
    }

    public MapNode(String id, double lat, double lon) {
        this.id = id;
        this.lat=lat;
        this.lon=lon;
    }

    public MapNode(String id, String name, double lat, double lon) {
        this.id = id;
        this.name = name;
        this.lat=lat;
        this.lon=lon;
    }

    public MapNode(String id, String name, String ip, double uptime) {
        this.id = id;
        this.name = name;
        this.ip = ip;
        this.uptime = uptime;
    }

    public MapNode(String id, String name, String ip, double uptime, Connections conn) {
        this.id = id;
        this.name = name;
        this.ip = ip;
        this.uptime = uptime;
        this.conn = conn;
    }

    public MapNode(String id, String name, String ip, double uptime, Connections conn, Interfaces ifaces) {
        this.id = id;
        this.name = name;
        this.ip = ip;
        this.uptime = uptime;
        this.conn = conn;
        this.inter = ifaces;
    }

    public MapNode(String id, String name, String ip, double uptime, Connections conn, Interfaces ifaces, double lat, double lon) {
        this.id = id;
        this.name = name;
        this.ip = ip;
        this.uptime = uptime;
        this.conn = conn;
        this.inter = ifaces;
        this.lat = lat;
        this.lon = lon;

    }

    public MapNode(String id, String name, String ip, double uptime, Connections conn, Interfaces ifaces, double lon) {
        this.id = id;
        this.name = name;
        this.ip = ip;
        this.uptime = uptime;
        this.conn = conn;
        this.inter = ifaces;
        this.lat = DEFAULT_LAT;
        this.lon = DEFAULT_LON;

    }

    public MapNode(String id, String name, String ip, double uptime, Connections conn, Interfaces ifaces, double lat, double lon, HashMap<String, Object> attributes) {
        this.id = id;
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
        if (this.id.equals(eqo.id)) {
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
        return id;
    }

    public int compareTo(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    
    public String id;
    public String name;
    public String ip;
    public double uptime;
    public Connections conn;
    public Interfaces inter;
    public Services services;
    public InetAddress addr;
    public double DEFAULT_LAT = Double.NaN;
    public double DEFAULT_LON = Double.NaN;
    public double lat;
    public double lon;
    public HashMap<String, Object> attributes = new HashMap<String, Object>();


}
