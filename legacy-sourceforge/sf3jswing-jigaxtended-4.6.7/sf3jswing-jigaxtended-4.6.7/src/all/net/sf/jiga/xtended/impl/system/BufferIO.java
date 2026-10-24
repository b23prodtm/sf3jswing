/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.system;

import java.awt.Dimension;
import java.awt.image.DataBuffer;
import java.io.*;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.nio.*;
import java.util.*;
import net.sf.jiga.xtended.kernel.DebugMap;
import net.sf.jiga.xtended.kernel.JXAenvUtils;
import net.sf.jiga.xtended.kernel.Level;
import org.apache.commons.io.FileUtils;

/**
 * BufferIO provides easy services for in- and output of Buffer's to disk or
 * network. Produces direct (with native ordered bytes) Buffers.<br> It clones,
 * serializes and compares {@linkplain DataBuffer}'s. This object is backing the
 * gl.* package for all VRAM buffers. <br><b>All Buffer's can be reused so that
 * their {@link Buffer#capacity() } will not always be equal to the
 * {@link Buffer#limit()}, i.e. any reading or writing of data will be
 * constrained to their limit value. PLEASE, always use {@link Buffer#limit()}
 * to read the buffer data size !</b>
 *
 * @param <B>
 * @author www.b23prodtm.info
 */
public class BufferIO<B extends Buffer> implements Externalizable, Cloneable {

    /**
     * End of Line detection for pixel buffer
     *
     * @param byteBufferIndex
     * @param pixelBufferSize
     * @param type
     * @return
     */
    public static boolean _isEOL(int byteBufferIndex, Dimension pixelBufferSize, Data type) {
        return (byteBufferIndex % (pixelBufferSize.width * type.byteSize)) == 0;
    }

    /**
     * find the byte offset in a pixel Buffer (e.g. BufferedImage or PBO)
     *
     * @param pixelBufferDirtyArea
     * @param pixelBufferArea
     * @param type
     * @return
     *
     * public static int _getByteOffset(Point pixelBufferDirtyAreaLocation,
     * Dimension pixelBufferAreaSize, Data type) { return type.byteSize *
     * (pixelBufferDirtyAreaLocation.x + pixelBufferDirtyAreaLocation.y *
     * pixelBufferAreaSize.width); }
     */
    /**
     * @param data
     * @return name of the data type
     */
    public static String _getBufferDataTypeName(Buffer data) {
        return Data._findTypeName(_getBufferDataType(data));
    }

    /**
     * @param data
     * @return type constants of class {@linkplain DataBuffer} that were used to
     * build the buffer
     */
    public static int _getBufferDataType(Buffer data) {
        if (data instanceof ByteBuffer) {
            return DataBuffer.TYPE_BYTE;
        } else if (data instanceof IntBuffer) {
            return DataBuffer.TYPE_INT;
        } else if (data instanceof FloatBuffer) {
            return DataBuffer.TYPE_FLOAT;
        } else if (data instanceof ShortBuffer) {
            return DataBuffer.TYPE_SHORT;
        } else if (data instanceof DoubleBuffer) {
            return DataBuffer.TYPE_DOUBLE;
        } else {
            return DataBuffer.TYPE_UNDEFINED;
        }
    }

    /**
     * uses the {@link Buffer#limit() } to compute size
     */
    public static String _countByteSize(Buffer pixelBuffer) {
        Data d = Data._findType(pixelBuffer);
        return FileUtils.byteCountToDisplaySize(pixelBuffer.limit() * d.byteSize);
    }

    /**
     * returns the current DataBuffer type of the buffer.
     *
     * @return the current DataBuffer type of the buffer
     */
    public int getBufferDataType() {
        return _getBufferDataType(data);
    }

    /**
     * returns a human-readable String of the contents of this BufferIO
     * instance.
     *
     * @return a human-readable String of the contents of this BufferIO
     * instance.
     */
    @Override
    public String toString() {
        return Data._findTypeName(getBufferDataType()) + " length (limit): " + data.limit() + " capacity : " + data.capacity() + " (hash " + hashCode() + ")";
    }
    /**
     * the hash
     */
    long hash = System.nanoTime();

    /**
     * unique hash-code (nano-time based)
     */
    @Override
    public int hashCode() {
        return (int) hash;
    }

    /**
     * DOES NOT COMPARE DATABUFFERS, SOLELY THE BUFFERIO INSTANCE IS COMPARED
     * WITH
     *
     * @param o
     */
    @Override
    public boolean equals(Object o) {
        return o == null ? false : o.hashCode() == hashCode();
    }
    /**
     * the buffer
     */
    transient Buffer data;

    /**
     * creates a BufferIO instance.
     *
     * @param buf the DataBuffer to control as the backing buffer.
     */
    public BufferIO(B buf) {
        data = buf;
    }

    /**
     * creates a BufferIO instance with an empty ByteBuffer as a backing buffer
     */
    public BufferIO() {
        this(0);
    }

    /**
     * creates a BufferIO instance with a ByteBuffer as a backing buffer
     *
     * @param capacity
     */
    public BufferIO(int capacity) {
        this((B) _new(capacity, false));
    }

    /**
     * CAUTION: NO COPY is made
     *
     * @return the backing (non-direct) data Buffer <B>
     */
    public B accessData() {
        return (B) data;
    }

    /**
     * WARNING : an actual DEEP COPY IS MADE each time this method is called
     * returns a deep (direct) copy Buffer instance that is backing this
     * BufferIO
     *
     * @return a deep copy Buffer
     */
    public B getData() {
        B b = (B) data;
        ByteBuffer bb = null;
        data.rewind();
        switch (getBufferDataType()) {
            case DataBuffer.TYPE_BYTE:
                bb = ByteBuffer.allocateDirect(data.limit()).order(ByteOrder.nativeOrder());
                while (((ByteBuffer) data).hasRemaining()) {
                    bb.put(((ByteBuffer) data).get());
                }
                break;
            case DataBuffer.TYPE_DOUBLE:
                bb = ByteBuffer.allocateDirect(data.limit() * Data.DOUBLE.byteSize).order(ByteOrder.nativeOrder());
                while (((DoubleBuffer) data).hasRemaining()) {
                    bb.putDouble(((DoubleBuffer) data).get());
                }
                break;
            case DataBuffer.TYPE_FLOAT:
                bb = ByteBuffer.allocateDirect(data.limit() * Data.FLOAT.byteSize).order(ByteOrder.nativeOrder());
                while (((FloatBuffer) data).hasRemaining()) {
                    bb.putFloat(((FloatBuffer) data).get());
                }
                break;
            case DataBuffer.TYPE_INT:
                bb = ByteBuffer.allocateDirect(data.limit() * Data.INT.byteSize).order(ByteOrder.nativeOrder());
                while (((IntBuffer) data).hasRemaining()) {
                    bb.putInt(((IntBuffer) data).get());
                }

                break;
            case DataBuffer.TYPE_USHORT:
            case DataBuffer.TYPE_SHORT:
                bb = ByteBuffer.allocateDirect(data.limit() * Data.SHORT.byteSize).order(ByteOrder.nativeOrder());
                while (((ShortBuffer) data).hasRemaining()) {
                    bb.putShort(((ShortBuffer) data).get());
                }
                break;
            default:
                break;
        }
        data.rewind();
        bb.flip();
        switch (getBufferDataType()) {
            case DataBuffer.TYPE_BYTE:
                return (B) bb;
            case DataBuffer.TYPE_DOUBLE:
                return (B) bb.asDoubleBuffer();
            case DataBuffer.TYPE_FLOAT:
                return (B) bb.asFloatBuffer();
            case DataBuffer.TYPE_INT:
                return (B) bb.asIntBuffer();
            case DataBuffer.TYPE_USHORT:
            case DataBuffer.TYPE_SHORT:
                return (B) bb.asShortBuffer();
            default:
                return null;
        }
    }

    /**
     * sets up a new buffer for this BufferIO instance. The current buffer will
     * be discarded.
     *
     * @param data the Buffer to set as the new backing buffer
     */
    public void setData(B data) {
        this.data = data;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        endWrite(out);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        endRead(in);
    }

    /**
     * The buffer is 1-to-one copied and cloned so that a new (non-direct) COPY
     * of the backing buffer is made useable. Both master and clone backing
     * buffers are rewinded after the process before to return. <br> CAUTION :
     * the clone buffer will have the original
     * {@link Buffer#limit() limit of the buffer} as its capacity
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        BufferIO cloned = (BufferIO) super.clone();
        data.rewind();
        switch (getBufferDataType()) {
            case DataBuffer.TYPE_BYTE:
                ByteBuffer buffer = _new(data.limit(), false);
                while (data.hasRemaining()) {
                    cloned.data = buffer.put(((ByteBuffer) data).get());
                }
                break;
            case DataBuffer.TYPE_DOUBLE:
                DoubleBuffer bufferDouble = _newd(data.limit(), false);
                while (data.hasRemaining()) {
                    cloned.data = bufferDouble.put(((DoubleBuffer) data).get());
                }
                break;
            case DataBuffer.TYPE_FLOAT:
                FloatBuffer bufferFloat = _newf(data.limit(), false);
                while (data.hasRemaining()) {
                    cloned.data = bufferFloat.put(((FloatBuffer) data).get());
                }
                break;
            case DataBuffer.TYPE_INT:
                IntBuffer bufferInt = _newi(data.limit(), false);
                while (data.hasRemaining()) {
                    cloned.data = bufferInt.put(((IntBuffer) data).get());
                }
                break;
            case DataBuffer.TYPE_USHORT:
            case DataBuffer.TYPE_SHORT:
                ShortBuffer bufferShort = _news(data.limit(), false);
                while (data.hasRemaining()) {
                    cloned.data = bufferShort.put(((ShortBuffer) data).get());
                }
                break;
            default:
                throw new CloneNotSupportedException("unsupported data type");
        }
        data.rewind();
        cloned.data.rewind();
        return cloned;
    }

    private void endWrite(ObjectOutput out) throws IOException {
        if (JXAenvUtils._debug) {
            System.out.println("write a " + toString());
        }
        out.writeInt(getBufferDataType());
        out.writeInt(data.limit());
        data.rewind();
        if (data.limit() > 0) {
            switch (getBufferDataType()) {
                case DataBuffer.TYPE_BYTE:
                    out.writeBoolean(((ByteBuffer) data).hasArray());
                    ((ByteBuffer) data).order(ByteOrder.nativeOrder());
                    while (((ByteBuffer) data).hasRemaining()) {
                        out.writeByte(((ByteBuffer) data).get());
                    }
                    break;
                case DataBuffer.TYPE_DOUBLE:
                    out.writeBoolean(((DoubleBuffer) data).hasArray());
                    while (((DoubleBuffer) data).hasRemaining()) {
                        out.writeDouble(((DoubleBuffer) data).get());
                    }
                    break;
                case DataBuffer.TYPE_FLOAT:
                    out.writeBoolean(((FloatBuffer) data).hasArray());
                    while (((FloatBuffer) data).hasRemaining()) {
                        out.writeFloat(((FloatBuffer) data).get());
                    }
                    break;
                case DataBuffer.TYPE_INT:
                    out.writeBoolean(((IntBuffer) data).hasArray());
                    while (((IntBuffer) data).hasRemaining()) {
                        out.writeInt(((IntBuffer) data).get());
                    }
                    break;
                case DataBuffer.TYPE_USHORT:
                case DataBuffer.TYPE_SHORT:
                    out.writeBoolean(((ShortBuffer) data).hasArray());
                    while (((ShortBuffer) data).hasRemaining()) {
                        out.writeShort(((ShortBuffer) data).get());
                    }
                    break;
                default:
                    throw new IOException("incompatible or unknown buffer type :" + toString());
            }
            data.rewind();
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        int currentPty = Thread.currentThread().getPriority();
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        out.writeLong(hash);
        endWrite(out);
        out.flush();
        Thread.currentThread().setPriority(currentPty);
    }

    private void endRead(ObjectInput in) throws IOException, ClassNotFoundException {
        int dataType = in.readInt();
        int length = in.readInt();
        if (length <= 0) {
            data = _new(0, false);
            switch (dataType) {
                case DataBuffer.TYPE_BYTE:
                    break;
                case DataBuffer.TYPE_DOUBLE:
                    data = ((ByteBuffer) data).asDoubleBuffer();
                    break;
                case DataBuffer.TYPE_FLOAT:
                    data = ((ByteBuffer) data).asFloatBuffer();
                    break;
                case DataBuffer.TYPE_INT:
                    data = ((ByteBuffer) data).asIntBuffer();
                    break;
                case DataBuffer.TYPE_USHORT:
                case DataBuffer.TYPE_SHORT:
                    data = ((ByteBuffer) data).asShortBuffer();
                    break;
                default:
                    throw new IOException("incompatible or unknown buffer type :" + toString());
            }
        } else {
            boolean isArray = in.readBoolean();
            switch (dataType) {
                case DataBuffer.TYPE_BYTE:
                    data = _new(length, false);
                    for (int i = 0; i < length; i++) {
                        ((ByteBuffer) data).put(in.readByte());
                    }
                    break;
                case DataBuffer.TYPE_DOUBLE:
                    data = _newd(length, false);
                    for (int i = 0; i < length; i++) {
                        ((DoubleBuffer) data).put(in.readDouble());
                    }
                    break;
                case DataBuffer.TYPE_FLOAT:
                    data = _newf(length, false);
                    for (int i = 0; i < length; i++) {
                        ((FloatBuffer) data).put(in.readFloat());
                    }
                    break;
                case DataBuffer.TYPE_INT:
                    data = _newi(length, false);
                    for (int i = 0; i < length; i++) {
                        ((IntBuffer) data).put(in.readInt());
                    }
                    break;
                case DataBuffer.TYPE_USHORT:
                case DataBuffer.TYPE_SHORT:
                    data = _news(length, false);
                    for (int i = 0; i < length; i++) {
                        ((ShortBuffer) data).put(in.readShort());
                    }
                    break;
                default:
                    throw new IOException("incompatible or unknown buffer type :" + toString());
            }
            data.rewind();
            if (JXAenvUtils._debug) {
                System.out.println("read a " + toString());
            }
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int currentPty = Thread.currentThread().getPriority();
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        hash = in.readLong();
        endRead(in);
        Thread.currentThread().setPriority(currentPty);
    }

    private static class WeakBuffer<T> extends WeakReference<T> {

        final long key = System.nanoTime(); /*
         * always make data member final
         */

        final ReferenceQueue myQueue;
        final boolean reuse;
        final int byteSize;

        public WeakBuffer(int byteSize, T ref, ReferenceQueue q, boolean reuse) {
            super(ref, q);
            this.byteSize = byteSize;
            myQueue = q;
            this.reuse = reuse;
        }

        public WeakBuffer(int byteSize, T ref, ReferenceQueue myQueue) {
            this(byteSize, ref, myQueue, true);
        }

        /**
         * As of java doc, WeakRerence gets enqueued when its referent is no
         * longer strongly reachable (faster than SOftReference, and before the
         * heap is full). This overriding function will store the referent into
         * a new WeakReference, so that the
         * {@linkplain #_findVacantBuffer(net.sf.jiga.xtended.impl.system.BufferIO.Data, int)}
         * function finds the referent Buffer instance to be re-used with (@link
         * Reference#get()}
         */
        @Override
        public void clear() {
            if (reuse) {
                /**
                 * as a WeakReference (enqueueClear false) it is enqueued with
                 * the referent Buffer to re-use
                 */
                WeakBuffer sReuse = new WeakBuffer(byteSize, get(), myQueue, false);
                _pool_stackAdd(sReuse);
                sReuse.enqueue();
            }
            super.clear();
        }
    }
    /**
     * Data.TYPE Buffer Maps
     */
    private final static Map<Data, ReferenceQueue<Buffer>> pool = Collections.synchronizedMap(new EnumMap<Data, ReferenceQueue<Buffer>>(Data.class));
    /**
     * Data.TYPE Buffer Maps
     */
    private final static Map<Data, ReferenceQueue<Buffer>> pool_s = Collections.synchronizedMap(new EnumMap<Data, ReferenceQueue<Buffer>>(Data.class));
    /**
     * living Buffers
     */
    private final static Map<Long, WeakBuffer<Buffer>> stack = Collections.synchronizedMap(new HashMap<Long, WeakBuffer<Buffer>>());
    /**
     * Because all returned Buffer's are direct and need to be re-used as often
     * as possible, this value defines the capacity limit between small and
     * large pools (where the Buffer's are gathered by GC for later re-use).
     *
     * @default 16 * 8 Bytes = 128B (a DoubleBuffer of 16 values)
     */
    public static int _BUFFER_BYTE_THRESHOLD = 16 * 8;
    /**
     * Use {@linkplain Dimension#setSize(int,int)} to change size !
     */
    public final static Dimension _WRITE_TILES_DIMENSION = new Dimension(128, 128);

    /**
     * only buffers of size smaller or equal to the tiles will be reused.
     */
    private static int _getBufferMaxReuseBYTES() {
        return _WRITE_TILES_DIMENSION.width * _WRITE_TILES_DIMENSION.height * 4;
    }

    static {
        for (Data t : Data.values()) {
            pool.put(t, new ReferenceQueue<Buffer>());
            pool_s.put(t, new ReferenceQueue<Buffer>());
        }
    }

    /**
     * a new Buffer of type Data.type is instanciated and registered as a
     * WeakRerence (then it might be reused when collected by the GC).
     */
    private static Buffer __new(int capacity, Data t, boolean direct) {
        int byteSize = capacity * t.byteSize;
        Buffer bb = (direct ? ByteBuffer.allocateDirect(byteSize) : ByteBuffer.allocate(byteSize)).order(ByteOrder.nativeOrder());
        switch (t.type) {
            case DataBuffer.TYPE_DOUBLE:
                bb = ((ByteBuffer) bb).asDoubleBuffer();
                break;
            case DataBuffer.TYPE_FLOAT:
                bb = ((ByteBuffer) bb).asFloatBuffer();
                break;
            case DataBuffer.TYPE_INT:
                bb = ((ByteBuffer) bb).asIntBuffer();
                break;
            case DataBuffer.TYPE_SHORT:
            case DataBuffer.TYPE_USHORT:
                bb = ((ByteBuffer) bb).asShortBuffer();
                break;
            default:
                break;
        }
        /**
         * stop re-pooling when buffer is too large or free memory too small
         */
        boolean reuse = byteSize <= _getBufferMaxReuseBYTES() || Runtime.getRuntime().freeMemory() > (20. / 100.) * Runtime.getRuntime().maxMemory();
        Map<Data, ReferenceQueue<Buffer>> p = byteSize > _BUFFER_BYTE_THRESHOLD ? pool : pool_s;
        WeakBuffer sb = new WeakBuffer(byteSize, bb, p.get(t), reuse);
        _pool_stackAdd(sb);
        return bb;
    }
    /**
     * total BufferIO pools size (in bytes)
     */
    private static long _pool_stackByteSize = 0;

    public static long getPool_stackByteSize() {
        return _pool_stackByteSize;
    }

    private static void _pool_stackAdd(WeakBuffer wb) {
        stack.put(wb.key, wb);
        _pool_stackByteSize += wb.byteSize;
    }

    private static void _pool_stackRemove(WeakBuffer wb) {
        stack.remove(wb.key);
        _pool_stackByteSize -= wb.byteSize;
    }
    /**
     * use DebugMap to disable REUSE of Buffer's. That is if {@link DebugMap#isDebugLevelEnabled(net.sf.jiga.xtended.kernel.Level)
     * } returns true for this variable, then REUSE is disabled.
     *
     * @default REUSE is enabled, or it will lead to unwanted
     * java.lang.OutOfMemoryError: ...growableArray.cpp. Out of swap space
     */
    public final static Level DBUG_disableREUSE = DebugMap._getInstance().newDebugLevel();

    /**
     * poll for any existing t-type Buffer instance that can be re-used
     *
     * @param t Buffer type (Double,Float,Byte,etc.)
     * @param capacity
     * @return a ero-filled buffer whose limit is set to capacity but whose
     * capacity may be greater.
     */
    protected static Buffer _findVacantBuffer(Data t, int capacity) {
        if (DebugMap._getInstance().isDebugLevelEnabled(DBUG_disableREUSE)) {
            return null;
        }
        WeakBuffer<Buffer> r;
        /**
         * use one of the two pools of buffers (big and small capacities)
         */
        Map<Data, ReferenceQueue<Buffer>> p = capacity * t.byteSize > _BUFFER_BYTE_THRESHOLD ? pool : pool_s;
        Buffer bb = null;
        /**
         * poll for any queued weak reference
         */
        while ((r = (WeakBuffer<Buffer>) p.get(t).poll()) != null) {
            /**
             * remove weakreference from pool because it has been discovered by
             * the referenceQueue
             */
            _pool_stackRemove(r);
            if (r.get() instanceof Buffer) {
                if (JXAenvUtils._debug) {
                    System.out.println("found one vacant buffer " + t);
                }
                bb = r.get();
                bb.clear();
                if (r.reuse) {
                    /**
                     * buffer is going to be reused
                     */
                    WeakBuffer sr = new WeakBuffer(capacity * t.byteSize, bb, p.get(t));
                    _pool_stackAdd(sr);
                    if (bb.capacity() >= capacity) {
                        /**
                         * buffer is of the correct size we can reuse it
                         */
                        bb.limit(capacity);
                        bb.rewind();
                        break;
                    } else {
                        /**
                         * not the buffer we want, re-enqueue it
                         */
                        sr.enqueue();
                        bb = null;
                    }
                }
            }
        }
        return bb;
    }

    /**
     * fills the buffer with zero from its current position int the remaining
     * space (remaining())
     *
     * @return the same buffer with zero, position is zero (rewind()).
     */
    public static <T extends Buffer> T _zero(T b) {
        Data d = Data._findType(b);
        switch (d.type) {
            case DataBuffer.TYPE_BYTE:
                byte[] a = new byte[b.remaining()];
                Arrays.fill(a, (byte) 0);
                b = (T) ((ByteBuffer) b).put(a);
                break;
            case DataBuffer.TYPE_DOUBLE:
                double[] ad = new double[b.remaining()];
                Arrays.fill(ad, (double) 0);
                b = (T) ((DoubleBuffer) b).put(ad);
                break;
            case DataBuffer.TYPE_FLOAT:
                float[] af = new float[b.remaining()];
                Arrays.fill(af, (float) 0);
                b = (T) ((FloatBuffer) b).put(af);
                break;
            case DataBuffer.TYPE_INT:
                int[] ai = new int[b.remaining()];
                Arrays.fill(ai, (int) 0);
                b = (T) ((IntBuffer) b).put(ai);
                break;
            case DataBuffer.TYPE_SHORT:
            case DataBuffer.TYPE_USHORT:
                short[] as = new short[b.remaining()];
                Arrays.fill(as, (short) 0);
                b = (T) ((ShortBuffer) b).put(as);
                break;
            default:
                break;
        }
        return (T) b.flip();
    }

    /**
     * finds a vacant or creates a direct, native ordered (byte)buffer
     *
     * @param capacity
     * @return
     */
    public static ByteBuffer _new(int capacity) {
        return _new(capacity, true);
    }

    /**
     * finds a vacant or creates a direct, native ordered (byte)buffer
     *
     * @param capacity
     * @param direct
     * @return
     */
    public static ByteBuffer _new(int capacity, boolean direct) {
        return (ByteBuffer) _new(capacity, direct, Data.BYTE);
    }

    /**
     * return a buffer of the specified Buffer class
     */
    public static <T extends Buffer> T _new(int capacity, boolean direct, Class<T> type) {
        return type.cast(_new(capacity, direct, Data._findTypeFromBufferClass(type)));
    }

    /**
     * return a buffer of the specified type
     */
    public static Buffer _new(int capacity, boolean direct, Data type) {
        Buffer bb = _findVacantBuffer(type, capacity);
        return bb == null ? __new(capacity, type, direct) : direct && !bb.isDirect() ? new BufferIO(bb).getData() : bb;
    }

    /**
     * wraps to a direct buffer
     *
     * @param array
     * @return
     */
    public static ByteBuffer _wrap(byte[] array) {
        return _wrap(array, true);
    }

    /**
     * wraps to a buffer
     *
     * @param array
     * @return
     */
    public static ByteBuffer _wrap(byte[] array, boolean direct) {
        return (ByteBuffer) _new(array.length, direct).put(array).flip();
    }

    /**
     * creates a direct, native ordered (double)buffer
     *
     * @param capacity
     * @return
     */
    public static DoubleBuffer _newd(int capacity) {
        return _newd(capacity, true);
    }

    /**
     * creates a direct, native ordered (double)buffer
     *
     * @param capacity
     * @param direct
     * @return
     */
    public static DoubleBuffer _newd(int capacity, boolean direct) {
        return _new(capacity, direct, DoubleBuffer.class);
    }

    /**
     * wraps to a direct buffer
     *
     * @param array
     * @return
     */
    public static DoubleBuffer _wrapd(double[] array) {
        return _wrapd(array, true);
    }

    /**
     * wraps to a buffer
     *
     * @param array
     * @return
     */
    public static DoubleBuffer _wrapd(double[] array, boolean direct) {
        return (DoubleBuffer) _newd(array.length, direct).put(array).flip();
    }

    /**
     * creates a direct native ordered (float)buffer
     *
     * @param capacity
     * @return
     */
    public static FloatBuffer _newf(int capacity) {
        return _newf(capacity, true);
    }

    /**
     * creates a native ordered (float)buffer
     *
     * @param capacity
     * @param direct
     * @return
     */
    public static FloatBuffer _newf(int capacity, boolean direct) {
        return _new(capacity, direct, FloatBuffer.class);
    }

    /**
     * wraps to a direct buffer
     *
     * @param array
     * @return
     */
    public static FloatBuffer _wrapf(float[] array) {
        return _wrapf(array, true);
    }

    /**
     * wraps to a buffer
     *
     * @param array
     * @return
     */
    public static FloatBuffer _wrapf(float[] array, boolean direct) {
        return (FloatBuffer) _newf(array.length, direct).put(array).flip();
    }

    /**
     * creates a direct, native ordered (short)buffer
     *
     * @param capacity
     * @return
     */
    public static ShortBuffer _news(int capacity) {
        return _news(capacity, true);
    }

    /**
     * creates a native ordered (short)buffer
     *
     * @param capacity
     * @param direct
     * @return
     */
    public static ShortBuffer _news(int capacity, boolean direct) {
        return _new(capacity, direct, ShortBuffer.class);
    }

    /**
     * wraps to a direct buffer
     *
     * @param array
     * @return
     */
    public static ShortBuffer _wraps(short[] array) {
        return _wraps(array, true);
    }

    /**
     * wraps to a buffer
     *
     * @param array
     * @return
     */
    public static ShortBuffer _wraps(short[] array, boolean direct) {
        return (ShortBuffer) _news(array.length, direct).put(array).flip();
    }

    /**
     * creates a direct, native ordered (int)buffer
     *
     * @param capacity
     * @return
     */
    public static IntBuffer _newi(int capacity) {
        return _newi(capacity, true);
    }

    /**
     * creates a native ordered (int)buffer
     *
     * @param capacity
     * @param direct
     * @return
     */
    public static IntBuffer _newi(int capacity, boolean direct) {
        return _new(capacity, direct, IntBuffer.class);
    }

    /**
     * wraps to a direct buffer
     *
     * @param array
     * @return
     */
    public static IntBuffer _wrapi(int[] array) {
        return _wrapi(array, true);
    }

    /**
     * wraps to a buffer
     *
     * @param array
     * @return
     */
    public static IntBuffer _wrapi(int[] array, boolean direct) {
        return (IntBuffer) _newi(array.length, direct).put(array).flip();
    }

    /**
     * useful to convert from non-direct buffer or simply make a copy of any
     * buffer to a direct buffer
     *
     * @param <C>
     * @param buf
     * @return
     */
    public static <C extends Buffer> C _cp(C buf) {
        return _cp(buf, true);
    }

    /**
     * useful to convert from non-direct buffer or simply make a copy of any
     * buffer to a buffer
     *
     * @param <C>
     * @param buf
     * @return
     */
    public static <C extends Buffer> C _cp(C buf, boolean direct) {
        try {
            BufferIO bio = new BufferIO(buf);
            return (C) (direct ? bio.getData() : ((BufferIO<C>) bio.clone()).accessData());
        } catch (CloneNotSupportedException ex) {
            if (JXAenvUtils._debug) {
                ex.printStackTrace();
            }
            return null;
        }
    }

    /**
     * (direct buffer) maps a sequence of values in the specified array and
     * returned it with {@link ByteBuffer#slice()}
     *
     * @param <T>
     * @param buffer
     * @param offset
     * @param length
     * @return a direct Buffer that is sharing a subsequence of the specified
     * original direct buffer
     */
    public static <T extends Buffer> T _map(T buffer, int offset, int length) {
        int position = buffer.position();
        int limit = buffer.limit();
        buffer = (T) buffer.limit(offset + length).position(offset);
        Data t = Data._findType(buffer);
        T b = buffer;
        switch (t.type) {
            case DataBuffer.TYPE_BYTE:
                b = (T) ((ByteBuffer) buffer).slice();
                break;
            case DataBuffer.TYPE_INT:
                b = (T) ((IntBuffer) buffer).slice();
                break;
            case DataBuffer.TYPE_FLOAT:
                b = (T) ((FloatBuffer) buffer).slice();
                break;
            case DataBuffer.TYPE_DOUBLE:
                b = (T) ((DoubleBuffer) buffer).slice();
                break;
            case DataBuffer.TYPE_SHORT:
                b = (T) ((ShortBuffer) buffer).slice();
                break;
            default:
                if (JXAenvUtils._debug) {
                    System.err.println(JXAenvUtils.log("unknown buffer type", JXAenvUtils.LVL.SYS_WRN));
                }
                break;
        }
        buffer.limit(limit).position(position);
        return b;

    }

    public static <A> A[] reverseOrder(A[] o) {
        List<A> l = Arrays.asList(o);
        Collections.sort(l, Collections.reverseOrder());
        return (A[]) l.toArray();
    }

    /**
     * running on an LITTLE_ENDIAN system ?
     */
    public static boolean isLSBEnv() {
        return ByteOrder.nativeOrder().toString().equals(ByteOrder.LITTLE_ENDIAN.toString());
    }

    /**
     * DataBuffer types are recognized through this enum
     */
    public static enum Data {

        /**
         *
         */
        BYTE(DataBuffer.TYPE_BYTE, 1, ByteBuffer.class),
        /**
         *
         */
        DOUBLE(DataBuffer.TYPE_DOUBLE, 8, DoubleBuffer.class),
        /**
         *
         */
        FLOAT(DataBuffer.TYPE_FLOAT, 4, FloatBuffer.class),
        /**
         *
         */
        INT(DataBuffer.TYPE_INT, 4, IntBuffer.class),
        /**
         *
         */
        SHORT(DataBuffer.TYPE_SHORT, 2, ShortBuffer.class),
        /**
         *
         */
        USHORT(DataBuffer.TYPE_USHORT, 2, ShortBuffer.class),
        /**
         *
         */
        UNDEFINED(DataBuffer.TYPE_UNDEFINED, 0, Buffer.class);
        /**
         * a {@link DataBuffer} type
         */
        public int type;
        /**
         * Byte size required for one value in this buffer type
         */
        public int byteSize;

        /**
         * corresponding Buffer class
         */
        public Class<? extends Buffer> clazz;

        Data(int type, int byteSize, Class<? extends Buffer> c) {
            this.type = type;
            this.byteSize = byteSize;
            this.clazz = c;
        }

        /**
         *
         * @param b
         * @return
         */
        public static Data _findType(Buffer b) {
            if (b instanceof ByteBuffer) {
                return BYTE;
            }
            if (b instanceof IntBuffer) {
                return INT;
            }
            if (b instanceof DoubleBuffer) {
                return DOUBLE;
            }
            if (b instanceof FloatBuffer) {
                return FLOAT;
            }
            if (b instanceof ShortBuffer) {
                return SHORT;
            }
            return UNDEFINED;
        }

        /**
         *
         * @param type
         * @return
         */
        public static String _findTypeName(int type) {
            for (Data d : Data.values()) {
                if (d.type == type) {
                    return d.name();
                }
            }
            return UNDEFINED.name();
        }

        /**
         * @return byteSize of the specified DataBuffer
         */
        public static long _findDataByteSize(DataBuffer d) {
            BufferIO.Data t = BufferIO.Data.valueOf(BufferIO.Data._findTypeName(d.getDataType()));
            return d.getSize() * d.getNumBanks() * t.byteSize;
        }

        /**
         * @return corresponding Data type for the specified Buffer class
         */
        public static Data _findTypeFromBufferClass(Class<? extends Buffer> clazz) {
            for (Data d : Data.values()) {
                if (d.clazz.equals(clazz)) {
                    return d;
                }
            }
            return UNDEFINED;
        }
    }
}
