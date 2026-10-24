/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.jiga.xtended.impl.system;

/**
 *
 * @author www.b23prodtm.info
 */
public interface LoadListener {
    public void loadBegin();
    public void loadIsComplete();
    public void unloadBegin();
    public void unloadIsComplete();
    public int getLoadState();
}
