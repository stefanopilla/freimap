/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package freimapgsoc;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ResultSet;
import com.mysql.jdbc.Statement;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import org.openide.util.Exceptions;

/**
 *
 * @author stefanopilla
 */
public class Layer {

    public Layer() {
        System.out.println("Creating one unique Layer with no id");
        createLayer();
    }

    public Layer(Vector<MapNode> nodes, Vector<Link> links, DataSource datasource) {
        this.currentDS = datasource;
        this.nodes = nodes;
        this.links =links;
        this.id = generateNewID();
        System.out.println("Creating new Layer with id: " + id);
        createLayer(data, datasource, id);
    }
    /*
    public Layer(HashMap<Object, Vector<Link>> config, DataSource datasource) {
        this.currentDS = datasource;
        this.dataOlsrd = config;
        this.id = generateNewID();
        System.out.println("Creating new Layer with id: " + id);
        createLayer(data, datasource, id);
    }
     *
     */

    public int getCurrentLayerId() {
        return this.id;
    }

    public HashMap<Vector<MapNode>, Vector<Link>> getData() {
        return this.data;
    }

    public HashMap<Integer, HashMap<Vector<MapNode>, Vector<Link>>> getLayers() {
        return this.layers;
    }

    public DataSource getCurrentDataSource(){
        return currentDS;
    }

    /**
     * Sets the current point in time to be displayed
     *
     * @param crtTime, an unix time stamp
     * @return true, if the layer has to be repainted consequently
     */
    public boolean setCurrentTime(long crtTime) {
        return false;
    }

    public void storeDB() throws ClassNotFoundException{
        int isGateway=0;
        String gwIp="0.0.0.0";
        try {
            System.out.println("Getting connection to MySql Database " + currentDS.getDatabase() + " at " + currentDS.getHost() + " " + ":" + currentDS.getPort() + "");
            Class.forName("com.mysql.jdbc.Driver");
            c = (Connection) DriverManager.getConnection("jdbc:mysql://" + currentDS.getHost() + ":" + currentDS.getPort() + "/" + currentDS.getDatabase(), currentDS.getUsername(), currentDS.getPassword());
            if (!c.isClosed()) {
                System.out.println("Connected!");
                for(int i=0; i<nodes.size(); i++){
                    stmt= (Statement) c.createStatement();
                    if(nodes.get(i).attributes.containsValue("0.0.0.0")){
                              isGateway=0;
                          }else{
                            isGateway=1;
                            gwIp=nodes.get(i).attributes.get("GATEWAY").toString();
                          }
                    String query = "INSERT INTO `nodes` (`lon`,`lat`,`ip`,`name`,`isGateway`,`gatewayIp`,`uptime`,`interfaces`) VALUES ("+
                            nodes.get(i).lat +
                           ", " + nodes.get(i).lon +
                           "," + nodes.get(i).ip +
                           "," + nodes.get(i).name+
                           "," + isGateway +
                           "," + gwIp +
                           "," + nodes.get(i).uptime +
                           "," + nodes.get(i).inter +
                           ");";
                    rss =(ResultSet) stmt.executeQuery(query);
                }
                
                for(int i=0; i<links.size(); i++){
                    stmt= (Statement) c.createStatement();
                    String query = "INSERT INTO `links` (`clock`,`src`,`dest`,`lq`,`nlq`,`etx`) VALUES ("+
                            links.get(i).timeStamp +
                           ", " + links.get(i).source.toString() +
                           "," + links.get(i).dest.toString() +
                           "," + links.get(i).lq+
                           "," + links.get(i).nlq +
                           "," + links.get(i).etx +
                           ");";
                    rss =(ResultSet) stmt.executeQuery(query);
                }


                String query2;
                while (rss.next()) {
                    query2 = "SELECT intIp FROM Interfaces WHERE mainIp = \"" + rss.getString("ip") + "\"";
                    //System.out.println(query2);
                    Vector<String> ifaces = new Vector<String>();
                    while (rss2.next()) {
                        ifaces.add(rss2.getString("IntIp"));
                    }
                }
            }
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
            }

    public void createLayer(HashMap<Vector<MapNode>, Vector<Link>> data, DataSource ds, int id) {
        try {
            storeDB();//Query to the MySql Server to store the data
            initLayout();
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
    }

    public void createLayer() {
        try {
            //Query to the MySql Server to store the data
            initLayout();
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
    }

    public void UpdateLayer(HashMap<Vector<MapNode>, Vector<Link>> data, int id) {
        //se sono uguali non aggiornare ma semplicemente crea aggiungi un id alle info già nel db
        try {
            layers.put(id, data);
            initLayout();

        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
    }

    public Vector<MapNode> getCurrentNodes() {
        return currentDS.getNodeList();
    }

    public MapNode getNodeByName(String name) {
        try {
            for (int i = 0; i < currentDS.getNodeList().size(); i++) {
                if (currentDS.getNodeList().elementAt(i).name.equals(name)) {
                    return currentDS.getNodeList().elementAt(i);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;

    }

    public Vector<Link> getCurrentLinks() {
        return currentDS.getLinks();
    }

    public void initLayout() {
        new MainLayer(this).setVisible(true);
    }

    public int generateNewID() {
        return id++;
    }

    Connection c = null;
    ResultSet rss, rss2;
    Statement stmt, stmt2;


    public int id;
    public Vector<MapNode> nodes = new Vector<MapNode>();
    public Vector<Link> links = new Vector<Link>();
    public HashMap<Object, Vector<Link>> dataOlsrd = null;
    public HashMap<Vector<MapNode>, Vector<Link>> data = null;
    public DataSource currentDS = null;
    public HashMap<Integer, HashMap<Vector<MapNode>, Vector<Link>>> layers = null;

}



