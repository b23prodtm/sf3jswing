/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.ui;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.event.IIOReadProgressListener;
import javax.imageio.event.IIOWriteProgressListener;
import javax.imageio.stream.ImageInputStream;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import net.sf.jiga.xtended.JXAException;
import net.sf.jiga.xtended.impl.Sprite;
import net.sf.jiga.xtended.impl.SpriteIO;
import net.sf.jiga.xtended.impl.game.gl.GLFX;
import net.sf.jiga.xtended.impl.system.SfMediaTracker;
import net.sf.jiga.xtended.kernel.*;

/**
 * ImageCollection is the most advanced implementation of the JXA caching system
 * for Swing. it's up to handle a collection of for an unlimited amount of
 * pictures, digital photography, etc. Heap memory is handled at it's top-most
 * level, using a SpritesCacheManager and the GarbageCollector, for a real-time
 * access to the collection. Loading images is done with a single call to
 * addImage() and then it can be requested the picture as a thumbnail or
 * full-size, as well as wrapped in a renderable JComponent. ImageIO extra
 * codecs are available with this class, as for the Sprite class. NEW : big
 * sized pictures may be tiled and file-cached. To define a different tile size,
 * change the static variable {@linkplain Sprite#_WRITE_TILES_DIMENSION}.
 * SERIALIZING THIS CLASS : if you plan to serialize this class (e.g. store in a
 * file), then you should be aware of the {@linkplain #getState() status} of the
 * instance you run.
 *
 * @author www.b23prodtm.info
 */
public class ImageCollection implements Externalizable, Resource, ImageCollectionRenderer {

        public final static ResourceBundle rb = ResourceBundle.getBundle("net.sf.jiga.xtended.impl.ImcBrowser");
        private final static Set<ImagePluginDescriptor> fileFilters = Collections.synchronizedSet(new HashSet<ImagePluginDescriptor>());
        private static boolean pluginsLoaded = false;

        public static Set<ImagePluginDescriptor> _getPluginsMap() {
                return pluginsLoaded ? fileFilters : _loadPluginsMap();
        }

        private static Set<ImagePluginDescriptor> _loadPluginsMap() {
                Enumeration<String> e = rb.getKeys();
                for (; e.hasMoreElements();) {
                        String key = e.nextElement();
                        String plugin = key.split("_")[0];
                        Properties p = new Properties();
                        p.setProperty(ImagePluginDescriptor.get.id.toString(), plugin);
                        synchronized (fileFilters) {
                                for (ImagePluginDescriptor ipd : fileFilters) {
                                        if (ipd.p.getProperty(ImagePluginDescriptor.get.id.toString()).equals(plugin)) {
                                                p = ipd.p;
                                                break;
                                        }
                                }
                        }
                        boolean add = true;
                        if (key.matches(".*_class_[rw]")) {
                                String mode = key.substring(key.length() - 1);
                                p.setProperty(ImagePluginDescriptor.get.valueOf("class_" + mode).toString(), rb.getString(key));
                        } else if (key.matches(".*_ext")) {
                                p.setProperty(ImagePluginDescriptor.get.extension.toString(), rb.getString(key));
                        } else if (key.matches("ImageReader\\d*")) {
                                p.setProperty(ImagePluginDescriptor.get.mime.toString(), rb.getString(key));
                        } else if (key.matches(".*_rw")) {
                                p.setProperty(ImagePluginDescriptor.get.support.toString(), rb.getString(key));
                        } else if (key.matches(".*_desc")) {
                                p.setProperty(ImagePluginDescriptor.get.description.toString(), rb.getString(key));
                        } else if (key.matches(".*_type")) {
                                p.setProperty(ImagePluginDescriptor.get.buffered_type.toString(), rb.getString(key));
                        } else if (key.matches(".*_css")) {
                                p.setProperty(ImagePluginDescriptor.get.compression_type.toString(), rb.getString(key));
                        } else {
                                add = false;
                        }
                        if (add) {
                                fileFilters.add(new ImagePluginDescriptor(p));
                        }
                }
                pluginsLoaded = true;
                return fileFilters;
        }

        public static Properties _getPluginProperties(String mime) {
                if (!pluginsLoaded) {
                        _loadPluginsMap();
                }
                synchronized (fileFilters) {
                        for (ImagePluginDescriptor ipd : fileFilters) {
                                Properties p = ipd.p;
                                if (mime.equals(p.getProperty(ImagePluginDescriptor.get.mime.toString()))) {
                                        return p;
                                }
                        }
                }
                return new Properties();

        }

        /**
         * @param fileMode "r" for read "w" for write or "rw" for both
         * @return mime type. if fileMode equals or contains "r* then an attempt
         * to open a stream is made and ImageIO detects the reader that is
         * appropriate. Otherwise , the file extension is checked up.
         */
        public static String _getImageMimeType(File f, String fileMode) {
                if (!pluginsLoaded) {
                        _loadPluginsMap();
                }
                String mime = null;
                /**
                 * check file extension first
                 */
                synchronized (fileFilters) {
                        for (ImagePluginDescriptor ipd : fileFilters) {
                                Properties p = ipd.p;
                                String[] exts = p.getProperty(ImagePluginDescriptor.get.extension.toString()).split(" ");
                                if (p.getProperty(ImagePluginDescriptor.get.support.toString()) != null) {
                                        if (p.getProperty(ImagePluginDescriptor.get.support.toString()).contains(fileMode)) {
                                                for (String ext : exts) {
                                                        if (f.getName().toLowerCase().endsWith("." + ext.toLowerCase())) {
                                                                mime = p.getProperty(ImagePluginDescriptor.get.mime.toString());
                                                                break;
                                                        }
                                                }
                                        }
                                }
                                if (mime != null) {
                                        break;
                                }
                        }
                }
                if (mime == null) {
                        /**
                         * try image header
                         */
                        try {
                                if (f.exists() && fileMode.contains("r")) {
                                        ImageInputStream iis = ImageIO.createImageInputStream(f);
                                        for (Iterator<ImageReader> itr = ImageIO.getImageReaders(iis); itr.hasNext() && mime == null;) {
                                                ImageReader r = itr.next();
                                                synchronized (fileFilters) {
                                                        for (ImagePluginDescriptor ipd : fileFilters) {
                                                                Properties p = ipd.p;
                                                                if (p.getProperty(ImagePluginDescriptor.get.support.toString()) != null) {
                                                                        if (p.getProperty(ImagePluginDescriptor.get.support.toString()).contains(fileMode)) {
                                                                                for (String m : r.getOriginatingProvider().getMIMETypes()) {
                                                                                        if (m.equals(p.getProperty(ImagePluginDescriptor.get.mime.toString()))) {
                                                                                                mime = m;
                                                                                                break;
                                                                                        }
                                                                                }
                                                                        }
                                                                }
                                                        }
                                                }
                                        }
                                        iis.flush();
                                        iis.close();
                                }
                        } catch (IOException ex) {
                                if (JXAenvUtils._debug) {
                                        ex.printStackTrace();
                                }
                        }
                }
                if (mime == null) {
                        mime = "UNKNOWN_MIME";
                }
                return mime;
        }

        /**
         * prints all supported storeMime-types on the System std output
         *
         * @see ImageIO#getReaderMIMETypes()
         * @see ImageIO#getUseCache()
         */
        public static void _printAllMimeTypes() {
                String sprint = "";
                if (ImageIO.getUseCache()) {
                        sprint = "ImageIO reader/writer will use cache directory " + ImageIO.getCacheDirectory() + net.sf.jiga.xtended.kernel.Console.newLine;
                } else {
                        sprint = "Warning ! ImageIO doesn't have any cache enabled. This may result in unexpected behaviours. (ImageIO.setUseCache())" + net.sf.jiga.xtended.kernel.Console.newLine;
                }
                String[] mimes = ImageIO.getReaderMIMETypes();
                for (int i = 0; i < mimes.length; i++) {
                        for (Iterator<ImageReader> it = ImageIO.getImageReadersByMIMEType(mimes[i]); it.hasNext();) {
                                ImageReader r = it.next();
                                sprint += "ImageReader " + mimes[i] + " : " + r.toString() + net.sf.jiga.xtended.kernel.Console.newLine;
                        }
                }
                mimes = ImageIO.getWriterMIMETypes();
                for (int i = 0; i < mimes.length; i++) {
                        String ctype = _getCompressionType(mimes[i], _getBufferedType(mimes[i]));
                        for (Iterator<ImageWriter> it = ImageIO.getImageWritersByMIMEType(mimes[i]); it.hasNext();) {
                                ImageWriter w = it.next();
                                boolean tiles = w.getDefaultWriteParam().canWriteTiles();
                                sprint += "ImageWriter " + mimes[i] + " : " + w.toString() + " tiling : " + tiles + net.sf.jiga.xtended.kernel.Console.newLine;
                                if (w.getDefaultWriteParam().canWriteCompressed()) {
                                        sprint += "ImageWriter " + mimes[i] + " compression types : " + net.sf.jiga.xtended.kernel.Console.newLine;
                                        String sep = "";
                                        for (String compress : w.getDefaultWriteParam().getCompressionTypes()) {
                                                String s = compress.equals(ctype) ? "*" + compress + "*" : compress;
                                                sprint += sep + s;
                                                sep = ", ";
                                        }
                                        sprint += net.sf.jiga.xtended.kernel.Console.newLine;
                                }
                        }
                }
                System.out.println(JXAenvUtils.log(sprint, JXAenvUtils.LVL.SYS_NOT));
        }

        /**
         * will return the selected compression type in
         * jxa.ImcBrowser.properties file for that storeMime type, if
         * bufferedType differs from the type value in the properties file, then
         * null is returned for safety.
         *
         * @return the compression type or null if none is found or it is
         * incompatible with the specified bufferedType or storeMime
         */
        public static String _getCompressionType(String outputMime, int bufferedType) {
                if (!pluginsLoaded) {
                        _loadPluginsMap();
                }
                Properties plugin = _getPluginProperties(outputMime);
                String cssType = plugin.getProperty(ImagePluginDescriptor.get.compression_type.toString());
                int bType = _getBufferedType(outputMime);
                /*if (bType != bufferedType) {
                 cssType = null;
                 }*/
                if (cssType != null) {
                        for (String t : ImageIO.getImageWritersByMIMEType(outputMime).next().getDefaultWriteParam().getCompressionTypes()) {
                                if (cssType.equals(t)) {
                                        return cssType;
                                }
                        }
                }
                if (DebugMap._getInstance().isDebugLevelEnabled(Sprite.DBUG_RENDER_LOW)) {
                        System.err.println(outputMime + " with no compression type selected, image buffer is " + BUFFERED_TYPE.valueOf(bufferedType));
                }
                return null;

        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
                out.writeObject(_collectionCache);
                out.writeObject(_tbCollectionCache);
                out.writeBoolean(buffered);
                out.writeLong(hash);
                out.writeBoolean(multiThreading);
                out.writeInt(state);
                out.writeInt(thumbnailSize.width);
                out.writeInt(thumbnailSize.height);
                out.flush();
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
                _collectionCache = (SpritesCacheManager<String, Map<String, Serializable>>) in.readObject();
                _tbCollectionCache = (SpritesCacheManager<String, Map<String, Serializable>>) in.readObject();
                buffered = in.readBoolean();
                hash = in.readLong();
                multiThreading = in.readBoolean();
                state = in.readInt();
                thumbnailSize.setSize(in.readInt(), in.readInt());
                endRead(in);
        }

        /**
         * Easy-to-use BufferedImage.TYPE_'s enum. it can convert from a
         * buffered type Integer to a constant that can be read by human.
         */
        public static enum BUFFERED_TYPE {

                DEFAULT(Sprite.DEFAULT_TYPE),
                TYPE_4BYTE_ABGR(BufferedImage.TYPE_4BYTE_ABGR),
                TYPE_4BYTE_ABGR_PRE(BufferedImage.TYPE_4BYTE_ABGR_PRE),
                TYPE_INT_ARGB(BufferedImage.TYPE_INT_ARGB),
                TYPE_INT_ARGB_PRE(BufferedImage.TYPE_INT_ARGB_PRE),
                CUSTOM(BufferedImage.TYPE_CUSTOM),
                TYPE_3BYTE_BGR(BufferedImage.TYPE_3BYTE_BGR),
                TYPE_INT_BGR(BufferedImage.TYPE_INT_BGR),
                TYPE_INT_RGB(BufferedImage.TYPE_INT_RGB),
                TYPE_USHORT_555_RGB(BufferedImage.TYPE_USHORT_555_RGB),
                TYPE_USHORT_565_RGB(BufferedImage.TYPE_USHORT_565_RGB),
                TYPE_BYTE_BINARY(BufferedImage.TYPE_BYTE_BINARY),
                TYPE_BYTE_INDEXED(BufferedImage.TYPE_BYTE_INDEXED),
                TYPE_BYTE_GRAY(BufferedImage.TYPE_BYTE_GRAY),
                TYPE_USHORT_GRAY(BufferedImage.TYPE_USHORT_GRAY);
                public final int type;

                BUFFERED_TYPE(int type) {
                        this.type = type;
                }

                public ImageTypeSpecifier getFeatures() {
                        return ImageTypeSpecifier.createFromBufferedImageType(type);
                }

                public static BUFFERED_TYPE valueOf(int bufferedType) {
                        for (BUFFERED_TYPE t : values()) {
                                if (t.type == bufferedType) {
                                        return t;
                                }
                        }
                        return CUSTOM;
                }
        }

        public static int _getBufferedType(String outputMime) {
                if (!pluginsLoaded) {
                        _loadPluginsMap();
                }
                ImageWriter w = ImageIO.getImageWritersByMIMEType(outputMime).next();
                Properties plugin = _getPluginProperties(outputMime);
                String n = plugin.getProperty(ImagePluginDescriptor.get.buffered_type.toString());
                BUFFERED_TYPE type = n == null ? BUFFERED_TYPE.DEFAULT : BUFFERED_TYPE.valueOf(n);
                while (!w.getOriginatingProvider().canEncodeImage(type.getFeatures())) {
                        if (JXAenvUtils._debug) {
                                new UIMessage(true, "Wrong type for this mime/type ! " + type.name() + " <> " + outputMime, null, UIMessage.ERROR_TYPE);
                                type = (BUFFERED_TYPE) UIMessage.showSelectDialog(null, "<html>Select image buffer-type for " + outputMime + ": <br><i>(to avoid this dialog, edit jxa/ImcBrowser.properties file)</i></html>", outputMime, type.values(), type);
                        } else {
                                System.err.println(JXAenvUtils.log("Selected type won't be written correctly by the " + outputMime + " ImageWriter", JXAenvUtils.LVL.SYS_WRN));
                                break;
                        }
                }
                return type.type;
        }
        long hash = System.nanoTime();
        private boolean buffered;

        @Override
        public int hashCode() {
                return (int) hash;
        }

        @Override
        public boolean equals(Object o) {
                return o == null ? false : o.hashCode() == hashCode();
        }
        /**
         *
         */
        SpritesCacheManager<String, Map<String, Serializable>> _collectionCache;
        /**
         *
         */
        SpritesCacheManager<String, Map<String, Serializable>> _tbCollectionCache;
        /**
         *
         */
        transient SortedMap<String, Map<String, Serializable>> collection;
        /**
         *
         */
        transient SortedMap<String, Map<String, Serializable>> tbCollection;
        /**
         *
         */
        transient MediaTracker mt;
        /**
         *
         */
        transient Component obs;

        /**
         * ImageObserver
         */
        public void setObs(Component obs) {
                mt = new SfMediaTracker(obs);
                this.obs = obs;
        }

        /**
         * ImageObserver
         */
        public Component getObs() {
                return obs;
        }

        /**
         * heap capacity is set to 1 living entry. No zip, no VolatileImage.
         * Caching.
         *
         * @see #ImageCollection(Component, int, boolean, boolean)
         */
        public ImageCollection() {
                this(new JLabel(), 1, false, false);
        }

        /**
         *
         * @param obs a Component that is used to make loading
         * (MediaTracker/ImageObserver issue)
         * @param initCapacity VERY important setting. Defines how the LRU_MRU
         * list will store living entries in the heap (generally 1 -> 10,
         * depending on the stored image sizes). Caching is always enabled.
         * @param zip ZipOutStream enable (EXPERIMENTAL)
         * @param hardwareAccel VolatileImage enable (EXPERIMENTAL)
         */
        public ImageCollection(Component obs, int initCapacity, boolean zip, boolean hardwareAccel) {
                this.buffered = hardwareAccel;
                collection = Collections.synchronizedSortedMap(_collectionCache = new SpritesCacheManager<String, Map<String, Serializable>>(initCapacity));
                tbCollection = Collections.synchronizedSortedMap(_tbCollectionCache = new SpritesCacheManager<String, Map<String, Serializable>>(initCapacity));
                _collectionCache.setSwapDiskEnabled(true);
                _collectionCache.setCompressionEnabled(zip);
                _tbCollectionCache.setSwapDiskEnabled(true);
                _tbCollectionCache.setCompressionEnabled(zip);
                mt = new SfMediaTracker(this.obs = obs);
                ThreadWorks.prioritizeThreadGroups(tbimageLoaders.workTG, imageLoaders.workTG);
        }
        public static SfMediaTracker _mt = SpriteIO._mt;

        private void endRead(ObjectInput in) throws IOException, ClassNotFoundException {
                collection = Collections.synchronizedSortedMap(_collectionCache);
                tbCollection = Collections.synchronizedSortedMap(_tbCollectionCache);
                /*        thumbnailComponentCollection = Collections.synchronizedSortedMap(new TreeMap<String, JComponent>());
                 componentCollection = Collections.synchronizedSortedMap(new TreeMap<String, JComponent>());*/
                synchronized (collection) {
                        for (Iterator<String> it = collection.keySet().iterator(); it.hasNext();) {
                                pendingPictures.put(it.next(), STATE_LOADED);
                        }
                }
                synchronized (tbCollection) {
                        for (Iterator<String> it = tbCollection.keySet().iterator(); it.hasNext();) {
                                String k = it.next();
                                int s = pendingPictures.get(k);
                                pendingPictures.put(k, s | STATE_THUMB_LOADED);
                        }
                }
        }


        /*public SortedMap<String, JComponent> getThumbnailComponentCollection() {
         return thumbnailComponentCollection;
         }*/
        public void setMt(MediaTracker mt) {
                this.mt = mt;
        }

        public MediaTracker getMt() {
                return mt;
        }

        private void removeCollectionSprite(String key) {
                collection.remove(key);
                tbCollection.remove(key);
                /* thumbnailComponentCollection.remove(key);
                 componentCollection.remove(key);*/
        }
        /**
         * sprite instance stored in the collection attributes map
         */
        public final static String ATTRIBUTE_SPRITE = "sprite";
        /**
         * dimension of the sprite (thumbnail is different from full sized) in
         * the collection attributes map
         */
        public final static String ATTRIBUTE_DIMENSION = "dimension";
        /**
         * mime type of the original sprite in the collection attributes map
         */
        public final static String ATTRIBUTE_MIME = "mime";
        /**
         * buffered type (=ImageTypeSpecifier) of the original sprite in the
         * collection attributes map
         */
        public final static String ATTRIBUTE_TYPE = "type";
        /**
         * original dimensions of the sprite (same for thumnails sprites and
         * full sized sprites) in the collection attributes map
         */
        public final static String ATTRIBUTE_ORIG_DIMENSION = "orig-dimension";
        private transient ImageCollectionRenderer renderer = this;

        private JComponent makeThumbnailStore(File image, final String key, String mime, Map<String, Serializable> attrs) throws IOException {
                String m = _getImageMimeType(image, "r");
                Sprite picture = null;
                ImageReader r = null;
                try {
                        Map<String, Object> s = Sprite._loadImage(image, false, m, Sprite.MODE_JAVA2D | Sprite.MODE_TILE, _getBufferedType(m), false);
                        r = (ImageReader) s.get("reader");
                        picture = new Sprite(image, m, new Dimension(r.getWidth(0), r.getHeight(0)), true);
                } catch (Exception ex) {
                        if (JXAenvUtils._debug) {
                                ex.printStackTrace();
                        }
                        IOException e = new IOException(ex.getMessage());
                        e.initCause(ex);
                        throw e;
                } finally {
                        ((ImageInputStream) r.getInput()).flush();
                        r.dispose();
                        ((ImageInputStream) r.getInput()).close();
                        picture.setStoreMode(Sprite.MODE_JAVA2D | Sprite.MODE_TILE);
                        return makeThumbnailStore(picture, key, mime, attrs);
                }

        }

        private JComponent makeThumbnailStore(BufferedImage image, final String key, String mime, Map<String, Serializable> attrs) {
                Sprite picture = new Sprite(image, mime, new Dimension(image.getWidth(), image.getHeight()));
                picture.setStoreMode(Sprite.MODE_JAVA2D);
                return makeThumbnailStore(picture, key, mime, attrs);
        }

        /**
         *
         */
        private JComponent makeThumbnailStore(Sprite thumbnail, final String key, String mime, Map<String, Serializable> attrs) {
                Dimension origDim = thumbnail.getSize();
                Dimension thumbDim;
                double scale = Math.min(thumbnailSize.getWidth() / (double) thumbnail.getWidth(), thumbnailSize.getHeight() / (double) thumbnail.getHeight());
                AffineTransform tx = AffineTransform.getScaleInstance(scale, scale);
                thumbDim = tx.createTransformedShape(new Rectangle(thumbnail.getSize())).getBounds().getSize();
                thumbnail.setSize(thumbDim);
                thumbnail.setBufferedImageEnabled(buffered);
                thumbnail.setMt(mt, obs);
                thumbnail.setMultiThreadingEnabled(multiThreading);
                thumbnail.setBufferedType(_getBufferedType(mime));
                thumbnail.setSPM(_tbCollectionCache);
                thumbnail.setStoreMime(mime);
                updateIIOListeners(thumbnail);
                Map<String, Serializable> map = Collections.synchronizedMap(new HashMap<String, Serializable>());
                map.put(ATTRIBUTE_MIME, thumbnail.getMime());
                map.put(ATTRIBUTE_TYPE, _getBufferedType(thumbnail.getMime()));
                map.put(ATTRIBUTE_SPRITE, thumbnail);
                map.put(ATTRIBUTE_DIMENSION, thumbnail.getSize());
                map.put(ATTRIBUTE_ORIG_DIMENSION, origDim);
                /**
                 * MAY OVERRIDE above settings
                 */
                if (attrs instanceof Map) {
                        map.putAll(attrs);
                }
                tbCollection.put(key, map);
                thumbnail.clearResource();
                return renderer.makeCollectionRendererComponent(this, key, true);
        }

        private JComponent makeStore(File image, final String key, String mime, Map<String, Serializable> attrs) throws IOException {
                String m = _getImageMimeType(image, "r");
                Sprite picture = null;
                ImageReader r = null;
                try {
                        Map<String, Object> s = Sprite._loadImage(image, false, m, Sprite.MODE_JAVA2D | Sprite.MODE_TILE, _getBufferedType(m), false);
                        r = (ImageReader) s.get("reader");
                        picture = new Sprite(image, m, new Dimension(r.getWidth(0), r.getHeight(0)), true);
                } catch (Exception ex) {
                        if (JXAenvUtils._debug) {
                                ex.printStackTrace();
                        }
                        IOException e = new IOException(ex.getMessage());
                        e.initCause(ex);
                        throw e;
                } finally {
                        ((ImageInputStream) r.getInput()).flush();
                        r.dispose();
                        ((ImageInputStream) r.getInput()).close();
                        picture.setStoreMode(Sprite.MODE_JAVA2D | Sprite.MODE_TILE);
                        return makeStore(picture, key, mime, attrs);
                }

        }

        private JComponent makeStore(BufferedImage image, final String key, String mime, Map<String, Serializable> attrs) {
                Sprite picture = new Sprite(image, mime, new Dimension(image.getWidth(), image.getHeight()));
                picture.setStoreMode(Sprite.MODE_JAVA2D);
                return makeStore(picture, key, mime, attrs);
        }

        private JComponent makeStore(Sprite picture, final String key, String mime, Map<String, Serializable> attrs) {
                Dimension dim = picture.getSize();
                picture.setBufferedImageEnabled(buffered);
                picture.setMt(mt, obs);
                picture.setMultiThreadingEnabled(multiThreading);
                picture.setBufferedType(_getBufferedType(mime));
                picture.setSPM(_collectionCache);
                picture.setStoreMime(mime);
                updateIIOListeners(picture);
                Map<String, Serializable> map = Collections.synchronizedMap(new HashMap<String, Serializable>());
                map.put(ATTRIBUTE_SPRITE, picture);
                map.put(ATTRIBUTE_DIMENSION, dim);
                map.put(ATTRIBUTE_ORIG_DIMENSION, dim);
                map.put(ATTRIBUTE_MIME, picture.getMime());
                map.put(ATTRIBUTE_TYPE, _getBufferedType(picture.getMime()));
                /**
                 * MAY OVERRIDE above settings
                 */
                if (attrs instanceof Map) {
                        map.putAll(attrs);
                }
                collection.put(key, map);
                picture.clearResource();
                return renderer.makeCollectionRendererComponent(this, key, false);
        }
        /**
         * *
         * private JComponent getImageComponent(SortedMap<String, JComponent>
         * components, String key) { return components.containsKey(key) ?
         * components.get(key) : Sprite._makeSpriteJComponent(new
         * Sprite(Sprite.createBufferedImage(thumbnailSize,
         * Sprite.DEFAULT_TYPE), "image/x-png", thumbnailSize)); }
         */
        private static final long serialVersionUID = 2323;

        /**
         *
         */
        protected void updateIIOListeners(Sprite picture) {
                synchronized (iiorList) {
                        for (IIOReadProgressListener iior : iiorList) {
                                picture.getIO().addIIOReadProgressListener(iior);
                        }
                }
                synchronized (iiowList) {
                        for (IIOWriteProgressListener iiow : iiowList) {
                                picture.getIO().addIIOWriteProgressListener(iiow);
                        }
                }
        }

        protected static class FX {

                int fx;
                Point fx_loc;
                Color fx_clr;

                public FX() {
                        this(0, new Point(0, 0), null);
                }

                private FX(int fx, Point fx_loc, Color fx_clr) {
                        this.fx = fx;
                        this.fx_loc = fx_loc;
                        this.fx_clr = fx_clr;
                }
        }
        private transient FX defaultFX = new FX();

        public void setFXEnabled(boolean b, int fx, Point fx_loc, Color fx_clr) {
                if (b) {
                        defaultFX = new FX(fx, fx_loc, fx_clr);
                } else {
                        fxMap.clear();
                        defaultFX = new FX();
                }
        }
        private transient Map<String, FX> fxMap = Collections.synchronizedSortedMap(new TreeMap<String, FX>());

        protected FX getFX(String renderPic) {
                return fxMap.get(renderPic);
        }

        public void setFXEnabled(String renderPic, boolean b, int fx, Point fx_loc, Color fx_clr) {
                if (b) {
                        this.fxMap.put(renderPic, new FX(fx, fx_loc, fx_clr));
                } else {
                        fxMap.remove(renderPic);
                }
        }

        public boolean isFXEnabled(String renderPic) {
                FX fx = fxMap.containsKey(renderPic) ? fxMap.get(renderPic) : defaultFX;
                return fx.fx != GLFX.gfx.FX_NONE;
        }

        public boolean isFxEnabled() {
                return !fxMap.isEmpty() || defaultFX.fx != GLFX.gfx.FX_NONE;
        }

        /**
         * will wait until all related activity have finished and loaded
         *
         * @see #hasPicture(int, boolean)
         * @throws InterruptedException if any arror occured. {@link getErrored}
         * to retrieve and clear error states.
         */
        public synchronized void waitForPicture(String key, long millis) throws InterruptedException {
                int chk = STATE_UNLOADED | STATE_LOADING | (!isEdit(key, false) ? STATE_EDITING : 0);
                if ((getState(key) & chk) != 0) {
                        wait(millis);
                }
                if ((getState(key) & STATE_ERRORED) != 0) {
                        throw new InterruptedException("error occured for " + key);
                }
        }

        /**
         * will wait until all related activity have finished and loaded
         */
        public synchronized void waitForPicture(String key) throws InterruptedException {
                int chk = STATE_UNLOADED | STATE_LOADING | (!isEdit(key, false) ? STATE_EDITING : 0);
                while ((getState(key) & chk) != 0) {
                        wait();
                }
                if ((getState(key) & STATE_ERRORED) != 0) {
                        throw new InterruptedException("error occured for " + key);
                }
        }

        /**
         * will wait until all related activity have finished and loaded
         *
         * @see #hasPicture(int, boolean)
         * @throws InterruptedException if any arror occured. {@link getErrored}
         * to retrieve and clear error states.
         */
        public synchronized void waitForThumbnail(String key, long millis) throws InterruptedException {
                int chk = STATE_THUMB_UNLOADED | STATE_THUMB_LOADING | (!isEdit(key, true) ? STATE_THUMB_EDITING : 0);
                if ((getState(key) & chk) != 0) {
                        wait(millis);
                }
                if ((getState(key) & STATE_ERRORED) != 0) {
                        throw new InterruptedException("error occured for " + key);
                }
        }

        /**
         * will wait until all related activity have finished and loaded
         *
         * @throws InterruptedException if any arror occured. {@link getErrored}
         * to retrieve and clear error states.
         */
        public synchronized void waitForThumbnail(String key) throws InterruptedException {
                int chk = STATE_THUMB_UNLOADED | STATE_THUMB_LOADING | (!isEdit(key, true) ? STATE_THUMB_EDITING : 0);
                while ((getState(key) & chk) != 0) {
                        wait();
                }
                if ((getState(key) & STATE_ERRORED) != 0) {
                        throw new InterruptedException("error occured for " + key);
                }
        }

        /**
         * will wait until all related activity have finished and loaded
         *
         * @throws InterruptedException if any arror occured. {@link getErrored}
         * to retrieve and clear error states.
         */
        public synchronized void waitForAll() throws InterruptedException {
                int chk = STATE_UNLOADED | STATE_LOADING | (!lock.isEdit() ? STATE_EDITING : 0);
                if ((getState() & chk) != 0) {
                        wait();
                }
                if ((getState() & STATE_ERRORED) != 0) {
                        throw new InterruptedException("error(s) occured");
                }
        }

        /**
         * will wait until all related activity have finished and loaded
         *
         * @throws InterruptedException if any arror occured. {@link getErrored}
         * to retrieve and clear error states.
         */
        public synchronized void waitForAllThumbnails() throws InterruptedException {
                int chk = STATE_THUMB_UNLOADED | STATE_THUMB_LOADING | (!tbLock.isEdit() ? STATE_THUMB_EDITING : 0);
                if ((getState() & chk) != 0) {
                        wait();
                }
                if ((getState() & STATE_ERRORED) != 0) {
                        throw new InterruptedException("error(s) occured");
                }
        }
        private transient Map<String, Integer> pendingPictures = Collections.synchronizedSortedMap(new TreeMap<String, Integer>());
        protected transient ThreadWorks imageLoaders = new ThreadWorks(ImageCollection.class.getSimpleName() + " " + hashCode());
        protected transient ThreadWorks tbimageLoaders = new ThreadWorks(ImageCollection.class.getSimpleName() + " tb " + hashCode());

        /**
         *
         */
        public void addImage(final File f, final String key) {
                String m = _getImageMimeType(f, "r");
                addImage(f, key, m);
        }

        /**
         * @param storeMime the storeMime type that will be used to store the
         * picture
         */
        public void addImage(final File f, final String key, final String storeMime) {
                addImage(f, key, storeMime, null);
        }

        public synchronized void addImage(final File f, final String key, final String storeMime, final Map<String, Serializable> attrs) {
                pendingPictures.put(key, STATE_UNLOADED | STATE_THUMB_LOADING);
                tbimageLoaders.doLater(new Runnable() {

                        @Override
                        public void run() {
                                synchronized (ImageCollection.this) {
                                        try {
                                                makeThumbnailStore(f, key, storeMime, attrs);
                                                pendingPictures.put(key, STATE_LOADING | STATE_THUMB_LOADED);
                                                imageLoaders.doLater(new Runnable() {

                                                        @Override
                                                        public void run() {
                                                                try {
                                                                        makeStore(f, key, storeMime, attrs);

                                                                        pendingPictures.put(key, STATE_LOADED | STATE_THUMB_LOADED);
                                                                } catch (Throwable e) {
                                                                        e.printStackTrace();
                                                                        pendingPictures.put(key, STATE_ERRORED);
                                                                } finally {
                                                                        synchronized (ImageCollection.this) {
                                                                                ImageCollection.this.notifyAll();
                                                                        }
                                                                }
                                                        }
                                                });
                                        } catch (Throwable e) {
                                                e.printStackTrace();
                                                pendingPictures.put(key, STATE_ERRORED);
                                        } finally {
                                                ImageCollection.this.notifyAll();
                                        }
                                }
                        }
                });
                notifyAll();
        }

        /**
         * CAUTION : This method is memory sensitive over the lifetime of this
         * ImageCollection. Use it with small images only !
         */
        public void addImage(final BufferedImage image, final String key) {
                addImage(image, key, "image/x-png");
        }

        /**
         * CAUTION : This method is memory sensitive over the lifetime of this
         * ImageCollection. Use it with small images only !
         *
         * @param storeMime the storeMime type that will be used to store the
         * picture
         */
        public void addImage(final BufferedImage image, final String key, final String storeMime) {
                addImage(image, key, storeMime, null);
        }

        public synchronized void addImage(final BufferedImage image, final String key, final String storeMime, final Map<String, Serializable> attrs) {
                pendingPictures.put(key, STATE_UNLOADED | STATE_THUMB_LOADING);
                imageLoaders.doLater(new Runnable() {

                        @Override
                        public void run() {
                                synchronized (ImageCollection.this) {
                                        makeThumbnailStore(image, key, storeMime, attrs);
                                        pendingPictures.put(key, STATE_LOADING | STATE_THUMB_LOADED);
                                        makeStore(image, key, storeMime, attrs);
                                        pendingPictures.put(key, STATE_LOADED | STATE_THUMB_LOADED);
                                        ImageCollection.this.notifyAll();
                                }
                        }
                });
                ImageCollection.this.notifyAll();
        }

        /**
         *
         */
        public void removeImage(final String key) {
                try {
                        imageLoaders.doAndWait(new Runnable() {

                                @Override
                                public void run() {
                                        synchronized (ImageCollection.this) {
                                                removeCollectionSprite(key);
                                                pendingPictures.remove(key);
                                                ImageCollection.this.notifyAll();
                                        }
                                }
                        });
                } catch (InterruptedException e) {
                        e.printStackTrace();
                }
        }
        /**
         *
         */
        private Dimension thumbnailSize = new Dimension(64, 64);

        /**
         *
         */
        public void setThumbnailSize(Dimension size) {
                thumbnailSize = size;
        }

        /**
         *
         */
        public Dimension getThumbnailSize() {
                return thumbnailSize;
        }

        /**
         *
         */
        public JComponent getThumbnailComponent(String key) {
                JComponent thumbnail = renderer.makeCollectionRendererComponent(this, key, true);
                thumbnail.setPreferredSize(thumbnailSize);
                return thumbnail;
        }

        /**
         *
         */
        public JComponent getRenderComponent(String key) {
                JComponent render = renderer.makeCollectionRendererComponent(this, key, false);/*getImageComponent(componentCollection, key);*/

                return render;
        }

        /*  public SortedMap<String, JComponent> getRenderComponentCollection() {
         return componentCollection;
         }
         */
        /**
         * @throws JXAException This method must be called between an
         * {@linkplain #beginEdit(boolean)} and {@linkplain #endEdit(boolean)}
         * call.
         */
        public BufferedImage getPicture(Component obs, String key) {
                if (!isEdit(key, false)) {
                        throw new JXAException("no edit-paint is current or another edit-paint is current on another Thread " + (lock.threadEdit));
                }
                try {
                        waitForPicture(key);
                        Sprite sprite;
                        if (collection.containsKey(key)) {
                                sprite = (Sprite) collection.get(key).get(ATTRIBUTE_SPRITE);
                                sprite.setSize((Dimension) collection.get(key).get(ATTRIBUTE_DIMENSION));
                        } else {
                                sprite = new Sprite();
                        }
                        return sprite.toBuffered();
                } catch (InterruptedException ex) {
                        ex.printStackTrace();
                        return null;
                }
        }

        /**
         * @throws JXAException This method must be called between an
         * {@linkplain #beginEdit(boolean)} and {@linkplain #endEdit(boolean)}
         * call.
         */
        public void setPictureStoreAttribute(String key, String attributeKey, Serializable attributeValue) {
                Map<String, Serializable> attrs = getPictureStore(key);
                attrs.put(attributeKey, attributeValue);
                collection.put(key, attrs);
        }

        /**
         * * @throws JXAException This method must be called between an
         * {@linkplain #beginEdit(boolean)} and {@linkplain #endEdit(boolean)}
         * call.
         */
        public void setThumbnailPictureStoreAttribute(String key, String attributeKey, Serializable attributeValue) {
                Map<String, Serializable> attrs = getThumbnailPictureStore(key);
                attrs.put(attributeKey, attributeValue);
                tbCollection.put(key, attrs);
        }

        /**
         * @return see ATTRIBUTES_* for the returned mapping
         * @throws JXAException This method must be called between an
         * {@linkplain #beginEdit(boolean)} and {@linkplain #endEdit(boolean)}
         * call.
         */
        public Map<String, Serializable> getPictureStoreAttributes(String key, String... attributes) {
                Map<String, Serializable> subStore = new HashMap<String, Serializable>(attributes.length), store = getPictureStore(key);
                for (String att : attributes) {
                        subStore.put(att, store.get(att));
                }
                return subStore;
        }

        /**
         * @return see ATTRIBUTES_* for the returned mapping
         * @throws JXAException This method must be called between an
         * {@linkplain #beginEdit(boolean)} and {@linkplain #endEdit(boolean)}
         * call.
         */
        public Map<String, Serializable> getPictureStore(String key) {
                if (!isEdit(key, false)) {
                        throw new JXAException("no edit-paint is current or another edit-paint is current on another Thread " + (lock.threadEdit));
                }
                return collection.get(key);
        }

        /**
         * @return see ATTRIBUTES_* for the returned mapping
         * @throws JXAException This method must be called between an
         * {@linkplain #beginEdit(boolean)} and {@linkplain #endEdit(boolean)}
         * call.
         */
        public Map<String, Serializable> getThumbnailPictureStoreAttributes(String key, String... attributes) {
                Map<String, Serializable> subStore = new HashMap<String, Serializable>(attributes.length), store = getThumbnailPictureStore(key);
                for (String att : attributes) {
                        subStore.put(att, store.get(att));
                }
                return subStore;
        }

        /**
         * @return see ATTRIBUTES_* for the returned mapping
         * @throws JXAException This method must be called between an
         * {@linkplain #beginEdit(boolean)} and {@linkplain #endEdit(boolean)}
         * call.
         */
        public Map<String, Serializable> getThumbnailPictureStore(String key) {
                if (!isEdit(key, true)) {
                        throw new JXAException("no edit-paint is current or another edit-paint is current on another Thread " + (tbLock.threadEdit));
                }
                return tbCollection.get(key);
        }

        /**
         *
         */
        public boolean hasPicture(String key, boolean thumbnail) {
                return thumbnail ? (getState(key) & STATE_THUMB_LOADED) != 0 : (getState(key) & STATE_LOADED) != 0;
        }

        /**
         * to make iteration over this sortedSet, it must be synchronized on
         * this ImageCollection instance.
         *
         * @param thumbnail may be faster to load with thumbnail set to true
         * @return a common keySet for the collection cache
         */
        public SortedSet<String> keySet(boolean thumbnail) {
                return thumbnail ? (SortedSet<String>) _tbCollectionCache.keySet() : (SortedSet<String>) _collectionCache.keySet();
        }

        /**
         * @return The cache instance backing the imagecollection. See ATTRIBUTES_* for the returned mapping <String, Serializable>
         */
        public SpritesCacheManager<String, Map<String, Serializable>> accessCache(boolean thumbnail) {
                return thumbnail ? _tbCollectionCache : _collectionCache;
        }

        /**
         *
         */
        public void addCacheListener(SpritesCacheListener scl) {
                _collectionCache.addSpritesCacheListener(scl);
                _tbCollectionCache.addSpritesCacheListener(scl);
        }

        /**
         *
         */
        public void removeCacheListener(SpritesCacheListener scl) {
                _collectionCache.removeSpritesCacheListener(scl);
                _tbCollectionCache.removeSpritesCacheListener(scl);
        }
        /**
         *
         */
        private transient List<IIOReadProgressListener> iiorList = Collections.synchronizedList(new Vector<IIOReadProgressListener>());
        /**
         *
         */
        private transient List<IIOWriteProgressListener> iiowList = Collections.synchronizedList(new Vector<IIOWriteProgressListener>());

        /**
         *
         */
        public void addIIORListener(IIOReadProgressListener iior) {
                if (!iiorList.contains(iior)) {
                        iiorList.add(iior);
                }
        }

        /**
         *
         */
        public void removeIIORListener(IIOReadProgressListener iior) {
                iiorList.remove(iior);
        }

        /**
         *
         */
        public void addIIOWListener(IIOWriteProgressListener iiow) {
                if (!iiowList.contains(iiow)) {
                        iiowList.add(iiow);
                }
        }

        /**
         *
         */
        public void removeIIOWListener(IIOWriteProgressListener iiow) {
                iiowList.remove(iiow);
        }
        private static BitStack bits = new BitStack();
        /**
         * some picture is loading
         */
        public final static int STATE_LOADING = bits._newBitRange();
        /**
         * no picture is loading
         */
        public final static int STATE_UNLOADED = bits._newBitRange();
        /**
         * some picture is errored
         */
        public final static int STATE_ERRORED = bits._newBitRange();
        /**
         * some picture is errored
         */
        public final static int STATE_EDITING = bits._newBitRange();
        /**
         * no picture-thumbnail is loading
         */
        public final static int STATE_THUMB_UNLOADED = bits._newBitRange();
        /**
         * some picture-thumbnail is loading
         */
        public final static int STATE_THUMB_LOADING = bits._newBitRange();
        /**
         * all picture-thumbnails are loaded
         */
        public final static int STATE_THUMB_LOADED = bits._newBitRange();
        /**
         * some picture is errored
         */
        public final static int STATE_THUMB_EDITING = bits._newBitRange();
        /**
         * all pictures are loaded
         */
        public final static int STATE_LOADED = bits._newBitRange();
        /**
         * @deprecated use {@linkplain  #getState()}
         */
        private int state = 0;

        /**
         * @see #STATE_LOADING
         * @see #STATE_UNLOADED
         * @see #STATE_ERRORED
         * @see #STATE_LOADED
         * @see #STATE_THUMB_UNLOADED
         * @see #STATE_THUMB_LOADED
         * @see #STATE_THUMB_LOADING
         * @see #STATE_THUMB_ERRORED
         */
        public int getState() {
                int state = 0;
                synchronized (pendingPictures) {
                        for (String k : pendingPictures.keySet()) {
                                state |= pendingPictures.get(k);
                        }
                }
                this.state = state;
                return state;
        }

        /**
         * @return current images that could not be loaded
         **/
        public String[] getErrored(boolean clearStates) {
                if ((getState() & STATE_ERRORED) != 0) {
                        Vector<String> errored = new Vector<String>();
                        synchronized (pendingPictures) {
                                for (String key : pendingPictures.keySet()) {
                                        if ((getState(key) & STATE_ERRORED) != 0) {
                                                errored.add(key);
                                        }
                                }
                        }
                        if (clearStates) {
                                pendingPictures.clear();
                        }
                        return errored.toArray(new String[]{});
                }
                return new String[]{};
        }

        public int getState(String key) {
                int s = pendingPictures.containsKey(key) ? pendingPictures.get(key) : 0;
                if (s == 0) {
                        if (tbCollection.containsKey(key)) {
                                s |= STATE_THUMB_LOADED;
                        } else {
                                s |= STATE_THUMB_UNLOADED;
                        }
                        if (collection.containsKey(key)) {
                                s |= STATE_LOADED;
                        } else {
                                s |= STATE_UNLOADED;
                        }
                }
                return s;
        }

        /**
         * waits for all thumbnails to finish loading
         */
        @Override
        public Object loadResource() {
                try {
                        waitForAllThumbnails();
                } catch (InterruptedException ex) {
                        ex.printStackTrace();
                } finally {
                        return null;
                }
        }

        public int size() {
                return keySet(true).size();
        }

        /**
         * cancels all loading pictures and clears resources used by the
         * collection
         */
        @Override
        public Object clearResource() {
                try {
                        waitForAllThumbnails();
                } catch (InterruptedException ex) {
                        if (JXAenvUtils._debug) {
                                ex.printStackTrace();
                        }
                } finally {
                        if (isResourceLoaded()) {
                                imageLoaders.cancelAllSchedules();
                                collection.clear();
                                tbCollection.clear();
                                pendingPictures.clear();
                        }
                        return null;
                }
        }

        @Override
        public boolean isResourceLoaded() {
                int state = getState();
                return (state & (STATE_THUMB_UNLOADED | STATE_THUMB_LOADING)) == 0 && state != 0;

        }

        boolean multiThreading = JXAenvUtils._multiThreading;

        /**
         *
         * @return if {@linkplain JXAenvUtils#isMultiThreadingEnabled()}
         */
        public boolean isMultiThreadingEnabled() {
                return JXAenvUtils._multiThreading;
        }

        /**
         * {@linkplain JXAenvUtils#setMultiThreadingEnabled(boolean)}. There are some threads (asynchronous) that will still be running when multi-threading is disabled. Only the low-level activity is involved by this switch.
         * @default enabled if a multi-threading CPU is detected.
         */
        public void setMultiThreadingEnabled(boolean b) {
                multiThreading = JXAenvUtils._multiThreading = b;
        }

        @Override
        public JComponent makeCollectionRendererComponent(final ImageCollection collection, final String key, final boolean thumbnail) {
                JComponent j = new JComponent() {

                        final long hash = System.nanoTime();

                        @Override
                        public final int hashCode() {
                                return (int) hash;
                        }

                        @Override
                        public final boolean equals(Object o) {
                                return o == null ? false : o.hashCode() == hashCode();
                        }

                        @Override
                        protected void paintComponent(Graphics g) {
                                super.paintComponent(g);
                                beginEdit(key, thumbnail);
                                Sprite picture = collection.getCollectionSprite(key, thumbnail);
                                boolean tiled = picture.isTileModeEnabled();
                                if (!thumbnail) {
                                        picture.setTileModeEnabled(picture.isTilesAvailable());
                                }
                                boolean bufferedOrig = picture.isBufferedImageEnabled();
                                picture.setBufferedImageEnabled(buffered);
                                Rectangle bounds = getBounds();
                                Rectangle origPicBds = picture.getBounds(), picBds = (Rectangle) origPicBds.clone();
                                /**
                                 * scale and preserve proportions
                                 */
                                double scale = Math.min((float) bounds.getWidth() / (float) picBds.getWidth(), (float) bounds.getHeight() / (float) picBds.getHeight());
                                picBds = AffineTransform.getScaleInstance(scale, scale).createTransformedShape(picBds).getBounds();
                                picBds.setLocation((int) Math.round(bounds.getCenterX() - picBds.getWidth() / 2.0), (int) Math.round(bounds.getCenterY() - picBds.getHeight() / 2.0));
                                FX fx = fxMap.containsKey(key) ? fxMap.get(key) : defaultFX;
                                if (isFXEnabled(key)) {
                                        picBds.grow(-Math.abs(fx.fx_loc.x), -Math.abs(fx.fx_loc.y));
                                }
                                picture.setBounds(picBds);
                                picture.runValidate();
                                g.translate(-bounds.x, -bounds.y);
                                picture.draw(this, (Graphics2D) g, fx.fx, fx.fx_loc, fx.fx_clr);
                                g.translate(bounds.x, bounds.y);
                                picture.setBounds(origPicBds);
                                picture.setTileModeEnabled(tiled);
                                picture.setBufferedImageEnabled(bufferedOrig);
                                endEdit(key, thumbnail);
                        }

                        @Override
                        public void addNotify() {
                                super.addNotify();
                                /* nothing here (just to add breakpoints) */
                        }

                        @Override
                        public void removeNotify() {
                                super.removeNotify();
                                /* nothing here (just to add breakpoints) */
                        }

                };

                j.addComponentListener(new ComponentAdapter() {

                        @Override
                        public void componentResized(ComponentEvent e) {
                                /* to avoid crappy affect if the component is stretched out*/
                                super.componentResized(e);
                                beginEdit(key, thumbnail);
                                collection.getCollectionSprite(key, thumbnail).clearResource();
                                endEdit(key, thumbnail);

                        }
                });
                if (thumbnail) {
                        j.setPreferredSize(thumbnailSize);
                }
                return j;
        }

        public void setRenderer(ImageCollectionRenderer renderer) {
                this.renderer = renderer;
        }

        public ImageCollectionRenderer getRenderer() {
                return renderer;
        }

        /**
         * collection sprite, cached. if it can be tiled, then the tilemode is
         * enabled
         *
         * @throws JXAException This method must be called between an
         * {@linkplain #beginEdit(boolean)} and {@linkplain #endEdit(boolean)}
         * call.
         */
        public Sprite getCollectionSprite(String key, boolean thumbnail) {
                if (!isEdit(key, thumbnail)) {
                        throw new JXAException("no edit-paint is current or another edit-paint is current on another Thread " + (thumbnail ? tbLock.threadEdit : lock.threadEdit));
                }
                Sprite picture;
                if (hasPicture(key, thumbnail)) {
                        picture = thumbnail ? (Sprite) tbCollection.get(key).get(ATTRIBUTE_SPRITE) : (Sprite) collection.get(key).get(ATTRIBUTE_SPRITE);
                } else {
                        picture = new Sprite();
                        picture.setSize(thumbnailSize);
                        picture.runValidate();
                        Image im = picture.getImage(obs);
                        Graphics2D g = Sprite.wrapRendering(Sprite._createImageGraphics(im));
                        Icon ic = UIMessage._getIcon(UIMessage.LOAD_TYPE, false);
                        ic.paintIcon(obs, g, (int) ((float) (picture.getWidth() - ic.getIconWidth()) / 2f), (int) ((float) (picture.getWidth() - ic.getIconWidth()) / 2f));
                        g.dispose();
                }
                picture.setMultiThreadingEnabled(multiThreading);
                updateIIOListeners(picture);
                picture.setMt(mt, obs);
                picture.setSPM(thumbnail ? _tbCollectionCache : _collectionCache);
                return picture;
        }

        private static class EditLock {

                final Monitor editMonitor = new Monitor();
                long threadEdit = 0;
                int threadEditLvl = 0;

                public void beginEdit() {
                        try {
                                synchronized (editMonitor) {
                                        while (threadEdit != 0 && threadEdit != Thread.currentThread().getId()) {
                                                editMonitor.wait();
                                        }
                                        threadEdit = Thread.currentThread().getId();
                                        threadEditLvl++;
                                }
                        } catch (InterruptedException ex) {
                                throw new JXAException(ex);
                        }
                }

                public boolean isEdit() {
                        return threadEdit == Thread.currentThread().getId();
                }

                public void clearEdit() {
                        synchronized (editMonitor) {
                                threadEdit = 0;
                                editMonitor.notifyAll();
                        }
                }

                public void endEdit() {
                        synchronized (editMonitor) {
                                if (threadEdit == 0) {
                                        if (JXAenvUtils._debug) {
                                                System.err.println(JXAenvUtils.log("no edit-paint is current", JXAenvUtils.LVL.APP_WRN));
                                        }
                                } else {
                                        if (threadEdit != Thread.currentThread().getId()) {
                                                throw new JXAException("another edit-paint is current on another Thread " + threadEdit);
                                        }
                                        threadEditLvl = Math.max(0, threadEditLvl - 1);
                                        threadEdit = threadEditLvl != 0 ? threadEdit : 0;
                                }
                                editMonitor.notifyAll();
                        }
                }
        }
        transient EditLock tbLock = new EditLock();
        transient EditLock lock = new EditLock();

        /**
         * use it to paint or edit, it will wait until no other thread is
         * editing on this imageCollection (another beginEdit was called). Then
         * it locks either
         * {@link #getThumbnailPictureStore(java.lang.String) thumbnails} or the
         * {@link #getCollectionSprite(java.lang.String, boolean) full picture collection}.
         * <br>
         * Many calls to this method on the same thread means as many
         * {@linkplain #endEdit()} to be called upon the next task
         * completion.<br>
         *
         * @param thumbnail if thumbnail is requested, the sync lock will not
         * affect the full sized sprite
         */
        public synchronized void beginEdit(String key, boolean thumbnail) {
                if (thumbnail) {
                        tbLock.beginEdit();
                        pendingPictures.put(key, getState(key) | STATE_THUMB_EDITING);
                } else {
                        lock.beginEdit();
                        pendingPictures.put(key, getState(key) | STATE_EDITING);
                }
                notifyAll();
        }

        /**
         * current thread is editing
         */
        public boolean isEdit(String key, boolean thumbnail) {
                boolean b = thumbnail ? tbLock.isEdit() : lock.isEdit();
                return (getState(key) & (thumbnail ? STATE_THUMB_EDITING : STATE_EDITING)) != 0 && b;
        }

        /**
         * clears edit thread state (in case of an interruption occured between
         * {@linkplain #beginEdit()} and {@linkplain #endEdit()} calls)
         */
        public synchronized void clearEdit(boolean thumbnail) {
                if (thumbnail) {
                        tbLock.clearEdit();
                } else {
                        lock.clearEdit();
                }
                synchronized (pendingPictures) {
                        for (Iterator<Map.Entry<String, Integer>> it = pendingPictures.entrySet().iterator(); it.hasNext();) {
                                Map.Entry<String, Integer> me = it.next();
                                me.setValue(me.getValue() & ~(STATE_THUMB_EDITING | STATE_EDITING));
                        }
                }
                notifyAll();
        }

        /**
         * use it to paint or edit
         *
         * @throws JXAException if another Thread has started the editing
         */
        public synchronized void endEdit(String key, boolean thumbnail) {
                if (thumbnail) {
                        tbLock.endEdit();
                        pendingPictures.put(key, getState(key) & ~STATE_THUMB_EDITING);
                } else {
                        lock.endEdit();
                        pendingPictures.put(key, getState(key) & ~STATE_EDITING);
                }
                notifyAll();
        }
}
