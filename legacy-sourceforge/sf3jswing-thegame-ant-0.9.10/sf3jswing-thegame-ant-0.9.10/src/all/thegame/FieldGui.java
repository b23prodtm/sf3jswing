package thegame;

import thegame.gui.SelectionMenu;
import thegame.gui.SelectionMenuComponent;
import thegame.gui.SelectionMenuCell;
import thegame.comp.Fighter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.*;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.*;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import org.eclipse.imagen.PerspectiveTransform;
import javax.swing.*;
import net.sf.jiga.xtended.impl.*;
import net.sf.jiga.xtended.impl.game.*;
import net.sf.jiga.xtended.impl.game.gl.*;
import net.sf.jiga.xtended.impl.game.gl.*;
import net.sf.jiga.xtended.impl.game.gl.geom.GLGeom;
import net.sf.jiga.xtended.impl.game.physics.Player;
import net.sf.jiga.xtended.impl.system.BufferIO;
import net.sf.jiga.xtended.impl.system.JXAenvUtils2;
import net.sf.jiga.xtended.impl.system.LoadListener;
import net.sf.jiga.xtended.impl.system.LoadListener2;
import net.sf.jiga.xtended.impl.system.input.ControllersHandler;
import net.sf.jiga.xtended.kernel.*;
import net.sf.jiga.xtended.ui.UIMessage;
import net.sf.jiga.xtended.ui.UIMessage.SwingStaticReturn;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.Util;
import thegame.layers.Fight;
import thegame.layers.GameMenu;
import thegame.layers.PlayerSelection;
import thegame.layers.UnknownGameState;

/**
 *
 * <pre>
 * v- O(x,y) for the field bounds
 * .---------------------------------
 * |                                |
 * |                                |
 * |	 o               o          |
 * player1v     x       v|      <-	| player 2
 * |	/ \             / \         |
 * 	-           -           -       | <- floor
 * |                                |
 * ----------------------------------
 *</pre>
 *
 */
public final class FieldGui implements GameStateHandler, AlgorithmListener, RenderingSceneListener, Debugger, KeyEventDispatcher, Threaded {

    public final static ResourceBundle rb = ResourceBundle.getBundle("thegame.gui");

    public static boolean rbHasKey(ResourceBundle rb, String name) {
        Enumeration<String> keys = rb.getKeys();
        for (; keys.hasMoreElements();) {
            if (keys.nextElement().equals(name)) {
                return true;
            }
        }
        return false;
    }
    /*
     * GRAPHICAL CONFIGURATION
     */
    /*
     * Display Modes
     */
    public static SortedSet<DisplayModeWrapper> dm = Collections.synchronizedSortedSet(new TreeSet<DisplayModeWrapper>());
    public static GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    public static GraphicsDevice[] gs = ge.getScreenDevices();
    private static int selectedDEV = 0;
    public static DisplayModeWrapper selectedDM;

    private static void _registerDM(DisplayMode dm) {
        FieldGui.dm.add(new DisplayModeWrapper(dm));
    }

    static {
        System.out.println(JXAenvUtils._JXAEnvOutput("Detecting display devices (screens)", JXAenvUtils.SYS_NOTICE));
        for (GraphicsDevice gd : gs) {
            System.out.print(gd.getIDstring());
            if (JXAenvUtils._debug) {
                for (int i = 0; i < gd.getDisplayModes().length; i++) {
                    DisplayMode dm = gd.getDisplayModes()[i];
                    System.out.println(" DM " + i + " : " + dm.getWidth() + "x" + dm.getHeight() + " @ " + dm.getRefreshRate() + "Hz");
                }
            }
            System.out.println();
        }
        System.out.println(JXAenvUtils._JXAEnvOutput("gs.length " + gs.length, JXAenvUtils.SYS_NOTICE));
        while (dm.isEmpty()) {
            for (DisplayMode m : gs[selectedDEV].getDisplayModes()) {
                _registerDM(m);
            }
            if (dm.isEmpty()) {
                if (JOptionPane.NO_OPTION == UIMessage.showConfirmDialog(null, "<html><b>Sorry !</b> The display device is unavailable.<br><center>Do you wish to try with another device ?</center>", "device error", JOptionPane.YES_NO_OPTION)) {
                    System.exit(1);
                } else {
                    selectedDEV++;
                }
            }
        }
        String[] displayStr = rb.getString("GFX_DISPLAY_MODE").split(" ");
        System.out.println(JXAenvUtils._JXAEnvOutput("GFX_DISPLAY_MODE in rendering.properties is requesting : ", JXAenvUtils.APP_NOTICE));
        int[] display = new int[displayStr.length];
        if (displayStr.length < 4) {
            System.err.println(JXAenvUtils._JXAEnvOutput("An error in thegame.gui[GFX_DISPLAY_MODE] syntax may have selected the default display resolution.", JXAenvUtils.APP_ERROR));
            displayStr = new String[]{"640", "480", "24", "80"};
        }
        int i = 0;
        for (String s : displayStr) {
            display[i++] = Integer.parseInt(s);
            if (i <= 1) {
                s = s + "x";
            } else {
                s = s + "@";
            }
            System.out.print(s);
        }
        System.out.println();
        DisplayModeWrapper targetDM = new DisplayModeWrapper(new DisplayMode(display[0], display[1], display[2], display[3]));
        while (selectedDM == null) {
            for (DisplayModeWrapper dmw : dm) {
                if (targetDM.compareTo(dmw) == 0 || targetDM.compareTo(dmw) == 1) {
                    selectedDM = dmw;
                }
            }
            if (selectedDM == null) {
                if (JOptionPane.NO_OPTION == UIMessage.showConfirmDialog(null, "<html><b>Sorry !</b> No compatible display mode is available for " + targetDM + ".<br><center>Do you wish to try with another mode ?</center>", "display mode error", JOptionPane.YES_NO_OPTION)) {
                    System.exit(1);
                } else {
                    TreeSet<DisplayModeWrapper> dmTree = new TreeSet<DisplayModeWrapper>();
                    for (DisplayModeWrapper dmw : dm) {
                        dmTree.add(dmw);
                    }
                    SortedSet<DisplayModeWrapper> upTree = dmTree.tailSet(targetDM);
                    if (upTree.isEmpty()) {
                        System.exit(1);
                    } else {
                        targetDM = upTree.first();
                    }
                }
            } else {
                System.err.println(JXAenvUtils._JXAEnvOutput("display resolution is OK : " + selectedDM, JXAenvUtils.APP_NOTICE));
            }
        }
    }

    static void addBackgroundModel(String grp, XtendedModel importModel) {
        if (availableBackgroundModels.get(grp) instanceof XtendedModel[]) {
            addBackgroundModel(grp, importModel, availableBackgroundModels.get(grp).length);
        } else {
            addBackgroundModel(grp, importModel, 0);
        }
    }
    long hash = System.nanoTime();

    public int hashCode() {
        return (int) hash;
    }

    public boolean equals(Object o) {
        return o == null ? false : o.hashCode() == hashCode();
    }

    public void switchDisplayMode() {
        DisplayModeWrapper sDmw = selectedDM;
        synchronized (dm) {
            for (Iterator<DisplayModeWrapper> i = dm.iterator(); i.hasNext();) {
                DisplayModeWrapper dmw = i.next();
                if (dmw.equals(selectedDM)) {
                    if (i.hasNext()) {
                        sDmw = i.next();
                        break;
                    } else {
                        sDmw = dm.iterator().next();
                        break;
                    }
                }
            }
        }
        switchDisplayMode(sDmw);
    }

    public void switchDisplayMode(final DisplayModeWrapper dmw) {
        try {
            JXAenvUtils2.invokeSwingAndReturn(new SwingStaticReturn() {

                public Object run() {
                    __switchDisplayMode(dmw);
                    return null;
                }
            });
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }

    private void __switchDisplayMode(DisplayModeWrapper dmw) {
        if (gs[selectedDEV].getFullScreenWindow() != null) {
            gs[selectedDEV].setDisplayMode(dmw.getDM());
            dmw = new DisplayModeWrapper(gs[selectedDEV].getDisplayMode());
        } else {
            if (scene instanceof RenderingScene) {
                scene.setPreferredSize(new Dimension(dmw.getDM().getWidth(), dmw.getDM().getHeight()));
                JFrame frame = scene.getToDevice();
                if (frame instanceof JFrame) {
                    frame.setPreferredSize(scene.getPreferredSize());
                }
                scene.setSize(scene.getPreferredSize());
                container.validate();
                container.repaint();
            }
        }
        selectedDM = dmw;
        props.dm = dmw;
    }

    public static DisplayModeWrapper getDisplayModeWrapper() {
        return selectedDM;
    }
    /**
     * SPRITES CACHE Manager (using hard-disk storage => slow with GL calls)
     */
    private static SpritesCacheManager<String, Serializable> _cacheManager = new SpritesCacheManager<String, Serializable>();
    /**
     * SPRITES CACHE synchronized view (using hard-disk storage => slow with GL
     * calls)
     *
     */
    protected static SortedMap<String, Serializable> _cacheSyncManager = Collections.synchronizedSortedMap(_cacheManager);
    /**
     * SPRITES cache Registry (hashCode())
     */
    protected static SortedMap<String, Integer> _cacheHashRegistry = Collections.synchronizedSortedMap(new TreeMap<String, Integer>());

    static {
        _cacheManager.setSwapDiskEnabled(true);
        _cacheManager.setCompressionEnabled(false);
    }
    private RenderingScene scene;
    private static java.util.List<XtendedModel> availablePlayerModels = Collections.synchronizedList(new Vector<XtendedModel>());

    public static List<XtendedModel> getAvailablePlayerModels() {
        return availablePlayerModels;
    }

    public static Map<String, XtendedModel[]> getAvailableBackgroundModels() {
        return availableBackgroundModels;
    }
    private static Map<String, XtendedModel[]> availableBackgroundModels = Collections.synchronizedMap(new HashMap<String, XtendedModel[]>());

    public XtendedModel[] getBackgroundModels(int gameState) {
        XtendedModel[] sprites_bg = availableBackgroundModels.get(screenState.get(gameState));
        if (selectedPlayer[0] instanceof SortedMap) {
            if (selectedPlayer[0].get("bg") instanceof XtendedModel[]) {
                sprites_bg = (XtendedModel[]) selectedPlayer[0].get("bg");
            }
        }
        if (sprites_bg == null) {
            sprites_bg = availableBackgroundModels.get(SCREEN_DEFAULT);
        }
        return sprites_bg;
    }

    public static void clearPlayerModelsStack() {
        availablePlayerModels.clear();
    }

    public static void clearBackgroundModelsStack() {
        availableBackgroundModels.clear();
    }

    public static java.util.List<XtendedModel> getPlayerModelsStack() {
        return availablePlayerModels;
    }

    public static Map<String, XtendedModel[]> getBackgroundModelsStack() {
        return availableBackgroundModels;
    }

    public static void addPlayerModel(XtendedModel model) {
        availablePlayerModels.add(model);
    }

    public static XtendedModel addBackgroundModel(String screen, XtendedModel model, int layer) {
        XtendedModel ancient = null;
        if (availableBackgroundModels.get(screen) instanceof XtendedModel[]) {
            XtendedModel[] m = availableBackgroundModels.get(screen);
            XtendedModel[] newM = new XtendedModel[m.length];
            if (layer >= newM.length) {
                newM = new XtendedModel[layer + 1];
            }
            for (int i = 0; i < newM.length; i++) {
                XtendedModel newModel;
                if (i == layer) {
                    newModel = model;
                    ancient = i < m.length ? m[i] : null;
                } else {
                    newModel = i < m.length ? m[i] : null;
                }
                newM[i] = newModel;
            }
            availableBackgroundModels.put(screen, newM);
        } else {
            availableBackgroundModels.put(screen, new XtendedModel[]{model});
        }
        return ancient;
    }

    public static void setBackgroundModels(String screen, XtendedModel[] models) {
        availableBackgroundModels.put(screen, models);
    }

    public static void removePlayerModel(XtendedModel model) {
        if (model.getRenderingScene() instanceof RenderingScene) {
            model.getRenderingScene().removeKeyboardEventsDispatcher(model);
        }
        availablePlayerModels.remove(model);
    }

    public static void removeBackgroundModels(String screen) {
        availableBackgroundModels.remove(screen);
    }

    public static void removeBackgroundModel(String screen, XtendedModel bg) {
        XtendedModel[] currentModels = FieldGui.getBackgroundModelsStack().get(screen);
        if (currentModels instanceof XtendedModel[]) {
            XtendedModel[] newM = new XtendedModel[currentModels.length - 1];
            boolean refresh = false;
            int removedIndex = -1;
            for (int m = 0; m < currentModels.length; m++) {
                if (bg.equals(currentModels[m])) {
                    refresh = true;
                    removedIndex = m;
                }
                if (m < newM.length) {
                    newM[m] = removedIndex <= m && removedIndex != -1 ? currentModels[m + 1] : currentModels[m];
                }
            }
            if (refresh || newM.length == 0) {
                FieldGui.setBackgroundModels(screen, currentModels = newM);
            }
        }
    }
    /*
     * player's sprites
     */
    public static int NPLAYERS = 2;
    private SortedMap<String, Object> selectedPlayer[] = new SortedMap[NPLAYERS];
    /**
     * ------- constantes ----
     */
    public final static Map<Integer, String> screenState = Collections.synchronizedMap(new HashMap<Integer, String>());
    public final static String SCREEN_DEFAULT = "default";
    public final static String SCREEN_SELECTION = "selection screen";
    public final static String SCREEN_BATTLE = "battle field";
    public final static String SCREEN_ENDSCREEN = "end screen";
    public final static String SCREEN_INTROSCREEN = "intro screen";

    static {
        screenState.put(Game.STATE_UNKNOWN, SCREEN_DEFAULT);
        screenState.put(Game.STATE_LOADING, SCREEN_DEFAULT);
        screenState.put(Game.STATE_MENU, SCREEN_INTROSCREEN);
        screenState.put(Game.STATE_READY_TO_START, SCREEN_SELECTION);
        screenState.put(Game.STATE_RUNNING, SCREEN_BATTLE);
        screenState.put(Game.STATE_PAUSED, SCREEN_BATTLE);
        screenState.put(Game.STATE_FINISHED, SCREEN_ENDSCREEN);
    }
    /**
     * cache ids
     */
    public final static int BACKGROUND = 0;
    public final static int PLAYER_ONE = 1;
    public final static int PLAYER_TWO = 2;
    /**
     * The actual properties of the game
     */
    GameProperties props;
    /**
     * The actual state of the game
     */
    Game gameState;
    /*
     * graphical buffering
     */
    boolean hardwareMode;
    int filtering = AffineTransformOp.TYPE_NEAREST_NEIGHBOR;
    Container container;
    Object constraints;
    public static final int UISELECTIONWIDTH = 3;
    public final static Dimension ICONSIZE = new Dimension(64, 64);
    public static Map<String, SoundInput> _soundInterface = Collections.synchronizedMap(new HashMap<String, SoundInput>());
    private SortedSet<Integer> _bgSndStg = Collections.synchronizedSortedSet(new TreeSet<Integer>());
    static Dimension baseCodeDim = new Dimension();

    public static Vector<Action> _getLoadLayers() {
        Vector<Action> loadLayers = new Vector<Action>();
        loadLayers.add(new AbstractAction("sound interface...") {

            public void actionPerformed(ActionEvent e) {
                Enumeration<String> keys = rb.getKeys();
                for (; keys.hasMoreElements();) {
                    String key = keys.nextElement();
                    if (key.startsWith("SFX")) {
                        _soundInterface.put(key, initSound());
                        if (key.contains("SNDTRK")) {
                            _soundInterface.get(key).setUseJLayerEnabled(Boolean.parseBoolean(rb.getString("mp3SndtrkEnabled")));
                        }
                        _soundInterface.get(key).load(rb.getString(key));
                    }
                }
            }
        });
        loadLayers.add(new AbstractAction("sprites...") {

            public void actionPerformed(ActionEvent e) {
                Sprite sp;
                HashMap<String, ROI> spLoad = new HashMap<String, ROI>();
                String[] rbSprites = new String[]{
                    "GFX_BACKGROUND_SCROLLING",
                    "GFX_GAUGE_SPRITE_LEFT",
                    "GFX_GAUGE_SPRITE_RIGHT",
                    "GFX_GAUGE_GLOW_LEFT",
                    "GFX_GAUGE_GLOW_RIGHT",
                    "GFX_LOGO",
                    "GFX_MENU_BACKGROUND",
                    "GFX_CHARACTER_ICON",
                    "GFX_CHARACTER_SELECTION_ICON"};
                for (String s : rbSprites) {
                    spLoad.put(rb.getString(s), _rbGetDim(s));
                }
                for (Iterator<String> it = spLoad.keySet().iterator(); it.hasNext();) {
                    String k = it.next();
                    try {
                        sp = new Sprite(k, true, "image/x-png", spLoad.get(k).ROI.getSize(), true);
                        sp.setStoreMode(Sprite.MODE_TEXTURE);
                        _cacheSyncManager.put(k, sp);
                        _cacheHashRegistry.put(k, sp.hashCode());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        loadLayers.add(new AbstractAction("base code sprites...") {

            public void actionPerformed(ActionEvent e) {
                String baseCodeStr = rb.getString("GFX_BASE_CODE_TILES_MAP");
                _cacheManager.setListCapacity(Integer.parseInt(rb.getString("GFX_BASE_CODE")));
                try {
                    Sprite spTiles = new Sprite(rb.getString("GFX_BASE_CODE_TILES"), true, "image/x-png", _rbGetDim("GFX_BASE_CODE_TILES").ROI.getSize(), true);
                    spTiles.setTileModeEnabled(true);
                    ImageReader r = spTiles.getTileReader(true);
                    ImageReadParam irp = spTiles.getTileReaderParams(r);
                    int n = 0;
                    try {
                        for (String tile : baseCodeStr.split(" ")) {
                            irp.setSourceRegion(new Rectangle((int) ((float) r.getWidth(0) / (float) Integer.parseInt(rb.getString("GFX_BASE_CODE")) * (float) n), 0, (int) ((float) r.getWidth(0) / (float) Integer.parseInt(rb.getString("GFX_BASE_CODE"))), r.getHeight(0)));
                            BufferedImage tileImg = r.read(0, irp);
                            Sprite sp = new Sprite(tileImg, "image/x-png", baseCodeDim = new Dimension(tileImg.getWidth(), tileImg.getHeight()));
                            sp.setStoreMode(Sprite.MODE_TEXTURE);
                            _cacheSyncManager.put(tile, sp);
                            _cacheHashRegistry.put(tile, sp.hashCode());
                            n++;
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    } finally {
                        r.dispose();
                        spTiles.clearResource();
                        Sprite grid = new Sprite();
                        grid.setSize(100, 100);
                        grid.clearImageGraphics();
                        grid.runValidate();
                        Graphics2D gridG = Sprite.wrapRendering(Sprite._createImageGraphics(grid.getImage(grid.getObs())));
                        gridG.setColor(Color.WHITE);
                        for (int x0 = 0; x0 <= grid.getSize().width; x0 = x0 + 20) {
                            gridG.drawLine(x0, 0, x0, grid.getSize().height);
                        }
                        for (int y0 = 0; y0 <= grid.getSize().height; y0 = y0 + 20) {
                            gridG.drawLine(0, y0, grid.getSize().width, y0);
                        }
                        gridG.dispose();
                        grid.runValidate();
                        grid.setStoreMode(Sprite.MODE_TEXTURE);
                        _cacheSyncManager.put("grid", grid);
                        _cacheHashRegistry.put("grid", grid.hashCode());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        loadLayers.add(new AbstractAction("animations...") {

            /**
             * ANIMATIONS USED FOR IN-GAME EFFECTS
             */
            public void actionPerformed(ActionEvent e) {
                try {
                    ROI dim = _rbGetDim("GFX_VSLOGO");
                    Animation vsLogo = new Animation(rb.getString("GFX_VSLOGO"), true, 0, (int) dim.winZ - 1, "versus_icon", ".png", "image/x-png", dim.ROI.getSize());
                    vsLogo.setStoreMode(Sprite.MODE_TEXTURE);
                    vsLogo.setRealTimeLength(1500);
                    _cacheSyncManager.put("GFX_VSLOGO", vsLogo);
                    String[] fxNames = new String[]{"FX_FIGHTER_big_hit", "FX_FIGHTER_block", "FX_FIGHTER_medium_hit", "FX_FIGHTER_small_hit"};
                    String fxres = rb.getString("FX_FIGHTER_res");
                    for (String fxName : fxNames) {
                        if (JXAenvUtils._debug) {
                            System.out.println(fxName);
                        }
                        /**
                         * @NAME@_DIM = width height length
                         */
                        String[] v = rb.getString(fxName + "_DIM").split(" ");
                        dim = new ROI(new Rectangle(Integer.parseInt(v[0]), Integer.parseInt(v[1])), Integer.parseInt(v[2]));
                        /*
                         * manage resolution resizing
                         */
                        int res = SpritesChar.HIGH;
                        if (new String("LOW").equals(fxres)) {
                            res = SpritesChar.LOW;
                        }
                        if (new String("MID").equals(fxres)) {
                            res = SpritesChar.MID;
                        }
                        String suf = SpritesChar.RES_EXT_map.get(res);
                        String mime = SpritesChar.RES_MIMETYPES_map.get(res);
                        dim.ROI.width = (int) (1.0 / SpritesChar.RES_FACTORS_map.get(res) * dim.ROI.width);
                        dim.ROI.height = (int) (1.0 / SpritesChar.RES_FACTORS_map.get(res) * dim.ROI.height);
                        String volume = "_high";
                        if (fxName.contains("medium")) {
                            volume = "_mid";
                        }
                        if (fxName.contains("small")) {
                            volume = "_low";
                        }
                        String type = "_hit";
                        if (fxName.contains("block")) {
                            type = "_void_block";
                        }
                        _cacheSyncManager.put("DIM_" + fxName, dim);
                        /*
                         * left sound
                         */
                        Animation fx = new Animation(rb.getString(fxName) + "/" + SpritesChar.RES_PATHS_map.get(res), true, 0, (int) dim.winZ - 1, "", suf, mime, dim.ROI.getSize());
                        fx.setSfx(rb.getString("SFX_FIGHTER" + type + volume + "_L"), true, 0);
                        fx.setFrameRate((int) (1000f / 24f));
                        fx.setStoreMode(Sprite.MODE_TEXTURE);
                        _cacheSyncManager.put(fxName + "_L", fx);
                        /*
                         * right sound
                         */
                        fx = new Animation(rb.getString(fxName) + "/" + SpritesChar.RES_PATHS_map.get(res), true, 0, (int) dim.winZ - 1, "", suf, mime, dim.ROI.getSize());
                        fx.setSfx(rb.getString("SFX_FIGHTER" + type + volume + "_R"), true, 0);
                        fx.setFrameRate((int) (1000f / 24f));
                        fx.setStoreMode(Sprite.MODE_TEXTURE);
                        _cacheSyncManager.put(fxName + "_R", fx);
                        /*
                         * center sound
                         */
                        fx = new Animation(rb.getString(fxName) + "/" + SpritesChar.RES_PATHS_map.get(res), true, 0, (int) dim.winZ - 1, "", suf, mime, dim.ROI.getSize());
                        fx.setSfx(rb.getString("SFX_FIGHTER" + type + volume), true, 0);
                        fx.setFrameRate((int) (1000f / 24f));
                        fx.setStoreMode(Sprite.MODE_TEXTURE);
                        _cacheSyncManager.put(fxName, fx);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        loadLayers.add(new AbstractAction("integrity check...") {

            public void actionPerformed(ActionEvent e) {
                _cacheSyncManager.clear();
                _cacheSyncManager.get("FX_FIGHTER_big_hit");
            }
        });
        return loadLayers;
    }

    public Serializable getCacheEntry(String key) {
        return _cacheSyncManager.get(rb.getString(key));
    }

    public int getCacheHashEntry(String key) {
        return _cacheHashRegistry.get(rb.getString(key));
    }

    public static Dimension _baseCodeStringCLIPSIZE(String s, Dimension font) {
        Dimension labDim = new Dimension(font.width, font.height);
        labDim.width = font.width * s.length();
        return labDim;
    }

    public static Dimension _GL_baseCodeStringCLIPSIZE(RenderingScene gld, String s, int font) {
        Dimension charDim = _GL_baseCodeStringFONT(gld, font);
        return _baseCodeStringCLIPSIZE(s, charDim);
    }

    public static Dimension _GL_baseCodeStringFONT(RenderingScene gld, int font) {
        Dimension charDim = RenderingScene._GLtoGraphicalMetrics(gld);
        charDim.width = charDim.width * font;
        charDim.height = charDim.height * font;
        return charDim;
    }

    /*
     * static void _baseCodeStringDRAW(Component comp, Graphics2D g, int x, int
     * y, int font, String s) throws InterruptedException {
     * _baseCodeStringDRAW(comp, g, x, y, font, s, 0, new Point(0, 0), null); }
     */
    public static void _GL_baseCodeStringDRAW(RenderingScene gld, int x, int y, double z, int font, String s) {
        _GL_baseCodeStringDRAW(gld, x, y, z, font, s, 0, new Point(0, 0), null);
    }

    public static void _GL_baseCodeStringDRAW(RenderingScene gld, int x, int y, double z, int font, String s, int fx, Point fx_loc, Color fx_color) {
        Dimension charDim = _GL_baseCodeStringFONT(gld, font);
        _GL_baseCodeStringDRAW(gld, x, y, z, charDim, s, fx, fx_loc, fx_color);
    }

    public static void _GL_baseCodeStringDRAW(RenderingScene gld, int x, int y, double z, Dimension charDim, String s) {
        _GL_baseCodeStringDRAW(gld, x, y, z, charDim, s, 0, new Point(0, 0), null);
    }

    public static void _GL_baseCodeStringDRAW(RenderingScene gld, int x, int y, double z, Dimension charDim, String s, int fx, Point fx_loc, Color fx_color) {
        _GL__baseCodeStringDRAW(gld, x, y, z, charDim, s, fx, fx_loc, fx_color);
    }

    private static void _GL__baseCodeStringDRAW(RenderingScene gld, int x, int y, double z, Dimension charDim, String s, int fx, Point fx_loc, Color fx_color) {
        int xTranslation = 0;
        s = s.toLowerCase();
        for (char c : s.toCharArray()) {
            String character = new String(new char[]{c});
            if (_cacheHashRegistry.containsKey(character)) {
                if (RenderingScene._GLgetLoadState(_cacheHashRegistry.get(character)) == RenderingScene._GLLOADSTATE_Cleared) {
                    RenderingScene._GLloadSprite((Sprite) _cacheSyncManager.get(character), (RenderingScene) gld);
                }
                Sprite._GLRenderSprite(gld, Sprite._GLHandlers.getHandler(_cacheHashRegistry.get(character)), new Rectangle(x + xTranslation, y, charDim.width, charDim.height), z, fx, fx_loc, fx_color, 0, null, null, null);
            }
            xTranslation += charDim.width;
        }
    }
    Map<String, Sprite> modelIcons = Collections.synchronizedSortedMap(new TreeMap<String, Sprite>());

    Rectangle getFightersFieldBounds(boolean gl) {
        Rectangle fieldBounds = new Rectangle((int) ((float) getFloorLocation(gl) * 5f / 2f), getFloorLocation(gl));
        return fieldBounds;
    }

    public Sprite getModelIconSprite(XtendedModel model) {
        if (model instanceof XtendedModel) {
            Sprite icon = modelIcons.containsKey("modelIcon" + model.hashCode()) ? modelIcons.get("modelIcon" + model.hashCode()) : null;
            if (icon == null) {
                icon = (Sprite) model.getAttribute("icon");
                if (icon == null) {
                    icon = (Sprite) _cacheSyncManager.get("GFX_CHARACTER_SELECTION_ICON");
                }
                modelIcons.put("modelIcon" + model.hashCode(), icon);
            }
            icon.setRenderingScene(scene);
            return icon;
        }
        return new Sprite();
    }

    public static ROI _rbGetDim(String rbGFXEntry) {
        assert rbGFXEntry.startsWith("GFX_") : "not a GFX_ entry " + rbGFXEntry;
        String[] v = rb.getString(rbGFXEntry + "_DIM").split(" ");
        return new ROI(new Rectangle(Integer.parseInt(v[0]), Integer.parseInt(v[1])), Integer.parseInt(v[2]));
    }

    public static Dimension _fillDim(Dimension target, Dimension source) {
        double scale = Math.max(target.getWidth() / source.getWidth(), target.getHeight() / source.getHeight());
        return new Dimension((int) Math.round(source.getWidth() * scale), (int) Math.round(source.getHeight() * scale));
    }

    public static Dimension _GLgetDimZRatio(RenderingScene scene, double z, String rbString) {
        return _GLgetDimZRatio(scene, z, _fillDim(new Dimension(scene.getWidth(), scene.getHeight()), _rbGetDim(rbString).ROI.getSize()));
    }

    public static Dimension _GLgetDimZRatio(RenderingScene scene, double z, Dimension dim) {
        return new Dimension((int) Math.round(scene._GLZRatio(z) * (double) dim.width), (int) Math.round(scene._GLZRatio(z) * (double) dim.height));
    }
    private SoundInput bgSnd = null;

    public SoundInput getBgSnd() {
        return bgSnd;
    }

    public SortedSet<Integer> getBgSndStg() {
        return _bgSndStg;
    }

    public void setBgSnd(SoundInput bgSnd, boolean play) {
        if (this.bgSnd != null) {
            this.bgSnd.stop();
        }
        this.bgSnd = bgSnd;
        bgSnd.setRepeatEnabled(true);
        if (play) {
            bgSnd.play();
        }
    }
    private SelectionMenuComponent default_menu;
    protected SelectionMenu menu_game_selection;

    public SelectionMenu getMenu_game_selection() {
        return menu_game_selection;
    }
    private GameInterface gameInterface;

    public GameInterface getGameInterface() {
        return gameInterface;
    }

    public void reset() {
        try {
            JXAenvUtils2.invokeSwingAndReturn(new SwingStaticReturn() {

                public Object run() {
                    _reset();
                    return null;
                }
            });
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }

    private void _reset() {
        /*
         * off();
         */
        _off();
        if (scene.getToDevice() instanceof JFrame) {
            scene.getToDevice().dispose();
        }
        scene.setToDevice(null);
        _on();
    }
    /*
     * Thread currentStateThread = null;
     */

    public boolean dispatchKeyEvent(KeyEvent e) {
        if (!scene.isKeyboardOptionsEnabled()) {
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_F2:
                        scene.setConsoleEnabled(!scene.isConsoleEnabled());
                        return true;
                    case KeyEvent.VK_PAGE_DOWN:
                        scene.getGCO().scrollDown();
                        return true;
                    case KeyEvent.VK_PAGE_UP:
                        scene.getGCO().scrollUp();
                        return true;
                    case KeyEvent.VK_F4:
                        switchFullScreen();
                        return true;
                    default:
                        break;
                }
            }
        }
        if (!scene.isConsoleEnabled()) {
            /*
             * if ((gameState.state & Game._bits._getMask(Game._STATE) &
             * Game.STATE_MENU) != 0) {
             */
            if (menu_game_selection instanceof SelectionMenu) {
                menu_game_selection.dispatchKeyEvent(e);
            }
            /*
             * }
             * if ((gameState.state & Game._bits._getMask(Game._STATE) &
             * Game.STATE_READY_TO_START) != 0) { if (e.getID() ==
             * KeyEvent.KEY_RELEASED && menu_player_selection instanceof
             * SelectionMenuComponent) { menu_player_selection.keyReleased(e); }
             * else if (e.getID() == KeyEvent.KEY_PRESSED &&
             * menu_player_selection instanceof SelectionMenuComponent) {
             * menu_player_selection.keyPressed(e); } } if ((gameState.state &
             * Game._bits._getMask(Game._STATE) & Game.STATE_PAUSED) != 0) { if
             * (e.getID() == KeyEvent.KEY_RELEASED && menu_game_selection
             * instanceof SelectionMenuComponent) {
             * menu_game_selection.keyReleased(e); } else if (e.getID() ==
             * KeyEvent.KEY_PRESSED && menu_game_selection instanceof
             * SelectionMenuComponent) { menu_game_selection.keyPressed(e); } }
             */
        }
        return e.isConsumed();
    }

    /**
     * Initializes a new created FieldGui object, and it sets the properties
     */
    public FieldGui(int layout, GameInterface gameInterface, Container container, Object constraints, GameProperties props) {
        this.layout = layout;
        console = new ConsoleInterface(System.out, System.err);
        this.gameInterface = gameInterface;
        this.container = container;
        this.constraints = constraints;
        List<SelectionMenuCell> l = new ArrayList<SelectionMenuCell>();
        l.add(new SelectionMenuCell(this, "restart game", new Runnable() {

            public void run() {
                reset();
            }
        }, null));
        default_menu = new SelectionMenuComponent(this, SCREEN_DEFAULT, l);
        menu_game_selection = new SelectionMenu(default_menu);
        setGroupMonitor(new Monitor(), new Monitor());
        initGraphics((props.getDebug() & GameProperties._DEBUG_GUI) == 0);
        setProps(props);
        initLayers();
        gameState = Game.UNKNOWN_GAMESTATE;
    }

    public XtendedModel[] getSelectedPlayer() {
        XtendedModel[] models = new XtendedModel[selectedPlayer.length];
        for (int i = 0; i < selectedPlayer.length; i++) {
            models[i] = selectedPlayer[i] instanceof SortedMap ? (XtendedModel) selectedPlayer[i].get("model") : null;
        }
        return models;
    }

    /**
     * returns the starting location for the specfied player on the current
     * scene bounds, that's the distance from the left-side of the virtual field
     */
    public Point2D getStartingPositions(int playerNum) {
        boolean gl = isLWJGLRendering();
        Rectangle fieldBounds = getBackFieldBounds();
        if (playerNum < NPLAYERS / 2f) {
            return new Point((int) ((float) fieldBounds.getWidth() * 1f / 4f), getFloorLocation(gl));
        }
        if (playerNum >= NPLAYERS / 2f) {
            return new Point((int) ((float) fieldBounds.getWidth() * 3f / 4f), getFloorLocation(gl));
        }
        return new Point();
    }

    /**
     * returns floor location on the current scene bounds, that's the distance
     * between the top-side and the virtual field floor
     */
    public int getFloorLocation(boolean gl) {
        return gl ? scene.getHeight() : (int) Math.round((float) scene.getHeight() * 6f / 7f);
    }

    public RenderingScene getScene() {
        return scene;
    }
    private ConsoleInterface console;
    final String backgroundsAttr = Fighter.rbAba.getString("linkedModelsArrayAttributeName");
    final String ballAttr = Fighter.rbAba.getString("linkedModelAttributeName");

    public void loadContentsOnGL(final Object sp, final List<LoadListener2> ll) {
        scene.doTaskOnGL(new Runnable() {

            public void run() {
                if (isDebugEnabled()) {
                    System.out.println("xxxx load VRAM xxxx");
                }
                long loadTime = System.currentTimeMillis();
                if (sp instanceof Model || sp instanceof Sprite) {
                    if (isLWJGLRendering() && scene._GLgetLoadState(sp) == RenderingScene._GLLOADSTATE_Cleared) {
                        if (isDebugEnabled()) {
                            System.out.println("xx load " + sp + " xx");
                        }
                        if (sp instanceof Model) {
                            Model bg = (Model) sp;
                            scene._GLloadModel((Model) sp, scene, ll);
                        }
                        if (sp instanceof Animation) {
                            scene._GLloadAnim((Animation) sp, scene, RenderingScene.PTY_ANIM, ll);
                        }
                        if (sp instanceof Sprite) {
                            scene._GLloadSprite((Sprite) sp, scene);
                        }
                        if (isDebugEnabled()) {
                            System.out.println("xx done " + (System.currentTimeMillis() - loadTime) + "ms xx");
                        }
                    }
                }
            }
        });
    }

    public void unloadContentsOnGL(final Object sp) {
        scene.doTaskOnGL(new Runnable() {

            public void run() {
                if (isDebugEnabled()) {
                    System.out.println("xxxx unload VRAM xxxx");
                }
                long loadTime = System.currentTimeMillis();
                if (sp instanceof Model || sp instanceof Sprite) {
                    if (scene._GLgetLoadState(sp) == RenderingScene._GLLOADSTATE_Loaded) {
                        if (isDebugEnabled()) {
                            System.out.println("xx unload " + sp + " xx");
                        }
                        if (sp instanceof Model) {
                            RenderingScene._GLunloadModel((ModelGLHandler) Model._GLHandlers.getHandler((RenderingSceneComponent) sp));
                        }
                        /*
                         * CAuTION : Animation is an extended Sprite
                         */
                        if (sp instanceof Animation) {
                            RenderingScene._GLunloadAnim((AnimationGLHandler) Animation._GLHandlers.getHandler((RenderingSceneComponent) sp));
                        }
                        if (sp instanceof Sprite) {
                            scene._GLunloadSprite((SpriteGLHandler) Sprite._GLHandlers.getHandler((RenderingSceneComponent) sp));
                        }
                        if (isDebugEnabled()) {
                            System.out.println("xx done " + (System.currentTimeMillis() - loadTime) + "ms xx");
                        }
                    }
                }
            }
        });
    }

    private void initGraphics(boolean enableHUDConsole) {
        try {
            RenderingScene._GLclearColor = BufferIO._wrapf(Color.WHITE.getRGBComponents(null));
            scene = new RenderingScene(container.getGraphicsConfiguration().getDevice());
            scene.setGL_NEARVAL(sceneNear);
            scene.setGL_FARVAL(sceneFar);
            /*
             * scene.setLoadingAnimLogo("/net/sf/jiga/xtended/impl/flares", 0,
             * 23, "flares", ".png", new Dimension(100, 20),
             * GUIConsoleOutput.CENTER, GUIConsoleOutput.CENTER);
             */
            scene.addListener(this);
            /**
             * MUTE SOUND
             */
            scene.setSoundMute(true);
            /**
             *
             */
            scene.addKeyboardEventsDispatcher(this);
            scene.addBeyondOffscreen(new GameActionLayer("UI GamePad dispatcher") {

                @Override
                protected void performTask() {
                    super.performTask();
                    if (!scene.isConsoleEnabled()) {
                        if ((gameState.state & Game._bits._getMask(Game._STATE) & (Game.STATE_MENU | Game.STATE_READY_TO_START | Game.STATE_PAUSED)) != 0) {
                            if (Sf3Player._availableGamepads.containsKey(0)) {
                                ControllersHandler._proceedGamePadEvents(Sf3Player._availableGamepads.get(0), scene, Player.id.ONE, FieldGui.this);
                            }
                        }
                    }
                }
            });
            scene.addActionOffscreen(new GameActionLayer("graphics") {

                @Override
                protected void renderToGL(RenderingScene gld) {
                    super.renderToGL(gld);
                    glow.glow();
                    GLdraw(gld);
                    /*
                     * GLfader(gld); /** TODO: HUD overlay
                     *
                     * scene.getHud_user().setBounds(0, 0, scene.getWidth(),
                     * scene.getHeight(), playerDepth + 1);
                     * scene.getHud_user().GLbegin(); /*
                     * scene.getHud_user().GLrenderContents(scene);
                     *
                     * scene.getHud_user().GLend();
                     */
                }
            });
            scene.getHud_user().pushContent("app specs", getClass().getPackage().getSpecificationVersion(), TextLayout.POS.LEFT, TextLayout.POS.TOP, true);
            if (enableHUDConsole) {
                /**
                 * by default it is the System.out as printstream, here it
                 * redirects to a cusotmized app logger
                 */
                console = new ConsoleInterface(scene.getPrintStream(), scene.getPrintStream());
            }
            console.println("FIELDGUI CONSOLE initied v." + console.getClass().getPackage().getSpecificationVersion());
        } catch (LWJGLException ex) {
            ex.printStackTrace();
        }
    }

    public void GLfader(RenderingScene gld) {
        try {
            GLFX._GLpushRender("_fader", this, new Object[]{gld}, new Class[]{RenderingScene.class});
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * DO NOT USE directly, it's for internal callback
     */
    public void _fader(RenderingScene gld) {
        double faderZ = -1;
        if (faderEnabled) {
            int fadeSign = _FADEINSTART != 0L ? 1 : (_FADEOUTSTART != 0L ? -1 : 0);
            long fadeStart = _FADEINSTART != 0L ? _FADEINSTART : _FADEOUTSTART != 0L ? _FADEOUTSTART : 0L;
            if (fadeSign != 0) {
                long now = System.nanoTime();
                if (now - fadeStart > _FADETIME) {
                    /**
                     * stop and reset fader if time elapsed
                     */
                    _FADEINSTART = _FADEOUTSTART = 0;
                } else {
                    _FADERValue = Math.max(0f, Math.min(1f,
                            (fadeSign > 0 ? 0f : 1f) + fadeSign * ((double) (now - fadeStart) / (double) _FADETIME)));
                    GLFX.beginRenderBlock();
                    /**
                     * draw a black screen with a growing hole by using the
                     * stencil clipper
                     */
                    Ellipse2D clipEllipse = new Ellipse2D.Float();
                    Rectangle2D blackScreen = new Rectangle2D.Float();
                    blackScreen.setFrameFromCenter(scene.getBounds().getCenterX() - scene.getBounds().getX(), scene.getBounds().getCenterY() - scene.getBounds().getY(), scene._GLZRatio(faderZ) * scene.getWidth(), scene._GLZRatio(faderZ) * scene.getHeight());
                    clipEllipse.setFrameFromCenter(blackScreen.getCenterX(), blackScreen.getCenterY(), _FADERValue * blackScreen.getWidth(), _FADERValue * blackScreen.getHeight());
                    GL11.glPushAttrib(GL11.GL_STENCIL_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
                    GL11.glEnable(GL11.GL_STENCIL_TEST);
                    GL11.glColorMask(false, false, false, false);
                    GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 1);
                    GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
                    GL11.glDisable(GL11.GL_DEPTH_TEST);
                    GLGeom._GLRenderCircle(gld, "fader-ellipse".hashCode(), false, (float) clipEllipse.getCenterX(), (float) clipEllipse.getCenterY(), (float) faderZ, (float) (clipEllipse.getWidth() / 2f), 50, 1f, true);
                    GL11.glColorMask(true, true, true, true);
                    GL11.glStencilFunc(GL11.GL_NOTEQUAL, 1, 1);
                    GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
                    GLGeom._GLfillPolygon(gld, false, blackScreen, GLGeom.getCWrgbaColors(Color.BLACK, 1), faderZ);
                    /**
                     * erase the used Stencil area
                     */
                    GL11.glColorMask(false, false, false, false);
                    GL11.glStencilFunc(GL11.GL_GREATER, 0, 1);
                    GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
                    GLGeom._GLfillPolygon(gld, false, blackScreen, GLGeom.getCWrgbaColors(Color.BLACK, 1), faderZ);
                    GL11.glPopAttrib();
                    Util.checkGLError();
                    GLFX.endRenderBlock();
                }
            }
        }
    }
    private int layout;

    public int getLayout() {
        return layout;
    }

    private void _off() {
        if (bgSnd instanceof SoundInput) {
            bgSnd.stop();
        }
        scene.stopRendering();
        if (scene.isFullscreen()) {
            scene.getToDevice().getContentPane().removeAll();
            scene.getToDevice().getContentPane().validate();
        } else {
            container.remove(scene);
            container.validate();
        }
        scene.setVisible(false);
    }

    /**
     *
     */
    private void _on() {
        /*
         * scene.setMultiThreadingEnabled(_multiThreading);
         */
        if (scene.isFullscreen()) {
            scene.getToDevice().getContentPane().removeAll();
            scene.getToDevice().getContentPane().add(scene);
            scene.getToDevice().validate();
        } else {
            if (guiContent instanceof JComponent) {
                container.remove(guiContent);
            }
            container.add(scene, constraints);
            container.validate();
        }
        scene.setVisible(true);
        scene.setExclusiveMode(layout);
        switchDisplayMode(selectedDM);
        scene.startRendering();
        _updateGameState(gameState, true);
        if (menu_game_selection instanceof SelectionMenu) {
            menu_game_selection.getCurrent().reset();
        }
        scene.requestFocusInWindow();
    }

    public void on() {
        if (ThreadWorks.Swing.isEventDispatchThread()) {
            _on();
        } else {
            try {
                ThreadWorks.Swing.invokeAndWait(new Runnable() {

                    public void run() {
                        _on();
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    private final static int modelResolutionDEFAULT = SpritesChar.HIGH;
    private int modelResolution = modelResolutionDEFAULT;

    public int getModelResolution() {
        return modelResolution;
    }
    /**
     * how big are the fighters on screen
     */
    static double playersFactor = .5;

    private int modelResolutionPreset(Rectangle sceneBounds, Model refModel, int refSpritesRes) {
        int resPreset = (int) -System.currentTimeMillis();
        double resFactor = (sceneBounds.getHeight() * playersFactor) / refModel.getInsetSize(refSpritesRes).getHeight() * scene._GLZRatio(playerDepth);
        if (SpritesChar.RES_FACTORS_map.get(modelResolution) != resFactor) {
            SpritesChar.RES_FACTORS_map.put(resPreset, resFactor);
            SpritesChar.RES_NAMES_map.put(resPreset, "GUI-live-resolution");
            return resPreset;
        } else {
            return modelResolution;
        }
    }

    /**
     * instances a new Sound interface
     *
     * @return one sound interface
     * @see Sound
     */
    private static SoundInput initSound() {
        return new SoundInput();
    }

    public boolean isLWJGLRendering() {
        if (scene instanceof RenderingScene) {
            return scene.isLWJGLAccel();
        } else {
            return false;
        }
    }

    public Rectangle getBackFieldBounds() {
        boolean gl = isLWJGLRendering();
        Rectangle fieldBounds = new Rectangle((int) ((float) getFloorLocation(gl) * 5f / 2f), getFloorLocation(gl));
        if (gl) {
            fieldBounds.width = (int) Math.round(fieldBounds.width * (float) scene._GLZRatio(bgDepth));
        }
        fieldBounds.setLocation((int) ((float) (scene.getWidth() - fieldBounds.width) / 2f), 0);
        if (isDebugEnabled()) {
            System.out.println("%");
            System.out.println("%");
            System.out.println("% field : " + fieldBounds);
            System.out.println("%");
            System.out.println("%");
        }
        return fieldBounds;
    }
    private Rectangle cameraBounds = new Rectangle();

    /**
     * actual field area that should the camera/user see (field view)
     */
    public Rectangle2D getCameraBounds() {
        Rectangle fieldBounds = getFightersFieldBounds(isLWJGLRendering());
        cameraBounds.setBounds(0, 0, scene.getWidth(), scene.getHeight());
        if ((gameState.state & Game._bits._getMask(Game._STATE) & (Game.STATE_RUNNING | Game.STATE_PAUSED)) != 0) {
            Point2D center = new Point2D.Double(.5 * (gameState.fighters[0].getWidestOpponent().getPosPlayer().getX() + gameState.fighters[0].getPosPlayer().getX()), 0);
            cameraBounds.setFrameFromCenter(center, new Point2D.Double(center.getX() + (cameraBounds.getWidth() / 2.), 0.));
            cameraBounds.setLocation(new Point((int) Math.round(Math.max(fieldBounds.getX(), Math.min(fieldBounds.getMaxX() - cameraBounds.getWidth(), cameraBounds.getX()))), 0));
        }
        if (isDebugEnabled()) {
            System.out.println("%");
            System.out.println("%");
            System.out.println("% camera/field view :" + cameraBounds);
            System.out.println("%");
            System.out.println("%");
        }
        return cameraBounds.getBounds2D();
    }
    private JFrame window = null;
    private JComponent guiContent = null;

    public void popExceptionMessage(Exception e, Class classLoader) {
        console.exceptionCaught(e);
        popMessage(new JButton(new AbstractAction("see console log...") {

            public void actionPerformed(ActionEvent e) {
                consoleShow();
                switchGui(null);
            }
        }), "Exception caught with : " + classLoader, UIMessage.ERROR_TYPE);
    }

    public void popMessage(final JComponent message, final String title, final int UIMessageType) {
        try {
            JXAenvUtils2.invokeSwingAndReturn(new SwingStaticReturn() {

                public Object run() {
                    _popMessage(message, title, UIMessageType);
                    return null;
                }
            });
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }

    private void _popMessage(JComponent message, String title, int UIMessageType) {
        JPanel panel = new JPanel(new BorderLayout(), true);
        panel.setBorder(BorderFactory.createTitledBorder(title));
        JLabel lab = new JLabel("", UIMessage._getIcon(UIMessageType, false), SwingConstants.LEADING);
        panel.add(lab, BorderLayout.WEST);
        panel.add(message, BorderLayout.CENTER);
        panel.validate();
        switchGui(panel, guiContent, null);
    }

    public void popMessage(String message, String title, int UIMessageType) {
        String htmlMessage = "<html>";
        for (String line : message.split(net.sf.jiga.xtended.kernel.Console.newLine)) {
            htmlMessage += line + "<br>";
        }
        htmlMessage += "</html>";
        popMessage(new JLabel(htmlMessage), title, UIMessageType);
    }

    public void switchGui(final JComponent comp) {
        try {
            JXAenvUtils2.invokeSwingAndReturn(new SwingStaticReturn() {

                public Object run() {
                    _switchGui(comp);
                    return null;
                }
            });
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }

    private void _switchGui(JComponent comp) {
        if (comp instanceof JComponent) {
            if (scene.isFullscreen()) {
                GraphicsConfiguration gc = scene.getGraphicsConfiguration();
                window = window instanceof JFrame ? window : new JFrame(gc);
                window.getContentPane().setBackground(scene.getBackground());
                window.getContentPane().setForeground(scene.getForeground());
                if (!window.isDisplayable()) {
                    window.setUndecorated(true);
                }
                _off();
                window.getContentPane().setLayout(new GridBagLayout());
                GridBagConstraints c = new GridBagConstraints();
                c.fill = c.BOTH;
                c.weightx = c.weighty = 1;
                c.gridwidth = c.REMAINDER;
                window.getContentPane().removeAll();
                window.add(comp, c);
                window.getContentPane().validate();
                gc.getDevice().setFullScreenWindow(window);
                window.setVisible(true);
                window.repaint();
            } else {
                _off();
                if (guiContent instanceof JComponent) {
                    container.remove(guiContent);
                }
                container.setBackground(scene.getBackground());
                container.setForeground(scene.getForeground());
                comp = new JScrollPane(comp);
                ((JScrollPane) comp).getHorizontalScrollBar().setUnitIncrement(30);
                ((JScrollPane) comp).getVerticalScrollBar().setUnitIncrement(30);
                container.add(comp, constraints);
                container.validate();
                container.repaint();
            }
        } else {
            if (scene.isFullscreen()) {
                GraphicsConfiguration gc = scene.getToDevice().getGraphicsConfiguration();
                gc.getDevice().setFullScreenWindow(scene.getToDevice());
                if (window instanceof JFrame) {
                    window.setVisible(false);
                }
            }
            _on();
        }
        guiContent = comp;
    }
    /*
     * private Thread onCloseSwitchToTread = null;
     */

    public void switchGui(final JComponent comp, final JComponent onCloseSwitchTo, final Action onCloseAction) {
        try {
            JXAenvUtils2.invokeSwingAndReturn(new SwingStaticReturn() {

                public Object run() {
                    _switchGui(comp, onCloseSwitchTo, onCloseAction);
                    return null;
                }
            });
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }
    ThreadWorks guiTasks = new ThreadWorks("FieldGUI");

    private void _switchGui(JComponent comp, final JComponent onCloseSwitchTo, final Action onCloseAction) {
        if (comp instanceof JComponent) {
            JPanel panel = new JPanel(new GridBagLayout(), true);
            Action a = new AbstractAction("done", UIMessage._getIcon(UIMessage.DELETE_TYPE, true)) {

                public void actionPerformed(final ActionEvent e) {

                    Runnable r = new Runnable() {

                        public void run() {
                            if (onCloseAction instanceof Action) {
                                onCloseAction.actionPerformed(e);
                            }
                            switchGui(onCloseSwitchTo);
                        }
                    };
                    guiTasks.doLater(r);
                }
            };
            GridBagConstraints c = new GridBagConstraints();
            c.fill = c.BOTH;
            c.weightx = c.weighty = 1;
            c.gridwidth = c.REMAINDER;
            panel.add(comp, c);
            c.fill = c.NONE;
            panel.add(new JButton(a), c);
            panel.validate();
            switchGui(panel);
        } else {
            switchGui(null);
        }
    }

    public void switchFullScreen() {
        scene.switchFullScreen(selectedDM);
    }

    public void selectPlayer(XtendedModel model, int playerNum) {
        if (model == null) {
            System.out.println("Invalid selected player! ('" + model + "')");
            selectedPlayer[playerNum] = null;
        } else {
            selectedPlayer[playerNum] = Collections.synchronizedSortedMap(new TreeMap<String, Object>());
            selectedPlayer[playerNum].put("model", model);
            selectedPlayer[playerNum].put("bg", model.getAttribute(backgroundsAttr));
            selectedPlayer[playerNum].put("ball", model.getAttribute(ballAttr));
        }
    }

    void loadSpritesCache(Game gameState, boolean startGL) {
        if (startGL && !scene.isLWJGLAccel()) {
            scene.setLWJGLAccel(true);
        }
        cacheloading.loadBegin();
        cacheloading.loadProgress(0, _cacheSyncManager.keySet().size() + (gameState.backgroundModels instanceof XtendedModel[] ? gameState.backgroundModels.length : 0) + (selectedPlayer instanceof SortedMap[] ? selectedPlayer.length : 0));
        int i = 0;
        synchronized (_cacheSyncManager) {
            for (String k : _cacheSyncManager.keySet()) {
                final int n = i++;
                final Serializable sp = _cacheSyncManager.get(k);
                Vector<LoadListener2> ll = new Vector<LoadListener2>();
                ll.add(new LoadAdapter() {

                    @Override
                    public void loadIsComplete() {
                        super.loadIsComplete();
                        cacheloading.loadProgress(n, cacheloading.getMax());
                        if (n == cacheloading.getMax()) {
                            cacheloading.loadIsComplete();
                        }
                    }

                    public int hashLinkToGLObject() {
                        return sp.hashCode();
                    }
                });
                ll.add(cacheItemloading);
                loadContentsOnGL(sp, ll);
            }
        }
        if (gameState.backgroundModels instanceof XtendedModel[]) {
            if (isDebugEnabled()) {
                System.out.println(backgroundsAttr + "...[" + gameState.backgroundModels.length + "]");
            }
            for (final XtendedModel bg : gameState.backgroundModels) {
                final int n = i++;
                if (bg instanceof Model) {
                    if (isDebugEnabled()) {
                        System.out.println(bg.id);
                    }
                    Vector<LoadListener2> ll = new Vector<LoadListener2>();
                    ll.add(new LoadAdapter() {

                        @Override
                        public void loadIsComplete() {
                            super.loadIsComplete();
                            cacheloading.loadProgress(n, cacheloading.getMax());
                            if (n == cacheloading.getMax()) {
                                cacheloading.loadIsComplete();
                            }
                        }

                        public int hashLinkToGLObject() {
                            return bg.hashCode();
                        }
                    });
                    ll.add(cacheItemloading);
                    loadContentsOnGL(bg, ll);
                }
            }
        }
        if (selectedPlayer instanceof SortedMap[]) {
            if (isDebugEnabled()) {
                System.out.println("players...[" + selectedPlayer.length + "]");
            }
            for (final SortedMap plrMap : selectedPlayer) {
                final int n = i++;
                if (plrMap instanceof SortedMap) {
                    final XtendedModel plr = (XtendedModel) plrMap.get("model");
                    if (plr instanceof XtendedModel) {
                        if (isDebugEnabled()) {
                            System.out.print(plr.id);
                        }

                        if (plr.getAttribute("ball") instanceof XtendedModel) {
                            loadContentsOnGL((XtendedModel) plr.getAttribute("ball"), new Vector<LoadListener2>());
                        }
                        Vector<LoadListener2> ll = new Vector<LoadListener2>();
                        ll.add(new LoadAdapter() {

                            @Override
                            public void loadIsComplete() {
                                super.loadIsComplete();
                                cacheloading.loadProgress(n, cacheloading.getMax());
                                if (n == cacheloading.getMax()) {
                                    cacheloading.loadIsComplete();
                                }
                            }

                            public int hashLinkToGLObject() {
                                return plr.hashCode();
                            }
                        });
                        ll.add(cacheItemloading);
                        loadContentsOnGL(plr, ll);
                    }
                }
            }
        }

        scene.doTaskOnGL(new Runnable() {

            public void run() {
                cacheloading.loadIsComplete();
            }
        });
    }
    private java.util.List<LoadListener> cacheLoadListeners = new Vector<LoadListener>();

    public void addLoadListener(LoadListener listener) {
        cacheLoadListeners.add(listener);
    }

    public void removeLoadListener(LoadListener listener) {
        cacheLoadListeners.remove(listener);
    }

    void clearSpritesCache(final boolean stopGL) {
        cacheloading.unloadBegin();
        synchronized (_cacheSyncManager) {
            for (String k : _cacheSyncManager.keySet()) {
                Serializable sp = _cacheSyncManager.get(k);
                unloadContentsOnGL(sp);
            }
        }
        if (gameState.backgroundModels instanceof XtendedModel[]) {
            for (XtendedModel bg : gameState.backgroundModels) {
                unloadContentsOnGL(bg);
            }
        }
        if (selectedPlayer instanceof SortedMap[]) {
            for (SortedMap plrMap : selectedPlayer) {
                if (plrMap instanceof SortedMap) {
                    XtendedModel plr = (XtendedModel) plrMap.get("model");
                    if (plr instanceof XtendedModel) {
                        if (plrMap.get("ball") instanceof XtendedModel) {
                            unloadContentsOnGL((Model) plrMap.get("ball"));
                        }
                        unloadContentsOnGL(plr);
                    }
                }
            }
        }
        scene.doTaskOnGL(new Runnable() {

            public void run() {
                cacheloading.unloadIsComplete();
                if (stopGL && scene.isLWJGLAccel()) {
                    scene.setLWJGLAccel(false);
                }
            }
        });
    }

    private void consoleLogInfo() {
        if (scene instanceof RenderingScene) {
            console.println("***** scene info *****");
            console.println("scene FPS : " + scene.getFPS());
            console.println("scene width : " + scene.getWidth());
            console.println("scene height : " + scene.getHeight());
            console.println("available player models : " + availablePlayerModels.size());
            console.println("available background models : " + availableBackgroundModels.size());
            console.println("object cache (w/o models) size : " + _cacheSyncManager.keySet().size());
            console.println("memory RAM usage : " + JXAenvUtils2._getMemoryStatusTK().getHeapMemStatus().getValue() / JXAenvUtils2._getMemoryStatusTK().getHeapMemStatus().getMaximum() * 100 + " %");
            console.println("swap disk usage : " + JXAenvUtils._getSwapUsage() / Math.pow(1024, 2) + "MB");
            console.println("system architecture : " + JXAenvUtils._systemBean().getArch());
            Map<String, String> gfx = JXAenvUtils2._graphicsInfo();
            console.println("graphics device : " + gfx.get(RenderingScene._GL_VENDOR + " " + gfx.get(RenderingScene._GL_RENDERER) + " " + gfx.get(RenderingScene._GL_VERSION)));
            console.println("**********************");
        }
    }

    private String preConcatStackTrace(String s) {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        if (stack.length >= 3) {
            s = stack[2].getClassName() + "." + stack[2].getMethodName() + " : " + s;
        }
        return s;
    }

    public void logLn(final String s) {
        if (!scene.isScreenThread()) {
            scene.doTaskOnGL(new Runnable() {

                public void run() {
                    _logLn(s);
                }
            });
        } else {
            _logLn(s);
        }
    }

    private void _logLn(String s) {
        assert scene.isScreenThread() : "running outside screen thread !";
        if (scene instanceof RenderingScene) {
            if (isDebugEnabled()) {
                s = preConcatStackTrace(s);
                System.out.println(s);
            } else {
                scene.getGCO().newLogPacket(s + net.sf.jiga.xtended.kernel.Console.newLine);
            }
        }
    }

    public void log(final String s) {
        if (!scene.isScreenThread()) {
            scene.doTaskOnGL(new Runnable() {

                public void run() {
                    _log(s);
                }
            });
        } else {
            _log(s);
        }
    }

    private void _log(String s) {
        assert scene.isScreenThread() : "running outside screen thread !";
        if (scene instanceof RenderingScene) {
            if (isDebugEnabled()) {
                s = preConcatStackTrace(s);
                System.out.print(s);
            } else {
                scene.getGCO().newLogPacket(s);
            }
        }
    }

    public void consoleShow() {
        if (scene instanceof RenderingScene) {
            scene.setConsoleEnabled(true);
        }
    }

    public void consoleHide() {
        if (scene instanceof RenderingScene) {
            scene.setConsoleEnabled(false);
        }
    }

    public GameProperties getProps() {
        return props;
    }

    /**
     * Set the properties.
     */
    public void setProps(GameProperties props) {
        this.props = props;
        RenderingScene.setDebugRenderEnabled((props.getDebug() & GameProperties._DEBUG_RENDER) != 0);
        scene.setDebugActivityEnabled((props.getDebug() & GameProperties._DEBUG_LOADING_ACTIVITY) != 0);
        scene.setKeyboardOptionsEnabled((props.getDebug() & GameProperties._DEBUG_GUI) != 0);
        DebugMap._getInstance().setJXADebugEnabled((props.getDebug() & GameProperties._DEBUG_JXA) != 0);
        DebugMap._getInstance().setJXADebugSysEnabled((props.getDebug() & GameProperties._DEBUG_JXA_KERNEL) != 0);
        Sprite._getRenderingHints().clear();
        Sprite._getRenderingHints().putAll(props.rendering);
        synchronized (props.bgModels) {
            FieldGui.clearBackgroundModelsStack();
            for (Iterator<Map.Entry<String, XtendedModel[]>> it = props.bgModels.entrySet().iterator(); it.hasNext();) {
                final Map.Entry<String, XtendedModel[]> s = it.next();
                FieldGui.setBackgroundModels(s.getKey(), s.getValue());
            }
        }
        synchronized (props.characters) {
            FieldGui.clearPlayerModelsStack();
            for (XtendedModel model : props.characters) {
                FieldGui.addPlayerModel(model);
            }
        }
        Player._gamepadCodeMap.putAll(props.gamepadCodeMap);
        Player._keyCodeMap.putAll(props.keyboardCodeMap);
        RenderingScene.lwjglEnabled = props.lwjglEnabled;
        switchDisplayMode(new DisplayModeWrapper(props.dm.getDM()));
    }
    /**
     *
     */
    private Map<Integer, Thegamelayer> gamestateLayers = Collections.synchronizedMap(new HashMap<Integer, Thegamelayer>());

    public Thegamelayer getGameLayer(int gameState) {
        return getActionLayerForGameState(gameState);
    }
    /**
     * TODO : enable
     */
    private static boolean faderEnabled = false;
    /**
     *
     */
    private static double _FADERValue = 0f;
    /**
     *
     */
    private static long _FADEINSTART = 0L;
    private static long _FADEOUTSTART = 0L;
    private static long _FADETIME = 3L * 10 ^ 9;

    private Thegamelayer getActionLayerForGameState(int gameState) {
        synchronized (gamestateLayers) {
            for (int state : gamestateLayers.keySet()) {
                if ((state & Game._bits._getMask(Game._STATE) & gameState) != 0) {
                    return gamestateLayers.get(state);
                }
            }
        }
        return gamestateLayers.get(Game.STATE_UNKNOWN);
    }

    public void updateGameState(Game gameState) {
        _updateGameState(gameState, false);
    }

    private void _updateGameState(Game gameState, boolean forceRefresh) {
        if (!(gameState instanceof Game)) {
            gameState = Game.UNKNOWN_GAMESTATE;
        }
        boolean refresh = true;
        if (this.gameState instanceof Game) {
            /**
             * same state
             */
            if (!forceRefresh && getGameLayer(gameState.state).equals(getGameLayer(this.gameState.state))) {
                refresh = false;
            } else {
                /**
                 * switch state
                 */
                boolean removed = scene.removeActionOffscreen(getGameLayer(this.gameState.state));
                if (isDebugEnabled()) {
                    if (removed) {
                        if (isDebugEnabled()) {
                            System.err.println("FieldGui : renderingscene removed one or more action layers !");
                        }
                    }
                }
            }
        }
        this.gameState = gameState;
        if (forceRefresh || refresh) {
            /**
             * switch rendering layer
             */
            if (getGameLayer(this.gameState.state).isLwjgl() != scene.isLWJGLAccel()) {
                scene.setLWJGLAccel(getGameLayer(this.gameState.state).isLwjgl());
            }
            scene.addActionOffscreen(getGameLayer(this.gameState.state));
            _FADEINSTART = System.currentTimeMillis();
        }
    }

    public void gameLoading(Game gameState) {
        updateGameState(gameState);
    }

    public void gameCanceled(Game gameState) {
        updateGameState(gameState);
        for (int i = 0; i < selectedPlayer.length; i++) {
            selectedPlayer[i] = null;
        }
        /*
         * switchGui(null);
         */
        if (isDebugEnabled()) {
            System.out.println(getClass().getName() + " - game has been canceled");
        }
    }

    /**
     * The ServerAlgorithm (or RemoteAlgorithm) calls this method when the game
     * starts
     *
     * @param startPlayer the player that has initially the ball. 0 and 1 for
     * player 1 and 2 respectively
     * @param gameState the initial state of the game
     */
    public void gameStarted(int startPlayer, Game gameState) {
        updateGameState(gameState);
        if (isDebugEnabled()) {
            System.out.println("Game STARTED");
        }
    }

    /**
     * The ServerAlgorithm (or RemoteAlgorithm) calls this method when the game
     * is paused
     */
    public void gamePaused(Game gameState) {
        updateGameState(gameState);
        if (isDebugEnabled()) {
            System.out.println("Game PAUSED");
        }
    }

    /**
     * The ServerAlgorithm (or RemoteAlgorithm) calls this method when the game
     * is resumed
     */
    public void gameResumed(Game gameState) {
        updateGameState(gameState);
        if (isDebugEnabled()) {
            System.out.println("Game RESUMED");
        }
    }

    /**
     * The ServerAlgorithm (or RemoteAlgorithm) calls this method after each
     * tick of the game
     *
     * @param gameState the actual state of the grame
     * @param ballBounced if true, the ball just touched the boundaries of the
     * field, or the rackets it could be used to play some sound
     */
    public void tickProcessed(Game gameState) {
        updateGameState(gameState);
        if ((gameState.state & Game._bits._getMask(Game._STATE) & Game.STATE_FINISHED) != 0) {
            if (isDebugEnabled()) {
                System.out.println("(->) Player " + (gameState.winner + 1) + " won");
            }
        }
    }

    /**
     * Methode pour dessiner les raquettes.
     *
     * @param cJ1 Couleur pour le joueur 1
     * @param cJ2 Couleur pour le joueur 2
     */
    boolean GLdrawPlayers(RenderingScene context, Rectangle fieldBounds, Color cJ1, Color cJ2) {
        boolean b = true;
        if (!availablePlayerModels.isEmpty()) {
            modelResolution = modelResolutionPreset(scene.getBounds(), availablePlayerModels.get(0), modelResolutionDEFAULT);
        }
        for (int i = 0; i < selectedPlayer.length; i++) {
            SortedMap plrMap = selectedPlayer[i];
            if (plrMap instanceof SortedMap) {
                XtendedModel plr = (XtendedModel) plrMap.get("model");
                if (plr.GLisLoaded()) {
                    Rectangle bounds = new Rectangle();
                    bounds.setSize(plr.getWidth(modelResolution), plr.getHeight(modelResolution));
                    plr.setRenderingScene(scene);
                    bounds.setLocation(gameState.fighters[i].getModelPaintLocation(modelResolution));
                    plr.setBounds(bounds);
                    plr.GLrunValidate();
                    Model._GLRenderModel(context, plr, plr.getBounds(), playerDepth, GLFX.gfx.FX_DOWNREFLEXION_EXT, new Point(0, fieldBounds.height), null, 0, null, null, null);
                    Map<Integer, Map<String, Object>> tag = gameState.fighters[i].getCollision_tag();
                    int shortTag = 0;
                    if (tag.containsKey(Fighter.FIGHTERMACHINE_A)) {
                        shortTag = Fighter.FIGHTERMACHINE_A;
                    }
                    if (tag.containsKey(Fighter.FIGHTERMACHINE_D)) {
                        shortTag = Fighter.FIGHTERMACHINE_D;
                    }
                    /*
                     * if (shortTag != 0) { int fullTag = (Integer)
                     * tag.get(shortTag).get("fullTag"); Rectangle collRect =
                     * (Rectangle) tag.get(shortTag).get("intersection"); if
                     * ((fullTag & (Fighter.FIGHTERMACHINE_A |
                     * Fighter.FIGHTERMACHINE_D)) != 0) { Animation hit = null;
                     * String side = ""; if (plr.getLocation().x <
                     * cameraBounds.getCenterX()) { side = "_L"; } if
                     * (plr.getLocation().x > cameraBounds.getCenterX()) { side
                     * = "_R"; } Dimension origSize = null; if ((fullTag &
                     * Fighter.FIGHTERMACHINE_A) != 0) { if ((fullTag &
                     * Fighter.FIGHTERMACHINE_Air) != 0) { hit = (Animation)
                     * buffer.get("FX_FIGHTER_big_hit" + side); origSize =
                     * (Dimension) buffer.get("DIM_FX_FIGHTER_big_hit"); } else
                     * if ((fullTag & Fighter.FIGHTERMACHINE_Stance) != 0) { hit
                     * = (Animation) buffer.get("FX_FIGHTER_medium_hit" + side);
                     * origSize = (Dimension)
                     * buffer.get("DIM_FX_FIGHTER_medium_hit"); } else { hit =
                     * (Animation) buffer.get("FX_FIGHTER_small_hit" + side);
                     * origSize = (Dimension)
                     * buffer.get("DIM_FX_FIGHTER_small_hit"); } } else if
                     * ((fullTag & Fighter.FIGHTERMACHINE_D) != 0) { hit =
                     * (Animation) buffer.get("FX_FIGHTER_block" + side);
                     * origSize = (Dimension)
                     * buffer.get("DIM_FX_FIGHTER_block"); } if (hit instanceof
                     * Animation) { hit.setRenderingScene(scene);
                     * hit.setSize((int) (origSize.width *
                     * SpritesChar.RES_FACTORS_map.get(modelResolution)), (int)
                     * (origSize.height *
                     * SpritesChar.RES_FACTORS_map.get(modelResolution)));
                     * hit.play(); hit.setLocation((int) (collRect.getCenterX()
                     * - (float) hit.getWidth() / 2f), (int)
                     * (collRect.getCenterY() - (float) hit.getHeight() / 2f));
                     * Animation._GLRenderAnimation(context,
                     * Animation._GLgetHandler(hit), hit.getBounds(),
                     * playerDepth); } } }
                     */
                    if (isDebugEnabled()) {
                        Color c = Color.GREEN;
                        Rectangle[] defBounds = gameState.fighters[i].getCollisionBounds(gameState.fighters[i].getCollisionDefBounds(), modelResolution);
                        for (Rectangle r : defBounds) {
                            GLGeom._GLdrawPolygon(context, false, bounds, playerDepth, 0, null, c);
                        }
                        c = Color.ORANGE;
                        Rectangle[] atkBounds = gameState.fighters[i].getCollisionBounds(gameState.fighters[i].getCollisionAtkBounds(), modelResolution);
                        for (Rectangle r : atkBounds) {
                            GLGeom._GLdrawPolygon(context, false, bounds, playerDepth, 0, null, c);
                        }
                        c = i == 0 ? cJ1 : cJ2;
                        GLGeom._GLdrawPolygon(context, true, gameState.fighters[i].getBounds2D(modelResolution).getBounds(), (double) playerDepth, 0, null, c);
                        Ellipse2D circle = gameState.fighters[i].getBoundsCircle(modelResolution);
                        GLGeom._GLRenderCircle(context, ("boundscircle-" + gameState.fighters[i].hashCode()).hashCode(), true, (float) circle.getBounds2D().getCenterX(), (float) circle.getBounds2D().getCenterY(), playerDepth, (float) circle.getHeight() / 2f, 50, 1, false);
                    }
                }
            }
        }
        return b;
    }

    /**
     * Ecrit le nom en bas a gauche de l'�cran.
     */
    boolean GLdrawName(RenderingScene context, Color couleur, String name) {
        int font = 3;
        Dimension dim = _GL_baseCodeStringCLIPSIZE(context, name, font);
        _GL_baseCodeStringDRAW(context, 20, scene.getHeight() - dim.height - 20, - 1, dim, name);
        return true;
    }

    boolean GLdrawBall(RenderingScene context, Rectangle fieldBounds, Color couleur) {
        boolean b = true;
        int i = 0;
        for (SortedMap plrMap : selectedPlayer) {
            if (plrMap instanceof SortedMap) {
                if (plrMap.get("ball") instanceof XtendedModel) {
                    XtendedModel ball = (XtendedModel) plrMap.get("ball");
                    if (ball instanceof XtendedModel) {
                        Rectangle bounds = new Rectangle();
                        bounds.setLocation(gameState.fighters[i].getBall().getModelPaintLocation(modelResolution));
                        bounds.setSize(Math.round((float) context._GLZRatio(playerDepth) * ball.getWidth(modelResolution)), Math.round((float) context._GLZRatio(playerDepth) * ball.getHeight(modelResolution)));
                        ball.setBounds(bounds);
                        ball.GLrunValidate();
                        Model._GLRenderModel(scene, ball, ball.getBounds(), playerDepth);
                    }
                }
            }
            i++;
        }
        return b;
    }

    PerspectiveTransform makeBackgroundLayerPTX(Rectangle fieldBounds, int layer, int layersStackLength, Dimension originalBgDim) {
        Polygon shape = makeBackgroundLayerShape(fieldBounds, layer, layersStackLength);
        PerspectiveTransform ptx = shape instanceof Polygon ? PerspectiveTransform.getQuadToQuad(
                (float) shape.xpoints[0], (float) shape.ypoints[0],
                (float) shape.xpoints[1], (float) shape.ypoints[1],
                (float) shape.xpoints[2], (float) shape.ypoints[2],
                (float) shape.xpoints[3], (float) shape.ypoints[3],
                0f, 0f,
                (float) originalBgDim.getWidth(), 0f,
                (float) originalBgDim.getWidth(), (float) originalBgDim.getHeight(),
                0f, (float) originalBgDim.getHeight()) : new PerspectiveTransform();
        if (shape instanceof Polygon) {
            try {
                ptx.preConcatenate(AffineTransform.getTranslateInstance(-shape.getBounds().x, -shape.getBounds().y).createInverse());
            } catch (NoninvertibleTransformException ex) {
                ex.printStackTrace();
            }
        }
        return ptx;
    }

    Polygon makeBackgroundLayerShape(Rectangle fieldBounds, int layer, int layersStackLength) {
        Camera cam = new Camera(
                new Point((int) scene.getBounds().getCenterX(),
                (int) scene.getBounds().getCenterY()),
                new Dimension(fieldBounds.width, scene.getHeight()),
                scene.getBounds().height - getFloorLocation(false),
                scene.getBounds().height - getFloorLocation(false));
        Polygon shape = null;
        switch (layer) {
            case 0:
                shape = cam.getRoofShape();
                break;
            case 1:
                shape = cam.getFloorShape();
                break;
            default:
                shape = cam.getBackgroundRect();
                int centerLayerIndex = layer - 2;
                int centerLayersAmount = layersStackLength - 2;
                Rectangle centerLayerBounds = (Rectangle) shape.getBounds().clone();
                centerLayerBounds.grow(
                        (int) ((float) (cam.getRoofShape().xpoints[1] - cam.getRoofShape().xpoints[0] - centerLayerBounds.getWidth()) * (0.5f * (float) centerLayerIndex / (float) centerLayersAmount)),
                        0);
                shape = new Polygon(
                        new int[]{
                            (int) centerLayerBounds.getX(), (int) centerLayerBounds.getMaxX(), (int) centerLayerBounds.getMaxX(), (int) centerLayerBounds.getX()
                        },
                        new int[]{
                            (int) centerLayerBounds.getY(), (int) centerLayerBounds.getY(), (int) centerLayerBounds.getMaxY(), (int) centerLayerBounds.getMaxY()
                        }, 4);
                break;
        }
        return shape;
    }
    /**
     * RENDERINGSCENE ASPECT (usually half cut the projection-pyramide i.e. near
     * == far / 2)
     */
    public final static int sceneNear = 500, sceneFar = 1000;
    public final static float bgDepth = -(sceneFar - sceneNear) / 2f;
    public final static float playerDepth = -3f;

    /**
     * draws the screen indexed with the specified layer
     *
     * @param layer layers are expressed as follows : <br>0 is roof <br>1 is
     * floor <br>2+ is for the center backgrounds where the greatest index is
     * for the top-most screen, the greater is displayed the larger
     */
    boolean GLdrawBg(RenderingScene context, Rectangle fieldBounds, int layer) {
        boolean b = true;
        Rectangle bounds = new Rectangle();
        GLHandler.stView.push();
        double z = 0;
        GL11.glTranslatef(-(float) (fieldBounds.width - scene.getWidth()) / 2f, -(float) (fieldBounds.height - scene.getHeight()) / 2f, - 1);
        switch (layer) {
            case 0: /*
                 * roof
                 */
                bounds.setBounds(new Rectangle(0, 0, (int) Math.round(fieldBounds.width * context._GLZRatio(bgDepth)), (int) bgDepth));
                GL11.glRotatef(-90f, 1, 0, 0);
                break;
            case 1: /*
                 * floor
                 */
                bounds.setBounds(new Rectangle(0, 0, (int) Math.round(fieldBounds.width * context._GLZRatio(bgDepth)), (int) bgDepth));
                z = fieldBounds.height;
                GL11.glRotatef(-90f, 1, 0, 0);
                break;
            default: /*
                 * center layer
                 */
                bounds.setBounds(0, 0, (int) Math.round(fieldBounds.width * context._GLZRatio(bgDepth)), fieldBounds.height);
                z = bgDepth;
                break;
        }
        if (_cacheHashRegistry.get("grid") instanceof Integer) {
            Sprite._GLRenderSprite(context, (SpriteGLHandler) Sprite._GLHandlers.getHandler(_cacheHashRegistry.get("grid")), bounds, z);
        }
        if (gameState.backgroundModels instanceof XtendedModel[]) {
            if (gameState.backgroundModels.length > layer) {
                XtendedModel bg = gameState.backgroundModels[layer];
                if (bg instanceof XtendedModel) {
                    if (bg.GLisLoaded()) {
                        bg.setRenderingScene(scene);
                        bg.setResolution(modelResolution);
                        bg.GLanim(bg.animsID_default, bg.getFrameRate(), true);
                        bg.GLrunValidate();
                        Model._GLRenderModel(context, bg, bounds, z);
                    }
                }
            }
        }
        GLHandler.stView.pop();
        Util.checkGLError();
        return b;
    }
    /**
     * public void GLdraw(RenderingScene context) throws InterruptedException {
     * RenderingScene._GLrender(RenderingScene._GL_getTopOftheStackMX(GL11.GL_MODELVIEW_MATRIX),
     * "CB_GLdraw", this, new Object[]{context}, new
     * Class[]{RenderingScene.class}); }
     */
    LoadAdapter cacheloading = new LoadAdapter() {

        @Override
        public void loadBegin() {
            super.loadBegin();
            synchronized (cacheLoadListeners) {
                for (LoadListener l : cacheLoadListeners) {
                    l.loadBegin();
                }
            }
        }

        @Override
        public void loadIsComplete() {
            super.loadIsComplete();
            synchronized (cacheLoadListeners) {
                for (LoadListener l : cacheLoadListeners) {
                    l.loadIsComplete();
                }
            }
        }

        @Override
        public void unloadBegin() {
            super.unloadBegin();
            synchronized (cacheLoadListeners) {
                for (LoadListener l : cacheLoadListeners) {
                    l.unloadBegin();
                }
            }
        }

        @Override
        public void unloadIsComplete() {
            super.unloadIsComplete();
            synchronized (cacheLoadListeners) {
                for (LoadListener l : cacheLoadListeners) {
                    l.unloadIsComplete();
                }
            }
        }

        public int hashLinkToGLObject() {
            return 0;
        }
    }, cacheItemloading = new LoadAdapter() {

        public int hashLinkToGLObject() {
            return 0;
        }
    };
    static Color bgcolor = Color.WHITE;

    /**
     * internal callback
     */
    public void GLdraw(RenderingScene context) {

        /*
         * Rectangle backgroundColor = new Rectangle(context.getWidth(),
         * context.getHeight()); backgroundColor.width *=
         * RenderingScene._GLZRatio(bgz); backgroundColor.height *=
         * RenderingScene._GLZRatio(bgz); backgroundColor.x -=
         * Math.round((float) (backgroundColor.width - context.getWidth()) /
         * 2f); backgroundColor.y -= Math.round((float) (backgroundColor.height
         * - context.getHeight()) / 2f);
         * RenderingScene._GLRenderTexture2D(context, false, null,
         * backgroundColor, bgz, 0, null, null, null, null,
         * bgcolor.getColorComponents(null));
         */
        Rectangle fieldBounds = getBackFieldBounds();
        String screen = screenState.get(gameState.state);
        GLFX.reflexionFXplaneColor = bgcolor;
        if (scene.isLoading() || (gameState.state & Game._bits._getMask(Game._STATE) & Game.STATE_LOADING) != 0) {
            String loading = "loading..." + String.format("%02d%%", Math.round((float) cacheloading.getVal() / (float) cacheloading.getMax() * 100f));
            String itemloading = String.format("%03d%%", Math.round((float) cacheItemloading.getVal() / (float) cacheItemloading.getMax() * 100f));
            if (isDebugEnabled()) {
                System.out.println("FIELDGUI GL " + loading);
            }
            int font = 4;
            Dimension dim = _GL_baseCodeStringCLIPSIZE(context, loading, font);
            _GL_baseCodeStringDRAW(context,
                    scene.getWidth() - dim.width,
                    scene.getHeight() - dim.height,
                    playerDepth,
                    font, loading, GLFX.gfx.FX_DOWNREFLEXION_EXT, new Point(0, scene.getHeight()), GLGeom.getGlowingColor(Color.ORANGE, glow.glowCos));
            font = font - 2;
            dim = _GL_baseCodeStringCLIPSIZE(context, itemloading, font);
            _GL_baseCodeStringDRAW(context,
                    5,
                    5,
                    playerDepth,
                    font, itemloading, GLFX.gfx.FX_DOWNREFLEXION_EXT, new Point(0, scene.getHeight()), GLGeom.getGlowingColor(Color.ORANGE, glow.glowCos));
        } else {
            if (isDebugEnabled()) {
                System.out.println("FIELDGUI GL draw");
            }
            Color cJ1 = Color.RED, cJ2 = Color.BLUE;
            Rectangle2D camera = getCameraBounds();
            /*
             * RenderingScene._GL_lookAt(new float[]{(float)
             * camera.getCenterX(), (float) camera.getCenterY(),
             * RenderingScene._GL_getLookAt_eye()[2]}, new float[]{(float)
             * camera.getCenterX(), (float) camera.getCenterY(),
             * RenderingScene._GL_getLookAt_center()[2]},
             * RenderingScene._GL_getLookAt_norm());
             */
            if (screen.equals(SCREEN_BATTLE)) {
                GLHandler.stView.push();
                GL11.glTranslated(fieldBounds.getX() - camera.getX(), fieldBounds.getY() - camera.getY(), 0);
                int l = gameState.backgroundModels instanceof XtendedModel[] ? gameState.backgroundModels.length : 3;
                for (int i = 0; i < l; i++) {
                    GLdrawBg(context, fieldBounds, i);
                }
                GLHandler.stView.pop();
                GLdrawPlayers(context, fieldBounds, cJ1, cJ2);
                GLdrawBall(context, fieldBounds, Color.YELLOW);
                /*
                 * GLdrawName(context, Color.WHITE, screen);
                 */
            }
            for (int i = 0; i < selectedPlayer.length; i++) {
                SortedMap plrMap = selectedPlayer[i];
                if (plrMap instanceof SortedMap) {
                    XtendedModel plr = (XtendedModel) plrMap.get("model");
                    if (plr.GLisLoaded()) {
                        String plrInfo = "Player " + i + " :: " + (gameState.fighters.length > i ? gameState.fighters[i] : "");
                        if (isDebugEnabled()) {
                            Color c = i < NPLAYERS / 2f ? cJ1 : cJ2;
                            for (String line : plrInfo.split("\n")) {
                                scene.getHud_user().pushContent("player" + 1, line, (i < (float) NPLAYERS / 2f) ? TextLayout.POS.LEFT : TextLayout.POS.RIGHT, TextLayout.POS.TOP, true);
                            }
                        }
                    }
                }
            }
        }
        /*
         * Sprite._GLEndFX();
         */
    }

    private void initLayers() {
        for (int i : new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9}) {
            _bgSndStg.add(i);
        }
        gamestateLayers.put(Game.STATE_LOADING, new Thegamelayer(this, "Loading Scene") {

            @Override
            protected void doLoad() {
                super.doLoad();
                loadSpritesCache(gameState, false);
            }

            @Override
            protected void renderToGL(RenderingScene context) {
                super.renderToGL(context);
            }
        });
        gamestateLayers.put(Game.STATE_READY_TO_START, new PlayerSelection(this));
        gamestateLayers.put(Game.STATE_FINISHED | Game.STATE_PAUSED | Game.STATE_RUNNING, new Fight(this));
        gamestateLayers.put(Game.STATE_MENU, new GameMenu(this));
        gamestateLayers.put(Game.STATE_GAMEOVER, new Thegamelayer(this, "Game Outro") {

            boolean played = false;

            @Override
            protected void doLoad() {
                super.doLoad();
            }
        });
        gamestateLayers.put(Game.STATE_UNKNOWN, new UnknownGameState(this));
    }

    class ConsoleInterface {

        PrintStream console, consoleErr;

        public ConsoleInterface(PrintStream console, PrintStream consoleErr) {
            this.console = console;
            this.consoleErr = consoleErr;
            /*
             * CoalescedThreadsMonitor._uncaughtStackTracePS = consoleErr;
             */
        }

        public void exceptionCaught(Exception e) {
            String message = e.toString();
            console.println("An Exception has been CAUGHT : " + message);
            e.printStackTrace(consoleErr);
        }

        public void println(String message) {
            console.println(message);
        }

        public void print(String message) {
            console.print(message);
        }
    }
    public final static Glow glow = new Glow();
    /**
     *
     */
    public static Color[] defaultSelectColors = new Color[]{Color.YELLOW, Color.RED, Color.GREEN, Color.BLUE};

    /**
     * define different colors for UI player selection
     */
    public static void _setUISelectingPlayerColor(Color c, int playerNum) {
        if (playerNum > defaultSelectColors.length) {
            Color[] newColors = new Color[playerNum + 1];
            for (int i = 0; i < defaultSelectColors.length; i++) {
                newColors[i] = defaultSelectColors[i];
            }
            for (int i = defaultSelectColors.length; i < newColors.length; i++) {
                newColors[i] = c;
            }
            defaultSelectColors = newColors;
        }
        defaultSelectColors[playerNum] = c;
    }
    /**
     *
     */
    private Monitor gameStateMonitor;
    /**
     * @deprecated
     */
    private boolean updating = false;
    /**
     *
     */
    private Monitor refreshMonitor;
    /**
     * @deprecated
     */
    private boolean refreshing = false;

    public Monitor[] getGroupMonitor() {
        return new Monitor[]{gameStateMonitor, refreshMonitor};
    }

    /**
     *
     */
    public void setGroupMonitor(Monitor... tg) {
        gameStateMonitor = tg[0];
        refreshMonitor = tg[1];
    }

    public boolean isDebugEnabled() {
        return (props.getDebug() & GameProperties._DEBUG_GUI) != 0;
    }

    public void setDebugEnabled(boolean arg0) {
        if (arg0) {
            props.setDebug(props.getDebug() | GameProperties._DEBUG_GUI);
        } else {
            props.setDebug(props.getDebug() & ~GameProperties._DEBUG_GUI);
        }
    }
    /*
     * static boolean _multiThreading = false;
     */

    public boolean isMultiThreadingEnabled() {
        return JXAenvUtils._multiThreading;
    }

    public void setMultiThreadingEnabled(boolean arg0) {
        JXAenvUtils._multiThreading = arg0;
    }

    public Game requestGameState() {
        return gameState;
    }

    public void modeChanged(int arg0) {
    }
}
