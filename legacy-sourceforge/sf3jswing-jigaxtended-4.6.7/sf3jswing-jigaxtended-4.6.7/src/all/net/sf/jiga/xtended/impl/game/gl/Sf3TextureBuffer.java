/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.game.gl;

import java.awt.Dimension;
import java.awt.image.DataBuffer;
import java.io.*;
import java.nio.Buffer;
import java.nio.IntBuffer;
import net.sf.jiga.xtended.JXAException;
import net.sf.jiga.xtended.impl.system.BufferIO;
import org.lwjgl.opengl.GL11;

/**
 * @author www.b23prodtm.info <b23prodtm@users.sourceforge.net>
 */
public class Sf3TextureBuffer implements Externalizable, Cloneable {

    private static final long serialVersionUID = 2323;
    protected transient IntBuffer map;
    protected int hash;
    /**
     * pixel buffer
     */
    protected BufferIO pixelBuffer;

    public Sf3TextureBuffer() {
        this((int) System.nanoTime(), BufferIO._wrapi(new int[]{0, 0}, false), new BufferIO());
    }

    protected Sf3TextureBuffer(int hash, IntBuffer map, BufferIO pixelBuffer) {
        this.map = map;
        this.hash = hash;
        this.pixelBuffer = pixelBuffer;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        return o == null ? false : o.hashCode() == hashCode();
    }
    
    public Dimension getRefDim() {
        return new Dimension(map.get(0), map.get(1));
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Sf3TextureBuffer cloned = (Sf3TextureBuffer) super.clone();
        cloned.map = BufferIO._cp(map, false);
        cloned.pixelBuffer = (BufferIO) pixelBuffer.clone();        
        return cloned;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        int currentPty = Thread.currentThread().getPriority();
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        out.writeInt(hash);
        out.writeObject(new BufferIO(map));
        out.writeObject(pixelBuffer);
        out.flush();
        Thread.currentThread().setPriority(currentPty);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int currentPty = Thread.currentThread().getPriority();
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        hash = in.readInt();
        map = (IntBuffer) ((BufferIO) in.readObject()).accessData();
        pixelBuffer = (BufferIO) in.readObject();
        Thread.currentThread().setPriority(currentPty);
    }

    /**
     * datastore : IntBuffer{0,0}
     */
    public IntBuffer toTexScratchDataStore() {
        return map;
    }

    /**
     * @return texture buffer size (usually byte, but may vary if aother type of
     * buffer than bytebuffer is used)
     */
    public int getTextureBufferSize() {
        return pixelBuffer.accessData().limit();
    }

    /**
     * CAUTION : NO COPY is made
     *
     * @return the backing (undirect) buffer instance
     * @see BufferIO#accessData()
     */
    public Buffer accessPixelBuffer() {
        return pixelBuffer.accessData();
    }

    /**
     * WARNING : an actual DEEP COPY IS MADE each time this method is called
     *
     * @return a deep (direct) copy of the backing buffer
     * @see BufferIO#getData()
     */
    public Buffer getPixelBuffer() {
        return pixelBuffer.getData();
    }

    /**
     * calls {@linkplain BufferIO#_getBufferDataTypeName(java.nio.Buffer)}
     */
    public String getPixelBufferDataTypeName() {
        return BufferIO._getBufferDataTypeName(pixelBuffer.accessData());
    }

    /**
     * calls {@linkplain BufferIO#_getBufferDataType(java.nio.Buffer)}
     */
    public int getPixelBufferDataType() {
        return pixelBuffer.getBufferDataType();
    }

    public int getTextureBufferDataType() {
        int glType;
        switch (pixelBuffer.getBufferDataType()) {
            case DataBuffer.TYPE_BYTE:
                glType = GL11.GL_UNSIGNED_BYTE;
                break;
            case DataBuffer.TYPE_DOUBLE:
                glType = GL11.GL_DOUBLE;
                break;
            case DataBuffer.TYPE_FLOAT:
                glType = GL11.GL_FLOAT;
                break;
            case DataBuffer.TYPE_SHORT:
            case DataBuffer.TYPE_USHORT:
                glType = GL11.GL_UNSIGNED_SHORT;
                break;
            case DataBuffer.TYPE_INT:
                glType = GL11.GL_UNSIGNED_INT;
                break;
            default:
                throw new JXAException(JXAException.LEVEL.APP, "unknown/unsupported datatype : " + pixelBuffer);
        }
        return glType;
    }

}
