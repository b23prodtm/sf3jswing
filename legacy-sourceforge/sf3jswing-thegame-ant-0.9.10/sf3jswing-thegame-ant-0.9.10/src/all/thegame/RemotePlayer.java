package thegame;

import java.awt.event.KeyEvent;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.lang.Exception;
import java.io.IOException;
import javax.swing.JLabel;
import net.sf.jiga.xtended.impl.game.GameActionLayer;
import net.sf.jiga.xtended.impl.system.input.KeyEventWrapper;
/**
*  When in server mode, Pong creates a RemotePlayer. RemotePlayer interacts with RemoteAlgorithm, which it is
 *  in the client side.<br/>
 *  The player in the client side informs to this RemoteAlgorithm of its actions. RemoteAlgorithm informs to this class,
 *  and this class informs to the ServerAlgorithm.<br/>
 *  The ServerAlgorithm, informs about the state of the game at every tick
 *  (and when the game is started, pausd, resumed) to this class. This class informs to RemoteAlgorithm class,
 *  and RemoteAlgorithm forwards this information to all its listeners (typically the KeyboardPlayer, fieldGui, statusBar).<br/>
 *  <br/>
 *  TODO: Il faut complÈter toutes les fonctions indiquÈes<br/>
 *  <br/>
 * Information suplementaire: @see RemoteAlgorithm
 */
public class RemotePlayer extends GameActionLayer implements AlgorithmListener
{
    long hash = System.nanoTime();

    public int hashCode() {
        return (int) hash;
    }

    public boolean equals(Object o) {
        return o == null ? false : o.hashCode() == hashCode();
    }
    /** the properties of the game */
    GameProperties props;

    /** the AlgorithmInterface used to communicate with the ServerAlgorithm */
    AlgorithmInterface algInterface;

    /** the socket with RemoteAlgorithm */
    Socket socket;

    /** the output stream of the socket */
    ObjectOutputStream out;

    /** the input stream of the socket */
    ObjectInputStream in;

    /*
     TODO: on peut ajouter des variables si nÈcessaires
     */

    /** gameStarted() est appelée */
    public final static int GAME_STARTED = 0;

    /** gamePaused() est appelée */
    public final static int GAME_PAUSED = 1;

    /** gameResumed() est appelée */
    public final static int GAME_RESUMED = 2;

    /** tickProcessed() est appelée */
    public final static int TICK = 3;

    /** cancelGame() est appelée */
    public final static int CANCEL_GAME = 4;

    /** creates a Remote player
        *  @param props the properties of the game
        *  @param algInterface the AlgorithmInterface used to communicate with the ServerAlgorithm (or RemoteAlgorithm)
        *  @param socket the open connection with RemoteAlgorithm returned by ServerConnectionDialog
        */
    public RemotePlayer(GameProperties props, AlgorithmInterface algInterface, Socket socket) throws Exception
      {
        super("remote player " + socket.toString());
        this.props = props;
          this.algInterface = algInterface;
          this.socket = socket;
          out = new ObjectOutputStream(socket.getOutputStream());
          in = new ObjectInputStream(socket.getInputStream());

          /*
           TODO: envoyer ‡ RemotePlayer, ‡ travers du socket, props et algInterface.requestGameState()
           */
          try {
              out.writeObject(props);
              out.writeObject(algInterface.requestGameState());
              out.flush();
          } catch (IOException e) {
            e.printStackTrace();
          }
      }
	  
	/** The ServerAlgorithm (or RemoteAlgorithm) calls this method when the game starts
        *  @param startPlayer the player that has initially the ball. 0 and 1 for player 1 and 2 respectively
        *  @param gameState the initial state of the game
        */
    public void gameStarted(int startPlayer, Game gameState)
      {
        /*
         TODO: Informer a RemoteAlgorithm de cette information
         */
        try {
            out.writeInt(GAME_STARTED);
            out.writeInt(startPlayer);
            out.writeObject(gameState);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
      }

    /** The ServerAlgorithm (or RemoteAlgorithm) calls this method when the game is paused */
    public void gamePaused(Game gameState)
      {
        /*
         TODO: Informer a RemoteAlgorithm de cette information
         */
        try {
            out.writeInt(GAME_PAUSED);
            out.writeObject(gameState);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
      }

    /** The ServerAlgorithm (or RemoteAlgorithm) calls this method when the game is resumed */
    public void gameResumed(Game gameState)
      {
        /*
         TODO: Informer a RemoteAlgorithm de cette information
         */
        try {
            out.writeInt(GAME_RESUMED);
            out.writeObject(gameState);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
      }

    /** The ServerAlgorithm (or RemoteAlgorithm) calls this method after each tick of the game
        *  @param gameState the actual state of the grame
        *  @param ballBounced if true, the ball just touched the boundaries of the field, or the rackets
        *                     it could be used to play some sound
        */
    public void tickProcessed(Game gameState)
      {
        /*
         TODO: Informer a RemoteAlgorithm de cette information
         */
        try {
            out.writeInt(TICK);
            out.writeObject(gameState);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
      }

	
    /** while the game is not finished, it reads the events from RemoteAlgorithm and it forwards them to the ServerAlgorithm by means of the algInterface */
    public void performTask()
      {
        /*
         TODO: Attendre la information de RemoteAlgorithm et la envoyer ‡ algInterface,
         toujours dans un loop, jusqu'‡ on appelle ‡ cancelGame() ou le jeux est fini
         */
        int event = -1;
        try {
            if (in.available() > 0) {
                try {
                    event = in.readInt();
                    switch (event) {
                        case RemoteAlgorithm.PAUSE_GAME:
                            algInterface.pauseGame();
                            break;
                        case RemoteAlgorithm.RESUME_GAME:
                            algInterface.resumeGame();
                            break;
                        case RemoteAlgorithm.CANCEL_GAME:
                            algInterface.cancelGame();
                            break;
                        case RemoteAlgorithm.DISPATCH_KEY:
                            KeyEventWrapper e = (KeyEventWrapper) in.readObject();
                            algInterface.dispatchKeyEvent(new KeyEvent(new JLabel("remote player"), e.get_ID(), System.currentTimeMillis(), in.readInt(), e.get_keyCode(), in.readChar()));
                            break;
                        default:
                            break;
                    }
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

      }

    /** cancel the game */
    public void cancelGame()
      {
        /*
         TODO: finir le jeu
         */
        try {
            out.writeInt(CANCEL_GAME);
            out.flush();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
      }

    public void gameCanceled(Game gameState) {
    }

    public void gameLoading(Game gameState) {
    }
}
