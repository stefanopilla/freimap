
import freimapgsoc.Configurator;
import freimapgsoc.DataSource;
import freimapgsoc.DataSourceListener;
import freimapgsoc.FreiLink;
import freimapgsoc.FreiNode;
import freimapgsoc.LinkInfo;
import freimapgsoc.NodeInfo;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

public class Test implements DataSource {

    private static Vector<FreiNode> nodes = new Vector<FreiNode>();
    private static Vector<FreiLink> links = new Vector<FreiLink>();
    HashMap<String, FreiNode> nodeByName = new HashMap<String, FreiNode>();
    public static Configurator config;
    public static HashMap<String, DataSource> sources;
    //NodeLayer nl;
    long initTime = System.currentTimeMillis() / 1000;
    boolean fetchLinks = true;

    public void init(String path) {
        HashMap<String, Object> configuration = new HashMap<String,Object>();
        configuration.put("freimapgsoc.LatLonJsDataSource",path);
        System.out.println(configuration.get(path));
        String sServerURL = path;
        try {
            //fetchLinks = Configurator.getB("fetchlinks", configuration);
            fetchLinks = true;
            System.out.println("Fetch Status Is: " + fetchLinks);
            System.out.println("fetching data from URL: " + sServerURL);
            if (!fetchLinks) {
                System.out.println("NOT fetching link information.");
            }
            System.out.print("This may take a while ... ");

            BufferedReader in = new BufferedReader(new InputStreamReader(new URL(sServerURL).openStream()));
            while (true) {
                String line = in.readLine();
                System.out.println("Line: " + line);
                if (line == null) {
                    break;//if there aren't string in a file
                }
                if ((line.length() > 4) && (line.substring(0, 4).equals("Node"))) {
                    StringTokenizer st = new StringTokenizer(line.substring(5, line.length() - 2), ",", false);
                    String ip = st.nextToken();
                    double lat = Double.parseDouble(st.nextToken());
                    double lon = Double.parseDouble(st.nextToken());
                    int isgateway = Integer.parseInt(st.nextToken());
                    String gatewayip = st.nextToken();
                    String fqid = st.nextToken();

                    ip = stripQuotes(ip); //strip single quotes
                    fqid = stripQuotes(fqid);
                    gatewayip = stripQuotes(gatewayip);

                    // Use ip or coordinates as fqid if tooltip is missing
                    if (ip.equals("")) {
                        ip = null;
                    }
                    if (fqid == null) {
                        if (ip == null) {
                            fqid = lat + "," + lon;
                        } else {
                            fqid = ip;
                        }
                    }
                    if (ip == null) { //we need at least one identifier
                        ip = fqid;
                    }

                    FreiNode nnode;
                    if ((lat < -90d) || (lat > 90d) || (lon < -180d) || (lon > 180d)) { //obviously bogus. some people do that.
                        nnode = new FreiNode(ip, fqid);//create a node with Default positions
                    } else {
                        nnode = new FreiNode(ip, fqid, lon, lat); //create a node with lat lon coordinates
                    }
                    nodes.add(nnode);//add node to Vector "nodes"
                    if (isgateway == 1) { //if node is a Gateway
                        nnode.attributes.put("Gateway", "SELF"); //add attributes ("Gateway", "SELF") or ("Gateway", "OTHER:"+gatewayip) to attributes hash table of freinode
                    } else {
                        nnode.attributes.put("Gateway", "OTHER: " + gatewayip);
                    }
                    //System.out.println("nnode.id:"+ nnode.id);
                    //System.out.println("nnode: "+ nnode);
                    nodeByName.put(nnode.id, nnode); //add node to hashmap of nodebyname <String, Freinode>

                } else if ((fetchLinks) && (line.length() > 5) && (line.substring(0, 5).equals("PLink"))) {
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

                    FreiNode nsrc = nodeByName.get(src);
                    FreiNode ndest = nodeByName.get(dest);

                    if (nsrc == null) {
                        System.err.println(src + " not found.");
                        continue;
                    }
                    if (ndest == null) {
                        System.err.println(dest + " not found.");
                        continue;
                    }

                    FreiLink link = new FreiLink(nsrc, ndest, (float) lq, (float) nlq, false);
                    links.add(link);
                }
            }

            System.out.println("finished.");
            //nl=new NodeLayer(this,map);

        } catch (MalformedURLException mue) {
            System.out.println("failed!");
            System.out.println(mue.getCause());
            System.out.println(mue.getMessage());
            throw new IllegalStateException(mue.getMessage());
        } catch (IOException ioe) {
            System.out.println("failed! IOException in LatLonJSDataSource");
            ioe.printStackTrace();
        }
    }

    public void init(HashMap<String, Object> configuration) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Vector<FreiNode> getNodeList() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Hashtable<String, Float> getNodeAvailability(long time) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getFirstUpdateTime() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getLastUpdateTime() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getLastAvailableTime() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getFirstAvailableTime() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getClosestUpdateTime(long time) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public FreiNode getNodeByName(String id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Vector<FreiLink> getLinks(long time) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addDataSourceListener(DataSourceListener dsl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void getLinkProfile(FreiLink link, LinkInfo info) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void getLinkCountProfile(FreiNode node, NodeInfo info) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private String stripQuotes(String str) {
        if (str.length() <= 2) {
            return null;
        }
        return str.substring(1, str.length() - 1);
    }

    public static void main(String[] args) {
        String path = "file:/Users/stefanopilla/Desktop/FreiFunk/latlon.js";
        try {
            String id = "jsFile";
            Test source = new Test();
            source.init(path); //initialize datasource with file's path
            System.out.println("NodeList: " + source.getNodeList());
            System.out.println("LinksList: " + source.getLinks(0));
            nodes = new Vector<FreiNode>(); //list of jnow nodes
            links = new Vector<FreiLink>(); //list of know links
            for (int t = 0; t < links.size(); t++) {
                System.out.println("Links Element(" + t + "):" + links.elementAt(t));
            }
            links = source.getLinks(0);
            nodes = source.getNodeList();
            for (int k = 0; k < nodes.size(); k++) {
                System.out.println("id: " + nodes.get(k) + " lat: " + nodes.get(k).lat + " lon: " + nodes.get(k).lon);
                System.out.println("to:" + links.get(k).to);
                System.out.println("from: " + links.get(k).from);
                System.out.println("HNA:" + links.get(k).HNA);
                System.out.println("udp:" + links.get(k).udp);
                System.out.println("udp:" + links.get(k).tcp);
                System.out.println("udp:" + links.get(k).packets);
                System.out.println("nlq:" + links.get(k).bytes);
                System.out.println("etx:" + links.get(k).etx);
                System.out.println("lq:" + links.get(k).lq);
                System.out.println("nlq:" + links.get(k).nlq);
                System.out.println("other:" + links.get(k).other);
                System.out.println("icmp:" + links.get(k).icmp);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
    }
}
