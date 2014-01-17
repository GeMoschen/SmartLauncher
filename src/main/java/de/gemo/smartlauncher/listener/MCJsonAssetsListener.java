package de.gemo.smartlauncher.listener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.eclipsesource.json.JsonObject;

import de.gemo.smartlauncher.core.Launcher;
import de.gemo.smartlauncher.core.Logger;
import de.gemo.smartlauncher.core.Main;
import de.gemo.smartlauncher.frames.MainFrame;
import de.gemo.smartlauncher.frames.StatusFrame;
import de.gemo.smartlauncher.internet.DownloadAction;
import de.gemo.smartlauncher.internet.HTTPAction;
import de.gemo.smartlauncher.internet.HTTPListener;
import de.gemo.smartlauncher.internet.Worker;
import de.gemo.smartlauncher.units.Asset;
import de.gemo.smartlauncher.units.Library;
import de.gemo.smartlauncher.units.VARS;

public class MCJsonAssetsListener extends HTTPListener {

    @Override
    public void onStart(HTTPAction action) {
        StatusFrame.INSTANCE.showGUI(true);
        StatusFrame.INSTANCE.setText("downloading assets...");
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
            Logger.fine("Assetindex downloaded: " + Launcher.getGameInfo().getAssetVersion() + ".json");
            try {
                BufferedReader reader = new BufferedReader(new FileReader(new File(VARS.DIR.ASSETS + "/indexes/" + Launcher.getGameInfo().getAssetVersion() + ".json")));
                JsonObject json = JsonObject.readFrom(reader);
                reader.close();

                // get assetsfile...
                // boolean virtual = false;
                // JsonValue virtualValue = json.get("virtual");
                // if (virtualValue != null) {
                // virtual = virtualValue.asBoolean();
                // }

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
                        Main.appendWorker(new Worker(new DownloadAction(VARS.URL.FILES.ASSETS + asset.getURL(), VARS.DIR.ASSETS + "/objects/" + asset.getHashFolder(), asset.getHash()), new MCDownloadAssetListener(asset)));
                    }
                }
                if (Asset.getAssetsToLoad() > 0) {
                    Main.startThread();
                } else {
                    Logger.fine("No need to download assets...");
                    // if (Library.getLibraryDownloadList().size() < 1) {
                    // Logger.info("All needed libraries downloaded...");
                    // Launcher.startGame();
                    // StatusFrame.INSTANCE.showGUI(false);
                    // MainFrame.CORE.showFrame(true);
                    // }
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
        Asset.reset();
        Library.clearLibrarys();
        Main.clearHTTPs();
        JOptionPane.showMessageDialog(null, "Could not start Minecraft...", "Error", JOptionPane.ERROR_MESSAGE);
        StatusFrame.INSTANCE.showGUI(false);
        MainFrame.CORE.showFrame(true);
    }

    @Override
    public void onProgress(int maximumLength, int currentLength) {
        // float percent = Float.valueOf(decimalFormat.format((float) (((float)
        // currentLength / (float) maximumLength) * 10000f)));
        // StatusFrame.INSTANCE.setProgress((int) percent);
    }
}
