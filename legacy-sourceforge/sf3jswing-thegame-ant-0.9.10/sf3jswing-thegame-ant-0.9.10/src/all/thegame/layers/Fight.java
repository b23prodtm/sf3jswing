/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thegame.layers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import net.sf.jiga.xtended.impl.Sprite;
import net.sf.jiga.xtended.impl.game.RenderingScene;
import net.sf.jiga.xtended.impl.game.gl.GLFX;
import net.sf.jiga.xtended.impl.game.gl.GLHandler;
import net.sf.jiga.xtended.impl.game.gl.SpriteGLHandler;
import net.sf.jiga.xtended.impl.game.gl.geom.GLGeom;
import net.sf.jiga.xtended.impl.system.Chrono;
import net.sf.jiga.xtended.ui.JProgressMemory;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.Util;
import thegame.comp.Battle;
import thegame.FieldGui;
import thegame.RandomComparator;
import thegame.Thegamelayer;

/**
 *
 * @author tianabruno
 */
public class Fight extends Thegamelayer {

    public Fight(FieldGui gui) {
        super(gui, "Fight");
        /**
         * chrono is resized later
         */
        this.chrono = new Chrono();
        life = new JProgressMemory[FieldGui.NPLAYERS];
        bgSndStg = RandomComparator._getRandomInt(gui.getBgSndStg().first(), gui.getBgSndStg().last());
    }
    private static Rectangle gaugeBounds = new Rectangle(0, 0, 200, 30);
    private Chrono chrono;
    private boolean roundStart = true;
    private int round = 1;
    public Battle battle = new Battle();
    long roundTime;
    long roundStartFXStart = 0;
    JProgressMemory life[];
    int bgSndStg;

    public Chrono getChrono() {
        return chrono;
    }

    @Override
    protected void doLoad() {
        super.doLoad();
        battle.reset();
        for (int i = 0; i < life.length; i++) {
            if (life[i] == null) {
                life[i] = new JProgressMemory(0, 100);
                life[i].setValue(100);
            }
            life[i].setLabels("", "", "");
        }
        bgSndStg = RandomComparator._getRandomInt(gui.getBgSndStg().first(), gui.getBgSndStg().last());
        gui.setBgSnd(gui._soundInterface.get("SFX_SNDTRK_stage_" + bgSndStg), true);
    }

    @Override
    protected void doPhysics() {
        super.doPhysics();
        if (battle.getState() == battle.STATE_START) {
            battle.newInput(battle.INPUT_START);
        }
    }

    @Override
    protected void doLoadOnGL(RenderingScene context) {
        super.doLoadOnGL(context);
        for (int i = 0; i < life.length; i++) {
            String leftRightBar = i >= (float) gui.NPLAYERS / 2f ? "_RIGHT" : "_LEFT";
            context._GLloadSprite((Sprite) gui.getCacheEntry("GFX_GAUGE_SPRITE" + leftRightBar), context);
            context._GLloadSprite((Sprite) gui.getCacheEntry("GFX_GAUGE_GLOW" + leftRightBar), context);
            Sprite sp = gui.getModelIconSprite(gui.getSelectedPlayer()[i]);
            playerIconID[i] = context._GLloadSprite(sp, context);
            playerIconDim = sp.getSize();
        }
    }

    @Override
    protected void doUnloadFromGL(RenderingScene context) {
        super.doUnloadFromGL(context);
        for (int i = 0; i < life.length; i++) {
            String leftRightBar = i >= (float) gui.NPLAYERS / 2f ? "_RIGHT" : "_LEFT";
            context._GLunloadSprite(Sprite._GLHandlers.getHandler(gui.getCacheHashEntry("GFX_GAUGE_SPRITE" + leftRightBar)));
            context._GLunloadSprite(Sprite._GLHandlers.getHandler(gui.getCacheHashEntry("GFX_GAUGE_MASK" + leftRightBar)));
            context._GLunloadSprite(playerIconID[i]);
        }
    }
    private SpriteGLHandler[] playerIconID = new SpriteGLHandler[gui.NPLAYERS];
    private Dimension playerIconDim = new Dimension();

    public void _barRender(RenderingScene context, int[][][] clipShape, Rectangle lifeBarMask, SpriteGLHandler lifeBarGlow, int i) {
        /**
         * GLdraw bar stencil
         */
        GLFX.beginNoFXBlock();
        GL11.glPushAttrib(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_ENABLE_BIT | GL11.GL_STENCIL_BUFFER_BIT);
        GL11.glColorMask(context.isDebugRenderEnabled(), context.isDebugRenderEnabled(), context.isDebugRenderEnabled(), context.isDebugRenderEnabled());
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glStencilMask(3);
        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 1);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GLHandler.stView.push();
        GL11.glTranslated(gaugeBounds.getX(), gaugeBounds.getY(), 0);
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        GL11.glBegin(GL11.GL_POLYGON);
        for (int coord = 0; coord < clipShape[i][0].length; coord++) {
            GL11.glVertex3i(clipShape[i][0][coord], clipShape[i][1][coord], - 1);
        }
        GL11.glEnd();
        GLHandler.stView.pop();
        GL11.glStencilFunc(GL11.GL_EQUAL, 3, 1);
        GLGeom._GLfillPolygon(context, false, lifeBarMask, GLGeom.getCWrgbaColors(Color.YELLOW, 1f), - 1);
        GL11.glColorMask(true, true, true, true);
        GL11.glStencilFunc(GL11.GL_EQUAL, 3, 3);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        /**
         * GLdraw bar and clear stencil
         */
        Sprite._GLRenderSprite(context, lifeBarGlow, gaugeBounds, gui.playerDepth);
        GL11.glPopAttrib();
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        GLFX.endNoFXBlock();
        Util.checkGLError();
    }

    @Override
    protected void renderToGL(RenderingScene context) {
        super.renderToGL(context);
        int arg0 = battle.getState();
        try {
            if (arg0 == Battle.STATE_ROUND_1 || arg0 == Battle.STATE_ROUND_2 || arg0 == Battle.STATE_ROUND_3 || arg0 == Battle.STATE_ROUND_FINAL) {
                Dimension chronoSize = gui._GL_baseCodeStringCLIPSIZE(context, "00", 4);
                /**
                 * *gauges
                 */
                int gaugePadding = chronoSize.width;
                Dimension gaugeSize = new Dimension((int) ((float) context.getWidth() / 2.5f), (int) ((float) context.getHeight() / (9f)));
                Point p = new Point((int) (((float) context.getWidth() - (float) gaugePadding) / 2f) - gaugeSize.width, (int) ((float) chronoSize.height / 2f));
                gaugeBounds.setLocation(p);
                gaugeBounds.setSize(gaugeSize);
                int[][][] clipShape = new int[gui.NPLAYERS][2][5];
                clipShape[0] = new int[][]{{(int) (1f / 34f * (float) gaugeSize.width),
                        (int) ((32f + 1f) / 34f * (float) gaugeSize.width),
                        (int) ((32f + 1f) / 34f * (float) gaugeSize.width),
                        (int) ((7f + 1f) / 34f * (float) gaugeSize.width),
                        (int) (1f / 34f * (float) gaugeSize.width)
                    },
                    {(int) (1f / 5f * (float) gaugeSize.height),
                        (int) (1f / 5f * (float) gaugeSize.height),
                        (int) ((2f + 1f) / 5f * (float) gaugeSize.height),
                        (int) ((3f + 1f) / 5f * (float) gaugeSize.height),
                        (int) ((3f + 1f) / 5f * (float) gaugeSize.height),}
                };
                clipShape[1] = new int[][]{{(int) (1f / 34f * (float) gaugeSize.width),
                        (int) ((32f + 1f) / 34f * (float) gaugeSize.width),
                        (int) ((32f + 1f) / 34f * (float) gaugeSize.width),
                        (int) ((24f + 1f) / 34f * (float) gaugeSize.width),
                        (int) (1f / 34f * (float) gaugeSize.width)
                    },
                    {(int) (1f / 5f * (float) gaugeSize.height),
                        (int) (1f / 5f * (float) gaugeSize.height),
                        (int) ((3f + 1f) / 5f * (float) gaugeSize.height),
                        (int) ((3f + 1f) / 5f * (float) gaugeSize.height),
                        (int) ((2f + 1f) / 5f * (float) gaugeSize.height),}
                };
                for (int i = 0; i < life.length; i++) {
                    String leftRightBar = i >= (float) gui.NPLAYERS / 2f ? "_RIGHT" : "_LEFT";
                    SpriteGLHandler lifeBar = Sprite._GLHandlers.getHandler(gui.getCacheHashEntry("GFX_GAUGE_SPRITE" + leftRightBar));
                    SpriteGLHandler lifeBarGlow = Sprite._GLHandlers.getHandler(gui.getCacheHashEntry("GFX_GAUGE_GLOW" + leftRightBar));
                    Rectangle lifeBarMask = gaugeBounds.getBounds();
                    lifeBarMask.setSize(gaugeBounds.width - 20, gaugeBounds.height);
                    lifeBarMask.setLocation((int) gaugeBounds.getX() + 10, (int) gaugeBounds.getY());
                    /**
                     * life bar
                     */
                    float lifeWidth = (float) lifeBarMask.getWidth() * (float) life[i].getValue() / (float) life[i].getMaximum();
                    if (leftRightBar.equals("_RIGHT")) {
                        lifeBarMask.setLocation(lifeBarMask.getLocation().x + (int) (lifeBarMask.width - lifeWidth), lifeBarMask.getLocation().y);
                    }
                    lifeBarMask.setSize((int) (lifeWidth), lifeBarMask.height);
                    try {
                        /**
                         * GLdraw bar stencil
                         */
                        GLFX._GLpushRender("_barRender", this, new Object[]{context, clipShape, lifeBarMask, lifeBarGlow, i}, new Class[]{RenderingScene.class, int[][][].class, Rectangle.class, SpriteGLHandler.class, int.class});
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        /**
                         * render bar artworks
                         */
                        Sprite._GLRenderSprite(context, lifeBar, gaugeBounds, gui.playerDepth);
                        Rectangle playerIcon = new Rectangle(playerIconDim);
                        playerIcon.setLocation((int) gaugeBounds.getX(), (int) gaugeBounds.getMaxY());
                        Sprite._GLRenderSprite(context, playerIconID[i], playerIcon, gui.playerDepth);
                        p.x = p.x + gaugeSize.width + gaugePadding;
                        gaugeBounds.setLocation(p);
                    }
                }
                /**
                 * chrono
                 */
                roundTime = gui.getProps().roundTime;
                long chronoTime = chrono.getCurrentChronoTime();
                String chronoStr = "" + ((int) Math.max(0, (float) (roundTime - chronoTime) / 1000f));
                gui._GL_baseCodeStringDRAW(context, (int) ((float) (context.getWidth() - chronoSize.width) / 2f), (int) gaugeBounds.getMaxY() - chronoSize.height, - 1, new Dimension((int) ((float) chronoSize.width / 2f), chronoSize.height), chronoStr);
                /**
                 * stage
                 */
                final float roundFXTime = 4500f;
                long now = System.currentTimeMillis();
                if (roundStart) {
                    if (roundStartFXStart == 0) {
                        roundStartFXStart = now;
                    }
                    String roundStr = "round " + round;
                    int roundFont = 4;
                    Dimension roundStrSize = gui._GL_baseCodeStringCLIPSIZE(context, roundStr, roundFont);
                    if (now - roundStartFXStart <= roundFXTime) {
                        double zoomFX = (double) (2f - (float) (now - roundStartFXStart) / roundFXTime);
                        AffineTransform fx_tx = AffineTransform.getScaleInstance(zoomFX, zoomFX);
                        Dimension dim = fx_tx.createTransformedShape(new Rectangle(gui._GL_baseCodeStringFONT(context, roundFont))).getBounds().getSize();
                        gui._GL_baseCodeStringDRAW(context, (int) ((float) (context.getWidth() - roundStrSize.width * zoomFX) / 2f), (int) ((float) (context.getHeight() - roundStrSize.height * zoomFX) / 2f), - 1, dim, roundStr);
                    } else if (now - roundStartFXStart <= roundFXTime + 2500f) {
                        gui._GL_baseCodeStringDRAW(context, (int) ((float) (context.getWidth() - roundStrSize.width) / 2f), (int) ((float) (context.getHeight() - roundStrSize.height) / 2f), - 1, roundFont, roundStr);
                    } else {
                        roundStart = false;
                        roundStartFXStart = 0;
                        chrono.start();
                    }
                }
            }
            if (arg0 == Battle.STATE_PAUSE) {
            }
            if (arg0 == Battle.STATE_START) {
            }
            if (arg0 == Battle.STATE_FINISH_1X) {
            }
            if (arg0 == Battle.STATE_FINISH_2X) {
            }
            if (arg0 == Battle.STATE_FINISH_DRAW) {
            }
            if (arg0 == Battle.STATE_EXIT) {
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
