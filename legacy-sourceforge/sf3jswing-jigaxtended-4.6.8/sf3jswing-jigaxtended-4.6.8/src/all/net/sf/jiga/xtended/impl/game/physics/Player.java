/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.game.physics;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import net.sf.jiga.xtended.impl.game.InteractiveModel;
import net.java.games.input.*;
import net.sf.jiga.xtended.impl.game.Model;
import net.sf.jiga.xtended.impl.game.XtendedModel;

/**
 * This class implements the Runnable interface to manage the Player inputs states at runtime.
 * defined two finals : {@linkplain id#ONE} {@linkplain id#TWO} to identify players.
 * @author www.b23prodtm.info
 */
public abstract class Player implements Runnable {

    /** the key codes map, bounding the keyboard to identifiers*/
    public final static Map<key, Integer> _keyCodeMap = Collections.synchronizedMap(new Hashtable<key, Integer>());
    /** the map for the reversed axis on X*/
    public static Map<id, Boolean> _reversedAxisX = Collections.synchronizedMap(new Hashtable<id, Boolean>());
    /** the map for the reversed axis on Y*/
    public static Map<id, Boolean> _reversedAxisY = Collections.synchronizedMap(new Hashtable<id, Boolean>());

    /** returns the current axis key code against the current status of the reversed axis maps for the specified key code
    @param player the player identifier ({@linkplain id#ONE}, {@linkplain id#TWO})
    @param keyCode the key code that might be reversed
    @return the corresponding key code for reverse (e.g. specifying VK_LEFT can return VK_RIGHT for reverse)
     * or the same keyCode if it is not mapped for reverse
    @see #_isReverseAxisXEnabled(String)
    @see #_isReverseAxisYEnabled(String)*/
    public static int _checkAxisKeyCode(id player, int keyCode) {
        int rev = keyCode;
        if (player != null) {
            if (_isReverseAxisXEnabled(player)) {
                if (keyCode == _keyCodeMap.get(key.valueOf(player.name() + "_backwards"))) {
                    rev = _keyCodeMap.get(key.valueOf(player.name() + "_forwards"));
                } else if (keyCode == _keyCodeMap.get(key.valueOf(player.name() + "_forwards"))) {
                    rev = _keyCodeMap.get(key.valueOf(player.name() + "_backwards"));
                }
            } else if (_isReverseAxisYEnabled(player)) {
                if (keyCode == _keyCodeMap.get(key.valueOf(player.name() + "_up"))) {
                    rev = _keyCodeMap.get(key.valueOf(player.name() + "_down"));
                } else if (keyCode == _keyCodeMap.get(key.valueOf(player.name() + "_down"))) {
                    rev = _keyCodeMap.get(key.valueOf(player.name() + "_up"));
                }
            }
        }
        return rev;
    }

    public static enum id {

        ONE(), TWO;
    }

    public static enum key {

        ONE_forwards(id.ONE), ONE_backwards(id.ONE),
        ONE_down(id.ONE), ONE_up(id.ONE),
        TWO_forwards(id.TWO), TWO_backwards(id.TWO),
        TWO_down(id.TWO), TWO_up(id.TWO),
        ONE_LP(id.ONE), ONE_MP(id.ONE), ONE_HP(id.ONE),
        ONE_LK(id.ONE), ONE_MK(id.ONE), ONE_HK(id.ONE),
        TWO_LP(id.TWO), TWO_MP(id.TWO), TWO_HP(id.TWO),
        TWO_LK(id.TWO), TWO_MK(id.TWO), TWO_HK(id.TWO),
        ONE_start(id.ONE), ONE_back(id.ONE),
        TWO_start(id.TWO), TWO_back(id.TWO);
        public final id player;

        key(id player) {
            this.player = player;
        }
    }

    public static void addKeyMap(key map, int keyCode) {
        _keyCodeMap.put(map, keyCode);
    }


    static {
        _reversedAxisX.put(id.ONE, false);
        _reversedAxisX.put(id.TWO, false);
        _reversedAxisY.put(id.ONE, false);
        _reversedAxisY.put(id.TWO, false);
        addKeyMap(key.ONE_forwards, KeyEvent.VK_RIGHT);
        addKeyMap(key.ONE_down, KeyEvent.VK_DOWN);
        addKeyMap(key.ONE_backwards, KeyEvent.VK_LEFT);
        addKeyMap(key.ONE_up, KeyEvent.VK_UP);
        addKeyMap(key.TWO_forwards, KeyEvent.VK_A);
        addKeyMap(key.TWO_down, KeyEvent.VK_S);
        addKeyMap(key.TWO_backwards, KeyEvent.VK_D);
        addKeyMap(key.TWO_up, KeyEvent.VK_W);
        addKeyMap(key.ONE_start, KeyEvent.VK_1);
        addKeyMap(key.ONE_back, KeyEvent.VK_2);
        addKeyMap(key.TWO_start, KeyEvent.VK_3);
        addKeyMap(key.TWO_back, KeyEvent.VK_4);
        addKeyMap(key.ONE_LP, /*KeyEvent.VK_SPACE);*/ KeyEvent.VK_U);
        addKeyMap(key.ONE_MP, /*KeyEvent.VK_ALT);*/ KeyEvent.VK_I);
        addKeyMap(key.ONE_HP, /*KeyEvent.VK_CONTROL);*/ KeyEvent.VK_O);
        addKeyMap(key.ONE_LK, /*KeyEvent.VK_BACK_SPACE);*/ KeyEvent.VK_J);
        addKeyMap(key.ONE_MK, /*KeyEvent.VK_ENTER);*/ KeyEvent.VK_K);
        addKeyMap(key.ONE_HK, /*KeyEvent.VK_SHIFT);*/ KeyEvent.VK_L);
        addKeyMap(key.TWO_LP, KeyEvent.VK_R);
        addKeyMap(key.TWO_MP, KeyEvent.VK_T);
        addKeyMap(key.TWO_HP, KeyEvent.VK_Z);
        addKeyMap(key.TWO_LK, KeyEvent.VK_F);
        addKeyMap(key.TWO_MK, KeyEvent.VK_G);
        addKeyMap(key.TWO_HK, KeyEvent.VK_H);
    }
    /** the game pad code map, keys identifies a game pad button (as defined by the JInput API) as the value for the mapping*/
    public final static Map<key, Component.Identifier> _gamepadCodeMap = Collections.synchronizedMap(new Hashtable<key, Component.Identifier>());

    public static void addPadMap(key map, Component.Identifier padButtonAxis) {
        _gamepadCodeMap.put(map, padButtonAxis);
    }


    static {
        addPadMap(key.ONE_forwards, Component.Identifier.Axis.X);
        addPadMap(key.ONE_down, Component.Identifier.Axis.Y);
        addPadMap(key.ONE_backwards, Component.Identifier.Axis.X);
        addPadMap(key.ONE_up, Component.Identifier.Axis.Y);
        addPadMap(key.TWO_forwards, Component.Identifier.Axis.X);
        addPadMap(key.TWO_down, Component.Identifier.Axis.Y);
        addPadMap(key.TWO_backwards, Component.Identifier.Axis.X);
        addPadMap(key.TWO_up, Component.Identifier.Axis.Y);
        addPadMap(key.ONE_start, Component.Identifier.Button._9);
        addPadMap(key.ONE_back, Component.Identifier.Button._10);
        addPadMap(key.TWO_start, Component.Identifier.Button._9);
        addPadMap(key.TWO_back, Component.Identifier.Button._10);
        addPadMap(key.ONE_LP, Component.Identifier.Button._0);
        addPadMap(key.ONE_MP, Component.Identifier.Button._1);
        addPadMap(key.ONE_HP, Component.Identifier.Button._2);
        addPadMap(key.ONE_LK, Component.Identifier.Button._3);
        addPadMap(key.ONE_MK, Component.Identifier.Button._4);
        addPadMap(key.ONE_HK, Component.Identifier.Button._5);
        addPadMap(key.TWO_LP, Component.Identifier.Button._0);
        addPadMap(key.TWO_MP, Component.Identifier.Button._1);
        addPadMap(key.TWO_HP, Component.Identifier.Button._2);
        addPadMap(key.TWO_LK, Component.Identifier.Button._3);
        addPadMap(key.TWO_MK, Component.Identifier.Button._4);
        addPadMap(key.TWO_HK, Component.Identifier.Button._5);
    }

    /** returns true or false, whether the X-Axis will be reversed when calling {@linkplain #_checkAxisKeyCode(String, int)}*/
    public static boolean _isReverseAxisXEnabled(id player) {
        if (player != null) {
            return _reversedAxisX.get(player);
        } else {
            return false;
        }
    }

    /** returns true or false, whether the Y-Axis will be reversed when calling {@linkplain #_checkAxisKeyCode(String, int)}*/
    public static boolean _isReverseAxisYEnabled(id player) {
        if (player != null) {
            return _reversedAxisY.get(player);
        } else {
            return false;
        }
    }

    /** set reverse dis/enabled for the X-Axis and the specified playerId
    @see #_isReverseAxisXEnabled(String)*/
    public static void _reverseAxisXEnabled(id player, boolean reverse) {
        _reversedAxisX.put(player, reverse);
        System.out.println(Player.class.getName() + " has" + ((_reversedAxisX.get(player)) ? "" : " not") + " reversed PLAYER " + player + " X-axis");
    }

    /** set reverse dis/enabled for the Y-Axis and the specified playerId
    @see #_isReverseAxisYEnabled(String)*/
    public static void _reverseAxisYEnabled(id player, boolean reverse) {
        _reversedAxisY.put(player, reverse);
        System.out.println(Player.class.getName() + " has" + ((_reversedAxisY.get(player)) ? "" : " not") + " reversed PLAYER " + player + " Y-axis");
    }
    /** the last time the run() method was called */
    protected long last_tick = 0L;
    /** the time the run() method is called*/
    protected long current_tick = 0L;
    /** the bounded XtendedModel*/
    protected XtendedModel model;
    /** the current position */
    protected final Point2D pos = new Point(0, 0);
    protected String modelName;

    /** creates a new instance
    @param model the bounded XtendedModel instance
    @param pos the starting position */
    public Player(XtendedModel model, Point2D pos) {
        this.pos.setLocation(pos);
        this.model = model;
        modelName = "" + model.getAttribute("name");
    }

    /** sets up the state of this Player instance
    @param the model
    @param pos the player's position 
    public void setState(XtendedModel model, Point2D pos) {
    this.model = model;
    this.pos.setLocation(pos);
    }*/
    /** registers the current tick as the {@linkplain #getLast_tick() last tick value}
     */
    public void setProcessed_tick() {
        current_tick = System.currentTimeMillis();
        System.out.println(getClass().getName() + this + " is being run.");
        last_tick = current_tick;
    }

    /** resets the ticker
    @see #last_tick */
    public void resetTicker() {
        last_tick = 0L;
    }

    /** returns the bounding Rectangle according to the values set as INSETS for the current XtendedModel and the current position.
    @see InteractiveModel#getInsetsSize()
    @return the bounding Rectangle according to the values set as INSETS for the current XtendedModel*/
    public Rectangle2D getBounds2D(int resolution) {
        return new Rectangle((int) pos.getX() - (int) ((float) model.getInsetSize(resolution).width / 2.0f), (int) pos.getY() - model.getInsetSize(resolution).height, model.getInsetSize(resolution).width, model.getInsetSize(resolution).height);
    }

    /** returns a String identifying this instance
    @return a String identifying this instance
    @see Model#getAttribute(String)*/
    public String toString() {
        return getClass().getName() + "-" + modelName;
    }
}
