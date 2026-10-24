/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jxa.fb;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Properties;
import javafx.scene.media.Media;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import jxa.AlbumWeb;
import jxa.BrowserAction;
import jxa.IMCBrowser;
import static jxa.IMCBrowser.facebookIcon_64;
import static jxa.IMCBrowser.getLocalizedStringCap;
import jxa.LocalizedResources;
import jxa.fb.FBConnector.Album;
import jxa.fb.FBConnector.Facebook;
import net.sf.jiga.xtended.ui.UIMessage;

/**
 *
 * @author www.b23prodtm.info
 */
public class FBexportAction extends BrowserAction {

        public FBexportAction(IMCBrowser aThis) {
                super(IMCBrowser.getLocalizedStringCap("exporttoFacebook..."), aThis.facebookIcon, aThis);
        }

        public void actionPerformed(ActionEvent e) {
                Runnable r = new Runnable() {
                        public void run() {
                                if (!instance.waitForAllPictures(instance.imageCollection)) {
                                        return;
                                }
                                String op = "Facebook";
                                boolean interrupt = false;
                                instance.currentProcessThumb.setIcon(facebookIcon_64);
                                long wait = UIMessage.newProgress(0, 100, instance.picView);
                                JDialog pp = instance.showLightPopupActivity(op, instance.currentProcessThumb, wait, null, UIMessage.UI_TOP_RIGHT);
                                UIMessage.hideProgress(wait);
                                try {
                                        Facebook client = instance.FBConnect().getFacebook();
                                        if (client == null) {
                                                return;
                                        }
                                        ArrayList<AlbumWeb> albumsNames = new ArrayList<AlbumWeb>();
                                        for (Album alb : client.albums().values()) {
                                                albumsNames.add(alb);
                                        }
                                        if (albumsNames.isEmpty()) {
                                                UIMessage.showLightPopupMessage(new JLabel(IMCBrowser.getLocalizedStringCap("label17")), null, instance.picView, UIMessage.UI_BOTTOM_RIGHT);
                                                return;
                                        }

                                        String[] lstr = LocalizedResources.localizedGetStringArray("dialog5", 2);
                                        AlbumWeb album = (AlbumWeb) UIMessage.showSelectDialog(instance.picView, lstr[0], lstr[1], albumsNames.toArray(new AlbumWeb[]{}), albumsNames.get(0));
                                        AlbumWeb _default;
                                        albumsNames.add(_default = new AlbumWeb("0", LocalizedResources.localized.getString("newalbum")));
                                        AlbumWeb select = (AlbumWeb) UIMessage.showSelectDialog(instance.picView, lstr[0], lstr[1], albumsNames.toArray(new AlbumWeb[]{}), albumsNames.get(0));
                                        if (select instanceof AlbumWeb) {
                                                if (select.equals(_default)) {
                                                        JPanel panel = new JPanel(true);
                                                        JTextField nameField = new JTextField((String) instance.dbList.getSelectedItem());
                                                        panel.add(new JLabel(getLocalizedStringCap("Enterthenameofthealbumtocreate")));
                                                        panel.add(nameField);
                                                        UIMessage.showPopupMessage(panel, null, instance.picView, UIMessage.UI_CENTER);
                                                        UIMessage.showProgress(wait);
                                                        _default = new Album("", nameField.getText());
                                                        UIMessage.hideProgress(wait);
                                                }
                                                instance.edit_started(wait, op);
                                                System.out.println("upload to : " + select.id);
                                                synchronized (instance.selectedKeys) {
                                                        for (String k : instance.selectedKeys) {
                                                                Properties info = (Properties) instance.imageCollectionInfo.get(k).get(IMCBrowser.INFO_PROP);
                                                                instance.updateCurrentStatus(k, null);
                                                                client.addAlbumPhoto(_default.id, new File(k));
                                                                instance.edit_progress(wait, 1);
                                                        }
                                                }
                                        }
                                        System.out.println("USER:  " + client.getUsername());
                                } catch (Exception ex) {
                                        ex.printStackTrace();
                                        String[] lstr = LocalizedResources.localizedGetStringArray("label16", 2);
                                        UIMessage.showLightPopupMessage(new JLabel(lstr[0] + ex.getMessage() + lstr[1], UIMessage._getIcon(UIMessage.ERROR_TYPE, false), SwingConstants.LEADING), null, instance.picView, UIMessage.UI_BOTTOM_RIGHT);
                                        interrupt = true;
                                } finally {
                                        instance.edit_finished(wait, op, interrupt);
                                        pp.setVisible(false);
                                        UIMessage.kill(wait);
                                }
                        }
                };
                instance.canvas.doTaskOnScreen(r);
        }
}
