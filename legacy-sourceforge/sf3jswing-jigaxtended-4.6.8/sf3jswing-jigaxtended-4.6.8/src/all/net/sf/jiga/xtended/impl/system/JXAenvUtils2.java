/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.system;

import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;
import net.sf.jiga.xtended.impl.Sprite;
import static net.sf.jiga.xtended.impl.Sprite._printAllMimeTypes;
import net.sf.jiga.xtended.impl.game.RenderingScene;
import net.sf.jiga.xtended.impl.game.gl.MemScratch;
import net.sf.jiga.xtended.kernel.ExtensionsClassLoader;
import net.sf.jiga.xtended.kernel.FileHelper;import net.sf.jiga.xtended.kernel.JXAenvUtils;
import net.sf.jiga.xtended.ui.MemStatusToolkit;
import net.sf.jiga.xtended.ui.UIMessage;
import net.sf.jiga.xtended.ui.UIMessage.SwingStaticReturn;

/**
 *
 * @author www.b23prodtm.info
 */
public class JXAenvUtils2 extends JXAenvUtils {

    static MemStatusToolkit memStat = null;
    static{    
            System.out.println(log("JigaXtended API v. " + _jxaVersion(), LVL.SYS_NOT));
    }

    /** JProgressBar instances that updates status of the memory usage
    @return MemStatusToolkit instance*/
    public static MemStatusToolkit _getMemoryStatusTK() {
        if (memStat == null) {
            memStat = new MemStatusToolkit();
        }
        return memStat;
    }

    /**
    @see RenderingScene#_getGraphicsRendererInfo(GraphicsConfiguration)*/
    public static Map<String, String> _graphicsInfo() {
        return AccessController.doPrivileged(new PrivilegedAction<Map<String, String>>()   {

            @Override
            public Map<String, String> run() {
                return RenderingScene._getGraphicsRendererInfo(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration(), MemScratch._isInitialized());
            }
        }, acc);
    }

    public static String _jxaVersion() {
        return Sprite.class.getPackage().getImplementationVersion();
    }

    /** to make use of the included Java Media Framework, you must first install the registry in the classpath and if you want to add native support (hardware devices) ensure that the 
     * correct shell environment variables were set on launch :
     * <ul>
     *  <li>for windows systems it is : PATH that must include the library path where jmf dll's are found (jxa usually installs into the %HOMEPATH% folder)</li>
     *  <li>for linux systems it is : LD_LIBRARY_PATH that must include the library path where jmf so's are found (jxa usually installs into the /home/$USER folder)</li>
     * <li>for mac systems there's no native support with JMF</li>
     * </ul>
     * @param cpDir must be a writable directory and found in the classPath
    @return true if the registry file's been successfully found and installed*/
    public static boolean _installJMFRegistry(File cpDir) {
        JXAenvUtils env = new JXAenvUtils(ExtensionsClassLoader.getInstance().getClassLoader());
        env.addEnvFile("jmfRegistry", env.getEnvClassLoader().getResource("jmf.properties"));
        env.setJXAenvPath(cpDir.getPath());
        env.loadResource();
        return env.isResourceLoaded();
    }

    /** allows a Swing context to be run with a returned value.
    it checks if the {@linkplain SwingUtilities#invokeAndWait(Runnable)} can be run (if not,
     * it will run on the current EDT anyway) and returns the value after executing the
     * {@linkplain SwingStaticReturn#run()} method
         * @param <T> the type of returned object or Void if none
         * @param r the object return instance
         * @throws java.lang.InterruptedException (thread was interrupted)
         * @throws java.lang.reflect.InvocationTargetException another exception occured (need more details on cause)
         */
    public static <T> T invokeSwingAndReturn(final SwingStaticReturn<T> r) throws InterruptedException, InvocationTargetException {
        return UIMessage.invokeSwingAndReturn(r);
    }

    /***/
    public static void _printAllMimes() {
        _printAllMimeTypes();
    }

    /**
    @see Sprite#_queueRef(Object, boolean)*/
    public static <T> T queueRef(T o, boolean enqueueNow) {
        return RefPool.getInstance().queueRef(o, enqueueNow);
    }

    /** cleans up the referenceQueue
    @see #_queueRef(Object, boolean)*/
    public void cleanup() {
        ImagePool.getInstance().cleanup();
    }

    /** creates a new ToolTipTableCellRenderer. This CellRenderer displays a tool-tip whenever the user flies over the cell and it shows
     * the whole content in this popup (very useful for a long file path or URL that hides in a table cell).
    @return a TableCellRenderer instance of ToolTopTableCellRenderer and implements a large-text-manager tool-tip
     */
    public static TableCellRenderer _getNewToolTipTableCellRenderer() {
        return new ToolTipTableCellRenderer();
    }
    static class ToolTipTableCellRenderer extends JLabel implements TableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String s = value.toString();
            int off = 0;
            while (s.length() > 30 && off + 30 < s.length()) {
                s = s.substring(0, off + 30) + "<br>" + s.substring(off + 30);
                off += 30 + 4;
            }
            s = "<html>" + s + "</html>";
            setToolTipText(s);
            setText(value.toString());
            return this;
        }
    }

    /** *   @param bigbuffer for a faster IO operation
     * @return buffered input
    @see JXAenvUtils#_BIGBUFFER_SIZE
    @see JXAenvUtils#_SMALLBUFFER_SIZE*/
    public static BufferedInputStream __getBuffered(InputStream s, boolean bigbuffer) {
        return new BufferedInputStream(s, bigbuffer ? FileHelper._BIGBUFFER_SIZE : FileHelper._SMALLBUFFFER_SIZE);
    }

    /** *  @param bigbuffer for a faster IO operation
     * @return buffered output
    @see JXAenvUtils#_BIGBUFFER_SIZE
    @see JXAenvUtils#_SMALLBUFFER_SIZE*/
    public static BufferedOutputStream __getBuffered(OutputStream s, boolean bigbuffer) {
        return new BufferedOutputStream(s, bigbuffer ? FileHelper._BIGBUFFER_SIZE : FileHelper._SMALLBUFFFER_SIZE);
    }
}
