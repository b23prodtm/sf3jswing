/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.game;

import java.awt.Point;
import java.awt.Rectangle;
import net.sf.jiga.xtended.impl.GUIConsoleOutput;

/**
 *
 * @author www.b23prodtm.info7
 */
public class TextLayout {

    /**
     * {@link GUIConsoleOutput} screen positions
     */
    public enum POS {

        TOP(), BOTTOM(),
        LEFT(), RIGHT(), CENTER(), UNKNOWN;
    }
    /**
     * bottom-left location for the next character/text output (substract
     * fontmetrics descent to draw characters)
     */
    public Point nextOutputPX;
    public POS valign;
    public POS align;
    public int interline, margin;
    public Rectangle bounds;

    public TextLayout(Point nextOutputPX, POS align, POS valign, int rowPadding, int insets, Rectangle bounds) {
        this.nextOutputPX = nextOutputPX;
        this.align = align;
        this.valign = valign;
        this.interline = rowPadding;
        this.margin = insets;
        this.bounds = bounds;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TextLayout) {
            TextLayout tobj = (TextLayout) obj;
            return tobj.align == align && tobj.bounds.equals(bounds) && tobj.margin == margin && tobj.nextOutputPX.equals(nextOutputPX) && tobj.interline == interline && tobj.valign == valign;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        String s = " ";
        switch (align) {
            case CENTER:
                s += "h:center";
                break;
            case LEFT:
                s += "h:left";
                break;
            case RIGHT:
                s += "h:right";
                break;
            default:
                s += "h:unknown";
                break;
        }
        s += " ";
        switch (valign) {
            case CENTER:
                s += "v:center";
                break;
            case TOP:
                s += "v:top";
                break;
            case BOTTOM:
                s += "v:bottom";
                break;
            default:
                s += "v:unknown";
                break;
        }
        return TextLayout.class.getSimpleName() + " " + s + " margin " + margin + "px - interline " + interline + "px - nextOutput " + nextOutputPX + " - bounds " + bounds;
    }
}
