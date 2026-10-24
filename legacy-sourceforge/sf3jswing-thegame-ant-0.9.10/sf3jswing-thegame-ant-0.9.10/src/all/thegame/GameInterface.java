package thegame;

import java.awt.Component;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author www.b23prodtm.info
 */
public interface GameInterface {

    Component getRootComponent();

    /**
     * Cancel the game. Do nothing if there is no game running
     */
    void cancelGame();

    /**
     * Change the options using the GamePropertiesDialog
     */
    void options();

    /**
     * Pause the game. Do nothing if there is no game running
     */
    void pauseResumeGame();

    /**
     * Play againt the machine.<br/>0
     * Creates the ServerAlgortihm, the MachinePlayer and the Keyboard Player,
     * and creates the listeners needed.
     */
    void playAgainstMachine();

    void playAgainstHuman();
    /**
     * Run Client mode.<br/>
     * Establish a connection with the server, and then creates the RemoteAlgortihm,
     * the Keyboard Player and creates the listeners needed.
     */
    void runClientMode();

    /**
     * Run Server mode.<br/>
     * Waits for a client connection, and then creates the ServerAlgortihm, the
     * Keyboard Player, the RemotePlayer and creates the listeners needed.
     */
    void runServerMode();

}
