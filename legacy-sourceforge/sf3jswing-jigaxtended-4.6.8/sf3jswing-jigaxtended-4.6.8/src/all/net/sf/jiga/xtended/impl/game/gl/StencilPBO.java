/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.game.gl;

import net.sf.jiga.xtended.impl.system.ThreadBlock;
import java.awt.Rectangle;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.sf.jiga.xtended.JXAException;
import net.sf.jiga.xtended.impl.Sprite;
import net.sf.jiga.xtended.impl.game.RenderingScene;
import net.sf.jiga.xtended.impl.game.gl.MemScratch.Item;
import net.sf.jiga.xtended.impl.game.gl.geom.GLGeom;
import net.sf.jiga.xtended.impl.system.BufferIO;
import net.sf.jiga.xtended.kernel.BitStack;
import net.sf.jiga.xtended.kernel.DebugMap;
import org.lwjgl.opengl.ARBPixelBufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.Util;
import org.lwjgl.util.glu.Project;

/**
 * Take a PBO Stencil_Index snapshot area in the framebuffer (download) and
 * paste it into a dedicated GL_LUMINANCE storage buffer object (upload to a
 * texture), which can be rendered then in the normal Gl_Color_Buffer. <br>
 * Region of interest -ROI- is the area of the framebuffer (both in
 * Gl_Stencil_Buffer and Gl_Color_Buffer) that the storage buffer is bound to
 * and renders to. <BR> 'Packed' means the storage buffer object has gathered
 * pixel data from PBO and is ready to render. <br> PBO swapping is used to
 * "stream" to the Buffer Object after a read from the Gl_Stencil_Buffer.
 * <pre>(MODE_STREAM) streaming uploadings to the texture (unstable)
 * Timeline ----->----->----(repeat as many times as necessary)--->----->----->----->----->----->
 *
 * rendering to Stencil and
 * start download (glreadpixels) -> PBO-0           start download (glreadpixels) -> PBO-1
 *                                       = SWAPPBO =
 * PBO-1 -> finish upload (glTexSubImage)           PBO-0 -> finish upload (glTexSubImage)
 *
 * Timeline --->----(after the last upload finishes) ----->----->----->----->----- Texture Render
 * </pre>
 * <pre>
 * (MODE_DYNAMIC) uploading asynchronously
 * Timeline ----->----->----(repeat as many times as necessary)--->----->----->----->----->----->
 *
 * GPUGPU start download (glgetteximage -> glreadpixels -> finish upload (glTexSubImage) = SWAPPBO =
 *
 * CPUCPU   ... -> rendering to Stencil -> do some other
 *                                    CPU Tasks  -> rendering to Stencil -> do ...
 *
 * Timeline --->----->----->----->----(after the last upload finishes) ----->----- Texture Render
 *
 * Clear Buffer feature : PBO-2 Dimension === ROI packed Dimension (from glProject window coordinates)
 * </pre> NOTE about DMA transfers : ..._DMA sufixed methods provide fast data
 * transfer with DMA (Direct Memory Access, i.e. no CPU cycles are involved in
 * the transfer)
 *
 * <br> Example of code :
 * <pre>
 * glPBO.beginStencil();
 *
 * while(!objectToRender.isEmpty()){
 * // do some (text or complex 2D shapes) rendering on the current frame and
 * Rectangle dirtyArea = glPBO.ROIAsDirty(objectToRender.getBounds(), z);
 * // pack the dirtyArea whenever it is needed
 * glPBO.GLpackStencil(dirtyArea);}
 *
 * glPBO.endStencil();
 * glPBO.GLflush(scene);
 * glPBO.GLrender(scene, GLGeom.getCWrgbaColors(foreground, 1));
 * </pre>
 *
 * @author www.b23prodtm.info
 */
public class StencilPBO {

    final long hash = System.nanoTime();
    private ROI ROI = new ROI(new Rectangle(), 0);
    /*
     * private RenderingSceneGL scene;
     */

    @Override
    public int hashCode() {
        return (int) hash;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == null ? false : obj.hashCode() == hashCode();
    }

    /**
     * {@link GLHandler.ext#PixelBufferObjects} must be available new packed
     * stencilpbo
     *
     * @param ROI Region of Interest Rectangle of the screen to store stencil as
     * Buffer .
     * @param z ROI z coord
     * @return {@link #readStencilBuffer() } to store (readStencilBuffer)
     * stencil buffer
     * @param mode choose mode of filling the PBO and texture
     */
    public StencilPBO(Rectangle ROI, double z, MODE mode) {
        this.mode = mode;
        prepareBufferObject(ROI, z, false);
        if (DebugMap._getInstance().isDebugLevelEnabled(Sprite.DBUG_RENDER_LOW)) {
            int read = GL11.glGetInteger(GL11.GL_READ_BUFFER);
            int draw = GL11.glGetInteger(GL11.GL_DRAW_BUFFER);
            System.out.println("GL readbuffer : " + (read == GL11.GL_BACK ? "back" : read == GL11.GL_FRONT ? "front" : read) + " drawbuffer :" + (draw == GL11.GL_BACK ? "back" : draw == GL11.GL_FRONT ? "front" : draw));
        }

    }

    /**
     * return packed (gluproject'ed bounds) ROIcontext contents size and
     * position _on window coordinates_
     *
     * @param area region in ROIcontext
     */
    private ROI returnPixelPackedRegion(Rectangle area) {
        ROI dirtyAreaPixelPack = new ROI(new Rectangle(), 0);
        /**
         * project depth bounds java2d coordinates ! x0y0 is top left corner
         */
        GLHandler._transformBegin(ROI.ROI, ROI.winZ, transform, scaleArgs, translateArgs, rotateArgs);
        pack_viewport.clear();
        pack_Rpos.clear();
        GL11.glGetInteger(GL11.GL_VIEWPORT, pack_viewport);
        pack_view = GLHandler._GLgetTopOftheStackMX(GL11.GL_MODELVIEW_MATRIX);
        pack_proj = GLHandler._GLgetTopOftheStackMX(GL11.GL_PROJECTION_MATRIX);
        Util.checkGLError();
        boolean success = Project.gluProject((float) area.getMinX(), (float) area.getMinY(), 0, pack_view, pack_proj, pack_viewport, pack_Rpos);
        if (!success) {
            throw new JXAException("error gluproject these coords " + area);
        }
        Util.checkGLError();
        double x0 = pack_Rpos.get(), y0 = pack_Rpos.get(), z0 = pack_Rpos.get();
        pack_view.rewind();
        pack_proj.rewind();
        pack_viewport.rewind();
        pack_Rpos.clear();
        success = Project.gluProject((float) area.getMaxX(), (float) area.getMaxY(), 0, pack_view, pack_proj, pack_viewport, pack_Rpos);
        pack_Rpos.rewind();
        if (!success) {
            throw new JXAException(JXAException.LEVEL.APP, "error gluproject these coords " + area);
        }
        Util.checkGLError();
        double x1 = pack_Rpos.get(), y1 = pack_Rpos.get(), z1 = pack_Rpos.get();
        /**
         * window coordinates !
         */
        dirtyAreaPixelPack.ROI.setFrameFromDiagonal(x0, y0, x1, y1);
        dirtyAreaPixelPack.winZ = (z0 + z1) / 2.;
        GLHandler._transformEnd();
        return dirtyAreaPixelPack;
    }

    /**
     * Region of Interest Rectangle in ModelView coord
     *
     * @return
     */
    public ROI ROIcontext() {
        return ROI;
    }

    private void checkSize(int nSize, boolean forceInit) {
        GLHandler._GLpushAttrib(GL11.GL_TEXTURE_BIT);
        GL11.glEnable(Sf3Texture._EXTtex);
        GL11.glBindTexture(Sf3Texture._EXTtex, GLHandler.sTex.firstItemBufferName(hashCode()));
        int width = GL11.glGetTexLevelParameteri(Sf3Texture._EXTtex, 0, GL11.GL_TEXTURE_WIDTH),
                height = GL11.glGetTexLevelParameteri(Sf3Texture._EXTtex, 0, GL11.GL_TEXTURE_HEIGHT);
        int texSize = width * height;
        if (texSize != nSize || forceInit) {
            _init();
        }
        GL11.glBindTexture(Sf3Texture._EXTtex, 0);
        GLHandler._GLpopAttrib();
    }

    private void _init() {
        GL11.glTexParameteri(Sf3Texture._EXTtex, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
        GL11.glTexParameteri(Sf3Texture._EXTtex, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
        GL11.glTexParameteri(Sf3Texture._EXTtex, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(Sf3Texture._EXTtex, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        ARBPixelBufferObject.glBindBufferARB(ARBPixelBufferObject.GL_PIXEL_UNPACK_BUFFER_ARB, GLHandler.sPbo.itemBufferName(hashCode(), 2));
        /**
         * Intensity is used as an internal format as it handles pixels with
         * only one (alpha) component, like the stencil buffer has. Luminance is
         * the input format, which copies RGB values only (alpha is set to 1.O).
         * Intensity will modulate everything to one intensity value (including
         * alpha).
         */
        GL11.glTexImage2D(Sf3Texture._EXTtex, 0, GL11.GL_INTENSITY, ROIpack.ROI.width, ROIpack.ROI.height, 0, GL11.GL_LUMINANCE, GL11.GL_FLOAT, 0 /*
                 * BufferIO._zero(BufferIO._newf(nSize))
                 */);
        ARBPixelBufferObject.glBindBufferARB(ARBPixelBufferObject.GL_PIXEL_UNPACK_BUFFER_ARB, 0);
        bstate = CLEARED;
    }

    /**
     *
     * @param target whatever target {@link ARBPixelBufferObject#GL_PIXEL_PACK_BUFFER_ARB}
     * or
     * {@link ARBPixelBufferObject#GL_PIXEL_UNPACK_BUFFER_ARB}, it's only for
     * initializing
     * @param pbo
     * @param nSize
     * @param force
     */
    private void PBOcheckSize(int target, Item pbo, int nSize, boolean force) {
        ARBPixelBufferObject.glBindBufferARB(target, pbo.name);
        int size = ARBPixelBufferObject.glGetBufferParameteriARB(target, ARBPixelBufferObject.GL_BUFFER_SIZE_ARB) / BufferIO.Data.FLOAT.byteSize;
        if (size != nSize || force) {
            _PBOinit(target, pbo, nSize, pbo.usage, false);
        }
        ARBPixelBufferObject.glBindBufferARB(target, 0);
    }

    /**
     * creates and initialize a new buffer at the specified pboName and target.
     * The previous buffer is discarded (any previous task will complete in the
     * background).
     */
    private void _PBOinit(int target, Item pbo, int nSize, int usage, boolean fillZeros) {
        FloatBuffer fb = fillZeros ? BufferIO._zero(BufferIO._newf(nSize)) : BufferIO._newf(nSize);
        _PBOload(target, pbo, usage, fb);
    }

    private void _PBOload(int target, Item pbo, int usage, FloatBuffer fb) {
        pbo.usage = usage;
        ARBPixelBufferObject.glBufferDataARB(target, fb.limit() * BufferIO.Data.FLOAT.byteSize, usage);
        ByteBuffer bb = ARBPixelBufferObject.glMapBufferARB(target, ARBPixelBufferObject.GL_WRITE_ONLY_ARB, null);
        if (bb != null) {
            bb.asFloatBuffer().put(fb).flip();
        }
        ARBPixelBufferObject.glUnmapBufferARB(target);
        Util.checkGLError();
    }

    private void PBOgetTex(Item pbo) {
        /**
         * read (pack) into the PBO to make a texture update possible the pbo
         * contents will be sent back to the texture after it has been modified
         */
        ARBPixelBufferObject.glBindBufferARB(ARBPixelBufferObject.GL_PIXEL_PACK_BUFFER_ARB, pbo.name);
        GLHandler._GLpushAttrib(GL11.GL_TEXTURE_BIT);
        GL11.glEnable(Sf3Texture._EXTtex);
        GL11.glBindTexture(Sf3Texture._EXTtex, GLHandler.sTex.firstItemBufferName(hashCode()));
        GL11.glGetTexImage(Sf3Texture._EXTtex, 0, GL11.GL_LUMINANCE, GL11.GL_FLOAT, 0);
        GL11.glBindTexture(Sf3Texture._EXTtex, 0);
        GLHandler._GLpopAttrib();
        ARBPixelBufferObject.glBindBufferARB(ARBPixelBufferObject.GL_PIXEL_PACK_BUFFER_ARB, 0);
        Util.checkGLError();
    }
    private final static ThreadBlock _stencilBlock = new ThreadBlock();

    public final void beginStencil() {
        _stencilBlock.begin();
        GLHandler._GLpushAttrib(GL11.GL_STENCIL_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
        /**
         * "decals" pattern drawing
         */
        Util.checkGLError();
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glColorMask(false, false, false, false);
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glStencilMask(GL11.GL_TRUE);
        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 1);
        GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE);
        /*
         * PBOgetTex(GLHandler.sPbo.firstItem(hashCode()));
         */
    }

    public final void endStencil() {
        if (_stencilBlock.isOn()) {
            GLHandler._GLpopAttrib();
        }
        _stencilBlock.end();
    }
    private Set<StencilPBOListener> listeners = Collections.synchronizedSet(new HashSet());

    public void addListener(StencilPBOListener listener) {
        listeners.add(listener);
    }

    public void removeListener(StencilPBOListener listener) {
        listeners.remove(listener);
    }

    private void contentsWereLost() {
        synchronized (listeners) {
            for (StencilPBOListener l : listeners) {
                l.contentsLost();
            }
        }
    }

    /**
     * PBO MODE
     */
    public static enum MODE {

        /**
         * this mode is stable
         */
        MODE_DYNAMIC(ARBPixelBufferObject.GL_DYNAMIC_COPY_ARB),
        /**
         * unstable mode due to a gltexsubimage issue with pbo uploading not of
         * the same size
         */
        MODE_STREAM(ARBPixelBufferObject.GL_STREAM_COPY_ARB);
        int ARBBufferMode;

        private MODE(int ARBBuffermode) {
            this.ARBBufferMode = ARBBuffermode;
        }
    }
    private MODE mode;

    public final void prepareBufferObject(Rectangle newROI, double z, boolean clearTex) {
        boolean prepareTex = !GLHandler.sTex.hasItem(hashCode());
        boolean preparePbo = !GLHandler.sPbo.hasItem(hashCode());
        ROI.ROI.setBounds(newROI);
        ROI.winZ = z;
        /**
         * zero the pbo if size are different or clearPBO is set
         */
        ROIpack = returnPixelPackedRegion(new Rectangle(ROI.ROI.getSize()));
        /**
         * handle no exttexturerectangle
         */
        if (Sf3Texture._EXTtex == GL11.GL_TEXTURE_2D) {
            ROIpack.ROI.setSize(Sf3Texture._getNextPowerOfTwo(newROI.getSize()));
        }
        int nSize = ROIpack.ROI.width * ROIpack.ROI.height;
        if (preparePbo) {
            /**
             * pbo
             */
            GLHandler.sPbo.removeItems(hashCode());
            /**
             * items index 0 and 1 will swap each other to allow one
             * readStencilBuffer and one write operation simultaneously with DMA
             * transfers
             */
            for (int i = 0; i < 3; i++) {
                int pboName = GLHandler.sPbo.getVRAMFindVacant();
                GLHandler.sPbo.addItem(hashCode(), new MemScratch.Item<DoubleBuffer>(pboName, ROIpack.toMemScratchDataStore(), mode.ARBBufferMode));
            }
        }
        /**
         * correct buffer sizes
         *
         */
        for (int i = 0; i < 3; i++) {
            Item pbo = GLHandler.sPbo.item(hashCode(), i);
            PBOcheckSize(ARBPixelBufferObject.GL_PIXEL_PACK_BUFFER_ARB, pbo, nSize, preparePbo);
            pbo.data = ROIpack.toMemScratchDataStore();
        }

        /*
         * texture buffer object
         */
        if (prepareTex) {
            GLHandler.sTex.removeItems(hashCode());
            int name = GLHandler.sTex.getVRAMFindVacant();
            GLHandler.sTex.addItem(hashCode(), new MemScratch.Item(name, BufferIO._wrapi(new int[]{ROIpack.ROI.width, ROIpack.ROI.height, -1})));
            bstate = CLEARED;
        }
        checkSize(nSize, clearTex || prepareTex);
        GL11.glPrioritizeTextures(BufferIO._wrapi(new int[]{GLHandler.sTex.firstItemBufferName(hashCode())}), BufferIO._wrapf(new float[]{1f}));
        Util.checkGLError();
        if ((bstate & CLEARED) != 0) {
            contentsWereLost();
        }
    }

    /**
     * swap read and write pbo
     */
    private void PBOswap() {
        List<Item<DoubleBuffer>> items = GLHandler.sPbo.getItems(hashCode());
        Item<DoubleBuffer> itemRead = items.get(0);
        items.set(0, items.get(1));
        items.set(1, itemRead);
    }
    /*
     * PBO is unuseable until the next call to prepareBufferObject().
     */

    protected void releaseBufferObject() {
        GLHandler.sTex.removeItems(hashCode());
        PBOrelease();
        bstate = CLEARED;
    }

    /*
     * PBO is unuseable until the next call to prepareBufferObject().
     */
    private void PBOrelease() {
        GLHandler.sPbo.removeItems(hashCode());
        bstate = CLEARED;
    }
    ROI ROIpack = new ROI(new Rectangle(), 0);

    /**
     * Window coordinates of the {@link #ROIcontext() Region of Interest Rectangle}
     */
    public ROI ROIpacked() {
        return ROIpack;
    }
    private final static BitStack _bState = new BitStack();
    private final static int CLEARED = _bState._newBitRange();
    private final static int PACKED = _bState._newBitRange();
    private final static int UNPACKED = _bState._newBitRange();
    private int bstate = CLEARED;
    private FloatBuffer pack_view = null, pack_proj = null;
    private int transform = 0;
    private DoubleBuffer scaleArgs = null, translateArgs = null, rotateArgs = null;

    public void setTransformContext(int transform, DoubleBuffer scaleArgs, DoubleBuffer translateArgs, DoubleBuffer rotateArgs) {
        this.transform = transform;
        this.scaleArgs = scaleArgs;
        this.translateArgs = translateArgs;
        this.rotateArgs = rotateArgs;
    }

    /*
     * wrong read process (pbocannot be used here as the read target, data has
     * to be downloaded into the ram first)
     */
    private void PBOpackAreaTo_DMA(Item pbo,
            ROI packedArea) {
        if (DebugMap._getInstance().isDebugLevelEnabled(Sprite.DBUG_RENDER_LOW)) {
            System.out.println("packedArea window-coords " + packedArea);
        }
        ARBPixelBufferObject.glBindBufferARB(ARBPixelBufferObject.GL_PIXEL_PACK_BUFFER_ARB,
                pbo.name);
        /**
         * read a region of the screen into a region of the pbo, which has just
         * downloaded the texture contents
         */
        GL11.glPixelStorei(GL11.GL_PACK_SKIP_PIXELS, packedArea.ROI.x - ROIpack.ROI.x);
        GL11.glPixelStorei(GL11.GL_PACK_SKIP_ROWS, packedArea.ROI.y - ROIpack.ROI.y);
        GL11.glPixelStorei(GL11.GL_PACK_ROW_LENGTH, ROIpack.ROI.width);
        /**
         * read stencil ROIpack
         */
        GL11.glReadPixels(packedArea.ROI.x, packedArea.ROI.y,
                packedArea.ROI.width, packedArea.ROI.height, GL11.GL_STENCIL_INDEX,
                GL11.GL_FLOAT, 0);
        ARBPixelBufferObject.glBindBufferARB(ARBPixelBufferObject.GL_PIXEL_PACK_BUFFER_ARB,
                0);
        GL11.glPixelStorei(GL11.GL_PACK_SKIP_PIXELS, 0);
        GL11.glPixelStorei(GL11.GL_PACK_SKIP_ROWS, 0);
        GL11.glPixelStorei(GL11.GL_PACK_ROW_LENGTH, 0);
        Util.checkGLError();
        pbo.data = ROIpack.toMemScratchDataStore();
        bstate = PACKED;
    }

    private void PBOclearArea_DMA(ROI packedClearedArea) {
        /**
         * select third PBO for DMA transfer from/to the texture
         */
        Item i = GLHandler.sPbo.item(hashCode(), 2);
        /**
         * download the texture for update
         */
        PBOgetTex(i);
        /**
         * clearing PBO region and resend to the texture
         */
        PBOclearArea_noDMA(ARBPixelBufferObject.GL_PIXEL_PACK_BUFFER_ARB, i, packedClearedArea);
        _PBOsendPixels_DMA(i, packedClearedArea);
    }

    /**
     * clear PBO ROI
     */
    private void PBOclearArea_noDMA(int target, Item pbo, ROI packedClearedArea) {
        if ((bstate & PACKED) == 0) {
            return;
        }
        FloatBuffer zeros = BufferIO._zero(BufferIO._newf(packedClearedArea.ROI.width));
        Rectangle pboROI = new ROI((DoubleBuffer) pbo.data).ROI;
        ARBPixelBufferObject.glBindBufferARB(target, pbo.name);
        Util.checkGLError();
        ByteBuffer bb = ARBPixelBufferObject.glMapBufferARB(target, ARBPixelBufferObject.GL_READ_WRITE_ARB, ROIpack.ROI.width * ROIpack.ROI.height * BufferIO.Data.FLOAT.byteSize, null);
        Util.checkGLError();
        if (bb instanceof ByteBuffer) {
            if (DebugMap._getInstance().isDebugLevelEnabled(Sprite.DBUG_RENDER_LOW)) {
                System.out.println("pbo buffer size " + bb.limit() / BufferIO.Data._findType(zeros).byteSize);
                System.out.println("map final pos " + ((packedClearedArea.ROI.y - pboROI.y + (packedClearedArea.ROI.height - 1)) * pboROI.width + (packedClearedArea.ROI.x - pboROI.x) * packedClearedArea.ROI.height));

            }
            FloatBuffer fb = bb.asFloatBuffer();
            for (int j = 0; j < packedClearedArea.ROI.height; j++) {
                /** line by line
                 * (yb + j) * width + xb
                 */
                fb.position((packedClearedArea.ROI.y - pboROI.y + j) * pboROI.width + packedClearedArea.ROI.x - pboROI.x);
                fb.put(zeros);
                zeros.rewind();
            }
        }
        ARBPixelBufferObject.glUnmapBufferARB(target);
        ARBPixelBufferObject.glBindBufferARB(target, 0);
        Util.checkGLError();
        bstate = PACKED;
    }
    /*
     * to "paste" the stencil PBO into the Stencil Buffer, while colormask is
     * disabled. readStencilBuffer() must be called once, first. Z-depth must be
     * the same value as when readStencilBuffer() was run, provided the same
     * ModelView matrix. {@link GLHandler.ext#PixelBufferObjects} must be
     * available
     *
     */

    private void PBOsendPixels_DMA(ROI packedROI) {
        if ((bstate & PACKED) != 0) {
            Item i = GLHandler.sPbo.firstItem(hashCode());
            _PBOsendPixels_DMA(i, packedROI);
        }
    }

    private FloatBuffer PBOgetContents_noDMA(Item i) {
        FloatBuffer fb = null;
        ARBPixelBufferObject.glBindBufferARB(ARBPixelBufferObject.GL_PIXEL_PACK_BUFFER_ARB, i.name);
        ByteBuffer bb = ARBPixelBufferObject.glMapBufferARB(ARBPixelBufferObject.GL_PIXEL_PACK_BUFFER_ARB, ARBPixelBufferObject.GL_READ_ONLY_ARB, ROIpack.ROI.width * ROIpack.ROI.height * BufferIO.Data.FLOAT.byteSize, null);
        if (bb != null) {
            fb = ((ByteBuffer) BufferIO._new(bb.limit()).put(bb).flip()).asFloatBuffer();
        }
        ARBPixelBufferObject.glUnmapBufferARB(ARBPixelBufferObject.GL_PIXEL_PACK_BUFFER_ARB);
        ARBPixelBufferObject.glBindBufferARB(ARBPixelBufferObject.GL_PIXEL_PACK_BUFFER_ARB, 0);
        return fb;
    }

    private void _PBOsendPixels_DMA(Item i, ROI packedROI) {
        if ((bstate & PACKED) == 0) {
            return;
        }
        if (!ROIcontext().ROI.isEmpty()) {
            if (!packedROI.ROI.isEmpty()) {
                GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, packedROI.ROI.x - ROIpack.ROI.x);
                GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, packedROI.ROI.y - ROIpack.ROI.y);
                GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, new ROI((DoubleBuffer) i.data).ROI.width);
                ARBPixelBufferObject.glBindBufferARB(ARBPixelBufferObject.GL_PIXEL_UNPACK_BUFFER_ARB, i.name);
                GLHandler._GLpushAttrib(GL11.GL_TEXTURE_BIT);
                GL11.glEnable(Sf3Texture._EXTtex);
                GL11.glBindTexture(Sf3Texture._EXTtex, GLHandler.sTex.firstItemBufferName(hashCode()));
                /**
                 * luminance texture is updated for displaying the stencil
                 * buffer that has been copied into one of the two r/w PBO
                 * dirtyArea should equal the ROIpack size, otherwise tears may
                 * appear on the texture.
                 */
                GL11.glTexSubImage2D(Sf3Texture._EXTtex, 0, packedROI.ROI.x - ROIpack.ROI.x, packedROI.ROI.y - ROIpack.ROI.y,
                        packedROI.ROI.width, packedROI.ROI.height, GL11.GL_LUMINANCE, GL11.GL_FLOAT, 0);
                GL11.glBindTexture(Sf3Texture._EXTtex, 0);
                GLHandler._GLpopAttrib();
                ARBPixelBufferObject.glBindBufferARB(ARBPixelBufferObject.GL_PIXEL_UNPACK_BUFFER_ARB, 0);
                Util.checkGLError();
                GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, 0);
                GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, 0);
                GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, 0);
                bstate = UNPACKED;
            }
        }
    }

    /**
     * packs the Stencil buffer contents into {@link #pboBufferR()} from current {@link #ROIcontext()}
     * on the specified dirtyArea. Something had to be rendered on StencilBuffer
     * within area of coords{ROIcontext().x,ROIcontext().y,winZ} when this
     * method is called. CAUTION : {@link GLHandler.ext#PixelBufferObjects} must
     * be available, the winZ-depth value is used with {@link Project#gluProject(float, float, float, java.nio.FloatBuffer, java.nio.FloatBuffer, java.nio.IntBuffer, java.nio.FloatBuffer)
     * } to get {@link #ROIpacked() packed}
     *
     * @param dirtyArea area in {@link #ROIcontext()} that is to be packed and
     * modified into the PBO
     */
    protected void readStencilBuffer_DMA(Rectangle dirtyArea) {
        if (!_stencilBlock.isOn()) {
            throw new JXAException(JXAException.LEVEL.SYSTEM, "must set stencil block active");
        }
        GLGeom._shrinkContent(ROI.ROI.getSize(), dirtyArea);
        /**
         * TODO : STREAM_MODE ; flickering issue due to incorrect handle of
         * glTexSubImage with source region (gl_unpack_pixel_*) not the same
         * size of the texture buffer. DYNAMIC_MODE : ok, but no PBO only wait
         * for each other, because it has to wait for the upload to finish
         * before to download from the texture buffer.
         */
        if ((bstate & (UNPACKED | CLEARED)) != 0 && mode == MODE.MODE_DYNAMIC) {
            PBOgetTex(GLHandler.sPbo.firstItem(hashCode()));
        }
        if (!dirtyArea.isEmpty()) {
            ROI dirtyAreaPixelPack = returnPixelPackedRegion(dirtyArea);

            PBOpackAreaTo_DMA(GLHandler.sPbo.firstItem(hashCode()), dirtyAreaPixelPack);
            if (mode == MODE.MODE_STREAM) {
                unpackStencilBuffer_DMA(dirtyAreaPixelPack);
            }
        }
    }

    /**
     * clears region of the Read-Pack PBO (0-put)
     *
     *
     * @param dirtyArea area to clear of its pixels in the PBO-R
     */
    protected void clearBufferObject(Rectangle dirtyArea) {
        if (!_stencilBlock.isOn()) {
            throw new JXAException(JXAException.LEVEL.SYSTEM, "must set stencil block active");
        }
        GLGeom._shrinkContent(ROI.ROI.getSize(), dirtyArea);
        if (!dirtyArea.isEmpty()) {
            ROI dirtyAreaPixelPack = returnPixelPackedRegion(dirtyArea);
            if ((bstate & PACKED) != 0) {
                PBOclearArea_noDMA(ARBPixelBufferObject.GL_PIXEL_PACK_BUFFER_ARB, GLHandler.sPbo.firstItem(hashCode()), dirtyAreaPixelPack);
            } else if ((bstate & UNPACKED) != 0) {
                PBOclearArea_DMA(dirtyAreaPixelPack);
            }
        }
    }

    /*
     * stencil is in "packed" state when at least one readStencilBuffer()
     * operation was successful.
     */
    public boolean isPacked() {
        return (bstate & PACKED) != 0;
    }

    /*
     * stencil is in "unpacked" state when at last one unpack() operation was
     * successful.
     */
    public boolean isUnpacked() {
        return (bstate & UNPACKED) != 0;
    }
    IntBuffer pack_viewport = BufferIO._newi(16);
    FloatBuffer pack_Rpos = BufferIO._newf(16);

    /**
     * the texels from glreadPixels window coordinates are copied in the texture
     * pixel buffer; therefore by rendering on a Java2D-style coordinates
     * system, the texture is transform flipped if this variable is set to true
     * (the default value for the RenderingSceneGL uses Java2D-style coords for
     * OpenGL) <br>This method affects the {@link #renderBufferObject(net.sf.jiga.xtended.impl.game.RenderingSceneGL, java.nio.FloatBuffer)}
     * method.
     */
    public static boolean isJava2DStyleCoordinates() {
        return RenderingScene.Java2DStyleCoordinates;
    }

    /**
     * sends stencil to the texture buffer
     */
    protected void unpackStencilBuffer_DMA(ROI packedArea) {
        PBOsendPixels_DMA(packedArea);
        PBOswap();
    }

    protected void unpackStencilBuffer_noDMA(ROI packedArea) {
        Item i = GLHandler.sPbo.firstItem(hashCode());
        FloatBuffer fb = PBOgetContents_noDMA(i);
        if (fb != null) {
            GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, packedArea.ROI.x - ROIpack.ROI.x);
            GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, packedArea.ROI.y - ROIpack.ROI.y);
            GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, new ROI((DoubleBuffer) i.data).ROI.width);
            GLHandler._GLpushAttrib(GL11.GL_TEXTURE_BIT);
            GL11.glEnable(Sf3Texture._EXTtex);
            GL11.glBindTexture(Sf3Texture._EXTtex, GLHandler.sTex.firstItemBufferName(hashCode()));
            /**
             * luminance texture is updated for displaying the stencil buffer
             * that has been copied into one of the two r/w PBO dirtyArea should
             * equal the ROIpack size, otherwise tears may appear on the
             * texture.
             */
            GL11.glTexSubImage2D(Sf3Texture._EXTtex, 0, 0, 0, ROIpack.ROI.width,
                    ROIpack.ROI.height, GL11.GL_LUMINANCE, GL11.GL_FLOAT, fb);
            GL11.glBindTexture(Sf3Texture._EXTtex, 0);
            GLHandler._GLpopAttrib();
            Util.checkGLError();
            GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, 0);
            GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, 0);
            GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, 0);
            bstate = UNPACKED;
        }
    }

    /**
     * renders as a texture on {@link #ROIcontext() ROI bounds}
     */
    protected void renderBufferObject(RenderingSceneGL scene, FloatBuffer colorBlend) {
        if (_stencilBlock.isOn()) {
            throw new JXAException(JXAException.LEVEL.SYSTEM, "stencil block active");
        }
        if (isUnpacked()) {
            GLHandler.__renderTexture(Sf3Texture._EXTtex, scene, true, hashCode(), 0, ROI.ROI, ROI.winZ, transform | (isJava2DStyleCoordinates() ? GLHandler._GL_TRANSFORM_FLIP_VERTICAL_BIT : 0), scaleArgs, rotateArgs, translateArgs, null, colorBlend);
        }
    }

    /**
     * (not used within actual rendering commands) To immediately clean the
     * stencil buffer ROI bounds
     */
    protected void cleanStencilBuffer(RenderingSceneGL scene, Rectangle dirtyArea) {
        GLHandler._GLpushAttrib(GL11.GL_STENCIL_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
        GL11.glStencilMask(GL11.GL_TRUE);
        GL11.glStencilFunc(GL11.GL_GREATER, 0, 1);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_REPLACE, GL11.GL_REPLACE);
        GL11.glColorMask(false, false, false, false);
        GLFX.beginRenderBlock();
        GLHandler._transformBegin(ROI.ROI, ROI.winZ, transform, scaleArgs, translateArgs, rotateArgs);
        GLGeom._GLfillRect(scene, hashCode(), true, dirtyArea, 1, 0);
        GLHandler._transformEnd();
        GLFX.endRenderBlock();
        GLHandler._GLpopAttrib();
    }

    /**
     * returns a Rectangle corresponding to the intersection with the current
     * ROI as a dirtyArea.
     *
     * @param area bounds that is set outside ROI but that may be packed as
     * dirtyArea.
     * @return inbounds area (= dirtyArea) corresponding to the specified
     * outbounds area intersection with ROI
     */
    public Rectangle ROIAsDirty(Rectangle area, double z) {
        assert z == ROI.winZ : "Specified area is not at the same z-depth.";
        Rectangle intersection = area.intersection(ROI.ROI);
        return new Rectangle(intersection.x - ROI.ROI.x, intersection.y - ROI.ROI.y, intersection.width, intersection.height);
    }

    /**
     * packs the Stencil Buffer contents in dirtyArea rectangle and immediately
     * starts upload to the texture if {@link MODE#MODE_STREAM} was selected
     *
     * @param dirtyArea area within the ROI (inside bounds)
     * @see #ROIAsDirty(java.awt.Rectangle, double)
     */
    public void GLpackStencil(Rectangle dirtyArea) {
        readStencilBuffer_DMA(dirtyArea);
    }

    /**
     * unpacks (= uploads) the PBO contents to the texture buffer if {@link MODE#MODE_DYNAMIC}
     * was selected. <br>cleans up the Stencil Buffer in the current {@link #ROIcontext()}
     */
    public void GLflush(RenderingSceneGL scene) {
        if (mode == MODE.MODE_DYNAMIC) {
            unpackStencilBuffer_DMA(ROIpacked());
        }
        cleanStencilBuffer(scene, ROIAsDirty(ROI.ROI, ROI.winZ));
    }

    /**
     * clears a region of the buffers
     *
     * @see #ROIAsDirty(java.awt.Rectangle, double)
     */
    public void GLclear(Rectangle dirtyArea) {
        clearBufferObject(dirtyArea);
    }

    /**
     * eventually {@link #GLflush(net.sf.jiga.xtended.impl.game.RenderingSceneGL) flushes}
     * the PBOs and renders to the current {@link #ROIcontext() bounds} with
     * blending colorBlend
     */
    public void GLrender(RenderingSceneGL scene, FloatBuffer colorBlend) {
        if ((bstate & UNPACKED) == 0) {
            GLflush(scene);
        }
        renderBufferObject(scene, colorBlend);
    }

    /**
     * deletes all buffers
     */
    public void GLdispose() {
        releaseBufferObject();
    }
}
