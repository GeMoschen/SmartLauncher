package de.gemo.smartlauncher.launcher.frames;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import de.gemo.smartlauncher.launcher.units.Pack;

public class PopupMenu extends JPopupMenu {

    private static final long serialVersionUID = 7638221142030411714L;

    private final JLabel label;
    private final Pack pack;
    private JMenuItem settingsItem;
    private JMenuItem exitItem;

    public PopupMenu(final JLabel fatherLabel, Pack pack) {
        this.label = fatherLabel;
        this.pack = pack;

        // separator
        JSeparator seperator = new JSeparator();
        this.add(seperator);

        // settings
        this.settingsItem = new JMenuItem("Settings");
        this.settingsItem.addMouseListener(new MouseListener() {
            @Override
            public void mouseReleased(MouseEvent e) {
                label.setBackground(Color.ORANGE);
                if (e.getButton() == MouseEvent.BUTTON1) {
                    onSettingsClick();
                }
            }

            //@formatter:off
            @Override public void mousePressed(MouseEvent e) {}            
            @Override public void mouseExited(MouseEvent e)  {}            
            @Override public void mouseEntered(MouseEvent e) {}            
            @Override public void mouseClicked(MouseEvent e) {}
            //@formatter:on
        });
        this.add(this.settingsItem);

        // separator
        seperator = new JSeparator();
        this.add(seperator);

        // exit
        this.exitItem = new JMenuItem("Exit");
        this.exitItem.addMouseListener(new MouseListener() {
            @Override
            public void mouseReleased(MouseEvent e) {
                label.setBackground(Color.ORANGE);
                if (e.getButton() == MouseEvent.BUTTON1) {
                    onExitClick();
                }
            }

            //@formatter:off
            @Override public void mousePressed(MouseEvent e) {}            
            @Override public void mouseExited(MouseEvent e)  {}            
            @Override public void mouseEntered(MouseEvent e) {}            
            @Override public void mouseClicked(MouseEvent e) {}
            //@formatter:on
        });
        this.add(this.exitItem);

        // separator
        seperator = new JSeparator();
        this.add(seperator);
    }

    private void onSettingsClick() {
        MainFrame.INSTANCE.showFrame(false);
        new SettingsFrame(this.pack);
    }

    private void onExitClick() {
        System.exit(0);
    }
}
