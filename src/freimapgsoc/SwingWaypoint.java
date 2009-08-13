package freimapgsoc;
import javax.swing.JComponent;

import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.Waypoint;

public class SwingWaypoint extends Waypoint{
	private JComponent component;

	public SwingWaypoint(JComponent component, GeoPosition gp) {
		super(gp);
		this.component = component;
	}

	public JComponent getComponent() {
		return component;
	}
}
