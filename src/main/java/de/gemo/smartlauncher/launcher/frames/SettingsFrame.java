package de.gemo.smartlauncher.launcher.frames;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.eclipsesource.json.JsonObject;

import de.gemo.smartlauncher.launcher.core.Launcher;
import de.gemo.smartlauncher.launcher.units.Pack;
import de.gemo.smartlauncher.universal.units.VARS;

public class SettingsFrame {

    private JDialog dialog;
    private final Pack pack;

    private JCheckBox cb_console, cb_closeOnStart;
    private JSpinField spin_min, spin_max, spin_perm;

    private boolean showConsole = false, closeOnStart = false;
    private int minRam = 512, maxRam = 1024, permGen = 128;

    public SettingsFrame(Pack pack) {
        this.pack = pack;
        this.dialog = new JDialog(MainFrame.INSTANCE.getFrame());
        this.dialog.setTitle("Settings for '" + this.pack.getPackName() + "'...");
        this.dialog.setSize(400, 300);
        this.dialog.setLayout(null);
        this.dialog.setLocationRelativeTo(null);
        this.dialog.setResizable(false);
        this.dialog.setVisible(true);

        // listener
        this.dialog.addWindowListener(new WindowListener() {
            //@formatter:off
            @Override public void windowOpened(WindowEvent e)      {}            
            @Override public void windowIconified(WindowEvent e)   {}            
            @Override public void windowDeiconified(WindowEvent e) {}            
            @Override public void windowDeactivated(WindowEvent e) {}
            @Override public void windowClosed(WindowEvent e)      {}
            @Override public void windowActivated(WindowEvent e)   {}
            //@formatter:on

            @Override
            public void windowClosing(WindowEvent e) {
                MainFrame.INSTANCE.showFrame(true);
            }
        });

        // load settings
        this.loadSettings();

        // create GUI
        this.createGUI();
    }

    private void createGUI() {
        int width = this.dialog.getWidth() - this.dialog.getInsets().right - this.dialog.getInsets().left;

        // launcher
        JPanel launcherPanel = new JPanel();
        launcherPanel.setSize(width - 20, 90);
        launcherPanel.setLocation(10, 10);
        launcherPanel.setLayout(null);
        launcherPanel.setBorder(BorderFactory.createTitledBorder("SmartLauncher"));
        // closeOnStart
        this.cb_closeOnStart = new JCheckBox("close after gamelaunch");
        this.cb_closeOnStart.setSelected(this.closeOnStart);
        this.cb_closeOnStart.setSize(launcherPanel.getWidth() - 40, 20);
        this.cb_closeOnStart.setLocation(20, 30);
        launcherPanel.add(this.cb_closeOnStart);
        // devconsole
        this.cb_console = new JCheckBox("show");
        this.cb_console.setSelected(this.showConsole);
        this.cb_console.setSize(launcherPanel.getWidth() - 40, 20);
        this.cb_console.setLocation(20, this.cb_closeOnStart.getLocation().y + this.cb_closeOnStart.getHeight() + 5);
        launcherPanel.add(this.cb_console);
        this.dialog.add(launcherPanel);

        // java
        JPanel javaPanel = new JPanel();
        javaPanel.setSize(width - 20, 120);
        javaPanel.setLocation(10, launcherPanel.getLocation().y + launcherPanel.getHeight() + 10);
        javaPanel.setLayout(null);
        javaPanel.setBorder(BorderFactory.createTitledBorder("Java"));
        this.dialog.add(javaPanel);

        // minimum ram
        this.spin_min = new JSpinField(512, 64 * 1024);
        this.spin_min.setValue(this.minRam);
        this.spin_min.setSize(80, 20);
        this.spin_min.setLocation(20, 30);
        javaPanel.add(this.spin_min);

        // label
        JLabel lbl_minRam = new JLabel("minimum RAM");
        lbl_minRam.setSize(150, 20);
        lbl_minRam.setLocation(this.spin_min.getLocation().x + this.spin_min.getWidth() + 5, this.spin_min.getLocation().y - 1);
        javaPanel.add(lbl_minRam);

        // maximum ram
        this.spin_max = new JSpinField(1024, 64 * 1024);
        this.spin_max.setValue(this.maxRam);
        this.spin_max.setSize(80, 20);
        this.spin_max.setLocation(20, this.spin_min.getLocation().y + this.spin_min.getHeight() + 5);
        javaPanel.add(this.spin_max);

        // label
        JLabel lbl_maxRam = new JLabel("maximum RAM");
        lbl_maxRam.setSize(150, 20);
        lbl_maxRam.setLocation(this.spin_max.getLocation().x + this.spin_max.getWidth() + 5, this.spin_max.getLocation().y - 1);
        javaPanel.add(lbl_maxRam);

        // permgen
        this.spin_perm = new JSpinField(128, 1024, 16);
        this.spin_perm.setValue(this.permGen);
        this.spin_perm.setSize(80, 20);
        this.spin_perm.setLocation(20, spin_max.getLocation().y + spin_max.getHeight() + 5);
        javaPanel.add(this.spin_perm);

        // label
        JLabel lbl_perm = new JLabel("Permgen");
        lbl_perm.setSize(150, 20);
        lbl_perm.setLocation(this.spin_perm.getLocation().x + this.spin_perm.getWidth() + 5, this.spin_perm.getLocation().y - 1);
        javaPanel.add(lbl_perm);

        // button ok
        JButton btn_ok = new JButton("OK");
        btn_ok.setSize(100, 25);
        btn_ok.setLocation(width / 2 - btn_ok.getSize().width - 10, javaPanel.getLocation().y + javaPanel.getHeight() + 5);
        btn_ok.addMouseListener(new MouseListener() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    onOKClick();
                }
            }

            //@formatter:off
            @Override public void mousePressed(MouseEvent e) {}            
            @Override public void mouseExited(MouseEvent e)  {}            
            @Override public void mouseEntered(MouseEvent e) {}            
            @Override public void mouseClicked(MouseEvent e) {}
            //@formatter:on
        });
        this.dialog.add(btn_ok);

        // button cancel
        JButton btn_cancel = new JButton("Cancel");
        btn_cancel.setSize(100, 25);
        btn_cancel.setLocation(width / 2 + 10, javaPanel.getLocation().y + javaPanel.getHeight() + 5);
        btn_cancel.addMouseListener(new MouseListener() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    onCancelClick();
                }
            }

            //@formatter:off
            @Override public void mousePressed(MouseEvent e) {}            
            @Override public void mouseExited(MouseEvent e)  {}            
            @Override public void mouseEntered(MouseEvent e) {}            
            @Override public void mouseClicked(MouseEvent e) {}
            //@formatter:on
        });
        this.dialog.add(btn_cancel);
    }

    private void onOKClick() {
        try {
            // safety checks...
            if (this.spin_max.getForeground().equals(Color.RED) || this.spin_min.getForeground().equals(Color.RED) || this.spin_perm.getForeground().equals(Color.RED) || this.spin_max.value < this.spin_min.value) {
                JOptionPane.showMessageDialog(null, "Settings are invalid!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // create json
            JsonObject json = new JsonObject();
            json.add("showConsole", this.cb_console.isSelected());
            json.add("closeOnStart", this.cb_closeOnStart.isSelected());
            json.add("minRAM", this.spin_min.value);
            json.add("maxRAM", this.spin_max.value);
            json.add("permGen", this.spin_perm.value);

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
        this.dialog.dispose();
        MainFrame.INSTANCE.showFrame(true);
    }

    private void onCancelClick() {
        this.dialog.dispose();
        MainFrame.INSTANCE.showFrame(true);
    }

    private void loadSettings() {
        try {
            File file = new File(VARS.DIR.PROFILES + "/" + Launcher.authData.getMCUserName(), this.pack.getPackName() + ".json");
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                JsonObject json = JsonObject.readFrom(reader);
                showConsole = json.get("showConsole").asBoolean();
                closeOnStart = json.get("closeOnStart").asBoolean();
                minRam = json.get("minRAM").asInt();
                maxRam = json.get("maxRAM").asInt();
                permGen = json.get("permGen").asInt();
                reader.close();
            }
        } catch (Exception e) {
            showConsole = false;
            permGen = 128;
            minRam = 512;
            maxRam = 1024;
            e.printStackTrace();
        }
    }
}
