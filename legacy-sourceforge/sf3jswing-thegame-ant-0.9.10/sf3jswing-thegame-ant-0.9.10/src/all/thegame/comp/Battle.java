/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thegame.comp;

import net.sf.jiga.xtended.impl.system.LogicalSystem;
import net.sf.jiga.xtended.kernel.BitStack;

/**
 *
 * @author www.b23prodtm.info
 */
public class Battle extends LogicalSystem {

    static BitStack bits = new BitStack();
    final static int STATE_3 = bits._newBitRange();
    final static int STATE_2 = bits._newBitRange();
    final static int STATE_1 = bits._newBitRange();
    final static int STATE_0 = bits._newBitRange();
    public final static int STATE_ROUND_1 = 0;
    public final static int STATE_ROUND_2 = STATE_3;
    public final static int STATE_ROUND_3 = STATE_2 | STATE_3;
    public final static int STATE_ROUND_FINAL = STATE_2;
    public final static int STATE_PAUSE = STATE_1 | STATE_2;
    public final static int STATE_START = STATE_1 | STATE_2 | STATE_3;
    public final static int STATE_FINISH_1X = STATE_0 | STATE_1 | STATE_2 | STATE_3;
    public final static int STATE_FINISH_2X = STATE_0 | STATE_1 | STATE_2;
    public final static int STATE_FINISH_DRAW = STATE_0 | STATE_1;
    public final static int STATE_EXIT = STATE_0;
    static BitStack inputBits = new BitStack();
    final static int INPUT_2 = inputBits._newBitRange();
    final static int INPUT_1 = inputBits._newBitRange();
    final static int INPUT_0 = inputBits._newBitRange();
    public final static int INPUT_START = 0;
    public final static int INPUT_STOP = INPUT_2;
    public final static int INPUT_WIN_1X = INPUT_1 | INPUT_2;
    public final static int INPUT_WIN_2X = INPUT_1;
    public final static int INPUT_DRAW_1X = INPUT_0 | INPUT_1;
    public final static int INPUT_DRAW_2X = INPUT_0 | INPUT_1 | INPUT_2;
    int state = STATE_START;

    public Battle() {
        super("Battle");
    }

    @Override
    public int getState() {
        return state;
    }

    @Override
    protected int nextState(int input) {
        int state = getState();
        int newState = 0;
        if (lookUp(state, STATE_0, false) && lookUp(state, STATE_1, false) && lookUp(state, STATE_2, false) && lookUp(input, INPUT_0, false) && lookUp(input, INPUT_1, true) && lookUp(input, INPUT_2, true) || lookUp(state, STATE_0, false) && lookUp(state, STATE_1, false) && lookUp(state, STATE_3, true) && lookUp(input, INPUT_0, false) && lookUp(input, INPUT_1, true) && lookUp(input, INPUT_2, false) || lookUp(state, STATE_0, false) && lookUp(state, STATE_1, false) && lookUp(state, STATE_2, false) && lookUp(input, INPUT_0, true) && lookUp(input, INPUT_1, true) && lookUp(input, INPUT_2, false) || lookUp(state, STATE_0, false) && lookUp(state, STATE_1, false) && lookUp(state, STATE_2, true) && lookUp(input, INPUT_0, true) && lookUp(input, INPUT_1, true) && lookUp(input, INPUT_2, true) || lookUp(state, STATE_0, false) && lookUp(state, STATE_1, false) && lookUp(state, STATE_2, true) && lookUp(state, STATE_3, true) && lookUp(input, INPUT_1, true) || lookUp(state, STATE_0, false) && lookUp(state, STATE_1, true) && lookUp(state, STATE_2, true) && lookUp(state, STATE_3, false) && lookUp(input, INPUT_0, false) && lookUp(input, INPUT_1, false) && lookUp(input, INPUT_2, true) || lookUp(state, STATE_0, true) && lookUp(state, STATE_1, true) && lookUp(state, STATE_2, true) && lookUp(state, STATE_3, false) || lookUp(state, STATE_0, true) && lookUp(state, STATE_1, false) || lookUp(state, STATE_1, true) && lookUp(state, STATE_2, false) && lookUp(state, STATE_3, true) || lookUp(state, STATE_0, false) && lookUp(state, STATE_1, true) && lookUp(state, STATE_2, false) && lookUp(state, STATE_3, false)) {
            newState |= STATE_0;
        }
        if (lookUp(state, STATE_0, false) && lookUp(state, STATE_1, false) && lookUp(input, INPUT_0, false) && lookUp(input, INPUT_1, false) || lookUp(state, STATE_0, false) && lookUp(state, STATE_1, false) && lookUp(state, STATE_3, false) && lookUp(input, INPUT_0, false) && lookUp(input, INPUT_2, true) || lookUp(state, STATE_0, false) && lookUp(state, STATE_1, false) && lookUp(state, STATE_3, true) && lookUp(input, INPUT_0, false) && lookUp(input, INPUT_1, true) && lookUp(input, INPUT_2, false) || lookUp(state, STATE_0, false) && lookUp(state, STATE_1, false) && lookUp(state, STATE_2, false) && lookUp(input, INPUT_0, false) && lookUp(input, INPUT_1, false) && lookUp(input, INPUT_2, true) || lookUp(state, STATE_0, false) && lookUp(state, STATE_1, false) && lookUp(state, STATE_2, true) && lookUp(input, INPUT_0, true) && lookUp(input, INPUT_1, true) && lookUp(input, INPUT_2, true) || lookUp(state, STATE_0, false) && lookUp(state, STATE_2, true) && lookUp(state, STATE_3, false) && lookUp(input, INPUT_1, true) || lookUp(state, STATE_0, false) && lookUp(state, STATE_1, true) && lookUp(state, STATE_2, true) && lookUp(input, INPUT_0, false) && lookUp(input, INPUT_1, false) && lookUp(input, INPUT_2, false) || (lookUp(state, STATE_0, false) && lookUp(state, STATE_2, true) && lookUp(state, STATE_3, false) || lookUp(state, STATE_1, true) && lookUp(state, STATE_2, true) && lookUp(state, STATE_3, true)) && lookUp(input, INPUT_0, false) && lookUp(input, INPUT_1, true) && lookUp(input, INPUT_2, false) || lookUp(state, STATE_0, false) && lookUp(state, STATE_1, true) && lookUp(state, STATE_2, true) && lookUp(state, STATE_3, false) && lookUp(input, INPUT_0, true) && lookUp(input, INPUT_1, false) || lookUp(state, STATE_0, true) && lookUp(state, STATE_1, true) && lookUp(state, STATE_2, true) && lookUp(state, STATE_3, true) || lookUp(state, STATE_1, true) && lookUp(state, STATE_2, true) && lookUp(state, STATE_3, true) && lookUp(input, INPUT_0, false) && lookUp(input, INPUT_1, false) || lookUp(state, STATE_0, true) && lookUp(state, STATE_1, true) && lookUp(state, STATE_2, false) && lookUp(state, STATE_3, false)) {
            newState |= STATE_1;
        }
        if (lookUp(state, STATE_0, false) && lookUp(state, STATE_1, false) && lookUp(state, STATE_2, false) && (lookUp(input, INPUT_0, false) && lookUp(input, INPUT_1, false) || lookUp(input, INPUT_0, false) && lookUp(input, INPUT_2, true)) || lookUp(state, STATE_0, false) && lookUp(state, STATE_1, false) && lookUp(state, STATE_3, true) && lookUp(input, INPUT_0, false) || lookUp(state, STATE_0, false) && lookUp(state, STATE_1, false) && lookUp(state, STATE_2, true) && lookUp(input, INPUT_0, true) && lookUp(input, INPUT_1, false) || lookUp(state, STATE_0, false) && lookUp(state, STATE_1, false) && lookUp(state, STATE_2, true) && lookUp(state, STATE_3, false) || lookUp(state, STATE_0, false) && lookUp(state, STATE_1, true) && lookUp(state, STATE_2, true) && lookUp(state, STATE_3, false) && (lookUp(input, INPUT_0, false) && lookUp(input, INPUT_1, true) || lookUp(input, INPUT_0, true)) || (lookUp(state, STATE_0, false) && lookUp(state, STATE_2, true) && lookUp(state, STATE_3, false) || lookUp(state, STATE_1, true) && lookUp(state, STATE_2, true) && lookUp(state, STATE_3, true)) && (lookUp(input, INPUT_0, false) && lookUp(input, INPUT_1, true) && lookUp(input, INPUT_2, false) || lookUp(input, INPUT_0, true) && lookUp(input, INPUT_1, true) && lookUp(input, INPUT_2, true) || lookUp(input, INPUT_0, false) && lookUp(input, INPUT_1, false) && lookUp(input, INPUT_2, false)) || lookUp(state, STATE_0, false) && lookUp(state, STATE_1, false) && lookUp(state, STATE_2, true) && lookUp(input, INPUT_0, true) && lookUp(input, INPUT_1, true) && lookUp(input, INPUT_2, false) || lookUp(state, STATE_1, true) && lookUp(state, STATE_2, true) && lookUp(state, STATE_3, true) && lookUp(input, INPUT_0, false) && lookUp(input, INPUT_1, false) || lookUp(state, STATE_0, true) && lookUp(state, STATE_1, true) && lookUp(state, STATE_2, true) && lookUp(state, STATE_3, true) || lookUp(state, STATE_0, true) && lookUp(state, STATE_1, true) && lookUp(state, STATE_2, false) && lookUp(state, STATE_3, false)) {
            newState |= STATE_2;
        }
        if (lookUp(state, STATE_0, false) && lookUp(state, STATE_1, false) && lookUp(state, STATE_2, false) && lookUp(input, INPUT_0, false) && lookUp(input, INPUT_1, true) && lookUp(input, INPUT_2, true) || lookUp(state, STATE_0, false) && lookUp(state, STATE_1, false) && lookUp(state, STATE_2, false) && lookUp(state, STATE_3, true) && lookUp(input, INPUT_0, true) && lookUp(input, INPUT_2, true) || lookUp(state, STATE_0, false) && lookUp(state, STATE_1, false) && lookUp(state, STATE_3, true) && lookUp(input, INPUT_0, true) && lookUp(input, INPUT_1, false) || lookUp(state, STATE_0, false) && lookUp(state, STATE_1, true) && lookUp(state, STATE_2, true) && lookUp(state, STATE_3, false) && lookUp(input, INPUT_0, false) && lookUp(input, INPUT_1, false) && lookUp(input, INPUT_2, false) || lookUp(state, STATE_0, false) && lookUp(state, STATE_1, true) && lookUp(state, STATE_2, true) && lookUp(state, STATE_3, true) && (lookUp(input, INPUT_0, false) && lookUp(input, INPUT_2, true) || lookUp(input, INPUT_1, true) && lookUp(input, INPUT_2, false)) || lookUp(state, STATE_0, true) && lookUp(state, STATE_1, true) && lookUp(state, STATE_2, true) && lookUp(state, STATE_3, true) || lookUp(state, STATE_0, true) && lookUp(state, STATE_1, true) && lookUp(state, STATE_2, false) && lookUp(state, STATE_3, false)) {
            newState |= STATE_3;
        }
        return newState;
    }

    @Override
    protected void setState(int arg0) {
        state = arg0;
    }

    @Override
    public void reset() {
        setState(STATE_START);
    }
}
