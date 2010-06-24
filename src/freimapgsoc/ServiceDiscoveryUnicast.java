/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ServiceDiscovery.java
 *
 * Created on 19-giu-2009, 21.30.56
 */
package freimapgsoc;

import freimapgsoc.log;
import freimapgsoc.Link;
import freimapgsoc.MapNode;
import freimapgsoc.addServices;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import javax.jmdns.ServiceTypeListener;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author stefano
 */
public class ServiceDiscoveryUnicast extends javax.swing.JFrame implements ServiceListener, ServiceTypeListener, ListSelectionListener {

    public ServiceDiscoveryUnicast(String ip) throws SocketException {
        System.out.println("Costructor PopUP(String ip)");
        initComponents();
        try {
            this.jmdns = JmDNS.create(ip);
            jmdns.addServiceTypeListener(this);
            System.out.println("HostName:" + jmdns.getHostName());
            System.out.println("Interface: " + jmdns.getInterface().toString());
            // register some well known types
            list = new String[]{
                        "_http._tcp" + domain + ".",
                        "_ftp._tcp" + domain + ".",
                        "_sftp._tcp" + domain + ".",
                        "_tftp._tcp" + domain + ".",
                        "_ssh._tcp" + domain + ".",
                        "_smb._tcp" + domain + ".",
                        "_printer._tcp" + domain + ".",
                        "_airport._tcp" + domain + ".",
                        "_afpovertcp._tcp" + domain + ".",
                        "_nfs._tcp" + domain + ".",
                        "_webdav._tcp" + domain + ".",
                        "_presence._tcp" + domain + ".",
                        "_eppc._tcp" + domain + ".",
                        "_telnet._tcp" + domain + ".",
                        "_raop._tcp" + domain + ".",
                        "_ipp._tcp" + domain + ".",
                        "_pdl-datastream._tcp" + domain + ".",
                        "_riousbprint._tcp" + domain + ".",
                        "_daap._tcp" + domain + ".",
                        "_distcc._tcp" + domain + ".",
                        "_xserveraid._tcp" + domain + ".",
                        "_net-assistant._tcp" + domain + ".",
                        "_workstation._tcp" + domain + ".",
                        "_h323._tcp" + domain + ".",
                        "_sip._udp" + domain + "."
                    };
            for (int i = 0; i < list.length; i++) {
                jmdns.registerServiceType(list[i]);
            }
        } catch (IOException ex) {
            Logger.getLogger(ServiceDiscovery.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    /**
     *
     * @param 
     * @return
     */
    @SuppressWarnings("static-access")
    public ServiceDiscoveryUnicast(MapNode node) {
        System.out.println("Costructor PopUP(MapNode node)");
        initComponents();
        InetAddress address = null;
        try {
            this.jmdns = JmDNS.create(address.getByName(node.ip));
            jmdns.addServiceTypeListener(this);
            infoNode(node);
        } catch (IOException ex) {
            Logger.getLogger(ServiceDiscovery.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

   
    public ServiceDiscoveryUnicast(JmDNS jmdns) throws IOException {
        System.out.println("Costructor PopUP(jmdns)");
        initComponents();


        this.jmdns = jmdns;
        //  jmdns.addServiceTypeListener(this);


        for (int i = 0; i
                < list.length; i++) {
            jmdns.registerServiceType(list[i]);


        }
    }

    public void infoNode(MapNode node) {
        for(int i=0;i<node.inter.size();i++){
            nodeIPs.insertItemAt(node.inter.elementAt(i), i);
        }
        nodeIPs.setSelectedIndex(0);
        nodeName.setText(node.toString());
        nodeLatLon.setText(node.lat + " / " + node.lon);
        nodegw.setText(node.uptime);
        //System.out.println(node.attributes.values());
        Set i = node.attributes.entrySet();
        Set keySet = node.attributes.keySet();
        StringBuffer buf=new StringBuffer();
        for (Object key : keySet) {
            Object value = node.attributes.get(key);
            buf.append(value+" ");
        }

    }


    /**
     * Add a service.
     */
    @Override
    public void serviceAdded(ServiceEvent event) {
        final String name = event.getName();
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                insertSorted(services, name);

            }
        });


    }

    /**
     * Remove a service.
     */
    @Override
    public void serviceRemoved(ServiceEvent event) {
        final String name = event.getName();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                services.removeElement(name);
            }
        });
    }

    /**
     * A new service type was <discovered.
     */
    @Override
    public void serviceTypeAdded(ServiceEvent event) {
        final String atype = event.getType();
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                insertSorted(types, atype);
            }
        });

    }

    private boolean isPresent(String type) {
        for (int i = 0; i < list.length; i++) {
            if (list[i].equals(type)) {
                return true;
            }
        }
        return false;
    }

    public void insertSorted(DefaultListModel model, String value) {
        for (int i = 0, n = model.getSize(); i
                < n; i++) {
            if (value.compareToIgnoreCase((String) model.elementAt(i)) < 0) {
                model.insertElementAt(value, i);
                return;
            }
        }
        model.addElement(value);
    }

    /**
     * Resolve a service.
     */
    @Override
    public void serviceResolved(ServiceEvent event) {
        String name = event.getName();
        String atype = event.getType();
        ServiceInfo ainfo = event.getInfo();
        if (!name.equals(serviceList.getSelectedValue())) {
            StringBuffer buf = new StringBuffer();
            buf.append(name);
            buf.append("\n");
            buf.append(atype);
            buf.append("\n");
            buf.append(ainfo.getServer());
            buf.append(':');
            buf.append(ainfo.getPort());
            buf.append('\n');
            buf.append(ainfo.getAddress().toString().substring(1));
            buf.append(':');
            buf.append(ainfo.getPort());
            buf.append('\n');
            for (Enumeration names = ainfo.getPropertyNames(); names.hasMoreElements();) {
                String prop = (String) names.nextElement();
                buf.append(prop);
                buf.append('=');
                buf.append(ainfo.getPropertyString(prop));
                buf.append('\n');
            }
            this.info.setText(buf.toString());
        } else {
            info.setText("Service unreacheable!");
        }
        if (name.equals(serviceList.getSelectedValue())) {
        }
    }

    public void deleteDuplicate(JList list) {
        //IMPLEMENT ME....!
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     * @return
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        nodesGroup = new javax.swing.ButtonGroup();
        flowGroup = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        nodeIPs = new javax.swing.JComboBox();
        nodeName = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        nodeLatLon = new javax.swing.JLabel();
        jComboBox2 = new javax.swing.JComboBox();
        jLabel16 = new javax.swing.JLabel();
        nodegw = new javax.swing.JLabel();
        upTimeLab = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        attributeArea = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        serviceList = new JList(services);
        jScrollPane2 = new javax.swing.JScrollPane();
        typeList = new JList(types);
        jScrollPane3 = new javax.swing.JScrollPane();
        info = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setAlwaysOnTop(true);
        setLocationByPlatform(true);
        setName("Form"); // NOI18N
        setResizable(false);

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance().getContext().getResourceMap(ServiceDiscoveryUnicast.class);
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setPreferredSize(new java.awt.Dimension(500, 162));

        jLabel7.setFont(resourceMap.getFont("jLabel7.font")); // NOI18N
        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        jLabel8.setFont(resourceMap.getFont("jLabel7.font")); // NOI18N
        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N

        nodeIPs.setFont(resourceMap.getFont("nodeIPs.font")); // NOI18N
        nodeIPs.setModel(new javax.swing.DefaultComboBoxModel(new String[] { " " }));
        nodeIPs.setName("nodeIPs"); // NOI18N

        nodeName.setFont(resourceMap.getFont("nodeName.font")); // NOI18N
        nodeName.setText(resourceMap.getString("nodeName.text")); // NOI18N
        nodeName.setName("nodeName"); // NOI18N

        jLabel11.setFont(resourceMap.getFont("jLabel7.font")); // NOI18N
        jLabel11.setText(resourceMap.getString("jLabel11.text")); // NOI18N
        jLabel11.setName("jLabel11"); // NOI18N

        nodeLatLon.setFont(resourceMap.getFont("jLabel7.font")); // NOI18N
        nodeLatLon.setText(resourceMap.getString("nodeLatLon.text")); // NOI18N
        nodeLatLon.setName("nodeLatLon"); // NOI18N

        jComboBox2.setFont(resourceMap.getFont("jComboBox2.font")); // NOI18N
        jComboBox2.setName("jComboBox2"); // NOI18N

        jLabel16.setFont(resourceMap.getFont("jLabel7.font")); // NOI18N
        jLabel16.setText(resourceMap.getString("jLabel16.text")); // NOI18N
        jLabel16.setName("jLabel16"); // NOI18N

        nodegw.setFont(resourceMap.getFont("jLabel7.font")); // NOI18N
        nodegw.setText(resourceMap.getString("nodegw.text")); // NOI18N
        nodegw.setName("nodegw"); // NOI18N

        upTimeLab.setFont(resourceMap.getFont("upTimeLab.font")); // NOI18N
        upTimeLab.setText(resourceMap.getString("upTimeLab.text")); // NOI18N
        upTimeLab.setName("upTimeLab"); // NOI18N

        jLabel9.setFont(resourceMap.getFont("jLabel9.font")); // NOI18N
        jLabel9.setText(resourceMap.getString("jLabel9.text")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N

        attributeArea.setFont(resourceMap.getFont("attributeArea.font")); // NOI18N
        attributeArea.setText(resourceMap.getString("attributeArea.text")); // NOI18N
        attributeArea.setName("attributeArea"); // NOI18N

        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(nodeLatLon))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(upTimeLab, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(nodegw))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                            .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(attributeArea, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(95, 95, 95)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(nodeName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(50, 50, 50)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel16)
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(nodeIPs, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jComboBox2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jButton1))
                .addGap(247, 247, 247))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(nodeName))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel11)
                            .addComponent(nodeLatLon))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(upTimeLab)
                            .addComponent(nodegw)))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(nodeIPs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addGap(35, 35, 35)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel16)
                                .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(attributeArea, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addGap(6, 6, 6))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Service Discovery"));
        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setPreferredSize(new java.awt.Dimension(500, 174));

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        serviceList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        serviceList.setName("serviceList"); // NOI18N
        jScrollPane1.setViewportView(serviceList);

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        typeList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        typeList.setName("typeList"); // NOI18N
        jScrollPane2.setViewportView(typeList);
        typeList.addListSelectionListener(this);

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        info.setColumns(20);
        info.setEditable(false);
        info.setFont(resourceMap.getFont("info.font")); // NOI18N
        info.setRows(5);
        info.setText(resourceMap.getString("info.text")); // NOI18N
        info.setName("info"); // NOI18N
        jScrollPane3.setViewportView(info);

        jLabel1.setFont(resourceMap.getFont("jLabel2.font")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel2.setFont(resourceMap.getFont("jLabel2.font")); // NOI18N
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jLabel3.setFont(resourceMap.getFont("jLabel2.font")); // NOI18N
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 11, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 11, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 11, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jScrollPane1, 0, 0, Short.MAX_VALUE)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel3.border.title"))); // NOI18N
        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setPreferredSize(new java.awt.Dimension(500, 174));

        jLabel17.setName("jLabel17"); // NOI18N

        jLabel18.setName("jLabel18"); // NOI18N

        jLabel19.setName("jLabel19"); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(94, 94, 94)
                .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(285, 285, 285)
                .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(20, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 11, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 11, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 11, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(194, 194, 194))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, 0, 679, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 679, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 679, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    @Override
    public void valueChanged(ListSelectionEvent evt) {
        if (evt.getValueIsAdjusting()) {
            if (evt.getSource()
                    == typeList) {
                type = (String) typeList.getSelectedValue();
                System.out.println(type);
                if (!types.isEmpty()) {
                    jmdns.removeServiceListener(type, this);
                    services.setSize(0);
                    info.setText("");
                }
                if (type != null) {

                    jmdns.addServiceListener(type, this);

                }
            } else if (evt.getSource() == serviceList) {
                String name = (String) serviceList.getSelectedValue();
                System.out.println("name: " + name);
                if (name == null) {
                    info.setText("Service is unreacheable!");
                } else {
                    ServiceInfo service = jmdns.getServiceInfo(type, name);
                    if (service == null) {
                        info.setText("Service not found!");
                    } else {
                        jmdns.requestServiceInfo(type, name);
                    }
                }
            }
        }
    }

    
    public void addServices() {
        new addServices(types, jmdns).setVisible(true);
        //jmdns.registerServiceType(types.lastElement().toString());
    }

    public void reloadServices() {
        try {
            jmdns.unregisterAllServices();
            services.removeAllElements();
            types.removeAllElements();
            for (int i = 0; i < list.length; i++) {
                types.addElement(list[i]);
                jmdns.registerServiceType(list[i]);

            }
        } catch (Exception e) {
            log.append(e.getMessage().toString());
        }

    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel attributeArea;
    private javax.swing.ButtonGroup flowGroup;
    private javax.swing.JTextArea info;
    private javax.swing.JButton jButton1;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JComboBox nodeIPs;
    private javax.swing.JLabel nodeLatLon;
    private javax.swing.JLabel nodeName;
    private javax.swing.JLabel nodegw;
    private javax.swing.ButtonGroup nodesGroup;
    private javax.swing.JList serviceList;
    private javax.swing.JList typeList;
    private javax.swing.JLabel upTimeLab;
    // End of variables declaration//GEN-END:variables
    private JmDNS jmdns;
    private String type;
    private DefaultListModel services = new DefaultListModel();
    private DefaultListModel types = new DefaultListModel();
    private MapNode from;
    private MapNode to;
    private Link link;
    private String list[];
    private String domain;
}
