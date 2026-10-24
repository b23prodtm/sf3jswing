/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.game.gl.geom;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.nio.FloatBuffer;
import net.sf.jiga.xtended.JXAException;
import net.sf.jiga.xtended.impl.system.BufferIO;
import org.lwjgl.opengl.ARBVertexBufferObject;

/**
 * * TODO : resizeable Circle in VBO binding.
 *
 * @author www.b23prodtm.info
 */
public class GLCircle extends GLGeomObject {

    final int resolutionFaces;
    final float radius;

    private FloatBuffer getVerticesArray() {
        FloatBuffer vtx = BufferIO._newf(resolutionFaces * 2);
        for (int i = 0; i < resolutionFaces; i++) {
            float cosinus = (float) Math.cos(i * 2.0 * Math.PI / resolutionFaces);
            float sinus = (float) Math.sin(i * 2.0 * Math.PI / resolutionFaces);
            vtx.put(radius * cosinus);
            vtx.put(radius * sinus);
        }
        return (FloatBuffer) vtx.flip();
    }

    /**
     *
     * @param keepBinding
     * @param xRender
     * @param yRender
     * @param lineWidth
     * @param resolutionFaces
     * @param radius
     * @param fill
     */
    public GLCircle(boolean keepBinding, float xRender, float yRender, float lineWidth, int resolutionFaces, float radius, boolean fill) {
        this(0, keepBinding, xRender, yRender, lineWidth, resolutionFaces, radius, fill);
    }

    /**
     *
     * @param keepBindingUID set it to a hashcode value if the vertices are
     * changing over time and the VBO may be reused.
     * @param keepBinding
     * @param xRender
     * @param yRender
     * @param lineWidth
     * @param resolutionFaces at least equal to 6 faces that defines a
     * (polygonal) circle
     * @param radius
     * @param fill
     */
    public GLCircle(int keepBindingUID, boolean keepBinding, float xRender, float yRender, float lineWidth, int resolutionFaces, float radius, boolean fill) {
        super(keepBindingUID, keepBinding, lineWidth, fill, new Point.Float(xRender, yRender));
        if (resolutionFaces <= 0) {
            throw new JXAException(JXAException.LEVEL.APP, "resolutionFaces must be greater than zero");
        }
        this.resolutionFaces = resolutionFaces;
        this.radius = radius;
        if (!VBOisLoaded()) {
            VBO_setItems(getVerticesArray(), BufferIO._wrapf(new float[]{generateUID()}));
        } else {
            if (generateUID() != VBO_getItem(1).data.get(0)) { /*
                 * control if update is really needed from original generated
                 * UID
                 */
                VBOupdateArray(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, getVerticesArray(), 0, 0);
            }
        }
    }

    @Override
    public FloatBuffer getVertices() {
        return (FloatBuffer) VBO_getItem(0).data.rewind();
    }

    @Override
    protected final int generateUID() {
        return (getClass().getName() + "++" + resolutionFaces + "++" + radius).hashCode();
    }

    /*
     * @Override public Runnable getList() { return new Runnable() {
     *
     * public void run() { GL11.glDrawArrays(GL11.GL_POLYGON, 0,
     * Math.round(VBO_getItem(0).data.limit() / 2f)); } }; }
     */
    @Override
    public Shape getShape() {
        Polygon p;
        if (GLGeom._shapes.containsKey(hashCode())) {
            p = (Polygon) GLGeom._shapes.get(hashCode());
        } else {
            p = new Polygon();
            FloatBuffer b = VBOMapArray(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, 0);
            b.rewind();
            while (b.hasRemaining()) {
                p.addPoint(Math.round(b.get()), Math.round(b.get()));
            }
            VBOUnmapArray(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, 0);
            if (keepBinding) {
                GLGeom._shapes.put(hashCode(), p);
            }
        }
        return p;
    }

    @Override
    public int getResolution() {
        return resolutionFaces;
    }
}
