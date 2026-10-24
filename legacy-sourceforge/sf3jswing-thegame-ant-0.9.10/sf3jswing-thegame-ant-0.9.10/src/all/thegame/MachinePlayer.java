package thegame;

import thegame.comp.Fighter;
import net.sf.jiga.xtended.kernel.Monitor;

/**
 *  A player who is driven by a stupid algorithm.<br/>
 *  It just tries to move the racket at the same height as the ball is, no anticipation
 *  <b> Classe modifiee. La methode tickProcessed() a ete modifie, de facon a ce que le joueur dirige par la machine ait un meilleur comportement. Sa vulnerabilite est plus grande.
 */
public class MachinePlayer extends Sf3Player implements AlgorithmListener {

    public MachinePlayer(int playerNum, FieldGui gui) {
        super(playerNum, gui);
    }

    @Override
    public void updateGameState(Game gs) {
        super.updateGameState(gs);
        if (playerPhysics instanceof Fighter) {
            playerPhysics.setMachine(true);
        }
    }

    public Monitor[] getGroupMonitor() {
        return new Monitor[]{};
    }

    public void setGroupMonitor(Monitor... mntrs) {
    }
}
