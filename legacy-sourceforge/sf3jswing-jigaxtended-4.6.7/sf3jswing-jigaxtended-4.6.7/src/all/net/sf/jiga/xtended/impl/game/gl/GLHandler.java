/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.game.gl;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.DataBuffer;
import java.nio.*;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import net.sf.jiga.xtended.JXAException;
import net.sf.jiga.xtended.impl.game.RenderingScene;
import net.sf.jiga.xtended.impl.game.gl.MemScratch.Item;
import net.sf.jiga.xtended.impl.system.BufferIO;
import net.sf.jiga.xtended.kernel.BitStack;
import net.sf.jiga.xtended.kernel.DebugMap;
import net.sf.jiga.xtended.kernel.JXAenvUtils;
import org.lwjgl.opengl.*;

/**
 * Toolkit for the openGL runtime launcher (i.e. <a href="http://www.lwjgl.org">
 * LWJGL</a>).
 *
 * @author www.b23prodtm.info
 */
public class GLHandler {

        /**
         * per-Thread Matrices Stacks for {@link GL11#GL_MODELVIEW_MATRIX} and
         * {@link GL11#GL_PROJECTION_MATRIX} stack pointers
         */
        public final static MatrixStack_tloc stView = new MatrixStack_tloc(GL11.GL_MODELVIEW_MATRIX), stProj = new MatrixStack_tloc(GL11.GL_PROJECTION_MATRIX);
        public static TexScratch sTex = new TexScratch();
        public static PBOscratch sPbo = new PBOscratch();
        public static VBOscratch sVbo = new VBOscratch();
        public static SndScratch sSnd = new SndScratch();

        /**
         * returns the current top of the stack matrix in a FloatBuffer
         *
         * @return the current top of the stack matrix in a FloatBuffer
         * @param stackPointer supported values are
         * {@linkplain GL11#GL_MODELVIEW_MATRIX}, {@linkplain GL11#GL_PROJECTION_MATRIX}
         * @see GL11#glGetFloat(int, FloatBuffer)
         */
        public static FloatBuffer _GLgetTopOftheStackMX(int stackPointer) {
                return MatrixStack_tloc._GLgetTopOftheStackMX(stackPointer);
        }

        /**
         * @param stackPointer supported values are
         * {@linkplain GL11#GL_MODELVIEW_MATRIX}, {@linkplain GL11#GL_PROJECTION_MATRIX}
         * @param mx 16 values matrix
         */
        public static void _GLloadMX(int stackPointer, FloatBuffer mx) {
                MatrixStack_tloc._GLloadMX(stackPointer, mx);
        }

        /**
         *
         * @return
         */
        public static FloatBuffer _GLgetCurrentRasterPos() {
                FloatBuffer rasterPos = BufferIO._newf(16);
                GL11.glGetFloat(GL11.GL_CURRENT_RASTER_POSITION, rasterPos);
                return rasterPos;
        }

        /**
         * This method loads up the full texture contents into the Video Card
         * RAM (VRAM). The textureHash hashCode is backed in a texBuffer map and
         * reused when the same textureHash is used later with the rendering
         * methods.
         *
         * @param texture
         * @see #_GLRenderTexture2D(RenderingScene, boolean, Sf3Texture,
         * Rectangle, int, DoubleBuffer, DoubleBuffer, DoubleBuffer,
         * FloatBuffer, FloatBuffer)
         * @return the GL buffer nameOrHash or 0 if textureHash was null or no
         * buffer could be created. If the texture size exceeds the
         * {#_GLgetMaxTextureSize() max texture size}, then it is a negative
         * signed integer, whose value is equal to the number of tiles that have
         * been loaded.
         * @param pboKeep want to keep a PBO for loading further Items ?
         * @param pboHash set a UID for the PBO, that will be reused in further
         * calls to load textures.
         * @param pboInput usually not in use, if false, the PBO is filled with
         * texture input first. <br>If true, PBOHash will be the PBO data input
         * (from outside of this method.
         */
        public static int _GLLoadVRAM2D(Sf3Texture texture, boolean pboKeep, int pboHash, boolean pboInput) {
                return _GLLoadVRAM2D(texture, new Dimension(texture.getWidth(), texture.getHeight()), pboKeep, pboHash, pboInput);
        }

        /**
         * This method loads up the full texture contents into the Video Card
         * RAM (VRAM). The textureHash hashCode is backed in a texBuffer map and
         * reused when the same textureHash is used later with the rendering
         * methods.
         *
         * @see #_GLRenderTexture2D(RenderingScene, boolean, Sf3Texture,
         * Rectangle, int, DoubleBuffer, DoubleBuffer, DoubleBuffer,
         * FloatBuffer, FloatBuffer)
         * @return the GL buffer nameOrHash or 0 if textureHash was null or no
         * buffer could be created. If the texture size exceeds the
         * {#_GLgetMaxTextureSize() max texture size}, then it is a negative
         * signed integer, whose value is equal to the number of tiles that have
         * been loaded.
         * @param texture
         * @param buffer use an explicit texture nameOrHash (it will be loaded
         * even if the buffer is already used); 0 will force the use a new
         * buffer nameOrHash
         * @param pboKeep want to keep a PBO for loading further Items ?
         * @param pboHash set a UID for the PBO, that will be reused in further
         * calls to load textures.
         * @param pboInput usually not in use, if false, the PBO is filled with
         * texture input first. <br>If true, PBOHash will be the PBO data input
         * (from outside of this method.
         */
        public static int _GLLoadVRAM2D(Sf3Texture texture, int buffer, boolean pboKeep, int pboHash, boolean pboInput) {
                return texture != null ? _GLLoadVRAM2D(texture, new Dimension(texture.getWidth(), texture.getHeight()), buffer, true, pboKeep, pboHash, pboInput) : 0;
        }

        /**
         * make tiles from a texture (use this if your texture is too big)
         *
         * @param texture
         * @param tiles
         * @param pboKeep want to keep a PBO for loading further Items ?
         * @param pboHash set a UID for the PBO, that will be reused in further
         * calls to load textures.
         * @param pboInput usually not in use, if false, the PBO is filled with
         * texture input first. <br>If true, PBOHash will be the PBO data input
         * (from outside of this method.
         * @return some integer (negative value for a tiled texture)
         */
        public static int _GLLoadVRAM2D(Sf3Texture texture, Dimension tiles, boolean pboKeep, int pboHash, boolean pboInput) {
                return _GLLoadVRAM2D(texture, tiles, 0, false, pboKeep, pboHash, pboInput);
        }

        private static void put(int bufferDataType, Buffer pixelBuffer, int px_tile, Object pixel) {
                switch (bufferDataType) {
                        case DataBuffer.TYPE_BYTE:
                                ByteBuffer.class.cast(pixelBuffer).put(px_tile, byte.class.cast(pixel));
                                break;
                        case DataBuffer.TYPE_DOUBLE:
                                DoubleBuffer.class.cast(pixelBuffer).put(px_tile, double.class.cast(pixel));
                                break;
                        case DataBuffer.TYPE_FLOAT:
                                FloatBuffer.class.cast(pixelBuffer).put(px_tile, float.class.cast(pixel));
                                break;
                        case DataBuffer.TYPE_SHORT:
                        case DataBuffer.TYPE_USHORT:
                                ShortBuffer.class.cast(pixelBuffer).put(px_tile, short.class.cast(pixel));
                                break;
                        case DataBuffer.TYPE_INT:
                                IntBuffer.class.cast(pixelBuffer).put(px_tile, int.class.cast(pixel));
                                break;
                        default:
                                throw new JXAException(JXAException.LEVEL.APP, "unknown/unsupported datatype : " + bufferDataType + " of " + pixelBuffer);
                }
        }

        private static void put(int bufferDataType, ByteBuffer pixelBuffer, Buffer dataBuffer) {
                switch (bufferDataType) {
                        case DataBuffer.TYPE_BYTE:
                                pixelBuffer.put(ByteBuffer.class.cast(dataBuffer));
                                break;
                        case DataBuffer.TYPE_DOUBLE:
                                pixelBuffer.asDoubleBuffer().put(DoubleBuffer.class.cast(dataBuffer));
                                break;
                        case DataBuffer.TYPE_FLOAT:
                                pixelBuffer.asFloatBuffer().put(FloatBuffer.class.cast(dataBuffer));
                                break;
                        case DataBuffer.TYPE_SHORT:
                        case DataBuffer.TYPE_USHORT:
                                pixelBuffer.asShortBuffer().put(ShortBuffer.class.cast(dataBuffer));
                                break;
                        case DataBuffer.TYPE_INT:
                                pixelBuffer.asIntBuffer().put(IntBuffer.class.cast(dataBuffer));
                                break;
                        default:
                                throw new JXAException(JXAException.LEVEL.APP, "unknown/unsupported datatype : " + bufferDataType + " of " + pixelBuffer);
                }
        }

        private static Object get(int bufferDataType, Buffer dataBuffer, int pixelPos) {
                Object r;
                switch (bufferDataType) {
                        case DataBuffer.TYPE_BYTE:
                                r = ByteBuffer.class.cast(dataBuffer).get(pixelPos);
                                break;
                        case DataBuffer.TYPE_DOUBLE:
                                r = DoubleBuffer.class.cast(dataBuffer).get(pixelPos);
                                break;
                        case DataBuffer.TYPE_FLOAT:
                                r = FloatBuffer.class.cast(dataBuffer).get(pixelPos);
                                break;
                        case DataBuffer.TYPE_SHORT:
                        case DataBuffer.TYPE_USHORT:
                                r = ShortBuffer.class.cast(dataBuffer).get(pixelPos);
                                break;
                        case DataBuffer.TYPE_INT:
                                r = IntBuffer.class.cast(dataBuffer).get(pixelPos);
                                break;
                        default:
                                throw new JXAException(JXAException.LEVEL.APP, "unknown/unsupported datatype : " + bufferDataType + " of " + dataBuffer);
                }
                return r;
        }

        private static void glloadTexture(Sf3Texture texture) {
                switch (texture.getPixelBufferDataType()) {
                        case DataBuffer.TYPE_BYTE:
                                GL11.glTexImage2D(Sf3Texture._EXTtex, 0, texture.getRenderFormat(), texture.getWidth(), texture.getHeight(), 0, texture.getEXTabgr(), texture.getTextureBufferDataType(), (ByteBuffer) texture.getPixelBuffer());
                                if (JXAenvUtils._debug) {
                                        System.out.println("xxxx VRAM xxxx BYTE tex2D");
                                }
                                break;
                        case DataBuffer.TYPE_DOUBLE:
                                GL11.glTexImage2D(Sf3Texture._EXTtex, 0, texture.getRenderFormat(), texture.getWidth(), texture.getHeight(), 0, texture.getEXTabgr(), GL11.GL_DOUBLE, (DoubleBuffer) texture.getPixelBuffer());
                                if (JXAenvUtils._debug) {
                                        System.out.println("xxxx VRAM xxxx DOUBLE tex2D");
                                }
                                break;
                        case DataBuffer.TYPE_FLOAT:
                                GL11.glTexImage2D(Sf3Texture._EXTtex, 0, texture.getRenderFormat(), texture.getWidth(), texture.getHeight(), 0, texture.getEXTabgr(), GL11.GL_FLOAT, (FloatBuffer) texture.getPixelBuffer());
                                if (JXAenvUtils._debug) {
                                        System.out.println("xxxx VRAM xxxx FLOAT tex2D");
                                }
                                break;
                        case DataBuffer.TYPE_INT:
                                GL11.glTexImage2D(Sf3Texture._EXTtex, 0, texture.getRenderFormat(), texture.getWidth(), texture.getHeight(), 0, texture.getEXTabgr(), GL11.GL_UNSIGNED_INT, (IntBuffer) texture.getPixelBuffer());
                                if (JXAenvUtils._debug) {
                                        System.out.println("xxxx VRAM xxxx INT tex2D");
                                }
                                break;
                        case DataBuffer.TYPE_USHORT:
                        case DataBuffer.TYPE_SHORT:
                                GL11.glTexImage2D(Sf3Texture._EXTtex, 0, texture.getRenderFormat(), texture.getWidth(), texture.getHeight(), 0, texture.getEXTabgr(), GL11.GL_UNSIGNED_SHORT, (ShortBuffer) texture.getPixelBuffer());
                                if (JXAenvUtils._debug) {
                                        System.out.println("xxxx VRAM xxxx SHORT tex2D");
                                }
                                break;
                        default:
                                throw new JXAException("incompatible or unknown BufferedImage type :" + texture.toString());
                }
        }

        private static void glloadTexture3D(Sf3Texture3D texture) {
                switch (texture.getPixelBufferDataType()) {
                        case DataBuffer.TYPE_BYTE:
                                GL12.glTexImage3D(GL12.GL_TEXTURE_3D, 0, texture.getRenderFormat(), texture.getWidth(), texture.getHeight(), texture.getDepth(), 0, texture.getEXTabgr(), texture.getTextureBufferDataType(), texture.accessPixelBuffer().limit() == 0 ? null : ((ByteBuffer) texture.getPixelBuffer()));
                                if (JXAenvUtils._debug) {
                                        System.out.println("xxxx VRAM xxxx BYTE tex3D");
                                }
                                break;
                        case DataBuffer.TYPE_DOUBLE:
                                GL12.glTexImage3D(GL12.GL_TEXTURE_3D, 0, texture.getRenderFormat(), texture.getWidth(), texture.getHeight(), texture.getDepth(), 0, texture.getEXTabgr(), texture.getTextureBufferDataType(), texture.accessPixelBuffer().limit() == 0 ? null : ((DoubleBuffer) texture.getPixelBuffer()));
                                if (JXAenvUtils._debug) {
                                        System.out.println("xxxx VRAM xxxx DOUBLE tex3D");
                                }
                                break;
                        case DataBuffer.TYPE_FLOAT:
                                GL12.glTexImage3D(GL12.GL_TEXTURE_3D, 0, texture.getRenderFormat(), texture.getWidth(), texture.getHeight(), texture.getDepth(), 0, texture.getEXTabgr(), texture.getTextureBufferDataType(), texture.accessPixelBuffer().limit() == 0 ? null : ((FloatBuffer) texture.getPixelBuffer()));
                                if (JXAenvUtils._debug) {
                                        System.out.println("xxxx VRAM xxxx FLOAT tex3D");
                                }
                                break;
                        case DataBuffer.TYPE_INT:
                                GL12.glTexImage3D(GL12.GL_TEXTURE_3D, 0, texture.getRenderFormat(), texture.getWidth(), texture.getHeight(), texture.getDepth(), 0, texture.getEXTabgr(), texture.getTextureBufferDataType(), texture.accessPixelBuffer().limit() == 0 ? null : ((IntBuffer) texture.getPixelBuffer()));
                                if (JXAenvUtils._debug) {
                                        System.out.println("xxxx VRAM xxxx INT tex3D");
                                }
                                break;
                        case DataBuffer.TYPE_USHORT:
                        case DataBuffer.TYPE_SHORT:
                                GL12.glTexImage3D(GL12.GL_TEXTURE_3D, 0, texture.getRenderFormat(), texture.getWidth(), texture.getHeight(), texture.getDepth(), 0, texture.getEXTabgr(), texture.getTextureBufferDataType(), texture.accessPixelBuffer().limit() == 0 ? null : ((ShortBuffer) texture.getPixelBuffer()));
                                if (JXAenvUtils._debug) {
                                        System.out.println("xxxx VRAM xxxx SHORT tex3D");
                                }
                                break;
                        default:
                                throw new JXAException("incompatible or unknown BufferedImage type :" + texture.toString());
                }
        }

        private static void glloadSubTexture3D(Sf3Texture texture2D, int rCoord, int width, int height) {
                switch (texture2D.getPixelBufferDataType()) {
                        case DataBuffer.TYPE_BYTE:
                                GL12.glTexSubImage3D(GL12.GL_TEXTURE_3D, 0, 0, 0, rCoord, width, height, 1, texture2D.getEXTabgr(), texture2D.getTextureBufferDataType(), ((ByteBuffer) texture2D.getPixelBuffer()));
                                if (JXAenvUtils._debug) {
                                        System.out.println("xxxx VRAM xxxx BYTE tex3D");
                                }
                                break;
                        case DataBuffer.TYPE_DOUBLE:
                                GL12.glTexSubImage3D(GL12.GL_TEXTURE_3D, 0, 0, 0, rCoord, width, height, 1, texture2D.getEXTabgr(), texture2D.getTextureBufferDataType(), ((DoubleBuffer) texture2D.getPixelBuffer()));
                                if (JXAenvUtils._debug) {
                                        System.out.println("xxxx VRAM xxxx DOUBLE tex3D");
                                }
                                break;
                        case DataBuffer.TYPE_FLOAT:
                                GL12.glTexSubImage3D(GL12.GL_TEXTURE_3D, 0, 0, 0, rCoord, width, height, 1, texture2D.getEXTabgr(), texture2D.getTextureBufferDataType(), ((FloatBuffer) texture2D.getPixelBuffer()));
                                if (JXAenvUtils._debug) {
                                        System.out.println("xxxx VRAM xxxx FLOAT tex3D");
                                }
                                break;
                        case DataBuffer.TYPE_INT:
                                GL12.glTexSubImage3D(GL12.GL_TEXTURE_3D, 0, 0, 0, rCoord, width, height, 1, texture2D.getEXTabgr(), texture2D.getTextureBufferDataType(), ((IntBuffer) texture2D.getPixelBuffer()));
                                if (JXAenvUtils._debug) {
                                        System.out.println("xxxx VRAM xxxx INT tex3D");
                                }
                                break;
                        case DataBuffer.TYPE_USHORT:
                        case DataBuffer.TYPE_SHORT:
                                GL12.glTexSubImage3D(GL12.GL_TEXTURE_3D, 0, 0, 0, rCoord, width, height, 1, texture2D.getEXTabgr(), texture2D.getTextureBufferDataType(), ((ShortBuffer) texture2D.getPixelBuffer()));
                                if (JXAenvUtils._debug) {
                                        System.out.println("xxxx VRAM xxxx SHORT tex3D");
                                }
                                break;
                        default:
                                throw new JXAException("incompatible or unknown BufferedImage type :" + texture2D.toString());
                }
        }

        private static void r_loadTiledTexture(Sf3Texture texture, Dimension tiles, int rows, int columns,
                int tileY,
                Buffer dataBuffer) {
                if (tileY >= columns) {
                        return;
                }
                r_loadTiledTexture_row(texture, tiles, rows, columns, 0, tileY, dataBuffer);
                r_loadTiledTexture(texture, tiles, rows, columns, tileY + 1, dataBuffer);
        }

        private static void r_loadTiledTexture_row(Sf3Texture texture, Dimension tiles, int rows, int columns,
                int tileX, int tileY,
                Buffer dataBuffer) {
                int bands = texture.getBands();
                int length = tiles.width * texture.getBands() * tiles.height;
                Buffer pixelBuffer = BufferIO._new(length, true, dataBuffer.getClass());
                if (tileX >= tiles.width) {
                        return;
                }
                r_loadTextureTile(texture, tiles, bands, dataBuffer, tileX, tileY, 0, pixelBuffer);
                Sf3Texture tile = new Sf3Texture(pixelBuffer, tiles.width, tiles.height, bands, tiles, 0);
                tile.setPty(texture.getPty());
                _GLLoadVRAM2DasTile(texture.hashCode(), texture.getWidth(), texture.getHeight(), tile, tileX, tileY);
                pixelBuffer.rewind();

                r_loadTiledTexture_row(texture, tiles, rows, columns, tileX + 1, tileY, dataBuffer);
        }

        private static void loadTiledTexture(Sf3Texture texture, Dimension tiles,
                Buffer dataBuffer) {
                int tilesRow = (int) Math.ceil((float) texture.getWidth() / (float) tiles.width);
                int tilesCol = (int) Math.ceil((float) texture.getHeight() / (float) tiles.height);
                sTex.genVRAMBuffersMap(tilesRow * tilesCol);
                dataBuffer.rewind();
                
                r_loadTiledTexture(texture, tiles, tilesRow, tilesCol, 0, dataBuffer);
        }

        private static void r_loadTextureTile_band(Sf3Texture texture, Dimension tiles, int bands, Buffer dataBuffer,
                int tileX, int tileY, int band, int xT, int yT,
                Buffer pixelBuffer) {
                if (band >= bands) {
                        return;
                }
                int px_xoff = tileX * tiles.width + xT;
                int px = bands * (px_xoff + (tileY * tiles.height + yT) * texture.getWidth()) + band;
                int px_tile = bands * (xT + yT * tiles.width) + band;
                Object pVal;
                if (dataBuffer.limit() > px && px_xoff < texture.getWidth() - 1) {
                        pVal = get(texture.getPixelBufferDataType(), dataBuffer, px);
                } else {
                        pVal = 0;
                }
                put(texture.getPixelBufferDataType(), pixelBuffer, px_tile, pVal);
                
                r_loadTextureTile_band(texture, tiles, bands, dataBuffer, tileX, tileY, band + 1, xT, yT, pixelBuffer);
        }

        private static void r_loadTextureTile_line(Sf3Texture texture, Dimension tiles, int bands, Buffer dataBuffer,
                int tileX, int tileY, int xT, int yT,
                Buffer pixelBuffer) {
                if (xT >= tiles.width) {
                        return;
                }
                r_loadTextureTile_band(texture, tiles, bands, dataBuffer, tileX, tileY, 0, xT, yT, pixelBuffer);
                
                r_loadTextureTile_line(texture, tiles, bands, dataBuffer, tileX, tileY, xT + 1, yT, pixelBuffer);
        }

        private static void r_loadTextureTile(Sf3Texture texture, Dimension tiles, int bands, Buffer dataBuffer,
                int tileX, int tileY, int yT,
                Buffer pixelBuffer) {
                if (yT >= tiles.height) {
                        return;
                }
                r_loadTextureTile_line(texture, tiles, bands, dataBuffer, tileX, tileY, 0, yT, pixelBuffer);
                
                r_loadTextureTile(texture, tiles, bands, dataBuffer, tileX, tileY, yT + 1, pixelBuffer);
        }

        /**
         * @param pboKeep want to keep a PBO for loading further Items ?
         * @param pboHash set a UID for the PBO, that will be reused in further
         * calls to load textures.
         * @param pboInput usually not in use, if false, the PBO is filled pif
         * true, PBOHash will be the PBO data input (from outside of this
         * method..
         */
        private static int _GLLoadVRAM2D(Sf3Texture texture, Dimension tiles, int buffer, boolean force, boolean pboKeep, int pboHash, boolean pboInput) {
                if (texture != null) {
                        if (tiles.width < texture.getWidth() || tiles.height < texture.getHeight()) {
                                if (JXAenvUtils._debug) {
                                        System.out.println("@@@@ TILE MODE load tiles-textures from full texture...");
                                }
                                tiles.width = Sf3Texture._getNextPowerOfTwo(tiles.width);
                                tiles.height = Sf3Texture._getNextPowerOfTwo(tiles.height);
                                texture.validatePixelBuffer();
                                Buffer dataBuffer = texture.accessPixelBuffer();
                                loadTiledTexture(texture, tiles, dataBuffer);
                                dataBuffer.rewind();
                                if (JXAenvUtils._debug) {
                                        System.out.println("@@@@ done.");
                                }
                                return -sTex.getItems(texture.hashCode()).size();
                                /**
                                 * return a negative value for a tiled texture
                                 */
                        }
                        if (texture.getWidth() > _GLgetMaxTextureSize() || texture.getHeight() > _GLgetMaxTextureSize()) {
                                if (JXAenvUtils._debug) {
                                        System.out.println("texture size is " + texture.getWidth() + "x" + texture.getHeight() + " but max texture size is " + _GLgetMaxTextureSize() + ".");
                                }
                                return 0;
                        }
                        /*
                         * create OpenGL counterpart if none has been already allocated
                         */
                        if (!sTex.isTextureLoaded(texture.hashCode()) || force) {
                                GLHandler._GLpushAttrib(GL11.GL_TEXTURE_BIT);
                                GL11.glEnable(Sf3Texture._EXTtex);
                                if (buffer == 0) {
                                        buffer = sTex.getVRAMFindVacant();
                                }
                                GL11.glBindTexture(Sf3Texture._EXTtex, buffer);
                                GL11.glTexParameteri(Sf3Texture._EXTtex, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
                                GL11.glTexParameteri(Sf3Texture._EXTtex, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
                                GL11.glTexParameteri(Sf3Texture._EXTtex, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
                                GL11.glTexParameteri(Sf3Texture._EXTtex, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
                                sTex.addItem(texture.hashCode(), new Item(buffer, texture.toTexScratchDataStore()));
                                texture.validatePixelBuffer();
                                /**
                                 * PBO loading
                                 */
                                if (ext.PixelBufferObjects.GL_isAvailable()) {
                                        if (!sPbo.hasItem(pboHash)) {
                                                sPbo.genVRAMBuffersMap(1);
                                                sPbo.addItem(pboHash, new Item(sPbo.getVRAMFindVacant(), BufferIO._new(0)));
                                        }
                                        Item pboItem = sPbo.firstItem(pboHash);
                                        ARBPixelBufferObject.glBindBufferARB(ARBPixelBufferObject.GL_PIXEL_UNPACK_BUFFER_ARB, pboItem.name);
                                        if (!pboInput) {
                                                ARBPixelBufferObject.glBufferDataARB(ARBPixelBufferObject.GL_PIXEL_UNPACK_BUFFER_ARB, texture.getTextureBufferSize() * BufferIO.Data._findType(texture.accessPixelBuffer()).byteSize, ARBPixelBufferObject.GL_STREAM_COPY_ARB);
                                                sPbo.firstItem(pboHash).data = ARBPixelBufferObject.glMapBufferARB(ARBPixelBufferObject.GL_PIXEL_UNPACK_BUFFER_ARB, ARBPixelBufferObject.GL_WRITE_ONLY_ARB, texture.getTextureBufferSize() * BufferIO.Data._findType(texture.accessPixelBuffer()).byteSize, null);
                                        }
                                }
                                if (ext.PixelBufferObjects.GL_isAvailable()) {
                                        /**
                                         * PBO loading
                                         */
                                        if (!pboInput) {
                                                ByteBuffer b = (ByteBuffer) sPbo.firstItem(pboHash).data;
                                                put(texture.getPixelBufferDataType(), b, texture.getPixelBuffer());
                                                b.flip();
                                                ARBPixelBufferObject.glUnmapBufferARB(ARBPixelBufferObject.GL_PIXEL_UNPACK_BUFFER_ARB);
                                        }
                                        GL11.glTexImage2D(Sf3Texture._EXTtex, 0, texture.getRenderFormat(), texture.getWidth(), texture.getHeight(), 0, texture.getEXTabgr(), texture.getTextureBufferDataType(), 0);
                                } else {
                                        glloadTexture(texture);
                                }
                                GL11.glBindTexture(Sf3Texture._EXTtex, 0);
                                if (ext.PixelBufferObjects.GL_isAvailable()) {
                                        ARBPixelBufferObject.glBindBufferARB(ARBPixelBufferObject.GL_PIXEL_UNPACK_BUFFER_ARB, 0);
                                        if (!pboKeep) {
                                                sPbo.removeItems(pboHash);
                                        }
                                }
                                if (JXAenvUtils._debug) {
                                        System.out.println("xxxx VRAM xxxx new TEX2D input : " + texture + " bufferSize : " + sTex.getItemScratchSize() + "/" + sTex.getItemScratchCapacity());
                                }
                                GL11.glPrioritizeTextures(BufferIO._wrapi(new int[]{buffer}), BufferIO._wrapf(new float[]{texture.getPty()}));
                                GLHandler._GLpopAttrib();
                                Util.checkGLError();
                        } else {
                                if (JXAenvUtils._debug) {
                                        System.err.println(JXAenvUtils.log("xxxx " + texture.toString() + " xxxx TEX2D already loaded", JXAenvUtils.LVL.APP_WRN));
                                }
                                buffer = sTex.firstItemBufferName(texture.hashCode());
                        }
                }
                return buffer;
        }

        /**
         * recognized formats are :
         * {@linkplain GL11#GL_RGBA}, {@linkplain GL11#GL_RGB}, {@linkplain GL12#GL_BGRA}, {@linkplain GL12#GL_BGR}
         * and {@linkplain EXTAbgr.GL_ABGR_EXT}
         *
         * @param format buffer in one of the recognized GL formats or use 0 if
         * you think the buffer is ABGR.
         * @param size
         * @param buffer pixel data in the specified format NOTiCE: JXA always
         * uses GL_UNPACK_ALIGNEMENT with value 1 (byte alignement)
         * @return
         */
        public static Sf3Texture newTexture(Buffer buffer, Dimension size, int format) {
                return newTexture((int) System.nanoTime(), buffer, size, format);
        }

        public static Sf3Texture newTexture(int hashCode, Buffer buffer, Dimension size, int format) {
                int transferMode = 0;
                int bands = 4;
                switch (format) {
                        case GL11.GL_RGBA:
                                transferMode = Sf3Texture.TRANSFER_BGRA_TO_RGBA;
                                break;
                        case GL11.GL_RGB:
                                transferMode = Sf3Texture.TRANSFER_BGR_TO_RGB;
                                break;
                        case GL12.GL_BGRA:
                                transferMode = Sf3Texture.TRANSFER_ABGR_TO_BGRA;
                                break;
                        case GL12.GL_BGR:
                                transferMode = Sf3Texture.TRANSFER_RGB_TO_BGR;
                                break;
                        case EXTAbgr.GL_ABGR_EXT:
                        case 0:
                                transferMode = Sf3Texture.TRANSFER_RGBA_TO_ABGR;
                                break;
                        default:
                                if (JXAenvUtils._debug) {
                                        System.err.println(JXAenvUtils.log("no recognized format were specified", JXAenvUtils.LVL.APP_WRN));
                                }
                                break;
                }
                if ((transferMode & Sf3Texture.TRANSFER_3_BANDS_BIT) != 0) {
                        bands = 3;
                }
                return new Sf3Texture(hashCode, buffer, size.width, size.height, bands, size, transferMode);
        }

        /**
         * after that, rendering the texture refHash will load all tiles loaded
         * with this refHash and try to render all tiles concatenated in a grid
         *
         * @param refHash the hashcode that will be used to retrieve the tiles
         * map afterwards
         * @param refWidth the width of the whole tiled texture
         * @param refHeight the height of the whole tiled texture
         * @param tile the tile to load
         * @param tileX
         * @param tileY
         * @return
         */
        public static int _GLLoadVRAM2DasTile(int refHash, int refWidth, int refHeight, Sf3Texture tile, int tileX, int tileY) {
                List<Item<IntBuffer>> tileMap = sTex.hasItem(refHash) ? sTex.getItems(refHash) : sTex._newSet();
                /*Note about using tiles items : Tile is loaded here with a new unique (tile.hashCode()) texture.*/
                _GLLoadVRAM2D(tile, true, refHash, false);
                Sf3TextureTile tileTex = new Sf3TextureTile(refHash, new Dimension(refWidth, refHeight), tile, tileX, tileY);
                /*Note about using tiles items : All Item.name's are hashcodes in the reference (refHash of Sprite) set of tiled TexScratch items. {@link TexScratch#delete(java.nio.IntBuffer)}.*/
                sTex.addItem(refHash, new Item(tile.hashCode(), tileTex.toTexScratchDataStore()));
                return -tileMap.size();
        }

        /**
         * returs true or false, whether a textureHash buffer has been found and
         * deleted or not, resp.
         *
         * @param texture
         * @return true or false, whether the textureHash buffer link has been
         * found and deleted or not, resp.
         */
        public static boolean _GLUnloadVRAM(Sf3Texture texture) {
                return _GLUnloadVRAM(texture.hashCode());
        }

        /**
         * returs true or false, whether a textureHash buffer has been found and
         * deleted or not, resp.
         *
         * @param textureHash the textureHash hash code.
         * @return true or false, whether the textureHash buffer link has been
         * found and deleted or not, resp.
         */
        public static boolean _GLUnloadVRAM(int textureHash) {
                return sTex.removeItems(textureHash);
        }

        /**
         *
         * @param texture add as an animation layer in the 3D texture
         * @param tex3DHash 3D texture hash link
         * @param depth layer depth which to add the texture at
         * @return
         */
        public static int _GLLoadVRAM3D(Sf3Texture texture, int tex3DHash, int depth) {
                return __GLLoadVRAM3D(texture, tex3DHash, depth, true, tex3DHash);
        }

        /**
         * @param texture load as 3D texture
         * @return
         */
        public static int _GLLoadVRAM3D(Sf3Texture3D texture, boolean pboKeep, int pboHash) {
                return __GLLoadVRAM3D(texture, texture.hashCode(), texture.getDepth(), pboKeep, pboHash);
        }

        private static int __GLLoadVRAM3D(Object textureObj, int hash, int rCoord, boolean pboKeep, int pboHash) {
                Sf3Texture3D texture = null;
                Sf3Texture texture2D = null;
                int buffer = 0;
                GLHandler._GLpushAttrib(GL11.GL_TEXTURE_BIT);
                Util.checkGLError();
                GL11.glEnable(GL12.GL_TEXTURE_3D);
                if (textureObj instanceof Sf3Texture3D) {
                        texture = (Sf3Texture3D) textureObj;
                        if (texture.getWidth() > _GLgetMax3DTextureSize() || texture.getHeight() > _GLgetMax3DTextureSize()) {
                                throw new IllegalArgumentException("texture size is " + texture.getWidth() + "x" + texture.getHeight() + " but max texture size is " + _GLgetMax3DTextureSize());
                        }
                } else if (textureObj instanceof Sf3Texture) {
                        texture2D = (Sf3Texture) textureObj;
                        buffer = sTex.firstItemBufferName(hash);
                        if (texture2D.getWidth() > _GLgetMax3DTextureSize() || texture2D.getHeight() > _GLgetMax3DTextureSize()) {
                                throw new IllegalArgumentException("texture size is " + texture2D.getWidth() + "x" + texture2D.getHeight() + " but max texture size is " + _GLgetMaxTextureSize());
                        }
                }
                if (texture != null) {
                        /*
                         * create OpenGL counterpart if none has been already allocated
                         */
                        if (!sTex.isTextureLoaded(hash)) {
                                buffer = sTex.getVRAMFindVacant();
                                GL11.glBindTexture(GL12.GL_TEXTURE_3D, buffer);
                                GL11.glTexParameteri(GL12.GL_TEXTURE_3D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
                                GL11.glTexParameteri(GL12.GL_TEXTURE_3D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
                                GL11.glTexParameteri(GL12.GL_TEXTURE_3D, GL12.GL_TEXTURE_WRAP_R, GL11.GL_CLAMP);
                                GL11.glTexParameteri(GL12.GL_TEXTURE_3D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
                                GL11.glTexParameteri(GL12.GL_TEXTURE_3D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
                                sTex.addItem(texture.hashCode(), new Item(buffer, texture.toTexScratchDataStore()));
                                /**
                                 * PBO loading
                                 */
                                if (ext.PixelBufferObjects.GL_isAvailable() && texture.accessPixelBuffer().limit() != 0) {
                                        if (!sPbo.hasItem(pboHash)) {
                                                sPbo.genVRAMBuffersMap(1);
                                                sPbo.addItem(pboHash, new Item(sPbo.getVRAMFindVacant(), BufferIO._new(0)));
                                        }
                                        Item pboItem = sPbo.firstItem(pboHash);
                                        ARBPixelBufferObject.glBindBufferARB(ARBPixelBufferObject.GL_PIXEL_UNPACK_BUFFER_ARB, pboItem.name);
                                        ARBPixelBufferObject.glBufferDataARB(ARBPixelBufferObject.GL_PIXEL_UNPACK_BUFFER_ARB, texture.accessPixelBuffer().limit() * BufferIO.Data._findType(texture.accessPixelBuffer()).byteSize, ARBPixelBufferObject.GL_STREAM_COPY_ARB);
                                        sPbo.firstItem(pboHash).data = ARBPixelBufferObject.glMapBufferARB(ARBPixelBufferObject.GL_PIXEL_UNPACK_BUFFER_ARB, ARBPixelBufferObject.GL_WRITE_ONLY_ARB, texture.accessPixelBuffer().limit() * BufferIO.Data._findType(texture.accessPixelBuffer()).byteSize, null);
                                }
                                /**
                                 * PBO loading
                                 */
                                if (ext.PixelBufferObjects.GL_isAvailable() && texture.accessPixelBuffer().limit() != 0) {
                                        ByteBuffer b = (ByteBuffer) sPbo.firstItem(pboHash).data;
                                        put(texture.getPixelBufferDataType(), b, texture.getPixelBuffer());
                                        b.flip();
                                        ARBPixelBufferObject.glUnmapBufferARB(ARBPixelBufferObject.GL_PIXEL_UNPACK_BUFFER_ARB);
                                        GL12.glTexImage3D(GL12.GL_TEXTURE_3D, 0, texture.getRenderFormat(), texture.getWidth(), texture.getHeight(), texture.getDepth(), 0, texture.getEXTabgr(), texture.getTextureBufferDataType(), 0);
                                } else {
                                        glloadTexture3D(texture);
                                }
                                if (JXAenvUtils._debug) {
                                        System.out.println("xxxx VRAM xxxx BYTE tex3D");
                                }
                                GL11.glBindTexture(GL12.GL_TEXTURE_3D, 0);
                                if (ext.PixelBufferObjects.GL_isAvailable() && texture.accessPixelBuffer().limit() != 0) {
                                        ARBPixelBufferObject.glBindBufferARB(ARBPixelBufferObject.GL_PIXEL_UNPACK_BUFFER_ARB, 0);
                                        if (!pboKeep) {
                                                sPbo.removeItems(pboHash);
                                        }
                                }
                                if (JXAenvUtils._debug) {
                                        System.out.println("xxxx VRAM xxxx new TEX3D input : " + texture + " bufferSize : " + sTex.getItemScratchSize() + "/" + sTex.getItemScratchCapacity());
                                }
                                GL11.glPrioritizeTextures(BufferIO._wrapi(new int[]{buffer}), BufferIO._wrapf(new float[]{texture.getPty()}));
                        } else {
                                buffer = sTex.firstItemBufferName(hash);
                        }
                } else if (texture2D != null) {
                        GL11.glBindTexture(GL12.GL_TEXTURE_3D, buffer);
                        IntBuffer dim = sTex.firstItem(hash).data;
                        sTex.addItem(texture2D.hashCode(), new Item(buffer, BufferIO._wrapi(new int[]{dim.get(0), dim.get(1), dim.get(2), dim.get(3), rCoord})));
                        texture2D.validatePixelBuffer();
                        /**
                         * PBO loading
                         */
                        if (ext.PixelBufferObjects.GL_isAvailable()) {
                                if (!sPbo.hasItem(pboHash)) {
                                        sPbo.genVRAMBuffersMap(1);
                                        sPbo.addItem(pboHash, new Item(sPbo.getVRAMFindVacant(), BufferIO._new(0)));
                                }
                                Item pboItem = sPbo.firstItem(pboHash);
                                ARBPixelBufferObject.glBindBufferARB(ARBPixelBufferObject.GL_PIXEL_UNPACK_BUFFER_ARB, pboItem.name);
                                ARBPixelBufferObject.glBufferDataARB(ARBPixelBufferObject.GL_PIXEL_UNPACK_BUFFER_ARB, texture2D.accessPixelBuffer().limit() * BufferIO.Data._findType(texture2D.accessPixelBuffer()).byteSize, ARBPixelBufferObject.GL_STREAM_COPY_ARB);
                                sPbo.firstItem(pboHash).data = ARBPixelBufferObject.glMapBufferARB(ARBPixelBufferObject.GL_PIXEL_UNPACK_BUFFER_ARB, ARBPixelBufferObject.GL_WRITE_ONLY_ARB, texture2D.accessPixelBuffer().limit() * BufferIO.Data._findType(texture2D.accessPixelBuffer()).byteSize, null);
                        }
                        /**
                         * add the texture as a layer for animation it is added
                         * several times for good quality
                         */
                        for (int i = 0; i < Sf3Texture3D._3DTexLayersAmount; i++) {
                                /**
                                 * PBO loading
                                 */
                                if (ext.PixelBufferObjects.GL_isAvailable()) {
                                        ByteBuffer b = (ByteBuffer) sPbo.firstItem(pboHash).data;
                                        if (i == 0) {
                                                put(texture2D.getPixelBufferDataType(), b, texture2D.getPixelBuffer());
                                                b.flip();
                                                ARBPixelBufferObject.glUnmapBufferARB(ARBPixelBufferObject.GL_PIXEL_UNPACK_BUFFER_ARB);
                                        }
                                        GL12.glTexSubImage3D(GL12.GL_TEXTURE_3D, 0, 0, 0, Sf3Texture3D._3DTexLayersAmount * rCoord + i, dim.get(0), dim.get(1), 1, texture2D.getEXTabgr(), texture2D.getTextureBufferDataType(), 0);
                                } else {
                                        glloadSubTexture3D(texture2D, Sf3Texture3D._3DTexLayersAmount * rCoord + i, dim.get(0), dim.get(1));
                                }
                                if (JXAenvUtils._debug) {
                                        System.out.println("xxxx VRAM xxxx " + texture2D.getPixelBufferDataTypeName() + " tex3D");
                                }
                        }
                        GL11.glBindTexture(GL12.GL_TEXTURE_3D, 0);
                        if (ext.PixelBufferObjects.GL_isAvailable()) {
                                ARBPixelBufferObject.glBindBufferARB(ARBPixelBufferObject.GL_PIXEL_UNPACK_BUFFER_ARB, 0);
                                if (!pboKeep) {
                                        sPbo.removeItems(pboHash);
                                }
                        }
                        if (JXAenvUtils._debug) {
                                System.out.println("xxxx VRAM xxxx new TEX3D input : " + texture2D + " bufferSize : " + sTex.getItemScratchSize() + "/" + sTex.getItemScratchCapacity());
                        }
                }
                GLHandler._GLpopAttrib();
                Util.checkGLError();
                return buffer;
        }

        /**
         * returs true or false, whether a textureHash buffer has been found and
         * deleted or not, resp.
         *
         * @param texture
         * @return true or false, whether the textureHash buffer link has been
         * found and deleted or not, resp.
         */
        public static boolean _GLUnloadVRAM3D(Sf3Texture3D texture) {
                return _GLUnloadVRAM(texture.hashCode());
        }

        /**
         *
         * @return
         */
        public static FloatBuffer _GLgetCurrentColor() {

                FloatBuffer currentColor = BufferIO._newf(16);

                GL11.glGetFloat(GL11.GL_CURRENT_COLOR, currentColor);

                return currentColor;

        }
        private final static BitStack _GL_TRANSFORM_bits = new BitStack();
        /**
         *
         */
        public final static int _GL_TRANSFORM_SCALE_BIT = _GL_TRANSFORM_bits._newBitRange();
        /**
         *
         */
        public final static int _GL_TRANSFORM_ROTATE_BIT = _GL_TRANSFORM_bits._newBitRange();
        /**
         *
         */
        public final static int _GL_TRANSFORM_TRANSLATE_BIT = _GL_TRANSFORM_bits._newBitRange();
        /**
         *
         */
        public final static int _GL_TRANSFORM_FLIP_HORIZONTAL_BIT = _GL_TRANSFORM_bits._newBitRange();
        /**
         *
         */
        public final static int _GL_TRANSFORM_FLIP_VERTICAL_BIT = _GL_TRANSFORM_bits._newBitRange();

        /**
         *
         * @param textureTarget
         * @param gld
         * @param keepBuffer
         * @param tile
         * @param rCoord
         * @param bounds
         * @param z
         * @param transform
         * @param scaleArgs
         * @param rotateArgs
         * @param translateArgs
         * @param alphaBlend
         * @param colorBlend
         * @return
         */
        protected static boolean __renderTileTexture(int textureTarget, RenderingScene gld, boolean keepBuffer, Sf3TextureTile tile, float rCoord, Rectangle bounds,
                double z, int transform, DoubleBuffer scaleArgs, DoubleBuffer rotateArgs, DoubleBuffer translateArgs, FloatBuffer alphaBlend, FloatBuffer colorBlend) {
                float ratioW = (float) tile.getRefDim().width / (float) bounds.width;
                float ratioH = (float) tile.getRefDim().height / (float) bounds.height;
                ratioW = (float) (tile.getRefDim().width + tile.getTrim().width) / ((float) bounds.width + (float) tile.getTrim().width / ratioW);
                ratioH = (float) (tile.getRefDim().height + tile.getTrim().height) / ((float) bounds.height + (float) tile.getTrim().height / ratioH);
                Rectangle tileBounds = new Rectangle(bounds.x, bounds.y, (int) Math.round((float) tile.getTileDim().width / ratioW), (int) Math.round((float) tile.getTileDim().height / ratioH));
                tileBounds.x += tile.getXTile() * tileBounds.width;
                tileBounds.y += tile.getYTile() * tileBounds.height;
                return __renderTexture(textureTarget, gld, keepBuffer, tile.hashCode(), rCoord, tileBounds, z, transform, scaleArgs, rotateArgs, translateArgs, alphaBlend, colorBlend);
        }

        /**
         *
         * @param textureHash
         * @return
         */
        public static boolean _GLisTiledTexture(int textureHash) {
                if (!sTex.hasItem(textureHash)) {
                        return false;
                } else if (sTex.getItems(textureHash).size() > 0) {
                        int hashOrName = sTex.firstItemBufferName(textureHash);
                        return sTex.hasItem(hashOrName);
                }
                return false;
        }

        /**
         *
         * @param textureTarget
         * @param gld
         * @param keepBuffer
         * @param texture can be of various types : SF3Texture, SF3Texture3D, or
         * Integer which must be a hashcode of {@link #sTex texture buffer}
         * @param rCoord
         * @param bounds
         * @param z
         * @param transform
         * @param scaleArgs
         * @param rotateArgs
         * @param translateArgs
         * @param alphaBlend
         * @param colorBlend
         * @return
         */
        protected static boolean __renderTexture(int textureTarget, RenderingScene gld, boolean keepBuffer, Object texture, float rCoord, Rectangle bounds,
                double z, int transform, DoubleBuffer scaleArgs, DoubleBuffer rotateArgs, DoubleBuffer translateArgs, FloatBuffer alphaBlend, FloatBuffer colorBlend) {
                int textureHash = 0;
                if (texture instanceof Sf3Texture) {
                        _GLLoadVRAM2D((Sf3Texture) texture, false, texture.hashCode(), false);
                        textureHash = texture.hashCode();
                } else if (texture instanceof Sf3Texture3D) {
                        _GLLoadVRAM3D((Sf3Texture3D) texture, false, texture.hashCode());
                        textureHash = texture.hashCode();
                } else if (texture instanceof Integer) {
                        textureHash = (Integer) texture;
                }
                boolean tileMode = _GLisTiledTexture(textureHash);
                if (DebugMap._getInstance().isDebugLevelEnabled(RenderingSceneGL.DBUG_RENDER)) {
                        DebugMap._getInstance().tickPrintln(System.out, "xxx VRAM xxx OPENGL TEXTURE BUFFERS " + sTex.getItemScratchSize() + "/" + sTex.getItemScratchCapacity(), false);
                }
                boolean b = true;
                if (tileMode) {
                        if (DebugMap._getInstance().isDebugLevelEnabled(RenderingSceneGL.DBUG_RENDER)) {
                                DebugMap._getInstance().tickPrintln(System.out, ">>>> GL TILED TEX >>>> OPENGL TEXTURE RENDER AT " + bounds + (transform != 0 ? " w/" : " w/o") + " transform", true);
                        }
                        for (Item tileItem : sTex.getItems(textureHash)) {
                                IntBuffer tileM = (IntBuffer) tileItem.data;
                                Sf3TextureTile tile = new Sf3TextureTile(tileItem.name, textureHash, tileM);
                                b = __renderTileTexture(textureTarget, gld, keepBuffer, tile, rCoord, bounds, z, transform, scaleArgs, rotateArgs, translateArgs, alphaBlend, colorBlend) && b;
                        }
                } else {
                        if (DebugMap._getInstance().isDebugLevelEnabled(RenderingSceneGL.DBUG_RENDER)) {
                                DebugMap._getInstance().tickPrintln(System.out, ">>>> GL TEX >>>> OPENGL TEXTURE RENDER AT " + bounds + (transform != 0 ? " w/" : " w/o") + " transform", true);
                        }
                        GLHandler._GLpushAttrib(GL11.GL_TEXTURE_BIT);
                        GL11.glEnable(textureTarget);
                        Sf3TextureBuffer tex = null;
                        IntBuffer dim = textureHash != 0 ? sTex.firstItem(textureHash).data : null;
                        if (dim instanceof IntBuffer) {
                                switch (textureTarget) {
                                        case GL11.GL_TEXTURE_2D:
                                        case EXTTextureRectangle.GL_TEXTURE_RECTANGLE_EXT:
                                                tex = new Sf3Texture(textureHash, dim);
                                                break;
                                        case GL12.GL_TEXTURE_3D:
                                                tex = new Sf3Texture3D(textureHash, dim);
                                                float rCoordMax = ((Sf3Texture3D) tex).getDepth();
                                                rCoord = Math.min(rCoord + 1, rCoordMax) / rCoordMax;
                                                break;
                                        default:
                                                tex = new Sf3TextureBuffer(textureHash, dim, new BufferIO());
                                                break;
                                }
                        }
                        Dimension textureDim = null;
                        if (tex != null) {
                                textureDim = tex.getRefDim();
                                int glBuffer = sTex.isTextureLoaded(tex.hashCode()) ? sTex.getTextureName(tex.hashCode()).get(tex.hashCode()) : 0;
                                GL11.glBindTexture(textureTarget, glBuffer);
                        }
                        if (colorBlend == null) {
                                /**
                                 * better texture lighting with white as the
                                 * blending
                                 */
                                colorBlend = BufferIO._wrapf(new float[]{1, 1, 1, 1});
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
                                alphaBlend.flip();
                        }
                        Blending blend = _blendingBegin(colorBlend, alphaBlend);
                        _transformBegin(bounds, z, transform, scaleArgs, translateArgs, rotateArgs);
                        GL11.glBegin(GL11.GL_QUADS);
                        GL11.glNormal3f(0, 0, 1);
                        if (tex != null) {
                                if (textureTarget == GL12.GL_TEXTURE_3D) {
                                        GL11.glTexCoord3f(0f, 0f, rCoord);
                                } else if (textureTarget != GL11.GL_TEXTURE_2D) {
                                        GL11.glTexCoord2f(0f, 0f);
                                } else {
                                        GL11.glTexCoord2f(0f, 0f);
                                }
                        }
                        GL11.glVertex2d(0, 0);
                        if (tex != null) {
                                if (textureTarget == GL12.GL_TEXTURE_3D) {
                                        GL11.glTexCoord3f(0f, 1f, rCoord);
                                } else if (textureTarget != GL11.GL_TEXTURE_2D) {
                                        GL11.glTexCoord2f(0, textureDim.height);
                                } else {
                                        GL11.glTexCoord2f(0f, 1f);
                                }
                        }
                        GL11.glVertex2d(0, bounds.getHeight());
                        if (tex != null) {
                                if (textureTarget == GL12.GL_TEXTURE_3D) {
                                        GL11.glTexCoord3f(1f, 1f, rCoord);
                                } else if (textureTarget != GL11.GL_TEXTURE_2D) {
                                        GL11.glTexCoord2f(textureDim.width, textureDim.height);
                                } else {
                                        GL11.glTexCoord2f(1f, 1f);
                                }
                        }
                        GL11.glVertex2d(bounds.getWidth(), bounds.getHeight());
                        if (tex != null) {
                                if (textureTarget == GL12.GL_TEXTURE_3D) {
                                        GL11.glTexCoord3f(1f, 0f, rCoord);
                                } else if (textureTarget != GL11.GL_TEXTURE_2D) {
                                        GL11.glTexCoord2f(textureDim.width, 0f);
                                } else {
                                        GL11.glTexCoord2f(1f, 0f);
                                }
                        }
                        GL11.glVertex2d(bounds.getWidth(), 0);
                        GL11.glEnd();
                        if (tex != null) {
                                if (!keepBuffer) {
                                        _GLUnloadVRAM(textureHash);
                                }
                                GL11.glBindTexture(textureTarget, 0);
                        }
                        _transformEnd();
                        _blendingEnd(blend);
                        GLHandler._GLpopAttrib();
                }
                Util.checkGLError();
                return b;
        }

        /**
         * translates to z, then all transform operations are completed, and
         * before to return, it will translate everything to bounds.x,bounds.y
         * (you may need to
         * {@linkplain GL11#glTranslatef(float,float,float) glTranslatef(-bounds.x, -bounds.y, -z)}
         * back right after this call)
         *
         * @see MatrixStack_tloc
         * @param bounds
         * @param z
         * @param rotateArgs length 3 {degrees,cx,cy}
         * @param scaleArgs length 2 {sx,sy}
         * @param transform GLHandler._GL_TRANSFORM* combination
         * @param translateArgs length 2 {tx,ty}
         */
        public static void _transformBegin(Rectangle bounds, double z, int transform, DoubleBuffer scaleArgs, DoubleBuffer translateArgs, DoubleBuffer rotateArgs) {
                GLHandler.stView.push();
                /**
                 * translate to z-plan
                 */
                GL11.glTranslated(0, 0, z);
                if ((transform & _GL_TRANSFORM_SCALE_BIT) != 0 && scaleArgs != null) {
                        GL11.glScaled(scaleArgs.get(0), scaleArgs.get(1), 1);
                }
                if ((transform & _GL_TRANSFORM_TRANSLATE_BIT) != 0 && translateArgs != null) {
                        GL11.glTranslated(translateArgs.get(0), translateArgs.get(1), 0);
                }
                if ((transform & _GL_TRANSFORM_ROTATE_BIT) != 0 && rotateArgs != null) {
                        GL11.glTranslated(rotateArgs.get(1), rotateArgs.get(2), 0);
                        GL11.glRotatef((float) rotateArgs.get(0), 0, 0, 1);
                        GL11.glTranslated(-rotateArgs.get(1), -rotateArgs.get(2), 0);
                }
                double cx = bounds.getCenterX(), cy = bounds.getCenterY();
                if ((transform & _GL_TRANSFORM_FLIP_HORIZONTAL_BIT) != 0) {
                        GL11.glTranslated(cx, cy, 0);
                        GL11.glScalef(-1, 1, 1);
                        GL11.glTranslated(-cx, -cy, 0);
                }
                if ((transform & _GL_TRANSFORM_FLIP_VERTICAL_BIT) != 0) {
                        GL11.glTranslated(cx, cy, 0);
                        GL11.glScalef(1, -1, 1);
                        GL11.glTranslated(-cx, -cy, 0);
                }
                /**
                 * translate to coords
                 */
                GL11.glTranslated(bounds.getX(), bounds.getY(), 0);
                Util.checkGLError();
        }

        /**
         * @see GL11#glPopMatrix()
         */
        public static void _transformEnd() {
                GLHandler.stView.pop();
        }

        /**
         *
         * @param colorBlend a color (3 or 4 rgb(a) components)
         * @param alphaBlend alpha values (from 0f<1f, 3 or 4 rgb(a) components)
         * @return
         */
        public static Blending _blendingBegin(FloatBuffer colorBlend, FloatBuffer alphaBlend) {
                return _blendingBegin(colorBlend, alphaBlend, false);
        }

        /**
         * @param flatShading default is false; true enabled flat SHADE_MODEL
         * which is faster, but looks worst
         */
        public static Blending _blendingBegin(FloatBuffer colorBlend, FloatBuffer alphaBlend, boolean flatShading) {
                Blending blending = new Blending(Blending._readState(), GL11.GL_MODULATE, alphaBlend, colorBlend, flatShading ? GL11.GL_FLAT : GL11.GL_SMOOTH);
                /**
                 * TEXTURE BLENDING DISABLED (GL_MODULATE is already the default
                 * blending) GL11.glTexEnv(GL11.GL_TEXTURE_ENV,
                 * GL11.GL_TEXTURE_ENV_COLOR, IOBuffer._wrapf(new
                 * float[]{texAlphaBlend.get(0), texAlphaBlend.get(1),
                 * texAlphaBlend.get(2), texAlphaBlend.get(3)}));
                 * GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE,
                 * GL13.GL_COMBINE); GL11.glTexEnvi(GL11.GL_TEXTURE_ENV,
                 * GL13.GL_COMBINE_RGB, GL11.GL_MODULATE);
                 * GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_RGB,
                 * GL11.GL_TEXTURE); GL11.glTexEnvi(GL11.GL_TEXTURE_ENV,
                 * GL13.GL_SOURCE1_RGB, GL13.GL_CONSTANT);
                 * GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND1_RGB,
                 * GL11.GL_SRC_COLOR); GL11.glTexEnvi(GL11.GL_TEXTURE_ENV,
                 * GL13.GL_COMBINE_ALPHA, GL11.GL_MODULATE);
                 * GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_ALPHA,
                 * GL11.GL_TEXTURE); GL11.glTexEnvi(GL11.GL_TEXTURE_ENV,
                 * GL13.GL_SOURCE1_ALPHA, GL13.GL_CONSTANT);
                 * GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND1_ALPHA,
                 * GL11.GL_SRC_ALPHA);
                 */
                blending._applyState(blending);
                return blending;
        }

        /**
         * BLENDING END
         *
         * @param texEnvMode
         * @param currentColor
         */
        public static void _blendingEnd(Blending b) {
                Blending._applyState(b.storedState);
        }

        /**
         *
         * @param extensionsToCheck
         * @return
         */
        private static SortedMap<String, Boolean> _GL_checkExtensions(boolean forceRead, String... extensionsToCheck) {
                String extensions = _GL_readGLExtensionsString(forceRead);
                SortedMap<String, Boolean> extMap = new TreeMap<String, Boolean>();
                for (String ext : extensionsToCheck) {
                        extMap.put(ext, extensions.contains(ext));
                }
                return extMap;
        }
        /**
         *
         */
        public static String GL_renderer = "N/A";

        /**
         *
         * @return
         */
        public static String _GL_readGLRendererName() {
                return GL_renderer = GL11.glGetString(GL11.GL_RENDERER);
        }
        /**
         *
         */
        public static String GL_vendor = "N/A";

        /**
         *
         * @return
         */
        public static String _GL_readGLVendorName() {
                return GL_vendor = GL11.glGetString(GL11.GL_VENDOR);
        }
        /**
         *
         */
        public static String GL_version = "N/A";

        /**
         *
         * @return
         */
        public static String _GL_readGLVersionName() {
                return GL_version = GL11.glGetString(GL11.GL_VERSION);
        }
        /**
         *
         */
        public static String GL_extensions = "N/A";

        /**
         * Retrieves all supported extensions.
         *
         * @return
         * @see GL11#GL_EXTENSIONS
         */
        public static String _GL_readGLExtensionsString(boolean forceRead) {
                return GL_extensions.equals("N/A") || forceRead ? (GL_extensions = GL11.glGetString(GL11.GL_EXTENSIONS)) : GL_extensions;
        }

        /**
         * common extensions are here gathered
         */
        public static enum ext {

                /**
                 *
                 */
                VertexBufferObjects("GL_ARB_vertex_buffer_object", "GL_EXT_vertex_buffer_object", "GL_NV_vertex_buffer_object"),
                /**
                 *
                 */
                PixelBufferObjects("GL_ARB_pixel_buffer_object", "GL_EXT_pixel_buffer_object"),
                /**
                 *
                 */
                ARBImaging("GL_ARB_imaging"),
                /**
                 *
                 */
                TextureRectangle("GL_EXT_texture_rectangle", "GL_NV_texture_rectangle", "GL_ARB_texture_rectangle"),
                /**
                 *
                 */
                ABGR("GL_EXT_abgr");
                /**
                 *
                 */
                public String[] names;

                ext(String... pnames) {
                        this.names = pnames;
                }

                /**
                 *
                 * @return
                 */
                public String[] getNames() {
                        return names;
                }

                /**
                 *
                 * @return
                 */
                public boolean GL_isAvailable() {
                        if (_GLdisableEXT || forceDisable) {
                                return false;
                        }
                        SortedMap<String, Boolean> exts = _GL_checkExtensions(false, names);
                        for (String name : names) {
                                if (exts.get(name)) {
                                        return true;
                                }
                        }
                        return false;
                }
                boolean forceDisable = false;

                public void GL_setDisabled(boolean b) {
                        forceDisable = b;
                }
                /**
                 *
                 */
                public static boolean _GLdisableEXT = Boolean.parseBoolean(RenderingScene.rb.getString("_GLdisableEXT"));
        }

        /**
         * max texture size H and W. if {@linkplain Sf3Texture#_EXTtex} is a
         * texture rectangle target, then the value is for use with texture
         * rectangle only
         *
         * @return max texture size e.g. width <= max && height <= max
         */
        public static int _GLgetMaxTextureSize() {
                IntBuffer max = BufferIO._newi(16);
                GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE, max);
                if (Sf3Texture._EXTtex != GL11.GL_TEXTURE_2D && ext.TextureRectangle.GL_isAvailable()) {
                        GL11.glGetInteger(EXTTextureRectangle.GL_MAX_RECTANGLE_TEXTURE_SIZE_EXT, max);
                }
                return max.get(0);
        }

        /**
         * @return max 3d texture size e.g. width <= max && height <= max
         */
        public static int _GLgetMax3DTextureSize() {
                IntBuffer max = BufferIO._newi(16);
                GL11.glGetInteger(GL12.GL_MAX_3D_TEXTURE_SIZE, max);
                return max.get(0);
        }

        /**
         * affects the GL_PROJECTION matrix stack <br> It may be necessary to
         * reload a gl_modelview_matrix accordingly to the new aspect.
         *
         * @param ortho
         * @param viewport
         * @param nearVal
         * @param farVal
         * @see #_GLsetViewMX(java.awt.Dimension, double, boolean)
         * @see
         * RenderingScene#_GL_reshape(net.sf.jiga.xtended.impl.game.RenderingScene,
         * boolean)
         */
        public static void _GLsetProjMX(boolean ortho, Dimension viewport, float nearVal, float farVal) {
                IntBuffer mode = BufferIO._newi(16);
                GL11.glGetInteger(GL11.GL_MATRIX_MODE, mode);
                GL11.glMatrixMode(GL11.GL_PROJECTION);
                GL11.glLoadIdentity();
                if (!ortho) {
                        GL11.glFrustum(-viewport.width / 2f, viewport.width / 2f, viewport.height / 2f, -viewport.height / 2f, nearVal, farVal);
                } else {
                        GL11.glOrtho(-viewport.width / 2f, viewport.width / 2f, viewport.height / 2f, -viewport.height / 2f, nearVal, farVal);
                }
                GL11.glMatrixMode(mode.get(0));
                Util.checkGLError();
        }

        /**
         * loads the identity matrix into the modelview stack and translates
         * origin to the correct location.
         *
         * @param z0 new z-coordinate origin usually equals
         * <pre>-RenderingScene.GL_NEARVAL</pre>
         *
         * @param enableJava2DStyleCoordinates moves the x0,y0,z0 origin of the
         * coordinates to Java2D style upper-left corner.
         * @param viewport current viewport dimension, needed only if
         * enableJava2DStyleCoordinates is true.
         */
        public static void _GLsetViewMX(Dimension viewport, double z0, boolean enableJava2DStyleCoordinates) {
                IntBuffer mode = BufferIO._newi(16);
                GL11.glGetInteger(GL11.GL_MATRIX_MODE, mode);
                GL11.glMatrixMode(GL11.GL_MODELVIEW);
                GL11.glLoadIdentity();
                if (enableJava2DStyleCoordinates) {
                        GL11.glTranslated(-viewport.getWidth() / 2., -viewport.getHeight() / 2., z0);
                } else {
                        GL11.glTranslated(0, 0, z0);
                }
                GL11.glMatrixMode(mode.get(0));
                Util.checkGLError();
        }

        /**
         * push attrib stacks
         */
        public static void _GLpushAttrib(int attrib_bitmask) {
                GL11.glPushAttrib(attrib_bitmask);
                if (DebugMap._getInstance().isDebugLevelEnabled(RenderingScene.DBUG_RENDER) && DebugMap._getInstance().isDebugLevelEnabled(DebugMap._getInstance()._VOID)) {
                        int depth = GL11.glGetInteger(GL11.GL_ATTRIB_STACK_DEPTH);
                        int maxDepth = GL11.glGetInteger(GL11.GL_MAX_ATTRIB_STACK_DEPTH);
                        /*System.out.println("attrib stack depth " + depth);*/
                        if (depth >= maxDepth) {
                                throw new JXAException(JXAException.LEVEL.SYSTEM, "OpenGL attrib stack overflow (possible cause : another OpenGL error occured before or missing one GLHandler.popAttrib())");
                        }
                }
                Util.checkGLError();
        }

        public static void _GLpopAttrib() {
                GL11.glPopAttrib();
        }

        /**
         * push client attrib stacks
         *
         * @throws JXAException or OpenGLException if attrib stack overflow is
         * detected
         */
        public static void _GLpushClientAttrib(int attrib_bitmask) {
                GL11.glPushClientAttrib(attrib_bitmask);
                if (DebugMap._getInstance().isDebugLevelEnabled(RenderingScene.DBUG_RENDER)) {
                        int depth = GL11.glGetInteger(GL11.GL_CLIENT_ATTRIB_STACK_DEPTH);
                        int maxDepth = GL11.glGetInteger(GL11.GL_MAX_CLIENT_ATTRIB_STACK_DEPTH);
                        System.out.println("client attrib stack depth " + depth);
                        if (depth >= maxDepth) {
                                throw new JXAException(JXAException.LEVEL.SYSTEM, "client attrib stack overflow");
                        }
                }
                Util.checkGLError();
        }

        public static void _GLpopClientAttrib() {
                GL11.glPopClientAttrib();
        }
}
