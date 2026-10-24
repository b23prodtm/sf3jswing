/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl;

import java.util.SortedMap;

/**
 *
 * @author www.b23prodtm.info
 */
public interface AnimationHandler<S> {

    void end();

    S getCurrentSprite();

    SortedMap<Integer, S> getFrames();

    /**
     * returns the current mirror orientation or
     * @see #NONE
     * @see #HORIZONTAL
     * @see #VERTICAL
     * @see #mirror
     * @return mirror orientation
     */
    int getMirrorOrientation();

    int getPlayerStatus();

    int getPosition();

    /**
     * returns the desired sprite instance at index
     * @param index the specified index of the animation Sprite to return [start index; end index]
     * @return the Sprite at the specified index
     */
    S getSprite(int index);

    long getStartTime();

    int getStartingFrame();

    long getTimeFramePosition();

    /**
     * returns current zoom value
     * @return zoom value (1.0 is no zoom)
     */
    double getZoomValue();

    boolean hasNext();

    /**
     * tells whether mirror transform is enabled
     * @see #getMirrorOrientation()
     * @return true or false
     */
    boolean isMirrored();

    boolean isReverseEnabled();

    /**
     * @return true or false, whether the transform is enabled or not, resp.
     */
    boolean isTransformEnabled();

    /**
     * tells whether the zoom transform is enabled
     * @see #getZoomValue()
     * @return true or false
     */
    boolean isZoomed();

    int length();

    /**
     * @return the next Sprite to display
     */
    S next();

    void pause();

    void play();

    boolean playSfx();

    long position(int i);

    int position(long timeFrame);

    S previous();

    long realTimeLength();

    void remove();

    void rewind();

    /**
     * refreshes the animation and the cache map to current params and prints current iterator value.
     * if activerendering is activated, it also runs the player thread to retrieve the animator index value to paint.
     * it must be called each time refreshing the current frame image is required.
     * @discussion (comprehensive description)
     * @see #spm
     * @see #setZoomEnabled(boolean, double)
     * @see #setFlipEnabled(boolean, int)
     * @see Animation#runValidate()
     */
    AnimationHandler runValidate();

    /** sets the current framerate to the specified delay
     * @param delay the delay to use as framerate in ms
     */
    void setFrameRate(long millis);

    /**
     * activates flip Transform (mirroring) on the next {@linkplain #runValidate()} call
     * @param b en/disabled
     * @param mirror orientation of the mirror
     * @see #NONE
     * @see #VERTICAL
     * @see #HORIZONTAL
     */
    void setMirrorEnabled(boolean b, int mirror);

    void setRealTimeLength(long millis);

    void setReverseEnabled(boolean b);

    /**
     * dis/enables transform. if it is disabled no transform will be made.
     * @see #setFlipEnabled(boolean, int)
     * @see #setZoomEnabled(boolean, double)
     * @param transform dis/enable
     */
    void setTransformEnabled(boolean transform);

    /**
     * dis/enables sp zoom transform
     * @param b en/disabled
     * @param zoom zoom value to apply
     */
    void setZoomEnabled(boolean b, double zoom);

    void stop();
}
