/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.game.gl;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.jiga.xtended.JXAException;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.Util;

/**
 *
 * @author www.b23prodtm.info
 */
public class SndScratch {

    public final Buffers b = new Buffers();
    public final Sources s = new Sources();

    public static class Buffers extends MemScratch<ByteBuffer> {

        private Buffers() {
            super();
        }

        @Override
        public boolean isBound(int name) {
            return AL10.alIsBuffer(name);
        }

        @Override
        protected void gen(IntBuffer scratch) {            
                try {if (!AL.isCreated()) {
                    AL.create();                
            }
            AL10.alGetError();
            AL10.alGenBuffers(scratch);
            Util.checkALError();
                } catch (LWJGLException ex) {
                throw new JXAException(ex);
                }
        }

        @Override
        protected void delete(IntBuffer scratch) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    public static class Sources extends MemScratch<FloatBuffer> {

        private Sources() {
            super();
        }

        @Override
        public boolean isBound(int name) {
            return AL10.alIsSource(name);
        }

        @Override
        protected void gen(IntBuffer scratch) {            
                try {if (!AL.isCreated()) {
                    AL.create();                
            }
            AL10.alGetError();
            AL10.alGenSources(scratch);
            Util.checkALError();
                } catch (LWJGLException ex) {
                throw new JXAException(ex);
                }
        }

        @Override
        protected void delete(IntBuffer scratch) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
}
