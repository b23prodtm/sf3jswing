/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.game.gl;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import net.sf.jiga.xtended.impl.system.BufferIO;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.Util;

/**
 * Texture Blending
 * @author www.b23prodtm.info
 */
public class Blending {
    Blending storedState;
    FloatBuffer alpha;
    FloatBuffer color;
    int shading;
    int texEnvMode;

    /**
     * 
     * @param storeState
     * @param texEnvMode GL11.GL_TEXTURE_ENV_MODE parameter value
     * @param alpha 3 or 4 components
     * @param color 3 or 4 components
     * @param shading GL11.GL_SHADE_MODEL
     */
    public Blending(Blending storeState, int texEnvMode, FloatBuffer alpha, FloatBuffer color, int shading) {
        if (color == null) {
            color = BufferIO._wrapf(new float[]{1, 1, 1, 1});
        }
        if (color.limit() < 4) {
            color = BufferIO._wrapf(new float[]{color.get(0), color.get(1), color.get(2), 1.0F});
        }
        if (alpha == null) {
            alpha = BufferIO._wrapf(new float[]{1, 1, 1, 1});
        }
        if (alpha.limit() < 4) {
            alpha.rewind();
            alpha = BufferIO._newf(4).put(alpha);
            while (alpha.hasRemaining()) {
                alpha.put(1.0F);
            }
            alpha.rewind();
        }
        this.texEnvMode = texEnvMode;
        this.alpha = alpha;
        this.color = color;
        this.shading = shading;
        this.storedState = storeState;
    }

    public static Blending _readState() {
        IntBuffer texEnvMode = BufferIO._newi(16);
        GL11.glGetTexEnv(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, texEnvMode);
        FloatBuffer color = GLHandler._GLgetCurrentColor();
        int shadeModel = GL11.glGetInteger(GL11.GL_SHADE_MODEL);
        FloatBuffer alpha = BufferIO._wrapf(new float[]{1, 1, 1, 1});
        Util.checkGLError();
        return new Blending(null, texEnvMode.get(0), alpha, color, shadeModel);
    }

    public static void _applyState(Blending b) {
        GL11.glShadeModel(b.shading);
        GL11.glMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_EMISSION, b.color);
        GL11.glColor4f(b.alpha.get(0) * b.color.get(0), b.alpha.get(1) * b.color.get(1), b.alpha.get(2) * b.color.get(2), b.alpha.get(3) * b.color.get(3));
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, b.texEnvMode);
        Util.checkGLError();
    }
    
}
