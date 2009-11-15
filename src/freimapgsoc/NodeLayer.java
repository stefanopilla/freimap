/* net.relet.freimap.VisorLayer.java

  This file is part of the freimap software available at freimap.berlios.de

  This software is copyright (c)2007 Thomas Hirsch <thomas hirsch gmail com>

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License with
  the Debian GNU/Linux distribution in file /doc/gpl.txt
  if not, write to the Free Software Foundation, Inc., 59 Temple Place,
  Suite 330, Boston, MA 02111-1307 USA
*/

package freimapgsoc;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.*;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.Waypoint;
import org.jdesktop.swingx.mapviewer.WaypointPainter;
import org.jdesktop.swingx.mapviewer.WaypointRenderer;

public class NodeLayer implements DataSourceListener {
  double scale;  // current scaling
  int zoom;       // zoom factor, according to OSM tiling
  int w, h;    //screen width, hight
  int cx, cy; //center of screen

  Vector<MapNode> nodes; //vector of known nodes
  Vector<Link> links; //vector of currently displayed links
  Hashtable<String, Float> availmap; //node availability in percent (0f-1f)
  Hashtable<String, NodeInfo> nodeinfo=new Hashtable<String, NodeInfo>(); //stores nodeinfo upon right click
  Hashtable<Link, LinkInfo> linkinfo=new Hashtable<Link, LinkInfo>(); //stores linkinfo upon right click

  //create a Set of waypoints
    Set<Waypoint> bluewaypoints = new HashSet<Waypoint>();
    Set<Waypoint> yellowwaypoints= new HashSet<Waypoint>();
    Set<Waypoint> whitewaypoints= new HashSet<Waypoint>();

    //crate a WaypointPainter to draw the points
    WaypointPainter painter = new WaypointPainter();
JXMapViewer mainMap;

  //FIXME the following paragraph is identical and static in VisorFrame. Use these definitions and remove the paragraph.
 
  DataSource source;
  MapNode selectedNode;
  Link selectedLink;
  double selectedNodeDistance,
         selectedLinkDistance;

  private MapNode uplink = new MapNode("0.0.0.0/0.0.0.0");

  long crtTime;

  boolean hideUnlocated = Configurator.getB(new String[]{"display", "hideUnlocated"});

  public NodeLayer(DataSource source) {
        this.source=source;
        this.source.addDataSourceListener(this);

    System.out.println("reading node list.");
        nodes=source.getNodeList();
    System.out.println("reading node availability.");
        availmap=source.getNodeAvailability(0);
    System.out.print("reading link list.");
        long now = System.currentTimeMillis();
        links = new Vector<Link>();//source.getLinks(firstUpdateTime);
    System.out.println("("+(System.currentTimeMillis()-now)+"ms)");
    drawNodes();
  }

  /* datasourcelistener */
  public void timeRangeAvailable(long from, long until) {
    //obsolete.
  }
  public void nodeListUpdate(MapNode node) {
    if (nodes!=null) { //this really should not happen
      nodes.add(node);
    }
  }

  public void addToWayPointsSets(Double lat, Double lon, String color){
      if(color.equals("Yellow")){
            yellowwaypoints.add(new Waypoint(lat,lon));
   }else if(color.equals("White")){
       whitewaypoints.add(new Waypoint(lat,lon));
   }else if(color.equals("Blue")){
       bluewaypoints.add(new Waypoint(lat,lon));
   }
  }

  /**
   * returns the DataSource of this layer. If the layer is just decorative, returns null.
   * 
   * @return null or DataSource
   */
  public DataSource getSource() {
    return source;
  }


      public void drawBlueWaypoint(){
      painter.setWaypoints(bluewaypoints);
        painter.setRenderer(new WaypointRenderer() {
        public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {
            g.setColor(Color.red);
            g.drawLine(-5,-5,+5,+5);
            g.drawLine(-5,+5,+5,-5);
            return true;
    }
});
mainMap.setOverlayPainter(painter);
      }

      public void drawYellowWaypoint(){
      painter.setWaypoints(yellowwaypoints);
        painter.setRenderer(new WaypointRenderer() {
        public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {
            g.setColor(Color.yellow);
            g.drawLine(-5,-5,+5,+5);
            g.drawLine(-5,+5,+5,-5);
            return true;
    }
});
mainMap.setOverlayPainter(painter);
      }

      public void drawWhiteWaypoint(){
      painter.setWaypoints(whitewaypoints);
        painter.setRenderer(new WaypointRenderer() {
        public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {
            g.setColor(Color.white);
            g.drawLine(-5,-5,+5,+5);
            g.drawLine(-5,+5,+5,-5);
            return true;
    }
});
mainMap.setOverlayPainter(painter);
      }

      /*
      public void drawLinks(){
           if (visible == 0) return;

        //draw links
        if (scale < 0) {
            System.err.println("DEBUG scale < 0");
            scale = 1;
    }

    Stroke linkStroke = new BasicStroke((float)(Math.min(2,0.00005 * scale)), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    Stroke linkStrokeThick = new BasicStroke((float)(Math.min(4,0.00010 * scale)), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    Stroke cableStroke = new BasicStroke((float)(Math.min(10,0.00015 * scale)), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    Stroke selectedStroke = new BasicStroke((float)(Math.min(20,0.00030 * scale)), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    //draw selected link extra thick
    if ((selectedLink != null) && ((selectedLink.from.lat == selectedLink.from.DEFAULT_LAT) || (selectedLink.to.lat == selectedLink.to.DEFAULT_LAT))) {
      selectedLink = null;
    } 
    if ((selectedLink != null) && matchFilter(selectedLink.from) && matchFilter(selectedLink.to)) {
      g.setStroke(selectedStroke);
      if (selectedLink.to.equals(uplink)) {
        double nsize = Math.min(25,Math.round(0.0015 * scale));
        g.drawOval((int)(converter.lonToViewX(selectedLink.from.lon)-nsize/2), (int)(converter.latToViewY(selectedLink.from.lat)-nsize/2), (int)(nsize), (int)(nsize));
      } else {
        g.drawLine(converter.lonToViewX(selectedLink.from.lon),
        	   converter.latToViewY(selectedLink.from.lat),
        	   converter.lonToViewX(selectedLink.to.lon), 
        	   converter.latToViewY(selectedLink.to.lat));
      }
    }

    //draw other links 
    g.setStroke(linkStroke);
    if ((links != null) && (links.size()>0)) {
      for(int i = 0; i < links.size(); i++) {
        Link link = links.elementAt(i);
        if (!(matchFilter(link.from)&&matchFilter(link.to))) continue;

        boolean isneighbourlink = (link.from.equals(selectedNode)||link.to.equals(selectedNode));
        if (link.to.equals(uplink)) {
          g.setColor(activeblue);
          g.setStroke(cableStroke);
          double nsize = Math.min(25,Math.round(0.0015 * scale));
          g.drawOval((int)(converter.lonToViewX(link.from.lon)-nsize/2), (int)(converter.latToViewY(link.from.lat)-nsize/2), (int)(nsize), (int)(nsize));
          g.setStroke(linkStroke);
        } else if (link.packets>0) { 
          if ((link.from.lat != link.from.DEFAULT_LAT) && (link.to.lat != link.to.DEFAULT_LAT)) {//ignore links to truly unlocated nodes (at default position)
              float value=0.000005f * (float)Math.log(link.packets);
              linkStroke = new BasicStroke((float)Math.min(15,value * scale), BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
              g.setStroke(linkStroke);
              double ratiotcp   = (double)link.tcp / (double)link.packets;
              double ratioudp   = (double)link.udp / (double)link.packets + ratiotcp;
              double ratioicmp  = (double)link.icmp / (double)link.packets + ratioudp;
              double ratioother = (double)link.other / (double)link.packets + ratioicmp;
              g.setColor(transred);
              drawLineSeg(g, link.from.lon, link.from.lat, link.to.lon, link.to.lat, 0.0, ratiotcp);
              g.setColor(transyellow);
              drawLineSeg(g, link.from.lon, link.from.lat, link.to.lon, link.to.lat, ratiotcp, ratioudp);
              g.setColor(transgreen);
              drawLineSeg(g, link.from.lon, link.from.lat, link.to.lon, link.to.lat, ratioudp, ratioicmp);
              g.setColor(transwhite);
              drawLineSeg(g, link.from.lon, link.from.lat, link.to.lon, link.to.lat, ratioicmp, 1.0f);
          }
        } else {
          if ((link.from.lat != link.from.DEFAULT_LAT) && (link.to.lat != link.to.DEFAULT_LAT)) {//ignore links to truly unlocated nodes (at default position)
            float green = 1;
            if (link.HNA || (link.etx < 0)) {
              g.setColor(activeblue);
            } else if (link.etx<1) {
              g.setColor(activewhite);
            } else {
              green=1/link.etx;
              g.setColor(new Color(1-green, green, 0.5f, currentalpha/255.0f));
            }
            g.setStroke(linkStroke);
            if (isneighbourlink) g.setStroke(linkStrokeThick);
            g.drawLine(converter.lonToViewX(link.from.lon), converter.latToViewY(link.from.lat), converter.lonToViewX(link.to.lon), converter.latToViewY(link.to.lat));
          }
        }
        if (link.to.unlocated && (link.from.lat != link.from.DEFAULT_LAT)) {
          double netx = (link.etx<1)?0d:1d/link.etx;
          link.to.lonsum+=link.from.lon*netx;
          link.to.latsum+=link.from.lat*netx;
          link.to.nc+= netx;
        }
        if (link.from.unlocated && (link.to.lat != link.to.DEFAULT_LAT)) {
          double netx = (link.etx<1)?0d:1d/link.etx;
          link.from.lonsum+=link.to.lon * netx;
          link.from.latsum+=link.to.lat * netx;
          link.from.nc+= netx;
        }
      }
    }
    
      
    }
      */

      public void drawNodes(){
      

        drawAllNode();
      }
            
      

      /*
      double nsize = Math.max(1,Math.min(8,Math.round(0.0003 * scale)));
      if (node.unlocated) nsize = Math.max(1,Math.min(4,Math.round(0.0001 * scale)));
      double nx = converter.lonToViewX(node.lon) - nsize/2,
             ny = converter.latToViewY(node.lat) - nsize/2;

      g.fillOval((int)nx, (int)ny, (int)nsize, (int)nsize);
      if (node.unlocated) {
        if (node.nc > 1) {
          node.lon=node.lonsum / node.nc;
          node.lat=node.latsum / node.nc;
        } else if (node.nc == 1) {
          node.lon=node.lonsum + 0.00003;
          node.lat=node.latsum + 0.00003;
        }
       
    }
  }
  */

      public void drawAllNode(){
          drawWhiteWaypoint();
          drawBlueWaypoint();
          drawYellowWaypoint();
      }

/*
      public void otheMethods(){

    //draw highlight
    if ((selectedNode != null)||(selectedLink != null)) {
    	g.setStroke(new BasicStroke((float)1f));

        double nx=0, ny=0;
        boolean showNodeInfo = true;

        //draw selected node
        if ((selectedNode != null) && matchFilter(selectedNode)) {
    	  double nsize = Math.min(15,Math.round(0.0006 * scale));
    	  nx = converter.lonToViewX(selectedNode.lon);
          ny = converter.latToViewY(selectedNode.lat);
    	  g.draw(new Ellipse2D.Double(nx - nsize/2, ny - nsize/2, nsize, nsize));
        } 
        
        if ((selectedLink!=null) && (selectedLinkDistance < selectedNodeDistance)) {
          if (selectedLink.to.equals(uplink)) {
            nx = converter.lonToViewX(selectedLink.from.lon);
            ny = converter.latToViewY(selectedLink.from.lat);
          } else {
            nx = converter.lonToViewX((selectedLink.from.lon + selectedLink.to.lon)/2);
            ny = converter.latToViewY((selectedLink.from.lat + selectedLink.to.lat)/2);
            int oof = 8; //an obscure offscreen compensation factor
            int ooc = 0;
            while ((ooc<10)&&((nx<0)||(ny<0)||(nx>w)||(ny>h))) { //do not draw boxes offscreen too easily
              nx = converter.lonToViewX((selectedLink.from.lon * (oof-1) + selectedLink.to.lon)/oof);
              ny = converter.latToViewY((selectedLink.from.lat * (oof-1) + selectedLink.to.lat)/oof);
              oof *= 8;
              ooc++;
            }
          }
          showNodeInfo = false; //show linkInfo instead
    	}

        double boxw;

        String label;
        Vector<String> infos= new Vector<String>();
        if (showNodeInfo) {
          label = "Node: "+selectedNode.fqid;
          if (!selectedNode.fqid.equals(selectedNode.id)) infos.add("IP address: "+selectedNode.id);
        	boxw = Math.max(180, g.getFontMetrics().stringWidth(label)+20);

          if (availmap!=null) {
            Float favail = availmap.get(selectedNode.id);
	          String savail=(favail==null)?"N/A":Math.round(favail.floatValue()*100)+"%";
	          infos.add ("Availability: "+savail);
          }

          Iterator<String> atts=selectedNode.attributes.keySet().iterator();
          while (atts.hasNext()) {
            String key = atts.next();
            infos.add(key+": "+selectedNode.attributes.get(key));
          }

	    NodeInfo info = nodeinfo.get(selectedNode.id);
      if (info!=null) {
	      if (info.status == info.STATUS_AVAILABLE) {
	        infos.add("min. links: " + info.minLinks );
	        infos.add("max. links: " + info.maxLinks );

	        if (info.linkCountChart != null) {
		      info.linkCountChart.draw(g, new Rectangle2D.Float(20, 180, 500, 300));
	      }
            } else if (info.status == info.STATUS_FETCHING) {
              infos.add("retrieving information");
	    }
	  } else {
	    infos.add("+ right click for more +");
	  }

        } else {
          boxw = g.getFontMetrics().stringWidth("Link: 999.999.999.999 -> 999.999.999.999/999.999.999.999");

          label = "Link: "+selectedLink.toString();

          if (selectedLink.packets > 0) {
            infos.add("packets: "+selectedLink.packets);
            infos.add("bytes  : "+selectedLink.bytes);
            infos.add("icmp   : "+selectedLink.icmp);
            infos.add("tcp    : "+selectedLink.tcp);
            infos.add("udp    : "+selectedLink.udp);
            infos.add("other  : "+selectedLink.other);
          }

          LinkInfo info = linkinfo.get(selectedLink);
          
          if (info != null) {
            if (info.status==info.STATUS_AVAILABLE) { 
	      if (info.linkChart != null) {
		  info.linkChart.draw(g, new Rectangle2D.Float(20, 180, 500, 300));
	      }
            } else if (info.status == info.STATUS_FETCHING) {
              infos.add("retrieving information");
            }
	  } else {
	    infos.add("+ right click for more +");
	  }

        }

        // Put box at fixed location.
        double boxx = w - 10 - boxw / 2;
        double boxy = 80;

        double labelh = g.getFontMetrics().getHeight(),
        infoh = g.getFontMetrics().getHeight(),
        boxh = labelh + infoh * infos.size() + 10;

	    // Connect with the bottom line of the box.
    		g.draw(new Line2D.Double(nx, ny, boxx, boxy+boxh));

        Shape box = new RoundRectangle2D.Double(boxx-boxw/2, boxy, boxw, boxh, 10, 10);
    	  g.fill(box); 
    	  g.draw(box);
	      g.setColor(showNodeInfo?activegreen:activeblue); 
      	g.drawString(label, (int)(boxx - boxw/2 + 10), (int)(boxy + labelh));
	      for (int i=0; i<infos.size(); i++) {
		      g.drawString(infos.elementAt(i), (int)(boxx - boxw/2 + 10), (int)(boxy + labelh + infoh*i + 15));
	      }
    }


  }
*/

 

  public MapNode getSelectedNode() {
    return selectedNode;
  }

  public double sqr(double x) { 
    return x*x;
  }
  public double dist (double x1, double x2, double y1, double y2) {
    return Math.sqrt(sqr(x1-x2)+sqr(y1-y2));
  }

  public Link getClosestLink(double lon, double lat) {
    if (links==null) return null;
    double dmin=Double.POSITIVE_INFINITY;
    Link closest=null, link;
    boolean within;
    for (int i=0; i<links.size(); i++) {
      link=links.elementAt(i);
      within=true;
      if (link.source.lon < link.dest.lon) {
        if ((lon < link.source.lon) || (lon > link.dest.lon)) within = false;
      } else {
        if ((lon > link.source.lon) || (lon < link.dest.lon)) within = false;
      }
      if (link.source.lat < link.dest.lat) {
        if ((lat < link.source.lat) || (lat > link.dest.lat)) within = false;
      } else {
        if ((lat > link.source.lat) || (lat < link.dest.lat)) within = false;
      }
      if (within) {
        if (dist(lat, link.source.lat, lon, link.source.lon) > dist(lat, link.dest.lat, lon, link.dest.lon)) continue;
           //we will then select the other link direction.
        double x1 = link.source.lat,
               x2 = link.dest.lat,
               y1 = link.source.lon,
               y2 = link.dest.lon;
        double d = Math.abs((x2-x1)*(y1-lon) - (x1-lat)*(y2-y1)) / Math.sqrt(sqr(x2-x1)+sqr(y2-y1));
        if (d<dmin) {
	  dmin=d;
	  closest=link;
        }
      }
    }
    selectedLinkDistance=dmin;
    return closest;
  }

  public MapNode getClosestNode(double lon, double lat) {
    double dmin=Double.POSITIVE_INFINITY;
    MapNode closest=null, node;
    for (int i=0; i<nodes.size(); i++) {
      node=nodes.elementAt(i);
      double d = Math.abs(node.lon - lon) + Math.abs(node.lat - lat); //no need to sqrt here
      if (d<dmin) {
	      dmin=d;
	      closest=node;
      }
    }
    selectedNodeDistance = (closest==null)?dmin:dist(closest.lon, lon, closest.lat, lat); //recalculate exact distance
    return closest;
  }


  /**
   * Indiciates whether this VisorLayer instance is transparent. 
   * 
   * @return true
   */

 


  /**
   * Attempts to set transparency to this VisorLayer.
   */



 

  

 /**
  * Sets the width and height of the section the layer is
  * showing.
  * 
  * <p>This method must be called whenever the size changes
  * otherwise calculations will get incorrect and drawing problems
  * may occur.</p>
  * 
  * @param w
  * @param h
  */


 /**
  * Sets the <code>VisorLayer</code>s zoom.
  * 
  * <p>This method must be called whenever the zoom changes
  * otherwise calculations will get incorrect and drawing problems
  * may occur.</p>
  * 
  * @param zoom
  */


 /**
  * Sets the current point in time to be displayed
  * 
  * @param crtTime, an unix time stamp
  * @return true, if the layer has to be repainted
  */
 public boolean setCurrentTime(long crtTime) {
   long adjusted=source.getClosestUpdateTime(crtTime);
   //FIXME: if the interval between crtTime and the closest Display time is too high, display nothing.
   if (adjusted != this.crtTime) {
     links = source.getLinks(this.crtTime);
     this.crtTime = adjusted;
     return true;
   }
   return false;
 }

 public void mouseMoved(double lat, double lon) {
   if ((lon==0) && (lat==0)) {
     selectedNode = null;
     selectedLink = null;
   } else {
     selectedNode = getClosestNode(lon, lat); 
     selectedLink = getClosestLink(lon, lat);
     if (selectedNodeDistance * mainMap.getZoom() < 10)
         selectedLinkDistance=Double.POSITIVE_INFINITY; //when close to a node, select a node
   }
 }

 public void mouseClicked(double lat, double lon, int button) {
   if (button==MouseEvent.BUTTON3) {
     if (selectedNodeDistance < selectedLinkDistance) {
       if (selectedNode != null) {
         NodeInfo info=new NodeInfo();
         source.getLinkCountProfile(selectedNode, info);
         nodeinfo.put(selectedNode.name, info);
       }
     } else if (selectedLink != null) {
       LinkInfo info=new LinkInfo();
       source.getLinkProfile(selectedLink, info);
       linkinfo.put(selectedLink, info);
     } 
   } 
 }


 public void hideUnlocatedNodes(boolean hide) {
   hideUnlocated = hide;
 }


}
