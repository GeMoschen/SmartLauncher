package de.gemo.smartlauncher.listener;

import java.net.HttpURLConnection;

import de.gemo.smartlauncher.core.Launcher;
import de.gemo.smartlauncher.core.Logger;
import de.gemo.smartlauncher.frames.StatusFrame;
import de.gemo.smartlauncher.internet.HTTPAction;
import de.gemo.smartlauncher.internet.HTTPListener;
import de.gemo.smartlauncher.units.DownloadInfo;
import de.gemo.smartlauncher.units.Library;

public class MCDownloadLibraryListener extends HTTPListener {

    private Library library;

    public MCDownloadLibraryListener(Library library) {
        this.library = library;
    }

    @Override
    public void onStart(HTTPAction action) {
        StatusFrame.INSTANCE.setProgress(0);
        StatusFrame.INSTANCE.setText("downloading '" + this.library.getFileName() + "'...");
        Logger.info("downloading library '" + this.library.getFileName() + "'...");
    }

    @Override
    public void onFinish(HTTPAction action) {
        if (action.getResponseCode() != HttpURLConnection.HTTP_OK) {
            this.onError(action);
            return;
        }

        try {
            StatusFrame.INSTANCE.setText("download finished...");
            Logger.fine("Library downloaded: " + this.library.getFileName());
            if (Library.incrementCount()) {
                Logger.fine("Libraries successfully downloaded...");

                // launch game, if there is nothing left to download...
                DownloadInfo downloadInfo = Launcher.getDownloadInfo();
                if (!downloadInfo.isDownloadMCJar()) {
                    if (Launcher.prepareGame()) {
                        Launcher.startGame();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.onError(action);
        }
    }

    @Override
    public void onError(HTTPAction action) {
        Launcher.handleException(new Exception("Could not download library '" + this.library.getName() + "'!"));
    }

    @Override
    public void onProgress(int maximumLength, int currentLength) {
        float percent = ((float) currentLength / (float) maximumLength) * 100;
        int percentInt = (int) (percent * 100);
        percent = percentInt / 100f;
        StatusFrame.INSTANCE.setProgress((int) percent);
    }
}
