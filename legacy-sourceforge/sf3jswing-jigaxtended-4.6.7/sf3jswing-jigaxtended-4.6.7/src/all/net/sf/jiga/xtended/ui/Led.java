/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.event.IIOReadProgressListener;
import javax.imageio.event.IIOWriteProgressListener;
import javax.swing.JComponent;
import net.sf.jiga.xtended.kernel.SpritesCacheAdapter;
import net.sf.jiga.xtended.kernel.SpritesCacheListener;
import net.sf.jiga.xtended.impl.IIOReadProgressAdapter;
import net.sf.jiga.xtended.impl.IIOWriteProgressAdapter;
import net.sf.jiga.xtended.impl.Sprite;
import net.sf.jiga.xtended.impl.SpriteIO;
import net.sf.jiga.xtended.impl.game.gl.GLFX;

/**
 *
 * @author www.b23prodtm.info
 */
public class Led extends JComponent {

        public final static int READ = 0;
        public final static int WRITE = 1;
        public final static int ON_STATE = 1;
        public final static int OFF_STATE = 0;
        public final static int ERROR_STATE = 2;
        public int state = 0;
        public int type;

        public Led(int type) {
                super();
                this.type = type;
                setLastErrorPopupEnabled(true);
        }

        public Led() {
                this(READ);
        }

        public int style = PaintStyle.STYLE_FLAT;

        @Override
        public boolean isOpaque() {
                return false;
        }
        private boolean lastErrorPopupEnabled = true;

        public void setLastErrorPopupEnabled(boolean b) {
                if (lastErrorPopupEnabled = b) {
                        addMouseListener(ma);
                        setToolTipText("Click for last error pop-up !");
                } else {
                        removeMouseListener(ma);
                        setToolTipText(null);
                }
        }

        public boolean isLastErrorPopupEnabled() {
                return lastErrorPopupEnabled;
        }
        Sprite mask = null;
        private MouseListener ma = new MouseAdapter() {

                long hash = System.nanoTime();

                @Override
                public boolean equals(Object o) {
                        return o == null ? false : o.hashCode() == hashCode();
                }

                @Override
                public int hashCode() {
                        return (int) hash;
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                        if (Led.this.type == READ ? lastSPMReadError != null : lastSPMWriteError != null) {
                                Exception error = Led.this.type == READ ? lastSPMReadError : lastSPMWriteError;
                                String msg = "Last " + (Led.this.type == READ ? "read" : "write") + " Error : " + error.getMessage() + "\n";
                                for (StackTraceElement ste : error.getStackTrace()) {
                                        msg += ste.toString() + "\n";
                                }
                                new UIMessage(false, msg, null, UIMessage.ERROR_TYPE);
                        } else {
                                new UIMessage(false, "No error has occured yet !", null);
                        }
                        clearError();
                        repaint();
                }
        };

        @Override
        protected void paintComponent(Graphics pg) {
                Graphics2D g = Sprite.wrapRendering(pg);
                Rectangle clip = g.getClipBounds();
                Composite cps = g.getComposite();
                Color c = g.getColor();
                Rectangle compBounds = getBounds();
                Rectangle bounds = (Rectangle) compBounds.clone();
                if (mask == null) {
                        mask = /*!Sprite._isTrueVolatile() ? */ new Sprite(SpriteIO.createBufferedImage(getSize(), Sprite.DEFAULT_TYPE), "image/x-png", getSize())/* : new Sprite(Sprite.createVolatileImage(getSize()), "image/x-png", getSize())*/;
                }
                bounds.grow(6 - (int) ((float) bounds.width / 2f), 6 - (int) ((float) bounds.height / 2f));
                g.translate(-compBounds.x, -compBounds.y);
                g.clip(compBounds);
                Color ledColor = Color.WHITE;
                if ((state & ON_STATE) != 0) {
                        g.setColor(ledColor = type == READ ? Color.GREEN.brighter() : Color.RED.brighter());
                        g.fillOval(bounds.x, bounds.y, bounds.width, bounds.height);
                }
                if ((state & ERROR_STATE) != 0) {
                        g.setColor(ledColor = Color.ORANGE.brighter());
                        g.fillOval(bounds.x, bounds.y, bounds.width, bounds.height);
                }
                if ((state & (ERROR_STATE | ON_STATE)) == 0) {
                        g.setColor(Color.BLACK);
                        g.fillOval(bounds.x, bounds.y, bounds.width, bounds.height);
                }
                mask.setBounds(bounds);
                mask.clearImageGraphics();
                Graphics2D gm = Sprite.wrapRendering(Sprite._createImageGraphics(mask.getImage(this)));
                gm.clip(new Ellipse2D.Float(-.5f, -.5f, bounds.width + 1, bounds.height + 1));
                gm.setColor(ledColor);
                gm.fill(gm.getClip());
                Composite cpsm = gm.getComposite();
                gm.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OUT, .3f));
                gm.fillOval(-(int) ((float) bounds.width / 4f), (int) ((float) bounds.height / 4f), bounds.width, bounds.height);
                gm.setComposite(cpsm);
                gm.setColor(Color.BLACK);
                Insets in = getInsets();
                if (style == PaintStyle.STYLE_GLOW) {
                        gm.drawOval(in.left, in.top, bounds.width - in.right, bounds.height - in.bottom);
                }
                gm.dispose();
                mask.runValidate();
                if (style == PaintStyle.STYLE_GLOW) {
                        mask.draw(this, g, GLFX.gfx.FX_DOWNREFLEXION_EXT, new Point(0, 0), ledColor);
                }
                if (style == PaintStyle.STYLE_FLAT) {
                        mask.draw(this, g, GLFX.gfx.FX_SHADOW.bit(), new Point(3, 3), ledColor.darker());
                }
                g.setComposite(cps);
                g.setColor(c);
                g.translate(compBounds.x, compBounds.y);
                g.setClip(clip);
        }

        public IIOReadProgressListener getIIOReadProgressAdapter() {
                return iior;
        }

        public IIOWriteProgressListener getIIOWriteProgressAdapter() {
                return iiow;
        }

        public SpritesCacheListener getSpritesCacheAdapter() {
                return scl;
        }
        private IIOReadProgressListener iior = new IIOReadProgressAdapter(false) {

                @Override
                public void imageStarted(ImageReader arg0, int arg1) {
                        if (type == READ) {
                                setOn();
                                repaint();
                        }
                }

                @Override
                public void imageComplete(ImageReader arg0) {
                        if (type == READ) {
                                setOff();
                                repaint();
                        }
                }
        };
        private IIOWriteProgressListener iiow = new IIOWriteProgressAdapter(false) {

                @Override
                public void imageStarted(ImageWriter arg0, int arg1) {
                        if (type == WRITE) {
                                setOn();
                                repaint();
                        }
                }

                @Override
                public void imageComplete(ImageWriter arg0) {
                        if (type == WRITE) {
                                setOff();
                                repaint();
                        }
                }
        };

        public void setOn() {
                state = ~Led.OFF_STATE & state | Led.ON_STATE;
        }

        public void setOff() {
                state = ~Led.ON_STATE & state | Led.OFF_STATE;
        }

        public void setError() {
                state |= Led.ERROR_STATE;
        }

        public void clearError() {
                state &= ~Led.ERROR_STATE;
        }
        private Exception lastSPMReadError = null, lastSPMWriteError = null;

        public Exception getLastSPMReadError() {
                return lastSPMReadError;
        }

        public Exception getLastSPMWriteError() {
                return lastSPMWriteError;
        }
        private SpritesCacheListener scl = new SpritesCacheAdapter() {

                @Override
                public void readStarted() {
                        if (type == READ) {
                                super.readStarted();
                                setOn();
                                repaint();
                        }
                }

                @Override
                public void readError(Exception arg0) {
                        if (type == READ) {
                                super.readError(arg0);
                                lastSPMReadError = arg0;
                                setError();
                                repaint();
                        }
                }

                @Override
                public void readCompleted() {
                        if (type == READ) {
                                setOff();
                                repaint();
                        }
                }

                @Override
                public void writeStarted() {
                        if (type == WRITE) {
                                super.writeStarted();
                                setOn();
                                repaint();
                        }
                }

                @Override
                public void writeError(Exception arg0) {
                        if (type == WRITE) {
                                super.writeError(arg0);
                                setError();
                                lastSPMWriteError = arg0;
                                repaint();
                        }
                }

                @Override
                public void writeCompleted() {
                        if (type == WRITE) {
                                setOff();
                                repaint();
                        }
                }
        };
}
