package de.gemo.smartlauncher.listener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.HttpURLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import de.gemo.smartlauncher.core.DownloadInfo;
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

public class MCJsonDownloadListener extends HTTPListener {

    private static final DecimalFormat decimalFormat = new DecimalFormat("0,00");

    private String version;
    private String fileName;

    public MCJsonDownloadListener(String version, String fileName) {
        this.version = version;
        this.fileName = fileName;
    }

    @Override
    public void onStart(HTTPAction action) {
        Library.clearLibrarys();
        StatusFrame.INSTANCE.showGUI(true);
        StatusFrame.INSTANCE.setText("downloading '" + this.fileName + "'...");
        StatusFrame.INSTANCE.setProgress(0);
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
            DownloadInfo downloadInfo = Launcher.getDownloadInfo();
            Launcher.getGameInfo().setMainClass(json.get("mainClass").asString());
            Launcher.getGameInfo().setMCArguments(json.get("minecraftArguments").asString());

            // get assetsfile...
            String assetsFile = "legacy";
            JsonValue assetsValue = json.get("assets");
            if (assetsValue != null) {
                assetsFile = assetsValue.asString().replaceAll("\"", "");
            }
            Launcher.getGameInfo().setAssetVersion(assetsFile);

            // get libraries...
            ArrayList<Library> librariesToDL = new ArrayList<Library>();
            JsonArray jsonLib = json.get("libraries").asArray();
            JsonObject singleLibrary;
            for (JsonValue value : jsonLib.values()) {
                singleLibrary = value.asObject();
                Library lib = this.getLibrary(singleLibrary);

                // add the library, if it should be added...
                if (lib.addLibrary()) {
                    if (lib.addLibraryToDownloads()) {
                        librariesToDL.add(lib);
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
            downloadInfo.setLibraryCount(librariesToDL.size());
            if (downloadInfo.getLibraryCount() > 0) {
                for (Library library : librariesToDL) {
                    Main.appendWorker(new Worker(new DownloadAction(VARS.URL.FILES.LIBRARIES + library.getFullPath(), library.getDir(), library.getFileName()), new MCDownloadLibraryListener(library)));
                }
            } else {
                Logger.fine("Libraries are fine...");
            }

            // append minecraft.jar, if needed
            File mcJar = new File(VARS.DIR.VERSIONS + "/" + this.version + "/", this.version + ".jar");
            downloadInfo.setDownloadMCJar(!mcJar.exists());
            if (!mcJar.exists()) {
                Main.appendWorker(new Worker(new DownloadAction(VARS.getString(VARS.URL.FILES.MC_JAR, Launcher.getGameInfo()), VARS.DIR.VERSIONS + "/" + this.version + "/", this.version + ".jar"), new MCDownloadFileListener(this.version + ".jar")));
            } else {
                Logger.fine("Minecraft-JAR is fine...");
            }

            // start thread...
            return (downloadInfo.isDownloadAssetJSON() || downloadInfo.isDownloadMCJar() || downloadInfo.getLibraryCount() > 0);
        } catch (Exception e) {
            e.printStackTrace();
            // reset all...
            Asset.reset();
            Library.clearLibrarys();
            Main.clearHTTPs();
            JOptionPane.showMessageDialog(null, "Could not start Minecraft...", "Error", JOptionPane.ERROR_MESSAGE);
            StatusFrame.INSTANCE.showGUI(false);
            MainFrame.CORE.showFrame(true);
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
                    Launcher.startGame();
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

    private Library getLibrary(JsonObject singleLibrary) throws Exception {
        JsonObject jsonObject;
        JsonArray jsonArray;
        JsonValue jsonValue;
        Library library = new Library(singleLibrary.get("name").asString());

        // natives...
        jsonValue = singleLibrary.get("natives");
        if (jsonValue != null) {
            jsonObject = jsonValue.asObject();
            JsonValue nativeWindows = jsonObject.get("windows");
            if (nativeWindows != null) {
                String str_win = nativeWindows.asString();
                if (str_win != null) {
                    library.setNative(str_win);
                }
            }
        }

        // extract...
        jsonValue = singleLibrary.get("extract");
        if (jsonValue != null) {
            jsonObject = jsonValue.asObject();
            jsonValue = jsonObject.get("exclude");
            if (jsonValue != null) {
                jsonArray = jsonValue.asArray();
                for (JsonValue value : jsonArray) {
                    library.addExtractExclude(value.asString());
                }
            }
            library.setExtract(true);
        } else {
            library.setExtract(false);
        }

        // rules...
        jsonValue = singleLibrary.get("rules");
        if (jsonValue != null) {
            jsonArray = jsonValue.asArray();
            boolean allowWindows = false;

            for (JsonValue val : jsonArray.values()) {
                JsonObject obj = val.asObject();
                boolean allow = !obj.get("action").asString().contains("disallow");
                JsonValue osValue = obj.get("os");
                if (allow) {
                    if (osValue == null) {
                        allowWindows = true;
                    } else {
                        JsonObject osObj = osValue.asObject();
                        if (osObj.get("name").asString().contains("windows")) {
                            allowWindows = true;
                        }
                    }
                } else {
                    if (osValue != null) {
                        JsonObject osObj = osValue.asObject();
                        if (osObj.get("name").asString().contains("windows")) {
                            allowWindows = false;
                        }
                    } else {
                        allowWindows = false;
                    }
                }
                if (allowWindows == false) {
                    library.setAllow(allowWindows);
                    break;
                }
            }
        }
        return library;
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
        float percent = Float.valueOf(decimalFormat.format((float) (((float) currentLength / (float) maximumLength) * 10000f)));
        StatusFrame.INSTANCE.setProgress((int) percent);
    }
}
