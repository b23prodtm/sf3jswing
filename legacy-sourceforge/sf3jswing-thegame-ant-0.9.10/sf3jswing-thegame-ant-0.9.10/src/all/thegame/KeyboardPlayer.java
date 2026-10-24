package thegame;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;
import net.sf.jiga.xtended.impl.game.InteractiveModel;
import net.sf.jiga.xtended.impl.game.XtendedModel;
import net.sf.jiga.xtended.kernel.Monitor;
import thegame.comp.Fighter;

/**
 *  A player who is driven by the keyboard.<br/>
 *  At every tick of the game, it needs to know if the cursor keys are pressed or not.<br/>
 *  So it cannot rely on typical AWT key events, as the frequency in which they report the
 *  events while a key is being pressed is system-dependent and usely slow (1/2 second).<br/>
 *  It uses the KeyboardFocusManager to know when the keys are pressed.
 */
public class KeyboardPlayer extends Sf3Player implements KeyEventDispatcher {

    public KeyboardPlayer(int playerNum, FieldGui gui) {
        super(playerNum, gui);
    }

    public boolean dispatchKeyEvent(KeyEvent e) {
        return playerModel instanceof XtendedModel ? playerModel.isLoaded() ? playerModel.dispatchKeyEvent(e) : false : false;
    }

    @Override
    public void updateGameState(Game gs) {
        super.updateGameState(gs);
        if (playerPhysics instanceof Fighter) {
            playerPhysics.setMachine(false);
        }
    }

    public Monitor[] getGroupMonitor() {
        return new Monitor[]{};
    }

    public void setGroupMonitor(Monitor... mntrs) {
    }
}
