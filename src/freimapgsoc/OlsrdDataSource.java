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
import java.text.ParsePosition;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.Position;
import org.openide.util.Exceptions;

public class OlsrdDataSource implements DataSource, DataSourceListener {

    String path;
    NSPluginListener dot;
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
    Vector<Link> linkData = new Vector<Link>();
    Vector<MapNode> nodeData = new Vector<MapNode>();
    Vector<MapNode> nodes = new Vector<MapNode>();
    Vector<Link> links = new Vector<Link>();
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
        this.path = "/var/run/latlon.js";
        init(path);
    }

    @Override
    public String getDatabase(){
        return database;
    }
    public String getPort(){
        return mysqlport;
    }
    public String getHost(){
        return mysqlhost;
    }
    public String getUsername(){
        return username;
    }
    public String getPassword(){
        return password;
    }
    public String getPath(){
        return path;
    }



    public boolean setConnection() {
        try {
            System.out.println("SetConnection Getting connection to MySql Database at " + mysqlhost + ":" + mysqlport + "");
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
        return nodes;
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

    public boolean isKnown(String id) {
        if (knownNodesByIP != null || knownNodes != null) {
            if (knownNodesByIP.containsKey(id) || knownNodes.containsKey(id)) {
                return true;
            }
        } else {
            return false;
        }
        return false;
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
            System.out.println("IL NOME DEL NODO RESTITUITO DA GETNODEBYNAME E': " + x);
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
        if (!isKnown(name)) {
            MapNode x = knownNodesByIP.get(name);
            System.out.println("L'IP DEL NODO RESTITUITO DA GETNODEBYNAME E': " + x);
            if (x != null) {
                return x;
            }
        } else {
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
    public void init(String path) {
        try {
            try {
                getNodesfromDB(); //QUERY TO THE DATABASE TO RETRIVE THE KNOW NODES FROM THE MAPNODE TABLE
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(OlsrdDataSource.class.getName()).log(Level.SEVERE, null, ex);
                }
                getLinksfromDB(); //QUERY TO THE DATABASE TO RETRIVE THE KNOW LINKS FROM THE LINKS TABLE
            } catch (ParseException ex) {
                Logger.getLogger(OlsrdDataSource.class.getName()).log(Level.SEVERE, null, ex);
            }
            Runnable r = new NSPluginListener(path, this);
            Thread thread = new Thread(r);
            thread.start();
            HashMap<Vector<MapNode>, Vector<Link>> hm = new HashMap<Vector<MapNode>, Vector<Link>>();
            Thread.sleep(3000);
            l = new Layer(nodes, links, this);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
     
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
                    //System.out.println(query2);
                    rss2 = stmt2.executeQuery(query2);
                    Vector<String> ifaces = new Vector<String>();
                    while (rss2.next()) {
                        ifaces.add(rss2.getString("IntIp"));
                    }
                    MapNode node = new MapNode(rss.getString("ip"), rss.getString("name"), rss.getString("uptime"), ifaces, rss.getDouble("lat"), rss.getDouble("lon"));
                    //System.out.println(node.ip);
                    //System.out.println(node.lat);
                    //System.out.println(node.lon);
                    // System.out.println(node.name);
                    // System.out.println(node.inter);
                    knownNodes.put(node.ip, node);
                    //  System.out.println(knownNodes.values());
                    //   System.out.println(knownNodes.toString());
                    knownNodesByIP.put(node.name, node);
                    // System.out.println(knownNodesByIP.values());
                    //  System.out.println(knownNodesByIP.toString());
                    nodes.add(node);
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
                    //System.out.println(query2);
                    rss2 = stmt2.executeQuery(query2);
                    MapNode src = knownNodes.get(rss.getString("src"));
                    MapNode dest = knownNodes.get(rss.getString("dest"));
                    Link link = new Link(src, dest, rss.getFloat("lq"), rss.getFloat("nlq"), rss.getFloat("etx"), rss.getString("clock"));
                    time = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(rss.getString("clock"));
                    knownLinks.put(time.getTime(), link);
                    //System.out.println(knownLinks.values());
                    links.add(link);
                    tmp = rss.getString("clock");
                    if (tmp2 == null || !tmp.equals(tmp2)) {
                        tmp2 = rss.getString("clock");
                        timeStamps.add(tmp);
                        time = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(rss.getString("clock"));
                    }
                }
                System.out.println("LinkData: " + links);
                LinkTimes.put(time.getTime(), links);
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
        return links;
    }

    public double getLanFromNode(String ip) {
        return 0;
    }

    @Override
    public void timeRangeAvailable(long from, long until) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void nodeListUpdate(MapNode node) {
        System.out.println("Added into nodeData from nodeListUpdate");
        nodeData.add(node);
        knownNodes.put(node.ip, node);
    }

    @Override
    public void init() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    //Listener Of DotDraw Plugin
    class NSPluginListener extends Thread {
        String line;
        BufferedReader in;
        String host;
        int port;
        String path;
        OlsrdDataSource parent;
        MapNode nFrom;
        MapNode nTo;
        String from, to;
        double DEFAULT_LAT = 41.86378;
        double DEFAULT_LON = 12.55347;
        Pattern reNode = Pattern.compile("^(\\d+) \\[label=\"(.*?)\",");
        Pattern reLink = Pattern.compile("^(\\d+) -> (\\d+) \\[");
        Pattern reETX = Pattern.compile("^label=\"(.*?)\"");

        //DotDraw Constructor
        public NSPluginListener(String host, int port, OlsrdDataSource parent) {
            this.parent = parent;
            this.host = host;
            this.port = port;
            System.setProperty("java.net.IPv4Stack", "true"); //not necessary, but works around a bug in older java versions.
            System.out.println("NSPluginListener correctly started on socket: " + host + ":" + port);

        }

        public void addDataSourceListener(DataSourceListener dsl) {
            listener = dsl;
            if (!dot.isAlive()) {
                dot.start();
            }
        }

        public NSPluginListener(String path, OlsrdDataSource parent) {
            this.parent = parent;
            this.path = path;
            System.setProperty("java.net.IPv4Stack", "true"); //not necessary, but works around a bug in older java versions.
        }

        private String stripQuotes(String str) {
            if (str.length() <= 2) {
                return null;
            }
            return str.substring(1, str.length() - 1);
        }

        private int getIndex(Vector<MapNode> nodes, String nodeip) {
            for (int i = 1; i < nodes.size(); i++) {
                if (nodeip.equals(nodes.get(i).ip)) {
                    return i;
                }
            }
            return -1;
        }

        public void parseNode(String path) {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(new URL(path).openStream()));
                while (true) {
                    line = in.readLine();
                    //System.out.println("Line: " + line);
                    if (line == null) {
                        break;//if there aren't string in a file
                    } //if substring is Node then add a node
                    if ((line.length() > 4) && (line.substring(0, 4).equals("Node"))) {

                        //PARSE NODE
                        StringTokenizer st = new StringTokenizer(line.substring(5, line.length() - 2), ",", false);
                        String ip = st.nextToken();
                        double lat = Double.parseDouble(st.nextToken());
                        double lon = Double.parseDouble(st.nextToken());
                        int isgateway = Integer.parseInt(st.nextToken());
                        String gatewayip = st.nextToken();
                        String name = st.nextToken();
                        ip = stripQuotes(ip); //strip single quotes
                        name = stripQuotes(name);
                        gatewayip = stripQuotes(gatewayip);
                        
                        // Use ip or coordinates as fqid if tooltip is missing
                        if (ip == null) { //i not need this but....! :-D
                            ip = null;
                        }
                        if (name == null) {
                            System.out.println("This node has no name!");
                            if (ip == null) {
                                System.out.println("This node has also no ip.");
                                System.out.println("I use (lat,lon) as name");
                                name = lat + "," + lon;
                            } else {
                                System.out.println("I use Ip Address as name");
                                name = ip;
                            }
                        }
                        if (ip == null) { //we need at least one identifier
                            ip = name;
                        }

                        int index = getIndex(nodes, ip);
                        if (index != -1) {
                            if (isgateway == 1) { //if node is a Gateway
                                nodes.get(index).attributes.put("Gateway", "SELF"); //add attributes ("Gateway", "SELF") or ("Gateway", "OTHER:"+gatewayip) to attributes hash table of Node
                            } else {
                                nodes.get(index).attributes.put("Gateway", "OTHER: " + gatewayip);
                            }
                            nodes.get(index).ip = ip;
                            nodes.get(index).name = name;
                            System.out.println("nnode.id:" + nodes.get(index).name);
                            System.out.println("nnode: " + nodes.get(index));
                            knownNodesByIP.put(nodes.get(index).ip, nodes.get(index)); //add node to hashmap of nodebyname <String, Node>
                            System.out.println("node by name:" + knownNodesByIP.values());
                        } else if (index == -1) {
                            MapNode nnode;
                            HashMap<String, Object> att=new HashMap<String, Object>();
                            if ((lat < -90d) || (lat > 90d) || (lon < -180d) || (lon > 180d)) { //obviously bogus. some people do that.
                                System.out.println("MapNodeNOLATLON(" + ip + "," + name + ")");
                                nnode = new MapNode(ip, name);//create a node with id=ip and name=fqid Default positions
                            } else {
                                System.out.println("MapNodeMore(" + ip + "," + name + "," + lat + "," + lon + ")");
                                nnode = new MapNode(ip, name, lat, lon); //create a node with id=ip and name=fqid lat lon coordinates                            
                            }
                            if (isgateway == 1) { //if node is a Gateway
                                att.put("Gateway", "SELF"); //add attributes ("Gateway", "SELF") or ("Gateway", "OTHER:"+gatewayip) to attributes hash table of Node
                            } else {
                                att.put("Gateway", "OTHER: " + gatewayip);
                            }
                            nnode.ip = ip;
                            nnode.name = name;
                            nnode.attributes=att;
                           // System.out.println("nnode.id(exist):" + nnode.name);
                           // System.out.println("nnode(exist): " + nnode);
                            knownNodes.put(nnode.name, nnode); //add node to hashmap of nodebyname <String(name), Node>
                            knownNodesByIP.put(nnode.ip, nnode);//add node to hashmap of nodebyip <String(ip), Node>
                           // System.out.println("Node by name (exist):" + knownNodes.values());
                           // System.out.println("Node by ip (exist):" + knownNodesByIP.values());
                            nodes.add(nnode);//add node to Vector "nodes"
                            
                        }

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        public void parseLink(String path) {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(new URL(path).openStream()));
                while (true) {
                    line = in.readLine();
                    //System.out.println("Line: " + line);
                    if (line == null) {
                        break;//if there aren't string in a file
                    }
                    if ((line.length() > 5) && (line.substring(0, 4).equals("Link"))) {
                        //PARSE LINK
                        StringTokenizer stlink = new StringTokenizer(line.substring(5, line.length() - 2), ",", false);
                        String srclink = stlink.nextToken();
                        String destlink = stlink.nextToken();
                        System.out.println("Src Link: " + srclink);
                        System.out.println("Dest Link: " + destlink);

                        double lqlink = Double.parseDouble(stlink.nextToken());
                        double nlqlink = Double.parseDouble(stlink.nextToken());
                        double etxlink = Double.parseDouble(stlink.nextToken());

                       // System.out.println("Link Quality: " + lqlink);
                        //System.out.println("Neighbor Link Quality: " + nlqlink);
                        //System.out.println("Expected Transmission Count (ETX): " + etxlink);
                        srclink = stripQuotes(srclink);
                        destlink = stripQuotes(destlink);
                       
                        MapNode nsrclink = knownNodes.get(srclink);
                        MapNode ndestlink = knownNodes.get(destlink);

                        if (nsrclink == null) {
                            System.err.println("Source Link: " + srclink + " not found.");
                        }

                        if (ndestlink == null) {
                            System.err.println("Destination Link: " + destlink + " not found.");
                        }
                        if (LinkTimes != null) {
                            addLinkTimes(System.currentTimeMillis() / 1000, links);
                        } else {
                            addLinkTimes(System.currentTimeMillis() / 1000, links);
                        }
                        links.add(new Link(nsrclink, ndestlink, (float) lqlink, (float) nlqlink, (float) etxlink));
                    }
                }

               
                System.out.println("finished.");

            } catch (Exception e) {
                e.getMessage();
            }
        }

        public void addInterfaces(String path) {
            System.out.println("Now check if nodes has more than one interface...");
            String sServerURL = path;
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(new URL(sServerURL).openStream()));
                //PARSE MID
                while (true) {
                    line = in.readLine();
                    //System.out.println("Line: " + line);
                    if (line == null) {
                        break;//if there aren't string in a file
                    }
                    if ((line.length() > 3) && (line.substring(0, 3).equals("Mid"))) {
                        StringTokenizer stnode = new StringTokenizer(line.substring(4, line.length() - 2), ",", false);
                        String nodeip = stnode.nextToken();
                        String nodeip2 = stnode.nextToken();
                        System.out.println("node " + nodeip + " has also " + nodeip2 + " interfaces");
                        nodeip = stripQuotes(nodeip);
                        nodeip2 = stripQuotes(nodeip2);
                        //Search a node in nodes...if there's the interface can be added
                        //else create e new node
                        //System.out.println("NodesSize: " + nodes.size());
                        int index = getIndex(nodes, nodeip);
                        System.out.println("INDEX IS: " + index);
                        if (index == -1) {
                            System.out.println("Node is not present in Nodes structure! I create it...");
                            MapNode nnode = new MapNode(nodeip, nodeip); //create a node with name=ip and Default Pos
                            nodes.add(nnode);
                            System.out.println("Node Added now Nodes structure Size is: " + nodes.size());
                        } else if (index != -1){
                            System.out.println("Nodes structure Size is: " + nodes.size());
                            System.out.println(nodes.get(index));
                            Vector<String> inter=new Vector<String>();
                            inter.add(nodeip2);
                            nodes.get(index).inter=inter;
                            
                        }
                    }
                }
            } catch (MalformedURLException ex) {
                Logger.getLogger(LatLonJsDataSource.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ioe) {
                System.out.println("failed! IOException in LatLonJSDataSource");
                ioe.printStackTrace();
            }
        }

        public void run() {
            try {
                HashMap<Vector<MapNode>, Vector<Link>> data = new HashMap<Vector<MapNode>, Vector<Link>>();
                String sServerURL = null;
                try {
                    sServerURL = "file:///var/run/latlon.js";
                    System.out.println("Fetching data from URL: " + sServerURL);
                    System.out.println("This may take a while ... ");
                    BufferedReader in = new BufferedReader(new InputStreamReader(new URL(sServerURL).openStream()));
                    parseNode(sServerURL);
                    System.out.println("Alla fine di ParseNode: " + nodes);
                    try {
                        Thread.sleep(500); // do nothing for 1000 miliseconds (1 second)
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    addInterfaces(sServerURL);
                    System.out.println("Alla fine di addInterfaces: " + nodes);

                    try {
                        Thread.sleep(500); // do nothing for 1000 miliseconds (1 second)
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    parseLink(sServerURL);
                    System.out.println("Alla fine di ParseLink: " + nodes);

                    try {
                        Thread.sleep(500); // do nothing for 1000 miliseconds (1 second)
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //  parsePLink(sServerURL); I nedd to know what Plink is!
                    //  parsePLink(sServerURL); I nedd to know what Plink is!
                } catch (MalformedURLException mue) {
                    System.out.println("failed! Invalid server URL: " + sServerURL);
                    mue.printStackTrace();
                } catch (IOException ioe) {
                    System.out.println("failed! IOException in LatLonJSDataSource");
                    ioe.printStackTrace();
                }
                data.put(nodes, links);
                Thread.sleep(10000);
            } catch (Exception ex) {
                System.err.println("[NameServicePlugin] Error while trying to parse data. " + ex.getMessage());
                ex.printStackTrace();

            }
        }
    }
}
