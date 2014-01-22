package de.gemo.smartlauncher.launcher.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.UIManager;

import com.eclipsesource.json.JsonObject;

import de.gemo.smartlauncher.launcher.actions.RefreshAction;
import de.gemo.smartlauncher.launcher.frames.LoginFrame;
import de.gemo.smartlauncher.launcher.listener.LoginListener;
import de.gemo.smartlauncher.launcher.units.AuthData;
import de.gemo.smartlauncher.universal.frames.StatusFrame;
import de.gemo.smartlauncher.universal.internet.Worker;
import de.gemo.smartlauncher.universal.units.VARS;

public class Main {

    private static final int version = 2;

    public static AuthData authData = new AuthData();

    public static void main(String[] args) {
        Main.createVersionFile();

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // refresh login...
        refreshLogin();
    }

    private static void refreshLogin() {
        if (authData.load()) {
            new StatusFrame();
            appendWorker(new Worker(new RefreshAction(authData), new LoginListener()));
            startThread();
        } else {
            new LoginFrame(200, 190);
        }
    }

    public synchronized static void createThreads() {
        ThreadHolder.createThreads();
    }

    public synchronized static void appendWorker(Worker worker) {
        ThreadHolder.appendWorker(worker);
    }

    public synchronized static void startThread() {
        ThreadHolder.startThread();
    }

    public static void clearHTTPs() {
        ThreadHolder.clearHTTPs();
    }

    private static void createVersionFile() {
        File versionFile = new File(VARS.DIR.APPDATA, "version.json");

        // create dir
        versionFile.mkdirs();

        // delete old file
        if (versionFile.exists()) {
            versionFile.delete();
        }

        try {
            // create json
            JsonObject json = new JsonObject();
            json.add("version", version);

            // write json to file
            BufferedWriter writer = new BufferedWriter(new FileWriter(versionFile));
            json.writeTo(writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
