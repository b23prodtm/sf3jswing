/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.game.gl;

/**
 *
 * @author www.b23prodtm.info
 */
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferDouble;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.io.Externalizable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.URL;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import net.sf.jiga.xtended.JXAException;
import net.sf.jiga.xtended.kernel.BitStack;
import net.sf.jiga.xtended.kernel.JXAenvUtils;
import net.sf.jiga.xtended.impl.Sprite;
import net.sf.jiga.xtended.impl.SpriteIO;
import static net.sf.jiga.xtended.impl.game.gl.Sf3Texture._EXTabgr;
import net.sf.jiga.xtended.impl.system.BufferIO;
import org.lwjgl.opengl.ARBTextureRectangle;
import org.lwjgl.opengl.EXTAbgr;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

/**
 * This class always generates POT (i.e. Power-of-two) textures. If a non-pot
 * image source is provided, the loader resizes it to fit the next greater or
 * equal pot size (width and/or height). Since textures can be freely scaled (GL
 * "magnified") with no time loss, this allows the developer not to focus on POT
 * resizing. Besides POT, there are routines called validation-processes and
 * serialization-in-output that handle pixel buffer conversions and storage. All
 * a GL loader would need to check is what buffer type was used to load the
 * texture : int, float, byte, short or double.
 * {@link #getPixelBufferDataType()} This is a modified version of the Brian
 * Matzon <brian@matzon.dk>'s org.lwjgl.examples.spaceinvaders.Texture class.
 *
 */
public class Sf3Texture extends Sf3TextureBuffer implements Externalizable, Cloneable {

    /**
     * serial version UID
     */
    private static final long serialVersionUID = 2323;

    /**
     * Get the closest greater or equal power of 2 to the fold number. <br>At
     * least equal to 64.
     *
     * @param fold The target number
     * @return The power of 2 greater or equal to the minimum allowed 64 x 64
     * texels size
     */
    public static int _getNextPowerOfTwo(int fold) {
        int pow = 2;
        while (pow < fold && pow < 64) {
            pow *= 2;
        }
        return pow;
    }

    /**
     * @see #_getNextPowerOfTwo(int)
     */
    public static Dimension _getNextPowerOfTwo(Dimension fold) {
        return new Dimension(Sf3Texture._getNextPowerOfTwo(fold.width), Sf3Texture._getNextPowerOfTwo(fold.height));
    }
    /**
     * Width of this texture
     *
     * @deprecated see {@link #toTexScratchDataStore() dataStore.get()}
     */
    private int width;
    /**
     * Height of this texture
     *
     * @deprecated see {@link #toTexScratchDataStore() dataStore.get()}
     */
    private int height;
    /**
     * size of the actual image data if a power-of-two texture has been enabled,
     * that is if {@linkplain #_EXTtex} equals {@linkplain GL11#GL_TEXTURE_2D}.
     * the image is originated at (0,0) in the power-of-two texture
     *
     * @see #_EXTtex
     */
    private final Dimension originalImageSize;
    /**
     * pixel buffers bands (e.g. RGB is 3 bands, ABGR is 4 bands)
     */
    private int bands;
    private int EXTabgr;

    /**
     * texture pixels format
     *
     * @see _EXTabgr
     */
    public int getEXTabgr() {
        return EXTabgr;
    }
    private int renderFormat = GL11.GL_RGBA;

    /**
     * the format to which the texture should render @default GL11#GL_RGBA
     *
     * @see #setRenderFormat(int)
     */
    public int getRenderFormat() {
        return renderFormat;
    }

    /**
     * changes the format to which the texture should render. Changing the
     * format does as OpenGL specifies it, e.g. GL_LUMINANCE_ALPHA will take RED
     * and ALPHA components in the data buffer ONLY for the rendering.
     */
    public void setRenderFormat(int renderFormat) {
        this.renderFormat = renderFormat;
    }

    public Sf3Texture() {
        this((int) System.nanoTime(), BufferIO._wrapi(new int[]{0, 0}, false));
    }

    /**
     *
     */
    public Sf3Texture(int hash, IntBuffer dataStore) {
        this(hash, BufferIO._new(0, false), dataStore.get(0), dataStore.get(1), 4, new Dimension(dataStore.get(0), dataStore.get(1)), 0);
    }

    /**
     * Creates a new Sf3Texture
     *
     * @param width Width of image
     * @param height Height of image
     * @param bands amount of color components (usually 4 for RGBA)
     * @param originalImageSize if null, {@linkplain #getOriginalImageSize()}
     * will return a size equal to the texture size
     */
    protected Sf3Texture(Buffer pixelBuffer, int width, int height, int bands, Dimension originalImageSize, int transferMode) {
        this((int) System.nanoTime(), pixelBuffer, width, height, bands, originalImageSize, transferMode);
    }

    protected Sf3Texture(int hash, Buffer pixelBuffer, int width, int height, int bands, Dimension originalImageSize, int transferMode) {
        super(hash, BufferIO._wrapi(new int[]{width, height, -1}), new BufferIO(pixelBuffer));
        this.width = map.get(0);
        this.height = map.get(1);
        this.bands = bands;
        this.originalImageSize = originalImageSize == null ? new Dimension(width, height) : originalImageSize;
        int transferMask = transferMode - (transferMode & bitStack._getAllBitRanges());
        /*
         * if ((transferMask & (TRANSFER_TO_POW_TWO)) != 0) { EXTtex =
         * GL11.GL_TEXTURE_2D; } else { EXTtex =
         * EXTTextureRectangle.GL_TEXTURE_RECTANGLE_EXT; }
         */
        if ((transferMode & TRANSFER_4_BANDS_BIT) != 0) {
            if ((transferMask & (TRANSFER_ABGR_TO_BGRA | TRANSFER_RGBA_TO_BGRA)) != 0) {
                EXTabgr = GL12.GL_BGRA;
            } else if ((transferMask & (TRANSFER_ABGR_TO_RGBA | TRANSFER_BGRA_TO_RGBA)) != 0) {
                EXTabgr = GL11.GL_RGBA;
            } else {
                EXTabgr = EXTAbgr.GL_ABGR_EXT;
            }
        } else if ((transferMode & TRANSFER_3_BANDS_BIT) != 0) {
            if ((transferMask & TRANSFER_BGR_TO_RGB) != 0) {
                EXTabgr = GL11.GL_RGB;
            } else {
                EXTabgr = GL12.GL_BGR;
            }
        } else {
            if (bands == 3) {
                EXTabgr = GL12.GL_BGR;
            } else if (bands == 4) {
                EXTabgr = EXTAbgr.GL_ABGR_EXT;
            } else {
                throw new JXAException("invalid bands count (must be 3 or 4)");
            }
        }
        pixelBuffer.rewind();
    }

    private final static boolean isPOT(double n) {
        double test = (Math.log10(n) / Math.log10(2.));
        return Math.round(test) == test;
    }

    /**
     * @return height of image
     */
    public int getHeight() {
        return map.get(1);
    }

    /**
     * @return width of image
     */
    public int getWidth() {
        return map.get(0);
    }

    /**
     * the texture size
     *
     * @see #getWidth()
     * @see #getHeight()
     */
    public Dimension getSize() {
        return new Dimension(getWidth(), getHeight());
    }

    /**
     * size of the actual image data, as an dimension information. IT MUST NOT
     * BE USED TO RENDER COORDS, because the texture is resized in pow-of-two
     * (even if we render with rectangles extensions). that is if
     * {@linkplain #_EXTtex} equals {@linkplain GL11#GL_TEXTURE_2D}.
     *
     * @see #_EXTtex
     * @return size of the original image data.
     */
    public Dimension getOriginalImageSize() {
        return originalImageSize;
    }

    /**
     * Returns a string representation of the image
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + hash + " [ " + getWidth() + " x " + getHeight() + " x " + bands + " (W x H x Bands) = " + pixelBuffer.accessData().limit() + " ] image : " + originalImageSize;
    }

    
    /**
     * converts from ABGR to the BGRA if {@linkplain #_EXTabgr} is
     * {@linkplain GL12#GL_BGRA} or any other supported conversion
     */
    public void validatePixelBuffer() {
        pixelBuffer.accessData().rewind();
        if (pixelBuffer.accessData().limit() == 0) {
            EXTabgr = _EXTabgr;
            return;
        }
        if (JXAenvUtils._debug) {
            System.out.print("Texture validation : ");
        }
        int transfer = 0;
        if (_EXTabgr != EXTabgr) {
            if (_EXTabgr == GL11.GL_RGBA) {
                if (EXTabgr == GL12.GL_BGRA) {
                    transfer |= TRANSFER_BGRA_TO_RGBA;
                    if (JXAenvUtils._debug) {
                        System.out.print(" BGRA->RGBA...");
                    }
                } else {
                    transfer |= TRANSFER_ABGR_TO_RGBA;
                    if (JXAenvUtils._debug) {
                        System.out.print(" ABGR->RGBA...");
                    }
                }
            } else if (_EXTabgr == GL12.GL_BGRA) {
                if (EXTabgr == GL11.GL_RGBA) {
                    transfer |= TRANSFER_RGBA_TO_BGRA;
                    if (JXAenvUtils._debug) {
                        System.out.print(" RGBA->BGRA...");
                    }
                } else {
                    transfer |= TRANSFER_ABGR_TO_BGRA;
                    if (JXAenvUtils._debug) {
                        System.out.print(" ABGR->BGRA...");
                    }
                }
            } else if (_EXTabgr == GL11.GL_RGB) {
                if (EXTabgr == GL12.GL_BGR) {
                    transfer |= TRANSFER_BGR_TO_RGB;
                    if (JXAenvUtils._debug) {
                        System.out.print(" BGR->RGB...");
                    }
                }
            } else if (_EXTabgr == GL12.GL_BGR) {
                if (EXTabgr == GL11.GL_RGB) {
                    transfer |= TRANSFER_RGB_TO_BGR;
                    if (JXAenvUtils._debug) {
                        System.out.print(" RGB->BGR...");
                    }
                }
            } else if (_EXTabgr == EXTAbgr.GL_ABGR_EXT) {
                if (EXTabgr == GL11.GL_RGBA) {
                    transfer |= TRANSFER_RGBA_TO_ABGR;
                    if (JXAenvUtils._debug) {
                        System.out.print(" RGBA->ABGR...");
                    }
                } else {
                    transfer |= TRANSFER_BGRA_TO_ABGR;
                    if (JXAenvUtils._debug) {
                        System.out.print(" BGRA->ABGR...");
                    }
                }
            }
            pixelBuffer.accessData().rewind();
            pixelBuffer.setData(_convert2DTexBuffer(pixelBuffer.accessData(), getSize(), bands, transfer, getPixelBufferDataType()));
            EXTabgr = _EXTabgr;
        }
        if (JXAenvUtils._debug) {
            System.out.println("done.");
        }
    }
    /**
     * texture 2D mode (usually changed to a texture-rectangle extension mode to
     * avoid GL_TEXTURE_2D limitations)
     */
    public static int _EXTtex = GL11.GL_TEXTURE_2D;
    /**
     * texture 2D format (usually changed to ABGR extension to avoid
     * {@linkplain BufferedImage#TYPE_4BYTE_ABGR_PRE} to BGRA array conversion)
     * supported values/conversions are : RGBA, BGRA, ABGR, RGB and BGR.
     * @default GL12.GL_BGRA or EXTAbgr.GL_ABGR_EXT if available
     */
    public static int _EXTabgr = GL12.GL_BGRA;
    private static BitStack bitStack = new BitStack();
    public static final int TRANSFER_3_BANDS_BIT = bitStack._newBitRange();
    public static final int TRANSFER_4_BANDS_BIT = bitStack._newBitRange();
    public static final int TRANSFER_ABGR_TO_BGRA = bitStack._newBit(TRANSFER_4_BANDS_BIT);
    public static final int TRANSFER_ABGR_TO_RGBA = bitStack._newBit(TRANSFER_4_BANDS_BIT);
    public static final int TRANSFER_BGRA_TO_ABGR = bitStack._newBit(TRANSFER_4_BANDS_BIT);
    public static final int TRANSFER_BGRA_TO_RGBA = bitStack._newBit(TRANSFER_4_BANDS_BIT);
    public static final int TRANSFER_RGBA_TO_ABGR = bitStack._newBit(TRANSFER_4_BANDS_BIT);
    public static final int TRANSFER_RGBA_TO_BGRA = bitStack._newBit(TRANSFER_4_BANDS_BIT);
    public static final int TRANSFER_RGB_TO_BGR = bitStack._newBit(TRANSFER_3_BANDS_BIT);
    public static final int TRANSFER_BGR_TO_RGB = bitStack._newBit(TRANSFER_3_BANDS_BIT);

    /**
     * pixels are copied from the src to the dst buffer. src limit must be at
     * least equal or greater than the dst limit. Both src and dst positions are
     * rewinded before and after the process. Dst is also cleared before to
     * begin the copy.
     */
    public static void _copyPixels(Buffer srcABGR, Dimension imageSize, Buffer dstGL, Dimension textureSize, int bands, int transferMode, int dataType) {
        /*
         * if (srcABGR.limit() < dstGL.limit()) { throw new
         * IllegalArgumentException("src limit " + srcABGR.limit() + " is
         * smaller than dst " + dstGL.limit()); }
         */
        dstGL.clear();
        dstGL.rewind();
        srcABGR.rewind();
        if (srcABGR.limit() == 0) {
            if (JXAenvUtils._debug) {
                System.err.println(JXAenvUtils.log("srcABGR is null !", JXAenvUtils.LVL.APP_WRN));
            }
            return;
        }
        for (int i = 0; i < textureSize.width; i++) {
            for (int band = 0; band < bands; band++) {
                int srcBand = band;
                if ((transferMode & TRANSFER_4_BANDS_BIT) != 0 && bands != 4) {
                    throw new JXAException("for RGBA tranfer modes, 4 bands must be specified");
                }
                if ((transferMode & TRANSFER_3_BANDS_BIT) != 0 && bands != 3) {
                    throw new JXAException("for RGB tranfer modes, 3 bands must be specified");
                }
                int transferMask = transferMode - (transferMode & bitStack._getAllBitRanges());
                if ((transferMask & TRANSFER_ABGR_TO_BGRA) != 0) {
                    srcBand = (band + 1) % bands;
                }
                if ((transferMask & TRANSFER_BGRA_TO_ABGR) != 0) {
                    srcBand = (band + 3) % bands;
                }
                if ((transferMask & (TRANSFER_ABGR_TO_RGBA | TRANSFER_RGBA_TO_ABGR | TRANSFER_BGR_TO_RGB | TRANSFER_RGB_TO_BGR)) != 0) {
                    srcBand = bands - 1 - band;
                }
                if ((transferMask & (TRANSFER_BGRA_TO_RGBA | TRANSFER_RGBA_TO_BGRA)) != 0) {
                    switch (band) {
                        case 0:
                            srcBand = 2;
                            break;
                        case 1:
                            srcBand = 1;
                            break;
                        case 2:
                            srcBand = 0;
                            break;
                        case 3:
                            srcBand = 3;
                            break;
                        default:
                            break;
                    }
                }
                for (int j = 0; j < textureSize.height; j++) {
                    if (i < imageSize.width && j < imageSize.height) {
                        dstGL.position(bands * (i + textureSize.width * j) + band);
                        srcABGR.position(bands * (i + imageSize.width * j) + srcBand);
                        _cpData(srcABGR, dstGL, dataType);
                    } else {
                        dstGL.position(bands * (i + textureSize.width * j) + band);
                        _cpData(null, dstGL, dataType);
                    }
                }
            }
        }
        dstGL.rewind();
        srcABGR.rewind();
    }

    /**
     * copies current position from src to dst of dataType (both src and dst
     * positions are incremented by one)
     */
    static void _cpData(Buffer src, Buffer dst, int dataType) {
        switch (dataType) {
            case DataBuffer.TYPE_BYTE:
                if (src == null) {
                    ((ByteBuffer) dst).put((byte) 0);
                } else {
                    ((ByteBuffer) dst).put(((ByteBuffer) src).get());
                }
                break;
            case DataBuffer.TYPE_DOUBLE:
                if (src == null) {
                    ((DoubleBuffer) dst).put(0.);
                } else {
                    ((DoubleBuffer) dst).put(((DoubleBuffer) src).get());
                }
                break;
            case DataBuffer.TYPE_FLOAT:
                if (src == null) {
                    ((FloatBuffer) dst).put(0f);
                } else {
                    ((FloatBuffer) dst).put(((FloatBuffer) src).get());
                }
                break;
            case DataBuffer.TYPE_INT:
                if (src == null) {
                    ((IntBuffer) dst).put(0);
                } else {
                    ((IntBuffer) dst).put(((IntBuffer) src).get());
                }
                break;
            case DataBuffer.TYPE_USHORT:
            case DataBuffer.TYPE_SHORT:
                if (src == null) {
                    ((ShortBuffer) dst).put((short) 0);
                } else {
                    ((ShortBuffer) dst).put(((ShortBuffer) src).get());
                }
                break;
            default:
                throw new JXAException("incompatible or unknown data type :" + BufferIO.Data._findTypeName(dataType));
        }
    }

    /**
     * convert the image buffer to fit the current GL settings : the Texture
     * format, {@linkplain GL12#GL_BGRA} or the ABGR extension. a new Buffer is
     * allocated. The src is cleared after the copy succeeded.
     *
     * @see #_EXTabgr
     */
    public static Buffer _convert2DTexBuffer(Buffer imageSrcTex, Dimension srcSize, int bands, int transferMode, int dataType) {
        imageSrcTex.rewind();
        if ((transferMode & (TRANSFER_3_BANDS_BIT | TRANSFER_4_BANDS_BIT)) != 0 && imageSrcTex.limit() > 0) {
            Buffer bgraData = null;
            switch (dataType) {
                case DataBuffer.TYPE_BYTE:
                    bgraData = BufferIO._new(srcSize.width * srcSize.height * bands, false);
                    break;
                case DataBuffer.TYPE_DOUBLE:
                    bgraData = BufferIO._newd(srcSize.width * srcSize.height * bands, false);
                    break;
                case DataBuffer.TYPE_FLOAT:
                    bgraData = BufferIO._newf(srcSize.width * srcSize.height * bands, false);
                    break;
                case DataBuffer.TYPE_INT:
                    bgraData = BufferIO._newi(srcSize.width * srcSize.height * bands, false);
                    break;
                case DataBuffer.TYPE_SHORT:
                case DataBuffer.TYPE_USHORT:
                    bgraData = BufferIO._news(srcSize.width * srcSize.height * bands, false);
                    break;
                default:
                    throw new JXAException("incompatible or unknown BufferedImage type :" + bgraData.toString());
            }
            _copyPixels(imageSrcTex, srcSize, bgraData, srcSize, bands, transferMode, dataType);
            imageSrcTex.clear();
            return bgraData;
        } else {
            return imageSrcTex;
        }
    }
    /**
     * default type {@link BufferedImage#TYPE_4BYTE_ABGR_PRE}
     */
    public static int DEFAULT_TYPE = BufferedImage.TYPE_4BYTE_ABGR_PRE;

    /**
     * Loads the named texture from : the classpath or a File with
     * {@linkplain ImageIO#read(String)}; or an Image; or a Sprite instance.
     *
     * @param src an Object source for loading the texture. (CAUTION : if it is
     * an Image, it will be flushed (data lost) and discarded after returnin
     * this method. Make a copy of the original Image instance !)
     * @param transferMode the transferMode should be from (A)BGR to another or
     * nothing (which is considered to be (A)BGR). <br> Notice : the
     * {@linkplain #validatePixelBuffer() validation} method DOES NOT convert 3
     * bands to 4 bands and you may notice that using alpha-pre-multiplied Color
     * components (it is used to be the case with Sprite's), GL Texture's are
     * better rendered.
     * @return the loaded Sf3Texture or null
     */
    public static Sf3Texture _loadTexture(Object src, int transferMode) throws InterruptedException, IOException {
        return _loadTexture((int) System.nanoTime(), src, transferMode);
    }
    /*
     * if {@link #_EXTtex} is set to {@link GLHandler.ext#TextureRectangle} then
     * the image will not be resized to a POT. @param hashCode a UID to identify
     * this Sf3Texture
     *
     */

    public static Sf3Texture _loadTexture(int hashCode, Object src, int transferMode) throws InterruptedException, IOException {
        Sf3Texture texture = null;
        Dimension imageSize = new Dimension(), textureSize = new Dimension();
        DataBuffer dataBuffer = null;
        int bands = 0;
        int transferMask = transferMode & bitStack._getAllBits();
        if ((transferMask & (TRANSFER_ABGR_TO_BGRA | TRANSFER_ABGR_TO_RGBA | TRANSFER_BGR_TO_RGB)) == 0 && transferMask != 0) {
            throw new JXAException("invalid transfer mode (use only ABGR->any or BGR->any conversions at loading !)");
        }
        Image bi = null;
        if (src instanceof Image) {
            bi = (Image) src;
        } else if (src instanceof String || src instanceof File) {
            File f = src instanceof String ? new File((String) src) : (File) src;
            bi = ImageIO.read(f);
        } else if (src instanceof URL) {
            bi = ImageIO.read((URL) src);
        } else if (src instanceof InputStream) {
            bi = ImageIO.read((InputStream) src);
        } else if (src instanceof ImageReader) {
            ImageReader r = ((ImageReader) src);
            bi = r.read(0, r.getDefaultReadParam());
            SpriteIO._mt.addImage(bi, 0);
            SpriteIO._mt.waitForID(0);
            SpriteIO._mt.removeImage(bi);
            r.dispose();
        } else {
            throw new JXAException("src is not one of the supported class.");
        }
        Sprite sp = null;
        boolean flushSp = false;
        ImageTypeSpecifier type = SpriteIO._getITS(DEFAULT_TYPE);
        if (!(src instanceof Sprite)) {
            sp = new Sprite(bi, "image/x-png", imageSize = new Dimension(bi.getWidth(null), bi.getHeight(null)));
            if (bi instanceof BufferedImage) {
                type = SpriteIO._getITS((BufferedImage) bi);
            }
            flushSp = true;
        } else {
            sp = (Sprite) src;
            type = sp.getFeatures();
            imageSize = sp.getSize();
        }
        sp.setBufferedType(type.getNumComponents() == 3 ? BufferedImage.TYPE_3BYTE_BGR : DEFAULT_TYPE);
        BufferedImage data = null;
        if (sp != null) {
            /**
             * provide a shortcut to texture rectangle (no resizing) ^^
             */
            if (_EXTtex == ARBTextureRectangle.GL_TEXTURE_RECTANGLE_ARB) {
                textureSize = imageSize.getSize();
            } else {
                textureSize.width = Sf3Texture._getNextPowerOfTwo(imageSize.width);
                textureSize.height = Sf3Texture._getNextPowerOfTwo(imageSize.height);
            }
            sp.setSize(textureSize);
            sp.runValidate();
            data = sp.toBuffered();
            textureSize.setSize(data.getWidth(), data.getHeight());
            Raster dataRaster = data.getRaster();
            dataBuffer = dataRaster.getDataBuffer();
            bands = dataRaster.getNumBands();
            if (JXAenvUtils._debug) {
                System.out.println("bufferedimage for texture has " + data.getRaster().getDataBuffer().getNumBanks() + " banks");
            }
        } else {
            return null;
        }
        Buffer pixelBuffer = null;
        switch (dataBuffer.getDataType()) {
            case DataBuffer.TYPE_BYTE:
                pixelBuffer = BufferIO._wrap(((DataBufferByte) dataBuffer).getData()).order(ByteOrder.nativeOrder());
                break;
            case DataBuffer.TYPE_DOUBLE:
                pixelBuffer = BufferIO._wrapd(((DataBufferDouble) dataBuffer).getData());
                break;
            case DataBuffer.TYPE_FLOAT:
                pixelBuffer = BufferIO._wrapf(((DataBufferFloat) dataBuffer).getData());
                break;
            case DataBuffer.TYPE_INT:
                pixelBuffer = BufferIO._wrapi(((DataBufferInt) dataBuffer).getData());
                break;
            case DataBuffer.TYPE_SHORT:
                pixelBuffer = BufferIO._wraps(((DataBufferShort) dataBuffer).getData());
                break;
            case DataBuffer.TYPE_USHORT:
                pixelBuffer = BufferIO._wraps(((DataBufferUShort) dataBuffer).getData());
                break;
            default:
                throw new JXAException("incompatible or unknown BufferedImage type :" + texture.toString());
        }
        data.flush();
        pixelBuffer = _convert2DTexBuffer(pixelBuffer, textureSize, bands, transferMode, dataBuffer.getDataType());
        if (pixelBuffer != null) {
            /**
             * instance Sf3Texture with the filled up pixelBuffer
             */
            texture = new Sf3Texture(hashCode, pixelBuffer, textureSize.width, textureSize.height, bands, imageSize, transferMode);
            if (JXAenvUtils._debug) {
                System.err.println("new Sf3Texture buffer " + BufferIO._getBufferDataTypeName(pixelBuffer) + " size : " + BufferIO._countByteSize(pixelBuffer) + " w/ " + bands + " bands is POT ? : " + (isPOT((float) pixelBuffer.limit() / (float) bands) ? "yes" : "no"));
            }
        } else {
            throw new JXAException("pixel buffer is null, no data could be loaded");
        }
        if (flushSp) {
            sp.clearResource();
        }
        return texture;
    }

    /**
     * 3 colors or 4 colors plus alpha (the components order is unknown) band
     * size of the original image file <br> this does not concern the actual
     * texture format after uploading it to the openGL texture memory
     */
    public int getBands() {
        return bands;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        validatePixelBuffer();
        Sf3Texture cloned = (Sf3Texture) super.clone();
        return cloned;
    }
    private float pty = .5f;

    /**
     * The GL establishes a ``working set'' of textures that are resident in
     * texture memory. These textures may be bound to a texture target much more
     * efficiently than textures that are not resident. By specifying a priority
     * for each texture, glPrioritizeTextures allows applications to guide the
     * GL implementation in determining which textures should be resident.<br>
     * The priorities given in priorities are clamped to the range 0 1 before
     * they are assigned. 0 indicates the lowest priority; textures with
     * priority 0 are least likely to be resident. 1 indicates the highest
     * priority; textures with priority 1 are most likely to be resident.
     * However, textures are not guaranteed to be resident until they are used.
     *
     * @default value is .5f by default
     */
    public void setPty(float pty) {
        assert pty <= 1f && pty >= 0f : "Priority must be in the range [0f, 1f].";
        this.pty = pty;
    }

    /**
     * @see #setTexPty(float)
     */
    public float getPty() {
        return pty;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        int currentPty = Thread.currentThread().getPriority();
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        validatePixelBuffer();
        super.writeExternal(out);
        out.writeInt(EXTabgr);
        out.writeInt(bands);
        out.writeInt(height);
        out.writeInt(originalImageSize.width);
        out.writeInt(originalImageSize.height);
        out.writeFloat(pty);
        out.writeInt(renderFormat);
        out.writeInt(width);
        out.flush();
        Thread.currentThread().setPriority(currentPty);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int currentPty = Thread.currentThread().getPriority();
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        super.readExternal(in);
        EXTabgr = in.readInt();
        bands = in.readInt();
        /*
         * height =
         */ in.readInt();
        height = getHeight();
        originalImageSize.width = in.readInt();
        originalImageSize.height = in.readInt();
        pty = in.readFloat();
        renderFormat = in.readInt();
        /*
         * width =
         */ in.readInt();
        width = getWidth();
        Thread.currentThread().setPriority(currentPty);
    }

    /**
     * datastore : IntBuffer{tex width, tex height, -1}
     */
    @Override
    public IntBuffer toTexScratchDataStore() {
        return super.toTexScratchDataStore();
    }
}
