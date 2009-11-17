/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package freimapgsoc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

/**
 *
 * @author stefanopilla
 */
public class Layer {

    public Layer(DataSource datasource){
        this.currentDs=datasource;
    }

    public Layer(HashMap<Vector<MapNode>, Vector<Link>> datasources) {
        this.datasources = datasources;
    }

    public DataSource getCurrentDataSource() {
        return this.currentDs;
    }

    public HashMap<Double, String> getDataSources() {
        return this.sources;
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

    /*
    public void createLayer() {
        currentDs = new DataSource();
        try {
            sources.put(this.id, currentDs.getCurrentID());
            Iterator<Double> j = sources.keySet().iterator();
            while (j.hasNext()) {
                Double id = j.next();
                String source = sources.get(id);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
    }
*/

    public void initLayout() {

    }

    public double id;
    public Vector<MapNode> nodes = new Vector<MapNode>();
    public Vector<Link> links = new Vector<Link>();
    public DataSource currentDs =null;
    public HashMap<Vector<MapNode>, Vector<Link>> datasources;
    public HashMap<Double, String> sources;
}



