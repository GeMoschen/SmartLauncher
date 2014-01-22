package de.gemo.smartlauncher.launcher.listener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.HttpURLConnection;

import javax.swing.JOptionPane;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import de.gemo.smartlauncher.launcher.core.GameLauncher;
import de.gemo.smartlauncher.launcher.core.Logger;
import de.gemo.smartlauncher.launcher.core.Main;
import de.gemo.smartlauncher.launcher.frames.MainFrame;
import de.gemo.smartlauncher.launcher.units.Asset;
import de.gemo.smartlauncher.launcher.units.DownloadInfo;
import de.gemo.smartlauncher.launcher.units.Library;
import de.gemo.smartlauncher.universal.frames.StatusFrame;
import de.gemo.smartlauncher.universal.internet.DownloadAction;
import de.gemo.smartlauncher.universal.internet.HTTPAction;
import de.gemo.smartlauncher.universal.internet.HTTPListener;
import de.gemo.smartlauncher.universal.internet.Worker;
import de.gemo.smartlauncher.universal.units.VARS;

public class MCJsonDownloadListener extends HTTPListener {

    private String version;
    private String fileName;

    public MCJsonDownloadListener(String fileName) {
        this.version = GameLauncher.getPackInfo().getGameVersion();
        this.fileName = fileName;
    }

    @Override
    public void onStart(HTTPAction action) {
        StatusFrame.INSTANCE.setProgress(0);
        StatusFrame.INSTANCE.setText("downloading '" + this.fileName + "'...");
        Logger.info("downloading '" + this.fileName + "'...");
    }

    /**
     * Read the json...
     * 
     * @param json
     * @return <b>true</b>, if there are some files missing.otherwise
     *         <b>false</b>
     */
    public boolean readJson(JsonObject json) {
        try {
            // get downloadinfo...
            DownloadInfo downloadInfo = GameLauncher.getDownloadInfo();
            GameLauncher.getPackInfo().setMainClass(json.get("mainClass").asString());
            GameLauncher.getPackInfo().setMCArguments(json.get("minecraftArguments").asString());

            // get assetsfile...
            String assetsFile = "legacy";
            JsonValue assetsValue = json.get("assets");
            if (assetsValue != null) {
                assetsFile = assetsValue.asString().replaceAll("\"", "");
            }
            GameLauncher.getPackInfo().setAssetVersion(assetsFile);

            // get libraries...

            JsonValue libValue = json.get("libraries");
            if (libValue != null) {
                JsonArray jsonLib = libValue.asArray();
                JsonObject singleLibrary;
                for (JsonValue value : jsonLib.values()) {
                    singleLibrary = value.asObject();
                    Library lib = new Library(singleLibrary);

                    // add the library, if it should be added...
                    if (lib.addLibrary()) {
                        lib.addLibraryToDownloads();
                    }
                }
            }

            // append assets.json, if needed...
            File assets = new File(VARS.DIR.ASSETS + "/indexes/", assetsFile + ".json");
            downloadInfo.setDownloadAssetJSON(!assets.exists() || !this.verifyAssets(assets));
            if (downloadInfo.isDownloadAssetJSON()) {
                Main.appendWorker(new Worker(new DownloadAction(VARS.getString(VARS.URL.JSON.MC_ASSETS, "version", assetsFile), VARS.DIR.ASSETS + "/indexes/", assetsFile + ".json"), new MCJsonAssetsListener()));
            } else {
                Logger.fine("Assets are fine...");
            }

            // append libraries,if needed...
            downloadInfo.setLibraryCount(Library.getLibraryDownloadList().size());
            if (downloadInfo.getLibraryCount() > 0) {
                for (Library library : Library.getLibraryDownloadList()) {
                    Main.appendWorker(new Worker(new DownloadAction(library.getURL() + library.getFullPath(), library.getDir(), library.getFileName()), new MCDownloadLibraryListener(library)));
                }
            } else {
                Logger.fine("Libraries are fine...");
            }

            // append minecraft.jar, if needed
            File mcJar = new File(VARS.DIR.VERSIONS + "/" + this.version + "/", this.version + ".jar");
            downloadInfo.setDownloadMCJar(!mcJar.exists());
            if (!mcJar.exists()) {
                Main.appendWorker(new Worker(new DownloadAction(VARS.getString(VARS.URL.FILES.MC_JAR, GameLauncher.getPackInfo()), VARS.DIR.VERSIONS + "/" + this.version + "/", this.version + ".jar"), new MCDownloadFileListener(this.version + ".jar")));
            } else {
                Logger.fine("Minecraft-JAR is fine...");
            }

            // start thread...
            return (downloadInfo.isDownloadAssetJSON() || downloadInfo.isDownloadMCJar() || downloadInfo.getLibraryCount() > 0);
        } catch (Exception e) {
            e.printStackTrace();

            // reset all...
            GameLauncher.onError();

            StatusFrame.INSTANCE.showFrame(false);
            MainFrame.INSTANCE.showFrame(true);
            JOptionPane.showMessageDialog(null, "Could not start Minecraft... 3", "Error", JOptionPane.ERROR_MESSAGE);
            return true;
        }
    }

    private boolean verifyAssets(File assets) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(assets));
            JsonObject json = JsonObject.readFrom(reader);
            reader.close();

            // iterate over assets and check, if they are valid...
            JsonObject objects = json.get("objects").asObject();
            for (String objectName : objects.names()) {
                JsonObject singleAsset = objects.get(objectName).asObject();
                Asset asset = new Asset(objectName.replaceAll("\"", ""), singleAsset.get("hash").asString().replaceAll("\"", ""), singleAsset.get("size").asInt());
                if (!asset.isFileValid()) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onFinish(HTTPAction action) {
        if (action.getResponseCode() != HttpURLConnection.HTTP_OK) {
            this.onError(action);
            return;
        }

        try {
            StatusFrame.INSTANCE.setText("download finished...");
            Logger.fine("Versionfile downloaded: " + this.version + ".json");
            try {

                // read json...
                BufferedReader reader = new BufferedReader(new FileReader(new File(VARS.DIR.VERSIONS + "/" + version + "/" + this.fileName)));
                JsonObject json = JsonObject.readFrom(reader);
                reader.close();

                // if there are files to download...
                if (this.readJson(json)) {
                    // ... start the download
                    Main.startThread();
                } else {
                    Logger.fine("All needed files are downloaded...");
                    if (GameLauncher.prepareGame()) {
                        GameLauncher.startGame();
                    }
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
        GameLauncher.handleException(new Exception("Could not download version.json!"));
    }

    @Override
    public void onProgress(int maximumLength, int currentLength) {
        float percent = ((float) currentLength / (float) maximumLength) * 100;
        int percentInt = (int) (percent * 100);
        percent = percentInt / 100f;
        StatusFrame.INSTANCE.setProgress((int) percent);
    }
}
