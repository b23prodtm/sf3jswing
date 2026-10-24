/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.ui;

import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.*;
import net.sf.jiga.xtended.kernel.ExtensionsInstaller;
import net.sf.jiga.xtended.kernel.JXAenvUtils;
import net.sf.jiga.xtended.kernel.ThreadWorks;

/**
 * Toolkit to handle Ant interfaces. (Runtime Launcher)
 *
 * @author www.b23prodtm.info
 */
public class AntHandler {

        public static JXAenvUtils _JXAEnvUtils = new JXAenvUtils();
        /**
         * disables any installed L&F (change it before loading)
         */
        public static boolean _noLAF = false;
        /**
         * all available L&F's Map<{os.name}, lookandfeel.binary.name><br>
         * <ol><li>you can add a mapping if you want a custom look and feel and
         * disable standard LAF in net.sf.jiga.xtended.ui.laf.properties; it
         * will load with
         * {@linkplain #_load(net.sf.jiga.xtended.ui.Ant, net.sf.jiga.xtended.ui.DisplayInterface, java.util.Map, java.util.List)};
         * </li><li>or use {@linkplain #_applyLAF(String)}.</li></ol>
         */
        private static Map<String, String> _LAFs = Collections.synchronizedMap(new HashMap<String, String>());

        /**
         * loads up the specified natives, layers and the look and feel and
         * other startup stuff
         *
         * @param splash the splash DisplayInterface
         * @param files the extra natives packages files (i.e. .dll, .jnilib,
         * .dylib, .so) to load in the JXAEnvPath with
         * {@linkplain JXAenvUtils#_loadEnvironment()}
         * @param loadLayers these specified Action's can hold various LOADING
         * SEQUENCES to immediatelly execute. *
         *
         * @see #_noLAF to disable Look'n'Feel install
         * @see #_LAFs to add look and feel
         */
        protected static void _load(Ant ant, DisplayInterface splash, Map<String, Map<String, URL>> files, List<Action> loadLayers) {
                int pty = Thread.currentThread().getPriority();
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                /**
                 * logging environment information
                 */
                String msg = "Running environment bits : ";
                for (JXAenvUtils.env e : JXAenvUtils.env.values()) {
                        if (e._isEnv(e.bitMask())) {
                                msg += e.toString() + " ";
                        }
                }
                System.err.println(JXAenvUtils.log(msg, JXAenvUtils.LVL.SYS_NOT));
                System.out.println(JXAenvUtils.log("Available processors : " + Runtime.getRuntime().availableProcessors(), JXAenvUtils.LVL.SYS_NOT));
                /**
                 * ***********
                 * Look And feels - this is available for change. *********
                 * //AppLauncher._applyLAF("org.fife.plaf.OfficeXP.OfficeXPLookAndFeel");
                 * //AppLauncher._applyLAF("org.fife.plaf.VisualStudio2005.VisualStudio2005LookAndFeel");
                 */
                JPopupMenu.setDefaultLightWeightPopupEnabled(false);
                long frame = -1l;
                if (splash == null) {
                        frame = UIMessage.displayWaiting("Loading...", null);
                } else {
                        if (ant instanceof AntApplet) {
                                _JXAEnvUtils.setAntApplet((AntApplet) ant);
                        }
                        _JXAEnvUtils.setSplashPicture(splash);
                        _JXAEnvUtils.showSplash();
                }
                _JXAEnvUtils.setJXAenvPath(ExtensionsInstaller._findExtPath(true));
                JProgressBar bar = UIMessage.getProgressBar(_JXAEnvUtils.jpb);
                System.out.println(JXAenvUtils.log("OS name : " + JXAenvUtils._getSysValue("os.name") + " OS arch : " + JXAenvUtils._getSysValue("os.arch"), JXAenvUtils.LVL.SYS_NOT));
                System.out.println(JXAenvUtils.log("Library-Path: " + JXAenvUtils.getLibraryPath(), JXAenvUtils.LVL.SYS_NOT));
                System.out.println(JXAenvUtils.log("Class-Path: " + JXAenvUtils.getClasspath(), JXAenvUtils.LVL.SYS_NOT));
                bar.setString("os " + JXAenvUtils._getSysValue("os.name") + " environment...");
                bar.paintImmediately(bar.getVisibleRect());
                if (files == null) {
                        files = new HashMap<String, Map<String, URL>>();
                }
                if (files != null) {
                        try {
                                ExtensionsInstaller._installExtensions(_JXAEnvUtils, files, true, null);
                        } catch (MalformedURLException ex) {
                                if (JXAenvUtils._debug) {
                                        ex.printStackTrace();
                                }
                        }
                }
                if (JXAenvUtils._getSysBoolean("com.sun.media.imageio.disableCodecLib")) {
                        bar.setString("JAI/JIIO codecs natives are disabled");
                        bar.paintImmediately(bar.getVisibleRect());
                        try {
                                Thread.sleep(500);
                        } catch (InterruptedException e) {
                                e.printStackTrace();
                        }
                }
                bar.setString("apply L&F...");
                bar.paintImmediately(bar.getVisibleRect());
                ResourceBundle rb = ResourceBundle.getBundle("net.sf.jiga.xtended.ui.laf");
                if (_noLAF) {
                        bar.setString("default L&F");
                } else if (new String("true").equalsIgnoreCase(rb.getString("enabled"))) {
                        Enumeration<String> keys = rb.getKeys();
                        while (keys.hasMoreElements()) {
                                String k = keys.nextElement();
                                if (JXAenvUtils._getSysValue("os.name").startsWith(k)) {
                                        bar.setString("apply " + rb.getString(k) + " L&F...");
                                        bar.paintImmediately(bar.getVisibleRect());
                                        _applyLAF(rb.getString(k));
                                        break;
                                }
                        }
                }
                if (loadLayers instanceof List) {
                        bar.setMaximum(100);
                        bar.setValue(0);
                        final ActionEvent ae = new ActionEvent(bar, loadLayers.hashCode(), loadLayers.toString());
                        int i = 1;
                        for (Action pa : loadLayers) {
                                try {
                                        final Action a = pa;
                                        if (a.getValue(Action.NAME) instanceof String) {
                                                bar.setString((String) a.getValue(Action.NAME));
                                        } else {
                                                bar.setString("please wait...");
                                        }
                                        bar.paintImmediately(bar.getVisibleRect());
                                        /*Thread t = new Thread(*/ Runnable r = new Runnable() {
                                                public void run() {
                                                        a.actionPerformed(ae);
                                                }
                                        };
                                        if (ThreadWorks.Swing.isEventDispatchThread()) {
                                                r.run();
                                        } else {
                                                try {
                                                        ThreadWorks.Swing.invokeAndWait(r);
                                                } catch (InvocationTargetException ex) {
                                                        if (JXAenvUtils._debug) {
                                                                ex.printStackTrace();
                                                        }
                                                }
                                        }
                                        bar.setValue((int) ((float) i / (float) loadLayers.size() * 100f));
                                        bar.paintImmediately(bar.getVisibleRect());
                                        i++;
                                } catch (InterruptedException ex) {
                                        ex.printStackTrace();
                                }
                        }
                }
                bar.setValue(bar.getMaximum());
                bar.setString("loading is complete");
                bar.paintImmediately(bar.getVisibleRect());
                try {
                        Thread.sleep(5000);
                        if (splash == null) {
                                UIMessage.kill(frame);
                        } else {
                                _JXAEnvUtils.hideSplash();
                        }
                        Thread.currentThread().setPriority(pty);
                } catch (InterruptedException ex) {
                        ex.printStackTrace();
                }
        }

        /**
         * returns true or false whether the look and feel has been applied or
         * not to the current UIManager, resp. Notice that the look and feel can
         * be set easily by editing the resource bundle
         * "net/sf/jiga/installer/laf.properties" file
         *
         * @throws Exception if the look and feel failed to initialize
         * @param className the class name of the look and feel to apply (null
         * or "": crossPlatform L&F; "native" : system native L&F)
         * @see UIManager#setLookAndFeel(String)
         *
         * @return true or false
         */
        public static boolean _applyLAF(String className) {
                try {
                        if (new String("").equals(className) || className == null) {
                                className = UIManager.getCrossPlatformLookAndFeelClassName();
                        } else if (className.equalsIgnoreCase("native")) {
                                className = UIManager.getSystemLookAndFeelClassName();
                        } else {
                                _LAFs.put(JXAenvUtils._getSysValue("os.name"), className);
                        }
                        UIManager.setLookAndFeel(className);
                        System.out.println("L&F " + className + " has been set up.");
                        return true;
                } catch (Exception e) {
                        UIMessage.showLightPopupMessage(new JLabel("<html>Look and Feel class not found <br>" + className + ".</html>"), null, null, UIMessage.UI_BOTTOM_RIGHT);
                        return false;
                }
        }
}
