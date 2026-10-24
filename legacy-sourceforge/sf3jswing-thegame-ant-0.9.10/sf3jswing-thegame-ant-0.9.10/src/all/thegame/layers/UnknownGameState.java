/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thegame.layers;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.swing.Icon;
import net.sf.jiga.xtended.impl.Sprite;
import net.sf.jiga.xtended.impl.game.RenderingScene;
import net.sf.jiga.xtended.impl.game.gl.Sf3Texture;
import net.sf.jiga.xtended.impl.game.gl.SpriteGLHandler;
import net.sf.jiga.xtended.ui.UIMessage;
import thegame.FieldGui;
import thegame.Thegamelayer;

/**
 *
 * @author tianabruno
 */
public class UnknownGameState extends Thegamelayer {

    public UnknownGameState(FieldGui gui) {
        super(gui, "Restart Game");
    }
    Sprite s = new Sprite();
    Icon i = UIMessage._getIcon(UIMessage.QUESTION_TYPE, false);

    @Override
    protected void doLoad() {
        super.doLoad();
        s.setSize(i.getIconWidth(), i.getIconHeight());
        s.clearImageGraphics();
        Graphics2D g = (Graphics2D) s.getImage(gui.getScene()).getGraphics();
        i.paintIcon(gui.getScene(), g, 0, 0);
        g.dispose();
        s.setRenderingScene(gui.getScene());
    }
    SpriteGLHandler sgl;
    Sf3Texture stex;
    Rectangle r = new Rectangle();

    @Override
    protected void doLoadOnGL(RenderingScene context) {
        super.doLoadOnGL(context);
        sgl = RenderingScene._GLloadSprite(s, context);
    }

    @Override
    protected void doUnload() {
        super.doUnload();
        stex = null;
    }

    @Override
    protected void renderToGL(RenderingScene context) {
        super.renderToGL(context);
        if (stex == null) {
            stex = sgl.GLgetTexInfo(0);
        }
        r.setFrameFromCenter(context.getBounds().getCenterX() - context.getBounds().getX(), context.getBounds().getCenterY() - context.getBounds().getY(), .5 * (context.getBounds().getCenterX() + stex.getWidth()), .5 * (context.getBounds().getCenterY() + stex.getHeight()));
        Sprite._GLRenderSprite(context, sgl, r, gui.playerDepth);
    }
}
