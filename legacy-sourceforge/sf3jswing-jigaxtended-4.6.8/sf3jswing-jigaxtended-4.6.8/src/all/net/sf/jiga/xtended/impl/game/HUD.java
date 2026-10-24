/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.game;

import java.awt.*;
import java.nio.FloatBuffer;
import java.util.*;
import org.eclipse.imagen.GraphicsJAI;
import net.sf.jiga.xtended.JXAException;
import net.sf.jiga.xtended.impl.GUIConsoleOutput;
import net.sf.jiga.xtended.impl.game.TextLayout.POS;
import net.sf.jiga.xtended.impl.game.gl.GLFX;
import net.sf.jiga.xtended.impl.game.gl.GLHandler;
import net.sf.jiga.xtended.impl.game.gl.RenderingSceneGL;
import net.sf.jiga.xtended.impl.game.gl.StencilPBO;
import net.sf.jiga.xtended.impl.game.gl.StencilPBOListener;
import net.sf.jiga.xtended.impl.system.ThreadBlock;
import net.sf.jiga.xtended.impl.game.gl.geom.GLGeom;
import net.sf.jiga.xtended.impl.system.BufferIO;
import net.sf.jiga.xtended.kernel.DebugMap;
import net.sf.jiga.xtended.kernel.JXAenvUtils;
import net.sf.jiga.xtended.kernel.Level;
import net.sf.jiga.xtended.kernel.Resource;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.Util;

/**
 * Heads-up-display : display Text on Scene. Text contents are "pushed" (*).
 * Each push adds one line/string of text, bound to a key-map. To modify the
 * contents, use the approriate methods. <br>PBO buffer required for openGL
 * rendering. there are two rendering capabilities :
 * <ol><li>{@link #GLbegin() begin openGL} or {@link #begin() Java2D} HUD
 * content accumulation eventually draws a string</li>
 * <li>{@link #pushContent(java.lang.String, java.lang.String, net.sf.jiga.xtended.impl.game.TextLayout.POS, net.sf.jiga.xtended.impl.game.TextLayout.POS, boolean) pushContent}
 * or
 * {@link #updateContent(java.lang.String, java.lang.String, boolean) updateContent}
 * </li><li>which is rendered when {@link #GLrenderContents(net.sf.jiga.xtended.impl.game.RenderingSceneGL)
 * } or {@link #renderContents(java.awt.Graphics2D) } is invoked;</li>
 * <li>{@link #GLend(net.sf.jiga.xtended.impl.game.RenderingSceneGL) end openGL}
 * or {@link #end() Java2D} HUD accumulation and paint to the front/back render
 * buffer</li></ol> All of these GL-calls must be called between a single
 * begin-end block {@link #GLbegin()
 * }
 * and {@link #GLend(net.sf.jiga.xtended.impl.game.RenderingSceneGL) }or {@link #begin()
 * } and {@link #end() } resp. for openGL or Java2D.
 *
 * @author www.b23prodtm.info
 */
public class HUD implements RenderingSceneComponent, Resource, StencilPBOListener {

    private RenderingScene scene;

    private HUD() {
        font = new Font("Monospaced", Font.PLAIN, 14);
        initHUDArgs();
    }

    public static final HUD _new(RenderingScene scene) {
        HUD h = new HUD();
        h.setRenderingScene(scene);
        return h;
    }
    private Color foreground = Color.BLUE;
    private Color background = Color.DARK_GRAY;

    public Color getBackground() {
        return background;
    }

    public Color getForeground() {
        return foreground;
    }

    public void setBackground(Color background) {
        this.background = background;
    }

    public void setForeground(Color foreground) {
        this.foreground = foreground;
    }
    private Rectangle bounds = new Rectangle();
    Stack<Rectangle> dirtyArea = new Stack<Rectangle>();
    protected double z = 0;

    public double getZ() {
        return z;
    }

    public void setBounds(Rectangle r, double z) {
        setBounds(r.x, r.y, r.width, r.height, z);
    }

    public void setBounds(int x, int y, int width, int height, double z) {
        Rectangle nBounds = new Rectangle(x, y, width, height);
        if (!nBounds.equals(bounds) || this.z != z) {
            resetContentsToDirty();
        }
        this.bounds.setBounds(nBounds);
        this.z = z;

    }

    public boolean isDirty() {
        return !dirtyArea.isEmpty();
    }
    ThreadBlock render = new ThreadBlock();

    private void GLclearRegion(Rectangle boundsRegion) {
        try {
            GLFX._GLpushRender("__GLclear", this, new Object[]{boundsRegion}, new Class[]{Rectangle.class});
        } catch (Exception ex) {
            if (DebugMap._getInstance().isDebugLevelEnabled(RenderingScene.DBUG_RENDER)) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * INTERNAL USAGE ONLY
     */
    public void __GLclear(Rectangle boundsRegion) {
        if (GLHandler.ext.PixelBufferObjects.GL_isAvailable() && enabled) {
            glPBO.GLclear(boundsRegion);
        }
    }

    public void begin() {
        render.begin();
        synchronized (HUDArgs) {
            for (TextLayout[] hca_arr : HUDArgs) {
                for (TextLayout hca : hca_arr) {
                    hca.nextOutputPX.setLocation(0, 0);
                }
            }
        }
    }

    /**
     * stencil is enabled (only if the {@link GLHandler.ext#PixelBufferObjects}
     * extension is available) <br> GLBegin uses the HUD bounds to project PBO
     * pixels buffers and will then translate as needed to its bounds.X and
     * bounds.Y (Z is a fixed member value), so that the raster pos must be set
     * to the orignal location which the HUD is placed from.
     */
    public void GLbegin() {
        try {
            GLFX._GLpushRender("__GLbegin", this, new Object[]{}, new Class[]{});
        } catch (Exception ex) {
            if (DebugMap._getInstance().isDebugLevelEnabled(RenderingScene.DBUG_RENDER)) {
                ex.printStackTrace();
            }
        }
    }
    /**
     * en/disable {@link StencilPBO.MODE pbo stream mode} (unstable)
     *
     * @see DebugMap#isDebugLevelEnabled(net.sf.jiga.xtended.kernel.Level)
     */
    public final static Level PBOModeSTREAMenabled = DebugMap._getInstance().newDebugLevel();

    /**
     * FOR INTERNAL USAGE ONLY
     */
    public void __GLbegin() {
        assert !bounds.isEmpty() : "no bounds defined";
        begin();
        if (GLHandler.ext.PixelBufferObjects.GL_isAvailable() && enabled) {
            if (glPBO == null) {
                glPBO = new StencilPBO(bounds, z, DebugMap._getInstance().isDebugLevelEnabled(PBOModeSTREAMenabled) ? StencilPBO.MODE.MODE_STREAM : StencilPBO.MODE.MODE_DYNAMIC);
                glPBO.addListener(this);
            } else /*
             * in case the context switched off in-between.
             */ {
                glPBO.prepareBufferObject(bounds, z, false);
            }
            glPBO.beginStencil();
        }
        /**
         * dirtyArea.clear();
         */
    }
    private StencilPBO glPBO = null;

    public void end() {
        render.end();
    }

    /**
     * packs dirtyregion into PBO and unpacks the PBO onto the stencil buffer
     * and draws a square over the stencil buffer (only if the
     * {@link GLHandler.ext#PixelBufferObjects} extension is available) <br>if
     * {@link #isEnabled()} returns false, then it will not draw anything, but
     * the PBO buffer will not be deleted.
     *
     * @param g2
     *
     */
    public void GLend() {
        try {
            GLFX._GLpushRender("__GLend", this, new Object[]{}, new Class[]{});
        } catch (Exception ex) {
            if (DebugMap._getInstance().isDebugLevelEnabled(RenderingScene.DBUG_RENDER)) {
                ex.printStackTrace();
            }
        }
    }
    private boolean backgroundOverlay = true;

    /**
     * @return
     */
    public boolean isBackgroundOverlay() {
        return backgroundOverlay;
    }

    /**
     * whether to draw a light translucent overlay of background color before to
     * draw the HUD contents.
     *
     * @param backgroundOverlay true to enable; false otherwise
     * @param backgroundOverlay
     */
    public void setBackgroundOverlay(boolean backgroundOverlay) {
        this.backgroundOverlay = backgroundOverlay;
    }

    /**
     * FOR INTERNAL USAGE ONLY
     *
     * @param g2
     */
    public void __GLend() {
        if (GLHandler.ext.PixelBufferObjects.GL_isAvailable() && enabled) {
            /**
             * the stencil buffer keeps the console contents as "decals"
             */
            while (!dirtyArea.isEmpty()) {
                glPBO.GLpackStencil(dirtyArea.pop());
            }
            /**
             * clean and disable stencil write
             */
            glPBO.endStencil();
            glPBO.GLflush((RenderingSceneGL)scene);
            Util.checkGLError();
            if (backgroundOverlay) {
                FloatBuffer c = GLHandler._GLgetCurrentColor(), backgroundColor = GLGeom.getCWrgbaColors(background, .2f);
                GL11.glColor4f(backgroundColor.get(), backgroundColor.get(), backgroundColor.get(), backgroundColor.get());
                GLHandler._transformBegin(bounds, z, 0, null, null, null);
                GLGeom._GLfillRect((RenderingSceneGL)scene, 0, true, new Rectangle(bounds.getSize()), 1, 0);
                GLHandler._transformEnd();
                GL11.glColor3f(c.get(), c.get(), c.get());
            }
            glPBO.GLrender((RenderingSceneGL)scene, GLGeom.getCWrgbaColors(foreground, 1));
            /*
             * StencilPBO.cleanStencilBuffer();
             */
        }
        end();
    }
    private boolean enabled = true;

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            resetContentsToDirty();
        }
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * renders contents defined from either {@link #pushContent(java.lang.String, java.lang.String, int, int, boolean)
     * }, {@link #updateContent(java.lang.String, java.lang.String) } or {@link #updateContent(java.lang.String, java.lang.String, boolean)
     * } methods.
     */
    public void GLrenderContents(RenderingSceneGL canvas) {
        try {
            GLFX._GLpushRender("__GLrenderContents", this, new Object[]{canvas}, new Class[]{RenderingSceneGL.class});
        } catch (Exception ex) {
            if (DebugMap._getInstance().isDebugLevelEnabled(RenderingScene.DBUG_RENDER)) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * FOR INTERNAL USAGE ONLY
     *
     * @param canvas
     */
    public void __GLrenderContents(RenderingSceneGL canvas) {
        _renderContents(canvas);
    }

    /**
     * renders text in the order it was pushed by the code. java2d rendering
     * renders contents defined from either {@link #pushContent(java.lang.String, java.lang.String, int, int, boolean)
     * }, {@link #updateContent(java.lang.String, java.lang.String) } or {@link #updateContent(java.lang.String, java.lang.String, boolean)
     * } methods. dirty state handle if
     * GLHandler.ext.PixelBufferObjects.GL_isAvailable().
     */
    public void renderContents(Graphics2D graphics) {
        _renderContents(graphics);
    }

    /**
     * renderContents HUD contents
     */
    private void _renderContents(Object canvas) {
        if (!render.isOn()) {
            throw new JXAException(JXAException.LEVEL.SYSTEM, "not in HUD rendering block, missing HUD.begin()");
        }
        if (!enabled) {
            return;
        }
        synchronized (contents) {
            /**
             * handle each line of text
             */
            for (Iterator<HUDContent> i = contents.values().iterator(); i.hasNext();) {
                HUDContent content = i.next();

                if (canvas instanceof RenderingSceneGL) {
                    Rectangle area = null;
                    /**
                     * find the area where the content is laid out, move the
                     * cursor if the painting is not needed
                     */
                    area = findStringArea(canvas, content.prev.getText(), content.prev.getLayout().align, content.prev.getLayout().valign, content.prev.isNewLine(), !content.repaintNeeded());
                    if (content.repaintNeeded() || !content.isDrawEnabled()) {
                        /*
                         * dont show the text or clean for repaint; erase pixels
                         * from buffer object (texture target)
                         */
                        GLclearRegion(area);
                    }
                    if (content.repaintNeeded()) {
                        GLdrawString((RenderingSceneGL) canvas, content.getText(), content.getLayout().align, content.getLayout().valign, content.isNewLine());
                        content.prev = new HUDContent(content.getText(), content.getLayout(), content.isNewLine());
                        content.prev.resetDirtyState(false);
                        content.resetDirtyState(false);
                    }
                } else {
                    /**
                     * or Java2D context
                     */
                    drawString((Graphics2D) canvas, content.getText(), content.getLayout().align, content.getLayout().valign, content.isNewLine());
                }
            }
        }
    }
    /**
     * the Strings map
     */
    Map<String, HUDContent> contents = Collections.synchronizedMap(new LinkedHashMap<String, HUDContent>());
    /**
     * the layout arguments
     */
    TextLayout HUDArgs[][] = new TextLayout[3][3];

    /**
     * adds/updates a short String to the HUD;
     * {@link #contents contents Map ordering} affects the way pushed contents
     * will output.
     *
     * @param key the HUD key to identify the helper
     *
     * @param content the HUD String to add
     *
     * @param align the horizontal position on console
     *
     * @param valign the vertical position on console
     *
     * @param newLine dis/enables writing a new line
     */
    public void pushContent(String key, String text, POS align, POS valign, boolean newLine) {
        HUDContent c = contents.get(key);
        TextLayout t = getHUDArgs(align, valign);
        if (c == null) {
            c = new HUDContent(text, t, newLine);
        }
        c.setText(text);
        c.setLayout(t);
        c.setNewLine(newLine);
        contents.put(key, c);
    }

    /**
     * removes all contents, clears buffer.
     */
    public void reset() {
        contents.clear();
        if (GLHandler.ext.PixelBufferObjects.GL_isAvailable() && glPBO != null) {
            glPBO.GLclear(glPBO.ROIAsDirty(bounds, z));
        } else {
            glPBO = null;
        }
    }

    /**
     *
     */
    public void updateContent(String key, String text) {
        updateContent(key, text, contents.get(key).isNewLine());
    }

    /**
     *
     */
    public void updateContent(String key, String text, boolean newLine) {
        assert contents.containsKey(key) : "no hud key associated for " + key + ", first call setContent";
        HUDContent c = contents.get(key);
        pushContent(key, text, c.getLayout().align, c.getLayout().valign, newLine);
    }

    /**
     * dis/enable rendering
     */
    public void updateContentVisiblity(String key, boolean draw) {
        assert contents.containsKey(key) : "no hud key for " + key + ", first call setContent";
        contents.get(key).setDrawEnabled(draw);
    }

    /**
     * contents states reset to dirty (forces redraw)
     */
    public void resetContentsToDirty() {
        synchronized (contents) {
            for (Iterator<HUDContent> i = contents.values().iterator(); i.hasNext();) {
                HUDContent help = i.next();
                help.resetDirtyState(true);
            }
        }
    }

    /**
     * draw one string at the specified position
     *
     * @param graphics
     * @param str string to draw
     *
     * @param hpos horizontal position
     *
     * @param vpos vertical position
     *
     * @param newLine dis/enables writing a new line
     *
     * @see GUIConsoleOutput#BOTTOM
     *
     * @see GUIConsoleOutput#TOP
     *
     * @see GUIConsoleOutput#RIGHT
     *
     * @see GUIConsoleOutput#LEFT
     *
     * @see GUIConsoleOutput#CENTER
     *
     * @see GUIConsoleOutput#_drawLine(Map, GraphicsJAI, StringBuffer,
     * StringBuffer, boolean)
     */
    public void drawString(Graphics2D graphics, String str, POS hpos, POS vpos, boolean newLine) {
        __drawString(graphics, str, hpos, vpos, newLine, true, false);
    }

    /**
     * erase the whole line in PBO <br>(line of bounds.width width using height
     * of the returned area of
     * {@link #findStringArea(java.lang.Object, java.lang.String, net.sf.jiga.xtended.impl.game.HUD.POS, net.sf.jiga.xtended.impl.game.HUD.POS, boolean)})
     */
    public void GLclearLine(RenderingSceneGL canvas, POS hpos, POS vpos, boolean newLine) {
        try {
            GLFX._GLpushRender("__GLclearLine", this, new Object[]{canvas, hpos, vpos, newLine}, new Class[]{RenderingSceneGL.class, POS.class, POS.class, boolean.class});
        } catch (Exception ex) {
            if (DebugMap._getInstance().isDebugLevelEnabled(RenderingScene.DBUG_RENDER)) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * INTERNAL USAGE ONLY
     */
    public void __GLclearLine(RenderingSceneGL canvas, POS hpos, POS vpos, boolean newLine) {
        Rectangle r = findStringArea(canvas, "line", hpos, vpos, newLine, false);
        r.width = bounds.width;
        r.x = 0;
        GLclearRegion(r);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    /**
     * GL draw for String (w/ Java Font)
     *
     * @param graphics
     * @param str
     * @param z
     * @param vpos
     * @param hpos
     * @param newLine
     */
    private void GLdrawString(RenderingSceneGL graphics, String str, POS hpos, POS vpos, boolean newLine) {
        try {
            GLFX._GLpushRender("__GLdrawString", this, new Object[]{graphics, str, hpos, vpos, newLine}, new Class[]{RenderingSceneGL.class, String.class, POS.class, POS.class, boolean.class});
        } catch (Exception ex) {
            if (DebugMap._getInstance().isDebugLevelEnabled(RenderingScene.DBUG_RENDER)) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * @return area where the specified string will eventually be drawn
     */
    public Rectangle findStringArea(Object graphics, String str, POS hpos, POS vpos, boolean newLine, boolean moveCursorLastouputPX) {
        Rectangle r = __drawString(graphics, str, hpos, vpos, newLine, false, !moveCursorLastouputPX);
        return r;
    }

    /**
     * FOR INTERNAL USAGE ONLY
     *
     * @param graphics
     * @param str
     * @param hpos
     * @param vpos
     * @param newLine
     */
    public void __GLdrawString(RenderingSceneGL graphics, String str, POS hpos, POS vpos, boolean newLine) {
        Rectangle r = __drawString(graphics, str, hpos, vpos, newLine, true, false);
        if (GLHandler.ext.PixelBufferObjects.GL_isAvailable() && enabled) {
            glPBO.GLpackStencil(r);
        }
    }

    private void initHUDArgs() {
        POS[] posX = new POS[]{POS.LEFT, POS.CENTER, POS.RIGHT}, posY = new POS[]{POS.TOP, POS.CENTER, POS.BOTTOM};
        for (int x = 0; x < HUDArgs.length; x++) {
            for (int y = 0; y < HUDArgs[x].length; y++) {
                HUDArgs[x][y] = new TextLayout(new Point(0, 0), posX[x], posY[y], 10, 10, new Rectangle());
            }
        }
    }

    public TextLayout getHUDArgs(POS hpos, POS vpos) {
        Point consoleArea = new Point(0, 0);
        switch (hpos) {
            case LEFT:
                consoleArea.x = 0;
                break;
            case CENTER:
                consoleArea.x = 1;
                break;
            case RIGHT:
                consoleArea.x = 2;
                break;
            default:
                break;
        }
        switch (vpos) {
            case TOP:
                consoleArea.y = 0;
                break;
            case CENTER:
                consoleArea.y = 1;
                break;
            case BOTTOM:
                consoleArea.y = 2;
                break;
            default:
                break;
        }
        return HUDArgs[consoleArea.x][consoleArea.y];
    }

    /**
     * DO NOT USE, this is for internal callback
     *
     * @param graphics
     * @param str
     * @param hpos
     * @param newLine
     * @param vpos
     * @param z
     * @param draw effectively draw
     * @param revertLastOutputPX if true, the lastoutputpx in guiconsoleoutput
     * args will be reset to the value before this call, that allows to repaint
     * on the same clip area (e.g. to find the specified string area, use
     * draw=false and revertLastOutputPX=true)
     * @return dirtyArea (area within bounds) is where the text must have been
     * painted
     */
    protected Rectangle __drawString(Object graphics, String str, POS hpos, POS vpos, boolean newLine, boolean draw, boolean revertLastOutputPX) {
        assert graphics instanceof Graphics ? ((Graphics2D) graphics).getClipBounds() instanceof Rectangle : true : "graphics has no clip set and will result in nullpointerexception";
        if (!render.isOn()) {
            throw new JXAException(JXAException.LEVEL.SYSTEM, "not in HUD rendering block, missing HUD.begin()");
        }
        Color c = null;
        FloatBuffer currentColor = null;
        if (graphics instanceof Graphics) {
            Graphics2D g = (Graphics2D) graphics;
            c = g.getColor();
            g.setColor(getForeground());
            g.setFont(getFont());
            g.translate(bounds.x, bounds.y);
        } else {
            GLHandler._transformBegin(bounds, z, 0, null, null, null);
            currentColor = GLHandler._GLgetCurrentColor();
            FloatBuffer color = BufferIO._wrapf(getForeground().getRGBComponents(new float[4]));
            GL11.glColor4f(color.get(0), color.get(1), color.get(2), color.get(3));
        }
        TextLayout m = getHUDArgs(hpos, vpos);
        m.bounds.setBounds(new Rectangle(bounds.getSize()));
        Point nextOutputPX = (Point) m.nextOutputPX.clone();
        Rectangle dirtyBounds = GUIConsoleOutput._drawLine(m, graphics, new GUIConsoleOutput.LineBuffer(str), null, newLine, 0, draw);
        if (revertLastOutputPX) {
            m.nextOutputPX.setLocation(nextOutputPX);
        }
        if (graphics instanceof Graphics2D) {
            Graphics2D g = (Graphics2D) graphics;
            g.setColor(c);
            g.translate(-bounds.x, -bounds.y);
        } else {
            GL11.glColor4f(currentColor.get(0), currentColor.get(1), currentColor.get(2), currentColor.get(3));
            GLHandler._transformEnd();
        }
        return dirtyBounds;
    }
    private Font font;

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    /**
     * @param inBoundsArea a rectangular area that fits in the HUD.bounds
     */
    public void setDirtyRegion(Rectangle inBoundsArea) {
        dirtyArea.clear();
        dirtyArea.push(inBoundsArea);
    }

    /**
     * make UNION addition of the current dirty area with the specified one
     */
    public void addDirtyRegion(Rectangle inBoundsArea) {
        dirtyAdd(inBoundsArea);
    }

    private void dirtyAdd(Rectangle region) {
        if (!(bounds.width >= region.getMaxX() && bounds.height >= region.getMaxY()) && DebugMap._getInstance().isDebugLevelEnabled(RenderingScene.DBUG_RENDER)) {
            System.out.println(JXAenvUtils.log("dirtyArea " + region.toString() + " not fully in bounds " + bounds.getSize(), JXAenvUtils.LVL.APP_WRN));
        }
        region = region.createIntersection(new Rectangle(bounds.getSize())).getBounds();
        dirtyArea.add(0, region);
    }

    @Override
    public RenderingScene getRenderingScene() {
        return scene;
    }

    @Override
    public void setRenderingScene(RenderingScene renderingScene) {
        this.scene = renderingScene;
    }

    @Override
    public void setHardwareAccel(boolean b) {
    }

    @Override
    public boolean isHardwareAccel() {
        return GLHandler.ext.PixelBufferObjects.GL_isAvailable();
    }

    /**
     * does nothing
     */
    @Override
    public Object loadResource() {
        return null;
    }

    /**
     * clears and release {@link #glPBO}, {@link #resetContentsToDirty()}<br>
     * simply resets all ready to redraw.
     */
    @Override
    public Object clearResource() {
        resetContentsToDirty();
        if (glPBO != null) {
            glPBO.GLdispose();
        }
        return glPBO;
    }

    @Override
    public boolean isResourceLoaded() {
        return glPBO != null;
    }

    @Override
    public void contentsLost() {
        resetContentsToDirty();
    }
}
