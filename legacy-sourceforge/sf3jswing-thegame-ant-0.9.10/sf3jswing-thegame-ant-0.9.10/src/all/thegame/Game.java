package thegame;


import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import thegame.comp.Fighter;
import net.sf.jiga.xtended.kernel.BitStack;
import net.sf.jiga.xtended.impl.game.Model;
import net.sf.jiga.xtended.impl.game.XtendedModel;

/**
 * Defines the state of the game.<br/>
 * <img src="field.gif"/><br/>
 * <br/>
 * TODO: Changer qui nécessaire dont il devient "serializable"
 */
public class Game implements Serializable {

    public final static BitStack _bits = new BitStack();
    /***/
    public boolean isLWJGLAccel;
    /***/
    public XtendedModel[] backgroundModels;
    /** Position of the two players, as defined in the picture */
    public Fighter[] fighters;
    /** The state of the game: STATE_READY_TO_START, STATE_RUNNING, STATE_PAUSED or STATE_FINISHED */
    public int state;
    /** The winner of the game. While the game is not finished, winner = -1. */
    public int winner;
    /* Battle round*/
    public int round;
    /** game mode (network, local, vs, hum vs com)*/
    public int game_mode;

    public final static int _MODE = _bits._newBitRange();
    public final static int MODE_SERVER = _bits._newBit(_MODE);
    public final static int MODE_CLIENT = _bits._newBit(_MODE);
    public final static int MODE_LOCAL = _bits._newBit(_MODE);
    public final static int MODE_VS_HUMAN = _bits._newBit(_MODE);
    public final static int MODE_VS_COMP = _bits._newBit(_MODE);

    public final static int _STATE = _bits._newBitRange();
    /** The ball and the rackets are in the initial position. The game is ready to start 
     */
    public static final int STATE_READY_TO_START = _bits._newBit(_STATE);
    /** The game is running */
    public static final int STATE_RUNNING = _bits._newBit(_STATE);
    /** The game is paused (and it is not STATE_READY_TO_START) */
    public static final int STATE_PAUSED = _bits._newBit(_STATE);
    /** The game has finished */
    public static final int STATE_FINISHED = _bits._newBit(_STATE);
    /***/
    public static final int STATE_MENU = _bits._newBit(_STATE);
    /***/
    public static final int STATE_UNKNOWN = _bits._newBit(_STATE);
    /***/
    public static final int STATE_LOADING = _bits._newBit(_STATE);
    /***/
    public static final int STATE_GAMEOVER= _bits._newBit(_STATE);
    /***/
    public Rectangle2D fieldBounds;

    /** Constructs a Game instance that defines the current status */
    public Game(boolean lwjgl, int game_mode, Rectangle2D fieldBounds, Fighter[] fighters, XtendedModel[] backgroundModels, int state, int winner) {
        this.isLWJGLAccel = lwjgl;
        this.game_mode = game_mode;
        this.fieldBounds = fieldBounds;
        this.fighters = fighters;
        this.state = state;
        this.winner = winner;
        this.backgroundModels = backgroundModels;
    }
    /***/
    public static Game UNKNOWN_GAMESTATE = new Game(false, 0, new Rectangle(), new Fighter[]{}, new XtendedModel[]{}, STATE_UNKNOWN, 0);
}
