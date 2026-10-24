/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thegame;

import net.sf.jiga.xtended.impl.system.LoadListener;
import net.sf.jiga.xtended.kernel.SpritesCacheAdapter;

/**
 *
 * @author www.b23prodtm.info
 */
public abstract class CacheLoadListener extends SpritesCacheAdapter implements LoadListener {

    int loaded = 1, cleared = 0, loading = 2;
    int load = cleared;

    public int getLoadState() {
        return load;
    }

    public void loadBegin() {
        load = loading;
    }

    public void loadIsComplete() {
        load = loaded;
    }

    public void unloadBegin() {
        load = loading;
    }

    public void unloadIsComplete() {
        load = cleared;
    }
}
