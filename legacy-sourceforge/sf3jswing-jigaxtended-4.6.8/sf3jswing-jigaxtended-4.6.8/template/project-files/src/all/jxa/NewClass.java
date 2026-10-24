/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jxa;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;
import javax.swing.Action;
import javax.swing.JApplet;
import javax.swing.JFrame;
import net.sf.jiga.xtended.ui.AntApplet;
import net.sf.jiga.xtended.ui.DisplayInterface;

/**
 *
 * @author www.b23prodtm.info
 */
public class @NEWJXAPROJECTCLASS@ implements AntApplet , AntFrame

    {

        /**
         * @param args args from java command-line
         */
    
    public @NEWJXAPROJECTCLASS@(String[] args) {}
    
    public boolean open(Object o) {
        /**
         * ! implement with your code !
         */
    }

    public boolean save(String string) {
        /**
         * ! implement with your code !
         */
    }

    /**
     * ### APPLET and FRAME SUPPORT ###
     */
    public void start() {
        /**
         * ! implement with your code !
         */
    }

    /**
     * ### APPLET and FRAME SUPPORT ###
     */
    public void stop() {
        /**
         * ! implement with your code !
         */
    }
    boolean initialized = false;

    public boolean isInitialized() {
        return initialized;
    }

    public Runnable shutdownHook() {
        /**
         * ! implement with your code !
         */
    }

    public DataFlavor[] getTransferDataFlavors() {
        /**
         * ! implement with your code !
         */
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        /**
         * ! implement with your code !
         */
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        /**
         * ! implement with your code !
         */
    }
    JApplet applet = null;

    /**
     * ### APPLET SUPPORT ###
     */
    public void setApplet(JApplet ja) {
        applet = ja;
    }

    /**
     * ### APPLET SUPPORT ###
     */
    public JApplet getApplet() {
        return applet;
    }

    /**
     * ### APPLET SUPPORT ###
     */
    public void destroy() {
        /**
         * ! implement with your code !
         */
        /**
         * 
         */
        initialized = false;
    }

    /**
     * ### APPLET SUPPORT ###
     */
    public void init() {
        /**
         * ! implement with your code !
         */
        /**
         * 
         */
        initialized = true;
    }
    JFrame frame = null;

    /**
     * ### JFRAME APP SUPPORT ###
     */
    public void setFrame(JFrame jframe) {
        frame = jframe;
    }

    /**
     * ### JFRAME APP SUPPORT ###
     */
    public JFrame getFrame() {
        return frame;
    }

    /**
     * ### JFRAME APP SUPPORT ###
     */
    public void destroyComponents() {
        /**
         * ! implement with your code !
         */
        /**
         * 
         */
        initialized = false;
    }

    /**
     * ### JFRAME APP SUPPORT ###
     */
    public void initComponents() {
        /**
         * ! implement with your code !
         */
        /**
         * 
         */
        initialized = true;
    }

    /**
     * ### APPLET and FRAME SUPPORT ###
     */
    public DisplayInterface getSplash() {
        /**
         * ! implement with your code !
         */
    }

    /**
     * ### APPLET and FRAME SUPPORT ###
     */
    public List<Action> getLoadLayers() {
        /**
         * ! implement with your code !
         */
    }
}
