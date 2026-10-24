/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.system;

import net.sf.jiga.xtended.JXAException;

/**
 * To handle code blocks, define the starting line with {@link #begin()} and
 * stop it (on the same Thread) {@link #end()}. check if between begin-end block
 * with {@link #isOn()}.
 *
 * @author www.b23prodtm.info
 */
public class ThreadBlock {

    private ThreadLocal<Integer> _isBlock = new ThreadLocal<Integer>() {

        @Override
        protected Integer initialValue() {
            return 0;
        }
    };
    
    
    /**
     * creates a threadblock handler. best use as a static object.
     */
    public ThreadBlock() {
    }

    /**
     * creates a threadblock handler. best use as a static object. (debug
     * feature)
     *
     * @param alwaysOn will default the block to on if true, which will always
     * return on (debug feature).
     */
    public ThreadBlock(boolean alwaysOn) {
        if (alwaysOn) {
            _isBlock.set(1);
        }
    }

    /**
     * threadlocal state
     *
     * @see #begin()
     */
    public boolean isOn() {
        return _isBlock.get() != 0;
    }

    /**
     * threadlocal state <br>any encaspulated {@link #_GLpushRender(java.lang.String, java.lang.Object, java.lang.Object[], java.lang.Class[])}
     * will render immediately till the next {@link #end()}
     *
     * @see #end()
     */
    public void begin() {
        _isBlock.set(_isBlock.get() + 1);
    }

    /**
     * threadlocal state
     *
     * @see #begin()
     */
    public void end() {
        _isBlock.set(_isBlock.get() - 1);
        if (_isBlock.get() < 0) {
            throw new JXAException(JXAException.LEVEL.SYSTEM, "end threadblock which has not begun; it maybe another thread that is using it.");
        }
    }
}
