/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.kernel;

import com.apple.eawt.*;
import net.sf.jiga.xtended.ui.JFCFrame;

/**
 *
 * @author www.b23prodtm.info
 */
public class JFCFrameApp {

    private static Application fApplication = Application.getApplication();

    /*public static void quit(ApplicationEvent e) {
     System.exit(0);
     }*/
    public static void main(final String args[]) {
        ThreadWorks.Swing.invokeLater(new Runnable() {
            public void run() {
                final JFCFrame frame = JFCFrame.launchFrame(args);
                /*fApplication.addApplicationListener(new ApplicationAdapter(){

                 @Override
                 public void handleQuit(ApplicationEvent ae) {
                 frame.destroyComponents();
                 ae.setHandled(true);
                 System.exit(0);
                 }

                 @Override
                 public void handleOpenFile(ApplicationEvent ae) {
                 boolean ret = frame.open(ae.getFilename());
                 ae.setHandled(ret);}
                    
                 });*/
                fApplication.setPreferencesHandler(null);
                fApplication.setQuitHandler(new QuitHandler() {
                    public void handleQuitRequestWith(AppEvent.QuitEvent qe, QuitResponse qr) {
                        frame.destroyComponents();
                        qr.performQuit();
                    }
                });
                fApplication.setOpenFileHandler(new OpenFilesHandler() {
                    public void openFiles(AppEvent.OpenFilesEvent ofe) {
                        frame.open(ofe.getSearchTerm());
                    }
                });
                fApplication.setOpenURIHandler(new OpenURIHandler() {
                    public void openURI(AppEvent.OpenURIEvent ourie) {
                        frame.open(ourie.getURI());
                    }
                });
                fApplication.setQuitStrategy(QuitStrategy.SYSTEM_EXIT_0);
            }
        });
    }
}
