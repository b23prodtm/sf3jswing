/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.game.test;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.*;
import javax.swing.*;
import static javax.swing.Action.NAME;
import javax.swing.Timer;
import net.sf.jiga.xtended.impl.Animation;
import net.sf.jiga.xtended.impl.GUIConsoleOutput;
import net.sf.jiga.xtended.impl.SoundInput;
import net.sf.jiga.xtended.impl.Sprite;
import net.sf.jiga.xtended.impl.game.*;
import net.sf.jiga.xtended.impl.game.TextLayout.POS;
import net.sf.jiga.xtended.impl.game.gl.*;
import net.sf.jiga.xtended.impl.game.scene.State;
import net.sf.jiga.xtended.impl.system.BufferIO;
import net.sf.jiga.xtended.impl.system.LoadListener2;
import net.sf.jiga.xtended.kernel.*;
import net.sf.jiga.xtended.ui.*;
import org.lwjgl.LWJGLException;

/**
 *
 * @author www.b23prodtm.info
 */
public class TestGameActionLayer extends GameActionLayer<RenderingSceneGL> implements Ant {

        /**
         * Available DEBUGGERS*
         */
        static Class[] CLASSDEBUGGERS = new Class[]{GLFX.class, Sprite.class, Animation.class, Model.class, GameActionLayer.class, RenderingScene.class, RenderingSceneGL.class, SpritesCacheManager.class, ExtensionsClassLoader.class, GUIConsoleOutput.class};
        static Level[] LEVELDEBUGGERS = new Level[CLASSDEBUGGERS.length];

        static {
                int i = 0;
                for (Class l : CLASSDEBUGGERS) {
                        LEVELDEBUGGERS[i] = DebugMap._getInstance().newDebugLevel();
                        DebugMap._getInstance().associateDebugLevel(l, LEVELDEBUGGERS[i]);
                        i++;
                }
        }

        /**
         * *
         */
        public TestGameActionLayer() {
                super("render test");
        }
        TestModel tm = new TestModel();

        public TestModel getTestModel() {
                return tm;
        }

        @Override
        protected void doLoad() {
                super.doLoad();
                RenderingScene rsc = getRenderingScene();
                Animation a = RenderingScene._getLogoAnimation(rsc, "/net/sf/jiga/xtended/impl/flame", 1, 16, "flame-", ".png", new Dimension(145, 256));
                a.setFrameRate(Math.round(1000. / 8.));
                tm.loadDefaults(rsc.isLWJGLAccel());
                tm.getImodel().loadResource();
                if (tm.getImodel() instanceof KeyEventDispatcher) {
                        rsc.addKeyboardEventsDispatcher((KeyEventDispatcher) tm.getImodel());
                }
        }
        SpriteGLHandler thanks = null;

        @Override
        protected void doUnload() {
                super.doUnload();
                if (tm.getImodel() instanceof KeyEventDispatcher) {
                        if (getRenderingScene() != null) {
                                getRenderingScene().removeKeyboardEventsDispatcher((KeyEventDispatcher) tm.getImodel());
                        }
                }
        }
        GLProgressBar modelLoadBar = new GLProgressBar();
        AnimationGLHandler flame = null;

        @Override
        protected void doLoadOnGL(RenderingSceneGL glAutoDrawable) {
                super.doLoadOnGL(glAutoDrawable);
                thanks = RenderingSceneGL._GLgetLogo(glAutoDrawable, "/net/sf/jiga/xtended/impl/sf3jswing_thanks.png", new Dimension(276, 852), true);
                flame = RenderingSceneGL._GLgetLogoAnimation(glAutoDrawable, "/net/sf/jiga/xtended/impl/flame", 1, 16, "flame-", ".png", new Dimension(145, 256));
                flame.setFadersEnabled(true);
                flame.setFrameRate(Math.round(1000. / 4.));
                final RenderingSceneGL rsc = getRenderingScene();
                RenderingSceneGL._GLloadModel(tm.getImodel(), rsc, new ArrayList<LoadListener2>(Collections.singletonList((LoadListener2) new LoadAdapter() {
                        @Override
                        public void loadBegin() {
                                super.loadBegin();
                                rsc.getGCO().newLogPacket("loading Model :  00%");
                                modelLoadBar.loadBegin();
                        }

                        @Override
                        public void loadProgress(int val, int max) {
                                super.loadProgress(val, max);
                                rsc.getGCO().deleteBufferChars(4);
                                rsc.getGCO().newLogPacket(String.format("%1$03d%%", Math.round((float) val / (float) max * 100f)));
                                modelLoadBar.loadProgress(val, max);
                        }

                        @Override
                        public void loadIsComplete() {
                                super.loadIsComplete();
                                rsc.getGCO().deleteBufferChars(4);
                                rsc.getGCO().newLogPacket("done" + Console.newLine);
                                modelLoadBar.loadIsComplete();
                        }

                        public int hashLinkToGLObject() {
                                return tm.getImodel().hashCode();
                        }
                })));
        }

        protected void renderToGL(RenderingSceneGL glAutoDrawable) {
                double z = 0.1 * (glAutoDrawable.getZDepth());
                RenderingSceneGL.__GLdrawLogo(glAutoDrawable, thanks, z,
                        new Dimension((int) Math.round(glAutoDrawable._GLZRatio(z) * 276), (int) Math.round(glAutoDrawable._GLZRatio(z) * 852)),
                        POS.CENTER, POS.CENTER,
                        GLFX.gfx.FX_DOWNREFLEXION_EXT | GLFX.gfx.FX_SCROLLING.bit() | GLFX.gfx.FX_VERTICAL.bit(),
                        new Point(0, glAutoDrawable.getHeight()),
                        null,
                        GLHandler._GL_TRANSFORM_TRANSLATE_BIT | GLHandler._GL_TRANSFORM_SCALE_BIT,
                        BufferIO._wrapd(new double[]{1, 1}),
                        null,
                        BufferIO._wrapd(new double[]{0, 0}));
                RenderingSceneGL.__GLdrawLogoAnimation(glAutoDrawable, flame, z + 1.,
                        new Dimension(145, 256),
                        POS.CENTER, POS.BOTTOM,
                        GLFX.gfx.FX_BACKGROUND.bit() | GLFX.gfx.FX_HORIZONTAL.bit() | GLFX.gfx.FX_REPEAT.bit() | GLFX.gfx.FX_DOWNREFLEXION_EXT,
                        new Point(0, glAutoDrawable.getHeight()),
                        null, GLHandler._GL_TRANSFORM_TRANSLATE_BIT,
                        null, null,
                        BufferIO._wrapd(new double[]{0, 50.}));
                modelLoadBar.render(glAutoDrawable, new Rectangle(5, 5, 200, 20), - 1, .2f);
                Model im = tm.getImodel();
                Rectangle timBds = im.getBounds();
                z += 3.;
                timBds.setSize((int) Math.round(glAutoDrawable._GLZRatio(z) * timBds.width), (int) Math.round(glAutoDrawable._GLZRatio(z) * timBds.height));
                timBds.setLocation(glAutoDrawable.getWidth() - timBds.width, glAutoDrawable.getHeight() - timBds.height);
                if (im instanceof XtendedModel) {
                        if (!((XtendedModel) im).getAnimsID_default().equals(((XtendedModel) im).getAnimName())) {
                                ((XtendedModel) im).GLanimPlayer_ChangeAnim(((XtendedModel) im).getAnimsID_default());
                        }
                        ((XtendedModel) im).GLanimPlayer_PlayAndLoop();
                }
                im.GLrunValidate();
                Model._GLRenderModel(glAutoDrawable, im, timBds, z,
                        GLFX.gfx.FX_DOWNREFLEXION_EXT,
                        new Point(0, glAutoDrawable.getHeight()),
                        Color.PINK,
                        GLHandler._GL_TRANSFORM_SCALE_BIT,
                        BufferIO._wrapd(new double[]{1., 1.}), null, null);
        }

        protected void renderToJava2D(Graphics2D graphics2D) {
                RenderingScene rsc = getRenderingScene();
                try {
                        Sprite thanks = RenderingScene._getLogo(rsc, "/net/sf/jiga/xtended/impl/sf3jswing_thanks.png", new Dimension(276, 852), true);
                        /*
                         * thanks.setTileModeEnabled(false);
                         */
                        RenderingScene.__drawLogo(rsc, graphics2D, thanks,
                                new Dimension(276, 852), POS.CENTER, POS.CENTER,
                                GLFX.gfx.FX_DOWNREFLEXION_EXT | GLFX.gfx.FX_SCROLLING.bit() | GLFX.gfx.FX_VERTICAL.bit(), new Point(0, 0), null);
                        RenderingScene._drawLogoAnimation(rsc, graphics2D, "/net/sf/jiga/xtended/impl/flame", 1, 16, "flame-", ".png",
                                new Dimension(145, 256), POS.CENTER, POS.BOTTOM,
                                GLFX.gfx.FX_DOWNREFLEXION_EXT, new Point(0, 0), null);
                        Model im = tm.getImodel();
                        if (!(im instanceof XtendedModel)) {
                                Rectangle timBds = im.getBounds();
                                timBds.setLocation(rsc.getWidth() - timBds.width, 20);
                                im.setBounds(timBds);
                                im.runValidate();
                                im.draw(rsc, graphics2D);
                        }
                } catch (InterruptedException ex) {
                        ex.printStackTrace();
                }
        }
        SoundInput si = null;
        Timer t = null;
        /**
         * the LogArea instance
         */
        LogArea log = new LogArea();
        RenderingSceneGL rs = null;
        MemStatusToolkit mstk = new MemStatusToolkit();

        public void initDemoScene(final Container applet) throws Throwable {
                t = new javax.swing.Timer(2000, new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                                System.out.println("PRINT LOOP " + System.currentTimeMillis());
                        }
                });
                si = new SoundInput();
                si.setUseJLayerEnabled(true);
                si.setRepeatEnabled(true);
                si.load("/net/sf/jiga/xtended/impl/opus-incertum-intro-test 1.mp3");
                applet.setLayout(new BorderLayout());
                final JProgressBar jpb = mstk.getMemStatus();
                jpb.setOrientation(JProgressBar.VERTICAL);
                final JProgressBar jpb2 = mstk.getHeapMemStatus();
                JPanel panel = new JPanel(new BorderLayout(), true);
                panel.add(new JLabel("Congratulations ! The JIGAXtended API has been built successfully !"), BorderLayout.NORTH);
                panel.add(jpb2, BorderLayout.SOUTH);
                applet.add(panel, BorderLayout.NORTH);
                applet.add(jpb, BorderLayout.WEST);
                final JProgressBar jpb1 = new JProgressMemory(JProgressBar.VERTICAL);
                jpb1.addMouseListener(new MouseAdapter() {
                        public void mouseClicked(MouseEvent e) {
                                jpb1.setIndeterminate(!jpb1.isIndeterminate());
                        }
                });
                jpb1.setIndeterminate(false);
                jpb1.setStringPainted(true);
                jpb1.setString("JPROGRESSMEMORY");
                ((JProgressMemory) jpb1).setLabels("INIT", "COMMIT", "OVERALL");
                ((JProgressMemory) jpb1).setValue(40);
                ((JProgressMemory) jpb1).setValue_committed(60);
                ((JProgressMemory) jpb1).setValue_init(80);
                applet.add(jpb1, BorderLayout.EAST);

                rs = new RenderingSceneGL(applet.getGraphicsConfiguration().getDevice());
                rs.setUseLWJGLKeyboardAndMouseEnabled(true);
                rs.setSwingKeyboardDisabled(true);
                si.setRenderingScene(rs);
                rs.setSoundMute(true);
                rs.setVisible(false);
                rs.addListener(new RenderingSceneListener() {
                        public void reset() {
                                applet.remove(rs);
                                if (rs.getToDevice() instanceof JFrame) {
                                        rs.getToDevice().dispose();
                                }
                                applet.add(rs, BorderLayout.CENTER);
                                applet.validate();
                        }

                        public void modeChanged(int mode) {
                        }

                        public void componentResized(ComponentEvent ce) {
                        }
                });
                rs.setDrawLogosEnabled(true);
                rs.setDrawFPSEnabled(true);
                rs.setConsoleEnabled(false);
                rs.getPrintStream().println("WELCOME TO " + getClass().getPackage().getSpecificationTitle() + " v." + getClass().getPackage().getSpecificationVersion() + " !");
                rs.addActionOffscreen(TestGameActionLayer.this);

                final Animation anim = RenderingScene._getLogoAnimation(applet, "/net/sf/jiga/xtended/impl/opengl", 0, 29, "", ".png", new Dimension(64, 30));
                anim.setReverseEnabled(true);
                JComponent renderSettingsPreview = Animation._makeAnimationJComponent(anim);
                javax.swing.Timer animT = new javax.swing.Timer(10, new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                                anim.play();
                        }
                });
                animT.start();
                final JPanel centerPanel = new JPanel(new GridBagLayout(), true);
                GridBagConstraints c = new GridBagConstraints();
                renderSettingsPreview.setPreferredSize(new Dimension(anim.getWidth() * 3, anim.getHeight() * 3));
                centerPanel.add(renderSettingsPreview, c);
                // TODO : splitpane
                applet.add(centerPanel, BorderLayout.CENTER);
                final JMenu menu = new JMenu("Scene");
                menu.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                                synchronized (debugMapSwitches) {
                                        for (SwitchDebugMapActionMenu s : debugMapSwitches) {
                                                s.update();
                                        }
                                }
                                menu.revalidate();
                                menu.repaint();
                        }
                });
                menu.add(new AbstractAction("on") {
                        public void actionPerformed(ActionEvent e) {
                                if (rs.isVisible()) {
                                        putValue(NAME, "on");
                                        applet.remove(rs);
                                        rs.setVisible(false);
                                        applet.add(centerPanel, BorderLayout.CENTER);
                                        applet.validate();
                                } else {
                                        putValue(NAME, "off");
                                        applet.remove(centerPanel);
                                        applet.add(rs, BorderLayout.CENTER);
                                        rs.setVisible(true);
                                        applet.validate();
                                        ThreadWorks.Swing.invokeLater(new Runnable() {

                                                public void run() {
                                                        rs.startRendering();
                                                }
                                        });
                                }
                        }
                });
                menu.add(new AbstractAction("pause") {
                        public void actionPerformed(ActionEvent e) {
                                this.putValue(NAME, rs.getState() != State.RUNNING ? "pause" : "resume");
                                if (rs.getState() == State.RUNNING) {
                                        rs.pauseRendering();
                                } else {
                                        rs.startRendering();
                                }
                        }
                });
                menu.add(new AbstractAction("switch fullscreen [F4]") {
                        public void actionPerformed(ActionEvent e) {
                                rs.switchFullScreen(null, null, rs.getDevice().getDisplayMode().getBitDepth());
                        }
                });
                menu.add(new AbstractAction("Test Windowed LWJGL on/off") {
                        public void actionPerformed(ActionEvent e) {
                                try {
                                        rs.stopRendering();
                                        if (rs.equals(org.lwjgl.opengl.Display.getParent())) {
                                                org.lwjgl.opengl.Display.setParent(null);
                                        } else {
                                                org.lwjgl.opengl.Display.setParent(rs);
                                                return;
                                        }
                                        org.lwjgl.opengl.Display.setDisplayMode(new org.lwjgl.opengl.DisplayMode(rs.getWidth(), rs.getHeight()));
                                        if (!org.lwjgl.opengl.Display.isCreated()) {
                                                org.lwjgl.opengl.Display.create();
                                        }
                                        UIMessage.showLightPopupMessage(new JLabel("LWJGL standard windowed Display successfully initalized"), null, menu, UIMessage.UI_CENTER);
                                } catch (LWJGLException ex) {
                                        ex.printStackTrace();
                                }
                        }
                });
                menu.add(new AbstractAction(rs.isConsoleEnabled() ? "set console off" : "set console on") {
                        public void actionPerformed(ActionEvent e) {
                                putValue(NAME, !rs.isConsoleEnabled() ? "set console off" : "set console on");
                                rs.setConsoleEnabled(!rs.isConsoleEnabled());
                        }
                });
                menu.add(new AbstractAction(rs.isLWJGLAccel() ? "set GL Hardware off" : "set GL Hardware on") {
                        public void actionPerformed(ActionEvent e) {
                                putValue(NAME, !rs.isLWJGLAccel() ? "set GL Hardware off" : "set GL Hardware on");
                                rs.setLWJGLAccel(!rs.isLWJGLAccel());
                        }
                });
                menu.add(new AbstractAction("reset") {
                        public void actionPerformed(ActionEvent e) {
                                rs.reset();
                        }
                });
                menu.add(new AbstractAction(rs.isDebugActivityEnabled() ? "set debug activity off" : "set debug activity on") {
                        public void actionPerformed(ActionEvent e) {
                                putValue(NAME, !rs.isDebugActivityEnabled() ? "set debug activity off" : "set debug activity on");
                                rs.setDebugActivityEnabled(!rs.isDebugActivityEnabled());
                                log.pushNewLog("Pixel scale factor " + org.lwjgl.opengl.Display.getPixelScaleFactor());
                        }
                });
                menu.add(new AbstractAction(!rs.isSoundMute() ? "set sound mute" : "set sound on") {
                        public void actionPerformed(ActionEvent e) {
                                putValue(NAME, rs.isSoundMute() ? "set sound mute" : "set sound on");
                                rs.setSoundMute(!rs.isSoundMute());
                        }
                });
                menu.add(new AbstractAction(!rs.isSwingKeyboardDisabled() ? "disable Swing keyboard" : "enable Swing keyboard") {
                        public void actionPerformed(ActionEvent e) {
                                putValue(NAME, rs.isSwingKeyboardDisabled() ? "disable Swing keyboard" : "enable Swing keyboard");
                                rs.setSwingKeyboardDisabled(!rs.isSwingKeyboardDisabled());
                        }
                });
                menu.add(new AbstractAction(!rs.isSwingMouseDisabled() ? "disable Swing mouse" : "enable Swing mouse") {
                        public void actionPerformed(ActionEvent e) {
                                putValue(NAME, rs.isSwingMouseDisabled() ? "disable Swing mouse" : "enable Swing mouse");
                                rs.setSwingMouseDisabled(!rs.isSwingMouseDisabled());
                        }
                });
                menu.addSeparator();
                menu.add(new AbstractAction(rs.isLoading() ? "set loading state off" : "set loading state on") {
                        public void actionPerformed(ActionEvent e) {
                                putValue(NAME, !rs.isLoading() ? "set loading state off" : "set loading state on");
                                rs.setLoading(!rs.isLoading());
                        }
                });
                menu.add(new AbstractAction("set render glyphs " + (rs.is_GL_renderGlyphsEnabled() ? "off" : "on")) {
                        public void actionPerformed(ActionEvent e) {
                                putValue(NAME, "set render glyphs " + (!rs.is_GL_renderGlyphsEnabled() ? "off" : "on"));
                                rs.set_GL_renderGlyphsEnabled(!rs.is_GL_renderGlyphsEnabled());
                        }
                });
                menu.add(new AbstractAction((!Sprite._paintStatus ? "enable" : "disable") + " Sprite._paintStatus") {
                        public void actionPerformed(ActionEvent e) {
                                putValue(NAME, (Sprite._paintStatus ? "enable" : "disable") + " Sprite._paintStatus");
                                Sprite._paintStatus = !Sprite._paintStatus;
                        }
                });
                menu.add(new AbstractAction((GLFX.isFXdisabled() ? "enable" : "disable") + " GLFX.gfx") {
                        public void actionPerformed(ActionEvent e) {
                                putValue(NAME, (!GLFX.isFXdisabled() ? "enable" : "disable") + " GLFX.gfx");
                                if (GLFX.isFXdisabled()) {
                                        GLFX.enableFX();
                                } else {
                                        GLFX.disableFX();
                                }
                        }
                });
                menu.addSeparator();
                menu.add(new AbstractAction((!GLHandler.ext._GLdisableEXT ? "enable" : "disable") + " GLHandler.ext._GLdisableEXT") {
                        public void actionPerformed(ActionEvent e) {
                                putValue(NAME, (GLHandler.ext._GLdisableEXT ? "enable" : "disable") + " GLHandler.ext._GLdisableEXT");
                                GLHandler.ext._GLdisableEXT = !GLHandler.ext._GLdisableEXT;
                        }
                });
                applet.add(log, BorderLayout.SOUTH);
                log.setPreferredSize(new Dimension(applet.getWidth(), (int) ((float) applet.getHeight() / 5f)));
                JMenuBar bar = new JMenuBar();
                bar.add(log.getActionMenu());
                bar.add(log.getHelpActionMenu());
                bar.add(menu);
                bar.add(getCodeDebugOptions());
                if (applet instanceof JApplet) {
                        ((JApplet) applet).setJMenuBar(bar);
                } else if (applet instanceof JFrame) {
                        ((JFrame) applet).setJMenuBar(bar);
                }
                applet.validate();
                initialized = true;
        }

        private JMenu getCodeDebugOptions() {
                JMenu menu = new JMenu("Debugging");
                menu.add(new AbstractAction(JXAenvUtils._debugSys ? "set Kernel debug off" : "set Kernel debug on") {
                        public void actionPerformed(ActionEvent e) {
                                putValue(NAME, !JXAenvUtils._debugSys ? "set Kernel debug off" : "set Kernel debug on");
                                JXAenvUtils._debugSys = (!JXAenvUtils._debugSys);
                        }
                });
                menu.add(new AbstractAction(JXAenvUtils._debug ? "set JXA debug off" : "set JXA debug on") {
                        public void actionPerformed(ActionEvent e) {
                                putValue(NAME, !JXAenvUtils._debug ? "set JXA debug off" : "set JXA debug on");
                                JXAenvUtils._debug = (!JXAenvUtils._debug);
                        }
                });
                menu.add(new SwitchDebugMapActionMenu("Render", RenderingScene.DBUG_RENDER));
                menu.add(new SwitchDebugMapActionMenu("Render Low-Level", Sprite.DBUG_RENDER_LOW));
                menu.add(new SwitchDebugMapActionMenu("Render Very-Low-Level", GLFX.gfx.DBUG));
                menu.addSeparator();
                menu.add(new SwitchDebugMapActionMenu("Disable BufferIO Reuse", BufferIO.DBUG_disableREUSE));
                menu.add(new SwitchDebugMapActionMenu("Enable HUD PBO Stream MODE", HUD.PBOModeSTREAMenabled));
                menu.add(new SwitchDebugMapActionMenu("Disable Rendering on EDT", ScreenPS.voidRenderOnEDT));
                menu.addSeparator();
                int i = 0;
                for (final Class l : CLASSDEBUGGERS) {
                        final Level level = LEVELDEBUGGERS[i++];
                        menu.add(new SwitchDebugMapActionMenu(l.getSimpleName(), l, level));
                }
                return menu;
        }

        public void stop() {
                if (t instanceof javax.swing.Timer) {
                        t.stop();
                }
                si.stop();
                log.pushNewLog(Console.newLine + "demo stopped " + System.currentTimeMillis());
                if (rs instanceof RenderingScene) {
                        rs.stopRendering();
                }
        }

        public void start() {
                log.pushNewLog(Console.newLine + "demo started " + System.currentTimeMillis());
                if (t instanceof javax.swing.Timer) {
                        if (!t.isRunning()) {
                                t.start();
                        }
                }
                si.play();
                if (rs instanceof RenderingScene) {
                        if (rs.isVisible()) {
                                rs.startRendering();
                        }
                }
        }

        public Vector<Action> getLoadLayers() {
                Vector<Action> v = new Vector<Action>();
                v.add(new AbstractAction("test loading layer") {
                        public void actionPerformed(ActionEvent e) {
                                try {
                                        Thread.sleep(5000);
                                } catch (InterruptedException ex) {
                                        ex.printStackTrace();
                                }
                        }
                });
                return v;
        }

        public DisplayInterface getSplash() {
                try {
                        return Display._Display(ExtensionsInstaller.splashPic, null);
                } catch (Exception ex) {
                        if (JXAenvUtils._debug) {
                                ex.printStackTrace();
                        }
                        return null;
                }
        }
        boolean initialized = false;

        public boolean isInitialized() {
                return initialized;
        }

        public Runnable shutdownHook() {
                return null;
        }

        public boolean open(Object data) {
                if (rs instanceof RenderingScene) {
                        Model m = getTestModel().getImodel();
                        if (m instanceof KeyEventDispatcher) {
                                rs.removeKeyboardEventsDispatcher((KeyEventDispatcher) m);
                        }
                }
                boolean ret = getTestModel().open(data);
                if (rs instanceof RenderingScene) {
                        Model m = getTestModel().getImodel();
                        if (m instanceof KeyEventDispatcher) {
                                rs.addKeyboardEventsDispatcher((KeyEventDispatcher) m);
                        }
                        rs.reset();
                }
                return ret;
        }

        public boolean save(String file) {
                return false;
        }

        public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[]{DataFlavor.javaFileListFlavor};
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
                return JOptionPane.YES_OPTION == UIMessage.showConfirmDialog(null, "Do you wish to import a " + flavor + " ?", "Confirm import", JOptionPane.YES_NO_OPTION);
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                return null;
        }
        static Set<SwitchDebugMapActionMenu> debugMapSwitches = Collections.synchronizedSet(new HashSet<SwitchDebugMapActionMenu>());

        class SwitchDebugMapActionMenu extends AbstractAction {

                private final String setWhat;
                private final Class<? extends Debugger> debuggerClass;
                private final Level debugLevel;

                public SwitchDebugMapActionMenu(String setWhat, Class<? extends Debugger> checkWhat) {
                        this(setWhat, checkWhat, null);
                }

                public SwitchDebugMapActionMenu(String setWhat, Level checkWhatLevel) {
                        this(setWhat, null, checkWhatLevel);
                }

                public SwitchDebugMapActionMenu(String setWhat, Class<? extends Debugger> debugger, Level level) {
                        super();
                        this.setWhat = setWhat;
                        this.debuggerClass = debugger;
                        this.debugLevel = level;
                        putValue(NAME, textSwitch(false));
                        debugMapSwitches.add(this);
                }

                public void update() {
                        putValue(NAME, textSwitch(false));
                }

                public String textSwitch(boolean nextState) {
                        if (debuggerClass != null) {
                                return "set debug " + setWhat + ((nextState ? !DebugMap._getInstance().isDebuggerEnabled(debuggerClass) : DebugMap._getInstance().isDebuggerEnabled(debuggerClass)) ? " off" : " on");
                        } else {
                                return "set debug " + setWhat + ((nextState ? !DebugMap._getInstance().isDebugLevelEnabled(debugLevel) : DebugMap._getInstance().isDebugLevelEnabled(debugLevel)) ? " off" : " on");
                        }
                }

                public void actionPerformed(ActionEvent e) {
                        putValue(NAME, textSwitch(true));
                        if (debuggerClass != null) {
                                if (debugLevel != null) {
                                        DebugMap._getInstance().setDebuggerEnabled(!DebugMap._getInstance().isDebuggerEnabled(debuggerClass), debuggerClass, debugLevel);
                                } else {
                                        DebugMap._getInstance().setDebuggerEnabled(!DebugMap._getInstance().isDebuggerEnabled(debuggerClass), debuggerClass);
                                }
                        } else {
                                DebugMap._getInstance().setDebugLevelEnabled(!DebugMap._getInstance().isDebugLevelEnabled(debugLevel), debugLevel);
                        }
                }
        }
}
