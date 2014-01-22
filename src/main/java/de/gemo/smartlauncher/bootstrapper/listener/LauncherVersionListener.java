package de.gemo.smartlauncher.bootstrapper.listener;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.List;

import javax.swing.JOptionPane;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import de.gemo.smartlauncher.bootstrapper.Bootstrapper;
import de.gemo.smartlauncher.launcher.core.Logger;
import de.gemo.smartlauncher.universal.frames.StatusFrame;
import de.gemo.smartlauncher.universal.internet.ByteResponse;
import de.gemo.smartlauncher.universal.internet.DownloadAction;
import de.gemo.smartlauncher.universal.internet.HTTPAction;
import de.gemo.smartlauncher.universal.internet.HTTPListener;
import de.gemo.smartlauncher.universal.internet.Worker;
import de.gemo.smartlauncher.universal.units.ThreadHolder;
import de.gemo.smartlauncher.universal.units.VARS;

public class LauncherVersionListener extends HTTPListener {

    public void onStart(HTTPAction action) {
        // delete old versionFile...
        File oldVersionFile = new File(VARS.DIR.APPDATA, "version.json");
        if (oldVersionFile.exists()) {
            oldVersionFile.delete();
        }

        Logger.info("Checking for update...");
        StatusFrame.INSTANCE.setText("Checking for update...");
        StatusFrame.INSTANCE.setProgress(0);
    }

    public void onFinish(HTTPAction action) {
        if (action.getResponseCode() == HttpURLConnection.HTTP_OK) {
            try {
                ByteResponse response = (ByteResponse) this.getWorker().getResponse();
                String responseString = new String(response.getResponse());
                JsonObject json = JsonObject.readFrom(responseString);
                int version = json.get("version").asInt();
                if (version <= Bootstrapper.INSTANCE.getInstalledLauncherVersion()) {
                    Logger.fine("Launcher is up to date! (Version " + version + ")");
                    Bootstrapper.launchLauncher();
                } else {
                    Logger.fine("Launcher needs an update (installed: " + Bootstrapper.INSTANCE.getInstalledLauncherVersion() + " , updated: " + version + ")!");

                    // delete old launcher...
                    File oldLauncher = new File(VARS.DIR.APPDATA, "Launcher.jar");
                    if (oldLauncher.exists()) {
                        oldLauncher.delete();
                    }

                    JsonArray neededFiles = json.get("files").asArray();
                    if (neededFiles != null) {
                        List<JsonValue> files = neededFiles.values();
                        for (JsonValue value : files) {
                            String fileName = value.asString();
                            ThreadHolder.appendWorker(new Worker(new DownloadAction(VARS.URL.FILES_LAUNCHER + fileName, VARS.DIR.APPDATA, fileName), new LauncherFileListener()));
                            Bootstrapper.setFilesToDownload(Bootstrapper.getFilesToDownload() + 1);
                        }
                        ThreadHolder.startThread();
                    } else {
                        this.onError(action);
                    }
                }
            } catch (Exception e) {
                this.onError(action);
            }
        } else {
            this.onError(action);
        }
    }

    @Override
    public void onError(HTTPAction action) {
        Logger.error("Error verifying launcherversion...");
        JOptionPane.showMessageDialog(null, "Error verifying version...\nAttempting to launch old version...", "Ooops...", JOptionPane.ERROR_MESSAGE);
        if (Bootstrapper.INSTANCE.getInstalledLauncherVersion() == -1) {
            JOptionPane.showMessageDialog(null, "No launcher found!\nExiting...", "Ooops...", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        } else {
            Bootstrapper.launchLauncher();
        }
    }

    @Override
    public void onProgress(int maximumLength, int currentLength) {
        float percent = ((float) currentLength / (float) maximumLength) * 100;
        int percentInt = (int) (percent * 100);
        percent = percentInt / 100f;
        StatusFrame.INSTANCE.setProgress((int) percent);
    }

}
