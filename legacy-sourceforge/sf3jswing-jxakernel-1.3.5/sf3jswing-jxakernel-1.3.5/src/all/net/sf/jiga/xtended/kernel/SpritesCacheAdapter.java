/*
 * SpritesCacheAdapter.java
 *
 * Created on 26 avril 2007, 01:48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.sf.jiga.xtended.kernel;

import java.io.Serializable;

/**
 * The SpriteCacheAdapter implements the SpritesCacheListener interface to serve SpritesCacheManager report.
 * @author www.b23prodtm.info
 */
public class SpritesCacheAdapter implements SpritesCacheListener {
        
        long hash = System.nanoTime();

        public int hashCode() {
            return (int) hash;
        }

        public boolean equals(Object o) {
            return o == null ? false : o.hashCode() == hashCode();
        }
        /** not implemented */
        public void writeAborted() {
        }
        
        /** not implemented */
        public void writeCompleted() {
        }
        
        /** not implemented */
        public void writeStarted() {
        }
        
        /** not implemented */
        public void writeError(Exception trace) {
        }
        
        /** not implemented */
        public void capacityExtended(int newCapacity) {
        }
        
        /** not implemented */
        public void readAborted() {
        }
        
        /** not implemented */
        public void readCompleted() {
        }
        
        /** not implemented */
        public void readError(Exception trace) {
        }
        
        /** not implemented */
        public void readStarted() {
        }
    }
