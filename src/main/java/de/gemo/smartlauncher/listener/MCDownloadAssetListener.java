package de.gemo.smartlauncher.listener;

import java.net.HttpURLConnection;

import javax.swing.JOptionPane;

import de.gemo.smartlauncher.core.Launcher;
import de.gemo.smartlauncher.core.Logger;
import de.gemo.smartlauncher.frames.MainFrame;
import de.gemo.smartlauncher.frames.StatusFrame;
import de.gemo.smartlauncher.internet.HTTPAction;
import de.gemo.smartlauncher.internet.HTTPListener;
import de.gemo.smartlauncher.units.Asset;
import de.gemo.smartlauncher.units.DownloadInfo;

public class MCDownloadAssetListener extends HTTPListener {

    private Asset asset;

    public MCDownloadAssetListener(Asset asset) {
        this.asset = asset;
    }

    @Override
    public void onStart(HTTPAction action) {
        StatusFrame.INSTANCE.setProgress(0);
        StatusFrame.INSTANCE.setText("downloading '" + this.asset.getPath() + "'...");
        Logger.info("downloading asset '" + this.asset.getPath() + "'...");
    }

    @Override
    public void onFinish(HTTPAction action) {
        if (action.getResponseCode() != HttpURLConnection.HTTP_OK) {
            this.onError(action);
            return;
        }

        try {
            StatusFrame.INSTANCE.setText("download finished...");
            Logger.fine("Asset downloaded: " + this.asset.getPath());
            Asset.decrementAssetsToLoad();
            if (Asset.getAssetsToLoad() < 1) {
                Logger.fine("Assets successfully downloaded...");

                // launch game, if there is nothing left to download...
                DownloadInfo downloadInfo = Launcher.getDownloadInfo();
                if (!downloadInfo.isDownloadMCJar() && downloadInfo.getLibraryCount() < 1) {
                    Launcher.startGame();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.onError(action);
        }
    }

    @Override
    public void onError(HTTPAction action) {
        // clear...
        Launcher.onError();

        StatusFrame.INSTANCE.showFrame(false);
        MainFrame.INSTANCE.showFrame(true);
        JOptionPane.showMessageDialog(null, "Could not start Minecraft...", "Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void onProgress(int maximumLength, int currentLength) {
        float percent = ((float) currentLength / (float) maximumLength) * 100;
        int percentInt = (int) (percent * 100);
        percent = percentInt / 100f;
        StatusFrame.INSTANCE.setProgress((int) percent);
    }
}
