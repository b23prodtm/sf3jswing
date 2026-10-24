package net.sf.jiga.xtended.kernel;


public class LongLevel {

    public static LongLevel NoDebug = new LongLevel(0L);
    long n;

    protected LongLevel(long levelInt) {
        this.n = levelInt;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof LongLevel ? n == ((LongLevel) obj).n : false;
    }

    @Override
    public int hashCode() {
        return ("" + n).hashCode();
    }

    public LongLevel or(LongLevel l) {
        return new LongLevel(n | l.n);
    }

    public LongLevel and(LongLevel l) {
        return new LongLevel(n & l.n);
    }

    public LongLevel andNot(LongLevel l) {
        return new LongLevel(n & ~l.n);
    }

    public LongLevel not() {
        return new LongLevel(~n);
    }
}
