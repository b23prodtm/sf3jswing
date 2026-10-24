package net.sf.jiga.xtended.impl;
/*
 * Sprite.java Sf3JSwing Created by www.b23prodtm.info on 08.08.06. Copyright
 * 2006 __MyCompanyName__. All rights reserved.
 */

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;
import java.awt.image.ImageProducer;
import java.awt.image.RenderedImage;
import java.awt.image.VolatileImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.security.*;
import java.util.List;
import java.util.*;
import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.FileCacheImageInputStream;
import javax.imageio.stream.ImageInputStream;
import org.eclipse.imagen.*;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import net.sf.jiga.xtended.JXAException;
import net.sf.jiga.xtended.impl.game.Custom;
import net.sf.jiga.xtended.impl.game.RenderingScene;
import net.sf.jiga.xtended.impl.game.RenderingSceneComponent;
import net.sf.jiga.xtended.impl.game.gl.GLFX.gfx;
import net.sf.jiga.xtended.impl.game.gl.*;
import net.sf.jiga.xtended.impl.system.BufferIO;
import net.sf.jiga.xtended.impl.system.ImagePool;
import net.sf.jiga.xtended.impl.system.SfMediaTracker;
import net.sf.jiga.xtended.kernel.*;
import static net.sf.jiga.xtended.kernel.JXAenvUtils._getSysBoolean;
import net.sf.jiga.xtended.kernel.ThreadWorks.Swing;
import net.sf.jiga.xtended.ui.Ant;
import net.sf.jiga.xtended.ui.ImageCollection;
import org.lwjgl.opengl.GL11;

/**
 * Sprite uses BufferedImage or VolatileImage instances to handle pictures of
 * any Java supported formats. (e.g. PNG images, JPEG compression, BMP bitmaps)
 * <br> NOTICE : some functions in this class provide a "useCache" param, which
 * should be set to "false" unless you explicitely need ImageIO In/OutputStream
 * cache features. Every other functions will be using or not the cache by
 * reading the {@linkplain #isUseIIOCacheEnabled() } boolean
 *
 * @discussion BufferedImage type used is TYPE_4BYTE_ABGR_PRE supporting
 * translucide images (PNG sprites, generally) <hr> NOTICE on code : the
 * sequence of code  <pre>boolean complete = _drawOrAnyotherCommand(thatReturnBoolean);
 * complete = complete && _drawOrAnyotherCommand()</pre> is UNSAFE : the boolean
 * agregators
 * <pre>&& or ||</pre> may return without processing the second member command.
 * USE INSTEAD :
 * <pre>complete = _drawOrAnyotherCommand(thatReturnBoolean) && complete;</pre>
 */
public class Sprite implements Debugger, CompositeCapable, Externalizable, Cloneable, Resource, Threaded, RenderingSceneComponent, Sf3Renderable {

    /**
     * low level debugging
     *
     * @see Ant#JXA_DEBUG_RENDER_LOW
     * @see DebugMap#setDebugLevelEnabled(boolean,
     * net.sf.jiga.xtended.kernel.Level)
     */
    public static final Level DBUG_RENDER_LOW = DebugMap._getInstance().newDebugLevel();

    /**
     * Tile flags, bitwise combined. _WRITE_SEQUENCE | _WRITE_INSERT |
     * _WRITE_REPLACE
     */
    public static final int _TILEFLAGS = SpriteIO._WRITE_SEQUENCE | SpriteIO._WRITE_INSERT | SpriteIO._WRITE_REPLACE;

    static {
        DebugMap._getInstance().associateDebugLevel(Sprite.class, DBUG_RENDER_LOW);
        DebugMap._getInstance().setDebugLevelEnabled(_getSysBoolean(Ant.JXA_DEBUG_RENDER_LOW), DBUG_RENDER_LOW);
    }
    /**
     *
     */
    protected transient AccessControlContext acc = AccessController.getContext();

    /**
     * returns true or false, whether the Sprite backend is accelerated by the
     * LocalGraphicsEnvironment or not
     *
     * @return true or false, whether the LocalGraphicsEnvironment accelerates
     * the Graphics or not, resp. (nothing to do with LWJGL acceleration)
     */
    public static boolean _isAccelerated() {
        return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            @Override
            public Boolean run() {
                return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getImageCapabilities().isAccelerated();
            }
        });
    }

    /**
     * returns true or false, whether the LocalGraphicsEnvironment can put the
     * Image into VRAM or not, resp. using VolatileImage instances
     *
     * @return true or false, whether the LocalGraphicsEnvironment can put the
     * Image into the VRAM or not, resp. using true VolatileImage's
     */
    public static boolean _isTrueVolatile() {
        return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            @Override
            public Boolean run() {
                return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getImageCapabilities().isTrueVolatile();
            }
        });
    }

    /**
     * returns sp new SpriteTransferHandler instance
     *
     * @return sp new SpriteTransferHandler instance
     * @see SpriteTransferHandler
     */
    public static SpriteTransferHandler _newTransferHandler() {
        return new SpriteTransferHandler();
    }
    /**
     * the DataFlavor used for the mime-type by this Sprite class
     *
     * @default DataFlavor.javaSerializedObjectMimeType
     */
    public static final String _MIME_TYPE = DataFlavor.javaSerializedObjectMimeType;
    /**
     * the file extensions supported by this Sprite class
     *
     * @default mc3 MC3
     */
    public static final String[] _MIME_EXT = new String[]{"mc1", "MC1"};
    /**
     * the DataFlavor used for identifying sp Sprite instance, as follows
     * <pre>
     * new DataFlavor(Sprite.class, "sf3jswing Sprite")</pre>
     */
    public static final DataFlavor _dataFlavor = new DataFlavor(Sprite.class, "sf3jswing Sprite");
    /**
     * supported DataFlavor's. Those involve the {@link DataFlavor#imageFlavor imageFlavor},
     * {@link DataFlavor.javaFileListFlavor javaFileListFlavor}, {@link DataFlavor.stringFlavor stringFlavor}, {@link #_dataFlavor Sprite DataFlavor}.
     */
    public static final DataFlavor[] _dataFlavors = new DataFlavor[]{DataFlavor.imageFlavor, DataFlavor.javaFileListFlavor, DataFlavor.stringFlavor, _dataFlavor};

    static {
        Custom._storeMIMETYPES(_MIME_TYPE, _MIME_EXT);
    }
    /**
     * serial version UID
     */
    private static final long serialVersionUID = 2323;
    /**
     * base URI link
     */
    protected URI base;
    /**
     * src
     */
    protected transient Object src = null;
    /**
     * tile temporary src
     */
    protected transient Object tileSrc = null;
    /**
     * tex src protected transient Object texSrc = null;
     */
    /**
     * Image data cache
     */
    private transient Image data;
    /**
     * Texture data cache
     */
    private SpritesCacheManager<Integer, Sf3Texture> _texData = null;
    /**
     * Texture data cache
     */
    private transient SortedMap<Integer, Sf3Texture> texData = null;
    /**
     * private transient int texRenderFormat = 0;
     */
    /**
     * cache type
     *
     * @see #VOLATILE
     * @see #BUFFERED
     */
    protected int cache;
    /**
     * hashCode to identify the sprite
     */
    private long hash = System.nanoTime();
    private final static BitStack _bits = new BitStack();
    /**
     * Dimensions protected Dimension size;
     */
    /**
     * Volatile buffer
     */
    public static final int VOLATILE = _bits._newBitRange();
    /**
     * Buffered buffer
     */
    public static final int BUFFERED = _bits._newBitRange();
    /**
     *
     */
    protected transient Sf3RenderableImpl renderableImpl = new Sf3RenderableImpl(Sprite.class, true);

    /**
     *
     * @return
     */
    public int getTexTilesX() {
        return texTilesX;
    }

    /**
     *
     * @return
     */
    public int getTexTilesY() {
        return texTilesY;
    }
    /**
     * Sprites, Animations and Models handlers are gathered here
     *
     * @see GLHandler#sTex
     */
    public static GLObjectHandler<SpriteGLHandler> _GLHandlers = RenderingSceneGL._GLSprites;

    /**
     * refreshes the texture contents. There are various ways it is refreshed :
     * <ol><li>if the Sprite is loaded from a source file, the texture is loaded
     * from the source.</li> <li>if the Sprite is created with a BufferedImage
     * as the source, then the texture will load from the current image
     * data.</li></ol> Recognize that the second option is similar to a
     * Render-To-Texture behaviour, but it is slower indeed !
     *
     * @see #clearTextData()
     * @see #loadTexData()
     */
    public void updateTexData() {
        updateTexData(0);
    }

    /**
     * refreshes the texture contents. There are various ways it is refreshed :
     * <ol><li>if the Sprite is loaded from a source file, the texture is loaded
     * from the source.</li> <li>if the Sprite is created with a BufferedImage
     * as the source, then the texture will load from the current image
     * data.</li></ol> Recognize that the second option is similar to a
     * Render-To-Texture behaviour, but it is slower indeed !
     *
     * @see #clearTextData()
     * @see #loadTexData()
     * @param texRenderFormat VRAM loading and rendering is done with the
     * specified
     * {@linkplain Sf3Texture#getRenderFormat() pixel internal render format}; 0
     * disables this setting.
     *
     */
    public void updateTexData(int texRenderFormat) {
        boolean isValid = valid;
        clearTexData();
        loadTexData(texRenderFormat, true);
        valid = isValid;
    }

    /**
     * associates this Sprite instance to an exisiting RenderingScene instance
     * where this Sprite instance is gonna be rendered. All properties are set
     * up so that they become fully compatible with the RenderingScene and
     * GraphicsConfiguration. The
     * {@link #setMt(MediaTracker, Component) MediaTracker is updated, the Component observer},
     * the {@link #setHardwareAccel(boolean) FB-Hardware acceleration}, and
     * further options, too.
     *
     * @param renderingScene the RenderingScene instance to associate with this
     * Sprite instance or null to disable the link, which is invoked EVERY call
     * to {@linkplain #runValidate()} or any another validation method.
     * @see #getRenderingScene()
     * @see #setDebugEnabled(boolean)
     * @see #setMultiThreadingEnabled(boolean)
     * @see #setMt(MediaTracker, Component)
     * @see #setHardwareAccel(boolean)
     * @see #setTextureModeEnabled(boolean)
     * @see #setJava2DModeEnabled(boolean)
     */
    @Override
    public void setRenderingScene(RenderingScene rsc) {
        this.rsc = rsc;
        renderableImpl.setRenderingScene(rsc);
        /**
         * update sprite "valid" state
         */
        Sf3RenderableImpl.modeCopy(renderableImpl, this);
        /**
         *
         */
        if (rsc instanceof RenderingScene) {
            setMt(rsc.getMt(), rsc);
        }
    }
    private transient RenderingScene rsc = null;

    /**
     * returns the current observer as if set up as sp RenderingScene instance,
     * this RenderingScene instance is returned back.
     *
     * @return the current observer as if set up as sp RenderingScene instance,
     * this RenderingScene instance is returned back
     * @see #setRenderingScene(RenderingScene)
     */
    @Override
    public RenderingScene getRenderingScene() {
        return rsc;
    }

    /**
     * sets the tracking priority to use in the MediaTracker
     *
     * @param trackerPty the priority value, must be >= 0
     * @see SfMediaTracker
     */
    public void setTrackerPty(int trackerPty) {
        final Monitor monitor = renderableImpl.imageSynch;
        synchronized (monitor) {
            if (data instanceof Image && trackerPty != renderableImpl.io.trackerPty) {
                valid = false;
                renderableImpl.io.trackerPty = trackerPty;
            } else if (renderableImpl.io.trackerPty != trackerPty) {
                valid = false;
                renderableImpl.io.trackerPty = trackerPty;
            }
        }
    }

    /**
     * returns the tracking priority for this Sprite instance
     *
     * @return the priority value used by the MediaTracker
     * @see SfMediaTracker
     */
    public int getTrackerPty() {
        return renderableImpl.io.trackerPty;
    }

    /**
     * default type {@link Sf3Texture#DEFAULT_TYPE}
     */
    public static int DEFAULT_TYPE = Sf3Texture.DEFAULT_TYPE;
    /**
     * buffered type {@link #DEFAULT_TYPE}
     */
    public int _type = DEFAULT_TYPE;
    /**
     * axial symetrics HORIZONTAL
     */
    public static final int HORIZONTAL = 0;
    /**
     * axial symetrics VERTICAL
     */
    public static final int VERTICAL = 1;
    /**
     * none constant
     */
    public static final int NONE = -1;
    /**
     * actual zoom
     *
     * @default 1.0
     */
    protected double zoom = 1.0;
    /**
     * actual mirror orientation
     */
    protected int mirror = NONE;
    /**
     * disk cached
     *
     * @deprecated not used
     */
    protected boolean diskCached;
    /**
     * image mime type
     */
    protected String mime;
    /**
     * the current applied AffineTransform
     */
    protected transient AffineTransform currentPreTransforming;
    /**
     * the current applied AffineTransform
     */
    protected transient PerspectiveTransform currentPrePerspectiveTransforming;
    /**
     * the currrent applied AffineTransform mirroring effect
     */
    protected transient AffineTransform currentPreMirroring;
    /**
     * the current applied AfinneTransform scaling effect
     */
    protected transient AffineTransform currentPreScaling;
    /**
     * the AffineTransform instance to apply
     */
    protected AffineTransform transforming = new AffineTransform();
    /**
     * the AffineTransform instance to apply
     */
    protected PerspectiveTransform perspectiveTransforming = new PerspectiveTransform();
    /**
     * the AffineTransform instance for mirroring effect to apply
     */
    protected AffineTransform mirroring = new AffineTransform();
    /**
     * the AffineTransform isntance for scaling effect to apply
     */
    protected AffineTransform scaling = new AffineTransform();
    /**
     * innerResource mode (inner(true)/outer(false) .jar)
     *
     * @default false
     */
    protected final boolean innerResource;
    /**
     * dis/enables the loading of the image data (used for sub-classes like
     * Animation)
     */
    private boolean load = true;
    /**
     * @deprecated
     */
    protected int MAX_SIZE = 4000000;
    /**
     * dis/enables the Composite effect
     */
    protected boolean compositeEnabled = false;
    /**
     * the Paint instance for effect
     */
    protected transient Paint pnt = null;
    /**
     * the Color instance for effect
     */
    protected transient Color clr = null;
    /**
     * the Composite instance for effect
     */
    protected transient Composite cps = null;
    /**
     * whether Composite effect has been applied
     */
    private transient boolean composited;
    /**
     * the paint monitor switch
     */
    protected transient boolean painting = false;
    /**
     * the validate monitor switch
     */
    protected transient boolean validating = false;
    /**
     * the serialized attributes for this Sprite
     */
    protected HashMap<String, Serializable> attributes = new HashMap<String, Serializable>();

    /**
     * loads the default attributes values
     *
     * @param attributes the serializable attributes for this Sprite
     */
    protected void loadDefaultAttributes() {
        attributes.put("composited", false);
        attributes.put("currentPreTransforming", new AffineTransform());
        attributes.put("currentPreMirroring", new AffineTransform());
        attributes.put("currentPreScaling", new AffineTransform());
        attributes.put("currentPrePerspectiveTransforming", new PerspectiveTransform());
        attributes.put("storeMime", mime);
        resetAttributes();
    }

    /**
     * resets the attributes to the stored values
     */
    protected void resetAttributes() {
        composited = (Boolean) attributes.get("composited");
        currentPreTransforming = (AffineTransform) attributes.get("currentPreTransforming");
        currentPreMirroring = (AffineTransform) attributes.get("currentPreMirroring");
        currentPreScaling = (AffineTransform) attributes.get("currentPreScaling");
        currentPrePerspectiveTransforming = (PerspectiveTransform) attributes.get("currentPrePerspectiveTransforming");
        storeMime = attributes.containsKey("storeMime") ? (String) attributes.get("storeMime") : storeMime == null ? mime : storeMime;
    }

    /**
     * stores the current attributes in the serializable map instance
     */
    protected void storeCurrentAttributes() {
        attributes.put("composited", composited);
        attributes.put("currentPreTransforming", currentPreTransforming);
        attributes.put("currentPreMirroring", currentPreMirroring);
        attributes.put("currentPreScaling", currentPreScaling);
        attributes.put("currentPrePerspectiveTransforming", currentPrePerspectiveTransforming);
        attributes.put("storeMime", storeMime);
    }

    /**
     * clones the attributes in sp new map
     *
     * @return the cloned attributes map
     */
    protected HashMap<String, Serializable> cloneAttributes() {
        Map<String, Serializable> baseAttr = Collections.synchronizedMap(this.attributes);
        HashMap<String, Serializable> attributes = (HashMap<String, Serializable>) this.attributes.clone();
        /*
         * add cloneables
         */
        synchronized (baseAttr) {
            for (String k : baseAttr.keySet()) {
                if (baseAttr.get(k) instanceof AffineTransform) {
                    attributes.put(k, (Serializable) ((AffineTransform) baseAttr.get(k)).clone());
                } else if (baseAttr.get(k) instanceof PerspectiveTransform) {
                    attributes.put(k, (Serializable) ((PerspectiveTransform) baseAttr.get(k)).clone());
                }
            }
        }
        return attributes;
    }

    /**
     * no arg constructor (Java-bean)
     */
    public Sprite() {
        this(SpriteIO.createBufferedImage(SpriteIO._WRITE_TILES_DIMENSION, Sprite.DEFAULT_TYPE), "image/x-png", SpriteIO._WRITE_TILES_DIMENSION);
    }

    /**
     * Instances sp volatile image as the data of this sprite.
     *
     * @param filename path to image (relative to CWD), if rsrcMode is true,
     * then only the reachable resources paths are available.
     * @param size dimension of the sprite
     * @param rsrcMode innerResource mode en/disabled (inn/outer .jar filepath)
     * @param format image mime type (e.g. image/png)
     * @param buf whether to use buffered images or not
     * @throws URISyntaxException
     */
    public Sprite(String filename, boolean rsrcMode, String format, Dimension size, boolean buf) throws URISyntaxException {
        this(true, filename, rsrcMode, format, size, buf);
    }

    /**
     * Instances sp volatile image as the data of this sprite.
     *
     * @param load dis/enables the very loading of the image data (true is the
     * usual state of this property)
     * @param filename path to image (relative to CWD), if rsrcMode is true,
     * then only the reachable resources paths are available.
     * @param size dimension of the sprite
     * @param rsrcMode innerResource mode en/disabled (inn/outer .jar filepath)
     * @param format image mime type (e.g. image/png)
     * @param buf whether to use buffered images or not
     * @throws URISyntaxException
     */
    public Sprite(boolean load, String filename, boolean rsrcMode, String format, Dimension size, boolean buf) throws URISyntaxException {
        super();
        if (isDebugEnabled()) {
            System.out.println("loading " + filename);
        }
        if (rsrcMode) {
            URL url = !renderableImpl.io.isExtClassLoaderRes() ? Sprite.class.getResource(filename) : ExtensionsClassLoader.getInstance().getClassLoader().getResource(SpriteIO._contextResource(filename));/*
             * Thread.currentThread().getContextClassLoader().getResource(_contextResource(filename));
             */

            this.base = (url == null) ? null : url.toURI();
        } else {
            this.base = new File(filename).toURI();
        }
        if (base == null) {
            throw new JXAException(filename + " is not found by current context ClassLoader");
        }
        this.load = load;
        setSize(size);
        innerResource = rsrcMode;
        diskCached = true;
        cache = (buf) ? BUFFERED : VOLATILE;
        /*
         * setMinimumSize(size);
         */
        src = tileSrc = filename;
        mime = tileMime = format;
        _type = _getBufferedType(mime);
        loadDefaultAttributes();
    }

    /**
     * Instances sp volatile image as the data of this sprite
     *
     * @param file path to image (relative to CWD).
     * @param size dimension of the sprite
     * @param format image mime type (e.g. image/png)
     * @param buf whether to use buffered images or not
     * @throws URISyntaxException
     */
    public Sprite(File file, String format, Dimension size, boolean buf) throws URISyntaxException {
        this(true, file, format, size, buf);
    }

    /**
     * Instances sp volatile image as the data of this sprite.
     *
     * @param load dis/enables the very loading of the image data (true is the
     * usual state of this property)
     * @param file path to image (relative to CWD).
     * @param size dimension of the sprite
     * @param format image mime type (e.g. image/png)
     * @param buf whether to use buffered images or not
     * @throws URISyntaxException
     */
    public Sprite(boolean load, File file, String format, Dimension size, boolean buf) {
        super();
        if (isDebugEnabled()) {
            System.out.println("loading " + file);
        }
        this.base = file.toURI();
        if (base == null) {
            throw new JXAException(file.getAbsolutePath() + " was not found");
        }
        this.load = load;
        setSize(size);
        innerResource = false;
        diskCached = true;
        cache = (buf) ? BUFFERED : VOLATILE;
        /*
         * setMinimumSize(size);
         */
        src = tileSrc = file;
        mime = tileMime = format;
        _type = _getBufferedType(mime);
        loadDefaultAttributes();
    }

    /**
     * Instances sp volatile image as the data of this sprite.
     *
     * @param stream input stream to image
     * @param format image mime type
     * @param buf whether to set the Image BufferedImage or not (VolatileImage
     * instead)
     * @param size dimension of the sprite
     * @see java.io.InputStream
     * @see java.awt.Image
     */
    public Sprite(InputStream stream, String format, Dimension size, boolean buf) {
        this(true, stream, format, size, buf);
    }

    /**
     * Instances sp volatile image as the data of this sprite.
     *
     * @param load dis/enables the very loading of the image data (true is the
     * usual state of this property)
     *
     * @param stream input stream to image
     * @param format image mime type
     * @param buf whether to set the Image BufferedImage or not (VolatileImage
     * instead)
     * @param size dimension of the sprite
     * @see java.io.InputStream
     * @see java.awt.Image
     */
    public Sprite(boolean load, InputStream stream, String format, Dimension size, boolean buf) {
        super();
        this.load = load;
        setSize(size);
        if (isDebugEnabled()) {
            System.out.println("loading " + stream);
        }
        innerResource = false;
        diskCached = false;
        cache = (buf) ? BUFFERED : VOLATILE;
        /*
         * setMinimumSize(size);
         */
        src = tileSrc = stream;
        mime = tileMime = format;
        _type = _getBufferedType(mime);
        loadDefaultAttributes();
    }

    /**
     * Sets image data of this sprite.
     *
     * @param data Image
     * @param size dimension of the sprite
     * @param format image mime type
     * @discussion (comprehensive description)
     */
    public Sprite(Image data, String format, Dimension size) {
        this(true, data, format, size);
    }

    /**
     * Sets image data of this sprite.
     *
     * @param load dis/enables the very loading of the image data (true is the
     * usual state of this property)
     *
     * @param data Image
     * @param size dimension of the sprite
     * @param format image mime type
     * @discussion (comprehensive description)
     */
    public Sprite(boolean load, Image data, String format, Dimension size) {
        super();
        this.load = load;
        setSize(size);
        diskCached = false;
        this.data = data;
        /*
         * setMinimumSize(size);
         */
        cache = data instanceof VolatileImage ? VOLATILE : BUFFERED;
        src = tileSrc = data.getSource();
        mime = tileMime = format;
        _type = _getBufferedType(mime);
        innerResource = false;
        loadDefaultAttributes();
    }

    /**
     * returns the hash code for this Sprite
     */
    @Override
    public int hashCode() {
        return (int) hash;
    }

    /**
     * checks for equality of hash codes
     *
     * @param o the object instance to compare against
     */
    @Override
    public boolean equals(Object o) {
        return o != null ? hash == o.hashCode() : false;
    }

    /**
     * WaitFor(data) and Refreshes size of sprite to current image data
     * dimension.
     *
     * @see #setPreferredSize(Dimension)
     * @see #data
     */
    private void fitSizeToImage() {
        try {
            final Monitor monitor = renderableImpl.imageSynch;
            synchronized (monitor) {
                waitFor(data);
                if (!renderableImpl.isTileModeEnabled() && renderableImpl.isJava2DModeEnabled()) {
                    if (data == null) {
                        throw new JXAException("no data to fit size");
                    }
                    renderableImpl.bounds.setSize(new Dimension(data.getWidth(renderableImpl.io.obs), data.getHeight(renderableImpl.io.obs)));
                } else {
                    ImageReader r = renderableImpl.io.__getInput(tileSrc, innerResource, renderableImpl.io.getReader(0, tileMime), isUseIIOCacheEnabled());
                    renderableImpl.bounds.setSize(new Dimension(r.getWidth(0), r.getHeight(0)));
                    ((ImageInputStream) r.getInput()).flush();
                    ((ImageInputStream) r.getInput()).close();
                    r.dispose();
                }
            }
        } catch (Exception ex) {
            JXAException e = new JXAException(ex);
            throw e;
        } finally {
            if (DebugMap._getInstance().isDebugLevelEnabled(gfx.DBUG)) {
                DebugMap._getInstance().tickPrintln(System.out, tileSrc + ": " + renderableImpl.bounds.width + "x" + renderableImpl.bounds.height, true);
            }
        }
    }

    /**
     * sets the MediaTracker instance to use as loader and the Component that
     * will receive the image data
     *
     * @param mt the MediaTracker instance to use as loader for the image data
     * @param pobs the Component instance that will receive the image data
     */
    public void setMt(MediaTracker mt, Component pobs) {
        renderableImpl.io.obs = pobs;
        /*
         * if (obs instanceof Component) { setBackground(obs.getBackground());
         * setForeground(obs.getForeground()); }
         */
        if (renderableImpl.io.mt instanceof MediaTracker) {
            if (!renderableImpl.io.mt.equals(mt)) {
                if (load) {
                    final Monitor monitor = renderableImpl.imageSynch;
                    synchronized (monitor) {
                        if (DebugMap._getInstance().isDebugLevelEnabled(GLFX.gfx.DBUG)) {
                            DebugMap._getInstance().tickPrintln(System.out, "MediaTracker changed (" + renderableImpl.io.mt.hashCode() + " to " + mt.hashCode() + ") for " + toString(), true);
                        }
                        MediaTracker previous = renderableImpl.io.mt;
                        renderableImpl.io.mt = mt;
                        if (data instanceof Image) {
                            previous.removeImage(data);
                        }
                        valid = false;
                    }
                } else {
                    renderableImpl.io.mt = mt;
                }
            }
        } else {
            renderableImpl.io.mt = mt;
            if (load) {
                final Monitor monitor = renderableImpl.imageSynch;
                synchronized (monitor) {
                    valid = false;
                }
            }
        }
    }

    /**
     * returns the current MediaTracker instance used as the loader of the image
     * data buffer
     *
     * @return the MediaTracker instance
     * @see SfMediaTracker
     */
    public MediaTracker getMt() {
        return renderableImpl.io.mt;
    }

    /**
     * sp {@linkplain FileCacheImageInputStream} is used to read the specified
     * source. Use a MediaTracker to ensure correct loading. CAUTION : in tile
     * mode, the input must be flushed and closed as needed, because it is file
     * cached
     *
     * @param src InputStream, File, or String
     * @param innerResource dis/enables classpath loading (false/true resp.)
     * @param mime the mimetype of the source
     * @param renderMode JAVA2D_MODE (+ TILE_MODE)
     * @param bufferedType
     * @return map with "image" for the BufferedImage (JAVA2D_MODE w/o
     * TILE_MODE) or "reader" for the ImageReader (TILE_MODE) and "metadata" for
     * the IIOMetaData
     * @throws IOException
     */
    public static Map<String, Object> _loadImage(Object src, boolean innerResource, String mime, int renderMode, int bufferedType) {
        return _loadImage(src, innerResource, mime, renderMode, bufferedType, true);
    }

    /**
     * sp {@linkplain FileCacheImageInputStream} is used to read the specified
     * source. Use a MediaTracker to ensure correct loading. CAUTION : in tile
     * mode, the input must be flushed and closed as needed, because it is file
     * cached
     *
     * @param src InputStream, File, or String
     * @param innerResource dis/enables classpath loading (false/true resp.)
     * @param mime the mimetype of the source
     * @param renderMode JAVA2D_MODE (+ TILE_MODE)
     * @param bufferedType
     * @param useCache instance a FileCacheImageInputStream if possible
     * @return
     * @return map with "image" for the BufferedImage (JAVA2D_MODE w/o
     * TILE_MODE) or "reader" for the ImageReader (TILE_MODE) and "metadata" for
     * the IIOMetaData
     * @throws IOException
     */
    public static Map<String, Object> _loadImage(Object src, boolean innerResource, String mime, int renderMode, int bufferedType, boolean useCache) {
        ImageReader r = SpriteIO._getReader(0, mime);
        Map<String, Object> m = _loadImage(src, innerResource, r, renderMode, bufferedType, useCache);
        if ((renderMode & Sf3RenderableImpl.MODE_TILE) == 0) {
            r.dispose();
        }
        return m;
    }

    /**
     * sp {@linkplain FileCacheImageInputStream} is used to read the specified
     * source. Use a MediaTracker to ensure correct loading. CAUTION : in tile
     * mode, the input must be flushed and closed as needed, because it is file
     * cached
     *
     * @param src InputStream, File or String
     * @param innerResource dis/enables classpath loading (false/true resp.)
     * @param r sp suitable ImageReader instance
     * @param bufferedType ignored
     * @param renderMode JAVA2D_MODE (+ TILE_MODE)
     * @return map with "image" for the BufferedImage, "imagereadparam" for
     * ImageReadParam and "metadata" for the IIOMetaData
     * @throws IOException
     */
    public static Map<String, Object> _loadImage(final Object src, final boolean innerResource, final ImageReader r, final int renderMode, final int bufferedType) {
        return _loadImage(src, innerResource, r, renderMode, bufferedType, true);
    }

    /**
     * sp {@linkplain FileCacheImageInputStream} is used to read the specified
     * source. Use a MediaTracker to ensure correct loading. CAUTION : in tile
     * mode, the input must be flushed and closed as needed, because it is file
     * cached
     *
     * @param src InputStream, File or String
     * @param innerResource dis/enables classpath loading (false/true resp.)
     * @param r sp suitable ImageReader instance
     * @param bufferedType ignored
     * @param renderMode JAVA2D_MODE (+ TILE_MODE)
     * @param useCache instance a FileCacheImageInputStream if possible
     * @return map with "image" for the BufferedImage, "imagereadparam" for
     * ImageReadParam and "metadata" for the IIOMetaData
     * @throws IOException
     */
    public static Map<String, Object> _loadImage(final Object src, final boolean innerResource, final ImageReader r, final int renderMode, final int bufferedType, final boolean useCache) {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Map<String, Object>>() {
                @Override
                public Map<String, Object> run() throws IOException, IllegalArgumentException {
                    return __loadImage(src, innerResource, r, renderMode, bufferedType, useCache);
                }
            });
        } catch (PrivilegedActionException ex) {
            Exception e = ex.getException();
            JXAException ioe = new JXAException(e);
            throw ioe;
        }
    }

    /**
     * <b>WARNING : By using the returned ImageWriter, original contents of the
     * source may be definitely altered. A safe usage is to
     * {@linkplain #clone()} this Sprite instance first and use the cloned
     * instance to get the ImageWriter.</b> if {@linkplain #isTilesAvailable()}
     * returns false, then tiling may be enabled but the cache is not tiled. if
     * {@linkplain #isTilesAvailable()} returns true, then tiling may be enabled
     * and the cache is tiled hence {@linkplain #getTileReader()} can return
     * tiles. CAUTION : the output must be flushed and closed as needed, because
     * it is file cached NOTICE : if you plan to use prepareReplacePixels, the
     * underlying stream mustn't be compressed in any way. TIFF can be LZW or
     * JPEG compressed which may not be edited by the tileWriter. Usually, JXA
     * automatically disables compression upon tilingMode status, that is to
     * check isTilesAvailable() before to use getTileWriter().
     *
     * @see #getTileWriterParams(ImageWriter) to retrieve compatible write
     * params
     * @see #getTileMime()* public ImageWriter getTileWriter() { return
     * getTileWriter(true); }
     *
     ** <b>WARNING : By using the returned ImageWriter, original contents of
     * the source may be definitely altered. A safe usage is to
     * {@linkplain #clone()} this Sprite instance first and use the cloned
     * instance to get the ImageWriter.</b> if {@linkplain #isTilesAvailable()}
     * returns false, then tiling may be enabled but the cache is not tiled. if
     * {@linkplain #isTilesAvailable()} returns true, then tiling may be enabled
     * and the cache is tiled hence {@linkplain #getTileReader()} can return
     * tiles. CAUTION : the output must be flushed and closed as needed, because
     * it is file cached NOTICE : if you plan to use prepareReplacePixels, the
     * underlying stream mustn't be compressed in any way. TIFF can be LZW or
     * JPEG compressed which may not be edited by the tileWriter. Usually, JXA
     * automatically disables compression upon tilingMode status, that is to
     * check isTilesAvailable() before to use getTileWriter().
     * @see #getTileWriterParams(ImageWriter) to retrieve compatible write
     * params
     * @see #getTileMime() public ImageWriter getTileWriter(boolean useCache) {
     * ImageWriter w = getWriter(0, tileMime); try { w = __getOutput(tileSrc, w,
     * useCache); } catch (IOException ex) { if (JXAenvUtils._debug) {
     * ex.printStackTrace(); } } finally { return w; } }
     */
    /**
     * current tile source mime type (will change over sprite operations, e.g.
     * setSize() followed by runValidate() or write...())
     *
     * @return
     */
    public String getTileMime() {
        return tileMime;
    }

    /**
     * an ImageWriteParam instance is created for it with compression and tiling
     * enabled if available, as well as the destination type. Only TIFF are
     * available in this mode.
     *
     * @param w an ImageWriter e.g. {@link #prepareTileWrite(boolean)}
     * @return
     * @see #_WRITE_TILES_DIMENSION for custom tiling dimensions
     */
    public ImageWriteParam getTileWriterParams(ImageWriter w) {
        return getWriterParams(w, -1f);
    }

    /**
     * @param compressionQuality decimal between 0f and 1f, not available for
     * tiling.
     */
    public ImageWriteParam getWriterParams(ImageWriter w, float compressionQuality) {
        try {
            return renderableImpl.io._getIWP(w, renderableImpl.bounds.getSize(), w.getOriginatingProvider().getMIMETypes()[0], _type, compressionQuality);
        } catch (Exception ex) {
            if (isDebugEnabled()) {
                ex.printStackTrace();
            }
            return w.getDefaultWriteParam();
        }
    }

    /**
     * DOES NOT WORK WITH IMAGEPRODUCER AS {@linkplain #getSource() SOURCE} and
     * it will have {@link ImageReader#getInput()} == null . CAUTION : the
     * output must be flushed and closed as needed, because it is file cached
     *
     * @return
     * @see #getTileMime()
     */
    public ImageReader getTileReader() {
        return getTileReader(true);
    }

    /**
     * DOES NOT WORK WITH IMAGEPRODUCER AS {@linkplain #getSource() SOURCE} and
     * it will have {@link ImageReader#getInput()} == null . CAUTION : the
     * output must be flushed and closed as needed, if cached
     *
     * @param useCache
     * @return
     * @see #getTileMime()
     */
    public ImageReader getTileReader(boolean useCache) {
        ImageReader r = renderableImpl.io.getReader(0, tileMime);
        try {
            r = renderableImpl.io.__getInput(tileSrc, innerResource, r, useCache);
        } catch (Exception ex) {
            if (isDebugEnabled()) {
                ex.printStackTrace();
            }
        } finally {
            return r;
        }
    }

    /**
     * an ImageReadParam instance is created for it with compression and tiling
     * enabled if available, as well as the destination type
     *
     * @param r give a {@link #getTileReader()} returned reader ONLY
     * @return
     */
    public ImageReadParam getTileReaderParams(ImageReader r) {
        try {
            return renderableImpl.io._getIRP(r, _type);
        } catch (IllegalArgumentException ex) {
            if (isDebugEnabled()) {
                ex.printStackTrace();
            }
            return r.getDefaultReadParam();
        }
    }

    /**
     *
     */
    private static Map<String, Object> __loadImage(Object src, boolean innerResource, ImageReader r, int renderMode, int bufferedType, boolean useCache) throws IOException, IllegalArgumentException {
        Map<String, Object> imageMap = new HashMap<String, Object>();
        BufferedImage ndata = null;
        IIOMetadata metadata = null;
        r = SpriteIO.__getInput(src, innerResource, r, useCache);
        if ((renderMode & Sf3RenderableImpl.MODE_TILE) == 0) {
            ndata = r.read(0, SpriteIO._getIRP(r, bufferedType));
        }
        metadata = r.getImageMetadata(0);
        imageMap.put("imagereadparam", SpriteIO._getIRP(r, bufferedType));
        if ((renderMode & Sf3RenderableImpl.MODE_TILE) != 0) {
            imageMap.put("reader", r);
        } else {
            imageMap.put("image", ndata);
            if (!(src instanceof ImageInputStream)) {
                ((ImageInputStream) r.getInput()).flush();
                ((ImageInputStream) r.getInput()).close();
            }
        }
        imageMap.put("metadata", metadata);
        return imageMap;
    }

    /**
     * mime-type (for use with ImageReader and ImageWriter) mime can change to
     * {@linkplain #getStoreMime()} if the sprite is serialized
     * {@linkplain #isTileModeEnabled() tile mode}
     *
     * @return the current mime-type of this Sprite
     */
    public String getMime() {
        return mime;
    }

    /**
     * loads Image from the currently specified source or file cache
     *
     * @see #src
     * @return nothing
     */
    @Override
    public Object loadResource() {
        int currentPty = Thread.currentThread().getPriority();
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        try {
            final Monitor monitor = renderableImpl.imageSynch;
            synchronized (monitor) {
                boolean needRestoreContents = !checkState(data, null);
                if (load) {
                    Map<String, Object> image = null;
                    if (!renderableImpl.isTileModeEnabled()) {
                        if (renderableImpl.isJava2DModeEnabled() && (!(data instanceof Image) || needRestoreContents)) {
                            resetAttributes();
                            Image ndata = null;
                            valid = false;
                            /**
                             * load from file,stream image source
                             */
                            if (!(src instanceof ImageProducer) && !SpriteIO.NO_SOURCE.equals(src)) {
                                image = _loadImage(src, innerResource, mime, renderableImpl.getMode(), _type, isUseIIOCacheEnabled());
                                ndata = image.get("image") == null ? data : (Image) image.get("image");
                                ImagePool.getInstance().deleteObsoleteSource(tileSrc, this);
                                tileSrc = src;
                                tileMime = mime;
                            } else {
                                /**
                                 * get correct type of image (a blank one)
                                 */
                                if (renderableImpl.bounds.isEmpty()) {
                                    renderableImpl.bounds.setSize(SpriteIO._WRITE_TILES_DIMENSION);
                                }
                                ndata = (cache & BUFFERED) != 0 ? (Image) SpriteIO.createBufferedImage(renderableImpl.bounds.getSize(), _type) : (Image) createVolatileImage(renderableImpl.bounds.getSize(), _getTransparency(_type));
                            }
                            if (ndata instanceof Image) {
                                renderableImpl.io.trackImage(renderableImpl.io.trackerPty, ndata);
                            }
                            data = ndata;
                            renderableImpl.io.waitFor(renderableImpl.io.trackerPty);
                            if (ndata instanceof Image) {
                                renderableImpl.io.untrackImage(renderableImpl.io.trackerPty, ndata);
                            }
                            if (renderableImpl.bounds.isEmpty()) {
                                renderableImpl.bounds.setSize(data.getWidth(renderableImpl.io.obs), data.getHeight(renderableImpl.io.obs));
                            }
                        }
                    } else {
                        if (tileSrc == null) {
                            tileSrc = src;
                            tileMime = mime;
                        }
                        ImageReader r = renderableImpl.io.getReader(0, tileMime);
                        image = _loadImage(tileSrc, innerResource, r, renderableImpl.getMode(), _type, isUseIIOCacheEnabled());
                        if (renderableImpl.bounds.isEmpty()) {
                            renderableImpl.bounds.setSize(r.getWidth(0), r.getHeight(0));
                        }
                        ((ImageInputStream) r.getInput()).flush();
                        ((ImageInputStream) r.getInput()).close();
                        r.dispose();
                    }
                    if (isTextureModeEnabled()) {
                        loadTexData();
                    }
                }
            }
        } catch (Exception ex) {
            if (isDebugEnabled()) {
                ex.printStackTrace();
            }
        } finally {
            Thread.currentThread().setPriority(currentPty);
            return null;
        }
    }

    /**
     * the current texture associated to this Sprite if store or render mode is
     * 0, the map contains all tiles ordered from top-left to bottom-right,
     * starting at key 1. the key 0 is reserved for the texture in non-tile mode
     * and for an empty texture that has the same values of the whole texture.
     * e.g. the first tile in the last line is got by the key : 0 +
     * ({@linkplain #getTexTilesX()} * {@linkplain #getTexTilesY()} - 1) + 1
     *
     * @return the current texture (if loaded) to this Sprite or null
     */
    public SortedMap<Integer, Sf3Texture> getTexData() {
        final Monitor monitor = renderableImpl.imageSynch;
        synchronized (monitor) {
            return texData;
        }
    }

    /**
     * the current texture source
     *
     * @return the current available texture source (file path, image or stream)
     *
     * public Object getTexSource() { return texSrc; }
     */
    /**
     * loads the texture for this Sprite to be used in an openGL rendering
     * context (it's still not loaded in VRAM until the Sprite is rendered) This
     * method is called each time {@linkplain #loadResource()} is invoked.
     *
     * @see RenderingScene#_GLLoadVRAM2D(Sf3Texture)
     * @throws java.io.IOException
     */
    public void loadTexData() throws IOException {
        loadTexData(0, false);
    }
    /**
     *
     */
    private int texTilesX = 0, texTilesY = 0;

    /**
     * loads the texture for this Sprite to be used in an openGL rendering
     * context (it's still not loaded in VRAM until the Sprite is rendered) This
     * method is called each time {@linkplain #loadResource()} is invoked.
     *
     * @see RenderingScene#_GLLoadVRAM2D(Sf3Texture)
     * @throws java.io.IOException
     * @param texRenderFormat VRAM loading and rendering is done with the
     * specified
     * {@linkplain Sf3Texture#getRenderFormat() pixel internal render format};
     * @param force forces the texture data to be reloaded from the image cache
     * (use it in case the image contents have changed) 0 disables this setting.
     */
    public void loadTexData(final int texRenderFormat, final boolean force) {
        AccessController.doPrivileged(new PrivilegedAction() {
            @Override
            public Object run() {
                _loadTexData(texRenderFormat, force);
                return null;
            }
        }, acc);
    }

    /**
     * DOES NOT WORK WITH IMAGEPRODUCER AS {@linkplain #getSource() SOURCE}
     *
     * @return
     */
    public int getNumTileX() {
        ImageReader r = renderableImpl.io.getReader(0, tileMime);
        try {
            r = renderableImpl.io.__getInput(tileSrc, innerResource, r, isUseIIOCacheEnabled());
            int d = renderableImpl.io.getNumTilesXY(r).width;
            ((ImageInputStream) r.getInput()).flush();
            ((ImageInputStream) r.getInput()).close();
            r.dispose();
            return d;
        } catch (Exception ex) {
            if (isDebugEnabled()) {
                ex.printStackTrace();
            }
            return 1;
        }
    }

    /**
     * DOES NOT WORK WITH IMAGEPRODUCER AS {@linkplain #getSource() SOURCE}
     *
     * @return
     */
    public int getNumTileY() {
        ImageReader r = renderableImpl.io.getReader(0, tileMime);
        try {
            r = renderableImpl.io.__getInput(tileSrc, innerResource, r, isUseIIOCacheEnabled());
            int d = renderableImpl.io.getNumTilesXY(r).height;
            ((ImageInputStream) r.getInput()).flush();
            ((ImageInputStream) r.getInput()).close();
            r.dispose();
            return d;
        } catch (Exception ex) {
            if (isDebugEnabled()) {
                ex.printStackTrace();
            }
            return 1;
        }
    }

    private void _loadTexData(int texRenderFormat, boolean force) {
        int pty = Thread.currentThread().getPriority();
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        try {
            final Monitor monitor = renderableImpl.imageSynch;
            synchronized (monitor) {
                if (load) {
                    waitFor(data);
                    if (force || texData == null || _texData == null) {
                        int texHash = spTexMap.containsKey(hashCode()) ? spTexMap.get(hashCode()) : (int) System.nanoTime();
                        if (force) {
                            if (_texData != null) {
                                _texData.clear();
                                ImagePool.getInstance().deleteObsoleteSource(_texData, this);
                                _texData = null;
                            }
                            texData = null;
                        }
                        if (_texData == null) {
                            _texData = new SpritesCacheManager<Integer, Sf3Texture>();
                            _texData.setCompressionEnabled(false);
                            _texData.setSwapDiskEnabled(true);
                            texData = null;
                        }
                        if (texData == null) {
                            texData = Collections.synchronizedSortedMap(_texData);
                        }
                        Sf3Texture texture = null;
                        Dimension tiles = new Dimension(Sf3Texture._getNextPowerOfTwo(SpriteIO._WRITE_TILES_DIMENSION.width), Sf3Texture._getNextPowerOfTwo(SpriteIO._WRITE_TILES_DIMENSION.height));
                        if (src instanceof ImageProducer || SpriteIO.NO_SOURCE.equals(src)) {
                            texture = data instanceof Image ? Sf3Texture._loadTexture(texHash, convertToBuffered(data, renderableImpl.io.obs, false), 0) : GLHandler.newTexture(texHash, BufferIO._new(0), renderableImpl.bounds.getSize(), 0);
                        } else if (renderableImpl.isTileModeEnabled()) {
                            Map<String, Object> loader = _loadImage(tileSrc, innerResource, tileMime, renderableImpl.getMode(), _type, isUseIIOCacheEnabled());
                            ImageReader r = (ImageReader) loader.get("reader");
                            texture = GLHandler.newTexture(texHash, BufferIO._new(0), new Dimension(r.getWidth(0), r.getHeight(0)), 0);
                            ((ImageInputStream) r.getInput()).flush();
                            ((ImageInputStream) r.getInput()).close();
                            r.dispose();
                        } else {
                            Map<String, Object> loader = _loadImage(src, innerResource, mime, renderableImpl.getMode(), _type, isUseIIOCacheEnabled());
                            Image im = (Image) loader.get("image");
                            waitFor(im);
                            texture = Sf3Texture._loadTexture(texHash, im, 0);
                            im.flush();
                        }
                        texture.setPty(texPty);
                        if (!renderableImpl.isTileModeEnabled()) {
                            texData.put(0, texture);
                        } else {
                            ImageReader r = (ImageReader) _loadImage(tileSrc, innerResource, tileMime, renderableImpl.getMode(), _type, isUseIIOCacheEnabled()).get("reader");
                            if (DebugMap._getInstance().isDebugLevelEnabled(gfx.DBUG)) {
                                DebugMap._getInstance().tickPrintln(System.out, "@@@@ TILE MODE create tiles-textures cache from tiled picture...", false);
                            }
                            tiles = SpriteIO._WRITE_TILES_DIMENSION;
                            Dimension tilesXY = renderableImpl.io.getNumTilesXY(r);
                            Dimension tiledImage = new Dimension(r.getWidth(0), r.getHeight(0));
                            final int tilesX = tilesXY.width;
                            final int tilesY = tilesXY.height;
                            float wRatioPOT = (float) tiles.width / (float) Sf3Texture._getNextPowerOfTwo(tiles.width);
                            float hRatioPOT = (float) tiles.height / (float) Sf3Texture._getNextPowerOfTwo(tiles.height);
                            final Dimension potTiled = new Dimension(tilesX * Sf3Texture._getNextPowerOfTwo(tiles.width),
                                    tilesY * Sf3Texture._getNextPowerOfTwo(tiles.height));
                            potTiled.width = potTiled.width - Math.round((float) (tilesX * tiles.width - tiledImage.width) / wRatioPOT);
                            potTiled.height = potTiled.height - Math.round((float) (tilesY * tiles.height - tiledImage.height) / hRatioPOT);
                            texData.put(0, texture);
                            for (int i = 0; i < tilesX; i++) {
                                for (int j = 0; j < tilesY; j++) {
                                    ImageReadParam irp = SpriteIO._getIRP(r, _type);
                                    Rectangle sroi = new Rectangle(i * tiles.width, j * tiles.height, tiles.width, tiles.height).intersection(new Rectangle(tiledImage));
                                    if (sroi.isEmpty()) {
                                        continue;
                                    }
                                    irp.setSourceRegion(sroi);
                                    BufferedImage tBi = r.read(0, irp);
                                    renderableImpl.io.trackImage(renderableImpl.io.trackerPty, tBi);
                                    renderableImpl.io.waitFor(renderableImpl.io.trackerPty);
                                    renderableImpl.io.untrackImage(renderableImpl.io.trackerPty, tBi);
                                    Image tBis;
                                    if (!new Dimension(tBi.getWidth(), tBi.getHeight()).equals(tiles)) {
                                        tBis = SpriteIO.createBufferedImage(tiles, _type);
                                        clearImage(tBis);
                                        Graphics2D g = wrapRendering(_createImageGraphics(tBis));
                                        g.drawRenderedImage(tBi, new AffineTransform());
                                        g.dispose();
                                        tBi.flush();
                                        if (DebugMap._getInstance().isDebugLevelEnabled(gfx.DBUG)) {
                                            DebugMap._getInstance().tickPrintln(System.out, "bi " + tBis.toString(), false);
                                        }
                                    } else {
                                        if (tBi.getType() != _type) {
                                            tBis = convertToBuffered((RenderedImage) tBi, renderableImpl.io.obs, true);
                                        } else {
                                            tBis = tBi;
                                        }
                                    }
                                    /**
                                     * new tile (new hash)
                                     */
                                    Sf3Texture tile = Sf3Texture._loadTexture(tBis, 0);
                                    tile.setPty(texPty);
                                    texData.put(i + j * tilesX + 1, tile);
                                    tBis.flush();
                                }
                            }
                            ((ImageInputStream) r.getInput()).flush();
                            ((ImageInputStream) r.getInput()).close();
                            r.dispose();
                            texTilesX = tilesX;
                            texTilesY = tilesY;
                            if (DebugMap._getInstance().isDebugLevelEnabled(gfx.DBUG)) {
                                DebugMap._getInstance().tickPrintln(System.out, "@@@@ done.", true);
                            }
                        }
                    }
                    if (texData instanceof SortedMap) {
                        /*
                         * texRef = new SoftReference(texData, refQueue);
                         */
                        if (isDebugEnabled()) {
                            DebugMap._getInstance().tickPrintln(System.out, "@@@@ " + toString() + " TEX CACHE IS " + texData.keySet().size() + " texture(s) big (> 1 means it is tiled from 1-key to (size-1)-key and the 0-key returns an empty texture)", false);
                            DebugMap._getInstance().tickPrintln(System.out, "context is " + Thread.currentThread().getContextClassLoader(), false);
                            DebugMap._getInstance().tickPrintln(System.out, "Sf3Texture loader is " + Sf3Texture.class.getClassLoader(), false);
                            DebugMap._getInstance().tickPrintln(System.out, "cache data is loaded by " + texData.get(0).getClass().getClassLoader(), true);
                        }
                        Sf3Texture ref = texData.get(0);
                        if (ref != null) {
                            if (texRenderFormat != ref.getRenderFormat() || texPty != ref.getPty()) {
                                if (texRenderFormat != 0) {
                                    ref.setRenderFormat(texRenderFormat);
                                }
                                ref.setPty(texPty);
                                texData.put(0, ref);
                            }
                            spTexMap.put(hashCode(), ref.hashCode());
                        } else {
                            if (isDebugEnabled()) {
                                System.err.println(JXAenvUtils.log("no texture available !" + toString(), JXAenvUtils.LVL.APP_WRN));
                            }
                        }
                    } else {
                        if (isDebugEnabled()) {
                            System.err.println(JXAenvUtils.log("Though specified, no Texture has been loaded " + toString(), JXAenvUtils.LVL.APP_WRN));
                        }
                    }
                }
            }
            /*
             * } catch (ClassNotFoundException ex) { if (isDebugEnabled()) {
             * ex.printStackTrace(); }
             */
        } catch (Exception ex) {
            if (isDebugEnabled()) {
                ex.printStackTrace();
            }
            JXAException e = new JXAException(ex);
            throw e;
        } finally {
            Thread.currentThread().setPriority(pty);
        }
    }

    /**
     * Java2D gradients
     */
    protected final static Map<Integer, GradientPaint> FX_gradients = Collections.synchronizedMap(new HashMap<Integer, GradientPaint>());

    /**
     * draws with Transforms concatenated as follows : tx X ptx x dataPixels.
     *
     * @param g Graphics2D instance to draw to
     * @param tx transform to be applied to drawing (unstable with some image
     * types)
     * @param ptx perspective Transform to be applied on drawing
     * @return true or false whether the painting has completed or not (this
     * returned value cannot be used for synchronization purposals)
     * @throws InterruptedException if an interruption has been requested while
     * painting
     * @throws NullPointerException if image data is null
     */
    private boolean __draw(Component pobs, Graphics2D g2, AffineTransform tx, PerspectiveTransform ptx, int fx, Point fx_loc, Color fx_color) throws InterruptedException {
        /*
         * if ((renderMode & MODE_TILE) != 0) { throw new
         * InterruptedException("unable to paint with the tile mode enabled"); }
         */
        if (!renderableImpl.isJava2DModeEnabled()) {
            throw new JXAException("unable to paint if the Java2D mode is disabled");
        }
        if (!SwingUtilities.isEventDispatchThread()) {
            System.err.println(JXAenvUtils.log("painting outside EDT !", JXAenvUtils.LVL.APP_WRN));
        }
        renderableImpl.io.obs = pobs;
        Throwable interrupt_ = null;
        boolean complete = true;
        Sprite clone = null;
        fx &= ~GLFX.bits._getAllBitRanges();
        /*
         * if (renderableImpl.isTileModeEnabled()) { if (isDebugEnabled()) {
         * System.err.println(toString() + " : tile mode disables fx in java2d
         * render mode !"); } fx = gfx.FX_NONE; }
         */
        if (fx != gfx.FX_NONE) {
            clone = (Sprite) clone();
        }
        final Monitor monitor = renderableImpl.validateMonitor;
        synchronized (monitor) {
            while (validating) {
                if (DebugMap._getInstance().isDebugLevelEnabled(gfx.DBUG)) {
                    DebugMap._getInstance().tickPrintln(System.out, "validating " + toString() + " sprite's waiting for...", false);
                }
                monitor.wait(1000);
            }
            if (!valid) {
                if (isDebugEnabled()) {
                    System.err.println("." + (!valid ? "!INVALID!" : ""));
                }
                return false;
            }
            painting = true;
            final Monitor monitor1 = renderableImpl.imageSynch;
            synchronized (monitor1) {
                Shape backgroundClip = g2.getClip();
                try {
                    waitFor(data);
                    if (data == null && !renderableImpl.isTileModeEnabled() || tileSrc == null && renderableImpl.isTileModeEnabled()) {
                        throw new JXAException("Sprite " + toString() + " has no data to draw ! try (re)validate().");
                    } else {
                        if (DebugMap._getInstance().isDebugLevelEnabled(gfx.DBUG)) {
                            DebugMap._getInstance().tickPrintln(System.out, ">>>>>> Java2D draw image size : " + renderableImpl.bounds.getWidth() + "x" + renderableImpl.bounds.getHeight() + " on clip : " + g2.getClipBounds() + " <<<<<< " + toString(), false);
                        }
                    }
                    AffineTransform baseTx = new AffineTransform();
                    if (tx != null) {
                        baseTx.concatenate(tx);
                    }
                    PerspectiveTransform basePtx = new PerspectiveTransform();
                    if (ptx != null) {
                        basePtx.concatenate(ptx);
                    }
                    if (_paintStatus) {
                        g2.drawString("sprite v", (int) renderableImpl.bounds.getX(), (int) renderableImpl.bounds.getY());
                    }
                    if (baseTx.getScaleX() == 0.0 || baseTx.getScaleY() == 0.0 || baseTx.getDeterminant() == 0.0) {
                        if (DebugMap._getInstance().isDebugLevelEnabled(gfx.DBUG)) {
                            DebugMap._getInstance().tickPrintln(System.out, "transform " + toString() + " contains Zero, so is avoided. TX=" + baseTx, true);
                        }
                        throw new JXAException(toString() + " transform failed! " + baseTx);
                    }
                    if (!renderableImpl.isTileModeEnabled()) {
                        if (data.getWidth(renderableImpl.io.obs) <= 0 || data.getHeight(renderableImpl.io.obs) <= 0) {
                            throw new JXAException(toString() + " image data size was not available!");
                        }
                    }
                    if (!basePtx.isIdentity()) {
                        basePtx.preConcatenate(baseTx.createInverse());
                        double[] perspectiveTransform = new double[]{0, 0, renderableImpl.bounds.getWidth(), 0, renderableImpl.bounds.getWidth(), renderableImpl.bounds.getHeight(), 0, renderableImpl.bounds.getHeight()};
                        basePtx.inverseTransform(perspectiveTransform, 0, perspectiveTransform, 0, 4);
                        Polygon perspectiveTransformPoly = new Polygon();
                        for (int i = 0; i < perspectiveTransform.length; i += 2) {
                            perspectiveTransformPoly.addPoint((int) perspectiveTransform[i], (int) perspectiveTransform[i + 1]);
                        }
                        if (DebugMap._getInstance().isDebugLevelEnabled(gfx.DBUG)) {
                            DebugMap._getInstance().tickPrintln(System.out, "Warped area : " + perspectiveTransformPoly.getBounds(), false);
                        }
                        g2.translate(getLocation().x, getLocation().y);
                        g2.clip(perspectiveTransformPoly);
                        if (isDebugEnabled()) {
                            g2.draw(perspectiveTransformPoly);
                        }
                        g2.translate(-getLocation().x, -getLocation().y);
                    } else if (!baseTx.isIdentity() && basePtx.isIdentity()) {
                        Rectangle transform = baseTx.createTransformedShape(new Rectangle(renderableImpl.bounds.getSize())).getBounds();
                        if (DebugMap._getInstance().isDebugLevelEnabled(gfx.DBUG)) {
                            DebugMap._getInstance().tickPrintln(System.out, "TXShape : " + transform, false);
                        }
                        if (transform.getSize().height <= 1 || transform.getSize().width <= 1) {
                            throw new JXAException(toString() + " transform failed! " + transform);
                        }
                        g2.translate(getLocation().x, getLocation().y);
                        g2.clip(transform);
                        if (isDebugEnabled()) {
                            g2.draw(transform);
                        }
                        g2.translate(-getLocation().x, -getLocation().y);
                    } else {
                        g2.clip(renderableImpl.bounds);
                    }
                    if (isDebugEnabled()) {
                        g2.draw(renderableImpl.bounds);
                    }
                    AffineTransform fx_tx = new AffineTransform();
                    AffineTransform fx_tx_mirror = new AffineTransform();
                    long now = System.currentTimeMillis();
                    if (DebugMap._getInstance().isDebugLevelEnabled(gfx.DBUG)) {
                        DebugMap._getInstance().tickPrint(System.out, "FX : ", false);
                        for (gfx select : gfx.values()) {
                            if ((select.bit() & fx) != 0) {
                                DebugMap._getInstance().tickPrint(System.out, " " + select, false);
                            }
                        }
                        DebugMap._getInstance().tickPrintln(System.out, ".", false);
                    }
                    /**
                     * START SCROLLLING
                     */
                    if ((fx & gfx.FX_SCROLLING.bit()) != 0) {
                        if (!(Sprite.fx.FX_lastTransformScrolling.get(hashCode()) instanceof AffineTransform)) {
                            Sprite.fx.FX_lastTransformScrolling.put(hashCode(), new AffineTransform());
                        }
                        if (!(Sprite.fx.FX_lastTransformTime.get(hashCode()) instanceof Long)) {
                            Sprite.fx.FX_lastTransformTime.put(hashCode(), now);
                        }
                        double translateH, translateV;
                        translateH = (fx & gfx.FX_HORIZONTAL.bit()) != 0 ? Sprite.fx.FX_overallSPEED * (now - Sprite.fx.FX_lastTransformTime.get(hashCode())) : 0f;
                        translateV = (fx & gfx.FX_VERTICAL.bit()) != 0 ? Sprite.fx.FX_overallSPEED * (now - Sprite.fx.FX_lastTransformTime.get(hashCode())) : 0f;
                        Sprite.fx.FX_lastTransformScrolling.get(hashCode()).setToTranslation(-((Math.abs(Sprite.fx.FX_lastTransformScrolling.get(hashCode()).getTranslateX()) + translateH) % renderableImpl.bounds.width), -((Math.abs(Sprite.fx.FX_lastTransformScrolling.get(hashCode()).getTranslateY()) + translateV) % renderableImpl.bounds.height));
                        fx_tx.preConcatenate(Sprite.fx.FX_lastTransformScrolling.get(hashCode()));
                        double mX = 1, mY = 1;
                        if ((fx & gfx.FX_REFLEXION.bit()) != 0) {
                            if ((fx & (gfx.FX_RIGHT.bit() | gfx.FX_LEFT.bit())) != 0) {
                                mX = -1;
                            }
                            if ((fx & (gfx.FX_TOP.bit() | gfx.FX_BOTTOM.bit())) != 0) {
                                mY = -1;
                            }
                        }
                        fx_tx_mirror.preConcatenate(AffineTransform.getTranslateInstance(mX * fx_tx.getTranslateX(), mY * fx_tx.getTranslateY()));
                        Sprite.fx.FX_lastTransformTime.put(hashCode(), now);
                    }
                    /**
                     * END SCROLLLING
                     */
                    /**
                     * START ROTQTION
                     */
                    if ((fx & gfx.FX_ROTATION.bit()) != 0) {
                        if (!(Sprite.fx.FX_lastTransformTime.get(hashCode()) instanceof Long)) {
                            Sprite.fx.FX_lastTransformTime.put(hashCode(), now);
                        }
                        float lastTransformTheta = Sprite.fx.FX_lastTransformTheta.containsKey(hashCode()) ? Sprite.fx.FX_lastTransformTheta.get(hashCode()) : (1f - 1f / 360f) * 2f * (float) Math.PI;
                        float rotationSpeed = Sprite.fx.FX_overallRADIALSPEED * 360f / (2f * (float) Math.PI);
                        if ((fx - (fx & (GLFX.bits._getAllBits() | GLFX.bits._getAllBitRanges()))) != 0) {
                            rotationSpeed = fx - (fx & (GLFX.bits._getAllBits() | GLFX.bits._getAllBitRanges()));
                            rotationSpeed /= 1000f;
                        }
                        lastTransformTheta -= (float) (now - Sprite.fx.FX_lastTransformTime.get(hashCode())) * rotationSpeed;
                        lastTransformTheta = lastTransformTheta % (2f * (float) Math.PI);
                        fx_tx.preConcatenate(AffineTransform.getRotateInstance(lastTransformTheta, (float) renderableImpl.bounds.width / 2f, (float) renderableImpl.bounds.height / 2f));
                        fx_tx_mirror.preConcatenate(AffineTransform.getRotateInstance(-lastTransformTheta, (float) renderableImpl.bounds.width / 2f, (float) renderableImpl.bounds.height / 2f));
                        Sprite.fx.FX_lastTransformTheta.put(hashCode(), lastTransformTheta);
                        Sprite.fx.FX_lastTransformTime.put(hashCode(), now);
                    }
                    /**
                     * END ROTQTION
                     */
                    /**
                     * START RELEXION + SHADOW
                     */
                    if ((fx & (gfx.FX_REFLEXION.bit() | gfx.FX_SHADOW.bit())) != 0) {
                        Sprite shadow = clone;
                        if (shadow == null) {
                            throw new JXAException("clone failed to recover data");
                        }
                        shadow.setCompositeEnabled(true);
                        shadow.setTX(fx_tx);
                        if ((fx & gfx.FX_REFLEXION.bit()) != 0 && (fx & (gfx.FX_TOP.bit() | gfx.FX_BOTTOM.bit() | gfx.FX_LEFT.bit() | gfx.FX_RIGHT.bit())) != 0) {
                            shadow.setTX(fx_tx_mirror);
                            if ((fx & (gfx.FX_TOP.bit() | gfx.FX_BOTTOM.bit())) != 0) {
                                shadow.setFlipEnabled(true, Sprite.VERTICAL);
                            }
                            if ((fx & (gfx.FX_LEFT.bit() | gfx.FX_RIGHT.bit())) != 0) {
                                shadow.setFlipEnabled(true, Sprite.HORIZONTAL);
                            }
                            Point endPoint = new Point(0, 0);
                            Point startPoint = new Point(0, 0);
                            if ((fx & gfx.FX_TOP.bit()) != 0) {
                                startPoint = new Point(0, (int) ((float) renderableImpl.bounds.height * .66f));
                                endPoint = new Point(0, 0);
                            }
                            if ((fx & gfx.FX_BOTTOM.bit()) != 0) {
                                startPoint = new Point(0, 0);
                                endPoint = new Point(0, (int) ((float) renderableImpl.bounds.height * .66f));
                            }
                            if ((fx & gfx.FX_LEFT.bit()) != 0) {
                                startPoint = new Point(0, 0);
                                endPoint = new Point((int) ((float) renderableImpl.bounds.width * .66f), 0);
                            }
                            if ((fx & gfx.FX_RIGHT.bit()) != 0) {
                                startPoint = new Point((int) ((float) renderableImpl.bounds.width * .66f), 0);
                                endPoint = new Point(0, 0);
                            }
                            if (!(FX_gradients.get(hashCode()) instanceof GradientPaint)) {
                                FX_gradients.put(hashCode(), new GradientPaint(startPoint.x, startPoint.y, new Color(1.0f, 1.0f, 1.0f, 1.0f), endPoint.x, endPoint.y, new Color(1.0f, 1.0f, 1.0f, 0.0f), false));
                            } else if (FX_gradients.get(hashCode()).getPoint2().distance(FX_gradients.get(hashCode()).getPoint1()) != startPoint.distance(endPoint)) {
                                FX_gradients.put(hashCode(), new GradientPaint(startPoint.x, startPoint.y, new Color(1.0f, 1.0f, 1.0f, 1.0f), endPoint.x, endPoint.y, new Color(1.0f, 1.0f, 1.0f, 0.0f), false));
                            }
                            Paint shadow_gradient = FX_gradients.get(hashCode());
                            shadow.setPaint(shadow_gradient);
                            shadow.setComposite(AlphaComposite.DstIn);
                            if (fx_color != null) {
                                shadow.runValidate();
                                shadow.setFlipEnabled(false, NONE);
                                shadow.setComposited(false);
                                shadow.setPaint(null);
                                shadow.setComposite(AlphaComposite.SrcAtop);
                                shadow.setColor(fx_color);
                            }
                        } else {
                            shadow.setColor(fx_color);
                            shadow.setComposite(AlphaComposite.SrcIn);
                        }
                        shadow.runValidate();
                        Shape clip = g2.getClip();
                        g2.setClip(backgroundClip);
                        if ((fx & gfx.FX_BACKGROUND.bit()) != 0 && (fx & gfx.FX_REPEAT.bit()) != 0) {
                            for (int j = -1; j < Math.ceil((float) backgroundClip.getBounds().getHeight() / (float) renderableImpl.bounds.height); j++) {
                                for (int i = -1; i < Math.ceil((float) backgroundClip.getBounds().getWidth() / (float) renderableImpl.bounds.width); i++) {
                                    int mh = (fx & gfx.FX_REFLEXION.bit()) != 0 && (fx & gfx.FX_BOTTOM.bit()) != 0 ? 2 : 1;
                                    mh = (fx & gfx.FX_REFLEXION.bit()) != 0 && (fx & gfx.FX_TOP.bit()) != 0 ? -1 : mh;
                                    int mw = (fx & gfx.FX_REFLEXION.bit()) != 0 && (fx & gfx.FX_RIGHT.bit()) != 0 ? 2 : 1;
                                    mw = (fx & gfx.FX_REFLEXION.bit()) != 0 && (fx & gfx.FX_LEFT.bit()) != 0 ? -1 : mw;
                                    shadow.setLocation((int) backgroundClip.getBounds().getX() + ((fx & gfx.FX_HORIZONTAL.bit()) != 0 ? i : 0) * mw * renderableImpl.bounds.width, (int) backgroundClip.getBounds().getY() + ((fx & gfx.FX_VERTICAL.bit()) != 0 ? j : 0) * mh * renderableImpl.bounds.height);
                                    shadow.setLocation(shadow.getLocation().x + fx_loc.x, shadow.getLocation().y + fx_loc.y);
                                    complete = shadow.draw(renderableImpl.io.obs, g2) && complete;
                                    if ((fx & gfx.FX_HORIZONTAL.bit()) == 0) {
                                        break;
                                    }
                                }
                                if ((fx & gfx.FX_VERTICAL.bit()) == 0) {
                                    break;
                                }
                            }
                        } else {
                            if ((fx & gfx.FX_REFLEXION.bit()) != 0) {
                                if ((fx & gfx.FX_BOTTOM.bit()) != 0) {
                                    shadow.setLocation((int) renderableImpl.bounds.getX(), (int) renderableImpl.bounds.getMaxY());
                                }
                                if ((fx & gfx.FX_TOP.bit()) != 0) {
                                    shadow.setLocation((int) renderableImpl.bounds.getX(), (int) (renderableImpl.bounds.getY() - renderableImpl.bounds.getHeight()));
                                }
                                if ((fx & gfx.FX_LEFT.bit()) != 0) {
                                    shadow.setLocation((int) (renderableImpl.bounds.getX() - renderableImpl.bounds.getWidth()), (int) renderableImpl.bounds.getY());
                                }
                                if ((fx & gfx.FX_RIGHT.bit()) != 0) {
                                    shadow.setLocation((int) renderableImpl.bounds.getMaxX(), (int) renderableImpl.bounds.getY());
                                }
                            }
                            shadow.setLocation(shadow.getLocation().x + fx_loc.x, shadow.getLocation().y + fx_loc.y);
                            shadow.draw(renderableImpl.io.obs, g2);
                        }
                        g2.setClip(clip);
                    }
                    /**
                     * END RELEXION + SHADOW
                     */
                    /**
                     * START BACKGROUND + REPEAT
                     */
                    if ((fx & gfx.FX_BACKGROUND.bit()) != 0 && (fx & gfx.FX_REPEAT.bit()) != 0) {
                        g2.setClip(backgroundClip);
                        for (int j = -1; j < Math.ceil((float) backgroundClip.getBounds().getHeight() / (float) renderableImpl.bounds.height); j++) {
                            for (int i = -1; i < Math.ceil((float) backgroundClip.getBounds().getWidth() / (float) renderableImpl.bounds.width); i++) {
                                setLocation((int) backgroundClip.getBounds().getX() + ((fx & gfx.FX_HORIZONTAL.bit()) != 0 ? i : 0) * renderableImpl.bounds.width, (int) backgroundClip.getBounds().getY() + ((fx & gfx.FX_VERTICAL.bit()) != 0 ? j : 0) * renderableImpl.bounds.height);
                                Point loc = getLocation();
                                /*
                                 * setLocation(loc.x + fx_loc.x, loc.y +
                                 * fx_loc.y);
                                 */
                                if ((fx & gfx.FX_SCROLLING.bit()) != 0 && (fx & gfx.FX_REPEAT.bit()) != 0) {
                                    setLocation(getLocation().x + renderableImpl.bounds.width, getLocation().y + renderableImpl.bounds.height);
                                    if ((fx & gfx.FX_HORIZONTAL.bit()) != 0 && (fx & gfx.FX_VERTICAL.bit()) != 0) {
                                        complete = __draw0(renderableImpl.io.obs, g2, baseTx, basePtx, fx_tx) && complete;
                                    }
                                    setLocation(getLocation().x - renderableImpl.bounds.width, getLocation().y);
                                    if ((fx & gfx.FX_VERTICAL.bit()) != 0) {
                                        complete = __draw0(renderableImpl.io.obs, g2, baseTx, basePtx, fx_tx) && complete;
                                    }
                                    setLocation(getLocation().x + renderableImpl.bounds.width, getLocation().y - renderableImpl.bounds.height);
                                    if ((fx & gfx.FX_HORIZONTAL.bit()) != 0) {
                                        complete = __draw0(renderableImpl.io.obs, g2, baseTx, basePtx, fx_tx) && complete;
                                    }
                                }
                                setLocation(loc);
                                complete = __draw0(renderableImpl.io.obs, g2, baseTx, basePtx, fx_tx) && complete;
                                setLocation(
                                        renderableImpl.bounds.getBounds().getLocation());
                                if ((fx & gfx.FX_HORIZONTAL.bit()) == 0) {
                                    break;
                                }
                            }
                            if ((fx & gfx.FX_VERTICAL.bit()) == 0) {
                                break;
                            }
                        }
                    } else {
                        /**
                         * END BACKGROUND + REPEAT
                         */
                        /**
                         * START SCROLLING + REPEAT
                         */
                        if ((fx & gfx.FX_SCROLLING.bit()) != 0 && (fx & gfx.FX_REPEAT.bit()) != 0) {
                            Point loc = getLocation();
                            setLocation(
                                    getLocation().x + renderableImpl.bounds.width, getLocation().y + renderableImpl.bounds.height);
                            if ((fx & gfx.FX_HORIZONTAL.bit()) != 0 && (fx & gfx.FX_VERTICAL.bit()) != 0) {
                                complete = __draw0(renderableImpl.io.obs, g2, baseTx, basePtx, fx_tx) && complete;
                            }
                            setLocation(getLocation().x - renderableImpl.bounds.width, getLocation().y);
                            if ((fx & gfx.FX_VERTICAL.bit()) != 0) {
                                complete = __draw0(renderableImpl.io.obs, g2, baseTx, basePtx, fx_tx) && complete;
                            }
                            setLocation(getLocation().x + renderableImpl.bounds.width, getLocation().y - renderableImpl.bounds.height);
                            if ((fx & gfx.FX_HORIZONTAL.bit()) != 0) {
                                complete = __draw0(renderableImpl.io.obs, g2, baseTx, basePtx, fx_tx) && complete;
                            }
                            setLocation(loc);
                        }
                        /**
                         * END SCROLLING + REPEAT
                         */
                        complete = __draw0(renderableImpl.io.obs, g2, baseTx, basePtx, fx_tx) && complete;
                    }
                } catch (Exception e) {
                    if (isDebugEnabled()) {
                        e.printStackTrace();
                    }
                    interrupt_ = e;
                } finally {
                    g2.setClip(backgroundClip);

                    if (fx != gfx.FX_NONE) {
                        if (clone instanceof Sprite) {
                            clone.clearResource();
                        }
                    }
                    if (isHardwareAccel()) {
                        clearResource(); /*
                         * free video memory
                         */

                    }
                    painting = false;
                    monitor1.notifyAll();
                }
            }
        }
        if (interrupt_ instanceof Throwable) {
            JXAException e = new JXAException("Sprite " + toString() + " caught an interruption.", interrupt_);
            throw e;
        }
        return complete;
    }

    /**
     *
     */
    private boolean __draw0(Component obs, Graphics2D g2, AffineTransform baseTX, PerspectiveTransform basePtx, AffineTransform fx_tx) throws InterruptedException, IOException, NoninvertibleTransformException, IllegalArgumentException {
        boolean complete = true;
        Rectangle drawBounds = getBounds();
        g2.translate(drawBounds.x, drawBounds.y);
        if (basePtx.isIdentity()) {
            if (!fx_tx.isIdentity()) {
                baseTX.preConcatenate(fx_tx);
            }
            if (!renderableImpl.isTileModeEnabled()) {
                if (data == null) {
                    throw new JXAException("No image data to render ! " + toString());
                }
                if (data instanceof RenderedImage) {
                    g2.drawRenderedImage((RenderedImage) data, baseTX);
                    complete = complete && true;
                } else {
                    complete = g2.drawImage(data, baseTX, obs) && complete;
                }
            } else {
                if (tileSrc == null) {
                    throw new JXAException("No suitable source for rendering ! " + toString());
                }
                ImageReader r = renderableImpl.io.__getInput(tileSrc, innerResource, renderableImpl.io.getReader(0, tileMime), isUseIIOCacheEnabled());
                Dimension numTiles = renderableImpl.io.getNumTilesXY(r);
                Dimension dim = new Dimension(r.getWidth(0), r.getHeight(0));
                Dimension tiles = SpriteIO._WRITE_TILES_DIMENSION;
                renderableImpl.io.notifyIIOReadListenersStarted(renderableImpl.io.rTileProgList, r, 0);
                Area clip = g2.getClip() != null ? new Area(g2.getClip()) : new Area(new Rectangle(drawBounds.getSize()));
                for (int x = 0; x < numTiles.width; x++) {
                    for (int y = 0; y < numTiles.height; y++) {
                        ImageReadParam irp = renderableImpl.io._getIRP(r, _type);
                        Rectangle sroi = new Rectangle(x * tiles.width, y * tiles.height, tiles.width, tiles.height).intersection(new Rectangle(dim));
                        if (sroi.isEmpty()) {
                            continue;
                        }
                        irp.setSourceRegion(sroi);
                        Area a = new Area(baseTX.createTransformedShape(sroi));
                        a.intersect(clip);
                        if (!a.isEmpty()) {
                            BufferedImage b = r.read(0, irp);
                            renderableImpl.io.trackImage(renderableImpl.io.trackerPty, b);
                            renderableImpl.io.waitFor(renderableImpl.io.trackerPty);
                            renderableImpl.io.untrackImage(renderableImpl.io.trackerPty, b);
                            AffineTransform tileTX = AffineTransform.getTranslateInstance(sroi.getX(), sroi.getY());
                            tileTX.preConcatenate(baseTX);
                            g2.drawRenderedImage(b, tileTX);
                            b.flush();
                            if (isDebugEnabled()) {
                                g2.draw(baseTX.createTransformedShape(sroi));
                            }
                        }
                        renderableImpl.io.notifyIIOReadListenersProgress(renderableImpl.io.rTileProgList, r, (float) (y * numTiles.width + x) / (float) (numTiles.width * numTiles.height));
                    }
                }
                ((ImageInputStream) r.getInput()).flush();
                ((ImageInputStream) r.getInput()).close();
                r.dispose();
                renderableImpl.io.notifyIIOReadListenersComplete(renderableImpl.io.rTileProgList, r);
            }
            if (isDebugEnabled()) {
                g2.draw(baseTX.createTransformedShape(new Rectangle(drawBounds.getSize())));
            }
        } else {
            if (!renderableImpl.isTileModeEnabled()) {
                if (data == null) {
                    throw new JXAException("No image data to render ! " + toString());
                }
                ParameterBlock pb = new ParameterBlock();
                pb.addSource(data);
                pb.add(new WarpPerspective(basePtx));
                pb.add(Interpolation.getInstance(Interpolation.INTERP_NEAREST));
                if (DebugMap._getInstance().isDebugLevelEnabled(gfx.DBUG)) {
                    DebugMap._getInstance().tickPrintln(System.out, "Sprite " + toString() + " warped to : " + basePtx, false);
                }
                RenderedOp warpOp = ImageN.create("warp", pb);
                RenderedImage warped = (RenderedImage) warpOp.createInstance();
                GraphicsJAI jai = GraphicsJAI.createGraphicsJAI(g2, obs);
                jai.drawRenderedImage(warped, new AffineTransform());
                complete = complete && true;
            } else {
                if (tileSrc == null) {
                    throw new JXAException("No suitable source for rendering ! " + toString());
                }
                ImageReader r = renderableImpl.io.__getInput(tileSrc, innerResource, renderableImpl.io.getReader(0, tileMime), isUseIIOCacheEnabled());
                Dimension numTiles = renderableImpl.io.getNumTilesXY(r);
                Dimension dim = new Dimension(r.getWidth(0), r.getHeight(0));
                Dimension tiles = SpriteIO._WRITE_TILES_DIMENSION;
                renderableImpl.io.notifyIIOReadListenersStarted(renderableImpl.io.rTileProgList, r, 0);
                Area clip = g2.getClip() != null ? new Area(g2.getClip()) : new Area(new Rectangle(drawBounds.getSize()));
                for (int x = 0; x < numTiles.width; x++) {
                    for (int y = 0; y < numTiles.height; y++) {
                        ImageReadParam irp = renderableImpl.io._getIRP(r, _type);
                        Rectangle sroi = new Rectangle(x * tiles.width, y * tiles.height, tiles.width, tiles.height).intersection(new Rectangle(dim));
                        if (sroi.isEmpty()) {
                            continue;
                        }
                        irp.setSourceRegion(sroi);
                        double[] perspectiveTransform = new double[]{sroi.x, sroi.y, sroi.width, sroi.y, sroi.width, sroi.height, sroi.x, sroi.height};
                        basePtx.inverseTransform(perspectiveTransform, 0, perspectiveTransform, 0, 4);
                        if (clip.intersects(perspectiveTransform[0], perspectiveTransform[1], perspectiveTransform[2] - perspectiveTransform[0], perspectiveTransform[3] - perspectiveTransform[1])) {
                            BufferedImage b = r.read(0, irp);
                            renderableImpl.io.trackImage(renderableImpl.io.trackerPty, b);
                            renderableImpl.io.waitFor(renderableImpl.io.trackerPty);
                            renderableImpl.io.untrackImage(renderableImpl.io.trackerPty, b);
                            ParameterBlock pb = new ParameterBlock();
                            pb.addSource(b);
                            PerspectiveTransform tilePtx = (PerspectiveTransform) basePtx.clone();
                            tilePtx.concatenate(AffineTransform.getTranslateInstance(sroi.getX(), sroi.getY()).createInverse());
                            pb.add(new WarpPerspective(tilePtx));
                            pb.add(Interpolation.getInstance(Interpolation.INTERP_NEAREST));
                            if (DebugMap._getInstance().isDebugLevelEnabled(gfx.DBUG)) {
                                DebugMap._getInstance().tickPrintln(System.out, "Sprite " + toString() + " warped to : " + basePtx, false);
                            }
                            RenderedOp warpOp = ImageN.create("warp", pb);
                            RenderedImage warped = (RenderedImage) warpOp.createInstance();
                            b.flush();
                            GraphicsJAI jai = GraphicsJAI.createGraphicsJAI(g2, obs);
                            jai.drawRenderedImage(warped, new AffineTransform());
                            jai.dispose();
                            if (isDebugEnabled()) {
                                g2.draw(new Rectangle((int) Math.round(perspectiveTransform[0]), (int) Math.round(perspectiveTransform[1]), (int) Math.round(perspectiveTransform[2] - perspectiveTransform[0]), (int) Math.round(perspectiveTransform[3] - perspectiveTransform[1])));
                            }
                        }
                        complete = complete && true;
                        renderableImpl.io.notifyIIOReadListenersProgress(renderableImpl.io.rTileProgList, r, (float) (y * numTiles.width + x) / (float) (numTiles.width * numTiles.height));
                    }
                }
                ((ImageInputStream) r.getInput()).flush();
                ((ImageInputStream) r.getInput()).close();
                r.dispose();
                renderableImpl.io.notifyIIOReadListenersComplete(renderableImpl.io.rTileProgList, r);
            }
            if (isDebugEnabled()) {
                try {
                    double[] perspectiveTransform = new double[]{0, 0, drawBounds.width, 0, drawBounds.width, drawBounds.height, 0, drawBounds.height};
                    basePtx.inverseTransform(perspectiveTransform, 0, perspectiveTransform, 0, 4);
                    Polygon perspectiveTransformPoly = new Polygon();
                    for (int i = 0; i < perspectiveTransform.length; i += 2) {
                        perspectiveTransformPoly.addPoint((int) perspectiveTransform[i], (int) perspectiveTransform[i + 1]);
                    }
                    g2.draw(perspectiveTransformPoly);
                } catch (NoninvertibleTransformException ex) {
                    if (isDebugEnabled()) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        g2.translate(-(int) drawBounds.x, -(int) drawBounds.y);
        if (DebugMap._getInstance().isDebugLevelEnabled(gfx.DBUG)) {
            DebugMap._getInstance().tickPrintln(System.out, ">+>+>+> Java2D draw " + (complete ? " successfully " : " failure ") + "at " + drawBounds + " <+<+<+<", true);
        }
        return complete;
    }

    /**
     *
     * @param sp
     * @param size
     * @return
     * @throws NoninvertibleTransformException
     */
    protected static Dimension _nextTransformSize(Sprite sp, Dimension size) throws NoninvertibleTransformException {
        AffineTransform baseTx = new AffineTransform();
        if (!sp.mirroring.equals(sp.currentPreMirroring)) {
            baseTx.preConcatenate(sp.mirroring);
        }
        if (!sp.scaling.equals(sp.currentPreScaling)) {
            baseTx.preConcatenate(sp.scaling);
        }
        if (!sp.transforming.equals(sp.currentPreTransforming)) {
            baseTx.preConcatenate(sp.transforming);
        }
        PerspectiveTransform basePtx = new PerspectiveTransform();
        if (!sp.transforming.equals(sp.currentPreTransforming)) {
            basePtx.concatenate(sp.transforming.createInverse());
        }
        if (!sp.mirroring.equals(sp.currentPreMirroring)) {
            basePtx.concatenate(sp.mirroring.createInverse());
        }
        if (!sp.scaling.equals(sp.currentPreScaling)) {
            basePtx.concatenate(sp.scaling.createInverse());
        }
        if (!sp.perspectiveTransforming.equals(sp.currentPrePerspectiveTransforming)) {
            basePtx.concatenate(sp.perspectiveTransforming);
        }
        Rectangle r = new Rectangle(size);
        if (sp.perspectiveTransforming.isIdentity()) {
            return baseTx.createTransformedShape(r).getBounds().getSize();
        } else {
            double[] perspectiveTransform = new double[]{0, 0, size.width, 0, size.width, size.height, 0, size.height};
            basePtx.inverseTransform(perspectiveTransform, 0, perspectiveTransform, 0, 4);
            Polygon perspectiveTransformPoly = new Polygon();
            for (int i = 0; i < perspectiveTransform.length; i += 2) {
                perspectiveTransformPoly.addPoint((int) Math.round(perspectiveTransform[i]), (int) Math.round(perspectiveTransform[i + 1]));
            }
            return perspectiveTransformPoly.getBounds().getSize();
        }
    }

    /**
     * CAUTION : DO NOT use this method externally, it's for private internal
     * callbacks ONLY. resizes image data and returns it to given size and adds
     * transforms previously set with
     * {@linkplain #setTX(AffineTransform)} {@linkplain #setZoomEnabled(boolean, double)},....
     * concatenation of the 4 matrixes are as follows : SCALING X MIRRORING X TX
     * x PTX x dataPixels of the image data. attributes register the new applied
     * transforms so that the next time it is called with the same args, it will
     * leave as is.
     *
     * @param data the Image to resize
     * @param size dimension to resize to, if the transforms affects the size,
     * the latter is updated {@linkplain Dimension#setSize(Dimension)}. Query
     * size.getSize() to quickly obtain the correct dim's after this method has
     * returned !
     * @param pobs observer to use
     * @param tx the AffineTransform matrix to use for applying it to the image
     * @param mirroring the AffineTransform mirroring matrix to use for applying
     * it to the image
     * @param ptx the PerspectiveTransform to apply
     * @param scaling the AffineTransform scaling matrix to use for applying it
     * to rhe image
     * @return new resized image data (still loading in the MediaTracker)
     */
    public Image resizeData(Image data, Dimension size, AffineTransform tx, AffineTransform mirroring, AffineTransform scaling, PerspectiveTransform ptx, Component pobs) throws NullPointerException {
        return (Image) _resizeData(data, size, tx, mirroring, scaling, ptx, pobs);
    }

    /**
     * CAUTION : DO NOT use this method externally, it's for private internal
     * callbacks ONLY. resizes image data and returns it to given size and adds
     * transforms previously set with
     * {@linkplain #setTX(AffineTransform)} {@linkplain #setZoomEnabled(boolean, double)},....
     * concatenation of the 4 matrixes are as follows : SCALING X MIRRORING X TX
     * x PTX x dataPixels of the image data. attributes register the new applied
     * transforms so that the next time it is called with the same args, it will
     * leave as is.
     *
     * @param data the reader source
     * @param size dimension to resize to, if the transforms affects the size,
     * the latter is updated {@linkplain Dimension#setSize(Dimension)}. Query
     * size.getSize() to quickly obtain the correct dim's after this method has
     * returned !
     * @param pobs observer to use
     * @param tx the AffineTransform matrix to use for applying it to the image
     * @param mirroring the AffineTransform mirroring matrix to use for applying
     * it to the image
     * @param ptx the PerspectiveTransform to apply
     * @param scaling the AffineTransform scaling matrix to use for applying it
     * to rhe image
     * @return new resized image data as a File if it succeeded or the
     * {@linkplain #tileSrc tile source object}
     */
    public Object resizeData(ImageReader data, Dimension size, AffineTransform tx, AffineTransform mirroring, AffineTransform scaling, PerspectiveTransform ptx, Component pobs) throws NullPointerException {
        return _resizeData(data, size, tx, mirroring, scaling, ptx, pobs);
    }

    private Object _resizeData(Object data, Dimension size, AffineTransform tx, AffineTransform mirroring, AffineTransform scaling, PerspectiveTransform ptx, Component pobs) throws NullPointerException {
        renderableImpl.io.obs = pobs;
        int currentPty = Thread.currentThread().getPriority();
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        AffineTransform baseTx = new AffineTransform();
        PerspectiveTransform basePtx = new PerspectiveTransform();
        if (size.width <= 0 || size.height <= 0) {
            if (isDebugEnabled()) {
                System.err.println(toString() + " image size is invalid (width or height <= 0), hence resizing aborted.");
            }
            return data instanceof ImageReader ? tileSrc : data;
        }
        Object ndata = null;
        try {
            waitFor(this.data);
            final Monitor monitor = renderableImpl.imageSynch;
            synchronized (monitor) {
                if (data == null) {
                    loadResource();
                    if (renderableImpl.isTileModeEnabled()) {
                        data = renderableImpl.io.__getInput(tileSrc, innerResource, renderableImpl.io.getReader(0, tileMime), isUseIIOCacheEnabled());
                    } else {
                        data = this.data;
                        waitFor(this.data);
                    }
                    if (data == null) {
                        throw new JXAException("Sprite " + toString() + " had no data");
                    }
                }
                if (tx == null) {
                    tx = new AffineTransform();
                }
                if (scaling == null) {
                    scaling = new AffineTransform();
                }
                if (mirroring == null) {
                    mirroring = new AffineTransform();
                }
                if (ptx == null) {
                    ptx = new PerspectiveTransform();
                }
                /*
                 * compute actual size with transforming (resize)
                 */
                Dimension dataSize = data instanceof ImageReader ? new Dimension(((ImageReader) data).getWidth(0), ((ImageReader) data).getHeight(0)) : new Dimension(((Image) data).getWidth(renderableImpl.io.obs), ((Image) data).getHeight(renderableImpl.io.obs));
                baseTx.scale((double) size.width / (double) dataSize.width, (double) size.height / (double) dataSize.height);
                if (!ptx.equals(currentPrePerspectiveTransforming) || !tx.equals(currentPreTransforming) || !mirroring.equals(currentPreMirroring) || !scaling.equals(currentPreScaling) || !dataSize.equals(size)) {
                    /*
                     * add transform (watch out to the order !), WarpPerspective
                     * maps DST onto the SRC unlike Affine maps SRC onto the
                     * DST.
                     */
                    if (!ptx.equals(currentPrePerspectiveTransforming)) {
                        /**
                         * SRC = TX' x MIRRORING' x SCALING' x PTX x resize' x
                         * DST
                         */
                        if (!tx.equals(currentPreTransforming)) {
                            basePtx.concatenate(tx.createInverse());
                            currentPreTransforming = tx;
                        }
                        if (!mirroring.equals(currentPreMirroring)) {
                            basePtx.concatenate(mirroring.createInverse());
                            currentPreMirroring = mirroring;
                        }
                        if (!scaling.equals(currentPreScaling)) {
                            basePtx.concatenate(scaling.createInverse());
                            currentPreScaling = scaling;
                        }
                        basePtx.concatenate(ptx);
                        currentPrePerspectiveTransforming = ptx;
                    } else {
                        /**
                         * DST = TX X MIRRORING x SCALING x resize x SRC
                         */
                        if (!scaling.equals(currentPreScaling)) {
                            baseTx.preConcatenate(scaling);
                            currentPreScaling = scaling;
                        }
                        if (!mirroring.equals(currentPreMirroring)) {
                            baseTx.preConcatenate(mirroring);
                            currentPreMirroring = mirroring;
                        }
                        if (!tx.equals(currentPreTransforming)) {
                            baseTx.preConcatenate(tx);
                            currentPreTransforming = tx;
                        }
                    }
                    Graphics dataG = null;
                    Dimension nsize = null;
                    Rectangle transform = null;
                    Polygon perspectiveTransformPoly = new Polygon();
                    double[] perspectiveTransform = new double[]{0, 0, size.width, 0, size.width, size.height, 0, size.height};
                    if (basePtx.isIdentity()) {
                        transform = baseTx.createTransformedShape(new Rectangle(dataSize)).getBounds();
                        nsize = new Dimension((int) Math.round(transform.getWidth()), (int) Math.round(transform.getHeight()));
                    } else {
                        basePtx.inverseTransform(perspectiveTransform, 0, perspectiveTransform, 0, 4);
                        for (int i = 0; i < perspectiveTransform.length; i += 2) {
                            perspectiveTransformPoly.addPoint((int) Math.round(perspectiveTransform[i]), (int) Math.round(perspectiveTransform[i + 1]));
                        }
                        transform = perspectiveTransformPoly.getBounds();
                        nsize = transform.getSize();
                    }
                    if (data instanceof ImageReader) {
                        ndata = FileHelper._createTempFile(_TILES_PREFIX, renderableImpl.io._getIIOTmpDir(), true);
                    } else {
                        ndata = (cache & BUFFERED) != 0 ? SpriteIO.createBufferedImage(nsize, _type) : (Image) createVolatileImage(nsize, _getTransparency(_type));
                    }
                    if (ndata == null || nsize.width < 1 || nsize.height < 1) {
                        throw new JXAException("Sprite " + toString() + " failed to create new data on resize");
                    }
                    if (basePtx.isIdentity()) {
                        if (data instanceof ImageReader) {
                            /**
                             * file output preparation
                             */
                            ImageWriter w = renderableImpl.io.getWriter(0, storeMime);
                            ImageWriteParam iwp = renderableImpl.io._getIWP(w, nsize, storeMime, _type, -1f);
                            ImageReader r = (ImageReader) data;
                            Dimension numTiles = renderableImpl.io.getNumTilesXY(r);
                            Dimension tiles = SpriteIO._WRITE_TILES_DIMENSION;

                            boolean doTiledReplace = (_TILEFLAGS & renderableImpl.io.prepareWrite(nsize, w, SpriteIO._getITS(_type), iwp, isUseIIOCacheEnabled(), ndata, r, _type)) == _TILEFLAGS;
                            if (doTiledReplace) {
                                /*
                                 * NEVER ! w.prepareReplacePixels(0, new
                                 * Rectangle(nsize));
                                 */
                                List<Area> tilesShapes = Collections.synchronizedList(new ArrayList<Area>(numTiles.width * numTiles.height));
                                for (int y = 0; y < numTiles.height; y++) {
                                    for (int x = 0; x < numTiles.width; x++) {
                                        Shape dstROI = baseTx.createTransformedShape(new Rectangle(x * tiles.width, y * tiles.height, tiles.width, tiles.height).intersection(new Rectangle(dataSize)));
                                        tilesShapes.add(new Area(dstROI));
                                    }
                                }
                                for (int y = 0; y < numTiles.height; y++) {
                                    for (int x = 0; x < numTiles.width; x++) {
                                        /**
                                         * dst transformed bounds
                                         */
                                        Shape dstROI = tilesShapes.get(y * numTiles.width + x);
                                        Rectangle dstROIB = dstROI.getBounds();
                                        Rectangle srcROIB = new Rectangle(x * tiles.width, y * tiles.height, tiles.width, tiles.height);
                                        for (int yn = 0; yn < numTiles.height; yn++) {
                                            for (int xn = 0; xn < numTiles.width; xn++) {
                                                Area s = (Area) tilesShapes.get(yn * numTiles.width + xn).clone();
                                                if (s.intersects(dstROIB)) {
                                                    srcROIB = srcROIB.union(new Rectangle(xn * tiles.width, yn * tiles.height, tiles.width, tiles.height)).intersection(new Rectangle(dataSize));
                                                }
                                            }
                                        }
                                        /**
                                         * read src tile into buffer
                                         */
                                        Image srcBuffer = (cache & VOLATILE) != 0 ? createVolatileImage(srcROIB.getSize(), _getTransparency(_type)) : SpriteIO.createBufferedImage(srcROIB.getSize(), _type);
                                        /**
                                         * a
                                         * cropped copy of source image
                                         */
                                        do {
                                            dataG = _createImageGraphics(srcBuffer);
                                            Graphics2D g = wrapRendering(dataG);
                                            g.translate(-srcROIB.x, -srcROIB.y);
                                            /**
                                             * copy intersecting transformed
                                             * neighbors to buffer
                                             */
                                            for (int yn = 0; yn < numTiles.height; yn++) {
                                                for (int xn = 0; xn < numTiles.width; xn++) {
                                                    Area s = (Area) tilesShapes.get(yn * numTiles.width + xn).clone();
                                                    if (s.intersects(dstROIB)) {
                                                        ImageReadParam irp = renderableImpl.io._getIRP(r, _type);
                                                        Rectangle sroi = new Rectangle(xn * tiles.width, yn * tiles.height, tiles.width, tiles.height).intersection(new Rectangle(dataSize));
                                                        if (sroi.isEmpty()) {
                                                            continue;
                                                        }
                                                        irp.setSourceRegion(sroi);
                                                        BufferedImage b = r.read(0, irp);
                                                        renderableImpl.io.trackImage(renderableImpl.io.trackerPty, b);
                                                        renderableImpl.io.waitFor(renderableImpl.io.trackerPty);
                                                        renderableImpl.io.untrackImage(renderableImpl.io.trackerPty, b);
                                                        /*
                                                         * if
                                                         * (b.getType()
                                                         * != _type) { b
                                                         * =
                                                         * convertToBuffered((RenderedImage)b,
                                                         * pobs, true);
                                                         * }
                                                         */
                                                        g.translate(xn * tiles.width, yn * tiles.height);
                                                        g.drawRenderedImage(b, new AffineTransform());
                                                        g.translate(-xn * tiles.width, -yn * tiles.height);
                                                        b.flush();
                                                    }
                                                }
                                            }
                                            g.dispose();
                                        } while (!checkState(srcBuffer, null));
                                        /**
                                         * buffer the output
                                         */
                                        BufferedImage dstBuffer = SpriteIO.createBufferedImage(dstROIB.getSize(), _type);
                                        dataG = _createImageGraphics(dstBuffer);
                                        Graphics2D g = wrapRendering(dataG);
                                        g.translate(-dstROIB.x, -dstROIB.y);
                                        /**
                                         * copy transformed source to buffer
                                         */
                                        /*
                                         * g.setClip(new Area(dstROI));
                                         */
                                        AffineTransform tileTx = AffineTransform.getTranslateInstance(srcROIB.getX(), srcROIB.getY());
                                        tileTx.preConcatenate(baseTx);
                                        g.drawImage(srcBuffer, tileTx, renderableImpl.io.obs);
                                        g.dispose();
                                        srcBuffer.flush();
                                        /**
                                         * apply buffer to output
                                         */
                                        if (DebugMap._getInstance().isDebugLevelEnabled(gfx.DBUG)) {
                                            System.out.println(x + ", " + y + " Tiling replace " + toString() + " " + dstROIB + " from source " + srcROIB);
                                        }
                                        iwp.setDestinationOffset(dstROIB.getLocation());
                                        iwp.setSourceRegion(new Rectangle(dstROIB.getSize()));
                                        w.prepareReplacePixels(0, dstROIB);
                                        w.replacePixels(dstBuffer, iwp);
                                        w.endReplacePixels();
                                        dstBuffer.flush();
                                        renderableImpl.io.notifyIIOWriteListenersProgress(renderableImpl.io.wTileProgList, w, (float) (y * numTiles.width + x) / (float) (numTiles.width * numTiles.height));
                                    }
                                }
                            }
                            if (!doTiledReplace) {
                                renderableImpl.io.writeToOutput(nsize, w, iwp, r, _type, baseTx);
                            }
                            renderableImpl.io.endWrite(w);
                        } else {
                            dataG = _createImageGraphics((Image) ndata);
                            Graphics2D g = wrapRendering(dataG);
                            if (data instanceof RenderedImage) {
                                g.drawRenderedImage((RenderedImage) data, baseTx);
                            } else {
                                g.drawImage((Image) data, baseTx, renderableImpl.io.obs);
                            }
                            g.dispose();
                        }
                        if (DebugMap._getInstance().isDebugLevelEnabled(gfx.DBUG)) {
                            System.out.println("Sprite " + toString() + " transformed with " + baseTx);
                        }
                    } else {
                        if (data instanceof ImageReader) {
                            /**
                             * file output preparation
                             */
                            ImageWriter w = renderableImpl.io.getWriter(0, storeMime);
                            ImageWriteParam iwp = renderableImpl.io._getIWP(w, nsize, storeMime, _type, -1f);
                            ImageReader r = (ImageReader) data;
                            Dimension numTiles = renderableImpl.io.getNumTilesXY(r);
                            Dimension tiles = SpriteIO._WRITE_TILES_DIMENSION;
                            boolean doTiledReplace = (renderableImpl.io.prepareWrite(nsize, w, SpriteIO._getITS(_type), iwp, isUseIIOCacheEnabled(), ndata, r, _type) & _TILEFLAGS) == _TILEFLAGS;
                            if (doTiledReplace) {
                                /*
                                 * NEVER ! w.prepareReplacePixels(0, new
                                 * Rectangle(nsize));
                                 */
                                List<Area> tilesShapes = Collections.synchronizedList(new ArrayList<Area>(numTiles.width * numTiles.height));
                                /**
                                 * add scale to specified size for the current
                                 * data size to handle correct data transform
                                 * see UP ^^
                                 */
                                basePtx.concatenate(baseTx.createInverse());
                                for (int y = 0; y < numTiles.height; y++) {
                                    for (int x = 0; x < numTiles.width; x++) {
                                        perspectiveTransform = new double[]{
                                            (double) (x * tiles.width), (double) (y * tiles.height),
                                            Math.min(dataSize.width, (double) ((x + 1) * tiles.width)), (double) (y * tiles.height),
                                            Math.min(dataSize.width, (double) ((x + 1) * tiles.width)), Math.min(dataSize.height, (double) ((y + 1) * tiles.height)),
                                            (double) (x * tiles.width), Math.min(dataSize.height, (double) ((y + 1) * tiles.height))
                                        };
                                        basePtx.inverseTransform(perspectiveTransform, 0, perspectiveTransform, 0, 4);
                                        perspectiveTransformPoly = new Polygon();
                                        for (int i = 0; i < perspectiveTransform.length; i += 2) {
                                            perspectiveTransformPoly.addPoint((int) Math.round(perspectiveTransform[i]), (int) Math.round(perspectiveTransform[i + 1]));
                                        }
                                        tilesShapes.add(new Area(perspectiveTransformPoly));
                                    }
                                }
                                for (int y = 0; y < numTiles.height; y++) {
                                    for (int x = 0; x < numTiles.width; x++) {
                                        /**
                                         * dst transformed bounds
                                         */
                                        Shape dstROI = tilesShapes.get(y * numTiles.width + x);
                                        Rectangle dstROIB = dstROI.getBounds();
                                        Rectangle srcROIB = new Rectangle(x * tiles.width, y * tiles.height, tiles.width, tiles.height);
                                        for (int yn = 0; yn < numTiles.height; yn++) {
                                            for (int xn = 0; xn < numTiles.width; xn++) {
                                                Area s = (Area) tilesShapes.get(yn * numTiles.width + xn).clone();
                                                if (s.intersects(dstROIB)) {
                                                    srcROIB = srcROIB.union(new Rectangle(xn * tiles.width, yn * tiles.height, tiles.width, tiles.height)).intersection(new Rectangle(dataSize));
                                                }
                                            }
                                        }
                                        /**
                                         * a
                                         * cropped copy of source image
                                         */
                                        Image srcBuffer = (cache & VOLATILE) != 0 ? createVolatileImage(srcROIB.getSize(), _getTransparency(_type)) : SpriteIO.createBufferedImage(srcROIB.getSize(), _type);
                                        do {
                                            dataG = _createImageGraphics(srcBuffer);
                                            Graphics2D g = wrapRendering(dataG);
                                            g.translate(-srcROIB.x, -srcROIB.y);
                                            /**
                                             * copy intersecting transformed
                                             * neighbors to buffer
                                             */
                                            for (int yn = 0; yn < numTiles.height; yn++) {
                                                for (int xn = 0; xn < numTiles.width; xn++) {
                                                    Area s = (Area) tilesShapes.get(yn * numTiles.width + xn).clone();
                                                    if (s.intersects(dstROIB)) {
                                                        ImageReadParam irp = renderableImpl.io._getIRP(r, _type);
                                                        Rectangle sroi = new Rectangle(xn * tiles.width, yn * tiles.height, tiles.width, tiles.height).intersection(new Rectangle(dataSize));
                                                        if (sroi.isEmpty()) {
                                                            continue;
                                                        }
                                                        irp.setSourceRegion(sroi);
                                                        BufferedImage b = r.read(0, irp);
                                                        renderableImpl.io.trackImage(renderableImpl.io.trackerPty, b);
                                                        renderableImpl.io.waitFor(renderableImpl.io.trackerPty);
                                                        renderableImpl.io.untrackImage(renderableImpl.io.trackerPty, b);
                                                        /*
                                                         * if
                                                         * (b.getType()
                                                         * != _type) { b
                                                         * =
                                                         * convertToBuffered((RenderedImage)b,
                                                         * pobs, true);
                                                         * }
                                                         */
                                                        g.translate(xn * tiles.width, yn * tiles.height);
                                                        g.drawRenderedImage(b, new AffineTransform());
                                                        g.translate(-xn * tiles.width, -yn * tiles.height);
                                                        b.flush();
                                                    }
                                                }
                                            }
                                            g.dispose();
                                        } while (!checkState(srcBuffer, null));
                                        /**
                                         * buffer the output
                                         */
                                        BufferedImage dstBuffer = SpriteIO.createBufferedImage(dstROIB.getSize(), _type);
                                        dataG = _createImageGraphics(dstBuffer);
                                        Graphics2D g = wrapRendering(dataG);
                                        ParameterBlock pb = new ParameterBlock();
                                        pb.addSource(srcBuffer);
                                        PerspectiveTransform tilePtx = (PerspectiveTransform) basePtx.clone();
                                        tilePtx.concatenate(AffineTransform.getTranslateInstance(srcROIB.getX(), srcROIB.getY()).createInverse());
                                        pb.add(new WarpPerspective(tilePtx));
                                        pb.add(Interpolation.getInstance(Interpolation.INTERP_NEAREST));
                                        RenderedOp warpOp = ImageN.create("warp", pb);
                                        RenderedImage warped = (RenderedImage) warpOp.createInstance();
                                        srcBuffer.flush();
                                        GraphicsJAI jai = GraphicsJAI.createGraphicsJAI(g, renderableImpl.io.obs);
                                        /**
                                         * copy transformed source to buffer
                                         */
                                        jai.translate(-dstROIB.x, -dstROIB.y);
                                        /*
                                         * jai.setClip(new
                                         * Area(dstROI));
                                         */
                                        jai.drawRenderedImage(warped, new AffineTransform());
                                        jai.dispose();
                                        g.dispose();
                                        /**
                                         * apply buffer to output
                                         */
                                        iwp.setDestinationOffset(dstROIB.getLocation());
                                        iwp.setSourceRegion(new Rectangle(dstROIB.getSize()));
                                        w.prepareReplacePixels(0, dstROIB);
                                        w.replacePixels(dstBuffer, iwp);
                                        w.endReplacePixels();
                                        dstBuffer.flush();
                                        renderableImpl.io.notifyIIOWriteListenersProgress(renderableImpl.io.wTileProgList, w, (float) (y * numTiles.width + x) / (float) (numTiles.width * numTiles.height));
                                    }
                                }
                            }
                            if (!doTiledReplace) {
                                basePtx.concatenate(baseTx.createInverse());
                                renderableImpl.io.writeToOutput(nsize, w, iwp, r, _type, basePtx);
                            }
                            renderableImpl.io.endWrite(w);
                        } else {
                            ndata = (cache & VOLATILE) != 0 ? createVolatileImage(nsize, _getTransparency(_type)) : SpriteIO.createBufferedImage(nsize, _type);
                            do {
                                dataG = _createImageGraphics((Image) ndata);
                                Graphics2D g = wrapRendering(dataG);
                                ParameterBlock pb = new ParameterBlock();
                                pb.addSource(data);
                                /**
                                 * add scale to specified size for the current
                                 * data size to handle correct data transform
                                 * see UP ^^
                                 */
                                basePtx.concatenate(baseTx.createInverse());
                                pb.add(new WarpPerspective(basePtx));
                                pb.add(Interpolation.getInstance(Interpolation.INTERP_NEAREST));
                                RenderedOp warpOp = ImageN.create("warp", pb);
                                RenderedImage warped = (RenderedImage) warpOp.createInstance();
                                GraphicsJAI jai = GraphicsJAI.createGraphicsJAI(g, renderableImpl.io.obs);
                                jai.drawRenderedImage(warped, new AffineTransform());
                                jai.dispose();
                            } while (!checkState((Image) ndata, null));
                        }
                        if (DebugMap._getInstance().isDebugLevelEnabled(gfx.DBUG)) {
                            System.out.println("Sprite " + toString() + " warped to : " + basePtx);
                        }
                    }
                    if (!renderableImpl.isTileModeEnabled()) {
                        dataG.dispose();
                        ((Image) data).flush();
                    }
                    if (DebugMap._getInstance().isDebugLevelEnabled(gfx.DBUG)) {
                        System.out.println(toString() + " - nsize=" + nsize + " size=" + size);
                    }
                } else {
                    /**
                     * no transform was required
                     */
                    if (data instanceof ImageReader) {
                        ndata = tileSrc;
                    } else {
                        ndata = data;
                    }
                }
                Thread.currentThread().setPriority(currentPty);
            }
        } catch (Exception e) {
            if (isDebugEnabled()) {
                e.printStackTrace();
            }
            if (data instanceof ImageReader) {
                ndata = tileSrc;
            } else {
                ndata = data;
            }
            Thread.currentThread().setPriority(currentPty);
        } finally {
            return ndata;
        }
    }

    /**
     * @return the current storage mode
     */
    @Override
    public int getStoreMode() {
        return renderableImpl.getStoreMode();
    }
    protected transient int rMode = 0;

    private void prepareWriteExternal(ObjectOutput out) throws IOException {
        rMode = renderableImpl.getMode();
        renderableImpl.setMode(renderableImpl.getStoreMode());
        if (!renderableImpl.isTextureModeEnabled()) {
            texData = null;
            if (_texData != null) {
                _texData.clear();
                ImagePool.getInstance().deleteObsoleteSource(_texData, this);
            }
            _texData = null;
        }
        __validate();
        storeCurrentAttributes();
        if (load) {
            try {
                final Monitor monitor = renderableImpl.validateMonitor;
                synchronized (monitor) {
                    while (validating) {
                        monitor.wait(1000);
                    }
                    final Monitor monitor1 = renderableImpl.imageSynch;
                    synchronized (monitor1) {
                        if (renderableImpl.isJava2DModeEnabled()) {
                            if (DebugMap._getInstance().isDebugLevelEnabled(gfx.DBUG)) {
                                System.out.println("storing " + toString() + " as JAVA2D " + (renderableImpl.isTileModeEnabled() ? " TILED" : "") + " data...");
                            }
                            boolean makeSrc = !(src instanceof File);
                            /**
                             * force source to temp directory
                             */
                            File dir = new File(FileHelper._TMPDIRECTORY.getPath() + File.separator + SpriteIO.image_dir);
                            if (!makeSrc) {
                                if (renderableImpl.isTileModeEnabled()) /**
                                 * create src if src differs from tilesrc or
                                 * tilesrc is not in the temp directory
                                 */
                                {
                                    makeSrc = !tileMime.equals(storeMime) || !((File) tileSrc).getCanonicalPath().startsWith(dir.getCanonicalPath());
                                } else {
                                    makeSrc = !mime.equals(storeMime) || !((File) src).getCanonicalPath().startsWith(dir.getCanonicalPath());
                                }
                            }
                            if (makeSrc) {
                                dir.mkdirs();
                                FileHelper._makeWritable(dir);
                                File newSrc = FileHelper._createTempFile("sp_" + hash, dir, true);
                                writeImageToOutput(newSrc, -1f);
                                ImagePool.getInstance().deleteObsoleteSource(tileSrc, this);
                                src = tileSrc = newSrc;
                                mime = tileMime = storeMime;
                            } else {
                                if (renderableImpl.isTileModeEnabled()) {
                                    src = tileSrc;
                                    mime = tileMime;
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                if (isDebugEnabled()) {
                    e.printStackTrace();
                }
                IOException ioe = new IOException(e.getMessage());
                ioe.initCause(e);
                throw ioe;
            }
        } else {
            storeCurrentAttributes();
        }
    }

    /**
     * NOTICE : {@link #getSource() src} may be moved to the a tile source if {@link #isTileModeEnabled()
     * } is enabled before to call this method.
     *
     * @param out
     * @throws IOException
     */
    private void endWriteExternal(ObjectOutput out) throws IOException {
        if (load) {
            try {
                final Monitor monitor = renderableImpl.validateMonitor;
                synchronized (monitor) {
                    while (validating) {
                        monitor.wait(1000);
                    }
                    final Monitor monitor1 = renderableImpl.imageSynch;
                    synchronized (monitor1) {
                        if (renderableImpl.isJava2DModeEnabled()) {
                            out.writeBoolean(true);
                            out.writeObject(src);
                            out.writeLong(((File) src).length());
                            RandomAccessFile raf = new RandomAccessFile((File) src, "r");
                            byte[] b = new byte[FileHelper._SMALLBUFFFER_SIZE];
                            int rBytes = 0;
                            while ((rBytes = raf.read(b)) != -1) {
                                out.write(b, 0, rBytes);
                            }
                            raf.close();
                        } else {
                            out.writeBoolean(false);
                            out.writeObject(renderableImpl.io.NO_SOURCE);
                        }
                    }
                }
            } catch (InterruptedException e) {
                if (isDebugEnabled()) {
                    e.printStackTrace();
                }
            }
        } else {
            if (src instanceof Serializable) {
                out.writeObject(src);
            } else {
                out.writeObject(renderableImpl.io.NO_SOURCE);
            }
        }
        renderableImpl.setMode(rMode);
    }

    /**
     * NOTICE : {@link #getSource() src} may moved to the a tile source if {@link #isTileModeEnabled()
     * } is enabled before to call this method.
     */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        int currentPty = Thread.currentThread().getPriority();
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        prepareWriteExternal(out);
        /*
         * out.defaultWriteObject();
         */
        out.writeInt(MAX_SIZE);
        if (_texData != null) {
            out.writeBoolean(true);
            out.writeObject(_texData);
        } else {
            out.writeBoolean(false);
        }
        out.writeInt(_type);
        if (attributes != null) {
            out.writeBoolean(true);
            out.writeObject(attributes);
        } else {
            out.writeBoolean(false);
        }
        if (base != null) {
            out.writeBoolean(true);
            out.writeUTF(base.toString());
        } else {
            out.writeBoolean(false);
        }
        out.writeInt(renderableImpl.bounds.x);
        out.writeInt(renderableImpl.bounds.y);
        out.writeInt(renderableImpl.bounds.width);
        out.writeInt(renderableImpl.bounds.height);
        out.writeInt(cache);
        out.writeBoolean(compositeEnabled);
        out.writeBoolean(diskCached);
        out.writeLong(hash);
        out.writeDouble(lastFPS);
        out.writeLong(lastFrameTime);
        out.writeBoolean(load);
        if (mime != null) {
            out.writeBoolean(true);
            out.writeUTF(mime);
        } else {
            out.writeBoolean(false);
        }
        out.writeInt(mirror);
        if (mirroring != null) {
            out.writeBoolean(true);
            out.writeObject(mirroring);
        } else {
            out.writeBoolean(false);
        }
        out.writeBoolean(opaque);
        if (perspectiveTransforming != null) {
            out.writeBoolean(true);
            out.writeObject(perspectiveTransforming);
        } else {
            out.writeBoolean(false);
        }
        out.writeInt(rMode);
        if (scaling != null) {
            out.writeBoolean(true);
            out.writeObject(scaling);
        } else {
            out.writeBoolean(false);
        }
        out.writeInt(renderableImpl.getStoreMode());
        out.writeFloat(texPty);
        out.writeInt(texTilesX);
        out.writeInt(texTilesY);
        out.writeInt(renderableImpl.io.trackerPty);
        if (transforming != null) {
            out.writeBoolean(true);
            out.writeObject(transforming);
        } else {
            out.writeBoolean(false);
        }
        out.writeDouble(zoom);
        endWriteExternal(out);
        out.flush();
        Thread.currentThread().setPriority(currentPty);
    }

    private void prepareReadExternal(ObjectInput in) throws ClassNotFoundException, IOException {
        /**
         * nothing
         */
    }

    private void endReadExternal(ObjectInput in) throws ClassNotFoundException, IOException {
        resetAttributes();
        if (load) {
            if (_texData instanceof SortedMap) {
                texData = Collections.synchronizedSortedMap(_texData);
            }
            long len = 0;
            boolean createTemp = true;
            final Monitor monitor = renderableImpl.imageSynch;
            synchronized (monitor) {
                File newSrc = null;
                boolean getdata = in.readBoolean();
                src = tileSrc = in.readObject();
                if (renderableImpl.isJava2DModeEnabled()) {
                    tileMime = mime = storeMime;
                } else if (renderableImpl.isTileModeEnabled()) {
                    tileMime = mime;
                }
                if (getdata) {
                    len = in.readLong();
                    if (src.equals(renderableImpl.io.NO_SOURCE)) {
                        createTemp = false;
                    } else if (src instanceof File) {
                        if (!((File) src).exists() || ((File) src).length() != len) {
                            newSrc = new File(FileHelper._TMPDIRECTORY.getPath() + File.separator + SpriteIO.image_dir + File.separator + ((File) src).getName());
                            if (newSrc.exists()) {
                                createTemp = false;
                            }
                            newSrc.deleteOnExit();
                            src = tileSrc = newSrc;
                        }
                        if (((File) src).exists() && ((File) src).length() == len) {
                            createTemp = false;
                        } else {
                            createTemp = true;
                        }
                    }
                    if (createTemp) {
                        RandomAccessFile raf = new RandomAccessFile((File) src, "rw");
                        raf.setLength(len);
                        byte[] b = new byte[FileHelper._SMALLBUFFFER_SIZE];
                        int rBytes = 0;
                        long rLen = 0;
                        while ((rBytes = in.read(b, 0, Math.min(b.length, (int) (len - rLen)))) != -1) {
                            raf.write(b, 0, rBytes);
                            rLen += rBytes;
                            if (rLen >= len) {
                                break;
                            }
                        }
                        if (rLen > len) {
                            throw new JXAException("Illegal rLen returned value " + rLen + ", expected len " + len);
                        }
                        raf.close();
                    } else {
                        in.skipBytes((int) len);
                    }
                } else {
                    createTemp = false;
                }
            }
        } else {
            src = tileSrc = in.readObject();
            tileMime = mime;
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int currentPty = Thread.currentThread().getPriority();
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        prepareReadExternal(in);
        /*
         * in.defaultReadObject();
         */
        MAX_SIZE = in.readInt();
        _texData = in.readBoolean() ? (SpritesCacheManager<Integer, Sf3Texture>) in.readObject() : _texData;
        _type = in.readInt();
        attributes = in.readBoolean() ? (HashMap<String, Serializable>) in.readObject() : attributes;
        try {
            base = in.readBoolean() ? new URI(in.readUTF()) : base;
            renderableImpl.bounds.setBounds(in.readInt(), in.readInt(), in.readInt(), in.readInt());
            cache = in.readInt();
            compositeEnabled = in.readBoolean();
            diskCached = in.readBoolean();
            hash = in.readLong();
            lastFPS = in.readDouble();
            lastFrameTime = in.readLong();
            load = in.readBoolean();
            mime = in.readBoolean() ? in.readUTF() : mime;
            mirror = in.readInt();
            mirroring = in.readBoolean() ? (AffineTransform) in.readObject() : mirroring;
            opaque = in.readBoolean();
            perspectiveTransforming = in.readBoolean() ? (PerspectiveTransform) in.readObject() : perspectiveTransforming;
            renderableImpl.setMode(in.readInt());
            scaling = in.readBoolean() ? (AffineTransform) in.readObject() : scaling;
            renderableImpl.setStoreMode(in.readInt());
            /**
             * stored data defines the rendermode (!!!)
             */
            renderableImpl.setMode(renderableImpl.getStoreMode());
            texPty = in.readFloat();
            texTilesX = in.readInt();
            texTilesY = in.readInt();
            renderableImpl.io.trackerPty = in.readInt();
            transforming = in.readBoolean() ? (AffineTransform) in.readObject() : transforming;
            zoom = in.readDouble();
            endReadExternal(in);
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        } catch (URISyntaxException ex) {
            throw new IOException(ex);
        } finally {
            Thread.currentThread().setPriority(currentPty);
        }
    }

    /**
     * removes an IIOController (not used)
     *
     * @param mime the controlled mime-time
     */
    public static void _removeIIOController(String mime) {
        SpriteIO.IIOControllers.remove(mime);
    }

    /**
     * are tiles available to {@linkplain #getTileReader()} /** DOES NOT WORK
     * WITH IMAGEPRODUCER AS {@linkplain #getSource() SOURCE}
     *
     * @return
     */
    public boolean isTilesAvailable() {
        if (tileSrc instanceof ImageProducer || renderableImpl.io.NO_SOURCE.equals(tileSrc)) {
            return false;
        }
        ImageReader r = renderableImpl.io.getReader(0, tileMime);
        try {
            r = renderableImpl.io.__getInput(tileSrc, innerResource, r, isUseIIOCacheEnabled());
            boolean b = r.getInput() != null;
            if (b) {
                ((ImageInputStream) r.getInput()).flush();
                ((ImageInputStream) r.getInput()).close();
                r.dispose();
            }
            return b;
        } catch (Exception ex) {
            if (isDebugEnabled()) {
                ex.printStackTrace();
            }
            return false;
        }
    }

    /**
     * are tiles available to load from a texture tile cache
     * {@linkplain #getTexData()}
     *
     * @return
     */
    public boolean isTexTilesAvailable() {
        final Monitor monitor1 = renderableImpl.imageSynch;
        synchronized (monitor1) {
            if (texData instanceof SortedMap) {
                return texData.keySet().size() > 1;
            } else {
                return false;
            }
        }
    }

    /**
     * prints all supported mime-types on the System std output
     *
     * @see ImageIO#getReaderMIMETypes()
     * @see ImageIO#getUseCache()
     */
    public static void _printAllMimeTypes() {
        ImageCollection._printAllMimeTypes();
    }

    /**
     * @param bufferedImageType
     * @return {@link VolatileImage#OPAQUE}, {@link VolatileImage#BITMASK} or
     * {@link VolatileImage#TRANSLUCENT}
     */
    public static int _getTransparency(int bufferedImageType) {
        return SpriteIO._getITS(bufferedImageType).getColorModel().getTransparency();
    }

    /**
     * attempt to retrieve thumbnails, or create one with the reader data
     *
     * @return a List of BufferedImage's (it's empty if none could be found or
     * made)
     */
    public List<BufferedImage> readThumbnails() {
        List<BufferedImage> v = Collections.synchronizedList(new ArrayList<BufferedImage>());
        ImageReader r = renderableImpl.io.getReader(0, tileMime);
        try {
            r = renderableImpl.io.__getInput(tileSrc, innerResource, r, isUseIIOCacheEnabled());
            if (r.getInput() == null) {
                int width = (int) Math.round(Math.max(SpriteIO._MIN_THUMBNAILS_SIZE, Math.min(SpriteIO._WRITE_TILES_DIMENSION.width, getSize().getWidth() / 2f)));
                v.add(convertToBuffered(getImage(renderableImpl.io.obs).getScaledInstance(width, (int) Math.round(getSize().getHeight() * width / getSize().getWidth()), Image.SCALE_FAST), renderableImpl.io.obs, true));
            } else {
                v = renderableImpl.io.getThumbnails(r, SpriteIO._MIN_THUMBNAILS_SIZE, _type);
            }
            ((ImageInputStream) r.getInput()).flush();
            ((ImageInputStream) r.getInput()).close();
            r.dispose();
        } catch (Exception ex) {
            if (isDebugEnabled()) {
                ex.printStackTrace();
            }
        } finally {
            return v;
        }
    }

    /**
     * @param s a source object : File, InputStream or filename as String
     * @return
     * @throws IOException
     */
    public static Sprite _load(Object s) throws IOException {
        return _load(s, true);
    }

    /**
     * @param useCache instance a FileCacheImageInputStream if possible
     * @param s a source object : File, InputStream or filename as String
     * @return
     * @throws IOException
     */
    public static Sprite _load(Object s, boolean useCache) throws IOException {
        Sprite sp = null;
        ImageInputStream in;
        Iterator<ImageReader> it;
        if (!(s instanceof ImageInputStream)) {
            in = ImageIO.createImageInputStream(s);
            it = ImageIO.getImageReaders(in);
            in.close();
        } else {
            throw new JXAException("Cannot use an ImageInputStream as source. A standard InputStream may be passed in.");
        }
        try {
            for (; it.hasNext();) {
                Map<String, Object> m = _loadImage(s, false, it.next(), Sf3RenderableImpl.MODE_TILE | Sf3RenderableImpl.MODE_JAVA2D, DEFAULT_TYPE, useCache);
                ImageReader r = (ImageReader) m.get("reader");
                if (s instanceof Image) {
                    sp = new Sprite((Image) s, r.getOriginatingProvider().getMIMETypes()[0], new Dimension(r.getWidth(0), r.getHeight(0)));
                }
                if (s instanceof File) {
                    sp = new Sprite((File) s, r.getOriginatingProvider().getMIMETypes()[0], new Dimension(r.getWidth(0), r.getHeight(0)), true);
                } else if (s instanceof InputStream) {
                    sp = new Sprite((InputStream) s, r.getOriginatingProvider().getMIMETypes()[0], new Dimension(r.getWidth(0), r.getHeight(0)), true);
                } else if (s instanceof String) {
                    sp = new Sprite((String) s, false, r.getOriginatingProvider().getMIMETypes()[0], new Dimension(r.getWidth(0), r.getHeight(0)), true);
                }
                r.dispose();
                break;
            }
            return sp;
        } catch (URISyntaxException ex) {
            JXAException e = new JXAException(ex);
            throw e;
        }
    }
    private transient String storeMime;
    private transient String tileMime;

    /**
     * this method will try the specified MIME type to encode with the current
     * {@link #_type buffered type}, if it fails, then an appropriate mime is
     * used. NOTICE : the specified mime type will be activated upon the next
     * write....() call (for best results, immediately serialize the Sprite w/o
     * running runValidate()).
     *
     * @param storeMime
     * @return the mime type specified IF AND ONLY IF it is appropriate to
     * write, otherwise a suitable one is returned and used instead.
     */
    public String setStoreMime(String storeMime) {
        if (renderableImpl.io.getWriter(0, storeMime).getOriginatingProvider().canEncodeImage(SpriteIO._getITS(_type))) {
            this.storeMime = storeMime;
        } else {
            for (String m : ImageIO.getWriterMIMETypes()) {
                if (renderableImpl.io.getWriter(0, m).getOriginatingProvider().canEncodeImage(SpriteIO._getITS(_type))) {
                    this.storeMime = m;
                    break;
                }
            }
            if (isDebugEnabled()) {
                System.out.println(JXAenvUtils.log("cannot encode in that " + storeMime + " format, trying with " + this.storeMime, JXAenvUtils.LVL.APP_WRN));
            }
        }
        return this.storeMime;
    }

    /**
     *
     * @return
     */
    public String getStoreMime() {
        return storeMime;
    }

    @Override
    public boolean isJava2DModeEnabled() {
        return renderableImpl.isJava2DModeEnabled();
    }

    @Override
    public boolean isTextureModeEnabled() {
        return renderableImpl.isTextureModeEnabled();
    }

    @Override
    public boolean isTileModeEnabled() {
        return renderableImpl.isTileModeEnabled();
    }

    @Override
    public void setJava2DModeEnabled(boolean b) {
        if ((renderableImpl.getMode() & Sf3RenderableImpl.MODE_JAVA2D) == 0 && b) {
            valid = false;
        }
        renderableImpl.setJava2DModeEnabled(b);
    }

    @Override
    public void setStoreMode(int mode) {
        renderableImpl.setStoreMode(mode);
    }

    @Override
    public void setTextureModeEnabled(boolean b) {
        if ((renderableImpl.getMode() & Sf3RenderableImpl.MODE_TEXTURE) == 0 && b) {
            valid = false;
        }
        renderableImpl.setTextureModeEnabled(b);
    }

    @Override
    public void setTileModeEnabled(boolean b) {
        if ((renderableImpl.getMode() & Sf3RenderableImpl.MODE_TILE) == 0 && b) {
            valid = false;
        }
        renderableImpl.setTileModeEnabled(b);
    }

    @Override
    public void setUseIIOCacheEnabled(boolean b) {
        renderableImpl.setUseIIOCacheEnabled(b);
    }

    @Override
    public boolean isUseIIOCacheEnabled() {
        return renderableImpl.isUseIIOCacheEnabled();
    }

    private transient Object newTileWrite = null;

    /**
     * !! WORKS ONLY WITH setStoreMime("image/tiff"), as of Image I/O Codecs
     * 1.1, other mime types than TIFF won't work !! this method prepares for a
     * tiled-mode writing on this sprite, a new tile File destination is created
     * in the temp. When finished, {@linkplain #endTileWrite(ImageWriter)} must
     * be called back to flush and close the stream and update the tile source
     * readers.
     *
     * @param useCache
     * @return an ImageWrite that can write tiles, replace contents, it should
     * already have thumbnails if available, etc.
     * @throws IOException
     * @throws IllegalArgumentException
     * @see #endTileWrite(javax.imageio.ImageWriter)
     */
    public ImageWriter prepareTileWrite(boolean useCache) throws IOException, InterruptedException {
        newTileWrite = FileHelper._createTempFile(_TILES_PREFIX, renderableImpl.io._getIIOTmpDir(), true);
        ImageWriter w = renderableImpl.io.getWriter(0, storeMime);
        ImageWriteParam iwp = getTileWriterParams(w);
        ImageTypeSpecifier its = getFeatures();

        ImageReader r = renderableImpl.io.getReader(0, tileMime);
        r = renderableImpl.io.__getInput(tileSrc, innerResource, r, useCache);
        Dimension dim = new Dimension(r.getWidth(0), r.getHeight(0));
        if ((_TILEFLAGS & renderableImpl.io.prepareWrite(dim, w, its, iwp, useCache, newTileWrite, r, _type)) != _TILEFLAGS) {
            throw new JXAException("Something went wrong to store " + storeMime + " from " + tileSrc);
        }
        return w;
    }

    /**
     * updates the {@link #tileSrc} and {@link #tileMime} with the
     * {@link #newTileWrite} and {@link #storeMime}
     *
     * @param w
     * @throws IOException
     */
    public void endTileWrite(ImageWriter w) throws IOException {
        renderableImpl.io.endWrite(w);
        ImagePool.getInstance().deleteObsoleteSource((File) tileSrc, this);
        tileSrc = newTileWrite;
        tileMime = storeMime;
    }

    /**
     * converts the specified image data to sp VolatileImage instance using the
     * specified Component that will receive the data CAUTION : avoid this
     * method if in {@linkplain #isTileModeEnabled() tile mode}, it may overflow
     * the memory if the image is too big.
     *
     * @param data the image data to specify as the source for the conversion
     * @param pobs the Component to receive the image data
     * @param flush will flush the source data, after conversion
     * @return a new VolatileImage data instance
     */
    public VolatileImage convertToVolatile(Image data, Component pobs, boolean flush) {
        return _convertToVolatile(data, pobs, flush);
    }

    /**
     * converts the specified image data to sp VolatileImage instance using the
     * specified Component that will receive the data CAUTION : avoid this
     * method if in {@linkplain #isTileModeEnabled() tile mode}, it may overflow
     * the memory if the image is too big.
     *
     * @param data the image data to specify as the source for the conversion
     * @param pobs the Component to receive the image data
     * @param flush will flush the source data, after conversion
     * @return a new VolatileImage data instance
     */
    public VolatileImage convertToVolatile(RenderedImage data, Component pobs, boolean flush) {
        return _convertToVolatile(data, pobs, flush);
    }

    private VolatileImage _convertToVolatile(Object data, Component pobs, boolean flush) {
        renderableImpl.io.obs = pobs;
        VolatileImage vdata = null;
        if (data == null) {
            if (isDebugEnabled()) {
                System.err.println("no data to convert " + toString());
            }
            return null;
        }
        try {
            final Monitor monitor1 = renderableImpl.imageSynch;
            synchronized (monitor1) {
                if (data instanceof Image) {
                    renderableImpl.io.trackImage(renderableImpl.io.trackerPty, (Image) data);
                    renderableImpl.io.waitFor(
                            renderableImpl.io.trackerPty);
                    renderableImpl.io.untrackImage(renderableImpl.io.trackerPty, (Image) data);
                }
                Dimension dim = new Dimension();
                if (data instanceof RenderedImage) {
                    dim = new Dimension(((RenderedImage) data).getWidth(), ((RenderedImage) data).getHeight());
                } else {
                    dim = new Dimension(((Image) data).getWidth(renderableImpl.io.obs), ((Image) data).getHeight(renderableImpl.io.obs));
                }
                vdata = createVolatileImage(dim, _getTransparency(_type));
                Graphics dataG = _createImageGraphics(vdata);
                do {
                    Graphics2D g = wrapRendering(dataG);
                    if (data instanceof RenderedImage) {
                        g.drawRenderedImage((RenderedImage) data, new AffineTransform());
                    } else {
                        g.drawImage((Image) data, new AffineTransform(), renderableImpl.io.obs);
                    }
                    dataG.dispose();
                } while (!checkState(vdata, null));
                if (data instanceof Image) {
                    if (flush) {
                        ((Image) data).flush();
                    }
                }
            }
        } catch (InterruptedException ex) {
            if (isDebugEnabled()) {
                ex.printStackTrace();
            }
        } finally {
            return vdata;
        }
    }

    /**
     * returns base URI to this sprite if it references sp file
     *
     * @see #src
     * @return link to this sprite or null if src isn't sp file reference
     */
    public URI getBase() {
        return base;
    }

    /**
     * repaints the component with the Sprite data. used by Swing EDT when
     * called
     *
     * @see JComponent#repaint()
     * @see JComponent#update(Graphics)
     * @param g1 Graphics instance
     */
    public void paintComponentImpl(Graphics g1, JComponent comp) {
        setJava2DModeEnabled(true);
        boolean myOpaque = isOpaque();
        setOpaque(comp.isOpaque());
        Component myObs = renderableImpl.io.obs;
        setObs(comp);
        Rectangle myBounds = renderableImpl.bounds.getBounds();
        setBounds(comp.getBounds());
        Graphics2D g = wrapRendering(g1);
        Shape clip = g.getClip();
        g.translate(-renderableImpl.bounds.x, -renderableImpl.bounds.y);
        g.clip(getBounds());
        if (isOpaque()) {
            Color c = getColor();
            g.setColor(g.getBackground());
            g.fill(getBounds());
            g.setColor(c);
        }
        runValidate();
        draw(renderableImpl.io.obs, g);
        g.translate(renderableImpl.bounds.x, renderableImpl.bounds.y);
        g.setClip(clip);
    }

    /**
     * creates the image Graphics
     *
     * @param image the specified to create Graphics onto
     * @return sp new valid Graphics instance for the specified Image
     */
    public static Graphics _createImageGraphics(Image image) {
        if (image == null) {
            return null;
        }
        Graphics dataG = image instanceof BufferedImage ? ((BufferedImage) image).createGraphics() : image instanceof VolatileImage ? ((VolatileImage) image).createGraphics() : image.getGraphics();
        return dataG;
    }

    /**
     * temporary files prefix
     */
    public final static String _TILES_PREFIX = "sptile_";

    private void clearImage(Image image) {
        Graphics2D jai = wrapRendering(_createImageGraphics(image));
        jai.setClip(0, 0, renderableImpl.bounds.width, renderableImpl.bounds.height);
        Color c = jai.getColor();
        jai.setColor(jai.getBackground());
        jai.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_IN, 0f));
        jai.fillRect(0, 0, renderableImpl.bounds.width, renderableImpl.bounds.height);
        jai.setColor(c);
        jai.setComposite(AlphaComposite.SrcOver);
        jai.dispose();
    }

    /**
     * erase all contents on the curent image data for this Sprite. Used for
     * image painting purposes.
     */
    public void clearImageGraphics() {
        clearImageGraphics(Color.WHITE);
    }

    public void clearImageGraphics(Color clearColor) {
        boolean doReplace = false;
        try {
            if (renderableImpl.isTileModeEnabled()) {
                if (doReplace = isTilesAvailable()) {
                    final Monitor monitor = renderableImpl.imageSynch;
                    synchronized (monitor) {
                        ImageReader r = renderableImpl.io.getReader(0, tileMime);
                        r = renderableImpl.io.__getInput(tileSrc, innerResource, r, isUseIIOCacheEnabled());
                        Dimension dim = new Dimension(r.getWidth(0), r.getHeight(0));
                        ((ImageInputStream) r.getInput()).flush();
                        ((ImageInputStream) r.getInput()).close();
                        r.dispose();
                        try {
                            ImageWriter w = prepareTileWrite(isUseIIOCacheEnabled());
                            ImageWriteParam iwp = getTileWriterParams(w);
                            BufferedImage dstBuffer = SpriteIO.createBufferedImage(dim, _type);
                            if (dstBuffer.getTransparency() != BufferedImage.TRANSLUCENT) {
                                Graphics2D g = Sprite.wrapRendering(dstBuffer.getGraphics());
                                g.setColor(clearColor);
                                g.fill(new Rectangle(dstBuffer.getWidth(), dstBuffer.getHeight()));
                                g.dispose();
                            }
                            /*
                             * if (doReplace = w.canReplacePixels(0)) {
                             */
                            Rectangle ROI = new Rectangle(dstBuffer.getWidth(), dstBuffer.getHeight());
                            w.prepareReplacePixels(0, ROI);
                            iwp.setDestinationOffset(ROI.getLocation());
                            iwp.setSourceRegion(new Rectangle(ROI.getSize()));
                            w.replacePixels(dstBuffer, iwp);
                            w.endReplacePixels();
                            dstBuffer.flush();
                            /*
                             * }
                             */
                            endTileWrite(w);
                            /*
                             * ((ImageOutputStream) w.getOutput()).flush();
                             * w.dispose(); ((ImageOutputStream)
                             * w.getOutput()).close();
                             */
                        } catch (Exception ex) {
                            if (isDebugEnabled()) {
                                ex.printStackTrace();
                            }
                            doReplace = false;
                        }
                    }
                }
            }
            if (!doReplace) {
                if (renderableImpl.isTileModeEnabled()) {
                    final Monitor monitor = renderableImpl.imageSynch;
                    synchronized (monitor) {
                        Image i = null;
                        if ((cache & VOLATILE) != 0) {
                            i = createVolatileImage(renderableImpl.bounds.getSize(), _getTransparency(_type));
                        } else {
                            BufferedImage dstBuffer = SpriteIO.createBufferedImage(renderableImpl.bounds.getSize(), _type);
                            if (dstBuffer.getTransparency() != BufferedImage.TRANSLUCENT) {
                                Graphics2D g = Sprite.wrapRendering(dstBuffer.getGraphics());
                                g.setColor(clearColor);
                                g.fill(new Rectangle(dstBuffer.getWidth(), dstBuffer.getHeight()));
                                g.dispose();
                            }
                            i = dstBuffer;
                        }
                        Sprite s = new Sprite(i, mime, renderableImpl.bounds.getSize());
                        File nTileSrc = FileHelper._createTempFile(_TILES_PREFIX, SpriteIO._getIIOTmpDir(), true);
                        s.setStoreMime(storeMime);
                        s.setBufferedType(_type);
                        s.runValidate();
                        s.writeImageToOutput(nTileSrc, -1f);
                        s.clearResource();
                        ImagePool.getInstance().deleteObsoleteSource(tileSrc, this);
                        tileSrc = nTileSrc;
                        tileMime = storeMime;
                    }
                } else {
                    final Monitor monitor = renderableImpl.imageSynch;
                    synchronized (monitor) {
                        if (data != null) {
                            clearImage(data);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            if (isDebugEnabled()) {
                ex.printStackTrace();
            }
        }
    }
    private boolean opaque = false;

    /**
     * returns the opaque value as VolatileImage is enabled or not
     *
     * @return
     */
    @Override
    public boolean isOpaque() {
        return opaque;
    }

    /**
     * sets up the opaque value as VolatileImage must be enabled or not
     *
     * @param b dis/enables the opaque property, that modifies the cache
     * property significantly
     * @see #cache
     */
    @Override
    public void setOpaque(boolean b) {
        if (opaque != b) {
            valid = false;
            opaque = b;
        }
    }

    /**
     * draws the current data on component
     *
     * @see #draw(Component, Graphics2D, AffineTransform, PerspectiveTransform)
     * @param obs the Component that is to be painted onto
     * @param g2 Graphics2D instance to draw to
     * @return true or false whether the painting has completed or not, resp.
     * (This returned value cannot be used for synchronization purposals))
     */
    @Override
    public boolean draw(Component obs, Graphics2D g2) {
        boolean b = false;
        try {
            b = __draw(obs, g2, null, null, 0, new Point(0, 0), null);
            markFPS();
        } catch (InterruptedException ex) {
            if (isDebugEnabled()) {
                ex.printStackTrace();
            }
        } finally {
            return b;
        }
    }
    /**
     * current backend rendering optimizations
     *
     * @default RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED
     * RenderingHints.KEY_ALPHA_INTERPOLATION,
     * RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED
     * RenderingHints.KEY_INTERPOLATION,
     * RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
     * RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF
     * RenderingHints.KEY_TEXT_ANTIALIASING,
     * RenderingHints.VALUE_TEXT_ANTIALIAS_OFF RenderingHints.KEY_DITHERING,
     * RenderingHints.VALUE_DITHER_DISABLE RenderingHints.KEY_COLOR_RENDERING,
     * RenderingHints.VALUE_COLOR_RENDER_SPEED
     */
    public final static RenderingHints renderingHints = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

    static {

        renderingHints.add(new RenderingHints(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED));
        renderingHints.add(new RenderingHints(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR));
        renderingHints.add(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF));
        renderingHints.add(new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF));
        renderingHints.add(new RenderingHints(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE));
        /*renderingHints.add(new RenderingHints(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED));*/
        renderingHints.add(new RenderingHints(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY));

    }

    /**
     * returns sp new RenderingHints with the HashMap instance that contains the
     * default rendering hints as {@link #renderingHints}
     *
     * @return the new hashMap instance that can be directly associated to sp
     * Graphics2D instance or modified globally for all rendering done with
     * {@link #wrapRendering(Graphics)}
     */
    public static RenderingHints _getRenderingHints() {
        return renderingHints;
    }
    /**
     * paint bounds switch
     */
    public static boolean _paintStatus = false;

    /**
     * draws with Transforms concatenated as follows : ptx X tx X dataPixels.
     *
     * @param pobs the Component that is to be painted onto
     * @param g Graphics2D instance to draw to
     * @param tx transform to be applied to drawing (unstable with some image
     * types)
     * @param ptx perspective Transform to be applied on drawing
     * @return true or false whether the painting has completed or not (this
     * returned value cannot be used for synchronization purposals)
     */
    @Override
    public boolean draw(Component pobs, Graphics2D g, AffineTransform tx, PerspectiveTransform ptx) {
        boolean b = false;
        try {
            b = __draw(pobs, g, tx, ptx, 0, new Point(0, 0), null);
            markFPS();
        } catch (InterruptedException ex) {
            if (isDebugEnabled()) {
                ex.printStackTrace();
            }
        } finally {
            return b;
        }
    }

    /**
     * draws with Transforms concatenated as follows : ptx X tx X dataPixels.
     *
     * @param pobs the Component that is to be painted onto
     * @param g the Graphics instance to paint onto
     * @param fx the gfx to use or 0 for none
     * @param fx_loc the location of the gfx or Point(0,0)
     * @param fx_color the color to use for the gfx or null
     * @return true or false whether the painting has completed or not (this
     * returned value cannot be used for synchronization purposals)
     */
    @Override
    public boolean draw(Component pobs, Graphics2D g, int fx, Point fx_loc, Color fx_color) {
        boolean b = false;
        try {
            b = __draw(pobs, g, null, null, fx, fx_loc, fx_color);
            markFPS();
        } catch (InterruptedException ex) {
            if (isDebugEnabled()) {
                ex.printStackTrace();
            }
        } finally {
            return b;
        }
    }

    /**
     * tells whether the Sprite has sp disk cached data available
     *
     * @return true or false
     * @deprecated use isStoreImageEnabled() instead
     */
    public boolean isDiskCached() {
        return diskCached;
    }

    /**
     * Tells whether buffer is enabled with sp BufferedImage instance for this
     * Sprite
     *
     * @see #cache
     * @return true if buffer is activated, false otherwise
     */
    public boolean isBufferedImageEnabled() {
        return (cache & BUFFERED) != 0;
    }

    /**
     * dis/enables using the BufferedImage for this Sprite instance; enabled by
     * default or disabled if sp VolatileImage was used to construct the sprite
     *
     * @param b dis/enables using the BufferedImage for this Sprite instance
     * @see #isBufferedImageEnabled()
     */
    public void setBufferedImageEnabled(boolean b) {
        if (b) {
            if ((this.cache & BUFFERED) != 0) {
                return;
            }
            this.cache = BUFFERED;
        } else {
            if ((this.cache & VOLATILE) != 0) {
                return;
            }
            this.cache = VOLATILE;
        }
        valid = false;
    }

    /**
     * EXPERIMENTAL dis/enables VRAM for this Sprite. A VolatileImage is used if
     * it is enabled.
     *
     * @param b dis/enables VRAM for this Sprite instance.
     * @see #setBufferedImageEnabled(boolean)
     * @deprecated not recommended to enable : 1� unstable ; 2� bufferedImage's
     * already use accelerated capabilities
     */
    @Override
    public void setHardwareAccel(boolean b) {
        setBufferedImageEnabled(!b);
    }

    /**
     * returns true or false, whether the VRAM (VolatileImage cache) is enabled
     * or not, resp.
     *
     * @return true or false, whether the VRAM is enabled or not, resp.
     * @see #setHardwareAccel(boolean)
     * @see #isBufferedImageEnabled()
     */
    @Override
    public boolean isHardwareAccel() {
        return !isBufferedImageEnabled();
    }

    /**
     * converts the Sprite to sp BufferedImage (Java2D mode must be enabled, if
     * not, use {@link #convertToBuffered(Image, Component, boolean)})
     *
     * @return buffered image
     */
    public BufferedImage toBuffered() {
        BufferedImage i = null;
        assert isJava2DModeEnabled() : "Java2D mode must be enabled";
        final Monitor monitor = renderableImpl.imageSynch;
        try {
            synchronized (monitor) {
                if (!isTileModeEnabled() && isJava2DModeEnabled()) {
                    i = convertToBuffered(data, renderableImpl.io.obs, false);
                } else {
                    ImageReader r = renderableImpl.io.getReader(0, tileMime);
                    r = renderableImpl.io.__getInput(tileSrc, innerResource, r, isUseIIOCacheEnabled());
                    ImageReadParam irp = renderableImpl.io._getIRP(r, _type);
                    if (r.getInput() == null) {
                        i = SpriteIO.createBufferedImage(renderableImpl.bounds.getSize(), _type);
                    } else {
                        try {
                            i = r.read(0, irp);
                            if (i.getType() != _type) {
                                i = convertToBuffered((RenderedImage) i, renderableImpl.io.obs, true);
                            }
                            renderableImpl.io.trackImage(
                                    renderableImpl.io.trackerPty, i);
                            renderableImpl.io.waitFor(renderableImpl.io.trackerPty);
                            renderableImpl.io.untrackImage(renderableImpl.io.trackerPty, i);
                        } catch (IOException ex) {
                            if (isDebugEnabled()) {
                                ex.printStackTrace();
                            }
                        }
                    }
                    ((ImageInputStream) r.getInput()).flush();
                    ((ImageInputStream) r.getInput()).close();
                    r.dispose();
                }
            }
        } catch (IOException ex) {
            if (isDebugEnabled()) {
                ex.printStackTrace();
            }
        } finally {
            return i;
        }
    }

    /**
     * converts the Sprite to VolatileImage (Java2D mode must be enabled, if
     * not, use {@link #convertToVolatile(Image, Component, boolean)})
     *
     * @return sp volatile image
     */
    public VolatileImage toVolatile() {
        assert isJava2DModeEnabled() : "Java2D mode must be enabled";
        final Monitor monitor = renderableImpl.imageSynch;
        synchronized (monitor) {
            if (!isTileModeEnabled() && isJava2DModeEnabled()) {
                return convertToVolatile(data, renderableImpl.io.obs, false);
            } else {
                return convertToVolatile((RenderedImage) toBuffered(), renderableImpl.io.obs, false);
            }
        }
    }

    /**
     * performs usual checks from an Image instance. A VolatileImage is checked
     * for its state. A BufferedImage may be loaded if read from an external
     * input (MediaTracker).
     *
     * @param i a VolatileImage to check for validating (it's automatically
     * restored if neccessary).
     * @param mt to check for the loading state of another Image, not useable
     * for VolatileImage.
     * @return true or false whether the Image is OK (and untouched) to be used
     * or not, if it has to be redrawn or loaded
     */
    public static boolean checkState(Image i, MediaTracker mt) {
        if (i == null) {
            return false;
        }
        if (i instanceof VolatileImage) {
            GraphicsConfiguration gc = JXAenvUtils._defaultGC;
            return ((VolatileImage) i).contentsLost() ? ((VolatileImage) i).validate(gc) == VolatileImage.IMAGE_OK : true;
        }
        if (mt != null) {
            mt.addImage(i, 99);
            boolean loaded = mt.checkID(99, false);
            mt.removeImage(i, 99);
            return loaded;
        }
        return true;
    }

    /**
     * Instances sp new VolatileImage from an image object.
     *
     * @param size dimensions
     * @param type {@link VolatileImage#OPAQUE}, {@link VolatileImage#BITMASK}
     * or {@link VolatileImage#TRANSLUCENT}
     * @return VolatileImage created
     * @see java.awt.image.VolatileImage
     * @see #_getTransparency(int)
     */
    public static VolatileImage createVolatileImage(Dimension size, int type) {
        VolatileImage vdata = null;
        GraphicsConfiguration gc = JXAenvUtils._defaultGC;
        Dimension d = new Dimension(Math.max(1, Math.abs(size.width)), Math.max(1, Math.abs(size.height)));
        do {
            Image i = ImagePool.getInstance().findVacantImage(VOLATILE, type, d);
            vdata = i == null ? gc.createCompatibleVolatileImage(d.width, d.height, type) : (VolatileImage) i;
            Graphics2D g = vdata.createGraphics();
            Color c = g.getColor();
            g.setColor(Color.WHITE);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_IN, 0f));
            g.fillRect(0, 0, d.width, d.height);
            g.setComposite(AlphaComposite.SrcOver);
            g.setColor(c);
            g.dispose();
        } while (!checkState(vdata, null));
        return vdata;
    }

    /**
     * Returns width of sprite.
     *
     * @return width in pixels
     * @see #getBounds()
     */
    @Override
    public int getWidth() {
        int w = renderableImpl.bounds.width;
        return w;
    }

    /**
     * Returns height of sprite.
     *
     * @return height in pixels
     * @see #getBounds()
     */
    @Override
    public int getHeight() {
        int h = renderableImpl.bounds.height;
        return h;
    }

    /**
     * Returns data field or the complete Image instance if tile mode is enabled
     * (!memory!). If <b>Java2D mode is enabled and tile mode disabled</b>, the
     * returned picture data is the same instance that is drawn next by the
     * draw() methods. It can be used to modify the data contents before
     * drawing. But, if you intend to get an independant Image instance, prefer
     * {@linkplain #toBuffered()} or {@linkplain #toVolatile()} which will
     * return a copy; thus any further call to {@linkplain #clearResource()}
     * from this Sprite instance won't flush contents of the image you're using.
     *
     * @param obs the Component where the image is or null
     * @return sprite Image data (can return null, if
     * {@link #setBounds(Rectangle)} was empty)
     */
    public Image getImage(Component obs) {
        if (DebugMap._getInstance().isDebugLevelEnabled(gfx.DBUG)) {
            DebugMap._getInstance().tickPrintln(System.out, src + ": fetching sprite data...", true);
        }
        Image i = null;
        try {
            final Monitor monitor = renderableImpl.imageSynch;
            synchronized (monitor) {
                if (!isTileModeEnabled() && isJava2DModeEnabled()) {
                    i = data;
                    waitFor(data);
                } else {
                    ImageReader r = renderableImpl.io.getReader(0, tileMime);
                    r = renderableImpl.io.__getInput(tileSrc, innerResource, r, isUseIIOCacheEnabled());
                    if (r.getInput() != null) {
                        i = r.read(0, renderableImpl.io._getIRP(r, _type));
                        if (cache == BUFFERED) {
                            if (((BufferedImage) i).getType() != _type) {
                                i = convertToBuffered(i, obs, true);
                            }
                        } else if (cache == VOLATILE) {
                            Image input = i;
                            do {
                                i = convertToVolatile(input, obs, true);
                            } while (!checkState(i, null));
                        }
                        renderableImpl.io.trackImage(renderableImpl.io.trackerPty, i);
                        renderableImpl.io.waitFor(renderableImpl.io.trackerPty);
                        renderableImpl.io.untrackImage(renderableImpl.io.trackerPty, i);
                    }
                    ((ImageInputStream) r.getInput()).flush();
                    ((ImageInputStream) r.getInput()).close();
                    r.dispose();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            return i;
        }
    }

    /**
     * returns true or false whether the Composite effect is enabled or not,
     * resp.
     *
     * @return true or false
     */
    @Override
    public boolean isCompositeEnabled() {
        return compositeEnabled;
    }

    /**
     * dis/enables the Composite effect
     *
     * @param b dis/enables the Composite effect for this Sprite
     */
    @Override
    public void setCompositeEnabled(boolean b) {
        if (compositeEnabled != b) {
            compositeEnabled = b;
            valid = false;
        }
    }

    /**
     * returns the Composite instance used by this Sprite
     *
     * @return sp Composite instance or null
     */
    @Override
    public Composite getComposite() {
        return cps;
    }

    /**
     * returns true or false whether the Composite effect has been applied or
     * not, resp.
     *
     * @return true or false
     */
    public boolean isComposited() {
        return composited;
    }

    /**
     * sets the "Composite effect has been applied" to off or on whether it has
     * to be applied again or not, resp.
     *
     * @param b false to apply the Composite effect one more time (it will be
     * set to true automatically when applied by the validation method)
     * @see #resizeData(Dimension, PerspectiveTransform, AffineTransform,
     * AffineTransform, AffineTransform, Component)
     */
    public void setComposited(boolean b) {
        composited = b;
    }

    /**
     * sets up the Composite instance to add effect to the Sprite.
     *
     * @param cps the Composite instance to set up
     */
    @Override
    public void setComposite(Composite cps) {
        if (this.cps instanceof Composite) {
            if (!this.cps.equals(cps)) {
                this.cps = cps;
                valid = false;
            }
        } else if (cps instanceof Composite) {
            this.cps = cps;
            valid = false;
        } else {
            this.cps = cps;
        }
    }

    /**
     * returns the Paint instance currently associated to this Sprite.
     *
     * @return sp Paint instance or null
     */
    @Override
    public Paint getPaint() {
        return pnt;
    }

    /**
     * sets up the Paint instance to use and add effect to the Sprite
     *
     * @param pnt sp Paint instance to set up
     */
    @Override
    public void setPaint(Paint pnt) {
        if (this.pnt instanceof Paint) {
            if (!this.pnt.equals(pnt)) {
                this.pnt = pnt;
                valid = false;
            }
        } else if (pnt instanceof Paint) {
            this.pnt = pnt;
            valid = false;
        } else {
            this.pnt = pnt;
        }
    }

    /**
     * returns the currently associated Color instance
     *
     * @return sp Color instance or null
     */
    @Override
    public Color getColor() {
        return clr;
    }

    /**
     * sets up the Color instance to add effect to this Sprite
     *
     * @param clr sp Color instance
     */
    @Override
    public void setColor(Color clr) {
        if (this.clr instanceof Color) {
            if (!this.clr.equals(clr)) {
                this.clr = clr;
                valid = false;
            }
        } else if (clr instanceof Color) {
            this.clr = clr;
            valid = false;
        } else {
            this.clr = clr;
        }
    }

    /**
     * Returns the image sprite scaled to the given factors.
     *
     * @param zoomX width scale factor
     * @param zoomY height scale factor
     * @param obs ImageObserver where further drawing will be done
     * @return new instance of Image
     */
    public Image getScaled(double zoomX, double zoomY, Component obs) {
        if (DebugMap._getInstance().isDebugLevelEnabled(GLFX.gfx.DBUG)) {
            DebugMap._getInstance().tickPrintln(System.out, src + ": scaling image X:" + zoomX + "x Y:" + zoomY + "x ...", true);
        }
        Dimension newSize = new Dimension((int) Math.ceil(renderableImpl.bounds.width * zoomX), (int) Math.ceil(renderableImpl.bounds.height * zoomY));
        Image sdata = null;
        try {
            if (spm instanceof SpritesCacheManager) {
                if (renderableImpl.isTileModeEnabled()) {
                    ImageReader r = renderableImpl.io.getReader(0, tileMime);
                    r = renderableImpl.io.__getInput(tileSrc, innerResource, r, isUseIIOCacheEnabled());
                    Object f = spm.memorySensitiveCallback("resizeData", this, new Object[]{r, newSize, transforming, mirroring, scaling, perspectiveTransforming, obs}, new Class[]{ImageReader.class, Dimension.class, AffineTransform.class, AffineTransform.class, AffineTransform.class, PerspectiveTransform.class, Component.class});
                    ((ImageInputStream) r.getInput()).flush();
                    ((ImageInputStream) r.getInput()).close();
                    ImageReadParam irp = renderableImpl.io._getIRP(r, _type);
                    sdata = renderableImpl.io.__getInput(f, innerResource, r, isUseIIOCacheEnabled()).read(0, irp);
                    ((ImageInputStream) r.getInput()).flush();
                    ((ImageInputStream) r.getInput()).close();
                    r.dispose();
                } else {
                    sdata = (Image) spm.memorySensitiveCallback("resizeData", this, new Object[]{data, newSize, transforming, mirroring, scaling, perspectiveTransforming, obs}, new Class[]{Image.class, Dimension.class, AffineTransform.class, AffineTransform.class, AffineTransform.class, PerspectiveTransform.class, Component.class});
                }
            } else {
                if (renderableImpl.isTileModeEnabled()) {
                    ImageReader r = renderableImpl.io.getReader(0, tileMime);
                    r = renderableImpl.io.__getInput(tileSrc, innerResource, r, isUseIIOCacheEnabled());
                    Object f = resizeData(r, newSize, transforming, mirroring, scaling, perspectiveTransforming, obs);
                    ((ImageInputStream) r.getInput()).flush();
                    ((ImageInputStream) r.getInput()).close();
                    ImageReadParam irp = renderableImpl.io._getIRP(r, _type);
                    sdata = renderableImpl.io.__getInput(f, innerResource, r, isUseIIOCacheEnabled()).read(0, irp);
                    ((ImageInputStream) r.getInput()).flush();
                    ((ImageInputStream) r.getInput()).close();
                    r.dispose();
                } else {
                    sdata = resizeData(data, newSize, transforming, mirroring, scaling, perspectiveTransforming, obs);
                }
            }
            renderableImpl.io.trackImage(hashCode(), sdata);
            renderableImpl.io.waitFor(hashCode());
            renderableImpl.io.untrackImage(hashCode(), sdata);
        } catch (Throwable ex) {
            if (isDebugEnabled()) {
                ex.printStackTrace();
            }
        } finally {
            return sdata;
        }
    }

    /**
     * Zooms the sprite to the given factor
     *
     * @param obs the Component that will receive this Sprite
     * @param zoom the scaling value
     * @see #getScaled(double, double, Component)
     */
    public void zoom(Component obs, double zoom) {
        final Monitor monitor = renderableImpl.imageSynch;
        synchronized (monitor) {
            data = getScaled(zoom, zoom, obs);
        }
        fitSizeToImage();
    }

    /**
     * returns the current zoom value
     *
     * @return zoom value, 1.0 is no zoom
     */
    public double getZoomValue() {
        return zoom;
    }

    /**
     * returns true or false whether sp zoom has been enabled or not, resp.
     *
     * @return true or false
     */
    public boolean isZoomEnabled() {
        return zoom != 1.0;
    }

    /**
     * dis/enables sp zoom transform
     *
     * @param b en/disabled
     * @param zoom zoom value to apply
     */
    public void setZoomEnabled(boolean b, double zoom) {
        scaling = new AffineTransform();
        scaling.scale((b) ? zoom : 1.0, (b) ? zoom : 1.0);
        if (this.zoom != zoom || (!b && 1.0 != this.zoom)) {
            this.zoom = (b) ? zoom : 1.0;
            valid = false;
        } else {
            this.zoom = (b) ? zoom : 1.0;
        }
    }

    /**
     * returns true or false whether sp mirror transform has been enabled or
     * not, resp.
     *
     * @return true or false
     */
    public boolean isFlipEnabled() {
        return mirror != NONE;
    }

    /**
     * activates flip Transform (mirroring) on the next
     * {@linkplain #runValidate()} call
     *
     * @param b en/disabled
     * @param mirror orientation of the mirror
     * @see #NONE
     * @see #VERTICAL
     * @see #HORIZONTAL
     */
    public void setFlipEnabled(boolean b, int mirror) {
        mirroring = new AffineTransform();
        switch ((b) ? mirror : NONE) {
            case HORIZONTAL:
                mirroring.translate((double) Math.abs(/*
                         * (float) scaling.getScaleX() *
                         */(float) renderableImpl.bounds.width), 0);
                mirroring.scale(-1.0, 1.0);
                break;
            case VERTICAL:
                mirroring.translate(0, (double) Math.abs(/*
                         * (float) scaling.getScaleY() *
                         */(float) renderableImpl.bounds.height));
                mirroring.scale(1.0, -1.0);
                break;
            default:
                break;
        }
        if (this.mirror != mirror || (!b && this.mirror != NONE)) {
            this.mirror = (b) ? mirror : NONE;
            valid = false;
        } else {
            this.mirror = (b) ? mirror : NONE;
        }
    }

    /**
     * Flips the current sprite This method synchronizes to sprite.
     *
     * @param obs the Component that will receive the image data
     * @param mirror the orientation of the symetric axis
     * @see #NONE
     * @see #VERTICAL
     * @see #HORIZONTAL
     * @see #getFlip(int, Component)
     */
    private void flip(Component obs, int mirror) throws InterruptedException {
        final Monitor monitor = renderableImpl.imageSynch;
        synchronized (monitor) {
            data = getFlip(mirror, obs);
        }
        if (DebugMap._getInstance().isDebugLevelEnabled(gfx.DBUG)) {
            DebugMap._getInstance().tickPrintln(System.out, src + " flipped", true);
        }
    }

    /**
     * Returns sp transformed image by the givsn symetrical-axis orientation
     *
     * @param orientation the mirror orientation to perform the flip-transform
     * @param pobs the observer
     * @see #NONE
     * @see #VERTICAL
     * @see #HORIZONTAL
     * @return the tranformed image instance
     */
    public Image getFlip(int orientation, Component pobs) {
        renderableImpl.io.obs = pobs;
        Image bdata = (cache & VOLATILE) != 0 ? (Image) createVolatileImage(renderableImpl.bounds.getSize(), _getTransparency(_type)) : SpriteIO.createBufferedImage(renderableImpl.bounds.getSize(), _type);
        AffineTransform tx;
        switch (orientation) {
            case HORIZONTAL:
                tx = AffineTransform.getTranslateInstance(renderableImpl.bounds.width, 0);
                tx.scale(-1.0, 1.0);
                break;
            case VERTICAL:
                tx = AffineTransform.getTranslateInstance(0, renderableImpl.bounds.height);
                tx.scale(1.0, -1.0);
                break;
            default:
                tx = AffineTransform.getTranslateInstance(0, 0);
                break;
        }
        do {
            Graphics dataG = _createImageGraphics(bdata);
            Graphics2D g = wrapRendering(dataG);
            draw(renderableImpl.io.obs, g, tx, null);
            g.dispose();
        } while (!checkState(bdata, null));
        return bdata;
    }

    /**
     * returns the current mirror orientation for this Sprite instance
     *
     * @return mirror orientation
     * @see #NONE
     * @see #VERTICAL
     * @see #HORIZONTAL
     */
    public int getMirrorValue() {
        return mirror;
    }

    /**
     * sets up the mirror orientation for this Sprite instance
     *
     * @param orientation
     */
    public void setMirror(int orientation) {
        if (mirror != orientation) {
            mirror = orientation;
            valid = false;
        } else {
            mirror = orientation;
        }
    }

    /**
     *
     * @param x
     * @param y
     * @param width
     * @param height
     */
    @Override
    public void setBounds(int x, int y, int width, int height) {
        if (renderableImpl.bounds instanceof Rectangle) {
            if (!renderableImpl.bounds.getSize().equals(new Dimension(width, height))) {
                valid = false;
            }
        }
        renderableImpl.setBounds(x, y, width, height);
    }

    /**
     *
     * @param r
     */
    @Override
    public void setBounds(Rectangle r) {
        if (renderableImpl.bounds instanceof Rectangle) {
            if (!renderableImpl.bounds.getSize().equals(r.getSize())) {
                valid = false;
            }
        }
        renderableImpl.setBounds(r);
    }

    /**
     * current bounds (those are valid AFTER a (run)validate() call). NOTICE :
     * if the zoom is enabled, these bounds may be altered in some way after the
     * validation and may be read again to get exact values. For Animation,
     * those bounds stay unaltered, and the curent Sprite may return the correct
     * bounds when the zoom is enabled.
     *
     * @param rv
     * @return
     * @see #setZoomEnabled(boolean, double)
     */
    @Override
    public Rectangle getBounds(Rectangle rv) {
        return renderableImpl.getBounds(rv);
    }

    /**
     * current bounds (those are valid AFTER a (run)validate() call). NOTICE :
     * if the zoom is enabled, these bounds may be altered in some way after the
     * validation and may be read again to get exact values. For Animation,
     * those bounds stay unaltered, and the
     * {@linkplain Animation#getCurrentSprite() current Sprite} can return the
     * correct bounds when the zoom is enabled.
     *
     * @return
     * @see #setZoomEnabled(boolean, double)
     */
    @Override
    public Rectangle getBounds() {
        return renderableImpl.getBounds();
    }

    /**
     *
     * @param width
     * @param height
     */
    @Override
    public void setSize(int width, int height) {
        if (renderableImpl.bounds instanceof Rectangle) {
            if (!renderableImpl.bounds.getSize().equals(new Dimension(width, height))) {
                valid = false;
            }
        }
        renderableImpl.setSize(width, height);
    }

    /**
     *
     * @param size
     */
    @Override
    public void setSize(Dimension size) {
        if (renderableImpl.bounds instanceof Rectangle) {
            if (!renderableImpl.bounds.getSize().equals(size)) {
                valid = false;
            }
        }
        renderableImpl.setSize(size);
    }

    /**
     *
     * @return
     */
    @Override
    public Dimension getSize() {
        return renderableImpl.getSize();
    }

    /**
     *
     * @return
     */
    @Override
    public Point getLocation() {
        return renderableImpl.getLocation();
    }

    /**
     *
     * @param p
     */
    @Override
    public void setLocation(Point p) {
        renderableImpl.setLocation(p);
    }

    /**
     *
     * @param x
     * @param y
     */
    @Override
    public void setLocation(int x, int y) {
        renderableImpl.setLocation(x, y);
    }

    /**
     * resets the transform matrix to apply to this Sprite
     *
     * @see #setTX(AffineTransform)
     */
    public void resetTX() {
        setTX(new AffineTransform());
    }

    /**
     * sets up the transform to apply to this Sprite
     *
     * @param tx the transform to apply
     */
    public void setTX(AffineTransform tx) {
        if (this.transforming instanceof AffineTransform) {
            if (!this.transforming.equals(tx)) {
                this.transforming = tx;
                valid = false;
            }
        } else if (tx instanceof AffineTransform) {
            this.transforming = tx;
            valid = false;
        } else {
            this.transforming = tx;
        }
    }

    /**
     * returns the current associated AffineTransform instance
     *
     * @return AffineTransform instance
     * @see #setTX(AffineTransform)
     */
    public AffineTransform getTX() {
        return transforming;
    }

    /**
     * resets the transform matrix to apply to this Sprite
     *
     * @see #setTX(AffineTransform)
     */
    public void resetPTX() {
        setPTX(new PerspectiveTransform());
    }

    /**
     * sets up the transform to apply to this Sprite
     *
     * @param ptx the PerspectiveTransform to apply
     */
    public void setPTX(PerspectiveTransform ptx) {
        if (this.perspectiveTransforming instanceof PerspectiveTransform) {
            if (!this.perspectiveTransforming.equals(ptx)) {
                this.perspectiveTransforming = ptx;
                valid = false;
            }
        } else if (ptx instanceof PerspectiveTransform) {
            this.perspectiveTransforming = ptx;
            valid = false;
        } else {
            this.perspectiveTransforming = ptx;
        }
    }

    /**
     * returns the current associated AffineTransform instance
     *
     * @return AffineTransform instance
     * @see #setTX(AffineTransform)
     */
    public PerspectiveTransform getPTX() {
        return perspectiveTransforming;
    }

    /**
     * unavailable THE FILE SOURCE OF TILES IS DELETED AFTER USE if not equal to
     * {@link #getSource()}.
     *
     * @deprecated
     */
    private final Object getTileSource() {
        return tileSrc;
    }

    /**
     * Returns source of image. usually it is a File instance. Source can change
     * to {@linkplain #tileSrc tile source} if it is serialized in
     * {@linkplain #isTileModeEnabled() tile mode}.
     * <br>To ensure this method returns the correct source that is requested in
     * tile-mode, the Sprite instance must have been
     * {@link #writeExternal(java.io.ObjectOutput) serialized/externalized} just
     * before this method is called.
     *
     * @see #writeExternal(java.io.ObjectOutput)
     * @return object source of this sprite (File, String or ImageInputStream)
     */
    public Object getSource() {
        return src;
    }

    /**
     * image status in the MediaTracker
     *
     * @return current status taken from current MediaTracker instance
     * @see MediaTracker#statusID(int, boolean)
     * @see MediaTracker#LOADING
     * @see MediaTracker#ABORTED
     * @see MediaTracker#ERRORED
     * @see MediaTracker#COMPLETE
     * @deprecated to access Image loading, use {@link #isResourceLoaded(), since the sprite never let living Image data in the
     * Mediatracker. It loads, waits, and it removes at once from the tracker array.
     */
    public int statusImage() {
        return renderableImpl.io.mt.statusID(renderableImpl.io.trackerPty, true);
    }

    /**
     * Clears out the image tracking, and removes the memory allocations.
     * <br>Obsolete temporary files and buffered textures are erased.
     *
     * @return null
     * @see MediaTracker#removeImage(Image)
     * @see #resetAttributes()
     * @see #_cleanup()
     * @see #clearTexData()
     */
    @Override
    public Object clearResource() {
        if (!load) {
            return null;
        }
        final Monitor monitor = renderableImpl.imageSynch;
        synchronized (monitor) {
            resetAttributes();
            valid = false;
            if (data != null) {
                renderableImpl.io.untrackImage(renderableImpl.io.trackerPty, data);
                data = null;
            }
            if (tileSrc != src) {
                ImagePool.getInstance().deleteObsoleteSource(tileSrc, this);
            }
            tileSrc = src;
            tileMime = mime;
            /*
             * metadata = null;
             */
            clearTexData();
        }
        ImagePool.getInstance().cleanup();
        if (DebugMap._getInstance().isDebugLevelEnabled(gfx.DBUG)) {
            DebugMap._getInstance().tickPrintln(System.out, "XXXXXX Cleared cached image " + src, true);
        }
        return null;
    }

    /**
     * clears used texture heap memory. This method is called each time
     * {@linkplain #clearResource()} is invoked.
     *
     * @see #loadTexData()
     */
    public void clearTexData() {
        final Monitor monitor = renderableImpl.imageSynch;
        synchronized (monitor) {
            valid = false;
            if (texData instanceof SortedMap) {
                texData.clear();
            }
        }
        ImagePool.getInstance().cleanup();
    }

    /**
     * clones this Sprite instance. the clone is first invalidated.
     *
     * @return the new cloned Sprite instance
     */
    @Override
    public Object clone() {
        if (spm instanceof SpritesCacheManager) {
            Object ret = null;
            try {
                ret = spm.memorySensitiveCallback("__clone", this, new Object[]{}, new Class[]{});
            } catch (Throwable t) {
                if (isDebugEnabled()) {
                    t.printStackTrace();
                }
            } finally {
                return ret;
            }
        } else {
            return __clone();
        }
    }

    /**
     * clones the Sprite instance (please use the original
     * {@linkplain #clone()}).<br>
     * Usage note : If any member variable added further in extensions, then
     * this method should be modified to reflect changes.<br>
     * This is implemented as "public" for the Reflection API
     *
     * @return the cloned Sprite
     */
    public Object __clone() {
        Sprite cloned = null;
        try {
            cloned = (Sprite) super.clone();
            cloned.base = (base instanceof URI) ? new URI(base.toString()) : null;
            cloned.attributes = cloneAttributes();
            cloned.transforming = (AffineTransform) transforming.clone();
            cloned.currentPreTransforming = (AffineTransform) currentPreTransforming.clone();
            cloned.perspectiveTransforming = (PerspectiveTransform) perspectiveTransforming.clone();
            cloned.currentPrePerspectiveTransforming = (PerspectiveTransform) currentPrePerspectiveTransforming.clone();
            cloned.mirroring = (AffineTransform) mirroring.clone();
            cloned.currentPreMirroring = (AffineTransform) currentPreMirroring.clone();
            cloned.scaling = (AffineTransform) scaling.clone();
            cloned.currentPreScaling = (AffineTransform) currentPreScaling.clone();
            cloned.renderableImpl = new Sf3RenderableImpl(Sprite.class, true);
            cloned.renderableImpl.bounds = (Rectangle) renderableImpl.getBounds().clone();
            if (!isTileModeEnabled()) {
                if (isJava2DModeEnabled()) {
                    cloned.data = (cache & BUFFERED) != 0 ? SpriteIO.createBufferedImage(cloned.renderableImpl.bounds.getSize(), cloned._type) : (Image) createVolatileImage(cloned.renderableImpl.bounds.getSize(), _getTransparency(cloned._type));
                    do {
                        Graphics dataG = _createImageGraphics(cloned.data);
                        Graphics2D g = wrapRendering(dataG);
                        Point loc = getLocation();
                        setLocation(0, 0);
                        __draw(cloned.renderableImpl.io.obs, g, null, null, gfx.FX_NONE, new Point(0, 0), null);
                        g.dispose();
                        setLocation(loc);
                    } while (!checkState(cloned.data, null));
                }
            } else {
                try {
                    /**
                     * create a new (tiled) file for the sprite clone.
                     */
                    File nTileSrc = FileHelper._createTempFile(_TILES_PREFIX, renderableImpl.io._getIIOTmpDir(), true);
                    writeImageToOutput(nTileSrc, -1f);
                    cloned.src = cloned.tileSrc = nTileSrc;
                    cloned.mime = cloned.tileMime = storeMime;
                } catch (IOException ex) {
                    if (isDebugEnabled()) {
                        ex.printStackTrace();
                    }
                    cloned.tileSrc = cloned.src;
                    cloned.tileMime = cloned.mime;
                }
            }
            cloned.texData = null;
            cloned.valid = false;
        } catch (Exception e) {
            if (isDebugEnabled()) {
                e.printStackTrace();
            }
            cloned = null;
        } finally {
            return cloned;
        }
    }

    /**
     * Called by the GC when the instance is about to be destructed. it is
     * overriden to immediately {@link #clearResource() clear the resources}
     * used by this Sprite.
     */
    @Override
    public void finalize() throws Throwable {
        super.finalize();
        clearResource();
        if (DebugMap._getInstance().isDebugLevelEnabled(gfx.DBUG)) {
            DebugMap._getInstance().tickPrintln(System.out, "GC's destroyed " + src, true);
        }
    }

    /**
     * checks whether the original image data is in a local file or a (remote)
     * class-path resource.
     * <br>after a successful deserialization operation (File I/O), the Sprite
     * data automatically becomes a local file source.
     *
     * @return true if it must be a class-path resource, false for a local file
     * source
     */
    private boolean isResource() {
        return innerResource;
    }

    /**
     * converts the source into sp String
     *
     * @return the source of this Sprite converted to sp String
     */
    @Override
    public String toString() {
        return mime + (isTileModeEnabled() ? " + tiled " + tileMime : "") + " > " + storeMime + " from " + (base == null ? (src == null ? "" + hash : src.toString()) : base.toString() + (isTileModeEnabled() ? " + " + tileSrc : ""));
    }
    /**
     * the valid switch
     */
    private transient boolean valid = false;

    /**
     * invalidates this Sprite instance
     */
    public void invalidate() {
        valid = false;
    }

    /**
     * returns true or false, whether it can be displayed or not, resp.
     *
     * @return
     * @see #validate()
     * @see #invalidate()
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * sets up the buffered type to use for the BufferedImage (VERY important if
     * you change the mime-type)
     *
     * @param type the buffered type to use for the BufferedImage
     */
    public void setBufferedType(int type) {
        if (this._type != type) {
            valid = false;
        }
        this._type = type;
    }

    /**
     * returns the current buffered type used for the BufferedImage
     *
     * @return the current buffered type used for the BufferedImage
     */
    public int getBufferedType() {
        return _type;
    }

    /**
     * @return ImageTypeSpecifier instance to better evaluate the Image data
     * features
     */
    public ImageTypeSpecifier getFeatures() {
        return SpriteIO._getITS(_type);
    }
    /**
     * the SpriteCacheManager instance that can make call-backs
     */
    private transient SpritesCacheManager spm;

    /**
     * sets up sp SpritesCacheManager instance where call-backs can be made to
     * save memory usage.
     *
     * @param spm the SpritesCacheManager instance to use for memory call-backs
     * @see SpritesCacheManager#memorySensitiveCallback(String, Object,
     * Object[], Class[])
     */
    public void setSPM(SpritesCacheManager spm) {
        this.spm = spm;
    }

    /**
     * validates this Sprite in the current Thread
     */
    private void __validate() {
        if (!load) {
            valid = true;
        }
        if (valid) {
            return;
        }
        try {
            final Monitor monitor = renderableImpl.paintMonitor;
            synchronized (monitor) {
                while (painting) {
                    monitor.wait(1000);
                }
                validating = true;
                if (DebugMap._getInstance().isDebugLevelEnabled(gfx.DBUG)) {
                    DebugMap._getInstance().tickPrintln(System.out, "Sprite " + toString() + " is being validated...", false);
                }
                setRenderingScene(rsc);
                if (!isResourceLoaded()) {
                    if (spm instanceof SpritesCacheManager) {
                        spm.memorySensitiveCallback("loadResource", Sprite.this, new Object[]{}, new Class[]{});
                    } else {
                        loadResource();
                    }
                }
                boolean texUpdate = false;
                waitFor(data);
                final Monitor monitor1 = renderableImpl.imageSynch;
                synchronized (monitor1) {
                    if (renderableImpl.isJava2DModeEnabled()) {
                        if (renderableImpl.isTileModeEnabled()) {
                            ImageReader r = renderableImpl.io.getReader(0, tileMime);
                            r = renderableImpl.io.__getInput(tileSrc, innerResource, r, isUseIIOCacheEnabled());
                            Object oldTileSrc = tileSrc;
                            if (spm instanceof SpritesCacheManager) {
                                tileSrc = spm.memorySensitiveCallback("resizeData", Sprite.this, new Object[]{r, renderableImpl.bounds.getSize(), transforming, mirroring, scaling, perspectiveTransforming, renderableImpl.io.obs}, new Class[]{ImageReader.class, Dimension.class, AffineTransform.class, AffineTransform.class, AffineTransform.class, PerspectiveTransform.class, Component.class});
                            } else {
                                tileSrc = resizeData(r, renderableImpl.bounds.getSize(), transforming, mirroring, scaling, perspectiveTransforming, renderableImpl.io.obs);
                            }
                            ((ImageInputStream) r.getInput()).flush();
                            ((ImageInputStream) r.getInput()).close();
                            r.dispose();
                            if (tileSrc instanceof File && !tileSrc.equals(oldTileSrc)) {
                                ImagePool.getInstance().deleteObsoleteSource(oldTileSrc, this);
                                texUpdate = true;
                                tileMime = storeMime;
                            }
                        } else {
                            Image s = data;
                            if (spm instanceof SpritesCacheManager) {
                                data = (Image) spm.memorySensitiveCallback("resizeData", Sprite.this, new Object[]{s, renderableImpl.bounds.getSize(), transforming, mirroring, scaling, perspectiveTransforming, renderableImpl.io.obs}, new Class[]{Image.class, Dimension.class, AffineTransform.class, AffineTransform.class, AffineTransform.class, PerspectiveTransform.class, Component.class});
                            } else {
                                data = resizeData(s, renderableImpl.bounds.getSize(), transforming, mirroring, scaling, perspectiveTransforming, renderableImpl.io.obs);
                            }
                            if (s.hashCode() != data.hashCode()) {
                                texUpdate = true;
                            }
                        }
                        fitSizeToImage();
                        if (!renderableImpl.isTileModeEnabled() && renderableImpl.isJava2DModeEnabled()) {
                            if ((cache & BUFFERED) != 0) {
                                boolean doBuf = true;
                                if (data instanceof BufferedImage) {
                                    if (((BufferedImage) data).getType() == _type) {
                                        doBuf = false;
                                    }
                                }
                                if (doBuf) {
                                    if (spm instanceof SpritesCacheManager) {
                                        data = (Image) spm.memorySensitiveCallback("convertToBuffered", Sprite.this, new Object[]{data, renderableImpl.io.obs, true}, new Class[]{Image.class, Component.class, boolean.class});
                                    } else {
                                        data = convertToBuffered(data, renderableImpl.io.obs, true);
                                    }
                                }
                                texUpdate = doBuf || texUpdate;
                            } else if ((cache & VOLATILE) != 0) {
                                boolean doVol = true;
                                if (data instanceof VolatileImage) {
                                    doVol = false;
                                }
                                if (doVol) {
                                    Image input = data;
                                    do {
                                        if (spm instanceof SpritesCacheManager) {
                                            data = (Image) spm.memorySensitiveCallback("convertToVolatile", Sprite.this, new Object[]{input, renderableImpl.io.obs, true}, new Class[]{Image.class, Component.class, boolean.class});
                                        } else {
                                            data = convertToVolatile(input, renderableImpl.io.obs, true);
                                        }
                                    } while (!checkState(data, null));
                                }
                                texUpdate = doVol || texUpdate;
                            }
                            if (compositeEnabled && !composited && data instanceof Image) {
                                texUpdate = true;
                                waitFor(data);
                                if (data == null) {
                                    throw new JXAException("Sprite " + toString() + " had no data to draw ");
                                }
                                Graphics g1 = _createImageGraphics(data);
                                g1.setClip(0, 0, data.getWidth(renderableImpl.io.obs), data.getHeight(renderableImpl.io.obs));
                                Graphics2D g = wrapRendering(g1);
                                Paint pnt = g.getPaint();
                                Composite cps = g.getComposite();
                                Color clr = g.getColor();
                                if (Sprite.this.pnt != null) {
                                    g.setPaint(Sprite.this.pnt);
                                }
                                if (Sprite.this.clr != null) {
                                    g.setColor(Sprite.this.clr);
                                }
                                if (Sprite.this.cps != null) {
                                    g.setComposite(Sprite.this.cps);
                                }
                                g.fillRect(g.getClipBounds().x, g.getClipBounds().y, g.getClipBounds().width, g.getClipBounds().height);
                                if (pnt != null) {
                                    g.setPaint(pnt);
                                }
                                if (cps != null) {
                                    g.setComposite(cps);
                                }
                                if (clr != null) {
                                    g.setColor(clr);
                                }
                                g.dispose();
                                composited = true;
                            }
                        }
                    }
                    if (renderableImpl.isTextureModeEnabled() && texUpdate) {
                        updateTexData();
                    }
                    valid = true;
                }
            }
        } catch (Throwable e) {
            if (isDebugEnabled()) {
                e.printStackTrace();
            }
        } finally {
            if (DebugMap._getInstance().isDebugLevelEnabled(gfx.DBUG)) {
                DebugMap._getInstance().tickPrintln(System.out, "...validation done.", true);
            }
            final Monitor monitor1 = renderableImpl.validateMonitor;
            synchronized (monitor1) {
                validating = false;
                monitor1.notifyAll();
            }
        }
    }

    private final static ThreadWorks spriteWorks = new ThreadWorks(Sprite.class.getCanonicalName());

    /**
     * validates the Sprite against the recent changes (mime type, buffered
     * type, cache, texture update, transform,...)
     * @return this instance.
     */
    @Override
    public Sprite runValidate() {
        try {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    __validate();
                }
            };
            if (isMultiThreadingEnabled() && !spriteWorks.isDT() && !Swing.isEventDispatchThread() && !Thread.holdsLock(renderableImpl.paintMonitor) && !Thread.holdsLock(renderableImpl.validateMonitor) && !Thread.holdsLock(renderableImpl.imageSynch)) {
                spriteWorks.doAndWait(r);
            } else {
                r.run();
            }
        } catch (InterruptedException ex) {
            if (isDebugEnabled()) {
                ex.printStackTrace();
            }
        } finally {
            return this;
        }
    }

    /**
     * component that is set as ImageObserver for this Sprite
     *
     * @return
     */
    public Component getObs() {
        return renderableImpl.io.obs;
    }

    /**
     * @param obs
     */
    public void setObs(Component obs) {
        renderableImpl.io.obs = obs;
    }

    /**
     * adds rendering hints and casts to Graphics2D.
     *
     * @param g the Graphics instance to wrap
     * @return the same Graphics2D-casted Graphics instance with rendering hints
     */
    public static Graphics2D wrapRendering(Graphics g) {
        if (g == null) {
            return null;
        }
        Graphics2D g2 = (Graphics2D) g;
        g2.setClip(g.getClip());
        g2.setRenderingHints(_getRenderingHints());
        return g2;
    }

    /**
     * returns true or false whether the inner-resource mode is enabled or not,
     * resp.
     *
     * @return true or false
     * @see #setInnerResourceModeEnabled(boolean)
     */
    public boolean isInnerResourceModeEnabled() {
        return isResource();
    }

    /**
     * return the last mesured frame-per-second (aka FPS). the framerate is
     * mesured each time the drawing process completes for this Sprite instance.
     *
     * @return FPS string, sp float-point number with 2 decimals
     * @see #markFPS()
     */
    public String getFPS() {
        Formatter f = new Formatter();
        f.format("%1$.2f", lastFPS);
        if (DebugMap._getInstance().isDebugLevelEnabled(gfx.DBUG)) {
            DebugMap._getInstance().tickPrintln(System.out, "fps :" + lastFPS, true);
        }
        if (f.toString().matches("\\d*.\\d*")) {
            return f.toString();
        } else {
            return "0";
        }
    }

    /**
     * returns the last mesured frame rate in milliseconds. It tells how much
     * time it took to render the last frame.
     *
     * @return sp long value of milliseconds indicating the last mesured frame
     * rate.
     */
    public long getFramerate() {
        return (long) Math.round(1000.0 / lastFPS);
    }

    /**
     * mark for FPS calculation
     *
     * @return calculated FPS
     */
    protected double markFPS() {
        double fps_n = 0;
        long now = System.nanoTime();
        if (lastFrameTime <= 0 || now - lastFrameTime > 1000000000L || now - lastFrameTime == 0) {
            lastFrameTime = now - 1000000000L;
        }
        /*
         * System.out.println(lastFPS + " " + lastFrameTime);
         */
        fps_n = (double) (1000000000f / (float) Math.abs(now - lastFrameTime));
        if (lastFPS <= 0) {
            lastFPS = fps_n;
        }
        fps_n = (double) (0.5f * (float) (lastFPS + fps_n));
        lastFPS = fps_n;
        lastFrameTime = now;
        return fps_n;
    }

    /**
     * returns true or false whether multi-threading has been statically
     * activated for the Sprite class or not, resp.
     *
     * @return true or false
     * @see #_multiThreading
     * @see #setMultiThreadingEnabled(boolean)
     */
    @Override
    public boolean isMultiThreadingEnabled() {
        return JXAenvUtils._multiThreading;
    }

    /**
     * the multi-threading switch public static boolean _multiThreading = false;
     */
    /**
     * sets up the static multi-threading switch to dis/enabled. Multi-Threading
     * is commonly known as the manner the JVM will treat mid/long-time
     * operations such as the validation process.
     *
     * @param b true or false
     */
    @Override
    public void setMultiThreadingEnabled(boolean b) {
        JXAenvUtils._multiThreading = b;
    }

    /**
     * returns true or false whether the tracked image data is loaded, that is
     * if the render mode (java2d-tile-texture combination) will be able to
     * display the corresponding contents.
     *
     * @return true or false
     * @see #setRenderMode(int)
     */
    @Override
    public boolean isResourceLoaded() {
        if (renderableImpl.isTileModeEnabled()) {
            return (renderableImpl.isJava2DModeEnabled() ? tileSrc != null /*
                     * && metadata != null
                     */ : true) && (renderableImpl.isTextureModeEnabled() ? texData != null : true);
        } else {
            return (renderableImpl.isJava2DModeEnabled() ? data instanceof Image && checkState(data, null) : true) && (renderableImpl.isTextureModeEnabled() ? texData != null : true);
        }
    }

    /**
     * @param sp a sprite or its hashcode
     * @return
     */
    public static boolean _GLIsLoaded(Object sp) {
        return RenderingSceneGL._GLgetLoadState(sp) == RenderingSceneGL._GLLOADSTATE_Loaded;
    }
    /**
     * last mesured FPS (frames per second)
     */
    protected double lastFPS = 0.0;
    /**
     * last drawn frametime in millisec
     */
    protected long lastFrameTime = 0;

    /**
     *
     */
    @Override
    public void setGroupMonitor(Monitor... tg) {
        renderableImpl.setGroupMonitor(tg);
    }

    /**
     *
     */
    @Override
    public Monitor[] getGroupMonitor() {
        return renderableImpl.getGroupMonitor();
    }

    /**
     *
     * @return
     */
    public boolean isDebugEnabled() {
        return renderableImpl.isDebugEnabled();
    }

    /**
     *
     * @param b
     */
    public void setDebugEnabled(boolean b) {
        renderableImpl.setDebugEnabled(b);
    }

    /**
     * Immediately paints any component,
     * {@linkplain JComponent#paintImmediately(Rectangle)} is called with
     * {@link Jcomponent#getVisibleRect()} This methdo is useful to repaint
     * while being in the EDT (e.g.
     * {@linkplain SwingUtilities#invokeLater(Runnable)})
     *
     * @param comp QUICKLY asap paints the JComponent with the Swing EDT
     */
    public static void _quickPaintImmediately(JComponent comp) {
        comp.paintImmediately(comp.getVisibleRect());
    }

    /**
     * Waits for current by the Sprite loaded data instance to load completely.
     *
     * @throws InterruptedException
     */
    public void waitForData() throws InterruptedException {
        waitFor(data);
    }

    /**
     * this method must be called between sp {@linkplain GLFX#_GLBeginFX()} call
     * and sp {@linkplain GLFX#_GLEndFX()} call.
     *
     * @param gld the RenderingSceneGL that is to be rendered onto
     * @param sp the Sprite to render
     * @param texBounds 2D Rectangle bounds to render at
     * @param z z-depth to render at
     * @param fx the gfx to apply, or 0 for none
     * @param fx_loc x,y Point location of the gfx, or Point(0,0)
     * @param fx_color Color of the gfx, or null
     * @param transform the transform bitwise-OR combination to apply or 0 for
     * none
     * {@linkplain RenderingSceneGL#_GL_TRANSFORM_ROTATE}, {@linkplain RenderingSceneGL#_GL_TRANSFORM_SCALE}, {@linkplain RenderingSceneGL#_GL_TRANSFORM_TRANSLATE}
     * @param scale
     * @param rotate
     * @param translate
     */
    public static void _GLRenderSprite(RenderingSceneGL gld, SpriteGLHandler sp, Rectangle texBounds, double z, int fx, Point fx_loc, Color fx_color, int transform, DoubleBuffer scale, DoubleBuffer rotate, DoubleBuffer translate) {
        _GLRenderSprite(gld, true, sp, texBounds, z, fx, fx_loc, fx_color, transform, scale, rotate, translate);
    }

    /**
     * renders the Sprite within an openGL context
     *
     * @param gld the RenderingSceneGL that is to be rendered onto
     * @param keepBinding keeps the associated texture for further use or not,
     * resp.
     * @param sp the Sprite to render
     * @param texBounds 2D Rectangle to render at
     * @param z z-depth to render at
     * @param fx the gfx to apply or 0 for none
     * @param fx_loc x,y Point location of the gfx, or Point(0,0)
     * @param fx_color Color for the gfx, or null
     * @param transform the transform bitwise-OR combination to apply or 0 for
     * none
     * {@linkplain RenderingSceneGL#_GL_TRANSFORM_ROTATE}, {@linkplain RenderingSceneGL#_GL_TRANSFORM_SCALE}, {@linkplain RenderingSceneGL#_GL_TRANSFORM_TRANSLATE}
     * @param scale
     * @param rotate
     * @param translate
     */
    public static void _GLRenderSprite(RenderingSceneGL gld, boolean keepBinding, final SpriteGLHandler sp, final Rectangle texBounds, double z, int fx, Point fx_loc, Color fx_color, int transform, DoubleBuffer scale, DoubleBuffer rotate, DoubleBuffer translate) {
        Sprite.fx._GLpushRender2DObject(gld, keepBinding, new GL2DObject() {
            @Override
            public Shape getShape() {
                return texBounds;
            }

            @Override
            public boolean isFill() {
                return true;
            }

            @Override
            public int getResolution() {
                return 1;
            }

            @Override
            public int getUID() {
                return sp.getSpHash();
            }
        }, z, fx, fx_loc, fx_color, transform, scale, rotate, translate);
    }
    private static SortedMap<Integer, Integer> spTexMap = /*
             * (SortedMap<Integer, Integer>) RenderingSceneGL._GLregMapOfLinks(
             */ Collections.synchronizedSortedMap(new TreeMap<Integer, Integer>())/*
             * )
             */;

    /**
     * returns the current texture hashCode for use with RenderingSceneGL
     * {@linkplain RenderingSceneGL#_GLIsTextureLoaded(int) HashMap's}.
     *
     * @param spHash
     * @return Sf3Texture hashcode or 0 if the Sf3Texture has not been loaded
     * once yet
     */
    public static int _GLgetTexHash(int spHash) {
        return spTexMap.containsKey(spHash) ? spTexMap.get(spHash) : 0;
    }

    /**
     * returns the Sprite (hash) that originated the texture (hash)
     *
     * @param texHash the texture hashCode that belows to the Sprite to return
     * @return
     */
    public static int _GLgetSpriteHash(int texHash) {
        synchronized (spTexMap) {
            for (int spHash : spTexMap.keySet()) {
                if (spTexMap.get(spHash) == texHash) {
                    return spHash;
                }
            }
        }
        return 0;
    }

    /**
     * render the Sprite within an OpenGL context
     *
     * @param gld the RenderingSceneGL that is to be rendered onto
     * @param sp the Sprite to render
     * @param texBounds 2D Rectangle to render at
     * @param z z-depth to render at
     */
    public static void _GLRenderSprite(RenderingSceneGL gld, SpriteGLHandler sp, Rectangle texBounds, double z) {
        _GLRenderSprite(gld, true, sp, texBounds, z);
    }

    /**
     * renders the Sprite within an OpenGL context
     *
     * @param gld the RenderingSceneGL that is to be rendered onto
     * @param keepBinding keeps the associated texture in VRAM for further use
     * or not, resp.
     * @param sp the Sprite to render
     * @param texBounds 2D Rectangle bounds to render at
     * @param z z-depth to render at
     */
    public static void _GLRenderSprite(RenderingSceneGL gld, boolean keepBinding, SpriteGLHandler sp, Rectangle texBounds, double z) {
        _GLRenderSprite(gld, keepBinding, sp, texBounds, z, 0, null, null, 0, null, null, null);
    }
    /**
     * fx renderer delegating
     */
    private static GLFX fx = new GLFX() {
        @Override
        protected void _GLRender2D(RenderingSceneGL gld, boolean keepBinding, GL2DObject obj, double z, int transform, DoubleBuffer scaleArgs, DoubleBuffer rotateArgs, DoubleBuffer translateArgs, FloatBuffer colorBlend, FloatBuffer alphaBlend) {
            Rectangle texBounds = obj.getShape().getBounds();
            if (!Sprite._GLIsLoaded(obj.getUID())) {
                return;
            }
            SpriteGLHandler mySp = Sprite._GLHandlers.getHandler(obj.getUID());
            if (DebugMap._getInstance().isDebuggerEnabled(Sprite.class) && DebugMap._getInstance().isDebugLevelEnabled(gfx.DBUG)) {
                System.out.println(">>>>>>>>>>>>>GL draw sprite " + obj.getUID() + " : " + texBounds + " z " + z);
            }
            if (_paintStatus) {
                GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
                _renderTexture2D(gld, false, null, texBounds, z, transform, scaleArgs, rotateArgs, translateArgs, null, BufferIO._wrapf(Color.RED.getRGBComponents(new float[4])));
                GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
            }
            /*
             * int tex = _GLgetTexHash(mySp.keySet().iterator().next());
             */
            if (GLHandler.sTex.isTexture3D(mySp.getTexHash())) {
                _renderTexture3D(gld, keepBinding, mySp.getTexHash(), mySp.getrCoord(), texBounds, z, transform, scaleArgs, rotateArgs, translateArgs, alphaBlend, colorBlend);
            } else {
                _renderTexture2D(gld, keepBinding, mySp.getTexHash(), texBounds, z, transform, scaleArgs, rotateArgs, translateArgs, alphaBlend, colorBlend);
            }
        }
    };
    private float texPty = .5f;

    /**
     * @param texPty
     * @default value is .5f by default
     */
    public void setTexPty(float texPty) {
        this.texPty = texPty;
    }

    /**
     * @return @see #setTexPty(float)
     */
    public float getTexPty() {
        return texPty;
    }

    /**
     *
     * @param sp
     * @return
     */
    public static JComponent _makeSpriteJComponent(final Sprite sp) {
        JComponent render = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                sp.paintComponentImpl(g, this);
            }
        };
        render.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                /*
                 * to avoid crappy affect if the component is stretched out
                 */
                super.componentResized(e);
                sp.clearResource();
            }
        });
        sp.setObs(render);
        render.setMinimumSize(sp.getSize());
        render.setPreferredSize(sp.getSize());
        return render;
    }

    /**
     * @param outputMime
     * @return
     * @see ImageCollection#_getBufferedType(String)
     */
    public static int _getBufferedType(String outputMime) {
        return ImageCollection._getBufferedType(outputMime);
    }

    private void waitFor(Image data) throws InterruptedException {
        renderableImpl.io.waitFor(data);
    }

    /**
     * writes the image data to sp stream that supports Image data. Output mime
     * type is {@linkplain #getStoreMime()}.
     * <br>TILING : if {@linkplain #isTileModeEnabled()} returns true, then a
     * tile writing is attempted.
     * <br><b>Closes any open stream specified as output after the write
     * finishes.</b>
     *
     * @param output any output like from {@link #_load(java.lang.Object)}, but
     * not a RandomAccessFile.
     * @param compressionQuality between 0 and 1, a negative value indicates
     * that it's unchanged
     * @throws IOException
     * @throws InterruptedException
     * @see ImageCollection#_getCompressionType(java.lang.String, int)
     */
    public void writeImageToOutput(Object output, float compressionQuality) throws IOException, InterruptedException {
        int pty = Thread.currentThread().getPriority();
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        if (this.isDebugEnabled()) {
            System.out.println("Writing sprite image to stream...");
        }
        if (output == null) {
            throw new JXAException("WARNING ! No suitable image output stream to write ! " + this.toString());
        }
        try {
            final Monitor monitor = this.renderableImpl.imageSynch;
            synchronized (monitor) {
                Dimension dim = null;
                IIOMetadata metadata = null;
                ImageWriter w = this.renderableImpl.io.getWriter(0, this.storeMime);
                ImageWriteParam iwp = this.renderableImpl.io._getIWP(w, dim, this.storeMime, this._type, compressionQuality);
                if (this.renderableImpl.io.IIOcontrollerEnabled) {
                    iwp.setController(SpriteIO.IIOControllers.get(this.storeMime));
                    if (iwp.hasController()) {
                        iwp.activateController();
                    }
                }
                ImageTypeSpecifier its = (this.renderableImpl.getStoreMode() & Sf3RenderableImpl.MODE_TILE) != 0 ? SpriteIO._getITS(this._type) : SpriteIO._getITS((RenderedImage) this.data);
                if ((this.renderableImpl.getStoreMode() & Sf3RenderableImpl.MODE_TILE) == 0) {
                    if (!(this.data instanceof BufferedImage)) {
                        this.data = convertToBuffered(this.data, this.renderableImpl.io.obs, true);
                    }
                    if (((BufferedImage) this.data).getType() != this._type) {
                        this.data = convertToBuffered(this.data, this.renderableImpl.io.obs, true);
                    }
                    this.waitFor(this.data);
                    if (this.data == null) {
                        throw new JXAException("Sprite " + this.toString() + " had no data to write ");
                    }
                    if (!(this.data instanceof RenderedImage)) {
                        throw new JXAException("READ/WRITE ERROR : Sprite image isn't compatible with RenderedImage. " + this.data.getClass().getName());
                    }
                    dim = new Dimension(this.data.getWidth(this.renderableImpl.io.obs), this.data.getHeight(this.renderableImpl.io.obs));
                    this.renderableImpl.io.prepareWrite(dim, w, its, iwp, this.isUseIIOCacheEnabled(), output, this.data, this._type);
                    this.renderableImpl.io.writeToOutput(metadata, null, w, iwp, (BufferedImage) this.data);
                } else {
                    ImageReader r = this.renderableImpl.io.getReader(0, this.tileMime);
                    r = this.renderableImpl.io.__getInput(this.tileSrc, this.innerResource, r, this.isUseIIOCacheEnabled());
                    dim = new Dimension(r.getWidth(0), r.getHeight(0));

                    if ((this.renderableImpl.io.prepareWrite(dim, w, its, iwp, this.isUseIIOCacheEnabled(), output, r, this._type) & _TILEFLAGS) == _TILEFLAGS) {
                        this.renderableImpl.io.writeToOutputTiled(dim, SpriteIO._WRITE_TILES_DIMENSION, w, iwp, r, this._type, null);
                    } else {
                        this.renderableImpl.io.writeToOutput(dim, w, iwp, r, this._type, null);
                    }
                }
                this.renderableImpl.io.endWrite(w);
                if (this.isDebugEnabled()) {
                    System.out.println("writing is done.");
                }
                Thread.currentThread().setPriority(pty);
            }
        } catch (IllegalArgumentException ex) {
            JXAException e = new JXAException(ex);
            throw e;
        }
    }

    /**
     * converts the specified image data to sp BufferedImage instance using the
     * specified Component that will receive the data. CAUTION : avoid this
     * method if in {@linkplain #isTileModeEnabled() tile mode}, it may overflow
     * the memory if the image is too big.
     *
     * @param data the image data to specify as the source for the conversion
     * @param pobs the Component to receive the image data
     * @param flush will flush the source data, after conversion
     * @return sp a new BufferedImage instance
     */
    public BufferedImage convertToBuffered(RenderedImage data, Component pobs, boolean flush) {
        final Monitor monitor1 = this.renderableImpl.imageSynch;
        synchronized (monitor1) {
            BufferedImage b = null;
            try {
                b = this.renderableImpl.io._convertToBuffered(data, this._type, pobs, flush);
            } catch (InterruptedException ex) {
                if (this.isDebugEnabled()) {
                    ex.printStackTrace();
                }
            } finally {
                return b;
            }
        }
    }

    /**
     * converts the specified image data to sp BufferedImage instance using the
     * specified Component that will receive the data. CAUTION : avoid this
     * method if in {@linkplain #isTileModeEnabled() tile mode}, it may overflow
     * the memory if the image is too big.
     *
     * @param data the image data to specify as the source for the conversion
     * @param pobs the Component to receive the image data
     * @param flush will flush the source data, after conversion
     * @return sp a new BufferedImage instance
     */
    public BufferedImage convertToBuffered(Image data, Component pobs, boolean flush) {
        final Monitor monitor1 = this.renderableImpl.imageSynch;
        synchronized (monitor1) {
            BufferedImage b = null;
            try {
                b = this.renderableImpl.io._convertToBuffered(data, this._type, pobs, flush);
            } catch (InterruptedException ex) {
                if (this.isDebugEnabled()) {
                    ex.printStackTrace();
                }
            } finally {
                return b;
            }
        }
    }

    /**
     * performs powerful IO operations.
     *
     * @return the SpriteIO instance for this sprite.
     */
    public SpriteIO getIO() {
        return renderableImpl.io;
    }
}
