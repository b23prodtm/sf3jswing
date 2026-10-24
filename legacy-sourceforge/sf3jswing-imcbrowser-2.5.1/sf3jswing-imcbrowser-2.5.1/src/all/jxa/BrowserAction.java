/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jxa;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

/**
 *
 * @author tiana
 */
public abstract class BrowserAction extends AbstractAction{
        public IMCBrowser instance;

        public BrowserAction(String name, ImageIcon icon, IMCBrowser instance) {
                super(name, icon);
                this.instance = instance;
        }
        
}
