/*

 * To change this template, choose Tools | Templates

 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.game;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.security.AccessController;
import java.security.PrivilegedAction;
import static java.util.logging.Level.ALL;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import net.sf.jiga.xtended.JXAException;
import net.sf.jiga.xtended.impl.game.RenderingScene.options;
import net.sf.jiga.xtended.impl.game.gl.GLFX;
import net.sf.jiga.xtended.impl.game.gl.RenderingSceneGL;
import net.sf.jiga.xtended.kernel.*;

/**
 * A GameActionLayer is provided to a RenderingScene instance for specifying
 * various Action's, such the offscren rendering. Each instance has a unique id,
 * the hash-code. Used with the RenderingScene, the registered GameActionLayer's
 * receive the Graphics/RenderingScene buffer throughout their synchronized
 * method. Inside the layers, the actionPerformed should retrieve the
 * synchronized Graphics/RenderingScene buffer with their own getter
 * {@link #getGraphicsLayer() getGraphicsLayer()} or {@link #getGLC()}.
 * <b>It cannot be used simultaneously on the Java2D buffer and the OpenGL
 * buffer, only one of the two is used at a time, depending on the
 * RenderingScene {@linkplain RenderingScene#getRenderMode() render mode}.</b>
 *
 * @author www.b23prodtm.info
 */
public abstract class GameActionLayer<R extends RenderingScene> implements Threaded, Resource, RenderingSceneListener, RenderingSceneComponent<R> {

        @Override
        public void componentResized(ComponentEvent e) {
        }

        /**
         * override to perform a task each time
         * {@linkplain #actionPerformed(ActionEvent)} is called, immediately
         * before any other action (no loading state is monitored)
         */
        protected void performTask() {
        }

        /**
         * override to perform a task after all loading of this layer has
         * completed
         */
        protected void doPhysics() {
        }

        /**
         * override to perform a task of rendering to an RenderingScene after
         * all loading of this layer has completed
         * <BR> ALL OpenGL calls should be encapsulated in one or many {@link GLFX#_GLpushRender(java.lang.String, java.lang.Object, java.lang.Object[], java.lang.Class[])
         * } to avoid graphics artefacts, because of the RenderingScene layer
         * contexts that accumulate FX passes.
         */
        protected void renderToGL(RenderingSceneGL context) {
        }

        /**
         * override to perform a task of rendering to a Java2D context after all
         * loading of this layer has completed
         */
        protected void renderToJava2D(Graphics2D graphics2D) {
        }

        /**
         * override to make loading of data on the GL Thread (e.g. Texture VRAM
         * loading)
         */
        protected void doLoadOnGL(RenderingSceneGL context) {
        }

        /**
         * override to make loading of data on another Thread (e.g. disk
         * caching)
         */
        protected void doLoad() {
        }

        /**
         * override to make unloading of data and free resources on the GL
         * Thread (e.g. freeing Texture VRAM)
         */
        protected void doUnloadFromGL(RenderingSceneGL context) {
        }

        /**
         * override to make unloading of data and free resources on another
         * Thread (e.g. freeing disk cache)
         */
        protected void doUnload() {
        }
        /**
         * the hash-code
         */
        long hash = System.nanoTime();
        private boolean multiThreading = JXAenvUtils._multiThreading;

        private String name;

        public String getName() {
                return name;
        }

        public void setName(String name) {
                this.name = name == null ? "" : name;
        }

        /**
         * creates a new instance
         *
         * @param s the name of this GameActionLayer instance
         * @see AbstractAction#AbstractAction(String)
         */
        public GameActionLayer(String s) {
                this.name = s;
                setLoadState(RSRC, CLEARED);
                setLoadState(GL_RSRC, GL_CLEARED);
                setGroupMonitor(new Monitor());
        }

        /**
         * returns the hash-code of this GameActionLayer instance
         *
         * @return the hash-code of this GameActionLayer instance
         */
        @Override
        public int hashCode() {
                return (int) hash;
        }

        /**
         * returns true or false, whether this GameActionLayer is equal to the
         * specified Object or not, resp.
         *
         * @param o the Object to compare to, hash-codes are generated and
         * compared with each other.
         * @return true or false, whether this GameActionLayer is equal to the
         * specified Object or not, resp.
         */
        @Override
        public boolean equals(Object o) {
                return o == null ? false : o.hashCode() == hashCode();
        }

        @Override
        public String toString() {
                return (String) name + " hash:" + hashCode();
        }
        /**
         * the graphics layer to paint on
         */
        private Object layer = null;

        /**
         * sets up the Graphics layer to be used, essentially used to perform a
         * drawing on the same buffer when using a buffer strategy and multiple
         * layers.
         *
         * @param g the available Graphics layer to be used
         */
        public final void setGraphicsLayer(Graphics2D g) {
                this.layer = g;
        }

        /**
         * returns the current Graphics layer if available
         *
         * @return the current Graphics layer
         */
        public final Graphics2D getGraphicsLayer() {
                return layer instanceof Graphics2D ? (Graphics2D) layer : null;
        }

        /**
         * sets up the RenderingScene layer to be used, essentially used to
         * perform a drawing on the same buffer when using a buffer strategy and
         * multiple layers.
         *
         * @param glc the available RenderingScene layer to be used
         */
        public final void setGLC(RenderingSceneGL glc) {
                this.layer = glc;
        }

        /**
         * returns the current RenderingScene layer if available
         *
         * @return the current RenderingScene layer
         */
        public final RenderingSceneGL getGLC() {
                return layer instanceof RenderingSceneGL ? (RenderingSceneGL) layer : null;
        }
        boolean rendering = false;
        private Monitor renderMonitor;

        /**
         * runs this layer tasks in order
         *
         */
        public final void actionPerformed() {
                synchronized (renderMonitor) {
                        rendering = false;
                        performTask();
                        if (isResourceLoaded()) {
                                rendering = true;
                                doPhysics();
                                if (layer instanceof Graphics2D) {
                                        renderToJava2D((Graphics2D) layer);
                                } else if (layer instanceof RenderingScene) {
                                        renderToGL((RenderingSceneGL) layer);
                                }
                                rendering = false;
                        } else {
                                loadResource();
                        }
                        renderMonitor.notifyAll();
                }
        }

        /**
         * executes the {@linkplain #doLoad()} method if necessary on another
         * Thread. Also, {@linkplain #doLoadOnGL()} is posted on the GL tasks
         * stack, if necessary.
         */
        @Override
        public final Object loadResource() {
                if (isResourceLoaded()) {
                        return null;
                }
                final Runnable loadGL = new Runnable() {

                        @Override
                        public void run() {
                                doLoadOnGL((RenderingSceneGL) rsc);
                                setLoadState(GL_RSRC, GL_LOADED);
                        }
                };
                return loadResource_r(new Runnable() {

                        @Override
                        public void run() {
                                /**
                                 * privileged callback (applet purpose)
                                 */
                                AccessController.doPrivileged(new PrivilegedAction() {

                                        @Override
                                        public Object run() {
                                                _run();
                                                return null;
                                        }
                                }, rsc != null ? rsc.acc : null);
                        }

                        private void _run() {
                                doLoad();
                                setLoadState(RSRC, LOADED);
                                loadResource_r(this, loadGL);
                        }

                }, loadGL);
        }

        /**
         * @return whether OpenGL accelearation is available or not
         */
        protected boolean isLWJGL() {
                return rsc == null ? false : (rsc.getRenderMode() & options.MODE_RENDER_GL.bitMask()) != 0;
        }

        private Object loadResource_r(Runnable load, Runnable loadGL) {
                if ((getLoadState(RSRC) & CLEARED) != 0) {
                        /**
                         * Runnable load on Screen Timer Thread (sometimes
                         * equals the GL Thread)
                         */
                        setLoadState(RSRC, LOADING);
                        _loadWorks.doLater(load);
                } else if (0 != (getLoadState(RSRC) & LOADED) && 0 == (getLoadState(GL_RSRC) & GL_LOADING) && isLWJGL()) {
                        /**
                         * Runnable load on GL Thread
                         */
                        setLoadState(GL_RSRC, GL_LOADING);
                        ((RenderingSceneGL) rsc).doTaskOnGL(loadGL);
                }
                return loadState;
        }
        /**
         * WARNING : if you make cancel operations, those handled Threads may
         * alter the state of layers
         */
        public final static ThreadWorks _loadWorks = new ThreadWorks(GameActionLayer.class.getSimpleName());

        /**
         * executes the {@linkplain #doUnload()} method if necessary on another
         * Thread. Also, {@linkplain #doUnloadFromGL()} is posted on the GL
         * tasks stack, if necessary.
         */
        @Override
        public final Object clearResource() {
                if (!isResourceLoaded()) {
                        return null;
                }
                final Runnable unload = new Runnable() {

                        @Override
                        public void run() {
                                synchronized (renderMonitor) {
                                        while (rendering) {
                                                try {
                                                        renderMonitor.wait(1000);
                                                } catch (InterruptedException ex) {
                                                        if (JXAenvUtils._debug) {
                                                                ex.printStackTrace();
                                                        }
                                                        rendering = false;
                                                }
                                        }
                                }
                                AccessController.doPrivileged(new PrivilegedAction() {

                                        @Override
                                        public Object run() {
                                                _run();
                                                return null;
                                        }
                                }, rsc != null ? rsc.acc : null);
                        }

                        private void _run() {
                                if (isLWJGL()) {
                                        waitFor(GL_RSRC, GL_CLEARED);
                                }
                                doUnload();
                                setLoadState(RSRC, CLEARED);
                        }
                };
                return clearResource_r(unload, new Runnable() {

                        @Override
                        public void run() {
                                doUnloadFromGL((RenderingSceneGL) rsc);
                                setLoadState(GL_RSRC, GL_CLEARED);
                                clearResource_r(unload, this);
                        }
                });
        }

        private Object clearResource_r(Runnable unload, Runnable unloadGL) {
                if ((getLoadState(GL_RSRC) & GL_LOADED) != 0 && isLWJGL()) {
                        /**
                         * Runnable load on GL Thread
                         */
                        setLoadState(GL_RSRC, GL_LOADING);
                        ((RenderingSceneGL) rsc).doTaskOnGL(unloadGL);
                } else if (0 != (getLoadState(RSRC) & LOADED) && 0 == (getLoadState(GL_RSRC) & GL_LOADING)) {
                        /**
                         * Runnable load on Screen Timer Thread (sometimes
                         * equals the GL Thread)
                         */
                        setLoadState(RSRC, LOADING);
                        _loadWorks.doLater(unload);
                }
                return loadState;
        }
        protected final static BitStack bits = new BitStack();
        public final static int RSRC = bits._newBitRange();
        public final static int CLEARED = bits._newBit(RSRC);
        public final static int LOADING = bits._newBit(RSRC);
        public final static int LOADED = bits._newBit(RSRC);
        public final static int GL_RSRC = bits._newBitRange();
        public final static int GL_CLEARED = bits._newBit(GL_RSRC);
        public final static int GL_LOADING = bits._newBit(GL_RSRC);
        public final static int GL_LOADED = bits._newBit(GL_RSRC);
        private int loadState = 0;

        /**
         * returns the current bitMask-state of this layer :
         * {@linkplain #CLEARED}, {@linkplain #LOADED} or {@linkplain #LOADING}
         * (specify {@linkplain #RSRC})
         * {@linkplain #GL_CLEARED}, {@linkplain #GL_LOADED} or
         * {@linkplain #GL_LOADING} (specify {@linkplain #GL_RSRC})
         */
        protected int getLoadState(int rsrcRange) {
                return bits._getMask(rsrcRange) & loadState;
        }

        protected final synchronized void setLoadState(int rsrcRange, int state) {
                loadState = (loadState & ~bits._getMask(rsrcRange)) | state;
                GameActionLayer.this.notifyAll();
        }

        /**
         * wait for a particular state on this layer
         */
        protected synchronized void waitFor(int rsrcType, int state) {
                if (0 == (rsrcType & (RSRC | GL_RSRC))) {
                        throw new JXAException(JXAException.LEVEL.APP, "unknown parameter rsrcType");
                }
                if (0 == (state & bits._getAllBits())) {
                        throw new JXAException(JXAException.LEVEL.APP, "unknown parameter state");
                }
                try {
                        while (0 == (getLoadState(rsrcType) & state)) {
                                if (!_loadWorks.checkAlive()) {
                                        break;
                                }
                                wait(1000);
                        }
                } catch (InterruptedException ex) {
                        Logger.getLogger(JXAenvUtils.LOGGER_NAME).log(ALL, null, ex);
                }
        }

        /**
         *
         */
        @Override
        public final boolean isResourceLoaded() {
                return (getLoadState(RSRC) & LOADED) != 0 && (isLWJGL() ? (getLoadState(GL_RSRC) & GL_LOADED) != 0 : true);
        }

        /**
         *
         */
        @Override
        public final boolean isMultiThreadingEnabled() {
                return JXAenvUtils._multiThreading;
        }

        /**
         * changes the global threading mode
         */
        @Override
        public final void setMultiThreadingEnabled(boolean b) {
                multiThreading = JXAenvUtils._multiThreading = b;
        }

        /**
         * used by the RenderingScene to notify a reset. It clears the state
         * bits.
         */
        @Override
        public final void reset() {
                synchronized (renderMonitor) {
                        rendering = false;
                        renderMonitor.notifyAll();
                }
                setLoadState(GL_RSRC | RSRC, GL_CLEARED | CLEARED);
        }

        /**
         * used by the RenderingScene to notify a change of mode.
         */
        @Override
        public final void modeChanged(int modeBit) {
                if (rsc instanceof RenderingScene) {
                        if ((modeBit & options._EXT_BIT.bitMask()) != 0) {
                                setMultiThreadingEnabled(rsc.isMultiThreadingEnabled());
                                setHardwareAccel(rsc.isHardwareAccel());
                        }
                }
        }

        /**
         * returns the current associated RenderingScene
         */
        @Override
        public final R getRenderingScene() {
                return rsc;
        }

        @Override
        public boolean isHardwareAccel() {
                return rsc == null ? false : rsc.isHardwareAccel();
        }

        /**
         * does nothing
         */
        @Override
        public void setHardwareAccel(boolean b) {
        }
        private R rsc = null;

        /**
         * must be set in order to do correct loading and use of the rendering
         * Thread
         */
        @Override
        public final void setRenderingScene(R renderingScene) {
                rsc = renderingScene;
        }

        @Override
        public final void setGroupMonitor(Monitor... mntr) {
                renderMonitor = mntr[0];
        }

        @Override
        public Monitor[] getGroupMonitor() {
                return new Monitor[]{renderMonitor};
        }

}
