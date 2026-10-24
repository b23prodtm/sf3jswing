package thegame.io;

import java.util.Stack;
/*
 * Recorder.java
 *
 * Created on 5 avril 2007, 05:33
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author www.b23prodtm.info
 */
public class Recorder<V> extends Stack<V> {

    public Recorder() {
        super();
    }

    public String toString() {
        String s = "";
        String sep = "";
        while (!empty()) {
            s += sep + ((V) peek());
            sep = "; ";
            pop();
        }
        return s;
    }
}
