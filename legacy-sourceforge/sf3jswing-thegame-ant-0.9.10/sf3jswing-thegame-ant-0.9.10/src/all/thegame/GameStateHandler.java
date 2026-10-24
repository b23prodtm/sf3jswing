/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package thegame;

/**
 *
 * @author www.b23prodtm.info
 */
public interface GameStateHandler {
    void updateGameState(Game gs);
    Game requestGameState();
}
