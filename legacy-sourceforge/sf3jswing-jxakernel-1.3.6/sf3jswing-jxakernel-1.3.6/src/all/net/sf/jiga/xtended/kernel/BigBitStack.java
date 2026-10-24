/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.kernel;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/** Extension of BitStack. Because the BigInteger class is backing this implementation, theorically an unlimited amount of {@linkplain #_newBit(BigInteger) values} can be stored in the BigBitStack.
 *This class provides support for bitmap comparison, e.g. :
 * compare against a CONSTANT_BIT existance in a set myValue : <pre>
 * final int CONSTANT1_BIT = _newBitRange(), CONSTANT2_BIT = _newBitRange();
 * ( ! myValue.and(_getAllBits()).and(CONSTANT_BIT).equals(BigInteger.ZERO) )</pre> or
 * remove a CONSTANT1_BIT and a CONSTANT2_BIT from a set myValue :
 * <pre>
 * myValue = myValue.andNot( _getAllBits().and( (CONSTANT1_BIT.or(CONSTANT2_BIT)) ) )</pre>
 * etc.
 * @author www.b23prodtm.info
 */
public class BigBitStack {

    /**
     * All bits bitmap
     */
    protected final Map<BigInteger, BigInteger> _ALLBITS = Collections.synchronizedMap(new HashMap<BigInteger, BigInteger>());
    private int _ALLBITSstack;

    /**Creates a new bitStack instance w/ offset at 1*/
    public BigBitStack() {
        this(BigInteger.ONE);
    }

    private static BigInteger _TWO = BigInteger.valueOf(2);
    /**offset the stack to prevent using a range of values that may be used for other purposes.
     * The first generated bit will equals the first pow-of-two greater than or equal to offset.
     * @param offset a positive integer as offset for the stack; e.g. 500 will generate bits starting at 512.
     */
    public BigBitStack(BigInteger offset) {
        _ALLBITSstack = 0;
        while (offset.compareTo(_TWO.pow(_ALLBITSstack + 1)) > 0) {
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
    public final BigInteger _newBitRange() {
        /*assert !isFull() : "overflowing the Integer limit";*/
        BigInteger newBit = _TWO.pow(_ALLBITSstack++);
        _ALLBITSRANGES = _ALLBITSRANGES.or(newBit);
        return newBit;
    }

    /*public boolean isFull() {
    return (double) Integer.MAX_VALUE / Math.pow(2, _ALLBITSstack - 1) < 2;
    }*/
    
    /** returns a new bit associated to the specified range. That is a bitwise-OR combination of the new bit and the range.
     * @param range the range {@linkplain #_newBitRange()} that will group this new bit with (can be 0)
     * @return the bitwise-OR combination of the new bit and its associated range.
      */
    public final BigInteger _newBit(BigInteger range) {
        /*assert !isFull() : "overflowing the Integer limit";*/
        if (!_ALLBITSRANGES.and(range).equals(range)) {
            throw new IllegalArgumentException("invalid supplied range");
        }
        BigInteger newBit = _TWO.pow(_ALLBITSstack++);
        if (!_ALLBITS.containsKey(range)) {
            _ALLBITS.put(range, BigInteger.ZERO);
        }
        BigInteger rangeMask = _ALLBITS.get(range).or(newBit);
        _ALLBITS.put(range, rangeMask);
        _ALLBITSNORANGE = _ALLBITSNORANGE.or(newBit);
        return range.or(newBit);
    }
    private BigInteger _ALLBITSRANGES = BigInteger.ZERO;

    /**
     * returns all stored ranges values, OR-ed.
     * @return all stored ranges
     */
    public final BigInteger _getAllBitRanges() {
        return _ALLBITSRANGES;
    }
    private BigInteger _ALLBITSNORANGE = BigInteger.ZERO;

    /**
     * returns all bits excepting the range bits, OR-ed.
     * @return all bits excepting the range bits
     */
    public final BigInteger _getAllBits() {
        return _ALLBITSNORANGE;
    }

        /**
     * returns the bits field associated to the specified range. Bitmask is all bits contained in the specified range bits, OR-ed.
     * @param forRange the specified range of bits can be a bitwise-OR combination of range bits, as well.
     * @return the bits field associated to the specified range of bits, OR-ed.
        @see #_newBit(BigInteger)
     * @see #_getNotMask(BigInteger)
     */
    public final BigInteger _getMask(BigInteger forRange) {
        BigInteger mask = BigInteger.ZERO;
        synchronized (_ALLBITS) {
            for (BigInteger range : _ALLBITS.keySet()) {
                if (!forRange.and(range).equals(BigInteger.ZERO)) {
                    mask = mask.or(_ALLBITS.get(range));
                }
            }
        }
        return mask;
    }

    /**
     * returns all bits that are NOT associated to the specified range.
     * @param forRange the specified range bits for computing the NOT mask, OR-ed.
     * @return the NOT-bitmask associated to the specified range, OR-ed.
     */
    public final BigInteger _getNotMask(BigInteger forRange) {
        BigInteger mask = BigInteger.ZERO;
        synchronized (_ALLBITS) {
            for (BigInteger range : _ALLBITS.keySet()) {
                if (!forRange.and(range).equals(BigInteger.ZERO)) {
                    mask = mask.or(_ALLBITS.get(range));
                }
            }
        }
        return mask;
    }

    /** returns a human-readable "10110" bits-String of this integer.
    @return a human-readable bits-String
    @param integer a -positive- BigInteger
     */
    public static String _toBitsString(BigInteger integer) {
        BigInteger s = integer;
        Vector<Character> sBits = new Vector<Character>();
        while (s.compareTo(BigInteger.ONE) == 1) {
            int n = 0;
            while (_TWO.pow(n).compareTo(s) <= 0) {
                n++;
            }
            n--;
            if (sBits.size() < n + 1) {
                sBits.setSize(n + 1);
            }
            sBits.set(n, '1');
            s = s.subtract(_TWO.pow(n));
        }
        if (s.equals(BigInteger.ONE)) {
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
    @param integer a -positive- BigInteger
    @param fields number of minimum number of fields to display*/
    public static String _toBitsString(BigInteger integer, int fields) {
        String bitsString = _toBitsString(integer);
        for (int i = bitsString.length(); i < fields; i++) {
            bitsString = "0" + bitsString;
        }
        return bitsString;
    }

    static {
        if (JXAenvUtils._debugSys) {
            System.err.println("BigBitStack test 7 bitsString: " + _toBitsString(BigInteger.valueOf(7), 4));
        }
    }
}
