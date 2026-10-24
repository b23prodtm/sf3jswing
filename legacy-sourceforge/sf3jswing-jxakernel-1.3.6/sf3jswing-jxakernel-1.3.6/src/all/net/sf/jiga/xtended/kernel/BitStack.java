/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.kernel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * This class provides support for bitmap comparison, e.g. : compare against a
 * CONSTANT_BIT existance in a set myValue :
 * <pre>
 * final int CONSTANT1_BIT = _newBitRange(), CONSTANT2_BIT = _newBitRange();
 * (myValue & _getAllBits() & CONSTANT_BIT) != 0</pre> or remove a CONSTANT1_BIT
 * and a CONSTANT2_BIT from a set myValue :
 * <pre>
 * myValue &= ~(_getAllBits() & (CONSTANT1_BIT | CONSTANT2_BIT))</pre> etc. It
 * uses primitive "int" for storage of constants and hence it is limited to 31
 * constants per instance of this class (i.e. 32 bits int are -+(2pow31 - 1)
 * (see WikiPedia about "complement of two's notation").
 *
 * @TODO : long bitmaps
 * @author www.b23prodtm.info
 * @see Sprite.gfx
 * @see http://docs.oracle.com/javase/tutorial/java/nutsandbolts/opsummary.html
 */
public class BitStack {

    /**
     * All bits bitmap
     */
    protected final Map<Integer, Integer> _ALLBITS = Collections.synchronizedMap(new HashMap<Integer, Integer>());
    private int _ALLBITSstack;

    /**
     * Creates a new bitStack instance w/ offset at 1
     */
    public BitStack() {
        this(1);
    }

    /**
     * offset the stack to prevent using a range of values that may be used for
     * other purposes. The first generated bit will equals the first pow-of-two
     * greater than or equal to offset.
     *
     * @param offset a positive integer as offset for the stack; e.g. 500 will
     * generate bits starting at 512.
     */
    public BitStack(int offset) {
        _ALLBITSstack = 0;
        while (Math.pow(2, _ALLBITSstack + 1) < offset) {
            _ALLBITSstack++;
        }
    }

    /**
     * returns the current bit level, that is the next new bit will be the
     * returned value + 1
     *
     * @return the current bit level
     */
    public int getBitsStackCurrentLevel() {
        return _ALLBITSstack - 1;
    }

    /**
     * returns a new bit range. That is a value that can be used to group
     * several constants together.
     *
     * @return a new bit range
     */
    public final int _newBitRange() {
        assert !isFull() : "overflowing the Integer limit";
        int newBit = (int) Math.pow(2, _ALLBITSstack++);
        _ALLBITSRANGES |= newBit;
        return newBit;
    }

    public boolean isFull() {
        return (double) Integer.MAX_VALUE / Math.pow(2, _ALLBITSstack - 1) < 2;
    }

    /**
     * returns a new bit associated to the specified range. That is a bitwise-OR
     * combination of the new bit and the range.
     *
     * @param range the range {@linkplain #_newBitRange()} that will group this
     * new bit with (can be 0)
     * @return the bitwise-OR combination of the new bit and its associated
     * range.
     */
    public final int _newBit(int range) {
        assert !isFull() : "overflowing the Integer limit";
        if ((_ALLBITSRANGES & range) != range) {
            throw new IllegalArgumentException("invalid supplied range");
        }
        int newBit = (int) Math.pow(2, _ALLBITSstack++);
        if (!_ALLBITS.containsKey(range)) {
            _ALLBITS.put(range, 0);
        }
        int rangeMask = _ALLBITS.get(range) | newBit;
        _ALLBITS.put(range, rangeMask);
        _ALLBITSNORANGE |= newBit;
        return range | newBit;
    }
    private int _ALLBITSRANGES = 0;

    /**
     * returns all stored ranges values, OR-ed.
     *
     * @return all stored ranges
     */
    public final int _getAllBitRanges() {
        return (int) _ALLBITSRANGES;
    }
    private int _ALLBITSNORANGE = 0;

    /**
     * returns all bits excepting the range bits, OR-ed.
     *
     * @return all bits excepting the range bits
     */
    public final int _getAllBits() {
        return (int) _ALLBITSNORANGE;
    }

    /**
     * returns the bits field associated to the specified range. Bitmask is all
     * bits contained in the specified range bits, OR-ed.
     *
     * @param forRange the specified range of bits can be a bitwise-OR
     * combination of range bits, as well.
     * @return the bits field associated to the specified range of bits, OR-ed.
     * @see #_newBit(int)
     * @see #_getNotMask(int)
     */
    public final int _getMask(int forRange) {
        int mask = 0;
        synchronized (_ALLBITS) {
            for (int range : _ALLBITS.keySet()) {
                if ((forRange & range) != 0) {
                    mask |= _ALLBITS.get(range);
                }
            }
        }
        return mask;
    }

    /**
     * returns all bits that are NOT associated to the specified range.
     *
     * @param forRange the specified range bits for computing the NOT mask,
     * OR-ed.
     * @return the NOT-bitmask associated to the specified range, OR-ed.
     */
    public final int _getNotMask(int forRange) {
        int mask = 0;
        synchronized (_ALLBITS) {
            for (int range : _ALLBITS.keySet()) {
                if ((forRange & range) == 0) {
                    mask |= _ALLBITS.get(range);
                }
            }
        }
        return mask;
    }

    /**
     * returns a human-readable "10110" bits-String of this integer.
     *
     * @return a human-readable bits-String
     * @param integer a -positive- int
     */
    public static String _toBitsString(int integer) {
        double s = integer;
        Vector<Character> sBits = new Vector<Character>();
        while (s > 1) {
            int n = 0;
            while (Math.pow(2, n) <= s) {
                n++;
            }
            n--;
            if (sBits.size() < n + 1) {
                sBits.setSize(n + 1);
            }
            sBits.set(n, '1');
            s -= Math.pow(2, n);
        }
        if (s == 1) {
            if (sBits.isEmpty()) {
                sBits.add('1');
            } else {
                sBits.set(0, '1');
            }
        }
        String bitsString = "";
        for (int i = 0; i < sBits.size(); i++) {
            bitsString = (sBits.get(i) == null ? "0" : sBits.get(i)) + bitsString;
        }
        return bitsString;
    }

    /**
     * returns a human-readable "10110" bits-String of this integer.
     *
     * @return a human-readable bits-String
     * @param integer a -positive- int
     * @param fields number of minimum number of fields to display (remember
     * maximum of 32 with integers)
     */
    public static String _toBitsString(int integer, int fields) {
        String bitsString = _toBitsString(integer);
        for (int i = bitsString.length(); i < fields; i++) {
            bitsString = "0" + bitsString;
        }
        return bitsString;
    }

    static {
        if (JXAenvUtils._debugSys) {
            System.err.println("bitstack test offset 7."
                    + "\n bitsString: " + _toBitsString(7, 4));
            new BitStack(7)._newBitRange();
        }
    }
}
