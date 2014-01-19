package de.gemo.smartlauncher.units;

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

import de.gemo.smartlauncher.core.Main;

public class PackInfo {

    private final String packVersion;
    private String mainClass = null, mcArguments = null;
    private String assetVersion = "TEST", gameVersion = "";
    private File gameDir, nativesDir, assetsDir;
    private final Pack pack;

    private boolean overrideMainClass = false;
    private boolean overrideMCArguments = false;

    public PackInfo(String packVersion, Pack pack) {
        this.packVersion = packVersion.replaceAll("\"", "");
        this.pack = pack;
        this.createDirs();
    }

    private void createDirs() {
        AuthData authData = Main.authData;

        // create gamedir
        this.gameDir = new File(VARS.DIR.PROFILES + "/" + authData.getMCUserName() + "/" + this.pack.getPackName() + "/" + this.packVersion + "/");

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

    public void setGameVersion(String gameVersion) {
        this.gameVersion = gameVersion;
    }

    public String getGameVersion() {
        return gameVersion;
    }

    public String getPackVersion() {
        return packVersion;
    }

    public void setAssetVersion(String assetVersion) {
        this.assetVersion = assetVersion;
    }

    public boolean isAssetsVirtual() {
        return assetVersion.equalsIgnoreCase("legacy");
    }

    public String getAssetVersion() {
        return assetVersion;
    }

    public void setMainClass(String mainClass) {
        if (this.overrideMainClass) {
            return;
        }
        if (mainClass != null) {
            mainClass = mainClass.replaceAll("\"", "");
        }
        this.mainClass = mainClass;
    }

    public void overrideMainClass(String mainClass) {
        if (mainClass != null) {
            mainClass = mainClass.replaceAll("\"", "");
        }
        this.mainClass = mainClass;
        this.overrideMainClass = true;
    }

    public String getMainClass() {
        return mainClass;
    }

    public void setMCArguments(String mcArguments) {
        if (this.overrideMCArguments) {
            return;
        }
        if (mcArguments != null) {
            mcArguments = mcArguments.replaceAll("\"", "");
        }
        this.mcArguments = mcArguments;
    }

    public void overrideMCArguments(String mcArguments) {
        if (mcArguments != null) {
            mcArguments = mcArguments.replaceAll("\"", "");
        }
        this.mcArguments = mcArguments;
        this.overrideMCArguments = true;
    }

    public ArrayList<String> getMCArguments() {
        ArrayList<String> args = new ArrayList<String>();
        String[] split = this.mcArguments.split(" ");

        AuthData authData = Main.authData;
        for (int index = 0; index < split.length; index += 2) {
            String current = split[index];
            String next = split[index + 1];
            if (current.equalsIgnoreCase("--username")) {
                next = authData.getMCUserName();
            }
            if (current.equalsIgnoreCase("--version")) {
                next = this.gameVersion;
            }
            if (current.equalsIgnoreCase("--gameDir")) {
                next = "\"" + this.gameDir.getAbsolutePath() + "\"";
            }
            if (current.equalsIgnoreCase("--assetsDir")) {
                next = "\"" + this.assetsDir.getAbsolutePath();
                if (this.isAssetsVirtual()) {
                    next += "/virtual/legacy";
                }
                next += "\"";
            }
            if (current.equalsIgnoreCase("--assetIndex")) {
                next = this.getAssetVersion();
            }
            if (current.equalsIgnoreCase("--uuid")) {
                next = authData.getProfileID();
            }
            if (current.equalsIgnoreCase("--accessToken")) {
                next = authData.getAccessToken();
            }
            if (current.equalsIgnoreCase("--session")) {
                next = authData.getAccessToken() + ":" + authData.getProfileID();
            }
            if (current.equalsIgnoreCase("--userProperties")) {
                next = "{}";
            }
            if (current.equalsIgnoreCase("--userType")) {
                next = "mojang";
            }

            if (next.length() > 0) {
                args.add(current);
                args.add(next.replaceAll("/", "\\\\"));
            }
        }
        return args;
    }

    public void unpackNatives(Library library) throws IOException {
        File file = new File(library.getDir(), library.getFileName());
        ZipFile zip = new ZipFile(file);
        try {
            Enumeration<? extends ZipEntry> entries = zip.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();

                if (!library.shouldExtract(entry.getName())) {
                    continue;
                }
                File targetFile = new File(this.getNativesDir(), entry.getName());
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

    public boolean reconstructAssets() {
        File legacyFolder = new File(VARS.DIR.ASSETS + "\\virtual\\legacy\\");
        // delete old folder...
        if (legacyFolder.exists()) {
            legacyFolder.delete();
        }

        // recreate it...
        try {
            legacyFolder.mkdirs();

            File legacyFile = new File(VARS.DIR.ASSETS + "\\indexes\\", this.getAssetVersion() + ".json");
            BufferedReader reader = new BufferedReader(new FileReader(legacyFile));
            JsonObject json = JsonObject.readFrom(reader);

            json = json.get("objects").asObject();

            // copy every asset
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

    public File getGameDir() {
        return gameDir;
    }

    public File getAssetsDir() {
        return assetsDir;
    }

    public File getNativesDir() {
        return nativesDir;
    }

}
