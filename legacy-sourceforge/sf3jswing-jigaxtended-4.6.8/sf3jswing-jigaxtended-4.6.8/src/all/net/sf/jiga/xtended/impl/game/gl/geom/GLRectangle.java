/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.game.gl.geom;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.nio.FloatBuffer;
import net.sf.jiga.xtended.impl.system.BufferIO;
import org.lwjgl.opengl.ARBVertexBufferObject;

/**
 * DONE : resizeable Rectangle in VBO binding.
 *
 * @author www.b23prodtm.info
 */
public class GLRectangle extends GLGeomObject {

    Rectangle2D r;

    private FloatBuffer getVerticesArray() {
        return BufferIO._wrapf(new float[]{(float) r.getX(), (float) r.getMaxY(), (float) r.getMaxX(), (float) r.getMaxY(), (float) r.getMaxX(), (float) r.getY(), (float) r.getX(), (float) r.getY()});
    }

    /**
     *
     * @param keepBindingUID set it to a hashcode value if the vertices are
     * changing over time and the VBO may be reused.
     * @param keepBinding
     * @param r
     * @param x
     * @param y
     * @param fill
     * @param lineWidth
     */
    public GLRectangle(int keepBindingUID, boolean keepBinding, Rectangle2D r, float x, float y, boolean fill, float lineWidth) {
        super(keepBindingUID, keepBinding, lineWidth, fill, new Point.Float(x, y));
        this.r = r;
        if (!VBOisLoaded()) {
            VBO_setItems(getVerticesArray(), BufferIO._wrapf(new float[]{generateUID()}));
        } else {
            if (generateUID() != VBO_getItem(1).data.get(0)) { /*
                 * control if update is really needed from original generated UID
                 */
                VBOupdateArray(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, getVerticesArray(), 0, 0);
            }
        }
    }

    /**
     *
     * @param keepBinding
     * @param r
     * @param x
     * @param y
     * @param fill
     * @param lineWidth
     */
    public GLRectangle(boolean keepBinding, Rectangle2D r, float x, float y, boolean fill, float lineWidth) {
        this(0, keepBinding, r, x, y, fill, lineWidth);
    }

    @Override
    public FloatBuffer getVertices() {
        return (FloatBuffer) VBO_getItem(0).data.rewind();
    }

    @Override
    public Shape getShape() {
        return r;
    }

    @Override
    public int getResolution() {
        return 1;
    }

    @Override
    protected final int generateUID() {
        return r.hashCode();
    }

    /*
     * @Override public Runnable getList() { return new Runnable() {
     *
     * public void run() { GL11.glDrawArrays(GL11.GL_QUADS, 0, 4); } };
    }
     */
}
