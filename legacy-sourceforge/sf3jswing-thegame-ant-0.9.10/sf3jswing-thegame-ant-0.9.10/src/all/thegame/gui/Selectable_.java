/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package thegame.gui;

import java.awt.Color;
import javax.swing.JComponent;
import net.sf.jiga.xtended.impl.system.LogicalSystem;
import thegame.FieldGui;

/**
 *
 * @author www.b23prodtm.info
 */
public abstract class Selectable_ extends LogicalSystem {

        protected JComponent component;
        protected int UISelectingPlayer;

        public int getUISelectingPlayer() {
            return UISelectingPlayer;
        }

        public void setUISelectingPlayer(int UISelectingPlayer) {
            this.UISelectingPlayer = UISelectingPlayer;            
            selectionColor = FieldGui.defaultSelectColors[UISelectingPlayer];
        }
        Color selectionColor;

        public Color getSelectionColor() {
            return selectionColor;
        }

        public Selectable_(JComponent component) {
            super(component == null ? "" : component.getName());
            assert component instanceof JComponent : "please set the component to control !";
            this.component = component;
            setUISelectingPlayer(0);
        }
    }
