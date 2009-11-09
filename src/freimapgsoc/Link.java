/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package freimapgsoc;

/**
 *
 * @author stefanopilla
 */
public class Link {

    public Link() {
    }

    public Link(MapNode source, MapNode dest, float lq, float nlq, boolean HNA) {
        this.source = source;
        this.dest = dest;
        this.lq = lq;
        this.nlq = nlq;
        this.HNA = HNA;

    }

    public Link(MapNode source, MapNode dest, float etx) {
        this(source, dest, etx, false);
    }

    public Link(MapNode source, MapNode dest, float etx, boolean HNA) {
        this.source = source;
        this.dest = dest;
        this.etx = etx;
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
    public float etx = -1, lq = -1, nlq = -1;
    public long bytes = 0, packets = 0, udp = 0, tcp = 0, icmp = 0, other = 0;
}

