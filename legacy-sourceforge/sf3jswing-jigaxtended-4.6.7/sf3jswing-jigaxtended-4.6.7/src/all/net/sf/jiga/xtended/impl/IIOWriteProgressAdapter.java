/*

 * IIOWriteProgressAdapter.java

 *

 * Created on 1 juin 2007, 01:43

 *

 * To change this template, choose Tools | Template Manager

 * and open the template in the editor.

 */
package net.sf.jiga.xtended.impl;

import javax.imageio.ImageWriter;

import javax.imageio.event.IIOWriteProgressListener;

/**

 * * this class implements IIOWriteProgressListener from JIIO to get a full log of the ImageIO write channels

 * @author www.b23prodtm.info

 */
public class IIOWriteProgressAdapter implements IIOWriteProgressListener {

    long hash = System.nanoTime();

    @Override
    public int hashCode() {
        return (int) hash;
    }

    @Override
    public boolean equals(Object o) {
        return o == null ? false : o.hashCode() == hashCode();
    }
    /** dis/enables printing to system std output */
    boolean printLog;

    /** creates a new instance

    @param printLog dis/enables printing log to system std output */
    public IIOWriteProgressAdapter(boolean printLog) {

        this.printLog = printLog;

    }

    /** an "image has completed"-event occured

    @param imageWriter the ImageWriter source that is logging*/
    @Override
    public void imageComplete(ImageWriter imageWriter) {

        String str;

        if (printLog) {
            System.out.println(str = "complete.");
        }

    }

    /** an "image has started"-event occured

    @param imageWriter the ImageWriter source that is logging

    @param i the index value on the writer source*/
    @Override
    public void imageStarted(ImageWriter imageWriter, int i) {

        String str;

        if (printLog) {
            System.out.println(str = "Starting write (i:" + i + ")... (" + imageWriter.getOriginatingProvider().getFormatNames()[0] + ")");
        }

    }

    /** an "image progress"-event occured

    @param imageWriter the ImageWriter source that is logging

    @param percentageDone the percentage done on the writer source*/
    @Override
    public void imageProgress(ImageWriter imageWriter, float percentageDone) {

        String str;

        if (printLog) {
            System.out.print(str = ".");
        }

    }

    /** an "thumbnail has completed"-event occured. NOT IMPLEMENTED

    @param imageWriter the ImageWriter source that is logging

     */
    @Override
    public void thumbnailComplete(ImageWriter imageWriter) {
        if (printLog) {
            System.out.println("thumb write is done.");
        }
    }

    /** an "thumbnail progress"-event occured. NOT IMPLEMENTED

    @param imageWriter the ImageWriter source that is logging

    @param percentageDone the percentage done on the image source*/
    @Override
    public void thumbnailProgress(ImageWriter imageWriter, float percentageDone) {
        if (printLog) {
            System.out.print(".");
        }
    }

    /** an "thumbnail has started"-event occured. NOT IMPLEMENTED

    @param imageWriter the ImageWriter source that is logging

    @param i index value on the image source

    @param i0 index value on the image source*/
    @Override
    public void thumbnailStarted(ImageWriter imageWriter, int i, int i0) {
        if (printLog) {
            System.out.print("Write Thumbnail(i:" + i + " t:" + i0 + ")... (" + imageWriter.getOriginatingProvider().getFormatNames()[0] + ")");
        }

    }

    /** an "write has aborted"-event occured

    @param imageWriter the ImageWriter source that is logging*/
    @Override
    public void writeAborted(ImageWriter imageWriter) {

        String str;

        if (printLog) {
            System.out.print(str = "aborted!");
        }

    }
}



