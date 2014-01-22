package de.gemo.smartlauncher.universal.frames;

import java.awt.Frame;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

public class StatusFrame {

    public static StatusFrame INSTANCE;

    private JFrame frame;
    private JLabel label;
    private JProgressBar progressBar;

    public StatusFrame() {
        int width = 400;
        int height = 60;
        StatusFrame.INSTANCE = this;

        this.frame = new JFrame();
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setBounds(0, 0, width, height);
        this.frame.setLayout(null);
        this.frame.setResizable(false);
        this.frame.setUndecorated(true);
        this.frame.setLocationRelativeTo(null);

        this.progressBar = new JProgressBar();
        this.progressBar.setMinimum(0);
        this.progressBar.setMaximum(100);
        this.progressBar.setValue(50);
        this.frame.add(this.progressBar);

        this.label = new JLabel();
        this.label.setSize(width, 20);
        this.label.setHorizontalAlignment(SwingConstants.CENTER);
        this.label.setVerticalAlignment(SwingConstants.CENTER);
        this.frame.add(label);
        this.frame.setVisible(true);

        this.repositionGUI();
    }

    private void repositionGUI() {
        int width = this.frame.getWidth() - this.frame.getInsets().right - this.frame.getInsets().left;
        int height = this.frame.getHeight() - this.frame.getInsets().top - this.frame.getInsets().bottom;

        this.progressBar.setSize(width - 20, 20);
        this.progressBar.setLocation(10, height / 2 + 5);
        this.label.setSize(width, 20);
        this.label.setLocation(0, progressBar.getLocation().y - 25);
    }

    public void setText(String text) {
        this.label.setText(text);
    }

    public void showFrame(boolean show) {
        this.frame.setVisible(show);
        if (show) {
            this.frame.setState(Frame.NORMAL);
        }
    }

    public void setProgress(int percent) {
        this.progressBar.setValue(percent);
    }

}
