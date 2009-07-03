/*
 * FreimapGSoCView.java
 */

package freimapgsoc;

import ServiceDiscovery.ServiceDiscovery;
import ServiceDiscovery.ServiceDiscoveryMain;
import freimapgsoc.MapLayer;
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
import javax.swing.JPanel;
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

        initComponents();

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

        /*
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
           
    

      }
    } catch (Exception ex) {
      ex.printStackTrace();
      return;
    }
    */
    }

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
        statusMessageLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        statusAnimationLabel = new javax.swing.JLabel();
        cBaseLabel = new javax.swing.JLabel();
        mapLayer1 = new freimapgsoc.MapLayer();
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

        javax.swing.GroupLayout sxPanelLayout = new javax.swing.GroupLayout(sxPanel);
        sxPanel.setLayout(sxPanelLayout);
        sxPanelLayout.setHorizontalGroup(
            sxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sxPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(sxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(sxPanelLayout.createSequentialGroup()
                        .addComponent(serviceD)
                        .addGap(18, 18, 18)
                        .addComponent(goToDefaultPosition, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(20, 20, 20))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, sxPanelLayout.createSequentialGroup()
                        .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        sxPanelLayout.setVerticalGroup(
            sxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sxPanelLayout.createSequentialGroup()
                .addGroup(sxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serviceD)
                    .addComponent(goToDefaultPosition))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(437, Short.MAX_VALUE))
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

        mapLayer1.setName("mapLayer1"); // NOI18N

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
                        .addComponent(mapLayer1, javax.swing.GroupLayout.DEFAULT_SIZE, 789, Short.MAX_VALUE)
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
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sxPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mapLayer1, javax.swing.GroupLayout.DEFAULT_SIZE, 530, Short.MAX_VALUE))
                .addContainerGap())
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

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JLabel cBaseLabel;
    public javax.swing.JButton goToDefaultPosition;
    public javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem1;
    public javax.swing.JMenu jMenu1;
    public javax.swing.JMenuItem jMenuItem1;
    public javax.swing.JMenuItem jMenuItem2;
    public javax.swing.JMenuItem jMenuItem3;
    public javax.swing.JPopupMenu jPopupMenu1;
    public javax.swing.JSeparator jSeparator1;
    public javax.swing.JSeparator jSeparator2;
    public javax.swing.JPanel mainPanel;
    public freimapgsoc.MapLayer mapLayer1;
    public javax.swing.JMenuBar menuBar;
    public javax.swing.JProgressBar progressBar;
    public javax.swing.JButton serviceD;
    public javax.swing.JLabel statusAnimationLabel;
    public javax.swing.JLabel statusMessageLabel;
    public javax.swing.JPanel sxPanel;
    // End of variables declaration//GEN-END:variables

    MapLayer mapLayer=new MapLayer();
    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;


            private JDialog aboutBox;



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
