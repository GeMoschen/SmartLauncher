package de.gemo.smartlauncher.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.eclipsesource.json.JsonObject;

import de.gemo.smartlauncher.actions.GetSinglePackAction;
import de.gemo.smartlauncher.core.minecraft.MinecraftProcess;
import de.gemo.smartlauncher.frames.MainFrame;
import de.gemo.smartlauncher.frames.StatusFrame;
import de.gemo.smartlauncher.internet.DownloadAction;
import de.gemo.smartlauncher.internet.Worker;
import de.gemo.smartlauncher.listener.GetSinglePackListener;
import de.gemo.smartlauncher.listener.MCJsonDownloadListener;
import de.gemo.smartlauncher.units.Asset;
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

    private final Pack pack;
    private final PackInfo packInfo;
    private final DownloadInfo downloadInfo;

    public Launcher(Pack pack, String packVersion) {
        INSTANCE = this;
        this.pack = pack;
        this.downloadInfo = new DownloadInfo();
        packVersion = packVersion.replaceAll(" - recommended", "").trim();
        this.packInfo = new PackInfo(packVersion, this.pack);
        this.downloadPack();
    }

    private void downloadPack() {
        // reset old data...
        Asset.reset();
        Library.clearLibrarys();
        Main.clearHTTPs();

        // start...
        Main.appendWorker(new Worker(new GetSinglePackAction(this.pack, this.packInfo.getPackVersion()), new GetSinglePackListener()));
        Main.startThread();
        StatusFrame.INSTANCE.showGUI(true);
        MainFrame.INSTANCE.showFrame(false);
    }

    public void launchPack() {
        StatusFrame.INSTANCE.setText("Preparing download...");
        StatusFrame.INSTANCE.showGUI(true);
        File versionFile = new File(VARS.DIR.VERSIONS + "/" + this.packInfo.getGameVersion() + "/", this.packInfo.getGameVersion() + ".json");
        MCJsonDownloadListener listener = new MCJsonDownloadListener(this.packInfo.getGameVersion() + ".json");
        if (!versionFile.exists()) {
            Main.appendWorker(new Worker(new DownloadAction(VARS.getString(VARS.URL.JSON.MC_VERSIONS, packInfo), VARS.DIR.VERSIONS + "/" + this.packInfo.getGameVersion() + "/", this.packInfo.getGameVersion() + ".json"), listener));
            Main.startThread();
        } else {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(versionFile));
                JsonObject json = JsonObject.readFrom(reader);
                reader.close();

                if (listener.readJson(json)) {
                    Logger.fine("Some files are missing...");
                    Main.startThread();
                } else {
                    Logger.fine("All needed files are downloaded...");
                    Launcher.startGame();
                }

            } catch (Exception e) {
                Main.appendWorker(new Worker(new DownloadAction(VARS.getString(VARS.URL.JSON.MC_VERSIONS, packInfo), VARS.DIR.VERSIONS + "/" + this.packInfo.getGameVersion() + "/", this.packInfo.getGameVersion() + ".json"), listener));
                Main.startThread();
            }
        }
    }

    private void extractLibraries() throws IOException {
        ArrayList<Library> libraryList = Library.getLibraryList();
        for (Library library : libraryList) {
            // only extract...
            if (!library.isAllow() || !library.isExtract()) {
                continue;
            }

            this.packInfo.unpackNatives(library);
        }
    }

    public static void startGame() throws IOException {
        if (!INSTANCE.error) {
            // some output...
            Logger.fine("Preparing launch...");

            // clear http...
            Main.clearHTTPs();

            // extract libraries...
            StatusFrame.INSTANCE.setText("Extracting libraries...");
            Logger.fine("Extracting libraries...");
            INSTANCE.extractLibraries();

            // reconstruct assets...
            StatusFrame.INSTANCE.setText("Reconstructing assets...");
            Logger.fine("Reconstructing assets...");
            if (INSTANCE.packInfo.reconstructAssets()) {
                // ... and finally start minecraft
                StatusFrame.INSTANCE.setText("Starting game...");
                INSTANCE.launchGame();
            }
        } else {
            // show GUIs
            StatusFrame.INSTANCE.showGUI(false);
            MainFrame.INSTANCE.showFrame(true);
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

    private void launchGame() {
        ArrayList<String> cmd = new ArrayList<String>();

        // standard...
        cmd.add("java");
        cmd.add("-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump");

        // some extra-info
        cmd.add("-Xmx2G");

        // append nativesdir...
        cmd.add("-Djava.library.path=\"" + this.packInfo.getNativesDir().getAbsolutePath() + "\"");

        // append classpath
        cmd.add("-cp");
        cmd.add(this.createClasspathArgument());

        // append mainclass
        cmd.add(this.packInfo.getMainClass());

        // append arguments needed by minecraft...
        cmd.addAll(this.packInfo.getMCArguments());

        // ... and finally, try to launch...
        try {
            if (!this.error) {
                String fullCMD = "";
                for (String cm : cmd) {
                    fullCMD += cm + " ";
                }

                Logger.fine("Starting Minecraft...");
                Logger.fine(fullCMD);
                Process process = new ProcessBuilder(cmd).directory(this.packInfo.getGameDir()).redirectErrorStream(true).start();
                if (process != null) {
                    Logger.fine("Minecraft started!");
                    new MinecraftProcess(process);
                    StatusFrame.INSTANCE.showGUI(false);
                    // new Thread(new GameWatcher(process)).start();
                } else {
                    // clear all...
                    Launcher.onError();

                    // show GUIs
                    StatusFrame.INSTANCE.showGUI(false);
                    MainFrame.INSTANCE.showFrame(true);

                    // some output...
                    Logger.error("Could not start Minecraft!");
                    JOptionPane.showMessageDialog(null, "Could not start Minecraft...", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // show GUIs
                StatusFrame.INSTANCE.showGUI(false);
                MainFrame.INSTANCE.showFrame(true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void onError() {
        // clear all...
        Asset.reset();
        Library.clearLibrarys();
        Main.clearHTTPs();
        INSTANCE.error = true;
    }
}
