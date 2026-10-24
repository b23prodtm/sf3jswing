/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.ui;

import java.util.Properties;

/**
 * It defines JXA supported ImageReader/Writers. Use {@link #get} to retrieve/write properties {@linkplain #p} !
 * @author www.b23prodtm.info
 */
public class ImagePluginDescriptor {

    public static enum get {

        id, extension, mime, support, description, buffered_type, compression_type, class_r, class_w;

        @Override
        public String toString() {
            return "plugin " + name().replace('_', '-');
        }
    }

    @Override
    public int hashCode() {
        return p.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ImagePluginDescriptor ? p.equals(((ImagePluginDescriptor) obj).p) : false;
    }
    public final Properties p;

    protected ImagePluginDescriptor(Properties p) {
        this.p = p;
    }
}
