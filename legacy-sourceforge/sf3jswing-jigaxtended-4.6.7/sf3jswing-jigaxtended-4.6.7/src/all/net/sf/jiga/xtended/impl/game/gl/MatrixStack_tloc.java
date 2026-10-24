/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.game.gl;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Stack;
import net.sf.jiga.xtended.JXAException;
import net.sf.jiga.xtended.impl.system.BufferIO;
import static net.sf.jiga.xtended.kernel.JXAenvUtils.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.Util;

/**
 * unlimited matrix stack with ThreadLocal storage
 *
 * @author www.b23prodtm.info
 */
public class MatrixStack_tloc {

    private ThreadLocal<Stack<FloatBuffer>> mStack = new ThreadLocal<Stack<FloatBuffer>>();
    private ThreadLocal<Stack<IntBuffer>> mStackMode = new ThreadLocal<Stack<IntBuffer>>();
    int stackPointer;

    public MatrixStack_tloc(int stackPointer) {
        this.stackPointer = stackPointer;
    }

    /**
     * returns the current top of the stack matrix in a FloatBuffer
     *
     * @return the current top of the stack matrix in a FloatBuffer
     * @param stackPointer supported values are {@linkplain GL11#GL_MODELVIEW_MATRIX}, {@linkplain GL11#GL_PROJECTION_MATRIX}
     * @see GL11#glGetFloat(int, FloatBuffer)
     */
    public static FloatBuffer _GLgetTopOftheStackMX(int stackPointer) {
        FloatBuffer modelViewMX = null;
        switch (stackPointer) {
            case GL11.GL_MODELVIEW_MATRIX:
            case GL11.GL_PROJECTION_MATRIX:
                modelViewMX = BufferIO._newf(16);
                break;
            default:
                System.err.println(log("invalid GL stack pointer", LVL.APP_WRN));
                break;
        }
        if (modelViewMX instanceof FloatBuffer) {
            GL11.glGetFloat(stackPointer, modelViewMX);
            Util.checkGLError();
            return modelViewMX;
        } else {
            return BufferIO._newf(16);
        }
    }

    /**
     * @param stackPointer supported values are {@linkplain GL11#GL_MODELVIEW_MATRIX}, {@linkplain GL11#GL_PROJECTION_MATRIX}
     * @param mx 16 values matrix
     */
    public static void _GLloadMX(int stackPointer, FloatBuffer mx) {
        GL11.glMatrixMode(_GLMatrixMode(stackPointer));
        GL11.glLoadMatrix(mx);
    }

    /**
     * @return associated mode for the stackPointer (GL_..._MATRIX)
     */
    public static int _GLMatrixMode(int stackPointer) {
        switch (stackPointer) {
            case GL11.GL_MODELVIEW_MATRIX:
                return GL11.GL_MODELVIEW;
            case GL11.GL_PROJECTION_MATRIX:
                return GL11.GL_PROJECTION;
            default:
                throw new JXAException("invalid GL stack pointer");
        }
    }

    /**
     * stores current stack mode and top of the stack matrix and switch to the
     * stackPointer mode
     */
    public void push() {
        if (mStack.get() == null) {
            mStack.set(new Stack<FloatBuffer>());
        }
        if (mStackMode.get() == null) {
            mStackMode.set(new Stack<IntBuffer>());
        }
        IntBuffer mode = BufferIO._newi(16);
        GL11.glGetInteger(GL11.GL_MATRIX_MODE, mode);
        mStackMode.get().push(mode);
        mStack.get().push(GLHandler._GLgetTopOftheStackMX(stackPointer));
        GL11.glMatrixMode(_GLMatrixMode(stackPointer));
        Util.checkGLError();
    }

    /**
     * restores the matrix on the stackPointer stack and last stack mode
     */
    public void pop() {
        GLHandler._GLloadMX(stackPointer, mStack.get().pop());
        GL11.glMatrixMode(mStackMode.get().pop().get(0));
        Util.checkGLError();
    }
}
