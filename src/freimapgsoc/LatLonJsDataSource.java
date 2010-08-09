package freimapgsoc;

import java.awt.Point;
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

    public LatLonJsDataSource() {
    }

    public LatLonJsDataSource(String path) {
        this.init(path);
    }

    public LatLonJsDataSource(String ip, String path){
        this.initFromIp(ip,path);
    }


    public void initFromIp(String ip, String path){
         HashMap<Vector<MapNode>, Vector<Link>> config = new HashMap<Vector<MapNode>, Vector<Link>>();
        String sServerURL = null;
        try {
            sServerURL = path;
            System.out.println("Fetching data from URL: " + sServerURL);
            System.out.println("This may take a while ... ");
            BufferedReader in = new BufferedReader(new InputStreamReader(new URL(sServerURL).openStream()));
            try {
                Thread.sleep(500); // do nothing for 1000 miliseconds (1 second)
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //at the end add others interfaces
            addInterfaces(path);
            try {
                Thread.sleep(500); // do nothing for 1000 miliseconds (1 second)
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            parseLink(sServerURL);
            try {
                Thread.sleep(500); // do nothing for 1000 miliseconds (1 second)
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //  parsePLink(sServerURL); I nedd to know what Plink is!
        } catch (MalformedURLException mue) {
            System.out.println("failed! Invalid server URL: " + sServerURL);
            mue.printStackTrace();
        } catch (IOException ioe) {
            System.out.println("failed! IOException in LatLonJSDataSource");
            ioe.printStackTrace();
        }
        config.put(nodes, links);
    }


    public void init(String path) {
        HashMap<Vector<MapNode>, Vector<Link>> config = new HashMap<Vector<MapNode>, Vector<Link>>();
        String sServerURL = null;
        try {
            sServerURL = path;
            System.out.println("Fetching data from URL: " + sServerURL);
            System.out.println("This may take a while ... ");
            BufferedReader in = new BufferedReader(new InputStreamReader(new URL(sServerURL).openStream()));
            parseNode(sServerURL);
            try {
                Thread.sleep(500); // do nothing for 1000 miliseconds (1 second)
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //at the end add others interfaces
            addInterfaces(path);
            try {
                Thread.sleep(500); // do nothing for 1000 miliseconds (1 second)
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            parseLink(sServerURL);
            try {
                Thread.sleep(500); // do nothing for 1000 miliseconds (1 second)
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //  parsePLink(sServerURL); I nedd to know what Plink is!
        } catch (MalformedURLException mue) {
            System.out.println("failed! Invalid server URL: " + sServerURL);
            mue.printStackTrace();
        } catch (IOException ioe) {
            System.out.println("failed! IOException in LatLonJSDataSource");
            ioe.printStackTrace();
        }
        Layer l = new Layer(nodes,links, this);
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
                        nodeByIP.put(nodes.get(index).ip, nodes.get(index)); //add node to hashmap of nodebyname <String, Node>
                        System.out.println("node by name:" + nodeByIP.values());
                    } else if (index == -1) {
                        MapNode nnode;
                        if ((lat < -90d) || (lat > 90d) || (lon < -180d) || (lon > 180d)) { //obviously bogus. some people do that.
                            System.out.println("MapNode(" + ip + "," + name + ")");
                            nnode = new MapNode(ip, name);//create a node with id=ip and name=fqid Default positions
                        } else {
                            System.out.println("MapNode(" + ip + "," + name + "," + lat + "," + lon + ")");

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
                        System.out.println("nnode.id(exist):" + nnode.name);
                        System.out.println("nnode(exist): " + nnode);
                        nodeByName.put(nnode.name, nnode); //add node to hashmap of nodebyname <String(name), Node>
                        nodeByIP.put(nnode.ip,nnode);//add node to hashmap of nodebyip <String(ip), Node>
                        System.out.println("Node by name (exist):" + nodeByName.values());
                        System.out.println("Node by ip (exist):" + nodeByIP.values());

                    }
                }
            }
        } catch (Exception e) {
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

                    System.out.println("Link Quality: " + lqlink);
                    System.out.println("Neighbor Link Quality: " + nlqlink);
                    System.out.println("Expected Transmission Count (ETX): " + etxlink);


                    srclink = stripQuotes(srclink);
                    destlink = stripQuotes(destlink);

                    MapNode nsrclink = nodeByIP.get(srclink);
                    MapNode ndestlink = nodeByIP.get(destlink);

                    if (nsrclink == null) {
                        System.err.println("Source Link: " + srclink + " not found.");
                    }

                    if (ndestlink == null) {
                        System.err.println("Destination Link: " + destlink + " not found.");
                    }

                    links.add(new Link(nsrclink, ndestlink, (float) lqlink, (float) nlqlink, (float) etxlink));
                }
            }

            for (int i = 0; i < nodes.size(); i++) {
                System.out.println("Nodes " + nodes.get(i).name + " has " + nodes.get(i).inter.size() + " interfaces");
                System.out.println(nodes.get(i).inter.toString());
            }
            //System.out.println("nodes SIZE:" + nodes.size());
            //System.out.println("Links SIZE:" + links.size());
            System.out.println("finished.");
            
        } catch (Exception e) {
            e.getMessage();
        }

    }

    public void parsePLink(String path) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new URL(path).openStream()));
            while (true) {
                line = in.readLine();
                //System.out.println("Line: " + line);
                if (line == null) {
                    break;//if there aren't string in a file
                }
                //if substring is Link then add a link
                if ((line.length() > 5) && (line.substring(0, 5).equals("PLink"))) {
                    //PARSE PLINK
                    StringTokenizer st = new StringTokenizer(line.substring(6, line.length() - 2), ",", false);
                    String src = st.nextToken();
                    String dest = st.nextToken();
                    System.out.println("SRC: " + stripQuotes(src));
                    System.out.println("DEST: " + stripQuotes(dest));

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

                    links.add(new Link(nsrc, ndest, (float) lq, (float) nlq, false));

                    if (nsrc == null) {
                        System.err.println("Source Node: " + src + " not found.");
                    }
                    if (ndest == null) {
                        System.err.println("Destination Node: " + dest + " not found.");
                    }

                }
            }

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
                    System.out.println(index);
                    if (index == -1) {
                        System.out.println("Node is not present in Nodes structure! I create it...");
                        MapNode nnode=new MapNode(nodeip, nodeip); //create a node with name=ip and Default Pos
                        nodes.add(nnode);
                        System.out.println("Node Added now Nodes structure Size is: " + nodes.size());
                    } else {
  //                      System.out.println("The node is present in Nodes structure");
//                        System.out.println("Nodes name/interfaces:" + nodes.get(index).ip + "/" + nodes.get(index).inter.toString());
                        if (!nodes.get(index).inter.contains(nodeip2)) {
                            nodes.get(index).inter.add(nodeip2);
                        }
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
        for (int i = 1; i < nodes.size(); i++) {
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

    public HashMap<String, Float> getNodeAvailability(long time) {
        // TODO: Implement me.
        return new HashMap<String, Float>();
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

    /**
	 * 
	 * @return 
	 */
	@Override
    public Vector<MapNode> getNodeList() {
        return nodes;
    }



    /**
	 * 
	 * @return 
	 */
	@Override
    public void init() {
        throw new UnsupportedOperationException("Not supported yet.");
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

    @Override
    public Vector<Link> getLinks() {
        return links;
    }

    //This method is DEPRECATED
    public MapNode getNodeById(String id) {
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.elementAt(i).name.equals(id)) {
                return nodes.elementAt(i);
            }
        }
        return null;

    }

    public MapNode getNodeByIp(String ip) {
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.elementAt(i).ip.equals(ip)) {
                return nodes.elementAt(i);
            }
        }
        return null;

    }

    public Vector<Link> getLinks(long time) {
        return links;
    }
    public static void main(String[] args) {
       new LatLonJsDataSource("file:///var/run/latlon.js");
     }
    private String line;
    public Vector<MapNode> nodes = new Vector<MapNode>();
    public Vector<Link> links = new Vector<Link>();
    public HashMap<String, MapNode> nodeByName = new HashMap<String, MapNode>();
    public HashMap<String, MapNode> nodeByIP= new HashMap<String, MapNode>();
    public long initTime = System.currentTimeMillis() / 1000;
    public int dsId = 0;

    @Override
    public String getDatabase() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getPort() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getHost() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getUsername() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getPassword() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getPath() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

