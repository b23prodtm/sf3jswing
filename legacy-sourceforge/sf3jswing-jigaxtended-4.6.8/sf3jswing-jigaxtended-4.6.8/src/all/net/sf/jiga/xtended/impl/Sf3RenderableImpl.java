/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import org.eclipse.imagen.PerspectiveTransform;
import net.sf.jiga.xtended.impl.game.RenderingScene;
import net.sf.jiga.xtended.kernel.DebugMap;
import net.sf.jiga.xtended.kernel.Debugger;
import net.sf.jiga.xtended.kernel.FileHelper;
import net.sf.jiga.xtended.kernel.JXAenvUtils;
import net.sf.jiga.xtended.kernel.Level;
import net.sf.jiga.xtended.kernel.Monitor;
import net.sf.jiga.xtended.kernel.Threaded;

/**
 *
 * @author www.b23prodtm.info
 */
public class Sf3RenderableImpl implements Sf3Renderable, Threaded, Debugger {

    /**
     * returns true or false, whether the Java2D mode is enabled or not, resp.
     *
     * @return true or false, whether the Java2D mode is enabled or not, resp.
     */
    @Override
    public boolean isJava2DModeEnabled() {
        return (renderMode & MODE_JAVA2D) != 0;
    }

    /**
     * returns true or false, whether the texturing mode is enabled or not,
     * resp.
     *
     * @return true or false, whether the texturing mode is enabled or not,
     * resp.
     */
    @Override
    public boolean isTextureModeEnabled() {
        return (renderMode & MODE_TEXTURE) != 0;
    }

    /**
     * NOTICE : tile mode is more stable with the image/tiff mime type
     *
     * @return
     * @see #setTileModeEnabled()
     */
    @Override
    public boolean isTileModeEnabled() {
        return (renderMode & MODE_TILE) != 0;
    }

    /**
     * sets on/off the Java2D mode, which is usually used for rendering with
     * Swing classic mode (not LWJGL for instance).
     *
     * @param b dis/enables the Java2D Swing classic mode. (you can disable this
     * when rendering openGL that can free up some memory)
     * @default true
     */
    @Override
    public void setJava2DModeEnabled(boolean b) {
        renderMode = b ? renderMode | MODE_JAVA2D : renderMode & ~MODE_JAVA2D;
    }

    /**
     * this changes the default status of the storing bits. it modifies how the
     * cache will store the Sprite data. {@link #MODE_TILE} is not available for
     * Sprite made from an Image instance source (ImageProducer). NOTICE : tile
     * mode is more stable with the image/tiff mime type
     *
     * @param mode the specified storage mode to use
     * @default MODE_JAVA2D | MODE_TEXTURE
     * @see #MODE_JAVA2D
     * @see #MODE_TEXTURE
     * @see #MODE_TILE
     */
    @Override
    public void setStoreMode(int mode) {
        assert (mode & (MODE_JAVA2D | MODE_TEXTURE)) != 0 : "please use a valid mode !";
        storeMode = mode;
    }

    @Override
    public int getStoreMode() {
        return storeMode;
    }

    /**
     * if the texturing mode is enabled, the cache will store the image as a new
     * texture ready for output to OpenGL.
     *
     * @param b dis/enables the texturing mode
     * @default false
     */
    @Override
    public void setTextureModeEnabled(boolean b) {
        renderMode = b ? renderMode | MODE_TEXTURE : renderMode & ~MODE_TEXTURE;
    }

    /**
     * if {@linkplain #isTilesAvailable()} returns false, then tiling may be
     * enabled but the cache is not tiled. if {@linkplain #isTilesAvailable()}
     * returns true, then tiling may be enabled and the cache is tiled hence
     * {@linkplain #getTileReader()} can return tiles. that is if you have once
     * cached this image and the tiling process succeeded. CAUTION : enabling
     * tileMode may not work for smaller pictures size than
     * {@linkplain #_WRITE_TILES_DIMENSION} NOTICE : tile mode cannot be enabled
     * for ImageProducer src (Sprite built with Image as data). NOTICE : tile
     * mode is more stable with the image/tiff mime type.
     *
     * @param b
     * @see #isJava2DModeEnabled()
     * @see #isTextureModeEnabled()
     */
    @Override
    public void setTileModeEnabled(boolean b) {
        renderMode = b ? renderMode | MODE_TILE : renderMode & ~MODE_TILE;
    }
    private int renderMode = MODE_JAVA2D;
    private int storeMode = MODE_JAVA2D | MODE_TEXTURE;

    public int getMode() {
        return renderMode;
    }

    public void setMode(int renderMode) {
        assert (renderMode & (MODE_JAVA2D | MODE_TEXTURE)) != 0 : "please use a valid mode !";
        this.renderMode = renderMode;
    }

    public static void modeCopy(Sf3Renderable src, Sf3Renderable dst) {
        dst.setStoreMode(src.getStoreMode());
        dst.setJava2DModeEnabled(src.isJava2DModeEnabled());
        dst.setTileModeEnabled(src.isTileModeEnabled());
        dst.setTextureModeEnabled(src.isTextureModeEnabled());
        dst.setHardwareAccel(src.isHardwareAccel());
        dst.setUseIIOCacheEnabled(src.isUseIIOCacheEnabled());
    }

    @Override
    public RenderingScene getRenderingScene() {
        return rsc;
    }
    RenderingScene rsc = null;

    @Override
    public void setRenderingScene(RenderingScene renderingScene) {
        rsc = renderingScene;
        if (renderingScene != null) {
            setMultiThreadingEnabled(renderingScene.isMultiThreadingEnabled());
            setHardwareAccel(rsc.isVRAMSpritesEnabled() && !rsc.isLWJGLAccel());
            if (rsc.isLWJGLAccel()) {
                setTextureModeEnabled(true);
            }
            if (!rsc.isLWJGLAccel()) {
                setJava2DModeEnabled(true);
            }
        }
    }
    boolean hardwareAccel = false;

    @Override
    public void setHardwareAccel(boolean b) {
        hardwareAccel = b;
    }

    @Override
    public boolean isHardwareAccel() {
        return hardwareAccel;
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
        bounds.setBounds(x, y, width, height);
    }

    /**
     *
     * @param r
     */
    @Override
    public void setBounds(Rectangle r) {
        setBounds(r.x, r.y, r.width, r.height);
    }
    /**
     *
     */
    protected Rectangle bounds = new Rectangle();

    /**
     *      */
    @Override
    public Rectangle getBounds(Rectangle rv) {
        if (rv != null) {
            rv.setBounds(bounds);
        } else {
            rv = bounds.getBounds();
        }
        return rv;
    }

    /**
     *
     */
    @Override
    public Rectangle getBounds() {
        return bounds.getBounds();
    }

    /**
     *
     * @param width
     * @param height
     */
    @Override
    public void setSize(int width, int height) {
        setSize(new Dimension(width, height));
    }

    /**
     *
     * @param size
     */
    @Override
    public void setSize(Dimension size) {
        if (size == null) {
            setBounds(bounds.x, bounds.y, 0, 0);
        } else {
            setBounds(bounds.x, bounds.y, size.width, size.height);
        }
    }

    /**
     *
     * @return
     */
    @Override
    public Dimension getSize() {
        return bounds.getSize();
    }

    /**
     *
     * @return
     */
    @Override
    public Point getLocation() {
        return bounds.getLocation();
    }

    /**
     *
     * @param p
     */
    @Override
    public void setLocation(Point p) {
        setBounds(p.x, p.y, bounds.width, bounds.height);
    }

    /**
     *
     * @param x
     * @param y
     */
    @Override
    public void setLocation(int x, int y) {
        setLocation(new Point(x, y));
    }
    public Monitor paintMonitor = new Monitor(), validateMonitor = new Monitor(), imageSynch = new Monitor();

    @Override
    public Monitor[] getGroupMonitor() {
        return new Monitor[]{paintMonitor, validateMonitor, imageSynch};
    }

    /**
     * tg [paintmonitor,validatemonitor,imagesynch]
     */
    @Override
    public void setGroupMonitor(Monitor... tg) {
        paintMonitor = tg[0];
        validateMonitor = tg[1];
        imageSynch = tg[2];
    }
    private boolean multiThread = false;

    @Override
    public boolean isMultiThreadingEnabled() {
        return multiThread;
    }

    @Override
    public void setMultiThreadingEnabled(boolean bln) {
        multiThread = bln;
    }

    @Override
    public boolean draw(Component obs, Graphics2D g2) throws InterruptedException {
        return true;
    }

    @Override
    public boolean draw(Component obs, Graphics2D g2, AffineTransform tx, PerspectiveTransform ptx) throws InterruptedException {
        return true;
    }

    @Override
    public boolean draw(Component obs, Graphics2D g2, int fx, Point fx_loc, Color fx_color) throws InterruptedException {
        return true;
    }

    @Override
    public Object runValidate() {
        return true;
    }

    @Override
    public int getWidth() {
        return bounds.width;
    }

    @Override
    public int getHeight() {
        return bounds.height;
    }
    Class<? extends Debugger> debugger;

    public Sf3RenderableImpl() {
        this(Sf3RenderableImpl.class);
    }

    SpriteIO io = null;
    
    public Sf3RenderableImpl(Class<? extends Debugger> clazzDebug) {
        this(clazzDebug, false);
    }
    
    public Sf3RenderableImpl(Class<? extends Debugger> clazzDebug, boolean withIO) {
        this.debugger = clazzDebug;
        DebugMap._getInstance().associateDebugLevel(clazzDebug, Sprite.DBUG_RENDER_LOW);
        io = new SpriteIO();
    }

    public boolean isDebugEnabled() {
        if (debugger instanceof Class) {
            return DebugMap._getInstance().isDebuggerEnabled(debugger);
        }
        return false;
    }

    public void setDebugEnabled(boolean bln) {
        if (debugger instanceof Class) {
            DebugMap._getInstance().setDebuggerEnabled(bln, debugger);
        }
    }

    @Override
    public void setUseIIOCacheEnabled(boolean b) {
        useIIOCache = b;
    }
    
    /**
     @see #setUseIIOCacheEnabled(boolean) 
     */
    boolean useIIOCache = false;

    @Override
    public boolean isUseIIOCacheEnabled() {
        return useIIOCache && FileHelper._isImageIOCacheEnabled();
    }
}
