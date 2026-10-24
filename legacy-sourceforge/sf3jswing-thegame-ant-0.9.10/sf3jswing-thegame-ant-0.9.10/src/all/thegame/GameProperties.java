package thegame;

import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.datatransfer.DataFlavor;
import java.io.*;
import java.util.*;
import javax.swing.JProgressBar;
import net.java.games.input.Component.Identifier;
import net.sf.jiga.xtended.impl.Sprite;
import net.sf.jiga.xtended.impl.game.DisplayModeWrapper;
import net.sf.jiga.xtended.impl.game.Model;
import net.sf.jiga.xtended.impl.game.RenderingScene;
import net.sf.jiga.xtended.impl.game.XtendedModel;
import net.sf.jiga.xtended.impl.game.physics.Player;
import net.sf.jiga.xtended.impl.game.physics.Player.key;
import net.sf.jiga.xtended.kernel.BitStack;
import net.sf.jiga.xtended.kernel.JXAenvUtils;
import net.sf.jiga.xtended.kernel.ThreadWorks;
import net.sf.jiga.xtended.ui.UIMessage;

/**
 * Defines the properties of the field, as defined in this picture.<br/> <img
 * src="field.gif"/><br/> <br/> TODO: Changer qui necessaire dont il devient
 * "serializable".
 */
public class GameProperties implements Externalizable {

    private static final long serialVersionUID = 2323;
    /*
     * public boolean hardwareAccel = false;
     */
    /**
     *
     */
    public final static String[] _MIME_EXTS = new String[]{"MCO", "mco"};
    /**
     *
     */
    public final static DataFlavor _DATAFLAVOR = new DataFlavor(GameProperties.class, "Game options");
    /**
     * rendering hint
     */
    public static SortedMap<String, RenderingHints> renderings = Collections.synchronizedSortedMap(new TreeMap<String, RenderingHints>());
    /**
     *
     */
    public static SortedMap<String, String> renderingsStrings = Collections.synchronizedSortedMap(new TreeMap<String, String>());

    static {
        SortedMap<String, RenderingHints> rh = new TreeMap<String, RenderingHints>();
        rh.put("fastest rendering mode", new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED));
        rh.put("best quality rendering mode", new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
        rh.put("default rendering mode", new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT));
        rh.put("anti-aliasing : default", new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT));
        rh.put("anti-aliasing : on", new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
        rh.put("anti-aliasing : off", new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF));
        rh.put("dithering : default", new RenderingHints(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DEFAULT));
        rh.put("dithering : disabled", new RenderingHints(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE));
        rh.put("dithering : enabled", new RenderingHints(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE));
        rh.put("interpolation : bicubic (best)", new RenderingHints(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC));
        rh.put("interpolation : bilinear (better)", new RenderingHints(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR));
        rh.put("interpolation : nearest neighbour (quicker)", new RenderingHints(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR));
        rh.put("text a-alias : default", new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT));
        rh.put("text a-alias : off", new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF));
        rh.put("text a-alias : on", new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON));
        rh.put("color rendering : default", new RenderingHints(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_DEFAULT));
        rh.put("color rendering : quality", new RenderingHints(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY));
        rh.put("color rendering : speed", new RenderingHints(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED));
        rh.put("alpha interpolation : default", new RenderingHints(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_DEFAULT));
        rh.put("alpha interpolation : speed", new RenderingHints(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED));
        rh.put("alpha interpolation : quality", new RenderingHints(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY));
        for (String str : rh.keySet()) {
            renderings.put(rh.get(str).toString(), rh.get(str));
            renderingsStrings.put(rh.get(str).toString(), str);
        }

    }
    /**
     * rendering mode
     */
    public static final RenderingHints DEFAULT_RENDERING = Sprite._getRenderingHints();
    public transient RenderingHints rendering = (RenderingHints) DEFAULT_RENDERING.clone();
    public final DisplayModeWrapper DEFAULT_DISPLAYRES = FieldGui.selectedDM;
    private static BitStack _debugBits = new BitStack();
    public final static int _DEBUG_PHYSICS = _debugBits._newBitRange();
    public final static int _DEBUG_JXA = _debugBits._newBitRange();
    public final static int _DEBUG_JXA_KERNEL = _debugBits._newBitRange();
    public final static int _DEBUG_GUI = _debugBits._newBitRange();
    public final static int _DEBUG_RENDER = _debugBits._newBitRange();
    public final static int _DEBUG_LOADING_ACTIVITY = _debugBits._newBitRange();
    /**
     * The width of the field
     */
    public DisplayModeWrapper dm = DEFAULT_DISPLAYRES;
    public Map<String, XtendedModel[]> bgModels = Collections.synchronizedMap(new HashMap<String, XtendedModel[]>());
    public List<XtendedModel> characters = Collections.synchronizedList(new Vector<XtendedModel>());
    boolean lwjglEnabled = RenderingScene.lwjglEnabled;
    /**
     * net port mapping to use, if 0 then a free random port will be used
     */
    int defaultPort = 0;
    public int fighterSpeed = 10;
    public int fighterWeight = 5;
    public int gravity = 10;
    public int DEFENSE_MOVE = 10;
    public int HIT_MOVE = 30;
    /**
     * The ball speed, used in ServerAlgorithm
     */
    public double speedBall = 6;
    public double speedBallMax = 10;
    public double speedBallMin = 1.5;
    public long roundTime = 60000;
    public SortedMap<Player.key, Integer> keyboardCodeMap = Collections.synchronizedSortedMap(new TreeMap<Player.key, Integer>(Player._keyCodeMap));
    /**
     * bit OR-wise combination {@link #_DEBUG_PHYSICS}, {@link #_DEBUG_JXA}, {@link #_DEBUG_JXA_KERNEL}, {@link #_DEBUG_GUI},...
     */
    private int debug = 0;

    public int getDebug() {
        return (JXAenvUtils._getSysBoolean("jxa.debug") ? _DEBUG_JXA : 0) | (JXAenvUtils._getSysBoolean("jxa.debugSys") ? _DEBUG_JXA_KERNEL : 0) | (JXAenvUtils._getSysBoolean("jxa.debugRender") ? _DEBUG_RENDER : 0) | debug;
    }

    public void setDebug(int debug) {
        this.debug = debug;
    }

    public static String getApiSpecVersion() {
        return Sf3.class.getPackage().getSpecificationVersion();
    }

    public int getStartingPositions(int playerNum) {
        if (playerNum == 0) {
            return Math.round((float) dm.getDM().getWidth() * 1f / 4f);
        }
        if (playerNum == 1) {
            return Math.round((float) dm.getDM().getWidth() * 3f / 4f);
        }
        return 0;
    }

    public int getFloor() {
        return Math.round((float) dm.getDM().getHeight() * 9f / 10f);
    }
    public transient final int MAX = 100;
    public transient final int MIN = 1;
    public transient SortedMap<Player.key, Identifier> gamepadCodeMap = Collections.synchronizedSortedMap(new TreeMap<Player.key, Identifier>(Player._gamepadCodeMap));

    public GameProperties() {
    }
    private static boolean deploying = false;
    static ThreadWorks propsTWKS = new ThreadWorks("gameproperties");
    transient long deployProgress = 0;

    /**
     * @param ll reports loading until it's complete (set null for no report)
     * @param prgBar reports loading progress (set 0 for no report)
     */
    public JProgressBar deployContentsCaches(final CacheLoadListener ll, final long prgBar) {
        if (deploying) {
            return JXAenvUtils._JTimeProgressBar();
        }
        if (prgBar == 0) {
            deployProgress = UIMessage.newProgress(0, 100, null);
        } else {
            deployProgress = prgBar;
        }
        deploying = true;
        if (ll != null) {
            ll.loadBegin();
        }
        Runnable r = new Runnable() {

            public void run() {
                int pty = Thread.currentThread().getPriority();
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                int total = bgModels.size() + characters.size();
                int prg = 0;
                try {
                    synchronized (bgModels) {
                        for (XtendedModel[] m : bgModels.values()) {
                            for (Model mLayer : m) {
                                if (m != null) {
                                    mLayer.load(true, prgBar, ll);
                                }
                            }
                            UIMessage.updateProgress(deployProgress, ++prg, total);
                        }
                    }
                    synchronized (characters) {
                        for (XtendedModel xm : characters) {
                            if (xm != null) {
                                xm.load(true, prgBar, ll);
                            }
                            UIMessage.updateProgress(deployProgress, ++prg, total);
                        }
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                } finally {
                    if (ll != null) {
                        ll.loadIsComplete();
                    }
                    deploying = false;
                    Thread.currentThread().setPriority(pty);
                }
            }
        };
        propsTWKS.doLater(r);
        return UIMessage.getProgressBar(deployProgress);
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(rendering.size());
        for (Iterator it = rendering.keySet().iterator(); it.hasNext();) {
            Key k = (Key) it.next();
            String name = new RenderingHints(k, rendering.get(k)).toString();
            out.writeObject(name);
        }
        TreeMap<Player.key, String> map = new TreeMap<Player.key, String>();
        for (Map.Entry<Player.key, Identifier> entry : gamepadCodeMap.entrySet()) {
            map.put(entry.getKey(), entry.getValue().getName());
        }
        out.writeObject(map);
        out.writeObject(dm);
        out.writeObject(bgModels);
        out.writeObject(characters);
        out.writeBoolean(lwjglEnabled);
        int[] ints = new int[]{debug, defaultPort, fighterSpeed, fighterWeight, gravity, DEFENSE_MOVE, HIT_MOVE};
        for (int i : ints) {
            out.writeInt(i);
        }
        double[] doubles = new double[]{speedBall, speedBallMax, speedBallMin};
        for (double d : doubles) {
            out.writeDouble(d);
        }
        long[] longs = new long[]{roundTime};
        for (long l : longs) {
            out.writeLong(l);
        }
        out.writeObject(keyboardCodeMap);
        out.flush();
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int rhs = in.readInt();
        rendering = new RenderingHints(null);
        for (int i = 0; i < rhs; i++) {
            rendering.add(renderings.get(in.readObject()));
        }
        gamepadCodeMap = Collections.synchronizedSortedMap(new TreeMap<Player.key, Identifier>());
        TreeMap<Player.key, String> map = (TreeMap<key, String>) in.readObject();
        for (Map.Entry<Player.key, String> entry : map.entrySet()) {
            for (Identifier i : Player._gamepadCodeMap.values()) {
                if (i.getName().equals(entry.getValue())) {
                    gamepadCodeMap.put(entry.getKey(), i);
                    break;
                }
            }
        }
        dm = (DisplayModeWrapper) in.readObject();
        bgModels = (Map<String, XtendedModel[]>) in.readObject();
        characters = (List<XtendedModel>) in.readObject();
        lwjglEnabled = in.readBoolean();
        debug = in.readInt();
        defaultPort = in.readInt();
        fighterSpeed = in.readInt();
        fighterWeight = in.readInt();
        gravity = in.readInt();
        DEFENSE_MOVE = in.readInt();
        HIT_MOVE = in.readInt();
        speedBall = in.readDouble();
        speedBallMax = in.readDouble();
        speedBallMin = in.readDouble();
        roundTime = in.readLong();
        keyboardCodeMap = (SortedMap<key, Integer>) in.readObject();
    }
}
