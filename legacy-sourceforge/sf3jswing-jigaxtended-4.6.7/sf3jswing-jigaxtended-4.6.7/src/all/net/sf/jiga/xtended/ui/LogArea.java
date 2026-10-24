package net.sf.jiga.xtended.ui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import javax.swing.*;
import net.sf.jiga.xtended.impl.Sprite;
import net.sf.jiga.xtended.kernel.*;

/** This class extends JScrollPane to serve logging purposals in one Swing application.
 */
public class LogArea extends JScrollPane implements InputLogListener, Menu {

    /** the Console instance */
    private Console console = new Console();
    /** the JTextArea instance */
    final JTextArea text;
    /** the ErrorListener's set */
    Set<ErrorListener> output = Collections.synchronizedSet(new HashSet<ErrorListener>());
    /** default text in the log */
    String defaultText;
    /** the "print error" state */
    private final static int PRINT_ERROR = 0;
    /** the switch to dis/enable reading System std output
     */
    private int fromStdout = 0;

    /** creates a new instance 
    @see #LogArea(String)*/
    public LogArea() {
        this(Console.class.getPackage().getSpecificationTitle() + " : " + Console.class.getPackage().getSpecificationVersion() + Console.newLine);
    }

    /** creates a new instance
     * @param defaultText text that is saved as the default text 
    @see JScrollPane#JScrollPane()*/
    public LogArea(String defaultText) {
        super();
        setAutoscrolls(true);
        setDoubleBuffered(true);
        setVisible(true);
        this.defaultText = defaultText;
        text = new JTextArea();
        text.setDragEnabled(true);
        text.setDoubleBuffered(true);
        text.setLineWrap(true);
        text.setText(defaultText);
        setViewportView(text);
        console.addInputLogListener(this);
        console.setStdoutEnabled(true);
        /*JPopupMenu popup = new JPopupMenu();
        popup.add(pMenu);
        add(popup);*/
        setWheelScrollingEnabled(true);
        validate();
        repaint();
    }

    /** logs a new message 
    @param message the message to log
    @see #pushNewLog(String)*/
    @Override
    public void newLogPacket(String message) {
        pushNewLog(message);
    }
    /***/
    private final static int LOGSTDOUT = 1;
    private final static int LOGSTDERR = 2;

    /** dis/enables reading the System std output
    @param b dis/enables reading the System std output System.out
    @see Console#setLogStdoutEnabled(boolean)
    @throws IOException thrown by the Console instance*/
    public void setLogStdoutEnabled(boolean b) {
        setLogStdoutEnabled(b, LOGSTDOUT);
    }

    /** dis/enables reading the System std output
    @param b dis/enables reading the System std output System.err
    @see Console#setLogStdoutEnabled(boolean)
    @throws IOException thrown by the Console instance*/
    public void setLogStderrEnabled(boolean b) {
        setLogStdoutEnabled(b, LOGSTDERR);
    }

    public boolean isLogStdoutEnabled() {
        return (fromStdout & LOGSTDOUT) != 0;
    }

    public boolean isLogStderrEnabled() {
        return (fromStdout & LOGSTDERR) != 0;
    }

    /** dis/enables reading the System std output
    @param b dis/enables reading the System std output
     * @param systemStream bitwise-OR combination of streams masks
     * @see #LOGSTDOUT 
     * @see #LOGSTDERR
    @see Console#setLogStdoutEnabled(boolean)
    @throws IOException thrown by the Console instance*/
    private void setLogStdoutEnabled(boolean b, int systemStream) {
        try {
            if ((systemStream & LOGSTDOUT) != 0) {
                console.setLogStdoutEnabled(b);
            }
            if ((systemStream & LOGSTDERR) != 0) {
                console.setLogStderrEnabled(b);
            }
            fromStdout = b ? fromStdout | systemStream : fromStdout - (fromStdout & systemStream);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public Console getConsole() {
        return console;
    }

    /** Returns the text in the current log area
     * @return text
    @see JTextArea#getText()*/
    private String getText() {
        return text.getText();
    }

    /** Clears the text but the stack memory keeps unchanged.
     * @see Console#clearContents()
     */
    public void clearLog() {
        text.setText(defaultText);
        console.clearContents();
    }

    /** appends a new Log line to this LogArea and repaints.
     *@param newLog log text
     *@return the logged text
     * @see JTextArea#append(String)
     */
    public String pushNewLog(final String newLog) {
        ThreadWorks.Swing.invokeLater(new Runnable() {

            @Override
            public void run() {
                text.append(newLog);
                LogArea.super.getVerticalScrollBar().setValue(LogArea.super.getVerticalScrollBar().getMaximum());
                LogArea.super.getHorizontalScrollBar().setValue(LogArea.super.getHorizontalScrollBar().getMaximum());
                repaint();
            }
        });
        return newLog;
    }

    /** stores log to a file on disk 
    @param filename the file name where to save the log contents*/
    public void saveToDisk(String filename) throws FileNotFoundException, IOException {
        RandomAccessFile raf = new RandomAccessFile(filename, "rw");
        raf.setLength(0);
        raf.write(this.text.getText().getBytes());
        raf.close();
    }

    /** returns the current associated JMenu for this LogArea instance
    @return the current associated JMenu for this LogArea instance*/
    @Override
    public JMenu getActionMenu() {
        final JMenu pMenu;

        pMenu = new JMenu("log");
        pMenu.add(new AbstractAction("log stdout -/o") {

                    @Override
            public void actionPerformed(ActionEvent e) {
                setLogStdoutEnabled(!isLogStdoutEnabled());
                this.putValue(Action.NAME, "log stdout " + (isLogStdoutEnabled() ? "off" : "on"));
                Sprite._quickPaintImmediately(pMenu);
            }
        });
        pMenu.add(new AbstractAction("log stderr -/o") {

                    @Override
            public void actionPerformed(ActionEvent e) {
                setLogStderrEnabled(!isLogStderrEnabled());
                this.putValue(Action.NAME, "log stderr " + (isLogStderrEnabled() ? "off" : "on"));
                Sprite._quickPaintImmediately(pMenu);
            }
        });
        pMenu.add(new AbstractAction("save to file...") {

                    @Override
            public void actionPerformed(ActionEvent e) {
                FileChooserPane fcp = new FileChooserPane(null, ".");
                fcp.setMultiSelectionEnabled(false);
                File[] f = fcp.getFile();
                try {
                    if (f != null) {
                        saveToDisk(f[0].getPath());
                    }
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }).setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_S, ActionEvent.ALT_MASK));
        pMenu.add(new AbstractAction("clear") {

                    @Override
            public void actionPerformed(ActionEvent e) {
                clearLog();
            }
        });
        return pMenu;
    }

    @Override
    public JMenu getHelpActionMenu() {
        JMenu menu = new JMenu("Help");
        menu.add(new AbstractAction("Print the system properties") {

                    @Override
            public void actionPerformed(ActionEvent e) {
                Enumeration enumeration = System.getProperties().keys();
                for (; enumeration.hasMoreElements();) {
                    String property = (String) enumeration.nextElement();
                    pushNewLog(Console.newLine + ":: " + property + "=" + System.getProperty(property) + " ::");
                }
            }
        });
        return menu;
    }
}
