/*

 * IIOReadWarningAdapter.java

 *

 * Created on 1 juin 2007, 01:46

 *

 * To change this template, choose Tools | Template Manager

 * and open the template in the editor.

 */
package net.sf.jiga.xtended.impl;

import java.io.IOException;


import javax.imageio.ImageReader;

import javax.imageio.event.IIOReadWarningListener;
import net.sf.jiga.xtended.kernel.JXAenvUtils;

/**

 * * this class implements IIOReadWarningListener from JIIO to get a full log of the ImageIO read channels

 * @author www.b23prodtm.info

 */
public class IIOReadWarningAdapter implements IIOReadWarningListener {

    long hash = System.nanoTime();

    @Override
    public int hashCode() {
        return (int) hash;
    }

    @Override
    public boolean equals(Object o) {
        return o == null ? false : o.hashCode() == hashCode();
    }
    /** dis/enables printing to system std output*/
    boolean printLog;

    /** creates a new instance 

    @param printLog sets up printing the log to system std output*/
    public IIOReadWarningAdapter(boolean printLog) {

        this.printLog = printLog;

    }

    /** an "warning has occured"-event occured

    @param source the ImageReader source that is logging

    @param warning the warning String

    @throws IOException if an error occured when trying to log */
    @Override
    public void warningOccurred(ImageReader source, String warning) {

        try {

            if (printLog) {
                System.out.println("(" + source.getFormatName() + ") " + warning);
            }

        } catch (IOException ex) {

            if (JXAenvUtils._debug) {
                ex.printStackTrace();
            }

        }

    }
}
