/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.game.gl;

import net.sf.jiga.xtended.impl.game.gl.geom.GLGeom;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.sf.jiga.xtended.impl.game.RenderingScene;
import net.sf.jiga.xtended.impl.system.BufferIO;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.Util;

/**
 * This class actually renders the text in a hardware mode, using GLList's. It's
 * way faster than rendering character as texture maps when handling
 * ASCII/Unicode character map. It's based on the Font Glyphs that the Java SDK
 * provides. GLUtesselator renders the Glyphs.
 *
 * @author www.b23prodtm.info
 */
public class GLText {

    private static class GLGlyphVector implements GL2DObject {

        static Map<Integer, GLGlyph> _glyphs = Collections.synchronizedMap(new HashMap<Integer, GLGlyph>());
        public int size;
        private Point2D.Float renderAt;
        public String text;
        public Font font;

        /**
         *
         * @param renderAt
         * @param text
         * @param font
         * @param size minimum value is 3 pt.
         */
        public GLGlyphVector(Point2D.Float renderAt, String text, Font font, int size) {
            this.renderAt = renderAt;
            this.text = text;
            this.font = font;
            this.size = Math.max(3, size);
        }

        @Override
        public boolean equals(Object o) {
            return o != null ? o.hashCode() == hashCode() : false;
        }

        @Override
        public int hashCode() {
            return getUID();
        }
        GlyphVector gv = null;

        /**
         * this method costs big resources usage, use it as little as possible
         */
        public GlyphVector getGlyphVector() {
            if (gv == null) {
                gv = font.createGlyphVector(new FontRenderContext(font.getTransform(), true, false), text);
            }
            return gv;
        }

        public GLGlyph getGlyph(int charAt) {
            if (!Character.isWhitespace(text.charAt(charAt))) {
                int uid = GLGlyph._calcUID(text, charAt, font, size);
                GLGlyph gly = _glyphs.get(uid);
                if (gly == null) {
                    _glyphs.put(uid, gly = new GLGlyph(this, charAt));
                    assert uid == gly.getUID() : "integrity error with getUID()";
                }
                return gly;
            } else {
                return null;
            }
        }
        private Shape s = null;

        @Override
        public Shape getShape() {
            if (s == null) {
                final Area a = new Area();
                for (int i = 0; i < text.length(); i++) {
                    GLGlyph gly = getGlyph(i);
                    if (gly != null) {
                        a.add(new Area(gly.getShape()));
                    }
                }
                s = new Shape() {

                                @Override
                    public Rectangle getBounds() {
                        return a.getBounds();
                    }

                                @Override
                    public Rectangle2D getBounds2D() {
                        return a.getBounds2D();
                    }

                                @Override
                    public boolean contains(double x, double y) {
                        return a.contains(x, y);
                    }

                                @Override
                    public boolean contains(Point2D p) {
                        return a.contains(p);
                    }

                                @Override
                    public boolean intersects(double x, double y, double w, double h) {
                        return a.intersects(x, y, w, h);
                    }

                                @Override
                    public boolean intersects(Rectangle2D r) {
                        return a.contains(r);
                    }

                                @Override
                    public boolean contains(double x, double y, double w, double h) {
                        return a.intersects(x, y, w, h);
                    }

                                @Override
                    public boolean contains(Rectangle2D r) {
                        return a.contains(r);
                    }

                                @Override
                    public PathIterator getPathIterator(AffineTransform at) {
                        return a.getPathIterator(at);
                    }

                                @Override
                    public PathIterator getPathIterator(AffineTransform at, double flatness) {
                        return a.getPathIterator(at, flatness);
                    }
                };
            }
            return s;
        }

        @Override
        public boolean isFill() {
            return true;
        }

        @Override
        public int getResolution() {
            return size;
        }

        @Override
        public int getUID() {
            return (text + "++" + text.length() + "++" + size + "++" + font.toString()).hashCode();
        }

        /**
         * makes FontMetrics calculations to return the location where it must
         * render the glyph character (the {@linkplain #getShape() shape} should
         * be rendered after translating to this location)
         */
        public static Point2D _getRenderCharLocation(RenderingScene gld, GLGlyphVector gv, int charAt) {
            FontMetrics fm = gld.getFontMetrics(gv.font);
            Point2D.Float p = new Point2D.Float(fm.stringWidth(gv.text.substring(0, charAt)), 0);
            p.x += gv.renderAt.x;
            p.y += gv.renderAt.y;
            return p;
        }
    }

    private static class GLGlyph extends GLList implements GL2DObject {

        static Map<Integer, Shape> _glyphs = Collections.synchronizedMap(new HashMap<Integer, Shape>());
        int charAt;
        int uid;
        /*
         * public GLGlyph(Point2D.Float textRenderAt, String text, int charAt,
         * Font font, int size) {
         *
         * }
         */
        GLGlyphVector parentVector;

        public GLGlyph(GLGlyphVector parentVector, int charAt) {
            super();
            this.charAt = charAt;
            this.parentVector = parentVector;
            uid = _calcUID(parentVector.text, charAt, parentVector.font, parentVector.size);
        }

        /**
         * computes a GlyphVector and returns the Shape corresponding to the
         * selected character (charAt) in String (text) for the font
         */
        @Override
        public Shape getShape() {
            Shape s = _glyphs.get(getUID());
            if (s == null) {
                GlyphVector gv = parentVector.getGlyphVector();
                Rectangle2D glyphBounds = gv.getGlyphOutline(charAt).getBounds2D();
                _glyphs.put(getUID(), s = gv.getGlyphOutline(charAt, -(float) glyphBounds.getX(), 0));/*
                 * -(float) glyphBounds.getY()));
                 */
            }
            return s;
        }

        @Override
        public boolean equals(Object o) {
            return o != null ? o.hashCode() == hashCode() : false;
        }

        @Override
        public int hashCode() {
            return getUID();
        }

        @Override
        public int getResolution() {
            return parentVector.getResolution();
        }

        @Override
        public Runnable getList() {
            return new Runnable() {

                @Override
                public void run() {
                    GLGeom._renderShape(getShape(), getResolution());
                }
            };
        }

        @Override
        public boolean isFill() {
            return parentVector.isFill();
        }

        @Override
        public int getUID() {
            return uid;
        }

        public static int _calcUID(String text, int charAt, Font font, int size) {
            return (text.charAt(charAt) + "++" + size + "++" + font.toString()).hashCode();
        }
    }
    private static GLFX fx = new GLFX() {

        @Override
        protected void _GLRender2D(final RenderingSceneGL gld, boolean keepBinding, final GL2DObject mySp, double z, int transform, DoubleBuffer scaleArgs, DoubleBuffer rotateArgs, DoubleBuffer translateArgs, FloatBuffer colorBlend, FloatBuffer alphaBlend) {

            Rectangle sBounds = mySp.getShape().getBounds();
            GLHandler._transformBegin(sBounds, z, transform, scaleArgs, translateArgs, rotateArgs);
            GL11.glTranslatef(-sBounds.x, -sBounds.y, 0);
            /**
             * render attribute
             */
            Blending blend = GLHandler._blendingBegin(colorBlend, alphaBlend, true);

            /**
             * glyphvector
             */
            GLGlyphVector gl = null;
            if (mySp instanceof GL2DFXObject) {
                final GLGlyphVector s = (GLGlyphVector) ((GL2DFXObject) mySp).getSrc();
                gl = new GLGlyphVector(s.renderAt, s.text, s.font, s.size) {

                    @Override
                    public Shape getShape() {
                        return mySp.getShape();
                    }

                    @Override
                    public int getUID() {
                        return (s.getUID() + "++" + ((GL2DFXObject) mySp).getFX()).hashCode();
                    }
                };
            } else if (mySp instanceof GLGlyphVector) {
                gl = (GLGlyphVector) mySp;
            }
            if (gl.isFill()) {
                GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
            } else {
                GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
            }
            GLHandler._GLpushAttrib(GL11.GL_LINE_BIT);
            GL11.glLineWidth(.2f);
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_FASTEST);

            /**
             * glyphs
             */
            if (mySp instanceof GL2DFXObject) {
                GLHandler.stView.push();
                GL11.glTranslatef((float) gl.renderAt.getX(), (float) gl.renderAt.getY(), 0);
                GLGeom._renderShape(gl.getShape(), gl.getResolution());
                GLHandler.stView.pop();
            } else {
                for (int i = 0; i < gl.text.length(); i++) {
                    if (!Character.isWhitespace(gl.text.charAt(i))) {
                        GLHandler.stView.push();
                        Point2D p = GLGlyphVector._getRenderCharLocation(gld, gl, i);
                        GL11.glTranslatef((float) p.getX(), (float) p.getY(), 0);
                        GLGlyph gly = gl.getGlyph(i);
                        if (gly != null) {
                            GLList._GLListCallback(gly);
                        }
                        GLHandler.stView.pop();
                    }
                }
            }
            GLHandler._GLpopAttrib();
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
            GLHandler._blendingEnd(blend);
            GLHandler._transformEnd();
        }
    };

    /**
     * the coordinate supplied is the location of the leftmost character on the baseline. 
     */
    public static void _GLRenderText2D(final RenderingSceneGL gld, String text, float x, float y, double z, Color clr) {
        _GLRenderText2D(gld, text, x, y, z, clr, 0, null);
    }

    /**
     *  the coordinate supplied is the location of the leftmost character on the baseline.<br>
     * fx can be added (e.g. color render, alpha blend, background, etc.), but
     * it can slow down very much rendering speed (e.g. reflexion-transform
     * effects)
     */
    public static void _GLRenderText2D(final RenderingSceneGL gld, String text, float x, float y, double z, Color clr, int fx, Point fx_loc) {
        final Font font = gld.getFont();
        clr = clr instanceof Color ? clr : gld.getForeground();
        /**
         * render glyphs
         */
        GLText.fx._GLpushRender2DObject(gld, new GLGlyphVector(new Point2D.Float(x, y), text, font, font.getSize()), z, fx, fx_loc, clr, 0, null, null, null);
        /*
         * for (int i = 0; i < /*gv.getNumGlyphs()* text.length(); i++) {
         * /*final Rectangle2D glyphBounds =
         * gv.getGlyphOutline(i).getBounds2D();* if
         * (!Character.isWhitespace(text.charAt(i))) { GLGlyph glGlyph = new
         * GLGlyph(new Point2D.Float(x, y), text, i, font, font.getSize());
         * GLText.fx._GLpushRender2DObject(gld, glGlyph, z, fx, fx_loc, clr, 0, null,
         * null, null); } }
         */
    }
}
