package thegame;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;

/**
	The players (MachinePlayer, KeyboardPlayer and RemotePlayer) has an AlgorithmInterface
	to inform the ServerAlgorithm (and the RemoteAlgorithm) to pause, resume or cancel the game.<br/>
	It can also ask to the state of the game, and move the player.
 */
public interface AlgorithmInterface extends KeyEventDispatcher, GameStateHandler
{
	/** Request to the ServerAlgorithm (or RemoteAlgorithm) the game state
	 *  @return the current state of the game
	 */
	public Game requestGameState();
	
	/** Inform the ServerAlgorithm (or RemoteAlgorithm) to pause the game */
	public void pauseGame();

	/** Inform the ServerAlgorithm (or RemoteAlgorithm) to resume the game */
	public void resumeGame();

	/** Inform the ServerAlgorithm (or RemoteAlgorithm) to cancel the game */
	public void cancelGame();	
        
}


