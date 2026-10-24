/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thegame;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.*;
import javax.swing.AbstractAction;
import javax.swing.Action;
import net.java.games.input.*;
import net.sf.jiga.xtended.impl.game.InteractiveModel;
import net.sf.jiga.xtended.impl.game.RenderingScene;
import net.sf.jiga.xtended.impl.game.RenderingSceneComponent;
import net.sf.jiga.xtended.impl.game.XtendedModel;
import net.sf.jiga.xtended.impl.game.physics.Player;
import net.sf.jiga.xtended.impl.game.physics.Player.id;
import net.sf.jiga.xtended.kernel.Threaded;
import thegame.comp.Fighter;

/**
 * Sf3Player is the class that interfaces the players and computer players in THE GAME.
 * Each player receives a JXA LogicalSystem impl. to manage the linked XtendedModel instance.
 * <br/><img src="doc-files/ai-model-diagram.jpg"/>
 * <br/><img src="doc-files/ai-model-diagram.jpg"/>
 * @author www.b23prodtm.info
 */
public abstract class Sf3Player implements Threaded, GameStateHandler, AlgorithmListener, RenderingSceneComponent {

    long hash = System.nanoTime();
    protected XtendedModel playerModel = null;
    protected int playerNum;
    protected Fighter playerPhysics = null;
    public final FieldGui gui;

    public Sf3Player(int playerNum, FieldGui gui) {
        this.playerNum = playerNum;
        this.gui = gui;
    }

    public FieldGui getGui() {
        return gui;
    }

    public Fighter getPlayerPhysics() {
        return playerPhysics;
    }

    public XtendedModel getPlayerModel() {
        return playerModel;
    }

    public int getPlayerNum() {
        return playerNum;
    }

    public int hashCode() {
        return (int) hash;
    }

    public boolean equals(Object o) {
        return o == null ? false : o.hashCode() == hashCode();
    }
    public static final SortedMap<Integer, Controller> _availableGamepads = Collections.synchronizedSortedMap(new TreeMap<Integer, Controller>());

    public static Vector<Action> getLoadLayers() {
        Vector<Action> loadLayers = new Vector<Action>();
        loadLayers.add(new AbstractAction("players gamepads...") {

            public void actionPerformed(ActionEvent e) {
                ControllerEnvironment.getDefaultEnvironment().addControllerListener(new ControllerListener() {

                    public void controllerRemoved(ControllerEvent arg0) {
                        _refreshGamepads();
                    }

                    public void controllerAdded(ControllerEvent arg0) {
                        _refreshGamepads();
                    }
                });
                _refreshGamepads();
            }
        });
        loadLayers.add(new AbstractAction("players keyboard...") {

            public void actionPerformed(ActionEvent e) {
                _refreshKeyboard();
            }
        });
        return loadLayers;
    }

    public static void _refreshKeyboard() {
        Enumeration<String> properties = (Enumeration<String>) Sf3.appProps.propertyNames();
        while (properties.hasMoreElements()) {
            String property = properties.nextElement();
            if (property.startsWith("Keyboard")) {
                Sf3.appProps.remove(property);
            }
        }
        synchronized (Fighter._keyCodeMap) {
            for (Player.key actionName : Fighter._keyCodeMap.keySet()) {
                Sf3.appProps.setProperty("Keyboard : " + actionName, KeyEvent.getKeyText(Fighter._keyCodeMap.get(actionName)));
            }
        }
    }

    public static void _refreshGamepads() {
        Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
        Enumeration<String> properties = (Enumeration<String>) Sf3.appProps.propertyNames();
        while (properties.hasMoreElements()) {
            String property = properties.nextElement();
            if (property.startsWith("JInput")) {
                Sf3.appProps.remove(property);
            }
        }
        Sf3.appProps.setProperty("JInput controllers", controllers.length + " controller(s)");
        _availableGamepads.clear();
        int i = 0;
        for (Controller controller : controllers) {
            if (controller.getType().equals(Controller.Type.GAMEPAD) || controller.getType().equals(Controller.Type.STICK)) {
                _availableGamepads.put(i++, controller);
            }
        }
        Sf3.appProps.setProperty("JInput gamepads and sticks", _availableGamepads.size() + " controller(s)");
        synchronized (_availableGamepads) {
            for (Integer cnum : _availableGamepads.keySet()) {
                Controller c = _availableGamepads.get(cnum);
                Sf3.appProps.setProperty("JInput{" + cnum + "} name", c.getName());
                Sf3.appProps.setProperty("JInput{" + cnum + "} type", c.getType().toString());
                Sf3.appProps.setProperty("JInput{" + cnum + "} port", c.getPortType().toString());
                for (Component comp : c.getComponents()) {
                    Sf3.appProps.setProperty("JInput{" + cnum + "} comp. id " + comp.getIdentifier().getName(), comp.getName());
                }
            }
        }
        synchronized (Fighter._gamepadCodeMap) {
            for (Player.key actionName : Fighter._gamepadCodeMap.keySet()) {
                Sf3.appProps.setProperty("Gamepad : " + actionName, Fighter._gamepadCodeMap.get(actionName).getName());
            }
        }
    }

    public void updateGameState(Game gs) {
        if ((gs.state & Game._bits._getMask(Game._STATE) & (Game.STATE_RUNNING | Game.STATE_PAUSED)) != 0) {
            playerPhysics = gs.fighters[playerNum];
            playerModel = playerPhysics.getModel();
            if (playerModel instanceof InteractiveModel && this instanceof KeyboardPlayer) {
                if (_availableGamepads.containsKey(playerNum)) {
                    playerModel.setGamePadController(_availableGamepads.get(playerNum),(id) playerModel.getAttribute("player"));
                }
            } else if (playerModel instanceof InteractiveModel) {
                playerModel.setGamePadController(null,(id) playerModel.getAttribute("player"));
            }
        } else if ((gs.state & Game._bits._getMask(Game._STATE) & (Game.STATE_FINISHED)) != 0) {
            if (playerModel instanceof InteractiveModel) {
                playerModel.setGamePadController(null,(id) playerModel.getAttribute("player"));
            }
            playerPhysics = null;
            playerModel = null;
        }
    }

    public Game requestGameState() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void gameLoading(Game gameState) {
        updateGameState(gameState);
    }

    public void gameCanceled(Game gameState) {
        updateGameState(gameState);
    }

    public void gamePaused(Game gameState) {
        updateGameState(gameState);
    }

    public void gameResumed(Game gameState) {
        updateGameState(gameState);
    }

    public void gameStarted(int startPlayer, Game gameState) {
        updateGameState(gameState);
    }

    public void tickProcessed(Game gameState) {
        if (rs instanceof RenderingScene) {
            setMultiThreadingEnabled(rs.isMultiThreadingEnabled());
        }
        updateGameState(gameState);
        if (playerPhysics instanceof Player) {
            if (playerPhysics.getModel().isLoaded()) {
                if (multiThreading) {
                    try {
                        Thread t = new Thread(playerPhysics);
                        t.start();
                        t.join();
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    playerPhysics.run();
                }
            }
        }
    }

    public void setThreadGroup(ThreadGroup arg0) {
    }

    public ThreadGroup getThreadGroup() {
        return Thread.currentThread().getThreadGroup();
    }
    private boolean multiThreading = false;

    public boolean isMultiThreadingEnabled() {
        return multiThreading;
    }

    public void setMultiThreadingEnabled(boolean arg0) {
        multiThreading = arg0;
    }
    private RenderingScene rs = null;

    public RenderingScene getRenderingScene() {
        return rs;
    }

    public void setRenderingScene(RenderingScene arg0) {
        rs = arg0;
    }

    public boolean isHardwareAccel() {
        return gui.isLWJGLRendering();
    }

    public void setHardwareAccel(boolean arg0) {
    }

    public String toString() {
        return getClass().getName() + " player " + playerNum;
    }
}
