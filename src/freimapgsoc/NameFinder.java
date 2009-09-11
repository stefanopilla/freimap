package freimapgsoc;

import java.util.Timer;
import java.util.TimerTask;


public class NameFinder extends TimerTask {
  private double lat, lon;
  private int zoom;

  private Timer timer = new Timer();

  private String location = "http://relet.net/trac/freimap";

  public void setLocation(double lat, double lon, int zoom) {
    this.lat = lat;
    this.lon = lon;
    this.zoom = zoom;
    timer.cancel();
    timer.schedule(this, 1000);
  }

  public String getLocation() {
    return location;
  }

  public void run() {
    //String url = "http://www.frankieandshadow.com/osm/search.xml?find=places+near+"+lat+","+lon;
    
  }

  public boolean cancel() {
    return false;
  }

  public long scheduledExecutionTime() {
    return 200;
  }
}
