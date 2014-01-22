package de.gemo.smartlauncher.launcher.units;

public class DownloadInfo {
    private int libraryCount = 0;
    private int assetCount = 0;
    private boolean downloadAssetJSON = false;
    private boolean downloadMCJar = false;

    public int getLibraryCount() {
        return libraryCount;
    }

    public void setLibraryCount(int libraryCount) {
        this.libraryCount = libraryCount;
    }

    public int getAssetCount() {
        return assetCount;
    }

    public void setAssetCount(int assetCount) {
        this.assetCount = assetCount;
    }

    public boolean isDownloadMCJar() {
        return downloadMCJar;
    }

    public void setDownloadMCJar(boolean downloadMCJar) {
        this.downloadMCJar = downloadMCJar;
    }

    public boolean isDownloadAssetJSON() {
        return downloadAssetJSON;
    }

    public void setDownloadAssetJSON(boolean downloadAssetJSON) {
        this.downloadAssetJSON = downloadAssetJSON;
    }

}
