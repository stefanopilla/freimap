/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MainLayer2.java
 *
 * Created on 9-mar-2010, 13.56.29
 */
package freimapgsoc;

import freimapgsoc.*;
import java.net.SocketException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.awt.AWTException;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;
import org.jdesktop.application.Action;
import java.awt.geom.Point2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.jmdns.JmDNS;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JSlider;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputAdapter;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.DefaultTileFactory;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.TileFactory;
import org.jdesktop.swingx.mapviewer.TileFactoryInfo;
import org.jdesktop.swingx.mapviewer.Waypoint;
import org.jdesktop.swingx.mapviewer.WaypointPainter;
import org.jdesktop.swingx.mapviewer.WaypointRenderer;
import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.Painter;

/**
 *
 * @author Stefano
 */
public class MainLayer extends javax.swing.JFrame {

    /** Creates new form MainLayer */
    public MainLayer(Layer l) {
        this.l = l;
        //public TileFactoryInfo(int minimumZoomLevel,int maximumZoomLevel,int totalMapZoom,int tileSize,boolean xr2l,boolean yt2b,String baseURL,String xparam,String yparam,String zparam)(
        TileFactoryInfo info = new TileFactoryInfo(0, maxzoomlevel, totalmapzoom, 256, false, false, "http://tile.openstreetmap.org", "x", "y", "z") {

            public String getTileUrl(int x, int y, int zoom) {
                zoom = maxzoomlevel - zoom;
                return this.baseURL + "/" + zoom + "/" + x + "/" + y + ".png";
            }
        };

        final int maxzoomlevel = 14;
        final int totalmapzoom = 14;

        //In a future: possibilty to change this with settings menu parameters; now is in Italy Rome
        tf = new DefaultTileFactory(info);
        double default_lat = 41.86378;
        double default_lon = 12.5534744;
        def = new GeoPosition(default_lat, default_lon);
        initComponents();
        initMapComponents();
        printDateTime();
        initData();
        drawAll(l.getCurrentLinks(), l.getCurrentNodes());
        System.out.println("NodeList:" + l.getCurrentNodes());
        System.out.println("LinksList:" + l.getCurrentLinks());
        writeLastAvailableTime();
        writefirstAvailableTime();
    }

    public void initData() {
        try {
            for (int i = 0; i < l.getCurrentNodes().size(); i++) {
                System.out.println(l.getCurrentNodes().elementAt(i).toString());
                listOfNodes.add(i, l.getCurrentNodes().elementAt(i).toString());
                MouseListener mouseListener = new MouseAdapter() {

                    public void mouseClicked(MouseEvent mouseEvent) {
                        JList nodeList = (JList) mouseEvent.getSource();
                        if (mouseEvent.getClickCount() == 2) {
                            int index = nodeList.locationToIndex(mouseEvent.getPoint());
                            if (index >= 0) {
                                getInfoFromSelectedNodes(index);
                            }
                        }
                    }
                };
                nodeList.addMouseListener(mouseListener);
                //Select the node on the map and getInfo!
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            log.append(dateInfo.getText() + ": Problem with loading components in MainLayer.java: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void getInfoFromSelectedNodes(int index) {
        try {
            String selNodeName = listOfNodes.getElementAt(index).toString();
            System.out.println("selNodeName:" + listOfNodes.getElementAt(index));
            MapNode selectedNode = l.getNodeByName(selNodeName);
            System.out.println(l.getNodeByName(selNodeName));
            //drawSelection(double xposition, double yposition);
            System.out.println("Lat: " + selectedNode.lat);
            System.out.println("Lon: " + selectedNode.lon);
            latitudeValue.setText(Double.toString(selectedNode.lat));
            longitudeValue.setText(Double.toString(selectedNode.lon));
            upTimeValue.setText(selectedNode.uptime);
            ipComboBox.removeAllItems();
            ipComboBox.insertItemAt(selectedNode.ip, 0);
            for (int i = 0; i < selectedNode.inter.size(); i++) {
                ipComboBox.insertItemAt(selectedNode.inter.elementAt(i), i + 1);
            }
            ipComboBox.setSelectedIndex(0);
        } catch (Exception e) {
            log.append("Exeption:" + e.getMessage() + " caused by: " + e.getCause() + "was occured in class: " + e.getClass());
        }
    }

    public void storeLatLon(Vector<MapNode> nodes) {
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

    public void drawNodes(Vector<MapNode> nodes) {

        for (int i = 0; i < nodes.size(); i++) {
            final GeoPosition posNode = new GeoPosition(nodes.elementAt(i).lat, nodes.elementAt(i).lon);
            final JButton waynode = new JButton(nodes.elementAt(i).toString());
            waynode.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    System.out.println("The test2 button was clicked");
                }
            });
            waypoints.add(new SwingWaypoint(waynode, posNode));
            painter.setRenderer(new WaypointRenderer() {

                public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {
                    g.setColor(Color.ORANGE);
                    JComponent component = ((SwingWaypoint) wp).getComponent();
                    Point2D gp_pt = map.getTileFactory().geoToPixel(wp.getPosition(), map.getZoom());
                    Rectangle rect = map.getViewportBounds();
                    Point pt = new Point((int) gp_pt.getX() - rect.x, (int) gp_pt.getY() - rect.y);
                    if (mainMap.getZoom() < 14 && mainMap.getZoom() > 7) {
                        component.setLocation(pt);
                        g.fillOval(0, 0, 4, 4);
                    } else if (mainMap.getZoom() <= 7 && mainMap.getZoom() >= 5) {
                        component.setLocation(pt);
                        g.fillOval(0, 0, 5, 5);
                        g.setColor(Color.RED);
                        g.draw(new Ellipse2D.Double(-5.0, -5.0, 15.0, 15.0));
                    } else if (mainMap.getZoom() == 4) {
                        component.setLocation(pt);
                        g.fillOval(0, 0, 6, 6);
                        g.setColor(Color.RED);
                        g.draw(new Ellipse2D.Double(-5.5, -5.5, 15.0, 15.0));
                    } else {
                        component.setLocation(pt);
                        g.fillOval(0, 0, 7, 7);
                        g.setColor(Color.RED);
                        BasicStroke stroke = new BasicStroke(1.0f);
                        g.setStroke(stroke);
                        g.draw(new Ellipse2D.Double(-7.0, -7.0, 20.0, 20.0));
                    }
                    return true;
                }
            });
            painter.setWaypoints(waypoints);
            mainMap.setOverlayPainter(painter);

        }
    }
    //}

    /*public void drawNodes(Vector<MapNode> nodes, Double lat, Double lon) {
    for (int i = 0; i < nodes.size(); i++) {
    GeoPosition posNode = new GeoPosition(nodes.elementAt(i).lat, nodes.elementAt(i).lon);
    if (nodes.elementAt(i).lat == lat && nodes.elementAt(i).lon == lon) {
    waypoints.add(new Waypoint(posNode));
    painter.setWaypoints(waypoints);
    painter.setRenderer(new WaypointRenderer() {
    public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {
    g.setColor(Color.ORANGE);
    if (mainMap.getZoom() < 17 && mainMap.getZoom() > 7) {
    g.setColor(Color.RED);
    g.drawLine(-2, -2, +2, +2);
    g.drawLine(-2, +2, +2, -2);
    return true;
    } else if (mainMap.getZoom() <= 7 && mainMap.getZoom() >= 5) {
    g.setColor(Color.RED);
    g.drawLine(-3, -3, +3, +3);
    g.drawLine(-3, +3, +3, -3);
    return true;
    } else if (mainMap.getZoom() == 4) {
    g.setColor(Color.RED);
    g.drawLine(-4, -4, +4, +4);
    g.drawLine(-4, +4, +4, -4);
    return true;
    } else {
    g.setColor(Color.RED);
    g.drawLine(-5, -5, +5, +5);
    g.drawLine(-5, +5, +5, -5);
    return true;
    }
    }
    });
    } else {
    final JButton waynode = new JButton(nodes.elementAt(i).toString());
    waypoints.add(new SwingWaypoint(waynode, posNode));
    painter.setWaypoints(waypoints);
    painter.setRenderer(new WaypointRenderer() {

    public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {
    map.add(waynode);
    g.setColor(Color.ORANGE);
    if (mainMap.getZoom() < 14 && mainMap.getZoom() > 7) {
    g.fillOval(0, 0, 4, 4);
    } else if (mainMap.getZoom() <= 7 && mainMap.getZoom() >= 5) {
    g.fillOval(0, 0, 5, 5);
    g.setColor(Color.RED);
    g.draw(new Ellipse2D.Double(-5.0, -5.0, 15.0, 15.0));
    } else if (mainMap.getZoom() == 4) {
    g.fillOval(0, 0, 6, 6);
    g.setColor(Color.RED);
    g.draw(new Ellipse2D.Double(-5.5, -5.5, 15.0, 15.0));
    } else {
    g.fillOval(0, 0, 7, 7);
    g.setColor(Color.RED);
    BasicStroke stroke = new BasicStroke(1.0f);
    g.setStroke(stroke);
    g.draw(new Ellipse2D.Double(-7.0, -7.0, 20.0, 20.0));
    }
    return true;
    }
    });
    }


    }
    mainMap.setOverlayPainter(painter);

    }
     *
     */
    //TO IMPLEMENT IT DOESN'T WORK
    public void drawLinks(Vector<Link> links) {
        for (int i = 0; i < links.size(); i++) {
            GeoPosition posFrom = new GeoPosition(links.elementAt(i).source.lat, links.elementAt(i).source.lon);
            System.out.println("LinksSourceLAT:" + links.elementAt(i).source.lat);
            System.out.println("LinksSourceLON:" + links.elementAt(i).source.lon);
            GeoPosition posTo = new GeoPosition(links.elementAt(i).dest.lat, links.elementAt(i).dest.lon);
            System.out.println("LinksDestLAT:" + links.elementAt(i).dest.lat);
            System.out.println("LinksDestLON:" + links.elementAt(i).dest.lon);
            final Point2D ptFrom = mainMap.getTileFactory().geoToPixel(posFrom, mainMap.getZoom());
            final Point2D ptTo = mainMap.getTileFactory().geoToPixel(posTo, mainMap.getZoom());
            System.out.println("PtFrom:" + ptFrom);
            System.out.println("PtTo:" + ptTo);
            Rectangle rect = mainMap.getViewportBounds();

            final Point pt_gpFrom = new Point((int) ptFrom.getX() - rect.x, (int) ptFrom.getY() - rect.y);
            final Point pt_gpTo = new Point((int) ptTo.getX() - rect.x, (int) ptTo.getY() - rect.y);
            System.out.println("pt_gpFrom:" + pt_gpFrom);
            System.out.println("pt_gpTo:" + pt_gpTo);
            linkwaypoints.add(new Waypoint(posFrom));
            linkwaypoints.add(new Waypoint(posTo));
            painter = new WaypointPainter();
            painter.setWaypoints(linkwaypoints);
            painter.setRenderer(new WaypointRenderer() {

                public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {

                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setPaint(Color.RED);
                    g.draw(new Line2D.Double(pt_gpFrom.getX(), pt_gpTo.getY(), 5.0, 5.0));
                    //    g.setColor(Color.RED);
                    //System.out.println("ptFrom: " + pt_gpFrom.getX());
                    //System.out.println("ptFrom: " + pt_gpFrom.getY());
                    //System.out.println("ptTo:" + pt_gpTo.getX());
                    //System.out.println("ptTo:" + pt_gpTo.getY());
                    //g.setStroke(new BasicStroke(3.0f));

                    //g.drawLine((int) ptFrom.getX(), (int) ptFrom.getY(), (int) ptTo.getX(), (int) ptTo.getY());
                    //g.draw(null);
                    return true;
                }
            });
            mainMap.setOverlayPainter(painter);
        }
    }

    public void drawAll(Vector<Link> links, Vector<MapNode> nodes) {
        try {
            if (links.size() == 0) {
                if (nodes.size() != 0) {
                    //Draw Links
                    drawNodes(nodes);
                } else if (nodes.size() == 0) {
                    if (countPop == 0) {
                        new InfoPopUp("There aren't DataSource file to draw!", "Open a file from File -> Open menu", "APPROVE").setVisible(true);
                        log.append("PopUp Message: There aren't DataSource file to draw! Open a file from File -> Open menu  ");
                        countPop = 1;
                    }
                }
            } else {
                if (nodes.size() == 0) {
                    if (countPop == 0) {
                        new InfoPopUp("There aren't DataSource file to draw", "Open a file from File -> Open menu", "APPROVE").setVisible(true);
                        log.append("PopUp Message: There aren't DataSource file to draw Open a file from File -> Open menu  ");
                        countPop = 1;
                    }
                } else if (nodes.size() != 0) {
                    //Draw Nodes and Links
                    drawLinks(links);
                    drawNodes(nodes);
                }
            }

        } catch (Exception e) {
            log.append("PopUp Message: There aren't input file to draw! Open a file from File -> Open menu  ");
        }
    }

    public void printDateTime() {
        Format formatter = new SimpleDateFormat("EEE, dd/MM/yyyy");
        String today = formatter.format(new Date());
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
        Date now = new Date();
        String strTime = sdfTime.format(now);
        dateInfo.setText(today + " - " + strTime);
//        dateInfo.setText("Today : " + today + " - " + strTime);

    }

    public boolean nodeIsPresent(String nodeName) {
        boolean find = false;
        for (int i = 0; i < currentDataS.getNodeList().size(); i++) {
            System.out.println("node name:" + currentDataS.getNodeList().elementAt(i).toString());
            if (currentDataS.getNodeList().elementAt(i).toString().equals(nodeName)) {
                System.out.println("node is present!");
                find = true;
            } else {
                find = false;
            }
        }
        return find;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        contestMenu = new javax.swing.JPopupMenu();
        addNode = new javax.swing.JMenuItem();
        centerMap = new javax.swing.JMenuItem();
        goToDefaultPosition = new javax.swing.JMenuItem();
        zoomInHere = new javax.swing.JMenuItem();
        zoomOutHere = new javax.swing.JMenuItem();
        contestMenuNode = new javax.swing.JPopupMenu();
        jCheckBoxMenuItem1 = new javax.swing.JCheckBoxMenuItem();
        deleteNode = new javax.swing.JMenuItem();
        selectThisNode = new javax.swing.JMenuItem();
        serviceDiscovery = new javax.swing.JMenuItem();
        SSH = new javax.swing.JMenuItem();
        Telnet = new javax.swing.JMenuItem();
        SNMP = new javax.swing.JMenuItem();
        jPanel1 = new javax.swing.JPanel();
        longitudeValue = new javax.swing.JLabel();
        lonLabel = new javax.swing.JLabel();
        latLabel = new javax.swing.JLabel();
        xValue1 = new javax.swing.JLabel();
        yValue = new javax.swing.JLabel();
        latitudeValue = new javax.swing.JLabel();
        locatedNodes = new javax.swing.JScrollPane();
        nodeList = new JList(listOfNodes);
        fqidLabel = new javax.swing.JLabel();
        ipLabel = new javax.swing.JLabel();
        ipComboBox = new javax.swing.JComboBox();
        locatedLabel1 = new javax.swing.JLabel();
        upTimeValue = new javax.swing.JLabel();
        fqidValue = new javax.swing.JLabel();
        jSeparator4 = new javax.swing.JSeparator();
        rtLat = new javax.swing.JLabel();
        rtLogitude = new javax.swing.JLabel();
        rtxPos = new javax.swing.JLabel();
        rtxPosValue = new javax.swing.JLabel();
        rtyPos = new javax.swing.JLabel();
        rtyPosValue = new javax.swing.JLabel();
        rtLatValue = new javax.swing.JLabel();
        rtLonValue = new javax.swing.JLabel();
        connectionValue = new javax.swing.JLabel();
        osLabel = new javax.swing.JLabel();
        ConnectionLabel = new javax.swing.JLabel();
        osValue = new javax.swing.JLabel();
        SDButton = new javax.swing.JButton();
        mapPanel = new javax.swing.JPanel();
        mainMap = new org.jdesktop.swingx.JXMapViewer();
        miniMap = new org.jdesktop.swingx.JXMapViewer();
        zoomSlider = new javax.swing.JSlider();
        zoomButtonIn = new javax.swing.JButton();
        zoomButtonOut = new javax.swing.JButton();
        hoverLabel = new javax.swing.JLabel();
        dateInfo = new javax.swing.JLabel();
        jSlider1 = new javax.swing.JSlider();
        lastAvailableTime = new javax.swing.JLabel();
        firstAvailableTime = new javax.swing.JLabel();
        elapsedTime = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        FileMenu = new javax.swing.JMenu();
        openMenu = new javax.swing.JMenu();
        xmlOpenMenu = new javax.swing.JMenuItem();
        txtOpenMenu = new javax.swing.JMenuItem();
        jsOpenMenu = new javax.swing.JMenuItem();
        AppendMenu = new javax.swing.JMenu();
        xmlAppendMenu = new javax.swing.JMenuItem();
        txtAppendMenu = new javax.swing.JMenuItem();
        jsAppendMenu = new javax.swing.JMenuItem();
        saveAsMenu = new javax.swing.JMenu();
        xmlSaveMenu = new javax.swing.JMenuItem();
        txtSaveMenu = new javax.swing.JMenuItem();
        jsSaveMenu = new javax.swing.JMenuItem();
        saveSelectedNodes = new javax.swing.JMenu();
        xmlSaveSelMenu = new javax.swing.JMenuItem();
        txtSaveSelMenu = new javax.swing.JMenuItem();
        jsSaveSelMenu = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        recentFilesMenu = new javax.swing.JMenu();
        deleteRecentMenu = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        closeItem = new javax.swing.JMenuItem();
        EditMenu = new javax.swing.JMenu();
        goToDefaultPos = new javax.swing.JMenuItem();
        goHere = new javax.swing.JMenuItem();
        addNodeItem = new javax.swing.JMenuItem();
        takeSnapItem = new javax.swing.JMenuItem();
        findNode = new javax.swing.JMenuItem();
        applyFilter = new javax.swing.JMenuItem();
        selectAll = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        preferencesItem = new javax.swing.JMenuItem();
        ViewMenu = new javax.swing.JMenu();
        mapNodesMenu = new javax.swing.JCheckBoxMenuItem();
        linksMenu = new javax.swing.JCheckBoxMenuItem();
        zoomSMenu = new javax.swing.JCheckBoxMenuItem();
        zoomButtons = new javax.swing.JCheckBoxMenuItem();
        nodesCheck = new javax.swing.JCheckBoxMenuItem();
        latLonMenu = new javax.swing.JCheckBoxMenuItem();
        miniMapMenu = new javax.swing.JCheckBoxMenuItem();
        HelpMenu = new javax.swing.JMenu();
        guideItem = new javax.swing.JMenuItem();
        aboutFreimap = new javax.swing.JMenuItem();

        contestMenu.setName("contestMenu"); // NOI18N

        addNode.setText("jMenuItem1");
        addNode.setName("addNode"); // NOI18N
        contestMenu.add(addNode);

        centerMap.setText("jMenuItem1");
        centerMap.setName("centerMap"); // NOI18N
        contestMenu.add(centerMap);

        goToDefaultPosition.setText("jMenuItem1");
        goToDefaultPosition.setName("goToDefaultPosition"); // NOI18N
        contestMenu.add(goToDefaultPosition);

        zoomInHere.setText("jMenuItem1");
        zoomInHere.setName("zoomInHere"); // NOI18N
        contestMenu.add(zoomInHere);

        zoomOutHere.setText("jMenuItem1");
        zoomOutHere.setName("zoomOutHere"); // NOI18N
        contestMenu.add(zoomOutHere);

        contestMenuNode.setName("contestMenuNode"); // NOI18N

        jCheckBoxMenuItem1.setSelected(true);
        jCheckBoxMenuItem1.setText("jCheckBoxMenuItem1");
        jCheckBoxMenuItem1.setName("jCheckBoxMenuItem1"); // NOI18N
        contestMenuNode.add(jCheckBoxMenuItem1);

        deleteNode.setText("jMenuItem1");
        deleteNode.setName("deleteNode"); // NOI18N
        contestMenuNode.add(deleteNode);

        selectThisNode.setText("jMenuItem1");
        selectThisNode.setName("selectThisNode"); // NOI18N
        contestMenuNode.add(selectThisNode);

        serviceDiscovery.setText("jMenuItem1");
        serviceDiscovery.setName("serviceDiscovery"); // NOI18N
        contestMenuNode.add(serviceDiscovery);

        SSH.setText("jMenuItem1");
        SSH.setName("SSH"); // NOI18N
        contestMenuNode.add(SSH);

        Telnet.setText("jMenuItem1");
        Telnet.setName("Telnet"); // NOI18N
        contestMenuNode.add(Telnet);

        SNMP.setText("jMenuItem1");
        SNMP.setName("SNMP"); // NOI18N
        contestMenuNode.add(SNMP);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel1.setName("jPanel1"); // NOI18N

        longitudeValue.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        longitudeValue.setText(" ");
        longitudeValue.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        longitudeValue.setName("longitudeValue"); // NOI18N

        lonLabel.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        lonLabel.setText("Longitude:");
        lonLabel.setName("lonLabel"); // NOI18N

        latLabel.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        latLabel.setText("Latitude:");
        latLabel.setName("latLabel"); // NOI18N

        xValue1.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        xValue1.setText("HNA:");
        xValue1.setName("xValue1"); // NOI18N

        yValue.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        yValue.setText(" ");
        yValue.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        yValue.setName("yValue"); // NOI18N

        latitudeValue.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        latitudeValue.setText(" ");
        latitudeValue.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        latitudeValue.setName("latitudeValue"); // NOI18N

        locatedNodes.setName("locatedNodes"); // NOI18N

        nodeList.setName("nodeList"); // NOI18N
        locatedNodes.setViewportView(nodeList);

        fqidLabel.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        fqidLabel.setText("FQID:");
        fqidLabel.setName("fqidLabel"); // NOI18N

        ipLabel.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        ipLabel.setText("Ip Address:");
        ipLabel.setName("ipLabel"); // NOI18N

        ipComboBox.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        ipComboBox.setName("ipComboBox"); // NOI18N

        locatedLabel1.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        locatedLabel1.setText("Uptime:");
        locatedLabel1.setName("locatedLabel1"); // NOI18N

        upTimeValue.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        upTimeValue.setText(" ");
        upTimeValue.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        upTimeValue.setName("upTimeValue"); // NOI18N

        fqidValue.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        fqidValue.setText(" ");
        fqidValue.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        fqidValue.setName("fqidValue"); // NOI18N

        jSeparator4.setName("jSeparator4"); // NOI18N

        rtLat.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        rtLat.setText("Latitute:");
        rtLat.setName("rtLat"); // NOI18N

        rtLogitude.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        rtLogitude.setText("Logitude:");
        rtLogitude.setName("rtLogitude"); // NOI18N

        rtxPos.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        rtxPos.setText("X:");
        rtxPos.setName("rtxPos"); // NOI18N

        rtxPosValue.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        rtxPosValue.setText("V");
        rtxPosValue.setName("rtxPosValue"); // NOI18N

        rtyPos.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        rtyPos.setText("Y:");
        rtyPos.setName("rtyPos"); // NOI18N

        rtyPosValue.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        rtyPosValue.setText("V");
        rtyPosValue.setName("rtyPosValue"); // NOI18N

        rtLatValue.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        rtLatValue.setText("V");
        rtLatValue.setName("rtLatValue"); // NOI18N

        rtLonValue.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        rtLonValue.setText("V");
        rtLonValue.setName("rtLonValue"); // NOI18N

        connectionValue.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        connectionValue.setText(" ");
        connectionValue.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        connectionValue.setName("connectionValue"); // NOI18N

        osLabel.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        osLabel.setText("OS:");
        osLabel.setName("osLabel"); // NOI18N

        ConnectionLabel.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        ConnectionLabel.setText("Connection:");
        ConnectionLabel.setName("ConnectionLabel"); // NOI18N

        osValue.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        osValue.setText(" ");
        osValue.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        osValue.setName("osValue"); // NOI18N

        SDButton.setText("Service Discovery");
        SDButton.setName("SDButton"); // NOI18N
        SDButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SDButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(rtxPos, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(rtxPosValue, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 73, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(rtLat)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(rtLatValue, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 53, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(8, 8, 8)
                                .add(rtyPos, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 34, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(rtyPosValue, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE))
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(rtLogitude)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(rtLonValue, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 74, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(ipLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(ipComboBox, 0, 183, Short.MAX_VALUE))
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(fqidLabel)
                                .add(47, 47, 47)
                                .add(fqidValue, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE))
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                    .add(latLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(lonLabel))
                                .add(18, 18, 18)
                                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(longitudeValue, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
                                    .add(latitudeValue, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)))
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(osLabel)
                                    .add(ConnectionLabel)
                                    .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                        .add(locatedLabel1)
                                        .add(xValue1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(yValue, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 76, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(upTimeValue, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 76, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(osValue, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 76, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(connectionValue, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 76, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                        .add(4, 4, 4))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, locatedNodes, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE)
                    .add(jSeparator4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE)
                    .add(SDButton))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(locatedNodes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 155, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(fqidLabel)
                    .add(fqidValue, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(4, 4, 4)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(ipLabel)
                    .add(ipComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(latLabel)
                    .add(latitudeValue))
                .add(4, 4, 4)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lonLabel)
                    .add(longitudeValue, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(locatedLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 12, Short.MAX_VALUE)
                        .add(xValue1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(osLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 12, Short.MAX_VALUE)
                        .add(ConnectionLabel))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(upTimeValue, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(yValue)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(osValue, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(connectionValue)))
                .add(18, 18, 18)
                .add(SDButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(9, 9, 9)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(rtLat)
                    .add(rtLatValue)
                    .add(rtLogitude)
                    .add(rtLonValue))
                .add(31, 31, 31)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(rtxPos)
                    .add(rtxPosValue)
                    .add(rtyPosValue)
                    .add(rtyPos))
                .addContainerGap(67, Short.MAX_VALUE))
        );

        mapPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        mapPanel.setName("mapPanel"); // NOI18N

        mainMap.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        mainMap.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        mainMap.setName("mainMap"); // NOI18N
        mainMap.setTileFactory(tf);
        mainMap.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mainMapMouseClicked(evt);
            }
        });
        mainMap.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                mainMapMouseMoved(evt);
            }
        });
        mainMap.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        miniMap.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        miniMap.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        miniMap.setName("miniMap"); // NOI18N
        miniMap.setTileFactory(tf);
        miniMap.setZoom(13);
        miniMap.setZoomEnabled(false);

        org.jdesktop.layout.GroupLayout miniMapLayout = new org.jdesktop.layout.GroupLayout(miniMap);
        miniMap.setLayout(miniMapLayout);
        miniMapLayout.setHorizontalGroup(
            miniMapLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 158, Short.MAX_VALUE)
        );
        miniMapLayout.setVerticalGroup(
            miniMapLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 138, Short.MAX_VALUE)
        );

        mainMap.add(miniMap, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 380, 160, 140));

        zoomSlider.setMaximum(14);
        zoomSlider.setOrientation(javax.swing.JSlider.VERTICAL);
        zoomSlider.setValue(13);
        zoomSlider.setInverted(true);
        zoomSlider.setName("zoomSlider"); // NOI18N
        zoomSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                zoomSliderStateChanged(evt);
            }
        });
        mainMap.add(zoomSlider, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 280, 40, -1));

        zoomButtonIn.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        zoomButtonIn.setText("-");
        zoomButtonIn.setName("zoomButtonIn"); // NOI18N
        zoomButtonIn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomButtonInActionPerformed(evt);
            }
        });
        mainMap.add(zoomButtonIn, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 480, -1, -1));

        zoomButtonOut.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        zoomButtonOut.setText("+");
        zoomButtonOut.setName("zoomButtonOut"); // NOI18N
        zoomButtonOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomButtonOutActionPerformed(evt);
            }
        });
        mainMap.add(zoomButtonOut, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 250, -1, -1));

        hoverLabel.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        hoverLabel.setText(" ");
        hoverLabel.setName("hoverLabel"); // NOI18N
        mainMap.add(hoverLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 140, -1, -1));

        org.jdesktop.layout.GroupLayout mapPanelLayout = new org.jdesktop.layout.GroupLayout(mapPanel);
        mapPanel.setLayout(mapPanelLayout);
        mapPanelLayout.setHorizontalGroup(
            mapPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mapPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(mainMap, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 870, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        mapPanelLayout.setVerticalGroup(
            mapPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mapPanelLayout.createSequentialGroup()
                .add(20, 20, 20)
                .add(mainMap, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 534, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        dateInfo.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        dateInfo.setText(" ");
        dateInfo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        dateInfo.setName("dateInfo"); // NOI18N

        jSlider1.setName("jSlider1"); // NOI18N

        lastAvailableTime.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        lastAvailableTime.setText("Now");
        lastAvailableTime.setName("lastAvailableTime"); // NOI18N

        firstAvailableTime.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        firstAvailableTime.setText("Beginning");
        firstAvailableTime.setName("firstAvailableTime"); // NOI18N

        elapsedTime.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        elapsedTime.setText("Elapsed Time:");
        elapsedTime.setName("elapsedTime"); // NOI18N

        jMenuBar1.setName("jMenuBar1"); // NOI18N

        FileMenu.setText("File");
        FileMenu.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        FileMenu.setName("FileMenu"); // NOI18N

        openMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/freimapgsoc/resources/open.png"))); // NOI18N
        openMenu.setText("Open...");
        openMenu.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        openMenu.setName("openMenu"); // NOI18N

        xmlOpenMenu.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        xmlOpenMenu.setText("XML File");
        xmlOpenMenu.setName("xmlOpenMenu"); // NOI18N
        xmlOpenMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xmlOpenMenuActionPerformed(evt);
            }
        });
        openMenu.add(xmlOpenMenu);

        txtOpenMenu.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        txtOpenMenu.setText("TXT File");
        txtOpenMenu.setName("txtOpenMenu"); // NOI18N
        openMenu.add(txtOpenMenu);

        jsOpenMenu.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        jsOpenMenu.setText("JS File");
        jsOpenMenu.setName("jsOpenMenu"); // NOI18N
        openMenu.add(jsOpenMenu);

        FileMenu.add(openMenu);

        AppendMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/freimapgsoc/resources/open.png"))); // NOI18N
        AppendMenu.setText("Append...");
        AppendMenu.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        AppendMenu.setName("AppendMenu"); // NOI18N

        xmlAppendMenu.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        xmlAppendMenu.setText("From XML");
        xmlAppendMenu.setName("xmlAppendMenu"); // NOI18N
        AppendMenu.add(xmlAppendMenu);

        txtAppendMenu.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        txtAppendMenu.setText("From TXT");
        txtAppendMenu.setName("txtAppendMenu"); // NOI18N
        AppendMenu.add(txtAppendMenu);

        jsAppendMenu.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        jsAppendMenu.setText("From JS");
        jsAppendMenu.setName("jsAppendMenu"); // NOI18N
        AppendMenu.add(jsAppendMenu);

        FileMenu.add(AppendMenu);

        saveAsMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/freimapgsoc/resources/save.png"))); // NOI18N
        saveAsMenu.setText("Save as...");
        saveAsMenu.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        saveAsMenu.setName("saveAsMenu"); // NOI18N

        xmlSaveMenu.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        xmlSaveMenu.setText("XML File");
        xmlSaveMenu.setName("xmlSaveMenu"); // NOI18N
        saveAsMenu.add(xmlSaveMenu);

        txtSaveMenu.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        txtSaveMenu.setText("TXT File");
        txtSaveMenu.setName("txtSaveMenu"); // NOI18N
        saveAsMenu.add(txtSaveMenu);

        jsSaveMenu.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        jsSaveMenu.setText("JS File");
        jsSaveMenu.setName("jsSaveMenu"); // NOI18N
        saveAsMenu.add(jsSaveMenu);

        FileMenu.add(saveAsMenu);

        saveSelectedNodes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/freimapgsoc/resources/save.png"))); // NOI18N
        saveSelectedNodes.setText("Save selected nodes...");
        saveSelectedNodes.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        saveSelectedNodes.setName("saveSelectedNodes"); // NOI18N

        xmlSaveSelMenu.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        xmlSaveSelMenu.setText("XML File");
        xmlSaveSelMenu.setName("xmlSaveSelMenu"); // NOI18N
        saveSelectedNodes.add(xmlSaveSelMenu);

        txtSaveSelMenu.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        txtSaveSelMenu.setText("TXT File");
        txtSaveSelMenu.setName("txtSaveSelMenu"); // NOI18N
        saveSelectedNodes.add(txtSaveSelMenu);

        jsSaveSelMenu.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        jsSaveSelMenu.setText("JS File");
        jsSaveSelMenu.setName("jsSaveSelMenu"); // NOI18N
        saveSelectedNodes.add(jsSaveSelMenu);

        FileMenu.add(saveSelectedNodes);

        jSeparator1.setName("jSeparator1"); // NOI18N
        FileMenu.add(jSeparator1);

        recentFilesMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/freimapgsoc/resources/setting.png"))); // NOI18N
        recentFilesMenu.setText("Recent Files");
        recentFilesMenu.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        recentFilesMenu.setName("recentFilesMenu"); // NOI18N

        deleteRecentMenu.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        deleteRecentMenu.setText("Delete Recent Files");
        deleteRecentMenu.setName("deleteRecentMenu"); // NOI18N
        recentFilesMenu.add(deleteRecentMenu);

        FileMenu.add(recentFilesMenu);

        jSeparator2.setName("jSeparator2"); // NOI18N
        FileMenu.add(jSeparator2);

        closeItem.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        closeItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/freimapgsoc/resources/close.png"))); // NOI18N
        closeItem.setText("Exit"); // NOI18N
        closeItem.setName("closeItem"); // NOI18N
        FileMenu.add(closeItem);

        jMenuBar1.add(FileMenu);

        EditMenu.setText("Edit");
        EditMenu.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        EditMenu.setName("EditMenu"); // NOI18N

        goToDefaultPos.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        goToDefaultPos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/freimapgsoc/resources/links3.png"))); // NOI18N
        goToDefaultPos.setText("Go To Default Position");
        goToDefaultPos.setName("goToDefaultPos"); // NOI18N
        EditMenu.add(goToDefaultPos);

        goHere.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        goHere.setIcon(new javax.swing.ImageIcon(getClass().getResource("/freimapgsoc/resources/gohere20.png"))); // NOI18N
        goHere.setText("Go Here");
        goHere.setName("goHere"); // NOI18N
        EditMenu.add(goHere);

        addNodeItem.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        addNodeItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/freimapgsoc/resources/add.png"))); // NOI18N
        addNodeItem.setText("Add Node");
        addNodeItem.setName("addNodeItem"); // NOI18N
        EditMenu.add(addNodeItem);

        takeSnapItem.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        takeSnapItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/freimapgsoc/resources/minimap.png"))); // NOI18N
        takeSnapItem.setText("Take Snapshot");
        takeSnapItem.setName("takeSnapItem"); // NOI18N
        EditMenu.add(takeSnapItem);

        findNode.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        findNode.setIcon(new javax.swing.ImageIcon(getClass().getResource("/freimapgsoc/resources/search.png"))); // NOI18N
        findNode.setText("Find");
        findNode.setName("findNode"); // NOI18N
        EditMenu.add(findNode);

        applyFilter.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        applyFilter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/freimapgsoc/resources/filter.png"))); // NOI18N
        applyFilter.setText("Apply Filter");
        applyFilter.setName("applyFilter"); // NOI18N
        EditMenu.add(applyFilter);

        selectAll.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        selectAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/freimapgsoc/resources/wrt.png"))); // NOI18N
        selectAll.setText("Select All Nodes");
        selectAll.setName("selectAll"); // NOI18N
        EditMenu.add(selectAll);

        jSeparator3.setName("jSeparator3"); // NOI18N
        EditMenu.add(jSeparator3);

        preferencesItem.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        preferencesItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/freimapgsoc/resources/settings.png"))); // NOI18N
        preferencesItem.setText("Preferences");
        preferencesItem.setName("preferencesItem"); // NOI18N
        EditMenu.add(preferencesItem);

        jMenuBar1.add(EditMenu);

        ViewMenu.setText("View");
        ViewMenu.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        ViewMenu.setName("ViewMenu"); // NOI18N

        mapNodesMenu.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        mapNodesMenu.setSelected(true);
        mapNodesMenu.setText("Nodes");
        mapNodesMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/freimapgsoc/resources/wrt.png"))); // NOI18N
        mapNodesMenu.setName("mapNodesMenu"); // NOI18N
        ViewMenu.add(mapNodesMenu);

        linksMenu.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        linksMenu.setSelected(true);
        linksMenu.setText("Links");
        linksMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/freimapgsoc/resources/links.png"))); // NOI18N
        linksMenu.setName("linksMenu"); // NOI18N
        ViewMenu.add(linksMenu);

        zoomSMenu.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        zoomSMenu.setSelected(true);
        zoomSMenu.setText("Zoom Slider");
        zoomSMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/freimapgsoc/resources/links2.png"))); // NOI18N
        zoomSMenu.setName("zoomSMenu"); // NOI18N
        ViewMenu.add(zoomSMenu);

        zoomButtons.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        zoomButtons.setSelected(true);
        zoomButtons.setText("Zoom Buttons");
        zoomButtons.setIcon(new javax.swing.ImageIcon(getClass().getResource("/freimapgsoc/resources/zoom.png"))); // NOI18N
        zoomButtons.setName("zoomButtons"); // NOI18N
        ViewMenu.add(zoomButtons);

        nodesCheck.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        nodesCheck.setSelected(true);
        nodesCheck.setText("Node Info");
        nodesCheck.setIcon(new javax.swing.ImageIcon(getClass().getResource("/freimapgsoc/resources/info.png"))); // NOI18N
        nodesCheck.setName("nodesCheck"); // NOI18N
        ViewMenu.add(nodesCheck);

        latLonMenu.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        latLonMenu.setSelected(true);
        latLonMenu.setText("Lat/Lon");
        latLonMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/freimapgsoc/resources/latlon.png"))); // NOI18N
        latLonMenu.setName("latLonMenu"); // NOI18N
        ViewMenu.add(latLonMenu);

        miniMapMenu.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        miniMapMenu.setSelected(true);
        miniMapMenu.setText("MiniMap");
        miniMapMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/freimapgsoc/resources/minimap2.png"))); // NOI18N
        miniMapMenu.setName("miniMapMenu"); // NOI18N
        ViewMenu.add(miniMapMenu);

        jMenuBar1.add(ViewMenu);

        HelpMenu.setText("Help");
        HelpMenu.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        HelpMenu.setName("HelpMenu"); // NOI18N

        guideItem.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        guideItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/freimapgsoc/resources/help.png"))); // NOI18N
        guideItem.setText("Guide");
        guideItem.setName("guideItem"); // NOI18N
        HelpMenu.add(guideItem);

        aboutFreimap.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        aboutFreimap.setIcon(new javax.swing.ImageIcon(getClass().getResource("/freimapgsoc/resources/about.png"))); // NOI18N
        aboutFreimap.setText("About");
        aboutFreimap.setName("aboutFreimap"); // NOI18N
        HelpMenu.add(aboutFreimap);

        jMenuBar1.add(HelpMenu);

        setJMenuBar(jMenuBar1);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(dateInfo, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(mapPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(101, 101, 101)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jSlider1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 527, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(layout.createSequentialGroup()
                                .add(lastAvailableTime)
                                .add(211, 211, 211)
                                .add(elapsedTime)
                                .add(171, 171, 171)
                                .add(firstAvailableTime)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(mapPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(dateInfo)
                            .add(elapsedTime))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 21, Short.MAX_VALUE)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(firstAvailableTime)
                            .add(lastAvailableTime))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                .add(jSlider1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(79, 79, 79))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void zoomSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_zoomSliderStateChanged
        // TODO add your handling code here:
        if (!zoomChanging) {
            mainMap.setZoom(zoomSlider.getValue());
            miniMap.setZoom(zoomSlider.getValue() + 3);
            drawAll(l.getCurrentLinks(), l.getCurrentNodes());
        }
    }//GEN-LAST:event_zoomSliderStateChanged

    private void xmlOpenMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xmlOpenMenuActionPerformed
        if (evt.getSource() == xmlOpenMenu) {
            JFileChooser fcxml = new JFileChooser();
            fcxml.addChoosableFileFilter(new xmlFileFilter());
            fcxml.setAcceptAllFileFilterUsed(false);
            int returnVal = fcxml.showOpenDialog(fcxml);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                deleteAllfromMap(mainMap, locatedN);
                File file = fcxml.getSelectedFile();
                //new Layer(new xmlDataSource().init(null));
                addRecentFile(file.getPath(), file.getName());
            } else if (returnVal == JFileChooser.CANCEL_OPTION) {
                System.out.println("Open command cancelled by user." + "\n");
            }

        }
    }//GEN-LAST:event_xmlOpenMenuActionPerformed

    private void zoomButtonInActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomButtonInActionPerformed
        setZoom(mainMap.getZoom() + 1);
        setZoom(miniMap.getZoom() + 5);
    }//GEN-LAST:event_zoomButtonInActionPerformed

    private void zoomButtonOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomButtonOutActionPerformed
        setZoom(mainMap.getZoom() - 1);
        setZoom(miniMap.getZoom() - 5);// TODO add your handling code here:
    }//GEN-LAST:event_zoomButtonOutActionPerformed

    private void mainMapMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mainMapMouseMoved

        System.out.println(waypoints);

        GeoPosition gp = mainMap.convertPointToGeoPosition(new Point2D.Double(evt.getX(), evt.getY()));
        DecimalFormat fmt = new DecimalFormat("#00.000000");
        rtLatValue.setText(fmt.format(gp.getLatitude()));
        rtLonValue.setText(fmt.format(gp.getLongitude()));
        rtxPosValue.setText(String.format("%.3f", mainMap.getTileFactory().geoToPixel(gp, mainMap.getZoom()).getX()));
        rtyPosValue.setText(String.format("%.3f", mainMap.getTileFactory().geoToPixel(gp, mainMap.getZoom()).getY()));        // TODO add your handling code here:

        //convert to world bitmap
        Point2D gp_pt = mainMap.getTileFactory().geoToPixel(gp, mainMap.getZoom());
        //convert to screen
        Rectangle rect = mainMap.getViewportBounds();
        Point converted_gp_pt = new Point((int) gp_pt.getX() - rect.x,
                (int) gp_pt.getY() - rect.y);
        //check if near the mouse
        if (converted_gp_pt.distance(evt.getPoint()) < 10) {
            hoverLabel.setLocation(converted_gp_pt);
            hoverLabel.setVisible(true);
        } else {
            hoverLabel.setVisible(false);
        }

    }//GEN-LAST:event_mainMapMouseMoved

    private void mainMapMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mainMapMouseClicked
        if (evt.getClickCount() == 2) {
            mainMap.setCenter(evt.getPoint());
        }
        GeoPosition posNode = mainMap.convertPointToGeoPosition(new Point2D.Double(evt.getX(), evt.getY()));
        Point2D gp_pt = mainMap.getTileFactory().geoToPixel(posNode, mainMap.getZoom());
        //convert to screen
        Rectangle rect = mainMap.getViewportBounds();
        Point converted_gp_pt = new Point((int) gp_pt.getX() - rect.x, (int) gp_pt.getY() - rect.y);
        //check if near the mouse
        if (converted_gp_pt.distance(evt.getPoint()) < 10 && isNearNode(posNode)) {
            System.out.println("OK SEI SUL NODO!");
        }
    }//GEN-LAST:event_mainMapMouseClicked

    private void SDButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SDButtonActionPerformed
        try {
            ServiceDiscovery sd = new ServiceDiscovery();
        } catch (SocketException ex) {
            Logger.getLogger(MainLayer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_SDButtonActionPerformed

    public boolean isNearNode(GeoPosition posNode) {
        for (int i = 0; i < l.getCurrentNodes().size(); i++) {
            if (l.getCurrentNodes().elementAt(i).lat == posNode.getLatitude() + 10 && l.getCurrentNodes().elementAt(i).lon == posNode.getLongitude() + 10) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    /**
     * @param args the command line arguments
     */
    //MAP COMPONENTS
    public void initMapComponents() {
        mainMap.setCenterPosition(new GeoPosition(0, 0));
        miniMap.setCenterPosition(new GeoPosition(0, 0));
        mainMap.setRestrictOutsidePanning(true);
        miniMap.setRestrictOutsidePanning(true);

        Set<Waypoint> wps = new HashSet<Waypoint>();
        WaypointPainter wp = new WaypointPainter();
        wp.setWaypoints(wps);

        mainMap.setOverlayPainter(new CompoundPainter(new Painter<JXMapViewer>() {

            @Override
            public void paint(Graphics2D g, JXMapViewer map, int width, int height) {
                g.setPaint(Color.WHITE);
                g.drawString(" ", 50, map.getHeight() - 10);
            }
        }, wp));


        // adapter to move the minimap after the main map has moved
        MouseInputAdapter ma = new MouseInputAdapter() {

            @Override
            public void mousePressed(MouseEvent evt) {
                String nodeName = "";
                Point pt = evt.getPoint();
                GeoPosition gp = mainMap.convertPointToGeoPosition(new Point2D.Double(evt.getX(), evt.getY()));
                //Se la posizione del mouse è uguale a una presente nel vettore latlon allora prendi il nome e visualizzalo

                coor.add(String.format("%.2f", gp.getLatitude()));
                coor.add(String.format("%.2f", gp.getLongitude()));
                //System.out.println("latlon.get(coor): " + latlon.get(coor));
                //System.out.println("evt.getPoint():" + evt.getPoint());
                //System.out.println("pt:" + pt);
                Point2D gp_pt = mainMap.getTileFactory().geoToPixel(gp, mainMap.getZoom());
                Rectangle rect = mainMap.getViewportBounds();
                Point converted_gp_pt = new Point((int) pt.getX() - rect.x, (int) pt.getY() - rect.y);

            }

            @Override
            public void mouseReleased(MouseEvent e) {
                miniMap.setCenterPosition(mapCenterPosition);
            }
        };


        mainMap.addMouseMotionListener(ma);
        mainMap.addMouseListener(ma);
        mainMap.addPropertyChangeListener("center", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                Point2D mapCenter = (Point2D) evt.getNewValue();
                TileFactory tf = mainMap.getTileFactory();
                GeoPosition mapPos = tf.pixelToGeo(mapCenter, mainMap.getZoom());
                miniMap.setCenterPosition(mapPos);
            }
        });


        mainMap.addPropertyChangeListener("centerPosition", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                mapCenterPosition = (GeoPosition) evt.getNewValue();
                miniMap.setCenterPosition(mapCenterPosition);
                Point2D pt = miniMap.getTileFactory().geoToPixel(mapCenterPosition, miniMap.getZoom());
                miniMap.setCenter(pt);
                miniMap.repaint();
            }
        });

        mainMap.addPropertyChangeListener("zoom", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                zoomSlider.setValue(mainMap.getZoom());
            }
        });


        miniMap.setOverlayPainter(new Painter<JXMapViewer>() {

            @Override
            public void paint(Graphics2D g, JXMapViewer map, int width, int height) {
                // get the viewport rect of the main map
                Rectangle mainMapBounds = mainMap.getViewportBounds();

                // convert to Point2Ds
                Point2D upperLeft2D = mainMapBounds.getLocation();
                Point2D lowerRight2D = new Point2D.Double(upperLeft2D.getX() + mainMapBounds.getWidth(),
                        upperLeft2D.getY() + mainMapBounds.getHeight());

                // convert to GeoPostions
                GeoPosition upperLeft = mainMap.getTileFactory().pixelToGeo(upperLeft2D, mainMap.getZoom());
                GeoPosition lowerRight = mainMap.getTileFactory().pixelToGeo(lowerRight2D, mainMap.getZoom());

                // convert to Point2Ds on the mini-map
                upperLeft2D =
                        map.getTileFactory().geoToPixel(upperLeft, map.getZoom());
                lowerRight2D =
                        map.getTileFactory().geoToPixel(lowerRight, map.getZoom());
                g =
                        (Graphics2D) g.create();
                Rectangle rect = map.getViewportBounds();
                //p("rect = " + rect);
                g.translate(-rect.x, -rect.y);
                Point2D centerpos = map.getTileFactory().geoToPixel(mapCenterPosition, map.getZoom());
                //p("center pos = " + centerpos);
                g.setPaint(Color.RED);
                //g.drawRect((int)centerpos.getX()-30,(int)centerpos.getY()-30,60,60);
                g.drawRect((int) upperLeft2D.getX(), (int) upperLeft2D.getY(),
                        (int) (lowerRight2D.getX() - upperLeft2D.getX()),
                        (int) (lowerRight2D.getY() - upperLeft2D.getY()));
                g.setPaint(new Color(255, 0, 0, 50));
                g.fillRect((int) upperLeft2D.getX(), (int) upperLeft2D.getY(),
                        (int) (lowerRight2D.getX() - upperLeft2D.getX()),
                        (int) (lowerRight2D.getY() - upperLeft2D.getY()));
                //g.drawOval((int)lowerRight2D.getX(),(int)lowerRight2D.getY(),1,1);
                g.dispose();
            }
        });

        setZoom(12);// HACK joshy: hack, i shouldn't need this here
        this.setCenterPosition(new GeoPosition(0, 0));
    }//OK
    //END OF INITMAPCOMPONENTS()

    //MAP METHODS ##################################
    public void setZoom(int zoom) {
        zoomChanging = true;
        mainMap.setZoom(zoom);
        miniMap.setZoom(mainMap.getZoom() + 5);
        if (sliderReversed) {
            zoomSlider.setValue(zoomSlider.getMaximum() - zoom);
        } else {
            zoomSlider.setValue(zoom);
            mainMap.repaint();
        }

        zoomChanging = false;
    } //OK

    /**
     * Indicates if the mini-map is currently visible
     * @return the current value of the mini-map property
     */
    public boolean isMiniMapVisible() {
        return miniMapVisible;
    }//OK

    /**
     * Sets if the mini-map should be visible
     * @param miniMapVisible a new value for the miniMap property
     */
    public void setMiniMapVisible(boolean miniMapVisible) {
        boolean old = this.isMiniMapVisible();
        MainLayer.miniMapVisible = miniMapVisible;
        miniMap.setVisible(miniMapVisible);
    }//OK

    /**
     * Indicates if the zoom slider is currently visible
     * @return the current value of the zoomSliderVisible property
     */
    public boolean isZoomSliderVisible() {
        return zoomSliderVisible;
    }//OK

    /**
     * Sets if the zoom slider should be visible
     * @param zoomSliderVisible the new value of the zoomSliderVisible property
     */
    public void setZoomSliderVisible(boolean zoomSliderVisible) {
        boolean old = this.isZoomSliderVisible();
        MainLayer.zoomSliderVisible = zoomSliderVisible;
        zoomSlider.setVisible(zoomSliderVisible);
    }//OK

    /**
     * Indicates if the zoom buttons are visible. This is a bound property
     * and can be listed for using a PropertyChangeListener
     * @return current value of the zoomButtonsVisible property
     */
    public boolean isZoomButtonsVisible() {
        return zoomButtonsVisible;
    }//OK

    /**
     * Sets if the zoom buttons should be visible. This ia bound property.
     * Changes can be listened for using a PropertyChaneListener
     * @param zoomButtonsVisible new value of the zoomButtonsVisible property
     */
    public void setZoomButtonsVisible(boolean zoomButtonsVisible) {
        boolean old = this.isZoomButtonsVisible();
        MainLayer.zoomButtonsVisible = zoomButtonsVisible;
        //zoomInButton.setVisible(zoomButtonsVisible);
        //zoomOutButton.setVisible(zoomButtonsVisible);
    }//OK

    /**
     * Sets the tile factory for both embedded JXMapViewer components.
     * Calling this method will also reset the center and zoom levels
     * of both maps, as well as the bounds of the zoom slider.
     * @param fact the new TileFactory
     */
    public void setTileFactory(TileFactory fact) {
        mainMap.setTileFactory(fact);
        mainMap.setZoom(fact.getInfo().getDefaultZoomLevel());
        mainMap.setCenterPosition(new GeoPosition(0, 0));
        miniMap.setTileFactory(fact);
        miniMap.setZoom(fact.getInfo().getDefaultZoomLevel() + 3);
        miniMap.setCenterPosition(new GeoPosition(0, 0));
        zoomSlider.setMinimum(fact.getInfo().getMinimumZoomLevel());
        zoomSlider.setMaximum(fact.getInfo().getMaximumZoomLevel());
    }//OK

    public void setCenterPosition(GeoPosition pos) {
        mainMap.setCenterPosition(pos);
        miniMap.setCenterPosition(pos);
    }//OK

    /**
     * Returns a reference to the main embedded JXMapViewer component
     * @return the main map
     */
    public JXMapViewer getMainMap() {
        return this.mainMap;
    }//OK

    /**
     * Returns a reference to the mini embedded JXMapViewer component
     * @return the minimap JXMapViewer component
     */
    public JXMapViewer getMiniMap() {
        return this.miniMap;
    }//OK

    /**
     * returns a reference to the zoom in button
     * @return a jLabel
     */
    public JButton getZoomInButton() {
        return this.zoomButtonIn;
    }//OK

    /**
     * returns a reference to the zoom out button
     * @return a jLabel
     */
    public JButton getZoomOutButton() {
        return this.zoomButtonOut;
    }//OK

    /**
     * returns a reference to the zoom slider
     * @return a jslider
     */
    public JSlider getZoomSlider() {
        return this.zoomSlider;
    }//OK

//Get String Latitude from the Map
    private String getLat(GeoPosition pos) {
        Double lat = pos.getLatitude();
        return lat.toString();
    }

//Get String Longitude from the Map
    private String getLon(GeoPosition pos) {
        Double lon = pos.getLatitude();
        return lon.toString();
    }

    public void goToDefaultPosition() {
        mainMap.setAddressLocation(def);
    }

    public void takeScreenShot() throws AWTException, IOException {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Point p = new Point(mainMap.getLocationOnScreen());
        Dimension d = new Dimension(mainMap.getSize());
        Rectangle mapPosition = new Rectangle(p, d);
        Robot robot = new Robot();
        BufferedImage image = robot.createScreenCapture(mapPosition);
        ImageIO.write(image, "jpg", new File("/tmp/freimapSnapShot.jpg"));
        //new InfoPopUp("Screenshot is in /tmp/ directory", "APPROVE").setVisible(true);
    }

//END OF MAP METHODS################################
    private void serviceDActionPerformed(java.awt.event.ActionEvent evt) {
        InetAddress intf = null;
        try {
            intf = InetAddress.getByName("10.0.1.29");
            try {
                new ServiceDiscovery(JmDNS.create(intf)).setVisible(true);
            } catch (IOException ex) {
                Logger.getLogger(MainLayer.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(SDMain.class.getName()).log(Level.SEVERE, null, ex);
        }        // TODO add your handling code here:
    }

    private void deleteAllfromMap(JXMapViewer map, DefaultListModel locatedN) {
        locatedN.removeAllElements();
        Set<Waypoint> remover = painter.getWaypoints();
        remover.clear();
        map.setOverlayPainter(painter);
    }

    public boolean verifyRecentFile() {
        String recentPath = "/Users/Stefano/Desktop/Freimap/hg/src/Data/.recentMenu";
        recentFile = new File(recentPath);
        if (recentFile.exists()) {
            return true;
        } else {
            return false;
        }
    }

    private void newRecentFileActionPerformed(java.awt.event.ActionEvent evt, String path) {
        deleteAllfromMap(mainMap, locatedN);
        File file = new File(path);
        String extension = Utils.getExtension(file);
        if (extension.equals(Utils.xml)) {
//                                new Layer(new xmlDataSource().init(null));
        }
        if (extension.equals(Utils.js)) {
            new LatLonJsDataSource(path);
        }
    }

    /** DELETED BECUASE IS SIMILAR TO XML FILES*/
    private void jsOpenMenuActionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getSource() == jsOpenMenu) {
            JFileChooser fcjs = new JFileChooser();
            fcjs.addChoosableFileFilter(new jsFileFilter());
            fcjs.setAcceptAllFileFilterUsed(false);
            int returnVal = fcjs.showOpenDialog(fcjs);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                deleteAllfromMap(mainMap, locatedN);

                File file = fcjs.getSelectedFile();
                System.out.println("FILE JS OPENED");
                System.out.println("Opening: " + file.getName() + ".\n");
                new LatLonJsDataSource(file.getPath());
                addRecentFile(file.getPath(), file.getName());
            } else if (returnVal == JFileChooser.CANCEL_OPTION) {
                System.out.println("Open command cancelled by user." + "\n");
            }

        }
    }

    private void saveAsMenuActionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getSource() == saveAsMenu) {
            JFileChooser fcsave = new JFileChooser();
            fcsave.addChoosableFileFilter(new saveFileFilter());
            fcsave.setAcceptAllFileFilterUsed(false);

            int returnVal = fcsave.showSaveDialog(fcsave);
            //Handle open button action.

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fcsave.getSelectedFile();
                System.out.println("FILE SAVED CORRECT!");
                //Create a File With all Links and Nodes Data
                System.out.println("Saved: " + file.getName() + ".\n");
            } else {
                System.out.println("Save command cancelled by user." + "\n");
            }

        }
    }

    private void addNodeButtonActionPerformed(java.awt.event.ActionEvent evt) {
        // new addNode(locatedN, nodes).setVisible(true); ######################## TO IMPLEMENT
    }

    // #################################  IMPLEMENT ME ##############################
   /*
    private void deleteNodeButtonActionPerformed(java.awt.event.ActionEvent evt) {
    try {
    int i = locatedNodes.getSelectedIndex();
    System.out.println("Index: " + locatedNodes.getSelectedIndex());
    System.out.println("Removed Element: " + locatedN.elementAt(i));
    for (int j = 0; j < nodes.size(); j++) {
    if (nodes.elementAt(i).equals(locatedNodes.getSelectedValue())) {
    System.out.println(locatedNodes.getSelectedValue().toString());
    drawNodes(nodes, nodes.elementAt(i).lat, nodes.elementAt(i).lon);
    nodes.remove(j);
    locatedN.remove(i);
    System.out.println("removed!");
    }

    }

    } catch (Exception e) {
    log.append("Exeption:" + e.getMessage() + " caused by: " + e.getCause() + "was occured in class: " + e.getClass());
    }
    }

     */
    private void xmlAppendMenuActionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getSource() == xmlAppendMenu) {
            JFileChooser fcxml = new JFileChooser();
            fcxml.addChoosableFileFilter(new xmlFileFilter());
            fcxml.setAcceptAllFileFilterUsed(false);
            int returnVal = fcxml.showOpenDialog(fcxml);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fcxml.getSelectedFile();
                System.out.println("FILE XML OPENED!");
                //new Layer(new xmlDataSource().init(file.getPath()));
                addRecentFile(file.getPath(), file.getName());
                System.out.println("Opening: " + file.getName() + ".\n");
            } else if (returnVal == JFileChooser.CANCEL_OPTION) {
                System.out.println("Open command cancelled by user." + "\n");
            }

        }            // TODO add your handling code here:
    }

    /**DELETED BECAUSE IS SIMILAR TO XML FILE*/
    private void jsAppendMenuActionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getSource() == jsAppendMenu) {
            JFileChooser fcjs = new JFileChooser();
            fcjs.addChoosableFileFilter(new jsFileFilter());
            fcjs.setAcceptAllFileFilterUsed(false);
            int returnVal = fcjs.showOpenDialog(fcjs);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fcjs.getSelectedFile();
                System.out.println("FILE JS OPENED");
                //OPEN A FILE AND RELOAD ALL DATA ON THE MAP!
                System.out.println("Opening: " + file.getName() + ".\n");
                //UpdateCurrent Layer not Create e NEW LAYER!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                addRecentFile(file.getPath(), file.getName());
            } else if (returnVal == JFileChooser.CANCEL_OPTION) {
                System.out.println("Open command cancelled by user." + "\n");
            }

        }
    }

    public void addRecentFile(final String path, String name) {
        JMenuItem newItem = new JMenuItem(name);
        newItem.setToolTipText(path);
        recentFilesMenu.setEnabled(true);
        recentFilesMenu.add(new JMenuItem(path));
        newItem.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newRecentFileActionPerformed(evt, path);
            }
        });
    }

    private void deleteRecentMenuActionPerformed(java.awt.event.ActionEvent evt) {
        int i = 0;
        while (i < recentFilesMenu.getItemCount() - 2) {
            recentFilesMenu.remove(i);
            i = i + 1;
        }
        recentFile.delete();
        recentFilesMenu.setEnabled(false);
    }

    private void defaultButtonActionPerformed(java.awt.event.ActionEvent evt) {
        setTileFactory(tf);        // TODO add your handling code here:
    }

    public void init(HashMap<String, Object> configuration) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Vector<MapNode> getNodeList() {
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

    // this write the first timestamp for the timeline!

    public void writefirstAvailableTime() {
        try {
            firstAvailableTime.setText(new Date() + "\n");
        } catch (Exception e) {
            firstAvailableTime.setText("Error! Updated every 10 sec");
        }
    }

    //this update the elapsedTime for the timeline! (every 1 sec)
    public void elapsedTime() {
       
        try {
         long start = Long.parseLong(firstAvailableTime.getText());
         Thread.sleep(5*60*10);
         long end = Long.parseLong(lastAvailableTime.getText());
         long diff = end - start;
         elapsedTime.setText(String.valueOf(diff));
      } catch (Exception e) {
         System.out.println("Error!");
      }
        
   }


    //this update LastAvailableTime for the timeline! (every 10 sec)
    public void writeLastAvailableTime() {
      
        try {
            lastAvailableTime.setText(new Date() + "\n");
            Thread.sleep(5 * 60 * 10);
            lastAvailableTime.setText(new Date() + "\n");
        } catch (Exception e) {
            lastAvailableTime.setText("Error! Updated every 10 sec");
        }
        
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

    public MapNode getNodeByName(String id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Vector<Link> getLinks(long time) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addDataSourceListener(DataSourceListener dsl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void getLinkProfile(Link link, LinkInfo info) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void getLinkCountProfile(MapNode node, NodeInfo info) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void init(String path) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addNodeOnMap() {
        //OPEN A DIALOG Where ask lat/lon and eventually icon for the new node!
    }

    public void findNode() {
        new FindNode().setVisible(true);
    }

    public void goHere() {
        new goHere(mainMap).setVisible(true);
    }

    public void showNodes() {
        //Hide All nodes on the map
    }

    public void showLatLon() {
        if (latLonMenu.isSelected()) {
            latitudeValue.setVisible(false);
            longitudeValue.setVisible(false);
        } else {
            latitudeValue.setVisible(true);
            longitudeValue.setVisible(true);
        }

    }

    public void showZoomButton() {
        if (zoomButtonIn.isVisible()) {
            zoomButtonIn.setVisible(false);
            zoomButtonOut.setVisible(false);
        } else {
            zoomButtonIn.setVisible(true);
            zoomButtonOut.setVisible(true);
        }

    }

    public void showSlider() {
        if (zoomSMenu.isSelected()) {
            setZoomSliderVisible(true);
        } else {
            setZoomSliderVisible(false);
        }

    }

    @Action
    public void showMiniMap() {
        if (miniMap.isVisible()) {
            miniMap.setVisible(false);
            mapPanel.validate();
        } else {
            miniMap.setVisible(true);
        }

    }
    // Variables declaration - do not modify
    public javax.swing.JButton defaultButton;
    public javax.swing.JScrollPane jScrollPane1;
    public javax.swing.JMenuItem listofnodes1;//contestMenu
    public javax.swing.JLabel ncLabel;
    public javax.swing.JMenuItem takePicture;//contestMenu
    // End of variables declaration
    private JDialog aboutBox;
    private Runtime runtime;
    public DefaultListModel locatedN = new DefaultListModel();
    public DefaultListModel listOfNodes = new DefaultListModel();
    private int countPop = 0;
    private String nodeName = null;
    private File recentFile;
    private String recentFilePath;

    public MapNode getNodeById(String id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Vector<Link> getLinksFromSource(String id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Vector<Link> getLinksFromDest(String id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public MapNode getNodeByIp(String ip) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    private HashMap<Vector, String> latlon;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu AppendMenu;
    private javax.swing.JLabel ConnectionLabel;
    private javax.swing.JMenu EditMenu;
    private javax.swing.JMenu FileMenu;
    private javax.swing.JMenu HelpMenu;
    private javax.swing.JButton SDButton;
    private javax.swing.JMenuItem SNMP;
    private javax.swing.JMenuItem SSH;
    private javax.swing.JMenuItem Telnet;
    private javax.swing.JMenu ViewMenu;
    private javax.swing.JMenuItem aboutFreimap;
    private javax.swing.JMenuItem addNode;
    private javax.swing.JMenuItem addNodeItem;
    private javax.swing.JMenuItem applyFilter;
    private javax.swing.JMenuItem centerMap;
    private javax.swing.JMenuItem closeItem;
    private javax.swing.JLabel connectionValue;
    private javax.swing.JPopupMenu contestMenu;
    private javax.swing.JPopupMenu contestMenuNode;
    private javax.swing.JLabel dateInfo;
    private javax.swing.JMenuItem deleteNode;
    private javax.swing.JMenuItem deleteRecentMenu;
    private javax.swing.JLabel elapsedTime;
    private javax.swing.JMenuItem findNode;
    private javax.swing.JLabel firstAvailableTime;
    private javax.swing.JLabel fqidLabel;
    private javax.swing.JLabel fqidValue;
    private javax.swing.JMenuItem goHere;
    private javax.swing.JMenuItem goToDefaultPos;
    private javax.swing.JMenuItem goToDefaultPosition;
    private javax.swing.JMenuItem guideItem;
    private javax.swing.JLabel hoverLabel;
    private javax.swing.JComboBox ipComboBox;
    private javax.swing.JLabel ipLabel;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSlider jSlider1;
    private javax.swing.JMenuItem jsAppendMenu;
    private javax.swing.JMenuItem jsOpenMenu;
    private javax.swing.JMenuItem jsSaveMenu;
    private javax.swing.JMenuItem jsSaveSelMenu;
    private javax.swing.JLabel lastAvailableTime;
    private javax.swing.JLabel latLabel;
    private javax.swing.JCheckBoxMenuItem latLonMenu;
    private javax.swing.JLabel latitudeValue;
    private javax.swing.JCheckBoxMenuItem linksMenu;
    private javax.swing.JLabel locatedLabel1;
    private javax.swing.JScrollPane locatedNodes;
    private javax.swing.JLabel lonLabel;
    private javax.swing.JLabel longitudeValue;
    private org.jdesktop.swingx.JXMapViewer mainMap;
    private javax.swing.JCheckBoxMenuItem mapNodesMenu;
    private javax.swing.JPanel mapPanel;
    private org.jdesktop.swingx.JXMapViewer miniMap;
    private javax.swing.JCheckBoxMenuItem miniMapMenu;
    private static javax.swing.JList nodeList;
    private javax.swing.JCheckBoxMenuItem nodesCheck;
    private javax.swing.JMenu openMenu;
    private javax.swing.JLabel osLabel;
    private javax.swing.JLabel osValue;
    private javax.swing.JMenuItem preferencesItem;
    private javax.swing.JMenu recentFilesMenu;
    private javax.swing.JLabel rtLat;
    private javax.swing.JLabel rtLatValue;
    private javax.swing.JLabel rtLogitude;
    private javax.swing.JLabel rtLonValue;
    private javax.swing.JLabel rtxPos;
    private javax.swing.JLabel rtxPosValue;
    private javax.swing.JLabel rtyPos;
    private javax.swing.JLabel rtyPosValue;
    private javax.swing.JMenu saveAsMenu;
    private javax.swing.JMenu saveSelectedNodes;
    private javax.swing.JMenuItem selectAll;
    private javax.swing.JMenuItem selectThisNode;
    private javax.swing.JMenuItem serviceDiscovery;
    private javax.swing.JMenuItem takeSnapItem;
    private javax.swing.JMenuItem txtAppendMenu;
    private javax.swing.JMenuItem txtOpenMenu;
    private javax.swing.JMenuItem txtSaveMenu;
    private javax.swing.JMenuItem txtSaveSelMenu;
    private javax.swing.JLabel upTimeValue;
    private javax.swing.JLabel xValue1;
    private javax.swing.JMenuItem xmlAppendMenu;
    private javax.swing.JMenuItem xmlOpenMenu;
    private javax.swing.JMenuItem xmlSaveMenu;
    private javax.swing.JMenuItem xmlSaveSelMenu;
    private javax.swing.JLabel yValue;
    private javax.swing.JButton zoomButtonIn;
    private javax.swing.JButton zoomButtonOut;
    private javax.swing.JCheckBoxMenuItem zoomButtons;
    private javax.swing.JMenuItem zoomInHere;
    private javax.swing.JMenuItem zoomOutHere;
    private javax.swing.JCheckBoxMenuItem zoomSMenu;
    private javax.swing.JSlider zoomSlider;
    // End of variables declaration//GEN-END:variables
    //MainMap and Minimap Variables
    private GeoPosition def;//OK
    DefaultTileFactory tf = null;
    private boolean zoomChanging = false;//OK
    private JLabel nodeLabel = new JLabel();
    final List<GeoPosition> region = new ArrayList<GeoPosition>();
    public static boolean miniMapVisible = true;//OK
    public static boolean zoomSliderVisible = true;//OK
    public static boolean zoomButtonsVisible = true;//OK
    public static final boolean sliderReversed = false;//OK
    private static WaypointPainter painter = new WaypointPainter();//OK
    private static Set<Waypoint> waypoints = new HashSet<Waypoint>();//OK
    private static WaypointPainter linkpainter = new WaypointPainter();//OK
    private static Set<Waypoint> linkwaypoints = new HashSet<Waypoint>();//OK
    private MapNode uplink = new MapNode("0.0.0.0/0.0.0.0");//OK
    private Point2D mapCenter = new Point2D.Double(0, 0);//OK
    private GeoPosition mapCenterPosition = new GeoPosition(0, 0);//OK
    Vector coor = new Vector();
    private final int maxzoomlevel = 14;
    private final int totalmapzoom = 14;
    private Layer l;
    private int layercount = 0;
    private Vector<Layer> layers = new Vector<Layer>();
    private DataSource currentDataS;
}

