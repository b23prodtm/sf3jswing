/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.jiga.xtended.impl.game.scene;

import net.sf.jiga.xtended.JXAException;
import net.sf.jiga.xtended.impl.game.RenderingScene;

/**
 *
 * @author tiana
 */
public enum State {
        RUNNING(0x2), SUSPENDED(0x1), STOPPED(0x0);
        int S;

        private State(int S) {
                this.S = S;
        }

        public static State valueOf(int state) {
                for (State s : State.values()) {
                        if (s.S == state) {
                                return s;
                        }
                }
                throw new JXAException(JXAException.LEVEL.APP, "Unknown state " + state);
        }
        
}
