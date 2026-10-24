/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.jiga.xtended.impl.game.gl;

/**
 *
 * @author www.b23prodtm.info
 */
public interface GL2DFXObject extends GL2DObject {
    /** original source GL2DObject that is transformed by the fx */
    public GL2DObject getSrc();
    /** fx bitmap */
    public int getFX();
}
