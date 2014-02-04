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
import de.gemo.smartlauncher.universal.units.ThreadHolder;
import de.gemo.smartlauncher.universal.units.VARS;

public class Launcher {
    private static final int version = 20;

    public static AuthData authData = new AuthData();

    public Launcher() {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // create versionfile...
        this.createVersionFile();

        // refresh login...
        this.refreshLogin();
    }

    public static int getVersion() {
        return version;
    }

    private void refreshLogin() {
        if (authData.load()) {
            new StatusFrame();
            ThreadHolder.appendWorker(new Worker(new RefreshAction(authData), new LoginListener()));
            ThreadHolder.startThread();
        } else {
            new LoginFrame(200, 190);
        }
    }

    private void createVersionFile() {
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
