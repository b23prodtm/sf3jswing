/*
 * InteractiveModel.java
 *
 * Created on 30 mai 2007, 05:09
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.game;

import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.Map.Entry;
import java.util.*;
import net.java.games.input.Controller;
import net.sf.jiga.xtended.impl.Animation;
import net.sf.jiga.xtended.impl.game.physics.Player;
import net.sf.jiga.xtended.impl.system.input.ControllersHandler;
import net.sf.jiga.xtended.impl.system.input.Interactive;
import net.sf.jiga.xtended.impl.system.input.KeyEventWrapper;
import net.sf.jiga.xtended.kernel.JXAenvUtils;
import net.sf.jiga.xtended.kernel.Monitor;

/** the InteractiveModel class allows the game to know how to gather multiple Animation's 
 * into one single instance and to link them with the user input actions.
 * Indeed the KeyEventWrapper's map links Animation's indexes to KeyEvents or KeyEvent's sequences. Gamepads are handled by {@linkplain ControllersHandler}. One instance of this class must be added to a KeyboardFocusManager or linked with a gamepad to make user action linked correctly. the Player class defines all user KeyEvent's and gamepad Controller Components. Everything gets synch'ed throughout the CoalescedThreadsMonitor instances.
 * If you want to change the input keys or gamepads buttons, set a specific player number for this instance by {@linkplain #setAttribute(String, Serializable) setAttribute("player", Player.ONE) or setAttribute("player", Player.TWO)} or {@linkplain #setGamePadController(Controller, String)} and go to {@linkplain Player} to modify the mappings !
 * @author www.b23prodtm.info
 */
public class InteractiveModel extends Model implements Externalizable, Interactive {

    /** serial version UID */
    private static final long serialVersionUID = 2323;
    /** the gamepad Controller instance associated to this InteractiveModel, default is always null*/
    private transient Controller gamePad = null;
    /** the DataFlavor DataFlavor.javaSerializedObjectMimeType */
    public static final String _MIME_TYPE = DataFlavor.javaSerializedObjectMimeType;
    /** the InteractiveModel mime-type extensions*/
    public static final String[] _MIME_EXT = new String[]{"mc4", "MC4"};
    /** the DataFlavor's used for InteractiveModel instances */
    public static final DataFlavor _dataFlavor = new DataFlavor(InteractiveModel.class, "sf3jswing Interactive Model");
    
    static {
        _storeMIMETYPES(_MIME_TYPE, _MIME_EXT);
    }
    /** the treaded events sequence map*/
    private transient Map<Long, KeyEventWrapper> threadedEventsSequence;
    /** the filter for keyboard events
    private transient SortedMap<Long, KeyEventWrapper> filteredEventsSequence;*/
    /** the pressed key events map*/
    private transient Map<Long, KeyEventWrapper> pressedKeys;
    /** the key events map */
    public transient Map<KeyEventWrapper, String> keyEvents;
    /** the key events backing map */
    private HashMap<KeyEventWrapper, String> _keyEvents;
    /** the highest key events sequence size*/
    int highestKeysSequence = 0;
    /** the highest key events sequence duration */
    long highestKeysSequenceTiming = 0;
    /** the dispatch monitor switch */
    private transient boolean dispatching = false;
    /** the process monitor switch */
    private transient boolean processing = false;
    /** the dispatch monitor */
    private transient Monitor dispatchMonitor;
    /** the process monitor */
    private transient Monitor processMonitor;

    /** creates a new instance
    
    @param name the name to identify the model, it is used in the animation path names, if {@linkplain #isInnerResourceModeEnabled()} is enabled, then only reachable resources paths may be available.
    @param res the resolution identifier for the Animation's and Sprite's    
    @param floor the graphical height in px to get the floor in the Animation's and Sprite's    
    @param spriteDimension the dimension of the Animation's and Sprite's    
    @param spriteInsideDimension the dimension of the Animation's and Sprite's pictures held within centered     
    @param frames the Animation's frames indexes [0[start][end]][1[start][end]]...    
    @param reversedAnims the Animation's that must be reversed must be identified by a String link id and gathered here    
    @param sfxRsrcs the sounds files to associate to this InteractiveModel    
    @param keyEvents the key events map to initially associate to this InteractiveModel, String identifiers must be used to identify the Animation's to map    
     */
    public InteractiveModel(String name, int res, int floor, Dimension spriteDimension, Dimension spriteInsideDimension, int[][] frames, String[] reversedAnims, String[] sfxRsrcs, HashMap<KeyEventWrapper, String> keyEvents) {
        super(name, res, floor, spriteDimension, spriteInsideDimension, frames, reversedAnims, sfxRsrcs);
        this.keyEvents = Collections.synchronizedMap(_keyEvents = keyEvents);
        highestKeysSequence = highestKeysSequence(this.keyEvents);
        highestKeysSequenceTiming = highestKeysSequenceTiming(this.keyEvents);
        /** rounding 9 ms of dispatching (about HDD average speed)*/
        resetKeyEventsMaps((int) Math.round(highestKeysSequenceTiming / 9.));
        setGroupMonitor(new Monitor(), new Monitor(), new Monitor(), new Monitor(),new Monitor());
    }

    /** resets the key events map     
     */
    private void resetKeyEventsMaps(int bufferSize) {
        pressedKeys = Collections.synchronizedMap(new Hashtable<Long, KeyEventWrapper>(bufferSize));
        threadedEventsSequence = Collections.synchronizedMap(new Hashtable<Long, KeyEventWrapper>(bufferSize));
    }
    
    public InteractiveModel() {
        this("unknown", HIGH, 0, new Dimension(), new Dimension(), new int[][]{}, new String[]{}, new String[]{});
    }

    /** creates a new instance with no associated key events map 
    
    @param name the name to identify the model, it is used in the animation path names, if {@linkplain #isInnerResourceModeEnabled()} is enabled, then only reachable resources paths may be available.
    @param res the resolution identifier for the Animation's and Sprite's    
    @param floor the graphical height in px to get the floor in the Animation's and Sprite's    
    @param spriteDimension the dimension of the Animation's and Sprite's    
    @param spriteInsideDimension the dimension of the Animation's and Sprite's pictures held within centered     
    @param frames the Animation's frames indexes [0[start][end]][1[start][end]]...    
    @param reversedAnims the Animation's that must be reversed must be identified by a String link id and gathered here    
    @param sfxRsrcs the sounds files to associate to this InteractiveModel     
     */
    public InteractiveModel(String name, int res, int floor, Dimension spriteDimension, Dimension spriteInsideDimension, int[][] frames, String[] reversedAnims, String[] sfxRsrcs) {
        this(name, res, floor, spriteDimension, spriteInsideDimension, frames, reversedAnims, sfxRsrcs, new HashMap<KeyEventWrapper, String>());
    }

    /** calculates the highest key events sequence for this InteractiveModel     
     * @param keyEvents the key events map to make the calculation    
    @return the length of the highest sequence contained in the key events map*/
    public static int highestKeysSequence(Map<KeyEventWrapper, String> keyEvents) {
        int highestKeysSequence = 0;
        Map<KeyEventWrapper, String> _keyEvents = Collections.synchronizedMap(keyEvents);
        Set<KeyEventWrapper> set = _keyEvents.keySet();
        synchronized (_keyEvents) {
            for (Iterator<KeyEventWrapper> i = set.iterator(); i.hasNext();) {
                int n = i.next().get_sequence().size();
                if (n > highestKeysSequence) {
                    highestKeysSequence = n;
                }
            }
        }
        return highestKeysSequence;
    }

    /** calculates the highest key events sequence duration for this InteractiveModel
    
    @param keyEvents the key events map to make the calculation
    
    @return the time of the highest seuqnece contained int the key events map
    
    @see KeyEventWrapper#TIMING_DEFAULT*/
    public static long highestKeysSequenceTiming(Map<KeyEventWrapper, String> keyEvents) {
        long highestKeysSequenceTiming = 0;
        Map<KeyEventWrapper, String> _keyEvents = Collections.synchronizedMap(keyEvents);
        Set<KeyEventWrapper> set = _keyEvents.keySet();
        synchronized (_keyEvents) {
            for (Iterator<KeyEventWrapper> i = set.iterator(); i.hasNext();) {
                long n = i.next().get_timing();
                if (n > highestKeysSequenceTiming) {
                    highestKeysSequenceTiming = n;
                }
            }
        }
        return highestKeysSequenceTiming;
    }

    /***/
    private KeyEventWrapper processMatcher(SortedMap<Long, KeyEventWrapper> filteredEventsSequence) {
        KeyEventWrapper match = null;
        synchronized (filteredEventsSequence) {
            SortedMap<Long, KeyEventWrapper> tail = filteredEventsSequence;
            while (!tail.isEmpty()) {
                Vector<KeyEventWrapper> seq = new Vector(tail.values());
                KeyEventWrapper combo = new KeyEventWrapper(seq);
                match = matchKeyEvents(combo);
                if (match instanceof KeyEventWrapper) {
                    break;
                } else {
                    tail = filteredEventsSequence.tailMap(tail.firstKey() + 1);
                }
            }
            return match;
        }
    }

    /** filters the threaded key events sequence to return the correct key events sequence at specified filtering timestamp. all key events that are older to the specified timestamp are discarded from the threead events map if the grab argument is set to true.
    
    @param filterTime the timestamp that will define the low-end of the sequence
    
    @param gfab true or false to discard all old events from the threaded events map
    
    @return the filtered map has the keys "time" and "event" mapping the KeyEventWrapper's timestamps and sequences, resp.
    
    @see #highestKeysSequenceTiming*/
    private SortedMap<Long, KeyEventWrapper> filterKeysSequence(Map<Long, KeyEventWrapper> sequence, long filterTime) {
        if (isDebugEnabled()) {
            System.out.println(JXAenvUtils.log("filter keys", JXAenvUtils.LVL.APP_NOT));
        }
        /* filter events posted with the desired timing e.g. 3 sec buffer*/
        long now = filterTime;
        long past = now - highestKeysSequenceTiming;
        SortedMap<Long, KeyEventWrapper> filteredEventsSequence = Collections.synchronizedSortedMap(new TreeMap<Long, KeyEventWrapper>());
        filteredEventsSequence.putAll(sequence);
        Vector<Long> removals = new Vector<Long>();
        synchronized (filteredEventsSequence) {
            for (Iterator<Long> it = filteredEventsSequence.keySet().iterator(); it.hasNext();) {
                Long time = it.next();
                if (time > now || time < past) {
                    removals.add(time);
                }
            }
            for (Long l : removals) {
                filteredEventsSequence.remove(l);
            }
        }
        /* add currently pressed keys */
        synchronized (pressedKeys) {
            for (Iterator<Map.Entry<Long, KeyEventWrapper>> i = pressedKeys.entrySet().iterator(); i.hasNext();) {
                Map.Entry<Long, KeyEventWrapper> entry = i.next();
                KeyEventWrapper kew = entry.getValue();
                kew.consumeReset();
                filteredEventsSequence.put(entry.getKey(), kew);
            }
        }
        if (isDebugEnabled()) {
            System.out.println(JXAenvUtils.log("pressed keys added = " + pressedKeys.size(), JXAenvUtils.LVL.APP_NOT));
            System.out.println(JXAenvUtils.log("filter keys end, " + filteredEventsSequence.size() + " events in sequence", JXAenvUtils.LVL.APP_NOT));
        }
        return filteredEventsSequence;
    }

    /** returns a singleton map that has the key True if the specified KeyEventWrapper matches one of the key events mapping or if the current combo sequence matches, too. 
     * otherwise the singleton map returns a key False mapping the same specified KeyEventWrapper.     
    @param readKew represents a sequence of keyEvents
     * @return a singleton map with the key True or False whether something has been matched or not, resp. 
    
    @see #keyEvents*/
    private KeyEventWrapper matchKeyEvents(KeyEventWrapper readKew) {
        KeyEventWrapper match = null;
        if (!readKew.isConsumed()) {
            /** find direct link to an Animation for the specified sequence (many-to-one)*/
            if (keyEvents.containsKey(readKew)) {
                readKew.consume();
                match = readKew;
            }
            /** look for the oldest unconsumed read keyEvent in the specified sequence (one-to-one)*/
            if (match == null) {
                for (Iterator<KeyEventWrapper> it = readKew.get_sequence().iterator(); it.hasNext();) {
                    KeyEventWrapper rKew = it.next();
                    if (!rKew.isConsumed()) {
                        if (keyEvents.containsKey(rKew)) {
                            rKew.consume();
                            match = rKew;
                        }
                        break;
                    }
                }
            }
        }
        if (match instanceof KeyEventWrapper) {
            if (isDebugEnabled()) {
                System.out.println(getClass().getSimpleName() + " " + id
                        + " has matched " + (match.get_sequence().isEmpty() ? (match.isBlankEvent() ? 0 : 1) : match.get_sequence().size())
                        + " keys to animate with " + match + ".");
            }
        }
        return match;
    }

    /** sends a KeyEventWrapper with the id KeyEvent.KEY_PRESSED at specified timestamp
    
    @param kew the KeyEventWrapper to send as PRESSED
    
    @param when the timestamp at which the event occurred */
    private void sendKeyEventWrapperPressed(KeyEventWrapper kew, long when) {
        KeyEventWrapper addKew = null;
        if (isDebugEnabled()) {
            System.out.println(JXAenvUtils.log("dispatch new key pressed " + kew, JXAenvUtils.LVL.APP_NOT));
        }
        Player.id player = attributes.containsKey("player") ? (Player.id) attributes.get("player") : null;
        Vector<Long> removals = new Vector<Long>();
        synchronized (pressedKeys) {
            for (Iterator<Map.Entry<Long, KeyEventWrapper>> i = pressedKeys.entrySet().iterator(); i.hasNext();) {
                Map.Entry<Long, KeyEventWrapper> ckew = i.next();
                /** check for the correct keycode*/
                if (Player._checkAxisKeyCode(player, kew.get_keyCode()) == ckew.getValue().get_keyCode()) {
                    addKew = ckew.getValue();
                    removals.add(ckew.getKey());
                    if (isDebugEnabled()) {
                        System.out.println(JXAenvUtils.log("continue pressed " + ckew, JXAenvUtils.LVL.APP_NOT));
                    }
                }
            }
            if (removals.isEmpty()) {
                if (isDebugEnabled()) {
                    System.out.println(JXAenvUtils.log("start pressed " + kew, JXAenvUtils.LVL.APP_NOT));
                }
            }
            for (Long r : removals) {
                pressedKeys.remove(r);
            }
        }
        if (addKew == null) {
            addKew = new KeyEventWrapper(kew.get_when(), Player._checkAxisKeyCode(player, kew.get_keyCode()), kew.get_ID(), kew.get_timing());
        }
        pressedKeys.put(when, addKew);
    }

    /** sends a new KeyEventWrapper with the id KeyEvent.KEY_RELEASED along with the corresponding KeyEventWrapper identifying how long the key has been pressed, together with the timestamp at which the event occurred
    
     * @param kew the KeyEventWrapper that wraps the KeyEvent
    
    @param when the timstamp at which the specified event occurred
    
     */
    private void sendKeyEventWrapperReleased(KeyEventWrapper kew, long when) {
        if (isDebugEnabled()) {
            System.out.println(JXAenvUtils.log("dispatch new key released " + kew, JXAenvUtils.LVL.APP_NOT));
        }
        Player.id player = attributes.containsKey("player") ? (Player.id) attributes.get("player") : null;
        SortedMap<Long, KeyEventWrapper> kews = new TreeMap<Long, KeyEventWrapper>();
        Vector<Long> removals = new Vector<Long>();
        synchronized (pressedKeys) {
            for (Iterator<Map.Entry<Long, KeyEventWrapper>> i = pressedKeys.entrySet().iterator(); i.hasNext();) {
                Map.Entry<Long, KeyEventWrapper> pressed = i.next();
                KeyEventWrapper pressedKew = pressed.getValue();
                /** select the pressed event and remove from pressedkeys stack*/
                if (Player._checkAxisKeyCode(player, kew.get_keyCode()) == pressedKew.get_keyCode()) {
                    pressedKew = new KeyEventWrapper(pressedKew.get_when(), pressedKew.get_keyCode(), KeyEvent.KEY_PRESSED, when - pressedKew.get_when());
                    kews.put(when, pressedKew);
                    removals.add(pressed.getKey());
                }
            }
            for (Long r : removals) {
                pressedKeys.remove(r);
            }
        }
        kews.put(when + 1, new KeyEventWrapper(when + 1, Player._checkAxisKeyCode(player, kew.get_keyCode()), KeyEvent.KEY_RELEASED));
        threadedEventsSequence.putAll(kews);
    }

    /** adds a new KeyEventWrapper mapping for this InteractiveModel. any previous mapping for the specified KeyEventWrapper will be replaced by the new one. the new mapping is added to the key events map.    
    @param kew the KeyEventWrapper to add    
    @param modelKey the Animation String key that will be mapped to the specified KeyEventWrapper*/
    @Override
    public void addKeyEventWrapperMapping(KeyEventWrapper kew, String modelKey) {
        addKeyEventWrapperMapping(kew, modelKey, false);
    }

    /** adds a new KeyEventWrapper mapping for this InteractiveModel. any previous mapping for the specified KeyEventWrapper will be replaced by the new one.
     * The new mapping is added to the key events map.    
    @param kew the KeyEventWrapper to add    
    @param modelKey the Animation String key that will be mapped to the specified KeyEventWrapper @param addReverseOrder if kew is a sequence, adds the reverse order sequence for the same mapping, as well (e.g.
    mapping VK_UP, VK_LEFT to "up-left" would also map VK_LEFT, VK_UP to "up-left".*/
    public void addKeyEventWrapperMapping(KeyEventWrapper kew, String modelKey, boolean addReverseOrder) {
        keyEvents.put(kew, modelKey);
        if (addReverseOrder) {
            keyEvents.put(kew.getReverse(), modelKey);
        }
        highestKeysSequence = highestKeysSequence(keyEvents);
        highestKeysSequenceTiming = highestKeysSequenceTiming(keyEvents);
    }

    /** adds a whole KeyEventWrapper map with their corresponding mapped Animation keys. any previous mappings for the same specified KeyEventWrapper's contained in the specified map will be replaced by the new ones. everyting is added to the key events map.    
    @param map the map to add as a whole    
    @see #addKeyEventWrapperMapping(KeyEventWrapper, String)*/
    @Override
    public void addKeyEventWrapperMap(Map<KeyEventWrapper, String> map) {
        keyEvents.putAll(map);
        highestKeysSequence = highestKeysSequence(keyEvents);
        highestKeysSequenceTiming = highestKeysSequenceTiming(keyEvents);
    }

    /** removes a KeyEventWrapper mapping 
    
    @param kew the KeyEventWrapper to remove from the key events map*/
    @Override
    public void removeKeyEventWrapperMapping(KeyEventWrapper kew) {
        removeKeyEventWrapperMapping(kew, false);
    }

    /**@param removeReverse removes the reverse order, as well*/
    public void removeKeyEventWrapperMapping(KeyEventWrapper kew, boolean removeReverse) {
        keyEvents.remove(kew);
        if (removeReverse) {
            keyEvents.remove(kew.getReverse());
        }
        highestKeysSequence = highestKeysSequence(keyEvents);
        highestKeysSequenceTiming = highestKeysSequenceTiming(keyEvents);
    }

    /** clears the whole key events map for this InteractiveModel.
    
    @see #addKeyEventWrapperMap(Map)*/
    @Override
    public void clearKeyEventWrapperMap() {
        keyEvents.clear();
        highestKeysSequence = 0;
        highestKeysSequenceTiming = 0;
    }

    /** will return true if and only if getAttribute("player") returns a value and that the specified KeyEvent
    is registered as a KeyEventWrapper in {@linkplain Player#_keyCodeMap} for that player*/
    public boolean isPlayerKeycode(KeyEvent e) {
        if (Player._keyCodeMap.containsValue(e.getKeyCode())) {
            synchronized (Player._keyCodeMap) {
                for (Iterator<Player.key> i = Player._keyCodeMap.keySet().iterator(); i.hasNext();) {
                    Player.key key = i.next();
                    int keyCode = Player._keyCodeMap.get(key);
                    if (key.player.equals(attributes.get("player")) && keyCode == e.getKeyCode()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** dispatches the specified KeyEvent to the key events map for comparison.
     * If getAttribute("player") is null, then all events are dispatched and will not be forwarded to others.
     * If getAttribute("player") is one of the {@linkplain Player#_keyCodeMap} registered maps, then only KeyEvents belowing the
     * getAttribute("player") value are processed, other events will be forwarded.
    @return true or false whether the KeyEvent is accepted by the key events map or not, resp.*/
    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        boolean interrupt_ = false;
        boolean b = false;
        try {
            final Monitor monitor = processMonitor;
            synchronized (monitor) {
                while (processing) {
                    monitor.wait();
                }
                monitor.notify();
                dispatching = true;
                long now = e.getWhen();
                if (hasAttribute("player") ? isPlayerKeycode(e) : true) {
                    /* manage key pression time*/
                    switch (e.getID()) {
                        case KeyEvent.KEY_PRESSED:
                            sendKeyEventWrapperPressed(new KeyEventWrapper(now, e.getKeyCode(), KeyEvent.KEY_PRESSED), now);
                            break;
                        case KeyEvent.KEY_RELEASED:
                            sendKeyEventWrapperReleased(new KeyEventWrapper(now, e.getKeyCode(), KeyEvent.KEY_RELEASED), now);
                            break;
                        default:
                            break;
                    }
                    b = true;
                }
            }
        } catch (InterruptedException ex) {
            if (isDebugEnabled()) {
                ex.printStackTrace();
            }
            interrupt_ = true;
        } finally {
            final Monitor monitor = dispatchMonitor;
            synchronized (monitor) {
                dispatching = false;
                monitor.notifyAll();
            }
            if (interrupt_) {
                return false;
            }
            return b;
        }
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        if (isDebugEnabled()) {
            System.out.println(JXAenvUtils.log("w-InteractiveModel", JXAenvUtils.LVL.APP_NOT));
        }
        int currentPty = Thread.currentThread().getPriority();
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        super.writeExternal(out);
        synchronized (_keyEvents) {
            out.writeInt(_keyEvents.size());
            for (Iterator<Entry<KeyEventWrapper, String>> it = _keyEvents.entrySet().iterator(); it.hasNext();) {
                Entry<KeyEventWrapper, String> e = it.next();
                out.writeObject(e.getKey());
                out.writeUTF(e.getValue());
            }
        }
        out.writeInt(highestKeysSequence);
        out.writeLong(highestKeysSequenceTiming);
        out.flush();
        Thread.currentThread().setPriority(currentPty);
        if (isDebugEnabled()) {
            System.out.println(JXAenvUtils.log("w-done", JXAenvUtils.LVL.APP_NOT));
        }
    }
    
    private void endRead(ObjectInput in) throws IOException, ClassNotFoundException {
        keyEvents = Collections.synchronizedMap(_keyEvents);
        gamePad = null;
        resetKeyEventsMaps(highestKeysSequence);
    }
    
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        if (isDebugEnabled()) {
            System.out.println(JXAenvUtils.log("r-InteractiveModel", JXAenvUtils.LVL.APP_NOT));
        }
        int currentPty = Thread.currentThread().getPriority();
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        super.readExternal(in);
        int s = in.readInt();
        for (int i = 0; i < s; i++) {
            keyEvents.put((KeyEventWrapper) in.readObject(), in.readUTF());
        }
        highestKeysSequence = in.readInt();
        highestKeysSequenceTiming = in.readLong();
        endRead(in);
        Thread.currentThread().setPriority(currentPty);
        if (isDebugEnabled()) {
            System.out.println(JXAenvUtils.log(toString(), JXAenvUtils.LVL.APP_NOT));
        }
        if (isDebugEnabled()) {
            System.out.println(JXAenvUtils.log("r-done", JXAenvUtils.LVL.APP_NOT));
        }
    }
    
    public static String readKeyMappings(InteractiveModel m) {
        String s = m.toString() + "\n:: keys mappings : { ";
        synchronized (m.keyEvents) {
            for (Iterator<KeyEventWrapper> i = m.keyEvents.keySet().iterator(); i.hasNext();) {
                KeyEventWrapper kew = i.next();
                s += "\n [" + kew.toString() + " <-> " + m.keyEvents.get(kew) + "] \n";
            }
        }
        s += "\n }\n";
        return s;
    }

    /** converts this InteractiveModel into a comprehensive String (may be very long if the instance holds key events mapppings)
    
    @return a comprehensive String to represent this instance */
    @Override
    public String toString() {
        String s = super.toString();
        return s;
    }

    /** sets up the gamepad JInput Controller that will send its events to this InteractiveModel    
    @param gamePad the gamepad JInput Controller instance that will send its events for this InteractiveModel
     */
    public void setGamePadController(Controller gamePad, Player.id player) {
        this.gamePad = gamePad;
        setAttribute("player", player);
    }

    /** returns the associated gamepad JInput Controller instance for this InteractiveModel
    
    @return the associated gamepad JInput Controller instance or null if none has been set previously*/
    public Controller getGamePadController() {
        return gamePad;
    }

    /** processes the whole dispatched KeyEventWrapper's 
     * sequences at current timestamp stack including any associated GamePadController's.
    The next Animation pointer is changed to that one matched by the keyEventMapping
    if and only if the following statements are true :
    <ol><li>the model is loaded;</li>
     * <li>the model current animation name is null or equal to the starting sequence
     * of characters of the matched animation name (i.e. <pre>nextAnimName.startsWith(animName)</pre> returns true);</li>
     * <li>the current animation name lock is off or equal to the starting sequence of characters of the matched animation name (i.e. <pre>nextAnimName.startsWith(animName_lock)</pre> returns true);</li>
     * </ol>*/
    protected void processKeyEventsStack() {
        if (gamePad instanceof Controller) {
            Player.id player = attributes.containsKey("player") ? (Player.id) attributes.get("player") : Player.id.ONE;
            getRenderingScene().getCtrlHDR().proceedGamePadEvents(gamePad, obs, player);
        }
        String nextAnimName = null;
        try {
            final Monitor monitor0 = dispatchMonitor;
            synchronized (monitor0) {
                while (dispatching) {
                    monitor0.wait();
                }
                monitor0.notify();
                processing = true;
                if ((!isLWJGLAccel() && isLoaded()) || (isLWJGLAccel() && GLisLoaded())) {
                    /* match animation troughout keys sequences*/
                    long now = System.currentTimeMillis();
                    SortedMap<Long, KeyEventWrapper> filter = filterKeysSequence(threadedEventsSequence, now);
                    threadedEventsSequence.clear();
                    if (isDebugEnabled()) {
                        System.out.println(JXAenvUtils.log("current key input filter @ " + now + " #### " + new KeyEventWrapper(new Vector<KeyEventWrapper>(filter.values())), JXAenvUtils.LVL.APP_NOT));
                    }
                    KeyEventWrapper match = processMatcher(filter);
                    nextAnimName = keyEvents.get(match);
                    if (getPlayerStatus() == Animation.PLAYING) {
                        if (animName instanceof String && nextAnimName instanceof String) {
                            if (!animName.equals(nextAnimName) && !nextAnimName.startsWith(animName)) {
                                if (isDebugEnabled()) {
                                    System.out.println(Model.class.getName() + " is playing " + animName + " matched keyEvent " + nextAnimName + " is ignored.");
                                }
                                nextAnimName = null;
                            }
                        }
                    }
                    if (isDebugEnabled()) {
                        System.out.println(JXAenvUtils.log("next anim " + nextAnimName, JXAenvUtils.LVL.APP_NOT));
                    }
                }
            }
        } catch (InterruptedException e) {
            if (isDebugEnabled()) {
                e.printStackTrace();
            }
        } finally {
            final Monitor monitor0 = processMonitor;
            synchronized (monitor0) {
                processing = false;
                monitor0.notifyAll();
            }
            if (nextAnimName instanceof String) {
                if (isLWJGLAccel()) {
                    GLanim(nextAnimName, frameRate, false);
                } else {
                    anim(nextAnimName, frameRate, false);
                }
                if (isDebugEnabled()) {
                    System.out.println(JXAenvUtils.log(id + "'s doing " + animName + " now.",JXAenvUtils.LVL.APP_NOT));
                }
            }
        }
    }

    /** clears the resource for this InteractiveModel */
    @Override
    public Object clearResource() {
        return super.clearResource();
    }

    /** 3 monitors + [dispatchmonitor, processmonitor]*/
    @Override
    public void setGroupMonitor(Monitor... tg) {
        super.setGroupMonitor(tg);
        if (tg.length > 3) {
            dispatchMonitor = tg[3];
            processMonitor = tg[4];
        }
    }

    /** validates the InteractiveModel instance for displaying it correctly. KeyEvents and the gamepads are processed for the current timestamp.
    
    it instances a new Thread for processing the validation if and only if the multi-threading option is enabled or the validation process will occur on the current runnning Thread.
    
    @see Model#runValidate()*/
    @Override
    public Object runValidate() {
        Object ret = super.runValidate();
        try {
            if (isMultiThreadingEnabled()) {
                Thread t = new Thread(new Runnable() {
                    
                                    @Override
                    public void run() {
                        processKeyEventsStack();
                    }
                }, "T-processKeyEventsStack");
                t.setPriority(Thread.MAX_PRIORITY);
                t.start();
                t.join();
            } else {
                processKeyEventsStack();
            }
        } catch (InterruptedException ex) {
            if (isDebugEnabled()) {
                ex.printStackTrace();
            }
        } finally {
        return ret;
        }
    }
    
    @Override
    public void GLrunValidate() {
        super.GLrunValidate();
        processKeyEventsStack();
    }
    
    public Map<Long, KeyEventWrapper> getPressedKeys() {
        return pressedKeys;
    }
    
    public SortedMap<Long, KeyEventWrapper> getFilteredEventsSequence() {
        return filterKeysSequence(threadedEventsSequence, System.currentTimeMillis());
    }
}
