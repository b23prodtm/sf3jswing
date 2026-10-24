/*
 * Chrono.java
 *
 * Created on 21 octobre 2007, 03:19
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.sf.jiga.xtended.impl.system;

import java.awt.image.BufferedImage;

/**
 * Chrono is a simple chronometer 
 * @author www.b23prodtm.info
 */
public class Chrono {
    /** timestamps */
    long start, end, last;
    /** state @default 0 */
    int state = 0;
    
    /** creates a new instance 
     * @param size the initial size */
    public Chrono() {
        super();
        start = end = last = 0;
    }
    
    /** starts the chronometer 
     * @see #stop() */
    public synchronized void start() {
        start = System.currentTimeMillis();
        state = 1;
        notify();
    }
    
    /** stops the chronometer
     * @see #start()*/
    public synchronized void stop() {
        end = System.currentTimeMillis();
        state = 0;
        notify();
    }
    
    /** returns the current chronometer elapsed time in ms
     * @return the current chronometer elapsed time in ms*/
    public long getCurrentChronoTime() {
        return (state == 0)?end - start:System.currentTimeMillis() - start;
    }
    
    /** returns the start timestamp */
    public long getStartTime() {
        return start;
    }
    
    /** converts the chronometer status in a String
     * @return the converter chronometer status 
     * @see #getCurrentChronoTime()*/
    public String toString() {
        long time = getCurrentChronoTime();
        String strSec = (time % 1000 < 500)?" ":":";            
        return String.format("%1$tH", time - 3600000) + ":" + String.format("%1$tM", time - 3600000) + strSec + String.format("%1$tS", time - 3600000) + "." + String.format("%1$tL", time - 3600000);
    }
    
}
