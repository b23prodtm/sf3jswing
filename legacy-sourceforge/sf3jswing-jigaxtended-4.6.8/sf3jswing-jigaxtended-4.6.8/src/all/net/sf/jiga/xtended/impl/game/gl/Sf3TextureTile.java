package net.sf.jiga.xtended.impl.game.gl;

import java.awt.Dimension;
import java.nio.IntBuffer;
import net.sf.jiga.xtended.impl.system.BufferIO;

/**
 Defines and stores (as Buffer's) tiles of Sf3texture's when needed by the environment.
 @author www.b23prodtm.info
 */
public class Sf3TextureTile extends Sf3TextureBuffer {

    int refHash;

    /**
     * 
     * @param refHash
     * @param refDim
     * @param tile
     * @param tileX
     * @param tileY
     */
    public Sf3TextureTile(int refHash, Dimension refDim, Sf3Texture tile, int tileX, int tileY) {
        this(tile.hashCode(), refHash, refDim, tileX, tileY, tile.getSize(), new Dimension(tile.getWidth() * (int) Math.ceil((float) refDim.width / (float) tile.getWidth()) - refDim.width, tile.getHeight() * (int) Math.ceil((float) refDim.height / (float) tile.getHeight()) - refDim.height));
    }

    /**
     * 
     * @param hash
     * @param refHash
     * @param refDim
     * @param xtile
     * @param ytile
     * @param tileDim
     * @param trim
     */
    public Sf3TextureTile(int hash, int refHash, Dimension refDim, int xtile, int ytile, Dimension tileDim, Dimension trim) {
        this(hash, refHash, BufferIO._wrapi(new int[]{refDim.width, refDim.height, -1, xtile, ytile, tileDim.width, tileDim.height, trim.width, trim.height}));
    }

    /**
     * 
     * @param hash
     * @param refHash
     * @param map
     */
    public Sf3TextureTile(int hash, int refHash, IntBuffer map) {
        super(hash, map, new BufferIO());
        this.refHash = refHash;
    }

    /**
     * 
     * @return
     */
    public int getRefHash() {
        return refHash;
    }

    /**
     * 
     * @return
     */
    public int getXTile() {
        return map.get(3);
    }

    /**
     * 
     * @return
     */
    public int getYTile() {
        return map.get(4);
    }

    /**
     * 
     * @return
     */
    public Dimension getTileDim() {
        return new Dimension(map.get(5), map.get(6));
    }

    /**
     * lower and right part of the tile to cut off while rendering (this is often due to
     * non-power of two texture sizes)
     * @return
     */
    public Dimension getTrim() {
        return new Dimension(map.get(7), map.get(8));
    }
    
}
