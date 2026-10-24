/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.ui;

import java.awt.GraphicsEnvironment;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.JLabel;
import net.sf.jiga.xtended.kernel.JXAenvUtils;

/**
 *
 * @author www.b23prodtm.info
 */
public class SystemTrayIcon {

    private TrayIcon trayIcon;
    private PopupMenu menu;

    public SystemTrayIcon(URL icon, String title) {
        if (isJavaSystemTray6Supported()) {
            try {
                trayIcon = new TrayIcon(ImageIO.read(icon));
                trayIcon.setPopupMenu(menu = new PopupMenu(title));
                SystemTray.getSystemTray().add(trayIcon);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (trayIcon == null) {
            System.err.println(JXAenvUtils.log("No SystemTray could be instantiated", JXAenvUtils.LVL.APP_WRN));
        }
    }

    public static boolean isJavaSystemTrayJDICsupported() {
        return false;
    }

    public static boolean isJavaSystemTray6Supported() {
        return !GraphicsEnvironment.isHeadless() && SystemTray.isSupported();
    }

    public static final int ERROR = 0, INFO = 1, NONE = 2, WARNING = 3;

    private int getUIMessageType(int type) {
        switch (type) {
            case ERROR:
                return UIMessage.ERROR_TYPE;
            case INFO:
            case NONE:
                return UIMessage.INFO_TYPE;
            case WARNING:
                return UIMessage.ABORT_TYPE;
            default:
                return UIMessage.INFO_TYPE;
        }
    }

    private TrayIcon.MessageType getMessageType6(int type) {
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

    public void pack() {
    }

    /**
     * @param a action that is linked to this item, any change made further to the action.NAME is going to reflect in the menu. (PropertyChangeListener)
     */
    public void addMenuItem(Action a) {
        if (menu != null) {
            addMenuItem(menu, a);
        }
    }

    /**
     * @param a action that is linked to this item, any change made further to the action.NAME is going to reflect in the menu. (PropertyChangeListener)
     */
    public void addMenu(String title, Action... a) {
        if (menu == null) {
            return;
        }
        PopupMenu submenu = new PopupMenu(title);
        for (Action item : a) {
            addMenuItem(submenu, item);
        }
        menu.add(submenu);
    }

    private void addMenuItem(PopupMenu menu, final Action action) {
        final MenuItem item = new MenuItem((String) action.getValue(Action.NAME));
        item.addActionListener(action);
        action.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                item.setLabel((String) action.getValue(Action.NAME));
            }
        });
        menu.add(item);
    }

    public void addMenuSeparator() {
        if (menu != null) {
            menu.addSeparator();
        }
    }

    public void displayMessage(String title, String message, int type) {
        if (trayIcon != null) {
            trayIcon.displayMessage(title, message, getMessageType6(type));
        } else {
            UIMessage.showLightPopupMessage(new JLabel(message), null, null, getUIMessageType(type));
        }
    }
}
