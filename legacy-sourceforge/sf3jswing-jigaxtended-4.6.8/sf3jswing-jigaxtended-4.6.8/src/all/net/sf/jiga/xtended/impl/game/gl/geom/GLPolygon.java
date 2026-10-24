/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.game.gl.geom;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.nio.FloatBuffer;
import net.sf.jiga.xtended.impl.system.BufferIO;
import org.lwjgl.opengl.ARBVertexBufferObject;

/**
 * * TODO : resizeable Polygon in VBO binding.
 *
 * @author www.b23prodtm.info
 */
public class GLPolygon extends GLGeomObject {

    /**
     *
     * @param keepBinding
     * @param fill
     * @param lineWidth
     * @param x
     * @param y
     * @param _2Dvertices
     * @param verticesColors
     */
    public GLPolygon(boolean keepBinding, boolean fill, float lineWidth, float x, float y, FloatBuffer _2Dvertices, FloatBuffer verticesColors) {
        this(0, keepBinding, fill, lineWidth, x, y, _2Dvertices, verticesColors);
    }

    /**
     *
     * @param keepBindingUID set it to a hashcode value if the vertices are
     * changing over time and the VBO may be reused.
     * @param keepBinding
     * @param fill
     * @param lineWidth
     * @param x
     * @param y
     * @param _2Dvertices
     * @param verticesColors
     */
    public GLPolygon(int keepBindingUID, boolean keepBinding, boolean fill, float lineWidth, float x, float y, FloatBuffer _2Dvertices, FloatBuffer verticesColors) {
        super(keepBindingUID, keepBinding, lineWidth, fill, new Point.Float(x, y));
        String n = "n";
        _2Dvertices.rewind();
        verticesColors.rewind();
        while (_2Dvertices.hasRemaining()) {            
            n += _2Dvertices.get() + ":" + _2Dvertices.get() + "::" + verticesColors.get() + "//";
        }
        _2Dvertices.rewind();
        verticesColors.rewind();
        this.n = n;
        if (!VBOisLoaded()) {
            VBO_setItems(_2Dvertices, verticesColors, BufferIO._wrapf(new float[]{generateUID()}));
        } else {
            if (generateUID() != VBO_getItem(2).data.get(0)) { /*
                 * control if update is really needed from original generated
                 * UID
                 */
                VBOupdateArray(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, _2Dvertices, 0, 0);
                VBOupdateArray(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, verticesColors, 1, 0);
            }
        }
    }
    final String n;

    @Override
    public FloatBuffer getVertices() {
        return (FloatBuffer) VBO_getItem(0).data.rewind();
    }

    public FloatBuffer getVerticesColors() {
        return (FloatBuffer) VBO_getItem(1).data.rewind();
    }

    @Override
    public Shape getShape() {
        Polygon p;
        if (GLGeom._shapes.containsKey(hashCode())) {
            p = (Polygon) GLGeom._shapes.get(hashCode());
        } else {
            p = new Polygon();
            FloatBuffer vertices = VBOMapArray(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, 0);
            vertices.rewind();
            while (vertices.hasRemaining()) {
                p.addPoint(Math.round(vertices.get()), Math.round(vertices.get()));
            }
            VBOUnmapArray(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, 0);
            if (keepBinding) {
                GLGeom._shapes.put(hashCode(), p);
            }
        }
        return p;
    }

    /**
     * the polygon UID, getShape is consistent with getUID, so that any other
     * Polygon may be equal to this if and only if both UID are equal.
     */
    @Override
    protected final int generateUID() {
        return (getClass().getName() + "++" + n).hashCode();
    }

    @Override
    public int getResolution() {
        return getVertices().limit();
    }

    /*
     * @Override public Runnable getList() { return new Runnable() {
     *
     * public void run() { /* GL11.glVertexPointer(2, 0, vertexes); not stored
     * in display list, but used to dereference vertex data to store
     */
    /*
     * GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY); not stored in display
     * list
     *
     * GL11.glDrawArrays(GL11.GL_POLYGON, 0,
     * Math.round(VBO_getItem(0).data.limit() / 2f)); /*
     * GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY); not stored in display
     * list
     *
     * }
     * }; }
     */
}
