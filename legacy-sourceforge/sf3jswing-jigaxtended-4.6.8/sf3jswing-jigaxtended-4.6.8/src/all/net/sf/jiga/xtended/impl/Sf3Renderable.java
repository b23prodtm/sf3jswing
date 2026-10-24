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
import javax.media.jai.PerspectiveTransform;
import net.sf.jiga.xtended.impl.game.RenderingSceneComponent;
import net.sf.jiga.xtended.kernel.JXAenvUtils;

/**
 *
 * @author www.b23prodtm.info
 */
public interface Sf3Renderable extends RenderingSceneComponent{

    /***/
    public static final int MODE_JAVA2D = 1;
    /***/
    public static final int MODE_TEXTURE = 2;
    /***/
    public static final int MODE_TILE = 4;
    /**
     * @return the current storage mode
     */
    int getStoreMode();

    /**
     * returns true or false, whether the Java2D mode is enabled or not, resp.
     * @return true or false, whether the Java2D mode is enabled or not, resp.
     */
    boolean isJava2DModeEnabled();

    /**
     * returns true or false, whether the texturing mode is enabled or not, resp.
     * @return true or false, whether the texturing mode is enabled or not, resp.
     */
    boolean isTextureModeEnabled();

    /**
     * NOTICE : tile mode is more stable with the image/tiff mime type
     * @return
     * @see #setTileModeEnabled()
     */
    boolean isTileModeEnabled();

    /**
     * sets on/off the Java2D mode, which is usually used for rendering with Swing classic mode (not LWJGL for instance).
     * @param b dis/enables the Java2D Swing classic mode. (you can disable this when rendering openGL that can free up some memory)
     * @default true
     */
    void setJava2DModeEnabled(boolean b);

    /**
     * this changes the default status of the storing bits. it modifies how the cache will store the Sprite data.
     * {@link #MODE_TILE} is not available for Sprite made from an Image instance source (ImageProducer).
     * NOTICE : tile mode is more stable with the image/tiff mime type
     * @param mode the specified storage mode to use
     * @default MODE_JAVA2D | MODE_TEXTURE
     * @see #MODE_JAVA2D
     * @see #MODE_TEXTURE
     * @see #MODE_TILE
     */
    void setStoreMode(int mode);

    /**
     * if the texturing mode is enabled, the cache will store the image as a new texture ready for output to OpenGL.
     * @param b dis/enables the texturing mode
     * @default false
     */
    void setTextureModeEnabled(boolean b);

    /**
     * if {@linkplain #isTilesAvailable()} returns false, then tiling may be enabled but the cache is not tiled.
     * if {@linkplain #isTilesAvailable()} returns true, then tiling may be enabled and the cache is tiled hence {@linkplain #getTileReader()} can return tiles.
     * if tiling is enabled you won't be able to use java2d {@linkplain #draw(Component, Graphics2D)} for rendering, only textures can be rendered.
     * that is if you have once cached this image and the tiling process succeeded.
     * CAUTION : enabling tileMode may not work for smaller pictures size than {@linkplain #_WRITE_TILES_DIMENSION}
     * NOTICE : tile mode cannot be enabled for ImageProducer src (Sprite built with Image as data).
     * NOTICE : tile mode is more stable with the image/tiff mime type.
     * @param b
     * @see #isJava2DModeEnabled()
     * @see #isTextureModeEnabled()
     */
    void setTileModeEnabled(boolean b);

    /**
     * current bounds (those are valid AFTER a (run)validate() call).
     * NOTICE : if the zoom is enabled, these bounds may be altered in some way after the validation
     * and may be read again to get exact values. For Animation, those bounds stay unaltered, and the {@linkplain Animation#getCurrentSprite() current Sprite} can return the
     * correct bounds when the zoom is enabled.
     * @return
     * @see #setZoomEnabled(boolean, double)
     */
    Rectangle getBounds();

    /**
     * current bounds (those are valid AFTER a (run)validate() call).
     * NOTICE : if the zoom is enabled, these bounds may be altered in some way after the validation
     * and may be read again to get exact values. For Animation, those bounds stay unaltered, and the curent Sprite may return the
     * correct bounds when the zoom is enabled.
     * @param rv
     * @return
     * @see #setZoomEnabled(boolean, double)
     */
    Rectangle getBounds(Rectangle rv);

    /**
     *
     * @return
     */
    Point getLocation();

    /**
     *
     * @return
     */
    Dimension getSize();

    /**
     *
     * @param r
     */
    void setBounds(Rectangle r);

    /**
     *
     * @param x
     * @param y
     * @param width
     * @param height
     */
    void setBounds(int x, int y, int width, int height);

    /**
     *
     * @param p
     */
    void setLocation(Point p);

    /**
     *
     * @param x
     * @param y
     */
    void setLocation(int x, int y);

    /**
     *
     * @param size
     */
    void setSize(Dimension size);

    /**
     *
     * @param width
     * @param height
     */
    void setSize(int width, int height);
    
    /***/
    int getWidth();
    
    /***/
    int getHeight();

    /**
     * draws the animation on the specified graphics and component context
     * @return true or false whether the drawing's completed or not (this value cannot be used for synchronization)
     * @see #draw(Component, Graphics2D, AffineTransform, PerspectiveTransform)
     * @param g2 graphics instance to draw on
     * @param obs the component that is observing this animation
     */
    boolean draw(Component obs, Graphics2D g2) throws InterruptedException;

    /**
     * draws with the specified AffineTransform and PerspectiveTransform instances
     * @return true or false whether the drawing's completed or not (this value cannot be used for synchronization)
     * @param g2 graphics instance to draw on
     * @param tx transform instance
     * @param ptx perspective transform instance
     * @param obs the component that is observing this animation
     */
    boolean draw(Component obs, Graphics2D g2, AffineTransform tx, PerspectiveTransform ptx) throws InterruptedException;

    /**
     * draws with the specified OR-bitwise-combination FX
     * @return true or false whether the drawing's completed or not (this value cannot be used for synchronization)
     * @param g2 graphics instance to draw on
     * @param fx the FX OR-bitwise-combination to use, or 0 for NONE
     * @param fx_loc the FX translation to add to the location, or new Point(0, 0) for NONE
     * @param fx_color the FX color or null for NONE
     * @param obs the component that is observing this animation
     * @throws java.lang.InterruptedException if the current Thread gets interrupted
     * @see #__draw(Component, Graphics2D, AffineTransform, PerspectiveTransform, int, Point, Color)
     * @see Sprite#draw(Component, Graphics2D, int, Point, Color)
     */
    boolean draw(Component obs, Graphics2D g2, int fx, Point fx_loc, Color fx_color) throws InterruptedException;

    /**
     * Animation : refreshes the animation and the cache map to current params and prints current iterator value.
     * if activerendering is activated, it also runs the player thread to retrieve the animator index value to paint.
     * it must be called each time refreshing the current frame image is required.
     * @discussion (comprehensive description)
     * @see #spm
     * @see #setZoomEnabled(boolean, double)
     * @see #setFlipEnabled(boolean, int)
     * @see Sprite#runValidate()
     */
    Object runValidate();
    
    /** may use temporary folders. Enable only if for big files, that are not accessed too frequently (unlike Animation files)
     * <br>ImageIO must be available.
     @see JXAenvUtils#_isImageIOCacheEnabled() 
     */
    public void setUseIIOCacheEnabled(boolean b);
    public boolean isUseIIOCacheEnabled();
    
}
