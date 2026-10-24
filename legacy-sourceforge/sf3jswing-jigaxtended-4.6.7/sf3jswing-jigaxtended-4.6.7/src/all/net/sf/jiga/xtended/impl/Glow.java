/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.jiga.xtended.impl;

/**
 *
 * @author www.b23prodtm.info
 */
public class Glow {
    public Glow() {
        glow();
    }
    /** radial speed per sec for {@linkplain #glow}
     @default Math.PI*/
    public float speed = (float) Math.PI;
    /** radial speed per sec for {@linkplain #glowCos}
     @default Math.PI/2*/
    public float speedCos = (float) (Math.PI / 2f);
    /** the glow (sin) value */
    public float glow = .0f;
    /** the glow (cos) value and slower*/
    public float glowCos = .0f;
    /** the timestamp */
    private long t = 0;
    /** the angular value */
    private float theta = 0f;
    /***/
    private float thetaCos = 0f;

    private float minAlpha = .3f;

    /** To get a glow that stays visible, use a non-zero minimum value for alpha
     * @default .3f*/
    public void setMinAlpha(float minAlpha) {
        this.minAlpha = minAlpha;
    }

    /** the glow effect is updated then use {@linkplain #glow} or {@linkplain #glowCos} to get the glow (sin- or cos-value)*/
    public void glow() {
        if (t == 0) {
            t = System.currentTimeMillis();
        }
        long now = System.currentTimeMillis();
        theta += ((float) (now - t) / 1000f) * speed;
        theta = theta % (2f * (float) Math.PI);
        thetaCos += ((float) (now - t) / 1000f) * speedCos;
        thetaCos = thetaCos % (2f * (float) Math.PI);
        glow = Math.max(.3f, Math.abs((float) Math.sin(theta)));
        glowCos = Math.max(.3f, Math.abs((float) Math.cos(thetaCos)));
        t = now;
    }

}
