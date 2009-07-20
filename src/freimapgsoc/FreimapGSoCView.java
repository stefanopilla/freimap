/*
 * FreimapGSoCView.java
 */
package freimapgsoc;

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
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.jmdns.JmDNS;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
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


        config = new Configurator();
        sources = new HashMap<String, DataSource>();
        try {
            HashMap<String, Object> ds = (HashMap<String, Object>) config.get("datasources");
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
                //System.out.println("id:"+ id);
                DataSource source = sources.get(id);
                source.init(ds2subconfig.get(id)); //initialize datasource with configuration parameters
                //System.out.println("Sources: "+sources.values());
                //System.out.println("NodeList: "+source.getNodeList());
                //System.out.println("LinksList: "+source.getLinks(0));

                nodes = new Vector<FreiNode>();
                links = new Vector<FreiLink>();

                for (int t = 0; t < links.size(); t++) {
                    //System.out.println("Links Element("+ t +"):" +links.elementAt(t));
                }
                links = source.getLinks(0);
                nodes = source.getNodeList();

                for (int k = 0; k < nodes.size(); k++) {
                    // System.out.println("id: "+nodes.get(k) + "lat: "+ nodes.get(k).lat + " lon: "+ nodes.get(k).lon);
                    addWaypoint(nodes.get(k).lat, nodes.get(k).lon, nodes.get(k));
                }


                drawAll(links, nodes);
                /*
                System.out.println("to:"+ links.get(k).to);
                System.out.println("from: "+links.get(k).from);
                System.out.println("HNA:"+ links.get(k).HNA);
                System.out.println("udp:"+ links.get(k).udp);
                System.out.println("udp:"+ links.get(k).tcp);
                System.out.println("udp:"+ links.get(k).packets);
                System.out.println("nlq:"+ links.get(k).bytes);
                System.out.println("etx:"+ links.get(k).etx);
                System.out.println("lq:"+ links.get(k).lq);
                System.out.println("nlq:"+ links.get(k).nlq);
                System.out.println("other:"+ links.get(k).other);
                System.out.println("icmp:"+ links.get(k).icmp);
                 */


            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }


    }

    public void drawAll(Vector<FreiLink> links, Vector<FreiNode> nodes) {
        if (nodes.size() == 0) {
            for (int i = 0; i < links.size(); i++) {
                FreiLink selectedLink = links.elementAt(i);
                //System.out.println("selectedLink.from.lat: "+ selectedLink.from.lat + "selectedLink.from.lon: "+ selectedLink.from.lon);
                //System.out.println("selectedLink.to.lat: "+ selectedLink.to.lat + "selectedLink.to.lon"+ selectedLink.to.lon);
                GeoPosition posFrom = new GeoPosition(selectedLink.from.lat, selectedLink.from.lon);
                GeoPosition posTo = new GeoPosition(selectedLink.to.lat, selectedLink.to.lon);
                //System.out.println("posFromLat:" +posFrom.getLatitude() + " posFromLon: "+ posFrom.getLongitude());
                //System.out.println("posToLat:" +posTo.getLatitude() + " posToLon: "+ posTo.getLongitude());

                final Point2D ptFrom = mainMap.getTileFactory().geoToPixel(posFrom, mainMap.getZoom());
                final Point2D ptTo = mainMap.getTileFactory().geoToPixel(posTo, mainMap.getZoom());
                //System.out.println("ptFrom X:" +(int)ptFrom.getX());
                //System.out.println("ptFrom Y:" +(int)ptFrom.getY());
                //System.out.println("ptTo X:" +(int)ptTo.getX());
                //System.out.println("ptTo Y:" +(int)ptTo.getY());
                painter.setRenderer(new WaypointRenderer() {

                    public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {
                        g.drawLine((int) ptFrom.getX(), (int) ptFrom.getY(), (int) ptTo.getX(), (int) ptTo.getY());
                        return false;
                    }
                });
            }
        } else if (links.size() == 0) {
            for (int i = 0; i < nodes.size(); i++) {
                mainMap.setAddressLocation(new GeoPosition(nodes.get(i).lat, nodes.get(i).lon));
                waypoints.add(new Waypoint(nodes.get(i).lat, nodes.get(i).lon));
                painter.setWaypoints(waypoints);
                painter.setRenderer(new WaypointRenderer() {

                    public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {

                        Toolkit toolkit = Toolkit.getDefaultToolkit();
                        Image i = toolkit.getImage("/home/stefano/NetBeansProjects/FreimapGSoC/src/gfx/wrt.png");
                        g.drawImage(i, 0, 0, null);
                        return true;
                    }
                });
            }

        } else {
            for (int i = 0; i < links.size(); i++) {
                FreiLink selectedLink = links.elementAt(i);
                System.out.println("selectedLink.from.lat: " + selectedLink.from.lat + "selectedLink.from.lon: " + selectedLink.from.lon);
                System.out.println("selectedLink.to.lat: " + selectedLink.to.lat + "selectedLink.to.lon" + selectedLink.to.lon);
                GeoPosition posFrom = new GeoPosition(selectedLink.from.lat, selectedLink.from.lon);
                GeoPosition posTo = new GeoPosition(selectedLink.to.lat, selectedLink.to.lon);
                System.out.println("posFromLat:" + posFrom.getLatitude() + " posFromLon: " + posFrom.getLongitude());
                System.out.println("posToLat:" + posTo.getLatitude() + " posToLon: " + posTo.getLongitude());

                final Point2D ptFrom = mainMap.getTileFactory().geoToPixel(posFrom, mainMap.getZoom());
                final Point2D ptTo = mainMap.getTileFactory().geoToPixel(posTo, mainMap.getZoom());
                System.out.println("ptFrom X:" + (int) ptFrom.getX());
                System.out.println("ptFrom Y:" + (int) ptFrom.getY());
                System.out.println("ptTo X:" + (int) ptTo.getX());
                System.out.println("ptTo Y:" + (int) ptTo.getY());
                mainMap.setAddressLocation(posFrom);
                mainMap.setAddressLocation(posTo);

                painter.setRenderer(new WaypointRenderer() {

                    public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {
                        Stroke linkStroke = new BasicStroke((float) (Math.min(2, 0.00005)), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
                        g.setStroke(linkStroke);
                        g.drawLine((int) ptFrom.getX(), (int) ptFrom.getY(), (int) ptTo.getX(), (int) ptTo.getY());
                        return true;
                    }
                });
            }
            for (int i = 0; i < nodes.size(); i++) {
                mainMap.setAddressLocation(new GeoPosition(nodes.get(i).lat, nodes.get(i).lon));
                waypoints.add(new Waypoint(nodes.get(i).lat, nodes.get(i).lon));
                painter.setWaypoints(waypoints);
                painter.setRenderer(new WaypointRenderer() {

                    public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {

                        Toolkit toolkit = Toolkit.getDefaultToolkit();
                        Image i = toolkit.getImage("/home/stefano/NetBeansProjects/FreimapGSoC/src/gfx/wrt.png");
                        g.drawImage(i, 0, 0, null);
                        g.drawLine(120, 320, 650, 320);
                        return true;
                    }
                });

            }

            mainMap.setOverlayPainter(painter);
            mainMap.repaint();

        }
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
     * @return a jbutton
     */
    public JButton getZoomInButton() {
        return this.zoomIn;
    }//OK

    /**
     * returns a reference to the zoom out button
     * @return a jbutton
     */
    public JButton getZoomOutButton() {
        return this.zoomOut;
    }//OK

    /**
     * returns a reference to the zoom slider
     * @return a jslider
     */
    public JSlider getZoomSlider() {
        return this.zoomSlider;
    }//OK

    public static void addWaypoint(Double lat, Double lon, FreiNode node) {
    }

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

    //END OF MAP METHODS################################à
    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = FreimapGSoCApp.getApplication().getMainFrame();
            aboutBox = new FreimapGSoCAboutBox(mainFrame);
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
        jPanel1 = new javax.swing.JPanel();
        mainMap = new org.jdesktop.swingx.JXMapViewer();
        miniMap = new org.jdesktop.swingx.JXMapViewer();
        zoomSlider = new javax.swing.JSlider();
        zoomIn = new javax.swing.JButton();
        zoomOut = new javax.swing.JButton();
        latitudeValue = new javax.swing.JLabel();
        longitudeValue = new javax.swing.JLabel();
        serviceD = new javax.swing.JButton();
        goToDefaultPosition = new javax.swing.JButton();
        jSlider1 = new javax.swing.JSlider();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
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
        jMenu3 = new javax.swing.JMenu();
        jCheckBoxMenuItem5 = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxMenuItem6 = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxMenuItem7 = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxMenuItem1 = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxMenuItem2 = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxMenuItem3 = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxMenuItem4 = new javax.swing.JCheckBoxMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();

        mainPanel.setName("mainPanel"); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanel1.setName("jPanel1"); // NOI18N

        mainMap.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        mainMap.setCenterPosition(def);
        mainMap.setDoubleBuffered(false);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(freimapgsoc.FreimapGSoCApp.class).getContext().getResourceMap(FreimapGSoCView.class);
        mainMap.setFont(resourceMap.getFont("mainMap.font")); // NOI18N
        mainMap.setName("mainMap"); // NOI18N
        mainMap.setRecenterOnClickEnabled(true);
        mainMap.setTileFactory(tf);
        mainMap.setZoom(14);
        mainMap.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                mainMapMousePressed(evt);
            }
        });
        mainMap.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                mainMapMouseMoved(evt);
            }
        });

        miniMap.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        miniMap.setCenterPosition(def);
        miniMap.setFont(resourceMap.getFont("miniMap.font")); // NOI18N
        miniMap.setName("miniMap"); // NOI18N
        miniMap.setTileFactory(tf);
        miniMap.setZoom(14);

        javax.swing.GroupLayout miniMapLayout = new javax.swing.GroupLayout(miniMap);
        miniMap.setLayout(miniMapLayout);
        miniMapLayout.setHorizontalGroup(
            miniMapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 120, Short.MAX_VALUE)
        );
        miniMapLayout.setVerticalGroup(
            miniMapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        zoomSlider.setMaximum(14);
        zoomSlider.setInverted(true);
        zoomSlider.setName("zoomSlider"); // NOI18N
        zoomSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                zoomSliderStateChanged(evt);
            }
        });

        zoomIn.setFont(resourceMap.getFont("zoomIn.font")); // NOI18N
        zoomIn.setText(resourceMap.getString("zoomIn.text")); // NOI18N
        zoomIn.setName("zoomIn"); // NOI18N
        zoomIn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomInActionPerformed(evt);
            }
        });

        zoomOut.setFont(resourceMap.getFont("zoomIn.font")); // NOI18N
        zoomOut.setText(resourceMap.getString("zoomOut.text")); // NOI18N
        zoomOut.setName("zoomOut"); // NOI18N
        zoomOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomOutActionPerformed(evt);
            }
        });

        latitudeValue.setText(resourceMap.getString("latitudeValue.text")); // NOI18N
        latitudeValue.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        latitudeValue.setName("latitudeValue"); // NOI18N

        longitudeValue.setText(resourceMap.getString("longitudeValue.text")); // NOI18N
        longitudeValue.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        longitudeValue.setName("longitudeValue"); // NOI18N

        javax.swing.GroupLayout mainMapLayout = new javax.swing.GroupLayout(mainMap);
        mainMap.setLayout(mainMapLayout);
        mainMapLayout.setHorizontalGroup(
            mainMapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainMapLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainMapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainMapLayout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addComponent(zoomOut, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(zoomIn, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(mainMapLayout.createSequentialGroup()
                        .addComponent(latitudeValue, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(longitudeValue, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 569, Short.MAX_VALUE)
                .addComponent(miniMap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(mainMapLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(zoomSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(738, Short.MAX_VALUE))
        );
        mainMapLayout.setVerticalGroup(
            mainMapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainMapLayout.createSequentialGroup()
                .addGroup(mainMapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(mainMapLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(miniMap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(mainMapLayout.createSequentialGroup()
                        .addGap(477, 477, 477)
                        .addComponent(zoomSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(mainMapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(zoomIn)
                            .addComponent(zoomOut))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(mainMapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(longitudeValue, javax.swing.GroupLayout.DEFAULT_SIZE, 14, Short.MAX_VALUE)
                            .addComponent(latitudeValue, javax.swing.GroupLayout.DEFAULT_SIZE, 14, Short.MAX_VALUE))))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mainMap, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(mainMap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

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

        jSlider1.setPaintTrack(false);
        jSlider1.setName("timeLine"); // NOI18N

        jLabel2.setIcon(resourceMap.getIcon("jLabel2.icon")); // NOI18N
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setToolTipText(resourceMap.getString("jLabel2.toolTipText")); // NOI18N
        jLabel2.setDoubleBuffered(true);
        jLabel2.setName("jLabel2"); // NOI18N

        jLabel3.setIcon(resourceMap.getIcon("jLabel3.icon")); // NOI18N
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setToolTipText(resourceMap.getString("jLabel3.toolTipText")); // NOI18N
        jLabel3.setDisabledIcon(resourceMap.getIcon("jLabel3.disabledIcon")); // NOI18N
        jLabel3.setDoubleBuffered(true);
        jLabel3.setName("jLabel3"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jList1.setName("jList1"); // NOI18N
        jScrollPane1.setViewportView(jList1);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, 611, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel2)
                        .addGap(35, 35, 35)
                        .addComponent(serviceD)
                        .addGap(18, 18, 18)
                        .addComponent(goToDefaultPosition)))
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(goToDefaultPosition)
                            .addComponent(serviceD)))
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel2)
                            .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(16, 16, 16))
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        jMenu1.setIcon(resourceMap.getIcon("jMenu1.icon")); // NOI18N
        jMenu1.setText(resourceMap.getString("jMenu1.text")); // NOI18N
        jMenu1.setName("jMenu1"); // NOI18N

        xmlOpenMenu.setIcon(resourceMap.getIcon("xmlOpenMenu.icon")); // NOI18N
        xmlOpenMenu.setText(resourceMap.getString("xmlOpenMenu.text")); // NOI18N
        xmlOpenMenu.setName("xmlOpenMenu"); // NOI18N
        xmlOpenMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xmlOpenMenuActionPerformed(evt);
            }
        });
        jMenu1.add(xmlOpenMenu);

        txtOpenMenu.setIcon(resourceMap.getIcon("txtOpenMenu.icon")); // NOI18N
        txtOpenMenu.setText(resourceMap.getString("txtOpenMenu.text")); // NOI18N
        txtOpenMenu.setName("txtOpenMenu"); // NOI18N
        txtOpenMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtOpenMenuActionPerformed(evt);
            }
        });
        jMenu1.add(txtOpenMenu);

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
        exitMenuItem.setIcon(resourceMap.getIcon("exitMenuItem.icon")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        jMenu2.setText(resourceMap.getString("jMenu2.text")); // NOI18N
        jMenu2.setName("jMenu2"); // NOI18N

        jSeparator1.setName("jSeparator1"); // NOI18N
        jMenu2.add(jSeparator1);

        jMenuItem8.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem8.setIcon(resourceMap.getIcon("jMenuItem8.icon")); // NOI18N
        jMenuItem8.setText(resourceMap.getString("jMenuItem8.text")); // NOI18N
        jMenuItem8.setName("jMenuItem8"); // NOI18N
        jMenu2.add(jMenuItem8);

        jMenuItem6.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem6.setIcon(resourceMap.getIcon("jMenuItem6.icon")); // NOI18N
        jMenuItem6.setText(resourceMap.getString("jMenuItem6.text")); // NOI18N
        jMenuItem6.setName("jMenuItem6"); // NOI18N
        jMenu2.add(jMenuItem6);

        jMenuItem5.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem5.setIcon(resourceMap.getIcon("jMenuItem5.icon")); // NOI18N
        jMenuItem5.setText(resourceMap.getString("jMenuItem5.text")); // NOI18N
        jMenuItem5.setName("jMenuItem5"); // NOI18N
        jMenu2.add(jMenuItem5);

        jSeparator2.setName("jSeparator2"); // NOI18N
        jMenu2.add(jSeparator2);

        jMenuItem12.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem12.setIcon(resourceMap.getIcon("jMenuItem12.icon")); // NOI18N
        jMenuItem12.setText(resourceMap.getString("jMenuItem12.text")); // NOI18N
        jMenuItem12.setName("jMenuItem12"); // NOI18N
        jMenu2.add(jMenuItem12);

        jMenuItem7.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem7.setIcon(resourceMap.getIcon("jMenuItem7.icon")); // NOI18N
        jMenuItem7.setText(resourceMap.getString("jMenuItem7.text")); // NOI18N
        jMenuItem7.setName("jMenuItem7"); // NOI18N
        jMenu2.add(jMenuItem7);

        jMenuItem9.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem9.setIcon(resourceMap.getIcon("jMenuItem9.icon")); // NOI18N
        jMenuItem9.setText(resourceMap.getString("jMenuItem9.text")); // NOI18N
        jMenuItem9.setName("jMenuItem9"); // NOI18N
        jMenu2.add(jMenuItem9);

        menuBar.add(jMenu2);

        jMenu3.setText(resourceMap.getString("jMenu3.text")); // NOI18N
        jMenu3.setName("jMenu3"); // NOI18N

        jCheckBoxMenuItem5.setSelected(true);
        jCheckBoxMenuItem5.setText(resourceMap.getString("jCheckBoxMenuItem5.text")); // NOI18N
        jCheckBoxMenuItem5.setIcon(resourceMap.getIcon("jCheckBoxMenuItem5.icon")); // NOI18N
        jCheckBoxMenuItem5.setName("jCheckBoxMenuItem5"); // NOI18N
        jMenu3.add(jCheckBoxMenuItem5);

        jCheckBoxMenuItem6.setSelected(true);
        jCheckBoxMenuItem6.setText(resourceMap.getString("jCheckBoxMenuItem6.text")); // NOI18N
        jCheckBoxMenuItem6.setIcon(resourceMap.getIcon("jCheckBoxMenuItem6.icon")); // NOI18N
        jCheckBoxMenuItem6.setName("jCheckBoxMenuItem6"); // NOI18N
        jMenu3.add(jCheckBoxMenuItem6);

        jCheckBoxMenuItem7.setSelected(true);
        jCheckBoxMenuItem7.setText(resourceMap.getString("jCheckBoxMenuItem7.text")); // NOI18N
        jCheckBoxMenuItem7.setIcon(resourceMap.getIcon("jCheckBoxMenuItem7.icon")); // NOI18N
        jCheckBoxMenuItem7.setName("jCheckBoxMenuItem7"); // NOI18N
        jMenu3.add(jCheckBoxMenuItem7);

        jCheckBoxMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_4, java.awt.event.InputEvent.CTRL_MASK));
        jCheckBoxMenuItem1.setSelected(true);
        jCheckBoxMenuItem1.setText(resourceMap.getString("jCheckBoxMenuItem1.text")); // NOI18N
        jCheckBoxMenuItem1.setIcon(resourceMap.getIcon("jCheckBoxMenuItem1.icon")); // NOI18N
        jCheckBoxMenuItem1.setName("jCheckBoxMenuItem1"); // NOI18N
        jMenu3.add(jCheckBoxMenuItem1);

        jCheckBoxMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_5, java.awt.event.InputEvent.CTRL_MASK));
        jCheckBoxMenuItem2.setSelected(true);
        jCheckBoxMenuItem2.setText(resourceMap.getString("jCheckBoxMenuItem2.text")); // NOI18N
        jCheckBoxMenuItem2.setIcon(resourceMap.getIcon("jCheckBoxMenuItem2.icon")); // NOI18N
        jCheckBoxMenuItem2.setName("jCheckBoxMenuItem2"); // NOI18N
        jMenu3.add(jCheckBoxMenuItem2);

        jCheckBoxMenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_6, java.awt.event.InputEvent.CTRL_MASK));
        jCheckBoxMenuItem3.setSelected(true);
        jCheckBoxMenuItem3.setText(resourceMap.getString("jCheckBoxMenuItem3.text")); // NOI18N
        jCheckBoxMenuItem3.setIcon(resourceMap.getIcon("jCheckBoxMenuItem3.icon")); // NOI18N
        jCheckBoxMenuItem3.setName("jCheckBoxMenuItem3"); // NOI18N
        jMenu3.add(jCheckBoxMenuItem3);

        jCheckBoxMenuItem4.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_7, java.awt.event.InputEvent.CTRL_MASK));
        jCheckBoxMenuItem4.setSelected(true);
        jCheckBoxMenuItem4.setText(resourceMap.getString("jCheckBoxMenuItem4.text")); // NOI18N
        jCheckBoxMenuItem4.setIcon(resourceMap.getIcon("jCheckBoxMenuItem4.icon")); // NOI18N
        jCheckBoxMenuItem4.setName("jCheckBoxMenuItem4"); // NOI18N
        jMenu3.add(jCheckBoxMenuItem4);

        menuBar.add(jMenu3);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setIcon(resourceMap.getIcon("aboutMenuItem.icon")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        jMenuItem1.setIcon(resourceMap.getIcon("jMenuItem1.icon")); // NOI18N
        jMenuItem1.setText(resourceMap.getString("jMenuItem1.text")); // NOI18N
        jMenuItem1.setName("jMenuItem1"); // NOI18N
        helpMenu.add(jMenuItem1);

        menuBar.add(helpMenu);

        setComponent(mainPanel);
        setMenuBar(menuBar);
    }// </editor-fold>//GEN-END:initComponents

    private void mainMapMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mainMapMousePressed

        //switch (evt.getClickCount()) {
        //case 1: new NodeInfoFlow().setVisible(true); break;

        //}
        if (evt.isPopupTrigger()) {
            // contestMenu.show(evt.getComponent(),evt.getX(), evt.getY());
        }        // TODO add your handling code here:
    }//GEN-LAST:event_mainMapMousePressed

    private void serviceDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serviceDActionPerformed
        InetAddress intf = null;
        try {
            intf = InetAddress.getByName("localhost");
            try {
                new PopUp(JmDNS.create(intf)).setVisible(true);
            } catch (IOException ex) {
                Logger.getLogger(FreimapGSoCView.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(PopUpMain.class.getName()).log(Level.SEVERE, null, ex);
        }        // TODO add your handling code here:
    }//GEN-LAST:event_serviceDActionPerformed

    private void zoomOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomOutActionPerformed
        setZoom(mainMap.getZoom() + 1);
        drawAll(links, nodes);// TODO add your handling code here:
    }//GEN-LAST:event_zoomOutActionPerformed

    private void zoomInActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomInActionPerformed
        setZoom(mainMap.getZoom() - 1);
        drawAll(links, nodes);// TODO add your handling code here:
    }//GEN-LAST:event_zoomInActionPerformed

    private void zoomSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_zoomSliderStateChanged
        if (!zoomChanging) {
            mainMap.setZoom(zoomSlider.getValue());
            // drawAll(links,nodes);
        }        // TODO add your handling code here:
    }//GEN-LAST:event_zoomSliderStateChanged

    private void mainMapMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mainMapMouseMoved

        Point pt = evt.getPoint();
        // convert to geoposition
        GeoPosition gp = mainMap.convertPointToGeoPosition(new Point2D.Double(evt.getX(), evt.getY()));
        //System.out.println("GeoPosition" + gp);
        DecimalFormat fmt = new DecimalFormat("#00.00000");
        latitudeValue.setText(fmt.format(gp.getLatitude()));
        longitudeValue.setText(fmt.format(gp.getLongitude()));

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
                System.out.println("Saved: " + file.getName() + ".\n");
            } else {
                System.out.println("Save command cancelled by user." + "\n");
            }
        }
    }//GEN-LAST:event_saveAsMenuActionPerformed

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
                upperLeft2D = map.getTileFactory().geoToPixel(upperLeft, map.getZoom());
                lowerRight2D = map.getTileFactory().geoToPixel(lowerRight, map.getZoom());


                g = (Graphics2D) g.create();
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
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JButton goToDefaultPosition;
    public javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem1;
    public javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem2;
    public javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem3;
    public javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem4;
    public javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem5;
    public javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem6;
    public javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem7;
    public javax.swing.JLabel jLabel2;
    public javax.swing.JLabel jLabel3;
    public javax.swing.JList jList1;
    public javax.swing.JMenu jMenu1;
    public javax.swing.JMenu jMenu2;
    public javax.swing.JMenu jMenu3;
    public javax.swing.JMenuItem jMenuItem1;
    public javax.swing.JMenuItem jMenuItem12;
    public javax.swing.JMenuItem jMenuItem5;
    public javax.swing.JMenuItem jMenuItem6;
    public javax.swing.JMenuItem jMenuItem7;
    public javax.swing.JMenuItem jMenuItem8;
    public javax.swing.JMenuItem jMenuItem9;
    public javax.swing.JPanel jPanel1;
    public javax.swing.JScrollPane jScrollPane1;
    public javax.swing.JSeparator jSeparator1;
    public javax.swing.JSeparator jSeparator2;
    public javax.swing.JSeparator jSeparator3;
    public javax.swing.JSlider jSlider1;
    public javax.swing.JMenuItem jsOpenMenu;
    public javax.swing.JLabel latitudeValue;
    public javax.swing.JLabel longitudeValue;
    public static org.jdesktop.swingx.JXMapViewer mainMap;
    public javax.swing.JPanel mainPanel;
    public javax.swing.JMenuBar menuBar;
    public static org.jdesktop.swingx.JXMapViewer miniMap;
    public javax.swing.JMenuItem saveAsMenu;
    public javax.swing.JButton serviceD;
    public javax.swing.JMenuItem txtOpenMenu;
    public javax.swing.JMenuItem xmlOpenMenu;
    public static javax.swing.JButton zoomIn;
    public static javax.swing.JButton zoomOut;
    public javax.swing.JSlider zoomSlider;
    // End of variables declaration//GEN-END:variables
    private JDialog aboutBox;
    private GeoPosition def;//OK
    private Double DEFAULT_LAT = 0.0;//OK
    private Double DEFAULT_LON = 0.0;//OK
    private TileFactory tf; //OK
    private Runtime runtime;
    //MainMap and Minimap Variables
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
}
