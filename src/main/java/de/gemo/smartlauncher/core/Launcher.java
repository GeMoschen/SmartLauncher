package de.gemo.smartlauncher.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.eclipsesource.json.JsonObject;

import de.gemo.smartlauncher.core.minecraft.MinecraftProcess;
import de.gemo.smartlauncher.frames.LogFrame;
import de.gemo.smartlauncher.frames.MainFrame;
import de.gemo.smartlauncher.frames.StatusFrame;
import de.gemo.smartlauncher.internet.DownloadAction;
import de.gemo.smartlauncher.internet.Worker;
import de.gemo.smartlauncher.listener.GetSinglePackListener;
import de.gemo.smartlauncher.listener.MCJsonDownloadListener;
import de.gemo.smartlauncher.units.Asset;
import de.gemo.smartlauncher.units.AuthData;
import de.gemo.smartlauncher.units.DownloadInfo;
import de.gemo.smartlauncher.units.Library;
import de.gemo.smartlauncher.units.Pack;
import de.gemo.smartlauncher.units.PackInfo;
import de.gemo.smartlauncher.units.VARS;

public class Launcher {

    public static Launcher INSTANCE;
    private boolean error = false;

    public static PackInfo getPackInfo() {
        return INSTANCE.packInfo;
    }

    public static DownloadInfo getDownloadInfo() {
        return INSTANCE.downloadInfo;
    }

    private final PackInfo packInfo;
    private final DownloadInfo downloadInfo;

    public Launcher(Pack pack, String packVersion) {
        INSTANCE = this;
        this.downloadInfo = new DownloadInfo();
        packVersion = packVersion.replaceAll(" - recommended", "").trim();
        this.packInfo = new PackInfo(packVersion, pack);
        LogFrame.create();
        this.launch();
    }

    public void launch() {
        try {
            if (this.checkPack() && Launcher.checkFiles() && Launcher.prepareGame() && Launcher.startGame()) {
                // simply do nothing...
            }
        } catch (Exception e) {
            Launcher.handleException(e);
        }
    }

    public static void handleException(Exception e) {
        Launcher.onError();
        Logger.error(e.getMessage());
        JOptionPane.showMessageDialog(null, "Could not start Minecraft...\n\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    private boolean checkPack() {
        // reset old data...
        Launcher.clearAll();

        // SOME INFO
        StatusFrame.INSTANCE.showFrame(true);
        MainFrame.INSTANCE.showFrame(false);
        StatusFrame.INSTANCE.setText("Preparing launch...");

        // start...
        AuthData authData = Main.authData;
        GetSinglePackListener listener = new GetSinglePackListener();
        File packJson = new File(VARS.DIR.PROFILES + "/" + authData.getMCUserName() + "/" + this.packInfo.getPackName() + "/" + this.packInfo.getPackVersion() + "/pack.json");
        File packFile = new File(VARS.DIR.PACKS + "/" + this.packInfo.getPackName(), this.packInfo.getPackName() + "-" + this.packInfo.getPackVersion() + ".zip");
        String packURL = (VARS.URL.PACKSERVER + "packs/" + this.packInfo.getPackName() + "/" + this.packInfo.getPackName() + "-" + this.packInfo.getPackVersion() + ".zip");
        if (!packJson.exists() || !packFile.exists()) {
            if (!packFile.exists()) {
                StatusFrame.INSTANCE.setText("downloading packfile...");
                Logger.info("Packfile is missing...");
                Main.appendWorker(new Worker(new DownloadAction(packURL, VARS.DIR.PACKS + "/" + this.packInfo.getPackName(), this.packInfo.getPackName() + "-" + this.packInfo.getPackVersion() + ".zip"), listener));
                Main.startThread();
                return false;
            } else {
                if (!this.packInfo.getPack().extractPack()) {
                    StatusFrame.INSTANCE.setText("downloading packfile...");
                    Logger.info("Packfile is invalid...");
                    Main.appendWorker(new Worker(new DownloadAction(packURL, VARS.DIR.PACKS + "/" + this.packInfo.getPackName(), this.packInfo.getPackName() + "-" + this.packInfo.getPackVersion() + ".zip"), listener));
                    Main.startThread();
                    return false;
                } else {
                    Logger.fine("Packfile is valid...");
                    return true;
                }
            }
        } else {
            if (!this.packInfo.getPack().handlePackJson(packJson)) {
                Logger.info("pack.json is invalid... redownloading pack...");
                Main.appendWorker(new Worker(new DownloadAction(packURL, VARS.DIR.PACKS + "/" + this.packInfo.getPackName(), this.packInfo.getPackName() + "-" + this.packInfo.getPackVersion() + ".zip"), listener));
                Main.startThread();
                return false;
            } else {
                Logger.fine("Pack is valid...");
                return true;
            }
        }
    }

    public static boolean checkFiles() {
        File versionFile = new File(VARS.DIR.VERSIONS + "/" + Launcher.INSTANCE.packInfo.getGameVersion() + "/", Launcher.INSTANCE.packInfo.getGameVersion() + ".json");
        MCJsonDownloadListener listener = new MCJsonDownloadListener(Launcher.INSTANCE.packInfo.getGameVersion() + ".json");
        if (!versionFile.exists()) {
            Main.appendWorker(new Worker(new DownloadAction(VARS.getString(VARS.URL.JSON.MC_VERSIONS, Launcher.INSTANCE.packInfo), VARS.DIR.VERSIONS + "/" + Launcher.INSTANCE.packInfo.getGameVersion() + "/", Launcher.INSTANCE.packInfo.getGameVersion() + ".json"), listener));
            Main.startThread();
            return false;
        } else {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(versionFile));
                JsonObject json = JsonObject.readFrom(reader);
                reader.close();

                if (listener.readJson(json)) {
                    Logger.info("Some files are missing...");
                    Main.startThread();
                    return false;
                } else {
                    Logger.fine("All needed files are downloaded...");
                    return true;
                }
            } catch (Exception e) {
                Main.appendWorker(new Worker(new DownloadAction(VARS.getString(VARS.URL.JSON.MC_VERSIONS, Launcher.INSTANCE.packInfo), VARS.DIR.VERSIONS + "/" + Launcher.INSTANCE.packInfo.getGameVersion() + "/", Launcher.INSTANCE.packInfo.getGameVersion() + ".json"), listener));
                Main.startThread();
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
        ArrayList<String> cmd = new ArrayList<String>();

        // standard...
        cmd.add("java");
        cmd.add("-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump");

        // some extra-info
        cmd.add("-Xmx1G");
        cmd.add("-XX:PermSize=128m");

        // append nativesdir...
        cmd.add("-Djava.library.path=\"" + Launcher.INSTANCE.packInfo.getNativesDir().getAbsolutePath() + "\"");

        // append classpath
        cmd.add("-cp");
        cmd.add(Launcher.INSTANCE.createClasspathArgument());

        // append mainclass
        cmd.add(Launcher.INSTANCE.packInfo.getMainClass());

        // append arguments needed by minecraft...
        cmd.addAll(Launcher.INSTANCE.packInfo.getMCArguments());

        // ... and finally, try to launch...
        try {
            if (!Launcher.INSTANCE.error) {
                String fullCMD = "";
                for (String cm : cmd) {
                    fullCMD += cm + " ";
                }

                Logger.info("Starting Minecraft...");
                StatusFrame.INSTANCE.setText("Starting Minecraft...");
                Logger.info(fullCMD);
                Process process = new ProcessBuilder(cmd).directory(Launcher.INSTANCE.packInfo.getGameDir()).redirectErrorStream(true).start();
                if (process != null) {
                    Logger.fine("Minecraft started!");
                    new MinecraftProcess(process);
                } else {
                    // clear all...
                    Launcher.onError();

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
            Launcher.handleException(new Exception("Error starting game!"));
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
            Launcher.handleException(new Exception("Error unpacking natives!"));
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
        Main.clearHTTPs();
    }

    public static void onError() {
        Launcher.clearAll();
        INSTANCE.error = true;
    }
}
