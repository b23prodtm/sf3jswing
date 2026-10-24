/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.game.gl;

import java.nio.Buffer;
import java.nio.IntBuffer;
import java.util.*;
import net.sf.jiga.xtended.JXAException;
import net.sf.jiga.xtended.impl.game.gl.geom.GLGeom;
import net.sf.jiga.xtended.impl.system.BufferIO;
import net.sf.jiga.xtended.kernel.JXAenvUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.opengl.ARBPixelBufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

/**
 * create one items like that :
 * <pre>if (!GLHandler.sPbo.hasItem(hashCode())) {
 * GLHandler.sPbo.addItem(hashCode(), new Item(GLHandler.sPbo.getVRAMFindVacant(), BufferIO._newi(0)));
 * }
 * int glPBO = GLHandler.sPbo.getItems(hashCode()).iterator().next().name;</pre>
 * create two or more items like that (names will be generated internally):
 * <pre> GLHandler.sPbo.addItems(hashCode(), data, data2);</pre> you can omit
 * items if you want to do so in one particular situation where you always use
 * the same buffer name over time (unless you delete it) :
 * <pre>
 * // here's the buffer name glPBO we generate
 * glPBO = GLHandler.sPbo.isValid(glPBO) ? glPBO : GLHandler.sPbo.getVRAMFindVacant();
 * // bind
 * ARBPixelBufferObject.glBindBufferARB(ARBPixelBufferObject.GL_PIXEL_PACK_BUFFER_ARB, glPBO);
 * // fill
 * GL11.glReadPixels(bounds.x, bounds.y, bounds.width, bounds.height, GL11.GL_STENCIL_INDEX, GL11.GL_UNSIGNED_BYTE, 0);
 * ARBPixelBufferObject.glBindBufferARB(ARBPixelBufferObject.GL_PIXEL_PACK_BUFFER_ARB, 0);
 * ...
 * </pre>
 *
 * <h3>About tiled textures</h3>
 * A tiled texture says that many Item's are bound to the same texture hash, i.e {@link #getItems(int)
 * } and {@link TexScratch#getTextureMap(int) } will return many Item's, one for
 * each tile.
 *
 * @author www.b23prodtm.info
 */
public abstract class MemScratch<D extends Buffer> {

    private static boolean _initialized = false;

    public static boolean _isInitialized() {
        return _initialized;
    }

    /**
     * * init some GL specific ressources
     */
    public static void _init() {
        if (_initialized) {
            return;
        }
        GLGeom._initTess();
        _initialized = true;
    }

    public static void _free() {
        if (!_initialized) {
            return;
        }
        try {
            synchronized (GLList.glLists) {
                for (GLList l : GLList.glLists) {
                    GL11.glDeleteLists(l.hashCode(), 1);
                }
            }
        } catch (Error ex) {
            if (JXAenvUtils._debug) {
                ex.printStackTrace();
            }
        } finally {
            GLGeom._killTess();
            GLList.glLists.clear();
            _initialized = false;
        }
    }

    /**
     * An Item represents a buffer address in the accelerated memory. Any Item's
     * of the same name will have the same hashCode return, regardless of their
     * data. That is true for example if an Item is added to a list that
     * contains another Item instance of the same name then the first one will
     * replace the original one. Still it has to be loaded in by its
     * corresponding toolkit (TextureScratch, SoundScratch, etc.) to indeed
     * erase the hardware address.
     */
    public static class Item<B extends Buffer> {

        /**
         * GL pointer (known as buffer name) that can be bound to a target (e.g.
         * {@link GL11#GL_TEXTURE_2D},{@link ARBPixelBufferObject#GL_PIXEL_UNPACK_BUFFER_ARB},...)
         */
        public final int name;
        public B data;
        int usage;

        /**
         * An Item represents a buffer address in the accelerated memory. Any
         * Item's of the same name will have the same hashCode return,
         * regardless of their data. That is true for example if an Item is
         * added to a list that contains another Item instance of the same name
         * then the first one will replace the original one. Still it has to be
         * loaded in by its corresponding toolkit (TextureScratch, SoundScratch,
         * etc.) to indeed erase the hardware address.
         *
         * @param name must refer to a
         * {@link #getVRAMFindVacant() vacant buffer}
         * @param data any data, {@link MemScratch} subclasses should implement
         * data handlers to describe the contents (this data will use cache Heap
         * memory, it may be temporarily used to store big data)
         * @param usage flag value (e.g.
         * {@link ARBPixelBufferObject#GL_STREAM_READ_ARB})
         */
        public Item(int name, B data, int usage) {
            assert data instanceof Buffer : "null data is invalid, please use an empty Buffer instead !";
            this.name = name;
            this.data = data;
            this.usage = usage;
        }

        /**
         * An Item represents a buffer address in the accelerated memory. Any
         * Item's of the same name will have the same hashCode return,
         * regardless of their data. That is true for example if an Item is
         * added to a list that contains another Item instance of the same name
         * then the first one will replace the original one. Still it has to be
         * loaded in by its corresponding toolkit (TextureScratch, SoundScratch,
         * etc.) to indeed erase the hardware address.
         *
         * @param name must refer to a
         * {@link #getVRAMFindVacant() vacant buffer}
         * @param data any data, {@link MemScratch} subclasses should implement
         * data handlers to describe the contents (this data will use cache Heap
         * memory, it may be temporarily used to store big data)
         */
        public Item(int name, B data) {
            this(name, data, 0);
        }

        @Override
        public final boolean equals(Object obj) {
            return obj == null ? false : obj.hashCode() == hashCode();
        }

        /**
         * the {@link #name} of this item
         *
         * @return {@link #name} of this item
         */
        @Override
        public final int hashCode() {
            return name;
        }
    }

    public List<Item<D>> _newSet() {
        return new ArrayList<Item<D>>();
    }
    private IntBuffer scratch = BufferIO._newi(1);
    /**
     *
     */
    private Map<Integer, List<Item<D>>> buffersMap = Collections.synchronizedMap(new LinkedHashMap<Integer, List<Item<D>>>());

    /**
     * items count
     */
    public int getItemScratchSize() {
        int i = 0;
        for (List<Item<D>> d : buffersMap.values()) {
            i += d.size();
        }
        return i;
    }

    public boolean hasItem(int bufferHash) {
        return buffersMap.containsKey(bufferHash);
    }

    /**
     * should return true if it is a valid buffer name
     *
     * @param name a valid buffer name (may be bound already)
     * @see GL11#glIsTexture(int)
     * @see GL15#glIsBuffer(int)
     * @see AL10#alIsBuffer(int)
     * @see AL10#alIsSource(int)
     */
    public abstract boolean isBound(int name);

    /**
     * @return a list of all bound Item's (VRAM buffer addresses) to the
     * specified hashcode
     */
    public List<Item<D>> getItems(int bufferHash) {
        return buffersMap.get(bufferHash);
    }

    /**
     * affects one item (with some GL pointer set as item.name) to the specified
     * buffer (heap memory hash-"pointer") CAUTION : Item.name is uniquely
     * identifying each items
     *
     * @param bufferHash
     * @param items
     */
    public void addItem(int bufferHash, Item<D> item) {
        addItems(bufferHash, new Item[]{item});
    }

    /**
     * affects several items (existing GL memory pointers) to the specified
     * buffer (heap memory hash-"pointer") CAUTION : Item.name is uniquely
     * identifying each items
     *
     * @param bufferHash
     * @param items
     */
    public void addItems(int bufferHash, D... itemsData) {
        for (D i : itemsData) {
            addItem(bufferHash, new Item(getVRAMFindVacant(), i));
        }
    }

    private void addItems(int bufferHash, Item<D>... items) {
        List<Item<D>> s = hasItem(bufferHash) ? getItems(bufferHash) : _newSet();
        for (Item i : items) {
            s.add(i);
        }
        buffersMap.put(bufferHash, s);
    }

    /**
     * makes the items {@link Item#name GL pointer} vacant for re-using it
     */
    public boolean removeItems(int bufferHash) {
        if (hasItem(bufferHash)) {
            try {
                List<Item<D>> k = getItems(bufferHash);
                List<Integer> deletedNames = new ArrayList<Integer>();
                for (Item<D> h : k) {
                    if (isBound(h.name)) {
                        deletedNames.add(h.name);
                        delete(BufferIO._wrapi(new int[]{h.name}));
                    }
                }
                /**
                 * delete (0) access to this name in scratch
                 */
                scratch.rewind();
                while (scratch.hasRemaining()) {
                    if (deletedNames.contains(scratch.get())) {
                        scratch.put(scratch.position() - 1, 0);
                    }
                }
                scratch.rewind();
                /**
                 * regenerate the number of deleted names
                 */
                genVRAMBuffersMap(deletedNames.size());
            } catch (Error ex) {
                /**
                 * catch error and continue
                 */
                ex.printStackTrace(System.out);
            } finally {
                buffersMap.remove(bufferHash);
                return true;
            }
        } else {
            return false;
        }
    }
    /**
     * TEXTURE SCRATCH CAPACITY. INCREASE TO FIT THE REQUIRED TEXTURE BUFFER
     * CAPACITY ! IF MODIFIED, THE NEXT TIME RenderingSceneGL IS INSTANCIATED IT
     * IS LOADED. IF MODIFIED, YOU CAN CALLVBACK
     * {@linkplain #genVRAMBuffersMap(int) genVRAMBuffersMap(_scratchCapacity)}.
     *
     * @default 1000
     */
    private int _scratchCapacity = 0;

    public int getItemScratchCapacity() {
        return _scratchCapacity;
    }

    private IntBuffer _genVRAMBuffersMap(int numBuffers) {
        IntBuffer scratch = BufferIO._newi(numBuffers);
        gen(scratch);
        return scratch;
    }
    private boolean initialized = false;

    /**
     * must be called to get things initialized
     *
     * @param scratchCapacity
     */
    public void initScratch(int scratchCapacity) {
        if (initialized) {
            return;
        }
        scratch = _genVRAMBuffersMap(_scratchCapacity = scratchCapacity);
        buffersMap.clear();
        initialized = true;
    }

    /**
     *
     */
    public void freeScratch() {
        if (!initialized) {
            return;
        }
        try {
            delete(scratch);
        } catch (Error ex) {
            if (JXAenvUtils._debug) {
                ex.printStackTrace();
            }
        } finally {
            buffersMap.clear();
            scratch = BufferIO._newi(_scratchCapacity = 0);
            initialized = false;
        }
    }

    /**
     *
     * @param scratch container for new buffer names
     */
    protected abstract void gen(IntBuffer scratch);

    /**
     * deletes a set of buffer names. after deletion, it cannot be reused from
     * here (see <a
     * href='http://www.opengl.org/sdk/docs/man/xhtml/glDeleteBuffers.xml'>OpenGL
     * references</a>), but {@link #gen(java.nio.IntBuffer)} may return the same
     * names again.
     *
     * @param scratch buffer names (or the value stored as {@link Item#name}
     * @see #removeItems(int)
     */
    protected abstract void delete(IntBuffer scratch);

    /**
     * a valid buffer name that can be bound with any suited glBind*(). It
     * remains "vacant" as long as it is not
     * {@link #addItem(int, net.sf.jiga.xtended.impl.game.gl.MemScratch.Item) added as an item}.
     *
     * @return a valid buffer name that is not bound to any target yet
     * @see #_genVRAMBuffersMap(int)
     * @see #delete(java.nio.IntBuffer)
     */
    public int getVRAMFindVacant() {
        if (!initialized) {
            throw new JXAException("_initScratch is missing");
        }
        int buffer = 0;
        scratch.rewind();
        while (scratch.hasRemaining()) {
            int name = scratch.get();
            /**
             * buffer name was deleted
             */
            if (name == 0) {
                continue;
            }
            /**
             * is that bound already ?
             */
            boolean bound = isBound(name);
            if (!bound) {
                /**
                 * ensure no items exists
                 */
                synchronized (buffersMap) {
                    for (List<Item<D>> buffers : buffersMap.values()) {
                        /**
                         * items will equal name if items.name is equal
                         */
                        for (Item<D> i : buffers) {
                            if (i.name == name) {
                                bound = true;
                                break;
                            }
                        }
                        if (bound) {
                            break;
                        }
                    }
                }
            }
            /*
             * vacant id if it is not found
             */
            if (!bound) {
                buffer = name;
                break;
            }
        }
        scratch.rewind();
        if (buffer == 0) {
            throw new JXAException("BuffersMap is overflowing the generated scratch capacity" + scratch.limit() + " . Please increase scratch/VRAMBuffersMap capacity to at least ~ " + buffersMap.size() + " names !");
        }
        return buffer;
    }

    /**
     * This method generates more GL texture/pbo/vbo buffer names. This
     * increases the {@linkplain #scratch} capacity.
     *
     * @param numBuffers
     */
    public void genVRAMBuffersMap(int numBuffers) {
        IntBuffer current = scratch;
        /**
         * gen new names
         */
        IntBuffer add = _genVRAMBuffersMap(numBuffers);
        scratch = BufferIO._newi(current.limit() + add.limit());
        /**
         * refill scratch
         */
        current.rewind();
        while (current.hasRemaining()) {
            int i = current.get();
            if (i != 0) {
                /**
                 * skip deleted (0) names
                 */
                scratch.put(i);
            }
        }
        add.rewind();
        /**
         * add new names to scratch
         */
        scratch.put(add);
        scratch.flip();
    }

    /**
     * returns the number of GL buffers allocated by the current application.
     * THIS IS NOT THE CURRENT TEXTURE BINDINGS AMOUNT.
     *
     * @return
     * @see #genVRAMBuffersMap(int)
     */
    public int getVRAMBuffersCapacity() {
        return scratch.limit();
    }

    public Item<D> item(int bufferHash, int i) {
        if (hasItem(bufferHash)) {
            return getItems(bufferHash).get(i);
        }
        throw new JXAException(JXAException.LEVEL.SYSTEM, "no valid item found !!!");
    }

    public int itemBufferName(int bufferHash, int i) {
        Item it = item(bufferHash, i);
        if (it != null) {
            return it.name;
        }
        throw new JXAException(JXAException.LEVEL.SYSTEM, "no valid name found !!!");
    }

    public Item<D> firstItem(int bufferHash) {
        return item(bufferHash, 0);
    }

    public int firstItemBufferName(int bufferHash) {
        return itemBufferName(bufferHash, 0);
    }

    /**
     * Deletes the selected buffer names on the next rendering frame. Therefore
     * the buffer contents can be usueable until the current rendering frame
     * fully finishes all of its tasks.
     *
     * @param scene must be the current RenderingSceneGL
     * @see RenderingSceneGL#doTaskOnGL(Runnable)
     * @see #delete(java.nio.IntBuffer)
     */
    public void discard(RenderingSceneGL scene, final int... names) {
        scene.doTaskOnGL(new Runnable() {
            @Override
            public void run() {
                delete(BufferIO._wrapi(names));
            }
        });
    }

    /**
     * Deletes the selected item buffer names on the next rendering frame.
     * Therefore the buffer contents can be usueable until the current rendering
     * frame fully finishes all of its tasks.
     *
     * @param scene must be the current RenderingSceneGL
     * @see RenderingSceneGL#doTaskOnGL(Runnable)
     * @see #removeItems(int)
     */
    public void discardItems(RenderingSceneGL scene, final int bufferHash) {
        scene.doTaskOnGL(new Runnable() {
            @Override
            public void run() {
                removeItems(bufferHash);
            }
        });
    }
}
