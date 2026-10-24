/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jxa.pi;

import com.google.gdata.client.photos.PicasawebService;
import com.google.gdata.data.media.mediarss.MediaContent;
import com.google.gdata.data.photos.AlbumEntry;
import com.google.gdata.data.photos.AlbumFeed;
import com.google.gdata.data.photos.PhotoEntry;
import com.google.gdata.data.photos.UserFeed;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import jxa.BrowserAction;
import jxa.IMCBrowser;
import static jxa.IMCBrowser.applet;
import static jxa.IMCBrowser.defaultImageDirectory;
import static jxa.IMCBrowser.getLocalizedStringCap;
import static jxa.IMCBrowser.picasaIcon;
import static jxa.IMCBrowser.picasaIcon_64;
import jxa.LocalizedResources;
import net.sf.jiga.xtended.impl.Sprite;
import net.sf.jiga.xtended.kernel.FileHelper;
import net.sf.jiga.xtended.kernel.ThreadWorks;
import net.sf.jiga.xtended.ui.ImageCollection;
import net.sf.jiga.xtended.ui.ImagePluginDescriptor;
import net.sf.jiga.xtended.ui.UIMessage;
import org.jdesktop.swingx.JXLoginDialog;
import org.jdesktop.swingx.auth.LoginAdapter;
import org.jdesktop.swingx.auth.LoginEvent;

/**
 *
 * @author www.b23prodtm.info
 */
public class PIimportAction extends BrowserAction {

        public PIimportAction(IMCBrowser aThis) {
                super(getLocalizedStringCap("importfromPicasa..."), picasaIcon, aThis);
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
                                                int fstate = applet ? 0 : ((JFrame) instance.mainFrame).getExtendedState();
                                                try {
                                                        /* send photos */
                                                        URL myAlbums = new URL(LocalizedResources.localized.getString("URL_picasaBase") + "?kind=album");
                                                        UserFeed albums = pcweb.getFeed(myAlbums, UserFeed.class);
                                                        UIMessage.hideProgress(wait);
                                                        List<AlbumEntry> entries = albums.getAlbumEntries();
                                                        ArrayList<String> entriesNames = new ArrayList<String>();
                                                        for (AlbumEntry alb : entries) {
                                                                entriesNames.add(alb.getTitle().getPlainText() + " {" + alb.getGphotoId() + "}");
                                                        }
                                                        AlbumEntry selectAlbum = null;
                                                        String[] lstr = LocalizedResources.localizedGetStringArray("dialog6", 2);
                                                        String select = (String) UIMessage.showSelectDialog(instance.mainFrame, lstr[0], lstr[1], entriesNames.toArray(new String[]{}), entriesNames.get(0));
                                                        if (select instanceof String) {
                                                                int sn = 0;
                                                                for (String n : entriesNames) {
                                                                        if (select.equals(n)) {
                                                                                selectAlbum = entries.get(sn);
                                                                                break;
                                                                        }
                                                                        sn++;
                                                                }
                                                                if (selectAlbum instanceof AlbumEntry) {

                                                                        instance.fcp.setModeSave(true);
                                                                        instance.fcp.initToPath(defaultImageDirectory.getPath());
                                                                        String dTitle = instance.fcp.getFiles().getDialogTitle();
                                                                        instance.fcp.getFiles().setDialogTitle(getLocalizedStringCap("Selectadirectorytosavethealbumto!"));
                                                                        File[] d = instance.fcp.getDirectory();
                                                                        instance.fcp.getFiles().setDialogTitle(dTitle);
                                                                        if (d.length > 0) {
                                                                                URL albumUrl = new URL(LocalizedResources.localized.getString("URL_picasaBase") + "albumid/" + selectAlbum.getGphotoId());
                                                                                System.out.println(albumUrl.toString());
                                                                                File albumDir = new File(d[0].getPath() + File.separator + selectAlbum.getName());
                                                                                albumDir.mkdirs();
                                                                                FileHelper._makeWritable(albumDir);
                                                                                instance.setImageCollection(instance.newImageCollection(selectAlbum.getName() + " @" + selectAlbum.getLocation()));
                                                                                ThreadWorks.Swing.invokeAndWait(new Runnable() {
                                                                                        public void run() {
                                                                                                instance.updateCollectionDB();
                                                                                        }
                                                                                });
                                                                                AlbumFeed feed = pcweb.getFeed(albumUrl, AlbumFeed.class);
                                                                                instance.edit_started(wait, op, feed.getPhotoEntries().size());
                                                                                int p = 0;
                                                                                for (PhotoEntry photo : feed.getPhotoEntries()) {
                                                                                        MediaContent ms = photo.getMediaContents().get(0);
                                                                                        /*InputStream in = new URL(ms.getUrl()).openStream();
                                                                                         Sprite sp = Sprite._load(ImageIO.createImageInputStream(in));*/
                                                                                        Sprite sp = new Sprite(ImageIO.read(new URL(ms.getUrl())), ms.getType(), new Dimension((int) ((long) photo.getWidth()), (int) ((long) photo.getHeight())));
                                                                                        /*sp.setTileModeEnabled(true);
                                                                                         sp.setStoreMode(Sprite.MODE_JAVA2D | Sprite.MODE_TILE);
                                                                                         */ sp.setBufferedType(ImageCollection._getBufferedType(ms.getType()));
                                                                                        Properties prop = ImageCollection._getPluginProperties(ms.getType());
                                                                                        String[] exts = prop.getProperty(ImagePluginDescriptor.get.extension.toString()).split(" ");
                                                                                        File filePath = new File(albumDir.getPath() + File.separator + p + "." + exts[0]);
                                                                                        sp.runValidate();
                                                                                        sp.writeImageToOutput(filePath, -1f);
                                                                                        sp.clearResource();
                                                                                        /*in.close();*/
                                                                                        instance.open(filePath, filePath.toString(), true);
                                                                                        instance.doBrowserUpdate();
                                                                                        p++;
                                                                                        instance.edit_progress(wait, 1, feed.getPhotoEntries().size());
                                                                                }
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
