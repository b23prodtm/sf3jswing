/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.ui;

import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import net.sf.jiga.xtended.kernel.JXAenvUtils;

/**
 *
 * @author www.b23prodtm.info
 */
public class SystemTrayIcon {

    Object icon = null;

    public SystemTrayIcon(URL icon, String title) {
        if (isJavaSystemTrayJDICsupported()) {
            org.jdesktop.jdic.tray.TrayIcon trayIcon = new org.jdesktop.jdic.tray.TrayIcon(new ImageIcon(icon));
            org.jdesktop.jdic.tray.SystemTray.getDefaultSystemTray().addTrayIcon(trayIcon);
            this.icon = trayIcon;
        } else if (isJavaSystemTray6Supported()) {
            TrayIcon trayIcon;
            try {
                trayIcon = new TrayIcon(ImageIO.read(icon));
                SystemTray.getSystemTray().add(trayIcon);
                this.icon = trayIcon;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            System.err.println(JXAenvUtils.log("No SystemTray could be instantiated", JXAenvUtils.LVL.APP_WRN));
        }
        setPopupMenu(title);
    }

    public static boolean isJavaSystemTrayJDICsupported() {
        return JXAenvUtils.env.JAVA_VERSION_5.isEnv() && (JXAenvUtils.env.ARCH_PPC.isEnv() || JXAenvUtils.env.ARCH_X86.isEnv());
    }

    public static boolean isJavaSystemTray6Supported() {
        return JXAenvUtils.env.JAVA_VERSION_5.isEnv() ? false : SystemTray.isSupported();
    }
    public static final int ERROR = 0, INFO = 1, NONE = 2, WARNING = 3;
    JPopupMenu menuJDIC;

    private void setPopupMenu(String title) {
        if (isJavaSystemTrayJDICsupported()) {
            ((org.jdesktop.jdic.tray.TrayIcon) icon).setPopupMenu(menuJDIC = new JPopupMenu(title));
        } else if (isJavaSystemTray6Supported()) {
            ((TrayIcon) icon).setPopupMenu(menu6 = new PopupMenu(title));
        }
    }

    private void addSubMenuJDIC(JPopupMenu menu) {
        if (isJavaSystemTrayJDICsupported()) {
            menuJDIC.add(menu);
        }
    }
    PopupMenu menu6;

    private void addSubMenu6(PopupMenu menu) {
        if (isJavaSystemTray6Supported()) {
            menu6.add(menu);
        }
    }

    private void addMenuItem(Object m, final Action a) {
        if (isJavaSystemTrayJDICsupported()) {
            ((JPopupMenu) m).add(a);
        } else if (isJavaSystemTray6Supported()) {
            final MenuItem item = new MenuItem((String) a.getValue(Action.NAME));
            item.addActionListener(a);
            a.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    item.setLabel((String) a.getValue(Action.NAME));
                }
            });
            ((PopupMenu) m).add(item);
        }
    }

    private int getUIMessageType(int type) {
        switch (type) {
            case ERROR:
                return UIMessage.ERROR_TYPE;
            case INFO:
                return UIMessage.INFO_TYPE;
            case NONE:
                return UIMessage.INFO_TYPE;
            case WARNING:
                return UIMessage.ABORT_TYPE;
            default:
                return UIMessage.INFO_TYPE;
        }
    }

    private int getMessageTypeJDIC(int type) {
        if (isJavaSystemTrayJDICsupported()) {
            switch (type) {
                case ERROR:
                    return org.jdesktop.jdic.tray.TrayIcon.ERROR_MESSAGE_TYPE;
                case INFO:
                    return org.jdesktop.jdic.tray.TrayIcon.INFO_MESSAGE_TYPE;
                case NONE:
                    return org.jdesktop.jdic.tray.TrayIcon.NONE_MESSAGE_TYPE;
                case WARNING:
                    return org.jdesktop.jdic.tray.TrayIcon.WARNING_MESSAGE_TYPE;
                default:
                    return org.jdesktop.jdic.tray.TrayIcon.NONE_MESSAGE_TYPE;
            }
        }
        return 0;
    }

    private TrayIcon.MessageType getMessageType6(int type) {
        if (isJavaSystemTray6Supported()) {
            switch (type) {
                case ERROR:
                    return TrayIcon.MessageType.ERROR;
                case INFO:
                    return TrayIcon.MessageType.INFO;
                case NONE:
                    return TrayIcon.MessageType.NONE;
                case WARNING:
                    return TrayIcon.MessageType.WARNING;
                default:
                    return TrayIcon.MessageType.NONE;
            }
        }
        return null;
    }

    public void pack() {
        if (isJavaSystemTrayJDICsupported()) {
            menuJDIC.pack();
        }
    }

    /**
     @param a action that is linked to this item, any change made further to the action.NAME is going to reflect in the menu. (PropertyChangeListener)
     */
    public void addMenuItem(Action a) {
        if (isJavaSystemTrayJDICsupported()) {
            addMenuItem(menuJDIC, a);
        } else if (isJavaSystemTray6Supported()) {
            addMenuItem(menu6, a);
        }
    }

    /**
     @param a action that is linked to this item, any change made further to the action.NAME is going to reflect in the menu. (PropertyChangeListener)
     */
    public void addMenu(String title, Action... a) {
        if (isJavaSystemTray6Supported()) {
            PopupMenu m = new PopupMenu(title);
            for (Action item : a) {
                addMenuItem(m, item);
            }
            addSubMenu6(m);
        } else if (isJavaSystemTrayJDICsupported()) {
            JPopupMenu m = new JPopupMenu(title);
            for (Action item : a) {
                addMenuItem(m, item);
            }
            addSubMenuJDIC(m);
        }
    }

    public void addMenuSeparator() {
        if (isJavaSystemTrayJDICsupported()) {
            menuJDIC.addSeparator();
        } else if (isJavaSystemTray6Supported()) {
            menu6.addSeparator();
        }
    }

    public void displayMessage(String title, String message, int type) {
        if (isJavaSystemTrayJDICsupported()) {
            ((org.jdesktop.jdic.tray.TrayIcon) icon).displayMessage(title, message, getMessageTypeJDIC(type));
        } else if (isJavaSystemTray6Supported()) {
            ((TrayIcon) icon).displayMessage(title, message, getMessageType6(type));
        } else {
            UIMessage.showLightPopupMessage(new JLabel(message), null, null, getUIMessageType(type));
        }
    }
}
