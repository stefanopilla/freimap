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
public class ServiceDiscovery extends javax.swing.JFrame implements ServiceListener, ServiceTypeListener, ListSelectionListener {

    /**
     *
     * @param 
     * @return
     */
    @SuppressWarnings("static-access")
    public ServiceDiscovery() throws SocketException {
        System.out.println("Costructor PopUP()");
        initComponents();
        System.out.println("Select your OLSR interface: ");
        String ip = selectIpAddress();
        domain = selectDomain();
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


    public ServiceDiscovery(JmDNS jmdns) throws IOException {
        System.out.println("Costructor PopUP(jmdns)");
        initComponents();
        this.jmdns = jmdns;
        //  jmdns.addServiceTypeListener(this);
        for (int i = 0; i
                < list.length; i++) {
            jmdns.registerServiceType(list[i]);
        }
    }

    private String selectIpAddress() {
        try {
            Object[] poss = null;
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            Vector<InetAddress> v = new Vector<InetAddress>();
            Enumeration<InetAddress> inetAddresses;
            for (NetworkInterface netint : Collections.list(nets)) {
                inetAddresses = netint.getInetAddresses();
                for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                    if (inetAddress.isReachable(2000)) {
                        v.add(inetAddress);
                    }
                }
            }
            poss = new Object[v.size()];
            v.copyInto(poss);

            for (int i = 0; i < v.size(); i++) {
                String tmp = v.elementAt(i).toString().substring(1);
                poss[i] = tmp;
            }
            String s = (String) JOptionPane.showInputDialog(null, "Select interface connected to OLSR Network: ", "Interface Selection", JOptionPane.PLAIN_MESSAGE, null, poss, poss[0]);
            if ((s != null) && (s.length() > 0)) {
                return s;
            }
        } catch (IOException ex) {
            Logger.getLogger(ServiceDiscovery.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "127.0.0.1";
    }

    private String selectDomain() {
        String s = (String) JOptionPane.showInputDialog(null, "Write your domain: ");
        if ((s != null) && (s.length() > 0)) {
            return "." + s;
        }
        return ".local";
    }

  
    /**
     * Add a service.
     */
    @Override
    public void serviceAdded(ServiceEvent event) {
        final String name = event.getName();
        sdLog.append("Service added: " + name + "\n");
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
        sdLog.append("Service removed: " + name + "\n");
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
        sdLog.append("ServiceType added: " + atype + "\n");
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
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        serviceList = new JList(services);
        jScrollPane2 = new javax.swing.JScrollPane();
        typeList = new JList(types);
        jScrollPane3 = new javax.swing.JScrollPane();
        info = new javax.swing.JTextArea();
        jScrollPane4 = new javax.swing.JScrollPane();
        sdLog = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        addTypeButton = new javax.swing.JButton();
        liveServicesButton = new javax.swing.JButton();
        reloadServices = new javax.swing.JButton();
        removeSelItem = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance().getContext().getResourceMap(ServiceDiscovery.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setAlwaysOnTop(true);
        setLocationByPlatform(true);
        setName("Form"); // NOI18N
        setResizable(false);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, resourceMap.getString("jPanel2.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, resourceMap.getFont("jPanel2.border.titleFont"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        serviceList.setFont(resourceMap.getFont("serviceList.font")); // NOI18N
        serviceList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        serviceList.setToolTipText(resourceMap.getString("serviceList.toolTipText")); // NOI18N
        serviceList.setName("serviceList"); // NOI18N
        jScrollPane1.setViewportView(serviceList);

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        typeList.setFont(resourceMap.getFont("serviceList.font")); // NOI18N
        typeList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        typeList.setToolTipText(resourceMap.getString("typeList.toolTipText")); // NOI18N
        typeList.setName("typeList"); // NOI18N
        jScrollPane2.setViewportView(typeList);
        typeList.addListSelectionListener(this);

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        info.setColumns(20);
        info.setEditable(false);
        info.setFont(resourceMap.getFont("serviceList.font")); // NOI18N
        info.setRows(5);
        info.setName("info"); // NOI18N
        jScrollPane3.setViewportView(info);

        jScrollPane4.setName("jScrollPane4"); // NOI18N

        sdLog.setColumns(20);
        sdLog.setEditable(false);
        sdLog.setFont(resourceMap.getFont("sdLog.font")); // NOI18N
        sdLog.setRows(5);
        sdLog.setDoubleBuffered(true);
        sdLog.setEnabled(false);
        sdLog.setName("sdLog"); // NOI18N
        jScrollPane4.setViewportView(sdLog);

        jLabel1.setName("jLabel1"); // NOI18N

        jLabel2.setName("jLabel2"); // NOI18N

        jLabel3.setName("jLabel3"); // NOI18N

        addTypeButton.setText(resourceMap.getString("addTypeButton.text")); // NOI18N
        addTypeButton.setActionCommand(resourceMap.getString("addTypeButton.actionCommand")); // NOI18N
        addTypeButton.setName("addTypeButton"); // NOI18N
        addTypeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addTypeButtonActionPerformed(evt);
            }
        });

        liveServicesButton.setText(resourceMap.getString("liveServicesButton.text")); // NOI18N
        liveServicesButton.setName("liveServicesButton"); // NOI18N
        liveServicesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                liveServicesButtonActionPerformed(evt);
            }
        });

        reloadServices.setText(resourceMap.getString("reloadServices.text")); // NOI18N
        reloadServices.setEnabled(false);
        reloadServices.setName("reloadServices"); // NOI18N
        reloadServices.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reloadServicesActionPerformed(evt);
            }
        });

        removeSelItem.setText(resourceMap.getString("removeSelItem.text")); // NOI18N
        removeSelItem.setActionCommand(resourceMap.getString("removeSelItem.actionCommand")); // NOI18N
        removeSelItem.setName("removeSelItem"); // NOI18N
        removeSelItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeSelItemActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(addTypeButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(removeSelItem)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(reloadServices)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(liveServicesButton))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE)
                            .addComponent(jScrollPane4))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 11, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 11, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 11, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addTypeButton)
                    .addComponent(liveServicesButton)
                    .addComponent(reloadServices)
                    .addComponent(removeSelItem)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addTypeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addTypeButtonActionPerformed
        String s = (String) JOptionPane.showInputDialog(null, "Select the type that you want to add:");
        if ((s != null) && (s.length() > 0)) {
            if (isPresent(type) == true) {
                int res = JOptionPane.showConfirmDialog(null, "Type already Present", "Warning!", JOptionPane.OK_CANCEL_OPTION);
                if (res != 1) {
                    s = (String) JOptionPane.showInputDialog(null, "Select the type that you want to add:");
                }
            } else {
                jmdns.registerServiceType(s);
                jmdns.addServiceListener(s, this);
                list[list.length - 1] = s;
            }
        }
    }//GEN-LAST:event_addTypeButtonActionPerformed

    private void liveServicesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_liveServicesButtonActionPerformed
        reloadServices.setEnabled(true);
        services.removeAllElements();
        types.removeAllElements();
        info.removeAll();
        String atype = "_services._dns-sd._udp" + domain + ".";
        jmdns.registerServiceType(atype);
}//GEN-LAST:event_liveServicesButtonActionPerformed

    private void reloadServicesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reloadServicesActionPerformed
        reloadServices();
    }//GEN-LAST:event_reloadServicesActionPerformed

    private void removeSelItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeSelItemActionPerformed
        int index = typeList.getSelectedIndex();
        types.removeElementAt(index);
    }//GEN-LAST:event_removeSelItemActionPerformed

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
                    sdLog.append(this + " valueChanged() type:" + type + " name:" + name);
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
            sdLog.append("All Services Reloaded...\n");
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

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
           public void run() {
                try {
                    new ServiceDiscovery().setVisible(true);
                } catch (SocketException ex) {
                    Logger.getLogger(ServiceDiscovery.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addTypeButton;
    private javax.swing.ButtonGroup flowGroup;
    private javax.swing.JTextArea info;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JButton liveServicesButton;
    private javax.swing.ButtonGroup nodesGroup;
    private javax.swing.JButton reloadServices;
    private javax.swing.JButton removeSelItem;
    private javax.swing.JTextArea sdLog;
    private javax.swing.JList serviceList;
    private javax.swing.JList typeList;
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
