/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.game.gl;

import net.sf.jiga.xtended.impl.game.HUD;

/**
 * StencilPBO sends events to notify when the application needs to refresh the contents if anything was stored in the Buffer Object (texture).
 * @see HUD
 * @author www.b23prodtm.info
 */
public interface StencilPBOListener {

    public void contentsLost();
}
