/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.game.gl.geom;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Iterator;
import java.util.List;
import net.sf.jiga.xtended.JXAException;
import net.sf.jiga.xtended.impl.game.RenderingScene;
import net.sf.jiga.xtended.impl.game.gl.GL2DObject;
import net.sf.jiga.xtended.impl.game.gl.GLHandler;
import net.sf.jiga.xtended.impl.game.gl.MemScratch;
import net.sf.jiga.xtended.impl.game.gl.MemScratch.Item;
import net.sf.jiga.xtended.impl.game.gl.RenderingSceneGL;
import net.sf.jiga.xtended.impl.system.BufferIO;
import net.sf.jiga.xtended.kernel.DebugMap;
import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.Util;

/**
 * VBO vertexbufferobject implementation
 *
 * @author www.b23prodtm.info
 */
public abstract class GLGeomObject implements GL2DObject {

    abstract FloatBuffer getVertices();
    boolean keepBinding;
    float lineWidth;
    int keepBindingUID;

    /**
     * Creates a GLGeomObject that can be updated directly from the {@link ARBVertexBufferObject}
     * that is linked to. This can produce scalable shapes, variable gradient
     * color filling, etc.
     *
     * @param keepBindingUID if not equal to zero, the UID will always return
     * this value as a hashCode and it can be reused for updating the VBO with a
     * variable data, even if the instance is lost. (please ensure that the
     * param keepBinding is enabled if using a specific UID, unless it becomes
     * unuseful)
     * @param keepBinding tells that the VBO link mustn't be deleted.
     * @param lineWidth must be greater than zero
     * @param fill
     * @param renderAt
     */
    public GLGeomObject(int keepBindingUID, boolean keepBinding, float lineWidth, boolean fill, Float renderAt) {
        super();
        if (lineWidth == 0f) {
            throw new JXAException(JXAException.LEVEL.APP, "GLLineWidth requires a stricly positive value");
        }
        this.keepBinding = keepBinding;
        this.keepBindingUID = keepBindingUID;
        this.lineWidth = lineWidth;
        this.fill = fill;
        this.renderAt = renderAt;
    }

    /**
     *
     * @param keepBinding
     * @param lineWidth must be greater than zero
     * @param fill
     * @param renderAt
     */
    public GLGeomObject(boolean keepBinding, float lineWidth, boolean fill, Float renderAt) {
        this(0, keepBinding, lineWidth, fill, renderAt);
    }

    /**
     * @return true if all associated VBO for this {@link #getUID() Shape Object UID}
     * are bound and valid
     */
    protected final boolean VBOisLoaded() {
        List<Item<FloatBuffer>> l = GLHandler.sVbo.getItems(getUID());
        boolean loaded = l != null && !l.isEmpty();
        if (GLHandler.ext.VertexBufferObjects.GL_isAvailable() && loaded) {
            for (Iterator<Item<FloatBuffer>> i = l.iterator(); i.hasNext();) {
                loaded = GLHandler.sVbo.isBound(i.next().name) && loaded;
            }
        }
        return loaded;
    }

    /**
     * sets up data to store in the VBO arrays; e.g. vertices or color-coords
     * buffers <br>VBOs have to be loaded/updated after such a call.
     *
     * @param data floats that will be assigned to VBO_items, each data is
     * indexed in the order it is specified here (itemIndex 0, 1, etc.)
     */
    protected void VBO_setItems(FloatBuffer... data) {
        if (GLHandler.sVbo.hasItem(getUID())) {
            GLHandler.sVbo.removeItems(getUID());
        }
        /**
         * add unaffected items data[s] to data[data.length - 1] to the end of
         * the list
         */
        for (FloatBuffer d : data) {
            GLHandler.sVbo.addItem(getUID(), new Item(GLHandler.sVbo.getVRAMFindVacant(), d));
        }
    }

    protected Item<FloatBuffer> VBO_getItem(int itemIndex) {
        return GLHandler.sVbo.getItems(getUID()).get(itemIndex);
    }

    /**
     * loads the buffer into the VBO ARRAY
     */
    protected void VBOloadArray(int target, int itemIndex) {
        Item<FloatBuffer> i = VBO_getItem(itemIndex);
        if (GLHandler.ext.VertexBufferObjects.GL_isAvailable()) {
            if (DebugMap._getInstance().isDebugLevelEnabled(RenderingScene.DBUG_RENDER)) {
                DebugMap._getInstance().tickPrintln(System.out, "loading array from " + getUID() + " to VBO name : " + i.name, true);
            }
            ARBVertexBufferObject.glBindBufferARB(target, i.name);
            Util.checkGLError();
            ARBVertexBufferObject.glBufferDataARB(target, i.data.limit() * BufferIO.Data.FLOAT.byteSize, ARBVertexBufferObject.GL_STATIC_DRAW_ARB);
            Util.checkGLError();
            ARBVertexBufferObject.glBindBufferARB(target, 0);
            VBOupdateArray(target, i.data, itemIndex, 0);
        }
    }

    /**
     * CAUTION : should have been {@link #VBOloadArray(int) loaded} before
     */
    protected void VBOupdateArray(int target, FloatBuffer vertices, int itemIndex, int targetOffset) {
        Item<FloatBuffer> i = VBO_getItem(itemIndex);
        if (GLHandler.ext.VertexBufferObjects.GL_isAvailable()) {
            if (DebugMap._getInstance().isDebugLevelEnabled(RenderingScene.DBUG_RENDER)) {
                DebugMap._getInstance().tickPrintln(System.out, "updating array from " + getUID() + " to VBO name : " + i.name, true);
            }
            ARBVertexBufferObject.glBindBufferARB(target, i.name);
            ByteBuffer bMap = ARBVertexBufferObject.glMapBufferARB(target, ARBVertexBufferObject.GL_WRITE_ONLY_ARB, i.data.limit() * BufferIO.Data.FLOAT.byteSize, null);
            Util.checkGLError();
            if (bMap != null) {
                ((FloatBuffer) bMap.asFloatBuffer().position(targetOffset)).put((FloatBuffer) vertices.rewind()).flip();
            }
            ARBVertexBufferObject.glUnmapBufferARB(target);
            ARBVertexBufferObject.glBindBufferARB(target, 0);
            Util.checkGLError();
        }
        i.data = (FloatBuffer) vertices.rewind();
    }

    /**
     * (loads and) binds buffer to VBO ARRAY target
     */
    protected final void VBObindArray(int target, int itemIndex) {
        if (GLHandler.ext.VertexBufferObjects.GL_isAvailable()) {
            if (!GLHandler.sVbo.isBound(VBO_getItem(itemIndex).name)) {
                VBOloadArray(target, itemIndex);
            }
            ARBVertexBufferObject.glBindBufferARB(target, VBO_getItem(itemIndex).name);
            Util.checkGLError();
        }
    }

    /**
     * unbinds current buffer from target
     */
    protected final void VBOunbindArray(int target) {
        if (GLHandler.ext.VertexBufferObjects.GL_isAvailable()) {
            ARBVertexBufferObject.glBindBufferARB(target, 0);
        }
    }

    /**
     * (loads and) maps buffer from VBO ARRAY target
     */
    protected FloatBuffer VBOMapArray(int target, int itemIndex) {
        Item<FloatBuffer> item = VBO_getItem(itemIndex);
        if (GLHandler.ext.VertexBufferObjects.GL_isAvailable()) {
            if (!GLHandler.sVbo.isBound(VBO_getItem(itemIndex).name)) {
                VBOloadArray(target, itemIndex);
            }
            VBObindArray(target, itemIndex);
            ByteBuffer bMap = ARBVertexBufferObject.glMapBufferARB(target, ARBVertexBufferObject.GL_READ_WRITE_ARB, item.data.limit() * BufferIO.Data.FLOAT.byteSize, null);
            Util.checkGLError();
            if (bMap != null) {
                return (FloatBuffer) bMap.asFloatBuffer().rewind();
            }
        }
        return (FloatBuffer) item.data.rewind();
    }

    /**
     * unmaps current mapped buffer from target
     */
    protected void VBOUnmapArray(int target, int itemIndex) {
        if (GLHandler.ext.VertexBufferObjects.GL_isAvailable()) {
            ARBVertexBufferObject.glUnmapBufferARB(target);
            VBOunbindArray(target);
        }
    }

    @Override
    public boolean equals(Object o) {
        return o != null ? hashCode() == o.hashCode() : false;
    }

    /**
     * equals {@link #getUID()}
     */
    @Override
    public final int hashCode() {
        return getUID();
    }

    /**
     * @return {@link #generateUID()} if {@link #keepBindingUID} was {@linkplain #GLGeomObject(int, boolean, float, boolean, java.awt.geom.Point2D.Float) equal to zero}.
     */
    @Override
    public final int getUID() {
        if (keepBindingUID != 0) {
            return keepBindingUID;
        } else {
            return generateUID();
        }
    }
    boolean fill;

    @Override
    public boolean isFill() {
        return fill;
    }
    Point2D.Float renderAt;

    /**
     * permanently deletes VBO links.
     *
     * @see MemScratch#removeItems(int)
     * @see GLHandler#sVbo
     * @see #VBOdiscard(net.sf.jiga.xtended.impl.game.RenderingSceneGL) 
     */
    public void VBOdestroy() {
        if (GLHandler.ext.VertexBufferObjects.GL_isAvailable()) {
            GLHandler.sVbo.removeItems(getUID());
        }
    }
    
    /**
     * discards and permanently deletes VBO links on the next rendered frame.
     *
     * @see MemScratch#discardItems(net.sf.jiga.xtended.impl.game.RenderingSceneGL, int) 
     */
    public void VBOdiscard(RenderingSceneGL scene) {
        if (GLHandler.ext.VertexBufferObjects.GL_isAvailable()) {
            GLHandler.sVbo.discardItems(scene, getUID());
        }
    }

    /**
     * must return a unique identifier (hashcode), consistent with the equals()
     * function.<br> this method is ignored if {@link #keepBindingUID} is not
     * equal to zero, which means the VBO is already linked for updating and the
     * GLGeomObject will refer to it as long as it is not {@link #VBOdestroy() destroyed}.
     *
     * @see #hashCode()
     */
    protected abstract int generateUID();
}
