/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.system;

import net.sf.jiga.xtended.kernel.BitStack;
import net.sf.jiga.xtended.kernel.JXAenvUtils;

/**
 * This is a class to produce small logical algorithms, such as automation or small AI systems.
 * {@linkplain #reset()} should be called once before to send {@linkplain #newInput(int)}'s.
 * @author www.b23prodtm.info
 */
public abstract class LogicalSystem {

    /** a name for this logical algorithm*/
    protected String name;
    /** hash */
    long hash = System.nanoTime();

    @Override
    public int hashCode() {
        return (int) hash;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == null ? false : obj.hashCode() == hashCode();
    }

    /** creates a new instance.
    @param name a name for this logical algorithm*/
    public LogicalSystem(String name) {
        this.name = name;
    }

    /** Use this method to compute the next state !
    @param input eventually add an input */
    public final int newInput(int input) {
        if (JXAenvUtils._debug) {
            System.out.println(JXAenvUtils.log("NEW INPUT (" + input + ") " + BitStack._toBitsString(input) + " for " + toString(),JXAenvUtils.LVL.APP_NOT));
        }
        setState(nextState(input));
        if (JXAenvUtils._debug) {
            System.out.println(JXAenvUtils.log("NEW STATE for " + toString(), JXAenvUtils.LVL.APP_NOT));
        }
        return getState();
    }

    /** this method must return the current state value of the LogicalSystem
    @return the current state of this LogicalSystem*/
    public abstract int getState();

    /** this method must be used to check availability of a value in a given set of bits.
    @return true or false, whether the "forValue" argument is available in the "bits" arguments or not, resp.
    @param bits the set of bits to check
    @param forValue the value to check for availability
    @param on if true, checks if forValue is on; if false, checks if it is off (e.g.
     * <pre>int STATE_0 = 0x1; STATE_1 = 0x2; STATE_2 = 0x4;
     * state = STATE_0 | STATE_1;
     * lookUp(state, STATE_0, true); // returns true
     * lookUp(state, STATE_1, false); // returns false
     * lookUp(state, STATE_2, false); // returns true
     * </pre>)
     */
    protected final boolean lookUp(int bits, int forValue, boolean on) {
        return on ? (bits & forValue) != 0 : (bits & forValue) == 0;
    }

    /** this method must compute the next state of this LogicalSystem for the given input.
     * It is called by {@linkplain #newInput(int)}.
    @param input the eventual input value */
    protected abstract int nextState(final int input);

    /** this method must apply the specified state to this LogicalSystem.
     * it is called each time {@linkplain #newInput(int)} is called by the user.
    @param state the state value to apply.*/
    protected abstract void setState(final int state);

    /**this method should be used when the LogicalSystem fails or needs to be set to its original state.*/
    public abstract void reset();

    /** this method returns a human-readable String of the name and current state (as int and "010110" bits string) of this LogicalSystem.
    @return a human readable String for this LogicalSystem*/
    @Override
    public String toString() {
        int s = getState();
        return getClass().getSimpleName() + " " + name + " :  state (" + s + ")" + BitStack._toBitsString(s);
    }
}