package net.sf.jiga.xtended.kernel;

import java.util.*;

/**
 * Listens to Console instances
 *
 * @see Console
 */
public interface InputLogListener extends EventListener {

    /**
     * usually called by an object that requires to get console messages. This
     * method should return quickly to avoid a too long time locking of the
     * console.
     *
     * @param message the message packet sent
     * @see Console
     */
    public void newLogPacket(String message);
}
