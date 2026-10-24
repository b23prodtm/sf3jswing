/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thegame.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import net.sf.jiga.xtended.impl.Sprite;
import net.sf.jiga.xtended.impl.game.RenderingScene;
import net.sf.jiga.xtended.impl.game.gl.GLText;
import net.sf.jiga.xtended.impl.game.gl.SpriteGLHandler;
import net.sf.jiga.xtended.impl.game.gl.geom.GLGeom;
import thegame.FieldGui;

/**
 *
 * @author www.b23prodtm.info
 */
public class SelectionMenuIconCell extends SelectionMenuCell {

    Sprite icon;

    public SelectionMenuIconCell(FieldGui gui, String label, Sprite icon, Runnable doSelection, Runnable doEscape) {
        super(gui, label, doSelection, doEscape);
        this.icon = icon;
    }
    SpriteGLHandler glSprite = null;

    @Override
    protected void GLpaintComponent(RenderingScene gld, double z, int fx, Point fx_loc, Color fx_color) {
        Dimension iconSize = gui.ICONSIZE;
        Rectangle bounds = getBounds();
        if (glSprite == null) {
            Sprite icon = this.icon instanceof Sprite ? this.icon : RenderingScene._getLogo(gui.getScene(), gui.rb.getString("ICON"), iconSize);
            glSprite = RenderingScene._GLloadSprite(icon, gui.getScene());
        }
            Sprite._GLRenderSprite(gld, glSprite, bounds, z, fx, fx_loc, fx_color, 0, null, null,null);
            Color c = (isSelected() ? selectable.selectionColor.brighter() : isCellHasFocus() ? selectable.selectionColor : selectable.selectionColor.darker());
            GLGeom._GLdrawRoundRect(gld, hashCode(), true, 3f, bounds, z, GLGeom.getCWrgbaColors(c, gui.glow.glow), fx, fx_loc, fx_color);
            Dimension d = RenderingScene._GLtoGraphicalMetrics(getText(), gld);
            GLText._GLRenderText2D(gld, getText(), (float) bounds.x + (float) (bounds.getWidth() - d.width) / 2f, (float) bounds.getMaxY() + 4f, (float) z, fx_color, fx, fx_loc);
    }

    /** the characters selection panel *
    protected void paintComponent(Graphics pg) {
        Graphics2D g = Sprite.wrapRendering(pg);
        Dimension iconSize = gui.ICONSIZE;
        Rectangle bounds = getBounds();
        g.translate(-bounds.x, -bounds.y);
        Sprite icon = this.icon instanceof Sprite ? this.icon : RenderingScene._getLogo(gui.getScene(), gui.rb.getString("ICON"), iconSize);
        try {
            icon.setBounds(bounds);
            icon.runValidate();
            icon.draw(gui.getScene(), g);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } finally {
            Stroke stroke = g.getStroke();
            BasicStroke bs = new BasicStroke(4f);
            g.setStroke(bs);
            Color c = g.getColor();
            g.setColor(isSelected() ? selectable.selectionColor.brighter() : isCellHasFocus() ? selectable.selectionColor : selectable.selectionColor.darker());
            Composite cps = g.getComposite();
            if (isCellHasFocus()) {
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, gui.glow));
            }
            g.drawRoundRect(bounds.x, bounds.y, (int) bounds.getWidth(), (int) bounds.getHeight(), 5, 5);
            g.setColor(c);
            g.setComposite(cps);
            g.setStroke(stroke);
            Dimension d = RenderingScene._toGraphicalMetrics(getText(), g);
            g.drawString(getText(), (float) bounds.x + (float) (bounds.getWidth() - d.width) / 2f, (float) bounds.getMaxY() + 4f);
            g.translate(bounds.x, bounds.y);
        }
    }*/
}

