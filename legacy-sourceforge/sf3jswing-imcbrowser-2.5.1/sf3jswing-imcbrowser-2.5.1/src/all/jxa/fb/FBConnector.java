/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jxa.fb;

import com.restfb.json.JsonObject;
import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import jxa.AlbumWeb;
import jxa.LocalizedResources;

/**
 *
 * @author Tiana
 */
public class FBConnector {

    private Facebook facebook = new Facebook();

    /**
     *
     */
    public static class Facebook {

        Map<String, Album> albums() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        String getUsername() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        void addAlbumPhoto(String id, File file) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        Map<String, Photo> getAlbumPhotos(String id) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }

    public Facebook getFacebook() {
        return facebook;
    }

    public static class Album extends AlbumWeb {

        public Album(String id, String title) {
            super(id, title);
        }

        public Album(String id, Properties props) {
            super(id, props);
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return getProps().getProperty("title", "");
        }

        int getCount() {
            return 0;
        }
    }

    public static abstract class Photo {

        URL getPicture() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    public FBConnector() {
        /**
         * WebView to login
         */
    }
    public final String login = LocalizedResources.localized.getString("FBlogin");
    Properties properties = new Properties();

    /**
     *
     * @param data
     */
    public void download(JsonObject... data) {
        /**
         * login
         */
    }
    
    public void upload(Photo... photos){
    }
}
