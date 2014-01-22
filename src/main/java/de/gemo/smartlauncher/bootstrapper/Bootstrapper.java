package de.gemo.smartlauncher.bootstrapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.eclipsesource.json.JsonObject;

import de.gemo.smartlauncher.bootstrapper.listener.LauncherVersionListener;
import de.gemo.smartlauncher.launcher.core.Logger;
import de.gemo.smartlauncher.universal.frames.LogFrame;
import de.gemo.smartlauncher.universal.frames.StatusFrame;
import de.gemo.smartlauncher.universal.internet.ByteAction;
import de.gemo.smartlauncher.universal.internet.Worker;
import de.gemo.smartlauncher.universal.units.ThreadHolder;
import de.gemo.smartlauncher.universal.units.VARS;

public class Bootstrapper {

    public static Bootstrapper INSTANCE;
    private static int filesToDownload = 0;

    private int installedLauncherVersion = -1;

    public Bootstrapper() {
        Bootstrapper.INSTANCE = this;
        this.readLauncherVersion();

        new StatusFrame();
        LogFrame.create();
        StatusFrame.INSTANCE.showFrame(true);
        StatusFrame.INSTANCE.setText("waiting...");

        ThreadHolder.appendWorker(new Worker(new ByteAction(VARS.URL.VERSION_LAUNCHER), new LauncherVersionListener()));
        ThreadHolder.startThread();
    }

    private void readLauncherVersion() {
        File versionFile = new File(VARS.DIR.APPDATA, "version.json");
        File launcherFile = new File(VARS.DIR.APPDATA, "Launcher.jar");
        if (!versionFile.exists() || !launcherFile.exists()) {
            this.installedLauncherVersion = -1;
            return;
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(versionFile));
            JsonObject json = JsonObject.readFrom(reader);
            this.installedLauncherVersion = json.get("version").asInt();
            reader.close();
        } catch (Exception e) {
            this.installedLauncherVersion = -1;
        }
    }

    public int getInstalledLauncherVersion() {
        return installedLauncherVersion;
    }

    public static void launchLauncher() {
        File launcherFile = new File(VARS.DIR.APPDATA, "Launcher.jar");
        if (!launcherFile.exists()) {
            JOptionPane.showMessageDialog(null, "Launcher not found!\nExiting...", "Ooops...", JOptionPane.ERROR_MESSAGE);
            Logger.fine("Bootstrapper will be terminated...");
            System.exit(0);
            return;
        }
        ArrayList<String> cmd = new ArrayList<String>();

        // standard...
        cmd.add("java");

        // append classpath
        cmd.add("-cp");
        cmd.add("\"" + VARS.DIR.APPDATA + "Launcher.jar" + "\"");

        // append mainclass
        cmd.add(VARS.MAINCLASS_LAUNCHER);

        // ... and finally, try to launch...
        try {
            Logger.info("Starting Launcher...");
            StatusFrame.INSTANCE.setText("Starting Launcher...");
            Process process = new ProcessBuilder(cmd).directory(new File(VARS.DIR.APPDATA)).redirectErrorStream(true).start();
            if (process != null) {
                Logger.fine("Launcher started!");
                Logger.fine("Bootstrapper will be terminated...");
                System.exit(0);
            } else {
                JOptionPane.showMessageDialog(null, "Could not start launcher!\nExiting...", "Ooops...", JOptionPane.ERROR_MESSAGE);
                Logger.fine("Bootstrapper will be terminated...");
                System.exit(0);
                return;
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Could not start launcher!\nExiting...", "Ooops...", JOptionPane.ERROR_MESSAGE);
            Logger.fine("Bootstrapper will be terminated...");
            System.exit(0);
            return;
        }
    }

    public static synchronized void setFilesToDownload(int filesToDownload) {
        Bootstrapper.filesToDownload = filesToDownload;
    }

    public static synchronized int getFilesToDownload() {
        return Bootstrapper.filesToDownload;
    }

}
