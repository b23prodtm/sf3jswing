/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.game.gl;

import java.awt.Shape;

/**
 *
 * @author www.b23prodtm.info
 */
public interface GL2DObject extends GLObject {

    /**
     * 2D Shape instance of the object
     */
    public Shape getShape();

    /**
     * fill with color or not
     */
    public boolean isFill();

    /**
     * resolution to use for non-rectangular Shapes (between 5 and reasonable
     * positive value) using Bezier Curves interpolation
     * <br>usually equals to the number of points that are to be rendered
     */
    public int getResolution();
}
