/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.jiga.xtended.impl.game.scene;

import net.sf.jiga.xtended.JXAException;

/**
 * scene timer input
 */
public enum Input {
        STOP(0x0), START(0x1), PAUSE(0x2);
        public int X;

        private Input(int X) {
                this.X = X;
        }

        public static Input valueOf(int input) {
                for (Input x : Input.values()) {
                        if (x.X == input) {
                                return x;
                        }
                }
                throw new JXAException(JXAException.LEVEL.APP, "Unknown state " + input);
        }
        
}
