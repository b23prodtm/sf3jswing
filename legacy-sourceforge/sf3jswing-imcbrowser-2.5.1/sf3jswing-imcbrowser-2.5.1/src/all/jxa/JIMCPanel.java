package jxa;

import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;
import net.sf.jiga.xtended.impl.Sprite;

class JIMCPanel extends JPanel {

    protected final String key;
    protected boolean saved = true;

    public JIMCPanel(String key) {
        super(true);
        this.key = key;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2 = Sprite.wrapRendering(g);
        if (!saved) {
            IMCBrowser.unSavedPic.setLocation(getWidth() - IMCBrowser.unSavedPic.getWidth() - 5, 5);
            IMCBrowser.unSavedPic.runValidate();
            IMCBrowser.unSavedPic.draw(this, g2);
        }
    }
}
