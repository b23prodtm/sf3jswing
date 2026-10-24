/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.ui.test;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Vector;
import javax.swing.Action;
import javax.swing.JApplet;
import net.sf.jiga.xtended.impl.game.test.TestGameActionLayer;
import net.sf.jiga.xtended.ui.AntApplet;
import net.sf.jiga.xtended.ui.DisplayInterface;

/**
 *
 * @author www.b23prodtm.info
 */
public class TestAntApplet implements AntApplet {

        public TestAntApplet(String[] args) {
        super();
    }

    public void start() {
        if (initialized) {
           tim.start();
        }
    }

    public void stop() {
        if (initialized) {
           tim.stop();
        }
    }
    TestGameActionLayer tim = new TestGameActionLayer();

    public void init() {
        if (initialized) {
            return;
        }
        
                try {
            tim.initDemoScene(applet);
            initialized = true;
        } catch (Throwable ex) {
            ex.printStackTrace();
            initialized = false;
        }
    }

    public void setApplet(JApplet arg0) {
        applet = arg0;
    }
    JApplet applet;

    public JApplet getApplet() {
        return applet;
    }

    public boolean open(Object data) {
        return tim.open(data);
    }

    public boolean save(String file) {
        return tim.save(file);
    }

    public DataFlavor[] getTransferDataFlavors() {
        return tim.getTransferDataFlavors();
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return tim.isDataFlavorSupported(flavor);
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        return tim.getTransferData(flavor);
    }

    public void destroy() {
        if (!initialized) {
            return;
        }
        applet.getContentPane().removeAll();
        applet.getContentPane().validate();
        initialized = false;
    }
    boolean initialized = false;

    public boolean isInitialized() {
        return initialized;
    }

    public Runnable shutdownHook() {
        return tim.shutdownHook();
    }

    public DisplayInterface getSplash() {
        return tim.getSplash();
    }

    public Vector<Action> getLoadLayers() {
        return tim.getLoadLayers();
    }
}
