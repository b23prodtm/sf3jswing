/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.game.gl;

import net.sf.jiga.xtended.impl.*;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;
import net.sf.jiga.xtended.kernel.Monitor;

/**
 *
 * @author www.b23prodtm.info
 */
public class AnimationGLHandler extends LoadAdapter implements AnimationHandler<SpriteGLHandler> {

    /**
     * 
     */
    boolean faders = false;
    /**
     * 
     */
    private int tex3DHash = (int) System.nanoTime();

    /**
     * fades from one frame to the other (useful for burning effects, stars,
     * etc.)
     *
     * @return may be true only if {@linkplain #isTexture3D()} is true @default false
     */
    public boolean isFadersEnabled() {
        return faders && texture3D;
    }

    /**
     * this may only enabled if {@linkplain #isTexture3D()} returns true
     *
     * @see #isFadersEnabled()
     */
    public void setFadersEnabled(boolean faders) {
        this.faders = faders;
    }
    /**
     * the image synch
     */
    protected Monitor imageSynch;
    int animator;
    int length;
    int statusID;
    long start;
    final int NEXT;
    final int PREVIOUS;
    long lastTick;
    SoundInput sfx;
    long frameRate;
    boolean sfx_played;
    int sfx_frame;
    String sfx_path;
    boolean sfxIsRsrc;
    SortedMap<Integer, Integer> frames;
    long hash = System.nanoTime();
    int animHash;
    int mirror;
    double zoom;
    String toString;

    @Override
    public boolean equals(Object obj) {
        return obj == null ? false : obj.hashCode() == hashCode();
    }

    @Override
    public int hashCode() {
        return (int) hash;
    }
    private boolean texture3D = false;

    /**
     * this may only enabled if the Animation width and height are smaller than {@linkplain GLHandler#_GLgetMax3DTextureSize()}
     */
    public void setTexture3D(boolean texture3D) {
        this.texture3D = texture3D;
    }

    /**
     * @default false
     */
    public boolean isTexture3D() {
        return texture3D;
    }

    /**
     * @throws IllegalStateException if the Animation has not been loaded yet.
     * @see RenderingSceneGL#_GLloadAnim(Animation)
     */
    protected AnimationGLHandler(Animation anim) {
        animHash = anim.hashCode();
        toString = anim.toString();
        assert anim.isResourceLoaded() : "anim " + anim + " must be loaded before to use it in GL mode";
        assert anim.isTextureModeEnabled() : "anim " + anim + " must be enabled for the texture mode";
        assert !Animation._GLHandlers.hasHandler(anim) : "anim " + anim + " already has a GL handler";
        frames = Animation._GLgetSpritesHashes(anim.hashCode());
        loadProgress(0, frames.size() - 1);
        /*
         * frames = Collections.synchronizedSortedMap(new TreeMap<Integer, Integer>());
         */
        imageSynch = new Monitor();
        animator = anim.getPosition();
        length = anim.length;
        statusID = anim.getState();
        start = 0;
        NEXT = anim.NEXT;
        PREVIOUS = anim.PREVIOUS;
        lastTick = 0;
        sfx = anim.getSfx();
        frameRate = anim.frameRate;
        sfx_played = false;
        sfx_frame = anim.getSfx_frame();
        sfx_path = anim.getSfx_path();
        sfxIsRsrc = anim.isSfxIsRsrc();
        mirror = anim.getMirrorOrientation();
        zoom = anim.getZoomValue();
    }

    /**
     * dis/enables transform. if it is disabled no transform will be made.
     *
     * @see #setFlipEnabled(boolean, int)
     * @see #setZoomEnabled(boolean, double)
     * @param transform dis/enable
     */
    @Override
    public void setTransformEnabled(boolean transform) {
        statusID = transform ? statusID | Animation.STATE_TRANSFORM : statusID - (statusID & Animation.STATE_TRANSFORM);
    }

    /**
     * @return true or false, whether the transform is enabled or not, resp.
     */
    @Override
    public boolean isTransformEnabled() {
        return (statusID & Animation.STATE_TRANSFORM) != 0;
    }

    /**
     * tells whether mirror transform is enabled
     *
     * @see #getMirrorOrientation()
     * @return true or false
     */
    @Override
    public boolean isMirrored() {
        return (mirror == Sprite.NONE) ? false : true;
    }

    /**
     * returns the current mirror orientation or
     *
     * @see #NONE
     * @see #HORIZONTAL
     * @see #VERTICAL
     * @see #mirror
     * @return mirror orientation
     */
    @Override
    public int getMirrorOrientation() {
        return mirror;
    }

    /**
     * activates flip Transform (mirroring) on the next {@linkplain #runValidate()}
     * call
     *
     * @param b en/disabled
     * @param mirror orientation of the mirror
     * @see Sprite#NONE
     * @see Sprite#VERTICAL
     * @see Sprite#HORIZONTAL
     */
    @Override
    public void setMirrorEnabled(boolean b, int mirror) {
        if (this.mirror != mirror || (!b && this.mirror != Sprite.NONE)) {
            this.mirror = (b) ? mirror : Sprite.NONE;
        } else {
            this.mirror = (b) ? mirror : Sprite.NONE;
        }
    }

    /**
     * dis/enables sp zoom transform
     *
     * @param b en/disabled
     * @param zoom zoom value to apply
     */
    @Override
    public void setZoomEnabled(boolean b, double zoom) {
        if (this.zoom != zoom || (!b && 1.0 != this.zoom)) {
            this.zoom = (b) ? zoom : 1.0;
        } else {
            this.zoom = (b) ? zoom : 1.0;
        }
    }

    /**
     * tells whether the zoom transform is enabled
     *
     * @see #getZoomValue()
     * @return true or false
     */
    @Override
    public boolean isZoomed() {
        return (zoom == 1.0) ? false : true;
    }

    /**
     * returns current zoom value
     *
     * @return zoom value (1.0 is no zoom)
     */
    @Override
    public double getZoomValue() {
        return zoom;
    }

    public int getAnimHash() {
        return animHash;
    }

    @Override
    public SortedMap<Integer, SpriteGLHandler> getFrames() {
        SortedMap<Integer, SpriteGLHandler> handlers = Collections.synchronizedSortedMap(new TreeMap<Integer, SpriteGLHandler>());
        for (Integer i : frames.keySet()) {
            if (Sprite._GLHandlers.hasHandler(frames.get(i))) {
                handlers.put(i, (SpriteGLHandler) Sprite._GLHandlers.getHandler(frames.get(i)));
            }
        }
        return handlers;
    }

    @Override
    public void end() {
        final Monitor monitor = imageSynch;
        synchronized (monitor) {
            if (!isReverseEnabled()) {
                animator = length - 1;
            } else {
                animator = 0;
            }
            monitor.notify();
        }
    }

    /**
     * gets the playerStatus of the animation
     *
     * @return playerStatus id
     * @see #STOPPED
     * @see #PLAYING
     * @see #PAUSED
     * @see #STATE_ACTIVE_RENDERING
     * @see #state
     */
    protected int status() {
        return statusID & (Animation.STOPPED | Animation.PLAYING | Animation.PAUSED);
    }

    /**
     * @param state go to the specified state (private use only, this is not
     * safe)
     */
    private void goState(int state) {
        statusID = statusID - (statusID & (Animation.PLAYING | Animation.PAUSED | Animation.STOPPED)) | state;
    }

    @Override
    public int getPlayerStatus() {
        return status();
    }

    @Override
    public int getPosition() {
        final Monitor monitor = imageSynch;
        synchronized (monitor) {
            monitor.notify();
            return animator;
        }
    }

    @Override
    public long getStartTime() {
        return System.currentTimeMillis() - Math.round(elapsedTime() / 1000000.);
    }
    /**
     * starting frame n
     */
    protected int startingFrame;

    @Override
    public int getStartingFrame() {
        return startingFrame;
    }

    @Override
    public long getTimeFramePosition() {
        return Math.round(getTimeFramePositionNanos() / 1000000.);
    }

    public long getTimeFramePositionNanos() {
        long posTime = length > 1 ? (long) Math.round(((double) getPosition() / ((double) length - 1.)) * (double) realTimeLengthNanos()) : 0;
        return isReverseEnabled() ? realTimeLengthNanos() - posTime : posTime;
    }

    @Override
    public boolean hasNext() {
        final Monitor monitor = imageSynch;
        synchronized (monitor) {
            int next = (isReverseEnabled()) ? -1 : +1;
            monitor.notify();
            return frames.containsKey(animator + next);
        }
    }

    @Override
    public boolean isReverseEnabled() {
        return (statusID & Animation.STATE_REVERSED) != 0;
    }

    @Override
    public int length() {
        return length;
    }

    /**
     * returns the animator for the specified operation-mode
     *
     * @param mode operation-mode
     * @see #NEXT
     * @see #PREVIOUS
     * @param position where to get the iterator from
     * @return the animator requested value (current animator won't be modified)
     */
    private int getAnimatorValue(int mode, int position) {
        int i = position;
        if (mode == NEXT) {
            if (isReverseEnabled()) {
                if (i > 0) {
                    i--;
                } else {
                    i = length - 1;
                }
            } else {
                if (i < length - 1) {
                    i++;
                } else {
                    i = 0;
                }
            }
        }
        if (mode == PREVIOUS) {
            if (isReverseEnabled()) {
                if (i < length - 1) {
                    i++;
                } else {
                    i = 0;
                }
            } else {
                if (i > 0) {
                    i--;
                } else {
                    i = length - 1;
                }
            }
        }
        return i;
    }

    /**
     * @return hashCode of the next Sprite to display
     */
    @Override
    public SpriteGLHandler next() {
        final Monitor monitor = imageSynch;
        synchronized (monitor) {
            pause();
            animator = getAnimatorValue(NEXT, animator);
            monitor.notify();
            return getSprite(animator);
        }
    }

    @Override
    public void pause() {
        goState(Animation.PAUSED);
    }

    /**
     * 
     */
    private long realTimeLengthNanos() {
        return frameRate * length;
    }

    /**
     * re-adjusts startTime to the animation current timeframe if needed,
     * thereby the animator can loop. But use play() to make loops is the
     * correct manner.
     *
     * @return new start time in ms if current is to old
     * @see #start
     * @see #play()
     */
    private long adjustStartTime() {
        if (elapsedTime() >= realTimeLengthNanos() || status() != Animation.PLAYING) {
            lastTick = System.nanoTime();
            start = lastTick - getTimeFramePositionNanos();
        }
        return start;
    }

    /**
     * elapsed period of time since the animator timer started.
     *
     * @return elapsed time since started in nanos
     * @see #realTimeLengthNanos()
     */
    private long elapsedTime() {
        long t = System.nanoTime() - start;
        if (status() != Animation.PLAYING) {
            t = 0;
        }
        return t;
    }

    @Override
    public void play() {
        int status = status();
        if (status == Animation.PAUSED) {
        }
        if (status == Animation.STOPPED) {
            rewind();
        }
        start = adjustStartTime();
        goState(Animation.PLAYING);
    }

    @Override
    public boolean playSfx() {
        if (sfx instanceof Sound) {
            sfx.play();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public long position(int i) {
        final Monitor monitor = imageSynch;
        synchronized (monitor) {
            assert (i < length && i >= 0) : getClass().getCanonicalName() + " iterator is not in the correct interval!";
            animator = i;
            monitor.notify();
        }
        adjustStartTime();
        return getTimeFramePosition();
    }

    @Override
    public int position(long timeFrame) {
        final Monitor monitor = imageSynch;
        synchronized (monitor) {
            if (isReverseEnabled()) {
                timeFrame = realTimeLength() - timeFrame;
            }
            animator = Math.min(length - 1, Math.max(0, (int) Math.round(((float) timeFrame / (float) realTimeLength()) * (float) length)));
            position(animator);
            monitor.notify();
            return animator;
        }
    }

    @Override
    public SpriteGLHandler previous() {
        final Monitor monitor = imageSynch;
        synchronized (monitor) {
            pause();
            animator = getAnimatorValue(PREVIOUS, animator);
            monitor.notify();
            return getSprite(animator);
        }
    }

    @Override
    public long realTimeLength() {
        return Math.round(realTimeLengthNanos() / 1000000.);
    }

    @Override
    public void remove() {
        final Monitor monitor = imageSynch;
        synchronized (monitor) {
            frames.remove(animator);
            monitor.notify();
        }
    }

    @Override
    public void rewind() {
        final Monitor monitor = imageSynch;
        synchronized (monitor) {
            pause();
            if (isReverseEnabled()) {
                animator = length - 1;
            } else {
                animator = 0;
            }
            sfx_played = false;
            monitor.notify();
        }
    }

    /**
     * @param delay the delay in millis between each frame change, e.g.
     * Math.round(1000./24.) = 24 FPS
     */
    @Override
    public void setFrameRate(long delay) {
        try {
            if (delay < 0) {
                throw new NullPointerException("framerate delay must be different from Zero!");
            }
            frameRate = delay * 1000000;
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setRealTimeLength(long millis) {
        frameRate = Math.round((double) millis * 1000000. / (double) length);
    }

    @Override
    public void setReverseEnabled(boolean b) {
        statusID = b ? statusID | Animation.STATE_REVERSED : statusID - (statusID & Animation.STATE_REVERSED);
    }

    @Override
    public void stop() {
        pause();
        goState(Animation.STOPPED);
    }

    /**
     * instances a new Sound interface
     *
     * @return one sound interface
     * @see Sound
     */
    private SoundInput initSound() {
        return initSound(sfx_path, sfxIsRsrc);
    }

    private SoundInput initSound(String sfx_path, boolean sfxIsRsrc) {
        return sfx = new SoundInput(this.sfx_path = sfx_path, this.sfxIsRsrc = sfxIsRsrc);
    }

    /**
     * elapsed period of time since the last tick occured on animator timer
     *
     * @return elapsed time in ms since last tick of the timer
     * @see #lastTick
     */
    private long elapsedTickTime() {
        long t = System.nanoTime() - lastTick;
        if (status() != Animation.PLAYING) {
            t = 0;
        }
        return t;
    }

    /**
     * elapsed frames since last tick occured on animator timer
     *
     * @return elapsed amount of frames since last tick of the timer
     * @see #elapsedTickTime()
     */
    private int elapsedTickFrames() {
        return Math.round((float) elapsedTickTime() / (float) realTimeLengthNanos() * (float) length);
    }

    /**
     * refreshes the animation and the cache map to current params and prints
     * current iterator value. if activerendering is activated, it also runs the
     * player thread to retrieve the animator index value to paint. it must be
     * called each time refreshing the current frame image is required.
     * @discussion (comprehensive description)
     *
     * @see #spm
     * @see #setZoomEnabled(boolean, double)
     * @see #setFlipEnabled(boolean, int)
     * @see Sprite#runValidate()
     */
    @Override
    public AnimationGLHandler runValidate() {
        if (sfx instanceof Sound) {
            if (!sfx.isResourceLoaded()) {
                sfx.loadResource();
            }
        } else {
            initSound().loadResource();
        }
        if ((getLoadState() & RenderingSceneGL._GLLOADSTATE_Loaded) == 0) {
            return this;
        }
        final Monitor monitor2 = imageSynch;
        synchronized (monitor2) {
            if (status() == Animation.PLAYING) {
                if (elapsedTime() >= realTimeLengthNanos()) {
                    stop();
                } else {
                    if (isReverseEnabled()) {
                        animator -= elapsedTickFrames();
                    } else {
                        animator += elapsedTickFrames();
                    }
                    lastTick = System.nanoTime();
                }
            }
            animator = Math.min(animator, length - 1);
            animator = Math.max(animator, 0);
            float rCoord = (float) Sf3Texture3D._3DTexLayersAmount * animator;
            float rCoordRange = Sf3Texture3D._3DTexLayersAmount * (length - 1);
            if (status() == Animation.PLAYING) {
                if (isReverseEnabled()) {
                    if (faders) {
                        rCoord = (1f - (float) elapsedTime() / (float) realTimeLengthNanos()) * rCoordRange;
                    }
                } else {
                    if (faders) {
                        rCoord = (float) elapsedTime() / (float) realTimeLengthNanos() * rCoordRange;
                    }
                }
            }
            getSprite(animator).setrCoord(rCoord);
            if (status() == Animation.PLAYING) {
                if ((isReverseEnabled() ? sfx_frame >= animator : sfx_frame <= animator) && sfx.isResourceLoaded()) {
                    if (!sfx_played) {
                        sfx_played = playSfx();
                    }
                } else {
                    sfx_played = false;
                }
            }
            monitor2.notify();
        }
        return this;
    }
    float texPty = .5f;

    public void setTexPty(float texPty) {
        this.texPty = texPty;
    }

    public float getTexPty() {
        return texPty;
    }

    public int getTex3DHash() {
        return tex3DHash;
    }

    /**
     * @return will return a true 3D texture if {@linkplain #isTexture3D()} is
     * true* public static int _getTex3DHash(AnimationGLHandler a) {
     * SpriteGLHandler s = a.getCurrentSprite(); return s != null ?
     * Sprite._GLgetTexHash(s.getSpHash()) : 0;
    }
     */
    /**
     * returns the desired sprite instance at index
     *
     * @param index the specified index of the animation Sprite to return [start
     * index; end index]
     * @return the Sprite at the specified index
     */
    @Override
    public SpriteGLHandler getSprite(int index) {
        return Sprite._GLHandlers.hasHandler(frames.get(index)) ? (SpriteGLHandler) Sprite._GLHandlers.getHandler(frames.get(index)) : null;
    }

    /**
     * returns the current Sprite hash and the associated rCoord float value
     */
    @Override
    public SpriteGLHandler getCurrentSprite() {
        final Monitor monitor = imageSynch;
        synchronized (monitor) {
            monitor.notify();
            return getSprite(animator);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + toString;
    }

    @Override
    public int hashLinkToGLObject() {
        return animHash;
    }
}
