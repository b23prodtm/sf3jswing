/*
 * SpritesCacheListener.java
 *
 * Created on 26 avril 2007, 01:47
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.kernel;

/**
 * The SpritesCacheListener listens to the SpritesCacheManager as data is handled over time.
 * @author www.b23prodtm.info
 */
public interface SpritesCacheListener {

    /** the "read started" event id */
    public static final int READ_STARTED = 0;
    /** the "read aborted" event id*/
    public static final int READ_ABORTED = 1;
    /** the "read completed" event id*/
    public static final int READ_COMPLETED = 2;
    /** the "read error" event id*/
    public static final int READ_ERROR = 3;
    /** the "write started" event id*/
    public static final int WRITE_STARTED = 4;
    /** the "write aborted" event id*/
    public static final int WRITE_ABORTED = 5;
    /** the "write completed" event id*/
    public static final int WRITE_COMPLETED = 6;
    /** the "write error" event id*/
    public static final int WRITE_ERROR = 7;
    /** the "capacity extended" event id*/
    public static final int CAPACITY_EXTENDED = 8;

    /** write has aborted in the cache*/
    public void writeAborted();

    /** write has completed in the cache*/
    public void writeCompleted();

    /** write has started in the cache*/
    public void writeStarted();

    /** write process has encountered an error
    @param trace the stack trace*/
    public void writeError(Exception trace);

    /** capacity has been extended 
    @param newCapacity the new extended capacity*/
    public void capacityExtended(int newCapacity);

    /** read has aborted in the cache*/
    public void readAborted();

    /** read has completed in the cache*/
    public void readCompleted();

    /** read process has encountered an error
    @param trace the stack trace */
    public void readError(Exception trace);

    /** read has started in the cache*/
    public void readStarted();
    }
    
