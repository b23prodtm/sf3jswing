/*
 * CompositeCapable.java
 *
 * Created on 16. juillet 2007, 01:19
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.sf.jiga.xtended.impl;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Paint;
import net.sf.jiga.xtended.ui.OpaqueComponent;

/**
 * This interface defines the composite capabilities.
 * @author www.b23prodtm.info
 */
public interface CompositeCapable extends OpaqueComponent{
    /** returns the composite status
     @return true or false*/
    public boolean isCompositeEnabled();
    
    /** dis/enable composite mode 
     @param b mode dis/enabled*/
    public void setCompositeEnabled(boolean b);
    
    /** returns the current associated Composite instance.
     @return the current associated composite instance 
     @see #setComposite(Composite)*/
    public Composite getComposite();
    
    /** defines the Composite instance to associate.
     @param cps the instance to associate*/
    public void setComposite(Composite cps);
    
    /** returns the current associated Paint instance.
     @return the current associated Paint instance
     @see #setPaint(Paint)*/
    public Paint getPaint();
    
    /** defines the Paint instance to associate.
     @param pnt the Paint instance to associate*/
    public void setPaint(Paint pnt);
    
    /** returns the current associated Color instance.
     @return the current associated Color instance
     @see #setColor(Color)*/
    public Color getColor();
    
    /** defines the Color instance to associate.
     @param clr the Color instance to associate*/
    public void setColor(Color clr);
}
