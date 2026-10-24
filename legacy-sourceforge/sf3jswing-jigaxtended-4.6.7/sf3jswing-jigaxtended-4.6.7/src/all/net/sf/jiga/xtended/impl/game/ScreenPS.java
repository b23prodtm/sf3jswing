/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.game;

import java.security.AccessController;
import java.security.PrivilegedAction;
import net.sf.jiga.xtended.impl.game.RenderingScene.options;
import net.sf.jiga.xtended.impl.game.gl.RenderingSceneGL;
import net.sf.jiga.xtended.kernel.DebugMap;
import net.sf.jiga.xtended.kernel.Debugger;
import net.sf.jiga.xtended.kernel.Level;
import net.sf.jiga.xtended.kernel.ThreadWorks;

/**
 * the screen rendering Runnable, for the rendering processor RenderingScene.
 */
public class ScreenPS implements Runnable{

        private final RenderingScene rdrProcessor;

        //private boolean renderOnEDT = 
        public final static Level voidRenderOnEDT = DebugMap._getInstance().newDebugLevel();
        static {
                DebugMap._getInstance().setDebugLevelEnabled(Boolean.getBoolean("jxa.screen.disableEDT"), voidRenderOnEDT);
        }
        /**
         * @see #setRenderOnEDT(boolean)
         */
        public boolean isRenderOnEDT() {
                return !DebugMap._getInstance().isDebugLevelEnabled(voidRenderOnEDT);
        }

        /**
         * Should always render on the Event Dispatcher Thread to avoid
         * conflicting / artefacts with AWT/SWING. If you use the {@link RenderingSceneGL}, the native OpenGL context can conflict, and you  might need to disable this option (but test it!). 
         * <br>-Djxa.screen.disableEDT system property.
         *
         * @default true
         */
        public void setRenderOnEDT(boolean renderOnEDT) {
                DebugMap._getInstance().setDebugLevelEnabled(!renderOnEDT, voidRenderOnEDT);
        }

        ScreenPS(final RenderingScene rdrProcessor) {
                this.rdrProcessor = rdrProcessor;
        }

        @Override
        public void run() {
                AccessController.doPrivileged(new PrivilegedAction() {
                        @Override
                        public Object run() {
                                _run();
                                return null;
                        }
                }, rdrProcessor.acc);
        }

        protected void _run() {
                if (!rdrProcessor.isDisplayable() || !rdrProcessor.isVisible()) {
                        return;
                }
                /**
                 * also checked the associated DBUG_RENDER level
                 */
                rdrProcessor.setLoadingState(options.MODE_STATE_LOADING_GL.bitMask() | options.MODE_STATE_LOADING_LAYER.bitMask(), rdrProcessor.loading);
                Runnable r;
                if ((rdrProcessor.mode & options.MODE_RENDER_HARD.bitMask()) != 0) {
                        r = rdrProcessor.renderHardmode();
                } else if ((rdrProcessor.mode & options.MODE_RENDER_GL.bitMask()) != 0) {
                        r = ((RenderingSceneGL) rdrProcessor).renderGLmode();
                } else {
                        r = rdrProcessor.renderSoftmode();
                }
                try {
                        if (!isRenderOnEDT() || ThreadWorks.Swing.isEventDispatchThread()) {
                                r.run();
                        } else {
                                ThreadWorks.Swing.invokeAndWait(r);
                        }
                } catch (Exception e) {
                        if (rdrProcessor.isDebugEnabled()) {
                                e.printStackTrace();
                        }
                } finally {
                        if (rdrProcessor.isOption(options.MODE_STATE_LOADING_LAYER)) {
                                Thread.yield();
                        }
                }
        }
}
