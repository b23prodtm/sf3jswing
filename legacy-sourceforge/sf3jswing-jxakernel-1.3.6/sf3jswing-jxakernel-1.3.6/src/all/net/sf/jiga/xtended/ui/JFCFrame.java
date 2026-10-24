/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.ui;

import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.LogManager;
import javax.swing.Action;
import net.sf.jiga.xtended.kernel.*;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.WindowConstants;
import net.sf.jiga.xtended.kernel.JXAenvUtils;

/**
 * relevant args to use JFCFrame as main and load an AntFrame are : "-antframe
 * class.binary.name" ["-width pixelwidth" "-height pixelheight"] and
 * JXAenvUtils._getSysValue(name) "jxa.debug"=boolean, "jxa.debugSys"=boolean
 * <br> Example of starting an application with the JFCFrame (in
 * build-youros-yourarch.xml, specified native librairies are first loaded in
 * temp in this way) :
 * <pre>java -jar jxa-kernel.jar net.sf.jiga.xtended.ui.JFCFrame -antframe myPackage.myAntFrame -width 1024</pre>
 * the above code implies that the antframe declares a constructor
 * AntFrame(String[] args) to pass the arguments of java.<br> JFCFrame extends
 * the JFrame class to make it possible to load
 * {@linkplain Ant#getLoadLayers() stuff} before to
 * {@linkplain AntFrame#initComponents() initialize} the user app.
 *
 * @author www.b23prodtm.info
 */
public final class JFCFrame extends JFrame implements AntFrame {

    static AccessControlContext _acc = AccessController.getContext();

    static {
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
    }
    private static boolean restartingEnv = false;

    public final void destroyComponents() {
        if (targetFrame != null) {
            if (targetFrame.isInitialized()) {
                targetFrame.destroyComponents();
            }
        }
        getContentPane().removeAll();
        getContentPane().validate();
        uninstall();
    }

    /**
     * returns the "isInitialized" property value
     *
     * @return the "isInitialized" property
     */
    public final boolean isInitialized() {
        if (targetFrame != null) {
            return targetFrame.isInitialized();
        }
        return false;
    }

    private void uninstall() {
        env.unloadEnvironment(!restartingEnv);
    }

    private void install() {
        env.loadEnvironment();
    }
    JXAenvUtils env;

    private JFCFrame() {
        super(JXAenvUtils._defaultGC);
        env = new JXAenvUtils(Thread.currentThread().getContextClassLoader());
        LogManager.getLogManager().addLogger(env);
    }

    public JFCFrame(String antframe, String[] args) {
        this();
        this.args = args;
        /**
         * UID for the application
         */
        env.antClassName = antframe;
    }

    public JFCFrame(AntFrame antframe, String[] args) {
        this();
        this.targetFrame = antframe;
        this.args = args;
        /**
         * UID for the application
         */
        env.antClassName = antframe.getClass().getName();
    }

    public JFCFrame(String[] args) {
        this();
        this.args = args;
        /**
         * UID for the application
         */
        env.antClassName = JXAenvUtils._getSysValue(ANT_FRAME);
    }

    public final JFrame getFrame() {
        if (targetFrame == null) {
            return this;
        } else {
            return targetFrame.getFrame();
        }
    }
    AntFrame targetFrame = null;

    /**
     * for a deep (load screen, ...) restart, the instance returned by
     * {@link AntFrame#getFrame()} should be a JFCFrame.
     */
    public static void _restart(AntFrame f) {
        restartingEnv = true;
        JFrame j = f.getFrame();
        if (j != null && j instanceof JFCFrame) {
            f = (JFCFrame) j;
        }
        f.stop();
        f.destroyComponents();
        f.initComponents();
        f.start();
        restartingEnv = false;
    }
    static String[] appClasspath = null;

    public final void initComponents() {
        try {
            getContentPane().removeAll();
            validate();
            /**
             * per-app install folder
             */
            File path = new File(env.getJXAenvPath() + File.separator + env.antClassName);
            path.mkdirs();
            env.setJXAenvPath(path.getAbsolutePath());
            if (appClasspath == null) {
                appClasspath = env.getClasspathArray();
                List<URL> urls = new ArrayList<URL>();
                for (String jar : appClasspath) {
                    File n = new File(jar);
                    if (n.isFile()) {
                        urls.add(n.toURI().toURL());
                    }
                }
                env.addEnvJars(urls.toArray(new URL[]{}));
            }
            env.showSplash();
            install();
            env.hideSplash();
            if (targetFrame == null) {
                /*
                 * 
                 */
                targetFrame = AccessController.doPrivileged(new PrivilegedExceptionAction<AntFrame>() {
                    public AntFrame run() throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
                        return (AntFrame) ExtensionsClassLoader._proxyClass(ExtensionsClassLoader.forName(env.antClassName).getConstructor(new Class[]{String[].class}).newInstance(new Object[]{args}), AntFrame.class);
                    }
                }, _acc);
            } else if (targetFrame.isInitialized()) {
                return;
            }
            if (targetFrame.shutdownHook() != null) {
                env.addPreUEnvLayer(targetFrame.shutdownHook());
            }
            targetFrame.setFrame(this);
            AntHandler._load(targetFrame, targetFrame.getSplash(), null, targetFrame.getLoadLayers());
            targetFrame.initComponents();
        } catch (Exception ex) {
            if (ex instanceof PrivilegedActionException) {
                ex = ((PrivilegedActionException) ex).getException();
            }
            JXAenvUtils._popExceptionToUser(true, Thread.currentThread(), ex);
        }
    }

    public final boolean open(Object data) {
        if (targetFrame != null) {
            if (targetFrame.isInitialized()) {
                return targetFrame.open(data);
            }
        }
        return false;
    }

    public final boolean save(String file) {
        if (targetFrame != null) {
            if (targetFrame.isInitialized()) {
                return targetFrame.save(file);
            }
        }
        return false;
    }

    public final void start() {
        if (targetFrame != null) {
            assert targetFrame.isInitialized() : "Maybe an error occured while init() was called or set the variable \"AntFrame.initialized\" to true).  Set -Djxa.debugSys=true to log system errors !";
            targetFrame.start();
        }
    }

    public final void stop() {
        if (targetFrame != null) {
            if (targetFrame.isInitialized()) {
                targetFrame.stop();
            }
        }
    }

    public final DataFlavor[] getTransferDataFlavors() {
        if (targetFrame != null) {
            if (targetFrame.isInitialized()) {
                return targetFrame.getTransferDataFlavors();
            }
        }
        return null;
    }

    public final boolean isDataFlavorSupported(DataFlavor flavor) {
        if (targetFrame != null) {
            if (targetFrame.isInitialized()) {
                return targetFrame.isDataFlavorSupported(flavor);
            }
        }
        return false;
    }

    public final Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (targetFrame != null) {
            if (targetFrame.isInitialized()) {
                return targetFrame.getTransferData(flavor);
            }
        }
        return null;
    }

    public final void setFrame(JFrame frame) {
        if (targetFrame != null) {
            targetFrame.setFrame(frame);
        }
    }

    public final Runnable shutdownHook() {
        if (targetFrame != null) {
            return targetFrame.shutdownHook();
        }
        return null;
    }

    /**
     * @param args valid options are -antframe [Jframe class binary name] -width
     * [px size] -height [px size] -device [device id]
     */
    public static JFCFrame launchFrame(String[] args) {
        String antframe = null;
        Dimension size = new Dimension(600, 500);
        /** in case the arguments were inline (MacOS bundle) */
        if (args.length == 1) {
            args = args[0].split("\\s");
        }        
        for (int i = 0; i < args.length; i++) {
            if ("-antframe".equals(args[i]) && i + 1 < args.length) {
                antframe = args[i + 1];
                JXAenvUtils._setSysValue(JXAenvUtils.env.APP_FRAME.propertyName(), JXAenvUtils.env.APP_FRAME.propertyValue());
            }
            if ("-width".equals(args[i]) && i + 1 < args.length) {
                size.width = Integer.parseInt(args[i + 1]);
            }
            if ("-height".equals(args[i]) && i + 1 < args.length) {
                size.height = Integer.parseInt(args[i + 1]);
            }
            if ("-device".equals(args[i]) && i + 1 < args.length) {
                int dev = Integer.parseInt(args[i + 1]);
                GraphicsDevice[] devs = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
                JXAenvUtils._defaultGC = devs.length > dev ? devs[dev].getDefaultConfiguration() : devs[0].getDefaultConfiguration();
                if (devs.length < dev) {
                    JXAenvUtils._popExceptionToUser(true, Thread.currentThread(), new IllegalArgumentException("-device argument value is invalid " + devs.length + " screens were detected"));
                }
            }
        }


        assert antframe != null : "Please set AntFrame implementation -antframe package.antframeimplementation !";
        final JFCFrame frame = new JFCFrame(antframe, args);
        DisplayMode dm = JXAenvUtils._defaultGC.getDevice().getDisplayMode();
        frame.setLocation((int) Math.round((double) (dm.getWidth() - size.width) / 2.), (int) Math.round((double) (dm.getHeight() - size.height) / 2.));
        frame.setPreferredSize(size);
        frame.setSize(size);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.initComponents();
        frame.pack();
        frame.setVisible(true);
        frame.start();
        return frame;
    }

    /**
     * @param args at least "-antframe package.theantframe" must be specified
     * optional is : -device screenDeviceIndex (0 to number of available screens
     * - 1)
     */
    public static void main(final String[] args) {
        ThreadWorks.Swing.invokeLater(new Runnable() {
            public void run() {
                launchFrame(args);
            }
        });
    }
    String[] args = null;

    public DisplayInterface getSplash() {
        if (targetFrame != null) {
            return targetFrame.getSplash();
        } else {
            try {
                return Display._Display(ExtensionsInstaller.splashPic, null);
            } catch (Exception ex) {
                if (JXAenvUtils._debugSys) {
                    ex.printStackTrace();
                }
                return null;
            }
        }
    }

    public List<Action> getLoadLayers() {
        if (targetFrame != null) {
            return targetFrame.getLoadLayers();
        } else {
            return null;
        }
    }
    
}
