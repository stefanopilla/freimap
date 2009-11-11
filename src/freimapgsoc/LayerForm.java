/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * NewJFrame.java
 *
 * Created on 10-nov-2009, 2.07.17
 */
package freimapgsoc;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.swing.JButton;
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
import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.Painter;

/**
 *
 * @author stefanopilla
 */
public class LayerForm extends javax.swing.JFrame implements DataSource {

    /** Creates new form NewJFrame */
    public LayerForm() {

        //public TileFactoryInfo(int minimumZoomLevel,int maximumZoomLevel,int totalMapZoom,int tileSize,boolean xr2l,boolean yt2b,String baseURL,String xparam,String yparam,String zparam)(
        TileFactoryInfo info = new TileFactoryInfo(0, maxzoomlevel, totalmapzoom, 256, false, false, "http://tile.openstreetmap.org", "x", "y", "z") {

            public String getTileUrl(int x, int y, int zoom) {
                zoom = maxzoomlevel - zoom;
                return this.baseURL + "/" + zoom + "/" + x + "/" + y + ".png";
            }
        };

        //In a future: possibilty to change this with settings menu parameters; now is in Italy Rome
        tf = new DefaultTileFactory(info);

        initComponents();
        initMapComponents();
        printDateTime();

    }

    public LayerForm(Layer l1) {
        this.l=l1;
        //public TileFactoryInfo(int minimumZoomLevel,int maximumZoomLevel,int totalMapZoom,int tileSize,boolean xr2l,boolean yt2b,String baseURL,String xparam,String yparam,String zparam)(
        TileFactoryInfo info = new TileFactoryInfo(0, maxzoomlevel, totalmapzoom, 256, false, false, "http://tile.openstreetmap.org", "x", "y", "z") {
            public String getTileUrl(int x, int y, int zoom) {
                zoom = maxzoomlevel - zoom;
                return this.baseURL + "/" + zoom + "/" + x + "/" + y + ".png";
            }
        };

        //In a future: possibilty to change this with settings menu parameters; now is in Italy Rome
        tf = new DefaultTileFactory(info);
System.out.println("Now Initialing Components....");
        initComponents();
        System.out.println("Now Initialing MAP Components....");
        initMapComponents();
        System.out.println("Now PRINT DATE....");
        printDateTime();
        System.out.println(l1.nodes.size());
        System.out.println(l1.links.size());
        initData(l1);
    }


    public void initData(Layer l1){

        for (int i = 0; i < l1.nodes.size(); i++) {
           System.out.println("node name:" + l1.nodes.get(i).name);
            listOfNodes.addItem(l1.nodes.get(i).name);
}
        for (int i = 0; i < l1.links.size(); i++) {
            System.out.println("____________________________");
            System.out.println("LINK FROM: " + l1.links.get(i).source.ip + "TO: "+l1.links.get(i).dest.ip);

            System.out.println("HNA:" + l1.links.get(i).HNA);
            System.out.println("ETX:" + l1.links.get(i).etx);
            System.out.println("LQ:" + l1.links.get(i).lq);
            System.out.println("NLQ:" + l1.links.get(i).nlq);
            System.out.println("TCP:" + l1.links.get(i).tcp);
            System.out.println("UDP:" + l1.links.get(i).udp);
            System.out.println("PACKETS:" + l1.links.get(i).packets);
            System.out.println("ICMP:" + l1.links.get(i).icmp);
            System.out.println("OTHER:" + l1.links.get(i).other);
            System.out.println("SOURCE:" + l1.links.get(i).source.name);
            System.out.println("DEST:"+l1.links.get(i).dest.name);
            System.out.println("BYTES:" + l1.links.get(i).bytes);
}
        for (int i = 0; i < l1.nodes.size(); i++) {
            System.out.println("____________________");
            System.out.println("ID:" +l1.nodes.get(i).name);
            System.out.println("IP:" +l1.nodes.get(i).ip);
            System.out.println("NAME:" +l1.nodes.get(i).name);
            System.out.println("LAT:" +l1.nodes.get(i).lat);
            System.out.println("LON:" +l1.nodes.get(i).lon);

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

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainMap = new org.jdesktop.swingx.JXMapViewer();
        miniMap = new org.jdesktop.swingx.JXMapViewer();
        zoomSlider = new javax.swing.JSlider();
        zoomButtonIn = new javax.swing.JButton();
        zoomButtonOut = new javax.swing.JButton();
        listOfNodes = new javax.swing.JComboBox();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jTabbedPane3 = new javax.swing.JTabbedPane();
        jTabbedPane4 = new javax.swing.JTabbedPane();
        jTabbedPane5 = new javax.swing.JTabbedPane();
        jTabbedPane6 = new javax.swing.JTabbedPane();
        jTabbedPane7 = new javax.swing.JTabbedPane();
        jTabbedPane8 = new javax.swing.JTabbedPane();
        dateInfo = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        mainMap.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        mainMap.setName("mainMap"); // NOI18N
        mainMap.setTileFactory(tf
        );
        mainMap.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        miniMap.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        miniMap.setName("miniMap"); // NOI18N
        miniMap.setTileFactory(tf);

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

        mainMap.add(miniMap, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 300, -1, -1));

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
        mainMap.add(zoomSlider, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 190, -1, -1));

        zoomButtonIn.setText("+");
        zoomButtonIn.setName("zoomButtonIn"); // NOI18N
        zoomButtonIn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomButtonInActionPerformed(evt);
            }
        });
        mainMap.add(zoomButtonIn, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 160, -1, -1));

        zoomButtonOut.setText("-");
        zoomButtonOut.setName("zoomButtonOut"); // NOI18N
        zoomButtonOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomButtonOutActionPerformed(evt);
            }
        });
        mainMap.add(zoomButtonOut, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 380, -1, -1));

        listOfNodes.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        listOfNodes.setName("listOfNodes"); // NOI18N

        jTabbedPane1.setTabPlacement(javax.swing.JTabbedPane.LEFT);
        jTabbedPane1.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        jTabbedPane2.setName("jTabbedPane2"); // NOI18N
        jTabbedPane1.addTab("Maps", jTabbedPane2);

        jTabbedPane3.setName("jTabbedPane3"); // NOI18N
        jTabbedPane1.addTab("ListOfNodes", jTabbedPane3);

        jTabbedPane4.setName("jTabbedPane4"); // NOI18N
        jTabbedPane1.addTab("Links", jTabbedPane4);

        jTabbedPane5.setName("jTabbedPane5"); // NOI18N
        jTabbedPane1.addTab("Flows", jTabbedPane5);

        jTabbedPane6.setName("jTabbedPane6"); // NOI18N

        jTabbedPane7.setName("jTabbedPane7"); // NOI18N
        jTabbedPane6.addTab("All", jTabbedPane7);

        jTabbedPane8.setName("jTabbedPane8"); // NOI18N
        jTabbedPane6.addTab("SD for 1 Node", jTabbedPane8);

        jTabbedPane1.addTab("ServiceDiscovery", jTabbedPane6);

        dateInfo.setText(" ");
        dateInfo.setName("dateInfo"); // NOI18N

        jMenuBar1.setName("jMenuBar1"); // NOI18N

        jMenu1.setText("File");
        jMenu1.setName("jMenu1"); // NOI18N
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenu2.setName("jMenu2"); // NOI18N
        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dateInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 294, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(237, 237, 237)
                        .addComponent(listOfNodes, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(56, 56, 56))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mainMap, javax.swing.GroupLayout.DEFAULT_SIZE, 892, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(listOfNodes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mainMap, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 465, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 124, Short.MAX_VALUE)
                .addComponent(dateInfo)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void zoomButtonOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomButtonOutActionPerformed
        setZoom(mainMap.getZoom() + 1);
    }//GEN-LAST:event_zoomButtonOutActionPerformed

    private void zoomButtonInActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomButtonInActionPerformed
        setZoom(mainMap.getZoom() - 1);
    }//GEN-LAST:event_zoomButtonInActionPerformed

    private void zoomSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_zoomSliderStateChanged
        if (!zoomChanging) {
            mainMap.setZoom(zoomSlider.getValue());
        }
    }//GEN-LAST:event_zoomSliderStateChanged

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
                g.drawString(" ", 50, map.getHeight() - 10);
            }
        }, wp));


        // adapter to move the minimap after the main map has moved
        MouseInputAdapter ma = new MouseInputAdapter() {

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

    //END OF INITMAPCOMPONENTS()
    //MAP METHODS ##################################
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
    //MainMap and Minimap Variables
    private GeoPosition def;//OK
    private TileFactory tf; //OK
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

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new LayerForm(new Layer(new LatLonJsDataSource().init("file:///var/run/latlon.js"))).setVisible(true);
            }
        });
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

    public long getLastAvailableTime() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getFirstAvailableTime() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getClosestUpdateTime(long time) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public MapNode getNodeByName(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public MapNode getNodeById(String id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Vector<Link> getLinks(long time) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Vector<Link> getLinksFromSource(String id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Vector<Link> getLinksFromDest(String id) {
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
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel dateInfo;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTabbedPane jTabbedPane3;
    private javax.swing.JTabbedPane jTabbedPane4;
    private javax.swing.JTabbedPane jTabbedPane5;
    private javax.swing.JTabbedPane jTabbedPane6;
    private javax.swing.JTabbedPane jTabbedPane7;
    private javax.swing.JTabbedPane jTabbedPane8;
    private javax.swing.JComboBox listOfNodes;
    private org.jdesktop.swingx.JXMapViewer mainMap;
    private org.jdesktop.swingx.JXMapViewer miniMap;
    private javax.swing.JButton zoomButtonIn;
    private javax.swing.JButton zoomButtonOut;
    private javax.swing.JSlider zoomSlider;
    // End of variables declaration//GEN-END:variables
    private final int maxzoomlevel = 14;
    private final int totalmapzoom = 14;
    private Layer l;
}
