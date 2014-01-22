package de.gemo.smartlauncher.launcher.listener;

import java.net.HttpURLConnection;

import de.gemo.smartlauncher.launcher.core.GameLauncher;
import de.gemo.smartlauncher.launcher.core.Logger;
import de.gemo.smartlauncher.universal.frames.StatusFrame;
import de.gemo.smartlauncher.universal.internet.HTTPAction;
import de.gemo.smartlauncher.universal.internet.HTTPListener;

public class MCDownloadFileListener extends HTTPListener {

    private String fileName;

    public MCDownloadFileListener(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void onStart(HTTPAction action) {
        StatusFrame.INSTANCE.setProgress(0);
        StatusFrame.INSTANCE.setText("downloading '" + this.fileName + "'...");
        Logger.info("downloading minecraft '" + this.fileName + "'...");
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
            if (GameLauncher.prepareGame()) {
                GameLauncher.startGame();
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.onError(action);
        }
    }

    @Override
    public void onError(HTTPAction action) {
        GameLauncher.handleException(new Exception("Could not download minecraft.jar!"));
    }

    @Override
    public void onProgress(int maximumLength, int currentLength) {
        float percent = ((float) currentLength / (float) maximumLength) * 100;
        int percentInt = (int) (percent * 100);
        percent = percentInt / 100f;
        StatusFrame.INSTANCE.setProgress((int) percent);
    }
}
