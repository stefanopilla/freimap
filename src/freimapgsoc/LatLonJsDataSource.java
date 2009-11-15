package freimapgsoc;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.swingx.JXMapViewer;

/**
 * A {@link DataSource} implementation that reads node data from the
 * <a href="http://www.layereight.de/software.php">FreifunkMap</a> plugin 
 * latlon.js file. You will need either a local copy or have it available
 * on a web server.
 * 
 * Since it works with URLs, downloaded data can be used as well.
 *  
 * @author Thomas Hirsch (thomas hirsch gmail com)
 *
 */
public class LatLonJsDataSource implements DataSource {

    Vector<MapNode> nodes = new Vector<MapNode>();
    Vector<Link> links = new Vector<Link>();
    HashMap<String, MapNode> nodeByName = new HashMap<String, MapNode>();
    //NodeLayer nl;
    long initTime = System.currentTimeMillis() / 1000;
    boolean fetchLinks = true;

    public HashMap<Vector<MapNode>, Vector<Link>> init(String path) {
        HashMap<Vector<MapNode>, Vector<Link>> config = new HashMap<Vector<MapNode>, Vector<Link>>();
        String sServerURL = null;
        try {
            sServerURL = path;
            System.out.println("Fetching data from URL: " + sServerURL);
            System.out.println("This may take a while ... ");

            BufferedReader in = new BufferedReader(new InputStreamReader(new URL(sServerURL).openStream()));
            while (true) {
                String line = in.readLine();
                //System.out.println("Line: " + line);
                if (line == null) {
                    break;//if there aren't string in a file
                }

                //PARSE NODE
                if ((line.length() > 4) && (line.substring(0, 4).equals("Node"))) {
                    StringTokenizer st = new StringTokenizer(line.substring(5, line.length() - 2), ",", false);
                    String ip = st.nextToken();
                    double lat = Double.parseDouble(st.nextToken());
                    double lon = Double.parseDouble(st.nextToken());
                    int isgateway = Integer.parseInt(st.nextToken());
                    String gatewayip = st.nextToken();
                    String name = st.nextToken();

                    System.out.println("ip:" + stripQuotes(ip));
                    //System.out.println("lat:" + lat);
                    //System.out.println("lon:" + lon);
                    //System.out.println("isgateway:" + isgateway);
                    //System.out.println("gatewayip:" + gatewayip);
                    //System.out.println("name:" + stripQuotes(name));

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
                            System.out.println("This node has no ip.");
                            System.out.println("I use (lat,lon) as name");
                            name = lat + "," + lon;
                        } else {
                            System.out.println("I use ip as name");
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
                        //System.out.println("nnode.id:" + nodes.get(index).name);
                        //System.out.println("nnode: " + nodes.get(index));
                        nodeByName.put(nodes.get(index).ip, nodes.get(index)); //add node to hashmap of nodebyname <String, Node>
                        System.out.println("node by name:" + nodeByName.values());
                    } else if (index == -1) {
                        MapNode nnode;
                        if ((lat < -90d) || (lat > 90d) || (lon < -180d) || (lon > 180d)) { //obviously bogus. some people do that.
                            System.out.println("MapNode(" + ip + "," + name + ")");
                            nnode = new MapNode(ip, name);//create a node with id=ip and name=fqid Default positions
                        } else {
                            System.out.println("MapNode(" + ip + "," + name + "." + lat + "," + lon + ")");

                            nnode = new MapNode(ip, name, lat, lon); //create a node with id=ip and name=fqid lat lon coordinates
                        }
                        nnode.ip = ip;
                        nnode.name = name;
                        if (isgateway == 1) { //if node is a Gateway
                            nnode.attributes.put("Gateway", "SELF"); //add attributes ("Gateway", "SELF") or ("Gateway", "OTHER:"+gatewayip) to attributes hash table of Node
                        } else {
                            nnode.attributes.put("Gateway", "OTHER: " + gatewayip);
                        }

                        nodes.add(nnode);//add node to Vector "nodes"
                        // System.out.println("nnode.id(exist):" + nnode.name);
                        // System.out.println("nnode(exist): " + nnode);
                        nodeByName.put(nnode.ip, nnode); //add node to hashmap of nodebyname <String, Node>
                        System.out.println("node by name(exist):" + nodeByName.values());
                    }

                }
            }

            addInterfaces(path);

            /*
            }//PARSE PLINK
            if ((line.length() > 5) && (line.substring(0, 5).equals("PLink"))) {
            StringTokenizer st = new StringTokenizer(line.substring(6, line.length() - 2), ",", false);
            String src = st.nextToken();
            String dest = st.nextToken();
            System.out.println("SRC: " + src);
            System.out.println("DEST: " + dest);

            double lq = Double.parseDouble(st.nextToken());
            double nlq = Double.parseDouble(st.nextToken());
            double etx = Double.parseDouble(st.nextToken());

            System.out.println("lq: " + lq);
            System.out.println("nlq: " + nlq);
            System.out.println("etx: " + etx);


            src = stripQuotes(src);
            dest = stripQuotes(dest);

            MapNode nsrc = nodeByName.get(src);
            MapNode ndest = nodeByName.get(dest);

            if (nsrc == null) {
            System.err.println(src + " not found.");
            continue;
            }
            if (ndest == null) {
            System.err.println(dest + " not found.");
            continue;
            }
            links.add(new Link(nsrc, ndest, (float) lq, (float) nlq, false));
            }

            /*
            //PARSE LINK
            if ((line.length() > 4) && (line.substring(0, 4).equals("Link"))) {
            StringTokenizer stlink = new StringTokenizer(line.substring(5, line.length() - 2), ",", false);
            String srclink = stlink.nextToken();
            String destlink = stlink.nextToken();
            System.out.println("SRCLINK: " + srclink);
            System.out.println("DESTLINK: " + destlink);

            double lqlink = Double.parseDouble(stlink.nextToken());
            double nlqlink = Double.parseDouble(stlink.nextToken());
            double etxlink = Double.parseDouble(stlink.nextToken());

            System.out.println("lqLINK: " + lqlink);
            System.out.println("nlqLINK: " + nlqlink);
            System.out.println("etxLINK: " + etxlink);


            srclink = stripQuotes(srclink);
            destlink = stripQuotes(destlink);

            MapNode nsrclink = nodeByName.get(srclink);
            MapNode ndestlink = nodeByName.get(destlink);

            if (nsrclink == null) {
            System.err.println(srclink + " not found.");
            continue;
            }
            if (ndestlink == null) {
            System.err.println(destlink + " not found.");
            continue;
            }
            links.add(new Link(nsrclink, ndestlink, (float) lqlink, (float) nlqlink, (float) etxlink));

            }
             */


            for (int i = 0; i < nodes.size(); i++) {
                System.out.println("Nodes " + nodes.get(i).name + " has " + nodes.get(i).inter.size() + " interfaces");
                System.out.println(nodes.get(i).inter.toString());
            }
            //System.out.println("nodes SIZE:" + nodes.size());
            //System.out.println("Links SIZE:" + links.size());
            System.out.println("finished.");
            for (int i = 0; i < nodes.size(); i++) {
                System.out.println("\n");
                System.out.println("Nodes " + nodes.get(i).name + " has: ");
                System.out.println("ip:" + nodes.get(i).ip);
                System.out.println("lat:" + nodes.get(i).lat);
                System.out.println("lon:" + nodes.get(i).lon);
                System.out.println("attributes:" + nodes.get(i).attributes);
                System.out.println("uptime:" + nodes.get(i).uptime);
                System.out.println("interfaces:" + nodes.get(i).inter.toString());
            }



        } catch (MalformedURLException mue) {
            System.out.println("failed!");
            throw new IllegalStateException("Invalid server URL: " + sServerURL);
        } catch (IOException ioe) {
            System.out.println("failed! IOException in LatLonJSDataSource");
            ioe.printStackTrace();
        }
        config.put(nodes, links);

        return config;
    }

    public void addInterfaces(String path) {
        System.out.println("Now take a control if nodes has more than one interface...");
        String sServerURL = null;
        try {
            sServerURL = path;
            BufferedReader in = new BufferedReader(new InputStreamReader(new URL(sServerURL).openStream()));
            //PARSE MID
            while (true) {
                String line = in.readLine();
                //System.out.println("Line: " + line);
                if (line == null || !(line.substring(0, 3).equals("Mid"))) {
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
                    if (index == -1) {
                        System.out.println("Node is not present in Nodes structure! I create it...");
                        MapNode nnode;
                        nnode = new MapNode(nodeip, nodeip); //create a node with name=ip and Default Pos
                        nodes.add(nnode);
                        System.out.println("Node Added now Nodes Size is: " + nodes.size());
                    } else {
                        System.out.println("The node is present in Nodes");
                        System.out.println("nodes name/interfaces:" + nodes.get(index).ip+"/"+ nodes.get(index).inter.toString());
                        nodes.get(index).inter.add(nodeip2);
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

    int getIndex(Vector<MapNode> nodes, String nodeip) {
        for (int i = 0; i < nodes.size(); i++) {
            if (nodeip.equals(nodes.get(i).ip)) {
                return i;
            }
        }
        return -1;
    }

    private String stripQuotes(String str) {
        if (str.length() <= 2) {
            return null;
        }
        return str.substring(1, str.length() - 1);
    }

    public void addDataSourceListener(DataSourceListener dsl) {
        // TODO: Implement me.
    }

    public long getClosestUpdateTime(long time) {
        return initTime;
    }

    public long getFirstUpdateTime() {
        return initTime;
    }

    public long getFirstAvailableTime() {
        return initTime;
    }

    public long getLastAvailableTime() {
        return initTime;
    }

    public long getLastUpdateTime() {
        return initTime;
    }

    public MapNode getNodeByName(String name) {
        return nodeByName.get(name);
    }

    public void addNode(MapNode node) {
        nodes.remove(node); //just in case
        nodes.add(node);
        nodeByName.put(node.name, node);
    }

    public void getLinkCountProfile(MapNode node, NodeInfo info) {
        // TODO: Implement me.
        info.setLinkCountProfile(new LinkedList<LinkCount>());
    }

    public void getLinkProfile(Link link, LinkInfo info) {
        // TODO: Implement me.
        info.setLinkProfile(new LinkedList<LinkData>());
    }

    public Hashtable<String, Float> getNodeAvailability(long time) {
        // TODO: Implement me.
        return new Hashtable<String, Float>();
    }

    public Vector<Link> getLinksFromSource(String sourceid) {
        Vector<Link> linksfs = new Vector<Link>();
        for (int i = 0; i < links.size(); i++) {
            links.elementAt(i).source.equals(sourceid);
            linksfs.add(links.elementAt(i));
        }
        return linksfs;
    }

    public Vector<Link> getLinksFromDest(String dest) {
        Vector<Link> linksfd = new Vector<Link>();
        for (int i = 0; i < links.size(); i++) {
            links.elementAt(i).dest.equals(dest);
            linksfd.add(links.elementAt(i));
        }
        return linksfd;
    }

    public Vector<MapNode> getNodeList() {
        return nodes;
    }

    public MapNode getNodeById(String id) {
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.elementAt(i).name.equals(id)) {
                return nodes.elementAt(i);
            }
        }
        return null;

    }

    public Vector<Link> getLinks(long time) {
        return links;
    }

    public static void main(String[] args) {
        new LatLonJsDataSource().init("file:///var/run/latlon.js");
    }
}
