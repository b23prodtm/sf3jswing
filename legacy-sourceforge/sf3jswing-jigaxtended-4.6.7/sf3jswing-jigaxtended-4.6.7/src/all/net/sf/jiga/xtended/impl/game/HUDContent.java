/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.game;

import net.sf.jiga.xtended.impl.game.gl.GLHandler;
import net.sf.jiga.xtended.kernel.BitStack;

/**
 *
 * @author www.b23prodtm.info7
 */
public class HUDContent {

    private final static BitStack _contentStatesBits = new BitStack();
    private final static int C_DIRTY = _contentStatesBits._newBitRange();
    /** by default state C_DRAW includes C_DIRTY*/
    private final static int C_DRAW = _contentStatesBits._newBit(C_DIRTY);
    /**
     * previous content associated (to keep backtrace of its layout); by default
     * equals to itself
     */
    public HUDContent prev;
    private String text;
    private TextLayout layout;
    private int state = C_DRAW;
    private boolean newLine;

    /**
     * content set is in c_draw (including c_dirty) state when it is first created and when it is drawn c_dirty is erased. 
     * c_dirty is added again whenever one of the member variables changes.
     */
    public HUDContent(String text, TextLayout layout, boolean newLine) {
        this(text, layout, newLine, new HUDContent(text, layout, newLine, null));
    }

    private HUDContent(String string, TextLayout layout, boolean newLine, HUDContent previous) {
        this.text = string;
        this.layout = layout;
        this.newLine = newLine;
        this.prev = previous == null ? this : previous;
    }

    public TextLayout getLayout() {
        return layout;
    }

    public String getText() {
        return text;
    }

    public boolean isNewLine() {
        return newLine;
    }

    public void setNewLine(boolean newLine) {
        if (this.newLine != newLine) {
            state |= C_DIRTY;
            this.prev.newLine = this.newLine;
        }
        this.newLine = newLine;
    }

    public void setLayout(TextLayout layout) {
        if (!this.layout.equals(layout)) {
            state |= C_DIRTY;
            this.prev.layout = this.layout;
        }
        this.layout = layout;
    }

    public void setText(String text) {
        if (!this.text.equals(text)) {
            state |= C_DIRTY;
            this.prev.text = this.text;
        }
        this.text = text == null ? "" : text;
    }

    /**
     * enable or disable draw
     */
    public void setDrawEnabled(boolean b) {
        this.prev.state = state;
        if (b) {
            state |= C_DRAW;
        } else {
            state &= ~C_DRAW;
        }
    }
    
    /*
     *
     */
    public boolean isDrawEnabled() {
        return (state & C_DRAW & ~C_DIRTY) != 0;
    }

    /**
     * call when content has just been drawn
     */
    public void resetDirtyState(boolean dirty) {
        this.prev.state = state;
        if (dirty) {
            state |= C_DIRTY;
        } else {
            state &= ~C_DIRTY;
        }
    }

    /**
     * repaint is needed if it is in dirty state and if draw was enabled or in any case if PBO is NOT available
     * (GLHandler.ext.PixelBufferObjects.GL_isAvailable())
     */
    public boolean repaintNeeded() {
        return (state & C_DRAW) == C_DRAW || !GLHandler.ext.PixelBufferObjects.GL_isAvailable();
    }
}
