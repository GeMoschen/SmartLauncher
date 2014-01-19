package de.gemo.smartlauncher.listener;

import java.net.HttpURLConnection;

import javax.swing.JOptionPane;

import de.gemo.smartlauncher.core.Launcher;
import de.gemo.smartlauncher.core.Logger;
import de.gemo.smartlauncher.frames.MainFrame;
import de.gemo.smartlauncher.frames.StatusFrame;
import de.gemo.smartlauncher.internet.HTTPAction;
import de.gemo.smartlauncher.internet.HTTPListener;

public class MCDownloadFileListener extends HTTPListener {

    private String fileName;

    public MCDownloadFileListener(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void onStart(HTTPAction action) {
        StatusFrame.INSTANCE.setText("downloading '" + this.fileName + "'...");
        StatusFrame.INSTANCE.setProgress(0);
    }

    @Override
    public void onFinish(HTTPAction action) {
        if (action.getResponseCode() != HttpURLConnection.HTTP_OK) {
            this.onError(action);
            return;
        }

        try {
            StatusFrame.INSTANCE.setText("download finished...");
            Logger.fine("Minecraft downloaded: " + this.fileName);
            Launcher.startGame();
        } catch (Exception e) {
            e.printStackTrace();
            this.onError(action);
        }
    }

    @Override
    public void onError(HTTPAction action) {
        // clear...
        Launcher.onError();

        StatusFrame.INSTANCE.showGUI(false);
        MainFrame.CORE.showFrame(true);
        JOptionPane.showMessageDialog(null, "Could not start Minecraft... 1", "Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void onProgress(int maximumLength, int currentLength) {
        float percent = ((float) currentLength / (float) maximumLength) * 100;
        int percentInt = (int) (percent * 100);
        percent = percentInt / 100f;
        StatusFrame.INSTANCE.setProgress((int) percent);
    }
}
