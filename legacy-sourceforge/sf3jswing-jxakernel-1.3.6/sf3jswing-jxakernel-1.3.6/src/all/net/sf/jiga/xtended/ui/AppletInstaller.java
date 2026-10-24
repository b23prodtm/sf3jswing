/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.ui;

import net.sf.jiga.xtended.kernel.ExtensionsInstaller;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.sf.jiga.xtended.kernel.JXAenvUtils;
import net.sf.jiga.xtended.kernel.ThreadWorks;

/**
 * This class "installs" every extensions .jar into a platform-specific folder (known by the JXAkernel only, usually user.home/.JXAStore directory)
 * @deprecated used to handle setup for Applets that required extensions installation. (Extensions Mechanism) as of the JXAkernel 1.1.5 it is substitued by an {@linkplain ExtensionsInstaller#_installExtensions(java.util.Map, boolean, java.io.File)} launched by the {@linkplain JFCApplet}.
 * @author www.b23prodtm.info
 */
public class AppletInstaller extends JApplet {

    /**
     * Initialization method that will be called after the applet is loaded
     * into the browser.
     */
    public void init() {
        getContentPane().setLayout(new BorderLayout());
        JPanel panel = new JPanel(new GridBagLayout(), true);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10,10,10,10);
        c.gridwidth = c.REMAINDER;
        c.weightx = .5;
        panel.add(new JButton(new AbstractAction("Install / Update...") {

            public void actionPerformed(ActionEvent e) {
                twks.doLater(new Runnable() {

                    public void run() {
                        ExtensionsInstaller.main(new String[]{"-keepAlive", "-install"});
                    }
                });                
            }
        }), c);
        panel.add(new JButton(new AbstractAction("Install / Update w/ log...") {

            public void actionPerformed(ActionEvent e) {
                twks.doLater(new Runnable() {

                    public void run() {
                        ExtensionsInstaller.main(new String[]{"-keepAlive", "-install", "-log"});
                    }
                });                
            }
        }), c);
        c.weightx = 1.0;
        panel.add(new JLabel("<html><center>You may want to uninstall " +
                "<br>the Java Extensions from your computer :</center></html>"), c);
        c.gridwidth = c.REMAINDER;
        c.weightx = .5;
        panel.add(new JButton(new AbstractAction("Uninstall...") {

            public void actionPerformed(ActionEvent e) {
                twks.doLater(new Runnable() {

                    public void run() {
                        ExtensionsInstaller.main(new String[]{"-uninstall", "-keepAlive"});
                    }
                });                
            }
        }), c);
        getContentPane().add(panel, BorderLayout.CENTER);
        validate();
    }

    ThreadWorks twks = new ThreadWorks("appletInstaller");
    @Override
    public void start() {
        super.start();
    }
}
