package net.sf.jiga.xtended.kernel;

import java.math.BigInteger;

public class Level {

    public static Level NoDebug = new Level(BigInteger.ZERO);
    BigInteger n;

    protected Level(BigInteger levelInt) {
        this.n = levelInt;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Level ? n.equals(((Level) obj).n) : false;
    }

    @Override
    public int hashCode() {
        return n.hashCode();
    }

    public Level or(Level l) {
        return new Level(n.or(l.n));
    }

    public Level and(Level l) {
        return new Level(n.and(l.n));
    }

    public Level andNot(Level l) {
        return new Level(n.andNot(l.n));
    }

    public Level not() {
        return new Level(n.not());
    }
}
