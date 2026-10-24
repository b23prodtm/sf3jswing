/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.game.test;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;
import java.util.Vector;
import net.sf.jiga.xtended.impl.game.InteractiveModel;
import net.sf.jiga.xtended.impl.game.Model;
import net.sf.jiga.xtended.impl.game.SpritesChar;
import net.sf.jiga.xtended.impl.game.XtendedModel;
import net.sf.jiga.xtended.impl.system.input.KeyEventWrapper;

/**
 * The returned InteractiveModel will map keyboard arrows to sample animation-sprite.
 * @author www.b23prodtm.info
 */
public class TestModel {

    Model imodel, imodel2;

    public void loadDefaults(boolean GL) {
        this.imodel = loadTestKeyboardModel(GL ? _loadTestKeyboardModel_x() : _loadTestKeyboardModel_i());
    }

    private InteractiveModel _loadTestKeyboardModel_i() {
        InteractiveModel imodel = new InteractiveModel("/net/sf/jiga/xtended/impl/arrows", SpritesChar.HIGH, 10, new Dimension(100, 100), new Dimension(80, 80), new int[][]{
                    {0, 0}, {1, 1}, {2, 2}, {3, 3}, {4, 4}, {5, 5}, {6, 6}, {7, 7}, {8, 8}
                }, new String[]{}, new String[]{});
        return imodel;
    }

    private XtendedModel _loadTestKeyboardModel_x() {
        XtendedModel imodel = new XtendedModel("/net/sf/jiga/xtended/impl/arrows", SpritesChar.HIGH, 10, new Dimension(100, 100), new Dimension(80, 80), new int[][]{
                    {0, 0}, {1, 1}, {2, 2}, {3, 3}, {4, 4}, {5, 5}, {6, 6}, {7, 7}, {8, 8}
                }, new String[]{}, new String[]{});
        return imodel;
    }

    private InteractiveModel loadTestKeyboardModel(InteractiveModel imodel) {
        imodel.setInnerResourceModeEnabled(true);
        KeyEventWrapper up = new KeyEventWrapper(0, KeyEvent.VK_UP, KeyEvent.KEY_PRESSED), down = new KeyEventWrapper(0, KeyEvent.VK_DOWN, KeyEvent.KEY_PRESSED),
                right = new KeyEventWrapper(0, KeyEvent.VK_RIGHT, KeyEvent.KEY_PRESSED), left = new KeyEventWrapper(0, KeyEvent.VK_LEFT, KeyEvent.KEY_PRESSED);
        imodel.animsID_default = "arrow";
        imodel.addAnimMap("arrow", 8);
        imodel.addAnimMap("arrow down", 0);
        imodel.addAnimMap("arrow down left", 1);
        imodel.addAnimMap("arrow left", 2);
        imodel.addAnimMap("arrow up left", 3);
        imodel.addAnimMap("arrow up", 4);
        imodel.addAnimMap("arrow up right", 5);
        imodel.addAnimMap("arrow right", 6);
        imodel.addAnimMap("arrow down right", 7);
        imodel.addKeyEventWrapperMapping(down, "arrow down");
        Vector<KeyEventWrapper> sequence = new Vector<KeyEventWrapper>();
        sequence.add(down);
        sequence.add(left);
        imodel.addKeyEventWrapperMapping(new KeyEventWrapper(sequence), "arrow down left", true);
        imodel.addKeyEventWrapperMapping(left, "arrow left");
        sequence = new Vector<KeyEventWrapper>();
        sequence.add(up);
        sequence.add(left);
        imodel.addKeyEventWrapperMapping(new KeyEventWrapper(sequence), "arrow up left", true);
        imodel.addKeyEventWrapperMapping(up, "arrow up");
        sequence = new Vector<KeyEventWrapper>();
        sequence.add(up);
        sequence.add(right);
        imodel.addKeyEventWrapperMapping(new KeyEventWrapper(sequence), "arrow up right", true);
        imodel.addKeyEventWrapperMapping(right, "arrow right");
        sequence = new Vector<KeyEventWrapper>();
        sequence.add(down);
        sequence.add(right);
        imodel.addKeyEventWrapperMapping(new KeyEventWrapper(sequence), "arrow down right", true);
        return imodel;
    }

    public Model getImodel() {
        return imodel2 != null ? imodel2 : imodel;
    }


    public boolean open(Object data) {
        List<File> files = null;
        if (data instanceof List) {
            files = ((List<File>) data);
        }
        if (files instanceof List) {
            for (File f : files) {
                final Model m = Model._importModel(f.getAbsolutePath());
                if (m instanceof Model) {
                    imodel2 = m;
                    break;
                } else {
                    continue;
                }
            }
            return true;
        }
        return false;
    }
}
