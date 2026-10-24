/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.ui;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 *
 * @author www.b23prodtm.info
 */
public class ImagePluginFilter {

    public String mime;
    public Properties props;
    public String[] exts;

    public ImagePluginFilter(Properties props) {
        this.mime = props.getProperty("plugin mime");
        this.props = props;
        String extension = props.getProperty("plugin extension");
        this.exts = extension.split(" ");
    }

    @Override
    public String toString() {
        return props.getProperty("plugin description") + " (" + props.getProperty("plugin extension") + ")";
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ImagePluginFilter ? ((ImagePluginFilter) o).mime.equals(mime) && props.equals(((ImagePluginFilter) o).props) : false;
    }

    @Override
    public int hashCode() {
        return (mime + props.hashCode()).hashCode();
    }

    /**
     * @param fileMode "rw", "r" or "w" for read / write capabilities
     */
    public static Set<ImagePluginFilter> _getFileFilters(String fileMode) {
        HashSet<ImagePluginFilter> ffSet = new HashSet();
        Set<ImagePluginDescriptor> plugins = ImageCollection._getPluginsMap();
        synchronized (plugins) {
            for (ImagePluginDescriptor plug : plugins) {
                System.out.println("plugin " + plug.p.getProperty("plugin mime") + " " + plug);
                if (plug.p.getProperty("plugin support").contains(fileMode)) {
                    ffSet.add(new ImagePluginFilter(plug.p));
                }
            }
        }
        return ffSet;
    }

    /**
     * reset file filters in the file chooser to handle all supported image
     * files, with the file mode set
     *
     * @param fileMode "rw", "r" or "w" for read / write capabilities
     */
    public static Set<ImagePluginFilter> _updateFileFilters(FileChooserPane fcp, String fileMode) {
        fcp.clearAllFileFilters();
        Set<ImagePluginFilter> ffSet = _getFileFilters(fileMode);
        HashSet<String> extensionsSet = new HashSet();
        for (ImagePluginFilter c : ffSet) {
            ExampleFileFilter eff = new ExampleFileFilter(c.exts, c.props.getProperty("plugin description"));
            fcp.getFiles().addChoosableFileFilter(eff);
            for (String e : c.exts) {
                extensionsSet.add(e);
            }
        }
        fcp.getFiles().setAcceptAllFileFilterUsed(false);
        fcp.getFiles().setFileFilter(fcp.addFileFilter(extensionsSet));
        return ffSet;
    }
}