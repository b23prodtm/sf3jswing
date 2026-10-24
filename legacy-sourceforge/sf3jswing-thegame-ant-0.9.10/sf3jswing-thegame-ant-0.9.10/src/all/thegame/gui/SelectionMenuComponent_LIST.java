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
public class SelectionMenuComponent_LIST extends Selectable_ {

    FieldGui gui;

    public SelectionMenuComponent_LIST(SelectionMenuComponent comp) {
        super(comp);
        this.gui = comp.gui;
    }
    protected final static BitStack stateBits = new BitStack(),  inputBits = new BitStack();
    protected final static int STATE_1 = stateBits._newBitRange();
    protected final static int STATE_0 = stateBits._newBitRange();
    public final static int STATE_CLOSE = 0;
    public final static int STATE_OPEN = STATE_1;
    public final static int STATE_MOVE_UP = STATE_0 | STATE_1;
    public final static int STATE_MOVE_DOWN = STATE_0;
    protected final static int INPUT_2 = inputBits._newBitRange();
    protected final static int INPUT_1 = inputBits._newBitRange();
    protected final static int INPUT_0 = inputBits._newBitRange();
    public final static int INPUT_ENTER_SPACE = 0;
    public final static int INPUT_ESC_DELETE = INPUT_2;
    public final static int INPUT_MOVE_UP = INPUT_1 | INPUT_2;
    public final static int INPUT_MOVE_DOWN = INPUT_1;
    public final static int INPUT_INDEX_NOT_FOUND = INPUT_0 | INPUT_1;
    public final static int INPUT_INDEX_FOUND = INPUT_0 | INPUT_1 | INPUT_2;
    int state = STATE_CLOSE;

    public int getState() {
        return state;
    }

    protected int nextState(int input) {
        int state = getState(), nextState = 0;
        if (lookUp(state, STATE_0, false) && lookUp(state, STATE_1, false) && lookUp(input, INPUT_0, false) && lookUp(input, INPUT_1, true) || lookUp(state, STATE_0, true) && lookUp(input, INPUT_0, false) || lookUp(state, STATE_0, true) && lookUp(input, INPUT_0, true) && lookUp(input, INPUT_1, true) && lookUp(input, INPUT_2, false) || lookUp(state, STATE_0, true) && lookUp(input, INPUT_0, true) && lookUp(input, INPUT_1, false)) {
            nextState |= STATE_0;
        }
        if (lookUp(state, STATE_0, false) && lookUp(input, INPUT_0, false) && lookUp(input, INPUT_1, false) && lookUp(input, INPUT_2, false) || lookUp(state, STATE_0, true) && lookUp(state, STATE_1, true) && lookUp(input, INPUT_0, false) && lookUp(input, INPUT_1, false) || lookUp(input, INPUT_0, false) && lookUp(input, INPUT_1, true) && lookUp(input, INPUT_2, true) || lookUp(state, STATE_0, false) && lookUp(state, STATE_1, true) && lookUp(input, INPUT_0, false) && lookUp(input, INPUT_1, true) || lookUp(state, STATE_0, false) && lookUp(state, STATE_1, true) && lookUp(input, INPUT_0, true) || lookUp(state, STATE_1, true) && lookUp(input, INPUT_0, true) && lookUp(input, INPUT_1, false) || lookUp(state, STATE_0, true) && lookUp(state, STATE_1, false) && lookUp(input, INPUT_0, true) && lookUp(input, INPUT_1, true) && lookUp(input, INPUT_2, false)) {
            nextState |= STATE_1;
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
        SelectionMenuComponent parent = ((SelectionMenuComponent) component).parent;
        Selectable_CELL cell = menu.length > focusedMenu ? menu[focusedMenu].selectable : null;
        String leftRightSFX = UISelectingPlayer >= (float) gui.NPLAYERS / 2f ? "_R" : "_L";
        if (state == STATE_OPEN) {
            if (cell != null) {
                cell.newInput(cell.INPUT_ENTER_SPACE);
                gui._soundInterface.get("SFX_MENU_VALIDATE" + leftRightSFX).play();
            }
        } else if (state == STATE_CLOSE) {
            if (cell != null) {
                cell.newInput(cell.INPUT_ESC_DELETE);
                gui._soundInterface.get("SFX_MENU_BACK" + leftRightSFX).play();
                if (parent instanceof SelectionMenuComponent) {
                    parent.selectable.reset();
                }
            }
        } else if (state == STATE_MOVE_DOWN) {
            if (cell != null) {
                gui._soundInterface.get("SFX_MENU_SELECT" + leftRightSFX).play();
                cell.newInput(cell.INPUT_MOVE);
                focusedMenu++;
                if (focusedMenu >= menu.length) {
                    focusedMenu = Math.min(menu.length - 1, focusedMenu - menu.length);
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
                focusedMenu--;
                if (focusedMenu < 0) {
                    focusedMenu = menu.length + Math.max(-menu.length, focusedMenu);
                }
                cell = menu.length > focusedMenu ? menu[focusedMenu].selectable : null;
                if (cell != null) {
                    cell.newInput(cell.INPUT_INDEX_SELECTED);
                }
            }
        }
        this.state = state;
    }

    @Override
    public void reset() {
        setState(STATE_CLOSE);
    }
}

