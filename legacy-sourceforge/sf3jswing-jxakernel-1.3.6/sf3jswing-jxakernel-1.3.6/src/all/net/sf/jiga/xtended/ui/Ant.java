/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.jiga.xtended.ui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.List;
import javax.swing.Action;

/**
 * Root interface for application run on JXAkernel
 * @author www.b23prodtm.info
 */
public interface Ant extends Transferable{

    public final static String ANT_FRAME = "antframe", ANT_APPLET = "antapplet";
    /** Debug System Properties Keys (JVM systeme properties parameter -J-D[theparam=true|false]: 
     * JXA (jigaxtended api) debug, JXAKernel (jxakernel) debug, JXA Graphics/Sound/etc. Rendering debug, JXAKernel Console debug (may deadlock on System.out if enabled), JXAKernel Cache Manager debug (to see any serialization faults), JXAKernel ClassLoader debug
     */
    public final static String JXA_DEBUG = "jxa.debug",  JXA_DEBUG_SYS = "jxa.debugSys", JXA_DEBUG_RENDER = "jxa.debugRender", JXA_DEBUG_RENDER_LOW = "jxa.debugRenderLow", JXA_DEBUG_CSL = "jxa.debugCSL", JXA_DEBUG_SPM = "jxa.debugSPM", JXA_DEBUG_ECL = "jxa.debugECL", JXA_DEBUG_GFX = "jxa.debugGfx";

    /** the Void LEVEL (traces that should not be cared about)*/
    public final static String JXA_DEBUG_VOID = "jxa.void";

    /** handles external data to be opened by this Applet
     * The data instance is a java.util.List<File> instance when the {@link DataFlavor#javaFileListFlavor file list flavor} is sent by a drag'n'drop operation or a String if it's DataFlavor.stringFlavor or any other flavor 
     * (operating system dependent).<br>
     * <ul><li>getTransferDataFlavors must return the DataFlavor's you'd like to accept, while this method would handle them.</li>
     * <li>isDataFlavorSupported() must tell if it's what you like. (prompt user...)</li>
     * </ul>
     *
     @param data the external data to open 
     @return true or false whether the process succeeded or not, resp.
     */
    public boolean open(Object data);
    
    /** handles saving options, images or other datas to a file 
     @param file the file path where to store the data
     @return true or false whether the process succeeded or failed, resp.*/
    public boolean save(String file);
    
    
    /** starts the Timer's, Thread's, ...*/
    public void start();
    
    /** eventually stops Threads or other activity */
    public void stop();

    /** @return true or false whether the applet is initialized or not
     */
    public boolean isInitialized();

    /** @return null or an Action to do on shutdown, e.g. backup, save state, ..
     */
    public Runnable shutdownHook();

    /** splash picture at loading
     @return null or Display instance*/
    public DisplayInterface getSplash();

    /** load layers right after class instanciation
     @return null or a list of loading layers*/
    public List<Action> getLoadLayers();
}
