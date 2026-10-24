/*

 * SfMediaTracker.java

 *

 * Created on 28 septembre 2007, 04:34

 *

 * To change this template, choose Tools | Template Manager

 * and open the template in the editor.

 */



package net.sf.jiga.xtended.impl.system;



import java.awt.Component;

import java.awt.MediaTracker;
import javax.swing.JLabel;



/**

 * This class is extending the original MediaTracker class to allows comparison of different instances of the MediaTracker, something impossible with the original.

 * @author www.b23prodtm.info

 */

public class SfMediaTracker extends MediaTracker {

    static JLabel _obs = new JLabel();

    /** identifies the SfMediaTracker with a timestamp */

    private long hash = System.nanoTime();

    

    /** the Component that is observing the data held by this SfMediaTracker instance */

    private Component obs;

    public SfMediaTracker() {
        super(_obs);
        obs = _obs;
    }

    


    /** creates a new instance

     @param component the Component that is observing the data held by this SfMediaTracker instance 

     @see MediaTracker#MediaTracker(Component)*/

    public SfMediaTracker(Component component) {

        super(component);

        obs = component;

    }

    

    /** returns the Component instance that is observing the data held in this SfMediaTracker 

     @return the Component instance that is observing the data held in this SfMediaTracker*/

    public Component getObserver() {

        return obs;

    }

    

    /** returns the hash code identifying this SfMediaTracker instance

     @return the hash code identifying this SfMediaTracker instance*/

    public int hashCode() {

        return (int)hash;

    }

    

    /** returns true or false whether this SfMediaTracker instance is equal to another instance or not, resp. Their hash code are compared each other.

    @param o the Object instance to compare to

     * @return true or false*/

    public boolean equals(Object o) {

        if(o instanceof MediaTracker) {

            MediaTracker e = (MediaTracker)o;

            return e.hashCode() == hashCode();

        } else

            return false;

    }

}

