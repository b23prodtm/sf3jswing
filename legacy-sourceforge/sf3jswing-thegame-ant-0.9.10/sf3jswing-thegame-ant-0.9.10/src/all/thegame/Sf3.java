package thegame;

/**
 * <p>Title: sf3jswing </p> <p>Description: THE GAME </p> <p>Copyright:
 * Copyright (c) 2003 - 2010 </p> <p>Company: </p>
 *
 * @author www.b23prodtm.info
 * @version 1.0
 */
import thegame.io.ReadAndSaveSettings;
import thegame.comp.Fighter;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.KeyEventDispatcher;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import net.sf.jiga.xtended.impl.SoundInput;
import net.sf.jiga.xtended.impl.game.GameActionLayer;
import net.sf.jiga.xtended.impl.game.InteractiveModel;
import net.sf.jiga.xtended.impl.game.RenderingScene;
import net.sf.jiga.xtended.kernel.JXAenvUtils;
import net.sf.jiga.xtended.kernel.ThreadWorks;
import net.sf.jiga.xtended.impl.game.physics.Player;
import net.sf.jiga.xtended.impl.system.JXAenvUtils2;
import net.sf.jiga.xtended.ui.AntApplet;
import net.sf.jiga.xtended.ui.Display;
import net.sf.jiga.xtended.ui.DisplayInterface;
import net.sf.jiga.xtended.ui.JFCApplet;
import net.sf.jiga.xtended.ui.UIMessage;

/**
 * C'est le jeu tres connu du Pong. Il y a un terrain de jeu, deux joueurs, et
 * un bille.<br/> Chaque joueur a une raquette qu'il peut bouger avec les
 * touches en haut et en bas.<br/> On peut jour contre la machine, ou avec une
 * autre personne a distance.<br/> <br/> To run it, execute "java Pong"<br/>
 * <br/> It creates the Frame, initializes the FieldGui, the menus, the staus
 * bar, and is responsable for executing the ServerAlgorithm, MachinePlayer,
 * KeyboardPlayer, RemoteAlgorithm and RemotePlayer when needed.
 */
public class Sf3 implements AntApplet, net.sf.jiga.xtended.ui.Menu, GameInterface {

    /*
     * static JXAenvUtils env = new JXAenvUtils();
     *
     * static { env.loadEnvironment(); }
     */
    /**
     * The current properties of the game
     */
    static GameProperties props;
    /**
     * Create a new objet ReadAndSaveSettings
     */
    static ReadAndSaveSettings rASSet;
    /**
     * The current AlgorithmInterface, to be able to pause/resume and cancel the
     * game
     */
    AlgorithmInterface algorithmInterface;
    /**
     *
     */
    GameActionLayer algorithmLayer;
    /**
     * The Swing component to paint the field
     */
    FieldGui fieldGui;
    /**
     * The status bar
     */
    JTextField statusBar;
    /**
     * A AlgorithmListener for updating the status bar (start, pause, resume)
     */
    AlgorithmListener statusBarAlgorithmListener;
    JApplet applet = null;
    /*
     * AppLauncher appLauncher;
     */
    Dimension size = new Dimension(640, 480);
    public static AccessControlContext acc;
    int guiLayout;

    /**
     * @param args <ul><li>fullscreen : -full</li><li>windowed :
     * -win</li><li>exclusive mode (no mode change allowed) : -fixed</li></ul>
     */
    public Sf3(String[] args) {
        super();
        acc = AccessController.getContext();
        int layout = 0;
        for (String arg : args) {
            if (arg.equals("-win")) {
                layout |= RenderingScene.options.MODE_WINDOWED.bitMask();
            }
            if (arg.equals("-full")) {
                layout |= RenderingScene.options.MODE_FULLSCREEN.bitMask();
            }
            if (arg.equals("-fixed")) {
                layout |= RenderingScene.options.MODE_EXT_FIXED.bitMask();
            }
        }
        if ((layout & RenderingScene.options.getOptionsMaskFor(RenderingScene.options._VIEWPORT_BIT)) == 0) {
            layout |= RenderingScene.options.MODE_WINDOWED.bitMask();
        }
        guiLayout = layout;
    }
    Container content;
    static AbstractAction loadLayer = new AbstractAction("option file & models") {

        public void actionPerformed(ActionEvent e) {
            props = new GameProperties();
            /*
             * Chargement des options s'il y a une sauvegarde
             */
            rASSet = new ReadAndSaveSettings(JXAenvUtils._USERHOMESTOREDIRECTORY + File.separator + GamePropertiesDialog.SETTINGSPATH, props);
            props = rASSet.loadOptions(null, 0);
            /**
             * loading of default option file included in class-path
             */
            if (rASSet.checkError()) {
                try {
                    rASSet = new ReadAndSaveSettings(JXAenvUtils._USERHOMESTOREDIRECTORY + File.separator + GamePropertiesDialog.SETTINGSPATH, getClass().getResourceAsStream("/thegame/" + GamePropertiesDialog.SETTINGSPATH));
                    props = rASSet.loadOptions(null, 0);
                } catch (Exception ex1) {
                    ex1.printStackTrace();
                } finally {
                    if (rASSet.checkError()) {
                        props = new GameProperties();
                    }
                }
            }
        }
    };
    boolean initialized = false;

    public void init() {
        if (initialized) {
            return;
        }
        SoundInput._genALBuffersAndSources(50);
        /*
         * Initialize variables
         */
        algorithmInterface = null;
        statusBarAlgorithmListener = new StatusBarAlgorithmListener();
        JMenuBar menuBar = new JMenuBar();
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        menuBar.add(getActionMenu());
        menuBar.add(getHelpActionMenu());
        applet.setJMenuBar(menuBar);
        /*
         * The layout
         */
        content = applet.getContentPane();
        content.setLayout(new BorderLayout());
        content.add(statusBar = getStatusBar(), BorderLayout.SOUTH);
        rASSet.addRaSSetListener((RaSSetListener) statusBarAlgorithmListener);
        /*
         * Make the same stuff after a cancelled game
         */
        applet.validate();
        applet.repaint();
        /*
         * The field GUI
         */
        fieldGui = newFieldGui(content, BorderLayout.CENTER, guiLayout);
        initialized = true;
        fieldGui.on();
        if ((props.getDebug() & props._DEBUG_GUI) != 0) {
            System.out.println("applet initialized");
        }
    }

    public void start() {
        if (initialized) {
            if ((props.getDebug() & props._DEBUG_GUI) != 0) {
                System.out.println("applet started");
            }
            cancelGame();
        } else if ((props.getDebug() & props._DEBUG_GUI) != 0) {
            System.out.println("applet couldn't be started");
        }

    }

    public void stop() {
        if (initialized) {
            if ((props.getDebug() & props._DEBUG_GUI) != 0) {
                System.out.println("applet stopped");
            }
            cancelGame();
        }
    }

    private JTextField getStatusBar() {
        JTextField statusBar = new JTextField();
        statusBar.setEditable(false);
        return statusBar;
    }

    private FieldGui newFieldGui(Container c, Object constraints, int guiLayout) {
        FieldGui gui = new FieldGui(guiLayout, this, c, constraints, props);
        gui.getScene().addKeyboardEventsDispatcher(new KeyEventDispatcher() {

            public boolean dispatchKeyEvent(KeyEvent e) {
                if ((e.getKeyCode() == Player._keyCodeMap.get(Player.key.ONE_start) || e.getKeyCode() == Player._keyCodeMap.get(Player.key.TWO_start) || e.getKeyCode() == KeyEvent.VK_P) && e.getID() == KeyEvent.KEY_PRESSED) {
                    pauseResumeGame();
                    return true;
                }
                return false;
            }
        });
        /*
         * gui.setMultiThreadingEnabled(appLauncher.isMultiThreadingEnabled());
         */
        return gui;
    }
    /**
     *
     */
    GamePropertiesDialog dialog;
    JFrame optionsFrame = null;

    /**
     * Change the options using the GamePropertiesDialog
     */
    public void options() {/*
         * First, cancel game
         */
        cancelGame();
        Runnable r = new Runnable() {

            public void run() {
                try {
                    dialog = new GamePropertiesDialog(props, fieldGui.getScene().isFullscreen());
                    fieldGui.switchGui(dialog, null, new AbstractAction() {

                        public void actionPerformed(ActionEvent e) {
                            GameProperties props2 = dialog.getResult();
                            dialog.dispose();
                            if (props2 != null) {
                                props = props2;
                                fieldGui.setProps(props);
                            }
                        }
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        fieldGui.getScene().doTask(r);
    }
    KeyboardPlayer[] keyboardPlayer;
    MachinePlayer[] machinePlayer;
    Sf3Player[] sf3Player;
    RemotePlayer remotePlayer;

    private void addAlgListeners(AlgorithmInterface algInter) {
        Game state = algInter.requestGameState();
        if ((state.game_mode & Game.MODE_LOCAL) != 0) {
            if (keyboardPlayer != null) {
                for (KeyboardPlayer plr : keyboardPlayer) {
                    fieldGui.getScene().addKeyboardEventsDispatcher(plr);
                    sAlg.addAlgorithmListener(plr);
                    plr.setRenderingScene(sAlg.getRenderingScene());
                }
            }
            if (machinePlayer != null) {
                for (MachinePlayer mplr : machinePlayer) {
                    sAlg.addAlgorithmListener(mplr);
                    mplr.setRenderingScene(sAlg.getRenderingScene());
                }
            }
        }
        if ((state.game_mode & Game.MODE_SERVER) != 0) {
            if (remotePlayer instanceof RemotePlayer) {
                sAlg.addAlgorithmListener(remotePlayer);
            }
        }
    }

    private void removeAlgListeners(AlgorithmInterface algInter) {
        Game state = algInter.requestGameState();
        if ((state.game_mode & Game.MODE_LOCAL) != 0) {
            if (keyboardPlayer != null) {
                for (KeyboardPlayer plr : keyboardPlayer) {
                    fieldGui.getScene().removeKeyboardEventsDispatcher(plr);
                    sAlg.removeAlgorithmListener(plr);
                }
            }
            if (machinePlayer != null) {
                for (MachinePlayer mplr : machinePlayer) {
                    sAlg.removeAlgorithmListener(mplr);
                }
            }
        }
        if ((state.game_mode & Game.MODE_SERVER) != 0) {
            if (remotePlayer instanceof RemotePlayer) {
                sAlg.removeAlgorithmListener(remotePlayer);
            }
        }
    }

    /**
     * Pause the game. Do nothing if there is no game running
     */
    public void pauseResumeGame() {
        Runnable r = new Runnable() {

            public void run() {
                for (InteractiveModel f : fieldGui.getSelectedPlayer()) {
                    if (f == null) {
                        return;
                    }
                }
                if (algorithmInterface == null) {
                    return;
                }
                Game state = algorithmInterface.requestGameState();
                if ((state.state & Game._bits._getMask(Game._STATE) & (Game.STATE_READY_TO_START | Game.STATE_PAUSED)) != 0) {
                    removeAlgListeners(algorithmInterface);
                    sf3Player = new Sf3Player[2];
                    if ((state.game_mode & Game.MODE_LOCAL) != 0) {
                        if ((state.game_mode & Game.MODE_VS_COMP) != 0) {
                            keyboardPlayer = new KeyboardPlayer[1];
                            machinePlayer = new MachinePlayer[1];
                            sf3Player[0] = keyboardPlayer[0] = new KeyboardPlayer(0, fieldGui);
                            sf3Player[1] = machinePlayer[0] = new MachinePlayer(1, fieldGui);
                        }
                        if ((state.game_mode & Game.MODE_VS_HUMAN) != 0) {
                            keyboardPlayer = new KeyboardPlayer[2];
                            sf3Player[0] = keyboardPlayer[0] = new KeyboardPlayer(0, fieldGui);
                            sf3Player[1] = keyboardPlayer[1] = new KeyboardPlayer(1, fieldGui);
                        }
                    }
                    algorithmInterface.resumeGame();
                    addAlgListeners(algorithmInterface);
                } else if ((state.state & Game._bits._getMask(Game._STATE) & Game.STATE_RUNNING) != 0) {
                    removeAlgListeners(algorithmInterface);
                    algorithmInterface.pauseGame();
                }
            }
        };
        fieldGui.getScene().doTask(r);
    }

    /**
     *
     */
    Game gameMenu() {
        Game gs = new Game(fieldGui.getGameLayer(Game.STATE_MENU).isLwjgl(), 0, new Rectangle(), new Fighter[]{}, fieldGui.getBackgroundModels(Game.STATE_MENU), Game.STATE_MENU, 0);
        return gs;
    }

    /**
     * Cancel the game. Do nothing if there is no game running
     */
    public void cancelGame() {
        Runnable r = new Runnable() {

            public void run() {
                if (algorithmInterface != null) {
                    removeAlgListeners(algorithmInterface);
                    algorithmInterface.cancelGame();
                }
                if (algorithmLayer != null) {
                    fieldGui.getScene().removeBeyondOffscreen(remotePlayer);
                    fieldGui.getScene().removeBeyondOffscreen(rAlg);
                    fieldGui.getScene().removeBeyondOffscreen(algorithmLayer);
                }
                algorithmLayer = null;
                machinePlayer = null;
                remotePlayer = null;
                keyboardPlayer = null;
                sf3Player = null;
                algorithmInterface = null;
                fieldGui.updateGameState(gameMenu());
                statusBar.setText("Start a new game! (Play/Play Against the machine)");
            }
        };
        fieldGui.getScene().doTask(r);
        /*
         * fieldGui.getScene().setLWJGLAccel(false);
         */
    }

    /**
     * Run the help
     */
    private void help() {
        cancelGame();
        try {
            fieldGui.popMessage((Display) Display._Display(getClass().getResource("images/cursor_copy.png"), null), "Quick help", UIMessage.INFO_TYPE);
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Si le fichier des scores existe, on l'efface! et on quitte...
     */
    private void exit() {
        System.exit(0);
    }

    /**
     * Play againt the machine.<br/>0 Creates the ServerAlgortihm, the
     * MachinePlayer and the Keyboard Player, and creates the listeners needed.
     */
    public void playAgainstMachine() {
        cancelGame(); /*
         * Cancel the game if there is a game running
         */
        /*
         * Thread t = new Thread(gameInterfaceCTM,
         */ Runnable r = new Runnable() {

            public void run() {
                if (!FieldGui.getPlayerModelsStack().isEmpty()) {
                    sAlg = new ServerAlgorithm(Sf3.this, Game.MODE_LOCAL | Game.MODE_VS_COMP, fieldGui, props);
                    algorithmInterface = sAlg.getAlgorithmInterface();
                    algorithmLayer = sAlg;
                    fieldGui.getScene().addBeyondOffscreen(algorithmLayer);
                    sAlg.setRenderingScene(fieldGui.getScene());
                    sAlg.addAlgorithmListener(fieldGui);
                    sAlg.addAlgorithmListener(statusBarAlgorithmListener);
                }
            }
        };/*
         * , "T-play against machine"); t.setPriority(Thread.MAX_PRIORITY);
         * t.start();
         */
        fieldGui.getScene().doTask(r);
    }

    /**
     * Play againt the machine.<br/>0 Creates the ServerAlgortihm, the
     * MachinePlayer and the Keyboard Player, and creates the listeners needed.
     */
    public void playAgainstHuman() {
        cancelGame(); /*
         * Cancel the game if there is a game running
         */
        /*
         * Thread t = new Thread(gameInterfaceCTM,
         */ Runnable r = new Runnable() {

            public void run() {
                if (!FieldGui.getPlayerModelsStack().isEmpty()) {
                    sAlg = new ServerAlgorithm(Sf3.this, Game.MODE_LOCAL | Game.MODE_VS_HUMAN, fieldGui, props);
                    algorithmInterface = sAlg.getAlgorithmInterface();
                    algorithmLayer = sAlg;
                    fieldGui.getScene().addBeyondOffscreen(algorithmLayer);
                    sAlg.setRenderingScene(fieldGui.getScene());
                    sAlg.addAlgorithmListener(fieldGui);
                    sAlg.addAlgorithmListener(statusBarAlgorithmListener);
                }
            }
        };/*
         * , "T-play against human"); t.setPriority(Thread.MAX_PRIORITY);
         * t.start();
         */
        fieldGui.getScene().doTask(r);
    }
    ServerAlgorithm sAlg = null;
    RemoteAlgorithm rAlg = null;
    /*
     * CoalescedThreadsMonitor gameInterfaceCTM = new
     * CoalescedThreadsMonitor("TG-game interface");
     */

    /**
     * Run Server mode.<br/> Waits for a client connection, and then creates the
     * ServerAlgortihm, the Keyboard Player, the RemotePlayer and creates the
     * listeners needed.
     */
    public void runServerMode() {
        cancelGame(); /*
         * Cancel the game is there is a game running
         */
        /*
         * Thread t = new Thread(gameInterfaceCTM,
         */ Runnable r = new Runnable() {

            public void run() {
                /**
                 * Wait for a client connection
                 */
                statusBar.setText("Waiting for a client to connect");
                try {
                    Socket socket = ServerConnectionDialog.getConnection(fieldGui);
                    if (socket == null) {
                        return;
                    }
                    if (!FieldGui.getPlayerModelsStack().isEmpty()) {
                        sAlg = new ServerAlgorithm(Sf3.this, Game.MODE_SERVER | Game.MODE_VS_HUMAN, fieldGui, props);
                        algorithmInterface = sAlg.getAlgorithmInterface();
                        algorithmLayer = sAlg;
                        fieldGui.getScene().addBeyondOffscreen(algorithmLayer);
                        sAlg.addAlgorithmListener(fieldGui);
                        sAlg.addAlgorithmListener(statusBarAlgorithmListener);
                        remotePlayer = new RemotePlayer(props, algorithmInterface, socket);
                        fieldGui.getScene().addBeyondOffscreen(remotePlayer);
                    }
                } catch (Exception e) {
                    System.err.println("runServerMode: " + e.toString());
                }
            }
        };
        fieldGui.getScene().doTask(r);
    }
    /*
     * ThreadWorks gamelocal = new ThreadWorks("thegame-local"), gameremote =
     * new ThreadWorks("thegame-remote");
     */

    /**
     * Run Client mode.<br/> Establish a connection with the server, and then
     * creates the RemoteAlgortihm, the Keyboard Player and creates the
     * listeners needed.
     */
    public void runClientMode() {
        cancelGame();
        Runnable r = new Runnable() {

            public void run() {
                /**
                 * Establish the connection with the server
                 */
                statusBar.setText("Waiting to establish a communication");

                try {
                    Socket socket = ClientConnectionDialog.getConnection(fieldGui);
                    if (socket == null) {
                        return;
                    }
                    if (!FieldGui.getPlayerModelsStack().isEmpty()) {
                        rAlg = new RemoteAlgorithm(Game.MODE_CLIENT | Game.MODE_VS_HUMAN, socket);
                        algorithmInterface = rAlg;
                        algorithmLayer = rAlg;
                        fieldGui.getScene().addBeyondOffscreen(algorithmLayer);
                        props = rAlg.getGameProperties();
                        fieldGui.setProps(props);
                        rAlg.addAlgorithmListener(fieldGui);
                        rAlg.addAlgorithmListener(statusBarAlgorithmListener);
                        fieldGui.getScene().addBeyondOffscreen(rAlg);
                    }
                } catch (Exception e) {
                    System.err.println("runClientMode: " + e.toString());
                }
            }
        };
        fieldGui.getScene().doTask(r);
    }

    public Component getRootComponent() {
        return applet;
    }

    public void setApplet(JApplet arg0) {
        applet = arg0;
    }

    public void destroy() {
        if (initialized) {
            applet.getContentPane().removeAll();
        }
        initialized = false;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public Runnable shutdownHook() {
        return null;
    }

    public DisplayInterface getSplash() {
        try {
            return Display._Display(getClass().getResource("/thegame/images/sf3thegame-logo-splash.png"), null);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public Vector<Action> getLoadLayers() {
        Vector<Action> loadLayers = FieldGui._getLoadLayers();
        loadLayers.addAll(Fighter.getLoadLayers());
        loadLayers.addAll(Sf3Player.getLoadLayers());
        loadLayers.add(loadLayer);
        return loadLayers;
    }

    /**
     * This AlgorithmListener is responsable to update the statusBar of Pong
     * when some change occurs in the game (started, paused, resumed, finished)
     */
    class StatusBarAlgorithmListener implements AlgorithmListener, RaSSetListener {

        long hash = System.nanoTime();

        public int hashCode() {
            return (int) hash;
        }

        public boolean equals(Object o) {
            return o == null ? false : o.hashCode() == hashCode();
        }

        /**
         * The ServerAlgorithm (or RemoteAlgorithm) calls this method when the
         * game starts
         *
         * @param startPlayer the player that has initially the ball. 0 and 1
         * for player 1 and 2 respectively
         * @param gameState the initial state of the game
         */
        public void gameStarted(int startPlayer, Game gameState) {
            String m = "Press 'P' to start";
            if (fieldGui instanceof FieldGui) {
                fieldGui.logLn(m);
            }
            statusBar.setText(m);
        }

        /**
         * The ServerAlgorithm (or RemoteAlgorithm) calls this method when the
         * game is paused
         */
        public void gamePaused(Game gameState) {
            String m = "Press 'P' to resume";
            if (fieldGui instanceof FieldGui) {
                fieldGui.logLn(m);
            }
            statusBar.setText(m);
        }

        /**
         * The ServerAlgorithm (or RemoteAlgorithm) calls this method when the
         * game is resumed
         */
        public void gameResumed(Game gameState) {
            String m = "Press 'P' to pause";
            if (fieldGui instanceof FieldGui) {
                fieldGui.logLn(m);
            }
            statusBar.setText(m);
        }

        /**
         * The ServerAlgorithm (or RemoteAlgorithm) calls this method after each
         * tick of the game
         *
         * @param gameState the actual state of the grame
         * @param ballBounced if true, the ball just touched the boundaries of
         * the field, or the rackets it could be used to play some sound
         */
        public void tickProcessed(Game gameState) {
            boolean error;
            String status = "";
            /*
             * Gestion des scores double[]scores = rASScore.readTheFile(); if
             * (gameState.state == Game.STATE_FINISHED) { if(rASScore.isFound())
             * error = rASScore.writeScores(gameState.winner + 1,true,scores);
             * else error = rASScore.writeScores(gameState.winner +
             * 1,false,scores); double[]leScore = rASScore.readTheFile();
             * if(leScore[0]==leScore[1]) status="Deuce"; else if
             * (leScore[0]<leScore[1]) status ="You lose"; else if
             * (leScore[0]>leScore[1]) status ="You win"; status += " (Score : "
             * + (int)leScore[0] + " - " + (int)leScore[1] + ")"; /* } status +=
             * " Fighters' overall speed: " + Sf3.this.props.fighterSpeed;
             * status += " overall weight: " + Sf3.this.props.fighterWeight;
             * status += " Gravity: " + Sf3.this.props.gravity;
             * statusBar.setText(status);
             */
        }
        /*
         * Gestion RaSSetListener. En fonction de ce que fait
         * ReadAndSaveSettings, la status bar informe
         */

        public void optionsSaved(String date) {
            String m = "Options saved to options data file.";
            if (fieldGui instanceof FieldGui) {
                fieldGui.logLn(date + " : " + m);
            }
            statusBar.setText(m);
        }

        public void optionsDeleted() {
            String m = "Option file deleted.";
            if (fieldGui instanceof FieldGui) {
                fieldGui.logLn(m);
            }
            statusBar.setText(m);
        }

        public void optionsLoaded(String date) {
            String m = "Last saved options loaded. (File created on " + date + ")";
            if (fieldGui instanceof FieldGui) {
                fieldGui.logLn(m);
            }
            statusBar.setText(m);
        }

        public void optionsNotFound() {
            String m = "No option file found.";
            if (fieldGui instanceof FieldGui) {
                fieldGui.logLn(m);
            }
            statusBar.setText(m);
        }

        public void gameCanceled(Game gameState) {
            String m = "The game has been canceled.";
            if (fieldGui instanceof FieldGui) {
                fieldGui.logLn(m);
            }
            statusBar.setText(m);
        }

        public void optionsError(Object e) {
            String m = "An error occurred with the options file, please check your settings ! caught error : " + e.toString();
            if (fieldGui instanceof FieldGui) {
                fieldGui.logLn(m);
            }
            statusBar.setText(m);
        }

        public void optionsAreSaving(String m) {
            if (fieldGui instanceof FieldGui) {
                fieldGui.logLn(m);
            }
            statusBar.setText(m);
        }

        public void optionsAreLoading(String m) {
            if (fieldGui instanceof FieldGui) {
                fieldGui.logLn(m);
            }
            statusBar.setText(m);
        }

        public void gameLoading(Game gameState) {
            statusBar.setText("Loading...");
        }
    }

    public JApplet getApplet() {
        return applet;
    }

    public boolean open(Object data) {
        String path = null;
        if (data instanceof java.util.List) {
            java.util.List<File> list = (java.util.List<File>) data;
            File o = list.get(0);
            path = o.getPath();
        }
        if (data instanceof String) {
            path = data.toString();
        }
        try {
            if (path instanceof String) {
                rASSet = new ReadAndSaveSettings(path, props);
                rASSet.addRaSSetListener((RaSSetListener) statusBarAlgorithmListener);
                GameProperties newProps = rASSet.loadOptions(null, 0);
                props = (newProps instanceof GameProperties) ? newProps : props;
                rASSet = new ReadAndSaveSettings(GamePropertiesDialog.SETTINGSPATH, props);
                rASSet.addRaSSetListener((RaSSetListener) statusBarAlgorithmListener);
                if (fieldGui instanceof FieldGui) {
                    fieldGui.setProps(props);
                }
                return true;
            } else if (data instanceof GameProperties) {
                rASSet = new ReadAndSaveSettings(GamePropertiesDialog.SETTINGSPATH, props = (GameProperties) data);
                rASSet.addRaSSetListener((RaSSetListener) statusBarAlgorithmListener);
                rASSet.saveOptions();
                if (fieldGui instanceof FieldGui) {
                    fieldGui.setProps(props);
                }
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{GameProperties._DATAFLAVOR, DataFlavor.javaFileListFlavor, DataFlavor.stringFlavor};
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        for (DataFlavor df : getTransferDataFlavors()) {
            if (df.equals(flavor)) {
                return true;
            }
        }
        return false;
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (isDataFlavorSupported(flavor)) {
            if (flavor.equals(DataFlavor.javaFileListFlavor) || flavor.equals(DataFlavor.stringFlavor)) {
                java.util.List<File> list = new Vector<File>();
                File f = new File("options.tmp");
                f.deleteOnExit();
                save(f.getPath());
                list.add(f);
                return flavor.equals(DataFlavor.javaFileListFlavor) ? list : f.getPath();
            }
        }
        throw new UnsupportedFlavorException(flavor);
    }

    public JMenu getActionMenu() {
        JMenu menu;
        JMenuItem menuItem;
        /*
         * The play menu
         */
        menu = new JMenu("Game");
        menuItem = new JMenuItem(new AbstractAction("Play Against the machine") {

            public void actionPerformed(
                    ActionEvent e) {
                Runnable r = new Runnable() {

                    public void run() {
                        playAgainstMachine();
                    }
                };
                gameMenuCommands.doLater(r);
            }
        });
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.ALT_MASK));
        menu.add(menuItem);
        menu.setMnemonic('y');
        menuItem = new JMenuItem(new AbstractAction("Run Server Mode") {

            public void actionPerformed(
                    ActionEvent e) {
                Runnable r = new Runnable() {

                    public void run() {
                        runServerMode();
                    }
                };
                gameMenuCommands.doLater(r);
            }
        });
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.ALT_MASK));
        menu.add(menuItem);
        menuItem = new JMenuItem(new AbstractAction("Connect to Server") {

            public void actionPerformed(
                    ActionEvent e) {
                Runnable r = new Runnable() {

                    public void run() {
                        runClientMode();
                    }
                };
                gameMenuCommands.doLater(r);
            }
        });
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.ALT_MASK));
        menu.add(menuItem);
        menuItem = new JMenuItem(new AbstractAction("Cancel Game") {

            public void actionPerformed(ActionEvent e) {
                Runnable r = new Runnable() {

                    public void run() {
                        cancelGame();
                    }
                };
                gameMenuCommands.doLater(r);
            }
        });
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, InputEvent.ALT_MASK));
        menu.add(menuItem);
        menuItem = new JMenuItem(new AbstractAction("Options") {

            public void actionPerformed(ActionEvent e) {
                Runnable r = new Runnable() {

                    public void run() {
                        options();
                    }
                };
                gameMenuCommands.doLater(r);
            }
        });
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.ALT_MASK));
        menu.add(menuItem);
        menu.addSeparator();
        menuItem = new JMenuItem(new AbstractAction("Exit") {

            public void actionPerformed(ActionEvent e) {
                Runnable r = new Runnable() {

                    public void run() {
                        exit();
                    }
                };
                gameMenuCommands.doLater(r);
            }
        });
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.ALT_MASK));
        menu.add(menuItem);
        menuItem = new JMenuItem(new AbstractAction("<Reset>") {

            public void actionPerformed(ActionEvent e) {
                Runnable r = new Runnable() {

                    public void run() {
                        JFCApplet._restart((JFCApplet) Sf3.this.getApplet());
                    }
                };
                gameMenuCommands.doLater(r);
            }
        });
        menu.add(menuItem);
        return menu;
    }
    ThreadWorks gameMenuCommands = new ThreadWorks("Sf3Menu");

    public JMenu getHelpActionMenu() {
        /*
         * The help menu
         */
        JMenu menu = new JMenu("?");
        JMenuItem menuItem = new JMenuItem(new AbstractAction("Help") {

            public void actionPerformed(ActionEvent e) {
                Runnable r = new Runnable() {

                    public void run() {
                        help();
                    }
                };
                gameMenuCommands.doLater(r);
            }
        });
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, InputEvent.SHIFT_MASK | InputEvent.ALT_MASK));
        menu.add(menuItem);
        menu.addSeparator();
        menuItem = new JMenuItem(new AbstractAction("Keyboards & Gamepads") {

            public void actionPerformed(ActionEvent e) {
                Sf3Player._refreshGamepads();
                Sf3Player._refreshKeyboard();
                Vector<Vector<String>> row = new Vector<Vector<String>>();
                Vector<String> v;
                SortedMap sortedProperties = new TreeMap();
                Enumeration<String> k = (Enumeration<String>) appProps.propertyNames();
                sortedProperties.putAll(appProps);
                for (Iterator it = sortedProperties.keySet().iterator(); it.hasNext();) {
                    v = new Vector<String>();
                    String property = it.next().toString();
                    v.add(property);
                    v.add(appProps.getProperty(property));
                    row.add(v);
                }
                v = new Vector<String>();
                v.add("property");
                v.add("property value");
                JTable table = new JTable(row, v);
                table.getColumn("property").setCellRenderer(JXAenvUtils2._getNewToolTipTableCellRenderer());
                table.getColumn("property value").setCellRenderer(JXAenvUtils2._getNewToolTipTableCellRenderer());
                JScrollPane sp = new JScrollPane(table);
                sp.setPreferredSize(new Dimension(300, 150));
                new UIMessage(false, sp, null);
            }
        });
        menu.add(menuItem);
        return menu;
    }
    public final static Properties appProps = new Properties();

    public boolean save(String file) {
        try {
            rASSet = new ReadAndSaveSettings(file, props);
            rASSet.addRaSSetListener((RaSSetListener) statusBarAlgorithmListener);
            rASSet.saveOptions();
            rASSet = new ReadAndSaveSettings(GamePropertiesDialog.SETTINGSPATH, props);
            rASSet.addRaSSetListener((RaSSetListener) statusBarAlgorithmListener);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
