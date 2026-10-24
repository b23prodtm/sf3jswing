/*

 * IIOWriteWarningAdapter.java

 *

 * Created on 1 juin 2007, 01:46

 *

 * To change this template, choose Tools | Template Manager

 * and open the template in the editor.

 */
package net.sf.jiga.xtended.impl;


import javax.imageio.ImageWriter;

import javax.imageio.event.IIOWriteWarningListener;

/**

 * * this class implements IIOWriteWarningListener from JIIO to get a full log of the ImageIO write channels

 * @author www.b23prodtm.info

 */
public class IIOWriteWarningAdapter implements IIOWriteWarningListener {

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

    @param printLog dis/enables printing log to the System std output*/
    public IIOWriteWarningAdapter(boolean printLog) {

        this.printLog = printLog;

    }

    /** an "warning has occurred"-event occurred

    @param imageWriter the ImageWriter source that is logging

    @param i the index value on the image source

    @param string the warning String message*/
    @Override
    public void warningOccurred(ImageWriter imageWriter, int i, String string) {

            if (printLog) {
                System.out.println("(" + imageWriter.getOriginatingProvider().getFormatNames()[0] + ") i:"+i+" " + string);
            }


    }
}

