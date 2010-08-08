/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package freimapgsoc;

import java.awt.Component;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;

public class FreimapWizardPanel3 implements WizardDescriptor.ValidatingPanel {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private FreimapVisualPanel3 component;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new FreimapVisualPanel3();
        }
        return component;
    }

    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx(SampleWizardPanel1.class);
    }

    public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
        return true;
        // If it depends on some condition (form filled out...), then:
        // return someCondition();
        // and when this condition changes (last form field filled in...) then:
        // fireChangeEvent();
        // and uncomment the complicated stuff below.
    }

    public final void addChangeListener(ChangeListener l) {
    }

    public final void removeChangeListener(ChangeListener l) {
    }
    /*
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1); // or can use ChangeSupport in NB 6.0
    public final void addChangeListener(ChangeListener l) {
    synchronized (listeners) {
    listeners.add(l);
    }
    }
    public final void removeChangeListener(ChangeListener l) {
    synchronized (listeners) {
    listeners.remove(l);
    }
    }
    protected final void fireChangeEvent() {
    Iterator<ChangeListener> it;
    synchronized (listeners) {
    it = new HashSet<ChangeListener>(listeners).iterator();
    }
    ChangeEvent ev = new ChangeEvent(this);
    while (it.hasNext()) {
    it.next().stateChanged(ev);
    }
    }
     */

    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.

    public void readSettings(Object settings) {
    }

    public void storeSettings(Object settings) {
   ((WizardDescriptor) settings).putProperty("olsrdPath", ((FreimapVisualPanel3)getComponent()).getOlsrdPath());
    ((WizardDescriptor) settings).putProperty("nameServicePath", ((FreimapVisualPanel3)getComponent()).getNameServicePath());
    ((WizardDescriptor) settings).putProperty("dotDrawPort", ((FreimapVisualPanel3)getComponent()).getDotDrawPort());

    }

    public void validate() throws WizardValidationException {
       String olsrdPath=component.getOlsrdPath();
       String nameServicePath=component.getNameServicePath();
       String dotDrawPort=component.getDotDrawPort();

       if(olsrdPath.equals("") && nameServicePath.equals("") && dotDrawPort.equals("")){
           olsrdPath="/etc/olsrd.conf";
           nameServicePath="/var/run/latlon.js";
           dotDrawPort="2004";
           JOptionPane.showMessageDialog(component, "Will be used the OLSRd default value");
           throw new WizardValidationException(null, "Will be used default value for the following values:\nOLSRd Config File: /etc/olsr.conf\nNameService LatLon.js file: /var/run/latlon.js\ndotDrawPort: 2004 ", null);
       /*}else if(olsrdPath.equals("") && nameServicePath.equals("")){
           olsrdPath="/etc/olsr.conf"; 
           nameServicePath="/var/run/latlon.js";
           throw new WizardValidationException(null, "Will be used default value for the following values:\nOLSRd Config File: /etc/olsr.conf\nNameService LatLon.js file: /var/run/latlon.js\n", null);
       }else if(nameServicePath.equals("") && dotDrawPort.equals("")){
           nameServicePath="/var/run/latlon.js";
            dotDrawPort="2004";
           throw new WizardValidationException(null, "Will be used default value for the following values:\nNameService LatLon.js file: /var/run/latlon.js", null);
       }else if(olsrdPath.equals("") && dotDrawPort.equals("") ){
           olsrdPath="/etc/olsr.conf";
           dotDrawPort="2004";
          throw new WizardValidationException(null, "Will be used default value for the following values:\ndotDrawPort: 2004 ", null);
       }else  if(olsrdPath.endsWith("")){
           olsrdPath="/etc/olsr.conf";
                throw new WizardValidationException(null, "Will be used default position: \"/etc/olsr.conf\"", null);
       }else if(nameServicePath.equals("")){
           nameServicePath="/var/run/latlon.js";
                throw new WizardValidationException(null, "Will be used default position: \"/var/run/latlon.js\"", null);
       }else if(dotDrawPort.equals("")){
           dotDrawPort="2004";
                throw new WizardValidationException(null, "Will be used default position: \"/etc/olsr.conf\"", null);
       }*/


    }
    }
    }


