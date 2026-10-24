/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.system;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This Class simply softly references the specified object and returns it. It
 * is used by {@link ImagePool} As it becomes not hardly referenced anymore, the
 * object is automatically enqueued for garbage collecting. Also, you have to
 * frequently call {@linkplain #poll()} to get everything collected.
 *
 * @author www.b23prodtm.info
 */
public class RefPool {

    private RefPool() {
        this.refQueue = new ReferenceQueue();
        this.refPool = Collections.synchronizedSet(new HashSet<Reference>());
    }

    public static RefPool getInstance() {
        return RefPoolHolder.INSTANCE;
    }

    private static class RefPoolHolder {

        private static final RefPool INSTANCE = new RefPool();
    }
    /**
     * reference Queue
     */
    private final ReferenceQueue refQueue;

    private final Set<Reference> refPool;

    /**
     * @param <T>
     * @param o
     * @param enqueueNow "true" will make it garbage-collected as, "false" will
     * keep the reference untouched (alive) until the reference is made
     * "weak-reachable"
     * @return
     */
    public <T> T queueRef(T o, final boolean enqueueNow) {
        if (o == null) {
            return null;
        }
        WeakReference ref;
        ref = new WeakReference(o, refQueue) {
            long hash = System.nanoTime();

            @Override
            public int hashCode() {
                return (int) hash;
            }

            @Override
            public boolean equals(Object obj) {
                return obj == null ? false : hashCode() == obj.hashCode();
            }

            @Override
            public void clear() {
                /**
                 * original contract of clear() is to enqueue without the
                 * referent that wouldn't be handled by our cleaner, so do an
                 * enqueueing WITH the referent as follows
                 */
                if (!enqueueNow) {
                    Object i = get();
                    if (i != null) {
                        WeakReference sClear = new WeakReference(i, refQueue);
                        sClear.enqueue();
                    }
                }
                super.clear();
            }
        };
        refPool.add(ref);
        if (enqueueNow) {
            ref.enqueue();
        }
        return o;
    }

    public Reference poll() {
        Reference r = refQueue.poll();
        if (r != null) {
            refPool.remove(r);
        }
        return r;
    }

}
