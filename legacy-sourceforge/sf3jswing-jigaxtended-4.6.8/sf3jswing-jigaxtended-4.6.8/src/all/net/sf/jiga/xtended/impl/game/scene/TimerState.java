/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.jiga.xtended.impl.game.scene;

import net.sf.jiga.xtended.impl.game.RenderingScene;
import net.sf.jiga.xtended.impl.system.LogicalSystem;

/**
 *
 * @author tiana
 */
public final class TimerState extends LogicalSystem {
        private final RenderingScene scene;
        int s2 = 0x2;
        int s3 = 0x1;
        int x1 = 0x2;
        int x2 = 0x1;

        public TimerState(final RenderingScene scene) {
                super("scene timer state");
                this.scene = scene;
        }

        @Override
        public int getState() {
                return scene.getState().S;
        }

        /**
         * Karnaugh Map (4var) solution : nextState Ouput Z is (Z2;Z3)
         * <br>
         * where Z2 = (~X1) & X2 | X2 & S2 and Z3 = X1 & S3 | X1 & (~X2)
         * & S2 .X2'.S2 with Input S (X1;X2) and State S (S2,S3).
         *
         * @param input corresponding values of {@link Input}
         * @return corresponding values of {@link State}
         */
        @Override
        protected int nextState(int input) {
                //return (lookUp(input, x1, false) && lookUp(input, x2, true) || lookUp(input, x2, true) && lookUp(zState, s2, true) || lookUp(input, x1, true) && lookUp(zState, s2, true) && lookUp(zState, s3, true) ? s2 : 0x0) | (lookUp(input, x1, true) && lookUp(zState, s3, true) || lookUp(input, x1, true) && lookUp(input, x2, false) && lookUp(zState, s2, true) ? s3 : 0x0);
                int X1 = (input & this.x1) >> 1;
                int X2 = input & this.x2;
                int S2 = (scene.getState().S & this.s2) >> 1;
                int S3 = scene.getState().S & this.s3;
                return ((((~X1) & X2 | X2 & S2) << 1) | (X1 & S3 | X1 & (~X2) & S2)) & (this.s2 | this.s3);
        }

        @Override
        protected void setState(int state) {
                scene.setState(State.valueOf(state & (s2 | s3)));
        }

        @Override
        public void reset() {
                setState(0x0);
        }
        
}
