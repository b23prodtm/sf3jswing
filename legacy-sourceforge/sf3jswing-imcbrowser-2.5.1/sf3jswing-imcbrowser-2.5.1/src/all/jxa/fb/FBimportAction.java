/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jxa.fb;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import jxa.BrowserAction;
import jxa.IMCBrowser;
import static jxa.IMCBrowser.applet;
import jxa.LocalizedResources;
import jxa.fb.FBConnector.Album;
import jxa.fb.FBConnector.Facebook;
import jxa.fb.FBConnector.Photo;
import net.sf.jiga.xtended.impl.Sprite;
import net.sf.jiga.xtended.kernel.FileHelper;
import net.sf.jiga.xtended.kernel.ThreadWorks;
import net.sf.jiga.xtended.ui.ImageCollection;
import net.sf.jiga.xtended.ui.UIMessage;

/**
 *
* @author www.b23prodtm.info
*/
public class FBimportAction extends BrowserAction {


        public FBimportAction(IMCBrowser instance) {
                super(instance.getLocalizedStringCap("importfromFacebook..."), instance.facebookIcon, instance);
        }

        public void actionPerformed(final ActionEvent e) {
                Runnable r = new Runnable() {
                        public void run() {
                                if (!instance.waitForAllPictures(instance.imageCollection)) {
                                        return;
                                }
                                String op = "Facebook";
                                boolean interrupt = false;
                                instance.currentProcessThumb.setIcon(instance.facebookIcon_64);
                                long wait = UIMessage.newProgress(0, 100, instance.picView);
                                JDialog pp = instance.showLightPopupActivity(op, instance.currentProcessThumb, null, UIMessage.UI_TOP_RIGHT);
                                int amountOfPictures = 0;
                                UIMessage.hideProgress(wait);
                                int fstate = applet ? 0 : ((JFrame) instance.mainFrame).getExtendedState();
                                try {
                                        Facebook client = instance.FBConnect().getFacebook();
                                        if (client == null) {
                                                return;
                                        }
                                        ArrayList<Album> albumsNames = new ArrayList<Album>();
                                        for (Album alb : client.albums().values()) {
                                                albumsNames.add(alb);
                                        }
                                        if (albumsNames.isEmpty()) {
                                                UIMessage.showLightPopupMessage(new JLabel(IMCBrowser.getLocalizedStringCap("label17")), null, instance.picView, UIMessage.UI_BOTTOM_RIGHT);
                                                return;
                                        }
                                        String[] lstr = LocalizedResources.localizedGetStringArray("dialog6", 2);
                                        Album album = (Album) UIMessage.showSelectDialog(instance.picView, lstr[0], lstr[1], albumsNames.toArray(new Album[]{}), albumsNames.get(0));
                                        if (album instanceof Album) {
                                                instance.fcp.setModeSave(true);
                                                instance.fcp.initToPath(instance.defaultImageDirectory.getPath());
                                                String dTitle = instance.fcp.getFiles().getDialogTitle();
                                                instance.fcp.getFiles().setDialogTitle(instance.getLocalizedStringCap("Selectadirectorytosavethealbumto!"));
                                                File[] d = instance.fcp.getDirectory();
                                                instance.fcp.getFiles().setDialogTitle(dTitle);
                                                if (d.length > 0) {
                                                        instance.setImageCollection(instance.newImageCollection(album.getProps().getProperty("Title")));
                                                        Runnable r = new Runnable() {
                                                                public void run() {
                                                                        instance.updateCollectionDB();
                                                                }
                                                        };
                                                        ThreadWorks.Swing.invokeAndWait(r);
                                                        File albumDir = new File(d[0].getPath() + File.separator + album);
                                                        albumDir.mkdirs();
                                                        FileHelper._makeWritable(albumDir);

                                                        instance.edit_started(wait, op, amountOfPictures = album.getCount());
                                                        for (Photo photo : client.getAlbumPhotos(album.id).values()) {
                                                                URL src = photo.getPicture();
                                                                String filePath = albumDir.getPath() + File.separator + src.getFile();
                                                                String mime = ImageCollection._getImageMimeType(new File(filePath), "w");
                                                                BufferedImage bim = ImageIO.read(src);
                                                                instance.edit_buffer(bim);
                                                                Sprite sp = new Sprite(bim, mime, new Dimension(bim.getWidth(), bim.getHeight()));
                                                                /*InputStream is = new URL(src).openStream();
                                                                 Sprite sp = Sprite._load(ImageIO.createImageInputStream(is));
                                                                 sp.setTileModeEnabled(true);*/
                                                                sp.setBufferedType(ImageCollection._getBufferedType(mime));
                                                                /*sp.setStoreMode(Sprite.MODE_JAVA2D | Sprite.MODE_TILE);*/
                                                                sp.runValidate();
                                                                File dstFile = new File(filePath);
                                                                sp.writeImageToOutput(dstFile, -1f);
                                                                sp.clearResource();
                                                                /*is.close();*/
                                                                instance.open(dstFile, filePath, true);
                                                                instance.doBrowserUpdate();
                                                                instance.edit_progress(wait, 1, amountOfPictures);
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
                                        instance.edit_finished(wait, op, interrupt, amountOfPictures);
                                        pp.setVisible(false);
                                        UIMessage.kill(wait);
                                }
                        }
                };
                instance.canvas.doTaskOnScreen(r);
        }
}
