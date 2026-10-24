/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.game.gl;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.Util;

/**
 * Vertex Buffer Objects scratch handler
 * * {@link GLHandler.ext#VertexBufferObjects} must be available
 *
 * @author www.b23prodtm.info
 */
public class VBOscratch extends MemScratch<FloatBuffer> {

    @Override
    public boolean isBound(int id) {
        return ARBVertexBufferObject.glIsBufferARB(id);
    }

    @Override
    protected void gen(IntBuffer scratch) {
        ARBVertexBufferObject.glGenBuffersARB(scratch);       
        Util.checkGLError();
    }

    @Override
    protected void delete(IntBuffer scratch) {
        ARBVertexBufferObject.glDeleteBuffersARB(scratch);
        Util.checkGLError();
    }
}
