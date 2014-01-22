package de.gemo.smartlauncher.universal.frames;

import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

public class LogFrame {

    public static LogFrame INSTANCE;

    private JFrame frame;
    private JTextArea textArea;
    private JScrollPane scrollPane;

    public static void create() {
        if (INSTANCE != null) {
            INSTANCE.frame.dispose();
            INSTANCE = null;
        }
        new LogFrame();
    }

    private LogFrame() {
        LogFrame.INSTANCE = this;
        int width = 600;
        int height = 400;

        this.frame = new JFrame();
        // this.frame.setDefaultCloseOperation(JFrame.);
        this.frame.setBounds(0, 0, width, height);
        this.frame.setLocation(0, 0);
        this.frame.setTitle("Console");

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

        this.frame.addWindowListener(new WindowListener() {
            // @formatter:off
            public void windowActivated(WindowEvent arg0)   {}
            public void windowClosed(WindowEvent arg0)      {}
            public void windowDeactivated(WindowEvent arg0) {}
            public void windowDeiconified(WindowEvent arg0) {}
            public void windowIconified(WindowEvent arg0)   {}
            public void windowOpened(WindowEvent arg0)      {}    
            // @formatter:on            
            public void windowClosing(WindowEvent arg0) {
                close();
            }
        });

        this.textArea = new JTextArea();
        this.textArea.setEditable(false);
        this.scrollPane = new JScrollPane(this.textArea);
        this.scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        this.scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.scrollPane.setViewportView(this.textArea);
        this.frame.add(this.scrollPane);

        this.frame.setVisible(true);
        this.repositionGUI();
    }

    private void repositionGUI() {
        int width = this.frame.getWidth() - this.frame.getInsets().right - this.frame.getInsets().left;
        int height = this.frame.getHeight() - this.frame.getInsets().top - this.frame.getInsets().bottom;

        this.textArea.setLocation(0, 0);
        this.textArea.setBounds(0, 0, width, height);
        this.scrollPane.setPreferredSize(new Dimension(width, height));
    }

    public void appendText(String text) {
        if (INSTANCE != null) {
            this.textArea.append(text + "\n");
            this.textArea.setCaretPosition(textArea.getDocument().getLength());
        }
    }

    public void close() {
        INSTANCE = null;
    }

}
