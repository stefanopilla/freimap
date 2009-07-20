/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package PopUp;

import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceTypeListener;

/**
 *
 * @author stefano
 */
public class ServiceListener {

    static class SListener implements ServiceTypeListener {

        public void serviceTypeAdded(ServiceEvent event) {
            System.out.println("Service type added: " +event.getType());
        }
    }

}
