package thegame;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;
import java.net.Socket;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.lang.Exception;
import java.io.IOException;
import net.sf.jiga.xtended.impl.game.GameActionLayer;
import net.sf.jiga.xtended.impl.system.input.KeyEventWrapper;

/**
 *  When in client mode, Pong creates a RemoteAlgorithm. RemoteAlgorithms interacts with RemotePlayer, which it is
 *  in the server side.<br/>
 *  The player in the client side informs to this class of its actions. This class informs to the RemotePlayer,
 *  and RemotePlayer informs to the ServerAlgorithm.<br/>
 *  The ServerAlgorithm, informs about the state of the game at every tick
 *  (and when the game is started, pausd, resumed) to the RemotePlayer. RemotePlayer informs to this class,
 *  and this class forwards this information to all its listeners (typically the KeyboardPlayer, fieldGui, statusBar).<br/>
 *  <br/>
 *  TODO: Il faut complÈter toutes les fonctions indiquÈes<br/>
 *  <br/>
 *  <br/>
 * Information suplementaire:<br/>
 * Faire que deux personnes puissent jouer ‡ distance.<br/>
 * Fichiers: GameProperites.java, Game.java, RemotePlayer.java, RemoteAlgorithm.java<br/>
 * NÈcessaire savoir: Serialization, Sockets, Listeners<br/>
 * <br/>
 * Il y a deux joueurs, joueur A et joueur B.<br/>
 * Le joueur A sÈlectionne les propriÈtÈs du jeu, et sÈlectionne Play->Run Server Mode<br/>
 * Le joueur B sÈlectionne Play->Connect to server<br/>
 * <br/>
 * Le programme du joueur A crÈe une instance de RemotePlayer, et commence la communication en enviant les propriÈtÈs du jeux et l'Ètat du jeu [AlgorithmInterface.requestGameState()].<br/>
 * <br/>
 * Le programme du joueur B crÈe une instance de RemoteAlogorithm, qui lit ces objets. (dans le constructeur!)<br/>
 * <br/>
 * AprËs, le programme du joueur B appel ‡ RemoteAlogorithm.getGameProperties() pour connaÓtre les propriÈtÈs du jeu et les visualiser.<br/>
 * <br/>
 * RemotePlayer implÈmente AlgorithmListener. Alors il doit implÈmenter les fonctions de cette interface. (Voir AlgorithmListener.java)<br/>
 * 	void gameStarted(int startPlayer, Game gameState);<br/>
 * 	void gamePaused();<br/>
 * 	void gameResumed();<br/>
 * 	void tickProcessed(Game gameState, boolean ballBounced);<br/>
 * <br/>
 * Quand ces fonctions sont appelÈ, il dois informer ‡ RemoteAlgorithm de ce qui passe, et RemoteAlgorithm doit transmettre cette information ‡ son liste de AlgorithmListeners.<br/>
 * <br/>
 * Avec �?a, le programme de joueur A informe au programme de joueur B de ce qui passe.<br/>
 * <br/>
 * Le programme de joueur B dois informer aussi au premier de ce qui passe ‡ son cotÈ.<br/>
 * <br/>
 * Pour �?a, RemoteAlgorithm implÈmente AlgorithmInterface:<br/>
 * 	public Game requestGameState();<br/>
 * 	public void movePlayer(int direction);<br/>
 * 	public void pauseGame();<br/>
 * 	public void resumeGame();<br/>
 * 	public void cancelGame();<br/>
 * Quand ces fonctions sont appelÈ, il dois informer ‡ RemotePlayer de ce qui passe, et RemotePlayer doit transmettre cette information ‡ son instance d'AlgorithmInterface.<br/>
 * <br/>
 * Pour ca, il faut:<br/>
 * Changer qui necessaire dans GameProperties dont il devient "serializable".<br/>
 * Changer qui necessaire dans Game dont il devient "serializable".<br/>
 * Completer RemotePlayer<br/>
 * Completer RemoteAlgorithm<br/>
 */
public class RemoteAlgorithm extends GameActionLayer implements AlgorithmInterface, KeyEventDispatcher {
    /* the socket with the RemotePlayer */

    Socket socket;
    /* the output stream of the socket */
    ObjectOutputStream out;
    /* the input stream of the socket */
    ObjectInputStream in;
    /* the properties of the game */
    GameProperties props;
    /* the current state of the game */
    Game gameState;
    /** a list of the AlgorithmListener's */
    HashSet<AlgorithmListener> listeners = new HashSet<AlgorithmListener>();
    /** pauseGame() est appelée */
    public final static int PAUSE_GAME = 1;
    /** resumeGame() est appelée */
    public final static int RESUME_GAME = 2;
    /** cancelGame() est appelée */
    public final static int CANCEL_GAME = 3;
    public final static int DISPATCH_KEY = 4;
    int mode;

    /**
     *  creates a RemoteAlgorithm with the given open connection.
     *  @param socket the open connection with RemotePlayer returned by ClientConnectionDialog
     */
    public RemoteAlgorithm(int mode, Socket socket) throws Exception {
        super("remote algorithm");
        assert (mode & Game.MODE_CLIENT) != 0 : "selected mode is not supported. Please, specify GameState.GAME_CLIENT !";
        assert (mode & (Game.MODE_VS_COMP | Game.MODE_VS_HUMAN)) != 0 : "selected mode is not supported. Please, specify GameState.GAME_VS_HUMAN or GAME_VS_COMP !";
        this.mode = mode;
        this.socket = socket;
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        /*
        TODO: lire de RemotePlayer, ‡ travers du socket, props et gameState
         */
        props = (GameProperties) in.readObject();
        gameState = (Game) in.readObject();
    }

    /** returns the properties of the game choosed by the server
     *  @return the properties of the game
     */
    public GameProperties getGameProperties() {
        return props;
    }

    /*boolean running = false;

    /** While the game is not finished, it reads the events from RemotePlayer and it forwards them to all the AlgorithmListener's 
    public void run() {
        /*
        TODO: Attendre la information de RemotePlayer et la envoyer ‡
        tous les AlgorithmListener de listeners,
        toujours dans un loop, jusqu'‡ on appelle ‡ cancelGame() ou le jeu est fini
         
        running = true;
    }*/

    /** Request to the ServerAlgorithm (or RemoteAlgorithm) the game state
     *  @return the current state of the game
     */
    public Game requestGameState() {
        return gameState;
    }

    /** Inform the ServerAlgorithm (or RemoteAlgorithm) to pause the game */
    public void pauseGame() {
        /*
        TODO: Informer a RemotePlayer de cette information
         */
        try {
            out.writeInt(PAUSE_GAME);
            out.flush();
        } catch (IOException e) {
        }
    }

    /** Inform the ServerAlgorithm (or RemoteAlgorithm) to resume the game */
    public void resumeGame() {
        /*
        TODO: Informer a RemotePlayer de cette information
         */
        try {
            out.writeInt(RESUME_GAME);
            out.flush();
        } catch (IOException e) {
        }
    }

    /** Inform the ServerAlgorithm (or RemoteAlgorithm) to cancel the game */
    public void cancelGame() {
        /*
        TODO: finir le jeu
         */
        try {
            out.writeInt(CANCEL_GAME);
            out.flush();
            try {
                socket.close();
            } catch (Exception e) {
            }
        } catch (IOException e) {
        }
    }

    /** add the specified listener to receive events from the game */
    public void addAlgorithmListener(AlgorithmListener listener) {
        listeners.add(listener);
    }

    /* remove the specified listener so that it no longer receives events from the game */
    public void removeAlgorithmListener(AlgorithmListener listener) {
        listeners.remove(listener);
    }

    public boolean dispatchKeyEvent(KeyEvent e) {
        try {
            out.writeInt(DISPATCH_KEY);
            out.writeObject(new KeyEventWrapper(e.getWhen(), e.getKeyCode(), e.getID()));
            out.writeInt(e.getModifiers());
            out.writeChar(e.getKeyChar());
            out.flush();
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public void updateGameState(Game gs) {
    }

    @Override
    public void performTask() {
        try {
            if (in.available() > 0) {
                int event = -1;
                try {
                    event = in.readInt();
                    System.out.println("rAlg : received event = " + event);
                    switch (event) {
                        case RemotePlayer.GAME_STARTED:
                            for (Iterator i = listeners.iterator(); i.hasNext();) {
                                AlgorithmListener algLi = (AlgorithmListener) i.next();
                                algLi.gameStarted(in.readInt(), (Game) in.readObject());
                            }
                            break;
                        case RemotePlayer.GAME_PAUSED:
                            for (Iterator i = listeners.iterator(); i.hasNext();) {
                                AlgorithmListener algLi = (AlgorithmListener) i.next();
                                algLi.gamePaused((Game) in.readObject());
                            }
                            break;
                        case RemotePlayer.GAME_RESUMED:
                            for (Iterator i = listeners.iterator(); i.hasNext();) {
                                AlgorithmListener algLi = (AlgorithmListener) i.next();
                                algLi.gameResumed((Game) in.readObject());
                            }
                            break;
                        case RemotePlayer.TICK:
                            for (Iterator i = listeners.iterator(); i.hasNext();) {
                                AlgorithmListener algLi = (AlgorithmListener) i.next();
                                algLi.tickProcessed((Game) in.readObject());
                            }
                            break;
                        case RemotePlayer.CANCEL_GAME:
                            cancelGame();
                            break;
                        default:
                            break;
                    }
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
