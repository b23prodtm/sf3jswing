/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.system.input;

import java.awt.Component;
import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import net.java.games.input.Component.Identifier;
import net.java.games.input.Controller;
import net.sf.jiga.xtended.kernel.JXAenvUtils;
import net.sf.jiga.xtended.impl.game.GameActionLayer;
import net.sf.jiga.xtended.impl.game.RenderingScene;
import net.sf.jiga.xtended.impl.game.physics.Player;
import net.sf.jiga.xtended.kernel.DebugMap;

/**
 * It has a fast processing timed framework to listen to keyboard and gamepad
 * events throughout a dedicated KeyEventDispacher and the JInput API. This
 * class is intensively used by RenderingScene. You may want to use it
 * elsewhere, but if you don't use RenderingScene ONLY because ALL JINPUT-EVENTS
 * COME INTO {@linkplain RenderingScene#ctrlHDR} when the scene's laid out on a
 * Frame.
 *
 * @author www.b23prodtm.info
 */
public abstract class ControllersHandler extends GameActionLayer implements KeyEventDispatcher {

        /**
         * adds a new KeyEventDispatcher to the list. If the same instance has
         * been found in the existing list, it is left single. Hence, each
         * keyEventDispatcher must be consistent with
         * {@linkplain Object#equals()} (see javadoc for this !).
         *
         * @param ked the KeyEventDispatcher instance to add to the list
         */
        public void addKeyboardEventsDispatcher(KeyEventDispatcher ked) {
                if (!keds.contains(ked)) {
                        keds.add(ked);
                }
        }
        
        /**
         * the KeyEventDispatcher's list
         */
        protected final List<KeyEventDispatcher> keds = Collections.synchronizedList(new ArrayList<KeyEventDispatcher>());
        /**
         * removes the specified KeyEventDispatcher instance from the existing
         * list
         *
         * @param ked the KeyEventDispatcher to remove from the existing list
         *
         * @see #addKeyboardEventsDispatcher(KeyEventDispatcher)
         */
        @SuppressWarnings("empty-statement")
        public void removeKeyboardEventsDispatcher(KeyEventDispatcher ked) {
                while (keds.remove(ked));
        }

        /**
         * the key pressed switch
         */
        protected boolean keyPressed = false;

        public boolean isKeyPressed() {
                return keyPressed;
        }
        
    long hash = System.nanoTime();

    public ControllersHandler(String s) {
        super(s);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == null ? false : obj.hashCode() == hashCode();
    }

    @Override
    public int hashCode() {
        return (int) hash;
    }
    private Stack<KeyEventItem> keyboardEvents = new Stack<KeyEventItem>();

    class KeyEventItem {

        long time;
        KeyEvent e;

        public KeyEventItem(long time, KeyEvent e) {
            this.time = time;
            this.e = e;
        }
    }

    /**
     * polls for any KeyEvent's that may have been registered until now and
     * removes them from the KeyEvent's stack so that they are known to have
     * been processed further NOTICE : calling this method should not be called
     * by the user, unless the RenderingScene which this handler is registered
     * to is not operating.
     */
    public Collection<KeyEvent> pollKeyboard() {
        long time = System.nanoTime();
        /**
         * splice keyboard events map
         */
        List<KeyEvent> poll = new ArrayList<KeyEvent>();
        while (!keyboardEvents.isEmpty()) {
            if (keyboardEvents.peek().time > time) {
                break;
            }
            poll.add(keyboardEvents.pop().e);
        }
        return poll;
    }
    boolean AWTkeyboardDisabled = true;
    boolean gamepadDisabled = false;

    /**
     * Dispatches the event and returns true unless
     *  {@link #isAWTKeyboardDisabled() the keyboard is disabled}
     *
     * @see #pushKeyEvent(java.awt.event.KeyEvent)
     */
    @Override
    public final boolean dispatchKeyEvent(KeyEvent e) {
        if (AWTkeyboardDisabled) {
            return false;
        } else {
            pushKeyEvent(e);
        }
        return true;
    }

    /**
     * Alaways registers the event and sends it to the internal event queue.
     *
     * @see #pollKeyboard()
     */
    public final void pushKeyEvent(KeyEvent e) {
        keyboardEvents.add(0, new KeyEventItem(System.nanoTime(), e));
    }

    public boolean isGamepadDisabled() {
        return gamepadDisabled;
    }

    public boolean isAWTKeyboardDisabled() {
        return AWTkeyboardDisabled;
    }

    public void setGamepadDisabled(boolean gamepadDisabled) {
        this.gamepadDisabled = gamepadDisabled;
    }

    public void setAWTKeyboardDisabled(boolean keyboardDisabled) {
        this.AWTkeyboardDisabled = keyboardDisabled;
    }

    /**
     * @default false
     */
    public boolean isDisabled() {
        return AWTkeyboardDisabled;
    }

    /**
     * processes the GamePad event and sends it to {@link #pushKeyEvent(java.awt.event.KeyEvent)}
     * using the {@linkplain Player} mappings.
     * <br> this method does nothing if
     * {@link #isGamepadDisabled() gamepad is disabled}
     *
     * @param player is usually {@linkplain Player#ONE} or
     * {@linkplain Player#TWO} for left-side player or right-side player, resp.
     */
    public final void proceedGamePadEvents(Controller gamePad, Component source, Player.id player) {
        if (gamePad instanceof Controller) {
            if (gamepadDisabled) {
                JXAenvUtils.log("Gamepad is disabled", JXAenvUtils.LVL.SYS_NOT);
                return;
            }
            gamePad.poll();
            net.java.games.input.EventQueue eq = gamePad.getEventQueue();
            net.java.games.input.Event e = new net.java.games.input.Event();
            while (eq.getNextEvent(e)) {
                if (DebugMap._getInstance().isDebugLevelEnabled(RenderingScene.DBUG_RENDER)) {
                    System.out.print("@@@@@ gamepad event : ");
                }
                boolean b = _processGamepadEvent(source, player, e);
                if (DebugMap._getInstance().isDebugLevelEnabled(RenderingScene.DBUG_RENDER)) {
                    System.out.println((b ? " processed" : " not recognized") + " @@@@@");
                }
            }
        }
        /*}
         }*/
    }
    /**
     * the last polled JInput Event X- and Y-Axis values to find zero "release
     * button"
     */
    private static transient float lastPollX, lastPollY;

    /**
     * @param player is usually {@linkplain Player#ONE} or
     * {@linkplain Player#TWO} for left-side player or right-side player, resp.
     */
    private boolean _processGamepadEvent(Component source, Player.id player, net.java.games.input.Event e) {
        Player.key keyboardKey = null;
        long time = (long) ((float) e.getNanos() / 1000f);
        float val = Math.round(e.getValue());
        net.java.games.input.Component button = e.getComponent();
        String log = "";
        if (DebugMap._getInstance().isDebugLevelEnabled(RenderingScene.DBUG_RENDER)) {
            log = button.getIdentifier().getName();
        }
        int ID = (val == -1f || val == 1f) ? KeyEvent.KEY_PRESSED : KeyEvent.KEY_RELEASED;
        if (button.getIdentifier() instanceof net.java.games.input.Component.Identifier.Axis) {
            String direction = "";
            Identifier axisX = Player._gamepadCodeMap.get(Player.key.valueOf(player + "_backwards"));
            Identifier axisY = Player._gamepadCodeMap.get(Player.key.valueOf(player + "_up"));
            if (axisY instanceof Identifier && axisX instanceof Identifier) {
                if (axisX.getName().equals(button.getIdentifier().getName())) {
                    if (Player.id.ONE.equals(player)) {
                        direction = (val == -1f) ? "backwards" : ((val == 1f) ? "forwards" : ((lastPollX == -1f) ? "backwards" : ((lastPollX == 1f) ? "forwards" : "")));
                    } else if (Player.id.TWO.equals(player)) {
                        direction = (val == 1f) ? "backwards" : ((val == -1f) ? "forwards" : ((lastPollX == 1f) ? "backwards" : ((lastPollX == -1f) ? "forwards" : "")));
                    }
                    lastPollX = val;
                } else if (axisY.getName().equals(button.getIdentifier().getName())) {
                    direction = (val == 1f) ? "down" : ((val == -1f) ? "up" : ((lastPollY == 1f) ? "down" : ((lastPollY == -1f) ? "up" : "")));
                    lastPollY = val;
                }
                if (!direction.equals("")) {
                    keyboardKey = Player.key.valueOf(player + "_" + direction);
                    if (JXAenvUtils._debug) {
                        log = " Player  " + player + " gamepad " + keyboardKey + ((ID == KeyEvent.KEY_PRESSED) ? "pressed" : "released");
                    }
                }
            }
        } else {
            synchronized (Player._gamepadCodeMap) {
                    for (Player.key key : Player._gamepadCodeMap.keySet()) {
                            if (key.player.equals(player)) {
                                    Identifier name = Player._gamepadCodeMap.get(key);
                                    if (button.getIdentifier().getName().equals(name.getName())) {
                                            keyboardKey = key;
                                            break;
                                    }
                            }
                    }
            }
        }
        if (DebugMap._getInstance().isDebugLevelEnabled(RenderingScene.DBUG_RENDER)) {
            JXAenvUtils.log(log + " " + keyboardKey, JXAenvUtils.LVL.SYS_NOT);
        }
        if (keyboardKey instanceof Player.key) {
            KeyEventWrapper kew = new KeyEventWrapper(time, Player._keyCodeMap.get(keyboardKey), ID);
            pushKeyEvent(new KeyEvent(source, ID, time, 0, kew._keyCode, KeyEvent.CHAR_UNDEFINED));
            return true;
        } else {
            return false;
        }
    }

    /**
     * runs the keyboard process. {@linkplain #pollKeyboard()} is firstly run to
     * gather registered KeyEvents and send them to
     * {@linkplain #processOneKeyboardEvent(KeyEvent)}
     */
    protected final void processKeyboardEvents() {
        for (KeyEvent e : pollKeyboard()) {
            processOneKeyboardEvent(e);
        }
    }

    /**
     * must be implemented to process action for the specified KeyEvent as would
     * a KeyEventDispatcher do.
     */
    protected abstract void processOneKeyboardEvent(KeyEvent e);

    @Override
    protected void performTask() {
        super.performTask();
        processKeyboardEvents();
    }
}
