/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.game.gl.geom;

import java.awt.*;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import net.sf.jiga.xtended.JXAException;
import net.sf.jiga.xtended.impl.game.RenderingScene;
import net.sf.jiga.xtended.impl.game.gl.GL2DFXObject;
import net.sf.jiga.xtended.impl.game.gl.GL2DObject;
import net.sf.jiga.xtended.impl.game.gl.GLFX;
import net.sf.jiga.xtended.impl.game.gl.GLHandler;
import net.sf.jiga.xtended.impl.game.gl.GLList;
import net.sf.jiga.xtended.impl.game.gl.RenderingSceneGL;
import net.sf.jiga.xtended.impl.system.BufferIO;
import net.sf.jiga.xtended.kernel.DebugMap;
import net.sf.jiga.xtended.kernel.JXAenvUtils;
import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.Util;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.glu.GLUtessellator;
import org.lwjgl.util.glu.GLUtessellatorCallbackAdapter;
import org.lwjgl.util.glu.tessellation.GLUtessellatorImpl;

/**
 * All public methods render within the current GL layer through {@link GLFX}.
 *
 * @author www.b23prodtm.info
 */
public class GLGeom {

    /**
     * @return a modified grown copy of the rectangle, replacing resulting
     * negative width and height with 0's; resulting rectangle is emtpy if the
     * specified rectangle was empty
     */
    public static Rectangle _grow(Rectangle r, int h, int v) {
        Rectangle rGrow = r.getBounds();
        rGrow.grow(h, v);
        rGrow.width = Math.max(rGrow.width, 0);
        rGrow.height = Math.max(rGrow.height, 0);
        return rGrow;
    }

    /**
     * checks wether the container contains the content Rectangle, according the
     * modified rules : <ol> <li>the content Rectangle is said "inside" if it
     * does fit the outlines (container.x == content.x or container.maxX ==
     * content.maxX) etc.) or is included into the container Rectangle</li>
     * <li>empty content Rectangle is allowed</li> </ol>
     */
    public static boolean _contains(Rectangle container, Rectangle content) {
        Rectangle rGrow = _grow(content, -1, -1);
        if (!container.contains(rGrow)) {
            rGrow.width = Math.max(rGrow.width, 1);
            rGrow.height = Math.max(rGrow.height, 1);
            return container.contains(rGrow);
        } else {
            return true;
        }
    }

    /**
     * @return true if the content "dirty area of" is well sized for the
     * container -size- width and height ; e.g. dirty=(40,0,100,100) fits
     * container=(0,0,100,100), hence true will be returned.
     */
    public static boolean _dirtyFits(Rectangle container, Rectangle content) {
        return _contains(new Rectangle(container.getSize()), content);
    }

    /**
     * ensure 'content' is smaller or equal to the 'containerClipSize' <br>
     * Rectangle 'content' original values are modified
     */
    public static void _shrinkContent(Dimension containerClipSize, Rectangle content) {
        _shrinkContent(containerClipSize, content, content);
    }

    /**
     * clips 'content' against 'containerClipSize' <br> Rectangle 'content'
     * original values are not modified if returnClipped is not any of the two
     * first specified Rectangle's
     *
     * @param returnClipped a Rectangle to return the clipped area
     */
    public static void _shrinkContent(Dimension containerClipSize, Rectangle content, Rectangle returnClipped) {
        Rectangle.intersect(new Rectangle(containerClipSize.getSize()), content, returnClipped);
    }

    /**
     *
     * @param gld
     * @param fx
     * @param eqr
     * @param alphaBlend
     * @param colorBlend
     */
    public static void _doPlane(RenderingSceneGL gld, int fx, DoubleBuffer eqr, FloatBuffer alphaBlend, FloatBuffer colorBlend) {
        GLHandler.stView.push();
        if (JXAenvUtils._debug) {
            System.err.println("plane eq " + eqr.get(0) + "x + " + eqr.get(1) + "y + " + eqr.get(2) + "z + " + eqr.get(3) + " >= 0");
            /**
             * stencil reflective plane
             */
        }
        Rectangle planeBounds = new Rectangle();
        if ((fx & (GLFX.gfx.FX_TOP.bit() | GLFX.gfx.FX_BOTTOM.bit())) != 0) {
            /**
             * make y->z
             */
            if (JXAenvUtils._debug) {
                System.err.println("horizontal plane");
            }
            GL11.glRotatef(-90f, 1f, 0, 0);
            int W = Math.round((float) gld.getWidth() * (float) gld._GLZRatio(gld.getZDepth()));
            planeBounds.setBounds(new Rectangle(-(int) ((float) (W - gld.getWidth()) / 2f), 0, W, -gld.getZDepth()));
        }
        if ((fx & (GLFX.gfx.FX_LEFT.bit() | GLFX.gfx.FX_RIGHT.bit())) != 0) {
            /**
             * make x->z
             */
            if (JXAenvUtils._debug) {
                System.err.println("vertical plane");
            }
            GL11.glRotatef(90f, 0, 1f, 0);
            int H = Math.round((float) gld.getHeight() * (float) gld._GLZRatio(gld.getZDepth()));
            planeBounds.setBounds(new Rectangle(0, -(int) ((float) (H - gld.getHeight()) / 2f), -gld.getZDepth(), H));
        }
        GLFX._renderTexture2D(gld, false, null, planeBounds, -eqr.get(3), 0, null, null, null, alphaBlend, colorBlend);
        GLHandler.stView.pop();
    }

    /**
     * P(t) = B(2,0)*P0 + B(2,1)*P1 + B(2,2)*P2, is a quadratic Bezier curve for
     * t ï¿? [0,1] usually, The P(t) defines a curve that binds P0 to PN
     * controlled by more interpolation points P1 to PN-1
     *
     * @param <P>
     * @param p the Point2D's (Point2D.Float, .Double as well) array to use in
     * order as control interpolation points
     * @param amount the number of points (min. value is 2) to compute between
     * P0 and PN, that affects the rendering details. For instance the rendering
     * in openGL mode uses the font-point-size to define the detail of the
     * bezier curves in font glyphs.
     * @return an array of <P>'s with coords of each computed point, its length
     * is equal to arg. "amount"
     * @see #_B(int, int, double)
     */
    public static <P extends Point2D> P[] _computeBezierCurve(P[] p, int amount) {
        assert amount > 2 : "not a valid value, amount must be greater than 2";
        Vector<Point2D> curve = new Vector<Point2D>();
        for (double t = 0.; t <= 1.; t += 1. / (double) (amount - 1)) {
            double x = 0, y = 0;
            for (int i = 0; i < p.length; i++) {
                x += _B(p.length - 1, i, t) * p[i].getX();
                y += _B(p.length - 1, i, t) * p[i].getY();
            }
            /*
             * System.err.println("bezier cubic : " + x + " ; " + y);
             */
            if (p instanceof Point.Float[]) {
                curve.add(new Point.Float((float) x, (float) y));
            } else if (p instanceof Point.Double[]) {
                curve.add(new Point.Double(x, y));
            } else {
                curve.add(new Point((int) Math.round(x), (int) Math.round(y)));
            }
        }
        if (p instanceof Point.Float[]) {
            return (P[]) curve.toArray(new Point2D.Float[curve.size()]);
        } else if (p instanceof Point.Double[]) {
            return (P[]) curve.toArray(new Point2D.Double[curve.size()]);
        } else {
            return (P[]) curve.toArray(new Point2D[curve.size()]);
        }
    }

    /**
     * the Bernstein polynomial : _B(n,m, t) = _C(n,m) * t^m (1 - t)^(n-m) for m
     * > 0 and _B(n,m, t) = t^m (1 - t)^(n-m) for m == 0
     *
     * @param n the first part of the binomial
     * @param m the second part of the binomial
     * @param t it is the parametric variable
     * @return
     * @see #_C(int, int)
     */
    public static double _B(int n, int m, double t) {
        return _bernstein(n, m, t);
    }

    static double _bernstein(int n, int m, double t) {
        assert n > 0 && m >= 0 : "_bernstein : invalid arg value for n,m : must be n > 0 and m >= 0";
        double b = Math.pow(t, m) * Math.pow(1 - t, n - m);
        if (m != 0) {
            return _C(n, m) * b;
        } else {
            return b;
        }
    }

    /**
     * Factorial and Stirling formula to n! for n greater than 100.
     *
     * @param n
     * @return
     */
    public static double _factorialN(int n) {
        if (n > 99) {
            return _stirlingFactor(n);
        } else {
            int ret = 1;
            for (int i = 1; i <= n; i++) {
                ret *= i;
            }
            return ret;
        }
    }

    private static double _stirlingFactor(int n) {
        return Math.pow((double) n / Math.E, n) * Math.sqrt(2.0 * Math.PI * n);
    }

    /**
     * C(n,m) combination of n, taken m at a time
     *
     * @param n
     * @param m
     * @return
     */
    public static double _C(int n, int m) {
        return _combine(n, m);
    }

    private static double _combine(int n, int m) {
        assert m != 0 : "zero is not allowed for arg m";
        return _factorialN(n) / (_factorialN(m) * _factorialN(n - m));
    }

    /**
     * @param gld
     * @param tess a positive integer that defines the granularity/resolution
     * for bezier-curves
     * @param fill
     * @param lineWidth
     * @param keepBinding
     * @param r
     * @param CWrgbaColors
     * @param z
     */
    protected static void _GLdoRoundRect(RenderingSceneGL gld, int keepBindingUID, boolean keepBinding, boolean fill, float lineWidth, int tess, Rectangle2D r, double z, FloatBuffer CWrgbaColors) {
        _GLdoRoundRect(gld, keepBindingUID, keepBinding, fill, lineWidth, tess, r, z, CWrgbaColors, 0, null, null, 0, null, null, null);
    }

    private static class RoundRectangle {

        float width, height;
        int tess;

        /**
         *
         * @param width
         * @param height
         * @param tess {@link #_computeBezierCurve(P[], int) } second arg value
         */
        public RoundRectangle(float width, float height, int tess) {
            this.width = width;
            this.height = height;
            this.tess = tess;
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null ? hashCode() == obj.hashCode() : false;
        }

        @Override
        public int hashCode() {
            return (getClass().getName() + "++" + width + "++" + height + "++" + tess).hashCode();
        }
    }
    static Map<Integer, RoundRectangle> _rRects = Collections.synchronizedMap(new HashMap<Integer, RoundRectangle>());
    static Map<Integer, FloatBuffer> _floatsVertices = Collections.synchronizedMap(new HashMap<Integer, FloatBuffer>());

    /**
     *
     * @param gld
     * @param keepBinding
     * @param fill
     * @param lineWidth
     * @param tess
     * @param r
     * @param z
     * @param CWrgbaColors
     * @param fx
     * @param fx_loc
     * @param fx_color
     * @param transform
     * @param scale
     * @param rotate
     * @param translate
     */
    protected static void _GLdoRoundRect(RenderingSceneGL gld, int keepBindingUID, boolean keepBinding, boolean fill, float lineWidth, int tess, Rectangle2D r, double z, FloatBuffer CWrgbaColors, int fx, Point fx_loc, Color fx_color, int transform, DoubleBuffer scale, DoubleBuffer rotate, DoubleBuffer translate) {
        float x = (float) r.getX(), y = (float) r.getY(), width = (float) r.getWidth(), height = (float) r.getHeight();
        if (width == 0 || height == 0) {
            return;
        }
        FloatBuffer pf = null;
        FloatBuffer colors = BufferIO._newf(tess * 4 * 4);
        CWrgbaColors.rewind();
        RoundRectangle rRect = new RoundRectangle(width, height, tess);
        if (_rRects.containsKey(rRect.hashCode())) {
            pf = _floatsVertices.get(rRect.hashCode());
        } else {
            float radius = height / 5f;
            float angleX = (float) ((1f - Math.cos(Math.PI / 4.)) * radius);
            float angleY = (float) ((1f - Math.sin(Math.PI / 4.)) * radius);
            Point.Float[][] pfs = new Point.Float[][]{_computeBezierCurve(new Point.Float[]{
                    new Point.Float(0f, radius),
                    new Point.Float(angleX, angleY),
                    new Point.Float(radius, 0f)
                }, tess), _computeBezierCurve(new Point.Float[]{
                    new Point.Float(width - radius, 0f),
                    new Point.Float(width - angleX, angleY),
                    new Point.Float(width, radius)
                }, tess), _computeBezierCurve(new Point.Float[]{
                    new Point.Float(width, height - radius),
                    new Point.Float(width - angleX, height - angleY),
                    new Point.Float(width - radius, height)
                }, tess), _computeBezierCurve(new Point.Float[]{
                    new Point.Float(radius, height),
                    new Point.Float(angleX, height - angleY),
                    new Point.Float(0f, height - radius)
                }, tess)};
            pf = BufferIO._newf(tess * 4 * 2);
            for (Point.Float[] roundRect : pfs) {
                for (Point.Float corner : roundRect) {
                    pf.put(corner.x);
                    pf.put(corner.y);
                }
            }
            _rRects.put(rRect.hashCode(), rRect);
            _floatsVertices.put(rRect.hashCode(), pf);
        }
        pf.rewind();
        /**
         * add each made-of-(=tess)-points-corner (out of 4 in a rectangle) a
         * color
         */
        for (int i = 0; i < pf.remaining(); i += tess * 2) {
            int p = CWrgbaColors.position();
            for (int j = 0; j < tess; j++) {
                CWrgbaColors.position(p);
                for (int c = 0; c < 4; c++) {
                    colors.put(CWrgbaColors.get());
                }
            }
        }
        _GLdoPolygon(gld, keepBindingUID, keepBinding, fill, lineWidth, x, y, pf, colors, z, fx, fx_loc, fx_color, transform, scale, rotate, translate);
    }
    static Map<Integer, Shape> _shapes = Collections.synchronizedMap(new HashMap<Integer, Shape>());

    /**
     *
     * @param gld
     * @param keepBinding
     * @param fill
     * @param lineWidth
     * @param x
     * @param y
     * @param vertices
     * @param verticesColors
     * @param z
     * @param fx
     * @param fx_loc
     * @param fx_color
     * @param transform
     * @param scale
     * @param rotate
     * @param translate
     */
    protected static void _GLdoPolygon(RenderingSceneGL gld, int keepBindingUID, boolean keepBinding, boolean fill, float lineWidth, float x, float y, FloatBuffer vertices, FloatBuffer verticesColors, double z, int fx, Point fx_loc, Color fx_color, int transform, DoubleBuffer scale, DoubleBuffer rotate, DoubleBuffer translate) {
        GLGeom.fx._GLpushRender2DObject(gld, keepBinding, new GLPolygon(keepBindingUID, keepBinding, fill, lineWidth, x, y, vertices, verticesColors), z, fx, fx_loc, fx_color, transform, scale, rotate, translate);
    }

    /**
     * @param gld context
     * @param keepBinding
     * @param x center of the circle (x)
     * @param y center of the circle (y)
     * @param z center of the circle (z)
     * @param radius radius px size
     * @param resolutionFaces number of faces to construct the polygon
     * @param lineWidth
     * @param fill it is filled with the current GL color
     * (GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL)) or traced
     * (GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE))
     */
    public static void _GLRenderCircle(RenderingSceneGL gld, int keepBindingUID, boolean keepBinding, float x, float y, float z, float radius, int resolutionFaces, float lineWidth, boolean fill) {
        _GLRenderCircle(gld, keepBindingUID, keepBinding, x, y, z, radius, resolutionFaces, lineWidth, fill, 0, null, null);
    }

    /**
     *
     * @param gld
     * @param keepBinding
     * @param x
     * @param y
     * @param z
     * @param radius
     * @param resolutionFaces
     * @param lineWidth
     * @param fill
     * @param fx
     * @param fx_loc
     * @param fx_color
     */
    public static void _GLRenderCircle(RenderingSceneGL gld, int keepBindingUID, boolean keepBinding, float x, float y, float z, float radius, int resolutionFaces, float lineWidth, boolean fill, int fx, Point fx_loc, Color fx_color) {
        _GLRenderCircle(gld, keepBindingUID, keepBinding, x, y, z, radius, resolutionFaces, lineWidth, fill, 0, null, null, null, fx, fx_loc, fx_color);
    }

    /**
     * @param gld context
     * @param keepBinding
     * @param x center of the circle (x)
     * @param y center of the circle (y)
     * @param z center of the circle (z)
     * @param radius radius px size
     * @param resolutionFaces number of faces to construct the polygon
     * @param fill it is filled with the current GL color
     * (GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL)) or traced
     * (GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE))
     * @param lineWidth
     * @param rotateArgs
     * @param translateArgs
     * @param transform
     * @param colorBlend
     * @param scaleArgs
     */
    public static void _GLRenderCircle(RenderingSceneGL gld, int keepBindingUID, boolean keepBinding, float x, float y, float z, float radius, int resolutionFaces, float lineWidth, boolean fill, int transform, DoubleBuffer scaleArgs, DoubleBuffer rotateArgs, DoubleBuffer translateArgs, FloatBuffer colorBlend) {
        _GLRenderCircle(gld, keepBindingUID, keepBinding, x, y, z, radius, resolutionFaces, lineWidth, fill, transform, scaleArgs, rotateArgs, translateArgs, 0, null, colorBlend != null ? getColorFromComponentsArray(colorBlend) : null);
    }

    /**
     *
     * @param gld
     * @param keepBinding
     * @param x
     * @param y
     * @param z
     * @param radius
     * @param resolutionFaces
     * @param lineWidth
     * @param fill
     * @param transform
     * @param scaleArgs
     * @param rotateArgs
     * @param translateArgs
     * @param fx
     * @param fx_loc
     * @param fx_color
     */
    public static void _GLRenderCircle(RenderingSceneGL gld, int keepBindingUID, boolean keepBinding, float x, float y, float z, float radius, int resolutionFaces, float lineWidth, boolean fill, int transform, DoubleBuffer scaleArgs, DoubleBuffer rotateArgs, DoubleBuffer translateArgs, int fx, Point fx_loc, Color fx_color) {
        GLCircle gc = new GLCircle(keepBindingUID, keepBinding, x, y, lineWidth, resolutionFaces, radius, fill);
        GLGeom.fx._GLpushRender2DObject(gld, gc, z, fx, fx_loc, fx_color);
    }

    /**
     *
     * @param gld
     * @param keepBinding
     * @param lineWidth
     * @param r
     * @param z
     * @param CWrgbaColors
     */
    public static void _GLdrawRoundRect(RenderingSceneGL gld, int keepBindingUID, boolean keepBinding, float lineWidth, Rectangle2D r, double z, FloatBuffer CWrgbaColors) {
        _GLdoRoundRect(gld, keepBindingUID, keepBinding, false, lineWidth, 5, r, z, CWrgbaColors);
    }

    /**
     *
     * @param gld
     * @param keepBinding
     * @param lineWidth
     * @param tess
     * @param r
     * @param z
     * @param CWrgbaColors
     */
    public static void _GLdrawRoundRect(RenderingSceneGL gld, int keepBindingUID, boolean keepBinding, float lineWidth, int tess, Rectangle2D r, double z, FloatBuffer CWrgbaColors) {
        _GLdoRoundRect(gld, keepBindingUID, keepBinding, false, lineWidth, tess, r, z, CWrgbaColors);
    }

    /**
     *
     * @param gld
     * @param keepBinding
     * @param lineWidth
     * @param r
     * @param z
     * @param CWrgbaColors
     * @param fx
     * @param fx_loc
     * @param fx_color
     */
    public static void _GLdrawRoundRect(RenderingSceneGL gld, int keepBindingUID, boolean keepBinding, float lineWidth, Rectangle2D r, double z, FloatBuffer CWrgbaColors, int fx, Point fx_loc, Color fx_color) {
        _GLdoRoundRect(gld, keepBindingUID, keepBinding, false, lineWidth, 5, r, z, CWrgbaColors, fx, fx_loc, fx_color, 0, null, null, null);
    }

    /**
     *
     * @param gld
     * @param keepBinding
     * @param lineWidth
     * @param tess
     * @param r
     * @param z
     * @param CWrgbaColors
     * @param fx
     * @param fx_loc
     * @param fx_color
     */
    public static void _GLdrawRoundRect(RenderingSceneGL gld, int keepBindingUID, boolean keepBinding, float lineWidth, int tess, Rectangle2D r, double z, FloatBuffer CWrgbaColors, int fx, Point fx_loc, Color fx_color) {
        _GLdoRoundRect(gld, keepBindingUID, keepBinding, false, lineWidth, tess, r, z, CWrgbaColors, fx, fx_loc, fx_color, 0, null, null, null);
    }

    /**
     *
     * @param gld
     * @param keepBinding
     * @param tess
     * @param r
     * @param z
     * @param CWrgbaColors
     */
    public static void _GLfillRoundRect(RenderingSceneGL gld, int keepBindingUID, boolean keepBinding, int tess, Rectangle2D r, double z, FloatBuffer CWrgbaColors) {
        _GLdoRoundRect(gld, keepBindingUID, keepBinding, true, 1, tess, r, z, CWrgbaColors);
    }

    /**
     *
     * @param gld
     * @param keepBinding
     * @param r
     * @param z
     * @param CWrgbaColors
     */
    public static void _GLfillRoundRect(RenderingSceneGL gld, int keepBindingUID, boolean keepBinding, Rectangle2D r, double z, FloatBuffer CWrgbaColors) {
        _GLdoRoundRect(gld, keepBindingUID, keepBinding, true, 1, 5, r, z, CWrgbaColors);
    }

    /**
     *
     * @param gld
     * @param keepBinding
     * @param tess
     * @param r
     * @param z
     * @param CWrgbaColors
     * @param fx
     * @param fx_loc
     * @param fx_color
     */
    public static void _GLfillRoundRect(RenderingSceneGL gld, int keepBindingUID, boolean keepBinding, int tess, Rectangle2D r, double z, FloatBuffer CWrgbaColors, int fx, Point fx_loc, Color fx_color) {
        _GLdoRoundRect(gld, keepBindingUID, keepBinding, true, 1, tess, r, z, CWrgbaColors, fx, fx_loc, fx_color, 0, null, null, null);
    }

    /**
     *
     * @param gld
     * @param keepBinding
     * @param r
     * @param z
     * @param CWrgbaColors
     * @param fx
     * @param fx_loc
     * @param fx_color
     */
    public static void _GLfillRoundRect(RenderingSceneGL gld, int keepBindingUID, boolean keepBinding, Rectangle2D r, double z, FloatBuffer CWrgbaColors, int fx, Point fx_loc, Color fx_color) {
        _GLdoRoundRect(gld, keepBindingUID, keepBinding, true, 1, 5, r, z, CWrgbaColors, fx, fx_loc, fx_color, 0, null, null, null);
    }

    /**
     *
     * @param gld
     * @param keepBinding
     * @param r
     * @param lineWidth
     * @param z
     */
    public static void _GLdrawRect(RenderingSceneGL gld, int keepBindingUID, boolean keepBinding, Rectangle r, float lineWidth, double z) {
        _GLdrawRect(gld, keepBindingUID, keepBinding, r, lineWidth, z, 0, null, null);
    }

    /**
     *
     * @param gld
     * @param keepBinding
     * @param r
     * @param lineWidth
     * @param z
     * @param fx
     * @param fx_loc
     * @param fx_color
     */
    public static void _GLdrawRect(RenderingSceneGL gld, int keepBindingUID, boolean keepBinding, Rectangle r, float lineWidth, double z, int fx, Point fx_loc, Color fx_color) {
        _GLrenderRect(gld, keepBindingUID, keepBinding, false, r, lineWidth, z, fx, fx_loc, fx_color);
    }

    /**
     *
     * @param gld
     * @param keepBinding
     * @param r
     * @param lineWidth
     * @param z
     */
    public static void _GLfillRect(RenderingSceneGL gld, int keepBindingUID, boolean keepBinding, Rectangle r, float lineWidth, double z) {
        _GLfillRect(gld, keepBindingUID, keepBinding, r, lineWidth, z, 0, null, null);
    }

    /**
     *
     * @param gld
     * @param keepBinding
     * @param r
     * @param lineWidth
     * @param z
     * @param fx
     * @param fx_loc
     * @param fx_color
     */
    public static void _GLfillRect(RenderingSceneGL gld, int keepBindingUID, boolean keepBinding, final Rectangle r, float lineWidth, double z, int fx, Point fx_loc, Color fx_color) {
        _GLrenderRect(gld, keepBindingUID, keepBinding, true, r, lineWidth, z, fx, fx_loc, fx_color);
    }

    /**
     *
     * @param gld
     * @param keepBinding
     * @param fill
     * @param r
     * @param lineWidth
     * @param z
     * @param fx
     * @param fx_loc
     * @param fx_color
     */
    public static void _GLrenderRect(RenderingSceneGL gld, int keepBindingUID, boolean keepBinding, boolean fill, Rectangle r, float lineWidth, double z, int fx, Point fx_loc, Color fx_color) {
        GLGeom.fx._GLpushRender2DObject(gld, keepBinding, new GLRectangle(keepBindingUID, keepBinding, new Rectangle(r.getSize()), (float) r.getX(), (float) r.getY(), fill, lineWidth), z, fx, fx_loc, fx_color, 0, null, null, null);
    }

    /**
     * @param gld
     * @param keepBinding
     * @param verticesColors rgba float values, the array is vertices.length x 4
     * big
     * @param lineWidth
     * @param x
     * @param vertices
     * @param y
     * @param z
     */
    public static void _GLdrawPolygon(RenderingSceneGL gld, int keepBindingUID, boolean keepBinding, float lineWidth, float x, float y, FloatBuffer vertices, FloatBuffer verticesColors, double z) {
        _GLdrawPolygon(gld, keepBindingUID, keepBinding, lineWidth, x, y, vertices, verticesColors, z, 0, null, null);
    }

    /**
     *
     * @param gld
     * @param keepBinding
     * @param lineWidth
     * @param x
     * @param y
     * @param vertices
     * @param verticesColors
     * @param z
     * @param fx
     * @param fx_loc
     * @param fx_color
     */
    public static void _GLdrawPolygon(RenderingSceneGL gld, int keepBindingUID, boolean keepBinding, float lineWidth, float x, float y, FloatBuffer vertices, FloatBuffer verticesColors, double z, int fx, Point fx_loc, Color fx_color) {
        _GLdoPolygon(gld, keepBindingUID, keepBinding, false, lineWidth, x, y, vertices, verticesColors, z, fx, fx_loc, fx_color, 0, null, null, null);
    }

    /**
     * @param gld
     * @param keepBinding
     * @param vertices
     * @param verticesColors rgba float values, the array is vertices.length x 4
     * big
     * @param z
     * @param x
     * @param y
     */
    public static void _GLfillPolygon(RenderingSceneGL gld, int keepBindingUID, boolean keepBinding, FloatBuffer vertices, float x, float y, FloatBuffer verticesColors, double z) {
        _GLfillPolygon(gld, keepBindingUID, keepBinding, x, y, vertices, verticesColors, z, 0, null, null);
    }

    /**
     *
     * @param gld
     * @param keepBinding
     * @param x
     * @param y
     * @param vertices
     * @param verticesColors
     * @param z
     * @param fx
     * @param fx_loc
     * @param fx_color
     */
    public static void _GLfillPolygon(RenderingSceneGL gld, int keepBindingUID, boolean keepBinding, float x, float y, FloatBuffer vertices, FloatBuffer verticesColors, double z, int fx, Point fx_loc, Color fx_color) {
        _GLdoPolygon(gld, keepBindingUID, keepBinding, true, 1, x, y, vertices, verticesColors, z, fx, fx_loc, fx_color, 0, null, null, null);
    }

    /**
     *
     * @param gld
     * @param keepBinding
     * @param polygon
     * @param z
     */
    public static void _GLdrawPolygon(RenderingSceneGL gld, boolean keepBinding, final Shape polygon, double z) {
        _GLdrawPolygon(gld, keepBinding, polygon, z, 0, null, null);
    }

    /**
     *
     * @param gld
     * @param keepBinding
     * @param polygon
     * @param z
     * @param fx
     * @param fx_loc
     * @param fx_color
     */
    public static void _GLdrawPolygon(RenderingSceneGL gld, boolean keepBinding, final Shape polygon, double z, int fx, Point fx_loc, Color fx_color) {
        _GLrenderCurvedShape(gld, keepBinding, polygon, 1, z, fx, fx_loc, fx_color, false);
    }

    /**
     *
     * @param gld
     * @param keepBinding
     * @param polygon
     * @param color
     * @param z
     */
    public static void _GLfillPolygon(RenderingSceneGL gld, boolean keepBinding, final Shape polygon, FloatBuffer color, double z) {
        _GLfillPolygon(gld, keepBinding, polygon, z, 0, null, color != null ? getColorFromComponentsArray(color) : null);
    }

    /**
     *
     * @param gld
     * @param keepBinding
     * @param polygon
     * @param z
     * @param fx
     * @param fx_loc
     * @param fx_color
     */
    public static void _GLfillPolygon(RenderingSceneGL gld, boolean keepBinding, final Shape polygon, double z, int fx, Point fx_loc, Color fx_color) {
        _GLrenderCurvedShape(gld, keepBinding, polygon, 1, z, fx, fx_loc, fx_color, true);
    }

    /**
     *
     * @param gld
     * @param keepBinding
     * @param shape
     * @param resolution
     * @param z
     */
    public static void _GLdrawCurvedShape(RenderingSceneGL gld, boolean keepBinding, final Shape shape, final int resolution, double z) {
        _GLdrawCurvedShape(gld, keepBinding, shape, resolution, z, 0, null, null);
    }

    /**
     *
     * @param gld
     * @param keepBinding
     * @param shape
     * @param resolution
     * @param z
     * @param fx
     * @param fx_loc
     * @param fx_color
     */
    public static void _GLdrawCurvedShape(RenderingSceneGL gld, boolean keepBinding, final Shape shape, final int resolution, double z, int fx, Point fx_loc, Color fx_color) {
        _GLrenderCurvedShape(gld, keepBinding, shape, resolution, z, fx, fx_loc, fx_color, false);
    }

    /**
     *
     * @param gld
     * @param keepBinding
     * @param shape
     * @param resolution
     * @param color
     * @param z
     */
    public static void _GLfillCurvedShape(RenderingSceneGL gld, boolean keepBinding, final Shape shape, final int resolution, FloatBuffer color, double z) {
        _GLrenderCurvedShape(gld, keepBinding, shape, resolution, z, 0, null, color != null ? getColorFromComponentsArray(color) : null, true);
    }

    /**
     *
     * @param gld
     * @param keepBinding
     * @param shape
     * @param resolution
     * @param z
     * @param fx
     * @param fx_loc
     * @param fx_color
     * @param fill
     */
    public static void _GLrenderCurvedShape(RenderingSceneGL gld, boolean keepBinding, final Shape shape, final int resolution, double z, int fx, Point fx_loc, Color fx_color, final boolean fill) {
        GLGeom.fx._GLpushRender2DObject(gld, keepBinding, new GL2DObject() {

            @Override
            public Shape getShape() {
                return shape;
            }

            @Override
            public boolean isFill() {
                return fill;
            }

            @Override
            public int getResolution() {
                return resolution;
            }

            @Override
            public int getUID() {
                return shape.hashCode();
            }
        }, z, fx, fx_loc, fx_color, 0, null, null, null);
    }

    /**
     *
     * @param c
     * @param glow
     * @return
     */
    public static Color getGlowingColor(FloatBuffer c, float glow) {
        return getGlowingColor(getColorFromComponentsArray(c), glow);
    }

    /**
     *
     * @param c
     * @param glow
     * @return
     */
    public static Color getGlowingColor(Color c, float glow) {
        return new Color(Math.round(c.getRed() * glow), Math.round(c.getGreen() * glow), Math.round(c.getBlue() * glow), Math.round(glow * 255));
    }

    /**
     *
     * @param color
     * @return
     */
    public static Color getColorFromComponentsArray(FloatBuffer color) {
        if (color.limit() == 3) {
            return new Color(color.get(0), color.get(1), color.get(2));
        }
        if (color.limit() >= 4) {
            return new Color(color.get(0), color.get(1), color.get(2), color.get(3));
        }
        throw new IllegalArgumentException("wrong number of components " + color.limit() + ", at least 3 rgb are needed (4 means rgba)");
    }

    /**
     *
     * @param CWrgbaColors
     * @param preAlpha
     * @return
     */
    public static FloatBuffer getCWrgbaColors(FloatBuffer CWrgbaColors, float preAlpha) {
        return getCWrgbaColors(getColorFromComponentsArray(BufferIO._map(CWrgbaColors, 0, 4)),
                getColorFromComponentsArray(BufferIO._map(CWrgbaColors, 4, 4)),
                getColorFromComponentsArray(BufferIO._map(CWrgbaColors, 8, 4)),
                getColorFromComponentsArray(BufferIO._map(CWrgbaColors, 12, 4)),
                preAlpha);
    }

    /**
     *
     * @param topleft
     * @param topright
     * @param downright
     * @param downleft
     * @param preAlpha
     * @return
     */
    public static FloatBuffer getCWrgbaColors(Color topleft, Color topright, Color downright, Color downleft, float preAlpha) {
        return getCWrgbaColors(getGlowingColor(topleft, preAlpha), getGlowingColor(topright, preAlpha),
                getGlowingColor(downright, preAlpha), getGlowingColor(downleft, preAlpha));
    }

    /**
     *
     * @param topleft
     * @param topright
     * @param downright
     * @param downleft
     * @return
     */
    public static FloatBuffer getCWrgbaColors(Color topleft, Color topright, Color downright, Color downleft) {
        return (FloatBuffer) BufferIO._newf(16).put(topleft.getRGBComponents(null)).put(topright.getRGBComponents(null)).put(downright.getRGBComponents(null)).put(downleft.getRGBComponents(null)).flip();
    }

    /**
     *
     * @param color
     * @param alpha
     * @return
     */
    public static FloatBuffer getCWrgbaColors(Color color, float alpha) {
        return getCWrgbaColors(color, color, color, color, alpha);
    }
    private static GLUtessellator _tess = null;

    private static GLUtessellator _newTess() {
        GLUtessellator tess = GLUtessellatorImpl.gluNewTess();
        GLUtessellatorCallbackAdapter tessCb = new GLUtessellatorCallbackAdapter() {

            @Override
            public void begin(int type) {
                GL11.glBegin(type);
            }

            @Override
            public void vertex(Object vertexData) {
                float[] vert = (float[]) vertexData;
                GL11.glVertex2f(vert[0], vert[1]);
            }

            @Override
            public void combine(double[] coords, Object[] data, float[] weight, Object[] outData) {
                for (int i = 0; i < outData.length; i++) {
                    float[] combined = new float[6];
                    combined[0] = (float) coords[0];
                    combined[1] = (float) coords[1];
                    /*
                     * combined[2] = (float) coords[2]; for (int j = 3; j < 6;
                     * j++) { for(int d = 0; d < data.length; d++) combined[j] =
                     * weight[d] * data[d][j]; }
                     */
                    outData[i] = combined;
                }
            }

            @Override
            public void end() {
                GL11.glEnd();
            }
        };
        tess.gluTessCallback(GLU.GLU_TESS_BEGIN, tessCb);
        tess.gluTessCallback(GLU.GLU_TESS_VERTEX, tessCb);
        tess.gluTessCallback(GLU.GLU_TESS_COMBINE, tessCb);
        tess.gluTessCallback(GLU.GLU_TESS_END, tessCb);
        return tess;
    }
    private static GLUtessellator _tessOff = null;

    /**
     *
     */
    public static void _initTess() {
        _tessOff = _newTess();
    }

    /**
     * use it to reduce memory allocation by sharing the tesselator object
     *
     * @see #_endTesselator()
     */
    public static void _beginTesselator() {
        if (_tessOff == null) {
            _initTess();
        }
        if (_tess == null) {
            _tess = _tessOff;
        } else if (DebugMap._getInstance().isDebugLevelEnabled(RenderingScene.DBUG_RENDER)) {
            System.err.println("between a beginTesselator() and endTesselator() block");
        }
    }

    /**
     * while in tesselator {@linkplain #_beginTesselator()}
     *
     * @return
     */
    public static GLUtessellator _getTess() {
        if (_tess != null) {
            return _tess;
        } else if (DebugMap._getInstance().isDebugLevelEnabled(RenderingScene.DBUG_RENDER)) {
            System.err.println("not between a beginTesselator() and endTesselator() block");
        }
        return null;
    }

    /**
     * @see #_beginTesselator()
     */
    public static void _endTesselator() {
        if (_tess != null) {
            _tessOff = _tess;
        } else if (DebugMap._getInstance().isDebugLevelEnabled(RenderingScene.DBUG_RENDER)) {
            System.err.println("no tess was present");
        }
        _tess = null;
    }

    /**
     *
     */
    public static void _killTess() {
        if (_tessOff != null) {
            _tessOff.gluDeleteTess();
        }
        if (_tess != null) {
            _tess.gluDeleteTess();
        }
        _tess = _tessOff = null;
    }

    /**
     *
     * @param s
     * @param resolution resolution of {@link #_computeBezierCurve(P[], int) bezier curves}
     */
    public static void _renderShape(Shape s, int resolution) {
        PathIterator pi = s.getPathIterator(null);
        Point2D.Float current = new Point2D.Float();
        _beginTesselator();
        GLUtessellator tess = _tess;
        tess.gluTessBeginPolygon(null);
        switch (pi.getWindingRule()) {
            case PathIterator.WIND_EVEN_ODD:
                tess.gluTessProperty(GLU.GLU_TESS_WINDING_RULE, GLU.GLU_TESS_WINDING_ODD);
                break;
            case PathIterator.WIND_NON_ZERO:
                tess.gluTessProperty(GLU.GLU_TESS_WINDING_RULE, GLU.GLU_TESS_WINDING_NONZERO);
                break;
            default:
                if (JXAenvUtils._debug) {
                    System.err.println(JXAenvUtils.log("no winding rule", JXAenvUtils.LVL.APP_WRN));
                }
                break;
        }
        while (!pi.isDone()) {
            float[] coords = new float[6];
            int path = pi.currentSegment(coords);
            switch (path) {
                case PathIterator.SEG_MOVETO:
                    /*
                     * if (JXAenvUtils._debug) { System.err.println(">>> GL TESS
                     * >>> begin move"); }
                     */
                    tess.gluTessBeginContour();
                    tess.gluTessVertex(new double[]{coords[0], coords[1], 0.}, 0, new float[]{coords[0], coords[1], 0f});
                    current.x = coords[0];
                    current.y = coords[1];
                    break;
                case PathIterator.SEG_CLOSE:
                    /*
                     * if (JXAenvUtils._debug) { System.err.println(">>> GL TESS
                     * >>> close"); }
                     */
                    tess.gluTessEndContour();
                    break;
                case PathIterator.SEG_LINETO:
                    /*
                     * if (JXAenvUtils._debug) { System.err.println(">>> GL TESS
                     * >>> line"); }
                     */
                    tess.gluTessVertex(new double[]{coords[0], coords[1], 0.}, 0, new float[]{coords[0], coords[1], 0f});
                    current.x = coords[0];
                    current.y = coords[1];
                    break;
                case PathIterator.SEG_CUBICTO:
                    /*
                     * if (JXAenvUtils._debug) { System.err.println(">>> GL TESS
                     * >>> cubic"); }
                     */
                    for (Point2D.Float p : GLGeom._computeBezierCurve(new Point2D.Float[]{current, new Point2D.Float(coords[0], coords[1]), new Point.Float(coords[2], coords[3]), new Point.Float(coords[4], coords[5])}, resolution)) {
                        tess.gluTessVertex(new double[]{p.x, p.y, 0.}, 0, new float[]{p.x, p.y, 0f});
                    }
                    current.x = coords[4];
                    current.y = coords[5];
                    break;
                case PathIterator.SEG_QUADTO:
                    /*
                     * if (JXAenvUtils._debug) { System.err.println(">>> GL TESS
                     * >>> quad"); }
                     */
                    for (Point2D.Float p : GLGeom._computeBezierCurve(new Point2D.Float[]{current, new Point2D.Float(coords[0], coords[1]), new Point.Float(coords[2], coords[3])}, resolution)) {
                        tess.gluTessVertex(new double[]{p.x, p.y, 0.}, 0, new float[]{p.x, p.y, 0f});
                    }
                    current.x = coords[2];
                    current.y = coords[3];
                    break;
                default:
                    break;
            }
            pi.next();
        }
        tess.gluTessEndPolygon();
        _endTesselator();
    }
    /**
     *
     */
    protected static GLFX fx = new GLFX() {

        @Override
        public void _GLRender2D(RenderingSceneGL gld, boolean keepBinding, GL2DObject mySp, double z, int transform, DoubleBuffer scaleArgs, DoubleBuffer rotateArgs, DoubleBuffer translateArgs, FloatBuffer colorBlend, FloatBuffer alphaBlend) {
            GL2DObject src = mySp;
            if (mySp instanceof GL2DFXObject) {
                src = ((GL2DFXObject) src).getSrc();
            }
            Rectangle sBounds = mySp.getShape().getBounds();
            GLHandler._transformBegin(sBounds, z, transform, scaleArgs, translateArgs, rotateArgs);
            GL11.glTranslatef(-sBounds.x, -sBounds.y, 0);
            if (mySp.isFill()) {
                GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
            } else {
                GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
            }
            Util.checkGLError();
            FloatBuffer color = GLHandler._GLgetCurrentColor();
            if (colorBlend == null) {
                colorBlend = color;
            }
            if (colorBlend.limit() < 4) {
                colorBlend = BufferIO._wrapf(new float[]{colorBlend.get(0), colorBlend.get(1), colorBlend.get(2), 1f});
            }
            if (alphaBlend == null) {
                alphaBlend = BufferIO._wrapf(new float[]{1, 1, 1, 1});
            }
            if (alphaBlend.limit() < 4) {
                alphaBlend.rewind();
                alphaBlend = BufferIO._newf(4).put(alphaBlend);
                while (alphaBlend.hasRemaining()) {
                    alphaBlend.put(1f);
                }
                alphaBlend.rewind();
            }
            GL11.glColor4f(alphaBlend.get(0) * colorBlend.get(0), alphaBlend.get(1) * colorBlend.get(1), alphaBlend.get(2) * colorBlend.get(2), alphaBlend.get(3) * colorBlend.get(3));
            if (src instanceof GLGeomObject) {
                GLHandler.stView.push();
                GLHandler._GLpushAttrib(GL11.GL_LINE_BIT | GL11.GL_ENABLE_BIT);
                GLGeomObject g = (GLGeomObject) src;
                GL11.glLineWidth(g.lineWidth);
                GL11.glEnable(GL11.GL_LINE_SMOOTH);
                GL11.glTranslatef(g.renderAt.x, g.renderAt.y, 0);
                Util.checkGLError();
                if (src instanceof GLPolygon) {
                    GLPolygon p = (GLPolygon) src;
                    if (GLHandler.ext.VertexBufferObjects.GL_isAvailable()) {
                        GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
                        Util.checkGLError();
                        p.VBObindArray(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, 1);
                        GL11.glColorPointer(4, GL11.GL_FLOAT, 0, 0);
                        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
                        p.VBObindArray(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, 0);
                        GL11.glVertexPointer(2, GL11.GL_FLOAT, 0, 0);
                        p.VBOunbindArray(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB);
                    } else {
                        GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
                        Util.checkGLError();
                        GL11.glColorPointer(4, 0, p.getVerticesColors());
                        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
                        GL11.glVertexPointer(2, 0, p.getVertices());
                    }
                    GL11.glDrawArrays(GL11.GL_POLYGON, 0, p.getResolution());
                } else if (src instanceof GLRectangle) {
                    GLRectangle r = (GLRectangle) src;
                    GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
                    Util.checkGLError();
                    if (GLHandler.ext.VertexBufferObjects.GL_isAvailable()) {
                        r.VBObindArray(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, 0);
                        GL11.glVertexPointer(2, GL11.GL_FLOAT, 0, 0);
                        r.VBOunbindArray(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB);
                    } else {
                        GL11.glVertexPointer(2, 0, r.getVertices());
                    }
                    GL11.glDrawArrays(GL11.GL_QUADS, 0, 4);
                } else if (src instanceof GLCircle) {
                    GLCircle c = (GLCircle) src;
                    GL11.glTranslatef(c.radius, c.radius, 0);
                    GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
                    Util.checkGLError();
                    if (GLHandler.ext.VertexBufferObjects.GL_isAvailable()) {
                        c.VBObindArray(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, 0);
                        GL11.glVertexPointer(2, GL11.GL_FLOAT, 0, 0);
                        c.VBOunbindArray(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB);
                    } else {
                        GL11.glVertexPointer(2, 0, c.getVertices());
                    }
                    GL11.glDrawArrays(GL11.GL_POLYGON, 0, c.resolutionFaces);
                } else {
                    throw new JXAException(JXAException.LEVEL.APP, "Unknown GL2DObject to render");
                }
                if (!keepBinding) {
                    ((GLGeomObject) src).VBOdestroy();
                }
                if (src instanceof GLCircle || src instanceof GLPolygon || src instanceof GLRectangle) {
                    GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
                    if (src instanceof GLPolygon) {
                        GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
                    }
                }
                GLHandler._GLpopAttrib();
                GLHandler.stView.pop();
            } else {
                /**
                 * displat list rendering
                 */
                if (src instanceof GLList) {
                    GLList gc = (GLList) src;
                    if (keepBinding) {
                        GLList._GLListCallback(gc);
                    } else {
                        gc.getList().run();
                    }
                } else {
                    /**
                     * default GL2DObject rendering
                     */
                    _renderShape(mySp.getShape(), mySp.getResolution());
                }
            }
            GL11.glColor4f(color.get(0), color.get(1), color.get(2), color.get(3));
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
            GLHandler._transformEnd();
            Util.checkGLError();
        }
    };
}
