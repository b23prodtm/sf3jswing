/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.game.gl;

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;
import net.sf.jiga.xtended.JXAException;
import net.sf.jiga.xtended.impl.Animation;
import net.sf.jiga.xtended.impl.Sprite;
import net.sf.jiga.xtended.impl.game.Model;
import net.sf.jiga.xtended.impl.game.RenderingSceneComponent;

/**
 *
 * @author www.b23prodtm.info
 */
public class GLObjectHandler<T extends LoadAdapter> {

    public GLObjectHandler() {
    }
    /**
     * Collection of openGL links (essentially texures bindings)
     */
    public final SortedMap<Integer, T> handlers = (SortedMap<Integer, T>) RenderingSceneGL._GLregMapOfLinks(Collections.synchronizedSortedMap(new TreeMap<Integer, T>()));

    /**
     * 
     * @param sp an instance of class<? extends LoadAdapter>, a hashCode or a RenderingSceneComponent (Xtended-Interactive-Model, Animation or Sprite)
     * @return
     */
    public T getHandler(RenderingSceneComponent sp) {
        return _getHandler(sp);
    }
    public T getHandler(int sp) {
        return _getHandler(sp);
    }
    private T _getHandler(Object sp) {        
        /** handler already present ?*/
        T a = (sp instanceof RenderingSceneComponent ?  handlers.get(sp.hashCode()) : handlers.get(sp));
        
        if (a == null) {
            if (sp instanceof Model) {
                a = (T) new ModelGLHandler((Model) sp);
            } else if (sp instanceof Animation) {
                a = (T) new AnimationGLHandler((Animation) sp);
            } else if (sp instanceof Sprite) {
                a = (T) new SpriteGLHandler((Sprite) sp);
            }
            if (a != null) {
                handlers.put(sp.hashCode(), a);
            }
        }
        if (a == null) {
            throw new JXAException("[" + sp + "]-Object must be loaded before to use it with a GLObjectHandler.");
        }
        return (T) a;
    }
    
    /**
     * 
     * @param sp an instance of class<? extends LoadAdapter>, a hashCode ({@link GLObject#getUID()}) or a RenderingSceneComponent (Xtended-Interactive-Model, Animation or Sprite)
     * @return
     */
    public boolean hasHandler(Object sp) {
        return (sp instanceof RenderingSceneComponent ? handlers.containsKey(sp.hashCode()) : handlers.containsKey(sp));
    }
}
