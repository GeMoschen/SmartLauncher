package de.gemo.smartlauncher.launcher.listener;

import java.net.HttpURLConnection;

import de.gemo.smartlauncher.launcher.core.GameLauncher;
import de.gemo.smartlauncher.launcher.units.Asset;
import de.gemo.smartlauncher.launcher.units.DownloadInfo;
import de.gemo.smartlauncher.universal.frames.StatusFrame;
import de.gemo.smartlauncher.universal.internet.HTTPAction;
import de.gemo.smartlauncher.universal.internet.HTTPListener;
import de.gemo.smartlauncher.universal.units.Logger;

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
                DownloadInfo downloadInfo = GameLauncher.getDownloadInfo();
                if (!downloadInfo.isDownloadMCJar() && downloadInfo.getLibraryCount() < 1) {
                    if (GameLauncher.prepareGame()) {
                        GameLauncher.startGame();
                    }
                }
            }
        } catch (Exception e) {
            this.onError(action);
        }
    }

    @Override
    public void onError(HTTPAction action) {
        GameLauncher.handleException(new Exception("Could not download asset '" + this.asset.getPath() + "'!"));
    }

    @Override
    public void onProgress(int maximumLength, int currentLength) {
        float percent = ((float) currentLength / (float) maximumLength) * 100;
        int percentInt = (int) (percent * 100);
        percent = percentInt / 100f;
        StatusFrame.INSTANCE.setProgress((int) percent);
    }
}
