package net.sf.jiga.xtended.impl.game.gl;

import net.sf.jiga.xtended.impl.game.RenderingScene;
import net.sf.jiga.xtended.impl.game.RenderingSceneComponent;
import net.sf.jiga.xtended.impl.system.LoadListener2;
import net.sf.jiga.xtended.kernel.DebugMap;

/**
 * adpater for the {@link Loadlistener2} interface.
 */
public abstract class LoadAdapter implements LoadListener2, RenderingSceneComponent<RenderingSceneGL> {

    public int state = RenderingSceneGL._GLLOADSTATE_Cleared;
    long hash = System.nanoTime();

    @Override
    public int hashCode() {
        return (int) hash;
    }

    @Override
    public boolean equals(Object o) {
        return o == null ? false : o.hashCode() == hashCode();
    }

    @Override
    public void loadBegin() {
        if (scene != null) {
            scene.GLloaders.add(this);
        }
        state = RenderingSceneGL._GLLOADSTATE_Loading;
        if (DebugMap._getInstance().isDebugLevelEnabled(RenderingScene.DBUG_RENDER)) {
            System.out.println("vvv " + getClass().getSimpleName() + " " + hashLinkToGLObject() + " starts loading...");
        }
    }

    @Override
    public void loadIsComplete() {
        if (scene != null) {
            scene.GLloaders.remove(this);
        }
        state = RenderingSceneGL._GLLOADSTATE_Loaded;
        if (DebugMap._getInstance().isDebugLevelEnabled(RenderingScene.DBUG_RENDER)) {
            System.out.println("^^^ " + getClass().getSimpleName() + " loading finished.");
        }
    }
    Object error = null;

    @Override
    public void loadError(Object error) {
        state |= RenderingSceneGL._GLLOADSTATE_Errored;
        this.error = error;
    }

    @Override
    public Object getError() {
        return error;
    }

    @Override
    public void unloadBegin() {
        if (scene != null) {
            scene.GLloaders.add(this);
        }
        state = RenderingSceneGL._GLLOADSTATE_Loading;
        if (DebugMap._getInstance().isDebugLevelEnabled(RenderingSceneGL.DBUG_RENDER)) {
            System.out.println("vvv " + getClass().getSimpleName() + " " + hashLinkToGLObject() + " starts unloading...");
        }
    }

    @Override
    public void unloadIsComplete() {
        if (scene != null) {
            scene.GLloaders.remove(this);
        }
        state = RenderingSceneGL._GLLOADSTATE_Cleared;
        if (DebugMap._getInstance().isDebugLevelEnabled(RenderingSceneGL.DBUG_RENDER)) {
            System.out.println("^^^ " + getClass().getSimpleName() + " " + hashLinkToGLObject() + " unloading finished.");
        }
    }

    /**
     * @return Double-check the loadstate bit value : {@linkplain RenderingSceneGL#_GLLOADSTATE_Cleared},
     * {@linkplain RenderingSceneGL#_GLLOADSTATE_Loaded},
     * {@linkplain RenderingSceneGL#_GLLOADSTATE_Loading} bitwise AND-ed with
     * the actual RenderingScene-Texture load state, i.e. it will return a
     * "zero-null" value if an error occured (loadlistener and actual state are
     * not the same value)
     * @see GLHandler#_GLisTextureLoaded(int)
     */
    @Override
    public int getLoadState() {
        return state;
    }

    @Override
    public RenderingSceneGL getRenderingScene() {
        return scene;
    }
    RenderingSceneGL scene = null;

    @Override
    public void setRenderingScene(RenderingSceneGL renderingScene) {
        scene = renderingScene;
    }

    @Override
    public void setHardwareAccel(boolean b) {
    }

    @Override
    public boolean isHardwareAccel() {
        return true;
    }
    protected int max = 100, val = 0;

    /**
     * @param max total length of the loaded content
     * @param val current load progress
     */
    @Override
    public void loadProgress(int val, int max) {
        this.max = max;
        this.val = val;
        if (DebugMap._getInstance().isDebugLevelEnabled(RenderingSceneGL.DBUG_RENDER)) {
            System.out.println("||| " + getClass().getSimpleName() + " " + hashLinkToGLObject() + " is loading " + val + "/" + max + "...");
        }
    }

    /**
     * @default 100
     * @see #loadProgress(int,int)
     */
    public int getMax() {
        return max;
    }

    /**
     * @default 0
     * @see #loadProgress(int,int)
     */
    public int getVal() {
        return val;
    }
}
