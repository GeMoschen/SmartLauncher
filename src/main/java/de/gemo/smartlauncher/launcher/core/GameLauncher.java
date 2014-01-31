package de.gemo.smartlauncher.launcher.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.eclipsesource.json.JsonObject;

import de.gemo.smartlauncher.launcher.core.minecraft.MinecraftProcess;
import de.gemo.smartlauncher.launcher.frames.MainFrame;
import de.gemo.smartlauncher.launcher.listener.GetSinglePackListener;
import de.gemo.smartlauncher.launcher.listener.MCJsonDownloadListener;
import de.gemo.smartlauncher.launcher.units.Asset;
import de.gemo.smartlauncher.launcher.units.AuthData;
import de.gemo.smartlauncher.launcher.units.DownloadInfo;
import de.gemo.smartlauncher.launcher.units.Library;
import de.gemo.smartlauncher.launcher.units.Pack;
import de.gemo.smartlauncher.launcher.units.PackInfo;
import de.gemo.smartlauncher.universal.frames.LogFrame;
import de.gemo.smartlauncher.universal.frames.StatusFrame;
import de.gemo.smartlauncher.universal.internet.DownloadAction;
import de.gemo.smartlauncher.universal.internet.Worker;
import de.gemo.smartlauncher.universal.units.Logger;
import de.gemo.smartlauncher.universal.units.ThreadHolder;
import de.gemo.smartlauncher.universal.units.VARS;

public class GameLauncher {

    public static GameLauncher INSTANCE;
    private static boolean showConsole = false;
    private static boolean closeOnStart = false;
    private static int minRam = 512, maxRam = 1024, permGen = 128;

    private boolean error = false;

    public static PackInfo getPackInfo() {
        return INSTANCE.packInfo;
    }

    public static DownloadInfo getDownloadInfo() {
        return INSTANCE.downloadInfo;
    }

    private final PackInfo packInfo;
    private final DownloadInfo downloadInfo;

    public GameLauncher(Pack pack, String packVersion) {
        INSTANCE = this;
        this.downloadInfo = new DownloadInfo();
        packVersion = packVersion.replaceAll(" - recommended", "").trim();
        this.packInfo = new PackInfo(packVersion, pack);
        this.loadSettings();

        if (showConsole) {
            LogFrame.create();
        }
        this.launch();
    }

    private void loadSettings() {
        try {
            File file = new File(VARS.DIR.PROFILES + "/" + Launcher.authData.getMCUserName(), getPackInfo().getPackName() + ".json");
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                JsonObject json = JsonObject.readFrom(reader);
                showConsole = json.get("showConsole").asBoolean();
                closeOnStart = json.get("closeOnStart").asBoolean();
                minRam = json.get("minRAM").asInt();
                maxRam = json.get("maxRAM").asInt();
                permGen = json.get("permGen").asInt();
                reader.close();
            }
        } catch (Exception e) {
            showConsole = false;
            permGen = 128;
            minRam = 512;
            maxRam = 1024;
            e.printStackTrace();
        }
    }

    public void launch() {
        try {
            if (this.checkPack() && GameLauncher.checkFiles() && GameLauncher.prepareGame() && GameLauncher.startGame()) {
                // simply do nothing...
            }
        } catch (Exception e) {
            GameLauncher.handleException(e);
        }
    }

    public static void handleException(Exception e) {
        GameLauncher.onError();
        Logger.error(e.getMessage());
        JOptionPane.showMessageDialog(null, "Could not start Minecraft...\n\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    private boolean checkPack() {
        // reset old data...
        GameLauncher.clearAll();

        // SOME INFO
        StatusFrame.INSTANCE.showFrame(true);
        MainFrame.INSTANCE.showFrame(false);
        StatusFrame.INSTANCE.setText("Preparing launch...");

        // start...
        AuthData authData = Launcher.authData;
        GetSinglePackListener listener = new GetSinglePackListener();
        File packJson = new File(VARS.DIR.PROFILES + "/" + authData.getMCUserName() + "/" + this.packInfo.getPackName() + "/" + this.packInfo.getPackVersion() + "/pack.json");
        File packFile = new File(VARS.DIR.PACKS + "/" + this.packInfo.getPackName(), this.packInfo.getPackName() + "-" + this.packInfo.getPackVersion() + ".zip");
        String packURL = (VARS.URL.PACKSERVER + "packs/" + this.packInfo.getPackName() + "/" + this.packInfo.getPackName() + "-" + this.packInfo.getPackVersion() + ".zip");
        if (!packJson.exists() || !packFile.exists()) {
            if (!packFile.exists()) {
                StatusFrame.INSTANCE.setText("downloading packfile...");
                Logger.info("Packfile is missing...");
                ThreadHolder.appendWorker(new Worker(new DownloadAction(packURL, VARS.DIR.PACKS + "/" + this.packInfo.getPackName(), this.packInfo.getPackName() + "-" + this.packInfo.getPackVersion() + ".zip"), listener));
                ThreadHolder.startThread();
                return false;
            } else {
                if (!this.packInfo.getPack().extractPack()) {
                    StatusFrame.INSTANCE.setText("downloading packfile...");
                    Logger.info("Packfile is invalid...");
                    ThreadHolder.appendWorker(new Worker(new DownloadAction(packURL, VARS.DIR.PACKS + "/" + this.packInfo.getPackName(), this.packInfo.getPackName() + "-" + this.packInfo.getPackVersion() + ".zip"), listener));
                    ThreadHolder.startThread();
                    return false;
                } else {
                    Logger.fine("Packfile is valid...");
                    return true;
                }
            }
        } else {
            if (!this.packInfo.getPack().handlePackJson(packJson)) {
                Logger.info("pack.json is invalid... redownloading pack...");
                ThreadHolder.appendWorker(new Worker(new DownloadAction(packURL, VARS.DIR.PACKS + "/" + this.packInfo.getPackName(), this.packInfo.getPackName() + "-" + this.packInfo.getPackVersion() + ".zip"), listener));
                ThreadHolder.startThread();
                return false;
            } else {
                Logger.fine("Pack is valid...");
                return true;
            }
        }
    }

    public static boolean checkFiles() {
        File versionFile = new File(VARS.DIR.VERSIONS + "/" + GameLauncher.INSTANCE.packInfo.getGameVersion() + "/", GameLauncher.INSTANCE.packInfo.getGameVersion() + ".json");
        MCJsonDownloadListener listener = new MCJsonDownloadListener(GameLauncher.INSTANCE.packInfo.getGameVersion() + ".json");
        if (!versionFile.exists()) {
            ThreadHolder.appendWorker(new Worker(new DownloadAction(VARS.getString(VARS.URL.JSON.MC_VERSIONS, GameLauncher.INSTANCE.packInfo), VARS.DIR.VERSIONS + "/" + GameLauncher.INSTANCE.packInfo.getGameVersion() + "/", GameLauncher.INSTANCE.packInfo.getGameVersion() + ".json"), listener));
            ThreadHolder.startThread();
            return false;
        } else {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(versionFile));
                JsonObject json = JsonObject.readFrom(reader);
                reader.close();

                if (listener.readJson(json)) {
                    Logger.info("Some files are missing...");
                    ThreadHolder.startThread();
                    return false;
                } else {
                    Logger.fine("All needed files are downloaded...");
                    return true;
                }
            } catch (Exception e) {
                ThreadHolder.appendWorker(new Worker(new DownloadAction(VARS.getString(VARS.URL.JSON.MC_VERSIONS, GameLauncher.INSTANCE.packInfo), VARS.DIR.VERSIONS + "/" + GameLauncher.INSTANCE.packInfo.getGameVersion() + "/", GameLauncher.INSTANCE.packInfo.getGameVersion() + ".json"), listener));
                ThreadHolder.startThread();
                return false;
            }
        }
    }

    public static boolean prepareGame() {
        if (!INSTANCE.error) {
            // some output...
            StatusFrame.INSTANCE.showFrame(true);
            Logger.info("Preparing launch...");

            // extract libraries...
            StatusFrame.INSTANCE.setText("Extracting libraries...");
            Logger.info("Extracting libraries...");
            if (!INSTANCE.extractLibraries()) {
                return false;
            }

            // reconstruct assets...
            StatusFrame.INSTANCE.setText("Reconstructing assets...");
            Logger.info("Reconstructing assets...");
            return INSTANCE.packInfo.reconstructAssets();
        } else {
            // show GUIs
            StatusFrame.INSTANCE.showFrame(false);
            MainFrame.INSTANCE.showFrame(true);
            return false;
        }
    }

    public static boolean startGame() {
        // create commands...
        ArrayList<String> cmd = new ArrayList<String>();

        // standard...
        cmd.add("java");
        cmd.add("-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump");

        // some extra-info
        cmd.add("-Xms" + minRam + "m");
        cmd.add("-Xmx" + maxRam + "m");
        cmd.add("-XX:PermSize=" + permGen + "m");

        // append nativesdir...
        cmd.add("-Djava.library.path=\"" + GameLauncher.INSTANCE.packInfo.getNativesDir().getAbsolutePath() + "\"");

        // append classpath
        cmd.add("-cp");
        cmd.add(GameLauncher.INSTANCE.createClasspathArgument());

        // append mainclass
        cmd.add(GameLauncher.INSTANCE.packInfo.getMainClass());

        // append arguments needed by minecraft...
        cmd.addAll(GameLauncher.INSTANCE.packInfo.getMCArguments());

        // override some vars...
        String gameDir = "\"" + getPackInfo().getGameDir().getAbsolutePath() + "\"";
        System.setProperty("user.home", gameDir);
        System.setProperty("minecraft.applet.TargetDirectory", gameDir);

        // ... and finally, try to launch...
        try {
            if (!GameLauncher.INSTANCE.error) {
                String fullCMD = "";
                for (String cm : cmd) {
                    fullCMD += cm + " ";
                }

                Logger.info("Starting Minecraft...");
                StatusFrame.INSTANCE.setText("Starting Minecraft...");
                Logger.info(fullCMD);
                Process process = new ProcessBuilder(cmd).directory(GameLauncher.INSTANCE.packInfo.getGameDir()).redirectErrorStream(true).start();
                if (process != null) {
                    Logger.fine("Minecraft started!");
                    new MinecraftProcess(process);
                    // close?
                    if (closeOnStart) {
                        System.exit(0);
                    }
                } else {
                    // clear all...
                    GameLauncher.onError();

                    // show GUIs
                    StatusFrame.INSTANCE.showFrame(false);
                    MainFrame.INSTANCE.showFrame(true);

                    // some output...
                    Logger.error("Could not start Minecraft!");
                    JOptionPane.showMessageDialog(null, "Could not start Minecraft...", "Error", JOptionPane.ERROR_MESSAGE);
                }
                return process != null;
            } else {
                // show GUIs
                StatusFrame.INSTANCE.showFrame(false);
                MainFrame.INSTANCE.showFrame(true);
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            GameLauncher.handleException(new Exception("Error starting game!"));
            return false;
        }
    }

    private boolean extractLibraries() {
        try {
            ArrayList<Library> libraryList = Library.getLibraryList();
            for (Library library : libraryList) {
                // only extract...
                if (!library.isAllow() || !library.isExtract()) {
                    continue;
                }
                this.packInfo.unpackNatives(library);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            StatusFrame.INSTANCE.showFrame(false);
            MainFrame.INSTANCE.showFrame(true);
            GameLauncher.handleException(new Exception("Error unpacking natives!"));
            return false;
        }
    }

    private String createClasspathArgument() {
        File libraryDir = new File(VARS.DIR.LIBRARIES, "");

        String libraries = "\"";
        // append all libraries...
        ArrayList<Library> libraryList = Library.getLibraryList();
        for (int index = 1; index <= libraryList.size(); index++) {
            Library library = libraryList.get(index - 1);
            // ... but only if they are used and are not natives...
            if (!library.isAllow() || library.isExtract()) {
                continue;
            }
            libraries += libraryDir.getAbsolutePath() + "\\" + library.getStartFile() + ";";
        }

        // append minecraft.jar
        libraries += (VARS.DIR.VERSIONS + "\\" + this.packInfo.getGameVersion() + "\\" + this.packInfo.getGameVersion() + ".jar");

        // replace "/" with "\"...
        libraries += "\"";
        libraries = libraries.replaceAll("/", "\\\\");
        return libraries;
    }

    public static void clearAll() {
        // clear all...
        Asset.reset();
        Library.clearLibrarys();
        ThreadHolder.clearHTTPs();
    }

    public static void onError() {
        if (INSTANCE != null) {
            GameLauncher.clearAll();
            INSTANCE.error = true;
        }
    }
}
