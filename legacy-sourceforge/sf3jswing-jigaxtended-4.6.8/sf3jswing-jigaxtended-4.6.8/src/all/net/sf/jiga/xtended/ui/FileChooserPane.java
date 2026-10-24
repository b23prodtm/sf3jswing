package net.sf.jiga.xtended.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import net.sf.jiga.xtended.impl.Sprite;
import net.sf.jiga.xtended.impl.system.JXAenvUtils2;
import net.sf.jiga.xtended.kernel.Console;
import net.sf.jiga.xtended.ui.UIMessage.SwingStaticReturn;

/**
 * A file chooser InstructionSet integrating one JFileChooser to serve any file
 * input from the user interface. It can be used in one InstructionPane instance
 * to get it working in a Steppable interface.
 *
 * @see InstructionsPane
 * @see StatusPane
 * @see JFileChooser
 */
public class FileChooserPane extends InstructionSet {

    /**
     * owner
     */
    protected Container owner;
    /**
     * File chooser
     */
    protected JFileChooser files;
    /**
     * File chooser open dialog button
     */
    protected JButton filesOpenButton;
    /**
     * file chooser printed result
     */
    protected LogArea filesResult;
    /**
     * selected files
     */
    private File[] selectedFiles;
    /**
     * JFileChooser status
     */
    protected int status;
    /**
     * open mode
     *
     * @see #show(int)
     */
    private final int OPEN = 0;
    /**
     * save mode
     *
     * @see #show(int)
     */
    private final int SAVE = 1;
    /**
     * default mode
     *
     * @see #show(int)
     */
    private final int DEFAULT = -1;
    /**
     * mode of the show method
     *
     * @default DEFAULT
     */
    private int mode = DEFAULT;
    /**
     * the preview JPanel instance
     *
     * @deprecated (Mac users already have it  in the native Java look and feel.)
     */
    private JPanel previewPanel = new JPanel(new BorderLayout(), true);
    /**
     * the current previewed file
     *
     * @deprecated (Mac users already have it in the native Java look and feel.)
     */
    private File currentPreviewedFile = null;
    private boolean imagePreview = false;
    public static ResourceBundle lang = ResourceBundle.getBundle("net.sf.jiga.xtended.ui.lang", Locale.getDefault());
    private ActionListener previewUpdater = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            if (!imagePreview && currentPreviewedFile != null) {
                setImagePreviewEnabled(imagePreview);
            } else if (imagePreview) {
                previewPanel.repaint();
                File f = files.getSelectedFile();
                if (currentPreviewedFile != null) {
                    if (currentPreviewedFile.equals(f)) {
                        return;
                    }
                }
                if (f == null) {
                    return;
                }
                if (f.isDirectory()) {
                    return;
                }
                currentPreviewedFile = f;
                try {
                    ImageInputStream in = ImageIO.createImageInputStream(new FileInputStream(
                            currentPreviewedFile));
                    Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
                    if (readers.hasNext()) {
                        ImageReader r = readers.next();
                        r.setInput(in);
                        ImageReadParam irp = r.getDefaultReadParam();
                        double scale = Math.min(128.0 / (double) r.getWidth(0), 128.0 / (double) r.getHeight(0));
                        Dimension renderSize = new Dimension((int) Math.round(r.getWidth(0) * scale), (int) Math.round(r.getHeight(0) * scale));
                        Image img = null;
                        if (r.hasThumbnails(0)) {
                            img = r.readThumbnail(0, 0);
                        } else if (irp.canSetSourceRenderSize()) {
                            irp.setSourceRenderSize(renderSize);
                            img = r.read(0, irp);
                        } else {
                            Sprite s = Sprite._load(currentPreviewedFile);
                            s.setTileModeEnabled(true);
                            s.setSize(renderSize);
                            img = s.getImage(previewPanel);
                            s.clearResource();
                            /*
                             * irp.setSourceRegion(new Rectangle((int)
                             * Math.max(1, (r.getWidth(0) - renderSize.width) /
                             * 2f), (int) Math.max(0, (r.getHeight(0) -
                             * renderSize.height) / 2f), renderSize.width,
                             * renderSize.height)); img = r.read(0, irp);
                             */
                        }
                        previewPanel.removeAll();
                        previewPanel.setLayout(new GridBagLayout());
                        if (img != null) {
                            GridBagConstraints c = new GridBagConstraints();
                            c.fill = c.NONE;
                            c.gridwidth = c.RELATIVE;
                            c.weightx = 1;
                            c.weighty = .1;
                            Display display = (Display) Display._Display(img,
                                    null);
                            previewPanel.add(new JLabel(r.getWidth(0) + "x" + r.getHeight(0)), c);
                            c.gridwidth = c.REMAINDER;
                            c.anchor = c.LINE_END;
                            UIMessage.JHoverIcon closeLab = UIMessage.newCloseSquareIcon();
                            previewPanel.add(closeLab, c);
                            closeLab.setOnclick(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    setImagePreviewEnabled(false);
                                }
                            });
                            display.setPreferredSize(renderSize);
                            c.gridwidth = 2;
                            c.weighty = 1;
                            previewPanel.add(display, c);
                        }
                        previewPanel.validate();
                        previewPanel.repaint();
                        r.dispose();
                    }
                    in.flush();
                    in.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    };

    /**
     * NOTE : the Quaqua Mac OS X JFilechooser already implements one.
     */
    public void setImagePreviewEnabled(final boolean b) {
        if (b) {
            files.setAccessory(previewPanel);
            if (previewTimer == null) {
                previewTimer = new javax.swing.Timer(10, previewUpdater);
                files.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        System.out.println("JFileChooser action performed.");
                        if (files.isShowing()) {
                            previewTimer.start();
                        } else {
                            previewTimer.stop();
                        }
                    }
                });

            } else {
                previewTimer.start();
            }
        }
        imagePreview = b;
        if (!imagePreview) {
            previewPanel.removeAll();
            previewPanel.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.gridwidth = c.REMAINDER;
            c.fill = c.HORIZONTAL;
            c.weightx = c.weighty = 1;
            previewPanel.add(new JButton(new AbstractAction(lang.getString("preview"), UIMessage._getIcon(UIMessage.ENABLED_TYPE, true)) {
                public void actionPerformed(ActionEvent e) {
                    setImagePreviewEnabled(true);
                }
            }), c);
            previewPanel.add(new JLabel(lang.getString("previewdisabled")), c);
            previewPanel.validate();
            previewPanel.repaint();
            currentPreviewedFile = null;
        }
    }

    /**
     * @return true if the custom
     * {@link JFileChooser#setAccessory(javax.swing.JComponent) preview}
     * accessory panel is enabled.
     * @deprecated as the native JFilechooser already implements one
     */
    public boolean isImagePreviewEnabled() {
        return imagePreview;
    }
    /**
     * the preview Timer instance
     *
     * @deprecated
     */
    javax.swing.Timer previewTimer;

    /**
     * Contructs a new InstructionSet with one JFileChooser at browsing desired
     * path or null.
     *
     * @param owner owner frame
     * @param path default path to browse
     */
    public FileChooserPane(Container owner, String path) {
        super(2);
        this.owner = owner;
        files = new JFileChooser();
        files.setDragEnabled(true);
        initToPath(path);
        files.setMultiSelectionEnabled(true);
        filesOpenButton = new JButton(lang.getString("select..."));
        filesOpenButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectedFiles = showDialog(selectionMode);
            }
        });
        filesResult = new LogArea(lang.getString("selectedfilesappear") + Console.newLine);
        add(new JLabel(lang.getString("selectthefiles")), 1, 2, 1.0, 1.0);
        add(filesOpenButton, 1, 2, 1.0, 0.0);
        add(filesResult, 1, 2, 1.0, 1.0);
    }

    /**
     * sets up the LogArea instance to use
     *
     * @param log the LogArea to use for this FileChooserPane instance
     */
    public void setLogArea(LogArea log) {
        filesResult = log;
    }

    /**
     * returns the current LogArea that this FileChooserPane instance is using
     *
     * @return the LogArea instance that this FileChooserPane instance is using
     */
    public LogArea getLogArea() {
        return filesResult;
    }

    /**
     * clears all file filters
     *
     * @see JFileChooser#resetChoosableFileFilters()
     */
    public void clearAllFileFilters() {
        files.resetChoosableFileFilters();
    }

    /**
     * adds one new file extension to filter.
     *
     * @param ext file extension to filter, ommitting the dot "."
     *
     * @see JFileChooser#addChoosableFileFilter(FileFilter)
     */
    public ExampleFileFilter addFileFilter(HashSet ext) {
        ExampleFileFilter myFF = new ExampleFileFilter();
        Set set = Collections.synchronizedSet(ext);
        synchronized (set) {
            Iterator i = set.iterator();
            while (i.hasNext()) {
                myFF.addExtension((String) i.next());
            }
        }
        files.addChoosableFileFilter(myFF);
        return myFF;
    }

    /**
     * pops up file chooser Dialog window.
     *
     * @return return state of the Dialog when closed
     * @see JFileChooser#APPROVE_OPTION
     * @see JFileChooser#CANCEL_OPTION
     * @see JFileChooser#ERROR_OPTION
     * @param mode the mode to use
     * @see #OPEN
     * @see #SAVE
     * @see #DEFAULT
     * @see Dialog
     */
    private int show(final int mode) {
        try {
            return UIMessage.invokeSwingAndReturn(new SwingStaticReturn<Integer>() {
                public Integer run() {
                    return _show(mode);
                }
            });
        } catch (Exception ex) {
            if (JXAenvUtils2._debug) {
                ex.printStackTrace();
            }
            return 0;
        }
    }

    private int _show(int mode) {
        String sel = null;
        if (imagePreview) {
            previewTimer.start();
        }
        switch (files.getFileSelectionMode()) {
            case JFileChooser.FILES_AND_DIRECTORIES:
                sel = (files.isMultiSelectionEnabled()) ? lang.getString("fileanddirectory") : lang.getString("fileordirectory");
                break;
            case JFileChooser.FILES_ONLY:
                sel = (files.isMultiSelectionEnabled()) ? lang.getString("files") : lang.getString("file");
                break;
            case JFileChooser.DIRECTORIES_ONLY:
                sel = (files.isMultiSelectionEnabled()) ? lang.getString("directories") : lang.getString("directory");
                break;
            default:
                break;
        }
        String str = lang.getString("Choose") + sel + lang.getString("Choose_de") + "...";
        switch (mode) {
            case OPEN:
                status = files.showOpenDialog(owner);
                break;
            case SAVE:
                status = files.showSaveDialog(owner);
                break;
            default:
                status = files.showDialog(owner, str);
                break;
        }
        selectedFiles = files.getSelectedFiles();
        return status;
    }

    /**
     * Returns the status of the Dialog window.
     *
     * @return last state
     * @see JFileChooser
     */
    public int getStatus() {
        return status;
    }

    /**
     * Returns the last selected files.
     *
     * @return Files array
     *
     */
    public File[] getSelectedFiles() {
        return selectedFiles;
    }

    /**
     * sets up manually the selected files array
     *
     * @param files the selected files array to manually set up
     */
    public void setSelectedFiles(File[] files) {
        this.files.setSelectedFiles(files);
        selectedFiles = files;
        for (File name : files) {
            filesResult.pushNewLog("\n" + lang.getString("copypasteop") + name.getPath());
        }
        filesResult.pushNewLog("\n");
    }

    /**
     * Loads a FileChooser dialog, then returns user selected directories.
     *
     * @return an array of files containing the selected directories
     */
    public File[] getDirectory() {
        return showDialog(JFileChooser.DIRECTORIES_ONLY);
    }

    /**
     * switch mode to OPEN
     *
     * @param b dis/enables the mode for OPEN
     */
    public void setModeOpen(boolean b) {
        mode = (b) ? OPEN : DEFAULT;
    }

    /**
     * switch mode to SAVE
     *
     * @param b dis/enables the mode for SAVE
     */
    public void setModeSave(boolean b) {
        mode = (b) ? SAVE : DEFAULT;
    }
    /**
     * selection mode
     */
    private int selectionMode = JFileChooser.FILES_AND_DIRECTORIES;

    /**
     * sets up the file and/or directory selection mode.
     *
     * @default {@link JFileChooser.FILES_AND_DIRECTORIES file and directories}
     *
     * @param mode the mode to use
     * @see JFileChooser#setFileSelectionMode(int)
     * @see JFileChooser#FILES_ONLY
     * @see JFileChooser#FILES_AND_DIRECTORIES
     * @see JFileChooser#DIRECTORIES_ONLY
     * @see #getFileSelectionMode()
     */
    public void setFileSelectionMode(int mode) {
        selectionMode = mode;
        switch (mode) {
            default:
                filesOpenButton.setText(lang.getString("select..."));
                break;
            case JFileChooser.FILES_ONLY:
                filesOpenButton.setText(lang.getString("selectfiles"));
                break;
            case JFileChooser.DIRECTORIES_ONLY:
                filesOpenButton.setText(lang.getString("selectfolders"));
                break;
        }
    }

    /**
     * returns the current selection mode
     *
     * @return the current selection mode
     * @see #setFileSelectionMode(int)
     */
    public int getFileSelectionMode() {
        return selectionMode;
    }

    /**
     * shows up a JFIleChooser file selection dialog
     *
     * @param mode the mode to use
     * @see JFileChooser#setFileSelectionMode(int)
     * @see JFileChooser#FILES_ONLY
     * @see JFileChooser#FILES_AND_DIRECTORIES
     * @see JFileChooser#DIRECTORIES_ONLY
     * @return Files array
     */
    public File[] showDialog(int mode) {
        this.files.setFileSelectionMode(mode);
        int returnState = show(this.mode);
        switch (returnState) {
            case JFileChooser.APPROVE_OPTION:
                filesResult.clearLog();
                selectedFiles = (this.files.isMultiSelectionEnabled()) ? this.files.getSelectedFiles() : new File[]{this.files.getSelectedFile()};
                if (imagePreview) {
                    previewTimer.stop();
                }
                Vector<File> c_files = new Vector<File>();
                if (this.files.isMultiSelectionEnabled()) {
                    File[] select = selectedFiles;
                    for (int i = 0; i < select.length; i++) {
                        File file = select[i];
                        if (!file.getAbsoluteFile().canRead() && this.mode == OPEN) {
                            new UIMessage(true, lang.getString("thefile") + " " + file.getAbsolutePath() + " " + lang.getString("isunreadable"), null);
                        } else {
                            System.out.println("selected File " + file.getAbsolutePath());
                            c_files.add(file.getAbsoluteFile());
                        }
                    }
                } else {
                    if (!selectedFiles[0].getAbsoluteFile().canRead() && this.mode == OPEN) {
                        new UIMessage(true, lang.getString("thefile") + " " + selectedFiles[0].getAbsolutePath() + lang.getString("isunreadable"), null);
                    } else {
                        System.out.println("selected File " + selectedFiles[0].getAbsolutePath());
                        c_files.add(selectedFiles[0].getAbsoluteFile());
                    }
                }
                filesResult.pushNewLog(lang.getString("selectedfiles"));
                String s = " ";
                long msgFrame = UIMessage.displayWaiting(lang.getString("loadingfiles"), this.files);
                for (File f : c_files) {
                    filesResult.pushNewLog(s + f.getName());
                    s = ", ";
                }
                UIMessage.kill(msgFrame);
                return c_files.toArray(new File[]{});
            case JFileChooser.CANCEL_OPTION:
                filesResult.pushNewLog(lang.getString("nofilesselected"));
                break;
            case JFileChooser.ERROR_OPTION:
                filesResult.pushNewLog("erroroccured");
                break;
            default:
                filesResult.pushNewLog("");
                break;
        }
        return new File[]{};
    }

    /**
     * shows up a file selection dialog and returns the selected files
     *
     * @return Files array or null
     */
    public File[] getFile() {
        return showDialog(JFileChooser.FILES_ONLY);
    }

    /**
     * shows a file and directory selection dialog and returns the selected
     * files and/or directories
     *
     * @return Files array or null
     */
    public File[] getFileAndDirectory() {
        return showDialog(JFileChooser.FILES_AND_DIRECTORIES);
    }

    /**
     * init to JFileChooser to path
     *
     * @return true or false whether it succeeded to init or not, resp.
     */
    public boolean initToPath(String path) {
        File file = null;
        try {
            if (path == "" || path == null) {
                file = null;
            } else {
                file = new File(path);
            }
            files.setCurrentDirectory(file);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * returns the JFileChooser instance used in this FileChooserPane
     *
     * @return JFileChooser instance
     */
    public JFileChooser getFiles() {
        return files;
    }

    /**
     * dis/enable multiple selection
     *
     * @param b dis/enable
     * @see JFileChooser#setMultiSelectionEnabled(boolean)
     */
    public void setMultiSelectionEnabled(boolean b) {
        files.setMultiSelectionEnabled(b);
    }
}
