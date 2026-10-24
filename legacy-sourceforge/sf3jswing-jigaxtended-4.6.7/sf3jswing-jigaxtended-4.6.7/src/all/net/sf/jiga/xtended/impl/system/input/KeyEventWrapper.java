/*
 * KeyEventWrapper.java
 *
 * Created on 30 mai 2007, 05:09
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.system.input;

import java.awt.event.KeyEvent;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import net.sf.jiga.xtended.kernel.BitStack;
import net.sf.jiga.xtended.kernel.JXAenvUtils;

/**
 *The KeyEventWrapper class is intended to simplify the readeability and comparison between distinct KeyEvent's or key strokes. several instances of this class can be naturally sorted in a specific Collection.
 * @author www.b23prodtm.info
 */
public class KeyEventWrapper implements Externalizable, Comparable {

    /** array of BitStack's for all the different keys strokes
    to represent a sequence of upto (32 - 4 stack bits - (5 various key pressings and 3 ranges)) * 10 stacks = 200 different keys */
    protected static BitStack[] maskBits = new BitStack[10];
    /** Map of keyCodes to masks  */
    protected static Map<Integer, Integer> masksMap = Collections.synchronizedMap(new HashMap<Integer, Integer>(32 * maskBits.length));
    /** Map of masks to keyCodes */
    protected static Map<Integer, Integer> codesMap = Collections.synchronizedMap(new HashMap<Integer, Integer>(32 * maskBits.length));
    /** timing bits*/
    protected static int _TIMING_bits;
    /** the default timing for a key stroke when it is pressed [ms] */
    protected static int TIMING_DEFAULT;
    /** the timing of a 2-sec-key-pressing */
    protected static int TIMING_2SEC;
    /** state bits*/
    protected static int _STATE_bits;
    /***/
    protected static int STATE_PRESSED;
    /***/
    protected static int STATE_TYPED;
    /***/
    protected static int STATE_RELEASED;
    /** virtual keyboard keys bits*/
    protected static int _VK_bits;

    /** each bitstack offset is guaranteed to be greater than maskBits.length,
     * so that bitstack values won't overlap either if we add "i" to them 
     * (bitstack values = 2^n > offset - 2 = max(i) = maskBits.length - 1).
     * @see #_makeKeyCodeMask()
     */
    static {
        for (int i = 0; i < maskBits.length; i++) {
            maskBits[i] = new BitStack(maskBits.length + 1) {

                /** this controls for 2^n (n=level) if greater than allowed max if we 
                want to add a i=maskBits.length to identify each value*/
                @Override
                public boolean isFull() {
                    return Integer.MAX_VALUE - maskBits.length < Math.pow(2, getBitsStackCurrentLevel() + 1);
                }
            };
            _TIMING_bits = maskBits[i]._newBitRange();
            TIMING_DEFAULT = maskBits[i]._newBit(_TIMING_bits);
            TIMING_2SEC = maskBits[i]._newBit(_TIMING_bits);
            _STATE_bits = maskBits[i]._newBitRange();
            STATE_PRESSED = maskBits[i]._newBit(_STATE_bits);
            STATE_TYPED = maskBits[i]._newBit(_STATE_bits);
            STATE_RELEASED = maskBits[i]._newBit(_STATE_bits);
            _VK_bits = maskBits[i]._newBitRange();
        }
    }

    /***/
    protected static int _allMasks(int maskType) {
        int mask = 0;
        for (int i = 0; i < maskBits.length; i++) {
            BitStack stack = maskBits[i];
            mask |= stack._getMask(maskType);
        }
        return mask;
    }

    /***/
    protected static int _allMasksRanges(int stack) {
        return maskBits[stack]._getAllBitRanges();
    }

    /** provide a unique bit for the specified keyCode and corresponding state and timing bits
    @param keyCode a valid KeyEvent keycode
    @param ID a valid KeyEvent ID
    @param time a time in ms that indicates how long the key was used*/
    protected static int _makeKeycodeMask(int keyCode, int ID, long time) throws Exception {
        int mask = 0;
        if (masksMap.containsKey(keyCode)) {
            mask = masksMap.get(keyCode);
        } else {
            for (int i = 0; i < maskBits.length; i++) {
                BitStack stack = maskBits[i];
                if (!stack.isFull()) {
                    if (JXAenvUtils._debug) {
                        System.err.println("adding new keycode " + keyCode + " to stack " + i);
                    }
                    masksMap.put(keyCode, mask = i + stack._newBit(_VK_bits));
                    codesMap.put(mask, keyCode);
                    break;
                }
            }
        }
        if (mask == 0) {
            throw new Exception("bits are full !");
        }
        switch (ID) {
            case KeyEvent.KEY_PRESSED:
                mask |= STATE_PRESSED;
                break;
            case KeyEvent.KEY_RELEASED:
                mask |= STATE_RELEASED;
                break;
            case KeyEvent.KEY_TYPED:
                mask |= STATE_TYPED;
                break;
            default:
                break;
        }
        if (time >= 2000) {
            mask |= TIMING_2SEC;
        } else {
            mask |= TIMING_DEFAULT;
        }
        return mask;
    }
    /** mask array */
    protected int[] mask;
    private boolean consumed = false;

    public void consume() {
        consumed = true;
        synchronized (_sequence) {
            for (KeyEventWrapper kew : _sequence) {
                kew.consume();
            }
        }
    }

    public void consumeReset() {
        consumed = false;
        synchronized (_sequence) {
            for (KeyEventWrapper kew : _sequence) {
                kew.consumeReset();
            }
        }
    }

    public boolean isConsumed() {
        if (_sequence.isEmpty()) {
            return consumed;
        } else {
            consumed = true;
        }
        synchronized (_sequence) {
            for (KeyEventWrapper kew : _sequence) {
                consumed = kew.isConsumed() && consumed;
            }
        }
        return consumed;
    }
    protected long when = 0L;
    /** the selected timing for a pressed key stroke [ms]
     */
    protected long _timing;
    /** the key-code for the wrapped KeyEvent or key stroke 
     * @see KeyEvent#getKeyCode()     */
    protected int _keyCode;
    /** the id for the wrapped KeyEvent
     * @see KeyEvent#getID()*/
    protected int _ID;
    /** the sequence of KeyEvent's that this KeyEventWrapper wraps */
    protected List<KeyEventWrapper> _sequence = Collections.synchronizedList(new ArrayList<KeyEventWrapper>());

    /** creates a new instance
     * @param when this millis defines the time when the event was caught up
     * @param keyCode the key-code that identifies the KeyEvent or key stroke
     * @param ID the id that identifies the KeyEvent
     * @see #_keyCode
     * @see #_ID*/
    public KeyEventWrapper(long when, int keyCode, int ID) {
        this(when, keyCode, ID, 100);
    }

    /** creates a new instance 
     * @param when this millis defines the time when the event was caught up
     * @param keyCode the key.code that identifies the KeyEvent or key stroke
     * @param ID the id that identifies the KeyEvent
     * @param timingMin the associated timing for this KeyEventWrapper [ms]
    @see #_keyCode
     * @see #_ID
     * @see #_timing*/
    public KeyEventWrapper(long when, int keyCode, int ID, long timingMin) {
        this._keyCode = keyCode;
        this._ID = ID;
        this._timing = timingMin;
        this.when = when;
        try {
            mask = new int[]{_makeKeycodeMask(keyCode, ID, _timing)};
            hash = BitStack._toBitsString(mask[0], 32).hashCode();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public KeyEventWrapper() {
        this(Collections.synchronizedList(new ArrayList<KeyEventWrapper>()));
    }

    /** creates a new instance
     * @param sequence the sequence of KeyEventWrapper's that will be wrapped and consumed state is &&-computed from the actual states of the specified list*/
    public KeyEventWrapper(List<KeyEventWrapper> sequence) {
        this._keyCode = -1;
        this._ID = -1;
        this._sequence = sequence;
        this._timing = 0;
        consumed = true;
        mask = new int[_sequence.size()];
        String hashStr = "";
        synchronized (_sequence) {
            int i = 0;
            for (Iterator<KeyEventWrapper> it = _sequence.iterator(); it.hasNext(); i++) {
                KeyEventWrapper kew = it.next();
                _timing += kew._timing;
                consumed = consumed && kew.isConsumed();
                mask[i] = kew.mask[0];
                hashStr += BitStack._toBitsString(mask[i], 32);
            }
        }
        hash = hashStr.hashCode();
    }

    /** returns true or false whether this KeyEventWrapper instance is a blank event or not, resp.
     * @return true or false*/
    public boolean isBlankEvent() {
        return (_ID == -1 && _keyCode == -1 && _sequence.isEmpty());
    }
    private int hash;

    /** returns the hash code that identifies this KeyEventWrapper instance
     * @return the hash code taht identifies this KeyEventWrapper instance*/
    public int hashCode() {
        return hash;
    }

    /** returns true or false whether the specified object is equal to this KeyEventWrapper instance. 2 distinct KeyEventWrapper instances are known to be equal if the {@link #compareTo(Object) compareTo} method returns 0.
     * @return true or false*/
    public boolean equals(Object o) {
        return o == null ? false : o.hashCode() == hashCode();
    }

    public int compareTo(Object o) {
        if (JXAenvUtils._debug) {
            System.out.println("KEW comparing " + this + " to " + o);
        }
        int ret = 1;
        if (o == null) {
            throw new NullPointerException("KeyEventWrapper cannot be compared to a null object !");
        }
        if (o instanceof KeyEvent) {
            KeyEvent e = (KeyEvent) o;
            long t = System.currentTimeMillis() - e.getWhen();
            ret = (e.getID() == _ID && e.getKeyCode() == _keyCode) ? ((t == _timing) ? 0 : ((t < _timing) ? 1 : -1)) : 1;
        } else if (o instanceof KeyEventWrapper) {
            KeyEventWrapper e = (KeyEventWrapper) o;
            for (int i = 0; i < mask.length && i < e.mask.length; i++) {
                int mBitsStack = mask[i] & ~_allMasks(_STATE_bits | _TIMING_bits | _VK_bits) & ~_allMasksRanges(0);
                int emBitsStack = e.mask[i] & ~_allMasks(_STATE_bits | _TIMING_bits | _VK_bits) & ~_allMasksRanges(0);
                if (mBitsStack == emBitsStack) {
                    /** OK SAME STACK*/
                    int mk, emk;
                    mk = mask[i] - mBitsStack;
                    emk = e.mask[i] - emBitsStack;
                    if (mk == emk) {
                        /** SAME CODE */
                        ret = 0;
                    } else if ((mk & ~_allMasks(_TIMING_bits)) == (emk & ~_allMasks(_TIMING_bits))) {
                        /** SAME KEY BUT TIMING NOT EQUAL*/
                        if ((mk & _allMasks(_TIMING_bits) & TIMING_2SEC) == 0 && (emk & _allMasks(_TIMING_bits) & TIMING_2SEC) != 0) {
                            /** mk is not 2sec but emk is*/
                            ret = -1;
                        } else {
                            ret = 1;
                        }
                    } else {
                        if (mask.length > e.mask.length) {
                            ret = 1;
                        } else if (mask.length < e.mask.length) {
                            ret = -1;
                        } else {
                            /** compare keycodes integer values*/
                            if (JXAenvUtils._debug) {
                                System.err.print("comparing keycode " + (mk & _allMasks(_VK_bits)) + " from stack " + mBitsStack);
                            }
                            int keyCode = codesMap.get(mBitsStack | _VK_bits | mk & _allMasks(_VK_bits));
                            if (JXAenvUtils._debug) {
                                System.err.println("to keycode " + (emk & _allMasks(_VK_bits)) + " from stack " + emBitsStack);
                            }
                            int eKeyCode = codesMap.get(emBitsStack | _VK_bits | emk & _allMasks(_VK_bits));
                            if (keyCode / eKeyCode > 1) {
                                ret = 1;
                            } else {
                                ret = -1;
                            }
                        }
                    }
                }
                if (ret == 1 || ret == -1) {
                    break;
                }
            }
        }
        return ret;
    }

    public int get_ID() {
        return _ID;
    }

    public int get_keyCode() {
        return _keyCode;
    }

    public long get_timing() {
        return _timing;
    }

    /** What is returned is a COPY of the sequence list, any change made it cannot affect the base sequence */
    public List<KeyEventWrapper> get_sequence() {
        return new Vector(_sequence);
    }

    /** converts this KeyEventWrapper instance into a comprehensive String
     * @return this KeyEventWrapper instance converted into a comprehensive String*/
    public String toString() {
        String s = _sequence.isEmpty() ? "no sequence " : "has sequence ";
        synchronized (_sequence) {
            for (Iterator<KeyEventWrapper> i = _sequence.iterator(); i.hasNext();) {
                KeyEventWrapper kew = i.next();
                s += kew + " - ";
            }
        }
        return getClass().getSimpleName()
                + ((_keyCode != -1) ? KeyEvent.getKeyText(_keyCode)
                + ((_ID == KeyEvent.KEY_PRESSED) ? "PRESSED" : ((_ID == KeyEvent.KEY_RELEASED) ? "RELEASED" : ((_ID == KeyEvent.KEY_TYPED) ? "TYPED" : "")))
                + " sec." + new Formatter().format("%1$.3f", (float) _timing / 1000f) : "") + " @" + when + " " + (when > 0 ? String.format("%1$tS sec. ago", System.currentTimeMillis() - when) : "") + " { " + s + "}";
    }

    /** reverse order KeyEventWrapper (a new instance)*/
    public KeyEventWrapper getReverse() {
        List<KeyEventWrapper> v = get_sequence();
        if (v.size() > 1) {
            Collections.reverse(v);
            return new KeyEventWrapper(v);
        } else {
            return new KeyEventWrapper(when, _keyCode, _ID, _timing);
        }
    }

    /** @return when the event occured */
    public long get_when() {
        return when;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        int currentPty = Thread.currentThread().getPriority();
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        out.writeInt(_ID);
        out.writeInt(_keyCode);
        synchronized (_sequence) {
            out.writeInt(_sequence.size());
            for (Iterator<KeyEventWrapper> it = _sequence.iterator(); it.hasNext();) {
                out.writeObject(it.next());
            }
        }
        out.writeLong(_timing);
        out.writeBoolean(consumed);
        out.writeInt(hash);
        out.writeObject(mask);
        out.writeLong(when);
        out.flush();
        Thread.currentThread().setPriority(currentPty);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int currentPty = Thread.currentThread().getPriority();
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        _ID = in.readInt();
        _keyCode = in.readInt();
        int s = in.readInt();
        for (int i = 0; i < s; i++) {
            _sequence.add((KeyEventWrapper) in.readObject());
        }
        _timing = in.readLong();
        consumed = in.readBoolean();
        hash = in.readInt();
        mask = (int[]) in.readObject();
        when = in.readLong();
        Thread.currentThread().setPriority(currentPty);
    }
}
