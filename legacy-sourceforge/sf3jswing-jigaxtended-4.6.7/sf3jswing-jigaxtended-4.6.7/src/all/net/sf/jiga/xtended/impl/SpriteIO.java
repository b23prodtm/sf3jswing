/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl;

import com.sun.media.imageio.plugins.tiff.BaselineTIFFTagSet;
import com.sun.media.imageio.plugins.tiff.EXIFTIFFTagSet;
import com.sun.media.imageio.plugins.tiff.TIFFDirectory;
import com.sun.media.imageio.plugins.tiff.TIFFField;
import com.sun.media.imageio.plugins.tiff.TIFFImageReadParam;
import com.sun.media.imageio.plugins.tiff.TIFFTag;
import com.sun.media.imageio.plugins.tiff.TIFFTagSet;
import com.sun.media.imageioimpl.plugins.tiff.TIFFImageReader;
import com.sun.media.imageioimpl.plugins.tiff.TIFFImageWriter;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.imageio.IIOImage;
import javax.imageio.IIOParamController;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.event.IIOReadProgressListener;
import javax.imageio.event.IIOReadWarningListener;
import javax.imageio.event.IIOWriteProgressListener;
import javax.imageio.event.IIOWriteWarningListener;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.FileCacheImageInputStream;
import javax.imageio.stream.FileCacheImageOutputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.PerspectiveTransform;
import javax.media.jai.RenderedOp;
import javax.media.jai.WarpPerspective;
import net.sf.jiga.xtended.JXAException;
import net.sf.jiga.xtended.impl.game.gl.GLFX;
import net.sf.jiga.xtended.impl.system.BufferIO;
import net.sf.jiga.xtended.impl.system.ImagePool;
import net.sf.jiga.xtended.impl.system.JXAenvUtils2;
import net.sf.jiga.xtended.impl.system.RefPool;
import net.sf.jiga.xtended.impl.system.SfMediaTracker;
import net.sf.jiga.xtended.impl.system.ThreadBlock;
import net.sf.jiga.xtended.impl.system.ThreadBlockData;
import net.sf.jiga.xtended.kernel.DebugMap;
import net.sf.jiga.xtended.kernel.Debugger;
import net.sf.jiga.xtended.kernel.ExtensionsClassLoader;
import net.sf.jiga.xtended.kernel.FileHelper;
import net.sf.jiga.xtended.kernel.JXAenvUtils;
import net.sf.jiga.xtended.ui.ImageCollection;

/**
 *
 * @author www.b23prodtm.info
 */
public class SpriteIO implements Debugger {

    private final static long hash = System.nanoTime();
    static Map<String, IIOParamController> IIOControllers = Collections.synchronizedMap(new HashMap<String, IIOParamController>());
    /**
     * RGB - no headroom/no footroom
     */
    static final float[] referenceBlackWhiteRGB_NOHead_NOFoot = new float[]{0, 255, 0, 255, 0, 255};
    /**
     * returns the current IIOMetadata instance. it is NOT stored until the
     * Sprite instance is serialized or one of the
     * {@link #writeImageToIOStream(OutputStream) writeImage...()} is called.
     *
     * @return
     *
     * public IIOMetadata getMetadata() { return metadata; }
     */
    /**
     * sets up the current IIOMetadata instance. it is NOT stored until the
     * Sprite instance is serialized or one of the
     * {@link #writeImageToIOStream(OutputStream) writeImage...()} is called.
     *
     * @param metadata the specified IIOMetadata to associate, if possible
     *
     * public void setMetadata(IIOMetadata metadata) { this.metadata = metadata;
     * }
     */
    /**
     * its recommended to use power-of-two sizes
     */
    public static Dimension _WRITE_TILES_DIMENSION = BufferIO._WRITE_TILES_DIMENSION;
    /**
     * YCbCr - CCIR Recommendation 601.1 headroom/footroom
     */
    static final float[] referenceBlackWhiteYCbCr_601_1 = new float[]{15, 235, 128, 240, 128, 240};
    /**
     * this system property is read at first static time and defines the
     * TIFFImageWriter IIOMetadata
     * {@linkplain BaselineTIFFTagSet#TAG_REFERENCE_BLACK_WHITE ReferenceBlackWhite}
     * with no Head/Footroom if set to true (
     *
     * @see http://partners.adobe.com/public/developer/en/tiff/TIFF6.pdf#page=86
     * TIFF 6.0 spec document}
     * @default jxa.tiff.nohf=false
     */
    public static boolean _useTIFF_NOHead_NOFoot = Boolean.getBoolean("jxa.tiff.nohf");
    /**
     * YCbCr coefficients CCIR Recommandation 601.1
     */
    static final float[] ycbcrCoefficients_601_1 = new float[]{299.0F / 1000.0F, 587.0F / 1000.0F, 114.0F / 1000.0F};
    /**
     * * EXIF 2.2 output for all TIFF images. (EXPERIMENTAL)
     *
     * @default jxa.tiff.exif=false
     */
    public static boolean _useTIFF_EXIF = Boolean.getBoolean("jxa.tiff.exif");
    /**
     * RGB - CCIR Recommendation 601.1 headroom/footroom
     */
    static final float[] referenceBlackWhiteRGB_601_1 = new float[]{16, 235, 16, 235, 16, 235};
    /**
     * YCbCr coefficients CCIR Recommandation 709
     */
    static final float[] ycbcrCoefficients_709 = new float[]{2125.0F / 10000.0F, 7154.0F / 10000.0F, 721.0F / 10000.0F};
    /**
     * * Unforced TIFF RGB PhotometricInterpretation (slower and buggy if
     * enabled)
     *
     * @default jxa.tiff.norgb=false
     */
    public static boolean _useTIFF_UnforcedRGB = Boolean.getBoolean("jxa.tiff.norgb");
    /**
     * YCbCr - no headroom/no footroom
     */
    static final float[] referenceBlackWhiteYCbCr_NOHead_NOFoot = new float[]{0, 255, 128, 255, 128, 255};
    /**
     * minimal pixels size of thumbnails cannot be greater than
     * {@link #_WRITE_TILES_DIMENSION}
     */
    public static int _MIN_THUMBNAILS_SIZE = 16;

    /**
     * TIFFImageWriter will create and return a TIFFDirectory metadata (IFD)
     * {@link #prepareTileWrite(boolean)}
     *
     * @param w
     * @param pmetadata
     * @param iwp
     * @param its
     * @return
     * @discussion
     * {@link http://download.java.net/media/jai-imageio/javadoc/1.1/com/sun/media/imageio/plugins/tiff/package-summary.html#EXIFStructure tiff plugin}
     */
    public static IIOMetadata _getIIOMforImageType(ImageWriter w, IIOMetadata pmetadata, ImageTypeSpecifier its, ImageWriteParam iwp) {
        IIOMetadata metadata = w.getDefaultImageMetadata(its, iwp);
        if (pmetadata != null && metadata != null) {
            if (metadata.getClass().isInstance(pmetadata)) {
                metadata = w.convertImageMetadata(pmetadata, its, iwp);
            }
        }
        if (w instanceof TIFFImageWriter) {
            TIFFTag ttagRefBW = BaselineTIFFTagSet.getInstance().getTag(BaselineTIFFTagSet.TAG_REFERENCE_BLACK_WHITE);
            TIFFTag ttagPhoto = BaselineTIFFTagSet.getInstance().getTag(BaselineTIFFTagSet.TAG_PHOTOMETRIC_INTERPRETATION);
            TIFFTag ttagCoeff = BaselineTIFFTagSet.getInstance().getTag(BaselineTIFFTagSet.TAG_Y_CB_CR_COEFFICIENTS);
            TIFFDirectory tifd;
            try {
                tifd = metadata == null ? new TIFFDirectory(new TIFFTagSet[]{BaselineTIFFTagSet.getInstance()}, null) : TIFFDirectory.createFromMetadata(metadata);
                if (!_useTIFF_UnforcedRGB) {
                    tifd.removeTIFFField(ttagPhoto.getNumber());
                    tifd.addTIFFField(new TIFFField(ttagPhoto, BaselineTIFFTagSet.PHOTOMETRIC_INTERPRETATION_RGB));
                }
                if (tifd.containsTIFFField(ttagPhoto.getNumber())) {
                    int photometric = tifd.getTIFFField(ttagPhoto.getNumber()).getAsInt(0);
                    float[] referenceBlackWhite;
                    float[] ycbcrCoefficients = null;
                    switch (photometric) {
                        case BaselineTIFFTagSet.PHOTOMETRIC_INTERPRETATION_RGB:
                            referenceBlackWhite = _useTIFF_NOHead_NOFoot ? referenceBlackWhiteRGB_NOHead_NOFoot : referenceBlackWhiteRGB_601_1;
                            break;
                        case BaselineTIFFTagSet.PHOTOMETRIC_INTERPRETATION_Y_CB_CR:
                            referenceBlackWhite = _useTIFF_NOHead_NOFoot ? referenceBlackWhiteYCbCr_NOHead_NOFoot : referenceBlackWhiteYCbCr_601_1;
                            ycbcrCoefficients = _useTIFF_NOHead_NOFoot ? ycbcrCoefficients_709 : ycbcrCoefficients_601_1;
                            break;
                        default:
                            referenceBlackWhite = null;
                            break;
                    }
                    if (referenceBlackWhite != null) {
                        tifd.removeTIFFField(ttagRefBW.getNumber());
                        tifd.addTIFFField(new TIFFField(ttagRefBW, TIFFTag.TIFF_FLOAT, 6, referenceBlackWhite));
                        if (ycbcrCoefficients != null) {
                            tifd.removeTIFFField(ttagCoeff.getNumber());
                            tifd.addTIFFField(new TIFFField(ttagCoeff, TIFFTag.TIFF_FLOAT, 3, ycbcrCoefficients));
                        }
                        if (DebugMap._getInstance().isDebuggerEnabled(Sprite.class) && DebugMap._getInstance().isDebugLevelEnabled(DebugMap._getInstance()._VOID)) {
                            System.out.print("TIFFField ReferenceBlackWhite ");
                            for (float f : referenceBlackWhite) {
                                System.out.print(" " + f);
                            }
                            System.out.println();
                        }
                    }
                }
                if (_useTIFF_EXIF) {
                    TIFFTagSet exif = EXIFTIFFTagSet.getInstance();
                    tifd.addTagSet(exif);
                    tifd.addTIFFField(new TIFFField(exif.getTag(EXIFTIFFTagSet.TAG_EXIF_VERSION), TIFFTag.TIFF_UNDEFINED, 4, EXIFTIFFTagSet.EXIF_VERSION_2_2));
                }
                metadata = tifd.getAsMetadata();
            } catch (IIOInvalidTreeException ex) {
                if (DebugMap._getInstance().isDebuggerEnabled(Sprite.class)) {
                    ex.printStackTrace();
                }
            }
        }
        return metadata;
    }

    /**
     * adds an IIOController as defined in JAI/JIIO (this is currently not used)
     *
     * @param mime the mime-type to control
     * @param iioc the parameters controller to use
     */
    public static void _addIIOController(String mime, IIOParamController iioc) {
        IIOControllers.put(mime, iioc);
    }

    /**
     * returns the current associated IIOController for the specified mime-type
     * (not used)
     *
     * @param mime the specified mime-type
     * @return the current associated IIOController for the specified mime-type
     */
    public static IIOParamController _getIIOController(String mime) {
        return IIOControllers.get(mime);
    }

    /**
     * {@link #prepareTileWrite(boolean)}
     *
     * @param type
     * @return
     */
    public static ImageTypeSpecifier _getITS(int type) {
        return ImageTypeSpecifier.createFromBufferedImageType(type);
    }

    /**
     * {@link #prepareTileWrite(boolean)}
     *
     * @param data
     * @return
     */
    public static ImageTypeSpecifier _getITS(RenderedImage data) {
        return ImageTypeSpecifier.createFromRenderedImage(data);
    }

    /**
     * {@link #prepareTileWrite(boolean)}
     *
     * @param w
     * @param pmetadata may be null and the default data is returned, otherwise
     * it is converted
     * @param iwp
     * @return ImageWriters that don't make use of stream metadata return null
     * (single-image mime-types file format).
     */
    public static IIOMetadata _getIIOMforStream(ImageWriter w, IIOMetadata pmetadata, ImageWriteParam iwp) {
        IIOMetadata metadata = w.getDefaultStreamMetadata(iwp);
        if (pmetadata != null && metadata != null) {
            if (metadata.getClass().isInstance(pmetadata)) {
                metadata = w.convertStreamMetadata(pmetadata, iwp);
            }
        }
        return metadata;
    }

    /**
     * Instances sp new BufferedImage from an image object.
     *
     * @param size dimensions
     * @param type the BufferedImage type to create
     * @return BufferedImage created
     * @see java.awt.image.BufferedImage
     */
    public static BufferedImage createBufferedImage(Dimension size, int type) {
        Dimension d = new Dimension(Math.max(1, Math.abs(size.width)), Math.max(1, Math.abs(size.height)));
        Image i = ImagePool.getInstance().findVacantImage(Sprite.BUFFERED, type, d);
        BufferedImage bdata = i == null ? new BufferedImage(d.width, d.height, type) : (BufferedImage) i;
        Graphics2D g = bdata.createGraphics();
        Color c = g.getColor();
        g.setColor(Color.WHITE);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_IN, 0.0F));
        g.fillRect(0, 0, d.width, d.height);
        g.setComposite(AlphaComposite.SrcOver);
        g.setColor(c);
        g.dispose();
        return bdata;
    }
    transient boolean IIOcontrollerEnabled = false;

    /**
     * returns the hash code for this Sprite
     */
    @Override
    public int hashCode() {
        return (int) hash;
    }

    /**
     * checks for equality of hash codes
     *
     * @param o the object instance to compare against
     */
    @Override
    public boolean equals(Object o) {
        return o != null ? hash == o.hashCode() : false;
    }
    /**
     * the image file cache directory to use
     */
    protected static String image_dir = "cache" + File.separator + "JXAimageIO";
    /*
     * SFmedia tracker
     */
    public final static SfMediaTracker _mt = new SfMediaTracker();
    /*
     * media tracker (links to the {@link #_mt SFmediatracker})
     */
    public MediaTracker mt = _mt;

    /**
     * the set of IIOReadProgressListener's
     */
    protected transient Set<IIOReadProgressListener> rProgList = Collections.synchronizedSet(new HashSet<IIOReadProgressListener>());
    /**
     * the set of IIOWriteProgressListener's
     */
    protected transient Set<IIOWriteProgressListener> wTileProgList = Collections.synchronizedSet(new HashSet<IIOWriteProgressListener>());
    /**
     * the set of IIOReadWarningListener's
     */
    protected transient Set<IIOReadWarningListener> rWarnList = Collections.synchronizedSet(new HashSet<IIOReadWarningListener>());
    /**
     * rendering hint protected transient RenderingHints rendering = new
     * RenderingHints(null);
     */
    /**
     *
     */
    protected int trackerPty = (int) hash;
    /**
     * the set of IIOWriteProgressListener's
     */
    protected transient Set<IIOWriteProgressListener> wProgList = Collections.synchronizedSet(new HashSet<IIOWriteProgressListener>());
    /**
     * the set of IIOReadProgressListener's
     */
    protected transient Set<IIOReadProgressListener> rTileProgList = Collections.synchronizedSet(new HashSet<IIOReadProgressListener>());
    /**
     * ImageObserver to avoid Garbage flushing protected Component obs;
     */
    protected transient Component obs = null;
    /**
     * the set of IIOWriteWarningListener's
     */
    protected transient Set<IIOWriteWarningListener> wWarnList = Collections.synchronizedSet(new HashSet<IIOWriteWarningListener>());

    public SpriteIO() {
    }

    protected static File _getIIOTmpDir() {
        File tmpDir;
        if (!(tmpDir = new File(FileHelper._TMPDIRECTORY.getPath() + File.separator + image_dir)).isDirectory()) {
            tmpDir.mkdirs();
            FileHelper._makeWritable(tmpDir);
        }
        return tmpDir;
    }
    /**
     * ExtensionsClassloader enforces loading Sprite resource when in resource
     * mode (a classloader issue)
     *
     * @default false.
     */
    public final static String JXA_ECLRES = "jxa.ecl.res";

    protected static boolean isExtClassLoaderRes() {
        return Boolean.getBoolean(JXA_ECLRES);
    }

    /**
     * use {@link JXAenvUtils#_BIGBUFFER_SIZE} large bufferedinput/outputstreams
     *
     * @default false
     */
    public static boolean bigBuffer = false;

    /**
     * returns sp compatible JIIO reader for the specified mime-type
     *
     * @param n the index of the reader if you want sp specific version of the
     * reader (user's savvy), default 0
     * @param mime the mime-type to look for
     * @return the ImageReader that fits the mime-type
     */
    protected static ImageReader _getReader(int n, String mime) {
        ImageReader r = null;
        IIOReadProgressAdapter rpl = new IIOReadProgressAdapter(DebugMap._getInstance().isDebuggerEnabled(Sprite.class) && DebugMap._getInstance().isDebugLevelEnabled(DebugMap._getInstance()._VOID));
        IIOReadWarningAdapter rwl = new IIOReadWarningAdapter(DebugMap._getInstance().isDebuggerEnabled(Sprite.class));
        int i = 0;
        for (Iterator readers = ImageIO.getImageReadersByMIMEType(mime); readers.hasNext(); i++) {
            r = (ImageReader) readers.next();
            if (i != n) {
                continue;
            }
            r.addIIOReadProgressListener(rpl);
            r.addIIOReadWarningListener(rwl);
            if (i == n) {
                return r;
            }
            i++;
        }
        return null;
    }

    /**
     * retrieves an available ImageReader instance for this Sprite
     *
     * @param n reader index in ImageIO
     * @return ImageReader instance
     * @see #mime
     */
    protected ImageReader getReader(int n, String mime) {
        ImageReader r = _getReader(n, mime);
        if (r == null) {
            return null;
        }
        synchronized (rProgList) {
            for (Iterator<IIOReadProgressListener> it = rProgList.iterator(); it.hasNext();) {
                r.addIIOReadProgressListener(it.next());
            }
        }
        synchronized (rWarnList) {
            for (Iterator<IIOReadWarningListener> it = rWarnList.iterator(); it.hasNext();) {
                r.addIIOReadWarningListener(it.next());
            }
        }
        return r;
    }

    /**
     * returns sp specific ImageWriter for the specified mime-type
     *
     * @param n index of the ImageWriter to use (user's savvy)
     * @param mime the mime-type to look for (e.g. image/jpeg)
     * @return the requested ImageWriter
     */
    protected static ImageWriter _getWriter(int n, String mime) {
        ImageWriter w = null;
        IIOWriteProgressAdapter wpl = new IIOWriteProgressAdapter(DebugMap._getInstance().isDebuggerEnabled(Sprite.class) && DebugMap._getInstance().isDebugLevelEnabled(DebugMap._getInstance()._VOID));
        IIOWriteWarningAdapter wwl = new IIOWriteWarningAdapter(DebugMap._getInstance().isDebuggerEnabled(Sprite.class));
        int i = 0;
        for (Iterator writers = ImageIO.getImageWritersByMIMEType(mime); writers.hasNext();) {
            w = (ImageWriter) writers.next();
            if (i != n) {
                continue;
            }
            w.addIIOWriteProgressListener(wpl);
            w.addIIOWriteWarningListener(wwl);
            if (i == n) {
                return w;
            }
            i++;
        }
        return null;
    }

    /**
     * retrieves an available ImageWriter instance for this Sprite
     *
     * @param n writer index in ImageIO
     * @return ImageWriter instance
     * @see #mime
     */
    protected ImageWriter getWriter(int n, String mime) {
        ImageWriter w = _getWriter(n, mime);
        if (w == null) {
            return w;
        }
        synchronized (wProgList) {
            for (Iterator<IIOWriteProgressListener> it = wProgList.iterator(); it.hasNext();) {
                w.addIIOWriteProgressListener(it.next());
            }
        }
        synchronized (wWarnList) {
            for (Iterator<IIOWriteWarningListener> it = wWarnList.iterator(); it.hasNext();) {
                w.addIIOWriteWarningListener(it.next());
            }
        }
        return w;
    }
    /**
     * String for the no source available state
     */
    protected static final String NO_SOURCE = "Offscreen Image";

    /**
     * suppress leading "/" for compliance with the classloader.getResource()
     */
    protected static String _contextResource(String src) {
        while (src.startsWith("/") && src.length() > 1) {
            src = src.substring(1);
        }
        return src;
    }

    protected static ImageReader __getInput(Object src, boolean innerResource, ImageReader r, boolean useCache) throws IOException, IllegalArgumentException {
        RandomAccessFile raf = null;
        InputStream is = null;
        File tmpDir;
        if (!(tmpDir = new File(FileHelper._TMPDIRECTORY.getPath() + File.separator + image_dir)).isDirectory()) {
            tmpDir.mkdirs();
            FileHelper._makeWritable(tmpDir);
        }
        if (src == null) {
            throw new JXAException("src is null !");
        } else {
            if (DebugMap._getInstance().isDebuggerEnabled(Sprite.class) && DebugMap._getInstance().isDebugLevelEnabled(DebugMap._getInstance()._VOID)) {
                System.out.println("New IIO Input " + (useCache ? "(may be cached)" : "(direct)") + ": " + src.getClass().getName() + " " + r.getFormatName());
            }
        }
        if (!innerResource && (src instanceof String)) {
            File inputFile = new File((String) src);
            if (!inputFile.isFile()) {
                throw new JXAException(src + " cannot be read");
            }
            raf = new RandomAccessFile(inputFile, "r");
            is = JXAenvUtils2.__getBuffered(new FileInputStream(raf.getFD()), bigBuffer);
            if (useCache) {
                r.setInput(new FileCacheImageInputStream(is, tmpDir));
            } else {
                r.setInput(ImageIO.createImageInputStream(is));
            }
        } else if (src instanceof ImageInputStream) {
            r.setInput(src);
        } else if (src instanceof InputStream) {
            is = JXAenvUtils2.__getBuffered((InputStream) src, bigBuffer);
            if (useCache) {
                r.setInput(new FileCacheImageInputStream(is, tmpDir));
            } else {
                r.setInput(ImageIO.createImageInputStream(is));
            }
        } else if (src instanceof String) {
            if (!src.equals(NO_SOURCE)) {
                if (!isExtClassLoaderRes()) {
                    is = JXAenvUtils2.__getBuffered(Sprite.class.getResourceAsStream((String) src), bigBuffer);
                } else {
                    is = JXAenvUtils2.__getBuffered(ExtensionsClassLoader.getInstance().getClassLoader().getResourceAsStream(_contextResource((String) src)), bigBuffer); /*
                     * Thread.currentThread().getContextClassLoader().getResourceAsStream(_contextResource((String)
                     * src));
                     */

                }
                if (useCache) {
                    r.setInput(new FileCacheImageInputStream(is, tmpDir));
                } else {
                    r.setInput(ImageIO.createImageInputStream(is));
                }
            } else {
                throw new JXAException("reading is not supported from " + src);
            }
        } else if (src instanceof File) {
            raf = new RandomAccessFile((File) src, "r");
            is = JXAenvUtils2.__getBuffered(new FileInputStream(raf.getFD()), bigBuffer);
            if (useCache) {
                r.setInput(new FileCacheImageInputStream(is, tmpDir));
            } else {
                r.setInput(ImageIO.createImageInputStream(is));
            }
        } else {
            throw new JXAException(src.getClass() + " input is not supported");
        }
        RefPool.getInstance().queueRef(is, false);
        return r;
    }

    private static ImageWriter __getOutput(Object dst, ImageWriter w, boolean useCache) throws IOException {
        RandomAccessFile raf = null;
        OutputStream os = null;
        File tmpDir = _getIIOTmpDir();
        if (dst == null) {
            throw new JXAException("dst is not supported : null");
        } else {
            if (DebugMap._getInstance().isDebuggerEnabled(Sprite.class) && DebugMap._getInstance().isDebugLevelEnabled(DebugMap._getInstance()._VOID)) {
                System.out.println("New IIO Output " + (useCache ? "(may be cached)" : "(direct)") + ": " + dst.getClass().getName() + " " + w.getOriginatingProvider().getFormatNames()[0]);
            }
        }
        if (dst instanceof String) {
            File outputFile = new File((String) dst);
            raf = new RandomAccessFile(outputFile, "rw");
            os = JXAenvUtils2.__getBuffered(new FileOutputStream(raf.getFD()), bigBuffer);
            if (useCache) {
                w.setOutput(new FileCacheImageOutputStream(os, tmpDir));
            } else {
                w.setOutput(ImageIO.createImageOutputStream(os));
            }
        } else if (dst instanceof OutputStream) {
            os = JXAenvUtils2.__getBuffered((OutputStream) dst, bigBuffer);
            if (useCache) {
                w.setOutput(new FileCacheImageOutputStream(os, tmpDir));
            } else {
                w.setOutput(ImageIO.createImageOutputStream(os));
            }
        } else if (dst instanceof File) {
            raf = new RandomAccessFile((File) dst, "rw");
            os = JXAenvUtils2.__getBuffered(new FileOutputStream(raf.getFD()), bigBuffer);
            if (useCache) {
                w.setOutput(new FileCacheImageOutputStream(os, tmpDir));
            } else {
                w.setOutput(ImageIO.createImageOutputStream(os));
            }
        } else if (dst instanceof ImageOutputStream) {
            w.setOutput(dst);
        } else {
            throw new JXAException(dst.getClass() + " output is not supported");
        }
        RefPool.getInstance().queueRef(os, false);
        return w;
    }

    /**
     * DOES NOT WORK WITH IMAGEPRODUCER AS {@linkplain #getSource() SOURCE}
     */
    protected Dimension getNumTilesXY(ImageReader r) {
        Dimension tiles = _WRITE_TILES_DIMENSION;
        try {
            Dimension tiledImage = new Dimension(r.getWidth(0), r.getHeight(0));
            final int tilesX = (int) Math.ceil((float) tiledImage.width / (float) tiles.width);
            final int tilesY = (int) Math.ceil((float) tiledImage.height / (float) tiles.height);
            if (tilesX <= 0 || tilesY <= 0) {
                if (isDebugEnabled() && DebugMap._getInstance().isDebugLevelEnabled(DebugMap._getInstance()._VOID)) {
                    System.err.println("Tiles are 1 to 1 " + tiledImage + " = " + tiles);
                }
                return new Dimension(1, 1);
            }
            return new Dimension(tilesX, tilesY);
        } catch (IOException ex) {
            if (isDebugEnabled()) {
                ex.printStackTrace();
            }
            return new Dimension(1, 1);
        }
    }

    /**
     * @param output must be a compatible output stream or file.
     * @return a bitwise combination of the _WRITE_SEQUENCE, _WRITE_INSERT,
     * _WRITE_REPLACE
     * @see #endWrite(javax.imageio.ImageWriter, boolean)
     * @throws JXAException if the output is not compatible.
     */
    public int prepareWrite(Dimension outputSize, ImageWriter w, ImageTypeSpecifier its, ImageWriteParam iwp, boolean useCache, Object output, Image data, int _type) throws IOException, InterruptedException {
        return _prepareWrite(outputSize, w, its, iwp, useCache, output, data, _type);
    }

    /**
     * Writer capabilities Flags.
     */
    public final static int _WRITE_SEQUENCE = 0x1, _WRITE_INSERT = 0x2, _WRITE_REPLACE = 0x4;

    /**
     * @param readerInput must be filled with an input already
     * @return a bitwise combination of the {@link #_WRITE_SEQUENCE}, {@link _WRITE_INSERT},
     * {@link #_WRITE_REPLACE} that is available (prepareWriteSequence is
     * already called in this case) or 0 if none functionality is available.
     * @see #endWrite(javax.imageio.ImageWriter, boolean)
     * @throws JXAException if the output is not compatible.
     */
    public int prepareWrite(Dimension outputSize, ImageWriter w, ImageTypeSpecifier its, ImageWriteParam iwp, boolean useCache, Object output, ImageReader readerInput, int _type) throws IOException, InterruptedException {
        return _prepareWrite(outputSize, w, its, iwp, useCache, output, readerInput, _type);
    }

    private transient ThreadBlockData<Integer> writeBlock = new ThreadBlockData<Integer>(0);

    private int _prepareWrite(Dimension outputSize, ImageWriter w, ImageTypeSpecifier its, ImageWriteParam iwp, boolean useCache, Object output, Object dataSource, int _type) throws IOException, InterruptedException {
        int ret = 0;
        writeBlock.begin();
        w = __getOutput(output, w, useCache);
        IIOMetadata metadata = dataSource instanceof ImageReader ? ((ImageReader) dataSource).getImageMetadata(0) : null;
        metadata = metadata instanceof IIOMetadata ? _getIIOMforImageType(w, metadata, its, iwp) : _getIIOMforDefaultImage(w, its, iwp);
        IIOMetadata streamMetadata = dataSource instanceof ImageReader ? ((ImageReader) dataSource).getStreamMetadata() : null;
        if (w.canWriteSequence()) {
            ret |= _WRITE_SEQUENCE;
            notifyIIOWriteListenersProgress(wTileProgList, w, 0);
            w.prepareWriteSequence(_getIIOMforStream(w, streamMetadata, iwp));
            if (w.canInsertEmpty(-1)) {
                ret |= _WRITE_INSERT;
                w.prepareInsertEmpty(-1, its, outputSize.width, outputSize.height, metadata,
                        null, /*
                         * thumbnail (has to be appended after IFD)
                         */
                        iwp);
                w.endInsertEmpty();
                ret |= w.canReplacePixels(0) ? _WRITE_REPLACE : 0;
                if (_useTIFF_EXIF && dataSource instanceof ImageReader) {
                    List<BufferedImage> thumbnails = getThumbnails(((ImageReader) dataSource), _MIN_THUMBNAILS_SIZE, _type);
                    if (!thumbnails.isEmpty()) {
                        w.writeToSequence(new IIOImage(thumbnails.get(0), null, _getIIOMforDefaultImage(w, _getITS(thumbnails.get(0)), iwp)), iwp);
                    }
                }
            }
        }
        /**
         * register in Threadblock the data of the writer flags
         */
        writeBlock.setTdata(ret);
        return ret;
    }

    /**
     * ends upon a write block started with {@link #prepareWrite(java.awt.Dimension, javax.imageio.ImageWriter, javax.imageio.ImageTypeSpecifier, javax.imageio.ImageWriteParam, boolean, java.lang.Object, java.awt.Image)
     * } or {@link #prepareWrite(java.awt.Dimension, javax.imageio.ImageWriter, javax.imageio.ImageTypeSpecifier, javax.imageio.ImageWriteParam, boolean, java.lang.Object, javax.imageio.ImageReader)
     * <br><b>Closes any open stream specified as output after the write
     * finishes.</b>
     */
    public void endWrite(ImageWriter w) throws IOException {
        if (!writeBlock.isOn()) {
            throw new JXAException(JXAException.LEVEL.SYSTEM, "not in write code block, use prepareWrite()-or-prepareTileWrite() before");
        }
        if (w.canWriteSequence() && (writeBlock.getTdata() & _WRITE_SEQUENCE) != 0) {
            w.endWriteSequence();
        }
        writeBlock.removeTdata();
        notifyIIOWriteListenersComplete(
                wTileProgList, w);
        ((ImageOutputStream) w.getOutput()).flush();
        ((ImageOutputStream) w.getOutput()).close();
        w.dispose();
        writeBlock.end();
    }

    /**
     * write to ImageWrite output from a RenderedImage input
     *
     * @param imgMetadata image metadata that can be obtained from the
     * ImageReader or with
     * {@link #_getIIOMforImageType(javax.imageio.ImageWriter, javax.imageio.metadata.IIOMetadata, javax.imageio.ImageTypeSpecifier, javax.imageio.ImageWriteParam) IIOMforImage..()}.
     * @param strMetadata stream metadata, that can be obtained from the
     * ImageReader or let it null. Default IIOM. is used in that case.
     */
    public void writeToOutput(IIOMetadata imgMetadata, IIOMetadata strMetadata, ImageWriter w, ImageWriteParam iwp, RenderedImage ndata) throws IOException, InterruptedException {
        if (!writeBlock.isOn()) {
            throw new JXAException(JXAException.LEVEL.SYSTEM, "not in write code block, use prepareWrite() before");
        }
        /**
         * untiled picture
         */
        IIOImage iioimage = new IIOImage((RenderedImage) ndata, null, imgMetadata);
        if (w.canWriteSequence()) {
            w.writeToSequence(iioimage, iwp);
        } else {
            w.write(_getIIOMforStream(w, strMetadata, iwp), iioimage, iwp);
        }
    }

    /**
     * write to ImageWrite output from ImageReader input, the output may be a
     * sequence of image and thus will append the image that is read.
     * <br> write to the output untiled
     *
     * @param isize input and output size (unmutable size)
     */
    public void writeToOutput(Dimension isize, ImageWriter w, ImageWriteParam iwp, ImageReader r, int _type, Object baseTransform) throws IOException, InterruptedException {
        writeToOutput(isize, w, iwp, r, _type, 0, baseTransform);
    }

    /**
     * write to ImageWrite output from ImageReader input, the output may be a
     * sequence of image and thus will append the image that is read at the
     * specified index.
     * <br> write (append if a sequence is possible) to the output untiled
     *
     * @param isize input and output size (unmutable size)
     * @param imageIndex index of the image to read in the input stream
     */
    public void writeToOutput(Dimension isize, ImageWriter w, ImageWriteParam iwp, ImageReader r, int _type, int imageIndex, Object baseTransform) throws IOException, InterruptedException {
        if (!writeBlock.isOn()) {
            throw new JXAException(JXAException.LEVEL.SYSTEM, "not in write code block, use prepareWrite() before");
        }
        IIOMetadata metadata = r.getImageMetadata(0);
        metadata = metadata instanceof IIOMetadata ? _getIIOMforImageType(w, metadata, _getITS(_type), iwp) : _getIIOMforDefaultImage(w, _getITS(_type), iwp);
        RenderedImage dataDst;
        BufferedImage i;
        i = r.read(imageIndex, _getIRP(r, _type));
        trackImage(trackerPty, i);
        waitFor(trackerPty);
        untrackImage(trackerPty, i);
        if (baseTransform instanceof PerspectiveTransform) {
            ParameterBlock pb = new ParameterBlock();
            pb.addSource(i);
            /**
             * add scale to specified size for the current data size to handle
             * correct data transform see UP ^^
             */
            pb.add(new WarpPerspective((PerspectiveTransform) baseTransform));
            pb.add(Interpolation.getInstance(Interpolation.INTERP_NEAREST));
            RenderedOp warpOp = JAI.create("warp", pb);
            dataDst = (RenderedImage) warpOp.createInstance();
        } else {
            if (baseTransform == null) {
                baseTransform = new AffineTransform();
            }
            dataDst = createBufferedImage(isize, _type);
            Graphics dataG = Sprite._createImageGraphics((BufferedImage) dataDst);
            Graphics2D g = Sprite.wrapRendering(dataG);
            g.drawRenderedImage(i, (AffineTransform) baseTransform);
            g.dispose();
            ((BufferedImage) dataDst).flush();
        }
        i.flush();
        writeToOutput(metadata, r.getStreamMetadata(), w, iwp, dataDst);
    }

    /**
     * write to ImageWrite output from ImageReader input
     * <br> {@link #prepareWrite(java.awt.Dimension, javax.imageio.ImageWriter, javax.imageio.ImageTypeSpecifier, javax.imageio.ImageWriteParam, boolean, java.lang.Object, javax.imageio.ImageReader)
     * } must have returned true, for a sequence of image data is going to be
     * written to the output.
     *
     * @param isize input and output size (unmutable size)
     * @param tilesSize tile image width and height (may not be smaller that
     * {@link #_WRITE_TILES_DIMENSION})
     * @param w
     * @param iwp
     * @param r
     * @param baseTransform
     * @throws java.io.IOException
     * @throws java.lang.InterruptedException
     */
    public void writeToOutputTiled(Dimension isize, Dimension tilesSize, ImageWriter w, ImageWriteParam iwp, ImageReader r, int _type, Object baseTransform) throws IOException, InterruptedException {
        /**
         * it is a tiled picture
         */
        Dimension numTiles = getNumTilesXY(r);
        ImageReadParam irp = _getIRP(r, _type);
        /*
         * NEVER ! w.prepareReplacePixels(0, new
         * Rectangle(dim));
         */
        for (int y = 0; y < numTiles.height; y++) {
            for (int x = 0; x < numTiles.width; x++) {
                Rectangle sroi = new Rectangle(x * tilesSize.width, y * tilesSize.height, tilesSize.width, tilesSize.height).intersection(new Rectangle(isize));
                if (sroi.isEmpty()) {
                    continue;
                }
                irp.setSourceRegion(sroi);
                BufferedImage b = r.read(0, irp);
                if (b.getType() != _type) {
                    b = _convertToBuffered(b, _type, obs, true);
                }
                trackImage(trackerPty, b);
                waitFor(
                        trackerPty);
                untrackImage(trackerPty, b);
                Rectangle replace = new Rectangle(x * tilesSize.width, y * tilesSize.height, tilesSize.width, tilesSize.height).intersection(new Rectangle(isize));
                iwp.setDestinationOffset(replace.getLocation());
                /**
                 * it's actually the destination rectangle size
                 */
                iwp.setSourceRegion(new Rectangle(replace.getSize()));
                w.prepareReplacePixels(0, replace);
                w.replacePixels(b, iwp);
                w.endReplacePixels();
                b.flush();
                notifyIIOWriteListenersProgress(
                        wTileProgList, w, (float) (y * numTiles.width + x) / (float) (numTiles.width * numTiles.height));
            }
        }
    }

    static {
        DebugMap._getInstance().associateDebugLevel(SpriteIO.class, Sprite.DBUG_RENDER_LOW);
    }

    public boolean isDebugEnabled() {
        return DebugMap._getInstance().isDebuggerEnabled(SpriteIO.class);
    }

    public void setDebugEnabled(boolean b) {
        DebugMap._getInstance().setDebuggerEnabled(b, SpriteIO.class);
    }

    /**
     *
     * @param l
     * @param source
     * @param index
     */
    public static void notifyIIOWriteListenersStarted(Set<IIOWriteProgressListener> l, ImageWriter source, int index) {
        synchronized (l) {
            for (Iterator<IIOWriteProgressListener> it = l.iterator(); it.hasNext();) {
                it.next().imageStarted(source, index);
            }
        }
    }

    /**
     *
     * @param l
     * @param source
     * @param percentageDone
     */
    public static void notifyIIOWriteListenersProgress(Set<IIOWriteProgressListener> l, ImageWriter source, float percentageDone) {
        synchronized (l) {
            for (Iterator<IIOWriteProgressListener> it = l.iterator(); it.hasNext();) {
                it.next().imageProgress(source, percentageDone);
            }
        }
    }

    /**
     *
     * @param l
     * @param source
     */
    public static void notifyIIOWriteListenersComplete(Set<IIOWriteProgressListener> l, ImageWriter source) {
        synchronized (l) {
            for (Iterator<IIOWriteProgressListener> it = l.iterator(); it.hasNext();) {
                it.next().imageComplete(source);
            }
        }
    }

    /**
     *
     * @param l
     * @param source
     */
    public static void notifyIIOWriteListenersAborted(Set<IIOWriteProgressListener> l, ImageWriter source) {
        synchronized (l) {
            for (Iterator<IIOWriteProgressListener> it = l.iterator(); it.hasNext();) {
                it.next().writeAborted(source);
            }
        }
    }

    /**
     *
     * @param l
     * @param source
     * @param index
     */
    public static void notifyIIOReadListenersStarted(Set<IIOReadProgressListener> l, ImageReader source, int index) {
        synchronized (l) {
            for (Iterator<IIOReadProgressListener> it = l.iterator(); it.hasNext();) {
                it.next().imageStarted(source, index);
            }
        }
    }

    /**
     *
     * @param l
     * @param source
     * @param percentageDone
     */
    public static void notifyIIOReadListenersProgress(Set<IIOReadProgressListener> l, ImageReader source, float percentageDone) {
        synchronized (l) {
            for (Iterator<IIOReadProgressListener> it = l.iterator(); it.hasNext();) {
                it.next().imageProgress(source, percentageDone);
            }
        }
    }

    /**
     *
     * @param l
     * @param source
     */
    public static void notifyIIOReadListenersComplete(Set<IIOReadProgressListener> l, ImageReader source) {
        synchronized (l) {
            for (Iterator<IIOReadProgressListener> it = l.iterator(); it.hasNext();) {
                it.next().imageComplete(source);
            }
        }
    }

    /**
     *
     * @param l
     * @param source
     */
    public static void notifyIIOReadListenersAborted(Set<IIOReadProgressListener> l, ImageReader source) {
        synchronized (l) {
            for (Iterator<IIOReadProgressListener> it = l.iterator(); it.hasNext();) {
                it.next().readAborted(source);
            }
        }
    }

    /**
     * TIFFImageWriter will create and return a TIFFDirectory metadata (IFD)
     * {@link #prepareTileWrite(boolean)}
     *
     * @param w
     * @param its
     * @param iwp
     * @return
     * @discussion <a
     * href="http://download.java.net/media/jai-imageio/javadoc/1.1/com/sun/media/imageio/plugins/tiff/package-summary.html#EXIFStructure"/>tiff
     * plugin</a>
     */
    public static IIOMetadata _getIIOMforDefaultImage(ImageWriter w, ImageTypeSpecifier its, ImageWriteParam iwp) {
        return _getIIOMforImageType(w, null, its, iwp);
    }

    /**
     * @return default param and correct destinationtype to match bufferedType
     * @param bufferedType it will be attempted to match the specified
     * bufferedtype at reading, but it is not guarranteed that the resulting
     * BufferedImage has the specified type
     * @see ImageReader#getImageTypes(int)
     */
    protected static ImageReadParam _getIRP(ImageReader r, int bufferedType) {
        ImageReadParam irp = r.getDefaultReadParam();
        ImageTypeSpecifier itsDef = _getITS(bufferedType);
        if (r.getInput() != null) {
            try {
                irp.setDestinationType(r.getRawImageType(0));
                for (Iterator<ImageTypeSpecifier> it = r.getImageTypes(0); it.hasNext();) {
                    ImageTypeSpecifier its = it.next();
                    if (its.equals(itsDef)) {
                        irp.setDestinationType(its);
                        break;
                    }
                }
            } catch (Throwable ex) {
                if (DebugMap._getInstance().isDebuggerEnabled(Sprite.class)) {
                    ex.printStackTrace();
                }
            }
        }
        if (irp instanceof TIFFImageReadParam) {
            if (_useTIFF_EXIF) {
                ((TIFFImageReadParam) irp).addAllowedTagSet(EXIFTIFFTagSet.getInstance());
            }
            if (DebugMap._getInstance().isDebuggerEnabled(Sprite.class) && irp.getDestinationType() != null) {
                try {
                    System.out.println("imagereader will use " + ImageCollection.BUFFERED_TYPE.valueOf(irp.getDestinationType().getBufferedImageType()) + " for " + r.getFormatName());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return irp;
    }

        /**
         *
         * @param w
         * @param srcDim
         * @param mime
         * @param bufferedType
         * @param compressionQuality between 0 and 1, a negative value indicates that it's unchanged
         * @return
         */
        protected static ImageWriteParam _getIWP(ImageWriter w, Dimension srcDim, String mime, int bufferedType, float compressionQuality) {
        ImageWriteParam iwp = w.getDefaultWriteParam();
        boolean found = false;
        for (String m : w.getOriginatingProvider().getMIMETypes()) {
            if (m.equals(mime)) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new JXAException("incompatible mime type specified " + mime + " for writer " + w.toString());
        }
        iwp.setDestinationType(_getITS(bufferedType));
        if (iwp.canWriteTiles()) {
            /*
             * if (_WRITE_TILES_DIMENSION.width < srcDim.width ||
             * _WRITE_TILES_DIMENSION.height < srcDim.height) {
             */
            /*
             * iwp.setTilingMode(iwp.MODE_EXPLICIT);
             * iwp.setTiling(_WRITE_TILES_DIMENSION.width,
             * _WRITE_TILES_DIMENSION.height, 0, 0);
             */
            /*
             * } else { iwp.setTilingMode(iwp.MODE_DISABLED); }
             */
        }
        if (iwp.canWriteCompressed()) {
            String css = ImageCollection._getCompressionType(mime, bufferedType);
            try {
                if (css != null) {
                    if (compressionQuality >= 0f) {
                        iwp.setCompressionMode(iwp.MODE_EXPLICIT);
                        iwp.setCompressionType(css);
                        iwp.setCompressionQuality(compressionQuality);
                    } else {
                        iwp.setCompressionMode(iwp.MODE_COPY_FROM_METADATA);
                    }
                } else {
                    iwp.setCompressionMode(iwp.MODE_DISABLED);
                }
            } catch (RuntimeException ex) {
                if (DebugMap._getInstance().isDebuggerEnabled(Sprite.class)) {
                    ex.printStackTrace();
                }
                try {
                    iwp.setCompressionMode(iwp.MODE_DEFAULT);
                } catch (RuntimeException ex2) {
                    if (DebugMap._getInstance().isDebuggerEnabled(Sprite.class)) {
                        ex.printStackTrace();
                    }
                }
            }
        }

        if (DebugMap._getInstance().isDebuggerEnabled(Sprite.class)) {
            System.out.print("imagewriter will use " + ImageCollection.BUFFERED_TYPE.valueOf(iwp.getDestinationType().getBufferedImageType()) + " for " + w.getOriginatingProvider().getFormatNames()[0]);
            if (iwp.canWriteCompressed()) {
                switch (iwp.getCompressionMode()) {
                    case ImageWriteParam.MODE_EXPLICIT:
                        System.out.println(" w/ " + iwp.getLocalizedCompressionTypeName());
                        break;
                    case ImageWriteParam.MODE_DEFAULT:
                        System.out.println(" w/ default compression.");
                        break;
                    default:
                        System.out.println(" w/o compressing.");
                        break;
                }
            } else {
                System.out.println(" uncompressed.");
            }
        }
        return iwp;
    }

    /**
     * wait for the data to be loaded with the mediatracker
     *
     * @throws InterruptedException
     */
    protected void waitFor(Image data) throws InterruptedException {
        InterruptedException interrupted = null;
        if (data != null) {
            trackImage(trackerPty, data);
        }
        try {
            waitFor(trackerPty);
        } catch (InterruptedException ex) {
            interrupted = ex;
        } finally {
            if (data != null) {
                untrackImage(trackerPty, data);
            }
            if (interrupted != null) {
                throw interrupted;
            }
        }
    }

    /**
     * waits for the image data to load completely
     *
     * @param id the id of the data in the MediaTracker
     * @see #setTrackerPty(int)
     * @see MediaTracker#waitForId(int)
     */
    protected void waitFor(int id) throws InterruptedException {
        /*if (load) {*/
        while (!mt.checkID(id)) {
            mt.waitForID(id, 10);
        }
        if (!mt.isErrorID(id)) {
            if (isDebugEnabled() && DebugMap._getInstance().isDebugLevelEnabled(DebugMap._getInstance()._VOID)) {
                System.out.println(toString() + " is loaded, mt-id " + id + ".");
            }
        } else {
            if (isDebugEnabled() && DebugMap._getInstance().isDebugLevelEnabled(DebugMap._getInstance()._VOID)) {
                System.err.println(toString() + " has error(s), mt-id " + id + ".");
            }/*
             clearResource();
             }*/

        }
    }

    /**
     * Tracks the image for loading with the current MediaTracker.
     *
     * @param id the id to set up in the MediaTracker
     * @param dat the Image instance to add to the MediaTracker
     * @see #setTrackerPty(int)
     */
    protected void untrackImage(int id, Image data) {
        if (data != null) {
            mt.removeImage(data, id);
        }
    }

    /**
     * Tracks the image for loading with the current MediaTracker. (CAUTION :
     * don't forget to {@link #untrack(int, image)} after completion of loading,
     * or the image will rest in heap memory)
     *
     * @param id the id to set up in the MediaTracker
     * @param dat the Image instance to add to the MediaTracker
     * @see #setTrackerPty(int)
     */
    protected void trackImage(int id, Image data) {
        if (data != null) {
            mt.addImage(data, id);
            ImagePool.getInstance().queueImage(data, false);
            data.setAccelerationPriority(1.0F);
        } else if (isDebugEnabled() && DebugMap._getInstance().isDebugLevelEnabled(DebugMap._getInstance()._VOID)) {
            System.out.println(" no data to track !");
        }
        ImagePool.getInstance().cleanup();
    }

    protected BufferedImage _convertToBuffered(Object data, int _type, Component pobs, boolean flush) throws InterruptedException {
        this.obs = pobs;
        BufferedImage bdata = null;
        if (data == null) {
            if (isDebugEnabled()) {
                System.err.println("no data to convert " + toString());
            }
            return null;
        }
        if (data instanceof Image) {
            trackImage(trackerPty, (Image) data);
            waitFor(trackerPty);
            untrackImage(trackerPty, (Image) data);
        }
        Dimension dim = new Dimension();
        if (data instanceof RenderedImage) {
            dim = new Dimension(((RenderedImage) data).getWidth(), ((RenderedImage) data).getHeight());
        } else {
            dim = new Dimension(((Image) data).getWidth(obs), ((Image) data).getHeight(obs));
        }
        bdata = createBufferedImage(dim, _type);
        Graphics dataG = Sprite._createImageGraphics(bdata);
        Graphics2D g = Sprite.wrapRendering(dataG);
        if (data instanceof RenderedImage) {
            g.drawRenderedImage((RenderedImage) data, new AffineTransform());
        } else {
            g.drawImage((Image) data, new AffineTransform(), obs);
        }
        dataG.dispose();
        if (data instanceof Image) {
            if (flush) {
                ((Image) data).flush();
            }
        }
        return bdata;
    }

    /**
     * attempt to retrieve thumbnails, or create one with the reader data
     *
     * @param r
     * @param minSize min size for thumbnails, if it appears that the source
     * picture is smaller or equal to that size, no thumbnail should be
     * returned.
     * @param bufferedType target type
     * @return
     * @throws IOException
     */
    protected List<BufferedImage> getThumbnails(ImageReader r, int minSize, int bufferedType) throws IOException, InterruptedException {
        List<BufferedImage> thumbnails = Collections.synchronizedList(new ArrayList<BufferedImage>());
        /*
         * return thumbnails;
         */
        ImageReadParam irp = _getIRP(r, bufferedType);
        Dimension dim = new Dimension(r.getWidth(0), r.getHeight(0));
        Dimension thumbs = _WRITE_TILES_DIMENSION;
        while ((thumbs.width >= dim.width || thumbs.height >= dim.height) && thumbs.width > minSize && thumbs.height > minSize) {
            thumbs = new Dimension((int) Math.max(minSize, thumbs.width / 4.), (int) Math.max(minSize, thumbs.height / 4.));
        }
        thumbs.height = Math.max(1, Math.round(thumbs.width / r.getAspectRatio(0)));
        if (thumbs.equals(dim)) {
            return thumbnails;
        }
        BufferedImage im = null;
        if (r.hasThumbnails(0)) {
            for (int i = 0; i < r.getNumThumbnails(0); i++) {
                thumbnails.add(im = r.readThumbnail(0, i));
                mt.addImage(im, 0);
                mt.waitForID(0);
                mt.removeImage(im, 0);
            }
        } else if (r.getNumImages(true) > 1 && r instanceof TIFFImageReader) {
            thumbnails.add(im = r.read(1, irp));
            mt.addImage(im, 0);
            mt.waitForID(0);
            mt.removeImage(im, 0);
        } else {
            if (irp.canSetSourceRenderSize()) {
                irp.setSourceRenderSize(thumbs);
            } else {
                int x = Math.round((dim.width - thumbs.width) / 2f);
                int y = Math.round((dim.height - thumbs.height) / 2f);
                irp.setSourceRegion(new Rectangle(x, y, thumbs.width, thumbs.height));
            }
            thumbnails.add(im = r.read(0, irp));
            mt.addImage(im, 0);
            mt.waitForID(0);
            mt.removeImage(im, 0);
        }
        for (int i = 0; i < thumbnails.size(); i++) {
            BufferedImage b = thumbnails.get(i);
            if (b.getType() != bufferedType) {
                thumbnails.set(i, _convertToBuffered((RenderedImage) b, bufferedType, obs, true));
            }
        }
        return thumbnails;
    }

    /**
     * removes an IIOWriteProgressListener from this Sprite instance
     *
     * @param l the listener that will be removed from the listeners set
     */
    public void removeIIOTileWriteProgressListener(IIOWriteProgressListener l) {
        wTileProgList.remove(l);
    }

    /**
     * removes an IIOReadProgressListener from this Sprite instance
     *
     * @param l the listener instance that will be removed from the listeners
     * set
     */
    public void removeIIOTileReadProgressListener(IIOReadProgressListener l) {
        rTileProgList.remove(l);
    }

    /**
     * adds an IIOReadProgressListener to this Sprite instance
     *
     * @param l the listener instance that will listen to each read operation
     * progress
     */
    public void addIIOReadProgressListener(IIOReadProgressListener l) {
        rProgList.add(l);
    }

    /**
     * removes an IIOWriteProgressListener from this Sprite instance
     *
     * @param l the listener that will be removed from the listeners set
     */
    public void removeIIOWriteProgressListener(IIOWriteProgressListener l) {
        wProgList.remove(l);
    }

    /**
     * removes an IIOReadWarningListener from this Sprite instance
     *
     * @param l the listener instance that will be removed from the listeners
     * set
     */
    public void removeIIOReadWarningListener(IIOReadWarningListener l) {
        rWarnList.remove(l);
    }

    /**
     * removes an IIOWriteWarningListener from this Sprite instance
     *
     * @param l the listener that will be removed from the listeners set
     */
    public void removeIIOWriteWarningListener(IIOWriteWarningListener l) {
        wWarnList.remove(l);
    }

    /**
     * adds an IIOWriteWarningListener to this Sprite instance
     *
     * @param l the listener that will listen to each write oparation warning
     */
    public void addIIOWriteWarningListener(IIOWriteWarningListener l) {
        wWarnList.add(l);
    }

    /**
     * returns true or false, whether the IIOController is enabled or not, resp.
     * if available. (not used)
     *
     * @return true or false, whether the IIOController should be enabled or
     * not, resp.
     */
    public boolean isIIOcontrollerEnabled() {
        return IIOcontrollerEnabled;
    }

    /**
     * adds an IIOWriteProgressListener to this Sprite instance
     *
     * @param l the listener that will listen to each write operation progress
     * (tile mode muste be enabled and image/tiff must be the
     * {@link #getStoreMime() store mime type})
     */
    public void addIIOTileWriteProgressListener(IIOWriteProgressListener l) {
        wTileProgList.add(l);
    }

    /**
     * adds an IIOReadWarninListener to this Sprite instance
     *
     * @param l the listener instance that will listen to each read operation
     * warning
     */
    public void addIIOReadWarningListener(IIOReadWarningListener l) {
        rWarnList.add(l);
    }

    /**
     * removes an IIOReadProgressListener from this Sprite instance
     *
     * @param l the listener instance that will be removed from the listeners
     * set
     */
    public void removeIIOReadProgressListener(IIOReadProgressListener l) {
        rProgList.remove(l);
    }

    /**
     * adds an IIOReadProgressListener to this Sprite instance
     *
     * @param l the listener instance that will listen to each tile read
     * operation progress (tile mode must be enabled)
     */
    public void addIIOTileReadProgressListener(IIOReadProgressListener l) {
        rTileProgList.add(l);
    }

    /**
     * adds an IIOWriteProgressListener to this Sprite instance
     *
     * @param l the listener that will listen to each write operation progress
     */
    public void addIIOWriteProgressListener(IIOWriteProgressListener l) {
        wProgList.add(l);
    }

    /**
     * dis/enables the IIOController for this Sprite instance (this is currently
     * not used)
     *
     * @param IIOcontrollerEnabled dis/enables the IIOController
     */
    public void setIIOcontrollerEnabled(boolean IIOcontrollerEnabled) {
        this.IIOcontrollerEnabled = IIOcontrollerEnabled;
    }

}
