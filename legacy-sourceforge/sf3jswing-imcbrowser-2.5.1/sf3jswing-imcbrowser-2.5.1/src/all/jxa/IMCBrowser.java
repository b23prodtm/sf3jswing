package jxa;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import cz.dhl.ftp.Ftp;
import cz.dhl.ftp.FtpFile;
import cz.dhl.ftp.FtpOutputStream;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ByteLookupTable;
import java.awt.image.LookupOp;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.renderable.ParameterBlock;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.event.IIOReadProgressListener;
import javax.imageio.event.IIOWriteProgressListener;
import javax.imageio.stream.ImageInputStream;
import javax.media.CaptureDeviceInfo;
import javax.media.CaptureDeviceManager;
import javax.media.Manager;
import javax.media.control.FormatControl;
import javax.media.control.FrameGrabbingControl;
import javax.media.format.VideoFormat;
import org.eclipse.imagen.ImageN;
import org.eclipse.imagen.RenderedOp;
import javax.media.util.BufferToImage;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.RootPaneContainer;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.MouseInputAdapter;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableCellRenderer;
import net.sf.jiga.xtended.JXAException;
import net.sf.jiga.xtended.impl.IIOReadProgressAdapter;
import net.sf.jiga.xtended.impl.IIOWriteProgressAdapter;
import net.sf.jiga.xtended.impl.Sprite;
import net.sf.jiga.xtended.impl.SpriteIO;
import net.sf.jiga.xtended.impl.game.GameActionLayer;
import net.sf.jiga.xtended.impl.game.RenderingScene;
import net.sf.jiga.xtended.impl.game.RenderingSceneListener;
import net.sf.jiga.xtended.impl.game.gl.GLFX;
import net.sf.jiga.xtended.impl.game.gl.GLHandler;
import net.sf.jiga.xtended.impl.game.gl.MemScratch;
import net.sf.jiga.xtended.impl.game.gl.MemScratch.Item;
import net.sf.jiga.xtended.impl.game.gl.RenderingSceneGL;
import net.sf.jiga.xtended.impl.game.gl.Sf3Texture;
import net.sf.jiga.xtended.impl.game.gl.Sf3TextureTile;
import net.sf.jiga.xtended.impl.game.gl.SpriteGLHandler;
import net.sf.jiga.xtended.impl.game.gl.geom.GLGeom;
import net.sf.jiga.xtended.impl.game.scene.State;
import net.sf.jiga.xtended.impl.system.BufferIO;
import net.sf.jiga.xtended.impl.system.JXAenvUtils2;
import net.sf.jiga.xtended.impl.system.SfMediaTracker;
import net.sf.jiga.xtended.kernel.FileHelper;
import static net.sf.jiga.xtended.kernel.FileHelper._BIGBUFFER_SIZE;
import static net.sf.jiga.xtended.kernel.FileHelper._TMPDIRECTORY;
import static net.sf.jiga.xtended.kernel.FileHelper._USERHOMEDIRECTORY;
import static net.sf.jiga.xtended.kernel.FileHelper._tmpFilesSuffix;
import net.sf.jiga.xtended.kernel.JXAenvUtils;
import static net.sf.jiga.xtended.kernel.JXAenvUtils.*;
import net.sf.jiga.xtended.kernel.JXAenvUtils.LVL;
import net.sf.jiga.xtended.kernel.JXAenvUtils.env;
import net.sf.jiga.xtended.kernel.SpritesCacheAdapter;
import net.sf.jiga.xtended.kernel.SpritesCacheListener;
import net.sf.jiga.xtended.kernel.ThreadWorks;
import net.sf.jiga.xtended.ui.Display;
import net.sf.jiga.xtended.ui.DisplayInterface;
import net.sf.jiga.xtended.ui.ExampleFileFilter;
import net.sf.jiga.xtended.ui.FileChooserPane;
import net.sf.jiga.xtended.ui.HyperlinkAdapter;
import net.sf.jiga.xtended.ui.ImageCollection;
import net.sf.jiga.xtended.ui.ImageCollectionRenderer;
import net.sf.jiga.xtended.ui.ImagePluginDescriptor;
import net.sf.jiga.xtended.ui.ImagePluginFilter;
import net.sf.jiga.xtended.ui.InstructionsPane;
import net.sf.jiga.xtended.ui.AntApplet;
import net.sf.jiga.xtended.ui.AntFrame;
import net.sf.jiga.xtended.ui.JFCApplet;
import net.sf.jiga.xtended.ui.JFCFrame;
import net.sf.jiga.xtended.ui.JXALabel;
import net.sf.jiga.xtended.ui.Led;
import net.sf.jiga.xtended.ui.LogArea;
import net.sf.jiga.xtended.ui.MemStatusToolkit;
import net.sf.jiga.xtended.ui.Navigator;
import net.sf.jiga.xtended.ui.StepListener;
import net.sf.jiga.xtended.ui.UIMessage;
import net.sf.jiga.xtended.ui.UIMessage.SwingStaticReturn;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.JXTitledPanel;
import org.jdesktop.swingx.auth.PasswordStore;
import org.jdesktop.swingx.auth.UserNameStore;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.Util;

/**
 *
 * @author www.b23prodtm.info
 */
public class IMCBrowser implements AntFrame, AntApplet, ClipboardOwner {

    private final static String COLLECTION_MIME_TILED = "image/tiff";
    private final static String COLLECTION_MIME_UNTILED = COLLECTION_MIME_TILED;
    private final static int SPRITE_TYPE_NO_ALPHA = ImageCollection.BUFFERED_TYPE.TYPE_3BYTE_BGR.type;
    private final static int SPRITE_TYPE_ALPHA = ImageCollection.BUFFERED_TYPE.TYPE_4BYTE_ABGR.type;
    public ImageCollection imageCollection;
    public SortedMap<String, Map<Integer, Serializable>> imageCollectionInfo = Collections.synchronizedSortedMap(new TreeMap<String, Map<Integer, Serializable>>());
    static SortedMap<String, ImageCollection> imageCollectionDB;

    static {
        SortedMap<String, ImageCollection> dbCache = new TreeMap<String, ImageCollection>();
        imageCollectionDB = Collections.synchronizedSortedMap(dbCache);
    }
    static SortedMap<String, SortedMap<String, Map<Integer, Serializable>>> imageCollectionInfoDB = Collections.synchronizedSortedMap(new TreeMap<String, SortedMap<String, Map<Integer, Serializable>>>());
    public final static int INFO_PROP = 0;
    public final static int INFO_DIMENSION = 1;
    public final static int INFO_ZOOM = 2;
    public final static int INFO_TIMESTAMP = 3;
    public final static int INFO_CONTRAST = 4;
    public final static int INFO_BRIGHTNESS = 5;
    public final static int INFO_CROPRECT = 6;
    private SortedMap<String, ImageCollection> imageHistoryCollection;
    /*Dimension originalSize;*/
    int gridCols = 3;
    public JPanel picView;
    private JXTaskPaneContainer jxLeft;
    JXTaskPane tasks_resize = new JXTaskPane();
    JXTaskPane tasks_imageParams = new JXTaskPane();
    JXTaskPane tasks_fx = new JXTaskPane();
    JXTaskPane tasks_db = new JXTaskPane();
    JXTaskPane tasks_ftp = new JXTaskPane();

    private Rectangle selectionRect = new Rectangle();
    public FileChooserPane fcp;
    private JLabel statusLabel;
    private /*JProgressBar */ JLabel statusDiskUsageLabel;
    private String statusLabelDefaultText;
    JXTitledPanel fileInfo;
    LogArea log;
    public Set<String> selectedKeys = Collections.synchronizedSet(new HashSet<String>());
    private AbstractAction action_new;
    private AbstractAction action_open;
    private AbstractAction action_save;
    private AbstractAction action_saveAs;
    private AbstractAction action_revertToOriginal;
    private AbstractAction action_quit;
    private AbstractAction action_restart;
    private AbstractAction action_refresh;
    private AbstractAction action_add;
    private AbstractAction action_remove;
    private AbstractAction action_import;
    private AbstractAction action_export;
    /**
     * resize Panel
     */
    JPanel resizePanel;
    JSpinner resize_scale,
            resize_width,
            resize_height;
    JCheckBox resize_widthCB,
            resize_heightCB,
            resize_canvasCB;
    private Led wLed, rLed;
    long timeRef = System.currentTimeMillis();
    ExampleFileFilter allFF = new ExampleFileFilter();
    private AbstractAction action_share;
    private AbstractAction action_undo;
    private AbstractAction action_selectAll;
    private AbstractAction action_undoAll;
    private JPanel dbPanel;
    public JComboBox dbList;
    private AbstractAction action_sendFTP;
    private AbstractAction action_fxRotateCCW;
    private AbstractAction action_fxRotateCW;
    private AbstractAction action_fxFlipV;
    private AbstractAction action_fxFlipH;
    private AbstractAction action_removeCollection;
    private AbstractAction action_resize;
    private AbstractAction action_opengl;
    private AbstractAction action_search;
    private AbstractAction action_help;
    private AbstractAction action_about;
    private AbstractAction action_JMFload;
    private AbstractAction action_seeInHdd;
    private AbstractAction action_copy_make;
    private AbstractAction action_copy_toClipboard;
    private AbstractAction action_copy_fromClipboard;
    private AbstractAction action_previewCrop;
    private AbstractAction action_zoom;
    private AbstractAction action_print;
    private AbstractAction action_printFormat;
    private AbstractAction action_lutApply;
    private JSlider previewContrast;
    private JSlider previewBrightness;
    private AbstractAction previewCDF;
    private JComboBox previewZoom;
    private SaveAccessory saveAccess;
    /**
     * splash screen system property (true|false)
     */
    public final static String ENVSPLASH = "jxa.imc.splash";

    /**
     * a relative path (or URL) to the splash picture (png|jpg)
     */
    public final static String ENVSPLASHURL = "jxa.imc.splash.url";

    private Set<ImagePluginFilter> updateFileFilters(final FileChooserPane fcp, final String fileMode) {
        try {
            return JXAenvUtils2.invokeSwingAndReturn(new SwingStaticReturn<Set<ImagePluginFilter>>() {
                public Set<ImagePluginFilter> run() {
                    return ImagePluginFilter._updateFileFilters(fcp, fileMode);
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            return new HashSet<ImagePluginFilter>();
        }
    }
    private static boolean JMFinstalled = false;

    private boolean detectJMFDevices() {
        boolean b = false;
        if (JOptionPane.YES_OPTION == UIMessage.showConfirmDialog(picView, new JLabel(getLocalizedStringCap("JMFloadConfirm")), getLocalizedStringCap("JMFloadConfirmTitle"), JOptionPane.YES_NO_OPTION)) {
            log.setLogStdoutEnabled(true);
            if (b = JXAenvUtils2._installJMFRegistry(_USERHOMEDIRECTORY)) {
                System.out.println(log("successfully installed JMF registry file", LVL.APP_NOT));
            } else {
                System.out.println(log("failed to install JMF registry file", LVL.APP_WRN));
            }
            if (env.OS_LINUX.isEnv()) {
                try {
                    Class cl = Class.forName("V4LAuto");
                    cl.newInstance();
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            }
            if (env.OS_WINDOWS.isEnv()) {
                try {
                    Class cl = Class.forName("VFWAuto");
                    cl.newInstance();
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            }
            log.setLogStdoutEnabled(false);
            new UIMessage(true, new JLabel(getLocalizedStringCap("JMFneedreboot")), null);
            reload();
        }
        return b;
    }

    public Container mainFrame;

    public IMCBrowser(String[] args) throws IOException, URISyntaxException, ClassNotFoundException {
        super(args);
        if (args != null) {
            for (String a : args) {
                if (a.equalsIgnoreCase("-vram")) {
                    /**
                     * check performance, may be unstable
                     */
                    hardwareAccel = true;
                } else if (a.equalsIgnoreCase("-applet")) {
                    applet = true;
                }/* else if(a.equalsIgnoreCase("-nodbuf")) {
                 RepaintManager.currentManager(new JLabel()).setDoubleBufferingEnabled(false);
                 }*/

            }
        }
        SortedMap<String, ImageCollection> archiveCache = new TreeMap<String, ImageCollection>();
        imageHistoryCollection = Collections.synchronizedSortedMap(archiveCache);
    }
    private boolean reloading = false;

    private void reload() {
        if (reloading) {
            return;
        }
        Runnable r = new Runnable() {
            public void run() {
                if (mainFrame instanceof JFrame) {
                    JFCFrame._restart(IMCBrowser.this);
                    reloading = false;
                } else if (mainFrame instanceof JApplet) {
                    JFCApplet._restart(IMCBrowser.this);
                }
            }
        };
        reloading = true;
        ThreadWorks.Swing.invokeLater(r);
    }

    public void edit_started(long progressBar, String opName) {

        edit_started(progressBar, opName, selectedKeys.size());
    }

    public void edit_started(long progressBar, String opName, int amountOfPictures) {
        canvas.pauseRendering();
        _debug = _debugops;
        UIMessage.getProgressBar(progressBar).setString(opName + "...");
        UIMessage.getProgressBar(progressBar).setStringPainted(true);
        UIMessage.getProgressBar(progressBar).setIndeterminate(false);
        UIMessage.getProgressBar(progressBar).setValue(0);
        UIMessage.getProgressBar(progressBar).setMaximum(amountOfPictures);
        UIMessage.showProgress(progressBar);
        updateCurrentStatus(null, opName + " " + amountOfPictures + " " + LocalizedResources.localized.getString("photos") + "...");
        log.pushNewLog(":: " + opName + " :: " + LocalizedResources.localized.getString("started") + " " + amountOfPictures + " " + LocalizedResources.localized.getString("photos") + "...");
        synchronized (actions_selection) {
            for (Action a : actions_selection) {
                if (a != null) {
                    a.setEnabled(false);
                }
            }
        }
        setEnabledAllChildren(picView, false);
        setEnabledAllChildren(resizePanel, false);
        setEnabledAllChildren(dbPanel, false);
    }

    private void setEnabledAllChildren(Container container, boolean enable) {
        container.setEnabled(enable);
        for (Component c : container.getComponents()) {
            c.setEnabled(enable);
        }
    }

    private void edit_savedState(boolean saved, String key) {
        ((Properties) imageCollectionInfo.get(key).get(INFO_PROP)).setProperty("saved", "" + saved);
    }

    public void edit_progress(long progressBar, int increment) {
        edit_progress(progressBar, increment, selectedKeys.size());
    }

    public void edit_progress(long progressBar, int increment, int amountOfPictures) {
        int val = UIMessage.getProgressBar(progressBar).getValue();
        UIMessage.getProgressBar(progressBar).setIndeterminate(false);
        UIMessage.updateProgress(progressBar, val + increment, amountOfPictures);
        updateCurrentStatus(null, (amountOfPictures - (val + increment)) + " " + LocalizedResources.localized.getString("photos") + " " + LocalizedResources.localized.getString("remaining") + "...");
        log.pushNewLog(":: " + LocalizedResources.localized.getString("progress") + " " + (amountOfPictures - (val + increment)) + " " + LocalizedResources.localized.getString("photos") + " " + LocalizedResources.localized.getString("remaining") + "...");
    }

    public void edit_finished(long progressBar, String opName, boolean errored) {
        edit_finished(progressBar, opName, errored, selectedKeys.size());
    }

    public void edit_finished(long progressBar, String opName, boolean errored, int amountOfPictures) {
        if (!errored) {
            UIMessage.getProgressBar(progressBar).setString(getLocalizedStringCap("completed"));
            UIMessage.getProgressBar(progressBar).repaint();
            updateCurrentStatus(null, opName + " " + amountOfPictures + " " + LocalizedResources.localized.getString("photos") + " " + LocalizedResources.localized.getString("isdone."));
            log.pushNewLog(":: " + opName + " :: " + LocalizedResources.localized.getString("finishedsuccessfully") + " " + amountOfPictures + " " + LocalizedResources.localized.getString("photos") + ".");
        } else {
            UIMessage.getProgressBar(progressBar).setString(getLocalizedStringCap("interrupted"));
            UIMessage.getProgressBar(progressBar).repaint();
            updateCurrentStatus(null, LocalizedResources.localized.getString("The") + " " + opName + " " + LocalizedResources.localized.getString("operationhasbeeninterrupted."));
            log.pushNewLog("!! " + opName + " !! " + LocalizedResources.localized.getString("erroredorinterrupted."));
        }
        UIMessage.getProgressBar(progressBar).setIndeterminate(false);
        Runnable r = new Runnable() {
            public void run() {
                /*final long wait = UIMessage.displayWaiting(localized.getString("pleasewait!"), browser);*/
                JDialog wait = UIMessage.showLightPopupMessage(new JLabel(getLocalizedStringCap("pleasewait!")), null, picView, UIMessage.UI_CENTER);
                updateCurrentStatus(null, getLocalizedStringCap("loading") + "...");
                try {
                    imageCollection.waitForAllThumbnails();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                } finally {
                    updateCurrentStatus(null, statusLabelDefaultText);
                    synchronized (actions_selection) {
                        for (Action a : actions_selection) {
                            if (a != null) {
                                a.setEnabled(true);
                            }
                        }
                    }
                    setEnabledAllChildren(picView, true);
                    setEnabledAllChildren(resizePanel, true);
                    setEnabledAllChildren(dbPanel, true);
                    /*UIMessage.kill(wait);*/
                    wait.setVisible(false);
                    callBrowserUpdate();
                }
            }
        };
        canvas.doTaskOnScreen(r);
    }

    SfMediaTracker mt = SpriteIO._mt;

    public void edit_buffer(Object src) throws InterruptedException {
        imageCollection.accessCache(false).buffer(src);
        if (src instanceof Image) {
            mt.addImage((Image) src, 0);
            mt.waitForID(0);
        }
    }

    private void edit_clearBuffer(Object src) {
        if (src instanceof Image) {
            mt.removeImage((Image) src, 0);
            ((Image) src).flush();
        }
        if (src instanceof RenderedOp) {
            ((RenderedOp) src).dispose();
        }
    }

    private void edit_clearCanvas(Sprite canvas) throws InterruptedException {
        boolean doTiled = canvas.isTilesAvailable();
        canvas.setTileModeEnabled(doTiled);
        canvas.runValidate();
        canvas.clearImageGraphics();
    }

    private void edit_copyImageOnCanvas(Object src, Sprite canvas) throws InterruptedException, OutOfMemoryError {
        try {
            imageCollection.accessCache(false).memorySensitiveCallback("__edit_copyImageOnCanvas", this, new Object[]{src, canvas}, new Class[]{Object.class, Sprite.class});
        } catch (Throwable ex) {
            if (ex instanceof InterruptedException) {
                throw (InterruptedException) ex;
            } else if (ex instanceof OutOfMemoryError) {
                throw (OutOfMemoryError) ex;
            } else {
                ex.printStackTrace();
            }
        }
    }

    public void __edit_copyImageOnCanvas(Object src, Sprite canvas) throws InterruptedException {
        boolean doTiled = canvas.isTilesAvailable();
        canvas.setTileModeEnabled(doTiled);
        canvas.runValidate();
        Rectangle canvasBounds = canvas.getBounds();
        edit_buffer(src);
        Rectangle imageBounds = new Rectangle();
        if (src instanceof RenderedImage) {
            RenderedImage image = (RenderedImage) src;
            imageBounds = new Rectangle(image.getWidth(), image.getHeight());
        } else if (src instanceof Image) {
            Image image = (Image) src;
            imageBounds = new Rectangle(image.getWidth(picView), image.getHeight(picView));
        } else if (src instanceof Sprite) {
            Sprite image = (Sprite) src;
            imageBounds = image.getBounds(imageBounds);
        }
        /**
         * a centered image will be copied on "canvas"
         */
        imageBounds.setLocation((int) Math.round(canvasBounds.getCenterX() - imageBounds.getWidth() / 2.0), (int) Math.round(canvasBounds.getCenterY() - imageBounds.getHeight() / 2.0));
        if (src instanceof RenderedImage) {
            RenderedImage image = (RenderedImage) src;
            if (doTiled) {
                Rectangle tileRect = imageBounds.intersection(canvasBounds);
                /**
                 * apply buffer to output
                 */
                try {
                    ImageWriter w = canvas.prepareTileWrite(false);
                    ImageWriteParam iwp = canvas.getTileWriterParams(w);
                    if (w.canReplacePixels(0)) {
                        if (!tileRect.isEmpty()) {
                            /**
                             * tile rect location is relative to canvasbounds
                             */
                            iwp.setDestinationOffset(tileRect.getLocation());
                            w.prepareReplacePixels(0, tileRect);
                            if (image instanceof BufferedImage ? ((BufferedImage) image).getType() != canvas._type : true) {
                                image = canvas.convertToBuffered(image, canvas.getObs(), false);
                            }
                            iwp.setSourceRegion(new Rectangle(tileRect.getSize()));
                            w.replacePixels(image.getData(), iwp);
                            w.endReplacePixels();
                        }
                    } else {
                        doTiled = false;
                    }
                    canvas.endTileWrite(w);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    doTiled = false;
                }
            }
        }
        if (src instanceof Sprite) {
            if (doTiled) {
                Sprite srcSp = (Sprite) src;
                ImageReader r = srcSp.getTileReader(false);
                Dimension numTiles = new Dimension(srcSp.getNumTileX(), srcSp.getNumTileY());
                try {
                    Dimension tiles = new Dimension(SpriteIO._WRITE_TILES_DIMENSION);
                    ImageWriter w = canvas.prepareTileWrite(false);
                    ImageWriteParam iwp = canvas.getTileWriterParams(w);
                    if (w.canReplacePixels(0)) {
                        /*NEVER ! w.prepareReplacePixels(0, imageBounds.intersection(canvasBounds));*/
                        for (int x = 0; x < numTiles.width; x++) {
                            for (int y = 0; y < numTiles.height; y++) {
                                Rectangle tileBounds = new Rectangle(x * tiles.width, y * tiles.height, tiles.width, tiles.height).intersection(new Rectangle(r.getWidth(0), r.getHeight(0)));
                                ImageReadParam irp = srcSp.getTileReaderParams(r);
                                irp.setSourceRegion(tileBounds);
                                if (tileBounds.isEmpty()) {
                                    continue;
                                }
                                BufferedImage t = r.read(0, irp);
                                if (t.getType() != canvas._type) {
                                    t = canvas.convertToBuffered((RenderedImage) t, picView, true);
                                }
                                tileBounds.setLocation(tileBounds.x + imageBounds.x, tileBounds.y + imageBounds.y);
                                /**
                                 * read src tile into buffer
                                 */
                                edit_buffer(t);
                                Rectangle tileRect = tileBounds.intersection(canvasBounds);
                                /**
                                 * apply buffer to output
                                 */
                                if (!tileRect.isEmpty()) {
                                    /**
                                     * tile rect location is relative to
                                     * canvasbounds
                                     */
                                    iwp.setDestinationOffset(tileRect.getLocation());
                                    iwp.setSourceRegion(new Rectangle(tileRect.getSize()));
                                    w.prepareReplacePixels(0, tileRect);
                                    w.replacePixels(t.getData(), iwp);
                                    w.endReplacePixels();
                                }
                                edit_clearBuffer(t);
                            }
                        }
                        /*w.endReplacePixels();*/
                    } else {
                        doTiled = false;
                    }
                    ((ImageInputStream) r.getInput()).flush();
                    r.dispose();
                    ((ImageInputStream) r.getInput()).close();
                    canvas.endTileWrite(w);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    doTiled = false;
                }
            }
        }
        if (src instanceof Image || src instanceof RenderedImage || src instanceof Sprite) {
            if (!doTiled) {
                canvas.setTileModeEnabled(doTiled);
                canvas.runValidate();
                Image canvasCopy = canvas.getImage(picView);
                Graphics2D g = Sprite.wrapRendering(canvasCopy.getGraphics());
                g.translate(imageBounds.x, imageBounds.y);
                if (src instanceof RenderedImage) {
                    g.drawRenderedImage((RenderedImage) src, new AffineTransform());
                } else if (src instanceof Image) {
                    g.drawImage((Image) src, new AffineTransform(), picView);
                } else if (src instanceof Sprite) {
                    Sprite s = (Sprite) src;
                    Point loc = s.getLocation();
                    s.setLocation(0, 0);
                    s.runValidate();
                    s.draw(picView, g);
                    s.setLocation(loc);
                }
                g.dispose();
            }
        } else if (src != null) {
            throw new InterruptedException("no Such src class support : " + src.getClass().getCanonicalName());
        }
        edit_clearBuffer(src);
    }

    private void updateImage(ImageCollection dst, String imageKey, Sprite sp) throws IOException {
        Map<String, Serializable> tbstore = getAttributesSet(dst, imageKey, true, ImageCollection.ATTRIBUTE_MIME, ImageCollection.ATTRIBUTE_TYPE);
        Map<String, Serializable> store = getAttributesSet(dst, imageKey, false, ImageCollection.ATTRIBUTE_MIME, ImageCollection.ATTRIBUTE_TYPE);
        updateAddImage(dst, imageKey, sp, tbstore, store);
    }

    private void updateAddImage(ImageCollection dst, String imageKey, Sprite sp, Map<String, Serializable> tbattrs, Map<String, Serializable> attrs) throws IOException {
        try {
            dst.beginEdit(imageKey, true);
            File tmp = FileHelper._createTempFile("imc_", _TMPDIRECTORY, true);
            sp.runValidate();
            sp.writeImageToOutput(tmp, -1f);
            updateAddImage(dst, imageKey, tmp, sp.getStoreMime(), tbattrs, attrs);
            dst.endEdit(imageKey, true);
        } catch (InterruptedException ex) {
            if (_debugops) {
                ex.printStackTrace();
            }
        }
    }

    private void updateImage(ImageCollection dst, String imageKey, File src, String mime) {
        Map<String, Serializable> tbstore = getAttributesSet(dst, imageKey, true, ImageCollection.ATTRIBUTE_MIME, ImageCollection.ATTRIBUTE_TYPE);
        Map<String, Serializable> store = getAttributesSet(dst, imageKey, false, ImageCollection.ATTRIBUTE_MIME, ImageCollection.ATTRIBUTE_TYPE);
        updateAddImage(dst, imageKey, src, mime, tbstore, store);
    }

    private void updateAddImage(ImageCollection dst, String imageKey, File src, String mime, Map<String, Serializable> tbattrs, Map<String, Serializable> attrs) {
        System.out.println("update image " + imageKey + " and register new thumbnail attributes " + Arrays.toString(tbattrs.keySet().toArray()) + " new attributes " + Arrays.toString(attrs.keySet().toArray()));
        dst.addImage(src, imageKey, mime);
    }

    private Sprite edit_newDrawingCanvas(final String selection) throws InterruptedException, IOException {
        long timeRef = System.currentTimeMillis() - IMCBrowser.this.timeRef;
        Sprite sp = imageCollection.getCollectionSprite(selection, false);
        ImageCollection icHistory;
        if (!imageHistoryCollection.containsKey(selection)) {
            imageHistoryCollection.put(selection, icHistory = new ImageCollection(picView, 1, _COMPRESS, hardwareAccel));
        } else {
            icHistory = imageHistoryCollection.get(selection);
        }
        Map<String, Serializable> tbstore = getAttributesSet(imageCollection, selection, true, ImageCollection.ATTRIBUTE_MIME, ImageCollection.ATTRIBUTE_TYPE);
        Map<String, Serializable> store = getAttributesSet(imageCollection, selection, false, ImageCollection.ATTRIBUTE_MIME, ImageCollection.ATTRIBUTE_TYPE);
        updateAddImage(icHistory, "" + timeRef, sp, tbstore, store);
        edit_savedState(false, selection);
        return sp;
    }

    /**
     * Defines the default storage compression quality for
     * Serialized/Externalized instances, if compression is available for the
     * current {@link #mime mime-type}<BR>
     * decimal between 0f and 1f. Use a negative value (-1f) to copy from the
     * {@linkplain ImageWriteParam#MODE_COPY_FROM_METADATA actual metadata}
     * found in the original picture.
     *
     * @default .8f = 80 percent compression of the original picture quality
     */
    public static float _DEF_CPR_QUALITY = .8f;

    private void edit_saveCanvas(Sprite edit, String selection) throws JXAException, IOException, InterruptedException {
        updateImage(imageCollection, selection, edit);
        imageCollectionInfo.get(selection).put(INFO_DIMENSION, edit.getSize());
    }

    /**
     * @param waitForPic set always TRUE, for synch'ing operations.
     */
    private void edit_finalize(String selection, boolean waitForPic) throws InterruptedException {
        assert waitForPic : "set always waitforPic TRUE after edit, for synch'ing operations.";
        if (waitForPic) {
            imageCollection.waitForPicture(selection);
        }
        if (imageHistoryCollection.containsKey(selection)) {
            ImageCollection archive = imageHistoryCollection.get(selection);
            archive.accessCache(false).clear();
        }
    }
    SpritesCacheListener spl = new SpritesCacheAdapter() {
        private void reportDiskUsage() {
            statusDiskUsageLabel./*setString*/setText(String.format(getLocalizedStringCap("diskusage") + " : %1$.1f " + LocalizedResources.localized.getString("MB"), (float) (IMCBrowser.this.imageCollection.accessCache(false).getSwapUsage() + IMCBrowser.this.imageCollection.accessCache(true).getSwapUsage()) / (1024f * 1024f)));
            statusDiskUsageLabel.repaint();
        }

        public void writeAborted() {
            super.writeAborted();
            reportDiskUsage();
        }

        public void writeCompleted() {
            super.writeCompleted();
            reportDiskUsage();
        }

        public void writeStarted() {
            super.writeStarted();
            reportDiskUsage();
        }

        public void writeError(Exception arg0) {
            super.writeError(arg0);
            reportDiskUsage();
        }

        public void capacityExtended(int arg0) {
            super.capacityExtended(arg0);
            reportDiskUsage();
        }
    };
    IIOReadProgressListener iior = new IIOReadProgressAdapter(false) {
        @Override
        public void imageProgress(ImageReader source, float percentageDone) {
            super.imageProgress(source, percentageDone);
            /* statusDiskUsageLabel.setMaximum(1000);
             statusDiskUsageLabel.setValue((int) (percentageDone * 10));*/
        }
    };
    IIOWriteProgressListener iiow = new IIOWriteProgressAdapter(false) {
        @Override
        public void imageProgress(ImageWriter source, float percentageDone) {
            super.imageProgress(source, percentageDone);
            /*statusDiskUsageLabel.setMaximum(1000);
             statusDiskUsageLabel.setValue((int) (percentageDone * 10));*/
        }
    };

    private void updateImageCollectionListeners(ImageCollection imageCollection) {
        imageCollection.addIIORListener(rLed.getIIOReadProgressAdapter());
        imageCollection.addIIOWListener(wLed.getIIOWriteProgressAdapter());
        imageCollection.addIIORListener(iior);
        imageCollection.addIIOWListener(iiow);
        imageCollection.addCacheListener(rLed.getSpritesCacheAdapter());
        imageCollection.addCacheListener(wLed.getSpritesCacheAdapter());
        imageCollection.addCacheListener(spl);
    }

    public void setImageCollection(Map<String, Serializable> imageCollection) {
        setImageCollection((ImageCollection) imageCollection.get("imageCollection"), (SortedMap<String, Map<Integer, Serializable>>) imageCollection.get("imageCollectionInfo"));
    }
    /**
     * SETTING cells allocated for image in the HEAP memory (JVM memory)
     */
    private static final int _imageCollectionHeapCells = 1, _imageCollectionHeapCells_thumnails = 10;

    private void setImageCollection(ImageCollection collection, SortedMap<String, Map<Integer, Serializable>> infos) {
        if (collection == null) {
            setImageCollection(newImageCollection());
            return;
        }
        if (!collection.equals(imageCollection) && imageCollection != null) {
            imageCollection.accessCache(false).clear();
            imageCollection.accessCache(true).clear();
        }
        this.imageCollection = collection;
        this.imageCollectionInfo = infos;
        imageCollection.accessCache(false).setListCapacity(_imageCollectionHeapCells);
        imageCollection.accessCache(true).setListCapacity(_imageCollectionHeapCells_thumnails);
    }

    private Map<String, Serializable> newImageCollection() {
        return newImageCollection("album " + imageCollectionDB.size());
    }
    /**
     * imagecollection with compression DEFAULT SET to FALSE
     */
    private static boolean _COMPRESS = false;

    public Map<String, Serializable> newImageCollection(String collectionName) {
        Map<String, Serializable> map = Collections.synchronizedMap(new HashMap<String, Serializable>());
        /**
         * capacity of living heap images is set to a low value corresponding to
         * memory heap usage
         */
        ImageCollection imageCollection = new ImageCollection(picView, _imageCollectionHeapCells, _COMPRESS, hardwareAccel);
        SortedMap<String, Map<Integer, Serializable>> imageCollectionInfo = Collections.synchronizedSortedMap(new TreeMap<String, Map<Integer, Serializable>>());
        updateImageCollectionListeners(imageCollection);
        imageCollectionDB.put(collectionName, imageCollection);
        imageCollectionInfoDB.put(collectionName, imageCollectionInfo);
        map.put("imageCollection", imageCollection);
        map.put("imageCollectionInfo", (Serializable) imageCollectionInfo);
        clearAllSelection();
        return map;
    }

    private void callCollectionDBUpdate() {
        ThreadWorks.Swing.invokeLater(new Runnable() {
            public void run() {
                updateCollectionDB();
            }
        });
    }

    public void updateCollectionDB() {
        String sel = null;
        synchronized (imageCollectionDB) {
            for (String k : imageCollectionDB.keySet()) {
                if (imageCollectionDB.get(k).equals(imageCollection)) {
                    sel = k;
                }
            }
        }
        dbList.removeAllItems();
        synchronized (imageCollectionDB) {
            for (String k : imageCollectionDB.keySet()) {
                dbList.addItem(k);
                System.out.println("dblist key : " + k);
            }
        }
        System.out.println("dblist selection : " + sel);
        if (sel != null) {
            dbList.setSelectedItem(sel);
        }

    }

    private void updateCurrentFileInfo() {
        fileInfo.removeAll();
        if (selectedKeys.size() == 1) {
            String selection = selectedKeys.iterator().next();
            switch (browserMode) {
                case MODE_GRID:
                    fileInfo.add(new JScrollPane(makeFileInfoTable(selection)));
                    break;
                case MODE_NAV:
                    fileInfo.add(imageCollection.getThumbnailComponent(selection));
                    break;
                default:
                    break;
            }
            if (imageCollection.hasPicture(selection, true)) {
                imageCollection.beginEdit(selection, true);
                Map<String, Serializable> attr = imageCollection.getThumbnailPictureStore(selection);
                Dimension dim = (Dimension) attr.get(ImageCollection.ATTRIBUTE_ORIG_DIMENSION);
                resize_width.setValue(dim.width);
                resize_height.setValue(dim.height);
                imageCollection.endEdit(selection, true);
            }
        } else if (selectedKeys.size() > 1) {
            fileInfo.add(new JLabel(selectedKeys.size() + " " + LocalizedResources.localized.getString("photos") + LocalizedResources.localized.getString("areselected")));
        } else {
            fileInfo.add(new JLabel(getLocalizedStringCap("noselection")));
        }
        fileInfo.validate();
        fileInfo.repaint();
        updateMenus();
    }

    private String updateCollectionNameProperty(String key, SortedMap<String, Map<Integer, Serializable>> imageCollectionInfo, ImageCollection imageCollection) {
        String name = "";
        if (imageCollectionInfo.containsKey(key)) {
            Properties infoMap = (Properties) imageCollectionInfo.get(key).get(INFO_PROP);
            synchronized (imageCollectionDB) {
                for (String s : imageCollectionDB.keySet()) {
                    if (imageCollectionDB.get(s).equals(imageCollection)) {
                        infoMap.setProperty("album", name = s);
                        break;
                    }
                }
            }
        }
        return name;
    }

    private JTable makeFileInfoTable(final String key) {
        Vector<String> cols = new Vector<String>();
        cols.add("property");
        cols.add("value");
        Vector<Vector<String>> rows = new Vector<Vector<String>>();
        if (imageCollectionInfo.containsKey(key)) {
            Properties infoMap = (Properties) imageCollectionInfo.get(key).get(INFO_PROP);
            Enumeration<String> e = (Enumeration<String>) infoMap.propertyNames();
            for (; e.hasMoreElements();) {
                Vector<String> propertyRow = new Vector<String>();
                String property = e.nextElement();
                propertyRow.add(property);
                propertyRow.add(infoMap.getProperty(property));
                rows.add(propertyRow);
            }
        }
        final JTable table = new JTable(rows, cols);
        table.setDragEnabled(true);
        TableCellRenderer propertyRenderer = new TableCellRenderer() {
            TableCellRenderer tooltip = JXAenvUtils2._getNewToolTipTableCellRenderer();

            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                String s = value.toString();
                Object localizedv = value;
                if (LocalizedResources.localized.getString(s) != null) {
                    localizedv = LocalizedResources.localized.getString(s);
                }
                return tooltip.getTableCellRendererComponent(table, localizedv, isSelected, hasFocus, row, column);
            }
        };
        table.getColumn("property").setCellRenderer(propertyRenderer);
        table.getColumn("value").setCellRenderer(
                JXAenvUtils2._getNewToolTipTableCellRenderer());
        final JTextField propertyEditor = new JTextField();
        propertyEditor.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Properties properties = (Properties) imageCollectionInfo.get(key).get(INFO_PROP);
                String property = (String) table.getValueAt(table.getSelectedRow(), 0);
                String value = (String) table.getValueAt(table.getSelectedRow(), 1);
                properties.setProperty(property, value);
                callFileInfoUpdate();
            }
        });
        table.getColumn("value").setCellEditor(new DefaultCellEditor(propertyEditor));
        return table;
    }

    private void updateMenus() {
        String[] lstr;
        if (selectedKeys.isEmpty()) {
            action_save.setEnabled(false);
            action_saveAs.setEnabled(false);
            action_revertToOriginal.setEnabled(false);
            action_undo.setEnabled(false);
            action_undoAll.setEnabled(false);
            action_add.setEnabled(false);
            action_remove.setEnabled(false);
            action_sendFTP.setEnabled(false);
            action_share.setEnabled(false);
            action_fxFlipH.setEnabled(false);
            action_fxFlipV.setEnabled(false);
            action_fxRotateCW.setEnabled(false);
            action_fxRotateCCW.setEnabled(false);
            action_resize.setEnabled(false);
            action_seeInHdd.setEnabled(false);
            action_copy_make.setEnabled(false);
            action_copy_toClipboard.setEnabled(false);
            action_print.setEnabled(false);
        } else if (selectedKeys.size() == 1) {
            action_save.setEnabled(true);
            action_save.putValue(Action.NAME, getLocalizedStringCap("save"));
            action_saveAs.setEnabled(true);
            action_saveAs.putValue(Action.NAME, getLocalizedStringCap("saveas") + "...");
            action_revertToOriginal.setEnabled(true);
            action_undo.setEnabled(true);
            action_undo.putValue(Action.NAME, getLocalizedStringCap("undo"));
            action_undoAll.setEnabled(true);
            action_undoAll.putValue(Action.NAME, getLocalizedStringCap("undoall"));
            action_add.setEnabled(true);
            action_add.putValue(Action.NAME, getLocalizedStringCap("addtoalbum") + "...");
            action_remove.setEnabled(true);
            action_remove.putValue(Action.NAME, getLocalizedStringCap("removefromalbum") + "...");
            action_sendFTP.setEnabled(true);
            action_sendFTP.putValue(Action.NAME, getLocalizedStringCap("send"));
            action_share.setEnabled(true);
            action_share.putValue(Action.NAME, getLocalizedStringCap("share") + "...");
            action_fxFlipH.setEnabled(true);
            action_fxFlipH.putValue(Action.NAME, getLocalizedStringCap("fliphorizontally"));
            action_fxFlipV.setEnabled(true);
            action_fxFlipV.putValue(Action.NAME, getLocalizedStringCap("flipvertically"));
            action_fxRotateCCW.setEnabled(true);
            action_fxRotateCCW.putValue(Action.NAME, getLocalizedStringCap("rotate90degreesCCW"));
            action_fxRotateCW.setEnabled(true);
            action_fxRotateCW.putValue(Action.NAME, getLocalizedStringCap("rotate90degreesCW"));
            action_resize.setEnabled(true);
            action_resize.putValue(Action.NAME, getLocalizedStringCap("resize"));
            action_seeInHdd.setEnabled(true);
            action_copy_make.setEnabled(true);
            action_copy_toClipboard.setEnabled(true);
            action_print.setEnabled(true);
            action_print.putValue(Action.NAME, getLocalizedStringCap("print") + "...");
        } else {
            action_save.setEnabled(true);
            action_save.putValue(Action.NAME, getLocalizedStringCap("save") + " (" + selectedKeys.size() + " " + LocalizedResources.localized.getString("pic.") + ")");
            action_saveAs.setEnabled(true);
            action_saveAs.putValue(Action.NAME, getLocalizedStringCap("save") + " (" + selectedKeys.size() + " " + LocalizedResources.localized.getString("pic.") + ") " + LocalizedResources.localized.getString("as") + "...");
            action_revertToOriginal.setEnabled(true);
            action_undo.setEnabled(true);
            action_undo.putValue(Action.NAME, getLocalizedStringCap("undo") + " (" + selectedKeys.size() + " " + LocalizedResources.localized.getString("pic.") + ")");
            action_undoAll.setEnabled(true);
            action_undoAll.putValue(Action.NAME, getLocalizedStringCap("undoall") + " (" + selectedKeys.size() + " " + LocalizedResources.localized.getString("pic.") + ")");
            action_add.setEnabled(true);
            action_add.putValue(Action.NAME, getLocalizedStringCap("add") + " (" + selectedKeys.size() + " " + LocalizedResources.localized.getString("pic.") + ") " + LocalizedResources.localized.getString("toalbum") + "...");
            action_remove.setEnabled(true);
            action_remove.putValue(Action.NAME, getLocalizedStringCap("remove") + " (" + selectedKeys.size() + LocalizedResources.localized.getString("pic.") + ") " + LocalizedResources.localized.getString("fromalbum") + "...");
            action_sendFTP.setEnabled(true);
            action_sendFTP.putValue(Action.NAME, getLocalizedStringCap("send") + " (" + selectedKeys.size() + " " + LocalizedResources.localized.getString("pic.") + ")");
            action_share.setEnabled(true);
            action_share.putValue(Action.NAME, getLocalizedStringCap("share") + " (" + selectedKeys.size() + ")");
            action_fxFlipH.setEnabled(true);
            lstr = LocalizedResources.localizedGetStringArray("action2", 2);
            action_fxFlipH.putValue(Action.NAME, lstr[0] + " (" + selectedKeys.size() + " " + LocalizedResources.localized.getString("pic.") + ") " + lstr[1]);
            action_fxFlipV.setEnabled(true);
            lstr = LocalizedResources.localizedGetStringArray("action3", 2);
            action_fxFlipV.putValue(Action.NAME, lstr[0] + " (" + selectedKeys.size() + " " + LocalizedResources.localized.getString("pic.") + ") " + lstr[1]);
            action_fxRotateCCW.setEnabled(true);
            lstr = LocalizedResources.localizedGetStringArray("action4", 2);
            action_fxRotateCCW.putValue(Action.NAME, lstr[0] + " (" + selectedKeys.size() + " " + LocalizedResources.localized.getString("pic.") + ") " + lstr[1]);
            action_fxRotateCW.setEnabled(true);
            lstr = LocalizedResources.localizedGetStringArray("action5", 2);
            action_fxRotateCW.putValue(Action.NAME, lstr[0] + " (" + selectedKeys.size() + " " + LocalizedResources.localized.getString("pic.") + ") " + lstr[1]);
            action_resize.setEnabled(true);
            action_resize.putValue(Action.NAME, getLocalizedStringCap("resize") + " (" + selectedKeys.size() + " " + LocalizedResources.localized.getString("pic.") + ")");
            action_copy_make.setEnabled(true);
            action_copy_toClipboard.setEnabled(true);
            action_seeInHdd.setEnabled(false);
            action_print.setEnabled(true);
            action_print.putValue(Action.NAME, getLocalizedStringCap("print") + " (" + selectedKeys.size() + " " + LocalizedResources.localized.getString("pic.") + ")...");
        }
        if (applet) {
            action_export.setEnabled(false);
            action_share.setEnabled(false);
            action_import.setEnabled(false);
            action_opengl.setEnabled(false);
            action_sendFTP.setEnabled(false);
            action_quit.setEnabled(false);
            action_JMFload.setEnabled(false);
            ((JApplet) mainFrame).getJMenuBar().validate();
            ((JApplet) mainFrame).getJMenuBar().repaint();
        } else {
            ((JFrame) mainFrame).getJMenuBar().validate();
            ((JFrame) mainFrame).getJMenuBar().repaint();
        }
        actions_selection.clear();
        actions_selection.add(action_seeInHdd);
        actions_selection.add(action_copy_make);
        actions_selection.add(action_copy_toClipboard);
        actions_selection.add(action_copy_fromClipboard);
        actions_selection.add(nullAction);
        actions_selection.add(action_undo);
        actions_selection.add(action_undoAll);
        actions_selection.add(nullAction);
        actions_selection.add(action_save);
        actions_selection.add(action_saveAs);
        actions_selection.add(nullAction);
        actions_selection.add(action_revertToOriginal);
        actions_selection.add(nullAction);
        actions_selection.add(action_add);
        actions_selection.add(action_remove);
        actions_selection.add(nullAction);
        /*actions_selection.add(action_previewCrop);*/
        actions_selection.add(nullAction);
        actions_selection.add(action_fxFlipH);
        actions_selection.add(action_fxFlipV);
        actions_selection.add(action_fxRotateCCW);
        actions_selection.add(action_fxRotateCW);
        actions_selection.add(nullAction);
        actions_selection.add(action_share);
        actions_selection.add(nullAction);
        actions_selection.add(action_print);
    }
    final Action nullAction = new AbstractAction("null") {
        long hash = System.nanoTime();

        public int hashCode() {
            return (int) hash;
        }

        public boolean equals(Object o) {
            return o == null ? false : hashCode() == o.hashCode();
        }

        public void actionPerformed(ActionEvent e) {
        }
    };
    SortedMap<String, JIMCPanel> imagePanels = Collections.synchronizedSortedMap(new TreeMap<String, JIMCPanel>());
    Color clr_selected[] = new Color[]{new Color(0f, .4f, 1f), Color.ORANGE};

    private void _setSelected(boolean b, String key) {
        if (imagePanels.containsKey(key)) {
            JPanel imagePanel = imagePanels.get(key);
            imageCollection.setFXEnabled(key, true, GLFX.gfx.FX_SHADOW.bit(), new Point(3, 3), b ? clr_selected[1] : Color.GRAY);
            if (b) {
                imagePanel.setBackground(clr_selected[0]);
                imagePanel.setForeground(clr_selected[1]);
                if (picView.getToolkit().getSystemSelection() != null) {
                    picView.getToolkit().getSystemSelection().setContents(this, this);
                }
            } else {
                imagePanel.setBackground(picView.getBackground());
                imagePanel.setForeground(picView.getForeground() == null ? Color.BLACK : picView.getForeground());
            }
            Sprite._quickPaintImmediately(imagePanel);
        }
    }

    public void setSelected(boolean b, String key) {
        _setSelected(b, key);
        if (b) {
            selectedKeys.add(key);
        } else {
            selectedKeys.remove(key);
        }
    }

    public void setSelectedAll(boolean b) {
        synchronized (imagePanels) {
            for (String imagePanel : imagePanels.keySet()) {
                setSelected(b, imagePanel);
            }
        }
    }

    public boolean isSelected(String key) {
        return selectedKeys.contains(key);
    }

    public void clearAllSelection() {
        synchronized (selectedKeys) {
            for (String selection : selectedKeys) {
                if (imagePanels.containsKey(selection)) {
                    _setSelected(false, selection);
                }
            }
            selectedKeys.clear();
        }
    }

    private MouseInputAdapter newMouseCropper(final String photoKey, final Rectangle cphotoBounds, final Point moveDrag) {
        return new MouseInputAdapter() {
            long hash = System.nanoTime();

            public int hashCode() {
                return (int) hash;
            }

            public boolean equals(Object o) {
                return o == null ? false : o.hashCode() == hashCode();
            }
            Point drag = null;
            ANCHOR a = null;

            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                if (env.OS_MAC.isEnv() ? e.isMetaDown() : e.isControlDown()) {
                    e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    Rectangle r = imageCollectionInfo.get(photoKey).containsKey(INFO_CROPRECT) ? (Rectangle) imageCollectionInfo.get(photoKey).get(INFO_CROPRECT) : null;
                    e.getComponent().setCursor(_getCursor(_toROISpace(e.getPoint(), cphotoBounds, false), r));
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                boolean cropping = env.OS_MAC.isEnv() ? !e.isMetaDown() : !e.isControlDown();
                drag = _toROISpace(e.getPoint(), cphotoBounds, cropping);
                if (cropping) {
                    Rectangle r = imageCollectionInfo.get(photoKey).containsKey(INFO_CROPRECT) ? (Rectangle) imageCollectionInfo.get(photoKey).get(INFO_CROPRECT) : null;
                    if (r == null) {
                        r = new Rectangle();
                    }
                    a = _findAnchor(drag, r);
                    if (!(a instanceof ANCHOR)) {
                        r = new Rectangle();
                    }
                    imageCollectionInfo.get(photoKey).put(INFO_CROPRECT, r);
                    e.getComponent().setCursor(Cursor.getDefaultCursor());
                } else {
                    e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
                Sprite._quickPaintImmediately(fileInfo);

            }

            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                boolean cropping = env.OS_MAC.isEnv() ? !e.isMetaDown() : !e.isControlDown();
                Point edrag = _toROISpace(e.getPoint(), cphotoBounds, cropping);
                if (cropping) {
                    Rectangle r = imageCollectionInfo.get(photoKey).containsKey(INFO_CROPRECT) ? (Rectangle) imageCollectionInfo.get(photoKey).get(INFO_CROPRECT) : null;
                    if (r instanceof Rectangle) {
                        _resizeCropper(r, drag, edrag, a);
                    }
                } else {
                    moveDrag.setLocation(moveDrag.x + (edrag.x - drag.x), moveDrag.y + (edrag.y - drag.y));
                }
                Sprite._quickPaintImmediately(fileInfo);

            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                e.getComponent().setCursor(Cursor.getDefaultCursor());
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                imageCollectionInfo.get(photoKey).remove(INFO_CROPRECT);
                Sprite._quickPaintImmediately(fileInfo);

            }
        };
    }

    private GameActionLayer newGLLayer(final String photoKey, final Rectangle cphotoBounds, final Point moveDrag) {
        return new GameActionLayer("render photo") {
            @Override
            protected void doLoad() {
                super.doLoad();
                imageCollection.beginEdit(photoKey, false);
                Map<String, Serializable> store = imageCollection.getPictureStore(photoKey);
                imageCollection.getCollectionSprite(photoKey, false).updateTexData();
                photoBounds.setSize((Dimension) store.get(ImageCollection.ATTRIBUTE_ORIG_DIMENSION));
                ImageTypeSpecifier its = ImageTypeSpecifier.createFromBufferedImageType((Integer) store.get(ImageCollection.ATTRIBUTE_TYPE));
                switch (its.getNumComponents()) {
                    case 1:
                    case 2:
                    case 3:
                        RenderingSceneGL._GLclearColor = BufferIO._wrapf(Color.BLACK.getRGBComponents(null));
                        break;
                    default:
                        RenderingSceneGL._GLclearColor = BufferIO._wrapf(Color.LIGHT_GRAY.getRGBComponents(null));
                        break;
                }
                imageCollection.endEdit(photoKey, false);
            }

            @Override
            protected void doUnload() {
                super.doUnload();
                texNamesMap.clear();
                mTexNamesMap.clear();
                rescale = 1;
                offset = 0;
            }
            public SpriteGLHandler GLphoto = null, GLphotoTb = null;
            int photoGLRenderFormat = 0;
            Rectangle photoBounds = new Rectangle();
            IntBuffer buf = BufferIO._newi(1);

            @Override
            protected void doLoadOnGL(RenderingSceneGL context) {
                super.doLoadOnGL(context);
                if (photoKey == null) {
                    return;
                }
                imageCollection.beginEdit(photoKey, false);
                GLphoto = doLoadSprite(imageCollection.getCollectionSprite(photoKey, false));
                imageCollection.endEdit(photoKey, false);
            }

            private SpriteGLHandler doLoadSprite(Sprite sp) {
                SpriteGLHandler sgh = null;
                sp.setTextureModeEnabled(true);
                System.err.println("GL max size " + GLHandler._GLgetMaxTextureSize());
                sgh = RenderingSceneGL._GLloadSprite(sp, canvas);
                photoGLRenderFormat = sp.getTexData().get(0).getRenderFormat();
                sp.setTextureModeEnabled(false);
                return sgh;
            }

            @Override
            protected void doUnloadFromGL(RenderingSceneGL context) {
                super.doUnloadFromGL(context);
                RenderingSceneGL._GLunloadSprite(GLphoto);
                RenderingSceneGL._GLunloadSprite(GLphotoTb);
                glInitialized = false;
            }

            Point.Float[] getFloats(Rectangle r) {
                return new Point.Float[]{
                    new Point.Float((float) r.getX(), (float) r.getY()),
                    new Point.Float((float) r.getMaxX(), (float) r.getY()),
                    new Point.Float((float) r.getMaxX(), (float) r.getMaxY()),
                    new Point.Float((float) r.getX(), (float) r.getMaxY())
                };
            }
            List<MemScratch.Item<IntBuffer>> texMap = null;
            Map<Integer, Integer> texNamesMap = Collections.synchronizedMap(new HashMap<Integer, Integer>()),
                    mTexNamesMap = Collections.synchronizedMap(new HashMap<Integer, Integer>());
            float rescale = 1;
            int offset = 0;
            boolean glInitialized = false;
            boolean tiled = false;

            @Override
            protected void renderToGL(RenderingSceneGL context) {
                super.renderToGL(context);
                if (photoKey == null) {
                    return;
                }
                SpriteGLHandler photo = GLphoto;
                if (photo == null) {
                    return;
                }
                if (_debugops) {
                    canvas.setDebugEnabled(true);
                }
                cphotoBounds.setBounds(photoBounds);
                double zoom = 1.;
                if (imageCollectionInfo.containsKey(photoKey)) {
                    if (imageCollectionInfo.get(photoKey).containsKey(INFO_ZOOM)) {
                        zoom = ((Integer) imageCollectionInfo.get(photoKey).get(INFO_ZOOM)) / 100.;
                    }
                }
                cphotoBounds.setSize((int) Math.round(cphotoBounds.width * zoom), (int) Math.round(cphotoBounds.height * zoom));
                cphotoBounds.setBounds(
                        moveDrag.x + Math.round(((float) context.getWidth() - cphotoBounds.width) / 2f),
                        moveDrag.y + Math.round(((float) context.getHeight() - cphotoBounds.height) / 2f),
                        cphotoBounds.width, cphotoBounds.height);
                /**
                 * lookup tables with LWJGl
                 */
                boolean enabled = true;
                if (enabled && !previewContrast.getValueIsAdjusting() && !previewBrightness.getValueIsAdjusting() && (RenderingSceneGL._GLgetLoadState(photo) & RenderingSceneGL._GLLOADSTATE_Loaded) != 0) {
                    float nrescale = 1f + (float) previewContrast.getValue() / 100f;
                    int noffset = previewBrightness.getValue();
                    if (nrescale != rescale || noffset != offset) {
                        rescale = nrescale;
                        offset = noffset;

                        if (!glInitialized) {
                            tiled = GLHandler._GLisTiledTexture(photo.getTexHash());
                            /**
                             * make a copy of the texture mappings
                             */
                            texMap = new ArrayList(GLHandler.sTex.getTextureMap(photo.getTexHash()));
                            GLHandler.sTex.genVRAMBuffersMap(texMap.size());
                            texNamesMap.clear();
                            texNamesMap.putAll(GLHandler.sTex.getTextureName(photo.getTexHash()));
                            mTexNamesMap.clear();
                        }
                        List<MemScratch.Item<IntBuffer>> mTexMap = GLHandler.sTex.getTextureMap(photo.getTexHash());
                        mTexMap.clear();
                        /**
                         * image is tiled then several names are processed and
                         * texture mappings are modified with new textures from
                         * LUT
                         */
                        for (Item texIt : texMap) {
                            try {
                                /**
                                 * original content is read from GL cache
                                 */
                                final IntBuffer attr = (IntBuffer) texIt.data;
                                int hash = tiled ? texIt.name : photo.getTexHash();
                                int name = tiled ? texNamesMap.get(hash) : texIt.name;
                                Dimension size = new Dimension(attr.get(0), attr.get(1));
                                if (tiled) {
                                    Sf3TextureTile tile = new Sf3TextureTile(hash, photo.getTexHash(), attr);
                                    size = tile.getTileDim();
                                }

                                GL11.glBindTexture(Sf3Texture._EXTtex, name);
                                ByteBuffer buffer = BufferIO._new(size.width * 4 * size.height);

                                GL11.glGetTexImage(Sf3Texture._EXTtex, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
                                Util.checkGLError();
                                /**
                                 * save into the GL cache (made to VRAM
                                 * available)
                                 */
                                GL11.glBindTexture(Sf3Texture._EXTtex, 0);
                                Sf3Texture tex = GLHandler.newTexture(buffer, size, GL11.GL_RGBA);
                                int newName = glInitialized ? mTexNamesMap.get(texIt) : GLHandler.sTex.getVRAMFindVacant();
                                /**
                                 * the texture is patched with LUT
                                 */
                                GL11.glPixelTransferf(GL11.GL_RED_SCALE, rescale);
                                GL11.glPixelTransferf(GL11.GL_GREEN_SCALE, rescale);
                                GL11.glPixelTransferf(GL11.GL_BLUE_SCALE, rescale);
                                GL11.glPixelTransferf(GL11.GL_RED_BIAS, (float) offset / 256f);
                                GL11.glPixelTransferf(GL11.GL_GREEN_BIAS, (float) offset / 256f);
                                GL11.glPixelTransferf(GL11.GL_BLUE_BIAS, (float) offset / 256f);
                                Util.checkGLError();
                                GLHandler.ext.PixelBufferObjects.GL_setDisabled(true);
                                newName = GLHandler._GLLoadVRAM2D(tex, newName, false, 0, false);
                                GLHandler.ext.PixelBufferObjects.GL_setDisabled(false);

                                GL11.glPixelTransferf(GL11.GL_RED_SCALE, 1f);
                                GL11.glPixelTransferf(GL11.GL_GREEN_SCALE, 1f);
                                GL11.glPixelTransferf(GL11.GL_BLUE_SCALE, 1f);
                                GL11.glPixelTransferf(GL11.GL_RED_BIAS, 0f);
                                GL11.glPixelTransferf(GL11.GL_GREEN_BIAS, 0f);
                                GL11.glPixelTransferf(GL11.GL_BLUE_BIAS, 0f);
                                Util.checkGLError();
                                /**
                                 * this line performs a clean modification of
                                 * the Sprite texture mapping with the new
                                 * texture
                                 */
                                mTexMap.add(new Item<IntBuffer>(tiled ? tex.hashCode() : newName, attr));
                                /**
                                 * this line saves the bindings for a further
                                 * update
                                 */
                                mTexNamesMap.put(texIt.name, newName);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                        glInitialized = true;
                    }
                }
                /**
                 * Sprite is rendered with the selection rectangles
                 */
                double z = canvas.getZDepth();
                Sprite._GLRenderSprite(context, photo, cphotoBounds, ++z);
                Rectangle cropper = imageCollectionInfo.get(photoKey).containsKey(INFO_CROPRECT) ? (Rectangle) imageCollectionInfo.get(photoKey).get(INFO_CROPRECT) : null;
                /**
                 * a set of surrounding translucent black rectangle is used to
                 * paint the selection area
                 */
                if (cropper instanceof Rectangle) {
                    if (!cropper.isEmpty()) {
                        cropper = cropper.getBounds();
                        cropper.x += cphotoBounds.x;
                        cropper.y += cphotoBounds.y;
                        Rectangle[] surround = _getSurrounders(new Dimension(context.getWidth(), context.getHeight()), cropper);
                        if (surround != null) {
                            for (Rectangle surrounder : surround) {
                                GLGeom._GLfillPolygon(context, false, surrounder, GLGeom.getCWrgbaColors(Color.BLACK, .7f), ++z);
                            }
                        }
                        GLGeom._GLdrawRect(context, 0, false, cropper, .2f, ++z, 0, null, Color.WHITE);
                        /*EnumMap<ANCHOR, Point> np = _getAnchors(cropper);
                         z++;
                         for (ANCHOR a : np.keySet()) {
                         if (!a.equals(a.CENTER)) {
                         GLGeom._GLfillShape(context, getFloats(_getMouseArea(np.get(a))), GLGeom.getCWrgbaColors(Color.ORANGE, .9f), z);
                         }
                         }*/
                    }
                }
                if (_debugops) {
                    canvas.setDebugEnabled(false);
                }
            }

            public boolean isInnerResourceModeEnabled() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            public void setInnerResourceModeEnabled(boolean arg0) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
    }
    private List<Action> actions_selection = Collections.synchronizedList(new Vector<Action>());
    MouseInputAdapter mia;
    public final static int MODE_GRID = 0;
    public final static int MODE_NAV = 1;
    private int browserMode = MODE_GRID;
    boolean browserUpdatePosted = false;
    public RenderingSceneGL canvas;

    private void switchGLLayer(final String photoKey) {
        final Rectangle cphotoBounds = new Rectangle();
        final Point moveDrag = new Point(0, 0);
        picView.remove(canvas);
        canvas.removeMouseListener(currentCropper);
        canvas.removeMouseMotionListener(currentCropper);
        canvas.removeMouseWheelListener(mwl);
        currentCropper = newMouseCropper(photoKey, cphotoBounds, moveDrag);
        canvas.removeActionOffscreen(currentGLLayer);
        currentGLLayer = newGLLayer(photoKey, cphotoBounds, moveDrag);
        canvas.addActionOffscreen(currentGLLayer);
        canvas.addMouseListener(currentCropper);
        canvas.addMouseMotionListener(currentCropper);
        canvas.addMouseWheelListener(mwl);
        picView.add(canvas, BorderLayout.CENTER);
        ThreadWorks.Swing.invokeLater(new Runnable() {

            public void run() {
                canvas.startRendering();
            }
        });
    }

    private static Rectangle[] _getSurrounders(Dimension context, Rectangle cropper) {
        if (cropper.isEmpty()) {
            return null;
        }
        Rectangle[] surround = new Rectangle[]{
            new Rectangle(0, 0, cropper.x, cropper.y),
            new Rectangle(cropper.x, 0, cropper.width, cropper.y),
            new Rectangle((int) cropper.getMaxX(), 0, (int) Math.round(context.getWidth() - cropper.getMaxX()), cropper.y),
            new Rectangle((int) cropper.getMaxX(), cropper.y, (int) Math.round(context.getWidth() - cropper.getMaxX()), cropper.height),
            new Rectangle((int) cropper.getMaxX(), (int) cropper.getMaxY(), (int) Math.round(context.getWidth() - cropper.getMaxX()), (int) Math.round(context.getHeight() - cropper.getMaxY())),
            new Rectangle(cropper.x, (int) cropper.getMaxY(), cropper.width, (int) Math.round(context.getHeight() - cropper.getMaxY())),
            new Rectangle(0, (int) cropper.getMaxY(), cropper.x, (int) Math.round(context.getHeight() - cropper.getMaxY())),
            new Rectangle(0, cropper.y, cropper.x, cropper.height)
        };
        return surround;
    }

    private static Rectangle _resizeCropper(Rectangle r, Point dragStart, Point dragCurrent, ANCHOR a) {
        EnumMap<ANCHOR, Point> anchors = _getAnchors(r);
        if (a == null) {
            r.setFrameFromDiagonal(dragStart, dragCurrent);
        } else {
            switch (a) {
                case NE:
                    r.setFrameFromDiagonal(dragCurrent, anchors.get(ANCHOR.SW));
                    break;
                case NW:
                    r.setFrameFromDiagonal(dragCurrent, anchors.get(ANCHOR.SE));
                    break;
                case SW:
                    r.setFrameFromDiagonal(dragCurrent, anchors.get(ANCHOR.NE));
                    break;
                case SE:
                    r.setFrameFromDiagonal(dragCurrent, anchors.get(ANCHOR.NW));
                    break;
                case N:
                    r.setFrame(r.x, dragCurrent.y, r.width, Math.abs(dragCurrent.y - anchors.get(ANCHOR.S).y));
                    break;
                case S:
                    r.setFrame(r.x, anchors.get(ANCHOR.N).y, r.width, Math.abs(dragCurrent.y - anchors.get(ANCHOR.N).y));
                    break;
                case E:
                    r.setFrame(anchors.get(ANCHOR.W).x, r.y, Math.abs(dragCurrent.x - anchors.get(ANCHOR.W).x), r.height);
                    break;
                case W:
                    r.setFrame(dragCurrent.x, r.y, Math.abs(dragCurrent.x - anchors.get(ANCHOR.E).x), r.height);
                    break;
                case CENTER:
                    r.setLocation(r.x + dragCurrent.x - dragStart.x, r.y + dragCurrent.y - dragStart.y);
                    break;
                default:
                    r.setFrameFromDiagonal(dragStart, dragCurrent);
                    break;
            }
        }
        return r;
    }
    /**
     * code disable the splash screen (classloader issues)
     */
    private boolean disableSplash = !Boolean.getBoolean(ENVSPLASH);

    public DisplayInterface getSplash() {
        if (disableSplash) {
            return null;
        }
        try {
            URL resource = System.getProperty(ENVSPLASHURL) != null ? new File(System.getProperty(ENVSPLASHURL)).toURI().toURL() : IMCBrowser.class.getResource("splash.jpg");
            final DisplayInterface disp = Display._Display(resource, null);
            return disp;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public Vector<Action> getLoadLayers() {
        Vector<Action> loaders = new Vector<Action>();
        loaders.add(loadAppStore);
        loaders.add(new AbstractAction(getLocalizedStringCap("Startup")) {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Your system uses " + LocalizedResources.language.getLanguage() + " language.");
                System.err.println("IMC Browser started.");
                storeApplication(applicationStore);
            }
        });
        return loaders;
    }

    private static enum ANCHOR {

        NW(), N(), NE(), E(), SE(), S(), SW(), W(), CENTER();
    }

    /**
     * 8 points are anchors for resizing the rectangle
     *
     * @return an array that begins with the {@link ANCHOR#NW) (0) Clockwise to {@link ANCHOR#W} (7), or null if the rectangle is empty
     */
    private static EnumMap<ANCHOR, Point> _getAnchors(Rectangle cropper) {
        if (cropper.isEmpty()) {
            return null;
        }
        EnumMap<ANCHOR, Point> np = new EnumMap<ANCHOR, Point>(ANCHOR.class);
        np.put(ANCHOR.NW, new Point(cropper.x, cropper.y));
        np.put(ANCHOR.N, new Point(Math.round(cropper.x + cropper.width / 2f), cropper.y));
        np.put(ANCHOR.NE, new Point(cropper.x + cropper.width, cropper.y));
        np.put(ANCHOR.E, new Point(cropper.x + cropper.width, Math.round(cropper.y + cropper.height / 2f)));
        np.put(ANCHOR.SE, new Point(cropper.x + cropper.width, cropper.y + cropper.height));
        np.put(ANCHOR.S, new Point(Math.round(cropper.x + cropper.width / 2f), cropper.y + cropper.height));
        np.put(ANCHOR.SW, new Point(cropper.x, cropper.y + cropper.height));
        np.put(ANCHOR.W, new Point(cropper.x, Math.round(cropper.y + cropper.height / 2f)));
        np.put(ANCHOR.CENTER, new Point((int) cropper.getCenterX(), (int) cropper.getCenterY()));
        return np;
    }
    static final int _mouseSize = 5;

    static Rectangle _getMouseArea(Point mouse) {
        Rectangle a = new Rectangle();
        a.setFrameFromCenter(mouse, new Point(mouse.x + Math.round(_mouseSize / 2f), mouse.y + Math.round(_mouseSize / 2f)));
        return a;
    }

    static Point _toROISpace(Point mouse, Rectangle roi, boolean clamp) {
        Point drag = mouse.getLocation();
        if (clamp) {
            drag.setLocation(
                    Math.min(roi.getMaxX(), Math.max(roi.getMinX(), drag.x)),
                    Math.min(roi.getMaxY(), Math.max(roi.getMinY(), drag.y)));

        }
        drag.x -= roi.x;
        drag.y -= roi.y;
        return drag;
    }

    static Cursor _getCursor(Point drag, Rectangle r) {
        Cursor c = Cursor.getDefaultCursor();
        ANCHOR a = null;
        if (r != null) {
            if ((a = _findAnchor(drag, r)) instanceof ANCHOR) {
                switch (a) {
                    case N:
                        c = Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
                        break;
                    case S:
                        c = Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
                        break;
                    case SE:
                        c = Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
                        break;
                    case SW:
                        c = Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
                        break;
                    case NE:
                        c = Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
                        break;
                    case NW:
                        c = Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
                        break;
                    case E:
                        c = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
                        break;
                    case W:
                        c = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
                        break;
                    default:
                        c = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
                        break;
                }
            }
        }
        return c;
    }

    private static ANCHOR _findAnchor(Point mouse, Rectangle cropper) {
        if (cropper.isEmpty()) {
            return null;
        }
        EnumMap<ANCHOR, Point> np = _getAnchors(cropper);
        Rectangle mouseArea = _getMouseArea(mouse);
        Map<ANCHOR, Line2D> outline = new EnumMap<ANCHOR, Line2D>(ANCHOR.class);
        outline.put(ANCHOR.N, new Line2D.Float(np.get(ANCHOR.NE), np.get(ANCHOR.NW)));
        outline.put(ANCHOR.E, new Line2D.Float(np.get(ANCHOR.NE), np.get(ANCHOR.SE)));
        outline.put(ANCHOR.S, new Line2D.Float(np.get(ANCHOR.SE), np.get(ANCHOR.SW)));
        outline.put(ANCHOR.W, new Line2D.Float(np.get(ANCHOR.SW), np.get(ANCHOR.NW)));
        int cnt = 0;
        ANCHOR ac = null;
        for (ANCHOR a : np.keySet()) {
            if (mouseArea.contains(np.get(a))) {
                ac = a;
                cnt++;
            }
        }
        if (cnt == 1) {
            return ac;
        }
        cnt = 0;
        for (ANCHOR a : outline.keySet()) {
            if (outline.get(a).intersects(mouseArea)) {
                ac = a;
                cnt++;
            }
        }
        if (cnt == 1) {
            return ac;
        }
        if (cropper.contains(mouse)) {
            return ANCHOR.CENTER;
        }
        return null;
    }
    GameActionLayer currentGLLayer = null;
    MouseInputAdapter currentCropper = null;
    /**
     * enables debugging
     */
    static boolean _debugops = Boolean.getBoolean("jxa.debugOps");
    MouseWheelListener mwl = new MouseWheelListener() {
        @Override
        public boolean equals(Object obj) {
            return obj == null ? false : hashCode() == obj.hashCode();
        }
        long hash = System.nanoTime();

        @Override
        public int hashCode() {
            return (int) hash;
        }

        public void mouseWheelMoved(MouseWheelEvent e) {
            int moved = e.getWheelRotation();
            if (previewZoom != null) {
                previewZoom.setSelectedIndex(Math.max(0, Math.min(previewZoom.getItemCount() - 1, previewZoom.getSelectedIndex() + moved)));
            }
        }
    };
    /**
     * navigator when previewing, editing a picture
     */
    private Navigator nav = null;
    /**
     * navigation panel when previewing, editing a picture
     */
    private InstructionsPane navPane = null;

    Dimension thumbSize = new Dimension(SpriteIO._WRITE_TILES_DIMENSION);

    private void _updateBrowser() {
        browserUpdatePosted = false;
        updateCurrentStatus(null, getLocalizedStringCap("Loadingphotoalbum") + "...");
        Sprite._quickPaintImmediately(statusLabel);
        statusLabel.repaint();
        /**
         * reflect thumbnail dim
         */
        imageCollection.setThumbnailSize(thumbSize);
        imageCollection.setFXEnabled(true, GLFX.gfx.FX_SHADOW.bit(), new Point(3, 3), Color.GRAY);
        if (!imageCollection.isResourceLoaded()) {
            imageCollection.loadResource();
        }
        picView.removeAll();
        picView.validate();
        synchronized (imageCollection) {
            for (String key : imageCollection.keySet(true)) {
                updateCollectionNameProperty(key, imageCollectionInfo, imageCollection);
            }
        }
        imagePanels = Collections.synchronizedSortedMap(new TreeMap<String, JIMCPanel>());
        /*SortedMap<String, JComponent> coll;*/
        switch (browserMode) {
            case MODE_NAV:
                dbList.setEnabled(false);
                action_new.setEnabled(false);
                action_removeCollection.setEnabled(false);
                action_search.setEnabled(false);
                if (selectedKeys.isEmpty()) {
                    browserUpdatePosted = false;
                    browserMode = MODE_GRID;
                    callBrowserUpdate();
                    return;
                }
                String newSelect = selectedKeys.iterator().next();
                navPane = new InstructionsPane(picView, imageCollection.keySet(true).headSet(newSelect).size(), imageCollection.keySet(true).size());
                final String[] filesIds,
                 filesKeys;
                filesIds = imageCollection.keySet(true).toArray(new String[]{});
                filesKeys = imageCollection.keySet(true).toArray(new String[]{});
                for (int i = 0; i < filesIds.length; i++) {
                    filesIds[i] = "no." + (i + 1) + " " + new File(filesIds[i]).getName();
                }
                nav = new Navigator(navPane, filesIds);

                imageCollection.setRenderer(navRenderer);
                try {
                    imageCollection.waitForAllThumbnails();
                    synchronized (imageCollection) {
                        for (Iterator<String> it = imageCollection.keySet(true).iterator(); it.hasNext();) {
                            int n = navPane.addStep();
                            String key = it.next();
                            JIMCPanel preview = new JIMCPanel(key);
                            preview.setLayout(new GridBagLayout());
                            GridBagConstraints c = new GridBagConstraints();
                            imageCollection.beginEdit(key, true);
                            JComponent pictureViewer = imageCollection.getThumbnailComponent(key);
                            Dimension imageDim = (Dimension) imageCollection.getThumbnailPictureStore(key).get(ImageCollection.ATTRIBUTE_ORIG_DIMENSION);
                            Properties p = (Properties) imageCollectionInfo.get(key).get(INFO_PROP);
                            p.setProperty("dimension", imageDim.width + "x" + imageDim.height);
                            imageCollectionInfo.get(key).put(INFO_DIMENSION, imageDim);
                            pictureViewer.setPreferredSize(null);
                            preview.add(pictureViewer, c);
                            navPane.addStepComponent(n, preview, 1, 1, 1.0, 1.0);
                            imagePanels.put(key, preview);
                            imageCollection.endEdit(key, true);
                        }
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                } finally {
                    picView.setLayout(new BorderLayout());
                    picView.add(canvas, BorderLayout.CENTER);
                    JPanel interactivePanel = new JPanel(new GridBagLayout(), true);
                    GridBagConstraints c = new GridBagConstraints();
                    c.weightx = 0.1;
                    c.weighty = 0.1;
                    c.gridwidth = 1;
                    c.anchor = c.NORTHWEST;
                    c.fill = c.HORIZONTAL;
                    interactivePanel.add(previewZoom = new JComboBox(new Integer[]{100, 200, 95, 90, 75, 66, 50, 33, 25, 10, 5}), c);
                    previewZoom.setEditable(true);
                    previewZoom.setRenderer(new ListCellRenderer() {
                        JLabel lab = new JLabel();

                        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                            lab.setText(value.toString() + "%");
                            lab.setBackground(isSelected ? Color.BLUE : Color.white);
                            lab.setForeground(isSelected ? Color.white : Color.black);
                            lab.setOpaque(true);
                            return lab;
                        }
                    });
                    previewZoom.addActionListener(action_zoom = doZm(filesKeys));
                    previewZoom.addMouseWheelListener(mwl);
                    /**
                     * sliders contrast and brightness
                     */
                    previewContrast = new JSlider(-50, 50, 0);
                    previewBrightness = new JSlider(-127, 127, 0);
                    previewContrast.setMajorTickSpacing(50);
                    previewBrightness.setMajorTickSpacing(127);
                    previewContrast.setPaintLabels(true);
                    previewBrightness.setPaintLabels(true);
                    previewContrast.addChangeListener(new ChangeListener() {
                        public void stateChanged(ChangeEvent e) {
                            String current = filesKeys[navPane.getCurrentStep()];
                            if (!imageCollectionInfo.get(current).containsKey(INFO_CONTRAST)) {
                                imageCollectionInfo.get(current).put(INFO_CONTRAST, previewContrast.getValue());
                            }
                        }
                    });
                    previewBrightness.addChangeListener(new ChangeListener() {
                        public void stateChanged(ChangeEvent e) {
                            String current = filesKeys[navPane.getCurrentStep()];
                            if (!imageCollectionInfo.get(current).containsKey(INFO_BRIGHTNESS)) {
                                imageCollectionInfo.get(current).put(INFO_BRIGHTNESS, previewBrightness.getValue());
                            }
                        }
                    });
                    interactivePanel.add(new JLabel(contrastIcon), c);
                    c.weightx = 1.0;
                    interactivePanel.add(previewContrast, c);
                    c.weightx = 0;
                    interactivePanel.add(new JLabel(brightnessIcon), c);
                    c.weightx = 1.0;
                    interactivePanel.add(previewBrightness, c);
                    c.gridwidth = c.REMAINDER;
                    interactivePanel.add(new JButton(action_lutApply = doLUT(filesKeys)), c);
                    c.gridwidth = c.RELATIVE;
                    interactivePanel.add(new JButton(previewCDF = doCDF(filesKeys)), c);
                    c.gridwidth = c.REMAINDER;
                    interactivePanel.add(new JButton(action_previewCrop = doCrop(filesKeys)), c);
                    c.fill = c.HORIZONTAL;
                    navPane.addStepListener(new StepListener() {
                        public void stepChanged() {
                            reloadCanvas(filesKeys[navPane.getCurrentStep()]);
                        }
                    });
                    c.weightx = 1.0;
                    c.fill = c.BOTH;
                    c.weightx = 0;
                    c.weighty = 0.2;
                    c.fill = c.HORIZONTAL;
                    c.anchor = c.SOUTH;
                    interactivePanel.add(nav, c);
                    JXALabel switchToGrid = new JXALabel(getLocalizedStringCap("backtogrid"));
                    switchToGrid.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            super.mouseClicked(e);
                            if (picView.isEnabled()) {
                                browserMode = MODE_GRID;
                                callBrowserUpdate();
                            }
                        }
                    });
                    c.fill = c.HORIZONTAL;
                    c.anchor = c.LINE_END;
                    interactivePanel.add(switchToGrid, c);
                    picView.add(interactivePanel, BorderLayout.SOUTH);
                    picView.validate();
                    reloadCanvas(newSelect);
                    if (canvas.getState() != State.RUNNING) {
                        canvas.startRendering();
                    }
                    break;
                }
            case MODE_GRID:
                dbList.setEnabled(true);
                action_new.setEnabled(true);
                action_removeCollection.setEnabled(true);
                action_search.setEnabled(true);
                imageCollection.setFXEnabled(true, GLFX.gfx.FX_SHADOW.bit(), new Point(3, 3), Color.GRAY);
                imageCollection.setRenderer(imageCollection);
                if (!imageCollection.isResourceLoaded()) {
                    imageCollection.loadResource();
                }
                picView.setLayout(new GridBagLayout());
                GridBagConstraints c = new GridBagConstraints();
                final JPanel panelBrowser = new JPanel(true);
                GridLayout grid;
                panelBrowser.setLayout(grid = new GridLayout());
                panelBrowser.addMouseListener(mia);
                panelBrowser.addMouseMotionListener(mia);
                c.fill = c.BOTH;
                c.weightx = 1.0;
                c.weighty = 1.0;
                c.anchor = c.FIRST_LINE_START;
                JScrollPane sp;
                picView.add(sp = new JScrollPane(panelBrowser), c);
                sp.getVerticalScrollBar().setUnitIncrement(30);
                sp.getHorizontalScrollBar().setUnitIncrement(30);
                picView.validate();
                sp.addHierarchyBoundsListener(new HierarchyBoundsListener() {
                    public void ancestorMoved(HierarchyEvent e) {
                    }

                    public void ancestorResized(HierarchyEvent e) {
                        callBrowserUpdate();
                    }
                });

                int hgap = 10;
                int vgap = 10;
                int col = (int) Math.max(1, Math.floor((float) picView.getWidth() / (float) (imageCollection.getThumbnailSize().getWidth() + hgap)));
                int row = (int) Math.max(Math.floor((float) imageCollection.keySet(true).size() / (float) col), Math.floor((float) picView.getHeight() / (float) (imageCollection.getThumbnailSize().getHeight() + vgap)));
                System.out.println("tb dim : " + imageCollection.getThumbnailSize() + " browser dim : " + picView.getSize());
                /**
                 * rows overrides columns if not zero
                 */
                grid.setColumns(col);
                grid.setRows(0);
                grid.setHgap(hgap);
                grid.setVgap(vgap);
                /**
                 *
                 */
                int n = 0;
                synchronized (imageCollection) {
                    for (Iterator<String> it = imageCollection.keySet(true).iterator(); it.hasNext(); n++) {
                        final String key = it.next();
                        JIMCPanel imagePanel = new JIMCPanel(key);
                        imagePanel.setLayout(new BorderLayout());
                        imagePanels.put(key, imagePanel);
                        imageCollection.beginEdit(key, true);
                        Dimension image = (Dimension) imageCollection.getThumbnailPictureStore(key).get(ImageCollection.ATTRIBUTE_ORIG_DIMENSION);
                        Properties p = (Properties) imageCollectionInfo.get(key).get(INFO_PROP);
                        p.setProperty("dimension", image.width + "x" + image.height);
                        imageCollectionInfo.get(key).put(INFO_DIMENSION, image);
                        imagePanel.add(imageCollection.getThumbnailComponent(key), BorderLayout.CENTER);
                        final JXALabel label;
                        imagePanel.add(label = new JXALabel(key.substring(key.lastIndexOf(File.separator) + 1)), BorderLayout.SOUTH);
                        label.setMaxWidth(imageCollection.getThumbnailSize().width, 4);
                        label.setToolTipText(key);
                        label.addMouseListener(new MouseAdapter() {
                            public void mouseClicked(MouseEvent e) {
                                super.mouseClicked(e);
                                if (picView.isEnabled()) {
                                    browserMode = MODE_NAV;
                                    clearAllSelection();
                                    setSelected(true, key);
                                    updateCurrentFileInfo();
                                    callBrowserUpdate();
                                }
                            }
                        });
                        /*c.gridwidth = n + 1 % col == 0 ? c.REMAINDER : 1;*/
                        panelBrowser.add(imagePanel);/*, c);*/

                        imageCollection.endEdit(key, true);
                    }
                }
                for (int i = n; i < col * row; i++) {
                    JLabel lab = new JLabel();
                    lab.setPreferredSize(imageCollection.getThumbnailSize());
                    /*c.gridwidth = i + 1 % col == 0 ? c.REMAINDER : 1;*/
                    panelBrowser.add(lab);/*, c);*/

                }
                picView.validate();
                break;
            default:
                break;
        }
        synchronized (selectedKeys) {
            for (Iterator<String> it = selectedKeys.iterator(); it.hasNext();) {
                String key = it.next();
                if (imagePanels.containsKey(key)) {
                    _setSelected(true, key);
                } else {
                    it.remove();
                }
            }
        }
        synchronized (imagePanels) {
            for (JIMCPanel panel : imagePanels.values()) {
                Properties prop = (Properties) imageCollectionInfo.get(panel.key).get(INFO_PROP);
                if (prop.getProperty("saved") != null) {
                    panel.saved = prop.getProperty("saved").equalsIgnoreCase("true");
                }
            }
        }
        updateCurrentFileInfo();
        updateCurrentStatus(null, statusLabelDefaultText);
        Sprite._quickPaintImmediately(picView);
    }

    private String canvasCurrentK = "";

    private void reloadCanvas(String filesKey) {
        if (imageCollectionInfo.containsKey(canvasCurrentK)) {
            imageCollectionInfo.get(canvasCurrentK).remove(INFO_CROPRECT);
        }
        if (filesKey != null && imageCollectionInfo.containsKey(filesKey)) {
            canvasCurrentK = filesKey;
            imageCollectionInfo.get(canvasCurrentK).remove(INFO_CROPRECT);
            clearAllSelection();
            setSelected(true, canvasCurrentK);
            if (imageCollectionInfo.get(canvasCurrentK).containsKey(INFO_ZOOM)) {
                previewZoom.setSelectedItem(imageCollectionInfo.get(canvasCurrentK).get(INFO_ZOOM));
            } else {
                action_zoom.actionPerformed(new ActionEvent(this, hashCode(), getLocalizedStringCap("zoom")));
            }
            if (imageCollectionInfo.get(canvasCurrentK).containsKey(INFO_CONTRAST)) {
                previewContrast.setValue((Integer) imageCollectionInfo.get(canvasCurrentK).get(INFO_CONTRAST));
            } else {
                previewContrast.setValue(0);
            }
            if (imageCollectionInfo.get(canvasCurrentK).containsKey(INFO_BRIGHTNESS)) {
                previewBrightness.setValue((Integer) imageCollectionInfo.get(canvasCurrentK).get(INFO_BRIGHTNESS));
            } else {
                previewBrightness.setValue(0);
            }
        }
        updateCurrentFileInfo();
        if (canvasCurrentK != null) {
            switchGLLayer(canvasCurrentK);
        }
    }

    private void undoErrors(final ImageCollection ic) {
        selectedKeys.clear();
        for (String err : ic.getErrored(true)) {
            selectedKeys.add(err);
        }
        ThreadWorks.Swing.invokeLater(new Runnable() {

            public void run() {
                switch (UIMessage.showConfirmDialog(picView, getLocalizedStringCap("erroroccuredDoUndo"), "erroroccured", JOptionPane.OK_CANCEL_OPTION)) {
                    case JOptionPane.OK_OPTION:
                        action_undo.actionPerformed(new ActionEvent(picView, ActionEvent.ACTION_PERFORMED, "undo"));
                        break;
                    case JOptionPane.CANCEL_OPTION:
                    default:
                        callBrowserUpdate();
                        break;
                }
            }
        });
    }

    /**
     *
     */
    public boolean waitForAllPictures(final ImageCollection ic) {
        try {
            ic.waitForAll();
            return true;
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            undoErrors(ic);
            return false;
        }
    }

    private AbstractAction doZm(final String[] filesKeys) {
        return new AbstractAction() {
            boolean updating = false;

            public void actionPerformed(ActionEvent e) {
                if (previewZoom.getSelectedItem() == null) {
                    return;
                }
                int selectedZoom = (Integer) previewZoom.getSelectedItem();
                if (selectedZoom <= 0) {
                    new UIMessage(true, getLocalizedStringCap("enterzoom"), null);
                    return;
                }
                String current = filesKeys[navPane.getCurrentStep()];
                if (imageCollectionInfo.get(current).containsKey(INFO_ZOOM)) {
                    if (imageCollectionInfo.get(current).get(INFO_ZOOM).equals(selectedZoom)) {
                        return;
                    }
                }
                if (!updating) {
                    updating = true;
                    imageCollectionInfo.get(current).put(INFO_ZOOM, selectedZoom);
                    updating = false;
                }
            }
        };
    }

    private AbstractAction doLUT(final String[] filesKeys) {
        return new AbstractAction(getLocalizedStringCap("Apply!"), lutIcon) {
            public void actionPerformed(ActionEvent e) {
                if (!picView.isEnabled()) {
                    return;
                }
                final String op = getLocalizedStringCap("L.U.T.");
                final String current = filesKeys[navPane.getCurrentStep()];

                Runnable r = new Runnable() {
                    public void run() {
                        if (!waitForAllPictures(imageCollection)) {
                            return;
                        }
                        currentProcessThumb.setIcon(lutIcon_64);
                        long wait = UIMessage.newProgress(0, 100, picView);
                        JDialog pp = showLightPopupActivity(op, currentProcessThumb, wait, env.THREADING_MULTI.isEnv() ? Thread.currentThread() : null, UIMessage.UI_TOP_RIGHT);
                        boolean interrupt = false;
                        action_lutApply.setEnabled(false);
                        edit_started(wait, op);
                        try {
                            imageCollection.beginEdit(current, false);
                            Sprite sp = edit_newDrawingCanvas(current);
                            float rescale = 1f + (float) previewContrast.getValue() / 100f;
                            int offset = previewBrightness.getValue();
                            sp.runValidate();
                            BufferedImage image = sp.toBuffered();
                            edit_buffer(image);
                            RenderedImage image2 = doLookupTable(image, rescale, offset);
                            edit_buffer(image2);
                            edit_copyImageOnCanvas(image2, sp);
                            edit_clearBuffer(image);
                            edit_clearBuffer(image2);
                            edit_saveCanvas(sp, current);
                            imageCollectionInfo.get(current).remove(INFO_BRIGHTNESS);
                            imageCollectionInfo.get(current).remove(INFO_CONTRAST);
                            edit_finalize(current, true);
                        } catch (Exception e) {
                            e.printStackTrace();
                            interrupt = true;
                        } finally {
                            imageCollection.endEdit(current, false);
                            edit_finished(wait, op, interrupt);
                            action_lutApply.setEnabled(true);
                            pp.setVisible(false);
                            UIMessage.kill(wait);
                        }
                    }
                };
                canvas.doTaskOnScreen(r);
            }
        };
    }

    private AbstractAction doCDF(final String[] filesKeys) {
        return new AbstractAction(getLocalizedStringCap("CDF"), magicIcon) {
            public void actionPerformed(ActionEvent e) {
                if (!picView.isEnabled()) {
                    return;
                }
                final String current = filesKeys[navPane.getCurrentStep()];
                Runnable run = new Runnable() {
                    public void run() {
                        if (!waitForAllPictures(imageCollection)) {
                            return;
                        }
                        boolean interrupt = false;
                        String op = getLocalizedStringCap("CDF");
                        currentProcessThumb.setIcon(magicIcon_64);
                        long wait = UIMessage.newProgress(0, 100, picView);
                        JDialog pp = showLightPopupActivity(op, currentProcessThumb, wait, env.THREADING_MULTI.isEnv() ? Thread.currentThread() : null, UIMessage.UI_TOP_RIGHT);
                        edit_started(wait, op);
                        try {
                            imageCollection.beginEdit(current, false);
                            Sprite edit = edit_newDrawingCanvas(current);
                            int bufferedType = edit.getBufferedType();
                            if (SpriteIO._getITS(bufferedType).getColorModel().hasAlpha()) {
                                edit.setBufferedType(SPRITE_TYPE_NO_ALPHA);
                            }
                            edit.runValidate();
                            BufferedImage src = edit.toBuffered();
                            edit.setBufferedType(bufferedType);
                            edit_buffer(src);
                            RenderedOp hist = doEnhance(src);
                            edit_buffer(hist);
                            edit_copyImageOnCanvas(hist, edit);
                            edit_clearBuffer(src);
                            edit_clearBuffer(hist);
                            edit_saveCanvas(edit, current);
                            edit_finalize(current, true);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            interrupt = true;
                        } finally {
                            imageCollection.endEdit(current, false);
                            edit_finished(wait, op, interrupt);
                            pp.setVisible(false);
                            UIMessage.kill(wait);
                        }
                    }
                };
                canvas.doTaskOnScreen(run);
            }
        };
    }

    private AbstractAction doResize(final long resizeProgress) {
        return new AbstractAction(getLocalizedStringCap("apply")) {

            public void actionPerformed(ActionEvent e) {

                Runnable r = new Runnable() {
                    public void run() {
                        if (!waitForAllPictures(imageCollection)) {
                            return;
                        }
                        String opName;
                        edit_started(resizeProgress, opName = getLocalizedStringCap("Resizing"));
                        Dimension dim = new Dimension(((SpinnerNumberModel) resize_width.getModel()).getNumber().intValue(), ((SpinnerNumberModel) resize_height.getModel()).getNumber().intValue());
                        double scale = !resize_heightCB.isSelected() && !resize_widthCB.isSelected() ? ((SpinnerNumberModel) resize_scale.getModel()).getNumber().doubleValue() / 100.0 : 1.0;
                        boolean canvas_scale = resize_canvasCB.isSelected();
                        boolean width_scale = resize_widthCB.isSelected();
                        boolean height_scale = resize_heightCB.isSelected();
                        boolean interrupt = false;
                        synchronized (selectedKeys) {
                            for (String selection : selectedKeys) {
                                Object src = null;
                                try {
                                    imageCollection.beginEdit(selection, false);
                                    Sprite edit = (Sprite) edit_newDrawingCanvas(selection);
                                    boolean doTiled = edit.isTilesAvailable();
                                    edit.setTileModeEnabled(doTiled);
                                    if (scale != 1.0) {
                                        if (canvas_scale) {
                                            edit.runValidate();
                                            if (doTiled) {
                                                src = edit.clone();
                                            } else {
                                                src = edit.toBuffered();
                                                edit_buffer(src);
                                            }
                                            edit_clearCanvas(edit);
                                        }
                                        edit.setSize((int) (scale * edit.getWidth()), (int) (scale * edit.getHeight()));
                                        edit.runValidate();
                                        if (canvas_scale) {
                                            edit_copyImageOnCanvas(src, edit);
                                            if (!doTiled) {
                                                edit_clearBuffer(src);
                                            }
                                        }
                                    }
                                    if (width_scale || height_scale) {
                                        if (canvas_scale) {
                                            edit.runValidate();
                                            if (doTiled) {
                                                src = edit.clone();
                                            } else {
                                                src = edit.toBuffered();
                                                edit_buffer(src);
                                            }
                                            edit_clearCanvas(edit);
                                        }
                                        if (width_scale && !height_scale) {
                                            double fixedScale = (double) dim.width / (double) edit.getWidth();
                                            edit.setSize(dim.width, (int) (fixedScale * edit.getHeight()));
                                        }
                                        if (height_scale && !width_scale) {
                                            double fixedScale = (double) dim.height / (double) edit.getHeight();
                                            edit.setSize((int) (fixedScale * edit.getWidth()), dim.height);
                                        }
                                        if (height_scale && width_scale) {
                                            edit.setSize(dim);
                                        }
                                        edit.runValidate();
                                        if (canvas_scale) {
                                            edit_copyImageOnCanvas(src, edit);
                                            if (!doTiled) {
                                                edit_clearBuffer(src);
                                            }
                                        }
                                    }
                                    edit_saveCanvas(edit, selection);
                                    edit_finalize(selection, true);
                                    edit_clearBuffer(src);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    interrupt = true;
                                } finally {
                                    imageCollection.endEdit(selection, false);
                                    edit_progress(resizeProgress, 1);
                                }
                            }
                        }
                        edit_finished(resizeProgress, opName, interrupt);
                    }
                };
                canvas.doTaskOnScreen(r);
            }
        };
    }

    private AbstractAction doFlipV(long fxProgress) {
        return doFlip(fxProgress, Sprite.VERTICAL);
    }

    private AbstractAction doFlipH(long fxProgress) {
        return doFlip(fxProgress, Sprite.HORIZONTAL);
    }

    private AbstractAction doFlip(final long fxProgress, final int orient) {
        String name = "flip";
        switch (orient) {
            case Sprite.VERTICAL:
                name += "vertically";
                break;
            default:
                name += "horizontally";
                break;
        }
        return new AbstractAction(getLocalizedStringCap(name)) {

            public void actionPerformed(ActionEvent e) {
                Runnable r = new Runnable() {
                    public void run() {
                        if (!waitForAllPictures(imageCollection)) {
                            return;
                        }
                        int n = 0;
                        boolean interrupt = false;
                        String opName;
                        edit_started(fxProgress, opName = getLocalizedStringCap("Transformingflip"));
                        synchronized (selectedKeys) {
                            for (String selection : selectedKeys) {
                                try {
                                    imageCollection.beginEdit(selection, false);
                                    Sprite edit = edit_newDrawingCanvas(selection);
                                    edit.setFlipEnabled(true, orient);
                                    edit.runValidate();
                                    edit_saveCanvas(edit, selection);
                                    edit_finalize(selection, true);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    interrupt = true;
                                } finally {
                                    imageCollection.endEdit(selection, false);
                                    edit_progress(fxProgress, 1);
                                }
                            }
                        }
                        edit_finished(fxProgress, opName, interrupt);
                    }
                };
                canvas.doTaskOnScreen(r);
            }
        };
    }

    private AbstractAction doRotateCW(final long fxProgress) {
        return doRotate90(fxProgress, false);
    }

    private AbstractAction doRotateCCW(final long fxProgress) {
        return doRotate90(fxProgress, true);
    }

    private AbstractAction doRotate90(final long fxProgress, final boolean CCW) {
        return new AbstractAction(getLocalizedStringCap("rotate90degrees" + (CCW ? "CCW" : "CW"))) {

            public void actionPerformed(ActionEvent e) {
                Runnable r = new Runnable() {
                    public void run() {
                        if (!waitForAllPictures(imageCollection)) {
                            return;
                        }
                        String opName = getLocalizedStringCap("Transformingrotation");
                        boolean interrupt = false;
                        edit_started(fxProgress, opName);
                        synchronized (selectedKeys) {
                            for (String selection : selectedKeys) {
                                try {
                                    imageCollection.beginEdit(selection, false);
                                    Sprite edit = edit_newDrawingCanvas(selection);
                                    AffineTransform tx = AffineTransform.getRotateInstance((CCW ? -1 : 1) * Math.PI / 2.0, edit.getBounds().getCenterX(), edit.getBounds().getCenterY());
                                    Rectangle2D txBds = tx.createTransformedShape(edit.getBounds()).getBounds2D();
                                    tx.preConcatenate(AffineTransform.getTranslateInstance(-txBds.getX(), -txBds.getY()));
                                    edit.setTX(tx);
                                    edit.runValidate();
                                    edit_saveCanvas(edit, selection);
                                    edit_finalize(selection, true);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    interrupt = false;
                                } finally {
                                    imageCollection.endEdit(selection, false);
                                    edit_progress(fxProgress, 1);
                                }
                            }
                        }
                        edit_finished(fxProgress, opName, interrupt);
                    }
                };
                canvas.doTaskOnScreen(r);
            }
        };
    }

    private AbstractAction doCrop(final String[] filesKeys) {
        return new AbstractAction(getLocalizedStringCap("Crop!"), cropIcon) {
            public void actionPerformed(ActionEvent e) {
                if (!picView.isEnabled()) {
                    return;
                }
                final String current = filesKeys[navPane.getCurrentStep()];
                final Rectangle cropB = imageCollectionInfo.get(current).containsKey(INFO_CROPRECT) ? (Rectangle) imageCollectionInfo.get(current).get(INFO_CROPRECT) : null;
                if (cropB instanceof Rectangle) {
                    if (cropB.isEmpty()) {
                        return;
                    }
                    Runnable run = new Runnable() {
                        public void run() {
                            if (!waitForAllPictures(imageCollection)) {
                                return;
                            }
                            boolean interrupt = false;
                            String op = getLocalizedStringCap("Cropping");
                            currentProcessThumb.setIcon(cropIcon_64);
                            long wait = UIMessage.newProgress(0, 100, picView);
                            JDialog pp = showLightPopupActivity(op, currentProcessThumb, wait, env.THREADING_MULTI.isEnv() ? Thread.currentThread() : null, UIMessage.UI_TOP_RIGHT);
                            edit_started(wait, op);

                            Rectangle cropRealB;
                            try {
                                imageCollection.beginEdit(current, false);
                                Sprite edit = edit_newDrawingCanvas(current);
                                int selectedZoom = imageCollectionInfo.get(current).containsKey(INFO_ZOOM) ? (Integer) imageCollectionInfo.get(current).get(INFO_ZOOM) : 100;
                                /**
                                 * displayed picture may have been scaled by the
                                 * user, get real Bounds
                                 */
                                AffineTransform zmTx = AffineTransform.getScaleInstance(100.0 / (double) selectedZoom, 100.0 / (double) selectedZoom);
                                /* clip to raster bounds intersection with cropping */
                                cropRealB = edit.getBounds().intersection(zmTx.createTransformedShape(cropB).getBounds());
                        
                                boolean doTiled = false;//edit.isTilesAvailable();
                                if (doTiled) {
                                    Sprite dst = (Sprite) edit.clone();
                                    ImageReader r = edit.getTileReader(false);
                                    dst.setSize(cropRealB.getSize());
                                    dst.runValidate();
                                    try {
                                        ImageWriter w = dst.prepareTileWrite(false);
                                        ImageWriteParam iwp = dst.getTileWriterParams(w);
                                        Dimension numTiles = new Dimension(edit.getNumTileX(), edit.getNumTileY());
                                        Dimension tiles = new Dimension(SpriteIO._WRITE_TILES_DIMENSION);
                                        if (w.canReplacePixels(0)) {
                                            /*NEVER ! w.prepareReplacePixels(0, new Rectangle(sr.getSize()));*/
                                            for (int x = 0; x < numTiles.width; x++) {
                                                for (int y = 0; y < numTiles.height; y++) {
                                                    /**
                                                     * read src tile into buffer
                                                     */
                                                    Rectangle srcROIB = new Rectangle(x * tiles.width, y * tiles.height, tiles.width, tiles.height).intersection(new Rectangle(r.getWidth(0), r.getHeight(0)));
                                                    ImageReadParam irp = edit.getTileReaderParams(r);
                                                    srcROIB = srcROIB.intersection(cropRealB);
                                                    if (srcROIB.isEmpty()) {
                                                        continue;
                                                    }
                                                    irp.setSourceRegion(srcROIB);
                                                    /**
                                                     * place dst bounds into
                                                     * cropping coords
                                                     */
                                                    Rectangle dstROIB = new Rectangle(srcROIB.x - cropRealB.x, srcROIB.y - cropRealB.y, srcROIB.width, srcROIB.height);
                                                    /**
                                                     * apply buffer to output
                                                     */
                                                    System.out.println("cropOp tiled : src : " + srcROIB.getWidth() + "x" + srcROIB.getHeight() + " crop : " + cropRealB);
                                                    if (!dstROIB.isEmpty()) {
                                                        iwp.setDestinationOffset(dstROIB.getLocation());
                                                        iwp.setSourceRegion(new Rectangle(dstROIB.getSize()));
                                                        w.prepareReplacePixels(0, dstROIB);
                                                        BufferedImage srcBuffer = r.read(0, irp);
                                                        if (srcBuffer.getType() != dst._type) {
                                                            srcBuffer = dst.convertToBuffered((RenderedImage) srcBuffer, picView, true);
                                                        }
                                                        edit_buffer(srcBuffer);
                                                        w.replacePixels(srcBuffer.getData(), iwp);
                                                        w.endReplacePixels();
                                                        edit_clearBuffer(srcBuffer);
                                                    }
                                                }
                                            }
                                        } else {
                                            doTiled = false;
                                        }
                                        dst.endTileWrite(w);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                        doTiled = false;
                                    } finally {
                                        ((ImageInputStream) r.getInput()).flush();
                                        r.dispose();
                                        ((ImageInputStream) r.getInput()).close();
                                        if (doTiled) {
                                            edit = dst;
                                        }
                                    }
                                }
                                if (!doTiled) {
                                    edit.setTileModeEnabled(false);
                                    edit.runValidate();
                                    BufferedImage src = edit.toBuffered();
                                    edit_buffer(src);
                                    edit_clearCanvas(edit);
                                    edit.setSize(cropRealB.getSize());
                                    edit.runValidate();
                                    BufferedImage i;
                                    System.out.println("cropOp untiled : src : " + src.getWidth() + "x" + src.getHeight() + " crop : " + cropRealB);
                                    edit_copyImageOnCanvas(i = src.getSubimage(cropRealB.x, cropRealB.y, cropRealB.width, cropRealB.height), edit);
                                    i.flush();
                                    edit_clearBuffer(src);
                                }
                                edit_saveCanvas(edit, current);
                                edit_finalize(current, true);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                interrupt = true;
                            } finally {
                                imageCollection.endEdit(current, false);
                                edit_finished(wait, op, interrupt);
                                pp.setVisible(false);
                                UIMessage.kill(wait);
                            }
                        }
                    };
                    canvas.doTaskOnScreen(run);
                }
            }
        };
    }

    private RenderedImage doLookupTable(BufferedImage img, float rescale, int offset) {
        if (img == null) {
            log("noimage", LVL.SYS_ERR);
            return null;
        }
        SampleModel model = img.getSampleModel();
        /* Setup a standard window-level lookup table. each band has 256 values indexed from 0 to 255*/
        byte[][] tableData = new byte[model.getNumBands()][256];
        for (int i = 0; i < 256; i++) {
            for (int band = 0; band < tableData.length; band++) {
                if (band > 2 && tableData.length > 3) /**
                 * alpha from abgr kept from modifications
                 */
                {
                    tableData[band][i] = (byte) i;
                } else {
                    tableData[band][i] = (byte) ((int) Math.max(0, Math.min(255, i * rescale + offset)));
                }
            }
        }
        /* Build a byte lookup table for the image. */
        return new LookupOp(new ByteLookupTable(0, tableData), null).filter(img, null);
    }

    /**
     * Retrieves a histogram for the image.
     */
    private RenderedOp doHistogram(RenderedImage img) {

        /* Get the band count.*/
        int numBands = img.getSampleModel().getNumBands();
        int[] binCount = new int[numBands];
        for (int b = 0; b < numBands; b++) {
            binCount[b] = (int) Math.round(Math.pow(2, img.getSampleModel().getSampleSize(b)));
        }
        /* Allocate histogram memory.*/
        int[] numBins = new int[numBands];
        double[] lowValue = new double[numBands];
        double[] highValue = new double[numBands];
        for (int i = 0; i < numBands; i++) {
            numBins[i] = binCount[i];
            lowValue[i] = 0.0;
            highValue[i] = 255.0;
        }

        ParameterBlock pb = new ParameterBlock();
        pb.addSource(img);               /* Specify the source image*/

        pb.add(null);                      /* No ROI*/

        pb.add(1);                         /* Sampling*/

        pb.add(1);                         /* periods*/

        pb.add(numBins);
        pb.add(lowValue);
        pb.add(highValue);
        /* Perform the histogram operation. image is kept untouched.*/
        RenderedOp image2 = ImageN.create("histogram", pb, Sprite._getRenderingHints());

        /* Retrieve the histogram.*
         hist = (Histogram) histImage.getProperty("histogram");*/
        return image2;
    }

    /**
     * @see {@link http://www.roborealm.com/help/Normalize.php}
     */
    private RenderedOp doHistogramNormalize(RenderedImage img) throws InterruptedException {
        img = doHistogram(img);
        edit_buffer(img);
        /* Get the band count.*/
        int numBands = img.getSampleModel().getNumBands();
        int[] binCount = new int[numBands];
        for (int b = 0; b < numBands; b++) {
            binCount[b] = (int) Math.round(Math.pow(2, img.getSampleModel().getSampleSize(b)));
        }
        /* Create a normalization CDF. */
        double[] mean = new double[]{128.0, 128.0, 128.0};
        double[] stDev = new double[]{64.0, 64.0, 64.0};
        float[][] CDFnorm = new float[numBands][];

        for (int b = 0; b < numBands; b++) {
            CDFnorm[b] = new float[binCount[b]];
            double mu = mean[b];
            double twoSigmaSquared = 2.0 * stDev[b] * stDev[b];
            CDFnorm[b][0]
                    = (float) Math.exp(-mu * mu / twoSigmaSquared);
            for (int i = 1; i < binCount[b]; i++) {
                double deviation = i - mu;
                CDFnorm[b][i] = CDFnorm[b][i - 1] + (float) Math.exp(-deviation * deviation / twoSigmaSquared);
            }
        }
        for (int b = 0; b < numBands; b++) {
            double CDFnormLast = CDFnorm[b][binCount[b] - 1];
            for (int i = 0; i < binCount[b]; i++) {
                CDFnorm[b][i] /= CDFnormLast;
            }
        }

        /* Create a histogram-normalized image. */
        ParameterBlock pb = new ParameterBlock();
        pb.addSource(img);
        pb.add(CDFnorm);
        RenderedOp image2 = ImageN.create("matchcdf", pb, Sprite._getRenderingHints());
        edit_clearBuffer(img);
        return image2;
    }

    /**
     * @see {@link http://www.roborealm.com/help/Equalize}
     */
    private RenderedOp doHistogramEqualize(RenderedImage img) throws InterruptedException {
        img = doHistogram(img);
        edit_buffer(img);
        /* Get the band count.*/
        int numBands = img.getSampleModel().getNumBands();
        int[] binCount = new int[numBands];
        for (int b = 0; b < numBands; b++) {
            binCount[b] = (int) Math.round(Math.pow(2, img.getSampleModel().getSampleSize(b)));
        }

        /* create equalization CDF */
        float[][] CDFeq = new float[numBands][];
        for (int b = 0; b < numBands; b++) {
            CDFeq[b] = new float[binCount[b]];
            for (int i = 0; i < binCount[b]; i++) {
                CDFeq[b][i] = (float) (i + 1) / (float) binCount[b];
            }
        }

        /* Create a histogram-equalized image.*/
        ParameterBlock pb = new ParameterBlock();
        pb.addSource(img);
        pb.add(CDFeq);
        RenderedOp image2 = ImageN.create("matchcdf", pb, Sprite._getRenderingHints());
        edit_clearBuffer(img);
        return image2;
    }

    private RenderedOp doEnhance(RenderedImage img) throws InterruptedException {
        return doHistogramEqualize(doHistogramNormalize(img));
    }

    private ImageCollectionRenderer navRenderer;

    public JFrame getFrame() {
        return !applet ? (JFrame) mainFrame : null;
    }

    public JApplet getApplet() {
        return applet ? (JApplet) mainFrame : null;
    }

    private static String cF(String capitalizeFirstLetter) {
        /**
         * is it HTML ? return same
         */
        if (capitalizeFirstLetter.startsWith("<")) {
            return capitalizeFirstLetter;
        } else {
            /**
             * make first letter upper case
             */
            return capitalizeFirstLetter.substring(0, 1).toUpperCase(Locale.ENGLISH) + capitalizeFirstLetter.substring(1);
        }
    }

    public static String getLocalizedStringCap(String localizedBundleKey) {
        return cF(LocalizedResources.localized.getString(localizedBundleKey));
    }

    public void initComponents() {
        try {
            ((JFrame) mainFrame).setTitle(getLocalizedStringCap("ImageMultiConverterandBrowser"));
            ((JFrame) mainFrame).setIconImage((Image) Sprite._loadImage(getClass().getResourceAsStream("icon.png"), true, "image/x-png", Sprite.MODE_JAVA2D, Sprite.DEFAULT_TYPE).get("image"));
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            init();
        }
    }

    private Map<String, Serializable> getAttributesSet(ImageCollection ic, String imageKey, boolean thumbnail, String... attrs) {
        ic.beginEdit(imageKey, thumbnail);
        Map<String, Serializable> store = thumbnail ? ic.getThumbnailPictureStoreAttributes(imageKey, attrs) : ic.getPictureStoreAttributes(imageKey, attrs);
        ic.endEdit(imageKey, thumbnail);
        return store;
    }

    public void init() {
        resize_scale = new JSpinner(new SpinnerNumberModel(100, 1, 500, 1));
        resize_width = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
        resize_height = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
        resize_widthCB = new JCheckBox(getLocalizedStringCap("fixedwidth"), false);
        resize_heightCB = new JCheckBox(getLocalizedStringCap("fixedheight"), false);
        resize_canvasCB = new JCheckBox(getLocalizedStringCap("canvasonly"), false);
        fcp = new FileChooserPane(mainFrame, defaultImageDirectory.getPath());
        fcp.setImagePreviewEnabled(!JXAenvUtils.env.OS_MAC.isEnv());
        fcp.getFiles().setAcceptAllFileFilterUsed(false);
        updateFileFilters(fcp, "rw");
        Container content = ((RootPaneContainer) mainFrame).getContentPane();
        content.setLayout(new BorderLayout());
        JSplitPane sPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
        fileInfo = new JXTitledPanel(getLocalizedStringCap("fileinformations"));
        jxLeft = new JXTaskPaneContainer();
        JSplitPane ssPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, jxLeft, fileInfo);
        sPane.setLeftComponent(ssPane);
        /**
         * task pane
         */
        tasks_resize = new JXTaskPane();
        tasks_imageParams = new JXTaskPane();
        tasks_fx = new JXTaskPane();
        tasks_db = new JXTaskPane();
        tasks_ftp = new JXTaskPane();
        tasks_resize.setTitle(getLocalizedStringCap("resize"));
        /*tasks_resize.setCollapsed(false);*/
        if (env.OS_LINUX.isEnv()) {
            try {
                _callback("setCollapsed", tasks_resize, new Object[]{true}, new Class[]{boolean.class});
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                _callback("setExpanded", tasks_resize, new Object[]{false}, new Class[]{boolean.class});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        tasks_fx.setTitle(getLocalizedStringCap("effects"));
        /*tasks_fx.setCollapsed(false);*/
        if (env.OS_LINUX.isEnv()) {
            try {
                _callback("setCollapsed", tasks_fx, new Object[]{true}, new Class[]{boolean.class});
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                _callback("setExpanded", tasks_fx, new Object[]{false}, new Class[]{boolean.class});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        tasks_db.setTitle(getLocalizedStringCap("albums"));
        /*tasks_db.setCollapsed(true);*/
        if (env.OS_LINUX.isEnv()) {
            try {
                _callback("setCollapsed", tasks_db, new Object[]{false}, new Class[]{boolean.class});
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                _callback("setExpanded", tasks_db, new Object[]{true}, new Class[]{boolean.class});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        tasks_ftp.setTitle(getLocalizedStringCap("filetransfer"));
        /*tasks_ftp.setCollapsed(false);*/
        if (env.OS_LINUX.isEnv()) {
            try {
                _callback("setCollapsed", tasks_ftp, new Object[]{true}, new Class[]{boolean.class});
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                _callback("setExpanded", tasks_ftp, new Object[]{false}, new Class[]{boolean.class});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        tasks_db.add(action_search = new AbstractAction(getLocalizedStringCap("search") + "...", UIMessage._getIcon(UIMessage.SEARCH_TYPE, true)) {
            public void actionPerformed(ActionEvent e) {
                final JTextField jtf;
                JPanel panel = new JPanel(new BorderLayout(), true);
                panel.add(new JLabel(getLocalizedStringCap("entereitheroneorseveralkeywords")), BorderLayout.NORTH);
                panel.add(jtf = new JTextField(), BorderLayout.CENTER);
                panel.add(new JButton(new AbstractAction(getLocalizedStringCap("Findit!")) {
                    public void actionPerformed(ActionEvent e) {
                        String[] findString = jtf.getText().split(" ");
                        setImageCollection(newImageCollection(getLocalizedStringCap("recentsearching")));
                        ImageCollection ic = null;
                        try {
                            synchronized (imageCollectionDB) {
                                for (String k : imageCollectionDB.keySet()) {
                                    ic = imageCollectionDB.get(k);
                                    if (!waitForAllPictures(ic)) {
                                        return;
                                    }
                                    SortedMap<String, Map<Integer, Serializable>> ici = imageCollectionInfoDB.get(k);
                                    synchronized (ic) {
                                        for (String file : ic.keySet(false)) {
                                            boolean find = true;
                                            for (String s : findString) {
                                                find = find && file.substring(file.lastIndexOf(File.separator) + 1).toLowerCase().contains(s.toLowerCase());
                                            }
                                            if (find) {
                                                Map<String, Serializable> tbstore = getAttributesSet(ic, file, true, ImageCollection.ATTRIBUTE_MIME, ImageCollection.ATTRIBUTE_TYPE);
                                                Map<String, Serializable> store = getAttributesSet(ic, file, false, ImageCollection.ATTRIBUTE_MIME, ImageCollection.ATTRIBUTE_TYPE);
                                                ic.beginEdit(file, false);
                                                updateAddImage(imageCollection, file, ic.getCollectionSprite(file, false), tbstore, store);
                                                ic.endEdit(file, false);
                                                imageCollectionInfo.put(file, ici.get(file));
                                            }
                                        }
                                    }
                                }
                            }
                            updateCollectionDB();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        } finally {
                            callBrowserUpdate();
                        }
                    }
                }), BorderLayout.SOUTH);
                UIMessage.showLightPopupMessage(panel, null, picView, UIMessage.UI_CENTER);
            }
        });
        action_search.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        resizePanel = new JXPanel(true);
        resizePanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = c.HORIZONTAL;
        c.weightx = 1.0;
        c.gridwidth = c.REMAINDER;
        resizePanel.add(new JLabel(getLocalizedStringCap("scalezoom") + " :"), c);
        resizePanel.add(resize_scale, c);
        c.gridwidth = 1;
        resizePanel.add(resize_widthCB, c);
        resize_widthCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                resize_width.setEnabled(resize_widthCB.isSelected());
                resize_scale.setEnabled(!resize_heightCB.isSelected() && !resize_widthCB.isSelected());
            }
        });
        c.gridwidth = c.REMAINDER;
        resizePanel.add(resize_width, c);
        resize_width.setEnabled(resize_widthCB.isSelected());
        c.gridwidth = 1;
        resizePanel.add(resize_heightCB, c);
        resize_heightCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                resize_height.setEnabled(resize_heightCB.isSelected());
                resize_scale.setEnabled(!resize_heightCB.isSelected() && !resize_widthCB.isSelected());
            }
        });
        c.gridwidth = c.REMAINDER;
        resizePanel.add(resize_height, c);
        resize_height.setEnabled(resize_heightCB.isSelected());
        resizePanel.add(resize_canvasCB, c);
        final long resizeProgress = UIMessage.newProgress(0, selectedKeys.size(), mainFrame);
        UIMessage.getProgressBar(resizeProgress).setStringPainted(false);
        resizePanel.add(UIMessage.getProgressBar(resizeProgress), c);
        resizePanel.add(new JButton(action_resize = doResize(resizeProgress)), c);
        tasks_resize.add(resizePanel);
        dbPanel = new JPanel(new GridBagLayout(), true);
        dbList = new JComboBox();
        dbList.setEditable(true);
        dbList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (dbList.getSelectedItem() != null) {
                    System.out.println("dbList : " + dbList.getSelectedItem());
                    if (imageCollectionDB.containsKey(dbList.getSelectedItem())) {
                        setImageCollection(imageCollectionDB.get(dbList.getSelectedItem()), imageCollectionInfoDB.get(dbList.getSelectedItem()));
                        clearAllSelection();
                        updateImageCollectionListeners(imageCollection);
                        callBrowserUpdate();
                    } else {
                        /**
                         * name change after edit
                         */
                        String oldK = null;
                        synchronized (imageCollectionDB) {
                            for (String k : imageCollectionDB.keySet()) {
                                if (imageCollectionDB.get(k).equals(imageCollection)) {
                                    oldK = k;
                                    break;
                                }
                            }
                        }
                        if (oldK != null) {
                            imageCollectionDB.remove(oldK);
                            imageCollectionInfoDB.remove(oldK);
                        }
                        /**
                         *
                         */
                        imageCollectionDB.put((String) dbList.getSelectedItem(), imageCollection);
                        imageCollectionInfoDB.put((String) dbList.getSelectedItem(), imageCollectionInfo);
                        storeApplication(applicationStore);
                    }
                }
            }
        });
        c = new GridBagConstraints();
        c.gridwidth = c.REMAINDER;
        c.fill = c.BOTH;
        c.weightx = 1.0;
        dbPanel.add(dbList, c);
        dbPanel.add(new JButton(action_removeCollection = new AbstractAction(getLocalizedStringCap("Remove")) {
            public void actionPerformed(ActionEvent e) {
                if (!picView.isEnabled()) {
                    return;
                }
                if (JOptionPane.OK_OPTION != UIMessage.showConfirmDialog(picView, getLocalizedStringCap("suretodelete_message"), getLocalizedStringCap("suretodelete_title"), JOptionPane.OK_CANCEL_OPTION)) {
                    return;
                }
                ImageCollection collection = imageCollectionDB.get(dbList.getSelectedItem());
                for (String k : collection.keySet(false)) {
                    File f = getBackupFile(k);
                    if (f != null) {
                        if (f.exists()) {
                            f.delete();
                        }
                    }
                }
                imageCollectionDB.remove(dbList.getSelectedItem());
                imageCollectionInfoDB.remove(dbList.getSelectedItem());
                browserMode = MODE_GRID;
                if (imageCollectionDB.isEmpty()) {
                    setImageCollection(newImageCollection());
                } else {
                    SortedMap<String, ImageCollection> map = imageCollectionDB.headMap((String) dbList.getSelectedItem());
                    if (map.isEmpty()) {
                        map = imageCollectionDB.tailMap((String) dbList.getSelectedItem());
                    }
                    setImageCollection(imageCollectionDB.get(map.lastKey()), imageCollectionInfoDB.get(map.lastKey()));
                }
                updateCollectionDB();
                storeApplication(applicationStore);
            }
        }), c);
        tasks_db.add(dbPanel);
        final long fxProgress = UIMessage.newProgress(0, selectedKeys.size(), mainFrame);
        UIMessage.getProgressBar(fxProgress).setStringPainted(false);
        tasks_fx.add(action_fxFlipH = doFlipH(fxProgress));
        tasks_fx.add(action_fxFlipV = doFlipV(fxProgress));
        tasks_fx.add(action_fxRotateCW = doRotateCW(fxProgress));
        tasks_fx.add(action_fxRotateCCW = doRotateCCW(fxProgress));
        JPanel fxProgressPanel = new JPanel(new GridBagLayout(), true);
        c = new GridBagConstraints();
        c.fill = c.BOTH;
        c.weightx = 1.0;
        fxProgressPanel.add(UIMessage.getProgressBar(fxProgress), c);
        tasks_fx.add(fxProgressPanel);
        JPanel ftpPanel = new JPanel(new GridBagLayout(), true);
        c = new GridBagConstraints();
        c.fill = c.BOTH;
        c.weightx = 1.0;
        final JTextField ftpHostname = new JTextField(LocalizedResources.localized.getString("ftp.yourhost.com"));
        final JTextField ftpPort = new JTextField("21");
        final JTextField ftpPath = new JTextField("/");
        final JTextField ftpUser = new JTextField("anonymous");
        final JPasswordField ftpPass = new JPasswordField("password");
        c.gridwidth = 1;
        ftpPanel.add(new JLabel(getLocalizedStringCap("host") + " :"));
        ftpPanel.add(ftpHostname, c);
        c.gridwidth = c.REMAINDER;
        ftpPanel.add(ftpPort, c);
        c.gridwidth = 1;
        ftpPanel.add(new JLabel(getLocalizedStringCap("path") + " :"));
        c.gridwidth = c.REMAINDER;
        ftpPanel.add(ftpPath, c);
        c.gridwidth = 1;
        ftpPanel.add(new JLabel(getLocalizedStringCap("user") + " :"));
        c.gridwidth = c.REMAINDER;
        ftpPanel.add(ftpUser, c);
        c.gridwidth = 1;
        ftpPanel.add(new JLabel(getLocalizedStringCap("pass") + " :"));
        c.gridwidth = c.REMAINDER;
        ftpPanel.add(ftpPass, c);
        log = new LogArea();
        log.setLogStderrEnabled(!_debugSys && !_debug);
        /*log.getConsole().setOutput(System.out);*/
        tasks_ftp.add(new AbstractAction(getLocalizedStringCap("openlog") + "...") {
            public void actionPerformed(ActionEvent e) {
                JPanel panel = new JPanel(new GridBagLayout(), true);
                GridBagConstraints c = new GridBagConstraints();
                c.fill = c.BOTH;
                c.weightx = 1.0;
                c.weighty = 1.0;
                log.setPreferredSize(new Dimension(400, 340));
                panel.add(log, c);
                new UIMessage(false, panel, null);
            }
        });
        final long ftpProgress = UIMessage.newProgress(0, selectedKeys.size(), mainFrame);
        UIMessage.getProgressBar(ftpProgress).setStringPainted(false);
        ftpPanel.add(UIMessage.getProgressBar(ftpProgress), c);
        tasks_ftp.add(ftpPanel);
        tasks_ftp.add(new JButton(action_sendFTP = new AbstractAction(getLocalizedStringCap("sendphotos")) {
            public void actionPerformed(ActionEvent e) {
                Runnable r = new Runnable() {
                    public void run() {
                        if (!waitForAllPictures(imageCollection)) {
                            return;
                        }
                        log.pushNewLog(getLocalizedStringCap("Sendingfiles") + "...");
                        String opName = getLocalizedStringCap("SendingFTP");
                        edit_started(ftpProgress, opName);
                        String hostname = "";
                        int port = 0;
                        String path = "";
                        Ftp ftp = null;
                        String user = "";
                        String pass = "";

                        log.pushNewLog(getLocalizedStringCap("openingremotetcpconnection") + " :");
                        log.pushNewLog(getLocalizedStringCap("ftpselected"));
                        hostname = ftpHostname.getText();
                        port = Integer.parseInt(ftpPort.getText());
                        path = ftpPath.getText();
                        user = ftpUser.getText();
                        pass = new String(ftpPass.getPassword());
                        ftp = new Ftp();

                        ftp.getContext().getConsole().addLogListener(log);
                        boolean interrupt = false;
                        try {
                            if (ftp.connect(hostname, port)) {
                                if (!ftp.login(user, pass)) {
                                    throw new Exception(getLocalizedStringCap("Wronguserandorpassword."));
                                }
                                ftp.mkdir(path);
                                if (!ftp.cd(path)) {
                                    throw new Exception(getLocalizedStringCap("Unabletoreach") + " " + path + ".");
                                }
                            } else {
                                throw new Exception(getLocalizedStringCap("Failedtoconnect."));
                            }
                            log.pushNewLog(getLocalizedStringCap("done") + ".");

                            Vector<File> v = new Vector<File>();
                            waitForAllPictures(imageCollection);
                            synchronized (selectedKeys) {
                                for (String selection : selectedKeys) {
                                    imageCollection.beginEdit(selection, false);
                                    Sprite sp = imageCollection.getCollectionSprite(selection, false);
                                    v.add((File) sp.getSource());
                                    imageCollection.endEdit(selection, false);
                                }
                            }
                            File[] files = v.toArray(new File[]{});
                            for (int i = 0; i < files.length; i++) {
                                JProgressBar filesProgressBar = UIMessage.getProgressBar(ftpProgress);
                                String name = files[i].getName();
                                FtpOutputStream tcpOutStream = new FtpOutputStream(new FtpFile(name, ftp));
                                byte[] buffer = new byte[512];
                                int len = (int) files[i].length();
                                int rB = 0;
                                RandomAccessFile raf = new RandomAccessFile(files[i], "r");
                                FileInputStream bis = new FileInputStream(raf.getFD());
                                filesProgressBar.setValue(0);
                                filesProgressBar.setMaximum(len);
                                while ((rB = bis.read(buffer)) != -1) {
                                    tcpOutStream.write(buffer, 0, rB);
                                    filesProgressBar.setValue(filesProgressBar.getValue() + rB);
                                }
                                raf.close();
                                tcpOutStream.close();
                                edit_progress(ftpProgress, 1);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            log.pushNewLog(ex.getMessage());
                            interrupt = true;
                        } finally {
                            ftp.disconnect();
                            edit_finished(ftpProgress, opName, interrupt);
                        }
                    }
                };
                canvas.doTaskOnScreen(r);
            }
        }));
        /**
         * Picture parameters
         */
        tasks_imageParams.setTitle(getLocalizedStringCap("imageParams"));
        saveAccess = new SaveAccessory();
        tasks_imageParams.add(saveAccess.getPanelOpt());
        /*
         task pane container
         */
        jxLeft.add(tasks_imageParams);
        jxLeft.add(tasks_resize);
        jxLeft.add(tasks_fx);
        jxLeft.add(tasks_db);
        jxLeft.add(tasks_ftp);
        /**
         * Image Collection Browser
         */
        picView = new JPanel(true) {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                Graphics2D g2 = Sprite.wrapRendering(g);
                if (!selectionRect.isEmpty()) {
                    Stroke stk = g2.getStroke();
                    g2.setStroke(new BasicStroke(0, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{5}, 0));
                    g2.draw(selectionRect);
                    Composite cps = g2.getComposite();
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .3f));
                    g2.fill(selectionRect);
                    g2.setComposite(cps);
                    g2.setStroke(stk);
                }
            }
        };
        picView.setTransferHandler(new TransferHandler() {

            /*Thread t_export = null;*/
            long hash = System.nanoTime();

            @Override
            public int hashCode() {
                return (int) hash;
            }

            public void exportToClipboard(JComponent pcomp, Clipboard pclip, int action) throws IllegalStateException {
                final Clipboard clip = pclip;
                final JComponent comp = pcomp;
                TransferHandler th = comp.getTransferHandler();
                if (th != null) {
                    if (th.hashCode() == hashCode()) {
                        switch (action) {
                            case COPY:
                                Runnable r = new Runnable() {
                                    public void run() {
                                        if (!waitForAllPictures(imageCollection)) {
                                            return;
                                        }
                                        final DataFlavor[] flavors = getTransferDataFlavors();
                                        final Map<DataFlavor, Boolean> supported = Collections.synchronizedMap(new HashMap<DataFlavor, Boolean>());
                                        final Map<DataFlavor, Object> data = Collections.synchronizedMap(new HashMap<DataFlavor, Object>());
                                        for (DataFlavor df : flavors) {
                                            supported.put(df, isDataFlavorSupported(df));
                                            if (isDataFlavorSupported(df)) {
                                                try {
                                                    data.put(df, getTransferData(df));
                                                } catch (Exception ex) {
                                                    ex.printStackTrace();
                                                }
                                            }
                                        }
                                        Transferable transfer = new Transferable() {
                                            public DataFlavor[] getTransferDataFlavors() {
                                                return flavors;
                                            }

                                            public boolean isDataFlavorSupported(DataFlavor arg0) {
                                                return supported.get(arg0);
                                            }

                                            public Object getTransferData(DataFlavor arg0) throws UnsupportedFlavorException, IOException {
                                                if (data.containsKey(arg0)) {
                                                    return data.get(arg0);
                                                }
                                                throw new UnsupportedFlavorException(arg0);
                                            }
                                        };
                                        clip.setContents((Transferable) transfer, IMCBrowser.this);
                                    }
                                };
                                canvas.doTaskOnScreen(r);
                                break;
                            default:
                                break;
                        }
                        return;
                    }
                }
            }

            public boolean importData(JComponent comp, final Transferable t) {
                TransferHandler th = comp.getTransferHandler();
                if (th != null) {
                    if (th.hashCode() == hashCode()) {
                        DataFlavor df = null;
                        for (DataFlavor supportedDf : importFlavors) {
                            for (DataFlavor tdf : t.getTransferDataFlavors()) {
                                df = (supportedDf.equals(tdf)) ? tdf : df;
                                if (df instanceof DataFlavor) {
                                    break;
                                }
                            }
                            if (df instanceof DataFlavor) {
                                break;
                            }
                        }
                        if (df == null) {
                            return false;
                        }
                        try {
                            open(t.getTransferData(df));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            return false;
                        } finally {
                            return true;
                        }
                    }
                }
                return false;
            }
        });
        picView.setDropTarget(new DropTarget(picView, new DropTargetAdapter() {
            @Override
            public void dragExit(DropTargetEvent dte) {
                super.dragExit(dte);
                dte.getDropTargetContext().getComponent().setCursor(Cursor.getDefaultCursor());
            }

            @Override
            public void dragOver(DropTargetDragEvent dtde) {
                int dropAct = dtde.getDropAction();
                System.err.println("DROPEVENT : " + dtde);
                dtde.getDropTargetContext().getComponent().setCursor(dropAct == java.awt.dnd.DnDConstants.ACTION_LINK || dropAct == java.awt.dnd.DnDConstants.ACTION_COPY || dropAct == java.awt.dnd.DnDConstants.ACTION_MOVE || dropAct == java.awt.dnd.DnDConstants.ACTION_COPY_OR_MOVE ? DragSource.DefaultCopyDrop : Cursor.getDefaultCursor());
            }

            public void drop(DropTargetDropEvent dtde) {
                String op = getLocalizedStringCap("copyfromclipboard");
                /*long wait = UIMessage.displayWaiting(op, currentProcessThumb, browser);*/
                JDialog p = showLightPopupActivity(op, currentProcessThumb, null, UIMessage.UI_TOP_RIGHT);
                dtde.acceptDrop(java.awt.dnd.DnDConstants.ACTION_COPY);
                dtde.dropComplete(picView.getTransferHandler().importData(picView, dtde.getTransferable()));
                dtde.getDropTargetContext().getComponent().setCursor(Cursor.getDefaultCursor());
                /*UIMessage.kill(wait);*/
                p.setVisible(false);
            }
        }));
        picView.setBorder(BorderFactory.createTitledBorder(getLocalizedStringCap("photosalbumbrowser")));
        sPane.setRightComponent(picView);
        content.add(sPane, BorderLayout.CENTER);
        /**
         * status bar
         */
        JPanel statusPanel = new JPanel(new GridBagLayout());
        GridBagConstraints stPanelCnst = new GridBagConstraints();
        stPanelCnst.fill = stPanelCnst.HORIZONTAL;
        String[] lstr = LocalizedResources.localizedGetStringArray("label5", 2);
        MemStatusToolkit mst = new MemStatusToolkit();
        stPanelCnst.weightx = 1.;
        statusPanel.add(mst.getHeapMemStatus(), stPanelCnst);
        rLed = new Led(Led.READ);
        Dimension ledSize = new Dimension(20, 20);
        rLed.setPreferredSize(ledSize);
        wLed = new Led(Led.WRITE);
        wLed.setPreferredSize(ledSize);
        stPanelCnst.weightx = .1;
        statusPanel.add(rLed, stPanelCnst);
        statusPanel.add(wLed, stPanelCnst);
        statusDiskUsageLabel = /*_JTimeProgressBar*/ new JLabel(getLocalizedStringCap("diskusage") + " : 0.0 " + LocalizedResources.localized.getString("MB"));
        stPanelCnst.weightx = .25;
        statusPanel.add(statusDiskUsageLabel, stPanelCnst);
        statusLabel = new JLabel(statusLabelDefaultText = lstr[0] + " " + LocalizedResources.localized.getString("ImageMultiConverterandBrowser") + " " + lstr[1] + getClass().getPackage().getSpecificationVersion() + " !");
        statusPanel.add(statusLabel, stPanelCnst);

        /**
         * thumb size slider
         */
        final JSlider thumbSlide = new JSlider(JSlider.HORIZONTAL, 20, 300, 100);
        thumbSlide.setMajorTickSpacing(100);
        thumbSlide.setMinorTickSpacing(20);
        thumbSlide.setSnapToTicks(true);
        thumbSlide.setPaintLabels(true);
        thumbSlide.setPaintTicks(true);

        thumbSlide.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                int value = thumbSlide.getValue();
                thumbSize.setSize(Math.max((value / 100) * SpriteIO._WRITE_TILES_DIMENSION.width, SpriteIO._MIN_THUMBNAILS_SIZE), Math.max((value / 100) * SpriteIO._WRITE_TILES_DIMENSION.height, SpriteIO._MIN_THUMBNAILS_SIZE));
                /**
                 * asynch update !
                 */
                callBrowserUpdate();
            }
        });
        stPanelCnst.gridwidth = stPanelCnst.REMAINDER;
        statusPanel.add(thumbSlide, stPanelCnst);
        content.add(statusPanel, BorderLayout.SOUTH);
        content.validate();
        /**
         * menu bar
         */
        JMenuBar menuBar = new JMenuBar();
        JMenu menu;
        menuBar.add(menu = new JMenu(getLocalizedStringCap("File")));
        menu.add(makeRecentFilesMenu());
        menu.addSeparator();
        action_new = new AbstractAction(getLocalizedStringCap("newalbum") + "...", UIMessage._getIcon(UIMessage.DOCUMENT_TYPE, true)) {
            public void actionPerformed(ActionEvent e) {
                if (!picView.isEnabled()) {
                    return;
                }
                setImageCollection(newImageCollection());
                updateCollectionDB();
                storeApplication(applicationStore);
                callBrowserUpdate();
            }
        };
        action_new.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        tasks_db.add(action_new);
        menu.add(action_new);
        menu.addSeparator();
        action_open = new AbstractAction(getLocalizedStringCap("open") + "...", UIMessage._getIcon(UIMessage.LOAD_TYPE, true)) {
            public void actionPerformed(ActionEvent e) {
                if (!picView.isEnabled()) {
                    return;
                }
                fcp.setModeOpen(true);
                fcp.setMultiSelectionEnabled(true);
                updateFileFilters(fcp, "r");
                final File[] files = fcp.getFile();
                if (files.length > 0) {

                    Runnable r = new Runnable() {
                        public void run() {
                            String op = getLocalizedStringCap("Opening");
                            /*long wait = UIMessage.displayWaiting(op, currentProcessThumb, browser);*/
                            long wait = UIMessage.newProgress(0, 100, picView);
                            JDialog pp = showLightPopupActivity(op, currentProcessThumb, wait, env.THREADING_MULTI.isEnv() ? Thread.currentThread() : null, UIMessage.UI_TOP_RIGHT);
                            int fstate = applet ? 0 : ((JFrame) mainFrame).getExtendedState();
                            edit_started(wait, op, files.length);
                            for (File f : files) {
                                open(f, true, true);
                                doBrowserUpdate();
                                edit_progress(wait, 1, files.length);
                            }
                            edit_finished(wait, op, false, files.length);
                            pp.setVisible(false);
                            UIMessage.kill(wait);
                            storeApplication(applicationStore);
                        }
                    };
                    canvas.doTaskOnScreen(r);
                }
            }
        };
        action_open.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(action_open);
        menu.addSeparator();
        action_save = new AbstractAction(getLocalizedStringCap("save"), UIMessage._getIcon(UIMessage.STORE_TYPE, true)) {
            public void actionPerformed(ActionEvent e) {
                if (!selectedKeys.isEmpty()) {
                    final int cpr = saveAccess.getCompressionQualitySlider().getValue();
                    Runnable r = new Runnable() {
                        public void run() {
                            if (!waitForAllPictures(imageCollection)) {
                                return;
                            }
                            fcp.setModeSave(true);
                            Set<ImagePluginFilter> exts = updateFileFilters(fcp, "w");
                            String op = getLocalizedStringCap("Saving");
                            long wait = UIMessage.newProgress(0, 100, picView);
                            JDialog pp = showLightPopupActivity(op, currentProcessThumb, wait, env.THREADING_MULTI.isEnv() ? Thread.currentThread() : null, UIMessage.UI_TOP_RIGHT);
                            int fstate = applet ? 0 : ((JFrame) mainFrame).getExtendedState();
                            edit_started(wait, op);
                            Vector<String> sKeys = new Vector<String>(selectedKeys);
                            for (String path : sKeys) {
                                boolean saved = false;
                                for (ImagePluginFilter converter : exts) {
                                    for (String e : converter.exts) {
                                        if (path.toLowerCase().endsWith(e)) {
                                            save(new File(path), path, cpr);
                                            saved = true;
                                            break;
                                        }
                                    }
                                    if (saved) {
                                        break;
                                    }
                                }
                                if (!saved) {
                                    UIMessage.showLightPopupMessage(new JLabel(getLocalizedStringCap("Unsupportedunknownextensionforwriting") + " : " + path.substring(path.lastIndexOf(File.separator) + 1), UIMessage._getIcon(UIMessage.ERROR_TYPE, false), SwingConstants.LEADING), null, picView, UIMessage.UI_BOTTOM_RIGHT);
                                } else {
                                    open(new File(path), false, true);
                                    doBrowserUpdate();
                                    edit_progress(wait, 1);
                                }
                            }
                            edit_finished(wait, op, false);
                            pp.setVisible(false);
                            UIMessage.kill(wait);
                            storeApplication(applicationStore);
                        }
                    };
                    canvas.doTaskOnScreen(r);
                } else {
                    updateCurrentStatus(null, getLocalizedStringCap("Pleaseselectaphototosave!"));
                }
            }
        };
        action_save.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(action_save);
        action_saveAs = new AbstractAction(getLocalizedStringCap("saveas") + "...", UIMessage._getIcon(UIMessage.STORE_TYPE, true)) {
            public void actionPerformed(ActionEvent e) {
                if (!waitForAllPictures(imageCollection)) {
                    return;
                }
                fcp.setModeSave(true);
                fcp.getFiles().setAccessory(saveAccess.getPanelOpt());
                final Set<ImagePluginFilter> ffExts = updateFileFilters(fcp, "w");
                if (selectedKeys.size() == 1) {
                    final File[] files = fcp.getFile();
                    final int cpr = saveAccess.getCompressionQualitySlider().getValue();
                    tasks_imageParams.add(saveAccess.getPanelOpt());
                    tasks_imageParams.validate();
                    if (files.length > 0) {
                        Runnable r = new Runnable() {
                            public void run() {
                                boolean saved = false;
                                for (ImagePluginFilter c : ffExts) {
                                    for (String ext : c.exts) {
                                        if (files[0].getName().toLowerCase().endsWith(ext)) {
                                            save(files[0], selectedKeys.iterator().next(), cpr);
                                            saved = true;
                                            break;
                                        }
                                        if (saved) {
                                            break;
                                        }
                                    }
                                }
                                if (!saved) {
                                    UIMessage.showLightPopupMessage(new JLabel(getLocalizedStringCap("Unsupportedunknownextensionforwriting") + " : " + files[0].getName(), UIMessage._getIcon(UIMessage.ERROR_TYPE, false), SwingConstants.LEADING), null, picView, UIMessage.UI_BOTTOM_RIGHT);
                                } else {
                                    /*open(files[0], false);*/
                                    doBrowserUpdate();
                                    storeApplication(applicationStore);
                                }
                            }
                        };
                        canvas.doTaskOnScreen(r);
                    }
                } else if (selectedKeys.size() > 1) {
                    final File[] directory = fcp.getDirectory();
                    final int cpr = saveAccess.getCompressionQualitySlider().getValue();
                    if (directory.length > 0) {
                        String ext = "";
                        Vector ffs = new Vector();
                        for (FileFilter ff : fcp.getFiles().getChoosableFileFilters()) {
                            if (!ff.getDescription().equals(allFF.getDescription())) {
                                ffs.add(ff.getDescription());
                            }
                        }
                        String[] lstr = LocalizedResources.localizedGetStringArray("dialog1", 2);
                        String selection = (String) UIMessage.showSelectDialog(picView, lstr[0], lstr[1], ffs.toArray(), fcp.getFiles().getFileFilter().getDescription());
                        if (ffs.contains(selection)) {
                            for (FileFilter ff : fcp.getFiles().getChoosableFileFilters()) {
                                if (ff.getDescription().equals(selection)) {
                                    for (ImagePluginFilter c : ffExts) {
                                        for (String myExt : c.exts) {
                                            if (ff.accept(new File("myFile." + myExt))) {
                                                ext = myExt;
                                                break;
                                            }
                                        }
                                        if (!"".equals(ext)) {
                                            break;
                                        }
                                    }
                                }
                                if (!"".equals(ext)) {
                                    break;
                                }
                            }
                            final String nExt = ext;
                            Runnable r = new Runnable() {
                                public void run() {
                                    if (!waitForAllPictures(imageCollection)) {
                                        return;
                                    }
                                    String op = getLocalizedStringCap("Saving");
                                    long wait = UIMessage.newProgress(0, 100, picView);
                                    JDialog pp = showLightPopupActivity(op, currentProcessThumb, wait, env.THREADING_MULTI.isEnv() ? Thread.currentThread() : null, UIMessage.UI_TOP_RIGHT);
                                    edit_started(wait, op);
                                    Vector<String> sKeys = new Vector<String>(selectedKeys);
                                    for (final String file : sKeys) {
                                        File f = new File(file);
                                        String name = f.getName();
                                        name = name.substring(0, name.lastIndexOf(".") + 1) + nExt;
                                        File nF = new File(directory[0].getPath() + File.separator + name);
                                        save(nF, file, cpr);
                                        /*open(nF, false);
                                         doBrowserUpdate();*/
                                        edit_progress(wait, 1);
                                    }
                                    edit_finished(wait, op, false);
                                    pp.setVisible(false);
                                    UIMessage.kill(wait);
                                    storeApplication(applicationStore);
                                    doBrowserUpdate();
                                }
                            };
                            canvas.doTaskOnScreen(r);
                        }
                    }
                }
            }
        };
        action_saveAs.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, (env.OS_MAC.isEnv() ? InputEvent.ALT_DOWN_MASK : InputEvent.SHIFT_DOWN_MASK) | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(action_saveAs);
        menu.addSeparator();
        action_revertToOriginal = new AbstractAction(getLocalizedStringCap("reverttooriginal")) {
            public void actionPerformed(ActionEvent e) {
                String[] lstr = LocalizedResources.localizedGetStringArray("dialog7", 2);
                if (JOptionPane.CANCEL_OPTION == UIMessage.showConfirmDialog(picView, lstr[0], lstr[1], JOptionPane.OK_CANCEL_OPTION)) {
                    return;

                }
                Runnable r = new Runnable() {
                    public void run() {
                        if (!waitForAllPictures(imageCollection)) {
                            return;
                        }
                        synchronized (selectedKeys) {
                            for (String k : selectedKeys) {
                                File f = getBackupFile(k);
                                if (f != null) {
                                    open(f, k, false);
                                }
                            }
                        }
                        storeApplication(applicationStore);
                        callBrowserUpdate();
                    }
                };
                canvas.doTaskOnScreen(r);
            }
        };
        menu.add(action_revertToOriginal);
        menu.addSeparator();
        action_printFormat = new AbstractAction(getLocalizedStringCap("printFormat") + "...") {
            public void actionPerformed(ActionEvent e) {
                PageFormat currentFormat = (PageFormat) pjFormat.clone();
                do {
                    PageFormat validateFormat = marginPageFormat((PageFormat) pjFormat.clone());
                    pjFormat = pj.pageDialog(validateFormat);
                    if (pjFormat.equals(validateFormat)) {
                        break;
                    }
                    validateFormat = (PageFormat) pjFormat.clone();
                    pjFormat = pj.pageDialog(validateFormat);
                    if (pjFormat.equals(validateFormat)) {
                        pjFormat = currentFormat;
                        break;
                    } else {
                        currentFormat = validateFormat;
                    }
                } while (pjFormat.getWidth() != currentFormat.getWidth() && pjFormat.getHeight() != currentFormat.getHeight());
            }
        };
        menu.add(action_printFormat);
        action_print = new AbstractAction(getLocalizedStringCap("print") + "...", UIMessage._getIcon(UIMessage.PRINT_TYPE, true)) {

            /*Thread t_print = null;*/
            public void actionPerformed(ActionEvent e) {
                if (!picView.isEnabled()) {
                    return;
                }
                final String[] outputMode = LocalizedResources.localizedGetStringArray("papersizefit", 3);
                String[] lstr = LocalizedResources.localizedGetStringArray("dialog2", 2);
                final String selectedOutputMode = (String) UIMessage.showSelectDialog(mainFrame, new JLabel(lstr[0]), lstr[1], outputMode, outputMode[0]);
                if (selectedOutputMode == null) {
                    return;
                }
                if (pj.printDialog()) {
                    if (!waitForAllPictures(imageCollection)) {
                        return;
                    }
                    final String op = getLocalizedStringCap("Printing");
                    boolean interrupt = false;
                    long wait = UIMessage.newProgress(0, 100, picView);
                    JDialog pp = showLightPopupActivity(op, currentProcessThumb, wait, env.THREADING_MULTI.isEnv() ? Thread.currentThread() : null, UIMessage.UI_TOP_RIGHT);
                    Vector<Printable> jobs = new Vector<Printable>();
                    /**
                     * print all thumbnails
                     */
                    if (selectedOutputMode.equals(outputMode[2])) {
                        Printable printable = new Printable() {
                            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                                String[] photoK = selectedKeys.toArray(new String[]{});
                                Graphics2D g = Sprite.wrapRendering(graphics);
                                Rectangle paperBounds = new Rectangle((int) pageFormat.getImageableX(), (int) pageFormat.getImageableY(), (int) pageFormat.getImageableWidth(), (int) pageFormat.getImageableHeight());
                                g.setClip(paperBounds);
                                Insets insets = new Insets(10, 5, 10, 5);
                                final int cols = 3;
                                final int picSquare = (int) Math.floor((float) (paperBounds.width - 2) / (float) cols);
                                final int rows = (int) Math.floor((float) (paperBounds.height - 2) / (float) picSquare);
                                int N = pageIndex * rows * cols;
                                if (N >= photoK.length) {
                                    return NO_SUCH_PAGE;
                                }
                                Rectangle nextBounds = new Rectangle(paperBounds.x + 1, paperBounds.y + 1, picSquare, picSquare);
                                System.out.println("paper dim : " + paperBounds + " pic square : " + picSquare);
                                while (paperBounds.contains(nextBounds) && N < photoK.length) {
                                    System.out.println("pic dim : " + nextBounds);
                                    imageCollection.beginEdit(photoK[N], true);
                                    Sprite sp = imageCollection.getCollectionSprite(photoK[N], true);
                                    Rectangle origBds = sp.getBounds();
                                    Rectangle squareBds = nextBounds.getBounds();
                                    squareBds.grow(-Math.round((float) (insets.right + insets.left) / 2f), -Math.round((float) (insets.top + insets.bottom) / 2f));
                                    double scale = Math.min((float) squareBds.width / (float) sp.getWidth(), (float) squareBds.height / (float) sp.getHeight());
                                    Rectangle picBds = AffineTransform.getScaleInstance(scale, scale).createTransformedShape(sp.getBounds()).getBounds();
                                    picBds.setLocation(Math.round((float) squareBds.getCenterX() - (float) picBds.getWidth() / 2f), Math.round((float) squareBds.getCenterY() - (float) picBds.getHeight() / 2f));
                                    sp.setBounds(picBds);
                                    sp.runValidate();
                                    String picName = new File(photoK[N]).getName();
                                    Font f = g.getFont();
                                    int fontSize = 7;
                                    g.setFont(new Font("Serif", Font.PLAIN, fontSize));
                                    Dimension strDim = RenderingScene._toGraphicalMetrics(picName, g);
                                    while (strDim.width > picSquare && fontSize > 1) {
                                        g.setFont(new Font("Serif", Font.PLAIN, --fontSize));
                                        strDim = RenderingScene._toGraphicalMetrics(picName, g);
                                    }
                                    g.drawString(picName, (float) picBds.getCenterX() - (float) strDim.width / 2f, (float) picBds.getMaxY() + strDim.height + 3);
                                    g.setFont(f);
                                    sp.draw(mainFrame, g);
                                    sp.setBounds(origBds);
                                    imageCollection.endEdit(photoK[N], true);
                                    /* next pic bounds */
                                    N++;
                                    int c = N % cols;
                                    int r = (int) Math.floor((float) N / (float) cols) % rows;
                                    System.out.println("next pic row x col : " + r + " x " + c);
                                    nextBounds = new Rectangle(paperBounds.x + 1 + c * picSquare, paperBounds.y + 1 + r * picSquare, picSquare, picSquare);
                                }
                                return PAGE_EXISTS;
                            }
                        };
                        jobs.add(printable);
                    } else {
                        /**
                         * print one each
                         */
                        synchronized (selectedKeys) {
                            for (final String s : selectedKeys) {
                                Printable printable = new Printable() {
                                    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                                        if (pageIndex > 0) {
                                            return Printable.NO_SUCH_PAGE;
                                        }
                                        imageCollection.beginEdit(s, false);
                                        updateCurrentStatus(s, null);
                                        Sprite sp = imageCollection.getCollectionSprite(s, false);
                                        Rectangle origBds = sp.getBounds();
                                        Rectangle paperBounds = new Rectangle((int) pageFormat.getImageableX(), (int) pageFormat.getImageableY(), (int) pageFormat.getImageableWidth(), (int) pageFormat.getImageableHeight());
                                        Graphics2D g = Sprite.wrapRendering(graphics);
                                        if (selectedOutputMode.equals(outputMode[0])) {
                                            double scaleFitPaper = Math.max(paperBounds.getWidth() / (double) sp.getWidth(), paperBounds.getHeight() / (double) sp.getHeight());
                                            sp.setZoomEnabled(true, scaleFitPaper);
                                        } else {
                                            double scaleFitPaper = Math.min(paperBounds.getWidth() / (double) sp.getWidth(), paperBounds.getHeight() / (double) sp.getHeight());
                                            sp.setZoomEnabled(true, scaleFitPaper);
                                        }
                                        sp.runValidate();
                                        sp.setLocation(Math.round((float) paperBounds.getCenterX() - sp.getWidth() / 2f),
                                                Math.round((float) paperBounds.getCenterY() - sp.getHeight() / 2f));
                                        sp.draw(mainFrame, g);
                                        sp.setZoomEnabled(false, 1.);
                                        sp.setBounds(origBds);
                                        imageCollection.endEdit(s, false);
                                        return Printable.PAGE_EXISTS;
                                    }
                                };
                                jobs.add(printable);
                            }
                        }
                    }
                    try {
                        int p = 0;
                        edit_started(wait, op, jobs.size());
                        for (Printable job : jobs) {
                            pj.setPrintable(job, pjFormat);
                            pj.setJobName(IMCBrowser.class.getSimpleName() + " " + (++p) + " of " + jobs.size());
                            pj.print();
                            edit_progress(wait, 1);
                        }
                    } catch (PrinterException ex) {
                        ex.printStackTrace();
                        UIMessage.showLightPopupMessage(new JLabel(getLocalizedStringCap("Printerror") + " : " + ex.getMessage(), UIMessage._getIcon(UIMessage.ERROR_TYPE, false), SwingConstants.LEADING), null, picView, UIMessage.UI_BOTTOM_RIGHT);
                        interrupt = true;
                    } finally {
                        edit_finished(wait, op, interrupt);
                        pp.setVisible(false);
                        UIMessage.kill(wait);
                    }
                }
            }
        };
        action_print.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(action_print);
        menu.addSeparator();
        menu.add(new AbstractAction(getLocalizedStringCap("erasealbumsandexit") + "...") {
            public void actionPerformed(ActionEvent e) {
                String[] lstr = LocalizedResources.localizedGetStringArray("dialog3", 2);
                if (JOptionPane.YES_OPTION == UIMessage.showConfirmDialog(picView, lstr[0], lstr[1], JOptionPane.YES_NO_OPTION)) {
                    for (File f : new File(defaultImageDirectory, "backup").listFiles(new FilenameFilter() {
                        public boolean accept(File dir, String name) {
                            return name.startsWith(filePrefix_backup);
                        }
                    })) {
                        f.delete();
                    }
                    storeApplication(applicationStore, false);
                    FileHelper._eraseTmpFiles(_tmpFilesSuffix, _TMPDIRECTORY);
                    System.exit(0);
                }
            }
        });
        menu.addSeparator();
        action_restart = new AbstractAction(getLocalizedStringCap("reload"), UIMessage._getIcon(UIMessage.REFRESH_TYPE, true)) {
            public void actionPerformed(ActionEvent e) {
                reload();
            }
        };
        action_quit = new AbstractAction(getLocalizedStringCap("quit"), UIMessage._getIcon(UIMessage.EXIT_TYPE, true)) {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        };
        action_quit.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(action_quit);
        menuBar.add(menu = new JMenu(getLocalizedStringCap("Edit")));
        action_undo = new AbstractAction(getLocalizedStringCap("undo"), UIMessage._getIcon(UIMessage.BACK_TYPE, true)) {
            public void actionPerformed(ActionEvent e) {
                Runnable r = new Runnable() {
                    public void run() {
                        if (!waitForAllPictures(imageCollection)) {
                            return;
                        }
                        Vector<String> sKeys = new Vector<String>(selectedKeys);
                        ImageCollection archive = null;
                        try {
                            for (String selection : sKeys) {
                                if (imageHistoryCollection.containsKey(selection)) {
                                    archive = imageHistoryCollection.get(selection);
                                    waitForAllPictures(archive);
                                    if (!archive.keySet(false).isEmpty()) {
                                        String lastModif = archive.keySet(false).last();
                                        Map<String, Serializable> tbstore = getAttributesSet(archive, lastModif, true, ImageCollection.ATTRIBUTE_MIME, ImageCollection.ATTRIBUTE_TYPE);
                                        Map<String, Serializable> store = getAttributesSet(archive, lastModif, false, ImageCollection.ATTRIBUTE_MIME, ImageCollection.ATTRIBUTE_TYPE);
                                        archive.beginEdit(lastModif, false);
                                        updateAddImage(imageCollection, selection, archive.getCollectionSprite(lastModif, false), tbstore, store);
                                        archive.endEdit(lastModif, false);
                                        archive.removeImage(lastModif);
                                    }
                                    if (archive.keySet(false).isEmpty()) {
                                        edit_savedState(true, selection);
                                    }
                                    archive.accessCache(false).clear();
                                    archive.accessCache(true).clear();
                                }
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        } finally {
                            doBrowserUpdate();
                        }
                    }
                };
                canvas.doTaskOnScreen(r);
            }
        };

        action_undo.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(action_undo);
        action_undoAll = new AbstractAction(getLocalizedStringCap("undoall")) {
            public void actionPerformed(ActionEvent e) {
                Runnable r = new Runnable() {
                    public void run() {
                        if (!waitForAllPictures(imageCollection)) {
                            return;
                        }
                        Vector<String> sKeys = new Vector<String>(selectedKeys);
                        ImageCollection archive = null;
                        try {
                            for (String historyKeySelection : sKeys) {
                                if (imageHistoryCollection.containsKey(historyKeySelection)) {
                                    archive = imageHistoryCollection.get(historyKeySelection);
                                    waitForAllPictures(archive);
                                    if (!archive.keySet(false).isEmpty()) {
                                        String lastModif = archive.keySet(false).first();

                                        Map<String, Serializable> tbstore = getAttributesSet(archive, lastModif, true, ImageCollection.ATTRIBUTE_MIME, ImageCollection.ATTRIBUTE_TYPE);
                                        Map<String, Serializable> store = getAttributesSet(archive, lastModif, false, ImageCollection.ATTRIBUTE_MIME, ImageCollection.ATTRIBUTE_TYPE);
                                        archive.beginEdit(lastModif, false);
                                        updateAddImage(imageCollection, historyKeySelection, archive.getCollectionSprite(lastModif, false), tbstore, store);
                                        archive.endEdit(lastModif, false);
                                        for (String modif : new Vector<String>(archive.keySet(false))) {
                                            archive.removeImage(modif);
                                        }
                                        edit_savedState(true, historyKeySelection);
                                    }
                                    archive.accessCache(false).clear();
                                    archive.accessCache(true).clear();
                                }
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        } finally {
                            doBrowserUpdate();
                        }
                    }
                };
                canvas.doTaskOnScreen(r);
            }
        };
        action_undoAll.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, (env.OS_MAC.isEnv() ? InputEvent.ALT_DOWN_MASK : InputEvent.SHIFT_DOWN_MASK) | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(action_undoAll);
        menu.addSeparator();
        action_seeInHdd = new AbstractAction(getLocalizedStringCap("seeinhdd"), UIMessage._getIcon(UIMessage.NEW_WINDOW_TYPE, true)) {
            public void actionPerformed(ActionEvent e) {
                if (!picView.isEnabled()) {
                    return;
                }
                synchronized (selectedKeys) {
                    if (selectedKeys.size() == 1) {
                        try {
                            _goWeb(new File(selectedKeys.iterator().next()).toURI().toURL());
                        } catch (MalformedURLException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        };
        menu.add(action_seeInHdd);
        action_copy_make = new AbstractAction(getLocalizedStringCap("copymake")) {
            public void actionPerformed(ActionEvent e) {
                if (!picView.isEnabled()) {
                    return;
                }
                String op = getLocalizedStringCap("copymake");
                /*long wait = UIMessage.displayWaiting(op, currentProcessThumb, browser);*/
                JDialog p = showLightPopupActivity(op, currentProcessThumb, null, UIMessage.UI_TOP_RIGHT);
                try {
                    synchronized (selectedKeys) {
                        for (String k : selectedKeys) {
                            File nF = new File(k);
                            String nk = nF.getParent() + File.separator + "copy_" + nF.getName();
                            Map<String, Serializable> tbstore = getAttributesSet(imageCollection, k, true, ImageCollection.ATTRIBUTE_MIME, ImageCollection.ATTRIBUTE_TYPE);
                            Map<String, Serializable> store = getAttributesSet(imageCollection, k, false, ImageCollection.ATTRIBUTE_MIME, ImageCollection.ATTRIBUTE_TYPE);
                            imageCollection.beginEdit(k, false);
                            updateAddImage(imageCollection, nk, imageCollection.getCollectionSprite(k, false), tbstore, store);
                            imageCollection.endEdit(k, false);
                            updateAddInfo(imageCollectionInfo, nk, nF, (String) store.get(ImageCollection.ATTRIBUTE_MIME), false);
                            edit_savedState(false, nk);
                        }
                    }
                    imageCollection.waitForAllThumbnails();
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    p.setVisible(false);
                    callBrowserUpdate();
                }
            }
        };
        action_copy_make.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(action_copy_make);
        action_copy_toClipboard = new AbstractAction(getLocalizedStringCap("copytoclipboard")) {
            public void actionPerformed(ActionEvent e) {
                if (!picView.isEnabled()) {
                    return;
                }
                picView.getTransferHandler().exportToClipboard(picView, picView.getToolkit().getSystemClipboard(), TransferHandler.COPY);
            }
        };
        action_copy_toClipboard.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(action_copy_toClipboard);
        action_copy_fromClipboard = new AbstractAction(getLocalizedStringCap("copyfromclipboard")) {
            public void actionPerformed(ActionEvent e) {
                if (!picView.isEnabled()) {
                    return;
                }
                String op = getLocalizedStringCap("copyfromclipboard");
                /*long wait = UIMessage.displayWaiting(op, currentProcessThumb, browser);*/
                JDialog p = showLightPopupActivity(op, currentProcessThumb, null, UIMessage.UI_TOP_RIGHT);
                picView.getTransferHandler().importData(picView, picView.getToolkit().getSystemClipboard().getContents(picView));
                /* UIMessage.kill(wait);*/
                p.setVisible(false);
                callBrowserUpdate();
            }
        };
        action_copy_fromClipboard.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(action_copy_fromClipboard);
        menu.addSeparator();
        action_selectAll = new AbstractAction(getLocalizedStringCap("selectall")) {
            public void actionPerformed(ActionEvent e) {
                if (!picView.isEnabled()) {
                    return;
                }
                setSelectedAll(true);
                callFileInfoUpdate();
            }
        };
        action_selectAll.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(action_selectAll);
        menu.addSeparator();
        action_refresh = new AbstractAction(getLocalizedStringCap("refresh"), UIMessage._getIcon(UIMessage.REFRESH_TYPE, true)) {
            public void actionPerformed(ActionEvent e) {
                if (!picView.isEnabled()) {
                    return;
                }
                // reset flag
                browserUpdatePosted = false;

                updateCollectionDB();
                callBrowserUpdate();
                updateCurrentFileInfo();
            }
        };
        action_refresh.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(action_refresh);
        menu.addSeparator();
        menu.add(action_add = new AbstractAction(getLocalizedStringCap("addtoalbum") + "...", UIMessage._getIcon(UIMessage.PLUS_TYPE, true)) {
            public void actionPerformed(ActionEvent e) {
                String[] lstr = LocalizedResources.localizedGetStringArray("dialog4", 2);
                String selection = (String) UIMessage.showSelectDialog(mainFrame, lstr[0], lstr[1], imageCollectionDB.keySet().toArray(new String[]{}), dbList.getSelectedItem());
                if (selection instanceof String) {
                    ImageCollection ic = imageCollectionDB.get(selection);
                    SortedMap<String, Map<Integer, Serializable>> icInfo = imageCollectionInfoDB.get(selection);
                    try {
                        if (!waitForAllPictures(imageCollection)) {
                            return;
                        }
                        synchronized (selectedKeys) {
                            for (String k : selectedKeys) {

                                Map<String, Serializable> tbstore = getAttributesSet(imageCollection, k, true, ImageCollection.ATTRIBUTE_MIME, ImageCollection.ATTRIBUTE_TYPE);
                                Map<String, Serializable> store = getAttributesSet(imageCollection, k, false, ImageCollection.ATTRIBUTE_MIME, ImageCollection.ATTRIBUTE_TYPE);
                                imageCollection.beginEdit(k, false);
                                updateAddImage(ic, k, imageCollection.getCollectionSprite(k, false), tbstore, store);
                                imageCollection.endEdit(k, false);
                                updateCollectionNameProperty(k, icInfo, ic);
                                updateAddInfo(icInfo, k, new File(k), (String) store.get(imageCollection.ATTRIBUTE_MIME), true);
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                callBrowserUpdate();
            }
        });
        action_remove = new AbstractAction(getLocalizedStringCap("removefromalbum") + "...", UIMessage._getIcon(UIMessage.MINUS_TYPE, true)) {
            public void actionPerformed(ActionEvent e) {
                if (JOptionPane.OK_OPTION == UIMessage.showConfirmDialog(picView, getLocalizedStringCap("suretodelete_message"), getLocalizedStringCap("suretodelete_title"), JOptionPane.OK_CANCEL_OPTION)) {
                    synchronized (selectedKeys) {
                        for (String k : selectedKeys) {
                            imageCollection.removeImage(k);
                            File orig = getBackupFile(k);
                            if (orig != null) {
                                if (orig.exists()) {
                                    orig.delete();
                                }
                            }
                            imageCollectionInfo.remove(k);
                        }
                    }
                    storeApplication(applicationStore);
                    callBrowserUpdate();
                }
            }
        };
        action_remove.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.ALT_DOWN_MASK));
        menu.add(action_remove);
        menuBar.add(menu = new JMenu(getLocalizedStringCap("Advanced")));
        menu.add(action_opengl = new AbstractAction(getLocalizedStringCap("openglOn"), UIMessage._getIcon(UIMessage.OPENGL_TYPE, true)) {
            public void actionPerformed(ActionEvent e) {
                if (picView.isEnabled()) {
                    JLabel label = new JLabel(getLocalizedStringCap("openglConfirm"));
                    if (JOptionPane.OK_OPTION == UIMessage.showConfirmDialog(picView, label, getLocalizedStringCap("areyousure"), JOptionPane.OK_CANCEL_OPTION)) {
                        initRenderingCanvas(true);
                        doBrowserUpdate();
                        putValue(Action.NAME, getLocalizedStringCap("openglOn"));
                    }
                }
            }
        });
        JMenu lang = new JMenu(getLocalizedStringCap("changelanguage"));
        lang.setEnabled(!applet);
        ButtonGroup langs = new ButtonGroup();
        for (int i = 0; i < LocalizedResources.languages.length; i++) {
            final ResourceBundle l = LocalizedResources.getLocalizedRB(LocalizedResources.languages[i]);
            JRadioButtonMenuItem rbm = new JRadioButtonMenuItem(new AbstractAction(l.getLocale().getDisplayLanguage(), new ImageIcon(IMCBrowser.class.getResource(l.getString("langIcon_small")))) {
                public void actionPerformed(ActionEvent e) {
                    if (!picView.isEnabled()) {
                        return;
                    }
                    LocalizedResources.localized = l;
                    reload();
                }
            });
            rbm.setSelected(LocalizedResources.languages[i].equals(LocalizedResources.localized.getLocale()));
            langs.add(rbm);
            lang.add(rbm);
        }
        menu.add(lang);
        menu.addSeparator();
        menu.add(action_search);
        menu.addSeparator();
        action_export = new AbstractAction(getLocalizedStringCap("exportalbum") + "...", UIMessage._getIcon(UIMessage.EXPORT_TYPE, true)) {
            public void actionPerformed(ActionEvent e) {
                if (!picView.isEnabled()) {
                    return;
                }
                fcp.setModeSave(true);
                fcp.clearAllFileFilters();
                HashSet set = new HashSet();
                set.add("imc");
                fcp.addFileFilter(set);
                File[] f = fcp.getFile();
                if (f.length > 0) {
                    try {
                        if (!f[0].getPath().toLowerCase().endsWith("imc")) {
                            f[0] = new File(f[0].getPath() + ".imc");
                        }
                        File tmp = FileHelper._createTempFile("imc_", _TMPDIRECTORY, true);
                        RandomAccessFile rafTmp = new RandomAccessFile(tmp, "rw");
                        FileOutputStream fos = new FileOutputStream(rafTmp.getFD());
                        ObjectOutputStream oos = new ObjectOutputStream(fos);
                        oos.writeObject(dbList.getSelectedItem());
                        oos.writeObject(imageCollection);
                        oos.writeObject(imageCollectionInfo);
                        rafTmp.close();
                        FileHelper._fileCopy(tmp, f[0], false, true);
                        tmp.delete();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        UIMessage.showLightPopupMessage(new JLabel(getLocalizedStringCap("IOerrorwhileattemptingtowriteonto") + " " + f[0].getPath() + " : " + ex.getMessage(), UIMessage._getIcon(UIMessage.ERROR_TYPE, false), SwingConstants.LEADING), null, picView, UIMessage.UI_BOTTOM_RIGHT);
                    }
                }
            }
        };
        action_export.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(action_export);
        action_import = new AbstractAction(getLocalizedStringCap("importalbum") + "...", UIMessage._getIcon(UIMessage.IMPORT_TYPE, true)) {
            public void actionPerformed(ActionEvent e) {
                if (!picView.isEnabled()) {
                    return;
                }
                fcp.setModeOpen(true);
                fcp.clearAllFileFilters();
                HashSet set = new HashSet();
                set.add("imc");
                fcp.addFileFilter(set);
                fcp.setMultiSelectionEnabled(false);
                File[] f = fcp.getFile();
                if (f.length > 0) {
                    try {
                        RandomAccessFile raf = new RandomAccessFile(f[0], "r");
                        FileInputStream fis = new FileInputStream(raf.getFD());
                        ObjectInputStream ois = new ObjectInputStream(fis);
                        String name = (String) ois.readObject();
                        imageCollection = (ImageCollection) ois.readObject();
                        imageCollectionInfo = (SortedMap) ois.readObject();
                        raf.close();
                        int i = 1;
                        while (imageCollectionDB.containsKey(name)) {
                            name += "(" + i + ")";
                            i++;
                        }
                        imageCollectionDB.put(name, imageCollection);
                        imageCollectionInfoDB.put(name, imageCollectionInfo);
                        updateImageCollectionListeners(imageCollection);
                        callCollectionDBUpdate();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        UIMessage.showLightPopupMessage(new JLabel(getLocalizedStringCap("Errorwhileattemptingtoreadfrom") + " " + f[0].getPath() + " : " + ex.getMessage(), UIMessage._getIcon(UIMessage.ERROR_TYPE, false), SwingConstants.LEADING), null, picView, UIMessage.UI_BOTTOM_RIGHT);
                    }
                }
            }
        };
        action_import.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(action_import);
        menu.addSeparator();
        action_share = new AbstractAction(getLocalizedStringCap("share") + "...") {
            public void actionPerformed(ActionEvent e) {
                shareSelectedImages();
            }
        };
        action_share.setEnabled(false);
        menu.add(action_share);

        menuBar.add(menu = new JMenu(getLocalizedStringCap("JMF")));
        Vector<CaptureDeviceInfo> devs = CaptureDeviceManager.getDeviceList(null);/* all formats includes YUV H264 etc...*/

        if (env.OS_MAC.isEnv() && devs.size() == 0) {
            menu.setEnabled(false);
        }
        JMenu JMFmenu = new JMenu(getLocalizedStringCap("JMFdevs"));
        menu.add(action_JMFload = new AbstractAction(getLocalizedStringCap("JMFload")) {
            public void actionPerformed(ActionEvent ae) {
                JMFinstalled = detectJMFDevices();
            }
        });
        System.err.println(log(devs.size() + " media devices found", LVL.APP_NOT));
        for (final CaptureDeviceInfo cdi : devs) {
            JMFmenu.add(new AbstractAction(cdi.getName(), UIMessage._getIcon(UIMessage.WEBCAM_TYPE, true)) {
                public void actionPerformed(ActionEvent ae) {
                    openCaptureFrame(cdi, null);
                }
            });
        }
        menu.add(JMFmenu);
        menuBar.add(menu = new JMenu(getLocalizedStringCap("Help")));
        menu.add(action_about = showAbout());
        action_help = new AbstractAction(getLocalizedStringCap("quickguide") + "...", UIMessage._getIcon(UIMessage.INFO_TYPE, true)) {
            public void actionPerformed(ActionEvent e) {
                if (!picView.isEnabled()) {
                    return;
                }
                try {
                    final JEditorPane jep = new JEditorPane(getClass().getResource(LocalizedResources.localized.getString("help.html")));
                    jep.setPreferredSize(new Dimension(600, 400));
                    jep.addHyperlinkListener(new HyperlinkAdapter() {
                        @Override
                        public void hyperlinkUpdate(HyperlinkEvent e) {
                            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED && !jep.getPage().sameFile(e.getURL())) {
                                _goWeb(e.getURL());
                            } else {
                                super.hyperlinkUpdate(e);
                            }
                        }
                    });
                    jep.setEditable(false);
                    new UIMessage(false, new JScrollPane(jep), null);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        };
        action_help.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        menu.add(action_help);
        if (applet) {
            ((JApplet) mainFrame).setJMenuBar(menuBar);
        } else {
            ((JFrame) mainFrame).setJMenuBar(menuBar);
        }
        initRenderingCanvas(false);
        navRenderer = getSoftRenderer();
        mia = getMouseInput();
        if (!applet) {
            ((JFrame) mainFrame).pack();
        }
        mainFrame.setVisible(true);
        if (imageCollectionDB.isEmpty()) {
            setImageCollection(newImageCollection());
        } else {
            String key = imageCollectionDB.firstKey();
            setImageCollection(imageCollectionDB.get(key), imageCollectionInfoDB.get(key));
        }
        initialized = true;
        updateMenus();
        updateCollectionDB();
        callBrowserUpdate();
        updateCurrentFileInfo();
        ssPane.setDividerLocation(.75);
    }

    private void openCaptureFrame(final CaptureDeviceInfo cdi, javax.media.Format vf) {
        try {
            final javax.media.Player player = Manager.createRealizedPlayer(cdi.getLocator());
            final Component comp;

            if ((comp = player.getVisualComponent()) != null) {
                setImageCollection(newImageCollection(LocalizedResources.localized.getString("JMF") + " @ " + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(new Date())));
                updateCollectionDB();
                final JFrame f = new JFrame(cdi.toString());

                f.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        player.close();
                        player.deallocate();
                    }
                });
                final FormatControl fc = (FormatControl) player.getControl("javax.media.control.FormatControl");
                fc.setFormat(vf);
                if (vf == null) {
                    vf = fc.getFormat();
                }
                comp.setPreferredSize(((VideoFormat) vf).getSize());
                f.add(comp, BorderLayout.CENTER);
                JPanel panel = new JPanel(true);
                panel.setLayout(new GridBagLayout());
                GridBagConstraints c = new GridBagConstraints();
                final JComboBox formatBox = new JComboBox(fc.getSupportedFormats());
                formatBox.setPrototypeDisplayValue("RGB=500x400 23 bits");
                formatBox.setRenderer(new ListCellRenderer() {
                    JLabel render = new JLabel();

                    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                        if (value instanceof VideoFormat) {
                            VideoFormat v = (VideoFormat) value;
                            render.setText(v.getEncoding() + " " + v.getSize().width + "x" + v.getSize().height);
                        } else {
                            render.setText(value.toString());
                        }
                        if (cellHasFocus || isSelected) {
                            render.setBackground(clr_selected[0]);
                            render.setForeground(clr_selected[1]);
                            render.setToolTipText(value.toString());
                        } else {
                            render.setBackground(f.getBackground());
                            render.setForeground(f.getForeground());
                        }
                        render.setOpaque(true);
                        return render;
                    }
                });
                formatBox.setSelectedItem(vf);
                formatBox.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (formatBox.getSelectedItem() == null) {
                            return;
                        }
                        VideoFormat v = (VideoFormat) formatBox.getSelectedItem();
                        player.close();
                        player.deallocate();
                        f.setVisible(false);
                        f.dispose();
                        openCaptureFrame(cdi, v);
                    }
                });
                panel.add(formatBox, c);
                panel.add(new JButton(new AbstractAction(getLocalizedStringCap("JMFcapture")) {
                    public void actionPerformed(ActionEvent ae) {
                        FrameGrabbingControl fgc = (FrameGrabbingControl) player.getControl("javax.media.control.FrameGrabbingControl");
                        javax.media.Buffer buf = fgc.grabFrame();
                        BufferToImage btoi = new BufferToImage((VideoFormat) buf.getFormat());
                        Image img = btoi.createImage(buf);
                        open(img);
                    }
                }), c);
                f.add(panel, BorderLayout.SOUTH);
                f.add(player.getControlPanelComponent(), BorderLayout.NORTH);
                f.setLocationRelativeTo(mainFrame);
                f.pack();
                f.setVisible(true);
                player.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            new UIMessage(true, e.getMessage(), null, UIMessage.ERROR_TYPE);
        }
    }

    private AbstractAction showAbout() {
        return new AbstractAction(getLocalizedStringCap("aboutIMCBrowser") + "...", UIMessage._getIcon(UIMessage.MAIL_TYPE, true)) {
            public void actionPerformed(ActionEvent e) {
                if (!picView.isEnabled()) {
                    return;
                }
                JPanel about = new JPanel(new GridBagLayout(), true);
                GridBagConstraints c = new GridBagConstraints();
                c.gridwidth = c.REMAINDER;
                c.weightx = c.weighty = 1.;
                c.fill = c.HORIZONTAL;
                String[] lstr = LocalizedResources.localizedGetStringArray("label8", 4);
                about.add(new JLabel(lstr[0] + IMCBrowser.class.getPackage().getSpecificationTitle() + lstr[1] + lstr[3]), c);
                about.add(new JLabel("<html>" + IMCBrowser.class.getPackage().getImplementationTitle() + lstr[2] + IMCBrowser.class.getPackage().getImplementationVersion() + "</html>"), c);
                about.add(new JLabel(getLocalizedStringCap("label10")), c);
                about.add(new JLabel(getLocalizedStringCap("label19")), c);
                about.add(new JLabel(getLocalizedStringCap("label11")), c);
                about.add(new JLabel(getLocalizedStringCap("labelOpenGL")), c);

                JXALabel storeLink = new JXALabel("*** " + getLocalizedStringCap("aboutdatabases") + " " + defaultImageDirectory + " ***");
                storeLink.setMaxWidth(300, 10);
                storeLink.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        super.mouseClicked(e);
                        try {
                            _goWeb(defaultImageDirectory.toURI().toURL());
                        } catch (MalformedURLException ex) {
                            if (_debug) {
                                ex.printStackTrace();
                            }
                        }
                    }
                });
                about.add(storeLink, c);
                if (applet) {
                    about.add(new JButton(new AbstractAction("donate", UIMessage._getIcon(UIMessage.DONATE_TYPE, true)) {
                        public void actionPerformed(ActionEvent e) {
                            try {
                                _goWeb(new URL(LocalizedResources.localized.getString("URL_donate")));
                            } catch (MalformedURLException ex) {
                                if (_debug) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                    }), c);
                }
                new UIMessage(false, about, null);
            }
        };
    }

    private void shareSelectedImages() {
        if (selectedKeys.isEmpty()) {
            return;
        }
        Object[] destinations = {
            getLocalizedStringCap("shareFacebook"),
            getLocalizedStringCap("shareGooglePhotos"),
            getLocalizedStringCap("shareCopyOnly")
        };
        int destination = JOptionPane.showOptionDialog(mainFrame,
                getLocalizedStringCap("shareInstructions"),
                getLocalizedStringCap("share"),
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                destinations,
                destinations[0]);
        if (destination == JOptionPane.CLOSED_OPTION) {
            return;
        }
        try {
            action_copy_toClipboard.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "share"));
            if (destination == 0) {
                _goWeb(new URL("https://www.facebook.com/"));
            } else if (destination == 1) {
                _goWeb(new URL("https://photos.google.com/"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            UIMessage.showLightPopupMessage(new JLabel(ex.getMessage(),
                    UIMessage._getIcon(UIMessage.ERROR_TYPE, false),
                    SwingConstants.LEADING), null, picView, UIMessage.UI_BOTTOM_RIGHT);
        }
    }

    private MouseInputAdapter getMouseInput() {
        return new MouseInputAdapter() {
            private void setSelected(boolean b, Rectangle r) {
                synchronized (imagePanels) {
                    for (JIMCPanel imagePanel : imagePanels.values()) {
                        IMCBrowser.this.setSelected(r.intersects(imagePanel.getBounds()), imagePanel.key);
                    }
                }
            }
            Point dragging = null;

            private void showPopup(MouseEvent e) {
                popup.removeAll();
                JMenuItem m;
                if (((Container) e.getSource()).getComponentAt(e.getPoint()) instanceof JIMCPanel) {
                    JIMCPanel imagePanel = (JIMCPanel) ((Container) e.getSource()).getComponentAt(e.getPoint());
                    IMCBrowser.this.setSelected(true, imagePanel.key);
                    popup.add(m = new JMenuItem(selectedKeys.size() > 1 ? selectedKeys.size() + " " + LocalizedResources.localized.getString("photos") : imagePanel.key.substring(imagePanel.key.lastIndexOf(File.separator) + 1)));
                    m.setEnabled(false);
                }
                updateCurrentFileInfo();
                for (Action action : actions_selection) {
                    if (action != null) {
                        if (action.equals(nullAction)) {
                            popup.addSeparator();
                        } else {
                            popup.add(action);
                        }
                    }
                }
                popup.pack();
                popup.show((Component) e.getSource(), e.getX(), e.getY());
                poppedup = true;
            }
            boolean poppedup = false;

            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                if (picView.isEnabled()) {
                    if (e.isPopupTrigger()) {
                        showPopup(e);
                    }
                    dragging = e.getPoint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                if (picView.isEnabled()) {
                    if (e.isPopupTrigger()) {
                        showPopup(e);
                    }
                    dragging = null;
                }
                selectionRect = new Rectangle();
                Sprite._quickPaintImmediately(picView);

            }

            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                if (picView.isEnabled()) {
                    if (env.OS_MAC.isEnv() ? !e.isMetaDown() : !e.isControlDown()) {
                        clearAllSelection();
                    }
                    if (dragging != null) {
                        selectionRect.setFrameFromDiagonal(dragging, e.getPoint());
                        setSelected(true, selectionRect);
                    }
                    updateCurrentFileInfo();
                    Sprite._quickPaintImmediately(picView);

                }
            }
            JPopupMenu popup = new JPopupMenu();

            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (picView.isEnabled()) {
                    if (!poppedup && ((Container) e.getSource()).getComponentAt(e.getPoint()) instanceof JIMCPanel) {
                        JIMCPanel imagePanel = (JIMCPanel) ((Container) e.getSource()).getComponentAt(e.getPoint());
                        if (env.OS_MAC.isEnv() ? !e.isMetaDown() : !e.isControlDown()) {
                            if (e.isShiftDown()) {
                                SortedMap<String, JIMCPanel> head = imagePanels.headMap(imagePanel.key);
                                SortedMap<String, JIMCPanel> tailReversed = new TreeMap(Collections.reverseOrder(imagePanels.comparator()));
                                tailReversed.putAll(imagePanels.tailMap(imagePanel.key));
                                SortedMap[] maps = new SortedMap[]{head, tailReversed};
                                for (SortedMap map : maps) {
                                    synchronized (imagePanels) {
                                        boolean select = false;
                                        for (JIMCPanel panel : (Collection<JIMCPanel>) map.values()) {
                                            if (!select && isSelected(panel.key)) {
                                                select = true;
                                            }
                                            IMCBrowser.this.setSelected(select, panel.key);
                                        }
                                    }
                                }
                            } else {
                                clearAllSelection();
                            }
                        }
                        IMCBrowser.this.setSelected(true, imagePanel.key);
                        updateCurrentFileInfo();
                        if (e.getClickCount() == 2) {
                            browserMode = MODE_NAV;
                            IMCBrowser.this.setSelected(true, imagePanel.key);
                            callBrowserUpdate();
                        }
                    } else {
                        if (!poppedup) {
                            clearAllSelection();
                        }
                        poppedup = false;
                    }
                }
            }
        };
    }

    private ImageCollectionRenderer getSoftRenderer() {
        return new ImageCollectionRenderer() {
            private long hash = System.nanoTime();

            public int hashCode() {
                return (int) hash;
            }

            public boolean equals(Object o) {
                return o == null ? false : o.hashCode() == hashCode();
            }

            public JComponent makeCollectionRendererComponent(final ImageCollection coll, final String pic, final boolean thumbnail) {
                final Rectangle curBds = new Rectangle();
                final JComponent renderer = new JComponent() {
                    long hash = System.nanoTime();

                    @Override
                    public int hashCode() {
                        return (int) hash;
                    }

                    @Override
                    public boolean equals(Object obj) {
                        return obj == null ? false : obj.hashCode() == hashCode();
                    }

                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        try {
                            if (thumbnail) {
                                coll.waitForThumbnail(pic);
                            } else {
                                coll.waitForPicture(pic);
                            }
                            coll.beginEdit(pic, thumbnail);
                            Sprite sp = coll.getCollectionSprite(pic, thumbnail);
                            Rectangle origBds = sp.getBounds();
                            Rectangle bounds = getBounds();
                            /**
                             * avoid thumbnail artefact on upscaling
                             */
                            if ((bounds.getWidth() * bounds.getHeight()) > (origBds.getWidth() * origBds.getHeight())) {
                                sp.clearResource();
                            }
                            Graphics2D g2 = Sprite.wrapRendering(g);
                            try {
                                /**
                                 * sp.setRenderingScene(null);
                                 */
                                sp.setObs(this);
                                sp.getBounds(curBds);
                                if (!previewContrast.getValueIsAdjusting() && !previewBrightness.getValueIsAdjusting()) {
                                    float rescale = 1f + (float) previewContrast.getValue() / 100f;
                                    int offset = previewBrightness.getValue();
                                    /*
                                     sp.runValidate();
                                    
                                     BufferedImage image = sp.toBuffered();
                                     edit_buffer(image);
                                     * NULLPOINTEREXCEPTION
                                     RenderedOp image2 = doLookupTable(image, rescale, offset);
                                     edit_buffer(image2);
                                     edit_copyImageOnCanvas(image2, sp);
                                     edit_clearBuffer(image);
                                     edit_clearBuffer(image2);*/
                                }
                                Rectangle picBds = getPictureBounds(bounds, curBds);
                                sp.setBounds(picBds);
                                sp.runValidate();
                                g2.translate(-bounds.x, -bounds.y);
                                sp.draw(this, g2);
                                if (imageCollectionInfo.containsKey(pic)) {
                                    Rectangle cropper = imageCollectionInfo.get(pic).containsKey(INFO_CROPRECT) ? (Rectangle) imageCollectionInfo.get(pic).get(INFO_CROPRECT) : null;
                                    if (cropper instanceof Rectangle) {
                                        cropper = cropper.getBounds();
                                        coll.beginEdit(pic, true);
                                        Dimension GLspDim = (Dimension) coll.getThumbnailPictureStore(pic).get(coll.ATTRIBUTE_ORIG_DIMENSION);
                                        double zoom = 1.;
                                        if (imageCollectionInfo.containsKey(pic)) {
                                            if (imageCollectionInfo.get(pic).containsKey(INFO_ZOOM)) {
                                                zoom = ((Integer) imageCollectionInfo.get(pic).get(INFO_ZOOM)) / 100.;
                                            }
                                        }
                                        AffineTransform GLtx = getScaleCphotoBounds(curBds, new Rectangle(GLspDim));
                                        GLtx.concatenate(AffineTransform.getScaleInstance(zoom, zoom).createInverse());
                                        cropper = GLtx.createTransformedShape(cropper).getBounds();
                                        coll.endEdit(pic, true);
                                        cropper = getScaleCphotoBounds(picBds, curBds).createTransformedShape(cropper).getBounds();
                                        cropper.x += picBds.x;
                                        cropper.y += picBds.y;
                                        Stroke stk = g2.getStroke();
                                        Color c = g2.getColor();
                                        Composite cps = g2.getComposite();
                                        g2.setColor(Color.BLACK);
                                        Rectangle[] surround = _getSurrounders(new Dimension(getWidth(), getHeight()), cropper);
                                        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .7f));
                                        if (surround != null) {
                                            for (Rectangle surrounder : surround) {
                                                g2.fill(surrounder);
                                            }
                                        }
                                        g2.setComposite(cps);
                                        g2.setColor(Color.WHITE);
                                        g2.setStroke(new BasicStroke(0, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{5f, 2.5f}, 0));
                                        g2.draw(cropper);
                                        g2.setColor(Color.ORANGE);
                                        g2.setStroke(stk);
                                        g2.setColor(c);
                                    }
                                    g2.translate(bounds.x, bounds.y);
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            } finally {
                                sp.setBounds(origBds);
                                coll.endEdit(pic, thumbnail);
                            }
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                };
                renderer.addMouseWheelListener(mwl);
                return renderer;
            }
        };
    }

    /**/
    private void initRenderingCanvas(boolean forceNew) {
        if (forceNew || canvas == null) {
            canvas = new RenderingSceneGL(mainFrame.getGraphicsConfiguration().getDevice());
            canvas.addListener(new RenderingSceneListener() {
                public void reset() {
                    initRenderingCanvas(true);
                }

                public void modeChanged(int i) {
                }

                public void componentResized(ComponentEvent e) {
                }
            });
        }
        canvas.setDrawFPSEnabled(false);
        canvas.setDrawKeyEventsEnabled(false);
        canvas.setOrthoProj(true);
        canvas.setLWJGLAccel(true);
        canvas.setKeyboardDisabled(true);
        canvas.setMouseDisabled(false);
        canvas.setSwingMouseDisabled(false);
    }

    private AffineTransform getScaleCphotoBounds(Rectangle displayBounds, Rectangle cphotoBounds) {
        double scale = Math.min(displayBounds.getWidth() / cphotoBounds.getWidth(), displayBounds.getHeight() / cphotoBounds.getHeight());
        return AffineTransform.getScaleInstance(scale, scale);
    }

    private Rectangle getPictureBounds(Rectangle displayBounds, Rectangle cphotoBounds) {
        displayBounds = displayBounds.getBounds();
        if (displayBounds.width > 2 && displayBounds.height > 2) {
            displayBounds.grow(-2, -2);
        }
        Rectangle picBds = getScaleCphotoBounds(displayBounds, cphotoBounds).createTransformedShape(cphotoBounds).getBounds();
        picBds.setLocation((int) Math.round(displayBounds.getCenterX() - picBds.getWidth() / 2f), (int) Math.round(displayBounds.getCenterY() - picBds.getHeight() / 2f));
        return picBds;
    }
    PrinterJob pj = PrinterJob.getPrinterJob();

    public PageFormat marginPageFormat(PageFormat pjFormat) {
        Paper paper = pjFormat.getPaper();
        paper.setImageableArea(0, 0, paper.getWidth(), paper.getHeight());
        pjFormat = (PageFormat) pjFormat.clone();
        pjFormat.setPaper(paper);
        return pjFormat;
    }
    PageFormat pjFormat = pj.validatePage(marginPageFormat(pj.defaultPage()));

    private File getBackupFile(String k) {
        Properties p = (Properties) imageCollectionInfo.get(k).get(INFO_PROP);
        String pty = "backup";
        if (p.getProperty(pty) == null) {
            return null;
        }
        File f = new File(new File(defaultImageDirectory, "backup"), p.getProperty(pty));
        if (f.isFile()) {
            return f;
        } else {
            return null;
        }
    }
    public JLabel currentProcessThumb = new JLabel(UIMessage._getIcon(UIMessage.WAIT_TYPE, true), SwingConstants.LEADING);

    /**
     * @param tInterrupt a Thread that may be interrupted by the user to cancel
     * some operation running
     * @param progressBar
     * {@link UIMessage#getProgressBar(long) UIMessage progress bar}
     * @param a a button will display to do the specified action on click
     * @param opMessage will set iconLabel text
     * @param ui_location
     * {@link UIMessage#UI_CENTER}, {@link UIMessage#UI_BOTTOM_LEFT}, ...
     */
    private JDialog showLightPopupActivity(String opMessage, JLabel iconLabel, long progressBar, final Thread tInterrupt, int ui_location) {
        Action a = tInterrupt == null ? null : new AbstractAction(getLocalizedStringCap("abort")) {
            public void actionPerformed(ActionEvent e) {
                tInterrupt.interrupt();
            }
        };
        return showLightPopupActivity(opMessage, iconLabel, progressBar, a, ui_location);
    }

    /**
     * @param progressBar
     * {@link UIMessage#getProgressBar(long) UIMessage progress bar}
     * @param a a button will display to do the specified action on click
     * @param opMessage will set iconLabel text
     * @param ui_location
     * {@link UIMessage#UI_CENTER}, {@link UIMessage#UI_BOTTOM_LEFT}, ...
     */
    public JDialog showLightPopupActivity(String opMessage, JLabel iconLabel, long progressBar, Action a, int ui_location) {
        JPanel p = new JPanel(new BorderLayout(), true);
        if (iconLabel == null) {
            iconLabel = new JLabel(opMessage);
        } else {
            iconLabel.setText(opMessage);
        }
        p.add(iconLabel, BorderLayout.NORTH);
        if (progressBar != 0) {
            JProgressBar jpb = UIMessage.getProgressBar(progressBar);
            jpb.setIndeterminate(true);
            p.add(jpb, BorderLayout.SOUTH);
        }
        return UIMessage.showLightPopupMessage(p, a, picView, ui_location);
    }

    /**
     * @param a a button will display to do the specified action on click
     * @param opMessage will set iconLabel text
     * @param ui_location
     * {@link UIMessage#UI_CENTER}, {@link UIMessage#UI_BOTTOM_LEFT}, ...
     */
    public JDialog showLightPopupActivity(String opMessage, JLabel iconLabel, Action a, int ui_location) {
        return showLightPopupActivity(opMessage, iconLabel, 0, a, ui_location);
    }

    private void open(File f, boolean backupOrig, boolean overwrite) {
        String k = f.getPath();
        if (!overwrite && imageCollection.hasPicture(k, false)) {
            k = f.getParent() + f.separator + filePrefix_copy + f.getName();
        }
        open(f, k, backupOrig);
    }
    private String filePrefix_backup = "_orig_";
    private String filePrefix_copy = "copy_";

    private File makeBackupFile(File filename) {
        FileHelper._makeWritable(filename);
        return new File(defaultImageDirectory, "backup" + File.separator + filePrefix_backup + filename.hashCode() + "_" + filename.getName());
    }
    static String[] tileMimes = new String[]{
        ".*jpg", ".*jpeg", ".*jp2",
        ".*tif", ".*tiff"
    };

    public void open(final File f, final String k, final boolean backupOrig) {
        try {
            UIMessage.invokeSwingAndReturn(new SwingStaticReturn<Object>() {
                public Object run() {
                    _open(f, k, backupOrig);
                    return null;
                }
            });
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }

    private void _open(final File f, String k, boolean backupOrig) {
        if (f.exists()) {
            try {
                updateCurrentStatus(null, getLocalizedStringCap("Openingfile") + " " + f.getPath() + "...");
                if (f.canRead()) {
                    String mime = ImageCollection._getImageMimeType(f, "r");
                    String collectionMime = ImageTypeSpecifier.createFromBufferedImageType(ImageCollection._getBufferedType(mime)).getColorModel().hasAlpha() ? COLLECTION_MIME_UNTILED : COLLECTION_MIME_TILED;
                    imageCollection.addImage(f, k, collectionMime);
                    updateAddInfo(imageCollectionInfo, k, f, mime, backupOrig);
                    addRecentFile(f);
                    imageCollection.waitForThumbnail(k);
                    updateCurrentStatus(k, getLocalizedStringCap("done") + ".");
                } else {
                    updateCurrentStatus(null, getLocalizedStringCap("Nofileat") + " " + f.getPath());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                String[] lstr = LocalizedResources.localizedGetStringArray("label12", 3);
                String m = lstr[0] + ex.getMessage() + lstr[1] + f.getPath() + lstr[2];
                updateCurrentStatus(null, m);
                UIMessage.showLightPopupMessage(new JLabel(m, UIMessage._getIcon(UIMessage.ERROR_TYPE, false), SwingConstants.LEADING), null, picView, UIMessage.UI_BOTTOM_RIGHT);
            }
        } else {
            System.err.println(log("No such file exists " + f, LVL.APP_ERR));
        }
    }

    private void updateAddInfo(SortedMap<String, Map<Integer, Serializable>> imageCollectionInfo, String k, File f, String mime, boolean backupOrig) throws IOException {
        File origF = makeBackupFile(new File(k));
        String origK = origF.getName();
        if (backupOrig) {
            FileHelper._fileCopy(f, origF, false, true);
        }
        Properties p = new Properties();
        if (imageCollectionInfo.containsKey(k) && !backupOrig) {
            p = (Properties) imageCollectionInfo.get(k).get(INFO_PROP);
        } else {
            p.setProperty("path", f.getAbsolutePath());
            p.setProperty("title", f.getName());
            p.setProperty("lastmodifiedon", DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, Locale.getDefault()).format(new Date(f.lastModified())));
        }
        p.setProperty("mimetype", mime);
        p.setProperty("filesize", String.format("%1$.3f " + LocalizedResources.localized.getString("MB"), (float) f.length() / (1024 * 1024)));
        p.setProperty("description", "");
        p.setProperty("album", "");
        if (backupOrig) {
            p.setProperty("backup", origK);
        }
        Map<Integer, Serializable> map = Collections.synchronizedMap(new HashMap<Integer, Serializable>());
        map.put(INFO_PROP, p);
        map.put(INFO_TIMESTAMP, f.lastModified());
        imageCollectionInfo.put(k, map);
    }

    public void updateCurrentStatus(final String key, final String statusString) {
        if (statusString != null) {
            statusLabel.setText(statusString);
        }
        statusLabel.repaint();
        if (key != null) {
            try {
                imageCollection.waitForThumbnail(key);
                imageCollection.beginEdit(key, true);
                Sprite thumb = imageCollection.getCollectionSprite(key, true);
                currentProcessThumb.setIcon(new ImageIcon(thumb.getScaled(.4, .4, picView)));
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            } finally {
                imageCollection.endEdit(key, true);
            }
        }
        currentProcessThumb.repaint();
    }

    private void save(final File f, final String selectedPic, final float compressionQuality) {
        try {
            UIMessage.invokeSwingAndReturn(new SwingStaticReturn<Object>() {
                public Object run() {
                    _save(f, selectedPic, compressionQuality);
                    return null;
                }
            });
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }

    private void _save(File f, String selectedPic, float compressionQuality) {
        boolean debug = _debug;
        _debug = _debugops;
        try {
            String[] lstr = LocalizedResources.localizedGetStringArray("label13", 3);
            updateCurrentStatus(selectedPic, lstr[0] + selectedPic + lstr[1] + f.getPath() + lstr[2]);
            String outputMime = ImageCollection._getImageMimeType(f, "w");
            imageCollection.beginEdit(selectedPic, false);
            Map<String, Serializable> store = imageCollection.getPictureStore(selectedPic);
            outputMime = (String) (outputMime == null ? store.get(ImageCollection.ATTRIBUTE_MIME) : outputMime);
            Sprite output = (Sprite) store.get(ImageCollection.ATTRIBUTE_SPRITE);/*new Sprite((BufferedImage) image, outputMime, new Dimension(image.getWidth(mainFrame), image.getHeight(mainFrame)));*/

            output.setBufferedType(ImageCollection._getBufferedType(outputMime));
            String collMime = output.getStoreMime();
            output.setStoreMime(outputMime);
            output.getIO().addIIOWriteProgressListener(wLed.getIIOWriteProgressAdapter());
            File tmp = FileHelper._createTempFile("imageBrowser_", _TMPDIRECTORY, true);
            output.runValidate();
            output.writeImageToOutput(tmp, compressionQuality);
            output.setStoreMime(collMime);
            output.setBufferedType(ImageCollection._getBufferedType(collMime));
            RandomAccessFile raf = new RandomAccessFile(tmp, "r");
            RandomAccessFile rafCopy = new RandomAccessFile(f, "rw");
            rafCopy.setLength(tmp.length());
            byte[] b = new byte[_BIGBUFFER_SIZE];
            int rBytes = 0;
            while ((rBytes = raf.read(b)) != -1) {
                rafCopy.write(b, 0, rBytes);
            }
            rafCopy.close();
            raf.close();
            tmp.delete();
            lstr = LocalizedResources.localizedGetStringArray("label14", 3);
            updateCurrentStatus(selectedPic, lstr[0] + selectedPic + lstr[1] + f.getPath() + lstr[2]);
            edit_savedState(new File(selectedPic).exists(), selectedPic);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            updateCurrentStatus(null, getLocalizedStringCap("Operationsbeeninterrupted."));
        } catch (IOException ex) {
            ex.printStackTrace();
            String[] lstr = LocalizedResources.localizedGetStringArray("label15", 2);
            updateCurrentStatus(null, lstr[0] + ex.getMessage() + lstr[1]);
        } catch (JXAException ex) {
            ex.printStackTrace();
            String[] lstr = LocalizedResources.localizedGetStringArray("label15", 2);
            updateCurrentStatus(null, lstr[0] + ex.getMessage() + lstr[1]);
        } finally {
            imageCollection.endEdit(selectedPic, false);
            _debug = debug;
        }
    }
    private static List<File> recentFiles = new ArrayList<File>();
    private static SortedMap<String, String> userPassStore = Collections.synchronizedSortedMap(new TreeMap<String, String>());
    final static PasswordStore passs = new PasswordStore() {
        @Override
        public boolean set(String user, String server, char[] pass) {
            userPassStoreTmp.put(user, new String(pass));
            return true;
        }

        @Override
        public char[] get(String user, String server) {
            return userPassStoreTmp.containsKey(user) ? userPassStoreTmp.get(user).toCharArray() : new char[]{};
        }
    };
    final static SortedMap<String, String> userPassStoreTmp = Collections.synchronizedSortedMap(new TreeMap<String, String>(userPassStore));
    final static UserNameStore users = new UserNameStore() {
        @Override
        public String[] getUserNames() {
            return userPassStoreTmp.keySet().toArray(new String[]{});
        }

        @Override
        public void setUserNames(String[] users) {
            for (String user : users) {
                addUserName(user);
            }
        }

        @Override
        public void loadUserNames() {
            userPassStoreTmp.clear();
            userPassStoreTmp.putAll(userPassStore);
        }

        @Override
        public void saveUserNames() {
            userPassStore.putAll(userPassStoreTmp);
        }

        @Override
        public boolean containsUserName(String user) {
            return userPassStoreTmp.containsKey(user);
        }

        @Override
        public void addUserName(String user) {
            if (!userPassStoreTmp.containsKey(user)) {
                userPassStoreTmp.put(user, "");
            }
        }

        @Override
        public void removeUserName(String user) {
            userPassStoreTmp.remove(user);
        }
    };
    private static File applicationStore = new File(FileHelper._USERHOMESTOREDIRECTORY, "." + System.getProperty("user.name") + ".imc.bkp");
    protected static Sprite unSavedPic;
    public static ImageIcon contrastIcon, brightnessIcon, cropIcon, magicIcon, lutIcon, cropIcon_64, magicIcon_64, lutIcon_64;

    private static void loadApplication(File fileStore) {
        try {
            Sprite._getRenderingHints().put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            Sprite._getRenderingHints().put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            Sprite._getRenderingHints().put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
            Sprite._getRenderingHints().put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            Sprite._getRenderingHints().put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            Sprite._getRenderingHints().put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            unSavedPic = new Sprite("/net/sf/jiga/xtended/ui/images/middle.gif", true, "image/gif", new Dimension(32, 32), true);
            unSavedPic.setStoreMode(Sprite.MODE_JAVA2D);
            Sprite sp = new Sprite("/jxa/contrast.png", true, "image/x-png", new Dimension(16, 16), true);
            sp.setStoreMode(Sprite.MODE_JAVA2D);
            sp.runValidate();
            contrastIcon = new ImageIcon(sp.toBuffered());
            sp = new Sprite("/jxa/brightness.png", true, "image/x-png", new Dimension(16, 16), true);
            sp.setStoreMode(Sprite.MODE_JAVA2D);
            sp.runValidate();
            brightnessIcon = new ImageIcon(sp.toBuffered());
            sp = new Sprite("/jxa/crop.png", true, "image/x-png", new Dimension(16, 16), true);
            sp.setStoreMode(Sprite.MODE_JAVA2D);
            sp.runValidate();
            cropIcon = new ImageIcon(sp.toBuffered());
            sp.setSize(24, 24);
            sp.setStoreMode(Sprite.MODE_JAVA2D);
            sp.runValidate();
            cropIcon_64 = new ImageIcon(sp.toBuffered());
            sp = new Sprite("/jxa/magic.png", true, "image/x-png", new Dimension(16, 16), true);
            sp.setStoreMode(Sprite.MODE_JAVA2D);
            sp.runValidate();
            magicIcon = new ImageIcon(sp.toBuffered());
            sp.setSize(24, 24);
            sp.setStoreMode(Sprite.MODE_JAVA2D);
            sp.runValidate();
            magicIcon_64 = new ImageIcon(sp.toBuffered());
            sp = new Sprite("/jxa/bright-contr.png", true, "image/x-png", new Dimension(16, 16), true);
            sp.setStoreMode(Sprite.MODE_JAVA2D);
            sp.runValidate();
            lutIcon = new ImageIcon(sp.toBuffered());
            sp.setSize(24, 24);
            sp.setStoreMode(Sprite.MODE_JAVA2D);
            sp.runValidate();
            lutIcon_64 = new ImageIcon(sp.toBuffered());
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (fileStore.exists()) {
                try {
                    RandomAccessFile raf = new RandomAccessFile(fileStore, "r");
                    ObjectInputStream in = new ObjectInputStream(new FileInputStream(raf.getFD()));
                    String version = in.readUTF();
                    if (version.equals(IMCBrowser.class.getPackage().getImplementationVersion())) {
                        JMFinstalled = in.readBoolean();
                        if (in.readBoolean()) {
                            List<File> fileList = (List<File>) in.readObject();
                            for (File f : fileList) {
                                recentFiles.add(f);
                            }
                            imageCollectionDB = (SortedMap) in.readObject();
                            imageCollectionInfoDB = (SortedMap) in.readObject();
                            userPassStore = (SortedMap) in.readObject();
                            users.setUserNames(userPassStore.keySet().toArray(new String[]{}));
                        }
                    }
                    raf.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void storeApplication(File fileStore) {
        storeApplication(fileStore, true);
    }

    private void storeApplication(File fileStore, boolean saveDatabases) {
        storeApplication(fileStore, saveDatabases, false);
    }

    private boolean savePosted = false;

    private void storeApplication(final File fileStore, final boolean saveDatabases, boolean sync) {
        try {
            if (!sync && saveDatabases) {
                if (savePosted) {
                    return;
                } else {
                    savePosted = true;
                }
                final JDialog p = UIMessage.showLightPopupMessage(new JLabel(getLocalizedStringCap("savingdatabases")), null, picView, UIMessage._BOTTOM_LEFT);
                ThreadWorks.securizedInvokeLater(new Runnable() {
                    public void run() {

                        int loadState = 0;
                        try {
                            do {
                                synchronized (imageCollectionDB) {
                                    imageCollectionDB.wait(500);
                                    for (Iterator<String> it = imageCollectionDB.keySet().iterator(); it.hasNext();) {
                                        String albumName = it.next();
                                        ImageCollection ic = imageCollectionDB.get(albumName);
                                        loadState |= ic.getState();
                                    }
                                }
                            } while ((loadState & ImageCollection.STATE_LOADING) != 0);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        } finally {
                            storeApplication(fileStore, true, true);
                            p.setVisible(false);
                            savePosted = false;
                        }
                    }
                });
            } else {
                if (fileStore.getParentFile() != null) {
                    fileStore.getParentFile().mkdirs();
                    FileHelper._makeWritable(fileStore.getParentFile());
                }
                File temp = FileHelper._createTempFile("ImageBrowser_", _TMPDIRECTORY, true);
                RandomAccessFile raf = new RandomAccessFile(temp, "rw");
                ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(raf.getFD())));
                out.writeUTF(IMCBrowser.class.getPackage().getImplementationVersion());
                out.writeBoolean(JMFinstalled);
                out.writeBoolean(saveDatabases);
                if (saveDatabases) {
                    out.writeObject(recentFiles);
                    synchronized (imageCollectionDB) {
                        for (Iterator<String> it = imageCollectionDB.keySet().iterator(); it.hasNext();) {
                            String albumName = it.next();
                            ImageCollection ic = imageCollectionDB.get(albumName);
                            waitForAllPictures(ic);
                            if (ic.keySet(false).isEmpty()) {
                                it.remove();
                                imageCollectionInfoDB.remove(albumName);
                            }
                        }
                    }
                    out.writeObject(imageCollectionDB);
                    out.writeObject(imageCollectionInfoDB);
                    users.saveUserNames();
                    out.writeObject(userPassStore);
                }
                out.flush();
                raf.close();
                FileHelper._fileCopy(temp, fileStore, false, true);
                temp.delete();
                if (JXAenvUtils.env.OS_WINDOWS.isEnv() && !JXAenvUtils.env.JAVA_VERSION_5.isEnv() && !JXAenvUtils.env.JAVA_VERSION_6.isEnv()) {
                    Files.setAttribute(fileStore.toPath(), "dos:hidden", true, LinkOption.NOFOLLOW_LINKS);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            new UIMessage(true, getLocalizedStringCap("anerroroccuredwhilesavingthedatabasesunsavedworkcanbelost"), null, UIMessage.ERROR_TYPE);
        }
    }
    private Vector<JMenu> recentFilesMenu = new Vector<JMenu>();

    private JMenu makeRecentFilesMenu() {
        recentFilesMenu.add(new JMenu((getLocalizedStringCap("recentfiles") + "...")));
        updateRecentFilesMenus();
        return recentFilesMenu.lastElement();
    }

    private void updateRecentFilesMenus() {
        for (JMenu menu : recentFilesMenu) {
            menu.removeAll();
            for (final File f : recentFiles) {
                menu.add(new AbstractAction(
                        f.getName()) {
                            public void actionPerformed(ActionEvent e) {
                                Runnable r = new Runnable() {
                                    public void run() {
                                        String op = getLocalizedStringCap("Opening");
                                        /*long wait = UIMessage.displayWaiting(op, currentProcessThumb, browser);*/
                                        final JDialog p = showLightPopupActivity(op, currentProcessThumb, null, UIMessage.UI_TOP_RIGHT);
                                        open(f, true, true);
                                        /*UIMessage.kill(wait);*/
                                        p.setVisible(false);
                                        doBrowserUpdate();
                                    }
                                };
                                canvas.doTaskOnScreen(r);
                            }
                        });
            }
            while (menu.getItemCount() > 5) {
                menu.remove(0);
            }
            menu.validate();
            menu.repaint();
        }
    }

    private void addRecentFile(final File f) {
        if (recentFiles.contains(f)) {
            return;
        }
        recentFiles.add(f);
        updateRecentFilesMenus();
    }

    public void callBrowserUpdate() {
        if (browserUpdatePosted) {
            return;
        } else {
            browserUpdatePosted = true;
        }
        ThreadWorks.Swing.invokeLater(new Runnable() {
            public void run() {
                if (picView.isEnabled()) {
                    _updateBrowser();
                }
            }
        });
    }

    public void doBrowserUpdate() {
        if (browserUpdatePosted) {
            return;
        } else {
            browserUpdatePosted = true;
        }
        try {
            if (ThreadWorks.Swing.isEventDispatchThread()) {
                _updateBrowser();
            } else {
                ThreadWorks.Swing.invokeAndWait(new Runnable() {
                    public void run() {
                        _updateBrowser();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void callFileInfoUpdate() {
        ThreadWorks.Swing.invokeLater(new Runnable() {
            public void run() {
                updateCurrentFileInfo();
            }
        });
    }
    public static Action loadAppStore = new AbstractAction(getLocalizedStringCap("albums")) {
        public void actionPerformed(ActionEvent e) {
            loadApplication(applicationStore);
        }
    };
    /**
     * check performance, may be unstable
     */
    public static boolean hardwareAccel = false, applet = false;
    public static File defaultImageDirectory = new File(System.getProperty("user.home") + File.separator + "Pictures" + File.separator + "Image Collection");

    static {
        defaultImageDirectory.mkdirs();
        FileHelper._makeWritable(defaultImageDirectory);
    }

    public boolean open(Object arg0) {
        if (arg0 instanceof Image) {
            try {
                Image image = (Image) arg0;
                edit_buffer(image);
                Dimension imageDim = new Dimension(image.getWidth(picView), image.getHeight(picView));
                String mime = COLLECTION_MIME_TILED;
                if (image instanceof RenderedImage) {
                    mime = ImageTypeSpecifier.createFromRenderedImage((RenderedImage) image).getColorModel().hasAlpha() ? COLLECTION_MIME_UNTILED : COLLECTION_MIME_TILED;
                }
                Sprite sp = new Sprite(image, mime, imageDim);
                sp.setBufferedType(ImageCollection._getBufferedType(mime));
                Properties props = ImageCollection._getPluginProperties(mime);
                String[] exts = props.getProperty(ImagePluginDescriptor.get.extension.toString()).split(" ");
                File f = new File(defaultImageDirectory.getPath() + File.separator + "photo-" + String.format("%1$tB,%1$ta %1$td - %1$tH-%1$tM-%1$tS - %1$tL", System.currentTimeMillis()) + "." + exts[0]);
                sp.setStoreMode(Sprite.MODE_JAVA2D);
                sp.runValidate();
                sp.writeImageToOutput(f, _DEF_CPR_QUALITY);
                sp.clearResource();
                open(f, true, true);
                storeApplication(applicationStore);
                callBrowserUpdate();
                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
                UIMessage.showLightPopupMessage(new JLabel(getLocalizedStringCap("IOErrorwhileaddinganexternalImage") + " : " + ex.getMessage(), UIMessage._getIcon(UIMessage.ERROR_TYPE, false), SwingConstants.LEADING), null, picView, UIMessage.UI_BOTTOM_RIGHT);
                return false;
            }
        } else if (arg0 instanceof java.util.List) {
            final java.util.List files = (java.util.List) arg0;
            Runnable r = new Runnable() {
                public void run() {
                    String op = getLocalizedStringCap("Opening");
                    long wait = UIMessage.newProgress(0, 100, picView);
                    JDialog pp = showLightPopupActivity(op, currentProcessThumb, wait, env.THREADING_MULTI.isEnv() ? Thread.currentThread() : null, UIMessage.UI_TOP_RIGHT);
                    int fstate = applet ? 0 : ((JFrame) mainFrame).getExtendedState();
                    edit_started(wait, op, files.size());
                    for (Object f : files) {
                        open((File) f, true, false);
                        edit_progress(wait, 1, files.size());
                        doBrowserUpdate();
                    }
                    edit_finished(wait, op, false, files.size());
                    pp.setVisible(false);
                    UIMessage.kill(wait);
                    storeApplication(applicationStore);
                }
            };
            canvas.doTaskOnScreen(r);
            return true;
        } else if (arg0 instanceof File) {
            open((File) arg0, true, false);
            storeApplication(applicationStore);
            callBrowserUpdate();
            return true;
        } else if (arg0 instanceof String) {
            File f = new File((String) arg0);
            if (f.exists()) {
                open(f, true, false);
                callBrowserUpdate();
                storeApplication(applicationStore);
                return true;
            }
        }
        return false;
    }

    public boolean save(String arg0) {
        File f = new File(arg0);
        save(f, selectedKeys.iterator().next(), _DEF_CPR_QUALITY);
        return true;
    }

    public void start() {
    }

    public void stop() {
    }
    DataFlavor[] importFlavors = new DataFlavor[]{DataFlavor.imageFlavor, DataFlavor.javaFileListFlavor, DataFlavor.stringFlavor};

    public DataFlavor[] getTransferDataFlavors() {
        if (selectedKeys.isEmpty()) {
            return new DataFlavor[]{};
        } else if (selectedKeys.size() == 1) {
            return new DataFlavor[]{DataFlavor.imageFlavor, DataFlavor.javaFileListFlavor, DataFlavor.stringFlavor};
        } else {
            return new DataFlavor[]{DataFlavor.javaFileListFlavor, DataFlavor.stringFlavor};
        }
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        for (DataFlavor df : getTransferDataFlavors()) {
            if (df.equals(flavor)) {
                return true;
            }
        }
        return false;
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (!isDataFlavorSupported(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        }
        List f;
        try {
            f = new Vector<File>();
            Object r = null;
            synchronized (selectedKeys) {
                if (selectedKeys.isEmpty()) {
                    r = null;
                } else {
                    for (String s : selectedKeys) {
                        if (flavor.equals(DataFlavor.imageFlavor)) {
                            imageCollection.beginEdit(s, false);
                            r = imageCollection.getPicture(mainFrame, s);
                            imageCollection.endEdit(s, false);
                            break;
                        } else if (flavor.equals(DataFlavor.javaFileListFlavor)) {
                            f.add(new File(s));
                            r = f;
                        } else if (flavor.equals(DataFlavor.stringFlavor)) {
                            r = new String(s.getBytes("UTF-8"));
                            break;
                        }
                    }
                }
            }
            return r;
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
            IOException e = new IOException(ex.getMessage());
            e.initCause(ex);
            throw e;
        }
    }

    public void lostOwnership(Clipboard clipboard, Transferable contents) {
    }

    public void setFrame(JFrame arg0) {
        mainFrame = arg0;
    }

    public void setApplet(JApplet ja) {
        mainFrame = ja;
    }

    public void destroyComponents() {
        destroy();
    }

    public void destroy() {
        ((RootPaneContainer) mainFrame).getContentPane().removeAll();
        ((RootPaneContainer) mainFrame).getContentPane().validate();
        mainFrame.setVisible(false);
        initialized = false;
    }
    boolean initialized = false;

    public boolean isInitialized() {
        return initialized;
    }

    public Runnable shutdownHook() {
        return null;
    }
}
