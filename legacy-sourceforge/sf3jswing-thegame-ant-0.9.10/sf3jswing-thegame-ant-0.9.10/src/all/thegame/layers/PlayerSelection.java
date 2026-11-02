/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thegame.layers;

import thegame.gui.SelectionMenuComponent;
import thegame.gui.SelectionMenuIconCell;
import thegame.gui.SelectionMenuComponent_TABLE;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.sf.jiga.xtended.impl.Animation;
import net.sf.jiga.xtended.impl.Sprite;
import net.sf.jiga.xtended.impl.game.gl.RenderingSceneGL;
import net.sf.jiga.xtended.impl.game.TextLayout;
import net.sf.jiga.xtended.impl.game.XtendedModel;
import net.sf.jiga.xtended.impl.game.gl.AnimationGLHandler;
import net.sf.jiga.xtended.impl.game.gl.GLFX;
import net.sf.jiga.xtended.impl.game.gl.GLHandler;
import net.sf.jiga.xtended.impl.game.gl.SpriteGLHandler;
import net.sf.jiga.xtended.impl.game.gl.geom.GLGeom;
import thegame.*;

/**
 *
 * @author tianabruno
 */
public class PlayerSelection extends Thegamelayer {


    public PlayerSelection(FieldGui gui) {
        super(gui, "Player Selection");
    }
    private List<SelectionMenuIconCell> character_selection_menu = new ArrayList<SelectionMenuIconCell>();
    private SelectionMenuComponent player_selection_menu = new SelectionMenuComponent(gui, null, character_selection_menu, SelectionMenuComponent.HORIZONTAL_LAYOUT | SelectionMenuComponent.VERTICAL_LAYOUT);

    @Override
    protected void doLoad() {
        super.doLoad();
        gui.setBgSnd(gui._soundInterface.get("SFX_SNDTRK_menu"), true);
        if (character_selection_menu.size() != gui.getAvailablePlayerModels().size()) {
            character_selection_menu = new ArrayList<SelectionMenuIconCell>(gui.getAvailablePlayerModels().size());
            synchronized (gui.getAvailablePlayerModels()) {
                for (Iterator<XtendedModel> it = gui.getAvailablePlayerModels().iterator(); it.hasNext();) {
                    XtendedModel model = it.next();
                    if (model instanceof XtendedModel) {
                        Sprite spIcon = gui.getModelIconSprite(model);
                        spIcon.setSize(gui.ICONSIZE);
                        character_selection_menu.add(new SelectionMenuIconCell(gui, "", spIcon, new Runnable() {

                            public void run() {
                                int uiplr = player_selection_menu.selectable.getUISelectingPlayer();
                                int UISelectedPlayer = player_selection_menu.getFocusedMenu();
                                if (uiplr < gui.NPLAYERS) {
                                    boolean validSelection = true;
                                    for (int i = 0; i < gui.NPLAYERS; i++) {
                                        if (i != uiplr) {
                                            if (gui.getSelectedPlayer()[i] != null) {
                                                if (gui.getAvailablePlayerModels().get(UISelectedPlayer).equals(gui.getSelectedPlayer()[i])) {
                                                    validSelection = false;
                                                    if (gui.isDebugEnabled()) {
                                                        System.out.println("player " + uiplr + " invalid selection " + UISelectedPlayer);
                                                    }
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    if (validSelection) {
                                        if (gui.isDebugEnabled()) {
                                            System.out.println("player " + uiplr + " valid selection " + UISelectedPlayer);
                                        }
                                        gui.selectPlayer(gui.getAvailablePlayerModels().get(UISelectedPlayer), uiplr);
                                    }
                                    player_selection_menu.selectable.setUISelectingPlayer(Math.min(gui.NPLAYERS - 1, uiplr + 1));
                                    if (!validSelection) {
                                        player_selection_menu.selectable.newInput(SelectionMenuComponent_TABLE.INPUT_ESC_DELETE);
                                    }
                                }
                            }
                        }, new Runnable() {

                            public void run() {
                                int uiplr = player_selection_menu.selectable.getUISelectingPlayer();
                                if (gui.getSelectedPlayer()[uiplr] instanceof XtendedModel) {
                                    gui.selectPlayer(null, uiplr);
                                } else if (uiplr > 0) {
                                    gui.selectPlayer(null, uiplr - 1);
                                    player_selection_menu.selectable.setUISelectingPlayer(uiplr - 1);
                                } else {
                                    boolean noSelectedPlayer = true;
                                    for (int i = 0; i < gui.NPLAYERS; i++) {
                                        noSelectedPlayer = noSelectedPlayer && gui.getSelectedPlayer()[i] == null;
                                    }
                                    if (noSelectedPlayer) {
                                        gui.getGameInterface().cancelGame();
                                    }
                                }
                            }
                        }));
                    }
                }
            }
            player_selection_menu = new SelectionMenuComponent(gui, null, character_selection_menu, SelectionMenuComponent.HORIZONTAL_LAYOUT | SelectionMenuComponent.VERTICAL_LAYOUT);
        }
        gui.getMenu_game_selection().setCurrent(player_selection_menu);
    }

    @Override
    protected void doUnload() {
        super.doUnload();
        player_selection_menu.selectable.setUISelectingPlayer(0);
        character_selection_menu.clear();
        gui.getBgSnd().stop();
    }
    AnimationGLHandler vsLogo = null;
    Rectangle vsLogoBds = null;
    SpriteGLHandler[] icons = new SpriteGLHandler[gui.NPLAYERS];

    @Override
    protected void doLoadOnGL(RenderingSceneGL context) {
        super.doLoadOnGL(context);
        RenderingSceneGL._GLloadSprite((Sprite) gui.getCacheEntry("GFX_BACKGROUND_SCROLLING"), context);
        RenderingSceneGL._GLloadSprite((Sprite) gui.getCacheEntry("GFX_CHARACTER_ICON"), context);
        Animation anim = (Animation) gui.getCacheEntry("GFX_VSLOGO");
        vsLogo = RenderingSceneGL._GLloadAnim(anim, (RenderingSceneGL) context, RenderingSceneGL.PTY_ANIM);
        vsLogoBds = anim.getBounds();
    }

    @Override
    protected void renderToGL(RenderingSceneGL context) {
        super.renderToGL(context);
        try {
            float z = context.getZDepth() * .2f;
            int pad = SelectionMenuComponent.PADDING;
            Dimension selectionSize = new Dimension(
                    (int) ((float) gui.UISELECTIONWIDTH * (float) gui.ICONSIZE.width + ((float) gui.UISELECTIONWIDTH - 1f) * pad),
                    (int) ((float) Math.ceil((float) character_selection_menu.size() / (float) gui.UISELECTIONWIDTH) * (float) (gui.ICONSIZE.height + pad)) - pad);
            player_selection_menu.setBounds(new Rectangle(
                    (int) (((float) context.getWidth() - (float) selectionSize.width) / 2f),
                    (int) (((float) context.getHeight() - (float) selectionSize.height) / 2f),
                    selectionSize.width,
                    selectionSize.height));
            Rectangle csmBounds = player_selection_menu.getBounds();
            Color selc = player_selection_menu.selectable.getSelectionColor();
            selc = GLGeom.getGlowingColor(selc, gui.glow.glow);
            Dimension dim = gui._rbGetDim("GFX_BACKGROUND_SCROLLING").ROI.getSize();
            Sprite._GLRenderSprite(context, Sprite._GLHandlers.getHandler(gui.getCacheHashEntry("GFX_BACKGROUND_SCROLLING")), new Rectangle(context._getLogoPosition(context, TextLayout.POS.LEFT, TextLayout.POS.TOP, dim), dim), z, GLFX.gfx.FX_DOWNREFLEXION_EXT | GLFX.gfx.FX_SCROLLING.bit() | GLFX.gfx.FX_BACKGROUNDREPEAT_EXT | GLFX.gfx.FX_HORIZONTAL.bit() | GLFX.gfx.FX_VERTICAL.bit(), new Point(0, context.getHeight()), selc, 0, null, null, null);
            z += 3;
            player_selection_menu.GLpaintComponent(context, z, GLFX.gfx.FX_DOWNREFLEXION_EXT, new Point(0, context.getHeight()), selc);
            /**
             * the selection status
             */
            context.getHud_user().pushContent("stage", getName(), TextLayout.POS.CENTER, TextLayout.POS.BOTTOM, true);
            vsLogo.play();
            Point vsLogoLoc = new Point(
                    (int) ((float) (context.getWidth() - vsLogoBds.getWidth()) / 2f),
                    (int) (csmBounds.y - vsLogoBds.getHeight() - pad * 2));
            vsLogoBds.setLocation(vsLogoLoc);
            for (int i = 0; i < gui.NPLAYERS; i++) {
                if (gui.getSelectedPlayer()[i] != null) {
                    Rectangle bounds = new Rectangle();
                    bounds.setSize(new Dimension(gui.ICONSIZE.width * 2, gui.ICONSIZE.height * 2));
                    boolean onTheLeft = (i % 2) == 0;
                    float translateX = (float) vsLogoLoc.x + (onTheLeft ? -(float) (i + 2f) / 2f * (float) (bounds.getWidth() + pad)
                            : (float) vsLogoBds.getWidth() + (float) Math.floor((float) i / 2f) * (float) (bounds.getWidth() + pad));
                    bounds.setLocation((int) translateX, (int) vsLogoBds.getBounds().getMaxY() - (int) bounds.getHeight());
                    int transform = !onTheLeft ? GLHandler._GL_TRANSFORM_FLIP_HORIZONTAL_BIT : 0;
                    Sprite icon = (Sprite) gui.getModelIconSprite(gui.getSelectedPlayer()[i]);
                    icons[i] = RenderingSceneGL._GLloadSprite(icon, context);
                    Sprite._GLRenderSprite(context, true, icons[i], bounds, z, GLFX.gfx.FX_DOWNREFLEXION_EXT, new Point(0, context.getHeight()), null, transform, null, null, null);

                }
            }
            vsLogo.runValidate();
            Animation._GLRenderAnimation(context, vsLogo, vsLogoBds, z, GLFX.gfx.FX_DOWNREFLEXION_EXT, new Point(0, context.getHeight()), null, 0, null, null, null);
            dim = gui._rbGetDim("GFX_CHARACTER_ICON").ROI.getSize();
            Sprite._GLRenderSprite(context, Sprite._GLHandlers.getHandler(gui.getCacheHashEntry("GFX_CHARACTER_ICON")), new Rectangle(context._getLogoPosition(context, TextLayout.POS.RIGHT, TextLayout.POS.BOTTOM, dim), dim), z, GLFX.gfx.FX_DOWNREFLEXION_EXT, new Point(0, context.getHeight()), null, 0, null, null, null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void doPhysics() {
        super.doPhysics();
    }
}
