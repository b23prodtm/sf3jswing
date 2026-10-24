/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.jiga.xtended.impl.system;

import net.sf.jiga.xtended.impl.game.gl.ModelGLHandler;
import net.sf.jiga.xtended.impl.game.gl.SpriteGLHandler;

/**
 * used by GL Object Handlers like {@link SpriteGLHandler} or {@link ModelGLHandler} to monitor their background loading process.
 * @author www.b23prodtm.info
 */
public interface LoadListener2 extends LoadListener {
    public void loadProgress(int val, int max);
    public void loadError(Object error);
    public Object getError();
    /** @return RenderingSceneComponent hash (Sprite, Model, Animation,etc.) it is refering to */
    public int hashLinkToGLObject();
}
