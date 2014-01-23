package de.gemo.smartlauncher.launcher.frames;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.MouseInputListener;

import de.gemo.smartlauncher.launcher.core.GameLauncher;
import de.gemo.smartlauncher.launcher.core.Launcher;
import de.gemo.smartlauncher.launcher.units.Pack;
import de.gemo.smartlauncher.launcher.units.PackVersion;

public class MainFrame {

    public static MainFrame INSTANCE;

    public static int IMAGE_DIM = 48;

    // GUI
    private JFrame frame;
    private JComboBox<String> versionBox;
    private JPanel packPanel;
    private JScrollPane packScrollPane;

    private ArrayList<JLabel> packLabels = new ArrayList<JLabel>();

    private Pack selectedPack = null;
    private JLabel selectedLabel = null;

    public MainFrame(String title, int width, int height) {
        MainFrame.INSTANCE = this;
        this.frame = new JFrame(title + " (v" + Launcher.INSTANCE.getVersion() + ")");
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setBounds(0, 0, width, height);
        this.frame.setLayout(null);
        this.frame.setLocationRelativeTo(null);

        this.frame.addComponentListener(new ComponentListener() {
            // @formatter:off
            public void componentShown(ComponentEvent e) {}
            public void componentMoved(ComponentEvent e) {}
            public void componentHidden(ComponentEvent e) {}
            // @formatter:on
            public void componentResized(ComponentEvent e) {
                repositionGUI();
            }
        });

        this.setIcon();

        // versionbox
        this.versionBox = new JComboBox<String>();
        this.versionBox.setEnabled(false);
        this.versionBox.setLocation(10, 10);
        this.frame.add(this.versionBox);

        this.packPanel = new JPanel();
        this.packPanel.setLayout(null);
        this.packScrollPane = new JScrollPane(this.packPanel);
        this.packScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        this.packScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        this.packScrollPane.setViewportView(this.packPanel);
        this.frame.add(this.packScrollPane);

        // add loaded packs...
        for (Pack pack : Pack.loadedPacks.values()) {
            this.addPack(pack);
        }

        // set visible
        this.frame.setVisible(true);

        // resize components
        this.repositionGUI();
    }

    private void setIcon() {
        ImageIcon imgicon = new ImageIcon("icon.png");
        this.frame.setIconImage(imgicon.getImage());
    }

    private void updateVersions(Pack pack) {
        this.versionBox.setSelectedIndex(-1);
        this.versionBox.removeAllItems();

        int selectedIndex = -1;
        int index = 0;
        for (PackVersion version : pack.getVersions()) {
            this.versionBox.addItem(version.getVersion().toString());
            if (version.getVersion().contains(" - recommended")) {
                selectedIndex = index;
            }
            index++;
        }
        this.versionBox.setEnabled(true);
        this.versionBox.setSelectedIndex(selectedIndex);
    }

    private void repositionGUI() {
        int width = this.frame.getWidth() - this.frame.getInsets().right - this.frame.getInsets().left;
        int height = this.frame.getHeight() - this.frame.getInsets().top - this.frame.getInsets().bottom;
        int index = 0;
        this.versionBox.setLocation(10, 10);
        this.versionBox.setSize(width - 20, 25);

        this.packScrollPane.setLocation(10, 45);
        this.packScrollPane.setSize(width - 20, height - 55);
        this.packPanel.setLocation(0, 0);
        this.packPanel.setPreferredSize(new Dimension(width - 20, this.packLabels.size() * (IMAGE_DIM + 2)));
        this.packScrollPane.setPreferredSize(new Dimension(width, this.packLabels.size() * (IMAGE_DIM + 2)));

        for (JLabel iconLabel : this.packLabels) {
            iconLabel.setSize(width - 20, IMAGE_DIM);
            iconLabel.setLocation(0, index * (IMAGE_DIM + 2));
            index++;
        }
    }

    public void exit(int statusCode) {
        System.exit(statusCode);
    }

    private void addPack(final Pack pack) {
        final JLabel iconLabel = new JLabel(new ImageIcon(pack.getIcon()), SwingConstants.LEFT);
        iconLabel.addMouseListener(new MouseInputListener() {
            // @formatter:off
            @Override public void mouseMoved(MouseEvent e) {}
            @Override public void mouseDragged(MouseEvent e) {}
            @Override public void mouseReleased(MouseEvent e) {}  
            @Override public void mouseExited(MouseEvent e) {}
            @Override public void mouseEntered(MouseEvent e) {}
            // @formatter:on
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    // START MODPACK
                    if (selectedLabel != null && selectedLabel.isEnabled()) {
                        new GameLauncher(selectedPack, versionBox.getItemAt(versionBox.getSelectedIndex()).toString());
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (selectedLabel != null) {
                    selectedLabel.setOpaque(false);
                    selectedLabel.setBackground(iconLabel.getBackground());
                }
                if (selectedLabel != iconLabel) {
                    selectedPack = pack;
                    selectedLabel = iconLabel;
                    selectedLabel.setOpaque(true);
                    selectedLabel.setBackground(Color.ORANGE);
                    updateVersions(selectedPack);
                }
            }
        });
        iconLabel.setText(pack.getPackName());
        this.packLabels.add(iconLabel);
        this.packPanel.add(iconLabel);
        this.packPanel.revalidate();
        this.packPanel.repaint();
    }

    public void showFrame(boolean show) {
        for (JLabel label : this.packLabels) {
            label.setEnabled(show);
        }
        this.versionBox.setEnabled(show);
        if (show) {
            this.frame.setState(Frame.NORMAL);
        } else {
            this.frame.setState(Frame.ICONIFIED);
        }
    }
}
