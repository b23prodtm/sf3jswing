/*
 * ErrorListener.java
 *
 * Created on 1 mai 2007, 19:35
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.sf.jiga.xtended.kernel;

import java.io.IOException;

/**
 * This interface is used in Console.
 * @author www.b23prodtm.info
 */
public interface ErrorListener {
        
    /** Console output printstream received a print error */
    public void printStreamError(IOException e);
}
