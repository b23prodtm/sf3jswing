/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thegame.layers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import net.sf.jiga.xtended.impl.Sprite;
import net.sf.jiga.xtended.impl.game.RenderingScene;
import net.sf.jiga.xtended.impl.game.gl.RenderingSceneGL;
import net.sf.jiga.xtended.impl.game.TextLayout;
import net.sf.jiga.xtended.impl.game.gl.GLFX;
import net.sf.jiga.xtended.impl.game.gl.geom.GLGeom;
import net.sf.jiga.xtended.kernel.JXAenvUtils;
import net.sf.jiga.xtended.ui.UIMessage;
import thegame.FieldGui;
import thegame.gui.SelectionMenuCell;
import thegame.gui.SelectionMenuComponent;
import thegame.Thegamelayer;

/**
 *
 * @author tianabruno
 */
public class GameMenu extends Thegamelayer {

    public GameMenu(FieldGui gui) {
        super(gui, "Game Menu");
        menu = new ArrayList<SelectionMenuCell>();
        menu_play = new ArrayList<SelectionMenuCell>();
        addToMenu(menu, "MENU_exit", new Runnable() {

            public void run() {
                if (JOptionPane.OK_OPTION == (GameMenu.this.gui.getScene().isFullscreen() ? JOptionPane.OK_OPTION : UIMessage.showConfirmDialog(GameMenu.this.gui.getScene(), "Do you wish to exit Java now ?", "exit Java ?", JOptionPane.OK_CANCEL_OPTION))) {
                    System.exit(0);
                }
            }
        }, null);

        addToMenu(menu, "MENU_options", new Runnable() {

            public void run() {
                GameMenu.this.gui.getScene().setLoading(true);
                GameMenu.this.gui.getGameInterface().options();
                GameMenu.this.gui.getScene().setLoading(false);
            }
        }, null);
        addToMenu(menu, (GameMenu.this.gui.getLayout() & RenderingScene.options.MODE_EXT_EXCLUSIVE.bitMask()) != 0 ? "-" : "MENU_fullscreen", new Runnable() {

            public void run() {
                GameMenu.this.gui.switchFullScreen();
            }
        }, null);
        addToMenu(menu, "MENU_play", new Runnable() {

            public void run() {
                GameMenu.this.gui.getMenu_game_selection().setCurrent(play_menu);
            }
        }, null);
        addToMenu(menu_play, "MENU_play_onePlr", new Runnable() {

            public void run() {
                GameMenu.this.gui.getGameInterface().playAgainstMachine();
            }
        }, null);
        addToMenu(menu_play, "MENU_play_twoPlr", new Runnable() {

            public void run() {
                GameMenu.this.gui.getGameInterface().playAgainstHuman();
            }
        }, null);
        addToMenu(menu_play, "MENU_play_twoNetHOST", new Runnable() {

            public void run() {
                GameMenu.this.gui.getGameInterface().runServerMode();
            }
        }, null);
        addToMenu(menu_play, "MENU_play_twoNetCLIENT", new Runnable() {

            public void run() {
                GameMenu.this.gui.getGameInterface().runClientMode();
            }
        }, null);
    }
    private List<SelectionMenuCell> menu_play;
    protected SelectionMenuComponent root_menu, play_menu;
    private List<SelectionMenuCell> menu;
    boolean showReadme = true;

    @Override
    protected void doLoad() {
        super.doLoad();
        if (showReadme) {
            URL readme = getClass().getResource("/readme.txt");
            if (readme == null) {
                System.out.println("no readme file found in jar resources /readme.txt");
            } else {
                try {
                    BufferedReader readmeSTREAM = new BufferedReader(new InputStreamReader(readme.openStream()));
                    if (readmeSTREAM.ready()) {
                        gui.consoleHide();
                        gui.getScene().getGCO().clearContents();
                        gui.logLn("README FILE >>");
                        String line;
                        while ((line = readmeSTREAM.readLine()) != null) {
                            gui.logLn(line);
                        }
                        gui.logLn("<< README FILE");
                        gui.getScene().getGCO().scrollTop();
                    }
                    readmeSTREAM.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        gui.setBgSnd(gui._soundInterface.get("SFX_SNDTRK_menu"), true);
        root_menu = root_menu instanceof SelectionMenuComponent ? root_menu : new SelectionMenuComponent(gui, "Main Menu", menu);
        play_menu = play_menu instanceof SelectionMenuComponent ? play_menu : new SelectionMenuComponent(gui, "Play Menu", root_menu, menu_play);
        gui.getMenu_game_selection().setCurrent(root_menu);
    }

    @Override
    protected void doUnload() {
        super.doUnload();
        gui.getBgSnd().stop();
    }

    @Override
    protected void doLoadOnGL(RenderingSceneGL context) {
        super.doLoadOnGL(context);
        RenderingSceneGL._GLloadSprite((Sprite) gui.getCacheEntry("GFX_MENU_BACKGROUND"), context);
        RenderingSceneGL._GLloadSprite((Sprite) gui.getCacheEntry("GFX_LOGO"), context);
    }

    @Override
    protected void renderToGL(RenderingSceneGL context) {
        super.renderToGL(context);
        if (context.isLoading()) {
            return;
        } else if (showReadme) {
            gui.consoleShow();
            showReadme = false;
        }
        try {
            float z = gui.bgDepth;
            Dimension dim = gui._GLgetDimZRatio(context, (double) z, "GFX_MENU_BACKGROUND");
            Sprite._GLRenderSprite(context, Sprite._GLHandlers.getHandler(gui.getCacheHashEntry("GFX_MENU_BACKGROUND")), new Rectangle(context._getLogoPosition(context, TextLayout.POS.CENTER, TextLayout.POS.BOTTOM, dim), dim), z,
                    GLFX.gfx.FX_DOWNREFLEXION_EXT, new Point(0, context.getHeight()), null, 0, null, null, null);
            context.getHud_user().pushContent("stage", getName(), TextLayout.POS.CENTER, TextLayout.POS.BOTTOM, true);
            z++;
            dim = gui._rbGetDim("GFX_LOGO").ROI.getSize();
            Sprite._GLRenderSprite(context, Sprite._GLHandlers.getHandler(gui.getCacheHashEntry("GFX_MENU_BACKGROUND")), new Rectangle(context._getLogoPosition(context, TextLayout.POS.CENTER, TextLayout.POS.TOP, dim), dim), z,
                    GLFX.gfx.FX_DOWNREFLEXION_EXT, new Point(0, context.getHeight()), GLGeom.getGlowingColor(Color.ORANGE, gui.glow.glow),
                    0, null, null, null);

            z++;
            Dimension menuSize = new Dimension((int) ((float) context.getWidth() / 2f), (int) ((float) context.getHeight() / 2f));
            menuSize = gui._GLgetDimZRatio(context, z, menuSize);
            Rectangle menuBounds = new Rectangle(
                    (int) ((float) (context.getWidth() - menuSize.width) / 2f),
                    (int) ((float) (context.getHeight() - menuSize.height) / 2f),
                    menuSize.width,
                    menuSize.height);
            gui.getMenu_game_selection().getCurrent().setBounds(menuBounds);
            gui.getMenu_game_selection().getCurrent().GLpaintComponent(context, z, GLFX.gfx.FX_DOWNREFLEXION_EXT, new Point(0, context.getHeight()), GLGeom.getGlowingColor(Color.ORANGE, gui.glow.glow));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void addToMenu(List<SelectionMenuCell> list, String stringResourceKey, Runnable enterAction, Runnable escapeAction) {
        list.add(new SelectionMenuCell(gui, stringResourceKey, enterAction, escapeAction));
    }

    private SelectionMenuCell removeFromMenu(List<SelectionMenuCell> list, String stringResourceKey) {
        synchronized (list) {
            for (SelectionMenuCell c : list) {
                if (c.name.equals(stringResourceKey)) {
                    list.remove(c);
                    return c;
                }
            }
        }
        return null;
    }
}
