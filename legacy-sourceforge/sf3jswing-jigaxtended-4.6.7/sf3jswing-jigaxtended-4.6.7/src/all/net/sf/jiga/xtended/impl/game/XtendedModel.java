/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.game;

import net.sf.jiga.xtended.impl.game.gl.AnimationGLHandler;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.geom.AffineTransform;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.TreeMap;
import javax.media.jai.PerspectiveTransform;
import net.sf.jiga.xtended.kernel.BitStack;
import net.sf.jiga.xtended.impl.Sprite;
import net.sf.jiga.xtended.impl.system.LogicalSystem;
import net.sf.jiga.xtended.impl.system.input.KeyEventWrapper;

/**
 * An XtendedModel is an InteractiveModel that is especially intended to be used
 * in an LWJGL context. Sprite's are stored in an exclusive Sf3Texture format to
 * improve speed at runtime.
 *
 * @author www.b23prodtm.info
 */
public class XtendedModel extends InteractiveModel implements Externalizable {

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        int currentPty = Thread.currentThread().getPriority();
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        super.writeExternal(out);
        out.flush();
        Thread.currentThread().setPriority(currentPty);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int currentPty = Thread.currentThread().getPriority();
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        super.readExternal(in);
        endRead(in);
        Thread.currentThread().setPriority(currentPty);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void endRead(ObjectInput in) throws IOException, ClassNotFoundException {
        animPlayer = new GLAnimPlayer(this);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        endRead(in);
    }
    /**
     * serial version UID
     */
    private static final long serialVersionUID = 2323;
    /**
     * the DataFlavor DataFlavor.javaSerializedObjectMimeType
     */
    public static final String _MIME_TYPE = DataFlavor.javaSerializedObjectMimeType;
    /**
     * the InteractiveModel mime-type extensions
     */
    public static final String[] _MIME_EXT = new String[]{"mcx", "MCX"};
    /**
     * the DataFlavor's used for InteractiveModel instances
     */
    public static final DataFlavor _dataFlavor = new DataFlavor(XtendedModel.class, "sf3jswing Xtended Model");

    static {
        _storeMIMETYPES(_MIME_TYPE, _MIME_EXT);
    }

    public XtendedModel() {
        this("unknown", HIGH, 0, new Dimension(), new Dimension(), new int[][]{}, new String[]{}, new String[]{});
    }

    /**
     * creates a new instance
     *
     * @param name the name to identify the model, it is used in the animation
     * path names, if {@linkplain #isInnerResourceModeEnabled()} is enabled,
     * then only reachable resources paths may be available.
     * @param res the resolution identifier for the Animation's and Sprite's
     * @param floor the graphical height in px to get the floor in the
     * Animation's and Sprite's
     * @param spriteDimension the dimension of the Animation's and Sprite's
     * @param spriteInsideDimension the dimension of the Animation's and
     * Sprite's pictures held within centered
     * @param frames the Animation's frames indexes
     * [0[start][end]][1[start][end]]...
     * @param reversedAnims the Animation's that must be reversed must be
     * identified by a String link id and gathered here
     * @param sfxRsrcs the sounds files to associate to this InteractiveModel
     * @param keyEvents the key events map to initially associate to this
     * InteractiveModel, String identifiers must be used to identify the
     * Animation's to map
     */
    public XtendedModel(String name, int res, int floor, Dimension spriteDimension, Dimension spriteInsideDimension, int[][] frames, String[] reversedAnims, String[] sfxRsrcs, TreeMap<KeyEventWrapper, String> keyEvents) {
        super(name, res, floor, spriteDimension, spriteInsideDimension, frames, reversedAnims, sfxRsrcs);
        setStoreMode(Sprite.MODE_TEXTURE);
        setJava2DModeEnabled(false);
        setTextureModeEnabled(true);
    }

    /**
     * creates a new instance with no associated key events map
     *
     * @param name the name to identify the model, it is used in the animation
     * path names, if {@linkplain #isInnerResourceModeEnabled()} is enabled,
     * then only reachable resources paths may be available.
     * @param res the resolution identifier for the Animation's and Sprite's
     * @param floor the graphical height in px to get the floor in the
     * Animation's and Sprite's
     * @param spriteDimension the dimension of the Animation's and Sprite's
     * @param spriteInsideDimension the dimension of the Animation's and
     * Sprite's pictures held within centered
     * @param frames the Animation's frames indexes
     * [0[start][end]][1[start][end]]...
     * @param reversedAnims the Animation's that must be reversed must be
     * identified by a String link id and gathered here
     * @param sfxRsrcs the sounds files to associate to this InteractiveModel
     */
    public XtendedModel(String name, int res, int floor, Dimension spriteDimension, Dimension spriteInsideDimension, int[][] frames, String[] reversedAnims, String[] sfxRsrcs) {
        this(name, res, floor, spriteDimension, spriteInsideDimension, frames, reversedAnims, sfxRsrcs, new TreeMap<KeyEventWrapper, String>());
    }

    /**
     * always disables {@link Sprite#MODE_JAVA2D} because store mode is locked
     * on {@link Sprite#MODE_TEXTURE texture mode} (with or without {@link Sprite#MODE_TILE tile mode})
     */
    @Override
    public final void setStoreMode(int storeMode) {
        super.setStoreMode(storeMode & ~Sprite.MODE_JAVA2D);
    }

    /**
     * always disables {@link Sprite#MODE_JAVA2D} because store mode is locked
     * on {@link Sprite#MODE_TEXTURE texture mode} (with or without {@link Sprite#MODE_TILE tile mode})
     */
    @Override
    public void setJava2DModeEnabled(boolean b) {
        super.setJava2DModeEnabled(false);
    }

    /**
     * always enables {@link Sprite#MODE_TEXTURE texture mode} (with or without {@link Sprite#MODE_TILE tile mode})
     */
    @Override
    public void setTextureModeEnabled(boolean b) {
        super.setTextureModeEnabled(true);
    }

    /**
     * @deprecated Java2D mode is off
     */
    @Override
    public boolean draw(Component pobs, Graphics2D g) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Cannot use this method with XtendedModel.");
    }

    /**
     * @deprecated Java2D mode is off
     */
    @Override
    public boolean draw(Component pobs, Graphics2D g, AffineTransform tx, PerspectiveTransform ptx) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Cannot use this method with XtendedModel.");
    }

    /**
     * @deprecated Java2D mode is off
     */
    @Override
    public boolean draw(Component pobs, Graphics2D g, int fx, Point fx_loc, Color fx_color) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Cannot use this method with XtendedModel.");
    }

    @Override
    public void GLanim(String animName, long pframeRate, boolean lock) {
        super.GLanim(animName, pframeRate, lock);
    }

    @Override
    public AnimationGLHandler GLgetCurrentAnim() {
        return super.GLgetCurrentAnim();
    }

    @Override
    public boolean GLisLoaded() {
        return super.GLisLoaded();
    }

    /**
     * default animation ID to use
     */
    public void setAnimsID_default(String animsID_default) {
        this.animsID_default = animsID_default;
    }

    /**
     * default animation ID to use
     */
    public String getAnimsID_default() {
        return animsID_default;
    }
    /**
     * Anmation player
     */
    transient GLAnimPlayer animPlayer = new GLAnimPlayer(this);

    /**
     * this method attempts to change the current Animation playing with the
     * specified one
     *
     * @return true if succeeded to change or false if not
     */
    public boolean GLanimPlayer_ChangeAnim(String anim) {
        if (GLisLoaded()) {
            if (animPlayer.getState() == STATE_CHANGE || animPlayer.getState() == STATE_LOOP || animPlayer.getState() == STATE_PLAY) {
                animPlayer.newInput(INPUT_AICS);
            }
            if (animPlayer.getState() == STATE_IDLE) {
                animPlayer.setNextAnim(anim);
                animPlayer.newInput(INPUT_MA);
                return true;
            }
        }
        return false;
    }

    /**
     * returns true or false, whether the current Animation is looping (and
     * locked) or not, resp.
     */
    public boolean GLanimPlayer_isLoop() {
        return animPlayer.getState() == STATE_LOOP;
    }

    /**
     * plays and loops over the current Animation
     */
    public void GLanimPlayer_PlayAndLoop() {
        if (GLisLoaded()) {
            if (animPlayer.getState() == STATE_PAUSE || animPlayer.getState() == STATE_IDLE || animPlayer.getState() == STATE_CHANGE) {
                animPlayer.newInput(INPUT_START);
            }
            if (animPlayer.getState() == STATE_PLAY) {
                animPlayer.newInput(INPUT_START);
            }
        }
    }

    /**
     * pauses the current Animation
     */
    public void GLanimPlayer_Pause() {
        if (GLisLoaded()) {
            animPlayer.newInput(INPUT_PAUSE);
        }
    }

    /**
     * plays once the current Animation, unlocks the loop state
     */
    public void GLanimPlayer_PlayOnce() {
        if (GLisLoaded()) {
            if (animPlayer.getState() == STATE_LOOP || animPlayer.getState() == STATE_PLAY) {
                animPlayer.newInput(INPUT_AICS);
            }
            if (animPlayer.getState() != STATE_PLAY) {
                animPlayer.newInput(INPUT_START);
            }
        }
    }

    /**
     * stops playing the current Animation
     */
    public void GLanimPlayer_Stop() {
        if (GLisLoaded()) {
            GLanimPlayer_PlayOnce();
            if (GLgetCurrentAnim() instanceof AnimationGLHandler) {
                GLgetCurrentAnim().stop();
            }
        }
    }
    protected final static BitStack _STATE_bits = new BitStack();
    protected final static int STATE_2 = _STATE_bits._newBitRange();
    protected final static int STATE_1 = _STATE_bits._newBitRange();
    protected final static int STATE_0 = _STATE_bits._newBitRange();
    public final static int STATE_IDLE = 0;
    public final static int STATE_PLAY = STATE_2;
    public final static int STATE_LOOP = STATE_1 | STATE_2;
    public final static int STATE_PAUSE = STATE_0 | STATE_1 | STATE_2;
    public final static int STATE_CHANGE = STATE_0 | STATE_2;
    protected final static BitStack _INPUT_bits = new BitStack();
    protected final static int INPUT_1 = _INPUT_bits._newBitRange();
    protected final static int INPUT_0 = _INPUT_bits._newBitRange();
    public final static int INPUT_AICS = 0;
    public final static int INPUT_MA = INPUT_1;
    public final static int INPUT_START = INPUT_0 | INPUT_1;
    public final static int INPUT_PAUSE = INPUT_0;

    static class GLAnimPlayer extends LogicalSystem {

        private String nextAnim;
        private XtendedModel m;

        public GLAnimPlayer(XtendedModel m) {
            super("GLAnimPlayer");
            this.m = m;
            reset();
        }
        int state = 0;

        @Override
        public int getState() {
            return state;
        }

        @Override
        protected int nextState(int in) {
            int newState = 0, cs = getState();
            if (lookUp(cs, STATE_0, false) && (lookUp(in, INPUT_0, true) && lookUp(in, INPUT_1, false) || lookUp(in, INPUT_0, false) && lookUp(in, INPUT_1, true) && lookUp(cs, STATE_2, false)) || lookUp(cs, STATE_0, true) && (lookUp(in, INPUT_0, true) && lookUp(in, INPUT_1, false) || lookUp(in, INPUT_0, false) && lookUp(cs, STATE_1, true))) {
                newState |= STATE_0;
            }
            if (lookUp(cs, STATE_0, false) && (lookUp(in, INPUT_0, true) && lookUp(in, INPUT_1, false) || lookUp(in, INPUT_1, true) && lookUp(cs, STATE_2, true)) || lookUp(cs, STATE_0, true) && (lookUp(in, INPUT_0, true) && lookUp(in, INPUT_1, false) || lookUp(in, INPUT_0, false) && lookUp(cs, STATE_1, true))) {
                newState |= STATE_1;
            }
            if (lookUp(cs, STATE_0, false) && (lookUp(in, INPUT_1, true) || lookUp(in, INPUT_0, true)) || lookUp(cs, STATE_0, true) && (lookUp(cs, STATE_1, true) || lookUp(in, INPUT_1, true) || lookUp(in, INPUT_0, true))) {
                newState |= STATE_2;
            }
            return newState;
        }

        @Override
        public void reset() {
            setState(STATE_IDLE);
        }

        @Override
        protected void setState(int state) {
            if (state == STATE_IDLE) {
                /**
                 * unlock animName and let it stop
                 */
                m.GLanim(m.animName, m.getFrameRate(), false);
                /**
                 * TODO unlock and let stop (not play)
                 */
            }
            if (state == STATE_PLAY) {
                /**
                 * plays animName
                 */
                m.GLanim(m.animName, m.getFrameRate(), false);
            }
            if (state == STATE_LOOP) {
                /**
                 * plays and loops animName (locks it)
                 */
                m.GLanim(m.animName, m.getFrameRate(), true);
            }
            if (state == STATE_PAUSE) {
                /**
                 * pauses animName
                 */
                if (m.GLgetCurrentAnim() instanceof AnimationGLHandler) {
                    m.GLgetCurrentAnim().pause();
                }
            }
            if (state == STATE_CHANGE) {
                /**
                 * plays the nextAnim
                 */
                m.GLanim(nextAnim, m.getFrameRate(), false);
            }
            this.state = state;
        }

        public void setNextAnim(String nextAnim) {
            this.nextAnim = nextAnim;
        }
    }
}
