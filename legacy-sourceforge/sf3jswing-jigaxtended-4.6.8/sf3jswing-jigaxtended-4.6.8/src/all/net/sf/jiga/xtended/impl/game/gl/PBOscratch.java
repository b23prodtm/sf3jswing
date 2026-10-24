/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.game.gl;

import java.nio.IntBuffer;
import org.lwjgl.opengl.ARBPixelBufferObject;
import org.lwjgl.opengl.Util;

/**
 * Pixel Buffer Objects scratch handler
 * {@link GLHandler.ext#PixelBufferObjects} must be available
 * @author www.b23prodtm.info
 */
public class PBOscratch extends MemScratch {

    @Override
    public boolean isBound(int name) {
        return ARBPixelBufferObject.glIsBufferARB(name);
    }

    @Override
    protected void gen(IntBuffer scratch) {
        ARBPixelBufferObject.glGenBuffersARB(scratch);
        Util.checkGLError();
    }

    @Override
    protected void delete(IntBuffer scratch) {
        ARBPixelBufferObject.glDeleteBuffersARB(scratch);
        Util.checkGLError();
    }
}
