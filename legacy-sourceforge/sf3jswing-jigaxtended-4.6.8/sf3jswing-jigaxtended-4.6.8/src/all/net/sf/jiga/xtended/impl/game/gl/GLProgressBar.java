/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.game.gl;

import net.sf.jiga.xtended.impl.game.gl.geom.GLGeom;
import java.awt.Color;
import java.awt.Rectangle;
import java.nio.FloatBuffer;
import net.sf.jiga.xtended.impl.Glow;

/**
 * An openGL ProgressBar (i.e. that can be rendered in an (JigaXtended)-OpenGL context).
 * @author www.b23prodtm.info
 */
public class GLProgressBar extends LoadAdapter {

    private FloatBuffer CWrgbaColors;
    static Glow glow = new Glow();

    public GLProgressBar() {
        super();
        CWrgbaColors = GLGeom.getCWrgbaColors(Color.GREEN, 1f);
    }

    public FloatBuffer getCWrgbaColors() {
        return CWrgbaColors;
    }

    public void setCWrgbaColors(FloatBuffer CWrgbaColors) {
        this.CWrgbaColors = CWrgbaColors;
    }
    private Color outline = Color.WHITE;

    public void setOutline(Color outline) {
        this.outline = outline;
    }

    public Color getOutline() {
        return outline;
    }

    /**
     * 
     * @param gld
     * @param bounds
     * @param z
     * @param outlineWidth must be greater than zero
     */
    public void render(RenderingSceneGL gld, Rectangle bounds, double z, float outlineWidth) {
        GLFX.beginNoFXBlock();
        glow.glow();
        /**bar screen*/
        GLGeom._GLfillRoundRect(gld, 0, true, bounds, z, GLGeom.getCWrgbaColors(CWrgbaColors, .2f));
        /**bar fill with glow*/
        /* double layPad = .5;*/
        Rectangle valBounds = bounds.getBounds();/*_getLayPaddedDim(bounds, z, -layPad);*/
        valBounds.width = Math.round(((float) val / (float) max) * (float) valBounds.width);
        /*value bar (variable size)*/
        GLGeom._GLfillRoundRect(gld, hashCode(), true, valBounds, z /*+ layPad*/, GLGeom.getCWrgbaColors(CWrgbaColors, glow.glow));
        /**bar draw glass-style*/
        /*layPad += .5;*/
        Rectangle effectBounds = bounds.getBounds();/*_getLayPaddedDim(bounds, z, -layPad);*/
        effectBounds.height = Math.round(effectBounds.height / 3f);
        GLGeom._GLfillRoundRect(gld, 0, true, effectBounds, z/* + layPad*/, GLGeom.getCWrgbaColors(GLGeom.getGlowingColor(outline, .8f), GLGeom.getGlowingColor(outline, .8f), GLGeom.getGlowingColor(outline, 0f), GLGeom.getGlowingColor(outline, 0f)));
        effectBounds.y += 2 * effectBounds.height;
        GLGeom._GLfillRoundRect(gld, 0, true, effectBounds, z/* + layPad*/, GLGeom.getCWrgbaColors(GLGeom.getGlowingColor(outline, 0f), GLGeom.getGlowingColor(outline, 0f), GLGeom.getGlowingColor(outline, .5f), GLGeom.getGlowingColor(outline, .5f)));
        /** outline with a thin line */
        /*layPad += .5;*/
        Rectangle outlineBounds = bounds.getBounds();/*_getLayPaddedDim(bounds, z, -layPad);*/
        GLGeom._GLdrawRoundRect(gld, 0, true, outlineWidth, outlineBounds, z/* + layPad*/, GLGeom.getCWrgbaColors(outline, 1f));
        GLFX.endNoFXBlock();
    }

    @Override
    public int hashLinkToGLObject() {
        return hashCode();
    }
}
