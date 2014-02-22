package de.gemo.smartlauncher.launcher.listener;

import java.net.HttpURLConnection;

import javax.swing.JOptionPane;

import de.gemo.smartlauncher.launcher.core.GameLauncher;
import de.gemo.smartlauncher.launcher.frames.MainFrame;
import de.gemo.smartlauncher.universal.frames.StatusFrame;
import de.gemo.smartlauncher.universal.internet.HTTPAction;
import de.gemo.smartlauncher.universal.internet.HTTPListener;
import de.gemo.smartlauncher.universal.units.Logger;

public class GetSinglePackListener extends HTTPListener {

    private String packName = "";

    public void onStart(HTTPAction action) {
        Logger.info("downloading packfile: '" + action.getShortDescription() + "'...");
        this.packName = action.getShortDescription();
    }

    public void onFinish(HTTPAction action) {
        try {
            if (action.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Logger.fine("Pack downloaded...");

                // launch game...
                if (GameLauncher.getPackInfo().getPack().extractPack()) {
                    if (GameLauncher.checkFiles() && GameLauncher.prepareGame()) {
                        GameLauncher.startGame();
                    }
                } else {
                    // clear all...
                    GameLauncher.onError();

                    // show info...
                    StatusFrame.INSTANCE.showFrame(false);
                    MainFrame.INSTANCE.showFrame(true);
                    Logger.error("Error extracting packfile...");
                    JOptionPane.showMessageDialog(null, "Error extracting packfile...", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else if (action.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                // clear all...
                GameLauncher.onError();

                // show info...
                StatusFrame.INSTANCE.showFrame(false);
                MainFrame.INSTANCE.showFrame(true);
                Logger.error("User is not allowed to use this pack! (ERROR " + action.getResponseCode() + ")");
                JOptionPane.showMessageDialog(null, "You are not allowed to use this pack!", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                this.onError(action);
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.onError(action);
            return;
        }
    }

    @Override
    public void onError(HTTPAction action) {
        GameLauncher.handleException(new Exception("Could not download pack! (Statuscode: " + action.getResponseCode() + ")"));
    }

    @Override
    public void onProgress(int maximumLength, int currentLength) {
        float percent = ((float) currentLength / (float) maximumLength) * 100;
        int percentInt = (int) (percent * 100);
        percent = percentInt / 100f;
        StatusFrame.INSTANCE.setProgress((int) percent);
        StatusFrame.INSTANCE.setText("downloading '" + this.packName + "': " + percent + "%");
    }

}
