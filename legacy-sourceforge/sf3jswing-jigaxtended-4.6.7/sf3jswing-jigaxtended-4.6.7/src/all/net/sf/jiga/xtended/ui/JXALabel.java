/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.ui;

import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;

/**
 * JLabel that displays text in HTML-<A>-tag-like behaviour. add a MousListener
 * to capture clicks !
 *
 * @author www.b23prodtm.info
 */
public class JXALabel extends JLabel {

    private String highlightedText;

    /**
     * @param text unlike JLabel, no <HTML> tag as text
     */
    public JXALabel(String text) {
        super();
        setText(text);
        addMouseListener(new MouseAdapter() {

            boolean clicked = false;

            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                style = style | UNDERLINE;
                setText(highlightedText);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                style = style - (UNDERLINE & style) | (clicked ? ITALIC : 0);
                setText(highlightedText);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                clicked = true;
            }
        });
    }
    public final static int UNDERLINE = 1;
    public final static int BOLD = 2;
    public final static int ITALIC = 4;
    private int style = 0;
    int maxWidth = 0, ending = 0;

    public void setMaxWidth(int maxPixWidth, int ending) {
        assert maxPixWidth >= 0 && ending >= 0 && ending < maxPixWidth - 2 : "invalid maxPixWidth (" + maxPixWidth + ")and/or ending (" + ending + "); must be >= 0 and ending < maxPixWidth - 2";
        this.maxWidth = maxPixWidth;
        this.ending = ending;
    }

    public void setStyle(int style) {
        this.style = style;
    }

    public int getStyle() {
        return style;
    }

    @Override
    protected void paintComponent(Graphics g) {
        setForeground(getParent().getForeground());
        if (maxWidth > 0) {
            /* a copy of the text, avoids recursive loop*/
            String trunk = "" + highlightedText;
            while (g.getFontMetrics(getFont()).stringWidth(trunk) > maxWidth) {
                trunk = trunk.substring(0, trunk.length() - 1);
            }
            if (!trunk.equals(highlightedText)) {
                trunk = trunk.substring(0, trunk.length() - 2) + ".." + highlightedText.substring(highlightedText.length() - ending);
            }
            setText(trunk);
        }
        super.paintComponent(g);
        if (maxWidth <= 0) {
            setText(highlightedText);
        }
    }

    @Override
    public void setText(String text) {
        highlightedText = text;
        /* System.out.println("JIMCLabel style : " + style);*/
        if ((style & UNDERLINE) != 0) {
            text = "<u>" + text + "</u>";
        }
        if ((style & BOLD)
                != 0) {
            text = "<b>" + text + "</b>";
        }
        if ((style & ITALIC)
                != 0) {
            text = "<i>" + text + "</i>";
        }
        super.setText("<html>" + text + "</html>");
    }
}
