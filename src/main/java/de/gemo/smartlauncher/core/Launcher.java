package de.gemo.smartlauncher.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JOptionPane;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import de.gemo.smartlauncher.core.minecraft.MinecraftProcess;
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

        // SOME INFO
        StatusFrame.INSTANCE.showFrame(true);
        MainFrame.INSTANCE.showFrame(false);
        StatusFrame.INSTANCE.setText("Preparing launch...");

        // start...
        AuthData authData = Main.authData;
        GetSinglePackListener listener = new GetSinglePackListener();
        File packJson = new File(VARS.DIR.PROFILES + "/" + authData.getMCUserName() + "/" + this.pack.getPackName() + "/" + this.packInfo.getPackVersion() + "/pack.json");
        File packFile = new File(VARS.DIR.PACKS + "/" + this.pack.getPackName(), this.pack.getPackName() + "-" + this.packInfo.getPackVersion() + ".zip");
        String packURL = (VARS.URL.PACKSERVER + "packs/" + this.pack.getPackName() + "/" + this.pack.getPackName() + "-" + this.packInfo.getPackVersion() + ".zip");
        if (!packJson.exists() || !packFile.exists()) {
            if (!packFile.exists()) {
                StatusFrame.INSTANCE.setText("downloading packfile...");
                Logger.info("Packfile is missing... downloading pack...");
                Main.appendWorker(new Worker(new DownloadAction(packURL, VARS.DIR.PACKS + "/" + this.pack.getPackName(), this.pack.getPackName() + "-" + this.packInfo.getPackVersion() + ".zip"), listener));
                Main.startThread();
            } else {
                if (!this.extractPack()) {
                    StatusFrame.INSTANCE.setText("downloading packfile...");
                    Logger.info("Packfile is invalid... downloading pack...");
                    Main.appendWorker(new Worker(new DownloadAction(packURL, VARS.DIR.PACKS + "/" + this.pack.getPackName(), this.pack.getPackName() + "-" + this.packInfo.getPackVersion() + ".zip"), listener));
                    Main.startThread();
                } else {
                    Logger.fine("Packfile is valid...");
                    Launcher.INSTANCE.launchPack();
                }
            }
        } else {
            if (!this.handlePackJson(packJson)) {
                Logger.info("pack.json is invalid... redownloading pack...");
                Main.appendWorker(new Worker(new DownloadAction(packURL, VARS.DIR.PACKS + "/" + this.pack.getPackName(), this.pack.getPackName() + "-" + this.packInfo.getPackVersion() + ".zip"), listener));
                Main.startThread();
            } else {
                Logger.fine("Pack is valid...");
                Launcher.INSTANCE.launchPack();
            }
        }
    }

    public boolean extractPack() {
        try {
            StatusFrame.INSTANCE.setText("extracting packfile...");
            InputStream inputStream = new FileInputStream(new File(VARS.DIR.PACKS + "/" + this.pack.getPackName(), this.pack.getPackName() + "-" + this.packInfo.getPackVersion() + ".zip"));
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(inputStream));
            ZipEntry entry;

            String dir = VARS.DIR.PROFILES + "/" + Main.authData.getMCUserName() + "/" + this.pack.getPackName() + "/" + this.packInfo.getPackVersion() + "/";
            while ((entry = zis.getNextEntry()) != null) {
                // modify entryName...
                String entryName = entry.getName();
                entryName = entryName.replaceFirst(this.pack.getPackName() + "/", "");
                entryName = entryName.replaceFirst("minecraft/", "");

                // create dirs...
                if (entry.isDirectory()) {
                    if (entryName.length() < 1) {
                        continue;
                    }
                    File file = new File(dir + entryName);
                    file.mkdirs();
                    continue;
                }

                entryName = (dir + entryName).replaceAll("/", "\\\\");

                // extract file...
                int size;
                byte[] buffer = new byte[2048];
                File file = new File(entryName);

                FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fos, buffer.length);

                while ((size = zis.read(buffer, 0, buffer.length)) != -1) {
                    bos.write(buffer, 0, size);
                }
                bos.flush();
                bos.close();
                if (entryName.endsWith("pack.json")) {
                    if (!this.handlePackJson(entryName)) {
                        return false;
                    }
                }
            }
            zis.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean handlePackJson(String fileName) {
        return this.handlePackJson(new File(fileName));
    }

    private boolean handlePackJson(File file) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            JsonObject json = JsonObject.readFrom(reader);

            // get minecraft-version
            JsonValue jsonValue = json.get("id");
            if (jsonValue == null) {
                return false;
            }
            Launcher.getPackInfo().setGameVersion(jsonValue.asString().replaceAll("\"", ""));

            // override mainClass...
            jsonValue = json.get("mainClass");
            if (jsonValue != null) {
                Launcher.getPackInfo().overrideMainClass(jsonValue.toString());
            }

            // override mcArguments
            jsonValue = json.get("minecraftArguments");
            if (jsonValue != null) {
                Launcher.getPackInfo().overrideMCArguments(jsonValue.toString());
            }

            // get libraries...
            jsonValue = json.get("libraries");
            if (jsonValue != null) {
                JsonArray jsonLib = jsonValue.asArray();
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

            // close stream
            reader.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void launchPack() {
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
                    Logger.info("Some files are missing...");
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
            StatusFrame.INSTANCE.showFrame(true);
            Logger.info("Preparing launch...");

            // extract libraries...
            StatusFrame.INSTANCE.setText("Extracting libraries...");
            Logger.info("Extracting libraries...");
            INSTANCE.extractLibraries();

            // reconstruct assets...
            StatusFrame.INSTANCE.setText("Reconstructing assets...");
            Logger.info("Reconstructing assets...");
            if (INSTANCE.packInfo.reconstructAssets()) {
                // ... and finally start minecraft
                INSTANCE.launchGame();
            }
        } else {
            // show GUIs
            StatusFrame.INSTANCE.showFrame(false);
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

                Logger.info("Starting Minecraft...");
                StatusFrame.INSTANCE.setText("Starting Minecraft...");
                Logger.info(fullCMD);
                Process process = new ProcessBuilder(cmd).directory(this.packInfo.getGameDir()).redirectErrorStream(true).start();
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
            } else {
                // show GUIs
                StatusFrame.INSTANCE.showFrame(false);
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
