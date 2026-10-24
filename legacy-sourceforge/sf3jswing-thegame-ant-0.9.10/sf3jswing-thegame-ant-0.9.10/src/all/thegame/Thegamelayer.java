/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package thegame;

import net.sf.jiga.xtended.impl.game.GameActionLayer;

/**
 *
 * @author www.b23prodtm.info
 */
public abstract class Thegamelayer extends GameActionLayer{

    /***/
    protected FieldGui gui;
    public Thegamelayer(FieldGui gui, String name) {
        super(name);
        this.gui = gui;
    }

    public boolean isLwjgl() {
        return gui.isLWJGLRendering();
    }


}
