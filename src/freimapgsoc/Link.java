/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package freimapgsoc;

import java.io.Serializable;

/**
 *
 * @author stefanopilla
 */
public class Link implements Comparable, Serializable {

    public Link() {
    }

    public Link(MapNode source, MapNode dest) {
        this(source, dest, 1, 1, 1, false, "0000-00-00 00:00:00");
    }

    public Link(MapNode source, MapNode dest, float etx) {
        this(source, dest, 1, 1, etx, false, "0000-00-00 00:00:00");
    }

    public Link(MapNode source, MapNode dest, float etx, boolean HNA) {
        this(source, dest, 1, 1, etx, HNA, "0000-00-00 00:00:00");

    }

    public Link(MapNode source, MapNode dest, float lq, float nlq) {
        this(source, dest, lq, nlq, 0, false, "0000-00-00 00:00:00");
    }

    public Link(MapNode source, MapNode dest, float lq, float nlq, boolean HNA) {
        this(source, dest, lq, nlq, 0, false, "0000-00-00 00:00:00");
    }

    public Link(MapNode source, MapNode dest, float lq, float nlq, float etx, String timeStamp) {
        this(source, dest, lq, nlq, etx, false, timeStamp);
    }

    public Link(MapNode source, MapNode dest, float lq, float nlq, float etx) {
        this(source, dest, lq, nlq, etx, false, "0000-00-00 00:00:00");
    }

    public Link(MapNode source, MapNode dest, float lq, float nlq, float etx, boolean HNA, String timeStamp) {
        this.source = source;
        this.dest = dest;
        this.lq = lq;
        this.nlq = nlq;
        this.etx = etx;
        this.timeStamp = timeStamp;
        this.HNA = HNA;

    }

    public void setPacketCounts(long packets, long bytes, long icmp, long tcp, long udp, long other) {
        this.packets = packets;
        this.bytes = bytes;
        this.icmp = icmp;
        this.tcp = tcp;
        this.udp = udp;
        this.other = other;
    }

    public boolean equals(Object o) {
        if (!(o instanceof Link)) {
            return false;
        }
        Link other = (Link) o;
        if ((this.source == null) || (this.dest == null)) {
            return false;
        }
        return this.source.equals(other.source) && this.source.equals(other.dest);
    }

    public String toString() {
        return source + " -> " + dest;
    }
    
    public MapNode source;
    public MapNode dest;
    public boolean HNA;
    public String timeStamp;
    public float etx = -1, lq = -1, nlq = -1;
    public long bytes = 0, packets = 0, udp = 0, tcp = 0, icmp = 0, other = 0;

    @Override
    public int compareTo(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

