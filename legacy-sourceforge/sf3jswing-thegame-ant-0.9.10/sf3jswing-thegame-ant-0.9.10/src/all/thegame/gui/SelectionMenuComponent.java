/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thegame.gui;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import net.sf.jiga.xtended.impl.Sprite;
import net.sf.jiga.xtended.impl.game.gl.RenderingSceneGL;
import net.sf.jiga.xtended.impl.game.physics.Player;
import thegame.FieldGui;

/**
 * FieldGui component that shows up a rolling (vertical) / grid (horizontal) menu. 
 * @author www.b23prodtm.info
 */
public class SelectionMenuComponent extends JComponent implements KeyListener {

    public final static int PADDING = 10;
    public final static int VERTICAL_LAYOUT = 8;
    public final static int HORIZONTAL_LAYOUT = 1;
    List<SelectionMenuCell> menu;
    Paint gradient;
    SelectionMenuComponent parent;
    int layout;
    public Selectable_ selectable;

    /** creates a new instance. */
    public SelectionMenuComponent(FieldGui gui, String name, List<? extends SelectionMenuCell> menu, int layout) {
        this(gui, name, null, menu, layout);
    }

    /** creates a new instance. the default layout VERTICAL_LAYOUT is used.*/
    public SelectionMenuComponent(FieldGui gui, String name, List<? extends SelectionMenuCell> menu) {
        this(gui, name, null, menu, VERTICAL_LAYOUT);
    }

    /** creates a new instance. the default layout VERTICAL_LAYOUT is used.*/
    public SelectionMenuComponent(FieldGui gui, String name, SelectionMenuComponent parent, List<? extends SelectionMenuCell> menu) {
        this(gui, name, parent, menu, VERTICAL_LAYOUT);
    }
    FieldGui gui;

    /** creates a new instance.
     * @param name the name to display as the title
    @param parent the parent menu, or null
    @param menu the menu cells
     * @param layout the layout to use; must be a combination of the available layouts and displays
     * @see #HORIZONTAL_LAYOUT
     * @see #VERTICAL_LAYOUT
     * @param quitMenuAction
    @see SelectionMenuComponent(String, SelectionMenuCell[])*/
    public SelectionMenuComponent(FieldGui gui, String name, SelectionMenuComponent parent, List<? extends SelectionMenuCell> menu, int layout) {
        super();
        this.gui = gui;
        setName(name);
        assert layout != 0 : getClass().getName() + " INVALID SELECTED LAYOUT !";
        this.layout = layout;
        this.menu = new ArrayList<SelectionMenuCell>();
        if (parent instanceof SelectionMenuComponent) {
            this.menu.add(new SelectionMenuCell(gui, "back to " + parent.getName(), new Runnable() {

                public void run() {
                    SelectionMenuComponent.this.gui.getMenu_game_selection().current = SelectionMenuComponent.this.parent;
                }
            }, null));            
        }
        this.menu.addAll(menu);
        this.parent = parent;
        if ((layout & HORIZONTAL_LAYOUT) != 0 && (layout & VERTICAL_LAYOUT) != 0) {
            selectable = new SelectionMenuComponent_TABLE(this);
        } else {
            selectable = new SelectionMenuComponent_LIST(this);
        }
        selectable.reset();
    }

    public SelectionMenuCell[] getMenuCells() {
        return menu.toArray(new SelectionMenuCell[]{});
    }

    public int getFocusedMenu() {
        int i = 0;
        synchronized(menu){for (Iterator<SelectionMenuCell> c = menu.iterator(); c.hasNext(); i++) {
            if (c.next().isCellHasFocus()) {
                return i;
            }
        }}
        return 0;
    }
    private Rectangle gradientBounds = new Rectangle();
    Sprite offscreen = null;

    /** INTERNAL : renders immediatelly*/
    public void GLpaintComponent(RenderingSceneGL gld, double z, int fx, Point fx_loc, Color fx_color) throws Exception {
        Rectangle menuBounds = getBounds();
        Rectangle lastBounds = null;
        int selected = getFocusedMenu();
        if (gui.isDebugEnabled()) {
            System.err.println("menu layout : " + layout);
        }
        if ((layout & VERTICAL_LAYOUT) != 0 && (layout & HORIZONTAL_LAYOUT) == 0) {
            SelectionMenuCell[] ordered = new SelectionMenuCell[menu.size()];
            if (menu.size() % 2 == 0) {
                ordered = new SelectionMenuCell[menu.size()+ 1];
            }
            selected -= (int) Math.floor((float) menu.size() / 2f);
            if (selected < 0) {
                selected = menu.size() + selected;
            }
            for (int i = 0; i < menu.size(); i++) {
                ordered[i] = menu.get((selected + i) % menu.size());
            }
            /* keep odd count to reach a middle while refreshing*/
            if (menu.size() % 2 == 0) {
                ordered[menu.size()] = new SelectionMenuCell(gui, "----", null, null);
            }
            for (SelectionMenuCell cell : ordered) {
                if (cell instanceof SelectionMenuCell) {
                    if (gui.isDebugEnabled()) {
                        System.err.println("menu cell : " + cell.getText());
                    }
                    Rectangle cellBounds = new Rectangle(menuBounds.x, (int) (lastBounds instanceof Rectangle ? lastBounds.getMaxY() : menuBounds.y), menuBounds.width, (int) ((float) menuBounds.height / (float) menu.size()));
                    cellBounds.grow(-PADDING, -(int) ((float) PADDING / 2f));
                    cell.setBounds(cellBounds);
                    cell.GLpaintComponent(gld, z, fx, fx_loc, fx_color);
                    lastBounds = cellBounds;
                }
            }
        }
        if ((layout & VERTICAL_LAYOUT) != 0 && (layout & HORIZONTAL_LAYOUT) != 0) {
            /** the characters selection panel */
            int selectionWidth = gui.UISELECTIONWIDTH;
            float pad = PADDING;
            lastBounds = menuBounds;
            int n = 0;
            for (SelectionMenuCell cell : menu) {
                if (cell instanceof SelectionMenuCell) {
                    Rectangle bounds = new Rectangle((n % selectionWidth) != 0 ? (int) (lastBounds.getMaxX() + pad) : menuBounds.x, (n % selectionWidth) != 0 || n == 0 ? lastBounds.y : (int) (lastBounds.getMaxY() + pad), gui.ICONSIZE.width, gui.ICONSIZE.height);
                    cell.setBounds(bounds);
                    cell.GLpaintComponent(gld, z, fx, fx_loc, fx_color);
                    lastBounds = bounds;
                    n++;
                }
            }
        }
    }

    /*protected void paintComponent(Graphics pg) {
        Rectangle menuBounds = getBounds();
        Graphics2D g2 = Sprite.wrapRendering(pg);
        Graphics2D jai = null;
        Graphics dataG = null;
        if (menu.length == 0) {
            return;
        }
        g2.translate(-menuBounds.x, -menuBounds.y);
        try {
            if (offscreen == null) {
                offscreen = new Sprite(Sprite.createBufferedImage(menuBounds.getSize(), Sprite.DEFAULT_TYPE), "image/x-png", menuBounds.getSize());
                offscreen.setRenderingScene(gui.getScene());
            }
            offscreen.setBounds(menuBounds);
            offscreen.clearImageGraphics();
            dataG = Sprite._createImageGraphics(offscreen.getImage(gui.getScene()));
            jai = Sprite.wrapRendering(dataG);
        } catch (InterruptedException e) {
            if (JXAenvUtils._debug) {
                e.printStackTrace();
            }
            return;
        } finally {
            jai.translate(-menuBounds.x, -menuBounds.y);
            Rectangle lastBounds = null;
            int selected = 0;
            for (int i = 0; i < menu.length; i++) {
                if (menu[i].isCellHasFocus()) {
                    selected = i;
                }
            }
            if (JXAenvUtils._debug) {
                System.err.println("menu layout : " + layout);
            }
            if ((layout & VERTICAL_LAYOUT) != 0 && (layout & HORIZONTAL_LAYOUT) == 0) {
                SelectionMenuCell[] ordered = new SelectionMenuCell[menu.length];
                if (menu.length % 2 == 0) {
                    ordered = new SelectionMenuCell[menu.length + 1];
                }
                selected -= (int) Math.floor((float) menu.length / 2f);
                if (selected < 0) {
                    selected = menu.length + selected;
                }
                for (int i = 0; i < menu.length; i++) {
                    ordered[i] = menu[(selected + i) % menu.length];
                }
                /* keep odd count to reach a middle while refreshing*
                if (menu.length % 2 == 0) {
                    ordered[menu.length] = new SelectionMenuCell(gui, "----", null, null);
                }
                for (SelectionMenuCell cell : ordered) {
                    if (cell instanceof SelectionMenuCell) {
                        if (JXAenvUtils._debug) {
                            System.err.println("menu cell : " + cell.getText());
                        }
                        Rectangle cellBounds = new Rectangle(menuBounds.x, (int) (lastBounds instanceof Rectangle ? lastBounds.getMaxY() : menuBounds.y), menuBounds.width, (int) ((float) menuBounds.height / (float) menu.length));
                        cellBounds.grow(-PADDING, -(int) ((float) PADDING / 2f));
                        cell.setBounds(cellBounds);
                        Color c = jai.getColor();
                        jai.setColor(Color.RED);
                        if (net.sf.jiga.xtended.impl.Sprite._paintStatus) {
                            jai.draw(cellBounds);
                        }
                        jai.translate(cellBounds.x, cellBounds.y);
                        cell.paintComponent(jai);
                        jai.translate(-cellBounds.x, -cellBounds.y);
                        jai.setColor(c);
                        lastBounds = cellBounds;
                    }
                }
                if (gradient == null || !gradientBounds.equals(menuBounds)) {
                    gradient = new GradientPaint(0f, 0f, new Color(1f, 1f, 1f, 0f), 0f, (float) menuBounds.getHeight() / 2f, new Color(1f, 1f, 1f, 1f), true);
                    gradientBounds = menuBounds;
                }
                offscreen.setComposited(false);
                offscreen.setCompositeEnabled(true);
                offscreen.setComposite(AlphaComposite.DstIn);
                offscreen.setPaint(gradient);
            }
            if ((layout & VERTICAL_LAYOUT) != 0 && (layout & HORIZONTAL_LAYOUT) != 0) {
                /** the characters selection panel *
                int selectionWidth = gui.UISELECTIONWIDTH;
                float pad = PADDING;
                lastBounds = menuBounds;
                int n = 0;
                for (SelectionMenuCell cell : menu) {
                    if (cell instanceof SelectionMenuCell) {
                        Rectangle bounds = new Rectangle((n % selectionWidth) != 0 ? (int) (lastBounds.getMaxX() + pad) : menuBounds.x, (n % selectionWidth) != 0 || n == 0 ? lastBounds.y : (int) (lastBounds.getMaxY() + pad), gui.ICONSIZE.width, gui.ICONSIZE.height);
                        cell.setBounds(bounds);
                        jai.translate(bounds.x, bounds.y);
                        cell.paintComponent(jai);
                        jai.translate(-bounds.x, -bounds.y);
                        lastBounds = bounds;
                        n++;
                    }
                }
            }
            dataG.dispose();
            try {
                offscreen.runValidate();
                offscreen.draw(gui.getScene(), g2);
                if (parent instanceof SelectionMenuComponent) {
                    String pName = "";
                    for (SelectionMenuCell pcell : parent.getMenuCells()) {
                        if (pcell.isCellHasFocus()) {
                            pName = " " + pcell.getText() + " ";
                            break;
                        }
                    }
                    int font = 2;
                    Dimension labelDim = FieldGui._baseCodeStringCLIPSIZE(jai, pName, font);
                    Shape clip = g2.getClip();
                    Rectangle scrollClip = new Rectangle((int) menuBounds.getX(), (int) menuBounds.getY() - labelDim.height - PADDING, (int) menuBounds.getWidth(), labelDim.height);
                    g2.clip(scrollClip);
                    FieldGui._baseCodeStringDRAW(gui.getScene(), g2, scrollClip.x, scrollClip.y, font, pName, Sprite.gfx.FX_SCROLLING.bit() | Sprite.gfx.FX_HORIZONTAL.bit() | Sprite.gfx.FX_BACKGROUNDREPEAT_EXT, new Point(0, 0), null);
                    g2.setClip(clip);
                }
                offscreen.setComposite(null);
                offscreen.setPaint(null);
            } catch (InterruptedException ex) {
                if (JXAenvUtils._debug) {
                    ex.printStackTrace();
                }
            } finally {
                g2.translate(menuBounds.x, menuBounds.y);
            }
        }
    }
*/
    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        boolean b = false;
        int kCode = e.getKeyCode();
        if (Player._keyCodeMap.get(Player.key.ONE_LK) == kCode || Player._keyCodeMap.get(Player.key.ONE_LP) == kCode) {
            kCode = KeyEvent.VK_ENTER;
        }
        if (Player._keyCodeMap.get(Player.key.ONE_MK) == kCode || Player._keyCodeMap.get(Player.key.ONE_MP) == kCode) {
            kCode = KeyEvent.VK_ESCAPE;
        }
        switch (kCode) {
            case KeyEvent.VK_UP:
                if (selectable instanceof SelectionMenuComponent_LIST) {
                    selectable.newInput(((SelectionMenuComponent_LIST) selectable).INPUT_MOVE_UP);
                    if (getFocusedMenu() >= 0 && getFocusedMenu() < menu.size()) {
                        selectable.newInput(((SelectionMenuComponent_LIST) selectable).INPUT_INDEX_FOUND);
                    } else {
                        selectable.newInput(((SelectionMenuComponent_LIST) selectable).INPUT_INDEX_NOT_FOUND);
                    }
                }
                if (selectable instanceof SelectionMenuComponent_TABLE) {
                    selectable.newInput(((SelectionMenuComponent_TABLE) selectable).INPUT_MOVE_UP);
                }
                b = true;
                break;
            case KeyEvent.VK_DOWN:
                if (selectable instanceof SelectionMenuComponent_LIST) {
                    selectable.newInput(((SelectionMenuComponent_LIST) selectable).INPUT_MOVE_DOWN);
                    if (getFocusedMenu() >= 0 && getFocusedMenu() < menu.size()) {
                        selectable.newInput(((SelectionMenuComponent_LIST) selectable).INPUT_INDEX_FOUND);
                    } else {
                        selectable.newInput(((SelectionMenuComponent_LIST) selectable).INPUT_INDEX_NOT_FOUND);
                    }
                }
                if (selectable instanceof SelectionMenuComponent_TABLE) {
                    selectable.newInput(((SelectionMenuComponent_TABLE) selectable).INPUT_MOVE_DOWN);
                }
                b = true;
                break;
            case KeyEvent.VK_RIGHT:
                if (selectable instanceof SelectionMenuComponent_LIST) {
                    selectable.newInput(((SelectionMenuComponent_LIST) selectable).INPUT_MOVE_DOWN);
                    if (getFocusedMenu() >= 0 && getFocusedMenu() < menu.size()) {
                        selectable.newInput(((SelectionMenuComponent_LIST) selectable).INPUT_INDEX_FOUND);
                    } else {
                        selectable.newInput(((SelectionMenuComponent_LIST) selectable).INPUT_INDEX_NOT_FOUND);
                    }
                }
                if (selectable instanceof SelectionMenuComponent_TABLE) {
                    selectable.newInput(((SelectionMenuComponent_TABLE) selectable).INPUT_MOVE_RIGHT);
                }
                b = true;
                break;
            case KeyEvent.VK_LEFT:
                if (selectable instanceof SelectionMenuComponent_LIST) {
                    selectable.newInput(((SelectionMenuComponent_LIST) selectable).INPUT_MOVE_UP);
                    if (getFocusedMenu() >= 0 && getFocusedMenu() < menu.size()) {
                        selectable.newInput(((SelectionMenuComponent_LIST) selectable).INPUT_INDEX_FOUND);
                    } else {
                        selectable.newInput(((SelectionMenuComponent_LIST) selectable).INPUT_INDEX_NOT_FOUND);
                    }
                }
                if (selectable instanceof SelectionMenuComponent_TABLE) {
                    selectable.newInput(((SelectionMenuComponent_TABLE) selectable).INPUT_MOVE_LEFT);
                }
                b = true;
                break;
            case KeyEvent.VK_ESCAPE:
            case KeyEvent.VK_BACK_SPACE:
                if (selectable instanceof SelectionMenuComponent_LIST) {
                    selectable.newInput(((SelectionMenuComponent_LIST) selectable).INPUT_ESC_DELETE);
                }
                if (selectable instanceof SelectionMenuComponent_TABLE) {
                    selectable.newInput(((SelectionMenuComponent_TABLE) selectable).INPUT_ESC_DELETE);
                }
                menu.get(getFocusedMenu()).doEscape();
                b = true;
                break;
            case KeyEvent.VK_ENTER:
            case KeyEvent.VK_SPACE:
                if (selectable instanceof SelectionMenuComponent_LIST) {
                    selectable.newInput(((SelectionMenuComponent_LIST) selectable).INPUT_ENTER_SPACE);
                }
                if (selectable instanceof SelectionMenuComponent_TABLE) {
                    selectable.newInput(((SelectionMenuComponent_TABLE) selectable).INPUT_ENTER_SPACE);
                }
                menu.get(getFocusedMenu()).doSelection();
                b = true;
                break;
            default:
                break;
        }
        if (b) {
            e.consume();
        }
    }

    public void keyReleased(KeyEvent e) {
        reset();
        e.consume();
    }

    public void reset() {
        selectable.reset();
    }
}

