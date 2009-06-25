/*
 * FreimapGSoCApp.java
 */

package freimapgsoc;

import java.util.HashMap;
import java.util.Iterator;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class FreimapGSoCApp extends SingleFrameApplication {

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
        show(new FreimapGSoCView(this));
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of FreimapGSoCApp
     */
    public static FreimapGSoCApp getApplication() {
        return Application.getInstance(FreimapGSoCApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
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
        DataSource source = sources.get(id);
        source.init(ds2subconfig.get(id)); //initialize datasource with configuration parameters
        System.out.println(source.getNodeList());
        //addWaypoint(source.getNodeByName(id).lat,source.getNodeByName(id).lon);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      return;
    }
        launch(FreimapGSoCApp.class, args);
    }

     public static Configurator config;
  public static HashMap<String, DataSource> sources;
}
