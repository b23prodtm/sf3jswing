/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.jiga.xtended.ui;

import java.awt.Image;
import javax.swing.JComponent;

/**
 *
 * @author www.b23prodtm.info
 */
public interface DisplayInterface {

    /**
     * returns the picture being displayed in this component
     *
     * @return the displayed picture
     */
    Image getPicture();
    
    /** Component that will eventually display the picture */
    JComponent getJComponentDisplay();
 
}
