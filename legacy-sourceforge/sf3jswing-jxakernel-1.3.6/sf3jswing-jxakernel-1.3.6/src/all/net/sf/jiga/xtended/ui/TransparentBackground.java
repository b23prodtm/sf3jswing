/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.swing.JPanel;
import net.sf.jiga.xtended.kernel.JXAenvUtils;

/**
 *
 * @author www.b23prodtm.info
 */
public class TransparentBackground extends JPanel {
    private BufferedImage background;
    private boolean disableTransparency = false;

    public TransparentBackground(LayoutManager layout, boolean doubleBufferedPanel) {
        super(layout, doubleBufferedPanel);
    }

    public void setDisableTransparency(boolean disableTransparency) {
        this.disableTransparency = disableTransparency;
    }

    AccessControlContext acc = AccessController.getContext();
    /**
     * blur effect by "jamescook2",
     * http://www.oreillynet.com//cs/user/view/cs_msg/75941
     */
    public void updateBackground() {
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                _updateBackground();
                return null;
            }
        }, acc);
    }
    float[] my_kernel = {0.00f, 0.00f, 0.05f, 0.00f, 0.00f, 0.00f, 0.05f, 0.05f, 0.05f, 0.00f, 0.05f, 0.05f, 0.10f, 0.05f, 0.05f, 0.00f, 0.05f, 0.05f, 0.05f, 0.00f, 0.00f, 0.00f, 0.05f, 0.00f, 0.00f};
    ConvolveOp _blurOp = new ConvolveOp(new Kernel(5, 5, my_kernel));

    private void _updateBackground() {
        Dimension dim = new Dimension(JXAenvUtils._defaultGC.getDevice().getDisplayMode().getWidth(), JXAenvUtils._defaultGC.getDevice().getDisplayMode().getHeight());
        if (background instanceof BufferedImage) {
            background.flush();
        }
        if (JXAenvUtils.env.OS_LINUX.isEnv()) {
            background = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_4BYTE_ABGR_PRE);
            Graphics g = background.getGraphics();
            g.setColor(getBackground());
            g.fillRect(0, 0, dim.width, dim.height);
            g.dispose();
        } else {
            try {
                Robot rbt = new Robot();
                BufferedImage b = rbt.createScreenCapture(new Rectangle(0, 0, (int) dim.getWidth(), (int) dim.getHeight()));
                background = _blurOp.filter(b, null);
                b.flush();
            } catch (Exception ex) {
                if (JXAenvUtils._debugSys) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void paintComponent(Graphics g) {
        if (disableTransparency) {
            super.paintComponent(g);
        } else if (isShowing()) {
            Point pos = this.getLocationOnScreen();
            Point offset = new Point(-pos.x, -pos.y);
            g.drawImage(background, offset.x, offset.y, null);
        }
    }
    
}
