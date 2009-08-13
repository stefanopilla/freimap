/*
 * FreimapGSoCView.java
 */
package freimapgsoc;

import java.awt.geom.Rectangle2D;
import org.jdesktop.swingx.mapviewer.Waypoint;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import PopUp.PopUp;
import PopUp.PopUpMain;
import java.awt.AWTException;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.jmdns.JmDNS;
import javax.swing.DefaultListModel;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
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
 * The application's main frame.
 */
public class FreimapGSoCView extends FrameView implements DataSource {

    public FreimapGSoCView(SingleFrameApplication app) {
        super(app);

        final int maxzoomlevel = 14;
        final int totalmapzoom = 14;

        //public TileFactoryInfo(int minimumZoomLevel,int maximumZoomLevel,int totalMapZoom,int tileSize,boolean xr2l,boolean yt2b,String baseURL,String xparam,String yparam,String zparam)(
        TileFactoryInfo info = new TileFactoryInfo(0, maxzoomlevel, totalmapzoom, 256, false, false, "http://tile.openstreetmap.org", "x", "y", "z") {

            public String getTileUrl(int x, int y, int zoom) {
                zoom = maxzoomlevel - zoom;
                return this.baseURL + "/" + zoom + "/" + x + "/" + y + ".png";
            }
        };

        //In a future: possibilty to change this with settings menu parameters; now is in Italy Rome
        tf = new DefaultTileFactory(info);
        DEFAULT_LAT = 41.8639;
        DEFAULT_LON = 12.5535;
        def = new GeoPosition(DEFAULT_LAT, DEFAULT_LON);

        initComponents();
        initMapComponents();
        printDateTime();
        config = new Configurator();
        sources = new HashMap<String, DataSource>();
        try {
            HashMap<String, Object> ds = (HashMap<String, Object>) config.get("datasources");
            System.out.println(ds);
            HashMap<String, HashMap<String, Object>> ds2subconfig = new HashMap<String, HashMap<String, Object>>();
            Iterator<String> i = ds.keySet().iterator();

            while (i.hasNext()) {
                String id = i.next();
                HashMap<String, Object> subconfig = (HashMap<String, Object>) ds.get(id);
                @SuppressWarnings("static-access")
                String claz = config.getS("class", subconfig);
                Class<DataSource> csource = (Class<DataSource>) Class.forName(claz); //this cast cannot be checked!
                DataSource source = csource.newInstance();
                ds2subconfig.put(id, subconfig);
                sources.put(id, source);
            }

            Iterator<String> j = sources.keySet().iterator();
            while (j.hasNext()) {
                String id = j.next();
                System.out.println("id:" + id);
                DataSource source = sources.get(id);
                source.init(ds2subconfig.get(id)); //initialize datasource with configuration parameters
                System.out.println("Sources: " + sources.values());
                System.out.println("NodeList: " + source.getNodeList());


                System.out.println("LinksList: " + source.getLinks(0));

                nodes = new Vector<FreiNode>(); //list of jnow nodes
                links = new Vector<FreiLink>(); //list of know links

                for (int t = 0; t < links.size(); t++) {
                    System.out.println("Links Element(" + t + "):" + links.elementAt(t));
                }
                links = source.getLinks(0);
                nodes = source.getNodeList();
                storeLatLon(nodes);
                for (int k = 0; k < nodes.size(); k++) {
                    //System.out.println("id: " + nodes.get(k) + " lat: " + nodes.get(k).lat + " lon: " + nodes.get(k).lon);
                    locatedN.addElement(nodes.get(k));
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

                drawAll(links, nodes);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }


    }

    public void storeLatLon(Vector<FreiNode> nodes) {
        latlon = new HashMap<Vector, String>();
        for (int i = 0; i < nodes.size(); i++) {
            Vector latlon2 = new Vector();
            latlon2.add(String.format("%.2f", nodes.elementAt(i).lat));
            latlon2.add(String.format("%.2f", nodes.elementAt(i).lon));
            System.out.println("LatLon vector: " + latlon2);
            //System.out.println("Value:" + nodes.elementAt(i).toString());
            latlon.put(latlon2, nodes.elementAt(i).toString());

        }
    }

    public void drawNodes(Vector<FreiNode> nodes) {

        for (int i = 0; i < nodes.size(); i++) {
            final JButton waynode = new JButton(nodes.elementAt(i).toString());

            GeoPosition posNode = new GeoPosition(nodes.elementAt(i).lat, nodes.elementAt(i).lon);
            waypoints.add(new SwingWaypoint(waynode, posNode));
            painter.setWaypoints(waypoints);
            painter.setRenderer(new WaypointRenderer() {

                public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {
                    map.add(waynode);
                    g.setColor(Color.BLUE);
                    g.fillOval(0, 0, 10, 10);

                    return true;
                }
            });
            mainMap.setOverlayPainter(painter);
            //AddWAYPOINT
            }

    }

    public void drawLinks(Vector<FreiLink> links) {
        for (int i = 0; i < links.size(); i++) {
            //ADD LINES
        }
    }

    public void drawAll(Vector<FreiLink> links, Vector<FreiNode> nodes) {
        if (links.size() != 0) {
            if (nodes.size() == 0) {
                //Draw Links
                drawLinks(links);
            } else if (nodes.size() != 0) {
                //Draw Nodes and Links
                drawNodes(nodes);
                drawLinks(links);
            }
        } else { //THERE ARE NO LINKS AND NO NODES
            new InfoPopUp("There are no links to draw!").setVisible(true);
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

//MAP METHODS ##################################
    /**
     * Set the current zoomlevel for the main map. The minimap will
     * be updated accordingly
     * @param zoom the new zoom level
     */
    public void setZoom(int zoom) {
        zoomChanging = true;
        mainMap.setZoom(zoom);
        miniMap.setZoom(mainMap.getZoom() + 4);
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
        this.miniMapVisible = miniMapVisible;
        miniMap.setVisible(miniMapVisible);
        firePropertyChange("miniMapVisible", old, this.isMiniMapVisible());
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
        this.zoomSliderVisible = zoomSliderVisible;
        zoomSlider.setVisible(zoomSliderVisible);
        firePropertyChange("zoomSliderVisible", old, this.isZoomSliderVisible());
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
        this.zoomButtonsVisible = zoomButtonsVisible;
        //zoomInButton.setVisible(zoomButtonsVisible);
        //zoomOutButton.setVisible(zoomButtonsVisible);
        firePropertyChange("zoomButtonsVisible", old, this.isZoomButtonsVisible());
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
    public JLabel getZoomInButton() {
        return this.zoomButtonIn;
    }//OK

    /**
     * returns a reference to the zoom out button
     * @return a jLabel
     */
    public JLabel getZoomOutButton() {
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

    @Action
    public void goToDefaultPosition() {
        mainMap.setAddressLocation(def);
    }

    @Action
    public void takeScreenShot() throws AWTException, IOException {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Point p = new Point(mainMap.getLocationOnScreen());
        Dimension d = new Dimension(mainMap.getSize());
        Rectangle mapPosition = new Rectangle(p, d);
        Robot robot = new Robot();
        BufferedImage image = robot.createScreenCapture(mapPosition);
        ImageIO.write(image, "jpg", new File("/tmp/screenshot.jpg"));
        new InfoPopUp("Screenshot is in /tmp/ directory").setVisible(true);
    }

//END OF MAP METHODS################################
    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = FreimapGSoCApp.getApplication().getMainFrame();
            aboutBox =
                    new FreimapGSoCAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }

        FreimapGSoCApp.getApplication().show(aboutBox);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        mapPanel = new javax.swing.JPanel();
        mainMap = new org.jdesktop.swingx.JXMapViewer();
        mainMap.add(nodeLabel);
        miniMap = new org.jdesktop.swingx.JXMapViewer();
        zoomSlider = new javax.swing.JSlider();
        zoomButtonIn = new javax.swing.JLabel();
        zoomButtonOut = new javax.swing.JLabel();
        serviceD = new javax.swing.JButton();
        goToDefaultPosition = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        locatedNodes = new JList(locatedN);
        dateInfo = new javax.swing.JLabel();
        yValue = new javax.swing.JLabel();
        yPos = new javax.swing.JLabel();
        xPos = new javax.swing.JLabel();
        xValue = new javax.swing.JLabel();
        latitudeValue = new javax.swing.JLabel();
        longitudeValue = new javax.swing.JLabel();
        Longitude = new javax.swing.JLabel();
        Latitude = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        ipLabel = new javax.swing.JLabel();
        latLabel = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        locatedLabel = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        fqidLabel = new javax.swing.JLabel();
        lonLabel = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        ncLabel = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        jMenu1 = new javax.swing.JMenu();
        xmlOpenMenu = new javax.swing.JMenuItem();
        txtOpenMenu = new javax.swing.JMenuItem();
        jsOpenMenu = new javax.swing.JMenuItem();
        saveAsMenu = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jSeparator1 = new javax.swing.JSeparator();
        jMenuItem8 = new javax.swing.JMenuItem();
        jMenuItem6 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        jMenuItem12 = new javax.swing.JMenuItem();
        jMenuItem7 = new javax.swing.JMenuItem();
        jMenuItem9 = new javax.swing.JMenuItem();
        viewMenu = new javax.swing.JMenu();
        mapNodesMenu = new javax.swing.JCheckBoxMenuItem();
        linksMenu = new javax.swing.JCheckBoxMenuItem();
        latLonMenu = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxMenuItem1 = new javax.swing.JCheckBoxMenuItem();
        zoomButtons = new javax.swing.JCheckBoxMenuItem();
        zoomSMenu = new javax.swing.JCheckBoxMenuItem();
        miniMapMenu = new javax.swing.JCheckBoxMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        ContestMenu = new javax.swing.JPopupMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        shMiniMap = new javax.swing.JCheckBoxMenuItem();
        shZoomSlider = new javax.swing.JCheckBoxMenuItem();
        shZoomButton = new javax.swing.JCheckBoxMenuItem();
        shMapNodes = new javax.swing.JCheckBoxMenuItem();
        ContestMenuNode = new javax.swing.JPopupMenu();
        SDContestMenuN = new javax.swing.JMenuItem();
        SSHConnectionContestMenuN = new javax.swing.JMenuItem();
        deleteNodeContestMenuN = new javax.swing.JMenuItem();
        deleteNodeFSourceContestMenuN = new javax.swing.JMenuItem();
        HideNodeContestMenuN = new javax.swing.JMenuItem();
        ShowNodeContestMenuN = new javax.swing.JMenuItem();

        mainPanel.setDoubleBuffered(false);
        mainPanel.setName("mainPanel"); // NOI18N

        mapPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        mapPanel.setMaximumSize(new java.awt.Dimension(949, 604));
        mapPanel.setMinimumSize(new java.awt.Dimension(949, 604));
        mapPanel.setName("MapPanel"); // NOI18N
        mapPanel.setRequestFocusEnabled(false);
        mapPanel.setLayout(new java.awt.CardLayout());

        mainMap.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        mainMap.setCenterPosition(def);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(freimapgsoc.FreimapGSoCApp.class).getContext().getResourceMap(FreimapGSoCView.class);
        mainMap.setFont(resourceMap.getFont("mainMap.font")); // NOI18N
        mainMap.setName("mainMap"); // NOI18N
        mainMap.setRecenterOnClickEnabled(true);
        mainMap.setTileFactory(tf);
        mainMap.setZoom(14);
        mainMap.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                mainMapMouseMoved(evt);
            }
        });
        mainMap.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                mainMapMousePressed(evt);
            }
        });

        miniMap.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        miniMap.setCenterPosition(def);
        miniMap.setFont(resourceMap.getFont("miniMap.font")); // NOI18N
        miniMap.setHorizontalWrapped(false);
        miniMap.setName("miniMap"); // NOI18N
        miniMap.setPreferredSize(new java.awt.Dimension(134, 134));
        miniMap.setTileFactory(tf);
        miniMap.setZoom(14);

        javax.swing.GroupLayout miniMapLayout = new javax.swing.GroupLayout(miniMap);
        miniMap.setLayout(miniMapLayout);
        miniMapLayout.setHorizontalGroup(
            miniMapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 132, Short.MAX_VALUE)
        );
        miniMapLayout.setVerticalGroup(
            miniMapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 132, Short.MAX_VALUE)
        );

        zoomSlider.setFont(resourceMap.getFont("zoomSlider.font")); // NOI18N
        zoomSlider.setMajorTickSpacing(1);
        zoomSlider.setMaximum(14);
        zoomSlider.setMinorTickSpacing(14);
        zoomSlider.setOrientation(javax.swing.JSlider.VERTICAL);
        zoomSlider.setPaintLabels(true);
        zoomSlider.setPaintTicks(true);
        zoomSlider.setPaintTrack(false);
        zoomSlider.setDoubleBuffered(true);
        zoomSlider.setInverted(true);
        zoomSlider.setMaximumSize(new java.awt.Dimension(150, 46));
        zoomSlider.setMinimumSize(new java.awt.Dimension(150, 46));
        zoomSlider.setName("zoomSlider"); // NOI18N
        zoomSlider.setPreferredSize(new java.awt.Dimension(150, 46));
        zoomSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                zoomSliderStateChanged(evt);
            }
        });

        zoomButtonIn.setIcon(resourceMap.getIcon("zoomButtonIn.icon")); // NOI18N
        zoomButtonIn.setText(resourceMap.getString("zoomButtonIn.text")); // NOI18N
        zoomButtonIn.setName("zoomButtonIn"); // NOI18N
        zoomButtonIn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                zoomButtonInMouseClicked(evt);
            }
        });

        zoomButtonOut.setIcon(resourceMap.getIcon("zoomButtonOut.icon")); // NOI18N
        zoomButtonOut.setName("zoomButtonOut"); // NOI18N
        zoomButtonOut.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                zoomButtonOutMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout mainMapLayout = new javax.swing.GroupLayout(mainMap);
        mainMap.setLayout(mainMapLayout);
        mainMapLayout.setHorizontalGroup(
            mainMapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainMapLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainMapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(zoomButtonOut)
                    .addComponent(zoomButtonIn)
                    .addComponent(zoomSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 577, Short.MAX_VALUE)
                .addComponent(miniMap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        mainMapLayout.setVerticalGroup(
            mainMapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainMapLayout.createSequentialGroup()
                .addContainerGap(322, Short.MAX_VALUE)
                .addGroup(mainMapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainMapLayout.createSequentialGroup()
                        .addComponent(zoomButtonIn)
                        .addComponent(zoomSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addComponent(zoomButtonOut)
                        .addGap(31, 31, 31))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainMapLayout.createSequentialGroup()
                        .addComponent(miniMap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );

        mapPanel.add(mainMap, "card2");

        serviceD.setFont(resourceMap.getFont("goToDefaultPosition.font")); // NOI18N
        serviceD.setText(resourceMap.getString("serviceD.text")); // NOI18N
        serviceD.setName("serviceD"); // NOI18N
        serviceD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serviceDActionPerformed(evt);
            }
        });

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(freimapgsoc.FreimapGSoCApp.class).getContext().getActionMap(FreimapGSoCView.class, this);
        goToDefaultPosition.setAction(actionMap.get("goToDefaultPosition")); // NOI18N
        goToDefaultPosition.setFont(resourceMap.getFont("goToDefaultPosition.font")); // NOI18N
        goToDefaultPosition.setText(resourceMap.getString("goToDefaultPosition.text")); // NOI18N
        goToDefaultPosition.setName("goToDefaultPosition"); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.setName("jPanel1"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        locatedNodes.setFont(resourceMap.getFont("locatedNodes.font")); // NOI18N
        locatedNodes.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        locatedNodes.setName("locatedNodes"); // NOI18N
        jScrollPane1.setViewportView(locatedNodes);

        dateInfo.setFont(resourceMap.getFont("dateInfo.font")); // NOI18N
        dateInfo.setIcon(resourceMap.getIcon("dateInfo.icon")); // NOI18N
        dateInfo.setText(resourceMap.getString("dateInfo.text")); // NOI18N
        dateInfo.setName("dateInfo"); // NOI18N

        yValue.setFont(resourceMap.getFont("latitudeValue.font")); // NOI18N
        yValue.setMaximumSize(new java.awt.Dimension(88, 14));
        yValue.setMinimumSize(new java.awt.Dimension(88, 14));
        yValue.setName("yValue"); // NOI18N

        yPos.setFont(resourceMap.getFont("yPos.font")); // NOI18N
        yPos.setText(resourceMap.getString("yPos.text")); // NOI18N
        yPos.setName("yPos"); // NOI18N

        xPos.setFont(resourceMap.getFont("jLabel4.font")); // NOI18N
        xPos.setText(resourceMap.getString("xPos.text")); // NOI18N
        xPos.setName("xPos"); // NOI18N

        xValue.setFont(resourceMap.getFont("latitudeValue.font")); // NOI18N
        xValue.setMaximumSize(new java.awt.Dimension(88, 14));
        xValue.setMinimumSize(new java.awt.Dimension(88, 14));
        xValue.setName("xValue"); // NOI18N

        latitudeValue.setFont(resourceMap.getFont("latitudeValue.font")); // NOI18N
        latitudeValue.setText(resourceMap.getString("latitudeValue.text")); // NOI18N
        latitudeValue.setMaximumSize(new java.awt.Dimension(88, 14));
        latitudeValue.setMinimumSize(new java.awt.Dimension(88, 14));
        latitudeValue.setName("latitudeValue"); // NOI18N

        longitudeValue.setFont(resourceMap.getFont("latitudeValue.font")); // NOI18N
        longitudeValue.setText(resourceMap.getString("longitudeValue.text")); // NOI18N
        longitudeValue.setMaximumSize(new java.awt.Dimension(88, 14));
        longitudeValue.setMinimumSize(new java.awt.Dimension(88, 14));
        longitudeValue.setName("longitudeValue"); // NOI18N

        Longitude.setFont(resourceMap.getFont("jLabel4.font")); // NOI18N
        Longitude.setText(resourceMap.getString("Longitude.text")); // NOI18N
        Longitude.setName("Longitude"); // NOI18N

        Latitude.setFont(resourceMap.getFont("jLabel4.font")); // NOI18N
        Latitude.setText(resourceMap.getString("Latitude.text")); // NOI18N
        Latitude.setName("Latitude"); // NOI18N

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, resourceMap.getString("jPanel2.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, resourceMap.getFont("jPanel2.border.titleFont"))); // NOI18N
        jPanel2.setFont(resourceMap.getFont("jPanel2.font")); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        jLabel5.setFont(resourceMap.getFont("jLabel8.font")); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel5.setName("jLabel5"); // NOI18N

        ipLabel.setFont(resourceMap.getFont("jLabel16.font")); // NOI18N
        ipLabel.setText(resourceMap.getString("ipLabel.text")); // NOI18N
        ipLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        ipLabel.setName("ipLabel"); // NOI18N

        latLabel.setFont(resourceMap.getFont("jLabel16.font")); // NOI18N
        latLabel.setText(resourceMap.getString("latLabel.text")); // NOI18N
        latLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        latLabel.setName("latLabel"); // NOI18N

        jLabel7.setFont(resourceMap.getFont("jLabel8.font")); // NOI18N
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel7.setName("jLabel7"); // NOI18N

        locatedLabel.setFont(resourceMap.getFont("jLabel16.font")); // NOI18N
        locatedLabel.setText(resourceMap.getString("locatedLabel.text")); // NOI18N
        locatedLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        locatedLabel.setName("locatedLabel"); // NOI18N

        jLabel11.setFont(resourceMap.getFont("jLabel11.font")); // NOI18N
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel11.setText(resourceMap.getString("jLabel11.text")); // NOI18N
        jLabel11.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel11.setName("jLabel11"); // NOI18N

        jLabel6.setFont(resourceMap.getFont("jLabel8.font")); // NOI18N
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel6.setName("jLabel6"); // NOI18N

        fqidLabel.setFont(resourceMap.getFont("jLabel16.font")); // NOI18N
        fqidLabel.setText(resourceMap.getString("fqidLabel.text")); // NOI18N
        fqidLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        fqidLabel.setName("fqidLabel"); // NOI18N

        lonLabel.setFont(resourceMap.getFont("lonLabel.font")); // NOI18N
        lonLabel.setText(resourceMap.getString("lonLabel.text")); // NOI18N
        lonLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        lonLabel.setName("lonLabel"); // NOI18N

        jLabel8.setFont(resourceMap.getFont("jLabel8.font")); // NOI18N
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel8.setName("jLabel8"); // NOI18N

        jLabel10.setFont(resourceMap.getFont("jLabel11.font")); // NOI18N
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel10.setText(resourceMap.getString("jLabel10.text")); // NOI18N
        jLabel10.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel10.setName("jLabel10"); // NOI18N

        ncLabel.setFont(resourceMap.getFont("jLabel16.font")); // NOI18N
        ncLabel.setText(resourceMap.getString("ncLabel.text")); // NOI18N
        ncLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        ncLabel.setName("ncLabel"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)
                    .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(latLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)
                    .addComponent(ipLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)
                    .addComponent(locatedLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)
                    .addComponent(fqidLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)
                    .addComponent(lonLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)
                    .addComponent(ncLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE))
                .addGap(72, 72, 72))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel5)
                    .addComponent(ipLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(latLabel, 0, 13, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(locatedLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(fqidLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(lonLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(ncLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, resourceMap.getString("jPanel3.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, resourceMap.getFont("jPanel3.border.titleFont"))); // NOI18N
        jPanel3.setName("jPanel3"); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 260, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 109, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(dateInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(Latitude, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(Longitude, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(latitudeValue, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(longitudeValue, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(xPos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(yPos, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(yValue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(xValue, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(xPos)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(yPos))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(xValue, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(yValue, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(latitudeValue, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Latitude))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(longitudeValue, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Longitude))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dateInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(mapPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 793, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(serviceD)
                        .addGap(18, 18, 18)
                        .addComponent(goToDefaultPosition)))
                .addGap(188, 188, 188))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                        .addComponent(mapPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 604, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(goToDefaultPosition)
                            .addComponent(serviceD))
                        .addGap(237, 237, 237))))
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setFont(resourceMap.getFont("fileMenu.font")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        jMenu1.setIcon(resourceMap.getIcon("jMenu1.icon")); // NOI18N
        jMenu1.setText(resourceMap.getString("jMenu1.text")); // NOI18N
        jMenu1.setFont(resourceMap.getFont("jMenu1.font")); // NOI18N
        jMenu1.setName("jMenu1"); // NOI18N

        xmlOpenMenu.setFont(resourceMap.getFont("xmlOpenMenu.font")); // NOI18N
        xmlOpenMenu.setIcon(resourceMap.getIcon("xmlOpenMenu.icon")); // NOI18N
        xmlOpenMenu.setText(resourceMap.getString("xmlOpenMenu.text")); // NOI18N
        xmlOpenMenu.setName("xmlOpenMenu"); // NOI18N
        xmlOpenMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xmlOpenMenuActionPerformed(evt);
            }
        });
        jMenu1.add(xmlOpenMenu);

        txtOpenMenu.setFont(resourceMap.getFont("txtOpenMenu.font")); // NOI18N
        txtOpenMenu.setIcon(resourceMap.getIcon("txtOpenMenu.icon")); // NOI18N
        txtOpenMenu.setText(resourceMap.getString("txtOpenMenu.text")); // NOI18N
        txtOpenMenu.setName("txtOpenMenu"); // NOI18N
        txtOpenMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtOpenMenuActionPerformed(evt);
            }
        });
        jMenu1.add(txtOpenMenu);

        jsOpenMenu.setFont(resourceMap.getFont("jsOpenMenu.font")); // NOI18N
        jsOpenMenu.setIcon(resourceMap.getIcon("jsOpenMenu.icon")); // NOI18N
        jsOpenMenu.setText(resourceMap.getString("jsOpenMenu.text")); // NOI18N
        jsOpenMenu.setName("jsOpenMenu"); // NOI18N
        jsOpenMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jsOpenMenuActionPerformed(evt);
            }
        });
        jMenu1.add(jsOpenMenu);

        fileMenu.add(jMenu1);

        saveAsMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        saveAsMenu.setFont(resourceMap.getFont("saveAsMenu.font")); // NOI18N
        saveAsMenu.setIcon(resourceMap.getIcon("saveAsMenu.icon")); // NOI18N
        saveAsMenu.setText(resourceMap.getString("saveAsMenu.text")); // NOI18N
        saveAsMenu.setName("saveAsMenu"); // NOI18N
        saveAsMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsMenuActionPerformed(evt);
            }
        });
        fileMenu.add(saveAsMenu);

        jSeparator3.setName("jSeparator3"); // NOI18N
        fileMenu.add(jSeparator3);

        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setFont(resourceMap.getFont("exitMenuItem.font")); // NOI18N
        exitMenuItem.setIcon(resourceMap.getIcon("exitMenuItem.icon")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        jMenu2.setText(resourceMap.getString("jMenu2.text")); // NOI18N
        jMenu2.setFont(resourceMap.getFont("jMenu2.font")); // NOI18N
        jMenu2.setName("jMenu2"); // NOI18N

        jSeparator1.setName("jSeparator1"); // NOI18N
        jMenu2.add(jSeparator1);

        jMenuItem8.setAction(actionMap.get("goToDefaultPosition")); // NOI18N
        jMenuItem8.setFont(resourceMap.getFont("jMenuItem5.font")); // NOI18N
        jMenuItem8.setIcon(resourceMap.getIcon("jMenuItem8.icon")); // NOI18N
        jMenuItem8.setText(resourceMap.getString("jMenuItem8.text")); // NOI18N
        jMenuItem8.setName("jMenuItem8"); // NOI18N
        jMenu2.add(jMenuItem8);

        jMenuItem6.setAction(actionMap.get("addNodeOnMap")); // NOI18N
        jMenuItem6.setFont(resourceMap.getFont("jMenuItem5.font")); // NOI18N
        jMenuItem6.setIcon(resourceMap.getIcon("jMenuItem6.icon")); // NOI18N
        jMenuItem6.setText(resourceMap.getString("jMenuItem6.text")); // NOI18N
        jMenuItem6.setName("jMenuItem6"); // NOI18N
        jMenu2.add(jMenuItem6);

        jMenuItem5.setAction(actionMap.get("takeScreenShot")); // NOI18N
        jMenuItem5.setFont(resourceMap.getFont("jMenuItem5.font")); // NOI18N
        jMenuItem5.setIcon(resourceMap.getIcon("jMenuItem5.icon")); // NOI18N
        jMenuItem5.setText(resourceMap.getString("jMenuItem5.text")); // NOI18N
        jMenuItem5.setName("jMenuItem5"); // NOI18N
        jMenu2.add(jMenuItem5);

        jSeparator2.setName("jSeparator2"); // NOI18N
        jMenu2.add(jSeparator2);

        jMenuItem12.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem12.setFont(resourceMap.getFont("jMenuItem5.font")); // NOI18N
        jMenuItem12.setIcon(resourceMap.getIcon("jMenuItem12.icon")); // NOI18N
        jMenuItem12.setText(resourceMap.getString("jMenuItem12.text")); // NOI18N
        jMenuItem12.setName("jMenuItem12"); // NOI18N
        jMenu2.add(jMenuItem12);

        jMenuItem7.setAction(actionMap.get("findNode")); // NOI18N
        jMenuItem7.setFont(resourceMap.getFont("jMenuItem5.font")); // NOI18N
        jMenuItem7.setIcon(resourceMap.getIcon("jMenuItem7.icon")); // NOI18N
        jMenuItem7.setText(resourceMap.getString("jMenuItem7.text")); // NOI18N
        jMenuItem7.setName("jMenuItem7"); // NOI18N
        jMenu2.add(jMenuItem7);

        jMenuItem9.setAction(actionMap.get("goHere")); // NOI18N
        jMenuItem9.setFont(resourceMap.getFont("jMenuItem5.font")); // NOI18N
        jMenuItem9.setIcon(resourceMap.getIcon("jMenuItem9.icon")); // NOI18N
        jMenuItem9.setText(resourceMap.getString("jMenuItem9.text")); // NOI18N
        jMenuItem9.setName("jMenuItem9"); // NOI18N
        jMenu2.add(jMenuItem9);

        menuBar.add(jMenu2);

        viewMenu.setText(resourceMap.getString("viewMenu.text")); // NOI18N
        viewMenu.setFont(resourceMap.getFont("viewMenu.font")); // NOI18N
        viewMenu.setName("viewMenu"); // NOI18N

        mapNodesMenu.setAction(actionMap.get("showNodes")); // NOI18N
        mapNodesMenu.setFont(resourceMap.getFont("mapNodesMenu.font")); // NOI18N
        mapNodesMenu.setSelected(true);
        mapNodesMenu.setText(resourceMap.getString("mapNodesMenu.text")); // NOI18N
        mapNodesMenu.setIcon(resourceMap.getIcon("mapNodesMenu.icon")); // NOI18N
        mapNodesMenu.setName("mapNodesMenu"); // NOI18N
        viewMenu.add(mapNodesMenu);

        linksMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_2, java.awt.event.InputEvent.CTRL_MASK));
        linksMenu.setFont(resourceMap.getFont("mapNodesMenu.font")); // NOI18N
        linksMenu.setSelected(true);
        linksMenu.setText(resourceMap.getString("linksMenu.text")); // NOI18N
        linksMenu.setIcon(resourceMap.getIcon("linksMenu.icon")); // NOI18N
        linksMenu.setName("linksMenu"); // NOI18N
        viewMenu.add(linksMenu);

        latLonMenu.setAction(actionMap.get("showLatLon")); // NOI18N
        latLonMenu.setFont(resourceMap.getFont("mapNodesMenu.font")); // NOI18N
        latLonMenu.setSelected(true);
        latLonMenu.setText(resourceMap.getString("latLonMenu.text")); // NOI18N
        latLonMenu.setIcon(resourceMap.getIcon("latLonMenu.icon")); // NOI18N
        latLonMenu.setName("latLonMenu"); // NOI18N
        viewMenu.add(latLonMenu);

        jCheckBoxMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_4, java.awt.event.InputEvent.CTRL_MASK));
        jCheckBoxMenuItem1.setFont(resourceMap.getFont("mapNodesMenu.font")); // NOI18N
        jCheckBoxMenuItem1.setSelected(true);
        jCheckBoxMenuItem1.setText(resourceMap.getString("jCheckBoxMenuItem1.text")); // NOI18N
        jCheckBoxMenuItem1.setIcon(resourceMap.getIcon("jCheckBoxMenuItem1.icon")); // NOI18N
        jCheckBoxMenuItem1.setName("jCheckBoxMenuItem1"); // NOI18N
        viewMenu.add(jCheckBoxMenuItem1);

        zoomButtons.setAction(actionMap.get("showZoomButton")); // NOI18N
        zoomButtons.setFont(resourceMap.getFont("mapNodesMenu.font")); // NOI18N
        zoomButtons.setSelected(true);
        zoomButtons.setText(resourceMap.getString("zoomButtons.text")); // NOI18N
        zoomButtons.setIcon(resourceMap.getIcon("zoomButtons.icon")); // NOI18N
        zoomButtons.setName("zoomButtons"); // NOI18N
        viewMenu.add(zoomButtons);

        zoomSMenu.setAction(actionMap.get("showSlider")); // NOI18N
        zoomSMenu.setFont(resourceMap.getFont("mapNodesMenu.font")); // NOI18N
        zoomSMenu.setSelected(true);
        zoomSMenu.setText(resourceMap.getString("zoomSMenu.text")); // NOI18N
        zoomSMenu.setIcon(resourceMap.getIcon("zoomSMenu.icon")); // NOI18N
        zoomSMenu.setName("zoomSMenu"); // NOI18N
        viewMenu.add(zoomSMenu);

        miniMapMenu.setAction(actionMap.get("showMiniMap")); // NOI18N
        miniMapMenu.setFont(resourceMap.getFont("mapNodesMenu.font")); // NOI18N
        miniMapMenu.setSelected(true);
        miniMapMenu.setText(resourceMap.getString("miniMapMenu.text")); // NOI18N
        miniMapMenu.setIcon(resourceMap.getIcon("miniMapMenu.icon")); // NOI18N
        miniMapMenu.setName("miniMapMenu"); // NOI18N
        viewMenu.add(miniMapMenu);

        menuBar.add(viewMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setFont(resourceMap.getFont("helpMenu.font")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setFont(resourceMap.getFont("jMenuItem1.font")); // NOI18N
        aboutMenuItem.setIcon(resourceMap.getIcon("aboutMenuItem.icon")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        jMenuItem1.setFont(resourceMap.getFont("jMenuItem1.font")); // NOI18N
        jMenuItem1.setIcon(resourceMap.getIcon("jMenuItem1.icon")); // NOI18N
        jMenuItem1.setText(resourceMap.getString("jMenuItem1.text")); // NOI18N
        jMenuItem1.setName("jMenuItem1"); // NOI18N
        helpMenu.add(jMenuItem1);

        menuBar.add(helpMenu);

        ContestMenu.setFont(resourceMap.getFont("rightClickMenu.font")); // NOI18N
        ContestMenu.setMaximumSize(null);
        ContestMenu.setMinimumSize(null);
        ContestMenu.setName("rightClickMenu"); // NOI18N
        ContestMenu.setOpaque(false);
        ContestMenu.setPreferredSize(null);

        jMenuItem2.setAction(actionMap.get("goToDefaultPosition")); // NOI18N
        jMenuItem2.setIcon(resourceMap.getIcon("jMenuItem2.icon")); // NOI18N
        jMenuItem2.setText(resourceMap.getString("jMenuItem2.text")); // NOI18N
        jMenuItem2.setName("jMenuItem2"); // NOI18N
        ContestMenu.add(jMenuItem2);

        jMenuItem3.setAction(actionMap.get("takeScreenShot")); // NOI18N
        jMenuItem3.setIcon(resourceMap.getIcon("jMenuItem3.icon")); // NOI18N
        jMenuItem3.setText(resourceMap.getString("jMenuItem3.text")); // NOI18N
        jMenuItem3.setName("jMenuItem3"); // NOI18N
        ContestMenu.add(jMenuItem3);

        shMiniMap.setSelected(true);
        shMiniMap.setText(resourceMap.getString("shMiniMap.text")); // NOI18N
        shMiniMap.setIcon(resourceMap.getIcon("shMiniMap.icon")); // NOI18N
        shMiniMap.setName("shMiniMap"); // NOI18N
        ContestMenu.add(shMiniMap);

        shZoomSlider.setSelected(true);
        shZoomSlider.setText(resourceMap.getString("shZoomSlider.text")); // NOI18N
        shZoomSlider.setName("shZoomSlider"); // NOI18N
        ContestMenu.add(shZoomSlider);

        shZoomButton.setSelected(true);
        shZoomButton.setText(resourceMap.getString("shZoomButton.text")); // NOI18N
        shZoomButton.setName("shZoomButton"); // NOI18N
        ContestMenu.add(shZoomButton);

        shMapNodes.setSelected(true);
        shMapNodes.setText(resourceMap.getString("shMapNodes.text")); // NOI18N
        shMapNodes.setName("shMapNodes"); // NOI18N
        ContestMenu.add(shMapNodes);

        ContestMenu.getAccessibleContext().setAccessibleParent(mainMap);

        ContestMenuNode.setFont(resourceMap.getFont("ContestMenuNode.font")); // NOI18N
        ContestMenuNode.setMaximumSize(null);
        ContestMenuNode.setMinimumSize(null);
        ContestMenuNode.setName("ContestMenuNode"); // NOI18N
        ContestMenuNode.setPreferredSize(null);

        SDContestMenuN.setIcon(resourceMap.getIcon("ServiceDiscovery.icon")); // NOI18N
        SDContestMenuN.setText(resourceMap.getString("ServiceDiscovery.text")); // NOI18N
        SDContestMenuN.setName("ServiceDiscovery"); // NOI18N
        ContestMenuNode.add(SDContestMenuN);

        SSHConnectionContestMenuN.setText(resourceMap.getString("SSHConnectionContestMenuN.text")); // NOI18N
        SSHConnectionContestMenuN.setName("SSHConnectionContestMenuN"); // NOI18N
        ContestMenuNode.add(SSHConnectionContestMenuN);

        deleteNodeContestMenuN.setIcon(resourceMap.getIcon("deleteNodeContestMenuN.icon")); // NOI18N
        deleteNodeContestMenuN.setText(resourceMap.getString("deleteNodeContestMenuN.text")); // NOI18N
        deleteNodeContestMenuN.setName("deleteNodeContestMenuN"); // NOI18N
        ContestMenuNode.add(deleteNodeContestMenuN);

        deleteNodeFSourceContestMenuN.setIcon(resourceMap.getIcon("deleteNodeFSourceContestMenuN.icon")); // NOI18N
        deleteNodeFSourceContestMenuN.setText(resourceMap.getString("deleteNodeFSourceContestMenuN.text")); // NOI18N
        deleteNodeFSourceContestMenuN.setName("deleteNodeFSourceContestMenuN"); // NOI18N
        ContestMenuNode.add(deleteNodeFSourceContestMenuN);

        HideNodeContestMenuN.setIcon(resourceMap.getIcon("HideNodeContestMenuN.icon")); // NOI18N
        HideNodeContestMenuN.setText(resourceMap.getString("HideNodeContestMenuN.text")); // NOI18N
        HideNodeContestMenuN.setName("HideNodeContestMenuN"); // NOI18N
        ContestMenuNode.add(HideNodeContestMenuN);

        ShowNodeContestMenuN.setIcon(resourceMap.getIcon("ShowNodeContestMenuN.icon")); // NOI18N
        ShowNodeContestMenuN.setLabel(resourceMap.getString("ShowNodeContestMenuN.label")); // NOI18N
        ShowNodeContestMenuN.setName("ShowNodeContestMenuN"); // NOI18N
        ContestMenuNode.add(ShowNodeContestMenuN);

        ContestMenuNode.getAccessibleContext().setAccessibleParent(mainMap);

        setComponent(mainPanel);
        setMenuBar(menuBar);
    }// </editor-fold>//GEN-END:initComponents

    private void mainMapMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mainMapMousePressed
        if (evt.isPopupTrigger()) {
            String nodeName = "";
            Point pt = evt.getPoint();
            GeoPosition gp = mainMap.convertPointToGeoPosition(new Point2D.Double(evt.getX(), evt.getY()));
            Vector coor = new Vector();
            coor.add(String.format("%.2f", gp.getLatitude()));
            coor.add(String.format("%.2f", gp.getLongitude()));
            Point2D gp_pt = mainMap.getTileFactory().geoToPixel(gp, mainMap.getZoom());
            Rectangle rect = mainMap.getViewportBounds();
            Point converted_gp_pt = new Point((int) pt.getX() - rect.x,
                    (int) pt.getY() - rect.y);
            if (latlon.get(coor) != null) {
                nodeName = latlon.get(coor);
                for (int i = 0; i < nodes.size(); i++) { //to implement with a serach by name!
                    if (nodes.elementAt(i).toString().equals(nodeName)) {
                        ContestMenuNode.show(evt.getComponent(), evt.getX(), evt.getY());

                    } else {
                        ContestMenu.show(evt.getComponent(), evt.getX(), evt.getY());

                    }
                }

            } else {
                ContestMenu.show(evt.getComponent(), evt.getX(), evt.getY());
            }


            //switch (evt.getClickCount()) {
            //case 1: new NodeInfoFlow().setVisible(true); break;

            //}
        }
        ContestMenu.setVisible(false);
    }//GEN-LAST:event_mainMapMousePressed

    private void serviceDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serviceDActionPerformed
        InetAddress intf = null;
        try {
            intf = InetAddress.getByName("10.0.1.29");
            try {
                new PopUp(JmDNS.create(intf)).setVisible(true);
            } catch (IOException ex) {
                Logger.getLogger(FreimapGSoCView.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(PopUpMain.class.getName()).log(Level.SEVERE, null, ex);
        }        // TODO add your handling code here:
    }//GEN-LAST:event_serviceDActionPerformed

    private void zoomSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_zoomSliderStateChanged
        if (!zoomChanging) {
            mainMap.setZoom(zoomSlider.getValue());
            drawAll(links, nodes);
        }        // TODO add your handling code here:
    }//GEN-LAST:event_zoomSliderStateChanged

    private void mainMapMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mainMapMouseMoved
        String nodeName = "";
        Point pt = evt.getPoint();
        GeoPosition gp = mainMap.convertPointToGeoPosition(new Point2D.Double(evt.getX(), evt.getY()));
        //Se la posizione del mouse è uguale a una presente nel vettore latlon allora prendi il nome e visualizzalo
        Vector coor = new Vector();
        coor.add(String.format("%.2f", gp.getLatitude()));
        coor.add(String.format("%.2f", gp.getLongitude()));
        //System.out.println("latlon.get(coor): " + latlon.get(coor));
        //System.out.println("evt.getPoint():" + evt.getPoint());
        //System.out.println("pt:" + pt);
        Point2D gp_pt = mainMap.getTileFactory().geoToPixel(gp, mainMap.getZoom());
        Rectangle rect = mainMap.getViewportBounds();
        Point converted_gp_pt = new Point((int) pt.getX() - rect.x,
                (int) pt.getY() - rect.y);

        //check if near the mouse
        if (converted_gp_pt.distance(evt.getPoint()) < 5) {

            nodeLabel.setVisible(true);
            nodeLabel.setLocation(converted_gp_pt);
            nodeLabel.setBounds(new Rectangle(0, 10, 5, 5));
            nodeLabel.setText("OKKKKKKKKKKKKKK!");

        } else {
            nodeLabel.setVisible(false);
        }

        if (latlon.get(coor) != null) {
            nodeName = latlon.get(coor);

            for (int i = 0; i < nodes.size(); i++) {
                System.out.println("Nodes.elementAt(i): " + nodes.elementAt(i).toString());
                System.out.println("NodeName:" + nodeName);

                if (nodes.elementAt(i).toString().equals(nodeName)) {
                    nodeLabel.setText(nodeName);
                    
                    ipLabel.setText(nodes.elementAt(i).id);
                    fqidLabel.setText(nodes.elementAt(i).fqid);
                    latLabel.setText(String.valueOf(nodes.elementAt(i).lat));
                    lonLabel.setText(String.valueOf(nodes.elementAt(i).lon));
                    if (nodes.elementAt(i).unlocated == true) {
                        ImageIcon located=new ImageIcon("/Users/stefanopilla/Desktop/FreimapGSoC/src/gfx/located.png");
                        locatedLabel.setIcon(located);

                        locatedLabel.setText("Unlocated");
                    } else {
                        ImageIcon notlocated=new ImageIcon("/Users/stefanopilla/Desktop/FreimapGSoC/src/gfx/notlocated.png");
                        locatedLabel.setIcon(notlocated);
                        locatedLabel.setText("Located");

                    }
                    ncLabel.setText(String.valueOf(nodes.elementAt(i).nc));
                    nodeLabel.setVisible(true);
                    nodeLabel.setLocation(evt.getPoint());
                }
            }



        } else {
            nodeLabel.setText("");
            nodeLabel.setVisible(false);


        }

        //System.out.println("GeoPosition" + gp);
        DecimalFormat fmt = new DecimalFormat("#00.00000");
        latitudeValue.setText(fmt.format(gp.getLatitude()));
        longitudeValue.setText(fmt.format(gp.getLongitude()));
        xValue.setText(String.format("%.3f", mainMap.getTileFactory().geoToPixel(gp, mainMap.getZoom()).getX()));
        yValue.setText(String.format("%.3f", mainMap.getTileFactory().geoToPixel(gp, mainMap.getZoom()).getY()));
    }//GEN-LAST:event_mainMapMouseMoved

    private void xmlOpenMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xmlOpenMenuActionPerformed
        if (evt.getSource() == xmlOpenMenu) {
            JFileChooser fcxml = new JFileChooser();
            fcxml.addChoosableFileFilter(new xmlFileFilter());
            fcxml.setAcceptAllFileFilterUsed(false);
            int returnVal = fcxml.showOpenDialog(fcxml);
            //Handle open button action.

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fcxml.getSelectedFile();
                System.out.println("FILE XML OPEN CORRECT!");
                //OPEN A FILE AND RELOAD ALL DATA ON THE MAP!

                System.out.println("Opening: " + file.getName() + ".\n");
            } else {
                System.out.println("Open command cancelled by user." + "\n");
            }

        }

    }//GEN-LAST:event_xmlOpenMenuActionPerformed

    private void txtOpenMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtOpenMenuActionPerformed
        if (evt.getSource() == txtOpenMenu) {
            JFileChooser fctxt = new JFileChooser();
            fctxt.setAcceptAllFileFilterUsed(false);

            fctxt.addChoosableFileFilter(new txtFileFilter());
            int returnVal = fctxt.showOpenDialog(fctxt);
            //Handle open button action.

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fctxt.getSelectedFile();
                System.out.println("FILE TXT OPEN CORRECT!");
                //OPEN A FILE AND RELOAD ALL DATA ON THE MAP!

                System.out.println("Opening: " + file.getName() + "\n");
            } else {
                System.out.println("Open command cancelled by user." + "\n");
            }

        }
    }//GEN-LAST:event_txtOpenMenuActionPerformed

    private void jsOpenMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jsOpenMenuActionPerformed
        if (evt.getSource() == jsOpenMenu) {
            JFileChooser fcjs = new JFileChooser();
            fcjs.addChoosableFileFilter(new jsFileFilter());
            fcjs.setAcceptAllFileFilterUsed(false);

            int returnVal = fcjs.showOpenDialog(fcjs);
            //Handle open button action.

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fcjs.getSelectedFile();
                System.out.println("FILE JS OPEN CORRECT!");
                //OPEN A FILE AND RELOAD ALL DATA ON THE MAP!
                System.out.println("Opening: " + file.getName() + ".\n");
            } else {
                System.out.println("Open command cancelled by user." + "\n");
            }

        }
    }//GEN-LAST:event_jsOpenMenuActionPerformed

    private void saveAsMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsMenuActionPerformed
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
    }//GEN-LAST:event_saveAsMenuActionPerformed

    private void zoomButtonInMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_zoomButtonInMouseClicked
        setZoom(mainMap.getZoom() - 1);
// TODO add your handling code here:
    }//GEN-LAST:event_zoomButtonInMouseClicked

    private void zoomButtonOutMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_zoomButtonOutMouseClicked
        setZoom(mainMap.getZoom() + 1);
        // TODO add your handling code here:
    }//GEN-LAST:event_zoomButtonOutMouseClicked

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

            public void paint(Graphics2D g, JXMapViewer map, int width, int height) {
                g.setPaint(Color.WHITE);
                g.drawString(" ", 50,
                        map.getHeight() - 10);
            }
        }, wp));


        // adapter to move the minimap after the main map has moved
        MouseInputAdapter ma = new MouseInputAdapter() {

            public void mouseReleased(MouseEvent e) {
                miniMap.setCenterPosition(mapCenterPosition);
            }
        };

        mainMap.addMouseMotionListener(ma);
        mainMap.addMouseListener(ma);
        mainMap.addPropertyChangeListener("center", new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                Point2D mapCenter = (Point2D) evt.getNewValue();
                TileFactory tf = mainMap.getTileFactory();
                GeoPosition mapPos = tf.pixelToGeo(mapCenter, mainMap.getZoom());
                miniMap.setCenterPosition(mapPos);
            }
        });


        mainMap.addPropertyChangeListener("centerPosition", new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                mapCenterPosition = (GeoPosition) evt.getNewValue();
                miniMap.setCenterPosition(mapCenterPosition);
                Point2D pt = miniMap.getTileFactory().geoToPixel(mapCenterPosition, miniMap.getZoom());
                miniMap.setCenter(pt);
                miniMap.repaint();
            }
        });

        mainMap.addPropertyChangeListener("zoom", new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                zoomSlider.setValue(mainMap.getZoom());
            }
        });


        miniMap.setOverlayPainter(new Painter<JXMapViewer>() {

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

    @Action
    public void addNodeOnMap() {
        //OPEN A DIALOG Where ask lat/lon and eventually icon for the new node!
    }

    @Action
    public void findNode() {
        new FindNode().setVisible(true);
    }

    @Action
    public void goHere() {
        new goHere().setVisible(true);
    }

    @Action
    public void showNodes() {
        //Hide All nodes on the map
    }

    @Action
    public void showLatLon() {
        if (latLonMenu.isSelected()) {
            latitudeValue.setVisible(false);
            longitudeValue.setVisible(false);
        } else {
            latitudeValue.setVisible(true);
            longitudeValue.setVisible(true);
        }

    }

    @Action
    public void showZoomButton() {
        if (zoomButtonIn.isVisible()) {
            zoomButtonIn.setVisible(false);
            zoomButtonOut.setVisible(false);
        } else {
            zoomButtonIn.setVisible(true);
            zoomButtonOut.setVisible(true);
        }

    }

    @Action
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
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JPopupMenu ContestMenu;
    public javax.swing.JPopupMenu ContestMenuNode;
    public javax.swing.JMenuItem HideNodeContestMenuN;
    public javax.swing.JLabel Latitude;
    public javax.swing.JLabel Longitude;
    public javax.swing.JMenuItem SDContestMenuN;
    public javax.swing.JMenuItem SSHConnectionContestMenuN;
    public javax.swing.JMenuItem ShowNodeContestMenuN;
    public javax.swing.JLabel dateInfo;
    public javax.swing.JMenuItem deleteNodeContestMenuN;
    public javax.swing.JMenuItem deleteNodeFSourceContestMenuN;
    public javax.swing.JLabel fqidLabel;
    public javax.swing.JButton goToDefaultPosition;
    public javax.swing.JLabel ipLabel;
    public javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem1;
    public javax.swing.JLabel jLabel10;
    public javax.swing.JLabel jLabel11;
    public javax.swing.JLabel jLabel5;
    public javax.swing.JLabel jLabel6;
    public javax.swing.JLabel jLabel7;
    public javax.swing.JLabel jLabel8;
    public javax.swing.JMenu jMenu1;
    public javax.swing.JMenu jMenu2;
    public javax.swing.JMenuItem jMenuItem1;
    public javax.swing.JMenuItem jMenuItem12;
    public javax.swing.JMenuItem jMenuItem2;
    public javax.swing.JMenuItem jMenuItem3;
    public javax.swing.JMenuItem jMenuItem5;
    public javax.swing.JMenuItem jMenuItem6;
    public javax.swing.JMenuItem jMenuItem7;
    public javax.swing.JMenuItem jMenuItem8;
    public javax.swing.JMenuItem jMenuItem9;
    public javax.swing.JPanel jPanel1;
    public javax.swing.JPanel jPanel2;
    public javax.swing.JPanel jPanel3;
    public javax.swing.JScrollPane jScrollPane1;
    public javax.swing.JSeparator jSeparator1;
    public javax.swing.JSeparator jSeparator2;
    public javax.swing.JSeparator jSeparator3;
    public javax.swing.JMenuItem jsOpenMenu;
    public javax.swing.JLabel latLabel;
    public javax.swing.JCheckBoxMenuItem latLonMenu;
    public javax.swing.JLabel latitudeValue;
    public javax.swing.JCheckBoxMenuItem linksMenu;
    public javax.swing.JLabel locatedLabel;
    public javax.swing.JList locatedNodes;
    public javax.swing.JLabel lonLabel;
    public javax.swing.JLabel longitudeValue;
    public static org.jdesktop.swingx.JXMapViewer mainMap;
    public javax.swing.JPanel mainPanel;
    public javax.swing.JCheckBoxMenuItem mapNodesMenu;
    public javax.swing.JPanel mapPanel;
    public javax.swing.JMenuBar menuBar;
    public static org.jdesktop.swingx.JXMapViewer miniMap;
    public javax.swing.JCheckBoxMenuItem miniMapMenu;
    public javax.swing.JLabel ncLabel;
    public javax.swing.JMenuItem saveAsMenu;
    public javax.swing.JButton serviceD;
    public javax.swing.JCheckBoxMenuItem shMapNodes;
    public javax.swing.JCheckBoxMenuItem shMiniMap;
    public javax.swing.JCheckBoxMenuItem shZoomButton;
    public javax.swing.JCheckBoxMenuItem shZoomSlider;
    public javax.swing.JMenuItem txtOpenMenu;
    public javax.swing.JMenu viewMenu;
    public javax.swing.JLabel xPos;
    public javax.swing.JLabel xValue;
    public javax.swing.JMenuItem xmlOpenMenu;
    public javax.swing.JLabel yPos;
    public javax.swing.JLabel yValue;
    public javax.swing.JLabel zoomButtonIn;
    public javax.swing.JLabel zoomButtonOut;
    public javax.swing.JCheckBoxMenuItem zoomButtons;
    public javax.swing.JCheckBoxMenuItem zoomSMenu;
    public javax.swing.JSlider zoomSlider;
    // End of variables declaration//GEN-END:variables
    private JDialog aboutBox;
    private GeoPosition def;//OK
    private Double DEFAULT_LAT = 0.0;//OK
    private Double DEFAULT_LON = 0.0;//OK
    private TileFactory tf; //OK
    private Runtime runtime;
//MainMap and Minimap Variables
    final List<GeoPosition> region = new ArrayList<GeoPosition>();
    public static boolean miniMapVisible = true;//OK
    public static boolean zoomSliderVisible = true;//OK
    public static boolean zoomButtonsVisible = true;//OK
    public static final boolean sliderReversed = false;//OK
    private static WaypointPainter painter = new WaypointPainter();//OK
    private static Set<Waypoint> waypoints = new HashSet<Waypoint>();//OK
    private FreiNode uplink = new FreiNode("0.0.0.0/0.0.0.0");//OK
    private Point2D mapCenter = new Point2D.Double(0, 0);//OK
    private GeoPosition mapCenterPosition = new GeoPosition(0, 0);//OK
    private boolean zoomChanging = false;//OK
    public static Configurator config;
    public static HashMap<String, DataSource> sources;
    Vector<FreiNode> nodes;
    Vector<FreiLink> links;
    public HashMap<String, FreiNode> nodeByName = new HashMap<String, FreiNode>();
    HashMap<Vector, String> latlon;
    Vector coor = new Vector();
    public DefaultListModel locatedN = new DefaultListModel();
    JLabel nodeLabel = new JLabel();
}
