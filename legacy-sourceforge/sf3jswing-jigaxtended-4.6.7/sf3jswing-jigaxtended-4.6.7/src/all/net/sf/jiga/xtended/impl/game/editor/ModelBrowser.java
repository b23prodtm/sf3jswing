/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.game.editor;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.List;
import java.util.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.sf.jiga.xtended.impl.Animation;
import net.sf.jiga.xtended.impl.Sprite;
import net.sf.jiga.xtended.impl.game.*;
import net.sf.jiga.xtended.impl.system.SfMediaTracker;
import net.sf.jiga.xtended.kernel.*;
import net.sf.jiga.xtended.ui.LogArea;
import net.sf.jiga.xtended.ui.UIMessage;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTitledPanel;

/**
 * the ModelBrowser class is a JPanel that loads all the Model into smaller
 * panels, known as ModelAnimBrowser Component's. It is used to browse and
 * customize the Model contents and bounded properties in the Animations Browser
 * Applet (ABA).
 *
 * @author www.b23prodtm.info
 */
public class ModelBrowser extends JPanel implements Resource, Threaded {

    /**
     * the selected Model instance to browse
     */
    public Model model;
    /**
     * the resolution bounded property
     */
    public final Map<String, Integer> resolution;
    /**
     * the zoom bounded property
     */
    public final SortedMap<Integer, Double> zoom;
    /**
     * the background color bounded property
     */
    public final Map<String, Color> bgColor;
    /**
     * the mirror bounded property
     */
    public final Map<String, Integer> mirror;
    /**
     * the shadow bounded property
     */
    public final Map<String, Boolean> shadow;
    /**
     * the gradient bounded property
     */
    public final Map<String, Boolean> gradient;
    /**
     * the current record bounded property
     */
    public final Map<String, ModelAnimBrowser> currentRecord;
    /**
     * the current record key bounded property
     */
    public final Map<String, Integer> currentRecordKey;
    /**
     * the collisions ("dedfensive context") map bounded property
     */
    public SortedMap<Integer, SpritesCacheManager<Integer, List<Rectangle>>> collisionsDef_map;
    /**
     * the collisions ("offensive context") map bounded property
     */
    public SortedMap<Integer, SpritesCacheManager<Integer, List<Rectangle>>> collisionsAtk_map;
    /**
     * the locations map bounded property
     */
    public SortedMap<Integer, SpritesCacheManager<Integer, Point>> locations_map;
    /**
     * the animations identifiers map bounded property
     */
    public final Map<String, List<String>[]> animsID;
    /**
     * the sfx identifiers map bounded property
     */
    public final Map<String, List<String>[]> sfxID;
    /**
     * the reverse-played animations identifiers map bounded property
     */
    public final Map<String, List<String>> reversedAnims;
    /**
     * the browser panel width (Component's width count)
     */
    public int MODELPANELWIDTH;
    /**
     * the stating label
     */
    private JLabel sw_state;
    /**
     * the scrollpane
     */
    private JScrollPane modelPanelSp;
    /**
     * the animations identifiers array
     */
    public static String[] ANIMSIDS;
    /**
     * the animations identifiers default value
     */
    public static String ANIMSID_DEFAULT;
    /**
     * the loggin area
     */
    public LogArea log = new LogArea();
    public JPopupMenu menu;

    /**
     * creates a new instance
     *
     * @param ANIMSIDS the animations identifiers array
     * @param ANIMSID_DEFAULT the animations identifiers default value
     * @param model the Model instance to browse
     * @param MODELPANELWIDTH the width of the Component-count of the browser
     * @param modelPanelSp the ScrollPane
     * @param sw_state the JLabel used for stating the Model state
     */
    public ModelBrowser(String[] ANIMSIDS, String ANIMSID_DEFAULT, Model model, int MODELPANELWIDTH, JScrollPane modelPanelSp, JLabel sw_state) {
        super(true);
        assert modelPanelSp instanceof JScrollPane && sw_state instanceof JLabel && model instanceof Model : "no null pointer is admitted in args";
        modelPanelSp.setViewportView(this);
        modelPanelSp.getHorizontalScrollBar().setUnitIncrement(30);
        modelPanelSp.getVerticalScrollBar().setUnitIncrement(30);
        ModelBrowser.ANIMSIDS = ANIMSIDS;
        ModelBrowser.ANIMSID_DEFAULT = ANIMSID_DEFAULT;
        resolution = Collections.synchronizedMap(new HashMap<String, Integer>());
        resolution.put(ModelAnimBrowser.ATT_RESOLUTION, SpritesChar.HIGH);
        animsID = Collections.synchronizedMap(new HashMap<String, List<String>[]>());
        animsID.put(ModelAnimBrowser.ATT_ANIMSID, new List[]{});
        sfxID = Collections.synchronizedMap(new HashMap<String, List<String>[]>());
        sfxID.put(ModelAnimBrowser.ATT_SFXID, new List[]{});
        reversedAnims = Collections.synchronizedMap(new HashMap<String, List<String>>());
        reversedAnims.put(ModelAnimBrowser.ATT_REVERSEDANIMS, new ArrayList<String>());
        this.modelPanelSp = modelPanelSp;
        this.sw_state = sw_state;
        this.MODELPANELWIDTH = MODELPANELWIDTH;
        zoom = Collections.synchronizedSortedMap(new TreeMap<Integer, Double>());
        bgColor = Collections.synchronizedMap(new HashMap<String, Color>());
        bgColor.put(ModelAnimBrowser.ATT_BGCOLOR, Color.ORANGE);
        mirror = Collections.synchronizedMap(new HashMap<String, Integer>());
        mirror.put(ModelAnimBrowser.ATT_MIRROR, Sprite.NONE);
        shadow = Collections.synchronizedMap(new HashMap<String, Boolean>());
        shadow.put(ModelAnimBrowser.ATT_SHADOW, false);
        gradient = Collections.synchronizedMap(new HashMap<String, Boolean>());
        gradient.put(ModelAnimBrowser.ATT_GRADIENT, true);
        this.model = model;
        currentRecord = Collections.synchronizedMap(new HashMap<String, ModelAnimBrowser>());
        currentRecord.put(ModelAnimBrowser.CURRENTRECORD, null);
        currentRecordKey = Collections.synchronizedMap(new HashMap<String, Integer>());
        currentRecordKey.put(ModelAnimBrowser.CURRENTRECORDKEY, -1);

        /**
         * Animations maps
         */
        /*
         * panelMaps.setExpanded(true);/*setCollapsed(false);
         */
        if (JXAenvUtils.env.OS_LINUX.isEnv()) {
            try {
                SpritesCacheManager.callback("setCollapsed", panelMaps, new Object[]{false}, new Class[]{boolean.class});
            } catch (Exception e) {
                if (JXAenvUtils._debug) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
                SpritesCacheManager.callback("setExpanded", panelMaps, new Object[]{true}, new Class[]{boolean.class});
            } catch (Exception e) {
                if (JXAenvUtils._debug) {
                    e.printStackTrace();
                }
            }
        }
        panelMaps.setTitle("Animation Maps");
        panelMaps.add(action_defineAnimation);
        panelMaps.add(action_selectAnimation);
        panelMaps.add(action_print);
        panelMaps.add(action_reset_maps);

        /**
         * FX tests panelFX.setExpanded(false);/*setCollapsed(true);
         */
        if (JXAenvUtils.env.OS_LINUX.isEnv()) {
            try {
                SpritesCacheManager.callback("setCollapsed", panelFX, new Object[]{true}, new Class[]{boolean.class});
            } catch (Exception e) {
                if (JXAenvUtils._debug) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
                SpritesCacheManager.callback("setExpanded", panelFX, new Object[]{false}, new Class[]{boolean.class});
            } catch (Exception e) {
                if (JXAenvUtils._debug) {
                    e.printStackTrace();
                }
            }
        }
        panelFX.setTitle("FX");
        panelFX.add(action_switchMirror);
        panelFX.add(mirror_str = new JLabel((mirror.get(ModelAnimBrowser.ATT_MIRROR) == Sprite.NONE) ? "disabled" : "enabled"));
        panelFX.add(action_switchShadow);
        panelFX.add(shadow_str = new JLabel((shadow.get(ModelAnimBrowser.ATT_SHADOW)) ? "enabled" : "disabled"));
        panelFX.add(action_switchGradient);
        panelFX.add(gradient_str = new JLabel(gradient.get(ModelAnimBrowser.ATT_GRADIENT) ? "enabled" : "disabled"));
        initData(ANIMSIDS, ANIMSID_DEFAULT);
        menu = new JPopupMenu("Animation box");
        menu.add(new AbstractAction("help...") {

            public void actionPerformed(ActionEvent e) {
                String msg = "<html>";
                for (StringBuffer helpLine : ModelAnimBrowser.help) {
                    msg += "<br>" + helpLine;
                }
                msg += "</html>";
                menu.setVisible(false);
                new UIMessage(false, new JLabel(msg), null);
            }
        });
        menu.add(new AbstractAction("reset collision Def (green)") {

            public void actionPerformed(ActionEvent e) {
                menu.setVisible(false);
                collisionsDef_map.remove(currentRecordKey.get(ModelAnimBrowser.CURRENTRECORDKEY));
                initCollisionsMap(currentRecordKey.get(ModelAnimBrowser.CURRENTRECORDKEY), ModelBrowser.this.model, collisionsDef_map, true);
            }
        });
        menu.add(new AbstractAction("reset collision Atk (red)") {

            public void actionPerformed(ActionEvent e) {
                menu.setVisible(false);
                collisionsAtk_map.remove(currentRecordKey.get(ModelAnimBrowser.CURRENTRECORDKEY));
                initCollisionsMap(currentRecordKey.get(ModelAnimBrowser.CURRENTRECORDKEY), ModelBrowser.this.model, collisionsAtk_map, false);
            }
        });
        menu.add(new AbstractAction("reset location") {

            public void actionPerformed(ActionEvent e) {
                menu.setVisible(false);
                locations_map.remove(currentRecordKey.get(ModelAnimBrowser.CURRENTRECORDKEY));
                initLocationsMap(currentRecordKey.get(ModelAnimBrowser.CURRENTRECORDKEY), ModelBrowser.this.model, locations_map);
            }
        });
    }
    /**
     * the N/A
     */
    Action refresh = new AbstractAction("N/A") {

        public void actionPerformed(ActionEvent e) {
            if (JXAenvUtils._debug) {
                System.out.println("MODELBROWSER WARNING : N/A for refresh !!");
            }
        }
    };
    /**
     * the current MediaTracker
     */
    MediaTracker mt = new SfMediaTracker(this);

    /**
     * Datas are initialized such as animations ids and screen buffer, as well
     * as refresh Timer. used by loadResource()
     *
     * @param animsIDs the animations identifiers array
     * @param animsID_default the animations identifiers default value
     */
    private void initData(String[] animsIDs, String animsID_default) {
        assert model != null : "MODELBROWSER : the Model instance is null, so no action can be taken";
        currentRecord.put(ModelAnimBrowser.CURRENTRECORD, null);
        currentRecordKey.put(ModelAnimBrowser.CURRENTRECORDKEY, -1);
        setEnabled(false);
        model.animsID = animsIDs;
        model.animsID_default = animsID_default;
        model.setMt(mt, this);
        /**
         * !!*
         */
        model.setMultiThreadingEnabled(isMultiThreadingEnabled());
        /**
         * ***
         */
        setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), model.id + " (" + Model.RES_NAMES_map.get(resolution.get(ModelAnimBrowser.ATT_RESOLUTION)) + ")", TitledBorder.RIGHT, TitledBorder.TOP));
        animsID.put(ModelAnimBrowser.ATT_ANIMSID, new List[model.frames.length]);
        sfxID.put(ModelAnimBrowser.ATT_SFXID, new List[model.sfx.length]);
        reversedAnims.put(ModelAnimBrowser.ATT_REVERSEDANIMS, new ArrayList<String>(model.animsID.length));
        /**
         * Model maps are copied into ModelAnimBrowser editing maps
         */
        Stack<Map<String, Integer>> stack = new Stack<Map<String, Integer>>();
        stack.push(model.animsMap);
        stack.push(model.sfxMap);
        /**
         * ModelAnimBrowser maps
         */
        Stack<List<String>[]> stack2 = new Stack<List<String>[]>();
        stack2.push(sfxID.get(ModelAnimBrowser.ATT_SFXID));
        stack2.push(animsID.get(ModelAnimBrowser.ATT_ANIMSID));
        for (Map<String, Integer> map : stack) {
            synchronized (map) {
                List<String>[] array = stack2.pop();
                /**
                 * init arrays
                 */
                for (int i = 0; i < array.length; i++) {
                    if (array[i] == null) {
                        array[i] = new ArrayList<String>();
                    }
                }
                for (Iterator<String> i = map.keySet().iterator(); i.hasNext();) {
                    String key = i.next();
                    Integer val = map.get(key);
                    if (val != null) {
                        /**
                         * maybe multiple animation ID (key) links for one
                         * animation index (val)
                         */
                        if (val < array.length) {
                            /**
                             * !!
                             */
                            array[val].add(key);
                        }
                    }
                }
            }
        }
        for (String id : model.reversedAnims) {
            if (!reversedAnims.get(ModelAnimBrowser.ATT_REVERSEDANIMS).contains(id)) {
                reversedAnims.get(ModelAnimBrowser.ATT_REVERSEDANIMS).add(id);
            }
        }
        /**
         * collisionts def
         */
        if (model.getAttribute(ModelAnimBrowser.COLLISIONSDEF) == null) {
            model.setAttribute(ModelAnimBrowser.COLLISIONSDEF, (Serializable) Collections.synchronizedSortedMap(new TreeMap<Integer, SpritesCacheManager<Integer, List<Rectangle>>>()));
        }
        collisionsDef_map = (SortedMap<Integer, SpritesCacheManager<Integer, List<Rectangle>>>) model.getAttribute(ModelAnimBrowser.COLLISIONSDEF);
        initCollisionsMap(model, collisionsDef_map, true);
        /**
         * locations atk
         */
        if (model.getAttribute(ModelAnimBrowser.COLLISIONSATK) == null) {
            model.setAttribute(ModelAnimBrowser.COLLISIONSATK, (Serializable) Collections.synchronizedSortedMap(new TreeMap<Integer, SpritesCacheManager<Integer, List<Rectangle>>>()));
        }
        collisionsAtk_map = (SortedMap<Integer, SpritesCacheManager<Integer, List<Rectangle>>>) model.getAttribute(ModelAnimBrowser.COLLISIONSATK);
        initCollisionsMap(model, collisionsAtk_map, false);
        /**
         * locations
         */
        if (model.getAttribute(ModelAnimBrowser.LOCATIONS) == null) {
            model.setAttribute(ModelAnimBrowser.LOCATIONS, (Serializable) Collections.synchronizedSortedMap(new TreeMap<Integer, SpritesCacheManager<Integer, Point>>()));
        }
        locations_map = (SortedMap<Integer, SpritesCacheManager<Integer, Point>>) model.getAttribute(ModelAnimBrowser.LOCATIONS);
        initLocationsMap(model, locations_map);
        model.setResolution(resolution.get(ModelAnimBrowser.ATT_RESOLUTION));
        setEnabled(true);
    }

    /**
     * defines the Action to call on refresh
     *
     * @param a the Action to call on refresh
     */
    public void setOnRefreshAction(Action a) {
        refresh = a;
    }

    /**
     * switches the current browsed model with the specified one. The ScrollPane
     * contents are cleared and the current Model instance is also cleared off
     * the heap memory. existing values are updated to the current Model
     * resolution bounded property.
     *
     * @param newModel the new Model instance to browse
     */
    public void switchModel(Model newModel) {
        clearResource();
        model = newModel;
        model.setJava2DModeEnabled(true);
        initData(ANIMSIDS, ANIMSID_DEFAULT);
    }

    /**
     * inits the specified collisions map with Rectangle's bounding the insets
     * of the Model instance if specified
     *
     * @param model the Model instance that is browsing
     * @param map the collisions Rectangle's map to init
     * @param addInsets if true, adds Rectangle's bounding the insets for each
     * Sprite of the specified Model
     */
    private void initCollisionsMap(Model model, SortedMap<Integer, SpritesCacheManager<Integer, List<Rectangle>>> map, boolean addInsets) {
        int[][] frames = model.frames;
        long progress = UIMessage.displayProgress(0, frames.length, this);
        for (int it0 = 0; it0 < frames.length; it0++) {
            initCollisionsMap(it0, model, map, addInsets);
            UIMessage.updateProgress(progress, it0, frames.length);
        }
        UIMessage.kill(progress);
    }

    public void initCollisionsMap(int record, Model model, SortedMap<Integer, SpritesCacheManager<Integer, List<Rectangle>>> map, boolean addInsets) {
        int[][] frames = model.frames;
        /*
         * resolution rescale
         */
        Dimension size = new Dimension(model.getWidth(resolution.get(ModelAnimBrowser.ATT_RESOLUTION)), model.getHeight(resolution.get(ModelAnimBrowser.ATT_RESOLUTION)));
        Dimension insets = model.getInsetSize(resolution.get(ModelAnimBrowser.ATT_RESOLUTION));
        double scale = SpritesChar.RES_FACTORS_map.get(resolution.get(ModelAnimBrowser.ATT_RESOLUTION)) / SpritesChar.RES_FACTORS_map.get(model.res);
        AffineTransform resTx = AffineTransform.getScaleInstance(scale, scale);
        /**
         * rescale rectangle maps
         */
        SpritesCacheManager<Integer, List<Rectangle>> collisions = null;
        int framesLength = frames[record][1] - frames[record][0] + 1;
        if (map.containsKey(record) ? map.get(record).getInitialListCapacity() != framesLength : true) {
            collisions = new SpritesCacheManager<Integer, List<Rectangle>>(Math.max(1, framesLength));
            collisions.setSwapDiskEnabled(true);
            for (int it1 = 0; it1 <= frames[record][1] - frames[record][0]; it1++) {
                int sp_record = it1;
                List<Rectangle> sp_collisions = new ArrayList<Rectangle>();
                if (addInsets) {
                    sp_collisions.add(new Rectangle((int) ((float) (size.width + insets.width) / 2f) - insets.width, size.height - model.getFloorDiff(resolution.get(ModelAnimBrowser.ATT_RESOLUTION)) - insets.height, insets.width, insets.height));
                }
                collisions.put(sp_record, sp_collisions);
            }
        } else {
            collisions = map.get(record);
            for (int it1 = 0; it1 <= frames[record][1] - frames[record][0]; it1++) {
                int sp_record = it1;
                List<Rectangle> sp_collisions = collisions.get(sp_record);
                for (Rectangle r : sp_collisions) {
                    Point2D[] src = new Point2D[]{
                        r.getLocation(),
                        new Point((int) r.getMaxX(), (int) r.getY()),
                        new Point((int) r.getMaxX(), (int) r.getMaxY()),
                        new Point((int) r.getX(), (int) r.getMaxY())
                    };
                    Point2D[] dst = new Point2D[4];
                    resTx.transform(src, 0, dst, 0, 4);
                    r.setBounds((int) dst[0].getX(), (int) dst[0].getY(), (int) Math.abs(dst[0].distance(dst[1])), (int) Math.abs(dst[0].distance(dst[3])));
                }
                collisions.put(sp_record, sp_collisions);
            }
        }
        map.put(record, collisions);
    }

    /**
     * inits the locations map for the specified Model instance. existing values
     * are updated to the current Model resolution bounded property.
     *
     * @param model the Model instance to map
     * @param map the locations map to init
     */
    private void initLocationsMap(Model model, SortedMap<Integer, SpritesCacheManager<Integer, Point>> map) {
        int[][] frames = ((Custom) model).frames;
        long progress = UIMessage.displayProgress(0, frames.length, this);
        for (int it0 = 0; it0 < frames.length; it0++) {
            initLocationsMap(it0, model, map);
            UIMessage.updateProgress(progress, it0, frames.length);
        }
        UIMessage.kill(progress);
    }

    public void initLocationsMap(int record, Model model, SortedMap<Integer, SpritesCacheManager<Integer, Point>> map) {
        int[][] frames = ((Custom) model).frames;
        double scale = SpritesChar.RES_FACTORS_map.get(resolution.get(ModelAnimBrowser.ATT_RESOLUTION)) / SpritesChar.RES_FACTORS_map.get(model.res);
        AffineTransform resTx = AffineTransform.getScaleInstance(scale, scale);
        SpritesCacheManager<Integer, Point> locations = null;
        int framesLength = frames[record][1] - frames[record][0] + 1;
        if (map.containsKey(record) ? map.get(record).getInitialListCapacity() != framesLength : true) {
            locations = new SpritesCacheManager<Integer, Point>(Math.max(1, framesLength));
            locations.setSwapDiskEnabled(true);
            for (int it1 = 0; it1 <= frames[record][1] - frames[record][0]; it1++) {
                int sp_record = it1;
                Point newLoc = new Point();
                locations.put(sp_record, newLoc);
            }
        } else {
            locations = map.get(record);
            for (int it1 = 0; it1 <= frames[record][1] - frames[record][0]; it1++) {
                int sp_record = it1;
                Point sp_loc = locations.get(sp_record);
                Point dst = new Point();
                resTx.transform(sp_loc, dst);
                locations.put(sp_record, dst);
            }
        }
        map.put(record, locations);
    }
    /**
     * the records map
     */
    private Map<Integer, ModelAnimBrowser> records = Collections.synchronizedMap(new LinkedHashMap<Integer, ModelAnimBrowser>());

    /**
     * loads this ModelBrowser with the current Model instance (can take a while
     * if the Model isn't yet cached)
     *
     * @see #isResourceLoaded()
     * @see #clearResource()
     */
    public Object loadResource() {
        Object returnVal = null;
        boolean validModel = true;
        if (model instanceof XtendedModel) {
            new UIMessage(true, new JLabel("WARNING ! XtendedModel's (.mcx) are not allowed to be used in a Java2D context, only OpenGL/LWJGL context can use them."), null, UIMessage.ERROR_TYPE);
            returnVal = validModel = false;
        }
        if (validModel && UIMessage.showConfirmDialog(this, "Do you wish to browse the current imported model " + (model.isLoaded() ? "" : "and load it (it is currently not loaded in cache and that can take a while) ") + "?", "imported model found", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            if (isResourceLoaded()) {
                clearResource();
            }
            returnVal = model.loadResource();
            if (Thread.currentThread().isInterrupted()) {
                if (JXAenvUtils._debug) {
                    System.out.println("Loading is interrupted. returning...");
                }
                returnVal = validModel = false;
                clearResource();
                return returnVal;
            }
            repaint();
            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.gridwidth = GridBagConstraints.REMAINDER;
            int width = MODELPANELWIDTH;
            int n = 0;
            final SortedMap<Integer, Animation> anim = model.accessSynchCache();
            long load = UIMessage.displayProgress(0, anim.size(), this);
            for (Integer key : anim.keySet()) {
                zoom.put(key, 1.0);
                final int record = key;
                final SortedMap<Integer, SpritesCacheManager<Integer, List<Rectangle>>> collisionsDef_map = (SortedMap<Integer, SpritesCacheManager<Integer, List<Rectangle>>>) model.getAttribute(ModelAnimBrowser.COLLISIONSDEF);
                final SortedMap<Integer, SpritesCacheManager<Integer, List<Rectangle>>> collisionsAtk_map = (SortedMap<Integer, SpritesCacheManager<Integer, List<Rectangle>>>) model.getAttribute(ModelAnimBrowser.COLLISIONSATK);
                JPanel ctrlPan = new JPanel(true);
                ctrlPan.setLayout(new BorderLayout());
                final JXTitledPanel titlePanel = new JXTitledPanel("animation box", ctrlPan);
                add(titlePanel);
                if ((n % width) == (width - 1)) {
                    ((GridBagLayout) getLayout()).setConstraints(titlePanel, c);
                }
                final ModelAnimBrowser comp = new ModelAnimBrowser(this, record, titlePanel, sw_state);
                comp.setEnabled(false);
                records.put(record, comp);
                comp.addMouseWheelListener(comp);
                comp.addMouseListener(new MouseListener() {

                    public void mouseClicked(MouseEvent e) {
                        setSelectedAnimation(record);
                        comp.mouseClicked(e);
                    }

                    public void mousePressed(MouseEvent e) {
                        comp.mousePressed(e);
                    }

                    public void mouseReleased(MouseEvent e) {
                        comp.mouseReleased(e);
                    }

                    public void mouseEntered(MouseEvent e) {
                        comp.mouseEntered(e);
                    }

                    public void mouseExited(MouseEvent e) {
                        comp.mouseExited(e);
                    }
                });
                comp.addMouseMotionListener(comp);
                final Dimension modelDim = new Dimension(model.getWidth(resolution.get(ModelAnimBrowser.ATT_RESOLUTION)), model.getHeight(resolution.get(ModelAnimBrowser.ATT_RESOLUTION)));
                int zoom = Math.round(comp.sw_zoom.getMaximum() / 100f);
                final JScrollPane sp = new JScrollPane(comp);
                sp.getVerticalScrollBar().setUnitIncrement(30);
                sp.getHorizontalScrollBar().setUnitIncrement(30);  
                /*
                 * sp.getVerticalScrollBar().getModel().addChangeListener(new
                 * ChangeListener() {
                 *
                 * public void stateChanged(ChangeEvent e) {
                 * comp.setCanvasOriginTranslate(-sp.getHorizontalScrollBar().getValue(),
                 * -sp.getVerticalScrollBar().getValue()); comp.repaint(); } });
                 * sp.getHorizontalScrollBar().getModel().addChangeListener(new
                 * ChangeListener() {
                 *
                 * public void stateChanged(ChangeEvent e) {
                 * comp.setCanvasOriginTranslate(-sp.getHorizontalScrollBar().getValue(),
                 * -sp.getVerticalScrollBar().getValue()); comp.repaint(); } });
                 */
                sp.setPreferredSize(new Dimension(Math.max(400, Math.min(800, modelDim.width)), Math.max(400, Math.min(600, modelDim.height))));
                comp.sw_zoom.addChangeListener(new ChangeListener() {

                    public void stateChanged(ChangeEvent e) {
                        final double zoom = comp.sw_zoom.getValue() / 100f;
                        comp.setPreferredSize(new Dimension((int) Math.round(sp.getWidth() * zoom), (int) Math.round(sp.getHeight() * zoom)));
                        comp.setCanvasOriginTranslate(Math.max(0, (int) Math.round((sp.getPreferredSize().width - comp.getPreferredSize().width) / 2.)), Math.max(0, (int) Math.round((sp.getPreferredSize().height - comp.getPreferredSize().height ) / 2.)));
                        comp.validate();
                        comp.repaint();
                    }
                });
                ctrlPan.add(sp, BorderLayout.NORTH);
                /**
                 * reverse switch
                 */
                ctrlPan.add(comp.reverseSwitch, BorderLayout.SOUTH);
                /**
                 * sfx spinner
                 */
                ctrlPan.add(comp.sfxPan, BorderLayout.EAST);
                ctrlPan.add(comp.timePan, BorderLayout.CENTER);
                ctrlPan.add(comp.sw_zoom, BorderLayout.WEST);
                ctrlPan.revalidate();
                sp.getHorizontalScrollBar().setValue((int) Math.round(((double) sp.getHorizontalScrollBar().getMaximum() - sp.getHorizontalScrollBar().getVisibleAmount() - sp.getHorizontalScrollBar().getMinimum()) / 2.));
                sp.getVerticalScrollBar().setValue((int) Math.round(((double) sp.getVerticalScrollBar().getMaximum() - sp.getVerticalScrollBar().getVisibleAmount() - sp.getVerticalScrollBar().getMinimum()) / 2.));
                UIMessage.updateProgress(load, ++n, anim.size());
            }
            UIMessage.kill(load);
        }
        revalidate();
        repaint();
        return returnVal;
    }

    /**
     * clears the browser off the heap memory
     *
     * @see #loadResource()
     */
    public Object clearResource() {
        removeAll();
        records.clear();
        validate();
        repaint();
        return model.clearResource();
    }

    /**
     * returns true or false, whether this ModelBrowser instance is loaded or
     * not, resp.
     *
     * @see #loadResource()
     */
    public boolean isResourceLoaded() {
        return model.isResourceLoaded();
    }

    /**
     * sets up the current selected record
     *
     * @param record the record key to select
     */
    private void setSelectedAnimation(int record) {
        SortedMap<Integer, Animation> c = model.accessSynchCache();
        Animation animation = c.get(record);
        if (animation instanceof Animation) {
            if (currentRecord.get(ModelAnimBrowser.CURRENTRECORD) instanceof JComponent) {
                currentRecord.get(ModelAnimBrowser.CURRENTRECORD).setBackground(getBackground());
                currentRecord.get(ModelAnimBrowser.CURRENTRECORD).setEnabled(false);
            }
            currentRecord.put(ModelAnimBrowser.CURRENTRECORD, records.get(record));
            records.get(record).setBackground(bgColor.get(ModelAnimBrowser.ATT_BGCOLOR));
            records.get(record).setEnabled(true);
            Animation oldSelection = c.get(currentRecordKey.get(ModelAnimBrowser.CURRENTRECORDKEY));
            currentRecordKey.put(ModelAnimBrowser.CURRENTRECORDKEY, record);
            JScrollBar h = modelPanelSp.getHorizontalScrollBar();
            JScrollBar v = modelPanelSp.getVerticalScrollBar();
            float targetX = (float) h.getMinimum() + (float) records.get(record).titlePanel.getX() / (float) getWidth() * (float) (h.getMaximum() - h.getMinimum());
            float targetY = (float) v.getMinimum() + (float) records.get(record).titlePanel.getY() / (float) getHeight() * (float) (v.getMaximum() - v.getMinimum());
            if (targetX /*
                     * + h.getVisibleAmount()
                     */ <= h.getValue() || h.getValue() <= targetX) {
                h.setValue((int) Math.min(h.getMaximum() - h.getVisibleAmount(), targetX));
            }
            if (targetY /*
                     * + v.getVisibleAmount()
                     */ <= v.getValue() || v.getValue() <= targetY) {
                v.setValue((int) Math.min(v.getMaximum() - v.getVisibleAmount(), targetY));
            }
        }

        sw_state.setText(ModelAnimBrowser.animationState(animation instanceof Animation ? animation.getState() : -1));
        repaint();
    }
    /**
     * configuration output
     */
    /**
     * the logging area frame
     */
    public static JFrame logFrame = new JFrame("Log area");
    /**
     * the logging area print setting frame
     */
    public static final JFrame printFrame = new JFrame("Print config.");
    /**
     * the logging area
     */
    public static LogArea sw_log;
    /**
     * the log area print setting fields
     */
    static JTextField sw_formatPrintID, sw_formatPrintSfxID;

    static {
        /**
         * log area
         */
        sw_log = new LogArea("--- log stack ---\n");
        sw_log.setLogStdoutEnabled(false);
        JMenuBar jmb = new JMenuBar();
        jmb.add(sw_log.getActionMenu());
        logFrame.setJMenuBar(jmb);
        logFrame.getContentPane().add(sw_log);
        logFrame.setLocationRelativeTo(null);
        logFrame.pack();
        logFrame.setSize(200, 200);
        printFrame.setLayout(new GridBagLayout());
        JXPanel panelPrint = new JXPanel(true);
        panelPrint.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;
        panelPrint.add(new JLabel("<html>Format print: <br>(%1$s=anim ID, %2$s=anim start index, %3$s=anim end index, %4$s=anim array index %n=new line)</html>"), c);
        sw_formatPrintID = new JTextField("animation %1$s -> [%4$s]=[%2$s]-[%3$s]%n");
        panelPrint.add(sw_formatPrintID, c);

        panelPrint.add(new JLabel("<html>Format print: <br>(%1$s=anim ID, %2$s=sfx index, %3$s=sound file name %n=new Line)</html>"), c);
        sw_formatPrintSfxID = new JTextField("sound (%3$s) %2$s -> %1$s%n");
        panelPrint.add(sw_formatPrintSfxID, c);
        printFrame.getContentPane().add(panelPrint);
        printFrame.setLocationRelativeTo(logFrame);
        printFrame.pack();
    }
    /**
     * opens the log area
     */
    public final static AbstractAction action_openLog = new AbstractAction("log open/close...") {

        public void actionPerformed(ActionEvent e) {
            logFrame.setVisible(!logFrame.isVisible());
        }
    };
    /**
     * prints the mapping to the log area
     */
    public final AbstractAction action_print = new AbstractAction("print maps to log") {

        public void actionPerformed(ActionEvent e) {
            printFrame.setVisible(true);
            logFrame.setVisible(true);
            logFrame.requestFocusInWindow();
            StringBuffer strBuf = new StringBuffer();
            Formatter formatter = new Formatter(strBuf);
            Custom model = (Custom) ModelBrowser.this.model;
            formatter.format("%1$s", model.toString());
            for (int i = 0; i < animsID.get(ModelAnimBrowser.ATT_ANIMSID).length; i++) {
                if (animsID.get(ModelAnimBrowser.ATT_ANIMSID)[i] != null) {
                    String names = "";
                    for (String n : animsID.get(ModelAnimBrowser.ATT_ANIMSID)[i]) {
                        names += n + " | ";
                    }
                    formatter.format(sw_formatPrintID.getText(), names, model.frames[i][0], model.frames[i][1], i);
                }

            }
            for (int i = 0; i < sfxID.get(ModelAnimBrowser.ATT_SFXID).length; i++) {
                if (sfxID.get(ModelAnimBrowser.ATT_SFXID)[i] != null) {
                    String names = "";
                    for (String n : sfxID.get(ModelAnimBrowser.ATT_SFXID)[i]) {
                        names += n + " | ";
                    }
                    formatter.format(sw_formatPrintSfxID.getText(), names, i, model.sfx[i]);
                }

            }
            sw_log.pushNewLog(strBuf.toString());
            if (!logFrame.isVisible()) {
                logFrame.setVisible(true);
            }
        }
    };
    /**
     * FX labels
     */
    public final JLabel shadow_str, gradient_str, mirror_str;
    /**
     * switches the mirror orientation
     */
    public final AbstractAction action_switchMirror = new AbstractAction("test on/off symectrical flip") {

        public void actionPerformed(ActionEvent e) {
            int mirrorVal = mirror.get(ModelAnimBrowser.ATT_MIRROR);
            mirrorVal = (mirrorVal == Sprite.NONE) ? ModelAnimBrowser.animationMirror((String) UIMessage.showSelectDialog(printFrame, "Choose orientation:", "FX test : mirror", new String[]{ModelAnimBrowser.animationMirror(Sprite.HORIZONTAL), ModelAnimBrowser.animationMirror(Sprite.VERTICAL), ModelAnimBrowser.animationMirror(Sprite.NONE)}, ModelAnimBrowser.animationMirror(mirrorVal))) : Sprite.NONE;
            mirror_str.setText(ModelAnimBrowser.animationMirror(mirrorVal));
            mirror.put(ModelAnimBrowser.ATT_MIRROR, mirrorVal);
            if (currentRecord.get(ModelAnimBrowser.CURRENTRECORD) != null) {
                currentRecord.get(ModelAnimBrowser.CURRENTRECORD).repaint();
            }
        }
    };
    /**
     * switches the gradient on/off
     */
    public final AbstractAction action_switchGradient = new AbstractAction("test on/off gradient effect") {

        public void actionPerformed(ActionEvent e) {
            gradient.put(ModelAnimBrowser.ATT_GRADIENT, gradient.get(ModelAnimBrowser.ATT_GRADIENT) ? false : true);
            if (shadow.get(ModelAnimBrowser.ATT_SHADOW) && gradient.get(ModelAnimBrowser.ATT_GRADIENT)) {
                action_switchShadow.actionPerformed(e);
            }

            gradient_str.setText((gradient.get(ModelAnimBrowser.ATT_GRADIENT)) ? "enabled" : "disabled");
            if (currentRecord.get(ModelAnimBrowser.CURRENTRECORD) != null) {
                currentRecord.get(ModelAnimBrowser.CURRENTRECORD).repaint();
            }
        }
    };
    /**
     * switches the shadow on/off
     */
    public final AbstractAction action_switchShadow = new AbstractAction("test on/off shadow effect") {

        public void actionPerformed(ActionEvent e) {
            shadow.put(ModelAnimBrowser.ATT_SHADOW, shadow.get(ModelAnimBrowser.ATT_SHADOW) ? false : true);
            if (shadow.get(ModelAnimBrowser.ATT_SHADOW) && gradient.get(ModelAnimBrowser.ATT_GRADIENT)) {
                action_switchGradient.actionPerformed(e);
            }

            shadow_str.setText((shadow.get(ModelAnimBrowser.ATT_SHADOW)) ? "enabled" : "disabled");
            if (currentRecord.get(ModelAnimBrowser.CURRENTRECORD) != null) {
                currentRecord.get(ModelAnimBrowser.CURRENTRECORD).repaint();
            }
        }
    };
    /**
     * the FX panel
     */
    public final JXTaskPane panelFX = new JXTaskPane();
    /**
     * plays the selected sfx
     */
    public final AbstractAction action_playSfx = new AbstractAction("play soundfx", UIMessage._getIcon(UIMessage.SOUNDON_TYPE, true)) {

        public void actionPerformed(ActionEvent e) {
            SortedMap<Integer, Animation> c = model.accessSynchCache();
            Animation anim = c.get(currentRecordKey.get(ModelAnimBrowser.CURRENTRECORDKEY));
            if (anim instanceof Animation) {
                anim.playSfx();
            }

        }
    };

    /**
     * returns the Color by user selection
     */
    private Color getColor(Color defColor) {
        Map<Color, String> colorNames = new HashMap<Color, String>();
        colorNames.put(Color.BLUE, "Blue");
        colorNames.put(Color.RED, "Red");
        colorNames.put(Color.ORANGE, "Orange");
        colorNames.put(Color.YELLOW, "Yellow");
        colorNames.put(Color.WHITE, "White");
        colorNames.put(Color.BLACK, "Black");
        colorNames.put(Color.GRAY, "Gray");
        colorNames.put(Color.GREEN, "Green");
        String cname = (String) UIMessage.showSelectDialog(this, "Select a color : ", "Color chooser", colorNames.values().toArray(), colorNames.get(defColor));
        if (cname != null) {
            for (Iterator<Color> i = colorNames.keySet().iterator(); i.hasNext();) {
                Color c = i.next();
                if (colorNames.get(c).equals(cname)) {
                    bgColor.put(ModelAnimBrowser.ATT_BGCOLOR, c);
                    break;
                }

            }
        }
        return bgColor.get(ModelAnimBrowser.ATT_BGCOLOR);
    }
    /**
     * switches the background color
     */
    public final AbstractAction action_setBgColor = new AbstractAction("switch the background...") {

        public void actionPerformed(ActionEvent e) {
            getColor(bgColor.get(ModelAnimBrowser.ATT_BGCOLOR));
        }
    };

    /**
     * switches the resolution
     *
     * @return the user selected resolution value
     */
    private int getResolution() {
        String selection = (String) UIMessage.showSelectDialog(this, "Select the resolution of the sprites to serialize:", "Sprites resolution", Model.RES_NAMES_map.values().toArray(), Model.RES_NAMES_map.get(resolution.get(ModelAnimBrowser.ATT_RESOLUTION)));
        for (Map.Entry<Integer, String> entry : InteractiveModel.RES_NAMES_map.entrySet()) {
            if (entry.getValue().equals(selection)) {
                int res = entry.getKey();
                return res;
            }
        }
        return resolution.containsKey(ModelAnimBrowser.ATT_RESOLUTION) ? resolution.get(ModelAnimBrowser.ATT_RESOLUTION) : -1;
    }
    /**
     * switches the resolution
     */
    public final AbstractAction action_setResolution = new AbstractAction("switch resolution...") {

        public void actionPerformed(ActionEvent e) {
            int res = getResolution();
            if (resolution.get(ModelAnimBrowser.ATT_RESOLUTION) != res) {
                resolution.put(ModelAnimBrowser.ATT_RESOLUTION, res);
                refresh.actionPerformed(e);
            }
        }
    };
    /**
     * the mappings panel
     */
    public final JXTaskPane panelMaps = new JXTaskPane();
    /**
     * defines the selected animation identifier
     */
    public final AbstractAction action_defineAnimation = new AbstractAction("define animation ID...") {

        public void actionPerformed(ActionEvent e) {
            int animKey = currentRecordKey.get(ModelAnimBrowser.CURRENTRECORDKEY);
            if (animKey < 0) {
                return;
            }
            if (animsID.get(ModelAnimBrowser.ATT_ANIMSID) != null) {
                List<String> names = new ArrayList<String>(), current = new ArrayList<String>();
                List<String> list = animsID.get(ModelAnimBrowser.ATT_ANIMSID)[animKey];
                names.add(Custom._getDefaultAnimName(animKey));
                for (List<String> n : animsID.get(ModelAnimBrowser.ATT_ANIMSID)) {
                    if (n != null) {
                        current.addAll(n);
                    }
                }
                names.addAll(list);
                for (String n : ((Custom) model).animsID) {
                    if (!current.contains(n) && !names.contains(n)) {
                        names.add(n);
                    }
                }
                /*
                 * Object name = UIMessage.showSelectDialog(ModelBrowser.this,
                 * "ID :", "Select ID", names.toArray(new String[]{}),
                 * animsID.get(ModelAnimBrowser.ATT_ANIMSID)[animKey]);
                 */
                JList IDSelections = new JList(names.toArray(new String[]{}));
                int[] indices = new int[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    for (int selectedIndex = 0; selectedIndex < names.size(); selectedIndex++) {
                        if (names.get(selectedIndex).equals(list.get(i))) {
                            indices[i] = selectedIndex;
                        }
                    }
                }
                IDSelections.setSelectedIndices(indices);
                JScrollPane sp = new JScrollPane(IDSelections);
                int o = UIMessage.showConfirmDialog(ModelBrowser.this, sp, "Select one or more ID for Animation n°" + animKey, JOptionPane.YES_NO_OPTION);
                if (o == JOptionPane.YES_OPTION) {
                    /**
                     * clear previous links (sfx and reversed)
                     */
                    reversedAnims.get(ModelAnimBrowser.ATT_REVERSEDANIMS).removeAll(animsID.get(ModelAnimBrowser.ATT_ANIMSID)[animKey]);
                    for (List<String> l : sfxID.get(ModelAnimBrowser.ATT_SFXID)) {
                        for (String n : animsID.get(ModelAnimBrowser.ATT_ANIMSID)[animKey]) {
                            if (l != null && n != null) {
                                l.remove(n);
                            }
                        }
                    }
                    animsID.get(ModelAnimBrowser.ATT_ANIMSID)[animKey].clear();
                    /**
                     * define new mapping name for animation index (animKey)
                     */
                    for (Object n : IDSelections.getSelectedValuesList()) {
                        String name = (String) n;
                        if (!animsID.get(ModelAnimBrowser.ATT_ANIMSID)[animKey].contains(name)) {
                            animsID.get(ModelAnimBrowser.ATT_ANIMSID)[animKey].add(name);
                        }
                        /**
                         * clear (again) previous links from new names (sfx and
                         * reversed)
                         */
                        reversedAnims.get(ModelAnimBrowser.ATT_REVERSEDANIMS).remove(name);
                        for (List<String> l : sfxID.get(ModelAnimBrowser.ATT_SFXID)) {
                            l.remove(name);
                        }
                        /**
                         * update reversed and sfx links
                         */
                        if (records.get(animKey).reverseSwitch.isSelected()) {
                            if (!reversedAnims.get(ModelAnimBrowser.ATT_REVERSEDANIMS).contains(name)) {
                                reversedAnims.get(ModelAnimBrowser.ATT_REVERSEDANIMS).add(name);
                            }
                        }
                        if (records.get(animKey).sw_sfx.isEnabled()) {
                            if (!sfxID.get(ModelAnimBrowser.ATT_SFXID)[((SpinnerNumberModel) records.get(animKey).sw_sfx.getModel()).getNumber().intValue()].contains(name)) {
                                sfxID.get(ModelAnimBrowser.ATT_SFXID)[((SpinnerNumberModel) records.get(animKey).sw_sfx.getModel()).getNumber().intValue()].add(name);
                            }
                        }
                        /**
                         * remove anyother mapping of the same name
                         */
                        List<String> maps[] = animsID.get(ModelAnimBrowser.ATT_ANIMSID);
                        for (int i = 0; i < maps.length; i++) {
                            if (maps[i] != null && i != animKey) {
                                while (maps[i].contains(name)) {
                                    maps[i].remove(name);
                                }
                            }
                        }
                    }
                }
            }
            repaint();
        }
    };
    /**
     * selects the animation by user selection
     */
    public final AbstractAction action_selectAnimation = new AbstractAction("select an animation...") {

        public void actionPerformed(ActionEvent e) {
            int animKey = currentRecordKey.get(ModelAnimBrowser.CURRENTRECORDKEY);
            List<String> animsIDS = new ArrayList<String>();
            for (List<String> s : animsID.get(ModelAnimBrowser.ATT_ANIMSID)) {
                /**
                 * we shall see "doublons"
                 */
                animsIDS.addAll(s);
            }
            String name = (String) UIMessage.showSelectDialog(ModelBrowser.this, "ID :", "Select ID", animsIDS.toArray(new String[]{}), null);
            if (name != null) {
                for (int i = 0; i < animsID.get(ModelAnimBrowser.ATT_ANIMSID).length; i++) {
                    if (animsID.get(ModelAnimBrowser.ATT_ANIMSID)[i].contains(name)) {
                        setSelectedAnimation(i);
                        return;
                    }
                }
            }
        }
    };
    /**
     * resets the mappings
     */
    public final AbstractAction action_reset_maps = new AbstractAction("Reset Animation IDs Maps", UIMessage._getIcon(UIMessage.REFRESH_TYPE, true)) {

        public void actionPerformed(ActionEvent e) {
            Custom model = (Custom) ModelBrowser.this.model;
            model.animsMap.clear();
            model.sfxMap.clear();
            model.reversedAnims = new String[]{};
            initData(ANIMSIDS, ANIMSID_DEFAULT);
        }
    };

    /**
     * returns true or false, whether the multi-threading mode is enabled or
     * not, resp. ®return true or false, whether the multi-threading mode is
     * enabled or not, resp.
     *
     * @see Model#isMultiThreadingEnabled()
     */
    public boolean isMultiThreadingEnabled() {
        return JXAenvUtils._multiThreading;
    }

    /**
     * dis/enables the multi-threading mode
     *
     * @param b dis/enables the multi-threading mode
     * @see Model#setMultiThreadingModeEnabled(boolean)
     */
    public void setMultiThreadingEnabled(boolean b) {
        JXAenvUtils._multiThreading = b;
    }

    public Monitor[] getGroupMonitor() {
        return null;
    }

    public void setGroupMonitor(Monitor... arg0) {
    }
}
