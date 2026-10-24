package jxa;

import com.sun.media.imageio.stream.FileChannelImageInputStream;
import com.sun.media.imageio.stream.FileChannelImageOutputStream;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.text.DateFormat;
import java.util.*;
import javax.imageio.*;
import javax.imageio.event.IIOReadProgressListener;
import javax.imageio.event.IIOReadWarningListener;
import javax.imageio.event.IIOWriteProgressListener;
import javax.imageio.event.IIOWriteWarningListener;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import net.sf.jiga.xtended.impl.Sprite;
import net.sf.jiga.xtended.impl.SpriteIO;
import net.sf.jiga.xtended.kernel.Console;
import net.sf.jiga.xtended.kernel.JXAenvUtils;
import net.sf.jiga.xtended.kernel.SpritesCacheManager;
import net.sf.jiga.xtended.kernel.ThreadWorks;
import net.sf.jiga.xtended.ui.*;

public class ImageMultiConverter_Ant implements AntApplet {

    private JCheckBox sw_resize_height, sw_resize_width, sw_resize;
    private JSpinner sw_resize_height_v;
    private HashMap<Integer, JCheckBox> sw_color_option;
    private JSpinner sw_compression_quality;
    private JSpinner sw_resize_width_v;
    private JFrame owner = null;
    private JPanel display;
    private File[] selectedFiles;
    private LogArea sw_log;
    private FileChooserPane jfc;
    private JButton sw_selectFormat, sw_convert;
    private ImageOutputStream ios;
    private JProgressBar fileJPB;
    private JEditorPane sw_html;
    private StatusPane toggleStep;
    private JSpinner sw_zoom;
    private String selectedConversion = "image/jpeg";
    private SpritesCacheManager<Object, Object> cache;
    private ThreadWorks converterThread;
    private int iterator = 0;
    private boolean ignoreMsg = false;
    boolean blackAndWhiteEnabled = false;
    boolean canvasScaleOnlyEnabled = false;
    float compression_quality = 0.8f;

    private InstructionsPane createDisplayNavigator(JPanel panel, int startStep, String[] indexedLabels) {
        InstructionsPane outputDisplayToggle;
        Navigator scroll;
        outputDisplayToggle = new InstructionsPane(panel, startStep, indexedLabels.length);
        scroll = new Navigator(outputDisplayToggle, indexedLabels);
        for (int i = 0; i < indexedLabels.length; i++) {
            outputDisplayToggle.addStep(1);
        }
        panel.setBorder(BorderFactory.createTitledBorder("Output pictures"));
        panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = c.REMAINDER;
        c.fill = c.BOTH;
        c.weighty = 0.9;
        panel.add(outputDisplayToggle, c);
        c.weighty = 0.1;
        panel.add(outputDisplayToggle.getStatusPane(), c);
        panel.validate();
        return outputDisplayToggle;
    }

    private JPanel createPanel2pic(final ImageCollection imageCollection, String k, final File outputFile, final String nK) {
        JPanel panel2pic = new JPanel(new GridBagLayout());
        GridBagConstraints outputC = new GridBagConstraints();
        outputC.fill = outputC.BOTH;
        outputC.weightx = 1.0;
        outputC.weighty = 1.0;
        if (imageCollection.hasPicture(k, true)) {
            JComponent picture = imageCollection.getThumbnailComponent(k);
            panel2pic.add(picture, outputC);
            if (imageCollection.hasPicture(nK, true)) {
                picture = imageCollection.getThumbnailComponent(nK);
                outputC.gridwidth = outputC.REMAINDER;
                panel2pic.add(picture, outputC);
                outputC.gridwidth = 2;
                final JLabel lab;
                lab = new JLabel("<html><u>" + nK + "</u></html>");
                if (outputFile instanceof File) {
                    lab.addMouseListener(new MouseAdapter() {
                        public void mouseClicked(MouseEvent e) {
                            if (JXAenvUtils.env.OS_WINDOWS.isEnv()) {
                                JXAenvUtils._runShell(new String[]{"start", "\\\"" + nK + "\\\"", "\"" + outputFile.getAbsolutePath() + "\""}, false, false, false);
                            } else {
                                JXAenvUtils._runShell(new String[]{"open", "\\\"" + outputFile.getAbsolutePath() + "\\\""}, false, false, false);
                            }
                            Sprite._quickPaintImmediately(imageCollection.getThumbnailComponent(nK));
                        }

                        public void mouseEntered(MouseEvent e) {
                            lab.setText("<html>" + nK + "</html>");
                            Sprite._quickPaintImmediately(lab);
                        }

                        public void mouseExited(MouseEvent e) {
                            lab.setText("<html><u>" + nK + "</u></html>");
                            Sprite._quickPaintImmediately(lab);
                        }
                    });
                }
                panel2pic.add(lab, outputC);
            }
        }
        panel2pic.validate();
        return panel2pic;
    }
    private DnDHandler dnd;

    /**
     * use Sprite IO for writing to the output files
     *
     * @default true
     */
    public boolean spriteIo = !Boolean.getBoolean("jxa.sprite.io.disable");

    public void init() {
        if (trayIcon == null) {
            trayIcon = new SystemTrayIcon(ImageMultiConverter_Ant.class.getResource("/jxa/imc.gif"), "IMC");
        }
        SpriteIO._MIN_THUMBNAILS_SIZE = SpriteIO._WRITE_TILES_DIMENSION.width;
        cache = new SpritesCacheManager<Object, Object>(100);
        applet.getContentPane().setLayout(new BorderLayout());
        Container content = applet.getContentPane();
        final InstructionsPane appletInsPane = new InstructionsPane(applet, 0, 5);
        toggleStep = new StatusPane(appletInsPane);
        BufferedImage logoImage = null;
        try {
            ResourceBundle ads = ResourceBundle.getBundle("ads_script");
            URL ads_url = getClass().getResource(ads.getString("src"));
            System.out.println("ads url : " + ads_url);
            /**
             * TODO : use JavaFX 2.0+ WebEngine to show javascript ads 
             * WebView
             * wv = new WebView(); wv.getEngine().load(ads_url);
             * fxContainer.setScene(new Scene(wv));
             */
            sw_html = new JEditorPane(ads_url);
            sw_html.addHyperlinkListener(new HyperlinkAdapter());
            sw_html.setPreferredSize(new Dimension(300, 150));
            sw_html.setEditable(false);
            /* logo image */
            logoImage = ImageIO.read(ImageMultiConverter_Ant.class.getResourceAsStream("/net/sf/jiga/xtended/ui/images/imc-logo.png"));
            content.add(sw_html, BorderLayout.NORTH);
            content.add(appletInsPane, BorderLayout.CENTER);
            content.add(toggleStep, BorderLayout.SOUTH);
            dnd = new DnDHandler(this);
            dnd.addDropTarget(appletInsPane);
            int step = appletInsPane.addStep();
            appletInsPane.addStepComponent(step, new JLabel("<html><center>Go to next step and <i>select the images files you want to convert</i> !"
                    + "<br>Tip : Use the <b>drag'n'drop</b> with this application to quickly select your files !</center></html>"));
            step = appletInsPane.addStep();
            jfc = new FileChooserPane(appletInsPane, System.getProperty("user.home"));
            jfc.setModeOpen(true);
            jfc.setImagePreviewEnabled(!JXAenvUtils.env.OS_MAC.isEnv());
            jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            ImagePluginFilter._updateFileFilters(jfc, "r");
            appletInsPane.addStepComponent(step, sw_selectFormat = new JButton("Conversion format..."));
            sw_selectFormat.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Set<ImagePluginFilter> s = new LinkedHashSet<ImagePluginFilter>(ImagePluginFilter._getFileFilters("w"));
                    int selected = 0, i = 0;
                    for (ImagePluginFilter plugin : s) {
                        if (plugin.props.getProperty("plugin mime").equals(getSelectedConversion())) {
                            selected = i;
                        }
                        i++;
                    }
                    ImagePluginFilter[] sArr = s.toArray(new ImagePluginFilter[]{});
                    ImagePluginFilter selectedOutput = (ImagePluginFilter) UIMessage.showSelectDialog(ImageMultiConverter_Ant.this.applet,
                            "<html>What conversion type <br>do you wish to use as the output ?</html>", "Convert...",
                            sArr,
                            sArr[selected]);
                    for (ImagePluginFilter mime : s) {
                        if (mime.equals(selectedOutput)) {
                            selectedConversion = mime.mime;
                        }
                    }
                    System.out.println("conv : " + selectedConversion);
                    sw_convert.setText("Convert to " + getSelectedConversion());
                }
            });

            appletInsPane.addInstructionSet(jfc);

            step = appletInsPane.addStep(2);
            appletInsPane.addStepComponent(step, new JLabel("This value will zoom the image to choosen scale (%):"), 1, 2);
            appletInsPane.addStepComponent(step, sw_zoom = new JSpinner(new SpinnerNumberModel(100, 1, 500, 1)), 1, 2);
            appletInsPane.addStepComponent(step, sw_resize_width = new JCheckBox("resize width :"));
            appletInsPane.addStepComponent(step, sw_resize_width_v = new JSpinner(new SpinnerNumberModel(150, 1, 3000, 1)));
            appletInsPane.addStepComponent(step, sw_resize_height = new JCheckBox("resize height :"));
            appletInsPane.addStepComponent(step, sw_resize_height_v = new JSpinner(new SpinnerNumberModel(150, 1, 3000, 1)));
            appletInsPane.addStepComponent(step, new JLabel("compression quality (JPEG only)"));
            appletInsPane.addStepComponent(step, sw_compression_quality = new JSpinner(new SpinnerNumberModel((int) (compression_quality * 100), 0, 100, 10)));
            final HashMap<Integer, Action> color_options = new HashMap<Integer, Action>();
            final HashMap<Integer, Boolean> color_options_switch = new HashMap<Integer, Boolean>();
            sw_color_option = new HashMap<Integer, JCheckBox>();
            final int BLACKANDWHITE = 0;
            final int CANVASSCALEONLY = 1;
            color_options.put(BLACKANDWHITE, new AbstractAction("black & white") {
                public void actionPerformed(ActionEvent e) {
                    blackAndWhiteEnabled = sw_color_option.get(BLACKANDWHITE).isSelected();
                }
            });
            color_options_switch.put(BLACKANDWHITE, blackAndWhiteEnabled);
            color_options.put(CANVASSCALEONLY, new AbstractAction("image canvas scale only") {
                public void actionPerformed(ActionEvent e) {
                    canvasScaleOnlyEnabled = sw_color_option.get(CANVASSCALEONLY).isSelected();
                }
            });
            color_options_switch.put(CANVASSCALEONLY, canvasScaleOnlyEnabled);
            for (Iterator<Integer> i = color_options.keySet().iterator(); i.hasNext();) {
                int aKey = i.next();
                sw_color_option.put(aKey, new JCheckBox(color_options.get(aKey)));
                sw_color_option.get(aKey).setSelected(color_options_switch.get(aKey));
                appletInsPane.addStepComponent(step, new JLabel("Option : "));
                appletInsPane.addStepComponent(step, sw_color_option.get(aKey));
            }
            sw_zoom.setValue(100);
            sw_resize_width.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    sw_resize_width_v.setEnabled(sw_resize_width.isSelected());
                }
            });
            sw_resize_width.setSelected(false);
            sw_resize_width_v.setEnabled(false);
            sw_resize_height.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    sw_resize_height_v.setEnabled(sw_resize_height.isSelected());
                }
            });
            sw_resize_height_v.setEnabled(false);
            sw_resize_height.setSelected(false);

            step = appletInsPane.addStep(2);
            appletInsPane.addStepComponent(step, sw_convert = new JButton("Convert to " + getSelectedConversion()));

            sw_log = new LogArea();
            sw_log.setPreferredSize(new Dimension(400, 100));
            appletInsPane.addStepComponent(step, new JButton(new AbstractAction("Show log") {
                public void actionPerformed(ActionEvent e) {
                    new UIMessage(false, sw_log, null);
                }
            }));
            appletInsPane.addStepComponent(step, new JLabel("Conversion progress: "), 1, 2);
            appletInsPane.addStepComponent(step, display = new JPanel(true), 1, 2, 1.0, 1.0);
            /**
             * example for layout
             */
            display.removeAll();
            InstructionsPane panelViewImageOutput = createDisplayNavigator(display, 0, new String[]{"logo"});
            final ImageCollection imageCollection = new ImageCollection(panelViewImageOutput, 10, false, true);
            JPanel p2picExample = createPanel2pic(imageCollection, "logo", null, "logo");
            imageCollection.setThumbnailSize(new Dimension(64, 64));
            imageCollection.addImage(logoImage, "logo");
            imageCollection.waitForAll();
            imageCollection.getErrored(true);
            panelViewImageOutput.addStepComponent(0, p2picExample, 1, 1, 1.0, 1.0);
            panelViewImageOutput.validate();
            /**/
            appletInsPane.addStepComponent(step, fileJPB = new JProgressMemory(0, 1), 1, 2, 1.0, 0.0);
            ((JProgressMemory) fileJPB).setLabels(" ", " ", " ");
            sw_convert.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (jfc.getSelectedFiles() != null) {
                        sw_log.clearLog();
                        File[] sFiles = jfc.getSelectedFiles();
                        Vector<File> files = new Vector<File>();
                        for (File file : sFiles) {
                            if (file.isDirectory()) {
                                for (File aFile : file.listFiles()) {
                                    if (jfc.getFiles().accept(aFile)) {
                                        files.add(aFile);
                                    }
                                }
                            } else {
                                if (jfc.getFiles().accept(file)) {
                                    files.add(file);
                                }
                            }
                        }
                        selectedFiles = files.toArray(new File[]{});
                        final FileChooserPane jfc_out = new FileChooserPane(null, System.getProperty("user.home"));
                        jfc_out.setMultiSelectionEnabled(false);
                        jfc_out.setModeSave(true);
                        File[] outputDirs = jfc_out.getDirectory();
                        if (outputDirs.length == 0) {
                            return;
                        }
                        final String[] outputDir = new String[outputDirs.length];
                        int i = 0;
                        for (File dir : outputDirs) {
                            outputDir[i++] = dir.getPath();
                        }
                        if (jfc_out.getStatus() == JFileChooser.APPROVE_OPTION) {
                            final ProgressListener uiprg = new ProgressListener();
                            final WarningListener uiwarn = new WarningListener();
                            String[] selectedFilenames = new String[selectedFiles.length];
                            for (int n = 0; n < selectedFiles.length; n++) {
                                selectedFilenames[n] = selectedFiles[n].getName();
                            }

                            /**
                             * re-layout panel
                             */
                            display.removeAll();
                            final InstructionsPane panelViewImageOutput = createDisplayNavigator(display, 0, selectedFilenames);

                            /**
                             *
                             */
                            cache.clear();
                            iterator = 0;
                            // images read
                            cache.cleanup();
                            converterThread = new ThreadWorks("converter");
                            final Runnable r = new Runnable() {
                                private void clearImage(BufferedImage img) {
                                    if (img.getTransparency() != BufferedImage.TRANSLUCENT) {
                                        Graphics2D g = Sprite.wrapRendering(img.getGraphics());
                                        g.setColor(Color.WHITE);
                                        g.fill(new Rectangle(img.getWidth(), img.getHeight()));
                                        g.dispose();
                                    }
                                }

                                public void run() {
                                    int pty = Thread.currentThread().getPriority();
                                    Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                                    Stack<BufferedImage> img = new Stack<BufferedImage>();
                                    Stack<IIOMetadata> readMeta = new Stack<IIOMetadata>();
                                    JComponent picture = null;
                                    fileJPB.setMaximum(selectedFiles.length - 1);
                                    fileJPB.setStringPainted(true);
                                    final int index = iterator++;
                                    File file = selectedFiles[index];
                                    cache.add(index, file);
                                    fileJPB.setValue(index);
                                    fileJPB.setString((selectedFiles.length - 1 - index) + " image files are remaining...");
                                    File inputFile = file;
                                    final String k = selectedFiles[index].getName();
                                    RandomAccessFile raf;
                                    ImageReader r = null;
                                    ImageInputStream iis = null;
                                    RProgressListener rpl = new RProgressListener();
                                    RWarningListener rwl = new RWarningListener();
                                    try {
                                        r = (ImageReader) ImageIO.getImageReadersByMIMEType(ImageCollection._getImageMimeType(inputFile, "r")).next();
                                        r.setInput(iis = new FileChannelImageInputStream((raf = new RandomAccessFile(inputFile, "r")).getChannel()));
                                        cache.buffer(iis);
                                        r.addIIOReadProgressListener(rpl);
                                        r.addIIOReadWarningListener(rwl);
                                        toggleStep.addActiveStream((Closeable) raf.getChannel());
                                        cache.buffer(img);
                                        /**
                                         * read once
                                         */
                                        img.push(r.read(r.getMinIndex(), r.getDefaultReadParam()));
                                        readMeta.push(r.getImageMetadata(r.getMinIndex()));
                                        r.dispose();
                                        iis.close();
                                        raf.close();
                                        imageCollection.addImage(img.peek(), k);
                                        imageCollection.waitForPicture(k);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                        String[] errs = imageCollection.getErrored(true);
                                        if (!ignoreMsg) {
                                            new UIMessage(false, Arrays.toString(errs) + " An unknown error has occured while reading..." + ex.getMessage(), ImageMultiConverter_Ant.this.owner);
                                            showIgnoreDialog();
                                        }
                                    } finally {
                                        MediaTracker mt = new MediaTracker(display);
                                        // images transformations
                                        double zoom = ((SpinnerNumberModel) sw_zoom.getModel()).getNumber().intValue() / 100.0;
                                        int width = (sw_resize_width_v.isEnabled()) ? ((SpinnerNumberModel) sw_resize_width_v.getModel()).getNumber().intValue() : 0;
                                        int height = (sw_resize_height_v.isEnabled()) ? ((SpinnerNumberModel) sw_resize_height_v.getModel()).getNumber().intValue() : 0;
                                        AffineTransform tx;
                                        // reverse order to keep integrity on writing
                                        BufferedImage[] revReadImg = new BufferedImage[img.size()];
                                        IIOMetadata[] revReadMeta = new IIOMetadata[readMeta.size()];
                                        img.toArray(revReadImg);
                                        readMeta.toArray(revReadMeta);
                                        readMeta.clear();
                                        img.clear();
                                        for (BufferedImage image : revReadImg) {
                                            img.add(0, image);
                                        }
                                        for (IIOMetadata meta : revReadMeta) {
                                            readMeta.add(0, meta);
                                        }
                                        revReadImg = null;
                                        revReadMeta = null;
                                        Stack<BufferedImage> newImg = new Stack<BufferedImage>();
                                        while (!img.empty()) {
                                            /**
                                             * 1st : do zoom with scale factor
                                             * spinner
                                             */
                                            tx = AffineTransform.getScaleInstance(zoom, zoom);
                                            System.out.println("original : " + img.peek().getWidth() + "x" + img.peek().getHeight() + " scale zoom : " + zoom);
                                            cache.buffer(tx);
                                            Dimension newDim = new Dimension((int) (img.peek().getWidth() * tx.getScaleX()), (int) (img.peek().getHeight() * tx.getScaleY()));
                                            BufferedImage bimg = new BufferedImage(newDim.width, newDim.height, ImageCollection._getBufferedType(selectedConversion));
                                            clearImage(bimg);
                                            cache.buffer(bimg);
                                            Graphics2D g2 = Sprite.wrapRendering(bimg.getGraphics());
                                            try {
                                                mt.addImage(img.peek(), 0, newDim.width, newDim.height);
                                                mt.waitForID(0);
                                                mt.removeImage(img.peek(), 0);
                                                if (canvasScaleOnlyEnabled) {
                                                    g2.translate((int) ((float) newDim.width / 2f - (float) img.peek().getWidth() / 2f), (int) ((float) newDim.height / 2f - (float) img.peek().getHeight() / 2f));
                                                    g2.drawImage(img.peek(), null, ImageMultiConverter_Ant.this.applet);
                                                } else {
                                                    g2.drawImage(img.peek(), tx, ImageMultiConverter_Ant.this.applet);
                                                }
                                                if (blackAndWhiteEnabled) {
                                                    BufferedImage bimgGRAY = new BufferedImage(newDim.width, newDim.height, BufferedImage.TYPE_BYTE_GRAY);
                                                    cache.buffer(bimgGRAY);
                                                    Graphics2D g2GRAY = Sprite.wrapRendering(bimgGRAY.createGraphics());
                                                    if (canvasScaleOnlyEnabled) {
                                                        g2GRAY.translate((int) ((float) newDim.width / 2f - (float) img.peek().getWidth() / 2f), (int) ((float) newDim.height / 2f - (float) img.peek().getHeight() / 2f));
                                                        g2GRAY.drawImage(img.peek(), null, ImageMultiConverter_Ant.this.applet);
                                                    } else {
                                                        g2GRAY.drawImage(img.peek(), tx, ImageMultiConverter_Ant.this.applet);
                                                    }
                                                    g2GRAY.dispose();
                                                    Composite cps = g2.getComposite();
                                                    g2.setComposite(AlphaComposite.SrcIn);
                                                    g2.drawImage(bimgGRAY, null, ImageMultiConverter_Ant.this.applet);
                                                    g2.setComposite(cps);
                                                }
                                                g2.dispose();
                                                /*
                                                 * 2nd : do zoom with the fixed
                                                 * dim's
                                                 */
                                                double zoom_width = 1.0;
                                                double zoom_height = 1.0;
                                                /*
                                                 * compute zoom by fixed
                                                 * dimensions
                                                 */
                                                if (width > 0) {
                                                    zoom_width = (double) width / (double) bimg.getWidth();
                                                    if (height == 0) {
                                                        zoom_height = zoom_width;
                                                    }
                                                }
                                                if (height > 0) {
                                                    zoom_height = (double) height / (double) bimg.getHeight();
                                                    if (width == 0) {
                                                        zoom_width = zoom_height;
                                                    }
                                                }
                                                tx = AffineTransform.getScaleInstance(zoom_width, zoom_height);
                                                Dimension newDim2 = new Dimension((int) (bimg.getWidth() * tx.getScaleX()), (int) (bimg.getHeight() * tx.getScaleY()));
                                                BufferedImage bimg2 = new BufferedImage(newDim2.width, newDim2.height, ImageCollection._getBufferedType(selectedConversion));
                                                System.out.println("destination : " + bimg2.getWidth() + "x" + bimg2.getHeight() + " proportioned zoom : " + zoom_width + "x" + zoom_height);
                                                System.out.println(tx.getScaleX());
                                                System.out.println(tx.getScaleY());
                                                clearImage(bimg2);
                                                cache.buffer(bimg2);
                                                Graphics2D g22 = Sprite.wrapRendering(bimg2.getGraphics());
                                                if (canvasScaleOnlyEnabled) {
                                                    g22.translate((int) ((float) newDim2.width / 2f - (float) bimg.getWidth() / 2f), (int) ((float) newDim2.height / 2f - (float) bimg.getHeight() / 2f));
                                                    g22.drawImage(bimg, null, ImageMultiConverter_Ant.this.applet);
                                                } else {
                                                    g22.drawImage(bimg, tx, ImageMultiConverter_Ant.this.applet);
                                                }
                                                g22.dispose();
                                                img.pop();
                                                newImg.push(bimg2);
                                            } catch (InterruptedException ex) {
                                                ex.printStackTrace();
                                            }
                                        }
                                        // images conversion
                                        String conversion = selectedConversion;
                                        String conversion_ext = "." + ImageCollection._getPluginProperties(selectedConversion).getProperty("plugin extension").split(" ")[0];
                                        // images write
                                        File of = new File(outputDir[0] + File.separator + file.getName().substring(0, file.getName().lastIndexOf('.')) + conversion_ext);
                                        int addName = 0;
                                        while (of.exists()) {
                                            of = new File(outputDir[0] + File.separator + file.getName().substring(0, file.getName().lastIndexOf('.')) + "_" + (addName++) + conversion_ext);
                                        }
                                        final File outputFile = of;
                                        cache.buffer(outputFile);
                                        sw_log.pushNewLog(Console.newLine + "output file: " + outputFile);
                                        //outputFile.createNewFile();
                                        compression_quality = ((SpinnerNumberModel) sw_compression_quality.getModel()).getNumber().floatValue() / 100f;
                                        sw_log.pushNewLog(Console.newLine + "conversion " + conversion);
                                        ImageWriter w = (ImageWriter) ImageIO.getImageWritersByMIMEType(selectedConversion).next();
                                        cache.buffer(w);
                                        final String nK = outputFile.getName();
                                        try {
                                            w.addIIOWriteProgressListener(uiprg);
                                            w.addIIOWriteWarningListener(uiwarn);
                                            while (!newImg.empty()) {
                                                if (spriteIo) {
                                                    Sprite sp = new Sprite(newImg.peek(), selectedConversion, new Dimension(newImg.peek().getWidth(), newImg.peek().getHeight()));
                                                    sp.runValidate();
                                                    sp.writeImageToOutput(outputFile, compression_quality);
                                                } else {
                                                    raf = new RandomAccessFile(outputFile, "rw");
                                                    try {
                                                        raf.setLength(0);
                                                        w.setOutput(ios = new FileChannelImageOutputStream((raf = new RandomAccessFile(outputFile, "rw")).getChannel()));
                                                        cache.buffer(ios);
                                                        cache.buffer(raf);
                                                        toggleStep.addActiveStream((Closeable) raf.getChannel());
                                                        mt.addImage(newImg.peek(), 0);
                                                        mt.waitForID(0);
                                                        mt.removeImage(newImg.peek(), 0);
                                                        ImageWriteParam wparam = w.getDefaultWriteParam();
                                                        if (wparam.canWriteCompressed()) {
                                                            try {
                                                                wparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                                                                wparam.setCompressionQuality(compression_quality);
                                                                wparam.setCompressionType(ImageCollection._getCompressionType(selectedConversion, newImg.peek().getType()));
                                                            } catch (RuntimeException ex) {
                                                                ex.printStackTrace();
                                                            }
                                                        }
                                                        IIOMetadata imgMeta = null;
                                                        ImageTypeSpecifier type = ImageTypeSpecifier.createFromRenderedImage((RenderedImage) newImg.peek());
                                                        imgMeta = w.getDefaultImageMetadata(type, wparam);
                                                        w.write(w.getDefaultStreamMetadata(wparam), new IIOImage((RenderedImage) newImg.peek(), null, imgMeta), wparam);
                                                    } catch (InterruptedException ex) {
                                                        ex.printStackTrace();
                                                    } finally {
                                                        ios.close();
                                                        raf.close();
                                                    }
                                                }
                                                imageCollection.addImage(newImg.peek(), nK);
                                                imageCollection.waitForPicture(nK);
                                                newImg.pop();
                                            }
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        String[] errs = imageCollection.getErrored(true);
                                            if (!ignoreMsg) {
                                                new UIMessage(false, Arrays.toString(errs) + " An unknown error has occured while writing. See log!", ImageMultiConverter_Ant.this.owner);
                                                showIgnoreDialog();
                                            }
                                        } finally {
                                            w.dispose();
                                            ThreadWorks.Swing.invokeLater(new Runnable() {

                                                public void run() {
                                                    JPanel panel2pic = createPanel2pic(imageCollection, k, outputFile, nK);
                                                    panelViewImageOutput.addStepComponent(index, panel2pic, 1, 1, 1.0, 1.0);
                                                    /**
                                                     * relayout InstructionsPane
                                                     */
                                                    panelViewImageOutput.getStatusPane().stepChanged();
                                                }
                                            });
                                        }
                                    }
                                }
                            };
                            converterThread.checkAlive();
                            converterThread.doLater(new Runnable() {
                                public void run() {
                                    sw_log.setLogStderrEnabled(true);
                                    toggleStep.addActiveThread(Thread.currentThread());
                                }
                            });
                            final long time = System.currentTimeMillis();
                            for (File f : selectedFiles) {
                                converterThread.doLater(r);
                            }
                            converterThread.doLater(new Runnable() {
                                public void run() {
                                    double d = System.currentTimeMillis() - time;
                                    String str = d > 60000 ? "about " + Math.round(d / 60000) + " minute" + (d < 120000 ? "" : "s") : Math.round(d / 1000) + " seconds";
                                    String message = selectedFiles.length + " files conversion took " + str;
                                    getTrayIcon().displayMessage("ImageMultiConverter", message, SystemTrayIcon.INFO);
                                    System.err.println(message);
                                    sw_log.setLogStderrEnabled(false);
                                }
                            });
                        } else {
                            return;
                        }
                        if (jfc.getStatus() == JFileChooser.CANCEL_OPTION) {
                            return;
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            new UIMessage(true, e.getMessage(), null);
        } finally {
            applet.validate();
            applet.repaint();
            initialized = true;
        }
    }

    private void showIgnoreDialog() {
        switch (UIMessage.showConfirmDialog(ImageMultiConverter_Ant.this.sw_convert, "Ignore further messages ?", "Ignore ?", JOptionPane.YES_NO_CANCEL_OPTION)) {
            case JOptionPane.YES_OPTION:
                ignoreMsg = true;
                break;

            case JOptionPane.CANCEL_OPTION:
                toggleStep.cancelAll();
                break;

            case JOptionPane.NO_OPTION:
            case JOptionPane.CLOSED_OPTION:
                ignoreMsg = false;
                break;

            default:

        }
    }

    private String getSelectedConversion() {
        return selectedConversion;
    }
    boolean initialized = false;

    public void destroy() {
        if (initialized) {
            applet.getContentPane().removeAll();
        }
        initialized = false;
    }

    public void start() {
    }
    JApplet applet = null;

    public void setApplet(JApplet arg0) {
        applet = arg0;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public Runnable shutdownHook() {
        return null;
    }

    public Display getSplash() {
        return null;
    }

    public Vector<Action> getLoadLayers() {
        Vector<Action> v = new Vector<Action>();
        return v;
    }

    private int remaining(JProgressBar jpb) {
        if (jpb == null) {
            return 0;
        }
        return jpb.getMaximum() - jpb.getValue();
    }

    class ProgressListener implements IIOWriteProgressListener {

        public void imageComplete(ImageWriter imageWriter) {
            String string = " Write done. " + remaining(fileJPB) + " picture(s) left.";
            System.out.println(string = imageWriter.getOriginatingProvider() == null ? string : imageWriter.getOriginatingProvider().getFormatNames()[0] + string);
            getTrayIcon().displayMessage("IMC", string, SystemTrayIcon.INFO);
        }

        public void imageStarted(ImageWriter imageWriter, int i) {
            String string = " Write started.";
            System.out.println(string = imageWriter.getOriginatingProvider() == null ? string : imageWriter.getOriginatingProvider().getFormatNames()[0] + string);
            getTrayIcon().displayMessage("IMC", string, SystemTrayIcon.INFO);
        }

        public void imageProgress(ImageWriter imageWriter, float f) {
            String string;
            System.out.print(string = ".");
        }

        public void thumbnailComplete(ImageWriter imageWriter) {
            String string = " Write thumbnail done.";
            System.out.println(string = imageWriter.getOriginatingProvider() == null ? string : imageWriter.getOriginatingProvider().getFormatNames()[0] + string);
            getTrayIcon().displayMessage("IMC", string, SystemTrayIcon.INFO);
        }

        public void thumbnailProgress(ImageWriter imageWriter, float f) {
            String string;
            System.out.print(string = ".");
        }

        public void thumbnailStarted(ImageWriter imageWriter, int i, int i0) {
            String string = " Write started.";
            System.out.println(string = imageWriter.getOriginatingProvider() == null ? string : imageWriter.getOriginatingProvider().getFormatNames()[0] + string);
            getTrayIcon().displayMessage("IMC", string, SystemTrayIcon.INFO);
        }

        public void writeAborted(ImageWriter imageWriter) {
            String string = " Write was aborted.";
            System.out.println(string = imageWriter.getOriginatingProvider() == null ? string : imageWriter.getOriginatingProvider().getFormatNames()[0] + string);
            getTrayIcon().displayMessage("IMC", string, SystemTrayIcon.ERROR);
        }
    }

    class WarningListener implements IIOWriteWarningListener {

        public void warningOccurred(ImageWriter imageWriter, int i, final String string) {
            System.out.println("Error! " + string);
            getTrayIcon().displayMessage("IMC", string, SystemTrayIcon.ERROR);
            if (!ImageMultiConverter_Ant.this.ignoreMsg) {
                new UIMessage(false, string, ImageMultiConverter_Ant.this.owner);
                ImageMultiConverter_Ant.this.showIgnoreDialog();
            }
        }
    }

    class RProgressListener implements IIOReadProgressListener {

        /**
         * Reports that a sequence of read operations is beginning.
         * <code>ImageReader</code> implementations are required to call this
         * method exactly once from their <code>readAll(Iterator)</code> method.
         *
         * @param source the <code>ImageReader</code> object calling this
         * method.
         * @param minIndex the index of the first image to be read.
         */
        public void sequenceStarted(ImageReader source, int minIndex) {
            final String string;
            System.out.print(string = "Read...");
            getTrayIcon().displayMessage("IMC", string, SystemTrayIcon.INFO);
        }

        /**
         * Reports that a sequence of read operationshas completed.
         * <code>ImageReader</code> implementations are required to call this
         * method exactly once from their <code>readAll(Iterator)</code> method.
         *
         * @param source the <code>ImageReader</code> object calling this
         * method.
         */
        public void sequenceComplete(ImageReader source) {
            String string = " Read done.";
            try {
                System.out.println(string = source.getFormatName() + string);
            } catch (IOException ex) {
                ex.printStackTrace();
                getTrayIcon().displayMessage("IMC", string, SystemTrayIcon.INFO);
            }
        }

        /**
         * Reports that an image read operation is beginning. All
         * <code>ImageReader</code> implementations are required to call this
         * method exactly once when beginning an image read operation.
         *
         * @param source the <code>ImageReader</code> object calling this
         * method.
         * @param imageIndex the index of the image being read within its
         * containing input file or stream.
         */
        public void imageStarted(ImageReader source, int imageIndex) {
            try {
                System.out.print("Read... (" + source.getFormatName() + ") " + imageIndex);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        /**
         * Reports the approximate degree of completion of the current
         * <code>read</code> call of the associated <code>ImageReader</code>.
         *
         * <p>
         * The degree of completion is expressed as a percentage varying from
         * <code>0.0F</code> to <code>100.0F</code>. The percentage should
         * ideally be calculated in terms of the remaining time to completion,
         * but it is usually more practical to use a more well-defined metric
         * such as pixels decoded or portion of input stream consumed. In any
         * case, a sequence of calls to this method during a given read
         * operation should supply a monotonically increasing sequence of
         * percentage values. It is not necessary to supply the exact values
         * <code>0</code> and <code>100</code>, as these may be inferred by the
         * callee from other methods.
         *
         * <p>
         * Each particular <code>ImageReader</code> implementation may call this
         * method at whatever frequency it desires. A rule of thumb is to call
         * it around each 5 percent mark.
         *
         * @param source the <code>ImageReader</code> object calling this
         * method.
         * @param percentageDone the approximate percentage of decoding that has
         * been completed.
         */
        public void imageProgress(ImageReader source, float percentageDone) {
            String string;
            System.out.print(string = ".");
        }

        /**
         * Reports that the current image read operation has completed. All
         * <code>ImageReader</code> implementations are required to call this
         * method exactly once upon completion of each image read operation.
         *
         * @param source the <code>ImageReader</code> object calling this
         * method.
         */
        public void imageComplete(ImageReader source) {
            String string = " Read done.";
            try {
                System.out.println(string = source.getFormatName() + string);
            } catch (IOException ex) {
                ex.printStackTrace();
                getTrayIcon().displayMessage("IMC", string, SystemTrayIcon.INFO);
            }
        }

        /**
         * Reports that a thumbnail read operation is beginning. All
         * <code>ImageReader</code> implementations are required to call this
         * method exactly once when beginning a thumbnail read operation.
         *
         * @param source the <code>ImageReader</code> object calling this
         * method.
         * @param imageIndex the index of the image being read within its
         * containing input file or stream.
         * @param thumbnailIndex the index of the thumbnail being read.
         */
        public void thumbnailStarted(ImageReader source,
                int imageIndex, int thumbnailIndex) {
            try {
                System.out.print("Read... (" + source.getFormatName() + ")");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        /**
         * Reports the approximate degree of completion of the current
         * <code>getThumbnail</code> call within the associated
         * <code>ImageReader</code>. The semantics are identical to those of
         * <code>imageProgress</code>.
         *
         * @param source the <code>ImageReader</code> object calling this
         * method.
         * @param percentageDone the approximate percentage of decoding that has
         * been completed.
         */
        public void thumbnailProgress(ImageReader source, float percentageDone) {
            String string;
            System.out.print(string = ".");
        }

        /**
         * Reports that a thumbnail read operation has completed. All
         * <code>ImageReader</code> implementations are required to call this
         * method exactly once upon completion of each thumbnail read operation.
         *
         * @param source the <code>ImageReader</code> object calling this
         * method.
         */
        public void thumbnailComplete(ImageReader source) {
            String string = " Read thumbnail done.";
            try {
                System.out.println(string = source.getFormatName() + string);
            } catch (IOException ex) {
                ex.printStackTrace();
                getTrayIcon().displayMessage("IMC", string, SystemTrayIcon.INFO);
            }

        }

        /**
         * Reports that a read has been aborted via the reader's
         * <code>abort</code> method. No further notifications will be given.
         *
         * @param source the <code>ImageReader</code> object calling this
         * method.
         */
        public void readAborted(ImageReader source) {
            String string = " Read was aborted.";
            try {
                System.out.println(string = source.getFormatName() + string);
            } catch (IOException ex) {
                ex.printStackTrace();
                getTrayIcon().displayMessage("IMC", string, SystemTrayIcon.ERROR);
            }
        }
    }

    class RWarningListener implements IIOReadWarningListener {

        public void warningOccurred(ImageReader source, final String warning) {
            getTrayIcon().displayMessage("IMC", warning, SystemTrayIcon.WARNING);
            System.out.println(warning);

        }
    }
    SystemTrayIcon trayIcon;

    private SystemTrayIcon getTrayIcon() {
        trayIcon.pack();
        return trayIcon;
    }

    public static Display makeDisplay(
            Image image, AffineTransform tx) {
        return (Display) Display._Display(image, tx);
    }

    /**
     *
     */
    public ImageMultiConverter_Ant(String[] args) {
        super();
    }

    public static void main(String[] args) {
        new ImageMultiConverter_Ant(args);
    }

    public JApplet getApplet() {
        return applet;
    }

    Vector<File> importedFiles = new Vector<File>();

    public boolean open(Object data) {
        importedFiles.clear();
        if (data instanceof java.util.List) {
            java.util.List<File> l = Collections.synchronizedList((java.util.List) data);
            Vector<File> files = new Vector<File>();
            synchronized (l) {
                for (Iterator<File> i = l.iterator(); i.hasNext();) {
                    File f = i.next();
                    if (f.isDirectory()) {
                        for (File aFile : f.listFiles()) {
                            if (jfc.getFiles().accept(aFile)) {
                                files.add(aFile);
                            }
                        }
                    } else {
                        if (jfc.getFiles().accept(f)) {
                            files.add(f);
                        }
                    }
                }
            }
            importedFiles.addAll(files);
        } else if (data instanceof String) {
            File f = new File(data.toString());
            if (f.exists()) {
                Vector<File> files = new Vector<File>();
                if (f.isDirectory()) {
                    for (File aFile : f.listFiles()) {
                        if (jfc.getFiles().accept(aFile)) {
                            files.add(aFile);
                        }
                    }
                } else {
                    if (jfc.getFiles().accept(f)) {
                        files.add(f);
                    }
                }
                importedFiles.addAll(files);
            }
        } else if (data instanceof Image) {
            MediaTracker mt = new MediaTracker(this.applet);
            mt.addImage(((Image) data), 0);
            try {
                mt.waitForID(0);
                mt.removeImage((Image) data, 0);
                Dimension size = new Dimension(((Image) data).getWidth(applet), ((Image) data).getHeight(applet));
                Sprite sp = new Sprite(SpriteIO.createBufferedImage(size, BufferedImage.TYPE_4BYTE_ABGR), "image/x-png", size);
                File file = new File(DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date()) + ".png");
                file.deleteOnExit();
                importedFiles.add(file);
                sp.writeImageToOutput(file, 1f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!importedFiles.isEmpty()) {
            jfc.setSelectedFiles(importedFiles.toArray(new File[]{}));
        }
        return !importedFiles.isEmpty();
    }

    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{DataFlavor.imageFlavor, DataFlavor.javaFileListFlavor, DataFlavor.stringFlavor};
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        for (DataFlavor df : getTransferDataFlavors()) {
            if (df.equals(flavor)) {
                return true;
            }
        }
        return false;
    }

    public Object getTransferData(
            DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        return null;
    }

    public boolean save(String file) {
        return false;
    }

    public void stop() {
    }
}
