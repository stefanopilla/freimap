
import freimapgsoc.Configurator;
import freimapgsoc.DataSource;
import freimapgsoc.DataSourceListener;
import freimapgsoc.FreiLink;
import freimapgsoc.FreiNode;
import freimapgsoc.InfoPopUp;
import freimapgsoc.LatLonJsDataSource;
import freimapgsoc.LinkInfo;
import freimapgsoc.NodeInfo;
import freimapgsoc.Utils;
import freimapgsoc.log;
import freimapgsoc.xmlDataSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;
import org.w3c.dom.NamedNodeMap;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
        HashMap<String, Object> configuration = new HashMap<String, Object>();
        configuration.put("freimapgsoc.xmlDataSource", path);
        String sServerURL = null;
        try {
            sServerURL = path;
            URL serverURL = new URL(sServerURL);
            System.out.println("fetching node data from URL: " + serverURL);
            System.out.print("This may take a while ... ");
            Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(serverURL.openStream());
            System.out.println("finished.");
            parseXml(dom);
        } catch (MalformedURLException mue) {
            System.out.println("failed!");
            throw new IllegalStateException("Invalid server URL: " + sServerURL);
        } catch (IOException ioe) {
            log.append("IOException while receiving XML");
            new InfoPopUp("IOException while receiving XML", "ERROR").setVisible(true);
            throw new IllegalStateException("IOException while receiving XML");
        } catch (ParserConfigurationException pce) {
            log.append("Class library broken. No suitable XML parser.");
            new InfoPopUp("Class library broken. No suitable XML parser.", "ERROR").setVisible(true);
            throw new IllegalStateException("Class library broken. No suitable XML parser.");
        } catch (SAXException saxe) {
            log.append("XML Broken. Not Valid.");
            new InfoPopUp("XML Broken. Not Valid.", "ERROR").setVisible(true);
            throw new IllegalStateException("XML broken. Not valid.");
        }
    }

    String getValue(Node n) {
        if (n != null) {
            return n.getNodeValue();
        }

        return null;
    }

    private void parseXml(Document dom) {
        Node ffmap = dom.getElementsByTagName("ffmap").item(0);
        if (ffmap == null) {
            throw new IllegalStateException("XML data contains no <ffmap> tag. Aborting ...");
        }

        Node versionNode = ffmap.getAttributes().getNamedItem("version");
        if (versionNode == null || Double.parseDouble(versionNode.getNodeValue()) != 1.0) {
            throw new IllegalStateException("Version info in XML does not exist or is invalid. Aborting ...");
        }

        NodeList nl = dom.getElementsByTagName("node");
        int size = nl.getLength();
        for (int i = 0; i < size; i++) {
            Node node = nl.item(i);
            NamedNodeMap attr = node.getAttributes();
            String klass = getValue(attr.getNamedItem("class"));
            String coords = getValue(attr.getNamedItem("coords"));
            String tooltip = getValue(attr.getNamedItem("tooltip"));
            String ip = getValue(attr.getNamedItem("ip"));
            String height = getValue(attr.getNamedItem("height"));

            // Skips old geo data for now.
            if (klass != null && klass.equals("old")) {
                continue;
            }

            if ((ip != null) && (ip.equals(""))) {
                ip = null; //empty ips == null
            }
            // Use ip or coordinates as fqid if tooltip is missing
            if (tooltip == null) {
                if (ip == null) {
                    tooltip = coords;
                } else {
                    tooltip = ip;
                }
            }

            String[] splitCoords = coords.split("\\s*,\\s*");

            if (ip == null) { //we need at least one identifier
                ip = tooltip;
            } else if (ip.indexOf(',') > -1) {  //someone abused XML to stuff several ips into a single attribute.
                StringTokenizer stip = new StringTokenizer(ip, ",", false);
                while (stip.hasMoreTokens()) {
                    String oneip = stip.nextToken();
                    FreiNode nnode = new FreiNode(oneip, tooltip, Double.parseDouble(splitCoords[1]), Double.parseDouble(splitCoords[0]));
                    if (height != null) {
                        nnode.attributes.put("height", new Integer(Integer.parseInt(height)));
                    }
                    nodes.add(nnode);
                    nodeByName.put(nnode.id, nnode);
                }
                continue;
            }
            FreiNode nnode = new FreiNode(ip, tooltip, Double.parseDouble(splitCoords[1]), Double.parseDouble(splitCoords[0]));
            if (height != null) {
                nnode.attributes.put("height", new Integer(Integer.parseInt(height)));
            }
            nodes.add(nnode);
            nodeByName.put(nnode.id, nnode);
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

      public static void storeLatLon(Vector<FreiNode> nodes) {
        latlon = new HashMap<Vector, String>();
        for (int i = 0; i < nodes.size(); i++) {
            Vector latlon2 = new Vector();
            latlon2.add(String.format("%.2f", nodes.elementAt(i).lat));
            latlon2.add(String.format("%.2f", nodes.elementAt(i).lon));
            //System.out.println("LatLon vector: " + latlon2);
            //System.out.println("Value:" + nodes.elementAt(i).toString());
            latlon.put(latlon2, nodes.elementAt(i).toString());

        }
    }

    public static void main(String[] args) {
        String path = "file:/Users/stefanopilla/Desktop/FreiFunk/freimap/trunk/data/ffmap.xml";
        try {
            String id = "xmlFile";
            File f = new File(path);
            String extension = Utils.getExtension(f);
            DataSource source = null;
            if (extension != null) {
                if (extension.equals(Utils.js)) {
                    source = new LatLonJsDataSource();
                }
                if (extension.equals(Utils.xml)) {
                    source = new xmlDataSource();
                }
                if (extension.equals(Utils.txt)) {
                    //IMPLEMENT ME
                    // source = new txtDataSource();
                }
            }
            System.out.println("file:" + path);
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
            System.out.println(source.getNodeList());
            storeLatLon(nodes);
                System.out.println(latlon);
            for (int k = 0; k < nodes.size(); k++) {
                System.out.println("id: " + nodes.get(k) + " lat: " + nodes.get(k).lat + " lon: " + nodes.get(k).lon);
                //locatedN.addElement(nodes.get(k));
                //System.out.println("to:" + links.get(k).to);
                //System.out.println("from: " + links.get(k).from);
                //System.out.println("HNA:" + links.get(k).HNA);
                //System.out.println("udp:" + links.get(k).udp);
                //System.out.println("udp:" + links.get(k).tcp);
                //System.out.println("udp:" + links.get(k).packets);
                //System.out.println("nlq:" + links.get(k).bytes);
                //System.out.println("etx:" + links.get(k).etx);
                //System.out.println("lq:" + links.get(k).lq);
                //System.out.println("nlq:" + links.get(k).nlq);
                //System.out.println("other:" + links.get(k).other);
                //System.out.println("icmp:" + links.get(k).icmp);
                }
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
    }

      

          private static  HashMap<Vector, String> latlon;

}
