/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended;

import java.util.Stack;
import net.sf.jiga.xtended.kernel.JXAenvUtils;
import net.sf.jiga.xtended.kernel.JXAenvUtils.LVL;
import net.sf.jiga.xtended.kernel.ThreadWorks;

/**
 * An Exception thrown by the JigaXtended API when an unexpected (Runtime-)error
 * occured. <br> scan for any JXAException occured if not using RenderingScene
 * with the {@link #_history history set}
 *
 * @author www.b23prodtm.info
 */
public class JXAException extends RuntimeException {

    public static enum LEVEL {

        /** Severe error that occurs at the application level, such as a division by zero, or anyother code error.*/
        APP(LVL.APP_ERR), 
        /** Common error that occurs when the user does a wrong action. */
        USER(LVL.USR_ERR), 
        /** Fatal low-level error, like when the system detects an hardware issue or incompatility. */
        SYSTEM(LVL.SYS_ERR);
        int jxaLevel;
        LVL lvl;
        private LEVEL(LVL l) {
            jxaLevel = l.level;
            lvl = l;
        }
    }
    /**
     * all reported and unreported JXAException (errors) that occured in the
     * current runtime instance
     */
    public final static Stack<JXAException> _history = new Stack<JXAException>();

    /**
     * Creates a new instance of
     * <code>JXAException</code> without detail message.
     */
    public JXAException() {
        super();
        _history.push(this);
    }

    /**
     * Creates a new instance of
     * <code>JXAException</code> without detail message.
     */
    public JXAException(Throwable tCause) {
        this("A JXAException was invoked by : " + tCause, tCause);
    }

    /**
     * Constructs an instance of
     * <code>JXAException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public JXAException(String msg) {
        this(msg, null);
    }

    /**
     * Constructs an instance of
     * <code>JXAException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public JXAException(LEVEL l, String msg) {
        this(JXAenvUtils.log(msg, l.lvl), null);
    }

    /**
     * Constructs an instance of
     * <code>JXAException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public JXAException(String msg, Throwable tCause) {
        super(msg);
        if (tCause != null) {
            initCause(tCause);
        }
        _history.push(this);
        printStackTrace(ThreadWorks.cLogFilePrintStream);
    }
    private long hash = System.nanoTime();

    public int hashCode() {
        return (int) hash;
    }

    public boolean equals(Object o) {
        return o == null ? false : o.hashCode() == hashCode();
    }
}
