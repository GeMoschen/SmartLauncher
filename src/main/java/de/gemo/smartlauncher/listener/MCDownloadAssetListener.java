package de.gemo.smartlauncher.listener;

import java.net.HttpURLConnection;

import javax.swing.JOptionPane;

import de.gemo.smartlauncher.core.Logger;
import de.gemo.smartlauncher.core.Main;
import de.gemo.smartlauncher.frames.MainFrame;
import de.gemo.smartlauncher.frames.StatusFrame;
import de.gemo.smartlauncher.internet.HTTPAction;
import de.gemo.smartlauncher.internet.HTTPListener;
import de.gemo.smartlauncher.units.Asset;
import de.gemo.smartlauncher.units.Library;

public class MCDownloadAssetListener extends HTTPListener {

    private Asset asset;

    public MCDownloadAssetListener(Asset asset) {
        this.asset = asset;
    }

    @Override
    public void onStart(HTTPAction action) {
        StatusFrame.INSTANCE.setText("downloading '" + this.asset.getPath() + "'...");
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
            Logger.fine("Asset downloaded: " + this.asset.getPath());
            Asset.decrementAssetsToLoad();
            if (Asset.getAssetsToLoad() < 1) {
                Logger.fine("Assets successfully downloaded...");
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.onError(action);
        }
    }

    @Override
    public void onError(HTTPAction action) {
        Asset.reset();
        Library.clearLibrarys();
        Main.clearHTTPs();
        JOptionPane.showMessageDialog(null, "Could not start Minecraft...", "Error", JOptionPane.ERROR_MESSAGE);
        StatusFrame.INSTANCE.showGUI(false);
        MainFrame.CORE.showFrame(true);
    }

    @Override
    public void onProgress(int maximumLength, int currentLength) {
        float percent = ((float) currentLength / (float) maximumLength) * 100;
        int percentInt = (int) (percent * 100);
        percent = percentInt / 100f;
        StatusFrame.INSTANCE.setProgress((int) percent);
    }
}
