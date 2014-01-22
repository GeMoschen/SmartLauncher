package de.gemo.smartlauncher.launcher.frames;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import de.gemo.smartlauncher.launcher.actions.LoginAction;
import de.gemo.smartlauncher.launcher.core.Main;
import de.gemo.smartlauncher.launcher.listener.LoginListener;
import de.gemo.smartlauncher.universal.frames.StatusFrame;
import de.gemo.smartlauncher.universal.internet.Worker;

public class LoginFrame {

    public static LoginFrame INSTANCE;

    private JFrame frame;

    private JButton btn_login;
    private JLabel lbl_mcUserName, lbl_mcPassword;
    private JTextField txt_mcUserName;
    private JPasswordField txt_mcPassword;

    public LoginFrame(int width, int height) {
        LoginFrame.INSTANCE = this;

        this.frame = new JFrame();
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setBounds(0, 0, width, height);
        this.frame.setLayout(null);
        this.frame.setLocationRelativeTo(null);
        this.frame.setResizable(false);
        this.frame.setUndecorated(true);

        this.createGUI();
        this.repositionGUI();
    }

    private void repositionGUI() {
        this.txt_mcUserName.setLocation(10, 25);
        this.lbl_mcUserName.setLocation(this.txt_mcUserName.getLocation().x + 2, this.txt_mcUserName.getLocation().y - 22);
        this.txt_mcPassword.setLocation(this.txt_mcUserName.getLocation().x, this.txt_mcUserName.getLocation().y + 50);
        this.lbl_mcPassword.setLocation(this.txt_mcPassword.getLocation().x + 2, this.txt_mcPassword.getLocation().y - 22);
        this.btn_login.setLocation(this.txt_mcPassword.getLocation().x, this.txt_mcPassword.getLocation().y + 40);
        int width = this.frame.getWidth() - this.frame.getInsets().right - this.frame.getInsets().left;
        int height = this.frame.getHeight() - this.frame.getInsets().top - this.frame.getInsets().bottom;
        width = this.frame.getInsets().right + this.frame.getInsets().left + this.txt_mcUserName.getLocation().x + this.txt_mcUserName.getLocation().x + this.txt_mcUserName.getWidth();
        height = this.frame.getInsets().top + this.frame.getInsets().bottom + this.btn_login.getLocation().y + 10 + this.btn_login.getHeight();
        this.frame.setSize(width, height);
    }

    private void createGUI() {

        // some vars...
        final int txt_height = 30;

        // download
        this.btn_login = new JButton("Login");
        this.btn_login.setSize(150, txt_height);
        this.btn_login.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doLogin();
            }
        });
        this.frame.add(this.btn_login);

        // MC-Username
        this.txt_mcUserName = new JTextField();
        this.txt_mcUserName.setSize(150, txt_height);
        this.frame.add(this.txt_mcUserName);

        this.lbl_mcUserName = new JLabel("Username:");
        this.lbl_mcUserName.setSize(this.txt_mcUserName.getWidth(), txt_height);
        this.frame.add(this.lbl_mcUserName);

        // MC-Password
        this.txt_mcPassword = new JPasswordField();
        this.txt_mcPassword.setSize(150, txt_height);
        this.frame.add(this.txt_mcPassword);

        this.lbl_mcPassword = new JLabel("Password:");
        this.lbl_mcPassword.setSize(this.txt_mcPassword.getWidth(), txt_height);
        this.frame.add(this.lbl_mcPassword);

        // set visible
        this.frame.setVisible(true);

        // resize components
        this.repositionGUI();
    }

    private void doLogin() {
        if (StatusFrame.INSTANCE == null) {
            new StatusFrame();
        } else {
            StatusFrame.INSTANCE.showFrame(true);
        }
        Main.authData.resetData();
        this.enableLoginGUI(false);
        Main.appendWorker(new Worker(new LoginAction(this.txt_mcUserName.getText(), new String(this.txt_mcPassword.getPassword())), new LoginListener()));
        Main.startThread();
    }

    public void enableLoginGUI(boolean enabled) {
        this.txt_mcPassword.setEnabled(enabled);
        this.txt_mcUserName.setEnabled(enabled);
        this.btn_login.setEnabled(enabled);
    }

    public void showGUI(boolean show) {
        this.frame.setVisible(show);
    }
}
