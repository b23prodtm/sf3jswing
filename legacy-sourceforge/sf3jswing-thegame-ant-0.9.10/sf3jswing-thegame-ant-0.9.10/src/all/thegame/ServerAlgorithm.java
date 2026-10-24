package thegame;

import thegame.comp.Fighter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.*;
import net.sf.jiga.xtended.impl.game.GameActionLayer;
import net.sf.jiga.xtended.impl.game.RenderingScene;
import net.sf.jiga.xtended.kernel.ThreadWorks;

/**
 *<p> Cette classe a ete modifiee pour implementer deux Threads, un pour le deplacement des raquettes et un autre pour celui de la balle. Ceci dans le but d'ameliorer les performances suite a une modification du deplacement des raquettes et du rebond de la balle. En effet, nous avons essaye de tenir compte de l'inertie des des raquettes et de la variabilite du rebond de la balle sur les raquettes, en fonction de l'impact. </p>
 *  This is the core of the game.<br/>
 *  The state of the game is computed at every tick (10 milliseconds).<br/>
 *  Basically, it computes the current position of the ball, and informs of the state of game at
 *  all the AlgorithmListener's.<br/>
 *  To add an AlgorithmListener, call addAlgorithmListener(AlgorithmListener listener);<br/>
 *  It also informs the listeners when the game is started, paused and resumed.<br/>
 *  <br/>
 *  Call ServerAlgorithm.getAlgorithmInterface(int playerNum) to get an interface for the
 *  players (MachinePlayer, KeyboardPlayer, RemotePlayer) to communicate with the ServerAlgorithm.<br/>
 *  <br/>
 *  or call ServerAlgorithm.getAlgorithmInterface() if you don't need to move the player,
 *  only pause, resume or cancel the game (Pong class)
 */
public class ServerAlgorithm extends GameActionLayer implements GameStateHandler/*implements Threaded, * Runnable */ {

    /** time interval for each tick 
    public final static long tickTime = (long) (1000f / 60f); */
    /** the properties of the game */
    GameProperties props;
    /** the start player. For now, it is always player number 1 */
    int startPlayer = 0;
    /** Position of the two players, as defined in the picture */
    Fighter[] players = null;
    /* floor position*/
    int floor;
    /* starting positions*/
    Point2D[] startingPositions = null;
    /* players' lives*/
    int[] lives = null;
    int state;
    /* winner*/
    int winner = -1;
    /** the position of the ball at the lastPosBallTick. used to compute a possible collision 
    javax.swing.Timer timer = null;*/
    /** a list of the AlgorithmListener's */
    Set<AlgorithmListener> listeners = Collections.synchronizedSet(new HashSet<AlgorithmListener>());
    /***/
    FieldGui gui;
    int mode;
    /*private java.util.Timer timer;*/
    Sf3 sf3;

    /** construct a ServerAlgorithm, with the defined props
     *  @param props the properties of the game
     */
    public ServerAlgorithm(Sf3 sf3, int mode, FieldGui gui, GameProperties props) {
        super("server algorithm");
        assert (mode & (Game.MODE_LOCAL | Game.MODE_SERVER)) != 0 : "selected mode is not supported. Please, specify GameState.GAME_LOCAL or GameState.GAME_SERVER !";
        assert (mode & (Game.MODE_VS_COMP | Game.MODE_VS_HUMAN)) != 0 : "selected mode is not supported. Please, specify GameState.GAME_VS_HUMAN or GAME_VS_COMP !";
        this.sf3 = sf3;
        this.mode = mode;
        this.gui = gui;
        this.props = props;
        gui.setProps(props);
        state = Game.STATE_UNKNOWN;
    }

    /** Request to the ServerAlgorithm (or RemoteAlgorithm) the game state
     *  @return the current state of the game
     */
    public Game requestGameState() {
        return new Game(gui.getGameLayer(state).isLwjgl(), mode, gui.getBackFieldBounds(), (Fighter[]) players, gui.getBackgroundModels(state), state, winner);
    }

    /** run the game, and put it in paused mode. call resumeGame() to begin *
     * 
     */
    @Override
    protected void doLoad() {
        super.doLoad();
        state = Game.STATE_READY_TO_START;
        final Game gs = requestGameState();
        updateGameState(gs);
        /*inform all the listeners that the game has started*/
        Runnable r = new Runnable() {

            public void run() {
                synchronized (listeners) {
                    for (Iterator i = listeners.iterator(); i.hasNext();) {
                        ((AlgorithmListener) i.next()).gameStarted(
                                startPlayer,
                                gs);
                    }
                }
            }
        };
        RenderingScene rsc = getRenderingScene();
        if (rsc instanceof RenderingScene) {
            rsc.doTask(r);
        } else {
            r.run();
        }
        System.out.println("GAME READY TO START");
        /*the game now is in paused mode. When resumeGame() is called, the game will begin, and eventually
        the game will finish or be cancelled, and this function will return*
        while ((state & Game._bits._getMask(Game._STATE) & (Game.STATE_FINISHED | Game.STATE_MENU)) == 0) {
        try {
        wait();
        } catch (InterruptedException e) {
        e.printStackTrace();
        }
        }
        clearPlayers();*/
    }

    @Override
    protected void doUnload() {
        super.doUnload();
        clearPlayers();
    }

    /** Inform the ServerAlgorithm (or RemoteAlgorithm) to pause the game */
    private void pauseGame() {
        if ((state & Game._bits._getMask(Game._STATE) & Game.STATE_RUNNING) == 0) {
            return;
        }
        /*if (timer != null) {
        timer.cancel();
        timer = null;
        }*/
        state = Game.STATE_PAUSED;
        final Game gs = requestGameState();
        updateGameState(gs);
        /*inform the listeners that the game is paused*/
        RenderingScene rsc = getRenderingScene();
        rsc.doTask(new Runnable() {

            public void run() {
                synchronized (listeners) {
                    for (Iterator i = listeners.iterator(); i.hasNext();) {
                        ((AlgorithmListener) i.next()).gamePaused(gs);
                    }
                }
            }
        });
    }

    private void clearPlayers() {
        floor = 0;
        lives = null;
        startingPositions = null;
        players = null;
        playersReady = false;
    }
    boolean playersReady = false;

    private void initPlayers(FieldGui gui) {
        lives = new int[FieldGui.NPLAYERS];
        startingPositions = new Point[FieldGui.NPLAYERS];
        for (int i = 0; i < FieldGui.NPLAYERS; i++) {
            startingPositions[i] = gui.getStartingPositions(i);
            lives[i] = 100;
        }
        final Fighter[] playerONE = new Fighter[1];
        final Fighter[] playerTWO = new Fighter[1];
        players = new Fighter[FieldGui.NPLAYERS];
        players[0] = playerONE[0] = new Fighter(sf3.sf3Player[0], Fighter.id.ONE, gui.getSelectedPlayer()[0],
                playerTWO,
                gui.getBackFieldBounds(),
                100,
                startingPositions[0]);
        players[1] = playerTWO[0] = new Fighter(sf3.sf3Player[1], Fighter.id.TWO, gui.getSelectedPlayer()[1],
                playerONE,
                gui.getBackFieldBounds(),
                100,
                startingPositions[1]);
        playersReady = true;
    }

    public void updateGameState(Game gameState) {
        if ((gameState.state & Game._bits._getMask(Game._STATE) & Game.STATE_RUNNING) != 0) {
            players[0].setState(
                    gui.getFightersFieldBounds(true), lives[0],
                    players[0].getPosPlayer());
            players[1].setState(
                    gui.getFightersFieldBounds(true), lives[1],
                    players[1].getPosPlayer());
            boolean ready = true;
            for (Fighter f : (Fighter[]) players) {
                ready = f.isLoaded() && ready;
            }
            for (Fighter f : (Fighter[]) players) {
                f.setEnabled(ready && !gui.getScene().isLoading());
            }
        }
        if ((gameState.state & Game._bits._getMask(Game._STATE) & (Game.STATE_FINISHED | Game.STATE_READY_TO_START | Game.STATE_MENU)) != 0) {
            clearPlayers();
        }
    }

    /** Inform the ServerAlgorithm (or RemoteAlgorithm) to resume the game */
    private void resumeGame() {
        if ((state & Game._bits._getMask(Game._STATE) & (Game.STATE_FINISHED | Game.STATE_RUNNING)) != 0) {
            return;
        }
        if (!playersReady) {
            initPlayers(gui);
        }
        if ((state & Game._bits._getMask(Game._STATE) & Game.STATE_PAUSED) == 0) {
            state = Game.STATE_LOADING;
            updateGameState(requestGameState());
            RenderingScene rsc = getRenderingScene();
            rsc.doTask(new Runnable() {

                public void run() {
                    synchronized (listeners) {
                        for (Iterator i = listeners.iterator(); i.hasNext();) {
                            ((AlgorithmListener) i.next()).gameLoading(requestGameState());
                        }
                    }
                }
            });
            synchronized (this) {
                try {
                    while ((gui.cacheloading.getLoadState() & RenderingScene._GLLOADSTATE_Loaded) == 0) {
                        wait(1000);
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        state = Game.STATE_RUNNING;
        /*if(!rsc.isLWJGLAccel())
        rsc.setLWJGLAccel(true);*/
        updateGameState(requestGameState());
        /*inform the listeners that the game is resumed*/
        RenderingScene rsc = getRenderingScene();
        rsc.doTask(new Runnable() {

            public void run() {
                synchronized (listeners) {
                    for (Iterator i = listeners.iterator(); i.hasNext();) {
                        ((AlgorithmListener) i.next()).gameResumed(requestGameState());
                    }
                }
            }
        });
    }

    /** Inform the ServerAlgorithm (or RemoteAlgorithm) to cancel the game */
    private void cancelGame() {
        System.out.println("Game CANCELED");
        /*if (timer != null) {
        timer.cancel();
        timer = null;
        }*/
        state = Game.STATE_MENU;
        final Game gs = requestGameState();
        updateGameState(gs);
        /*inform the listeners that the game is cancelled        */
        Runnable r = new Runnable() {

            public void run() {
                synchronized (listeners) {
                    for (Iterator i = listeners.iterator(); i.hasNext();) {
                        ((AlgorithmListener) i.next()).gameCanceled(gs);
                    }
                }
            }
        };
        RenderingScene rsc = getRenderingScene();
        if (rsc instanceof RenderingScene) {
            rsc.doTask(r);
        } else {
            r.run();
        }

    }

    /** computes the new state of the game, and informs the listeners about it */
    @Override
    protected void performTask() {
        int pty = Thread.currentThread().getPriority();
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        /*if (rs instanceof RenderingScene) {
        setMultiThreadingEnabled(rs.isMultiThreadingEnabled());
        }*/
        updateGameState(requestGameState());
        /*if the game has finished, make run() return*/
        if (state == Game.STATE_FINISHED) {
        }
        if (state == Game.STATE_RUNNING && (gui.cacheloading.getLoadState() & RenderingScene._GLLOADSTATE_Loaded) != 0) {
            processing = true;
            System.out.println("(/) 'Tick processed' event spreading " + new Date(System.currentTimeMillis()).toString());
            /*inform all the listeners about the current state of the game*/
            synchronized (listeners) {
                for (Iterator i = listeners.iterator(); i.hasNext();) {
                    ((AlgorithmListener) i.next()).tickProcessed(requestGameState());
                }
            }
            processing = false;
        }
        Thread.currentThread().setPriority(pty);
    }

    /** add the specified listener to receive events from the game */
    public void addAlgorithmListener(AlgorithmListener listener) {
        listeners.add(listener);
    }

    /* remove the specified listener so that it no longer receives events from the game */
    public void removeAlgorithmListener(AlgorithmListener listener) {
        listeners.remove(listener);
    }

    /** get an AlgorithmInterface to interact with the ServerAlgorithm */
    public AlgorithmInterface getAlgorithmInterface() {
        return new PlayerAlgorithmInterface();
    }

    /** get an AlgorithmInterface to interact with the ServerAlgorithm for a given player number
     *  @param playerNum the player number that will interact with the ServerAlgorithm<br/>
     *                   (this avoids that some malicious coder could modify RemotePlayer-RemoteAlgorithm
     *                    to also move the other player remotely)
     */
    public AlgorithmInterface getAlgorithmInterface(int playerNum) {
        return new PlayerAlgorithmInterface(playerNum);
    }
    ThreadWorks gameCommands = new ThreadWorks("SalgInterface");

    /**
     *  This class is for the players (MachinePlayer, KeyboardPlayer, RemotePlayer)
     *  to interact with the ServerAlgorithm.<br/>
     *  If we let the players interact directly with the ServerAlgorithm,
     *  it would be possible for your 'friend' to modify RemoteAlgorithm in his computer
     *  and move your racket also, calling ServerAlgorithm.movePlayer(playerNum, direction)
     *  (playerNum = 0'you' or 1'him')
     */
    class PlayerAlgorithmInterface implements AlgorithmInterface {

        /** 0 or 1 for player 1 or 2, respectively, or -1 if no player defined */
        int playerNum;

        /* creates a AlgorithmInterface for a specified player */
        public PlayerAlgorithmInterface(int playerNum) {
            this.playerNum = playerNum;
        }

        /* creates a AlgorithmInterface (without specifying a player) */
        public PlayerAlgorithmInterface() {
            this.playerNum = -1;
        }

        /** Request to the ServerAlgorithm (or RemoteAlgorithm) the game state
         *  @return the current state of the game
         */
        public Game requestGameState() {
            return ServerAlgorithm.this.requestGameState();
        }

        /** Inform the ServerAlgorithm (or RemoteAlgorithm) to pause the game */
        public void pauseGame() {
            gameCommands.doLater(new Runnable() {

                public void run() {
                    ServerAlgorithm.this.pauseGame();
                }
            });
        }

        /** Inform the ServerAlgorithm (or RemoteAlgorithm) to resume the game */
        public void resumeGame() {
            gameCommands.doLater(new Runnable() {

                public void run() {
                    ServerAlgorithm.this.resumeGame();
                }
            });
        }

        /** Inform the ServerAlgorithm (or RemoteAlgorithm) to cancel the game */
        public void cancelGame() {
            gameCommands.doLater(new Runnable() {

                public void run() {
                    ServerAlgorithm.this.cancelGame();
                }
            });
        }

        public void updateGameState(final Game gs) {
            gameCommands.doLater(new Runnable() {

                public void run() {
                    ServerAlgorithm.this.updateGameState(gs);
                }
            });
        }

        public boolean dispatchKeyEvent(KeyEvent e) {
            return ServerAlgorithm.this.gui.dispatchKeyEvent(e);
        }
    }
    boolean processing = false;
}
