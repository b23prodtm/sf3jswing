/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.game.gl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.lwjgl.opengl.GL11;

/**
 * A GLList is used by RenderingScene to define common OpenGL lists callbacks ({@linkplain GL11#glCallList(int)}).
 * All GLList hashCode's must be {@link Object#equals() consistent with equals()}, c.f. java documentation.
 * @author www.b23prodtm.info
 */
public abstract class GLList {

    long hash = System.nanoTime();

    /** returns an instance-unique identifying hash value
    @discussion must be overriden if {@see #equals(Object)} is overriden*/
    @Override
    public int hashCode() {
        return (int) hash;
    }

    /** compares the hashcode
    @discussion must consistently be equal to {@link #hashCode()} == o.hashCode() */
    @Override
    public boolean equals(Object o) {
        return o == null ? false : o.hashCode() == hashCode();
    }

    /** It must return the Runnable list that is compiled and executed each time for this
    GLList. NOTICE : the Runnable must run GL calls ONLY, otherwise the gllist will  behave strangely after the first call.
    @see #_GLListCallback(GLList)*/
    public abstract Runnable getList();
    public static Set<GLList> glLists = (Set<GLList>) Collections.synchronizedSet(new HashSet<GLList>());

    /** The specfied GLList is checked against existing lists with a call to {@linkplain GLList#equals(Object)},
    and if it is found then, {@linkplain GL11#glCallList(int)} runs on; the given GLList is added with a {@linkplain GL11#glNewList(int, int)}--hashcode() referencing this GLList--
    if it is new and backed in {@linkplain #glLists}.
    NOTICE : all GL calls are compiled and executed, but the other API calls {@linkplain #getList() won't}. */
    public static void _GLListCallback(GLList list) {
        if (!glLists.contains(list)) {
            GL11.glNewList(list.hashCode(), GL11.GL_COMPILE);
            list.getList().run();
            GL11.glEndList();
            glLists.add(list);
        }
        GL11.glCallList(list.hashCode());
    }
}
