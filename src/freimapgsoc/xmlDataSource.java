package freimapgsoc;

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

/**
 * A {@link DataSource} implementation that reads node data from the
 * <a href="http://www.layereight.de/software.php">FreifunkMap</a> server.
 * 
 * Since it works with URLs, downloaded XML data can be used as well.
 *  
 * @author Robert Schuster (robertschuster@fsfe.org)
 *
 */
public class xmlDataSource implements DataSource {

    Vector<MapNode> nodes = new Vector<MapNode>();
    HashMap<String, MapNode> nodeByName = new HashMap<String, MapNode>();

    public void init(HashMap<String, Object> configuration) {
        String sServerURL = null;
        try {
          //  sServerURL = Configurator.getS("url", configuration);
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
            throw new IllegalStateException("IOException while receiving XML");
        } catch (ParserConfigurationException pce) {
            throw new IllegalStateException(
                    "Class library broken. No suitable XML parser.");
        } catch (SAXException saxe) {
            throw new IllegalStateException("XML broken. Not valid.");
        }
    }

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
                    MapNode nnode = new MapNode(oneip, tooltip, Double.parseDouble(splitCoords[1]), Double.parseDouble(splitCoords[0]));
                    if (height != null) {
                        nnode.attributes.put("height", new Integer(Integer.parseInt(height)));
                    }
                    nodes.add(nnode);
                    nodeByName.put(nnode.name, nnode);
                }
                continue;
            }
            MapNode nnode = new MapNode(ip, tooltip, Double.parseDouble(splitCoords[1]), Double.parseDouble(splitCoords[0]));
            if (height != null) {
                nnode.attributes.put("height", new Integer(Integer.parseInt(height)));
            }
            nodes.add(nnode);
            nodeByName.put(nnode.name, nnode);
        }
    }

    public void addDataSourceListener(DataSourceListener dsl) {
        // TODO: Implement me.
    }

    public long getClosestUpdateTime(long time) {
        // TODO: Implement me.
        return 1;
    }

    public long getFirstUpdateTime() {
        // TODO: Implement me.
        return 1;
    }

    public long getFirstAvailableTime() {
        // TODO: Implement me.
        return 1;
    }

    public long getLastAvailableTime() {
        // TODO: Implement me.
        return 1;
    }

    public long getLastUpdateTime() {
        // TODO: Implement me.
        return 1;
    }

    public MapNode getNodeByName(String id) {
        return nodeByName.put("null", new MapNode("prova","Prova"));
    }

    public void addNode(MapNode node) {
        nodes.remove(node); //just in case
        nodes.add(node);
       // nodeByName.put(node.id, node);
    }

    public void getLinkCountProfile(MapNode node, NodeInfo info) {
        // TODO: Implement me.
        info.setLinkCountProfile(new LinkedList<LinkCount>());
    }

    public void getLinkProfile(Link link, LinkInfo info) {
        // TODO: Implement me.
        info.setLinkProfile(new LinkedList<LinkData>());
    }

    public Vector<Link> getLinks(long time) {
        // TODO: Implement me.
        return new Vector<Link>();
    }

    public HashMap<String, Float> getNodeAvailability(long time) {
        // TODO: Implement me.
        return new HashMap<String, Float>();
    }

    public Vector<MapNode> getNodeList() {
        return nodes;
    }



    public freimapgsoc.MapNode getNodeById(String id) {
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
        throw new UnsupportedOperationException("Not supported yet.");
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

    /**
	 * 
	 * @return 
	 */
	@Override
    public Vector<Link> getLinks() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

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
