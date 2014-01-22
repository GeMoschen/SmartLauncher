package de.gemo.smartlauncher.launcher.listener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import com.eclipsesource.json.JsonObject;

import de.gemo.smartlauncher.launcher.core.GameLauncher;
import de.gemo.smartlauncher.launcher.core.Logger;
import de.gemo.smartlauncher.launcher.core.ThreadHolder;
import de.gemo.smartlauncher.launcher.units.Asset;
import de.gemo.smartlauncher.universal.frames.StatusFrame;
import de.gemo.smartlauncher.universal.internet.DownloadAction;
import de.gemo.smartlauncher.universal.internet.HTTPAction;
import de.gemo.smartlauncher.universal.internet.HTTPListener;
import de.gemo.smartlauncher.universal.internet.Worker;
import de.gemo.smartlauncher.universal.units.VARS;

public class MCJsonAssetsListener extends HTTPListener {

    @Override
    public void onStart(HTTPAction action) {
        StatusFrame.INSTANCE.setProgress(0);
        StatusFrame.INSTANCE.setText("downloading assets...");
        Logger.info("downloading assets.json...");
    }

    @Override
    public void onFinish(HTTPAction action) {
        if (action.getResponseCode() != HttpURLConnection.HTTP_OK) {
            this.onError(action);
            return;
        }

        try {
            StatusFrame.INSTANCE.setText("download finished...");
            Logger.fine("Assetindex downloaded: " + GameLauncher.getPackInfo().getAssetVersion() + ".json");
            try {
                BufferedReader reader = new BufferedReader(new FileReader(new File(VARS.DIR.ASSETS + "/indexes/" + GameLauncher.getPackInfo().getAssetVersion() + ".json")));
                JsonObject json = JsonObject.readFrom(reader);
                reader.close();

                ArrayList<Asset> assetsToDL = new ArrayList<Asset>();
                JsonObject objects = json.get("objects").asObject();
                for (String objectName : objects.names()) {
                    JsonObject singleAsset = objects.get(objectName).asObject();
                    assetsToDL.add(new Asset(objectName.replaceAll("\"", ""), singleAsset.get("hash").asString().replaceAll("\"", ""), singleAsset.get("size").asInt()));
                }
                // append assets to download...
                for (Asset asset : assetsToDL) {
                    if (!asset.isFileValid()) {
                        Asset.incrementAssetsToLoad();
                        ThreadHolder.appendWorker(new Worker(new DownloadAction(VARS.URL.FILES.ASSETS + asset.getURL(), VARS.DIR.ASSETS + "/objects/" + asset.getHashFolder(), asset.getHash()), new MCDownloadAssetListener(asset)));
                    }
                }
                if (Asset.getAssetsToLoad() > 0) {
                    ThreadHolder.startThread();
                } else {
                    Logger.fine("No need to download assets...");
                }
            } catch (Exception e) {
                e.printStackTrace();
                this.onError(action);
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.onError(action);
        }
    }

    @Override
    public void onError(HTTPAction action) {
        GameLauncher.handleException(new Exception("Could not download assets.json!"));
    }

    @Override
    public void onProgress(int maximumLength, int currentLength) {
        float percent = ((float) currentLength / (float) maximumLength) * 100;
        int percentInt = (int) (percent * 100);
        percent = percentInt / 100f;
        StatusFrame.INSTANCE.setProgress((int) percent);
    }
}
