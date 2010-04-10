/* net.relet.freimap.MysqlDataSource.java

This file is part of the freimap software available at freimap.berlios.de

This software is copyright (c)2007 Thomas Hirsch <thomas hirsch gmail com>

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License with
the Debian GNU/Linux distribution in file /doc/gpl.txt
if not, write to the Free Software Foundation, Inc., 59 Temple Place,
Suite 330, Boston, MA 02111-1307 USA
 */
package freimapgsoc;

import java.util.*;
import java.sql.*;

public class MysqlDataSource implements DataSource {

    private Vector<MapNode> nodeList = new Vector<MapNode>();
    private HashMap<String, MapNode> nodeByName = new HashMap<String, MapNode>(); //fixme: not effectiv
    private HashMap<String, MapNode> nodeByIp = new HashMap<String, MapNode>();
    private LinkedList<Long> updateTimes = new LinkedList<Long>();
    private Vector<Link> linkList = new Vector<Link>();
    private HashMap<MapNode, Link> linkBySrc = new HashMap<MapNode, Link>();
    private HashMap<MapNode, Link> linkByDest = new HashMap<MapNode, Link>();
    private HashMap<String, String> interfaces = new HashMap<String, String>();
    private Vector<String> iface = new Vector<String>();
    private HashMap<String, Object> attributes = new HashMap<String, Object>();
    boolean updateClosest = true;
    private HashMap<String, Float> availmap = null;
    private Connection conn, conn2, conn3;
    private Statement stmt;
    private ResultSet rss;
    private long firstUpdateTime = 1,
            lastUpdateTime = 1,
            firstAvailableTime = 1,
            lastAvailableTime = 1;
    private DataSourceListener listener = null;
    private String host, user, pass, db, port;
    private String TABLE_LINKS;
    private String TABLE_NODES;
    private boolean nodeDataOnly = false;
    DataSource nodeSource;
    String sNodeSource;
    HashMap<Vector<MapNode>, Vector<Link>> config = new HashMap<Vector<MapNode>, Vector<Link>>();

    public MysqlDataSource() {
        this(null, null, null, null, null, false);
    }

    MysqlDataSource(String host, String user, String pass) {
    }

    public MysqlDataSource(String host, String user, String pass, String db, String port, boolean nodeDataOnly) {
        this.host = host;
        this.user = user;
        this.pass = pass;
        this.db = db;
        this.port = port;
        this.nodeDataOnly = nodeDataOnly;
    }

    public void parseNode() {
        try {

            //Leggi i nodi e mettili nel vettore di nodi
            if (!conn.isClosed()) {
                stmt = (Statement) conn.createStatement();//create Statmente fror conn connection
                rss = stmt.executeQuery("SELECT * FROM nodes"); //Excute Query
                rss.beforeFirst();
                while (rss.next()) { 
                String nodeIp = rss.getString("ip"); //prendo l'ip della prima riga
                System.out.println(nodeIp);
                Statement stmt2 = conn.createStatement();
                ResultSet rss2 = stmt2.executeQuery("SELECT * FROM interfaces WHERE mainIp=" + "\"" + nodeIp + "\"");
                System.out.println("SELECT * FROM interfaces WHERE mainip=" + "\"" + nodeIp + "\"");
                rss2.beforeFirst();
                //tiro fuori tutte le interfacce di quel nodo...!
                while (rss2.next()) {
                    iface.removeAllElements();
                    iface.add(rss2.getString("intip"));
                    interfaces.put(rss2.getString("mainip"), rss2.getString("intip"));
                }
                    if (rss.getBoolean("isGateway") == true) {
                        attributes.put("Gateway", "SELF");
                    } else if (rss.getBoolean("isGateway") == false) {
                        attributes.put("Gateway", "Other:" + rss.getString("gatewayIp"));
                    }
                    MapNode node = new MapNode(rss.getString("ip"), rss.getString("name"), rss.getString("uptime"), iface, rss.getDouble("lat"), rss.getDouble("lon"), attributes);
                    nodeList.add(node);
                    nodeByName.put(rss.getString("name"), node);
                    nodeByIp.put(rss.getString("ip"), node);
                }

            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void parseLinks() {
        try {
            //Leggi i links e mettili nel vettore di link
            rss = stmt.executeQuery("SELECT * FROM links");
            rss.beforeFirst();
            while (rss.next()) {
                MapNode src = nodeByIp.get(rss.getString("src"));
                MapNode dest = nodeByIp.get(rss.getString("dest"));
                Link link = new Link(src, dest, rss.getFloat("lq"), rss.getFloat("nlq"), rss.getFloat("etx"));
                linkList.add(link);
                linkBySrc.put(src, link);
                linkByDest.put(dest, link);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    //You don't really need this method because this operation is made in parseNode method!
    public void parseInterfaces() {
        try {
            //LEggi le interfacce e mettili nel vettore delle interfacce
            rss = stmt.executeQuery("SELECT * FROM interfaces");
            rss.beforeFirst();
            while (rss.next()) {
                interfaces.put(rss.getString("mainIp"), rss.getString("Intip"));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
	 * 
	 * @return 
	 */
	@Override
    public void init() {
        try {
            System.out.println("Fetching data from Database: " + db);
            System.out.println("This may take a while ... ");
            Class.forName("com.mysql.jdbc.Driver");
            conn = (Connection) DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + db, user, pass);
            if (!conn.isClosed()) {
                if (nodeDataOnly == false) {
                    parseNode();
                    parseLinks();
                    parseInterfaces();
                } else {
                    parseNode();
                    parseInterfaces();
                }
            } else {
                System.out.println("I've lost connection with Mysql Database!");
                System.out.println("I'm trying to go in realtime mode...");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        config.put(nodeList, linkList);
        Layer l = new Layer(config, this);
    }

    private void updateNodeList() throws SQLException {
    }

    public Vector<MapNode> getNodeList() {
        return nodeList;

    }

    public MapNode getNodeByName(String name) {
        return nodeByName.get(name);

    }

    public void addNode(MapNode node) {
        nodeList.remove(node); //just in case
        nodeList.add(node);
        nodeByName.put(node.name, node);

    }

    public HashMap<String, Float> getNodeAvailability(long time) {
        //time is ignored in this implementation
        if (availmap == null) {
            try {
                availmap = new HashMap<String, Float>();
                Statement s = conn.createStatement();
                ResultSet r = s.executeQuery("SELECT * from avail_nodes");

                while (r.next()) {
                    String id = r.getString("node");

                    float avail = r.getFloat("avail");
                    availmap.put(id, new Float(avail));

                }
            } catch (Exception ex) {
                System.err.println("Availability table not found or broken. Ignoring.");
                //ex.printStackTrace();
                availmap = null;

            }
        }
        return availmap;

    }

    public long getLastUpdateTime() {
        long newLastUpdateTime = -1;

        try {
            Statement s = conn.createStatement();
            ResultSet r = s.executeQuery("SELECT unix_timestamp(max(clock)) as last from " + TABLE_LINKS + " WHERE clock>from_unixtime(" + lastUpdateTime + ")");

            if (r.next()) {
                newLastUpdateTime = r.getLong("last");

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (newLastUpdateTime > lastUpdateTime) {
            lastUpdateTime = newLastUpdateTime;
        }
        return lastUpdateTime;

    }

    public long getLastAvailableTime() {
        return lastAvailableTime;

    }

    public long getFirstAvailableTime() {
        return firstAvailableTime;

    }

    public void setAvailableTime(long first, long last) {
        if ((lastAvailableTime == 1) || (last > lastAvailableTime)) {
            lastAvailableTime = last;

        }
        if ((firstAvailableTime == 1) || (first < firstAvailableTime)) {
            firstAvailableTime = first;

        }
        if (listener != null) {
            listener.timeRangeAvailable(first, last);

        }
    }

    public void addDataSourceListener(DataSourceListener dsl) {
        this.listener = dsl; //todo: allow multiple listeners

    }

    private void fetchAvailableTimeStamps() {
        new TimeStampFetcher();

        try {
            Thread.sleep(100); //sleep to allow a few time stamps to load. this is a hack, indeed.

        } catch (InterruptedException ex) {
        }
    }

    public long getClosestUpdateTime(long time) {
        long closest = -1;

        int tries = 0;

        while (true) {
            try {
                ListIterator<Long> li = updateTimes.listIterator(0);
                while (li.hasNext()) {
                    long stamp = (li.next()).longValue();

                    if (time > stamp) {
                        closest = stamp;

                    } else {
                        break;
                    }
                }
                return closest;

            } catch (ConcurrentModificationException ex) {
                //ok, we will have to try again.
                tries++;

                if (tries == 10) {
                    System.err.println("Too many concurrent modifications in \"MysqlDataSource.getClosestUpdateTime\". Strange things may happen.");

                    return firstUpdateTime;

                }
            }
        }
    }

    public long getFirstUpdateTime() {
        if (firstUpdateTime == 1) {
            try {
                Statement s = conn.createStatement();
                ResultSet r = s.executeQuery("SELECT unix_timestamp(min(clock)) as last from " + TABLE_LINKS);

                if (r.next()) {
                    firstUpdateTime = r.getLong("last");

                }
            } catch (Exception ex) {
                ex.printStackTrace();

            }
        }
        if (firstAvailableTime == 1) {
            firstAvailableTime = firstUpdateTime;

        }
        if (lastAvailableTime == 1) {
            lastAvailableTime = firstUpdateTime;

        }
        return firstUpdateTime;

    }

    public Vector<Link> getLinks(long time) {
        linkList = new Vector<Link>();

        if ((time <= 0) /* || (time>MAX_UNIX_TIME)*/) {
            return linkList; //empty

        }
        try {
            Statement s = conn.createStatement();
            ResultSet r = s.executeQuery("SELECT HIGH_PRIORITY * from " + TABLE_LINKS + " where clock=from_unixtime(" + time + ")");

            while (r.next()) {
                String src = r.getString("src");
                String dest = r.getString("dest");

                float q = r.getFloat("quality");
                //float  q    = r.getFloat("quality");
                Object srcn = nodeByName.get(src);
                Object dstn = nodeByName.get(dest);

                if (srcn == null) { //enable real-time interpolation
                    srcn = new MapNode(src);
                    nodeByName.put(src, (MapNode) srcn);

                    if (listener != null) {
                        listener.nodeListUpdate((MapNode) srcn);
                    }
                }
                if (dstn == null) {
                    dstn = new MapNode(dest);
                    nodeByName.put(dest, (MapNode) dstn);
                    if (listener != null) {
                        listener.nodeListUpdate((MapNode) dstn);
                    }
                }
                if ((srcn != null) && (dstn != null)) {
                    Link link = new Link((MapNode) srcn, (MapNode) dstn, q);
                    linkList.remove(link);
                    linkList.add(link);

                }
            }
        } catch (Exception ex) {
            System.out.println("clock = " + time);
            ex.printStackTrace();

        }
        return linkList;

    }

    public void getLinkCountProfile(MapNode node, NodeInfo nodeinfo) {
        new LCPFetcher(node, nodeinfo).start();

    }

    public void getLinkProfile(Link link, LinkInfo linkinfo) {
        new LPFetcher(link, linkinfo).start();
    }

    public void init(String path) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
	 * 
	 * @param ip
	 * @return 
	 */
	@Override
    public MapNode getNodeById(String ip) {
        return nodeByIp.get(ip);
    }

    /**
	 * 
	 * @param id
	 * @return 
	 */
	@Override
    public Vector<Link> getLinksFromSource(String id) {
        Vector<Link> linkFromSrc = new Vector<Link>();
        for (int i = 0; i < linkBySrc.size(); i++) {
            linkFromSrc.add(linkBySrc.get(i));
        }
        return linkFromSrc;
    }

    /**
	 * 
	 * @param id
	 * @return 
	 */
	@Override
	@SuppressWarnings("element-type-mismatch")
    public Vector<Link> getLinksFromDest(String id) {
        Vector<Link> linkFromDest = new Vector<Link>();
        for (int i = 0; i < linkByDest.size(); i++) {
            linkFromDest.add(linkByDest.get(i));
        }
        return linkFromDest;
    }

    /**
	 * 
	 * @param ip
	 * @return 
	 */
	@Override
    public MapNode getNodeByIp(String ip) {
        return nodeByIp.get(ip);
    }

    /**
	 * 
	 * @param configuration
	 * @return 
	 */
	@Override
    public HashMap<String, Object> read_conf(HashMap<String, Object> configuration) {
        throw new UnsupportedOperationException("Not supported yet.");

    }

    /**
	 * 
	 * @return 
	 */
	@Override
    public Vector<Link> getLinks() {
        return linkList;
    }

    class LCPFetcher extends Thread {

        MapNode node;
        NodeInfo nodeinfo;

        public LCPFetcher(MapNode node, NodeInfo nodeinfo) {
            this.node = node;
            this.nodeinfo = nodeinfo;
        }

        public void run() {
            LinkedList<LinkCount> lcp = new LinkedList<LinkCount>();
            try {
                Statement s = conn3.createStatement();
                ResultSet r = s.executeQuery("select HIGH_PRIORITY unix_timestamp(clock) as time, count(*) as num_links from " + TABLE_LINKS + " where dest='" + node.name + "' group by clock");
                while (r.next()) {
                    long clock = r.getLong("time");
                    int links = r.getInt("num_links");
                    lcp.add(new LinkCount(clock, links));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                nodeinfo.status = nodeinfo.STATUS_FAILED;
                return;
            }
            nodeinfo.setLinkCountProfile(lcp);
        }
    }

    class LPFetcher extends Thread {

        Link link;
        LinkInfo linkinfo;

        public LPFetcher(Link link, LinkInfo linkinfo) {
            this.link = link;
            this.linkinfo = linkinfo;
        }

        public void run() {
            LinkedList<LinkData> lp = new LinkedList<LinkData>();
            try {
                Statement s = conn3.createStatement();
                ResultSet r = s.executeQuery("select HIGH_PRIORITY unix_timestamp(clock) as time, quality from " + TABLE_LINKS + " where src='" + link.source.name + "' and dest='" + link.dest.name + "'");
                while (r.next()) {
                    long clock = r.getLong("time");
                    float quality = r.getFloat("quality");
                    lp.add(new LinkData(clock, quality));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                linkinfo.status = linkinfo.STATUS_FAILED;
                return;
            }
            linkinfo.setLinkProfile(lp);
        }
    }

    class TimeStampFetcher implements Runnable { //use an own connection for concurrency!

        private final static int OFFSET = 1000;
        private final static int SLEEP = 10;

        public TimeStampFetcher() {
            new Thread(this).start();
        }

        public void run() {
            long offset = 0;
            try {
                while (true) {
                    Statement s = conn2.createStatement();
                    ResultSet r = s.executeQuery("select unix_timestamp(clock) as stamp from " + TABLE_LINKS + " group by clock limit " + OFFSET + " offset " + offset);
                    boolean hasResults = false;
                    long stamp = 0;
                    while (r.next()) {
                        hasResults = true;
                        stamp = r.getLong("stamp");
                        Long clock = new Long(stamp);
                        updateTimes.add(clock);
                        Thread.yield();
                    }
                    if (!hasResults) {
                        break;
                    }
                    setAvailableTime(firstUpdateTime, stamp);
                    //System.out.println("fetched "+firstUpdateTime+" - "+stamp);
                    offset += OFFSET;
                    Thread.sleep(SLEEP);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
