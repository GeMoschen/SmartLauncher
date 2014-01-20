package de.gemo.smartlauncher.listener;

import java.net.HttpURLConnection;

import javax.swing.JOptionPane;

import de.gemo.smartlauncher.core.Launcher;
import de.gemo.smartlauncher.core.Logger;
import de.gemo.smartlauncher.frames.MainFrame;
import de.gemo.smartlauncher.frames.StatusFrame;
import de.gemo.smartlauncher.internet.HTTPAction;
import de.gemo.smartlauncher.internet.HTTPListener;

public class GetSinglePackListener extends HTTPListener {

    public void onStart(HTTPAction action) {
        Logger.info(action.getShortDescription());
    }

    public void onFinish(HTTPAction action) {
        try {
            if (action.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Logger.fine("Pack downloaded...");

                // launch game...
                if (Launcher.INSTANCE.extractPack()) {
                    Launcher.INSTANCE.launchPack();
                } else {
                    // clear all...
                    Launcher.onError();

                    // show info...
                    StatusFrame.INSTANCE.showFrame(false);
                    MainFrame.INSTANCE.showFrame(true);
                    Logger.error("Error extracting packfile...");
                    JOptionPane.showMessageDialog(null, "Error extracting packfile...", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else if (action.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                // clear all...
                Launcher.onError();

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
        // clear...
        Launcher.onError();

        // show info...
        Logger.error("Could not download pack! (Statuscode: " + action.getResponseCode() + ")");
        StatusFrame.INSTANCE.showFrame(false);
        MainFrame.INSTANCE.showFrame(true);
        JOptionPane.showMessageDialog(null, "Could not download pack...\n\nExiting....", "Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void onProgress(int maximumLength, int currentLength) {
        float percent = ((float) currentLength / (float) maximumLength) * 100;
        int percentInt = (int) (percent * 100);
        percent = percentInt / 100f;
        StatusFrame.INSTANCE.setProgress((int) percent);
    }

}
