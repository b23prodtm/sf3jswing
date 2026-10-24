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
import javax.swing.JFrame;
import net.sf.jiga.xtended.impl.game.test.TestGameActionLayer;
import net.sf.jiga.xtended.ui.AntFrame;
import net.sf.jiga.xtended.ui.DisplayInterface;

/**
 *
 * @author www.b23prodtm.info
 */
public class TestAntFrame implements AntFrame {

        JFrame frame;

    public TestAntFrame(String[] args) {
    }

    public void setFrame(JFrame arg0) {
        frame = arg0;
    }

    public JFrame getFrame() {
        return frame;
    }
    boolean initialized = false;
    TestGameActionLayer tim = new TestGameActionLayer();

    public void initComponents() {
        if (initialized) {
            return;
        }
        try {           
           tim.initDemoScene(frame);            
            initialized = true;
        } catch (Throwable ex) {
            ex.printStackTrace();
            initialized = false;
        }
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

    /*public AppLauncher getAppLauncher() {
    return appLauncher;
    }*/
    public void start() {
        if (initialized) {
            frame.pack();
            frame.setVisible(true);
            tim.start();
        }
    }

    public void stop() {
        if (initialized) {
            tim.stop();
        }
    }

    public void destroyComponents() {
        if (!initialized) {
            return;
        }
        frame.getContentPane().removeAll();
        initialized = false;
    }

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
