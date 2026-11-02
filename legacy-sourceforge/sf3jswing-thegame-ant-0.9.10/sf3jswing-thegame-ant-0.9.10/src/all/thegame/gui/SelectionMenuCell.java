/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thegame.gui;

import java.awt.*;
import javax.swing.JLabel;
import net.sf.jiga.xtended.impl.game.gl.RenderingSceneGL;
import net.sf.jiga.xtended.impl.game.gl.geom.GLGeom;
import thegame.FieldGui;

/**
 *
 * @author www.b23prodtm.info
 */
public class SelectionMenuCell extends JLabel {

    public Selectable_CELL selectable;
    Paint shadow_gradient = null;
    public final String name;
    FieldGui gui;

    public SelectionMenuCell(FieldGui gui, String name, Runnable doSelection, Runnable doEscape) {
        super(gui.rbHasKey(gui.rb, name) ? gui.rb.getString(name) : name);
        this.gui = gui;
        this.name = name;
        this.selectable = new Selectable_CELL(this);
        this.selectable.reset();
        this.doEscape = doEscape;
        this.doSelection = doSelection;
    }
    private Runnable doEscape;

    public void doEscape() {
        if (doEscape instanceof Runnable) {
            gui.getScene().doTask(doEscape);
        }
    }
    private Runnable doSelection;

    public void doSelection() {
        if (doSelection instanceof Runnable) {
            gui.getScene().doTask(doSelection);
        }
    }

    public int hashCode() {
        return name.hashCode();
    }

    public boolean equals(Object o) {
        if (o != null) {
            return o.hashCode() == hashCode();
        } else {
            return false;
        }
    }
    Rectangle gradientBounds = new Rectangle();

    private Paint newGradient(Color first, Color second) {
        gradientBounds = getBounds();
        return new GradientPaint(gradientBounds.x, gradientBounds.y, first, gradientBounds.x, gradientBounds.y + (float) gradientBounds.height * 0.66f, second, false);
    }
    
    @Override
    public void setForeground(Color c) {
        super.setForeground(c);
        shadow_gradient = newGradient(new Color(1f, 1f, 1f, 0f), getForeground());
    }
    boolean isSelected = false;
    boolean cellHasFocus = false;

    public boolean isSelected() {
        return isSelected;
    }

    public boolean isCellHasFocus() {
        return cellHasFocus;
    }

    static int font = 5;
    protected void GLpaintComponent(RenderingSceneGL gld, double z, int fx, Point fx_loc, Color fx_color) throws Exception {
        Rectangle cellBounds = getBounds();
        boolean i = gui.glow.glow > .5f;
        String label = (cellHasFocus ? (i ? "> " : " >") : "") + getText() + (cellHasFocus ? (i ? " <" : "< ") : "");
        if (isSelected) {
            GLGeom._GLfillRoundRect(gld, 0, true, cellBounds, z, GLGeom.getCWrgbaColors(Color.GREEN, .8f), fx, fx_loc, fx_color);
        } else {
            GLGeom._GLfillRoundRect(gld, 0, true, cellBounds, z, GLGeom.getCWrgbaColors(Color.YELLOW, .8f),fx, fx_loc, fx_color);
        }
        if (cellHasFocus) {
            GLGeom._GLdrawRoundRect(gld,hashCode(), true,  3, cellBounds, z, GLGeom.getCWrgbaColors(Color.GREEN, gui.glow.glow), fx, fx_loc, fx_color);
        }
        Dimension labelDim = gui._GL_baseCodeStringCLIPSIZE(gld, label, font);
        labelDim.width = Math.min(labelDim.width, cellBounds.width);
        labelDim.height = Math.min(labelDim.height, cellBounds.height);
        Dimension fontSize = new Dimension((int) ((float) labelDim.width / (float) label.length()), labelDim.height);
        gui._GL_baseCodeStringDRAW(gld,
                cellBounds.x + (int) ((float) (cellBounds.getWidth() - labelDim.width) / 2f),
                cellBounds.y + (int) ((float) (cellBounds.getHeight() - labelDim.height) / 2f),
                z + 1,
                fontSize,
                label, fx, fx_loc, GLGeom.getGlowingColor(Color.WHITE, gui.glow.glowCos));
    }

    /*protected void paintComponent(Graphics g) {
    Graphics2D g2 = Sprite.wrapRendering(g);
    Rectangle originalBounds = g2.getClipBounds();
    Rectangle cellBounds = getBounds();
    boolean i = gui.glowCos > .5f;
    String label = (cellHasFocus ? (i ? "> " : " >") : "") + getText() + (cellHasFocus ? (i ? " <" : "< ") : "");
    int font = 2;
    Dimension labelDim = gui._baseCodeStringCLIPSIZE(g2, label, font);
    labelDim.width = Math.min(labelDim.width, cellBounds.width);
    labelDim.height = Math.min(labelDim.height, cellBounds.height);
    Dimension fontSize = new Dimension((int) ((float) labelDim.width / (float) label.length()), labelDim.height);
    if (shadow_gradient == null || !gradientBounds.equals(cellBounds)) {
    shadow_gradient = newGradient(new Color(1f, 1f, 1f, 0f), Color.ORANGE);
    }
    g2.translate(-cellBounds.x, -cellBounds.y);
    g2.setClip(cellBounds);
    if (JXAenvUtils._debug) {
    System.err.println(getClass().getName() + " " + getText() + "-original clipbounds " + originalBounds);
    System.err.println(getClass().getName() + " " + getText() + "-clipbounds " + g2.getClipBounds());
    System.err.println(getClass().getName() + " " + getText() + "-bounds " + cellBounds);
    }
    Paint pnt = g2.getPaint();
    g2.setPaint(shadow_gradient);
    Color c = g2.getColor();
    Composite cps = g2.getComposite();
    int arc = 20;
    g2.fillRoundRect(cellBounds.x, cellBounds.y, (int) cellBounds.getWidth(), (int) cellBounds.getHeight(), arc, arc);
    g2.setPaint(pnt);
    if (cellHasFocus) {
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, gui.glow));
    g2.setColor(selectable.selectionColor.brighter());
    g2.fillRoundRect(cellBounds.x, cellBounds.y, (int) cellBounds.getWidth(), (int) cellBounds.getHeight(), arc, arc);
    g2.setColor(c);
    g2.setComposite(cps);
    }
    if (isSelected) {
    g2.setColor(selectable.selectionColor);
    g2.drawRoundRect(cellBounds.x, cellBounds.y, (int) cellBounds.getWidth(), (int) cellBounds.getHeight(), arc, arc);
    g2.setColor(c);
    } else {
    g2.setColor(selectable.selectionColor.darker());
    g2.drawRoundRect(cellBounds.x, cellBounds.y, (int) cellBounds.getWidth(), (int) cellBounds.getHeight(), arc, arc);
    g2.setColor(c);
    }

    try {
    gui._baseCodeStringDRAW(gui.getScene(), g2, cellBounds.x + (int) ((float) (cellBounds.getWidth() - labelDim.width) / 2f), cellBounds.y + (int) ((float) (cellBounds.getHeight() - labelDim.height) / 2f), fontSize, label, 0, new Point(0, 0), null);
    } catch (InterruptedException ex) {
    if (JXAenvUtils._debug) {
    ex.printStackTrace();
    }
    } finally {
    g2.setColor(c);
    g2.setComposite(cps);
    g2.setPaint(pnt);
    g2.setColor(selectable.selectionColor);
    BasicStroke stroke = new BasicStroke(3);
    Stroke str = g2.getStroke();
    g2.setStroke(stroke);
    Rectangle clip = g2.getClipBounds();
    clip.grow(-1, -1);
    g2.drawRoundRect(clip.x, clip.y, clip.width, clip.height, arc, arc);
    clip.grow(2, 2);
    g2.setClip(clip);
    g2.setColor(selectable.selectionColor.brighter());
    g2.drawRoundRect(clip.x, clip.y, clip.width, clip.height, arc, arc);
    g2.setStroke(str);
    g2.translate(cellBounds.x, cellBounds.y);
    if (originalBounds instanceof Rectangle2D) {
    g2.setClip(originalBounds);
    }
    }
    }*/
}
