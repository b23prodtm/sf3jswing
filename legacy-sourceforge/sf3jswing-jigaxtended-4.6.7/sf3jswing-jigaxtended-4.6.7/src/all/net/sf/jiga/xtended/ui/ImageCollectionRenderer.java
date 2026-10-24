/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.jiga.xtended.ui;

import javax.swing.JComponent;

/**
 *
 * @author www.b23prodtm.info
 */
public interface ImageCollectionRenderer {
    
    /** please use {@link ImageCollection#getCollectionSprite(String, boolean) ImageCollection.getCollectionSprite()} to update the component graphics, 
     * otherwise the ImageCollection can behave strangely.
     * @param collection the ImageCollection instance that will use the component
     * @param key the key to retrieve the correct picture
     * @param thumbnail whether to get a thumbnail or the original picture, resp.
     * @return must return a new JComponent instance that can render the requested collection picture.
     * For example, non-scaled picture can be painted like this :
     <pre> protected void paintComponent(Graphics g) {
                collection.beginEdit();
                Sprite picture = collection.getCollectionSprite(key, false);
                Rectangle bounds = getBounds();
                g.translate(-bounds.x, -bounds.y);
     * // it may be added some more to center the picture
                try {
                    picture.draw(this, (Graphics2D) g, fx, fx_loc, fx_clr);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                } finally {
                    g.translate(bounds.x, bounds.y);
                   // picture.clearResource();
                   collection.endEdit();
                }
            }</pre>*/
    public JComponent makeCollectionRendererComponent(ImageCollection collection, String key, boolean thumbnail);
}
