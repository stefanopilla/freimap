/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package freimapgsoc;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

/**
 *
 * @author stefanopilla
 */
public class Layer {

    public Layer() {
        System.out.println("Creating one unique Layer with no id");
        createLayer();
    }

    public Layer(HashMap<Vector<MapNode>, Vector<Link>> config, DataSource datasource) {
        this.currentDS = datasource;
        this.data = config;
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

    public void createLayer(HashMap<Vector<MapNode>, Vector<Link>> data, DataSource ds, int id) {
        try {
            //Query to the MySql Server to store the data
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
    public int id;
    public Vector<MapNode> nodes = new Vector<MapNode>();
    public Vector<Link> links = new Vector<Link>();
    public HashMap<Object, Vector<Link>> dataOlsrd = null;
    public HashMap<Vector<MapNode>, Vector<Link>> data = null;
    public DataSource currentDS = null;
    public HashMap<Integer, HashMap<Vector<MapNode>, Vector<Link>>> layers = null;

}



