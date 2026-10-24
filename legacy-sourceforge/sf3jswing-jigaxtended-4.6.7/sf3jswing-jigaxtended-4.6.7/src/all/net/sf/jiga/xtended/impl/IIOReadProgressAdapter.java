/*

 * IIOReadProgressAdapter.java

 *

 * Created on 1 juin 2007, 01:42

 *

 * To change this template, choose Tools | Template Manager

 * and open the template in the editor.

 */
package net.sf.jiga.xtended.impl;

import java.io.IOException;


import javax.imageio.ImageReader;

import javax.imageio.event.IIOReadProgressListener;
import net.sf.jiga.xtended.kernel.JXAenvUtils;

/**

 * * this class implements IIOReadProgressListener from JIIO to get a full log of the ImageIO read channels

 * @author www.b23prodtm.info

 */
public class IIOReadProgressAdapter implements IIOReadProgressListener {

    long hash = System.nanoTime();

    @Override
    public int hashCode() {
        return (int) hash;
    }

    @Override
    public boolean equals(Object o) {
        return o == null ? false : o.hashCode() == hashCode();
    }
    /** dis/enables printing log to System std output */
    boolean printLog;

    /** creates a new instance

    @param printLog sets up logging to System.output*/
    public IIOReadProgressAdapter(boolean printLog) {

        this.printLog = printLog;

    }

    /** an "sequence has started"-event occured 

    @param source the ImageReader source that is logging

    @param minIndex the minimum index value read on the image source

    @throws IOException if an error occured when trying to log */
    @Override
    public void sequenceStarted(ImageReader source, int minIndex) {

        try {

            if (printLog) {
                System.out.print("Read Sequence... (" + source.getFormatName() + ")");
            }

        } catch (IOException ex) {

            if (JXAenvUtils._debug) {
                ex.printStackTrace();
            }

        }

    }

    /** an "sequence has completed"-event occured

    @param source the imageReader source that is logging */
    @Override
    public void sequenceComplete(ImageReader source) {

        if (printLog) {
            System.out.println("sequence read is done.");
        }

    }

    /** an "image has started"-event occured

    @param source the ImageReader source that is logging

    @param imageIndex te image index value read on the image source

    @throws IOException if an error occured when trying to log*/
    @Override
    public void imageStarted(ImageReader source, int imageIndex) {

        try {

            if (printLog) {
                System.out.print("Read (i:" + imageIndex + ")... (" + source.getFormatName() + ")");
            }

        } catch (IOException ex) {

            if (JXAenvUtils._debug) {
                ex.printStackTrace();
            }

        }

    }

    /** an "image progress"-event occured

    @param source the imageReader soure that is logging

    @param percentageDone the percentage done on the image source*/
    @Override
    public void imageProgress(ImageReader source, float percentageDone) {

        if (printLog) {
            System.out.print(".");
        }

    }

    /** an "image has completed"-event occured

    @param source the ImageReader source that is logging*/
    @Override
    public void imageComplete(ImageReader source) {

        if (printLog) {
            System.out.println("read is done.");
        }

    }

    /** an "thumbnail has started"-event occured

    @param source the ImageReader source that is logging

    @param imageIndex the image index value on the image source

    @param thumbnailIndex the image thumbnail index value on the image source

    @throws IOException if a error occured when trying to log*/
    @Override
    public void thumbnailStarted(ImageReader source,
            int imageIndex, int thumbnailIndex) {

        try {

            if (printLog) {
                System.out.print("Read Thumbnail(i:" + imageIndex + " t:" + thumbnailIndex + ")... (" + source.getFormatName() + ")");
            }

        } catch (IOException ex) {

            if (JXAenvUtils._debug) {
                ex.printStackTrace();
            }

        }

    }

    /** an "thumbnail progress"-event occured

    @param source the ImageReader source that is logging

    @param percentageDone the percentage done on the image source*/
    @Override
    public void thumbnailProgress(ImageReader source, float percentageDone) {

        if (printLog) {
            System.out.print(".");
        }

    }

    /** an "thumbnail has completed"-event occured

    @param source the ImageReader source that is logging*/
    @Override
    public void thumbnailComplete(ImageReader source) {

        if (printLog) {
            System.out.println("thumb read is done.");
        }

    }

    /** an "read has aborted"-event occured

    @param source the ImageReader source that is logging*/
    @Override
    public void readAborted(ImageReader source) {

        if (printLog) {
            System.out.println("canceled!");
        }

    }
}

