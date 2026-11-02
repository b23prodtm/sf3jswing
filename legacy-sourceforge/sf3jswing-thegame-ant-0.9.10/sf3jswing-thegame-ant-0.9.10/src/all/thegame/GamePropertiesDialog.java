package thegame;

// Importation des libraires dont on a l'utilisation
import java.awt.*;
import java.awt.RenderingHints.Key;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableCellRenderer;
import net.java.games.input.Component.Identifier;
import net.sf.jiga.xtended.impl.Animation;
import net.sf.jiga.xtended.impl.game.*;
import net.sf.jiga.xtended.impl.game.physics.Player;
import net.sf.jiga.xtended.kernel.JXAenvUtils;
import net.sf.jiga.xtended.kernel.FileHelper;
import net.sf.jiga.xtended.kernel.ThreadWorks;
import net.sf.jiga.xtended.ui.*;
import thegame.comp.Fighter;
import thegame.io.ReadAndSaveSettings;

public class GamePropertiesDialog extends JPanel implements RaSSetListener {

    public static final String SETTINGSPATH = "gameOptions." + GameProperties._MIME_EXTS[0];
    GameProperties props;
    final ReadAndSaveSettings rASSet;
    Container contenu;
    JTabbedPane jtp;
    JPanel panelPhysics;
    JPanel panelBallSettings;
    JPanel panelFieldDimension;
    JPanel panelFighters;
    JPanel panelOverall;
    JPanel panelMoreOptions;
    JPanel panelResources;
    JPanel panelSave;
    JPanel panelBoutonSave;
    Border border;
    TitledBorder titledBorder3;
    TitledBorder titledBorder4;
    TitledBorder titledBorder5;
    JSlider sliderSpeedBall;
    JSlider sliderDEFENSE_MOVE;
    JSlider sliderHIT_MOVE;
    JSpinner spinnerFighterSpeed;
    JSpinner spinnerFighterWeight;
    JSpinner spinnerGravity;
    JComboBox comboScreenResolution;
    /**
     * resources directory (beginning with '/' sets to jar-archive external
     * resource else to in-jar-archive resource)
     */
    InstructionsPane sw_resourcesDir;
    /**
     * rendering options
     */
    Map<RenderingHints.Key, JComboBox> sw_rendering;
    JLabel labelSpeed;
    JLabel labelScreenResolution;
    JLabel labelInfoSave;
    JLabel labelSelectType;
    JButton boutonLoad;
    JButton boutonSave;
    JButton boutonDelete;
    Dimension panelSize;
    final boolean isFullscreen;
    private JCheckBox sw_rendering_debug, sw_rendering_debug_loading_activity, sw_rendering_debug_render, sw_rendering_debug_kernel, sw_rendering_debug_physics, sw_rendering_debug_gui;
    private JCheckBox sw_rendering_lwjgl;
    private JPanel panelJInput;
    javax.swing.Timer[] jinputTimer;
    javax.swing.Timer animT;
    private KeyEventDispatcher ked;

    public GamePropertiesDialog(GameProperties props, boolean fullscreen) throws Exception {
        super(true);
        this.props = props;
        this.rASSet = new ReadAndSaveSettings(FileHelper._USERHOMESTOREDIRECTORY + File.separator + SETTINGSPATH, props);
        rASSet.addRaSSetListener(this);
        isFullscreen = fullscreen;
        rASSet.addRaSSetListener(new RaSSetListener() {

            public void optionsSaved(String date) {
                if (!isFullscreen) {
                    UIMessage.kill(wait);
                }
            }

            public void optionsDeleted() {
            }

            public void optionsLoaded(String date) {
                if (!isFullscreen) {
                    UIMessage.kill(wait);
                }
            }

            public void optionsNotFound() {
                if (!isFullscreen) {
                    UIMessage.kill(wait);
                }
            }

            public void optionsError(Object e) {
                if (!isFullscreen) {
                    UIMessage.kill(wait);
                }
            }

            public void optionsAreSaving(String m) {
                if (!isFullscreen) {
                    wait = UIMessage.displayWaiting(m, GamePropertiesDialog.this);
                }
            }
            private long wait = 0;

            public void optionsAreLoading(String m) {
                if (!isFullscreen) {
                    wait = UIMessage.displayWaiting(m, GamePropertiesDialog.this);
                }
            }
        });

        /*
         * Le container de la frame
         */
        contenu = this;
        contenu.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        /*
         * OPTIONS TABS
         */
        jtp = new JTabbedPane(JTabbedPane.LEFT, JTabbedPane.WRAP_TAB_LAYOUT);

        panelSize = new Dimension(300, 300);
        border = BorderFactory.createEtchedBorder(Color.white, new Color(150, 145, 140));

        /**
         * PHYSICS
         */
        titledBorder3 = new TitledBorder(border, "Field Dimension");
        titledBorder4 = new TitledBorder(border, "Fighters' overall parameters");
        titledBorder5 = new TitledBorder(border, "Speed");
        panelPhysics = new JPanel(new GridBagLayout(), true);

        panelBallSettings = new JPanel(new GridLayout(0, 2, 5, 10), true);
        panelBallSettings.setBorder(titledBorder5);
        c.gridwidth = c.REMAINDER;
        panelPhysics.add(panelBallSettings, c);

        labelSpeed = new JLabel("Speed");
        sliderSpeedBall = new JSlider(JSlider.HORIZONTAL, (int) props.speedBallMin, (int) props.speedBallMax, (int) props.speedBall);

        panelBallSettings.add(labelSpeed);
        panelBallSettings.add(sliderSpeedBall);

        panelFighters = new JPanel(new GridBagLayout(), true);
        panelFighters.setBorder(titledBorder4);
        c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        panelPhysics.add(panelFighters, c);

        JLabel spd, ftr, grv, def, hit;
        panelFighters.add(spd = new JLabel("Overall speed"));
        panelFighters.add(ftr = new JLabel("Overall weight"));
        panelFighters.add(grv = new JLabel("Overall gravity"));
        sliderDEFENSE_MOVE = new JSlider(JSlider.HORIZONTAL, 0, 100, props.DEFENSE_MOVE);
        sliderHIT_MOVE = new JSlider(JSlider.HORIZONTAL, 0, 200, props.HIT_MOVE);
        spinnerFighterSpeed = new JSpinner(new SpinnerNumberModel(props.fighterSpeed, props.MIN, props.MAX, 1));
        spinnerFighterWeight = new JSpinner(new SpinnerNumberModel(props.fighterWeight, props.MIN, props.MAX, 1));
        spinnerGravity = new JSpinner(new SpinnerNumberModel(props.gravity, props.MIN, props.MAX, 1));

        panelFighters.add(spinnerFighterSpeed, c);
        panelFighters.add(spinnerFighterWeight, c);
        panelFighters.add(spinnerGravity, c);
        panelFighters.add(def = new JLabel("Overall defense move length"), c);
        panelFighters.add(sliderDEFENSE_MOVE, c);
        panelFighters.add(hit = new JLabel("Overall hit move length"), c);
        panelFighters.add(sliderHIT_MOVE, c);


        panelFieldDimension = new JPanel(new GridBagLayout(), true);
        panelFieldDimension.setBorder(titledBorder3);
        c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        panelPhysics.add(panelFieldDimension, c);
        labelScreenResolution = new JLabel("Screen Resolution");

        comboScreenResolution = new JComboBox(FieldGui.dm.toArray());
        panelFieldDimension.add(labelScreenResolution, c);
        panelFieldDimension.add(comboScreenResolution, c);

        /**
         * JInput
         */
        panelJInput = new JPanel(new GridBagLayout(), true);
        loadJinput();
        /**
         * ADVANCED
         */
        panelMoreOptions = new JPanel(new GridBagLayout(), true);

        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;
        panelMoreOptions.add(new JLabel("Graphics optimization : ", UIMessage._getIcon(System.getProperty("os.name").startsWith("Windows") ? UIMessage.DIRECTX_TYPE : UIMessage.OPENGL_TYPE, true), SwingConstants.TRAILING), c);
        Dimension animDim;
        final Animation anim = RenderingScene._getLogoAnimation(this, "/net/sf/jiga/xtended/impl/opengl", 0, 29, "", ".png", animDim = new Dimension(64, 30));
        JComponent renderSettingsPreview = anim._makeAnimationJComponent(anim);
        renderSettingsPreview.setPreferredSize(new Dimension(animDim.width * 2, animDim.height * 2));

        animT = new javax.swing.Timer(100, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                anim.play();
            }
        });
        animT.start();
        sw_rendering = Collections.synchronizedMap(new HashMap<RenderingHints.Key, JComboBox>(GameProperties.renderings.size()));
        synchronized (GameProperties.renderings) {
            for (RenderingHints r : GameProperties.renderings.values()) {
                RenderingHints.Key k = (Key) r.keySet().iterator().next();
                if (sw_rendering.containsKey(k)) {
                    sw_rendering.get(k).addItem(GameProperties.renderingsStrings.get(r.toString()));
                } else {
                    JComboBox box;
                    sw_rendering.put(k, box = new JComboBox(new Object[]{GameProperties.renderingsStrings.get(r.toString())}));
                    panelMoreOptions.add(box, c);
                }
            }
        }
        c.gridwidth = c.RELATIVE;
        panelMoreOptions.add(new JLabel(UIMessage._getIcon(UIMessage.LWJGL_TYPE, true)), c);
        c.gridwidth = c.REMAINDER;
        panelMoreOptions.add(sw_rendering_lwjgl = new JCheckBox("LWJGL API"), c);
        sw_rendering_lwjgl.setEnabled(false);
        panelMoreOptions.add(sw_rendering_debug_gui = new JCheckBox("GUI debug mode"), c);
        panelMoreOptions.add(sw_rendering_debug_loading_activity = new JCheckBox("Loading activity debug mode"), c);
        panelMoreOptions.add(sw_rendering_debug_render = new JCheckBox("Render debug mode"), c);
        panelMoreOptions.add(sw_rendering_debug_physics = new JCheckBox("Physics debug mode"), c);
        panelMoreOptions.add(sw_rendering_debug = new JCheckBox("JXA debug mode"), c);
        panelMoreOptions.add(sw_rendering_debug_kernel = new JCheckBox("JXA Sys-Kernel debug mode"), c);
        c.weightx = 0;
        panelMoreOptions.add(renderSettingsPreview, c);

        /**
         * SAVE LOAD
         */
        panelSave = new JPanel(new GridBagLayout(), true);
        panelSave.setBorder(new TitledBorder(border, "Options Data file"));
        boutonLoad = new JButton("Load", UIMessage._getIcon(UIMessage.LOAD_TYPE, true));
        boutonSave = new JButton("Save", UIMessage._getIcon(UIMessage.STORE_TYPE, true));
        boutonDelete = new JButton("Delete", UIMessage._getIcon(UIMessage.DELETE_TYPE, true));
        boutonDelete.setEnabled(!isFullscreen);
        boutonLoad.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                propsDialog.doLater(new Runnable() {

                    public void run() {
                        GameProperties newProps = readGameProperties();
                        if (newProps instanceof GameProperties) {
                            GamePropertiesDialog.this.props = newProps;
                            loadOptions();
                        }
                    }
                });
            }
        });

        boutonSave.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                propsDialog.doLater(new Runnable() {

                    public void run() {
                        saveOptions();
                        writeGameProperties();
                    }
                });
            }
        });

        boutonDelete.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                GamePropertiesDialog.this.rASSet.deleteFile();
            }
        });

        labelInfoSave = new JLabel("[option file]", 0);
        labelInfoSave.setIcon(UIMessage._getIcon(UIMessage.TEMPLATE_TYPE, true));
        labelInfoSave.setFont(new java.awt.Font("Dialog", 0, 9));
        labelInfoSave.setForeground(Color.red); // couleur du texte

        panelSave.add(labelInfoSave);
        c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        ((GridBagLayout) panelSave.getLayout()).setConstraints(labelInfoSave, c);
        panelSave.add(boutonLoad);
        panelSave.add(boutonSave);
        ((GridBagLayout) panelSave.getLayout()).setConstraints(boutonSave, c);
        panelSave.add(boutonDelete);

        /**
         * PATHS
         */
        panelResources = new JPanel(new BorderLayout(), true);
        panelResources.setBorder(new TitledBorder(border, "Resources folders :"));

        InstructionsPane ipane = sw_resourcesDir = new InstructionsPane(jtp, 0, 3);
        ipane.setPreferredSize(panelSize);
        final JLabel playerModelsFolder = fullscreen ? switchToWMLabel : new JLabel("Locate the players' models folder !");
        final FileChooserPane fcp = new FileChooserPane(this, "");
        fcp.setModeOpen(true);
        fcp.setMultiSelectionEnabled(true);
        fcp.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int step = fullscreen ? ipane.addStep() : ipane.addInstructionSet(fcp);
        ipane.addStepComponent(step, playerModelsFolder, 1, 2);
        if (!fullscreen) {
            ipane.addStepComponent(step, new JButton(new AbstractAction("load the selected players folder(s) !", UIMessage._getIcon(UIMessage.TEMPLATE_TYPE, false)) {

                /*
                 * Thread t = null;
                 */
                public void actionPerformed(ActionEvent e) {
                    /*
                     * if (t instanceof Thread) { if (t.isAlive() &&
                     * !t.isInterrupted()) { return; } } t = new Thread(
                     */
                    Runnable r = new Runnable() {

                        public void run() {
                            Vector<File> vf = new Vector<File>();
                            if (fcp.getSelectedFiles() == null) {
                                FieldGui.clearPlayerModelsStack();
                            } else {
                                for (File directory : fcp.getSelectedFiles()) {
                                    vf.addAll(findModelsInPath(directory, XtendedModel._MIME_EXT));
                                }
                                playerModelsFolder.setText("Found " + vf.size() + " models for players.");
                                FieldGui.clearPlayerModelsStack();
                                for (File path : vf) {
                                    XtendedModel model = (XtendedModel) InteractiveModel._importModel(path.getPath());
                                    String map = null;
                                    while (new String("").equals(map) || map == null) {
                                        map = (String) UIMessage.showSelectDialog(GamePropertiesDialog.this, "What map does this Model use ? ", "Choose a map !", new String[]{Fighter.SUPERMOVEST1, Fighter.SUPERMOVEST2}, map);
                                    }
                                    model.setAttribute("eventMapTYPE", map);
                                    FieldGui.addPlayerModel(model);
                                }
                            }
                            /*
                             * Thread.currentThread().interrupt();
                             */
                        }
                    };/*
                     * , "t-load models"); t.start();
                    status.addActiveThread(t);
                     */
                    propsDialog.doLater(r);
                }
            }), 1, 2);
        }
        final JLabel backgroundModelsFolder = fullscreen ? switchToWMLabel : new JLabel("Locate the backgrounds' models folder(s) !");
        final FileChooserPane fcp2 = new FileChooserPane(this, "");
        fcp2.setModeOpen(true);
        fcp2.setMultiSelectionEnabled(true);
        fcp2.getFiles().addChoosableFileFilter(new ExampleFileFilter(XtendedModel._MIME_EXT, XtendedModel._dataFlavor.getHumanPresentableName()));
        if (!fullscreen) {
            step = ipane.addInstructionSet(fcp2);
        }
        ipane.addStepComponent(step, backgroundModelsFolder, 1, 2);
        tableOfBackgroundsModelsStep = ipane.addStep();
        tableOfBackgroundsModelsIndex = ipane.addStepComponent(tableOfBackgroundsModelsStep, new JScrollPane(new JTable()), 1, 1, 1, 1);
        if (!fullscreen) {
            ipane.addStepComponent(step, new JButton(new AbstractAction("load the selected backgrounds folder(s) !", UIMessage._getIcon(UIMessage.TEMPLATE_TYPE, false)) {

                /*
                 * Thread t = null;
                 */
                public void actionPerformed(ActionEvent e) {
                    /*
                     * if (t instanceof Thread) { if (t.isAlive() &&
                     * !t.isInterrupted()) { return; } } t = new Thread(
                     */
                    Runnable r = new Runnable() {

                        public void run() {
                            Vector<File> vf = new Vector<File>();
                            if (fcp2.getSelectedFiles() == null) {
                                FieldGui.clearBackgroundModelsStack();
                            } else {
                                for (File directory : fcp2.getSelectedFiles()) {
                                    vf.addAll(findModelsInPath(directory, XtendedModel._MIME_EXT));
                                }
                                backgroundModelsFolder.setText("Found " + vf.size() + " models for backgrounds.");
                                FieldGui.clearBackgroundModelsStack();
                                for (int i = 0; i < vf.size(); i++) {
                                    FieldGui.addBackgroundModel(FieldGui.SCREEN_DEFAULT, (XtendedModel) Model._importModel(vf.get(i).getPath()));
                                }
                            }
                            updateTableOfBackgroundsModels(tableOfBackgroundsModelsStep, tableOfBackgroundsModelsIndex);
                            /*
                             * Thread.currentThread().interrupt();
                             */
                        }
                    };/*
                     * , "t-load models"); t.start();
                    status.addActiveThread(t);
                     */
                    propsDialog.doLater(r);
                }
            }), 1, 2);
        }
        panelResources.add(sw_resourcesDir, BorderLayout.CENTER);
        panelResources.add(status = new StatusPane(ipane, new AbstractAction("Store !", UIMessage._getIcon(UIMessage.REFRESH_TYPE, true)) {

            public void actionPerformed(ActionEvent e) {
                propsDialog.doLater(new Runnable() {

                    public void run() {
                        saveOptions();
                    }
                });
            }
        }, new AbstractAction("Try reloading !") {

            public void actionPerformed(ActionEvent e) {
                propsDialog.doLater(new Runnable() {

                    public void run() {
                        loadOptions();
                    }
                });
            }
        }), BorderLayout.SOUTH);

        /*
         * Creation des onglets JTabbedPane
         */
        jtp.addTab("Physics Settings", UIMessage._getIcon(UIMessage.INPUT_TYPE, true), panelPhysics, "Change game speed, difficulty, ... !");
        JScrollPane sc = new JScrollPane(panelJInput);
        sc.getHorizontalScrollBar().setUnitIncrement(30);
        sc.getVerticalScrollBar().setUnitIncrement(30);
        sc.setPreferredSize(new Dimension(500, 300));
        jtp.addTab("Keyboard & Pads", UIMessage._getIcon(UIMessage.LWJGL_TYPE, true), sc, "Configure keyboard and pads !");
        jtp.addTab("Advanced...", UIMessage._getIcon(UIMessage.OPENGL_TYPE, true), panelMoreOptions, "Change game graphics, sound, ... !");
        jtp.addTab("Paths", UIMessage._getIcon(UIMessage.SEARCH_TYPE, true), panelResources, "Add more characters and stages to the game !");
        jtp.setSelectedIndex(0);
        if (fullscreen) {
            panelResources.setEnabled(false);
        }
        c = new GridBagConstraints();
        c.gridwidth = c.REMAINDER;
        c.fill = c.BOTH;
        c.weightx = c.weighty = 1;
        contenu.add(jtp, c);
        c.weighty = .5;
        contenu.add(panelSave, c);
        validate();
        loadOptions();

    }
    StatusPane status = null;
    final int tableOfBackgroundsModelsStep;
    final int tableOfBackgroundsModelsIndex;
    private JLabel switchToWMLabel = new JLabel("Please, switch back (F4) to windowed mode to have access to this panel !");
    ThreadWorks propsDialog = new ThreadWorks("propDialog");

    private Vector<File> findModelsInPath(File path, String[] modelext) {
        Vector<File> files = new Vector<File>();
        Stack<File> dirs = new Stack<File>();
        dirs.push(path);
        while (dirs.empty() ? false : dirs.peek().isDirectory()) {
            for (File file : dirs.pop().listFiles()) {
                if (file.isDirectory()) {
                    dirs.push(file);
                } else {
                    for (String ext : modelext) {
                        if (file.getName().endsWith(ext)) {
                            files.add(file);
                            break;
                        }
                    }
                }
            }
        }
        return files;

    }

    private void loadJinput() {
        panelJInput.removeAll();
        panelJInput.validate();
        int padNum = 0;
        synchronized (Sf3Player._availableGamepads) {
            if (jinputTimer != null) {
                for (javax.swing.Timer t : jinputTimer) {
                    t.stop();
                }
            }
            jinputTimer = new javax.swing.Timer[Sf3Player._availableGamepads.size()];
            for (final net.java.games.input.Controller pad : Sf3Player._availableGamepads.values()) {
                Vector<Vector> jinput = new Vector<Vector>();
                final SortedMap<Player.key, Identifier> codeMap = props.gamepadCodeMap;
                synchronized (codeMap) {
                    for (final Player.key action : codeMap.keySet()) {
                        Vector buttonCfg = new Vector();
                        buttonCfg.add(action);
                        buttonCfg.add(codeMap.get(action));
                        jinput.add(buttonCfg);
                    }
                }
                Vector<String> cols = new Vector();
                cols.add("action");
                cols.add("button");
                GridBagConstraints cs = new GridBagConstraints();
                cs.gridwidth = cs.REMAINDER;
                cs.fill = cs.BOTH;
                cs.weightx = 1;
                cs.weighty = .1;
                panelJInput.add(new JLabel("[ " + padNum + " ] " + pad.getName()), cs);
                cs.weighty = 1;
                final JTable table = new JTable(jinput, cols);
                panelJInput.add(table, cs);
                String[] buttons = new String[pad.getComponents().length];
                for (int i = 0; i < pad.getComponents().length; i++) {
                    buttons[i] = pad.getComponents()[i].getIdentifier().getName();
                }
                final JComboBox mapping = new JComboBox(buttons);
                final ActionListener mappingAct;
                mapping.addActionListener(mappingAct = new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        String action = table.getValueAt(table.getSelectedRow(), 0).toString();
                        String id = table.getValueAt(table.getSelectedRow(), 1).toString();
                        for (Identifier i : Player._gamepadCodeMap.values()) {
                            if (i.getName().equals(id)) {
                                props.gamepadCodeMap.put(Player.key.valueOf(action), i);
                                break;
                            }
                        }
                        System.out.println("mapped " + action + " to " + id + " of controller " + pad.getName());
                    }
                });
                table.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(mapping));
                jinputTimer[padNum] = new javax.swing.Timer(10, new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        pad.poll();
                        net.java.games.input.EventQueue eq = pad.getEventQueue();
                        net.java.games.input.Event ev = new net.java.games.input.Event();
                        while (eq.getNextEvent(ev)) {
                            if (table.getSelectedRow() != -1) {
                                String id = ev.getComponent().getIdentifier().getName();
                                table.setValueAt(id, table.getSelectedRow(), 1);
                                mappingAct.actionPerformed(e);
                            }
                        }
                    }
                });
                jinputTimer[padNum].start();
                padNum++;
            }
        }
        /**
         * keyboard
         */
        Vector<Vector> jinput = new Vector<Vector>();
        final SortedMap<Player.key, Integer> codeMap = new TreeMap<Player.key, Integer>(props.keyboardCodeMap);
        synchronized (codeMap) {
            for (final Player.key action : codeMap.keySet()) {
                Vector buttonCfg = new Vector();
                buttonCfg.add(action);
                buttonCfg.add(codeMap.get(action));
                jinput.add(buttonCfg);
            }
        }
        Vector<String> cols = new Vector();
        cols.add("action");
        cols.add("keystroke");
        GridBagConstraints cs = new GridBagConstraints();
        cs.gridwidth = cs.REMAINDER;
        cs.fill = cs.BOTH;
        cs.weightx = 1;
        cs.weighty = .1;
        panelJInput.add(new JLabel("keyboard"), cs);
        cs.weighty = 1;
        final JTable table = new JTable(jinput, cols);
        panelJInput.add(table, cs);
        final JTextField mapping = new JTextField("key stroke");
        final ActionListener mappingAct;
        mapping.addActionListener(mappingAct = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String action = table.getValueAt(table.getSelectedRow(), 0).toString();
                int id = Integer.parseInt(table.getValueAt(table.getSelectedRow(), 1).toString());
                props.keyboardCodeMap.put(Player.key.valueOf(action), id);
                System.out.println("mapped " + action + " to " + KeyEvent.getKeyText(id));
            }
        });
        table.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(mapping));
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(ked);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(ked = new KeyEventDispatcher() {

            long hash = System.nanoTime();

            @Override
            public boolean equals(Object obj) {
                return obj == null ? false : hash == obj.hashCode();
            }

            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getID() == KeyEvent.KEY_PRESSED) {
                    if (table.getSelectedRow() != -1) {
                        int id = e.getKeyCode();
                        table.setValueAt(id, table.getSelectedRow(), 1);
                        mappingAct.actionPerformed(new ActionEvent(this, e.getID(), e.getKeyText(e.getKeyCode())));
                        return true;
                    }
                }
                return false;
            }
        });
        table.getColumnModel().getColumn(1).setCellRenderer(new TableCellRenderer() {

            JLabel label = new JLabel();

            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                label.setText(KeyEvent.getKeyText(Integer.parseInt(value.toString())));
                return label;
            }
        });
        panelJInput.validate();
    }

    private JTable updateTableOfBackgroundsModels(
            final int iPaneStep, final int iPaneIndex) {
        Vector<Vector> rows = new Vector<Vector>();
        final Vector<XtendedModel> MODELSTACK = new Vector<XtendedModel>();
        synchronized (FieldGui.getBackgroundModelsStack()) {
            for (Iterator<Map.Entry<String, XtendedModel[]>> it = FieldGui.getBackgroundModelsStack().entrySet().iterator(); it.hasNext();) {
                final Map.Entry<String, XtendedModel[]> s = it.next();
                int i = 0;
                for (XtendedModel m : s.getValue()) {
                    if (m instanceof XtendedModel) {
                        Vector v = new Vector();
                        v.add(new File(m.id + File.separator + m.prefix + "xx" + m.suffix).getName());
                        if (FieldGui.screenState.containsValue(s.getKey())) {
                            v.add(s.getKey());
                        } else {
                            v.add(FieldGui.SCREEN_DEFAULT);
                        }
                        v.add(i);
                        MODELSTACK.add(m);
                        rows.add(v);
                    }
                    i++;
                }
            }
        }
        Vector v = new Vector();
        final int VPATHcolumn = 0;
        final int SCREENNAMEcolumn = 1;
        final int LAYERcolumn = 2;
        v.add("background model v-path");
        v.add("screen name");
        v.add("layer (0 : roof, 1 : floor, 2+ : center back to front)");
        JScrollPane tableOfModels = (JScrollPane) sw_resourcesDir.getStepComponent(iPaneStep, iPaneIndex)[0];
        tableOfModels.getHorizontalScrollBar().setUnitIncrement(30);
        tableOfModels.getVerticalScrollBar().setUnitIncrement(30);
        final JTable table = new JTable(rows, v);
        final Object[][] lastValue = new Object[table.getRowCount()][table.getColumnCount()];
        for (int i = 0;
                i < table.getRowCount(); i++) {
            for (int j = 0; j < table.getColumnCount(); j++) {
                lastValue[i][j] = table.getValueAt(i, j);
            }
        }
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JComboBox layersComboBox = new JComboBox(new Integer[]{0, 1, 2, 3, 4, 5});
        layersComboBox.addActionListener(new ActionListener() {

            private void refreshTable() {
                for (int i = 0; i < table.getRowCount(); i++) {
                    for (int j = 0; j < table.getColumnCount(); j++) {
                        table.setValueAt(lastValue[i][j], i, j);
                    }
                }
            }

            public void actionPerformed(ActionEvent e) {
                if (table.getSelectedRow() == -1) {
                    refreshTable();
                    return;
                }
                XtendedModel selectedModel = MODELSTACK.get(table.getSelectedRow());
                if (selectedModel instanceof XtendedModel) {
                    /**
                     * previous screen background layer selection
                     */
                    FieldGui.removeBackgroundModel((String) table.getValueAt(table.getSelectedRow(), SCREENNAMEcolumn), selectedModel);
                    /**
                     * new screen background layer selection
                     */
                    /**
                     * 
                     */
                    XtendedModel replace = selectedModel;
                    int replaceIndex = (Integer) table.getValueAt(table.getSelectedRow(), LAYERcolumn);
                    while (replace instanceof XtendedModel) {
                        replace = FieldGui.addBackgroundModel((String) table.getValueAt(table.getSelectedRow(), SCREENNAMEcolumn), replace, replaceIndex++);
                    }
                    /**
                     * later or infinite loop ?
                     */
                    ThreadWorks.Swing.invokeLater(new Runnable() {

                        public void run() {
                            updateTableOfBackgroundsModels(iPaneStep, iPaneIndex);
                        }
                    });
                }
            }
        });
        table.getColumnModel().getColumn(LAYERcolumn).setCellEditor(new DefaultCellEditor(layersComboBox));
        TableCellRenderer modelRdr = new TableCellRenderer() {

            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Object _icon = MODELSTACK.get(row).getAttribute("icon");
                /*
                 * if (_icon instanceof Sprite) { // TODO: check display size
                 * Sprite icon = (Sprite) _icon; return
                 * icon._makeSpriteJComponent(icon); } else { *
                 */
                return new JLabel(value.toString());
                /*
                 * }
                 */
            }
        };

        table.getColumnModel().getColumn(VPATHcolumn).setCellRenderer(modelRdr);
        Vector<String> backgrounds = new Vector<String>();
        for (String screen : FieldGui.screenState.values()) {
            if (!backgrounds.contains(screen)) {
                backgrounds.add(screen);
            }
        }
        final JComboBox screenNameComboBox = new JComboBox(backgrounds);
        screenNameComboBox.addActionListener(
                new ActionListener() {

                    private void refreshTable() {
                        for (int i = 0; i < table.getRowCount(); i++) {
                            for (int j = 0; j < table.getColumnCount(); j++) {
                                table.setValueAt(lastValue[i][j], i, j);
                            }
                        }
                    }

                    public void actionPerformed(ActionEvent e) {
                        if (table.getSelectedRow() == -1) {
                            refreshTable();
                            return;
                        }

                        XtendedModel selectedModel = MODELSTACK.get(table.getSelectedRow());
                        if (selectedModel instanceof XtendedModel) {
                            /**
                             * previous screen background layer group selection
                             */
                            FieldGui.removeBackgroundModel((String) lastValue[table.getSelectedRow()][SCREENNAMEcolumn], selectedModel);
                            /**
                             * new screen background layer group selection
                             */
                            XtendedModel replace = selectedModel;
                            int replaceIndex = (Integer) table.getValueAt(table.getSelectedRow(), LAYERcolumn);
                            while (replace instanceof XtendedModel) {
                                replace = FieldGui.addBackgroundModel((String) table.getValueAt(table.getSelectedRow(), SCREENNAMEcolumn), replace, replaceIndex++);
                            }
                            /**
                             * later or infinite loop ?
                             */
                            ThreadWorks.Swing.invokeLater(new Runnable() {

                                public void run() {
                                    updateTableOfBackgroundsModels(iPaneStep, iPaneIndex);
                                }
                            });
                        }
                    }
                });
        ;
        table.getColumnModel().getColumn(SCREENNAMEcolumn).setCellEditor(new DefaultCellEditor(screenNameComboBox));
        tableOfModels.setPreferredSize(panelSize);
        table.setPreferredSize(panelSize);
        tableOfModels.setViewportView(table);
        sw_resourcesDir.validate();
        sw_resourcesDir.repaint();
        return table;
    }

    public int getButtonGroupCheck(ButtonGroup bgrp, AbstractButton[] bm) {
        int i = 0;
        while (i < bgrp.getButtonCount()) {
            if (bgrp.isSelected(bm[i].getModel())) {
                break;
            } else {
                i++;
            }
        }
        return i >= bgrp.getButtonCount() ? 0 : i;
    }

    private void setButtonGroupCheck(ButtonGroup bgrp, AbstractButton[] bm, String check) {
        for (int i = 0;
                i < bm.length; i++) {
            if (check.equals(bm[i].getText())) {
                bgrp.setSelected((ButtonModel) bm[i].getModel(), true);
            }
        }
    }

    private void setCurrentSettings() {
        comboScreenResolution.setSelectedItem(props.dm);
        spinnerFighterSpeed.setValue(new Integer(props.fighterSpeed));
        spinnerFighterWeight.setValue(new Integer(props.fighterWeight));
        spinnerGravity.setValue(new Integer(props.gravity));
        sliderDEFENSE_MOVE.setValue(props.DEFENSE_MOVE);
        sliderHIT_MOVE.setValue(props.HIT_MOVE);
        sliderSpeedBall.setValue((int) props.speedBall);
        synchronized (sw_rendering) {
            for (RenderingHints.Key key : sw_rendering.keySet()) {
                for (Object newKey : props.rendering.keySet()) {
                    if (key.equals(newKey)) {
                        RenderingHints rh = new RenderingHints((Key) newKey, props.rendering.get(newKey));
                        sw_rendering.get(key).setSelectedItem(GameProperties.renderingsStrings.get(rh.toString()));
                    }
                }
            }
        }
        sw_rendering_debug_gui.setSelected((props.getDebug() & props._DEBUG_GUI) != 0);
        sw_rendering_debug_loading_activity.setSelected((props.getDebug() & props._DEBUG_LOADING_ACTIVITY) != 0);
        sw_rendering_debug_render.setSelected((props.getDebug() & props._DEBUG_RENDER) != 0);
        sw_rendering_debug_physics.setSelected((props.getDebug() & props._DEBUG_PHYSICS) != 0);
        sw_rendering_debug.setSelected((props.getDebug() & props._DEBUG_JXA) != 0);
        sw_rendering_debug_kernel.setSelected((props.getDebug() & props._DEBUG_JXA_KERNEL) != 0);
        sw_rendering_lwjgl.setSelected(props.lwjglEnabled);
        repaint();
    }

    private void setSettingsToCurrentValues() {
        props.dm = (DisplayModeWrapper) comboScreenResolution.getSelectedItem();
        props.fighterSpeed = ((SpinnerNumberModel) spinnerFighterSpeed.getModel()).getNumber().intValue();
        props.fighterWeight = ((SpinnerNumberModel) spinnerFighterWeight.getModel()).getNumber().intValue();
        props.gravity = ((SpinnerNumberModel) spinnerGravity.getModel()).getNumber().intValue();
        props.DEFENSE_MOVE = sliderDEFENSE_MOVE.getValue();
        props.HIT_MOVE = sliderHIT_MOVE.getValue();
        props.speedBall = sliderSpeedBall.getValue();
        props.rendering.clear();
        synchronized (sw_rendering) {
            for (RenderingHints.Key key : sw_rendering.keySet()) {
                String selectedRendering = (String) sw_rendering.get(key).getSelectedItem();
                synchronized (GameProperties.renderingsStrings) {
                    for (Iterator<String> it = GameProperties.renderingsStrings.keySet().iterator(); it.hasNext();) {
                        String myKey = it.next();
                        if (GameProperties.renderingsStrings.get(myKey).equals(selectedRendering)) {
                            props.rendering.add(GameProperties.renderings.get(myKey));
                        }
                    }
                }
            }
        }
        props.setDebug((sw_rendering_debug.isSelected() ? props._DEBUG_JXA : 0) | (sw_rendering_debug_render.isSelected() ? props._DEBUG_RENDER : 0) | (sw_rendering_debug_loading_activity.isSelected() ? props._DEBUG_LOADING_ACTIVITY : 0) | (sw_rendering_debug_kernel.isSelected() ? props._DEBUG_JXA_KERNEL : 0) | (sw_rendering_debug_physics.isSelected() ? props._DEBUG_PHYSICS : 0) | (sw_rendering_debug_gui.isSelected() ? props._DEBUG_GUI : 0));
        props.lwjglEnabled = sw_rendering_lwjgl.isSelected();
    }

    public GameProperties readGameProperties() {
        return rASSet.loadOptions(ll, prg);
    }

    public void writeGameProperties() {
        rASSet.saveOptions();
    }

    public void loadOptions() {
        setCurrentSettings();
        synchronized (props.bgModels) {
            FieldGui.clearBackgroundModelsStack();
            for (Iterator<Map.Entry<String, XtendedModel[]>> it = props.bgModels.entrySet().iterator(); it.hasNext();) {
                final Map.Entry<String, XtendedModel[]> s = it.next();
                FieldGui.setBackgroundModels(s.getKey(), s.getValue());
            }
        }
        synchronized (props.characters) {
            FieldGui.clearPlayerModelsStack();
            for (XtendedModel model : props.characters) {
                FieldGui.addPlayerModel(model);
            }
        }
        ThreadWorks.Swing.invokeLater(new Runnable() {

            public void run() {
                updateTableOfBackgroundsModels(tableOfBackgroundsModelsStep, tableOfBackgroundsModelsIndex);
                loadJinput();
            }
        });
    }
    private CacheLoadListener ll = new CacheLoadListener() {

        @Override
        public void readCompleted() {
            super.readCompleted();
            labelIOmessage("deploying models caches " + UIMessage.getProgressBar(prg).getString() + "...");
        }

        public void loadBegin() {
            super.loadBegin();
            labelIOmessage("deploying models caches " + UIMessage.getProgressBar(prg).getString() + "...");
        }

        public void loadIsComplete() {
            super.loadIsComplete();
            labelIOmessage("deployed models caches.");
            boutonSave.setEnabled(true);
            boutonLoad.setEnabled(true);
            boutonDelete.setEnabled(true);
        }
    };
    long prg = UIMessage.newProgress(0, 100, panelSave);

    private void deployCaches() {
        props.deployContentsCaches(ll, prg);
    }

    public void saveOptions() {
        boutonSave.setEnabled(false);
        boutonLoad.setEnabled(false);
        boutonDelete.setEnabled(false);
        setSettingsToCurrentValues();
        props.bgModels = Collections.synchronizedMap(new HashMap<String, XtendedModel[]>());
        props.characters = Collections.synchronizedList(new Vector<XtendedModel>());
        props.bgModels.putAll(FieldGui.getBackgroundModelsStack());
        props.characters.addAll(FieldGui.getPlayerModelsStack());
        ThreadWorks.Swing.invokeLater(new Runnable() {

            public void run() {
                updateTableOfBackgroundsModels(tableOfBackgroundsModelsStep, tableOfBackgroundsModelsIndex);
            }
        });
        deployCaches();
    }

    public void optionsSaved(String date) {
        boutonDelete.setEnabled(!isFullscreen);
        labelIOmessage("Options succesfully saved! (" + date + ")");
    }

    public void optionsLoaded(String date) {
        boutonDelete.setEnabled(!isFullscreen);
        setCurrentSettings();
        labelIOmessage("Options loaded. Saved at " + date + ".");
        repaint();
    }

    public void optionsNotFound() {
        boutonDelete.setEnabled(false);
        labelInfoSave.setText("Data not found");
    }

    public void optionsDeleted() {
        boutonDelete.setEnabled(false);
        labelIOmessage("Saved options deleted");

    }

    /**
     * Returns the properties changed by the user; return null if the user
     * cancelled the dialog, otherwise it returns the props set by the user
     */
    public GameProperties getResult() {
        saveOptions();
        return props;
    }

    public void optionsError(Object e) {
        labelIOmessage("An error occurred with the options file, please check your settings ! caught error : " + e.toString());
        repaint();
    }

    public void optionsAreSaving(String m) {
        labelIOmessage(m);
    }

    public void optionsAreLoading(String m) {
        labelIOmessage(m);
    }

    public void labelIOmessage(String m) {
        labelInfoSave.setText(m);
    }

    /**
     * releases all resources
     */
    public void dispose() {
        for (javax.swing.Timer t : jinputTimer) {
            t.stop();
        }
        animT.stop();
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(ked);
    }
}
