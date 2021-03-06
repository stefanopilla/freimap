/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package freimapgsoc;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbPreferences;

public class FreimapWizardPanel4 implements WizardDescriptor.Panel {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private Component component;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new FreimapVisualPanel4();
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

    /*
    hostNameLabel.setText(NbPreferences.forModule(FreimapWizardPanel2.class).get("host", "localhost"));
    mySQLLabel.setText(NbPreferences.forModule(FreimapWizardPanel2.class).get("dbText", "freimap"));
    mySQLPortLabel.setText(NbPreferences.forModule(FreimapWizardPanel2.class).get("port", "3306"));
    usernameLabel.setText(NbPreferences.forModule(FreimapWizardPanel2.class).get("username", "root"));
    passwordLabel.setText(NbPreferences.forModule(FreimapWizardPanel2.class).get("password", ""));
    olsrdLabel.setText(NbPreferences.forModule(FreimapWizardPanel3.class).get("olsrdPath", "/etc/olsrd.conf"));
    dotDrawPortLabel.setText(NbPreferences.forModule(FreimapWizardPanel3.class).get("dotDrawPort", "2004"));
    NameServicePathLabel.setText(NbPreferences.forModule(FreimapWizardPanel3.class).get("nameServicePath", "/var/run/latlon.js"));
     *
     */
    public void readSettings(Object settings) {
    }

    public void storeSettings(Object settings) {
        ((WizardDescriptor) settings).putProperty("host", ((FreimapVisualPanel2) getComponent()).getHost());
        ((WizardDescriptor) settings).putProperty("username", ((FreimapVisualPanel2) getComponent()).getUserName());
        ((WizardDescriptor) settings).putProperty("password", ((FreimapVisualPanel2) getComponent()).getPassword());
        ((WizardDescriptor) settings).putProperty("dbText", ((FreimapVisualPanel2) getComponent()).getDatabase());
        ((WizardDescriptor) settings).putProperty("port", ((FreimapVisualPanel2) getComponent()).getPort());
        ((WizardDescriptor) settings).putProperty("olsrdPath", ((FreimapVisualPanel3) getComponent()).getOlsrdPath());
        ((WizardDescriptor) settings).putProperty("nameServicePath", ((FreimapVisualPanel3) getComponent()).getNameServicePath());
        ((WizardDescriptor) settings).putProperty("dotDrawPort", ((FreimapVisualPanel3) getComponent()).getDotDrawPort());


    }
}

