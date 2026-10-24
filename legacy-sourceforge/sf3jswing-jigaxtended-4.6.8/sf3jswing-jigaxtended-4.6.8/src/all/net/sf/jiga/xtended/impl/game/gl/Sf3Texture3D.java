/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.game.gl;

import java.awt.Dimension;
import java.awt.image.DataBuffer;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import net.sf.jiga.xtended.impl.Animation;
import net.sf.jiga.xtended.impl.game.RenderingScene;
import net.sf.jiga.xtended.impl.system.BufferIO;
import net.sf.jiga.xtended.kernel.JXAenvUtils;
import org.lwjgl.opengl.GL11;

/**
 * Defines and stores 3D textures for {@link Animation} purposes.
 *
 * @author www.b23prodtm.info
 */
public class Sf3Texture3D extends Sf3TextureBuffer {

    private BufferIO pixelBuffer;
    private int bands;
    /** 
     * @deprecated see {@link #toTexScratchDataStore() dataStore.get()}
     */
    private int width;
    /**
     * @deprecated see {@link #toTexScratchDataStore() dataStore.get()}
     */
    private int depth;
    /**
     * @deprecated see {@link #toTexScratchDataStore() dataStore.get()}
     */
    private int height;

    /**
     *
     * @param hash
     * @param map
     */
    public Sf3Texture3D(int hash, IntBuffer map) {
        this(hash, BufferIO._new(0, false), Sf3Texture._EXTabgr, map.get(0), map.get(1), map.get(2), 4);
    }

    private Sf3Texture3D(int hash, Buffer pixelBuffer, int EXTabgr, int width, int height, int depth, int bands) {
        super(hash, BufferIO._wrapi(new int[]{width, height, Sf3Texture._getNextPowerOfTwo(depth), 0}), new BufferIO(pixelBuffer));
        assert _3DTexLayersAmount > 0 : "ERROR : " + Sf3Texture3D.class.getName() + " _3DTexLayersAmount must be greater than 0.";
        pixelBuffer.rewind();
        this.width = map.get(0);
        this.height = map.get(1);
        this.depth = map.get(2);
        this.bands = bands;
        this.EXTabgr = EXTabgr;
    }

    /**
     *
     * @param pixelBuffer
     * @param EXTabgr
     * @param width
     * @param height
     * @param depth
     * @param bands
     */
    protected Sf3Texture3D(Buffer pixelBuffer, int EXTabgr, int width, int height, int depth, int bands) {
        this((int) System.nanoTime(), pixelBuffer, EXTabgr, width, height, depth, bands);
    }

    private int EXTabgr;

    /**
     * texture pixels format
     *
     * @return
     * @see Sf3Texture#_EXTabgr
     */
    public int getEXTabgr() {
        return EXTabgr;
    }
    private int renderFormat = GL11.GL_RGBA;

    /**
     * the format to which the texture should render
     *
     * @return @default GL11#GL_RGBA
     * @see #setRenderFormat(int)
     */
    public int getRenderFormat() {
        return renderFormat;
    }

    /**
     * changes the format to which the texture should render. Changing the
     * format does as OpenGL specifies it, e.g. GL_LUMINANCE_ALPHA will take RED
     * and ALPHA components in the data buffer ONLY for the rendering.
     *
     * @param renderFormat
     */
    public void setRenderFormat(int renderFormat) {
        this.renderFormat = renderFormat;
    }

    /**
     * Returns a string representation of the image
     */
    @Override
    public String toString() {
        return getClass().getName() + " [ " + width + " x " + height + " x " + depth + " x " + bands + " (W x H x D x Bands) = " + pixelBuffer.accessData().limit();
    }


    /**
     *
     * @return
     */
    public int getWidth() {
        return map.get(0);
    }

    /**
     *
     * @return
     */
    public int getHeight() {
        return map.get(1);
    }

    /**
     *
     * @return
     */
    public int getDepth() {
        return map.get(2);
    }

    /**
     *
     * @return
     */
    public int getBands() {
        return bands;
    }

    /**
     * instanciates a Sf3Texture3D with the same size as the specified
     * Sf3Texture adding an R-Coordinate.
     *
     * @param texRef
     * @param RDepth
     * @return the Sf3Texture3D exactly of the same dimensions and properties as
     * the texRef but its pixelBuffer is cleared off and limit set to 0, so it
     * has to be filled later with {@linkplain RenderingScene#_GLLoadVRAM3D(net.sf.jiga.xtended.impl.game.Sf3Texture, int, int) subtextures}.
     */
    public static Sf3Texture3D _loadTexture3D(Sf3Texture texRef, int RDepth) {
        return _loadTexture3D((int) System.nanoTime(), texRef, RDepth);
    }

    public static Sf3Texture3D _loadTexture3D(int hashCode, Sf3Texture texRef, int RDepth) {
        try {
            Sf3Texture c = (Sf3Texture) texRef.clone();
            Sf3Texture3D tex3D = new Sf3Texture3D(hashCode, c.accessPixelBuffer(), c.getEXTabgr(), c.getWidth(), c.getHeight(), _3DTexLayersAmount * RDepth, c.getBands());
            tex3D.setPty(c.getPty());
            tex3D.setRenderFormat(c.getRenderFormat());
            tex3D.accessPixelBuffer().clear();
            tex3D.accessPixelBuffer().limit(0);
            return tex3D;
        } catch (CloneNotSupportedException ex) {
            if (JXAenvUtils._debug) {
                ex.printStackTrace();
            }
            return null;
        }

    }
    /**
     * how many contigous layers are filled in a GL_TEXTURE_3D for each 2D tex
     * (especially for Animation's) ? Then all R-coord are multiplied with this
     * number which must be > 0. @default 2
     */
    public static int _3DTexLayersAmount = 2;

    /**
     * supplied textures will be packed in a 3D texture. To each one a
     * R-coordinate will correspond. Actually, the Buffer's are concatenated in
     * a new Buffer.
     *
     * @param texs
     * @return
     */
    public static Sf3Texture3D _loadTexture3D(Sf3Texture[] texs) {
        return _loadTexture3D((int) System.nanoTime(), texs);
    }

    public static Sf3Texture3D _loadTexture3D(int hashCode, Sf3Texture[] texs) {
        int dataType = 0;
        Dimension srcSize = new Dimension();
        int bands = 0;
        int depth = Sf3Texture._getNextPowerOfTwo(_3DTexLayersAmount * texs.length);
        int EXTabgr = 0;
        for (Sf3Texture tex : texs) {
            tex.validatePixelBuffer();
            if (dataType == 0) {
                dataType = tex.getPixelBufferDataType();
            }
            if (dataType != tex.getPixelBufferDataType()) {
                throw new UnsupportedOperationException("Cannot create the 3D Texture : specified Sf3Texture's must be of the same Buffer type.");
            }
            if (srcSize.equals(new Dimension())) {
                srcSize = new Dimension(tex.getWidth(), tex.getHeight());
            }
            if (!srcSize.equals(new Dimension(tex.getWidth(), tex.getHeight()))) {
                throw new UnsupportedOperationException("Cannot create the 3D Texture : specified Sf3Texture's must be of the same size.");
            }
            if (bands == 0) {
                bands = tex.getBands();
            }
            if (bands != tex.getBands()) {
                throw new UnsupportedOperationException("Cannot create the 3D Texture : specified Sf3Texture's must be of the same color model type (3 or 4 bands).");
            }
            if (EXTabgr == 0) {
                EXTabgr = tex.getEXTabgr();
            }
            if (EXTabgr != tex.getEXTabgr()) {
                throw new UnsupportedOperationException("Cannot create the 3D Texture : specified Sf3Texture's must be of the same internal format type (GL_RGBA, GL_EXT_abgr, etc.).");
            }
        }
        Buffer newData = null;
        switch (dataType) {
            case DataBuffer.TYPE_BYTE:
                newData = BufferIO._new(srcSize.width * srcSize.height * bands * depth, false);
                break;
            case DataBuffer.TYPE_DOUBLE:
                newData = BufferIO._newd(srcSize.width * srcSize.height * bands * depth, false);
                break;
            case DataBuffer.TYPE_FLOAT:
                newData = BufferIO._newf(srcSize.width * srcSize.height * bands * depth, false);
                break;
            case DataBuffer.TYPE_INT:
                newData = BufferIO._newi(srcSize.width * srcSize.height * bands * depth, false);
                break;
            case DataBuffer.TYPE_SHORT:
            case DataBuffer.TYPE_USHORT:
                newData = BufferIO._news(srcSize.width * srcSize.height * bands * depth, false);
                break;
            default:
                throw new UnsupportedOperationException("incompatible or unknown type :" + dataType/*
                         * dataBuffer.toString()
                         */);
        }
        for (Sf3Texture tex : texs) {
            for (int i = 0; i < _3DTexLayersAmount; i++) {
                switch (tex.getPixelBufferDataType()) {
                    case DataBuffer.TYPE_BYTE:
                        ((ByteBuffer) newData).put((ByteBuffer) tex.accessPixelBuffer());
                        break;
                    case DataBuffer.TYPE_DOUBLE:
                        ((DoubleBuffer) newData).put((DoubleBuffer) tex.accessPixelBuffer());
                        break;
                    case DataBuffer.TYPE_FLOAT:
                        ((FloatBuffer) newData).put((FloatBuffer) tex.accessPixelBuffer());
                        break;
                    case DataBuffer.TYPE_INT:
                        ((IntBuffer) newData).put((IntBuffer) tex.accessPixelBuffer());
                        break;
                    case DataBuffer.TYPE_SHORT:
                    case DataBuffer.TYPE_USHORT:
                        ((ShortBuffer) newData).put((ShortBuffer) tex.accessPixelBuffer());
                        break;
                    default:
                        throw new UnsupportedOperationException("incompatible or unknown type :" + tex.getPixelBufferDataTypeName());
                }
            }
        }
        newData.flip();
        Buffer pixelBuffer = newData;
        return new Sf3Texture3D(hashCode, pixelBuffer, EXTabgr, srcSize.width, srcSize.height, depth, bands);
    }
    private float pty = .5f;

    /**
     * @param pty @default value is .5f by default
     */
    public void setPty(float pty) {
        assert pty <= 1f && pty >= 0f : "Priority must be in the range [0f, 1f].";
        this.pty = pty;
    }

    /**
     * @return @see #setTexPty(float)
     */
    public float getPty() {
        return pty;
    }
    
    
}
