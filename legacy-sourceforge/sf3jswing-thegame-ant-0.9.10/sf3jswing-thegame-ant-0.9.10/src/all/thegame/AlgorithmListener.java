package thegame;

/**
 * The ServerAlgorithm (and the RemoteAlorithm) has a list of AlgorithmListener's.<br/>
 * It sends notification to all of them about changes in the game<br/>
 * (started, paused, resumed, and at every tick)
 */
public interface AlgorithmListener {

    /***/
    void gameLoading(Game gameState);

    /** The ServerAlgorithm (or RemoteAlgorithm) calls this method when the game starts
     *  @param startPlayer the player that has initially the ball. 0 and 1 for player 1 and 2 respectively
     *  @param gameState the initial state of the game
     */
    void gameStarted(int startPlayer, Game gameState);

    /** The ServerAlgorithm (or RemoteAlgorithm) calls this method when the game is paused */
    void gamePaused(Game gameState);

    /** The ServerAlgorithm (or RemoteAlgorithm) calls this method when the game is resumed */
    void gameResumed(Game gameState);

    /** The ServerAlgorithm canceled the game - we may return to the menu */
    void gameCanceled(Game gameState);

    /** The ServerAlgorithm (or RemoteAlgorithm) calls this method after each tick of the game
     *  @param gameState the actual state of the grame
     *  @param ballBounced if true, the ball just touched the boundaries of the field, or the rackets
     *                     it could be used to play some sound
     */
    void tickProcessed(Game gameState);
}
