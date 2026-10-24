/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jxa.pi;

import com.google.gdata.client.photos.PicasawebService;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.media.MediaFileSource;
import com.google.gdata.data.photos.AlbumEntry;
import com.google.gdata.data.photos.PhotoEntry;
import com.google.gdata.data.photos.UserFeed;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import jxa.AlbumWeb;
import jxa.BrowserAction;
import jxa.IMCBrowser;
import static jxa.IMCBrowser.INFO_PROP;
import static jxa.IMCBrowser.INFO_TIMESTAMP;
import static jxa.IMCBrowser.getLocalizedStringCap;
import static jxa.IMCBrowser.picasaIcon_64;
import jxa.LocalizedResources;
import net.sf.jiga.xtended.ui.ImageCollection;
import net.sf.jiga.xtended.ui.UIMessage;
import org.jdesktop.swingx.JXLoginDialog;
import org.jdesktop.swingx.auth.LoginAdapter;
import org.jdesktop.swingx.auth.LoginEvent;

/**
 *
 * @author www.b23prodtm.info
 */
public class PIexportAction extends BrowserAction{
        
        public PIexportAction(IMCBrowser aThis) {
                super(IMCBrowser.getLocalizedStringCap("exporttoPicasa..."), aThis.picasaIcon, aThis);
        }
                        
        public void actionPerformed(ActionEvent e) {
                                final PicasawebService pcweb = new PicasawebService(instance.getClass().getPackage().getSpecificationVendor() + "-" + instance.getClass().getPackage().getSpecificationTitle() + "-" + instance.getClass().getPackage().getSpecificationVersion());
                                JXLoginDialog jlogin = instance.PICASALogin(pcweb, new LoginAdapter() {
                                        public void loginSucceeded(LoginEvent arg0) {
                                                super.loginSucceeded(arg0);
                                                Runnable r = new Runnable() {
                                                        public void run() {
                                                                if (!instance.waitForAllPictures(instance.imageCollection)) {
                                                                        return;
                                                                }
                                                                String op = "Picasa";
                                                                instance.currentProcessThumb.setIcon(picasaIcon_64);
                                                                long wait = UIMessage.newProgress(0, 100, instance.picView);
                                                                JDialog pp = instance.showLightPopupActivity(op, instance.currentProcessThumb, wait, null, UIMessage.UI_TOP_RIGHT);
                                                                boolean interrupt = false;
                                                                try {
                                                                        /* send photos */
                                                                        URL myAlbums = new URL(LocalizedResources.localized.getString("URL_picasaBase") + "?kind=album");
                                                                        UserFeed albums = pcweb.getFeed(myAlbums, UserFeed.class);
                                                                        UIMessage.hideProgress(wait);
                                                                        List<AlbumEntry> entries = albums.getAlbumEntries();
                                                                        AlbumEntry newAlbum = new AlbumEntry();
                                                                        newAlbum.setTitle(new PlainTextConstruct(":" + LocalizedResources.localized.getString("newalbum") + ":"));
                                                                        ArrayList<AlbumWeb> entriesNames = new ArrayList<AlbumWeb>();
                                                                        AlbumWeb defaultEntryName = new AlbumWeb("0", newAlbum.getTitle().getPlainText());
                                                                        entriesNames.add(defaultEntryName);
                                                                        for (AlbumEntry alb : entries) {
                                                                                AlbumWeb a;
                                                                                entriesNames.add(a = new AlbumWeb(alb.getGphotoId(), alb.getTitle().getPlainText()));
                                                                        }
                                                                        String[] lstr = LocalizedResources.localizedGetStringArray("dialog5", 2);
                                                                        AlbumWeb select = (AlbumWeb) UIMessage.showSelectDialog(instance.mainFrame, lstr[0], lstr[1], entriesNames.toArray(new AlbumWeb[]{}), entriesNames.get(0));
                                                                        if (select instanceof AlbumWeb) {
                                                                                if (select.equals(defaultEntryName)) {
                                                                                        JPanel panel = new JPanel(true);
                                                                                        JTextField nameField = new JTextField((String) instance.dbList.getSelectedItem());
                                                                                        panel.add(new JLabel(getLocalizedStringCap("Enterthenameofthealbumtocreate") + " : "));
                                                                                        panel.add(nameField);
                                                                                        UIMessage.showPopupMessage(panel, null, instance.picView, UIMessage.UI_CENTER);
                                                                                        newAlbum.setTitle(new PlainTextConstruct(nameField.getText()));
                                                                                        AlbumEntry selectAlbum = newAlbum;
                                                                                        UIMessage.showProgress(wait);
                                                                                        selectAlbum = pcweb.insert(myAlbums, selectAlbum);
                                                                                        select.id = selectAlbum.getGphotoId();
                                                                                        select.getProps().setProperty("Title", selectAlbum.getTitle().getPlainText());
                                                                                        UIMessage.hideProgress(wait);
                                                                                }
                                                                                URL albumUrl = new URL(LocalizedResources.localized.getString("URL_picasaBase") + "albumid/" + select.id);
                                                                                System.out.println(albumUrl.toString());
                                                                                instance.edit_started(wait, op);
                                                                                synchronized (instance.selectedKeys) {
                                                                                        for (String s : instance.selectedKeys) {
                                                                                                Properties info = (Properties) instance.imageCollectionInfo.get(s).get(INFO_PROP);
                                                                                                instance.updateCurrentStatus(s, null);
                                                                                                MediaFileSource media = new MediaFileSource(new File(s), ImageCollection._getImageMimeType(new File(s), "r"));
                                                                                                PhotoEntry photo = new PhotoEntry();
                                                                                                photo.setTitle(new PlainTextConstruct(info.getProperty("title")));
                                                                                                photo.setDescription(new PlainTextConstruct(info.getProperty("description")));
                                                                                                photo.setTimestamp(new Date((Long) instance.imageCollectionInfo.get(s).get(INFO_TIMESTAMP)));
                                                                                                photo.setMediaSource(media);
                                                                                                pcweb.insert(albumUrl, photo);
                                                                                                instance.edit_progress(wait, 1);
                                                                                        }
                                                                                }
                                                                        }
                                                                } catch (Exception ex) {
                                                                        ex.printStackTrace();
                                                                        String[] lstr = LocalizedResources.localizedGetStringArray("label6", 2);
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
                                });
                                jlogin.setVisible(true);
                        }
                }
