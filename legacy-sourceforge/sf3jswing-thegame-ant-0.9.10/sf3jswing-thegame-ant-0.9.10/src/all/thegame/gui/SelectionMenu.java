/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thegame.gui;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;

/**
 * SelectionMenuComponents handler. it also dispatches keyEvent's to the current SelectionMenuComponent's.
 * @author tianabruno
 */
public class SelectionMenu implements KeyEventDispatcher {

    SelectionMenuComponent current;

    public SelectionMenu(SelectionMenuComponent current) {
        this.current = current;
    }

    public void setCurrent(SelectionMenuComponent current) {
        this.current = current;
    }

    public SelectionMenuComponent getCurrent() {
        return current;
    }

    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getID() == KeyEvent.KEY_RELEASED) {
            current.keyReleased(e);
        } else if (e.getID() == KeyEvent.KEY_PRESSED) {
            current.keyPressed(e);
        }
        return e.isConsumed();
    }
}
