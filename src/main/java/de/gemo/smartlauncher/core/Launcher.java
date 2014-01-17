package de.gemo.smartlauncher.core;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.eclipsesource.json.JsonObject;

import de.gemo.smartlauncher.frames.StatusFrame;
import de.gemo.smartlauncher.internet.DownloadAction;
import de.gemo.smartlauncher.internet.Worker;
import de.gemo.smartlauncher.listener.MCDownloadFileListener;
import de.gemo.smartlauncher.listener.MCJsonDownloadListener;
import de.gemo.smartlauncher.units.Asset;
import de.gemo.smartlauncher.units.AuthData;
import de.gemo.smartlauncher.units.GameInfo;
import de.gemo.smartlauncher.units.Library;
import de.gemo.smartlauncher.units.Pack;
import de.gemo.smartlauncher.units.VARS;

public class Launcher {
    public static Launcher INSTANCE;

    private final Pack pack;
    private final String version;
    private File gameDir, nativesDir, assetsDir;
    private final GameInfo gameInfo;
    private String mainClass = null, mcArguments = null;

    public Launcher(Pack pack, String version) {
        INSTANCE = this;
        this.pack = pack;
        this.version = version.replaceAll(" - recommended", "").trim();
        this.gameInfo = new GameInfo(this.version);
        this.launch();
    }

    public GameInfo getGameInfo() {
        return gameInfo;
    }

    public void setMCArguments(String mcArguments) {
        if (mcArguments != null) {
            mcArguments = mcArguments.replaceAll("\"", "");
        }
        this.mcArguments = mcArguments;
    }

    public void setMainClass(String mainClass) {
        if (mainClass != null) {
            mainClass = mainClass.replaceAll("\"", "");
        }
        this.mainClass = mainClass;
    }

    private void launch() {
        StatusFrame.INSTANCE.setText("Preparing download...");
        StatusFrame.INSTANCE.showGUI(true);
        Main.appendWorker(new Worker(new DownloadAction(VARS.URL.getString(VARS.URL.JSON.MC_VERSIONS, gameInfo), VARS.DIR.VERSIONS + "/" + this.version + "/", this.version + ".json"), new MCJsonDownloadListener(this.version, this.version + ".json")));
        Main.appendWorker(new Worker(new DownloadAction(VARS.URL.getString(VARS.URL.FILES.MC_JAR, gameInfo), VARS.DIR.VERSIONS + "/" + this.version + "/", this.version + ".jar"), new MCDownloadFileListener(this.version + ".jar")));
        Main.startThread();
    }

    private void createDirs() {
        AuthData authData = Main.authData;

        // create gamedir
        this.gameDir = new File(VARS.DIR.PROFILES + "/" + authData.getMCUserName() + "/" + this.pack.getPackName() + "/" + this.version + "/");

        // create assetsdir
        this.assetsDir = new File(VARS.DIR.ASSETS + "/");
        this.assetsDir.mkdir();

        // clear nativesDir & recreate it
        this.nativesDir = new File(this.gameDir, "natives/");
        if (this.nativesDir.exists()) {
            this.nativesDir.delete();
        }
        this.nativesDir.mkdirs();
    }

    private void extractLibraries() throws IOException {
        ArrayList<Library> libraryList = Library.getLibraryList();
        for (Library library : libraryList) {
            // only extract...
            if (!library.isAllow() || !library.isExtract()) {
                continue;
            }

            this.unpack(library);
        }
    }

    private void unpack(Library library) throws IOException {
        File file = new File(library.getDir(), library.getFileName());
        ZipFile zip = new ZipFile(file);
        try {
            Enumeration<? extends ZipEntry> entries = zip.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();

                if (!library.shouldExtract(entry.getName())) {
                    continue;
                }
                File targetFile = new File(this.nativesDir, entry.getName());
                if (targetFile.getParentFile() != null) {
                    targetFile.getParentFile().mkdirs();
                }

                if (!entry.isDirectory()) {
                    BufferedInputStream inputStream = new BufferedInputStream(zip.getInputStream(entry));

                    byte[] buffer = new byte[2048];
                    FileOutputStream outputStream = new FileOutputStream(targetFile);
                    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
                    try {
                        int length;
                        while ((length = inputStream.read(buffer, 0, buffer.length)) != -1) {
                            bufferedOutputStream.write(buffer, 0, length);
                        }
                    } finally {
                        bufferedOutputStream.close();
                    }
                }
            }
        } finally {
            zip.close();
        }
    }

    public static void startGame() throws IOException {
        StatusFrame.INSTANCE.setText("Creating directories...");
        INSTANCE.createDirs();
        StatusFrame.INSTANCE.setText("Extracting libraries...");
        INSTANCE.extractLibraries();
        StatusFrame.INSTANCE.setText("Reconstructing assets...");

        if (INSTANCE.copyAssets()) {
            StatusFrame.INSTANCE.setText("Starting game...");
            INSTANCE.launchGame();
        }
    }

    private boolean copyAssets() {
        File legacyFolder = new File(VARS.DIR.ASSETS + "\\virtual\\legacy\\");
        // delete old folder...
        if (legacyFolder.exists()) {
            legacyFolder.delete();
        }

        // recreate it...
        try {
            legacyFolder.mkdirs();

            File legacyFile = new File(VARS.DIR.ASSETS + "\\indexes\\", this.gameInfo.getAssetVersion() + ".json");
            BufferedReader reader = new BufferedReader(new FileReader(legacyFile));
            JsonObject json = JsonObject.readFrom(reader);

            json = json.get("objects").asObject();

            // copy every asset
            File appDir = new File("");
            for (String objectName : json.names()) {
                JsonObject singleAsset = json.get(objectName).asObject();
                Asset asset = new Asset(objectName.replaceAll("\"", ""), singleAsset.get("hash").asString().replaceAll("\"", ""), singleAsset.get("size").asInt());
                if (!asset.isFileValid()) {
                    return false;
                }
                File dir = new File(VARS.DIR.ASSETS + "/objects/" + asset.getHashFolder());
                File assetFile = new File(dir, asset.getHash());

                File newFile = new File(VARS.DIR.ASSETS + "\\virtual\\legacy\\", objectName.replaceAll("\"", ""));
                newFile.mkdirs();
                Files.copy(assetFile.toPath(), newFile.toPath(), REPLACE_EXISTING);
            }

            reader.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void launchGame() {
        ArrayList<String> cmd = new ArrayList<String>();

        cmd.add("java");
        cmd.add("-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump");
        cmd.add("-Xmx1G");

        // cmd.add("-jar");
        // cmd.add(new File("").getAbsolutePath() + "\\versions\\" +
        // this.version + "\\" + this.version + ".jar");
        cmd.add("-Djava.library.path=" + this.nativesDir.getAbsolutePath());
        cmd.add("-cp");
        File libDir = new File(VARS.DIR.LIBRARIES, "");
        String libs = "";
        ArrayList<Library> libraryList = Library.getLibraryList();
        for (int index = 1; index <= libraryList.size(); index++) {
            Library library = libraryList.get(index - 1);
            if (!library.isAllow() || library.isExtract()) {
                continue;
            }
            libs += libDir.getAbsolutePath() + "\\" + library.getStartFile() + ";";
        }
        libs += (VARS.DIR.VERSIONS + "\\" + this.version + "\\" + this.version + ".jar");
        cmd.add(libs);
        cmd.add(mainClass);

        cmd.addAll(this.getMCArguments());
        try {
            String fullCMD = "";
            for (String cm : cmd) {
                fullCMD += cm + " ";
            }

            Logger.info("Starting Minecraft...");
            Logger.info(fullCMD);
            new ProcessBuilder(cmd).directory(this.gameDir).redirectErrorStream(true).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<String> getMCArguments() {
        ArrayList<String> args = new ArrayList<String>();

        String[] split = this.mcArguments.split(" ");

        AuthData authData = Main.authData;
        for (int index = 0; index < split.length; index += 2) {
            String current = split[index];
            String next = "";
            if (current.equalsIgnoreCase("--username")) {
                next = authData.getMCUserName();
            }
            if (current.equalsIgnoreCase("--version")) {
                next = this.version;
            }
            if (current.equalsIgnoreCase("--gameDir")) {
                next = this.gameDir.getAbsolutePath();
            }
            if (current.equalsIgnoreCase("--assetsDir")) {
                next = this.assetsDir.getAbsolutePath();
                if (this.gameInfo.isAssetsVirtual()) {
                    next += "\\virtual\\legacy\\";
                }
            }
            if (current.equalsIgnoreCase("--assetIndex")) {
                next = this.gameInfo.getAssetVersion();
            }
            if (current.equalsIgnoreCase("--uuid")) {
                next = authData.getProfileID();
            }
            if (current.equalsIgnoreCase("--accessToken")) {
                next = authData.getAccessToken();
            }
            if (current.equalsIgnoreCase("--userProperties")) {
                next = "{}";
            }
            if (current.equalsIgnoreCase("--userType")) {
                next = "mojang";
            }
            if (next.length() > 0) {
                args.add(current);
                args.add(next);
            }
        }
        return args;
    }
}
