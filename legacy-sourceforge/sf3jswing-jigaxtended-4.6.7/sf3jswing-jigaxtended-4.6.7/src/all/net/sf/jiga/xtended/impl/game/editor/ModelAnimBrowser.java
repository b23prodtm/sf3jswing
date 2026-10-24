/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.game.editor;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.List;
import java.util.*;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputListener;
import net.sf.jiga.xtended.impl.Animation;
import net.sf.jiga.xtended.impl.GUIConsoleOutput;
import net.sf.jiga.xtended.impl.Sprite;
import net.sf.jiga.xtended.impl.game.Custom;
import net.sf.jiga.xtended.impl.game.Model;
import net.sf.jiga.xtended.impl.game.TextLayout;
import net.sf.jiga.xtended.impl.game.gl.GLFX;
import net.sf.jiga.xtended.kernel.JXAenvUtils;
import net.sf.jiga.xtended.kernel.SpritesCacheManager;
import org.jdesktop.swingx.JXTitledPanel;

/**
 * This JComponent is used in the Animations Browser Applet (ABA) to browse the
 * content of a Model and customize its bounded properties.
 *
 * @author www.b23prodtm.info
 */
public class ModelAnimBrowser extends JComponent implements MouseInputListener, MouseWheelListener {

    /**
     * the Model browser
     */
    ModelBrowser browser;
    /**
     * the Model instance to browse
     */
    Model model;
    /**
     * the synch-cache of the Model Animation's (a.k.a. records)
     */
    final SortedMap<Integer, Animation> animations;
    /**
     * the current browsed record number
     */
    public final int record;
    /**
     * the titled panel
     */
    public final JXTitledPanel titlePanel;
    /**
     * the reverse play switch
     */
    public final JCheckBox reverseSwitch;
    /**
     * the record frame-position slider
     */
    public final JSlider sw_position;
    /**
     * the record frame-position panel
     */
    public final JPanel timePan = new JPanel(true);
    /**
     * the record sfx spinner
     */
    public final JSpinner sw_sfx;
    /**
     * the record sfx panel
     */
    public final JPanel sfxPan = new JPanel(true);
    /**
     * the record zoom slider
     */
    public final JSlider sw_zoom;
    /**
     * the record state label
     */
    public final JLabel sw_state;
    /**
     * the resolution bounded property name
     */
    public final static String ATT_RESOLUTION = "resolution";
    /**
     * the background color bounded property name
     */
    public final static String ATT_BGCOLOR = "bgcolor";
    /**
     * the mirror bounded property name
     */
    public final static String ATT_MIRROR = "mirror";
    /**
     * the shadow bounded property name
     */
    public final static String ATT_SHADOW = "shadow";
    /**
     * the gradient bounded property name
     */
    public final static String ATT_GRADIENT = "gradient";
    /**
     * the collision ("defensive context") map bounded property name
     */
    public final static String COLLISIONSDEF = "collisionsDef_map";
    /**
     * the collision ("offensive context") map bounded property name
     */
    public final static String COLLISIONSATK = "collisionsAtk_map";
    /**
     * the location ("translation") map bounded property name
     */
    public final static String LOCATIONS = "locations_map";
    /**
     * the current record animation bounded property name
     */
    public final static String CURRENTRECORD = "current record";
    /**
     * the current record key bounded property name
     */
    public final static String CURRENTRECORDKEY = "current record key";
    /**
     * the animations identifiers bounded property name
     */
    public final static String ATT_ANIMSID = "animsID";
    /**
     * the sound effects identifiers bounded property name
     */
    public final static String ATT_SFXID = "sfxID";
    /**
     * the reverse-played animatins identifiers bounded property name
     */
    public final static String ATT_REVERSEDANIMS = "reversedAnims";

    /**
     * creates a new instance
     *
     * @param browser the parent browser instance
     * @param record the record key of this instance
     * @param titlePanel the panal that holds this instance
     * @param sw_state the label that states this instance
     */
    public ModelAnimBrowser(
            ModelBrowser browser,
            int record,
            JXTitledPanel titlePanel,
            JLabel sw_state) {
        super();
        this.browser = browser;
        this.model = browser.model;
        setMinimumSize(new Dimension(model.getWidth(browser.resolution.get(ATT_RESOLUTION)), model.getHeight(browser.resolution.get(ATT_RESOLUTION))));
        menu = browser.menu;
        Custom model = (Custom) this.model;
        animations = this.model.accessSynchCache();
        this.record = record;
        this.titlePanel = titlePanel;
        this.sw_state = sw_state;
        addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                /*
                 * to avoid crappy affect if the component is stretched out*
                 * animations.get(ModelAnimBrowser.this.record).clearResource();---
                 * SPRITE clearesource
                 */
            }
        });
        hash = System.nanoTime();

        /**
         * reverse play switch
         */
        reverseSwitch = new JCheckBox();

        for (String id : browser.reversedAnims.get(ATT_REVERSEDANIMS)) {
            if (id instanceof String) {
                reverseSwitch.setSelected(browser.animsID.get(ATT_ANIMSID)[record].contains(id));
            }
        }
        reverseSwitch.setAction(new AbstractAction("set up reverse play (set an anim id first !)") {

            public void actionPerformed(ActionEvent e) {
                List<String> s = ModelAnimBrowser.this.browser.reversedAnims.get(ModelAnimBrowser.ATT_REVERSEDANIMS);
                for (String n : ModelAnimBrowser.this.browser.animsID.get(ModelAnimBrowser.ATT_ANIMSID)[ModelAnimBrowser.this.record]) {
                    if (!s.contains(n)) {
                        s.add(n);
                    }
                }
            }
        });
        /**
         * add sfx panel
         */
        int sfxKey = -1;
        for (String n : browser.animsID.get(ModelAnimBrowser.ATT_ANIMSID)[record]) {
            if (model.sfxMap.containsKey(n)) {
                sfxKey = Math.max(0, Math.min(model.sfx.length - 1, model.sfxMap.get(n)));
            }
        }

        sw_sfx = new JSpinner(new SpinnerNumberModel(Math.max(sfxKey, 0), 0, Math.max(0, model.sfx.length - 1), 1));
        sw_sfx.setEnabled(sfxKey > 0);

        sfxPan.setLayout(new BorderLayout());
        sfxPan.add(new JLabel("Select animation sfx:"), BorderLayout.NORTH);
        sfxPan.add(sw_sfx, BorderLayout.CENTER);
        JCheckBox sfxEnable = new JCheckBox(new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                if (ModelAnimBrowser.this.sw_sfx.isEnabled()) {
                    int selected = ((SpinnerNumberModel) ModelAnimBrowser.this.sw_sfx.getModel()).getNumber().intValue();
                    ModelAnimBrowser.this.browser.sfxID.get(ATT_SFXID)[selected].removeAll(ModelAnimBrowser.this.browser.animsID.get(ATT_ANIMSID)[ModelAnimBrowser.this.record]);
                    ModelAnimBrowser.this.sw_sfx.setEnabled(!ModelAnimBrowser.this.sw_sfx.isEnabled());
                } else {
                    ModelAnimBrowser.this.sw_sfx.setEnabled(!ModelAnimBrowser.this.sw_sfx.isEnabled());
                    int selected = ((SpinnerNumberModel) ModelAnimBrowser.this.sw_sfx.getModel()).getNumber().intValue();
                    List<String> links = ModelAnimBrowser.this.browser.sfxID.get(ATT_SFXID)[selected];
                    for (String n : ModelAnimBrowser.this.browser.animsID.get(ATT_ANIMSID)[ModelAnimBrowser.this.record]) {
                        if (!links.contains(n)) {
                            links.add(n);
                        }
                    }
                }
            }
        });
        sfxEnable.setSelected(sw_sfx.isEnabled());
        sfxPan.add(sfxEnable, BorderLayout.EAST);
        sw_sfx.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                if (JXAenvUtils._debug) {
                    System.out.println("sfx changed");
                }
                int selected = ((SpinnerNumberModel) ModelAnimBrowser.this.sw_sfx.getModel()).getNumber().intValue();
                List<String> sfxLinks = ModelAnimBrowser.this.browser.sfxID.get(ATT_SFXID)[selected];
                for (String n : ModelAnimBrowser.this.browser.animsID.get(ATT_ANIMSID)[ModelAnimBrowser.this.record]) {
                    if (!sfxLinks.contains(n)) {
                        sfxLinks.add(n);
                    }
                }
                repaint();
            }
        });
        sfxPan.add(new JButton(browser.action_playSfx), BorderLayout.SOUTH);

        /**
         * mini time
         */
        sw_position = new JSlider(0, 100, 0);
        timePan.setLayout(new BorderLayout());
        timePan.add(new JLabel("timeframe:"), BorderLayout.NORTH);
        timePan.add(sw_position, BorderLayout.CENTER);
        sw_position.setPaintTrack(true);
        sw_position.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                Animation animRecord = animations.get(ModelAnimBrowser.this.record);
                if (animRecord.getState() != Animation.PLAYING) {
                    repaint();
                }

                if (JXAenvUtils._debug) {
                    System.out.println("position changed");
                }
            }
        });
        /**
         * mini zoom
         */
        sw_zoom = new JSlider(10, 200, 100);
        Dictionary labels_zm = new Hashtable();
        for (int i = 50; i <= 200; i = i + 50) {
            labels_zm.put(new Integer(i), new JLabel(i / 100.0 + "x"));
        }

        sw_zoom.setLabelTable(labels_zm);
        sw_zoom.setPaintLabels(true);
        sw_zoom.setPaintTicks(true);
        sw_zoom.setSnapToTicks(true);
        sw_zoom.setMajorTickSpacing(50);
        sw_zoom.setMinorTickSpacing(25);
        sw_zoom.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                double zoom = sw_zoom.getValue() / 100.;
                ModelAnimBrowser.this.browser.zoom.put(ModelAnimBrowser.this.record, zoom);
                repaint();
                if (JXAenvUtils._debug) {
                    System.out.println("zoom changed");
                }
            }
        });
    }

    /**
     * Returns the file name associated to the specified sfx index.
     *
     * @param n the sfx index to gather the file
     * @return the file name if it has been mapped for the specified index, or
     * null
     * @see Model#sfx
     */
    public final String getSfx(int n) {
        File f = null;
        Custom model = (Custom) this.model;
        if (n <= (model.sfx.length - 1)) {
            f = new File(model.sfx[n]);
            return f.getPath();
        } else {
            return null;
        }

    }
    /**
     * the unique hash-code
     */
    long hash;

    /**
     * returns the corresponding mirror identifier for the specified String
     * value.
     *
     * @param mirror the mirror value to find an identifier
     * @return the corresponding mirror identifier for the specified String
     * value
     * @see Animation#NONE
     * @see Animation#HORIZONTAL
     * @see Animation#VERTICAL
     * @see #animationMirror(int)
     */
    public final static int animationMirror(String mirror) {
        int[] mirrors = new int[]{Sprite.HORIZONTAL, Sprite.VERTICAL, Sprite.NONE};
        for (int m : mirrors) {
            if (animationMirror(m).equals(mirror)) {
                return m;
            }

        }
        return Sprite.NONE;
    }

    /**
     * returns the corrsponding name for the specified mirror identifier
     *
     * @param mirror the identifier to find a name for
     * @return the name corresponding the specified identifier
     * @see Animation#HORIZONTAL
     * @see Animation#VERTICAL
     * @see Animation#NONE
     * @see #animationMirror(String)
     */
    public final static String animationMirror(int mirror) {
        switch (mirror) {
            case Sprite.HORIZONTAL:
                return "horizontal";
            case Sprite.VERTICAL:
                return "vertical";
            default:
                return "none";
        }

    }

    /**
     * returns the corresponding name for the specified identifier
     *
     * @param status the state identifier to find a name for
     * @return the name corresponding to the specified identifier
     * @see Animation#PAUSED
     * @see Animation#PLAYING
     * @see Animation#STOPPED
     */
    public final static String animationState(int status) {
        if (status == Animation.PAUSED) {
            return "paused";
        }
        if (status == Animation.PLAYING) {
            return "playing";
        }
        if (status == Animation.STOPPED) {
            return "stopped";
        }
        return "no state";
    }

    /**
     * returns the unique hash-code
     *
     * @return the unique hash-code
     */
    public int hashCode() {
        return (int) hash;
    }

    /**
     * returns true or false, whether this instance is equal or not to the
     * specified Object, resp. ®return true or false, whether this instance is
     * equal or not to the specified Object, resp.
     */
    public boolean equals(Object o) {
        return o != null ? o.hashCode() == hashCode() : false;
    }

    /**
     * always returns true
     *
     * @return always true
     */
    public boolean isOpaque() {
        return true;
    }

    /**
     * paints this instance
     *
     * @param g the Graphics instance to paint onto
     * @see #paintComponent(Graphics)
     */
    public void paint(Graphics g) {
        try {
            SpritesCacheManager<Integer, Animation> c = model.accessCache();
            c.memorySensitiveCallback("paintComponent", this, new Object[]{g}, new Class[]{Graphics.class});
        } catch (Throwable ex) {
            if (JXAenvUtils._debug) {
                ex.printStackTrace();
            }
        }
    }
    Point canvasOriginTranslate = new Point(0, 0);

    public void setCanvasOriginTranslate(int x, int y) {
        this.canvasOriginTranslate.setLocation(x, y);
    }

    public Point getCanvasOriginTranslate() {
        return canvasOriginTranslate;
    }

    /**
     * translation from top-left corner to the default location of sprites
     * (about the center-left area)
     *
     * @return translation values x,y added on painting Sprites of the browsed
     * Animation
     */
    private Point getCanvasOrigin() {
        Point translate = new Point(
                (int) Math.round((double) (getWidth() - model.getWidth(browser.resolution.get(ATT_RESOLUTION))) / 2.),
                (int) Math.round((double) (getHeight() - model.getHeight(browser.resolution.get(ATT_RESOLUTION))) / 2.));
        translate.x *= browser.zoom.get(record);
        translate.y *= browser.zoom.get(record);
        translate.x += canvasOriginTranslate.x;
        translate.y += canvasOriginTranslate.y;
        return translate;
    }

    /**
     * paints the contents of this ModelAnimBrowser instance
     *
     * @param graphics the Graphics instanceof to paint onto
     */
    @Override
    public void paintComponent(Graphics graphics) {
        Graphics2D g = Sprite.wrapRendering(graphics);
        Shape clip = g.getClip();
        g.clip(new Rectangle(getSize()));
        Point translate = getCanvasOrigin();
        g.translate(translate.x, translate.y);
        if (JXAenvUtils._debug) {
            System.err.println("MODELPANEL PAINT");
        }
        Animation animRecord = animations.get(record);
        animRecord.setSize(model.getDisplaySize(browser.resolution.get(ATT_RESOLUTION)));
        animRecord.setDebugEnabled(model.isDebugEnabled());
        String names = "";
        for (String n : browser.animsID.get(ATT_ANIMSID)[record]) {
            names += n + " | ";
        }
        titlePanel.setTitle(names + " : " + animRecord.getStartingFrame() + " - " + (animRecord.getStartingFrame() + animRecord.length - 1) + " (" + animRecord.realTimeLength() + "ms)");
        Animation current = animations.get(browser.currentRecordKey.get(CURRENTRECORDKEY));
        if (current instanceof Animation) {
            sw_state.setText(animationState(current.getState()) + " [" + current.getPosition() + "]");
        }

        animRecord.setReverseEnabled(reverseSwitch.isSelected());
        /*
         * if (animRecord.getZoomValue() != browser.zoom.get(record)) {
         * animRecord.clearResource(); }
         */
        animRecord.setZoomEnabled(browser.zoom.get(record) != .0, browser.zoom.get(record));
        animRecord.setTransformEnabled(true);
        float position = (float) sw_position.getValue() / 100f;
        if (sw_position.getValueIsAdjusting()) {
            animRecord.position((long) Math.min(animRecord.realTimeLength(), (int) (position * (float) animRecord.realTimeLength())));
        } else {
            int val = (int) ((float) animRecord.getTimeFramePosition() / (float) animRecord.realTimeLength() * 100f);
            sw_position.setValue(val);
        }

        int selectedSfx = ((SpinnerNumberModel) sw_sfx.getModel()).getNumber().intValue();
        if (sw_sfx.isEnabled()) {
            animRecord.setSfx(getSfx(selectedSfx), false, animRecord.getPosition());
        } else {
            animRecord.setSfx("", false, animRecord.getPosition());
        }
        int animator = 0;
        g.setBackground(getBackground());
        Color c = g.getColor();
        g.setColor(g.getBackground());
        g.fillRect(-translate.x, -translate.y, getWidth(), getHeight());
        g.setColor(c);
        /*
         * if (animRecord.getMirrorOrientation() !=
         * browser.mirror.get(ATT_MIRROR)) { animRecord.clearResource(); }
         */
        animator = animRecord.getPosition();
        Point loc = browser.locations_map.get(record).get(animator);
        Point displayLoc = new Point((int) Math.round(loc.x * browser.zoom.get(record)), (int) Math.round(loc.y * browser.zoom.get(record)));
        animRecord.setLocation(displayLoc);
        int fx = GLFX.gfx.FX_NONE;
        Point fx_loc = browser.shadow.get(ATT_SHADOW) ? new Point(5, 5) : browser.gradient.get(ATT_GRADIENT) ? new Point(0, - 2 * model.getFloorDiff(browser.resolution.get(ATT_RESOLUTION))) : new Point(0, 0);
        fx = fx | (browser.shadow.get(ATT_SHADOW) ? GLFX.gfx.FX_SHADOW.bit() : GLFX.gfx.FX_NONE) | (browser.gradient.get(ATT_GRADIENT) ? GLFX.gfx.FX_DOWNREFLEXION_EXT : GLFX.gfx.FX_NONE);
        fx_loc.x *= browser.zoom.get(record);
        fx_loc.y *= browser.zoom.get(record);
        animRecord.setMirrorEnabled(true, browser.mirror.get(ATT_MIRROR));
        animRecord.setTransformEnabled(true);
        animRecord.runValidate();
        if (animRecord.draw(this, g, fx, fx_loc, null)) {
            if (JXAenvUtils._debug) {
                System.err.println("MODELPANEL paint() completed w/ loc translation : " + fx_loc);
            }
        }
        /**
         * display anim bounds
         */
        Sprite animSp = animRecord.getCurrentSprite();
        animSp.runValidate();
        Rectangle animBounds = animSp.getBounds();
        g.draw(animBounds);
        animSp.clearResource();
        /**
         * display default animBounds
         */
        Rectangle defBounds = new Rectangle(animBounds.getSize());
        g.draw(defBounds);
        g.drawLine(0, 0, defBounds.width, defBounds.height);
        g.drawLine(defBounds.width, 0, 0, defBounds.height);

        /**
         * display floor
         */
        int floor = (int) Math.round(defBounds.getHeight() - browser.zoom.get(record) * model.getFloorDiff(browser.resolution.get(ATT_RESOLUTION)));
        g.drawLine((int) Math.round((double) defBounds.width / 2.0), floor, (int) Math.round((double) defBounds.width / 2.0), floor - 30);
        g.drawLine(-translate.x, floor, getWidth(), floor);
        g.drawString("forwards -->", defBounds.width, floor);

        /**
         * display insets
         */
        Rectangle insets = new Rectangle(model.getInsetSize(browser.resolution.get(ATT_RESOLUTION)));
        insets = AffineTransform.getScaleInstance(browser.zoom.get(record), browser.zoom.get(record)).createTransformedShape(insets).getBounds();
        insets.setLocation((int) Math.round((double) (defBounds.width - insets.width) / 2.), floor - insets.height);
        g.draw(insets);

        /**
         * display grid
         */
        double padding = 10. * browser.zoom.get(record);
        Composite cps = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .2f));
        for (int row = 0; row < getHeight(); row += padding) {
            g.drawLine(-translate.x, -translate.y + row, -translate.x + getWidth(), -translate.y + row);
        }
        for (int col = 0; col < getWidth(); col += padding) {
            g.drawLine(-translate.x + col, -translate.y, -translate.x + col, -translate.y + getHeight());
        }
        g.setComposite(cps);
        c = g.getColor();
        /**
         * display collisions rectangles
         */
        g.setColor(Color.GREEN);
        Stroke s = g.getStroke();
        BasicStroke bs = new BasicStroke(2f);
        g.setStroke(bs);
        List<Rectangle> collisions = browser.collisionsDef_map.get(record).get(animator);
        if (collisions instanceof List) {
            for (Rectangle rect : collisions) {
                Rectangle displayRect = AffineTransform.getScaleInstance(animRecord.getZoomValue(), animRecord.getZoomValue()).createTransformedShape(rect).getBounds();
                g.drawRect(displayRect.x, displayRect.y, displayRect.width, displayRect.height);
            }

        }
        g.setColor(Color.RED);
        collisions = browser.collisionsAtk_map.get(record).get(animator);
        if (collisions instanceof List) {
            for (Rectangle rect : collisions) {
                Rectangle displayRect = AffineTransform.getScaleInstance(animRecord.getZoomValue(), animRecord.getZoomValue()).createTransformedShape(rect).getBounds();
                g.drawRect(displayRect.x, displayRect.y, displayRect.width, displayRect.height);
            }

        }
        g.translate(-translate.x, -translate.y);
        g.setColor(c);
        g.setStroke(s);
        cps = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        TextLayout args = new TextLayout(new Point(0, 0), TextLayout.POS.LEFT, TextLayout.POS.TOP, 10, 10, new Rectangle(getSize()));
        GUIConsoleOutput._drawNewLine(args, g, new GUIConsoleOutput.LineBuffer("-RIGHT CLICK (CMD CLICK) for help-"), 0);
        g.setComposite(cps);
        g.setClip(clip);
        if (JXAenvUtils._debug) {
            System.err.println("MODELPANEL paint RECT'S completed");
        }
    }
    /**
     * the help String's buffer
     */
    public static StringBuffer[] help = new StringBuffer[]{
        new StringBuffer("interactive- <b>DEFENSIVE (green)</b> and <b>OFFENSIVE (red)</b> contexts"),
        new StringBuffer("<b>\"" + COLLISIONSDEF + "\"</b> and <b>\"" + COLLISIONSATK + "\"</b> resp. for (InteractiveModel).getAttribute()) :"),
        new StringBuffer("<b><i>click'n'drag</i></b> for modifying an interactive rectangle (<b>+ ALT</b> for offensive context)"),
        new StringBuffer("<b><i>SHIFT + click</i></b> for removing an interactive rectangle (<b>+ ALT</b> for offensive context)"),
        new StringBuffer("<b><i>" + (JXAenvUtils.env.OS_MAC.isEnv() ? "CMD" : "CTRL") + " + click'n'drag</i></b> to move the sprite"),
        new StringBuffer("<b><i>SHIFT + " + (JXAenvUtils.env.OS_MAC.isEnv() ? "CMD" : "CTRL") + " + click</i></b> to reset to the original location"),
        new StringBuffer("<b><i>Mouse Wheel</i></b> to next/previous frame"),
        new StringBuffer("<b><i>CTRL + Mouse Wheel</i></b> to zoom in/out")
    };
    /**
     * the current drag-n-drop y-translation
     */
    private int dragLocY = -1;
    /**
     * the current drag-n-drop x-translation
     */
    private int dragLocX = -1;

    /**
     * computes the outcode (what side the mouse hit) onto the specified
     * collisions Rectangle's stack for the specified cursor position
     */
    Map<Integer, Map<Integer, Rectangle>> mouse_computeOutcode(List<Rectangle> collisions, Rectangle cursor) {
        int outcode = 0, index = 0;
        Rectangle collision = new Rectangle();
        Point2D[] cursorCorners = new Point2D[]{
            new Point.Double(cursor.getX(), cursor.getY()),
            new Point.Double(cursor.getMaxX(), cursor.getY()),
            new Point.Double(cursor.getMaxX(), cursor.getMaxY()),
            new Point.Double(cursor.getX(), cursor.getMaxY())};
        if (collisions instanceof List) {
            for (int i = 0; i < collisions.size(); i++) {
                Rectangle mapRect = collisions.get(i);
                Rectangle rect = AffineTransform.getScaleInstance(browser.zoom.get(record), browser.zoom.get(record)).createTransformedShape(mapRect).getBounds();
                boolean incodeFound = false;
                for (Point2D p : cursorCorners) {
                    int code = rect.outcode(p);
                    if (code == 0) {
                        incodeFound = true;
                    }
                    outcode |= code;
                }
                if (incodeFound) {
                    if (outcode != 0) {
                        collision = rect;
                        index = i;
                        break;
                    }
                } else {
                    outcode = 0;
                }
            }
        }
        return (outcode == 0) ? null : Collections.singletonMap(outcode, Collections.singletonMap(index, collision));
    }
    /**
     * outcodes for the collision ("defensive context")
     */
    Map<Integer, Map<Integer, Rectangle>> outcodeDef = null;
    /**
     * outcodes for the collision ("offensive context")
     */
    Map<Integer, Map<Integer, Rectangle>> outcodeAtk = null;
    /**
     * a new Rectangle for the collsions map
     */
    Map<Integer, Rectangle> newCollision = null;
    /**
     * the collision ("defensive context") Rectangle's list
     */
    List<Rectangle> listDef = null;
    /**
     * the collision ("offensive context") Rectangle's list
     */
    List<Rectangle> listAtk = null;
    /**
     * a new Point for the locations map
     */
    Point newLoc = null;

    /**
     * implements the mouse dragged mouseEvent
     *
     * @param e the MouseEvent originating this event
     */
    public void mouseDragged(MouseEvent e) {
        if (JXAenvUtils._debug) {
            System.err.println(e);
        }
        Animation current = animations.get(record);
        Rectangle cursor = mouse_getCursorBounds(e);
        int animator = current.getPosition();
        Point drag = new Point((int) cursor.getCenterX() - dragLocX, (int) cursor.getCenterY() - dragLocY);
        if (JXAenvUtils.env.OS_MAC.isEnv() ? e.isMetaDown() : e.isControlDown()) {
            if (newLoc instanceof Point) {
                /**
                 * move the animation frame default location from model location
                 */
                mouse_validateDrag(newLoc, drag.x, drag.y);
                dragLocX = (int) cursor.getCenterX();
                dragLocY = (int) cursor.getCenterY();
                /**
                 * compute unscaled bounds
                 */
                Point newOrigLoc = new Point((int) Math.round(newLoc.getX() * 1. / browser.zoom.get(record)), (int) Math.round(newLoc.getY() * 1. / browser.zoom.get(record)));
                Point lastLoc = browser.locations_map.get(record).get(animator);
                browser.locations_map.get(record).put(animator, newOrigLoc);
                /**
                 * update collisions maps
                 */
                List<Rectangle> collisionsLists[] = new List[]{
                    browser.collisionsDef_map.get(record).get(animator),
                    browser.collisionsAtk_map.get(record).get(animator)
                };
                for (List<Rectangle> list : collisionsLists) {
                    synchronized (list) {
                        for (Rectangle rect : list) {
                            rect.x += newOrigLoc.x - lastLoc.x;
                            rect.y += newOrigLoc.y - lastLoc.y;
                        }
                    }
                }
                browser.collisionsDef_map.get(record).put(animator, collisionsLists[0]);
                browser.collisionsAtk_map.get(record).put(animator, collisionsLists[1]);
                e.consume();
            }
        } else if (listDef instanceof List || listAtk instanceof List) {
            /**
             * create or resize collision rectangles
             */
            if (outcodeDef instanceof Map || outcodeAtk instanceof Map) {
                if (outcodeDef instanceof Map) {
                    mouse_validateDrag(outcodeDef, drag.x, drag.y);
                }
                if (outcodeAtk instanceof Map) {
                    mouse_validateDrag(outcodeAtk, drag.x, drag.y);
                }
                dragLocX = (int) cursor.getCenterX();
                dragLocY = (int) cursor.getCenterY();
                /**
                 * resize in original unscaled bounds
                 */
                if (!e.isAltDown()) {
                    Map<Integer, Rectangle> defRect = outcodeDef.values().iterator().next();
                    Rectangle newRect = AffineTransform.getScaleInstance(1. / browser.zoom.get(record), 1. / browser.zoom.get(record)).createTransformedShape(defRect.values().iterator().next()).getBounds();
                    listDef.set(defRect.keySet().iterator().next(), newRect);
                } else {
                    Map<Integer, Rectangle> atkRect = outcodeAtk.values().iterator().next();
                    Rectangle newRect = AffineTransform.getScaleInstance(1. / browser.zoom.get(record), 1. / browser.zoom.get(record)).createTransformedShape(atkRect.values().iterator().next()).getBounds();
                    listAtk.set(atkRect.keySet().iterator().next(), newRect);
                }
                e.consume();
            } else if (newCollision instanceof Map) {
                /**
                 * create in original unscaled bounds
                 */
                Rectangle rect = newCollision.values().iterator().next();
                Point locCorner = rect.getLocation();
                Point moveCorner = new Point((int) Math.round(rect.getMaxX()), (int) Math.round(rect.getMaxY()));
                int outcode = rect.outcode(cursor.getCenterX(), cursor.getCenterY());
                if (outcode != 0) {
                    if ((outcode & (Rectangle.OUT_RIGHT | Rectangle.OUT_BOTTOM)) != 0) {
                        mouse_validateDrag(moveCorner, drag.x, drag.y);
                    } else if ((outcode & (Rectangle.OUT_TOP | Rectangle.OUT_LEFT)) != 0) {
                        mouse_validateDrag(locCorner, drag.x, drag.y);
                    }
                    dragLocX = (int) cursor.getCenterX();
                    dragLocY = (int) cursor.getCenterY();
                }
                rect.setFrameFromDiagonal(locCorner, moveCorner);
                if (rect.getWidth() > 2 && rect.getHeight() > 2) {
                    Rectangle newRect = AffineTransform.getScaleInstance(1. / browser.zoom.get(record), 1. / browser.zoom.get(record)).createTransformedShape(rect).getBounds();
                    if (newCollision.keySet().iterator().next() >= 0) {
                        if (!e.isAltDown()) {
                            listDef.set(newCollision.keySet().iterator().next(), newRect);
                        } else {
                            listAtk.set(newCollision.keySet().iterator().next(), newRect);
                        }
                    } else {
                        if (!e.isAltDown()) {
                            listDef.add(newRect);
                            newCollision = Collections.singletonMap(listDef.size() - 1, newRect);
                        } else {
                            listAtk.add(newRect);
                            newCollision = Collections.singletonMap(listAtk.size() - 1, newRect);
                        }
                    }
                }
                e.consume();
            }
            browser.collisionsDef_map.get(record).put(animator, listDef);
            browser.collisionsAtk_map.get(record).put(animator, listAtk);
        }
        if (outcodeDef instanceof Map || outcodeAtk instanceof Map || newCollision instanceof Map || newLoc instanceof Point) {
            repaint();
        }
    }
    /**
     * the default Cursor aspect
     */
    Cursor defaultCursor = Cursor.getDefaultCursor();

    /**
     * updates the Cursor aspect to the current playerStatus
     *
     * @param collision the outcodes collision map
     * @param e the MouseEvent originating the event
     */
    private void mouse_updateCursor(Map<Integer, Map<Integer, Rectangle>> collision, MouseEvent e) {
        if (collision instanceof Map) {
            if (e.isShiftDown() || collision.keySet().iterator().next() != 0) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                return;
            }
        }
        setCursor(defaultCursor);
    }

    /**
     * updates the Cursor aspect against the current playerStatus
     *
     * @param loc the Point instance for the locations map
     * @param e the MouseEvent originating the event
     */
    private void mouse_updateCursor(Point loc, MouseEvent e) {
        if (JXAenvUtils.env.OS_MAC.isEnv() ? e.isMetaDown() : e.isControlDown()) {
            if (JXAenvUtils._debug) {
                System.err.println("record " + record + " frame " + animations.get(record).getPosition() + " loc translation : " + loc);
            }
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return;
        }
        setCursor(defaultCursor);
    }

    /**
     * implements the mouse moved mouseEvent
     *
     * @param e the MouseEvent originating the event
     */
    public void mouseMoved(MouseEvent e) {
        if (JXAenvUtils._debug) {
            System.err.println(e);
        }
        Animation animRecord = animations.get(record);
        if (!browser.isResourceLoaded()) {
            return;
        }
        if (e.isAltDown()) {
            mouse_updateCursor(mouse_computeOutcode(browser.collisionsAtk_map.get(record).get(animRecord.getPosition()), mouse_getCursorBounds(e)), e);
        } else if (JXAenvUtils.env.OS_MAC.isEnv() ? e.isMetaDown() : e.isControlDown()) {
            Point loc = browser.locations_map.get(record).get(animRecord.getPosition());
            mouse_updateCursor(new Point((int) Math.round(loc.getX() * browser.zoom.get(record)), (int) Math.round(loc.getY() * browser.zoom.get(record))), e);
        } else {
            mouse_updateCursor(mouse_computeOutcode(browser.collisionsDef_map.get(record).get(animRecord.getPosition()), mouse_getCursorBounds(e)), e);
        }
        if (JXAenvUtils.env.OS_MAC.isEnv() ? e.isMetaDown() : e.isControlDown()) {
            mouseDragged(e);
        }
    }

    /**
     * implements the mous clicked mouseEvent
     *
     * @param e the MouseEvent originating the event
     */
    public void mouseClicked(MouseEvent e) {
        if (JXAenvUtils._debug) {
            System.err.println(e);
        }
        Animation current = animations.get(record);
        if (!browser.isResourceLoaded()) {
            return;
        }
        Rectangle cursor = mouse_getCursorBounds(e);
        int animator = current.getPosition();
        /**
         * current values
         */
        List<Rectangle> collisionsListDef = browser.collisionsDef_map.get(record).get(animator);
        List<Rectangle> collisionsListAtk = browser.collisionsAtk_map.get(record).get(animator);
        if (e.isShiftDown()) {
            if (!e.isAltDown()) {
                if (JXAenvUtils.env.OS_MAC.isEnv() ? e.isMetaDown() : e.isControlDown()) {
                    Point p = browser.locations_map.get(record).get(animator);
                    p.setLocation(0, 0);
                    browser.locations_map.get(record).put(animator, p);
                    e.consume();
                } else {
                    outcodeDef = mouse_computeOutcode(collisionsListDef, cursor);
                    if (outcodeDef instanceof Map) {
                        int collMapRect = outcodeDef.values().iterator().next().keySet().iterator().next();
                        collisionsListDef.remove(collMapRect);
                        outcodeDef = null;
                        e.consume();
                    }
                }
            } else {
                outcodeAtk = mouse_computeOutcode(collisionsListAtk, cursor);
                if (outcodeAtk instanceof Map) {
                    int collMapRect = outcodeAtk.values().iterator().next().keySet().iterator().next();
                    collisionsListAtk.remove(collMapRect);
                    outcodeAtk = null;
                    e.consume();
                }
            }
        }
        /**
         * store results (in cache)
         */
        browser.collisionsDef_map.get(record).put(animator, collisionsListDef);
        browser.collisionsAtk_map.get(record).put(animator, collisionsListAtk);
        repaint();
    }
    JPopupMenu menu;
    /*
     * int popupTrigger = 0;
     */

    /**
     * implements the mouse pressed mouseEvent
     *
     * @param e the MouseEvent originating the event
     */
    public void mousePressed(MouseEvent e) {
        if (JXAenvUtils._debug) {
            System.err.println(e);
        }
        Animation current = animations.get(record);
        if (!browser.isResourceLoaded()) {
            return;
        }
        Rectangle cursor = mouse_getCursorBounds(e);
        int animator = current.getPosition();
        listDef = listDef instanceof List ? listDef : browser.collisionsDef_map.get(record).get(animator);
        listAtk = listAtk instanceof List ? listAtk : browser.collisionsAtk_map.get(record).get(animator);

        if (e.isPopupTrigger()) {
            menu.pack();
            menu.show((Component) e.getSource(), e.getX(), e.getY());
            /*
             * popupTrigger = 0; }/* else if (popupTrigger == 0 &&
             * menu.isVisible()) { menu.setVisible(false);
             */
        } else if (!e.isShiftDown()) {
            if (dragLocX == -1) {
                dragLocX = (int) cursor.getCenterX();
            }
            if (dragLocY == -1) {
                dragLocY = (int) cursor.getCenterY();
            }
            if (!e.isAltDown()) {
                if (JXAenvUtils.env.OS_MAC.isEnv() ? e.isMetaDown() : e.isControlDown()) {
                    /**
                     * move location , first read current
                     */
                    Point loc = browser.locations_map.get(record).get(animator);
                    if (newLoc == null) {
                        newLoc = new Point();
                    }
                    newLoc.x = (int) Math.round(loc.getX() * browser.zoom.get(record));
                    newLoc.y = (int) Math.round(loc.getY() * browser.zoom.get(record));
                    e.consume();
                } else {
                    /**
                     * create or resize collision def
                     */
                    outcodeDef = mouse_computeOutcode(listDef, cursor);
                    e.consume();
                }
            } else {
                /**
                 * create or resize collision atk
                 */
                outcodeAtk = mouse_computeOutcode(listAtk, cursor);
                e.consume();
            }
            if (outcodeDef == null && outcodeAtk == null) {
                newCollision = Collections.singletonMap(-1, new Rectangle((int) cursor.getCenterX(), (int) cursor.getCenterY(), 1, 1));
                e.consume();
            }
        }
    }

    /**
     * validates the drag against the current playerStatus
     *
     * @param loc the Point for the locations map
     * @param dragX the dragging x value
     * @param dragY the dragging y value
     */
    void mouse_validateDrag(Point loc, int dragX, int dragY) {
        if (loc instanceof Point) {
            loc.setLocation(loc.x + dragX, loc.y + dragY);
        }
    }

    /**
     * validates the drag against the current playerStatus
     *
     * @param map the Rectangle's collisions map
     * @param dragX the drag x distance
     * @param dragY the drag y distance
     */
    void mouse_validateDrag(Map<Integer, Map<Integer, Rectangle>> map, int dragX, int dragY) {
        if (map instanceof Map) {
            if (map.isEmpty()) {
                return;
            }
            Rectangle rect = map.values().iterator().next().values().iterator().next();
            int outcode = map.keySet().iterator().next();
            Point2D moveCorner = new Point.Double(rect.getMaxX(), rect.getMaxY()), fixedCorner = rect.getLocation();
            if (outcode != 0) {
                if ((outcode & (Rectangle.OUT_TOP | Rectangle.OUT_LEFT)) != 0) {
                    fixedCorner = moveCorner;
                    moveCorner = rect.getLocation();
                }
                rect.setFrameFromDiagonal(fixedCorner.getX(), fixedCorner.getY(), moveCorner.getX() + dragX, moveCorner.getY() + dragY);
            }
        }
    }

    /**
     * implements the mouse released mouseEvent
     *
     * @param e the MouseEvent originating the event
     */
    public void mouseReleased(MouseEvent e) {
        if (JXAenvUtils._debug) {
            System.err.println(e);
        }
        listDef = null;
        listAtk = null;
        outcodeDef = null;
        outcodeAtk = null;
        newCollision = null;
        newLoc = null;
        dragLocX = dragLocY = -1;
        if (e.isPopupTrigger()) {
            menu.pack();
            menu.show((Component) e.getSource(), e.getX(), e.getY());
            /*
             * popupTrigger = 1;
             */
        }/*
         * else if (popupTrigger == 1 && menu.isVisible()) {
         * menu.setVisible(false); }
         */

    }
    /**
     * the default border decoration
     */
    final Border defaultBorder = BorderFactory.createEmptyBorder();

    /**
     * returns the current Cursor animBounds for the specified MouseEvent
     *
     * @param e the MouseEvent originating the event
     * @return the current Cursor animBounds for the specified MouseEvent
     */
    private Rectangle mouse_getCursorBounds(
            MouseEvent e) {
        Point translate = getCanvasOrigin();
        return new Rectangle(-translate.x + e.getX() - 2, -translate.y + e.getY() - 2, 5, 5);
    }

    /**
     * implements the mouse entered mouseEvent
     *
     * @param e the MouseEvent originating the event
     */
    public void mouseEntered(MouseEvent e) {
        if (JXAenvUtils._debug) {
            System.err.println(e);
        }
        setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, Color.BLACK, Color.GRAY));
        repaint();
        e.consume();
    }

    /**
     * implements the mouse exited mouseEvent
     *
     * @param e the MouseEvent originating the event
     */
    public void mouseExited(MouseEvent e) {
        if (JXAenvUtils._debug) {
            System.err.println(e);
        }
        setBorder(defaultBorder);
        setCursor(defaultCursor);
        repaint();
        e.consume();
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        Animation a = animations.get(record);
        a.stop();
        int w = e.getWheelRotation();
        if (browser.currentRecordKey.get(CURRENTRECORDKEY) != record) {
            return;
        }
        if (e.isControlDown()) {
            sw_zoom.setValue(Math.max(sw_zoom.getMinimum(), Math.min(sw_zoom.getMaximum(), sw_zoom.getValue() + w)));
        } else {
            for (int i = 0; i < Math.abs(w); i++) {
                if (w > 0) {
                    a.next();
                } else {
                    a.previous();
                }
            }
        }
        e.consume();
    }
}
