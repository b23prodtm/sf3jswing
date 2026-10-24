/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package thegame.gui;

import thegame.gui.SelectionMenuCell;
import net.sf.jiga.xtended.kernel.BitStack;
import thegame.FieldGui;

/**
 *
 * @author www.b23prodtm.info
 */
public class Selectable_CELL extends Selectable_ {

            FieldGui gui;
            public Selectable_CELL(SelectionMenuCell cell) {
                super(cell);
                this.gui = cell.gui;
            }
            protected final static BitStack stateBits = new BitStack(),  inputBits = new BitStack();
            protected final static int STATE_1 = stateBits._newBitRange();
            protected final static int STATE_0 = stateBits._newBitRange();
            public final static int STATE_SELECTED = STATE_0;
            public final static int STATE_UNSELECTED = STATE_1;
            public final static int STATE_FOCUSED = STATE_0 | STATE_1;
            protected final static int INPUT_1 = inputBits._newBitRange();
            protected final static int INPUT_0 = inputBits._newBitRange();
            public final static int INPUT_ESC_DELETE = 0;
            public final static int INPUT_ENTER_SPACE = INPUT_1;
            public final static int INPUT_MOVE = INPUT_0 | INPUT_1;
            public final static int INPUT_INDEX_SELECTED = INPUT_0;
            private int state = STATE_UNSELECTED;

            public int getState() {
                return state;
            }

            protected int nextState(int input) {
                int state = getState(), nextState = 0;
                if (lookUp(input, INPUT_0, false) && lookUp(state, STATE_0, true) || lookUp(state, STATE_0, true) && lookUp(state, STATE_1, false) || lookUp(input, INPUT_0, true) && lookUp(input, INPUT_1, false) && lookUp(state, STATE_1, true)) {
                    nextState |= STATE_0;
                }
                if (lookUp(state, STATE_0, false) && lookUp(state, STATE_1, true) || lookUp(input, INPUT_0, false) && lookUp(input, INPUT_1, false) || lookUp(state, STATE_1, true) && lookUp(input, INPUT_0, true) || lookUp(state, STATE_0, false) && lookUp(state, STATE_1, false)) {
                    nextState |= STATE_1;
                }
                return nextState;
            }

            protected void setState(int state) {
                if (state == STATE_FOCUSED) {
                    ((SelectionMenuCell)component).cellHasFocus = true;
                    ((SelectionMenuCell)component).isSelected = false;
                }
                if (state == STATE_UNSELECTED) {
                    ((SelectionMenuCell)component).isSelected = false;
                    ((SelectionMenuCell)component).cellHasFocus = false;
                }
                if (state == STATE_SELECTED) {
                    ((SelectionMenuCell)component).isSelected = true;
                    ((SelectionMenuCell)component).cellHasFocus = true;
                }
                this.state = state;
            }

            @Override
            public void reset() {
                setState(STATE_UNSELECTED);
            }
        }

