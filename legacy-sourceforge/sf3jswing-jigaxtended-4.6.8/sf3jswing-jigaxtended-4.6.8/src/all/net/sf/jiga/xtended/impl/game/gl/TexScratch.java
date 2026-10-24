/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.game.gl;

import java.awt.Dimension;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.jiga.xtended.impl.game.gl.MemScratch.Item;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.Util;

/**
 *
 * @author www.b23prodtm.info
 */
public class TexScratch extends MemScratch<IntBuffer> {

    @Override
    public boolean isBound(int id) {
        return GL11.glIsTexture(id);
    }

    /**
     * @param texHash
     * @return
     * @see #isTextureLoaded(int)
     */
    public boolean isTexture3D(int texHash) {
        if (isTextureLoaded(texHash)) {
            return getItems(texHash).iterator().next().data.get(2) != -1;
        } else {
            return false;
        }
    }

    /**
     * @param texHash
     * @return
     */
    public int getTexture3DRCoord(int texHash) {
        if (isTexture3D(texHash)) {
            return Sf3Texture3D._3DTexLayersAmount * getItems(texHash).iterator().next().data.get(3);
        } else {
            return 0;
        }
    }

    /**
     * @param texHash
     * @return
     */
    public int getTexture3DDepth(int texHash) {
        if (isTexture3D(texHash)) {
            return getItems(texHash).iterator().next().data.get(2);
        } else {
            return 1;
        }
    }

    /**
     * <ol><li>1 for a standard texture (one buffer)</li>
     * <li>2 or more for a tiled texture (many buffers)</li>
     * </ol>
     */
    public int getTextureTilesCount(int texHash) {
        return getTextureName(texHash).size();
    }

    /**
     * tiled texture will return map of hashcodes linked to names
     *
     * @param texHash
     * @return an map containing all texture names (GL id as values for textures
     * hashCodes as keys) bound for the specified texture.hashCode()
     */
    public Map<Integer, Integer> getTextureName(int texHash) {
        Map<Integer, Integer> names = new HashMap<Integer, Integer>();
        boolean b = hasItem(texHash);
        if (b) {
            List<Item<IntBuffer>> s = getItems(texHash);
            for (Item i : s) {
                try {
                    if (i != null) {
                        /**
                         * name is a hash of another texture item (tile)
                         */
                        if (hasItem(i.name)) {
                            names.put(i.name, getTextureName(i.name).get(i.name));
                        } else {
                            /*
                             * this no hash, then it is the buffer name
                             */
                            names.put(texHash, i.name);
                        }
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }
        return names;
    }

    /**
     * returns true or false, whether the specified texture is loaded or not,
     * resp. it calls native GL code : the glIsTexture() method.
     *
     * @param texHash
     * @return
     * @see #getLoadState(Object)
     * @see GL11#glIsTexture(int)
     */
    public boolean isTextureLoaded(int texHash) {
        Map<Integer, Integer> names = getTextureName(texHash);
        boolean b = !names.isEmpty();
        if (b) {
            for (Integer name : names.values()) {
                b = isBound(name) && b;
            }
        }
        return b;
    }

    /**
     * the internal mapping made for the specified texture.hashcode().
     *
     * @param texHash
     * @return it's a list of all bound textures for this hashcode. It normally
     * returns just one texture Item (singleton), but if the Sf3Texture is known
     * to be a tiled set or a 3D texture, then it returns as many. In these
     * cases, the Item's.name's are not VRAM / GL buffer addresses (names) but
     * hashcode to retrieve another textureMap, singleton of the registered
     * texture buffer.
     * @see Sf3TextureTile
     * @see Sf3Texture
     * @see Sf3Texture3D
     */
    public List<Item<IntBuffer>> getTextureMap(int texHash) {
        return getItems(texHash);
    }

    /**
     * gathers WIDTH and HEIGHT of the registered texture<br> It does not read
     * from the GL state parameters.
     */
    public Dimension getTexSize(int texHash) {
        Item<IntBuffer> i = firstItem(texHash);
        return new Dimension(i.data.get(0), i.data.get(1));
    }

    /**
     * returns WIDTH of the registered texture<br> It does not read from the GL
     * state parameters.
     */
    public int getTexWidth(int texHash) {
        return firstItem(texHash).data.get(0);
    }

    /**
     * returns HEIGHT of the registered texture<br> It does not read from the GL
     * state parameters.
     */
    public int getTexHeight(int texHash) {
        return firstItem(texHash).data.get(1);
    }

    @Override
    protected void gen(IntBuffer scratch) {
        GL11.glGenTextures(scratch);
        Util.checkGLError();
    }

    @Override
    protected void delete(IntBuffer names) {
        names.rewind();
        /**
         * tiles ? then all names are hashcodes
         */
        if (names.limit() > 1 && hasItem(names.get(0))) {
            while (names.remaining() > 0) {
                removeItems(names.get());
            }
        } else {
            GL11.glDeleteTextures(names);
            Util.checkGLError();
        }
    }
}
