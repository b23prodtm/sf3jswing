/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.jiga.xtended.kernel.JXAenvUtils;

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
        APP(JXAenvUtils.APP_ERROR), 
        /** Common error that occurs when the user does a wrong action. */
        USER(JXAenvUtils.USER_ERROR), 
        /** Fatal low-level error, like when the system detects an hardware issue or incompatility. */
        SYSTEM(JXAenvUtils.SYS_ERROR);
        int jxaLevel;

        private LEVEL(int l) {
            jxaLevel = l;
        }
    }
    
    /**
     * Creates a new instance of
     * <code>JXAException</code> without detail message.
     */
    public JXAException() {
        super();
        Logger.getLogger(JXAenvUtils.LOGGER_NAME).log(Level.SEVERE, getMessage(), getCause());
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
         * @param l severity of this error
     * @param msg the detail message.
     */
    public JXAException(LEVEL l, String msg) {
        this(msg, new Throwable("Severity : " + l));
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
        Logger.getLogger(JXAenvUtils.LOGGER_NAME).log(Level.ALL, msg, tCause);
    }
    private long hash = System.nanoTime();

    @Override
    public int hashCode() {
        return (int) hash;
    }

    @Override
    public boolean equals(Object o) {
        return o == null ? false : o.hashCode() == hashCode();
    }
}
