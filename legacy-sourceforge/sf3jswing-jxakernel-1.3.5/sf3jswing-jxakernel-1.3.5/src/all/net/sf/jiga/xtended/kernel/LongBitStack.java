/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.kernel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/** Extension of BitStack. Because the Long class is backing this implementation, theorically an unlimited amount of {@linkplain #_newBit(Long) values} can be stored in the LongBitStack.
 *This class provides support for bitmap comparison, e.g. :
 * compare against a CONSTANT_BIT existance in a set myValue : <pre>
 * ( (myValue & _getAllBits() & CONSTANT_BIT) != 0L )</pre> or
 * remove a CONSTANT1_BIT and a CONSTANT2_BIT from a set myValue :
 * <pre>
 * myValue = myValue & ~( _getAllBits() & (CONSTANT1_BIT | CONSTANT2_BIT) )</pre>
 * etc.
 * @author www.b23prodtm.info
 */
public class LongBitStack {

    /**
     * All bits bitmap
     */
    protected final Map<Long, Long> _ALLBITS = Collections.synchronizedMap(new HashMap<Long, Long>());
    private int _ALLBITSstack;

    /**Creates a new bitStack instance w/ offset at 1*/
    public LongBitStack() {
        this(0L);
    }
    private static Long _TWO = Long.valueOf(2);

    /**offset the stack to prevent using a range of values that may be used for other purposes.
     * The first generated bit will equals the first pow-of-two greater than or equal to offset.
     * @param offset a positive integer as offset for the stack; e.g. 500 will generate bits starting at 512.
     */
    public LongBitStack(Long offset) {
        _ALLBITSstack = 0;
        while (offset.compareTo((long) Math.pow(_TWO, _ALLBITSstack + 1)) > 0) {
            _ALLBITSstack++;
        }
    }

    /** returns the current bit level, that is the next new bit will be
    the returned value + 1
    @return the current bit level*/
    public int getBitsStackCurrentLevel() {
        return _ALLBITSstack - 1;
    }

    /**
     * returns a new bit range.
     * That is a value that can be used to group several constants together.
     * @return a new bit range
     */
    public final Long _newBitRange() {
        /*assert !isFull() : "overflowing the Integer limit";*/
        Long newBit = (long) Math.pow(_TWO, _ALLBITSstack++);
        _ALLBITSRANGES = _ALLBITSRANGES | newBit;
        return newBit;
    }

    /*public boolean isFull() {
    return (double) Integer.MAX_VALUE / Math.pow(2, _ALLBITSstack - 1) < 2;
    }*/
    /** returns a new bit for the specified range. That is a bitwise-OR combination of the new bit and the range.
     * @param range the range {@linkplain #_newBitRange()} that will group this new bit with (can be 0)
     * @return the bitwise-OR combination of the new bit and its specified range.
     */
    public final Long _newBit(Long range) {
        /*assert !isFull() : "overflowing the Integer limit";*/
        if ((_ALLBITSRANGES & range) != range) {
            throw new IllegalArgumentException("invalid supplied range");
        }
        Long newBit = (long) Math.pow(_TWO, _ALLBITSstack++);
        if (!_ALLBITS.containsKey(range)) {
            _ALLBITS.put(range, 0L);
        }
        Long rangeMask = _ALLBITS.get(range) | newBit;
        _ALLBITS.put(range, rangeMask);
        _ALLBITSNORANGE = _ALLBITSNORANGE | newBit;
        return range | newBit;
    }
    private Long _ALLBITSRANGES = 0L;

    /**
     * returns all stored ranges values, OR-ed.
     * @return all stored ranges
     */
    public final Long _getAllBitRanges() {
        return _ALLBITSRANGES;
    }
    private Long _ALLBITSNORANGE = 0L;

    /**
     * returns all bits excepting the range bits, OR-ed.
     * @return all bits excepting the range bits
     */
    public final Long _getAllBits() {
        return _ALLBITSNORANGE;
    }

    /**
     * returns an AND-bitmask for the specified range of bits. Bitmask is all bits contained in the specified range bits, OR-ed.
     * @param forRange the specified range of bits can be a bitwise-OR combination of range bits, as well.
     * @return the AND-bitmask for the specified range of bits, OR-ed.
     * @see #_newBit(Long)
     * @see #_getNotMask(Long)
     */
    public final Long _getMask(Long forRange) {
        Long mask = 0L;
        synchronized (_ALLBITS) {
            for (Long range : _ALLBITS.keySet()) {
                if ((forRange & range) != 0L) {
                    mask = mask | _ALLBITS.get(range);
                }
            }
        }
        return mask;
    }

    /**
     * returns all bits that are NOT for the specified range bits.
     * @param forRange the specified range bits for computing the NOT mask, OR-ed.
     * @return the NOT-bitmask of the specified range bits, OR-ed.
     */
    public final Long _getNotMask(Long forRange) {
        Long mask = 0L;
        synchronized (_ALLBITS) {
            for (Long range : _ALLBITS.keySet()) {
                if ((forRange & range) != 0L) {
                    mask = mask | _ALLBITS.get(range);
                }
            }
        }
        return mask;
    }

    /** returns a human-readable "10110" bits-String of this integer.
    @return a human-readable bits-String
    @param integer a -positive- Long
     */
    public static String _toBitsString(Long integer) {
        Long s = integer;
        Vector<Character> sBits = new Vector<Character>();
        while (s.compareTo(1L) == 1) {
            int n = 0;
            while (((Long) ((long) Math.pow(_TWO, n))).compareTo(s) <= 0) {
                n++;
            }
            n--;
            if (sBits.size() < n + 1) {
                sBits.setSize(n + 1);
            }
            sBits.set(n, '1');
            s = s - (long) Math.pow(_TWO, n);
        }
        if (s.equals(1L)) {
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

    /** returns a human-readable "10110" bits-String of this integer.
    @return a human-readable bits-String
    @param integer a -positive- Long
    @param fields number of minimum number of fields to display*/
    public static String _toBitsString(Long integer, int fields) {
        String bitsString = _toBitsString(integer);
        for (int i = bitsString.length(); i < fields; i++) {
            bitsString = "0" + bitsString;
        }
        return bitsString;
    }

    static {
        if (JXAenvUtils._debugSys) {
            System.err.println("BigBitStack test 7 bitsString: " + _toBitsString(Long.valueOf(7), 4));
        }
    }
}
