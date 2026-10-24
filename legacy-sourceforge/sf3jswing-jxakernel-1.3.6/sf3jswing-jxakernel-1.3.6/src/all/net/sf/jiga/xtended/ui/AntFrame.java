/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.ui;

import javax.swing.JFrame;

/**
 * the AntFrame is implemented by the Applets that wants to have a compatible interface for JFCFrame
 * @author www.b23prodtm.info
 */
public interface AntFrame extends Ant {

    public void setFrame(JFrame frame);

    public JFrame getFrame();

    /** destroys the components */
    public void destroyComponents();

    /** inits the JFrame layout and variables */
    public void initComponents();
}
