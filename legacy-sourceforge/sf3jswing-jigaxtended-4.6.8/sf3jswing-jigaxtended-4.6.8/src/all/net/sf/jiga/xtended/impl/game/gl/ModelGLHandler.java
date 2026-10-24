/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.game.gl;

import java.util.SortedMap;
import net.sf.jiga.xtended.impl.game.Model;

/**
 *
 * @author www.b23prodtm.info
 */
public class ModelGLHandler extends LoadAdapter {

    private int modelhash;
    private SortedMap<Integer, Integer> GLcache;

    protected ModelGLHandler(Model model) {
        modelhash = model.hashCode();
        assert model.isResourceLoaded() : "model " + model + " must be loaded before to use it in GL mode";
        assert model.isTextureModeEnabled() : "model " + model + " must be enabled for the texture mode";
        assert !Model._GLHandlers.hasHandler(model) : "model " + model + " already has a GL handler";
        GLcache = model.getGLcache();
        loadProgress(0, GLcache.size() - 1);
    }

    public SortedMap<Integer, Integer> getGLcache() {
        return GLcache;
    }

    public int getModelHash() {
        return modelhash;
    }

    @Override
    public int hashLinkToGLObject() {
        return modelhash;
    }
}
