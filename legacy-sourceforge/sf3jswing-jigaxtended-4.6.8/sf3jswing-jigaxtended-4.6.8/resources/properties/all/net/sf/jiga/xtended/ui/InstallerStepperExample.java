package all.net.sf.jiga.xtended.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import net.sf.jiga.xtended.ui.FileChooserPane;
import net.sf.jiga.xtended.ui.InstructionsPane;
import net.sf.jiga.xtended.ui.StatusPane;
/* The JXA kernel AntApplet may be used as interface instead of a full JApplet
     *
     * class InstallerStepperExample implements AntApplet {
     *
     */
class InstallerStepperExample extends JApplet { 
    

    private JButton sw_selectFormat, sw_convert;
    private String selectedConversion = "";
    private JSpinner sw_zoom;
    private JSpinner sw_compression_quality;
    private int compression_quality = 80;
    private boolean blackAndWhiteEnabled;
    private HashMap<Integer, JCheckBox> sw_color_option;

    private String getSelectedConversion() {
        return selectedConversion;
    }

    @Override
    public void init() {
        Container content = getContentPane();
        final InstructionsPane pane = new InstructionsPane(this, 0, 5);
        StatusPane toggleStep = new StatusPane(pane);
        content.add(pane, BorderLayout.CENTER);
        content.add(toggleStep, BorderLayout.SOUTH);

// add one new step in the panel store
        int step = pane.addStep();

// add one Text Area
        pane.addStepComponent(step, new JTextArea("Go to next step and select the images files you want to convert."));

// add another step in the panel store
        step = pane.addStep();
        Container owner = this.getContentPane();

// here is the file chooser panel as an InstructionSet
        final FileChooserPane jfc = new FileChooserPane(owner, "");
        jfc.setModeOpen(true);

// add the file chooser panel set to the InstructionsPane at the next available empty step
        pane.addInstructionSet(jfc);

// here is how to add button with action
        pane.addStepComponent(step, sw_selectFormat = new JButton("Conversion format..."));
        pane.addStepComponent(step, sw_convert = new JButton("Convert"));
// add one action listener to the button
// you can access the class- , static- and final- local variables
        sw_selectFormat.addActionListener(new ActionListener() {
                    @Override
            public void actionPerformed(ActionEvent e) {
                selectedConversion = JOptionPane.showInputDialog(InstallerStepperExample.this,
                        "What conversion type do you want?", "Convert...",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        ImageIO.getWriterMIMETypes(),
                        getSelectedConversion()).toString();
                sw_convert.setText("Convert to " + getSelectedConversion());
            }
        });

// add one more step with 2 columns of components for the layout
        step = pane.addStep(2);

// each added component is given with row column span as arguments
        pane.addStepComponent(step, new JLabel("This value will zoom the image to choosen scale (%):"), 1, 2);
        pane.addStepComponent(step, sw_zoom = new JSpinner(new SpinnerNumberModel(100, 1, 500, 1)), 1, 2);

// (É)
        pane.addStepComponent(step, new JLabel("compression quality (JPEG only)"));
        pane.addStepComponent(step, sw_compression_quality = new JSpinner(new SpinnerNumberModel((int) (compression_quality * 100), 0, 100, 10)));

// there are accessible final variables
        final HashMap<Integer, Action> color_options = new HashMap<Integer, Action>();
        final HashMap<Integer, Boolean> color_options_switch = new HashMap<Integer, Boolean>();
        sw_color_option = new HashMap<Integer, JCheckBox>();
        final int BLACKANDWHITE = 0;
        final int CANVASSCALEONLY = 1;

// you can define final actions that will act with buttons or checkboxes
        color_options.put(BLACKANDWHITE, new AbstractAction("black & white") {
                    @Override
            public void actionPerformed(ActionEvent e) {
                blackAndWhiteEnabled = sw_color_option.get(BLACKANDWHITE).isSelected();
            }
        });
        color_options_switch.put(BLACKANDWHITE, blackAndWhiteEnabled);

//(É)
        for (Iterator<Integer> i = color_options.keySet().iterator(); i.hasNext();) {
            int aKey = i.next();
            sw_color_option.put(aKey, new JCheckBox(color_options.get(aKey)));
            sw_color_option.get(aKey).setSelected(color_options_switch.get(aKey));
            pane.addStepComponent(step, new JLabel(color_options.get(aKey).getValue(Action.NAME).toString()));
            pane.addStepComponent(step, sw_color_option.get(aKey));
        }
        sw_zoom.setValue(100);
// Applet may be visible so validate in swing EDT
        pane.validate();
    }
}
