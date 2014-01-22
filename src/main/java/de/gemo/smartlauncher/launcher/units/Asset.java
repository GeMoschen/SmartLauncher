package de.gemo.smartlauncher.launcher.units;

import java.io.File;

import de.gemo.smartlauncher.universal.units.VARS;

public class Asset {
    private static int assetsToLoad = 0;

    public static synchronized void incrementAssetsToLoad() {
        assetsToLoad = assetsToLoad + 1;
    }

    public static synchronized void decrementAssetsToLoad() {
        assetsToLoad = assetsToLoad - 1;
    }

    public static synchronized int getAssetsToLoad() {
        return assetsToLoad;
    }

    public static synchronized void reset() {
        assetsToLoad = 0;
    }

    private final String path, hash, hashFolder;
    private final int fileSize;

    public Asset(String path, String hash, int fileSize) {
        this.path = path;
        this.hash = hash;
        this.hashFolder = hash.substring(0, 2);
        this.fileSize = fileSize;
    }

    public String getPath() {
        return path;
    }

    public String getHash() {
        return hash;
    }

    public String getHashFolder() {
        return hashFolder;
    }

    public int getFileSize() {
        return fileSize;
    }

    public String getAssetPath() {
        return VARS.DIR.ASSETS + "/" + this.getURL();
    }

    public String getURL() {
        return this.hashFolder + "/" + this.hash;
    }

    public String getHashedFilePath() {
        return VARS.DIR.ASSETS + "/objects/" + this.hashFolder;
    }

    public boolean isFileValid() {
        File dir = new File(VARS.DIR.ASSETS + "/objects/" + this.hashFolder);
        File file = new File(dir, this.hash);
        return file.exists() && file.length() == this.fileSize;
    }

}
