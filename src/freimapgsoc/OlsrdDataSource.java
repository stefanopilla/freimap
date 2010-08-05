/* net.relet.freimap.OlsrdDataSource.java

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

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;
import java.io.*;
import java.net.*;
import java.security.Timestamp;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OlsrdDataSource implements DataSource {

    DotPluginListener dot;
    DataSourceListener listener;
    TreeMap<Long, Vector<Link>> LinkTimes = new TreeMap<Long, Vector<Link>>();
    Hashtable<String, MapNode> generatedNodes = new Hashtable<String, MapNode>();
    Hashtable<String, MapNode> knownNodes = new Hashtable<String, MapNode>();
    Hashtable<String, MapNode> knownNodesByIP = new Hashtable<String, MapNode>();
    Hashtable<Long, Link> generatedLinks = new Hashtable<Long, Link>();
    Hashtable<Long, Link> knownLinks = new Hashtable<Long, Link>();
    long lastUpdateTime = 1,
            firstUpdateTime = 1;
    String nodefile;
    MysqlDataSource mysqlSource;
    xmlDataSource ffmdSource;
    String sNodeSource;
    String username, password, mysqlhost, mysqlport, database;
    Connection c = null;
    ResultSet rss, rss2;
    Statement stmt, stmt2;
    Layer l = null;
    Vector<Link> linkData = null;
    Vector<MapNode> nodeData = null;

    /*
     * ****************************************
     ****************************************
    If I know some nodes I've to add these nodes in the knownodes with a method in the constructor!!!
     * After I can check the presence of the node with the method getNodeByName in the DotListener Class...!!
     * ****************************************
     * ****************************************
     */
    public OlsrdDataSource() {
    }

    public OlsrdDataSource(String host, String port, String username, String password, String database) {
        this.mysqlhost = host;
        this.mysqlport = port;
        this.username = username;
        this.password = password;
        this.database = database;
        init();
    }

    public boolean setConnection() {
        try {
            System.out.println("SetConnection Getting connection to MySql Database at " + mysqlhost + " " + ":" + mysqlport + "");
            Thread.sleep(1000);
            Class.forName("com.mysql.jdbc.Driver");
            c = (Connection) DriverManager.getConnection("jdbc:mysql://" + mysqlhost + ":" + mysqlport, username, password);
            if (!c.isClosed()) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(OlsrdDataSource.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(OlsrdDataSource.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(OlsrdDataSource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public Vector<MapNode> getNodeList() {
        if (setConnection()) {
            if ((this == null)) {
                System.out.println("nodeSource in OlsrdDataSource.java:" + l.getCurrentDataSource().getNodeList());
                sNodeSource = null;
            }

            if (this != null) {
                Vector<MapNode> nodes = this.getNodeList();
                for (Enumeration<String> enu = generatedNodes.keys(); enu.hasMoreElements();) {
                    nodes.add(generatedNodes.get(enu.nextElement()));
                }

                for (int i = 0; i < nodes.size(); i++) {
                    knownNodes.put(nodes.elementAt(i).ip, nodes.elementAt(i));
                }
                return nodes;
            } else {
                try {
                    ObjectInputStream ois = new ObjectInputStream(ClassLoader.getSystemResourceAsStream(nodefile));
                    Vector<MapNode> nodes = (Vector<MapNode>) ois.readObject();
                    ois.close();
                    for (int i = 0; i < nodes.size(); i++) {
                        knownNodes.put(nodes.elementAt(i).ip, nodes.elementAt(i));
                    }
                    return nodes;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return null;
    }

    public HashMap<String, Float> getNodeAvailability(long time) {
        if (this.getNodeList() != null) {
            return this.getNodeAvailability(time);
        } else {
            return new HashMap<String, Float>(); //empty
        }
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public long getFirstUpdateTime() {
        return firstUpdateTime;
    }

    public long getLastAvailableTime() {
        return lastUpdateTime;
    }

    public long getFirstAvailableTime() {
        return firstUpdateTime;
    }

    public long getClosestUpdateTime(long time) {
        long cur = -1, closest = Long.MAX_VALUE;
        Set<Long> keys = LinkTimes.keySet();
        Iterator<Long> ki = keys.iterator();
        while (ki.hasNext()) {
            cur = ki.next().longValue();
            long d = Math.abs(time - cur);
            if (d < closest) {
                closest = d;
            } else {
                break;
            }
        }
        return cur;
    }

    public Vector<Link> getLinks(long time) {
        Vector<Link> linkdata = LinkTimes.get(new Long(time));
        return linkdata;
    }
    //threaded information fetching

    public void addDataSourceListener(DataSourceListener dsl) {
        listener = dsl;
        if (!dot.isAlive()) {
            dot.start();
        }
    }
    //some optional methods

    public void getLinkCountProfile(MapNode node, NodeInfo info) {
        LinkedList<LinkCount> lcp = new LinkedList<LinkCount>();
        //select HIGH_PRIORITY unix_timestamp(clock) as time, count(*) as num_links from "+TABLE_LINKS+" where dest='"+node.id+"' group by clock"
        Iterator<Long> times = LinkTimes.keySet().iterator();
        while (times.hasNext()) {
            Long time = times.next();
            Vector<Link> links = LinkTimes.get(time);
            int lc = 0;
            for (int i = 0; i < links.size(); i++) {
                Link link = links.elementAt(i);
                if (link.dest.equals(node)) {
                    lc++;
                }
            }
            lcp.add(new LinkCount(time.longValue(), lc));
        }
        info.setLinkCountProfile(lcp);
    }

    public void getLinkProfile(Link mylink, LinkInfo info) {
        LinkedList<LinkData> lp = new LinkedList<LinkData>();
        //select HIGH_PRIORITY unix_timestamp(clock) as time, quality from "+TABLE_LINKS+" where src='"+link.from.id+"' and dest='"+link.to.id+"'");
        Iterator<Long> times = LinkTimes.keySet().iterator();
        while (times.hasNext()) {
            Long time = times.next();
            Vector<Link> links = LinkTimes.get(time);
            float quality = 0;
            for (int i = 0; i < links.size(); i++) {
                Link link = links.elementAt(i);
                if (link.equals(mylink)) {
                    quality = link.etx;
                }
            }
            lp.add(new LinkData(time.longValue(), quality));
        }
        info.setLinkProfile(lp);
    }

    //private methods
    private void addLinkTimes(long time, Vector<Link> linkData) {
        LinkTimes.put(new Long(time), linkData);
        if (firstUpdateTime == 1) {
            firstUpdateTime = time;
        }
        lastUpdateTime = time;
        if (listener != null) {
            listener.timeRangeAvailable(firstUpdateTime, lastUpdateTime);
        }
    }

    public MapNode getNodeById(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Vector<Link> getLinksFromSource(MapNode source) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Vector<Link> getLinksFromDest(MapNode dest) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Vector<Link> getLinksFromSource(String id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Vector<Link> getLinksFromDest(String id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     *
     * @param ip
     * @return
     */
    @Override
    public MapNode getNodeByIp(String ip) {
        System.out.println("knownNodes:" + knownNodes);
        if (knownNodes != null) {
            System.out.println("getNodeByIP --> knownNodesByIP != null");
            MapNode x = knownNodes.get(ip);
            System.out.println("IL NOME DEL NODO RESTITUITO DA GETNODEBYNAME E': "+x);
            if (x != null) {
                return x;
            }
        } else {
            System.out.println("getNodeByIp --> knownNodesByIP == null");
            return null;
        }
        return null;

    }

    @Override
    public MapNode getNodeByName(String name) {
        System.out.println("knownNodes:" + knownNodesByIP);
        if (knownNodesByIP != null) {
            System.out.println("getNodeByName --> knownNodesByIP != null");
            MapNode x = knownNodesByIP.get(name);
            System.out.println("L'IP DEL NODO RESTITUITO DA GETNODEBYNAME E': "+x);
            if (x != null) {
                return x;
            }
        } else {
            System.out.println("getNodeByName --> knownNodesByIP == null");

            return null;
        }
        return null;
    }

    public void init_nostore() {
        new Layer().initLayout();
    }

    /**
     *
     * @return
     */
    public void init() {
        String host = "localhost";
        int port = 2004;
        try {

            getNodesfromDB(); //QUERY TO THE DATABASE TO RETRIVE THE KNOW NODES FROM THE MAPNODE TABLE
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(OlsrdDataSource.class.getName()).log(Level.SEVERE, null, ex);
            }
            getLinksfromDB(); //QUERY TO THE DATABASE TO RETRIVE THE KNOW LINKS FROM THE LINKS TABLE

        } catch (ParseException ex) {
            Logger.getLogger(OlsrdDataSource.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (port == -1) {
            System.err.println("invalid port parameter " + port);
            System.exit(1);
        }
        dot = new DotPluginListener(host, port, this);
    }

    public void getNodesfromDB() {
        try {
            System.out.println("Getting connection to MySql Database " + database + " at " + mysqlhost + " " + ":" + mysqlport + "");
            Class.forName("com.mysql.jdbc.Driver");
            c = (Connection) DriverManager.getConnection("jdbc:mysql://" + mysqlhost + ":" + mysqlport + "/" + database, username, password);
            if (!c.isClosed()) {
                System.out.println("Connected!");
                stmt = (Statement) c.createStatement();
                stmt2 = (Statement) c.createStatement();
                String query = "SELECT * FROM NODES";
                rss = stmt.executeQuery(query);
                String query2;
                while (rss.next()) {
                    query2 = "SELECT intIp FROM Interfaces WHERE mainIp = \"" + rss.getString("ip") + "\"";
                    System.out.println(query2);
                    rss2 = stmt2.executeQuery(query2);
                    Vector<String> ifaces = new Vector<String>();
                    while (rss2.next()) {
                        ifaces.add(rss2.getString("IntIp"));
                    }
                    MapNode node = new MapNode(rss.getString("ip"), rss.getString("name"), rss.getString("uptime"), ifaces, rss.getDouble("lat"), rss.getDouble("lon"));
                    System.out.println(node.ip);
                    System.out.println(node.lat);
                    System.out.println(node.lon);
                    System.out.println(node.name);
                    System.out.println(node.inter);
                    knownNodes.put(node.ip, node);
                    System.out.println(knownNodes.values());
                    System.out.println(knownNodes.toString());
                    knownNodesByIP.put(node.name, node);
                     System.out.println(knownNodesByIP.values());
                    System.out.println(knownNodesByIP.toString());
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(OlsrdDataSource.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(OlsrdDataSource.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void getLinksfromDB() throws ParseException {
        try {
            System.out.println("Getting connection to MySql Database " + database + " at " + mysqlhost + " " + ":" + mysqlport + "");
            Class.forName("com.mysql.jdbc.Driver");
            c = (Connection) DriverManager.getConnection("jdbc:mysql://" + mysqlhost + ":" + mysqlport + "/" + database, username, password);
            if (!c.isClosed()) {
                System.out.println("Connected!");
                stmt = (Statement) c.createStatement();
                stmt2 = (Statement) c.createStatement();
                String query = "SELECT * FROM LINKS ORDER BY CAST(CLOCK AS SIGNED)";
                rss = stmt.executeQuery(query);
                String query2;
                Vector<String> timeStamps = new Vector<String>();
                String tmp, tmp2 = null;
                Date time = null;
                while (rss.next()) {
                    query2 = "SELECT * FROM LINKS WHERE CLOCK = \"" + rss.getString("clock") + "\"";
                    System.out.println(query2);
                    rss2 = stmt2.executeQuery(query2);
                    MapNode src=knownNodes.get(rss.getString("src"));
                    MapNode dest=knownNodes.get(rss.getString("dest"));
                    Link link = new Link( src,  dest, rss.getFloat("lq"), rss.getFloat("nlq"), rss.getFloat("etx"), rss.getString("clock"));
                    time = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(rss.getString("clock"));
                    System.out.println("time: " + time.getTime());
                    knownLinks.put(time.getTime(), link);
                    tmp = rss.getString("clock");
                    System.out.println("Primo tmp: " + tmp);
                    if (tmp2 == null || !tmp.equals(tmp2)) {
                        tmp2 = rss.getString("clock");
                        timeStamps.add(tmp);
                        time = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(rss.getString("clock"));
                        System.out.println("time: " + time.getTime());
                    }
                }
                LinkTimes.put(time.getTime(), linkData);
            }

        } catch (SQLException ex) {
            Logger.getLogger(OlsrdDataSource.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(OlsrdDataSource.class.getName()).log(Level.SEVERE, null, ex);
        }
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double getLanFromNode(String ip) {
        return 0;
    }

    //Listener Of DotDraw Plugin
    class DotPluginListener extends Thread {

        BufferedReader in;
        String host;
        int port;
        String url;
        OlsrdDataSource parent;
        MapNode nFrom = null;
        MapNode nTo = null;
        String from, to;
        double DEFAULT_LAT = 41.86378;
        double DEFAULT_LON = 12.55347;
        Hashtable<String, MapNode> genNodes = new Hashtable<String, MapNode>();
        Pattern reNode = Pattern.compile("^(\\d+) \\[label=\"(.*?)\",");
        Pattern reLink = Pattern.compile("^(\\d+) -> (\\d+) \\[");
        Pattern reETX = Pattern.compile("^label=\"(.*?)\"");

        //DotDraw Constructor
        public DotPluginListener(String host, int port, OlsrdDataSource parent) {
            this.parent = parent;
            this.host = host;
            this.port = port;
            System.setProperty("java.net.IPv4Stack", "true"); //not necessary, but works around a bug in older java versions.
            System.out.println("DotPluginListener correctly started on socket: " + host + ":" + port);
            this.run();
        }

        public DotPluginListener(String url, OlsrdDataSource parent) {
            this.parent = parent;
            this.url = url;
            System.setProperty("java.net.IPv4Stack", "true"); //not necessary, but works around a bug in older java versions.
        }

        //DotDraw Methods that open a Socket on the port 2004 and listen the traffic
        public void run() {
            boolean usingWebServiceFormat = false;
            try {
                InetSocketAddress destination = new InetSocketAddress(host, port);
                while (true) { //reconnect upon disconnection
                    if (url != null) {
                        System.out.println("Fetching topology from url " + url);
                        usingWebServiceFormat = true;
                    }
                    Socket s = new Socket();
                    //s.setSoTimeout(10000);
                    s.connect(destination, 25000);
                    in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    System.out.println("in: " + in);
                    while (in != null) {
                        String line = in.readLine();
                        System.out.println("line is: " + line);
                        { //this used to be a try-catch statement
                            if (line == null) {
                                System.out.println(nodeData);
                                System.out.println(linkData);
                                System.out.println(LinkTimes);
                                HashMap<Vector<MapNode>, Vector<Link>> hm = new HashMap<Vector<MapNode>, Vector<Link>>();
                                hm.put(nodeData, linkData);
                                System.out.println("DotDraw Parsed. Creating new Layer...");
                                l = new Layer(hm, this.parent);
                                break;
                            }
                            if (line.equals("digraph topology")) {
                                System.out.println("First Line!");
                                if (LinkTimes != null) {
                                    System.out.println("LinkTimes first: " + LinkTimes.values());
                                    System.out.println("LinkTimes first: " + linkData);
                                    addLinkTimes(System.currentTimeMillis() / 1000, linkData);
                                    System.out.println("LinkTimes After: " + LinkTimes.values());
                                    System.out.println("LinkTimes After: " + linkData);
                                } else {
                                    addLinkTimes(System.currentTimeMillis() / 1000, linkData);
                                }
                            } else if (!(usingWebServiceFormat) && (linkData != null)) {  //yeah, some regexp parsing might be more adequate here
                                Matcher m = reNode.matcher(line);
                                if (m.matches()) {
                                    String id = m.group(1);
                                    String snode = m.group(2);
                                    MapNode node = getNodeByName(snode);
                                    if (node == null) {
                                        node = new MapNode(snode);
                                        generatedNodes.put(snode, node);
                                        if (listener != null) {
                                            listener.nodeListUpdate(node);
                                        }
                                    }
                                    genNodes.put(id, node);
                                } else {
                                    m = reLink.matcher(line);
                                    if (m.matches()) {
                                        from = m.group(1);
                                        to = m.group(2);
                                    } else {
                                        m = reETX.matcher(line);
                                        if (m.matches()) {
                                            String setx = m.group(1);
                                            MapNode nfrom = genNodes.get(from);
                                            MapNode nto = genNodes.get(to);
                                            float etx = setx.equals("INFINITE") ? 0.0f : Float.parseFloat(setx);
                                            if (nfrom == null) {
                                                System.err.println("ERROR: MISSING " + from);
                                            }
                                            if (nto == null) {
                                                System.err.println("ERROR: MISSING TO " + to);
                                            }
                                            linkData.add(new Link(nfrom, nto, etx, false));
                                        }
                                    }
                                }
                            } else if ((linkData != null) && (line.length() > 0) && (line.charAt(0) == '"')) {
                                StringTokenizer st = new StringTokenizer(line, "\"", false);
                                String from = st.nextToken();
                                System.out.println("nextToken() From: " + from);
                                if (from.indexOf("/") > -1) {
                                    from = from.substring(0, from.indexOf("/"));
                                }
                                st.nextToken();
                                if (st.hasMoreTokens()) { //otherwise it's a gateway node!
                                    String to = st.nextToken();
                                    System.out.println("nextToken() To: " + to);
                                    if (to.indexOf("/") > -1) {
                                        to = to.substring(0, to.indexOf("/"));
                                    }
                                    st.nextToken();
                                    String setx = st.nextToken();
                                    System.out.println("setx: " + setx);
                                    if (setx.equals("INFINITE")) {
                                        setx = "0";
                                    }
                                    boolean hna = setx.equals("HNA");
                                    float etx = hna ? 0 : Float.parseFloat(setx);
                                    System.out.println("Node From:" + from);
                                    System.out.println("Node To:" + to);
                                    if (getNodeByName(from) == null) {
                                        nFrom = new MapNode(from, from, DEFAULT_LAT, DEFAULT_LON);
                                        System.out.println(nFrom.DEFAULT_LAT);
                                        System.out.println(nFrom.DEFAULT_LON);
                                        System.out.println(nFrom.ip);
                                        System.out.println(nFrom.name);
                                        //*************read LatLon for LatLon*************
                                        //*************double lat=getLatFromNode(from);*************
                                        //*************double lon=getLonFromNode(from);*************
                                        if (!(genNodes.containsKey(from))) {
                                            genNodes.put(from, nFrom);
                                        }
                                    }
                                    if (getNodeByName(to) == null) {
                                        nTo = new MapNode(to, to, DEFAULT_LAT, DEFAULT_LON);
                                        if (!(genNodes.containsKey(to))) {
                                            genNodes.put(to, nTo);
                                        }
                                    }
                                    /*
                                    if (new MapNode(from) == null) {
                                    Thread.sleep(1000);
                                    System.out.println("NFrom is null:" + nFrom);
                                    System.out.println(from);
                                    if (listener != null) {
                                    //Updates node list adding nfrom node
                                    listener.nodeListUpdate(nFrom);
                                    }
                                    }
                                    if (nTo == null) {
                                    //nto = new MapNode(to);
                                    System.out.println("NTo is null: " + nTo);
                                    //nodeData.add(nto);
                                    if (listener != null) {
                                    listener.nodeListUpdate(nTo);
                                    }
                                    }*/
                                    linkData.add(new Link(nFrom, nTo, etx, hna));

                                }
                            }
                        }
                    }
                    Thread.sleep(10000);
                }
            } catch (SocketTimeoutException ex) {
                System.err.println("[OlsrdDataSource] timeout while trying to connect. " + ex.getMessage());
                return;
            } catch (ConnectException ex) {
                System.err.println("connection to " + host + ":" + port + " failed. Detailed node data won't be available.");
                return;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
