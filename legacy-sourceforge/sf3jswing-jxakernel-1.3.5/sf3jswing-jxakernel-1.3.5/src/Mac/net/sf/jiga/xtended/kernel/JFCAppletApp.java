/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.kernel;

import com.apple.eawt.*;
import net.sf.jiga.xtended.ui.JFCApplet;

/**
 *
 * @author www.b23prodtm.info
 */
public class JFCAppletApp {

    private static final Application fApplication = Application.getApplication();

    /*    public static void quit(ApplicationEvent e) {
     System.exit(0);
     }*/
    public static void main(final String args[]) {
        ThreadWorks.Swing.invokeLater(new Runnable() {

            public void run() {
                final JFCApplet applet = JFCApplet.launchAppletFrame(args);
                /*fApplication.addApplicationListener(new ApplicationAdapter(){

                 @Override
                 public void handleQuit(ApplicationEvent ae) {
                 applet.destroy();
                 ae.setHandled(true);
                 System.exit(0);
                 }

                 @Override
                 public void handleOpenFile(ApplicationEvent ae) {
                 boolean ret = applet.open(ae.getFilename());
                 ae.setHandled(ret);}
                    
                 }); */
                fApplication.setPreferencesHandler(null);
                fApplication.setQuitHandler(new QuitHandler() {

                    public void handleQuitRequestWith(AppEvent.QuitEvent qe, QuitResponse qr) {
                        applet.destroy();
                        qr.performQuit();
                    }
                });
                fApplication.setOpenFileHandler(new OpenFilesHandler() {

                    public void openFiles(AppEvent.OpenFilesEvent ofe) {
                        applet.open(ofe.getSearchTerm());
                    }
                });
                fApplication.setOpenURIHandler(new OpenURIHandler() {

                    public void openURI(AppEvent.OpenURIEvent ourie) {
                        applet.open(ourie.getURI());
                    }
                });
                fApplication.setQuitStrategy(QuitStrategy.SYSTEM_EXIT_0);
            }
        });
    }
}
