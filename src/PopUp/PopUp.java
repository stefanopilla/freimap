/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ServiceDiscovery.java
 *
 * Created on 19-giu-2009, 21.30.56
 */
package PopUp;

import freimapgsoc.FreiLink;
import freimapgsoc.FreiNode;
import freimapgsoc.addServices;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;
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
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jdesktop.application.Action;

/**
 *
 * @author stefano
 */
public class PopUp extends javax.swing.JFrame implements ServiceListener, ServiceTypeListener, ListSelectionListener {

    @SuppressWarnings("static-access")
    public PopUp(String ip) {
        System.out.println("Costructor PopUP(String ip)");
        initComponents();
        InetAddress address=null;
        try {
            this.jmdns=JmDNS.create(address.getByName(ip));
            jmdns.addServiceTypeListener(this);
        } catch (IOException ex) {
            Logger.getLogger(PopUp.class.getName()).log(Level.SEVERE, null, ex);
        }
        // register some well known types
        list = new String[] {
            "_services._dns-sd._udp.local.",
            "_http._tcp.local.",
                    "_ftp._tcp.local.",
                    "_sftp._tcp.local.",
                    "_tftp._tcp.local.",
                    "_ssh._tcp.local.",
                    "_smb._tcp.local.",
                    "_printer._tcp.local.",
                    "_airport._tcp.local.",
                    "_afpovertcp._tcp.local.",
                    "_nfs._tcp.local.",
                    "_webdav._tcp.local.",
                    "_presence._tcp.local.",
                    "_eppc._tcp.local.",
                    "_telnet._tcp.local.",
                    "_raop._tcp.local.",
                    "_ipp._tcp.local.",
                    "_pdl-datastream._tcp.local.",
                    "_riousbprint._tcp.local.",
                    "_daap._tcp.local.",
                    "_distcc._tcp.local.",
                    "_xserveraid._tcp.local.",
                    "_net-assistant._tcp.local.",
                    "_workstation._tcp.local.",
                    "_h323._tcp.local.",
                    "_sip._udp.local."
        };

        for (int i = 0 ; i < list.length ; i++) {
            jmdns.registerServiceType(list[i]);
        }
    }

    public PopUp(FreiNode from) {
        System.out.println("Costructor PopUP(String ip)");
        initComponents();
        InetAddress address=null;
        try {
            this.jmdns=JmDNS.create(address.getByName(from.id));
            jmdns.addServiceTypeListener(this);
            infoNode(from);
        } catch (IOException ex) {
            Logger.getLogger(PopUp.class.getName()).log(Level.SEVERE, null, ex);
        }
        // register some well known types
        list = new String[] {
            "_services._dns-sd._udp.local.",
            "_http._tcp.local.",
                    "_ftp._tcp.local.",
                    "_sftp._tcp.local.",
                    "_tftp._tcp.local.",
                    "_ssh._tcp.local.",
                    "_smb._tcp.local.",
                    "_printer._tcp.local.",
                    "_airport._tcp.local.",
                    "_afpovertcp._tcp.local.",
                    "_nfs._tcp.local.",
                    "_webdav._tcp.local.",
                    "_presence._tcp.local.",
                    "_eppc._tcp.local.",
                    "_telnet._tcp.local.",
                    "_raop._tcp.local.",
                    "_ipp._tcp.local.",
                    "_pdl-datastream._tcp.local.",
                    "_riousbprint._tcp.local.",
                    "_daap._tcp.local.",
                    "_distcc._tcp.local.",
                    "_xserveraid._tcp.local.",
                    "_net-assistant._tcp.local.",
                    "_workstation._tcp.local.",
                    "_h323._tcp.local.",
                    "_sip._udp.local."
        };

        for (int i = 0 ; i < list.length ; i++) {
            jmdns.registerServiceType(list[i]);
        }
    }


    /** Creates new form ServiceDiscovery */
    public PopUp(FreiNode from, FreiNode to, FreiLink link) {
        initComponents();
        this.from=from;
        this.to=to;
        this.link=link;
        aboutNodes();

        n1Ip.setText(from.id);

    }

    public PopUp(JmDNS jmdns) throws IOException {
          System.out.println("Costructor PopUP(jmdns)");
        initComponents();
       

        this.jmdns = jmdns;
        jmdns.addServiceTypeListener(this);

        // register some well known types
        String list[] = new String[] {
            "_http._tcp.local.",
                    "_ftp._tcp.local.",
                    "_sftp._tcp.local.",
                    "_tftp._tcp.local.",
                    "_ssh._tcp.local.",
                    "_smb._tcp.local.",
                    "_printer._tcp.local.",
                    "_airport._tcp.local.",
                    "_afpovertcp._tcp.local.",
                    "_nfs._tcp.local.",
                    "_webdav._tcp.local.",
                    "_presence._tcp.local.",
                    "_eppc._tcp.local.",
                    "_telnet._tcp.local",
                    "_raop._tcp.local.",
                    "_ipp._tcp.local.",
                    "_pdl-datastream._tcp.local.",
                    "_riousbprint._tcp.local.",
                    "_daap._tcp.local",
                    "_distcc._tcp.local.",
                    "_xserveraid._tcp.local.",
                    "_net-assistant._tcp.local.",
                    "_workstation._tcp.local.",
                    "_h323._tcp.local.",
                    "_sip._udp.local."
        };

       
        for (int i = 0 ; i < list.length ; i++) {
            jmdns.registerServiceType(list[i]);
        }
    }

    public void infoNode(FreiNode node){
        n1Ip.setText(node.id);
        n1Name.setText(node.toString());
        n1LatLon.setText(node.lat+" / "+node.lon);
        n1Avail.setText(node.fqid);
        System.out.println(node.attributes.values());
        Set i=node.attributes.entrySet();
        Set keySet = node.attributes.keySet();
        for(Object key:keySet){
        Object value = node.attributes.get(key);
            attributeArea.append(value.toString());
        }

    }

    public void registerAllServices(Vector<FreiNode> list){
        HashMap<FreiNode, ServiceInfo> all= new HashMap<FreiNode, ServiceInfo>();
        //Before Discover All Services for every node.... 
        //the goal is to have an HashMap in which there are the key=the name of the node and a value = ServiceInfo vector in which there are all services
        for(int i=0;i<list.size();i++){
           // ServiceInfo ser=list[i];
           // all.put(i, serviceinfo);
        }
    }

    public void aboutNodes(){
        n1Ip.setText(from.id);
        if(from.lat == 0 || from.lat == 0){
           n1LatLon.setText(from.DEFAULT_LAT +"/"+from.DEFAULT_LON);
        }
        n1LatLon.setText(from.lat +"/"+from.lon);
        //Find a value in the attributes HAshMap and set minLinks and MaxLinks
    }

    /**
     * Add a service.
     */
    public void serviceAdded(ServiceEvent event) {
        final String name = event.getName();
        sdLog.append("Service added: " + name + "\n");
        System.out.println("ADD: " + name);
        SwingUtilities.invokeLater(new Runnable() {
        public void run() {
            insertSorted(services, name); }
        });
    }

    /**
     * Remove a service.
     */
    public void serviceRemoved(ServiceEvent event) {
        final String name = event.getName();
        sdLog.append("Service removed: " + name + "\n");
        System.out.println("REMOVE: " + name);
        SwingUtilities.invokeLater(new Runnable() {
        public void run() {
            services.removeElement(name); }
        });
    }

    /**
     * A new service type was <discovered.
     */
    public void serviceTypeAdded(ServiceEvent event) {
        final String type = event.getType();
        System.out.println("Event Type: "+event.getType());
        System.out.println("Event Name: "+event.getName());
        System.out.println("Event DNS: "+event.getDNS());
        //if(event.getName().equals("_services._dns-sd._udp.local.")){
       //    services.addElement("See All Services");
       //    services.removeElement("_services._dns-sd._udp.local.");
      // }
         sdLog.append("ServiceType added: " + type + "\n");
        System.out.println("TYPE: " + type);
        SwingUtilities.invokeLater(new Runnable() {
        public void run() {
                insertSorted(types, type); }
        });
    }


    void insertSorted(DefaultListModel model, String value) {
        for (int i = 0, n = model.getSize() ; i < n ; i++) {
            if (value.compareToIgnoreCase((String)model.elementAt(i)) < 0) {
                model.insertElementAt(value, i);
                return;
            }
        }
        model.addElement(value);
    }

    /**
     * Resolve a service.
     */
    public void serviceResolved(ServiceEvent event) {
        String name = event.getName();
        String type = event.getType();
        ServiceInfo info = event.getInfo();
       
        if (name.equals(serviceList.getSelectedValue())) {

            if (info == null) {
                this.info.setText("service not found");
            } else {
            
                StringBuffer buf = new StringBuffer();
                buf.append(name);
                buf.append('.');
                buf.append(type);
                buf.append('\n');
                buf.append(info.getServer());
                buf.append(':');
                buf.append(info.getPort());
                buf.append('\n');
                buf.append(info.getAddress());
                buf.append(':');
                buf.append(info.getPort());
                buf.append('\n');
                for (Enumeration names = info.getPropertyNames() ; names.hasMoreElements() ; ) {
                    String prop = (String)names.nextElement();
                    buf.append(prop);
                    buf.append('=');
                    buf.append(info.getPropertyString(prop));
                    buf.append('\n');
                }

                this.info.setText(buf.toString());
            }
        }
    }

    /**
     * List selection changed.
     */
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            if (e.getSource() == typeList) {
                type = (String)typeList.getSelectedValue();
                jmdns.removeServiceListener(type, this);
                services.setSize(0);
                info.setText("");
                if (type != null) {
                jmdns.addServiceListener(type, this);
                }
            } else if (e.getSource() == serviceList) {
                String name = (String)serviceList.getSelectedValue();
                if (name == null) {
                    info.setText("");
                } else {
                    System.out.println(this+" valueChanged() type:"+type+" name:"+name);
                    System.out.flush();
                    ServiceInfo service = jmdns.getServiceInfo(type, name);
                    if (service == null) {
                        info.setText("service not found");
                    } else {
                        jmdns.requestServiceInfo(type, name);
                    }
                }
            }
        }
    }

    public void deleteDuplicate(JList list){
       //IMPLEMENT ME....!
    }
    


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        nodesGroup = new javax.swing.ButtonGroup();
        flowGroup = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        n1Avail = new javax.swing.JLabel();
        n1Name = new javax.swing.JLabel();
        n1LatLon = new javax.swing.JLabel();
        n1Ip = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        attributeArea = new javax.swing.JTextArea();
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
        addServicesButton = new javax.swing.JButton();
        ReloadServicesButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(freimapgsoc.FreimapGSoCApp.class).getContext().getResourceMap(PopUp.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setAlwaysOnTop(true);
        setLocationByPlatform(true);
        setName("Form"); // NOI18N
        setResizable(false);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, resourceMap.getString("jPanel1.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, resourceMap.getFont("jPanel1.border.titleFont"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        jLabel10.setText(resourceMap.getString("jLabel10.text")); // NOI18N
        jLabel10.setName("jLabel10"); // NOI18N

        n1Avail.setText(resourceMap.getString("n1Avail.text")); // NOI18N
        n1Avail.setName("n1Avail"); // NOI18N

        n1Name.setText(resourceMap.getString("n1Name.text")); // NOI18N
        n1Name.setName("n1Name"); // NOI18N

        n1LatLon.setText(resourceMap.getString("n1LatLon.text")); // NOI18N
        n1LatLon.setName("n1LatLon"); // NOI18N

        n1Ip.setText(resourceMap.getString("n1Ip.text")); // NOI18N
        n1Ip.setName("n1Ip"); // NOI18N

        jScrollPane5.setName("jScrollPane5"); // NOI18N

        attributeArea.setColumns(20);
        attributeArea.setEditable(false);
        attributeArea.setRows(5);
        attributeArea.setEnabled(false);
        attributeArea.setName("attributeArea"); // NOI18N
        jScrollPane5.setViewportView(attributeArea);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(n1Name, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                                .addComponent(n1LatLon, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                                .addComponent(n1Avail, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE))
                            .addComponent(n1Ip, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(125, 125, 125))
                    .addComponent(jLabel10))
                .addGap(43, 43, 43)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(n1Ip))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(n1Name))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(n1LatLon)
                            .addComponent(jLabel6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel10)
                            .addComponent(n1Avail))))
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, resourceMap.getString("jPanel2.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, resourceMap.getFont("jPanel2.border.titleFont"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        serviceList.setFont(resourceMap.getFont("serviceList.font")); // NOI18N
        serviceList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        serviceList.setToolTipText(resourceMap.getString("serviceList.toolTipText")); // NOI18N
        serviceList.setName("serviceList"); // NOI18N
        serviceList.addListSelectionListener(this);
        jScrollPane1.setViewportView(serviceList);

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        typeList.setFont(resourceMap.getFont("serviceList.font")); // NOI18N
        typeList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        typeList.setToolTipText(resourceMap.getString("typeList.toolTipText")); // NOI18N
        typeList.setName("typeList"); // NOI18N
        typeList.addListSelectionListener(this);
        jScrollPane2.setViewportView(typeList);

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

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(freimapgsoc.FreimapGSoCApp.class).getContext().getActionMap(PopUp.class, this);
        addServicesButton.setAction(actionMap.get("addServices")); // NOI18N
        addServicesButton.setText(resourceMap.getString("addServicesButton.text")); // NOI18N
        addServicesButton.setActionCommand(resourceMap.getString("addServicesButton.actionCommand")); // NOI18N
        addServicesButton.setName("addServicesButton"); // NOI18N

        ReloadServicesButton.setAction(actionMap.get("reloadServices")); // NOI18N
        ReloadServicesButton.setText(resourceMap.getString("reloadServices.text")); // NOI18N
        ReloadServicesButton.setName("reloadServices"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(addServicesButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(ReloadServicesButton))
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
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addServicesButton)
                    .addComponent(ReloadServicesButton)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(25, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(17, 17, 17))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                InetAddress add = null;
                try {
                    add=InetAddress.getByName("10.0.1.29");
                    System.out.println(add);
                    //add = InetAddress.getByName("10.0.1.29"); //logically this is to try...
                } catch (UnknownHostException ex) {
                    Logger.getLogger(PopUp.class.getName()).log(Level.SEVERE, null, ex);
                }
               
                    new PopUp("10.0.1.29").setVisible(true);
              
            }
        });
    }

    @Action
    public void reloadNode1Data() {
        //Reset View of frame...all to default position!
        //When Node 1 is selected reload data in jchart, ServiceDiscovery and Labels...

    }

    @Action
    public void reloadNode2Data() {
        //Reset View of frame...all to default position!
        //When Node 2 is selected reload data in jchart, ServiceDiscovery and Labels...
    }

    @Action
    public void addServices() {
        new addServices(types,jmdns).setVisible(true);
       // jmdns.registerServiceType(types.lastElement().toString());

    }

    @Action
    public void reloadServices() {
        try{
        sdLog.append("All Services Reloaded...");
        jmdns.unregisterAllServices();
        services.removeAllElements();
        types.removeAllElements();
        for(int i=0;i<list.length;i++){
            types.addElement(list[i]);
            jmdns.registerServiceType(list[i]);
        }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton ReloadServicesButton;
    private javax.swing.JButton addServicesButton;
    private javax.swing.JTextArea attributeArea;
    private javax.swing.ButtonGroup flowGroup;
    private javax.swing.JTextArea info;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JLabel n1Avail;
    private javax.swing.JLabel n1Ip;
    private javax.swing.JLabel n1LatLon;
    private javax.swing.JLabel n1Name;
    private javax.swing.ButtonGroup nodesGroup;
    private javax.swing.JTextArea sdLog;
    private javax.swing.JList serviceList;
    private javax.swing.JList typeList;
    // End of variables declaration//GEN-END:variables
    private JmDNS jmdns;
    private String type;
    private DefaultListModel services = new DefaultListModel();
    private DefaultListModel types=new DefaultListModel();
    private FreiNode from;
    private FreiNode to;
    private FreiLink link;
    private String list[];
   
   }
