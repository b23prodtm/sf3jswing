/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jxa;

import java.awt.BorderLayout;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

/**
 * Accessory panel that can be used as the {@link JFileChooser#setAccessory(javax.swing.JComponent) JFileChooser accessory}.
 * @author www.b23prodtm.info
 */
public class SaveAccessory {

    JPanel panelOpt = new JPanel(new BorderLayout(), true);
    JSlider compressionQuality = new JSlider(0, 100, Math.round(IMCBrowser._DEF_CPR_QUALITY * 100f));
    JLabel compressionQualityLabel = new JLabel(LocalizedResources.localized.getString("compressionQuality"));

    public SaveAccessory() {
        compressionQuality.setMajorTickSpacing(20);
        compressionQuality.setMinorTickSpacing(5);
        compressionQuality.setSnapToTicks(true);
        compressionQuality.setPaintLabels(true);
        compressionQuality.setPaintTicks(true);
        panelOpt.add(compressionQualityLabel, BorderLayout.NORTH);
        panelOpt.add(compressionQuality, BorderLayout.SOUTH);
    }

    public JSlider getCompressionQualitySlider() {
        return compressionQuality;
    }

    public JPanel getPanelOpt() {
        return panelOpt;
    }

}
