/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ServiceDiscovery.java
 *
 * Created on 19-giu-2009, 21.30.56
 */
package ServiceDiscovery;

import freimapgsoc.InfoPopUp;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
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

/**
 *
 * @author stefano
 */
public class ServiceDiscovery extends javax.swing.JFrame implements ServiceListener, ServiceTypeListener, ListSelectionListener {

    @SuppressWarnings("static-access")
    public ServiceDiscovery(String ip) {
        initComponents();
        InetAddress address;
        try {
            address = InetAddress.getByName(ip);
            JmDNS.create(address);
            jmdns.addServiceTypeListener(this);
        } catch (IOException ex) {
            Logger.getLogger(ServiceDiscovery.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /** Creates new form ServiceDiscovery */
    public ServiceDiscovery() {
        initComponents();
    }

    public ServiceDiscovery(JmDNS jmdns) throws IOException {
        initComponents();
        this.jmdns = jmdns;
        jmdns.addServiceTypeListener(this);
        insertProt();
        insertApp();
        domainLabel.setText(jmdns.getHostName());
        interfaceLabel.setText(jmdns.getInterface().toString());
    }

    public void createCorrispondenceArray() {
        applpro = new String[100];
        int k = 0;

        //Create Vector String that conteins i=proto name i+1=application name
        for (int s = 0; s < application.length; s++) {
            applpro[k] = new String(prot[s]);
            applpro[k + 1] = new String(application[s]);

            System.out.println("\napplpro[" + k + "]  PROT= " + applpro[k]);
            System.out.println("applpro [" + (k + 1) + "] APPL= " + applpro[k + 1]);
            k += 2;
        }
    }
    //This Method insert Protocol in typesList ComboBox

    public void insertProt() {
        // register some well known types
        prot = new String[]{
                    //"All protocols",
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
                    "_sip._udp.local.",};

        for (int i = 0; i < prot.length - 1; i++) {
            typesList.addItem(prot[i]);
            //  System.out.println("Protocol:"+ prot[i]);
            jmdns.registerServiceType(prot[i]);
            jmdns.addServiceListener(prot[i], this);
        }
    }

    //This Method insert name in nameList ComboBox
    public void insertApp() {
        // register some well known Services
        application = new String[]{
                    "Hypertext Transfer Protocol (HTTP)", //_http._tcp
                    "File Transfer Protocol (FTP)", // _ftp._tcp
                    "Secure File Transfer Protocol (SFTP)", // _sftp._tcp
                    "Trivial File Transfer Protocol (TFTP)",// _tftp._udp
                    "Secure Shell (SSH)", //_ssh._tcp
                    "Samba Protocol (SMB)", //_smb._tcp.local.
                    "Line Printer Daemon (LPD/LPR)", //_printer._tcp
                    "Airport Base Station",// _airport._tcp
                    "AppleTalk Filing Protocol (AFP)", //_afpovertcp._tcp
                    "Network File System (NFS)", //_nfs._tcp
                    "WebDAV File System (WEBDAV)", // _webdav._tcp
                    "iChat Instant Messaging Protocol",// _presence._tcp
                    "Remote AppleEvents", //_eppc._tcp
                    "Remote Login (TELNET)",// _telnet._tcp
                    "Remote Audio Output Protocol (RAOP)",// _raop._tcp
                    "Internet Printing Protocol (IPP)",// _ipp._tcp
                    " PDL Data Stream (Port 9100)",// _pdl-datastream._tcp
                    "Remote I/O USB Printer Protocol",// _riousbprint._tcp
                    "Digital Audio Access Protocol (DAAP)", //_daap._tcp
                    "Distributed Compiler (XCODE)", //_distcc._tcp.local.
                    "Xserver RAID",// _xserveraid._tcp
                    "Apple Remote Desktop (ARD)", //_net-assistant._tcp
                    "Workgroup Manager", //_workstation._tcp.local.
                    "H.323 Telefonie", // _h323._tcp.local.
                    "Session Initiation Protocol (SIP)",//_sip._udp.local.
                };
        for (int i = 0; i < application.length; i++) {
            nameList.addItem(application[i]);
        }
        createCorrispondenceArray();

    }

    //This Method search App name in String[] applpro array from protocol name
    public String searchApp(String prot) {
        for (int i = 0; i < applpro.length; i += 2) {
            if (applpro[i].equals(prot)) {
                System.out.println("App: " + applpro[i + 1]);
                return applpro[i + 1];
            }
        }
        return "notfound";
    }

    //This Method search Protocol name in String[] applpro array from application name
    public String searchProt(String app) {
        for (int i = 1; i < applpro.length; i += 2) {
            if (applpro[i].equals(app)) {
                System.out.println("Prot: " + applpro[i]);
                return applpro[i - 1];
            }
        }
        return "notfound";
    }

    //This Method check if the String prot is in String[] applpro array and RETURN app NAME!
    //Userd Bye combobox change value!
    public void setCorrispondenceProApp(String prot) {
        String app = searchApp(prot);
        if (app.equals("notfound")) {
            jTextAreaInfo.setText("Service Not Found! Error!");
        } else {
            nameList.setSelectedItem(app);
        }
    }

    //This Method check if the String app is in String[] applpro array and change value in TypeList ComboBox!
    //Used by ComboBox change value!
    public void setCorrispondenceAppPro(String app) {
        String proto = searchProt(app);
        if (proto.equals("notfound")) {
            jTextAreaInfo.setText("Protocol Type Not Found! Error!");
        } else {
            typesList.setSelectedItem(proto);
        }
    }

//this is the Listener for types
    public void serviceTypeAdded(ServiceEvent event) {
        final String type = event.getType();
        System.out.println("TYPE ADDED: " + type);
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                if (!isPresent(type)) {
                    typesList.addItem(type);
                }
            }
        });
    }

    //this is the Listener for services
    public void serviceAdded(ServiceEvent event) {
        System.out.println("New Service Added!");
        final String name = event.getName();
        System.out.println("SERVICE ADDED: " + name);
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                insertSorted(services, name);
            }
        });
    }
    
    void insertSorted(DefaultListModel model, String value) {
        for (int i = 0, n = model.getSize(); i < n; i++) {
            if (value.compareToIgnoreCase((String) model.elementAt(i)) < 0) {
                model.insertElementAt(value, i);

            }
        }
        model.addElement(value);
    }



    //************** NOT USED****************//
    public boolean isPresent(String type) {
        boolean p = false;
        for (int i = 0; i < typesList.getItemCount(); i++) {
            if (typesList.getItemAt(i).toString().equals(type)) {
                p = true;
            }
        }
        return p;
    }

    public void serviceRemoved(ServiceEvent event) {
        final String name = event.getName();
        System.out.println("REMOVE: " + name);
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                services.removeElement(name);
            }
        });
    }

    //************** NOT USED****************//
    /**
     * List selection changed.
     */
    public void valueChanged(ListSelectionEvent e) {
        //System.out.println(e);
        type = (String) typesList.getSelectedItem().toString();
        if (!e.getValueIsAdjusting()) {
            //I don't need this control but I leave this for future implementation or modification!
            /**  if (e.getSource() == typesList) {
            jmdns.removeServiceListener(type, this);
            servicesList.removeAll();
            jTextAreaInfo.setText("");
            if (type != null) {
            jmdns.addServiceListener(type, this);
            }
            } else */
            if (e.getSource() == servicesList) {
                String name = (String) servicesList.getSelectedValue();
                if (name == null) {
                    jTextAreaInfo.setText("No Value Selected");
                } else {
                    System.out.println(this + " valueChanged() type: " + type + " name: " + name);
                    System.out.flush();
                    ServiceInfo service = jmdns.getServiceInfo(type, name);
                    if (service == null) {
                        jTextAreaInfo.setText("service not found or removed!");
                    } else {

                        StringBuffer buf = new StringBuffer();
                        buf.append(name);
                        buf.append('.');
                        buf.append(type);
                        buf.append('\n');
                        buf.append("Server: " + service.getServer());
                        buf.append(':');
                        buf.append(service.getPort());
                        buf.append('\n');
                        buf.append("Address: " + service.getAddress());
                        buf.append(':');
                        buf.append(service.getPort());
                        buf.append('\n');
                        for (Enumeration names = service.getPropertyNames(); names.hasMoreElements();) {
                            String prop = (String) names.nextElement();
                            buf.append(prop);
                            buf.append('=');
                            buf.append(service.getPropertyString(prop));
                            buf.append('\n');
                        }
                        this.jTextAreaInfo.setText(buf.toString());
                    }
                }
            }
        }
    }

    public void searchServices(String protocol) {
        servicesList.removeAll();
        ServiceInfo[] list = jmdns.list(protocol);

        for (int i = 0; i < list.length; i++) {
            System.out.println("ServiceInfo: " + list[i]);
        }
        try {
            String state = null;
            if (list.length == 0) {
                jTextAreaInfo.setText("No Services for this protocol!");
            } else {
                System.out.println("Requested value for" + protocol + "! Found " + list.length + " services");
                System.out.flush();
                for (int i = 0; i < list.length; i++) {
                    services.add(i, list[i].getName());
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public String toString() {
        return "RVBROWSER";
    }

    public void removeAll() {
        servicesList.removeSelectionInterval(0, servicesList.getMaxSelectionIndex());
    }

    //SEARCH ALL SERVICES
    /**
    public void searchAllServices()  {
    servicesList.removeAll();
    HashMap<ServiceInfo[], String> foundServices= new HashMap<ServiceInfo[], String>();
    for(int i =0; i<typesList.getItemCount(); i++){
    System.out.println("TypesList Element: "+ typesList.getItemAt(i).toString());
    System.out.println("ServiceInfo[]: " + jmdns.list(typesList.getItemAt(i).toString()));
    String prot=typesList.getItemAt(i).toString();
    foundServices.put(jmdns.list(prot),prot);
    }
    for(int i=0; i<foundServices.size(); i++){
    System.out.println(i+":  "+foundServices.get(i));
    }   
    try{
    String state=null;
    if (foundServices.isEmpty()) {
    jTextAreaInfo.setText("No Services for this protocol!");
    } else {
    services.removeAllElements();
    System.out.println("Requested value for all protocols! Found " + foundServices.size() + " services");
    System.out.flush();
    for(int j=0; j<foundServices.size(); j++){
    services.add(j, foundServices.get(j));
    }
    }
    }catch(Exception e){
    System.out.println(e.getMessage());
    }
    }*/
    
     public void serviceResolved(ServiceEvent e) {
        System.out.println("Service resolved: " + e.getInfo());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        servicesList = new JList(services);
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextAreaInfo = new javax.swing.JTextArea();
        typesList = new javax.swing.JComboBox();
        nameList = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        unregServiceButton = new javax.swing.JButton();
        addProtName = new javax.swing.JButton();
        removeAll = new javax.swing.JButton();
        serviceAdded = new javax.swing.JLabel();
        domainLabel = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        interfaceLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(freimapgsoc.FreimapGSoCApp.class).getContext().getResourceMap(ServiceDiscovery.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setAlwaysOnTop(true);
        setIconImages(null);
        setName("Form"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        servicesList.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        servicesList.setFont(resourceMap.getFont("nameList.font")); // NOI18N
        servicesList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        servicesList.setToolTipText(resourceMap.getString("servicesList.toolTipText")); // NOI18N
        servicesList.setName("servicesList"); // NOI18N
        servicesList.addListSelectionListener(this);
        jScrollPane1.setViewportView(servicesList);

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        jTextAreaInfo.setColumns(20);
        jTextAreaInfo.setEditable(false);
        jTextAreaInfo.setFont(resourceMap.getFont("nameList.font")); // NOI18N
        jTextAreaInfo.setRows(5);
        jTextAreaInfo.setToolTipText(resourceMap.getString("jTextAreaInfo.toolTipText")); // NOI18N
        jTextAreaInfo.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jTextAreaInfo.setFocusable(false);
        jTextAreaInfo.setName("jTextAreaInfo"); // NOI18N
        jScrollPane2.setViewportView(jTextAreaInfo);

        typesList.setFont(resourceMap.getFont("nameList.font")); // NOI18N
        typesList.setName("typesList"); // NOI18N
        typesList.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    // selectedItem is selectioned Item
                    Object selectedItem = e.getItem();
                }
            }
        });

        nameList.setFont(resourceMap.getFont("nameList.font")); // NOI18N
        nameList.setName("nameList"); // NOI18N
        nameList.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                nameListItemStateChanged(evt);
            }
        });

        jLabel2.setFont(resourceMap.getFont("jLabel1.font")); // NOI18N
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jLabel1.setFont(resourceMap.getFont("jLabel1.font")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        unregServiceButton.setFont(resourceMap.getFont("removeAll.font")); // NOI18N
        unregServiceButton.setText(resourceMap.getString("unregServiceButton.text")); // NOI18N
        unregServiceButton.setName("unregServiceButton"); // NOI18N
        unregServiceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                unregServiceButtonActionPerformed(evt);
            }
        });

        addProtName.setFont(resourceMap.getFont("removeAll.font")); // NOI18N
        addProtName.setText(resourceMap.getString("addProtName.text")); // NOI18N
        addProtName.setName("addProtName"); // NOI18N

        removeAll.setFont(resourceMap.getFont("removeAll.font")); // NOI18N
        removeAll.setText(resourceMap.getString("removeAll.text")); // NOI18N
        removeAll.setName("removeAll"); // NOI18N

        serviceAdded.setFont(resourceMap.getFont("serviceAdded.font")); // NOI18N
        serviceAdded.setForeground(resourceMap.getColor("serviceAdded.foreground")); // NOI18N
        serviceAdded.setText(resourceMap.getString("serviceAdded.text")); // NOI18N
        serviceAdded.setName("serviceAdded"); // NOI18N

        domainLabel.setFont(resourceMap.getFont("jLabel4.font")); // NOI18N
        domainLabel.setText(resourceMap.getString("domainLabel.text")); // NOI18N
        domainLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        domainLabel.setName("domainLabel"); // NOI18N

        jLabel3.setBackground(resourceMap.getColor("jLabel4.background")); // NOI18N
        jLabel3.setFont(new java.awt.Font("DejaVu Sans", 1, 10)); // NOI18N
        jLabel3.setLabelFor(domainLabel);
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabel3.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel3.setName("jLabel3"); // NOI18N
        jLabel3.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

        jLabel4.setBackground(resourceMap.getColor("jLabel4.background")); // NOI18N
        jLabel4.setFont(resourceMap.getFont("jLabel4.font")); // NOI18N
        jLabel4.setLabelFor(interfaceLabel);
        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabel4.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel4.setName("jLabel4"); // NOI18N
        jLabel4.setRequestFocusEnabled(false);
        jLabel4.setVerifyInputWhenFocusTarget(false);
        jLabel4.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

        interfaceLabel.setFont(resourceMap.getFont("jLabel4.font")); // NOI18N
        interfaceLabel.setText(resourceMap.getString("interfaceLabel.text")); // NOI18N
        interfaceLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        interfaceLabel.setName("interfaceLabel"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(serviceAdded, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(typesList, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(removeAll)
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(nameList, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                    .addComponent(jLabel2))
                                .addGap(49, 49, 49))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(domainLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(interfaceLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 179, Short.MAX_VALUE)))
                        .addContainerGap())
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(unregServiceButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(addProtName, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(typesList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nameList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 178, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(serviceAdded)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addProtName, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(unregServiceButton, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(24, 24, 24)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(domainLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 15, Short.MAX_VALUE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 15, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(5, 5, 5)
                                .addComponent(interfaceLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                        .addGap(32, 32, 32))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(removeAll, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void nameListItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_nameListItemStateChanged
        services.removeAllElements();
        try {
            if (nameList.getSelectedItem() == null) {
                new InfoPopUp("Select an Application!").setVisible(true);
            }//else if(typesList.getSelectedItem().equals("All protocols")){
            //   searchAllServices();
            //}
            else {
                setCorrispondenceAppPro(nameList.getSelectedItem().toString());
                searchServices(typesList.getSelectedItem().toString());
                System.out.println("Selected: " + typesList.getSelectedItem().toString());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }//GEN-LAST:event_nameListItemStateChanged

        private void unregServiceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unregServiceButtonActionPerformed
            String type = typesList.getSelectedItem().toString();
            if (!type.equals(null)) {
                new InfoPopUp("Select a Type please!");
            }//else{
                //unregister Service
            //}
        }//GEN-LAST:event_unregServiceButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                InetAddress add = null;
                try {
                    add = InetAddress.getByName("10.0.1.29"); //logically this is to try...
                } catch (UnknownHostException ex) {
                    Logger.getLogger(ServiceDiscovery.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    new ServiceDiscovery(JmDNS.create(add)).setVisible(true);
                } catch (IOException ex) {
                    Logger.getLogger(ServiceDiscovery.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addProtName;
    private javax.swing.JLabel domainLabel;
    private javax.swing.JLabel interfaceLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextAreaInfo;
    private javax.swing.JComboBox nameList;
    private javax.swing.JButton removeAll;
    private javax.swing.JLabel serviceAdded;
    private javax.swing.JList servicesList;
    private javax.swing.JComboBox typesList;
    private javax.swing.JButton unregServiceButton;
    // End of variables declaration//GEN-END:variables
    private JmDNS jmdns;
    private String type;
    private DefaultListModel services = new DefaultListModel();
    private String[] prot, application;
    private String[] applpro;

   
   }
