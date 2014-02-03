package de.gemo.smartlauncher.launcher.frames;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.eclipsesource.json.JsonObject;

import de.gemo.smartlauncher.launcher.core.Launcher;
import de.gemo.smartlauncher.launcher.units.Pack;
import de.gemo.smartlauncher.universal.units.VARS;

public class SettingsFrame extends JDialog {

    private static final long serialVersionUID = 6125415388456153339L;

    private final JPanel contentPanel = new JPanel();

    private final Pack pack;
    private int minRAM = 512, maxRAM = 1024, permgen = 128;
    boolean showConsole = false, closeOnStart = false;

    /**
     * Create the dialog.
     */
    public SettingsFrame(Pack pack) {
        // load settings
        this.pack = pack;
        this.loadSettings();

        // build gui
        setTitle("Settings for '" + this.pack.getPackName() + "'...");
        setType(Type.UTILITY);
        setResizable(false);
        setBounds(100, 100, 368, 272);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(null);
        {
            JPanel panel_smartLauncher = new JPanel();
            panel_smartLauncher.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "SmartLauncher", TitledBorder.LEADING, TitledBorder.TOP, null, null));
            panel_smartLauncher.setBounds(10, 11, 342, 71);
            contentPanel.add(panel_smartLauncher);
            panel_smartLauncher.setLayout(null);

            final JCheckBox cb_closeOnStart = new JCheckBox("close after gamelaunch");
            cb_closeOnStart.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    closeOnStart = cb_closeOnStart.isSelected();
                    System.out.println("Close: " + closeOnStart);
                }
            });
            cb_closeOnStart.setSelected(this.closeOnStart);
            cb_closeOnStart.setBounds(6, 18, 330, 23);
            panel_smartLauncher.add(cb_closeOnStart);

            final JCheckBox cb_showConsole = new JCheckBox("show console");
            cb_showConsole.setSelected(this.showConsole);
            cb_showConsole.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    showConsole = cb_showConsole.isSelected();
                    System.out.println("Show: " + showConsole);
                }
            });
            cb_showConsole.setBounds(6, 44, 330, 23);
            panel_smartLauncher.add(cb_showConsole);
        }

        JPanel panel_java = new JPanel();
        panel_java.setBorder(new TitledBorder(null, "Java", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel_java.setBounds(10, 93, 342, 107);
        contentPanel.add(panel_java);
        panel_java.setLayout(null);

        JLabel lbl_minRAM = new JLabel("minimum RAM (in MB)");
        lbl_minRAM.setBounds(85, 24, 247, 14);
        panel_java.add(lbl_minRAM);

        final JSpinner spin_minRAM = new JSpinner();
        final JSpinner spin_maxRAM = new JSpinner();

        spin_minRAM.setModel(new SpinnerNumberModel(minRAM, 512, maxRAM, 128));
        spin_minRAM.setBounds(10, 21, 65, 20);
        panel_java.add(spin_minRAM);

        spin_maxRAM.setModel(new SpinnerNumberModel(maxRAM, minRAM, 32768, 128));
        spin_maxRAM.setBounds(10, 49, 65, 20);
        panel_java.add(spin_maxRAM);

        spin_minRAM.addChangeListener(new ChangeListener() {
            @SuppressWarnings("unchecked")
            public void stateChanged(ChangeEvent e) {
                SpinnerNumberModel minModel = (SpinnerNumberModel) spin_minRAM.getModel();
                SpinnerNumberModel maxModel = (SpinnerNumberModel) spin_maxRAM.getModel();
                maxModel.setMinimum((Comparable<Integer>) minModel.getValue());
                minRAM = (Integer) minModel.getValue();
            }
        });

        spin_maxRAM.addChangeListener(new ChangeListener() {
            @SuppressWarnings("unchecked")
            public void stateChanged(ChangeEvent e) {
                SpinnerNumberModel minModel = (SpinnerNumberModel) spin_minRAM.getModel();
                SpinnerNumberModel maxModel = (SpinnerNumberModel) spin_maxRAM.getModel();
                minModel.setMaximum((Comparable<Integer>) maxModel.getValue());
                maxRAM = (Integer) maxModel.getValue();
            }
        });

        JLabel lbl_maxRAM = new JLabel("maximum RAM (in MB)");
        lbl_maxRAM.setBounds(85, 52, 247, 14);
        panel_java.add(lbl_maxRAM);

        final JSpinner spin_permgen = new JSpinner();
        spin_permgen.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                SpinnerNumberModel permModel = (SpinnerNumberModel) spin_permgen.getModel();
                permgen = (Integer) permModel.getValue();
            }
        });
        JLabel lbl_permgen = new JLabel("PermGen (in MB)");
        lbl_permgen.setBounds(85, 80, 247, 14);
        panel_java.add(lbl_permgen);

        spin_permgen.setModel(new SpinnerNumberModel(128, 128, 1024, 16));
        spin_permgen.setBounds(10, 77, 65, 20);

        panel_java.add(spin_permgen);
        {
            JButton btn_ok = new JButton("OK");
            btn_ok.setBounds(212, 211, 65, 23);
            contentPanel.add(btn_ok);
            btn_ok.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    onOKClick();
                }
            });
            btn_ok.setActionCommand("OK");
            getRootPane().setDefaultButton(btn_ok);
        }
        {
            JButton btn_cancel = new JButton("Cancel");
            btn_cancel.setBounds(287, 211, 65, 23);
            contentPanel.add(btn_cancel);
            btn_cancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    onCancelClick();
                }
            });
            btn_cancel.setActionCommand("Cancel");
        }

        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void loadSettings() {
        try {
            File file = new File(VARS.DIR.PROFILES + "/" + Launcher.authData.getMCUserName(), this.pack.getPackName() + ".json");
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                JsonObject json = JsonObject.readFrom(reader);
                showConsole = json.get("showConsole").asBoolean();
                closeOnStart = json.get("closeOnStart").asBoolean();
                minRAM = json.get("minRAM").asInt();
                maxRAM = json.get("maxRAM").asInt();
                permgen = json.get("permGen").asInt();
                reader.close();
            }
        } catch (Exception e) {
            showConsole = false;
            permgen = 128;
            minRAM = 512;
            maxRAM = 1024;
            e.printStackTrace();
        }
    }

    private void onOKClick() {
        try {
            // create json
            JsonObject json = new JsonObject();
            json.add("showConsole", this.showConsole);
            json.add("closeOnStart", this.closeOnStart);
            json.add("minRAM", this.minRAM);
            json.add("maxRAM", this.maxRAM);
            json.add("permGen", this.permgen);

            // delete old file
            File file = new File(VARS.DIR.PROFILES + "/" + Launcher.authData.getMCUserName(), this.pack.getPackName() + ".json");
            if (file.exists()) {
                file.delete();
            }

            // save it
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            json.writeTo(writer);
            writer.close();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error while saving!", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        // close dialog
        this.dispose();
        MainFrame.INSTANCE.showFrame(true);
    }

    private void onCancelClick() {
        this.dispose();
        MainFrame.INSTANCE.showFrame(true);
    }
}
