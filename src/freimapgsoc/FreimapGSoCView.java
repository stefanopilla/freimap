/*
 * FreimapGSoCView.java
 */

package freimapgsoc;

import ServiceDiscovery.ServiceDiscovery;
import ServiceDiscovery.ServiceDiscoveryMain;
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
public class FreimapGSoCView extends FrameView implements DataSource{

    public FreimapGSoCView(SingleFrameApplication app) {
        super(app);


        final int maxzoomlevel = 17;
        final int totalmapzoom=17;


       //public TileFactoryInfo(int minimumZoomLevel,int maximumZoomLevel,int totalMapZoom,int tileSize,boolean xr2l,boolean yt2b,String baseURL,String xparam,String yparam,String zparam)(
      TileFactoryInfo info = new TileFactoryInfo(0,maxzoomlevel,totalmapzoom, 256, false, false, "http://tile.openstreetmap.org","x","y","z") {
                        public String getTileUrl(int x, int y, int zoom) {
                zoom = maxzoomlevel-zoom;
                return this.baseURL +"/"+zoom+"/"+x+"/"+y+".png";
            }
        };

        //In a future: possibilty to change this with settings menu parameters; now is in Italy Rome
        tf= new DefaultTileFactory(info);
        DEFAULT_LAT=41.8639;
        DEFAULT_LON=12.5535;
        def = new GeoPosition(DEFAULT_LAT, DEFAULT_LON);

        initComponents();
        initMapComponents();

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String)(evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer)(evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
     config=new Configurator();
        sources = new HashMap<String, DataSource>();
    try {
      HashMap<String, Object> ds = (HashMap<String, Object>)config.get("datasources");
      HashMap<String, HashMap<String, Object>> ds2subconfig = new HashMap<String, HashMap<String, Object>>();
      Iterator<String> i = ds.keySet().iterator();
      while (i.hasNext()) {
        String id   = i.next();
        HashMap<String, Object> subconfig = (HashMap<String, Object>) ds.get(id);
        @SuppressWarnings("static-access")
        String claz = config.getS("class", subconfig);
        Class<DataSource> csource=(Class<DataSource>)Class.forName(claz); //this cast cannot be checked!
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

        nodes=new Vector<FreiNode>();
        links=new Vector<FreiLink>();

        for(int t=0;t<links.size(); t++){
           //System.out.println("Links Element("+ t +"):" +links.elementAt(t));
        }
        links=source.getLinks(0);
        nodes=source.getNodeList();

        for(int k=0;k<nodes.size(); k++){
           // System.out.println("id: "+nodes.get(k) + "lat: "+ nodes.get(k).lat + " lon: "+ nodes.get(k).lon);
                addWaypoint(nodes.get(k).lat, nodes.get(k).lon, nodes.get(k));
        }


            drawAll(links,nodes);
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

    public void drawAll(Vector<FreiLink> links,  Vector<FreiNode> nodes){
       if(nodes.size()==0){
          for (int i=0; i<links.size(); i++){
            FreiLink selectedLink= links.elementAt(i);
            //System.out.println("selectedLink.from.lat: "+ selectedLink.from.lat + "selectedLink.from.lon: "+ selectedLink.from.lon);
            //System.out.println("selectedLink.to.lat: "+ selectedLink.to.lat + "selectedLink.to.lon"+ selectedLink.to.lon);
            GeoPosition posFrom=new GeoPosition(selectedLink.from.lat, selectedLink.from.lon);
            GeoPosition posTo=new GeoPosition(selectedLink.to.lat, selectedLink.to.lon);
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
        g.drawLine((int)ptFrom.getX(), (int)ptFrom.getY(), (int)ptTo.getX(), (int)ptTo.getY());
            return false;
        }
    });
           }
       }else if(links.size()==0){
           for(int i=0;i<nodes.size(); i++){
            mainMap.setAddressLocation(new GeoPosition(nodes.get(i).lat,nodes.get(i).lon));
            waypoints.add(new Waypoint(nodes.get(i).lat,nodes.get(i).lon));
        painter.setWaypoints(waypoints);
        painter.setRenderer(new WaypointRenderer() {
            public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {

      Toolkit toolkit = Toolkit.getDefaultToolkit();
      Image i=toolkit.getImage("/home/stefano/NetBeansProjects/FreimapGSoC/src/gfx/wrt.png");
      g.drawImage(i, 0, 0, null);
        return true;
    }

});
           }

       }else{
           for (int i=0; i<links.size(); i++){
            FreiLink selectedLink= links.elementAt(i);
            System.out.println("selectedLink.from.lat: "+ selectedLink.from.lat + "selectedLink.from.lon: "+ selectedLink.from.lon);
            System.out.println("selectedLink.to.lat: "+ selectedLink.to.lat + "selectedLink.to.lon"+ selectedLink.to.lon);
            GeoPosition posFrom=new GeoPosition(selectedLink.from.lat, selectedLink.from.lon);
            GeoPosition posTo=new GeoPosition(selectedLink.to.lat, selectedLink.to.lon);
            System.out.println("posFromLat:" +posFrom.getLatitude() + " posFromLon: "+ posFrom.getLongitude());
            System.out.println("posToLat:" +posTo.getLatitude() + " posToLon: "+ posTo.getLongitude());

    final Point2D ptFrom = mainMap.getTileFactory().geoToPixel(posFrom, mainMap.getZoom());
    final Point2D ptTo = mainMap.getTileFactory().geoToPixel(posTo, mainMap.getZoom());
            System.out.println("ptFrom X:" +(int)ptFrom.getX());
            System.out.println("ptFrom Y:" +(int)ptFrom.getY());
            System.out.println("ptTo X:" +(int)ptTo.getX());
            System.out.println("ptTo Y:" +(int)ptTo.getY());
            mainMap.setAddressLocation(posFrom);
            mainMap.setAddressLocation(posTo);

    painter.setRenderer(new WaypointRenderer() {
        public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {
            Stroke linkStroke = new BasicStroke((float)(Math.min(2,0.00005)), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            g.setStroke(linkStroke);
        g.drawLine((int)ptFrom.getX(), (int)ptFrom.getY(), (int)ptTo.getX(), (int)ptTo.getY());
            return true;
        }
    });
       }
             for(int i=0;i<nodes.size(); i++){
            mainMap.setAddressLocation(new GeoPosition(nodes.get(i).lat,nodes.get(i).lon));
            waypoints.add(new Waypoint(nodes.get(i).lat,nodes.get(i).lon));
        painter.setWaypoints(waypoints);
        painter.setRenderer(new WaypointRenderer() {
            public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {

      Toolkit toolkit = Toolkit.getDefaultToolkit();
      Image i=toolkit.getImage("/home/stefano/NetBeansProjects/FreimapGSoC/src/gfx/wrt.png");
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
        miniMap.setZoom(mainMap.getZoom()+4);
        if(sliderReversed) {
            zoomSlider.setValue(zoomSlider.getMaximum()-zoom);
        } else {
            zoomSlider.setValue(zoom);
            mainMap.repaint();
        }
        zoomChanging = false;
    }

    /**
     * Indicates if the mini-map is currently visible
     * @return the current value of the mini-map property
     */
    public boolean isMiniMapVisible() {
        return miniMapVisible;
    }

    /**
     * Sets if the mini-map should be visible
     * @param miniMapVisible a new value for the miniMap property
     */
    public void setMiniMapVisible(boolean miniMapVisible) {
        boolean old = this.isMiniMapVisible();
        this.miniMapVisible = miniMapVisible;
        miniMap.setVisible(miniMapVisible);
        firePropertyChange("miniMapVisible",old,this.isMiniMapVisible());
    }

    /**
     * Indicates if the zoom slider is currently visible
     * @return the current value of the zoomSliderVisible property
     */
    public boolean isZoomSliderVisible() {
        return zoomSliderVisible;
    }

    /**
     * Sets if the zoom slider should be visible
     * @param zoomSliderVisible the new value of the zoomSliderVisible property
     */
    public void setZoomSliderVisible(boolean zoomSliderVisible) {
        boolean old = this.isZoomSliderVisible();
        this.zoomSliderVisible = zoomSliderVisible;
        zoomSlider.setVisible(zoomSliderVisible);
        firePropertyChange("zoomSliderVisible",old,this.isZoomSliderVisible());
    }

    /**
     * Indicates if the zoom buttons are visible. This is a bound property
     * and can be listed for using a PropertyChangeListener
     * @return current value of the zoomButtonsVisible property
     */
    public boolean isZoomButtonsVisible() {
        return zoomButtonsVisible;
    }

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
        firePropertyChange("zoomButtonsVisible",old,this.isZoomButtonsVisible());
    }

    /**
     * Sets the tile factory for both embedded JXMapViewer components.
     * Calling this method will also reset the center and zoom levels
     * of both maps, as well as the bounds of the zoom slider.
     * @param fact the new TileFactory
     */
    public void setTileFactory(TileFactory fact) {
        mainMap.setTileFactory(fact);
        mainMap.setZoom(fact.getInfo().getDefaultZoomLevel());
        mainMap.setCenterPosition(new GeoPosition(0,0));
        miniMap.setTileFactory(fact);
        miniMap.setZoom(fact.getInfo().getDefaultZoomLevel()+3);
        miniMap.setCenterPosition(new GeoPosition(0,0));
        zoomSlider.setMinimum(fact.getInfo().getMinimumZoomLevel());
        zoomSlider.setMaximum(fact.getInfo().getMaximumZoomLevel());
    }

    public void setCenterPosition(GeoPosition pos) {
        mainMap.setCenterPosition(pos);
        miniMap.setCenterPosition(pos);
    }

    /**
     * Returns a reference to the main embedded JXMapViewer component
     * @return the main map
     */
    public JXMapViewer getMainMap() {
        return this.mainMap;
    }

    /**
     * Returns a reference to the mini embedded JXMapViewer component
     * @return the minimap JXMapViewer component
     */
    public JXMapViewer getMiniMap() {
        return this.miniMap;
    }

    /**
     * returns a reference to the zoom in button
     * @return a jbutton
     */
    public JButton getZoomInButton() {
       return this.zoomIn;
    }

    /**
     * returns a reference to the zoom out button
     * @return a jbutton
     */
    public JButton getZoomOutButton() {
        return this.zoomOut;
    }

    /**
     * returns a reference to the zoom slider
     * @return a jslider
     */
    public JSlider getZoomSlider() {
        return this.zoomSlider;
    }

    public static void addWaypoint(Double lat, Double lon, FreiNode node) {
   
    }

    //Get String Latitude from the Map
     private String getLat (GeoPosition pos){
        Double lat=pos.getLatitude();
        return lat.toString();
    }

    //Get String Longitude from the Map
    private String getLon (GeoPosition pos){
        Double lon=pos.getLatitude();
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
        Rectangle mapPosition = new Rectangle(p,d);
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
        sxPanel = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        serviceD = new javax.swing.JButton();
        goToDefaultPosition = new javax.swing.JButton();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        latitudeValue = new javax.swing.JLabel();
        longitudeValue = new javax.swing.JLabel();
        zoomSlider = new javax.swing.JSlider();
        zoomOut = new javax.swing.JButton();
        zoomIn = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        mainMap = new org.jdesktop.swingx.JXMapViewer();
        miniMap = new org.jdesktop.swingx.JXMapViewer();
        statusMessageLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        statusAnimationLabel = new javax.swing.JLabel();
        cBaseLabel = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        jPopupMenu1 = new javax.swing.JPopupMenu();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jCheckBoxMenuItem1 = new javax.swing.JCheckBoxMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        jMenuItem3 = new javax.swing.JMenuItem();

        mainPanel.setName("mainPanel"); // NOI18N

        sxPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        sxPanel.setName("sxPanel"); // NOI18N

        jSeparator1.setName("jSeparator1"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(freimapgsoc.FreimapGSoCApp.class).getContext().getResourceMap(FreimapGSoCView.class);
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

        jLabel15.setText(resourceMap.getString("jLabel15.text")); // NOI18N
        jLabel15.setName("jLabel15"); // NOI18N

        jLabel16.setText(resourceMap.getString("jLabel16.text")); // NOI18N
        jLabel16.setName("jLabel16"); // NOI18N

        latitudeValue.setText(resourceMap.getString("latitudeValue.text")); // NOI18N
        latitudeValue.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        latitudeValue.setName("latitudeValue"); // NOI18N

        longitudeValue.setText(resourceMap.getString("longitudeValue.text")); // NOI18N
        longitudeValue.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        longitudeValue.setName("longitudeValue"); // NOI18N

        zoomSlider.setMaximum(17);
        zoomSlider.setValue(14);
        zoomSlider.setName("zoomSlider"); // NOI18N
        zoomSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                zoomSliderStateChanged(evt);
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

        zoomIn.setFont(resourceMap.getFont("zoomIn.font")); // NOI18N
        zoomIn.setText(resourceMap.getString("zoomIn.text")); // NOI18N
        zoomIn.setName("zoomIn"); // NOI18N
        zoomIn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomInActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout sxPanelLayout = new javax.swing.GroupLayout(sxPanel);
        sxPanel.setLayout(sxPanelLayout);
        sxPanelLayout.setHorizontalGroup(
            sxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sxPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(sxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(sxPanelLayout.createSequentialGroup()
                        .addGroup(sxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(serviceD)
                            .addComponent(goToDefaultPosition))
                        .addGap(122, 122, 122))
                    .addGroup(sxPanelLayout.createSequentialGroup()
                        .addGroup(sxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel15)
                            .addComponent(latitudeValue, javax.swing.GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(sxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel16)
                            .addComponent(longitudeValue, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(29, 29, 29))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, sxPanelLayout.createSequentialGroup()
                        .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE)
                        .addContainerGap())))
            .addGroup(sxPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(zoomSlider, javax.swing.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(sxPanelLayout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addComponent(zoomOut, javax.swing.GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(zoomIn, javax.swing.GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE)
                .addGap(44, 44, 44))
        );
        sxPanelLayout.setVerticalGroup(
            sxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sxPanelLayout.createSequentialGroup()
                .addGap(75, 75, 75)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(serviceD)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(goToDefaultPosition)
                .addGap(193, 193, 193)
                .addGroup(sxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(zoomIn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(zoomOut, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(zoomSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(sxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(7, 7, 7)
                .addGroup(sxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(longitudeValue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(latitudeValue, javax.swing.GroupLayout.DEFAULT_SIZE, 17, Short.MAX_VALUE))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanel1.setName("jPanel1"); // NOI18N

        mainMap.setCenterPosition(def);
        mainMap.setDoubleBuffered(false);
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
            .addGap(0, 100, Short.MAX_VALUE)
        );
        miniMapLayout.setVerticalGroup(
            miniMapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout mainMapLayout = new javax.swing.GroupLayout(mainMap);
        mainMap.setLayout(mainMapLayout);
        mainMapLayout.setHorizontalGroup(
            mainMapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainMapLayout.createSequentialGroup()
                .addContainerGap(656, Short.MAX_VALUE)
                .addComponent(miniMap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        mainMapLayout.setVerticalGroup(
            mainMapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainMapLayout.createSequentialGroup()
                .addContainerGap(380, Short.MAX_VALUE)
                .addComponent(miniMap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainMap, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainMap, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        statusMessageLabel.setText(resourceMap.getString("statusMessageLabel.text")); // NOI18N
        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        statusAnimationLabel.setText(resourceMap.getString("statusAnimationLabel.text")); // NOI18N
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        cBaseLabel.setBackground(resourceMap.getColor("cBaseLabel.background")); // NOI18N
        cBaseLabel.setFont(resourceMap.getFont("cBaseLabel.font")); // NOI18N
        cBaseLabel.setForeground(resourceMap.getColor("cBaseLabel.foreground")); // NOI18N
        cBaseLabel.setText(resourceMap.getString("cBaseLabel.text")); // NOI18N
        cBaseLabel.setName("cBaseLabel"); // NOI18N

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(sxPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                        .addComponent(statusAnimationLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 706, Short.MAX_VALUE)
                        .addComponent(cBaseLabel)
                        .addGap(18, 18, 18)
                        .addComponent(statusMessageLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(21, 21, 21))))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(statusAnimationLabel)
                        .addComponent(statusMessageLabel))
                    .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(cBaseLabel)
                        .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sxPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(2085, 2085, 2085))
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setIcon(resourceMap.getIcon("exitMenuItem.icon")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

        jMenu1.setText(resourceMap.getString("jMenu1.text")); // NOI18N
        jMenu1.setName("jMenu1"); // NOI18N
        jPopupMenu1.add(jMenu1);

        jMenuItem1.setText(resourceMap.getString("jMenuItem1.text")); // NOI18N
        jMenuItem1.setName("jMenuItem1"); // NOI18N
        jPopupMenu1.add(jMenuItem1);

        jMenuItem2.setText(resourceMap.getString("jMenuItem2.text")); // NOI18N
        jMenuItem2.setName("jMenuItem2"); // NOI18N
        jPopupMenu1.add(jMenuItem2);

        jCheckBoxMenuItem1.setSelected(true);
        jCheckBoxMenuItem1.setText(resourceMap.getString("jCheckBoxMenuItem1.text")); // NOI18N
        jCheckBoxMenuItem1.setName("jCheckBoxMenuItem1"); // NOI18N
        jPopupMenu1.add(jCheckBoxMenuItem1);

        jSeparator2.setName("jSeparator2"); // NOI18N
        jPopupMenu1.add(jSeparator2);

        jMenuItem3.setText(resourceMap.getString("jMenuItem3.text")); // NOI18N
        jMenuItem3.setName("jMenuItem3"); // NOI18N
        jPopupMenu1.add(jMenuItem3);

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
      InetAddress intf=null;
        try {
            intf = InetAddress.getByName("localhost");
            try {
                new ServiceDiscovery(JmDNS.create(intf)).setVisible(true);
            } catch (IOException ex) {
                Logger.getLogger(FreimapGSoCView.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(ServiceDiscoveryMain.class.getName()).log(Level.SEVERE, null, ex);
        }        // TODO add your handling code here:
    }//GEN-LAST:event_serviceDActionPerformed

    private void zoomOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomOutActionPerformed
    setZoom(mainMap.getZoom()+1);
    drawAll(links,nodes);// TODO add your handling code here:
    }//GEN-LAST:event_zoomOutActionPerformed

    private void zoomInActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomInActionPerformed
    setZoom(mainMap.getZoom()-1);
    drawAll(links,nodes);// TODO add your handling code here:
    }//GEN-LAST:event_zoomInActionPerformed

    private void zoomSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_zoomSliderStateChanged
     if(!zoomChanging){
            mainMap.setZoom(zoomSlider.getValue());
            drawAll(links,nodes);
        }        // TODO add your handling code here:
    }//GEN-LAST:event_zoomSliderStateChanged

    private void mainMapMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mainMapMouseMoved
            mainMap.add(cBaseLabel);
            
                Point pt = evt.getPoint();
                // convert to geoposition
                GeoPosition gp = mainMap.convertPointToGeoPosition(new Point2D.Double(evt.getX(),evt.getY()));
                //System.out.println("GeoPosition" + gp);
                DecimalFormat fmt = new DecimalFormat("#00.00000");
                latitudeValue.setText(fmt.format(gp.getLatitude()));
                longitudeValue.setText(fmt.format(gp.getLongitude()));

         //C-BASE LABEL
           GeoPosition cbgp = new GeoPosition(52.51298, 13.42012);
        //convert to world bitmap
        Point2D gp_pt = mainMap.getTileFactory().geoToPixel(cbgp, mainMap.getZoom());
        //convert to screen
        Rectangle rect = mainMap.getViewportBounds();
        Point converted_gp_pt = new Point((int)gp_pt.getX()-rect.x,
                                          (int)gp_pt.getY()-rect.y);
        //check if near the mouse
        if(converted_gp_pt.distance(evt.getPoint()) < 10 && mainMap.getZoom()<5) {
            cBaseLabel.setLocation(converted_gp_pt);
            cBaseLabel.setVisible(true);
        } else {
            cBaseLabel.setVisible(false);
        }

    }//GEN-LAST:event_mainMapMouseMoved


    //MAP COMPONENTS
    public void initMapComponents(){


         mainMap.setCenterPosition(new GeoPosition(0,0));
        miniMap.setCenterPosition(new GeoPosition(0,0));
        mainMap.setRestrictOutsidePanning(true);
        miniMap.setRestrictOutsidePanning(true);

        Set<Waypoint> wps = new HashSet<Waypoint>();
        WaypointPainter wp = new WaypointPainter();
        wp.setWaypoints(wps);


        mainMap.setOverlayPainter(new CompoundPainter(new Painter<JXMapViewer>() {
            public void paint(Graphics2D g, JXMapViewer map, int width, int height) {
                g.setPaint(Color.WHITE);
                g.drawString(" ",50,
                        map.getHeight()-10);
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
                Point2D mapCenter = (Point2D)evt.getNewValue();
                TileFactory tf = mainMap.getTileFactory();
                GeoPosition mapPos = tf.pixelToGeo(mapCenter,mainMap.getZoom());
               miniMap.setCenterPosition(mapPos);
            }
        });


        mainMap.addPropertyChangeListener("centerPosition", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                mapCenterPosition = (GeoPosition)evt.getNewValue();
                miniMap.setCenterPosition(mapCenterPosition);
                Point2D pt = miniMap.getTileFactory().geoToPixel(mapCenterPosition,miniMap.getZoom());
                miniMap.setCenter(pt);
               miniMap.repaint();
            }
        });

        mainMap.addPropertyChangeListener("zoom",new PropertyChangeListener() {
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
                GeoPosition upperLeft = mainMap.getTileFactory().pixelToGeo(upperLeft2D,mainMap.getZoom());
                GeoPosition lowerRight = mainMap.getTileFactory().pixelToGeo(lowerRight2D,mainMap.getZoom());

                // convert to Point2Ds on the mini-map
                upperLeft2D = map.getTileFactory().geoToPixel(upperLeft,map.getZoom());
                lowerRight2D = map.getTileFactory().geoToPixel(lowerRight,map.getZoom());


                g = (Graphics2D) g.create();
                Rectangle rect = map.getViewportBounds();
                //p("rect = " + rect);
                g.translate(-rect.x, -rect.y);
                Point2D centerpos = map.getTileFactory().geoToPixel(mapCenterPosition, map.getZoom());
                //p("center pos = " + centerpos);
                g.setPaint(Color.RED);
                //g.drawRect((int)centerpos.getX()-30,(int)centerpos.getY()-30,60,60);
                g.drawRect((int)upperLeft2D.getX(),(int)upperLeft2D.getY(),
                        (int)(lowerRight2D.getX()-upperLeft2D.getX()),
                        (int)(lowerRight2D.getY()-upperLeft2D.getY()));
                g.setPaint(new Color(255,0,0,50));
                g.fillRect((int)upperLeft2D.getX(),(int)upperLeft2D.getY(),
                        (int)(lowerRight2D.getX()-upperLeft2D.getX()),
                        (int)(lowerRight2D.getY()-upperLeft2D.getY()));
                //g.drawOval((int)lowerRight2D.getX(),(int)lowerRight2D.getY(),1,1);
                g.dispose();
            }
        });

        setZoom(14);// HACK joshy: hack, i shouldn't need this here
        this.setCenterPosition(new GeoPosition(0,0));
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JLabel cBaseLabel;
    public javax.swing.JButton goToDefaultPosition;
    public javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem1;
    public javax.swing.JLabel jLabel15;
    public javax.swing.JLabel jLabel16;
    public javax.swing.JMenu jMenu1;
    public javax.swing.JMenuItem jMenuItem1;
    public javax.swing.JMenuItem jMenuItem2;
    public javax.swing.JMenuItem jMenuItem3;
    public javax.swing.JPanel jPanel1;
    public javax.swing.JPopupMenu jPopupMenu1;
    public javax.swing.JSeparator jSeparator1;
    public javax.swing.JSeparator jSeparator2;
    public javax.swing.JLabel latitudeValue;
    public javax.swing.JLabel longitudeValue;
    public static org.jdesktop.swingx.JXMapViewer mainMap;
    public javax.swing.JPanel mainPanel;
    public javax.swing.JMenuBar menuBar;
    public static org.jdesktop.swingx.JXMapViewer miniMap;
    public javax.swing.JProgressBar progressBar;
    public javax.swing.JButton serviceD;
    public javax.swing.JLabel statusAnimationLabel;
    public javax.swing.JLabel statusMessageLabel;
    public javax.swing.JPanel sxPanel;
    public static javax.swing.JButton zoomIn;
    public static javax.swing.JButton zoomOut;
    public javax.swing.JSlider zoomSlider;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;

    private GeoPosition def;
    private Double DEFAULT_LAT=0.0;
    private Double DEFAULT_LON=0.0;
    private TileFactory tf;
    private Runtime runtime;

    //MainMap and Minimap Variables
    public static boolean miniMapVisible = true;
    public static boolean zoomSliderVisible = true;
    public static boolean zoomButtonsVisible = true;
    public static final boolean sliderReversed = false;
    private static WaypointPainter painter = new WaypointPainter();
    private static Set<Waypoint> waypoints = new HashSet<Waypoint>();
    private FreiNode uplink = new FreiNode("0.0.0.0/0.0.0.0");

    private Point2D mapCenter = new Point2D.Double(0,0);
    private GeoPosition mapCenterPosition = new GeoPosition(0,0);
    private boolean zoomChanging = false;

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
