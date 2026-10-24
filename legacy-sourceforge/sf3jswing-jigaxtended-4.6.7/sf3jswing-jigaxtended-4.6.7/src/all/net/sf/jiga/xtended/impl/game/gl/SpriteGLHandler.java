/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.game.gl;

import net.sf.jiga.xtended.impl.Sprite;

/**
 * Defines a toolkit to handle Sprite's in a GL context.
 * @author www.b23prodtm.info
 */
public class SpriteGLHandler extends LoadAdapter {
    
    /** link to all of the textures scratch 
     */
    public final static TexScratch sTex = GLHandler.sTex;

    int spHash;
    float rCoord = 0f;

    /**
     * 
     * @param sp
     */
    protected SpriteGLHandler(Sprite sp) {
        super();
        assert sp.isResourceLoaded() : "sprite " + sp + " must be loaded before to use it in GL mode";
        assert sp.isTextureModeEnabled() : "sprite " + sp + " must be enabled for the texture mode";
        assert !Sprite._GLHandlers.hasHandler(sp) : "sprite " + sp + " already has a GL handler";
        spHash = sp.hashCode();
    }

    /**
     * 
     * @return
     */
    public int getSpHash() {
        return spHash;
    }

    /**
     * 
     * @return
     */
    public int getTexHash() {
        return Sprite._GLgetTexHash(spHash);
    }

    /**
     * 
     * @return
     */
    public float getrCoord() {
        return rCoord;
    }

    /**
     * 
     * @param rCoord
     */
    public void setrCoord(float rCoord) {
        this.rCoord = rCoord;
    }

    @Override
    public int hashLinkToGLObject() {
        return spHash;
    }
    
    /**
     * @param texItemIndex default value is 0 which always returns the associated texture specs. other indices are reserved for tiled or Animation textures.
     * @return creates a new Sf3Texture instance that contains <b>all specifications of the current loaded GL texture for this SpriteGLHandler; <i>that instance has no pixel data</i></b>.
     */
    public Sf3Texture GLgetTexInfo(int texItemIndex) {
        return new Sf3Texture(getTexHash(), GLHandler.sTex.getTextureMap(getTexHash()).get(texItemIndex).data);
    }
}
