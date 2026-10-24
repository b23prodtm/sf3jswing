/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thegame.gui;

import thegame.gui.SelectionMenuComponent;
import net.sf.jiga.xtended.kernel.BitStack;
import thegame.FieldGui;

/**
 *
 * @author www.b23prodtm.info
 */
public class SelectionMenuComponent_TABLE extends Selectable_ {

    protected final static BitStack stateBits = new BitStack(),  inputBits = new BitStack();
    FieldGui gui;

    public SelectionMenuComponent_TABLE(SelectionMenuComponent comp) {
        super(comp);
        this.gui = comp.gui;
    }
    protected final static int STATE_2 = stateBits._newBitRange();
    protected final static int STATE_1 = stateBits._newBitRange();
    protected final static int STATE_0 = stateBits._newBitRange();
    public final static int STATE_IDLE = 0;
    public final static int STATE_CLOSE = STATE_2;
    public final static int STATE_MOVE_UP = STATE_1 | STATE_2;
    public final static int STATE_MOVE_DOWN = STATE_1;
    public final static int STATE_MOVE_RIGHT = STATE_0 | STATE_1;
    public final static int STATE_MOVE_LEFT = STATE_0 | STATE_1 | STATE_2;
    public final static int STATE_OPEN = STATE_0;
    protected final static int INPUT_2 = inputBits._newBitRange();
    protected final static int INPUT_1 = inputBits._newBitRange();
    protected final static int INPUT_0 = inputBits._newBitRange();
    public final static int INPUT_MOVE_UP = 0;
    public final static int INPUT_MOVE_RIGHT = INPUT_2;
    public final static int INPUT_MOVE_LEFT = INPUT_1 | INPUT_2;
    public final static int INPUT_MOVE_DOWN = INPUT_1;
    public final static int INPUT_ESC_DELETE = INPUT_0 | INPUT_1;
    public final static int INPUT_ENTER_SPACE = INPUT_0 | INPUT_1 | INPUT_2;
    private int state = 0;

    public int getState() {
        return state;
    }

    protected int nextState(int input) {
        int state = getState(), nextState = 0;
        if (lookUp(state, STATE_0, false) && lookUp(state, STATE_1, false) && lookUp(state, STATE_2, false) && lookUp(input, INPUT_0, false) && lookUp(input, INPUT_2, true) || lookUp(state, STATE_0, false) && lookUp(state, STATE_1, true) && lookUp(input, INPUT_0, false) && lookUp(input, INPUT_2, true) || lookUp(state, STATE_0, true) && lookUp(state, STATE_1, true) && lookUp(input, INPUT_0, false) && lookUp(input, INPUT_2, true) || lookUp(state, STATE_0, false) && lookUp(state, STATE_1, false) && lookUp(state, STATE_2, false) && lookUp(input, INPUT_0, true) && lookUp(input, INPUT_1, true) && lookUp(input, INPUT_2, true)) {
            nextState |= STATE_0;
        }
        if (lookUp(state, STATE_0, false) && lookUp(state, STATE_1, false) && lookUp(state, STATE_2, false) && lookUp(input, INPUT_0, false) || lookUp(state, STATE_1, true) && lookUp(input, INPUT_0, false)) {
            nextState |= STATE_1;
        }
        if (lookUp(state, STATE_0, false) && lookUp(state, STATE_1, false) && lookUp(state, STATE_2, false) && lookUp(input, INPUT_0, false) && lookUp(input, INPUT_1, false) && lookUp(input, INPUT_2, false) || lookUp(state, STATE_1, true) && lookUp(input, INPUT_0, false) && lookUp(input, INPUT_1, false) && lookUp(input, INPUT_2, false) || lookUp(state, STATE_0, false) && lookUp(state, STATE_1, false) && lookUp(state, STATE_2, false) && lookUp(input, INPUT_0, false) && lookUp(input, INPUT_1, true) && lookUp(input, INPUT_2, true) || lookUp(state, STATE_1, true) && lookUp(input, INPUT_0, false) && lookUp(input, INPUT_1, true) && lookUp(input, INPUT_2, true) || lookUp(state, STATE_0, false) && lookUp(state, STATE_1, false) && lookUp(state, STATE_2, false) && lookUp(input, INPUT_0, true) && lookUp(input, INPUT_1, true) && lookUp(input, INPUT_2, false)) {
            nextState |= STATE_2;
        }
        return nextState;
    }

    @Override
    public void setUISelectingPlayer(int UISelectingPlayer) {
        super.setUISelectingPlayer(UISelectingPlayer);
        SelectionMenuCell[] menu = ((SelectionMenuComponent) component).getMenuCells();
        for (int i = 0; i < menu.length; i++) {
            menu[i].selectable.setUISelectingPlayer(UISelectingPlayer);
        }
    }

    protected void setState(int state) {
        int focusedMenu = ((SelectionMenuComponent) component).getFocusedMenu();
        SelectionMenuCell[] menu = ((SelectionMenuComponent) component).getMenuCells();
        Selectable_CELL cell = menu.length > focusedMenu ? menu[focusedMenu].selectable : null;
        String leftRightSFX = UISelectingPlayer >= (float) gui.NPLAYERS / 2f ? "_R" : "_L";
        if (state == STATE_IDLE) {
            if (cell != null) {
                cell.newInput(cell.INPUT_INDEX_SELECTED);
            }
        } else if (state == STATE_CLOSE) {
            if (cell != null) {
                cell.newInput(cell.INPUT_ESC_DELETE);
                gui._soundInterface.get("SFX_MENU_BACK" + leftRightSFX).play();
            }
        } else if (state == STATE_MOVE_DOWN) {
            if (cell != null) {
                gui._soundInterface.get("SFX_MENU_SELECT" + leftRightSFX).play();
                cell.newInput(cell.INPUT_MOVE);
                focusedMenu += gui.UISELECTIONWIDTH;
                if (focusedMenu >= menu.length) {
                    focusedMenu = Math.min(menu.length - 1, focusedMenu - menu.length);
                }
                cell = menu.length > focusedMenu ? menu[focusedMenu].selectable : null;
                if (cell != null) {
                    cell.newInput(cell.INPUT_INDEX_SELECTED);
                }
            }
        } else if (state == STATE_MOVE_LEFT) {
            if (cell != null) {
                gui._soundInterface.get("SFX_MENU_SELECT" + leftRightSFX).play();
                cell.newInput(cell.INPUT_MOVE);
                focusedMenu--;
                if (focusedMenu < 0) {
                    focusedMenu = menu.length - 1;
                }
                cell = menu.length > focusedMenu ? menu[focusedMenu].selectable : null;
                if (cell != null) {
                    cell.newInput(cell.INPUT_INDEX_SELECTED);
                }
            }
        } else if (state == STATE_MOVE_RIGHT) {
            if (cell != null) {
                gui._soundInterface.get("SFX_MENU_SELECT" + leftRightSFX).play();
                cell.newInput(cell.INPUT_MOVE);
                focusedMenu++;
                if (focusedMenu >= menu.length) {
                    focusedMenu = 0;
                }
                cell = menu.length > focusedMenu ? menu[focusedMenu].selectable : null;
                if (cell != null) {
                    cell.newInput(cell.INPUT_INDEX_SELECTED);
                }
            }
        } else if (state == STATE_MOVE_UP) {
            if (cell != null) {
                gui._soundInterface.get("SFX_MENU_SELECT" + leftRightSFX).play();
                cell.newInput(cell.INPUT_MOVE);
                focusedMenu -= gui.UISELECTIONWIDTH;
                if (focusedMenu < 0) {
                    focusedMenu = menu.length + Math.max(-menu.length, focusedMenu);
                }
                cell = menu.length > focusedMenu ? menu[focusedMenu].selectable : null;
                if (cell != null) {
                    cell.newInput(cell.INPUT_INDEX_SELECTED);
                }
            }
        } else if (state == STATE_OPEN) {
            if (cell != null) {
                cell.newInput(cell.INPUT_ENTER_SPACE);
                gui._soundInterface.get("SFX_MENU_VALIDATE" + leftRightSFX).play();
            }
        }
        this.state = state;
    }

    @Override
    public void reset() {
        setState(STATE_IDLE);
        setState(STATE_CLOSE);
        setState(STATE_IDLE);
    }
}

