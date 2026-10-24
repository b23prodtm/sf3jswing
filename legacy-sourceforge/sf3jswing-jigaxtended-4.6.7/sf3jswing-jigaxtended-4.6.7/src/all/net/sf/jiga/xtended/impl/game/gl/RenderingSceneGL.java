/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.game.gl;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import net.sf.jiga.xtended.impl.Animation;
import net.sf.jiga.xtended.impl.GUIConsoleOutput;
import net.sf.jiga.xtended.impl.Sprite;
import net.sf.jiga.xtended.impl.SpriteIO;
import net.sf.jiga.xtended.impl.game.GameActionLayer;
import net.sf.jiga.xtended.impl.game.Model;
import net.sf.jiga.xtended.impl.game.RenderingScene;
import net.sf.jiga.xtended.impl.game.RenderingSceneComponent;
import net.sf.jiga.xtended.impl.game.scene.State;
import net.sf.jiga.xtended.impl.game.TextLayout;
import net.sf.jiga.xtended.impl.game.gl.geom.GLGeom;
import net.sf.jiga.xtended.impl.system.BufferIO;
import net.sf.jiga.xtended.impl.system.LoadListener;
import net.sf.jiga.xtended.impl.system.LoadListener2;
import net.sf.jiga.xtended.impl.system.input.ControllersHandler;
import net.sf.jiga.xtended.impl.system.input.KLAWT;
import net.sf.jiga.xtended.kernel.BitStack;
import net.sf.jiga.xtended.kernel.DebugMap;
import static net.sf.jiga.xtended.kernel.JXAenvUtils.*;
import net.sf.jiga.xtended.kernel.Monitor;
import net.sf.jiga.xtended.kernel.SpritesCacheManager;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.ARBTextureRectangle;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.EXTAbgr;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.opengl.Util;
import org.lwjgl.util.glu.GLU;

/**
 * <h2>LWJGL and openGL</h2> <br/>Important notes about LWJGL rendering : This
 * framework significantly adds speed and robustness to the graphical rendering
 * process, as of openGL specs the hardware acceleration can be boosted to its
 * maximum. BUT there are few checks to verify before using OpenGL and
 * hardware-accelerated graphics rendering : <ul> <li>to be familiar with the
 * OpenGL framework (use a book or the official guide);</li> <li>then
 * accessing/rendering/parametrizing/modifying/loading, if not with the provided
 * JXA functions, 2D textures MUST BE DONE using the
 * {@linkplain Sf3Texture#_EXTtex} target or it won't display properly;</li>
 * <li>about multi-threading (MT), still very experimental : it is not permitted
 * to enable multi-threading over LWJGL or Swing, only the Java hardware active
 * rendering is authorized for MT;</li> <li>hardware acceleration is recommended
 * to enable when using 100+ _GLSprites in the scene;</li> <li>though all
 * settings can be dynamically changed over the run-time, a properties
 * configuration file provide a flexible way to change (some) settings
 * (resources/properties)/net/sf/jiga/xtended/impl/game/rendering.properties;</li>
 * <li>it is recommended to add ONE or more RenderingSceneListener to stay
 * informed of what the rendering has done, e.g. the switch-fullscreen process
 * sends a reset() (it will be run on the Swing EDT) event when coming off the
 * full-screen back to windowed so that a new RenderingSceneGL must be laid out
 * from its container (the RenderingListener has to do that externally
 * itself);</li> <li>DO NOT engage dramatical changes in any way of the source
 * code or the stability may be affected ! ;)</li> <li>PREFER to extend the
 * source code than changing it !</li> </ul> Using OpenGL also requires to
 * allocate all Sprites to the VRAM memory of OpenGL (the texture pixel
 * buffers). That is using the
 * {@linkplain #_GLloadModel(Model, RenderingScene)}, {@linkplain #_GLloadSprite(Sprite, RenderingScene)}
 * and or {@linkplain #_GLloadAnim(Animation, RenderingScene)}. Plus, the
 * rendering speed is VERY SENSIBLE to disk access, that's why it must also be
 * avoided to freqently access cache while rendering. Load all cache data into
 * declared variables and ensure the heap is not overloaded or increase the heap
 * size (Java option (-J)-Xmx=XXXM XXX is the amount of Ram to allocate in
 * MegaBytes) !<br> Cache access includes the following methods : <ul>
 * <li>{@linkplain Animation#getCurrentSprite()} and other related methods that
 * return a Sprite instance, therefore use
 * {@linkplain GLAnimationHandler#getCurrentSprite()};</li>
 * <li>{@linkplain Model#anim(String, long, boolean)} accesses the Model
 * animation cache, therefore use
 * {@linkplain Model#GLanim(String, long, boolean)};</li>
 * <li>all {@linkplain SpritesCacheManager} instance methods;</li> <li>all
 * {@linkplain RenderingScene} methods that return an Animation or Sprite
 * instance, therefore use GL variants;</li> </ul> If you want to get control
 * over an Animation that has been loaded, use {@linkplain AnimationGLHandler}.
 * The {@linkplain Model}'s can be controlled with GL_...() functions.
 *
 * <h3>alpha blending (source : Jogl Texture class)</h3>
 * <p>
 * <a name="premult"><b>Alpha premultiplication and blending</b></a> <br> The
 * mathematically correct way to perform blending in OpenGL (with the SrcOver
 * "source over destination" mode, or any other Porter-Duff rule) is to use
 * "premultiplied color components", which means the R/G/ B color components
 * have already been multiplied by the alpha value. To make things easier for
 * developers, the Texture class will automatically convert non-premultiplied
 * image data into premultiplied data when storing it into an OpenGL texture. As
 * a result, it is important to use the correct blending function; for example,
 * the SrcOver rule is expressed as:
 * <pre>
 * GL11.glBlendFunc(GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA);
 * </pre> Also, when using a texture function like <code>GL_MODULATE</code>
 * where the current color plays a role, it is important to remember to make
 * sure that the color is specified in a premultiplied form, for example:
 * <pre>
 * float a = ...;
 * float r = r * a;
 * float g = g * a;
 * float b = b * a;
 * GL11.glColor4f(r, g, b, a);
 * </pre>
 *
 * For reference, here is a list of the Porter-Duff compositing rules and the
 * associated OpenGL blend functions (source and destination factors) to use in
 * the face of premultiplied alpha:
 *
 * <CENTER> <TABLE WIDTH="75%"> <TR> <TD> Rule <TD> Source <TD> Dest <TR> <TD>
 * Clear <TD> GL_ZERO <TD> GL_ZERO <TR> <TD> Src <TD> GL_ONE <TD> GL_ZERO <TR>
 * <TD> SrcOver <TD> GL_ONE <TD> GL_ONE_MINUS_SRC_ALPHA <TR> <TD> DstOver <TD>
 * GL_ONE_MINUS_DST_ALPHA <TD> GL_ONE <TR> <TD> SrcIn <TD> GL_DST_ALPHA <TD>
 * GL_ZERO <TR> <TD> DstIn <TD> GL_ZERO <TD> GL_SRC_ALPHA <TR> <TD> SrcOut <TD>
 * GL_ONE_MINUS_DST_ALPHA <TD> GL_ZERO <TR> <TD> DstOut <TD> GL_ZERO <TD>
 * GL_ONE_MINUS_SRC_ALPHA <TR> <TD> Dst <TD> GL_ZERO <TD> GL_ONE <TR> <TD>
 * SrcAtop <TD> GL_DST_ALPHA <TD> GL_ONE_MINUS_SRC_ALPHA <TR> <TD> DstAtop <TD>
 * GL_ONE_MINUS_DST_ALPHA <TD> GL_SRC_ALPHA <TR> <TD> AlphaXor <TD>
 * GL_ONE_MINUS_DST_ALPHA <TD> GL_ONE_MINUS_SRC_ALPHA </TABLE> </CENTER> <h3>GL
 * lighting</h3> GL_COLOR_MATERIAL is enabled and glColorMaterial has the
 * default values GL_FRONT_AND_BACK to GL_AMBIENT_AND_DIFFUSE GL_LIGHTING is not
 * enabled by default. GL_EMISSION is changed to the colorBlend argument for
 * every renderContents texture call. before rendering a texture or a quad with
 * no texture and lighting enabled, GL_SHININESS and GL_SPECULAR color can be
 * changed with the usual GL calls. To change the GL_AMBIENT_AND_DIFFUSE and
 * GL_EMISSION, change the color blend arg. !
 *
 * <br>
 * <p>
 * <h2>Displaying text onto the openGL overlay</h2>
 * <pre>scene.addActionOffscreen(new GameActionLayer("graphics") {
 * protected void renderToGL(RenderingSceneGL gld) {
 * super.renderToGL(gld);
 * try {
 * GLdraw(gld);
 * GLfader(gld);
 *
 * scene.getHud_user().setBounds(0, 0, scene.getWidth(), scene.getHeight(), playerDepth + 1);
 * scene.getHud_user().GLbegin();
 * scene.getHud_user().GLrenderContents(scene);
 * scene.getHud_user().GLend();
 * } catch (Exception ex) {
 * ex.printStackTrace();
 * }
 * }
 * });
 * scene.getHud_user().pushContent("app specs", getClass().getPackage().getSpecificationVersion(), TextLayout.POS.LEFT, TextLayout.POS.TOP, true);</pre>
 * </p>
 *
 * @author www.b23prodtm.info
 */
public class RenderingSceneGL extends RenderingScene {

        /**
         * change the PixeFormat param's before to instanciate a
         * RenderingSceneGL ! It defines wether OpenGL can make use of Alpha
         * colors, Depth buffering or Stencil, etc.
         */
        public static PixelFormat _GL_pixFormat = new PixelFormat().withAlphaBits(8).withDepthBits(24).withStencilBits(8).withSamples(4);
        /**
         * GLclearRegion color for openGL to use, a 4 components rgba float
         * array
         */
        public static FloatBuffer _GLclearColor = BufferIO._wrapf(new float[]{0, 0, 0, 1});
        private final static List<Map> _GLmapOfLinks = Collections.synchronizedList(new ArrayList<Map>());
        private final static List<Collection> _GLcollectionOfLinks = Collections.synchronizedList(new ArrayList<Collection>());
        /**
         * if true, GLText does renderContents with GLTesselator all Java glyphs
         * !
         *
         * @deprecated set on to use GLText
         */
        protected static boolean _GL_renderGlyphs = true;
        /**
         * font map character 'c' => {font instance => {size, hash}}
         */
        private static Map<Character, Map<Font, Map<Integer, Integer>>> _GLcharacterMap = _GLregMapOfLinks(Collections.synchronizedMap(new HashMap<Character, Map<Font, Map<Integer, Integer>>>()));
        static Set<GLList> glLists = GLList.glLists;
        private static final BitStack _GLLOADSTATE_bits = new BitStack();
        /**
         *
         */
        public static final int _GLLOADSTATE_Cleared = _GLLOADSTATE_bits._newBitRange();
        /**
         *
         */
        public static final int _GLLOADSTATE_Loading = _GLLOADSTATE_bits._newBitRange();
        /**
         *
         */
        public static final int _GLLOADSTATE_Loaded = _GLLOADSTATE_bits._newBitRange();
        /**
         *
         */
        public static final int _GLLOADSTATE_Errored = _GLLOADSTATE_bits._newBitRange();
        /**
         *
         */
        public static final GLObjectHandler<SpriteGLHandler> _GLSprites = new GLObjectHandler<SpriteGLHandler>();
        /**
         * GL priority
         */
        public static float PTY_SPRITE = 0.5F;
        public static float PTY_ANIM = 0.6F;
        public static float PTY_MODEL = 0.7F;
        /**
         *
         */
        public static final GLObjectHandler<ModelGLHandler> _GLModels = new GLObjectHandler<ModelGLHandler>();
        /**
         *
         */
        public static final GLObjectHandler<AnimationGLHandler> _GLAnimations = new GLObjectHandler<AnimationGLHandler>();

        /**
         * eventually instances and returns a Sprite instance with the desired
         * logo image/x-png image file CAUTION : this method loads Sprite for
         * both Java2D and LWJGL mode, so it might be slower for bigger sprite
         * as the BufferedImage is also serialized out
         *
         * @param c
         * @param logo the file name of the logo
         * @param size the size to set the Sprite to display at
         * @return the Sprite instance as a hashCode
         * @see #_drawLogo(Component, Graphics2D, String, Dimension, int, int,
         * int, int)
         */
        public static SpriteGLHandler _GLgetLogo(Component c, String logo, Dimension size) {
                return _GLgetLogo(c, logo, size, false);
        }

        /**
         * CAUTION : this method loads Sprite for both Java2D and LWJGL mode, so
         * it might be slower for bigger sprite as the BufferedImage is also
         * serialized out
         *
         * @param c
         * @param logo
         * @param size
         * @param tileMode
         * @return
         */
        public static SpriteGLHandler _GLgetLogo(Component c, String logo, Dimension size, boolean tileMode) {
                if (!spritesHashesBuffer.containsKey(logo)) {
                        _getLogo(c, logo, size, tileMode);
                }
                if ((_GLgetLoadState(spritesHashesBuffer.get(logo)) & _GLLOADSTATE_Cleared) != 0) {
                        Sprite sp = _getLogo(c, logo, size, tileMode);
                        _GLloadSprite(sp, (RenderingSceneGL) c);
                }
                int hash = spritesHashesBuffer.get(logo);
                return _GLSprites.getHandler(hash);
        }

        /**
         * CAUTION : this method loads Sprite for both Java2D and LWJGL mode, so
         * it might be slower for bigger sprite as the BufferedImage is also
         * serialized out
         *
         * @param c
         * @param animation_folder
         * @param start
         * @param GLend
         * @param prefix
         * @param suffix
         * @param size
         * @return
         */
        public static AnimationGLHandler _GLgetLogoAnimation(RenderingSceneGL c, String animation_folder, int start, int end, String prefix, String suffix, Dimension size) {
                String bufferPath = _getBufferPathAnimation(animation_folder, start, end, prefix, suffix);
                if (!spritesHashesBuffer.containsKey(bufferPath)) {
                        _getLogoAnimation(c, animation_folder, start, end, prefix, suffix, size);
                }
                if ((_GLgetLoadState(spritesHashesBuffer.get(bufferPath)) & _GLLOADSTATE_Cleared) != 0) {
                        _GLloadAnim(_getLogoAnimation(c, animation_folder, start, end, prefix, suffix, size), (RenderingSceneGL) c, PTY_ANIM);
                }
                return _GLAnimations.getHandler(spritesHashesBuffer.get(bufferPath));
        }

        /**
         *
         * @param c
         * @param logo
         * @param size
         * @param hpos
         * @param vpos
         * @param fx
         * @param fx_loc
         * @param fx_color
         * @param transform
         * @param scale
         * @param rotate
         * @param translate
         * @throws InterruptedException
         */
        public static void _GLdrawLogo(RenderingSceneGL c, String logo, Dimension size, TextLayout.POS hpos, TextLayout.POS vpos, int fx, Point fx_loc, Color fx_color, int transform, DoubleBuffer scale, DoubleBuffer rotate, DoubleBuffer translate) throws InterruptedException {
                _GLdrawLogo(c, logo, -1, size, hpos, vpos, fx, fx_loc, fx_color, transform, scale, rotate, translate);
        }

        /**
         *
         * @param c
         * @param logo
         * @param z
         * @param size
         * @param hpos
         * @param vpos
         * @param fx
         * @param fx_loc
         * @param fx_color
         * @param transform
         * @param scale
         * @param rotate
         * @param translate
         * @throws InterruptedException
         */
        public static void _GLdrawLogo(RenderingSceneGL c, String logo, double z, Dimension size, TextLayout.POS hpos, TextLayout.POS vpos, int fx, Point fx_loc, Color fx_color, int transform, DoubleBuffer scale, DoubleBuffer rotate, DoubleBuffer translate) throws InterruptedException {
                SpriteGLHandler logo_sp = _GLgetLogo((Component) c, logo, size);
                __GLdrawLogo(c, logo_sp, z, size, hpos, vpos, fx, fx_loc, fx_color, transform, scale, rotate, translate);
        }

        /**
         *
         * @param c
         * @param animation_folder
         * @param start
         * @param GLend
         * @param prefix
         * @param suffix
         * @param size
         * @param hpos
         * @param vpos
         * @param fx
         * @param fx_loc
         * @param fx_color
         * @param transform
         * @param scale
         * @param rotate
         * @param translate
         * @throws InterruptedException
         */
        public static void _GLdrawLogoAnimation(RenderingSceneGL c, String animation_folder, int start, int end, String prefix, String suffix, Dimension size, TextLayout.POS hpos, TextLayout.POS vpos, int fx, Point fx_loc, Color fx_color, int transform, DoubleBuffer scale, DoubleBuffer rotate, DoubleBuffer translate) throws InterruptedException {
                _GLdrawLogoAnimation(c, animation_folder, -1, start, end, prefix, suffix, size, hpos, vpos, fx, fx_loc, fx_color, transform, scale, rotate, translate);
        }

        /**
         *
         * @param c
         * @param animation_folder
         * @param z
         * @param start
         * @param GLend
         * @param prefix
         * @param suffix
         * @param size
         * @param hpos
         * @param vpos
         * @param fx
         * @param fx_loc
         * @param fx_color
         * @param transform
         * @param scale
         * @param rotate
         * @param translate
         * @throws InterruptedException
         */
        public static void _GLdrawLogoAnimation(RenderingSceneGL c, String animation_folder, double z, int start, int end, String prefix, String suffix, Dimension size, TextLayout.POS hpos, TextLayout.POS vpos, int fx, Point fx_loc, Color fx_color, int transform, DoubleBuffer scale, DoubleBuffer rotate, DoubleBuffer translate) throws InterruptedException {
                AnimationGLHandler logoAnimation_sp = _GLgetLogoAnimation(c, animation_folder, start, end, prefix, suffix, size);
                __GLdrawLogoAnimation(c, logoAnimation_sp, z, size, hpos, vpos, fx, fx_loc, fx_color, transform, scale, rotate, translate);
        }

        /**
         *
         * @param c
         * @param logo_sp
         * @param z
         * @param size
         * @param hpos
         * @param vpos
         * @param fx
         * @param fx_loc
         * @param fx_color
         * @param transform
         * @param scale
         * @param rotate
         * @param translate
         */
        public static void __GLdrawLogoAnimation(RenderingSceneGL c, AnimationGLHandler logo_sp, double z, Dimension size, TextLayout.POS hpos, TextLayout.POS vpos, int fx, Point fx_loc, Color fx_color, int transform, DoubleBuffer scale, DoubleBuffer rotate, DoubleBuffer translate) {
                Point translatePt = __getPosition(c, hpos, vpos, size);
                logo_sp.play();
                Rectangle bounds = new Rectangle(translatePt.x, translatePt.y, size.width, size.height);
                logo_sp.runValidate();
                Animation._GLRenderAnimation(c, logo_sp, bounds, z, fx, fx_loc, fx_color, transform, scale, rotate, translate);
        }

        /**
         *
         * @param c
         * @param logo_sp
         * @param z
         * @param size
         * @param hpos
         * @param vpos
         * @param fx
         * @param fx_loc
         * @param fx_color
         * @param transform
         * @param scale
         * @param rotate
         * @param translate
         */
        public static void __GLdrawLogo(RenderingSceneGL c, SpriteGLHandler logo_sp, double z, Dimension size, TextLayout.POS hpos, TextLayout.POS vpos, int fx, Point fx_loc, Color fx_color, int transform, DoubleBuffer scale, DoubleBuffer rotate, DoubleBuffer translate) {
                Point translatePt = __getPosition(c, hpos, vpos, size);
                Rectangle bounds = new Rectangle(translatePt.x, translatePt.y, size.width, size.height);
                Sprite._GLRenderSprite(c, logo_sp, bounds, z, fx, fx_loc, fx_color, transform, scale, rotate, translate);
        }

        /**
         *
         * @param canvas
         * @see #createBufferStrategy()
         */
        public static void _GL_initCurrentContext(RenderingSceneGL canvas) {
                /*
                 * will fill the variable (but nothing can be read rom hardware yet if
                 * not displayable createBufferStrategy
                 */
                GLHandler._GL_readGLRendererName();
                GLHandler._GL_readGLVendorName();
                GLHandler._GL_readGLVersionName();
                if (DebugMap._getInstance().isDebuggerEnabled(RenderingSceneGL.class)) {
                        System.out.println(log(" Init GL context \n" + "read GL Extensions string \n" + GLHandler._GL_readGLExtensionsString(true), LVL.APP_NOT));
                }
                System.out.println("Attrib max stack depth " + GL11.glGetInteger(GL11.GL_MAX_ATTRIB_STACK_DEPTH));
                /**
                 * texture_rectangle
                 */
                if (GLHandler.ext.TextureRectangle.GL_isAvailable()) {
                        Sf3Texture._EXTtex = ARBTextureRectangle.GL_TEXTURE_RECTANGLE_ARB;
                        if (DebugMap._getInstance().isDebuggerEnabled(RenderingSceneGL.class)) {
                                System.out.println(log("SELECTED TEX 2D MODE EXT rectangle " + Sf3Texture._EXTtex, LVL.APP_NOT));
                        }
                } else {
                        Sf3Texture._EXTtex = GL11.GL_TEXTURE_2D;
                        if (DebugMap._getInstance().isDebuggerEnabled(RenderingSceneGL.class)) {
                                System.out.println(log("SELECTED TEX 2D MODE " + GL11.GL_TEXTURE_2D, LVL.APP_NOT));
                        }
                }
                /**
                 * abgr
                 */
                if (GLHandler.ext.ABGR.GL_isAvailable()) {
                        Sf3Texture._EXTabgr = EXTAbgr.GL_ABGR_EXT;
                        /*
                         * GL11.glEnable(Sf3Texture._EXTabgr);
                         */
                        if (DebugMap._getInstance().isDebuggerEnabled(RenderingSceneGL.class)) {
                                System.out.println(log("SELECTED TEX FORMAT EXT abgr", LVL.APP_NOT));
                        }
                } else {
                        Sf3Texture._EXTabgr = GL12.GL_BGRA;
                        if (DebugMap._getInstance().isDebuggerEnabled(RenderingSceneGL.class)) {
                                System.out.println(log("SELECTED TEX FORMAT BGRA format", LVL.APP_NOT));
                        }
                }
                /**
                 * PIXEL STORE INTO CLIENT MEMORY
                 */
                GL11.glPixelStorei(GL11.GL_PACK_LSB_FIRST, BufferIO.isLSBEnv() ? GL11.GL_TRUE : GL11.GL_FALSE);
                GL11.glPixelStorei(GL11.GL_UNPACK_LSB_FIRST, BufferIO.isLSBEnv() ? GL11.GL_TRUE : GL11.GL_FALSE);
                /**
                 * tight storage : texture loading, drawPixels
                 */
                GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
                /**
                 * tight storage : readPixels
                 */
                GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
                Util.checkGLError();
                /*
                 * set the background colour of the display to black
                 */
                GL11.glClearColor(_GLclearColor.get(0), _GLclearColor.get(1), _GLclearColor.get(2), _GLclearColor.get(3));
                GL11.glColor4f(1, 1, 1, 1);
                GL11.glClearStencil(0);
                /*
                 * disable the OpenGL depth test since we're rendering 2D graphics
                 */
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glDepthFunc(GL11.GL_LEQUAL);
                GL11.glDisable(GL11.GL_DITHER);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
                /*
                 * enable textures since we're going to use these for our _GLSprites
                 */
                FloatBuffer materialColor = BufferIO._wrapf(new float[]{0, 0, 0, 0.1F});
                GL11.glMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT_AND_DIFFUSE, materialColor);
                GL11.glMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_SPECULAR, BufferIO._wrapf(new float[]{1, 1, 1, 0.5F}));
                GL11.glMateriali(GL11.GL_FRONT_AND_BACK, GL11.GL_SHININESS, 80);
                GL11.glMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_EMISSION, materialColor);
                GL11.glEnable(GL11.GL_COLOR_MATERIAL);
                Util.checkGLError();
                /**
                 * one light
                 */
                _GL_lightAmb(GL11.GL_LIGHT0, BufferIO._wrapf(new float[]{0.7F, 0.7F, 0.7F, 0.5F}));
                _GL_lightDif(GL11.GL_LIGHT0, BufferIO._wrapf(new float[]{1.0F, 1.0F, 1.0F, 0.5F}));
                _GL_lightPos(GL11.GL_LIGHT0, BufferIO._wrapf(new float[]{10.0F, 10.0F, -canvas._GL_CAMZ, 1.0F}));
                GL11.glEnable(GL11.GL_LIGHT0);
                GL11.glDisable(GL11.GL_LIGHTING);
                Util.checkGLError();
        }

        /**
         *
         * @param viewport
         * @return
         */
        public static FloatBuffer _GL_getLookAt_center(Dimension viewport, int zDepth) {
                return BufferIO._wrapf(new float[]{(float) viewport.getWidth() / 2.0F, (float) viewport.getHeight() / 2.0F, zDepth});
        }

        /**
         * plane that defines the eye horizontal plane
         * ({@linkplain #_GL_getLookAt_center(Dimension) centered})
         *
         * @param viewport
         * @param lookAt_norm
         * @return
         */
        public static DoubleBuffer _GL_getLookAt_plane(Dimension viewport, int zDepth, FloatBuffer lookAt_norm) {
                FloatBuffer lookAt_center = _GL_getLookAt_center(viewport, zDepth);
                return BufferIO._wrapd(new double[]{lookAt_norm.get(0), lookAt_norm.get(1), lookAt_norm.get(2), -(lookAt_norm.get(0) * lookAt_center.get(0) + lookAt_norm.get(1) * lookAt_center.get(1) + lookAt_norm.get(2) * lookAt_center.get(2))});
        }

        /**
         *
         * @param eye
         * @param center
         * @param norm
         */
        public static void _GL_lookAt(FloatBuffer eye, FloatBuffer center, FloatBuffer norm) {
                GL11.glMatrixMode(GL11.GL_MODELVIEW);
                GLU.gluLookAt(eye.get(0), eye.get(1), eye.get(2), center.get(0), center.get(1), center.get(2), norm.get(0), norm.get(1), norm.get(2));
        }

        /**
         *
         * @param lightID
         * @param lightPos
         */
        public static void _GL_lightPos(int lightID, FloatBuffer lightPos) {
                GL11.glLight(lightID, GL11.GL_POSITION, lightPos);
        }

        /**
         *
         * @param lightID
         * @param lightAmb
         */
        public static void _GL_lightAmb(int lightID, FloatBuffer lightAmb) {
                GL11.glLight(lightID, GL11.GL_AMBIENT, lightAmb);
        }

        /**
         *
         * @param lightID
         * @param lightDif
         */
        public static void _GL_lightDif(int lightID, FloatBuffer lightDif) {
                GL11.glLight(lightID, GL11.GL_DIFFUSE, lightDif);
        }

        /**
         * a symetrical Projection is made and the origin is lifted from the
         * center of the viewport to <b>the Java2D base location </b> -- that is
         * the up-left corner with x and y pointing to the opposite corner --,
         * Z-negative is where the camera points to.<br> This method is called
         * at the beginning of each rendering loop (included in
         * update(Graphics)) and affects both the GL_PROJECTION and GL_MODELVIEW
         * matrix stacks.
         */
        protected static void _GL_reshape(RenderingSceneGL canvas, boolean ortho) {
                GL11.glViewport(0, 0, canvas.getWidth(), canvas.getHeight());
                GLHandler._GLsetProjMX(ortho, canvas.getSize(), canvas._GL_NEARVAL, canvas._GL_FARVAL);
                GLHandler._GLsetViewMX(canvas.getSize(), -canvas._GL_NEARVAL + canvas._GL_CAMZ, Java2DStyleCoordinates);
                GL11.glFlush();
                if (DebugMap._getInstance().isDebuggerEnabled(RenderingSceneGL.class)) {
                        System.out.println("GL Viewport " + canvas.getWidth() + " x " + canvas.getHeight() + "\nGL ProjMX " + (ortho ? "orthographic" : "frustum N:" + canvas._GL_NEARVAL + " F:" + canvas._GL_FARVAL) + "\nGL 0,0,0 is the up-left-corner");
                }
        }

        /**
         * returns graphical metrics of the specified String on the current
         * renderingscene.getFont().
         *
         * @param str the string to get graphical metrics from
         * @param gld defines Font peer
         * @return the dimension of the string as if it were drawn on the
         * Graphics instance
         * <pre>return new Dimension(f.stringWidth(str), f.getMaxAscent() + f.getMaxDescent());</pre>
         *
         * @see Graphics#getFontMetrics()
         *
         * @see #_toGraphicalMetrics(Graphics)
         */
        public static Dimension _GLtoGraphicalMetrics(String str, RenderingSceneGL gld) {
                FontMetrics f = _GL_fontMetrics(gld);
                return new Dimension(f.stringWidth(str), f.getMaxAscent() + f.getMaxDescent());
        }

        /**
         *
         * @param gld
         * @return
         */
        public static FontMetrics _GL_fontMetrics(RenderingSceneGL gld) {
                FontMetrics fm = gld.getFontMetrics(gld.getFont());
                assert fm != null : "no metrics could be retrieven";
                return fm;
        }

        /**
         * returns the default graphical metrics for one character on the
         * specified graphics
         *
         * @param gld
         * @return the dimension of the character as if it were drawn on the
         * Graphics instance
         *
         * @see Graphics#getFontMetrics()
         *
         * @see #_toGraphicalMetrics(String, Graphics)
         */
        public static Dimension _GLtoGraphicalMetrics(RenderingSceneGL gld) {
                return _GLtoGraphicalMetrics("G", gld);
        }

        /**
         * returns the max characters that can be drawn each-nearby on the
         * specified Graphics instance . The graphics clip size is the available
         * space.
         *
         * NOTICE : it strictly uses Bitmap fonts.
         *
         * @param clipWidth available clip width on the graphics
         *
         * @param inset the inset (left or right) is the distance between the
         * border of the clipping area and the characters
         *
         * @param gld
         * @return the number of maximal characters that can get drawn
         * continuously without going out of the scene
         *
         * @see #_GLtoGraphicalMetrics(RenderingSceneGL)
         */
        public static int _GLtoMaxCharsCOLS(int clipWidth, int inset, RenderingSceneGL gld) {
                return (int) Math.floor((float) (clipWidth - inset * 2) / (float) _GLtoGraphicalMetrics(gld).width);
        }

        /**
         * registers the Map as a map of links in the GL context. This map, and
         * the other registered ones, will be cleared each time the GL context
         * needs to refresh its memory (such as texture buffers).
         *
         * @param <T>
         * @param <V>
         * @param coll
         * @return
         */
        public static <T, V> Map<T, V> _GLregMapOfLinks(Map<T, V> coll) {
                _GLmapOfLinks.add(coll);
                return coll;
        }

        /**
         * registers the Collection as a collection of links in the GL context.
         * This collection, and the other registered ones, will be cleared each
         * time the GL context needs to refresh its memory (such as texture
         * buffers).
         *
         * @param <T>
         * @param coll
         * @return
         */
        public static <T> Collection<T> _GLregCollectionOfLinks(Collection<T> coll) {
                _GLcollectionOfLinks.add(coll);
                return coll;
        }

        /**
         * returns the current top of the stack matrix in a FloatBuffer
         *
         * @return the current top of the stack matrix in a FloatBuffer
         * @param stackPointer supported values are
         * {@linkplain GL11#GL_MODELVIEW_MATRIX}, {@linkplain GL11#GL_PROJECTION_MATRIX}
         * @see GL11#glGetFloat(int, FloatBuffer)
         */
        public static FloatBuffer _GL_getTopOftheStackMX(int stackPointer) {
                return GLHandler._GLgetTopOftheStackMX(stackPointer);
        }

        /**
         *
         * @param gld
         * @return
         */
        public static FloatBuffer _GLgetCurrentRasterPos() {
                return GLHandler._GLgetCurrentRasterPos();
        }

        /**
         * @param gld
         * @param x
         * @param text
         * @param y
         * @param clr
         */
        public static void _GLRenderText2D(RenderingSceneGL gld, String text, float x, float y, Color clr) {
                _GLRenderText2D(gld, text, x, y, -1, clr);
        }

        /**
         * the coordinate supplied is the location of the leftmost character on
         * the baseline.
         *
         * @param gld
         * @param text
         * @param z
         * @param y
         * @param x
         */
        public static void _GLRenderText2D(RenderingSceneGL gld, String text, float x, float y, double z) {
                _GLRenderText2D(gld, text, x, y, z, null);
        }

        /**
         * Text rendering is done by : <ol><li>if set false, rendering cached
         * textures of characters; ({@link Sprite})</li> <li>if set true,
         * rendering font glyphs by calling-back renderContents lists.
         * ({@link GLText})</li></ol>
         *
         * @param _GL_renderGlyphs
         * @default true
         * @deprecated set on to use GLText
         */
        public static void set_GL_renderGlyphsEnabled(boolean _GL_renderGlyphs) {
                _GL_renderGlyphs = _GL_renderGlyphs;
        }

        /**
         * @return @see #set_GL_renderGlyphsEnabled(boolean)
         * @deprecated set on to use GLText
         */
        public static boolean is_GL_renderGlyphsEnabled() {
                return _GL_renderGlyphs;
        }

        /**
         * the coordinate supplied is the location of the leftmost character on
         * the baseline.
         *
         * @param gld
         * @param text
         * @param y
         * @param x
         * @param z
         * @param clr
         * @see #set_GL_renderGlyphsEnabled(boolean)
         */
        public static void _GLRenderText2D(RenderingSceneGL gld, String text, float x, float y, double z, Color clr) {
                clr = clr instanceof Color ? clr : gld.getForeground();
                if (_GL_renderGlyphs) {
                        GLText._GLRenderText2D(gld, text, x, y, z, clr);
                } else {
                        final Font font = gld.getFont();
                        final GlyphVector gv = gld.getFont().createGlyphVector(new FontRenderContext(font.getTransform(), true, false), text);
                        for (int i = 0; i < gv.getNumGlyphs(); i++) {
                                char glyphChar = text.charAt(i);
                                Rectangle2D glyphBounds = gv.getGlyphOutline(i).getBounds2D();
                                boolean newGlyph = !_GLcharacterMap.containsKey(glyphChar);
                                Map<Font, Map<Integer, Integer>> fontMap = null;
                                if (!newGlyph) {
                                        fontMap = _GLcharacterMap.get(glyphChar);
                                        newGlyph = !fontMap.containsKey(font);
                                        if (!newGlyph) {
                                                newGlyph = (_GLgetLoadState(fontMap.get(font).get(font.getSize())) & _GLLOADSTATE_Cleared) != 0;
                                        } else {
                                                fontMap.put(font, new HashMap<Integer, Integer>());
                                        }
                                }
                                fontMap = fontMap == null ? new HashMap<Font, Map<Integer, Integer>>(Collections.singletonMap(font, new HashMap<Integer, Integer>())) : fontMap;
                                if (newGlyph) {
                                        Shape glyph = gv.getGlyphOutline(i, -(float) glyphBounds.getX(), -(float) glyphBounds.getY());
                                        GLHandler.sTex.genVRAMBuffersMap(1);
                                        Sprite textCanvas = new Sprite();
                                        textCanvas.setBounds(glyphBounds.getBounds());
                                        textCanvas.setLocation((int) Math.round(glyphBounds.getX() + x), (int) Math.round(glyphBounds.getY() + y));
                                        textCanvas.runValidate();
                                        textCanvas.clearImageGraphics();
                                        Graphics2D g = Sprite.wrapRendering(textCanvas.getImage(gld).getGraphics());
                                        g.setFont(font);
                                        g.fill(glyph);
                                        g.draw(glyph);
                                        g.dispose();
                                        textCanvas.updateTexData(GL11.GL_LUMINANCE_ALPHA);
                                        SpriteGLHandler sp = _GLloadSprite(textCanvas, (RenderingSceneGL) gld);
                                        Sprite._GLRenderSprite(gld, true, sp, textCanvas.getBounds(), z, 0, new Point(0, 0), clr, 0, null, null, null);
                                        fontMap.get(font).put(font.getSize(), textCanvas.hashCode());
                                        _GLcharacterMap.put(glyphChar, fontMap);
                                } else {
                                        Rectangle bounds = new Rectangle((int) Math.round(glyphBounds.getX() + x), (int) Math.round(glyphBounds.getY() + y), (int) glyphBounds.getWidth(), (int) glyphBounds.getHeight());
                                        int spHash = fontMap.get(font).get(font.getSize());
                                        Sprite._GLRenderSprite(gld, true, _GLSprites.getHandler(spHash), bounds, z, 0, new Point(0, 0), clr, 0, null, null, null);
                                }
                        }
                }
        }

        /**
         * @param gld
         * @param x
         * @param y
         * @param radius
         * @param resolutionFaces
         * @param fill
         * @deprecated use GLGeom
         */
        public static void _GLRenderCircle(RenderingSceneGL gld, float x, float y, float radius, int resolutionFaces, boolean fill) {
                GLGeom._GLRenderCircle(gld, 0, false, x, y, -1, radius, resolutionFaces, 0.2F, fill);
        }

        private static int __GLgetLoadState(int hash) {
                if (GLHandler.sTex.isTextureLoaded(hash)) {
                        if (DebugMap._getInstance().isDebugLevelEnabled(DBUG_RENDER)) {
                                System.out.println("GL texture " + hash + " is loaded. " + (GLHandler.sTex.isTexture3D(hash) ? " (3D- or Animation-texture) " : " (Sprite " + Sprite._GLgetSpriteHash(hash) + ")"));
                        }
                        return _GLLOADSTATE_Loaded;
                }
                if (_GLSprites.hasHandler(hash)) {
                        SpriteGLHandler sp = _GLSprites.getHandler(hash);
                        return __GLgetLoadState(sp.getTexHash()) | (sp.state & _GLLOADSTATE_Errored);
                }
                if (_GLAnimations.hasHandler(hash)) {
                        AnimationGLHandler a = _GLAnimations.getHandler(hash);
                        if (a.isTexture3D()) {
                                return __GLgetLoadState(a.getTex3DHash()) | (a.state & _GLLOADSTATE_Errored);
                        } else {
                                synchronized (a.getFrames()) {
                                        int loadState = 0;
                                        for (int spindex : a.getFrames().keySet()) {
                                                loadState |= __GLgetLoadState(a.getSprite(spindex).getSpHash());
                                        }
                                        return (loadState == _GLLOADSTATE_Loaded ? _GLLOADSTATE_Loaded : (loadState & _GLLOADSTATE_Loading) != 0 ? _GLLOADSTATE_Loading : _GLLOADSTATE_Cleared) | (a.state & _GLLOADSTATE_Errored);
                                }
                        }
                }
                if (_GLModels.hasHandler(hash)) {
                        ModelGLHandler m = _GLModels.getHandler(hash);
                        int loadState = 0;
                        synchronized (m.getGLcache()) {
                                for (int animIndex : m.getGLcache().keySet()) {
                                        loadState |= __GLgetLoadState(m.getGLcache().get(animIndex));
                                }
                        }
                        return (loadState == _GLLOADSTATE_Loaded ? _GLLOADSTATE_Loaded : (loadState & _GLLOADSTATE_Loading) != 0 ? _GLLOADSTATE_Loading : _GLLOADSTATE_Cleared) | (m.state & _GLLOADSTATE_Errored);
                }
                return _GLLOADSTATE_Cleared;
        }

        /**
         * @param o hashcode, Sprite, Animation, Anim or Model (sub-)instances
         * are supported, too
         * @return
         */
        public static int _GLgetLoadState(Object o) {
                if (o == null) {
                        return _GLLOADSTATE_Cleared;
                }
                if (o instanceof LoadListener) {
                        return ((LoadListener) o).getLoadState();
                }
                if (o instanceof Integer) {
                        return __GLgetLoadState((Integer) o);
                }
                return __GLgetLoadState(o.hashCode());
        }

        /**
         * loading may be ASYNCHRONOUS (tileMode) or instant (standard). usethe
         * returned handler to check for the completion of the task afterwards.
         *
         * @param sp
         * @param scene
         * @return
         */
        public static SpriteGLHandler _GLloadSprite(Sprite sp, RenderingSceneGL scene) {
                return _GLloadSprite(sp, scene, PTY_SPRITE);
        }

        private static void r_loadSpriteTiles(RenderingSceneGL scene, final float pty, final int I, final int J, final int tilesX, final int tilesY, final SortedMap<Integer, Sf3Texture> cache, final Sf3Texture tex, final SpriteGLHandler ll) {
                final int n = I + J * tilesX + 1;
                if (n > tilesX * tilesY) {
                        scene.doTaskOnGL(new Runnable() {

                                @Override
                                public void run() {
                                        cache.clear();
                                        ll.loadIsComplete();
                                }
                        });
                }
                if (I == tilesX || J == tilesY) {
                        return;
                }
                scene.doTaskOnGL(new Runnable() {
                        @Override
                        public void run() {
                                try {
                                        Sf3Texture tile = cache.get(n);
                                        tile.setPty(pty);
                                        tile.setRenderFormat(tex.getRenderFormat());
                                        GLHandler._GLLoadVRAM2DasTile(tex.hashCode(), tex.getWidth(), tex.getHeight(), tile, I, J);
                                } catch (Exception ex) {
                                        if (DebugMap._getInstance().isDebuggerEnabled(RenderingSceneGL.class)) {
                                                ex.printStackTrace();
                                        }
                                        ll.loadError(ex);
                                } finally {
                                        ll.loadProgress(n, tilesX * tilesY);
                                }
                        }
                });
                r_loadSpriteTiles(scene, pty, I, J + 1, tilesX, tilesY, cache, tex, ll);
                r_loadSpriteTiles(scene, pty, I + 1, J, tilesX, tilesY, cache, tex, ll);
        }

        /**
         * ASYNCHRONOUS (method return quick, you should spot for task
         * completion using appropriate handlers)
         *
         * @param sp
         * @param scene
         * @param pty
         * @return
         */
        public static SpriteGLHandler _GLloadSprite(Sprite sp, RenderingSceneGL scene, final float pty) {
                if (sp != null) {
                        sp.setRenderingScene(scene);
                } else {
                        return null;
                }
                if ((_GLgetLoadState(sp) & _GLLOADSTATE_Cleared) == 0) {
                        return _GLSprites.getHandler(sp);
                }
                if (DebugMap._getInstance().isDebuggerEnabled(RenderingSceneGL.class)) {
                        System.out.println("loading GL Sprite " + sp.toString() + "...");
                }
                if (!sp.isResourceLoaded()) {
                        sp.loadResource();
                }
                final SpriteGLHandler ll = _GLSprites.getHandler(sp);
                if (ll instanceof RenderingSceneComponent) {
                        ((RenderingSceneComponent) ll).setRenderingScene(scene);
                }
                ll.loadBegin();
                sp.setTexPty(pty);
                sp.runValidate();
                /**
                 * should update texture if needed
                 */
                /*
                 * sp.updateTexData(); forces recreating a texture
                 */
                final SortedMap<Integer, Sf3Texture> cache = sp.getTexData();
                final Sf3Texture tex = cache.get(0);
                try {
                        if (sp.isTexTilesAvailable()) {
                                if (DebugMap._getInstance().isDebuggerEnabled(RenderingSceneGL.class)) {
                                        System.out.print("true tile mode " + sp);
                                }
                                final int tilesX = sp.getTexTilesX();
                                final int tilesY = sp.getTexTilesY();
                                if (DebugMap._getInstance().isDebuggerEnabled(RenderingSceneGL.class)) {
                                        System.out.println(" nb. of tiles (X x Y) " + tilesX + " x " + tilesY);
                                }
                                ll.loadProgress(0, tilesX * tilesY);
                                GLHandler.sTex.genVRAMBuffersMap(ll.getMax());
                                r_loadSpriteTiles(scene, pty, 0, 0, tilesX, tilesY, cache, tex, ll);
                        } else {
                                if (sp.isTileModeEnabled()) {
                                        if (DebugMap._getInstance().isDebuggerEnabled(RenderingSceneGL.class)) {
                                                System.out.println("parsed tile mode " + sp);
                                        }
                                        GLHandler._GLLoadVRAM2D(tex, SpriteIO._WRITE_TILES_DIMENSION, false, tex.hashCode(), false);
                                } else {
                                        if (DebugMap._getInstance().isDebuggerEnabled(RenderingSceneGL.class)) {
                                                System.out.println("plain mode " + sp);
                                        }
                                        GLHandler._GLLoadVRAM2D(tex, false, tex.hashCode(), false);
                                }
                                ll.loadIsComplete();
                        }
                        return ll;
                } catch (Exception ex) {
                        if (DebugMap._getInstance().isDebuggerEnabled(RenderingSceneGL.class)) {
                                ex.printStackTrace();
                        }
                        ll.loadError(ex);
                        return ll;
                }
        }

        /**
         * ASYNCHRONOUS (method return quick, you should spot for task
         * completion using appropriate handlers)
         *
         * @param sp
         * @param scene
         * @param pty
         * @return
         */
        public static AnimationGLHandler _GLloadAnim(Animation sp, RenderingSceneGL scene, float pty) {
                return _GLloadAnim(sp, scene, pty, Collections.synchronizedList(new ArrayList<LoadListener2>()));
        }

        /**
         * ASYNCHRONOUS (method return quick, you should spot for task
         * completion using appropriate handlers)
         *
         * @param sp
         * @param scene
         * @param pty
         * @param loadlistener
         * @return
         */
        public static AnimationGLHandler _GLloadAnim(Animation sp, final RenderingSceneGL scene, final float pty, final List<LoadListener2> loadlistener) {
                if (sp != null) {
                        sp.setRenderingScene(scene);
                } else {
                        return null;
                }
                if ((_GLgetLoadState(sp) & _GLLOADSTATE_Cleared) == 0) {
                        return _GLAnimations.getHandler(sp);
                }
                if (!sp.isResourceLoaded()) {
                        sp.loadResource();
                }
                if (DebugMap._getInstance().isDebuggerEnabled(RenderingSceneGL.class)) {
                        System.out.println("loading GL Animation " + sp.toString() + "...");
                }
                final SortedMap<Integer, Sprite> frames = sp.accessSynchCache();
                final AnimationGLHandler anim = _GLAnimations.getHandler(sp);
                anim.setTexPty(pty);
                loadlistener.add(anim);
                synchronized (loadlistener) {
                        for (LoadListener2 ll : loadlistener) {
                                if (ll instanceof RenderingSceneComponent) {
                                        ((RenderingSceneComponent) ll).setRenderingScene(scene);
                                }
                                ll.loadBegin();
                                ll.loadProgress(0, anim.length() - 1);
                        }
                }
                Sprite sprite = frames.get(0);
                sprite.setRenderingScene(scene);
                sprite.setTexPty(anim.getTexPty());
                sprite.runValidate();
                Sf3Texture tex = sprite.getTexData().get(0);
                /**
                 * always try texture 3d mode (for smoother playback)
                 */
                if (tex.getWidth() <= GLHandler._GLgetMax3DTextureSize() && tex.getHeight() <= GLHandler._GLgetMax3DTextureSize()) {
                        anim.setTexture3D(true);
                }
                /**
                 * new texture 3d, sprites textures will be added subsequently
                 */
                final Sf3Texture3D tex3D = anim.isTexture3D() ? Sf3Texture3D._loadTexture3D(anim.getTex3DHash(), tex, anim.length()) : null;
                /*
                 * if (anim.isTexture3D()) { GLHandler._GLUnloadVRAM3D(tex3D); }
                 */
                try {
                        if (anim.isTexture3D()) {
                                GLHandler._GLLoadVRAM3D(tex3D, false, tex3D.hashCode());
                        }
                        synchronized (frames) {
                                for (int f : frames.keySet()) {
                                        final int spriteFrame = f;
                                        scene.doTaskOnGL(new Runnable() {
                                                @Override
                                                public void run() {
                                                        Sprite sprite = frames.get(spriteFrame);
                                                        if (sprite != null) {
                                                                sprite.setTexPty(anim.getTexPty());
                                                                sprite.setRenderingScene(scene);
                                                                sprite.runValidate();
                                                                try {
                                                                        if (anim.isTexture3D()) {
                                                                                _GLSprites.getHandler(sprite);
                                                                                /**
                                                                                 * add
                                                                                 * sprites
                                                                                 * tex
                                                                                 * to
                                                                                 * the
                                                                                 * texture
                                                                                 * 3d
                                                                                 * buffer
                                                                                 */
                                                                                if (!GLHandler.sTex.hasItem(tex3D.hashCode())) {
                                                                                        throw new IllegalStateException("unknown texture 3D");
                                                                                }
                                                                                GLHandler._GLLoadVRAM3D(sprite.getTexData().get(0), tex3D.hashCode(), spriteFrame);
                                                                                anim.getSprite(spriteFrame).loadIsComplete();
                                                                        } else {
                                                                                _GLloadSprite(sprite, scene);
                                                                        }
                                                                } catch (Exception ex) {
                                                                        if (DebugMap._getInstance().isDebuggerEnabled(RenderingSceneGL.class)) {
                                                                                ex.printStackTrace();
                                                                        }
                                                                        synchronized (loadlistener) {
                                                                                for (LoadListener2 ll : loadlistener) {
                                                                                        ll.loadError(ex);
                                                                                }
                                                                        }
                                                                }
                                                        }
                                                        synchronized (loadlistener) {
                                                                for (LoadListener2 ll : loadlistener) {
                                                                        ll.loadProgress(spriteFrame, anim.length() - 1);
                                                                }
                                                        }
                                                        if (spriteFrame == anim.length() - 1) {
                                                                synchronized (loadlistener) {
                                                                        for (LoadListener2 ll : loadlistener) {
                                                                                ll.loadIsComplete();
                                                                        }
                                                                }
                                                                if (DebugMap._getInstance().isDebuggerEnabled(RenderingSceneGL.class)) {
                                                                        System.out.println("finished loading GL AnimationGLHandler " + anim.toString());
                                                                }
                                                        }
                                                }
                                        });
                                }
                        }
                } catch (Exception ex) {
                        if (DebugMap._getInstance().isDebuggerEnabled(RenderingSceneGL.class)) {
                                ex.printStackTrace();
                        }
                        synchronized (loadlistener) {
                                for (LoadListener2 ll : loadlistener) {
                                        ll.loadError(ex);
                                }
                        }
                } finally {
                        return anim;
                }
        }

        /**
         * * ASYNCHRONOUS (method return quick, you should spot for task
         * completion using appropriate handlers)
         *
         * @param sp
         * @param scene
         * @return
         */
        public static ModelGLHandler _GLloadModel(Model sp, RenderingSceneGL scene) {
                return _GLloadModel(sp, scene, Collections.synchronizedList(new ArrayList<LoadListener2>()));
        }

        /**
         * ASYNCHRONOUS (method return quick, you should spot for task
         * completion using appropriate handlers)
         *
         * @param sp
         * @param scene
         * @param loadlisteners
         * @return
         */
        public static ModelGLHandler _GLloadModel(Model sp, final RenderingSceneGL scene, final List<LoadListener2> loadlisteners) {
                if (sp != null) {
                        sp.setRenderingScene(scene);
                } else {
                        return null;
                }
                if ((_GLgetLoadState(sp) & _GLLOADSTATE_Cleared) == 0) {
                        return _GLModels.getHandler(sp);
                }
                if (!sp.isResourceLoaded()) {
                        sp.loadResource();
                }
                if (DebugMap._getInstance().isDebuggerEnabled(RenderingSceneGL.class)) {
                        System.out.print("loading GL Model " + sp.toString() + "...");
                }
                final SortedMap<Integer, Animation> anims = sp.accessSynchCache();
                if (DebugMap._getInstance().isDebuggerEnabled(RenderingSceneGL.class)) {
                        System.out.print("cache size " + anims.keySet().size() + "...");
                }
                ModelGLHandler modelHdr = _GLModels.getHandler(sp);
                loadlisteners.add(modelHdr);
                synchronized (loadlisteners) {
                        for (LoadListener2 ll : loadlisteners) {
                                if (ll instanceof RenderingSceneComponent) {
                                        ((RenderingSceneComponent) ll).setRenderingScene(scene);
                                }
                                ll.loadBegin();
                                ll.loadProgress(0, anims.keySet().size() - 1);
                        }
                }
                if (DebugMap._getInstance().isDebuggerEnabled(RenderingSceneGL.class)) {
                        System.out.print(" ModelGLHandler size " + modelHdr.getGLcache().keySet().size() + "...");
                }
                synchronized (anims) {
                        for (final int a : anims.keySet()) {
                                final Animation anim = anims.get(a);
                                anim.setRenderingScene(scene);
                                if (anim != null) {
                                        List<LoadListener2> lls = Collections.synchronizedList(new ArrayList<LoadListener2>());
                                        lls.add(new LoadAdapter() {
                                                @Override
                                                public void loadIsComplete() {
                                                        super.loadIsComplete();
                                                        synchronized (loadlisteners) {
                                                                for (LoadListener2 ll : loadlisteners) {
                                                                        ll.loadProgress(a, anims.keySet().size() - 1);
                                                                        if (a == (anims.keySet().size() - 1)) {
                                                                                ll.loadIsComplete();
                                                                        }
                                                                }
                                                        }
                                                }

                                                @Override
                                                public int hashLinkToGLObject() {
                                                        return anim.hashCode();
                                                }

                                                @Override
                                                public void loadError(Object error) {
                                                        super.loadError(error);
                                                        synchronized (loadlisteners) {
                                                                for (LoadListener2 ll : loadlisteners) {
                                                                        ll.loadError(error);
                                                                }
                                                        }
                                                }
                                        });
                                        _GLloadAnim(anim, scene, PTY_MODEL, lls);
                                }
                                if (DebugMap._getInstance().isDebuggerEnabled(RenderingSceneGL.class)) {
                                        System.out.println("loading GL Model anim : " + anim + "...");
                                }
                        }
                }
                return modelHdr;
        }

        /**
         * IMMEDIATE (/= asynchronous)
         *
         * @param sp
         */
        public static void _GLunloadSprite(SpriteGLHandler sp) {
                if ((_GLgetLoadState(sp) & _GLLOADSTATE_Loaded) != 0) {
                        GLHandler._GLUnloadVRAM(sp.getTexHash());
                        sp.unloadIsComplete();
                }
        }

        /**
         * IMMEDIATE (/= asynchronous)
         *
         * @param sp
         */
        public static void _GLunloadAnim(AnimationGLHandler sp) {
                if ((_GLgetLoadState(sp) & _GLLOADSTATE_Loaded) != 0) {
                        SortedMap<Integer, SpriteGLHandler> frames = sp.getFrames();
                        sp.unloadBegin();
                        synchronized (frames) {
                                for (SpriteGLHandler f : frames.values()) {
                                        _GLunloadSprite(f);
                                }
                        }
                        sp.unloadIsComplete();
                        _GLAnimations.handlers.remove(sp.getAnimHash());
                }
        }

        /**
         * IMMEDIATE (/= asynchronous)
         *
         * @param modelHash
         */
        public static void _GLunloadModel(ModelGLHandler modelHash) {
                if ((_GLgetLoadState(modelHash) & _GLLOADSTATE_Loaded) != 0) {
                        synchronized (modelHash.getGLcache()) {
                                for (int a : modelHash.getGLcache().keySet()) {
                                        _GLunloadAnim(_GLAnimations.getHandler(modelHash.getGLcache().get(a)));
                                }
                        }
                        _GLModels.handlers.remove(modelHash.getModelHash());
                }
        }

        /**
         * this method is equivalent to {@linkplain GLFX#_GLEndFX()}
         *
         * @deprecated use {@link GLFX}
         */
        public static void _GLflush() {
                GLFX._GLEndFX();
        }

        /**
         * call a rendering method when all fx are finished so that it doesn't
         * overlaps 1st and 2nd passes fx rendering <b>PLEASE USE THIS METHOD
         * whenever you want to do personalized GL rendering not available in
         * the current JXA _GL_ methods ! <i>To do so, encapsulate your GL...
         * calls into a method that you call with _GLpushRender and it will be
         * added on the rendering stack.</i></b> NOTICE : your encapsulation
         * must be declared "public" ortherwise the Reflexion API cannot access
         * the method you want to invoke.<br>
         *
         * @param method
         * @param objectArgs
         * @param targetArg
         * @param classArgs
         * @throws Exception
         * @see GLFX#_GLpushRender(FloatBuffer, String, Object, Object[],
         * Class[])
         * @see #_GL_getTopOftheStackMX(int)
         * @deprecated use {@link GLFX}
         */
        public static void _GLrender(String method, Object targetArg, Object[] objectArgs, Class[] classArgs) throws Exception {
        }
        /**
         * GL tasks stack
         */
        protected final Stack<Runnable> GLTasksStack = new Stack<Runnable>();
        protected boolean selfLock = false;
        private int _GL_NEARVAL = 10;
        private int _GL_FARVAL = 100;
        private int _GL_CAMZ = 0;
        /**
         *
         */
        protected List<LoadListener> GLloaders = (List<LoadListener>) _GLregCollectionOfLinks(Collections.synchronizedList(new ArrayList<LoadListener>()));
        private GameActionLayer gl_DEFAULT_BACK = new GameActionLayer("DEFAULT_BACK") {
                @Override
                protected void doLoad() {
                        super.doLoad();
                        loading_logo.loadResource();
                }

                @Override
                protected void doLoadOnGL(RenderingSceneGL context) {
                        super.doLoadOnGL(context);
                        _GLloadAnim(loading_logo, RenderingSceneGL.this, PTY_ANIM);
                }

                @Override
                protected void renderToGL(RenderingSceneGL context) {
                        super.renderToGL(context);
                        if (isLoading() && !loading_logo_isPaintedOnFrontLayer) {
                                AnimationGLHandler a = _GLAnimations.getHandler(loading_logo);
                                a.setRealTimeLength(1000);
                                a.setFadersEnabled(true);
                                a.setReverseEnabled(false);
                                double z = 1;
                                Dimension d = loading_logo.getBounds().getSize();
                                d.width = (int) Math.round(_GLZRatio(z) * d.width);
                                d.height = (int) Math.round(_GLZRatio(z) * d.height);
                                __GLdrawLogoAnimation(RenderingSceneGL.this, a, z, d, loading_logo_hpos, loading_logo_vpos, 0, new Point(0, 0), Color.GREEN, 0, null, null, null);
                        }
                }
        };
        private GameActionLayer gl_DEFAULT_FRONT = new GameActionLayer("DEFAULT_FRONT") {
                /*
                 * Animation opengl = null;
                 */
                @Override
                protected void doLoad() {
                        super.doLoad();
                }

                @Override
                protected void doLoadOnGL(RenderingSceneGL context) {
                        super.doLoadOnGL(context);
                        _GLloadAnim(loading_logo, RenderingSceneGL.this, PTY_ANIM);
                }

                @Override
                protected void doUnload() {
                        super.doUnload();
                        loading_logo_isPaintedOnFrontLayer = false;
                }

                @Override
                protected void renderToGL(RenderingSceneGL canvas) {
                        if (isDebugEnabled()) {
                                System.out.println("///////////////////// renderGL-mode");
                        }
                        try {
                                FloatBuffer color = BufferIO._wrapf(getForeground().getRGBComponents(new float[4]));
                                FloatBuffer currentColor = GLHandler._GLgetCurrentColor();
                                GL11.glColor4f(color.get(0), color.get(1), color.get(2), color.get(3));
                                if (keyEvent && drawKeyEventsEnabled) {
                                        GLGeom._GLRenderCircle(canvas, 0, true, 20, 20, -1, 15, 50, 1, getCtrlHDR().isKeyPressed());
                                }
                                GL11.glColor4f(currentColor.get(0), currentColor.get(1), currentColor.get(2), currentColor.get(3));
                                ticker = !ticker;
                                /**
                                 * HUD RENDERING (orthogonal proj)
                                 */
                                hud_helpcommands.setBounds(0, 0, getWidth(), getHeight(), -1.5);
                                hud_debugActivity.setBounds(0, 0, getWidth(), getHeight(), -2.0);
                                /**
                                 * BEGIN ORTHO PROJ
                                 *
                                 * GLHandler._GLsetProjMX(true,
                                 * canvas.getSize(), _GL_NEARVAL, _GL_FARVAL);
                                 */
                                hud_debugActivity.setEnabled((RenderingSceneGL.this.mode & options.MODE_EXT_DEBUG_ACTIVITY.bitMask()) != 0);
                                if (hud_debugActivity.isEnabled()) {
                                        /**
                                         * mem scratches
                                         */
                                        MemScratch[] scratches = new MemScratch[]{GLHandler.sTex, GLHandler.sVbo, GLHandler.sPbo};
                                        for (MemScratch ms : scratches) {
                                                hud_debugActivity.pushContent(ms.toString(), ms.getClass().getSimpleName() + " : " + ms.getItemScratchSize() + "/" + ms.getItemScratchCapacity(), TextLayout.POS.RIGHT, TextLayout.POS.BOTTOM, true);
                                        }
                                        /**
                                         * loadhandlers
                                         */
                                        List<GLObjectHandler<? extends LoadAdapter>> hdlr = new ArrayList<GLObjectHandler<? extends LoadAdapter>>();
                                        hdlr.add(_GLModels);
                                        hdlr.add(_GLAnimations);
                                        hdlr.add(_GLSprites);
                                        for (int i = 0; i < hdlr.size(); i++) {
                                                GLObjectHandler<? extends LoadAdapter> gl = hdlr.get(i);
                                                synchronized (gl.handlers) {
                                                        for (LoadAdapter l : gl.handlers.values()) {
                                                                String loadingState = null;
                                                                String type = l.getClass().getSimpleName();
                                                                if ((l.getLoadState() & _GLLOADSTATE_Loading) != 0) {
                                                                        loadingState = "loading ";
                                                                        synchronized (RenderingScene.spritesHashesBuffer) {
                                                                                for (String name : RenderingScene.spritesHashesBuffer.keySet()) {
                                                                                        if (l.hashLinkToGLObject() == RenderingScene.spritesHashesBuffer.get(name)) {
                                                                                                loadingState += name + " ";
                                                                                                break;
                                                                                        }
                                                                                }
                                                                        }
                                                                }
                                                                if ((l.getLoadState() & _GLLOADSTATE_Errored) != 0) {
                                                                        loadingState = "!error occured (" + l.getError() + ") ";
                                                                }
                                                                if (loadingState != null) {
                                                                        hud_debugActivity.pushContent("Handler-" + i, loadingState + type + " " + l.hashLinkToGLObject() + " : " + l.getVal() + "/" + l.getMax(), TextLayout.POS.RIGHT, TextLayout.POS.TOP, true);
                                                                }
                                                        }
                                                }
                                        }
                                        hud_debugActivity.GLbegin();
                                        hud_debugActivity.GLrenderContents(canvas);
                                        hud_debugActivity.GLend();
                                }
                                hud_helpcommands.setEnabled(RenderingScene._paintCommandsEnabled);
                                if (hud_helpcommands.isEnabled()) {
                                        hud_helpcommands.updateContent("_HELP_debugActivity", "[F3] : " + ((RenderingSceneGL.this.mode & options.MODE_EXT_DEBUG_ACTIVITY.bitMask()) != 0 ? "hide" : "show") + " loading activity");
                                        hud_helpcommands.updateContent("_HELP_rdrMode", "[F12] rendering" + ((ticker) ? "-" : " ") + (((RenderingSceneGL.this.mode & (options.MODE_RENDER_GL.bitMask() | options.MODE_RENDER_HARD.bitMask())) != 0) ? "HARD" : "SOFT") + ((ticker) ? "-" : " ") + ((RenderingSceneGL.this.mode & options.MODE_EXT_FORCEVRAM.bitMask()) != 0 ? " VRAM Sprites" : " Buffered Sprites"));
                                        hud_helpcommands.updateContent("_HELP_rdrOptions", "[F11] on/off, [F4] full-screen, [F9] multi-threading : " + ((RenderingSceneGL.this.mode & options.MODE_EXT_MULTITHREADING.bitMask()) == 0 ? "off" : "on") + ", [F10] ImageIO cache : " + ((ImageIO.getUseCache()) ? "on" : "off"));
                                        hud_helpcommands.updateContent("_HELP_gcoScroll", isConsoleEnabled() ? "[PGUp/PGDown] console scroll up/down" : "");
                                        hud_helpcommands.updateContent("_HELP_rdrPipe", getWidth() + "x" + getHeight() + "@ " + displayMode.getRefreshRate() + " Hz " + displayMode.getBitDepth() + " bits " + (RenderingScene.openglEnabled ? "w/ OpenGL" : RenderingScene.d3dEnabled ? "w/ Direct3D" : "w/ Sun Micr.") + " pipeline");
                                        hud_helpcommands.pushContent("_GPU_vendor", "GPU : " + GLHandler._GL_readGLVendorName(), TextLayout.POS.RIGHT, TextLayout.POS.TOP, true);
                                        hud_helpcommands.pushContent("_GPU_name", GLHandler._GL_readGLRendererName(), TextLayout.POS.RIGHT, TextLayout.POS.TOP, true);
                                        hud_helpcommands.pushContent("_GPU_version", " v." + GLHandler._GL_readGLVersionName(), TextLayout.POS.RIGHT, TextLayout.POS.TOP, true);
                                        hud_helpcommands.GLbegin();
                                        hud_helpcommands.GLrenderContents(canvas);
                                        hud_helpcommands.GLend();
                                }
                                /**
                                 * GLend hud_helpcommands
                                 */
                                if (isConsoleEnabled()) {
                                        GUIConsoleOutput console = getGCO();
                                        if (console != null) {
                                                console.setAlpha(0.5F);
                                                int x = (int) ((float) (getWidth() - console.getBounds().getWidth()) / 2.0F);
                                                int y = (int) ((float) (getHeight() - console.getBounds().getHeight()) / 2.0F);
                                                console.setLocation(x, y);
                                                console.GLdraw(canvas, -1);
                                        }
                                }
                                /*
                                 * GLHandler._GLsetProjMX(canvas.isOrthoProj(),
                                 * canvas.getSize(), _GL_NEARVAL, _GL_FARVAL);
                                 */
                                /**
                                 * END ORTHO PROJ
                                 */
                                if (drawFPSEnabled) {
                                        Dimension size = new Dimension(32, 32);
                                        SpriteGLHandler fpsCnt = _GLgetLogo(canvas, "/net/sf/jiga/xtended/impl/fpsCnt.png", size);
                                        Rectangle fpsCntBds = new Rectangle(size);
                                        fpsCntBds.setLocation(32, canvas.getHeight() - 32);
                                        Sprite._GLRenderSprite(canvas, fpsCnt, fpsCntBds, -1, GLFX.gfx.FX_DOWNREFLEXION_EXT, new Point(0, getHeight()), null, 0, null, null, null);
                                        Dimension dim = new Dimension(32, 32);
                                        SpriteGLHandler fpsVal = _GLgetLogo(canvas, "/net/sf/jiga/xtended/impl/fpsVal.png", dim);
                                        int ref = displayMode.getRefreshRate();
                                        if (ref == 0 || ref == DisplayMode.REFRESH_RATE_UNKNOWN) {
                                                ref = 60;
                                        }
                                        float val = NumberFormat.getInstance().parse(getFPS()).floatValue();
                                        double rotation = (double) (-3.0F * (180.0F / 2.0F) + 360.0F * (val / ((float) ref != 0 ? ref : val)));
                                        Rectangle fpsValBds = new Rectangle(dim);
                                        fpsValBds.setLocation(32, canvas.getHeight() - 32);
                                        Sprite._GLRenderSprite(canvas, fpsVal, fpsValBds, -1, GLFX.gfx.FX_DOWNREFLEXION_EXT, new Point(0, getHeight()), null, GLHandler._GL_TRANSFORM_ROTATE_BIT, null, BufferIO._wrapd(new double[]{rotation}), null);
                                        String fpsStr = getFPS();
                                        /*
                                         * _GLRenderText2D(canvas, fpsStr, fpsValBds.x, fpsValBds.y,
                                         * Color.WHITE);
                                         */
                                }
                                /**
                                 * Loading State animation
                                 */
                                if (isLoading()) {
                                        loading_logo_isPaintedOnFrontLayer = true;
                                        AnimationGLHandler a = _GLAnimations.getHandler(loading_logo);
                                        a.setFadersEnabled(true);
                                        a.setReverseEnabled(false);
                                        a.setRealTimeLength(1000);
                                        __GLdrawLogoAnimation(RenderingSceneGL.this, a, -1, loading_logo.getBounds().getSize(), loading_logo_hpos, loading_logo_vpos, 0, new Point(0, 0), Color.GREEN, 0, null, null, null);
                                }
                                /**
                                 *
                                 */
                                if (drawLogosEnabled) {
                                        _GLdrawLogo(canvas, logo_java_file, logo_java_size, TextLayout.POS.CENTER, TextLayout.POS.BOTTOM, GLFX.gfx.FX_SHADOW.bit() | GLFX.gfx.FX_BOTTOM.bit(), new Point(0, getHeight() - 1), null, 0, null, null, null);
                                        logo_java_size = new Dimension(53, 53);
                                        _GLdrawLogo(canvas, logo_lwjgl_file, logo_java_size, TextLayout.POS.RIGHT, TextLayout.POS.BOTTOM, GLFX.gfx.FX_SHADOW.bit() | GLFX.gfx.FX_BOTTOM.bit(), new Point(0, getHeight() - 1), null, 0, null, null, null);
                                        AnimationGLHandler a = _GLgetLogoAnimation(canvas, "/net/sf/jiga/xtended/impl/opengl", 0, 29, "", ".png", new Dimension(64, 30));
                                        a.setRealTimeLength(2500);
                                        a.setReverseEnabled(true);
                                        __GLdrawLogoAnimation(canvas, a, -1, new Dimension(64, 30), TextLayout.POS.RIGHT, TextLayout.POS.TOP, GLFX.gfx.FX_SHADOW.bit() | GLFX.gfx.FX_BOTTOM.bit(), new Point(0, getHeight() - 1), null, 0, null, null, null);
                                }
                        } catch (InterruptedException ex) {
                                if (isDebugEnabled()) {
                                        ex.printStackTrace();
                                }
                        } catch (ParseException ex) {
                                if (isDebugEnabled()) {
                                        ex.printStackTrace();
                                }
                        }
                }
        };

        @Override
        protected GameActionLayer defaultRenderingLayer(int mode, int layer) {
                if ((mode & options.MODE_RENDER_GL.bitMask()) == 0) {
                        return super.defaultRenderingLayer(mode, layer);
                }
                GameActionLayer a = null;
                switch (layer) {
                        case LAYER_BACK:
                                a = gl_DEFAULT_BACK;
                                break;
                        case LAYER_FRONT:
                                a = gl_DEFAULT_FRONT;
                                break;
                        default:
                                a = new GameActionLayer("N/A") {
                                };
                                break;
                }
                return a;
        }

        public RenderingSceneGL() {
                this(new ContextAttribs());
        }

        public RenderingSceneGL(ContextAttribs contextAttribs) {
                super();
                this.contextAttribs = contextAttribs;
        }

        public RenderingSceneGL(GraphicsDevice device) {
                this(device, new ContextAttribs());
        }

        public RenderingSceneGL(GraphicsDevice device, ContextAttribs contextAttribs) {
                super(device);
                this.contextAttribs = contextAttribs;
        }

        public RenderingSceneGL(Dimension size, GraphicsDevice device) {
                this(size, device, new ContextAttribs());
        }

        public RenderingSceneGL(Dimension size, GraphicsDevice device, ContextAttribs contextAttribs) {
                super(size, device);
                this.contextAttribs = contextAttribs;
        }

        /**
         * re-implemented to return correct values in fullscreen Opengl Display
         * mode
         */
        @Override
        public int getWidth() {
                int w = super.getWidth();
                /**
                 * selfLock is checked and avoids an internal loop while Display
                 * requests the canvas size
                 */
                if (isLWJGLAccel() && isFullscreen() && !selfLock) {
                        selfLock = true;
                        w = Display.getWidth();
                }
                selfLock = false;
                return w;
        }

        /**
         * re-implemented to return correct values in fullscreen Opengl Display
         * mode
         */
        @Override
        public int getHeight() {
                int h = super.getHeight();
                /**
                 * selfLock is checked and avoids an internal loop while Display
                 * requests the canvas size
                 */
                if (isLWJGLAccel() && isFullscreen() && !selfLock) {
                        selfLock = true;
                        h = Display.getHeight();
                }
                selfLock = false;
                return h;
        }

        /**
         * re-implemented to return correct values in fulscreen Opengl Display
         * mode
         */
        @Override
        public Dimension getSize(Dimension rv) {
                rv = super.getSize(rv);
                rv.setSize(getWidth(), getHeight());
                return rv;
        }

        /**
         * re-implemented to return correct values in fulscreen Opengl Display
         * mode
         */
        @Override
        public Dimension getSize() {
                Dimension d = super.getSize();
                d.setSize(getWidth(), getHeight());
                return d;
        }

        /**
         * re-implemented to return correct values in fulscreen Opengl Display
         * mode
         */
        @Override
        public Rectangle getBounds() {
                Rectangle r = super.getBounds();
                r.setSize(getWidth(), getHeight());
                return r;
        }

        /**
         * re-implemented to return correct values in fullscreen Opengl Display
         * mode
         */
        @Override
        public int getX() {
                int x = super.getX();
                /**
                 * selfLock is checked and avoids an internal loop while Display
                 * requests the canvas size
                 */
                if (isLWJGLAccel() && isFullscreen() && !selfLock) {
                        selfLock = true;
                        x = Display.getX();
                }
                selfLock = false;
                return x;
        }

        /**
         * re-implemented to return correct values in fullscreen Opengl Display
         * mode
         */
        @Override
        public int getY() {
                int y = super.getY();
                /**
                 * selfLock is checked and avoids an internal loop while Display
                 * requests the canvas size
                 */
                if (isLWJGLAccel() && isFullscreen() && !selfLock) {
                        selfLock = true;
                        y = Display.getY();
                }
                selfLock = false;
                return y;
        }

        /**
         * re-implemented to return correct values in fulscreen Opengl Display
         * mode
         */
        @Override
        public Rectangle getBounds(Rectangle rv) {
                Rectangle r = super.getBounds(rv);
                r.setSize(getWidth(), getHeight());
                return r;
        }

        /**
         * re-implemented to return correct values in fulscreen Opengl Display
         * mode
         */
        @Override
        public Point getLocation() {
                return new Point(getX(), getY());
        }

        /**
         * re-implemented to return correct values in fulscreen Opengl Display
         * mode
         */
        @Override
        public Point getLocation(Point rv) {
                rv = super.getLocation(rv);
                rv.setLocation(getLocation());
                return rv;
        }

        /**
         * for a realistic rendering, nearval should be half of the farval
         * value, i.e. the truncated pyramid frustum keeps objects look real
         * even if near the near clipplane e.g. near 500 / far 1000
         *
         * @see http://www.songho.ca/opengl/gl_projectionmatrix.html
         * @default 10
         */
        public void setGL_NEARVAL(int _GL_NEARVAL) {
                this._GL_NEARVAL = _GL_NEARVAL;
        }

        public int getGL_NEARVAL() {
                return _GL_NEARVAL;
        }

        public int getGL_FARVAL() {
                return _GL_FARVAL;
        }

        /**
         * for a realistic rendering, nearval should be half of the farval
         * value, i.e. the truncated pyramid frustum keeps objects look real
         * even if near the near clipplane e.g. near 500 / far 1000
         *
         * @see http://www.songho.ca/opengl/gl_projectionmatrix.html
         * @default 100
         */
        public void setGL_FARVAL(int _GL_FARVAL) {
                this._GL_FARVAL = _GL_FARVAL;
        }

        /**
         * should be less or equal to zero (<= 0). {@link
         * #_GL_reshape(net.sf.jiga.xtended.impl.game.RenderingScene, boolean)}
         * steadily translates camera to the center of the viewport
         * (width/2.,height/2., -_GL_NEARVAL+_GL_CAMZ). that is 0,0,0 is the
         * up-left corner of the viewport. @default 0
         */
        public void setGL_CAMZ(int _GL_CAMZ) {
                this._GL_CAMZ = _GL_CAMZ;
        }

        public int getGL_CAMZ() {
                return _GL_CAMZ;
        }

        /**
         * -(_GL_FARVAL - _GL_NEARVAL), i.e. the Z-depth (always a negative
         * coord value) of the RenderingScene
         */
        public int getZDepth() {
                return -(_GL_FARVAL - _GL_NEARVAL);
        }

        /**
         * texture default scratch size :
         * net.sf.jiga.xtended.impl.game.rendering.properties ->
         * _GLScratchCapacity pbo default scratch size : 4 vbo default scratch
         * size : net.sf.jiga.xtended.impl.game.rendering.properties ->
         * _GLScratchCapacity
         */
        protected void initGL() {
                synchronized (vSynch) {
                        try {
                                if (!Display.isCurrent()) {
                                        Display.makeCurrent();
                                }
                                _GL_initCurrentContext(this);
                                GLHandler.sTex.freeScratch();
                                GLHandler.sPbo.freeScratch();
                                GLHandler.sVbo.freeScratch();
                                MemScratch._free();
                                MemScratch._init();
                                GLHandler.sTex.initScratch(Integer.parseInt(rb.getString("_GLScratchCapacity")));
                                /**
                                 * r and write buffers
                                 */
                                GLHandler.sPbo.initScratch(Integer.parseInt(rb.getString("_GLScratchCapacity")));
                                GLHandler.sVbo.initScratch(Integer.parseInt(rb.getString("_GLScratchCapacity")));
                        } catch (LWJGLException ex) {
                                if (isDebugEnabled()) {
                                        ex.printStackTrace();
                                }
                        }
                }
        }

        /**
         * OPENGL RENDERING
         */
        protected void paintGL() {
                try {
                        if ((mode & options.MODE_RENDER_GL.bitMask()) == 0) {
                                return;
                        }
                        screenThreadId = Thread.currentThread().getId();
                        if (!Display.isCurrent()) {
                                return;
                        }
                        GL11.glClearColor(_GLclearColor.get(0), _GLclearColor.get(1), _GLclearColor.get(2), _GLclearColor.get(3));
                        _GL_reshape(this, (mode & options.MODE_EXT_ORTHO.bitMask()) != 0);
                        if (!GLTasksStack.empty()) {
                                GLTasksStack.pop().run();
                        }
                        /*
                         * GLclearRegion screen
                         */
                        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);
                        final Monitor monitor = offscreenSynch;
                        synchronized (monitor) {
                                synchronized (GLloaders) {
                                        for (LoadListener gl : GLloaders) {
                                                if ((gl.getLoadState() & _GLLOADSTATE_Loading) != 0) {
                                                        setLoadingState(options.MODE_STATE_LOADING_GL.bitMask(), true);
                                                        break;
                                                }
                                        }
                                }
                                ActionEvent e = new ActionEvent(this, hashCode(), "offscreen tasks");
                                GLFX._GLBeginFX(true);
                                doOffscreenTasks(this, before_action_list);
                                GLFX._GLEndFX();
                                GLFX._GLBeginFX(false);
                                doOffscreenTasks(this, offscreen_action_list);
                                GLFX._GLEndFX();
                                GLFX._GLBeginFX(false);
                                doOffscreenTasks(this, after_action_list);
                                GLFX._GLEndFX();
                                monitor.notify();
                        }
                        GL11.glFlush();
                        markFPS();
                } catch (Exception ex) {
                        if (isDebugEnabled()) {
                                ex.printStackTrace();
                        }
                }
        }

        /**
         * GL draw for String (w/ Java Font)
         *
         * @param graphics
         * @param str
         * @param z ignored
         * @param vpos
         * @param hpos
         * @param newLine
         * @deprecated use {@link #hud_user}
         */
        public void GL_drawString(RenderingSceneGL graphics, String str, TextLayout.POS hpos, TextLayout.POS vpos, boolean newLine, float z) {
                __drawString(graphics, str, hpos, vpos, newLine, z);
        }

        /**
         * To CHAIN task on the rendering thread while rendering on the
         * scene.<BR>
         * a Runnable task can be posted on the GL task to be processed ONCE by
         * each rendering frame FIFO-order. <br>
         * ONE TASK MAY BE PROCESSED BY FRAME RENDERED,rendering can be slowed
         * down, depending on the time it takes for the run() method to finish.
         * PLEASE MAKE NO RENDERING HERE, because the renderContents-buffer is
         * cleared right after the execution of GL tasks ! Use a GameActionLayer
         * for rendering ! Unlike the {@linkplain #doTask(Runnable)}, the method
         * runs the task INSIDE the rendering block (actually just before it to
         * GLbegin).
         *
         * @param r
         * @see #countTasksOnGL()
         * @see #clearTasksOnGL()
         */
        public void doTaskOnGL(final Runnable r) {
                final String name = Thread.currentThread().getName();
                GLTasksStack.add(0, new Runnable() {
                        @Override
                        public void run() {
                                if (isDebugEnabled()) {
                                        System.out.println("task on GL post from " + name);
                                }
                                r.run();
                        }
                });
        }

        /**
         * counts the tasks left to proceed on the GL Tread
         *
         * @return
         * @see #doTaskOnGL(Runnable)
         */
        public int countTasksOnGL() {
                return GLTasksStack.size();
        }

        /**
         * discards tasks left to proceed on the GL Thread
         *
         * @return the amount of tasks that have been discarded
         */
        public int clearTasksOnGL() {
                int i = countTasksOnGL();
                GLTasksStack.clear();
                return i;
        }

        /**
         * returns a scaling "double" value for the specified z-depth to allow
         * binding "far" backgrounds textures to the viewport bounds width and
         * height. Multiply the returned double to the viewport width and/or
         * height to find the correct aspect ratio for plotting on the whole
         * viewport bounds !
         *
         * @param z (is the distance from the near clip-plane to object, usually
         * minus signed)
         * @return ratio corresponding to the current {@link #getGL_NEARVAL()}
         * and {@link #getGL_FARVAL()}
         */
        public double _GLZRatio(double z) {
                return Math.abs(_GL_FARVAL / (_GL_NEARVAL - z));
        }

        ContextAttribs contextAttribs;

        /**
         * CAUTION : FOR INTERNAL CALLBACK.<br>
         * all the rendering process is maintained in this
         * Runnable.<br>{@link Display#processMessages()} is run by each frame
         * before the layers are.
         * <br> Therefore anyone can run (the LWJGL handler) Keyboard or Mouse
         * functions from within a {@link GameActionLayer}.
         * <br> Of course {@link #isSwingKeyboardDisabled() } and {@link #isSwingMouseDisabled()
         * } must be disabled respectively, unless the user wants to relay on
         * Swing's Listeners.
         *
         * @return an instance that can be run on any thread
         */
        public Runnable renderGLmode() {
                return new Runnable() {
                        @Override
                        public void run() {
                                screenThreadId = Thread.currentThread().getId();
                                try {
                                        final Monitor monitor0 = vSynch;
                                        synchronized (monitor0) {
                                                rendering = true;
                                                if (isDisplayable() && (getState() == State.RUNNING)) {
                                                        if (!Display.isCreated()) {
                                                                if (!isFullscreen()) {
                                                                        Display.setParent(RenderingSceneGL.this);
                                                                }
                                                                Display.setDisplayMode(new org.lwjgl.opengl.DisplayMode(RenderingSceneGL.this.getWidth(), RenderingSceneGL.this.getHeight()));
                                                                Display.create(_GL_pixFormat, contextAttribs);
                                                                initGL();
                                                        }
                                                        /* Disable LWJGL input overlay, if Swing is available and not in fullscreen mode
                                                         */
                                                        if (!isKeyboardDisabled() && isUseLWJGLKeyboardAndMouseEnabled() && !Keyboard.isCreated()) {
                                                                Keyboard.create();
                                                        } else if (isKeyboardDisabled() || !isUseLWJGLKeyboardAndMouseEnabled()) {
                                                                Keyboard.destroy();
                                                        }
                                                        if (!isMouseDisabled() && isUseLWJGLKeyboardAndMouseEnabled() && !Mouse.isCreated()) {
                                                                Mouse.create();
                                                        } else if (isMouseDisabled() || !isUseLWJGLKeyboardAndMouseEnabled()) {
                                                                Mouse.destroy();
                                                        }
                                                        if (!Display.isCurrent()) {
                                                                Display.makeCurrent();
                                                        }
                                                        Display.processMessages();
                                                        paintGL();
                                                        Display.update(false);
                                                        /**
                                                         * WILL LOSE CONTEXT :
                                                         * it seems like the
                                                         * GLContext is lost
                                                         * when released
                                                         * (incremental index is
                                                         * 0) TODO : Try Shared
                                                         * Context to make
                                                         * GLContext survive ?
                                                         * Display.releaseContext();
                                                         */
                                                        /**
                                                         * if the
                                                         * gameactionlayer did
                                                         * not process them
                                                         */
                                                        processlLWJGLInput();
                                                }
                                        }
                                } catch (Exception ex) {
                                        if (isDebugActivityEnabled()) {
                                                ex.printStackTrace();
                                        }
                                } finally {
                                        final Monitor monitor1 = vSynch;
                                        synchronized (monitor1) {
                                                rendering = false;
                                                if (Display.isCreated() && Display.isCloseRequested()) {
                                                        stopRendering();
                                                }
                                                monitor1.notifyAll();
                                        }
                                }
                        }
                };
        }

        public void setUseLWJGLKeyboardAndMouseEnabled(final boolean b) {
                doTask(new Runnable() {

                        @Override
                        public void run() {
                                setMode(b ? (mode | options.MODE_EXT_INPUT_USE_LWJGL.bitMask()) : (mode & ~options.MODE_EXT_INPUT_USE_LWJGL.bitMask()) | options._EXT_BIT.bitMask());
                        }
                });
        }

        public boolean isUseLWJGLKeyboardAndMouseEnabled() {
                return 0 != (mode & (options.getAllOptionsMask() & options.MODE_EXT_INPUT_USE_LWJGL.bitMask()));
        }

        @Override
        public void stopRendering() {
                super.stopRendering();
                final Runnable r = new Runnable() {
                        @Override
                        public void run() {

                                try {
                                        synchronized (vSynch) {
                                                while (rendering) {
                                                        vSynch.wait();
                                                }
                                                if (Display.isCreated()) {
                                                        try {
                                                                if (Display.isCurrent()) {
                                                                        Display.destroy();
                                                                }
                                                        } catch (LWJGLException ex) {
                                                                ex.printStackTrace();
                                                        }
                                                }
                                        }
                                } catch (InterruptedException ex) {
                                        ex.printStackTrace();
                                }
                        }
                };
                doTaskOnScreen(r);
        }

        @Override
        public Object clearResource() {
                Object o = super.clearResource();
                clearTasksOnGL();
                Runnable r = new Runnable() {
                        @Override
                        public void run() {
                                hud_debugActivity.clearResource();
                                hud_helpcommands.clearResource();
                                getGCO().clearResource();
                                synchronized (_GLSprites.handlers) {
                                        for (int sp : _GLSprites.handlers.keySet()) {
                                                SpriteGLHandler hdr = _GLSprites.getHandler(sp);
                                                hdr.unloadIsComplete();
                                        }
                                }
                                synchronized (_GLAnimations.handlers) {
                                        for (int a : _GLAnimations.handlers.keySet()) {
                                                AnimationGLHandler hdr = _GLAnimations.getHandler(a);
                                                hdr.unloadIsComplete();
                                        }
                                }
                                synchronized (_GLModels.handlers) {
                                        for (int m : _GLModels.handlers.keySet()) {
                                                ModelGLHandler hdr = _GLModels.getHandler(m);
                                                hdr.unloadIsComplete();
                                        }
                                }
                                synchronized (_GLmapOfLinks) {
                                        for (Map c : _GLmapOfLinks) {
                                                c.clear();
                                        }
                                }
                                synchronized (_GLcollectionOfLinks) {
                                        for (Collection c : _GLcollectionOfLinks) {
                                                c.clear();
                                        }
                                }
                                _backBuffer.clear();
                        }
                };
                GameActionLayer._loadWorks.doLater(r);
                return o;
        }

        /**
         * (LWJGL handler) Mouse. and Keyboard.poll() are here for handling
         * RenderingScene options, essentially. It will process all resting
         * input events (from the last processed messages) and send them to the
         * {@link ControllersHandler controllers handler}.
         * <br> Any
         * {@link #addBeyondOffscreen(net.sf.jiga.xtended.impl.game.GameActionLayer) offscreene layer}
         * can process those events (Keyboard or Mouse) before it reaches this
         * point (at the time the screen is refreshed, buffer swapped).
         */
        protected void processlLWJGLInput() {
                if (Keyboard.isCreated()) {
                        while (Keyboard.next()) {
                                KLAWT k = KLAWT._findL(Keyboard.getEventKey());
                                int id = Keyboard.getEventKeyState() ? KeyEvent.KEY_PRESSED : KeyEvent.KEY_RELEASED;
                                if (isDebugActivityEnabled()) {
                                        Logger.getLogger(LOGGER_NAME).log(Level.CONFIG, "caught LWJGL Keyboard event {0}", k);
                                }
                                ctrlHDR.pushKeyEvent(new KeyEvent(RenderingSceneGL.this, id, Math.round(Keyboard.getEventNanoseconds() / 1000000L), 0, k.awtKeyCode, k.character, k.keyLocation));
                        }
                }
                if (Mouse.isCreated()) {
                        while (Mouse.next()) {
                                if (isDebugActivityEnabled()) {
                                        Logger.getLogger(LOGGER_NAME).log(Level.CONFIG, "caught LWJGL Mouse event {0};{1}", new Object[]{Mouse.getEventX(), Mouse.getEventY()});
                                }
                        }
                }
        }

}
