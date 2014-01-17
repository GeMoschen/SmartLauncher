package de.gemo.smartlauncher.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import de.gemo.smartlauncher.frames.MainFrame;
import de.gemo.smartlauncher.frames.StatusFrame;
import de.gemo.smartlauncher.internet.DownloadAction;
import de.gemo.smartlauncher.internet.Worker;
import de.gemo.smartlauncher.listener.MCJsonDownloadListener;
import de.gemo.smartlauncher.units.Asset;
import de.gemo.smartlauncher.units.GameInfo;
import de.gemo.smartlauncher.units.Library;
import de.gemo.smartlauncher.units.Pack;
import de.gemo.smartlauncher.units.VARS;

public class Launcher {

    public static Launcher INSTANCE;

    public static GameInfo getGameInfo() {
        return INSTANCE.gameInfo;
    }

    private final Pack pack;
    private final String version;
    private final GameInfo gameInfo;

    public Launcher(Pack pack, String version) {
        INSTANCE = this;
        this.pack = pack;
        this.version = version.replaceAll(" - recommended", "").trim();
        this.gameInfo = new GameInfo(this.version, this.pack);
        this.launch();
    }

    private void launch() {
        StatusFrame.INSTANCE.setText("Preparing download...");
        StatusFrame.INSTANCE.showGUI(true);
        Main.appendWorker(new Worker(new DownloadAction(VARS.getString(VARS.URL.JSON.MC_VERSIONS, gameInfo), VARS.DIR.VERSIONS + "/" + this.version + "/", this.version + ".json"), new MCJsonDownloadListener(this.version, this.version + ".json")));
        Main.startThread();
    }

    private void extractLibraries() throws IOException {
        ArrayList<Library> libraryList = Library.getLibraryList();
        for (Library library : libraryList) {
            // only extract...
            if (!library.isAllow() || !library.isExtract()) {
                continue;
            }

            this.gameInfo.unpackNatives(library);
        }
    }

    public static void startGame() throws IOException {
        // create dirs...
        StatusFrame.INSTANCE.setText("Creating directories...");
        Logger.fine("Creating directories...");
        INSTANCE.gameInfo.createDirs();

        // extract libraries...
        StatusFrame.INSTANCE.setText("Extracting libraries...");
        Logger.fine("Extracting libraries...");
        INSTANCE.extractLibraries();

        // reconstruct assets...
        StatusFrame.INSTANCE.setText("Reconstructing assets...");
        Logger.fine("Reconstructing assets...");
        if (INSTANCE.gameInfo.reconstructAssets()) {
            // ... and finally start minecraft
            StatusFrame.INSTANCE.setText("Starting game...");
            INSTANCE.launchGame();
        }
    }

    private String createClasspathArgument() {
        File libraryDir = new File(VARS.DIR.LIBRARIES, "");

        String libraries = "";
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
        libraries += (VARS.DIR.VERSIONS + "\\" + this.version + "\\" + this.version + ".jar");

        // replace "/" with "\"...
        libraries = libraries.replaceAll("/", "\\\\");
        return libraries;
    }

    private void launchGame() {
        ArrayList<String> cmd = new ArrayList<String>();

        // standard...
        cmd.add("java");
        cmd.add("-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump");

        // some extra-info
        cmd.add("-Xmx1G");

        // append nativesdir...
        cmd.add("-Djava.library.path=" + this.gameInfo.getNativesDir().getAbsolutePath());

        // append classpath
        cmd.add("-cp");
        cmd.add(this.createClasspathArgument());

        // append mainclass
        cmd.add(this.gameInfo.getMainClass());

        // append arguments needed by minecraft...
        cmd.addAll(this.gameInfo.getMCArguments());

        // ... and finally, try to launch...
        try {
            String fullCMD = "";
            for (String cm : cmd) {
                fullCMD += cm + " ";
            }

            Logger.fine("Starting Minecraft...");
            Logger.fine(fullCMD);
            Process process = new ProcessBuilder(cmd).directory(this.gameInfo.getGameDir()).redirectErrorStream(true).start();
            if (process != null) {
                Logger.fine("Minecraft started!");
            } else {
                // clear all...
                Asset.reset();
                Library.clearLibrarys();
                Main.clearHTTPs();

                // some output...
                Logger.error("Could not start Minecraft!");
                JOptionPane.showMessageDialog(null, "Could not start Minecraft...", "Error", JOptionPane.ERROR_MESSAGE);

                // show GUIs
                StatusFrame.INSTANCE.showGUI(false);
                MainFrame.CORE.showFrame(true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
