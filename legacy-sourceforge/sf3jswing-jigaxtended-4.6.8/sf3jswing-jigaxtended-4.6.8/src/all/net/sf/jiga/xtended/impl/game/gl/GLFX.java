/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.game.gl;

import net.sf.jiga.xtended.impl.system.ThreadBlock;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.lang.reflect.InvocationTargetException;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;
import org.eclipse.imagen.PerspectiveTransform;
import net.sf.jiga.xtended.impl.Sprite;
import net.sf.jiga.xtended.impl.game.RenderingScene;
import net.sf.jiga.xtended.impl.game.gl.geom.GLGeom;
import net.sf.jiga.xtended.impl.system.BufferIO;
import net.sf.jiga.xtended.kernel.BitStack;
import net.sf.jiga.xtended.kernel.DebugMap;
import net.sf.jiga.xtended.kernel.Debugger;
import net.sf.jiga.xtended.kernel.JXAenvUtils;
import net.sf.jiga.xtended.kernel.Level;
import net.sf.jiga.xtended.kernel.SpritesCacheManager;
import net.sf.jiga.xtended.ui.Ant;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.Util;

/**
 *
 * @author www.b23prodtm.info
 */
public abstract class GLFX implements Debugger {

    static {
        DebugMap._getInstance().associateDebugLevel(GLFX.class, RenderingScene.DBUG_RENDER);
    }

    public void setDebugEnabled(boolean bln) {
        DebugMap._getInstance().setDebuggerEnabled(bln, GLFX.class, RenderingScene.DBUG_RENDER);
    }

    public boolean isDebugEnabled() {
        return DebugMap._getInstance().isDebuggerEnabled(GLFX.class);
    }
    public static boolean disableDepthReflexionFXplane = false;
    public static Color reflexionFXplaneColor = Color.BLACK, currentReflexionFXPlaneColor = reflexionFXplaneColor;
    /**
     * DO NOT MODIFY, it is for internal callback
     */
    public static final Stack<Map<String, Object>> _GLFXStack = new Stack<Map<String, Object>>();
    private static Vector<Map<Integer, Point>> reflexionPlanesDone = new Vector<Map<Integer, Point>>();
    /**
     * the BitStack for storing options bits (internal use)
     */
    public static BitStack bits = new BitStack(360);

    /**
     * the Sprite GRAPHICS FX option bits that are available for rendering
     */
    public static enum gfx {

        /**
         * gfx parent options bit {@linkplain #gfx()}
         */
        _FX(),
        /**
         * gfx parent orientation bit {@linkplain #gfx()}
         */
        _FX_ORIENT(),
        /**
         * gfx parent extensions bit {@linkplain #gfx()}
         */
        _FX_EXT(),
        /**
         * gfx parent pass bit {@linkplain #gfx()}
         */
        _FX_PASS(),
        /**
         * HORIZONTAL Constant used with FX_REFLEXION, FX_SCROLLING,
         * FX_BACKGROUND
         */
        FX_HORIZONTAL(_FX_ORIENT),
        /**
         * VERTICAL Constant used with FX_REFLEXION, FX_SCROLLING, FX_BACKGROUND
         */
        FX_VERTICAL(_FX_ORIENT),
        /**
         * the repeat FX used with FX_BACKGROUND
         */
        FX_REPEAT(_FX_EXT),
        /**
         * the backgroung FX add FX_REPEAT, FX_HORIZONTAL, FX_VERTICAL and/or
         * FX_SCROLLING
         */
        FX_BACKGROUND(_FX),
        /**
         * scrolling fx add FX_HORIZONTAL and/or FX_VERTICAL
         */
        FX_SCROLLING(_FX),
        /**
         * reflexion fx add FX_TOP, FX_LEFT, FX_BOTTOM or FX_RIGHT Point defines
         * a top-, left-, bottom- or right-plane to reflect on it
         */
        FX_REFLEXION(_FX),
        /**
         * the rotation FX it can be OR-ed with an integer value in the degrees
         * range [1, 360] to specify a different radial speed (degrees/sec)
         */
        FX_ROTATION(_FX),
        /**
         * the shadow FX add FX_TOP, FX_LEFT, FX_BOTTOM or FX_RIGHT Point
         * defines a top-, left-, bottom- or right-plane to map the shadow on it
         */
        FX_SHADOW(_FX),
        /**
         * the TOP-side FX used with FX_REFLEXION, FX_SHADOW
         */
        FX_TOP(_FX_EXT),
        /**
         * the BOTTOM-side FX used with FX_REFLEXION, FX_SHADOW
         */
        FX_BOTTOM(_FX_EXT),
        /**
         * the RIGHT-side FX used with FX_REFLEXION, FX_SHADOW
         */
        FX_RIGHT(_FX_EXT),
        /**
         * the LEFT-side FX used with FX_REFLEXION, FX_SHADOW
         */
        FX_LEFT(_FX_EXT),
        /**
         * 1st pass before rendering (don't use it, it is managed internally)
         */
        FX_1ST_PASS(_FX_PASS),
        /**
         * 2nd pass before rendering (don't use it, it is managed internally)
         */
        FX_2ND_PASS(_FX_PASS);
        /**
         * reflexion fx on the bottom side
         */
        public final static int FX_DOWNREFLEXION_EXT = FX_REFLEXION.bit() | FX_BOTTOM.bit();
        /**
         * background repeat fx
         */
        public final static int FX_BACKGROUNDREPEAT_EXT = FX_BACKGROUND.bit() | FX_REPEAT.bit();
        /**
         * the no fx
         */
        public final static int FX_NONE = 0;
        /**
         * associated bit(s), field
         */
        int bit;
        
        /** Graphics FX debugger */
        public static Level DBUG = DebugMap._getInstance().newDebugLevel();
        
        public boolean isDebugEnabled() {
            return DebugMap._getInstance().isDebugLevelEnabled(DBUG) || Boolean.getBoolean(Ant.JXA_DEBUG_GFX);
        }
        
        
        gfx(gfx parentOption) {
            bit = bits._newBit(parentOption.bit);
        }

        gfx() {
            bit = bits._newBitRange();
        }

        /**
         * fx fields associated with this bit
         */
        public int bitRangeFields() {
            return bits._getMask(bit);
        }

        /**
         * fx field value
         */
        public int bit() {
            return bit;
        }
        
    }

    /**
     * call a rendering method when all fx are finished so that it doesn't
     * overlaps 1st and 2nd passes fx rendering <b>PLEASE USE THIS METHOD
     * whenever you want to do personalized GL rendering (e.g. Stencil passes,
     * Shaders, etc.) not available in the current JXA _GL_ methods ! <i>To do
     * so, encapsulate your GL11..12. calls into a method that you call with
     * _GLpushRender and it will be added onto the rendering stack
     * (First-in-First-Out). It is recommended to encapsulate in the method code
     * block JXA _GL*()-prefixed functions like GLGeom._draw,fill, etc.</i></b>
     * NOTICE : your encapsulation must be declared "public" otherwise the
     * Reflexion API cannot access the method you want to invoke.<br>
     * <pre>public void myPublicMethod1(Object arg1, String arg2){
     *   GL11.glBegin();
     *   ...
     *   GL11.glEnd();
     *   GL...;
     * </pre> // n.b. any encaspulated _GLpushRender() will render immediately,
     * as if beginRenderBlock() was called. <br> // the following _GLfillRect()
     * method internally calls _GLpushRender(), as all _GL*()-prefixed methods
     * do
     * <pre>
     *   GLGeom._GLfillRect(...);
     * }
     * </pre> // the actual call in the program will be run like that (declare
     * another new encaspulating method for more flexibility) :<br> // example
     * for GameActionLayer renderToGL usage with the RenderingSceneGL
     * <pre>public class TestGameActionLayer extends GameActionLayer { protected void renderToGL(RenderingSceneGL glAutoDrawable) {
     * double z = 0.1 * (glAutoDrawable.getZDepth());
     * </pre>// normal callback to JXA GL render function<pre>
     * RenderingSceneGL._GLdrawLogo(...);
     * </pre>// encaspulated callback to a customized GL rendering block (Java Reflection API alike)<pre>_GLpushRender("myPublicMethod1", object OR clazz.class for any static called function, new Object[]{arg1,arg2}, new Class[]{Object.class,String.class}, stencilBuffer, true); </pre><pre>
     * }}
     * applet.add(glAutoDrawable, BorderLayout.CENTER);
     * glAutoDrawable.setVisible(true);
     * applet.validate();
     * glAutoDrawable.startRendering();
     * glAutoDrawable.addActionOffscreen(new TestGameActionLayer());
     * </pre> NOTE : in the _GLpushRender stack, all GLFX effects are disabled.
     */
    public static void _GLpushRender(String method, Object targetArg, Object[] objectArgs, Class[] classArgs) throws Exception {
        _GLpushRender(method, targetArg, objectArgs, classArgs, false);
    }

    private static void _GLpushRender(String method, Object targetArg, Object[] objectArgs, Class[] classArgs, boolean now) throws Exception {
        _GLpushRender(RenderingSceneGL._GL_getTopOftheStackMX(GL11.GL_PROJECTION_MATRIX), RenderingSceneGL._GL_getTopOftheStackMX(GL11.GL_MODELVIEW_MATRIX), GLHandler._GLgetCurrentColor(), DebugMap._getInstance().isDebuggerEnabled(GLFX.class) ? Thread.currentThread().getStackTrace() : new StackTraceElement[]{}, method, targetArg, objectArgs, classArgs, now);
    }

    /**
     * if now is false, can call a rendering method when all fx are finished so
     * that it doesn't overlaps 1st and 2nd passes fx rendering <i>To do so,
     * encapsulate your GL... calls into a method that you call with
     * _GLpushRender and it will be added on the rendering stack.</i></b> NOTICE
     * : your encapsulation must be declared "public" ortherwise the Reflexion
     * API cannot access the method you want to invoke. CAUTION : if another
     * _GLpushRender callback is encapsulated, please temporarily {@link #_def_disableFX disable FX}
     * before it.
     *
     * @param now will render immediatelly, assuming it's in the correct pass
     * @param stencilBuffer portion of the screen that has to be rendered
     */
    private static void _GLpushRender(FloatBuffer projMX, FloatBuffer modelViewMX, FloatBuffer currentColor, StackTraceElement[] stackTraceDump, String method, Object targetArg, Object[] objectArgs, Class[] classArgs, boolean now) throws Exception {
        assert targetArg != null && method != null : "either method or target cannot be null";
        /**
         * handling multi-pass rendering
         */
        if (objectArgs == null) {
            objectArgs = new Object[]{};
        }
        if (classArgs == null) {
            classArgs = new Class[]{};
        }
        if (!now && !isNoFXBlock() && !isRenderBlock()) {
            /**
             * store current state of the scene
             */
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("CB_objects", objectArgs);
            map.put("CB_classes", classArgs);
            map.put("CB_method", method);
            map.put("CB_target", targetArg);
            map.put("CB_modelViewMX", modelViewMX);
            map.put("CB_colorBuffer", currentColor);
            map.put("CB_stackTraceDump", stackTraceDump);
            map.put("CB_projMX", projMX);
            _GLFXStack.add(0, map);
        } else {
            beginRenderBlock();
            /**
             * try to restore render state; no glpush and glpop() to void stack
             * overflow of the matrix stack if running this recursively
             */
            FloatBuffer mProj = RenderingSceneGL._GL_getTopOftheStackMX(GL11.GL_PROJECTION_MATRIX);
            FloatBuffer mView = RenderingSceneGL._GL_getTopOftheStackMX(GL11.GL_MODELVIEW_MATRIX);
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glLoadMatrix(projMX);
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glLoadMatrix(modelViewMX);
            FloatBuffer colorPush = GLHandler._GLgetCurrentColor();
            GL11.glColor4f(currentColor.get(0), currentColor.get(1), currentColor.get(2), currentColor.get(3));
            Exception e = null;
            try {
                SpritesCacheManager.callback(method, targetArg, objectArgs, classArgs);
            } catch (NoSuchMethodException ex) {
                if (DebugMap._getInstance().isDebuggerEnabled(GLFX.class)) {
                    ex.printStackTrace();
                }
            } catch (IllegalAccessException ex) {
                if (DebugMap._getInstance().isDebuggerEnabled(GLFX.class)) {
                    ex.printStackTrace();
                }
            } catch (InvocationTargetException ex) {
                if (DebugMap._getInstance().isDebuggerEnabled(GLFX.class)) {
                    ex.printStackTrace();
                }
                e = ex;
            } finally {
                GL11.glColor4f(colorPush.get(0), colorPush.get(1), colorPush.get(2), colorPush.get(3));
                GL11.glMatrixMode(GL11.GL_PROJECTION);
                GL11.glLoadMatrix(mProj);
                GL11.glMatrixMode(GL11.GL_MODELVIEW);
                GL11.glLoadMatrix(mView);
                endRenderBlock();
                if (e != null) {
                    throw new Exception("Exception caught while calling back method " + method + " in " + (stackTraceDump.length > 3 ? stackTraceDump[4].toString() : " ") + " < " + (stackTraceDump.length > 2 ? stackTraceDump[3].toString() : " ") + " < " + (stackTraceDump.length > 1 ? stackTraceDump[2].toString() : ""), e.getCause());
                }
            }
        }
    }
    Map<String, Object> mapRender = new HashMap<String, Object>();

    /**
     * renders the GL2DObject within an openGL context
     *
     * @param gld the RenderingSceneGL that is to be rendered onto
     * @param keepBinding keeps the associated texture for further use or not,
     * resp.
     * @param sp the GL2DObject to render
     * @param z z-depth to render at
     * @param fx the gfx to apply or 0 for none
     * @param fx_loc x,y Point location of the gfx, or Point(0,0)
     * @param fx_color Color for the gfx, or null
     * @param transform the transform bitwise-OR combination to apply or 0 for
     * none {@linkplain RenderingSceneGL#_GL_TRANSFORM_ROTATE}, {@linkplain RenderingSceneGL#_GL_TRANSFORM_SCALE}, {@linkplain RenderingSceneGL#_GL_TRANSFORM_TRANSLATE}
     * @param scale double array of scaling args (scaleX, scaleY) or null
     * @param rotate double array of rotation arg (rotateDEGREES) or null
     * @param translate double array of translation args (translateX,
     * translateY) or null
     */
    public void _GLpushRender2DObject(RenderingSceneGL gld, boolean keepBinding, GL2DObject sp, double z, int fx, Point fx_loc, Color fx_color, int transform, DoubleBuffer scale, DoubleBuffer rotate, DoubleBuffer translate) {
        __GLpushRender2DObject(gld, keepBinding, sp, z, fx, fx_loc, fx_color, transform, scale, rotate, translate);
    }

    /**
     * push FX PASSES IF NEEDED AND RENDER ONCE
     */
    private void __GLpushRender2DObject(RenderingSceneGL gld, boolean keepBinding, GL2DObject sp, double z, int fx, Point fx_loc, Color fx_color, int transform, DoubleBuffer scale, DoubleBuffer rotate, DoubleBuffer translate) {
        fx &= ~bits._getAllBitRanges() | ~gfx._FX_PASS.bitRangeFields();
        if (isNoFXBlock()) {
            fx = gfx.FX_NONE;
        }
        if (fx_loc == null) {
            fx_loc = new Point(0, 0);
        }
        mapRender.clear();
        mapRender.put("sp", sp);
        mapRender.put("gld", gld);
        mapRender.put("z", z);
        mapRender.put("fx_loc", fx_loc.clone());
        mapRender.put("fx_color", fx_color);
        mapRender.put("transform", transform);
        mapRender.put("scale", scale);
        mapRender.put("rotate", rotate);
        mapRender.put("translate", translate);
        /**
         * Push 2nd pass, force keep binding of textures if any
         */
        if (fx != gfx.FX_NONE) {
            mapRender.put("keepBinding", true);
            mapRender.put("fx", fx | gfx.FX_2ND_PASS.bit());
            try {
                /**
                 * PASS �2
                 */
                _GLpushRender("__GLRender2DFX", this, new Object[]{mapRender.get("gld"), mapRender.get("keepBinding"), mapRender.get("sp"), mapRender.get("z"), mapRender.get("fx"), mapRender.get("fx_loc"), mapRender.get("fx_color"), mapRender.get("transform"), mapRender.get("scale"), mapRender.get("rotate"), mapRender.get("translate")}, new Class[]{RenderingSceneGL.class, boolean.class, GL2DObject.class, double.class, int.class, Point.class, Color.class, int.class, DoubleBuffer.class, DoubleBuffer.class, DoubleBuffer.class});
            } catch (Exception ex) {
                if (isDebugEnabled()) {
                    ex.printStackTrace();
                }
            }
        }
        /**
         * Push normal (3rd) pass, textures can be flushed if specified
         */
        try {
            /**
             * PASS �3 or Unique PASS if fx == NONE (but it is done later unless
             * it is in a begin-endRenderBlock.)
             */
            mapRender.put("keepBinding", keepBinding);
            mapRender.put("fx", fx);
            _GLpushRender("__GLRender2DFX", this, new Object[]{mapRender.get("gld"), mapRender.get("keepBinding"), mapRender.get("sp"), mapRender.get("z"), mapRender.get("fx"), mapRender.get("fx_loc"), mapRender.get("fx_color"), mapRender.get("transform"), mapRender.get("scale"), mapRender.get("rotate"), mapRender.get("translate")}, new Class[]{RenderingSceneGL.class, boolean.class, GL2DObject.class, double.class, int.class, Point.class, Color.class, int.class, DoubleBuffer.class, DoubleBuffer.class, DoubleBuffer.class});
            if (fx != gfx.FX_NONE) {
                /**
                 * PASS �1 Do 1st pass NOW, force keep binding of textures if
                 * any
                 */
                mapRender.put("keepBinding", keepBinding);
                mapRender.put("fx", fx | gfx.FX_1ST_PASS.bit());
                _GLpushRender("__GLRender2DFX", this, new Object[]{mapRender.get("gld"), mapRender.get("keepBinding"), mapRender.get("sp"), mapRender.get("z"), mapRender.get("fx"), mapRender.get("fx_loc"), mapRender.get("fx_color"), mapRender.get("transform"), mapRender.get("scale"), mapRender.get("rotate"), mapRender.get("translate")}, new Class[]{RenderingSceneGL.class, boolean.class, GL2DObject.class, double.class, int.class, Point.class, Color.class, int.class, DoubleBuffer.class, DoubleBuffer.class, DoubleBuffer.class}, true);
            }
        } catch (Exception ex) {
            if (isDebugEnabled()) {
                ex.printStackTrace();
            }
        } finally {
            mapRender.clear();
        }
    }
    private static ThreadBlock _isRenderBlock = new ThreadBlock();

    /**
     * threadlocal state
     *
     * @see #beginRenderBlock()
     */
    public static boolean isRenderBlock() {
        return _isRenderBlock.isOn();
    }

    /**
     * threadlocal state <br>any encaspulated {@link #_GLpushRender(java.lang.String, java.lang.Object, java.lang.Object[], java.lang.Class[])}
     * will render immediately till the next {@link #endRenderBlock()}
     *
     * @see #endRenderBlock()
     */
    public static void beginRenderBlock() {
        _isRenderBlock.begin();
    }

    /**
     * threadlocal state
     *
     * @see #beginRenderBlock()
     */
    public static void endRenderBlock() {
        _isRenderBlock.end();
    }
    public final static boolean _def_disableFX = Boolean.parseBoolean(RenderingScene.rb.getString("_GLdisableFX")) || Boolean.parseBoolean(System.getProperty("jxa.nofx"));
    private static ThreadBlock _disableFX = new ThreadBlock(_def_disableFX);

    public static void disableFX() {
        _disableFX = new ThreadBlock(true);
    }

    /**
     * if FX were globally {@link #disableFX() disabled}, it always returns
     * true. equivalent to {@link #isNoFXBlock()}
     */
    public static boolean isFXdisabled() {
        return _disableFX.isOn();
    }

    public static void enableFX() {
        _disableFX = new ThreadBlock(false);
    }

    /**
     * threadlocal state <br> system property -Djxa.nofx=true overrides this and
     * disables all effects at runtime
     *
     * @see #beginNoFXBlock()
     */
    public static boolean isNoFXBlock() {
        return _disableFX.isOn();
    }

    /**
     * threadlocal state disables graphics effects (FX) so that all rendering is
     * done in one pass _GLpushRender will render immediately and 1st and 2nd
     * passes FX are discarded <b>on the current thread</b> until {@link #endNoFXBlock()
     * } is invoked.<br> system property -Djxa.nofx=true overrides this and
     * disables all effects at runtime
     *
     * @see #endNoFXBlock()
     */
    public static void beginNoFXBlock() {
        _disableFX.begin();
    }

    /**
     * threadlocal state <br> system property -Djxa.nofx=true overrides this and
     * disables all effects at runtime
     *
     * @see #beginNoFXBlock()
     */
    public static void endNoFXBlock() {
        _disableFX.end();
    }

    /**
     * buffers and begins gathering {@linkplain #_GLpushRender2DObject(RenderingSceneGL, GL2DObject, double, int, Point, Color, FloatBuffer)}
     * and {@linkplain #_GLpushRender2DObject(RenderingSceneGL, GL2DObject, int, Point, Color, FloatBuffer)}
     * if using the RenderingSceneGL, you should NOT use this method, since it is
     * reserved internally to gather all rendering in multi-pass.
     *
     * @see #_GLEndFX()
     * @param clearFXPlanesDone set to true before the first fx is rendered, it
     * is set to false to prevent clearing planes from one begin-endFX block to
     * the other.
     */
    public static void _GLBeginFX(boolean clearFXPlanesDone) {
        if (!_GLFXStack.empty()) {
            if (DebugMap._getInstance().isDebuggerEnabled(GLFX.class)) {
                System.err.println(JXAenvUtils.log("WARNING : FX stack wasn't empty (" + _GLFXStack.size() + "), the stack is cleared so that previous fx are garbaged. _GLEndFX might not have been called. listing stack content; rendering order is reversed :", JXAenvUtils.LVL.SYS_WRN));
                for (Map<String, Object> m : _GLFXStack) {
                    System.err.print("method name " + m.get("CB_method") + " at ");
                    int max = 6;
                    StackTraceElement[] st = (StackTraceElement[]) m.get("CB_stackTraceDump");
                    for (int i = 1; i < max && i < st.length; i++) {
                        System.err.print(st[i].getClassName() + "//" + st[i].getMethodName() + " < ");
                        System.err.println();
                    }
                }
            }
            _GLFXStack.clear();
            if (clearFXPlanesDone) {
                reflexionPlanesDone.clear();
            }
        }
    }
    /**
     * @see #_GLEndFX()
     */
    private static Stack<Map<String, Object>> fxPass2 = new Stack<Map<String, Object>>(), fxPassFinal = new Stack<Map<String, Object>>();

    /**
     * flushes and terminates rendering of {@linkplain #_GLpushRender2DObject(RenderingSceneGL, GL2DObject, double, int, Point, Color, FloatBuffer)}
     * and {@linkplain #_GLpushRender2DObject(RenderingSceneGL, GL2DObject, int, Point, Color, FloatBuffer)}
     * if using the RenderingSceneGL, you should NOT use this method, since it is
     * reserved internally to gather all rendering in multi-pass.
     *
     * @see #_GLBeginFX()
     */
    public static void _GLEndFX() {
        fxPass2.clear();
        fxPassFinal.clear();
        Map<String, Object> map = null;
        try {
            synchronized (_GLFXStack) {
                while (!_GLFXStack.empty()) {
                    map = _GLFXStack.pop();
                    /**
                     * sort fx pass rendering
                     */
                    if (map.get("CB_method").equals("__GLRender2DFX")) {
                        Object[] args = ((Object[]) map.get("CB_objects"));
                        int fx = (Integer) args[4];
                        fx &= bits._getAllBitRanges();
                        if ((fx & gfx.FX_2ND_PASS.bit()) != 0) {
                            fxPass2.add(0, map);
                        } else {
                            fxPassFinal.add(0, map);
                        }
                    } else {
                        fxPassFinal.add(0, map);
                    }
                }
            }
            while (!fxPass2.empty()) {
                map = fxPass2.pop();
                _GLpushRender((FloatBuffer) map.get("CB_projMX"), (FloatBuffer) map.get("CB_modelViewMX"), (FloatBuffer) map.get("CB_colorBuffer"), (StackTraceElement[]) map.get("CB_stackTraceDump"), (String) map.get("CB_method"), map.get("CB_target"), (Object[]) map.get("CB_objects"), (Class[]) map.get("CB_classes"), true);
            }
            while (!fxPassFinal.empty()) {
                map = fxPassFinal.pop();
                _GLpushRender((FloatBuffer) map.get("CB_projMX"), (FloatBuffer) map.get("CB_modelViewMX"), (FloatBuffer) map.get("CB_colorBuffer"), (StackTraceElement[]) map.get("CB_stackTraceDump"), (String) map.get("CB_method"), map.get("CB_target"), (Object[]) map.get("CB_objects"), (Class[]) map.get("CB_classes"), true);
            }
        } catch (Exception ex) {
            if (DebugMap._getInstance().isDebuggerEnabled(GLFX.class)) {
                Vector<StackTraceElement> st = new Vector<StackTraceElement>();
                for (StackTraceElement el : ex.getStackTrace()) {
                    st.add(el);
                }
                for (StackTraceElement el : (StackTraceElement[]) map.get("CB_stackTraceDump")) {
                    st.add(el);
                }
                ex.setStackTrace(st.toArray(new StackTraceElement[]{}));
                ex.printStackTrace();
            }
        }
    }
    /**
     * angular speed rad/msec for the PerspectiveTransform effect
     */
    public float FX_overallRADIALSPEED = (float) Math.PI / (2f * 1000f);
    /**
     * speed px/msec for FX
     */
    public float FX_overallSPEED = 30f / 1000f;
    /**
     * map for the last scrolling transform matrixes
     */
    public final Map<Integer, AffineTransform> FX_lastTransformScrolling = Collections.synchronizedMap(new HashMap<Integer, AffineTransform>());
    /**
     * map for the last transform timestamps
     */
    public final Map<Integer, Long> FX_lastTransformTime = Collections.synchronizedMap(new HashMap<Integer, Long>());
    /**
     * map for the last transform angular values
     */
    public final Map<Integer, Float> FX_lastTransformTheta = Collections.synchronizedMap(new HashMap<Integer, Float>());
    private final static ThreadBlock _fxPushBlock = new ThreadBlock();

    /**
     * WARNING : DO NOT USE, this is for internal callback ! in {@link #_GLpushRender2DObject(net.sf.jiga.xtended.impl.game.RenderingSceneGL, boolean, net.sf.jiga.xtended.impl.game.gl.GL2DObject, double, int, java.awt.Point, java.awt.Color, int, java.nio.DoubleBuffer, java.nio.DoubleBuffer, java.nio.DoubleBuffer)
     * }
     *
     * @param gld
     * @param keepBinding
     * @param z
     * @param fx
     * @param fx_loc
     * @param fx_color
     * @param transform
     * @param scale
     * @param rotate
     * @param translate
     */
    public void __GLRender2DFX(RenderingSceneGL gld, boolean keepBinding, final GL2DObject mySp, double z, int fx, Point fx_loc, Color fx_color, int transform, DoubleBuffer scale, DoubleBuffer rotate, DoubleBuffer translate) {
        /**
         * METHOD MEMBER VARIABLES
         */
        int pty = Thread.currentThread().getPriority();
        final Rectangle texBounds = mySp.getShape().getBounds();
        AffineTransform fx_tx = new AffineTransform();
        AffineTransform fx_tx_mirror = new AffineTransform();
        if (scale != null && (transform & GLHandler._GL_TRANSFORM_SCALE_BIT) != 0) {
            if (isDebugEnabled()) {
                System.err.println("scale " + scale.get(0) + ", " + scale.get(1));
            }
        } else {
            scale = BufferIO._wrapd(new double[]{1, 1});
        }
        if ((transform & GLHandler._GL_TRANSFORM_TRANSLATE_BIT) != 0 && translate != null) {
            if (isDebugEnabled()) {
                System.err.println("translate " + translate.get(0) + ", " + translate.get(1));
            }
        } else {
            translate = BufferIO._wrapd(new double[]{0, 0});
        }
        DoubleBuffer eqr = null, eqEye = null;
        DoubleBuffer rotationFX = null, rotationFX_mirror = null;
        if ((transform & GLHandler._GL_TRANSFORM_ROTATE_BIT) != 0 && rotate != null) {
            rotate = BufferIO._wrapd(new double[]{rotate.get(0), 0, 0});
            if (isDebugEnabled()) {
                System.err.println("rotate " + rotate.get(0));
            }
        } else {
            rotate = BufferIO._wrapd(new double[]{0, 0, 0});
        }
        rotationFX = rotationFX_mirror = rotate;
        FloatBuffer colorBlend = fx_color != null ? BufferIO._wrapf(fx_color.getRGBComponents(new float[4])) : null;
        FloatBuffer alphaBlend = BufferIO._wrapf(new float[]{1, 1, 1, 1});
        long now = System.currentTimeMillis();
        final Rectangle fxBounds = texBounds.getBounds();
        Rectangle backgroundClip = new Rectangle(Math.round((float) gld._GLZRatio(z) * gld.getWidth()), Math.round((float) gld._GLZRatio(z) * gld.getHeight()));
        backgroundClip.setLocation(-Math.round(.5f * (backgroundClip.width - gld.getWidth())), -Math.round(.5f * (backgroundClip.height - gld.getHeight())));
        /**
         * retrieve all fx enum fields
         */
        fx &= gfx._FX.bitRangeFields() | gfx._FX_EXT.bitRangeFields() | gfx._FX_ORIENT.bitRangeFields() | gfx._FX_PASS.bitRangeFields();
        /*
         *
         */
        if (fx != gfx.FX_NONE) {
            if (isDebugEnabled()) {
                System.err.print("GLFX ");
                for (gfx select : gfx.values()) {
                    if ((select.bit() & fx) != 0) {
                        System.err.print("fx " + select.toString());
                    }
                }
                System.err.println();
            }
        }
        /**
         * OVERALL FX SETTINGS SCROLLING
         */
        if ((fx & gfx.FX_SCROLLING.bit()) != 0) {
            if (!(FX_lastTransformScrolling.get(mySp.getUID()) instanceof AffineTransform)) {
                FX_lastTransformScrolling.put(mySp.getUID(), new AffineTransform());
            }
            if (!(FX_lastTransformTime.get(mySp.getUID()) instanceof Long)) {
                FX_lastTransformTime.put(mySp.getUID(), now);
            }
            double translateH, translateV;
            translateH = (fx & gfx.FX_HORIZONTAL.bit()) != 0 ? FX_overallSPEED * (now - FX_lastTransformTime.get(mySp.getUID())) : 0f;
            translateV = (fx & gfx.FX_VERTICAL.bit()) != 0 ? FX_overallSPEED * (now - FX_lastTransformTime.get(mySp.getUID())) : 0f;
            FX_lastTransformScrolling.get(mySp.getUID()).setToTranslation(-((Math.abs(FX_lastTransformScrolling.get(mySp.getUID()).getTranslateX()) + translateH) % -texBounds.width), -((Math.abs(FX_lastTransformScrolling.get(mySp.getUID()).getTranslateY()) + translateV) % texBounds.height));
            fx_tx.preConcatenate(FX_lastTransformScrolling.get(mySp.getUID()));
            fx_tx_mirror.preConcatenate(FX_lastTransformScrolling.get(mySp.getUID()));
            FX_lastTransformTime.put(mySp.getUID(), now);
        }
        /**
         * OVERALL FX SETTINGS ROTATION
         */
        if ((fx & gfx.FX_ROTATION.bit()) != 0) {
            if (!(FX_lastTransformTime.get(mySp.getUID()) instanceof Long)) {
                FX_lastTransformTime.put(mySp.getUID(), now);
            }
            float lastTransformTheta = FX_lastTransformTheta.containsKey(mySp.getUID()) ? FX_lastTransformTheta.get(mySp.getUID()) : (1f - 1f / 360f) * 360f;
            float rotationSpeed = FX_overallRADIALSPEED * 360f / (2f * (float) Math.PI);
            if ((fx - (fx & (bits._getAllBits() | bits._getAllBitRanges()))) != 0) {
                rotationSpeed = fx - (fx & (bits._getAllBits() | bits._getAllBitRanges()));
                rotationSpeed /= 1000f;
            }
            lastTransformTheta -= (float) (now - FX_lastTransformTime.get(mySp.getUID())) * rotationSpeed;
            lastTransformTheta = lastTransformTheta % 360f;
            FX_lastTransformTheta.put(mySp.getUID(), lastTransformTheta);
            FX_lastTransformTime.put(mySp.getUID(), now);
            rotationFX = BufferIO._wrapd(new double[]{lastTransformTheta, texBounds.getCenterX(), texBounds.getCenterY()});
            rotationFX_mirror = rotationFX;
        }

        /**
         * OVERALL FX SETTINGS REFLEXION
         */
        if ((fx & gfx.FX_REFLEXION.bit()) != 0) {
            assert (fx & (gfx.FX_TOP.bit() | gfx.FX_BOTTOM.bit() | gfx.FX_LEFT.bit() | gfx.FX_RIGHT.bit())) != 0 : "no orientation defined FX_BOTTOM, TOP etc.";

            if ((fx & (gfx.FX_1ST_PASS.bit() | gfx.FX_2ND_PASS.bit())) != 0) {
                if ((fx & (gfx.FX_TOP.bit() | gfx.FX_BOTTOM.bit())) != 0) {
                    fx_tx_mirror.scale(1., -1.);
                    /*
                     * Clip Plane Equations
                     */
                    if ((fx & gfx.FX_BOTTOM.bit()) != 0) {
                        eqr = BufferIO._wrapd(new double[]{0.0f, 1, 0.0f, -(fx_loc.y)});
                        eqEye = RenderingSceneGL._GL_getLookAt_plane(gld.getSize(), gld.getZDepth(), BufferIO._wrapf(new float[]{0, 1, 0}));
                    } else {
                        eqr = BufferIO._wrapd(new double[]{0.0f, -1, 0.0f, -(fx_loc.y)});
                        eqEye = RenderingSceneGL._GL_getLookAt_plane(gld.getSize(), gld.getZDepth(), BufferIO._wrapf(new float[]{0, -1, 0}));
                    }
                    fx_tx_mirror.translate(0, -2. * eqr.get(3));
                }
                if ((fx & (gfx.FX_LEFT.bit() | gfx.FX_RIGHT.bit())) != 0) {
                    fx_tx_mirror.scale(-1., 1.);
                    /*
                     * Clip Plane Equations
                     */
                    if ((fx & gfx.FX_RIGHT.bit()) != 0) {
                        eqr = BufferIO._wrapd(new double[]{1, 0f, 0.0f, -(fx_loc.x)});
                        eqEye = RenderingSceneGL._GL_getLookAt_plane(gld.getSize(), gld.getZDepth(), BufferIO._wrapf(new float[]{1, 0, 0}));
                    } else {
                        eqr = BufferIO._wrapd(new double[]{-1, 0f, 0.0f, -(fx_loc.x)});
                        eqEye = RenderingSceneGL._GL_getLookAt_plane(gld.getSize(), gld.getZDepth(), BufferIO._wrapf(new float[]{-1, 0, 0}));
                    }
                    fx_tx_mirror.translate(-2. * eqr.get(3), 0);
                }
            } else {
                if ((fx & (gfx.FX_TOP.bit() | gfx.FX_BOTTOM.bit())) != 0) {
                    /*
                     * Clip Plane Equations
                     */
                    if ((fx & gfx.FX_BOTTOM.bit()) != 0) {
                        eqr = BufferIO._wrapd(new double[]{0.0f, -1, 0.0f, (fx_loc.y)});
                    } else {
                        eqr = BufferIO._wrapd(new double[]{0.0f, 1, 0.0f, (fx_loc.y)});
                    }
                }
                if ((fx & (gfx.FX_LEFT.bit() | gfx.FX_RIGHT.bit())) != 0) {
                    /*
                     * Clip Plane Equations
                     */
                    if ((fx & gfx.FX_RIGHT.bit()) != 0) {
                        eqr = BufferIO._wrapd(new double[]{-1, 0f, 0.0f, (fx_loc.x)});
                    } else {
                        eqr = BufferIO._wrapd(new double[]{1, 0f, 0.0f, (fx_loc.x)});
                    }
                }
            }
            /**
             * FX SETTINGS SHADOW
             */
        } else if ((fx & gfx.FX_SHADOW.bit()) != 0) {
            assert (fx & (gfx.FX_TOP.bit() | gfx.FX_BOTTOM.bit() | gfx.FX_LEFT.bit() | gfx.FX_RIGHT.bit())) != 0 : "no orientation defined FX_BOTTOM, TOP etc.";
            if ((fx & (gfx.FX_TOP.bit() | gfx.FX_BOTTOM.bit())) != 0) {
                /*
                 * Clip Plane Equations
                 */
                if ((fx & gfx.FX_BOTTOM.bit()) != 0) {
                    eqr = BufferIO._wrapd(new double[]{0.0f, 1, 0.0f, -fx_loc.y});
                } else {
                    eqr = BufferIO._wrapd(new double[]{0.0f, -1, 0.0f, -fx_loc.y});
                }
            }
            if ((fx & (gfx.FX_LEFT.bit() | gfx.FX_RIGHT.bit())) != 0) {
                /*
                 * Clip Plane Equations
                 */
                if ((fx & gfx.FX_RIGHT.bit()) != 0) {
                    eqr = BufferIO._wrapd(new double[]{1, 0f, 0.0f, -fx_loc.x});
                } else {
                    eqr = BufferIO._wrapd(new double[]{-1, 0f, 0.0f, -fx_loc.x});
                }
            }
        }
        /**
         * FX GL BLOCK begin
         */
        boolean fxPushBlockInit = !_fxPushBlock.isOn();
        if (fxPushBlockInit) {
            _fxPushBlock.begin();
            GLHandler._GLpushAttrib(GL11.GL_ENABLE_BIT);
        }
        Util.checkGLError();
        /**
         * 1ST AND 2ND PASSES
         */
        if ((fx & (gfx.FX_1ST_PASS.bit() | gfx.FX_2ND_PASS.bit())) != 0) {
            if ((fx & gfx.FX_1ST_PASS.bit()) != 0) {
                if ((fx & gfx.FX_REFLEXION.bit()) != 0) {
                    GL11.glEnable(GL11.GL_CLIP_PLANE0);
                    GL11.glClipPlane(GL11.GL_CLIP_PLANE0, eqr);
                    GL11.glEnable(GL11.GL_CLIP_PLANE1);
                    GL11.glClipPlane(GL11.GL_CLIP_PLANE1, eqEye);
                    GL11.glDisable(GL11.GL_DEPTH_TEST);
                    Util.checkGLError();
                }
            }
            if ((fx & gfx.FX_2ND_PASS.bit()) != 0) {
                if ((fx & gfx.FX_SHADOW.bit()) != 0) {
                    GLHandler.stView.push();
                    /**
                     * column-major matrix to cast shadows onto the selected
                     * plane
                     */
                    DoubleBuffer shadowCasting = BufferIO._wrapd(new double[]{-eqr.get(3), 0, 0, eqr.get(0), 0, -eqr.get(3), 0, eqr.get(1), 0, 0, -eqr.get(3), eqr.get(2), 0, 0, 0, 0});
                    GL11.glMultMatrix(shadowCasting);
                    /**
                     * translate to make the light source 0,0,0,1
                     */
                    FloatBuffer lightPos = BufferIO._newf(4);
                    GL11.glGetLight(GL11.GL_LIGHT0, GL11.GL_POSITION, lightPos);
                    GL11.glTranslatef(-lightPos.get(0), -lightPos.get(1), -lightPos.get(2));
                    colorBlend = BufferIO._wrapf(Color.BLACK.getRGBColorComponents(null));
                }
                if ((fx & gfx.FX_REFLEXION.bit()) != 0) {
                    boolean doPlane = true;
                    int fxPlane = fx & (gfx.FX_BOTTOM.bit() | gfx.FX_TOP.bit() | gfx.FX_LEFT.bit() | gfx.FX_RIGHT.bit());
                    for (Map<Integer, Point> plane : reflexionPlanesDone) {
                        if (plane.containsKey(fxPlane)) {
                            if (fx_loc.equals(plane.get(fxPlane))) {
                                doPlane = false;
                            }
                            break;
                        }
                    }
                    /**
                     * render only once per reflexion plane
                     */
                    if (doPlane) {
                        reflexionPlanesDone.add(Collections.singletonMap(fxPlane, fx_loc));
                        GLHandler._GLpushAttrib(GL11.GL_LIGHTING_BIT | GL11.GL_ENABLE_BIT);
                        if (disableDepthReflexionFXplane) {
                            GL11.glDisable(GL11.GL_DEPTH_TEST);
                        }
                        GL11.glEnable(GL11.GL_LIGHTING);
                        if (isDebugEnabled() && !currentReflexionFXPlaneColor.equals(Color.ORANGE)) {
                            reflexionFXplaneColor = currentReflexionFXPlaneColor;
                            currentReflexionFXPlaneColor = Color.ORANGE;
                        } else if (!isDebugEnabled()) {
                            currentReflexionFXPlaneColor = reflexionFXplaneColor;
                        }
                        FloatBuffer color = BufferIO._wrapf(currentReflexionFXPlaneColor.getRGBComponents(null));
                        GL11.glMateriali(GL11.GL_FRONT_AND_BACK, GL11.GL_SHININESS, 10);
                        GLGeom._doPlane(gld, fx, eqr, BufferIO._wrapf(new float[]{.3f, .3f, .3f, .3f}), color);
                        GLHandler._GLpopAttrib();
                        Util.checkGLError();
                    }
                }
            }
            if (((fx & gfx.FX_REFLEXION.bit()) != 0 && (fx & gfx.FX_1ST_PASS.bit()) != 0) || ((fx & gfx.FX_SHADOW.bit()) != 0 && (fx & gfx.FX_2ND_PASS.bit()) != 0)) {
                if ((fx & gfx.FX_BACKGROUND.bit()) != 0 && (fx & gfx.FX_REPEAT.bit()) != 0) {
                    int endI = (int) Math.ceil((float) backgroundClip.getBounds().getWidth() / (float) texBounds.width);
                    int endJ = (int) Math.ceil((float) backgroundClip.getBounds().getHeight() / (float) texBounds.height);
                    if ((fx & gfx.FX_HORIZONTAL.bit()) == 0) {
                        backgroundClip.setLocation(texBounds.x, backgroundClip.y);
                    }
                    if ((fx & gfx.FX_VERTICAL.bit()) == 0) {
                        backgroundClip.setLocation(backgroundClip.x, texBounds.y);
                    }
                    for (int j = -1; j <= endJ; j++) {
                        for (int i = -1; i <= endI; i++) {
                            fxBounds.setLocation((int) backgroundClip.getBounds().getX() + ((fx & gfx.FX_HORIZONTAL.bit()) != 0 ? i : 0) * texBounds.width, (int) backgroundClip.getBounds().getY() + ((fx & gfx.FX_VERTICAL.bit()) != 0 ? j : 0) * texBounds.height);
                            if ((fx & gfx.FX_ROTATION.bit()) != 0 || (transform & GLHandler._GL_TRANSFORM_ROTATE_BIT) != 0) {
                                rotationFX_mirror.put(1, fxBounds.getCenterX());
                                rotationFX_mirror.put(2, fxBounds.getCenterY());
                            }
                            _GLRender2D(gld, newFXObject(mySp, fxBounds, fx), z, transform | GLHandler._GL_TRANSFORM_SCALE_BIT | GLHandler._GL_TRANSFORM_TRANSLATE_BIT | GLHandler._GL_TRANSFORM_ROTATE_BIT, BufferIO._wrapd(new double[]{fx_tx_mirror.getScaleX() * scale.get(0), fx_tx_mirror.getScaleY() * scale.get(1)}), rotationFX_mirror, BufferIO._wrapd(new double[]{fx_tx_mirror.getTranslateX() + translate.get(0), fx_tx_mirror.getTranslateY() + translate.get(1)}), colorBlend, alphaBlend);
                            if ((fx & gfx.FX_HORIZONTAL.bit()) == 0) {
                                break;
                            }
                        }
                        if ((fx & gfx.FX_VERTICAL.bit()) == 0) {
                            break;
                        }
                    }
                } else {
                    if ((fx & gfx.FX_ROTATION.bit()) != 0 || (transform & GLHandler._GL_TRANSFORM_ROTATE_BIT) != 0) {
                        rotationFX_mirror.put(1, fxBounds.getCenterX());
                        rotationFX_mirror.put(2, fxBounds.getCenterY());
                    }
                    _GLRender2D(gld, newFXObject(mySp, fxBounds, fx), z, transform | GLHandler._GL_TRANSFORM_SCALE_BIT | GLHandler._GL_TRANSFORM_TRANSLATE_BIT | GLHandler._GL_TRANSFORM_ROTATE_BIT, BufferIO._wrapd(new double[]{fx_tx_mirror.getScaleX() * scale.get(0), fx_tx_mirror.getScaleY() * scale.get(1)}), rotationFX_mirror, BufferIO._wrapd(new double[]{fx_tx_mirror.getTranslateX() + translate.get(0), fx_tx_mirror.getTranslateY() + translate.get(1)}), colorBlend, alphaBlend);
                }
            }
            if ((fx & gfx.FX_2ND_PASS.bit()) != 0) {
                if ((fx & gfx.FX_SHADOW.bit()) != 0) {
                    GLHandler.stView.pop();
                }
            }
        }
        /**
         * FINAL PASS
         */
        if ((fx & (gfx.FX_1ST_PASS.bit() | gfx.FX_2ND_PASS.bit())) == 0) {
            if ((fx & gfx.FX_REFLEXION.bit()) != 0) {
                GL11.glEnable(GL11.GL_CLIP_PLANE0);
                GL11.glClipPlane(GL11.GL_CLIP_PLANE0, eqr);
                Util.checkGLError();
            }
            if ((fx & gfx.FX_BACKGROUND.bit()) != 0 && (fx & gfx.FX_REPEAT.bit()) != 0) {
                int endI = (int) Math.ceil((float) backgroundClip.getBounds().getWidth() / (float) texBounds.width);
                int endJ = (int) Math.ceil((float) backgroundClip.getBounds().getHeight() / (float) texBounds.height);
                boolean bgKeepBinding = keepBinding;
                if ((fx & gfx.FX_HORIZONTAL.bit()) == 0) {
                    backgroundClip.setLocation(texBounds.x, backgroundClip.y);
                }
                if ((fx & gfx.FX_VERTICAL.bit()) == 0) {
                    backgroundClip.setLocation(backgroundClip.x, texBounds.y);
                }
                for (int j = -1; j <= endJ; j++) {
                    for (int i = -1; i <= endI; i++) {
                        texBounds.setLocation((int) backgroundClip.getBounds().getX() + ((fx & gfx.FX_HORIZONTAL.bit()) != 0 ? i : 0) * texBounds.width, (int) backgroundClip.getBounds().getY() + ((fx & gfx.FX_VERTICAL.bit()) != 0 ? j : 0) * texBounds.height);
                        keepBinding = (i == endI && j == endJ) ? bgKeepBinding : true;
                        if ((fx & gfx.FX_ROTATION.bit()) != 0 || (transform & GLHandler._GL_TRANSFORM_ROTATE_BIT) != 0) {
                            rotationFX.put(1, texBounds.getBounds().getCenterX());
                            rotationFX.put(2, texBounds.getBounds().getCenterY());
                        }
                        _GLRender2D(gld, keepBinding, newFXObject(mySp, texBounds, fx), z, transform | GLHandler._GL_TRANSFORM_TRANSLATE_BIT | GLHandler._GL_TRANSFORM_ROTATE_BIT, scale, rotationFX, BufferIO._wrapd(new double[]{fx_tx.getTranslateX() + translate.get(0), fx_tx.getTranslateY() + translate.get(1)}), colorBlend, alphaBlend);
                        texBounds.setLocation(texBounds.getBounds().getLocation());
                        if ((fx & gfx.FX_HORIZONTAL.bit()) == 0) {
                            break;
                        }
                    }
                    if ((fx & gfx.FX_VERTICAL.bit()) == 0) {
                        break;
                    }
                }
            } else {
                if ((fx & gfx.FX_ROTATION.bit()) != 0 || (transform & GLHandler._GL_TRANSFORM_ROTATE_BIT) != 0) {
                    rotationFX.put(1, texBounds.getBounds().getCenterX());
                    rotationFX.put(2, texBounds.getBounds().getCenterY());
                }
                _GLRender2D(gld, keepBinding, newFXObject(mySp, texBounds, fx), z, transform | GLHandler._GL_TRANSFORM_TRANSLATE_BIT | GLHandler._GL_TRANSFORM_ROTATE_BIT, scale, rotationFX, BufferIO._wrapd(new double[]{fx_tx.getTranslateX() + translate.get(0), fx_tx.getTranslateY() + translate.get(1)}), colorBlend, alphaBlend);
            }
        }
        /**
         * FX GL BLOCK end
         */
        if (fxPushBlockInit) {
            GL11.glPopAttrib();
            _fxPushBlock.end();
        }
        Thread.currentThread().setPriority(pty);
    }

    /**
     * a GL2DFXObject is returned if and only if fxBounds is not equal to the
     * source shape bounds (i.e. !fxBounds.equals(src.getShape().getBounds()))
     */
    private GL2DObject newFXObject(final GL2DObject src, final Rectangle fxBounds, final int fx) {
        Rectangle srcBounds = src.getShape().getBounds();
        if (srcBounds.equals(fxBounds)) {
            return src;
        }
        /**
         * Transforms src shape to fxBounds
         */
        PerspectiveTransform ptx = PerspectiveTransform.getQuadToQuad(srcBounds.getMinX(), srcBounds.getMinY(), srcBounds.getMaxX(), srcBounds.getMinY(),
                srcBounds.getMaxX(), srcBounds.getMaxY(), srcBounds.getMinX(), srcBounds.getMaxY(),
                fxBounds.getMinX(), fxBounds.getMinY(), fxBounds.getMaxX(), fxBounds.getMinY(),
                fxBounds.getMaxX(), fxBounds.getMaxY(), fxBounds.getMinX(), fxBounds.getMaxY());
        /**
         * instance a transformed shape throughout a GeneralPath
         */
        final Vector<Map<Integer, float[]>> perspectiveTransformPoly = new Vector<Map<Integer, float[]>>();
        final PathIterator pi = src.getShape().getPathIterator(null);
        final GeneralPath gp = new GeneralPath(pi.getWindingRule());
        while (!pi.isDone()) {
            float[] coords = new float[6];
            float[] fxCoords = new float[6];
            int path = pi.currentSegment(coords);
            perspectiveTransformPoly.add(Collections.singletonMap(path, fxCoords));
            switch (path) {
                case PathIterator.SEG_MOVETO:
                    ptx.transform(coords, 0, fxCoords, 0, 1);
                    break;
                case PathIterator.SEG_CLOSE:
                    break;
                case PathIterator.SEG_LINETO:
                    ptx.transform(coords, 0, fxCoords, 0, 1);
                    break;
                case PathIterator.SEG_CUBICTO:
                    ptx.transform(coords, 0, fxCoords, 0, 3);
                    break;
                case PathIterator.SEG_QUADTO:
                    ptx.transform(coords, 0, fxCoords, 0, 2);
                    break;
                default:
                    break;
            }
            pi.next();
        }
        gp.append(new PathIterator() {

            Iterator<Map<Integer, float[]>> i = perspectiveTransformPoly.iterator();
            Map<Integer, float[]> k = i.next();

            @Override
            public int getWindingRule() {
                return pi.getWindingRule();
            }

            @Override
            public boolean isDone() {
                return !i.hasNext();
            }

            @Override
            public void next() {
                k = i.next();
            }

            @Override
            public int currentSegment(float[] coords) {
                float[] fCoords = k.values().iterator().next();
                int p = k.keySet().iterator().next();
                for (int i = 0; i < fCoords.length; i++) {
                    coords[i] = fCoords[i];
                }
                return p;
            }

            @Override
            public int currentSegment(double[] coords) {
                float[] fCoords = k.values().iterator().next();
                int p = k.keySet().iterator().next();
                for (int i = 0; i < fCoords.length; i++) {
                    coords[i] = fCoords[i];
                }
                return p;
            }
        }, false);
        /**
         * shape for the fx-object
         */
        GL2DFXObject fxSp = new GL2DFXObject() {

            @Override
            public Shape getShape() {
                return gp;
            }

            @Override
            public int getUID() {
                return src.getUID();
            }

            @Override
            public GL2DObject getSrc() {
                return src;
            }

            @Override
            public boolean isFill() {
                return src.isFill();
            }

            @Override
            public int getResolution() {
                return src.getResolution();
            }

            @Override
            public int getFX() {
                return fx;
            }
        };
        return fxSp;
    }

    /**
     * renders the GL2DObject within an openGL context
     *
     * @param gld the RenderingSceneGL that is to be rendered onto
     * @param sp the GL2DObject to render
     * @param z z-depth to render at
     */
    public void _GLpushRender2DObject(RenderingSceneGL gld, GL2DObject sp, double z) {
        _GLpushRender2DObject(gld, sp, z, 0, null, null);
    }

    /**
     * renders the GL2DObject within an openGL context
     *
     * @param gld the RenderingSceneGL that is to be rendered onto
     * @param sp the GL2DObject to render
     * @param z z-depth to render at
     * @param transform the transform bitwise-OR combination to apply or 0 for
     * none {@linkplain GLHandler#_GL_TRANSFORM_ROTATE}, {@linkplain GLHandler#_GL_TRANSFORM_SCALE}, {@linkplain GLHandler#_GL_TRANSFORM_TRANSLATE}
     * @param scale double array of scaling args (scaleX, scaleY) or null
     * @param rotate double array of rotation arg (rotateDEGREES) or null
     * @param translate double array of translation args (translateX,
     * translateY) or null
     */
    public void _GLpushRender2DObject(RenderingSceneGL gld, GL2DObject sp, double z, int transform, DoubleBuffer scale, DoubleBuffer rotate, DoubleBuffer translate) {
        _GLpushRender2DObject(gld, sp, z, 0, null, null, transform, scale, rotate, translate);
    }

    /**
     * renders the GL2DObject within an openGL context
     *
     * @param gld the RenderingSceneGL that is to be rendered onto
     * @param sp the GL2DObject to render
     * @param z z-depth to render at
     * @param fx the gfx to apply or 0 for none
     * @param fx_loc x,y Point location of the gfx, or Point(0,0)
     * @param fx_color Color for the gfx, or null
     */
    public void _GLpushRender2DObject(RenderingSceneGL gld, GL2DObject sp, double z, int fx, Point fx_loc, Color fx_color) {
        _GLpushRender2DObject(gld, sp, z, fx, fx_loc, fx_color, 0, null, null, null);
    }

    /**
     * renders the GL2DObject within an openGL context
     *
     * @param gld the RenderingSceneGL that is to be rendered onto
     * @param sp the GL2DObject to render
     * @param z z-depth to render at
     * @param fx the gfx to apply or 0 for none
     * @param fx_loc x,y Point location of the gfx, or Point(0,0)
     * @param fx_color Color for the gfx, or null
     * @param transform the transform bitwise-OR combination to apply or 0 for
     * none {@linkplain GLHandler#_GL_TRANSFORM_ROTATE}, {@linkplain GLHandler#_GL_TRANSFORM_SCALE}, {@linkplain GLHandler#_GL_TRANSFORM_TRANSLATE}
     * @param scale double array of scaling args (scaleX, scaleY) or null
     * @param rotate double array of rotation arg (rotateDEGREES) or null
     * @param translate double array of translation args (translateX,
     * translateY) or null
     */
    public void _GLpushRender2DObject(RenderingSceneGL gld, GL2DObject sp, double z, int fx, Point fx_loc, Color fx_color, int transform, DoubleBuffer scale, DoubleBuffer rotate, DoubleBuffer translate) {
        _GLpushRender2DObject(gld, true, sp, z, fx, fx_loc, fx_color, transform, scale, rotate, translate);
    }

    /**
     * NOTICE : encapsulated in a {@link #__GLRender2DFX(net.sf.jiga.xtended.impl.game.RenderingSceneGL, boolean, net.sf.jiga.xtended.impl.game.gl.GL2DObject, double, int, java.awt.Point, java.awt.Color, int, java.nio.DoubleBuffer, java.nio.DoubleBuffer, java.nio.DoubleBuffer) CALLBACK}<br>
     * renders the Shape within an OpenGL context
     *
     * @param z the z-depth to render at
     * @param gld the RenderingSceneGL that is to be rendered onto
     * @param sp the Shape to render
     * @param transform the transform bitwise-OR combination to apply or 0 for
     * none {@linkplain GLHandler#_GL_TRANSFORM_ROTATE}, {@linkplain GLHandler#_GL_TRANSFORM_SCALE}, {@linkplain GLHandler#_GL_TRANSFORM_TRANSLATE}
     * @param scale double array of scaling args (scaleX, scaleY) or null
     * @param rotate double array of rotation arg (rotateDEGREES) or null
     * @param translate double array of translation args (translateX,
     * translateY) or null
     * @param colorBlend Color blending or null
     */
    protected void _GLRender2D(RenderingSceneGL gld, GL2DObject sp, double z, int transform, DoubleBuffer scaleArgs, DoubleBuffer rotateArgs, DoubleBuffer translateArgs, FloatBuffer colorBlend, FloatBuffer alphaBlend) {
        _GLRender2D(gld, true, sp, z, transform, scaleArgs, rotateArgs, translateArgs, colorBlend, alphaBlend);
    }

    /**
     * NOTICE : encapsulated in a {@link #__GLRender2DFX(net.sf.jiga.xtended.impl.game.RenderingSceneGL, boolean, net.sf.jiga.xtended.impl.game.gl.GL2DObject, double, int, java.awt.Point, java.awt.Color, int, java.nio.DoubleBuffer, java.nio.DoubleBuffer, java.nio.DoubleBuffer) CALLBACK}.<br>
     * Implent this method : render the Shape within an OpenGL context and
     * perform the specified transform operations, see {@linkplain GLHandler#_transformBegin(java.awt.Rectangle, double, int, java.nio.DoubleBuffer, java.nio.DoubleBuffer, java.nio.DoubleBuffer) GLHandler._transformBegin() and GLHandler._transformEnd()}.
     *
     * @param z z-depth to render at
     * @param keepBinding keeps the associated texture in VRAM for further use
     * or not, resp.
     * @param gld the RenderingSceneGL that is to be rendered onto
     * @param mySp the Shape to render
     * @param transform the transform bitwise-OR combination to apply or 0 for
     * none {@linkplain RenderingSceneGL#_GL_TRANSFORM_ROTATE}, {@linkplain RenderingSceneGL#_GL_TRANSFORM_SCALE}, {@linkplain RenderingSceneGL#_GL_TRANSFORM_TRANSLATE}
     * @param scale double array of scaling args (scaleX, scaleY) or null
     * @param rotate double array of rotation arg (rotateDEGREES) or null
     * @param translate double array of translation args (translateX,
     * translateY) or null
     * @param colorBlend Color blending or null
     * @see GLGeom example source code to render 2D geometrical shapes
     */
    protected abstract void _GLRender2D(RenderingSceneGL gld, boolean keepBinding, GL2DObject mySp, double z, int transform, DoubleBuffer scaleArgs, DoubleBuffer rotateArgs, DoubleBuffer translateArgs, FloatBuffer colorBlend, FloatBuffer alphaBlend);

    /**
     * Renders the textureHash. Occasional transforms are applied in the
     * following Matrix order : current x SCALE x TRANSLATE x ROTATE x
     * tex-quad(bounds.x,bounds.y,z,1)
     *
     * @param gld the canvas instance to render onto (it must be up and ready)
     * @param keepBuffer specify the garbage collection to be keep the buffer in
     * VRAM or not, resp.
     * @param texture the Sf3Texture texture or the texture.hashCode() to render
     * @param bounds the 2D Rectangle bounds of the textureHash within the
     * canvas bounds (usual Java 2D coords, 0;0 is located at the upper left
     * corner)
     * @param z the Z-Depth that locates the textureHash quad in the space
     * @param transform {@linkplain #_GL_TRANSFORM_SCALE_BIT}, {@linkplain #_GL_TRANSFORM_TRANSLATE_BIT}, {@linkplain #_GL_TRANSFORM_ROTATE_BIT}
     * or 0 for no transform
     * @param scaleArgs scaling vector array(scaleX, scaleY)
     * @param rotateArgs rotation vector array(rotateDEGREES, rotateCenterX,
     * rotateCenterY)
     * @param translateArgs translate vector array(translateX, translateY)
     * @param alphaBlend alpha blending components vector array(alphaRed,
     * alphaGreen, alphaBlue, alpha)
     * @param colorBlend color blending components vector array(red, green,
     * blue, alpha)
     */
    public static void _renderTexture2D(RenderingSceneGL gld, boolean keepBuffer, Object texture, Rectangle bounds,
            double z, int transform, DoubleBuffer scaleArgs, DoubleBuffer rotateArgs, DoubleBuffer translateArgs, FloatBuffer alphaBlend, FloatBuffer colorBlend) {
        if (!GLHandler.__renderTexture(Sf3Texture._EXTtex, gld, keepBuffer, texture, 0, bounds, z, transform, scaleArgs, rotateArgs, translateArgs, alphaBlend, colorBlend)) {
            System.err.println(JXAenvUtils.log("No such texture found in buffers, please check loading ! tex hash is : " + texture.hashCode() + " original sprite can be : " + Sprite._GLgetSpriteHash(texture.hashCode()), JXAenvUtils.LVL.APP_ERR));
        }
    }

    /**
     * @param rCoord the 3D-Texture R coordinate
     * @see #_renderTexture2D(RenderingSceneGL, boolean, Object, Rectangle,
     * double, int, DoubleBuffer, DoubleBuffer, DoubleBuffer, FloatBuffer,
     * FloatBuffer)
     */
    protected static void _renderTexture3D(RenderingSceneGL gld, boolean keepBuffer, Object texture, float rCoord, Rectangle bounds,
            double z, int transform, DoubleBuffer scaleArgs, DoubleBuffer rotateArgs, DoubleBuffer translateArgs, FloatBuffer alphaBlend, FloatBuffer colorBlend) {
        if (!GLHandler.__renderTexture(GL12.GL_TEXTURE_3D, gld, keepBuffer, texture, rCoord, bounds, z, transform, scaleArgs, rotateArgs, translateArgs, alphaBlend, colorBlend)) {
            System.err.println(JXAenvUtils.log("No such texture found in buffers, please check loading ! tex hash is : " + texture.hashCode() + " original sprite can be : " + Sprite._GLgetSpriteHash(texture.hashCode()), JXAenvUtils.LVL.APP_ERR));
        }
    }
}
