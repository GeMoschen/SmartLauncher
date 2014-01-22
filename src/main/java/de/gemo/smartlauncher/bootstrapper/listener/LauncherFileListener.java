package de.gemo.smartlauncher.bootstrapper.listener;

import java.io.File;
import java.net.HttpURLConnection;

import javax.swing.JOptionPane;

import de.gemo.smartlauncher.bootstrapper.Bootstrapper;
import de.gemo.smartlauncher.universal.frames.StatusFrame;
import de.gemo.smartlauncher.universal.internet.DownloadAction;
import de.gemo.smartlauncher.universal.internet.DownloadResponse;
import de.gemo.smartlauncher.universal.internet.HTTPAction;
import de.gemo.smartlauncher.universal.internet.HTTPListener;
import de.gemo.smartlauncher.universal.units.Logger;

public class LauncherFileListener extends HTTPListener {

    public void onStart(HTTPAction action) {
        // delete old file
        DownloadAction thisAction = (DownloadAction) action;
        File file = new File(thisAction.getSaveDir(), thisAction.getFileName());
        if (file.exists()) {
            file.delete();
        }
        // some status...
        Logger.info("Downloading '" + action.getShortDescription() + "'...");
        StatusFrame.INSTANCE.setText("Downloading '" + action.getShortDescription() + "'...");
        StatusFrame.INSTANCE.setProgress(0);
    }

    public void onFinish(HTTPAction action) {
        if (action.getResponseCode() == HttpURLConnection.HTTP_OK) {
            Bootstrapper.setFilesToDownload(Bootstrapper.getFilesToDownload() - 1);
            if (Bootstrapper.getFilesToDownload() < 1) {
                Logger.fine("All files downloaded...");
                Bootstrapper.launchLauncher();
            }
        } else {
            this.onError(action);
        }
    }

    @Override
    public void onError(HTTPAction action) {
        DownloadResponse response = (DownloadResponse) this.getWorker().getResponse();
        Logger.error("Error downloading file '" + response.getFileName() + "'...\nAttempting to launch old version...");
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
