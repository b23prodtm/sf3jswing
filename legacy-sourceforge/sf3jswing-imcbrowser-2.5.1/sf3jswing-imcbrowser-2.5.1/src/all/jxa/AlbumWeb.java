/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jxa;

import java.util.Properties;

/**
 *
 * @author tiana
 */
public class AlbumWeb {
        private Properties props;
        public String id;

        public Properties getProps() {
                return props;
        }

        public AlbumWeb(String id, String title) {
                props = new Properties();
                props.setProperty("Title", title);
                this.id = id;
        }

        public AlbumWeb(String id, Properties props) {
                assert props.containsKey("Title") : "you must specify a property named \"Title\".";
                this.id = id;
                this.props.putAll(props);
        }

        @Override
        public boolean equals(Object obj) {
                return obj instanceof AlbumWeb ? ((AlbumWeb) obj).id.equals(id) : false;
        }
        long hash = System.nanoTime();

        @Override
        public int hashCode() {
                return (int) hash;
        }

        @Override
        public String toString() {
                return props.getProperty("Title") + " {" + id + "}";
        }
        
}
