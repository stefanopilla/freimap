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
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OlsrdDataSource implements DataSource {

    DotPluginListener dot;
    DataSourceListener listener;
    TreeMap<Long, Vector<Link>> data = new TreeMap<Long, Vector<Link>>();
    Hashtable<String, MapNode> knownNodes = new Hashtable<String, MapNode>();
    Hashtable<String, MapNode> generatedNodes = new Hashtable<String, MapNode>();
    long lastUpdateTime = 1,
            firstUpdateTime = 1;
    String nodefile;
    MysqlDataSource mysqlSource;
    xmlDataSource ffmdSource;
    String sNodeSource;
    String username, password, mysqlhost, mysqlport;
    Connection c = null;
    ResultSet rss;
    Statement stmt;
    Layer l=null;

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


    public OlsrdDataSource(String host, String port, String username, String password) {
        this.mysqlhost = host;
        this.mysqlport = port;
        this.username = username;
        this.password = password;
        init();
    }

    public boolean setConnection() {
        try {
            System.out.println("Getting connection to MySql Database at " + mysqlhost + " " + ":" + mysqlport + "");
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

    @Override
    public MapNode getNodeByName(String id) {
        if (this != null) {
            MapNode x = this.getNodeByIp(id);
            if (x != null) {
                return x;
            }
        }
        MapNode node = knownNodes.get(id);
        if (node != null) {
            return node;
        } else {
            return generatedNodes.get(id);
        }
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
        Set<Long> keys = data.keySet();
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
        Vector<Link> linkdata = data.get(new Long(time));
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
        Iterator<Long> times = data.keySet().iterator();
        while (times.hasNext()) {
            Long time = times.next();
            Vector<Link> links = data.get(time);
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
        Iterator<Long> times = data.keySet().iterator();
        while (times.hasNext()) {
            Long time = times.next();
            Vector<Link> links = data.get(time);
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
    private void addLinkData(long time, Vector<Link> linkData) {
        data.put(new Long(time), linkData);
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
        if()
        try{
            return getNodeByIp(ip);
        }catch (Exception e){
            return null;
        }
        
    }

    /**
     *
     * @return
     */
    public void init() {
        String host = "localhost";
        int port = 2004;

        // nodefile = Configurator.getS("nodefile", conf);
        //System.out.println("nodefile = "+nodefile);

        //sNodeSource = Configurator.getS("nodesource", conf);

        if (port == -1) {
            System.err.println("invalid port parameter " + port);
            System.exit(1);
        }
        dot = new DotPluginListener(host, port, this);
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

    //Listener Of DotDraw Plugin
    class DotPluginListener extends Thread {

        BufferedReader in;
        String host;
        int port;
        OlsrdDataSource parent;

        //DotDraw Constructor
        public DotPluginListener(String host, int port, OlsrdDataSource parent) {
            this.parent = parent;
            this.host = host;
            this.port = port;
            System.setProperty("java.net.IPv4Stack", "true"); //not necessary, but works around a bug in older java versions.
            System.out.println("DotPluginListener correctly started on socket: " + host + ":" + port);
            this.run();
        }

        //DotDraw Methods that open a Socket on the port 2004 and listen the traffic
        public void run() {
            Vector<Link> linkData = null;
            Vector<MapNode> nodeData = null;
            try {
                InetSocketAddress destination = new InetSocketAddress(host, port);
                while (true) { //reconnect upon disconnection
                    Socket s = new Socket();
                    //s.setSoTimeout(10000);
                    s.connect(destination, 25000);
                    in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    while (in != null) {
                        String line = in.readLine();
                        { //this used to be a try-catch statement
                            if (line == null) {
                                break;
                            }
                            if (line.equals("digraph topology")) {
                                if (linkData != null) {
                                    parent.addLinkData(System.currentTimeMillis() / 1000, linkData);
                                }
                                linkData = new Vector<Link>();
                            } else if ((linkData != null) && (line.length() > 0) && (line.charAt(0) == '"')) {
                                StringTokenizer st = new StringTokenizer(line, "\"", false);
                                String from = st.nextToken();
                                if (from.indexOf("/")>-1) { from = from.substring(0, from.indexOf("/")); }
                                st.nextToken();
                                if (st.hasMoreTokens()) { //otherwise it's a gateway node!
                                    String to = st.nextToken();
                                    if (to.indexOf("/")>-1) { to = to.substring(0, to.indexOf("/")); }
                                    st.nextToken();
                                    String setx = st.nextToken();
                                    if (setx.equals("INFINITE")) {
                                        setx = "0";
                                    }
                                    boolean hna = setx.equals("HNA");
                                    float etx = hna ? 0 : Float.parseFloat(setx);
                                    System.out.println("Node From:"+from);
                                    System.out.println("Node To:"+to);
                                   MapNode nfrom = getNodeByName(from),
                                            nto = getNodeByName(to);
                                   //here the code that try to get MapNode name from NameServicePlugin
                                    if (nfrom == null) {
                                        nfrom = new MapNode(from);
                                        nodeData.add(nfrom);
                                        if (listener != null) {
                                            listener.nodeListUpdate(nfrom);
                                        }
                                    }
                                    if (nto == null) {
                                        nto = new MapNode(to);
                                        nodeData.add(nto);
                                        if (listener != null) {
                                            listener.nodeListUpdate(nto);
                                        }
                                    }
                                    linkData.add(new Link(nfrom, nto, etx, hna));
                                    HashMap<Vector<MapNode>, Vector<Link>> hm = new HashMap<Vector<MapNode>, Vector<Link>>();
                                    hm.put(nodeData,linkData);
                                    System.out.println("Creating new Layer...");
                                    l = new Layer(hm, this.parent);
                                }
                            }
                        }
                    }
                    Thread.sleep(1000);
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
