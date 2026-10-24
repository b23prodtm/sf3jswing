/* * RenderingScene.java
 * * Created on 28 avril 2007, 02:13 *
 * To change this template, choose Tools | Template Manager *
 and open the template in the editor. */
package net.sf.jiga.xtended.impl.game;
/*
 * import com.sun.opengl.util.j2d.TextRenderer;
 */

import java.awt.*;
import java.awt.DisplayMode;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.media.jai.GraphicsJAI;
import javax.swing.*;
import net.sf.jiga.xtended.JXAException;
import net.sf.jiga.xtended.impl.*;
import net.sf.jiga.xtended.impl.game.TextLayout.POS;
import net.sf.jiga.xtended.impl.game.gl.*;
import net.sf.jiga.xtended.impl.game.scene.ControllersHandlerImpl;
import static net.sf.jiga.xtended.impl.game.scene.Input.*;
import net.sf.jiga.xtended.impl.game.scene.State;
import static net.sf.jiga.xtended.impl.game.scene.State.*;
import net.sf.jiga.xtended.impl.game.scene.TimerState;
import net.sf.jiga.xtended.impl.system.*;
import net.sf.jiga.xtended.impl.system.input.ControllersHandler;
import net.sf.jiga.xtended.kernel.*;
import static net.sf.jiga.xtended.kernel.JXAenvUtils.*;
import net.sf.jiga.xtended.kernel.JXAenvUtils.LVL;
import net.sf.jiga.xtended.kernel.JXAenvUtils.env;
import net.sf.jiga.xtended.ui.Ant;
import net.sf.jiga.xtended.ui.SystemTrayIcon;
import net.sf.jiga.xtended.ui.UIMessage;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/**
 * This class extends a Canvas to bring the fastest rendering to Sprites as a
 * lightweight component. It refreshes itself at the current refresh rate taken
 * from the running DisplayMode. It has an internal double-buffer and can show
 * the console log above the screen layers. Type "f4" function key to switch to
 * fullscreen mode. Toggling multi-threading on and off is done by pressing
 * "f10". On some machine multi-threading does significantly improve the speed.
 * This Canvas can refresh its contents synchronously with various Action's so
 * that if the Action list changes, content will update at the same time.
 * Therefore we assume Action list as "layers", where the first added Action is
 * the back-layer and going front with further adds. Type "f12" to switch
 * between hardware accelerated rendering and software rendering (Java2D
 * rendering). <center>F1 toggles help on/off, where recent adds can be found as
 * options.</center>
 *
 * @see RenderingSceneListener#reset()
 * @author www.b23prodtm.info
 */
public class RenderingScene extends Canvas implements Resource, Threaded, Debugger, Thread.UncaughtExceptionHandler {

    /**
     * @see Ant#JXA_DEBUG_RENDER
     * @see DebugMap#setDebugLevelEnabled(boolean,
     * net.sf.jiga.xtended.kernel.Level)
     */
    public static final Level DBUG_RENDER = DebugMap._getInstance().newDebugLevel();

    static {
        DebugMap._getInstance().associateDebugLevel(RenderingScene.class, DBUG_RENDER);
        DebugMap._getInstance().setDebugLevelEnabled(_getSysBoolean(Ant.JXA_DEBUG_RENDER), DBUG_RENDER);
    }
    /**
     *
     */
    public final static ResourceBundle rb = ResourceBundle.getBundle("net.sf.jiga.xtended.impl.game.rendering");
    /**
     * the loading playerStatus switch
     */
    boolean loading = false;
    /**
     * the FPS paint mode switch
     */
    protected boolean drawFPSEnabled = Boolean.parseBoolean(rb.getString("showFPSEnabled"));

    protected Runnable renderHardmode() {
        return new Runnable() {
            @Override
            public void run() {
                screenThreadId = Thread.currentThread().getId();
                try {
                    final Monitor monitor0 = vSynch;
                    synchronized (monitor0) {
                        rendering = true;
                        Graphics2D g = null;
                        BufferStrategy strategy = null;
                        do {
                            strategy = getBufferStrategy();
                            do {
                                g = getOffscreenGraphics();
                                if (g == null) {
                                    break;
                                }
                                final Monitor monitor = offscreenSynch;
                                synchronized (monitor) {
                                    ActionEvent e = new ActionEvent(this, hashCode(), "offscreen tasks");
                                    doOffscreenTasks(g, before_action_list);
                                    doOffscreenTasks(g, offscreen_action_list);
                                    doOffscreenTasks(g, after_action_list);
                                    monitor.notify();
                                }
                                if (numBuffers < 2) {
                                    g.dispose();
                                    Graphics gd = strategy.getDrawGraphics();
                                    if (gd == null) {
                                        break;
                                    }
                                    g = Sprite.wrapRendering(gd);
                                }
                                if (g != null) {
                                    Rectangle box = getBounds();
                                    g.translate(-box.x, -box.y);
                                    g.setClip(box);
                                    g.setBackground(getBackground());
                                    g.setColor(g.getBackground());
                                    if (numBuffers < 2) {
                                        g.fill(box);
                                    }
                                    g.setColor(getForeground());
                                    draw(g, RenderingScene.this);
                                    g.dispose();
                                }
                            } while (strategy.contentsRestored());
                            if (isDisplayable()) {
                                strategy.show();
                            }
                        } while (strategy.contentsLost());
                        monitor0.notify();
                    }
                } catch (InterruptedException e) {
                    if (isDebugEnabled()) {
                        e.printStackTrace();
                    }
                } finally {
                    final Monitor monitor1 = vSynch;
                    synchronized (monitor1) {
                        rendering = false;
                        monitor1.notifyAll();
                    }
                }
            }
        };
    }

    protected Runnable renderSoftmode() {
        return new Runnable() {
            @Override
            public void run() {
                screenThreadId = Thread.currentThread().getId();
                try {
                    final Monitor monitor0 = vSynch;
                    synchronized (monitor0) {
                        rendering = true;
                        Graphics g = getGraphics();
                        if (g instanceof Graphics) {
                            update(g);
                            g.dispose();
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
                        monitor1.notifyAll();
                    }
                }
            }
        };
    }

    /**
     * to enable LWJGL acceleration support for 2D/3D at startup edit
     * net/sf/jiga/xtended/impl/game/rendering.properties or change this setting
     * and call {@linkplain #setHardwareAccel(boolean) setHardwareAccel(true)}
     */
    public static boolean lwjglEnabled = Boolean.parseBoolean(rb.getString("lwjglEnabled"));
    /**
     * true or false whether the Java2D pipeline uses openGL or not, resp. (this
     * is NOT LWJGL for 3D acceleration)
     */
    public final static boolean openglEnabled = env.JAVA_2D_OPENGL.isEnv();
    /**
     * true or false whether the Java2D pipeline uses Direct3D or not, resp.
     * (this is NOT DirectX 3D for 3D acceleration)
     */
    public final static boolean d3dEnabled = env.JAVA_2D_D3D.isEnv();

    /**
     *
     */
    protected final static BitStack bits = new BitStack();

    /**
     *
     */
    public enum options {

        /**
         *
         */
        _RENDER_MODE_BIT, /**
         *
         */
        MODE_RENDER_SOFT(_RENDER_MODE_BIT), /**
         *
         */
        MODE_RENDER_HARD(_RENDER_MODE_BIT), /**
         *
         */
        MODE_RENDER_GL(_RENDER_MODE_BIT), /**
         *
         */
        _EXT_BIT, /**
         *
         */
        MODE_EXT_ORTHO(_EXT_BIT), /**
         *
         */
        MODE_EXT_MULTITHREADING(_EXT_BIT), /**
         *
         */
        MODE_EXT_FORCEVRAM(_EXT_BIT), /**
         *
         */
        MODE_EXT_EXCLUSIVE(_EXT_BIT), /**
         *
         */
        MODE_EXT_DEBUG_ACTIVITY(_EXT_BIT), /**
         *
         */
        MODE_EXT_KEYBOARD_DISABLE(_EXT_BIT),
        /**
         *
         */
        MODE_EXT_MOUSE_DISABLE(_EXT_BIT),
        /**
         * Disable {@link ControllersHandler} Gamepad process (usually linked to
         * {@link InteractiveModel Interactive-XtendedModels}).
         */
        MODE_EXT_GAMEPAD_DISABLE(_EXT_BIT),
        /**
         * Use LWJGL Keyboard and Mouse.
         */
        MODE_EXT_INPUT_USE_LWJGL(_EXT_BIT),
        /**
         *
         */
        _STATE_BIT, /**
         *
         */
        MODE_STATE_LOADING_LAYER(_STATE_BIT), /**
         *
         */
        MODE_STATE_LOADING_GL(_STATE_BIT), /**
         *
         */
        _VIEWPORT_BIT, /**
         *
         */
        MODE_VIEWPORT_WINDOWED(_VIEWPORT_BIT), /**
         *
         */
        MODE_VIEWPORT_FULLSCREEN(_VIEWPORT_BIT);
        /**
         * associated bitMask bits
         */
        int mask;

        options(options parentOption) {
            mask = bits._newBit(parentOption.mask);
        }

        options() {
            mask = bits._newBitRange();
        }

        /**
         * @return option bit and parentOption bit OR-ed.
         */
        public int bitMask() {
            return mask;
        }

        public static int getOptionsMaskFor(options _OPTION_BIT) {
            return bits._getMask(_OPTION_BIT.bitMask());
        }

        public static int getAllOptionsMask() {
            return bits._getAllBits();
        }

    }

    /**
     *
     */
    protected int mode = ((Integer.parseInt(rb.getString("exclMode")) == 1 ? options.MODE_EXT_EXCLUSIVE.bitMask() | options.MODE_VIEWPORT_FULLSCREEN.bitMask() : Integer.parseInt(rb.getString("exclMode")) == 2 ? options.MODE_EXT_EXCLUSIVE.bitMask() | options.MODE_VIEWPORT_FULLSCREEN.bitMask() : options.MODE_VIEWPORT_WINDOWED.bitMask()) | (lwjglEnabled ? options.MODE_RENDER_GL.bitMask() : options.MODE_RENDER_SOFT.bitMask()) | (_multiThreading ? options.MODE_EXT_MULTITHREADING.bitMask() : 0)) & options.getAllOptionsMask();

    /**
     * dis/enables displaying the FPS on screen
     *
     * @param drawFPSEnabled dis/enables displaying the FPS on screen
     * @default true
     */
    public void setDrawFPSEnabled(boolean drawFPSEnabled) {
        this.drawFPSEnabled = drawFPSEnabled;
    }

    /**
     * returns true or false whether displaying FPS is enabled or not, resp.
     *
     * @return true or false
     *
     * @see #setDrawFPSEnabled(boolean)
     */
    public boolean isDrawFPSEnabled() {
        return drawFPSEnabled;
    }

    /**
     * returns the current EXCLUSIVE MODE playerStatus
     *
     * @return the current EXCLUSIVE MODE playerStatus. Do Bitwise-AND
     * comparison against {@link options#MODE_EXT_EXCLUSIVE} and/or {@link options#MODE_FULLSCREEN) or {@link options#MODE_WINDOWED) bitmask to check if exclusive mode is enabled
     *
     * @see options
     *
     * @see #setExclusiveMode(int)
     */
    public int getExclusiveMode() {
        return mode & (options.getOptionsMaskFor(options._VIEWPORT_BIT) | options.MODE_EXT_EXCLUSIVE.bitMask());
    }

    /*
     *
     * EXPERIMENTAL enables VolatileImage for Sprites. Cannot be enabled if
     * neither sun.java2d.opengl nor sun.java2.d3d is on.
     *
     * @param b
     *
     */
    public void setVRAMSpritesEnabled(final boolean b) {
        doTask(new Runnable() {
            @Override
            public void run() {
                setMode(b ? mode | (!openglEnabled && !d3dEnabled ? 0 : options.MODE_EXT_FORCEVRAM.bitMask()) : mode & ~options.MODE_EXT_FORCEVRAM.bitMask() | options._EXT_BIT.bitMask());
            }
        });
    }
    /*
     *
     * displays a small output of how many objects are in the video memory (loading activity)
     * This is used for the OpenGL context.
     *
     */

    public boolean isDebugActivityEnabled() {
        return (mode & options.MODE_EXT_DEBUG_ACTIVITY.bitMask()) != 0;
    }/*
     * Displays GL buffers loading activity
     *
     * @param b
     *
     */


    public void setDebugActivityEnabled(final boolean b) {
        doTask(new Runnable() {
            @Override
            public void run() {
                setMode(b ? mode | options.MODE_EXT_DEBUG_ACTIVITY.bitMask() : mode & ~options.MODE_EXT_DEBUG_ACTIVITY.bitMask() | options._EXT_BIT.bitMask());
            }
        });
    }

    /**
     *
     * @param arg0
     */
    public void setVSyncEnabled(final boolean arg0) {
        doTask(new Runnable() {
            @Override
            public void run() {
                org.lwjgl.opengl.Display.setVSyncEnabled(arg0);

            }
        });
    }

    /**
     *
     * @param arg0
     */
    public void setSwapInterval(final int arg0) {
        doTask(new Runnable() {
            @Override
            public void run() {
                org.lwjgl.opengl.Display.setSwapInterval(arg0);

            }
        });
    }

    /**
     *
     * @return
     */
    public boolean isVRAMSpritesEnabled() {
        return (mode & options.MODE_EXT_FORCEVRAM.bitMask()) != 0;
    }

    /**
     * returns the current renderContents mode
     * {@linkplain options#_RENDER_MODE_BIT}.
     *
     * @return
     */
    public int getRenderMode() {
        return bits._getMask(options._RENDER_MODE_BIT.bitMask()) & mode;
    }

    /**
     *
     * (this method should not be called more than required, because it alters
     * the rendering layers) NOTICE : to remove an option, do notAnd (&~) it and
     * add (|) the parentOption (not to remove the other children bits).
     */
    protected void setMode(int newMode) {
        /**
         * the ranges of options that are to be modified
         */
        int modeChangeRange = newMode & bits._getAllBitRanges();
        /**
         * only remove (~) changed bits and add (|) newMode with allbits mask
         * (&) (no parentOption bits are stored)
         */
        newMode = bits._getAllBits() & (mode & ~bits._getMask(modeChangeRange) | newMode);
        if (modeChangeRange == 0) {
            throw new JXAException("mode must include one of the range bits options");
        } else {
            String changeBitstr = "[", changeBitstrSettings = "[";
            for (options opt : options.values()) {
                if ((opt.bitMask() & modeChangeRange) != 0 && (opt.bitMask() & newMode) != 0) {
                    changeBitstrSettings += opt.toString();
                }
                if ((opt.bitMask() & modeChangeRange) == opt.bitMask()) {
                    changeBitstr += opt.toString();
                }
            }
            changeBitstr += "]";
            changeBitstrSettings += "]";
            if (isDebugEnabled()) {
                log("setting " + changeBitstr + " to " + changeBitstrSettings + " new mode settings :", LVL.SYS_NOT);
                for (options opt : options.values()) {
                    if ((opt.bitMask() & newMode) != 0) {
                        log(" " + opt.toString(), LVL.SYS_NOT);
                    }
                }
                log(".", LVL.SYS_NOT);
            }
        }
        boolean running = (state == RUNNING);

        if ((modeChangeRange & options._EXT_BIT.bitMask()) != 0) {
            /**
             * input mode change
             */
            if (0 != (newMode & (options.MODE_EXT_KEYBOARD_DISABLE.bitMask() | options.MODE_EXT_INPUT_USE_LWJGL.bitMask()))) {
                ctrlHDR.setAWTKeyboardDisabled(true);
            } else {
                ctrlHDR.setAWTKeyboardDisabled(false);
            }
            if ((newMode & options.MODE_EXT_MOUSE_DISABLE.bitMask()) != 0) {
                /* mode state change only*/
            }
            if ((newMode & options.MODE_EXT_GAMEPAD_DISABLE.bitMask()) != 0) {
                ctrlHDR.setGamepadDisabled(true);
            } else {
                ctrlHDR.setGamepadDisabled(false);
            }
            /**
             * call focus for input
             */
            if (!requestFocusInWindow()) {
                requestFocus();
            }
        }
        /**
         * if Java2D rendering is not set correctly in FScreen or Windowed,
         * suspend rendering and change mode
         */
        if ((newMode & options.MODE_VIEWPORT_FULLSCREEN.bitMask()) != 0 && (newMode & options.MODE_RENDER_SOFT.bitMask()) != 0) {
            /* disable current render mode and add Hardware accelerated mode*/
            newMode = bits._getAllBits() & (newMode & ~options.getOptionsMaskFor(options._RENDER_MODE_BIT)
                    | options.MODE_RENDER_HARD.bitMask());
            modeChangeRange |= options._RENDER_MODE_BIT.bitMask();
        }
        if ((newMode & options.MODE_VIEWPORT_WINDOWED.bitMask()) != 0 && (newMode & options.MODE_RENDER_HARD.bitMask()) != 0) {
            /* disable current render mode and add Software rendering mode*/
            newMode = bits._getAllBits() & (newMode & ~options.getOptionsMaskFor(options._RENDER_MODE_BIT)
                    | options.MODE_RENDER_SOFT.bitMask());
            modeChangeRange |= options._RENDER_MODE_BIT.bitMask();
        }
        if ((modeChangeRange & options._RENDER_MODE_BIT.bitMask()) != 0 && running) {
            suspend();
        }
        /**
         * multiThreading forceDisable when soft- or GL-rendering is enabled
         */
        _multiThreading = (newMode & options.MODE_EXT_MULTITHREADING.bitMask()) != 0;
        if ((modeChangeRange & options._RENDER_MODE_BIT.bitMask()) != 0) {
            removeOffscreenLayers();
        }
        /**
         * disallowed layout change in exclusive (fixed) viewport mode
         */
        if ((newMode & options.MODE_EXT_EXCLUSIVE.bitMask()) != 0 && (newMode & options.getOptionsMaskFor(options._VIEWPORT_BIT)) != (mode & options.getOptionsMaskFor(options._VIEWPORT_BIT))) {
            newMode &= (~options.getOptionsMaskFor(options._VIEWPORT_BIT)) | (mode & options.getOptionsMaskFor(options._VIEWPORT_BIT));
        }
        /**
         * newMode is actually stored into mode (no parentOption bits are
         * stored)
         */
        mode = bits._getAllBits() & newMode;
        /**
         * viewport state check
         */
        if ((newMode & options.MODE_VIEWPORT_FULLSCREEN.bitMask()) != 0) {
            if (!isFullscreen()) {
                switchFullScreen();
            }
        } else if ((newMode & options.MODE_VIEWPORT_WINDOWED.bitMask()) != 0) {
            if (isFullscreen()) {
                switchFullScreen();
            }
        }
        /**
         * load layers and (re)start render loop
         */
        if ((modeChangeRange & options._RENDER_MODE_BIT.bitMask()) != 0) {
            addOffscreenLayers();
        }

        if (running && (state != RUNNING)) {
            resume();
        }
        synchronized (listeners) {
            for (RenderingSceneListener rsl : listeners) {
                if (rsl != null) {
                    rsl.modeChanged(modeChangeRange);
                }
            }
        }
    }

    /**
     * sets up the EXCLUSIVE MODE to use
     *
     * (this method should not be called more than required, because it alters
     * the rendering layers)
     *
     * @param viewportMode the VIEWPORT mode to use (options.*)
     *
     * @throws JXAException if the specified mode is not known
     *
     * @see options#MODE_VIEWPORT_FULLSCREEN
     * @see options#MODE_VIEWPORT_WINDOWED
     * @see #getExclusiveMode()
     */
    public void setExclusiveMode(final int viewportMode) {
        doTask(new Runnable() {
            @Override
            public void run() {
                if (0 == (viewportMode & options._VIEWPORT_BIT.bitMask())) {
                    throw new JXAException(JXAException.LEVEL.APP, "Illegal argument for exclusive mode");
                }
                setMode(viewportMode | options.MODE_EXT_EXCLUSIVE.bitMask());
            }
        });
    }

    protected void setLoadingState(final int loading_state_bit, final boolean b) {
        doTask(new Runnable() {
            @Override
            public void run() {
                if (b) {
                    setMode(mode & bits._getMask(options._STATE_BIT.bitMask()) | loading_state_bit);
                } else {
                    setMode(mode & ~loading_state_bit | options._STATE_BIT.bitMask());
                }
            }
        });
    }

    /**
     * dis/enables the loading playerStatus of this RenderingScene instance
     *
     * @param b dis/enables the loading playerStatus of this RenderingScene
     * instance
     *
     * @default false
     */
    public void setLoading(final boolean b) {
        loading = b;
    }

    /**
     * returns true or false whether the loading playerStatus is on or off,
     * resp.
     *
     * @return true or false
     *
     * @see #setLoading(boolean)
     */
    public boolean isLoading() {
        return isOption(options.MODE_STATE_LOADING_LAYER) || isOption(options.MODE_STATE_LOADING_GL) || loading;
    }

    /**
     * checks for the option in the current state mode
     *
     * @param option
     * @return
     */
    public boolean isOption(options option) {
        return (mode & option.bitMask()) != 0;
    }
    /**
     * the Sprite buffer sorted map
     */
    protected static SortedMap<Integer, Sf3Renderable> backBuffer;
    /**
     * the Sprite back-buffer map
     */
    protected static SpritesCacheManager<Integer, Sf3Renderable> _backBuffer;
    /**
     * the Sprite's hashes buffer sorted map
     */
    protected static SortedMap<String, Integer> spritesHashesBuffer = Collections.synchronizedSortedMap(new TreeMap<String, Integer>());

    static {
        _backBuffer = new SpritesCacheManager<Integer, Sf3Renderable>();
        _backBuffer.setSwapDiskEnabled(true);
        backBuffer = Collections.synchronizedSortedMap(_backBuffer);
    }
    /**
     * the MediaTracker instance of this RenderingScene instance
     *
     *
     * @see SfMediaTracker
     */
    MediaTracker mt = SpriteIO._mt;
    /**
     * the default size of this scene
     *
     * @default Dimension(150, 150)
     */
    static Dimension defaultSize = new Dimension(150, 150);
    /**
     * default refresh rate [Hz]
     *
     * @default 30 (30 frames per second)
     *
     * public static int _defaultRefreshRate = 30;
     */
    /**
     * the screen refresh Timer
     */
    java.util.Timer refresh;

    private void newTimer() {
        refresh = new java.util.Timer("Timer-screen-" + hash);
        if (isDebugEnabled()) {
            System.out.println("                                  rendering's initialized");
        }
        uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

    }

    /**
     * if you use {@link #workDT} to invoke tasks, then checkAlive checks that a
     * Timer is alive for dispatching tasks.
     *
     * @return if false, all scheduled tasks must be reloaded again
     */
    public boolean checkAlive() {
        try {
            if (refresh == null) {
                return false;
            }
            refresh.schedule(new TimerTask() {
                @Override
                public void run() {
                    /**
                     * schedule is tested
                     */
                }
            }, 0);
            return true;
        } catch (IllegalStateException ex) {
            int n = refresh.purge();
            System.err.println(JXAenvUtils.log(n + " tasks have been purged because the timer was dead. (" + refresh.toString() + ")", JXAenvUtils.LVL.SYS_NOT));
            if (DebugMap._getInstance().isDebugLevelEnabled(DebugMap._getInstance()._VOID)) {
                ex.printStackTrace();
            }
            return false;
        }
    }
    /**
     *
     */
    TimerTask refreshTask;
    /**
     * the offscreen refresh Timer * java.util.Timer refresh_off;
     *
     * /**
     *
     * TimerTask refresh_offTask;
     */
    /**
     * the graphics device
     */
    protected GraphicsDevice device;
    /**
     * refresh rate in Hz given by the current display Mode
     *
     * int refreshRate;
     */
    /**
     * the containing frame for the Canvas
     */
    JFrame frame;
    /**
     * the current display mode
     */
    protected DisplayMode displayMode;
    /**
     * the initial display mode
     */
    protected DisplayMode initialDisplayMode;
    /**
     * refresh keyboard disptach * static final int KEYBOARDDP_REFRESH = 4;
     */
    /**
     *
     */
    protected final static int LAYER_BACK = 8;
    /**
     *
     */
    protected final static int LAYER_OFFSCREEN = 16;
    /**
     *
     */
    protected final static int LAYER_FRONT = 32;
    /**
     * the actions before drawing
     */
    protected final List<GameActionLayer> before_action_list = Collections.synchronizedList(new ArrayList<GameActionLayer>()), beforeActionLayers = Collections.synchronizedList(new ArrayList<GameActionLayer>());
    /**
     * the actions after drawn
     */
    protected final List<GameActionLayer> after_action_list = Collections.synchronizedList(new ArrayList<GameActionLayer>()), afterActionLayers = Collections.synchronizedList(new ArrayList<GameActionLayer>());
    /**
     * offscreen action list, or "layers"
     */
    protected final List<GameActionLayer> offscreen_action_list = Collections.synchronizedList(new ArrayList<GameActionLayer>()), offscreenActionLayers = Collections.synchronizedList(new ArrayList<GameActionLayer>());
    /**
     * RenderingSceneListener's set
     */
    protected final Set<RenderingSceneListener> listeners = Collections.synchronizedSet(new HashSet<RenderingSceneListener>());
    /**
     * current state of the rendering scene
     *
     *
     * @see #STOPPED
     *
     * @see #RUNNING
     *
     * @see #SUSPENDED
     * @default STOPPED
     */
    private LogicalSystem _timerstate = new TimerState(this);
    private State state = STOPPED;

    /**
     * the inner-resource mode switch
     *
     * @default true public static boolean _innerRsrcMode = true;
     */
    /**
     * the rendering monitor switch
     */
    protected boolean rendering = false;
    /**
     * the rendering monitor
     */
    protected Monitor vSynch;

    /**
     * the buffering monitor switch
     *
     * private boolean buffering = false;
     */
    /**
     * the buffering monitor
     *
     * public Monitor bufferSynch;
     */
    /**
     * the multi-threading switch
     *
     * private static boolean multiThreading = false;
     */
    /**
     * the JFrame instance to set as Container for this Canvas. it can be null
     * and any full-screen attempt will instance a new JFrame instance to allow
     * full-screen
     *
     *
     * @param frame the JFrame instance that contains this RenderingScene Canvas
     *
     * @see #getToDevice()
     */
    public void setToDevice(JFrame frame) {
        this.frame = frame;
    }

    /**
     * returns the JFrame instance that is set as the current device of this
     * RenderingScene instance
     *
     * @return the current JFrame instance that is set as the current device.
     * Full-screen mode use it as the peer-component for this RenderingScene
     * instance.
     *
     * @see #setToDevice(JFrame)
     */
    public JFrame getToDevice() {
        return frame;
    }
    /**
     *
     */
    private int numBuffers = 0;

    /**
     * (re)-creates the BufferStrategy to fit the current layout.
     */
    public void createBufferStrategy() {
        AccessController.doPrivileged(new PrivilegedAction() {
            @Override
            public Object run() {
                _createBufferStrategy();
                return null;
            }
        }, acc);
    }

    private void _createBufferStrategy() {
        GraphicsConfiguration[] gcs = device.getConfigurations();
        BufferCapabilities c = null;
        /*
         * will setup to the actual capabilities from hardware
         */
        for (GraphicsConfiguration gc : gcs) {
            c = gc.getBufferCapabilities();
            Map<String, String> info = _getGraphicsRendererInfo(gc, MemScratch._isInitialized());
            String s = "";
            for (String key : info.keySet()) {
                s += key + " : " + info.get(key) + "\n";
            }
            if (isDebugEnabled()) {
                System.out.print(log(s, JXAenvUtils.LVL.SYS_NOT));
            }
            if (c.getFrontBufferCapabilities().isAccelerated() || gc.getImageCapabilities().isAccelerated() || c.getBackBufferCapabilities().isAccelerated()) {
                break;
            }
        }
        try {
            if (getGraphicsConfiguration().getDevice().getFullScreenWindow() == null) {
                createBufferStrategy(numBuffers = 1, c);
            } else if (c.isPageFlipping() && c.isMultiBufferAvailable()) {
                createBufferStrategy(numBuffers = 3, c);
            } else {
                createBufferStrategy(numBuffers = 2, c);
            }
        } catch (AWTException ex) {
            if (isDebugEnabled()) {
                ex.printStackTrace();
            }
            try {
                createBufferStrategy(numBuffers = 1, c);
            } catch (AWTException ex2) {
                if (isDebugEnabled()) {
                    ex2.printStackTrace();
                }
            }
        } finally {
        }
    }

    /**
     * notifies the Canvas that it has been added to some Container and
     * activates the best available BufferStrategy
     *
     * @see Canvas#addNotify()
     * @see BufferCapabilities
     * @see #createBufferStrategy(int, BufferCapabilities)
     */
    @Override
    public void addNotify() {
        super.addNotify();
        setIgnoreRepaint(true);
        createBufferStrategy();
        addKeyboardEvents();
        loadResource();
        /**
         * don't start rendering too early, layout event queue is not free.
         * startRendering();
         */
    }

    /**
     * calls {@link #stopRendering()} immediately before to
     * {@link #clearResource()}
     */
    @Override
    public void removeNotify() {
        super.removeNotify();
        /**
         * force stop of the rendering to free resources before to lose the
         * graphics/context instance
         */
        stopRendering();
        clearResource();
        removeKeyboardEvents();
    }

    /**
     * constructs a new RenderingScene instance that can be added to a JFrame
     * instance with a default size
     *
     * @see #defaultSize
     * @see #startRendering()
     */
    public RenderingScene() {
        this(defaultSize, GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice());
    }

    /**
     *
     * @param device
     */
    public RenderingScene(GraphicsDevice device) {
        this(defaultSize, device);
    }

    @Override
    public GraphicsConfiguration getGraphicsConfiguration() {
        GraphicsConfiguration gc = super.getGraphicsConfiguration();
        if (gc == null) {
            gc = device.getDefaultConfiguration();
        }
        return gc;
    }

    /**
     *
     * @return
     */
    public GraphicsDevice getDevice() {
        return device;
    }
    /**
     *
     */
    protected AccessControlContext acc = AccessController.getContext();
    /**
     * default color scheme
     *
     * @see #setForeground(Color)
     * @see #setBackground(Color)
     */
    public static Color _defaultForegroundColor = Color.ORANGE, _defaultBackgroundColor = Color.BLACK;

    private boolean consoleEnabled = false;

    /**
     * Creates a new instance of RenderingScene
     *
     * @param size the display Dimension
     * @param device
     * @see Canvas#Canvas(GraphicsConfiguration)
     * @discussion the enableInputMethods() is set to false by default
     */
    public RenderingScene(Dimension size, GraphicsDevice device) {
        super(device.getDefaultConfiguration());
        this.device = device;
        displayMode = initialDisplayMode = device.getDisplayMode();
        /*
         * refreshRate = _defaultRefreshRate;
         */
        defaultSize = size;
        setSize(size);
        setPreferredSize(size);
        setBackground(_defaultBackgroundColor);
        setForeground(_defaultForegroundColor);
        setGroupMonitor(new Monitor(), new Monitor(), new Monitor(), new Monitor());
        loading_logo = _getLogoAnimation(this, (String) loading_logo_file[0], (Integer) loading_logo_file[1], (Integer) loading_logo_file[2], (String) loading_logo_file[3], (String) loading_logo_file[4], (Dimension) loading_logo_file[5]);
        /**
         * GLbegin hud_helpcommands data
         */
        hud_helpcommands.pushContent("_HELP_help", "[F1] this help [+ALT or +SHIFT debug mode]", POS.LEFT, POS.TOP, true);
        hud_helpcommands.pushContent("_HELP_gco", "[F2] : show/hide console logger (+ALT or +SHIFT : clear contents)", POS.LEFT, POS.TOP, true);
        hud_helpcommands.pushContent("_HELP_debugActivity", "[F3] : show/hide loading activity", POS.LEFT, POS.TOP, true);
        hud_helpcommands.pushContent("_HELP_sound", "[F7] Sound on/off", POS.LEFT, POS.TOP, true);
        hud_helpcommands.pushContent("_HELP_fontSize", "[F5/F6] decrease/increase font size", POS.LEFT, POS.TOP, true);
        hud_helpcommands.pushContent("_HELP_rdrMode", "[F12] rendering" + ((ticker) ? "-" : " ") + ((mode & (options.MODE_RENDER_GL.bitMask() | options.MODE_RENDER_HARD.bitMask())) != 0 ? "HARD" : "SOFT") + ((ticker) ? "-" : " ") + ((mode & options.MODE_EXT_FORCEVRAM.bitMask()) != 0 ? " VRAM Sprites" : " Buffered Sprites"), POS.LEFT, POS.TOP, true);
        hud_helpcommands.pushContent("_HELP_rdrOptions", "[F11] on/off, [F4] full-screen, [F9] multi-threading : " + ((!isMultiThreadingEnabled()) ? "off" : "on") + ", [F10] ImageIO cache : " + ((ImageIO.getUseCache()) ? "on" : "off"), POS.LEFT, POS.TOP, true);
        hud_helpcommands.pushContent("_HELP_gcoScroll", (consoleEnabled ? "[PGUp/PGDown] console scroll up/down" : ""), POS.LEFT, POS.TOP, true);
        hud_helpcommands.pushContent("_HELP_javaPack", getClass().getPackage().getSpecificationTitle() + " v." + getClass().getPackage().getSpecificationVersion() + " by " + getClass().getPackage().getSpecificationVendor(), POS.RIGHT, POS.BOTTOM, false);
        hud_helpcommands.pushContent("_HELP_javaEnv", "current Java Environment v." + System.getProperty("java.version"), POS.RIGHT, POS.BOTTOM, true);
        hud_helpcommands.pushContent("_HELP_rdrPipe", getWidth() + "x" + getHeight() + "@ " + displayMode.getRefreshRate() + " Hz " + displayMode.getBitDepth() + " bits " + (openglEnabled ? "w/ OpenGL" : d3dEnabled ? "w/ Direct3D" : "w/ Sun Micr.") + " pipeline", POS.LEFT, POS.BOTTOM, true);
        /**
         * GLend hud_helpcommands data
         */
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (getState() == RUNNING) {
                    startRendering();
                }
            }
        });
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                synchronized (listeners) {
                    for (RenderingSceneListener r : listeners) {
                        r.componentResized(e);
                    }
                }
            }
        });
        addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                if (isDebugActivityEnabled()) {
                    JXAenvUtils.log("Focus gained for " + e.getOppositeComponent(), LVL.SYS_NOT);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (isDebugActivityEnabled()) {
                    JXAenvUtils.log("Focus lost for " + e.getOppositeComponent(), LVL.SYS_NOT);
                }
            }
        });
        URL iconUrl = UIMessage._getIconResourceURL(UIMessage.MODEL_TYPE, true);
        if (trayIcon == null && iconUrl != null) {
            trayIcon = new SystemTrayIcon(iconUrl, "OpenGL Settings");
            trayIcon.addMenuItem(new AbstractAction("About...") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    UIMessage.showLightPopupMessage(new JLabel("<html>"
                            + "Current JXA v." + JXAenvUtils2._jxaVersion()
                            + "<br>Graphics card driver :<br>"
                            + GLHandler.GL_vendor + "<br>" + GLHandler.GL_renderer + "<br>" + GLHandler.GL_version
                            + "<br></html>"), null, null, UIMessage._BOTTOM_RIGHT);
                }
            });
            final Action activityLoad = new AbstractAction("Debug Activity") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setDebugActivityEnabled(!isDebugActivityEnabled());
                }
            }, displayFeature = new AbstractAction("Display") {
                @Override
                public void actionPerformed(ActionEvent e) {
                }
            };
            trayIcon.addMenu("Debug", new AbstractAction("Std Log -> On") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    RenderingScene.setDebugRenderEnabled(true);
                }
            }, new AbstractAction("Std Log -> Off") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    RenderingScene.setDebugRenderEnabled(false);
                }
            }, activityLoad, displayFeature);
            addListener(new RenderingSceneListener() {
                @Override
                public void reset() {
                }

                @Override
                public void modeChanged(int modeBitRange) {
                    activityLoad.putValue(Action.NAME, !isDebugActivityEnabled() ? "Debug Activity On" : "Debug Activity Off");
                }

                @Override
                public void componentResized(ComponentEvent e) {
                    displayFeature.putValue(Action.NAME, getWidth() + " x " + getHeight() + " @ " + displayMode.getRefreshRate());
                }
            });
            trayIcon.pack();
        }
    }
    private static SystemTrayIcon trayIcon;

    private void switchLayersOff() {
        synchronized (beforeActionLayers) {
            for (GameActionLayer a : beforeActionLayers) {
                removeOffscreen(a, before_action_list);
            }
        }
        synchronized (offscreenActionLayers) {
            for (GameActionLayer a : offscreenActionLayers) {
                removeOffscreen(a, offscreen_action_list);
            }
        }
        synchronized (afterActionLayers) {
            for (GameActionLayer a : afterActionLayers) {
                removeOffscreen(a, after_action_list);
            }
        }
    }

    private void switchLayersOn() {
        synchronized (beforeActionLayers) {
            for (GameActionLayer a : beforeActionLayers) {
                addOffscreen(a, before_action_list, before_action_list.size(), true);
            }
        }
        synchronized (offscreenActionLayers) {
            for (GameActionLayer a : offscreenActionLayers) {
                addOffscreen(a, offscreen_action_list, offscreen_action_list.size(), true);
            }
        }
        synchronized (afterActionLayers) {
            for (GameActionLayer a : afterActionLayers) {
                addOffscreen(a, after_action_list, after_action_list.size(), true);
            }
        }
    }

    /**
     * adds default layers as first (index = 0) to the layers lists
     */
    protected void addOffscreenLayers() {
        addBeyondOffscreen(ctrlHDR);
        addBeyondOffscreen(getDefaultRenderingLayer(mode, LAYER_BACK));
        addActionOffscreen(getDefaultRenderingLayer(mode, LAYER_OFFSCREEN));
        addAboveOffscreen(getDefaultRenderingLayer(mode, LAYER_FRONT));
        switchLayersOn();
    }

    /**
     * removes default layers
     */
    protected void removeOffscreenLayers() {
        switchLayersOff();
        removeBeyondOffscreen(ctrlHDR);
        removeBeyondOffscreen(getDefaultRenderingLayer(mode, LAYER_BACK));
        removeActionOffscreen(getDefaultRenderingLayer(mode, LAYER_OFFSCREEN));
        removeAboveOffscreen(getDefaultRenderingLayer(mode, LAYER_FRONT));
    }

    /**
     * notifies the RenderingListener's set for a reset (usually after coming
     * off another rendering mode or full-screen mode). Then a new instance of
     * RenderingScene must be reinitialized again.
     *
     * @see RenderingSceneListener#reset()
     */
    private void notifyReset() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                log("RenderingScene reset", LVL.APP_NOT);
                List<RenderingSceneListener> rsls = Collections.synchronizedList(new ArrayList<RenderingSceneListener>(listeners));
                for (RenderingSceneListener rsl : rsls) {
                    if (rsl != null) {
                        rsl.reset();
                    }
                }
            }
        };
        if (ThreadWorks.Swing.isEventDispatchThread()) {
            r.run();
        } else {
            try {
                ThreadWorks.Swing.invokeAndWait(r);
            } catch (InterruptedException ex) {
                if (isDebugEnabled()) {
                    ex.printStackTrace();
                }
            } catch (InvocationTargetException ex) {
                if (isDebugEnabled()) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * rendering is stopped and the listeners get notified of a reset e.g. a
     * renderingscenelistener performs the reset callback :
     * <pre>new RenderingSceneListener() {
     *
     * public void reset() {
     * if (rs.getToDevice() instanceof JFrame) {
     * rs.getToDevice().dispose();
     * }
     * applet.remove(rs);
     * applet.add(rs, BorderLayout.CENTER);
     * applet.validate();
     * }
     *
     * public void modeChanged(int mode) {
     * }
     * }</pre>
     */
    public void reset() {
        doTask(new Runnable() {
            @Override
            public void run() {
                stopRendering();
                notifyReset();
            }
        });
    }

    /**
     * Synchronizes the display device. (on Linux, this fixes event queue
     * problems)
     *
     * @see Toolkit#sync()
     */
    public void sync() {
        getToolkit().sync();
    }
    /**
     * the displaying official logos switch
     *
     * @default false
     */
    protected boolean drawLogosEnabled = false;

    /**
     * dis/enables displaying the default official logos
     *
     * @param drawLogosEnabled dis/enables displaying the default logos
     * @deprecated used to have it forceDisable since it slew down the
     * initialization of the canvas
     */
    public void setDrawLogosEnabled(boolean drawLogosEnabled) {
        this.drawLogosEnabled = drawLogosEnabled;
    }

    /**
     * returns true or false whether displaying the default official logos is
     * enabled or not, resp.
     *
     * @return true or false
     * @see #setDrawLogosEnabled(boolean)
     */
    public boolean isDrawLogosEnabled() {
        return drawLogosEnabled;
    }

    /**
     * CAUTION : this method loads Sprite for both Java2D and LWJGL mode, so it
     * might be slower for bigger sprite as the BufferedImage is also serialized
     * out eventually instances and returns a Sprite instance with the desired
     * logo image/x-png image file
     *
     * @param c
     * @param logo the file name of the logo
     * @param size the size to set the Sprite to display at
     * @return the Sprite instance
     * @see #_drawLogo(Component, Graphics2D, String, Dimension, int, int, int,
     * int)
     */
    public static Sprite _getLogo(Component c, String logo, Dimension size) {
        return _getLogo(c, logo, size, false);
    }

    /**
     * CAUTION : this method loads Sprite for both Java2D and LWJGL mode, so it
     * might be slower for bigger sprite as the BufferedImage is also serialized
     * out eventually instances and returns a Sprite instance with the desired
     * logo image/x-png image file
     *
     * @param c
     * @param logo the file name of the logo
     * @param size the size to set the Sprite to display at
     * @param tileMode choose to enable tile mode readers (which can save heap
     * memory) or not. if enabled, image/tiff file format will be used to make
     * the cache storage.
     * @return the Sprite instance
     * @see #_drawLogo(Component, Graphics2D, String, Dimension, int, int, int,
     * int)
     */
    public static Sprite _getLogo(Component c, String logo, Dimension size, boolean tileMode) {
        Sprite sp = null;
        if (backBuffer.containsKey(spritesHashesBuffer.get(logo))) {
            sp = (Sprite) backBuffer.get(spritesHashesBuffer.get(logo));
            if (sp == null) {
                JXAenvUtils.log("Detected a Serialization error. Run with -Djxa.debugSPM=true and/or -Djxa.debugVoid=true", JXAenvUtils.LVL.APP_ERR);
            }
        }
        try {
            if (sp == null) {
                sp = __getSprite(logo, size);
                sp.setStoreMime(tileMode ? "image/tiff" : sp.getMime());
                sp.setStoreMode(Sprite.MODE_JAVA2D | Sprite.MODE_TEXTURE | (tileMode ? Sprite.MODE_TILE : 0));
                backBuffer.put(sp.hashCode(), sp);
                spritesHashesBuffer.put(logo, sp.hashCode());
            }
            sp.setSPM(_backBuffer);
            sp.setTileModeEnabled(tileMode);
            if (c instanceof RenderingScene) {
                /*
                 * saves heap with opengl Rendering*
                 */
                _backBuffer.setListCapacity(((RenderingScene) c).isLWJGLAccel() ? 1 : _backBuffer.getSwapMap().size());
                sp.setRenderingScene((RenderingScene) c);
            }
        } catch (Exception ex) {
            if (DebugMap._getInstance().isDebugLevelEnabled(DBUG_RENDER)) {
                ex.printStackTrace();
            }
        } finally {
            return sp;
        }
    }

    /**
     *
     */
    private static Sprite __getSprite(String logo, Dimension size) throws URISyntaxException {
        return new Sprite(logo, true, "image/x-png", size, true);
    }

    protected static String _getBufferPathAnimation(String animation_folder, int start, int end, String prefix, String suffix) {
        return animation_folder + "_" + prefix + "X" + start + "-" + end + "X" + suffix;
    }

    /**
     * CAUTION : this method loads Sprite for both Java2D and LWJGL mode, so it
     * might be slower for bigger sprite as the BufferedImage is also serialized
     * out
     *
     * @param c
     * @param prefix
     * @param animation_folder
     * @param start
     * @param GLend
     * @param suffix
     * @param size
     * @return
     */
    public static Animation _getLogoAnimation(Component c, String animation_folder, int start, int end, String prefix, String suffix, Dimension size) {
        Animation sp = null;
        String bufferPath = _getBufferPathAnimation(animation_folder, start, end, prefix, suffix);
        try {
            if (backBuffer.containsKey(spritesHashesBuffer.get(bufferPath))) {
                sp = (Animation) backBuffer.get(spritesHashesBuffer.get(bufferPath));
                if (sp == null) {
                    JXAenvUtils.log("Detected a Serialization error. Run with -Djxa.debugSPM=true and/or -Djxa.debugVoid=true", JXAenvUtils.LVL.APP_ERR);
                }
            }
            if (sp == null) {
                sp = new Animation(animation_folder, true, start, end, prefix, suffix, "image/x-png", size);
                backBuffer.put(sp.hashCode(), sp);
                spritesHashesBuffer.put(bufferPath, sp.hashCode());

            }
            if (c instanceof RenderingScene) {
                _backBuffer.setListCapacity(((RenderingScene) c).isLWJGLAccel() ? 1 : _backBuffer.getSwapMap().size());
                sp.setRenderingScene((RenderingScene) c);
            }
        } catch (Exception ex) {
            if (DebugMap._getInstance().isDebugLevelEnabled(DBUG_RENDER)) {
                ex.printStackTrace();
            }
        } finally {
            return sp;
        }
    }

    /**
     * draws a logo caught with getLogo() using a specific layout position. a
     * rotation effect can be added
     *
     * @param g te Graphics2D isntance where to draw onto
     * @param logo the file name of the logo to draw
     * @param size the size at which to draw the logo
     * @param hpos the horizontal position
     * @param vpos the vertical postion
     * @param fx dis/enables the selected FX (a bitwise-inclusive-OR combination
     * of FX)
     * @param c the Component to draw onto
     * @param fx_loc
     * @param fx_color
     * @throws InterruptedException
     * @see GLFX#gfx
     * @see #getLogo(String, Dimension)
     * @see POS#LEFT
     * @see POS#CENTER
     * @see POS#RIGHT
     * @see POS#TOP
     * @see POS#BOTTOM
     */
    public static void _drawLogo(Component c, Graphics2D g, String logo, Dimension size, POS hpos, POS vpos, int fx, Point fx_loc, Color fx_color) throws InterruptedException {
        _drawLogo(c, g, logo, size, hpos, vpos, fx, fx_loc, fx_color, false);
    }

    /**
     *
     * @param c
     * @param g
     * @param logo
     * @param size
     * @param hpos
     * @param vpos
     * @param fx
     * @param fx_loc
     * @param fx_color
     * @param tileMode
     * @throws InterruptedException
     */
    public static void _drawLogo(Component c, Graphics2D g, String logo, Dimension size, POS hpos, POS vpos, int fx, Point fx_loc, Color fx_color, boolean tileMode) throws InterruptedException {
        Sprite logo_sp = _getLogo(c, logo, size, tileMode);
        __drawLogo(c, g, logo_sp, size, hpos, vpos, fx, fx_loc, fx_color);
    }

    /**
     *
     * @param c
     * @param g
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
     * @throws InterruptedException
     */
    public static void _drawLogoAnimation(Component c, Graphics2D g, String animation_folder, int start, int end, String prefix, String suffix, Dimension size, POS hpos, POS vpos, int fx, Point fx_loc, Color fx_color) throws InterruptedException {
        Animation logoAnimation_sp = _getLogoAnimation(c, animation_folder, start, end, prefix, suffix, size);
        __drawLogo(c, g, logoAnimation_sp, size, hpos, vpos, fx, fx_loc, fx_color);
    }

    /*
     * static screen postioning (logos, messages...)
     */
    public static Point _getLogoPosition(RenderingScene c, POS hpos, POS vpos, Dimension size) {
        return __getPosition(c, hpos, vpos, size);
    }
    /*
     * screen postioning
     */

    protected static Point __getPosition(Component c, POS hpos, POS vpos, Dimension size) {
        Point translate = new Point(0, 0);
        Insets insets = new Insets(10, 10, 10, 10);
        switch (hpos) {
            case LEFT:
                translate.x = insets.left;
                break;
            case CENTER:
                translate.x = Math.round((float) c.getWidth() / 2.0f - (float) size.width / 2.0f);
                break;
            case RIGHT:
                translate.x = -insets.right + (int) (c.getWidth() - size.width);
                break;
            default:
                break;
        }
        switch (vpos) {
            case TOP:
                translate.y = insets.top;
                break;
            case CENTER:
                translate.y = Math.round((float) c.getHeight() / 2.0f - (float) size.height / 2.0f);
                break;
            case BOTTOM:
                translate.y = -insets.bottom + (int) (c.getHeight() - size.height);
                break;
            default:
                break;
        }
        return translate;
    }

    /**
     *
     * @param c
     * @param g
     * @param logo_sp
     * @param size
     * @param hpos
     * @param vpos
     * @param fx
     * @param fx_loc
     * @param fx_color
     * @throws InterruptedException
     */
    public static void __drawLogoAnimation(Component c, Graphics2D g, Animation logo_sp, Dimension size, POS hpos, POS vpos, int fx, Point fx_loc, Color fx_color) throws InterruptedException {
        __drawLogo(c, g, logo_sp, size, hpos, vpos, fx, fx_loc, fx_color);
    }

    /**
     *
     * @param c
     * @param g
     * @param logo_sp
     * @param size
     * @param hpos
     * @param vpos
     * @param fx
     * @param fx_loc
     * @param fx_color
     * @throws InterruptedException
     */
    public static void __drawLogo(Component c, Graphics2D g, Sf3Renderable logo_sp, Dimension size, POS hpos, POS vpos, int fx, Point fx_loc, Color fx_color) throws InterruptedException {
        Point translate = __getPosition(c, hpos, vpos, size);
        if (logo_sp instanceof Animation) {
            ((Animation) logo_sp).setActiveRenderingEnabled(true);
            ((Animation) logo_sp).play();
        }
        Rectangle bounds = new Rectangle(translate.x, translate.y, size.width, size.height);
        if (g instanceof Graphics2D) {
            logo_sp.setBounds(bounds);
            logo_sp.runValidate();
            logo_sp.draw(c, (Graphics2D) g, fx, fx_loc, fx_color);
        }
    }
    /**
     * RenderingScene uses Java2D-style coords for OpenGL
     *
     * @default true
     */
    public static boolean Java2DStyleCoordinates = true;

    /**
     *
     * @return
     */
    public boolean isOrthoProj() {
        return (mode & options.MODE_EXT_ORTHO.bitMask()) != 0;
    }

    /**
     * <s>DISABLED NOTICE : {@linkplain GLFX#_GLdisableFX} is enabled with this
     * option.</s>
     *
     * @param b
     */
    public void setOrthoProj(final boolean b) {
        doTask(new Runnable() {
            @Override
            public void run() {
                try {
                    setMode(b ? mode | options.MODE_EXT_ORTHO.bitMask() : mode & ~options.MODE_EXT_ORTHO.bitMask() | options._EXT_BIT.bitMask());
                } catch (Exception ex) {
                    if (DebugMap._getInstance().isDebugLevelEnabled(DBUG_RENDER)) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }
    /**
     * Thread that is running on the graphics processor
     */
    protected long screenThreadId = 0;

    /**
     * returns true or false, whether the current Thread is the same Thread
     * where the rendering is done.
     *
     * The screen Thread often changes, but generally it is one of the AWT-EDT.
     *
     * @return true or false, whether the current Thread is the same Thread
     * where the rendering is done.
     */
    public boolean isScreenThread() {
        return Thread.currentThread().getId() == screenThreadId;
    }

    /**
     * returns the screen rendering refresh task
     *
     * @return screen rendering refresh task
     */
    protected TimerTask getScreenTask() {
        return new TimerTask() {
            @Override
            public void run() {
                screenThreadId = Thread.currentThread().getId();
                screen.run();
            }
        };
    }
    private Map<Integer, Map<Integer, GameActionLayer>> actionMap = Collections.synchronizedMap(new HashMap<Integer, Map<Integer, GameActionLayer>>());

    /**
     * returns the refresh action associated to the given mode or the default
     * that refreshes the keyboard listeners
     *
     * @param mode the mode to get the action from (the integer 0 for the
     * default)
     *
     * @param layer the layer type (the integer 0 for the default)
     *
     * @see #FULLSCREEN_MODE
     *
     * @see #WINDOWED_MODE
     *
     * @see #LAYER_FRONT
     *
     * @see #LAYER_BACK
     *
     * @see #LAYER_OFFSCREEN
     *
     * @return the new action instance
     */
    protected GameActionLayer getDefaultRenderingLayer(int mode, int layer) {
        Map<Integer, GameActionLayer> map = actionMap.get(mode & bits._getMask(options._RENDER_MODE_BIT.bitMask()));
        if (map == null) {
            actionMap.put(mode & bits._getMask(options._RENDER_MODE_BIT.bitMask()), map = new HashMap<Integer, GameActionLayer>());
        }
        if (!map.containsKey(layer)) {
            map.put(layer, defaultRenderingLayer(mode & bits._getMask(options._RENDER_MODE_BIT.bitMask()), layer));
        }
        return map.get(layer);
    }
    protected boolean loading_logo_isPaintedOnFrontLayer = false;
    private GameActionLayer j2D_DEFAULT_BACK = new GameActionLayer("DEFAULT_BACK") {
        @Override
        protected void doLoad() {
            super.doLoad();
            loading_logo.loadResource();
        }

        @Override
        protected void renderToJava2D(Graphics2D g) {
            Composite cps = g.getComposite();
            Shape clip = g.getClip();
            g.setClip(0, 0, getWidth(), getHeight());
            g.setComposite(AlphaComposite.SrcOver);
            if (isDebugEnabled()) {
                System.out.println("////////////////////////////////////////////offscreen inited - " + System.currentTimeMillis());
            }
            g.setColor(g.getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(getForeground());
            g.setComposite(cps);
            g.setClip(clip);
            if (isLoading() && !loading_logo_isPaintedOnFrontLayer) {
                loading_logo.setReverseEnabled(false);
                loading_logo.setRealTimeLength(1000);
                try {
                    __drawLogoAnimation(RenderingScene.this, g, loading_logo,
                            loading_logo.getBounds().getSize(), loading_logo_hpos, loading_logo_vpos,
                            GLFX.gfx.FX_DOWNREFLEXION_EXT, new Point(0, 0), Color.GREEN);
                } catch (InterruptedException ex) {
                    if (isDebugEnabled()) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    };
    private GameActionLayer j2D_DEFAULT_FRONT = new GameActionLayer("DEFAULT_FRONT") {
        @Override
        protected void doLoad() {
            super.doLoad();
            /*
             * _getLogoAnimation(RenderingScene.this,
             * "/net/sf/jiga/xtended/impl/opengl", 0, 29, "", ".png", new
             * Dimension(64, 30));
             */
        }

        @Override
        protected void doUnload() {
            super.doUnload();
            loading_logo_isPaintedOnFrontLayer = false;
        }

        @Override
        protected void renderToJava2D(Graphics2D g) {
            try {
                if (drawFPSEnabled) {
                    Sprite fpsCnt = _getLogo(RenderingScene.this, "/net/sf/jiga/xtended/impl/fpsCnt.png", new Dimension(32, 32));
                    Sprite fpsVal = _getLogo(RenderingScene.this, "/net/sf/jiga/xtended/impl/fpsVal.png", new Dimension(32, 32));
                    fpsCnt.setLocation(32, RenderingScene.this.getHeight() - 32);
                    fpsCnt.runValidate();
                    fpsCnt.draw(RenderingScene.this, g, GLFX.gfx.FX_DOWNREFLEXION_EXT, new Point(0, 0), null);
                    try {
                        int ref = displayMode.getRefreshRate();
                        if (ref == 0 || ref == DisplayMode.REFRESH_RATE_UNKNOWN) {
                            ref = 60;
                        }
                        float val = NumberFormat.getInstance().parse(getFPS()).floatValue();
                        AffineTransform tx = AffineTransform.getRotateInstance((double) (-3f * (float) (Math.PI / 2f) + 2f * (float) Math.PI * (val / ((float) ref != 0 ? ref : val))), (double) ((float) fpsVal.getWidth() / 2f), (double) ((float) fpsVal.getHeight() / 2f));
                        fpsVal.setLocation(32, RenderingScene.this.getHeight() - 32);
                        fpsVal.runValidate();
                        fpsVal.draw(RenderingScene.this, g, tx, null);
                        Shape clip = g.getClip();
                        g.clip(fpsCnt.getBounds());
                        String fpsStr = getFPS();
                        g.drawString(fpsStr, (float) (g.getClipBounds().getCenterX() - (float) _toGraphicalMetrics(fpsStr, g).width / 2f), (float) (g.getClipBounds().getCenterY() + _toGraphicalMetrics(fpsStr, g).height));
                        g.setClip(clip);
                    } catch (ParseException ex) {
                        if (isDebugEnabled()) {
                            ex.printStackTrace();
                        }
                    }
                }
                if (consoleEnabled) {
                    GUIConsoleOutput console = getGCO();
                    if (console != null) {
                        console.setAlpha(0.5f);
                        int x = (int) ((float) (getWidth() - console.getBounds().getWidth()) / 2f);
                        int y = (int) ((float) (getHeight() - console.getBounds().getHeight()) / 2f);
                        console.setLocation(x, y);
                        console.draw(RenderingScene.this, g);
                    }
                }
                if (isLoading()) {
                    loading_logo_isPaintedOnFrontLayer = true;
                    loading_logo.setReverseEnabled(false);
                    loading_logo.setRealTimeLength(1000);
                    __drawLogoAnimation(RenderingScene.this, g, loading_logo,
                            loading_logo.getBounds().getSize(), loading_logo_hpos, loading_logo_vpos,
                            0, new Point(0, 0), Color.GREEN);
                }
                if (drawLogosEnabled) {
                    _drawLogo(RenderingScene.this, g, logo_java_file, logo_java_size, POS.CENTER, POS.BOTTOM, GLFX.gfx.FX_SHADOW.bit(), new Point(10, 10), null);
                    logo_java_size = new Dimension(53, 53);
                    _drawLogo(RenderingScene.this, g, logo_lwjgl_file, logo_java_size, POS.RIGHT, POS.BOTTOM, GLFX.gfx.FX_SHADOW.bit(), new Point(10, 10), null);
                    if (openglEnabled) {
                        Animation a = _getLogoAnimation(RenderingScene.this, "/net/sf/jiga/xtended/impl/opengl", 0, 29, "", ".png", new Dimension(64, 30));
                        a.setRealTimeLength(2500);
                        a.setReverseEnabled(true);
                        __drawLogoAnimation(RenderingScene.this, g, a, new Dimension(64, 30), POS.RIGHT, POS.TOP, GLFX.gfx.FX_DOWNREFLEXION_EXT, new Point(0, 0), null);
                    }
                }
                Color clr = g.getColor();
                g.setColor(getForeground());
                if (keyEvent && drawKeyEventsEnabled) {
                    g.drawOval(10, 10, 30, 30);
                    if (ctrlHDR.isKeyPressed()) {
                        g.fillOval(10, 10, 30, 30);
                    }
                }
                g.setColor(clr);
                ticker = !ticker;

                /**
                 * begin hud_helpcommands java2D renderContents
                 */
                hud_helpcommands.setEnabled(_paintCommandsEnabled);
                if (hud_helpcommands.isEnabled()) {
                    hud_helpcommands.begin();

                    hud_helpcommands.updateContent("_HELP_rdrMode", "[F12] rendering" + ((ticker) ? "-" : " ") + (((RenderingScene.this.mode & (options.MODE_RENDER_GL.bitMask() | options.MODE_RENDER_HARD.bitMask())) != 0) ? "HARD" : "SOFT") + ((ticker) ? "-" : " ") + ((RenderingScene.this.mode & options.MODE_EXT_FORCEVRAM.bitMask()) != 0 ? " VRAM Sprites" : " Buffered Sprites"));
                    hud_helpcommands.updateContent("_HELP_rdrOptions", "[F11] on/off, [F4] full-screen, [F9] multi-threading : " + ((RenderingScene.this.mode & options.MODE_EXT_MULTITHREADING.bitMask()) == 0 ? "off" : "on") + ", [F10] ImageIO cache : " + ((ImageIO.getUseCache()) ? "on" : "off"));
                    hud_helpcommands.updateContent("_HELP_gcoScroll", (consoleEnabled ? "[PGUp/PGDown] console scroll up/down" : ""));
                    hud_helpcommands.updateContent("_HELP_rdrPipe", getWidth() + "x" + getHeight() + "@ " + displayMode.getRefreshRate() + " Hz " + displayMode.getBitDepth() + " bits " + (openglEnabled ? "w/ OpenGL" : d3dEnabled ? "w/ Direct3D" : "w/ Sun Micr.") + " pipeline");
                    hud_helpcommands.renderContents(g);
                    hud_helpcommands.end();
                }
                /**
                 * GLend hud_helpcommands java2D
                 */
            } catch (InterruptedException ex) {
                if (isDebugEnabled()) {
                    ex.printStackTrace();
                }
            }
        }
    };

    /**
     * returns the new refresh action associated to the given mode or the
     * default that refreshes the keyboard listeners
     *
     * @param mode the mode to get the action from (the integer 0 for the
     * default or MODE_RENDER_*)
     * @param layer the layer type (the integer 0 for the default)
     * @see #MODE_FULLSCREEN
     * @see #MODE_WINDOWED
     * @see #MODE_RENDER_HARD
     * @see #LAYER_FRONT
     * @see #LAYER_BACK
     * @see #LAYER_OFFSCREEN
     * @return the new action instance
     */
    protected GameActionLayer defaultRenderingLayer(int mode, int layer) {
        GameActionLayer a = null;
        switch (layer) {
            case LAYER_BACK:
                a = j2D_DEFAULT_BACK;
                break;
            case LAYER_FRONT:
                a = j2D_DEFAULT_FRONT;
                break;
            default:
                a = new GameActionLayer("N/A") {
                };
                break;
        }
        return a;
    }
    protected String logo_java_file = "/net/sf/jiga/xtended/impl/logo_java_grey.png";
    protected String logo_lwjgl_file = "/net/sf/jiga/xtended/impl/lwjgl.png";
    protected Dimension logo_java_size = new Dimension(53, 53);
    protected Object[] loading_logo_file = new Object[]{"/net/sf/jiga/xtended/impl/bullets", 0, 9, "bullets", ".png", new Dimension(64, 64)};
    protected Animation loading_logo;
    protected POS loading_logo_hpos = POS.CENTER, loading_logo_vpos = POS.CENTER;

    /**
     *
     * @param folderpath
     * @param start
     * @param GLend
     * @param prefix
     * @param suffix
     * @param d
     * @param hpos
     * @param vpos
     */
    public void setLoadingAnimLogo(String folderpath, int start, int end, String prefix, String suffix, Dimension d, POS hpos, POS vpos) {
        loading_logo = _getLogoAnimation(this, folderpath, start, end, prefix, suffix, d);
        loading_logo_hpos = hpos;
        loading_logo_vpos = vpos;
    }

    /**
     *
     * @return
     */
    public Animation getLoadingAnimLogo() {
        return loading_logo;
    }

    /**
     * performs the Action's before the offscreen rendering refreshes
     *
     *
     */
    protected void doOffscreenTasks(Object layer, List<GameActionLayer> layers) throws InterruptedException {
        GameActionLayer[] actions = layers.toArray(new GameActionLayer[]{});
        for (int i = 0; i < actions.length; i++) {
            final GameActionLayer a = actions[i];
            if (a instanceof GameActionLayer) {
                if (isDebugEnabled()) {
                    System.out.println(a);
                }
                a.setRenderingScene(this);
                if (layer instanceof Graphics2D) {
                    a.setGraphicsLayer((Graphics2D) layer);
                } else {
                    a.setGLC((RenderingSceneGL) layer);
                }
                a.actionPerformed();
                if ((a.getLoadState(GameActionLayer.RSRC) & GameActionLayer.LOADING) != 0) {
                    setLoadingState(options.MODE_STATE_LOADING_LAYER.bitMask(), true);
                }
                if ((a.getLoadState(GameActionLayer.GL_RSRC) & GameActionLayer.GL_LOADING) != 0) {
                    setLoadingState(options.MODE_STATE_LOADING_GL.bitMask(), true);
                }
            }
        }
    }

    /**
     * adds a new listener to the RenderingSceneListners
     *
     * @param l the listener to add
     *
     * @see #removeListener(RenderingSceneListener)
     */
    public void addListener(RenderingSceneListener l) {
        listeners.add(l);
    }

    /**
     * removes the given listener from the RenderingSceneListeners
     *
     * @param l the listener to remove
     *
     * @see #addListener(RenderingSceneListener)
     */
    public void removeListener(RenderingSceneListener l) {
        listeners.remove(l);
    }

    /**
     * NOTE of use : It should be called before the RenderingScene canvas is
     * displayed, added to a container or frame. Better remove the canvas from
     * its container before to add a new rendering layer.
     *
     * @param a the action to add to offscreen rendering
     *
     * @see #getOffscreen()
     *
     * @see #removeActionOffscreen(GameActionLayer)
     */
    public void addActionOffscreen(GameActionLayer a) {
        addOffscreen(a, offscreenActionLayers, offscreenActionLayers.size(), true);
        if (isResourceLoaded()) {
            addOffscreen(a, offscreen_action_list, offscreen_action_list.size(), true);
        }
    }

    /**
     * adds a new Action before the offscreen rendering (it may also
     * renderContents to the offscreen, but will appear "under" and as drawn
     * before) NOTE of use : It should be called before the RenderingScene
     * canvas is displayed, added to a container or frame. Better remove the
     * canvas from its container before to add a new rendering layer.
     *
     * @param a the action to add
     * @see #removeBeyondOffscreen(GameActionLayer)
     */
    public void addBeyondOffscreen(GameActionLayer a) {
        addOffscreen(a, beforeActionLayers, beforeActionLayers.size(), true);
        if (isResourceLoaded()) {
            addOffscreen(a, before_action_list, before_action_list.size(), true);

        }
    }

    /**
     * adds a new Action after the offscreen rendering (it may also
     * renderContents to the offscreen, but will appear "above" and as drawn
     * afterwards) NOTE of use : It should be called before the RenderingScene
     * canvas is displayed, added to a container or frame. Better remove the
     * canvas from its container before to add a new rendering layer.
     *
     * @param a the action to add
     * @see #removeAboveOffscreen(GameActionLayer)
     */
    public void addAboveOffscreen(GameActionLayer a) {
        addOffscreen(a, afterActionLayers, afterActionLayers.size(), true);
        if (isResourceLoaded()) {
            addOffscreen(a, after_action_list, after_action_list.size(), true);
        }
    }

    private void addOffscreen(GameActionLayer a, List<GameActionLayer> layerslist, int index, boolean ifnotexists) {
        if (a instanceof GameActionLayer) {
            boolean add = true;
            if (ifnotexists) {
                add = !layerslist.contains(a);
            }
            if (add) {
                a.setRenderingScene(this);
                addListener(a);
                layerslist.add(index, a);
            }
        }
    }

    private boolean removeOffscreen(final GameActionLayer a, List<GameActionLayer> layerslist) {
        boolean ret = false;
        while (layerslist.remove(a) && a != null) {
            ret = true;
        }
        if (a != null) {
            a.clearResource();
            removeListener(a);
        }
        return ret;

    }

    /**
     * removes an Action layer from the default offscreen layer rendering
     *
     * @param a the Action layer to remove from offscreen rendering list
     *
     * @return true or false whether it has found the action or not
     *
     * @see #addActionOffscreen(GameActionLayer)
     */
    public boolean removeActionOffscreen(final GameActionLayer a) {
        return removeOffscreen(a, offscreenActionLayers) || removeOffscreen(a, offscreen_action_list);
    }

    /**
     * removes an Action before the default offscreen layer rendering
     *
     * @param a the Action to remove
     *
     * @return true or false whether it has found the action or not
     *
     * @see #addBeyondOffscreen(GameActionLayer)
     */
    public boolean removeBeyondOffscreen(final GameActionLayer a) {
        return removeOffscreen(a, beforeActionLayers) || removeOffscreen(a, before_action_list);
    }

    /**
     * removes an Action after the default offscreen layer rendering
     *
     * @param a the Action to remove
     *
     * @return true or false whether it has found the action or not
     *
     * @see #addAboveOffscreen(GameActionLayer)
     */
    public boolean removeAboveOffscreen(final GameActionLayer a) {
        return removeOffscreen(a, afterActionLayers) || removeOffscreen(a, after_action_list);
    }

    /**
     * returns the current state of the rendering scene
     *
     * @return the current state
     *
     * @see #STOPPED
     *
     * @see #RUNNING
     *
     * @see #SUSPENDED
     *
     * @see #startRendering()
     *
     * @see #stopRendering()
     */
    public State getState() {
        return state;
    }

    /**
     * tries to suspend the timertask. It is not guaranteed that the rendering
     * is stopped immediately after this method returns.
     */
    public void pauseRendering() {
        doTask(new Runnable() {

            @Override
            public void run() {
                suspend();
            }
        });
    }

    /**
     * suspends the rendering timer
     *
     * @see #resume()
     */
    private void suspend() {
        State ostate = state;
        switch (State.valueOf(_timerstate.newInput(PAUSE.X))) {
            case SUSPENDED:
                try {
                    final Monitor monitor = vSynch;
                    synchronized (monitor) {
                        while (rendering && !isScreenThread()) {
                            monitor.wait();
                        }
                        refresh.cancel();
                    }
                } catch (InterruptedException ex) {
                    if (isDebugEnabled()) {
                        ex.printStackTrace();
                    }
                } finally {
                    if (isDebugEnabled()) {
                        System.out.println("                                  rendering's suspended");
                    }
                    break;
                }
            case RUNNING:
            case STOPPED:
            default:
                log("Unable to suspend from " + ostate, LVL.APP_WRN);
        }
    }

    /**
     * resumes the rendering timer
     *
     * @see #suspend()
     */
    private void resume() {
        State ostate = state;
        switch (State.valueOf(_timerstate.newInput(START.X))) {
            case RUNNING:
                if (!checkAlive()) {
                    newTimer();
                    refresh.schedule(new TimerTask() {

                        @Override
                        public void run() {
                            Thread.setDefaultUncaughtExceptionHandler(RenderingScene.this);
                        }
                    }, 0);
                    /**
                     * see that screen refresh could at the very max rise to 100
                     * FPS (10ms/frame), change period to get higher rates
                     */
                    refresh.scheduleAtFixedRate(refreshTask = getScreenTask(), 0, 10);
                    if (isDebugEnabled()) {
                        System.out.println("                                  rendering's resumed");
                    }
                }
                break;
            default:
                log("unable to resume from " + ostate, LVL.APP_WRN);
                break;
        }
        if (!requestFocusInWindow()) {
            requestFocus();
        }
    }
    Thread.UncaughtExceptionHandler uncaughtExceptionHandler = null;

    /**
     * override to implement special exception handler for unknown errors
     */
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        if (uncaughtExceptionHandler != null) {
            uncaughtExceptionHandler.uncaughtException(t, e);
        }
        /*
         * stopRendering(); infinite loop
         */
    }

    /**
     * starts the rendering timer
     *
     * @see #stopRendering()
     */
    public void startRendering() {
        doTask(new Runnable() {

            @Override
            public void run() {
                resume();
            }
        });
    }

    /**
     * stops the rendering timer
     *
     * @see #startRendering()
     */
    public void stopRendering() {
        doTask(new Runnable() {

            @Override
            public void run() {
                suspend();
                State ostate = state;
                switch (State.valueOf(_timerstate.newInput(STOP.X))) {
                    case STOPPED:
                        if (isDebugEnabled()) {
                            System.out.println("                                  rendering's stopped");
                        }
                        Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);
                        refresh = null;
                    default:
                        log("Unable to stop from " + ostate, LVL.APP_WRN);
                        break;
                }
            }
        });
    }

    /**
     * stops the rendering timer and clears any resource before loosing the
     * RenderingScene instance
     *
     * @see #removeKeyboardEvents()
     *
     * @see #stopRendering()
     *
     * @see #clearResource()
     */
    @Override
    protected void finalize() throws Throwable {
        removeKeyboardEvents();
        stopRendering();
        clearResource();
        super.finalize();
    }

    /**
     * modifies the offscreen sprite in buffer with a new one
     *
     * @param offscreen the new Sprite to use as offscreen
     */
    public void setOffscreen(Sprite offscreen) {
        offscreenBuffer = offscreen;
    }

    /**
     * returns the Graphics2D-casted instance that can be painted on for the
     * rendering to process with,
     *
     * the offscreen Sprite-Graphics or the current flip- or
     * multi-BufferStrategy draw-Graphics. The returned instance should be
     * disposed at the application level.
     *
     * DO NOT USE THIS METHOD TO DRAW ON THE CANVAS ! PLEASE USE A
     * GAMEACTIONLAYER !
     *
     *
     * @return the Graphics2D-casted instance that can be painted on for the
     * rendering to process with or null if no Graphics context is available
     * (you should call it at most once per rendering loop,if running in
     * software rendering mode).
     *
     * @see GameActionLayer#getGraphicsLayer()
     */
    protected Graphics2D getOffscreenGraphics() {
        Graphics2D graphics = null;
        if ((mode & options.MODE_RENDER_GL.bitMask()) != 0) {
            return null;
        } else if (numBuffers < 2) {
            graphics = Sprite.wrapRendering(Sprite._createImageGraphics(getOffscreen().getImage(this)));
        } else {
            if (offscreenBuffer != null) {
                offscreenBuffer.clearResource();
            }
            Graphics g = getBufferStrategy() != null ? getBufferStrategy().getDrawGraphics() : null;
            if (g != null) {
                graphics = Sprite.wrapRendering(g);
                graphics.setComposite(composite);
            }
        }
        if (graphics instanceof Graphics2D) {
            Rectangle bounds = getBounds();
            graphics.translate(-bounds.x, -bounds.y);
            graphics.setClip(getBounds());
            graphics.translate(bounds.x, bounds.y);
            if (clip instanceof Shape) {
                graphics.setClip(clip);
            }
            graphics.setBackground(getBackground());
            graphics.setColor(getForeground());
        }
        return graphics;
    }
    private Shape clip = null;

    /**
     * a custom clip Shape can be defined with this method.
     *
     * @param clip a custom clip Shape or null
     */
    public void setClip(Shape clip) {
        this.clip = clip;
    }

    /**
     * returns the custom clip Shape or null if none has been defined.
     *
     * @return the custom clip Shape or null if none hash been defined.
     *
     * @see #setClip()
     */
    public Shape getClip() {
        return clip;
    }
    private Sprite offscreenBuffer = null;

    /**
     * returns the current offscreen Sprite or creates a new instance
     *
     * @return the current offscreen Sprite instance or null if
     * {@link #isLWJGLAccel() GL mode} is enabled
     */
    protected Sprite getOffscreen() {
        if ((mode & options.MODE_RENDER_GL.bitMask()) != 0) {
            return null;
        } else {
            if (offscreenBuffer == null) {
                offscreenBuffer = false ? new Sprite(SpriteIO.createBufferedImage(defaultSize, Sprite.DEFAULT_TYPE), "image/x-png", defaultSize) : new Sprite(Sprite.createVolatileImage(defaultSize, Sprite._getTransparency(Sprite.DEFAULT_TYPE)), "image/x-png", defaultSize);
                offscreenBuffer.setSPM(_backBuffer);
            }
            offscreenBuffer.setSize(new Dimension(getWidth(), getHeight()));
            return offscreenBuffer;
        }
    }
    private boolean switching = false;
    private boolean fullscreenFrame = false;

    /**
     * switches to fullscreen-mode or back to windowed mode
     *
     * (this method should not be called more than required, because it alters
     * the rendering layers)
     */
    private void switchFullScreen(final DisplayModeWrapper target, final DisplayModeWrapper targetMin, final DisplayModeWrapper targetMax, final int bitDepth) {
        if (switching) {
            if (isDebugEnabled()) {
                System.err.println("cannot switch the screen now... please wait !");
            }
            return;
        }
        Runnable r = new Runnable() {
            @Override
            public void run() {
                switching = true;
                AccessController.doPrivileged(new PrivilegedAction() {
                    @Override
                    public Object run() {
                        __switchFullScreen(target, targetMin, targetMax, bitDepth);
                        return null;
                    }
                }, acc);
                switching = false;
            }
        };
        /*
         * if (ThreadWorks.Swing.isEventDispatchThread()) { r.run(); } else {
         */
        ThreadWorks.Swing.invokeLater(r);
        /*
         * }
         */
    }

    private boolean isFullScreenSupported() {
        return device.isFullScreenSupported();
    }

    /**
     * switches to fullscreen-mode or back to windowed mode
     *
     * (this method should not be called more than required, because it alters
     * the rendering layers)
     */
    private void __switchFullScreen(DisplayModeWrapper target, DisplayModeWrapper targetMin, DisplayModeWrapper targetMax, int bitDepth) {
        if ((mode & options.MODE_VIEWPORT_FULLSCREEN.bitMask()) != 0 && (mode & options.MODE_EXT_EXCLUSIVE.bitMask()) == 0) {
            /**
             * quit full screen
             */
            suspend();
            try {
                if ((mode & options.MODE_RENDER_GL.bitMask()) == 0) {
                    if (device.isDisplayChangeSupported()) {
                        device.setDisplayMode(initialDisplayMode);
                        displayMode = initialDisplayMode;
                    }
                    if (isFullScreenSupported()) {
                        device.setFullScreenWindow(null);
                    }
                    frame.setExtendedState(frame.NORMAL);
                } else {
                    org.lwjgl.opengl.Display.setFullscreen(false);
                    org.lwjgl.opengl.Display.setDisplayMode(new DisplayModeWrapper(initialDisplayMode).getDmGL());
                }
                setMode(mode & ~(options.MODE_VIEWPORT_FULLSCREEN.bitMask() | options.MODE_RENDER_HARD.bitMask() | options.MODE_RENDER_GL.bitMask()) | options.MODE_VIEWPORT_WINDOWED.bitMask() | ((mode & options.MODE_RENDER_GL.bitMask()) != 0 ? options.MODE_RENDER_GL.bitMask() : options.MODE_RENDER_SOFT.bitMask()));
            } catch (Exception ex) {
                if (DebugMap._getInstance().isDebugLevelEnabled(DBUG_RENDER)) {
                    ex.printStackTrace();
                }
            } finally {
                if ((mode & options.MODE_RENDER_GL.bitMask()) == 0) {
                    frame.setResizable(true);
                    setPreferredSize(defaultSize);
                    frame.setPreferredSize(getPreferredSize());
                    frame.pack();
                    if (fullscreenFrame) {
                        frame.dispose();
                        frame = null;
                        fullscreenFrame = false;
                    }
                    /*notifyReset();*/
                }
            }
        } else if ((mode & options.MODE_VIEWPORT_WINDOWED.bitMask()) != 0 && ((mode & options.MODE_EXT_EXCLUSIVE.bitMask()) == 0)) {
            /**
             * to enter fullscreen, first set a valid display resolution
             */
            suspend();
            SortedSet<DisplayModeWrapper> dms = new TreeSet<DisplayModeWrapper>();
            if ((mode & options.MODE_RENDER_GL.bitMask()) == 0) {
                initialDisplayMode = device.getDisplayMode();
                for (DisplayMode dm : device.getDisplayModes()) {
                    if (bitDepth <= 0 || dm.getBitDepth() == bitDepth) {
                        dms.add(new DisplayModeWrapper(dm));
                    }
                }
            } else {
                initialDisplayMode = new DisplayModeWrapper(org.lwjgl.opengl.Display.getDisplayMode()).getDM();
                try {
                    for (org.lwjgl.opengl.DisplayMode dm : org.lwjgl.opengl.Display.getAvailableDisplayModes()) {
                        if ((bitDepth <= 0 || dm.getBitsPerPixel() == bitDepth) && dm.isFullscreenCapable()) {
                            dms.add(new DisplayModeWrapper(dm));
                        }
                    }
                } catch (LWJGLException ex) {
                    if (isDebugEnabled()) {
                        ex.printStackTrace();
                    }
                }
            }
            if (dms.isEmpty()) {
                if (isDebugEnabled()) {
                    System.err.println(log("cannot switch to full-screen : Either targetMin " + targetMin + " and/or targetMax " + targetMax + " and/or bitDepth " + bitDepth + " do not match a valid DisplayMode.", JXAenvUtils.LVL.SYS_ERR));
                }
                return;
            }
            DisplayModeWrapper dmw = null;
            if (target instanceof DisplayModeWrapper) {
                if (dms.contains(target)) {
                    for (DisplayModeWrapper orig : dms) {
                        if (orig.equals(target)) {
                            dmw = orig;
                        }
                    }
                }
            } else {
                if (targetMax instanceof DisplayModeWrapper) {
                    dms = dms.headSet(targetMax);
                }
                if (dms.isEmpty()) {
                    if (isDebugEnabled()) {
                        System.err.println(log("cannot switch to full-screen : Either targetMin " + targetMin + " and/or targetMax " + targetMax + " and/or bitDepth " + bitDepth + " do not match a valid DisplayMode.", JXAenvUtils.LVL.SYS_ERR));
                    }
                    return;
                }
                if (targetMin instanceof DisplayModeWrapper) {
                    dms = dms.tailSet(targetMin);
                }
                if (dms.isEmpty()) {
                    if (isDebugEnabled()) {
                        System.err.println(log("cannot switch to full-screen : Either targetMin " + targetMin + " and/or targetMax " + targetMax + " and/or bitDepth " + bitDepth + " do not match a valid DisplayMode.", JXAenvUtils.LVL.SYS_ERR));
                    }
                    return;
                }
                if (dms.size() > 1) {
                    while (dmw == null) {
                        dmw = ((DisplayModeWrapper) UIMessage.showSelectDialog(this, "Please, select your display mode (32 bits for colors are recommended) !", "Fullscreen mode", dms.toArray(new DisplayModeWrapper[]{}), new DisplayModeWrapper(displayMode)));
                        if (dmw == null && (mode & options.MODE_EXT_EXCLUSIVE.bitMask()) == 0) {
                            resume();
                            return;
                        }
                    }
                } else {
                    dmw = dms.first();
                }
            }
            /* DisplayMode is ready, go Fullscreen.*/
            displayMode = dmw.getDM();
            defaultSize = new Dimension(getWidth(), getHeight());
            try {
                if ((mode & options.MODE_RENDER_GL.bitMask()) == 0) {
                    if (!(frame instanceof JFrame)) {
                        frame = new JFrame(device.getDefaultConfiguration());
                        fullscreenFrame = true;
                    }
                    if (!frame.isDisplayable()) {
                        frame.setUndecorated(true);
                    }
                    Container parent = getParent();
                    if (parent instanceof Container) {
                        parent.remove(this);
                        parent.validate();
                    }
                } else {
                    org.lwjgl.opengl.Display.setDisplayMode(dmw.getDmGL());
                }
                /**
                 * disable soft windowed mode and enable full screen
                 */
                setMode(mode & ~(options.MODE_VIEWPORT_WINDOWED.bitMask() | options.MODE_RENDER_SOFT.bitMask())
                        | options.MODE_VIEWPORT_FULLSCREEN.bitMask()
                        | ((mode & options.MODE_RENDER_GL.bitMask()) != 0 ? options.MODE_RENDER_GL.bitMask()
                                : device.isFullScreenSupported() ? options.MODE_RENDER_HARD.bitMask() : options.MODE_RENDER_SOFT.bitMask()));
            } catch (LWJGLException ex) {
                if (DebugMap._getInstance().isDebugLevelEnabled(DBUG_RENDER)) {
                    ex.printStackTrace();
                }
            } finally {
                if ((mode & options.MODE_RENDER_GL.bitMask()) == 0) {
                    frame.getContentPane().removeAll();
                    frame.getContentPane().add(this);
                    if (isFullScreenSupported()) {
                        device.setFullScreenWindow(frame);
                    } else {
                        frame.setVisible(true);
                    }
                    createBufferStrategy();
                    try {
                        if (device.isDisplayChangeSupported()) {
                            device.setDisplayMode(displayMode);
                        }
                    } catch (IllegalArgumentException ex) {
                        if (DebugMap._getInstance().isDebugLevelEnabled(DBUG_RENDER)) {
                            ex.printStackTrace();
                        }
                        new UIMessage(true, ex, null);
                    } finally {
                        /*
                         * fix for mac os x
                         */
                        setPreferredSize(new Dimension(displayMode.getWidth(), displayMode.getHeight()));
                        frame.setSize(getPreferredSize());
                        frame.setResizable(false);
                        frame.pack();
                        frame.setExtendedState(frame.MAXIMIZED_BOTH);
                    }
                } else {
                    try {
                        org.lwjgl.opengl.Display.setFullscreen(true);
                    } catch (LWJGLException ex) {
                        if (DebugMap._getInstance().isDebugLevelEnabled(DBUG_RENDER)) {
                            ex.printStackTrace();
                        }
                        new UIMessage(true, ex, null);
                    }
                }
            }
        }
        resume();
    }

    /**
     * sets up the preferred size of the rendering scene to given size
     *
     * @param size the new preferred size
     */
    @Override
    public void setPreferredSize(Dimension size) {
        super.setPreferredSize(size);
        defaultSize = size;
    }
    /**
     * the ticker
     */
    protected boolean ticker = false;
    /**
     * the key event switch
     */
    protected boolean keyEvent = false;
    /**
     * the offscreen monitor
     */
    protected Monitor offscreenSynch;
    /**
     * the Composite instance effect
     *
     * @default AlphaComposite#SrcOver
     */
    protected Composite composite = AlphaComposite.SrcOver;

    /**
     * sets up a Composite instance to apply effect right before painting on the
     * screen
     *
     * @param cps the Composite instance to apply an effect
     * @default AlphaComposite#SrcOver
     */
    public void setComposite(Composite cps) {
        assert cps instanceof Composite : "use AlphaComposite.SrcOver as default !";
        composite = cps;
    }

    /**
     * returns the Composite instance that is applying effect before painting on
     * the screen
     *
     * @return the current associated Composite instance that is applying effect
     * on screen
     *
     * @see #setComposite(Composite)
     */
    public Composite getComposite() {
        return composite;
    }
    protected long lastFrameTime = 0;
    protected double lastFPS = 0.0;

    /**
     * return the last mesured frame-per-second (aka FPS). the framerate is
     * mesured each time the drawing process completes for this Sprite instance.
     *
     * @return FPS string, a float-point number with 2 decimals
     *
     * @see #markFPS()
     */
    public String getFPS() {
        Formatter f = new Formatter();
        f.format("%1$04.2f", lastFPS);
        if (isDebugEnabled()) {
            System.out.println("fps : " + lastFPS + " (~" + f.toString() + ")");
        }
        if (f.toString().matches("\\d*.\\d*")) {
            return f.toString();
        } else {
            return "0";
        }
    }

    /**
     * returns the last mesured frame rate in milliseconds. It tells how much
     * time it took to renderContents the last frame.
     *
     * @return a long value of milliseconds indicating the last mesured frame
     * rate.
     */
    public long getFramerate() {
        return (long) Math.round(1000000000.0 / lastFPS);
    }

    /**
     * mark for FPS calculation
     *
     * @return calculated FPS
     */
    protected double markFPS() {
        double fps_n = 0;
        long now = System.nanoTime();
        if (lastFrameTime == 0 || Math.abs(now - lastFrameTime) > 1000000000L || now - lastFrameTime == 0) {
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
        if (isDebugEnabled()) {
            System.out.println("Rendering Thread " + Thread.currentThread().getName() + " priority : " + Thread.currentThread().getPriority() + " FPS : " + lastFPS);
        }
        return fps_n;
    }

    /**
     * draws the offscreen to the graphics on given component (when there is
     * less than 2 buffers)
     *
     * @param graphics the Graphics instance
     *
     * @param c the component to draw on (Graphics g should be those from that
     * component)
     * @throws InterruptedException
     */
    protected void draw(Graphics2D graphics, Component c) throws InterruptedException {
        graphics.setRenderingHints(Sprite._getRenderingHints());
        if (isDebugEnabled()) {
            System.out.print("OK\n\r");
        }
        final Monitor monitor = offscreenSynch;
        synchronized (monitor) {
            Rectangle bounds = c.getBounds();
            graphics.translate(bounds.x, bounds.y);
            if ((mode & options.MODE_RENDER_GL.bitMask()) == 0 && numBuffers < 2) {
                Composite cps = graphics.getComposite();
                graphics.setComposite(composite);
                Sprite off = getOffscreen();
                off.runValidate();
                off.draw(c, graphics);
                graphics.setComposite(cps);
            }
            graphics.translate(-bounds.x, -bounds.y);
            monitor.notify();
        }
        markFPS();
        if (isDebugEnabled()) {
            System.out.println("\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\screen flushed - " + System.currentTimeMillis());
        }
    }
    /**
     * activate hud_helpcommands with {@link #_paintCommandsEnabled} switch;
     * activate hud_debugactivty with {@link #setDebugActivityEnabled(boolean)}
     * swtich; hud_user is for drawing text onto the display (must be managed
     * within the target app).
     *
     * @see #getHud_user()
     */
    protected HUD hud_helpcommands = HUD._new(this), hud_debugActivity = HUD._new(this);
    public HUD hud_user = HUD._new(this);

    /**
     * adds a short help String to the help console, which is activated with the
     * F1 key stroke
     *
     * @param key the help key to identify the helper
     *
     * @param helper the help String to add
     *
     * @param align the horizontal position on console
     *
     * @param valign the vertical position on console
     *
     * @param newLine dis/enables writing a new line
     *
     * @see POS#BOTTOM
     *
     * @see POS#TOP
     *
     * @see POS#RIGHT
     *
     * @see POS#LEFT
     *
     * @see POS#CENTER
     *
     * @see #getContent(String)
     * @deprecated use {@link #hud_user}
     */
    public void addHelper(String key, String helper, POS align, POS valign, boolean newLine) {
        hud_helpcommands.pushContent(key, helper, align, valign, newLine);
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
     * @see POS#BOTTOM
     *
     * @see POS#TOP
     *
     * @see POS#RIGHT
     *
     * @see POS#LEFT
     *
     * @see POS#CENTER
     *
     * @see POS#_drawLine(Map, GraphicsJAI, StringBuffer, StringBuffer, boolean)
     * @deprecated use {@link #hud_user}
     */
    public void drawString(Graphics2D graphics, String str, POS hpos, POS vpos, boolean newLine) {
        __drawString(graphics, str, hpos, vpos, newLine, 0);
    }

    /**
     * DO NOT USE, this is for internal callback
     *
     * @param graphics
     * @param str
     * @param hpos
     * @param newLine
     * @param vpos
     * @param z ignored
     * @deprecated use {@link #hud_user}
     */
    public void __drawString(Object graphics, String str, POS hpos, POS vpos, boolean newLine, float z) {
        hud_helpcommands.__drawString(graphics, str, hpos, vpos, newLine, true, false);
    }

    /**
     * returns the current Graphics as defined by the Canvas super-class
     *
     * @return the current Graphics instance as defined by the Canvas
     * super-class
     *
     * @see Canvas#getGraphics()
     */
    @Override
    public Graphics getGraphics() {
        return super.getGraphics();
    }

    /**
     * overriden to draw the contents with the software rendering mode
     *
     * @param g1
     * @see #update(Graphics)
     */
    @Override
    public void update(Graphics g1) {
        if ((mode & options.MODE_RENDER_GL.bitMask()) != 0) {
            screenThreadId = Thread.currentThread().getId();
            /**
             * nothing to do here
             */
        } else {
            screenThreadId = Thread.currentThread().getId();
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            try {
                final Monitor monitor0 = vSynch;
                synchronized (monitor0) {
                    rendering = true;
                    final Monitor monitor = offscreenSynch;
                    synchronized (monitor) {
                        ActionEvent e = new ActionEvent(this, hashCode(), "offscreen tasks");
                        Graphics2D offscreen = getOffscreenGraphics();
                        if (offscreen instanceof Graphics2D) {
                            doOffscreenTasks(offscreen, before_action_list);
                            doOffscreenTasks(offscreen, offscreen_action_list);
                            doOffscreenTasks(offscreen, after_action_list);
                            monitor.notify();
                            offscreen.dispose();
                        }
                    }
                    Graphics2D g = Sprite.wrapRendering(g1);
                    g.setBackground(Color.BLACK);
                    g.setColor(Color.CYAN);
                    Rectangle clip = g.getClipBounds();
                    Rectangle bounds = getBounds();
                    g.translate(-bounds.x, -bounds.y);
                    g.setClip(bounds);
                    draw(g, this);
                    g.translate(bounds.x, bounds.y);
                    g.setClip(clip);
                    g.dispose();
                    monitor0.notify();
                }
            } catch (InterruptedException e) {
                if (isDebugEnabled()) {
                    e.printStackTrace();
                }
            } finally {
                final Monitor monitor1 = vSynch;
                synchronized (monitor1) {
                    rendering = false;
                    monitor1.notifyAll();
                }
            }
        }
    }

    /**
     *
     */
    private ScreenPS screen = new ScreenPS(this);

    public ScreenPS getScreenProcessor() {
        return screen;
    }

    /**
     * Renders the scene as soon as possible (though it will run immediatelly on
     * EDT if available). MUST BE CALLED OUTSIDE OF AN OFFSCREEN LAYER.
     *
     * @see #doTask(Runnable)
     */
    public void doRender() {
        doTask(screen);
    }

    /**
     * Loading ASYNCHRONOUS on the {@linkplain GameActionLayer#_loadWorks}
     * Timer. Useful to load some stuff outside of EDT and 3.
     *
     * @param r
     * @see RenderingSceneGL#doTaskOnGL(Runnable)
     * @see #doTask(Runnable)
     */
    public void doLoadTask(Runnable r) {
        GameActionLayer._loadWorks.doLater(r);
    }

    /**
     * To CHAIN task with the rendering on the scene.<BR>
     * Proceeds with the specified task as soon as possible on the
     * renderContents EDT (not AWT/Swing EDT). The task should complete quickly
     * or the scene will stay unrefreshed till completion. However, the scene
     * can be rendered within a task by calling {@link #doRender()}. Unlike
     * {@linkplain #doTaskOnGL(Runnable)}, this method will run the task OUTSIDE
     * the rendering block.
     *
     * @param r
     * @see #doLoadTask(java.lang.Runnable)
     */
    public void doTask(final Runnable r) {
        if (!isScreenThread() && checkAlive()) {
            refresh.schedule(new TimerTask() {
                @Override
                public void run() {
                    r.run();
                }
            }, 0);
        } else {
            r.run();
        }
    }

    /**
     * actually runs on the EDT by scheduling from the RenderingScene
     * Timer-Thread.
     *
     * @see ScreenPS
     * @see RenderingSceneGL
     *
     */
    public void doTaskOnScreen(final Runnable r) {
        doTask(new Runnable() {

            @Override
            public void run() {
                try {
                    if (screen.isRenderOnEDT()) {
                        if (ThreadWorks.Swing.isEventDispatchThread()) {
                            r.run();
                        } else {
                            ThreadWorks.Swing.invokeAndWait(r);
                        }
                    } else {
                        r.run();
                    }
                } catch (Exception ex) {
                    if (isDebugRenderEnabled()) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * the update monitor
     *
     * public Monitor updateSynch = null; /** the update monitor switch
     *
     * private boolean updating = false;
     */
    /**
     * returns graphical metrics of the specified String for the specified
     * graphics.
     *
     * @param str the string to get graphical metrics from
     *
     * @param g the graphics to get to font metrics from
     *
     * @return the dimension of the string as if it were drawn on the Graphics
     * instance
     * <pre>return new Dimension(f.stringWidth(str), f.getMaxAscent() + f.getMaxDescent());</pre>
     *
     * @see Graphics#getFontMetrics()
     *
     * @see #_toGraphicalMetrics(Graphics)
     */
    public static Dimension _toGraphicalMetrics(
            String str, Graphics g) {
        FontMetrics f = _fontMetrics(g);
        return new Dimension(f.stringWidth(str), f.getMaxAscent() + f.getMaxDescent());
    }

    /**
     * @param g
     * @return
     */
    public static FontMetrics _fontMetrics(Graphics g) {
        return g.getFontMetrics();
    }

    /**
     * returns the default graphical metrics for one character on the specified
     * graphics
     *
     * @return the dimension of the character as if it were drawn on the
     * Graphics instance
     *
     * @param g the graphics to get the font metrics from
     *
     * @see Graphics#getFontMetrics()
     *
     * @see #_toGraphicalMetrics(String, Graphics)
     */
    public static Dimension _toGraphicalMetrics(
            Graphics g) {
        return _toGraphicalMetrics("G", g);
    }

    /**
     * returns the max characters that can be drawn each-nearby on the specified
     * Graphics instance . The graphics clip size is the available space.
     *
     * @param clipWidth available clip width on the graphics
     *
     * @param inset the insets you may want to apply
     *
     * @param g the graphics to take mesurement on
     *
     * @return the number of maximal characters that can get drawn continuously
     * without going out of the scene
     *
     * @see #_toGraphicalMetrics(Graphics)
     */
    public static int _toMaxCharsCOLS(int clipWidth, int inset, Graphics g) {
        return (int) Math.floor((float) (clipWidth - inset * 2) / (float) RenderingScene._toGraphicalMetrics(g).width);
    }

    /**
     * sets up the MediaTracker instance to associate to this RenderingScene
     * instance
     *
     * @param mt the MediaTracker instance to associate to this RenderingScene
     * instance
     */
    public void setMt(MediaTracker mt) {
        this.mt = mt;
    }

    /**
     * returns the current associated MediaTracker instance
     *
     * @return the current associated MediaTracker instance
     *
     * @see #setMt(MediaTracker)
     */
    public MediaTracker getMt() {
        return mt;
    }
    private GUIConsoleOutput console = null;

    /**
     * returns the current GUIConsoleOutput instance
     *
     * @return the current console GUIConsoleOutput instance
     * @see #getConsole()
     */
    public GUIConsoleOutput getGCO() {
        if (console == null) {
            console = new GUIConsoleOutput(this, new Dimension(getWidth(), getHeight()), consoleEnabled);
        }
        console.setRenderingScene(this);
        console.setSize(new Dimension(Math.round((float) getWidth() * 0.7f), Math.round((float) getHeight() * .7f)));
        console.setValign(POS.TOP);
        console.setAlign(POS.LEFT);
        return console;
    }

    public HUD getHud_debugActivity() {
        return hud_debugActivity;
    }

    public HUD getHud_helpcommands() {
        return hud_helpcommands;
    }

    /**
     * to draw text onto the display scene. <br>
     *
     */
    public HUD getHud_user() {
        return hud_user;
    }

    /**
     * returns a PrintStream instance to this RenderingScene Console. Console is
     * displayed with the key stroke [f2].
     *
     * @return a PrintStream instance got from the GUIConsoleOutput Component
     * @see GUIConsoleOutput
     */
    public PrintStream getPrintStream() {
        return getGCO().getPrintStream();
    }

    /**
     * dis/enables the console
     *
     * @param b dis/enables the console
     *
     * @see #getGCO()
     * @default false
     */
    public void setConsoleEnabled(boolean b) {
        getGCO().setConsoleEnabled(b);
        if (consoleEnabled != b && b) {
            getPrintStream().println("[::CONSOLE GUI (FPS=" + getFPS() + "Fps)::]");
        }
        consoleEnabled = b;
    }

    /**
     * returns true or false whether the console is enabled or not, resp.
     *
     * @return true or false
     *
     * @see #setConsoleEnabled(boolean)
     */
    public boolean isConsoleEnabled() {
        return consoleEnabled;
    }

    /**
     * returns the current Console instance
     *
     * @return the current Console instance
     *
     * @see GUIConsoleOutput#getConsole() *
     */
    public Console getConsole() {
        return getGCO().getConsole();
    }

    /**
     * returns true or false whether it is hardware accelerated or not, resp.
     * (active rendering is NOT OGL LWJGL rendering)
     *
     * @return true or false whether it is hardware accelerated or not, resp.
     * @see #setHardwareAccel(boolean)
     */
    public boolean isHardwareAccel() {
        return (mode & options.MODE_RENDER_HARD.bitMask()) != 0;
    }

    /**
     * returns true or false whether this RenderingScene is rendering with LWJGL
     * (OpenGL) or not, resp.
     *
     * @return true or false whether this RenderingScene is rendering with LWJGL
     * (OpenGL) or not, resp.
     */
    public boolean isLWJGLAccel() {
        return (mode & options.MODE_RENDER_GL.bitMask()) != 0;
    }

    /**
     * LWJGL is enabled for 3D-hardware Acceleration.
     *
     * @param b
     */
    public void setLWJGLAccel(final boolean b) {
        doTask(new Runnable() {
            @Override
            public void run() {
                try {
                    setMode(b ? options.MODE_RENDER_GL.bitMask() : isHardwareAccel() ? options.MODE_RENDER_HARD.bitMask() : options.MODE_RENDER_SOFT.bitMask());
                } catch (Exception ex) {
                    if (DebugMap._getInstance().isDebugLevelEnabled(DBUG_RENDER)) {
                        ex.printStackTrace();
                    }
                } finally {
                    if (b) {
                        offscreenBuffer = null;
                    }
                }
            }
        });
    }

    /**
     * dis/enables hardware acceleration. This method does nothing if
     * {@linkplain #isLWJGLAccel()} returns true. (this method should not be
     * called more than required, because it alters the rendering layers)
     *
     * @param hardwareAccel dis/enables hardware acceleration (direct "active"
     * rendering)
     * @see #isHardwareAccel()
     */
    public void setHardwareAccel(final boolean hardwareAccel) {
        if (isLWJGLAccel()) {
            return;
        }
        doTask(new Runnable() {
            @Override
            public void run() {
                try {
                    setMode(hardwareAccel ? options.MODE_RENDER_HARD.bitMask() : options.MODE_RENDER_SOFT.bitMask());
                } catch (Exception ex) {
                    if (DebugMap._getInstance().isDebugLevelEnabled(DBUG_RENDER)) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * adds a new KeyEventDispatcher to the list. If the same instance has been
     * found in the existing list, it is left single. Hence, each
     * keyEventDispatcher must be consistent with {@linkplain Object#equals()}
     * (see javadoc for this !).
     *
     * @param ked the KeyEventDispatcher instance to add to the list
     */
    public void addKeyboardEventsDispatcher(KeyEventDispatcher ked) {
        ctrlHDR.addKeyboardEventsDispatcher(ked);
    }
    private long hash = System.nanoTime();

    @Override
    public boolean equals(Object o) {
        return o == null ? false : o.hashCode() == hashCode();
    }

    @Override
    public int hashCode() {
        return (int) hash;

    }

    /**
     * removes the specified KeyEventDispatcher instance from the existing list
     *
     * @param ked the KeyEventDispatcher to remove from the existing list
     *
     * @see #addKeyboardEventsDispatcher(KeyEventDispatcher)
     */
    @SuppressWarnings("empty-statement")
    public void removeKeyboardEventsDispatcher(KeyEventDispatcher ked) {
        ctrlHDR.removeKeyboardEventsDispatcher(ked);
    }
    boolean keyboardOptions = Boolean.parseBoolean(rb.getString("interactiveOptions"));

    /**
     * dis/enables the keyboard options, function key strokes (F1, F2, etc.) for
     * interactive rendering settings of this RenderingScene.
     *
     * @param b dis/enables function key strokes for interactive rendering
     * settings
     *
     */
    public void setKeyboardOptionsEnabled(boolean b) {
        keyboardOptions = b;
    }

    /**
     * returns true or false, whether the keyboard options, function
     * key-strokes, are enabled or not resp.
     *
     * @return true or false, whether the keyboard optioins, functions
     * key-strokes, are enabled or not, resp.
     */
    public boolean isKeyboardOptionsEnabled() {
        return keyboardOptions;
    }

    /**
     * removes all the registered KeyEventDispatcher's from the current
     * KeyboardFocusManager instance, this RenderingScene instance, too
     *
     * @see #addKeyboardEvents()
     *
     * @see #addKeyboardEventsDispatcher(KeyEventDispatcher)
     */
    private void removeKeyboardEvents() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(ctrlHDR);
        if (isDebugRenderEnabled()) {
            System.out.println(JXAenvUtils.log("Just removed ControllersHandler.KeyEventDispatcher", JXAenvUtils.LVL.SYS_NOT));
        }
        keyEvent = false;
    }

    /**
     * adds all the registered KeyEventDispatcher's to the current
     * KeyboardFocusManager instance, incl. this RenderingScene instance.
     *
     * @see #addKeyboardEventsDispatcher(KeyEventDispatcher)
     */
    private void addKeyboardEvents() {
        if (!keyEvent) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(ctrlHDR);
            if (isDebugRenderEnabled()) {
                System.out.println(JXAenvUtils.log("Just added ControllersHandler.KeyEventDispatcher", JXAenvUtils.LVL.SYS_NOT));
            }
        } else {
            if (isDebugRenderEnabled()) {
                System.out.println(JXAenvUtils.log("Already added ControllersHandler.KeyEventDispatcher", JXAenvUtils.LVL.SYS_NOT));
            }
        }
        keyEvent = true;
    }
    /**
     * the paint commands playerStatus switch
     */
    public static boolean _paintCommandsEnabled = false;

    /**
     * returns true or false whether the multi-threading is enabled or not,
     * resp.
     *
     * @return
     * @see #setMultiThreadingEnabled(boolean)
     */
    @Override
    public boolean isMultiThreadingEnabled() {

        return (mode & options.MODE_EXT_MULTITHREADING.bitMask()) != 0;
    }

    /**
     * dis/enables the multi-threading mode CAUTION : the Sprite class is
     * altered by this method call.
     *
     * (this method should not be called more than required, because it alters
     * the rendering layers)
     *
     * @param b dis/enables the multi-threading mode
     * @default false
     *
     * @see Sprite#setMultiThreadingEnabled(boolean)
     */
    @Override
    public void setMultiThreadingEnabled(final boolean b) {
        doTask(new Runnable() {
            @Override
            public void run() {
                try {
                    setMode(b ? mode | options.MODE_EXT_MULTITHREADING.bitMask() : mode & ~options.MODE_EXT_MULTITHREADING.bitMask() | options._EXT_BIT.bitMask());
                } catch (Exception ex) {
                    if (DebugMap._getInstance().isDebugLevelEnabled(DBUG_RENDER)) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * switches to fullscreen mode or back to windowed mode
     */
    public void switchFullScreen() {
        switchFullScreen(null, null, null, 0);
    }

    /**
     *
     * @param targetMin minimal target resolution and refresh rate or null
     *
     * @param targetMax maximal target resolution and refresh rate or null or
     * equal to targetMin
     *
     * @param bitDepth target bitMask depth or 0
     * @throws IllegalArgumentException
     */
    public void switchFullScreen(final DisplayModeWrapper targetMin, final DisplayModeWrapper targetMax, final int bitDepth) {
        if (targetMin != null && targetMax != null) {
            if (targetMin.compareTo(targetMax) > 0) {
                throw new JXAException("targetMin " + targetMin + " must be lesser or equal to targetMax " + targetMax + ".");
            }
        }
        switchFullScreen(null, targetMin, targetMax, bitDepth);
    }

    /**
     *
     * @param target target resolution and refresh rate or null
     *
     */
    public void switchFullScreen(final DisplayModeWrapper target) {
        switchFullScreen(target, null, null, target.getDM().getBitDepth());
    }
    /**
     * the resource switch
     */
    private boolean resourceLoaded = false;

    /**
     * loads up the entire required resource for this RenderingScene instance
     *
     * (this method should not be called more than required, because it alters
     * the rendering layers)
     *
     * @return null
     *
     * @see #clearResource()
     */
    @Override
    public Object loadResource() {
        try {
            setMode(mode | bits._getAllBitRanges());
            getOffscreen();
            getGCO().loadResource();
            resourceLoaded = true;
        } catch (IllegalArgumentException ex) {

            ex.printStackTrace();

        } finally {
            return null;
        }
    }

    /**
     * clears the entire used resource for this RenderingScene instance (e.g.
     * the back-buffer is cleared off)
     *
     * @return
     * @see SpritesCacheManager#GLclearRegion()
     *
     * @see #loadResource()
     */
    @Override
    public Object clearResource() {
        if (!isResourceLoaded()) {
            return null;
        }
        if (offscreenBuffer != null) {
            offscreenBuffer.clearResource();
        }
        if (console != null) {
            console.clearResource();
        }
        GameActionLayer._loadWorks.cancelAllSchedules();
        switchLayersOff();
        resourceLoaded = false;
        return null;
    }

    /**
     * returns true or false whether the resources have been loaded or not,
     * resp.
     *
     * @return true or false
     *
     * @see #loadResource()
     */
    @Override
    public boolean isResourceLoaded() {
        return resourceLoaded;
    }

    /**
     *
     */
    @Override
    public Monitor[] getGroupMonitor() {
        return new Monitor[]{vSynch, offscreenSynch};
    }

    /**
     *
     */
    @Override
    public void setGroupMonitor(Monitor... tg) {
        vSynch = tg[0];
        offscreenSynch = tg[1];
    }
    /**
     * the displaying key events switch
     */
    protected boolean drawKeyEventsEnabled = true;

    /**
     * returns true or false whether displaying the key events playerStatus is
     * enabled or not, resp.
     *
     * @return
     * @see #setDrawKeyEventsEnabled(boolean)
     */
    public boolean isDrawKeyEventsEnabled() {
        return drawKeyEventsEnabled;
    }

    /**
     * dis/enables displaying the key events
     *
     * @param drawKeyEventsEnabled dis/enables displaying the key events
     */
    public void setDrawKeyEventsEnabled(boolean drawKeyEventsEnabled) {
        this.drawKeyEventsEnabled = drawKeyEventsEnabled;
    }

    /**
     *
     * @param ctrlHDR input handler (will replace the default renderingscene
     * input handler)
     */
    protected void setCtrlHDR(ControllersHandler ctrlHDR) {
        this.ctrlHDR = ctrlHDR;
    }

    /**
     *
     * @return
     */
    protected ControllersHandler getCtrlHDR() {
        return ctrlHDR;
    }
    /**
     * keyboard input handler
     */
    protected ControllersHandler ctrlHDR = new ControllersHandlerImpl("Keyboard handler", this);

    /**
     * All keyboard dispatchers are disabled if specified here.
     *
     * @param b
     *
     * @see #addKeyboardEventsDispatcher(java.awt.KeyEventDispatcher)
     */
    public void setKeyboardDisabled(final boolean b) {
        doTask(new Runnable() {

            @Override
            public void run() {
                setMode(b ? (mode | options.MODE_EXT_KEYBOARD_DISABLE.bitMask()) : (mode & ~options.MODE_EXT_KEYBOARD_DISABLE.bitMask()) | options._EXT_BIT.bitMask());
            }
        });
    }

    /**
     */
    public boolean isKeyboardDisabled() {
        return (mode & options.MODE_EXT_KEYBOARD_DISABLE.bitMask()) == 0;
    }

    /**
     * The {@link Keyboard alternative} input may be used if the (AWT)keyboard
     * is disabled here.
     *
     * @param b *
     */
    public void setSwingKeyboardDisabled(boolean b) {
        ctrlHDR.setAWTKeyboardDisabled(b);
    }

    /**
     * The {@link Keyboard alternative} input may be used if the (AWT)keyboard
     * is disabled here.
     *
     * @return
     */
    public boolean isSwingKeyboardDisabled() {
        return ctrlHDR.isAWTKeyboardDisabled();
    }

    /**
     * The (canvas) Mouse dispatchers are disabled here.
     */
    public void setMouseDisabled(final boolean b) {
        doTask(new Runnable() {

            @Override
            public void run() {
                setMode(b ? (mode | options.MODE_EXT_MOUSE_DISABLE.bitMask()) : (mode & ~options.MODE_EXT_MOUSE_DISABLE.bitMask()) | options._EXT_BIT.bitMask());
            }
        });
    }

    /**
     * The {@link Mouse alternative} input may be used if the (AWT)Mouse is
     * disabled here.
     */
    public boolean isMouseDisabled() {
        return 0 != (mode & options.MODE_EXT_MOUSE_DISABLE.bitMask());
    }

    boolean isSwingMouseDisabled = false;

    /**
     * The {@link Mouse alternative} input may be used if the (AWT)Mouse is
     * disabled here.
     */
    public void setSwingMouseDisabled(boolean b) {
        isSwingMouseDisabled = b;
    }

    /**
     * The {@link Mouse alternative} input may be used if the (AWT)Mouse is
     * disabled here.
     */
    public boolean isSwingMouseDisabled() {
        return isSwingMouseDisabled;
    }

    /**
     * JInput gamepad disable
     *
     * @param b
     * @see ControllersHandler
     */
    public void setGamepadDisabled(final boolean b) {
        doTask(new Runnable() {

            @Override
            public void run() {
                setMode(b ? (mode | options.MODE_EXT_GAMEPAD_DISABLE.bitMask()) : (mode & ~options.MODE_EXT_GAMEPAD_DISABLE.bitMask()) | options._EXT_BIT.bitMask());
            }
        });
    }

    /**
     * The {@link Mouse alternative} input may be used if the (AWT)Mouse is
     * disabled here.
     */
    public boolean isGamepadDisabled() {
        return (mode & options.MODE_EXT_MOUSE_DISABLE.bitMask()) != 0;
    }

    /**
     *
     * @param b
     */
    public void setSoundMute(boolean b) {
        SoundInput._mute = b;
    }

    /**
     *
     * @return
     */
    public boolean isSoundMute() {
        return SoundInput._mute;
    }

    /**
     * returns true or false, whether the fullscreen mode is enabled or not,
     * resp.
     *
     * @return true or false, whether the fullscreen mode is enabled or not,
     * resp.
     */
    public boolean isFullscreen() {
        return (mode & options.MODE_VIEWPORT_FULLSCREEN.bitMask()) != 0;
    }

    /**
     * returns true or false, whether this instance debugs its output or not,
     * resp. if isDebugEnabled(RenderingScene.class) return true, it will be
     * enabled debugging here.
     *
     *
     * @return true or false, whether this instance debugs its output or not,
     * resp.
     *
     */
    public boolean isDebugEnabled() {
        return DebugMap._getInstance().isDebuggerEnabled(RenderingScene.class);
    }

    /**
     * returns true or false, whether this instance debugs its output or not,
     * resp. if Debugmap isDebugLevelEnabled ({@link #DBUG_RENDER})
     *
     * @return true or false, whether this instance debugs its output or not,
     * resp.
     *
     */
    public static boolean isDebugRenderEnabled() {
        return DebugMap._getInstance().isDebugLevelEnabled(DBUG_RENDER);
    }

    /**
     * dis/enables the debugging mode
     *
     * @param b dis/enables the debugging mode of RenderingScene.class
     */
    public void setDebugEnabled(final boolean b) {
        DebugMap._getInstance().setDebuggerEnabled(b, RenderingScene.class);
    }

    /**
     * dis/enables the debugging DBUG_RENDER LEVEL
     *
     * @param b dis/enables the debugging mode of RenderingScene.class
     */
    public static void setDebugRenderEnabled(final boolean b) {
        DebugMap._getInstance().setDebugLevelEnabled(b, DBUG_RENDER);
    }
    /**
     *
     */
    public final static String _GL_VENDOR = "OGL vendor";
    /**
     *
     */
    public final static String _GL_RENDERER = "OGL renderer";
    /**
     *
     */
    public final static String _GL_VERSION = "OGL v.";
    /**
     *
     */
    public final static String _GL_EXTS = "OGL ext's";
    /**
     *
     */
    public final static String _IC_ACC = "Image accel.";
    /**
     *
     */
    public final static String _IC_VRAM = "Image vram";
    /**
     *
     */
    public final static String _BB_ACC = "back accel.";
    /**
     *
     */
    public final static String _BB_VRAM = "back vram";
    /**
     *
     */
    public final static String _FB_ACC = "front accel.";
    /**
     *
     */
    public final static String _FB_VRAM = "front vram";

    /**
     * returns a table of strings where the keys can be fetched in order or with
     * the following values :<br> valid after
     * {@link MemScratch#_isInitialized()}, set param ogl true :<br>
     * {@linkplain #_GL_VENDOR}, {@linkplain #_GL_RENDERER}, {@linkplain #_GL_VERSION}, {@linkplain #_GL_EXTS},<br>
     * <br> ImageCapabilities : <br> {@linkplain #_IC_ACC}, {@linkplain #_IC_VRAM}, {@linkplain #_BB_ACC}, {@linkplain #_BB_VRAM},
     * {@linkplain #_FB_ACC}, {@linkplain #_FB_VRAM}
     *
     * @param gc
     * @return
     */
    public static Map<String, String> _getGraphicsRendererInfo(GraphicsConfiguration gc, boolean ogl) {
        Map<String, String> info = new LinkedHashMap<String, String>();
        if (ogl) {
            info.put(_GL_VENDOR, GLHandler.GL_vendor);
            info.put(_GL_RENDERER, GLHandler.GL_renderer);
            info.put(_GL_VERSION, GLHandler.GL_version);
            info.put(_GL_EXTS, GLHandler.GL_extensions);
        }
        info.put(_IC_ACC, "" + gc.getImageCapabilities().isAccelerated());
        info.put(_IC_VRAM, "" + gc.getImageCapabilities().isTrueVolatile());
        info.put(_BB_ACC, "" + gc.getBufferCapabilities().getBackBufferCapabilities().isAccelerated());
        info.put(_BB_VRAM, "" + gc.getBufferCapabilities().getBackBufferCapabilities().isTrueVolatile());
        info.put(_FB_ACC, "" + gc.getBufferCapabilities().getFrontBufferCapabilities().isAccelerated());
        info.put(_FB_VRAM, "" + gc.getBufferCapabilities().getFrontBufferCapabilities().isTrueVolatile());
        return info;
    }

    public void setState(State valueOf) {
        state = valueOf;
    }

}
